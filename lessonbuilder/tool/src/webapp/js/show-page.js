var dropdownViaClick = false;

function msg(s) {
   return document.getElementById(s).innerHTML;
}

function checksize(oe) {
    var nsize = oe.height();
    var bsize = $('body').height();
    if ((nsize+50) > bsize) {
	$('body').height(nsize+50);
	setMainFrameHeight(window.name);
    }
}

function checkgroups(elt, groups) {
    var groupar = groups.split(",");
    elt.find('input').removeAttr('checked');
    for (i = 0; i < groupar.length; i++) {
	var inp = elt.find('input[value="' + groupar[i] + '"]');
	if (inp != null)
	    inp.attr('checked', 'checked');
    }
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
	
	$('#import-cc-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});
	
	$('#comments-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});
	
	$('#student-dialog').dialog({
		autoOpen: false,
		width: 600,
		modal: false,
		resizable: false,
		draggable: false
	});
	
	$("#select-resource-group").hide();

	$('.subpage-link').click(function(){
		var position =  $(this).position();
		$("#subpage-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#subpage-dialog').dialog('open');
		checksize($('#subpage-dialog'));
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
		checksize($('#edit-title-dialog'));
		return false;
	});

	$('#import-cc').click(function(){
		var position =  $(this).position();
		$("#import-cc-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#import-cc-dialog').dialog('open');
		checksize($('#import-cc-dialog'));
		return false;
	});

	$('#import-cc-submit').click(function() {
		$('#loading').show();
		return true;
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
		checksize($('#new-page-dialog'));
		return false;
	});

	$('#remove-page').click(function(){
		var position =  $(this).position();
		$("#remove-page-dialog").dialog("option", "position", [position.left, position.top]);
		// rsf puts the URL on the non-existent src attribute
		$('.hideOnDialog').hide();
		$('#remove-page-dialog').dialog('open');
		checksize($('#remove-page-dialog'));
		return false;
	});

	//	$('#remove-page-submit').click(function() {
	//		if ($("#remove-page-submit").attr("src") != null) {
	//		    window.location.href= $("#remove-page-submit").attr("src");
	//		    return false;
	//		}
	//		return true;
	//	});

	var outerWidth = $('#outer').width();
	if (outerWidth < 500) {
	    $("#subpage-dialog").dialog("option", "width", outerWidth-10);
	    $("#edit-item-dialog").dialog("option", "width", outerWidth-10);
	    $("#edit-multimedia-dialog").dialog("option", "width", outerWidth-10);
	    $("#add-multimedia-dialog").dialog("option", "width", outerWidth-10);
	    $("#edit-title-dialog").dialog("option", "width", outerWidth-10);
	    $("#import-cc-dialog").dialog("option", "width", outerWidth-10);
	    $("#new-page-dialog").dialog("option", "width", outerWidth-10);
	    $("#remove-page-dialog").dialog("option", "width", outerWidth-10);
	    $("#youtube-dialog").dialog("option", "width", outerWidth-10);
	    $("#movie-dialog").dialog("option", "width", outerWidth-10);
	    $("#subpage-link").dialog("option", "width", outerWidth-10);
	    $("#comments-dialog").dialog("option", "width", outerWidth-10);
	    $("#student-dialog").dialog("option", "width", outerWidth-10);
	}

	if (!(navigator.userAgent.indexOf("Firefox/2.") > 0)) {
	    $('.usebutton').button({text:true});
	} else {
	    // fake it; can't seem to get rid of underline though
	    $('.usebutton').css('border', '1px solid black').css('padding', '1px 4px').css('color', 'black');
	}

	$(".edit-youtube").click(function(){
		$("#editgroups-youtube").after($("#grouplist"));
		$("#grouplist").hide();
		$("#editgroups-youtube").hide();

		var row = $(this).parent().parent().parent();

		var groups = row.find(".item-groups").text();
		var grouplist = $("#grouplist");
		if ($('#grouplist input').size() > 0) {
		    $("#editgroups-youtube").show();
		    $("#grouplist").show();
		    if (groups != null) {
			checkgroups(grouplist, groups);
		    }
		}

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
		checksize($('#youtube-dialog'));
		$("#grouplist").hide();
		return false;
	});
	
	$("#editgroups-youtube").click(function(){
		$("#editgroups-youtube").hide();
		$("#grouplist").show();
	    });

	$('.edit-movie').click(function(){

                //var object = this.parentNode.parentNode.childNodes[3].childNodes[1];                                                                
		$("#expert-movie").hide();
		$("#expert-movie-toggle-div").show();
		$("#editgroups-movie").after($("#grouplist"));
		$("#grouplist").hide();
		$("#editgroups-movie").hide();

		var row = $(this).parent().parent().parent();

		var groups = row.find(".item-groups").text();
		var grouplist = $("#grouplist");
		if ($('#grouplist input').size() > 0) {
		    $("#editgroups-movie").show();
		    $("#grouplist").show();
		    if (groups != null) {
			checkgroups(grouplist, groups);
		    }
		}

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
		checksize($("#movie-dialog"));
		$("#grouplist").hide();
		return false;
	});
	
	$(".edit-comments").click(function(){
		$("#editgroups-comments").after($("#grouplist"));
		$("#grouplist").hide();
		$("#editgroups-comments").hide();

		var row = $(this).parent().parent().parent();

		var groups = row.find(".item-groups").text();
		var grouplist = $("#grouplist");
		if ($('#grouplist input').size() > 0) {
		    $("#editgroups-comments").show();
		    $("#grouplist").show();
		    if (groups != null) {
			checkgroups(grouplist, groups);
		    }
		}

		var itemId = row.find(".comments-id").text();
		
		$("#commentsEditId").val(itemId);
		
		var anon = row.find(".commentsAnon").text();
		if(anon == "true") {
			$("#comments-anonymous").attr("checked", true);
			$("#comments-anonymous").attr("defaultChecked", true)
		}else {
			$("#comments-anonymous").attr("checked", false);
		}
		var required = row.find(".commentsitem-required").text();
		if(required == "true") {
			$("#comments-required").attr("checked", true);
		}else {
			$("#comments-required").attr("checked", false);
		}
		var prerequisite = row.find(".commentsitem-prerequisite").text();
		if(prerequisite == "true") {
			$("#comments-prerequisite").attr("checked", true);
		}else {
			$("#comments-prerequisite").attr("checked", false);
		}

		
		var position = row.position();
		$("#comments-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#comments-dialog').dialog('open');
		checksize($("#comments-dialog"));
		$("#grouplist").hide();
		return false;
	});
	
	$("#editgroups-comments").click(function(){
		$("#editgroups-comments").hide();
		$("#grouplist").show();
	    });

	$(".edit-student").click(function(){
		$("#editgroups-student").after($("#grouplist"));
		$("#grouplist").hide();
		$("#editgroups-student").hide();

		var row = $(this).parent().parent().parent();

		var groups = row.find(".item-groups").text();
		var grouplist = $("#grouplist");
		if ($('#grouplist input').size() > 0) {
		    $("#editgroups-student").show();
		    $("#grouplist").show();
		    if (groups != null) {
			checkgroups(grouplist, groups);
		    }
		}

		var itemId = row.find(".student-id").text();
		
		$("#studentEditId").val(itemId);
		
		var anon = row.find(".studentAnon").text();
		if(anon == "true") {
			$("#student-anonymous").attr("checked", true);
			$("#student-anonymous").attr("defaultChecked", true)
		}else {
			$("#student-anonymous").attr("checked", false);
		}
		
		var comments = row.find(".studentComments").text();
		if(comments == "true") {
			$("#student-comments").attr("checked", true);
			$("#student-comments").attr("defaultChecked", true)
		}else {
			$("#student-comments").attr("checked", false);
		}
		
		var forcedAnon = row.find(".forcedAnon").text();
		if(forcedAnon == "true") {
			$("#student-comments-anon").attr("checked", true);
			$("#student-comments-anon").attr("defaultChecked", true)
		}else {
			$("#student-comments-anon").attr("checked", false);
		}
		
		var required = row.find(".studentitem-required").text();
		if(required == "true") {
			$("#student-required").attr("checked", true);
		}else {
			$("#student-required").attr("checked", false);
		}
		var prerequisite = row.find(".studentitem-prerequisite").text();
		if(prerequisite == "true") {
			$("#student-prerequisite").attr("checked", true);
		}else {
			$("#student-prerequisite").attr("checked", false);
		}

		if(!$("#student-comments").attr("checked")) {
			$("#student-comments-anon").attr("disabled", true).removeAttr("checked");
		}else {
			$("#student-comments-anon").removeAttr("disabled");
		}
		
		var position = row.position();
		$("#student-dialog").dialog("option", "position", [position.left, position.top]);
		$('.hideOnDialog').hide();
		$('#student-dialog').dialog('open');
		checksize($("#student-dialog"));
		$("#grouplist").hide();
		return false;
	});
	
	$("#editgroups-student").click(function(){
		$("#editgroups-students").hide();
		$("#grouplist").show();
	    });

	$("#student-comments").click(function() {
		if(!$("#student-comments").attr("checked")) {
			$("#student-comments-anon").attr("disabled", true).removeAttr("checked");
		}else {
			$("#student-comments-anon").removeAttr("disabled");
		}
	});

	$("#editgroups-movie").click(function(){
		$("#editgroups-movie").hide();
		$("#grouplist").show();
	    });

	function fixhref(href, pageitemid, resourcetype, website) {
	    href = href.replace(/&pageItemId=-?[0-9]*/, "&pageItemId=" + pageitemid);
	    href = href.replace(/&resourceType=[a-z]*/, "&resourceType=" + resourcetype);
	    href = href.replace(/&website=[a-z]*/, "&website=" + website);
	    return href;
	}

	$('#change-resource-movie').click(function(){
		closeMovieDialog();
		$("#mm-item-id").val($("#movieEditId").val());
		$("#mm-is-mm").val('true');
		var href=$("#mm-choose").attr("href");
		href=fixhref(href, $("#movieEditId").val(), "true", "false");
		$("#mm-choose").attr("href",href);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());

		var position =  $("#movie-dialog").dialog('option','position');
		$("#add-multimedia-dialog").dialog("option", "position", position);
		$(".mm-additional").show();
		$(".mm-additional-website").hide();
		$(".mm-url-section").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		checksize($("#add-multimedia-dialog"));
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$("#expert-movie-toggle").click(function(){
		$("#expert-movie-toggle-div").hide();
		$("#expert-movie").show();
		checksize($("#movie-dialog"));
		return false;
	});

	function fixitemshows(){
		var val = $(".format:checked").val();
		if (val == "window")
		    $("#edit-height").hide();
		else
		    $("#edit-height").show();
		if (val == "inline") {
		    $("#prereqstuff").hide();
		} else {
		    $("#prereqstuff").show();
		}
	}

	$(".edit-link").click(function(){
		$("#require-label2").hide();
		$("#item-required2").hide();
		$("#assignment-dropdown-selection").hide();
		$("#assignment-points").hide();
		$("#assignment-points").hide();
		$("#grouplist").hide();
		$("#editgroups").hide();
		$("#resource-group-inherited").hide();
		$("#assignment-points").val("");
		$("#assignment-points-label").hide();
		$("#change-assignment-p").hide();		
		$("#change-quiz-p").hide();		
		$("#change-forum-p").hide();		
		$("#change-resource-p").hide();	
		$("#change-blti-p").hide();
		$("#change-page-p").hide();	
		$("#edit-item-object-p").hide();	
		$("#edit-item-settings-p").hide();	
		$("#pagestuff").hide();
		$("#newwindowstuff").hide();
		$("#formatstuff").hide();
		$("#edit-height").hide();
		$("#editgroups").after($("#grouplist"));
		
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
		
                var samewindow = row.find(".item-samewindow").text();
                if (samewindow != '') {
                    if (samewindow == "true")
                        $("#item-newwindow").attr("checked", false);
                    else
                        $("#item-newwindow").attr("checked", true);
                    $("#newwindowstuff").show();
                }

		var format = row.find(".item-format").text();
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

		    var groups = row.find(".item-groups").text();
		    var grouplist = $("#grouplist");
		    if ($('#grouplist input').size() > 0) {
			$("#editgroups").show();
			$("#grouplist").show();
			if (groups != null) {
			    checkgroups(grouplist, groups);
			}
		    }

		} else if(type != '') {
			// Must be an assignment, assessment, forum
			
			var groups = row.find(".item-groups").text();
			var grouplist = $("#grouplist");
			if ($('#grouplist input').size() > 0) {
			    $("#editgroups").show();
			    $("#grouplist").show();
			    if (groups != null) {
				checkgroups(grouplist, groups);
			    }
			}

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

			}else if (type == 'b'){
				var height = row.find(".item-height").text();
				$("#edit-height-value").val(height);
				$("#edit-height").show();				
				$("#change-blti-p").show();
				$("#change-blti").attr("href", 
				      $("#change-blti").attr("href").replace("itemId=-1", "itemId=" + itemid));
				$("#require-label").text(msg("simplepage.require_submit_blti"));
				if (format == '')
				    format = 'page';
				$(".format").attr("checked", false);
				$("#format-" + format).attr("checked", true);
				$("#formatstuff").show();
				$("#edit-item-object-p").show();
				fixitemshows();

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
		    var groups = row.find(".item-groups").text();
		    var grouplist = $("#grouplist");
		    if (groups == "--inherited--")
			$("#resource-group-inherited").show();
		    else if ($('#grouplist input').size() > 0) {
			$("#editgroups").show();
			$("#grouplist").show();
			$("#select-resource-group").show();
			if (groups != null) {
			    checkgroups(grouplist, groups);
			}
		    }

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
		checksize($("#edit-item-dialog"));
		$("#grouplist").hide();
		return false;
	});

	$("#editgroups").click(function(){
		$("#editgroups").hide();
		$("#grouplist").show();
	    });

	$(".format").change(function(){
		fixitemshows();
	    });

	$('#change-resource').click(function(){
		closeEditItemDialog();
		$("#mm-item-id").val($("#item-id").val());
		$("#mm-is-mm").val('false');
		var href=$("#mm-choose").attr("href");
		href=fixhref(href, $("#item-id").val(), "false", "false");
		$("#mm-choose").attr("href",href);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		var position =  $("#edit-item-dialog").dialog('option','position');
		$("#add-multimedia-dialog").dialog("option", "position", position);
		$(".mm-additional").show();
		$(".mm-additional-website").hide();
		$(".mm-url-section").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		checksize($("#add-multimedia-dialog"));
		$('.edit-multimedia-input').blur();
		$('.edit-multimedia-input').blur();
		return false;
	});

	$(".add-multimedia").click(function(){
		$("#mm-item-id").val(-1);
		$("#mm-is-mm").val('true');
		$("#mm-is-website").val('false');
		var href=$("#mm-choose").attr("href");
		href=fixhref(href, "-1", "true", "false");
		$("#mm-choose").attr("href",href);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		var position =  $(this).position();
		$("#add-multimedia-dialog").dialog("option", "position", [position.left, position.top]);
		$(".mm-additional").show();
		$(".mm-additional-website").hide();
		$(".mm-url-section").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		checksize($("#add-multimedia-dialog"));
		$('.edit-multimedia-input').blur();
		$('.mm-additional-instructions').blur();
		return false;
	});

	$(".add-resource").click(function(){
		$("#mm-item-id").val(-1);
		$("#mm-is-mm").val('false');
		$("#mm-is-website").val('false');
		var href=$("#mm-choose").attr("href");
		href=fixhref(href,"-1","false","false");
		$("#mm-choose").attr("href",href);
		var position =  $(this).position();
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		$("#add-multimedia-dialog").dialog("option", "position", [position.left, position.top]);
		$(".mm-additional").hide();
		$(".mm-additional-website").hide();
		$(".mm-url-section").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		checksize($("#add-multimedia-dialog"));
		$('.edit-multimedia-input').blur();
		return false;
	});

	$(".add-website").click(function(){
		$("#mm-item-id").val(-1);
		$("#mm-is-mm").val('false');
		$("#mm-is-website").val('true');
		var href=$("#mm-choose").attr("href");
		href=fixhref(href, "-1","false","true");
		$("#mm-choose").attr("href",href);
		var position =  $(this).position();
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		$("#add-multimedia-dialog").dialog("option", "position", [position.left, position.top]);
		$(".mm-additional").hide();
		$(".mm-additional-website").show();
		$(".mm-url-section").hide();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		checksize($("#add-multimedia-dialog"));
		$('.edit-multimedia-input').blur();
		$('.mm-additional-website-instructions').blur();
		return false;
	});

	$(".multimedia-edit").click(function(){
		$("#expert-multimedia").hide();
		$("#expert-multimedia-toggle-div").show();
		$("#editgroups-mm").after($("#grouplist"));
		$("#grouplist").hide();
		$("#editgroups-mm").hide();

		var row = $(this).parent().parent().parent();

		var groups = row.find(".item-groups").text();
		var grouplist = $("#grouplist");
		if ($('#grouplist input').size() > 0) {
		    $("#editgroups-mm").show();
		    $("#grouplist").show();
		    if (groups != null) {
			checkgroups(grouplist, groups);
		    }
		}

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
		checksize($("#edit-multimedia-dialog"));
		$("#grouplist").hide();
		return false;
	});

	$("#editgroups-mm").click(function(){
		$("#editgroups-mm").hide();
		$("#grouplist").show();
	    });

	$("#expert-multimedia-toggle").click(function(){
		$("#expert-multimedia-toggle-div").hide();
		$("#expert-multimedia").show();
		checksize($("#edit-multimedia-dialog"));
		return false;
	});

	$('#change-resource-mm').click(function(){
		closeMultimediaEditDialog();
		$("#mm-item-id").val($("#multimedia-item-id").val());
		$("#mm-is-mm").val('true');
		var href=$("#mm-choose").attr("href");
		href=fixhref(href, $("#multimedia-item-id").val(), true, false);
		$("#add-multimedia-dialog").prev().children(".ui-dialog-title").text($(this).text());
		$("#mm-choose").attr("href",href);
		var position =  $("#edit-multimedia-dialog").dialog('option','position');
		$("#add-multimedia-dialog").dialog("option", "position", position);
		$(".mm-additional").show();
		$(".mm-additional-website").hide();
		$(".mm-url-section").show();
		$('.hideOnDialog').hide();
		$("#add-multimedia-dialog").dialog('open');
		checksize($("#add-multimedia-dialog"));
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
	
	var megaConfig = {	
			interval: 200,
			sensitivity: 7,
			over: addHighlight,
			timeout: 700,
			out: removeHighlight
	};
	
	$("li.dropdown").hoverIntent(megaConfig);
	$("li.dropdown").children("div").hide();
	$("li.dropdown").click(toggleDropdown);
	dropDownViaClick = false;
});

function closeSubpageDialog() {
	$("#subpage-dialog").dialog("close");
	$('#subpage-error-container').hide();
}

function closeEditItemDialog() {
	$("#edit-item-dialog").dialog("close");
	$('#edit-item-error-container').hide();
	$("#select-resource-group").hide();
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

function closeImportCcDialog() {
	$('#import-cc-dialog').dialog('close');
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

function closeCommentsDialog() {
	$('#comments-dialog').dialog('close');
}

function closeStudentDialog() {
	$('#student-dialog').dialog('close');
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

function checkCommentsForm() {
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

function addHighlight() {
	if(!$(this).children("div").is(":visible")) {
		reposition();
		$(this).children("div").show("slide", {direction: "up"}, 300);
	}
	//$(this).addClass("hovering");
}

function removeHighlight() {
	if($(this).children("div").is(":visible") && !dropdownViaClick)
		$(this).children("div").hide("slide", {direction: "up"}, 300);
	//$(this).removeClass("hovering");
}

function toggleDropdown() {
	if($(this).children("div").is(":visible")) {
		reposition();
		$(this).children("div").hide("slide", {direction: "up"}, 300);
		dropdownViaClick = false;
	}else {
		$(this).children("div").show("slide", {direction: "up"}, 300);
		dropdownViaClick = true;
	}
}

function reposition() {
	var dropX = $("li.dropdown").offset().left;
	var dropdown = $("li.dropdown").children("div");
	//alert("DropX: " + dropX);
	//alert("Width: " + window.innerWidth);
	//alert("Width2: " + dropdown.width());
	if(dropX + dropdown.width() > $(window).width()) {
		dropdown.css("left", ($(window).width() - dropdown.width() - dropX - 100) + "px");
	}
}