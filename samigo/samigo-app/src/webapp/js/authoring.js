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
/// for only one checkbox
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
  $('#itemForm\\:hiddenAddChoicelink')[0].click();
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
//any formulas, variables or global variables.  Called on document.ready() for Calculated questions
function initCalcQuestion() {
 var dirty = false;
 $(".changeWatch").change(function() {
         dirty = true;
 });
 $(".saveButton").click(function() {
     if (!dirty) {
         if (!confirm("You have not changed variables, global variables or formulas.  Are you sure that you want to Save?")) {
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
                if (minValue < 0 || minValue > pointValue) {
                    validationWarningSetDefault(minField, "");
                }
            }
        }
    });

    // validation for minPoints
    $( "#itemForm\\:minPoints\\:answerminptr" ).change( function() {
        var pointValue = parseFloat( $( "#itemForm\\:answerptr" ).val() );
        var minValue = parseFloat( $( this ).val() );
        // minValue should not be greater than pointValue
        if (minValue < 0 || minValue > pointValue) {
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
    const negField = document.getElementById("itemForm:answerdsc");
    negField && negField.addEventListener("change", function () {

      const pointValue = parseFloat(document.getElementById("itemForm:answerptr").value);
      const negValue = parseFloat(this.value);
      // minValue should not be equal to or greater than pointValue
      if (negValue < 0 || negValue > pointValue) {
          validationWarningSetDefault($(this), "0");
      } else {
        // negValue should 0 if using minPoints
        const minField = document.getElementById("itemForm:minPoints:answerminptr");
        const warning = document.getElementById("itemForm:minPoints:min-point-warning");
        const info = document.getElementById("itemForm:minPoints:min-point-info");
        if (negValue == 0.0) {
          minField.disabled = false;
          warning.style.display = "none";
          info.style.display = "inline-block";
        } else {
          if (minField) {
            const minValue = parseFloat(minField.value);
            minField.value = "";
            minField.disabled = true;
            warning.style.display = "inline-block";
            info.style.display = "none";
          }
        }
      }
    });

    $(function() {

        if (document.querySelector("input[name='itemForm:partialCredit_NegativeMarking']:checked")) {
          // This is the partial credit option
          let minField = document.getElementById("itemForm:minPoints:answerminptr");
          minField.value = "";
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
    $('input[name="assessmentSettingsAction\\:userOrGroup"]').change(function () {
        checkUserOrGroupRadio();
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

// If we select "No, do not display any feedback to the student"
// it will uncheck feedback as well as blank out text, otherwise,
// if a different radio button is selected, we reenable feedback checkboxes & text.
function disableAllFeedbackCheck() {
	disableFeedbackDateCheck();
	disableOtherFeedbackComponentOption();
}

// Display the date selectors when the feedback is shown by date.
function disableFeedbackDateCheck() {
    const dateFeedback = 2;
    const feedbackType = document.querySelector("input[id*=feedbackDelivery]:checked").value;

    if (feedbackType == dateFeedback) {
        $("#feedbackByDatePanel").show();
        $("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").prop("disabled", false);
        $("input#assessmentSettingsAction\\:feedbackEndDate.hasDatepicker").prop("disabled", false);
        $("td.feedbackColumn1 > img.ui-datepicker-trigger").prop("hidden", false);
        $("td.feedbackColumn2").prop("hidden", false);
    } else {
        $("#feedbackByDatePanel").hide();
        $("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").prop("disabled", true);
        $("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").val( "" );
        $("input#assessmentSettingsAction\\:feedbackEndDate.hasDatepicker").prop("disabled", true);
        $("input#assessmentSettingsAction\\:feedbackEndDate.hasDatepicker").val( "" );
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

function disableOtherFeedbackComponentOption (){
	const noFeedback = 3;
	const feedbackType = document.querySelector("input[id*=feedbackDelivery]:checked").value;
	const fields = document.querySelectorAll("input[id*=feedbackComponentOption]");
	const field = Array.from(fields).find(radio => radio.checked);
	const respChoice = document.querySelector('.respChoice');

	fields.forEach(radio => {
		radio.disabled = feedbackType == noFeedback;
	});

	if (field !== undefined) {
		respChoice.style.display = (field.value === "2" && !field.disabled) ? "block" : "none";
	} else {
		//Set default value when no radio is selected and call function again
		fields[0].checked = true;
		disableOtherFeedbackComponentOption();
	}
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

function showHideReleaseGroups() {
  const groupDiv = document.getElementById("groupDiv");
  const releaseTo = document.getElementById("assessmentSettingsAction:releaseTo");
  
  if (releaseTo?.selectedIndex === 2) {
    groupDiv.style.display = "block";
    groupDiv.style.width = "80%";
  } else {
    groupDiv.style.display = "none";
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
function checkTimedRadio(){
        var timelimitEnabled=$('input[id*="selTimeAssess"]:checked').val();
        var hourSelect=$('select[id*="timedHours"]');
        var minuteSelect=$('select[id*="timedMinutes"]');
        var hourLabel = $('[id*="timedHoursLabel"]');
        var minuteLabel = $('[id*="timedMinutesLabel"]');
        var firstLabel = $('label[for*="selTimeAssess:1"]');
        var secondLabel = $('label[id*="isTimedTimeLimitLabel"]');
        var dot = ".";

        if(timelimitEnabled == 'true') {
                //timelimit enabled
                hourSelect.show();
                minuteSelect.show();
                hourLabel.show();
                minuteLabel.show();
                secondLabel.show();
                if(firstLabel.text().indexOf(dot) == -1) {
                    firstLabel.text(firstLabel.text() + dot);
                    firstLabel.after('<span id="timedSpace"> </span>');
                }
        }
        else if(timelimitEnabled == 'false') {
                //timelimit disabled
                hourSelect.hide();
                minuteSelect.hide();
                hourLabel.hide();
                minuteLabel.hide();
                secondLabel.hide();
                if(firstLabel.text().indexOf(dot) > -1) {
                    firstLabel.text(firstLabel.text().substring(0, firstLabel.text().indexOf(dot)));
                    $("#timedSpace").remove();
                }
                //set hour and min to 0
                hourSelect[0].options.selectedIndex = 0;
                minuteSelect[0].options.selectedIndex = 0;
        }
}

function initTimedRadio(){
	timedSettings = $('[id*="selTimeAssess"]');
	//false -> No; true -> Yes (time limit)
	defaultValue = false ? 1 : 0;
	//If no option is selected
	if(timedSettings.filter(':checked').length == 0) {
		//Select default value
		timedSettings.slice(defaultValue, defaultValue + 1).prop('checked', 'checked');
	}
	checkTimedRadio();
}

function initAnononymousUsers(){
	let releaseTo = document.getElementById('assessmentSettingsAction:releaseTo');
	releaseTo.prevValue = releaseTo.value;
	if (releaseTo.value === 'Anonymous Users') {
		handleAnonymousUsers(releaseTo.value, releaseTo.value);
	}
}

//This is just needed for the published settings
//In unpublished settings the default is set in the AssessmentSettingsBean
function setSubmissionLimit() {
	var textField = $('[id*="submissions_Allowed"]')
	if(textField.val() == "") {
		textField.val("1");
	}
}

//Sets default values for time exceptions (User/Group)
function setExceptionDefault() {
	defaultButton = $('[id*="extendedEnableUser"]');
	defaultButton.first().prop('checked', 'checked');
        checkUserOrGroupRadio();
}

const ANON_USERS = "Anonymous Users";
function lockdownAnonyGrading(value, prevValue) {
	const ag = document.getElementById("assessmentSettingsAction:anonymousGrading");
	if (ag !== null) {
		if (value === ANON_USERS) {
			ag.checked = true;
		}
		else if (prevValue === ANON_USERS) {
			ag.checked = false;
		}
		ag.disabled = value === ANON_USERS;
	}
}

function lockdownGradebook(value) {
	if (value == 'Anonymous Users') {
		$('#assessmentSettingsAction\\:toDefaultGradebook').prop('checked', '');
		$('#assessmentSettingsAction\\:toDefaultGradebook').prop('disabled', 'disabled');
		$('#assessmentSettingsAction\\:toGradebookName input').prop('disabled', 'disabled');
	} 
	else {
		$('#assessmentSettingsAction\\:toDefaultGradebook').prop('disabled', '');
	}
}

function handleAnonymousUsers(value, prevValue) {
	lockdownAnonyGrading(value, prevValue);
	lockdownGradebook(value);
	const msg = document.getElementById("assessmentSettingsAction:gradingOptionsDisabledInfo");
	if (msg !== null) {
		msg.style.display = value === ANON_USERS ? "block" : "none";
	}
}



function handleAnonymousUsersChange(element)
{
	const value = element.value; 	// possible values are Anonymous Users, <site title>, Specific Groups
	const prevValue = "prevValue" in element ? element.prevValue : [...element.options].filter(o => o.defaultSelected)[0].label;
	element.prevValue = value;

		handleAnonymousUsers(value, prevValue);
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
	var retractDate = $('input[id*="retractDate"]');
	var deadlineLabel = $('[id*="lateHandlingDeadlineLabel"]');
	var firstLabel = $('label[for*="lateHandling:1"]');
	var dot = ".";
	//$('input[id*="retractDate"]:visible').prop( "disabled", isDisabled);
	//$('input[id*="retractDate"]:visible').next().show;
	
	if(isDisabled==2){
		retractDate.hide().next().hide();
		deadlineLabel.hide();
		if(firstLabel.text().indexOf(dot) > -1) {
			firstLabel.text(firstLabel.text().substring(0, firstLabel.text().indexOf(dot)));
			$("#lateSpace").remove();
		}
	}else{
		retractDate.show().next().show();
		deadlineLabel.show();
		if(firstLabel.text().indexOf(dot) == -1) {
			firstLabel.text(firstLabel.text() + dot);
			firstLabel.after('<span id="lateSpace"> </span>');
		}
	}
}

function checkUserOrGroupRadio() {
	var checkedSettingId = $('input[id*="extendedEnable"]:checked').attr('id');

	if(checkedSettingId.indexOf('User') > -1) {
		//User is selected -> disable group selection
		$('select[name*="newEntry-group"]').prop('disabled', 'disabled');
		$('select[name*="newEntry-user"]').prop('disabled', '');
	}
	else if(checkedSettingId.indexOf('Group') > -1) {
		//Group is selected -> disable user selection
		$('select[name*="newEntry-user"]').prop('disabled', 'disabled');
		$('select[name*="newEntry-group"]').prop('disabled', '');
	}
}

function enableDisableToGradebook() {
	var toDefaultGradebookVal = $('#assessmentSettingsAction\\:toDefaultGradebook input:checked').val();
	if (toDefaultGradebookVal == 3) {
		$('#assessmentSettingsAction\\:toGradebookName').prop('disabled', '');
	}
	else {
		$('#assessmentSettingsAction\\:toGradebookName').prop('disabled', 'disabled');
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
  $(hiddenSelector)[0].click()
}

// Show MathJax warning messages if applicable
if (typeof MathJax != 'undefined') {
  $(document).ready(function() {
    $(".mathjax-warning").show();
  });
}

function toggleCategories(checkbox) {
  // Toggle categories selector. If categories are disabled it won't exist
  // so check first.
  var categoryDiv = $('#assessmentSettingsAction\\:toGradebookCategory');
  var selectedGradebook = $('#assessmentSettingsAction\\:toGradebookSelected');

  if (categoryDiv != undefined && categoryDiv.length) {
      if ($(checkbox).val() === '1') {
          categoryDiv.fadeIn();
      } else {
          categoryDiv.fadeOut();
      }
  }

  if (selectedGradebook != undefined && selectedGradebook.length) {
    if ($(checkbox).val() === '3') {
      selectedGradebook.fadeIn();
    } else {
      selectedGradebook.fadeOut();
    }
  }
}

function expandAccordion(iframId){
    $('div#jqueryui-accordion > .ui-accordion-content').show();
    mySetMainFrameHeight(iframId);
    $("#collapseLink").show();
    $("#expandLink").hide();
    $("div#jqueryui-accordion > h3.ui-accordion-header > span").removeClass("ui-icon-triangle-1-e").addClass("ui-icon-triangle-1-s");
    $("div#jqueryui-accordion > h3.ui-accordion-header").addClass("ui-accordion-header-active ui-state-active");
}

function collapseAccordion(iframId){
    $('.ui-accordion-content').hide();
    mySetMainFrameHeight(iframId);
    $("#collapseLink").hide();
    $("#expandLink").show();
    $("div#jqueryui-accordion > h3.ui-accordion-header > span").removeClass("ui-icon-triangle-1-s").addClass("ui-icon-triangle-1-e");
    $("div#jqueryui-accordion > h3.ui-accordion-header").removeClass("ui-accordion-header-active ui-state-active");
}

/*
* This will set an aria-describedbiy attribute to every setting with a descriptive label when
* the lablels id equals [the id of the option] + [whatever helpBlockDetectionString is]
*/
function setAccessibilityAttributes() {
	var helpBlockDetectString = "HelpBlock";
	var helpBlocks = $("[id*=" + helpBlockDetectString +"]");
	for(i = 0; i < helpBlocks.length; i++) {
		var helpBlockId = helpBlocks[i].id;
		//removes helpBlockDetectString resulting in the id of the setting
		var settingId = helpBlockId.substring(0,helpBlockId.indexOf(helpBlockDetectString));
		//querries elements with that exact id
		var settingQuerryIdent = "#" + settingId.replace(/:/g, "\\:");
		//querries elements with appending :0, :1, ...
		var settingQuerryOption = "[id*=" + settingId.replace(/:/g, "\\:") + "\\:]";
		//sets aria-describredby attribute
		$(settingQuerryIdent + ", " + settingQuerryOption).first().attr("aria-describredby", helpBlockId);
	}
}

function toggleSection(sectionId, visible){
	if(visible === "true"){
		document.getElementById(sectionId).classList.remove('hidden');
	} else {
		document.getElementById(sectionId).classList.add('hidden');
	}
}
function changeStatusCorrectResponseCheckbox() {
  const hideCorrectResponse = document.getElementById('assessmentSettingsAction:hideCorrectResponse');

  hideCorrectResponse.style.display = (hideCorrectResponse.style.display == "none") ? "block" : "none";
}
