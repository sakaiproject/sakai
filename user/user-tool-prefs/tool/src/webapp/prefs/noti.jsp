<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/prefs" prefix="prefs" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>



<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
    <sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>

<f:verbatim>
	<script type="text/javascript" src="/library/js/jquery.js"></script>
    <script type="text/javascript" language="JavaScript" src="/library/js/fluid-latest/InfusionAll.js">//</script>
	<script type="text/javascript">
	<!--
		function removeOverride(cur) {
			//set to true
			cur.nextSibling.value=true;

			//hide the row
			cur.parentNode.parentNode.style.display="none";
			return false;
		}
			//-->

	</script>
</f:verbatim>
	
		<h:form id="options_form">

				
		<script type="text/javascript" language="JavaScript" src="/library/js/jquery.js"></script>
		<script type="text/javascript" language="JavaScript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
		<script type="text/javascript">
			$(document).ready(function(){
				setupPrefsGen();
				fixImplicitLabeling();
			})  
		</script>
				<%--h:outputText value="User ID: "/><h:inputText value="#{AdminPrefsTool.userId}" /--%>	
				<sakai:tool_bar>
			  <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmNoti}" value="Refresh" /--%>
 		    <sakai:tool_bar_item value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 1}" />
 		    
 		    <sakai:tool_bar_item value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 2}" />
 		    
 		    <sakai:tool_bar_item value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 3}" />
 		    
 		    <sakai:tool_bar_item value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 4}" />
 		    
 		    <sakai:tool_bar_item value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 5}" />
 		    
 			</sakai:tool_bar>
				
				<h3>
					<h:outputText value="#{msgs.prefs_noti_title}" />
					<h:panelGroup rendered="#{UserPrefsTool.notiUpdated}"  style="margin:0 3em;font-weight:normal">
						<jsp:include page="prefUpdatedMsg.jsp"/>	
					</h:panelGroup>
				</h3>

	
				<sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />
				

<%--(gsilver) selectOneRadio renders a table but will not accept a summary attribute. Need mechanism to tell screen readers that the table is a layour table.	 --%>
				<p class="instruction"><h:outputText value="#{msgs.noti_inst_second}"/></p>

  			<h:dataTable id="typeOverride" value="#{UserPrefsTool.registeredNotificationItems}" var="decoItem" border="0">
      			<h:column rendered="#{!decoItem.hidden}">
      			   <prefs:prefsHideDivision id="decoItemDiv" title="#{decoItem.userNotificationPreferencesRegistration.sectionTitle}" 
      			      hideByDefault="#{!decoItem.expand}" key="#{decoItem.key}">
      			
      				<h:outputText value="#{decoItem.userNotificationPreferencesRegistration.sectionDescription}" styleClass="instruction indnt2" 
      				     rendered="#{not empty decoItem.userNotificationPreferencesRegistration.sectionDescription}"/>
					<h:selectOneRadio value="#{decoItem.selectedOption}" layout="pageDirection" styleClass="indnt2">
		    			<f:selectItems value="#{decoItem.optionSelectItems}"/>
  					</h:selectOneRadio>
      				<f:subview id="siteOverrideSub" rendered="#{decoItem.userNotificationPreferencesRegistration.overrideBySite}">
      					<f:verbatim><h4 class="indnt2"></f:verbatim><h:outputText value="#{decoItem.userNotificationPreferencesRegistration.sectionTitleOverride}"/>
      					<h:outputText value="&nbsp;&nbsp;" escape="false" />
      					<h:commandLink action="#{decoItem.processActionAddOverrides}" title="#{msgs.noti_add_site}">
      						<h:outputText value="#{msgs.noti_add_site}" escape="false" />
      					</h:commandLink>
      					<f:verbatim></h4></f:verbatim>
      					<h:dataTable id="siteOverride" value="#{decoItem.siteOverrides}" var="decoOverrideSite" border="0" styleClass="indnt3">
      						<h:column>
      							<h:outputText value="#{decoOverrideSite.siteTitle}"></h:outputText>
      						</h:column>
      						<h:column>
	      						<h:selectOneMenu id="optionSelect" value="#{decoOverrideSite.option}">
	      							<f:selectItems value="#{decoItem.optionSelectItems}"/>
	      						</h:selectOneMenu>
	      					</h:column>
	      					<h:column>
	      						<h:outputText value="&nbsp;" escape="false" />
	      						<h:graphicImage onclick="javascript:removeOverride(this);" id="deleteImage" 
	      							style="cursor:pointer"
	      							value="/../../library/image/silk/delete.png" />
	      						<t:inputHidden id="shouldRemove" forceId="true" value="#{decoOverrideSite.remove}" />
	      					</h:column>
	      				</h:dataTable>
      				</f:subview>
      				</prefs:prefsHideDivision>	
      			</h:column>
   			</h:dataTable>
  				
				<p class="act">
				<h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionNotiSave}"></h:commandButton>
				<h:commandButton accesskey="x" id="cancel" styleClass="formButton"  value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionNotiCancel}"></h:commandButton>
				<h:commandButton type="button"  styleClass="dummy blocked" value="#{msgs.update_pref}" style="display:none"></h:commandButton>
				<h:commandButton type="button"  styleClass="dummy blocked" value="#{msgs.cancel_pref}" style="display:none"></h:commandButton>
				</p>	
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
