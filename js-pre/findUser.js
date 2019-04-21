// initialize elements
M.AutoInit();
// Get csrf token
var inputs = document.querySelectorAll('input');
const csrf = inputs[inputs.length - 1].value;

document.getElementById('send-btn').onclick = function() {
    if(confirm('Are you sure you want to do this? This will email every user in the system.')) {
        // Send request to server
        var request = new XMLHttpRequest();
        request.open('POST', '/users/points', true);
        // Set JSON header as well as the CSRF token header, both very important
        request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        request.setRequestHeader('X-CSRF-Token', csrf);
        // On success, redirect user
        request.onload = function() {
            if (request.status >= 200 && request.status < 400) {
                M.toast({html: 'Points emails sent.'});
            } else { // send toast to user on fail
                M.toast({html: 'Something went wrong, please try again.'});
            }
        };
        request.send();
    }
};

document.getElementById('reset-btn').onclick = function() {
    if(confirm('Are you sure you want to reset all part timer point balances to zero?')) {
        var request = new XMLHttpRequest();
        request.open('POST', '/users/points/reset', true);
        // Set JSON header as well as the CSRF token header, both very important
        request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        request.setRequestHeader('X-CSRF-Token', csrf);
        // On success, redirect user
        request.onload = function() {
            if (request.status >= 200 && request.status < 400) {
                M.toast({html: 'Point balances reset.'});
            } else { // send toast to user on fail
                M.toast({html: 'Something went wrong, please try again.'});
            }
        };
        request.send();
    }
};
