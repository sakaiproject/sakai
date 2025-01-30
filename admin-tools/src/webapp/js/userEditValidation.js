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

  const userIdReqField = USER.get("userIdRequired");
  USER.userValid = false;
  const eid = USER.get("user_eid")?.value;
  if (!eid || !userIdReqField || USER.trim(eid).length > 0) {
    USER.userValid = true;
  }

  USER.validatePassword();
};

// Validate the password from the form
USER.validatePassword = function () {

  const strongMsg = USER.get("strongMsg");
  const moderateMsg = USER.get("moderateMsg");
  const weakMsg = USER.get("weakMsg");
  const failMsg = USER.get("failMsg");
  const strengthInfo = USER.get("strengthInfo");
  const strengthBar = USER.get("strengthBar");
  const strengthBarMeter = USER.get("strengthBarMeter");
  const pass = USER.get("user_pw")?.value;

  // If there's a password field and the password policy is enabled, get the password valud and the user ID
  if (pass && USER.isPasswordPolicyEnabled) {
    const eid = USER.get("user_eid")?.value;
    const eidValue = USER.get("eidValue")?.innerHTML;
    let username = "";
    if (eid) {
      username = USER.trim(eid);
    } else if (eidValue) {
      username = eidValue;
    }

    // If the password field has a value:
    // 1) make the AJAX call to the validate password REST endpoint
    // 2) conditionally display the appropriate messages
    // 3) conditionally hide/show the strength info message
    if (pass.length > 0) {
      USER.validatePasswordREST(pass, username);
    } else {
      // Password field has no value, hide all messages
      USER.hideAllElements(strongMsg, moderateMsg, weakMsg, failMsg, strengthInfo, strengthBar, strengthBarMeter);
    }
  } else {
    // There is no password field or the password policy is disabled, mark the password as valid and hide all messages
    USER.hideAllElements(strongMsg, moderateMsg, weakMsg, failMsg, strengthInfo, strengthBar, strengthBarMeter);
    USER.passwordValid = true;
  }

  // Verify the passwords match (which in turn validates the form)
  USER.verifyPasswordsMatch();
};

// Verify the passwords match
USER.verifyPasswordsMatch = function () {

  const pass = USER.get("user_pw")?.value;
  USER.passwordsMatch = false;

  if (pass !== null) {
    const verPass = USER.get("user_pw0")?.value;
    USER.passwordsMatch = pass === verPass;

    const matchMsg = USER.get("matchMsg");
    const noMatchMsg = USER.get("noMatchMsg");

    if (pass.length > 0 && verPass.length > 0) {
      USER.display(matchMsg, USER.passwordsMatch);
      USER.display(noMatchMsg, !USER.passwordsMatch);
    } else {
      USER.display(matchMsg, false);
      USER.display(noMatchMsg, false);
    }

    USER.validateForm();
  }
};

// Validate the email address from the form
USER.validateEmail = function () {

  USER.emailValid = false;
  const email = USER.get("email")?.value;

  if (!email) {
    USER.emailValid = true;
  } else {
    const address = USER.trim(email);

    if (address.length < 1) {
      USER.emailValid = true;
    } else {
      USER.emailValid = USER.checkEmail(address);
    }
  }

  USER.display(USER.get("emailWarningMsg"), !USER.emailValid);
  USER.validateForm();
};

// Validate the current password from the form
USER.validateCurrentPassword = function () {

  const pwcur = USER.get("user_pwcur")?.value;
  USER.currentPassValid = true;
  if (pwcur != null) {
    USER.currentPassValid = pwcur.length > 0;
  }

  USER.validateForm();
};

// Validate the form (enabled/disable the submit button)
USER.validateForm = function () {

  const submitButton = USER.get("eventSubmit_doSave");
  submitButton && (submitButton.disabled = !(USER.userValid && USER.passwordsMatch && USER.emailValid && (USER.isSuperUser || (USER.passwordValid && USER.currentPassValid))));
  setMainFrameHeightNow(window.name);
};

// Initialization function
window.addEventListener("DOMContentLoaded", () => {

  USER.validateEmail();
  USER.validateCurrentPassword();
  USER.validateUserId();
});
