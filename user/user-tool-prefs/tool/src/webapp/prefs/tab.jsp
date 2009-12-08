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
		<h:form id="prefs_form">
		<script type="text/javascript" language="JavaScript" src="/library/js/jquery.js">//</script>			
		<script type="text/javascript" language="JavaScript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
		<script type="text/javascript">
			$(document).ready(function(){
				setupPrefsGen();
				setupPrefsTabs('sl','sr');				
			})  
		</script>

				
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
				<p class="instruction"><h:outputText value="#{msgs.tab_inst_1}"/><br/><br/><h:outputText value="#{msgs.tab_inst_2}"/><br/><br/><h:outputText value="#{msgs.tab_inst_3}"/><br /><br /><h:outputText value="#{msgs.tab_inst_save}"/></p>
				<table cellspacing="0" cellpadding="5%" class="sidebyside" summary="layout" border="0">
    			  <tr>
    			    <td>
    			      <b><h:outputText value="#{msgs.tab_not_vis_inst}"/></b>
    			      <br />
    			  	  <h:selectManyListbox value="#{UserPrefsTool.selectedExcludeItems}" size="10" styleClass="sl">
				   		<f:selectItems value="#{UserPrefsTool.prefExcludeItems}" />
				 	  </h:selectManyListbox>
				 	</td>
				 	
				 	<td style="text-align: center;">
						<div style="margin-bottom:1.5em">
						  <h:commandLink id="remove" action="#{UserPrefsTool.processActionRemove}" title="#{msgs.tab_move_inst_re}" styleClass="blockable bl"><h:graphicImage value="prefs/to-left.png" alt="#{msgs.tab_move_inst_re}" /></h:commandLink>
						  <h:commandLink id="add" action="#{UserPrefsTool.processActionAdd}" title="#{msgs.tab_move_inst}" styleClass="blockable br"><h:graphicImage value="prefs/to-right.png" alt="#{msgs.tab_move_inst}" /></h:commandLink>
						</div>
						<div>
							<h:commandLink id="removeAll" action="#{UserPrefsTool.processActionRemoveAll}" title="#{msgs.tab_move_all_inst_re}" styleClass="blockable bl"><h:graphicImage value="prefs/all-to-left.png" alt="#{msgs.tab_move_all_inst_re}" /></h:commandLink>
							<h:commandLink id="addAll" action="#{UserPrefsTool.processActionAddAll}" title="#{msgs.tab_move_all_inst}" styleClass="blockable br"><h:graphicImage value="prefs/all-to-right.png" alt="#{msgs.tab_move_all_inst}" /></h:commandLink>
						</div>	
				 	</td>
				 	
				 	<td>
					  <b><h:outputText value="#{msgs.tab_vis_inst}"/></b>
    			      <br/>
				 	  <h:selectManyListbox value="#{UserPrefsTool.selectedOrderItems}" size="10" styleClass="sr">
				        <f:selectItems value="#{UserPrefsTool.prefOrderItems}" />
				      </h:selectManyListbox>
				 	</td>
				 	<td style="width:27px;">
				 	  <p style="margin:0"><h:commandLink id="moveTop" action="#{UserPrefsTool.processActionMoveTop}" title="#{msgs.tab_move_top}" styleClass="blockable ud"> <h:graphicImage value="prefs/to-top.png" alt="#{msgs.tab_move_top}" /> </h:commandLink></p>
				 	  <p style="margin:5px 0 0  0;"><h:commandLink id="moveUp" action="#{UserPrefsTool.processActionMoveUp}" title="#{msgs.tab_move_up}" styleClass="blockable ud"> <h:graphicImage value="prefs/up.png" alt="#{msgs.tab_move_up}" /> </h:commandLink></p>
					  <div style="margin-top:15px">
				 	  	<p style="margin:0 0 5px 0;"><h:commandLink id="moveDown" action="#{UserPrefsTool.processActionMoveDown}" title="#{msgs.tab_move_down}" styleClass="blockable ud"> <h:graphicImage value="prefs/down.png" alt="#{msgs.tab_move_down}" /> </h:commandLink></p>
						<p style="margin:0;"><h:commandLink id="moveBottom" action="#{UserPrefsTool.processActionMoveBottom}" title="#{msgs.tab_move_bottom}" styleClass="blockable ud"> <h:graphicImage value="prefs/to-bottom.png" alt="#{msgs.tab_move_bottom}" /> </h:commandLink></p>
						</div>	
				 	</td>    			  
    			  </tr>
				  <tr>
				  <td></td><td></td>
				  	<td>
						<h:panelGrid cellpadding="0" cellspacing="0">
						<h:panelGroup>
							 <h:outputLabel for="numtabs" style="font-weight:bold"  value="#{msgs.tab_count}"/><f:verbatim>&nbsp;&nbsp;</f:verbatim> <h:inputText size="2" id="numtabs" value="#{UserPrefsTool.tabCount}" />
					  </h:panelGroup>
					  </h:panelGrid>
					</td>
					<td></td>
					</tr>
				</table>
			    <p class="act" style="margin:0;padding:0">
				 	<h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionSave}"></h:commandButton>
					 <h:commandButton accesskey="x" id="cancel"  value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionCancel}" styleClass="formButton"></h:commandButton>
					<h:commandButton type="button" styleClass="dummy blocked" value="#{msgs.update_pref}"  style="display:none"/>
					<h:commandButton type="button" styleClass="dummy blocked" value="#{msgs.cancel_pref}"  style="display:none"/>
			    </p>
		 </h:form>
		 <%-- gsilver: this next bit renders an empty unbalanced div if there is no value --%>
		 <sakai:peer_refresh value="#{UserPrefsTool.refreshElement}" />

	</sakai:view_content>                                                                                                                     
	</sakai:view_container>
</f:view>
