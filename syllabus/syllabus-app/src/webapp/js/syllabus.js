let dragStartIndex;
let editing = false;
let editorIndex = 1;
let bodiesLoaded = false;

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
	const numItems = $( "#accordion .group" ).children("h3").size();
	if (numItems <= 1) {
		//only one to expand, might as well hide the expand all link:
		$("#expandLink").closest("li").hide();
	}
	$("#accordion > span > div").each(function(index) {
		$(this).accordion({
			header: "> div > h3",
			active: index === 0 ? 0 : false, // Active for the first panel, false for others
			autoHeight: false,
			collapsible: true,
			heightStyle: "content"
		});
	});
	if(isInstructor){
		let itemsOrder = [];

		function updatePositions() {
			itemsOrder = [];
			$('.reorder-element .group').each(function() {
				itemsOrder.push($(this).attr('syllabusitem'));
			});
		}
		updatePositions();
		let saveTimeout;
		$('#lastItemMoved').change(function() {
			// Clear the enqueued positions save
			clearTimeout(saveTimeout);
			const syllabusId = $(this).text();
			const syllabusItem = $('#' + syllabusId).parent().attr('syllabusitem');
			saveTimeout = setTimeout(function(){
				// Save the positions after 500ms with no more changes
				// First of all save the selected item
				const positionBefore = itemsOrder.indexOf(syllabusItem);
				let index = $('#' + syllabusId).parent().parent().index();
				const move = positionBefore - index;
				if (move !== 0) {
					postAjax(syllabusItem, {"move": move}, msgs);
					itemsOrder.move(positionBefore, index);
				}
				index = 0;
				// After this, check if other elements also should be saved (mulitple changes)
				$('.reorder-element .group').each(function() {
					const syllabusItem = $(this).attr('syllabusitem');
					const positionBefore = itemsOrder.indexOf(syllabusItem);
					const move = positionBefore - index;
					if (move === 0) {
						index++;
						return;
					}
					// This request should send all the array of positions in order to make it asynchronous (this logic will need a change)
					postAjax(syllabusItem, {"move": move}, msgs);
					itemsOrder.move(positionBefore, index);
					index++;
				});
			}, 500);
		});
	} else {
		$( "#accordion > span > div" ).sortable({disable: true});
	}
	Array.prototype.move = function(from,to){
		this.splice(to,0,this.splice(from,1)[0]);
		return this;
	};
	if (openDataId && openDataId !== ''){
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
	let spanItem;
	if(success){
		spanItem = $("#successInfo");
	}else{
		spanItem = $("#warningInfo");
	}
	$(spanItem).html(message);
	let topPos = 0;
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
	const d = $.Deferred;
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
			let successText = msgs.saved;
			if (params.delete !== null && params.delete) {
				successText = msgs.deleted;
			}
			showMessage(successText, true);
			d().resolve();
		}
	});
	return d().promise();
}

function getDateTime(dateTimeStr){
	const split = dateTimeStr.split(" ");
	if(split.length === 3){
		const dateStr = split[0];
		const timeStr = split[1] + " " + split[2];
		const date = new Date(Date.parse(dateStr));
		//TODO, internationalize the "P" match?
		const time = timeStr.match(/(\d+)(?::(\d\d))?\s*(P?)/);
		date.setHours( parseInt(time[1]) + (time[3] ? 12 : 0) );
		date.setMinutes( parseInt(time[2]) || 0 );
		return date;
	}else{
		return null;
	}
}

function setupToggleImages(action, imgClass, classOn, classOff, msgs){
	$("." + imgClass).click(function(event){
		let status;
		//custom action for calendar:
		if(action === "linkCalendar"){
			//make sure at least one date is set
			if(!$(this).hasClass(classOn)){
				//only warn user is they are turning on the calendar sync
				const startTime = $(this).parents('div.group').find(".startTimeInput").text();
				const endTime = $(this).parents('div.group').find(".endTimeInput").text();
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
				$(this).parent().find( ".draftTitlePrefix" ).remove();
			}else{
				$(this).parent().find(".editItemTitle").parent().addClass("draft");
				const span = "<span class='draftTitlePrefix'>" + msgs.draftTitlePrefix + "</span>";
				$(this).parent().find(".editItemTitle").parent().prepend( span );
			}
		}

		const id = $(this).parents('div.group').attr("syllabusItem");
		const params = {"toggle": action, "status": status};
		postAjax(id, params, msgs);
		event.stopPropagation();
	});
}
function showConfirmDeleteAttachment(deleteButton, msgs, event){
	const title = $(deleteButton).parent().find(".attachment").html();
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
						const id = $(deleteButton).parents('div.group').attr("syllabusItem");
						const params = {"deleteAttachment": true, "attachmentId": $(deleteButton).attr("attachmentId")};
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
	const title = $(deleteButton).parent().parent().find(".syllabusItemTitle").html();
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
						const id = $(deleteButton).parents('div.group').attr("syllabusItem");
						const params = {"delete": true};
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

function doAddDraftItem(msgs) {
	let title = $("#newTitle").val();
	if (!title || "" === title.trim()) {
		title = msgs.syllabus_title;
	}
	const id = "0";
	const params = {
		"add": true,
		"title": title,
		"siteId": $("#siteId").val(),
		"published": false,
		"content": msgs.syllabus_content
	};

	postAjax(id, params, msgs);
	if ($("#successInfo").is(":visible")) {
		location.reload();
		return true;
	}
}

function doAddItemButtonClick( msgs, published )
{
	const title = $("#newTitle").val();
	if( !title || "" === title.trim() )
	{
		$( "#requiredTitle" ).show();
		setTimeout( function() { $( "#requiredTitle" ).fadeOut(); }, 5000 );
	}
	else
	{
		// ID doesn't exist since we're adding a new one
		const id = "0";
		const params =
			{
				"add": true,
				"title": title,
				"siteId": $("#siteId").val(),
				"published": published,
				"content": CKEDITOR.instances.newContentTextAreaWysiwyg.getData()
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
