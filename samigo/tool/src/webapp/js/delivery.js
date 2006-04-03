<%-- JAVSCRIPT FOR DISABLING SHOWFEEDBACK AND TOC AFTER ONE CLICK --%>
<script language="javascript" style="text/JavaScript">
<!--

function printForm(){
  alert("print 0");
  for(i=0; i<document.forms[0].elements.length; i++)
  {
    alert("The field name is: " + document.forms[0].elements[i].name); 
  }
}

function printLink(){
  for (var i=0; i < document.links.length; i++){
    alert(document.links[i].id);
  }
}

var disabledFeedback = 'false';
function disableFeedback(){
  if (disabledFeedback == 'false'){
    disabledFeedback = 'true'
    for (var i=0; i < document.links.length; i++){
      if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showTOC')
        document.links[i].disabled = true;
    }
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    for (var i=0; i < document.links.length; i++){
      if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showFeedback')
        document.links[i].disabled = true;
    }
  }
}

var disabledTOC = 'false';
function disableTOC(){
  if (disabledTOC == 'false'){
    disabledTOC = 'true'
    for (var i=0; i < document.links.length; i++){
      if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showFeedback')
        document.links[i].disabled = true;
    }
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    for (var i=0; i < document.links.length; i++){
      if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showTOC')
        document.links[i].disabled = true;
    }
  }
}

function disableTOCFeedback(){
  for (var i=0; i < document.links.length; i++){
    if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showTOC')
      document.links[i].disabled = true;
    if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showFeedback')
      document.links[i].disabled = true;
  }
}

var nextDisabled = 'false';
function disableNext(){
  if (nextDisabled == 'false'){
    nextDisabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
  }
}

var previousDisabled = 'false';
function disablePrevious(){
  if (previousDisabled == 'false'){
    previousDisabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
  }
}

var submitDisabled = 'false';
function disableSubmit(){
  if (submitDisabled == 'false'){
    submitDisabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
  }
}

var submit2Disabled = 'false';
function disableSubmit2(){
  if (submit2Disabled == 'false'){
    submit2Disabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
  }
}

var saveDisabled = 'false';
function disableSave(){
  if (saveDisabled == 'false'){
    saveDisabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
  }
}

var saveDisabled2 = 'false';
function disableSave2(){
  if (saveDisabled2 == 'false'){
    saveDisabled2 = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
  }
}

var quitDisabled = 'false';
function disableQuit(){
  if (quitDisabled == 'false'){
    quitDisabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
  }
}


var submitForGradeDisabled = 'false';
function disableSubmitForGrade(){
  if (submitForGradeDisabled == 'false'){
    submitForGradeDisabled = 'true'
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm2'])
      document.forms[0].elements['takeAssessmentForm:submitForm2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
}
//-->
</script>

