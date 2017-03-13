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
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{questionPoolMessages.add_title}"/></title>
      </head>
      <body onload="<%= request.getAttribute("html.body.onload") %>">
<!-- content... -->
 <div class="portletBody container-fluid">

<script type="text/javascript">
function textCounter(field, maxlimit) {
	if (field.value.length > maxlimit) // if too long...trim it!
		field.value = field.value.substring(0, maxlimit);
}
</script>


 <h:form id="questionpool">
<h3 class="insColor insBak insBor">
<h:outputText value="#{questionPoolMessages.add_p}"/>
</h3>
<h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>
<p>
    <h:outputText value="#{questionPoolMessages.add_p_required}"/>
</p>

    <div class="form-group row">
        <h:outputLabel for="namefield" value="#{questionPoolMessages.p_name}#{questionPoolMessages.star}" styleClass="form-control-label col-md-2"/>
        <div class="col-md-6">
            <h:inputText id="namefield" maxlength="255" size="30" value="#{questionpool.currentPool.displayName}" styleClass="form-control"/>
        </div>
    </div>
    
    <div class="form-group row">
        <h:outputLabel for="ownerfield" value="#{questionPoolMessages.creator}" styleClass="form-control-label col-md-2"/>
        <div class="col-md-6">
            <h:outputText id="ownerfield" value="#{questionpool.currentPool.owner}"/>
        </div>
    </div>
    
    <div class="form-group row">
        <h:outputLabel for="orgfield" value="#{questionPoolMessages.dept} " styleClass="form-control-label col-md-2"/>
        <div class="col-md-6">
            <h:inputText id="orgfield" maxlength="255" size="30" value="#{questionpool.currentPool.organizationName}" styleClass="form-control"/>
        </div>
    </div>
    
    <div class="form-group row">
        <h:outputLabel for="descfield" value="#{questionPoolMessages.desc}" styleClass="form-control-label col-md-2"/>
        <div class="col-md-10">
            <h:inputTextarea id="descfield" value="#{questionpool.currentPool.description}" cols="40" rows="6" />
        </div>
    </div>

    <div class="form-group row">
        <h:outputLabel for="objfield" value="#{questionPoolMessages.obj} " styleClass="form-control-label col-md-2"/>
        <div class="col-md-6">
            <h:inputText id="objfield" maxlength="255" size="30" value="#{questionpool.currentPool.objectives}" styleClass="form-control"/>
        </div>
    </div>
    
    <div class="form-group row">
        <h:outputLabel for="keyfield" value="#{questionPoolMessages.keywords} " styleClass="form-control-label col-md-2"/>
        <div class="col-md-6">
            <h:inputText id="keyfield" maxlength="255" size="30" value="#{questionpool.currentPool.keywords}" styleClass="form-control"/>
        </div>
    </div>

<p class="act">
  <h:commandButton id="submit"  action="#{questionpool.doit}"
	value="#{questionPoolMessages.save}" styleClass="active">
  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.PoolSaveListener" />
  <f:attribute name="addsource" value="editpool"/>
  </h:commandButton>
  <h:commandButton style="act" value="#{commonMessages.cancel_action}" action="#{questionpool.cancelPool}" immediate="true">
    <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.questionpool.CancelPoolListener" />
    <f:attribute name="returnToParentPool" value="true"/>
  </h:commandButton>

</p>
 </h:form>
</div>
<!-- end content -->
      </body>
    </html>
  </f:view>



