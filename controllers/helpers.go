package dpm

import (
	"fmt"
	"net/http"
	"strconv"
	"strings"
	"time"

	"github.com/gorilla/csrf"

	"github.com/airfork/dpm_sql/models"
	"github.com/gorilla/securecookie"
	"golang.org/x/crypto/bcrypt"
)

var typeMap = map[string]int16{
	"Type G: Good! (+1 Point Each)":                         1,
	"Type G: 200 Hours Safe (+2 Points)":                    2,
	"Type G: Voluntary Clinic/Road Test Passed (+2 Points)": 2,
	"Type L: 1-5 Minutes Late to OFF (-2 Points)":           -2,
	"Type A: 1-5 Minutes Late to BLK (-5 Points)":           -5,
	"Type A: Improper Shutdown (-5 Points)":                 -5,
	"Type A: Off-Route, No Stops Missed (-5 Points)":        -5,
	"Type A: Out of Uniform (-5 Points)":                    -5,
	"Type A: Improper Radio Procedure (-5 Points)":          -5,
	"Type A: Improper Bus Log (-5 Points)":                  -5,
	"Type A: Timesheet/Improper Book Change (-5 Points)":    -5,
	"Type A: 6-15 Minutes Late to Blk (-7 Points)":          -7,
	"Type B: Off Route, Stops Missed (-7 Points)":           -7,
	"Type B: Passenger Inconvenience (-10 Points)":          -10,
	"Type B: Moving Downed Bus (-10 Points)":                -10,
	"Type B: Improper 10-50 Procedure (-10 Points)":         -10,
	"Type B: 16+ Minutes Late (-10 Points)":                 -10,
	"Type B: Failed Ride-Along/Road Test (-10 Points)":      -10,
	"Type C: Failure to Report 10-50 (-15 Points)":          -15,
	"Type C: Insubordination (-15 Points)":                  -15,
	"Type C: Safety Offense (-15 Points)":                   -15,
	"Type C: Preventable Accident 1,2 (-15 Points)":         -15,
	"Type D: DNS/Did Not Show (-15 Points)":                 -15,
	"Type D: Preventable Accident 3,4 (-20 Points)":         -20,
	"Type A: Custom (-5 Points)":                            -5,
	"Type B: Custom (-10 Points)":                           -10,
	"Type C: Custom (-15 Points)":                           -15,
}

// Creates a DPM from the shortened version taken from client
func generateDPM(d *models.DPMRes) *models.DPM {
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
	if d.DpmType == "Type G: Good! (+1 Point Each)" {
		id64, err = strconv.ParseInt(bm.Sanitize(d.Points), 10, 64)
		if err != nil {
			fmt.Println(err)
			return nil
		}
		points = int16(id64)
	}
	dpm := &models.DPM{
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

// This function returns the userID based on the session ID
func (c Controller) getUser(w http.ResponseWriter, r *http.Request) (*models.User, error) {
	u := &models.User{}
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

func (c Controller) createUserMessage(w http.ResponseWriter, r *http.Request, message string) {
	// Render login template
	err := c.tpl.ExecuteTemplate(w, "createUser.gohtml", map[string]interface{}{"message": message, "csrf": csrf.TemplateField(r)})
	if err != nil {
		out := fmt.Sprintln("Something went wrong, please try again")
		fmt.Println(err)
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(out))
		return
	}
	return
}
