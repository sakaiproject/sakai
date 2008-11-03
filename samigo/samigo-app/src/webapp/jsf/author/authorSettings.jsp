<%@ page contentType="text/html;charset=UTF-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>


<!--
* $Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License. 
*
**********************************************************************************/
--%>
-->
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{assessmentSettingsMessages.sakai_assessment_manager} #{assessmentSettingsMessages.dash} #{assessmentSettingsMessages.settings}" /></title>
      <samigo:script path="/jsf/widget/hideDivision/hideDivision.js"/>
      <samigo:script path="/jsf/widget/datepicker/datepicker.js"/>
      <samigo:script path="/jsf/widget/colorpicker/colorpicker.js"/>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>


<script language="javascript" style="text/JavaScript">
<!--
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


function validateUrl0(){
  alert("hu");
  var finalPageUrl = document.getElementsById("assessmentSettingsAction:finalPageUrl");
  alert("hello"+finalPageUrl.value);
  window.open(finalPageUrl.value,'validateUrl');
}

function submitForm()
{
  document.forms[0].onsubmit();
  document.forms[0].submit();
}


// By convention we start all feedback JSF ids with "feedback".
var feedbackIdFlag = "assessmentSettingsAction:feedback";
var noFeedback = "3";

// If we select "No Feedback will be displayed to the student"
// it will disable and uncheck feedback as well as blank out text, otherwise,
// if a different radio button is selected, we reenable feedback checkboxes & text.
function disableAllFeedbackCheck(feedbackType)
{
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
      }
      else
      {
        feedbacks[i].disabled = false;
      }
    }
  }

  document.forms[0].onsubmit();
  document.forms[0].submit();
}

