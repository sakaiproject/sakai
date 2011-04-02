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

<script type="text/javascript">
function checkReloadTop() {
    check = jQuery('input[id$=reloadTop]').val();
    if (check == 'true' ) parent.location.reload();
}

jQuery(document).ready(function () {
    setTimeout('checkReloadTop();', 1500);
});


//var demo = demo || {};
(function ($, fluid) {
    //fluid.setLogging(true);
    initlayoutReorderer = function () {
        fluid.reorderLayout("#demo-layoutReorderer", {
            listeners: {
       	    	afterMove: function (args) {
    		    var ids = '';  
		    jQuery('.col1 .demo-last-login').each(function(idx, item) {  
			// alert(item.id);
                        if ( ids.length > 1 ) ids += ', ' ;
        	        ids += item.id ; 
    		    });
            	    jQuery('input[name$=prefTabString]').val(ids);
    		    var ids = '';  
		    jQuery('.col2 .demo-last-login').each(function(idx, item) {  
                        if ( ids.length > 1 ) ids += ', ' ;
        	        ids += item.id ; 
    		    });
            	    jQuery('input[name$=prefDrawerString]').val(ids);
    		    var ids = '';  
		    jQuery('.col3 .demo-last-login').each(function(idx, item) {  
                        if ( ids.length > 1 ) ids += ', ' ;
        	        ids += item.id ; 
    		    });
            	    jQuery('input[name$=prefHiddenString]').val(ids);
       	        }
	    },
            selectors: {
                lockedModules: ".colTitle"
            },
            styles: {
                defaultStyle: "demo-layoutReorderer-movable-default",
                selected: "demo-layoutReorderer-movable-selected",
                dragging: "demo-layoutReorderer-movable-dragging",
                mouseDrag: "demo-layoutReorderer-movable-mousedrag",
                dropMarker: "demo-layoutReorderer-dropMarker",
                avatar: "demo-layoutReorderer-avatar"
            },
            disableWrap: true
        });
    };
})(jQuery, fluid);

</script>
<style type="text/css">
.demo-layoutReorderer-container {
    width:95%;
    margin: 0 auto;
    background-color: #ccd3ff;
    overflow:auto;
}
 
.demo-layoutReorderer-container h2 {
    margin-top: 0px;
}

.demo-layoutReorderer-container:focus{
    outline: none; /* overrides fss-reset.css */
}

.demo-layoutReorderer-container ul li {
    padding-bottom:0.5em;
}

.demo-layoutReorderer-module,
.demo-layoutReorderer-locked  {
    border: 1px solid #808285;
    margin: 6px;
    background-color: #fff;
}

.demo-layoutReorderer-module-dragbar {
    /* dragbar is the bar on top of each module */
    cursor: move;
    height: 20px;
    background-color: #A5CFEF;
    background-position: right center;
    background-repeat: no-repeat;
    background-image: url("../images/move.png");
}

.demo-layoutReorderer-locked .demo-layoutReorderer-module-dragbar {
    /* Locked module */
    cursor: default;
    background-color: #fff;
    background-image: url("../images/locked.png");	
    background-position: right center;
}

/* 
 * demo-module-content is the box below the dragbar, which contains the
 * actual content of the module
 */
.demo-layoutReorderer-module-content {
    padding: 6px;
	width: 95%;
    overflow: auto;
}

.demo-layoutReorderer-module-content img {
    border: 1px solid #afafaf;
    float:left;
    margin-right: 10px;
    width: 30%;
}
	
.demo-layoutReorderer-movable-hover, .demo-layoutReorderer-movable-selected{
    outline: 2px solid #000;
}

.demo-layoutReorderer-list-emphasis {
    font-weight: bold;
}

/**
 * Interaction styling.
 */

.demo-layoutReorderer-avatar {
    background-color:#9fff9f;
    outline: 2px solid #000;
    opacity: 0.5;
    filter: alpha(opacity=50); /* ie opacity fix */
}
.demo-layoutReorderer-avatar .demo-layoutReorderer-module-dragbar, 
.demo-layoutReorderer-avatar .demo-layoutReorderer-module-content {
    /* hide contents of the module's avatar, while dragging. */
    visibility: hidden;
}

