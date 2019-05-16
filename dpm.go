package main

import (
	"html/template"
	"net/http"

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
func newController(db *sqlx.DB, store *sessions.CookieStore, tpl *template.Template) *Controller {
	return &Controller{db, store, tpl}
}

// Index loads the main page
// Handles all type of requests to "/"
func (c Controller) index(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.renderIndexPage(w, r)
}

// CreateDPM handles the creation of DPM objects and their insertion to the db
// Handles POST requests to "/dpm"
func (c Controller) createDPM(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.createDPMLogic(w, r)
}

// ShowDPM renders the dpm input html file
// Handles requests other than POST to "/dpm"
func (c Controller) showDPM(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.renderDPM(w, r)
}

// Users gets list of all users names, their dpm IDS, and the user name of the user loading page
// Handles GET requests to "/users"
func (c Controller) users(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.getAllUsers(w, r)
}

// User creates a user in the database
// Handles POST requests to "/users"
func (c Controller) user(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.createUser(w, r)
}

// ShowUserCreate shows the html for creating a new user
func (c Controller) showUserCreate(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.renderCreateUser(w, r)
}

// Login handles regular logic of users logging in
// Does not get called on password reset
// Handles GET and POST requests to "/login"
func (c Controller) login(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.logInUser(w, r)
	} else {
		c.renderLogin(w, r)
	}
}

// ChangePass renders the html file for changing a password
// It also handles updating the user in the database
// Handles POST and GET requests to /change
func (c Controller) changePass(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.changeUserPassword(w, r)
	} else {
		c.renderChangeUserPassword(w, r)
	}
}

// Logout removes the user's session
// Handes POST request to /logout
func (c Controller) logout(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.logoutUser(w, r)
}

// Reset handles an admin wanting to reset a user's password
// Handes Get and Post requests to /users/reset
func (c Controller) reset(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.resetPassword(w, r)
	} else {
		c.renderResetPassword(w, r)
	}
}

// AutogenDPM handles the process of autogenerating a dpm report and then submitting the dpms to the db, eventually
// Handles POST and GET requests to /dpm/auto
func (c Controller) autogenDPM(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.callAutoSubmit(w, r)
	} else {
		c.renderAutoGen(w, r)
	}
}

// SendDriverDPM creates a slightly altered DPM and sends it to the client
// as part of ajax call so that they can view more detailed info about
// their DPMs
// Handles GET requests to /dpm/all
func (c Controller) sendDriverDPM(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.sendDriverLogic(w, r)
}

// SendApprovalDPMS sends all unapproved DPMS to the admin requesting the page
// Handles GET requests to /dpm/approve
func (c Controller) sendApprovalDPMS(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.sendApprovalLogic(w, r)
}

// ApproveDPM approves a dpm upon admin request
func (c Controller) approveDPM(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.approveDPMLogic(w, r)
}

// DenyDPM denies a DPM, but keeps it in the database
func (c Controller) denyDPM(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.denyDPMLogic(w, r)
}

// DataPage just renders the data page
func (c Controller) dataPage(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.renderDataPage(w, r)
}

// GetUserXLSX creates an excel file from the users table
func (c Controller) getUserXLSX(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.usersXLSX(w, r)
}

// GetDPMXLSX creates an excel file from the dpm table
func (c Controller) getDPMXLSX(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	c.dpmXLSX(w, r)
}

// findForm renders the find form and handles the searching for a user
// Handles POST?GET requests to /users/find
func (c Controller) findForm(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.findUser(w, r)
	} else {
		c.renderFindUser(w, r)
	}
}

// updateUser handles post and delete requests to /users/edit/{id}
func (c Controller) updateUser(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "DELETE" {
		c.deleteUser(w, r)
	} else if r.Method == "POST" {
		c.editUser(w, r)
	}
}

// sendPointsAll sends the points balance of every user. Responds to post requests to /users/points
func (c Controller) sendPointsAll(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.sendPointsToAll(w, r)
	}
}

// sendPoints sends the points balance of a specific user. Responds to post requests to /users/points/{id}
func (c Controller) sendPoints(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.sendUserPoints(w, r)
	}
}

// resetPoints sets the point balance of all part timers to zero. It also sets all previously approved DPMS to be ignored.
// Responds to post requests to /users/points/reset
func (c Controller) resetPoints(w http.ResponseWriter, r *http.Request) {
	// Redirect if not right domain
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "POST" {
		c.resetPartTimePoints(w, r)
	}
}

func (c Controller) showUsersDPMS(w http.ResponseWriter, r *http.Request) {
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "GET" {
		c.renderUserDPMS(w, r)
	}
}

func (c Controller) showFullUsersDPMS(w http.ResponseWriter, r *http.Request) {
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "GET" {
		c.sendUsersDPMs(w, r)
	}
}

func (c Controller) deleteDPM(w http.ResponseWriter, r *http.Request) {
	v := redirect(w, r)
	if v {
		return
	}
	if r.Method == "DELETE" {
		c.removeDPMPostLogic(w, r)
	}
}
