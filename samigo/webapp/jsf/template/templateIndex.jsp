<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
  <f:view>
  <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.TemplateMessages"
     var="msg"/>
   <f:loadBundle
     basename="org.sakaiproject.tool.assessment.bundle.GeneralMessages"
     var="genMsg"/>
    <f:verbatim><!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
    </f:verbatim>
    <html xmlns="http://www.w3.org/1999/xhtml">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="#{msg.index_title}"/></title>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>">

<!-- content... -->
 <!-- this should probably be a widget -->
 <!-- div class="heading"><h:outputText value="#{msg.index_heading}"/></div -->
  <h:form id="templateEditorForm">
   <p class="navIntraTool">
   <h:commandLink action="author" id="authorlink" immediate="true">
      <h:outputText value="#{msg.link_assessments}" />
       <f:actionListener
         type="org.sakaiproject.tool.assessment.ui.listener.author.AuthorActionListener" />
   </h:commandLink>
    <h:outputText value=" | " />
      <h:outputText value="#{msg.index_templates}" />
    <h:outputText value=" | " />
    <h:commandLink action="poolList" id="poolLink" immediate="true">
      <h:outputText value="#{msg.link_pool}" />
    </h:commandLink>
   </p>

 <f:verbatim><h3></f:verbatim>
   <h:outputText value="#{msg.index_templates}"/>
 <f:verbatim></h3></f:verbatim>

 <div class="indnt1">
 <f:verbatim><h4></f:verbatim>
   <h:outputText value="#{msg.index_new}"/>
 <f:verbatim></h4></f:verbatim>
<div class="indnt2">
 <f:verbatim><h5 class="plain"></f:verbatim>
   <h:outputText value="#{msg.index_create_new}"/>
 <f:verbatim></h5></f:verbatim>
      <div class="shorttext">
      <h:outputLabel for="newName" value="#{msg.index_templates_title}"/>
      <!--h:outputText value="#{msg.index_templates_title}" /-->

      <h:inputText id="tempName" value="#{template.newName}" size="60" required="true"/>

      <h:commandButton type="submit" id="Submit" value="#{msg.index_button_create}"
       action="newTemplate">
              <f:actionListener
                type="org.sakaiproject.tool.assessment.ui.listener.author.EditTemplateListener" />
       </h:commandButton>

<br/><h:message for="tempName" styleClass="validate"/>

  </div>
    </div>
    </div>
</h:form>
 <h:form>
 <div class="indnt1">
 <f:verbatim><h4></f:verbatim>
   <h:outputText value="#{msg.index_saved}"/>
 <f:verbatim></h4></f:verbatim>

 <div class="indnt2">

  <!-- controller buttons for invisible pager control -->
  <!-- samigo:pagerButtonControl controlId="templates" formId="editOrRemoveTemplateForm" / -->
  <h:dataTable id="editDataTable" value="#{templateIndex.sortTemplateList}"
    var="templateListItem" styleClass="listHier">
    <h:column>
     <f:facet name="header">
       <h:panelGroup>
        <h:commandLink immediate="true"  action="sort" rendered="#{templateIndex.templateOrderBy!='templateName'}">
          <f:param name="templateSortType" value="templateName"/>
          <f:param name="templateAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{msg.index_templates_title} " rendered="#{templateIndex.templateOrderBy!='templateName'}" />
        </h:commandLink>
          <h:outputText  value="#{msg.index_templates_title} " styleClass="currentSort" rendered="#{templateIndex.templateOrderBy=='templateName'}" />
          <h:commandLink immediate="true" action="sort" rendered="#{templateIndex.templateOrderBy=='templateName' && templateIndex.templateAscending }">
           <f:param name="templateAscending" value="false" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
             <h:graphicImage alt="#{msg.asc}" rendered="#{templateIndex.templateAscending}" url="/images/sortascending.gif"/>
          </h:commandLink>
          <h:commandLink immediate="true"  action="sort"  rendered="#{templateIndex.templateOrderBy=='templateName'&& !templateIndex.templateAscending }">
           <f:param name="templateAscending" value="true" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!templateIndex.templateAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
     </f:facet>
     <!--h:panelGrid columns="1"-->
       <!--h:panelGroup-->
        <h:commandLink id="editlink" action="editTemplate" immediate="true">
          <h:outputText value="#{templateListItem.templateName}" />

          <f:actionListener
              type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateLoadListener" />
          <f:param name="templateId" value="#{templateListItem.idString}"/>
        </h:commandLink> <f:verbatim><br/></f:verbatim>
       <!--/h:panelGroup-->
      <!--h:panelGroup-->
        <h:commandLink id="deletelink" action="confirmDeleteTemplate" immediate="true"
            rendered="#{templateListItem.idString ne 1}">
          <h:outputText value="#{msg.index_button_remove}" styleClass="itemAction"/>
            <f:param name="templateId" value="#{templateListItem.idString}"/>
            <f:actionListener
                type="org.sakaiproject.tool.assessment.ui.listener.author.ConfirmDeleteTemplateListener" />
        </h:commandLink>
      <!--/h:panelGroup-->
     <!--/h:panelGrid-->
    </h:column>

    <h:column>
      <f:facet name="header">
       <h:panelGroup>
        <h:commandLink  immediate="true" action="sort"  rendered="#{templateIndex.templateOrderBy!='lastModified'}">
          <f:param name="templateSortType" value="lastModified"/>
          <f:param name="templateAscending" value="true"/>
          <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
          <h:outputText value="#{msg.index_templates_modified} " rendered="#{templateIndex.templateOrderBy!='lastModified'}" />
        </h:commandLink>
        <h:outputText  value="#{msg.index_templates_modified} " styleClass="currentSort" rendered="#{templateIndex.templateOrderBy=='lastModified'}" />
        <h:commandLink immediate="true" action="sort" rendered="#{templateIndex.templateOrderBy=='lastModified' && templateIndex.templateAscending }">
           <f:param name="templateSortType" value="lastModified"/>
           <f:param name="templateAscending" value="false"/>
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{msg.asc}" rendered="#{templateIndex.templateAscending}" url="/images/sortascending.gif"/>
        </h:commandLink>
          <h:commandLink immediate="true" action="sort" rendered="#{templateIndex.templateOrderBy=='lastModified'&& !templateIndex.templateAscending }">
           <f:param name="templateSortType" value="lastModified"/>
           <f:param name="templateAscending" value="true" />
           <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.TemplateListener" />
           <h:graphicImage alt="#{msg.desc}" rendered="#{!templateIndex.templateAscending}" url="/images/sortdescending.gif"/>
          </h:commandLink>
         </h:panelGroup>
      </f:facet>
      <h:outputText value="#{templateListItem.modifiedDate}">
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

 <p align="center">&#160;</p>
 <p align="center">&#160;</p>
<!-- end content -->

      </body>
    </html>
  </f:view>
