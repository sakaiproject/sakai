<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.tool.summarycalendar.bundle.Messages"/>
</jsp:useBean>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<sakai:script contextBase="/sakai-jsf-resource/" path="/inputColor/inputColor.js"/>
	<sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
	<sakai:script path="/summary-calendar/js/calendar-summary.js"/>

	<h3><h:outputText value="#{msgs.menu_prefs}"/></h3>
	<sakai:instruction_message value="#{msgs.instructions_preferences}" />
		
	<f:subview id="msg" rendered="#{PrefsBean.messageToBeDisplayed}">
		<h:message for="msg" infoClass="success" fatalClass="alertMessage" style="margin-top: 15px;" showDetail="true"/>
	</f:subview>		
	
	<h:form id="prefsForm">
	
		<%/* Calendar display */%>
		<h4><h:outputText value="#{msgs.prefs_section_display}"/></h4>
		<sakai:instruction_message value="#{msgs.instructions_display}" />
		<t:div styleClass="indnt1">
        	<t:panelGrid columns="2">
            	<t:outputLabel for="selectViewMode" value="#{msgs.prefs_view_mode}: "/>
                <t:selectOneMenu
                	id="selectViewMode"
                    immediate="false"
                    value="#{PrefsBean.selectedViewMode}">
                   	<f:selectItems value="#{PrefsBean.viewModes}"/>
				</t:selectOneMenu>
			</t:panelGrid>
		</t:div>
		
		
		<%/* Event priorities */%>
		<h4><h:outputText value="#{msgs.prefs_section_priority}"/></h4>
		<sakai:instruction_message value="#{msgs.instructions_priority}" />
		
		<%/* Event priorities: event types */%>
		<h5><h:outputText value="#{msgs.prefs_section_priority_eventypes}" styleClass="indnt1"/></h5>
		<t:div styleClass="indnt2">
			<h:panelGrid columns="2">
	        	<h:outputLabel for="highPriorityEvents" value="#{msgs.prefs_high_priority}: "/>
	            <h:selectManyListbox id="highPriorityEvents" size="200" style="height:50px; width: 200px;"
	            	value="#{PrefsBean.selectedHighPriorityEvents}">
	            	<f:selectItems value="#{PrefsBean.highPriorityEvents}"/>
	            </h:selectManyListbox>
	
				<h:outputText value=""/>
		        <h:panelGrid columns="2" style="width: 200px; text-align: center;">
	    	        <h:commandButton onclick="moveMediumToHigh(); return false;"
            	                    action="" immediate="true" value="#{msgs.prefs_move_up}" />
	            	<h:commandButton onclick="moveHighToMedium(); return false;"
                    	            action="" immediate="true" value="#{msgs.prefs_move_down}" />   
		        </h:panelGrid>
	            
				<h:outputLabel for="mediumPriorityEvents" value="#{msgs.prefs_medium_priority}: "/>	            
	            <h:selectManyListbox id="mediumPriorityEvents" size="200" style="height:50px; width: 200px;"
	            	value="#{PrefsBean.selectedMediumPriorityEvents}">
	            	<f:selectItems value="#{PrefsBean.mediumPriorityEvents}"/>
	            </h:selectManyListbox>
	
				<h:outputText value=""/>
		        <h:panelGrid columns="2" style="width: 200px; text-align: center;">
		            <h:commandButton onclick="moveLowToMedium(); return false;"
	                                action="" immediate="true" value="#{msgs.prefs_move_up}" />
		            <h:commandButton onclick="moveMediumToLow(); return false;"
	                                action="" immediate="true" value="#{msgs.prefs_move_down}" />
		        </h:panelGrid>
	            
	        	<h:outputLabel for="lowPriorityEvents" value="#{msgs.prefs_low_priority}: "/>	            
	            <h:selectManyListbox id="lowPriorityEvents" size="200" style="height:50px; width: 200px;"
	            	value="#{PrefsBean.selectedLowPriorityEvents}">
	            	<f:selectItems value="#{PrefsBean.lowPriorityEvents}"/>
	            </h:selectManyListbox>
	        </h:panelGrid>	  
		</t:div>
		
		<%/* Event priorities: colors */%>
		<h5><h:outputText value="#{msgs.prefs_section_priority_color}" styleClass="indnt1"/></h5>
		<t:div styleClass="indnt2">
			<t:panelGrid columns="2">
            	<t:outputLabel for="highPriorityColor" value="#{msgs.prefs_high_priority}: "/>
                <sakai:inputColor 
                	id="highPriorityColor"
                    value="#{PrefsBean.selectedHighPriorityColor}" />
				
				<t:outputLabel for="mediumPriorityColor" value="#{msgs.prefs_medium_priority}: "/>
                <sakai:inputColor 
                	id="mediumPriorityColor"
                    value="#{PrefsBean.selectedMediumPriorityColor}"/>
				
				<t:outputLabel for="lowPriorityColor" value="#{msgs.prefs_low_priority}: "/>
                <sakai:inputColor 
                	id="lowPriorityColor"
                    value="#{PrefsBean.selectedLowPriorityColor}"/>
			</t:panelGrid>
		</t:div>
		
		<f:verbatim>
        	<script type="text/javascript">javascript:prepForms();</script>
        </f:verbatim>
        
		<%/* BUTTONS */%><p>
		<t:div styleClass="act">
			<h:commandButton
				action="#{PrefsBean.update}"
				value="#{msgs.update}"				
				styleClass="active"
				onclick="highlightEvents()"
				immediate="true"
				/>        
			<h:commandButton
				action="#{PrefsBean.cancel}"
				value="#{msgs.cancel}"/>
		</t:div>       
	</h:form>
</sakai:view>
</f:view>
