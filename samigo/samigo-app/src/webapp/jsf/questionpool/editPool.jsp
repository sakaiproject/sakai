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
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{questionPoolMessages.edit_p}"/></title>

<%@ include file="/js/delivery.js" %>

<script language="javascript" type="text/JavaScript">
<!--
var textcheckall="<h:outputText value="#{questionPoolMessages.t_checkAll}"/>";
var textuncheckall="<h:outputText value="#{questionPoolMessages.t_uncheckAll}"/>";
<%@ include file="/js/samigotree.js" %>
<%@ include file="/js/authoring.js" %>
//-->
</script>
      </head>

<f:verbatim><body onload="disableIt();checkUpdate();collapseRowsByLevel(</f:verbatim><h:outputText value="#{questionpool.htmlIdLevel}"/><f:verbatim>);flagRows();<%= request.getAttribute("html.body.onload") %>;"></f:verbatim>
  
 <div class="portletBody">
<h:form id="editform">
  <!-- HEADINGS -->
  <%@ include file="/jsf/questionpool/questionpoolHeadings.jsp" %>

<!-- dataLine here is not working -->
<br />
<samigo:dataLine value="#{questionpool.currentPool.parentPoolsArray}" var="parent"
   separator=" > " first="0" rows="100" >
  <h:column>
    <h:commandLink action="#{questionpool.editPool}"  immediate="true">
      <h:outputText value="#{parent.displayName}" escape="false"/>
      <f:param name="qpid" value="#{parent.questionPoolId}"/>
    </h:commandLink>
  </h:column>
</samigo:dataLine>

<h:outputText rendered="#{questionpool.currentPool.showParentPools}" value=" > " />
<h:outputText rendered="#{questionpool.currentPool.showParentPools}" value="#{questionpool.currentPool.displayName}"/>

<h3 class="insColor insBak insBor">
<h:outputText value="#{questionPoolMessages.qp}#{questionPoolMessages.column} #{questionpool.currentPool.displayName}"/>
</h3>
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
<h:outputText rendered="#{questionpool.importToAuthoring == 'true'}" value="#{questionPoolMessages.msg_imp_editpool}"/>
 <div class="tier2">
<h:panelGrid columns="2" columnClasses="shorttext">
  <h:outputLabel for="namefield" value="#{questionPoolMessages.p_name}"/>
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" onkeydown="inIt()" id="namefield" size="30" value="#{questionpool.currentPool.displayName}" />
  <h:outputLabel for="ownerfield" value="#{questionPoolMessages.creator}"/>
  <h:outputText id="ownerfield" value="#{questionpool.currentPool.owner}"/>

  <h:outputLabel rendered="!#{questionpool.currentPool.showParentPools}"  for="orgfield" value="#{questionPoolMessages.dept}"/>
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" onkeydown="inIt() "id="orgfield" size="30"  value="#{questionpool.currentPool.organizationName}" rendered="!#{questionpool.currentPool.showParentPools}"/>
    
  <h:outputLabel rendered="!#{questionpool.currentPool.showParentPools}" for="descfield" value="#{questionPoolMessages.desc}" />
  <h:inputTextarea disabled="#{questionpool.importToAuthoring == 'true'}" onkeydown="inIt()" id="descfield" rendered="!#{questionpool.currentPool.showParentPools}" value="#{questionpool.currentPool.description}" cols="30" rows="5"/>
  
  <h:outputLabel for="objfield" value="#{questionPoolMessages.obj} " rendered="!#{questionpool.currentPool.showParentPools}"/>
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" onkeydown="inIt()" id="objfield" size="30"  value="#{questionpool.currentPool.objectives}" rendered="!#{questionpool.currentPool.showParentPools}"/>
  
  <h:outputLabel for="keyfield" value="#{questionPoolMessages.keywords} " rendered="!#{questionpool.currentPool.showParentPools}" />
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" onkeydown="inIt()" id="keyfield" size="30" value="#{questionpool.currentPool.keywords}" rendered="!#{questionpool.currentPool.showParentPools}" />

  <h:inputHidden id="createdDate" value="#{questionpool.currentPool.dateCreated}">
  <f:convertDateTime pattern="yyyy-MM-dd HH:mm:ss"/>
  </h:inputHidden>
