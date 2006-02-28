<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
   <sakai:view>

    <%  
      /** initialize javascript from db **/
    FacesContext context = FacesContext.getCurrentInstance();
    Application app = context.getApplication();
    ValueBinding binding = app.createValueBinding("#{ForumTool}");
    DiscussionForumTool dft = (DiscussionForumTool) binding.getValue(context);
    out.print(dft.generatePermissionScript());
    %>
   
   
   <SCRIPT type="text/javascript">

    function setCorrespondingLevel(checkBox)
    {
		//alert(checkBox);
		var2 = checkBox.split(":");
		selectLevel = getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":level");

		changeSettings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":changeSetting");
		deletePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":deletePostings");
		deleteAny=getDeleteAny(deletePostings);
		deleteOwn=getDeleteOwn(deletePostings);
		markAsRead=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":markAsRead");
		movePosting=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":movePosting");
		newForum=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newForum");
		newResponse=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newR");
		r2R=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newRtoR");
		newTopic=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newTopic");
		postGrades=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":postGrades");
		read=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":read");
		revisePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":revisePostings")
		reviseAny=getReviseAny(revisePostings);
		reviseOwn= getReviseOwn(revisePostings);
		moderatePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":moderatePostings");

		if(selectLevel)
		{
			if(!(changeSettings && markAsRead &&  movePosting && newForum && newResponse && r2R && newTopic && postGrades && read &&moderatePostings && deletePostings && revisePostings))
			{
				setIndexWithTextValue(selectLevel, custom)
			}
			else
			{
				var newArray = [changeSettings.checked,deleteAny,deleteOwn,markAsRead.checked, movePosting.checked, newForum.checked, newResponse.checked, r2R.checked, newTopic.checked, postGrades.checked, read.checked, reviseAny, reviseOwn, moderatePostings.checked];
				//alert(newArray);
				//alert(checkLevel(newArray));
				setIndexWithTextValue(selectLevel, checkLevel(newArray));
			}

			role=getTheElement(var2[0]+":role");

				//alert(role);
				roleValue=role.options[var2[2]].value;
				//alert(roleValue);
 				var lev=selectLevel.options[selectLevel.selectedIndex].text;
				//alert(lev);
				var newval=roleValue+"("+lev+")";
				//alert(newval);

				role.options[var2[2]]=new Option(newval, roleValue, true);
				role.options[var2[2]].selected=true;
		}

    }

    function setIndexWithTextValue(element, textValue)
    {            
		for (i=0;i<element.length;i++)
				{
					if (element.options[i].value==textValue)
					{
						element.selectedIndex=i;
					}
		}
	}


    function getReviseAny(element)
    {
		if(!element)
		{
			//alert("getReviseAny: Returning");
		return false;
		}
		var user_input =  getRadioButtonCheckedValue(element);
		//alert(user_input);
			if(user_input==all)
				return true;
			else
				return false;
	}

    function getReviseOwn(element)
	{
		if(!element)
			{	return false;}
				var user_input =  getRadioButtonCheckedValue(element);
					if(user_input==all)
						return true;
					if(user_input==own)
						return true;

					else

				return false;
	}

	function getDeleteAny(element)
	{
		if(!element)
			{	return false;}
				var user_input =  getRadioButtonCheckedValue(element);
					if(user_input==all)
						return true;
					else
				return false;
	}

	function getDeleteOwn(element)
	{
		if(!element)
			{	return false;}
				var user_input =  getRadioButtonCheckedValue(element);
					if(user_input==all)
						return true;

					if(user_input==own)
						return true;
					else
				return false;
	}

    function getRadioButtonCheckedValue(element)
    {
		var user_input=none;
		//alert(element.length+element.id);
		var inputs = element.getElementsByTagName ('input');
		for (i=0;i<inputs.length;i++)
		{
		//	alert(inputs[i].value+inputs.length+inputs.id);
			if (inputs[i].checked==true)
			{
				user_input = inputs[i].value;
			}
		}
		//alert("Radio checked :"+user_input );
		return user_input;

	}

	function setRadioButtonValue(element, newValue)
    {
		var inputs = element.getElementsByTagName ('input');
		for (i=0;i<inputs.length;i++)
		{
			if (inputs[i].value==newValue)
			{
				inputs[i].checked=true;
			}
		}
	}

    function setCorrespondingCheckboxes(checkBox)
	    {
			var2 = checkBox.split(":");
			selectLevel = getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":level");

			changeSettings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":changeSetting");
			deletePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":deletePostings");
			deleteAny=getDeleteAny(deletePostings);
			deleteOwn=getDeleteOwn(deletePostings);
			markAsRead=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":markAsRead");
			movePosting=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":movePosting");
			newForum=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newForum");
			newResponse=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newR");
			r2R=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newRtoR");
			newTopic=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":newTopic");
			postGrades=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":postGrades");
			read=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":read");
			revisePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":revisePostings")
			reviseAny=getReviseAny(revisePostings);
			reviseOwn= getReviseOwn(revisePostings);
			moderatePostings=getTheElement(var2[0]+":"+ var2[1]+":"+ var2[2]+":moderatePostings");

			role=getTheElement(var2[0]+":role");
			//alert(selectLevel.options[selectLevel.selectedIndex].value);
			if(selectLevel)
			{

				if(!(changeSettings && markAsRead &&  movePosting && newForum && newResponse && r2R && newTopic && postGrades && read &&moderatePostings && deletePostings && revisePostings))
				{
					setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  noneLevelArray);
				}


				if(selectLevel.options[selectLevel.selectedIndex].value==owner)
					{
					 	setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  ownerLevelArray);
				    }

				else 	if(selectLevel.options[selectLevel.selectedIndex].value==author)
					{					    
					 	setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  authorLevelArray);
				    }
				else 	if(selectLevel.options[selectLevel.selectedIndex].value==nonEditingAuthor)
					{
					 	setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  noneditingAuthorLevelArray);
				    }
				else 	if(selectLevel.options[selectLevel.selectedIndex].value==reviewer)
					{
					 	setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  reviewerLevelArray);
				    }
				   else 	if(selectLevel.options[selectLevel.selectedIndex].value==none)
					{
					 	setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  noneLevelArray);
				    }
			    else 	if(selectLevel.options[selectLevel.selectedIndex].value==contributor)
					{
						setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  contributorLevelArray);
				    }

				roleValue=role.options[var2[2]].value;
				var lev=selectLevel.options[selectLevel.selectedIndex].text;
				var newval=roleValue+"("+lev+")";
				role.options[var2[2]]=new Option(newval, roleValue, true);
				role.options[var2[2]].selected=true;

		}

    }

