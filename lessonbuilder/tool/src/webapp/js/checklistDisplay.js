(function (checklistDisplay, $, undefined) {

	checklistDisplay.initSaveChecklistForm = function (checklistIdInputId, checklistItemIdInputId, checklistItemDoneId, csrfFieldId, elBinding) {

		const checklistIdInput = document.getElementById(checklistIdInputId);
		const checklistItemIdInput = document.getElementById(checklistItemIdInputId);
		const checklistItemDone = document.getElementById(checklistItemDoneId);
		const csrfField = document.getElementById(csrfFieldId);
		
		if (!checklistIdInput || !checklistItemIdInput || !checklistItemDone || !csrfField) {
		    console.warn("initSaveChecklistForm not called correctly");
		    return;
		}

		const ajaxUrl = checklistIdInput.form.action;

		const callback = function (results) {
			if (results.EL[elBinding][0] === "success") {
				$("[id*='error-checklist-not-saved']").hide();
			} else {
				$("[id*='error-checklist-not-saved']").show();
			}
		};

		// setup the function which initiates the AJAX request
		const updater = RSF.getAJAXUpdater([checklistIdInput, checklistItemIdInput, checklistItemDone, csrfField], ajaxUrl, [elBinding], callback);
		// setup the input field event to trigger the ajax request function
		checklistItemDone.onchange = updater; // send request when field changes
	};
}(window.checklistDisplay = window.checklistDisplay || {}, jQuery));