<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
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
      <title><h:outputText value="#{questionPoolMessages.edit_p}"/></title>

<%@ include file="/js/delivery.js" %>

<script>
var textcheckall="<h:outputText value="#{questionPoolMessages.t_checkAll}"/>";
var textuncheckall="<h:outputText value="#{questionPoolMessages.t_uncheckAll}"/>";
<%@ include file="/js/samigotree.js" %>
<%@ include file="/js/authoring.js" %>

function textCounter(field, maxlimit) {
        if (field.value.length > maxlimit) // if too long...trim it!
                field.value = field.value.substring(0, maxlimit);
}

</script>
<f:verbatim rendered="#{questionpool.showTags && questionpool.canManageTags}">
	<script>
		// Initialize input sync
		window.addEventListener("load", () => {
			window.syncTagSelectorInput("tag-selector", "editform:questionPoolTags");
		});
	</script>
</f:verbatim>
<script src="/library/js/spinner.js"></script>
          <script>
              function flagFolders() {
                  collapseRowsByLevel(<h:outputText value="#{questionpool.htmlIdLevel}"/>);
              }
              function initPage()
              {
                  checkUpdate();
                  var importButton = document.getElementById('editform:import');
                  if (importButton !== null)
                  {
                      importButton.disabled=true;
                  }
                  flagFolders();
              }
              window.onload = initPage;
          </script>
      </head>

<f:verbatim><body onload="checkUpdate();collapseRowsByLevel(</f:verbatim><h:outputText value="#{questionpool.htmlIdLevel}"/><f:verbatim>);<%= request.getAttribute("html.body.onload") %>;"></f:verbatim>
  
<div class="portletBody container-fluid">
<h:form id="editform">
  <!-- HEADINGS -->
  <%@ include file="/jsf/questionpool/questionpoolHeadings.jsp" %>

<br />
<h:panelGroup rendered="#{questionpool.currentPool.showParentPools}">
  <ol class="breadcrumb">
    <li>
      <h:outputText value="#{authorMessages.global_nav_pools}" />
    </li>
    <samigo:dataLine value="#{questionpool.currentPool.parentPoolsArray}" var="parent" separator="" first="0" rows="100" >
      <h:column>
        <li>
          <h:commandLink action="#{questionpool.editPool}" immediate="true">
            <h:outputText value="#{parent.displayName}" escape="false"/>
            <f:param name="qpid" value="#{parent.questionPoolId}"/>
          </h:commandLink>
        </li>
      </h:column>
    </samigo:dataLine>
    <li>
      <h:outputText value="#{questionpool.currentPool.displayName}"/>
    </li>
  </ol>
</h:panelGroup>
<div class="page-header">
  <h1>
    <h:outputText value="#{questionPoolMessages.qp}#{questionPoolMessages.column} #{questionpool.currentPool.displayName}"/>
  </h1>
</div>

<h:messages styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<h:outputText rendered="#{questionpool.importToAuthoring == 'true'}" value="#{questionPoolMessages.msg_imp_editpool}"/>

<div class="form-group row"> 
    <h:outputLabel for="namefield" value="#{questionPoolMessages.p_name}" styleClass="col-sm-2  form-control-label"/>
    <div class="col-sm-6">
    	<h:inputText readonly="#{questionpool.importToAuthoring == 'true' || questionpool.owner!=questionpool.currentPool.owner}"  id="namefield" size="30" maxlength="255" value="#{questionpool.currentPool.displayName}" styleClass="form-control"/>
    </div>
</div>
<div class="form-group row"> 
    <h:outputLabel for="ownerfield" value="#{questionPoolMessages.creator}" styleClass="col-sm-2  form-control-label"/>
    <div class="col-sm-6">
        <h:outputText id="ownerfield" value="#{questionpool.currentPool.owner}"/>
    </div>
</div>
<h:panelGroup layout="block" styleClass="form-group row">
    <h:outputLabel for="orgfield" value="#{questionPoolMessages.dept}" styleClass="col-sm-2 form-control-label"/>
    <div class="col-sm-6">
       <h:inputText readonly="#{questionpool.importToAuthoring == 'true' || questionpool.owner!=questionpool.currentPool.owner}"  id="orgfield" size="30" maxlength="255" value="#{questionpool.currentPool.organizationName}" styleClass="form-control"/>
    </div>
