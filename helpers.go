package main

import (
	"fmt"
	"html"
	"io"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/gorilla/csrf"
	"github.com/xlzd/gotp"

	"github.com/gorilla/securecookie"
	"golang.org/x/crypto/bcrypt"
)

var typeMap = map[string]int16{
	"Type G: Good! (+1 Point)":                              1,
	"Type G: 200 Hours Safe (+2 Points)":                    2,
	"Type G: Voluntary Clinic/Road Test Passed (+2 Points)": 2,
	"Type L: 1-5 Minutes Late to OFF (-1 Point)":            -1,
	"Type A: Missed Email Announcement (-2 Points)":         -2,
	"Type A: 1-5 Minutes Late to BLK (-5 Points)":           -5,
	"Type A: Improper Shutdown (-2 Points)":                 -2,
	"Type A: Off-Route (-2 Points)":                         -2,
	"Type A: Out of Uniform (-5 Points)":                    -5,
	"Type A: Improper Radio Procedure (-5 Points)":          -5,
	"Type A: Improper Bus Log (-5 Points)":                  -5,
	"Type A: Timesheet/Improper Book Change (-5 Points)":    -5,
	"Type A: 6-15 Minutes Late to Blk (-3 Points)":          -3,
	"Type B: Attendance Infraction (-10 Points)":            -10,
	"Type B: Passenger Inconvenience (-5 Points)":           -5,
	"Type B: Moving Downed Bus (-10 Points)":                -10,
	"Type B: Improper 10-50 Procedure (-10 Points)":         -10,
	"Type B: 16+ Minutes Late (-5 Points)":                  -5,
	"Type B: Failed Ride-Along/Road Test (-10 Points)":      -10,
	"Type C: Failure to Report 10-50 (-15 Points)":          -15,
	"Type C: Insubordination (-15 Points)":                  -15,
	"Type C: Safety Offense (-15 Points)":                   -15,
	"Type C: Preventable Accident 1, 2 (-15 Points)":        -15,
	"Type D: DNS/Did Not Show (-10 Points)":                 -10,
	"Type D: Preventable Accident 3, 4 (-20 Points)":        -20,
	"Type A: Custom (-5 Points)":                            -5,
	"Type B: Custom (-10 Points)":                           -10,
	"Type C: Custom (-15 Points)":                           -15,
}

// generateDPM creates a DPM from the shortened version taken from client
func generateDPM(d *dpmRes) *dpm {
	// Slice of name inputted
	// Done to handle multiple name last names
	ns := strings.Split(d.Name, " ")
	// Sanitize first name
	first := bm.Sanitize(ns[0])
	last := ""
	// Join indexes after 0 into last name string and sanitize
	if len(ns) > 1 {
		ns = append(ns[:0], ns[1:]...)
		last = bm.Sanitize(strings.Join(ns, " "))
	}
	// Get id of person receiving dpm
	id64, err := strconv.ParseInt(bm.Sanitize(d.ID), 10, 64)
	userID := int16(id64)
	if err != nil {
		fmt.Println(err)
		return nil
	}
	// Get ID of person creating DPM
	id64, err = strconv.ParseInt(bm.Sanitize(d.Sender), 10, 64)
	if err != nil {
		fmt.Println(err)
		return nil
	}
	createID := int16(id64)
	dpmType := bm.Sanitize(d.DpmType)
	points, ok := typeMap[dpmType]
	if !ok {
		return nil
	}
	dpm := &dpm{
		CreateID:  createID,
		UserID:    userID,
		FirstName: html.UnescapeString(first),
		LastName:  html.UnescapeString(last),
		Block:     html.UnescapeString(strings.ToUpper(bm.Sanitize(d.Block))),
		Location:  html.UnescapeString(strings.ToUpper(bm.Sanitize(d.Location))),
		Date:      bm.Sanitize(d.Date),
		StartTime: bm.Sanitize(d.StartTime),
		EndTime:   bm.Sanitize(d.EndTime),
		DPMType:   dpmType,
		Points:    points,
		Notes:     html.UnescapeString(bm.Sanitize(strings.TrimSpace(d.Notes))),
		Created:   time.Now().Format("2006-1-02 15:04:05"),
	}
	return dpm
}

