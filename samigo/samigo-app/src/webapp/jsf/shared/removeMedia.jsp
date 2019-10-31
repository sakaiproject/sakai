<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!-- $Id$
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
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{deliveryMessages.remove_p_conf}" /></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
 <!-- content... -->

 <h:form>
   <h:inputHidden id="mediaId" value="#{mediaBean.mediaId}"/>
   <h3> <h:outputText  value="#{deliveryMessages.remove_media_conf}" /></h3>
   <div class="validation tier1">
          <h:outputText value="#{deliveryMessages.cert_rem_media}" />
   </div>
   <p>
     <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
     <h:outputLink title="#{deliveryMessages.t_media}" value="#" onclick="window.open('#{mediaBean.mediaUrl}','new_window');" onkeypress="window.open('#{mediaBean.mediaUrl}','new_window');">
       <h:outputText value="#{mediaBean.filename}" />
     </h:outputLink>
   </p>
   <p class="act">
      <h:commandButton id="remove" type="submit" value="#{commonMessages.remove_action}" action="#{delivery.getOutcome}" styleClass="active">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.shared.RemoveMediaListener" />
      </h:commandButton>
      <h:commandButton id="cancel" value="#{commonMessages.cancel_action}" type="submit" action="takeAssessment">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.delivery.UpdateTimerListener" />
      </h:commandButton>
   </p>
 </h:form>
 <!-- end content -->
<!-- end content -->

      </body>
    </html>
  </f:view>
