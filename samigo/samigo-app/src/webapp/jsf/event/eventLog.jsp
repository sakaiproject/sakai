<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html>
  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="EventLog"/></title>
      <script>
        var deletedText = '<h:outputText value="#{eventLogMessages.assessment_deleted}" />';
      </script>
      <script src="/samigo-app/js/eventInfo.js"></script>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>;initHelpValue('<h:outputText value="#{eventLogMessages.search_hint}"/>', 'eventLogId:filteredUser');">

<div class="portletBody container-fluid">
  <h:form id="eventLogId">
  <!-- HEADINGS -->
  <%@ include file="/jsf/event/eventLogHeadings.jsp" %>
  
  <h:messages rendered="#{! empty facesContext.maximumSeverity}" infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>

  <div class="page-header">
    <h1>
      <h:outputText value="#{eventLog.siteTitle} "/>
      <small>
        <h:outputText value="#{eventLogMessages.log}"/>
      </small>
    </h1>
  </div>

  <h:panelGroup layout="block" styleClass="pull-right">
    <h:commandButton action="eventLog" value="#{eventLogMessages.previous}" disabled="#{!eventLog.hasPreviousPage}" title="#{eventLogMessages.previous}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogPreviousPageListener" />
    </h:commandButton>
    <h:commandButton action="eventLog" value="#{eventLogMessages.next}" disabled="#{!eventLog.hasNextPage}" title="#{eventLogMessages.previous}">
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogNextPageListener" />
    </h:commandButton>
  </h:panelGroup>

 <div class="divContainer row">
   <div class="divLeft col-lg-6 col-md-4 col-sm-5 col-xs-12">
     <h:outputLabel value="#{eventLogMessages.filterBy}"  />
     <h:selectOneMenu value="#{eventLog.filteredAssessmentId}" id="assessmentTitle"
         required="true" onchange="document.forms[0].submit();">
        <f:selectItems value="#{eventLog.assessments}"/>
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
     </h:selectOneMenu>
   </div>
   <div class="divRight col-md-8 col-lg-6 col-sm-7 col-xs-12">
      <h:inputText id="IE_hidden" value="" disabled="true" style="display: none;" />
      <h:inputText id="filteredUser" value="#{eventLog.filteredUser}" size="30"
         onfocus="resetHelpValue('#{eventLogMessages.search_hint}', 'eventLogId:filteredUser')"
         onclick="resetHelpValue('#{eventLogMessages.search_hint}', 'eventLogId:filteredUser')"/>
      <h:outputText value="&#160;" escape="false" />
      <h:commandButton value="#{eventLogMessages.search}" type="submit" id="search" accesskey="#{eventLogMessages.a_search}">
         <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandButton>
      <h:commandButton value="#{eventLogMessages.clear}" type="submit" id="clear" accesskey="#{eventLogMessages.a_clear}">
         <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandButton>
   </div>
 </div>

 <div class="table-responsive">
 <h:dataTable styleClass="table table-striped" value="#{eventLog.eventLogDataList}" var="log">
  <!-- Assessment Title... -->
    <h:column>
    <f:facet name="header">
        <h:commandLink title="#{eventLogMessages.t_sortTitle}" action="eventLog">
        <h:outputText value="#{eventLogMessages.title}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="title" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortTitleAscending}" rendered="#{eventLog.sortType eq 'title' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortTitleDescending}" rendered="#{eventLog.sortType eq 'title' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
    </f:facet>

   <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
    <h:outputText value="#{log.title}"/>
    <h:outputText value="#{eventLogMessages.assessment_deleted}" rendered="#{eventLog.isDeleted(log.assessmentId)}"/>
   </h:panelGroup>
  </h:column>

  <!-- Assessment ID... -->
  <h:column>
    <f:facet name="header">
      <h:commandLink title="#{eventLogMessages.t_sortId}" action="eventLog">
        <h:outputText value="#{eventLogMessages.id}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="id" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortIdAscending}" rendered="#{eventLog.sortType eq 'id' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortIdDescending}" rendered="#{eventLog.sortType eq 'id' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
    </f:facet>
    <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
      <h:outputText value="#{log.assessmentIdStr}" />
    </h:panelGroup>
  </h:column>

	 <!-- UserID... -->
	 <h:column >
	  <f:facet name="header">
        <h:commandLink title="#{eventLogMessages.t_sortUser}" action="eventLog">
        <h:outputText value="#{eventLogMessages.user_id}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="userDisplay" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortUserAscending}" rendered="#{eventLog.sortType eq 'userDisplay' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortUserDescending}" rendered="#{eventLog.sortType eq 'userDisplay' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.userDisplay}"/>
     </h:panelGroup>
	</h:column>
	 <!-- Date Started... -->
	<h:column>
	  <f:facet name="header">
        <h:commandLink title="#{eventLogMessages.t_sortStartDate}" action="eventLog">
        <h:outputText value="#{eventLogMessages.date_startd}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="startDate" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortStartDateAscending}" rendered="#{eventLog.sortType eq 'startDate' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortStartDateDescending}" rendered="#{eventLog.sortType eq 'startDate' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.startDate}">
	    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
	  </h:outputText>
     </h:panelGroup>
	</h:column>
	<!-- Date Submitted... -->
	<h:column>
	  <f:facet name="header">
        <h:commandLink title="#{eventLogMessages.t_sortEndDate}" action="eventLog">
        <h:outputText value="#{eventLogMessages.date_submitted}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="endDate" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortEndDateAscending}" rendered="#{eventLog.sortType eq 'endDate' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortEndDateDescending}" rendered="#{eventLog.sortType eq 'endDate' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.endDate}">
	    <f:convertDateTime dateStyle="medium" timeStyle="short" timeZone="#{author.userTimeZone}" />
	  </h:outputText>
     </h:panelGroup>
	</h:column>

	<!-- Duration... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.duration}"/>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.eclipseTime}"/>
	  <h:outputText value=" "/>
	  <h:outputText value="#{eventLogMessages.minutes}" rendered="#{log.eclipseTime > 1}"/>
	   <h:outputText value="#{eventLogMessages.minute}" rendered="#{log.eclipseTime < 2}"/>
     </h:panelGroup>
	</h:column>

	<!-- Errors... -->
	<h:column>
	  <f:facet name="header">
        <h:commandLink title="#{eventLogMessages.t_sortErrorMsg}" action="eventLog">
        <h:outputText value="#{eventLogMessages.errors}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="errorMsg" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortErrorMsgAscending}" rendered="#{eventLog.sortType eq 'errorMsg' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortErrorMsgDescending}" rendered="#{eventLog.sortType eq 'errorMsg' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	  <h:outputText value="#{log.errorMsg}" rendered="#{!log.isNoErrors}"/>
	    <h:outputText value="#{log.errorMsg}" styleClass="prePopulateText" rendered="#{log.isNoErrors}"/>
     </h:panelGroup>
	</h:column>

	<!-- IP Address -->
	<h:column rendered="#{eventLog.enabledIpAddress}">
	  <f:facet name="header">
        <h:commandLink title="#{eventLogMessages.t_sortIP}" action="eventLog">
        <h:outputText value="#{eventLogMessages.ipAddress}"/>
        <f:param name="sortAscending" value="#{!eventLog.sortAscending}"/>
        <f:param name="sortBy" value="ipAddress" />
        <h:graphicImage alt="#{eventLogMessages.alt_sortIPAscending}" rendered="#{eventLog.sortType eq 'ipAddress' && eventLog.sortAscending}" url="/images/sortascending.gif"/>
        <h:graphicImage alt="#{eventLogMessages.alt_sortIPDescending}" rendered="#{eventLog.sortType eq 'ipAddress' && !eventLog.sortAscending}" url="/images/sortdescending.gif"/>
        <f:actionListener
             type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandLink>
	  </f:facet>

	 <h:panelGroup styleClass="#{eventLog.isDeleted(log.assessmentId) ? 'eventLogDeleted' : ''}">
	    <h:outputText value="#{log.ipAddress}" />
     </h:panelGroup>
	</h:column>

	</h:dataTable>

   <h:outputText rendered="#{empty eventLog.eventLogDataList && empty eventLog.filteredUser}" value="#{eventLogMessages.no_data}"/>
   <h:outputFormat rendered="#{empty eventLog.eventLogDataList && not empty eventLog.filteredUser}" value="#{eventLogMessages.no_data_search}">
      <f:param value="#{eventLog.filteredUser}"></f:param>
   </h:outputFormat>
</div>
</h:form>
<!-- end content -->
</div>
</body>
</html>
  </f:view>

