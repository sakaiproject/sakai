// If the user hits a button while we are waiting for an update
// we get a data discrepancy, because he will submit it with an
// old value of the time. So we disable the buttons while doing an
// autosave. Because Javascript doesn't have a sleep, we can't
// just delay the submit.
//    This array is a list of the buttons we have disabled.
// It doesn't include anything that was already disabled.
var disabledButtons = [];

// confirm box needs to disable autosave temporarily
var doautosave = true;
var counter = 0;
var failCounter = 0;
var successCounter = 0;
var formId, buttonName, updateVar, updateVar2, repeatMilliseconds;

//    Links are more difficult, as there isn't a disabled
// attribute for them. Currently I clear onmouseup and onclick.
//    I've tried to make this script generic, but if it is
// used with other jsp's, you may need to save and restore additional 
// attributes of the links. If it's used on a page where they don't
// have id's you'll need another way to identify them.
//    disabledLinks is a list of triples, [id, onmouseup, onclick]
var disabledLinks = [];

function GetFormContent() {
    try {
        toggleSubmissionControls(false);
        //If the autosave submits any fill in numeric question, validate it before submitting. Wipe the value if it's incorrect and notify the user.
        $('.fillInNumericInput').each( function() {
          validateFinInput(this);
        });
    } catch(error) {
        //Fail silently if this validation fails.
    }

    var theForm = document.getElementById(formId);
    var elements = theForm.elements;
    var pairs = [];
    disabledButtons = [];
    //autosave for ckeditor
    for(var i in CKEDITOR.instances) {
        var encoded = encodeURIComponent(CKEDITOR.instances[i].name)+"="+encodeURIComponent(CKEDITOR.instances[i].getSnapshot());
        pairs.push(encoded);
    }
    for (var i=0; i<elements.length; i++) {
        var elt = elements[i];
        var eltName = elt.name;
        if (!eltName) continue;

        var type = typeof(elt.type)=='string' ? elt.type.toLowerCase() : '';
        var value = elt.value;
        var encoded = encodeURIComponent(eltName) + "=" + encodeURIComponent(value);
        if (type == "submit" && !elt.disabled) {
            // save name of buttons we are disabling, and disable
            disabledButtons.push(eltName);
            elt.disabled = true;
        }
        else if (type != "submit" && !((type == "radio" || type == "checkbox") && !elt.checked)) {
            pairs.push(encoded);
        }
    }
    // save attributes and disable links
    disabledLinks = [];
    for (var i=0; i < document.links.length; i++){
	var link = document.links[i];
	if (link.id != null && link.id != "" && link.onclick != null) {
	    disabledLinks.push([link.id, link.onmouseup, link.onclick]);
	    link.onmouseup = null;
	    link.onclick = null;
	}
    }
    pairs.push(encodeURIComponent(buttonName)+"="+encodeURIComponent("true"));
    //    alert(pairs.join("&"));
    return pairs.join("&");
}

function SaveFormContentAsync(toUrl, formIdFromServer, buttonNameFromServer, updateVarFromServer, updateVar2FromServer, repeatMillisecondsFromServer, ok ) {
    if (!ok || repeatMilliseconds == -1) return;

    counter += 1;
    formId = formIdFromServer;
    buttonName = buttonNameFromServer;
    updateVar = updateVarFromServer;
    updateVar2 = updateVar2FromServer;
    repeatMilliseconds = repeatMillisecondsFromServer;

    // asyncronously send form content to toUrl, wait for response, sleep, repeat
    if (doautosave && counter > 1) {
        var payload = GetFormContent();
        asyncSaveToServer(toUrl, payload);
        // console.debug("payload: " + payload);
    } else {
        var onTimeout = TimeOutAction(toUrl);
        setTimeout(onTimeout, repeatMilliseconds);
    }
}

function TimeOutAction(toUrl) {
    function ActionResult() {
        SaveFormContentAsync(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds, true);
    }
    return ActionResult;
}

function asyncSaveToServer(toUrl, postData) {
    var success = true;

    $.ajax({ method: "POST", url: toUrl, data: postData }, function () {
        // Promises below will allow handling of a connection failure
    })
        .done(function (html) {
            connAlive = true;
            successCounter++;

            // User connection working again?
            if (failCounter > 1) {
                $("#autosave-conn-error").hide();
            }

            success = updateFormFromAsyncResponse(html);
        })
        .fail(function () {
            alert("error");
            connAlive = false;
            failCounter++;
            $("#autosave-conn-error").show();
        })
        .always(function () {
            // Reactivate the buttons even if the user connection appears to be down
            reactivateButtons();

            // wait and then call save form again
            if (success) {
                var onTimeout = TimeOutAction(toUrl);
                setTimeout(onTimeout, repeatMilliseconds);
            } else {
                $("#autosave-submit-error").show();
                console.error("problem saving async. will not try again.");
            }
        });
}

function reactivateButtons() {
    // Now that we have the updated date, it's safe for the user to do submits.
    // Reenable any buttons we disabled.
    for (var i = 0; i < disabledButtons.length; i++) {
        document.forms[formId].elements[disabledButtons[i]].disabled = false;
    }

    // And links
    for (var i = 0; i < disabledLinks.length; i++) {
        var item = disabledLinks[i];
        var link = document.getElementById(item[0]);
        link.onmouseup = item[1];
        link.onclick = item[2];
    }
}

function updateFormFromAsyncResponse(text) {
    // This is an Ajax response. It isn't normally processed.
    // So if we need anything from it we have to get it.
    // Get new date from response and update the form variable
    var saveok = true;
    var i = text.indexOf('"' + updateVar);
    if (i < 0) {
        i = text.indexOf('"' + updateVar2);
    }
    var j = -1;
    if (i >= 0) {
        i = text.indexOf("value=", i);
    }
    if (i >= 0) {
        j = text.indexOf('"', i + 7);
    }
    var d = -1;
    if (j >= 0) {
        var d = text.substring(i + 7, j);
        if (document.forms[0].elements[updateVar] != null) {
            document.forms[0].elements[updateVar].value = d;
        } else if (document.forms[0].elements[updateVar2] != null) {
            document.forms[0].elements[updateVar2].value = d;
        } else {
            saveok = false;
        }
    } else {
        saveok = false;
    }

    //check noLateSubmission or isRetracted controlled by pastDueDate()
    var noLateSubmission = text.indexOf("noLateSubmission");
    var isRetracted = text.indexOf("isRetracted");
    if (noLateSubmission >= 0 || isRetracted >= 0) {
        timeExpired();
        $("[id$=\\:submitNoCheck]")[0].click();
    }

    if (d !== -1) {
        var timeNow = Date.now();
        var i = text.indexOf("retractDate");
        if (i >= 0) {
            i = text.indexOf("value=", i);
        }
        else {
            i = text.indexOf("dueDate");
            if (i >= 0) {
                i = text.indexOf("value=", i);
            }
        }
        var j = -1;
        if (i >= 0) {
            j = text.indexOf('"', i + 7);
        }
        if (j >= 0) {
            var dueDateorRetractDate = text.substring(i + 7, j);
            if (dueDateorRetractDate - timeNow <= repeatMilliseconds) {
                timeLeft();
            }
        }
    }

    return saveok;
}