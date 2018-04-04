var checkflag = "false";

function checkAll(field) {
if (field != null) {
  if (field.length >0){
// for more than one checkbox
    if (checkflag == "false") {
       for (i = 0; i < field.length; i++) {
           field[i].checked = true;}
       checkflag = "true";
       return "Uncheck all"; }
    else {
       for (i = 0; i < field.length; i++) {
           field[i].checked = false; }
       checkflag = "false";
       return "Check all"; }
  }
  else {
// for only one checkbox
    if (checkflag == "false") {
  field.checked = true;
  checkflag = "true";
  return "Uncheck all"; }
else {
  field.checked = false; 
  checkflag = "false";
  return "Check all"; }

   }
}
}


function uncheck(field){
      field.checked = false; 
  checkflag = "false";
    return "uncheck"; 
}



// this is for multiple choice authoring. uncheck all other radio buttons when one is checked, to make it  behave like they are in an array.

function uncheckOthers(field){
 var fieldname = field.getAttribute("name");
 var tables= document.getElementsByTagName("TABLE");
 var prevCorrectBtn=null;

 for (var i = 0; i < tables.length; i++) {
    if ( tables[i].id.indexOf("mcradiobtn") >=0){
       var radiobtn = tables[i].getElementsByTagName("INPUT")[0];
       if (fieldname!=radiobtn.getAttribute("name")){
          if (radiobtn.checked){
             prevCorrectBtn=radiobtn.getAttribute("name");
          }
          radiobtn.checked = false;
       }
    }
 }
 
var selectId =  field.getAttribute("value");
var inputhidden = document.getElementById("itemForm:selectedRadioBtn");
inputhidden.setAttribute("value", selectId);
switchPartialCredit(fieldname,prevCorrectBtn); 
}

function updateHiddenMultipleChoice() {
  var checkboxes = jQuery("input[id*=':mccheckboxes:']:checked");
  var selectedCheckboxes = "";
  for (var i=0; i <= checkboxes.size()-1; i++) {
    selectedCheckboxes += $(checkboxes[i]).val() + ",";
  }
  selectedCheckboxes = selectedCheckboxes.substring(0, selectedCheckboxes.length-1);
  jQuery('#itemForm\\:selectedCheckboxesAnswers').val(selectedCheckboxes);
}

function switchPartialCredit(newCorrect,oldCorrect){
   var pInput = document.getElementById('itemForm:mcchoices:0:partialCredit');
   if(typeof(pInput) == 'undefined' || pInput == null){
      // partial credit function is disabled
      return;
   }	
   
   var toggleDiv=document.getElementById('partialCredit_toggle');
   if( typeof(toggleDiv) == 'undefined' ||toggleDiv == null){
      return;
   }
   else{
       //setting old one to zero
       if(oldCorrect!=null && oldCorrect!='undefined'){
             var position= oldCorrect.split(":");
             var  prevcorrId="itemForm:mcchoices:"+position[2]+":partialCredit";
             var pInput= document.getElementById(prevcorrId);
             pInput.value=0;
             pInput.style.borderStyle = "solid double";
             pInput.style.borderColor="red";
			 pInput.disabled=false;
             pInput.focus();
             var reminderTextId="itemForm:mcchoices:"+position[2]+":partialCreditReminder";
             var reminderTexElement= document.getElementById(reminderTextId);
             reminderTexElement.style.visibility="visible";
         }
         //setting new one to 100 
         position= newCorrect.split(":");
         var currCorrId="itemForm:mcchoices:"+position[2]+":partialCredit";
         var correctPInput= document.getElementById(currCorrId);
         correctPInput.value=100;
		 correctPInput.disabled=true;
  }
}

function resetInsertAnswerSelectMenus(){
  var selectlist = document.getElementsByTagName("SELECT");

  for (var i = 0; i < selectlist.length; i++) {
        if ( selectlist[i].id.indexOf("insertAdditionalAnswerSelectMenu") >=0){
          selectlist[i].value = 0;
        }
  }

  var toggleDiv=document.getElementById('partialCredit_toggle');
  if( typeof(toggleDiv) == 'undefined' || toggleDiv == null){ return;}
  else{
    var QtypeTable=document.getElementById('itemForm:chooseAnswerTypeForMC');
    QtypeTable.rows[0].cells[0].appendChild(toggleDiv); 
  }
}

