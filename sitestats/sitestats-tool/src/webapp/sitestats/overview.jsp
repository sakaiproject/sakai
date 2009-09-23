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
	

	<a4j:form id="overviewForm" rendered="#{ServiceBean.allowed}">	
		
		<%/* #####  MENU  ##### */%>
		<h:panelGroup>
	        <t:aliasBean alias="#{viewName}" value="OverviewBean">
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
			<h:outputText value="#{msgs.menu_overview} (#{ServiceBean.siteTitle})" rendered="#{ServiceBean.adminView}"/>
			<h:outputText value="#{msgs.menu_overview}" rendered="#{!ServiceBean.adminView}"/>
		</t:htmlTag>
	    
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">
			
			<%/* #####  VISITS  ##### */%>
			<t:htmlTag value="h4" styleClass="summaryHeader" rendered="#{ServiceBean.siteVisitsEnabled}">
				<h:outputText value="#{msgs.overview_title_visits}"/>
			</t:htmlTag>
			<h:panelGrid id="visitsMainArea" styleClass="sectionContainerNav" style="width:100%" 
				columns="2" columnClasses="halfSize,halfSize" rendered="#{ServiceBean.siteVisitsEnabled}">
				<a4j:region id="visitsChartRegion">
						<%/* #####  VISITS CHART SELECTORS  ##### */%>
						<a4j:outputPanel id="visitsSelectors">					
							<a4j:commandLink id="visitsWeekSel" value="#{msgs.submenu_week}" actionListener="#{ChartParams.selectVisitsWeekView}" rendered="#{ChartParams.selectedVisitsView ne 'week'}"
			                	status="visitsChartStatus" reRender="visitsSelectors,visitsChartPanel" styleClass="selector"
			                	oncomplete="setVisitsChartRenderFalse()"/>
							<t:outputText id="visitsWeekLbl" value="#{msgs.submenu_week}" rendered="#{ChartParams.selectedVisitsView eq 'week'}" styleClass="selector"/>
							
							<t:outputText value="    |    "/>
							
							<a4j:commandLink id="visitsMonthSel" value="#{msgs.submenu_month}" actionListener="#{ChartParams.selectVisitsMonthView}" rendered="#{ChartParams.selectedVisitsView ne 'month'}"
			                	status="visitsChartStatus" reRender="visitsSelectors,visitsChartPanel" styleClass="selector"
			                	oncomplete="setVisitsChartRenderFalse()"/>
							<t:outputText id="visitsMonthLbl" value="#{msgs.submenu_month}" rendered="#{ChartParams.selectedVisitsView eq 'month'}" styleClass="selector"/>
							
							<t:outputText value="    |    "/>
							
							<a4j:commandLink id="visitsYearSel" value="#{msgs.submenu_year}" actionListener="#{ChartParams.selectVisitsYearView}" rendered="#{ChartParams.selectedVisitsView ne 'year'}"
			                	status="visitsChartStatus" reRender="visitsSelectors,visitsChartPanel" styleClass="selector"
			                	oncomplete="setVisitsChartRenderFalse()"/>
							<t:outputText id="visitsYearLbl" value="#{msgs.submenu_year}" rendered="#{ChartParams.selectedVisitsView eq 'year'}" styleClass="selector"/>
							
							<a4j:status id="visitsChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading"/>							
						</a4j:outputPanel>
	            </a4j:region>
						
				<t:htmlTag value=""/>
					
				<a4j:region id="visitsChartRegion2">
						<%/* #####  VISITS CHART  ##### */%>
	            		<a4j:outputPanel id="visitsChartPanel">
	            			<t:commandLink action="maximize" title="#{msgs.click_to_max}" actionListener="#{ChartParams.selectMaximizedVisits}">
		            			<a4j:mediaOutput 
									id="visitsChart"
									element="img" cacheable="false"
									createContent="#{OverviewBean.generateVisitsChart}" 
									value="#{ChartParams}"
									mimeType="image/png"
			                        rendered="#{ChartParams.renderVisitsChart}"
								/>
							</t:commandLink>
	                    </a4j:outputPanel>
	            </a4j:region>
	                 
				<t:div styleClass="right">			
					<a4j:region id="visitsTableRegion">
			            <a4j:status id="visitsTableStatus" startText="..." stopText=" " startStyleClass="ajaxLoading"/>	            		
						<a4j:outputPanel id="visitsTablePanel">
							<%/* #####  VISITS TABLE  ##### */%>
							<t:panelGrid styleClass="summaryTable" columns="2" rendered="#{OverviewBean.renderVisitsTable}">
								<t:outputLabel value="#{msgs.overview_title_visits_sum}" styleClass="indnt0" for="tblVisitsTitle"/>
								<t:outputText value="" id="tblVisitsTitle"/>
									<t:outputLabel value="#{msgs.overview_total_visits}" styleClass="indnt1" for="tblTotalVisits"/>
									<t:outputText value="#{OverviewBean.summaryVisitsTotals.totalVisits}" id="tblTotalVisits"/>
									<t:outputLabel value="#{msgs.overview_average}" styleClass="indnt1" for="tblAvgVisits"/>
									<t:outputText value="#{OverviewBean.summaryVisitsTotals.last7DaysVisitsAverage}/#{OverviewBean.summaryVisitsTotals.last30DaysVisitsAverage}/#{OverviewBean.summaryVisitsTotals.last365DaysVisitsAverage}" id="tblAvgVisits"/>
									
								<t:outputLabel value="#{msgs.overview_title_unique_visits_sum}" styleClass="indnt0" for="tblUnqVisitsTitle"/>
								<t:outputText value="" id="tblUnqVisitsTitle"/>
									<t:outputLabel value="#{msgs.overview_total_unique_visits}" styleClass="indnt1" for="tblTotalUniqueVisits"/>
									<t:outputText value="#{OverviewBean.summaryVisitsTotals.totalUniqueVisits}" id="tblTotalUniqueVisits"/>
									<t:outputLabel value="#{msgs.overview_unique_visits_total_users}" styleClass="indnt1" for="tblUniqueTotalVisits"/>
									<t:outputText value="#{OverviewBean.summaryVisitsTotals.totalUniqueVisits}/#{OverviewBean.summaryVisitsTotals.totalUsers} (#{OverviewBean.summaryVisitsTotals.percentageOfUsersThatVisitedSite}%)" id="tblUniqueTotalVisits"/>
							</t:panelGrid>
		            	</a4j:outputPanel> 
	            	</a4j:region>
				</t:div>	           
            </h:panelGrid>		
			
			<%/* #####  ACTIVITY  ##### */%>
			<t:htmlTag value="h4" styleClass="summaryHeader" rendered="#{ServiceBean.siteActivityEnabled}">
				<h:outputText value="#{msgs.overview_title_activity}"/>
			</t:htmlTag>
			<h:panelGrid id="activityMainArea" styleClass="sectionContainerNav" style="width:100%" columns="2" columnClasses="halfSize,halfSize" rendered="#{ServiceBean.siteActivityEnabled}">
				<a4j:region id="activityChartRegion">
						<%/* #####  ACTIVITY CHART SELECTORS  ##### */%>
						<a4j:outputPanel id="activitySelectors">					
								<t:div styleClass="left" style="width: 60%">
									<a4j:commandLink id="activityWeekSel" value="#{msgs.submenu_week}" actionListener="#{ChartParams.selectActivityWeekView}" rendered="#{ChartParams.selectedActivityView ne 'week'}"
					                	status="activityChartStatus" reRender="activitySelectors,activityChartPanel" styleClass="selector"
			                			oncomplete="setActivityChartRenderFalse()"/>
									<t:outputText id="activityWeekLbl" value="#{msgs.submenu_week}" rendered="#{ChartParams.selectedActivityView eq 'week'}" styleClass="selector"/>
									
									<t:outputText value="    |    "/>
									
									<a4j:commandLink id="activityMonthSel" value="#{msgs.submenu_month}" actionListener="#{ChartParams.selectActivityMonthView}" rendered="#{ChartParams.selectedActivityView ne 'month'}"
					                	status="activityChartStatus" reRender="activitySelectors,activityChartPanel" styleClass="selector"
			                			oncomplete="setActivityChartRenderFalse()"/>
									<t:outputText id="activityMonthLbl" value="#{msgs.submenu_month}" rendered="#{ChartParams.selectedActivityView eq 'month'}" styleClass="selector"/>
									
									<t:outputText value="    |    "/>
									
									<a4j:commandLink id="activityYearSel" value="#{msgs.submenu_year}" actionListener="#{ChartParams.selectActivityYearView}" rendered="#{ChartParams.selectedActivityView ne 'year'}"
					                	status="activityChartStatus" reRender="activitySelectors,activityChartPanel" styleClass="selector"
			                			oncomplete="setActivityChartRenderFalse()"/>
									<t:outputText id="activityYearLbl" value="#{msgs.submenu_year}" rendered="#{ChartParams.selectedActivityView eq 'year'}" styleClass="selector"/>							
									
									<a4j:status id="activityChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading"/>	            		
								</t:div>
								<t:div styleClass="right" style="width: 40%">
									<a4j:commandLink id="activityPieSel" value="#{msgs.submenu_byTool}" actionListener="#{ChartParams.selectActivityPieChart}" rendered="#{ChartParams.selectedActivityChartType ne 'pie'}"
					                	status="activityChartStatus" reRender="activitySelectors,activityChartPanel" styleClass="selector"
			                			oncomplete="setActivityChartRenderFalse()"/>
									<t:outputText id="activityPieLbl" value="#{msgs.submenu_byTool}" rendered="#{ChartParams.selectedActivityChartType eq 'pie'}" styleClass="selector"/>
									
									<t:outputText value="    |    "/>
									
									<a4j:commandLink id="activityBarSel" value="#{msgs.submenu_byDate}" actionListener="#{ChartParams.selectActivityBarChart}" rendered="#{ChartParams.selectedActivityChartType ne 'bar'}"
					                	status="activityChartStatus" reRender="activitySelectors,activityChartPanel" styleClass="selector"
			                			oncomplete="setActivityChartRenderFalse()"/>
									<t:outputText id="activityBarLbl" value="#{msgs.submenu_byDate}" rendered="#{ChartParams.selectedActivityChartType eq 'bar'}" styleClass="selector"/>									
								</t:div>
						</a4j:outputPanel>						
	            </a4j:region>
	            
	            <t:htmlTag value=""/>
	            
				<a4j:region id="activityChartRegion2">									
						<%/* #####  ACTIVITY CHART  ##### */%>
	            		<a4j:outputPanel id="activityChartPanel">
	            			<t:commandLink action="maximize" title="#{msgs.click_to_max}" actionListener="#{ChartParams.selectMaximizedActivity}">
		            			<a4j:mediaOutput 
									id="activityChart"
									element="img" cacheable="false"
									createContent="#{OverviewBean.generateActivityChart}" 
									value="#{ChartParams}"
									mimeType="image/png"
			                        rendered="#{ChartParams.renderActivityChart}"
								/>
							</t:commandLink>
		                    <t:htmlTag value="br"/>
		                    <t:outputText value="#{msgs.legend_activity_piechart}" styleClass="instruction" style="padding-left: 5px" rendered="#{ChartParams.selectedActivityChartType eq 'pie'}"/>
		                    <t:outputText value="#{msgs.legend_activity_barchart}" styleClass="instruction" style="padding-left: 5px" rendered="#{ChartParams.selectedActivityChartType eq 'bar'}"/>
	                    </a4j:outputPanel>
	            </a4j:region>	                 
	                 
				<t:div styleClass="right">		
					<a4j:region id="activityTableRegion">		
		            	<a4j:status id="activityTableStatus" startText="..." stopText=" " startStyleClass="ajaxLoading"/>	            		
						<a4j:outputPanel id="activityTablePanel">
							<%/* #####  ACTIVITY TABLE  ##### */%>
							<t:panelGrid styleClass="summaryTable" columns="2" rendered="#{OverviewBean.renderActivityTable}">
								<t:outputLabel value="#{msgs.overview_title_activity_sum}" styleClass="indnt0" for="tblActivityTitle"/>
								<t:outputText value="" id="tblActivityTitle"/>
									<t:outputLabel value="#{msgs.overview_total_activity}" styleClass="indnt1" for="tblTotalActivity"/>
									<t:outputText value="#{OverviewBean.summaryActivityTotals.totalActivity}" id="tblTotalActivity"/>
									<t:outputLabel value="#{msgs.overview_average}" styleClass="indnt1" for="tblAvgActivity"/>
									<t:outputText value="#{OverviewBean.summaryActivityTotals.last7DaysActivityAverage}/#{OverviewBean.summaryActivityTotals.last30DaysActivityAverage}/#{OverviewBean.summaryActivityTotals.last365DaysActivityAverage}" id="tblAvgActivity"/>
							</t:panelGrid>
		            	</a4j:outputPanel> 
	            	</a4j:region>
				</t:div>	           
            </h:panelGrid>	
			
		</t:div>		    
		
			
		
		<%/* #####  Load data with ajax for the first time  ##### */%>
		<a4j:jsFunction name="renderVisitsChart"
			actionListener="#{ChartParams.renderVisitsChart}"
		    reRender="visitsSelectors,visitsChartPanel" status="visitsChartStatus"
		    immediate="true" oncomplete="setMainFrameHeightNoScroll(window.name, 640);">
		    <a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>
		   	<f:param name="siteId" value="#{ServiceBean.siteId}"/>
		</a4j:jsFunction>
		<a4j:jsFunction name="renderVisitsTable"
			actionListener="#{OverviewBean.renderVisitsTable}"
			reRender="visitsTablePanel" status="visitsTableStatus"
		    immediate="true"
		    oncomplete="setTablesRenderFalse(); setChartsRenderFalse();"/>
		    
		<a4j:jsFunction name="renderActivityChart"
			actionListener="#{ChartParams.renderActivityChart}"
		    reRender="activitySelectors,activityChartPanel" status="activityChartStatus"
		    immediate="true" oncomplete="setMainFrameHeightNoScroll(window.name, 642);">  
		    <a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>          
		   	<f:param name="siteId" value="#{ServiceBean.siteId}"/>
        </a4j:jsFunction>
		<a4j:jsFunction name="renderActivityTable"
			actionListener="#{OverviewBean.renderActivityTable}"
		    reRender="activityTablePanel" status="activityTableStatus"
		    immediate="true" 
		    oncomplete="setTablesRenderFalse(); setChartsRenderFalse();"/>	
		    
		<%/* #####  Set chart params in bean ##### */%>
		<a4j:jsFunction name="setChartParameters"
			actionListener="#{ChartParams.setChartParameters}" immediate="true" >
		   	<a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>
		   	<f:param name="siteId" value="#{ServiceBean.siteId}"/>
		</a4j:jsFunction>
		
		<%/* #####  Set chart and summary tables render flags to false on startup ##### */%>
		<a4j:jsFunction name="setChartsRenderFalse" actionListener="#{ChartParams.setAllRenderFalse}" immediate="true"/>
		<a4j:jsFunction name="setVisitsChartRenderFalse" actionListener="#{ChartParams.setVisitsRenderFalse}" immediate="true"/>
		<a4j:jsFunction name="setActivityChartRenderFalse" actionListener="#{ChartParams.setActivityRenderFalse}" immediate="true"/>		    
		<a4j:jsFunction name="setTablesRenderFalse" actionListener="#{OverviewBean.setAllRenderFalse}" immediate="true"/>
		
	</a4j:form>
	
	

	   <f:verbatim>
	       	<script type="text/javascript">
	       		function getMainAreaWidth(){
	       			var element = document.getElementById('overviewForm:activityMainArea');
	       			if (element == null){
	       				element = document.getElementById('overviewForm:visitsMainArea');
	       				if (element==null){
	       					element = document.getElementById('overviewForm:activityMainArea');
	       				}
	       			}
	       			return element.offsetWidth - 10;
	       		}	       		
	       	</script>
	   </f:verbatim>


		<f:verbatim>
	       	<script type="text/javascript">
	       		function getChartWidth(){
	       			//return document.getElementById('overviewForm:left').offsetWidth;
	       			return (getMainAreaWidth() / 2);
	       		}
	       		function getChartHeight(){
	       			return 200;
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
		
		<f:subview id="visitsPartialLoader" rendered="#{ServiceBean.siteVisitsEnabled}">
			<f:verbatim>
	        	<script type="text/javascript">
	        		//renderVisitsChart(getChartWidth(), getChartHeight(), getBodyBackgroundColor());
	        		renderVisitsChart(getChartWidth(), getChartHeight(), 'white');
	        		renderVisitsTable();
	        	</script>
			</f:verbatim>
		</f:subview>	
		
		<f:subview id="activityPartialLoader" rendered="#{ServiceBean.siteActivityEnabled}">
			<f:verbatim>
	        	<script type="text/javascript">
                  	renderActivityChart(getChartWidth(), getChartHeight(), 'white');
                  	renderActivityTable();
	        	</script>
			</f:verbatim>
		</f:subview>
    
</sakai:view>
</f:view>