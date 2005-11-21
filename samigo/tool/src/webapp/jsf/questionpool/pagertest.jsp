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
      <title><h:outputText value="#{msg.q_mgr}"/></title>
<script language="javascript" style="text/JavaScript">
<!--
<%@ include file="/js/samigotree.js" %>
//-->
</script>

      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
<h:form id="questionpool">

<h:dataTable id="TreeTable" value="#{questionpool.testPools}"
    var="pool" rows="3" headerClass="altHeading2" rowClasses="trEven,trOdd">

    <h:column id="col1">

     <f:facet name="header">
       <h:outputText id="c1header" value="name"/>
     </f:facet>


<h:panelGroup id="firstcolumn">
<h:inputHidden id="rowid" value="#{pool.trid}"/>
<h:selectBooleanCheckbox id="checkboxes" value ="#{questionpoo.destPools}"/>
<h:outputLink id="togglelink"  onclick="toggleRows(this)" value="#" styleClass="folder">
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

  <samigo:pager dataTableId="TreeTable" showpages="3" styleClass="rtEven" selectedStyleClass="rtOdd"/>

</h:form>
      </body>
    </html>
  </f:view>
