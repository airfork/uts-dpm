package main

import (
	"fmt"
	"log"
	"os"
	"strings"

	mailgun "github.com/mailgun/mailgun-go"
	"github.com/matcornic/hermes/v2"
)

var yourDomain = "utsdpm.com"

var privateAPIKey = os.Getenv("MAILGUN_KEY")

// sendPasswordChanged sends confirmation of a changed password to user
func sendPasswordChanged(recipient, firstname, lastname string) {
	h := hermes.Hermes{
		// Optional Theme
		// Theme: new(Default)
		Product: hermes.Product{
			// Appears in header & footer of e-mails
			Name:      "University Transit Service",
			Link:      "https://www.utsdpm.com/",
			Copyright: "Copyright © 2019 University Transit Service. All rights reserved.",
		},
	}

	email := hermes.Email{
		Body: hermes.Body{
			Greeting: "",
			Name:     firstname + " " + lastname,
			Intros: []string{
				"Your password has been successfully changed. If you did not authorize this change, please contact an admin.",
			},
		},
	}
	// Generate an HTML email with the provided contents (for modern clients)
	body, err := h.GenerateHTML(email)
	if err != nil {
		fmt.Println("Failed to generate email")
		fmt.Println(err)
	}
	// Generate the plaintext version of the e-mail (for clients that do not support xHTML)
	emailText, err := h.GeneratePlainText(email)
	if err != nil {
		fmt.Println("Failed to generate plaintext")
		fmt.Println(err)
	}

	// Create an instance of the Mailgun Client
	mg := mailgun.NewMailgun(yourDomain, privateAPIKey)

	sender := "DPM@utsdpm.com"
	subject := "Password Changed"

	sendMessage(mg, sender, subject, emailText, body, recipient)
}

// sendResetPasswordEmail sends an email to user after their password has been reset by an admin
func sendResetPasswordEmail(recipient, pass, firstname, lastname string) {
	h := hermes.Hermes{
		// Optional Theme
		// Theme: new(Default)
		Product: hermes.Product{
			// Appears in header & footer of e-mails
			Name:      "University Transit Service",
			Link:      "https://www.utsdpm.com/",
			Copyright: "Copyright © 2019 University Transit Service. All rights reserved.",
		},
	}

	email := hermes.Email{
		Body: hermes.Body{
			Greeting: "",
			Name:     firstname + " " + lastname,
			Intros: []string{
				"Your password has been reset.",
			},
			Dictionary: []hermes.Entry{
				{Key: "Temporary Password", Value: pass},
			},
			Actions: []hermes.Action{
				{
					Instructions: `Please copy your temporary password and sign in to change your password.`,
					Button: hermes.Button{
						Color:     "#0d47a1",
						TextColor: "#ffffff",
						Text:      "Log In",
						Link:      "https://www.utsdpm.com/login",
					},
				},
			},
			Outros: []string{
				"If this email was unexpected, please contact an admin and or manager.",
			},
		},
	}

	// Generate an HTML email with the provided contents (for modern clients)
	body, err := h.GenerateHTML(email)
	if err != nil {
		fmt.Println("Failed to generate email")
		fmt.Println(err)
	}
	// Generate the plaintext version of the e-mail (for clients that do not support xHTML)
	emailText, err := h.GeneratePlainText(email)
	if err != nil {
		fmt.Println("Failed to generate plaintext")
		fmt.Println(err)
	}

	// Create an instance of the Mailgun Client
	mg := mailgun.NewMailgun(yourDomain, privateAPIKey)

	sender := "DPM@utsdpm.com"
	subject := "Password Reset"

	sendMessage(mg, sender, subject, emailText, body, recipient)
}

// sendNewUserEmail sends email to user when then are initially added to the app
func sendNewUserEmail(recipient, pass, firstname, lastname string) {
	h := hermes.Hermes{
		// Optional Theme
		// Theme: new(Default)
		Product: hermes.Product{
			// Appears in header & footer of e-mails
			Name:      "University Transit Service",
			Link:      "https://www.utsdpm.com/",
			Copyright: "Copyright © 2019 University Transit Service. All rights reserved.",
		},
	}

	email := hermes.Email{
		Body: hermes.Body{
			Name: firstname + " " + lastname,
			Intros: []string{
				"You have been added to UTS DPM.",
			},
			Dictionary: []hermes.Entry{
				{Key: "Temporary Password", Value: pass},
			},
			Actions: []hermes.Action{
				{
					Instructions: `To get started, please copy your temporary password and use it to sign in along with your email address.`,
					Button: hermes.Button{
						Color:     "#0d47a1",
						TextColor: "#ffffff",
						Text:      "Log In",
						Link:      "https://www.utsdpm.com/login",
					},
				},
			},
			Outros: []string{
				"If this email was unexpected, please ignore it or contact an admin and or manager.",
			},
		},
	}

	// Generate an HTML email with the provided contents (for modern clients)
	body, err := h.GenerateHTML(email)
	if err != nil {
		fmt.Println("Failed to generate email")
		fmt.Println(err)
	}
	// Generate the plaintext version of the e-mail (for clients that do not support xHTML)
	emailText, err := h.GeneratePlainText(email)
	if err != nil {
		fmt.Println("Failed to generate plaintext")
		fmt.Println(err)
	}

	// Create an instance of the Mailgun Client
	mg := mailgun.NewMailgun(yourDomain, privateAPIKey)

	sender := "DPM@utsdpm.com"
	subject := "Welcome to UTS DPM"

	sendMessage(mg, sender, subject, emailText, body, recipient)
}

