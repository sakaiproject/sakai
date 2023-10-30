window.addEventListener('load', () => {
  document.querySelectorAll('.hasTooltip').forEach(tooltip => {
    const content = tooltip.nextElementSibling;
    new bootstrap.Tooltip(tooltip, {
      title: content,
      placement: 'top',
      trigger: 'click',
    });
  });
});

includeWebjarLibrary('mathjs');

document.addEventListener('DOMContentLoaded', () => {
  const fillInNumericInputs = document.querySelectorAll('.fillInNumericInput');
  
  fillInNumericInputs.forEach(input => {
    input.setAttribute('data-bs-toggle', 'popover');
    input.setAttribute('data-bs-content', finFormatError);
    input.setAttribute('data-bs-trigger', 'focus');
    
    const popover = new bootstrap.Popover(input, {
      trigger: 'focus',
    });

    input.addEventListener('change', () => {
      if (validateFinInput(input)) {
        popover.hide();
      }
    });

    input.addEventListener('keyup', throttle(() => {
      if (input.value && !['+', '-', '{', '}', 'e', 'E'].some(v => input.value.includes(v))) {
        if (validateFinInput(input)) {
          popover.hide();
        }
      }
    }));
  });

  document.querySelector('#takeAssessmentForm').addEventListener('submit', event => {
    event.preventDefault();
    fillInNumericInputs.forEach(input => {
      validateFinInput(input);
    });
  });
});

const validateFinInput = input => {
  if (!input.value) return true;

  let rawInput = input.value.replace(/,/g, '.').replace(/\s/g, '');
  input.value = rawInput;
  let isValidFinInput = true;
  
  if (rawInput.includes('{')) {
    try {
      math.complex(rawInput.match(/\{(.+?)\}/g));
    } catch {
      console.debug('Invalid complex number syntax.');
      isValidFinInput = false;
    }
  } else {
    try {
      math.add(rawInput, 0.0);
    } catch {
      console.debug('Invalid numeric value syntax.');
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

const throttle = (func, delay = 200) => {
  let timer = null;
  return (...args) => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      func.apply(this, args);
    }, delay);
  };
};
