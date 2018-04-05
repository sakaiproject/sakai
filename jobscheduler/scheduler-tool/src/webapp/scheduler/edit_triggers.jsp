<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>


<f:view>
	<sakai:view_container title="#{msgs.title_trigger}">		
	  <h:form>
  	  <h:graphicImage value="/images/quartz.jpg" alt="#{msgs.powered_by} Quartz"/>
  	  <sakai:tool_bar_message value="#{msgs.edit_trigger_for_job} #{schedulerTool.selectedJobDetailWrapper.jobDetail.name}"/>
  	  <sakai:tool_bar>
		   <sakai:tool_bar_item
		     action="create_trigger"
			   value="#{msgs.bar_create_trigger}" />
		   <sakai:tool_bar_item
  		   rendered="#{!empty schedulerTool.selectedJobDetailWrapper.triggerWrapperList}"
		     action="#{schedulerTool.processRefreshFilteredTriggers}"
			   value="#{msgs.bar_delete_triggers}" />
		   <sakai:tool_bar_item
		     action="#{schedulerTool.processPrepRunJobNow}"
			   value="#{msgs.bar_run_job_now}"  />
			 <sakai:tool_bar_item 
		     action="#{schedulerTool.processRefreshJobs}"
			   value="#{msgs.bar_return_jobs}" />
   	  </sakai:tool_bar>
   	  <br/>
   	  <sakai:view_content>
  	    <h:dataTable rendered="#{!empty schedulerTool.selectedJobDetailWrapper.triggerWrapperList}" value="#{schedulerTool.selectedJobDetailWrapper.triggerWrapperList}" var="wrapper" styleClass="table table-hover table-striped table-bordered">
  	      <h:column>
    	      <f:facet name="header">    	      
    	        <h:commandButton alt="SelectAll" image="/scheduler-tool/images/checkbox.gif" action="#{schedulerTool.processSelectAllTriggers}"/>    	        
    	      </f:facet>
    	      <h:selectBooleanCheckbox value="#{wrapper.isSelected}"/>
    	    </h:column>
  	      <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.trigger_name}"/>
    	      </f:facet>
   	        <h:outputText value="#{wrapper.trigger.name}"/>
    	    </h:column>  	      
    	    <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.trigger_expression}"/>
    	      </f:facet>
   	        <h:outputText rendered="#{wrapper.cron}" value="#{wrapper.trigger.cronExpression}"/>
    	    </h:column>
    	    <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.trigger_willrun}"/>
    	      </f:facet>
    	      <h:outputText value="#{wrapper.trigger.nextFireTime}">
    	        <f:convertDateTime type="both" dateStyle="short"/>
    	      </h:outputText>
    	    </h:column>  	      
        </h:dataTable>
		  </sakai:view_content>
  	</h:form>
	</sakai:view_container>
</f:view>
