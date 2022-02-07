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
    const endpoint = "/direct/assignment/addTimeSheet.json";
    const messages = [];

    const tsComment = document.getElementById("comment").value;
    if (tsComment == null || tsComment.trim() === '') {
        messages.push("ts.add.err.comment");
    }

    const tsDuration = document.getElementById("duration").value;
    if (tsDuration == null || tsDuration.trim() === '') {
        messages.push("ts.add.err.duration");
    }

    if (messages.length !== 0) {
        onError(null, messages);
        return;
    }

    const params = {
        "tsAssignmentId" : document.getElementById("assignmentId").value,
        "tsStartTime" : document.getElementById("startTime").value,
        "new_ts_record_day" : document.getElementById("new_ts_record_day").value,
        "new_ts_record_month" : document.getElementById("new_ts_record_month").value,
        "new_ts_record_year" : document.getElementById("new_ts_record_year").value,
        "new_ts_record_hour" : document.getElementById("new_ts_record_hour").value,
        "new_ts_record_minute" : document.getElementById("new_ts_record_minute").value,
        tsComment,
        tsDuration
    };

    button.classList.add("spinButton");
    button.disabled = true;

    ASN_TS_API._POST(endpoint, params, onSuccess, onError);
};

ASN_TS_API.removeTimeSheet = function (button, onSuccess, onError) {
    const endpoint = "/direct/assignment/removeTimeSheet.json";
    const params = {
        "selectedTimeSheets" : [...document.getElementsByName("selectedTimesheet")].filter((el) => el.checked).map((el) => el.value),
        "tsAssignmentId" : document.getElementById("assignmentId").value,
    };

    button.classList.add("spinButton");
    button.disabled = true;

    ASN_TS_API._POST(endpoint, params, onSuccess, onError);
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
    const alertTsheetAddRecord = document.getElementById("alertTsheetAddRecord");
    if (data.error && data.error.message) {
        const button = document.getElementById("btnTimesheetAdd");
        button.classList.remove("spinButton");
        button.disabled = false;
        alertTsheetAddRecord.innerHTML = window.i18nWlogTab[data.error.message];
        if(alertTsheetAddRecord.classList.contains('hidden')) {
            alertTsheetAddRecord.classList.toggle('hidden');
        }
    } else {
        if(!alertTsheetAddRecord.classList.contains('hidden')) {
            alertTsheetAddRecord.classList.toggle('hidden');
        }
        ASN.submitForm('addSubmissionForm', 'view', null, null);
    }
};

ASN.tsHandleAjaxRemoveSuccess = function (data) {
    const alertTsheetDelRecord = document.getElementById("alertTsheetDelRecord");
    if (data.error && data.error.message) {
        const button = document.getElementById("btnTimesheetDelete");
        button.classList.remove("spinButton");
        button.disabled = false;
        alertTsheetDelRecord.innerHTML = window.i18nWlogTab[data.error.message];
        if(alertTsheetDelRecord.classList.contains('hidden')) {
            alertTsheetDelRecord.classList.toggle('hidden');
        }
    } else {
        if(!alertTsheetDelRecord.classList.contains('hidden')) {
            alertTsheetDelRecord.classList.toggle('hidden');
        }
        ASN.submitForm('addSubmissionForm', 'view', null, null);
    }
};

ASN.tsAddHandleAjaxError = function (xhr, messagesParam) {
    const messages = typeof(messagesParam) === 'string' ? [] : messagesParam;

    const button = document.getElementById("btnTimesheetAdd");
    const alertTsheetAddRecord = document.getElementById("alertTsheetAddRecord");
    button.classList.remove("spinButton");
    button.disabled = false;
    if(alertTsheetAddRecord.classList.contains('hidden')) {
        alertTsheetAddRecord.classList.toggle('hidden');
    }
    const messageArray = [];
    alertTsheetAddRecord.innerHTML
      = Object.entries(messages).reduce((acc, entry) => { acc.push(i18nWlogTab[entry[1]]); return acc; }, []).join("<br>");
};

ASN.tsRemoveHandleAjaxError = function (xhr, messagesParam) {
    const messages = typeof(messagesParam) === 'string' ? [] : messagesParam;

    var button = document.getElementById("btnTimesheetDelete");
    const alertTsheetDelRecord = document.getElementById("alertTsheetDelRecord");
    button.classList.remove("spinButton");
    button.disabled = false;
    if(alertTsheetDelRecord.classList.contains('hidden')) {
        alertTsheetDelRecord.classList.toggle('hidden');
    }
    alertTsheetDelRecord.innerHTML
      = Object.entries(messages).reduce((acc, entry) => { acc.push(i18nWlogTab[entry[1]]); return acc; }, []).join("<br>");
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
