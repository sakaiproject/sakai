<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>

  <f:view>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
       var="msg"/>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
       var="authmsg"/>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
       var="genMsg"/>

    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="Edit Pool"/></title>
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<f:verbatim><body onload="collapseRowsByLevel(</f:verbatim><h:outputText value="#{questionpool.htmlIdLevel}"/><f:verbatim>);flagRows();"></f:verbatim>

<h:form id="editform">

<h:messages />

<p class="navIntraTool" style="background-color:DDE3EB">

<h:commandLink action="author" id="authorlink" immediate="true">
  <h:outputText id="myassessment" value="#{msg.my_assessments}"/>
  <f:actionListener
    type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
</h:commandLink>


<h:outputText value=" | " />


    <h:commandLink action="template" immediate="true">
      <h:outputText value="#{msg.my_templates}" />
    </h:commandLink>
    <h:outputText value=" | " />
    <h:commandLink action="poolList" immediate="true">
      <h:outputText value="#{msg.qps}" />
    </h:commandLink>

</p>

<samigo:dataLine value="#{questionpool.currentPool.parentPoolsArray}" var="parent"
   separator=" > " first="0" rows="100" >
  <h:column>
    <h:commandLink action="#{questionpool.editPool}"  immediate="true">
      <h:outputText value="#{parent.displayName}" />
      <f:param name="qpid" value="#{parent.questionPoolId}"/>
    </h:commandLink>
  </h:column>
</samigo:dataLine>

<h:outputText rendered="#{questionpool.currentPool.showParentPools}" value=" > " />
<h:outputText rendered="#{questionpool.currentPool.showParentPools}" value="#{questionpool.currentPool.displayName}"/>

<h3 class="insColor insBak insBor">
<h:outputText value="#{msg.qp}: "/>
<h:outputText value="#{questionpool.currentPool.displayName}"/>
</h3>
<h:outputText rendered="#{questionpool.importToAuthoring == 'true'}" value="#{msg.msg_imp_editpool}"/>
 <div class="shorttext indnt2">
  <h:outputLabel for="namefield" value="#{msg.p_name}"/>
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}"  id="namefield" size="30" value="#{questionpool.currentPool.displayName}" required="true"/>
<h:message for="namefield" styleClass="validate"/>
 </div>
 <div class="shorttext indnt2">
  <h:outputLabel for="ownerfield" value="#{msg.creator}"/>
  <h:outputText id="ownerfield"  value="#{questionpool.currentPool.owner}"/>
 </div>

 <div class="shorttext indnt2">
  <h:outputLabel rendered="!#{questionpool.currentPool.showParentPools}"  for="orgfield" value="#{msg.dept}"/>
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" id="orgfield" size="30" value="#{questionpool.currentPool.organizationName}" rendered="!#{questionpool.currentPool.showParentPools}"/>
 </div>

 <div class="shorttext indnt2">
  <h:outputLabel rendered="!#{questionpool.currentPool.showParentPools}" for="descfield" value="#{msg.desc}" />
  <h:inputTextarea disabled="#{questionpool.importToAuthoring == 'true'}"  id="descfield" rendered="!#{questionpool.currentPool.showParentPools}" value="#{questionpool.currentPool.description}" cols="30" rows="5"/>
 </div>

 <div class="shorttext indnt2">
  <h:outputLabel for="objfield" value="#{msg.obj} " rendered="!#{questionpool.currentPool.showParentPools}"/>
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" id="objfield" size="30" value="#{questionpool.currentPool.objectives}" rendered="!#{questionpool.currentPool.showParentPools}"/>
 </div>

 <div class="shorttext indnt2">
  <h:outputLabel for="keyfield" value="#{msg.keywords} " rendered="!#{questionpool.currentPool.showParentPools}" />
  <h:inputText disabled="#{questionpool.importToAuthoring == 'true'}" id="keyfield" size="30" value="#{questionpool.currentPool.keywords}" rendered="!#{questionpool.currentPool.showParentPools}" />
 </div>


