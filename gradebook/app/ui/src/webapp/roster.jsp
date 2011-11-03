<f:view>
	<script type="text/javascript" language="JavaScript" src="/library/js/jquery-ui-latest/js/jquery.min.js"></script>
	<script type="text/javascript" language="JavaScript" src="/library/js/jquery-ui-latest/js/jquery-ui.min.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/spreadsheetUI.js"></script>
	<script type="text/javascript" src="${pageContext.request.contextPath}/js/dialog.js"></script>
			<link rel="stylesheet" type="text/css" href="${pageContext.request.contextPath}/css/dialog.css" />
			
	<%
	  	String thisId = request.getParameter("panel");
	  	if (thisId == null) 
	  	{
	    	thisId = "Main" + org.sakaiproject.tool.cover.ToolManager.getCurrentPlacement().getId();
	 	}	
	%>
	<script type="text/javascript">
		var iframeId = '<%= org.sakaiproject.util.Web.escapeJavascript(thisId)%>';
	</script>
	
	<sakai:flowState bean="#{rosterBean}" />			
			
	
    
   
		    
	<div class="portletBody">
	  <h:form id="gbForm">
		<t:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@ include file="/inc/appMenu.jspf"%>
		</t:aliasBean>
		
		<div id="dialogDiv" title="Grade Messages" style="display:none;">
			   <iframe id="dialogFrame" name="dialogFrame" width="100%" height="100%" frameborder="0"></iframe>
	    </div>

		<h2>
			<h:outputText value="#{msgs.roster_page_title}"/>
			<h:commandButton
				id="exportExcel"
				value="#{msgs.roster_export_excel}"
				actionListener="#{rosterBean.exportExcel}"
				rendered="#{!rosterBean.emptyEnrollments}"
				style="float: right; margin-left: 15px;"
				/>
			<h:commandButton
				id="exportCsv"
				value="#{msgs.roster_export_csv}"
				actionListener="#{rosterBean.exportCsv}"
				rendered="#{!rosterBean.emptyEnrollments}"
				style="float: right;  margin-left: 15px;"
				/>
			<h:commandButton
				id="exportPdf"
				value="#{msgs.roster_export_pdf}"
				actionListener="#{rosterBean.exportPdf}"
				rendered="#{!rosterBean.emptyEnrollments}"
				style="float: right"
				/>
		</h2>

  	<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary"
			rendered="#{rosterBean.userAbleToGradeAll}">
			<h:outputText id="courseGradeLabel" value="#{msgs.avg_course_grade_name}"  />
			<h:panelGroup>
				<h:outputText id="letterGrade" value="#{rosterBean.avgCourseGradeLetter} " />
				<h:outputText id="cumScore" value="#{rosterBean.avgCourseGrade}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:panelGroup>
			<h:outputText value="#{msgs.roster_average_category}" rendered="#{rosterBean.selectedCategory != null}" />
			<h:panelGroup rendered="#{rosterBean.selectedCategory != null}" >
				<h:outputText value="#{rosterBean.selectedCategory}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:panelGroup>
		</h:panelGrid>

		<t:aliasBean alias="#{bean}" value="#{rosterBean}">
			<%@ include file="/inc/filterPagingRoster.jspf"%>
		</t:aliasBean>

		<gbx:spreadsheetUI 
			colLock="#{rosterBean.colLock}"
			initialHeight="200px"
			value="#{rosterBean.studentRows}" 
			binding="#{rosterBean.rosterDataTable}" 
			sortColumn="#{rosterBean.sortColumn}"
            sortAscending="#{rosterBean.sortAscending}"
			var="row">
			<h:column id="studentNameData">
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentSortName" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_name}"/>
		            </t:commandSortHeader>
		    </f:facet>
				<h:commandLink action="instructorView">
					<h:outputText value="#{row.sortName}"/>
					<f:param name="studentUid" value="#{row.studentUid}"/>
					<f:param name="returnToPage" value="roster" />
				</h:commandLink>
			</h:column>
			<h:column id="studentIdData">
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentDisplayId" immediate="true" arrow="true" actionListener="#{rosterBean.sort}">
		                <h:outputText value="#{msgs.roster_student_id}"/>
		            </t:commandSortHeader>
		        </f:facet>
				<h:outputText value="#{row.displayId}"/>
			</h:column>
			<%/* Assignment columns will be dynamically appended, starting here. */%>
		</gbx:spreadsheetUI>

		<p class="instruction">
			<h:outputText value="#{msgs.roster_no_enrollments}" rendered="#{rosterBean.emptyEnrollments}" />
		</p>

	  </h:form>
	  	<h:panelGrid styleClass="instruction" cellpadding="0" cellspacing="0" columns="1">
		<h:outputText value="#{msgs.overview_legend_title}" />
		<h:panelGroup>
			<h:outputText value="#{msgs.roster_footnote_symbol1}" />
			<h:outputText value="#{msgs.roster_footnote_legend1}" />
		</h:panelGroup>
		<h:panelGroup>
			<h:outputText value="#{msgs.roster_footnote_symbol2}" />
			<h:outputText value="#{msgs.roster_footnote_legend2}" />
		</h:panelGroup>
		<h:panelGroup>
			<h:outputText value="#{msgs.roster_footnote_legend3}" />
		</h:panelGroup>
	</h:panelGrid>
	</div>
</f:view>