function checkTimeSelect(){
  var autoSubmitId;
  var timedAssessmentId;
  var inputList= document.getElementsByTagName("INPUT");
  for (i = 0; i <inputList.length; i++) {
    if(inputList[i].type=='checkbox'){
      if(inputList[i].id.indexOf("selTimeAssess")>=0)
        timedAssessmentId= inputList[i].id;
      if(inputList[i].id.indexOf("automatic")>=0)
        autoSubmitId=inputList[i].id;
    }
  }

  if(document.getElementById(timedAssessmentId) != null)
  {
    if(!document.getElementById(timedAssessmentId).checked && document.getElementById(autoSubmitId) != null)
    {
      document.getElementById(autoSubmitId).disabled=true;
    }
    else if((document.getElementById(autoSubmitId) != null) && (document.getElementById(autoSubmitId).disabled != null))
    {
      document.getElementById(autoSubmitId).disabled=false;
    }
  }
  else if((document.getElementById(autoSubmitId) != null) && (document.getElementById(autoSubmitId).disabled != null))
  {
    document.getElementById(autoSubmitId).disabled=false;
  }
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
      }
    }
  }
}
function uncheckOther(field){
 var fieldname = field.getAttribute("name");
 var inputList = document.getElementsByTagName("INPUT");

 for(i = 0; i < inputList.length; i++){
    if((inputList[i].name.indexOf("background")>=0)&&(inputList[i].name != fieldname))
         inputList[i].checked=false;
      
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

 
function showHideReleaseGroups(){
  var showGroups;
  var inputList= document.getElementsByTagName("INPUT");
  for (i = 0; i <inputList.length; i++) 
  {
    if(inputList[i].type=='radio')
    {
      if(inputList[i].value.indexOf("Selected Groups")>=0) {
        showGroups=inputList[i].checked;
        break;
      }  
    }
  }
  if(showGroups) {
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


//-->
</script>



      </head>
    <body onload="checkTimeSelect();<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">

<!-- content... -->
<h:form id="assessmentSettingsAction" onsubmit="return editorCheck();">

  <h:inputHidden id="assessmentId" value="#{assessmentSettings.assessmentId}"/>
  <h:inputHidden id="blockDivs" value=""/>

  <!-- HEADINGS -->
  <%@ include file="/jsf/author/allHeadings.jsp" %>

    <h3>
     <h:outputText value="#{assessmentSettingsMessages.settings} #{assessmentSettingsMessages.dash} #{assessmentSettings.title}"/>
    </h3>
<p>
  <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
</p>

<div class="tier1">
  <!-- *** GENERAL TEMPLATE INFORMATION *** -->

<p>
<h:outputLink value="#" title="#{templateMessages.t_showDivs}" onclick="showDivs();" onkeypress="showDivs();">
<h:outputText value="#{templateMessages.open}"/>
</h:outputLink>
<h:outputText value=" | " />
<h:outputLink value="#" title="#{templateMessages.t_hideDivs}" onclick="hideDivs();" onkeypress="hideDivs();">
<h:outputText value="#{templateMessages.close}"/>
</h:outputLink>
<h:outputText value="#{templateMessages.allMenus}"/>

</p>
<h:panelGroup rendered="#{assessmentSettings.valueMap.templateInfo_isInstructorEditable==true and !assessmentSettings.noTemplate}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_template_information}" >
<f:verbatim> <div class="tier2"></f:verbatim>
 <h:panelGrid columns="2" columnClasses="shorttext">
        <h:outputLabel value="#{assessmentSettingsMessages.template_title}"/>
        <h:outputText escape="false" value="#{assessmentSettings.templateTitle}" />
        <h:outputLabel value="#{assessmentSettingsMessages.template_authors}" rendered="#{assessmentSettings.templateAuthors!=null}"/>
        <h:outputText escape="false" rendered="#{assessmentSettings.templateAuthors!=null}" value="#{assessmentSettings.templateAuthors}" />
        <h:outputLabel value="#{assessmentSettingsMessages.template_description}" rendered="#{assessmentSettings.templateDescription!=null}"/>
        <h:outputText escape="false" rendered="#{assessmentSettings.templateDescription!=null}" value="#{assessmentSettings.templateDescription}" />
    </h:panelGrid>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** ASSESSMENT INTRODUCTION *** -->
<f:subview id="intro">

  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_assessment_introduction}" > <div class="tier2">
    <h:panelGrid columns="2" columnClasses="shorttext" id="first"
      summary="#{templateMessages.enter_template_info_section}">

        <h:outputLabel for="assessment_title" value="#{assessmentSettingsMessages.assessment_title}"/>
        <h:inputText id="assessment_title" size="80" value="#{assessmentSettings.title}" />

        <h:outputLabel for="creator" value="#{assessmentSettingsMessages.assessment_creator}" rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>

        <h:outputText id="creator" value="#{assessmentSettings.creator}"
          rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>
       

        <h:outputLabel for="assessment_author" rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}" value="#{assessmentSettingsMessages.assessment_authors}"/>

        <h:inputText id="assessment_author" size="80" value="#{assessmentSettings.authors}"
          rendered="#{assessmentSettings.valueMap.assessmentAuthor_isInstructorEditable==true}"/>

        <h:outputLabel value="#{assessmentSettingsMessages.assessment_description}" rendered="#{assessmentSettings.valueMap.description_isInstructorEditable==true}"/>

        <%-- SAM-363: this is a work around given samigo:wysiwyg does not support disabled --%>
       
        <h:panelGrid rendered="#{assessmentSettings.valueMap.description_isInstructorEditable==true}">
          <samigo:wysiwyg rows="140" value="#{assessmentSettings.description}" hasToggle="yes" >
           <f:validateLength maximum="4000"/>
         </samigo:wysiwyg>
        </h:panelGrid>

       <!-- ASSESSMENT ATTACHMENTS -->
       <h:panelGroup>
         <h:panelGrid columns="1">
           <%@ include file="/jsf/author/authorSettings_attachment.jsp" %>
         </h:panelGrid>
       </h:panelGroup>
  
    </h:panelGrid>
</div>
  </samigo:hideDivision>
</f:subview>

  <!-- *** DELIVERY DATES *** -->
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_assessment_delivery_dates}"> <div class="tier2">
    <h:panelGrid columns="2" columnClasses="shorttext"
      summary="#{templateMessages.delivery_dates_sec}">
    <h:outputLabel for="startDate" value="#{assessmentSettingsMessages.assessment_available_date}"/>
      <samigo:datePicker value="#{assessmentSettings.startDateString}" size="25" id="startDate" />

     <h:outputLabel for="endDate" rendered="#{assessmentSettings.valueMap.dueDate_isInstructorEditable==true}" value="#{assessmentSettingsMessages.assessment_due_date}" />
        <h:panelGroup rendered="#{assessmentSettings.valueMap.dueDate_isInstructorEditable==true}">

          <samigo:datePicker value="#{assessmentSettings.dueDateString}" size="25" id="endDate"/>

        </h:panelGroup>

    <h:outputLabel for="retractDate" value="#{assessmentSettingsMessages.assessment_retract_date}" rendered="#{assessmentSettings.valueMap.retractDate_isInstructorEditable==true}" />
        <h:panelGroup rendered="#{assessmentSettings.valueMap.retractDate_isInstructorEditable==true}">
          <samigo:datePicker value="#{assessmentSettings.retractDateString}" size="25" id="retractDate" />
        </h:panelGroup>
    </h:panelGrid>
 </div>
  </samigo:hideDivision>

  <!-- *** RELEASED TO *** -->
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_released_to}">
  <div class="tier2">
    <h:panelGrid summary="#{templateMessages.released_to_info_sec}">
      <h:selectOneRadio layout="pagedirection" value="#{assessmentSettings.firstTargetSelected}"
        required="true" onclick="showHideReleaseGroups();setBlockDivs();submitForm();">
        <f:selectItems value="#{assessmentSettings.publishingTargets}" />
      </h:selectOneRadio>
    </h:panelGrid>
  

  <f:verbatim><div id="groupDiv" class="tier3"></f:verbatim>
  <f:verbatim><table bgcolor="#CCCCCC"><tr><td></f:verbatim>  
    <h:selectBooleanCheckbox id="checkUncheckAllReleaseGroups" onclick="checkUncheckAllReleaseGroups();"/>
      
  <f:verbatim></td><td></f:verbatim>
  <h:outputText value="#{assessmentSettingsMessages.title_description}" />
  <f:verbatim></td></tr></table></f:verbatim>
  
    <h:selectManyCheckbox id="groupsForSite" layout="pagedirection" value="#{assessmentSettings.groupsAuthorized}">
     <f:selectItems value="#{assessmentSettings.groupsForSite}" />
    </h:selectManyCheckbox>
  <f:verbatim></div></f:verbatim>
 
  </div>
  </samigo:hideDivision>

  <!-- *** HIGH SECURITY *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true or assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_high_security}">
    <f:verbatim><div class="tier2"></f:verbatim>
    <h:panelGrid border="0" columns="2"
        summary="#{templateMessages.high_security_sec}">
       <!--h:selectBooleanCheckbox
         rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}"
         value="#{assessmentSettings.valueMap.hasSpecificIP}"/-->
      <h:outputText value="#{assessmentSettingsMessages.high_security_allow_only_specified_ip}"
        rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}"/>
      <%-- no WYSIWYG for IP addresses --%>
      <h:panelGroup rendered="#{assessmentSettings.valueMap.ipAccessType_isInstructorEditable==true}">
      <h:inputTextarea value="#{assessmentSettings.ipAddresses}" cols="40" rows="5"/>

