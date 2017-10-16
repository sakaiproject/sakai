<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<!--jsp/discussionForum/area/dfViewAllRanks.jsp-->
<f:view>
<sakai:view title="View All Ranks">

<link rel="stylesheet" href="/messageforums-tool/css/msgcntr.css" type="text/css" />
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<link rel="stylesheet" href="/messageforums-tool/css/forum_rank.css" type="text/css" />

<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
<script type="text/javascript" src="/messageforums-tool/js/json2.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/fluidframework-min.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/Scroller.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/forum.js"></script>


    <h:form id="addRank">
        <f:verbatim><br/></f:verbatim>
		<h:outputText value="#{msgs.ranks}"  styleClass="title-text"/>
		<h:outputText value="- #{msgs.ranks_desc}" />
		<f:verbatim><br/></f:verbatim>
		<h:graphicImage id="iamgespacer" url="/images/ranksSpaceFiller.jpg" height="10"/>
		<sakai:tool_bar separator="#{msgs.cdfm_toolbar_separator}" >
			<sakai:tool_bar_item value="#{msgs.add_rank}" action="#{ForumTool.processActionAddRank}" rendered="#{ForumTool.instructor}" />
		</sakai:tool_bar>
        <f:verbatim><br/></f:verbatim>

        <%-- ranks --%>
		<h:outputText value="#{msgs.no_ranks_defined}" rendered="#{empty ForumTool.rankBeanList}" />
        <div class="table-responsive">
            <h:dataTable value="#{ForumTool.rankBeanList}" var="eachrank" rendered="#{!empty ForumTool.rankBeanList}" summary="layout"
                         styleClass="table table-hover table-striped table-bordered" border="0" cellpadding="3" cellspacing="0"
                         columnClasses="ranktable-name,ranktable-image,ranktable-assignto,ranktable-minpost,ranktable-delete">
                <h:column id="_checkbox">
                    <f:facet name="header">
                        <h:outputText value="#{msgs.rank_title}" />
                    </f:facet>
                    <h:commandLink id="editRank" action="#{ForumTool.processActionEditRank}" immediate="true">
                        <h:outputText value="#{eachrank.rank.title}" />
                        <f:param value="#{eachrank.rank.id}" name="rankId" />
                    </h:commandLink>
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.rank_image}" />
                    </f:facet>
                    <h:graphicImage id="image" alt="#{eachrank.rank.rankImage.attachmentName}"
                        title="#{eachrank.rank.rankImage.attachmentName} (#{eachrank.rank.rankImage.attachmentSizeInKB} #{msgs.imagesize_unit}) "
                        url="#{eachrank.rank.rankImage.attachmentUrl}" width="25" height="25" rendered="#{!(eachrank.rank.rankImage eq null)}">
                    </h:graphicImage>
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.assigned_to}" />
                    </f:facet>
                    <h:outputText value="#{eachrank.rank.assignToDisplay}" rendered="#{eachrank.rank.type == 1}" />
                    <h:outputText value="#{msgs.rank_na}" rendered="#{eachrank.rank.type == 2}" />
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.min_posts}" />
                    </f:facet>
                    <h:outputText value="#{eachrank.rank.minPosts}" rendered="#{eachrank.rank.type == 2}" />
                    <h:outputText value="#{msgs.rank_na}" rendered="#{eachrank.rank.type == 1}" />
                </h:column>
                <h:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.cdfm_button_bar_delete}" />
                    </f:facet>
                    <h:selectManyCheckbox onclick="checkUpdate()" onkeypress="checkUpdate()" id="removeCheckbox" value="#{ForumTool.deleteRanks}">
                        <f:selectItem itemValue="#{eachrank.rank.id}" itemLabel="" />
                    </h:selectManyCheckbox>
                </h:column>
            </h:dataTable>
        </div>
            <f:verbatim><br/></f:verbatim>
		    <h:graphicImage id="iamgespacer" url="/images/ranksSpaceFiller.jpg" height="20"/>
			<div class="act">
          		<h:commandButton type="submit" id="delete_submit" immediate="true" rendered="#{! empty ForumTool.rankBeanList}" action="#{ForumTool.processActionConfirmDeleteRanks}" value="#{msgs.delete_rank}" styleClass="active" />
			    <h:commandButton action="#{ForumTool.processActionHome}" type="submit" immediate="true"  id="delete_cancel" rendered="#{! empty ForumTool.rankBeanList}" value="#{msgs.cdfm_cancel}" />
			</div>

            <%
                String thisId = request.getParameter("panel");
                if (thisId == null) {
                        thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
                }
            %>
            <script type="text/javascript">
                function resize(){
                    mySetMainFrameHeight('<%=org.sakaiproject.util.Web.escapeJavascript(thisId)%>');
                }
            </script>

            <script type="text/javascript">
                resize();
                //find the anchor
                document.location.href=document.location.href + "#boldMsg";
                //Set attribute onload here to skip calling portal's setMainFrameHeight, otherwise the scroll bar will reset to go to the top. Put setFocus method here is because portal's onload has two methods. one is setMainFrameHeight, another is setFocus.
    	        document.body.setAttribute("onload", "setFocus(focus_path)");
                document.body.setAttribute("onload", "disabledButton()");
            </script>
	 </h:form>
    </sakai:view>
</f:view>
