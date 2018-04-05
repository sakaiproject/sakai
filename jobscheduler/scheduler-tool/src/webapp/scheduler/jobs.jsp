<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>


<f:view>
	<sakai:view_container title="#{msgs.title_job}">		
	  <h:form>
		<div class="clear">
			<h:graphicImage value="/images/quartz.jpg" alt="#{msgs.powered_by} Quartz"/>
		</div>
  	  <sakai:tool_bar>
		   <sakai:tool_bar_item
		     action="create_job"
			   value="#{msgs.bar_create_job}"/>
		   <sakai:tool_bar_item
		     action="#{schedulerTool.processRefreshFilteredJobs}"
			   value="#{msgs.bar_delete_jobs}"/>
		   <sakai:tool_bar_item
		     action="main"
			   value="#{msgs.bar_event_log}"/>
		<sakai:tool_bar_item
		     action="runningJobs"
			   value="#{msgs.running_jobs}"/>
   	  </sakai:tool_bar>
   	  <br/>
   	  <sakai:view_content>
  	    <h:dataTable value="#{schedulerTool.jobDetailWrapperList}" var="job" styleClass="table table-hover table-striped table-bordered">
  	      <h:column>
    	      <f:facet name="header">    	      
    	        <h:commandButton alt="SelectAll" image="images/checkbox.gif" action="#{schedulerTool.processSelectAllJobs}"/>
    	      </f:facet>
    	      <h:selectBooleanCheckbox value="#{job.isSelected}"/>
    	    </h:column>
  	      <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.job_name}"/>
    	      </f:facet>
   	        <h:outputText value="#{job.jobDetail.name}"/>
    	    </h:column>
    	    <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.triggers}"/>
    	      </f:facet>
    	      <h:commandLink action="edit_triggers" actionListener="#{schedulerTool.editTriggersListener}" >
    	        <h:outputFormat value="#{msgs.triggers0}">
    	          <f:param value="#{job.triggerCount}"/>
    	        </h:outputFormat>
    	        <f:param name="jobName" value="#{job.jobDetail.name}"/>
    	      </h:commandLink>  	        
  	      </h:column>  	      
  	      <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.job_class}"/>
    	      </f:facet>
  	        <h:outputText value="#{job.jobType}"/>
  	      </h:column>            	      
        </h:dataTable>
		  </sakai:view_content>
  	</h:form>
	</sakai:view_container>
</f:view>
