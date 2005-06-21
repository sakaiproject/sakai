
<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: fillInTheBlank.jsp,v 1.29 2005/06/10 00:02:08 esmiley.stanford.edu Exp $ -->
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
      <samigo:script path="/js/authoring.js"/>
      </head>
      <body onload="countNum();<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->

<!-- HEADING -->
<%@ include file="/jsf/author/item/itemHeadings.jsp" %>
<h:form id="itemForm">
<!-- QUESTION PROPERTIES -->
  <!-- 1 POINTS -->
<div class="indnt2">

    <span id="num1" class="number"></span>
    <div class="shorttext"><h:outputLabel for="answerptr" value="#{msg.answer_point_value}" />
    <h:inputText id="answerptr" value="#{itemauthor.currentItem.itemScore}" >
<f:validateDoubleRange />
</h:inputText>
  <h:outputText value="  #{msg.zero_survey}" />
 <br/>  <h:message for="answerptr" styleClass="validate"/>
  </div>
  <!-- 2 TEXT -->

  <span id="num2" class="number"></span>
  <div class="longtext indnt2"> <h:outputLabel for="qtextarea" value="#{msg.q_text}" />
<br/>
  <h:outputText value="#{msg.note_place_curly}" />
  <f:verbatim><br/></f:verbatim>
  <h:outputText value="#{msg.for_example_curly}" />
  <br/>
<%-- PLAIN TEXTAREA QUESTION so we can allow brackets --%>
<%-- to work around the fact that we are not using a PLAIN TEXTAREA , we use a clean up
  script to fix simple XHTML errors that would mess up export, of course it would be
  nice if user would not try to get so fancy  --%>
<script>
<!--
// clean up script to fix simple XHTML errors that would mess up export
// not foolproof in presence of sufficiently sophisticated fool
function cleanUpTags(html)
{

  // convert all tags to lowercase (includes attributes)
  html = html.replace(/<[^>]+>/g, function(w) { return w.toLowerCase() }).

  // fix funky "standalone" tags, e.g. SAM 458
  replace(/<br>/gi,'<br \/>').
  replace(/<hr>/gi,'<hr \/>').
  replace(/<img[^>]+>/g, function(w) { return w.replace(/>/gi, '/>') }).

  // find empty tags, some horror from cut and paste no doubt
  replace(/<p><\/p>/gi,'').
  replace(/<i><\/i>/gi,'').
  replace(/<b><\/b>/gi,'').
  replace(/<h1><\/h1>/gi,'').
  replace(/<h2><\/h2>/gi,'').
  replace(/<h3><\/h3>/gi,'').
  replace(/<h4><\/h4>/gi,'').
  replace(/<h5><\/h5>/gi,'').
  replace(/<h6><\/h6>/gi,'').
  replace(/<em><\/em>/gi,'').
  replace(/<strong><\/strong>/gi,'').
  replace(/<pre><\/pre>/gi,'').
  replace(/<tt><\/tt>/gi,'').
  replace(/<div><\/div>/gi,'').
  replace(/<span><\/span>/gi,'').

	// nuke double spaces, they won't show up in HTML
	replace(/  */gi,' ');

  return html;

};
//-->
</script>
<h:inputTextarea id="qtextarea_nowysiwyg"
  onchange="this.value=cleanUpTags(this.value)"
  cols="48" rows="9" value="#{itemauthor.currentItem.itemText}" required="true" />
<%-- END PLAIN TEXTAREA QUESTION --%>
<br />
    <h:message for="qtextarea_nowysiwyg" styleClass="validate"/><br/>
<%--
    <h:panelGrid columns="2">
      <h:inputTextarea id="qtextarea" cols="48" rows="9" value="#{itemauthor.currentItem.itemText}" />
      <h:outputLink id="sh_qtextarea" value="#"
        onclick="hideUnhide(this.id.substring(0,this.id.indexOf('sh_qtextarea'))+'qtextarea');">
        <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
      </h:outputLink>
    </h:panelGrid>
    <h:inputText id="qtextarea" size="150" value="#{itemauthor.currentItem.itemText}" />
--%>
  </div>

  <!-- 3 PART -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
   <f:verbatim><span id="num3" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{itemauthor.currentItem.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <!-- use this in real  value="#{section.sectionNumberList}" -->
  </h:selectOneMenu>
  </h:panelGrid>

  <!-- 5 POOL -->
<h:panelGrid columns="3" columnClasses="shorttext" rendered="#{itemauthor.target == 'assessment'}">
   <f:verbatim><span id="num4" class="number"></span></f:verbatim>
  <h:outputLabel value="#{msg.assign_to_question_p}" />
<%-- stub debug --%>
  <h:selectOneMenu id="assignToPool" value="#{itemauthor.currentItem.selectedPool}">
     <f:selectItem itemValue="" itemLabel="#{msg.select_a_pool_name}" />
     <f:selectItems value="#{itemauthor.poolSelectList}" />
  </h:selectOneMenu>

  </h:panelGrid>

 <!-- FEEDBACK -->

  <span id="num5" class="number"></span>
<div class="longtext">
 <h:outputLabel value="#{msg.correct_incorrect_an}" />
<div class="indnt3">

  <h:panelGrid columns="2">
  <h:outputLabel value="#{msg.correct_answer_opti}" />
  <h:outputLabel value="#{msg.incorrect_answer_op}" />

   <!-- WYSIWYG  -->
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.corrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

<%--
   <h:panelGrid columns="2">
     <h:inputTextarea id="corrfdbk" cols="48" rows="9" value="#{itemauthor.currentItem.corrFeedback}" />
     <h:outputLink id="sh_corrfdbk" value="#"
       onclick="hideUnhide(this.id.substring(0,this.id.indexOf('sh_corrfdbk'))+'corrfdbk');">
       <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
     </h:outputLink>
   </h:panelGrid>
--%>
   <!-- WYSIWYG  -->
   <samigo:wysiwyg rows="140" value="#{itemauthor.currentItem.incorrFeedback}" >
     <f:validateLength maximum="4000"/>
   </samigo:wysiwyg>

<%--
   <h:panelGrid columns="2">
     <h:inputTextarea id="incorrfdbk" cols="48" rows="9" value="#{itemauthor.currentItem.incorrFeedback}" />
     <h:outputLink id="sh_incorrfdbk" value="#"
       onclick="hideUnhide(this.id.substring(0,this.id.indexOf('sh_incorrfdbk'))+'incorrfdbk');">
       <h:outputText value="#{msg.show_hide}<br />#{msg.editor}" escape="false"/>
     </h:outputLink>
   </h:panelGrid>
--%>

  </h:panelGrid>
  </div>  </div>


 <!-- METADATA -->

<h:panelGroup rendered="#{itemauthor.showMetadata == 'true'}" styleClass="longtext">
<f:verbatim><span id="num6" class="number"></span></f:verbatim>
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
