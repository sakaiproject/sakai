<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: selectQuestionType.jsp,v 1.10 2005/05/25 23:21:54 lydial.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
     var="qpmsg"/>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->
<h:form>

<!-- CHANGE TYPE -->
 <div class="shorttext indnt1">
  <h:outputText styleClass="number" value="1" />

<h:outputText value="#{qpmsg.sel_q_type}"/>
<h:selectOneMenu id="selType" value="#{itemauthor.itemType}" required="true">
  <f:selectItem itemLabel="#{msg.select_qtype}" itemValue=""/>
  <f:selectItem itemLabel="#{msg.multiple_choice_type}" itemValue="1"/>
  <f:selectItem itemLabel="#{msg.multiple_choice_surv}" itemValue="3"/>
  <f:selectItem itemLabel="#{msg.short_answer_essay}" itemValue="5"/>
  <f:selectItem itemLabel="#{msg.fill_in_the_blank}" itemValue="8"/>
  <f:selectItem itemLabel="#{msg.matching}" itemValue="9"/>
  <f:selectItem itemLabel="#{msg.file_upload}" itemValue="6"/>
  <f:selectItem itemLabel="#{msg.true_false}" itemValue="4"/>
</h:selectOneMenu>
<h:message for="selType" styleClass="validate"/>
</div>
 <div class="shorttext indnt1">
  <h:outputText styleClass="number" value="2" />
</div>




<h:outputText value="#{qpmsg.click_save}"/>
<p class="act">
<h:commandButton type="submit"  action="#{itemauthor.doit}" value="#{msg.button_save}" styleClass="active">
   <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
   <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
</h:commandButton>

  <h:commandButton type="button" id="Cancel" value="#{msg.button_cancel}" immediate="true"
    onclick="document.location='editPool.faces'"/>
</p>




</h:form>


<!-- end content -->
    </body>
  </html>
</f:view>