// getUser returns the userID based on the session ID
func (c Controller) getUser(w http.ResponseWriter, r *http.Request) (*user, error) {
	u := &user{}
	// Find cookie, if no cookie, they are not logged in
	_, err := r.Cookie("dpm_cookie")
	if err != nil {
		return u, err
	}
	// Get their cookie
	session, err := c.store.Get(r, "dpm_cookie")
	// If there is an error here, it is probablly due to changing keys of cookie store
	// without following proper fallback
	if err != nil {
		return nil, err
	}
	// Find session id, find user based off that session ID, and return
	sid := session.Values["id"]
	err = c.db.QueryRowx("SELECT * FROM users WHERE sessionkey=$1 LIMIT 1", sid).StructScan(u)
	// If this fails, invalid session key or db failure
	if err != nil {
		return nil, err
	}
	return u, err
}

// createSession creates a new session for the user
func (c Controller) createSession(w http.ResponseWriter, r *http.Request) (string, error) {
	// Get a session. Get() always returns a session, even if empty.
	session, err := c.store.Get(r, "dpm_cookie")
	if err != nil {
		http.Error(w, err.Error(), http.StatusInternalServerError)
		return "", err
	}
	// Set session ID
	sid, _ := bcrypt.GenerateFromPassword(securecookie.GenerateRandomKey(32), bcrypt.MinCost)
	session.Values["id"] = string(sid)
	// Save it before we write to the response/return from the handler.
	err = session.Save(r, w)
	if err != nil {
		return "", err
	}
	return string(sid), nil
}

// cookieSignIn signs in user
func (c Controller) cookieSignIn(w http.ResponseWriter, r *http.Request) (string, error) {
	// Create new session, prompt user to try again if this fails
	sk, err := c.createSession(w, r)
	if err != nil {
		out := fmt.Sprintln("There seems to have been a problem, please try and hopefully it goes away")
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte(out))
		return "", err
	}
	return sk, nil
}

