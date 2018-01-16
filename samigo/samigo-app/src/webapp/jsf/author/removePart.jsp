<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<!--
* $Id$
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
      <title><h:outputText value="#{authorMessages.remove_p_conf}" /></title>
      <samigo:script path="/../library/js/spinner.js" type="text/javascript"/>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">

<div class="portletBody">
 <!-- content... -->

 <h:form>
   <h3> <h:outputText  value="#{authorMessages.remove_p_conf}" /></h3>
   <h:panelGrid cellpadding="6" cellspacing="4">
     <h:panelGroup>
      <f:verbatim><div class="validation"></f:verbatim>
     <h:outputText value="#{authorMessages.choose_rem}" />
     <f:verbatim></div></f:verbatim>
     </h:panelGroup>
     <h:panelGrid columns="1">
       <h:selectOneRadio value="#{sectionBean.removeAllQuestions}" layout="pageDirection">
         <f:selectItem itemValue="1"
           itemLabel="#{authorMessages.rem_p_all}" />
         <f:selectItem itemValue="0"
           itemLabel="#{authorMessages.rem_p_only}" />
       </h:selectOneRadio>
       <h:panelGroup>
         <f:verbatim>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</f:verbatim>
         <h:selectOneMenu id="sectionId" value="#{sectionBean.destSectionId}" >
           <f:selectItem itemValue="" itemLabel="#{authorMessages.select_one}"/>
           <f:selectItems value="#{assessmentBean.otherSectionList}" />
         </h:selectOneMenu>
       </h:panelGroup>
     </h:panelGrid>
 </h:panelGrid>
   <p class="act">
      <h:commandButton type="submit" value="#{commonMessages.remove_action}" action="removePart" styleClass="active" onclick="SPNR.disableControlsAndSpin( this, null );" >
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.RemovePartListener" />
      </h:commandButton>
      <h:commandButton value="#{commonMessages.cancel_action}" type="submit" action="editAssessment" onclick="SPNR.disableControlsAndSpin( this, null );" />
   </p>
 </h:form>
 <!-- end content -->
<!-- end content -->
</div>

      </body>
    </html>
  </f:view>
