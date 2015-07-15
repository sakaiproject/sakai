<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<!--jsp/discussionForum/area/dfConfirmRemoveRanks.jsp-->
<f:view>
    <sakai:view title="Confirm Remove Ranks">
        <link rel="stylesheet" href="/messageforums-tool/css/msgcntr.css" type="text/css" />
        <link rel="stylesheet" href="/messageforums-tool/css/forum_rank.css" type="text/css" />
    
        <h:form id="revise">
            <script type="text/javascript" src="/library/js/fluid/1.5/MyInfusion.js"></script>
            <script type="text/javascript" src="/messageforums-tool/js/forum_rank.js"></script>
            <script type="text/javascript" src="/messageforums-tool/js/forum.js"></script>
    
            <sakai:tool_bar_message value="#{msgs.delete_rank}" />
            <f:verbatim><div style="padding:1em 0;"></f:verbatim>
    
            <h:outputText styleClass="messageAlert" value="- #{msgs.delete_info}" />
            <f:verbatim> </div> </f:verbatim>
    
            <%-- ranks to be deleted --%>
            <h:dataTable  value="#{ForumTool.checkedRanks}" var="eachrank" rendered="#{!empty ForumTool.checkedRanks}" 
                          summary="layout" style="font-size:.9em;width:auto;margin-left:1em" border="0" cellpadding="3" 
                          cellspacing="0">
                <h:column id="_checkbox">
                    <h:outputText value="- #{eachrank.title}"  />
                </h:column>
            </h:dataTable>
    
            <div class="act">
                <h:commandButton action="#{ForumTool.processActionDeleteRanks}" value="#{msgs.cdfm_button_bar_delete}" styleClass="active"/>
                <h:commandButton action="#{ForumTool.gotoViewRank}" value="#{msgs.cdfm_cancel}" />
            </div>
        </h:form>
    </sakai:view>
</f:view>
