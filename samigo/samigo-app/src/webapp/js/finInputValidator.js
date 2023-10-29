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

document.addEventListener('DOMContentLoaded', function() {
  const fillInNumericInputs = document.querySelectorAll('.fillInNumericInput');
  const takeAssessmentForm = document.getElementById('takeAssessmentForm');
  
  fillInNumericInputs.forEach(input => {
    input.dataset.bsToggle = 'popover';
    input.dataset.bsContent = finFormatError;
    input.dataset.bsTrigger = 'focus';
  });

  takeAssessmentForm.addEventListener('submit', function(event) {
    fillInNumericInputs.forEach(input => {
      !validateFinInput(input) && event.preventDefault();
    });
  });

  fillInNumericInputs.forEach(input => {
    input.addEventListener('shown.bs.popover', function () {
      setTimeout(() => {
        const popover = new bootstrap.Popover(input);
        popover.hide();
      }, 3000);
    });

    input.addEventListener('change', function() {
      validateFinInput(input) && bootstrap.Popover.getOrCreateInstance(input).dispose();
    });

    input.addEventListener('input', function() {
      if (input.value &&
        (input.value.includes('+') ||
        input.value.includes('-') ||
        input.value.includes('{') ||
        input.value.includes('}') ||
        input.value.includes('e') ||
        input.value.includes('E')
        )
      ) {
        return;
      }
      validateFinInput(input) && bootstrap.Popover.getOrCreateInstance(input).dispose();
    });
  });
});

function validateFinInput(input) {
  if (!input.value) {
    return true;
  }

  const rawInput = input.value.replace(/,/g, '.');
  input.value = rawInput;
  let isValidFinInput = true;
  const numericInputValue = rawInput;
  const complexInputValue = [];
  rawInput.replace(/\{(.+?)\}/g, function(_, m) {
    complexInputValue.push(m);
  });

  if (complexInputValue.length !== 0) {
    try {
      const complexNumber = math.complex(complexInputValue);
    } catch (error) {
      console.debug('The inserted complex number is not valid, please review the syntax. e.g., {8.5 + 9.4i}');
      isValidFinInput = false;
    }
  } else {
    try {
      const numericNumber = math.add(numericInputValue, 0.0);
    } catch (error) {
      console.debug('The inserted value is not numeric, please review the syntax. e.g., 1.5, 9, -4, -3.1415');
      isValidFinInput = false;
    }
  }

  if (!isValidFinInput) {
    input.value = '';
    alert(finFormatError);
    return false;
  }

  return true;
}
