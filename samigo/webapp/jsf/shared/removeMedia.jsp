<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!-- $Id: removeMedia.jsp,v 1.3 2005/06/10 16:48:12 daisyf.stanford.edu Exp $ -->
  <f:view>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.DeliveryMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.remove_p_conf}" /></title>
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->

 <h:form>
   <h:inputHidden id="mediaId" value="#{mediaBean.mediaId}"/>
   <h3> <h:outputText  value="#{msg.remove_media_conf}" /></h3>
   <div class="validation indnt1">
          <h:outputText value="#{msg.cert_rem_media}" />
   </div>
   <p>
     <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
     <h:outputLink value="#" onclick="window.open('#{mediaBean.mediaUrl}','new_window');">
       <h:outputText escape="false" value="#{mediaBean.filename}" />
     </h:outputLink>
   </p>
   <p class="act">
      <h:commandButton type="submit" value="#{msg.button_remove}" action="takeAssessment" styleClass="active">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.RemoveMediaListener" />
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
