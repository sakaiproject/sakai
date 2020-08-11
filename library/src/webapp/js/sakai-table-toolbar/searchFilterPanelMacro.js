// scripts for the #searchFilterPanel velocity macro (see VM_chef_library.vm in the velocity project)
// will use SPNR (spinner.js) if it is already loaded
var VM_SFP = VM_SFP || {};

VM_SFP.spinButton = VM_SFP.spinButton || function(buttonElement)
{
    // if SPNR is available, use it
    if (typeof SPNR !== "undefined")
    {
        // if variable "escapeList" is defined on the page, use it
        var escape = typeof escapeList === "undefined" ? null : escapeList;
        SPNR.disableControlsAndSpin(buttonElement, escape);
    }
};

VM_SFP.keyupListener = VM_SFP.keyupListener || function(event)
{
    if (event.keyCode === 13) // Enter
    {
        // immediate nextSibling is an empty text node 
        event.currentTarget.nextSibling.nextSibling.click();
    }
};

var searchFields = document.getElementsByClassName("sakai-table-searchFilter-searchField");
Array.prototype.forEach.call(searchFields, function(field) {
    field.addEventListener("keyup", VM_SFP.keyupListener);
});

VM_SFP.doSearch = VM_SFP.doSearch || function(url, buttonElementId, textElementId)
{
    var buttonElement = document.getElementById(buttonElementId);
    VM_SFP.spinButton(buttonElement);
    var searchText = document.getElementById(textElementId).value;
    location = encodeURI(url + "&search=" + searchText);
    return true;
};

VM_SFP.doClearSearch = VM_SFP.doClearSearch || function(url, buttonElement)
{
    VM_SFP.spinButton(buttonElement);
    location = encodeURI(url);
    return false;
};