function	setCheckBoxes(changeSettings, deletePostings, markAsRead ,movePosting, newForum, newResponse,  r2R, newTopic, postGrades, read,revisePostings, moderatePostings,  arrayLevel)
	{	
		changeSettings.checked= arrayLevel[0];
		//deletePostings
		if(arrayLevel[1]==true)
		{
			setRadioButtonValue(deletePostings, all);
		}
		else if(arrayLevel[2]==true)
		{
			setRadioButtonValue(deletePostings, own);
		}
		else
		{
			setRadioButtonValue(deletePostings, none);
		}

		markAsRead.checked= arrayLevel[3];
		movePosting.checked= arrayLevel[4];
		newForum.checked= arrayLevel[5];
		newResponse.checked= arrayLevel[6];
		r2R.checked= arrayLevel[7];
		newTopic.checked= arrayLevel[8];
		postGrades.checked= arrayLevel[9];
		read.checked= arrayLevel[10];
		//revisePostings,
		if(arrayLevel[11]==true)
		{
			setRadioButtonValue(revisePostings, all);
		}
		else if(arrayLevel[12]==true)
		{
			setRadioButtonValue(revisePostings, own);
		}
		else
		{
			setRadioButtonValue(revisePostings, none);
		}
		moderatePostings.checked= arrayLevel[13];
	}


    function displayRelevantBlock()
    {
		role=getTheElement("revise:role");
		i=0;
		while(true)
		{
			spanId=getTheElement("revise:perm:"+i+":permissionSet");
			if(spanId)
			{
				spanId.style.display="none";
			}
			else
			{
				break;
			}
			i++;
		}

		spanId=getTheElement("revise:perm:"+ role.selectedIndex+":permissionSet");
		if(spanId)
		{
			spanId.style.display="block";
		}
	}