function disablePartialCreditField(){
 var pInput = document.getElementById('itemForm:mcchoices:0:partialCredit');
 if(typeof(pInput) == 'undefined' || pInput == null){
    // partial credit function is disabled
    return;
 }
 
 var inputs= document.getElementsByTagName("INPUT");

 for (var i = 0; i < inputs.length; i++) {
    if (inputs[i].name.indexOf("mcradiobtn") >= 0){
	   var radiobtn = inputs[i];
       if (radiobtn.checked){
          var subElement= radiobtn.name.split(":");
          var currCorrId="itemForm:mcchoices:"+subElement[2]+":partialCredit";
		  var correctPInput= document.getElementById(currCorrId);
		  correctPInput.value=100;
          correctPInput.disabled=true;
       }   
    }
 }
}

function clickAddChoiceLink(){

var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if ( document.links[i].id.indexOf("hiddenAddChoicelink") >=0){
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
}

function clickAddEmiAnswerOptionsLink(){
	var newindex = 0;
	for (i=0; i<document.links.length; i++) {
	  if ( document.links[i].id.indexOf("hiddenAddEmiAnswerOptionsActionlink") >=0){
	    newindex = i;
	    break;
	  }
	}
	document.links[newindex].onclick();
}

function clickAddEmiQuestionAnswerCombinationsLink(){
	var newindex = 0;
	for (i=0; i<document.links.length; i++) {
	  if ( document.links[i].id.indexOf("hiddenAddEmiQuestionAnswerCombinationsActionlink") >=0){
	    newindex = i;
	    break;
	  }
	}
	document.links[newindex].onclick();
}

function countNum(){
  var spanList= document.getElementsByTagName("SPAN");
  var count=1;
  for (var i = 0; i < spanList.length; i++) 
    {
        if(spanList[i].id.indexOf("num")>=0)
         {
           spanList[i].innerHTML = count;
           count++;
         }
    }
  
}

/*
 * document.htmlareas will be undefined if the wysiwyg is anything
 * other then htmlarea, so we know if the onsubmit trick is needed
 * to get htmlarea to work right or not
 *
 */
function editorCheck(){
   if (document.htmlareas==undefined) {
      return true;
   }
   else {
      return false;
   }
}

//display a prompt if the user tries to save the question but has not edited 
//any formulas or variables.  Called on document.ready() for Calculated questions
function initCalcQuestion() {
 var dirty = false;
 $(".changeWatch").change(function() {
         dirty = true;
 });
 $(".saveButton").click(function() {
     if (!dirty) {
         if (!confirm("You have not changed variables or formulas.  Are you sure that you want to Save?")) {
             return false;
         }
     }           
 });
}

$( document ).ready( function() {

    // inputText with class ConvertPoint changes
    $( "input.ConvertPoint[type='text']" ).change( function() {
        var value = $( this ).val();
        if (value) {
            $( this ).val(value.replace(',','.'));
        } else {
            $( this ).val("0")
        }
    });

    // validation for points
    $( "#itemForm\\:answerptr" ).change( function() {
        var pointValue = parseFloat( $( this ).val() );
        var negField = $( "#itemForm\\:answerdsc" );
        var minField = $( "#itemForm\\:minPoints\\:answerminptr" );
        // do not allow negative points for point value
        if (pointValue < 0) {
            validationWarningSetDefault($( this ), "0");
            validationWarningSetDefault(negField, "0");
            validationWarningSetDefault(minField, "");
        } else {
            // negValue should be less than pointValue
            var negValue = parseFloat(negField.val());
            if (negValue < 0 || negValue > pointValue) {
                validationWarningSetDefault(negField, "0");
            }
            // minField may not be on the page if disabled
            if (minField) {
                var minValue = parseFloat(minField.val());
                if (minValue < 0 || minValue >= pointValue) {
                    validationWarningSetDefault(minField, "");
                }
            }
        }
    });

    // validation for minPoints
    $( "#itemForm\\:minPoints\\:answerminptr" ).change( function() {
        var pointValue = parseFloat( $( "#itemForm\\:answerptr" ).val() );
        var minValue = parseFloat( $( this ).val() );
        // minValue should not be equal to or greater than pointValue
        if (minValue < 0 || minValue >= pointValue) {
            validationWarningSetDefault($( this ), "0")
        } else {
            // minValue is valid disable negative points
            var negField = $( "#itemForm\\:answerdsc" );
            if (negField) {
                var negValue = parseFloat(negField.val());
                if (negValue > 0) {
                    validationWarningSetDefault(negField, "0");
                }
            }
        }
    });

    // validation for negative points
    $( "#itemForm\\:answerdsc" ).change( function() {
        var pointValue = parseFloat( $( "#itemForm\\:answerptr" ).val() );
        var negValue = parseFloat ( $( this ).val() );
        // minValue should not be equal to or greater than pointValue
        if (negValue < 0 || negValue > pointValue) {
            validationWarningSetDefault($( this ), "0")
        } else {
            // negValue should 0 if using minPoints
            var minField = $( "#itemForm\\:minPoints\\:answerminptr" );
            if (minField) {
                var minValue = parseFloat(minField.val());
                if (minValue > 0) {
                    validationWarningSetDefault(minField, "");
                }
            }
        }
    });

    $( function() {
        // negValue should be 0 and minValue should be empty if using partial credit
        var pcValue = $( "input[name='itemForm\\:partialCredit_NegativeMarking']:checked", "#itemForm" ).val();
        if (pcValue == "true") {
            var negField = $( "#itemForm\\:answerdsc" );
            if (negField) {
                var negValue = parseFloat(negField.val());
                if (negValue > 0) {
                    validationWarningSetDefault(negField, "0");
                }
            }

            var minField = $( "#itemForm\\:minPoints\\:answerminptr" );
            if (minField) {
                var minValue = parseFloat(minField.val());
                if (minValue > 0) {
                    validationWarningSetDefault(minField, "");
                }
            }
        }

    });

    // Fix the input value and display for the correct answer in Multiple Choice when entering with partial credit enabled.
    $('[id$="mcradiobtn"]').click( function(e) {

        // Transform the previous full credit label into an empty text field
        $('[id$="partialCredit"]').removeAttr('disabled');
        $('[id$="partialCredit"]').attr('type', 'text');
        $('#correctAnswerPC').remove();

        // For the choice that was marked as correct, replace the editable point value field with a static value/label of 100%
        pcBox = $('#'+this.id.replace(/:/g, '\\:').replace('mcradiobtn', 'partialCredit'));
        pcBox.after('<span id="correctAnswerPC">100%</span>');
        pcBox.attr('disabled', true);
        pcBox.attr('type', 'hidden');
        pcBox.val("0");
    });

    // Apply the above when the form loads.
    $('[id$="mcradiobtn"]').each(function() {
        if( $("input", this).attr("checked") ) {
            $("input", this).click();
        }
    });
});

function validationWarningSetDefault(element, value) {
    $( element ).animate({ backgroundColor: "red" });
    $( element ).val(value);
    $( element ).animate({ backgroundColor: "transparent" });
}

/**
 * adds a change handler to the pulldown menu, which shows/hides the rich text editor
 * for the match part of the MatchItemBean.  Odd syntax for id required to find the
 * jsf pulldown menu, standard #<pulldown> would not retrieve the component.
 * @param pulldown
 * @param feedbackContainerID the ID of the feedback container to conditionally show/hide based on the pulldown selection
 * @param noFeedbackMsgID the ID of the container which contains the feedback not available message
 */
function applyMenuListener(pulldown, feedbackContainerID, noFeedbackMsgID) {
	var $pulldownHolder = $("[id='itemForm:" + pulldown + "']");
	$pulldownHolder.change( function() {
		var applyNoFeedbackChanges = (feedbackContainerID !== undefined && feedbackContainerID !== null) && (noFeedbackMsgID !== undefined && noFeedbackMsgID !== null);
		var $feedbackContainer = applyNoFeedbackChanges ? $("[id='itemForm:" + feedbackContainerID + "']") : null;
		var $noFeedbackMsgID = applyNoFeedbackChanges ? $("[id='itemForm:" + noFeedbackMsgID + "']") : null;
		var $editor = $(this).parent("div").find("div.toggle_link_container").parent("td:last");
		if (this.value === "*new*") {
			$editor.show();
			if (applyNoFeedbackChanges) {
				$feedbackContainer.show();
				$noFeedbackMsgID.hide();
			}
		} else {
			$editor.hide();
			if (applyNoFeedbackChanges) {
				$feedbackContainer.hide();
				$noFeedbackMsgID.show();
			}
		}
	});
	
	// hide the match if needed
	$pulldownHolder.change();
}

//consolidate common used functions here for assessment settings
//improve feedback UI, get rid of page reload bugid:5574 -Qu 10/31/2013

// If we select "No Feedback will be displayed to the student"
// it will disable and uncheck feedback as well as blank out text, otherwise,
// if a different radio button is selected, we reenable feedback checkboxes & text.
function disableAllFeedbackCheck(feedbackType)
{
	var noFeedback = 3;
	
    if (feedbackType == noFeedback){
     	$("#assessmentSettingsAction\\:feedbackComponentOption input").prop("disabled", true);
		$(".respChoice input").prop({disabled:true, checked:false});
	}
    else
	{
    	$("#assessmentSettingsAction\\:feedbackComponentOption input").prop("disabled", false);
    	if ($("input[name=assessmentSettingsAction\\:feedbackComponentOption]:checked").val() == 1) {
    		$(".respChoice input").prop({disabled:true, checked:false});
    	} else {
    		$(".respChoice input").prop("disabled", false);
    	}
	}
    disableFeedbackDateCheck(feedbackType);
}

function disableFeedbackDateCheck(feedbackType) {
	var dateFeedback = 2;

    if (feedbackType == dateFeedback) {
    	$("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").prop("disabled", false);
    	$("td.feedbackColumn1 > img.ui-datepicker-trigger").prop("hidden", false);
        $("td.feedbackColumn2").prop("hidden", false);
    } else {
    	$("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").prop("disabled", true);
        $("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").val( "" );
    	$("td.feedbackColumn1 > img.ui-datepicker-trigger").prop("hidden", true);
        $("td.feedbackColumn2").prop("hidden", true);
    }
}

function disableAllFeedbackCheckTemplate(feedbackType)
{
	// By convention we start all feedback JSF ids with "feedback".
	var feedbackIdFlag = "templateEditorForm:feedbackComponent";
	var noFeedback = "3";
	var feedbacks = document.getElementsByTagName('INPUT');

	for (i=0; i<feedbacks.length; i++)
	{
		if (feedbacks[i].name.indexOf(feedbackIdFlag)==0)
		{
			if (feedbackType == noFeedback)
			{
				if (feedbacks[i].type == 'checkbox')
				{
					feedbacks[i].checked = false;
					feedbacks[i].disabled = true;
				}
				else if (feedbacks[i].type == 'text')
				{
					feedbacks[i].value = "";
					feedbacks[i].disabled = true;
				}
				else if ((feedbacks[i].type == 'radio') && (feedbacks[i].name.indexOf(feedbackComponentOptionFlag)==0))
				{
					if(feedbacks[i].value == 2) {
						feedbacks[i].checked = true;
					}
					if(feedbacks[i].value == 1) {
						feedbacks[i].checked = false;
						feedbacks[i].disabled = true;
					}
				}
			}
			else
			{
				feedbacks[i].disabled = false;
			}
		}
	}
}

$(window).load( function() {
	checkNoFeedbackOnLoad();
});

function checkNoFeedbackOnLoad(){
	var noFeedback = 3;
	var feedbackType = $("input[name=assessmentSettingsAction\\:feedbackDelivery]:checked").val();

	if(feedbackType == noFeedback) {
		$("#assessmentSettingsAction\\:feedbackComponentOption input").prop("disabled", true);
		$(".respChoice input").prop('disabled', true);
	}
	disableFeedbackDateCheck(feedbackType);
}

function disableOtherFeedbackComponentOption(field)
{
	var fieldValue = field.getAttribute("value");
	if(fieldValue ==1 )
		$(".respChoice input").prop({disabled:true, checked:false});
	else
		$(".respChoice input").prop("disabled", false);
}

function validateUrl(){
  var list =document.getElementsByTagName("input");
  for (var i=0; i<list.length; i++){
    if (list[i].id.indexOf("finalPageUrl") >=0){
      var finalPageUrl = list[i].value;
	  if (finalPageUrl.substring(0,4).toLowerCase().indexOf("http") == -1)
	  {
		finalPageUrl = "http://" + finalPageUrl;
	  }
	  //alert(finalPageUrl);
      window.open(finalPageUrl,'validateUrl');
    }
  }
}

function updateItemNavigation(isFromItemNavigation)
{
  var inputhidden = document.getElementById("assessmentSettingsAction:itemNavigationUpdated");
  inputhidden.value = isFromItemNavigation;
}
    
function uncheckOther(field){
 var fieldname = field.getAttribute("name");
 var inputList = document.getElementsByTagName("INPUT");

 for(i = 0; i < inputList.length; i++){
    if((inputList[i].name.indexOf("background")>=0)&&(inputList[i].name != fieldname))
         inputList[i].checked=false;
      
 }
}

function showHideReleaseGroups(){
  var showGroups;
  var el = document.getElementById("assessmentSettingsAction:releaseTo");
  if (el != null && el.selectedIndex == 2) {
	document.getElementById("groupDiv").style.display = "block";
	document.getElementById("groupDiv").style.width = "80%";
  }
  else {
	document.getElementById("groupDiv").style.display = "none";
  }
}

function setBlockDivs()
{  
   //alert("setBlockDivs()");
   var divisionNo = ""; 
   var blockDivs = ""; 
   blockElements = document.getElementsByTagName("div");
   //alert("blockElements.length" + blockElements.length);
   for (i=0 ; i < blockElements.length; i++)
   {
      divisionNo = "" + blockElements[i].id;
	  //alert("divisionNo=" + divisionNo);
	  //alert("display=" + blockElements[i].style.display);
      if(divisionNo.indexOf("__hide_division_assessmentSettingsAction") >=0 && blockElements[i].style.display == "block")
      { 
         //alert("divisionNo=" + divisionNo);
         var id = divisionNo.substring(41);
		 if (blockDivs == "") {
            blockDivs = id;
         }
		 else {
			 blockDivs = blockDivs + ";" + id; 
		 }
		 //alert("blockDivs=" + blockDivs);
	  }
   }
   //document.forms[0].elements['assessmentSettingsAction:blockDivs'].value = "_id224";
   document.forms[0].elements['assessmentSettingsAction:blockDivs'].value = blockDivs;
}

function checkUncheckTimeBox(){
  var inputList= document.getElementsByTagName("INPUT");
  var timedCheckBoxId;
  var timedHourId;
  var timedMinuteId;
  for (i = 0; i <inputList.length; i++) 
  {
    if(inputList[i].type=='checkbox')
    {
      if(inputList[i].id.indexOf("selTimeAssess")>=0)
        timedCheckBoxId = inputList[i].id;
    }
  }
  inputList= document.getElementsByTagName("select");
  for (i = 0; i <inputList.length; i++) 
  {
    if(inputList[i].id.indexOf("timedHours")>=0)
      timedHourId =inputList[i].id;
    if(inputList[i].id.indexOf("timedMinutes")>=0)
      timedMinuteId =inputList[i].id;
  }
  if(document.getElementById(timedCheckBoxId) != null)
  {
    if(!document.getElementById(timedCheckBoxId).checked)
    {
      if(document.getElementById(timedHourId) != null)
      {
        for(i=0; i<document.getElementById(timedHourId).options.length; i++)
        {
          if(i==0)
            document.getElementById(timedHourId).options[i].selected = true;
          else
            document.getElementById(timedHourId).options[i].selected = false;
        }
        document.getElementById(timedHourId).disabled = true;
      }
      if(document.getElementById(timedMinuteId) != null)
      {
        for(i=0; i<document.getElementById(timedMinuteId).options.length; i++)
        {
          if(i==0)
            document.getElementById(timedMinuteId).options[i].selected = true;
          else
            document.getElementById(timedMinuteId).options[i].selected = false;
        }
        document.getElementById(timedMinuteId).disabled = true;
      }
    }
    else 
    { // SAM-2121: now the "Timed Assessment" box is checked"
      // I wish we didn't have to submit this form now, but I could not get it to work properly without submitting.
      // SAM-2262: fixed
      document.getElementById(timedHourId).disabled = false;
      document.getElementById(timedMinuteId).disabled = false;
      //document.forms[0].submit();
    }    
  }
}

function checkUncheckAllReleaseGroups(){
  var checkboxState = document.getElementById("assessmentSettingsAction:checkUncheckAllReleaseGroups").checked;
  var inputList= document.getElementsByTagName("INPUT");
  for (i = 0; i <inputList.length; i++) 
  {
    if(inputList[i].type=='checkbox')
    {
      if(inputList[i].name.indexOf("groupsForSite")>=0)
        inputList[i].checked=checkboxState;
    }
  }
}

function lockdownQuestionLayout(value) {
  if (value == 1) {
    $('#assessmentSettingsAction\\:assessmentFormat input[value=1]').prop('checked', 'checked');
    $('#assessmentSettingsAction\\:assessmentFormat input').prop('disabled', 'disabled');
  } 
  else {
    $('#assessmentSettingsAction\\:assessmentFormat input').prop('disabled', '');
  }
}

function lockdownMarkForReview(value) {
  if (value == 1) {
    $('#assessmentSettingsAction\\:markForReview1').prop('checked', '');
    $('#assessmentSettingsAction\\:markForReview1').prop('disabled', 'disabled');
  } 
  else {
    $('#assessmentSettingsAction\\:markForReview1').prop('disabled', '');
  }
}

function initTimedCheckBox(){
		var timedHours = document.getElementById("assessmentSettingsAction\:timedHours");
		var timedHoursVal = timedHours.options[timedHours.selectedIndex].value;
		var timedMinutes = document.getElementById("assessmentSettingsAction\:timedMinutes");
		var timedMinutesVal = timedMinutes.options[timedMinutes.selectedIndex].value;
		
		if((timedHoursVal != "0") || (timedMinutesVal != "0")) document.getElementById("assessmentSettingsAction\:selTimeAssess").checked=true;
}

function lockdownAnonyGrading(value) {
	if (value == 'Anonymous Users') {
		$('#assessmentSettingsAction\\:anonymousGrading').prop('checked', 'checked');
		$('#assessmentSettingsAction\\:anonymousGrading').prop('disabled', 'disabled');
	} 
	else {
		$('#assessmentSettingsAction\\:anonymousGrading').prop('disabled', '');
	}
}

function lockdownGradebook(value) {
	if (value == 'Anonymous Users') {
		$('#assessmentSettingsAction\\:toDefaultGradebook').prop('checked', '');
		$('#assessmentSettingsAction\\:toDefaultGradebook').prop('disabled', 'disabled');
	} 
	else {
		$('#assessmentSettingsAction\\:toDefaultGradebook').prop('disabled', '');
	}
}

function show_multiple_text(show_multiple_link){
	$('.simple_text_area').each(function(){
		var $currentStatus=$(this).siblings('[type="hidden"]');
		if(typeof $currentStatus.data("first") != 'undefined' && typeof $currentStatus.data("second") != 'undefined'){
			show_editor($currentStatus.data("first"),$currentStatus.data("second"));
		}
	});
	$(show_multiple_link).hide();
}

function checkLastHandling(){
	var isDisabled=$('input[id*="lateHandling"]:checked').val();
	var retractDate = $('input[id*="retractDate"]:visible');
	//$('input[id*="retractDate"]:visible').prop( "disabled", isDisabled);
	//$('input[id*="retractDate"]:visible').next().show;
	
	if(isDisabled==2){
		$(retractDate).prop( "disabled", true );
		$(retractDate).next().hide();
	}else{
		$(retractDate).prop( "disabled", false );
		$(retractDate).next().show();
	}
}

//if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
//ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	mySetMainFrameHeight(id, null);
}

function mySetMainFrameHeight(id, minHeight)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
			if(innerDocScrollH != null && innerDocScrollH > height){
				height = innerDocScrollH;
			}
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		if(minHeight && minHeight > newHeight){
			newHeight = minHeight;
		}
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}

function resetSelectMenus(){
  var selectlist = document.getElementsByTagName("SELECT");

  for (var i = 0; i < selectlist.length; i++) {
        if ( selectlist[i].id.indexOf("changeQType") >=0){
          selectlist[i].value = "";
        }
  }
}

function clickInsertLink(field){
  var insertlinkid = field.id.replace("changeQType", "hiddenlink");
  var hiddenSelector = "#" + insertlinkid.replace( /(:|\.|\[|\]|,)/g, "\\$1" );
  $(hiddenSelector).click();
}

// Show MathJax warning messages if applicable
if (typeof MathJax != 'undefined') {
  $(document).ready(function() {
    $(".mathjax-warning").show();
  });
}