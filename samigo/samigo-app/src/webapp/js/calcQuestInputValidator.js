includeWebjarLibrary('mathjs');

$( document ).ready(function() {

  $('.calculatedQuestionInput').each( function() {
    $(this).attr('data-toggle', 'popover'); 
    $(this).attr('data-content', calcqFormatError);
    $(this).attr('data-trigger', 'focus');
  });

  $('#takeAssessmentForm').submit(function() {
    $('.calculatedQuestionInput').each(function() {
      //If a part or an exam is submitted, validate all the FIN inputs and alert about the invalid ones to prevent a response loss.
      validateCalculatedQuestionInput(this);
    });
  });

  $('.calculatedQuestionInput').popover({
    trigger: 'focus'
  });

  $('.calculatedQuestionInput').change( function() {
    validateCalculatedQuestionInput(this);
  });

  $('.calculatedQuestionInput').keyup( throttle(function(){
    // Do not validate on key up when the user is inserting a scientific notation or a real with sign.
    if (this.value !== '' && 
        (this.value.includes('+') ||
        this.value.includes('-') ||
        this.value.includes('e') ||
        this.value.includes('E'))
    ) {
        return;
    }
    validateCalculatedQuestionInput(this);
  }));

});

// same as validateFinInput but don't accept complex numbers
var validateCalculatedQuestionInput = function(input) {
  if (!input.value) {
    //Empty inputs are accepted.
    return true;
  }
  //Replace the comma decimal separator as point.
  var rawInput = input.value.replace(/,/g, '.');
  //Replace all the whitespaces.
  rawInput = rawInput.replace(/\s/g,'');
  input.value = rawInput;

  var isValidInput = true;
  var numericInputValue = rawInput;
    // If not complex, lets check if is numeric.
    try {
      //Simple as that, try to add 0.0 to a numeric value.
      const numericNumber = math.add(numericInputValue, 0.0); 
    } catch(error) {
      console.debug('The inserted value is not numeric, please review the syntax. eg: 1.5 , 9, -4, -3.1415');
      isValidInput = false;
    }

  if (!isValidInput) {
    input.value = '';
    alert(calcqFormatError);
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