</SCRIPT>
      <h:form id="revise">
      <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
        <sakai:tool_bar_message value="#{msgs.cdfm_default_template_settings}" />
 		 <div class="instruction">
  			    <h:outputText id="instruction"  value="#{msgs.cdfm_default_template_settings_instruction}"/>
		 </div>
       <mf:forumHideDivision title="#{msgs.cdfm_permissions}" id="cntrl_perm" >

       <h:selectOneListbox size="4" style="width: 300px" id="role" value ="#{ForumTool.selectedRole}" onchange="javascript:displayRelevantBlock();">
	   					<f:selectItems value="#{ForumTool.siteRoles}"/>
	   </h:selectOneListbox>

		 <h:dataTable styleClass="listHier" id="perm" value="#{ForumTool.templatePermissions}" var="cntrl_settings">
   			<h:column>
				<h:panelGroup id="permissionSet" >
	       		<f:verbatim>	<table><tr><td colspan="2"></f:verbatim>
	       		<h:outputText value="#{msgs.perm_level}" style="font-weight:bold"/>		<h:selectOneMenu id="level" value="#{cntrl_settings.selectedLevel}" onchange="javascript:setCorrespondingCheckboxes(this.id);">
							<f:selectItems value="#{ForumTool.levels}"/>
						</h:selectOneMenu>
				<f:verbatim>	</td>
					</tr>
					<tr>
						<td></f:verbatim>
							<h:selectBooleanCheckbox id="newForum" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.newForum}"/>
							<h:outputText value="#{msgs.perm_new_forum}"/>
					<f:verbatim>	</td>
						<td></f:verbatim>
							<h:selectBooleanCheckbox id="changeSetting" value="#{cntrl_settings.changeSettings}" onclick="javascript:setCorrespondingLevel(this.id);" />
							<h:outputText value="#{msgs.perm_change_settings}"/>
					<f:verbatim>	</td>
					</tr>
					<tr>
						<td></f:verbatim>
							<h:selectBooleanCheckbox id="newTopic" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.newTopic}"/>
							<h:outputText value="#{msgs.perm_new_topic}" />
					<f:verbatim>	</td>
						<td></f:verbatim>
							<h:selectBooleanCheckbox id="postGrades" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.postToGradebook}"/>
							<h:outputText value="#{msgs.perm_post_to_gradebook}" />
						<f:verbatim></td>
					</tr>
					<tr>
						<td></f:verbatim>
							<h:selectBooleanCheckbox  id="newR" value="#{cntrl_settings.newResponse}" onclick="javascript:setCorrespondingLevel(this.id);"/>
							<h:outputText value="#{msgs.perm_new_response}" />
					<f:verbatim>	</td>
						<td></f:verbatim>
							<h:selectBooleanCheckbox id="read" onclick="javascript:setCorrespondingLevel(this.id);" value="#{cntrl_settings.read}"/>
							<h:outputText value="#{msgs.perm_read}" />
					<f:verbatim>	</td>
					</tr>
					<tr>
						<td></f:verbatim>
						<h:selectBooleanCheckbox id="newRtoR"  value="#{cntrl_settings.responseToResponse}" onclick="javascript:setCorrespondingLevel(this.id);" />
							<h:outputText value="#{msgs.perm_response_to_response}" />

						<f:verbatim></td>
						<td></f:verbatim>
							<h:selectBooleanCheckbox  id="markAsRead" value="#{cntrl_settings.markAsRead}" onclick="javascript:setCorrespondingLevel(this.id);"/>
							<h:outputText value="#{msgs.perm_mark_as_read}" />
					<f:verbatim>	</td>
					</tr>
					<tr>
						<td></f:verbatim>
								<h:selectBooleanCheckbox  id="movePosting" value="#{cntrl_settings.movePosting}" onclick="javascript:setCorrespondingLevel(this.id);"/>
								<h:outputText value="#{msgs.perm_move_postings}" />
						<f:verbatim></td>
						<td></f:verbatim>
							<h:selectBooleanCheckbox id="moderatePostings"  value="#{cntrl_settings.moderatePostings}" onclick="javascript:setCorrespondingLevel(this.id);" />
							<h:outputText value="#{msgs.perm_moderate_postings}" />
					<f:verbatim>	</td>
					</tr>
					<tr>
						<td>
							 </f:verbatim><h:outputText value="#{msgs.perm_revise_postings}" style="font-weight:bold" /><f:verbatim>
						</td>
						<td>
							 </f:verbatim><h:outputText value="#{msgs.perm_delete_postings}" style="font-weight:bold" /><f:verbatim>
						</td>
					</tr>
					<tr>
						<td></f:verbatim>
							 <h:selectOneRadio id="revisePostings" value="#{cntrl_settings.revisePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);">
							   <f:selectItems   value="#{ForumTool.postingOptions}" />
							  </h:selectOneRadio>
						<f:verbatim></td>
						<td></f:verbatim>
							 <h:selectOneRadio id="deletePostings" value="#{cntrl_settings.deletePostings}"  layout="pageDirection"  onclick="setCorrespondingLevel(this.name);">
							   <f:selectItems   value="#{ForumTool.postingOptions}" />
							  </h:selectOneRadio>
						<f:verbatim></td>
					</tr>
					</table></f:verbatim>
				</h:panelGroup>
			</h:column>

	  </h:dataTable> </mf:forumHideDivision>
      <p class="act">
          <h:commandButton action="#{ForumTool.processActionSaveTemplateSettings}" onclick="form.submit;" value="#{msgs.cdfm_button_bar_save_setting}"/>
          <h:commandButton action="#{ForumTool.processActionRestoreDefaultTemplate}" value="Restore Defaults"/>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>

	 </h:form>

	  <SCRIPT type="text/javascript">
	 		role=getTheElement("revise:role");
	 		role.selectedIndex=0;
	 		i=0;
	 		while(true)
	 		{
	 			spanId=getTheElement("revise:perm:"+i+":permissionSet");
	 			if(spanId)
	 			{
	 				spanId.style.display="none";
	 			}
	 			else
	 			{
	 				break;
	 			}
	 			i++;
	 		}

	 		spanId=getTheElement("revise:perm:0:permissionSet");
	 		if(spanId)
	 		{
	 			spanId.style.display="block";
	 		}

	</SCRIPT>

    </sakai:view>
</f:view>
