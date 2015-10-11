var USERID_MIN_LENGTH = 3;
var USERID_MAX_LENGTH = 32;
var PASSWORD_MIN_LENGTH = 5;
var PASSWORD_MAX_LENGTH = 32;

function selectDocRoot() {
    var docRootWin = open('/webfilesys/servlet?command=admin&cmd=selectDocRoot','docRootWin','status=no,toolbar=no,menu=no,width=550,height=500,resizable=yes,scrollbars=yes,left=100,top=50,screenX=100,screenY=50');
    docRootWin.focus();
}

function switchAllDrivesAccess(checkbox, initial) {
    if (checkbox.checked) {
        document.getElementById('documentRoot').value = '';
        document.getElementById('documentRoot').disabled = true;
        document.getElementById('docRootButton').disabled = true;
    } else {
        document.getElementById('documentRoot').disabled = false;
        document.getElementById('docRootButton').disabled = false;
        if (!initial) {
            document.getElementById('documentRoot').focus();
        }
    }
}

function switchDiskQuota(checkbox) {
    if (checkbox.checked) {
        document.getElementById('diskQuota').disabled = false;
        document.getElementById('diskQuota').focus();
    } else {
        document.getElementById('diskQuota').value = "";
        document.getElementById('diskQuota').disabled = true;
    }
}

function validateUser(isEdit) {

    clearValidationErrors();

    var validationError = false;
    
    if (!isEdit) {

        var userid = trim(document.getElementById('username').value);
    
        if (userid.length < USERID_MIN_LENGTH) {
            addValidationError("username", "the minimum length of the userid is " + USERID_MIN_LENGTH + " characters");
            validationError = true;
        }
    
        if (userid.length > USERID_MAX_LENGTH) {
            addValidationError("username", "the maximum length of the userid is " + USERID_MAX_LENGTH + " characters");
            validationError = true;
        }

        if (userid.indexOf(' ') >= 0) {
            addValidationError("username", "the userid must not contain spaces");
            validationError = true;
        }
    }

    var password = trim(document.getElementById('password').value);
    var pwconfirm = trim(document.getElementById('pwconfirm').value);
    
    if (!isEdit) {
        if (password.length < PASSWORD_MIN_LENGTH) {
            addValidationError("password", "the minimum length of the password is " + PASSWORD_MIN_LENGTH + " characters");
            validationError = true;
        }
    }
    
    if (password.length > PASSWORD_MAX_LENGTH) {
        addValidationError("password", "the maximum length of the password is " + PASSWORD_MAX_LENGTH + " characters");
        validationError = true;
    }
    
    if (password.indexOf(' ') >= 0) {
        addValidationError("password", "the password must not contain spaces");
        validationError = true;
    }
    
    if (password != pwconfirm) {
        addValidationError("pwconfirm", "password and password confirmation must be equal");
        validationError = true;
    }

    var ropassword = trim(document.getElementById('ropassword').value);
    var ropwconfirm = trim(document.getElementById('ropwconfirm').value);

    if (ropassword.length > 0) {
        if (ropassword.length < PASSWORD_MIN_LENGTH) {
            addValidationError("ropassword", "the minimum length of the password is " + PASSWORD_MIN_LENGTH + " characters");
            validationError = true;
        }
        if (ropassword.length > PASSWORD_MAX_LENGTH) {
            addValidationError("ropassword", "the maximum length of the password is " + PASSWORD_MAX_LENGTH + " characters");
            validationError = true;
        }
        if (ropassword.indexOf(' ') >= 0) {
            addValidationError("ropassword", "the password must not contain spaces");
            validationError = true;
        }
    }

    if ((ropassword.length > 0) || (ropwconfirm.length > 0)) {
        if (ropassword != ropwconfirm) {
            addValidationError("ropwconfirm", "read-only password and confirmation must be equal");
            validationError = true;
        }
    }

    allDrives = document.getElementById('allDrives')

    var docRoot = trim(document.getElementById('documentRoot').value);
    
    if ((docRoot.length == 0) && ((!allDrives) || (!allDrives.checked))) {
        if (allDrives) {
            addValidationError("documentRoot", "document root must be selected or select checkbox for full access to all drives");
        } else {
            addValidationError("documentRoot", "document root must be selected");
        }
        validationError = true;
    }

    var email = trim(document.getElementById('email').value);
    
    if (email.length == 0) {
        addValidationError("email", "e-mail address is a required field");
        validationError = true;
    } else if (!validateEmail(email)) {
        addValidationError("email", "the e-mail address does not conform to the required format");
        validationError = true;
    }

    var diskQuotaCheckbox = document.getElementById("checkDiskQuota");
    
    var diskQuotaValue = trim(document.getElementById('diskQuota').value);
    
    if (diskQuotaCheckbox.checked) {
        if (diskQuotaValue.length == 0) {
            addValidationError("diskQuota", "please enter a disk quota value");
            validationError = true;
        } else {
            if (isNaN(diskQuotaValue)) {
                addValidationError("diskQuota", "please enter a number for the disk quota value");
                validationError = true;
            } else {
                var diskQuotaNum = Number(diskQuotaValue);
                if ((diskQuotaNum < 1) || (diskQuotaNum > 10000000)) {
                    addValidationError("diskQuota", "the disk quota value must be in the range between 1 and 10000000 (MByte)");
                    validationError = true;
                }
            }
        }
    }

    if (!isEdit) {
        var languageSelect = document.getElementById('language');

        if (languageSelect.selectedIndex == 0) {
            addValidationError("language", "please select a language");
            validationError = true;
        }
    }
	
    if (!validationError) {
        document.getElementById('userForm').submit();
    }
    
    return (!validationError);
}

function clearValidationErrors() {
    removeAllChildNodes("validationErrorList");
    removeCSSRecursive(document.getElementById("userForm"), "validationError");
}

function addValidationError(inputId, errorMsg) {
    var validationErrorList = document.getElementById("validationErrorList");

	var listElem = document.createElement("li");
	listElem.setAttribute("class", "validationError");
    var msgTextNode = document.createTextNode(errorMsg);
	listElem.appendChild(msgTextNode);	
   	validationErrorList.appendChild(listElem);
   	
   	if (inputId != null) {
   	    var invalidInput = document.getElementById(inputId);
   	    invalidInput.setAttribute("class", "validationError");
    }	
}