<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<f:loadBundle basename="org.sakaiproject.tool.messageforums.bundle.Messages" var="msgs"/>
<link href='/sakai-messageforums-tool/css/msgForums.css' rel='stylesheet' type='text/css' />
<f:view>
   <sakai:view>
      <h:form id="forum_revise_settings">
      <sakai:script contextBase="/sakai-jsf-resource" path="/hideDivision/hideDivision.js"/>
        <sakai:tool_bar_message value="Default Setting Template" />
 			 <div class="instruction">
  			    <h:outputText id="instruction"  value="Settings from this template will apply each time a new Forum or Topic is created. You can override these settings for a
specific Forum or Topic after it has been created."/>
			 </div>
 
      <mf:forumHideDivision title="#{msgs.cdfm_control_permissions}" id="cntrl_perm">
      </mf:forumHideDivision>
      <mf:forumHideDivision title="#{msgs.cdfm_message_permissions}" id="msg_perm">
      </mf:forumHideDivision>
      
      <p class="act">
          <h:commandButton action="#{ForumTool.processActionSaveForumSettings}" value="#{msgs.cdfm_button_bar_save_setting}"/> 
          <h:commandButton action="#{ForumTool.processActionSaveForumAsDraft}" value="Restore Defaults"/> 
          <h:commandButton immediate="true" action="#{ForumTool.processActionHome}" value="#{msgs.cdfm_button_bar_cancel}" />
       </p>
       
	 </h:form>
    </sakai:view>
</f:view>
