"use strict";

// initialize elements
M.AutoInit(); // Confirm that user wants to autosubmit, if yes, submit form

document.getElementById('auto-submit').onclick = function submitAutogen() {
  if (confirm('Are you sure you want to submit these DPMs? Please double check W2W for accuracy before submitting.')) {
    document.getElementById('auto-form').submit();
  }
};