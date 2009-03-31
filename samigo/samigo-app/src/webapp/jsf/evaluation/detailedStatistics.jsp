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
$Id: histogramScores.jsp 38982 2007-12-06 13:05:38Z gopal.ramasammycook@gmail.com $
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
<h:form id="histogram">

  <h:inputHidden id="publishedId" value="#{histogramScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{histogramScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{evaluationMessages.item_analysis}"/>
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
      <h:outputText value="#{evaluationMessages.title_total}" />
    </h:commandLink>
    
    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>
    
    <h:commandLink title="#{evaluationMessages.t_questionScores}" action="questionScores" immediate="true" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
      <h:outputText value="#{evaluationMessages.q_view}" />
    </h:commandLink>

    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" />
    
    <h:commandLink title="#{evaluationMessages.t_histogram}" action="histogramScores" immediate="true"
      rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" >
      <h:outputText value="#{evaluationMessages.stat_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>


    <h:outputText value=" #{evaluationMessages.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>
    
    <h:outputText value="#{evaluationMessages.item_analysis}" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>

    <h:outputText value=" #{evaluationMessages.separator} "  rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>
    
    <h:commandLink title="#{evaluationMessages.t_export}" action="exportResponses" immediate="true"  rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <h:outputText value="#{evaluationMessages.export}" />
  	  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ExportResponsesListener" />
    </h:commandLink>
    
    <h:outputText value=" </p>" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" escape="false"/>

<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

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
    </h:panelGroup>

    <h:panelGroup rendered="#{histogramScores.randomType =='true'}">
    <h:outputText value="#{evaluationMessages.no_histogram_for_random}" />
      </h:panelGroup>






<!-- 
***************************************************
***************************************************
***************************************************
Below added by gopalrc Nov 2007 
***************************************************
***************************************************
***************************************************
-->

<br/>
<br/>
<br/>

  <h:dataTable value="#{histogramScores.detailedStatistics}" var="item" styleClass="listHier lines">

<!-- need to add a randomtype property for histogramQuestionScoreBean (item) and if it's true, hide histogram  -->
<%--
    <h:column rendered="#{histogramScores.randomType =='true'}">
      <h:outputText value="#{evaluationMessages.no_histogram_for_random}" />
    </h:column>
--%>


    <h:column rendered="#{histogramScores.randomType =='false'}">
        <f:facet name="header">
            <h:outputText escape="false" value="#{evaluationMessages.question}" /> 
        </f:facet>
        <h:outputText value="#{item.questionLabel}" escape="false" />
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false'}">
        <f:facet name="header">
            <h:outputText escape="false" value="N" /> 
        </f:facet>
        <h:outputText value="#{item.n}" escape="false" />
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false'}">
        <f:facet name="header">
            <h:outputText escape="false" value="#{evaluationMessages.pct_correct_of}<br/>#{evaluationMessages.whole_group}" /> 
        </f:facet>
        <h:outputText value="#{item.percentCorrect}" escape="false"  rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.showDiscriminationColumn=='true'}">
        <f:facet name="header">
            <h:outputText escape="false" value="#{evaluationMessages.pct_correct_of}<br/>#{evaluationMessages.upper_pct}" /> 
        </f:facet>
        <h:outputText value="#{item.percentCorrectFromUpperQuartileStudents}" escape="false" rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.showDiscriminationColumn=='true'}">
        <f:facet name="header">
            <h:outputText escape="false" value="#{evaluationMessages.pct_correct_of}<br/>#{evaluationMessages.lower_pct}" /> 
        </f:facet>
        <h:outputText value="#{item.percentCorrectFromLowerQuartileStudents}" escape="false"  rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.showDiscriminationColumn=='true'}">
        <f:facet name="header">
            <h:outputText escape="false" value="#{evaluationMessages.discrim_abbrev}" /> 
        </f:facet>
        <h:outputText value="#{item.discrimination}" escape="false"  rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>0}">
        <f:facet name="header">
            <h:outputText escape="false" value="-" /> 
        </f:facet>
        <h:outputText value="#{item.numberOfStudentsWithZeroAnswers}" escape="false" />
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>0}">
        <f:facet name="header">
            <h:outputText escape="false" value="A" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[0].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>0 && !item.histogramBars[0].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[0].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>0 && item.histogramBars[0].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>


    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>1}">
        <f:facet name="header">
            <h:outputText escape="false" value="B" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[1].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>1 && !item.histogramBars[1].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[1].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>1 && item.histogramBars[1].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>


    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>2}">
        <f:facet name="header">
            <h:outputText escape="false" value="C" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[2].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>2 && !item.histogramBars[2].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[2].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>2 && item.histogramBars[2].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>


    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>3}">
        <f:facet name="header">
            <h:outputText escape="false" value="D" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[3].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>3 && !item.histogramBars[3].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[3].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>3 && item.histogramBars[3].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>


    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>4}">
        <f:facet name="header">
            <h:outputText escape="false" value="E" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[4].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>4 && !item.histogramBars[4].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[4].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>4 && item.histogramBars[4].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>


    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>5}">
        <f:facet name="header">
            <h:outputText escape="false" value="F" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[5].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>5 && !item.histogramBars[5].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[5].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>5 && item.histogramBars[5].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>6}">
        <f:facet name="header">
            <h:outputText escape="false" value="G" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[6].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>6 && !item.histogramBars[6].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[6].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>6 && item.histogramBars[6].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>7}">
        <f:facet name="header">
            <h:outputText escape="false" value="H" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[7].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>7 && !item.histogramBars[7].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[7].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>7 && item.histogramBars[7].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>8}">
        <f:facet name="header">
            <h:outputText escape="false" value="I" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[8].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>8 && !item.histogramBars[8].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[8].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>8 && item.histogramBars[8].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>9}">
        <f:facet name="header">
            <h:outputText escape="false" value="J" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[9].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>9 && !item.histogramBars[9].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[9].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>9 && item.histogramBars[9].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>10}">
        <f:facet name="header">
            <h:outputText escape="false" value="K" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[10].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>10 && !item.histogramBars[10].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[10].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>10 && item.histogramBars[10].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>11}">
        <f:facet name="header">
            <h:outputText escape="false" value="L" /> 
        </f:facet>
        <h:outputText value="#{item.histogramBars[11].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>11 && !item.histogramBars[11].isCorrect}"/>
        <h:outputText value="#{item.histogramBars[11].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>11 && item.histogramBars[11].isCorrect}" styleClass="detailedStatsCorrectAnswerText"/>
    </h:column>

    <h:column rendered="#{histogramScores.randomType =='false' && histogramScores.maxNumberOfAnswers>12}">
        <f:facet name="header">
            <h:outputText escape="false" value="#{histogramScores.undisplayedStudentResponseInItemAnalysisColumnHeader}" /> 
        </f:facet>
        <h:outputText value="#{item.sumOfStudentResponsesInUndisplayedItemAnalysisColumns}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers>12 && item.histogramBars[12]!=null}" title="#{item.studentResponsesInUndisplayedItemAnalysisColumns}"/>
    </h:column>

  </h:dataTable>


<!-- 
***************************************************
***************************************************
***************************************************
Above added by gopalrc Nov 2007 
***************************************************
***************************************************
***************************************************
-->





<h:commandButton accesskey="#{evaluationMessages.a_return}"value="#{evaluationMessages.return}" action="select" type="submit" rendered="#{histogramScores.hasNav=='false'}"/>
</div>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
