/**************************************************************************************
 *                    Gradebook Bulk Edit Javascript                                   
 *************************************************************************************/
function toggleCheckboxes(checkboxFamily) {
  if(document.getElementById(checkboxFamily+'ToggleAll').checked) { //selecting all
    $(':checkbox').each(function() {
      if(this.name.includes(checkboxFamily) && !this.checked && !this.disabled) {
        this.click();
      }
    });
  } else {  //deselecting all
    $(':checkbox').each(function() {
      if(this.name.includes(checkboxFamily) && this.checked && !this.disabled) {
        this.click();
      }
    });
  }
}

function releaseToggleCheckboxes() {
  toggleCheckboxes('release');
}

function includeToggleCheckboxes() {
  toggleCheckboxes('include');
}

function deleteToggleCheckboxes() {
  toggleCheckboxes('delete');
}

function disableAndStrikeoutOneRow(deleteBox) {
    var listRow = deleteBox.parentElement.parentElement;
    var releaseBox = listRow.getElementsByClassName('release')[0];
    var includeBox = listRow.getElementsByClassName('include')[0];
    var titleBox = listRow.getElementsByClassName('itemTitle')[0];
    if (deleteBox.checked){
        if (includeBox.disabled){	//if Include is already disabled, it's supposed to stay disabled; this appears to be the case for Uncategorized items.
            includeBox.setAttribute('class', includeBox.getAttribute('class') + ' stayDisabled');	//fake CSS class to denote that this box should always be disabled
        }
        releaseBox.disabled = true;
        includeBox.disabled = true;
        titleBox.setAttribute('style', 'text-decoration: line-through;');
    } else {
        releaseBox.disabled = false;
        if (!includeBox.getAttribute('class').includes('stayDisabled')){	//don't enable ones that are marked as stayDisabled.
            includeBox.disabled = false;
        }
        titleBox.removeAttribute('style');
    }
}

var GBBE = GBBE || {};

GBBE.init = function(contentId) {
    GBBE.$content = $(document.getElementById(contentId));
    GBBE.$fakeSubmit = $(document.getElementById("gb-bulk-edit-fake-submit"));
    GBBE.$realSubmit = $(document.getElementById("gb-bulk-edit-real-submit"));

    GBBE.$fakeSubmit.off('click').on('click', GBBE.handleFakeSubmit);
};

GBBE.showConfirmation = function() {
    const templateHtml = $("#bulkEditConfirmationModalTemplate").html().trim();
    const modalTemplate = TrimPath.parseTemplate(templateHtml);
    const $confirmationModal = $(modalTemplate.process());

    $confirmationModal.one("click", ".gb-bulk-edit-continue", function() {
        GBBE.performRealSubmit();
    });
    $(document.body).append($confirmationModal);

    $confirmationModal.on("hidden.bs.modal", function() {
        $confirmationModal.remove();
    });
    $confirmationModal.on("show.bs.modal", function() {
        const $formModal = GBBE.$content.closest(".wicket-modal");
        $confirmationModal.css("marginTop", $formModal.offset().top + 40);
    });

    $confirmationModal.on("shown.bs.modal", function() {
        $confirmationModal.find(".gb-bulk-edit-cancel").focus();
    });

    $confirmationModal.modal().modal('show');
};

GBBE.performRealSubmit = function() {
    GBBE.$fakeSubmit.hide();
    GBBE.$realSubmit.toggleClass("hide");
    GBBE.$realSubmit.trigger("click");
    GBBE.$content.find(":input").prop("disabled", true);
};

GBBE.handleFakeSubmit = function(event) {

    if (document.querySelectorAll(".deleteBox:checked").length > 0) {
        event.preventDefault();
        event.stopPropagation();

        GBBE.showConfirmation();

        return false;
    }

    GBBE.performRealSubmit();
};