<h:outputText escape="false" value="<br/>#{assessmentSettingsMessages.ip_note} <br/>#{assessmentSettingsMessages.ip_example}#{assessmentSettingsMessages.ip_ex}<br/>"/> 
     </h:panelGroup>
      <!--h:selectBooleanCheckbox
         rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}"
         value="#{assessmentSettings.valueMap.hasUsernamePassword}"/-->
      <h:outputText value="#{assessmentSettingsMessages.high_security_secondary_id_pw}"
        rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}"/>
      <h:panelGrid border="0" columns="2"  columnClasses="longtext"
        rendered="#{assessmentSettings.valueMap.passwordRequired_isInstructorEditable==true}">
        <h:outputLabel for="username" value="#{assessmentSettingsMessages.high_security_username}"/>
        <h:inputText id="username" size="20" value="#{assessmentSettings.username}"/>
        <h:outputLabel for="password" value="#{assessmentSettingsMessages.high_security_password}"/>
        <h:inputText id="password" size="20" value="#{assessmentSettings.password}"/>
      </h:panelGrid>
    </h:panelGrid>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** TIMED *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.timedAssessment_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_timed_assessment}">
    <f:verbatim><div class="tier2"></f:verbatim>
<%--DEBUGGING:
     Time Limit= <h:outputText value="#{assessmentSettings.timeLimit}" /> ;
     Hours= <h:outputText value="#{assessmentSettings.timedHours}" /> ;
     Min= <h:outputText value="#{assessmentSettings.timedMinutes}" /> ;
     hasQuestions?= <h:outputText value="#{not assessmentSettings.hasQuestions}" />
--%>
    <h:panelGrid
        summary="#{templateMessages.timed_assmt_sec}">
      <h:panelGroup>
        <h:selectBooleanCheckbox id="selTimeAssess" onclick="checkUncheckTimeBox();setBlockDivs();document.forms[0].onsubmit();document.forms[0].submit();"
         value="#{assessmentSettings.valueMap.hasTimeAssessment}">
				</h:selectBooleanCheckbox>
        <h:outputText value="#{assessmentSettingsMessages.timed_assessment} " />
				<h:selectOneMenu id="timedHours" value="#{assessmentSettings.timedHours}" disabled="#{!assessmentSettings.valueMap.hasTimeAssessment}" >
          <f:selectItems value="#{assessmentSettings.hours}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_hours}. " />
        <h:selectOneMenu id="timedMinutes" value="#{assessmentSettings.timedMinutes}" disabled="#{!assessmentSettings.valueMap.hasTimeAssessment}">
          <f:selectItems value="#{assessmentSettings.mins}" />
        </h:selectOneMenu>
        <h:outputText value="#{assessmentSettingsMessages.timed_minutes}. " />
       <f:verbatim><br/></f:verbatim>
        <h:outputText value="#{assessmentSettingsMessages.auto_submit_description}" />
      </h:panelGroup>
    </h:panelGrid>
