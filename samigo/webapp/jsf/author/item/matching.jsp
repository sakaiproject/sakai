<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: matching.jsp,v 1.32 2005/06/01 23:16:00 lydial.stanford.edu Exp $ -->
<%-- "checked in wysiwyg code but disabled, added in lydia's changes between 1.9 and 1.10" --%>
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      <%-- later, we'll use the new sakai 2.0 stylesheet tags --%>
      <link href="/library/skin/tool_base.css" type="text/css" rel="stylesheet" media="all" />
      <link href="/library/skin/default/tool.css" type="text/css" rel="stylesheet" media="all" />
      <!-- HTMLAREA -->
      <samigo:stylesheet path="/htmlarea/htmlarea.css"/>
      <samigo:script path="/htmlarea/htmlarea.js"/>
      <samigo:script path="/htmlarea/lang/en.js"/>
      <samigo:script path="/htmlarea/dialog.js"/>
      <samigo:script path="/htmlarea/popupwin.js"/>
      <samigo:script path="/htmlarea/popups/popup.js"/>
      <samigo:script path="/htmlarea/navigo_js/navigo_editor.js"/>
      <samigo:script path="/jsf/widget/wysiwyg/samigo/wysiwyg.js"/>
      <!-- AUTHORING -->
      <samigo:script path="/js/authoring.js"/>
<%--
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/authoring.js" %>
//-->
</script>
--%>
      </head>
<%-- unfortunately have to use a scriptlet here --%>
<body onload="countNum();<%= request.getAttribute("html.body.onload") %>">
<%--
      <body onload="javascript:initEditors('<%=request.getContextPath()%>');;<%= request.getAttribute("html.body.onload") %>">
--%>
<!-- content... -->
<!-- FORM -->

<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
  <!-- QUESTION PROPERTIES -->
  <!-- this is for creating multiple choice questions -->
  <%-- kludge: we add in 1 useless textarea, the 1st does not seem to work --%>
  <div style="display:none">
  <h:inputTextarea id="ed0" cols="10" rows="10" value="            " />
  </div>

  <!-- 1 POINTS -->
  <div class="indnt2">

   <span id="num1" class="number"></span>
   <div class="shorttext"> <h:outputLabel for="answerptr" value="#{msg.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" required="true">
<f:validateDoubleRange/>
</h:inputText>
  <h:outputText value="  #{msg.zero_survey}" />
<br/><h:message for="answerptr" styleClass="validate"/>
  </div>
  <!-- 2 TEXT -->

    <span id="num2" class="number"></span>
  <div class="longtext"> <h:outputLabel for="qtextarea" value="#{msg.q_text}" />
  <br/>
  <!-- WYSIWYG -->
  <h:panelGrid width="50%">
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.instruction}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  </h:panelGrid>
  </div>

  <!-- 3 ANSWER -->

      <span id="num3" class="number"></span>
    <div class="longtext"> <h:outputLabel value="#{msg.create_pairing} " /></div>

  <!-- display existing pairs -->

<h:dataTable styleClass="listHier" id="pairs" width="100%" headerClass="regHeading" value="#{itemauthor.currentItem.matchItemBeanList}" var="pair">

      <h:column>
        <f:facet name="header">
          <h:outputText value=""  />
        </f:facet>
          <h:outputText value="#{pair.sequence}"  />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{msg.matching_choice_col}"  />
        </f:facet>
          <h:outputText escape="false" value="#{pair.choice}"  />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value="#{msg.matching_match_col}"  />
        </f:facet>
          <h:outputText escape="false" value="#{pair.match}"  />
      </h:column>

      <h:column>
        <f:facet name="header">
          <h:outputText value=""/>
        </f:facet>

     <h:panelGrid>
     <h:panelGroup>
<h:commandLink rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}" id="modifylink" immediate="true" action="#{itemauthor.currentItem.editMatchPair}">
  <h:outputText id="modifytext" value="#{msg.button_edit}"/>
  <f:param name="sequence" value="#{pair.sequence}"/>
</h:commandLink>

          <h:outputText value="#{msg.matching_currently_editing}" rendered="#{itemauthor.currentItem.currentMatchPair.sequence== pair.sequence}"/>
          <h:outputText value=" | " rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}"/>

<h:commandLink id="removelink" immediate="true" action="#{itemauthor.currentItem.removeMatchPair}" rendered="#{itemauthor.currentItem.currentMatchPair.sequence != pair.sequence}">
  <h:outputText id="removetext" value="#{msg.button_remove}"/>
  <f:param name="sequence" value="#{pair.sequence}"/>
</h:commandLink>
     </h:panelGroup>
     </h:panelGrid>
      </h:column>

     </h:dataTable>

        <!-- WYSIWYG -->
