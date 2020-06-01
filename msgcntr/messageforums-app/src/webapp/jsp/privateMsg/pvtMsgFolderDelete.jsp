<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<f:view>

    <sakai:view title="#{msgs.pvt_delcon}">
    <!--jsp/privateMsg/pvtMsgFolderDelete.jsp-->
    <h:form id="pvtMsgFolderDelete">
        <script>includeLatestJQuery("msgcntr");</script>
        <script src="/messageforums-tool/js/sak-10625.js"></script>
        <script src="/messageforums-tool/js/messages.js"></script>
        <script>
            $(document).ready(function() {
                var menuLink = $('#messagesMainMenuLink');
                var menuLinkSpan = menuLink.closest('span');
                menuLinkSpan.addClass('current');
                menuLinkSpan.html(menuLink.text());
            });
        </script>
        <%@ include file="/jsp/privateMsg/pvtMenu.jsp" %>
        <sakai:tool_bar_message value="#{msgs.pvt_delcon}" />
        <h:outputText styleClass="sak-banner-warn" value="#{msgs.pvt_delete_folder_confirm}" />

        <table class="table table-striped table-bordered table-hover">
             <thead>
                 <tr>
                     <td><h:outputText value="#{msgs.pvt_folder_title}"/></td>
                     <td><h:outputText value="#{msgs.pvt_num_messages}"/></td>
                 </tr>
             </thead>
             <tbody>
                 <tr>
                     <td><h:outputText value="#{PrivateMessagesTool.selectedTopicTitle}" /></td>
                     <td><h:outputText value=" #{PrivateMessagesTool.totalMsgInFolder} #{msgs.pvt_lowercase_msgs} " /></td>
                 </tr>
             </tbody>
        </table>
          
        <sakai:button_bar>
            <h:commandButton action="#{PrivateMessagesTool.processPvtMsgFldDelete}" value="#{msgs.pvt_delete}" accesskey="s" styleClass="active"/>
            <h:commandButton action="#{PrivateMessagesTool.processPvtMsgReturnToFolderView}" value="#{msgs.pvt_cancel}" accesskey="x" />
        </sakai:button_bar>

    </h:form>

    </sakai:view>
</f:view>

