function fireScheduleTimeClicked(obj, e, formId, scheduleId) {
	//make sure it works on IE too
	if (!e) var e = window.event;
	//capture the mouse coordinates relative to the foreground layer
	var y = e.layerY;
	var el = e.target;
	//make sure it works in IE too
	if (!y) y = e.offsetY;
	if (!el) el = e.srcElement;
	if (el != obj) return false;
	var dateInputId = scheduleId + "_last_clicked_date";
	var yInputId = scheduleId + "_last_clicked_y";
	document.forms[formId][dateInputId].value = el.id;
	document.forms[formId][yInputId].value = y;
	document.forms[formId].submit();
	return true;
}

function fireScheduleDateClicked(obj, e, formId, scheduleId) {
	//make sure it works on IE too
	if (!e) var e = window.event;
	var el = e.target;
	//make sure it works in IE too
	if (!el) el = e.srcElement;
	if (el != obj) return false;
	var dateInputId = scheduleId + "_last_clicked_date";
	document.forms[formId][dateInputId].value = el.id;
	document.forms[formId].submit();
	return true;
}

function fireEntrySelected(formId, scheduleId, entryId) {
	document.forms[formId][scheduleId].value = entryId;
	document.forms[formId].submit();
	return true;
}
