<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<f:view>
<sakai:view_container title="#{msgs.prefs_title}">
<sakai:stylesheet path="/css/prefs.css"/>
<sakai:view_content>

<f:verbatim>
  <script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">//</script>
  <script type="text/javascript" language="JavaScript" src="/library/js/fluid/1.4/MyInfusion.js">//</script>
  <script type="text/javascript" language="JavaScript" src="/sakai-user-tool-prefs/js/prefs.js">//</script>
<script type="text/javascript">
<!--
function checkReloadTop() {
    check = jQuery('input[id$=reloadTop]').val();
    if (check == 'true' ) parent.location.reload();
}

jQuery(document).ready(function () {
    setTimeout('checkReloadTop();', 1500);
});
//-->
</script>
</f:verbatim>
		<h:form id="prefs_form">
				<sakai:tool_bar>
			  <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmEdit}" value="Refresh" /--%>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 1}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 1}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 1}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 2}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 2}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 3}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 3}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 4}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 4}" />
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 5}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 5}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 5}" />
 		    
   	  	</sakai:tool_bar>

				<h3>
					<h:outputText value="#{msgs.prefs_tab_title}" />
					<h:panelGroup rendered="#{UserPrefsTool.tabUpdated}" style="margin:0 3em;font-weight:normal">
						<jsp:include page="prefUpdatedMsg.jsp"/>	
					</h:panelGroup>
				</h3>
				
				
                          <sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />
<f:verbatim>
<div class="layoutReorderer-container fl-container-flex" id="layoutReorderer" style="margin:.5em 0">
<p>
<h:outputText value="#{msgs.prefs_mouse_instructions}" escape="false"/>
</p>
<p>
<h:outputText value="#{msgs.prefs_keyboard_instructions}" escape="false"/>
</p>
<div class="columnSetup3 fluid-vertical-order">
<!-- invalid drag n drop message template -->
<p class="flc-reorderer-dropWarning layoutReorderer-dropWarning">
<h:outputText value="#{msgs.prefs_element_locked}" />
</p>
</f:verbatim>
                <f:verbatim>
			<!-- Column #1 -->
                        <div class="flc-reorderer-column col1">
                            <div class="colTitle layoutReorderer-locked"><h4><h:outputText value="#{msgs.prefs_fav_sites}" /></h4></div>

<div class="flc-reorderer-module layoutReorderer-module layoutReorderer-locked">
<div class="demoSelector-layoutReorderer layoutReorderer-module-dragbar"><h:outputText value="#{msgs.prefs_my_workspace}" /></div></div>

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
                            <div class="colTitle layoutReorderer-locked"><h4><h:outputText value="#{msgs.prefs_active_sites}" /></h4></div>
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
                            <div class="colTitle layoutReorderer-locked"><h4><h:outputText value="#{msgs.prefs_archive_sites}" /></h4></div>
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
<h:outputText value="#{msgs.prefs_auto_refresh}" />
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
