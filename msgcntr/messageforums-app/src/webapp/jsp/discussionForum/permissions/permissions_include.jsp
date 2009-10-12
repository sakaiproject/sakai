<%  
    /** initialize javascript from db **/
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{ForumTool}");
    DiscussionForumTool dft = (DiscussionForumTool) binding.getValue(context);
    out.print(dft.generatePermissionScript());
%>
<%--
       		<script type="text/javascript" src="/library/js/jquery.js"></script>
       		<sakai:script contextBase="/sakai-messageforums-tool" path="/js/sak-10625.js"/>
<sakai:script contextBase="/sakai-messageforums-tool" path="/js/permissions_header.js"/>
<sakai:script contextBase="/sakai-messageforums-tool" path="/js/forum.js"/>
--%>
<!--jsp/discussionForum/permissions/permissions_include.jsp-->
<mf:forumHideDivision title="#{msgs.cdfm_permissions}" id="cntrl_perm" hideByDefault="false" >
	<%--role picker pane--%>
	<h:panelGrid  columns="2" summary="layout" style="margin:.5em 0;" styleClass="jsfFormTable">
	  <h:panelGroup styleClass="shorttext">
			<h:outputLabel for="role" >
				<h:outputText value="#{msgs.perm_role_label}" />
			</h:outputLabel>
	  </h:panelGroup>
	  <h:panelGroup>
			<h:selectOneListbox size="1" id="role" value ="#{ForumTool.selectedRole}" onchange="javascript:displayRelevantBlock();">
	      <f:selectItems value="#{ForumTool.siteRoles}"/>
	    </h:selectOneListbox>
	  </h:panelGroup>
	</h:panelGrid>
  <%--
  <f:verbatim><p class="act"></f:verbatim>
    <h:commandButton immediate="true" action="#{ForumTool.processActionAddGroupsUsers}" value="#{msgs.cdfm_button_bar_add_groups_users}" rendered="#{ForumTool.editMode}"/> 
  <f:verbatim><p/></f:verbatim>
  --%>
<%
  String thisId = request.getParameter("panel");
  if (thisId == null) 
  {
    thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  }

