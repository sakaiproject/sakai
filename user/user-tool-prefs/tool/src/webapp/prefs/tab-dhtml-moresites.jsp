<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<f:view>
    <sakai:view_container title="#{msgs.prefs_title}">
    <sakai:stylesheet path="/css/useDHTMLMore.css"/>
    <sakai:view_content>
        <h:form id="prefs_form">
                
                <sakai:tool_bar>
              <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmEdit}" value="Refresh" /--%>
            <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" />
            <sakai:tool_bar_item value="#{msgs.prefs_tab_title}" />
            <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" />
            <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}"/>
            <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}" rendered="#{UserPrefsTool.privacyEnabled}" />
        </sakai:tool_bar>
                
                <h3><h:outputText value="#{msgs.prefs_tab_title}" /></h3>
                
                <h:panelGroup rendered="#{UserPrefsTool.tabUpdated}">
                    <jsp:include page="prefUpdatedMsg.jsp"/>    
                    </h:panelGroup>
                
                <sakai:messages />
    <div id="tab-dhtml-more-sites">
    <div id="top-text">
        <h:outputText value="#{msgs.tab_inst_1_alt}"/> 
        <div>
            <h:outputText value="#{msgs.tab_inst_2_alt}"/>
        </div>
    </div>              

    <table cellspacing="0" cellpadding="5%" class="sidebyside" summary="layout">
        <tr>
            <td id="sites-active-button" class='buttoncell'>
                <h:commandLink action="#{UserPrefsTool.processActionMoveUp}" title="#{msgs.tab_move_up}"> <h:graphicImage value="prefs/Up-Arrow.gif"/> </h:commandLink>
                <br />
                <h:commandLink action="#{UserPrefsTool.processActionMoveDown}" title="#{msgs.tab_move_down}"> <h:graphicImage value="prefs/Down-Arrow.gif"/> </h:commandLink>
            </td>
            <td id="sites-active">
                <b><h:outputText value="#{msgs.tab_inst_3_alt}"/></b>
                <div class="instruction">
                    <h:outputFormat value="#{msgs.tab_inst_4_alt}">
                        <f:param value="#{UserPrefsTool.tabCount - 1 }"/>
                    </h:outputFormat> 
                </div>
								<b><h:outputText value="#{msgs.tab_count}"/></b>
								<h:inputText size="2" value="#{UserPrefsTool.tabCount}" />
                <br />
                <h:selectManyListbox value="#{UserPrefsTool.selectedOrderItems}" size="10">
                    <f:selectItems value="#{UserPrefsTool.prefOrderItems}" />
                </h:selectManyListbox>
            </td>
            <td class='buttoncell' style="text-align: center;">                     
              <h:commandButton id="add" value="#{msgs.tab_move_lone}" action="#{UserPrefsTool.processActionAdd}" title="#{msgs.tab_move_inst}"></h:commandButton>
              <br />
              <h:commandButton id="remove" value="#{msgs.tab_move_rone}" action="#{UserPrefsTool.processActionRemove}" title="#{msgs.tab_move_inst_re}"></h:commandButton>
              <br />
              <br />
              <h:commandButton id="removeAll" value="#{msgs.tab_move_rall}" action="#{UserPrefsTool.processActionRemoveAll}" title="#{msgs.tab_move_all_inst_re}"></h:commandButton>
              <br />
              <h:commandButton id="addAll" value="#{msgs.tab_move_lall}" action="#{UserPrefsTool.processActionAddAll}" title="#{msgs.tab_move_all_inst}"></h:commandButton>
            </td>
            <td id="sites-hidden">
                <b><h:outputText value="#{msgs.tab_inst_5_alt}"/>   </b>
    
                <div class="instruction">
                    <h:outputText value="#{msgs.tab_inst_6_alt}"/>  
                </div>
                <br />
          
                
                <h:selectManyListbox value="#{UserPrefsTool.selectedExcludeItems}" size="10">
                    <f:selectItems value="#{UserPrefsTool.prefExcludeItems}" />
                </h:selectManyListbox>
            </td>
        </tr>
    </table>

                <p class="act">
                    <h:commandButton accesskey="s" id="submit" styleClass="active" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionSave}"></h:commandButton>
                     <h:commandButton accesskey="x" id="cancel"  value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionCancel}"></h:commandButton>
                </p>

        </div>
        </h:form>
        <sakai:peer_refresh value="#{UserPrefsTool.refreshElement}" />
    
    </sakai:view_content>
    </sakai:view_container>
</f:view>
