<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
       var="msg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="copy pool"/></title>
                        <!-- stylesheet and script widgets -->
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<body onload="collapseAllRowsForSelectList();flagRows();;<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->

<h:form id="copyPool">

<h3>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{msg.copy_p}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{msg.copy_q}"/>
</h3>


<div class="indnt1">
<h:outputText value="#{msg.sel_dest_copy} "/>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionpool.currentPool.displayName}"/>
<h:outputText  rendered="#{questionpool.actionType == 'item'}" value="#{questionpool.currentItem.text}"/>

</div>
<div class="longtext indnt2">
<%--
<h:outputText styleClass="number" value="1"/>
<h:outputLabel rendered="#{questionpool.actionType == 'pool'}" value="#{msg.copy_p_to}"/>
<h:outputLabel rendered="#{questionpool.actionType == 'item'}" value="#{msg.copy_q_to}"/>

<br/><br/>
--%>

<%@ include file="/jsf/questionpool/copyPoolTree.jsp" %>

<%--
<br/>
<h:outputText styleClass="number" value="2"/>
<h:outputLabel value="#{msg.click_copy}"/>

--%>
</div>

<p class="act">

  <h:commandButton id="copypoolsubmit" immediate="true" value="#{msg.copy}"
    action="#{questionpool.copyPool}" styleClass="active" rendered="#{questionpool.actionType == 'pool'}">
  </h:commandButton>

  <h:commandButton id="copyitemsubmit" immediate="true" value="#{msg.copy}"
    action="#{questionpool.copyQuestion}" styleClass="active" rendered="#{questionpool.actionType == 'item'}">
  </h:commandButton>

<h:commandButton value="#{msg.cancel}" action="poolList"/>

</p>
</h:form>
</body>
</html>
</f:view>
