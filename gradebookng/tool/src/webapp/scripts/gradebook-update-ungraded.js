/**************************************************************************************
 *                    Gradebook Update Ungraded Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookUpdateUngraded to encapsulate all the update ungraded form behaviours 
 */
function GradebookUpdateUngraded($content) {
  this.$content = $content;

  this.setupConfirmation();
};

GradebookUpdateUngraded.prototype.setupConfirmation = function(){
  var self = this;

  function isExtraCreditValue() {
    var gradePoints = parseFloat(self.$content.find("#gradePoints").val());
    var updateValue = parseFloat(self.$content.find(".gb-update-ungraded-value").val());

    return updateValue > gradePoints;
  };

  function buildConfirmationModal() {
    var templateHtml = $("#updateUngradedConfirmationModalTemplate").html().trim();
    var modalTemplate = TrimPath.parseTemplate(templateHtml);
    return $(modalTemplate.process({
      score: self.$content.find(".gb-update-ungraded-value").val(),
      group: self.$content.find(".gb-update-ungraded-group :selected").text().trim(),
      isExtraCredit: isExtraCreditValue()
    }));
  };

  function showConfirmation() {
      var $confirmationModal = buildConfirmationModal();

      $confirmationModal.one("click", ".gb-update-ungraded-continue", function() {
        performRealSubmit();
      });
      $(document.body).append($confirmationModal);

      $confirmationModal.on("hidden.bs.modal", function() {
        $confirmationModal.remove();
        self.$content.find(".gb-update-ungraded-group").focus();
      });
      $confirmationModal.on("show.bs.modal", function() {
        var $formModal = self.$content.closest(".wicket-modal");
        $confirmationModal.css("marginTop", $formModal.offset().top + 40);
      });

      $confirmationModal.on("shown.bs.modal", function() {
        $confirmationModal.find(".gb-update-ungraded-cancel").focus();
      });

      $confirmationModal.modal().modal('show');
  };


  function performRealSubmit() {
    self.$content.find(".gb-update-ungraded-real-submit").trigger("click");
    self.$content.find(":input").prop("disabled", true);
  };


  function handleFakeSubmit(event) {
    event.preventDefault();
    event.stopPropagation();

    showConfirmation();

    return false;
  };

  this.$content.find(".gb-update-ungraded-fake-submit").off("click").on("click", handleFakeSubmit);
};