</h:panelGroup>    
<h:panelGroup layout="block" styleClass="form-group row">
    <h:outputLabel for="descfield" value="#{questionPoolMessages.desc}" styleClass="col-sm-2 form-control-label"/>
    <div class="col-sm-6">
        <h:inputTextarea readonly="#{questionpool.importToAuthoring == 'true' || questionpool.owner!=questionpool.currentPool.owner}"
         onchange="inIt();" id="descfield" value="#{questionpool.currentPool.description}" cols="40" rows="6"/>
    </div>
</h:panelGroup>  
<h:panelGroup layout="block"  styleClass="form-group row">
    <h:outputLabel for="objfield" value="#{questionPoolMessages.obj}" styleClass="col-sm-2 form-control-label"/>
    <div class="col-sm-6">
        <h:inputText readonly="#{questionpool.importToAuthoring == 'true' || questionpool.owner!=questionpool.currentPool.owner}"  id="objfield" size="30" maxlength="255" value="#{questionpool.currentPool.objectives}" styleClass="form-control"/>
    </div>
</h:panelGroup>   
<h:panelGroup layout="block" styleClass="form-group row">
    <h:outputLabel for="keyfield" value="#{questionPoolMessages.keywords}" styleClass="col-sm-2 form-control-label"/>
    <div class="col-sm-6">
        <h:inputText readonly="#{questionpool.importToAuthoring == 'true' || questionpool.owner!=questionpool.currentPool.owner}"  id="keyfield" size="30" maxlength="255" value="#{questionpool.currentPool.keywords}" styleClass="form-control"/>
    </div>
</h:panelGroup>
<h:panelGroup rendered="#{questionpool.showTags && questionpool.canManageTags}" layout="block" styleClass="form-group row">
    <label for="tag-selector" class="col-sm-2 form-control-label">
        <h:outputText value="#{questionPoolMessages.t_tags}" />
    </label>
    <div class="col-sm-6">
        <sakai-tag-selector
            id="tag-selector"
            class="b5 flex-grow-1"
            selected-temp="<h:outputText value='#{questionpool.currentPool.tags.tagIdsCsv}'/>"
            collection-id="<h:outputText value='#{questionpool.currentPool.ownerId}'/>"
            site-id="<h:outputText value='#{author.currentSiteId}'/>"
            add-new="true"
        ></sakai-tag-selector>
        <h:inputHidden id="questionPoolTags" value="#{questionpool.currentPool.tags.tagIdsCsv}" />
    </div>
</h:panelGroup>

  <h:inputHidden id="createdDate" value="#{questionpool.currentPool.dateCreated}">
    <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
  </h:inputHidden>

<!-- display subpools  -->
<div class="tier1">
<h4>
<h:panelGrid width="100%" columns="2" columnClasses="h3text,navList">
<h:panelGroup >
<h:outputText value="#{questionpool.currentPool.numberOfSubpools}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfSubpools > 1}" value=" #{questionPoolMessages.subps}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfSubpools == 1}" value=" #{questionPoolMessages.subp}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfSubpools == 0}" value=" #{questionPoolMessages.subps}"/>
</h:panelGroup>
<h:panelGroup>
<h:commandLink title="#{questionPoolMessages.t_addSubpool}" rendered="#{questionpool.importToAuthoring != 'true' && questionpool.canAddPools}" id="addlink" immediate="true" action="#{questionpool.addPool}">
  <h:outputText  id="add" value="#{questionPoolMessages.t_addSubpool}"/>
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
  <f:param name="outCome" value="editPool"/>
</h:commandLink>
<h:outputText rendered="#{questionpool.importToAuthoring != 'true'}" value=" #{questionPoolMessages.separator} " />
<h:commandLink title="#{questionPoolMessages.preview}" rendered="#{questionpool.importToAuthoring != 'true'}"  id="previewlink" immediate="true" action="#{questionpool.startPreviewPool}">
  <h:outputText id="previewq" value="#{questionPoolMessages.preview}"/>
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
</h:commandLink>
<!-- Export Pool -->
<h:outputText title="#{questionPoolMessages.t_exportPool}" rendered="#{questionpool.importToAuthoring != 'true'}" value=" #{questionPoolMessages.separator} " />
<h:commandLink title="#{questionPoolMessages.t_exportPool}" rendered="#{questionpool.importToAuthoring != 'true'}" action="#{questionpool.startExportPool}" >
  <h:outputText id="export" value="#{questionPoolMessages.t_exportPool}"/>
  <f:param name="action" value="exportPool" />
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
  <f:param name="outCome" value="editPool"/>
