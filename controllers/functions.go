package dpm

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/airfork/webScrape"
	"github.com/gorilla/mux"

	"github.com/airfork/dpm_sql/mail"
	"github.com/airfork/dpm_sql/models"
	"github.com/xlzd/gotp"
	"golang.org/x/crypto/bcrypt"
)

func (c Controller) createDPMLogic(w http.ResponseWriter, r *http.Request) {
	// Validate user
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
	// Get JSON from request body
	decoder := json.NewDecoder(r.Body)
	var d models.DPMRes
	// Parse JSON into DPMRes struct
	err = decoder.Decode(&d)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	// Turn simple DPM into full DPM
	dpm := generateDPM(&d)
	if dpm == nil {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Ensure that the user has access to this function
	stmt := `SELECT admin, sup FROM users WHERE id=$1`
	var (
		admin    bool
		sup      bool
		username string
	)
	err = c.db.QueryRow(stmt, dpm.CreateID).Scan(&admin, &sup)
	// If this
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Gets username, not actually needed for anything, but it serves the
	// role of checking to see if the first and last name in the database match the id being provided
	// This prevents DPMS from being created with non matching user ids and name fields
	stmt = `SELECT username FROM users WHERE id=$1 AND firstname=$2 AND lastname=$3 LIMIT 1`
	err = c.db.QueryRow(stmt, dpm.UserID, dpm.FirstName, dpm.LastName).Scan(&username)
	// If err, assume it is because a descrepancy between what's in the db and the info provided
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// If they are not a sup or admin, they do not
	// have permission to create a dpm
	if !admin && !sup {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Prepare query string
	dpmIn := `INSERT INTO dpms (createid, userid, firstname, lastname, block, date, starttime, endtime, dpmtype, points, notes, created, location, approved) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, false)`
	// Insert unapproved dpm into databas
	_, err = c.db.Exec(dpmIn, dpm.CreateID, dpm.UserID, dpm.FirstName, dpm.LastName, dpm.Block, dpm.Date, dpm.StartTime, dpm.EndTime, dpm.DPMType, dpm.Points, dpm.Notes, dpm.Created, dpm.Location)
	if err != nil {
		fmt.Println("DPM failure")
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	w.WriteHeader(http.StatusOK)
	return
}

// This gets all the users, their ids, and the id of the user requesting this
// It then this data as JSON back to the client
func (c Controller) getAllUsers(w http.ResponseWriter, r *http.Request) {
	// Validate that this request is authorized
	sender, err := c.getUser(w, r)
	// If user is not signed in, redirect
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// user needs to be admin or sup to do this
	if !sender.Admin && !sender.Sup {
		w.WriteHeader(http.StatusNotFound)
		return
	}

	// if user has not changed password, redirect
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Struct to be marshalled into JSON and sent to client
	type passUser struct {
		Names  []string `json:"names"`  // Slice of drivers' names
		Ids    []int16  `json:"ids"`    // Slice of drivers' ids
		UserID string   `json:"userID"` // Username of the user loading this resource
	}
	// Get all users
	rows, err := c.db.Query("SELECT firstname, lastname, id FROM users")
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	defer rows.Close()
	// Slices to hold names and ids
	names := make([]string, 0)
	ids := make([]int16, 0)
	// Iterate through rows returned filling slices with infor
	for rows.Next() {
		var (
			firstname string
			lastname  string
			id        int16
		)
		rows.Scan(&firstname, &lastname, &id)
		names = append(names, firstname+" "+lastname)
		ids = append(ids, id)
	}
	i := int(sender.ID)
	temp := strconv.Itoa(i)
	// Fill in struct values
	pU := passUser{
		Names:  names,
		Ids:    ids,
		UserID: temp,
	}
	// Turn struct into JSON and respond with it
	j, err := json.Marshal(pU)
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write(j)
}

// Creates a user in the database
func (c Controller) createUser(w http.ResponseWriter, r *http.Request) {
	// Validate that this request is authorized
	sender, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	if !sender.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	u := &models.User{}
	user := bm.Sanitize(r.FormValue("email"))
	var test bool
	if user == "testing@testing.com" {
		test = true
	}
	err = c.db.QueryRowx("SELECT * FROM users WHERE username=$1 LIMIT 1", user).StructScan(u)
	// If no error, that means user with that username already exists
	// Render template mentioning this
	// Only give error if not testing
	if err == nil && !test {
		out := "The username name you are trying to register is already in use, please try a different username."
		c.createUserMessage(w, r, out)
		return
	}
	// Generate 16 character random password for the user and send it to them
	pass := gotp.RandomSecret(16)
	// Create go routine to handle sending the email
	// Only send email if not testing
	if !test {
		go mail.SendTempPass(user, pass)
	}
	// Get password hash
	hash, err := bcrypt.GenerateFromPassword([]byte(pass), bcrypt.MinCost)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	// Determine if user is a fulltimer
	fulltime := false
	if r.FormValue("fullTimer") == "on" {
		fulltime = true
	}
	// Create user struct from form data
	u = &models.User{
		Username:   user,
		Password:   string(hash),
		FirstName:  bm.Sanitize(r.FormValue("firstName")), // Sanitize first name
		LastName:   bm.Sanitize(r.FormValue("lastName")),  // Sanitize last name
		FullTime:   fulltime,
		Changed:    false,
		Admin:      false,
		Sup:        false,
		Analysist:  false,
		SessionKey: gotp.RandomSecret(16), // Temp value for session, never valid
		Points:     0,
		Added:      time.Now().Format("2006-1-02 15:04:05"),
	}
	userIn := `INSERT INTO users (username, password, firstname, lastname, fulltime, sessionkey, added) VALUES($1, $2, $3, $4, $5, $6, $7)`
	_, err = c.db.Exec(userIn, u.Username, u.Password, u.FirstName, u.LastName, u.FullTime, u.SessionKey, u.Added)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	// Display success message informing if part/full time driver was created
	// In case admin misses the small checkbox
	if u.FullTime {
		out := "Fulltime driver has been added to the database!"
		c.createUserMessage(w, r, out)
		return
	}
	out := "Part time driver has been added to the database"
	c.createUserMessage(w, r, out)
}

// Logs in the user
func (c Controller) logInUser(w http.ResponseWriter, r *http.Request) {
	// Struct for later use
	u := &models.User{}
	// Get user input
	user := r.FormValue("username")
	pass := r.FormValue("password")
	// Find user in database
	err := c.db.QueryRowx("SELECT * FROM users WHERE username=$1 LIMIT 1", user).StructScan(u)
	// If they do not exist, complain
	if err != nil {
		out := "Username or password was incorrect, please try again."
		c.loginError(w, r, out)
		return
	}
	// Validate password
	err = bcrypt.CompareHashAndPassword([]byte(u.Password), []byte(pass))
	// If passwords do not match, render template with message
	if err != nil {
		out := "Username or password was incorrect, please try again."
		c.loginError(w, r, out)
		return
	}
	// Create a session for the user
	sk, err := c.cookieSignIn(w, r)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Something went wrong, pleae try again."))
		return
	}
	// Set user's session key
	u.SessionKey = sk
	// Update user in database to contain this new session
	update := `UPDATE users SET sessionkey=$1 WHERE id=$2`
	_, err = c.db.Exec(update, u.SessionKey, u.ID)
	if err != nil {
		out := fmt.Sprintln("Something went wrong")
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}

	// If signing in with temporary password, make user change it
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Redirect user after succesful login
	http.Redirect(w, r, "/", http.StatusFound)
}

// Logic for changing the password of a user
func (c Controller) changeUserPassword(w http.ResponseWriter, r *http.Request) {
	// Validate user
	u, err := c.getUser(w, r)
	if err != nil {
		c.renderLogin(w, r)
		return
	}
	// Get old password and make sure it matches what is in the database
	og := r.FormValue("originalPass")
	// Ensure new password matches what's in db
	err = bcrypt.CompareHashAndPassword([]byte(u.Password), []byte(og))
	// If passwords do not match, inform user
	if err != nil {
		out := "Please ensure that you are inputting your old password correctly."
		c.changePasswordError(w, r, out)
		return
	}
	// Get both copies of new password and ensure they are the same
	pass1 := r.FormValue("pass1")
	pass2 := r.FormValue("pass2")
	if pass1 != pass2 {
		out := "Please ensure that your new passwords match."
		c.changePasswordError(w, r, out)
		return
	}
	// If user enters temporary password for their new password, complain
	if pass1 == og {
		out := "Please make your new password different from your temporary one"
		c.changePasswordError(w, r, out)
		return
	}
	// Create a new session for the user
	sk, err := c.cookieSignIn(w, r)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte("Something went wrong with creating your session."))
		return
	}
	u.SessionKey = sk
	// Hash their new password and store it in struct
	hash, err := bcrypt.GenerateFromPassword([]byte(pass1), bcrypt.MinCost)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	u.Password = string(hash)
	// They have changed password, so they are definately not using temp pass any more
	u.Changed = true
	// Create go routine to handle username change email
	// Update user in database to contain this new session
	update := `UPDATE users SET sessionkey=$1, changed=$2, password=$3 WHERE id=$4`
	_, err = c.db.Exec(update, u.SessionKey, u.Changed, u.Password, u.ID)
	if err != nil {
		out := fmt.Sprintln("Something went wrong")
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	// Send password change email
	go mail.SendPassChangeMail(u.Username)
	http.Redirect(w, r, "/", http.StatusFound)
}

// Logs a user out
func (c Controller) logoutUser(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// If can't find user, same behavior as regular logout
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// Set session key to random string
	u.SessionKey = gotp.RandomSecret(32)
	// Fetch session
	sess, err := c.store.Get(r, "dpm_cookie")
	// Even if session somehow does not exit at this stage, store.Get
	// generates a new session so if this step fails, seomthing weird is going on
	if err != nil {
		fmt.Println(err)
		http.Redirect(w, r, "/", http.StatusInternalServerError)
		return
	}
	// Expire session
	sess.Options.MaxAge = -1
	c.store.Save(r, w, sess)
	// Update user in db with invalid session
	update := `UPDATE users SET sessionkey=$1 WHERE id=$2`
	_, err = c.db.Exec(update, u.SessionKey, u.ID)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	http.Redirect(w, r, "/login", http.StatusFound)
}

// This handles reseting a user's password on admin request
func (c Controller) resetPassword(w http.ResponseWriter, r *http.Request) {
	// If user not logged in, redirect
	sender, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// If user is not an admin, 404
	if !sender.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Sanitize username
	username := bm.Sanitize(r.FormValue("username"))
	// If user is trying to reset their own password, do not allow it
	// Send message back stating this
	if sender.Username == username {
		out := "Please do not try to reset your own password."
		c.resetPasswordMessage(w, r, out)
		return
	}
	u := &models.User{}
	// Get user's id, username, and sessionkey from db
	stmt := `SELECT id, username, sessionkey FROM users WHERE username=$1 LIMIT 1`
	err = c.db.QueryRowx(stmt, username).StructScan(u)
	// Assume, to client, that error is because could not find username in db
	if err != nil {
		fmt.Println(err)
		out := "Could not find user with that username in the database."
		c.resetPasswordMessage(w, r, out)
		return
	}
	// Generate 16 character random password for the user and send it to them
	pass := gotp.RandomSecret(16)
	// Send email telling user than an admin has reset your password
	go mail.SendAdminTempPass(u.Username, pass)
	// Get password hash
	hash, err := bcrypt.GenerateFromPassword([]byte(pass), bcrypt.MinCost)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	// Invalidate session for user so they have to sign in with new password
	u.SessionKey = gotp.RandomSecret(32)
	u.Password = string(hash)
	// Change to false because they are now using a temp password
	u.Changed = false
	// Update user in the db to reflect, new pass, new value for changed, and
	// invalid session key
	update := `UPDATE users SET password=$1, changed=$2, sessionkey=$3 WHERE id=$4`
	_, err = c.db.Exec(update, u.Password, u.Changed, u.SessionKey, u.ID)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	// Display success message
	out := "User password successfully reset"
	c.resetPasswordMessage(w, r, out)
}

func (c Controller) callAutoSubmit(w http.ResponseWriter, r *http.Request) {
	// Get user and validate
	sender, err := c.getUser(w, r)
	if err != nil {
		fmt.Println(err)
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// Only admins and sups can do this
	if !sender.Admin && !sender.Sup {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// If still using temp password, redirect
	if !sender.Changed {
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	// Regenerate DPMS
	// This is ineffecient because I already generate these when the user submits a get to /dpm/all
	// The reason why I generate again is to help protect against the unlikely odds of someone changing data on their end and sending it back to me to submit
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
	err = autodpm.AutoSubmit(c.db, dpms, sender.ID)
	// If error, render the autogenErr template stating this
	if err != nil {
		type autoErr struct {
			Nav Navbar
			Err string
		}
		n := Navbar{
			Admin:     sender.Admin,
			Sup:       sender.Sup,
			Analysist: sender.Analysist,
		}
		auto := autoErr{n, err.Error()}
		err = c.tpl.ExecuteTemplate(w, "autogenErr.gohtml", auto)
		if err != nil {
			out := fmt.Sprintln("Something went wrong, please try again")
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			w.Write([]byte(out))
			return
		}
		return
	}
	http.Redirect(w, r, "/", http.StatusFound)
}

// sendApprovalLogic sends all unapproved DPMS to the admin requesting the page
// Helper function for SendApprovalDPM
func (c Controller) sendApprovalLogic(w http.ResponseWriter, r *http.Request) {
	// If can't find user/user not logged in, redirect to login page
	u, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins can do this
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Redirect is still on temporary password
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Variables need for loop
	var firstname, lastname, block, location, date, startTime, endTime, dpmType, points, notes, created, supFirst, supLast, id string
	// Variable containing the id of the supervisor who submitted each dpm
	var supID int16
	// Query that gets most of the relevant information about each non-approved dpm
	stmt := `SELECT id, createid, firstname, lastname, block, location, date, starttime, endtime, dpmtype, points, notes, created FROM dpms WHERE approved=false AND ignored=false ORDER BY created DESC`
	// Query that gets the name of the supervisor that submitted each dpm
	supQuery := `SELECT firstname, lastname FROM users WHERE id=$1`
	ds := make([]models.DPMApprove, 0)
	rows, err := c.db.Query(stmt)
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	var dd models.DPMApprove
	for rows.Next() {
		// Get variables from the row
		err = rows.Scan(&id, &supID, &firstname, &lastname, &block, &location, &date, &startTime, &endTime, &dpmType, &points, &notes, &created)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		// Ensure that positive points start with a '+'
		// Positive DPMS get autoapproved, and I am querying for DPMS where approved equals false
		// This code should not run, until I add to ability to make approved dpms non-approved
		if string(points[0]) != "-" {
			points = "+" + points
		}
		// Find sup who submitted this DPM
		err = c.db.QueryRow(supQuery, supID).Scan(&supFirst, &supLast)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		// Create DPMApprove struct to pass into slice
		dd = models.DPMApprove{
			ID:        id,
			Name:      firstname + " " + lastname,
			SupName:   supFirst + " " + supLast,
			Block:     block,
			Location:  location,
			Date:      date,
			StartTime: startTime,
			EndTime:   endTime,
			DPMType:   dpmType,
			Points:    points,
			Notes:     notes,
			Created:   created,
		}
		ds = append(ds, dd)
	}
	// Turn slice into JSON and respond with it
	j, err := json.Marshal(ds)
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write(j)
}

// sendDriverLogic handles sending simplified DPMs to drivers
func (c Controller) sendDriverLogic(w http.ResponseWriter, r *http.Request) {
	// If can't find user/user not logged in, redirect to login page
	u, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	stmt := `SELECT firstname, lastname, block, location, date, starttime, endtime, dpmtype, points, notes FROM dpms WHERE userid=$1 AND approved=true AND ignored=false ORDER BY created DESC`
	ds := make([]models.DPMDriver, 0)
	rows, err := c.db.Queryx(stmt, u.ID)
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	var dd models.DPMDriver
	for rows.Next() {
		err = rows.StructScan(&dd)
		if err != nil {
			fmt.Println(err)
			w.WriteHeader(http.StatusInternalServerError)
			return
		}
		if string(dd.Points[0]) != "-" {
			dd.Points = "+" + dd.Points
		}
		ds = append(ds, dd)
	}
	// Turn slice into JSON and respond with it
	j, err := json.Marshal(ds)
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	w.Write(j)
}

// approveDPMLogic handles logic for approving a DPM
func (c Controller) approveDPMLogic(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins can do this
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Redirect if still on temporary password
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Temporary struct to hold response from client
	type approveDPM struct {
		Points string
		Name   string
	}
	a := approveDPM{}
	// Get JSON from request body
	decoder := json.NewDecoder(r.Body)
	// Parse JSON to get points value
	err = decoder.Decode(&a)
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	// Parse the URL and get the id from the URL
	vars := mux.Vars(r)
	id, err := strconv.Atoi(vars["id"])
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Update specified DPM to make approved equal to true
	update := `UPDATE dpms SET approved=true WHERE id=$1`
	_, err = c.db.Exec(update, id)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	// Split name based on spaces
	ns := strings.Split(a.Name, " ")
	// Get first name
	first := bm.Sanitize(ns[0])
	last := ""
	// Join indexes after 0 into last name string and sanitize
	// If last name exists, set it to the remainder of slice joined together
	if len(ns) > 1 {
		ns = append(ns[:0], ns[1:]...)
		last = bm.Sanitize(strings.Join(ns, " "))
	}
	// Convert points into int16
	points32, err := strconv.Atoi(a.Points)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	points := int16(points32)
	// Update user's point balance to reflect the new points
	update = `UPDATE users set points=points + $1 WHERE firstname=$2 AND lastname=$3`
	_, err = c.db.Exec(update, points, first, last)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	w.WriteHeader(http.StatusOK)
}

func (c Controller) denyDPMLogic(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins can do this
	if !u.Admin {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Redirect if still on temporary password
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Parse URL for id of DPM
	vars := mux.Vars(r)
	// Convert id to int
	id, err := strconv.Atoi(vars["id"])
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Update specified DPM to set ignored to false and set approved to false for extra redundancy
	update := `UPDATE dpms SET approved=false, ignored=true WHERE id=$1`
	_, err = c.db.Exec(update, id)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	w.WriteHeader(http.StatusOK)
}