<%-- SAK-3578: auto submit will always be true for timed assessment,
     so no need to have this option
    <h:panelGrid>
      <h:panelGroup rendered="#{assessmentSettings.valueMap.timedAssessmentAutoSubmit_isInstructorEditable==true}">
       <h:selectBooleanCheckbox id="automatic"
         value="#{assessmentSettings.autoSubmit}"/>
        <h:outputText value="#{assessmentSettingsMessages.auto_submit}" />
     </h:panelGroup>
    </h:panelGrid>
       <h:outputText value="#{assessmentSettingsMessages.autoSummit_warning}" />
--%>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** ASSESSMENT ORGANIZATION *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.itemAccessType_isInstructorEditable==true or assessmentSettings.valueMap.displayChucking_isInstructorEditable==true or assessmentSettings.valueMap.displayNumbering_isInstructorEditable==true }" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_assessment_organization}" >
  <f:verbatim> <div class="tier2"></f:verbatim>
    <!-- NAVIGATION -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.itemAccessType_isInstructorEditable==true}">
  <f:verbatim> <div class="longtext"></f:verbatim> <h:outputLabel for="itemNavigation" value="#{assessmentSettingsMessages.navigation}" /><f:verbatim></div><div class="tier3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="itemNavigation" value="#{assessmentSettings.itemNavigation}"  layout="pageDirection" 
		onclick="setBlockDivs();submitForm();">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.linear_access}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.random_access}"/>
        </h:selectOneRadio>
      </h:panelGrid>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- QUESTION LAYOUT -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.displayChunking_isInstructorEditable==true}">
    <f:verbatim><div class="longtext"></f:verbatim><h:outputLabel for="assessmentFormat" value="#{assessmentSettingsMessages.question_layout}" /><f:verbatim></div><div class="tier3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="assessmentFormat" value="#{assessmentSettings.assessmentFormat}"  layout="pageDirection"  rendered="#{assessmentSettings.itemNavigation!=1}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.layout_by_assessment}"/>
        </h:selectOneRadio>
	 </h:panelGrid>
	 <!-- If "linear access" is selected, checked layout by question radio button and then disable all three radio buttons -->
	 <!-- Here we just manipulate the displayed value. The value of assessmentFormat is updated in SaveAssessmentSeetings.java -->
	 <h:panelGrid columns="2"  >
		<h:selectOneRadio id="assessmentFormat2" value="1"  layout="pageDirection"  disabled="true" rendered="#{assessmentSettings.itemNavigation==1}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.layout_by_question}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.layout_by_part}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.layout_by_assessment}"/>
        </h:selectOneRadio>
      </h:panelGrid>
	<f:verbatim></div></f:verbatim>
    </h:panelGroup>


    <!-- NUMBERING -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.displayNumbering_isInstructorEditable==true}">
     <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel for="itemNumbering" value="#{assessmentSettingsMessages.numbering}" /> <f:verbatim> </div><div class="tier3"> </f:verbatim>
       <h:panelGrid columns="2"  >
         <h:selectOneRadio id="itemNumbering" value="#{assessmentSettings.itemNumbering}"  layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.continous_numbering}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.part_numbering}"/>
         </h:selectOneRadio>
      </h:panelGrid>
 <f:verbatim></div></f:verbatim>
    </h:panelGroup>
 <f:verbatim></div></f:verbatim>

  </samigo:hideDivision>
</h:panelGroup>

