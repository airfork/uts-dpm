"use strict";

// initialize elements
M.AutoInit(); // Get csrf token

var inputs = document.querySelectorAll('input');
var csrf = inputs[inputs.length - 1].value;
var originalUserName = document.getElementById('username').value; // Get url from from on page

var url = document.getElementById('edit-form').action; // Add event listener to the delete button

document.getElementById('delete-btn').onclick = function () {
  if (confirm('Are you sure you want to delete this user, this cannot be undone?')) {
    // Send request to server
    var request = new XMLHttpRequest();
    request.open('DELETE', url, true); // Set JSON header as well as the CSRF token header, both very important

    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf); // On success, redirect user

    request.onload = function () {
      if (request.status >= 200 && request.status < 400) {
        window.location.replace("/users/find");
      } else {
        // send toast to user on fail
        M.toast({
          html: 'Could not delete user, refresh and try again.'
        });
      }
    };

    request.send();
  }
};

document.getElementById('send-btn').onclick = function () {
  if (confirm("Are you sure you want to update this user?")) {
    document.getElementById('edit-form').submit();
  }
};

document.getElementById('email-btn').onclick = function () {
  if (originalUserName === 'testing@testing.com') {
    M.toast({
      html: 'Can\'t send points balance to a testing account.'
    });
    return;
  }

  if (confirm('Are you sure you want to email this user their point balance?')) {
    // Get id from url variable
    var id = url[url.length - 1]; // Append id to correct url for this action

    var newURL = '/users/points/' + id; // Send request to server

    var request = new XMLHttpRequest();
    request.open('POST', newURL, true); // Set JSON header as well as the CSRF token header, both very important

    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf); // On success, redirect user

    request.onload = function () {
      if (request.status >= 200 && request.status < 400) {
        M.toast({
          html: 'Email sent.'
        });
      } else {
        // send toast to user on fail
        M.toast({
          html: 'Failed to send email to user, please try again.'
        });
      }
    };

    request.send();
  }
};