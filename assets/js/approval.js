"use strict";

// Get the last input box on the page, which holds a csrf token, and store it
var inputs = document.querySelectorAll('input');
var csrf = inputs[inputs.length - 1].value;
var backdrop = document.querySelector('.backdrop');
var dataList = [];
var objectList = [];
var modal = null; // Send AJAX call to server, to get DPM data

var request = new XMLHttpRequest();
request.open('GET', '/dpm/approve', true);

request.onload = function () {
  if (request.status >= 200 && request.status < 400) {
    // Success!
    var data = JSON.parse(request.responseText); // Get items from data into dataList

    for (var i = 0; i < data.length; i++) {
      dataList[i] = data[i];
    }

    var modalText = document.querySelector('.modal-content'); // Add event listener to each dpm that pulls up more information

    var dpmList = document.querySelectorAll('.dpm');

    for (var i = 0; i < dpmList.length; i++) {
      // Push item to object list so I know which dpm is being clicked
      objectList.push(item);

      item.onclick = function () {
        // Each object is mapped by index to dpm data
        // Do find dpm data based on index of clicked dpm
        var list = dataList[objectList.indexOf(this)];
        var timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
        modalText.innerHTML = "<p>Driver: ".concat(list.name, "</p>\n            <p>Supervisor: ").concat(list.supName, "\n            <p>Points: ").concat(list.points, "</p>\n            <p>").concat(list.dpmtype, "</p>\n            <p>Block: ").concat(list.block, "</p>\n            <p>Location: ").concat(list.location, "</p>\n            <p>Date: ").concat(timeObj.date, "</p>\n            <p>Time: ").concat(timeObj.startTime, "-").concat(timeObj.endTime, "</p>\n            <p>Notes: ").concat(list.notes, "</p>\n            <p>Created: ").concat(createdFormat(list.created), "</p>");
        var buttons = document.querySelectorAll('.btn-flat');
        addButtonLogic(buttons, this, list.id, list.points, list.name);
        modal.open();
      };
    }
  } else {
    // We reached our target server, but it returned an error
    console.log('Error');
  }
};

request.onerror = function () {
  // There was a connection error of some sort
  console.log("There was an error of sometype, please try again");
};

request.send(); // Format the time into more user friendly format

function timeAndDateFormat(startTime, endTime, date) {
  var year = date.substring(0, 4);
  var month = date.substring(5, 7);
  var day = date.substring(8, 10);
  var startHour = startTime.substring(11, 13);
  var startMinute = startTime.substring(14, 16);
  var endHour = endTime.substring(11, 13);
  var endMinute = endTime.substring(14, 16);
  var fulldate = "".concat(month, "-").concat(day, "-").concat(year);
  var fulltime = startHour + startMinute;
  var fullEndTime = endHour + endMinute;
  t = {};
  t.date = fulldate;
  t.startTime = fulltime;
  t.endTime = fullEndTime;
  return t;
} // Format the created field into more user friendly format


function createdFormat(createdDate) {
  var year = createdDate.substring(0, 4);
  var month = createdDate.substring(5, 7);
  var day = createdDate.substring(8, 10);
  var startHour = createdDate.substring(11, 13);
  var startMinute = createdDate.substring(14, 16);
  var fullDate = "".concat(month, "/").concat(day, "/").concat(year, " @ ").concat(startHour).concat(startMinute);
  return fullDate;
}

function addButtonLogic(buttons, dpm, id, points, name) {
  points = points[0] == '+' ? points.substring(1) : points;

  buttons[0].onclick = function () {
    dpm.style.display = 'none';
    approve(id, points, name);
  };

  buttons[1].onclick = function () {
    dpm.style.display = 'none';
    deny(id);
  };
} // Approve sends AJAX request in order to approve a DPM


function approve(id, points, name) {
  // Set post request URL and set headers
  var request = new XMLHttpRequest();
  request.open('POST', "/dpm/approve/".concat(id), true); // Set JSON and CSRF token headers

  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
  request.setRequestHeader('X-CSRF-Token', csrf); // Create object to hold name and points values for the DPM

  var temp = {};
  temp.points = points;
  temp.name = name; // Stringify the object in order to send it to the server

  var out = JSON.stringify(temp);
  request.send(out);
} // Deny sends AJAX request in order to deny a DPM


function deny(id) {
  // Set post request URL and set headers
  var request = new XMLHttpRequest();
  request.open('POST', "/dpm/deny/".concat(id), true); // Set JSON and CSRF token headers

  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
  request.setRequestHeader('X-CSRF-Token', csrf);
  request.send();
} // Navbar initialized


document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('.sidenav');
  var instances = M.Sidenav.init(elems);
}); // Initialize modal

document.addEventListener('DOMContentLoaded', function () {
  var modalElement = document.querySelectorAll('.modal');
  var instances = M.Modal.init(modalElement);
  modal = instances[0];
});