"use strict";

// Holds names of all the users for autocomplete box
var people = []; // Relates users to their ids

var peopleIds = []; // Holds the userID of the person loading the page

var userID = ""; //Datepicker element

var datepicker = null; // Send AJAX call to server, to fill up arrays

var request = new XMLHttpRequest();
request.open('GET', '/users', true);

request.onload = function () {
  if (request.status >= 200 && request.status < 400) {
    // Success!
    var data = JSON.parse(request.responseText); // Get ids, people, and the username

    peopleIds = data.ids;
    people = data.names;
    userID = data.userID;
    var dataobj = {};
    people.forEach(function (name) {
      dataobj[name] = null;
    });
    var elems = document.querySelectorAll('.autocomplete');
    var instances = M.Autocomplete.init(elems, {
      data: dataobj,
      limit: 5
    });
  } else {
    // We reached our target server, but it returned an error
    console.log('Error');
  }
};

request.onerror = function () {
  // There was a connection error of some sort
  console.log("There was an error of sometype, please try again");
};

request.send(); // Get the last input box on the page, which holds a csrf token, and store it

var inputs = document.querySelectorAll('input');
var csrf = inputs[inputs.length - 1].value; // Parses the inputs and gets information from them
// Creates and sends JSON object for the server to handle

function submitLogic() {
  // Takes all of these fields and puts them in an object
  var obj = {};
  obj.name = document.getElementById('autocomplete-input').value;

  if (people.indexOf(obj.name) == -1) {
    return "name";
  }

  obj.block = document.getElementById('block').value.toUpperCase();
  obj.location = document.getElementById('location').value.toUpperCase();
  obj.date = document.getElementById('date').value;

  if (obj.date == "") {
    return "date";
  }

  obj.startTime = document.getElementById('starttime').value;
  obj.endTime = document.getElementById('endtime').value;

  if (obj.startTime == "" || obj.endTime == "") {
    return "time";
  }

  obj.notes = document.getElementById('notes').value;
  obj.dpmType = document.getElementById('type').value;
  obj.sender = userID;
  id = peopleIds[people.indexOf(obj.name)];
  obj.id = id.toString();
  obj.points = '0'; // Clear out text inputs

  document.querySelectorAll('.input').forEach(function (input) {
    input.value = "";
  }); // Remove active class from labels

  document.querySelectorAll('label').forEach(function (label) {
    label.classList.remove('active');
  }); // Reset date picker

  datePickerInit(); // Create JSON, then POST to server

  var jObj = JSON.stringify(obj);
  sendDPM(jObj);
  return true;
} // Actually sends the JSON


function sendDPM(jOBJ) {
  var request = new XMLHttpRequest();
  request.open('POST', '/dpm', true); // Set JSON header as well as the CSRF token header, both very important

  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
  request.setRequestHeader('X-CSRF-Token', csrf);
  request.send(jOBJ);
} // Dealing with timepicker forms


document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('.timepicker');
  var instances = M.Timepicker.init(elems, {
    twelveHour: false,
    showClearBtn: true,
    autoClose: true
  });
}); // dealing with date picker

function datePickerInit() {
  var elems = document.querySelectorAll('.datepicker');
  M.Datepicker.init(elems, {
    format: 'yyyy-mm-dd',
    defaultDate: new Date(),
    setDefaultDate: true
  });
}

document.addEventListener('DOMContentLoaded', datePickerInit); // dealing with selects

document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('select');
  var instances = M.FormSelect.init(elems);
}); // dealing with floating button

document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('.fixed-action-btn');
  var instances = M.FloatingActionButton.init(elems);

  elems[0].onclick = function () {
    submitted = submitLogic();

    if (submitted == "name") {
      M.toast({
        html: 'Please input a valid name.'
      });
    } else if (submitted == "date") {
      M.toast({
        html: 'Please provide a date.'
      });
    } else if (submitted == "time") {
      M.toast({
        html: 'Please input a start and end time.'
      });
    } else {
      M.toast({
        html: 'DPM Submitted!'
      });
    }
  };
});
document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('.sidenav');
  var instances = M.Sidenav.init(elems);
});