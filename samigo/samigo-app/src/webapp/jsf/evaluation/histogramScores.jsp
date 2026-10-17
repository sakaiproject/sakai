<?xml version="1.0" encoding="UTF-8"?>
<jsp:root version="3.0"
    xmlns:jsp="http://java.sun.com/JSP/Page"
    xmlns:h="http://java.sun.com/jsf/html"
    xmlns:f="http://java.sun.com/jsf/core"
    xmlns:t="http://myfaces.apache.org/tomahawk"
    xmlns:samigo="http://www.sakaiproject.org/samigo">
<jsp:directive.page pageEncoding="UTF-8" contentType="text/html;charset=utf-8" trimDirectiveWhitespaces="false" isELIgnored="false" deferredSyntaxAllowedAsLiteral="false"/>
<jsp:output omit-xml-declaration="yes"/>
  <jsp:directive.include file="/include/header.inc"/>
  <jsp:text><![CDATA[




<!DOCTYPE html
  PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

]]></jsp:text>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head>
        <jsp:expression> request.getAttribute("html.head") </jsp:expression>
        <title>
          <h:outputText value="#{evaluationMessages.title_stat}"/>
        </title>
        <link href="/samigo-app/css/print/print.css" type="text/css" rel="stylesheet" media="print"/>
      </head>
      <jsp:element name="body">
        <jsp:attribute name="onload" trim="false"><jsp:expression> request.getAttribute(" html.body.onload") </jsp:expression></jsp:attribute>
        <jsp:body>
          <!--
$Id$

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
            <jsp:text>
  $(document).ready(function(){
    // The current class is assigned using Javascript because we don't use facelets and the include directive does not support parameters.
    let currentLink = $('#histogram\\:histogramScoresMenuLink');
    currentLink.addClass('current');
    // Remove the link of the current option
    currentLink.html(currentLink.find('a').text());
  });
