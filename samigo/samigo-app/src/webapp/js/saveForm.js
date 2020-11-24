
// If the user hits a button while we are waiting for an update
// we get a data discrepancy, because he will submit it with an
// old value of the time. So we disable the buttons while doing an
// autosave. Because Javascript doesn't have a sleep, we can't
// just delay the submit.
//    This array is a list of the buttons we have disabled.
// It doesn't include anything that was already disabled.

var disabledButtons = [];
var counter = 0;

// confirm box needs to disable autosave temporarily

//    Links are more difficult, as there isn't a disabled
// attribute for them. Currently I clear onmouseup and onclick.
//    I've tried to make this script generic, but if it is
// used with other jsp's, you may need to save and restore additional 
// attributes of the links. If it's used on a page where they don't
// have id's you'll need another way to identify them.
//    disabledLinks is a list of triples, [id, onmouseup, onclick]

var disabledLinks = [];

function GetFormContent(formId, buttonName) {

    try {
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

function SaveFormContentAsync(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds, ok ) {
    if (!ok) { 
		return;
    }

	if (repeatMilliseconds == -1) {
		return;
	}

    // asyncronously send form content to toUrl, wait for response, sleep, repeat
    function onready_callback(text) {
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
	    j = text.indexOf('"', i+7);
	}
	var d = -1;
	if (j >= 0) {
	    var d = text.substring(i+7, j);
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

	// Now that we have the updated date, it's safe for the user to do submits.
	// Reenable any buttons we disabled.
	for (var i=0; i<disabledButtons.length; i++) {
	    document.forms[formId].elements[disabledButtons[i]].disabled=false;
	}

	// And links
	for (var i=0; i<disabledLinks.length; i++) {
	    var item = disabledLinks[i];
	    var link = document.getElementById(item[0]);
	    link.onmouseup = item[1];
	    link.onclick = item[2];
	}

        // wait and then call save form again
	if (saveok) {
	    var onTimeout = TimeOutAction(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds);
	    setTimeout(onTimeout, repeatMilliseconds);
	} else {
	    //alert("Attempt to save your work automatically failed. One common cause is that you have a second window open on Tests and Quizes. We strongly suggest that you not continue working in this window. If you go to the top level of Tests and Quizes, you can restart this test or quiz.");
	}
	window.status = "";

    //check noLateSubmission or isRetracted controlled by pastDueDate()
    if (text.indexOf("noLateSubmission") >= 0 || text.indexOf("isRetracted") >= 0 || text.indexOf("assessment_has_been_submitted") >= 0) {
        $("#autosave-timeexpired-warning").show();
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
            j = text.indexOf('"', i+7);
        }
        if (j >= 0) {
            var dueDateorRetractDate = text.substring(i+7, j);
            if (document.getElementById("progressbar") === null && dueDateorRetractDate - timeNow <= repeatMilliseconds) {
                $("#autosave-timeleft-warning").show();
            }
        }
    }

        // when the request is done the scope of the function can be garbage collected...
    }

    if (counter > 0) {
      var payload = GetFormContent(formId, buttonName);

      $.ajax({ method: "POST", url: toUrl, data: payload }, function () {
        // Promises below will allow handling of a connection failure
      })
        .done(function (html) {
          onready_callback(html);
        })
        .fail(function () {
          $("#autosave-failed-warning").show();
          onready_callback("");
        });
    } else {
	    var onTimeout = TimeOutAction(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds);
	    setTimeout(onTimeout, repeatMilliseconds);
    }

    counter += 1;
}

function TimeOutAction(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds) {
    function ActionResult() {
	SaveFormContentAsync(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds, true);
    }
    return ActionResult;
}

