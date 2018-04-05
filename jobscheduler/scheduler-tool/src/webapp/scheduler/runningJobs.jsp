<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>

<f:loadBundle basename="org.sakaiproject.tool.scheduler.bundle.Messages" var="msgs"/>

<f:view>
	<sakai:view_container title="#{msgs.title_job}">		
		<h:form>
			<div class="clear">
				<h:graphicImage value="/images/quartz.jpg" alt="#{msgs.powered_by} Quartz"/>
			</div>
  	  <sakai:tool_bar>
		   <sakai:tool_bar_item
		        action="#{schedulerTool.processRefreshJobs}"
			      value="#{msgs.bar_jobs}"/>
		   <sakai:tool_bar_item
		     action="main"
			   value="#{msgs.bar_event_log}"/>
	  
   	  </sakai:tool_bar>
   	  <sakai:view_content>
   	  	<h:messages warnClass="warningMessage" infoClass="information"/>
  	    <h:dataTable value="#{schedulerTool.runningJobs}" var="job" styleClass="table table-hover table-striped table-bordered">
  	      <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.job_name}"/>
    	      </f:facet>
   	        <h:outputText value="#{job.jec.jobDetail.name}"/>
    	    </h:column>
    	    <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.actions}"/>
    	      </f:facet>
    	      <h:commandLink action="#{job.processActionKill}"  
    	      	rendered="#{job.isKillable}">
    	        <h:outputText value="#{msgs.kill}"/>
    	      </h:commandLink>  	        
  	      </h:column>
  	      <h:column>
    	      <f:facet name="header">
    	        <h:outputText value="#{msgs.fire_time}"/>
    	      </f:facet>
   	        <h:outputText value="#{job.jec.fireTime}">
   	        	<f:convertDateTime pattern="MM/dd/yy 'at' HH:mm:ss"/>
   	        </h:outputText>
    	    </h:column>
        </h:dataTable>
		  </sakai:view_content>
  	</h:form>
	</sakai:view_container>
</f:view>
