package main

import (
	"database/sql"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"strconv"
	"strings"
	"sync"
	"time"

	"github.com/gorilla/mux"

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
	// No regular users can do this
	if !sender.Admin && !sender.Sup && !sender.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	if !sender.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Get JSON from request body
	decoder := json.NewDecoder(r.Body)
	var d dpmRes
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
	stmt := `SELECT admin, sup, analyst FROM users WHERE id=$1`
	var (
		admin    bool
		sup      bool
		analyst  bool
		username string
	)
	err = c.db.QueryRow(stmt, dpm.CreateID).Scan(&admin, &sup, &analyst)
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
	// If they are a regular user, they do not
	// have permission to create a dpm
	if !admin && !sup && !analyst {
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
	// No regular users can do this
	if !sender.Admin && !sender.Sup && !sender.Analyst {
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
	u := &user{}
	username := bm.Sanitize(r.FormValue("email"))
	firstname := bm.Sanitize(r.FormValue("firstName"))
	lastname := bm.Sanitize(r.FormValue("lastName"))
	// Ensure username and firstname are not empty
	// Ideally these are not necessary as they are required fields, along with lastname which I am not checking for here
	if username == "" {
		out := "Username cannot be empty."
		c.createUserMessage(w, r, out)
		return
	}
	if firstname == "" {
		out := "Please provide a first name"
		c.createUserMessage(w, r, out)
		return
	}
	// Test credentials
	var test bool
	if username == "testing@testing.com" {
		test = true
	}
	err = c.db.QueryRowx("SELECT * FROM users WHERE username=$1 LIMIT 1", username).StructScan(u)
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
		go sendTempPass(username, pass)
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
	u = &user{
		Username:   username,
		Password:   string(hash),
		FirstName:  firstname, // Sanitize first name
		LastName:   lastname,  // Sanitize last name
		FullTime:   fulltime,
		Changed:    false,
		Admin:      false,
		Sup:        false,
		Analyst:    false,
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
	u := &user{}
	// Get user input
	user := r.FormValue("username")
	pass := r.FormValue("password")
	// Find user in database
	err := c.db.QueryRowx("SELECT * FROM users WHERE username=$1 LIMIT 1", user).StructScan(u)
	// If they do not exist, complain
	if err != nil {
		fmt.Println(err)
		out := "Username or password was incorrect, please try again."
		c.loginError(w, r, out)
		return
	}
	// Validate password
	err = bcrypt.CompareHashAndPassword([]byte(u.Password), []byte(pass))
	// If passwords do not match, render template with message
	if err != nil {
		fmt.Println(err)
		out := "Username or password was incorrect, please try again."
		c.loginError(w, r, out)
		return
	}
	// Create a session for the user
	sk, err := c.cookieSignIn(w, r)
	if err != nil {
		fmt.Println(err)
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
		fmt.Println(err)
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
	// Check is password is shorter than 8 characters
	if len(pass1) < 8 {
		out := "Please make your password at least eight characters long."
		c.changePasswordError(w, r, out)
		return
	}
	// If user enters temporary password for their new password, complain
	if pass1 == og {
		out := "Please make your new password different from your temporary one."
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
	go sendPassChangeMail(u.Username)
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
	u := &user{}
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
	go sendAdminTempPass(u.Username, pass)
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
	// No regular users can do this
	if !sender.Admin && !sender.Sup && !sender.Analyst {
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
	dpms, err := autoGen()
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
	err = autoSubmit(c.db, dpms, sender.ID)
	// If error, render the autogenErr template stating this
	if err != nil {
		n := navbar{
			Admin:   sender.Admin,
			Sup:     sender.Sup,
			Analyst: sender.Analyst,
		}
		auto := err.Error()
		err = c.tpl.ExecuteTemplate(w, "autogenErr.gohtml", map[string]interface{}{
			"Nav": n, "Err": auto,
		})
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

// sendApprovalLogic sends all unapproved DPMS to the user requesting the page
// Helper function for SendApprovalDPM
func (c Controller) sendApprovalLogic(w http.ResponseWriter, r *http.Request) {
	// If can't find user/user not logged in, redirect to login page
	u, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins and analysts can do this
	if !u.Admin && !u.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Redirect is still on temporary password
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Variables need for loop
	var stmt, firstname, lastname, block, location, date, startTime, endTime, dpmType, points, notes, created, supFirst, supLast, id string
	// Variable containing the id of the supervisor who submitted each dpm
	var supID int16
	var rows *sql.Rows
	if u.Admin {
		// Query that gets most of the relevant information about each non-approved dpm
		stmt = `SELECT id, createid, firstname, lastname, block, location, date, starttime, endtime, dpmtype, points, notes, created FROM dpms WHERE approved=false AND ignored=false ORDER BY created DESC`
		// If analyst, there is a more complicated query to get the dpms
	} else {
		stmt = `SELECT a.id, a.createid, a.firstname, a.lastname, a.block, a.location, a.date, a.starttime, a.endtime, a.dpmtype, a.points, a.notes, a.created FROM dpms a
		JOIN users b ON b.id=a.userid
		WHERE approved=false AND ignored=false AND managerid=$1 ORDER BY created DESC`
	}
	// Query that gets the name of the supervisor that submitted each dpm
	supQuery := `SELECT firstname, lastname FROM users WHERE id=$1`
	ds := make([]dpmApprove, 0)
	if u.Admin {
		rows, err = c.db.Query(stmt)
		// If they are an analyst, I need to pass their id into the query
	} else {
		rows, err = c.db.Query(stmt, u.ID)
	}
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	var dd dpmApprove
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
		dd = dpmApprove{
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
	ds := make([]dpmDriver, 0)
	rows, err := c.db.Queryx(stmt, u.ID)
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	var dd dpmDriver
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
	// Only admins and analyst can do this
	if !u.Admin && !u.Analyst {
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
	var secondID, managerid int
	var fulltime bool
	var dpmtype, username string
	// This checks that this dpm id relates to a real dpm and gets the fulltime status, username, and dpm type of the driver
	stmt := `SELECT a.id, a.fulltime, a.username, b.dpmtype FROM users a
	JOIN dpms b ON a.id=b.userid
	WHERE b.id=$1;`
	err = c.db.QueryRow(stmt, id).Scan(&secondID, &fulltime, &username, &dpmtype)
	// If this fails, assume the ID is not valid and abort
	if err != nil {
		w.WriteHeader(http.StatusUnauthorized)
		fmt.Println(err)
		return
	}
	// If not an admin, make sure they have access to this dpm
	if !u.Admin {
		// Select the manager ID for the driver who owns this DPM
		stmt := `SELECT managerid FROM users a
		JOIN dpms b ON a.id=b.userid
		WHERE b.id=$1;`
		err = c.db.QueryRow(stmt, id).Scan(&managerid)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Println(err)
			return
		}
		// If ids do not match, abort
		if managerid != int(u.ID) {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
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
	if !fulltime && points < 0 {
		go negativeDPMEmail(username, dpmtype)
	}
	w.WriteHeader(http.StatusOK)
}

// denyDPMLogic handles logic for denying a dpm
func (c Controller) denyDPMLogic(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins and analysts can do this
	if !u.Admin && !u.Analyst {
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
	var secondID, managerid int
	// All this does is check that this dpm id relates to a real dpm
	stmt := `SELECT id FROM dpms WHERE id=$1`
	err = c.db.QueryRow(stmt, id).Scan(&secondID)
	// If this fails, assume the ID is not valid and abort
	if err != nil {
		w.WriteHeader(http.StatusUnauthorized)
		fmt.Println(err)
		return
	}
	// If analyst, make sure they have access to this dpm
	if u.Analyst {
		// Select the manager ID for the driver who owns this DPM
		stmt := `SELECT managerid FROM users a
		JOIN dpms b ON a.id=b.userid
		WHERE b.id=$1;`
		err = c.db.QueryRow(stmt, id).Scan(&managerid)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Println(err)
			return
		}
		// If ids do not match, abort
		if managerid != int(u.ID) {
			w.WriteHeader(http.StatusUnauthorized)
			return
		}
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

// usersCSV gets data from the users table and creates a csv file
func (c Controller) usersCSV(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins and analysts can do this
	if !u.Admin && !u.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Redirect if still on temporary password
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}

	// Create a bunch of variables to extract row data into
	var (
		id        int16
		username  string
		firstname string
		lastname  string
		admin     bool
		sup       bool
		analyst   bool
		fulltime  bool
		points    int16
		managerid int16
	)
	// String that will eventually hold all the csv data
	// RIght now, I am creating the headers for the csv file
	final := "ID,Username,Firstname,Lastname,Admin,Sup,Analyst,Fulltime,Points,Managerid\n"
	// Query database for required info
	stmt := `SELECT id, username, firstname, lastname, admin, sup, analyst, fulltime, points, managerid FROM users ORDER BY lastname, firstname`
	rows, err := c.db.Query(stmt)
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	for rows.Next() {
		// Extract data into variables
		rows.Scan(&id, &username, &firstname, &lastname, &admin, &sup, &analyst, &fulltime, &points, &managerid)
		// Create a string with all the data separated by commas for the csv
		out := fmt.Sprintf("%v,%s,%s,%s,%v,%v,%v,%v,%v,%v\n", id, username, firstname, lastname, admin, sup, analyst, fulltime, points, managerid)
		// Append created string to final string
		final += out
	}
	// Create a mutex lock so file writing does not cause problems
	var mu sync.Mutex
	// Lock this process so only one write happens at a time
	mu.Lock()
	defer mu.Unlock()
	// Write file
	d1 := []byte(final)
	err = ioutil.WriteFile("user.csv", d1, 0644)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	returnFile(w, r, "user.csv")
}

// dpmCSV creates a csv file with all the data from the dpms table
func (c Controller) dpmCSV(w http.ResponseWriter, r *http.Request) {
	u, err := c.getUser(w, r)
	// Validate user
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	// Only admins and analysts can do this
	if !u.Admin && !u.Analyst {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	// Redirect if still on temporary password
	if !u.Changed {
		http.Redirect(w, r, "/change", http.StatusFound)
		return
	}
	// Create a bunch of variables to extract row date into
	var (
		id        int16
		createid  int16
		userid    int16
		firstname string
		lastname  string
		block     string
		date      string
		dpmtype   string
		points    int16
		notes     string
		created   string
		approved  bool
		location  string
		startTime string
		endtime   string
		ignored   bool
	)

	// String that will eventually hold all the csv data
	// Right now, I am creating the headers for the csv file
	final := "ID,Createid,Userid,Firstname,Lastname,Block,Date,Type,Points,Notes,Created,Approved,Location,Start Time,End Time,Ignored\n"
	// Select everything from this table
	stmt := `SELECT * FROM dpms ORDER BY userid`
	rows, err := c.db.Query(stmt)
	defer rows.Close()
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	for rows.Next() {
		// Scan row data into variables
		rows.Scan(&id, &createid, &userid, &firstname, &lastname, &block, &date, &dpmtype, &points, &notes, &created, &approved, &location, &startTime, &endtime, &ignored)
		// Format date, created, startTime, and endTime into a more user friendly data format
		date = formatDate(date)
		created = formatCreatedDate(created)
		startTime = startTime[11:13] + startTime[14:16]
		endtime = endtime[11:13] + endtime[14:16]
		// Create string with all the values separated by commas
		out := fmt.Sprintf("%v,%v,%v,%s,%s,%s,%s,%s,%v,%s,%s,%v,%s,%s,%s,%v", id, createid, userid, firstname, lastname, block, date, dpmtype, points, notes, created, approved, location, startTime, endtime, ignored)
		// Replace any newlines with empty strings, newlines get put in for some I never bothered to figured out
		out = strings.Replace(out, "\n", "", -1)
		// Add newline to string
		out += "\n"
		// Append created string to final string
		final += out
	}
	// Create a mutex lock so file writing does not cause problems
	var mu sync.Mutex
	// Lock this process so only one write happens at a time
	mu.Lock()
	defer mu.Unlock()
	// Write file
	d1 := []byte(final)
	err = ioutil.WriteFile("dpm.csv", d1, 0644)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	returnFile(w, r, "dpm.csv")
}

// findUser tries to find a user in the database matching the input
func (c Controller) findUser(w http.ResponseWriter, r *http.Request) {
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
	var userid int16
	// Get name from form
	name := r.FormValue("name")
	// If they are trying to reset password, redirect them
	if name == "reset" {
		http.Redirect(w, r, "/users/reset", http.StatusFound)
		return
	}
	// Split name into first and last, if applicabale
	ns := strings.Split(name, " ")
	// Sanitize first name and put it in the format "%firstname%"
	first := fmt.Sprintf("%%%s%%", bm.Sanitize(ns[0]))
	last := ""
	// Join indexes after 0 into last name string and sanitize
	// If last name exists, form a different query
	if len(ns) > 1 {
		ns = append(ns[:0], ns[1:]...)
		// Sanitize last name and put it in the format "%lastname%"
		last = fmt.Sprintf("%%%s%%", bm.Sanitize(strings.Join(ns, " ")))
		// Try to find user based on first and last name
		stmt := `SELECT id FROM users WHERE firstname LIKE $1 AND lastname LIKE $2 LIMIT 1`
		err = c.db.QueryRow(stmt, first, last).Scan(&userid)
		// If error is not nil, assume user does not exist and redirect
		if err != nil {
			fmt.Println(err)
			// Remove the %'s and render template
			first = strings.Replace(first, "%", "", -1)
			last = strings.Replace(last, "%", "", -1)
			c.createUserFill(w, r, first, last)
			return
		}
	} else { // Handle first name only
		stmt := `SELECT id FROM users WHERE firstname LIKE $1 LIMIT 1`
		err = c.db.QueryRow(stmt, first).Scan(&userid)
		// If error is not nil, assume user does not exist and send them to create user page
		if err != nil {
			fmt.Println(err)
			// Remove the %'s and render template
			first = strings.Replace(first, "%", "", -1)
			c.createUserFill(w, r, first, last)
			return
		}
	}
	url := fmt.Sprintf("/users/edit/%v", userid)
	http.Redirect(w, r, url, http.StatusFound)
	return
}

func (c Controller) editUser(w http.ResponseWriter, r *http.Request) {
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
	// Get user id from url and convert it to an int
	vars := mux.Vars(r)
	id, err := strconv.Atoi(vars["id"])
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Get reset status
	reset := false
	if r.FormValue("reset") == "on" {
		reset = true
	}
	// Get fulltime status
	fulltime := false
	if r.FormValue("fulltime") == "on" {
		fulltime = true
	}
	// Get form information
	username := bm.Sanitize(r.FormValue("username"))
	firstname := bm.Sanitize(r.FormValue("firstname"))
	lastname := bm.Sanitize(r.FormValue("lastname"))
	// Convert manager id to an int
	manager := r.FormValue("manager")
	managerid, err := strconv.Atoi(manager)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	role := bm.Sanitize(r.FormValue("role"))
	// Make sure that role is lowered, for consistency
	role = strings.ToLower(role)
	// Assign values to roles
	var admin, analyst, sup bool
	if role == "admin" {
		admin = true
	} else if role == "manager" {
		analyst = true
	} else if role == "sup" {
		analyst = true
	}
	stmt := `UPDATE users SET admin=$1, analyst=$2, sup=$3, username=$4, firstname=$5, lastname=$6, managerid=$7, fulltime=$8 WHERE id=$9`
	_, err = c.db.Exec(stmt, admin, analyst, sup, username, firstname, lastname, managerid, fulltime, id)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// If reset is true, render the reset password form with the username filled in
	if reset {
		c.resetUserFill(w, r, username)
		return
	}
	http.Redirect(w, r, r.URL.String(), http.StatusFound)
	return
}
