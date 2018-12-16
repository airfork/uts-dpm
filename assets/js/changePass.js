// Function that checks to make sure that the passwords match
// Will not submit if they do not, and then colors the border of the textbox
function validate() {
    // Get password, password confirmation, and original password
    let pass1 = document.getElementById('confirmPass').value;
    let pass2 = document.getElementById('newPass').value;
    let og = document.getElementById('originalPass').value;
    let ok = true;
    // If new passwords do not match, complain
    if (pass1 != pass2) {
        document.getElementById('confirmPass').style.borderColor = '#E34234';
        document.getElementById('newPass').style.borderColor = '#E34234';
        document.querySelector('.error').innerHTML = 'Passwords do not match';
        ok = false;
    }
    // If password matches the original password, compain
    if (pass1 == og) {
        document.getElementById('originalPass').style.borderColor = '#E34234'
        document.querySelector('.error').innerHTML = 'New password must be different from the old password';
        ok = false;
    }
    return ok;
}