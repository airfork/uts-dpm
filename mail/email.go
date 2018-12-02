package mail

import (
	"bytes"
	"fmt"
	"mime/quotedprintable"
	"net/smtp"
	"strings"
)

/**
	Very slightly modified from https://github.com/tangingw/go_smtp
	Mainly added comments and changed a few exported variables to non exported
**/

const (
	/**
		Gmail SMTP Server
	**/
	smtpServer = "smtp.gmail.com"
)

// Holds the information about who is sending the email
type sender struct {
	User     string
	Password string
}

// NewSender creates a new sender struct
func newSender(Username, Password string) sender {

	return sender{Username, Password}
}

// SendMail is what actually sends the mail
func (sender sender) SendMail(Dest []string, Subject, bodyMessage string) {

	msg := "From: " + sender.User + "\n" +
		"To: " + strings.Join(Dest, ",") + "\n" +
		"Subject: " + Subject + "\n" + bodyMessage

	err := smtp.SendMail(smtpServer+":587",
		smtp.PlainAuth("", sender.User, sender.Password, smtpServer),
		sender.User, Dest, []byte(msg))

	if err != nil {

		fmt.Printf("smtp error: %s", err)
		return
	}

	fmt.Println("Mail sent successfully!")
}

// WriteEmail writes the email content
func (sender sender) WriteEmail(dest []string, contentType, subject, bodyMessage string) string {

	header := make(map[string]string)
	header["From"] = sender.User

	receipient := ""

	for _, user := range dest {
		receipient = receipient + user
	}

	header["To"] = receipient
	header["Subject"] = subject
	header["MIME-Version"] = "1.0"
	header["Content-Type"] = fmt.Sprintf("%s; charset=\"utf-8\"", contentType)
	header["Content-Transfer-Encoding"] = "quoted-printable"
	header["Content-Disposition"] = "inline"

	message := ""

	for key, value := range header {
		message += fmt.Sprintf("%s: %s\r\n", key, value)
	}

	var encodedMessage bytes.Buffer

	finalMessage := quotedprintable.NewWriter(&encodedMessage)
	finalMessage.Write([]byte(bodyMessage))
	finalMessage.Close()

	message += "\r\n" + encodedMessage.String()

	return message
}

// WriteHTMLEmail writes the email while allowing html text
func (sender sender) WriteHTMLEmail(dest []string, subject, bodyMessage string) string {

	return sender.WriteEmail(dest, "text/html", subject, bodyMessage)
}

// WritePlainEmail writes the email with plain text
func (sender sender) WritePlainEmail(dest []string, subject, bodyMessage string) string {

	return sender.WriteEmail(dest, "text/plain", subject, bodyMessage)
}