</h:commandLink>
</h:panelGroup>
</h:panelGrid>
</h4>
<div class="tier2">
<h:panelGrid rendered="#{questionpool.currentPool.numberOfSubpools > 0 }" width="100%" >
<h:panelGroup>
<%@ include file="/jsf/questionpool/subpoolsTreeTable.jsp" %>

</h:panelGroup>
</h:panelGrid>
</div>
 </div>
<!-- dispaly questions-->


 <div class="tier1">
 <h4>
<h:panelGrid width="100%" columns="2" columnClasses="h3text,navList">
<h:panelGroup >
<h:outputText value="#{questionpool.currentPool.numberOfQuestions}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfQuestions >1}" value=" #{questionPoolMessages.qs}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfQuestions ==1}" value=" #{questionPoolMessages.q}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfQuestions ==0}" value=" #{questionPoolMessages.qs}"/>
</h:panelGroup>
<h:commandLink title="#{questionPoolMessages.t_addQuestion}" rendered="#{questionpool.importToAuthoring != 'true' && questionpool.canAddQuestions}" id="addQlink" immediate="true" action="#{questionpool.selectQuestionType}">
  <h:outputText id="addq" value="#{questionPoolMessages.t_addQuestion}"/>
  <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
  <f:param name="outCome" value="editPool"/>
</h:commandLink>
</h:panelGrid>
</h4>
  <div class="tier2">
<h:panelGroup layout="block" rendered="#{questionpool.currentPool.numberOfQuestions > 0 }">
<%@ include file="/jsf/questionpool/questionTreeTable.jsp" %>
</h:panelGroup>
 </div>
</div>
<!-- END -->
<f:verbatim><br/></f:verbatim>
<h:panelGrid rendered="#{(questionpool.importToAuthoring == 'true') && (questionpool.currentPool.numberOfQuestions > 0)}" columnClasses="shorttext">  <h:panelGroup>
  <h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{questionpool.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>

  <h:panelGroup rendered="#{questionpool.currentPool.numberOfQuestions > 0}">

    <!-- for normal pool operations -->

    <!-- for importing questions from pool to authoring -->
    <!-- disable copy button once clicked.  show processing... -->
    <h:commandButton id="import" rendered="#{questionpool.importToAuthoring == 'true'}"
      action="#{questionpool.doit}" onclick="SPNR.disableControlsAndSpin(this, null);" value="#{questionPoolMessages.copy}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.ImportQuestionsToAuthoring" />
    </h:commandButton>

    <h:commandButton id="removeSubmit" rendered="#{questionpool.importToAuthoring == 'false' && questionpool.canDeleteQuestions}" 
      action="#{questionpool.doit}" value="#{commonMessages.remove_action}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.StartRemoveItemsListener" />
        <f:param name="outCome" value="editPool"/>
    </h:commandButton>

    <h:commandButton title="#{questionPoolMessages.t_copyQuestion}" rendered="#{questionpool.importToAuthoring != 'true' && questionpool.canCopyQuestions}"
      id="copySubmit" immediate="true" action="#{questionpool.startCopyQuestions}" value="#{questionPoolMessages.copy}" />

    <h:commandButton title="#{questionPoolMessages.t_moveQuestion}" rendered="#{questionpool.importToAuthoring != 'true' && questionpool.canMoveQuestions}"
      id="moveSubmit" immediate="true" action="#{questionpool.startMoveQuestions}" value="#{questionPoolMessages.move}" />

    <h:commandButton title="#{questionPoolMessages.exp_q}" disabled="#{questionpool.currentPool.numberOfQuestions == 0 }" rendered="#{questionpool.importToAuthoring != 'true'}"
      id="exportSubmit" immediate="true" action="#{questionpool.startExportQuestions}" value="#{questionPoolMessages.t_exportPool}">
        <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
        <f:param name="outCome" value="editPool"/>
    </h:commandButton>

 </h:panelGroup>

  <h:panelGroup styleClass="act">
    <h:commandButton styleClass="active" id="Update" rendered="#{questionpool.importToAuthoring == 'false' && questionpool.owner == questionpool.currentPool.owner}" action="#{questionpool.getOutcomeEdit}" value="#{questionPoolMessages.update}">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener" />
      <f:attribute name="addsource" value="editpoolattr"/>
    </h:commandButton>

    <h:commandButton value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" onclick="SPNR.disableControlsAndSpin(this, null);">
      <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener" />
      <f:attribute name="returnToParentPool" value="true"/>
    </h:commandButton>
  </h:panelGroup>

</h:form>
</div>
</body>
</html>
</f:view>

