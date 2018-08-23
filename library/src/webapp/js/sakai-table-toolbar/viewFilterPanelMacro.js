// scripts for the #viewFilterPanel velocity macro (see VM_chef_library.vm in the velocity project)
// will use SPNR (spinner.js) if it is already loaded
var VM_VFP = VM_VFP || {};

VM_VFP.doChangeView = VM_VFP.doChangeView || function(url, selectElement)
{
	if (typeof SPNR !== "undefined")
	{
		SPNR.disableControlsAndSpin(selectElement, null);
	}
	var selectedView = selectElement.options[selectElement.selectedIndex].value;
	location = encodeURI(url + "&" + selectElement.name + "=" + selectedView);
	return true;
};
