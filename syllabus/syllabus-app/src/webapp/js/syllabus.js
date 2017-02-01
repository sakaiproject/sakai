var dragStartIndex;
var editing = false;
var editorIndex = 1;
var bodiesLoaded = false;

//https://gist.github.com/Reinmar/b9df3f30a05786511a42
$.widget( 'ui.dialog', $.ui.dialog, {
    _allowInteraction: function( event ) {
        if ( this._super( event ) ) {
            return true;
        }

        // Address interaction issues with general iframes with the dialog.
        // Fixes errors thrown in IE when clicking CKEditor magicline's "Insert paragraph here" button.
        if ( event.target.ownerDocument != this.document[ 0 ] ) {
            return true;
        }

        // Address interaction issues with dialog window.
        if ( $( event.target ).closest( '.cke_dialog' ).length ) {
            return true;
        }

        // Address interaction issues with iframe based drop downs in IE.
        if ( $( event.target ).closest( '.cke' ).length ) {
            return true;
        }
    },

    // Uncomment this code when using jQuery UI 1.10.*.
    // Addresses http://dev.ckeditor.com/ticket/10269
    _moveToTop: function ( event, silent ) {
        if ( !event || !this.options.modal ) {
            this._super( event, silent );
        }
    }
} );

function setupAccordion(iframId, isInstructor, msgs, openDataId){
	var activeVar = false;
	if($( "#accordion .group" ).children("h3").size() <= 1){
		//since there is only 1 option, might was well keep it open instead of collapsed
		activeVar = 0;
		//only one to expand, might as well hide the expand all link:
		$("#expandLink").closest("li").hide();
	}
	$( "#accordion > span > div" ).accordion({ 
		header: "> div > h3",
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
	});
	if(isInstructor){
		$( "#accordion span" ).sortable({
			axis: "y",
			handle: "h3",
			start: function(event, ui){
			dragStartIndex = ui.item.index();
		},
		stop: function( event, ui ) {
			// IE doesn't register the blur when sorting
			// so trigger focusout handlers to remove .ui-state-focus
			ui.item.children( "h3" ).triggerHandler( "focusout" );

			//find how much this item was dragged:
			var dragEndIndex = ui.item.index();
			var moved = dragStartIndex - dragEndIndex;
			if(moved !== 0){
				//update the position:
				postAjax($(ui.item).children(":first").attr("syllabusItem"), {"move": moved}, msgs);
			}
		}
		});
	}
	if(activeVar === false && openDataId && openDataId !== ''){
		//instructor is working on this data item, keep it open and focused on when refreshing
		$( "#accordion div[syllabusItem=" + openDataId + "].group .ui-accordion-header").click().focus();
		
	}
	
	$( "#accordion div.group:first-child h3:first-child").focus();
	
	//set hover over text for collapse/expand arrow:
	$(".ui-accordion-header-icon").attr("title", msgs.clickToExpandAndCollapse);
}

// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	mySetMainFrameHeight(id, null);
}

function mySetMainFrameHeight(id, minHeight)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name !== "undefined" && id !== window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) !== 'undefined' || typeof(frame.contentWindow) !== 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc !== null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH !== null)
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
		if(minHeight && minHeight > newHeight){
			newHeight = minHeight;
		}
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}

function expandAccordion(iframId){
	$('#accordion > span > div.ui-accordion').each(function(){
		if(!$(this).find(".ui-accordion-content:first").is(":visible")){
			$(this).find(".ui-accordion-header:first").click();
		}
	});
	mySetMainFrameHeight(iframId);
	$("#collapseLink").show();
	$("#expandLink").hide();
}

function collapseAccordion(iframId){
	$('#accordion > span > div.ui-accordion').each(function(){
		if($(this).find(".ui-accordion-content:first").is(":visible")){
			$(this).find(".ui-accordion-header:first").click();
		}
	});
	mySetMainFrameHeight(iframId);
	$("#collapseLink").hide();
	$("#expandLink").show();
}

