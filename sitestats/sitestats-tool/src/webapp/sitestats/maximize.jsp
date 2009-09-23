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
	<f:verbatim>
		<script type="text/javascript">
	    	function getChartWidth(){
	       		return document.getElementById('maximizeForm:mainArea').offsetWidth * 0.98;
	       	}
	       	function getChartHeight(){
	       		//return document.getElementById('maximizeForm:mainArea').offsetHeight;
	       		return getChartWidth() / 2;
	       	}
	       	function fixPageHeight(){
	       		document.getElementById('maximizeForm:mainArea').style.height = (getChartHeight() * 1.30) + 'px';
	       	}
		</script>
	</f:verbatim>	

	<%/* #####  FACES MESSAGES  ##### */%>
	<f:subview id="allowed" rendered="#{!ServiceBean.allowed}">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>
	

	<a4j:form id="maximizeForm" rendered="#{ServiceBean.allowed}">	    
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div id="mainArea" style="width: 100%; height: 400px;">
		
			<t:htmlTag value="br"/>
		
			<a4j:region id="visitsRegion">
				<%/* #####  VISITS CHART  ##### */%>
	            <a4j:status id="visitsChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading" layout="inline"/>
	            <a4j:outputPanel id="visitsChartPanel">
	            	<a4j:mediaOutput 
						id="visitsChart"
						element="img" cacheable="false"
						createContent="#{OverviewBean.generateVisitsChart}" 
						value="#{ChartParams}"
						mimeType="image/png"
		                rendered="#{ChartParams.renderVisitsChart}"
						/>
	            </a4j:outputPanel>
			</a4j:region>
	                						
	        <a4j:region id="activityRegion">					
				<%/* #####  ACTIVITY CHART  ##### */%>
				<a4j:status id="activityChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading" layout="inline"/>
	            <a4j:outputPanel id="activityChartPanel">
	            	<a4j:mediaOutput 
						id="activityChart"
						element="img" cacheable="false"
						createContent="#{OverviewBean.generateActivityChart}" 
						value="#{ChartParams}"
						mimeType="image/png"
		            	rendered="#{ChartParams.renderActivityChart}"
						/>		            
	            </a4j:outputPanel>
           </a4j:region>
           
		
	       <a4j:region id="reportChartRegion">					
				<%/* #####  ACTIVITY CHART  ##### */%>
				<a4j:status id="reportChartStatus" startText="..." stopText=" " startStyleClass="ajaxLoading" layout="inline"/>
	            <a4j:outputPanel id="reportChartPanel">
          			<a4j:mediaOutput 
						id="reportChart"
						element="img" cacheable="false"
						createContent="#{ServerWideReportBean.generateReportChart}" 
						value="#{ChartParams}"
						mimeType="image/png"
                        rendered="#{ChartParams.renderReportChart}"
					/>       
	            </a4j:outputPanel>
            </a4j:region>
           
           
			<%/* #####  BUTTONS  ##### */%>
			<sakai:button_bar>
				<t:commandButton value="#{msgs.overview_back}" action="overview" actionListener="#{ChartParams.restoreSize}" styleClass="active" rendered="#{ChartParams.maximizedVisits}"/>
				<t:commandButton value="#{msgs.overview_back}" action="overview" actionListener="#{ChartParams.restoreSize}" styleClass="active" rendered="#{ChartParams.maximizedActivity}"/>
				<t:commandButton value="#{msgs.serverwide_back}" action="serverwide" actionListener="#{ChartParams.restoreSize}" styleClass="active" rendered="#{ChartParams.maximizedReport}"/>
			</sakai:button_bar>
			
			<f:verbatim>
				<script type="text/javascript">fixPageHeight();</script>
			</f:verbatim>
		</t:div>		    
		
		
		
		<%/* #####  Load data with ajax for the first time  ##### */%>
		<a4j:jsFunction name="renderVisitsChart"
			actionListener="#{ChartParams.renderVisitsChart}"
		    reRender="visitsChartPanel" status="visitsChartStatus"
		    immediate="true">
		    <a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>
		   	<f:param name="siteId" value="#{ServiceBean.siteId}"/>
		</a4j:jsFunction>
		<a4j:jsFunction name="renderActivityChart"
			actionListener="#{ChartParams.renderActivityChart}"
		    reRender="activityChartPanel" status="activityChartStatus"
		    immediate="true">
		    <a4j:actionparam name="chartWidth"/>
		   	<a4j:actionparam name="chartHeight"/>
		    <a4j:actionparam name="backgroundColor"/>
		   	<f:param name="siteId" value="#{ServiceBean.siteId}"/>
		</a4j:jsFunction>
		    
		<a4j:jsFunction name="renderReportChart"
			actionListener="#{ChartParams.renderReportChart}"
		    reRender="reportChartPanel" status="reportChartStatus"
		    immediate="true">  
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
		   	<f:param name="siteId" value="#{ServiceBean.siteId}"/>
		</a4j:jsFunction>
		
	</a4j:form>
	
	
		
		
		<f:subview id="visitsLoader" rendered="#{ChartParams.maximizedVisits}">
			<f:verbatim>
	        	<script type="text/javascript">renderVisitsChart(getChartWidth(), getChartHeight(), 'white');</script>
			</f:verbatim>
		</f:subview>
		<f:subview id="activityLoader" rendered="#{ChartParams.maximizedActivity}">
			<f:verbatim>
	        	<script type="text/javascript">renderActivityChart(getChartWidth(), getChartHeight(), 'white');</script>
			</f:verbatim>
		</f:subview>
		<f:subview id="reportLoader" rendered="#{ChartParams.maximizedReport}">
			<f:verbatim>
	        	<script type="text/javascript">renderReportChart(getChartWidth(), getChartHeight(), 'white');</script>
			</f:verbatim>
		</f:subview>
		
    
</sakai:view>
</f:view>