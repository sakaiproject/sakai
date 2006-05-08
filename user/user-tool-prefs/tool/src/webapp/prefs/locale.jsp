<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
	<sakai:view_content>
		<h:form id="locale_form">

				
				<%--h:outputText value="User ID: "/><h:inputText value="#{AdminPrefsTool.userId}" /--%>	
				<sakai:tool_bar>
			  <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmNoti}" value="Refresh" /--%>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionEdit}" value="#{msgs.prefs_tab_title}" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" />
 		    <sakai:tool_bar_item value="#{msgs.prefs_lang_title}" />
   	  	</sakai:tool_bar>
				
	
				<sakai:messages />
				
				<h3><h:outputText value="#{msgs.prefs_lang_title}" /></h3>

				<h:panelGroup rendered="#{UserPrefsTool.locUpdated}">
					<jsp:include page="prefUpdatedMsg.jsp"/>	
				</h:panelGroup>

				
				<p class="instruction"><h:outputText value="#{msgs.locale_msg}"/> <h:outputText value="#{UserPrefsTool.selectedLocaleName}"/></p>
				
    			 <h:selectOneListbox 
                      value="#{UserPrefsTool.selectedLocaleString}"
                      size="20">
				    <f:selectItems value="#{UserPrefsTool.prefLocales}" />
				 </h:selectOneListbox>
			    <div class="act">
				    <h:commandButton accesskey="s" id="submit" styleClass="active" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionLocSave}"></h:commandButton>
					<h:commandButton accesskey="x" id="cancel"  value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionLocCancel}"></h:commandButton>
			    </div>
		 </h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
