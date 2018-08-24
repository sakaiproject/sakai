// scripts for the #pagerPanel velocity macro (see VM_chef_library.vm in the velocity project)
// will use SPNR (spinner.js) if it is already loaded
var VM_PP = VM_PP || {};

VM_PP.doChangePageSize = VM_PP.doChangePageSize || function(url, selectElement)
{
    if (typeof SPNR !== "undefined") // if SPNR is available, use it
    {
        SPNR.disableControlsAndSpin(selectElement, null);
    }
    var selectedPageSizeValue = selectElement.options[selectElement.selectedIndex].value;
    location = encodeURI(url + "&selectPageSize=" + selectedPageSizeValue);
    return true;
};

VM_PP.doPageNav = VM_PP.doPageNav || function(url, buttonElement)
{
    if (typeof SPNR !== "undefined") // if SPNR is available, use it
    {
        SPNR.disableControlsAndSpin(buttonElement, null);
    }
    location = encodeURI(url);
    return true;
};
