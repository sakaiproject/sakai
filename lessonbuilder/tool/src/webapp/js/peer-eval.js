/* The following applies to the Peer Evaluation Setup (ShowPage.html) page. BEGIN */
//conditional functions
var addEmptyCategoryRow, addCategoryRow, displayBlankRubric, createRubric, updateRubric, addIndexAndMoveToSubmitRubric, buildExistingRubrics, deleteNotLast, saveRubricSelection, closePeerReviewDialog;


$(function() {

	// selectedPeerCell is to mark original
	// newPeerCell is new selection if there's a save
	// start out by setting them the same
	$('.selectedPeerCell').each(function(e) {
		$(this).addClass("newPeerCell");
		var val = '';
		for (i = 0; i < 5; i++) {
		    if ($(this).hasClass(""+i))
			val = i;
		}
		$(this).parents('.peer-eval-row').find('.peerReviewValue').text(val);
	    });

	$('.selectedPeerCell').attr('aria-selected','true');

	$('.peer-eval-row td').click(function(e) {
		var row = $(this).parents('.peer-eval-row');
		var data = row.find('.peer-eval-row-data');
		if (data.size() == 0)
		    return;
		var peerReviewId = row.find('.peerReviewId').text();
		var peerReviewTarget = $(this).parents('.peer-eval-target').find('.peer-eval-target-id').val();
		row.find("td").removeClass("newPeerCell");
		row.find("td").removeAttr('aria-selected');
		$(this).addClass("newPeerCell");
		$(this).attr('aria-selected', 'true');
		for (i = 0; i < 5; i++) {
		    if ($(this).hasClass(""+i)) {
			data.val(peerReviewId + ":" +i+ ":" + peerReviewTarget);
			$(this).parents('.peer-eval-row').find('.peerReviewValue').text(""+i);
		    }
		}
	    });

        $('.cancel-peereval-link').click(function(e) {
		var form = $(this).parents('form');
		// put back original classes
		form.find('.newPeerCell').removeAttr('aria-selected');
		form.find('.newPeerCell').removeClass('newPeerCell');
		form.find('.selectedPeerCell').addClass("newPeerCell");
		form.find('.selectedPeerCell').attr('aria-selected', 'true');
		// kill anything set up for next save
		form.find('.peer-eval-row-data').val("");
		$('.selectedPeerCell').each(function(e) {
			var val = '';
			for (i = 0; i < 5; i++) {
			    if ($(this).hasClass(""+i))
				val = i;
			}
			$(this).parents('.peer-eval-row').find('.peerReviewValue').text(val);
		    });
            });

	if ($(".studentContentType").length > 0) {
		//nextNumber has been negated...
		addEmptyCategoryRow = function(tableSelector, customId) {
			var nextNumber = (customId !== -1) ? customId : ($(tableSelector + " .newRow").length + 1) * -1;
			var newRow = (customId === -1) ? "newRow" : "";

			$(tableSelector + " tbody").append('<tr><td><span class="rubricRowIndex ' + newRow + '" style="display:none;">' + nextNumber + '</span>' + blankRubricRow + '</td><td></td><td></td><td></td><td></td><td></td></tr>');
			$firstInput = $(tableSelector + " tbody tr:last input:first");
			$secondInput = $(tableSelector + " tbody tr:last input:last");
			$firstInput.attr("name", $firstInput.attr("name") + nextNumber);
			$secondInput.attr("name", $firstInput.attr("name") + "-fossil");
		};

		addCategoryRow = function(tableSelector, value, customId) {
			addEmptyCategoryRow(tableSelector, customId);
			$(tableSelector + " tbody tr:last input:first").val(value);
		};

		displayBlankRubric = function(isNew) {
			$("#peer-eval-create-dialog .peer-eval-create-form").html(blankRubricTemplate);

			if (isNew) {
				addCategoryRow("#peer-eval-create-dialog #peer-eval-create-table", "", -1);
				$("#peer-eval-create-dialog #peer-eval-create-table" + " tbody tr:last input:first").click(function() {
					$(this).unbind("click");
					$(this).val("");
				});
			}

			$("#peer-eval-create-table").css("width", "100%").css("height", "100%").css("border", "1px solid black"); //.css("background-color","green")
			$("#peer-eval-create-table th").css("border", "1px solid black");

			$("#peer-eval-create-dialog #addRowBtn").click(function() {
				addEmptyCategoryRow("#peer-eval-create-dialog #peer-eval-create-table", -1);
			});
		};

		//After pressing "Continue"
		//This is called on click when creation of rubric is complete.
		//creates the rubric from the table data.
		//build the string... removing the delimeters
		//adds new radio button+
		//use blankRubricRow instead.
		createRubric = function(isNew) {
			isNew = typeof isNew !== 'undefined' ? isNew : true;
			$rubricRadioContainer = $("#available-rubrics-container");

			//add radio button with label
			var label = $("#peer-eval-create-dialog input[name=peer-eval-input-title]").val();
			var idLabel = "peer-eval-id-label";
			idLabel = idLabel + $("[id^=" + idLabel + "]").length;
			$rubricRadioContainer.append('<div>' + '<input type="radio" name="student-peer-review-create" id="' + idLabel + '" class="indent" />' + '<label for="' + idLabel + '"><span>' + label + '</span></label><br />' + '<div style="display:none;" class="rubricForm"></div></div>');

			//Select current radio and add click handler.
			$(".rubricForm:last", $rubricRadioContainer).html($("#peer-eval-create-dialog .peer-eval-create-form").clone());
			$("input:checked", $rubricRadioContainer).removeAttr("checked");
			$("#" + idLabel, $rubricRadioContainer).attr("checked", true).attr("defaultChecked", true).click(function() {
				$("#peer-eval-create-dialog .peer-eval-create-form").replaceWith($("#available-rubrics-container #" + idLabel).parent().find("form").clone());
				addIndexAndMoveToSubmitRubric();
				$("#peer-eval-create-dialog .peer-eval-create-form").replaceWith($("#available-rubrics-container #" + idLabel).parent().find("form").clone());
				$("#peer-eval-create-dialog #addRowBtn").click(function() {
					addEmptyCategoryRow("#peer-eval-create-dialog #peer-eval-create-table", -1);
				});
				$(".peer-eval-create-form").submit(function(e) {
					e.preventDefault();
				});
				$('#peer-eval-create-dialog').dialog('open');
				$('#peer-eval-create-dialog').dialog("option", "width", modalDialogWidth());
				$('#createRubricBtn').hide();
				$('#updateRubricBtn').show();
			});

			addIndexAndMoveToSubmitRubric();
			if (isNew) closePeerReviewDialog();
		};

		//onclick of "Update" button in the rubric edit dialog
		updateRubric = function() {
			var $currentRubric = $("#available-rubrics-container input:checked");
			var $rubricContainer = $("#available-rubrics-container");
			var $currentRubricDiv = $currentRubric.parent();
			var id = $currentRubric.attr("id");
			var $title = $("#peer-eval-create-dialog #rubric-title input[name=peer-eval-input-title]");
			
			$title.val($title.val().replace(/["'=!<>(){}\[\]]/g , "")); //filter

			var label = $title.val();
			$("label[for=" + id + "]", $rubricContainer).html("<span>" + label + "</span>");

			$(".rubricForm", $currentRubricDiv).html($("#peer-eval-create-dialog .peer-eval-create-form").clone());

			addIndexAndMoveToSubmitRubric();
			closePeerReviewDialog();
		};

		addIndexAndMoveToSubmitRubric = function() {
			$(rowInputs = "#peer-eval-create-dialog .peer-eval-create-form table tbody tr").each(function() {
				var $inputBox = $("input:first", this);
				$inputBox.val($("span.rubricRowIndex", $inputBox.parent()).text() + ":" + $inputBox.val());
			});
			$("#peer-eval-rows-for-submission").html($("#peer-eval-create-dialog .peer-eval-create-form input").clone());
		};

		buildExistingRubrics = function(rubric) {
			displayBlankRubric(false);
			$form = $("#peer-eval-create-dialog .peer-eval-create-form");
			$(".peer-eval-input-title", $form).val(rubric.title);
			for (var i = 0; i < rubric.rows.length; i++) {
				addCategoryRow("#peer-eval-create-dialog #peer-eval-create-table", rubric.rows[i].text, rubric.rows[i].id);
				//console.log(rubric.rows[i].text);
			}

			if (rubric.rows.length > 0) createRubric(false);
		};

		deleteNotLast = function(btn) {
			if ($("#peer-eval-create-dialog .peer-eval-create-form .peer-eval-input-row").length > 1) $(btn).parent().parent().remove();
		};

		closePeerReviewDialog = function() {
			$('#peer-eval-create-dialog').dialog('close');
		};

	}
}); /* The above applies to the Peer Evaluation Setup (ShowPage.html) page. END */

/* The following applies to the Peer Evaluation Statistics (PeerEvalStats.html) page. BEGIN */

function countByGrader(grader) {
   var count = 0;

   $(".peer-eval-gradee-branch").each(function() {
      var countPerGradee = 0;
      $(".peer-eval-grader-id", this).each(function() {
	 if ($(this).text() === grader) countPerGradee++;
      });
      if (countPerGradee > (numCategories / 2)) count++;
   });
   return count;
}


$(function() {
	if ($(".peer-eval-rubric").length > 0) {
		var CELL_SELECTED_BACKGROUND_COLOR = "rgb(177, 204, 235)";
		var activityMsg = document.getElementById("simplepage.user.activity").innerHTML;
		var ratingToolTip = document.getElementById("simplepage.peer-cell-tooltip").innerHTML;

		var gradees = [];

		getGradeeByUserId = function(userId) {
			//console.log('getGradeeByUserId(' + userId + ')');
			for (w in gradees) {
				if (gradees[w].userid === userId) return gradees[w];
		}
		};

		//Build the JSON Object.
		$(".peer-eval-gradee-branch").each(function() {
			var name = $(".user-name", this).text();
			var userid = $(".user-id", this).text();
			var pageid = $(".user-pageid", this).text();

			var grades = [];
			$(".peer-eval-data-grade", this).each(function() {
				var rowText = $(".peer-eval-row-text", this).text();
				var grade = $(".peer-eval-grade", this).text();
				var count = $(".peer-eval-count", this).text();
				var graders = [];
				$(".peer-eval-grader-branch", this).each(function() {
					var name = $(".peer-eval-grader-name", this).text();
					var id = $(".peer-eval-grader-id", this).text();
					graders.push({
						"id": id,
						"name": name
					});
				});
				grades.push({
					"rowText": rowText,
					"grade": grade,
					"graders": graders,
					"count": count
				});
			});
			gradees.push({
				"gradee": name,
				"userid": userid,
				"pageid": pageid,
				"grades": grades,
				"branch": $(this)
			});
		});

		//Determine user activity **Includes activity that could be invalid due to changes to the rubric after grading has opened.
		numCategories = $(".peer-eval-rubric .peer-eval-row").length;
		for (x in gradees) {
			var target = gradees[x].userid;
			gradees[x].activity = 0;

			$(".peer-eval-gradee-branch").each(function() {
				var countPerGradee = 0;
				$(".peer-eval-grader-id", this).each(function() {
					if ($(this).text() === target) countPerGradee++;
				});
				if (countPerGradee > (numCategories / 2)) gradees[x].activity++;
			});
		}

		//console.log(gradees);

		//Generate rubrics for each student.
		for (x in gradees) {
			var rubricsDiv = [];
			rubricsDiv.push('<div class="rubric-set"><div class="rubric-name">' + gradees[x].gradee);
			rubricsDiv.push('<span class="rubric-userid-div"> (<span class="rubric-userid">' + gradees[x].userid + '</span>) </span>');
                        if (gradees[x].branch.find('.peer-eval-gradee-members').size() > 0) {
                           rubricsDiv.push('<ul>');
                           var graders = gradees[x].branch.find('.peer-eval-gradee-members');
                           rubricsDiv.push(graders.html());
                           rubricsDiv.push('</ul>');
                        }
                        else {
			    rubricsDiv.push('<span role="link" class="rubric-activity">' + (activityMsg === undefined ? 'User Activity' : activityMsg) + ': ');
			    rubricsDiv.push(gradees[x].activity + '</span>');
                        }
                        rubricsDiv.push('</div><div class="rubric-rubric">' + $(".peer-eval-rubric").html() + "</div></div>");
			$(".rubrics").append(rubricsDiv.join(""));
			for (y in gradees[x].grades) {
				var text = gradees[x].grades[y].rowText;
				var grade = gradees[x].grades[y].grade;
				var count = gradees[x].grades[y].count;

				var isValidGrade = false;
				$(".peer-eval-rubric-table:last .peer-eval-row").each(function() {
					if ($("td:first", this).text().trim() === text) {
						isValidGrade = true;
						var graderString = [];
						for (z in gradees[x].grades[y].graders) {
							graderString.push(gradees[x].grades[y].graders[z].name + '<span class="rubric-cell-grader-id">' + gradees[x].grades[y].graders[z].id + '</span>');
						}
						$("." + grade, this).html('<span role="link" class="rating">&nbsp;' + count + '&nbsp;</span><div class="rubric-graders-cell">' + graderString.join("<br>") + "</div>");
					}
				});
         $(".rubric-graders-cell").parent().attr('title',ratingToolTip);               
				if (!isValidGrade) {
					//The activity point should be removed from all of the graders who gave this grade to this gradee.
					for (z in gradees[x].grades[y].graders) {
						var grader = getGradeeByUserId(gradees[x].grades[y].graders[z].id);
						if (grader !== undefined) //graders who do not have a page of their own will be undefined.
						grader.activity--;
					}
				}
				//console.log(gradees[x]);
			}
		}

		//var activityDenom;
		//if(gradees.length>0){
		//	activityDenom=(gradees.length*$(".peer-eval-rubric .peer-eval-row").length)-$(".peer-eval-rubric .peer-eval-row").length;
		//}
		//console.log(activityDenom);
		$(".rubric-set").each(function() {
			//Show and hide student rubrics - onclick
			$(".rubric-name", this).click(function() {
				if ($(this).parent().find(".rubric-rubric").is(":visible")) $(this).parent().find(".rubric-rubric").hide();
				else $(this).parent().find(".rubric-rubric").show();
			});

			//Add updated activity
			//var activity = (getGradeeByUserId($(".rubric-userid", this).text()).activity / activityDenom)*100;
			//$(".rubric-name", this).append('<span class="rubric-activity">'+(activityMsg==undefined?'User Activity':activityMsg)+': '+activity+'%</span>');
		});

		//See graders - display the users who clicked on the particular grade
		$(".rubric-graders-cell").each(function() {
			($(this).parent()).click(function() {
				$("<div>" + $(".rubric-graders-cell", this).html() + "</div>").dialog({
					title: "Graders",
					width: modalDialogWidth(),
					modal: true
				});
			});
		});

                $('.inactive-member').each(function() {
                   var count = countByGrader($(this).find('.inactive-member-id').text());
                   $(this).find('.inactive-member-name').append(': ' + (activityMsg === undefined ? 'User Activity' : activityMsg) + ': ' + count);
                });

		//See activity - when a user clicks on "Activity", the cells the user clicked on will be highlighted.
		$(".rubric-activity , .inactive-member").each(function() {
			$(this).click(function() {
				$(this).parent().click(); //Click the parent again to undo this click's action.
				$(".rubric-graders-cell").each(function() {
					$(this).parent().css("background-color", "");
					$(this).parents('peer-eval-row').find('peer-eval-chosen').text('');
				});
				if ($(this).hasClass("activity-active")) {
					$(".activity-active").removeClass("activity-active");
				} else {
					$(this).addClass("activity-active");
					var userid = $(this).parent().find(".rubric-userid").text();
					if ($(this).hasClass("inactive-member")) {
						userid = $(this).find(".inactive-member-id").text();
					}
					$(".rubric-graders-cell").each(function() {
						$(this).parent().css("background-color", "");
					        $(this).parents('peer-eval-row').find('peer-eval-chosen').text('');
						var htmlSS = $(this).html();
						//console.log(userid);
						if (htmlSS.indexOf(userid) !== -1) {
							$(this).parent().css("background-color", CELL_SELECTED_BACKGROUND_COLOR);
	       						var val = '';
							for (i = 0; i < 5; i++) {
		    						if ($(this).parent().hasClass(""+i))
									val = i;
							}
							var person = $(this).closest('.rubric-set').find('.rubric-name').contents().first().text();
					        	$(this).closest('.peer-eval-row').find('.peer-eval-chosen').text(val);
					        	$(this).closest('.peer-eval-row').find('.peer-eval-person').text(person);
						}
					});
				}
			});
		});

		$(".expand-collapse-btn").click(function(e) {
			if ($(this).hasClass("collaping")) {
				//Hide all
				$(this).removeClass("collaping").addClass("expanding");
				$(".collapse-txt").hide();
				$(".expand-txt").show();

				$(".rubric-rubric").hide();
			} else {
				//Show all
				$(this).removeClass("expanding").addClass("collaping");
				$(".collapse-txt").show();
				$(".expand-txt").hide();

				$(".rubric-rubric").show();
			}
			return false;
		});
	}
        $('.add-peereval-button').click(function(e) {
		$('.peer-eval-div').toggle();
                if ($('.peer-eval-div').css('display') === 'none')
		    $('.peer-eval-div').attr('aria-hidden','true');
                else
		    $('.peer-eval-div').attr('aria-hidden','false');
	});

});

/* The above applies to the Peer Evaluation Statistics page. END */

/* The following applies to the Peer Evaluation display on the STUDENT pages (ShowPage.html). BEGIN */

