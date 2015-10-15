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
   <sakai:view title="#{msgs.watch_forums_options}" toolCssHref="/messageforums-tool/css/msgcntr.css">           
      <h:form id="revise">
        		<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
       		<sakai:script contextBase="/messageforums-tool" path="/js/sak-10625.js"/>
       		<sakai:script contextBase="/messageforums-tool" path="/js/messages.js"/>
<!--jsp/discussionForum/area/dfWatchSettings.jsp-->
        <sakai:tool_bar_message value="#{msgs.watch_forums_options}" />
				<p class="instruction">
					<h:outputText escape="false" value="#{msgs.watch_forums_options_instruction}" />
				</p>
				  <h:panelGroup>
					  <h:selectOneRadio layout="pageDirection" id="emailNotificationOption"  value="#{ForumTool.watchSettingsBean.emailNotification.notificationLevel}">
    					<f:selectItem itemValue="2" itemLabel="#{msgs.notify_for_all_postings}"/>
    					<f:selectItem itemValue="1" itemLabel="#{msgs.notify_for_postings_to_my_thread}"/>
    					<f:selectItem itemValue="0" itemLabel="#{msgs.notify_none}"/>
  					</h:selectOneRadio>
				  </h:panelGroup>

        <div class="act">
          <h:commandButton action="#{ForumTool.processActionSaveEmailNotificationOption}" 
                           value="#{msgs.cdfm_button_bar_save_setting}" 
                           accesskey="r"
						   styleClass="active"/>            

          <h:commandButton action="#{ForumTool.processActionHome}" 
                           value="#{msgs.cdfm_button_bar_cancel}"
                           accesskey="x" />
       </div>
	  </h:form>
    </sakai:view>
</f:view>
