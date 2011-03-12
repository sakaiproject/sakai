function msg(s) {
   return document.getElementById(s).innerHTML;
}

$(function() {

	$('#subpage-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});

	$('#edit-item-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});

	$('#edit-multimedia-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});

	$('#add-multimedia-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});

	// hardcode height so we have space for date picker
	$('#edit-title-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});
	
	$('#new-page-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});

	$('#remove-page-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});

	$('#youtube-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});
	
	$('#movie-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});
	
	$('.subpage-link').click(function(){
		var position =  $(this).position();
		$("#subpage-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#subpage-dialog').dialog('open');
		return false;
	});

	$('#edit-title').click(function(){
		var position =  $(this).position();
		$("#edit-title-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		if ($("#page-points").val() == '') {
		    $("#page-gradebook").attr("checked", false);
		    $("#page-points").attr("disabled", true);
		} else { 
		    $("#page-gradebook").attr("checked", true);
		}
		$('#edit-title-dialog').dialog('open');
		return false;
	});

	$('#releaseDiv').click(function(){
		$('#edit-title-dialog').height(550);
	    });

	$("#page-gradebook").click(function(){
		if ($("#page-gradebook").attr("checked")) {
		    if ($("#page-points").val() == '')
			$("#page-points").val('1');
		    $("#page-points").attr("disabled", false);
		} else {
		    $("#page-points").val('');
		    $("#page-points").attr("disabled", true);
		}
	    });

	$('#new-page').click(function(){
		var position =  $(this).position();
		$("#new-page-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#new-page-dialog').dialog('open');
		return false;
	});

	$('#remove-page').click(function(){
		var position =  $(this).position();
		$("#remove-page-dialog").dialog("option", "position", [position.left, position.top]);
		// rsf puts the URL on the non-existent src attribute
		$('.hideOnDialog').hide();
		$('#remove-page-dialog').dialog('open');
		return false;
	});

	$('#remove-page-submit').click(function() {
		window.location.href= $("#remove-page-submit").attr("src");
		return false;
	});

	var outerWidth = $('#outer').width();
	if (outerWidth < 500) {
	    $("#subpage-dialog").dialog("option", "width", outerWidth-10);
	    $("#edit-item-dialog").dialog("option", "width", outerWidth-10);
	    $("#edit-multimedia-dialog").dialog("option", "width", outerWidth-10);
	    $("#add-multimedia-dialog").dialog("option", "width", outerWidth-10);
	    $("#edit-title-dialog").dialog("option", "width", outerWidth-10);
	    $("#new-page-dialog").dialog("option", "width", outerWidth-10);
	    $("#remove-page-dialog").dialog("option", "width", outerWidth-10);
	    $("#youtube-dialog").dialog("option", "width", outerWidth-10);
	    $("#movie-dialog").dialog("option", "width", outerWidth-10);
	    $("#subpage-link").dialog("option", "width", outerWidth-10);
	}

	if (!(navigator.userAgent.indexOf("Firefox/2.") > 0)) {
	    $('.usebutton').button({text:true});
	} else {
	    // fake it; can't seem to get rid of underline though
	    $('.usebutton').css('border', '1px solid black').css('padding', '1px 4px').css('color', 'black');
	}

	$(".edit-youtube").click(function(){
		var row = $(this).parent().parent().parent();
		var itemid = row.find(".mm-item-id").text();
		
		$("#youtubeEditId").val(row.find(".youtube-id").text());
		$("#youtubeURL").val(row.find(".youtube-url").text());
		$("#youtubeHeight").val(row.find(".mm-height").text());
		$("#youtubeWidth").val(row.find(".mm-width").text());
		$("#description4").val(row.find(".description").text());
		var position =  row.position();
		$("#youtube-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#youtube-dialog').dialog('open');
		return false;
	});
	
	$('.edit-movie').click(function(){

                //var object = this.parentNode.parentNode.childNodes[3].childNodes[1];                                                                
		$("#expert-movie").hide();
		$("#expert-movie-toggle-div").show();
		
		var row = $(this).parent().parent().parent();
		var itemid = row.find(".mm-item-id").text();

		$("#movieEditId").val(row.find(".movie-id").text());
		$("#movie-height").val(row.find(".mm-height").text());
		$("#movie-width").val(row.find(".mm-width").text());
		$("#description3").val(row.find(".description").text());
		$("#mimetype4").val(row.find(".mm-type").text());
		var position =  row.position();
		$("#movie-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$("#movie-dialog").dialog('open');
		return false;
	});

	$('#change-resource-movie').click(function(){
		closeMovieDialog();
		$("#mm-item-id").val($("#movieEditId").val());
		$("#mm-is-mm").val('true');
		var href=$("#mm-choose").attr("href");
		var i=href.indexOf("&pageItemId=");
		href=href.substring(0,i) + "&pageItemId=" + $("#movieEditId").val() + "&resourceType=true";
		$("#mm-choose").attr("href",href);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());

		var position =  $("#movie-dialog").dialog('option','position');
		$("#add-multimedia-dialog").dialog("option", "position", position);
		$(".mm-additional").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$("#expert-movie-toggle").click(function(){
		$("#expert-movie-toggle-div").hide();
		$("#expert-movie").show();
		return false;
	});

	$(".edit-link").click(function(){
		$("#require-label2").hide();
		$("#item-required2").hide();
		$("#assignment-dropdown-selection").hide();
		$("#assignment-points").hide();
		$("#assignment-points").hide();
		$("#assignment-points").val("");
		$("#assignment-points-label").hide();
		$("#change-assignment-p").hide();		
		$("#change-quiz-p").hide();		
		$("#change-forum-p").hide();		
		$("#change-resource-p").hide();	
		$("#change-page-p").hide();	
		$("#edit-item-object-p").hide();	
		$("#edit-item-settings-p").hide();	
		$("#pagestuff").hide();
		
		var row = $(this).parent().parent().parent();
		var itemid = row.find(".current-item-id2").text();

		$("#name").val(row.find(".link-text").text());
		$("#description").val(row.find(".rowdescription").text());
				      
		var prereq = row.find(".prerequisite-info").text();

		if(prereq == "true") {
			$("#item-prerequisites").attr("checked", true);
			$("#item-prerequisites").attr("defaultChecked", true);
		}else {
			$("#item-prerequisites").attr("checked", false);
		}
		
		var req = row.find(".requirement-text").text();
		var type = row.find(".type").text();
		var editurl = row.find(".edit-url").text();
		var editsettingsurl = row.find(".edit-settings-url").text();
		
		if(type == 'page') {
                    $("#pagestuff").show();
		    var pagenext = row.find(".page-next").text();
		    if(pagenext == "true") {
			$("#item-next").attr("checked", true);
			$("#item-next").attr("defaultChecked", true);
		    }else {
			$("#item-next").attr("checked", false);
		    }

		    var pagebutton = row.find(".page-button").text();
		    if(pagebutton == "true") {
			$("#item-button").attr("checked", true);
			$("#item-button").attr("defaultChecked", true);
		    }else {
			$("#item-button").attr("checked", false);
		    }

		    $("#change-page-p").show();
		    $("#change-page").attr("href", 
			$("#change-page").attr("href").replace("itemId=-1", "itemId=" + itemid));

		} else if(type != '') {
			// Must be an assignment or assessment
			
			if(type == 6) {
				$("#change-quiz-p").show();
				$("#change-quiz").attr("href", 
				      $("#change-quiz").attr("href").replace("itemId=-1", "itemId=" + itemid));
				$("#require-label").text(msg("simplepage.require_submit_assessment"));
				$("#edit-item-object-p").show();
				$("#edit-item-object").attr("href", 
					$("#edit-item-object").attr("href").replace("source=SRC", "source="+escape(editurl)));
				$("#edit-item-text").text(msg("simplepage.edit_quiz"));
				$("#edit-item-settings-p").show();
				$("#edit-item-settings").attr("href", 
					$("#edit-item-settings").attr("href").replace("source=SRC", "source="+escape(editsettingsurl)));
				$("#edit-item-settings-text").text(msg("simplepage.edit_quiz_settings"));

			}else if (type == 8){
				$("#change-forum-p").show();
				$("#change-forum").attr("href", 
				      $("#change-forum").attr("href").replace("itemId=-1", "itemId=" + itemid));
				$("#require-label").text(msg("simplepage.require_submit_forum"));
				$("#edit-item-object-p").show();
				$("#edit-item-object").attr("href", 
					$("#edit-item-object").attr("href").replace("source=SRC", "source="+escape(editurl)));
				$("#edit-item-text").text(msg("simplepage.edit_topic"));

			}else {
				$("#change-assignment-p").show();
				$("#change-assignment").attr("href", 
				     $("#change-assignment").attr("href").replace("itemId=-1", "itemId=" + itemid));
				$("#require-label").text(msg("simplepage.require_submit_assignment"));
				$("#edit-item-object-p").show();
				$("#edit-item-object").attr("href", 
					$("#edit-item-object").attr("href").replace("source=SRC", "source="+escape(editurl)));
				$("#edit-item-text").text(msg("simplepage.edit_assignment"));

			}
			
			if(type == 3 || type == 6) {
				// Points or Assessment
				
				$("#require-label2").show();
				$("#require-label2").html(msg("simplepage.require_receive") + " ");
				if(type == 3) {
				    $("#assignment-points-label").text(" " + msg("simplepage.require_points_assignment"));
				}else if(type == 6) {
				    $("#assignment-points-label").text(" " + msg("simplepage.require_points_assessment"));
				}
				
				$("#item-required2").show();
				
				$("#assignment-points").show();
				$("#assignment-points-label").show();
				
				if(req == "false") {
					$("#item-required2").attr("checked", false);
				}else {
					// Need both of these statements, because of a stupid
					// little IE bug.
					$("#item-required2").attr("checked", true);
					$("#item-required2").attr("defaultChecked", true);
					
					$("#assignment-points").val(req);
				}
			}else if(type == 4) {
				// Pass / Fail
				$("#require-label2").show();
				$("#require-label2").html(msg("simplepage.require_pass_assignment"));
				$("#item-required2").show();
				
				if(req == "true") {
					// Need both of these statements, because of a stupid
					// little IE bug.
					$("#item-required2").attr("checked", true);
					$("#item-required2").attr("defaultChecked", true);
				}else {
					$("#item-required2").attr("checked", false);
				}
			}else if(type == 2) {
				// Letter Grade
				
				$("#require-label2").show();
				$("#require-label2").text(msg("simplepage.require_atleast"));
				$("#item-required2").show();
				$("#assignment-dropdown-selection").show();
				
				if(req == "false") {
					$("#item-required2").attr("checked", false);
				}else {
					// Need both of these statements, because of a stupid
					// little IE bug.
					$("#item-required2").attr("checked", true);
					$("#item-required2").attr("defaultChecked", true);
					
					$("#assignment-dropdown-selection").val(req);
				}
			}else if(type == 1) {
				// Ungraded
				// Nothing more that we need to do
			}else if(type == 5) {
				// Checkmark
				$("#require-label2").show();
				$("#require-label2").text(msg("simplepage.require_checkmark"));
				$("#item-required2").show();
				
				if(req == "true") {
					// Need both of these statements, because of a stupid
					// little IE bug.
					$("#item-required2").attr("checked", true);
					$("#item-required2").attr("defaultChecked", true);
				}else {
					$("#item-required2").attr("checked", false);
				}
			}
		} else {
		    // resource
		    $("#change-resource-p").show();
		    $("#change-resource").attr("href", 
		        $("#change-resource").attr("href").replace("pageItemId=-1", "pageItemId=" + itemid));
		}

		if(row.find(".status-image").attr("src") == undefined) {
		    $("#item-required").attr("checked", false);
		} else if (row.find(".status-image").attr("src").indexOf("not-required.png") > -1) {
			$("#item-required").attr("checked", false);
		} else {
			// Need both of these statements, because of a stupid
			// little IE bug.
			$("#item-required").attr("checked", true);
			$("#item-required").attr("defaultChecked", true);
		}

		setUpRequirements();
	        $("#item-id").val(row.find(".current-item-id2").text());
		$("#edit-item-error-container").hide();
		var position =  $(this).position();
		$("#edit-item-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$("#edit-item-dialog").dialog('open');
		return false;
	});

	$('#change-resource').click(function(){
		closeEditItemDialog();
		$("#mm-item-id").val($("#item-id").val());
		$("#mm-is-mm").val('false');
		var href=$("#mm-choose").attr("href");
		var i=href.indexOf("&pageItemId=");
		href=href.substring(0,i) + "&pageItemId=" + $("#item-id").val() + "&resourceType=false";
		$("#mm-choose").attr("href",href);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		var position =  $("#edit-item-dialog").dialog('option','position');
		$("#add-multimedia-dialog").dialog("option", "position", position);
		$(".mm-additional").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$(".add-multimedia").click(function(){
		$("#mm-item-id").val(-1);
		$("#mm-is-mm").val('true');
		var href=$("#mm-choose").attr("href");
		var i=href.indexOf("&pageItemId=");
		href=href.substring(0,i) + "&pageItemId=-1&resourceType=true";
		$("#mm-choose").attr("href",href);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		var position =  $(this).position();
		$("#add-multimedia-dialog").dialog("option", "position", [position.left, position.top]);
		$(".mm-additional").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$(".add-resource").click(function(){
		$("#mm-item-id").val(-1);
		$("#mm-is-mm").val('false');
		var href=$("#mm-choose").attr("href");
		var i=href.indexOf("&pageItemId=");
		href=href.substring(0,i) + "&pageItemId=-1&resourceType=false";
		$("#mm-choose").attr("href",href);
		var position =  $(this).position();
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		$("#add-multimedia-dialog").dialog("option", "position", [position.left, position.top]);
		$(".mm-additional").hide();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$(".multimedia-edit").click(function(){
		$("#expert-multimedia").hide();
		$("#expert-multimedia-toggle-div").show();

		var row = $(this).parent().parent().parent();

		$("#height").val(row.find(".mm-height").text());
		$("#width").val(row.find(".mm-width").text());
		$("#description2").val(row.find(".description").text());
		$("#mimetype").val(row.find(".mm-type").text());
		if (row.find(".multimedia").get(0).nodeName.toLowerCase() == "img") {
		    $("#alt").val(row.find(".multimedia").attr("alt"));
		    $("#alt").parent().show();
		    $("#tagnameused").html(msg("simplepage.tag_img"));
		    $("#iframe-note").hide();
	        } else {
		    $("#alt").parent().hide();
		    $("#tagnameused").html(msg("simplepage.tag_iframe"));
		    $("#iframe-note").show();
		}
		$("#change-resource-mm").attr("href", 
		     $("#change-resource-mm").attr("href").replace("pageItemId=-1", 
			   "pageItemId=" + row.find(".mm-itemid").text()));
		$("#multimedia-item-id").val(row.find(".mm-itemid").text());
		var position =  row.position();
		$("#edit-multimedia-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$("#edit-multimedia-dialog").dialog('open');
		return false;
	});

	$("#expert-multimedia-toggle").click(function(){
		$("#expert-multimedia-toggle-div").hide();
		$("#expert-multimedia").show();
		return false;
	});

	$('#change-resource-mm').click(function(){
		closeMultimediaEditDialog();
		$("#mm-item-id").val($("#multimedia-item-id").val());
		$("#mm-is-mm").val('true');
		var href=$("#mm-choose").attr("href");
		var i=href.indexOf("&pageItemId=");
		href=href.substring(0,i) + "&pageItemId=" + $("#multimedia-item-id").val() + "&resourceType=true";
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		$("#mm-choose").attr("href",href);
		var position =  $("#edit-multimedia-dialog").dialog('option','position');
		$("#add-multimedia-dialog").dialog("option", "position", position);
		$(".mm-additional").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$("#item-required").click(function(){
		setUpRequirements();
	});
	
	$("#item-required2").click(function(){
		setUpRequirements();
	});
	
	 $('body').bind('dialogclose', function(event) {
	     $('.hideOnDialog').show();
	 });
	
	$('#edit-title-error-container').hide();
	$('#new-page-error-container').hide();
	$('#edit-item-error-container').hide();
	$('#subpage-error-container').hide();
	$("#require-label2").hide();
	$("#item-required2").hide();
	$("#assignment-dropdown-selection").hide();
	$("#edit-youtube-error-container").hide();
	$("#messages").hide();
});

function closeSubpageDialog() {
	$("#subpage-dialog").dialog("close");
	$('#subpage-error-container').hide();
}

function closeEditItemDialog() {
	$("#edit-item-dialog").dialog("close");
	$('#edit-item-error-container').hide();
}

function closeMultimediaEditDialog() {
	$("#edit-multimedia-dialog").dialog("close");
}

function closeAddMultimediaDialog() {
	$("#add-multimedia-dialog").dialog("close");
}

function closeEditTitleDialog() {
	$('#edit-title-dialog').dialog('close');
	$('#edit-title-error-container').hide();
}

function closeNewPageDialog() {
	$('#new-page-dialog').dialog('close');
	$('#new-page-error-container').hide();
}

function closeRemovePageDialog() {
	$('#remove-page-dialog').dialog('close');
}

function closeYoutubeDialog() {
	$('#edit-youtube-error-container').hide();
	$('#youtube-dialog').dialog('close');
}

function closeMovieDialog() {
	$('#movie-dialog').dialog('close');
}

function checkEditTitleForm() {
	if($('#pageTitle').val() == '') {
		$('#edit-title-error').text(msg("simplepage.title_notblank"));
		$('#edit-title-error-container').show();
		return false;
	}else {
		$('#edit-title-error-container').hide();
		return true;
	}
}

// these tests assume \d finds all digits. This may not be true for non-Western charsets
function checkNewPageForm() {
    if($('#newPage').val() == '') {
        $('#new-page-error').text(msg("simplepage.title_notblank"));
        $('#new-page-error-container').show();
        return false;
    }
    if($('#new-page-number').val() != '') {
        if(! $('#new-page-number').val().match('^\\d*$')) {
            $('#new-page-error').text(msg("simplepage.number_pages_not_number"));
            $('#new-page-error-container').show();
            return false;
        }
        if (!$('#newPage').val().match('\\d')) {
            $('#new-page-error').text(msg("simplepage.title_no_number"));
            $('#new-page-error-container').show();
            return false;
        }
    }
    $('#new-page-error-container').hide();
    return true;

}

function checkYoutubeForm() {
	if($('#youtubeURL').val().contains('youtube.com')) {
		return true;
	}else {
		$('#edit-youtube-error').val(msg("simplepage.must_be_youtube"));
		$('#edit-youtube-error-container').show();
		return false;
	}
}

function checkMovieForm() {
	return true;
}

function checkEditItemForm() {
	if($('#name').val() == '') {
		$('#edit-item-error').text(msg("simplepage.item_notblank"));
		$('#edit-item-error-container').show();
		return false;
	}else {
		$('#edit-item-error-container').hide();
		return true;
	}
}

function checkSubpageForm() {
	if($('#subpage-title').val() == '') {
		$('#subpage-error').text(msg("simplepage.page_notblank"));
		$('#subpage-error-container').show();
		return false;
	}else {
		$('#subpage-error-container').hide();
		return true;
	}
}

function disableSecondaryRequirements() {
	$("item-required2").attr("disabled", true);
	$("assignment-dropdown-selection").attr("disabled", true);
	$("assignment-points").attr("disabled", true);
}

function disableSecondarySubRequirements() {
	$("assignment-dropdown-selection").attr("disabled", true);
	$("assignment-points").attr("disabled", true);
}

function setUpRequirements() {
	if($("#item-required").attr("checked")) {
		$("#item-required2").attr("disabled", false);
		
		if($("#item-required2").attr("checked")) {
			$("#assignment-dropdown-selection").attr("disabled", false);
			$("#assignment-points").attr("disabled", false);
		}else {
			$("#assignment-dropdown-selection").attr("disabled", true);
			$("#assignment-points").attr("disabled", true);
		}
	}else {
		$("#item-required2").attr("disabled", true);
		$("#assignment-dropdown-selection").attr("disabled", true);
		$("#assignment-points").attr("disabled", true);
	}
}

/**
 * Workaround in ShowPage.html to change which submit is triggered
 * when you press the Enter key.
 */
$(function() {
	$(".edit-multimedia-input").keypress(function (e) { 
	    if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {  
	        $('#edit-multimedia-item').click();  
	        return false;  
	    } else {  
	        return true;  
	    }  
	});  
	
	$(".edit-form-input").keypress(function (e) {  
		if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {  
	        $('#edit-item').click();  
	        return false;  
	    } else {  
	        return true;  
	    }  
	});
	
	$(".edit-youtube-input").keypress(function (e) {  
		if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {  
	        $('#update-youtube').click();  
	        return false;  
	    } else {  
	        return true;  
	    }  
	});
	
	$(".edit-movie-input").keypress(function (e) {  
		if ((e.which && e.which == 13) || (e.keyCode && e.keyCode == 13)) {  
	        $('#update-movie').click();  
	        return false;  
	    } else {  
	        return true;  
	    }  
	});
});