<%@ page import="java.util.*, javax.faces.context.*, javax.faces.application.*,
                 javax.faces.el.*, org.sakaiproject.tool.messageforums.*"%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://sakaiproject.org/jsf/messageforums" prefix="mf" %>
<jsp:useBean id="msgs" class="org.sakaiproject.util.ResourceLoader" scope="session">
   <jsp:setProperty name="msgs" property="baseName" value="org.sakaiproject.api.app.messagecenter.bundle.Messages"/>
</jsp:useBean>

<!--jsp/discussionForum/area/dfEditRank.jsp-->
<f:view>

<sakai:view title="Forums">
<link rel="stylesheet" href="/messageforums-tool/css/msgcntr.css" type="text/css" />
<link rel="stylesheet" href="/library/webjars/jquery-ui/1.12.1/jquery-ui.min.css" type="text/css" />
<link rel="stylesheet" href="/messageforums-tool/css/forum_rank.css" type="text/css" />

<script type="text/javascript">includeLatestJQuery("msgcntr");</script>
<script type="text/javascript" src="/messageforums-tool/js/fluidframework-min.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/json2.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/Scroller.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/forum.js"></script>
<script type="text/javascript" src="/messageforums-tool/js/forum_rank.js"></script>
<script type="text/JavaScript">
    var thresholdHint='<%=msgs.getString("rank_threshold_hint")%>';
    function showThresholdHint(obj){
        if (obj.value == '') {
            obj.value=thresholdHint; 
        }
    }
    function hideThresholdHint(obj){
        if (obj.value == thresholdHint ) {
            obj.value='';
        }
    }
