package mail

import (
	"os"
	"strings"
)

var gmail = os.Getenv("GMAIL_APP")

// SendTempPass sends user an email with temporary password for initial login to site
func SendTempPass(email, tempPass string) {
	s := newSender("airfork@gmail.com", gmail)
	//The receiver needs to be in slice as the receive supports multiple receiver
	receiver := []string{email}

	subject := "Temporary Password"
	var sb strings.Builder
	sb.WriteString(`
	<!DOCTYPE HTML PULBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	<html>
	<head>
	<meta http-equiv="content-type" content="text/html"; charset=ISO-8859-1">
	</head>
	<body>
	<p>Hey there, here is your temporary password, make sure to change this on login!</p>
	<p><strong>`)
	sb.WriteString(tempPass + `</strong></p>`)
	sb.WriteString(`
	<div class="moz-signature"><i><br>
	<br>
	Regards<br>
	Tunji<br>
	<i></div>
	</body>
	</html>
	`)
	bodyMessage := s.WriteHTMLEmail(receiver, subject, sb.String())

	s.SendMail(receiver, subject, bodyMessage)
}

// SendAdminTempPass sends user an email with temporary password
// This is for when an admin resets their password
func SendAdminTempPass(email, tempPass string) {
	s := newSender("airfork@gmail.com", gmail)
	//The receiver needs to be in slice as the receive supports multiple receiver
	receiver := []string{email}

	subject := "Temporary Password"
	var sb strings.Builder
	sb.WriteString(`
	<!DOCTYPE HTML PULBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	<html>
	<head>
	<meta http-equiv="content-type" content="text/html"; charset=ISO-8859-1">
	</head>
	<body>
	<p>Hey there, an administrator has reset your password, please login with the password below to create a new password</p>
	<p><strong>`)
	sb.WriteString(tempPass + `</strong></p>`)
	sb.WriteString(`
	<div class="moz-signature"><i><br>
	<br>
	Regards<br>
	Tunji<br>
	<i></div>
	</body>
	</html>
	`)
	bodyMessage := s.WriteHTMLEmail(receiver, subject, sb.String())

	s.SendMail(receiver, subject, bodyMessage)
}

// SendPassChangeMail sends user an email notifying them that their password has been changed
func SendPassChangeMail(email string) {
	s := newSender("airfork@gmail.com", gmail)
	//The receiver needs to be in slice as the receive supports multiple receiver
	receiver := []string{email}

	subject := "Password Changed"
	var sb strings.Builder
	sb.WriteString(`
	<!DOCTYPE HTML PULBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	<html>
	<head>
	<meta http-equiv="content-type" content="text/html"; charset=ISO-8859-1">
	</head>
	<body>
	<p>Your password has been changed, if you did not authorize this, please contact an administrator.</p>
	<div class="moz-signature"><i><br>
	<br>
	Regards<br>
	Tunji<br>
	<i></div>
	</body>
	</html>
	`)
	bodyMessage := s.WriteHTMLEmail(receiver, subject, sb.String())

	s.SendMail(receiver, subject, bodyMessage)
}

// NegativeDPMEmail sends a email informing the drive that they have received a negative dpm
// Only happens with parttimers
func NegativeDPMEmail(email, dpmtype string) {
	s := newSender("airfork@gmail.com", gmail)
	//The receiver needs to be in slice as the receive supports multiple receiver
	receiver := []string{email}

	subject := dpmtype
	var sb strings.Builder
	sb.WriteString(`
	<!DOCTYPE HTML PULBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
	<html>
	<head>
	<meta http-equiv="content-type" content="text/html"; charset=ISO-8859-1">
	</head>
	<body>
	<p>This email is to inform you that you have received a negative DPM:` + dpmtype + `. If you have any issures with this, please contact Allison Day directly</p>
	</body>
	</html>
	`)
	bodyMessage := s.WriteHTMLEmail(receiver, subject, sb.String())
	s.SendMail(receiver, subject, bodyMessage)
}
