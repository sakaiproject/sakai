(function (checklistDisplay, $, undefined) {

    checklistDisplay.initSaveChecklistForm = function (checklistIdInputId, checklistItemIdInputId, checklistItemDoneId, csrfFieldId, elBinding) {

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
        };

        // setup the function which initiates the AJAX request
        var updater = RSF.getAJAXUpdater([checklistIdInput, checklistItemIdInput, checklistItemDone, csrfField], ajaxUrl, [elBinding], callback);
        // setup the input field event to trigger the ajax request function
        checklistItemDone.onchange = updater; // send request when field changes
    };

    checklistDisplay.setUpToolTipDisplay = function() {
        $(".checklistLabel.disabled").each(function() {
           $(this).mouseenter(function(){
               var tooltipContent = $(this).siblings(".tooltip-content");

               tooltipContent.fadeIn().position({
                   "my": "right-15 bottom+10",
                   "at": "left bottom",
                   "of": this
               });
           });

           $(this).mouseleave(function() {
               $(this).siblings(".tooltip-content").fadeOut();
           })
        });
    }
}(window.checklistDisplay = window.checklistDisplay || {}, jQuery));

window.onload = checklistDisplay.setUpToolTipDisplay;
