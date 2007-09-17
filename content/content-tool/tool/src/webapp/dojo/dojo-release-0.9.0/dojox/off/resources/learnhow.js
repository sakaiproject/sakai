window.onload = function(){
	// get the app name from our URL
	var href = window.location.href;
	var matches = href.match(/appName=([a-z0-9 \%]*)/i);
	var appName = "Application";
	if(matches && matches.length > 0){
		appName = decodeURIComponent(matches[1]);
	}
	
	// set it in our UI
	var appNameSpan = document.getElementById("dot-learn-how-app-name");
	appNameSpan.innerHTML = "";
	appNameSpan.appendChild(document.createTextNode(appName));
	
	// if we need an offline cache, and we already have one installed,
	// update the UI
	matches = href.match(/hasOfflineCache=(true|false)/);
	var hasOfflineCache = false;
	if(matches && matches.length > 0){
		hasOfflineCache = matches[1];
		// convert to boolean
		hasOfflineCache = (hasOfflineCache == "true") ? true : false;
	}
	if(hasOfflineCache == true){
		// delete the download and install steps
		var downloadStep = document.getElementById("dot-download-step");
		var installStep = document.getElementById("dot-install-step");
		downloadStep.parentNode.removeChild(downloadStep);
		installStep.parentNode.removeChild(installStep);
	}
	
	// get our run link info and update the UI
	matches = href.match(/runLink=([^\&]*)\&runLinkText=([^\&]*)/);
	if(matches && matches.length > 0){
		var runLink = decodeURIComponent(matches[1]);
		var runLinkElem = document.getElementById("dot-learn-how-run-link");
		runLinkElem.setAttribute("href", runLink);
		
		var runLinkText = decodeURIComponent(matches[2]);
		runLinkElem.innerHTML = "";
		runLinkElem.appendChild(document.createTextNode(runLinkText));
	}
}
