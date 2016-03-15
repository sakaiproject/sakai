<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
  	<sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>
		<h:form id="hidden_sites_form">

                        <script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
                        <script type="text/javascript">
                                $(document).ready(function(){
                                        setupPrefsGen();
                                        fixImplicitLabeling();
                                })  
                        </script>

                        <%--h:outputText value="User ID: "/><h:inputText value="#{AdminPrefsTool.userId}" /--%>	
                        <sakai:tool_bar>
                                <%--sakai:tool_bar_item action="#{UserPrefsTool.processActionRefreshFrmNoti}" value="Refresh" /--%>
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 1}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 1}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 1}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 1}" current="false" />
                                <sakai:tool_bar_item value="#{msgs.prefs_hidden}"  rendered="#{UserPrefsTool.hidden_selection == 1}" current="true" />

                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 2}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 2}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 2}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 2}" current="false" />
                                <sakai:tool_bar_item value="#{msgs.prefs_hidden}"  rendered="#{UserPrefsTool.hidden_selection == 2}" current="true" />

                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 3}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 3}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 3}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 3}" current="false" />
                                <sakai:tool_bar_item value="#{msgs.prefs_hidden}"  rendered="#{UserPrefsTool.hidden_selection == 3}" current="true" />

                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 4}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 4}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 4}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 4}" current="false" />
                                <sakai:tool_bar_item value="#{msgs.prefs_hidden}"  rendered="#{UserPrefsTool.hidden_selection == 4}" current="true" />

                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionNotiFrmEdit}" value="#{msgs.prefs_noti_title}" rendered="#{UserPrefsTool.noti_selection == 5}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionTZFrmEdit}" value="#{msgs.prefs_timezone_title}" rendered="#{UserPrefsTool.timezone_selection == 5}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionLocFrmEdit}" value="#{msgs.prefs_lang_title}" rendered="#{UserPrefsTool.language_selection == 5}" current="false" />
                                <sakai:tool_bar_item action="#{UserPrefsTool.processActionPrivFrmEdit}" value="#{msgs.prefs_privacy}"  rendered="#{UserPrefsTool.privacy_selection == 5}" current="false" />
                                <sakai:tool_bar_item value="#{msgs.prefs_hidden}"  rendered="#{UserPrefsTool.hidden_selection == 5}" current="true" />
                        </sakai:tool_bar>

                        <h3>
                                <h:outputText value="#{msgs.prefs_hidden}" />
                                <h:panelGroup rendered="#{UserPrefsTool.hiddenUpdated}"  style="margin:0 3em;font-weight:normal">
                                        <jsp:include page="prefUpdatedMsg.jsp"/>
                                </h:panelGroup>
                        </h3>


                        <h:outputText value="#{Portal.latestJQuery}" escape="false"/>

                        <div id="reallyHideConfirm" style="display: none">
                                <h:outputText value="#{msgs.hidden_really_hide_confirm}" />
                        </div>

                        <div id="sitesByTerm" style="display: none">
                                <t:dataList id="termList" value="#{UserPrefsTool.termSites.terms}"
                                            var="term" layout="simple"
                                            rowIndexVar="counter">

                                        <f:verbatim><div class="term"></f:verbatim>

                                        <f:verbatim><div class="term-type"></f:verbatim>
                                        <h:outputText value="#{term.type}" />
                                        <f:verbatim></div></f:verbatim>

                                        <f:verbatim><h2></f:verbatim>
                                        <h:outputText value="#{term.label}" />
                                        <f:verbatim></h2></f:verbatim>

                                        <t:dataList id="termList" value="#{term.sites}"
                                                var="site" layout="simple"
                                                rowIndexVar="counter"
                                                styleClass="termSites"
                                                itemStyleClass="dataListStyle">

                                                <f:verbatim><li class="site"></f:verbatim>

                                                <f:verbatim><span class="site-id"></f:verbatim>
                                                <h:outputText value="#{site.id}" />
                                                <f:verbatim></span></f:verbatim>

                                                <f:verbatim><span class="site-title"></f:verbatim>
                                                <h:outputText value="#{site.title}" />
                                                <f:verbatim></span></f:verbatim>

                                                <f:verbatim><span class="site-short-description"></f:verbatim>
                                                <h:outputText value="#{site.shortDescription}" />
                                                <f:verbatim></span></f:verbatim>

                                                <f:verbatim></li></f:verbatim>
                                        </t:dataList>
                                        <f:verbatim></div></f:verbatim>
                                </t:dataList>
                        </div>

                        <script src="/sakai-user-tool-prefs/js/manage-hidden-sites.js"></script>

                        <div class="submit-buttons">
                                <h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processHiddenSites}" />
                                <h:commandButton accesskey="x" id="cancel" styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionHiddenFrmEdit}"></h:commandButton>
                        </div>

                        <t:inputHidden id="hiddenSites" value="#{UserPrefsTool.hiddenSites}" />
		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
