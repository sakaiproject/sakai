
<%/* #####  TAGLIBS, BUNDLE, Etc  ##### */%>
<%@include file="inc/common.jsp"%>


<f:view>
<sakai:view title="#{msgs.tool_title}">

	<%/* #####  CSS  ##### */%>
	<style type="text/css">
		@import url("/sakai-sitestats-tool/sitestats/css/sitestats.css");
	</style>
	
	<%/* #####  JAVASCRIPT  ##### */%>
	<sakai:script path="/sitestats/script/reportresults.js"/>

	<%/* #####  FACES MESSAGES  ##### */%>
	<f:subview id="allowed">
		<h:message for="allowed" fatalClass="alertMessage" fatalStyle="margin-top: 15px;" showDetail="true"/>
	</f:subview>
	

	<h:form id="reportResultsForm" rendered="#{ServiceBean.allowed}">
		
		<%/* #####  MENU  ##### */%>
		<h:panelGroup rendered="#{!ReportsBean.printVersion}">
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
	    
	    
	    <%/* #####  PRINT  ##### */%>
	    <t:div rendered="#{!ReportsBean.printVersion}" style="float: right; padding-top: 12px; clear:both">
			<h:graphicImage url="/sitestats/images/silk/icons/printer.png" alt="#{msgs.reportres_printversion}"/>
		    <h:commandLink target="_new" title="#{msgs.reportres_printversion}" >
				<h:outputText value="#{msgs.reportres_printversion} " />
				<f:param name="printVersion" value="true"/>
			</h:commandLink>
		</t:div>
		<t:div rendered="#{ReportsBean.printVersion}" styleClass="act">
			<h:graphicImage url="/sitestats/images/silk/icons/printer.png" alt="#{msgs.reportres_print}"/>
		    <h:commandLink onclick="window.print(); return false;" title="#{msgs.reportres_print}">
				<h:outputText value="#{msgs.reportres_print} " />
			</h:commandLink>
		</t:div>
	    
		
		<%/* #####  TITLE  ##### */%>
		<t:htmlTag value="h2">
			<h:outputText value="#{msgs.reportres_title} (#{ServiceBean.siteTitle})" rendered="#{ServiceBean.adminView}"/>
			<h:outputText value="#{msgs.reportres_title}" rendered="#{!ServiceBean.adminView}"/>
		</t:htmlTag>
	    
		
		<%/* #####  PAGE CONTENT  ##### */%>
		<t:div style="width:100%">	
			
			<%/* #####  ACTIVITY TABLE  ##### */%>
			<t:panelGrid styleClass="summaryTable" columns="2" columnClasses="autoSize,autoSize">				
				<t:outputLabel value="#{msgs.reportres_summ_act_basedon}" styleClass="indnt0" for="tblReportActivityBasedOn"/>
				<t:outputText value="#{ReportsBean.reportActivityBasedOn}" id="tblReportActivityBasedOn"/>
				
				<t:outputLabel value="#{ReportsBean.reportResourceActionTitle}" styleClass="indnt0" for="tblReportResourceAction" rendered="#{ReportsBean.reportResourceAction != null}"/>
				<t:outputText value="#{ReportsBean.reportResourceAction}" id="tblReportResourceAction" rendered="#{ReportsBean.reportResourceAction != null}"/>
				
				<t:outputLabel value="#{ReportsBean.reportActivitySelectionTitle}" styleClass="indnt0" for="tblReportActivitySelection" rendered="#{ReportsBean.reportActivitySelection != null}"/>
				<t:outputText value="#{ReportsBean.reportActivitySelection}" id="tblReportActivitySelection" rendered="#{ReportsBean.reportActivitySelection != null}"/>
				
				<t:outputLabel value="#{msgs.reportres_summ_timeperiod}" styleClass="indnt0" for="tblReportTimePeriod"/>
				<t:outputText value="#{ReportsBean.reportTimePeriod}" id="tblReportTimePeriod"/>
				
				<t:outputLabel value="#{msgs.reportres_summ_usr_selectiontype}" styleClass="indnt0" for="tblReportUserSelectionType"/>
				<t:outputText value="#{ReportsBean.reportUserSelectionType}" id="tblReportUserSelectionType"/>
				
				<t:outputLabel value="#{ReportsBean.reportUserSelectionTitle}" styleClass="indnt0" for="tblReportUserSelection" rendered="#{ReportsBean.reportUserSelectionTitle != null}"/>
				<t:outputText value="#{ReportsBean.reportUserSelection}" id="tblReportUserSelection" rendered="#{ReportsBean.reportUserSelectionTitle != null}"/>
				
				<t:outputLabel value="#{msgs.reportres_summ_generatedon}" styleClass="indnt0" for="tblReportDate"/>
				<t:outputText value="#{ReportsBean.report.localizedReportGenerationDate}" id="tblReportDate"/>
			</t:panelGrid>
			
			
			<%/* #####  PAGER  ##### */%>
			<t:div styleClass="right">
				<sakai:pager  
					rendered="#{!ReportsBean.printVersion}"
					totalItems="#{ReportsBean.pagerTotalItems}"
					firstItem="#{ReportsBean.pagerFirstItem}"
					pageSize="#{ReportsBean.pagerSize}"
					accesskeys="true" immediate="true"/>
				<t:htmlTag value="br" rendered="#{ReportsBean.printVersion}"/>
			</t:div>
			
			
			<%/* #####  ACTIVITY DATA  ##### */%>
			<t:dataTable
				id="dataTable"
				value="#{ReportsBean.report.reportData}"
				var="row"
				styleClass="listHier narrowTable"
				columnClasses="leftAlign,leftAlign,leftAlign,leftAlign,leftAlign"
				sortColumn="#{ReportsBean.sortColumn}" 
	            sortAscending="#{ReportsBean.sortAscending}"
	            first="#{param.printVersion == 'true' ? 0 : ReportsBean.pagerFirstItem}"
	            rows="#{param.printVersion == 'true' ? ReportsBean.pagerTotalItems : ReportsBean.pagerSize}">
				<t:column id="id">
					<f:facet name="header">	 
			            <t:commandSortHeader columnName="id" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_id}"/>		                
			            </t:commandSortHeader>               
			        </f:facet>
			        <h:outputText value="#{row.userId}" escape="false">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.USER_ID_EID"/>
					</h:outputText>
				</t:column>
				<t:column id="user">
					<f:facet name="header">	 
			            <t:commandSortHeader columnName="user" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_user}"/>		                
			            </t:commandSortHeader>               
			        </f:facet>
			        <h:outputText value="#{row.userId}" escape="true">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.USER_ID_NAME"/>
					</h:outputText>
				</t:column>
				<t:column id="event" rendered="#{ReportsBean.report.reportParams.what ne 'what-resources' && ReportsBean.report.reportParams.who ne 'who-none'}">
					<f:facet name="header">
			            <t:commandSortHeader columnName="event" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_event}"/>		                
			            </t:commandSortHeader>   	                
			        </f:facet>
			        <h:outputText value="#{row.ref}" escape="false">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.EVENT_ID_NAME"/>
					</h:outputText>
				</t:column>
				<t:column id="resource"  rendered="#{ReportsBean.report.reportParams.what eq 'what-resources' && ReportsBean.report.reportParams.who ne 'who-none'}">
					<f:facet name="header">
			            <t:commandSortHeader columnName="resource" propertyName="resource" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_resource}"/>		                
			            </t:commandSortHeader>   	                
			        </f:facet>
			        <t:graphicImage value="#{row.refImg}" rendered="#{row.refImg != null}"/>
			        <h:outputLink value="#{row.refUrl}" target="_new" rendered="#{row.refUrl != null}">
                    	<h:outputText value="#{row.ref}" escape="false">
							<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.RES_REF_NAME"/>
						</h:outputText>    
					</h:outputLink>
					<h:outputText value="#{row.ref}" escape="false" rendered="#{row.refUrl == null && row.refImg != null}">
						<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.RES_REF_NAME"/>
					</h:outputText>
				</t:column>
				<t:column id="action" rendered="#{ReportsBean.report.reportParams.what eq 'what-resources' && ReportsBean.report.reportParams.who ne 'who-none'}">
					<f:facet name="header">
			            <t:commandSortHeader columnName="action" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_action}"/>		                
			            </t:commandSortHeader>   	                
			        </f:facet>
			        <h:outputText value="#{row.refAction}">
							<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.RES_ACTION"/>
			        </h:outputText>
				</t:column>
				<t:column id="date" rendered="#{ReportsBean.report.reportParams.who ne 'who-none'}">
					<f:facet name="header">
			            <t:commandSortHeader columnName="date" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_date}"/>		                
			            </t:commandSortHeader>   	  	                
			        </f:facet>		  
			        <t:outputText value="#{row.date}">
			        	<f:converter converterId="org.sakaiproject.sitestats.tool.jsf.converter.LOCALIZED_DATE"/>
			        </t:outputText>
				</t:column>
				<t:column id="total" rendered="#{ReportsBean.report.reportParams.who ne 'who-none'}">
					<f:facet name="header">
			            <t:commandSortHeader columnName="total" immediate="true" arrow="true">
			                <h:outputText value="#{msgs.th_total}" styleClass="center"/>		                
			            </t:commandSortHeader>  		                
			        </f:facet>		  
			        <h:outputText value="#{row.count}" style="text-align: right;"/>    
				</t:column>
			</t:dataTable>
			<p class="instruction">
				<h:outputText value="#{msgs.no_data}" rendered="#{ReportsBean.report == null || ReportsBean.report.reportData == null}" />
			</p>
			
			<%/* #####  BUTTONS  ##### */%>
			<sakai:button_bar rendered="#{!ReportsBean.printVersion}">
				<t:commandButton id="back" value="#{msgs.report_back}" action="#{ReportsBean.processReportGoBack}" styleClass="active"/> 
				<t:commandButton id="export" value="#{msgs.bt_export}" onclick="showExportButtons(); return false;" rendered="#{ReportsBean.reportNotEmpty}"/>
				<t:commandButton id="exportXls" style="display: none"
					actionListener="#{ReportsBean.processExportExcel}" value="#{msgs.bt_export_excel}"/>
				<t:commandButton id="exportCsv" style="display: none" 
					actionListener="#{ReportsBean.processExportCSV}" value="#{msgs.bt_export_csv}"/>
				<t:commandButton id="exportPdf" style="display: none"
					actionListener="#{ReportsBean.processExportPDF}" value="#{msgs.bt_export_pdf}"/>				
			</sakai:button_bar>
			
		</t:div>
	    
	</h:form>
    
</sakai:view>
</f:view>