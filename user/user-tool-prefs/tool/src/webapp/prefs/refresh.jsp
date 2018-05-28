<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
	<sakai:view_container title="Preferences">
	<sakai:view_content>
		<h:form id="options_form">

				
				<sakai:tool_bar>
			  <sakai:tool_bar_item value="Refresh" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmRefresh}" value="Notifications" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="Customize Tabs" />
   	  	</sakai:tool_bar>
				
				<br>

				<t:div rendered="#{UserPrefsTool.refreshUpdated}">
					<jsp:include page="prefUpdatedMsg.jsp"/>	
				</t:div>
				
				<sakai:messages />
				
				<br><br>
				<h:outputText value="Automatic Refresh" style="font-size: 13px;font-weight: bold;font-family: verdana, arial, helvetica, sans-serif;"/>
				<br>
				<h:outputText value="Refresh" style="font-size: 12px;font-family: verdana, arial, helvetica, sans-serif;"/>
				<br>
				<h:selectOneRadio value="#{UserPrefsTool.selectedRefreshItem}" layout="pageDirection" style="font-size: 12px;font-family: verdana, arial, helvetica, sans-serif;">
    			<f:selectItem itemValue="2" itemLabel="Enable Refresh"/><br>
    			<f:selectItem itemValue="1" itemLabel="Disable Refresh- This option requires you to manually refresh a page to see new updates, such as chat messages."/>
  			</h:selectOneRadio>
  			<br>
  			<h:outputText value="Disabling refresh may not work with older browsers, and may log you out unexpectedly. If you disable automatic refresh and you are logged off after a minute of inactivity, 
  			you should log back in and re-enable automatic refresh." style="font-size: 12px;font-family: verdana, arial, helvetica, sans-serif;"/>
  				
				<br><br>
				<sakai:button_bar>
					<sakai:button_bar_item action="#{UserPrefsTool.processActionRefreshSave}" value="Update Preferences" />
					<sakai:button_bar_item action="#{UserPrefsTool.processActionRefreshCancel}" value="Cancel" />
				</sakai:button_bar>
				
				<%--h:commandButton action="#{UserPrefsTool.processActionRefreshSave}" value="Update Preferences"/>
				<h:commandButton action="#{UserPrefsTool.processActionRefreshCancel}" type="reset" value="Cancel"/--%>


					
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
