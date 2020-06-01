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