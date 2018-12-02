package models

// User defines what a user should look like
type User struct {
	ID         int16  `json:"id"`          // User ID in the database, unique
	Username   string `json:"username"`    // Username of user, unique
	Password   string `json:"password"`    // Hash of password + plus salt
	FirstName  string `json:"firstName"`   // First name of the user
	LastName   string `json:"lastName"`    // Last name of the user
	FullTime   bool   `json:"fullTime"`    // Is user a fulltime driver
	Changed    bool   `json:"changed"`     // Have they changed their initial password?
	Admin      bool   `json:"admin"`       // Admin priveleges?
	Sup        bool   `json:"sup"`         // Supervisor priveleges?
	Analysist  bool   `json:"analysist"`   // Analysist priveleges
	Points     int16  `json:"points"`      // How many points a driver has
	SessionKey string `json:"session_key"` // Session key that tracks if the user is signed in or not
	Added      string `json:"added"`       // Time user was added to the system
}

// DPM holds all the information about a submitted DPM
type DPM struct {
	ID        int16  `json:"id"`        // ID of the DPM in the database, unique
	CreateID  int16  `json:"createID"`  // UserID relates to user ID of person who created the DPM
	UserID    int16  `json:"userID"`    // User who is being written the ID
	FirstName string `json:"firstName"` // First name of the user
	LastName  string `json:"lastName"`  // Last name of the user
	Block     string `json:"block"`     // Block number
	Location  string `json:"location"`  // Location
	Date      string `json:"date"`      // Data DPM is issued for
	StartTime string `json:"startTime"` // Start time for the DPM
	EndTime   string `json:"endTime"`   // End time for the DPM
	DPMType   string `json:"dpmtype"`   // Type of DPM
	Points    int16  `json:"points"`    // Number of points DPM is worth
	Notes     string `json:"notes"`     // Any extra notes about the DPM
	Created   string `json:"created"`   // Time DPM was created
	Approved  bool   `json:"approved"`  // If the DPM is approved or not
}

// DPMDriver is used for sending dpm info to the client when they
// want to view their dpms
type DPMDriver struct {
	FirstName string `json:"firstName"` // First name of the user
	LastName  string `json:"lastName"`  // Last name of the user
	Block     string `json:"block"`     // Block number
	Location  string `json:"location"`  // Location
	Date      string `json:"date"`      // Data DPM is issued for
	StartTime string `json:"startTime"` // Start time for the DPM
	EndTime   string `json:"endTime"`   // End time for the DPM
	DPMType   string `json:"dpmtype"`   // Type of DPM
	Points    string `json:"points"`    // Number of points DPM is worth
	Notes     string `json:"notes"`     // Any extra notes about the DPM
}

// DPMRes is solely for getting response from client. Not all the required information exists client side
// the full DPM struct to be used
type DPMRes struct {
	Name      string // Inputed name, needs to be split into first and Last
	Block     string
	Location  string
	Date      string
	StartTime string
	EndTime   string
	Notes     string
	DpmType   string
	Sender    string
	ID        string
	Points    string
}

// DPMApprove is used for sending to admin's approval page
type DPMApprove struct {
	ID        string `json:"id"`        // ID of DPM
	Name      string `json:"name"`      // Full name of the driver
	SupName   string `json:"supName"`   // Name of the supervisor that submitted this DPM
	Block     string `json:"block"`     // Block number
	Location  string `json:"location"`  // Location
	Date      string `json:"date"`      // Data DPM is issued for
	StartTime string `json:"startTime"` // Start time for the DPM
	EndTime   string `json:"endTime"`   // End time for the DPM
	DPMType   string `json:"dpmtype"`   // Type of DPM
	Points    string `json:"points"`    // Number of points DPM is worth
	Notes     string `json:"notes"`     // Any extra notes about the DPM
	Created   string `json:"created"`   // Time DPM was created
}
