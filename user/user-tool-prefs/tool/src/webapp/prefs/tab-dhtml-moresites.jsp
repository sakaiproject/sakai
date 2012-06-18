<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:view>
    <sakai:view_container title="#{msgs.prefs_title}">
    <sakai:stylesheet path="/css/useDHTMLMore.css"/>
    <sakai:stylesheet path="/css/prefs.css"/>
    <sakai:view_content>
<f:verbatim> 
		<script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">//</script>
		<script type="text/javascript" language="JavaScript" src="/library/js/fluid-latest/InfusionAll.js">//</script> 
		<script type="text/javascript" language="JavaScript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
		<script type="text/javascript">
			<!--
			$(document).ready(function(){
				setupPrefsGen();
				setupPrefsTabs('sl','sr');				
			})  

                        function checkReloadTop() {
                            var name_element = document.getElementById('prefs_form:reloadTop');
                            check = name_element.value;
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
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 2}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 2}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 2}"/>
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 3}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 3}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 3}"/>
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 4}"/>
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" rendered="#{UserPrefsTool.tab_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 4}"/>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 4}"/>
 		    
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}" rendered="#{UserPrefsTool.privacyEnabled}" />
              
              </sakai:tool_bar>
                
                <h3>
                	<h:outputText value="#{msgs.prefs_tab_title}" />
	                <h:panelGroup rendered="#{UserPrefsTool.tabUpdated}"  style="margin:0 3em;font-weight:normal">
    	                <jsp:include page="prefUpdatedMsg.jsp"/>    
                    </h:panelGroup>
				</h3>
                
		    <sakai:messages rendered="#{!empty facesContext.maximumSeverity}" />
    <div id="tab-dhtml-more-sites">
    <div id="top-text">
        <h:outputText value="#{msgs.tab_inst_1_alt}"/> 
        <div>
            <h:outputText value="#{msgs.tab_inst_2_alt}"/>
        </div>
    </div>              

    <table cellspacing="0" cellpadding="5%" class="sidebyside">
        <tr>
            <td id="sites-active-button" class='buttoncell'>
  		 	   	<p style="margin:5px 0 0  0;"><h:commandLink id="moveUp" action="#{UserPrefsTool.processActionMoveUp}" title="#{msgs.tab_move_up}" styleClass="blockable ud"> <h:graphicImage value="prefs/up.png" alt="#{msgs.tab_move_up}" /> </h:commandLink></p>
		 	  	<p style="margin:0 0 5px 0;"><h:commandLink id="moveDown" action="#{UserPrefsTool.processActionMoveDown}" title="#{msgs.tab_move_down}" styleClass="blockable ud"> <h:graphicImage value="prefs/down.png" alt="#{msgs.tab_move_down}" /> </h:commandLink></p>
            </td>
            <td id="sites-active">
                <b><h:outputText value="#{msgs.tab_inst_3_alt}"/></b>
                <div class="instruction">
                    <h:outputFormat value="#{msgs.tab_inst_4_alt}">
                        <f:param value="#{UserPrefsTool.tabCount - 1 }"/>
                    </h:outputFormat> 
                </div>
				<b><h:outputLabel for="numtabs" value="#{msgs.tab_count}"/></b>
                <!-- SAK-18563 -->
                <h:selectOneMenu id="numtabs" value="#{UserPrefsTool.tabCount}" styleClass="notsbs">
                    <f:selectItems value="#{UserPrefsTool.tabsChoices}" />
                </h:selectOneMenu>
				<%--<h:inputText id="numtabs" size="2" value="#{UserPrefsTool.tabCount}" />--%>
                <br />
                <h:selectManyListbox value="#{UserPrefsTool.selectedOrderItems}" size="10" styleClass="sr">
                    <f:selectItems value="#{UserPrefsTool.prefOrderItems}" />
                </h:selectManyListbox>
            </td>
            <td class='buttoncell' style="text-align: center;">       
  						<div style="margin-bottom:1.5em">
						  <h:commandLink id="remove" action="#{UserPrefsTool.processActionAdd}" title="#{msgs.tab_move_inst}" styleClass="blockable br"><h:graphicImage value="prefs/to-left.png" alt="#{msgs.tab_move_inst_re}" /></h:commandLink>
						  <h:commandLink id="add" action="#{UserPrefsTool.processActionRemove}" title="#{msgs.tab_move_inst_re}" styleClass="blockable bl"><h:graphicImage value="prefs/to-right.png" alt="#{msgs.tab_move_inst}" /></h:commandLink>
						</div>
						<div>
							<h:commandLink id="removeAll" action="#{UserPrefsTool.processActionAddAll}" title="#{msgs.tab_move_all_inst}" styleClass="blockable br"><h:graphicImage value="prefs/all-to-left.png" alt="#{msgs.tab_move_all_inst_re}" /></h:commandLink>
							<h:commandLink id="addAll" action="#{UserPrefsTool.processActionRemoveAll}" title="#{msgs.tab_move_all_inst_re}" styleClass="blockable bl"><h:graphicImage value="prefs/all-to-right.png" alt="#{msgs.tab_move_all_inst}" /></h:commandLink>
						</div>	

            </td>
            <td id="sites-hidden">
                <b><h:outputText value="#{msgs.tab_inst_5_alt}"/>   </b>
    
                <div class="instruction">
                    <h:outputText value="#{msgs.tab_inst_6_alt}"/>  
                </div>
                <br />
          
                
                <h:selectManyListbox value="#{UserPrefsTool.selectedExcludeItems}" size="10" styleClass="sl">
                    <f:selectItems value="#{UserPrefsTool.prefExcludeItems}" />
                </h:selectManyListbox>
            </td>
        </tr>
    </table>
                <p class="act">
                    <h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionSave}"></h:commandButton>
                     <h:commandButton accesskey="x" id="cancel" styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionCancel}"></h:commandButton>
					<h:commandButton type="button" styleClass="dummy blocked" value="#{msgs.update_pref}"  style="display:none"/>
					<h:commandButton type="button" styleClass="dummy blocked" value="#{msgs.cancel_pref}"  style="display:none"/>

                </p>

                <h:inputHidden id="reloadTop" value="#{UserPrefsTool.reloadTop}" />
        </div>
        </h:form>
    
    </sakai:view_content>
    </sakai:view_container>
</f:view>
