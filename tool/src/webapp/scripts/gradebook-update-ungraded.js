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
        self.$content.find(".gb-update-ungraded-real-submit").trigger("click");
      });
      $(document.body).append($confirmationModal);
      $confirmationModal.modal().modal('show');
      $confirmationModal.on("hidden.bs.modal", function() {
        $confirmationModal.remove();
      });
  };

  function handleSubmit(event) {
    if (isExtraCreditValue()) {
      event.preventDefault();
      event.stopPropagation();

      showConfirmation();

      return false;
    } else {
      return true;
    }
  };

  this.$content.find(".gb-update-ungraded-fake-submit").click(handleSubmit);
};
