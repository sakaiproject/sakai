var logdiv = null;
var logdivID = "logdiv";
var logInfo = false;
function log(message) {
	if ( logInfo ) {
		if ( logdiv == null ) {
			logdiv = document.getElementById(logdivID);
		}
		var ts = new Date();
		logdiv.innerHTML+=ts+":"+message+"<br>";
	}
}
function clearLog() {
	if ( logdiv == null ) {
		logdiv = document.getElementById("logdiv");
	}
	logdiv.innerHTML="<hr>";
}

