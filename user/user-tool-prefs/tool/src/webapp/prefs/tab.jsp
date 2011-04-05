<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<f:view>
<head>
  <script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">//</script>
  <script type="text/javascript" language="JavaScript" src="/library/js/fluid-latest/InfusionAll.js">//</script>
  <script type="text/javascript" language="JavaScript" src="/sakai-user-tool-prefs/js/prefs.js">//</script>
<script type="text/javascript">
function checkReloadTop() {
    check = jQuery('input[id$=reloadTop]').val();
    if (check == 'true' ) parent.location.reload();
}

jQuery(document).ready(function () {
    setTimeout('checkReloadTop();', 1500);
});

</script>
</head>
	<sakai:view_container title="#{msgs.prefs_title}">
	<sakai:view_content>
		<h:form id="prefs_form">
				<sakai:stylesheet path="/css/prefs.css"/>
				<sakai:tool_bar>
			  <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmEdit}" value="Refresh" /--%>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 1}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}"rendered="#{UserPrefsTool.language_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 1}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 2}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}"rendered="#{UserPrefsTool.language_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 2}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 3}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}"rendered="#{UserPrefsTool.language_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 3}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 4}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}"rendered="#{UserPrefsTool.language_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 4}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 5}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}"rendered="#{UserPrefsTool.language_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 5}" />
 		    
   	  	</sakai:tool_bar>

				<h3>
					<h:outputText value="#{msgs.prefs_tab_title}" />
					<h:panelGroup rendered="#{UserPrefsTool.tabUpdated}" style="margin:0 3em;font-weight:normal">
						<jsp:include page="prefUpdatedMsg.jsp"/>	
					</h:panelGroup>
				</h3>
				
				
				<sakai:messages />
<f:verbatim>
<div class="layoutReorderer-container fl-container-flex" id="layoutReorderer" style="margin:.5em 0">
<p>
<strong>Mouse instructions:</strong>
Drag and drop items with the mouse.
</p>
<p>
<strong>Keyboard instructions:</strong>Direction keys (arrow keys or i-j-k-m) move focus. CTRL + direction keys move the item.
</p>
<div class="columnSetup3 fluid-vertical-order">
<!-- invalid drag n drop message template -->
<p class="flc-reorderer-dropWarning layoutReorderer-dropWarning">
This element can not be placed here.  The element underneath is locked.
</p>
</f:verbatim>
                <f:verbatim>
			<!-- Column #1 -->
                        <div class="flc-reorderer-column col1">
                            <div class="colTitle layoutReorderer-locked"><h4>Favorite Sites (in top bar)</h4></div>

<div class="flc-reorderer-module layoutReorderer-module layoutReorderer-locked">
<div class="demoSelector-layoutReorderer layoutReorderer-module-dragbar">My Workspace</div></div>



		</f:verbatim>
		<t:dataList id="dt1" value="#{UserPrefsTool.prefTabItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim>
                   <div class="flc-reorderer-module layoutReorderer-module last-login"
			id="</f:verbatim>
                  <t:outputText value="#{item.value}"></t:outputText>
                 <f:verbatim>">
                      <div class="demoSelector-layoutReorderer layoutReorderer-module-dragbar">
		</f:verbatim>
                <t:outputText value="#{item.label}"></t:outputText>
                <f:verbatim></div></div></f:verbatim>
		</t:dataList>
                <f:verbatim></div></f:verbatim>

                <f:verbatim>
			<!-- Column #2 -->
                        <div class="flc-reorderer-column col2">
                            <div class="colTitle layoutReorderer-locked"><h4>Active Sites (your drawer)</h4></div>
		</f:verbatim>
		<t:dataList id="dt2" value="#{UserPrefsTool.prefDrawerItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim>
                   <div class="flc-reorderer-module layoutReorderer-module last-login"
			id="</f:verbatim>
                  <t:outputText value="#{item.value}"></t:outputText>
                 <f:verbatim>">
                      <div class="demoSelector-layoutReorderer layoutReorderer-module-dragbar">
		</f:verbatim>
                <t:outputText value="#{item.label}"></t:outputText>
                <f:verbatim></div></div></f:verbatim>
		</t:dataList>
                <f:verbatim></div></f:verbatim>

                <f:verbatim>
			<!-- Column #3 -->
                        <div class="flc-reorderer-column fl-container-flex25 fl-force-left col3">
                            <div class="colTitle layoutReorderer-locked"><h4>Archived Sites (not shown in drawer)</h4></div>
		</f:verbatim>
		<t:dataList id="dt3" value="#{UserPrefsTool.prefHiddenItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim>
                   <div class="flc-reorderer-module layoutReorderer-module last-login"
			id="</f:verbatim>
                  <t:outputText value="#{item.value}"></t:outputText>
                 <f:verbatim>">
                      <div class="demoSelector-layoutReorderer layoutReorderer-module-dragbar">
		</f:verbatim>
                <t:outputText value="#{item.label}"></t:outputText>
                <f:verbatim></div></div></f:verbatim>
		</t:dataList>
                <f:verbatim></div></f:verbatim>
                <f:verbatim></div></div>
<div style="float:none;clear:both;margin:2em 0">
</f:verbatim>

	 	<h:commandButton accesskey="s" id="prefAllSub" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionSaveOrder}"></h:commandButton>
		 <h:commandButton accesskey="x" id="cancel"  value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionCancel}" styleClass="formButton"></h:commandButton>
		<h:inputHidden id="prefTabString" value="#{UserPrefsTool.prefTabString}" />
		<h:inputHidden id="prefDrawerString" value="#{UserPrefsTool.prefDrawerString}" />
		<h:inputHidden id="prefHiddenString" value="#{UserPrefsTool.prefHiddenString}" />
		<h:inputHidden id="reloadTop" value="#{UserPrefsTool.reloadTop}" />
<f:verbatim>
<p>
When you save your tab preferences, the entire page will automatically refresh to update
the top navigation.
</p>
</div>
<script type="text/javascript">
   initlayoutReorderer();
</script>
</f:verbatim>
</h:form>

</sakai:view_content>                                                                                                                     
</sakai:view_container>

</f:view>
