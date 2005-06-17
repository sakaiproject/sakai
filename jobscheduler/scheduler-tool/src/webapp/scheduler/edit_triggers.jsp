<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>

<f:loadBundle basename="org.sakaiproject.tool.scheduler.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view_container title="#{msgs.title_trigger}">		
	  <h:form>
  	  <h:graphicImage value="/images/quartz.jpg" alt="Powered By Quartz"/>
  	  <sakai:tool_bar_message value="Currently editing triggers for job: #{schedulerTool.selectedJobDetailWrapper.jobDetail.name}"/>
  	  <sakai:tool_bar>
		   <sakai:tool_bar_item
		     action="create_trigger"
			   value="#{msgs.bar_create_trigger}" />
		   <sakai:tool_bar_item
  		   rendered="#{!empty schedulerTool.selectedJobDetailWrapper.triggerWrapperList}"
		     action="#{schedulerTool.processRefreshFilteredTriggers}"
			   value="#{msgs.bar_delete_triggers}" />
			 <sakai:tool_bar_item 
		     action="#{schedulerTool.processRefreshJobs}"
			   value="#{msgs.bar_return_jobs}" />
   	  </sakai:tool_bar>
   	  <sakai:view_content>
  	    <h:dataTable rendered="#{!empty schedulerTool.selectedJobDetailWrapper.triggerWrapperList}" value="#{schedulerTool.selectedJobDetailWrapper.triggerWrapperList}" var="wrapper" styleClass="chefFlatListViewTable" >  	    
  	      <h:column>
    	      <f:facet name="header">    	      
    	        <h:commandButton alt="SelectAll" image="/sakai-scheduler-tool/images/checkbox.gif" action="#{schedulerTool.processSelectAllTriggers}"/>    	        
    	      </f:facet>
    	      <h:selectBooleanCheckbox value="#{wrapper.isSelected}"/>
    	    </h:column>
  	      <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="Trigger Name"/>
    	      </f:facet>
   	        <h:outputText value="#{wrapper.trigger.name}"/>
    	    </h:column>  	      
    	    <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="Cron Expression"/>
    	      </f:facet>
   	        <h:outputText value="#{wrapper.trigger.cronExpression}"/>
    	    </h:column>  	      
        </h:dataTable>
		  </sakai:view_content>
  	</h:form>
	</sakai:view_container>
</f:view>