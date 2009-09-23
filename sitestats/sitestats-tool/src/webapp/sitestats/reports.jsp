<%--

    $URL:$
    $Id:$

    Copyright (c) 2006-2009 The Sakai Foundation

    Licensed under the Educational Community License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

                http://www.osedu.org/licenses/ECL-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

--%>

<%/* #####  TAGLIBS, BUNDLE, Etc  ##### */%>
<%@include file="inc/common.jsp"%>


<f:view>
<sakai:view title="#{msgs.tool_title}">

	<%/* #####  CSS  ##### */%>
	<style type="text/css">
		@import url("/sakai-sitestats-tool/sitestats/css/sitestats.css");
	</style>
	
	<%/* #####  JAVASCRIPT  ##### */%>
	<sakai:script path="/sitestats/script/common.js"/>
	<sakai:script path="/sitestats/script/reports.js"/>
	<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/inputDate.js"/>		
	<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar1.js"/>		
	<sakai:script contextBase="/sakai-jsf-resource" path="/inputDate/calendar2.js"/>

	<%/* #####  FACES MESSAGES  ##### */%>
	<f:subview id="allowed" rendered="#{!ServiceBean.allowed}">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>
	

	<h:form id="reportsFormPre" rendered="#{ServiceBean.allowed}">		
		<%/* #####  MENU  ##### */%>
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="ReportsBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
	    </h:panelGroup>
	    
	    <%/* #####  INFORMATION ABOUT LAST UPDATE ##### */%>
		<f:subview id="additional-info">
			<%@include file="inc/additional-info.jsp"%>
		</f:subview>
	    
		
		<%/* #####  TITLE  ##### */%>
		<t:htmlTag value="h2">
			<h:outputText value="#{msgs.menu_reports} (#{ServiceBean.siteTitle})" rendered="#{ServiceBean.adminView}"/>
			<h:outputText value="#{msgs.menu_reports}" rendered="#{!ServiceBean.adminView}"/>
		</t:htmlTag>
	</h:form>
	
	
	<%/* #####  FACES MESSAGES: General ##### */%>
	<f:subview id="msg" rendered="#{ReportsBean.messages != null}">
	    <h:message for="msg" infoClass="success" fatalClass="alertMessage" style="margin-top: 15px;" showDetail="true" />
	</f:subview>
	
	
	<h:form id="reportsForm" rendered="#{ServiceBean.allowed}">    
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">			
			
			<%/* #####  WHAT?  ##### */%>
			<t:htmlTag value="h4" styleClass="summaryHeader">
				<t:graphicImage value="/sitestats/images/silk/icons/application_view_tile.png" border="0"/>		                
				<h:outputText value="#{msgs.report_what}"/>
			</t:htmlTag>		
			<t:htmlTag value="p" styleClass="instruction">
				<h:outputText value="#{msgs.report_what_instructions}"/>
			</t:htmlTag>
			<t:selectOneRadio id="what" value="#{ReportsBean.params.what}" layout="spread" immediate="true" onclick="checkWhatSelection();">
		    	<f:selectItem id="what-visits" itemValue="what-visits" itemLabel="#{msgs.report_what_visits}" />
		    	<f:selectItem id="what-events" itemValue="what-events" itemLabel="#{msgs.report_what_events}" />
		    	<f:selectItem id="what-resources" itemValue="what-resources" itemLabel="#{msgs.report_what_resources}" />
		    </t:selectOneRadio>	
			<t:selectOneRadio id="what-events-by" value="#{ReportsBean.params.whatEventSelType}" layout="spread" immediate="true" onclick="checkWhatSelection();">
		    	<f:selectItem id="what-events-bytool" itemValue="what-events-bytool" itemLabel="#{msgs.report_what_events_bytool}" />
		    	<f:selectItem id="what-events-byevent" itemValue="what-events-byevent" itemLabel="#{msgs.report_what_events_byevent}" />
		    </t:selectOneRadio>	     
		       
			<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="halfSize,halfSize">
				<%-- Visits --%>
				<t:htmlTag id="what-visits-option" value="span" style="#{not ServiceBean.siteVisitsEnabled? 'display:none': ''}">
					<t:radio index="0" for="what"/>
				</t:htmlTag>
                <t:outputText  id="what-visits-spacer" value="" style="#{not ServiceBean.siteVisitsEnabled? 'display:none': ''}" />
				
				<%-- Events --%>
				<t:div>
					<t:radio index="1" for="what"/>
					<t:div id="what-events-by-selectionRadio" styleClass="indnt2" style="display: none">
						<t:radio index="0" for="what-events-by"/>
						<t:div>
							<t:radio index="1" for="what-events-by"/>
							<t:outputText value="  (#{msgs.report_what_fine_grained_selection})" styleClass="instruction"/>
						</t:div>
					</t:div>
				</t:div>
				<t:div id="what-selectionPanel">			
	          		<t:selectManyListbox id="what-tools-select" value="#{ReportsBean.params.whatToolIds}" onchange="checkWhatSelection();"
						immediate="true" style="width: 250px; display: none; overflow: auto;" size="7"
						title="#{msgs.report_multiple_sel_instruction}">
						<f:selectItems value="#{ReportsBean.tools}"/>
					</t:selectManyListbox>	
					<t:selectManyListbox id="what-events-select" value="#{ReportsBean.params.whatEventIds}" onchange="checkWhatSelection();"
						immediate="true" style="width: 250px; display: none" size="7"
						title="#{msgs.report_multiple_sel_instruction}">
						<f:selectItems value="#{ReportsBean.events}"/>
					</t:selectManyListbox>
				</t:div>
				
				<%-- Resources --%>
				<t:radio index="2" for="what"/>
				<t:outputText value=""/>
				<t:div id="what-resourcesPanel1" styleClass="indnt2" style="display: none;" >
					<t:selectBooleanCheckbox id="what-resources-action-check" value="#{ReportsBean.selectedLimitedAction}"
						onchange="checkWhatSelection()">
					</t:selectBooleanCheckbox>
					<t:outputLabel for="what-resourcesAction" value="#{msgs.report_what_resource_action}"/>
				</t:div>
				<t:selectOneMenu id="what-resourcesAction" value="#{ReportsBean.params.whatResourceAction}" 
					immediate="true" style="display: none">
					<f:selectItems value="#{ReportsBean.resourceActions}"/>
				</t:selectOneMenu>
				<a4j:outputPanel id="what-resourcesPanel2" layout="block" style="display: none;" styleClass="indnt2">
					<t:selectBooleanCheckbox id="what-resources-check" value="#{ReportsBean.selectedLimitedActivity}"
						onchange="checkWhatSelection()">
						<a4j:support actionListener="#{ReportsBean.processLoadResources}"
							event="onclick" reRender="what-resources-select" 
							oncomplete="checkWhatSelection()" status="what-resources-status"
							rendered="#{!ReportsBean.resourcesLoaded}"/>
					</t:selectBooleanCheckbox>
					<t:outputLabel id="what-resources-label" for="what-resources-check" value="#{msgs.report_what_sel_resources}"/>
					<a4j:status id="what-resources-status" startText="..." stopText=" " startStyleClass="ajaxLoading"/>
				</a4j:outputPanel>
				<a4j:region id="what-resourcesRegion2">	
					<t:div id="what-resources-select-container" style="overflow:auto; width: 304px; height: 147px; display:none">
						<t:selectManyListbox id="what-resources-select" value="#{ReportsBean.params.whatResourceIds}" immediate="true"  
							size="8" style="width:304px"
							disabled="#{!ReportsBean.selectedLimitedActivity}"
							title="#{msgs.report_multiple_sel_instruction}" >
							<f:selectItems value="#{ReportsBean.resources}"/>
						</t:selectManyListbox>
					</t:div>
				</a4j:region>							
				 
			</h:panelGrid>
			
			
			<%/* #####  WHEN?  ##### */%>
			<t:htmlTag value="h4" styleClass="summaryHeader">
				<t:graphicImage value="/sitestats/images/silk/icons/date.png" border="0"/>	
				<h:outputText value="#{msgs.report_when}"/>
			</t:htmlTag>
			<t:htmlTag value="p" styleClass="instruction">
				<h:outputText value="#{msgs.report_when_instructions}"/>
			</t:htmlTag>
			<t:selectOneRadio id="when" value="#{ReportsBean.params.when}" layout="spread" immediate="true" onclick="checkWhenSelection();">
            	<f:selectItem itemValue="when-all" itemLabel="#{msgs.report_when_all}" />
                <f:selectItem itemValue="when-last7days" itemLabel="#{msgs.report_when_last7days}" />
                <f:selectItem itemValue="when-last30days" itemLabel="#{msgs.report_when_last30days}" />
                <f:selectItem itemValue="when-custom" itemLabel="#{msgs.report_when_custom}" />
            </t:selectOneRadio>
			<h:panelGrid styleClass="sectionContainerNav" columns="2" columnClasses="halfSize,halfSize" style="width: 100%">
				<t:radio index="0" for="when"/>
				<t:radio index="1" for="when"/>
				<t:radio index="3" for="when"/>
				<t:radio index="2" for="when"/>
            </h:panelGrid>
			<h:panelGrid id="when-customPanel" columns="2"  style="width: auto" styleClass="indnt2">
				<h:outputLabel value="#{msgs.report_when_from_date}" for="when-custom-from"/>
				<sakai:input_date showDate="true" showTime="true" id="when-custom-from"
					value="#{ReportsBean.params.whenFrom}">
				</sakai:input_date>
				<h:outputLabel value="#{msgs.report_when_to_date}" for="when-custom-to"/>
				<sakai:input_date showDate="true" showTime="true" id="when-custom-to"
					value="#{ReportsBean.params.whenTo}">
				</sakai:input_date>
			</h:panelGrid>
			
			
			<%/* #####  WHO?  ##### */%>
			<t:htmlTag value="h4" styleClass="summaryHeader">
				<t:graphicImage value="/sitestats/images/silk/icons/user_suit.png" border="0"/>	
				<h:outputText value="#{msgs.report_who}"/>
			</t:htmlTag>
			<t:htmlTag value="p" styleClass="instruction">
				<h:outputText value="#{msgs.report_who_instructions}"/>
			</t:htmlTag>
			<t:selectOneRadio id="who" value="#{ReportsBean.params.who}" layout="spread" immediate="true" onclick="checkWhoSelection();">
            	<f:selectItem itemValue="who-all" itemLabel="#{msgs.report_who_all}" />
                <f:selectItem itemValue="who-role" itemLabel="#{msgs.report_who_role}" />
                <f:selectItem itemValue="who-groups" itemLabel="#{msgs.report_who_group}" />
                <f:selectItem itemValue="who-custom" itemLabel="#{msgs.report_who_custom}" />
                <f:selectItem itemValue="who-none" itemLabel="#{msgs.report_who_none}" />
            </t:selectOneRadio>
			<h:panelGrid styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="halfSize,halfSize">
				<t:radio index="0" for="who"/>
				<t:outputText value=""/>
				<t:radio index="1" for="who"/>
				<t:selectOneMenu id="who-role-select" value="#{ReportsBean.params.whoRoleId}" immediate="true" style="display: none;">
					<f:selectItems value="#{ReportsBean.roles}"/>
				</t:selectOneMenu>
				<t:radio index="2" for="who" />
				<t:selectOneMenu id="who-groups-select" value="#{ReportsBean.params.whoGroupId}" immediate="true" style="display: none;"
					disabled="#{ReportsBean.siteWithNoGroups}">
					<f:selectItems value="#{ReportsBean.groups}"/>
				</t:selectOneMenu>
				<a4j:outputPanel id="who-custom-ajax">
                	<a4j:support actionListener="#{ReportsBean.processLoadUsers}"
                    	event="onclick"
                        reRender="who-custom-select"
                        rendered="#{!ReportsBean.usersLoaded}" status="who-custom-status"
                        oncomplete="checkWhoSelection(); setMainFrameHeightNoScroll(window.name);"/>
                        <t:radio index="3" for="who"/>
                        <a4j:status id="who-custom-status" startText="..." stopText=" " startStyleClass="ajaxLoading"/>
				</a4j:outputPanel>
				<a4j:region id="who-custom-selectRegion">
					<t:div>
						<t:selectManyListbox id="who-custom-select" value="#{ReportsBean.params.whoUserIds}"
							immediate="true" style="width: 304px; display: none" size="7"
							title="#{msgs.report_multiple_sel_instruction}">
							<f:selectItems value="#{ReportsBean.users}"/>
						</t:selectManyListbox>				
					</t:div>
				</a4j:region>
				<t:div>
					<t:radio index="4" for="who"/>
                    <t:outputText value="  (#{msgs.report_who_not_match})" styleClass="instruction"/>
                </t:div>
                <t:outputText value=""/>		
            </h:panelGrid>
			
			
			<%/* #####  BUTTONS  ##### */%>
			<sakai:button_bar>
				<h:commandButton value="#{msgs.report_generate}" action="#{ReportsBean.processGenerateReport}" styleClass="active"/> 
			</sakai:button_bar>
			
		</t:div>
	    
	</h:form>
	
	
	<f:subview id="hideVisits" rendered="#{!ServiceBean.siteVisitsEnabled}">
        <f:verbatim>
            <script type="text/javascript">
                document.getElementById('reportsForm:what-visits-option').style.display = 'none';
                document.getElementById('reportsForm:what-visits-spacer').style.display = 'none';
                document.getElementsByName('reportsForm:what')[1].checked = true;
            </script>
        </f:verbatim>
    </f:subview>	
	
    <f:verbatim>
		<script type="text/javascript">
			checkWhatSelection();
			checkWhenSelection();
			checkWhoSelection();
		</script>
	</f:verbatim>
</sakai:view>
</f:view>