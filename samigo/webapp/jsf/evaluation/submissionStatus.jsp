<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.EvaluationMessages"
     var="msg"/>
   <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText
        value="#{msg.title_total}" /></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- $Id:  -->
<!-- content... -->
<h:form id="editTotalResults">
  <h:inputHidden id="publishedId" value="#{totalScores.publishedId}" />
  <h:inputHidden id="itemId" value="#{totalScores.firstItem}" />

  <!-- HEADINGS -->
  <p class="navIntraTool">
    <h:commandLink action="author" immediate="true">
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
      <h:outputText value="#{msg.global_nav_assessmt}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.global_nav_template}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.global_nav_pools}" />
    </h:commandLink>
  </p>
  <h3>
    <h:outputText value="#{msg.title_total}"/>
    <h:outputText value=": "/>
    <h:outputText value="#{totalScores.assessmentName} "/>
  </h3>
  <p class="navModeAction">
    <h:outputText value="#{msg.sub_status}" />
    <h:outputText value=" | " />
    <h:commandLink action="totalScores" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
      <h:outputText value="#{msg.title_total}" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{totalScores.firstItem ne ''}" />
    <h:commandLink action="questionScores" immediate="true" 
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{msg.q_view}" />
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.QuestionScoreListener" />
    </h:commandLink>
    <h:outputText value=" | " rendered="#{totalScores.firstItem ne ''}" />
    <h:commandLink action="histogramScores" immediate="true"
      rendered="#{totalScores.firstItem ne ''}" >
      <h:outputText value="#{msg.stat_view}" />
      <f:param name="hasNav" value="true"/>
      <f:actionListener
        type="org.sakaiproject.tool.assessment.ui.listener.evaluation.HistogramListener" />
    </h:commandLink>
  </p>

  <h:messages layout="table" style="color:red"/>

  <!-- LAST/ALL SUBMISSIONS; PAGER; ALPHA INDEX  -->
  <span class="indnt1">
     <h:outputText value="#{msg.max_score_poss}" style="instruction"/>
     <h:outputText value="#{totalScores.maxScore}" style="instruction"/></span>
     <f:verbatim><br /></f:verbatim>
<span class="indnt1">
     <h:outputText value="#{msg.view}" />
     <h:outputText value=": " /></span>
     <h:selectOneMenu value="#{totalScores.allSubmissions}" id="allSubmissions"
        required="true" onchange="document.forms[0].submit();" >
      <f:selectItem itemValue="false" itemLabel="#{msg.last_sub}" />
      <f:selectItem itemValue="true" itemLabel="#{msg.all_sub}" />
      <f:valueChangeListener
         type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
     </h:selectOneMenu>
  </span>

<%--  THIS MIGHT BE FOR NEXT RELEASE

  <span class="rightNav">
    <!-- Need to hook up to the back end, DUMMIED -->
    <samigo:pagerButtons  formId="editTotalResults" dataTableId="myData"
      firstItem="1" lastItem="10" totalItems="50"
      prevText="Previous" nextText="Next" numItems="10" />
    </span>
END OF TEMPORARY OUT FOR THIS RELEASE --%>
    <span class="abc">
      <samigo:alphaIndex initials="#{totalScores.agentInitials}" />

    </span>


  <!-- STUDENT RESPONSES AND GRADING -->
  <!-- note that we will have to hook up with the back end to get N at a time -->
  <h:dataTable id="totalScoreTable" value="#{totalScores.agents}"
    var="description" style="listHier indnt2" columnClasses="textTable">
    <!-- NAME/SUBMISSION ID -->

    <h:column>
     <f:facet name="header">
        <h:commandLink immediate="true" id="lastName" action="totalScores">
          <h:outputText value="#{msg.name}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="lastName" />
        </h:commandLink>
     </f:facet>
     <h:panelGroup> <span class="indnt2">
       <h:outputText value="<a name=\"" escape="false" />
       <h:outputText value="#{description.lastInitial}" />
       <h:outputText value="\"></a>" escape="false" />  
       <h:outputText value="#{description.firstName}" />
       <h:outputText value=" " />
       <h:outputText value="#{description.lastName}" />
     </span>
     </h:panelGroup>
    </h:column>

   <!-- STUDENT ID -->
    <h:column>
     <f:facet name="header">
       <h:commandLink id="idString" action="totalScores" >
          <h:outputText value="#{msg.uid}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="idString" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.idString}" />
    </h:column>

    <h:column rendered="#{totalScores.anonymous eq 'false' && totalScores.sortType eq 'idString'}" >
     <f:facet name="header">
       <h:outputText value="#{msg.uid}" />
     </f:facet>
        <h:outputText value="#{description.idString}" />
    </h:column>

    <!-- ROLE -->
    <h:column>
     <f:facet name="header" >
        <h:commandLink id="role" action="totalScores">
          <h:outputText value="#{msg.role}" />
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="role" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <h:column rendered="#{totalScores.sortType eq 'role'}">
     <f:facet name="header" >
       <h:outputText value="#{msg.role}" />
     </f:facet>
        <h:outputText value="#{description.role}"/>
    </h:column>

    <!-- DATE -->
    <h:column>
     <f:facet name="header">
        <h:commandLink id="submittedDate" action="totalScores">
          <h:outputText value="#{msg.date}" />
        <f:actionListener
          type="org.sakaiproject.tool.assessment.ui.listener.evaluation.TotalScoreListener" />
        <f:param name="sortBy" value="submittedDate" />
        </h:commandLink>
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
          <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
    </h:column>

    <h:column rendered="#{totalScores.sortType=='submittedDate'}">
     <f:facet name="header">
       <h:outputText value="#{msg.date}" />
     </f:facet>
        <h:outputText value="#{description.submittedDate}">
           <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
        </h:outputText>
    </h:column>

  </h:dataTable>

</h:form>

  <!-- end content -->
      </body>
    </html>
  </f:view>
