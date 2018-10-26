// Monitor input actions.
function installInputEventListener(field, functionName) {
	if (field.attachEvent) {
	    field.attachEvent("onfocus", functionName);
	    field.attachEvent("onkeyup", functionName);
	    field.attachEvent("onmouseup", functionName);
	    field.attachEvent("oncut", functionName);
	    field.attachEvent("onpaste", functionName);
	} else if (field.addEventListener) {
	    field.addEventListener("focus", functionName, true);
	    field.addEventListener("keyup", functionName, true);
	    field.addEventListener("mouseup", functionName, true);
	}
}

// Describe what fields to monitor and to use for updates.
// The assumptions are that we want to track a list of elements
// whose IDs only differ by an embedded counter, and that the
// dynamic size tracking message for each input has the same
// ID up and including the counter. These are JSF conventions.
var dynamicSizeCheckTargets = {
	idPrefix : "",
	idPostfix : "",
	sizeIdPostfix : "",
	maxLength : 0,
	sizeMessage : ""
};

function dynamicSizeCheck(fieldId) {
	var field = document.getElementById(fieldId);
	var newSize = field.value.length;
	var sizeMessageDiv = fieldId.replace(dynamicSizeCheckTargets.idPostfix, dynamicSizeCheckTargets.sizeIdPostfix);
	sizeMessageDiv = document.getElementById(sizeMessageDiv);

	// Mozilla / Firefox supports "textContent" and "innerHTML".
	// MSIE supports "innerHTML" and "innerText". So "innerHTML"
	// it is.
	var oldSizeText = sizeMessageDiv.innerHTML;
	var newSizeText = dynamicSizeCheckTargets.sizeMessage.replace("{0}", (dynamicSizeCheckTargets.maxLength - newSize));
	if (oldSizeText != newSizeText) {
		sizeMessageDiv.innerHTML = newSizeText;
	}
}

function dynamicSizeCheckListener(ev) {
	if (ev == null)
		ev = window.event;
	if (ev == null)
		return;
	var src = (ev.srcElement ? ev.srcElement : ev.target);
	if (src == null)
		return;
	dynamicSizeCheck(src.id);
}

function initDynamicSizeCheckListener() {
	var i = 0;
	while (true) {
		var textareaId = dynamicSizeCheckTargets.idPrefix + i + dynamicSizeCheckTargets.idPostfix;
		var textareaElement = document.getElementById(textareaId);
		if (textareaElement == null)
			break;
		installInputEventListener(textareaElement, dynamicSizeCheckListener);
		dynamicSizeCheck(textareaId);
		i++;
	}
}

function initDynamicSizeCheck(idPrefix, idPostfix, sizeIdPostfix, maxLength, sizeMessage) {
	dynamicSizeCheckTargets.idPrefix = idPrefix;
	dynamicSizeCheckTargets.idPostfix = idPostfix;
	dynamicSizeCheckTargets.sizeIdPostfix = sizeIdPostfix;
	dynamicSizeCheckTargets.maxLength = maxLength;
	dynamicSizeCheckTargets.sizeMessage = sizeMessage;
	if (window.addEventListener)
		window.addEventListener("load", initDynamicSizeCheckListener, false);
	else if (window.attachEvent)
		window.attachEvent("onload", initDynamicSizeCheckListener);
}
