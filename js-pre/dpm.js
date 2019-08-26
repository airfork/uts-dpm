// Holds names of all the users for autocomplete box
let people = [];
// Relates users to their ids
let peopleIds = [];
// Holds the userID of the person loading the page
let userID = "";
//Datepicker element
const datepicker = null;

// Send AJAX call to server, to fill up arrays
const request = new XMLHttpRequest();
request.open('GET', '/users', true);
request.onload = function() {
  if (request.status >= 200 && request.status < 400) {
    // Success!
    const data = JSON.parse(request.responseText);
    // Get ids, people, and the username
    peopleIds = data.ids;
    people = data.names;
    userID = data.userID;
    const dataobj = {};
    for (let i = 0; i < people.length; i++) {
        people[i] = he.decode(people[i]);
        dataobj[people[i]] = null;
    }
    // people.forEach(function(name) {
    //     name = unescape(name);
    //     dataobj[name] = null;
    // });
    const elems = document.querySelectorAll('.autocomplete');
    const instances = M.Autocomplete.init(elems, {
        data: dataobj,
        limit: 5,
    });

  } else {
      // We reached our target server, but it returned an error
      M.toast({html: 'There was an error loading the resources for this page, please try again'});
      console.log('Error');
  }
};

request.onerror = function() {
  // There was a connection error of some sort
    M.toast({html: 'There was an error, please try again'});
  console.log("There was an error of some type, please try again")
};

request.send();

// Get the last input box on the page, which holds a csrf token, and store it
const inputs = document.querySelectorAll('input');
const csrf = inputs[inputs.length - 1].value;

// Parses the inputs and gets information from them
// Creates and sends JSON object for the server to handle
function submitLogic() {
// Takes all of these fields and puts them in an object
    const obj = {};
    obj.name = document.getElementById('autocomplete-input').value;
    if (people.indexOf(obj.name) === -1) {
        return "name";
    }
    obj.block = document.getElementById('block').value.toUpperCase();
    obj.location = document.getElementById('location').value.toUpperCase();
    obj.date = document.getElementById('date').value;
    if (obj.date === "") {
        return "date";
    }
    obj.startTime = document.getElementById('starttime').value;
    obj.endTime = document.getElementById('endtime').value;
    if (obj.startTime === "" || obj.endTime === "") {
        return "time";
    }
    obj.notes = document.getElementById('notes').value;
    obj.dpmType = document.getElementById('type').value;
    obj.sender = userID;
    const id = peopleIds[people.indexOf(obj.name)];
    obj.id = id.toString();
    obj.points = '0';
    // Clear out text inputs
    const inputList = document.querySelectorAll('.input');
    for (var i = 0; i < inputList.length; i++) {
        inputList[i].value = "";
    }

    // Remove active class from labels
    const labelList = document.querySelectorAll('label');
    for (var i = 0; i < labelList.length; i++) {
        labelList[i].classList.remove('active');
    }
    // Reset date picker
    datePickerInit();
    // Create JSON, then POST to server
    const jObj = JSON.stringify(obj);
    sendDPM(jObj);
    return true;
}
// Actually sends the JSON
function sendDPM(jOBJ) {
    const request = new XMLHttpRequest();
    request.open('POST', '/dpm', true);
    // Set JSON header as well as the CSRF token header, both very important
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.setRequestHeader('X-CSRF-Token', csrf);
    request.send(jOBJ);
}

// Dealing with timepicker forms
document.addEventListener('DOMContentLoaded', function() {
    const elems = document.querySelectorAll('.timepicker');
    const instances = M.Timepicker.init(elems, {
        twelveHour: false,
        showClearBtn: true,
        autoClose: true,
    });
});

// dealing with date picker
function datePickerInit() {
    var elems = document.querySelectorAll('.datepicker');
    M.Datepicker.init(elems, {
        format: 'yyyy-mm-dd',
        defaultDate: new Date(),
        setDefaultDate: true,
    });
}
document.addEventListener('DOMContentLoaded', datePickerInit);

// dealing with selects
document.addEventListener('DOMContentLoaded', function() {
    const elems = document.querySelectorAll('select');
    const instances = M.FormSelect.init(elems);
});

// dealing with floating button
document.addEventListener('DOMContentLoaded', function() {
    const elems = document.querySelectorAll('.fixed-action-btn');
    const instances = M.FloatingActionButton.init(elems);
    elems[0].onclick = function() {
        var submitted = submitLogic();
        if (submitted === "name") {
            M.toast({html: 'Please input a valid name.'});
        } 
        else if (submitted === "date") {
            M.toast({html: 'Please provide a date.'});
        } else if (submitted === "time") {
            M.toast({html: 'Please input a start and end time.'});
        } else {
            M.toast({html: 'DPM Submitted!'});
        }
    }
});

document.addEventListener('DOMContentLoaded', function() {
    const elems = document.querySelectorAll('.sidenav');
    const instances = M.Sidenav.init(elems);
});
