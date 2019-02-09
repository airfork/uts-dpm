"use strict";

document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('.sidenav');
  var instances = M.Sidenav.init(elems);
});
document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('select');
  var instances = M.FormSelect.init(elems);
}); // dealing with floating button

document.addEventListener('DOMContentLoaded', function () {
  var elems = document.querySelectorAll('.fixed-action-btn');
  var instances = M.FloatingActionButton.init(elems);
});
var modal = null;
document.addEventListener('DOMContentLoaded', function () {
  var modalElement = document.querySelectorAll('.modal');
  var instances = M.Modal.init(modalElement);
  modal = instances[0];
});
var dataList = [];
var objectList = []; // Send AJAX call to server, to get DPM data

var request = new XMLHttpRequest();
request.open('GET', '/dpm/all', true);

request.onload = function () {
  if (request.status >= 200 && request.status < 400) {
    var modaltext = document.querySelector('.modal-content'); // Success!

    var data = JSON.parse(request.responseText); // Get items from data into dataList

    for (i = 0; i < data.length; i++) {
      dataList[i] = data[i];
    } // Add event listener to each dpm that pulls up more information


    document.querySelectorAll('.dpm').forEach(function (item) {
      // Push item to object list so I know which dpm is being clicked
      objectList.push(item);

      item.onclick = function () {
        // Each object is mapped by index to dpm data
        // Do find dpm data based on index of clicked dpm
        var list = dataList[objectList.indexOf(this)];
        timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
        modaltext.innerHTML = "\n            <h4>".concat(list.firstName, " ").concat(list.lastName, "</h4>\n            <p>Points: ").concat(list.points, "</p>\n            <p>").concat(list.dpmtype, "</p>\n            <p>Block: ").concat(list.block, "</p>\n            <p>Location: ").concat(list.location, "</p>\n            <p>Date: ").concat(timeObj.date, "</p>\n            <p>Time: ").concat(timeObj.startTime, "-").concat(timeObj.endTime, "</p>\n            <p>Notes: ").concat(list.notes, "</p>\n            ");
        modal.open();
      };
    });
  } else {
    // We reached our target server, but it returned an error
    console.log('Error');
  }
};

request.send(); // Format the time into more user friendly format

function timeAndDateFormat(startTime, endTime, date) {
  year = date.substring(0, 4);
  month = date.substring(5, 7);
  day = date.substring(8, 10);
  startHour = startTime.substring(11, 13);
  startMinute = startTime.substring(14, 16);
  endHour = endTime.substring(11, 13);
  endMinute = endTime.substring(14, 16);
  fulldate = "".concat(month, "-").concat(day, "-").concat(year);
  fulltime = startHour + startMinute;
  fullEndTime = endHour + endMinute;
  t = {};
  t.date = fulldate;
  t.startTime = fulltime;
  t.endTime = fullEndTime;
  return t;
}