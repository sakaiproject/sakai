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
          <title><h:outputText value="#{questionPoolMessages.q_mgr}"/></title>
          <script type="text/JavaScript">
              <%@ include file="/js/samigotree.js" %>
          </script>
          <script type="text/javascript">
              function flagFolders() {
                  collapseAllRows();
                  flagRows();
              }
              window.onload = flagFolders;
          </script>
      </head>
<body onload="collapseAllRows();flagRows();;<%= request.getAttribute("html.body.onload") %>;disabledButton()">
 <div class="portletBody container-fluid">
<!-- content... -->
<h:form id="questionpool">

<f:verbatim><ul class="navIntraTool actionToolbar" role="menu">
<li role="menuitem" class="firstToolBarItem"><span></f:verbatim>

   <h:commandLink title="#{generalMessages.t_assessment}" rendered="#{questionpool.importToAuthoring == 'true'}" action="author" immediate="true">
   <h:outputText value="#{generalMessages.assessment}"/>
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelImportToAssessmentListener" />
   </h:commandLink>

   <h:commandLink title="#{generalMessages.t_assessment}" rendered="#{questionpool.importToAuthoring == 'false'}" action="author"  immediate="true">
   <h:outputText value="#{generalMessages.assessment}"/>
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>
                                       
<f:verbatim></span></li></f:verbatim>
<h:panelGroup rendered="#{authorization.adminTemplate and template.showAssessmentTypes}">
<li role="menuitem" ><span>
 
   <h:commandLink title="#{generalMessages.t_template}" rendered="#{questionpool.importToAuthoring == 'false'}" action="template" immediate="true">
        <h:outputText value="#{generalMessages.template}"/>
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
   </h:commandLink>

   <h:commandLink title="#{generalMessages.t_questionPool}" rendered="#{questionpool.importToAuthoring == 'true'}" action="template" immediate="true">
        <h:outputText value="#{generalMessages.template}"/>
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelImportToAssessmentListener" />
   </h:commandLink>

</span></li>
</h:panelGroup>
<li role="menuitem" ><span class="current">

        <h:outputText value="#{questionPoolMessages.qps}"/>

<f:verbatim></span></li>

<li role="menuitem" ><span></f:verbatim>
    <h:commandLink id="evnetLogLink" accesskey="#{generalMessages.a_log}" title="#{generalMessages.t_eventLog}" action="eventLog" immediate="true" rendered="#{authorization.adminQuestionPool}">
      <h:outputText value="#{generalMessages.eventLog}" />
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
    </h:commandLink>
<f:verbatim></span></li>
<li role="menuitem" ><span></f:verbatim>
	<h:commandLink id="sectionActivity" accesskey="#{generalMessages.a_section_activity}" title="#{generalMessages.section_activity}" action="sectionActivity" immediate="true" rendered="#{authorization.adminQuestionPool}">
		<h:outputText value="#{generalMessages.section_activity}" />
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.SectionActivityListener" />
	</h:commandLink>
    </span>
  </li> 
</ul>

<h1>
  <h:outputText value="#{generalMessages.questionPool}"/>
</h1>

<h:outputText rendered="#{questionpool.importToAuthoring == 'true'}" value="#{questionPoolMessages.msg_imp_poolmanager}"/>

<div class="tier1">
<h:outputText escape="false" rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" value="<p class=\"navViewAction\">"/>
<h:commandLink title="#{questionPoolMessages.t_addPool}" rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" id="add" immediate="true" action="#{questionpool.addPool}">
 <h:outputText value="#{questionPoolMessages.add_new_pool}"/>
  <f:param name="qpid" value="0"/>
  <f:param name="outCome" value="poolList"/>
</h:commandLink>

<h:outputText value=" #{generalMessages.separator}" rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" />
<h:outputText value="&#160;" escape="false" />

<h:commandLink title="#{questionPoolMessages.t_importPool}" rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" id="import" immediate="true" action="importPool">
 <h:outputText value="#{questionPoolMessages.t_importPool}"/>
 <f:param name="qpid" value="0"/>
 <f:param name="outCome" value="poolList"/>
</h:commandLink> 

<!-- SAM-2049 -->
<h:outputText value=" #{generalMessages.separator}" rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" />
<h:outputText value="&#160;" escape="false" />

<h:commandLink title="#{questionPoolMessages.t_transferPool}" rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" 
	id="transfer" immediate="true" action="#{questionpool.transferPool}">
	<h:outputText value="#{questionPoolMessages.transfer_pool_ownership}" />
	<f:param name="qpid" value="0" />
	<f:param name="outCome" value="poolList"/>
</h:commandLink>
 
<h:outputText rendered="#{questionpool.importToAuthoring == 'false' && authorization.createQuestionPool}" escape="false" value="</p>"/>
</div>

<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
 
<div class="tier2">
<%@ include file="/jsf/questionpool/poolTreeTable.jsp" %>

 </div>


<p class="act">
 
<h:commandButton rendered="#{questionpool.importToAuthoring == 'false' && authorization.deleteOwnQuestionPool}" type="submit" immediate="true" id="Submit" value="#{questionPoolMessages.delete}" action="#{questionpool.startRemovePool}" styleClass="active" >
  </h:commandButton>

  <h:commandButton rendered="#{questionpool.importToAuthoring == 'true'}"  type="submit" immediate="true" id="cancel" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelImport}"  >
  </h:commandButton>
</p>

</h:form>
</div>
      </body>
    </html>
  </f:view>
