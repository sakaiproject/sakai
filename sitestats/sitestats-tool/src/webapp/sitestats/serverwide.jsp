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

	<%/* #####  FACES MESSAGES  ##### */%>
	<f:subview id="allowed" rendered="#{!ServiceBean.allowed}">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>

	<a4j:form id="serverWideReportForm" rendered="#{ServerWideReportBean.allowed}">
	    
		<%/* #####  MENU  ##### */%>
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="ServerWideReportBean">
	            <f:subview id="menu">
					<%@include file="inc/navmenu.jsp"%>
				</f:subview>
	        </t:aliasBean>
        </h:panelGroup>
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">
			<a4j:region id="reportListRegion">
				<%/* #####  REPORT SELECTORS  ##### */%>
				<a4j:outputPanel id="reportSelectors">					
					<a4j:commandLink id="reportMonthlyLoginSel" value="#{msgs.submenu_monthly_login_report}" 
						actionListener="#{ChartParams.selectMonthlyLoginReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'monthlyLogin'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportMonthlyLoginLbl" value="#{msgs.submenu_monthly_login_report}" 
						rendered="#{ChartParams.selectedReportType eq 'monthlyLogin'}" styleClass="selector"/>
					
					<t:outputText value="    |    "/>
					
					<a4j:commandLink id="reportWeeklyLoginSel" value="#{msgs.submenu_weekly_login_report}" 
						actionListener="#{ChartParams.selectWeeklyLoginReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'weeklyLogin'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportWeeklyLoginLbl" value="#{msgs.submenu_weekly_login_report}" 
						rendered="#{ChartParams.selectedReportType eq 'weeklyLogin'}" styleClass="selector"/>
					
					<t:outputText value="    |    "/>
					
					<a4j:commandLink id="reportDailyLoginSel" value="#{msgs.submenu_daily_login_report}" 
						actionListener="#{ChartParams.selectDailyLoginReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'dailyLogin'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportDailyLoginLbl" value="#{msgs.submenu_daily_login_report}" 
						rendered="#{ChartParams.selectedReportType eq 'dailyLogin'}" styleClass="selector"/>
					
					<t:outputText value="    |    "/>
					
					<a4j:commandLink id="reportRegularUsersSel" value="#{msgs.submenu_regular_users_report}" 
						actionListener="#{ChartParams.selectRegularUsersReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'regularUsers'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportRegularUsersLbl" value="#{msgs.submenu_regular_users_report}" 
						rendered="#{ChartParams.selectedReportType eq 'regularUsers'}" styleClass="selector"/>
					
					<t:outputText value="    |    "/>
					
					<a4j:commandLink id="reportHourlyUsageSel" value="#{msgs.submenu_hourly_usage_report}" 
						actionListener="#{ChartParams.selectHourlyUsageReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'hourlyUsage'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportHourlyUsageLbl" value="#{msgs.submenu_hourly_usage_report}" 
						rendered="#{ChartParams.selectedReportType eq 'hourlyUsage'}" styleClass="selector"/>
					
					<t:outputText value="    |    "/>
					
					<a4j:commandLink id="reportTopActivitiesSel" value="#{msgs.submenu_top_activities_report}" 
						actionListener="#{ChartParams.selectTopActivitiesReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'topActivities'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportTopActivitiesLbl" value="#{msgs.submenu_top_activities_report}" 
						rendered="#{ChartParams.selectedReportType eq 'topActivities'}" styleClass="selector"/>

					<t:outputText value="    |    "/>
					
					<a4j:commandLink id="reportToolSel" value="#{msgs.submenu_tool_report}" 
						actionListener="#{ChartParams.selectToolReportType}" 
						rendered="#{ChartParams.selectedReportType ne 'toolReport'}"
	                	status="reportChartStatus" reRender="reportSelectors,reportChartPanel,reportTitle,reportDescription,reportNotes" 
	                	styleClass="selector"
               			oncomplete="setReportChartRenderFalse()"/>
					<t:outputText id="reportToolLbl" value="#{msgs.submenu_tool_report}" 
						rendered="#{ChartParams.selectedReportType eq 'toolReport'}" styleClass="selector"/>
				</a4j:outputPanel>						

				<a4j:status id="reportChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading"/>	            		
            </a4j:region>

            <t:htmlTag value=""/>

			<%/* #####  REPORTS  ##### */%>
			<h:panelGrid id="reportMainArea" styleClass="sectionContainerNav" style="width:100%" columns="1" 
				columnClasses="left">
				<t:div styleClass="left">		
					<a4j:region id="reportChartRegion">									
						<%/* #####  TITLE  ##### */%>
						<a4j:outputPanel id="reportTitle">					
							<t:htmlTag value="h2">
								<t:outputText value="#{msgs.title_monthly_login_report}" 
									rendered="#{ChartParams.selectedReportType eq 'monthlyLogin'}"/>
								<t:outputText value="#{msgs.title_weekly_login_report}" 
									rendered="#{ChartParams.selectedReportType eq 'weeklyLogin'}"/>
								<t:outputText value="#{msgs.title_daily_login_report}" 
									rendered="#{ChartParams.selectedReportType eq 'dailyLogin'}"/>
								<t:outputText value="#{msgs.title_regular_users_report}" 
									rendered="#{ChartParams.selectedReportType eq 'regularUsers'}"/>
								<t:outputText value="#{msgs.title_hourly_usage_report}" 
									rendered="#{ChartParams.selectedReportType eq 'hourlyUsage'}"/>
								<t:outputText value="#{msgs.title_top_activities_report}" 
									rendered="#{ChartParams.selectedReportType eq 'topActivities'}"/>
								<t:outputText value="#{msgs.title_tool_report}" 
									rendered="#{ChartParams.selectedReportType eq 'toolReport'}"/>
							</t:htmlTag>
						</a4j:outputPanel>						

						<a4j:outputPanel id="reportDescription">					
							<%/* #####  REPORT DESC ##### */%>
							<t:outputText value="#{msgs.desc_monthly_login_report}" 
								rendered="#{ChartParams.selectedReportType eq 'monthlyLogin'}" styleClass="description"/>
							<t:outputText value="#{msgs.desc_weekly_login_report}" 
								rendered="#{ChartParams.selectedReportType eq 'weeklyLogin'}" styleClass="description"/>
							<t:outputText value="#{msgs.desc_daily_login_report}" 
								rendered="#{ChartParams.selectedReportType eq 'dailyLogin'}" styleClass="description"/>
							<t:outputText value="#{msgs.desc_regular_users_report}" 
								rendered="#{ChartParams.selectedReportType eq 'regularUsers'}" styleClass="description"/>
							<t:outputText value="#{msgs.desc_hourly_usage_report}" 
								rendered="#{ChartParams.selectedReportType eq 'hourlyUsage'}" styleClass="description"/>
							<t:outputText value="#{msgs.desc_top_activities_report}" 
								rendered="#{ChartParams.selectedReportType eq 'topActivities'}" styleClass="description"/>
							<t:outputText value="#{msgs.desc_tool_report}" 
								rendered="#{ChartParams.selectedReportType eq 'toolReport'}" styleClass="description"/>
						</a4j:outputPanel>						

	                    <t:htmlTag value="br"/>
	                    <t:htmlTag value="br"/>
	                    
						<%/* #####  REPORT CHART  ##### */%>
	            		<a4j:outputPanel id="reportChartPanel">
	            			<t:commandLink action="maximize" title="#{msgs.click_to_max}" actionListener="#{ChartParams.selectMaximizedReport}">
		            			<a4j:mediaOutput 
									id="reportChart"
									element="img" cacheable="false"
									createContent="#{ServerWideReportBean.generateReportChart}" 
									value="#{ChartParams}"
									mimeType="image/png"
			                        rendered="#{ChartParams.renderReportChart}"
								/>
							</t:commandLink>
	                    </a4j:outputPanel>
	                    
	                    <t:htmlTag value="br"/>
	                    <t:htmlTag value="br"/>	                    

						<a4j:outputPanel id="reportNotes">					
							<%/* #####  REPORT NOTES ##### */%>
							<t:outputText value="#{msgs.notes_monthly_login_report}" 
								rendered="#{ChartParams.selectedReportType eq 'monthlyLogin'}" styleClass="notes"/>
							<t:outputText value="#{msgs.notes_weekly_login_report}" 
								rendered="#{ChartParams.selectedReportType eq 'weeklyLogin'}" styleClass="notes"/>
							<t:outputText value="#{msgs.notes_daily_login_report}" 
								rendered="#{ChartParams.selectedReportType eq 'dailyLogin'}" styleClass="notes"/>
							<t:outputText value="#{msgs.notes_regular_users_report}" 
								rendered="#{ChartParams.selectedReportType eq 'regularUsers'}" styleClass="notes"/>
							<t:outputText value="#{msgs.notes_hourly_usage_report}" 
								rendered="#{ChartParams.selectedReportType eq 'hourlyUsage'}" styleClass="notes"/>
							<t:outputText value="#{msgs.notes_top_activities_report}" 
								rendered="#{ChartParams.selectedReportType eq 'topActivities'}" styleClass="notes"/>
							<t:outputText value="#{msgs.notes_tool_report}" 
								rendered="#{ChartParams.selectedReportType eq 'toolReport'}" styleClass="notes"/>
						</a4j:outputPanel>						
		            </a4j:region>	                 
				</t:div>	           
            </h:panelGrid>	
		</t:div>		    
		
		<a4j:jsFunction name="renderReportChart"
			actionListener="#{ChartParams.renderReportChart}"
		    reRender="reportSelectors,reportChartPanel" status="reportChartStatus"
		    immediate="true" oncomplete="setMainFrameHeightNoScroll(window.name, 700);">  
		    <a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>          
        </a4j:jsFunction>
		    
		<%/* #####  Set chart params in bean ##### */%>
		<a4j:jsFunction name="setChartParameters"
			actionListener="#{ChartParams.setChartParameters}" immediate="true" >
		   	<a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>
		</a4j:jsFunction>
		
		<%/* #####  Set chart and summary tables render flags to false on startup ##### */%>
		<a4j:jsFunction name="setChartsRenderFalse" actionListener="#{ChartParams.setAllRenderFalse}" immediate="true"/>
		<a4j:jsFunction name="setReportChartRenderFalse" actionListener="#{ChartParams.setReportRenderFalse}" immediate="true"/>		    
		
		
	    
	</a4j:form>
	
	<f:verbatim>
       	<script type="text/javascript">
       		function getMainAreaWidth(){
       			return document.getElementById('serverWideReportForm:reportMainArea').offsetWidth - 10;
       		}
       		function getChartWidth(){
       			//return document.getElementById('serverWideReportForm:left').offsetWidth;
       			return (getMainAreaWidth());
       		}
       		function getChartHeight(){
       			return 420;
       		}
       		function getBodyBackgroundColor(){
       			var bgColor;
				if(document.body.currentStyle){
					// IE based
					if(window.name && parent.document.getElementById(window.name)){
						bgColor = parent.document.getElementById(window.name).currentStyle["backgroundColor"];
					}else{
                   		bgColor = document.body.currentStyle["backgroundColor"];
                   	}
                   }else if(window.getComputedStyle){
                   	// Mozilla based
                   	var elstyle;
                   	if(parent.document.getElementById(window.name)){
                   		elstyle = window.getComputedStyle(parent.document.getElementById(window.name), "");
                   	}else{
                   		elstyle = window.getComputedStyle(document.body, "");
                   	}
                       bgColor = elstyle.getPropertyValue("background-color");
                   }
                   if(bgColor == 'transparent') bgColor = "white";
                   return bgColor;
       		}
       	</script>
	</f:verbatim>		
	
	<f:subview id="reportPartialLoader">
		<f:verbatim>
        	<script type="text/javascript">
                 	renderReportChart(getChartWidth(), getChartHeight(), 'white');
        	</script>
		</f:verbatim>
	</f:subview>
   
	
	
</sakai:view>
</f:view>
