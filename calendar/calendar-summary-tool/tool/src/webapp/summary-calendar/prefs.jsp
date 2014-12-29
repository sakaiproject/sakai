<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai"%>
<% 
	response.setContentType("text/html; charset=UTF-8");
	response.addDateHeader("Expires", System.currentTimeMillis() - (1000L * 60L * 60L * 24L * 365L));
	response.addDateHeader("Last-Modified", System.currentTimeMillis());
	response.addHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0, post-check=0, pre-check=0");
	response.addHeader("Pragma", "no-cache");
%>

<% try{ %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="calendar"/>
</jsp:useBean>
<% }catch(Exception e) {return;} %>

<f:view>
<sakai:view title="#{msgs.tool_title}">
	<sakai:script contextBase="/jsf-resource/" path="/inputColor/inputColor.js"/>
	<sakai:script contextBase="/jsf-resource" path="/hideDivision/hideDivision.js"/>
	<sakai:script path="/summary-calendar/js/calendar-summary.js"/>
	
	<h:outputText value="#{PrefsBean.initValues}"/>
	
	<h3><h:outputText value="#{msgs.menu_prefs}"/></h3>
	<sakai:instruction_message value="#{msgs.instructions_preferences}" />
		
	<f:subview id="msg" rendered="#{PrefsBean.messageToBeDisplayed}">
		<h:message for="msg" infoClass="success" fatalClass="alertMessage" style="margin-top: 15px;" showDetail="true"/>
	</f:subview>		
	
	<h:form id="prefsForm">
	
		<%/* Calendar display */%>
		<h4><h:outputText value="#{msgs.prefs_section_display}"/></h4>
		<sakai:instruction_message value="#{msgs.instructions_display}" />
		<h:panelGrid styleClass="indnt2" columns="2">
            	<h:outputLabel for="selectViewMode" value="#{msgs.prefs_view_mode}: "/>
                <h:selectOneMenu
                	id="selectViewMode"
                    immediate="false"
                    value="#{PrefsBean.selectedViewMode}">
                   	<f:selectItems value="#{PrefsBean.viewModes}"/>
				</h:selectOneMenu>
		</h:panelGrid>
		
		
		<%/* Event priorities */%>
		<h4><h:outputText value="#{msgs.prefs_section_priority}"/></h4>
		<sakai:instruction_message value="#{msgs.instructions_priority}" />
		
		<%/* Event priorities: event types */%>
		<h5><h:outputText value="#{msgs.prefs_section_priority_eventypes}" styleClass="indnt1"/></h5>
		<h:panelGrid styleClass="indnt2" columns="2">
	        	<h:outputLabel for="highPriorityEvents" value="#{msgs.prefs_high_priority}: "/>
	            <h:selectManyListbox id="highPriorityEvents" size="200" style="height:50px; width: 200px;"
	            	value="#{PrefsBean.selectedHighPriorityEvents}">
	            	<%--<f:selectItems value="#{PrefsBean.highPriorityEvents}" var="mapEntry" itemValue="#{mapEntry.value}" itemLabel="#{mapEntry.key}"/>--%>
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
	            	<%--<f:selectItems value="#{PrefsBean.mediumPriorityEvents}" var="mapEntry" itemValue="#{mapEntry.value}" itemLabel="#{mapEntry.key}"/>--%>
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
	            	<%--<f:selectItems value="#{PrefsBean.lowPriorityEvents}" var="mapEntry" itemValue="#{mapEntry.value}" itemLabel="#{mapEntry.key}"/>--%>
	            	<f:selectItems value="#{PrefsBean.lowPriorityEvents}"/>
	            </h:selectManyListbox>
		</h:panelGrid>
		
		<%/* Event priorities: colors */%>
		<h5><h:outputText value="#{msgs.prefs_section_priority_color}" styleClass="indnt1"/></h5>
		<h:panelGrid styleClass="indnt2" columns="2">
            	<h:outputLabel for="highPriorityColor" value="#{msgs.prefs_high_priority}: "/>
                <sakai:inputColor 
                	id="highPriorityColor"
                    value="#{PrefsBean.selectedHighPriorityColor}" />
				
				<h:outputLabel for="mediumPriorityColor" value="#{msgs.prefs_medium_priority}: "/>
                <sakai:inputColor 
                	id="mediumPriorityColor"
                    value="#{PrefsBean.selectedMediumPriorityColor}"/>
				
				<h:outputLabel for="lowPriorityColor" value="#{msgs.prefs_low_priority}: "/>
                <sakai:inputColor 
                	id="lowPriorityColor"
                    value="#{PrefsBean.selectedLowPriorityColor}"/>
		</h:panelGrid>
		
		<f:verbatim>
        	<script type="text/javascript">javascript:prepForms();</script>
        </f:verbatim>
        
		<%/* BUTTONS */%><p>
		<h:panelGrid styleClass="act" columns="2">
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
		</h:panelGrid>       
	</h:form>
</sakai:view>
</f:view>
