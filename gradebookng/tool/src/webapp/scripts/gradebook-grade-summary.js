/**************************************************************************************
 *                    Gradebook Grade Summary  Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookGradeSummary to encapsulate all the grade summary content behaviours 
 */
function GradebookGradeSummary($content, blockout, modalTitle) {
  this.$content = $content;

  this.blockout = blockout || false;

  this.modalTitle = modalTitle || false;

  this.studentId = this.$content.find("[data-studentid]").data("studentid");

  this.setupCategoryToggles();
  this.setupPopovers();

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
    this.updateTitle();
    this.setupTabs();
    this.setupStudentNavigation();
    this.setupFixedFooter();
    this.setupMask();
    this.bindModalClose();
};


GradebookGradeSummary.prototype.updateTitle = function() {
  if (this.modalTitle) {
    this.$modal.find("h3.w_captionText").html(this.modalTitle);
  }
};


GradebookGradeSummary.prototype.setupTabs = function() {
  // if blockout, then confirmation required when changing tabs
  if (this.blockout) {
    var $otherTab = this.$content.find(".nav.nav-tabs li:not(.active) a");
    var $cloneOfTab = $otherTab.clone();

    $otherTab.hide();
    $cloneOfTab.attr("href", "javascript:void(0)").removeAttr("id");
    $cloneOfTab.insertAfter($otherTab);
    $cloneOfTab.click(function(event) {
      event.stopPropagation();

      var $confirmationModal = $($("#studentGradeSummaryCloseConfirmationTemplate").html());
      $confirmationModal.on("click", ".btn-student-summary-continue", function() {
        $otherTab.trigger("click");
      });
      $(document.body).append($confirmationModal);
      $confirmationModal.modal().modal('show');
      $confirmationModal.on("hidden.bs.modal", function() {
        $confirmationModal.remove();
        $cloneOfTab.focus();
      });
      $confirmationModal.on("shown.bs.modal", function() {
        $confirmationModal.find(".btn-student-summary-cancel").focus();
      });
    });
  }
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
    var $contentPane =  this.$content.find(".gb-grade-summary-content");

    var paddingSize = 160; // modal padding and modal content padding/margins (yep... fudged)

    var height = $tabPane.height() - (this.$modal.height() - $(window).height()) - ($contentPane.height() - this.$modal.height()) - paddingSize;

    $tabPane.height(Math.max(200, height));
  }
};


GradebookGradeSummary.prototype.setupMask = function() {
  var $mask = this.$modal.siblings(".wicket-mask-transparent, .wicket-mask-dark");
  if (this.blockout) {
    // Darken the mask
    $mask.removeClass("wicket-mask-transparent").addClass("wicket-mask-dark");
    // Add a blur effect to the main page container
    $("#pageBody").addClass("gb-blur");
  } else {
    $mask.removeClass("wicket-mask-dark").addClass("wicket-mask-transparent");
    GradebookGradeSummaryUtils.clearBlur();
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
        self.$content.find(".gb-summary-fake-close").focus();
      });
      $confirmationModal.on("shown.bs.modal", function() {
        $confirmationModal.find(".btn-student-summary-cancel").focus();
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


GradebookGradeSummary.prototype.setupPopovers = function() {
  this.$content.find('[data-toggle="popover"]').popover();
};


var GradebookGradeSummaryUtils = {
  clearBlur: function() {
    $(".gb-blur").removeClass("gb-blur");
  }
};
