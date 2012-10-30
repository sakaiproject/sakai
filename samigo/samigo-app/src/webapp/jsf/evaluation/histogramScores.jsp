<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{evaluationMessages.title_stat}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
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
<!-- content... -->
 <div class="portletBody">
 
<!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE -->
<h:outputText  value="#{delivery.secureDeliveryHTMLFragment}" escape="false"  />
 
<h:form id="histogram">

  <h:inputHidden id="publishedId" value="#{histogramScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{histogramScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{evaluationMessages.stat_view}"/>
    <h:outputText value="#{evaluationMessages.column} "/>
    <h:outputText value="#{histogramScores.assessmentName} " escape="false"/>
  </h3>
  
     <h:outputText value=" <p class=\"navViewAction\">" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" escape="false"/>
     
    <h:commandLink title="#{evaluationMessages.t_submissionStatus}" action="submissionStatus" immediate="true" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <h:outputText value="#{evaluationMessages.sub_status}" />
      <f:param name="allSubmissions" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.SubmissionStatusListener" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>

    <h:commandLink title="#{evaluationMessages.t_totalScores}" action="totalScores" immediate="true" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{commonMessages.total_scores}" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>

    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
      <h:outputText value="#{evaluationMessages.q_view}" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>

    <h:outputText value="#{evaluationMessages.stat_view}" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>


    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>

    <h:commandLink title="#{evaluationMessages.t_itemAnalysis}" action="detailedStatistics" immediate="true"
      rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" >
      <h:outputText value="#{evaluationMessages.item_analysis}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>


    <h:outputText value=" #{evaluationMessages.separator} "  rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>

    <h:commandLink title="#{commonMessages.export_action}" action="exportResponses" immediate="true"  rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <h:outputText value="#{commonMessages.export_action}" />
  	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportResponsesListener" />
    </h:commandLink>

    <h:outputText value=" </p>" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" escape="false"/>

   <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
   
<div class="tier1">


  <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX  -->
    <h:panelGroup rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
     <h:outputText value="#{evaluationMessages.view} " />
      <h:outputText value="#{evaluationMessages.column} " />

     <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsL"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
      <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsH"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
      <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
     </h:selectOneMenu>
	 <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsA"
       required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '4'}">
       <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
       <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
     </h:selectOneMenu>
    </h:panelGroup>
       
    <h:panelGroup>
 <f:verbatim><h4></f:verbatim>
  <h:outputText value="#{evaluationMessages.tot}" />
   <f:verbatim></h4><div class="tier2"></f:verbatim>

 <h:dataTable value="#{histogramScores.histogramBars}" var="bar" headerClass="navView">

    <h:column>
        <f:facet name="header">
        <h:outputText escape="false" value="<U>#{evaluationMessages.num_points}</U>" /> 
      </f:facet>
        <h:outputText value=" #{bar.rangeInfo}" />
    </h:column>
     <h:column>
<f:facet name="header">
         <h:outputText escape="false" value="<U>#{evaluationMessages.num_students}</U>" />
      </f:facet>
<h:panelGroup>
        <h:graphicImage url="/images/reddot.gif" height="12" width="#{bar.columnHeight}"/>
        <h:outputText value=" #{bar.numStudents}" />
</h:panelGroup>

</h:column>

      
  </h:dataTable>
<f:verbatim></div></f:verbatim>
    </h:panelGroup>

<p class="tier2">
<h:panelGrid columns="2">

<h:outputLabel value="#{evaluationMessages.sub_view}"/>
<h:outputLabel value="#{histogramScores.numResponses}" />

<h:outputLabel value="#{evaluationMessages.tot_score_possible} " />
<h:outputText value="#{histogramScores.roundedTotalPossibleScore}"/>

<h:outputLabel value="#{evaluationMessages.mean_eq}" />
<h:outputText value="#{histogramScores.mean}"/>

<h:outputLabel value="#{evaluationMessages.median}" />
<h:outputText value="#{histogramScores.median}"/>

<h:outputLabel value="#{evaluationMessages.mode}" />
<h:outputText value="#{histogramScores.mode}"/>

<h:outputLabel value="#{evaluationMessages.range_eq}" />
<h:outputText value="#{histogramScores.range}"/>

<h:outputLabel value="#{evaluationMessages.qtile_1_eq}" />
<h:outputText value="#{histogramScores.q1}"/>

<h:outputLabel value="#{evaluationMessages.qtile_3_eq}" />
<h:outputText value="#{histogramScores.q3}"/>

<h:outputLabel value="#{evaluationMessages.std_dev}" />
<h:outputText value="#{histogramScores.standDev}"/>
</h:panelGrid>
</p>

<h:panelGroup>
    <f:verbatim><h4></f:verbatim>
    <h:outputText value="#{evaluationMessages.q_view}" />
    <f:verbatim></h4></f:verbatim>
</h:panelGroup>