.demo-layoutReorderer-movable-dragging {
	margin: 6px 2px 6px 12px;
}

.demo-layoutReorderer-movable-mousedrag {
    /* hide the original module while being dragged. */
    display:none;
}

.demo-layoutReorderer-dropMarker {
    background-color: #00a651;
    height: 3px;
    padding: 0 0 0 0;
    margin-left: 1em;
    margin-right: 1em;
}

.demo-layoutReorderer-dropWarning {
    border: 3px solid black;
    background-color: #ffffff;
    background-image: url('../images/info.png');
    background-repeat: no-repeat;
    background-position: left center;
    display: none;
    padding: 5px 30px;
    z-index: 10;
    width:250px;
}

.col1{

    width:30%;
    float:left
}
.col2{
    width:30%;
    float:left
}

.col3{
    width:30%;
    float:left
}
</style>
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
<div class="demo-layoutReorderer-container fl-container-flex" id="demo-layoutReorderer" style="margin:2em">
<p>
<strong>Mouse instructions:</strong>
Drag and drop items with the mouse.
</p>
<p>
<strong>Keyboard instructions:</strong>Direction keys (arrow keys or i-j-k-m) move focus. CTRL + direction keys move the item.
</p>
<div class="columnSetup3 fluid-vertical-order">
<!-- invalid drag n drop message template -->
<p class="flc-reorderer-dropWarning demo-layoutReorderer-dropWarning">
This element can not be placed here.  The element underneath is locked.
</p>
</f:verbatim>
                <f:verbatim>
			<!-- Column #1 -->
                        <div class="flc-reorderer-column fl-container-flex25 fl-force-left col1">
                            <div class="colTitle">Favorite Sites (in top bar)</div>

<div class="flc-reorderer-module demo-layoutReorderer-module colTitle">
<div class="demoSelector-layoutReorderer demo-layoutReorderer-module-dragbar">My Workspace</div></div>



		</f:verbatim>
		<t:dataList id="dt1" value="#{UserPrefsTool.prefTabItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim>
                   <div class="flc-reorderer-module demo-layoutReorderer-module demo-last-login"
			id="</f:verbatim>
                  <t:outputText value="#{item.value}"></t:outputText>
                 <f:verbatim>">
                      <div class="demoSelector-layoutReorderer demo-layoutReorderer-module-dragbar">
		</f:verbatim>
                <t:outputText value="#{item.label}"></t:outputText>
                <f:verbatim></div></div></f:verbatim>
		</t:dataList>
                <f:verbatim></div></f:verbatim>

                <f:verbatim>
			<!-- Column #2 -->
                        <div class="flc-reorderer-column fl-container-flex25 fl-force-left col2">
                            <div class="colTitle">Active Sites (your drawer)</div>
		</f:verbatim>
		<t:dataList id="dt2" value="#{UserPrefsTool.prefDrawerItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim>
                   <div class="flc-reorderer-module demo-layoutReorderer-module demo-last-login"
			id="</f:verbatim>
                  <t:outputText value="#{item.value}"></t:outputText>
                 <f:verbatim>">
                      <div class="demoSelector-layoutReorderer demo-layoutReorderer-module-dragbar">
		</f:verbatim>
                <t:outputText value="#{item.label}"></t:outputText>
                <f:verbatim></div></div></f:verbatim>
		</t:dataList>
                <f:verbatim></div></f:verbatim>

                <f:verbatim>
			<!-- Column #3 -->
                        <div class="flc-reorderer-column fl-container-flex25 fl-force-left col3">
                            <div class="colTitle">Archived Sites (not shown in drawer)</div>
		</f:verbatim>
		<t:dataList id="dt3" value="#{UserPrefsTool.prefHiddenItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim>
                   <div class="flc-reorderer-module demo-layoutReorderer-module demo-last-login"
			id="</f:verbatim>
                  <t:outputText value="#{item.value}"></t:outputText>
                 <f:verbatim>">
                      <div class="demoSelector-layoutReorderer demo-layoutReorderer-module-dragbar">
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
