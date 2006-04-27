<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
	<sakai:view_content>
		<h:form id="prefs_form">
				
				<sakai:tool_bar>
			  <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmEdit}" value="Refresh" /--%>
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" />
 		    <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" />
 		    <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" />
   	  	</sakai:tool_bar>

				<br />
				

				<h:panelGroup rendered="#{UserPrefsTool.tabUpdated}">
					<jsp:include page="prefUpdatedMsg.jsp"/>	

					<%--  (gsilver) there are 2 types of messages being rendered here with the same wrapper - errors and info (success) items 
	for the first the response should include a block element classed as "alertMessage" - if informational, "information", if success "success" --%>

	
					</h:panelGroup>
				
				<sakai:messages />
			
				<h3><h:outputText value="#{msgs.prefs_tab_title}" /></h3>
			
				<p class="instruction"><h:outputText value="#{msgs.tab_inst_1}"/><br><br><h:outputText value="#{msgs.tab_inst_2}"/></p>

				
	<%-- (gsilver) 2 issues 
	1.  if there are no sites to populate both selects a message should put in the response to the effect that there are no memberships, hence cannot move things onto tabs group or off it. The table and all its children should then be excluded  from the response.
		2. if a given select is empty (has no option children) the resultant xhtml is invalid - we may need to seed it if this is important. This is fairly standard practice and helps to provide a default width to an empty select item (ie: about 12 dashes)
--%>	

			   <table cellspacing="23" cellpadding="5%" class="sidebyside">
    			  <tr>
    			    <td>
    			      <b><h:outputText value="#{msgs.tab_not_vis_inst}"/></b>
    			      <br/>
    			  	  <h:selectManyListbox value="#{UserPrefsTool.selectedExcludeItems}" size="10">
				   		<f:selectItems value="#{UserPrefsTool.prefExcludeItems}" />
				 	  </h:selectManyListbox>
				 	</td>
				 	
				 	<td style="text-align: center;">
				 	  <p class="instruction"><h:outputText value="#{msgs.tab_move_inst}"/></p>
<%-- (gsilver)  collapsed all next 3 lines - as jsf seems to be creating wspace that destroys alignment  --%>
				 	  <h:commandButton id="add" value="#{msgs.tab_move_rone}" action="#{UserPrefsTool.processActionAdd}"></h:commandButton><br /><h:commandButton id="remove" value="#{msgs.tab_move_lone}" action="#{UserPrefsTool.processActionRemove}"></h:commandButton>
		         	  <p class="instruction">
		         	 <h:outputText value="#{msgs.tab_move_all_inst}"/>
		         	  </p>
<%-- (gsilver)  collapsed all next 3 lines - as jsf seems to be creating wspace that destroys alignment  --%>					  
		         	  <h:commandButton id="addAll" value="#{msgs.tab_move_rall}" action="#{UserPrefsTool.processActionAddAll}"></h:commandButton><br /><h:commandButton id="removeAll" value="#{msgs.tab_move_lall}" action="#{UserPrefsTool.processActionRemoveAll}"></h:commandButton>
				 	</td>
				 	
				 	<td>
				 	  <b><h:outputText value="#{msgs.tab_vis_inst}"/></b>
    			      <br/>
				 	  <h:selectManyListbox value="#{UserPrefsTool.selectedOrderItems}" size="10">
				        <f:selectItems value="#{UserPrefsTool.prefOrderItems}" />
				      </h:selectManyListbox>
				 	</td>
				 	
<%-- (gsilver) I think I understand the use of commandLink below instead of commandButton,  need up and down arrows,  we could use a commandButton that has a background image, but Safari will display nothing then, so this is the best possible solution --%>					
					
				 	<td>
				 	  <h:commandLink action="#{UserPrefsTool.processActionMoveUp}"> <h:graphicImage value="prefs/Up-Arrow.gif"/> </h:commandLink>
		              <br />
		              <h:commandLink action="#{UserPrefsTool.processActionMoveDown}"> <h:graphicImage value="prefs/Down-Arrow.gif"/> </h:commandLink>
				 	</td>    			  
    			  </tr>
				</table>
			    <br /><br />
			    <div class="act">
			    <h:commandButton id="submit" style="active;" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionSave}"></h:commandButton>
				 <h:commandButton id="cancel" style="active;" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionCancel}"></h:commandButton>
			    </div>

		 </h:form>
		 </div>
		 <sakai:peer_refresh value="#{UserPrefsTool.refreshElement}" />
	</sakai:view_content>
	</sakai:view_container>
</f:view>
