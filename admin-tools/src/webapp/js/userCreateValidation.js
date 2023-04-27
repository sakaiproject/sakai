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

// Validate the password from the form
USER.validatePassword = function () {

  const username = USER.trim((USER.get("user_eid") || USER.get("email")).value);
  const pwField = USER.get("user_pw");
  if (!pwField) {
    // There's no password field to validate; consider the password valid.
    USER.passwordValid = true;
    USER.passwordsMatch = true;
    return;
  }
  const pw = pwField.value;
  const strongMsg = USER.get("strongMsg");
  const moderateMsg = USER.get("moderateMsg");
  const weakMsg = USER.get("weakMsg");
  const failMsg = USER.get("failMsg");
  const strengthInfo = USER.get("strengthInfo");
  const strengthBar = USER.get("strengthBar");
  const strengthBarMeter = USER.get("strengthBarMeter");

  // If the password field has a value:
  // 1) make the AJAX call to the validate password REST endpoint
  // 2) conditionally display the appropriate messages
  // 3) conditionally hide/show the strength info message
  if (USER.isPasswordPolicyEnabled && pw.length > 0) {
    USER.validatePasswordREST(pw, username);
  } else {
    // Otherwise, password policy is disabled OR the password field has no value
    USER.passwordValid = pw.length > 0;
    USER.hideAllElements(strongMsg, moderateMsg, weakMsg, failMsg, strengthInfo, strengthBar, strengthBarMeter);
  }

  // Verify the passwords match (which in turn validates the form)
  USER.verifyPasswordsMatch();
};

// Verify the passwords match
USER.verifyPasswordsMatch = function () {

  const pwField = USER.get("user_pw");

  if (!pwField) return;

  const pw = pwField.value;
  const matchMsg = USER.get("matchMsg");
  const noMatchMsg = USER.get("noMatchMsg");
  const pw2 = USER.get("user_pw0").value;

  USER.passwordsMatch = pw === pw2;
  if (pw.length > 0 && pw2.length > 0) {
    USER.display(matchMsg, USER.passwordsMatch);
    USER.display(noMatchMsg, !USER.passwordsMatch);
  } else {
    USER.display(matchMsg, false);
    USER.display(noMatchMsg, false);
  }

  USER.validateForm();
};

// Validate the user ID from the form
USER.validateUserId = function () {

  const eidField = USER.get("user_eid");
  if (eidField) {
    USER.userValid = USER.trim(eidField.value).length > 0
    USER.validatePassword();
  } else {
    USER.userValid = true;
  }
  USER.validateForm();
};

// Validate the email address from the form
USER.validateEmail = function () {

  const emailRequired = USER.get("email").required;
  const email = USER.trim(USER.get("email").value);

  if (email.length < 1) {
    if (emailRequired) {
      USER.emailValid = false;
    }
    else {
      USER.emailValid = true;
    }
  } else {
    USER.emailValid = USER.checkEmail(email);
  }

  USER.display(USER.get("emailWarningMsg"), !USER.emailValid);
  USER.validateForm();
};

// Validate the form (enable/disable the submit button)
USER.validateForm = function () {

  USER.get("eventSubmit_doSave").disabled
    = !(USER.userValid && USER.emailValid && USER.passwordValid && USER.passwordsMatch);

  setMainFrameHeightNow(window.name);
};

// Initialization function
window.addEventListener("DOMContentLoaded", () => {

  USER.validateUserId();
  if (!USER.get('user_eid')) {
    USER.userValid = true;
  }
  if (!USER.get("user_pw")) {
    USER.passwordValid = true;
    USER.passwordsMatch = true;
  }
  if (!USER.get("email") || !USER.get("email").required ) {
    USER.emailValid = true;
  }
  USER.validateForm();
});
