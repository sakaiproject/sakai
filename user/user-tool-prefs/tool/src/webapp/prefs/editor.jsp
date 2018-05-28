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
		<h:form id="ckeditor_form">

                        <h:outputText value="#{Portal.latestJQuery}" escape="false"/>

                        <script type="text/javascript" src="/sakai-user-tool-prefs/js/prefs.js">// </script>
                        <script type="text/javascript" src="/library/js/spinner.js"></script>
                        <script type="text/javascript">
                                $(document).ready(function(){
                                        setupPrefsGen();
                                        fixImplicitLabeling();
                                })  
                        </script>

						<c:set var="cTemplate" value = "editor" scope="session"/>

						<%@ include file="toolbar.jspf"%>

                        <t:div rendered="#{UserPrefsTool.editorUpdated}">
                                <jsp:include page="prefUpdatedMsg.jsp"/>
                        </t:div>

                        <h3 style="display: inline-block;">
                                <h:outputText rendered="#{UserPrefsTool.prefShowTabLabelOption==true}" value="#{msgs.prefs_editor_tab_options}" />
                        </h3>

                        <p class="instruction"><h:outputText value="#{msgs.editor_prompt}"  rendered="#{UserPrefsTool.prefShowTabLabelOption==true}"/></p>
                        <h:selectOneRadio value="#{UserPrefsTool.selectedEditorType}" layout="pageDirection"  rendered="#{UserPrefsTool.prefShowTabLabelOption==true}">
                                                <f:selectItem itemValue="auto" itemLabel="#{msgs.editor_auto}"/>
                                                <f:selectItem itemValue="basic" itemLabel="#{msgs.editor_basic}"/>
                                                <f:selectItem itemValue="full" itemLabel="#{msgs.editor_full}"/>
                        </h:selectOneRadio>
                        
                        <div class="submit-buttons act">
                                <h:commandButton accesskey="s" id="submit" styleClass="active formButton" value="#{msgs.update_pref}" action="#{UserPrefsTool.processActionEditorSave}" onclick="SPNR.disableControlsAndSpin( this, null );" />
                                <h:commandButton accesskey="x" id="cancel" styleClass="formButton" value="#{msgs.cancel_pref}" action="#{UserPrefsTool.processActionEditorFrmEdit}" onclick="SPNR.disableControlsAndSpin( this, null );" />
                        </div>

		</h:form>
	</sakai:view_content>
	</sakai:view_container>
</f:view>
