<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view>
	<sakai:view toolCssHref="/messageforums-tool/css/msgcntr.css">
      <h:form id="revise">
        <script type="text/javascript">includeLatestJQuery("msgcntr");</script>
        <sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
		<script type="text/javascript">
			$(document).ready(function(){
				//fade permission block and then disable all the inputs/selects in the permission include so as not to confuse people
				$('#permissionReadOnly').fadeTo("fast", 0.50);
				// cannot seem to disable these controls and still submit
				// $('#permissionReadOnly input, #permissionReadOnly select').attr('disabled', 'disabled');
				//toggle the long description, hiding the hide link, then toggling the hide, show links and description
				$('a#hide').hide();
				$('#toggle').hide();
				$('a#show,a#hide').click(function(){
					$('#toggle,a#hide,a#show').toggle();
					resizeFrame('grow');
					return false;
				});
			});
		</script>
		<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
		%>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>			
		<%--//designNote: this just feels weird - presenting somehting that sort of looks like the form used to create the topic (with an editable permissions block!) to comfirm deletion --%>
<!--jsp/discussionForum/topic/dfTopicSettings.jsp-->
		<%--<sakai:tool_bar_message value="#{msgs.cdfm_delete_topic_title}"/>--%>
        
		<h:outputText id="alert-delete" styleClass="messageAlert" style="display:block" value="#{msgs.cdfm_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"/>
        <h:outputText styleClass="messageAlert" value="#{msgs.cdfm_duplicate_topic_confirm}" rendered="#{ForumTool.selectedTopic.markForDuplication}" style="display:block" />
		<div class="topicBloc" style="padding:0 .5em"><h:messages styleClass="messageAlert" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
			<p>
				<span class="title">
					<h:graphicImage url="/images/silk/lock.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedTopic.topic.locked=='true'}"  style="margin-right:.3em"/>
					<h:graphicImage url="/images/silk/lock_open.png" alt="#{msgs.cdfm_forum_locked}" rendered="#{ForumTool.selectedTopic.topic.locked=='false'}"  style="margin-right:.3em"/>
					<h:outputText value="#{ForumTool.selectedTopic.topic.title}" rendered="#{!ForumTool.selectedTopic.markForDuplication}"/>
                    <h:inputText size="50" value="#{ForumTool.selectedTopic.topic.title}" id="topic_title" rendered="#{ForumTool.selectedTopic.markForDuplication}">
                        <f:validateLength maximum="255" minimum="1" />
                    </h:inputText>                   
                    
				</span>
				<h:outputText   value="#{msgs.cdfm_openb}"/>
				<h:outputText   value="#{msgs.cdfm_moderated}"  rendered="#{ForumTool.selectedTopic.topic.moderated=='true'}" />
				<h:outputText   value="#{msgs.cdfm_notmoderated}"  rendered="#{ForumTool.selectedTopic.topic.moderated=='false'}" />
				<h:outputText   value="#{msgs.cdfm_closeb}"/>

				</p>
			<p class="textPanel">
				    <h:outputText id="topic_shortDescription"  value="#{ForumTool.selectedTopic.topic.shortDescription}"/>
			</p>
			<p><a id="show" class="show"  href="#">
				<h:graphicImage url="/images/collapse.gif" alt="" /><h:outputText   value="#{msgs.cdfm_full_description}"/>
			</a></p>
			<p><a id="hide" class="hide"  href="#">
				<h:graphicImage url="/images/expand.gif" alt="" /><h:outputText   value="#{msgs.cdfm_full_description}"/>
			</a></p>
				
			<div class="textPanel toggle"  id="toggle">
				<mf:htmlShowArea hideBorder="true" id="topic_fullDescription"  value="#{ForumTool.selectedTopic.topic.extendedDescription}"/>
				
				<div class="table-responsive">
				<h:dataTable value="#{ForumTool.selectedTopic.attachList}" var="eachAttach" rendered="#{!empty ForumTool.selectedTopic.attachList}" styleClass="table table-hover table-striped table-bordered" columnClasses="attach,bogus">
				  <h:column>
					<sakai:contentTypeMap fileType="#{eachAttach.attachment.attachmentType}" mapType="image" var="imagePath" pathPrefix="/library/image/"/>									
					<h:graphicImage id="exampleFileIcon" value="#{imagePath}" alt="" />					
					</h:column>
					  <h:column>
					<h:outputLink value="#{eachAttach.url}" target="_blank">
					  <h:outputText value="#{eachAttach.attachment.attachmentName}"  style="text-decoration:underline;"/>
				    </h:outputLink>
				  </h:column>
				</h:dataTable>
				</div>
			</div>
		</div>	
		<%-- originally hidden
		   <h4><h:outputText  value="Anonymous Responses"/></h4>
		   <h:selectBooleanCheckbox   title= "#{msgs.cdfm_topic_allow_anonymous_postings}" disabled="true" value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_allow_anonymous_postings}" /> 
		   <br/>
		   <h:selectBooleanCheckbox  disabled="true" title= "#{msgs.cdfm_topic_author_identity}"  value="false" />
		   <h:outputText   value="  #{msgs.cdfm_topic_author_identity}" />
       <h4><h:outputText  value="Post Before Reading"/></h4>
    	   <p class="shorttext">
				<h:panelGrid columns="2">
					<h:panelGroup><h:outputLabel id="outputLabel5" for="topic_reading"  value="Users must post a response before reading others"/>	</h:panelGroup>
					<h:panelGroup>
						<h:selectOneRadio disabled ="true" layout="pageDirection"  id="topic_reading" value="#{ForumTool.selectedTopic.mustRespondBeforeReading}">
	    					<f:selectItem itemValue="true" itemLabel="Yes"/>
	    					<f:selectItem itemValue="false" itemLabel="No"/>
	  					</h:selectOneRadio>
					</h:panelGroup>
				</h:panelGrid>
			</p>

		  
				<div id="permissionReadOnly">	  
	  <%@ include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
				 </div> 
		  --%>	    
	  <%--
      <mf:forumHideDivision title="#{msgs.cdfm_access}" id="access_perm" hideByDefault="true">
	  	<p class="shorttext">
			<h:panelGrid columns="2" width="50%">
				<h:panelGroup><h:outputLabel id="outputLabelCont" for="contributors"  value="#{msgs.cdfm_contributors}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox disabled ="true" id="contributors"  value="#{ForumTool.selectedTopic.contributorsList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}" />
  					</h:selectManyListbox>
				</h:panelGroup>

			  <h:panelGroup><h:outputLabel id="outputLabelRead" for="readOnly"  value="#{msgs.cdfm_read_only_access}"/>	</h:panelGroup>
				<h:panelGroup>
					<h:selectManyListbox  disabled ="true" id="readOnly"  value="#{ForumTool.selectedTopic.accessorList}" size="5" style="width:200px;">
    					<f:selectItems value="#{ForumTool.totalComposeToList}"  />
  					</h:selectManyListbox>
				</h:panelGroup>
			</h:panelGrid>
		</p>
	  </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm" hideByDefault="true">
          <h:dataTable styleClass="table table-hover table-striped table-bordered" id="control_permissions" value="#{ForumTool.topicControlPermissions}" var="cntrl_settings">
   			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_role}" /></f:facet>
				<h:outputText value="#{cntrl_settings.role}"/>
			</h:column> 			 
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_new_response}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.newResponse}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_response_to_response}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.responseToResponse}"/>
			</h:column>
		    <h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_move_postings}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.movePostings}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_change_settings}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.changeSettings}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_post_to_gradebook}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{cntrl_settings.postToGradebook}"/>
			</h:column>
		</h:dataTable>
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm" hideByDefault="true">
      <h:dataTable styleClass="table table-hover table-striped table-bordered" id="message_permissions" value="#{ForumTool.topicMessagePermissions}" var="msg_settings">
   			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_role}" /></f:facet>
				<h:outputText value="#{msg_settings.role}"/>
			</h:column>
			 <h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_read}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.read}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_revise_any}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.reviseAny}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_revise_own}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.reviseOwn}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_delete_any}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.deleteAny}"/>
			</h:column>
			<h:column>
				<f:facet name="header">	<h:outputText value="#{msgs.perm_delete_own}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.deleteOwn}"/>
			</h:column>
			<h:column>
				<f:facet name="header"><h:outputText value="#{msgs.perm_mark_as_read}" /></f:facet>
				<h:selectBooleanCheckbox disabled="true" value="#{msg_settings.markAsRead}"/>
			</h:column>			 		
		</h:dataTable>	 	
      </mf:forumHideDivision>
      --%>
    
       <div class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTopicSettings}" id="revise"  
                           value="#{msgs.cdfm_button_bar_revise}" rendered="#{!ForumTool.selectedTopic.markForDeletion && !ForumTool.selectedTopic.markForDuplication}"
                           accesskey="r" styleClass="active"> 
    	 	  	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/> 
    	 	  	<f:param value="#{ForumTool.selectedForum.forum.id}" name="forumId"/>        
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopicConfirm}" id="delete_confirm" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{!ForumTool.selectedTopic.markForDeletion && !ForumTool.selectedTopic.markForDuplication}"
                           styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          <h:commandButton action="#{ForumTool.processActionDeleteTopic}" id="delete" 
                           value="#{msgs.cdfm_button_bar_delete_topic}" rendered="#{ForumTool.selectedTopic.markForDeletion}"
                           styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          
          <h:commandButton id="duplicate" action="#{ForumTool.processActionDuplicateTopic}" 
                           value="#{msgs.cdfm_duplicate_topic}" rendered="#{ForumTool.selectedTopic.markForDuplication}"
                           accesskey="s" styleClass="blockMeOnClick">
	        	<f:param value="#{ForumTool.selectedTopic.topic.id}" name="topicId"/>
          </h:commandButton>
          
          <h:commandButton immediate="true" action="#{ForumTool.processReturnToOriginatingPage}" id="cancel" 
                           value="#{msgs.cdfm_button_bar_cancel} " accesskey="x" />
         <h:outputText styleClass="messageProgress" style="display:none" value="#{msgs.cdfm_processing_submit_message}" />
       </div>
	 </h:form>
    </sakai:view>
</f:view>
