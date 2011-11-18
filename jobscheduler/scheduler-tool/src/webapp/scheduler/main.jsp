<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>
                                                                                                      
                 

<f:view>
	<sakai:view_container title="#{msgs.title_job}">		
	  <sakai:view_content>
    	<h:graphicImage value="/images/quartz.jpg" alt="#{msgs.powered_by} Quartz"/>
	    <h:form>  	  
  	    <sakai:tool_bar>
 			    <sakai:tool_bar_item
		        action="#{schedulerTool.schedulerManager.globalTriggerListener.processSelect}"
			      value="#{schedulerTool.schedulerManager.globalTriggerListener.isViewAllSelected ? msgs.select_today : msgs.select_all}"/>		  
		      <sakai:tool_bar_item
		        action="#{schedulerTool.processRefreshJobs}"
			      value="#{msgs.bar_jobs}"/>		   
			  <sakai:tool_bar_item
		     	action="runningJobs"
			   	value="#{msgs.running_jobs}"/>
			           		      
     	  </sakai:tool_bar>           
      	
  	    <sakai:panel_titled title="#{msgs.event_log}">
              <h:commandLink value="#{msgs.filterEvents}" immediate="true" action="filter"/>
              <h:commandLink value="#{msgs.clearFilters}" action="#{schedulerTool.processClearFilters}"
                            rendered="#{schedulerTool.eventPager.filterEnabled}"/>
              <sakai:pager totalItems="#{schedulerTool.eventPager.totalItems}"
                           firstItem="#{schedulerTool.eventPager.firstItem}"
                           pageSize="#{schedulerTool.eventPager.pageSize}"
                           valueChangeListener="#{schedulerTool.eventPager.handleValueChange}"
                           textItem="events"
                           accesskeys="true"
                           immediate="true"/>

  	      <h:dataTable
                    first="#{schedulerTool.eventPager.firstItem}"
                    rows="#{schedulerTool.eventPager.pageSize}"
                    value="#{schedulerTool.eventPager.events}" var="event" styleClass="listHier lines">
  	        <h:column>
    	        <f:facet name="header">
    	          <h:outputText value="#{msgs.job_name}"/>
    	        </f:facet>
   	          <h:outputText value="#{event.jobName}"/>
    	      </h:column>
  	        <h:column>
    	        <f:facet name="header">
    	          <h:outputText value="#{msgs.eventType}"/>
    	        </f:facet>
  	          <h:outputText value="#{event.eventType}"/>
  	        </h:column>            	      
  	        <h:column>
    	          <f:facet name="header">
    	          <h:outputText value="#{msgs.timestamp}"/>
    	        </f:facet>
  	          <h:outputText value="#{event.time}">
  	            <f:convertDateTime pattern="MM/dd/yy 'at' HH:mm:ss"/>
  	          </h:outputText>
  	        </h:column>            	      
            <h:column>
              <f:facet name="header">
                <h:outputText value="#{msgs.message}"/>
              </f:facet>
              <h:outputText value="#{event.message}"/>
            </h:column>
          </h:dataTable>
   	    </sakai:panel_titled>
   	  </h:form>
    </sakai:view_content>
	</sakai:view_container>
</f:view>