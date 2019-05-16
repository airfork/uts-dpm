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
    var modaltext = document.querySelector('.modal-content'); // Success!

    var data = JSON.parse(request.responseText); // Get items from data into dataList

    for (var i = 0; i < data.length; i++) {
      dataList[i] = data[i];
    } // Add event listener to each dpm that pulls up more information


    for (var i = 0; i < rows.length; i++) {
      // Push item to object list so I know which dpm is being clicked
      objectList.push(rows[i]);

      rows[i].onclick = function () {
        // Each object is mapped by index to dpm data
        // Do find dpm data based on index of clicked dpm
        var list = dataList[objectList.indexOf(this)];
        var timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
        var status = getStatus(list.approved, list.ignored);
        modaltext.innerHTML = "\n                <h4>".concat(list.firstName, " ").concat(list.lastName, "</h4>\n                <p>Points: ").concat(list.points, "</p>\n                <p>").concat(list.dpmtype, "</p>\n                <p>Block: ").concat(list.block, "</p>\n                <p>Location: ").concat(list.location, "</p>\n                <p>Date: ").concat(timeObj.date, "</p>\n                <p>Time: ").concat(timeObj.startTime, "-").concat(timeObj.endTime, "</p>\n                <p>Notes: ").concat(list.notes, "</p>\n                <p>Status: ").concat(status, "</p>\n                ");
        removeBtnLogic(this, document.getElementById('delete'), list.id);
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

function removeBtnLogic(row, button, id) {
  button.onclick = function () {
    if (confirm('Are you sure you want to remove this DPM?')) {
      remove(id, row);
    }
  };
} // This deletes the DPM


function remove(id, row) {
  // Set post request URL and set headers
  var request = new XMLHttpRequest();
  request.open('DELETE', "/dpm/".concat(id), true); // Set JSON and CSRF token headers

  request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
  request.setRequestHeader('X-CSRF-Token', csrf);

  request.onload = function () {
    if (request.status >= 200 && request.status < 400) {
      row.style.display = 'none';
      M.toast({
        html: 'DPM Removed'
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
      html: 'There was an error removing this DPM.'
    });
  };

  request.send();
}

function getStatus(approved, ignored) {
  if (approved === true && ignored === false) {
    return 'DPM approved';
  }

  if (approved === true && ignored === true) {
    return 'DPM denied';
  }

  if (approved === false && ignored === false) {
    return 'DPM has not been looked at';
  }

  if (approved === false && ignored === true) {
    return 'This should not happen. If you see this, go ahead and delete this DPM';
  }
}