</jsp:text>
          </script>
          <!-- content...  -->
          <jsp:text><![CDATA[
    <div class="portletBody container-fluid">

      ]]></jsp:text>
          <!-- IF A SECURE DELIVERY MODULE HAS BEEN SELECTED, INJECT ITS HTML FRAGMENT (IF ANY) HERE  -->
          <h:outputText value="#{delivery.secureDeliveryHTMLFragment}" escape="false"/>
          <h:form id="histogram">
            <h:inputHidden id="publishedId" value="#{histogramScores.publishedId}"/>
            <h:inputHidden id="itemId" value="#{histogramScores.itemId}"/>
            <!-- HEADINGS  -->
            <jsp:directive.include file="/jsf/evaluation/evaluationHeadings.jsp"/>
            <h:panelGroup layout="block" styleClass="page-header">
              <h1>
                <h:outputText value="#{evaluationMessages.stat_view}#{evaluationMessages.column} " escape="false"/>
                <small>
                  <h:outputText value="#{histogramScores.assessmentName} " escape="false"/>
                </small>
              </h1>
            </h:panelGroup>
            <!-- EVALUATION SUBMENU  -->
            <jsp:directive.include file="/jsf/evaluation/evaluationSubmenu.jsp"/>
            <h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
            <h:panelGroup styleClass="b5 d-flex justify-content-between my-2" layout="block">
              <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX   -->
              <h:panelGroup styleClass="" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
                <label>
                  <h:outputText value="#{evaluationMessages.view} "/>
                  <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsL" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
                    <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}"/>
                    <f:selectItem itemValue="2" itemLabel="#{evaluationMessages.last_sub}"/>
                    <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                  </h:selectOneMenu>
                  <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsH" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
                    <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}"/>
                    <f:selectItem itemValue="1" itemLabel="#{evaluationMessages.highest_sub}"/>
                    <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                  </h:selectOneMenu>
                  <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsA" required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '4'}">
                    <f:selectItem itemValue="3" itemLabel="#{evaluationMessages.all_sub}"/>
                    <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                  </h:selectOneMenu>
                </label>
              </h:panelGroup>
              <h:panelGroup rendered="#{authorization.gradeAnyAssessment or authorization.gradeOwnAssessment}">
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
                      <h:outputLink value="#{histogramScores.exportStatisticsPdf}" styleClass="d-block" title="#{evaluationMessages.export_as_pdf}" target="_blank">
                        <h:outputText value="#{evaluationMessages.export_pdf}"/>
                      </h:outputLink>
                    </li>
                    <li>
                      <h:outputLink value="#{histogramScores.exportStatisticsXlsx}" styleClass="d-block" title="#{evaluationMessages.export_as_xlsx}" target="_blank">
                        <h:outputText value="#{evaluationMessages.export_xlsx}"/>
                      </h:outputLink>
                    </li>
                  </ul>
                </div>
              </h:panelGroup>
            </h:panelGroup>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jquery/1.12.4/jquery.min.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/jquery.jqplot.min.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.barRenderer.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.categoryAxisRenderer.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.highlighter.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.canvasTextRenderer.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.canvasAxisLabelRenderer.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <jsp:element name="script">
              <jsp:attribute name="type" trim="false">text/javascript</jsp:attribute>
              <jsp:attribute name="src" trim="false">/library/webjars/jqplot/1.0.9.d96a669/plugins/jqplot.pointLabels.js</jsp:attribute>
              <jsp:body>
              </jsp:body>
            </jsp:element>
            <link href="/library/webjars/jqplot/1.0.9.d96a669/jquery.jqplot.css" type="text/css" rel="stylesheet"/>
            <h:outputText escape="false" value="&#10;&#9;&lt;script&gt;&#10;&#9; window.onload=function(){&#10;     var dataSet=#{histogramScores.histogramChartOptions};&#10;     $('#chartdiv').height(($('.panel-stats').height() - 89)+'px');&#10;     let cssStyles = getComputedStyle(document.firstElementChild);&#10;     var plot1=$.jqplot('chartdiv',dataSet,{&#10;&#9;&#9;&#9;seriesDefaults:{&#10;&#9;&#9;&#9;  shadow:false,&#10;&#9;&#9;&#9;  pointLabels:{&#10;&#9;&#9;&#9;&#9;  show:true,&#10;&#9;&#9;&#9;&#9;  formatString: '%d'&#10;&#9;&#9;&#9;  },&#10;&#9;&#9;&#9;  renderer:$.jqplot.BarRenderer&#10;&#9;&#9;&#9;},&#10;&#9;&#9;&#9;axes:{&#10;&#9;&#9;&#9;  yaxis:{&#10;&#9;&#9;&#9;    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,&#10;&#9;&#9;&#9;    label:'#{evaluationMessages.num_students}',&#10;&#9;                    labelOptions:{&#10;                              fontSize: '14pt',&#10;                              textColor: cssStyles.getPropertyValue('--sakai-text-color-1')&#10;                            }&#10;&#9;&#9;&#9;  },&#10;&#9;&#9;&#9;  xaxis:{&#10;&#9;&#9;&#9;    labelRenderer: $.jqplot.CanvasAxisLabelRenderer,&#10;&#9;&#9;&#9;    label:'#{evaluationMessages.num_points}',&#10;&#9;&#9;&#9;    renderer:$.jqplot.CategoryAxisRenderer,&#10;&#9;                    labelOptions:{&#10;                              fontSize: '14pt',&#10;                              textColor: cssStyles.getPropertyValue('--sakai-text-color-1')&#10;                            }&#10;&#9;&#9;&#9;  }&#10;&#9;&#9;&#9;},&#10;      grid: {&#10;        gridLineColor: cssStyles.getPropertyValue('--sakai-border-color'),&#10;        background: cssStyles.getPropertyValue('--sakai-background-color-1')&#10;      }&#10;&#9;&#9;});&#10;    console.log(plot1);&#10;          $('.presentation').attr('role','presentation');&#10;&#9;        $(window).resize(function(){plot1.replot(dataSet);});&#10;&#9;}&lt;/script&gt;"/>
            <h:panelGrid columns="2" width="100%" columnClasses="stats-info-column, stats-graph-column">
              <h:panelGroup layout="block" styleClass="panel panel-default panel-stats">
                <table columns="2" class="table table-striped" width="250px">
                  <caption>
                    <h:outputText escape="false" value="&lt;div class=&quot; panel-heading&quot; style=&quot;padding-top:&#10;                    1px;padding-bottom: 1px;border:0;&quot;&gt;&lt;strong&gt;&#10;                      &lt;h2&gt;#{evaluationMessages.gs}&lt;/h2&gt;&#10;                    &lt;/strong&gt;&#10;        &lt;/div&gt;"/>
                  </caption>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.sub_view}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.numResponses}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.tot_score_possible} "/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.roundedTotalPossibleScore}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.mean_eq}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.mean}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.median}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.median}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.mode}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.mode}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.range_eq}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.range}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.qtile_1_eq}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.q1}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.qtile_3_eq}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.q3}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.std_dev}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.standDev}"/>
                    </td>
                  </tr>
                  <tr>
                    <th>
                      <h:outputText value="#{evaluationMessages.skew_coef}"/>
                    </th>
                    <td>
                      <h:outputText value="#{histogramScores.skewnessCoefficient}"/>
                    </td>
                  </tr>
                  <h:panelGroup rendered="#{histogramScores.trackingQuestion}">
                    <tr>
                      <th>
                        <h:outputText value="#{evaluationMessages.time_min}:"/>
                      </th>
                      <td>
                        <h:outputText value="#{histogramScores.timeStats[0]}"/>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <h:outputText value="#{evaluationMessages.time_avg}:"/>
                      </th>
                      <td>
                        <h:outputText value="#{histogramScores.timeStats[1]}"/>
                      </td>
                    </tr>
                    <tr>
                      <th>
                        <h:outputText value="#{evaluationMessages.time_max}:"/>
                      </th>
                      <td>
                        <h:outputText value="#{histogramScores.timeStats[2]}"/>
                      </td>
                    </tr>
                  </h:panelGroup>
                </table>
              </h:panelGroup>
              <h:panelGroup layout="block" styleClass="panel panel-default">
                <div class="card text-center" style="padding-top: 1px;padding-bottom: 1px;">
                  <strong>
                    <h2>
                      <h:outputText value="#{evaluationMessages.fsd}"/>
                    </h2>
                  </strong>
                </div>
                <div aria-describedby="chartdiv_reader">
                  <div id="chartdiv_reader" style="position: absolute;left: -999em;width: 1em;overflow: hidden;">
                    <h:outputText escape="false" value="#{histogramScores.histogramChartReader}"/>
                  </div>
                  <jsp:element name="div">
                    <jsp:attribute name="id" trim="false">chartdiv</jsp:attribute>
                    <jsp:attribute name="style" trim="false">height:283px;width:95%;margin: 20px auto; </jsp:attribute>
                    <jsp:attribute name="aria-hidden" trim="false">true</jsp:attribute>
                    <jsp:body>
                    </jsp:body>
                  </jsp:element>
                </div>
              </h:panelGroup>
            </h:panelGrid>
            <h:panelGroup layout="block" styleClass="page-header" rendered="#{histogramScores.trackingQuestion &amp;&amp; histogramScores.timeStatsVariationArray.size() &gt; 0 &amp;&amp; histogramScores.timeStatsVariationArray.get(0).get(0) &gt; 0}">
              <h2>
                <h:outputText value="#{evaluationMessages.timeStatsVariation_title}"/>
              </h2>
            </h:panelGroup>
            <h:dataTable styleClass="table table-bordered table-striped presentation" value="#{histogramScores.timeStatsVariationArray}" var="statsVariations" rendered="#{histogramScores.trackingQuestion &amp;&amp; histogramScores.timeStatsVariationArray.size() &gt; 0 &amp;&amp; histogramScores.timeStatsVariationArray.get(0).get(0) &gt; 0}">
              <h:column>
                <h:panelGroup>
                  <h:dataTable styleClass="table panel-body stats-tracking" value="#{statsVariations[2]}" var="statVariation">
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{statsVariations[1]}"/>
                      </f:facet>
                      <h:panelGroup>
                        <h:panelGroup styleClass="boldText stats-track-answers">
                          <h:outputText value="#{statVariation[0]}"/>
                        </h:panelGroup>
                        <h:panelGroup styleClass="elapsed-quantity-col">
                          <h:outputText value="#{statVariation[1]}"/>
                        </h:panelGroup>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                </h:panelGroup>
              </h:column>
            </h:dataTable>
            <h:panelGroup rendered="#{histogramScores.showObjectivesColumn=='true'}">
              <div class="objectives">
                <h:dataTable styleClass="table" value="#{histogramScores.objectives}" var="obj" columnClasses="w-75,w-25 text-end text-nowrap">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.obj}"/>
                    </f:facet>
                    <h:outputText value="#{obj.key}" escape="false"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.objPercent}"/>
                    </f:facet>
                    <h:outputText value="#{obj.value}">
                      <f:convertNumber maxFractionDigits="2" minFractionDigits="0"/>
                    </h:outputText>
                    <h:outputText value="%"/>
                  </h:column>
                </h:dataTable>
              </div>
            </h:panelGroup>
            <h:panelGroup rendered="#{histogramScores.showObjectivesColumn=='true'}">
              <div class="keywords">
                <h:dataTable styleClass="table" value="#{histogramScores.keywords}" var="keyword_s" columnClasses="w-75,w-25 text-end text-nowrap">
                  <h:column>
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.keywords}"/>
                    </f:facet>
                    <h:outputText value="#{keyword_s.key}" escape="false"/>
                  </h:column>
                  <h:column>
                    <f:facet name="header">
                      <h:outputText escape="false" value="#{evaluationMessages.objPercent}"/>
                    </f:facet>
                    <h:outputText value="#{keyword_s.value}">
                      <f:convertNumber maxFractionDigits="2" minFractionDigits="0"/>
                    </h:outputText>
                    <h:outputText value="%"/>
                  </h:column>
                </h:dataTable>
              </div>
            </h:panelGroup>
            <div class="page-header">
              <h2>
                <h:outputText value="#{evaluationMessages.q_view}"/>
              </h2>
            </div>
            <!-- The parts drop down.  -->
            <h:panelGroup layout="block" styleClass="form-group" rendered="#{histogramScores.assesmentPartCount &gt; 1}">
              <h:outputLabel value="#{evaluationMessages.part} #{evaluationMessages.column} ">
                <h:selectOneMenu id="partNumber" onchange="document.forms[0].submit();" value="#{histogramScores.partNumber}">
                  <f:selectItem itemValue="" itemLabel="#{evaluationMessages.all_parts}"/>
                  <f:selectItems value="#{histogramScores.selectItemParts}"/>
                  <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener"/>
                </h:selectOneMenu>
              </h:outputLabel>
            </h:panelGroup>
            <h:dataTable styleClass="table table-bordered table-striped presentation" value="#{histogramScores.partInfo}" var="item">
              <h:column>
                <h:panelGroup>
                  <h3 class="part-title">
                    <h:outputText value="#{item.title}" escape="false"/>
                  </h3>
                  <jsp:text><![CDATA[
              <div class="card" />
              ]]></jsp:text>
                  <div class="question-text panel-heading">
                    <strong>
                      <h:outputText value="#{item.questionText}" escape="false"/>
                    </strong>
                  </div>
                  <h:dataTable columnClasses="stats-answers,stats-correctness" styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar" rendered="#{item.displaysAnswerStatsWithCorrectnessColumn}">
                    <!-- MULTIPLE_CHOICE (1)  -->
                    <!-- MULTIPLE_CORRECT (2)  -->
                    <!-- TRUE_FALSE (4)  -->
                    <!-- MULTIPLE_CORRECT_SINGLE_SELECTION (12)  -->
                    <!-- CALCULATED_QUESTION (15)  -->
                    <h:column>
                      <f:facet name="header">
                        <h:panelGroup>
                          <h:outputText value="#{evaluationMessages.stats_ans_opt}"/>
                        </h:panelGroup>
                      </f:facet>
                      <h:panelGroup styleClass="answer-bar-label" layout="block">
                        <h:outputText value="#{bar.label}" escape="false">
                          <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter"/>
                        </h:outputText>
                      </h:panelGroup>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.correctness}"/>
                      </f:facet>
                      <h:outputText rendered="#{bar.isCorrect}" value="#{evaluationMessages.correct}"/>
                      <h:outputText rendered="#{!bar.isCorrect}" value="#{evaluationMessages.not_correct}"/>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_num_responses}"/>
                      </f:facet>
                      <h:panelGroup>
                        <span class="progress-num">
                          <h:outputText value="#{bar.numStudentsText}"/>
                        </span>
                        <div class="progress-stat">
                          <h:outputText value="&lt;div class=&quot; progress-bar #{ bar.isCorrect ? 'bg-success' : 'bg-danger' } &quot;&#10;                        style=&quot;width: #{bar.columnHeight}%;&quot;&gt;" escape="false"/>
                          <jsp:text><![CDATA[
                        &nbsp;
                    ]]></jsp:text>
                        </div>
                        <div class="num-students-text hide">
                          <h:outputText value=" #{bar.numStudentsText}"/>
                        </div>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar" rendered="#{item.displaysFillInBlankStats}">
                    <!-- FILL_IN_BLANK (8)  -->
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_ans_opt}"/>
                      </f:facet>
                      <h:panelGroup styleClass="answer-bar-label" layout="block">
                        <h:outputText value="#{bar.label}" escape="true">
                          <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter"/>
                        </h:outputText>
                      </h:panelGroup>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_num_correct_responses}"/>
                      </f:facet>
                      <h:panelGroup>
                        <span class="progress-num">
                          <h:outputText value="#{bar.numStudentsText}"/>
                        </span>
                        <div class="progress-stat">
                          <h:outputText value="&lt;div class=&quot; progress-bar #{ bar.isCorrect ? 'bg-success' : 'bg-danger' } &quot;&#10;                        style=&quot;width:#{bar.columnHeight}%;&quot;&gt;" escape="false">
                          </h:outputText>
                          <jsp:text><![CDATA[
                      &nbsp;
                    ]]></jsp:text>
                        </div>
                        <div class="num-students-text hide">
                          <h:outputText value=" #{bar.numStudentsText}"/>
                        </div>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar" rendered="#{item.displaysAnswerStatsWithoutCorrectnessColumn}">
                    <!-- MATCHING (9)  -->
                    <!-- FILL_IN_NUMERIC (11)  -->
                    <!-- EXTENDED_MATCHING_ITEMS (14)  -->
                    <!-- IMAGEMAP_QUESTION (16)  -->
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_ans_opt}"/>
                      </f:facet>
                      <h:panelGroup styleClass="answer-bar-label" layout="block">
                        <h:outputText value="#{bar.label}" escape="false">
                          <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter"/>
                        </h:outputText>
                      </h:panelGroup>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_num_correct_responses}"/>
                      </f:facet>
                      <h:panelGroup>
                        <span class="progress-num">
                          <h:outputText value="#{bar.numStudentsText}"/>
                        </span>
                        <div class="progress-stat">
                          <h:outputText value="&lt;div class=&quot; progress-bar #{ bar.isCorrect ? 'bg-success' : 'bg-danger' } &quot;&#10;                        style=&quot;width:#{bar.columnHeight}%;&quot;&gt;" escape="false">
                          </h:outputText>
                          <jsp:text><![CDATA[
                      &nbsp;
                    ]]></jsp:text>
                        </div>
                        <div class="num-students-text hide">
                          <h:outputText value=" #{bar.numStudentsText}"/>
                        </div>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar" rendered="#{item.displaysScoreStats}">
                    <!-- ESSAY_QUESTION (5)  -->
                    <!-- FILE_UPLOAD (6)  -->
                    <!-- AUDIO_RECORDING (7)  -->
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_scores}"/>
                      </f:facet>
                      <h:panelGroup styleClass="answer-bar-label" layout="block">
                        <h:outputText value="#{bar.label}" escape="false">
                          <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter"/>
                        </h:outputText>
                      </h:panelGroup>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_num_responses}"/>
                      </f:facet>
                      <h:panelGroup>
                        <span class="progress-num">
                          <h:outputText value="#{bar.numStudentsText}"/>
                        </span>
                        <div class="progress-stat">
                          <h:outputText value="&lt;div class=&quot; progress-bar&quot;&#10;                        style=&quot;width:#{bar.columnHeight}%;&quot;&gt;" escape="false">
                          </h:outputText>
                          <jsp:text><![CDATA[
                      &nbsp;
                    ]]></jsp:text>
                        </div>
                        <div class="num-students-text hide">
                          <h:outputText value=" #{bar.numStudentsText}"/>
                        </div>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar" rendered="#{item.displaysMultipleChoiceSurveyStats}">
                    <!-- MULTIPLE_CHOICE_SURVEY (3)  -->
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_ans_opt}"/>
                      </f:facet>
                      <h:panelGroup styleClass="answer-bar-label" layout="block">
                        <h:outputText value="#{bar.label}" escape="false">
                          <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter"/>
                        </h:outputText>
                      </h:panelGroup>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_num_responses}"/>
                      </f:facet>
                      <h:panelGroup>
                        <span class="progress-num">
                          <h:outputText value="#{bar.numStudentsText}"/>
                        </span>
                        <div class="progress-stat">
                          <h:outputText value="&lt;div class=&quot; progress-bar&quot;&#10;                        style=&quot;width:#{bar.columnHeight}%;&quot;&gt;" escape="false">
                          </h:outputText>
                          <jsp:text><![CDATA[
                      &nbsp;
                    ]]></jsp:text>
                        </div>
                        <div class="num-students-text hide">
                          <h:outputText value=" #{bar.numStudentsText}"/>
                        </div>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <h:dataTable columnClasses="stats-answers" styleClass="table panel-body stats-bod stats-bar" value="#{item.histogramBars}" var="bar" rendered="#{item.displaysMatrixSurveyStats}">
                    <!-- MATRIX_CHOICES_SURVEY (13)  -->
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.stats_ans_opt}"/>
                      </f:facet>
                      <h:panelGroup styleClass="answer-bar-label" layout="block">
                        <h:outputText value="#{bar.label}" escape="false">
                          <f:converter converterId="org.sakaiproject.tool.assessment.jsf.convert.AnswerSurveyConverter"/>
                        </h:outputText>
                      </h:panelGroup>
                    </h:column>
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.ans_key}"/>
                      </f:facet>
                      <t:dataTable style="width:100%;" styleClass="question-with-progress" value="#{bar.itemBars}" var="itemBar">
                        <h:column>
                          <f:facet name="header">
                            <h:outputText value="#{evaluationMessages.match_match}"/>
                          </f:facet>
                          <div>
                            <h:outputText value="#{itemBar.itemText}  "/>
                          </div>
                        </h:column>
                        <h:column>
                          <f:facet name="header">
                            <h:outputText value="#{evaluationMessages.stats_num_responses}"/>
                          </f:facet>
                          <span class="progress-num">
                            <h:outputText value="#{itemBar.numStudentsText}"/>
                          </span>
                          <div class="progress-stat">
                            <h:outputText value="&lt;div class=&quot; progress-bar&quot;&#10;                          style=&quot;width: #{itemBar.columnHeight}%;&quot;&gt;" escape="false"/>
                            <jsp:text><![CDATA[
                          &nbsp;
                      ]]></jsp:text>
                          </div>
                        </h:column>
                      </t:dataTable>
                    </h:column>
                    <h:column>
                      <h:panelGroup>
                        <div class="table question-thirteen-holder">
                          <t:dataList layout="unorderedList" styleClass="question-with-progress" value="#{bar.itemBars}" var="itemBar">
                          </t:dataList>
                        </div>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <h:dataTable styleClass="table panel-body stats-tracking" rendered="#{histogramScores.trackingQuestion}" value="#{item.timeStatsArray}" var="timeString">
                    <h:column>
                      <f:facet name="header">
                        <h:outputText value="#{evaluationMessages.timeStats_title}"/>
                      </f:facet>
                      <h:panelGroup>
                        <h:panelGroup styleClass="boldText stats-track-answers">
                          <h:outputText value="#{timeString[0]}"/>
                        </h:panelGroup>
                        <h:panelGroup styleClass="elapsed-quantity-col">
                          <h:outputText value=" #{timeString[1]}"/>
                        </h:panelGroup>
                      </h:panelGroup>
                    </h:column>
                  </h:dataTable>
                  <!-- 1-2=mcmc 3=mcsc 4=tf 5=essay 6=file 7=audio 8=FIB 9=matching 14=emi  -->
                  <h:panelGrid styleClass="table table-condensed table-striped" columns="5" rendered="#{item.displaysScoreStats}">
                    <h:outputLabel value="#{evaluationMessages.responses}"/>
                    <h:outputLabel value="#{evaluationMessages.tot_poss_eq}"/>
                    <h:outputLabel value="#{evaluationMessages.mean_eq}"/>
                    <h:outputLabel value="#{evaluationMessages.median}"/>
                    <h:outputLabel value="#{evaluationMessages.mode}"/>
                    <h:outputText id="responses" value="#{item.numResponses}"/>
                    <h:outputText id="possible" value="#{item.totalScore}"/>
                    <h:outputText id="mean" value="#{item.mean}"/>
                    <h:outputText id="median" value="#{item.median}"/>
                    <h:outputText id="mode" value="#{item.mode}"/>
                  </h:panelGrid>
                  <h:panelGrid styleClass="table table-striped" columns="2" rendered="#{item.displaysSurveySummary}">
                    <h:outputText escape="false" id="responses1" value="&lt;strong&gt;#{item.numResponses}&lt;/strong&gt; #{evaluationMessages.responses}"/>
                  </h:panelGrid>
                  <h:panelGrid styleClass="table table-striped" columns="2" rendered="#{item.displaysAnswerStatsSummary}" columnClasses="alignLeft,aligntRight">
                    <h:outputText rendered="#{item.numResponses != 0}" escape="false" value="&lt;strong&gt;#{item.numResponses}&lt;/strong&gt; #{evaluationMessages.responses}, &lt;strong&gt;#{item.percentCorrect}%&lt;/strong&gt; #{evaluationMessages.percentCorrect}"/>
                    <h:outputText rendered="#{item.numResponses == 0}" escape="false" value="&lt;strong&gt;No Responses&lt;/strong&gt;"/>
                  </h:panelGrid>
                </h:panelGroup>
              </h:column>
            </h:dataTable>
            <h:commandButton value="#{evaluationMessages.return_s}" action="select" type="submit" rendered="#{histogramScores.hasNav=='false'}"/>
          </h:form>
          <jsp:text><![CDATA[
    </div>
    ]]></jsp:text>
          <!-- end content  -->
        </jsp:body>
      </jsp:element>
    </html>
  </f:view>
</jsp:root>