//http://vitalets.github.io/x-editable/
function setupEditable(msgs, iframId){
	//setup editables:
	$(".editItemTitle").editable({
		name: "title",
		type: 'text',
		title: msgs.syllabus_title,
		emptytext: msgs.clickToAddTitle,
		placement: "right",
		url: function(params) {
			postAjax($(this).parents('div.group').attr("syllabusItem"), params, msgs);
		},
		validate: function(value) {
		    if($.trim(value) === '') {
		        return msgs.required;
		    }
		}
	}).click(function (event){
		event.stopPropagation();
	});
	
	$(".startTimeInput").editable({
		name: "startDate",
		type: "combodate",
		title: msgs.startdatetitle,
		emptytext: msgs.clickToAddStartDate,
		combodate: {
			minYear: new Date().getFullYear() - 5,
			maxYear: new Date().getFullYear() + 5
		},
		format: 'YYYY-MM-DD HH:mm',
		viewformat: 'YYYY/MM/DD h:mm a',
		template: 'YYYY / MM / DD hh:mm a',
		placement: "left",
		url: function(params) {
			postAjax($(this).parents('div.group').attr("syllabusItem"), params, msgs);
		},
		validate: function(value){
			if(value && "" !== $.trim(value)){
				endTime = $(this).parents('div.group').find(".endTimeInput").text();
				if(endTime && "" !== $.trim(endTime) && $.trim(endTime) !== $.trim(msgs.clickToAddEndDate) && value > getDateTime(endTime)){
					return msgs.startBeforeEndDate + "  " + msgs.enddatetitle + ": " + endTime;
				}
			}
		}
	}).click(function (event){
		event.stopPropagation();
	});
	$(".endTimeInput").editable({
		name: "endDate",
		type: "combodate",
		title: msgs.enddatetitle,
		emptytext: msgs.clickToAddEndDate,
		combodate: {
			minYear: new Date().getFullYear() - 5,
			maxYear: new Date().getFullYear() + 5
		},
		format: 'YYYY-MM-DD HH:mm',
		viewformat: 'YYYY/MM/DD h:mm a',
		template: 'YYYY / MM / DD hh:mm a',
		placement: "left",
		url: function(params) {
			postAjax($(this).parents('div.group').attr("syllabusItem"), params, msgs);
		},
		validate: function(value){
			if(value && "" !== $.trim(value)){
				startTime = $(this).parents('div.group').find(".startTimeInput").text();
				if(startTime && "" !== $.trim(startTime) && $.trim(startTime) !== $.trim(msgs.clickToAddStartDate) && getDateTime(startTime) > value){
					return msgs.startBeforeEndDate + "  " + msgs.startdatetitle + ": " + startTime;
				}
			}
		}
	}).click(function (event){
		event.stopPropagation();
	});
	
	$(".bodyInput").editable({
		name: "body",
		type: 'textarea',
		title: msgs.syllabus_content,
		emptytext: msgs.clickToAddBody,
		onblur: "ignore",
		display: function(value, sourceData) {
			//when the display is first displayed (loading the page), the value is plain text (no html),
			//do so not change anything for the first load (use a boolean set after this jquery: bodiesLoaded)
			//after it's been loaded and the value changes, we need to update the html with the new value to display the
			//change properly to the user
			if(bodiesLoaded){
				//clear out old html
				$(this).html("");
				//set the new html
				$(this).append(value);
				//we want the user's to be able to click links in their body text without
				//having the edit popup
				$(this).find("a").click(function (event){
					event.stopPropagation();
				});
			}else{
				//there is a bug in x-editable that, when an element hasn't loaded (i.e. just a youtube video (nothing else))
				//and has a width of 0, will consider the element empty and put in the default empty text and not
				//display the real value.  Work around is to set the width/height to a number > 0
				//https://github.com/vitalets/x-editable/issues/344
				$(this).width(1);
				$(this).height(1);
			}
		},
		url: function(params) {
			postAjax($(this).parents('div.group').attr("syllabusItem"), params, msgs);
		}
	}).on( "tooltipopen", function( event, ui ) {
		var innerHtml = $(this).closest(".bodyInput").html();
		setTimeout(function(){
					//scroll user to top of the section so they will see the popup box if the
					//item is very large:
					var topOffset = $(".ui-tooltip").offset().top;
					if($("#" + iframId, window.parent.document).parents('html, body').size() > 0){
						//we are in the portal, grab parent
						$("#" + iframId, window.parent.document).parents('html, body').animate({scrollTop: topOffset});
					}else{
						//we are in tool view w/o portal, grab html/body
						$('html, body').animate({scrollTop: topOffset});
					}
					
					$("#textAreaWysiwyg").attr("id","textAreaWysiwyg" + editorIndex).focus();
					$("#textAreaWysiwyg" + editorIndex).val(innerHtml);
					$("#loading").hide();
					$(".editable-submit").click(function(event) {
						editorClick(event);
					});
					var toolTipLeft = $("#loading").closest(".ui-tooltip").position().left;
					var accordionLeft = $( "#accordion" ).position().left;
					var moveLeft = toolTipLeft - accordionLeft - 50;
					$("#loading").closest(".ui-tooltip").animate({left: "-=" + moveLeft, top: (topOffset)}, 10);
					var width = $( "#accordion" ).width() - 100;
					sakai.editor.launch("textAreaWysiwyg" + editorIndex, {}, width, 300);
					editorIndex++;
					$(".editable-buttons").css({"display":"block", "margin-left":"0px","margin-top":"7px"});
					//CKEditor needs to run some final functions when the checkbox is clicked:
					$("button.editable-submit").click(function(event){
						for ( instance in CKEDITOR.instances ){
					        CKEDITOR.instances[instance].updateElement();
					        CKEDITOR.instances[instance].destroy();
						}
					});
					mySetMainFrameHeight(iframId, 600);
			}, 1000);
	});
	//since we set the width/height to 1 to work around a bug, we need to set it back to auto:
	//https://github.com/vitalets/x-editable/issues/344
	$(".bodyInput").css('width', 'auto');
	$(".bodyInput").css('height', 'auto');
	
	//we want the user's to be able to click links in their body text without
	//having the edit popup
	$(".bodyInput a").click(function (event){
		event.stopPropagation();
	});
	bodiesLoaded = true;
}

