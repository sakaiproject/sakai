<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf2/sakai" prefix="sakai" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
    <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>
<f:view><sakai:view title="#{msgs.pvt_publish_to_faq} #{msgs.pvt_rcvd_msgs}">
    <!--jsp/privateMsg/pvtMsgPublishToFaqEdit.jsp-->
    <h:form id="pvtPublishToFaqEdit">
        <script>includeLatestJQuery("msgcntr");</script>
        <script src="/messageforums-tool/js/sak-10625.js"></script>
        <script src="/messageforums-tool/js/messages.js"></script>
        <script>
            $(document).ready(function () {
                var menuLink = $('#messagesMainMenuLink');
                var menuLinkSpan = menuLink.closest('span');
                menuLinkSpan.addClass('current');
                menuLinkSpan.html(menuLink.text());
            });
        </script>

        <%@ include file="/jsp/privateMsg/pvtMenu.jsp" %>

        <%-- BREADCRUMBS START --%>
        <f:verbatim><div class="breadCrumb specialLink"><h3></f:verbatim>
            <h:panelGroup rendered="#{PrivateMessagesTool.messagesandForums}" >
                <h:commandLink action="#{PrivateMessagesTool.processActionHome}" value="#{msgs.cdfm_message_forums}" title="#{msgs.cdfm_message_forums}"/>
                <h:outputText value="#{PrivateMessagesTool.BREADCRUMB_SEPARATOR}" />
            </h:panelGroup>

            <h:commandLink action="#{PrivateMessagesTool.processActionPrivateMessages}" value="#{msgs.pvt_message_nav}" title=" #{msgs.cdfm_message_forums}"/>

            <h:outputText value="#{PrivateMessagesTool.BREADCRUMB_SEPARATOR}" />

            <h:commandLink action="#{PrivateMessagesTool.processDisplayForum}" value="#{msgs.pvt_received}" title="#{msgs.pvt_received}" />

            <h:outputText value="#{PrivateMessagesTool.BREADCRUMB_SEPARATOR}" />

            <h:outputText value="#{msgs.pvt_publish_to_faq}" />	
        <f:verbatim></h3></div></f:verbatim>
        <%-- BREADCRUMBS START --%>

        <%-- ERROR MESSAGE --%>
        <h:messages layout="table" styleClass="sak-banner-error" rendered="#{! empty facesContext.maximumSeverity}"/>

        <%-- CONTENT START --%>

        <h:panelGroup styleClass="form-control-required mb-3" layout="block">
            <h:outputLabel for="title" styleClass="form-label">
                <h:outputText value="#{msgs.pvt_title}" />
            </h:outputLabel>
            <h:inputText id="title" value="#{mfPublishToFaqBean.title}" styleClass="form-control" >
                <f:validateLength maximum="255" />
            </h:inputText>
        </h:panelGroup>
        <h:panelGroup styleClass="form-control-required mb-3" layout="block">
            <h:outputLabel for="question" styleClass="form-label">
                <h:outputText value="#{msgs.pvt_question}" />
            </h:outputLabel>
            <sakai:inputRichText id="question" value="#{mfPublishToFaqBean.question}" textareaOnly="#{PrivateMessagesTool.mobileSession}"
                    rows="#{ForumTool.editorRows}" />
        </h:panelGroup>
        <h:panelGroup styleClass="mb-3" layout="block" rendered="#{mfPublishToFaqBean.canReply}">
            <h:outputLabel for="answer" value="#{msgs.pvt_answer}" styleClass="form-label" />
            <sakai:inputRichText id="answer" value="#{mfPublishToFaqBean.answer}" textareaOnly="#{PrivateMessagesTool.mobileSession}"
                    rows="#{ForumTool.editorRows}" />
            <h:panelGroup styleClass="form-text" layout="block">
                <h:outputText value="#{msgs.pvt_answer_info}" />
            </h:panelGroup>
        </h:panelGroup>

        <sakai:button_bar>
            <h:commandButton action="#{PrivateMessagesTool.processPvtMsgPublishToFaq}" value="#{msgs.pvt_publish_to_faq}" accesskey="s" styleClass="active"/>
            <h:commandButton action="#{PrivateMessagesTool.processPvtMsgCancelToDetailView}" value="#{msgs.pvt_cancel}" accesskey="x" />
        </sakai:button_bar>

        <%-- CONTENT END --%>

    </h:form>
</sakai:view></f:view>
