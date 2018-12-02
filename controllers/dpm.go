package dpm

import (
	"encoding/json"
	"fmt"
	"html/template"
	"net/http"

	"github.com/airfork/dpm_sql/models"
	"github.com/gorilla/sessions"
	"github.com/jmoiron/sqlx"
	"github.com/microcosm-cc/bluemonday"
)

// If in any function you notice ' if r.method == "POST" ', note that it is unecessary as I can specifiy  http actions via the Gorrila mux, I only do so here because I am used to using the default mux where it is required, I also like being redundant at times

// Variable to allow me sanitize data
var bm = bluemonday.StrictPolicy()

// Controller holds the database pool, the cookie store, and any templates to be parsed
type Controller struct {
	db    *sqlx.DB              // Database session, needed for any calls to db
	store *sessions.CookieStore // Needed to do anything with sessions
	tpl   *template.Template    // Needed to execute parsed templates/html files
}

// NewController returns a pointer a struct that contains all the route functions
func NewController(db *sqlx.DB, store *sessions.CookieStore, tpl *template.Template) *Controller {
	return &Controller{db, store, tpl}
}

// Index loads the main page
// Handles all type of requests to "/"
func (c Controller) Index(w http.ResponseWriter, r *http.Request) {
	c.showIndexTemp(w, r)
}

// CreateDPM for now, just prints out what I get from JS
// Will eventually create a DPM and insert it into the database
// Handles POST requests tp "/dpm"
func (c Controller) CreateDPM(w http.ResponseWriter, r *http.Request) {
	c.createDPMLogic(w, r)
}

// ShowDPM renders the dpm input html file
// Handles requests other than POST to "/dpm"
func (c Controller) ShowDPM(w http.ResponseWriter, r *http.Request) {
	c.renderDPM(w, r)
}

// Users gets list of all users names, their dpm IDS, and the user name of the user loading page
// Handles GET requests to "/users"
func (c Controller) Users(w http.ResponseWriter, r *http.Request) {
	c.getAllUsers(w, r)
}

// User creates a user in the database
// Handles POST requests to "/users"
func (c Controller) User(w http.ResponseWriter, r *http.Request) {
	c.createUser(w, r)
}

// ShowUserCreate shows the html for creating a new user
func (c Controller) ShowUserCreate(w http.ResponseWriter, r *http.Request) {
	c.renderCreateUser(w, r)
}

// Login handles regular logic of users logging in
// Does not get called on password reset
// Handles GET and POST requests to "/login"
func (c Controller) Login(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		c.logInUser(w, r)
	} else {
		c.renderLogin(w, r)
	}
}

// ChangePass renders the html file for changing a password
// It also handles updating the user in the database
// Handles POST and GET requests to /change
func (c Controller) ChangePass(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		c.changeUserPassword(w, r)
	} else {
		c.renderChangeUserPassword(w, r)
	}
}

// Logout removes the user's session
// Handes POST request to /logout
func (c Controller) Logout(w http.ResponseWriter, r *http.Request) {
	c.logoutUser(w, r)
}

// Reset handles an admin wanting to reset a user's password
// Handes Get and Post requests to /users/reset
func (c Controller) Reset(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		c.resetPassword(w, r)
	} else {
		c.renderResetPassword(w, r)
	}
}

// AutogenDPM handles the process of autogenerating a dpm report and then submitting the dpms to the db, eventually
// Handles POST and GET requests to /dpm/auto
func (c Controller) AutogenDPM(w http.ResponseWriter, r *http.Request) {
	if r.Method == "POST" {
		c.callAutoSubmit(w, r)
	} else {
		c.renderAutoGen(w, r)
	}
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

// SendApprovalDPMS sends all unapproved DPMS to the admin requesting the page
func (c Controller) SendApprovalDPMS(w http.ResponseWriter, r *http.Request) {
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
	stmt := `SELECT id, createid, firstname, lastname, block, location, date, starttime, endtime, dpmtype, points, notes, created FROM dpms WHERE approved=false ORDER BY created DESC`
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
