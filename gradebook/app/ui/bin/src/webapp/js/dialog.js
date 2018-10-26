var dialogutil = dialogutil || {};

(function(jQuery, dialogutil) {

	dialogutil.openDialog = function(divId, frameId) {
		var vHeight = 300;
		
		

		$("#" + divId).dialog({
			resizable: false,
			autoOpen:false,
			draggable:false,
			modal: true,
			width: 600,
			height: vHeight,
			close: function(event, ui) {
				dialogutil.closeDialog(divId, frameId);
			},
			open: function() {
				$("#" + divId).delay(500, function(){dialogutil.turnOnPortalOverlay();});
				dialogutil.updateMainFrameHeight(window.name, frameId, vHeight);
				
			},
			zIndex: 10
		});

		$("#" + divId).dialog( "option", "position", "top");
		$("#" + divId).dialog("open");
	};

	/**
	 * Turns on the 2.x portals background overlay
	 */
	dialogutil.turnOnPortalOverlay = function() {
		$("body", parent.document).append('<div id="portalMask" style="position:fixed;width:100%;height:100%;"></div>');
		$("#" + iframeId, parent.document).parent().css("z-index", "9001").css("position", "relative").css("background", "#fff");
	};

	/**
	 * Turns off the 2.x portal background overlay
	 */
	dialogutil.turnOffPortalOverlay = function() {
		$("#portalMask", parent.document).trigger("unload").unbind().remove();
		$("#" + iframeId, parent.document).parent().css("z-index", "0");
	};

	dialogutil.closeDialogAndSubmitForm = function(divId, frameId, form){
		dialogutil.closeDialog(divId, frameId);
		form.submit();
	}
	
	dialogutil.closeDialog = function(divId, frameId) {
		$("#" + frameId).removeAttr("src");
		$("#" + divId).dialog('destroy');
		$("#" + divId).hide();
		dialogutil.turnOffPortalOverlay();
	};

	dialogutil.showDiv = function(divId) {
		$("#" + divId).show();
		$("#" + divId).delay(5000, function(){$("#" + divId).fadeOut(1000)});
	};

	$.fn.delay = function(time, func) {
		return this.each(function(){
			setTimeout(func,time);
		});
	};
	
	dialogutil.updateMainFrameHeight = function (theParentFrame, frameId, vHeight) {
		var frame = parent.document.getElementById(theParentFrame);
		if (frame)
		{
			var objToResize = (frame.style) ? frame.style : frame;
			
			// reset the scroll
	//		parent.window.scrollTo(0,0);

			// Mozilla way to detect height
			var localHeight = document.body.offsetHeight;

			// Internet Explorer way to detect height
			if (document.body.scrollHeight)
			{
				localHeight = document.body.scrollHeight;
			}
			
			localHeight +=10;

			var jqFrame = $("#" + frameId);
			
			var innerIframe = document.getElementById(jqFrame[0].id);
			var innerObjToResize = (innerIframe.style) ? innerIframe.style : innerIframe;

			innerObjToResize.height = vHeight + "px";
			
			localHeight += vHeight;
			if (frame)
			{
				objToResize.height = localHeight + "px";
			}
		}
	}

	dialogutil.replaceBodyOnLoad = function (newOnLoad, contextObject) {
		$("body", contextObject.document).attr("onload", newOnLoad);
	}


})(jQuery, dialogutil);
