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
      <title><h:outputText value="Move Pool"/></title>
                        <!-- stylesheet and script widgets -->
      <samigo:stylesheet path="/css/samigo.css"/>
      <samigo:stylesheet path="/css/sam.css"/>
      <samigo:stylesheet path="/css/nav.css"/>
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<body onload="collapseAllRowsForSelectList();flagRows();;<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->

<h:form id="movePool">
<h3>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{msg.mv_p}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{msg.mv_q}"/>
</h3>

<div class="indnt1">
<h:outputText value="#{msg.sel_dest_move} "/>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionpool.currentPool.displayName}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{questionpool.currentItem.text}"/>

</div>

<%--
<h:outputText styleClass="number" value="1"/>
<h:outputLabel rendered="#{questionpool.actionType == 'item'}" value="#{msg.mv_q_to}"/>
<h:outputLabel rendered="#{questionpool.actionType == 'pool'}" value="#{msg.mv_p_to}"/>
<br/><br/>
--%>


<div class="longtext indnt2">
<%@ include file="/jsf/questionpool/movePoolTree.jsp" %>

<h:inputHidden id="selectedRadioBtn" value="#{questionpool.destPool}"/>

<%--
<br/>
<h:outputText styleClass="number" value="2"/>
<h:outputLabel value="#{msg.click_move}"/>
--%>

</div>

<p class="act">
  <h:commandButton type="submit" immediate="true" id="poolSubmit" value="#{msg.move}"
    action="#{questionpool.movePool}" rendered="#{questionpool.actionType == 'pool'}" styleClass="active">
  </h:commandButton>

  <h:commandButton type="submit" immediate="true" id="itemSubmit" value="#{msg.move}"
    action="#{questionpool.moveQuestion}" rendered="#{questionpool.actionType == 'item'}" styleClass="active">
  </h:commandButton>

  <h:commandButton value="#{msg.cancel}" action="poolList"/>
</p>

</h:form>
</body>
</html>
</f:view>
