//Setup qtips
window.onload = function() {
	$('.hasTooltip').each(function() { // Notice the .each() loop, discussed below
	    $(this).qtip({
	        content: {
	            text: $(this).next('div') // Use the "div" element after this for the content
	        },
	        position: {
	          target: 'mouse',
	          adjust: {
	            mouse: false
	          }
	       },
	       style: {
	         classes: 'qtip-tipped qtip-shadow qtipBodyContent',
	       },
	       show: 'click',
	       hide: 'unfocus click'
	      });
	});
};

includeWebjarLibrary('mathjs');

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
    input.addEventListener('keyup', throttle(function(){
      // Do not validate on key up when the user is inserting a complex number or scientific notation or a real with sign.
      if (this.value !== '' &&
          (this.value.includes('+') ||
          this.value.includes('-') ||
          this.value.includes('{') ||
          this.value.includes('}') ||
          this.value.includes('e') ||
          this.value.includes('E'))
      ) {
          return;
      }

      handleInput(input);
    }, 200));
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

  //Replace the comma decimal separator as point, the JSF validator does the same and all the JS libraries work with point as decimal separator.
  let rawInput = input.value.replace(/,/g, '.');
  //Replace all the whitespaces.
  rawInput = rawInput.replace(/\s/g,'');
  input.value = rawInput;
  let isValidFinInput = true;
  let numericInputValue = rawInput;
  let complexInputValue = [];
  rawInput.replace(/\{(.+?)\}/g, function(_, m) {complexInputValue.push(m)} );
  // Check if the number is complex first
  if (complexInputValue != '') {
    try{
      //Parsing relies on MathJS https://mathjs.org
      const complexNumber = math.complex(complexInputValue);
    } catch(error) {
      console.debug('The inserted complex number is not valid, please review the syntax. eg: {8.5 + 9.4i}');
      isValidFinInput = false;
    }
  } else {
    // If not complex, lets check if is numeric.
    try {
      //Simple as that, try to add 0.0 to a numeric value.
      const numericNumber = math.add(numericInputValue, 0.0);
    } catch(error) {
      console.debug('The inserted value is not numeric, please review the syntax. eg: 1.5 , 9, -4, -3.1415');
      isValidFinInput = false;
    }
  }

  if (!isValidFinInput) {
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
