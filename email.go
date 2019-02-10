package main

import (
	"fmt"
	"log"
	"os"

	mailgun "github.com/mailgun/mailgun-go"
	"github.com/matcornic/hermes/v2"
)

var yourDomain = "airfork.icu"

var privateAPIKey = os.Getenv("MAILGUN_KEY")

// sendPasswordChanged sends confirmation of a changed password to user
func sendPasswordChanged(recipient, firstname, lastname string) {
	h := hermes.Hermes{
		// Optional Theme
		// Theme: new(Default)
		Product: hermes.Product{
			// Appears in header & footer of e-mails
			Name:      "University Transit Service",
			Link:      "https://www.airfork.icu/",
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

	sender := "mail@airfork.icu"
	subject := "Password Has Been Reset"

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
			Link:      "https://www.airfork.icu/",
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
						// Color: "#22BC66", // Optional action button color
						Text: "Log In",
						Link: "https://www.airfork.icu/login",
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

	sender := "mail@airfork.icu"
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
			Link:      "https://www.airfork.icu/",
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
					Instructions: `To get started, please copy your temporay password and use it sign in along with your email address.`,
					Button: hermes.Button{
						// Color: "#22BC66", // Optional action button color
						Text: "Log In",
						Link: "https://www.airfork.icu/login",
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

	sender := "mail@airfork.icu"
	subject := "Welcome to UTS DPM"

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
