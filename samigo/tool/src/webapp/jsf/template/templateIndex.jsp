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
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="msg"/>
   <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
  
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.index_title}"/></title>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
<div class="portletBody">
  <h:form id="templateCreateForm">
<h:messages styleClass="validation"/>
  <!-- HEADINGS -->
  <%@ include file="/jsf/template/templateHeadings.jsp" %>

<h3>
   <h:outputText value="#{msg.index_templates}"/>
 </h3>

<div class="tier1">
   <h4>
   <h:outputText value="#{msg.index_new}"  rendered="#{authorization.createTemplate}"/>
   </h4>
<div class="tier2">
  <h5>
   <h:outputText value="#{msg.index_create_new}"  rendered="#{authorization.createTemplate}"/>
  </h5>
    <div class="shorttext">
      <h:outputLabel for="newName" value="#{msg.index_templates_title}"  rendered="#{authorization.createTemplate}"/>
      <!--h:outputText value="#{msg.index_templates_title}" /-->

      <h:inputText id="tempName" value="#{template.newName}" size="60" rendered="#{authorization.createTemplate}"/>

      <h:commandButton accesskey="#{msg.a_create}" type="submit" id="Submit" value="#{msg.index_button_create}"
        rendered="#{authorization.createTemplate}"
        action="#{template.getOutcome}">
              <f:actionListener
                type="org.sakaiproject.tool.assessment.ui.listener.author.EditTemplateListener" />
       </h:commandButton>

  </div>
    </div>
    </div>
</h:form>

<br/><h:message for="tempName" styleClass="validate"/>

 <h:form id="templateListForm">
 <div class="tier1">
 <h4>
   <h:outputText value="#{msg.index_saved}"/>
 </h4>
 <div class="tier2">

  <!-- controller buttons for invisible pager control -->
  <!-- samigo:pagerButtonControl controlId="templates" formId="editOrRemoveTemplateForm" / -->
  <h:dataTable cellpadding="0" cellspacing="0" id="editDataTable" value="#{templateIndex.sortTemplateList}"
    var="templateListItem" styleClass="listHier">
    <h:column>
     <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{msg.t_sortTitle}" immediate="true"  action="template" rendered="#{templateIndex.templateOrderBy!='templateName'}">
          <f:param name="templateSortType" value="templateName"/>
          <f:param name="templateAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{msg.index_templates_title} " rendered="#{templateIndex.templateOrderBy!='templateName'}" />
        </h:commandLink>
          <h:outputText  value="#{msg.index_templates_title} " styleClass="currentSort" rendered="#{templateIndex.templateOrderBy=='templateName'}" />
          <h:commandLink title="#{msg.t_sortTitle}" immediate="true" action="template" rendered="#{templateIndex.templateOrderBy=='templateName' && templateIndex.templateAscending }">
           <f:param name="templateAscending" value="false" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
             <h:graphicImage alt="#{msg.alt_sortTitleDescending}" rendered="#{templateIndex.templateAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{msg.t_sortTitle}" immediate="true"  action="template"  rendered="#{templateIndex.templateOrderBy=='templateName'&& !templateIndex.templateAscending }">
           <f:param name="templateAscending" value="true" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{msg.alt_sortTitleAscending}" rendered="#{!templateIndex.templateAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
     </f:facet>
     <!--h:panelGrid columns="1"-->
       <!--h:panelGroup-->

      <h:panelGroup rendered="#{person.isAdmin || (authorization.editOwnTemplate && templateListItem.idString!='1')}">
        <h:commandLink title="#{msg.t_editTemplate}" id="editlink" action="editTemplate" immediate="true">
          <h:outputText value="#{templateListItem.templateName}" />

          <f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateLoadListener" />
          <f:param name="templateId" value="#{templateListItem.idString}"/>
        </h:commandLink> <f:verbatim><br/></f:verbatim>
      </h:panelGroup>

      <h:panelGroup rendered="#{!authorization.editOwnTemplate && templateListItem.idString!='1'}">
          <h:outputText value="#{templateListItem.templateName}" />
          <f:verbatim><br/></f:verbatim>
      </h:panelGroup>
      <!--/h:panelGroup-->

      <!--h:panelGroup-->
      <h:panelGroup rendered="#{authorization.deleteOwnTemplate}">
        <h:commandLink title="#{msg.t_removeTemplate}" id="deletelink" action="confirmDeleteTemplate" immediate="true"
            rendered="#{templateListItem.idString ne 1}">
          <h:outputText value="#{msg.index_button_remove}" styleClass="itemAction"/>
            <f:param name="templateId" value="#{templateListItem.idString}"/>
            <f:actionListener
                type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmDeleteTemplateListener" />
        </h:commandLink>
      </h:panelGroup>
      <!--/h:panelGroup-->
     <!--/h:panelGrid-->
    </h:column>

    <h:column >
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{msg.t_sortLastModified}" immediate="true" action="template"  rendered="#{templateIndex.templateOrderBy!='lastModified'}">
          <f:param name="templateSortType" value="lastModified"/>
          <f:param name="templateAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{msg.index_templates_modified} " rendered="#{templateIndex.templateOrderBy!='lastModified'}" />
        </h:commandLink>
        <h:outputText  value="#{msg.index_templates_modified} " styleClass="currentSort" rendered="#{templateIndex.templateOrderBy=='lastModified'}" />
        <h:commandLink title="#{msg.t_sortLastModified}" immediate="true" action="template" rendered="#{templateIndex.templateOrderBy=='lastModified' && templateIndex.templateAscending }">
           <f:param name="templateSortType" value="lastModified"/>
           <f:param name="templateAscending" value="false"/>
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{msg.alt_sortLastModifiedDescending}" rendered="#{templateIndex.templateAscending}" url="/images/sortascending.gif"/>
        </h:commandLink>
          <h:commandLink title="#{msg.t_sortLastModified}" immediate="true" action="template" rendered="#{templateIndex.templateOrderBy=='lastModified'&& !templateIndex.templateAscending }">
           <f:param name="templateSortType" value="lastModified"/>
           <f:param name="templateAscending" value="true" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{msg.alt_sortLastModifiedAscending}" rendered="#{!templateIndex.templateAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{templateListItem.modifiedDate}" rendered="#{person.isAdmin or (templateListItem.idString ne '1')}">
         <f:convertDateTime pattern="#{genMsg.output_date_picker}"/>
      </h:outputText>
    </h:column>

  </h:dataTable>
  <!-- invisible pager control -->
  <!-- samigo:pager controlId="templates" dataTableId="editDataTable"
    showpages="999" showLinks="false" styleClass="rtEven"
    selectedStyleClass="rtOdd"/ -->
 </div>
</div>
 </h:form>

 
<!-- end content -->
</div>
      </body>
    </html>
  </f:view>