// sendPointsBalance sends the user's current point balance
func sendPointsBalance(recipient, firstname, lastname, points string) {
	h := hermes.Hermes{
		// Optional Theme
		// Theme: new(Default)
		Product: hermes.Product{
			// Appears in header & footer of e-mails
			Name:      "University Transit Service",
			Link:      "https://www.utsdpm.com/",
			Copyright: "Copyright © 2019 University Transit Service. All rights reserved.",
		},
	}

	email := hermes.Email{
		Body: hermes.Body{
			Greeting: "",
			Name:     firstname + " " + lastname,
			Intros: []string{
				"Below is your current points balance. To get more details about this value, please talk to your manager.",
			},
			Dictionary: []hermes.Entry{
				{Key: "Points Balance", Value: points},
			},
		},
	}
	// Generate an HTML email with the provided contents (for modern clients)
	body, err := h.GenerateHTML(email)
	if err != nil {
		fmt.Println("Failed to generate email")
		fmt.Println(err)
	}
	// Generate the plaintext version of the e-mail (for clients that do not support xHTML)
	emailText, err := h.GeneratePlainText(email)
	if err != nil {
		fmt.Println("Failed to generate plaintext")
		fmt.Println(err)
	}

	// Create an instance of the Mailgun Client
	mg := mailgun.NewMailgun(yourDomain, privateAPIKey)

	sender := "DPM@utsdpm.com"
	subject := "DPM Point Balance"

	sendMessage(mg, sender, subject, emailText, body, recipient)
}

func sendDPMEmail(recipient, firstname, lastname, dpmtype string, points int) {
	var pointString string
	switch {
	case points == 0, points < -1:
		pointString = fmt.Sprintf("(%d Points)", points)
	case points == -1:
		pointString = fmt.Sprintf("(%d Point)", points)
	case points == 1:
		pointString = fmt.Sprintf("(+%d Point)", points)
	default:
		pointString = fmt.Sprintf("(+%d Points)", points)
	}
	// Add space to dpmtype to make string manipulation easier
	dpmtype += " "
	// Gets letter of DPM, eg. G
	letter := fmt.Sprintf("[%s]", dpmtype[5:6])
	// Gets the part of dpm past Type[G]:, but minus the points in parenthesis
	colonIndex := strings.Index(dpmtype, ":") + 2
	parenIndex := strings.Index(dpmtype, "(") - 1
	description := dpmtype[colonIndex:parenIndex]
	out := fmt.Sprintf("Type %s DPM: %s %s", letter, description, pointString)
	subject := fmt.Sprintf("%s: %s", dpmtype[0:6], description)
	message := fmt.Sprintf("This email is to inform you that you have received a %s. If you have any issues with this, please contact %s directly.", out, os.Getenv("BOSS"))
	if (firstname + " " + lastname) == os.Getenv("BOSS") {
		message = "You got a DPM, anyways I hope you're having a nice day!"
	}
	h := hermes.Hermes{
		// Optional Theme
		// Theme: new(Default)
		Product: hermes.Product{
			// Appears in header & footer of e-mails
			Name:      "University Transit Service",
			Link:      "https://www.utsdpm.com/",
			Copyright: "Copyright © 2019 University Transit Service. All rights reserved.",
		},
	}

	email := hermes.Email{
		Body: hermes.Body{
			Name: firstname + " " + lastname,
			Intros: []string{
				message,
			},
		},
	}
	// Generate an HTML email with the provided contents (for modern clients)
	body, err := h.GenerateHTML(email)
	if err != nil {
		fmt.Println("Failed to generate email")
		fmt.Println(err)
	}
	// Generate the plaintext version of the e-mail (for clients that do not support xHTML)
	emailText, err := h.GeneratePlainText(email)
	if err != nil {
		fmt.Println("Failed to generate plaintext")
		fmt.Println(err)
	}

	// Create an instance of the Mailgun Client
	mg := mailgun.NewMailgun(yourDomain, privateAPIKey)

	sender := "DPM@utsdpm.com"

	sendMessage(mg, sender, subject, emailText, body, recipient)
}

// sendMessage handles actually sending out the email
func sendMessage(mg mailgun.Mailgun, sender, subject, body, html, recipient string) {
	message := mg.NewMessage(sender, subject, body, recipient)
	message.SetHtml(html)
	resp, id, err := mg.Send(message)

	if err != nil {
		log.Fatal(err)
	}

	fmt.Printf("ID: %s Resp: %s\n", id, resp)
}
