/******************************************************************************
 * $URL$
 * $Id$
 ******************************************************************************
 *
 * Copyright (c) 2003-2014 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *****************************************************************************/
// USER declared in userValidationCommon.js

// Validate the user ID from the form
USER.validateUserId = function () {
    var eid = USER.get("user_eid");
    var userIdReq = USER.get("userIdRequired");
    USER.userValid = false;
    if (eid === null || userIdReq === null || USER.trim(eid.value).length > 0) {
        USER.userValid = true;
    }

    USER.validatePassword();
};

// Validate the password from the form
USER.validatePassword = function () {
    var strongMsg = USER.get("strongMsg");
    var moderateMsg = USER.get("moderateMsg");
    var weakMsg = USER.get("weakMsg");
    var failMsg = USER.get("failMsg");
    var strengthInfo = USER.get("strengthInfo");
    var strengthBar = USER.get("strengthBar");
    var strengthBarMeter = USER.get("strengthBarMeter");
    var pw = USER.get("user_pw");

    // If there's a password field and the password policy is enabled, get the password valud and the user ID
    if (pw !== null && USER.isPasswordPolicyEnabled) {
        var pass = pw.value;
        var eid = USER.get("user_eid");
        var eidValue = USER.get("eidValue");
        var username = "";
        if (eid !== null) {
            username = USER.trim(eid.value);
        }
        else if (eidValue !== null) {
            username = eidValue.innerHTML;
        }

        // If the password field has a value:
        // 1) make the AJAX call to the validate password REST endpoint
        // 2) conditionally display the appropriate messages
        // 3) conditionally hide/show the strength info message
        if (pass.length > 0) {
            USER.validatePasswordREST(pass, username);
            USER.displayMessages(strongMsg, moderateMsg, weakMsg, failMsg, strengthBar, strengthBarMeter);
            USER.displayStrengthInfo();
        }

        // Password field has no value, hide all messages
        else {
            USER.hideAllElements(strongMsg, moderateMsg, weakMsg, failMsg, strengthInfo, strengthBar, strengthBarMeter);
        }
    }

    // There is no password field or the password policy is disabled, mark the password as valid and hide all messages
    else {
        USER.hideAllElements(strongMsg, moderateMsg, weakMsg, failMsg, strengthInfo, strengthBar, strengthBarMeter);
        USER.passwordValid = true;
    }

    // Verify the passwords match (which in turn validates the form)
    USER.verifyPasswordsMatch();
};

// Verify the passwords match
USER.verifyPasswordsMatch = function () {
    var pw = USER.get("user_pw");
    var pw0 = USER.get("user_pw0");
    var matchMsg = USER.get("matchMsg");
    var noMatchMsg = USER.get("noMatchMsg");
    USER.passwordsMatch = false;

    if (pw !== null) {
        var pass = pw.value;
        var verPass = pw0.value;
        USER.passwordsMatch = pass === verPass;

        if (pass.length > 0 && verPass.length > 0) {
            USER.display(matchMsg, USER.passwordsMatch);
            USER.display(noMatchMsg, !USER.passwordsMatch);
        }
        else {
            USER.display(matchMsg, false);
            USER.display(noMatchMsg, false);
        }
    }
    else {
        return;
    }

    USER.validateForm();
};

// Validate the email address from the form
USER.validateEmail = function () {
    USER.emailValid = false;
    var email = USER.get("email");
    var emailWarningMsg = USER.get("emailWarningMsg");

    if (email === null) {
        USER.emailValid = true;
    }
    else {
        var address = USER.trim(email.value);

        if (address.length < 1) {
            USER.emailValid = true;
        }
        else {
            USER.emailValid = USER.checkEmail(address);
        }
    }

    USER.display(emailWarningMsg, !USER.emailValid);
    USER.validateForm();
};

// Validate the current password from the form
USER.validateCurrentPassword = function () {
    var pwcur = USER.get("user_pwcur");
    USER.currentPassValid = true;
    if (pwcur !== null) {
        USER.currentPassValid = pwcur.value.length > 0;
    }

    USER.validateForm();
};

// Validate the form (enabled/disable the submit button)
USER.validateForm = function () {
    var submitButton = USER.get("eventSubmit_doSave");

    if (USER.userValid && USER.passwordsMatch && USER.emailValid && (USER.isSuperUser || (USER.passwordValid && USER.currentPassValid))) {
        submitButton.disabled = false;
    }
    else {
        submitButton.disabled = true;
    }

    setMainFrameHeightNow(window.name);
};

// Initialization function
jQuery(document).ready(function () {
    USER.validateEmail();
    USER.validateCurrentPassword();
    USER.validateUserId();
});
