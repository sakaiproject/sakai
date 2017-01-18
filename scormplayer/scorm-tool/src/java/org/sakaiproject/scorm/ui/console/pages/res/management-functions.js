// Scorm Management Functions Namespace
if (typeof(ScormManageFunctions) == "undefined")
	ScormManageFunctions = { };

ScormManageFunctions.confirmDelete = function confirmDelete(callbackScript) {
	var isConfirmed = confirm('Continuing will permanently delete this content package');

	if (isConfirmed) {
		var wcall = wicketAjaxGet(callbackScript, function() { }.bind(this), function() { }.bind(this));
	}
}