</h:panelGrid>
<div style="margin-top:1em;margin-bottom:2.5em">
  <h:commandButton accesskey="#{questionPoolMessages.a_update}" id="Update"   rendered="#{questionpool.importToAuthoring == 'false'}" action="#{questionpool.getOutcomeEdit}" value="#{questionPoolMessages.update}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener" />
  </h:commandButton>
</div>

 </div>


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
<h:commandLink title="#{questionPoolMessages.t_addSubpool}" rendered="#{questionpool.importToAuthoring != 'true'}" id="addlink" immediate="true" action="#{questionpool.addPool}">
  <h:outputText  id="add" value="#{questionPoolMessages.add}"/>
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
  <f:param name="addsource" value="editpool"/>
</h:commandLink>
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
<h:outputText rendered ="#{questionpool.currentPool.numberOfQuestions >1}"value=" #{questionPoolMessages.qs}"/>
<h:outputText rendered ="#{questionpool.currentPool.numberOfQuestions ==1}"value=" #{questionPoolMessages.q}"/>
<h:outputText rendered ="#{questionpool.currentPool.numberOfQuestions ==0}"value=" #{questionPoolMessages.qs}"/>
</h:panelGroup>
<h:commandLink title="#{questionPoolMessages.t_addQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}" id="addQlink" immediate="true" action="#{questionpool.selectQuestionType}">
  <h:outputText id="addq" value="#{questionPoolMessages.add}"/>
  <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
</h:commandLink>
</h:panelGrid>
</h4>
  <div class="tier2">
<div class="navIntraToolLink">
  <h:commandButton accesskey="#{questionPoolMessages.a_update}" id="removeSubmit"   rendered="#{questionpool.importToAuthoring == 'false'}" action="#{questionpool.removeQuestionsFromPool}" value="#{questionPoolMessages.remove}">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.StartRemoveItemsListener" />
  </h:commandButton>
 
 <h:outputText escape="false" value=" | " rendered="#{questionpool.importToAuthoring != 'true'}" />
 
 <h:commandButton title="#{questionPoolMessages.t_copyQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}" id="copySubmit" immediate="true" action="#{questionpool.startCopyQuestions}" value="#{questionPoolMessages.copy}">
 
 </h:commandButton>
 
 <h:outputText escape="false" value=" | " rendered="#{questionpool.importToAuthoring != 'true'}" />
 
 <h:commandButton title="#{questionPoolMessages.t_moveQuestion}" rendered="#{questionpool.importToAuthoring != 'true'}" id="moveSubmit" immediate="true" action="#{questionpool.startMoveQuestions}" value="#{questionPoolMessages.move}">
 </h:commandButton>
 
 </div>

<h:panelGrid rendered="#{questionpool.currentPool.numberOfQuestions > 0 }" width="100%">
<h:panelGroup>
<%@ include file="/jsf/questionpool/questionTreeTable.jsp" %>
</h:panelGroup>
</h:panelGrid>
 </div>
</div>
<!-- END -->
<f:verbatim><br/></f:verbatim>
<h:panelGrid rendered="#{(questionpool.importToAuthoring == 'true') && (questionpool.currentPool.numberOfQuestions > 0)}" columnClasses="shorttext">  <h:panelGroup>
  <h:outputLabel value="#{authorMessages.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{questionpool.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <!-- use this in real  value="#{section.sectionNumberList}" -->
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>

<div class="tier1">
<!-- for normal pool operations -->

<!-- for importing questions from pool to authoring -->
<!-- disable copy button once clicked.  show processing... -->

  <h:commandButton accesskey="#{questionPoolMessages.a_copy}" id="import"   rendered="#{(questionpool.importToAuthoring == 'true') && (questionpool.currentPool.numberOfQuestions > 0)}" action="#{questionpool.doit}"
   onclick="disableImport(); showNotif('submitnotif',this.name,'editform');" onkeypress="disableImport(); showNotif('submitnotif',this.name,'editform');"
        value="#{questionPoolMessages.copy}">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.ImportQuestionsToAuthoring" />
  </h:commandButton>

  <h:commandButton accesskey="#{questionPoolMessages.a_cancel}" style="act" value="#{questionPoolMessages.cancel}" action="poolList" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.QuestionPoolListener" />
  </h:commandButton>

 <h:outputText escape="false" value="<span id=\"submitnotif\" style=\"visibility:hidden\"> #{deliveryMessages.processing}</span>"/>
 </div>

</h:form>
</div>
</body>
</html>
</f:view>

