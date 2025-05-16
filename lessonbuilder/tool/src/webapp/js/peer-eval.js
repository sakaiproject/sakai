/* The following applies to the Peer Evaluation Setup (ShowPage.html) page. BEGIN */
//conditional functions
var addEmptyCategoryRow, addCategoryRow, buildExistingRubrics, deleteNotLast, saveRubricSelection;

var lessons = lessons || {};
lessons.wrapGraderListInModal = graders => {

  return `
    <div class="modal fade" class="gradersModal" tabindex="-1" aria-labelledby="gradersModalLabel" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title" id="gradersModalLabel">Graders</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="rubric-graders-cell">
              ${graders.join("<br>")}
            </div>
          </div>
        </div>
      </div>
    </div>
  `;
};

$(function() {

  // selectedPeerCell is to mark original
  // newPeerCell is new selection if there's a save
  // start out by setting them the same
  $('.selectedPeerCell').each(function(e) {

    $(this).addClass("newPeerCell");
    let val = '';
    for (let i = 0; i < 5; i++) {
      if ($(this).hasClass(""+i)) {
        val = i;
      }
    }
    $(this).parents('.peer-eval-row').find('.peerReviewValue').text(val);
  });

  $('.selectedPeerCell').attr('aria-selected','true');

  $('.peer-eval-row td').click(function (e) {

    var row = $(this).parents('.peer-eval-row');
    var data = row.find('.peer-eval-row-data');
    if (data.size() == 0) {
      return;
    }
    const peerReviewId = row.find('.peerReviewId').text();
    const peerReviewTarget = $(this).parents('.peer-eval-target').find('.peer-eval-target-id').val();
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

  $('.cancel-peereval-link').click(function (e) {

    var form = $(this).parents('form');
    // put back original classes
    form.find('.newPeerCell').removeAttr('aria-selected');
    form.find('.newPeerCell').removeClass('newPeerCell');
    form.find('.selectedPeerCell').addClass("newPeerCell");
    form.find('.selectedPeerCell').attr('aria-selected', 'true');
    // kill anything set up for next save
    form.find('.peer-eval-row-data').val("");
    $('.selectedPeerCell').each(function(e) {

      let val = '';
      for (i = 0; i < 5; i++) {
        if ($(this).hasClass(""+i)) {
          val = i;
        }
      }
      $(this).parents('.peer-eval-row').find('.peerReviewValue').text(val);
    });
  });

  if ($(".studentContentType").length > 0) {
    //nextNumber has been negated...
    addEmptyCategoryRow = function (tableSelector, customId) {

      const nextNumber = (customId !== -1) ? customId : ($(tableSelector + " .newRow").length + 1) * -1;
      const newRow = (customId === -1) ? "newRow" : "";

      $(tableSelector + " tbody").append(`
        <tr>
          <td>
            <span class="rubricRowIndex ${newRow}" style="display:none;">${nextNumber}</span>
            ${blankRubricRow}
          </td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
          <td></td>
        </tr>
      `);
      $firstInput = $(tableSelector + " tbody tr:last input:first");
      $secondInput = $(tableSelector + " tbody tr:last input:last");
      $firstInput.attr("name", $firstInput.attr("name") + nextNumber);
      $secondInput.attr("name", $firstInput.attr("name") + "-fossil");
    };

    addCategoryRow = function (tableSelector, value, customId) {

      addEmptyCategoryRow(tableSelector, customId);
      $(tableSelector + " tbody tr:last input:first").val(value);
    };

    $("#addRowBtn").click(function () {
      addEmptyCategoryRow("#peer-eval-create-table", -1);
    });

    buildExistingRubrics = function (rubric) {

      $form = $(".peer-eval-create-form");
      $(".peer-eval-input-title").val(rubric.title);
      $("#peer-eval-create-table tbody tr").remove();
      for (let i = 0; i < rubric.rows.length; i++) {
        addCategoryRow("#peer-eval-create-table", rubric.rows[i].text, rubric.rows[i].id);
      }
    };

    deleteNotLast = function (btn) {

      if (document.querySelectorAll("#student-dialog .peer-eval-input-row").length > 1) {
        btn.closest("tr").remove();
      }
    };
  }
}); /* The above applies to the Peer Evaluation Setup (ShowPage.html) page. END */

/* The following applies to the Peer Evaluation Statistics (PeerEvalStats.html) page. BEGIN */

function countByGrader(grader) {

  let count = 0;

  $(".peer-eval-gradee-branch").each(function () {

    let countPerGradee = 0;
    $(".peer-eval-grader-id", this).each(function () {
      if ($(this).text() === grader) countPerGradee++;
    });
    if (countPerGradee > (numCategories / 2)) count++;
  });
  return count;
}

$(function() {

  if ($(".peer-eval-rubric").length > 0) {
    const CELL_SELECTED_BACKGROUND_COLOR = "rgb(177, 204, 235)";
    const activityMsg = document.getElementById("simplepage.user.activity").innerHTML;
    const ratingToolTip = document.getElementById("simplepage.peer-cell-tooltip").innerHTML;

    const gradees = [];

    getGradeeByUserId = function (userId) {

      //console.log('getGradeeByUserId(' + userId + ')');
      for (w in gradees) {
        if (gradees[w].userid === userId) return gradees[w];
      }
    };

    //Build the JSON Object.
    $(".peer-eval-gradee-branch").each(function () {

      const name = $(".user-name", this).text();
      const userid = $(".user-id", this).text();
      const pageid = $(".user-pageid", this).text();

      const grades = [];
      $(".peer-eval-data-grade", this).each(function () {

        const rowText = $(".peer-eval-row-text", this).text();
        const grade = $(".peer-eval-grade", this).text();
        const count = $(".peer-eval-count", this).text();
        const graders = [];
        $(".peer-eval-grader-branch", this).each(function () {

          const graderName = $(".peer-eval-grader-name", this).text();
          const id = $(".peer-eval-grader-id", this).text();
          graders.push({
            "id": id,
            "name": graderName
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
      const target = gradees[x].userid;
      gradees[x].activity = 0;

      $(".peer-eval-gradee-branch").each(function () {

        let countPerGradee = 0;
        $(".peer-eval-grader-id", this).each(function () {
          if ($(this).text() === target) countPerGradee++;
        });
        if (countPerGradee > (numCategories / 2)) gradees[x].activity++;
      });
    }

    //console.log(gradees);

    //Generate rubrics for each student.
    for (x in gradees) {
      const rubricsDiv = [];
      rubricsDiv.push('<div class="rubric-set"><div class="rubric-name">' + gradees[x].gradee);
      rubricsDiv.push('<span class="rubric-userid-div"> (<span class="rubric-userid">' + gradees[x].userid + '</span>) </span>');
      if (gradees[x].branch.find('.peer-eval-gradee-members').size() > 0) {
        rubricsDiv.push('<ul>');
        const graders = gradees[x].branch.find('.peer-eval-gradee-members');
        rubricsDiv.push(graders.html());
        rubricsDiv.push('</ul>');
      } else {
        rubricsDiv.push('<span role="link" class="rubric-activity">' + (activityMsg === undefined ? 'User Activity' : activityMsg) + ': ');
        rubricsDiv.push(gradees[x].activity + '</span>');
      }
      rubricsDiv.push('</div><div class="rubric-rubric">' + $(".peer-eval-rubric").html() + "</div></div>");
      $(".rubrics").append(rubricsDiv.join(""));
      for (y in gradees[x].grades) {
        const text = gradees[x].grades[y].rowText;
        const grade = gradees[x].grades[y].grade;
        const count = gradees[x].grades[y].count;

        let isValidGrade = false;
        $(".peer-eval-rubric-table:last .peer-eval-row").each(function () {

          if ($("td:first", this).text().trim() === text) {
            isValidGrade = true;
            const graderString = [];
            for (z in gradees[x].grades[y].graders) {
              graderString.push(gradees[x].grades[y].graders[z].name + '<span class="rubric-cell-grader-id">' + gradees[x].grades[y].graders[z].id + '</span>');
            }
            //$("." + grade, this).html('<span role="link" class="rating">&nbsp;' + count + '&nbsp;</span><div class="rubric-graders-cell">' + graderString.join("<br>") + "</div>");
            $("." + grade, this).html(`<span role="link" class="rating">&nbsp;${count}&nbsp;</span>${lessons.wrapGraderListInModal(graderString)}`);
          }
        });
        $(".rubric-graders-cell").parent().attr('title',ratingToolTip);
        if (!isValidGrade) {
          //The activity point should be removed from all of the graders who gave this grade to this gradee.
          for (z in gradees[x].grades[y].graders) {
            var grader = getGradeeByUserId(gradees[x].grades[y].graders[z].id);
            if (grader !== undefined) {
              //graders who do not have a page of their own will be undefined.
              grader.activity--;
            }
          }
        }
        //console.log(gradees[x]);
      }
    }

    //var activityDenom;
    //if(gradees.length>0){
    //  activityDenom=(gradees.length*$(".peer-eval-rubric .peer-eval-row").length)-$(".peer-eval-rubric .peer-eval-row").length;
    //}
    //console.log(activityDenom);
    $(".rubric-set").each(function () {
      //Show and hide student rubrics - onclick
      $(".rubric-name", this).click(function () {

        if ($(this).parent().find(".rubric-rubric").is(":visible")) {
          $(this).parent().find(".rubric-rubric").hide();
        } else {
          $(this).parent().find(".rubric-rubric").show();
        }
      });

      //Add updated activity
      //var activity = (getGradeeByUserId($(".rubric-userid", this).text()).activity / activityDenom)*100;
      //$(".rubric-name", this).append('<span class="rubric-activity">'+(activityMsg==undefined?'User Activity':activityMsg)+': '+activity+'%</span>');
    });

    //See graders - display the users who clicked on the particular grade
    //See graders - display the users who clicked on the particular grade
    document.querySelectorAll(".peer-eval-row .rating").forEach(el =>{

      el.addEventListener("click", e => {

        const modalEl = e.target.nextElementSibling;
        const modal = bootstrap.Modal.getOrCreateInstance(modalEl)
        modal.show();
      });
    });

    $('.inactive-member').each(function () {

      const count = countByGrader($(this).find('.inactive-member-id').text());
      $(this).find('.inactive-member-name').append(': ' + (activityMsg === undefined ? 'User Activity' : activityMsg) + ': ' + count);
    });

    //See activity - when a user clicks on "Activity", the cells the user clicked on will be highlighted.
    $(".rubric-activity , .inactive-member").each(function () {

      $(this).click(function () {

        $(this).parent().click(); //Click the parent again to undo this click's action.
        $(".rubric-graders-cell").each(function () {

          $(this).parent().css("background-color", "");
          $(this).parents('peer-eval-row').find('peer-eval-chosen').text('');
        });
        if ($(this).hasClass("activity-active")) {
          $(".activity-active").removeClass("activity-active");
        } else {
          $(this).addClass("activity-active");
          let userid = $(this).parent().find(".rubric-userid").text();
          if ($(this).hasClass("inactive-member")) {
            userid = $(this).find(".inactive-member-id").text();
          }
          $(".rubric-graders-cell").each(function () {

            $(this).parent().css("background-color", "");
            $(this).parents('peer-eval-row').find('peer-eval-chosen').text('');
            var htmlSS = $(this).html();
            //console.log(userid);
            if (htmlSS.indexOf(userid) !== -1) {
              $(this).parent().css("background-color", CELL_SELECTED_BACKGROUND_COLOR);
              let val = '';
              for (let i = 0; i < 5; i++) {
                if ($(this).parent().hasClass(""+i)) {
                  val = i;
                }
              }
              const person = $(this).closest('.rubric-set').find('.rubric-name').contents().first().text();
              $(this).closest('.peer-eval-row').find('.peer-eval-chosen').text(val);
              $(this).closest('.peer-eval-row').find('.peer-eval-person').text(person);
            }
          });
        }
      });
    });

    $(".expand-collapse-btn").click(function (e) {

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

  });

/* The above applies to the Peer Evaluation Statistics page. END */

/* The following applies to the Peer Evaluation display on the STUDENT pages (ShowPage.html). BEGIN */

