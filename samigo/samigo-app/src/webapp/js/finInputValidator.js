document.addEventListener('DOMContentLoaded', () => {

  if (typeof finFormatError === 'undefined') {
    throw new Error('finFormatError is not defined');
  }
  
  setupTooltips();
  setupFinInputs();
  setupFormSubmission();
});

function setupTooltips() {
  document.querySelectorAll('.hasTooltip').forEach(tooltipTriggerEl => {
    new bootstrap.Tooltip(tooltipTriggerEl, {
      trigger: 'click',
      placement: 'auto'
    });
  });
}

function setupFinInputs() {
  const finInputs = document.querySelectorAll('.fillInNumericInput');

  const handleInput = input => {
    if (validateFinInput(input, finFormatError)) {
      const popoverInstance = bootstrap.Popover.getInstance(input);
      popoverInstance?.dispose();
    }
  };

  finInputs.forEach(input => {
    new bootstrap.Popover(input, {
      trigger: 'focus',
      content: finFormatError
    });

    input.addEventListener('change', () => handleInput(input));
    input.addEventListener('keyup', throttle(() => handleInput(input), 200));
  });
}

function setupFormSubmission() {
  const form = document.getElementById('takeAssessmentForm');
  form?.addEventListener('submit', event => {
    const finInputs = document.querySelectorAll('.fillInNumericInput');
    let isFormValid = Array.from(finInputs).every(validateFinInput);

    if (!isFormValid) {
      event.preventDefault();
    }
  });
}

const validateFinInput = (input, finFormatError) => {
  if (!input.value) {
    return true;
  }

  let rawInput = input.value.replace(/[,\s]+/g, '.');
  input.value = rawInput;

  if (!isFinite(rawInput)) {
    console.debug('Invalid numeric input detected.');
    input.value = '';
    alert(finFormatError);
    return false;
  }

  return true;
};

const throttle = (func, limit) => {
  let inThrottle;
  return function() {
    const context = this, args = arguments;
    if (!inThrottle) {
      func.apply(context, args);
      inThrottle = true;
      setTimeout(() => inThrottle = false, limit);
    }
  };
};
