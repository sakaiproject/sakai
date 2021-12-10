<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>



<f:view>
	<sakai:view_container title="#{msgs.title_trigger}">

	  <h:form id="filterForm">
  	    <h:graphicImage value="/images/quartz.jpg" alt="#{msgs.powered_by} Quartz"/>

        <sakai:panel_titled>
            <h1><h:outputText value="#{msgs.filter_title}"/></h1>

            <h:outputText value="#{msgs.filter_instructions}" styleClass="instructions"/>

            <h2>
                <h:outputText value="#{msgs.filter_date_title}"/>
            </h2>
            <h:outputText value="#{msgs.filter_date_instructions}" styleClass="instructions"/><br/>
            <h3>
                <label for="beforeFilter"><h:outputText value="#{msgs.filter_before_title}"/></label>
            </h3>
            <input type="datetime-local" name="before" id="beforeFilter"/>
            <h3>
                <label for="afterFilter"><h:outputText value="#{msgs.filter_after_title}"/></label>
            </h3>
            <input type="datetime-local" name="after" id="afterFilter"/>

            <h2>
                <h:outputText value="#{msgs.filter_job_title}"/>
            </h2>
            <h:outputText value="#{msgs.filter_job_instructions}" styleClass="instructions"/><br/>
            <h:selectManyListbox value="#{schedulerTool.eventPager.jobs}" size="8" styleClass="jobFilterSelect">
                <f:selectItems value="#{schedulerTool.scheduledJobs}"/>
            </h:selectManyListbox>

            <h2>
                <h:outputText value="#{msgs.filter_type_title}"/>
            </h2>
            <h:outputText value="#{msgs.filter_type_instructions}" styleClass="instructions"/><br/>
            <h:dataTable value="#{schedulerTool.eventPager.eventTypes}" var="eType">
                <h:column>
                    <h:selectBooleanCheckbox value="#{schedulerTool.eventPager.selectedEventTypes[eType]}"/>
                </h:column>
                <h:column>
                    <h:outputText value="#{eType}"/>
                </h:column>
            </h:dataTable>
        </sakai:panel_titled>

        <sakai:button_bar>
            <h:commandButton
                id="submitButton"
                action="#{schedulerTool.processSetFilters}"
                value="#{msgs.setFilters}" />
            <h:commandButton
                action="#{schedulerTool.processClearFilters}"
                value="#{msgs.clearFilters}" />
        </sakai:button_bar>

      </h:form>
    </sakai:view_container>
</f:view>
