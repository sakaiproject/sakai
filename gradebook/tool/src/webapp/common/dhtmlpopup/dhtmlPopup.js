var dhtmlPopupVars = new Array;
dhtmlPopupVars.currentWin = "";
dhtmlPopupVars.dragging = false;
dhtmlPopupVars.offsetX = 0;
dhtmlPopupVars.offsetY = 0;

function dhtmlDebug(s) {
	var theDiv = document.getElementById("dhtmlMonitor");
	if (theDiv != null) {
		theDiv.innerHTML = s;
	}
}

function dhtmlPopupGetDiv(winName) {
	var theDiv = document.getElementById("dhtmlPopup_" + winName);
	return theDiv;
}

function dhtmlPopupToggle(winName, e) {
	var theDiv = dhtmlPopupGetDiv(winName);
	dhtmlDebug("theDiv for " + winName + " = " + theDiv);
	if (theDiv.style.visibility == "hidden") {
		dhtmlPopupShow(winName, e);
	} else {
		dhtmlPopupHide(winName, e);
	}
}

function dhtmlPopupHide(winName, e) {
	var theDiv = dhtmlPopupGetDiv(winName);
	theDiv.style.visibility = "hidden";
}

function dhtmlPopupShow(winName, e) {
	var theDiv = dhtmlPopupGetDiv(winName);
	dhtmlDebug("e.clientX=" + e.clientX + ", window.pageXOffset=" + window.pageXOffset + ", window.innerWidth=" + window.innerWidth + ", window.pageYOffset=" + window.pageYOffset + ", theDiv.offsetWidth=" + theDiv.offsetWidth + ", theDiv.offsetHeight=" + theDiv.offsetHeight);
	if (e.clientX > 0) {
		theDiv.style.left = (e.clientX - 10) + "px";
		theDiv.style.top = (e.clientY + 15) + "px";
	} else {
		// The keyboard was used instead of the mouse, so we
		// don't have a good starting position.
		// Just center the popup for now.
		theDiv.style.left = (window.pageXOffset + window.innerWidth/2 - theDiv.offsetWidth/2) + "px";
		theDiv.style.top = (window.pageYOffset + window.innerHeight/2 - theDiv.offsetHeight/2) + "px";
	}
	theDiv.style.visibility = "visible";
}

function dhtmlPopupMouseover(winName, e) {
	document.onmousedown = dhtmlPopupMousedown;
	dhtmlPopupVars.currentWin = winName;
}

function dhtmlPopupMouseout(e) {
	if (!dhtmlPopupVars.dragging) {
		document.onmousedown = null;
		dhtmlPopupVars.currentWin = "";
	}
}

function dhtmlPopupPxToInt(s) {
	// Strip off the last two characters ("px").
	s = s.substring(0, s.length - 2);
	return parseInt(s);
}

function dhtmlPopupMousedown(e) {
	dhtmlDebug("e=" + e);
	if (typeof e == 'undefined') e = event;
	if (dhtmlPopupVars.currentWin != "") {
		document.onmousemove = dhtmlPopupMousemove;
		document.onmouseup = dhtmlPopupMouseup;
		dhtmlPopupVars.dragging = true;
		var theDiv = dhtmlPopupGetDiv(dhtmlPopupVars.currentWin);
		var currentX = dhtmlPopupPxToInt(theDiv.style.left);
		var currentY = dhtmlPopupPxToInt(theDiv.style.top);
		dhtmlPopupVars.offsetX = e.clientX - currentX;
		dhtmlPopupVars.offsetY = e.clientY - currentY;
	}
}

function dhtmlPopupMouseup(e) {
	dhtmlPopupVars.dragging = false;
	document.onmousemove = null;
	document.onmouseup = null;
}

function dhtmlPopupMousemove(e) {
	if (typeof e == 'undefined') e = event;
	if (dhtmlPopupVars.dragging && (dhtmlPopupVars.currentWin != "")) {
		var theDiv = dhtmlPopupGetDiv(dhtmlPopupVars.currentWin);
		theDiv.style.left = (e.clientX - dhtmlPopupVars.offsetX) + "px";
		theDiv.style.top = (e.clientY - dhtmlPopupVars.offsetY) + "px";
	}
}