<!-- *** MARK FOR REVIEW *** -->
<!-- *** (disabled for linear assessment) *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.markForReview_isInstructorEditable==true}">
  <samigo:hideDivision title="#{assessmentSettingsMessages.mark_for_review}" >
    <f:verbatim><div class="tier2"></f:verbatim>
    <h:panelGrid columns="1">
	  <!-- random navigation -->
      <h:panelGroup rendered="#{assessmentSettings.itemNavigation != 1}">
        <h:selectBooleanCheckbox id="markForReview1" value="#{assessmentSettings.isMarkForReview}"/>
        <h:outputLabel value="#{assessmentSettingsMessages.mark_for_review_label}"/>
        <h:outputLink title="#{assessmentSettingsMessages.whats_this_link}" value="#" onclick="javascript:window.open('markForReviewPopUp.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');" onkeypress="javascript:window.open('markForReviewTipText.faces','MarkForReview','width=300,height=220,scrollbars=yes, resizable=yes');" >
            <h:outputText  value=" #{assessmentSettingsMessages.whats_this_link}"/>
        </h:outputLink>
      </h:panelGroup>
  	  <!-- linear navigation -->
	  <h:panelGroup rendered="#{assessmentSettings.itemNavigation == 1}">
        <h:selectBooleanCheckbox id="markForReview2" value="false" disabled="true"/>
        <h:outputLabel value="#{assessmentSettingsMessages.mark_for_review_label}"/>
      </h:panelGroup>
      <h:outputText value="#{assessmentSettingsMessages.mark_for_review_text_1}" />
	  <h:outputText value="#{assessmentSettingsMessages.mark_for_review_text_2}" />
    </h:panelGrid>
	 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** SUBMISSIONS *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.submissionModel_isInstructorEditable==true or assessmentSettings.valueMap.lateHandling_isInstructorEditable==true or assessmentSettings.valueMap.autoSave_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_submissions}" >
 <f:verbatim><div class="tier2"></f:verbatim>

    <!-- NUMBER OF SUBMISSIONS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.submissionModel_isInstructorEditable==true}">
      <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel for="unlimitedSubmissions" value="#{assessmentSettingsMessages.submissions}" /> <f:verbatim> </div> <div class="tier3"></f:verbatim>
      <f:verbatim><table><tr><td></f:verbatim>
        <h:selectOneRadio id="unlimitedSubmissions" value="#{assessmentSettings.unlimitedSubmissions}" layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.unlimited_submission}"/>
          <f:selectItem itemValue="0" itemLabel="#{assessmentSettingsMessages.only}" />
        </h:selectOneRadio>
      <f:verbatim></td><td valign="bottom"></f:verbatim>
        <h:panelGroup>
          <h:inputText size="5"  id="submissions_Allowed" value="#{assessmentSettings.submissionsAllowed}" />
          <h:outputLabel for="submissions_Allowed" value="#{assessmentSettingsMessages.limited_submission}" />
        </h:panelGroup>
      <f:verbatim></td></tr></table></div></f:verbatim>
    </h:panelGroup>

    <!-- LATE HANDLING -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.lateHandling_isInstructorEditable==true}">
      <f:verbatim> <div class="longtext"> </f:verbatim> <h:outputLabel for="lateHandling" value="#{assessmentSettingsMessages.late_handling}" /> <f:verbatim> </div></f:verbatim>
<f:verbatim><div class="tier3"></f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="lateHandling" value="#{assessmentSettings.lateHandling}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.not_accept_latesubmission}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.accept_latesubmission}"/>
        </h:selectOneRadio>
      </h:panelGrid>
   <f:verbatim> </div> </f:verbatim>
    </h:panelGroup>

    <!-- AUTOMATIC SUBMISSION -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.automaticSubmission_isInstructorEditable==true}">
      <f:verbatim> <div class="longtext"> </f:verbatim> 
      <h:outputLabel value="#{assessmentSettingsMessages.automatic_submission}" />
      <f:verbatim> </div></f:verbatim>
      <f:verbatim><div class="tier3"></f:verbatim>
      <h:panelGrid columns="1" border="0">
	    <h:panelGroup>
	      <h:selectBooleanCheckbox value="#{assessmentSettings.autoSubmit}"/>
          <h:outputLabel value="#{assessmentSettingsMessages.auto_submit}"/>
        </h:panelGroup>
		<h:panelGroup>
          <f:verbatim>&nbsp;</f:verbatim>
          <h:outputText value="#{assessmentSettingsMessages.automatic_submission_note_1}"/>
		</h:panelGroup>
      </h:panelGrid>
      <f:verbatim> </div> </f:verbatim>
    </h:panelGroup>

    <!-- AUTOSAVE -->
<%-- hide for 1.5 release SAM-148
    <h:panelGroup rendered="#{assessmentSettings.valueMap.autoSave_isInstructorEditable==true}">
      <h:outputText value="#{assessmentSettingsMessages.auto_save}" />
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="autoSave" value="#{assessmentSettings.submissionsSaved}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.user_click_save}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.save_automatically}"/>
        </h:selectOneRadio>
      </h:panelGrid>
    </h:panelGroup>
--%>
 <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** SUBMISSION MESSAGE *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.submissionMessage_isInstructorEditable==true or assessmentSettings.valueMap.finalPageURL_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_submission_message}" >
   <f:verbatim><div class="tier2"></f:verbatim>
    <h:panelGrid rendered="#{assessmentSettings.valueMap.submissionMessage_isInstructorEditable==true}">
    <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{assessmentSettingsMessages.submission_message}" /> <f:verbatim><br/></f:verbatim>

