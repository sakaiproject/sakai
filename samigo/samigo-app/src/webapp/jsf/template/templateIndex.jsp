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
      <title><h:outputText value="#{templateMessages.index_title}"/></title>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
<div class="portletBody">
  <h:form id="templateCreateForm">
  <!-- HEADINGS -->
  <%@ include file="/jsf/template/templateHeadings.jsp" %>
  <h:messages styleClass="messageSamigo" rendered="#{! empty facesContext.maximumSeverity}" layout="table"/>

<h3>
   <h:outputText value="#{templateMessages.index_templates}"/>
 </h3>

<div class="tier1">
 <h:outputText value="#{templateMessages.index_desc}"  rendered="#{authorization.createTemplate}"/>

   <h4>
   <h:outputText value="#{templateMessages.index_new}"  rendered="#{authorization.createTemplate}"/>
   </h4>
   <h5>
    <h:outputText value="#{templateMessages.index_create_new}"  rendered="#{authorization.createTemplate}"/>
   </h5>
</div>

<div class="form-inline">
    <div class="form-group">
        <h:outputLabel for="tempName" value="#{templateMessages.index_templates_title}"  rendered="#{authorization.createTemplate}"/>
        <h:inputText id="tempName" value="#{template.newName}" size="60" maxlength="255" styleClass="form-control" />
    </div>
    <h:commandButton type="submit" id="Submit" value="#{templateMessages.index_button_create}" action="#{template.getOutcome}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EditTemplateListener" />
    </h:commandButton>
</div>


</h:form>

<br/><h:message for="tempName" styleClass="validate"/>

 <h:form id="templateListForm">
 <div class="tier1">
 <h4>
   <h:outputText value="#{templateMessages.index_saved}"/>
 </h4>
 <div class="tier2">

  <!-- controller buttons for invisible pager control -->
  <!-- samigo:pagerButtonControl controlId="templates" formId="editOrRemoveTemplateForm" / -->
  <div class="table-responsive">
  <h:dataTable cellpadding="0" cellspacing="0" id="editDataTable" value="#{templateIndex.sortTemplateList}"
    var="templateListItem" styleClass="table table-hover table-bordered table-striped">
    <h:column>
     <f:facet name="header">
       <h:panelGroup>
        <h:commandLink title="#{templateMessages.t_sortTitle}" immediate="true"  action="template" rendered="#{templateIndex.templateOrderBy!='templateName'}">
          <f:param name="templateSortType" value="templateName"/>
          <f:param name="templateAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{templateMessages.index_templates_title} "/>
        </h:commandLink>
 <h:commandLink title="#{templateMessages.t_sortTitle}" immediate="true" action="template" rendered="#{templateIndex.templateOrderBy=='templateName' && templateIndex.templateAscending }">
          <h:outputText  value="#{templateMessages.index_templates_title} " styleClass="currentSort" />
         
           <f:param name="templateAscending" value="false" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
             <h:graphicImage alt="#{templateMessages.alt_sortTitleDescending}" rendered="#{templateIndex.templateAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink title="#{templateMessages.t_sortTitle}" immediate="true"  action="template"  rendered="#{templateIndex.templateOrderBy=='templateName'&& !templateIndex.templateAscending }">
 <h:outputText  value="#{templateMessages.index_templates_title} " styleClass="currentSort" />
           <f:param name="templateAscending" value="true" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{templateMessages.alt_sortTitleAscending}" rendered="#{!templateIndex.templateAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
     </f:facet>
     <!--h:panelGrid columns="1"-->
       <!--h:panelGroup-->

      <h:panelGroup rendered="#{person.isAdmin || (authorization.editOwnTemplate && templateListItem.idString!='1')}">
        <h:commandLink title="#{templateMessages.t_editTemplate}" id="editlink" action="editTemplate" immediate="true">
          <h:outputText value="#{templateListItem.templateName}" escape="false"/>

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
        <h:commandLink title="#{templateMessages.t_removeTemplate}" id="deletelink" action="confirmDeleteTemplate" immediate="true"
            rendered="#{templateListItem.typeId ne '142' || (person.isAdmin && (templateListItem.typeId eq '142' && templateListItem.idString ne '1'))}">
          <h:outputText value="#{commonMessages.remove_action}" styleClass="itemAction"/>
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
        <h:commandLink title="#{templateMessages.t_sortLastModified}" immediate="true" action="template"  rendered="#{templateIndex.templateOrderBy!='lastModified'}">
          <f:param name="templateSortType" value="lastModified"/>
          <f:param name="templateAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{templateMessages.index_templates_modified} " />
        </h:commandLink>
<h:commandLink title="#{templateMessages.t_sortLastModified}" immediate="true" action="template" rendered="#{templateIndex.templateOrderBy=='lastModified' && templateIndex.templateAscending }">
        <h:outputText  value="#{templateMessages.index_templates_modified} " styleClass="currentSort"  />
        
           <f:param name="templateSortType" value="lastModified"/>
           <f:param name="templateAscending" value="false"/>
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{templateMessages.alt_sortLastModifiedDescending}" rendered="#{templateIndex.templateAscending}" url="/images/sortascending.gif"/>
        </h:commandLink>
          <h:commandLink title="#{templateMessages.t_sortLastModified}" immediate="true" action="template" rendered="#{templateIndex.templateOrderBy=='lastModified'&& !templateIndex.templateAscending }">
<h:outputText  value="#{templateMessages.index_templates_modified} " styleClass="currentSort"  />
           <f:param name="templateSortType" value="lastModified"/>
           <f:param name="templateAscending" value="true" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{templateMessages.alt_sortLastModifiedAscending}" rendered="#{!templateIndex.templateAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{templateListItem.modifiedDate}" rendered="#{person.isAdmin or (templateListItem.idString ne '1')}">
         <f:convertDateTime pattern="#{generalMessages.output_date_picker}"/>
      </h:outputText>
    </h:column>

  </h:dataTable></div>
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
