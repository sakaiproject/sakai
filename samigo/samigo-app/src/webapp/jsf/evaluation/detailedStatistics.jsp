<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text>&#13;
&#13;
&#13;
&#13;<![CDATA[
<!DOCTYPE html]]>&#13;
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"&#13;<![CDATA[
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">]]>&#13;
&#13;
  </jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{evaluationMessages.title_stat}"/>
        </title>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute("html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!--
$Id: histogramScores.jsp 38982 2007-12-06 13:05:38Z gopal.ramasammycook@gmail.com $

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

 -->
          <script>
            <jsp:text>&#13;
  $(document).ready(function(){&#13;
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.&#13;
    var currentLink = $('#histogram\\:detailedStatisticsMenuLink');&#13;
    currentLink.addClass('current');&#13;
    // Remove the link of the current option&#13;
    currentLink.html(currentLink.find('a').text());&#13;
  });&#13;
</jsp:text>
          </script>
          <!-- content...  -->
          <div class="portletBody container-fluid">
            <h:form id="histogram">
              <h:inputHidden id="publishedId" value="#{histogramScores.publishedId}"/>
              <h:inputHidden id="itemId" value="#{histogramScores.itemId}"/>
              <!-- HEADINGS  -->
              <jsp:directive.include file="/jsf/evaluation/evaluationHeadings.jsp"/>
              <h:panelGroup layout="block" styleClass="page-header">
                <h1>
                  <h:outputText value="#{evaluationMessages.item_analysis}#{evaluationMessages.column} " escape="false"/>
                  <small>
                    <h:outputText value="#{histogramScores.assessmentName} " escape="false"/>
                  </small>
                </h1>
              </h:panelGroup>
              <!-- EVALUATION SUBMENU  -->
              <jsp:directive.include file="/jsf/evaluation/evaluationSubmenu.jsp"/>
              <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
              <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX   -->
              <div class="b5 d-flex justify-content-between my-2">
                <div>
                  <h:panelGroup rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
                    <h:outputText value="#{evaluationMessages.view} "/>
                    <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsL" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
                      <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}"/>
                      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}"/>
                      <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                    </h:selectOneMenu>
                    <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsH" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
                      <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}"/>
                      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}"/>
                      <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                    </h:selectOneMenu>
                    <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsA" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '4'}">
                      <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}"/>
                      <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                    </h:selectOneMenu>
                  </h:panelGroup>
                </div>
                <div class="dropdown">
                  <button class="btn btn-link dropdown-toggle" name="Export Button" type="button" data-bs-toggle="dropdown">
                    <h:outputText value="#{evaluationMessages.export}"/>
                    <jsp:element name="span">
                      <jsp:attribute name="class" trim="false">caret</jsp:attribute>
                      <jsp:body>
                      </jsp:body>
                    </jsp:element>
                  </button>
                  <ul class="dropdown-menu row">
                    <li>
                      <h:outputLink value="#{histogramScores.exportItemAnalysisPdf}" styleClass="d-block" title="#{evaluationMessages.export_as_pdf}" target="_blank">
                        <h:outputText value="#{evaluationMessages.export_pdf}"/>
                      </h:outputLink>
                    </li>
                    <li>
                      <h:outputLink value="#{histogramScores.exportItemAnalysisXlsx}" styleClass="d-block" title="#{evaluationMessages.export_as_xlsx}" target="_blank">
                        <h:outputText value="#{evaluationMessages.export_xlsx}"/>
                      </h:outputLink>
                    </li>
                  </ul>
                </div>
              </div>
              <div class="table-responsive">
                <h:dataTable value="#{histogramScores.detailedStatistics}" var="item" styleClass="table table-bordered table-striped">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.question}"/>
                    </f:facet>
                    <h:outputText value="#{item.questionLabel}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.randomType =='false'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="N"/>
                    </f:facet>
                    <h:outputText value="#{item.numResponses}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.randomType =='true'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="N(#{histogramScores.numResponses})"/>
                    </f:facet>
                    <h:outputText value="#{item.numResponses}" escape="false"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.pct_correct_of}&lt;br/&gt;#{evaluationMessages.whole_group}"/>
                    </f:facet>
                    <h:outputText value="#{item.percentCorrect}" escape="false" rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.showDiscriminationColumn=='true'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.pct_correct_of}&lt;br/&gt;#{evaluationMessages.upper_pct}"/>
                    </f:facet>
                    <h:outputText value="#{item.percentCorrectFromUpperQuartileStudents}" escape="false" rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.showDiscriminationColumn=='true'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.pct_correct_of}&lt;br/&gt;#{evaluationMessages.lower_pct}"/>
                    </f:facet>
                    <h:outputText value="#{item.percentCorrectFromLowerQuartileStudents}" escape="false" rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.showDiscriminationColumn=='true'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.discrim_abbrev}"/>
                    </f:facet>
                    <h:outputText value="#{item.discrimination}" escape="false" rendered="#{item.showPercentageCorrectAndDiscriminationFigures}"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers &gt; 0}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.difficulty}"/>
                    </f:facet>
                    <h:outputText value="#{item.difficulty}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers &gt; 0}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.total_correct}"/>
                    </f:facet>
                    <h:outputText value="#{item.numberOfStudentsWithCorrectAnswers}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers &gt; 0}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.total_incorrect}"/>
                    </f:facet>
                    <h:outputText value="#{item.numberOfStudentsWithIncorrectAnswers}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;0}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.no_answer}"/>
                    </f:facet>
                    <h:outputText value="#{item.numberOfStudentsWithZeroAnswers}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.showObjectivesColumn=='true'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.obj}"/>
                    </f:facet>
                    <h:outputText value="#{item.objectives}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.showObjectivesColumn=='true'}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.keywords}"/>
                    </f:facet>
                    <h:outputText value="#{item.keywords}" escape="false"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;0}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="A"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[0].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;0 &amp;&amp; !item.histogramBars[0].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[0].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;0 &amp;&amp; item.histogramBars[0].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;1}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="B"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[1].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;1 &amp;&amp; !item.histogramBars[1].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[1].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;1 &amp;&amp; item.histogramBars[1].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;2}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="C"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[2].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;2 &amp;&amp; !item.histogramBars[2].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[2].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;2 &amp;&amp; item.histogramBars[2].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;3}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="D"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[3].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;3 &amp;&amp; !item.histogramBars[3].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[3].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;3 &amp;&amp; item.histogramBars[3].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;4}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="E"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[4].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;4 &amp;&amp; !item.histogramBars[4].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[4].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;4 &amp;&amp; item.histogramBars[4].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;5}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="F"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[5].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;5 &amp;&amp; !item.histogramBars[5].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[5].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;5 &amp;&amp; item.histogramBars[5].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;6}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="G"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[6].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;6 &amp;&amp; !item.histogramBars[6].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[6].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;6 &amp;&amp; item.histogramBars[6].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;7}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="H"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[7].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;7 &amp;&amp; !item.histogramBars[7].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[7].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;7 &amp;&amp; item.histogramBars[7].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;8}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="I"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[8].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;8 &amp;&amp; !item.histogramBars[8].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[8].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;8 &amp;&amp; item.histogramBars[8].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;9}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="J"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[9].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;9 &amp;&amp; !item.histogramBars[9].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[9].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;9 &amp;&amp; item.histogramBars[9].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;10}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="K"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[10].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;10 &amp;&amp; !item.histogramBars[10].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[10].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;10 &amp;&amp; item.histogramBars[10].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;11}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="L"/>
                    </f:facet>
                    <h:outputText value="#{item.histogramBars[11].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;11 &amp;&amp; !item.histogramBars[11].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}"/>
                    <h:outputText value="#{item.histogramBars[11].numStudents}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;11 &amp;&amp; item.histogramBars[11].isCorrect &amp;&amp; item.showIndividualAnswersInDetailedStatistics}" styleClass="detailedStatsCorrectAnswerText"/>
                  </h:column>
                  <h:column rendered="#{histogramScores.maxNumberOfAnswers&gt;12}">
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{histogramScores.undisplayedStudentResponseInItemAnalysisColumnHeader}"/>
                    </f:facet>
                    <h:outputText value="#{item.sumOfStudentResponsesInUndisplayedItemAnalysisColumns}" escape="false" rendered="#{histogramScores.maxNumberOfAnswers&gt;12 &amp;&amp; item.histogramBars[12]!=null}" title="#{item.studentResponsesInUndisplayedItemAnalysisColumns}"/>
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
                <h:commandButton value="#{evaluationMessages.return_s}" action="select" type="submit" rendered="#{histogramScores.hasNav=='false'}"/>
              </div>
            </h:form>
          </div>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
