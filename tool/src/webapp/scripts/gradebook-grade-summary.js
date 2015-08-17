/**************************************************************************************
 *                    Gradebook Grade Summary  Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookGradeSummary to encapsulate all the grade summary content behaviours 
 */
function GradebookGradeSummary($content, darkerMask) {
  this.$content = $content;

  this.studentId = this.$content.find("[data-studentid]").data("studentid");

  this.setupTabs();
  this.setupCategoryToggles();
  this.setupStudentNavigation();

  this.$modal = this.$content.closest(".wicket-modal");

  if (this.$modal.length > 0 && this.$modal.is(":visible")) {
    this.setupFixedFooter();
    this.setupMask(darkerMask);
  } else {
    setTimeout($.proxy(function() {
      this.$modal = this.$content.closest(".wicket-modal");
      this.setupFixedFooter();
    }, this));
  }
};


GradebookGradeSummary.prototype.setupTabs = function() {
  this.$content.find('#studentGradeSummaryTabs a').click(function (e) {
    e.preventDefault()
    $(this).tab('show')
  });
};


GradebookGradeSummary.prototype.setupCategoryToggles = function() {
  this.$content.find(".gb-summary-category-toggle").click(function() {
    var $toggle = $(this);
    if ($toggle.is(".collapsed")) {
      $toggle.closest("tbody").find(".gb-summary-grade-row").show();
    } else {
      $toggle.closest("tbody").find(".gb-summary-grade-row").hide();
    }
    $toggle.toggleClass("collapsed");
  });

  this.$content.find(".gb-summary-expand-all").click(function() {
    $(".gb-summary-category-toggle.collapsed").trigger("click");
  });
  this.$content.find(".gb-summary-collapse-all").click(function() {
    $(".gb-summary-category-toggle:not(.collapsed)").trigger("click");
  });
};


GradebookGradeSummary.prototype.setupStudentNavigation = function() {
  var $showPrevious = this.$content.find(".gb-summary-previous-student");
  var $showNext = this.$content.find(".gb-summary-next-student");
  var $done = this.$content.find(".gb-summary-close");

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


GradebookGradeSummary.prototype.setupFixedFooter = function() {
  // do this by setting the height of the tab content to leave room for the navigation
  if (this.$modal.height() > $(window).height()) {
    var $tabPane = this.$content.find(".tab-content");
    var paddingSize = 80; // modal padding and modal content padding/margins (yep... fudged)
    var newHeight = $(window).height() - this.$content.offset().top - this.$content.find("h2").outerHeight() - this.$content.find(".nav").outerHeight() - this.$content.find(".gb-summary-modal-actions").outerHeight() - paddingSize;
    $tabPane.height(Math.max(200, newHeight));
  }
};


GradebookGradeSummary.prototype.setupMask = function(darkerMask) {
  var $mask = this.$modal.siblings(".wicket-mask-transparent, .wicket-mask-dark");
  if (darkerMask) {
    $mask.removeClass("wicket-mask-transparent").addClass("wicket-mask-dark");
  } else {
    $mask.removeClass("wicket-mask-dark").addClass("wicket-mask-transparent");
  }
};