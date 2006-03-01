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

  <h:dataTable styleClass="listHier" id="perm" value="#{ForumTool.templatePermissions}" var="cntrl_settings">
    <h:column>
      <h:panelGroup id="permissionSet" >
        <f:verbatim>	<table><tr><td colspan="2"></f:verbatim>
        <h:outputText value="#{msgs.perm_level}" style="font-weight:bold"/>
        <h:selectOneMenu id="level" value="#{cntrl_settings.selectedLevel}" onchange="javascript:setCorrespondingCheckboxes(this.id);"  disabled="#{not ForumTool.editMode}">
          <f:selectItems value="#{ForumTool.levels}"/>
        </h:selectOneMenu>
        <f:verbatim>	</td></tr><tr><td> </f:verbatim>
        <h:selectBooleanCheckbox id="newForum" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.newForum}" disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_new_forum}"/>
        <f:verbatim></td><td></f:verbatim>
        <h:selectBooleanCheckbox id="changeSetting" value="#{cntrl_settings.changeSettings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_change_settings}"/>
		<f:verbatim></td></tr><tr><td></f:verbatim>
	    <h:selectBooleanCheckbox id="newTopic" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.newTopic}" disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_new_topic}" />
		<f:verbatim></td><td></f:verbatim>
        <h:selectBooleanCheckbox id="postGrades" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.postToGradebook}" disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_post_to_gradebook}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:selectBooleanCheckbox  id="newR" value="#{cntrl_settings.newResponse}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
        <h:outputText value="#{msgs.perm_new_response}" />
		<f:verbatim></td><td></f:verbatim>
        <h:selectBooleanCheckbox id="read" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.read}"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_read}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:selectBooleanCheckbox id="newRtoR"  value="#{cntrl_settings.responseToResponse}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_response_to_response}" />
		<f:verbatim></td><td></f:verbatim>
		<h:selectBooleanCheckbox  id="markAsRead" value="#{cntrl_settings.markAsRead}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_mark_as_read}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:selectBooleanCheckbox  id="movePosting" value="#{cntrl_settings.movePosting}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_move_postings}" />
		<f:verbatim></td><td></f:verbatim>
		<h:selectBooleanCheckbox id="moderatePostings"  value="#{cntrl_settings.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_moderate_postings}" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
		<h:outputText value="#{msgs.perm_revise_postings}" style="font-weight:bold" />
		<f:verbatim></td><td></f:verbatim>
		<h:outputText value="#{msgs.perm_delete_postings}" style="font-weight:bold" />
		<f:verbatim></td></tr><tr><td></f:verbatim>
        <h:selectOneRadio id="revisePostings" value="#{cntrl_settings.revisePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}">
		  <f:selectItems   value="#{ForumTool.postingOptions}" />
		</h:selectOneRadio>
		<f:verbatim></td><td></f:verbatim>
    	<h:selectOneRadio id="deletePostings" value="#{cntrl_settings.deletePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}">
		  <f:selectItems   value="#{ForumTool.postingOptions}" />
		</h:selectOneRadio>
		<f:verbatim></td></tr></table></f:verbatim>
      </h:panelGroup>
    </h:column>
  </h:dataTable>
  <sakai:script contextBase="/sakai-messageforums-tool" path="/js/permissions_footer.js"/>	 	
</mf:forumHideDivision>
