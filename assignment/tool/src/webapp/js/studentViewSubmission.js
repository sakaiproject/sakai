var ASN_SVS = ASN_SVS || {};
var ASN = ASN || {};
var ASN_TS_API = ASN_TS_API || {};

window.i18nWlogTab = [];

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

ASN_TS_API.addTimeSheet = function (button, onSuccess, onError) {
    var messages = [];
    const endpoint = "/direct/assignment/addTimeSheet.json";
    const params = {
        "tsAssignmentId" : document.getElementById("assignmentId").value,
        "tsStartTime" : document.getElementById("startTime").value,
        "new_ts_record_day" : document.getElementById("new_ts_record_day").value,
        "new_ts_record_month" : document.getElementById("new_ts_record_month").value,
        "new_ts_record_year" : document.getElementById("new_ts_record_year").value,
        "new_ts_record_hour" : document.getElementById("new_ts_record_hour").value,
        "new_ts_record_minute" : document.getElementById("new_ts_record_minute").value,
        "tsComment" : document.getElementById("comment").value,
        "tsDuration" : document.getElementById("duration").value,
    };
    if(!document.getElementById("comment").value){ messages.push("ts.add.err.comment"); }
    if(!document.getElementById("duration").value){ messages.push("ts.add.err.duration"); }
    button.classList.add("spinButton");
    button.disabled = true;
    if(messages.length === 0){
        ASN_TS_API._POST(endpoint, params, onSuccess, onError);
    } else {
        onError(null, messages);
    }
};

ASN_TS_API.removeTimeSheet = function (button, onSuccess, onError) {
    var messages = [];
    const endpoint = "/direct/assignment/removeTimeSheet.json";
    const params = {
        "selectedTimeSheets" : [...document.getElementsByName("selectedTimesheet")].filter((el) => el.checked).map((el) => el.value),
        "tsAssignmentId" : document.getElementById("assignmentId").value,
    };
    if(!params.selectedTimeSheets || params.selectedTimeSheets.length === 0){messages.push("ts.rem.err.empty"); }
    button.classList.add("spinButton");
    button.disabled = true;
    if(messages.length === 0){
        ASN_TS_API._POST(endpoint, params, onSuccess, onError);
    } else {
        onError(null, messages);
    }
};

ASN_TS_API._GET = function (url, data, onSuccess, onError, onComplete) {
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

ASN_TS_API._POST = function (url, data, onSuccess, onError, onComplete) {
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

ASN.switchTimesheetTab = function (source) {
    if (['tabAssignment', 'tabTimeSheet'].includes(source)) {
        document.getElementById('tabAssignmentCurrent').classList.toggle('hidden');
        document.getElementById('tabAssignmentRef').classList.toggle('hidden');
        document.getElementById('tabTimeSheetRef').classList.toggle('hidden');
        document.getElementById('tabTimeSheetCurrent').classList.toggle('hidden');
        document.getElementById('StudentAssignmentCurrent').classList.toggle('hidden');
        document.getElementById('StudentTimesheetCurrent').classList.toggle('hidden');
    }
};

ASN.tsHandleAjaxAddSuccess = function (data) {
    ASN.submitForm( 'addSubmissionForm', 'view', null, null );
};

ASN.tsHandleAjaxRemoveSuccess = function (data) {
    ASN.submitForm( 'addSubmissionForm', 'view', null, null );
};

ASN.tsAddHandleAjaxError = function (xhr, messagesParam) {
    const messages = typeof(messagesParam) === 'string' ? [] : messagesParam;
    if(xhr && xhr.status){
      switch(xhr.status){
        case 400: messages.push("ts.add.err.duration");
                  break;
        case 401: messages.push("ts.add.err.permission");
                  break;
        case 403: messages.push("ts.add.err.userId");
                  break;
        case 404: messages.push("ts.add.err.assignmentId");
      }
    }

    const button = document.getElementById("btnTimesheetAdd");
    button.classList.remove("spinButton");
    button.disabled = false;

    const alertTsheetAddRecord = document.getElementById("alertTsheetAddRecord");
    alertTsheetAddRecord.classList.remove("hidden");
    // Object.keys(window.i18nWlogTab).find((key) => key.includes('ts.add.err.permission'))
    let messageArray = [];
    for (const [index, key] of Object.entries(messages)) {
      messageArray.push(window.i18nWlogTab[key]);
    }
    const node = document.createElement("br");
    alertTsheetAddRecord.appendChild(node);
};

ASN.tsRemoveHandleAjaxError = function (xhr, messagesParam) {
    const messages = typeof(messagesParam) === 'string' ? [] : messagesParam;
    if(xhr && xhr.status){
      switch(xhr.status){
        case 400: messages.push("ts.add.err.assignmentId");
                  break;
        case 401: messages.push("ts.add.err.permission");
                  break;
        case 403: messages.push("ts.rem.err.userId");
      }
    }

    const button = document.getElementById("btnTimesheetDelete");
    button.classList.remove("spinButton");
    button.disabled = false;

    const alertTsheetDelRecord = document.getElementById("alertTsheetDelRecord");
    alertTsheetDelRecord.classList.remove("hidden");
    // Object.keys(window.i18nWlogTab).find((key) => key.includes('ts.add.err.permission'))
    let messageArray = [];
    for (const [index, key] of Object.entries(messages)) {
      messageArray.push(window.i18nWlogTab[key]);
    }
    const node = document.createElement("br");
    alertTsheetDelRecord.appendChild(node);
};

ASN.checkTimesheetRecord = function () {
    const selected = document.querySelectorAll("input[name='selectedTimesheet']:checked").length > 0;
    document.getElementById("btnTimesheetDelete").disabled = !selected;
    document.getElementById("btnTimesheetDelete").className = (selected ? "btn btn-primary active" : "");
};

ASN.checkTimesheetSpent = function (totalTimeRecord) {
    if (document.getElementById("timeTimesheet")?.checked) {
        document.getElementById("assignment_input_add_time_spent").disabled = true;
        document.getElementById("assignment_input_add_time_spent").value = totalTimeRecord;
    } else {
        document.getElementById("assignment_input_add_time_spent").disabled = false;
        document.getElementById("assignment_input_add_time_spent").value = document.getElementById("assignment_input_add_submission_time").value;
    }
};

//SAK-43155
ASN.addTimeSheet = function (button) {
    ASN_TS_API.addTimeSheet(button, ASN.tsHandleAjaxAddSuccess, ASN.tsAddHandleAjaxError);
};

//SAK-43155
ASN.removeTimeSheet = function (button) {
    ASN_TS_API.removeTimeSheet(button, ASN.tsHandleAjaxRemoveSuccess, ASN.tsRemoveHandleAjaxError);
};
