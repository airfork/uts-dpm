package dpm

import (
	"fmt"
	"html/template"
	"net/http"

	"github.com/airfork/dpm_sql/models"
	csrf "github.com/gorilla/csrf"

	"github.com/airfork/webScrape"
)

// Navbar holds info for templates on what navbar tabs should be displayed
type Navbar struct {
	Admin     bool
	Sup       bool
	Analysist bool
	CSRF      template.HTML
}

// Index holds info for rendering the driver's index page
type Index struct {
	Nav    Navbar
	Points []string
}

// Auto holds info for rendering the autogen template
type Auto struct {
	Nav  Navbar
	DPMS []models.DPMDriver
	Csrf template.HTML
}

// Approval holds first and lastname as well as the point value for each non approved DPM in the db
type Approval struct {
	Name   string
	Points string
}

// ApprovalCSRF is a struct holding a list of approvals as well as a csrf input template field
type ApprovalCSRF struct {
	List []Approval
	CSRF template.HTML
}

// Renders the index page
func (c Controller) showIndexTemp(w http.ResponseWriter, r *http.Request) {
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
	// Get point value for each approved DPM the user has
	stmt := `SELECT points FROM dpms WHERE userid=$1 AND approved=true AND ignored=false ORDER BY created DESC`
	ss := make([]string, 0)
	// Make query
	rows, err := c.db.Query(stmt, sender.ID)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Make sure to close rows
	defer rows.Close()
	// Iterate over rows
	for rows.Next() {
		var points string
		err = rows.Scan(&points)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		// Make sure positive point values have '+' in front
		if string(points[0]) != "-" {
			points = "+" + points
		}
		ss = append(ss, points)
	}
	// Struct to allow navbar to only show tabs user is allowed to see
	n := Navbar{
		Admin:     sender.Admin,
		Sup:       sender.Sup,
		Analysist: sender.Analysist,
	}
	// Struct to hold navbar struct and the slice of point values
	in := Index{
		Nav:    n,
		Points: ss,
	}
	// Render index.gohtml template
	err = c.tpl.ExecuteTemplate(w, "index.gohtml", in)
	if err != nil {
		out := fmt.Sprintln("Uh-Oh")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
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
	if !sender.Admin && !sender.Sup {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	n := Navbar{
		Admin:     sender.Admin,
		Sup:       sender.Sup,
		Analysist: sender.Analysist,
		CSRF:      csrf.TemplateField(r),
	}
	// Render html
	err = c.tpl.ExecuteTemplate(w, "dpm.gohtml", n)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	return
}

// Renders the create user page
func (c Controller) renderCreateUser(w http.ResponseWriter, r *http.Request) {
	sender, err := c.getUser(w, r)
	// If user is not signed in, redirect
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// user needs to be admin or sup to do this
	if !sender.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// if user has not changed password, redirect
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	//Render createuser template
	err = c.tpl.ExecuteTemplate(w, "createUser.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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
		c.showIndexTemp(w, r)
		return
	}
	// Render changepass template
	err = c.tpl.ExecuteTemplate(w, "changePass.gohtml", map[string]interface{}{"csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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
	// Only admins and sups can autogen
	if !u.Admin && !u.Sup {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// If user has not changed off temp password, redirect
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
	}
	// Call autogen and get slice out
	dpms, err := autodpm.AutoGen()
	// If error, render the autogenErr template stating this
	if err != nil {
		err = c.tpl.ExecuteTemplate(w, "autogenErr.gohtml", nil)
		if err != nil {
			out := fmt.Sprintln("Something went wrong, please try again")
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte(out))
			return
		}
		return
	}
	// Assign values to navbar struct
	n := Navbar{
		Admin:     u.Admin,
		Sup:       u.Sup,
		Analysist: u.Analysist,
	}
	// Assign values to auto struct to be parsed in the template file
	d := Auto{
		Nav:  n,
		DPMS: dpms,
		Csrf: csrf.TemplateField(r),
	}
	// Render autogen template and pass in data
	err = c.tpl.ExecuteTemplate(w, "autogen.gohtml", d)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	return
}

// RenderApprovals gives data to and renders the approvals template
func (c Controller) RenderApprovals(w http.ResponseWriter, r *http.Request) {

	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
	}
	stmt := `SELECT firstname, lastname, points FROM dpms WHERE approved=false AND ignored=false ORDER BY created DESC`
	rows, err := c.db.Queryx(stmt)
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
	container := ApprovalCSRF{
		List: list,
		CSRF: csrf.TemplateField(r),
	}
	// Render approvals template and pass in data
	err = c.tpl.ExecuteTemplate(w, "approvals.gohtml", container)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	return
}
