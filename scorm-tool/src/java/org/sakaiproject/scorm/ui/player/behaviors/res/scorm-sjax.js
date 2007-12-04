// Scorm Sjax Namespace
if (typeof(ScormSjax) == "undefined")
	ScormSjax = { };


ScormSjax.pausecomp = function pausecomp(call_number, call) {
	//Wicket.Log.info("URL is: " + call.request.url);
	var startDate = new Date();
	var curDate = null;
	
	do { 
		curDate = new Date(); 
	    ScormSjax.lookForResponse(call.request);	
		//Wicket.Log.info("api result: " + api_result[call_number]);
		var diff = curDate - startDate;
		//Wicket.Log.info("diff: " + diff);
		if (diff > 300) {
			//Wicket.Log.info("Timed out--unable to get result: " + api_result[call_number] + " for " + call_number);
			break;
		}
		
	}
	while(api_result[call_number] == undefined);
} 


ScormSjax.sjaxCall = function sjaxCall(scoId, url, arg1, arg2, successHandler, failureHandler, precondition, channel) {
	//Wicket.Log.info("Calling sjaxCall with url: " + url + " sco: " + scoId + " arg1: " + arg1 + " arg2: " + arg2);
		
	var call = new Wicket.Ajax.Call(url + '&scoId=' + scoId + '&arg1=' + arg1 + '&arg2=' + arg2 + '&callNumber=' + call_number, function() {}, function() {}, channel);
	call.request.async = false;
	var resultCode = call.call();
	
	ScormSjax.pausecomp(call_number, call);
	
	//Wicket.Log.info("sjaxCall resultCode = " + resultCode);
	var resultValue = api_result[call_number];
	//Wicket.Log.info("sjaxCall resultValue = " + resultValue);
	call_number++;
	return resultValue;
};




ScormSjax.lookForResponse = function lookForResponse(request) {	
		var t = request.transport;

		if (t != null && t.readyState == 4) {
			try {
				status = t.status;
			}
			catch (e) {
				Wicket.Log.error("Exception evaluating AJAX status: " + e);
				status = "unavailable";
			}
			if (status == 200 || status == "") { // as stupid as it seems, IE7 sets satus to "" on ok
				// response came without error
				var responseAsText = t.responseText;
				
				// first try to get the redirect header
				var redirectUrl;
				try {
					redirectUrl = t.getResponseHeader('Ajax-Location');
				} catch (ignore) { // might happen in older mozilla
				}
				
				// the redirect header was set, go to new url
				if (typeof(redirectUrl) != "undefined" && redirectUrl != null && redirectUrl != "") {
					t.onreadystatechange = Wicket.emptyFunction;
					window.location = redirectUrl;
				}
				else {
					// no redirect, just regular response
					var log = Wicket.Log.info;				
					log("Received ajax response (" + responseAsText.length + " characters)");
					if (request.debugContent != false) {
						log("\n" + responseAsText);
					}
	        		
	        		// parse the response if the callback needs a DOM tree
	        		if (request.parseResponse == true) {
						var xmldoc;					
						if (typeof(window.XMLHttpRequest) != "undefined" && typeof(DOMParser) != "undefined") {						
							var parser = new DOMParser();
							xmldoc = parser.parseFromString(responseAsText, "text/xml");						
						} else if (window.ActiveXObject) {
							xmldoc = t.responseXML;
						}
						// invoke the loaded callback with an xml document
						request.loadedCallback(xmldoc); 
					} else {
						// invoke the loaded callback with raw string
						request.loadedCallback(responseAsText);
					}        		
					if (request.suppressDone == false)
						request.done();
				}
        	} else {
        		// when an error happened
        		var log = Wicket.Log.error;
        		log("Received Ajax response with code: " + status);
		   		request.done();        		
        		request.failure();
        	}    	
        	t.onreadystatechange = Wicket.emptyFunction;
        	t.abort();
        	request.transport = null;       
        }       
    };
