<%-- TODO: DETERMINE IF WE CAN USE RENDERED --%>
       <samigo:wysiwyg rows="140" value="#{assessmentSettings.submissionMessage}" hasToggle="yes" >
         <f:validateLength maximum="4000"/>
       </samigo:wysiwyg>
<f:verbatim></div></f:verbatim>
    </h:panelGrid>
    <f:verbatim><br/></f:verbatim>
    <h:panelGroup rendered="#{assessmentSettings.valueMap.finalPageURL_isInstructorEditable==true}">
     <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel for="finalPageUrl" value="#{assessmentSettingsMessages.submission_final_page_url}" /> <f:verbatim><br/></f:verbatim>
      <h:inputText size="80" id="finalPageUrl" value="#{assessmentSettings.finalPageUrl}" />
      <h:commandButton value="#{assessmentSettingsMessages.validateURL}" type="button" onclick="javascript:validateUrl();"/>
   <f:verbatim></div></f:verbatim>
    </h:panelGroup>
    <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** FEEDBACK *** -->
 
<h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackAuthoring_isInstructorEditable==true or assessmentSettings.valueMap.feedbackType_isInstructorEditable==true or assessmentSettings.valueMap.feedbackComponents_isInstructorEditable==true}" >
 <samigo:hideDivision title="#{assessmentSettingsMessages.heading_feedback}" >
  <f:verbatim> <div class="tier2"></f:verbatim>
  <!-- FEEDBACK AUTHORING -->
 <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackAuthoring_isInstructorEditable==true}">
   <f:verbatim><div class="longtext"></f:verbatim>
  <h:outputLabel for="feedbackAuthoring" value="#{assessmentSettingsMessages.feedback_authoring}"/>
    <f:verbatim> </div> </f:verbatim>
     <f:verbatim> <div class="tier3"> </f:verbatim>
     <h:panelGrid border="0" columns="1">
         <h:selectOneRadio id="feedbackAuthoring" value="#{assessmentSettings.feedbackAuthoring}" layout="pageDirection">
           <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.questionlevel_feedback}"/>
           <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.sectionlevel_feedback}"/>
           <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.both_feedback}"/>
         </h:selectOneRadio>
     </h:panelGrid>
  
    <f:verbatim> </div> </f:verbatim>
  </h:panelGroup>
 <!-- FEEDBACK DELIVERY -->
 <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackType_isInstructorEditable==true}">
 <f:verbatim><div class="longtext"></f:verbatim>
   <h:outputLabel for="feedbackDelivery" value="#{assessmentSettingsMessages.feedback_delivery}"/> 
    <f:verbatim></div><div class="tier3"></f:verbatim>
   
      <h:panelGrid border="0" columns="1"  >
        <h:selectOneRadio id="feedbackDelivery" value="#{assessmentSettings.feedbackDelivery}"
           layout="pageDirection" onclick="setBlockDivs();disableAllFeedbackCheck(this.value);">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.immediate_feedback}"/>
          <f:selectItem itemValue="4" itemLabel="#{assessmentSettingsMessages.feedback_on_submission} #{assessmentSettingsMessages.note_of_feedback_on_submission}"/>
          <f:selectItem itemValue="3" itemLabel="#{assessmentSettingsMessages.no_feedback}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.feedback_by_date} "/>
        </h:selectOneRadio>

	    <h:panelGrid columns="7" >
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
          <samigo:datePicker value="#{assessmentSettings.feedbackDateString}" size="25" id="feedbackDate" >
            <f:convertDateTime pattern="#{generalMessages.output_date_picker}" />
          </samigo:datePicker>
        </h:panelGrid>

	    <h:panelGrid columns="7" >
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
		  <h:outputText value=" "/>
          <h:outputText value="#{assessmentSettingsMessages.gradebook_note_f}" />
        </h:panelGrid>
      </h:panelGrid>
  
<f:verbatim></div></f:verbatim>
  </h:panelGroup>

    <!-- FEEDBACK COMPONENTS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.feedbackComponents_isInstructorEditable==true}">
     <f:verbatim> <div class="longtext"> </f:verbatim>  <h:outputLabel value="#{templateMessages.feedback_components_sub}" /> <f:verbatim> </div> <div class="tier3"></f:verbatim>
      <h:panelGrid columns="2"  >

        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentResponse}" id="feedbackCheckbox1"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.student_response}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showQuestionLevelFeedback}" id="feedbackCheckbox2"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.question_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showCorrectResponse}" id="feedbackCheckbox3"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.correct_response}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showSelectionLevelFeedback}" id="feedbackCheckbox4"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.selection_level_feedback}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentScore}" id="feedbackCheckbox5"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.student_assessment_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showGraderComments}" id="feedbackCheckbox6"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.grader_comments}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStudentQuestionScore}" id="feedbackCheckbox7"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.student_question_score}" />
        </h:panelGroup>
        <h:panelGroup>
          <h:selectBooleanCheckbox value="#{assessmentSettings.showStatistics}" id="feedbackCheckbox8"
            disabled="#{assessmentSettings.feedbackDelivery==3}" />
          <h:outputText value="#{assessmentSettingsMessages.statistics_and_histogram}" />
        </h:panelGroup>

      </h:panelGrid>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>
 <f:verbatim></div></f:verbatim>
 </samigo:hideDivision>
