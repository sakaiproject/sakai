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
			handle: "h3 span.handleIcon",
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

function expandAccordion(iframId){
	$('#accordion > span > div.ui-accordion').each(function(){
		if(!$(this).find(".ui-accordion-content:first").is(":visible")){
			$(this).find(".ui-accordion-header:first").click();
		}
	});
	$("#collapseLink").show();
	$("#expandLink").hide();
}

function collapseAccordion(iframId){
	$('#accordion > span > div.ui-accordion').each(function(){
		if($(this).find(".ui-accordion-content:first").is(":visible")){
			$(this).find(".ui-accordion-header:first").click();
		}
	});
	$("#collapseLink").hide();
	$("#expandLink").show();
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
		.html('<div><div class="messageError">' + msgs.noUndoWarning + '</div><h6>' + msgs.confirmDelete + " '" + title + "'?</h6></div>")
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
		.html('<div><div class="messageError">' + msgs.noUndoWarning + '</div><h6>' + msgs.confirmDelete + " '" + title + "'?</h6></div>")
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
			return true;
		}
	}

	return false;
}

function showConfirmAdd(msgs, mainframeId){
	$('#container', this.top.document).append("<div></div>");
	$('<div></div>').appendTo('body')
		.html("<div><h6><span class='reqStar'>* </span>" + msgs.syllabus_title + "</h6><input type='text' id='newTitle'/></div><div style='display:none' id='requiredTitle' class='messageError'>" + msgs.required + "</div>" +
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
					click: function() { if (doAddItemButtonClick( msgs, true )) {$( this ).dialog( "close" );} }
				},
				{
					text: msgs.bar_new,
					click: function() { if (doAddItemButtonClick( msgs, false )) {$( this ).dialog( "close" );} }
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
	sakai.editor.launch("newContentTextAreaWysiwyg", {}, 900, 300);
}

if(typeof String.prototype.trim !== 'function') {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g, ''); 
	};
}
