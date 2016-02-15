// bbailla2, plukasew, bjones86 - SAK-24427

// 'Namespace'
var VALIDATOR = VALIDATOR || {};

// Variables
VALIDATOR.passwordValid = false;
VALIDATOR.passwordWeak = false;
VALIDATOR.passwordModerate = false;
VALIDATOR.passwordStrong = false;
VALIDATOR.passwordsMatch = false;
VALIDATOR.firstNameValid = false;
VALIDATOR.lastNameValid = false;
VALIDATOR.termsChecked = false;
VALIDATOR.isPasswordPolicyEnabled = false;
VALIDATOR.lastSentPasswordLength = 0; // SAK-29099

// Validate the password from the form
VALIDATOR.validatePassword = function() {
	var username = VALIDATOR.get("eid").innerHTML;
	var pw = VALIDATOR.get("passrow1::password1").value;
	var strongMsg = VALIDATOR.get("strongMsg");
	var moderateMsg = VALIDATOR.get("moderateMsg");
	var weakMsg = VALIDATOR.get("weakMsg");
	var failMsg = VALIDATOR.get("failMsg");
	var strengthInfo = VALIDATOR.get("strengthInfo");
	var strengthBar = VALIDATOR.get("strengthBar");
	var strengthBarMeter = VALIDATOR.get("strengthBarMeter");
	
	// SAK-29099 - password likely hasn't changed, so abort
	if (pw.length === VALIDATOR.lastSentPasswordLength) {
		return;
	}
	
	// If the password policy is enabled and the password field has a value
	if (VALIDATOR.isPasswordPolicyEnabled && pw.length > 0) {
		
		// Make the AJAX call to the validate password REST endpoint
		jQuery.ajax({
	    	url: "/direct/user/validatePassword",
	    	type: "POST",
			data: "password=" + pw + "&username=" + username,
			async: false,
			success: function(data) {
				VALIDATOR.passwordValid = false;
				VALIDATOR.passwordWeak = false;
				VALIDATOR.passwordModerate = false;
				VALIDATOR.passwordStrong = false;
				
				if ("WEAK" === data) {
					VALIDATOR.passwordValid = true;
					VALIDATOR.passwordWeak = true;
				}
				else if ("MODERATE" === data) {
					VALIDATOR.passwordValid = true;
					VALIDATOR.passwordModerate = true;
				}
				else if ("STRONG" === data) {
					VALIDATOR.passwordValid = true;
					VALIDATOR.passwordStrong = true;
				}
				
				// SAK-29099 - track current length of input password
				VALIDATOR.lastSentPasswordLength = pw.length;
	    	}
	    });
		
		// Display the appropriate messages
		var showStrengthBar = (VALIDATOR.passwordStrong || VALIDATOR.passwordModerate || VALIDATOR.passwordWeak || !VALIDATOR.passwordValid);
		VALIDATOR.display(strongMsg, VALIDATOR.passwordStrong);
		VALIDATOR.display(moderateMsg, VALIDATOR.passwordModerate);
		VALIDATOR.display(weakMsg, VALIDATOR.passwordWeak);
		VALIDATOR.display(failMsg, !VALIDATOR.passwordValid);
		VALIDATOR.display(strengthBar, showStrengthBar);
		VALIDATOR.display(strengthBarMeter, showStrengthBar);
		VALIDATOR.displayStrengthInfo();
		
		// Update the strength meter accordingly
		if (VALIDATOR.passwordStrong) {
			strengthBarMeter.style.width = "100%";
			strengthBarMeter.style.backgroundColor = "#178c0b";
		}
		else if (VALIDATOR.passwordModerate) {
			strengthBarMeter.style.width = "66%";
			strengthBarMeter.style.backgroundColor = "#edbc03";
		}
		else if (VALIDATOR.passwordWeak) {
			strengthBarMeter.style.width = "33%";
			strengthBarMeter.style.backgroundColor = "#900";
		}
		else {
			strengthBarMeter.style.width = "0%";
			strengthBarMeter.style.backgroundColor = "#900";
		}
	}
	
	// Otherwise, password policy is disabled or the password field has no value
	else {
		VALIDATOR.display(strongMsg, false);
		VALIDATOR.display(moderateMsg, false);
		VALIDATOR.display(weakMsg, false);
		VALIDATOR.display(failMsg, false);
		VALIDATOR.display(strengthInfo, false);
		VALIDATOR.display(strengthBar, false);
		VALIDATOR.display(strengthBarMeter, false);
		VALIDATOR.passwordValid = pw.length > 0;
	}
	
	// Verify the passwords match (which in turn validates the form)
	VALIDATOR.verifyPasswordsMatch();
};

// Verify the passwords match
VALIDATOR.verifyPasswordsMatch = function() {
	var pw = VALIDATOR.get("passrow1::password1").value;
	if(VALIDATOR.get("passrow2::password2")) {
		var pw2 = VALIDATOR.get("passrow2::password2").value;
	}
	else {
		var pw2 = pw;
	}

	var matchMsg = VALIDATOR.get("matchMsg");
	var noMatchMsg = VALIDATOR.get("noMatchMsg");
	
	VALIDATOR.passwordsMatch = pw === pw2;
	if (pw.length > 0 || pw2.length > 0) {
		VALIDATOR.display(matchMsg, VALIDATOR.passwordsMatch);
		VALIDATOR.display(noMatchMsg, !VALIDATOR.passwordsMatch);
	}
	else {
		VALIDATOR.display(matchMsg, false);
		VALIDATOR.display(noMatchMsg, false);
	}
	
	VALIDATOR.validateActivateForm();
};

