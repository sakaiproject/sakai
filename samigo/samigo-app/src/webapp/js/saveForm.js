
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
        var name = elt.name;
        var type = typeof(elt.type)=='string' ? elt.type.toLowerCase() : '';
        var value = elt.value;
        var encoded = encodeURIComponent(name)+"="+encodeURIComponent(value);
	if (type == "submit" && !elt.disabled) {
	    // save name of buttons we are disabling, and disable
	    disabledButtons.push(name);
	    elt.disabled = true;
        }
        if (type != "submit" &&
	    !((type == "radio" || type == "checkbox") && !elt.checked)){
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

var counter = 0

function SaveFormContentAsync(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds, ok ) {
    if (!ok) { 
		return;
    }

	if (repeatMilliseconds == -1) {
		return;
	}

    // asyncronously send form content to toUrl, wait for response, sleep, repeat
    //    var theStatus = document.getElementById(statusId);
    counter += 1;
    //    theStatus.innerHTML = "count "+counter+" saving form at "+Date();
    var request = initXMLHTTPRequest();
    var method = "POST";
    var async = true;
    var payload;
    function onready_callback() {
	var state = request.readyState;
	if (state!=4) {
	    // ignore intermediate states
	    return;
	}
	    // This is an Ajax response. It isn't normally processed.
	// So if we need anything from it we have to get it.
	// Get new date from response and update the form variable

	var saveok = true;
	var text = request.responseText;
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
	    document.forms[0].elements[disabledButtons[i]].disabled=false;
	}

	// And links

	for (var i=0; i<disabledLinks.length; i++) {
	    var item = disabledLinks[i];
	    var link = document.getElementById(item[0]);
	    link.onmouseup = item[1];
	    link.onclick = item[2];
	}
	//	theStatus.innerHTML = "count "+counter+" save complete at "+Date();
        // wait and then call save form again
	if (saveok) {
	    var onTimeout = TimeOutAction(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds);
	    setTimeout(onTimeout, repeatMilliseconds);
	} else {
	    //alert("Attempt to save your work automatically failed. One common cause is that you have a second window open on Tests and Quizes. We strongly suggest that you not continue working in this window. If you go to the top level of Tests and Quizes, you can restart this test or quiz.");
	}
	window.status = "";

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
            j = text.indexOf('"', i+7);
        }
        if (j >= 0) {
            var dueDateorRetractDate = text.substring(i+7, j);
            if (dueDateorRetractDate - timeNow <= repeatMilliseconds) {
                timeLeft();
            }
        }
    }

        // when the request is done the scope of the function can be garbage collected...
    }
    if (doautosave && counter > 1) {
	payload = GetFormContent(formId, buttonName);
	request.open(method, toUrl, async);
	request.onreadystatechange = onready_callback;
	request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	//alert("about to send");
	window.status = "Saving...";
	request.send(payload);
    } else {
	//alert("first time" + 	    document.forms[0].elements['takeAssessmentForm:lastSubmittedDate1'].value);
        var onTimeout = TimeOutAction(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds);
        setTimeout(onTimeout, repeatMilliseconds);
    }

    // onready_callback called on request response.
}

function TimeOutAction(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds) {
    function ActionResult() {
	SaveFormContentAsync(toUrl, formId, buttonName, updateVar, updateVar2, repeatMilliseconds, true);
    }
    return ActionResult;
}

function initXMLHTTPRequest() {
        var result = null;
        if (window.XMLHttpRequest) {
                ////al("xmlhttp");
                result = new XMLHttpRequest();
        } else if (window.ActiveXObject) {
                ////al("microsoft");
                result = new ActiveXObject("Microsoft.XMLHTTP");
        } else {
                throw new Error("failed to create XMLHTTPRequest");
        }
        return result;
}

