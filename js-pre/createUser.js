'use strict';
// Materialize components initialized
M.AutoInit();

// Get the last input box on the page, which holds a csrf token, and store it
const inputs = document.querySelectorAll('input');
const csrf = inputs[inputs.length - 1].value;

document.getElementById('createUser').onclick = () => {
    const result = getData();
    if (result !== false) {
        let proceed = false;
        if (result.queue === true) {
            proceed = confirm('Are you sure you want to add this user without notifying them? Please ensure that all data is entered correctly.');
        } else {
            proceed = confirm('Are you sure you want to add and notify this user? Please ensure that all data is entered correctly.');
        }
        if (proceed) {
            const request = new XMLHttpRequest();
            request.open('POST', '/users', true);
            request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
            request.setRequestHeader('X-CSRF-Token', csrf);
            request.onload = function() {
                if (request.status >= 200 && request.status < 400) {
                    const data = JSON.parse(request.responseText);
                    if (data.Error !== "") {
                        M.toast({html: data.Error});
                        return;
                    }
                    // Clear out text inputs
                    const inputList = document.querySelectorAll('input');
                    for (var i = 0; i < inputList.length - 1; i++) {
                        inputList[i].value = "";
                    }
                    // Remove active class from labels
                    const labelList = document.querySelectorAll('label');
                    for (var i = 0; i < labelList.length; i++) {
                        labelList[i].classList.remove('active');
                    }
                    document.getElementById('manager-select').selectedIndex = 0;
                    document.getElementById('role-select').selectedIndex = 0;
                    document.getElementById('fulltime').checked = false;
                    document.getElementById('queue').checked = false;
                    M.AutoInit();
                    M.toast({html: 'User successfully created'});
                } else {
                    // We reached our target server, but it returned an error
                    console.log('Error');
                    M.toast({html: 'There was an error, please try again'});
                }
            };

            request.onerror = function() {
                // There was a connection error of some sort
                M.toast({html: 'There was an error, please try again'});
            };

            request.send(JSON.stringify(result));
        }
    }
};

// Get data from fields and prompt user if there are any missing ones
function getData() {
    let obj = {
        username: document.getElementById('username').value,
        firstname: document.getElementById('firstname').value,
        lastname: document.getElementById('lastname').value,
        manager: document.getElementById('manager-select').value,
        role: document.getElementById('role-select').value,
        fulltime: document.getElementById('fulltime').checked,
        queue: document.getElementById('queue').checked,
    };

    if (obj.username === undefined || obj.username.trim() === "") {
        M.toast({html: 'Please enter a username.'});
        return false;
    }
    if (obj.firstname === undefined || obj.firstname.trim() === "") {
        M.toast({html: 'Please enter a first name.'});
        return false;
    }
    if (obj.lastname === undefined || obj.lastname.trim() === "") {
        M.toast({html: 'Please enter a last name.'});
        return false;
    }
    if (obj.manager === undefined || obj.manager.trim() === "") {
        M.toast({html: 'Please select a manager'});
        return false;
    }
    if (obj.role === undefined || obj.role.trim() === "") {
        M.toast({html: 'Please select a role'});
        return false;
    }
    if ((obj.fulltime !== false && obj.fulltime !== true) || (obj.queue !== false && obj.queue !== true)) {
        M.toast({html: 'Please refresh the page and try again'});
        return false;
    }
    return obj;
}