function editorClick(event){
	$("#textAreaWysiwyg" + (editorIndex - 1)).val($(event.target).closest(".control-group").find("iframe").contents().find('body').html()).change();
}

function showMessage(message, success){
	var spanItem;
	if(success){
		spanItem = $("#successInfo");
	}else{
		spanItem = $("#warningInfo");
	}
	$(spanItem).html(message);
	var topPos = 0;
	//set topPos to top of the scroll bar for this iFrame
	try{
		topPos = $(window.parent.$("html,body")).scrollTop();
		if(topPos === 0){
			//try a different method to be sure
			topPos = $(window.parent.$("body")).scrollTop();
		}
	}catch(e){}
	//if this is an iframe, adjust top by the offset of the iframe
	try{
		if(topPos !== 0){
			topPos = topPos - $(window.parent.$("iframe")).offset().top; 
		}
	}catch(e){}
	//unless the users scrolls past the iframe, the position will be negative... just make it 0
	if(topPos < 0){
		topPos = 0;
	}
	$(spanItem).css("top", topPos); 
	$(spanItem).show();
	
	setTimeout(function(){$(spanItem).fadeOut();}, 4000);
}

function postAjax(id, params, msgs){
	var d = $.Deferred;
	$.ajax({
		type: 'POST',
		url: "/direct/syllabus/" + id + ".json",
		data: params,
		async:false,
		error: function error(data){
			showMessage(msgs.error, false);
			d().reject();
		},
		failure: function failure(data){
			showMessage(msgs.error, false);
			d().reject();
		},
		success: function success(data){
			showMessage(msgs.saved, true);
			d().resolve();
		}
	});
	return d().promise();
}

function getDateTime(dateTimeStr){
	var split = dateTimeStr.split(" ");
	if(split.length === 3){
		var dateStr = split[0];
		var timeStr = split[1] + " " + split[2];
		var date = new Date(Date.parse(dateStr));
		//TODO, internationalize the "P" match?
		var time = timeStr.match(/(\d+)(?::(\d\d))?\s*(P?)/);
		date.setHours( parseInt(time[1]) + (time[3] ? 12 : 0) );
		date.setMinutes( parseInt(time[2]) || 0 );
		return date;
	}else{
		return null;
	}
}

function setupToggleImages(action, imgClass, classOn, classOff, msgs){
	$("." + imgClass).click(function(event){
		var status;
		//custom action for calendar:
		if(action === "linkCalendar"){
			//make sure at least one date is set
			if(!$(this).hasClass(classOn)){
				//only warn user is they are turning on the calendar sync
				var startTime = $(this).parents('div.group').find(".startTimeInput").text();
				var endTime = $(this).parents('div.group').find(".endTimeInput").text();
				if((startTime === null || "" === $.trim(startTime) || $.trim(startTime) === $.trim(msgs.clickToAddStartDate))
						&& (endTime === null || "" === $.trim(endTime) || $.trim(endTime) === $.trim(msgs.clickToAddEndDate))){
					showMessage(msgs.calendarDatesNeeded, false);
					event.stopPropagation();
					return;
				}
			}
		}
		
		if($(this).hasClass(classOn)){
			//need to toggle to false
			status = false;
			$(this).hide();
			$(this).parents('div.group').find("." + classOff).fadeIn();
		}else{
			//need to toggle to true
			status = true;
			$(this).hide();
			$(this).parents('div.group').find("." + classOn).fadeIn();
		}
		//custom action for publish and unpublish:
		if(action === "publish"){
			//toggle the draft class
			if(status){
				$(this).parent().find(".editItemTitle").parent().removeClass("draft");
				$(this).parent().find( ".draftTitlePrefix " ).remove();
			}else{
				$(this).parent().find(".editItemTitle").parent().addClass("draft");
				var span = "<span class='draftTitlePrefix'>" + msgs.draftTitlePrefix + "</span>";
				$(this).parent().find(".editItemTitle").parent().prepend( span );
			}
		}
		
		var id = $(this).parents('div.group').attr("syllabusItem");
		params = {"toggle" : action,
					"status": status};
		postAjax(id, params, msgs);
		event.stopPropagation();
	});
}
function showConfirmDeleteAttachment(deleteButton, msgs, event){
	var title = $(deleteButton).parent().find(".attachment").html();
	$('<div></div>').appendTo('body')
		.html('<div><h6>' + msgs.confirmDelete + " '" + title + "'?</h6></div>")
		.dialog({
			position: { my: 'left center', at: 'right center', of: $(deleteButton)},
			modal: true, title: msgs.deleteAttachmentTitle, zIndex: 10000, autoOpen: true,
			width: 'auto', resizable: true,
			buttons: [
				{
					text: msgs.bar_delete,
					click: function () {
						var id = $(deleteButton).parents('div.group').attr("syllabusItem");
						params = {"deleteAttachment" : true,
									"attachmentId" : $(deleteButton).attr("attachmentId")};
						postAjax(id, params, msgs);
						if($("#successInfo").is(":visible")){
							$(deleteButton).parents('tr').remove();
						}
						$(this).dialog("close");
					}
				},
				{
					text: msgs.bar_cancel,
					click: function () {
						$(this).dialog("close");
					}
				}
			],
			close: function (event, ui) {
				$(this).remove();
			}
	});
	event.stopPropagation();
}

