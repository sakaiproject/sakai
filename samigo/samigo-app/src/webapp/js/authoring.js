/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * Portions of this software are based upon public domain software
 * originally written at the National Center for Supercomputing Applications,
 * University of Illinois, Urbana-Champaign.
 *
 */

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

// this is to disable the 'copy' button when importing questions from pool to assessement. itemtype =10

var disabledImport= 'false';
function disableImport(){
  if (disabledImport== 'false'){
    disabledImport= 'true'
  }
  else{ // any subsequent click disable button & action
    if (document.forms[0].elements['editform:import'])
    {
      document.forms[0].elements['editform:import'].disabled=true;
    }
  }
}

function toPoint(id)
{
  var x=document.getElementById(id).value
  document.getElementById(id).value=x.replace(',','.')
}

/**
 * adds a change handler to the pulldown menu, which shows/hides the rich text editor
 * for the match part of the MatchItemBean.  Odd syntax for id required to find the
 * jsf pulldown menu, standard #<pulldown> would not retrieve the component.
 * @param pulldown
 */
function applyMenuListener(pulldown) {
	var $pulldownHolder = $("[id='itemForm:" + pulldown + "']");	
	$pulldownHolder.change( function() {
		var $editor = $(this).parent("div").find("div.toggle_link_container").parent("td:last");
		if (this.value === "*new*") {
			$editor.show();
		} else {
			$editor.hide();			
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
    	$("td.feedbackColumn2 > img.ui-datepicker-trigger").prop("hidden", false);
    } else {
    	$("input#assessmentSettingsAction\\:feedbackDate.hasDatepicker").prop("disabled", true);
    	$("td.feedbackColumn2 > img.ui-datepicker-trigger").prop("hidden", true);
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
