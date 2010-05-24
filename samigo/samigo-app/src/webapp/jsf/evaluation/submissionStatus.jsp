<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
  
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
  

<!--
$Id$
<%--
***********************************************************************************
*
* Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
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

<f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{evaluationMessages.sub_status}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->

<!-- JAVASCRIPT -->
<%@ include file="/js/delivery.js" %>

<script>
function clickEmailLink(field, fromName, fromEmailAddress, toName, toEmailAddress, assessmentName){
var emaillinkid= field.id.replace("createEmail", "hiddenlink");
//fromName = escapeApostrophe(fromName);
//to = escapeApostrophe(toName);
var newindex = 0;
for (i=0; i<document.links.length; i++) {
  if(document.links[i].id == emaillinkid)
  {
    newindex = i;
    break;
  }
}

document.links[newindex].onclick();
window.open("../evaluation/createNewEmail.faces?fromEmailLinkClick=true&fromName=" + fromName + "&fromEmailAddress=" + fromEmailAddress + "&toName=" + toName + "&toEmailAddress=" + toEmailAddress +  "&assessmentName=" + assessmentName,'createEmail','width=600,height=600,scrollbars=yes, resizable=yes');

document.location='../evaluation/submissionStatus';
}
/*
function escapeApostrophe(name) {
	//alert(name);
	if (name.indexOf('\'') < 0) {
		//alert("<0");
		finalName = name;
	}
	else {
		//alert(">=0");
	var finalName;
	for (i=0; i<name.length; i++) {
	  //alert(name.charAt(i));
	  
    if (name.charAt(i) != '\'') {
		finalName = finalName + name.charAt(i);
    }
	else {
		finalName = finalName + '\\' + name.charAt(i);
	}
  }
}
  //alert("finalName=" + finalName);
  return finalName;
}
*/
</script>

 <div class="portletBody">
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{evaluationMessages.sub_status}"/>
    <h:outputText value="#{evaluationMessages.column} "/>
    <h:outputText value="#{totalScores.assessmentName} " escape="false"/>
  </h3>
  
  <p class="navViewAction">
    <h:outputText value="#{evaluationMessages.sub_status}" />
    
    <h:outputText value=" #{evaluationMessages.separator} " />
    
    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{evaluationMessages.title_total}" />
    </h:commandLink>
    
    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne ''}" />
    
    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{evaluationMessages.q_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    
    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" />
    
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" />
    
    <h:commandLink title="#{evaluationMessages.t_itemAnalysis}" action="detailedStatistics" immediate="true"
      rendered="#{totalScores.firstItem ne '' && !totalScores.hasRandomDrawPart}" >
      <h:outputText value="#{evaluationMessages.item_analysis}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>


    <h:outputText value=" #{evaluationMessages.separator} " />
    
    <h:commandLink title="#{evaluationMessages.t_export}" action="exportResponses" immediate="true">
      <h:outputText value="#{evaluationMessages.export}" />
  	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportResponsesListener" />
    </h:commandLink>

  </p>

  <h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

  <div class="tier1">
     <h:outputText value="#{evaluationMessages.max_score_poss}" style="instruction"/>
     <h:outputText value="#{totalScores.maxScore}" style="instruction"/>
     <br />


<sakai:flowState bean="#{submissionStatus}" />
<h:panelGrid columns="2" columnClasses="samLeftNav,samRightNav" width="100%">
  <h:panelGroup>
    <h:panelGrid columns="1" columnClasses="samLeftNav" width="100%">
	  <h:panelGroup>
        <!-- SECTION AWARE -->
        <h:outputText value="#{evaluationMessages.view}"/>
        <h:outputText value="#{evaluationMessages.column}"/>
        <h:selectOneMenu value="#{submissionStatus.selectedSectionFilterValue}" id="sectionpicker" required="true" onchange="document.forms[0].submit();">
          <f:selectItems value="#{totalScores.sectionFilterSelectItems}"/>
          <f:valueChangeListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener"/>
        </h:selectOneMenu>
      </h:panelGroup>
	
	  <h:panelGroup>
			<h:outputText value="#{evaluationMessages.search}"/>
            <h:outputText value="#{evaluationMessages.column}"/>
			<h:inputText
				id="searchString"
				value="#{submissionStatus.searchString}"
				onfocus="clearIfDefaultString(this, '#{evaluationMessages.search_default_student_search_string}')"
				onkeypress="return submitOnEnter(event, 'editTotalResults:searchSubmitButton');"/>
			<f:verbatim> </f:verbatim>
			<h:commandButton actionListener="#{submissionStatus.search}" value="#{evaluationMessages.search_find}" id="searchSubmitButton" />
			<f:verbatim> </f:verbatim>
			<h:commandButton actionListener="#{submissionStatus.clear}" value="#{evaluationMessages.search_clear}"/>
	  </h:panelGroup>
    </h:panelGrid>
  </h:panelGroup>

  <h:panelGroup>
	<div>
	<sakai:pager id="pager" totalItems="#{submissionStatus.dataRows}" firstItem="#{submissionStatus.firstRow}" pageSize="#{submissionStatus.maxDisplayedRows}" textStatus="#{evaluationMessages.paging_status}" />
	</div>
  </h:panelGroup>
</h:panelGrid>

  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
<div class="tier2">
  <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier" id="totalScoreTable" value="#{submissionStatus.agents}"
    var="description">
    <!-- NAME/SUBMISSION ID -->

    <h:column rendered="#{submissionStatus.sortType ne 'lastName'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" immediate="true" id="lastName" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="lastName" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
       <h:outputText value="#{description.firstName}" />
       <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:outputLink id="createEmail1" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{description.firstName} #{description.lastName}\", '#{description.email}', '#{totalScores.assessmentName}');" value="#">
	     <h:outputText value="  #{evaluationMessages.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
	   </h:outputLink>
	 </span>
     </span>
     </h:panelGroup>
	 	<h:commandLink id="hiddenlink1" value="" action="submissionStatus" immediate="true">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toUserId" value="#{description.idString}" />
	 </h:commandLink>

	 <h:outputText value=" #{evaluationMessages.separator} " rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != '' &&  description.retakeAllowed}" />

	 <span class="itemAction">
     <h:commandLink title="#{evaluationMessages.t_retake}" id="retakeAssessment1" immediate="true" 
        rendered="#{description.retakeAllowed}"
        action="confirmRetake">
        <h:outputText value="#{evaluationMessages.allow_retake}"/>
        <f:param name="agentIdString" value="#{description.idString}" />
        <f:param name="publishedAssessmentId" value="#{totalScores.publishedId}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ConfirmRetakeAssessmentListener" />
     </h:commandLink>
	 </span>
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'lastName' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortBy" value="lastName" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
       <h:outputText value="#{description.firstName}" />
       <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:outputLink id="createEmail2" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{description.firstName} #{description.lastName}\",' #{description.email}', '#{totalScores.assessmentName}');" value="#">
	     <h:outputText value="  #{evaluationMessages.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
	   </h:outputLink>
	 </span>
	 </span>
     </h:panelGroup>
	 <h:commandLink id="hiddenlink2" value="" action="submissionStatus">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toUserId" value="#{description.idString}" />
	</h:commandLink>
	 <h:outputText value=" #{evaluationMessages.separator} " rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != '' &&  description.retakeAllowed}" />

	 <span class="itemAction">
     <h:commandLink title="#{evaluationMessages.t_retake}" id="retakeAssessment2" immediate="true" 
        rendered="#{description.retakeAllowed}"
        action="confirmRetake">
        <h:outputText value="#{evaluationMessages.allow_retake}"/>
        <f:param name="agentIdString" value="#{description.idString}" />
        <f:param name="publishedAssessmentId" value="#{totalScores.publishedId}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ConfirmRetakeAssessmentListener" />
     </h:commandLink>
	 </span>

    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'lastName' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortLastName}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.name}" />
          <f:param name="sortBy" value="lastName" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortLastNameAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
     <h:panelGroup>
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />
       <h:outputText value="#{description.lastName}" />
         <h:outputText value=", " rendered="#{description.lastInitial ne 'Anonymous'}"/>
       <h:outputText value="#{description.firstName}" />
       <h:outputText value="#{evaluationMessages.na}" rendered="#{description.lastInitial eq 'Anonymous'}" />
       <f:verbatim><br/></f:verbatim>
	   <span class="itemAction">
	   <h:outputLink id="createEmail3" onclick="clickEmailLink(this, \"#{totalScores.graderName}\", '#{totalScores.graderEmailInfo}', \"#{description.firstName} #{description.lastName}\", '#{description.email}', '#{totalScores.assessmentName}');" value="#">
	     <h:outputText value="  #{evaluationMessages.email}" rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != ''}" />
	   </h:outputLink>
	 </span>
     </span>
     </h:panelGroup>
	<h:commandLink id="hiddenlink3" value="" action="submissionStatus">
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.util.EmailListener" />
		  <f:param name="toUserId" value="#{description.idString}" />
	</h:commandLink>
	<h:outputText value=" #{evaluationMessages.separator} " rendered="#{description.email != null && description.email != '' && email.fromEmailAddress != null && email.fromEmailAddress != '' &&  description.retakeAllowed}" />

	 <span class="itemAction">
     <h:commandLink title="#{evaluationMessages.t_retake}" id="retakeAssessment3" immediate="true" 
        rendered="#{description.retakeAllowed}"
        action="confirmRetake">
        <h:outputText value="#{evaluationMessages.allow_retake}"/>
        <f:param name="agentIdString" value="#{description.idString}" />
        <f:param name="publishedAssessmentId" value="#{totalScores.publishedId}" />
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ConfirmRetakeAssessmentListener" />
     </h:commandLink>
	 </span>

    </h:column>


   <!-- STUDENT ID -->
    <h:column  rendered="#{submissionStatus.sortType ne 'agentEid'}" >
     <f:facet name="header">
       <h:commandLink title="#{evaluationMessages.t_sortUserId}" id="agentEid" action="submissionStatus" >
          <h:outputText value="#{evaluationMessages.uid}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="agentEid" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.agentEid}" />
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'agentEid' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.agentEid}" />
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'agentEid' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortUserId}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.uid}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortUserIdAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.agentEid}" />
    </h:column>


    <!-- ROLE -->
    <h:column rendered="#{submissionStatus.sortType ne 'role'}">
     <f:facet name="header" >
        <h:commandLink title="#{evaluationMessages.t_sortRole}" id="role" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.role}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="role" />
        <f:param name="sortAscending" value="true"/>
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'role' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.role}" />
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'role' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortRole}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.role}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortRoleAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
       <h:outputText value="#{description.role}" />
    </h:column>

    <!-- DATE -->
    <h:column rendered="#{submissionStatus.sortType ne 'submittedDate'}">
     <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" id="submittedDate" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.date}" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
        <f:param name="sortBy" value="submittedDate" />
        </h:commandLink>
     </f:facet>
        <h:outputText rendered="#{description.submittedDate !=null && description.submittedDate ne ''}" value="#{description.submittedDate}">
          <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
		<h:outputText rendered="#{description.submittedDate == null || description.submittedDate eq ''}" value="#{evaluationMessages.no_submission}"/>
    </h:column>
	
	<h:column rendered="#{submissionStatus.sortType eq 'submittedDate' && submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.date}" />
          <f:param name="sortAscending" value="false" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateDescending}" rendered="#{submissionStatus.sortAscending}" url="/images/sortascending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText rendered="#{description.submittedDate !=null && description.submittedDate ne ''}" value="#{description.submittedDate}">
           <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
		<h:outputText rendered="#{description.submittedDate == null || description.submittedDate eq ''}" value="#{evaluationMessages.no_submission}"/>
    </h:column>

	<h:column rendered="#{submissionStatus.sortType eq 'submittedDate' && !submissionStatus.sortAscending}">
      <f:facet name="header">
        <h:commandLink title="#{evaluationMessages.t_sortSubmittedDate}" action="submissionStatus">
          <h:outputText value="#{evaluationMessages.date}" />
          <f:param name="sortAscending" value="true" />
          <h:graphicImage alt="#{evaluationMessages.alt_sortSubmittedDateAscending}" rendered="#{!submissionStatus.sortAscending}" url="/images/sortdescending.gif"/>
          <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
          </h:commandLink>    
      </f:facet>
        <h:outputText rendered="#{description.submittedDate !=null && description.submittedDate ne ''}" value="#{description.submittedDate}">
           <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
        </h:outputText>
		<h:outputText rendered="#{description.submittedDate == null || description.submittedDate eq ''}" value="#{evaluationMessages.no_submission}"/>
    </h:column>

  </h:dataTable>
</div>
</div>

</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
