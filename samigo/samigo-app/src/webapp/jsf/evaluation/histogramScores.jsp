<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
  PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<f:view>
  <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">

  <head>
    <%= request.getAttribute("html.head") %>
    <title>
      <h:outputText value="#{evaluationMessages.title_stat}" />
    </title>
    <link href="/samigo-app/css/print/print.css" type="text/css" rel="stylesheet" media="print" />
  </head>

  <body onload="<%= request.getAttribute(" html.body.onload") %>">
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
<script>
  $(document).ready(function(){
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
    var currentLink = $('#histogram\\:histogramScoresMenuLink');
    currentLink.addClass('current');
    // Remove the link of the current option
    currentLink.html(currentLink.find('a').text());
  });
</script>
    <!-- content... -->
    <div class="portletBody container-fluid">

      <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE -->
      <h:outputText value="#{delivery.secureDeliveryHTMLFragment}" escape="false" />

      <h:form id="histogram">
        <h:inputHidden id="publishedId" value="#{histogramScores.publishedId}" />
        <h:inputHidden id="itemId" value="#{histogramScores.itemId}" />

        <!-- HEADINGS -->
        <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

        <h:panelGroup layout="block" styleClass="page-header">
          <h1>
            <h:outputText value="#{evaluationMessages.stat_view}#{evaluationMessages.column} " escape="false" />
            <small><h:outputText value="#{histogramScores.assessmentName} " escape="false" /></small>
          </h1>
        </h:panelGroup>

        <!-- EVALUATION SUBMENU -->
        <%@ include file="/jsf/evaluation/evaluationSubmenu.jsp" %>

        <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table" />

        <div class="tier1">

          <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX  -->
          <h:panelGroup rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
            <h:outputText value="#{evaluationMessages.view} " />

            <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsL" required="true"
              onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
              <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
              <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}" />
              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
            </h:selectOneMenu>

            <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsH" required="true"
              onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
              <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
              <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}" />
              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
            </h:selectOneMenu>
            <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsA" required="true"
              onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '4'}">
              <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}" />
              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
            </h:selectOneMenu>
          </h:panelGroup>

          <br /><br />

          <script type="text/javascript" src="/library/webjars/jquery/1.12.4/jquery.min.js"></script>

          <h:outputText escape="false" value="<!--[if lt IE 9]>" />
          <script type="text/javascript" src="/library/webjars/explorercanvas/r3/excanvas.min.js"></script>
          <h:outputText escape="false" value="<![endif]-->" />
          <script type="text/javascript" src="/library/webjars/jqplot/1.0.9.d96a669/jquery.jqplot.min.js"></script>
          <script type="text/javascript"
            src="/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.barRenderer.js"></script>
          <script type="text/javascript"
            src="/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.categoryAxisRenderer.js"></script>
          <script type="text/javascript"
            src="/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.highlighter.js"></script>
          <script type="text/javascript"
            src="/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.canvasTextRenderer.js"></script>
          <script type="text/javascript"
            src="/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.canvasAxisLabelRenderer.js"></script>
          <script type="text/javascript"
            src="/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.pointLabels.js"></script>
          <link href="/library/webjars/jqplot/1.0.9.d96a669/jquery.jqplot.css" type="text/css" rel="stylesheet" />
          <h:outputText escape="false" value="
	<script>
	 window.onload=function(){
     var dataSet=#{histogramScores.histogramChartOptions};
     $('#chartdiv').height(($('.panel-stats').height() - 89)+'px');
		var plot1=$.jqplot('chartdiv',dataSet,{
			seriesDefaults:{
			  shadow:false,
			  pointLabels:{
				  show:true,
				  formatString: '%d'
			  },
			  renderer:$.jqplot.BarRenderer
			},
			axes:{
			  yaxis:{
			    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
			    label:'#{evaluationMessages.num_students}',
	                    labelOptions:{
                              fontFamily:'Helvetica',
                              fontSize: '14pt'
                            }
			  },
			  xaxis:{
			    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
			    label:'#{evaluationMessages.num_points}',
			    renderer:$.jqplot.CategoryAxisRenderer,
	                    labelOptions:{
                              fontFamily:'Helvetica',
                              fontSize: '14pt'
                            }
			  }
			}
		});
          $('.presentation').attr('role','presentation');
	        $(window).resize(function(){plot1.replot(dataSet);});
	}</script>" />

          <h:panelGrid columns="2" width="100%" columnClasses="stats-info-column, stats-graph-column">
            <h:panelGroup layout="block" styleClass="panel panel-default panel-stats">
              <table columns="2" class="table table-striped" width="250px">
                <caption>
                  <h:outputText escape="false" value="<div class=\" panel-heading\" style=\"padding-top:
                    1px;padding-bottom: 1px;border:0;\"><strong>
                      <h2>#{evaluationMessages.gs}</h2>
                    </strong>
        </div>" />
        </caption>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.sub_view}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.numResponses}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.tot_score_possible} " />
          </th>
          <td>
            <h:outputText value="#{histogramScores.roundedTotalPossibleScore}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.mean_eq}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.mean}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.median}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.median}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.mode}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.mode}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.range_eq}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.range}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.qtile_1_eq}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.q1}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.qtile_3_eq}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.q3}" />
          </td>
        </tr>
        <tr>
          <th>
            <h:outputText value="#{evaluationMessages.std_dev}" />
          </th>
          <td>
            <h:outputText value="#{histogramScores.standDev}" />
          </td>
        </tr>
        </table>
        </h:panelGroup>
        <h:panelGroup layout="block" styleClass="panel panel-default">
          <div class="panel-heading" style="padding-top: 1px;padding-bottom: 1px;">
            <strong>
              <h2>
                <h:outputText value="#{evaluationMessages.fsd}" />
              </h2>
            </strong>
          </div>
          <div aria-describedby="chartdiv_reader">
            <div id="chartdiv_reader" style="position: absolute;left: -999em;width: 1em;overflow: hidden;">
              <h:outputText escape="false" value="#{histogramScores.histogramChartReader}"></h:outputText>
            </div>
            <div id="chartdiv" style="height:283px;width:95%;margin: 20px auto; " aria-hidden="true"></div>
          </div>
        </h:panelGroup>
        </h:panelGrid>

        <h:panelGroup rendered="#{histogramScores.showObjectivesColumn=='true'}">
          <div class="objectives">
            <h:dataTable styleClass="table" value="#{histogramScores.objectives}" var="obj">
              <h:column>
                <f:facet name="header">
                  <h:outputText escape="false" value="#{evaluationMessages.obj}" />
                </f:facet>
                <h:outputText value="#{obj.key}" escape="false" />
              </h:column>
              <h:column>
                <f:facet name="header">
                  <h:outputText escape="false" value="#{evaluationMessages.objPercent}" />
                </f:facet>
                <h:outputText value="#{obj.value}%" escape="false" />
              </h:column>
            </h:dataTable>
          </div>
        </h:panelGroup>

        <h:panelGroup rendered="#{histogramScores.showObjectivesColumn=='true'}">
          <div class="keywords">
            <h:dataTable styleClass="table" value="#{histogramScores.keywords}" var="keyword_s">
              <h:column>
                <f:facet name="header">
                  <h:outputText escape="false" value="#{evaluationMessages.keywords}" />
                </f:facet>
                <h:outputText value="#{keyword_s.key}" escape="false" />
              </h:column>
              <h:column>
                <f:facet name="header">
                  <h:outputText escape="false" value="#{evaluationMessages.objPercent}" />
                </f:facet>
                <h:outputText value="#{keyword_s.value}%" escape="false" />
              </h:column>
            </h:dataTable>
          </div>
        </h:panelGroup>

        <div class="page-header">
          <h2>
            <h:outputText value="#{evaluationMessages.q_view}" />
          </h2>
        </div>

        <!-- The parts drop down. -->
        <h:panelGroup layout="block" styleClass="form-group" rendered="#{histogramScores.assesmentPartCount > 1}">
          <h:outputLabel value="#{evaluationMessages.part} #{evaluationMessages.column} ">
            <h:selectOneMenu id="partNumber" onchange="document.forms[0].submit();"
              value="#{histogramScores.partNumber}">
              <f:selectItem itemValue="" itemLabel="#{evaluationMessages.all_parts}" />
              <f:selectItems value="#{histogramScores.selectItemParts}" />
              <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
            </h:selectOneMenu>
          </h:outputLabel>
        </h:panelGroup>

        <h:dataTable styleClass="table table-striped presentation" value="#{histogramScores.partInfo}" var="item">
          <h:column>
            <h:panelGroup>
              <h3 class="part-title">
                <h:outputText value="#{item.title}" escape="false" />
              </h3>
              <div class="panel panel-default" />
              <div class="question-text panel-heading">
                <strong>
                  <h:outputText value="#{item.questionText}" escape="false" /></strong>
              </div>

              <h:dataTable columnClasses="stats-answers,stats-correctness"
                styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar"
                rendered="#{item.questionType == '1' or item.questionType == '2' or item.questionType == '12' or item.questionType == '4' or item.questionType == '15'}">
                <!-- MULTIPLE_CHOICE (1) -->
                <!-- MULTIPLE_CORRECT (2) -->
                <!-- TRUE_FALSE (4) -->
                <!-- MULTIPLE_CORRECT_SINGLE_SELECTION (12) -->
                <!-- CALCULATED_QUESTION (15) -->
                <h:column>
                  <f:facet name="header">
                    <h:panelGroup>
                      <h:outputText value="#{evaluationMessages.stats_ans_opt}" />
                    </h:panelGroup>
                  </f:facet>
                  <h:panelGroup styleClass="answer-bar-label" layout="block">
                    <h:outputText value="#{bar.label}" escape="false">
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.correctness}" />
                  </f:facet>
                  <h:outputText rendered="#{bar.isCorrect}" value="#{evaluationMessages.correct}" />
                  <h:outputText rendered="#{!bar.isCorrect}" value="#{evaluationMessages.not_correct}" />
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_num_responses}" />
                  </f:facet>
                  <h:panelGroup>
                    <span class="progress-num">
                      <h:outputText value="#{bar.numStudentsText}" />
                    </span>
                    <div class="progress-stat">
                      <h:outputText value="<div class=\" progress-bar #{ bar.isCorrect ? 'progress-bar-success' : '' }
                        test\" role=\"progressbar\" aria-valuenow=\"#{bar.columnHeight}\" aria-valuemin=\"0\"
                        aria-valuemax=\"100\" style=\"width: #{bar.columnHeight}%;\">"
                        escape="false" />
                        &nbsp;
                    </div>
                    <div class="num-students-text hide">
                      <h:outputText value=" #{bar.numStudentsText}" />
                    </div>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>

              <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar"
                value="#{item.histogramBars}" var="bar"
                rendered="#{item.questionType == '8'}">
                <!-- FILL_IN_BLANK (8) -->
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_ans_opt}" />
                  </f:facet>
                  <h:panelGroup styleClass="answer-bar-label" layout="block">
                    <h:outputText value="#{bar.label}" escape="true"> 
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_num_correct_responses}" />
                  </f:facet>
                  <h:panelGroup>
                    <span class="progress-num">
                      <h:outputText value="#{bar.numStudentsText}" />
                    </span>
                    <div class="progress-stat">
                      <h:outputText value="<div class=\" progress-bar progress-bar-success\" role=\"progressbar\"
                        aria-valuenow=\"#{bar.columnHeight}\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width:
                        #{bar.columnHeight}%;\">"
                        escape="false">
                      </h:outputText>
                      &nbsp;
                    </div>
                    <div class="num-students-text hide">
                      <h:outputText value=" #{bar.numStudentsText}" />
                    </div>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>

              <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar"
                value="#{item.histogramBars}" var="bar"
                rendered="#{item.questionType == '9' or item.questionType == '11' or item.questionType == '14' or item.questionType == '16'}">
                <!-- MATCHING (9) -->
                <!-- FILL_IN_NUMERIC (11) -->
                <!-- EXTENDED_MATCHING_ITEMS (14) -->
                <!-- IMAGEMAP_QUESTION (16) -->
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_ans_opt}" />
                  </f:facet>
                  <h:panelGroup styleClass="answer-bar-label" layout="block">
                    <h:outputText value="#{bar.label}" escape="false">
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_num_correct_responses}" />
                  </f:facet>
                  <h:panelGroup>
                    <span class="progress-num">
                      <h:outputText value="#{bar.numStudentsText}" />
                    </span>
                    <div class="progress-stat">
                      <h:outputText value="<div class=\" progress-bar progress-bar-success\" role=\"progressbar\"
                        aria-valuenow=\"#{bar.columnHeight}\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width:
                        #{bar.columnHeight}%;\">"
                        escape="false">
                      </h:outputText>
                      &nbsp;
                    </div>
                    <div class="num-students-text hide">
                      <h:outputText value=" #{bar.numStudentsText}" />
                    </div>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>

              <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar"
                value="#{item.histogramBars}" var="bar"
                rendered="#{item.questionType == '6' or item.questionType == '5' or item.questionType == '7'}">
                <!-- ESSAY_QUESTION (5) -->
                <!-- FILE_UPLOAD (6) -->
                <!-- AUDIO_RECORDING (7) -->
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_scores}" />
                  </f:facet>
                  <h:panelGroup styleClass="answer-bar-label" layout="block">
                    <h:outputText value="#{bar.label}" escape="false">
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_num_responses}" />
                  </f:facet>
                  <h:panelGroup>
                    <span class="progress-num">
                      <h:outputText value="#{bar.numStudentsText}" />
                    </span>
                    <div class="progress-stat">
                      <h:outputText value="<div class=\" progress-bar\" role=\"progressbar\"
                        aria-valuenow=\"#{bar.columnHeight}\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width:
                        #{bar.columnHeight}%;\">"
                        escape="false">
                      </h:outputText>
                      &nbsp;
                    </div>
                    <div class="num-students-text hide">
                      <h:outputText value=" #{bar.numStudentsText}" />
                    </div>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>

              <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar"
                value="#{item.histogramBars}" var="bar"
                rendered="#{item.questionType == '3'}">
                <!-- MULTIPLE_CHOICE_SURVEY (3) -->
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_ans_opt}" />
                  </f:facet>
                  <h:panelGroup styleClass="answer-bar-label" layout="block">
                    <h:outputText value="#{bar.label}" escape="false">
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_num_responses}" />
                  </f:facet>
                  <h:panelGroup>
                    <span class="progress-num">
                      <h:outputText value="#{bar.numStudentsText}" />
                    </span>
                    <div class="progress-stat">
                      <h:outputText value="<div class=\" progress-bar\" role=\"progressbar\"
                        aria-valuenow=\"#{bar.columnHeight}\" aria-valuemin=\"0\" aria-valuemax=\"100\" style=\"width:
                        #{bar.columnHeight}%;\">"
                        escape="false">
                      </h:outputText>
                      &nbsp;
                    </div>
                    <div class="num-students-text hide">
                      <h:outputText value=" #{bar.numStudentsText}" />
                    </div>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>

              <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar"
                value="#{item.histogramBars}" var="bar" rendered="#{item.questionType == '13'}">
                <!-- MATRIX_CHOICES_SURVEY (13) -->
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.stats_ans_opt}" />
                  </f:facet>
                  <h:panelGroup styleClass="answer-bar-label" layout="block">
                    <h:outputText value="#{bar.label}" escape="false">
                      <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter" />
                    </h:outputText>
                  </h:panelGroup>
                </h:column>
                <h:column>
                  <f:facet name="header">
                    <h:outputText value="#{evaluationMessages.ans_key}" />
                  </f:facet>
                  <t:dataTable style="width:100%;" styleClass="question-with-progress" value="#{bar.itemBars}"
                    var="itemBar">
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.match_match}" />
                      </f:facet>
                      <div>
                        <h:outputText value="#{itemBar.itemText}  " />
                      </div>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_num_responses}" />
                      </f:facet>
                      <span class="progress-num">
                        <h:outputText value="#{itemBar.numStudentsText}" />
                      </span>
                      <div class="progress-stat">
                        <h:outputText value="<div class=\" progress-bar\" role=\"progressbar\"
                          aria-valuenow=\"#{itemBar.columnHeight}\" aria-valuemin=\"0\" aria-valuemax=\"100\"
                          style=\"width: #{itemBar.columnHeight}%;\">" escape="false" />
                          &nbsp;
                      </div>
                    </h:column>
                  </t:dataTable>
                </h:column>
                <h:column>
                  <h:panelGroup>
                    <div class="table-responsive question-thirteen-holder">
                      <t:dataList layout="unorderedList" styleClass="question-with-progress" value="#{bar.itemBars}"
                        var="itemBar">
                      </t:dataList>
                    </div>
                  </h:panelGroup>
                </h:column>
              </h:dataTable>

              <!-- 1-2=mcmc 3=mcsc 4=tf 5=essay 6=file 7=audio 8=FIB 9=matching 14=emi -->

	      <h:panelGrid styleClass="table table-condensed table-striped" columns="5" rendered="#{item.questionType == '5' or item.questionType == '6' or item.questionType == '7'}">
                  <h:outputLabel value="#{evaluationMessages.responses}" />
                  <h:outputLabel value="#{evaluationMessages.tot_poss_eq}" />
                  <h:outputLabel value="#{evaluationMessages.mean_eq}" />
                  <h:outputLabel value="#{evaluationMessages.median}" />
                  <h:outputLabel value="#{evaluationMessages.mode}" />
		<h:outputText id="responses" value="#{item.numResponses}" />
		<h:outputText id="possible" value="#{item.totalScore}" />
		<h:outputText id="mean" value="#{item.mean}" />
		<h:outputText id="median" value="#{item.median}" />
		<h:outputText id="mode" value="#{item.mode}" />
              </h:panelGrid>

              <h:panelGrid styleClass="table table-striped" columns="2"
                rendered="#{item.questionType == '3' or item.questionType == '13'}">
                <h:outputText escape="false" id="responses1"
                  value="<strong>#{item.numResponses}</strong> #{evaluationMessages.responses}" />
              </h:panelGrid>
              <h:panelGrid styleClass="table table-striped" columns="2"
                rendered="#{item.questionType == '1' or  item.questionType == '2' or  item.questionType == '4' or  item.questionType == '8' or item.questionType == '9' or item.questionType == '11' or item.questionType == '12' or item.questionType == '14' or item.questionType == '15' or item.questionType == '16'}"
                columnClasses="alignLeft,aligntRight">
                <h:outputText rendered="#{item.numResponses != 0}" escape="false"
                  value="<strong>#{item.numResponses}</strong> #{evaluationMessages.responses}, <strong>#{item.percentCorrect}%</strong> #{evaluationMessages.percentCorrect}" />
                <h:outputText rendered="#{item.numResponses == 0}" escape="false"
                  value="<strong>No Responses</strong>" />
              </h:panelGrid>

            </h:panelGroup>
          </h:column>
        </h:dataTable>


        <h:commandButton value="#{evaluationMessages.return_s}" action="select" type="submit"
          rendered="#{histogramScores.hasNav=='false'}" />
      </h:form>
    </div>
    <!-- end content -->
  </body>

  </html>
</f:view>
