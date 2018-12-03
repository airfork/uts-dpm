package dpm

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
func NewController(db *sqlx.DB, store *sessions.CookieStore, tpl *template.Template) *Controller {
	return &Controller{db, store, tpl}
}

// Index loads the main page
// Handles all type of requests to "/"
func (c Controller) Index(w http.ResponseWriter, r *http.Request) {
	c.showIndexTemp(w, r)
}

// CreateDPM handles the creation of DPM objects and their insertion to the db
// Handles POST requests to "/dpm"
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
// Handles GET requests to /dpm/all
func (c Controller) SendDriverDPM(w http.ResponseWriter, r *http.Request) {
	c.sendDriverLogic(w, r)
}

// SendApprovalDPMS sends all unapproved DPMS to the admin requesting the page
// Handles GET requests to /dpm/approve
func (c Controller) SendApprovalDPMS(w http.ResponseWriter, r *http.Request) {
	c.sendApprovalLogic(w, r)
}

// ApproveDPM approves a dpm upon admin request
func (c Controller) ApproveDPM(w http.ResponseWriter, r *http.Request) {
	c.approveDPMLogic(w, r)
}

// DenyDPM denies a DPM, but keeps it in the database
func (c Controller) DenyDPM(w http.ResponseWriter, r *http.Request) {
	c.denyDPMLogic(w, r)
}
