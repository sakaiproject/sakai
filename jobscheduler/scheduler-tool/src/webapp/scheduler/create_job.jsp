<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>
<f:view>
	<sakai:view_container title="#{msgs.title_job}">
		<sakai:view_content>
			<h:form>
  	    <h:graphicImage value="/images/quartz.jpg"/>
				<sakai:group_box title="#{msgs.create_job}">				 
				  <sakai:panel_edit>
				  				  					  						
						<h:outputText value="#{msgs.job_name}"/>					  					  
					  
					  <h:panelGroup>
  					  <h:inputText id="job_name" value="#{schedulerTool.jobName}"
  					               maxlength="#{schedulerTool.jobNameMaxLength}" size="40"
  					               required="true" validator="#{schedulerTool.validateJobName}">
  					    <f:validateLength maximum="#{schedulerTool.jobNameMaxLength}"/>
  					  </h:inputText>
  					  <h:message for="job_name" styleClass="chefRequiredInline"/>
  					</h:panelGroup>  					
  					  					  					
						<h:outputText value="#{msgs.job_type}"/>
  					<h:selectOneMenu value="#{schedulerTool.selectedClass}">
	   				  <f:selectItems value="#{schedulerTool.beanJobs}"/>
	   				  <f:selectItems value="#{schedulerTool.jobClasses}"/>
			  		</h:selectOneMenu>

  				</sakai:panel_edit>  				 				          
				</sakai:group_box>
									
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{schedulerTool.processCreateJob}"
						value="#{msgs.bar_post}" />					
					<sakai:button_bar_item immediate="true"
						action="jobs"
						value="#{msgs.cancel}" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
