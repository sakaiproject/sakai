<%-- JAVSCRIPT FOR DISABLING SHOWFEEDBACK AND TOC AFTER ONE CLICK 
Note that this will be embedded in the page exactly as is
--%><!-- Samigo embedded delivery.js starts here -->
<script type="text/javascript">

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

var beginAssessment1Disabled = 'false';
function disableBeginAssessment1(){
  if (beginAssessment1Disabled === 'false'){
    beginAssessment1Disabled = 'true';
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment2'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment3'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment3'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel1'])
      document.forms[0].elements['takeAssessmentForm:cancel1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel2'])
      document.forms[0].elements['takeAssessmentForm:cancel2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment1'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment1'].disabled=true;
  }
}


var beginAssessment2Disabled = 'false';
function disableBeginAssessment2(){
  if (beginAssessment2Disabled === 'false'){
    beginAssessment2Disabled = 'true';
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment1'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment3'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment3'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel1'])
      document.forms[0].elements['takeAssessmentForm:cancel1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel2'])
      document.forms[0].elements['takeAssessmentForm:cancel2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment2'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment2'].disabled=true;
  }
}

var beginAssessment3Disabled = 'false';
function disableBeginAssessment3(){
  if (beginAssessment3Disabled === 'false'){
    beginAssessment3Disabled = 'true';
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment1'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment2'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel1'])
      document.forms[0].elements['takeAssessmentForm:cancel1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel2'])
      document.forms[0].elements['takeAssessmentForm:cancel2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment3'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment3'].disabled=true;
  }
}

