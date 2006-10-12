<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

  <f:view>
   
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.EvaluationMessages"
     var="msg"/>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{msg.title_stat}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!--
$Id$
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
<!-- content... -->
 <div class="portletBody">
<h:form id="histogram">

  <h:inputHidden id="publishedId" value="#{histogramScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{histogramScores.itemId}" />

  <!-- HEADINGS -->
  <%@ include file="/jsf/evaluation/evaluationHeadings.jsp" %>

  <h3>
    <h:outputText value="#{msg.stat_view}"/>
    <h:outputText value="#{msg.column} "/>
    <h:outputText value="#{histogramScores.assessmentName} "/>
  </h3>
<h:outputText value=" <p class=\"navViewAction\">" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" escape="false"/>
 
    <h:commandLink title="#{msg.t_totalScores}" action="totalScores" immediate="true" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.ResetTotalScoreListener" />
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{msg.title_total}" />
    </h:commandLink>
    <h:outputText value=" #{msg.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>
    <h:commandLink title="#{msg.t_questionScores}" action="questionScores" immediate="true" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
      <h:outputText value="#{msg.q_view}" />
    </h:commandLink>
    <h:outputText value=" #{msg.separator} " rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>
      <h:outputText value="#{msg.stat_view}" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}"/>
 <h:outputText value=" </p>" rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}" escape="false"/>

  <h:messages styleClass="validation" />

<div class="tier1">


  <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX  -->
    <h:panelGroup rendered="#{histogramScores.hasNav==null || histogramScores.hasNav=='true'}">
     <h:outputText value="#{msg.view} " />
      <h:outputText value="#{msg.column} " />

     <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsL"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '2'}">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="2" itemLabel="#{msg.last_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
     </h:selectOneMenu>

     <h:selectOneMenu value="#{histogramScores.allSubmissions}" id="allSubmissionsH"
        required="true" onchange="document.forms[0].submit();" rendered="#{totalScores.scoringOption eq '1'}">
      <f:selectItem itemValue="3" itemLabel="#{msg.all_sub}" />
      <f:selectItem itemValue="1" itemLabel="#{msg.highest_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
     </h:selectOneMenu>
    </h:panelGroup>

    <h:panelGroup rendered="#{histogramScores.randomType =='true'}">
    <h:outputText value="#{msg.no_histogram_for_random}" />
      </h:panelGroup>


    <h:panelGroup rendered="#{histogramScores.randomType =='false'}">
 <f:verbatim><h4></f:verbatim>
  <h:outputText value="#{msg.tot}" />
   <f:verbatim></h4><div class="tier2"></f:verbatim>

 <h:dataTable value="#{histogramScores.histogramBars}" var="bar" headerClass="navView">

    <h:column>
        <f:facet name="header">
        <h:outputText escape="false" value="<U>#{msg.num_points}</U>" /> 
      </f:facet>
        <h:outputText value=" #{bar.rangeInfo}" />
    </h:column>
     <h:column>
<f:facet name="header">
         <h:outputText escape="false" value="<U>#{msg.num_students}</U>" />
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

<h:outputLabel value="#{msg.sub_view}"/>
<h:outputLabel value="#{histogramScores.numResponses}" />

<h:outputLabel value="#{msg.tot} Possible" />
<h:outputText value="#{histogramScores.roundedTotalPossibleScore}"/>

<h:outputLabel value="#{msg.mean_eq}" />
<h:outputText value="#{histogramScores.mean}"/>

<h:outputLabel value="#{msg.median}" />
<h:outputText value="#{histogramScores.median}"/>

<h:outputLabel value="#{msg.mode}" />
<h:outputText value="#{histogramScores.mode}"/>

<h:outputLabel value="#{msg.range_eq}" />
<h:outputText value="#{histogramScores.range}"/>

<h:outputLabel value="#{msg.qtile_1_eq}" />
<h:outputText value="#{histogramScores.q1}"/>

<h:outputLabel value="#{msg.qtile_3_eq}" />
<h:outputText value="#{histogramScores.q3}"/>

