package main

import (
	"fmt"
	"io"
	"net/http"
	"os"
	"strconv"
	"strings"
	"time"

	"github.com/gorilla/csrf"

	"github.com/gorilla/securecookie"
	"golang.org/x/crypto/bcrypt"
)

var typeMap = map[string]int16{
	"Type G: Good! (+1 Point)":                              1,
	"Type G: 200 Hours Safe (+2 Points)":                    2,
	"Type G: Voluntary Clinic/Road Test Passed (+2 Points)": 2,
	"Type L: 1-5 Minutes Late to OFF (-1 Point)":            -1,
	"Type A: Missed Email Announcment (-2 Points)":          -2,
	"Type A: 1-5 Minutes Late to BLK (-5 Points)":           -5,
	"Type A: Improper Shutdown (-2 Points)":                 -2,
	"Type A: Off-Route (-2 Points)":                         -2,
	"Type A: Out of Uniform (-5 Points)":                    -5,
	"Type A: Improper Radio Procedure (-5 Points)":          -5,
	"Type A: Improper Bus Log (-5 Points)":                  -5,
	"Type A: Timesheet/Improper Book Change (-5 Points)":    -5,
	"Type A: 6-15 Minutes Late to Blk (-3 Points)":          -3,
	"Type B: Attendence Infraction (-10 Points)":            -10,
	"Type B: Passenger Inconvenience (-5 Points)":           -5,
	"Type B: Moving Downed Bus (-10 Points)":                -10,
	"Type B: Improper 10-50 Procedure (-10 Points)":         -10,
	"Type B: 16+ Minutes Late (-5 Points)":                  -5,
	"Type B: Failed Ride-Along/Road Test (-10 Points)":      -10,
	"Type C: Failure to Report 10-50 (-15 Points)":          -15,
	"Type C: Insubordination (-15 Points)":                  -15,
	"Type C: Safety Offense (-15 Points)":                   -15,
	"Type C: Preventable Accident 1,2 (-15 Points)":         -15,
	"Type D: DNS/Did Not Show (-10 Points)":                 -10,
	"Type D: Preventable Accident 3,4 (-20 Points)":         -20,
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
	// Get id of person recieving dpm
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
		FirstName: first,
		LastName:  last,
		Block:     strings.ToUpper(bm.Sanitize(d.Block)),
		Location:  strings.ToUpper(bm.Sanitize(d.Location)),
		Date:      bm.Sanitize(d.Date),
		StartTime: bm.Sanitize(d.StartTime),
		EndTime:   bm.Sanitize(d.EndTime),
		DPMType:   dpmType,
		Points:    points,
		Notes:     bm.Sanitize(d.Notes),
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
	session.Save(r, w)
	return string(sid), nil
}

// cookieSignIn signs in user
func (c Controller) cookieSignIn(w http.ResponseWriter, r *http.Request) (string, error) {
	// Create new session, prompt user to try again if this fails
	sk, err := c.createSession(w, r)
	if err != nil {
		out := fmt.Sprintln("There seems to have been a problem, please try and hopefully it goes away")
		w.WriteHeader(http.StatusOK)
		w.Write([]byte(out))
		return "", err
	}
	return sk, nil
}

// loginError renders login with an error message
func (c Controller) loginError(w http.ResponseWriter, r *http.Request, message string) {
	// Render login template
	err := c.tpl.ExecuteTemplate(w, "login.gohtml", map[string]interface{}{"message": message, "csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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
		w.Write([]byte(out))
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

func returnFile(w http.ResponseWriter, r *http.Request, filename string) {
	//Check if file exists and open
	openfile, err := os.Open(filename)
	defer openfile.Close() //Close after function return
	if err != nil {
		//File not found, send 404
		http.Error(w, "File not found.", 404)
		return
	}

	//File is found, create and send the correct headers

	//Get the Content-Type of the file
	//Create a buffer to store the header of the file in
	fileHeader := make([]byte, 512)
	//Copy the headers into the FileHeader buffer
	openfile.Read(fileHeader)
	//Get content type of file
	fileContentType := http.DetectContentType(fileHeader)

	//Get the file size
	fileStat, _ := openfile.Stat()                     //Get info from file
	fileSize := strconv.FormatInt(fileStat.Size(), 10) //Get file size as a string

	//Send the headers
	w.Header().Set("Content-Disposition", "attachment; filename="+filename)
	w.Header().Set("Content-Type", fileContentType)
	w.Header().Set("Content-Length", fileSize)

	//Send the file
	//We read 512 bytes from the file already, so we reset the offset back to 0
	openfile.Seek(0, 0)
	io.Copy(w, openfile) //'Copy' the file to the client
	return
}

func redirect(w http.ResponseWriter, r *http.Request) bool {
	// Check url and make sure that it is coming from the right domain
	url := r.Host
	if url == "thawing-garden-44847.herokuapp.com" {
		http.Redirect(w, r, "https://www.airfork.icu/login", http.StatusMovedPermanently)
		return true
	}
	return false
}
