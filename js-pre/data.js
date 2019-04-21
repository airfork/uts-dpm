'use strict';
// Initialize elements
M.AutoInit();

// dealing with date picker
function datePickerInit() {
    var currentDate = new Date();
    var monthEarlier = new Date();
    var elems = document.querySelector('#endDate');
    M.Datepicker.init(elems, {
        format: 'yyyy-mm-dd',
        defaultDate: currentDate,
        setDefaultDate: true,
    });
    var month = currentDate.getMonth() - 1;
    month = month < 0 ? 12 : month;
    monthEarlier.setMonth(month);
    elems = document.querySelector('#startDate');
    M.Datepicker.init(elems, {
        format: 'yyyy-mm-dd',
        defaultDate: monthEarlier,
        setDefaultDate: true,
    });
}
document.addEventListener('DOMContentLoaded', datePickerInit);