</h:panelGroup>


  <!-- *** GRADING *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.testeeIdentity_isInstructorEditable==true or assessmentSettings.valueMap.toGradebook_isInstructorEditable==true or assessmentSettings.valueMap.recordedScore_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_grading}" >
 <f:verbatim><div class="tier2"></f:verbatim>
<%--     DEBUGGING:
     AnonymousGrading= <h:outputText value="#{assessmentSettings.anonymousGrading}" /> ;
--%>

    <h:panelGroup rendered="#{assessmentSettings.valueMap.testeeIdentity_isInstructorEditable==true}"> <f:verbatim> <div class="longtext"></f:verbatim>  <h:outputLabel value="#{assessmentSettingsMessages.student_identity}" /><f:verbatim></div><div class="tier3"> </f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="anonymousGrading" value="#{assessmentSettings.anonymousGrading}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.not_anonymous}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.anonymous}"/>
        </h:selectOneRadio>
      </h:panelGrid>

<f:verbatim></div></f:verbatim>
</h:panelGroup>
    <!-- GRADEBOOK OPTIONS -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.toGradebook_isInstructorEditable==true && assessmentSettings.gradebookExists==true}">
     <f:verbatim>  <div class="longtext">  </f:verbatim> <h:outputLabel for="toDefaultGradebook" value="#{assessmentSettingsMessages.gradebook_options}" /><f:verbatim></div> <div class="tier3"> </f:verbatim>
      <h:panelGrid columns="2" rendered="#{assessmentSettings.firstTargetSelected != 'Anonymous Users'}">
        <h:selectOneRadio id="toDefaultGradebook1" value="#{assessmentSettings.toDefaultGradebook}"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.to_default_gradebook} #{assessmentSettingsMessages.gradebook_note_g}"/>
        </h:selectOneRadio>
      </h:panelGrid>

      <h:panelGrid columns="2" rendered="#{assessmentSettings.firstTargetSelected == 'Anonymous Users'}">
        <h:selectOneRadio id="toDefaultGradebook2" disabled="true" value="2"  layout="pageDirection">
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.to_no_gradebook}"/>
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.to_default_gradebook} #{assessmentSettingsMessages.gradebook_note_g}"/>
        </h:selectOneRadio>
      </h:panelGrid>

	<f:verbatim></div></f:verbatim>
    </h:panelGroup>

    <!-- RECORDED SCORE AND MULTIPLES -->
    <h:panelGroup rendered="#{assessmentSettings.valueMap.recordedScore_isInstructorEditable==true}">
   <f:verbatim>  <div class="longtext">  </f:verbatim> <h:outputLabel for="scoringType" value="#{assessmentSettingsMessages.recorded_score}" /><f:verbatim></div> <div class="tier3"> </f:verbatim>
      <h:panelGrid columns="2"  >
        <h:selectOneRadio id="scoringType" value="#{assessmentSettings.scoringType}"  layout="pageDirection">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.highest_score}"/>
          <f:selectItem itemValue="2" itemLabel="#{assessmentSettingsMessages.last_score}"/>
        </h:selectOneRadio>
      </h:panelGrid>
     <f:verbatim></div></f:verbatim>
    </h:panelGroup>

   <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>


  <!-- *** COLORS AND GRAPHICS	*** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.bgColor_isInstructorEditable==true}" >
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_graphics}" >
    <f:verbatim><div class="tier2"></f:verbatim>
 
        <h:selectOneRadio onclick="uncheckOther(this)" id="background_color" value="#{assessmentSettings.bgColorSelect}">
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_color}"/>
       </h:selectOneRadio>

      <samigo:colorPicker value="#{assessmentSettings.bgColor}" size="10" id="pickColor"/>
       <h:selectOneRadio onclick="uncheckOther(this)" id="background_image" value="#{assessmentSettings.bgImageSelect}"  >
          <f:selectItem itemValue="1" itemLabel="#{assessmentSettingsMessages.background_image}"/>
       </h:selectOneRadio>  
   
        <h:inputText size="80" value="#{assessmentSettings.bgImage}"/>
     
    <f:verbatim></div></f:verbatim>
  </samigo:hideDivision>
</h:panelGroup>

  <!-- *** META *** -->
