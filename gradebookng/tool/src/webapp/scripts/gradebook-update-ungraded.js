/**
 * Gradebook Update Ungraded Javascript
 * Modern implementation without jQuery dependencies
 */

class GradebookUpdateUngraded {
  constructor(contentElement, enableInputs) {
    // Handle both jQuery objects and DOM elements
    this.content = contentElement instanceof jQuery ? contentElement[0] : contentElement;
    
    if (!this.content || !(this.content instanceof Element)) {
      throw new Error('GradebookUpdateUngraded requires a valid DOM element or jQuery object');
    }

    this.setupConfirmation();
    
    // Enable inputs if requested
    if (enableInputs) {
      this.content.querySelectorAll('input, select, button').forEach(input => {
        input.disabled = false;
      });
    }
  }

  isExtraCreditValue() {
    const gradePoints = parseFloat(this.content.querySelector('#gradePoints').value);
    const updateValue = parseFloat(this.content.querySelector('.gb-update-ungraded-value').value);
    return updateValue > gradePoints;
  }

  buildConfirmationModal() {
    const score = this.content.querySelector('.gb-update-ungraded-value').value;
    const group = this.content.querySelector('.gb-update-ungraded-group').selectedOptions[0].textContent.trim();
    const isExtraCredit = this.isExtraCreditValue();

    // Get the template from the DOM
    const templateElement = document.getElementById('updateUngradedConfirmationModalTemplate');
    if (!templateElement) {
      console.error('Modal template not found in the DOM');
      return null;
    }

    // Use TrimPath to process the template
    const templateHtml = templateElement.innerHTML.trim();
    const modalTemplate = TrimPath.parseTemplate(templateHtml);
    const processedHtml = modalTemplate.process({
      score,
      group,
      isExtraCredit
    });

    // Create the modal element
    const modalElement = document.createElement('div');
    modalElement.innerHTML = processedHtml;
    
    // Ensure proper z-index stacking for nested modals
    const firstModal = document.querySelector('.modal');
    if (firstModal) {
      const currentZIndex = parseInt(window.getComputedStyle(firstModal).zIndex, 10);
      modalElement.style.zIndex = (currentZIndex + 10).toString();
    }

    return modalElement.firstElementChild;
  }

  showConfirmation() {
    const modalElement = this.buildConfirmationModal();
    if (!modalElement) return;
    
    document.body.appendChild(modalElement);

    // Configure Bootstrap modal with nested modal options
    const modal = new bootstrap.Modal(modalElement, {
      backdrop: 'static', // Prevent clicking outside to close when nested
      keyboard: true // Allow escape key
    });
    
    // Store reference to parent modal
    const parentModal = this.content.closest('.wicket-modal');
    const parentBootstrapModal = parentModal ? bootstrap.Modal.getInstance(parentModal) : null;
    
    modalElement.addEventListener('hidden.bs.modal', () => {
      modalElement.remove();
      // Return focus to the group selector in parent modal
      this.content.querySelector('.gb-update-ungraded-group').focus();
    });

    modalElement.addEventListener('show.bs.modal', () => {
      if (parentModal) {
        // Adjust margin to position relative to parent modal
        modalElement.style.marginTop = `${parentModal.offsetTop + 40}px`;
      }
    });

    modalElement.addEventListener('shown.bs.modal', () => {
      // Set focus to cancel button
      modalElement.querySelector('.gb-update-ungraded-cancel').focus();
    });

    // Handle escape key manually to ensure proper modal stacking
    modalElement.addEventListener('keydown', (event) => {
      if (event.key === 'Escape') {
        event.stopPropagation(); // Prevent parent modal from catching escape
        modal.hide();
      }
    });

    modalElement.querySelector('.gb-update-ungraded-continue').addEventListener('click', () => {
      this.performRealSubmit();
      modal.hide();
    });

    modal.show();
  }

  performRealSubmit() {
    const realSubmitButton = this.content.querySelector('.gb-update-ungraded-real-submit');
    realSubmitButton.click();
    this.content.querySelectorAll('input, select, button').forEach(input => {
      input.disabled = true;
    });
  }

  setupConfirmation() {
    const fakeSubmitButton = this.content.querySelector('.gb-update-ungraded-fake-submit');
    
    const handleFakeSubmit = (event) => {
      event.preventDefault();
      event.stopPropagation();
      this.showConfirmation();
      return false;
    };

    // Remove any existing listeners and add the new one
    fakeSubmitButton.replaceWith(fakeSubmitButton.cloneNode(true));
    this.content.querySelector('.gb-update-ungraded-fake-submit')
      .addEventListener('click', handleFakeSubmit);
  }
}

// Export for module systems
if (typeof module !== 'undefined' && module.exports) {
  module.exports = GradebookUpdateUngraded;
}