// Validate the first name on the form
VALIDATOR.validateFirstName = function() {
	VALIDATOR.firstNameValid = false;
	var firstName = VALIDATOR.get("firstName");
	if (firstName === null || firstName.value.length > 0) {
		VALIDATOR.firstNameValid = true;
	}
	
	VALIDATOR.validateActivateForm();
};

// Validate the last name on the form
VALIDATOR.validateLastName = function() {
	VALIDATOR.lastNameValid = false;
	var lastName = VALIDATOR.get("surName");
	if (lastName === null || lastName.value.length > 0) {
		VALIDATOR.lastNameValid = true;
	}
	
	VALIDATOR.validateActivateForm();
};

VALIDATOR.validateTermsChecked = function() {
	VALIDATOR.termsChecked = true;
	var terms = VALIDATOR.get("termsrow::terms");
	if (terms !== null)
	{
		VALIDATOR.termsChecked = terms.checked;
	}

	VALIDATOR.validateActivateForm();
};

// Conditionally show/hide the strength info message
VALIDATOR.displayStrengthInfo = function() {
	if (VALIDATOR.isPasswordPolicyEnabled) {
		var showStrengthInfo = false;
		var strengthInfo = VALIDATOR.get("strengthInfo");
		var passField = VALIDATOR.get("passrow1::password1");
		if (passField.value.length > 0) {
			if (!VALIDATOR.passwordValid || (!VALIDATOR.passwordStrong && passField === document.activeElement)) {
				showStrengthInfo = true;
			}
		}
		
		VALIDATOR.display(strengthInfo, showStrengthInfo);
	}
};

// Validate the form (enable/disable the submit button)
VALIDATOR.validateActivateForm = function() {
	var submitButton = VALIDATOR.get("addDetailsSub");
	if (submitButton !== null)
	{
		if (VALIDATOR.firstNameValid && VALIDATOR.lastNameValid && VALIDATOR.passwordValid && VALIDATOR.passwordsMatch && VALIDATOR.termsChecked) {
			submitButton.disabled = false;
		}
		else {
			submitButton.disabled = true;
		}
	}
};

// bbailla2 - enables/disables the Transfer memberships button as well as the yes button based on how the required fields are filled
VALIDATOR.checkTransferStatus = function() {
	var enable = false;
	var userId = VALIDATOR.get("userName");
	var transfer = VALIDATOR.get("transferMemberships");

	if (userId && transfer) {
		if (userId.value !== "") {
			var pw = VALIDATOR.get("password");
			if (pw.value !== "") {
				enable = true;
			}
		}

		if (enable) {
			transfer.disabled = false;
		}
		else {
			transfer.disabled = true;
		}
	}
};

// Get an element by ID
VALIDATOR.get = function(id) {
	return document.getElementById(id);
};

//Determine if the given string is empty/null
VALIDATOR.isEmpty = function(inputString) {
	return inputString === null || inputString.length === 0 || inputString.replace(/^\s*/, "").replace(/\s*$/, "") === "";
};

// Show/hide the given element
VALIDATOR.display = function(element, show) {
	if (show) {
		element.style.display = "block";
	}
	else {
		element.style.display = "none";
	}
};

// Original document ready function
$(document).ready(function() {
    /* Hide div with yellow background if it is not null and is empty */
    if (($('.yellowBackground').length > 0) && ($('.yellowBackground').html().trim() == '')){
	   $('.yellowBackground').hide();
    }
    if ($("form").length === 0) {
        $("table").remove();
        return false;
    }
    var css_invalidField = {"border":"1px solid red"};
    $("input.inputBox").bind("keyup", function(){
        $(this).removeAttr("style");
    });
    $("input[type=checkbox]").click(function() {
    	$(this).parents(".required").removeAttr("style");
    });
    $("input.submit").bind("click", function() {
        var that = $(this),
                form = that.parents("form:eq(0)"),
                errors = 0;
        $("form .required").removeAttr("style");
        $.each(form.find(".required"), function(i, _this) {
            var field = $(_this);
            if (field.attr("type") === "text") {
                if (VALIDATOR.isEmpty(field.val())) {
                    field.css(css_invalidField);
                    errors ++;
                }
            } else if (field.attr("type") === "password") {
                if (field.attr("class").search("password2") === -1) {
                    if (VALIDATOR.isEmpty(field.val())) {
                        field.css(css_invalidField);
                        errors ++;
                    }
                }
            // This is for the checkbox as we can't add a border to it.
            } else if (field.is("div")) {
                $("input[type=checkbox]", field).each(function(j, checkbox) {
                    if (!$(checkbox).is(":checked")) {
                        field.css(css_invalidField);
                        errors ++;
                    }
                });
            }
        });
        if (form.find("input.password2").length > 0) {
            var p1 = form.find("input.password1"),
                    p2 = form.find("input.password2");
            if (!VALIDATOR.isEmpty(p1.val())) {
                if (p1.val() !== p2.val()) {
                    p1.css(css_invalidField);
                    p2.css(css_invalidField);
                    errors ++;
                }
            }
        }
        return errors === 0;
    });

    // SAK-24427
    VALIDATOR.validateFirstName();
    VALIDATOR.validateLastName();
    VALIDATOR.validateTermsChecked();
    VALIDATOR.checkTransferStatus();
});
