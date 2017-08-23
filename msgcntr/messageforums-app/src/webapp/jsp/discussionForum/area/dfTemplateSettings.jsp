<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messageforums.bundle.Messages"/>
</jsp:useBean>

<f:view>
   <sakai:view title="#{msgs.cdfm_default_template_settings}" toolCssHref="/messageforums-tool/css/msgcntr.css">           
      <h:form id="revise">
        <script type="text/javascript">includeLatestJQuery("msgcntr");</script>
		<sakai:script contextBase="/messageforums-tool" path="/js/datetimepicker.js"/>             		             		
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/permissions_header.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/forum.js"/>
		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
		<link href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" rel="stylesheet" type="text/css" />
		<script type="text/javascript" src="/library/js/lang-datepicker/lang-datepicker.js"></script>

<%
	  	String thisId = request.getParameter("panel");
  		if (thisId == null) 
  		{
    		thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
  		}
	%>
	<script type="text/javascript">
		function resize(){
  			mySetMainFrameHeight('<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
  		}
	</script> 
<script type="text/javascript">
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


        <sakai:tool_bar_message value="#{msgs.cdfm_default_template_settings}" />
		 		<div class="instruction">
		  		  <h:outputText id="instruction" value="#{msgs.cdfm_default_template_settings_instruction}"/>
				</div>
				<h:messages styleClass="messageAlert" id="errorMessages" rendered="#{! empty facesContext.maximumSeverity}" />
				<h4><h:outputText  value="#{msgs.cdfm_forum_posting}" /></h4>
				<h:panelGrid columns="1" columnClasses="shorttext,checkbox">
				  <h:panelGroup><h:outputText id="outputLabel4"   value="#{msgs.cdfm_moderate_forums}"/>	</h:panelGroup>
				  <h:panelGroup>
					  <h:selectOneRadio layout="pageDirection" disabled="#{not ForumTool.editMode}" id="moderated"  value="#{ForumTool.template.moderated}"
					  	onclick="javascript:disableOrEnableModeratePerm();">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				  </h:panelGroup>
				  <h:panelGroup><h:outputText id="outputLabel15"   value="#{msgs.cdfm_postFirst}"/>	</h:panelGroup>
				  <h:panelGroup>
					  <h:selectOneRadio layout="pageDirection" disabled="#{not ForumTool.editMode}" id="postFirst"  value="#{ForumTool.template.postFirst}">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				  </h:panelGroup>
			  </h:panelGrid>
			  
			  <h4><h:outputText  value="#{msgs.cdfm_forum_availability}" /></h4>
            <h:panelGrid columns="1" columnClasses="longtext,checkbox">
              <h:panelGroup>
                 <h:selectOneRadio layout="pageDirection" onclick="this.blur()" onchange="setDatesEnabled(this);" disabled="#{not ForumTool.editMode}" id="availabilityRestricted"  value="#{ForumTool.template.availabilityRestricted}">
                  <f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_forum_avail_show}"/>
                  <f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_forum_avail_date}" />
               </h:selectOneRadio>
               </h:panelGroup>
               <h:panelGroup id="openDateSpan" styleClass="indnt2 openDateSpan" style="display: #{ForumTool.template.availabilityRestricted ? '' : 'none'}">
               	   <h:outputText value="#{msgs.openDate}: "/>
	               <h:inputText id="openDate" styleClass="openDate" value="#{ForumTool.template.openDate}"/>
	               

              	</h:panelGroup>
              	<h:panelGroup id="closeDateSpan" styleClass="indnt2 closeDateSpan" style="display: #{ForumTool.template.availabilityRestricted ? '' : 'none'}">
              		<h:outputText value="#{msgs.closeDate}: "/>
	               <h:inputText id="closeDate" styleClass="closeDate" value="#{ForumTool.template.closeDate}"/>
	               

              	</h:panelGroup>              	
           </h:panelGrid>

           
            <script type="text/javascript">
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


				<h4><h:outputText  value="#{msgs.cdfm_forum_mark_read}" /></h4>
				<h:panelGrid columns="1" columnClasses="shorttext,checkbox" style="margin-bottom:1em">
				  <h:panelGroup><h:outputLabel id="outputLabel5" for="autoMarkThreadsRead"  value="#{msgs.cdfm_auto_mark_threads_read}"/>	</h:panelGroup>
				  <h:panelGroup>
					  <h:selectOneRadio layout="pageDirection" disabled="#{not ForumTool.editMode}" id="autoMarkThreadsRead" 
					    value="#{ForumTool.template.autoMarkThreadsRead}" onclick="javascript:disableOrEnableModeratePerm();">
    					<f:selectItem itemValue="true" itemLabel="#{msgs.cdfm_yes}"/>
    					<f:selectItem itemValue="false" itemLabel="#{msgs.cdfm_no}"/>
  					</h:selectOneRadio>
				  </h:panelGroup>
			    </h:panelGrid>
			  
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
