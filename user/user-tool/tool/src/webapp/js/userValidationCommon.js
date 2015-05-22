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
// 'Namespace'
var USER = {};

// Variables
USER.emailValid = false;
USER.userValid = false;
USER.passwordValid = false;
USER.currentPassValid = false;
USER.passwordWeak = false;
USER.passwordModerate = false;
USER.passwordStrong = false;
USER.passwordsMatch = false;
USER.isPasswordPolicyEnabled = false;
USER.lastSentPasswordLength = 0; // SAK-29099

// Get an element by ID
USER.get = function (id) {
    return document.getElementById(id);
};

// Trim leading and trailing whitespace from the given string
USER.trim = function (inputString) {
    return inputString.replace(/^\s+|\s+$/g, "");
};

// Show/hide the given element
USER.display = function (element, show) {
    if (show) {
        if (element) {
            element.style.display = "block";
        }
    }
    else {
        if (element) {
            element.style.display = "none";
        }
    }
};

// Validate the given email address string
USER.checkEmail = function (email) {
    if (!email || email.length < 5) {
        return false;
    }

    var simpleRegexForEmail = /\S+@\S+\.\S\S+/;
    return simpleRegexForEmail.test(email);
};

// Conditionally hide/show the strength info message
USER.displayStrengthInfo = function () {
    if (USER.isPasswordPolicyEnabled) {
        var showStrengthInfo = false;
        var strengthInfo = USER.get("strengthInfo");
        var passField = USER.get("user_pw");
        if (passField !== null && passField.value.length > 0) {
            if (!USER.passwordValid || (!USER.passwordStrong && passField === document.activeElement)) {
                showStrengthInfo = true;
            }
        }

        USER.display(strengthInfo, showStrengthInfo);
    }
};

// Make the AJAX call to the validate password REST endpoint
USER.validatePasswordREST = function (password, username) {
    
    // SAK-29099 - password likely hasn't changed, so abort
    if (password.length === USER.lastSentPasswordLength) {
        return;
    }
    
    jQuery.ajax({
        url: "/direct/user/validatePassword",
        type: "POST",
        data: "password=" + password + "&username=" + username,
        async: false,
        success: function (data) {
            USER.passwordValid = false;
            USER.passwordWeak = false;
            USER.passwordModerate = false;
            USER.passwordStrong = false;

            if ("WEAK" === data) {
                USER.passwordValid = true;
                USER.passwordWeak = true;
            }
            else if ("MODERATE" === data) {
                USER.passwordValid = true;
                USER.passwordModerate = true;
            }
            else if ("STRONG" === data) {
                USER.passwordValid = true;
                USER.passwordStrong = true;
            }
            
            // SAK-29099 - track current length of input password
            USER.lastSentPasswordLength = password.length;
        }
    });
};

// Display the appropriate messages based on the current password valid and strength status
USER.displayMessages = function (strongMsg, moderateMsg, weakMsg, failMsg, strengthBar, strengthBarMeter) {
    USER.display(strongMsg, USER.passwordStrong);
    USER.display(moderateMsg, USER.passwordModerate);
    USER.display(weakMsg, USER.passwordWeak);
    USER.display(failMsg, !USER.passwordValid);

    if (USER.passwordStrong) {
        strengthBarMeter.style.width = "100%";
        strengthBarMeter.style.backgroundColor = "#178c0b";
    }
    else if (USER.passwordModerate) {
        strengthBarMeter.style.width = "66%";
        strengthBarMeter.style.backgroundColor = "#edbc03";
    }
    else if (USER.passwordWeak) {
        strengthBarMeter.style.width = "33%";
        strengthBarMeter.style.backgroundColor = "#900";
    }
    else {
        strengthBarMeter.style.width = "0%";
        strengthBarMeter.style.backgroundColor = "#900";
    }

    var showStrengthBar = (USER.passwordStrong || USER.passwordModerate || USER.passwordWeak || !USER.passwordValid);
    USER.display(strengthBar, showStrengthBar);
    USER.display(strengthBarMeter, showStrengthBar);
};

// Hide all password policy related messages
USER.hideAllElements = function (strongMsg, moderateMsg, weakMsg, failMsg, strengthInfo, strengthBar, strengthBarMeter) {
    USER.display(strongMsg, false);
    USER.display(moderateMsg, false);
    USER.display(weakMsg, false);
    USER.display(failMsg, false);
    USER.display(strengthInfo, false);
    USER.display(strengthBar, false);
    USER.display(strengthBarMeter, false);
};
