var ASN_SVS = ASN_SVS || {};
var ASN = ASN || {};
var ASN_TS_API = ASN_TS_API || {};

window.i18nProgresa = [];

/* For the cancel button - if the user made progress, we need them to confirm that they want to discard their progress */
ASN_SVS.confirmDiscardOrSubmit = function(editorInstanceName, attachmentsModified)
{
	var inlineProgress = false;
	var ckEditor = CKEDITOR.instances[editorInstanceName];
	if (ckEditor)
	{
		inlineProgress = ckEditor.checkDirty();
	}
	var showDiscardDialog = inlineProgress || attachmentsModified;
	var submitPanel = document.getElementById("submitPanel");
	var confirmationDialogue = document.getElementById("confirmationDialogue");
	if (showDiscardDialog)
	{
		submitPanel.style.display = "none"
		confirmationDialogue.style.display = "block";
	}
	else
	{
		SPNR.disableControlsAndSpin( this, null );
		ASN.submitForm( 'addSubmissionForm', 'cancel', null, null );
	}
};

ASN_SVS.undoCancel = function()
{
	var submitPanel = document.getElementById("submitPanel");
	var confirmationDialogue = document.getElementById("confirmationDialogue");
	submitPanel.style.display = "block";
	confirmationDialogue.style.display = "none";
};

ASN_TS_API.addTimeSheet = function(button, onSuccess, onError)
{
	var endpoint = "/direct/assignment/addTimeSheet.json";
	var params = {};

	var tsassignmentId = document.getElementById("assignmentId").value;
	var tsDate = document.getElementById("regDate").value;
	
	var newTsRecordDay = document.getElementById("newTsRecordDay").value;
	var newTsRecordMonth = document.getElementById("newTsRecordMonth").value;
	var newTsRecordYear = document.getElementById("newTsRecordYear").value;
	var newTsRecordHour = document.getElementById("newTsRecordHour").value;
	var newTsRecordMinute = document.getElementById("newTsRecordMinute").value;
	
	var tsComment = document.getElementById("regComment").value;
	var tsTime = document.getElementById("regTime").value;
	
	params.assignmentId = tsassignmentId;
	params.regDate = tsDate;
	
	params.newTsRecordDay = newTsRecordDay;
	params.newTsRecordMonth = newTsRecordMonth;
	params.newTsRecordYear = newTsRecordYear;
	params.newTsRecordHour = newTsRecordHour;
	params.newTsRecordMinute = newTsRecordMinute;
	
	params.regComment = tsComment;
	params.regTime = tsTime;
	button.classList.add("spinButton");
	button.disabled = true;
	
	ASN_TS_API._POST(endpoint, params, onSuccess, onError);
};

ASN_TS_API.removeTimeSheet = function(button, onSuccess, onError)
{
	var endpoint = "/direct/assignment/removeTimeSheet.json";
	var params = {};
	var ts = document.getElementsByName("selectedTimesheet");
	var tsassignmentId = document.getElementById("assignmentId").value;

	params.selectedTimeSheets = Array.apply(null, ts).filter((el) => el.checked).map((el) => el.value);
	params.assignmentId = tsassignmentId;

	button.classList.add("spinButton");
	button.disabled = true;

	ASN_TS_API._POST(endpoint, params, onSuccess, onError);
};

ASN_TS_API._GET = function(url, data, onSuccess, onError, onComplete)
{
	$.ajax(
	{
		type: "GET",
		url: url,
		data: data,
		cache: false,
		success: onSuccess || $.noop,
		error: onError || $.noop,
		complete: onComplete || $.noop
	});
};

ASN_TS_API._POST = function(url, data, onSuccess, onError, onComplete)
{
	$.ajax(
	{
		type: "POST",
		url: url,
		data: data,
		success: onSuccess || $.noop,
		error: onError || $.noop,
		complete: onComplete || $.noop
	});
};

ASN.switchTimesheetTab = function( source )
{
	if (['tabAssignment', 'tabTimeSheet'].includes(source)) {
		document.getElementById('tabAssignmentCurrent').classList.toggle('hidden');
		document.getElementById('tabAssignmentRef').classList.toggle('hidden');
		document.getElementById('tabTimeSheetRef').classList.toggle('hidden');
		document.getElementById('tabTimeSheetCurrent').classList.toggle('hidden');
		document.getElementById('StudentAssignmentCurrent').classList.toggle('hidden');
		document.getElementById('StudentTimesheetCurrent').classList.toggle('hidden');
	}
};

ASN.handleAjaxAddSuccess = function(data)
{
	if(data.error && data.error.message) {
		var button = document.getElementById("btnTimesheetAdd");
		button.classList.remove("spinButton");
		button.disabled = false;
		var alertTsheetAddRecord = document.getElementById("alertTsheetAddRecord");
		alertTsheetAddRecord.classList.toggle('hidden');
		alertTsheetAddRecord.innerHTML= window.i18nProgresa[data.error.message];		
	} else {
		ASN.submitForm( 'addSubmissionForm', 'view', null, null );
	}
};

ASN.handleAjaxRemoveSuccess = function(data)
{
	if(data.error && data.error.message) {
		var button = document.getElementById("btnTimesheetDelete");
		button.classList.remove("spinButton");
		button.disabled = false;
		var alertTsheetDelRecord = document.getElementById("alertTsheetDelRecord");
		alertTsheetDelRecord.classList.toggle('hidden');
		alertTsheetDelRecord.innerHTML= window.i18nProgresa[data.error.message];
	} else {
		ASN.submitForm( 'addSubmissionForm', 'view', null, null );
	}
};

ASN.addHandleAjaxError = function(xhr)
{
	var button = document.getElementById("btnTimesheetAdd");
	button.classList.remove("spinButton");
	button.disabled = false;
	alert('Error: ' + xhr.status);
	console.log("Ajax call error when add time sheet register.");
};

ASN.removeHandleAjaxError = function(xhr)
{
	var button = document.getElementById("btnTimesheetDelete");
	button.classList.remove("spinButton");
	button.disabled = false;
	alert('Error: ' + xhr.status);
	console.log("Ajax call error when remove time sheet register.");
};

ASN.checkTimesheetRecord = function()
{
	var selected = document.querySelectorAll("input[name='selectedTimesheet']:checked").length > 0;
    document.getElementById( "btnTimesheetDelete" ).disabled = !selected;
    document.getElementById( "btnTimesheetDelete" ).className = (selected ? "btn btn-primary active" : "" );
};

ASN.checkTimesheetSpent = function(totalTimeRecord)
{
	if( document.getElementById( "timeTimesheet" ).checked ) {
		document.getElementById( "assignment_input_add_time_spent" ).disabled = true;
		document.getElementById( "assignment_input_add_time_spent" ).value = totalTimeRecord;
	} else {
		document.getElementById( "assignment_input_add_time_spent" ).disabled = false;
		document.getElementById( "assignment_input_add_time_spent" ).value = document.getElementById("assignment_input_add_submission_time").value;
	}
};

ASN.addTimeSheet = function(button)
{
    ASN_TS_API.addTimeSheet(button, ASN.handleAjaxAddSuccess, ASN.addHandleAjaxError);
};

ASN.removeTimeSheet = function(button)
{
	ASN_TS_API.removeTimeSheet(button, ASN.handleAjaxRemoveSuccess, ASN.removeHandleAjaxError);
};