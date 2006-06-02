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
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
--%>
-->

  <f:view>
    <f:loadBundle
       basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
       var="msg"/>
 
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.mv_p"/></title>
                        <!-- stylesheet and script widgets -->
<script language="javascript" type="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>
      </head>
<body onload="collapseAllRowsForSelectList();flagRows();;<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
 <div class="portletBody">
<h:form id="movePool">
<h:messages styleClass="validation"/>
<h3>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{msg.mv_p}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{msg.mv_q}"/>
</h3>

<div class="tier1">
<h:outputText value="#{msg.sel_dest_move} "/>
<h:outputText rendered="#{questionpool.actionType == 'pool'}" value="#{questionpool.currentPool.displayName}"/>
<h:outputText rendered="#{questionpool.actionType == 'item'}" value="#{questionpool.currentItem.text}" escape="false"/>

</div>

<%--
<h:outputText styleClass="number" value="1"/>
<h:outputLabel rendered="#{questionpool.actionType == 'item'}" value="#{msg.mv_q_to}"/>
<h:outputLabel rendered="#{questionpool.actionType == 'pool'}" value="#{msg.mv_p_to}"/>
<br/><br/>
--%>


<div class="longtext tier2">
<%@ include file="/jsf/questionpool/movePoolTree.jsp" %>

<h:inputHidden id="selectedRadioBtn" value="#{questionpool.destPool}"/>

<%--
<br/>
<h:outputText styleClass="number" value="2"/>
<h:outputLabel value="#{msg.click_move}"/>
--%>

</div>

<p class="act">
  <h:commandButton accesskey="#{msg.a_move}" type="submit" immediate="true" id="poolSubmit" value="#{msg.move}"
    action="#{questionpool.movePool}" rendered="#{questionpool.actionType == 'pool'}" styleClass="active">
  </h:commandButton>

  <h:commandButton accesskey="#{msg.a_move}" type="submit" immediate="true" id="itemSubmit" value="#{msg.move}"
    action="#{questionpool.moveQuestion}" rendered="#{questionpool.actionType == 'item'}" styleClass="active">
  </h:commandButton>

  <h:commandButton id="cancel" accesskey="#{msg.a_cancel}" value="#{msg.cancel}" action="poolList"/>
</p>

</h:form>
</div>
</body>
</html>
</f:view>
