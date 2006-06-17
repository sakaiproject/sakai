<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>

<f:view>
   <sakai:view>
      <link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />                 
      <h:form id="revise">
        <sakai:tool_bar_message value="#{msgs.cdfm_default_template_settings}" />
 		<div class="instruction">
  		  <h:outputText id="instruction"  value="#{msgs.cdfm_default_template_settings_instruction}"/>
		</div>
		 <%@include file="/jsp/discussionForum/permissions/permissions_include.jsp"%>
        <p class="act">
          <h:commandButton action="#{ForumTool.processActionReviseTemplateSettings}" value="#{msgs.cdfm_button_bar_revise}" rendered="#{not ForumTool.editMode}"/>            
          <h:commandButton action="#{ForumTool.processActionSaveTemplateSettings}" onclick="form.submit;" value="#{msgs.cdfm_button_bar_save_setting}" rendered="#{ForumTool.editMode}"/>
<%--          <h:commandButton action="#{ForumTool.processActionRestoreDefaultTemplate}" value="Restore Defaults" rendered="#{ForumTool.editMode}"/>--%>
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
	  </h:form>
    </sakai:view>
</f:view>
