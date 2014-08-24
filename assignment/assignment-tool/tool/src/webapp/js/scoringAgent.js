function openScoringAgent(url) {
	window.open(url,'_blank',
     'width=800,height=600,top=20,left=100,menubar=yes,status=yes,location=no,toolbar=yes,scrollbars=yes,resizable=yes');
}

/*
 * Retrieve a student's grade from the external scoring service and update
 * the score input with the returned grade
 */
function refreshScore(refreshUrl) {
	jQuery.getJSON(encodeUrl(refreshUrl),function(data){
		if (data['score']) {
			var newScore = data['score'];
			var inp = document.getElementById("grade");
			if(newScore != '' && newScore > 0) {
				newScore = Math.round(newScore*10)/10;
			}
			
			inp.value = newScore;
			$('#scoringAgent-refresh-reminder').hide();
		}
	});
}

function displayRefreshReminder() {
	$('#scoringAgent-refresh-reminder').show();
}

function encodeUrl(url) {

	if (url.indexOf("?") > 0) {
		encodedParams = "?";
		parts = url.split("?");
		params = parts[1].split("&");
		for (i = 0; i < params.length; i++) {
			if (i > 0) {
				encodedParams += "&";
			}
			if (params[i].indexOf("=") > 0) {
				// Avoid null values
				p = params[i].split("=");
				encodedParams += (p[0] + "=" + escape(encodeURI(p[1])));
			} else {
				encodedParams += params[i];
			}
		}
		url = parts[0] + encodedParams;
	}
	return url;
}
