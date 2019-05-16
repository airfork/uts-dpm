// initialize elements
M.AutoInit();
let modal = null;
document.addEventListener('DOMContentLoaded', function () {
    var modalElement = document.querySelectorAll('.modal');
    var instances = M.Modal.init(modalElement);
    modal = instances[0];
});

const inputs = document.querySelectorAll('input');
const csrf = inputs[inputs.length - 1].value;
const id = document.getElementById('uid').value;
const rows = document.querySelectorAll('.dpm-rows');
let dataList = [];
let objectList = [];

// Send AJAX call to server, to get DPM data
let request = new XMLHttpRequest();
request.open('GET', `/users/${id}/dpms/full`, true);
request.onload = function () {
    if (request.status >= 200 && request.status < 400) {
        var modaltext = document.querySelector('.modal-content');
        // Success!
        const data = JSON.parse(request.responseText);
        // Get items from data into dataList
        for (var i = 0; i < data.length; i++) {
            dataList[i] = data[i];
        }
        // Add event listener to each dpm that pulls up more information
        for (var i = 0; i < rows.length; i++) {
            // Push item to object list so I know which dpm is being clicked
            objectList.push(rows[i]);
            rows[i].onclick = function () {
                // Each object is mapped by index to dpm data
                // Do find dpm data based on index of clicked dpm
                const list = dataList[objectList.indexOf(this)];
                const timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
                const status = getStatus(list.approved, list.ignored);
                modaltext.innerHTML = `
                <h4>${list.firstName} ${list.lastName}</h4>
                <p>Points: ${list.points}</p>
                <p>${list.dpmtype}</p>
                <p>Block: ${list.block}</p>
                <p>Location: ${list.location}</p>
                <p>Date: ${timeObj.date}</p>
                <p>Time: ${timeObj.startTime}-${timeObj.endTime}</p>
                <p>Notes: ${list.notes}</p>
                <p>Status: ${status}</p>
                `;
                removeBtnLogic(this, document.getElementById('delete'), list.id);
                modal.open();
            }
        }
    } else {
        // We reached our target server, but it returned an error
        console.log('Error');
    }
};

request.send();

// Format the time into more user friendly format
function timeAndDateFormat(startTime, endTime, date) {
    var year = date.substring(0, 4);
    var month = date.substring(5, 7);
    var day = date.substring(8, 10);
    var startHour = startTime.substring(11, 13);
    var startMinute = startTime.substring(14, 16);
    var endHour = endTime.substring(11, 13);
    var endMinute = endTime.substring(14, 16);
    var fulldate = `${month}-${day}-${year}`;
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
    }
}

// This deletes the DPM
function remove(id, row) {
    // Set post request URL and set headers
    let request = new XMLHttpRequest();
    request.open('DELETE', `/dpm/${id}`, true);
    // Set JSON and CSRF token headers
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf);
    request.onload = function () {
        if (request.status >= 200 && request.status < 400) {
            row.style.display = 'none';
            M.toast({html: 'DPM Removed'});
            modal.close();
        } else {
            M.toast({html: 'There was an error removing this DPM.'});
        }
    };
    request.onerror = function () {
        M.toast({html: 'There was an error removing this DPM.'});
    };
    request.send();
}

function getStatus(approved, ignored) {
    if (approved === true && ignored === false) {
        return 'DPM approved'
    }
    if (approved === true && ignored === true) {
        return 'DPM was approved but driver can not longer view it'
    }
    if (approved === false && ignored === false) {
        return 'DPM has not been looked at'
    }
    if (approved === false && ignored === true) {
        return 'DPM was denied'
    }
}