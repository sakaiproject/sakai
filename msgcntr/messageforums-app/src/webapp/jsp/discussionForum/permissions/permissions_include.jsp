<%  
    /** initialize javascript from db **/
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{ForumTool}");
    DiscussionForumTool dft = (DiscussionForumTool) binding.getValue(context);
    out.print(dft.generatePermissionScript());
%>

<sakai:script contextBase="/sakai-messageforums-tool" path="/js/permissions_header.js"/>

<mf:forumHideDivision title="#{msgs.cdfm_permissions}" id="cntrl_perm" >

  <h:selectOneListbox size="4" style="width: 300px" id="role" value ="#{ForumTool.selectedRole}" onchange="javascript:displayRelevantBlock();">
    <f:selectItems value="#{ForumTool.siteRoles}"/>
  </h:selectOneListbox>

  <h:dataTable style="border-collapse:collapse ; border-width: 0px none; border:0px; margin: 0px; padding: 0px; border-spacing:0px" id="perm" value="#{ForumTool.permissions}" var="permission">
    <h:column>
      <h:panelGroup id="permissionSet" style="display:inline; margin:0; padding:0; border:0 none">
        <f:verbatim>	<table><tr><td colspan="2"></f:verbatim>
        <h:outputText value="#{msgs.perm_level}" style="font-weight:bold"/>
        <h:selectOneMenu id="level" value="#{permission.selectedLevel}" onchange="javascript:setCorrespondingCheckboxes(this.id);"  disabled="#{not ForumTool.editMode}">
          <f:selectItems value="#{ForumTool.levels}"/>
        </h:selectOneMenu>
        <f:verbatim>	</td></tr><tr><td> </f:verbatim>
        <h:selectBooleanCheckbox id="newForum" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newForum}" disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_new_forum}"/>
        <f:verbatim></td><td></f:verbatim>
        <h:selectBooleanCheckbox id="changeSetting" value="#{permission.changeSettings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_change_settings}"/>
		<f:verbatim></td></tr><tr><td></f:verbatim>
	    <h:selectBooleanCheckbox id="newTopic" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newTopic}" disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_new_topic}" />
		<f:verbatim></td><td></f:verbatim>
        <h:selectBooleanCheckbox id="postGrades" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.postToGradebook}" disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_post_to_gradebook}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:selectBooleanCheckbox  id="newR" value="#{permission.newResponse}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_new_response}" />
		<f:verbatim></td><td></f:verbatim>
        <h:selectBooleanCheckbox id="read" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.read}"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_read}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:selectBooleanCheckbox id="newRtoR"  value="#{permission.responseToResponse}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_response_to_response}" />
		<f:verbatim></td><td></f:verbatim>
		<h:selectBooleanCheckbox  id="markAsRead" value="#{permission.markAsRead}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_mark_as_read}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:selectBooleanCheckbox  id="movePosting" value="#{permission.movePosting}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_move_postings}" />
		<f:verbatim></td><td></f:verbatim>
		<h:selectBooleanCheckbox id="moderatePostings"  value="#{permission.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_moderate_postings}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:outputText value="#{msgs.perm_revise_postings}" style="font-weight:bold" />
		<f:verbatim></td><td></f:verbatim>
		<h:outputText value="#{msgs.perm_delete_postings}" style="font-weight:bold" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
        <h:selectOneRadio id="revisePostings" value="#{permission.revisePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}">
		  <f:selectItems   value="#{ForumTool.postingOptions}" />
		</h:selectOneRadio>
		<f:verbatim></td><td></f:verbatim>
    	<h:selectOneRadio id="deletePostings" value="#{permission.deletePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}">
		  <f:selectItems   value="#{ForumTool.postingOptions}" />
		</h:selectOneRadio>
		<f:verbatim></td></tr></table></f:verbatim>
      </h:panelGroup>
    </h:column>
  </h:dataTable>
  <sakai:script contextBase="/sakai-messageforums-tool" path="/js/permissions_footer.js"/>	 	
</mf:forumHideDivision>
