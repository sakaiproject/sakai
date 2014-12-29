<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>


<f:view>
	<sakai:view_container title="#{msgs.title_job}">
		<sakai:view_content>
			<h:form>
  	    <h:graphicImage value="/images/quartz.jpg"/>
				<sakai:group_box title="#{msgs.run_job} #{schedulerTool.selectedJobDetailWrapper.jobDetail.name}">
				  <sakai:panel_edit>
				  		
                     <h:outputText value="#{msgs.already_running}" 
                           styleClass="validation" rendered="#{schedulerTool.selectedJobRunning == 1}" />
   
  				</sakai:panel_edit>
				  <sakai:panel_edit>
				  		
   
						<h:outputText value="#{msgs.confirm_running_job_now}"/>
  				</sakai:panel_edit>
				</sakai:group_box>
									
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{schedulerTool.processRunJobNow}"
						value="#{msgs.bar_run_now}" />
					<sakai:button_bar_item immediate="true"
						action="cancel"
						value="#{msgs.cancel}" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
