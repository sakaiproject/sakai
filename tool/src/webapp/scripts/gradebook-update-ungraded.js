/**************************************************************************************
 *                    Gradebook Update Ungraded Javascript                                      
 *************************************************************************************/

/**************************************************************************************
 * A GradebookUpdateUngraded to encapsulate all the update ungraded form behaviours 
 */
function GradebookUpdateUngraded($content) {
  this.$content = $content;

  this.setupExtraCreditCheck();
};

GradebookUpdateUngraded.prototype.setupExtraCreditCheck = function(){
  var self = this;

  function isExtraCreditValue() {
    var gradePoints = parseFloat(self.$content.find("#gradePoints").val());
    var updateValue = parseFloat(self.$content.find(".gb-update-ungraded-value").val());

    return updateValue > gradePoints;
  };

  function showConfirmation() {
      var $confirmationModal = $($("#extraCreditModalTemplate").html());
      $confirmationModal.on("click", ".gb-update-ungraded-extracredit-continue", function() {
        performRealSubmit();
      });
      $(document.body).append($confirmationModal);

      $confirmationModal.on("hidden.bs.modal", function() {
        $confirmationModal.remove();
        self.$content.find(".gb-update-ungraded-value").focus();
      });
      $confirmationModal.on("show.bs.modal", function() {
        var $formModal = self.$content.closest(".wicket-modal");
        $confirmationModal.css("marginTop", $formModal.offset().top + 40);
      });

      $confirmationModal.on("shown.bs.modal", function() {
        $confirmationModal.find(".gb-update-ungraded-extracredit-cancel").focus();
      });

      $confirmationModal.modal().modal('show');
  };


  function performRealSubmit() {
    self.$content.find(".gb-update-ungraded-real-submit").trigger("click");
  };


  function handleFakeSubmit(event) {
    event.preventDefault();
    event.stopPropagation();

    if (isExtraCreditValue()) {
      showConfirmation();
    } else {
      performRealSubmit();
    }

    return false;
  };

  this.$content.find(".gb-update-ungraded-fake-submit").off("click").on("click", handleFakeSubmit);
};
