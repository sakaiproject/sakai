var dhtmlPopupVars = new Array;
dhtmlPopupVars.currentWin = "";
dhtmlPopupVars.dragging = false;
dhtmlPopupVars.offsetX = 0;
dhtmlPopupVars.offsetY = 0;

function dhtmlDebug(s) {
	var theDiv = document.getElementById("dhtmlMonitor");
	if (theDiv != null) {
		theDiv.innerHTML = theDiv.innerHTML + s;
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

function dhtmlGetInnerWidth() {
	var theWidth = 0;
	if (typeof(window.innerWidth) == "number") {
		theWidth = window.innerWidth;
	} else if (document.documentElement && document.documentElement.clientWidth) {
		theWidth = document.documentElement.clientWidth;
	} else if (document.body && document.body.clientWidth) {
		theWidth = document.body.clientWidth;
	}
	return theWidth;
}
function dhtmlGetInnerHeight() {
	var theHeight = 0;

	if (typeof(window.innerHeight) == "number") {
		theHeight = window.innerHeight;
	} else if (document.documentElement && document.documentElement.clientHeight) {
		theHeight = document.documentElement.clientHeight;
	} else if (document.body && document.body.clientHeight) {
		theHeight = document.body.clientHeight;
	}
	return theHeight;
}
function dhtmlGetPageXOffset() {
	theOffset = 0;
	if (typeof(window.pageXOffset) == "number") {
		theOffset = window.pageXOffset;
	} else if (document.body && document.body.scrollLeft) {
		theOffset = document.body.scrollLeft;
	} else if (document.documentElement && document.documentElement.scrollLeft) {
		theOffset = document.documentElement.scrollLeft;
	}
	return theOffset;
}
function dhtmlGetPageYOffset() {
	theOffset = 0;
	if (typeof(window.pageYOffset) == "number") {
		theOffset = window.pageYOffset;
	} else if (document.body && document.body.scrollTop) {
		theOffset = document.body.scrollTop;
	} else if (document.documentElement && document.documentElement.scrollTop) {
		theOffset = document.documentElement.scrollTop;
	}
	return theOffset;
}

function dhtmlPopupHide(winName, e) {
	var theDiv = dhtmlPopupGetDiv(winName);
	theDiv.style.visibility = "hidden";
}

function dhtmlPopupShow(winName, e) {
	var theDiv = dhtmlPopupGetDiv(winName);
	dhtmlDebug("<br />e.clientX=" + e.clientX + ", dhtmlGetPageXOffset=" + dhtmlGetPageXOffset() + ", dhtmlGetInnerWidth=" + dhtmlGetInnerWidth() + ", dhtmlGetPageYOffset=" + dhtmlGetPageYOffset() + ", theDiv.offsetWidth=" + theDiv.offsetWidth + ", theDiv.offsetHeight=" + theDiv.offsetHeight);
	if (e.clientX > 0) {
		var maxLeftPos = dhtmlGetInnerWidth() - theDiv.offsetWidth;
		var leftPos = e.clientX - 10;
		if (leftPos > maxLeftPos) {
			leftPos = maxLeftPos;
		}
		theDiv.style.left = (leftPos) + "px";
		theDiv.style.top = (dhtmlGetPageYOffset() + e.clientY + 15) + "px";
	} else {
		// The keyboard was used instead of the mouse, so we
		// don't have a good starting position.
		// Just center the popup for now.
		theDiv.style.left = (dhtmlGetPageXOffset() + dhtmlGetInnerWidth()/2 - theDiv.offsetWidth/2) + "px";
		theDiv.style.top = (dhtmlGetPageYOffset() + dhtmlGetInnerHeight()/2 - theDiv.offsetHeight/2) + "px";
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
