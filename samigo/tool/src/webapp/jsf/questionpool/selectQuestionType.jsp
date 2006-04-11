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
     var="qpmsg"/>
    <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.AuthorMessages"
     var="msg"/>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.item_display_author}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
<!-- FORM -->
<h:form>

<!-- CHANGE TYPE -->
 <div class="shorttext tier1">
  <h:outputText styleClass="number" value="1" />

<h:outputText value="#{qpmsg.sel_q_type}"/>
<h:selectOneMenu id="selType" value="#{itemauthor.itemType}" required="true">
  <f:selectItems value="#{itemConfig.itemTypeSelectList}" />
</h:selectOneMenu>
<h:message for="selType" styleClass="validate"/>
</div>
 <div class="shorttext tier1">
  <h:outputText styleClass="number" value="2" />
</div>

<h:outputText value="#{qpmsg.click_save}"/>
<p class="act">
<h:commandButton accesskey="#{msg.a_save}" type="submit"  action="#{itemauthor.doit}" value="#{msg.button_save}" styleClass="active">
   <f:actionListener
           type="org.sakaiproject.tool.assessment.ui.listener.author.StartCreateItemListener" />
   <f:param name="poolId" value="#{questionpool.currentPool.id}"/>
</h:commandButton>

  <h:commandButton accesskey="#{msg.a_cancel}" type="button" id="Cancel" value="#{msg.button_cancel}" immediate="true"
    onclick="document.location='editPool.faces'"/>
</p>

</h:form>


<!-- end content -->
    </body>
  </html>
</f:view>