var cancel1Disabled = 'false';
function disableCancel1(){
  if (cancel1Disabled === 'false'){
    cancel1Disabled = 'true';
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment1'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment2'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment3'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment3'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel2'])
      document.forms[0].elements['takeAssessmentForm:cancel2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['takeAssessmentForm:cancel1'])
      document.forms[0].elements['takeAssessmentForm:cancel1'].disabled=true;
  }
}

var cancel2Disabled = 'false';
function disableCancel2(){
  if (cancel2Disabled === 'false'){
    cancel2Disabled = 'true';
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment1'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment2'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:beginAssessment3'])
      document.forms[0].elements['takeAssessmentForm:beginAssessment3'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:cancel1'])
      document.forms[0].elements['takeAssessmentForm:cancel1'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['takeAssessmentForm:cancel2'])
      document.forms[0].elements['takeAssessmentForm:cancel2'].disabled=true;
  }
}

var submitForGradeTOC1Disabled = 'false';
function disableSubmitForGradeTOC1(){
  if (submitForGradeTOC1Disabled === 'false'){
    submitForGradeTOC1Disabled = 'true';
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:exitTOC1'])
      document.forms[0].elements['tableOfContentsForm:exitTOC1'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:exitTOC2'])
      document.forms[0].elements['tableOfContentsForm:exitTOC2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'].disabled=true;
  }
}

var submitForGradeTOC2Disabled = 'false';
function disableSubmitForGradeTOC2(){
  if (submitForGradeTOC2Disabled === 'false'){
    submitForGradeTOC2Disabled = 'true';
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:exitTOC1'])
      document.forms[0].elements['tableOfContentsForm:exitTOC1'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:exitTOC2'])
      document.forms[0].elements['tableOfContentsForm:exitTOC2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'].disabled=true;
  }
}

var exitTOC1Disabled = 'false';
function disableExitTOC1(){
  if (exitTOC1Disabled === 'false'){
    exitTOC1Disabled = 'true';
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:exitTOC2'])
      document.forms[0].elements['tableOfContentsForm:exitTOC2'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['tableOfContentsForm:exitTOC1'])
      document.forms[0].elements['tableOfContentsForm:exitTOC1'].disabled=true;
  }
}

var exitTOC2Disabled = 'false';
function disableExitTOC2(){
  if (exitTOC2Disabled === 'false'){
    exitTOC2Disabled = 'true';
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC1'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'])
      document.forms[0].elements['tableOfContentsForm:submitForGradeTOC2'].disabled=true;
    if (document.forms[0].elements['tableOfContentsForm:exitTOC1'])
      document.forms[0].elements['tableOfContentsForm:exitTOC1'].disabled=true;
  }
  else{ // any subsequent click disable feeback link & action
    if (document.forms[0].elements['tableOfContentsForm:exitTOC2'])
      document.forms[0].elements['tableOfContentsForm:exitTOC2'].disabled=true;
  }
}

var disabledFeedback = 'false';
function disableFeedback(){
  if (disabledFeedback === 'false'){
    disabledFeedback = 'true';
    for (var i=0; i < document.links.length; i++){
      if (document.links[i].id === 'takeAssessmentForm:assessmentDeliveryHeading:showTOC')
        document.links[i].disabled = true;
    }
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
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
  if (disabledTOC === 'false'){
    disabledTOC = 'true';
    for (var i=0; i < document.links.length; i++){
      if (document.links[i].id == 'takeAssessmentForm:assessmentDeliveryHeading:showFeedback')
        document.links[i].disabled = true;
    }
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;  
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
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
  if (nextDisabled === 'false'){
    nextDisabled = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true; 
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
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
  if (previousDisabled === 'false'){
    previousDisabled = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;   
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
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
  if (submitDisabled === 'false'){
    submitDisabled = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;  
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
  }
}

var submit1Disabled = 'false';
function disableSubmit1(){
  if (submit1Disabled === 'false'){
    submit1Disabled = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;  
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
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
  }
}

var saveDisabled = 'false';
function disableSave(){
  if (saveDisabled === 'false'){
    saveDisabled = 'true';
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
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;
  }
}


var saveDisabled = 'false';
function disableSaveAndExit(){
  if (saveDisabled === 'false'){
    saveDisabled = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;  
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
  }
}

var saveDisabled2 = 'false';
function disableSaveAndExit2(){
  if (saveDisabled2 === 'false'){
    saveDisabled2 = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;    
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
  }
}

var cancelDisabled = 'false';
function disableCancel(){
  if (cancelDisabled === 'false'){
    cancelDisabled = 'true';
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:cancel'])
      document.forms[0].elements['takeAssessmentForm:cancel'].disabled=true;
  }
}

var quitDisabled = 'false';
function disableQuit(){
  if (quitDisabled === 'false'){
    quitDisabled = 'true';
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
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
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
  if (submitForGradeDisabled === 'false'){
    submitForGradeDisabled = 'true';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:save'])
      document.forms[0].elements['takeAssessmentForm:save'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=true;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade']) {
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=true;
}
  }
}

function enableSubmitForGrade(){
  if (submitForGradeDisabled === 'true'){
    submitForGradeDisabled = 'false';
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=false;
  }
}

function enableSave(){
  if (saveDisabled === 'true'){
    saveDisabled = 'false';
    disableTOCFeedback();
    if (document.forms[0].elements['takeAssessmentForm:next'])
      document.forms[0].elements['takeAssessmentForm:next'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:previous'])
      document.forms[0].elements['takeAssessmentForm:previous'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:quit'])
      document.forms[0].elements['takeAssessmentForm:quit'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit2'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit2'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:submitForm'])
      document.forms[0].elements['takeAssessmentForm:submitForm'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:submitForm1'])
      document.forms[0].elements['takeAssessmentForm:submitForm1'].disabled=false;
    if (document.forms[0].elements['takeAssessmentForm:submitForGrade'])
      document.forms[0].elements['takeAssessmentForm:submitForGrade'].disabled=false;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['takeAssessmentForm:saveAndExit'])
      document.forms[0].elements['takeAssessmentForm:saveAndExit'].disabled=false;
  }
}

var continueDisabled = 'false';
function disableContinue(){
  if (continueDisabled === 'false'){
    continueDisabled = 'true';
    if (document.forms[0].elements['saveForLater:returnToAssessment'])
      document.forms[0].elements['saveForLater:returnToAssessment'].disabled=true;  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['saveForLater:continue'])
      document.forms[0].elements['saveForLater:continue'].disabled=true;
  }
}

var returnToAssessmentDisabled = 'false';
function disableReturnToAssessment(){
    if (returnToAssessmentDisabled === 'false'){
      returnToAssessmentDisabled = 'true';
    if (document.forms[0].elements['saveForLater:continue'])
      document.forms[0].elements['saveForLater:continue'].disabled=true;
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['saveForLater:returnToAssessment'])
      document.forms[0].elements['saveForLater:returnToAssessment'].disabled=true;
  }
}



//  show Processing for file upload questions 
//  taking out of deliveryAssessment.jsp, so authoring can use it too.   

function showNotif(item, button,formName)
{

/*
        if (button !="noBlock")
        {
                eval("document." + formName + "." + button + ".disabled=true");
        }
*/
        if (item !== "noNotif") {
                var browserType;
                if (document.all) {browserType = "ie";}
                if (window.navigator.userAgent.toLowerCase().match("gecko")) {browserType= "gecko";}
                if (browserType === "gecko" )
                        document.showItem = eval('document.getElementById(item)');
                else if (browserType === "ie")
                        document.showItem = eval('document.all[item]');
                else
                        document.showItem = eval('document.layers[item]');

                        document.showItem.style.visibility = "visible";
        }

        for (var i=0;i<document.getElementsByTagName("input").length; i++)
        {
                if (document.getElementsByTagName("input").item(i).className == "disableme")
                {
                        document.getElementsByTagName("input").item(i).disabled = "disabled";
                }
        }

}

function showNotif2(item, button,formName)
{
        alert('item: '+item);
        alert('button: '+button);
       alert('formname: '+formName);
        if (button !="noBlock")
        {
                eval("document." + formName + "." + button + ".disabled=true");
        }
        if (item !="noNotif")
        {
                var browserType;
                if (document.all) {browserType = "ie";}
                if (window.navigator.userAgent.toLowerCase().match("gecko")) {browserType= "gecko";}
                if (browserType == "gecko" )
                        document.showItem = eval('document.getElementById(item)');
                else if (browserType == "ie")
                        document.showItem = eval('document.all[item]');
                else
                        document.showItem = eval('document.layers[item]');

                        document.showItem.style.visibility = "visible";
        }

        for (var i=0;i<document.getElementsByTagName("input").length; i++)
        {
                if (document.getElementsByTagName("input").item(i).className == "disableme")
                {
                        document.getElementsByTagName("input").item(i).disabled = "disabled";
                }
        }
}

function clearIfDefaultString(formField, defaultString) {
    if(formField.value == defaultString) {
        formField.value = "";
    }
}


function submitOnEnter(event, defaultButtonId) {
    var characterCode;
    if (event.which) {
        characterCode = event.which;
    } else if (event.keyCode) {
        characterCode = event.keyCode;
    }

    if (characterCode == 13) {
        event.returnValue = false;
        event.cancel = true;
        document.getElementById(defaultButtonId).click();
        return false;
    } else {
        return true;
    }
}

function show(obj) {
        document.getElementById(obj).style.display = '';
}

function hide(obj) {
        document.getElementById(obj).style.display = 'none';
}

function clickReloadLink(windowToGetFocus){
    

var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if ( document.links[i].id.indexOf("hiddenReloadLink") >=0){
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
windowToGetFocus.focus();

return false;
}

/* Converts implicit form control labeling to explicit by
 * adding an unique id to form controls if they don't already
 * have one and then setting the corresponding label element's
 * for attribute to form control's id value. This explicit 
 * linkage is better supported by adaptive technologies.
 * See SAK-18851 for original.
 */
fixImplicitLabeling = function(){
  var idCounter = 0;
  $('label select,label input').each(function (idx, oInput) {
    if (!oInput.id) {
       idCounter++;
       $(oInput).attr('id', 'a11yAutoGenInputId' + idCounter.toString());
    }
    if (!$(oInput).parents('label').eq(0).attr('for')) {
       $(oInput).parents('label').eq(0).attr('for', $(oInput).attr('id'));
    }
  });
};

</script>
