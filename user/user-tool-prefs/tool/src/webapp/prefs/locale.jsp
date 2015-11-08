<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
	<sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>
		<h:form id="locale_form">
<h:outputText value="#{Portal.latestJQuery}" escape="false"/>
        <script type="text/javascript" src="/library/js/fluid/1.5/MyInfusion.js">//</script>
		<script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
		<script type="text/javascript">
			$(document).ready(function(){
				setupPrefsGen();
			});
		</script>

				
				<%--h:outputText value="User ID: "/><h:inputText value="#{AdminPrefsTool.userId}" /--%>	
				<sakai:tool_bar>
			  <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmNoti}" value="Refresh" /--%>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 1}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 1}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 1}" current="false" />
 		    <sakai:tool_bar_item value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 1}" current="true" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 1}" current="false" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 2}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 2}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 2}" current="false" />
  		    <sakai:tool_bar_item value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 2}" current="true" />
  		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 2}" current="false" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 3}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 3}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 3}" current="false" />
 		    <sakai:tool_bar_item value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 3}" current="true" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 3}" current="false" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 4}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 4}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 4}" current="false" />
 		    <sakai:tool_bar_item value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 4}" current="true" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 4}" current="false" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 5}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 5}" current="false" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 5}" current="false" />
 		    <sakai:tool_bar_item value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 5}" current="true" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 5}" current="false" />
 		    
   	  	</sakai:tool_bar>
				
	
				<sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />
				
				<h3>
					<h:outputText value="#{msgs.prefs_lang_title}" />
					<h:panelGroup rendered="#{UserPrefsTool.locUpdated}" style="margin:0 3em;font-weight:normal">
						<jsp:include page="prefUpdatedMsg.jsp"/>	
					</h:panelGroup>
				</h3>


				
				<p class="instruction"><h:outputText value="#{msgs.locale_msg}"/> <h:outputText value="#{UserPrefsTool.selectedLocaleName}"  styleClass="highlight" style="font-weight:bold !important;"/></p>
				
    			 <h:selectOneListbox 
                      value="#{UserPrefsTool.selectedLocaleString}"
                      size="20">
				    <f:selectItems value="#{UserPrefsTool.prefLocales}" />
				 </h:selectOneListbox>
			    <div class="act">
				    <h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionLocSave}"></h:commandButton>
					<h:commandButton accesskey="x" id="cancel"  styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionLocCancel}"></h:commandButton>
					<h:commandButton type="button"  styleClass="dummy blocked" value="#{msgs.update_pref}" style="display:none"></h:commandButton>
					<h:commandButton type="button"  styleClass="dummy blocked" value="#{msgs.cancel_pref}" style="display:none"></h:commandButton>
			    </div>
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
