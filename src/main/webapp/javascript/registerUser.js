function initUserRegistration() {
    xmlGetRequest("checkOpenRegistration", { }, responseXml => {
        const resultElem = responseXml.getElementsByTagName("result")[0];
        const openRegistration = resultElem.firstChild.nodeValue;
        if ("true" === openRegistration) {
            loadLanguages();
            loadSkins();
        } else {
            customAlert("open registration not allowed", null,
                () => window.location.href = "/webfilesys/servlet");
        }
    });
}

function loadLanguages() {
    xmlGetRequest("languages", {}, responseXml => {
        const languagesElem = responseXml.getElementsByTagName("languages")[0];
        const languages = languagesElem.getElementsByTagName("language");
        const languageSelection = document.getElementById("languageSelection");
        languageSelection.options[0] = new Option(resourceBundle["label.selectLanguage"], "");
        for (let i = 0; i < languages.length; i++) {
            let language = languages[i].firstChild.nodeValue;
            languageSelection.options[i + 1] = new Option(language, language);
        }
    });
}

function loadSkins() {
    xmlGetRequest("skins", {}, responseXml => {
        const skinsElem = responseXml.getElementsByTagName("skins")[0];
        const skins = skinsElem.getElementsByTagName("skin");
        const skinSelection = document.getElementById("skinSelection");
        for (let i = 0; i < skins.length; i++) {
            let skin = skins[i].firstChild.nodeValue;
            skinSelection.options[i] = new Option(skin, skin);
            if (skin === "dark") {
                skinSelection.options[i].selected = true;
            }
        }
    });
}

function validateAndSubmitRegistration() {
    if (!validateUserName()) {
        return;
    }
    if (!validatePasswords("password", "pwconfirm")) {
        return;
    }
    if (document.getElementById("ropassword").value.length > 0) {
        if (!validatePasswords("ropassword", "ropwconfirm")) {
            return;
        }
    }
    if (!validateEmail()) {
        return;
    }
    if (!validateLanguage()) {
        return;
    }
    checkDuplicateUserAndSubmit();
}

function validateUserName() {
    const userName = document.getElementById("userName").value;
    if (userName.length < 3 || userName.length > 64) {
        customAlert(resourceBundle["error.loginLength"]);
        return false;
    }
    if (!/^[a-zA-Z0-9.\-_@]*$/.test(userName)) {
        customAlert(resourceBundle["error.loginInvalidChar"]);
        return false;
    }
    return true;
}

function validatePasswords(passwordDomId, pwconfirmDomId) {
    const password = document.getElementById(passwordDomId).value;
    if (password.length < 5 || password.length > 64) {
        customAlert(resourceBundle["error.passwordlength"]);
        return false;
    }
    if (password.indexOf(" ") >= 0) {
        customAlert(resourceBundle["error.spacesinpw"]);
        return false;
    }
    const pwconfirm = document.getElementById(pwconfirmDomId).value;
    if (password !== pwconfirm) {
        customAlert(resourceBundle["error.pwmissmatch"]);
        return false;
    }
    return true;
}

function validateLanguage() {
    const language = document.getElementById("languageSelection").value;
    if (language.length === 0) {
        customAlert(resourceBundle["error.missingLanguage"]);
        return false;
    }
    return true;
}

function validateEmail() {
    const email = document.getElementById("email").value;
    if (email.length < 3) {
        customAlert(resourceBundle["error.email"]);
        return false;
    }
    return true;
}

function checkDuplicateUserAndSubmit() {
    const userName = document.getElementById("userName").value;
    xmlGetRequest("existUser", { userName }, responseXml => {
        const resultElem = responseXml.getElementsByTagName("result")[0];
        const duplicateUser = resultElem.firstChild.nodeValue;
        console.log("duplicateUser:", duplicateUser);
        if ("true" === duplicateUser) {
            customAlert(resourceBundle["error.duplicatelogin"]);
        } else {
            xmlFetchPost(getFormData(document.getElementById("registrationForm")), responseXml => {
                const resultElem = responseXml.getElementsByTagName("result")[0];
                const success = resultElem.getElementsByTagName("success")[0].firstChild.nodeValue;
                if ("true" === success) {
                    const activationByAdminElem = resultElem.getElementsByTagName("activationByAdminRequired");
                    const activationByAdminRequired = activationByAdminElem && activationByAdminElem.length > 0;
                    const infoTextKey = activationByAdminRequired ? "registrationAdminActivation" : "registrationConfirmation";
                    customAlert(resourceBundle[infoTextKey], resourceBundle["button.registrationToLogin"],
                        () => window.location.href = "/webfilesys/servlet");
                }
            });
        }
    });
}