</script>
    <h:form enctype="multipart/form-data" id="addRank">
            <sakai:tool_bar_message value="#{msgs.edit_rank}" />

            <%-- Validation Message--%>
            <f:verbatim><div id="topAlert" style="display:none; padding:1em 0;"></f:verbatim>
            <h:outputText styleClass="messageAlert" value="#{msgs.add_validate}" />
            <f:verbatim></div></f:verbatim>

            <f:verbatim><div id="imageSizeAlert" style="display:block"></f:verbatim>
            <h:panelGrid columns="1" cellpadding="10px" rendered="#{ForumTool.forumRankBean.imageSizeErr eq 'true' }">
                <h:outputText styleClass="messageAlert" value="#{msgs.image_size_err}"  />
            </h:panelGrid>
            <f:verbatim></div></f:verbatim>

            <f:subview id="picker2">
                <%@ include file="peoplePicker.jsp"%>
            </f:subview>

            <%-- Rank Name --%>
            <h:panelGrid columns="2" columnClasses="shorttext rankeditor-col1, checkbox">
                <h:panelGroup>
                    <h:outputText id="ranktitle" value="#{msgs.rank_title}" />
                </h:panelGroup>
                <h:panelGroup>
                    <f:verbatim>
                        <div id="ranknamebox" style="padding: 0.1em 0;">
                    </f:verbatim>
                    <h:inputText id="rankname" value="#{ForumTool.forumRankBean.title}" />
                    <f:verbatim></div></f:verbatim>
                </h:panelGroup>
            </h:panelGrid>

            <h:graphicImage url="/images/hr.jpg" style="padding-top:10px;"/>

            <h:panelGrid columns="2" columnClasses="shorttext rankeditor-col1, checkbox">
                <h:panelGroup>
                    <h:outputText id="rank_assign" value="#{msgs.rank_assign}" />
                </h:panelGroup>

                <h:panelGroup>
                    <h:inputHidden id="selectedRankType" value="#{ForumTool.forumRankBean.type}" />
                    <!-- Had to do separate radio buttons because each option has additional hidden div that expands when a radio button is clicked. JSF does not seem to have an easy way to do that,  it only has f:selectItem, which displays a label only.  -->
                    <f:verbatim><table><tr><td></f:verbatim>
                    <h:selectOneRadio id="radiobtnType1" layout="pageDirection"
                        onclick="uncheckOthers(this)"
                        value="">
                        <f:selectItem itemValue="1" itemLabel="#{msgs.rank_based_on_individual}" />
                    </h:selectOneRadio>
                    <f:verbatim></td></tr></table></f:verbatim>
                    <f:verbatim><div id="type1div" style="display: none"></f:verbatim>
                    <f:subview id="picker1">
                        <%@ include file="sendToPicker.jsp"%>
                    </f:subview>
                    <h:inputHidden id="aggregate_assign_to_item_ids" value="#{ForumTool.aggregatedAssignToItemIds}" />
                    <h:inputHidden id="selected_indiv_ids" value="#{ForumTool.selectedIndividualMemberItemIds}" />

                    <f:verbatim></div></f:verbatim>
                </h:panelGroup>
                <h:panelGroup>
                    <f:verbatim><div id="rankTypeAlert" style="display:none; padding:1em 0;"></f:verbatim>
                        <h:outputText styleClass="messageAlert" value="#{msgs.rank_assign_err}" />
                    <f:verbatim> </div> </f:verbatim>
                </h:panelGroup>

                <h:panelGroup>
                    <f:verbatim><table><tr><td></f:verbatim>
                    <h:selectOneRadio id="radiobtnType2" layout="pageDirection"
                        onclick="uncheckOthers(this)"
                        value="">
                        <f:selectItem itemValue="2" itemLabel="#{msgs.rank_based_post_count}" />
                    </h:selectOneRadio>
                    <f:verbatim></td><td id="minpostbox"  valign="bottom" style="padding: 0.1em 0;" ></f:verbatim>
                    <h:inputText  id="minpost" value="#{ForumTool.forumRankBean.minPosts}" 
                                  onfocus="hideThresholdHint(this)"  
                                  onblur="showThresholdHint(this)"/>
                    <f:verbatim></td></tr></table></f:verbatim>

                    <f:verbatim><div class="addrank_msgbox"></f:verbatim>
                    <h:outputText id="addrank_note" value="#{msgs.addrank_note}" />
                    <f:verbatim> </div> </f:verbatim>
                </h:panelGroup>
            </h:panelGrid>
            
            <h:graphicImage url="/images/hr.jpg" style="padding-top:5px; padding-bottom:5px"/>

            <%--Images  --%>
            <h:panelGrid columns="2" columnClasses="shorttext rankeditor-col1, checkbox">
                <h:panelGroup>
                    <h:outputText id="rank_image" value="#{msgs.rank_image}" />
                    <f:verbatim><br/></f:verbatim>
                    <h:outputText id="optional" value="#{msgs.optional}"/>
                </h:panelGroup>

                <h:panelGroup rendered="#{(ForumTool.forumRankBean.rank.rankImage eq null) || ForumTool.imageDeletePending}" >
                <%-- setting disabled = false, does not work,  the Browse button is still disabled, so here I have to use 2 panelGroups --%>
                    <sakai:inputFileUpload id="add_attach" valueChangeListener="#{ForumTool.processUpload}"/>
                </h:panelGroup>

                <h:panelGroup rendered="#{!(ForumTool.forumRankBean.rank.rankImage eq null) && !(ForumTool.imageDeletePending)}" id ="hasImage" >
                <h:graphicImage id="image" alt="#{ForumTool.forumRankBean.rank.rankImage.attachmentName}"
                                url="#{ForumTool.forumRankBean.rank.rankImage.attachmentUrl}" width ="25" height="25" >
                </h:graphicImage>

                <h:graphicImage url="/images/ranksSpaceFiller.jpg" height="25"  width="10" />
                <h:commandButton action="#{ForumTool.processDeleteRankImage}" value="#{msgs.cdfm_remove}" styleClass="active"  immediate="true" />
                </h:panelGroup>

                <h:panelGroup>
                   <h:graphicImage url="/images/ranksSpaceFiller.jpg" width="10" height="10"/>
                </h:panelGroup>
                <h:panelGroup rendered="#{(ForumTool.forumRankBean.rank.rankImage eq null) ||  (ForumTool.imageDeletePending)}">
                    <h:outputText style="color: #404040; font-size: 95%;" value="#{msgs.image_size_info}"/>
                </h:panelGroup>
            </h:panelGrid>

            <div class="act">
                <h:commandButton
                    action="#{ForumTool.processActionUpdateRank}"
                    value="#{msgs.syn_update}" onclick="return validate(this);" 
                    styleClass="active sakai-ppkr-send-button" /> 
                    
                <h:commandButton
                    action="#{ForumTool.gotoViewRank}"
                    styleClass="active sakai-ppkr-cancel-button"
                    value="#{msgs.cdfm_cancel}" />
            </div>

            <script language="javascript" type="text/JavaScript">
<!--
    var inputhidden = document.getElementById("addRank:selectedRankType");
    var ranktype = inputhidden.getAttribute("value");
    var type1div = document.getElementById("type1div");
    var minpost= document.getElementById("addRank:minpost");
    var type1= document.getElementById("addRank:radiobtnType1");
    var type2= document.getElementById("addRank:radiobtnType2");
    
    var radio1 = type1.getElementsByTagName("INPUT")[0];
    var radio2 = type2.getElementsByTagName("INPUT")[0];
          
    if (ranktype == "1") {
        type1div.style.display="block";
        minpost.value=thresholdHint;
        minpost.disabled=true;
        
        radio1.checked = true;
        radio2.checked = false;
    }
    else {
        type1div.style.display="none";
        minpost.disabled=false;
        radio2.checked = true;
        radio1.checked = false;
    }
-->
            </script>
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
            </script>
        </h:form>
    </sakai:view>
</f:view>
