"use strict";

// initialize elements
M.AutoInit(); // Get csrf token

var inputs = document.querySelectorAll('input');
var csrf = inputs[inputs.length - 1].value; // Get url from from on page

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