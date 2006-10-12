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
* Licensed under the Educational Community License, Version 1.0 (the"License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.opensource.org/licenses/ecl1.php
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
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.QuestionPoolMessages"
     var="msg"/>
   
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.q_mgr}"/></title>
<script language="javascript" type="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>

      </head>
    <body onload="collapseAllRows();flagRows();;<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
<h:form id="questionpool">

<h:dataTable id="TreeTable" value="#{questionpool.testPools}"
    var="pool" rows="5" headerClass="altHeading2" rowClasses="trEven,trOdd">

    <h:column id="col1">

     <f:facet name="header">
       <h:outputText id="c1header" value="name"/>
     </f:facet>


<h:panelGroup styleClass="tier#{pool.level}"  id="firstcolumn">
<h:inputHidden id="rowid" value="#{pool.trid}"/>
<h:selectBooleanCheckbox id="checkboxes" value ="#{questionpoo.destPools}"/>
<h:outputLink id="togglelink"  onclick="toggleRows(this)" onkeypress="toggleRows(this)" value="#" styleClass="folder">
<h:graphicImage id="spacer_for_mozilla" style="border:0" width="17" value="../../images/delivery/spacer.gif" />
</h:outputLink>

<h:outputLink id="poolnamelink" value="editPool.faces?id=#{pool.name}">
<h:outputText id="poolnametext" value="#{pool.name}"/>
</h:outputLink>

</h:panelGroup>
    </h:column>

    <h:column id="col2">
     <f:facet name="header">
       <h:outputText id="c2header" value="creator"/>
     </f:facet>
     <h:panelGroup id="secondcolumn">
        <h:outputText value="#{pool.creator}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col3">
     <f:facet name="header">
       <h:outputText id="c3header" value="last modified"/>
     </f:facet>
     <h:panelGroup id="thirdcolumn">
        <h:outputText value="#{pool.lastModified}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col4">
     <f:facet name="header">
       <h:outputText value="# of questions"/>
     </f:facet>
     <h:panelGroup id="fourthcolumn">
        <h:outputText value="#{pool.noQuestions}"/>
     </h:panelGroup>
    </h:column>


    <h:column id="col5">
     <f:facet name="header">
       <h:outputText value="# of subpools"/>
     </f:facet>
     <h:panelGroup id="fifthcolumn">
        <h:outputText value="#{pool.nosubpools}"/>
     </h:panelGroup>
    </h:column>

    <h:column id="col6">
            <h:commandButton type="submit" id="addbutton" value="Add Subpool"
              action="addPool">
            </h:commandButton>
    </h:column>



  </h:dataTable>

  <samigo:pager dataTableId="TreeTable" showpages="2" styleClass="rtEven" selectedStyleClass="rtOdd"/>

</h:form>
      </body>
    </html>
  </f:view>
