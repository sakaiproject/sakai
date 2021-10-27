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

$( document ).ready(function() {

  $('.fillInNumericInput').each( function() {
    $(this).attr('data-toggle', 'popover'); 
    $(this).attr('data-content', finFormatError);
    $(this).attr('data-trigger', 'focus');
  });

  $('#takeAssessmentForm').submit(function() {
    $('.fillInNumericInput').each(function() {
      //If a part or an exam is submitted, validate all the FIN inputs and alert about the invalid ones to prevent a response loss.
      validateFinInput(this);
    });
  });

  $('.fillInNumericInput').popover({
    trigger: 'focus'
  });

  $('.fillInNumericInput').change( function() {
    if (validateFinInput(this)) {
      $(this).popover('destroy');
    }
  });

  $('.fillInNumericInput').keyup( throttle(function(){
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
    if (validateFinInput(this)) {
      $(this).popover('destroy');
    }
  }));

});

var validateFinInput = function(input) {
  if (!input.value) {
    //Empty inputs are accepted.
    return true;
  }
  //Replace the comma decimal separator as point, the JSF validator does the same and all the JS libraries work with point as decimal separator.
  var rawInput = input.value.replace(/,/g, '.');
  //Replace all the whitespaces.
  rawInput = rawInput.replace(/\s/g,'');
  input.value = rawInput;
  var isValidFinInput = true;
  var numericInputValue = rawInput;
  var complexInputValue = [];
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

}

function throttle(f, delay) {
  var timer = null;
  return function() {
   var context = this, args = arguments;
   clearTimeout(timer);
   timer = window.setTimeout(function() {
     f.apply(context, args);
   }, delay || 200);
  };
}
