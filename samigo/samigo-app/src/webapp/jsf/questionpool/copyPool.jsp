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
      <title><h:outputText value="#{questionPoolMessages.copy_p}"/></title>
                        <!-- stylesheet and script widgets -->
<script type="text/JavaScript">
<%@ include file="/js/samigotree.js" %>
              function flagFolders() {
	          collapseAllRowsForSelectList();
                  flagRows();
              }
              function initPage()
              {
                  var poolSubmitButton = document.getElementById('copyPool:copypoolsubmit');
                  if (poolSubmitButton !== null)
                  {
                      poolSubmitButton.disabled=true;
                  }
                  var itemSubmitButton = document.getElementById('copyPool:copyitemsubmit');
                  if (itemSubmitButton !== null)
                  {
                      itemSubmitButton.disabled=true;
                  }
                  flagFolders();
              }
              window.onload = initPage;
</script>
<samigo:script path="/../library/js/spinner.js"/>
</head>
<body onload="collapseAllRowsForSelectList();flagRows();;<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
<h:form id="copyPool">
<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
 
<h3>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionPoolMessages.copy_p}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.copy_q}"/>
</h3>


<div class="tier1">
<h:outputText value="#{questionPoolMessages.sel_dest_copy} "/>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionpool.currentPool.displayName}"/>
<h:outputText rendered="#{questionpool.actionType == 'item' && questionpool.currentItems[1] == null}" escape="false" value="#{questionpool.currentItems[0].text}"/>

<h:dataTable rendered="#{questionpool.actionType == 'item' && questionpool.currentItems[1] != null}" id="questions" value="#{questionpool.currentItems}" var="curItem" >
<h:column>
<h:outputText escape="false" value="#{curItem.text}"/>
</h:column>

</h:dataTable>

</div>
<div class="longtext tier2">
<%--
<h:outputText styleClass="number" value="1"/>
<h:outputLabel rendered="#{questionpool.actionType == 'pool'}" value="#{questionPoolMessages.copy_p_to}"/>
<h:outputLabel rendered="#{questionpool.actionType == 'item'}" value="#{questionPoolMessages.copy_q_to}"/>

<br/><br/>
--%>

<%@ include file="/jsf/questionpool/copyPoolTree.jsp" %>

<%--
<br/>
<h:outputText styleClass="number" value="2"/>
<h:outputLabel value="#{questionPoolMessages.click_copy}"/>

--%>
</div>

<p class="act">

  <h:commandButton id="copypoolsubmit" immediate="true" value="#{questionPoolMessages.copy}"
    action="#{questionpool.copyPool}" styleClass="active" rendered="#{questionpool.actionType == 'pool'}"
    onclick="SPNR.disableControlsAndSpin(this, null);">
  </h:commandButton>

  <h:commandButton id="copyitemsubmit" immediate="true" value="#{questionPoolMessages.copy}"
    action="#{questionpool.copyQuestion}" styleClass="active" rendered="#{questionpool.actionType == 'item'}"
    onclick="SPNR.disableControlsAndSpin(this, null);">
  </h:commandButton>

	<h:commandButton id="cancel" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" immediate="true"
					 rendered="#{questionpool.actionType == 'pool'}" onclick="SPNR.disableControlsAndSpin(this, null);">
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener" />
		<f:attribute name="returnToParentPool" value="true"/>
	</h:commandButton>
  
	<h:commandButton id="cancelItem" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" immediate="true"
					 rendered="#{questionpool.actionType == 'item'}" onclick="SPNR.disableControlsAndSpin(this, null);">
		<f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener" />
		<f:attribute name="returnToParentPool" value="false"/>
	</h:commandButton>
</p>
</h:form>
</div>
</body>
</html>
</f:view>
