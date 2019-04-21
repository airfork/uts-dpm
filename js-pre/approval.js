// Get the last input box on the page, which holds a csrf token, and store it
var inputs = document.querySelectorAll('input');
const csrf = inputs[inputs.length - 1].value;
var dataList = [];
var objectList = [];
var modal = null;

// Send AJAX call to server, to get DPM data
var request = new XMLHttpRequest();
request.open('GET', '/dpm/approve', true);
request.onload = function() {
  if (request.status >= 200 && request.status < 400) {
    // Success!
    var data = JSON.parse(request.responseText);
    // Get items from data into dataList
    for (var i = 0; i < data.length; i++) {
        dataList[i] = data[i];
    }
    // Add event listener to each dpm that pulls up more information
    const dpmList = document.querySelectorAll('.dpm');
    for (var i = 0; i < dpmList.length; i++) {
        // Push item to object list so I know which dpm is being clicked
        const index = i;
        objectList.push(dpmList[i]);
        dpmList[i].onclick = function() {
            // Each object is mapped by index to dpm data
            // Do find dpm data based on index of clicked dpm
            const list = dataList[objectList.indexOf(this)];
            setModalContent(list);
            let points = list.points[0] === '+' ? list.points.substr(1) : list.points;
            document.getElementById('edit-points').value = points;
            document.getElementById('edit-label').classList.add('active');
            const buttons = document.querySelectorAll('.btn-flat');
            buttons[0].classList.remove('disabled');
            buttons[1].classList.remove('disabled');
            buttons[2].classList.remove('disabled');
            addButtonLogic(buttons, this, list.id, list.points, list.name);

            document.querySelector('.modal-content').onclick = function () {
                const newPoints = document.getElementById('edit-points').value;
                if (isNaN(newPoints)) {
                    document.getElementById('edit-points').value = points;
                    return;
                }
                if (points !== newPoints) {
                    updatePointValue(list, newPoints, index);
                }
                document.getElementById('edit-field').style.display = 'none';
                const buttons = document.querySelectorAll('.btn-flat');
                buttons[0].style.display = 'inline-block';
                buttons[1].style.display = 'inline-block';
                buttons[2].style.display = 'inline-block';
            };
            modal.open();
        }
    }
  } else {
    // We reached our target server, but it returned an error
    console.log('Error');
  }
};

request.onerror = function() {
    // There was a connection error of some sort
    console.log("There was an error of some type, please try again")
};
  
request.send();

// Format the time into more user friendly format
function timeAndDateFormat(startTime, endTime, date) {
    const year = date.substring(0, 4);
    const month = date.substring(5, 7);
    const day = date.substring(8, 10);
    const startHour = startTime.substring(11, 13);
    const startMinute = startTime.substring(14, 16);
    const endHour = endTime.substring(11, 13);
    const endMinute = endTime.substring(14, 16);
    const fulldate = `${month}-${day}-${year}`;
    const fulltime = startHour + startMinute;
    const fullEndTime = endHour + endMinute;
    let t = {};
    t.date = fulldate;
    t.startTime = fulltime;
    t.endTime = fullEndTime;
    return t;
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

function setModalContent(list) {
    const modalText =  document.querySelector('.modal-content');
    const timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
    modalText.innerHTML =
        `<p>Driver: ${list.name}</p>
            <p>Supervisor: ${list.supName}
            <p>Points: ${list.points}</p>
            <p>${list.dpmtype}</p>
            <p>Block: ${list.block}</p>
            <p>Location: ${list.location}</p>
            <p>Date: ${timeObj.date}</p>
            <p>Time: ${timeObj.startTime}-${timeObj.endTime}</p>
            <p>Notes: ${list.notes}</p>
            <p>Created: ${createdFormat(list.created)}</p>`;
}

function addButtonLogic(buttons, dpm, id, points, name) {
    points = (points[0] === '+' ? points.substring(1) : points);
    buttons[0].onclick = showEditField;
    buttons[1].onclick = function() {
        event.stopPropagation();
        dpm.style.display = 'none';
        approve(id, points, name);
        modal.close();
    };
    buttons[2].onclick = function() {
        event.stopPropagation();
        dpm.style.display = 'none';
        deny(id);
        modal.close();
    };
}

// Approve sends AJAX request in order to approve a DPM
function approve(id, points, name) {
    // Set post request URL and set headers
    let request = new XMLHttpRequest();
    request.open('POST', `/dpm/approve/${id}`, true);
    // Set JSON and CSRF token headers
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf);
    request.onload = function () {
        if (request.status >= 200 && request.status < 400) {
            M.toast({html: 'DPM Approved'});
        }
    };
    // Create object to hold name and points values for the DPM
    let temp = {};
    temp.points = points;
    temp.name = name;
    // Stringify the object in order to send it to the server
    const out = JSON.stringify(temp);

    request.onerror = function() {
        M.toast({html: 'There was an error approving this DPM.'});
    };
    request.send(out);
}