function showConfirmDelete(deleteButton, msgs, event){
	var title = $(deleteButton).parent().find(".editItemTitle").html();
	$('<div></div>').appendTo('body')
		.html('<div><h6>' + msgs.confirmDelete + " '" + title + "'?</h6></div>")
		.dialog({
			position: { my: 'left center', at: 'right center', of: $(deleteButton)},
			modal: true, title: msgs.deleteItemTitle, zIndex: 10000, autoOpen: true,
			width: 'auto', resizable: true,
			buttons: [
				{
					text: msgs.bar_delete,
					click: function () {
						var id = $(deleteButton).parents('div.group').attr("syllabusItem");
						params = {"delete" : true};
						postAjax(id, params, msgs);
						if($("#successInfo").is(":visible")){
							$(deleteButton).parents('div.group').remove();
						}
						$(this).dialog("close");
					}
				},
				{
					text: msgs.bar_cancel,
					click: function () {
						$(this).dialog("close");
					}
				}
			],
			close: function (event, ui) {
				$(this).remove();
				return false;
			}
	});
	event.stopPropagation();
}

function doAddItemButtonClick( msgs, published )
{
	var title = $( "#newTitle" ).val();
	if( !title || "" === title.trim() )
	{
		$( "#requiredTitle" ).show();
		setTimeout( function() { $( "#requiredTitle" ).fadeOut(); }, 5000 );
	}
	else
	{
		// Fetch the content from the new wysiwyg
		$("#newContentTextAreaWysiwyg").val($('#newContentDiv').find('iframe').contents().find('body').html()).change();

		// ID doesn't exist since we're adding a new one
		var id = "0";
		params = 
		{
			"add" : true,
			"title": title,
			"siteId": $("#siteId").val(),
			"published": published,
			"content": $("#newContentTextAreaWysiwyg").val()
		};

		postAjax( id, params, msgs );
		if( $( "#successInfo" ).is( ":visible" ) )
		{
			location.reload();
		}
	}
}

function showConfirmAdd(msgs, mainframeId){
	$('#container', this.top.document).append("<div></div>");
	$('<div></div>').appendTo('body')
		.html("<div><h6>" + msgs.syllabus_title + "</h6><input type='text' id='newTitle'/></div><div style='display:none' id='requiredTitle' class='warning'>" + msgs.required + "</div>" +
				"<h6>" + msgs.syllabus_content + "</h6><div class='bodyInput' id='newContentDiv'><textarea cols='120' id='newContentTextAreaWysiwyg'/></div>")
		.dialog({
			position: {
				my: 'center',
				at: 'center',
				of: window
			},
			modal: true,
			title: msgs.addItemTitle,
			zIndex: 11100,
			autoOpen: true,
			width: 'auto',
			height: 'auto',
			resizable: true,
			buttons: [
				{
					text: msgs.bar_publish,
					click: function() { doAddItemButtonClick( msgs, true ); $( this ).dialog( "close" ); }
				},
				{
					text: msgs.bar_new,
					click: function() { doAddItemButtonClick( msgs, false ); $( this ).dialog( "close" ); }
				},
				{
					text: msgs.bar_cancel,
					click: function () {
						$(this).dialog("close");
					}
				}
			],
			close: function (event, ui) {
				$(this).remove();
				return false;
			},
			open: function(event){
				if($("#accordion .group").children("h3").size() <= 1){
					// we have 1 or 0 items, so make sure the window size is large enough
					mySetMainFrameHeight(mainframeId);
				}
			}
		});
	sakai.editor.launch("newContentTextAreaWysiwyg", {}, 900, 300);
}

if(typeof String.prototype.trim !== 'function') {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, ''); 
	};
}
