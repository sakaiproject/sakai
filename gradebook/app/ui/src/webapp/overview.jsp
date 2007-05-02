<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
	  <t:aliasBean alias="#{bean}" value="#{overviewBean}">
		<%@include file="/inc/appMenu.jspf"%>
	  </t:aliasBean>

	  <sakai:flowState bean="#{overviewBean}" />

		<h2><h:outputText value="#{msgs.appmenu_overview}"/></h2>

		<div class="instruction">
			<h:outputText value="#{msgs.overview_instruction}" escape="false"/>
			<h:panelGroup rendered="#{overviewBean.userAbleToEditAssessments}">
				<f:verbatim><p></f:verbatim>
				<h:outputText value="#{overviewBean.gradeOptionSummary} "/>
				<h:commandLink action="feedbackOptions" immediate="true">
					<h:outputText value="#{msgs.overview_grade_option_change}"/>
				</h:commandLink>
				<f:verbatim></p></f:verbatim>
			</h:panelGroup>
		</div>
		
		<sakai:tool_bar rendered="#{overviewBean.userAbleToEditAssessments}">
    	<sakai:tool_bar_item value="#{msgs.overview_add_item}" action="addAssignment" />
    	<sakai:tool_bar_item value="#{msgs.overview_import_item}" action="spreadsheetListing" />
  	</sakai:tool_bar>
  	
  	<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">
			<h:outputText id="cumLabel" value="#{msgs.overview_avg_cum_score}" rendered="#{overviewBean.userAbleToGradeAll}"/>
			<h:outputText id="cumScore" value="#{overviewBean.courseGrade.formattedMean}" rendered="#{overviewBean.userAbleToGradeAll}">
				<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.PERCENTAGE" />
			</h:outputText>

			<h:outputText id="avgGradeLabel" value="#{msgs.overview_avg_course_grade}" rendered="#{overviewBean.userAbleToGradeAll}"/>
			<h:outputText id="avgGrade" value="#{overviewBean.avgCourseGradeLetter}" rendered="#{overviewBean.userAbleToGradeAll}" />
		</h:panelGrid>

		<%@include file="/inc/globalMessages.jspf"%>

		<h4><h:outputText value="#{msgs.overview_assignments_title}"/></h4>
		<gbx:gradebookItemTable cellpadding="0" cellspacing="0"
			value="#{overviewBean.gradebookItemList}"
			var="gradebookItem"
			sortColumn="#{overviewBean.assignmentSortColumn}"
      sortAscending="#{overviewBean.assignmentSortAscending}"
      columnClasses="attach,left,center,center,center,left,center,center,external"
			styleClass="listHier"
			expanded="true"
			rowClasses="#{overviewBean.rowStyles}"
			headerClasses="attach">
			
			<h:column id="_toggle" rendered="#{overviewBean.categoriesEnabled}">
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
			</h:column>
			
			<h:column id="_title">
				<f:facet name="header">
		    	<t:commandSortHeader columnName="name" immediate="true" arrow="true">
		      	<h:outputText value="#{msgs.overview_assignments_header_name}" />
		      	<h:outputText value="#{msgs.overview_footnote_symbol1}" />
		      </t:commandSortHeader>
		    </f:facet>

				<!-- Assignment / Assessment link -->
				
				<h:panelGroup rendered="#{gradebookItem.assignment}">
					<h:commandLink action="assignmentDetails" rendered="#{gradebookItem.released}">
						<h:outputText value="#{gradebookItem.name}" />
						<f:param name="assignmentId" value="#{gradebookItem.id}"/>
					</h:commandLink>
					<h:commandLink action="assignmentDetails" rendered="#{!gradebookItem.released}" styleClass="inactive">
						<h:outputText value="#{gradebookItem.name}"/>
						<f:param name="assignmentId" value="#{gradebookItem.id}"/>
					</h:commandLink>
				</h:panelGroup>
				
				<h:outputText value="#{gradebookItem.name}" styleClass="categoryHeading" rendered="#{gradebookItem.category}" />

				<!-- Course grade link -->
				<h:commandLink action="courseGradeDetails" rendered="#{gradebookItem.courseGrade}"  styleClass="courseGrade">
					<h:outputText value="#{msgs.course_grade_name}" />
				</h:commandLink>
			</h:column>
			<h:column rendered="#{overviewBean.userAbleToEditAssessments}">
				<f:facet name="header">
		    	<h:outputText escape="false" value="&nbsp;" />
		    </f:facet>
				<h:commandLink action="editAssignment" rendered="#{gradebookItem.assignment}">
					<h:outputText value="#{msgs.overview_edit}" />
					<f:param name="assignmentId" value="#{gradebookItem.id}"/>
				</h:commandLink>
			</h:column>
			
			<h:column rendered="#{overviewBean.userAbleToGradeAll}">
				<f:facet name="header">
		    	<t:commandSortHeader columnName="mean" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_average}" />
						<h:outputText value="#{msgs.overview_footnote_symbol2}" />
		      </t:commandSortHeader>
		    </f:facet>

				<h:outputText value="#{gradebookItem}" escape="false">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:column>
			
			<h:column rendered="#{overviewBean.weightingEnabled}">
				<f:facet name="header">
		    	<t:commandSortHeader columnName="weight" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_weight}"/>
		      </t:commandSortHeader>
		    </f:facet>

				<h:outputText value="#{gradebookItem.weight}" rendered="#{gradebookItem.category}">
					<f:convertNumber type="percent" maxFractionDigits="2" />
				</h:outputText>
			</h:column>
			
			<h:column>
				<f:facet name="header">
		    	<t:commandSortHeader columnName="dueDate" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_due_date}"/>
		      </t:commandSortHeader>
		    </f:facet>

				<h:outputText value="#{gradebookItem.dueDate}" rendered="#{gradebookItem.assignment && gradebookItem.dueDate != null}">
        	<gbx:convertDateTime/>
        </h:outputText>
				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{gradebookItem.assignment && gradebookItem.dueDate == null}"/>
			</h:column>
			
			<h:column>
				<f:facet name="header">
        	<t:commandSortHeader columnName="released" immediate="true" arrow="true">
          	<h:outputText value="#{msgs.overview_released}" escape="false"/>
          </t:commandSortHeader>
        </f:facet>
				<h:outputText value="#{msgs.overview_released_true}" rendered="#{gradebookItem.assignment && gradebookItem.released == true }"/>
				<h:outputText value="#{msgs.overview_released_false}" rendered="#{gradebookItem.assignment && gradebookItem.released == false}"/>
			</h:column>
			
			<h:column>
				<f:facet name="header">
        	<t:commandSortHeader columnName="counted" immediate="true" arrow="true">
          	<h:outputText value="#{msgs.overview_included_in_cum}" escape="false"/>
          </t:commandSortHeader>
        </f:facet>
				<h:outputText value="#{msgs.overview_included_in_cum_true}" rendered="#{gradebookItem.assignment && gradebookItem.counted == true }"/>
				<h:outputText value="#{msgs.overview_included_in_cum_false}" rendered="#{gradebookItem.assignment && gradebookItem.counted == false}"/>
			</h:column>
			
			<h:column rendered="#{overviewBean.displayGradeEditorCol}">
				<f:facet name="header">
        	<t:commandSortHeader columnName="gradeEditor" immediate="true" arrow="true">
          	<h:outputText value="#{msgs.overview_grade_editor}" />
						<h:outputText value="#{msgs.overview_footnote_symbol3}" />
          </t:commandSortHeader>
        </f:facet>
				<h:outputText value="from #{gradebookItem.externalAppName}" rendered="#{gradebookItem.assignment && ! empty gradebookItem.externalAppName}"/>
			</h:column>
		</gbx:gradebookItemTable>

	  </h:form>
	</div>
	
	<h:panelGrid styleClass="instruction" cellpadding="0" cellspacing="0" columns="1">
		<h:outputText value="#{msgs.overview_legend_title}" />
		<h:panelGroup>
			<h:outputText value="#{msgs.overview_footnote_symbol1}" />
			<h:outputText value="#{msgs.overview_footnote_legend1}" />
		</h:panelGroup>
		<h:panelGroup>
			<h:outputText value="#{msgs.overview_footnote_symbol2}" />
			<h:outputText value="#{msgs.overview_footnote_legend2}" />
		</h:panelGroup>
		<h:panelGroup>
			<h:outputText value="#{msgs.overview_footnote_symbol3}" />
			<h:outputText value="#{msgs.overview_footnote_legend3}" />
		</h:panelGroup>
	</h:panelGrid>
	
</f:view>
