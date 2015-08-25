/**************************************************************************************
 *                    Gradebook Grade Summary  Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookGradeSummary to encapsulate all the grade summary content behaviours 
 */
function GradebookGradeSummary($content, blockout) {
  this.$content = $content;

  this.blockout = blockout || false;

  this.studentId = this.$content.find("[data-studentid]").data("studentid");

  this.setupCategoryToggles();

  this.$modal = this.$content.closest(".wicket-modal");

  if (this.$modal.length > 0 && this.$modal.is(":visible")) {
    this.setupWicketModal();
  } else {
    setTimeout($.proxy(function() {
      this.$modal = this.$content.closest(".wicket-modal");
      this.setupWicketModal();
    }, this));
  }
};


GradebookGradeSummary.prototype.setupWicketModal = function() {
    this.setupTabs();
    this.setupStudentNavigation();
    this.setupFixedFooter();
    this.setupMask();
    this.bindModalClose();
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
    var paddingSize = 150; // modal padding and modal content padding/margins (yep... fudged)
    var newHeight = $(window).height() - this.$content.offset().top - this.$content.find("h2").outerHeight() - this.$content.find(".nav").outerHeight() - this.$content.find(".gb-summary-modal-actions").outerHeight() - paddingSize;
    $tabPane.height(Math.max(200, newHeight));
  }
};


GradebookGradeSummary.prototype.setupMask = function() {
  var $mask = this.$modal.siblings(".wicket-mask-transparent, .wicket-mask-dark");
  if (this.blockout) {
    $mask.removeClass("wicket-mask-transparent").addClass("wicket-mask-dark");
  } else {
    $mask.removeClass("wicket-mask-dark").addClass("wicket-mask-transparent");
  }
};


GradebookGradeSummary.prototype.bindModalClose = function() {
  var self = this;

  if (self.blockout) {
    self.$content.find(".gb-summary-fake-close").show();
    self.$content.find(".gb-summary-close").hide();
  } else {
    self.$content.find(".gb-summary-fake-close").hide();
    self.$content.find(".gb-summary-close").show();
  }

  function showConfirmation(event) {
    if (self.blockout) {
      event.preventDefault();
      event.stopPropagation();

      var $confirmationModal = $($("#studentGradeSummaryCloseConfirmationTemplate").html());
      $confirmationModal.on("click", ".btn-student-summary-continue", function() {
        self.$modal.find(".gb-summary-close").trigger("click");
      });
      $(document.body).append($confirmationModal);
      $confirmationModal.modal().modal('show');
      $confirmationModal.on("hidden.bs.modal", function() {
        $confirmationModal.remove();
      });

      return false;
    } else {
      if ($(this).data("clickCallback")) {
        $(this).data("clickCallback")();
      }

      return true;
    }
  }

  self.$modal.find(".w_close, .gb-summary-fake-close").each(function() {
    if (this.onclick) {
      $(this).data("clickCallback", this.onclick);
      this.onclick = null;
    }
  });

  self.$modal.find(".w_close, .gb-summary-fake-close").off("click").on("click", showConfirmation);
};