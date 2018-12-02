// Selects all the DPM types and adds an event listener to them
document.querySelectorAll('.type').forEach((item) => {
    // On click, remove active class from whatever element has it
    // Then add the class to the clicked element
    item.onclick = () => {
        document.querySelector('.active').classList.remove('active');
        item.classList.toggle('active');
    }
});

// Adds an eventlistener to the submit button
document.querySelectorAll('.submit').forEach((button) => {
    button.onclick = () => {
        submitLogic();
    }
});

pointsMap = new Map();
pointsMap.set("A", -5);
pointsMap.set("B", -10);
pointsMap.set("C", -15);
pointsMap.set("D", -20);
pointsMap.set("G", 2);
pointsMap.set("L", -2);
// Sets time and date inputs
setTimeAndDate();

// Sets time and date inputs based on the current time
function setTimeAndDate() {
    // Get date to prefill inputs
    let d = new Date();
    // Turn each into a string
    let month = d.getMonth() + 1; // Add one because January is 0
    month = month.toString();
    let minute = d.getMinutes().toString();
    let hours = d.getHours().toString();
    let day = d.getDate().toString();
    // Make sure that the length of each is 2, otherwise browser will complain
    day = (day.length == 1 ? '0' + day : day);
    hours = (hours.length == 1 ? '0' + hours : hours);
    minute = (minute.length == 1 ? '0' + minute : minute);
    month = (month.length == 1 ? '0' + month : month);
    // Put time and date in variable then set input values
    const startTime = `${hours}:${minute}`;
    const endTime = `${hours}:${minute}`;
    const date = `${d.getFullYear()}-${month}-${day}`;
    document.getElementById('date_input').value = date;
    document.getElementById('startTime_input').value = startTime;
    document.getElementById('endTime_input').value = endTime;
}

function getPoints(type) {
    if (type.length < 6) {
        return null;
    }
    const letter = type[5];
    if (!pointsMap.has(letter)) {
        return null;
    }
    return pointsMap.get(letter);
} 

// Parses the inputs and gets information from them
// Creates and sends JSON object for the server to handle
function submitLogic() {
    // Takes all of these fields and puts them in an object
    let obj = {};
    obj.name = document.getElementById('name_input').value;
    if (people.indexOf(obj.name) == -1) {
        console.log(`${obj.name} is not a valid name`);
        return;
    }
    obj.block = document.getElementById('block_input').value.toUpperCase();
    obj.location = document.getElementById('location_input').value.toUpperCase();
    // if (obj.location == "") {
    //     console.log('Location cannot be blank');
    //     return;
    // }
    obj.date = document.getElementById('date_input').value;
    obj.startTime = document.getElementById('startTime_input').value;
    obj.endTime = document.getElementById('endTime_input').value;
    obj.notes = document.getElementById('notes_input').value;
    obj.dpmType = document.querySelector('.active').textContent;
    obj.sender = userID;
    id = peopleIds[people.indexOf(obj.name)];
    obj.id = id.toString();
    points = getPoints(obj.dpmType);
    qty = document.getElementById('qty');
    if (points == null) {
        console.log("Point value must be specified");
        return
    }
    typeG = document.getElementById('typeG').textContent;
    if (obj.dpmType == typeG) {
        points = (qty.value == "" ? 1 : qty.value);
    }
    obj.points = points.toString();
    // Remove highlight around selected type, highlight default type
    document.querySelector('.active').classList.remove('active');
    document.querySelector('#typeG').classList.add('active');
    // Clear out all inputs
    document.querySelectorAll('.input').forEach((input) => {
        input.value = "";
    });
    qty.value = "";
    // Fill in date and time inputs
    setTimeAndDate();
    // Create JSON, then POST to server
    const jObj = JSON.stringify(obj);
    sendDPM(jObj);
}

// Actually sends the JSON
function sendDPM(jOBJ) {
    let request = new XMLHttpRequest();
    request.open('POST', '/dpm', true);
    request.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    request.send(jOBJ);
}