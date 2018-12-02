package dpm

import (
	"encoding/json"
	"fmt"
	"net/http"
	"strconv"
	"time"

	"github.com/airfork/webScrape"

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
		full     bool
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
	// Gets full time status and username, not actually needed unless this is a negative DPM, but it also serves the
	// role of checking to see if the first and last name in the database match the id being provided
	// This prevents DPMS from being created with non matching user ids and name fields
	stmt = `SELECT fulltime, username FROM users WHERE id=$1 AND firstname=$2 AND lastname=$3 LIMIT 1`
	err = c.db.QueryRow(stmt, dpm.UserID, dpm.FirstName, dpm.LastName).Scan(&full, &username)
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
	dpmIn := `INSERT INTO dpms (createid, userid, firstname, lastname, block, date, starttime, endtime, dpmtype, points, notes, created, location, approved) VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)`
	pointUpdate := `UPDATE users SET points=points + $1 WHERE id=$2`
	// If points are positive can just insert into database without
	if dpm.Points >= 0 {
		_, err = c.db.Exec(dpmIn, dpm.CreateID, dpm.UserID, dpm.FirstName, dpm.LastName, dpm.Block, dpm.Date, dpm.StartTime, dpm.EndTime, dpm.DPMType, dpm.Points, dpm.Notes, dpm.Created, dpm.Location, true)
		if err != nil {
			fmt.Println("Positive DPM failure")
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Println(err)
			return
		}
		// Update driver point balance
		_, err = c.db.Exec(pointUpdate, dpm.Points, dpm.UserID)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Println(err)
			return
		}
		w.WriteHeader(http.StatusOK)
		return
	}
	// If negative points need to handle the case for part timers,
	// They get emailed, and fultimers, whose dpms need to get approved
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// If not fulltimer, send email about naegative dpm
	// And insert dpm into database
	if !full {
		// Insert DPM into db
		_, err = c.db.Exec(dpmIn, dpm.CreateID, dpm.UserID, dpm.FirstName, dpm.LastName, dpm.Block, dpm.Date, dpm.StartTime, dpm.EndTime, dpm.DPMType, dpm.Points, dpm.Notes, dpm.Created, dpm.Location, true)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Println(err)
			return
		}
		// Update point balance
		_, err = c.db.Exec(pointUpdate, dpm.Points, dpm.UserID)
		if err != nil {
			w.WriteHeader(http.StatusInternalServerError)
			fmt.Println(err)
			return
		}
		go mail.NegativeDPMEmail(username, dpm.DPMType)
		w.WriteHeader(http.StatusOK)
		return
	}
	// Driver is fulltime, insert dpm into database but set approved to false
	_, err = c.db.Exec(dpmIn, dpm.CreateID, dpm.UserID, dpm.FirstName, dpm.LastName, dpm.Block, dpm.Date, dpm.StartTime, dpm.EndTime, dpm.DPMType, dpm.Points, dpm.Notes, dpm.Created, dpm.Location, false)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return
	}
	w.WriteHeader(http.StatusOK)
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
		c.createUserMessage(w, out)
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
		c.createUserMessage(w, out)
		return
	}
	out := "Part time driver has been added to the database"
	c.createUserMessage(w, out)
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
		c.loginError(w, out)
		return
	}
	// Validate password
	err = bcrypt.CompareHashAndPassword([]byte(u.Password), []byte(pass))
	// If passwords do not match, render template with message
	if err != nil {
		out := "Username or password was incorrect, please try again."
		c.loginError(w, out)
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
	// Get new and original password
	new := r.FormValue("newPassword")
	og := r.FormValue("originalPass")
	// Ensure new password matches what's in db
	err = bcrypt.CompareHashAndPassword([]byte(u.Password), []byte(og))
	// If passwords do not match, inform user
	if err != nil {
		out := "Please ensure that you are inputting your old password correctly."
		c.changePasswordError(w, out)
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
	hash, err := bcrypt.GenerateFromPassword([]byte(new), bcrypt.MinCost)
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
		c.resetPasswordMessage(w, out)
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
		c.resetPasswordMessage(w, out)
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
	c.resetPasswordMessage(w, out)
}

// SendDriverDPM creates a slightly altered DPM and sends it to the client
// as part of ajax call so that they can view more detailed info about
// their DPMs
func (c Controller) SendDriverDPM(w http.ResponseWriter, r *http.Request) {
	// If can't find user/user not logged in, redirect to login page
	u, err := c.getUser(w, r)
	if err != nil {
		http.Redirect(w, r, "/login", http.StatusFound)
		fmt.Println(err)
		return
	}
	stmt := `SELECT firstname, lastname, block, location, date, starttime, endtime, dpmtype, points, notes FROM dpms WHERE userid=$1 AND approved=true ORDER BY created DESC`
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
	http.Redirect(w, r, "/", http.StatusFound)
}
