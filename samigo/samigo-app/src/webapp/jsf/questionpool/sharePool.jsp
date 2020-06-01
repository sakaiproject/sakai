<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
 
<!-- $Id: poolList.jsp 22618 2007-03-14 19:58:35Z ktsao@stanford.edu $
<%--
***********************************************************************************
*
* Copyright (c) 2006 The Sakai Foundation.
*
* Licensed under the Educational Community License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.osedu.org/licenses/ECL-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.s
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
<script>
<%@ include file="/js/samigotree.js" %>
	
function flagFolders() {
    collapseAllRows();
    flagRows();
}

function initPage()
{
    document.getElementById('questionpoolshare:Submit').disabled=true;
    flagFolders();
}

window.onload = initPage;

function checkUpdate()
{
    updateButtonStatusOnCheck(document.getElementById('questionpoolshare:Submit'), document.getElementById('questionpoolshare'));
}

</script>
<script src="/library/js/spinner.js"></script>
      </head>
<body onload="collapseAllRows();flagRows();disabledButton();<%= request.getAttribute("html.body.onload") %>">
 <div class="portletBody">
<!-- content... -->
<h:form id="questionpoolshare">

<ul class="navIntraTool actionToolbar" role="menu">
    <li role="menuitem">
        <span>
            <h:commandLink title="#{generalMessages.t_assessment}" action="author"  immediate="true">
                <h:outputText value="#{generalMessages.assessment}"/>
                <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
            </h:commandLink>
        </span>
    </li>
    <h:panelGroup rendered="#{authorization.adminTemplate and template.showAssessmentTypes}">
        <li role="menuitem">
            <span>
                <h:commandLink title="#{generalMessages.t_template}" action="template" immediate="true">
                    <h:outputText value="#{generalMessages.template}"/>
                    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
                </h:commandLink>
            </span>
        </li>
    </h:panelGroup>
    <li role="menuitem">
        <span class="current">
            <h:outputText value="#{questionPoolMessages.qps}"/>
        </span>
    </li>
</ul>


 <h3><h:outputText value="#{questionPoolMessages.share_pool}"/></h3>

<h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
 
<div class="tier1">
<h4><h:outputText value="#{questionPoolMessages.members_with_access} #{questionpoolshare.questionPoolName}"/></h4>
</div>

