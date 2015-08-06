/**************************************************************************************
 *                    Gradebook Grade Summary  Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookGradeSummary to encapsulate all the grade summary content behaviours 
 */
function GradebookGradeSummary($modal) {
  this.$modal = $modal;

  this.studentId = this.$modal.find("[data-studentid]").data("studentid");

  this.setupTabs();
  this.setupCategoryToggles();
  this.setupStudentNavigation();
};


GradebookGradeSummary.prototype.setupTabs = function() {
  this.$modal.find('#studentGradeSummaryTabs a').click(function (e) {
    e.preventDefault()
    $(this).tab('show')
  });
};


GradebookGradeSummary.prototype.setupCategoryToggles = function() {
  this.$modal.find(".gb-summary-category-toggle").click(function() {
    var $toggle = $(this);
    if ($toggle.is(".collapsed")) {
      $toggle.closest("tbody").find(".gb-summary-grade-row").show();
    } else {
      $toggle.closest("tbody").find(".gb-summary-grade-row").hide();
    }
    $toggle.toggleClass("collapsed");
  });

  this.$modal.find(".gb-summary-expand-all").click(function() {
    $(".gb-summary-category-toggle.collapsed").trigger("click");
  });
  this.$modal.find(".gb-summary-collapse-all").click(function() {
    $(".gb-summary-category-toggle:not(.collapsed)").trigger("click");
  });
};


GradebookGradeSummary.prototype.setupStudentNavigation = function() {
  var $showPrevious = this.$modal.find(".gb-summary-previous-student");
  var $showNext = this.$modal.find(".gb-summary-next-student");
  var $done = this.$modal.find(".gb-summary-close");

  var $previous = sakai.gradebookng.spreadsheet.findVisibleStudentBefore(this.studentId);
  var $next = sakai.gradebookng.spreadsheet.findVisibleStudentAfter(this.studentId);

  if ($previous) {
    $showPrevious.click(function() {
      $previous.find("a.gb-student-label").trigger("click");
    });
  } else {
    $showPrevious.hide();
  }

  if ($next) {
    $showNext.click(function() {
      $next.find("a.gb-student-label").trigger("click");
    });    
  } else {
    $showNext.hide();
  }
};