<h:outputLabel value="#{msg.std_dev}" />
<h:outputText value="#{histogramScores.standDev}"/>
</h:panelGrid>
</p>

  <h:dataTable value="#{histogramScores.info}" var="item">

<!-- need to add a randomtype property for histogramQuestionScoreBean (item) and if it's true, hide histogram  -->
<%--
    <h:column rendered="#{histogramScores.randomType =='true'}">
      <h:outputText value="#{msg.no_histogram_for_random}" />
    </h:column>
--%>


    <h:column rendered="#{histogramScores.randomType =='false'}">
      <h:panelGroup>
        <f:verbatim><h4></f:verbatim>
          <h:outputText value="#{item.title}" escape="false" />
        <f:verbatim></h4></f:verbatim>
<f:verbatim><div class="tier2"/></f:verbatim>
        <h:outputText value="#{item.questionText}" escape="false" />

        <h:dataTable value="#{item.histogramBars}" var="bar">
          <h:column>
            <h:panelGrid columns="1">
              <h:panelGroup>

<h:graphicImage id="image8" rendered="#{bar.isCorrect}" width="12" height="12"
        alt="#{msg.correct}" url="/images/delivery/checkmark.gif" >
       </h:graphicImage>

<h:graphicImage id="image9" rendered="#{!bar.isCorrect}" width="12" height="12"
        alt="#{msg.not_correct}" url="/images/delivery/spacer.gif" >
       </h:graphicImage>

                <h:graphicImage url="/images/reddot.gif" height="12" width="#{bar.columnHeight}"/>
                <h:outputText value=" #{bar.numStudentsText}" />
              </h:panelGroup>
               <h:panelGroup>
               <h:graphicImage width="12" height="12" url="/images/delivery/spacer.gif" />
              <h:outputText value="#{bar.label}" escape="false" />
</h:panelGroup>
            </h:panelGrid>
          </h:column>
        </h:dataTable>

        <!-- 1-2=mcmc 3=mcsc 4=tf 5=essay 6=file 7=audio 8=FIB 9=matching -->

        <h:panelGrid columns="2" rendered="#{item.questionType == '5' or item.questionType == '6' or item.questionType == '7'}">

          <h:outputLabel value="#{msg.responses}" />
          <h:outputText id="responses" value="#{item.numResponses}" />
          <h:outputLabel value="#{msg.tot_poss_eq}" />
          <h:outputText id="possible" value="#{item.totalScore}" />
          <h:outputLabel value="#{msg.mean_eq}" />
          <h:outputText id="mean" value="#{item.mean}" />
          <h:outputLabel  value="#{msg.median}" />
          <h:outputText id="median" value="#{item.median}" />
          <h:outputLabel value="#{msg.mode}" />
          <h:outputText id="mode" value="#{item.mode}" />
        </h:panelGrid>
       <h:panelGrid columns="2" rendered="#{item.questionType == '3'}">
          <h:outputLabel for="responses1" value="#{msg.responses}" />
          <h:outputText id="responses1" value="#{item.numResponses}" />
        </h:panelGrid>
         <h:panelGrid columns="2" rendered="#{item.questionType == '1' or  item.questionType == '2' or  item.questionType == '4' or  item.questionType == '8' or item.questionType == '9' or item.questionType == '11'}" columnClasses="alignLeft,aligntRight">
             <h:outputLabel for="responses2" value="#{msg.responses}" />
          <h:outputText id="responses2" value="#{item.numResponses}" />
          <h:outputLabel for="percentCorrect" value="#{msg.percentCorrect}" />
          <h:outputText id="percentCorrect" value="#{item.percentCorrect}" />
        </h:panelGrid>


      </h:panelGroup>
<f:verbatim></div></div></f:verbatim> 
    </h:column>
  </h:dataTable>


<h:commandButton accesskey="#{msg.a_return}"value="#{msg.return}" action="select" type="submit" rendered="#{histogramScores.hasNav=='false'}"/>
</div>
</h:form>
</div>
  <!-- end content -->
      </body>
    </html>
  </f:view>