<div class="tier2">
<h:dataTable cellpadding="0" cellspacing="0" id="withAccessTable" value="#{questionpoolshare.agentsWithAccess}"
    var="agent" styleClass="listHier" >

    <h:column id="col1">

     	<f:facet name="header">
      		<h:panelGroup>
		       	<h:commandLink title="#{questionPoolMessages.alt_sortName}" id="sortByDisplayName" immediate="true"  rendered="#{questionpoolshare.sortPropertyWith !='displayName'}" action="#{questionpoolshare.sortByColumnHeader}">
		          	<f:param name="orderBy" value="displayName"/>
		          	<f:param name="ascending" value="true"/>
					<f:param name="list" value="agentsWithAccess"/>
		          	<h:outputText  value="#{questionPoolMessages.name}"  rendered="#{questionpoolshare.sortPropertyWith !='displayName'}" />
		       	</h:commandLink>
      
		       	<h:commandLink title="#{questionPoolMessages.alt_sortName}" immediate="true" rendered="#{questionpoolshare.sortPropertyWith =='displayName' && questionpoolshare.sortAscendingWith }"  action="#{questionpoolshare.sortByColumnHeader}">
		           	<h:outputText  value="#{questionPoolMessages.name}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWith =='displayName'}" />
		          	<f:param name="orderBy" value="displayName"/>
		          	<f:param name="ascending" value="false" />
					<f:param name="list" value="agentsWithAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortNameDescending}" rendered="#{questionpoolshare.sortAscendingWith}" url="/images/sortascending.gif"/>
		      	</h:commandLink>

		      	<h:commandLink title="#{questionPoolMessages.alt_sortName}" immediate="true" rendered="#{questionpoolshare.sortPropertyWith =='displayName' && !questionpoolshare.sortAscendingWith }"  action="#{questionpoolshare.sortByColumnHeader}">
		           	<h:outputText  value="#{questionPoolMessages.name}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWith =='displayName'}" />
		          	<f:param name="orderBy" value="displayName"/>
		          	<f:param name="ascending" value="true" />
					<f:param name="list" value="agentsWithAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortNameAscending}" rendered="#{!questionpoolshare.sortAscendingWith}" url="/images/sortdescending.gif"/>
		      	</h:commandLink>
     		</h:panelGroup>
     	</f:facet>

		<h:panelGroup id="firstcolumn">
  			<h:outputText value="#{agent.displayName}"/>
		</h:panelGroup>
	</h:column>

  
    <h:column id="col2">
     	<f:facet name="header">
     		<h:panelGroup>
       			<h:commandLink title="#{questionPoolMessages.alt_sortRole}" id="sortByRole" immediate="true"  rendered="#{questionpoolshare.sortPropertyWith !='role'}" action="#{questionpoolshare.sortByColumnHeader}">
		          	<f:param name="orderBy" value="role"/>
		          	<f:param name="ascending" value="true"/>
					<f:param name="list" value="agentsWithAccess"/>
		          	<h:outputText  value="#{questionPoolMessages.role}"  rendered="#{questionpoolshare.sortPropertyWith !='role'}" />
		       	</h:commandLink>
      
		       	<h:commandLink title="#{questionPoolMessages.alt_sortRole}" immediate="true" rendered="#{questionpoolshare.sortPropertyWith =='role' && questionpoolshare.sortAscendingWith }"  action="#{questionpoolshare.sortByColumnHeader}">
		           	<h:outputText  value="#{questionPoolMessages.role}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWith =='role'}" />
		          	<f:param name="orderBy" value="role"/>
		          	<f:param name="ascending" value="false" />
					<f:param name="list" value="agentsWithAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortRoleDescending}" rendered="#{questionpoolshare.sortAscendingWith}" url="/images/sortascending.gif"/>
		      	</h:commandLink>
		      	<h:commandLink title="#{questionPoolMessages.alt_sortRole}" immediate="true" rendered="#{questionpoolshare.sortPropertyWith =='role' && !questionpoolshare.sortAscendingWith }"  action="#{questionpoolshare.sortByColumnHeader}">
		          	<h:outputText  value="#{questionPoolMessages.role}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWith =='role'}" />
		          	<f:param name="orderBy" value="role"/>
		          	<f:param name="ascending" value="true" />
					<f:param name="list" value="agentsWithAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortRoleAscending}" rendered="#{!questionpoolshare.sortAscendingWith}" url="/images/sortdescending.gif"/>
		      	</h:commandLink>
     		</h:panelGroup>
     	</f:facet>
     	<h:panelGroup id="secondcolumn">
        	<h:outputText value="#{agent.role}"/>
     	</h:panelGroup>
    </h:column>

    <h:column id="col3" >
     	<f:facet name="header">
       		<h:outputText value="#{questionPoolMessages.revoke_access}"/>
     	</f:facet>
		<h:selectManyCheckbox id="revokeCheckbox" value ="#{questionpoolshare.destPools}" onclick="checkUpdate();" onkeypress="checkUpdate();">
			<f:selectItem itemValue="#{agent.agentInstanceString}" itemDisabled="#{agent.agentInstanceString == questionpoolshare.questionPoolOwnerId}" itemLabel=""/>
		</h:selectManyCheckbox>
	</h:column>

</h:dataTable>
</div>

<br/>

<div class="tier1">
<h4><h:outputText value="#{questionPoolMessages.members_without_access} #{questionpoolshare.questionPoolName}"/></h4>
</div>

