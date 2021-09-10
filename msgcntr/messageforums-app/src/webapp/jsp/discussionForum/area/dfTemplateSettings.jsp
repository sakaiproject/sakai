<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messageforums.bundle.Messages"/>
</jsp:useBean>

<f:view>
	<sakai:view title="#{msgs.cdfm_default_template_settings}" toolCssHref="/messageforums-tool/css/msgcntr.css">
	<h:form id="revise">
        <script>includeLatestJQuery("msgcntr");</script>
		<script src="/messageforums-tool/js/datetimepicker.js"></script>             		             		
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
		<script src="/messageforums-tool/js/permissions_header.js"></script>
		<script src="/messageforums-tool/js/forum.js"></script>
		<script src="/messageforums-tool/js/messages.js"></script>
		<link href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" rel="stylesheet" type="text/css" />
		<script src="/library/js/lang-datepicker/lang-datepicker.js"></script>

<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
	%>
	<script>
		function resize(){
  			mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  		}

         $(document).ready(function() {
             var menuLink = $('#forumsTemplateSettingsMenuLink');
             var menuLinkSpan = menuLink.closest('span');
             menuLinkSpan.addClass('current');
             menuLinkSpan.html(menuLink.text());
         });

	</script> 
<script>
	function setDatesEnabled(radioButton){
		$(".openDateSpan").toggle();
		$(".closeDateSpan").toggle();
		resize();
	}

	function openDateCal(){
			NewCal('revise:openDate','MMDDYYYY',true,12, '<h:outputText value="#{ForumTool.defaultAvailabilityTime}"/>');
	}

	function closeDateCal(){
			NewCal('revise:closeDate','MMDDYYYY',true,12, '<h:outputText value="#{ForumTool.defaultAvailabilityTime}"/>');
	}
</script>

<!--jsp/discussionForum/area/dfTemplateSettings.jsp-->


				<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
				<div class="page-header">
					<h1><h:outputText value="#{msgs.cdfm_default_template_settings}" /></h1>
				</div>
		 		<div class="instruction">
		  		  <h:outputText id="instruction" value="#{msgs.cdfm_default_template_settings_instruction}"/>
				</div>
				<h:messages styleClass="messageAlert" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />

				<h2><h:outputText value="#{msgs.cdfm_forum_posting}" /></h2>
				<h3><h:outputText id="outputLabel4" value="#{msgs.cdfm_moderate_forums}" /></h3>
				<h:panelGroup layout="block" styleClass="indnt1">
					<h:panelGroup styleClass="checkbox">
						<h:selectOneRadio layout="pageDirection" disabled="#{not ForumTool.editMode}" id="moderated"  value="#{ForumTool.template.moderated}"
							onclick="javascript:disableOrEnableModeratePerm();">
							<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
							<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
						</h:selectOneRadio>
					</h:panelGroup>
				</h:panelGroup>
				<h3><h:outputText id="outputLabel15" value="#{msgs.cdfm_postFirst}"/></h3>
				<h:panelGroup layout="block" styleClass="indnt1">
					<h:panelGroup styleClass="checkbox">
						<h:selectOneRadio layout="pageDirection" disabled="#{not ForumTool.editMode}" id="postFirst"  value="#{ForumTool.template.postFirst}">
							<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
							<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
						</h:selectOneRadio>
					</h:panelGroup>
				</h:panelGroup>

				<h2><h:outputText value="#{msgs.cdfm_forum_availability}" /></h2>
				<h:panelGroup layout="block" styleClass="indnt1">
					<h:panelGroup styleClass="checkbox">
						<h:selectOneRadio layout="pageDirection" onclick="this.blur()" onchange="setDatesEnabled(this);" disabled="#{not ForumTool.editMode}" id="availabilityRestricted"  value="#{ForumTool.template.availabilityRestricted}">
							<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_forum_avail_show}"/>
							<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_forum_avail_date}" />
						</h:selectOneRadio>
					</h:panelGroup>
					<h:panelGroup id="openDateSpan" styleClass="indnt2 openDateSpan calWidget" style="display: #{ForumTool.template.availabilityRestricted ? 'block' : 'none'}">
						<h:outputLabel value="#{msgs.openDate}: " for="openDate" />
						<h:inputText id="openDate" styleClass="openDate" value="#{ForumTool.template.openDate}" />
					</h:panelGroup>
					<h:panelGroup id="closeDateSpan" styleClass="indnt2 closeDateSpan calWidget" style="display: #{ForumTool.template.availabilityRestricted ? 'block' : 'none'}">
						<h:outputLabel value="#{msgs.closeDate}: " for="closeDate" />
						<h:inputText id="closeDate" styleClass="closeDate" value="#{ForumTool.template.closeDate}" />
					</h:panelGroup>
				</h:panelGroup>

				<script>
					localDatePicker({
						input:'.openDate',
						allowEmptyDate:true,
						ashidden: { iso8601: 'openDateISO8601' },
						getval:'.openDate',
						useTime:1
					});
					localDatePicker({
						input:'.closeDate',
						allowEmptyDate:true,
						ashidden: { iso8601: 'closeDateISO8601' },
						getval:'.closeDate',
						useTime:1
					});
				</script>

				<h2><h:outputText  value="#{msgs.cdfm_forum_mark_read}" /></h2>
				<h:outputLabel id="outputLabel5" for="autoMarkThreadsRead" value="#{msgs.cdfm_auto_mark_threads_read}"/>
				<h:panelGroup layout="block" styleClass="indnt1">
					<h:panelGroup styleClass="checkbox">
						<h:selectOneRadio layout="pageDirection" disabled="#{not ForumTool.editMode}" id="autoMarkThreadsRead"
								value="#{ForumTool.template.autoMarkThreadsRead}" onclick="javascript:disableOrEnableModeratePerm();">
							<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
							<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
						</h:selectOneRadio>
					</h:panelGroup>
				</h:panelGroup>
				<br>

		 		<%@ include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
		 		
        <div class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTemplateSettings}" 
                           value="#{msgs.cdfm_button_bar_revise}" 
                           rendered="#{not ForumTool.editMode}"
                           accesskey="r"
						   styleClass="active"/>            
          <h:commandButton action="#{ForumTool.processActionSaveTemplateSettings}" 
                           onclick="form.submit;" value="#{msgs.cdfm_button_bar_save_setting}" 
                           rendered="#{ForumTool.editMode}"
                           accesskey="s" 
						   styleClass="active"/>
<%--          <h:commandButton action="#{ForumTool.processActionRestoreDefaultTemplate}" value="Restore Defaults" rendered="#{ForumTool.editMode}"/>--%>
          <h:commandButton immediate="true"
                           action="#{ForumTool.processActionCancelTemplateSettings}" 
                           value="#{msgs.cdfm_button_bar_cancel}"
                           accesskey="x" />
       </div>
	  </h:form>
    </sakai:view>
</f:view>
