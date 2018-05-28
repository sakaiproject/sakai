<%-- HTML JSF tag libary --%>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%-- Core JSF tag library --%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%-- Sakai JSF tag library --%>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<%-- Core JSTL tag library --%>
<%@ taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c" %>

<f:view>
	<sakai:view_container title="#{msgs.prefs_title}">
  	<sakai:stylesheet path="/css/prefs.css"/>
	<sakai:view_content>
		<h:form id="hidden_sites_form">

                        <h:outputText value="#{Portal.latestJQuery}" escape="false"/>

                        <script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
                        <script type="text/javascript" src="/library/js/spinner.js"></script>
                        <script type="text/javascript">
                                $(document).ready(function(){
                                        setupPrefsGen();
                                        fixImplicitLabeling();
                                })  
                        </script>

						<c:set var="cTemplate" value = "hidden" scope="session"/>

						<%@ include file="toolbar.jspf"%>

                        <h:panelGroup rendered="#{UserPrefsTool.prefShowTabLabelOption==true}">
                            <t:div rendered="#{UserPrefsTool.prefShowTabLabelOption==true and UserPrefsTool.hiddenUpdated}">
                                <jsp:include page="prefUpdatedMsg.jsp"/>
                            </t:div>
                            <t:htmlTag value="h3" rendered="#{UserPrefsTool.prefShowTabLabelOption==true}">
                                <h:outputText value="#{msgs.prefs_site_tab_display_format}" />
                            </t:htmlTag>

                            <%-- ## SAK-23895 :Display full name of course, not just code, in site tab  --%>
                            <t:div rendered="#{UserPrefsTool.prefShowTabLabelOption==true}">
                                <h:outputText value="#{msgs.tabDisplay_prompt}" rendered="#{UserPrefsTool.prefShowTabLabelOption==true}"/>
                                <h:selectOneRadio value="#{UserPrefsTool.selectedTabLabel}" layout="pageDirection" rendered="#{UserPrefsTool.prefShowTabLabelOption==true}">
                                    <f:selectItem itemValue="1" itemLabel="#{msgs.tabDisplay_coursecode}"/>
                                    <f:selectItem itemValue="2" itemLabel="#{msgs.tabDisplay_coursename}"/>
                                </h:selectOneRadio>
                            </t:div>
                        </h:panelGroup>

                        <t:div rendered="#{UserPrefsTool.prefShowTabLabelOption==false and UserPrefsTool.hiddenUpdated}">
                            <jsp:include page="prefUpdatedMsg.jsp"/>
                        </t:div>
                        <h3 style="display: inline-block;">
                                <h:outputText value="#{msgs.hidden_title}" />
                        </h3>

                        <p class="instruction"><h:outputText value="#{msgs.hidden_instructions}" escape="false" /></p>


                        <div id="reallyHideConfirm" style="display: none">
                                <h:outputText value="#{msgs.hidden_really_hide_confirm}" />
                        </div>

                        <div id="selectedTabLabelValue" style="display: none">
                                <h:outputText value="#{UserPrefsTool.selectedTabLabel}" />
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

                                                <f:verbatim><span class="site-titleFull"></f:verbatim>
                                                <h:outputText value="#{site.infoUrl}" />
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

                        <div class="submit-buttons act">
                                <h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processHiddenSites}" onclick="SPNR.disableControlsAndSpin( this, null );" />
                                <h:commandButton accesskey="x" id="cancel" styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionHiddenFrmEdit}" onclick="SPNR.disableControlsAndSpin( this, null );" />
                        </div>

                        <t:inputHidden id="hiddenSites" value="#{UserPrefsTool.hiddenSites}" />
		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