<div class="tier2">
<h:dataTable cellpadding="0" cellspacing="0" id="withoutAccessTable" value="#{questionpoolshare.agentsWithoutAccess}"
    var="agent" styleClass="listHier" >

    <h:column id="col1" >

     	<f:facet name="header">
      		<h:panelGroup>
		       	<h:commandLink title="#{questionPoolMessages.alt_sortName}" id="sortByDisplayName" immediate="true"  rendered="#{questionpoolshare.sortPropertyWithout !='displayName'}" action="#{questionpoolshare.sortByColumnHeader}">
		          	<f:param name="orderBy" value="displayName"/>
		          	<f:param name="ascending" value="true"/>
					<f:param name="list" value="agentsWithoutAccess"/>
		          	<h:outputText  value="#{questionPoolMessages.name}" rendered="#{questionpoolshare.sortPropertyWithout !='displayName'}" />
		       	</h:commandLink>
      
		       	<h:commandLink title="#{questionPoolMessages.alt_sortName}" immediate="true" rendered="#{questionpoolshare.sortPropertyWithout =='displayName' && questionpoolshare.sortAscendingWithout }"  action="#{questionpoolshare.sortByColumnHeader}">
		           	<h:outputText  value="#{questionPoolMessages.name}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWithout =='displayName'}" />
		          	<f:param name="orderBy" value="displayName"/>
		          	<f:param name="ascending" value="false" />
					<f:param name="list" value="agentsWithoutAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortNameDescending}" rendered="#{questionpoolshare.sortAscendingWithout}" url="/images/sortascending.gif"/>
		      	</h:commandLink>

		      	<h:commandLink title="#{questionPoolMessages.alt_sortName}" immediate="true" rendered="#{questionpoolshare.sortPropertyWithout =='displayName' && !questionpoolshare.sortAscendingWithout }"  action="#{questionpoolshare.sortByColumnHeader}">
		           	<h:outputText  value="#{questionPoolMessages.name}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWithout =='displayName'}" />
		          	<f:param name="orderBy" value="displayName"/>
		          	<f:param name="ascending" value="true" />
					<f:param name="list" value="agentsWithoutAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortNameAscending}" rendered="#{!questionpoolshare.sortAscendingWithout}" url="/images/sortdescending.gif"/>
		      	</h:commandLink>
     		</h:panelGroup>
     	</f:facet>

		<h:panelGroup id="firstcolumn">
  			<h:outputText value="#{agent.displayName}"/>
		</h:panelGroup>
	</h:column>

  
    <h:column id="col2">
     	<f:facet name="header">
     		<h:panelGroup>
       			<h:commandLink title="#{questionPoolMessages.alt_sortRole}" id="sortByRole" immediate="true"  rendered="#{questionpoolshare.sortPropertyWithout !='role'}" action="#{questionpoolshare.sortByColumnHeader}">
		          	<f:param name="orderBy" value="role"/>
		          	<f:param name="ascending" value="true"/>
					<f:param name="list" value="agentsWithoutAccess"/>
		          	<h:outputText  value="#{questionPoolMessages.role}" rendered="#{questionpoolshare.sortPropertyWithout !='role'}" />
		       	</h:commandLink>
      
		       	<h:commandLink title="#{questionPoolMessages.alt_sortRole}" immediate="true" rendered="#{questionpoolshare.sortPropertyWithout =='role' && questionpoolshare.sortAscendingWithout }"  action="#{questionpoolshare.sortByColumnHeader}">
		           	<h:outputText  value="#{questionPoolMessages.role}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWithout =='role'}" />
		          	<f:param name="orderBy" value="role"/>
		          	<f:param name="ascending" value="false" />
					<f:param name="list" value="agentsWithoutAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortRoleDescending}" rendered="#{questionpoolshare.sortAscendingWithout}" url="/images/sortascending.gif"/>
		      	</h:commandLink>
		      	<h:commandLink title="#{questionPoolMessages.alt_sortRole}" immediate="true" rendered="#{questionpoolshare.sortPropertyWithout =='role' && !questionpoolshare.sortAscendingWithout }"  action="#{questionpoolshare.sortByColumnHeader}">
		          	<h:outputText  value="#{questionPoolMessages.role}" styleClass="currentSort" rendered="#{questionpoolshare.sortPropertyWithout =='role'}" />
		          	<f:param name="orderBy" value="role"/>
		          	<f:param name="ascending" value="true" />
					<f:param name="list" value="agentsWithoutAccess"/>
		          	<h:graphicImage alt="#{questionPoolMessages.alt_sortRoleAscending}" rendered="#{!questionpoolshare.sortAscendingWithout}" url="/images/sortdescending.gif"/>
		      	</h:commandLink>
     		</h:panelGroup>
     	</f:facet>
     	<h:panelGroup id="secondcolumn">
        	<h:outputText value="#{agent.role}"/>
     	</h:panelGroup>
    </h:column>

    <h:column id="col3" >
     	<f:facet name="header">
       		<h:outputText value="#{questionPoolMessages.grant_access}"/>
     	</f:facet>
		<h:selectManyCheckbox id="grantCheckbox" value ="#{questionpoolshare.destPools}" onclick="checkUpdate();" onkeypress="checkUpdate();">
			<f:selectItem itemValue="#{agent.agentInstanceString}" itemDisabled="#{agent.agentInstanceString == questionpoolshare.questionPoolOwnerId}" itemLabel=""/>
		</h:selectManyCheckbox>
	</h:column>

</h:dataTable>
</div>

<p class="act">
 
  <h:commandButton type="submit" immediate="true" id="Submit" value="#{questionPoolMessages.t_updateSharedPoolAccess}"
                   action="#{questionpoolshare.sharePool}" onclick="SPNR.disableControlsAndSpin(this, null);">
  </h:commandButton>

  <h:commandButton type="submit" immediate="true" id="cancel" value="#{commonMessages.cancel_action}"
                   action="poolList" onclick="SPNR.disableControlsAndSpin(this, null);">
  </h:commandButton>
</p>

</h:form>
</div>
      </body>
    </html>
  </f:view>
