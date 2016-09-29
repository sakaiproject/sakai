<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ include file="security_static_include.jsp"%>


<f:view>
	<sakai:view_container title="#{msgs.title_job}">
		<sakai:view_content>
			<h:form>
  	    <h:graphicImage value="/images/quartz.jpg"/>
				<sakai:group_box title="#{msgs.create_trigger}">				 
				  <sakai:panel_edit>
				  				  					  						
						<h:outputText value="#{msgs.trigger_name}"/>					  					  					  
					  <h:panelGroup>
  					  <h:inputText id="trigger_name" value="#{schedulerTool.triggerName}"
  					               maxlength="#{schedulerTool.triggerNameMaxLength}" size="40"
  					               required="true" validator="#{schedulerTool.validateTriggerName}">
  					    <f:validateLength maximum="#{schedulerTool.triggerNameMaxLength}"/>
  					  </h:inputText>
  					  <h:message for="trigger_name" styleClass="chefRequiredInline"/>
  					</h:panelGroup>  					
  					
  					<h:outputText value="#{msgs.trigger_expression}"/>
  					<h:panelGroup>
  					  <h:inputText id="trigger_expression" value="#{schedulerTool.triggerExpression}"
  					               required="true" validator="#{schedulerTool.validateTriggerExpression}"/>
  					  <h:message for="trigger_expression" styleClass="chefRequiredInline"/>
  					  <h:outputLink value="http://www.quartz-scheduler.org/documentation/quartz-2.1.x/tutorials/crontrigger.html" title="Quartz Cron Expression Support" target="_new">
  					    <h:outputText value="#{msgs.help}"/>  					    
  					  </h:outputLink>
  					</h:panelGroup>  					  					  					  											

  				</sakai:panel_edit>  				 				          
				</sakai:group_box>
									
				<sakai:button_bar>
					<sakai:button_bar_item
						action="#{schedulerTool.processCreateTrigger}"
						value="#{msgs.bar_post}" />					
					<sakai:button_bar_item immediate="true"
						action="edit_triggers"
						value="#{msgs.cancel}" />
				</sakai:button_bar>

			</h:form>
		</sakai:view_content>
	</sakai:view_container>
</f:view>
