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
	<sakai:view_content>
		<h:form id="prefs_form">
		<script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">//</script>			
		<script type="text/javascript" language="JavaScript" src="/library/js/fluid-latest/InfusionAll.js">//</script>			
		<script type="text/javascript" language="JavaScript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
<script type="text/javascript">
$(document).ready(function(){
    setupPrefsGen();
    setupPrefsTabs('sl','sr');				
    })  
</script>

<script type="text/javascript">
jQuery(document).ready(function () {
     var opts = {
	listeners: {
       	    afterMove: function (args) {
    		var ids = '';  
    		jQuery('#prefsList li').each(function(idx, item) {  
        		ids += item.id + ':/:' ; 
    		});
            	jQuery('input[id$=prefAllString]').val(ids);
        	},
	    }
    };

    fluid.reorderList("#prefs_form:dt1", opts);
});

function checkReloadTop() {
    check = jQuery('input[id$=reloadTop]').val();
    if (check == 'true' ) parent.location.reload();
}

jQuery(document).ready(function () {
    setTimeout('checkReloadTop();', 1500);
});
</script>
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
<p>Reorder tabs by dragging and dropping them into the sections and order you prefer.
When you save your tab preferences, the entire page will automatically refresh to update
the top navigation.</p>
</f:verbatim>
                <f:verbatim><ul id="prefsList" style="list-style-type: none"></f:verbatim>
		<t:dataList id="dt1" value="#{UserPrefsTool.prefAllItems}" 
			var="item" layout="simple" 
			rowIndexVar="counter"
			itemStyleClass="dataListStyle">
                <f:verbatim><li class="flc-reorderer-movable" style="padding: 3px;" id="</f:verbatim>
                        <t:outputText value="#{item.value}"></t:outputText>
                <f:verbatim>"></f:verbatim>
                        <t:outputText value="#{item.label}"></t:outputText>
                <f:verbatim></li></f:verbatim>
		</t:dataList>
                <f:verbatim></ul></f:verbatim>
	 	<h:commandButton accesskey="s" id="prefAllSub" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionSaveOrder}"></h:commandButton>
		 <h:commandButton accesskey="x" id="cancel"  value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionCancel}" styleClass="formButton"></h:commandButton>
		<h:inputHidden id="prefAllString" value="#{UserPrefsTool.prefAllString}" />
		<h:inputHidden id="reloadTop" value="#{UserPrefsTool.reloadTop}" />
<f:verbatim>
<p>The tabs in the drawer are not maintained in a particular order within the drawer.
The top tabs will be shown in the order you specify.</p>
</f:verbatim>
					<h:commandButton type="button" styleClass="dummy blocked" value="#{msgs.update_pref}"  style="display:none"/>
					<h:commandButton type="button" styleClass="dummy blocked" value="#{msgs.cancel_pref}"  style="display:none"/>
			    </p>
		 </h:form>

	</sakai:view_content>                                                                                                                     
	</sakai:view_container>
</f:view>
