/**************************************************************************************
 *                    Gradebook Bulk Edit Javascript                                   
 *************************************************************************************/
function toggleCheckboxes(checkboxFamily) {
  if(document.getElementById(checkboxFamily+'ToggleAll').checked) {
    $(':checkbox').each(function() {
      if(this.name.includes(checkboxFamily)) {
        this.checked = true;
      }
    });
  } else {
    $(':checkbox').each(function() {
      if(this.name.includes(checkboxFamily)) {
        this.checked = false;
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

