
function APIAdapter() { };


function apiCall(formId, url, successHandler, failureHandler, channel) {
	var call = new Wicket.Ajax.Call(url, function() {}, function() {}, channel);
	call.request.async = false;
	return call.submitFormById(formId, null);
}