// loginError renders login with an error message
func (c Controller) loginError(w http.ResponseWriter, r *http.Request, message, username string) {
	// Render login template
	err := c.tpl.ExecuteTemplate(w, "login.gohtml", map[string]interface{}{"message": message, "username": username, "csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// changePasswordError renders changePass with an error message
func (c Controller) changePasswordError(w http.ResponseWriter, r *http.Request, message string) {
	// Render login template
	err := c.tpl.ExecuteTemplate(w, "changePass.gohtml", map[string]interface{}{"message": message, "csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// resetPasswordMessage renders resetPassword with a message underneath
func (c Controller) resetPasswordMessage(w http.ResponseWriter, r *http.Request, message string) {
	// Render login template
	err := c.tpl.ExecuteTemplate(w, "resetPassword.gohtml", map[string]interface{}{"message": message, "csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// createUserMessage renders createUser template with a message underneath
func (c Controller) createUserMessage(w http.ResponseWriter, r *http.Request, message string) {
	u, err := c.getUser(w, r)
	if err != nil {
		fmt.Println(err)
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	n := navbar{
		Admin:   u.Admin,
		Analyst: u.Analyst,
		Sup:     u.Sup,
	}
	// Render createUser template
	err = c.tpl.ExecuteTemplate(w, "createUser.gohtml", map[string]interface{}{"message": message, "csrf": csrf.TemplateField(r), "Nav": n})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// createUserFill renders createUser template with some fields prefilled
func (c Controller) createUserFill(w http.ResponseWriter, r *http.Request, firstname, lastname string) {
	u, err := c.getUser(w, r)
	if err != nil {
		fmt.Println(err)
		http.Redirect(w, r, "/login", http.StatusFound)
		return
	}
	n := navbar{
		Admin:   u.Admin,
		Analyst: u.Analyst,
		Sup:     u.Sup,
	}
	// If lastname does exist, only pass in csrf and firstname, otherwise pass in token, first name, and lastname
	if lastname == "" {
		err = c.tpl.ExecuteTemplate(w, "createUser.gohtml", map[string]interface{}{"firstname": firstname, "csrf": csrf.TemplateField(r), "Nav": n})
	} else {
		err = c.tpl.ExecuteTemplate(w, "createUser.gohtml", map[string]interface{}{"firstname": firstname, "lastname": lastname, "csrf": csrf.TemplateField(r), "Nav": n})
	}
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// resetUserFill loads reset template with the username to reset prefilled
func (c Controller) resetUserFill(w http.ResponseWriter, r *http.Request, username string) {
	err := c.tpl.ExecuteTemplate(w, "resetPassword.gohtml", map[string]interface{}{"username": username, "csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = w.Write([]byte(out))
		return
	}
	return
}

// formatDate takes in a pqsl date string and converts it into a more friendly format
func formatDate(date string) string {
	// 2018-11-29T00:00:00Z
	if len(date) != 20 {
		return date
	}
	year := date[0:4]
	month := date[5:7]
	day := date[8:10]
	return fmt.Sprintf("%s-%s-%s\n", month, day, year)
}

// formatCreatedDate takes in pqsl timestamp and turns into user friendly format
func formatCreatedDate(date string) string {
	year := date[0:4]
	month := date[5:7]
	day := date[8:10]
	hour := date[11:13]
	minute := date[14:16]
	second := date[17:19]
	return fmt.Sprintf("%s-%s-%s %s:%s:%s\n", month, day, year, hour, minute, second)
}

func returnFile(w http.ResponseWriter, filename string) {
	// Check if file exists and open
	openfile, err := os.Open(filename)
	defer openfile.Close() // Close after function return
	if err != nil {
		// File not found, send 404
		http.Error(w, "File not found.", 404)
		return
	}

	// File is found, create and send the correct headers

	// Get the Content-Type of the file
	// Create a buffer to store the header of the file in
	fileHeader := make([]byte, 512)
	// Copy the headers into the FileHeader buffer
	_, err = openfile.Read(fileHeader)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	// Get content type of file
	fileContentType := http.DetectContentType(fileHeader)

	// Get the file size
	fileStat, _ := openfile.Stat()                     // Get info from file
	fileSize := strconv.FormatInt(fileStat.Size(), 10) // Get file size as a string

	// Send the headers
	w.Header().Set("Content-Disposition", "attachment; filename="+filename)
	w.Header().Set("Content-Type", fileContentType)
	w.Header().Set("Content-Length", fileSize)

	// Send the file
	// We read 512 bytes from the file already, so we reset the offset back to 0
	_, err = openfile.Seek(0, 0)
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	_, err = io.Copy(w, openfile) // 'Copy' the file to the client
	if err != nil {
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		return
	}
	return
}

func redirect(w http.ResponseWriter, r *http.Request) bool {
	// Check url and make sure that it is coming from the right domain
	url := r.Host
	if url == os.Getenv("HEROKU_APP") {
		http.Redirect(w, r, os.Getenv("DOMAIN"), http.StatusMovedPermanently)
		return true
	}
	return false
}

func (c Controller) resetPassHelper(w http.ResponseWriter, r *http.Request, username string, sender *user) bool {
	// If user is trying to reset their own password, do not allow it
	// Send message back stating this
	if sender.Username == username {
		out := "Please do not try to reset your own password."
		c.resetPasswordMessage(w, r, out)
		return false
	}
	// If admin is trying to reset the password of a testing account, send message and return
	if username == "testing@testing.com" {
		out := "Can't reset the password of a testing account"
		c.resetPasswordMessage(w, r, out)
		return false
	}
	u := &user{}
	// Get user's id, username, and sessionkey from db
	stmt := `SELECT id, username, sessionkey, firstname, lastname FROM users WHERE username=$1 LIMIT 1`
	err := c.db.QueryRowx(stmt, username).StructScan(u)
	// Assume, to client, that error is because could not find username in db
	if err != nil {
		fmt.Println(err)
		out := "Could not find user with that username in the database."
		c.resetPasswordMessage(w, r, out)
		return false
	}
	// Generate random password for user
	pass := gotp.RandomSecret(16)
	// Send email telling user than an admin has reset your password
	go sendResetPasswordEmail(u.Username, pass, u.FirstName, u.LastName)
	// Get password hash
	hash, err := bcrypt.GenerateFromPassword([]byte(pass), bcrypt.DefaultCost)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return false
	}
	// Invalidate session for user so they have to sign in with new password
	u.SessionKey = gotp.RandomSecret(32)
	u.Password = string(hash)
	// Change to false because they are now using a temp password
	u.Changed = false
	// Update user in the db to reflect new pass, new value for changed, and
	// invalid session key
	update := `UPDATE users SET password=$1, changed=$2, sessionkey=$3 WHERE id=$4`
	_, err = c.db.Exec(update, u.Password, u.Changed, u.SessionKey, u.ID)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		fmt.Println(err)
		return false
	}
	return true
}

// getStatus gets the status of a DPM based on the two booleans
func (c Controller) getStatus(approved, ignored bool) string {
	if approved == true && ignored == false {
		return "DPM approved"
	} else if approved == true && ignored == true {
		return "DPM approved but not visible to driver"
	} else if approved == false && ignored == false {
		return "DPM has not been looked at"
	} else {
		return "DPM denied"
	}
}