// Deny sends AJAX request in order to deny a DPM
function deny(id) {
    // Set post request URL and set headers
    let request = new XMLHttpRequest();
    request.open('POST', `/dpm/deny/${id}`, true);
    // Set JSON and CSRF token headers
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf);
    request.onload = function() {
        if (request.status >= 200 && request.status < 400) {
            M.toast({html: 'DPM Denied'});
        }
    };
    request.onerror = function() {
        M.toast({html: 'There was an error denying this DPM.'});
    };
    request.send();
}

function updatePointValue(list, points, index) {
    const buttons = document.getElementsByClassName('btn-flat');
    buttons[0].classList.add('disabled');
    buttons[1].classList.add('disabled');
    buttons[2].classList.add('disabled');
    const mainDPM = objectList[index].firstElementChild.firstElementChild;
    // Set post request URL and set headers
    let request = new XMLHttpRequest();
    request.open('PATCH', `/dpm/${list.id}`, true);
    // Set JSON and CSRF token headers
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf);
    request.onload = function() {
        if (request.status >= 200 && request.status < 400) {
            if (points !== 0) {
                list.points = points > 0 ? '+' + points : points;
            }
            setModalContent(list);
            buttons[0].classList.remove('disabled');
            buttons[1].classList.remove('disabled');
            buttons[2].classList.remove('disabled');

            buttons[1].onclick = function() {
                event.stopPropagation();
                objectList[index].style.display = 'none';
                approve(list.id, points, list.name);
                modal.close();
            };
            buttons[2].onclick = function() {
                event.stopPropagation();
                objectList[index].style.display = 'none';
                deny(list.id);
                modal.close();
            };

            mainDPM.innerHTML = `${list.name}<br>${list.points}`;
            M.toast({html: 'Point value updated.'});
        } else {
            M.toast({html: 'There was an error updating the point value of this DPM.'});
        }
    };
    // Create object to hold name and points values for the DPM
    let temp = {};
    temp.points = points;
    // Stringify the object in order to send it to the server
    const out = JSON.stringify(temp);

    request.onerror = function() {
        M.toast({html: 'There was an error updating the point value of this DPM.'});
    };
    request.send(out);
}

function showEditField() {
    event.stopPropagation();
    const buttons = document.querySelectorAll('.btn-flat');
    buttons[0].style.display = 'none';
    buttons[1].style.display = 'none';
    buttons[2].style.display = 'none';
    document.getElementById('edit-field').style.display = 'block';
}

// Navbar initialized
document.addEventListener('DOMContentLoaded', function() {
    var elems = document.querySelectorAll('.sidenav');
    M.Sidenav.init(elems);
});

// Initialize modal
document.addEventListener('DOMContentLoaded', function() {
    var modalElement = document.querySelectorAll('.modal');
    var instances = M.Modal.init(modalElement);
    modal = instances[0];
});