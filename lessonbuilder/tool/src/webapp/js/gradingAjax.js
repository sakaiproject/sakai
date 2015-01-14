var gradingReturnHook = null;
var gradingDoneHook = null;

function setGradingReturnHook(item) {
    gradingReturnHook = item;
}

function setGradingDoneHook(item) {
    gradingDoneHook = item;
}

function initGradingForm(idFieldId, pointsFieldId, jsIdFieldId, typeFieldId, csrfFieldId, elBinding) {
	//var idField = $(this).parents("div").children(".gradingForm").children(".idField");
	//var pointsField = $(this).parents("div").children(".gradingForm").children(".pointsField");
	
	var idField = document.getElementById(idFieldId);
	var pointsField = document.getElementById(pointsFieldId);
	var jsIdField = document.getElementById(jsIdFieldId);
	var typeField = document.getElementById(typeFieldId);
	var csrfField = document.getElementById(csrfFieldId);
	
	var ajaxUrl = idField.form.action;

	var callback = function(results) {
	      //var resultArray = RSF.decodeRSFStringArray(results.EL[elBinding]);
		
		var status = results.EL[elBinding][0];
		var jsId = results.EL[elBinding][1];
		var points = results.EL[elBinding][2];
		
		if(status === "success") {
			if (gradingReturnHook !== null)
			    window.location = gradingReturnHook;

			var jsObj = $("#" + jsId);
			jsObj.attr("src", getStrippedImgSrc(jsId) + "success.png");
			
			// Check if we are dealing with a comment, so that we can set all points values
			if(jsObj.parents(".commentDiv").length > 0) {
				var uuid = jsObj.parents(".commentDiv").find(".authorUUID").text();
				jsObj.parents(".replaceWithComments").find(".authorUUID").filter(":contains(" + uuid + ")").parents(".commentDiv").find(".pointsBox").val(points);
			}
		}else {
			$("#" + jsId).attr("src", getStrippedImgSrc(jsId) + "failed.png");
		}
		if (gradingDoneHook !== null) {
		    gradingDoneHook.call();
		}
	};

	// setup the function which initiates the AJAX request
	var updater = RSF.getAJAXUpdater([idField, pointsField, jsIdField, typeField, csrfField], ajaxUrl, [elBinding], callback);
	// setup the input field event to trigger the ajax request function
	pointsField.onchange = updater; // send request when field changes
	
	
}

function getStrippedImgSrc(id) {
	var imgsrc = $("#" + id).attr("src");
	imgsrc = imgsrc.replace("loading.gif", "");
	imgsrc = imgsrc.replace("success.png", "");
	imgsrc = imgsrc.replace("failed.png", "");
	imgsrc = imgsrc.replace("no-status.png", "");
	
	return imgsrc;
}