<!-- The parts drop down. -->
<h:panelGroup rendered="#{histogramScores.assesmentPartCount > 1}">
    <h:outputText value="#{evaluationMessages.part} " />
    <h:outputText value="#{evaluationMessages.column} " />
    <h:selectOneMenu id="partNumber" onchange="document.forms[0].submit();"
                     value="#{histogramScores.partNumber}" >
	<f:selectItem itemValue="" itemLabel="#{evaluationMessages.all_parts}" />
        <f:selectItems value="#{histogramScores.selectItemParts}"/>
        <f:valueChangeListener
            type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:selectOneMenu>
</h:panelGroup>

  <h:dataTable value="#{histogramScores.partInfo}" var="item">

    <h:column>
      <h:panelGroup>
        <f:verbatim><h4></f:verbatim>
          <h:outputText value="#{item.title}" escape="false" />
        <f:verbatim></h4></f:verbatim>
<f:verbatim><div class="tier2"/></f:verbatim>
        <h:outputText value="#{item.questionText}" escape="false" />

        <h:dataTable value="#{item.histogramBars}" var="bar">
          <h:column>
            <h:panelGrid columns="1">
              <h:panelGroup rendered="#{item.questionType !='13'}">
				<h:graphicImage id="image8" rendered="#{bar.isCorrect}" width="12" height="12"
        			alt="#{evaluationMessages.alt_correct}" url="/images/delivery/checkmark.gif" >
       			</h:graphicImage>
				<h:graphicImage id="image9" rendered="#{!bar.isCorrect}" width="12" height="12"
        			alt="#{evaluationMessages.alt_incorrect}" url="/images/delivery/spacer.gif" >
       			</h:graphicImage>

                <h:graphicImage url="/images/reddot.gif" height="12" width="#{bar.columnHeight}"/>
                <h:outputText value=" #{bar.numStudentsText}" />
              </h:panelGroup>
              
              <h:panelGroup>
              	<h:graphicImage width="12" height="12" url="/images/delivery/spacer.gif" />
              	<h:outputText value="#{bar.label}" escape="false" >
              		<f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
             	</h:outputText>
			  </h:panelGroup>
			  
			  <h:panelGroup rendered="#{item.questionType == '13' }">
			    <f:verbatim><div class="tier3"></f:verbatim>
				<h:dataTable value="#{bar.itemBars}" var="itemBar" >
					<h:column>
					<h:panelGrid columns="3">
					<h:panelGroup>
						<h:outputText value="#{itemBar.itemText}  "/>
						<h:graphicImage url="/images/reddot.gif" height="12" width="#{itemBar.columnHeight}"/>
						<h:outputText value="#{itemBar.numStudentsText}"/> 
					</h:panelGroup>
					</h:panelGrid>
					</h:column>
				</h:dataTable>
				<f:verbatim></div></f:verbatim>
			  </h:panelGroup>
            </h:panelGrid>
          </h:column>
        </h:dataTable>

        <!-- 1-2=mcmc 3=mcsc 4=tf 5=essay 6=file 7=audio 8=FIB 9=matching -->

        <h:panelGrid columns="2" rendered="#{item.questionType == '5' or item.questionType == '6' or item.questionType == '7'}">

          <h:outputLabel value="#{evaluationMessages.responses}" />
          <h:outputText id="responses" value="#{item.numResponses}" />
          <h:outputLabel value="#{evaluationMessages.tot_poss_eq}" />
          <h:outputText id="possible" value="#{item.totalScore}" />
          <h:outputLabel value="#{evaluationMessages.mean_eq}" />
          <h:outputText id="mean" value="#{item.mean}" />
          <h:outputLabel  value="#{evaluationMessages.median}" />
          <h:outputText id="median" value="#{item.median}" />
          <h:outputLabel value="#{evaluationMessages.mode}" />
          <h:outputText id="mode" value="#{item.mode}" />
        </h:panelGrid>
       <h:panelGrid columns="2" rendered="#{item.questionType == '3' or item.questionType == '13'}">
          <h:outputLabel for="responses1" value="#{evaluationMessages.responses}" />
          <h:outputText id="responses1" value="#{item.numResponses}" />
        </h:panelGrid>
        <h:panelGrid columns="2" rendered="#{item.questionType == '1' or  item.questionType == '2' or  item.questionType == '4' or  item.questionType == '8' or item.questionType == '9' or item.questionType == '11' or item.questionType == '12' or item.questionType == '15'}" columnClasses="alignLeft,aligntRight"><!-- // CALCULATED_QUESTION -->
             <h:outputLabel for="responses2" value="#{evaluationMessages.responses}" />
          <h:outputText id="responses2" value="#{item.numResponses}" />
          <h:outputLabel for="percentCorrect" value="#{evaluationMessages.percentCorrect}" />
          <h:outputText id="percentCorrect" value="#{item.percentCorrect}" />
        </h:panelGrid>


      </h:panelGroup>
<f:verbatim></div></f:verbatim> 
    </h:column>
  </h:dataTable>




<h:commandButton value="#{evaluationMessages.return}" action="select" type="submit" rendered="#{histogramScores.hasNav=='false'}"/>
</div>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
