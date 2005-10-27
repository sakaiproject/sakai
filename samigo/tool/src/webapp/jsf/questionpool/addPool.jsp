<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
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
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.add_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody">
<h3 class="insColor insBak insBor">
<h:outputText value="#{msg.add_p}"/>
</h3>

 <h:form id="questionpool">
<h:messages styleClass="validation"/>
<h:outputText value="#{msg.add_p_required}"/>

 <div class="shorttext indnt1">
  <h:outputLabel for="namefield" value="#{msg.p_name}*"/>
  <h:inputText id="namefield" size="30" value="#{questionpool.currentPool.displayName}" required="true"/>
<h:message for="namefield" styleClass="validate"/>
 </div>

 <div class="shorttext indnt1">
  <h:outputLabel for="ownerfield" value="#{msg.creator}"/>
  <h:outputText id="ownerfield" value="#{questionpool.currentPool.owner}"/>
 </div>

 <div class="shorttext indnt1">
  <h:outputLabel for="orgfield" value="#{msg.dept} "/>
  <h:inputText id="orgfield" size="30" value="#{questionpool.currentPool.organizationName}"/>
 </div>

 <div class="shorttext indnt1">
  <h:outputLabel for="descfield" value="#{msg.desc}"/>
  <h:inputTextarea id="descfield" value="#{questionpool.currentPool.description}" cols="30" rows="5"/>
 </div>

 <div class="shorttext indnt1">
  <h:outputLabel for="objfield" value="#{msg.obj} "/>
  <h:inputText id="objfield" size="30" value="#{questionpool.currentPool.objectives}"/>
 </div>

 <div class="shorttext indnt1">
  <h:outputLabel for="keyfield" value="#{msg.keywords} "/>
  <h:inputText id="keyfield" size="30" value="#{questionpool.currentPool.keywords}"/>
 </div>


<p class="act">
  <h:commandButton id="submit"  action="#{questionpool.doit}"
	value="#{msg.save}" styleClass="active">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener" />
  </h:commandButton>
<h:commandButton style="act" value="#{msg.cancel}" action="poolList" immediate="true"/>

</p>
 </h:form>
</div>
<!-- end content -->
      </body>
    </html>
  </f:view>



