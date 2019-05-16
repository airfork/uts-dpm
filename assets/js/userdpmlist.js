"use strict";

// initialize elements
M.AutoInit();
var modal = null;
document.addEventListener('DOMContentLoaded', function () {
  var modalElement = document.querySelectorAll('.modal');
  var instances = M.Modal.init(modalElement);
  modal = instances[0];
});
var inputs = document.querySelectorAll('input');
var csrf = inputs[inputs.length - 1].value;
var id = document.getElementById('uid').value;
var rows = document.querySelectorAll('.dpm-rows');
var dataList = [];
var objectList = []; // Send AJAX call to server, to get DPM data

var request = new XMLHttpRequest();
request.open('GET', "/users/".concat(id, "/dpms/full"), true);

request.onload = function () {
  if (request.status >= 200 && request.status < 400) {
    // Success!
    var data = JSON.parse(request.responseText); // Get items from data into dataList

    for (var i = 0; i < data.length; i++) {
      dataList[i] = data[i];
    } // Add event listener to each dpm that pulls up more information


    for (var i = 0; i < rows.length; i++) {
      // Push item to object list so I know which dpm is being clicked
      objectList.push(rows[i]);

      rows[i].onclick = function () {
        prepareModal('', this);
        modal.open();
      };
    }
  } else {
    // We reached our target server, but it returned an error
    console.log('Error');
  }
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
  var t = {};
  t.date = fulldate;
  t.startTime = fulltime;
  t.endTime = fullEndTime;
  return t;
}

function removeBtnLogic(row, button, id, denied) {
  if (denied === true) {
    button.classList.add('disabled');
  } else {
    button.classList.remove('disabled');

    button.onclick = function () {
      if (confirm('Are you sure you want to deny this DPM?')) {
        remove(id, row);
      }
    };
  }
} // This deletes the DPM


function remove(id, row) {
  // Set post request URL and set headers
  var request = new XMLHttpRequest();
  request.open('DELETE', "/dpm/".concat(id), true); // Set JSON and CSRF token headers

  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
  request.setRequestHeader('X-CSRF-Token', csrf);

  request.onload = function () {
    if (request.status >= 200 && request.status < 400) {
      row.onclick = function () {
        prepareModal('DPM denied', row);
        modal.open();
      };

      M.toast({
        html: 'DPM Denied'
      });
      modal.close();
    } else {
      M.toast({
        html: 'There was an error removing this DPM.'
      });
    }
  };

  request.onerror = function () {
    M.toast({
      html: 'There was an error denying this DPM.'
    });
  };

  request.send();
} // Get the status of the DPM based on the approved and ignored fields of the DPM


function getStatus(approved, ignored) {
  if (approved === true && ignored === false) {
    return 'DPM approved';
  }

  if (approved === true && ignored === true) {
    return 'DPM was approved but driver can not longer view it';
  }

  if (approved === false && ignored === false) {
    return 'DPM has not been looked at';
  }

  if (approved === false && ignored === true) {
    return 'DPM denied';
  }
} // Format the created field into more user friendly format


function createdFormat(createdDate) {
  var year = createdDate.substring(0, 4);
  var month = createdDate.substring(5, 7);
  var day = createdDate.substring(8, 10);
  var startHour = createdDate.substring(11, 13);
  var startMinute = createdDate.substring(14, 16);
  return "".concat(month, "/").concat(day, "/").concat(year, " @ ").concat(startHour).concat(startMinute);
}

function prepareModal(optionalStatus, row) {
  // Each object is mapped by index to dpm data
  // Do find dpm data based on index of clicked dpm
  var modaltext = document.querySelector('.modal-content');
  var list = dataList[objectList.indexOf(row)];
  var timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
  var status = optionalStatus === '' ? getStatus(list.approved, list.ignored) : optionalStatus;
  modaltext.innerHTML = "\n    <h4>".concat(list.firstName, " ").concat(list.lastName, "</h4>\n    <p>Supervisor: ").concat(list.supname, "</p>\n    <p>Points: ").concat(list.points, "</p>\n    <p>").concat(list.dpmtype, "</p>\n    <p>Block: ").concat(list.block, "</p>\n    <p>Location: ").concat(list.location, "</p>\n    <p>Date: ").concat(timeObj.date, "</p>\n    <p>Time: ").concat(timeObj.startTime, "-").concat(timeObj.endTime, "</p>\n    <p>Notes: ").concat(list.notes, "</p>\n    <p>Created: ").concat(createdFormat(list.created), "</p>\n    <p>Status: ").concat(status, "</p>\n    ");
  var denied = false;

  if (list.approved === false && list.ignored === true || status === 'DPM denied') {
    denied = true;
  }

  removeBtnLogic(row, document.getElementById('delete'), list.id, denied);
}