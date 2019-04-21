package main

import (
	"database/sql"
	"fmt"
	"net/http"
	"strconv"
	"strings"

	"github.com/gorilla/csrf"
	"github.com/gorilla/mux"
)

// navbar holds info for templates on what navbar tabs should be displayed
type navbar struct {
	Admin   bool
	Sup     bool
	Analyst bool
}

// Index holds info for rendering the driver's index page
type Index struct {
	Types []string
}

// Auto holds info for rendering the autogen template
type Auto struct {
	DPMS []dpmDriver
}

// Approval holds first and last name as well as the point value for each non approved DPM in the db
type Approval struct {
	Name   string
	Points string
}

// Renders the index page
func (c Controller) renderIndexPage(w http.ResponseWriter, r *http.Request) {
	// Validate user
	sender, err := c.getUser(w, r)
	// If not signed in, redirect them
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// If they have not changed their temp password, redirect
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	var (
		dpmtype string
		points int
	)
	// Get the type and points of each DPM a user has
	// Need to get approved DPMs that are not being ignored
	stmt := `SELECT dpmtype, points FROM dpms WHERE userid=$1 AND approved=true AND ignored=false AND created > now() - interval '6 months' ORDER BY created DESC`
	ss := make([]string, 0)
	// Make query
	rows, err := c.db.Query(stmt, sender.ID)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	defer rows.Close()
	for rows.Next() {
		err = rows.Scan(&dpmtype, &points)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		var pointString string
		switch {
		case points == 0, points < -1:
			pointString = fmt.Sprintf("(%d Points)", points)
		case points == -1:
			pointString = fmt.Sprintf("(%d Point)", points)
		case points == 1:
			pointString = fmt.Sprintf("(+%d Point)", points)
		default:
			pointString = fmt.Sprintf("(+%d Points)", points)
		}
		// Add space to dpmtype to make string manipulation easier
		dpmtype += " "
		// Gets letter of DPM, eg. G
		letter := fmt.Sprintf("%s", dpmtype[5:6])
		// Gets the part of dpm past Type[G]:, but minus the points in parenthesis
		description := strings.Trim(strings.Replace(dpmtype[8:len(dpmtype)-12], "(", "", -1), " ")
		out := fmt.Sprintf("Type %s: %s %s", letter, description, pointString)
		ss = append(ss, out)
	}
 	// Struct to allow navbar to only show tabs user is allowed to see
	n := navbar{
		Admin:   sender.Admin,
		Sup:     sender.Sup,
		Analyst: sender.Analyst,
	}
	// Render index.gohtml template
	err = c.tpl.ExecuteTemplate(w, "index.gohtml", map[string]interface{}{
		"Nav": n, "Types": ss,
	})
	if err != nil {
		out := fmt.Sprintln("Uh-Oh")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// Renders the dpm page
func (c Controller) renderDPM(w http.ResponseWriter, r *http.Request) {
	// Verify user has access to this
	sender, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	if !sender.Admin && !sender.Sup && !sender.Analyst {
		w.WriteHeader(http.StatusUnauthorized)
		return
	}
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	n := navbar{
		Admin:   sender.Admin,
		Sup:     sender.Sup,
		Analyst: sender.Analyst,
	}
	// Render html
	err = c.tpl.ExecuteTemplate(w, "dpm.gohtml", map[string]interface{}{
		"Nav": n, "csrf": csrf.TemplateField(r),
	})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// Renders the create user page
func (c Controller) renderCreateUser(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// user needs to be admin or sup to do this
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// if user has not changed password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	n := navbar{
		Admin:   u.Admin,
		Analyst: u.Analyst,
		Sup:     u.Sup,
	}
	// Render createuser template
	err = c.tpl.ExecuteTemplate(w, "createUser.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r), "Nav": n})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
	}
}

// Renders the login page
func (c Controller) renderLogin(w http.ResponseWriter, r *http.Request) {
	// If logged in, redirect to index route
	_, err := c.getUser(w, r)
	if err == nil {
		http.Redirect(w, r, "/", http.StatusFound)
		return
	}
	// Render login template
	err = c.tpl.ExecuteTemplate(w, "login.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// Renders the html for when a user wants to change password
func (c Controller) renderChangeUserPassword(w http.ResponseWriter, r *http.Request) {
	// Validate user
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect them
	if err != nil {
		c.renderLogin(w, r)
		return
	}
	// Disallows non admin users who have changed their password from seeing this page
	// This makes it so users can only change their password if an admin makes the request
	if u.Changed && !u.Admin {
		c.renderIndexPage(w, r)
		return
	}
	// Render changepass template
	err = c.tpl.ExecuteTemplate(w, "changePass.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

func (c Controller) renderResetPassword(w http.ResponseWriter, r *http.Request) {
	// Validate user
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect them
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// Only admins should be able to reset passwords
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// If user has not changed off temp password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Render changepass template
	err = c.tpl.ExecuteTemplate(w, "resetPassword.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// renderAutoGen will parse the current day's when2work schedule and call a function which will autogenerate dpms from the data
// This function returns a slice of DPMDriver's that will be placed as data input for the template for the sup/admin to check for accuracy
// If the autogen function returns an error,
func (c Controller) renderAutoGen(w http.ResponseWriter, r *http.Request) {
	// Validate user
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect them
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// No regular users can access this
	if !u.Admin && !u.Sup && !u.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// If user has not changed off temp password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Assign values to navbar struct
	n := navbar{
		Admin:   u.Admin,
		Sup:     u.Sup,
		Analyst: u.Analyst,
	}
	// Call autogen and get slice out
	dpms, err := autoGen()
	// If error, render the autogenErr template stating this
	if err != nil {
		auto := err.Error()
		err = c.tpl.ExecuteTemplate(w, "autogenErr.gohtml", map[string]interface{}{
			"Nav": n, "Err": auto,
		})
		if err != nil {
			out := fmt.Sprintln("Something went wrong, please try again")
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			_, _ = w.Write([]byte(out))
			return
		}
		return
	}

	// Render autogen template and pass in data
	err = c.tpl.ExecuteTemplate(w, "autogen.gohtml", map[string]interface{}{
		"Nav": n, "dpms": dpms, "csrf": csrf.TemplateField(r),
	})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// RenderApprovals gives data to and renders the approvals template
func (c Controller) renderApprovals(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	if !u.Admin && !u.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	var (
		stmt string
		rows *sql.Rows
	)
	// If user is analyst, a different query is needed
	if u.Admin {
		// Query that gets name and point value for each dpm
		stmt = `SELECT firstname, lastname, points FROM dpms WHERE approved=false AND ignored=false ORDER BY created DESC`
	} else {
		// Get name and point value of each dpm whose manager is this person
		stmt = `SELECT a.firstname, a.lastname, a.points FROM dpms a
		JOIN users b ON b.id=a.userid
		WHERE approved=false AND ignored=false AND managerid=$1 ORDER BY created DESC`
	}
	if u.Admin {
		rows, err = c.db.Query(stmt)
	} else {
		// If they are an analyst, I need to pass their id into the query
		rows, err = c.db.Query(stmt, u.ID)
	}
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	defer rows.Close()
	list := make([]Approval, 0)
	// Variables for use in the loop
	var points, firstname, lastname string
	// Iterate over returned rows
	for rows.Next() {
		// Place returned values in variables
		err = rows.Scan(&firstname, &lastname, &points)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		// Make sure points has '+' in front for positive values
		if string(points[0]) != "-" {
			points = "+" + points
		}
		// Create struct to pass into slice
		a := Approval{
			Name:   firstname + " " + lastname,
			Points: points,
		}
		list = append(list, a)
	}
	// Create navbar struct in order to render html header tabs correctly
	n := navbar{
		Admin:   u.Admin,
		Sup:     u.Sup,
		Analyst: u.Analyst,
	}
	// Render approvals template and pass in data
	err = c.tpl.ExecuteTemplate(w, "approvals.gohtml", map[string]interface{}{
		"List": list, "Nav": n, "csrf": csrf.TemplateField(r),
	})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// renderDataPage renders the data page template
func (c Controller) renderDataPage(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	if !u.Admin && !u.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
	}
	n := navbar{
		Admin:   u.Admin,
		Sup:     u.Sup,
		Analyst: u.Analyst,
	}
	// Render data page and pass in data
	err = c.tpl.ExecuteTemplate(w, "data.gohtml", map[string]navbar{
		"Nav": n,
	})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// renderFindUser renders this template, no data passed in
func (c Controller) renderFindUser(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// user needs to be admin or sup to do this
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// if user has not changed password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Render finduser template
	err = c.tpl.ExecuteTemplate(w, "findUser.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
	}
}

// renderEditUser renders and fills out the userPage template
func (c Controller) renderEditUser(w http.ResponseWriter, r *http.Request) {
	// redirect if not correct domain
	v := redirect(w, r)
	if v {
		return
	}
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// user needs to be admin
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// if user has not changed password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Get user id from the url and convert it to an int
	vars := mux.Vars(r)
	id, err := strconv.Atoi(vars["id"])
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// user struct to put data into
	foundUser := user{}
	// These will get the name of the associated manager
	var managerFirst, managerLast string
	var manageID int16
	type manage struct {
		Name string
		ID   int16
	}
	m := manage{}
	// This will hold all of the managers that exist
	managerSlice := make([]manage, 0)
	var roles []string
	// Find user and put data into the struct
	stmt := `SELECT * FROM users WHERE id=$1`
	err = c.db.QueryRowx(stmt, id).StructScan(&foundUser)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Get the first name, last name of the driver's manager
	stmt = `SELECT firstname, lastname FROM users WHERE id=$1`
	err = c.db.QueryRow(stmt, foundUser.ManagerID).Scan(&managerFirst, &managerLast)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	m.Name = fmt.Sprintf("%s %s", managerFirst, managerLast)
	m.ID = foundUser.ManagerID
	managerSlice = append(managerSlice, m)
	// Find all the managers/admins in the database, except the one we already have
	stmt = `SELECT id, firstname, lastname FROM users WHERE (analyst=true OR admin=true) AND id!=$1`
	rows, err := c.db.Query(stmt, foundUser.ManagerID)
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Iterate over returned managers
	for rows.Next() {
		err = rows.Scan(&manageID, &managerFirst, &managerLast)
		if err != nil {
			fmt.Println(err)
		}
		// Add managers to slice
		m.Name = fmt.Sprintf("%s %s", managerFirst, managerLast)
		m.ID = manageID
		managerSlice = append(managerSlice, m)
	}
	points := fmt.Sprintf("%v", foundUser.Points)
	// Get role list in correct order
	if foundUser.Admin {
		roles = []string{"Admin", "Manager", "Driver", "Supervisor"}
	} else if foundUser.Analyst {
		roles = []string{"Manager", "Admin", "Driver", "Supervisor"}
	} else if foundUser.Sup {
		roles = []string{"Supervisor", "Admin", "Manager", "Driver"}
	} else {
		roles = []string{"Driver", "Admin", "Manager", "Supervisor"}
	}

	// Pass all the data into a map
	data := map[string]interface{}{
		"csrf":      csrf.TemplateField(r),
		"username":  bm.Sanitize(foundUser.Username),
		"firstname": bm.Sanitize(foundUser.FirstName),
		"lastname":  bm.Sanitize(foundUser.LastName),
		"manager":   managerSlice,
		"role":      roles,
		"fulltime":  foundUser.FullTime,
		"points":    bm.Sanitize(points),
		"url":       r.URL.String(),
	}
	// Render userpage template
	err = c.tpl.ExecuteTemplate(w, "userpage.gohtml", data)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
}

func (c Controller) renderUserList(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// If user is not signed in, redirect
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// user needs to be admin to do this
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// if user has not changed password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Get all users
	rows, err := c.db.Query("SELECT firstname, lastname, id FROM users ORDER BY lastname")
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	defer rows.Close()
	// Slices to hold names and ids
	users := make([]map[string]string, 0)
	// Iterate through rows returned filling slices with info
	for rows.Next() {
		var (
			firstname string
			lastname  string
			id        string
		)
		err = rows.Scan(&firstname, &lastname, &id)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		m := map[string]string{
			"firstname": firstname,
			"lastname":  lastname,
			"id":        id,
		}
		users = append(users, m)
	}
	n := navbar{
		Admin:   u.Admin,
		Sup:     u.Sup,
		Analyst: u.Analyst,
	}
	data := map[string]interface{}{
		"Nav":  n,
		"user": users,
	}
	err = c.tpl.ExecuteTemplate(w, "userList.gohtml", data)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
	}
}
