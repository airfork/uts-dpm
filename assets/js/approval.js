// Get the last input box on the page, which holds a csrf token, and store it
let inputs = document.querySelectorAll('input');
const csrf = inputs[inputs.length - 1].value;
let backdrop = document.querySelector('.backdrop');
let modal =  document.querySelector('.modal');
let dataList = [];
let objectList = [];

// Send AJAX call to server, to get DPM data
let request = new XMLHttpRequest();
request.open('GET', '/dpm/approve', true);
request.onload = () => {
  if (request.status >= 200 && request.status < 400) {
    // Success!
    let data = JSON.parse(request.responseText);
    // Get items from data into dataList
    for (i = 0; i < data.length; i++) {
        dataList[i] = data[i];
    }
    // Add event listener to each dpm that pulls up more information
    document.querySelectorAll('.dpm').forEach((item) => {
        // Push item to object list so I know which dpm is being clicked
        objectList.push(item);
        item.onclick = function() {
            // Each object is mapped by index to dpm data
            // Do find dpm data based on index of clicked dpm
            var list = dataList[objectList.indexOf(this)];
            timeObj = timeAndDateFormat(list.startTime, list.endTime, list.date);
            modal.innerHTML = 
             `<p>Driver: ${list.name}</p>
            <p>Supervisor: ${list.supName}
            <p>Points: ${list.points}</p>
            <p>${list.dpmtype}</p>
            <p>Block: ${list.block}</p>
            <p>Location: ${list.location}</p>
            <p>Date: ${timeObj.date}</p>
            <p>Time: ${timeObj.startTime}-${timeObj.endTime}</p>
            <p>Notes: ${list.notes}</p>
            <p>Created: ${createdFormat(list.created)}</p>
            <button>Approve</button>
            <button>Deny</button>`;
            // Select two buttons and give them event listeners
            let buttons = document.querySelectorAll('button');
            addButtonLogic(buttons, this, list.id, list.points, list.name);
            // Make backdrop, modal, and buttons visible
            backdrop.classList.add('popup');
            modal.classList.add('popup');
        }
    });
  } else {
    // We reached our target server, but it returned an error
    console.log('Error');
  }
};

request.onerror = () => {
    // There was a connection error of some sort
    console.log("There was an error of sometype, please try again")
  };
  
request.send();

// On click of backdrop. remove classes making backdrop and modal visible
// clear content of modal
backdrop.onclick = () => {
    backdrop.classList.remove('popup');
    modal.classList.remove('popup');
    modal.innerHTML = '';
}

// Format the time into more user friendly format
function timeAndDateFormat(startTime, endTime, date) {
    year = date.substring(0, 4);
    month = date.substring(5, 7);
    day = date.substring(8, 10);
    startHour = startTime.substring(11, 13);
    startMinute = startTime.substring(14, 16);
    endHour = endTime.substring(11, 13);
    endMinute = endTime.substring(14, 16);
    fulldate = `${month}-${day}-${year}`
    fulltime = startHour + startMinute;
    fullEndTime = endHour + endMinute;
    t = {};
    t.date = fulldate;
    t.startTime = fulltime;
    t.endTime = fullEndTime;
    return t;
}

// Format the created field into more user friendly format
function createdFormat(createdDate) {
    year = createdDate.substring(0, 4);
    month = createdDate.substring(5, 7);
    day = createdDate.substring(8, 10);
    startHour = createdDate.substring(11, 13);
    startMinute = createdDate.substring(14, 16);
    fullDate = `${month}/${day}/${year} @ ${startHour}${startMinute}`;
    return fullDate;
}

function addButtonLogic(buttons, dpm, id, points, name) {
    points = (points[0] == '+' ? points.substring(1) : points);
    buttons[0].onclick = () => {
        dpm.style.display = 'none';
        approve(id, points, name);
        clearModal();
    }
    buttons[1].onclick = () => {
        dpm.style.display = 'none';
        deny(id);
        clearModal();
    }
}

// Clears modal of all of its contents
function clearModal() {
    backdrop.classList.remove('popup');
    modal.classList.remove('popup');
    modal.innerHTML = '';
}

// Approve sends AJAX request in order to approve a DPM
function approve(id, points, name) {
    // Set post request URL and set headers
    let request = new XMLHttpRequest();
    request.open('POST', `/dpm/approve/${id}`, true);
    // Set JSON and CSRF token headers
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf);
    // Create object to hold name and points values for the DPM
    temp = {};
    temp.points = points;
    temp.name = name;
    // Stringify the object in order to send it to the server
    out = JSON.stringify(temp);
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
    request.send();
}