<div class="indnt2">
     <h:panelGrid columns="2" columnClasses="longtext">
          <h:outputLabel value=" Choice: "/>
          <h:outputLabel value=" Match: "/>

   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.choice}">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.match}">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

   </h:panelGrid>
</div>

 <!-- Match FEEDBACK -->
<div class="indnt2">
  <h:panelGrid columns="2" columnClasses="longtext">
  <h:outputLabel value="#{msg.correct_match_feedback_opt}" />
  <h:outputLabel value="#{msg.incorrect_match_feedback_opt}" />


  <!-- WYSIWYG -->
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.corrMatchFeedback}">
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  <!-- WYSIWYG -->
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.currentMatchPair.incorrMatchFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>


  </h:panelGrid>
  </div>

<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
  <h:commandButton value="#{msg.button_save_pair}" action="#{itemauthor.currentItem.addMatchPair}">
  </h:commandButton>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>
<f:verbatim><br/></f:verbatim>

<%--
    <!-- 4 RANDOMIZE -->

    <span id="num4" class="number"></span>
   <div class="longtext">  <h:outputText value="#{msg.randomize_answers}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.randomized}" >
     <f:selectItem itemValue="true"
       itemLabel="#{msg.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{msg.no}" />
    </h:selectOneRadio>
  </div>


    <!-- 5 RATIONALE -->

      <span id="num5" class="number"></span>
   <div class="longtext"> <h:outputText value="#{msg.req_rationale}" />
    <h:selectOneRadio value="#{itemauthor.currentItem.rationale}" >
     <f:selectItem itemValue="true"
       itemLabel="#{msg.yes}" />
     <f:selectItem itemValue="false"
       itemLabel="#{msg.no}" />
    </h:selectOneRadio>
  </div>

--%>
    <!-- 6 PART -->

<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
       <f:verbatim> <span id="num6" class="number"></span></f:verbatim>

  <h:outputLabel value="#{msg.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid>

    <!-- 7 POOL -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">

   <f:verbatim> <span id="num7" class="number"></span></f:verbatim>

  <h:outputLabel value="#{msg.assign_to_question_p}" />
<%-- stub debug --%>
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid>


 <!-- FEEDBACK -->

  <span id="num8" class="number"></span>
 <div class="longtext">
  <h:outputLabel value="#{msg.correct_incorrect_an}" />
  <h:panelGrid columns="2">
  <h:outputLabel value="#{msg.correct_answer_opti}" />
  <h:outputLabel value="#{msg.incorrect_answer_op}" />



  <!-- WYSIWYG -->
    <%--
  <h:panelGrid columns="2">
  <h:inputTextarea id="corrfdbk" value="#{itemauthor.currentItem.corrFeedback}" cols="30" rows="3"/>
  <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
  </h:panelGrid>
    --%>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}"  >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

  <%--</h:panelGroup>

  <h:panelGroup>--%>
  <!-- WYSIWYG -->
    <%--
  <h:panelGrid columns="2">
  <h:inputTextarea id="incorrfdbk" value="#{itemauthor.currentItem.incorrFeedback}" cols="30" rows="3"/>
  <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
  </h:panelGrid>
    --%>
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}"  >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

<%--  </h:panelGroup> --%>

  </h:panelGrid>
  </div>

<!-- METADATA -->

<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim><span id="num9" class="number"></span></f:verbatim>
<h:outputLabel value="Metadata"/><br/>
<f:verbatim><div class="indnt3"></f:verbatim>

<h:panelGrid columns="2" columnClasses="shorttext">
<h:outputLabel value="#{msg.objective}" />
  <h:inputText id="obj" value="#{itemauthor.currentItem.objective}" />
<h:outputLabel value="#{msg.keyword}" />
  <h:inputText id="keyword" value="#{itemauthor.currentItem.keyword}" />
<h:outputLabel value="#{msg.rubric_colon}" />
  <h:inputText id="rubric" value="#{itemauthor.currentItem.rubric}" />
</h:panelGrid>
 <f:verbatim></div></f:verbatim>
</h:panelGroup>
</div>



<p class="act">
  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_save}" action="editAssessment" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>
  <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_save}" action="editPool" styleClass="active">
        <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.ItemAddListener" />
  </h:commandButton>


  <h:commandButton rendered="#{itemauthor.target=='assessment'}" value="#{msg.button_cancel}" action="editAssessment" immediate="true"/>
 <h:commandButton rendered="#{itemauthor.target=='questionpool'}" value="#{msg.button_cancel}" action="editPool" immediate="true"/>
</p>
</h:form>
<!-- end content -->
    </body>
  </html>
</f:view>

