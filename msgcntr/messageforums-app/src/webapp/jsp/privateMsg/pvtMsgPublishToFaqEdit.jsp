<%-- TODO Master merge:
        - Replace form-group with mb-3
        - Replace control-label with form-label
        - Make use of https://sakaiproject.atlassian.net/browse/SAK-48088
--%>

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

        <h:panelGroup styleClass="form-group" layout="block">
            <h:outputLabel for="title" styleClass="control-label">
                <h:outputText value="#{msgs.pvt_title}" />
                <h:outputText value=" #{msgs.pvt_star}" styleClass="highlight" />
            </h:outputLabel>
            <h:inputText id="title" value="#{mfPublishToFaqBean.title}" styleClass="form-control" >
                <f:validateLength maximum="255" />
            </h:inputText>
        </h:panelGroup>
        <h:panelGroup styleClass="form-group" layout="block">
            <h:outputLabel for="question" styleClass="control-label">
                <h:outputText value="#{msgs.pvt_question}" />
                <h:outputText value=" #{msgs.pvt_star}" styleClass="highlight" />
            </h:outputLabel>
            <sakai:inputRichText id="question" value="#{mfPublishToFaqBean.question}" textareaOnly="#{PrivateMessagesTool.mobileSession}"
                    rows="#{ForumTool.editorRows}" />
        </h:panelGroup>
        <h:panelGroup styleClass="form-group" layout="block" rendered="#{mfPublishToFaqBean.canReply}">
            <h:outputLabel for="answer" value="#{msgs.pvt_answer}" styleClass="control-label" />
            <sakai:inputRichText id="answer" value="#{mfPublishToFaqBean.answer}" textareaOnly="#{PrivateMessagesTool.mobileSession}"
                    rows="#{ForumTool.editorRows}" />
            <h:panelGroup styleClass="b5 form-text" layout="block">
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
