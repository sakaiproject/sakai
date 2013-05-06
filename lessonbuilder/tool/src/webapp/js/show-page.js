var dropdownViaClick = false;
var lessonBuilderAnimationLocked = false;
var oldloc;
var requirementType = 0;
var importccactive = false;

// in case user includes the URL of a site that replaces top,
// give them a way out. Handler is set up in the html file.
// Unload it once the page is fully loaded.

$(window).load(function () {
	window.onbeforeunload = null;
});

function msg(s) {
    var m = document.getElementById(s);
    if (m == null) {
       return s;
    }  else 
       return m.innerHTML;;
}

function checksize(oe) {
	var nsize = oe.height() + oe.parent().position().top;
	var bsize = $("#outer").height();
	if ((nsize) > bsize) {
		$("#outer").height(nsize);
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

var blankRubricTemplate, blankRubricRow;

$(function() {
	// This is called in comments.js as well, however this may be faster.
	//if(sakai.editor.editors.ckeditor==undefined) {
//		$(".evolved-box :not(textarea)").hide();
//	}else {
		//$(".evolved-box").hide();
	//}

	// We don't need to run all of this javascript if the user isn't an admin
	if($("#subpage-dialog").length > 0) {
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
	
		$('#export-cc-dialog').dialog({
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
		
		$('#question-dialog').dialog({
			autoOpen: false,
			width: 600,
			modal: false,
			resizable: false,
			draggable: false
		});
		
		/* RU Rubrics ********************************************* */
		$("#rubric-title").append($("#peer-eval-title-cloneable input"));
		blankRubricTemplate=$(".peer-eval-create-form").html();
		blankRubricRow=$("#peer-eval-input-cloneable").html();
		
		$peerButtons=$("#peer-review-buttonset").clone();
		$("#peer-review-buttonset").remove();
				
		$('#peer-eval-create-dialog').dialog({
			autoOpen: false,
			width: 600,
			height: 400,
			modal: false,
			resizable: false,
			draggable: false
		});
		
		$('#peer-eval-create-dialog').parent().append($peerButtons);
		
		$("#peer-eval-input-cloneable").html("").remove();
		$(".peer-eval-create-form").submit(function(e){e.preventDefault();});
		
		$("#select-resource-group").hide();

		$('.subpage-link').click(function(){
			closeDropdown();
			var position =  $(this).position();
			$("#subpage-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(this);
			$('#subpage-dialog').dialog('open');
			checksize($('#subpage-dialog'));
			return false;
		});

		$('#edit-title').click(function(){
			closeDropdown();
			$('#edit-title-error-container').hide();
			var position =  $(this).position();
			$("#edit-title-dialog").dialog("option", "position", [position.left, position.top]);
			if ($("#page-points").val() == '') {
				$("#page-gradebook").attr("checked", false);
				$("#page-points").attr("disabled", true);
			} else { 
				$("#page-gradebook").attr("checked", true);
			}
			oldloc = $(".dropdown a");
			$('#edit-title-dialog').dialog('open');
			checksize($('#edit-title-dialog'));
			return false;
		});

		$("#releaseDiv input").change(function(){
			$("#page-releasedate").attr('checked', 'checked');
		    });

		$('#import-cc').click(function(){
			closeDropdown();
			var position =  $(this).position();
			$("#import-cc-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(".dropdown a");
			importccactive = true;
			$('#import-cc-dialog').dialog('open');
			checksize($('#import-cc-dialog'));
			return false;
		});
		
		$('#export-cc').click(function(){
			closeDropdown();
			var position =  $(this).position();
			$("#export-cc-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(".dropdown a");
			$('#export-cc-dialog').dialog('open');
			checksize($('#export-cc-dialog'));
			return false;
		});

		$('#export-cc-submit').click(function(){
			// jquery click doesn't actually click, so get the js object and do a native click call
                        if ($('#export-cc-v11').attr('checked') == 'checked') {
                            $("#export-cc-link").attr('href', $("#export-cc-link").attr('href').replace(/version=[0-9.]*/, "version=1.1"));
                        } else {
                            $("#export-cc-link").attr('href', $("#export-cc-link").attr('href').replace(/version=[0-9.]*/, "version=1.2"));
                        }
			$("#export-cc-link").get(0).click();
			closeExportCcDialog();
			return false;
		    });

		$('#import-cc-submit').click(function() {
			// prevent double clicks
			if (!importccactive)
			    return false;
			importccactive = false;
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
			closeDropdown();
			var position =  $(this).position();
			$("#new-page-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(".dropdown a");
			$('#new-page-dialog').dialog('open');
			checksize($('#new-page-dialog'));
			return false;
		});

		$('#remove-page').click(function(){
			closeDropdown();
			var position =  $(this).position();
			$("#remove-page-dialog").dialog("option", "position", [position.left, position.top]);
			// rsf puts the URL on the non-existent src attribute
			oldloc = $(".dropdown a");
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
			$("#export-cc-dialog").dialog("option", "width", outerWidth-10);
			$("#new-page-dialog").dialog("option", "width", outerWidth-10);
			$("#remove-page-dialog").dialog("option", "width", outerWidth-10);
			$("#youtube-dialog").dialog("option", "width", outerWidth-10);
			$("#movie-dialog").dialog("option", "width", outerWidth-10);
			$("#subpage-link").dialog("option", "width", outerWidth-10);
			$("#comments-dialog").dialog("option", "width", outerWidth-10);
			$("#student-dialog").dialog("option", "width", outerWidth-10);
			$("#question-dialog").dialog("option", "width", outerWidth-10);
		}
		
		$(".edit-youtube").click(function(){
			closeDropdown();
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
			oldloc = $(this);
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
			closeDropdown();
	                //var object = this.parentNode.parentNode.childNodes[3].childNodes[1];                                                                
			$("#expert-movie").hide();
			$("#expert-movie-toggle-div").show();
			$("#editgroups-movie").after($("#grouplist"));
			$("#grouplist").hide();
			$("#editgroups-movie").hide();

			var row = $(this).parent().parent().parent();
			
			var findObject = row.find('object').find('object');
			row.find(".path-url").attr("href", findObject.attr("data"));
			$("#movie-path").html(row.find(".item-path").html());

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
			oldloc = $(this);
			$("#movie-dialog").dialog('open');
			checksize($("#movie-dialog"));
			$("#grouplist").hide();
			return false;
		});
		
		$(".edit-comments").click(function(){
			closeDropdown();
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
			
			var grade = row.find(".commentsGrade").text();
			if(grade == "true") {
				$("#comments-graded").attr("checked", true);
				$("#comments-graded").attr("defaultChecked", true)
			}else {
				$("#comments-graded").attr("checked", false);
			}
			
			$("#comments-max").val(row.find(".commentsMaxPoints").text());
			if($("#comments-max").val() == "null") {
				$("#comments-max").val("");
			}
			
			var position = row.position();
			$("#comments-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(this);
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
			closeDropdown();
			$("#editgroups-student").after($("#grouplist"));
			$("#grouplist").hide();
			$("#editgroups-student").hide();
			$("#student-group-show").hide();

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

			var groups = row.find(".student-owner-groups").text();
			var grouplist = $("#student-grouplist");
			if ($('#student-grouplist input').size() > 0) {
			    $("#student-grouplist").show();
			    if (groups != null) {
				checkgroups(grouplist, groups);
			    }
			}
			var groupOwned = row.find(".student-group-owned").text();
			$("#student-group-owned").attr("checked",(groupOwned == "true"));
			if (groupOwned == "true")
			    $("#student-group-show").show();

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
			
			/* RU Rubrics ********************************************* */
			//Because all Student Content boxes use the same dialog, the settings are applied when Edit is clicked. 
			//The following decides whether to have the box already checked when it is first opened.
			var peerReview = row.find(".peer-eval").text();
			if(peerReview == "true") {
				$("#peer-eval-check").attr("checked", true);
				$("#peer-eval-check").attr("defaultChecked", true);
			}else {
				$("#peer-eval-check").attr("checked", false);
			}
			
			$("#available-rubrics-container").html("");//Add sample rubric			
			var peerEvalSample = new Object();
			peerEvalSample.rows=new Array();
			row.find(".peer-eval-sample-data").each(function(){
				var categoryId = $(".peer-eval-sample-id" , $(this)).text();
				var categoryText = $(".peer-eval-sample-text" , $(this)).text();
				peerEvalSample.rows.push({"id":categoryId , "text":categoryText});
			});
			peerEvalSample.title= row.find(".peer-eval-sample-title").text();
			buildExistingRubrics(peerEvalSample);
			console.log(peerEvalSample);
			
			var rubric = new Object();
			rubric.rows=new Array();
			row.find(".peer-eval-row").each(function(){
				var categoryId = $(".peerReviewId" , $(this)).text();
				var categoryText = $(".peerReviewText" , $(this)).text();
				rubric.rows.push({"id":categoryId , "text":categoryText});
			});
			rubric.title= row.find(".peer-eval-title").text();
			buildExistingRubrics(rubric);
			//console.log(rubric);
			
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
				$("#student-comments-graded").attr("disabled", true).removeAttr("checked");
				$("#student-comments-max").attr("disabled", true).val("");
			}else {
				$("#student-comments-anon").removeAttr("disabled");
				$("#student-comments-graded").removeAttr("disabled");
				$("#student-comments-max").removeAttr("disabled");
			}
			
			/* RU Rubrics ********************************************* */
			var peerEvalOpenDate = row.find(".peer-eval-open-date").text();
			if (peerEvalOpenDate!=undefined) {
				console.log("testing");
				var peerEvalOpenTime = row.find(".peer-eval-open-time").text();
				$("#peer-eval-open-date input:nth(0)").val(peerEvalOpenDate).change();
				$("#peer-eval-open-date input:nth(4)").val(peerEvalOpenTime).change();
				
				var peerEvalDueDate = row.find(".peer-eval-due-date").text();
				var peerEvalDueTime = row.find(".peer-eval-due-time").text();
				$("#peer-eval-due-date input:nth(0)").val(peerEvalDueDate).change();
				$("#peer-eval-due-date input:nth(4)").val(peerEvalDueTime).change();
			}else console.log("peerEvalOpenDate is undefined!!!!");
			
			if(!$("#peer-eval-check").attr("checked")) {
				$("#available-rubrics-container input").attr("disabled", true).removeAttr("checked");
				$(".student-peer-review-selected").val("");
				
				$("#peer-eval-open-date").hide();
				$("#peer-eval-due-date").hide();
				$("#peer-eval-allow-self-div").hide();
			}else {
				$("#available-rubrics-container input").removeAttr("disabled");
				
				$("#peer-eval-open-date").show();
				$("#peer-eval-due-date").show();
				$("#peer-eval-allow-self-div").show();
				$("#peer-eval-allow-selfgrade").attr("Checked", false);
			}
			var selfEval = row.find(".peer-eval-allow-self").text();
			
			if(selfEval == "true") {
				$("#peer-eval-allow-selfgrade").attr("checked", true);
	 			$("#peer-eval-allow-selfgrade").attr("defaultChecked", true);
			}else {
				$("#peer-eval-allow-selfgrade").attr("checked", false);
			}
			var grade = row.find(".studentGrade").text();
			if(grade == "true") {
				$("#student-graded").attr("checked", true);
				$("#student-graded").attr("defaultChecked", true)
			}else {
				$("#student-graded").attr("checked", false);
			}
			
			$("#student-max").val(row.find(".studentMaxPoints").text());
			if($("#student-max").val() == "null") {
				$("#student-max").val("");
			}
			
			grade = row.find(".studentGrade2").text();
			if(grade == "true") {
				$("#student-comments-graded").attr("checked", true);
				$("#student-comments-graded").attr("defaultChecked", true)
			}else {
				$("#student-comments-graded").attr("checked", false);
			}
			
			$("#student-comments-max").val(row.find(".studentMaxPoints2").text());
			if($("#student-comments-max").val() == "null") {
				$("#student-comments-max").val("");
			}
			
			var position = row.position();
			$("#student-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(this);
			$('#student-dialog').dialog('open');
			checksize($("#student-dialog"));
			$("#grouplist").hide();
			return false;
		});
		
		$("#editgroups-student").click(function(){
			$("#editgroups-student").hide();
			$("#grouplist").show();
			checksize($("#student-dialog"));
		    });

		$("#student-group-owned").click(function(){
			$("#student-group-show").show();
			$("#student-grouplist").show();
			checksize($("#student-dialog"));
		    });

		$("#student-comments").click(function() {
			if(!$("#student-comments").attr("checked")) {
				$("#student-comments-anon").attr("disabled", true).removeAttr("checked");
				$("#student-comments-graded").attr("disabled", true).removeAttr("checked");
				$("#student-comments-max").attr("disabled", true).val("");
			}else {
				$("#student-comments-anon").removeAttr("disabled");
				$("#student-comments-graded").removeAttr("disabled");
				$("#student-comments-max").removeAttr("disabled");
			}
		});
		
		/* RU Rubrics ********************************************* */
		$("#peer-eval-check").change(function() {
			if(!$("#peer-eval-check").attr("checked")) {
				$("#available-rubrics-container input").attr("disabled", true).removeAttr("checked");
				/*show the dateEvolver */
				$("#peer-eval-open-date").hide();
				$("#peer-eval-due-date").hide();
				$("#peer-eval-allow-self-div").hide();
			}else {
				console.log("#peer-eval-check is checked");
				$("#available-rubrics-container input").removeAttr("disabled");
				
				$("#peer-eval-open-date").show();
				$("#peer-eval-due-date").show();
				$("#peer-eval-allow-self-div").show();
				$("#peer-eval-allow-selfgrade").attr("checked", false);
			}
		});
		
		$("#student-peer-review-create").hover(function(){$(this).css("cursor", "default")});
		$("#student-peer-review-create").click(function(){
			if(!$("#peer-eval-check").attr("checked")) {
				$("#peer-eval-check").attr("checked", true);
				$("#peer-eval-check").change();
			}
			displayBlankRubric(true);
			$('#peer-eval-create-dialog').dialog('open');
			$('#createRubricBtn').show();
			$('#updateRubricBtn').hide();
		});

		$("#editgroups-movie").click(function(){
			$("#editgroups-movie").hide();
			$("#grouplist").show();
		});
		
		// IE8 was handling this event oddly.  This is the only pattern I could
		// get to work.
		$("[name='question-select-selection']").bind('click', function() {
			if($(this).attr("id") === "multipleChoiceSelect") {
				$("#shortanswerDialogDiv").hide();
				$("#multipleChoiceDialogDiv").show();
			}else {
				$("#shortanswerDialogDiv").show();
				$("#multipleChoiceDialogDiv").hide();
			}
		});
		
		$('.question-link').click(function(){
			closeDropdown();
			var position =  $(this).position();
			
			$("#questionEditId").val("-1");
			$("#question-text-input").val("");
			$("#question-answer-input").val("");
			$("#question-graded").attr("checked", false);
			$("#question-gradebook-title").val("");
			$("#question-max").val("");
			$("#question-required").attr("checked", false);
			$("#question-prerequisite").attr("checked", false);
			$("#question-show-poll").attr("checked", false);
			$("#multipleChoiceSelect").click();
			resetMultipleChoiceAnswers();
			resetShortanswers();
			
			$("#multipleChoiceSelect").removeAttr("disabled");
			$("#shortanswerSelect").removeAttr("disabled");
			checkQuestionGradedForm();
			
			$("#question-correct-text").val("");
			$("#question-incorrect-text").val("");
			
			$("#delete-question-div").hide();
			
			$("#question-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(this);
			$('#question-dialog').dialog('open');
			checksize($('#subpage-dialog'));
			return false;
		});
		
		$("#question-graded").click(checkQuestionGradedForm);
		
		$(".edit-question").click(function(){
			closeDropdown();
			
			var row = $(this).parent().parent().parent();
			
			var itemId = row.find(".question-id").text();
			$("#questionEditId").val(itemId);
			
			var questionText = row.find(".questionText").text();
			$("#question-text-input").val(questionText);
			
			resetMultipleChoiceAnswers();
			resetShortanswers();
			
			// We can't have these disabled when trying to select them (which we do to set the type
			// in the dialog).  They're disabled again later in this function so that users can't
			// change the question type of an already existing question.
			$("#multipleChoiceSelect").removeAttr("disabled");
			$("#shortanswerSelect").removeAttr("disabled");
			
			var questionType = row.find(".questionType").text();
			if(questionType === "shortanswer") {
				$("#shortanswerSelect").click();
				
				var questionAnswers = row.find(".questionAnswer").text().split("\n");
				for(var index = 0; index < questionAnswers.length - 1; index++) {
					var answerSlot;
					if(index == 0) {
						answerSlot = $("#copyableShortanswerDiv").first();
					}else {
						answerSlot = addShortanswer();
					}
					
					answerSlot.find(".question-shortanswer-answer").val(questionAnswers[index]);
				}
			}else {
				$("#multipleChoiceSelect").click();
				
				$("#question-answer-input").val("");
				
				row.find(".questionMultipleChoiceAnswer").each(function(index, el) {
					var id = $(el).find(".questionMultipleChoiceAnswerId").text();
					var text = $(el).find(".questionMultipleChoiceAnswerText").text();
					var correct = $(el).find(".questionMultipleChoiceAnswerCorrect").text();
					
					var answerSlot;
					if(index == 0) {
						answerSlot = $("#copyableMultipleChoiceAnswerDiv").first();
					}else {
						answerSlot = addMultipleChoiceAnswer();
					}
					
					answerSlot.find(".question-multiplechoice-answer-id").val(id);
					answerSlot.find(".question-multiplechoice-answer").val(text);
					if(correct == "true") {
						answerSlot.find(".question-multiplechoice-answer-correct").attr("checked", true);
					}else {
						answerSlot.find(".question-multiplechoice-answer-correct").attr("checked", false);
					}
				});
				
				var questionShowPoll = row.find(".questionShowPoll").text();
				if(questionShowPoll == "true") {
					$("#question-show-poll").attr("checked", true);
				}else {
					$("#question-show-poll").attr("checked", false);
				}
			}
			
			// Don't allow question types to be changed.  Simplifies consistency in grading on the backend.
			$("#multipleChoiceSelect").attr("disabled", "disabled");
			$("#shortanswerSelect").attr("disabled", "disabled");
			
			var questionGraded = row.find(".questionGrade").text();
			if(questionGraded == "true") {
				$("#question-graded").attr("checked", true);
			}else {
				$("#question-graded").attr("checked", false);
			}
			
			checkQuestionGradedForm();
			
			var gradebookTitle = row.find(".questionGradebookTitle").text();
			if(gradebookTitle == "null") {
				$("#question-gradebook-title").val("");
			}else {
				$("#question-gradebook-title").val(gradebookTitle);
			}
			
			var maxPoints = row.find(".questionMaxPoints").text();
			if(maxPoints == "null") {
				$("#question-max").val("");
			}else {
				$("#question-max").val(maxPoints);
			}
			
			var questionCorrectText = row.find(".questionCorrectText").text();
			$("#question-correct-text").val(questionCorrectText);
			
			var questionIncorrectText = row.find(".questionIncorrectText").text();
			$("#question-incorrect-text").val(questionIncorrectText);
			
			var required = row.find(".questionitem-required").text();
			if(required == "true") {
				$("#question-required").attr("checked", true);
			}else {
				$("#question-required").attr("checked", false);
			}
			
			var prerequisite = row.find(".questionitem-prerequisite").text();
			if(prerequisite == "true") {
				$("#question-prerequisite").attr("checked", true);
			}else {
				$("#question-prerequisite").attr("checked", false);
			}
			
			$("#delete-question-div").show();
			
			var position = row.position();
			$("#question-dialog").dialog("option", "position", [position.left, position.top]);
			oldloc = $(this);
			$('#question-dialog').dialog('open');
			checksize($("#question-dialog"));
			return false;
		});
		
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
			$("#add-multimedia-dialog").dialog('open');
			checksize($("#add-multimedia-dialog"));
			// originally I thought it was confusing to start with the focus on some
			// specific item in the dialog. The problem is that JAWS won't announce
			// the dialog unless some item has focus
			//$('.edit-multimedia-input').blur();
			//$('.mm-additional-instructions').blur();
			return false;
		});

		$("#expert-movie-toggle").click(function(){
			$("#expert-movie-toggle-div").hide();
			$("#expert-movie").show();
			checksize($("#movie-dialog"));
			return false;
		});
		
		$(".edit-link").click(function(){
			closeDropdown();
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
                        requirementType = type;
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
						$("#edit-item-object").attr("href").replace(/(source=).*?(&)/, '$1' + escape(editurl) + '$2'));
					$("#edit-item-text").text(msg("simplepage.edit_quiz"));
					$("#edit-item-settings-p").show();
					$("#edit-item-settings").attr("href", 
						$("#edit-item-settings").attr("href").replace(/(source=).*?(&)/, '$1' + escape(editsettingsurl) + '$2'));
					$("#edit-item-settings-text").text(msg("simplepage.edit_quiz_settings"));

				}else if (type == 8){
					$("#change-forum-p").show();
					$("#change-forum").attr("href", 
					      $("#change-forum").attr("href").replace("itemId=-1", "itemId=" + itemid));
					$("#require-label").text(msg("simplepage.require_submit_forum"));
					$("#edit-item-object-p").show();
					$("#edit-item-object").attr("href", 
						$("#edit-item-object").attr("href").replace(/(source=).*?(&)/, '$1' + escape(editurl) + '$2'));
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
						$("#edit-item-object").attr("href").replace(/(source=).*?(&)/, '$1' + escape(editurl) + '$2'));
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
			    row.find(".path-url").attr("href", row.find(".itemlink").attr('href'));
			    $("#path").html(row.find(".item-path").html());

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
			oldloc = $(this);
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
			$("#add-multimedia-dialog").dialog('open');
			checksize($("#add-multimedia-dialog"));
			//$('.edit-multimedia-input').blur();
			//$('.edit-multimedia-input').blur();
			return false;
		});

		$(".add-multimedia").click(function(){
			closeDropdown();
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
			oldloc = $(this);
			$("#add-multimedia-dialog").dialog('open');
			checksize($("#add-multimedia-dialog"));
			//$('.edit-multimedia-input').blur();
			//$('.mm-additional-instructions').blur();
			return false;
		});

		$(".add-resource").click(function(){
			closeDropdown();
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
			oldloc = $(this);
			$("#add-multimedia-dialog").dialog('open');
			checksize($("#add-multimedia-dialog"));
			//$('.edit-multimedia-input').blur();
			return false;
		});

		$(".add-website").click(function(){
			closeDropdown();
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
			oldloc = $(".dropdown a");
			$("#add-multimedia-dialog").dialog('open');
			checksize($("#add-multimedia-dialog"));
			//$('.edit-multimedia-input').blur();
			//$('.mm-additional-website-instructions').blur();
			return false;
		});

		$(".multimedia-edit").click(function(){
			closeDropdown();
			$("#expert-multimedia").hide();
			$("#expert-multimedia-toggle-div").show();
			$("#editgroups-mm").after($("#grouplist"));
			$("#grouplist").hide();
			$("#editgroups-mm").hide();

			var row = $(this).parent().parent().parent();

			row.find(".path-url").attr("href", row.find(".multimedia").attr("src"));
			$("#mm-path").html(row.find(".item-path").html());
			
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
			oldloc = $(this);
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
			$("#add-multimedia-dialog").dialog('open');
			checksize($("#add-multimedia-dialog"));
			//$('.edit-multimedia-input').blur();
			//$('.mm-additional-instructions').blur();
			return false;
		});

		$("#item-required").click(function(){
			setUpRequirements();
		});
		
		$("#item-required2").click(function(){
			setUpRequirements();
		});
		
		$('body').bind('dialogopen', function(event) {
			hideMultimedia();
		});
		
		$('body').bind('dialogclose', function(event) {
			if (!($('#subpage-dialog').dialog('isOpen') ||
				$('#edit-item-dialog').dialog('isOpen') ||
				$('#edit-multimedia-dialog').dialog('isOpen') ||
				$('#add-multimedia-dialog').dialog('isOpen') ||
				$('#edit-title-dialog').dialog('isOpen') ||
				$('#new-page-dialog').dialog('isOpen') ||
				$('#remove-page-dialog').dialog('isOpen') ||
				$('#youtube-dialog').dialog('isOpen') ||
				$('#movie-dialog').dialog('isOpen') ||
				$('#import-cc-dialog').dialog('isOpen') ||
				$('#export-cc-dialog').dialog('isOpen') ||
				$('#comments-dialog').dialog('isOpen') ||
				$('#student-dialog').dialog('isOpen')) ||
				$('#question-dialog').dialog('isOpen')) {
					unhideMultimedia();
				}
		});
		 
		$("#cssDropdown-selection").children(":contains(---" + msg("simplepage.site") + "---)").attr("disabled", "disabled");
		$("#cssDropdown-selection").children(":contains(---" + msg("simplepage.system") + "---)").attr("disabled", "disabled");
		$("#cssDropdown-selection").children(":contains(----------)").attr("disabled", "disabled");
		
		$("#studentPointsBox").val($("#studentPointsBox").parent().children(".pointsSpan").text());
		
		$("#studentPointsBox").live('change', function(){
			var img = $(this).parent().children("img");
			img.attr("src", getStrippedImgSrc(img.attr("id")) + "no-status.png");
			$(this).addClass("unsubmitted");
		});
		
		$("#studentPointsBox").keyup(function(event){
			if(event.keyCode == 13) {
			    submitgrading($(this));
			    return false;
			}
		});
		
		$(".grading-nextprev").click(function(event){
			// if unsubmitted grade, submit it before going to new page
			if ($("#studentPointsBox").hasClass("unsubmitted")) {
			    submitgrading($(this));
			    // set hook to do follow the link when the grade update returns
			    setGradingReturnHook($(this).attr('href'));
			    return false;
			}
			return true;
		});

		$("#submit-grading").click(function() {
			submitgrading($(this));
			return false;
		});
		
		$("#peer-eval-open-date input:nth(4)").blur(function(e){
			e.target.value=e.target.value.toUpperCase();
			if((e.target.value.substring(e.target.value.length-2) == "AM" || e.target.value.substring(e.target.value.length-2) == "PM")
				&& e.target.value.substring(e.target.value.length-3, e.target.value.length-2)!=" ")
				e.target.value= e.target.value.substring(0, e.target.value.length-2)+" "+e.target.value.substring(e.target.value.length-2);
		});
		
		$("#peer-eval-due-date input:nth(4)").blur(function(e){
			e.target.value=e.target.value.toUpperCase();
			if((e.target.value.substring(e.target.value.length-2) == "AM" || e.target.value.substring(e.target.value.length-2) == "PM")
				&& e.target.value.substring(e.target.value.length-3, e.target.value.length-2)!=" ")
				e.target.value= e.target.value.substring(0, e.target.value.length-2)+" "+e.target.value.substring(e.target.value.length-2);
		});

	} // Closes admin if statement

	$(".showPollGraph").click(function() {
		var pollGraph = $(this).parents(".questionDiv").find(".questionPollGraph");
		
		if($(this).find("span").text() === $(this).parent().find(".show-poll").text()) {
			pollGraph.empty();
			var pollData = [];
			pollGraph.parent().find(".questionPollData").each(function(index) {
				var text = $(this).find(".questionPollText").text();
				var count = $(this).find(".questionPollNumber").text();
				
				pollData[index] = [parseInt(count), text];
			});
			
			pollGraph.show();
			pollGraph.jqBarGraph({data: pollData, height:100, speed:1});
			
			$(this).find("span").text($(this).parent().find(".hide-poll").text());
		}else {
			pollGraph.hide();
			pollGraph.empty();
			
			$(this).find("span").text($(this).parent().find(".show-poll").text());
		}
		
		setMainFrameHeight(window.name);
	});
	
	function submitgrading(item) {
	    var img = item.parent().children("img");
			
	    item.parent().children("#studentPointsBox").removeClass("unsubmitted");
	    img.attr("src", getStrippedImgSrc(img.attr("id")) + "loading.gif");
			
	    $(".idField").val(item.parent().children(".uuidBox").text()).change();
	    $(".jsIdField").val(img.attr("id")).change();
	    $(".typeField").val("student");
	    
	    // This one triggers the update
	    $(".pointsField").val(item.parent().children("#studentPointsBox").val()).change();
	    
	    return false;
	};

	if (!(navigator.userAgent.indexOf("Firefox/2.") > 0)) {
	    $('.usebutton').button({text:true});
	} else {
	    // fake it; can't seem to get rid of underline though
	    $('.usebutton').css('border', '1px solid black').css('padding', '1px 4px').css('color', 'black');
	}

	$('.buttonset').buttonset();

	function fixhref(href, pageitemid, resourcetype, website) {
	    href = href.replace(/&pageItemId=-?[0-9]*/, "&pageItemId=" + pageitemid);
	    href = href.replace(/&resourceType=[a-z]*/, "&resourceType=" + resourcetype);
	    href = href.replace(/&website=[a-z]*/, "&website=" + website);
	    return href;
	}


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

	$('.textbox a[class!=itemcopylink]').each(function(index) {
		try {
		    if ($(this).attr('href').match("^http://lessonbuilder.sakaiproject.org/") != null) {
			var item = $(this).attr('href').substring(38).replace('/','');
			var a = $('a[lessonbuilderitem=' + item + ']').first();
			$(this).replaceWith(a);
		    }
		} catch (err) {};
	    });
	
	
	$('#edit-title-error-container').hide();
	$('#new-page-error-container').hide();
	$('#edit-item-error-container').hide();
	$('#movie-error-container').hide();
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
			out: buttonRemoveHighlight
	};
	
	var dropdownConfig = {	
			interval: 0,
			sensitivity: 7,
			over: menuAddHighlight,
			timeout: 700,
			out: removeHighlight
	};


	$("li.dropdown").hoverIntent(megaConfig);
	$("#dropDownDiv").hide();
	$("#dropDownDiv").hoverIntent(dropdownConfig);
	$("li.dropdown").click(toggleDropdown);
	dropDownViaClick = false;
	return false;
});

function closeSubpageDialog() {
	$("#subpage-dialog").dialog("close");
	$('#subpage-error-container').hide();
	oldloc.focus();
}

function closeEditItemDialog() {
	$("#edit-item-dialog").dialog("close");
	$('#edit-item-error-container').hide();
	$("#select-resource-group").hide();
	oldloc.focus();
}

function closeMultimediaEditDialog() {
	$("#edit-multimedia-dialog").dialog("close");
	$('#movie-error-container').hide();
	oldloc.focus();
}

function closeAddMultimediaDialog() {
	$("#add-multimedia-dialog").dialog("close");
	oldloc.focus();
}

function closeEditTitleDialog() {
	$('#edit-title-dialog').dialog('close');
	$('#edit-title-error-container').hide();
	oldloc.focus();
}

function closeNewPageDialog() {
	$('#new-page-dialog').dialog('close');
	$('#new-page-error-container').hide();
	oldloc.focus();
}

function closeImportCcDialog() {
	$('#import-cc-dialog').dialog('close');
	oldloc.focus();
}

function closeExportCcDialog() {
	$('#export-cc-dialog').dialog('close');
	oldloc.focus();
}

function closeRemovePageDialog() {
	$('#remove-page-dialog').dialog('close');
	oldloc.focus();
}

function closeYoutubeDialog() {
	$('#edit-youtube-error-container').hide();
	$('#youtube-dialog').dialog('close');
	oldloc.focus();
}

function closeMovieDialog() {
	$('#movie-error-container').hide();
	$('#movie-dialog').dialog('close');
	oldloc.focus();
}

function closeCommentsDialog() {
	$('#comments-dialog').dialog('close');
	oldloc.focus();
}

function closeStudentDialog() {
	$('#student-dialog').dialog('close');
	oldloc.focus();
}

function closeQuestionDialog() {
	$('#question-dialog').dialog('close');
	oldloc.focus();
}

function closePeerReviewDialog() {
	$('#peer-eval-create-dialog').dialog('close');
}

function checkEditTitleForm() {
	if($('#pageTitle').val() == '') {
		$('#edit-title-error').text(msg("simplepage.title_notblank"));
		$('#edit-title-error-container').show();
		return false;
	}else if ($("#page-gradebook").attr("checked") && !isFinite(parseFloat($("#page-points").val()))) {
		$('#edit-title-error').text(msg("simplepage.integer-expected"));
		$('#edit-title-error-container').show();
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

function checkYoutubeForm(w, h) {
	if(w && h && !checkMovieForm(w, h, true)) {
		return false;
	}

	if($('#youtubeURL').val().contains('youtube.com')) {
		return true;
	}else {
		$('#edit-youtube-error').val(msg("simplepage.must_be_youtube"));
		$('#edit-youtube-error-container').show();
		return false;
	}
}

//this checks the width and height fields in the Edit dialog to validate the input
function checkMovieForm(w, h, y) {
		var wmatch = checkPercent(w); 	// use a regex to check if the input is of the form ###%
		var hmatch = checkPercent(h);
		var wvalid = false; 			// these hold whether the width or height input has been validated
		var hvalid = false;

		var eitem, econtainer;			// the span and div, respectively, for each dialog's error message
		var pre;
		if (y) {						// determine which dialog we're in and which error span/div to populate if there's an error
			pre = '#edit-youtube';
		} else {
			pre = '#movie';
		}

		eitem = $(pre + '-error');
		econtainer = $(pre + '-error-container');

		if (w.trim() == "") {			// empty input is ok
			wvalid = true;
		} 

		if (h.trim() == "") {
			hvalid = true;
		}

		if (wmatch !== null && !wvalid) {	// if it's of the form ###%, check if the ### is between 0 and 100
			var nw = Number(w.substring(0, w.length-1));
			if (nw < 1 || nw > 100) {
				// paint error message
				eitem.text(msg("simplepage.nothing-over-100-percent"));
				econtainer.show();
				return false;
			} else {
				wvalid = true;
			}
		}
		
		if (hmatch !== null && !hvalid) {
			var nh = Number(h.substring(0, h.length-1));
			if (nh > 100) {
				// paint error message
				eitem.text(msg("simplepage.nothing-over-100-percent"));
				econtainer.show();
				return false;
			} else {
				hvalid = true;
			}
		}

		wmatch = checkWidthHeight(w);	// if it's not a percentage, check to make sure it's of the form ### or ###px
		hmatch = checkWidthHeight(h);

		if (wmatch == null && !wvalid) {
			// paint error message
			eitem.text(msg("simplepage.width-height"));
			econtainer.show();
			return false;
		}

		if (hmatch == null && !hvalid) {
			// paint error message
			eitem.text(msg("simplepage.width-height"));
			econtainer.show();
			return false;
		}
		econtainer.hide();
		return true;
}

function checkWidthHeight(x) {
	var regex = /^[0-9]+$|^[0-9]+px$/;
	return (x.match(regex));
}

function checkPercent(x) {
	var regex = /^[0-9]+\%$/;
	return (x.match(regex));
}

function checkCommentsForm() {
	return true;
}

function checkEditItemForm() {
	if($('#name').val() == '') {
		$('#edit-item-error').text(msg("simplepage.item_notblank"));
		$('#edit-item-error-container').show();
		return false;
        } else if ((requirementType == 3 || requirementType == 6) && 
		   $("#item-required2").attr("checked") && !isFinite(parseFloat($("#assignment-points").val()))) {
		$('#edit-item-error').text(msg("simplepage.integer-expected"));
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

var hasBeenInMenu = false;

function menuAddHighlight() {
    hasBeenInMenu = true;
    addHighlight();
	return false;
}

function buttonRemoveHighlight() {
    if (!hasBeenInMenu)
	removeHighlight();
	return false;
}

function addHighlight() {
	if(!lessonBuilderAnimationLocked) {
		if(!$("#dropDownDiv").is(":visible")) {
			lessonBuilderAnimationLocked = true;
			hideMultimedia();
			reposition();
			$("#dropDownDiv").show("slide", {direction: "up"}, 300, unlockAnimation);
			$(".add-forum-link").focus();
		}
	}
	//$(this).addClass("hovering");
	return false;
}

function removeHighlight() {
	if(!lessonBuilderAnimationLocked) {
		if($("#dropDownDiv").is(":visible") && !dropdownViaClick) {
			hasBeenInMenu = false;
			lessonBuilderAnimationLocked = true;
			unhideMultimedia();
			$("#dropDownDiv").hide("slide", {direction: "up"}, 300, unlockAnimation);
			$(".dropdown a").focus();
		}
	}
	//$(this).removeClass("hovering");
	return false;
}

function toggleDropdown() {
	if(!lessonBuilderAnimationLocked) {
		if($("#dropDownDiv").is(":visible")) {
			lessonBuilderAnimationLocked = true;
			hasBeenInMenu = false;
			unhideMultimedia();
			$("#dropDownDiv").hide("slide", {direction: "up"}, 300, unlockAnimation);
			dropdownViaClick = false;
			$(".dropdown a").focus();
		}else {
			lessonBuilderAnimationLocked = true;
			hideMultimedia();
			reposition();
			$("#dropDownDiv").show("slide", {direction: "up"}, 300, unlockAnimation);
			$(".add-forum-link").focus();
			dropdownViaClick = true;
		}
	}
	return false;
}

function closeDropdown() {

	if(!lessonBuilderAnimationLocked) {
		if($("#dropDownDiv").is(":visible")) {
			hasBeenInMenu = false;
			unhideMultimedia();
			$("#dropDownDiv").hide();
			dropdownViaClick = false;
			$(".dropdown a").focus();
		}
	}
	return false;
}

function reposition() {
	var dropX = $(".dropdown a").offset().left;
	var dropdown = $("#dropDownDiv");
	//alert("DropX: " + dropX);
	//alert("Width: " + window.innerWidth);
	//alert("Width2: " + dropdown.width());
	if(dropX + dropdown.width() > ($(window).width()-30)) {
	    dropdown.css("left", Math.max(0,($(window).width() - dropdown.width() - 30)) + "px");
	} else {
	    // in case user changes zoom and then tries again, we could end up
            // with a value from the case above that is now incorrect                        	    
            dropdown.css("left", dropX);
	}

}

// Keeps JQuery from getting confused mid-animation
function unlockAnimation() {
	lessonBuilderAnimationLocked = false;
}

function hideMultimedia() {
	$('.hideOnDialog').hide();
}

// When dialogs close, this shows the stuff that was hidden
function unhideMultimedia() {
	$('.hideOnDialog').show();
	$("#outer").height("auto");
	setMainFrameHeight(window.name);
}

// Peer evaluation functions are located in peer-eval.js 

// Clones one of the multiplechoice answers in the Question dialog and appends it to the end of the list
function addMultipleChoiceAnswer() {
	var clonedAnswer = $("#copyableMultipleChoiceAnswerDiv").clone(true);
	var num = $("#extraMultipleChoiceAnswers").find("div").length + 2; // Should be currentNumberOfAnswers + 1
	
	clonedAnswer.find(".question-multiplechoice-answer-id").val("-1");
	clonedAnswer.find(".question-multiplechoice-answer-correct").attr("checked", false);
	clonedAnswer.find(".question-multiplechoice-answer").val("");
	
	clonedAnswer.attr("id", "multipleChoiceAnswerDiv" + num);
	
	// Each input has to be renamed so that RSF will recognize them as distinct
	clonedAnswer.find("[name='question-multiplechoice-answer-complete']")
		.attr("name", "question-multiplechoice-answer-complete" + num);
	clonedAnswer.find("[name='question-multiplechoice-answer-complete-fossil']")
		.attr("name", "question-multiplechoice-answer-complete" + num + "-fossil");
	clonedAnswer.find("[name='question-multiplechoice-answer-id']")
		.attr("name", "question-multiplechoice-answer-id" + num);
	clonedAnswer.find("[for='question-multiplechoice-answer-correct']")
		.attr("for", "question-multiplechoice-answer-correct" + num);
	clonedAnswer.find("[name='question-multiplechoice-answer-correct']")
		.attr("name", "question-multiplechoice-answer-correct" + num);
	clonedAnswer.find("[for='question-multiplechoice-answer']")
		.attr("for", "question-multiplechoice-answer" + num);
	clonedAnswer.find("[name='question-multiplechoice-answer']")
		.attr("name", "question-multiplechoice-answer" + num);
	
	// Unhide the delete link on every answer choice other than the first.
	// Not allowing them to remove the first makes this AddAnswer code simpler,
	// and ensures that there is always at least one answer choice.
	clonedAnswer.find(".deleteAnswerLink").removeAttr("style");

	clonedAnswer.appendTo("#extraMultipleChoiceAnswers");
	
	return clonedAnswer;
}

// Clones one of the shortanswers in the Question dialog and appends it to the end of the list
function addShortanswer() {
	var clonedAnswer = $("#copyableShortanswerDiv").clone(true);
	
	clonedAnswer.find(".question-shortanswer-answer").val("");
	
	// Unhide the delete link on every answer choice other than the first.
	// Not allowing them to remove the first makes this AddAnswer code simpler,
	// and ensures that there is always at least one answer choice.
	clonedAnswer.find(".deleteAnswerLink").removeAttr("style");

	// have to make name unique, so append a count
	var n = $("#extraShortanswers div").length;
	var elt = clonedAnswer.find("label");
	elt.attr("for", elt.attr("for") + n);
	elt = clonedAnswer.find("input");
	elt.attr("name", elt.attr("name") + n);
	
	clonedAnswer.appendTo("#extraShortanswers");


	
	return clonedAnswer;
}

function updateMultipleChoiceAnswers() {
	$(".question-multiplechoice-answer-complete").each(function(index, el) {
		var id = $(el).parent().find(".question-multiplechoice-answer-id").val();
		var checked = $(el).parent().find(".question-multiplechoice-answer-correct").is(":checked");
		var text = $(el).parent().find(".question-multiplechoice-answer").val();
		
		$(el).val(index + ":" + id + ":" + checked + ":" + text);
	});
}

function updateShortanswers() {
	var answerText = "";
	
	$(".question-shortanswer-answer").each(function() {
		answerText += $(this).val() + "\n"; 
	});
	
	$("#question-answer-full-shortanswer").val(answerText);
}

function deleteAnswer(el) {
	el.parent('div').remove();
}

// Enabled or disables the subfields under grading in the question dialog
function checkQuestionGradedForm() {
	if($("#question-graded").is(":checked")) {
		$("#question-max").removeAttr("disabled");
		$("#question-gradebook-title").removeAttr("disabled");
	}else {
		$("#question-max").attr("disabled", "disabled");
		$("#question-gradebook-title").attr("disabled", "disabled");
	}
}

// Prepares the question dialog to be submitted
function prepareQuestionDialog() {
	updateMultipleChoiceAnswers();
	updateShortanswers();
	
	// RSF bugs out if we don't undisable these before submitting
	$("#multipleChoiceSelect").removeAttr("disabled");
	$("#shortanswerSelect").removeAttr("disabled");
}

// Reset the multiple choice answers to prevent problems when submitting a shortanswer
function resetMultipleChoiceAnswers() {
	var firstMultipleChoice = $("#copyableMultipleChoiceAnswerDiv");
	firstMultipleChoice.find(".question-multiplechoice-answer-id").val("-1");
	firstMultipleChoice.find(".question-multiplechoice-answer").val("");
	firstMultipleChoice.find(".question-multiplechoice-answer-correct").attr("checked", false);
	$("#extraMultipleChoiceAnswers").empty();
}

//Reset the shortanswers to prevent problems when submitting a multiple choice
function resetShortanswers() {
	$("#copyableShortanswerDiv").find(".question-shortanswer-answer").val("");
	$("#extraShortanswers").empty();
}

