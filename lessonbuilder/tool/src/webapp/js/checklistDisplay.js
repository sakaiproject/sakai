(function (checklistDisplay, $, undefined) {

	checklistDisplay.initSaveChecklistForm = function (checklistIdInputId, checklistItemIdInputId, checklistItemDoneId, csrfFieldId, elBinding) {

		var checklistIdInput = document.getElementById(checklistIdInputId);
		var checklistItemIdInput = document.getElementById(checklistItemIdInputId);
		var checklistItemDone = document.getElementById(checklistItemDoneId);
		var csrfField = document.getElementById(csrfFieldId);

		var ajaxUrl = checklistIdInput.form.action;

		var callback = function (results) {
			if (results.EL[elBinding][0] === "success") {
				$("[id*='error-checklist-not-saved']").hide();
			} else {
				$("[id*='error-checklist-not-saved']").show();
			}
		};

		// setup the function which initiates the AJAX request
		var updater = RSF.getAJAXUpdater([checklistIdInput, checklistItemIdInput, checklistItemDone, csrfField], ajaxUrl, [elBinding], callback);
		// setup the input field event to trigger the ajax request function
		checklistItemDone.onchange = updater; // send request when field changes
	};
}(window.checklistDisplay = window.checklistDisplay || {}, jQuery));