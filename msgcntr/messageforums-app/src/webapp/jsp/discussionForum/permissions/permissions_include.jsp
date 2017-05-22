<%  
    /** initialize javascript from db **/
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{ForumTool}");
    DiscussionForumTool dft = (DiscussionForumTool) binding.getValue(context);
    out.print(dft.generatePermissionScript());
%>
<!--jsp/discussionForum/permissions/permissions_include.jsp-->
<mf:forumHideDivision title="#{msgs.cdfm_permissions}" id="cntrl_perm" hideByDefault="#{ForumTool.collapsePermissionPanel}">
  <%--
  <f:verbatim><p class="act"></f:verbatim>
    <h:commandButton immediate="true" action="#{ForumTool.processActionAddGroupsUsers}" value="#{msgs.cdfm_button_bar_add_groups_users}" rendered="#{ForumTool.editMode}"/> 
  <f:verbatim><p/></f:verbatim>
  --%>

<script type="text/javascript">
  setPanelId('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
</script>   
    <h:panelGroup styleClass="permissionRoleLabel">
		<h:outputLabel style="font-weight:bold;padding-left:5px;"><h:outputText value="#{msgs.perm_role}" /></h:outputLabel>
    </h:panelGroup> 
	<h:panelGroup style="padding-left:15px">
		<h:outputText value="#{msgs.perm_level}" style="font-weight:bold;" />
    </h:panelGroup>
  <div class="table-responsive"> 
  <h:dataTable id="perm" value="#{ForumTool.permissions}" var="permission" cellpadding="0" cellspacing="0" styleClass="table table-hover table-striped table-bordered">
    <h:column>
    <%-- row for role permission level begin --%>
    <h:panelGroup styleClass="permissionRow">
        
        <h:panelGroup styleClass="permissionRoleLabel">
          <h:outputLabel for="level"><h:outputText value="#{permission.name}" /></h:outputLabel>
        </h:panelGroup> 
          <h:panelGroup style="padding-left:5px">
          <h:selectOneMenu id="level" value="#{permission.selectedLevel}" onchange="javascript:setCorrespondingCheckboxes(this.id);"  disabled="#{not ForumTool.editMode}">
            <f:selectItems value="#{ForumTool.levels}"/>
          </h:selectOneMenu>
        </h:panelGroup>
    <h:panelGroup id="customize" styleClass="permissionCustomize">
      <h:outputText value=" #{msgs.perm_customize}" />
    </h:panelGroup>
    
    <%--specific permission pane grid begin--%>
    <f:verbatim><div class="permissionPanel"></f:verbatim>
    <%-- h:panelGrid  id="permissionSet" columns="4" cellpadding="0" cellspacing="0" --%>       
		<h:panelGroup styleClass="checkbox_group">
		    <h:panelGroup styleClass="checkbox" >
          <h:selectBooleanCheckbox id="newForum" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newForum}" disabled="#{not ForumTool.editMode || ForumTool.permissionMode != 'template'}"/>
          <h:outputLabel for="newForum"><h:outputText  value="#{msgs.perm_new_forum}" /></h:outputLabel>    
        </h:panelGroup>
        
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="newTopic" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newTopic}" disabled="#{not ForumTool.editMode || ForumTool.permissionMode == 'topic'}"/>
          <h:outputLabel for="newTopic"><h:outputText value="#{msgs.perm_new_topic}" /></h:outputLabel>
        </h:panelGroup>
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox  id="newR" value="#{permission.newResponse}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="newR"><h:outputText value="#{msgs.perm_new_response}" /></h:outputLabel>
        </h:panelGroup>
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="newRtoR"  value="#{permission.responseToResponse}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="newRtoR"><h:outputText value="#{msgs.perm_response_to_response}" /></h:outputLabel>
        </h:panelGroup> 
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="postGrades" rendered="#{ForumTool.gradebookExist}" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.postToGradebook}" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="postGrades" rendered="#{ForumTool.gradebookExist}"><h:outputText rendered="#{ForumTool.gradebookExist}" value="#{msgs.perm_post_to_gradebook}" /></h:outputLabel>
        </h:panelGroup>
		</h:panelGroup>
		<h:panelGroup styleClass="checkbox_group">
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="changeSetting" value="#{permission.changeSettings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="changeSetting"><h:outputText value="#{msgs.perm_change_settings}" /></h:outputLabel>
        </h:panelGroup>
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="read" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.read}"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="read"><h:outputText value="#{msgs.perm_read}" /></h:outputLabel>
        </h:panelGroup>
         <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="markAsRead" value="#{permission.markAsRead}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="markAsRead"><h:outputText value="#{msgs.perm_mark_as_read}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="moderatePostings" value="#{permission.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{(not ForumTool.editMode) || ForumTool.disableModeratePerm}"/>
          <h:outputLabel for="moderatePostings"><h:outputText value="#{msgs.perm_moderate_postings}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox" style="display: #{ForumTool.anonymousEnabled ? '' : 'none'}">
          <h:selectBooleanCheckbox id="identifyAnonAuthors" value="#{permission.identifyAnonAuthors}" onclick="setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="identifyAnonAuthors"><h:outputText value="#{msgs.perm_identify_anon_authors}" /></h:outputLabel>
        </h:panelGroup>
		</h:panelGroup>
        <h:panelGroup styleClass="radio_group permissionRadioGroup">
						<h:outputText value="#{msgs.perm_revise_postings}" style="display:block;padding:.3em;margin:0  "/>
						<h:selectOneRadio id="revisePostings" value="#{permission.revisePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}" styleClass="selectOneRadio" >
							<f:selectItems   value="#{ForumTool.postingOptions}" />
						</h:selectOneRadio>
        </h:panelGroup>       
        <h:panelGroup styleClass="radio_group permissionRadioGroup">
						<h:outputText  value="#{msgs.perm_delete_postings}" style="display:block;padding:.3em;margin:0"/>	
						<h:selectOneRadio id="deletePostings" value="#{permission.deletePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}" styleClass="selectOneRadio">
		        <f:selectItems   value="#{ForumTool.postingOptions}" />
		      </h:selectOneRadio>
        </h:panelGroup> 
				<%--specific permission  pane grid end--%>	
      <%-- /h:panelGrid --%>
    <f:verbatim></div></f:verbatim>
    </h:panelGroup>
        
    <%--<h:selectBooleanCheckbox id="movePosting" value="#{permission.movePosting}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
    <h:outputText value="#{msgs.perm_move_postings}" />
    --%>
    <%--<h:selectBooleanCheckbox id="moderatePostings"  value="#{permission.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
    <h:outputText value="#{msgs.perm_moderate_postings}" />
    --%>
    </h:column>
  </h:dataTable>
</div>
</mf:forumHideDivision>
