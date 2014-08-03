/*
 Description
   Encoding the request URL
 Params
   url
     the request URL
 Return
   the encoded URL
 */
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

/*
 Description:
 get the element which is on the same row with refresh icon to refresh
 the score input with the grade from the external scoring agent
 Params:
 refreshIconId
 The element id of refresh icon
 Return:
 the score input element id
 */
function getScoreTextBoxId(refreshIconId) {
	var possibleIds = ["Score","Score2","LetterScore","LetterScore2"];
	var pos = refreshIconId.lastIndexOf(":");
	var id="";
	for (i=0;i<possibleIds.length;i++) {
		id = refreshIconId.substr(0, pos + 1).concat(possibleIds[i]);
		var elementId = document.getElementById(id);
		if (elementId != null) {
			break;
		}
	}
	return id;
}

//update score textbox
function updateScoreTextbox(fieldToUpdate, newScore) {
	var doc = document;
	var inp = doc.getElementById(fieldToUpdate);
	inp.value = newScore;
}

//gets field to update element
function getFieldToUpdateElement(refreshIconId, elementId) {
	var pos = refreshIconId.lastIndexOf(":");
	return refreshIconId.substr(0, pos + 1).concat(elementId);
}

//remove the refresh link to update score from external scoring system
function removeLinkUpdate(refreshIconId){
	var d = document.getElementById(getFieldToUpdateElement(refreshIconId,"updateLink"));
	if(d!=null)
		discardElement(d);
}

/*
 Description
 Showing a popup to browse a URL for the external scoring agent
 Parameters
 urlPage
 a scoring agent URL to browse
 */
function showPopup(urlPage) {
	window
	.open(
			urlPage,
			'_blank',
	'width=800,height=600,top=20,left=100,menubar=yes,status=yes,location=no,toolbar=yes,scrollbars=yes,resizable=yes');
}


/*
 * Retrieve a student's grade from the external scoring service and update
 * the score input with the returned grade
 */
function refreshGrade(fieldToUpdate,refreshUrl) {
	jQuery.getJSON(encodeUrl(refreshUrl),function(data){
		if (typeof (data['score']) != 'undefined') {
			updateScoreTextbox(fieldToUpdate, data['score']);
		}
		
		if (data['message']) {
			alert(data['message']);
		}
	});
}


/*
 Description:
 Replace the contents of the score inputs with the scores retrieved from
 the external scoring system
 Params:
 scoreStream
   the scores returned by the external scoring system. Its format is <elementId1>,<score1>|<elementId2>,<score2>|...
 Return:
 None
 */
function refreshAllScores(refreshUrl, confirmationMsg) {

	jQuery.getJSON(encodeUrl(refreshUrl),function(data){
		if (data['message']) {
			alert(data['message']);
		}
		
		if (data.gradeData) {
			var a={};
			for (i=0; i< data.gradeData.length; i++) {
				a[data.gradeData[i].id] = data.gradeData[i].score;
			}

			var gradingTable = $('table.gradingTable');
			var tableRows = gradingTable.find('tr > td');
			tableRows.each(function (i, row) {

				var associatedId = $(this).find(".associatedId").text();
				if (associatedId != 'undefined' && associatedId != '') {
					var score = a[associatedId];
					if (typeof (score) != 'undefined') {

						// the next column contains the input text box to update
						var scoreTd = $(this).next();

						var scoreInput = scoreTd.find("input[id$='Score']");
						if (scoreInput.length <1) {
							// try to find the alternate rendered input
							scoreInput = scoreTd.find("input[id$='Score2']");
						}

						if (scoreInput.length > 0) {
							scoreInput.val(score);
						}
					}
				}
			});

			if(confirmationMsg) {
				alert(confirmationMsg);
			}
		}
	});
}


/*
Description:
 Move iframe to div element
 After that, remove div emlement
 Target: fixed memory leaks bug in firefox
 Source: http://social.msdn.microsoft.com/Forums/en-US/iewebdevelopment/thread/c76967f0-dcf8-47d0-8984-8fe1282a94f5
 Params: element
 Return:
 None
 */
function discardElement(element) {
	var doc = document;
	var garbageBin = doc.getElementById('LeakGarbageBin');
	if (!garbageBin) {
		garbageBin = doc.createElement('DIV');
		garbageBin.id = 'LeakGarbageBin';
		garbageBin.style.display = 'none';
		doc.body.appendChild(garbageBin);
	}

	// move the element to the garbage bin
	garbageBin.appendChild(element);
	garbageBin.innerHTML = '';
	removeChildSafe(garbageBin);
}
/*
Description:
 Remove element by tree
 Params: element
 Return:
 None
 */
function removeChildSafe(el) {
	//before deleting el, recursively delete all of its children.
	while(el.childNodes.length > 0) {      
		removeChildSafe(el.childNodes[el.childNodes.length-1]);
	}
	el.parentNode.removeChild(el);
}

//render the refresh grade link 
function renderLinkUpdate(scoringAgentIconId, event, msg){
	var doc = document;
	var updateLink = doc.getElementById(getFieldToUpdateElement(scoringAgentIconId,"updateLink"));

	if(updateLink == null){
		//remove tooltip    
		$("#portalMask", parent.document).trigger("unload").unbind().remove();

		var x = event.clientX + 200;
		var y = event.clientY + 65;  

		//wait after set tool tip then add event click anywhere
		setTimeout(function(){

			//show tool tip
			$("body", parent.document).append('<div id="portalMask" style="opacity:1;height:16px;border:1px solid;color:red;top:'+ 
					y +'px;left:'+ x +
					'px;">' + msg + '</div>');

			//bind click remove tool tip when click body iframe
			$(document.body).bind('click', removeToolTipClickIframe);

			//bind click remove tool tip when click body iframe
			$(parent.document.body).bind('click', removeToolTipClickParent);

		}, 50);
	} 
}

//function remove tool tip when user click anywhere in body iframe
var removeToolTipClickIframe = function(event){
	//remove tool tip
	$("#portalMask", parent.document).trigger("unload").unbind().remove();

	//unbind click body iframe
	$(document.body).unbind('click', removeToolTipClickIframe); 

	//unbind click parent 
	$(parent.document.body).unbind('click', removeToolTipClickParent);

	//hide selectsite when click body iframe
	$('div#selectSite div', parent.document).hide();
	$('#selectSite', parent.document).slideUp('fast'); // hide the box
}

//function remove tool tip when user click anywhere in parent
var removeToolTipClickParent = function(event){
	//remove tool tip
	$("#portalMask", parent.document).trigger("unload").unbind().remove();

	//click parent body
	$(parent.document.body).unbind('click', removeToolTipClickParent);

}
