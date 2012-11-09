<%@ page contentType="text/html;charset=utf-8" pageEncoding="utf-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://www.sakaiproject.org/samigo" prefix="samigo" %>
<!DOCTYPE html
     PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

  <f:view>
    <html xmlns="http://www.w3.org/1999/xhtml" lang="en" xml:lang="en">
      <head><%= request.getAttribute("html.head") %>
      <title><h:outputText value="EventLog"/></title>
      <script type="text/javascript" language="JavaScript" src="/library/js/jquery-1.1.2.js"></script>
      <script type="text/javascript">jQuery.noConflict();</script>
      <script type="text/javascript" language="JavaScript" src="/samigo-app/js/jquery-1.7.2.min.js"></script>
      <script type="text/javascript">var $j = jQuery.noConflict(true);</script>
      <script type="text/javascript" language="JavaScript" src="/samigo-app/js/eventInfo.js"></script>
      </head>
    <body onload="<%= request.getAttribute("html.body.onload") %>;initHelpValue('<h:outputText value="#{eventLogMessages.search_hint}"/>', 'eventLogId:filteredUser');">

<div class="portletBody">

  <h:form id="eventLogId">
<h:messages infoClass="validation" warnClass="validation" errorClass="validation" fatalClass="validation"/>
  <!-- HEADINGS -->
  <%@ include file="/jsf/event/eventLogHeadings.jsp" %>

<!-- content... -->
 <div align="center">
   <h3>
      <h:outputText value="#{eventLog.siteTitle}"/>
      <h:outputText value="#{eventLogMessages.log}"/>
   </h3>
 </div>

 <div align="right">
 	<h:panelGroup>
 	  <h:outputText   value="#{eventLogMessages.previous}"  rendered="#{!eventLog.hasPreviousPage}" />
	  <h:commandLink action="eventLog" value="#{eventLogMessages.previous}" rendered="#{eventLog.hasPreviousPage}" title="#{eventLogMessages.previous}">
		  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogPreviousPageListener" />
	  </h:commandLink>
	  <h:outputText escape="false" value="&nbsp;&nbsp;&nbsp;" />

	  <h:outputText   value="#{eventLogMessages.next}"  rendered="#{!eventLog.hasNextPage}" />
	  <h:commandLink action="eventLog" value="#{eventLogMessages.next}" rendered="#{eventLog.hasNextPage}" title="#{eventLogMessages.previous}">
		  <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogNextPageListener" />
	  </h:commandLink>
	  <h:outputText escape="false" value="&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" />

	</h:panelGroup>
 </div>

 <div class="divContainer">
   <span class="divLeft">
     <h:outputText   value="#{eventLogMessages.filterBy}"  />
     <h:selectOneMenu value="#{eventLog.filteredAssessmentId}" id="assessmentTitle"
         required="true" onchange="document.forms[0].submit();">
        <f:selectItems value="#{eventLog.assessments}"/>
        <f:valueChangeListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
     </h:selectOneMenu>
   </span>
   <span class="divRight">
      <h:inputText id="IE_hidden" value="" disabled="true" style="display: none;" />
      <h:inputText id="filteredUser" value="#{eventLog.filteredUser}" size="30"
         onfocus="resetHelpValue('#{eventLogMessages.search_hint}', 'eventLogId:filteredUser')"
         onclick="resetHelpValue('#{eventLogMessages.search_hint}', 'eventLogId:filteredUser')"/>

      <h:commandButton value="#{eventLogMessages.search}" type="submit" id="search" accesskey="#{eventLogMessages.a_search}">
         <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandButton>
      <h:commandButton value="#{eventLogMessages.clear}" type="submit" id="clear" accesskey="#{eventLogMessages.a_clear}">
         <f:actionListener type="org.sakaiproject.tool.assessment.ui.listener.author.EventLogListener" />
      </h:commandButton>
   </span>
 </div>


 <div>
 <h:dataTable cellpadding="0" cellspacing="0" styleClass="listHier listHierEventLog" value="#{eventLog.eventLogDataList}" var="log">
 	<!-- AssessmentID... -->
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

	 <h:panelGroup>
	  <h:outputText value="#{log.shortenedTitle}"/><h:outputText rendered="#{log.title != log.shortenedTitle}" value="#{eventLogMessages.dotdotdot}"/>
	  <f:verbatim><span class="info"></f:verbatim>
	  <h:graphicImage id="infoImg" url="/images/info_icon.gif" alt="" styleClass="infoDiv"/>
	   <h:panelGroup styleClass="makeLogInfo" style="display:none;z-index:2000;" >
				<h:outputText rendered="#{log.title != log.shortenedTitle}" value="#{log.title}"/>
            <h:outputText rendered="#{log.title != log.shortenedTitle}" value="<br>" escape="false"/>
            <h:outputFormat value="#{eventLogMessages.id}" >
               <f:param value="#{log.assessmentIdStr}" />
            </h:outputFormat>
		</h:panelGroup>
	  <f:verbatim></span></f:verbatim>

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

	 <h:panelGroup>
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

	 <h:panelGroup>
	  <h:outputText value="#{log.startDate}">
	  <f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
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

	 <h:panelGroup>
	  <h:outputText value="#{log.endDate}">
	  	<f:convertDateTime pattern="#{generalMessages.output_data_picker_w_sec}"/>
	  </h:outputText>
     </h:panelGroup>
	</h:column>

	<!-- Duration... -->
	<h:column>
	  <f:facet name="header">
        <h:outputText value="#{eventLogMessages.duration}"/>
	  </f:facet>

	 <h:panelGroup>
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

	 <h:panelGroup>
	  <h:outputText value="#{log.errorMsg}" rendered="#{!log.isNoErrors}"/>
	    <h:outputText value="#{log.errorMsg}" styleClass="prePopulateText" rendered="#{log.isNoErrors}"/>
     </h:panelGroup>
	</h:column>

	<!-- IP Address -->
	<h:column>
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

	 <h:panelGroup>
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