<!-- dispaly subpools  -->
<div class="indnt1">
<h4>
<h:panelGrid width="100%" columns="2" columnClasses="h3text,alignRight">
<h:panelGroup >
<h:outputText value="#{questionpool.currentPool.numberOfSubpools}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfSubpools > 1}" value=" #{msg.subps}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfSubpools == 1}" value=" #{msg.subp}"/>
<h:outputText rendered="#{questionpool.currentPool.numberOfSubpools == 0}" value=" #{msg.subps}"/>
</h:panelGroup>
<h:commandLink  rendered="#{questionpool.importToAuthoring != 'true'}" id="addlink" immediate="true" action="#{questionpool.addPool}">
  <h:outputText  id="add" value="#{msg.add}"/>
  <f:param name="qpid" value="#{questionpool.currentPool.id}"/>
  <f:param name="addsource" value="editpool"/>
</h:commandLink>
</h:panelGrid>
</h4>
<div class="indnt2">
<h:panelGrid rendered="#{questionpool.currentPool.numberOfSubpools > 0 }" width="100%" >
<h:panelGroup>
<%@ include file="/jsf/questionpool/subpoolsTreeTable.jsp" %>

</h:panelGroup>
</h:panelGrid>
</div>
 </div>
<!-- dispaly questions-->


 <div class="indnt1">
 <h4>
<h:panelGrid width="100%" columns="2" columnClasses="h3text,alignRight">
<h:panelGroup >
<h:outputText value="#{questionpool.currentPool.numberOfQuestions}"/>
<h:outputText rendered ="#{questionpool.currentPool.numberOfQuestions >1}"value=" #{msg.qs}"/>
<h:outputText rendered ="#{questionpool.currentPool.numberOfQuestions ==1}"value=" #{msg.q}"/>
<h:outputText rendered ="#{questionpool.currentPool.numberOfQuestions ==0}"value=" #{msg.qs}"/>
</h:panelGroup>
<h:commandLink rendered="#{questionpool.importToAuthoring != 'true'}" id="addQlink" immediate="true" action="#{questionpool.selectQuestionType}">
  <h:outputText id="addq" value="#{msg.add}"/>
  <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
</h:commandLink>
</h:panelGrid>
</h4>
  <div class="indnt2">
<h:panelGrid rendered="#{questionpool.currentPool.numberOfQuestions > 0 }" width="100%">
<h:panelGroup>
<%@ include file="/jsf/questionpool/questionTreeTable.jsp" %>
</h:panelGroup>
</h:panelGrid>
 </div>
</div>
<!-- END -->
<f:verbatim><br/></f:verbatim>
<h:panelGrid rendered="#{questionpool.importToAuthoring == 'true'}" columnClasses="shorttext">  <h:panelGroup>
  <h:outputLabel value="#{authmsg.assign_to_p}" />
  <h:selectOneMenu id="assignToPart" value="#{questionpool.selectedSection}">
     <f:selectItems  value="#{itemauthor.sectionSelectList}" />
     <!-- use this in real  value="#{section.sectionNumberList}" -->
  </h:selectOneMenu>
  </h:panelGroup>
  </h:panelGrid>

<div class="indnt1">
<!-- for normal pool operations -->
  <h:commandButton id="submit"   rendered="#{questionpool.importToAuthoring == 'false'}" action="#{questionpool.doit}"
        value="#{msg.update}">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener" />
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.StartRemoveItemsListener" />
  </h:commandButton>

<!-- for importing questions from pool to authoring -->

  <h:commandButton id="import"   rendered="#{questionpool.importToAuthoring == 'true'}" action="#{questionpool.doit}"
        value="#{msg.import}">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.ImportQuestionsToAuthoring" />
  </h:commandButton>



 <h:commandButton style="act" value="#{msg.cancel}" action="poolList" immediate="true"/>
 </div>

</h:form>
</body>
</html>
</f:view>

