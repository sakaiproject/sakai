<!--jsp/discussionForum/permissions/permissions_include.jsp-->
<mf:forumHideDivision title="#{msgs.cdfm_permissions}" id="cntrl_perm" hideByDefault="#{ForumTool.collapsePermissionPanel}">
<script>
  // Permission level arrays injected via JSF
  window.ownerLevelArray = <h:outputText value="#{ForumTool.ownerLevelArray}" escape="false"/>;
  window.authorLevelArray = <h:outputText value="#{ForumTool.authorLevelArray}" escape="false"/>;
  window.noneditingAuthorLevelArray = <h:outputText value="#{ForumTool.noneditingAuthorLevelArray}" escape="false"/>;
  window.reviewerLevelArray = <h:outputText value="#{ForumTool.reviewerLevelArray}" escape="false"/>;
  window.noneLevelArray = <h:outputText value="#{ForumTool.noneLevelArray}" escape="false"/>;
  window.contributorLevelArray = <h:outputText value="#{ForumTool.contributorLevelArray}" escape="false"/>;

  setPanelId('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  var isGradebookGroupEnabled = <h:outputText value="#{ForumTool.gradebookGroupEnabled}"/>;

  $(document).ready(function() {
          $('input[id*="\\:revisePostings\\:"], input[id*="\\:deletePostings\\:"]').each(function() {
                  let elementId = $(this).attr("id");
                  let rowIndex = elementId.split(":")[2];
                  let groupLabel = elementId.split(":")[3];
                  let permRole = "";
                  if ($("#revise\\:perm\\:"+ rowIndex +"\\:perm_role_label1").length) {
                       permRole = $("#revise\\:perm\\:"+ rowIndex +"\\:perm_role_label1").text();
                  }
                  else if ($("#revise\\:perm\\:"+ rowIndex +"\\:perm_role_label2").length) {
                      permRole = $("#revise\\:perm\\:"+ rowIndex +"\\:perm_role_label2").text();
                  }
                  let label = permRole  + " " + $('#revise\\:perm\\:'+rowIndex+'\\:'+ groupLabel +"_label").text() + " " + $(this).next().text();
                  $(this).attr('aria-label', label);
          });
          $('input:checkbox[id^="revise\\:perm"]').each(function() {
            let elementId = $(this).attr("id");
            let rowIndex = elementId.split(":")[2];
            let permRoleLabel = "";

            if ($("#revise\\:perm\\:"+ rowIndex +"\\:perm_role_label1").length) {
                permRoleLabel = "revise:perm:"+ rowIndex +":perm_role_label1";
            } else if ($("#revise\\:perm\\:"+ rowIndex +"\\:perm_role_label2").length) {
                permRoleLabel = "revise:perm:"+ rowIndex +":perm_role_label2";
            }

            $(this).attr('aria-labelledby', permRoleLabel + " " + $(this).attr('id') + '_label');
          });

          if (isGradebookGroupEnabled) {
            $('select[id^="revise\\:perm"]').each(function() {
              let elementId = $(this).attr("id");
              let rowIndex = elementId.split(":")[2];
              const elementSelect = $("#revise\\:perm\\:" + rowIndex + "\\:level");
              const permissionTrigger = document.getElementById("revise:perm:" + rowIndex + ":customize");

              if (elementSelect) {
                elementSelect.prop("disabled", true);
              }

              if (permissionTrigger) {
                permissionTrigger.style.display = "none";
              }
            });
          }
    });
</script>
    <h:panelGroup rendered="#{ForumTool.selectedForum.restrictPermissionsForGroups == 'true' && ForumTool.permissionMode == 'forum'}" styleClass="itemAction">
		<h:outputText value="#{msgs.cdfm_autocreate_forums_edit}" style="display:block"/>
    </h:panelGroup>
    <h:panelGroup rendered="#{ForumTool.selectedTopic.restrictPermissionsForGroups == 'true' && ForumTool.permissionMode == 'topic'}" styleClass="itemAction">
		<h:outputText value="#{msgs.cdfm_autocreate_topics_edit}" style="display:block"/>
    </h:panelGroup>
    <h:panelGroup styleClass="permissionRoleLabel">
		<h:outputLabel style="font-weight:bold;padding-left:5px;"><h:outputText value="#{msgs.perm_role}" /></h:outputLabel>
    </h:panelGroup> 
	<h:panelGroup style="padding-left:15px">
		<h:outputText value="#{msgs.perm_level}" style="font-weight:bold;" />
    </h:panelGroup>
  <div class="table"> 
  <h:dataTable id="perm" value="#{ForumTool.permissions}" var="permission" cellpadding="0" cellspacing="0" styleClass="table table-hover table-striped table-bordered">
    <h:column>
    <%-- row for role permission level begin --%>
	<h:panelGroup rendered="#{permission.item.type == 3 && (ForumTool.selectedForum.restrictPermissionsForGroups || ForumTool.selectedTopic.restrictPermissionsForGroups)}" styleClass="permissionRow">
		<h:panelGroup styleClass="permissionRoleLabel">
          <h:outputLabel for="level"><h:outputText value="#{permission.name}" id="perm_role_label1" /></h:outputLabel>
        </h:panelGroup> 
		<h:panelGroup style="padding-left:5px">
          <h:outputText value="#{permission.selectedLevel}" />
        </h:panelGroup>
	</h:panelGroup>
    <h:panelGroup rendered="#{!(ForumTool.selectedForum.restrictPermissionsForGroups || ForumTool.selectedTopic.restrictPermissionsForGroups) || permission.item.type != 3}" styleClass="permissionRow">
        <h:panelGroup styleClass="permissionRoleLabel">
          <h:outputLabel for="level"><h:outputText value="#{permission.name}" id="perm_role_label2" /></h:outputLabel>
        </h:panelGroup> 
          <h:panelGroup style="padding-left:5px">
          <h:selectOneMenu id="level" value="#{permission.selectedLevel}" onchange="javascript:setCorrespondingCheckboxes(this.id);" disabled="#{not ForumTool.editMode}">
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
          <h:outputLabel for="newForum"><h:outputText id="newForum_label" value="#{msgs.perm_new_forum}" /></h:outputLabel>
        </h:panelGroup>
        
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="newTopic" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.newTopic}" disabled="#{not ForumTool.editMode || ForumTool.permissionMode == 'topic'}"/>
          <h:outputLabel for="newTopic"><h:outputText id="newTopic_label" value="#{msgs.perm_new_topic}" /></h:outputLabel>
        </h:panelGroup>
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox  id="newR" value="#{permission.newResponse}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="newR"><h:outputText id="newR_label" value="#{msgs.perm_new_response}" /></h:outputLabel>
        </h:panelGroup>
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="newRtoR"  value="#{permission.responseToResponse}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="newRtoR"><h:outputText id="newRtoR_label" value="#{msgs.perm_response_to_response}" /></h:outputLabel>
        </h:panelGroup> 
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="postGrades" rendered="#{ForumTool.gradebookExist}" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.postToGradebook}" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="postGrades" rendered="#{ForumTool.gradebookExist}"><h:outputText id="postGrades_label" rendered="#{ForumTool.gradebookExist}" value="#{msgs.perm_post_to_gradebook}" /></h:outputLabel>
        </h:panelGroup>
		</h:panelGroup>
		<h:panelGroup styleClass="checkbox_group">
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="changeSetting" value="#{permission.changeSettings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="changeSetting"><h:outputText id="changeSetting_label" value="#{msgs.perm_change_settings}" /></h:outputLabel>
        </h:panelGroup>
		<h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="read" onclick="javascript:setCorrespondingLevel(this.id);" value="#{permission.read}"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="read"><h:outputText id="read_label" value="#{msgs.perm_read}" /></h:outputLabel>
        </h:panelGroup>
         <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="markAsNotRead" value="#{permission.markAsNotRead}" onclick="javascript:setCorrespondingLevel(this.id);"  disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="markAsNotRead"><h:outputText id="markAsNotRead_label" value="#{msgs.perm_mark_as_not_read}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox">
          <h:selectBooleanCheckbox id="moderatePostings" value="#{permission.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);" disabled="#{(not ForumTool.editMode) || ForumTool.disableModeratePerm}"/>
          <h:outputLabel for="moderatePostings"><h:outputText id="moderatePostings_label" value="#{msgs.perm_moderate_postings}" /></h:outputLabel>
        </h:panelGroup>
        <h:panelGroup styleClass="checkbox" style="display: #{ForumTool.anonymousEnabled ? '' : 'none'}">
          <h:selectBooleanCheckbox id="identifyAnonAuthors" value="#{permission.identifyAnonAuthors}" onclick="setCorrespondingLevel(this.id);" disabled="#{not ForumTool.editMode}"/>
          <h:outputLabel for="identifyAnonAuthors"><h:outputText id="identifyAnonAuthors_label" value="#{msgs.perm_identify_anon_authors}" /></h:outputLabel>
        </h:panelGroup>
		</h:panelGroup>
        <h:panelGroup styleClass="radio_group permissionRadioGroup">
						<h:outputText value="#{msgs.perm_revise_postings}" id="revisePostings_label" style="display:block;padding:.3em;margin:0  "/>
						<h:selectOneRadio id="revisePostings" value="#{permission.revisePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);"  disabled="#{not ForumTool.editMode}" styleClass="selectOneRadio" >
							<f:selectItems   value="#{ForumTool.postingOptions}" />
						</h:selectOneRadio>
        </h:panelGroup>       
        <h:panelGroup styleClass="radio_group permissionRadioGroup">
						<h:outputText  value="#{msgs.perm_delete_postings}" id="deletePostings_label" style="display:block;padding:.3em;margin:0"/>
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
