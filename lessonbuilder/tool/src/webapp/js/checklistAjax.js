(function (checklistAjax, $, undefined) {

    checklistAjax.initSaveChecklistForm = function (checklistIdInputId, checklistItemIdInputId, checklistItemDoneId, csrfFieldId, elBinding) {

        var checklistIdInput = document.getElementById(checklistIdInputId);
        var checklistItemIdInput = document.getElementById(checklistItemIdInputId);
        var checklistItemDone = document.getElementById(checklistItemDoneId);
        var csrfField = document.getElementById(csrfFieldId);

        var ajaxUrl = checklistIdInput.form.action;

        var callback = function (results) {

            var status = results.EL[elBinding][0];

            if (status === "success") {
                $(".savingChecklistItem").parent().nextAll(".saveChecklistSaving").hide();
                $(".savingChecklistItem").parent().nextAll(".saveChecklistSuccess").show().delay(3000).fadeOut();
                $(".savingChecklistItem").removeClass("savingChecklistItem");
            } else {
                $(".savingChecklistItem").parent().nextAll(".saveChecklistSaving").hide();
                $(".savingChecklistItem").parent().nextAll(".saveChecklistError").show().delay(3000).fadeOut();
                $(".savingChecklistItem").removeClass("savingChecklistItem");
            }
        }

        // setup the function which initiates the AJAX request
        var updater = RSF.getAJAXUpdater([checklistIdInput, checklistItemIdInput, checklistItemDone, csrfField], ajaxUrl, [elBinding], callback);
        // setup the input field event to trigger the ajax request function
        checklistItemDone.onchange = updater; // send request when field changes

    }
}(window.checklistAjax = window.checklistAjax || {}, jQuery));