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
            rows[i].onclick = function() {
                prepareModal('', this);
                modal.open();
            };
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

function removeBtnLogic(row, button, id, denied) {
    if (denied === true) {
        button.classList.add('disabled');
    } else {
        button.classList.remove('disabled');
        button.onclick = function () {
            if (confirm('Are you sure you want to deny this DPM?')) {
                remove(id, row);
            }
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
            row.onclick = function() {
                prepareModal('DPM denied', row);
                modal.open();
            };
            M.toast({html: 'DPM Denied'});
            modal.close();
        } else {
            M.toast({html: 'There was an error removing this DPM.'});
        }
    };
    request.onerror = function () {
        M.toast({html: 'There was an error denying this DPM.'});
    };
    request.send();
}

// Get the status of the DPM based on the approved and ignored fields of the DPM
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
        return 'DPM denied'
    }
}

// Format the created field into more user friendly format
function createdFormat(createdDate) {
    const year = createdDate.substring(0, 4);
    const month = createdDate.substring(5, 7);
    const day = createdDate.substring(8, 10);
    const startHour = createdDate.substring(11, 13);
    const startMinute = createdDate.substring(14, 16);
    return `${month}/${day}/${year} @ ${startHour}${startMinute}`;
}

function prepareModal(optionalStatus, row) {
    // Each object is mapped by index to dpm data
    // Do find dpm data based on index of clicked dpm
    let modaltext = document.querySelector('.modal-content');
    const list = dataList[objectList.indexOf(row)];
    const timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
    const status = optionalStatus === '' ? getStatus(list.approved, list.ignored) : optionalStatus;
    modaltext.innerHTML = `
    <h4>${list.firstName} ${list.lastName}</h4>
    <p>Supervisor: ${list.supname}</p>
    <p>Points: ${list.points}</p>
    <p>${list.dpmtype}</p>
    <p>Block: ${list.block}</p>
    <p>Location: ${list.location}</p>
    <p>Date: ${timeObj.date}</p>
    <p>Time: ${timeObj.startTime}-${timeObj.endTime}</p>
    <p>Notes: ${list.notes}</p>
    <p>Created: ${createdFormat(list.created)}</p>
    <p>Status: ${status}</p>
    `;
    let denied = false;
    if ((list.approved === false && list.ignored === true) || status === 'DPM denied') {
        denied = true;
    }
    removeBtnLogic(row, document.getElementById('delete'), list.id, denied);
}