<h:panelGroup rendered="#{assessmentSettings.valueMap.metadataAssess_isInstructorEditable==true}">
  <samigo:hideDivision title="#{assessmentSettingsMessages.heading_metadata}">
    <f:verbatim><div class="tier2"></f:verbatim>
   <f:verbatim><div class="longtext"></f:verbatim> <h:outputLabel value="#{assessmentSettingsMessages.assessment_metadata}" /> <f:verbatim></div><div class="tier3"></f:verbatim>
    <h:panelGrid columns="2" columnClasses="shorttext">
      <h:outputLabel for="keywords" value="#{assessmentSettingsMessages.metadata_keywords}"/>
      <h:inputText id="keywords" size="80" value="#{assessmentSettings.keywords}"/>

      <h:outputLabel for="objectives" value="#{assessmentSettingsMessages.metadata_objectives}"/>
      <h:inputText id="objectives" size="80" value="#{assessmentSettings.objectives}"/>

      <h:outputLabel for="rubrics" value="#{assessmentSettingsMessages.metadata_rubrics}"/>
      <h:inputText id="rubrics" size="80" value="#{assessmentSettings.rubrics}"/>
    </h:panelGrid>
   <f:verbatim></div><div class="longtext"></f:verbatim>   <h:outputLabel value="#{assessmentSettingsMessages.record_metadata}" /> <f:verbatim></div><div class="tier3"></f:verbatim>
    <h:panelGrid columns="2" >
<%-- see bug# SAM-117 -- no longer required in Samigo
     <h:selectBooleanCheckbox
       rendered="#{assessmentSettings.valueMap.metadataParts_isInstructorEditable==true}"
       value="#{assessmentSettings.valueMap.hasMetaDataForPart}"/>
     <h:outputText value="#{assessmentSettingsMessages.metadata_parts}"
       rendered="#{assessmentSettings.valueMap.metadataParts_isInstructorEditable==true}"/>
--%>
     <h:selectBooleanCheckbox
       rendered="#{assessmentSettings.valueMap.metadataQuestions_isInstructorEditable==true}"
       value="#{assessmentSettings.valueMap.hasMetaDataForQuestions}"/>
     <h:outputText value="#{assessmentSettingsMessages.metadata_questions}"
       rendered="#{assessmentSettings.valueMap.metadataQuestions_isInstructorEditable==true}" />
    </h:panelGrid>
    <f:verbatim></div></div></f:verbatim>
  </samigo:hideDivision>
 </h:panelGroup>
</div>
 <p class="act">

 <!-- save & publish -->
  <h:commandButton  value="#{assessmentSettingsMessages.button_unique_save_and_publish}" type="submit" styleClass="active" rendered="#{assessmentSettings.hasQuestions}"
      action="#{assessmentSettings.getOutcomePublish}" disabled="#{not assessmentSettings.hasQuestions}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.author.PublishAssessmentListener" />
  </h:commandButton>
<h:commandButton  value="#{assessmentSettingsMessages.button_unique_save_and_publish}" type="submit" rendered="#{not assessmentSettings.hasQuestions}"
      action="#{assessmentSettings.getOutcomePublish}" disabled="true">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmPublishAssessmentListener" />
  </h:commandButton>

<!-- save -->
<h:commandButton type="submit" value="#{assessmentSettingsMessages.button_save_settings}" action="#{assessmentSettings.getOutcomeSave}" rendered="#{not assessmentSettings.hasQuestions}" styleClass="active">
      <f:param name="assessmentId" value="#{assessmentSettings.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentSettingsListener" />
  </h:commandButton>

  <h:commandButton type="submit" value="#{assessmentSettingsMessages.button_save_settings}" action="#{assessmentSettings.getOutcomeSave}" rendered="#{assessmentSettings.hasQuestions}">
      <f:param name="assessmentId" value="#{assessmentSettings.assessmentId}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SaveAssessmentSettingsListener" />
  </h:commandButton>

  <!-- cancel -->
  <h:commandButton value="#{assessmentSettingsMessages.button_cancel}" type="submit" action="editAssessment" rendered="#{author.fromPage == 'editAssessment'}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetAssessmentAttachmentListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditAssessmentListener" />
  </h:commandButton>

    <h:commandButton value="#{assessmentSettingsMessages.button_cancel}" type="submit" action="#{author.getFromPage}" rendered="#{author.fromPage != 'editAssessment'}">
	      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.ResetAssessmentAttachmentListener" />
  </h:commandButton>

</p>
</h:form>
<!-- end content -->
</div>
         <script language="javascript" style="text/JavaScript">retainHideUnhideStatus('none');showHideReleaseGroups();</script>

      </body>
    </html>
  </f:view>
