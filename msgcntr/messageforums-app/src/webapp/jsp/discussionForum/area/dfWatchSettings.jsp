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
   <sakai:view title="#{msgs.watch_forums_options}" toolCssHref="/messageforums-tool/css/msgcntr.css">
      <h:form id="revise">
        		<script>includeLatestJQuery("msgcntr");</script>
       		<script src="/messageforums-tool/js/sak-10625.js"></script>
       		<script src="/messageforums-tool/js/messages.js"></script>
          <script>
              $(document).ready(function() {
                  var menuLink = $('#forumsWatchMenuLink');
                  var menuLinkSpan = menuLink.closest('span');
                  menuLinkSpan.addClass('current');
                  menuLinkSpan.html(menuLink.text());
                  $('#revise\\:emailNotificationOption1, #revise\\:emailNotificationOption2, #revise\\:emailNotificationOption3').each(function() {
                       let label = $('#revise\\:notify_for_all_postings_head').text() + " " + $(this).next().text();
                       $(this).attr('aria-label', label);
                  });
              });
          </script>
				<%@ include file="/jsp/discussionForum/menu/forumsMenu.jsp" %>
<!--jsp/discussionForum/area/dfWatchSettings.jsp-->
				<div class="page-header">
					<h1 id="notifications_options_head"><h:outputText value="#{msgs.watch_forums_options}" /></h1>
				</div>
				<div>
					<h:outputText escape="false" value="#{msgs.watch_forums_options_instruction}" />
					<h:outputText  styleClass="sak-banner-info" escape="false" value="#{msgs.watch_forums_options_instruction_note}" />
                    <f:verbatim><br/></f:verbatim>
                </div>
				<h:panelGroup>
                      <h:outputText style="display:block" id="notify_for_all_postings_head" escape="false" value="#{msgs.notify_for_all_postings_head}" />
                      <ul style="list-style: none;">
                          <li>
                            <h:selectOneRadio id="emailNotificationOption1" group="emailNotificationOption" value="#{ForumTool.watchSettingsBean.emailNotification.notificationLevel}">
                                <f:selectItem itemValue="2" itemLabel="#{msgs.notify_for_all_postings}"/>
                            </h:selectOneRadio>
                          </li>
                          <li>
                              <h:selectOneRadio id="emailNotificationOption2" group="emailNotificationOption" value="#{ForumTool.watchSettingsBean.emailNotification.notificationLevel}">
                                  <f:selectItem itemValue="1" itemLabel="#{msgs.notify_for_postings_to_my_thread}"/>
                              </h:selectOneRadio>
                          </li>
                          <li>
                                <h:selectOneRadio id="emailNotificationOption3" group="emailNotificationOption" value="#{ForumTool.watchSettingsBean.emailNotification.notificationLevel}">
                                   <f:selectItem itemValue="0" itemLabel="#{msgs.notify_none}"/>
                                </h:selectOneRadio>
                           </li>
                      </ul>
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
