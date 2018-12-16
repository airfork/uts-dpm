package main

import (
	"fmt"
	"html/template"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/gorilla/csrf"
	"github.com/gorilla/mux"
	"github.com/gorilla/sessions"
	"github.com/jmoiron/sqlx"
	_ "github.com/lib/pq"
)

// var tpl = template.Must(template.ParseFiles("views/index.gohtml", "views/dpm.gohtml", "views/login.gohtml", "views/changePass.gohtml", "views/createUser.gohtml", "views/resetPassword.gohtml", "views/autogen.gohtml", "views/autogenErr.gohtml"))
var tpl = template.Must(template.ParseGlob("views/*.gohtml"))
var store = sessions.NewCookieStore(
	[]byte(os.Getenv("SESSION_KEY")),
	[]byte(os.Getenv("ENCRYPTION_KEY")))

func init() {
	// Sets all cookies stored in this cookie store to have these values
	store.Options = &sessions.Options{
		Path:     "/",
		MaxAge:   86400, // Max age of one day
		HttpOnly: true,
	}
}

func main() {
	r := mux.NewRouter()
	c := NewController(getSession(), store, tpl)
	// Creates some timeout rules for connections
	// Using the regular http.ListenAndServe does not set any timeout values, and this is a bad thing
	srv := http.Server{
		ReadTimeout:  5 * time.Second,
		WriteTimeout: 10 * time.Second,
		IdleTimeout:  120 * time.Second,
		Addr:         ":8080",
		Handler:      csrf.Protect([]byte(os.Getenv("CSRF_KEY")), csrf.Secure(false))(r),
	}
	r.HandleFunc("/", c.Index)
	r.HandleFunc("/dpm", c.CreateDPM).Methods("POST")
	r.HandleFunc("/dpm", c.ShowDPM).Methods("GET")
	r.HandleFunc("/dpm/all", c.SendDriverDPM).Methods("GET")
	r.HandleFunc("/dpm/auto", c.AutogenDPM).Methods("GET", "POST")
	r.HandleFunc("/approve", c.RenderApprovals).Methods("GET")
	r.HandleFunc("/dpm/approve", c.SendApprovalDPMS).Methods("GET")
	r.HandleFunc("/dpm/approve/{id}", c.ApproveDPM).Methods("POST")
	r.HandleFunc("/dpm/deny/{id}", c.DenyDPM).Methods("POST")
	r.HandleFunc("/users", c.User).Methods("POST")
	r.HandleFunc("/users", c.Users).Methods("GET")
	r.HandleFunc("/users/create", c.ShowUserCreate).Methods("GET")
	r.HandleFunc("/users/reset", c.Reset).Methods("POST", "GET")
	r.HandleFunc("/login", c.Login).Methods("POST", "GET")
	r.HandleFunc("/logout", c.Logout)
	r.HandleFunc("/change", c.ChangePass).Methods("POST", "GET")
	r.HandleFunc("/data", c.DataPage).Methods("GET")
	r.HandleFunc("/data/users", c.GetUserCSV).Methods("GET")
	r.HandleFunc("/data/dpms", c.GetDPMCSV).Methods("GET")
	r.PathPrefix("/views/").Handler(http.StripPrefix("/views/", http.FileServer(http.Dir("views/"))))
	r.PathPrefix("/assets/").Handler(http.StripPrefix("/assets/", http.FileServer(http.Dir("assets/"))))
	http.Handle("/", r)
	fmt.Println("Server started on port 8080")
	log.Fatal(srv.ListenAndServe())
}

// Connect to database and return a pointer to than connection
func getSession() *sqlx.DB {
	// connStr := "user=tunji dbname=balloon password=" + os.Getenv("PSQL_PASS") + " sslmode=verify-full"
	db, err := sqlx.Open("postgres", os.Getenv("DATABASE_URL"))
	if err != nil {
		panic(err)
	}
	err = db.Ping()
	if err != nil {
		panic(err)
	}
	return db
}