%>
<script type="text/javascript">
  setPanelId('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
</script>   

	<h:dataTable id="perm" value="#{ForumTool.permissions}" var="permission" cellpadding="0" cellspacing="0" styleClass="jsfFormTable">
    <h:column>
			<h:panelGrid id="permissionSet"  columns="2" summary="layout"  styleClass="permissionPanel jsfFormTable lines nolines" cellpadding="0" cellspacing="0" rowClasses="blank exclude, header, colored,colored,colored,colored,colored,colored  last " columnClasses="first, second" >   
				<%--permission level picker pane--%>
        <h:panelGroup styleClass="shorttext">
          <h:outputLabel for="level"><h:outputText value="#{msgs.perm_level}" /></h:outputLabel>
        </h:panelGroup> 
					<h:panelGroup style="padding-left:5px">
          <h:selectOneMenu id="level" value="#{permission.selectedLevel}" onchange="javascript:setCorrespondingCheckboxes(this.id);"  disabled="#{not ForumTool.editMode}">
            <f:selectItems value="#{ForumTool.levels}"/>
          </h:selectOneMenu>
        </h:panelGroup>
				<%--permission level picker pane end--%>	
				<%--specific permission picker pane label  begin--%>	
					<h:panelGroup>	
						<h:outputText value="#{msgs.perm_custom}" />
					</h:panelGroup>
					<h:panelGroup>
						<h:outputText value=" "/>
					</h:panelGroup>
				<%--specific permission picker pane label  end--%>	
				<%--specific permission  pane grid start--%>
        <h:panelGroup styleClass="checkbox" >
          <h:selectBooleanCheckbox id="newForum" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newForum}" disabled="#{not ForumTool.editMode || ForumTool.permissionMode != 'template'}"/>
          <h:outputLabel for="newForum"><h:outputText  value="#{msgs.perm_new_forum}" /></h:outputLabel>    
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="changeSetting" value="#{permission.changeSettings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="changeSetting"><h:outputText value="#{msgs.perm_change_settings}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="newTopic" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newTopic}" disabled="#{not ForumTool.editMode || ForumTool.permissionMode == 'topic'}"/>
          <h:outputLabel for="newTopic"><h:outputText value="#{msgs.perm_new_topic}" /></h:outputLabel>
        </h:panelGroup>        
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="read" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.read}"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="read"><h:outputText value="#{msgs.perm_read}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox  id="newR" value="#{permission.newResponse}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="newR"><h:outputText value="#{msgs.perm_new_response}" /></h:outputLabel>
        </h:panelGroup>
         <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="markAsRead" value="#{permission.markAsRead}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="markAsRead"><h:outputText value="#{msgs.perm_mark_as_read}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="newRtoR"  value="#{permission.responseToResponse}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="newRtoR"><h:outputText value="#{msgs.perm_response_to_response}" /></h:outputLabel>
        </h:panelGroup>  
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="moderatePostings" value="#{permission.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{(not ForumTool.editMode) || ForumTool.disableModeratePerm}"/>
          <h:outputLabel for="moderatePostings"><h:outputText value="#{msgs.perm_moderate_postings}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="postGrades" rendered="#{ForumTool.gradebookExist}" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.postToGradebook}" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="postGrades" rendered="#{ForumTool.gradebookExist}"><h:outputText rendered="#{ForumTool.gradebookExist}" value="#{msgs.perm_post_to_gradebook}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup>
        	<h:outputText value="" />
        </h:panelGroup>
        <h:panelGroup>
						<h:outputText value="#{msgs.perm_revise_postings}" style="display:block;padding:.3em;margin:0  "/>
						<h:selectOneRadio id="revisePostings" value="#{permission.revisePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}" styleClass="selectOneRadio" >
							<f:selectItems   value="#{ForumTool.postingOptions}" />
						</h:selectOneRadio>
        </h:panelGroup>       
        <h:panelGroup>
						<h:outputText  value="#{msgs.perm_delete_postings}" style="display:block;padding:.3em;margin:0"/>	
						<h:selectOneRadio id="deletePostings" value="#{permission.deletePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}" styleClass="selectOneRadio">
		        <f:selectItems   value="#{ForumTool.postingOptions}" />
		      </h:selectOneRadio>
        </h:panelGroup> 
				<%--specific permission  pane grid end--%>	
      </h:panelGrid>
		<%--<h:selectBooleanCheckbox id="movePosting" value="#{permission.movePosting}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_move_postings}" />
		--%>
		<%--<h:selectBooleanCheckbox id="moderatePostings"  value="#{permission.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
		<h:outputText value="#{msgs.perm_moderate_postings}" />
		--%>
    </h:column>
  </h:dataTable>
  	<%--designNote: gradebook assignment - need to finesse this - make aware that functionality exists, but flag that there are no gb assignmetns to select --%>
	<%--designNote:  How is this a "permission" item? --%>  
	<h:panelGrid columns="2" summary="layout" rendered="#{ForumTool.gradebookExist &&  ForumTool.permissionMode == 'forum'}" style="margin-top:.5em;clear:both">
    <h:panelGroup styleClass="shorttext">
			<h:outputLabel for="forum_assignments" value="#{msgs.perm_choose_assignment}"></h:outputLabel>  
      </h:panelGroup>
	  <h:panelGroup>
			<h:selectOneMenu id="forum_assignments" value="#{ForumTool.selectedForum.gradeAssign}" disabled="#{not ForumTool.editMode}">
   	    <f:selectItems value="#{ForumTool.assignments}" />
      </h:selectOneMenu>
    </h:panelGroup>
  </h:panelGrid>
	<h:panelGrid columns="2" summary="layout" rendered="#{ForumTool.gradebookExist &&  ForumTool.permissionMode == 'topic' && !ForumTool.selectedForum.markForDeletion}" style="margin-top:.5em;clear:both">
    <h:panelGroup>  	
			<h:outputLabel for="topic_assignments"  value="#{msgs.perm_choose_assignment}"  ></h:outputLabel>
  	</h:panelGroup>		
  	<h:panelGroup>
			<h:selectOneMenu value="#{ForumTool.selectedTopic.gradeAssign}" id="topic_assignments" disabled="#{not ForumTool.editMode}">
     	    <f:selectItems value="#{ForumTool.assignments}" />
  	    </h:selectOneMenu>
  	  </h:panelGroup>
  </h:panelGrid>
  <sakai:script contextBase="/sakai-messageforums-tool" path="/js/permissions_footer.js"/>	 	
</mf:forumHideDivision>
