var editing = false;
var editorIndex = 1;

function setupAccordion(iframId){
	var activeVar = false;
	if($( "#accordion" ).children("h3").size() == 1){
		//since there is only 1 option, might was well keep it open instead of collapsed
		activeVar = 0;
	}
	$( "#accordion" ).accordion({ 
		active: activeVar,
		autoHeight: false,
		collapsible: true,
		heightStyle: "content",
		activate: function( event, ui ) {
			mySetMainFrameHeight(iframId);
			if(ui.newHeader[0]){
				if($("#" + iframId, window.parent.document).parents('html, body').size() > 0){
					//we are in the portal, grab parent
					$("#" + iframId, window.parent.document).parents('html, body').animate({scrollTop: $(ui.newHeader[0]).offset().top});
				}else{
					//we are in tool view w/o portal, grab html/body
					$('html, body').animate({scrollTop: $(ui.newHeader[0]).offset().top});
				}
			}
		}
	})
	$( "#accordion h3:first-child" ).focus();
}

// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}

function setupEditable(msgs, iframId){
//	//Edit button display/hide
//	$(".ui-accordion-header").focus(function() {
//		displayEditButtons(this);
//	}).blur(function() {
//		hideEditButtons(this);
//	}).hover(
//		function () {
//			//enter
//			displayEditButtons(this);
//		},
//		function () {
//			//leave
//			hideEditButtons(this);
//		}
//	);
//	//focus on the first child to recall "focus" to display edit button
//	$( "#accordion h3:first-child" ).focus();
	//setup editables:
	$(".editItemTitle").editable({
		type: 'text',
		title: msgs.syllabus_title,
		emptytext: msgs.clickToAddTitle,
		//set tooltip position (popup)
		position: { my: "right center", at: "left center" },
		show: { effect: "blind", duration: 100 }
	});
	$(".startTimeInput").editable({
		type: "combodate",
		title: msgs.startdatetitle,
		emptytext: msgs.clickToAddStartDate,
		combodate: {
			
		},
		format: 'YYYY-MM-DD HH:mm',
		viewformat: 'MM/DD/YYYY h:mm a',
		template: 'MM / DD / YYYY HH:mm a',
		//set tooltip position (popup)
		position: { my: "right center", at: "left center" },
		show: { effect: "blind", duration: 100 }
	});
	$(".endTimeInput").editable({
		type: "combodate",
		title: msgs.enddatetitle,
		emptytext: msgs.clickToAddEndDate,
		combodate: {
			
		},
		format: 'YYYY-MM-DD HH:mm',
		viewformat: 'MM/DD/YYYY h:mm a',
		template: 'MM / DD / YYYY HH:mm a',
		//set tooltip position (popup)
		position: { my: "right center", at: "left center" },
		show: { effect: "blind", duration: 100 }
	});
	$(".bodyInput").editable({
		savenochange: true,
		type: 'textarea',
		emptytext: 'click to add body text',
		onblur: "ignore",
		display: function(value, sourceData) {
			//clear out old html
			$(this).html("");
			//set the new html
			$(this).append(value);
		}
	}).on( "tooltipopen", function( event, ui ) {
		
		setTimeout(function(){
					$("#textAreaWysiwyg").attr("id","textAreaWysiwyg" + editorIndex);
					$("#loading").hide();
					$(".editable-submit").click(function(event) {
						editorClick(event);
					});
					var toolTipLeft = $("#loading").closest(".ui-tooltip").position().left;
					var accordionLeft = $( "#accordion" ).position().left;
					var moveLeft = toolTipLeft - accordionLeft - 50;
					$("#loading").closest(".ui-tooltip").animate({left: "-=" + moveLeft}, 10);
					var width = $( "#accordion" ).width() - 100;
					sakai.editor.launch("textAreaWysiwyg" + editorIndex, {}, width, 300);
					editorIndex++;
					mySetMainFrameHeight(iframId);
			}, 1000);
	});
	
}

function editorClick(event){
	$("#textAreaWysiwyg" + (editorIndex - 1)).val($(event.target).closest(".control-group").find("iframe").contents().find('body').html()).change();
}
