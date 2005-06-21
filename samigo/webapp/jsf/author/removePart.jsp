<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: removePart.jsp,v 1.13 2005/05/24 16:54:50 janderse.umich.edu Exp $ -->
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
      <title><h:outputText value="#{msg.remove_p_conf}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->

 <h:form>
   <h3> <h:outputText  value="#{msg.remove_p_conf}" /></h3>
   <h:panelGrid cellpadding="6" cellspacing="4">
     <h:panelGroup>
      <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{msg.choose_rem}" />
     <f:verbatim></div></f:verbatim>
     </h:panelGroup>
     <h:panelGrid columns="1">
       <h:selectOneRadio value="#{sectionBean.removeAllQuestions}" layout="pageDirection">
         <f:selectItem itemValue="1"
           itemLabel="#{msg.rem_p_all}" />
         <f:selectItem itemValue="0"
           itemLabel="#{msg.rem_p_only}" />
       </h:selectOneRadio>
       <h:panelGroup>
         <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
         <h:selectOneMenu id="sectionId" value="#{sectionBean.destSectionId}" >
           <f:selectItem itemValue="" itemLabel="select one ..."/>
           <f:selectItems value="#{assessmentBean.otherSectionList}" />
         </h:selectOneMenu>
       </h:panelGroup>
     </h:panelGrid>
 </h:panelGrid>
   <p class="act">
      <h:commandButton type="submit" value="#{msg.button_remove}" action="removePart" styleClass="active">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePartListener" />
      </h:commandButton>
       <h:commandButton value="#{msg.button_cancel}" type="submit"
         action="editAssessment" />
   </p>
 </h:form>
 <!-- end content -->
<!-- end content -->

      </body>
    </html>
  </f:view>
