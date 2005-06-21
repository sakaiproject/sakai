<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>


  <f:view>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
     var="msg"/>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.q_mgr}"/></title>
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<body onload="collapseAllRows();flagRows();;<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
<h:form id="questionpool">


<p class="navIntraTool" style="background-color:DDE3EB">

   <h:commandLink rendered="#{questionpool.importToAuthoring == 'true'}" action="author" immediate="true">
   <h:outputText value="#{msg.my_assessments}"/>
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelImportToAssessmentListener" />
   </h:commandLink>

   <h:commandLink rendered="#{questionpool.importToAuthoring == 'false'}" action="author"  immediate="true">
   <h:outputText value="#{msg.my_assessments}"/>
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>

<h:outputText value=" | " />

   <h:commandLink rendered="#{questionpool.importToAuthoring == 'false'}" action="template" immediate="true">
        <h:outputText value="#{msg.my_templates}"/>
   </h:commandLink>


   <h:commandLink rendered="#{questionpool.importToAuthoring == 'true'}" action="template" immediate="true">
        <h:outputText value="#{msg.my_templates}"/>
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelImportToAssessmentListener" />
   </h:commandLink>

<h:outputText value=" | " />
        <h:outputText value="#{msg.qps}"/>
</p>


 <h3><h:outputText value="#{msg.qps}"/></h3>

<h:outputText rendered="#{questionpool.importToAuthoring == 'true'}" value="#{msg.msg_imp_poolmanager}"/>

<div class="indnt1">
<h4 class="nav"><h:commandLink rendered="#{questionpool.importToAuthoring == 'false'}" id="add" immediate="true" action="#{questionpool.addPool}">
  <h:outputText value="#{msg.add_new_pool}"/>
  <f:param name="qpid" value="0"/>
</h:commandLink>
</h4>
 <div class="indnt2">
<%@ include file="/jsf/questionpool/poolTreeTable.jsp" %>

 </div>
</div>

<p class="act">
  <h:commandButton rendered="#{questionpool.importToAuthoring == 'false'}"  type="submit" immediate="true" id="Submit" value="#{msg.update}" action="#{questionpool.startRemovePool}" styleClass="active" >
  </h:commandButton>
  <h:commandButton rendered="#{questionpool.importToAuthoring == 'true'}"  type="submit" immediate="true" id="cancel" value="#{msg.cancel}" action="#{questionpool.cancelImport}"  >
  </h:commandButton>
</p>


</h:form>
      </body>
    </html>
  </f:view>
