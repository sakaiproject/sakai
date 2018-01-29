<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t" %>
<%@ taglib uri="http://sakaiproject.org/jsf/sakai" prefix="sakai" %>
<script src="/library/js/spinner.js" type="text/javascript"></script>
<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
	  <t:aliasBean alias="#{bean}" value="#{overviewBean}">
		<%@ include file="/inc/appMenu.jspf"%>
	  </t:aliasBean>

	  <sakai:flowState bean="#{overviewBean}" />

		<h2><h:outputText value="#{msgs.appmenu_overview}"/></h2>

		<div class="instruction">
			<h:panelGroup rendered="#{overviewBean.userAbleToEditAssessments}">
				<f:verbatim><p></f:verbatim>
				<h:outputText value="#{overviewBean.gradeOptionSummary} "/>
				<h:commandLink action="#{overviewBean.navigateToFeedbackOptions}" immediate="true">
					<h:outputText value="#{msgs.overview_grade_option_change}"/>
				</h:commandLink>
				<f:verbatim></p></f:verbatim>
			</h:panelGroup>
		</div>
		
		<sakai:tool_bar rendered="#{overviewBean.userAbleToEditAssessments}">
    		<sakai:tool_bar_item value="#{msgs.overview_add_item}"    action="#{overviewBean.navigateToAddAssignment}" current="false" style="sakai_jsf_not_current_marker" />
   			<sakai:tool_bar_item value="#{msgs.overview_import_item}" action="#{overviewBean.navigateToSpreadsheet}" current="false" style="sakai_jsf_not_current_marker" />
  		</sakai:tool_bar>
  		
  						
  	<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary gbSection"
			rendered="#{overviewBean.userAbleToGradeAll}">
			<h:outputText value="#{msgs.avg_course_grade_name}" styleClass="courseGrade"/>
			<h:panelGroup>
				<h:outputText id="letterGrade" value="#{overviewBean.avgCourseGradeLetter} " />
				<h:outputText id="cumScore" value="#{overviewBean.courseGrade}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:panelGroup>	
		</h:panelGrid>

		<%@ include file="/inc/globalMessages.jspf"%>

		<h4><h:outputText value="#{msgs.overview_assignments_title}"/></h4>
        <div style="width: 100%;">
        	<div class="instruction" style="float:left;">
        		<h:outputText value="#{msgs.overview_instruction}" escape="false"/>
        	</div>
            <div class="instruction" style="float:right;">
                <h:commandButton action="#{overviewBean.saveCurrentSort}" 
                    disabled="#{!overviewBean.enabledSaveSort}"
                    immediate="true" value="#{msgs.overview_save_current_sort}" />
            </div>
        </div>
    
		<gbx:gradebookItemTable cellpadding="0" cellspacing="0"
			value="#{overviewBean.gradebookItemList}"
			var="gradebookItem"
			sortColumn="#{overviewBean.assignmentSortColumn}"
      sortAscending="#{overviewBean.assignmentSortAscending}"
      columnClasses="attach,left,center,center,center,center,center,center,center,external,center"
			styleClass="listHier lines nolines"
			expanded="true"
			rowClasses="#{overviewBean.rowStyles}"
			headerClasses="attach,left,center,center,center,center,center,center,center,external,center">
			
			<h:column id="_toggle" rendered="#{overviewBean.categoriesEnabled}">
				<f:facet name="header">
					<h:outputText value="" />
				</f:facet>
			</h:column>
			
			<h:column id="_title">
				<f:facet name="header">
		    	<t:commandSortHeader columnName="name" propertyName="name" immediate="true" arrow="true">
		      	<h:outputText value="#{msgs.overview_assignments_header_name}" />
		      	<h:outputText value="#{msgs.overview_footnote_symbol1}" />
		      </t:commandSortHeader>
		    </f:facet>

				<!-- Assignment / Assessment link -->
				
				<h:panelGroup rendered="#{gradebookItem.assignment}">
					<h:commandLink action="#{overviewBean.navigateToAssignmentDetails}" rendered="#{gradebookItem.released}" onclick="SPNR.insertSpinnerInPreallocated( this, null, 'spinnerContainer' );">
						<h:outputText value="#{gradebookItem.name}" />
						<f:param name="assignmentId" value="#{gradebookItem.id}"/>
					</h:commandLink>
					<h:panelGroup rendered="#{!gradebookItem.released}" styleClass="inactive">
						<h:commandLink action="#{overviewBean.navigateToAssignmentDetails}" onclick="SPNR.insertSpinnerInPreallocated( this, null, 'spinnerContainer' );">
							<h:outputText value="#{gradebookItem.name}"/>
							<f:param name="assignmentId" value="#{gradebookItem.id}"/>
						</h:commandLink>
					</h:panelGroup>
					<h:outputText value=" (#{msgs.extra_credit})" rendered="#{gradebookItem.isExtraCredit && (gradebookItem.category == null || !gradebookItem.category.isExtraCredit) }"/>
					<t:div styleClass="allocatedSpinPlaceholder" id="spinnerContainer"></t:div>
				</h:panelGroup>
				
				
				<h:outputText value="#{gradebookItem.name}" styleClass="categoryHeading" rendered="#{gradebookItem.isCategory}" />
				<h:outputText value=" (#{msgs.extra_credit})" rendered="#{gradebookItem.isCategory && gradebookItem.isExtraCredit}"/>
				<h:outputText value=" (" rendered="#{gradebookItem.isCategory && (gradebookItem.dropHighest != 0 || gradebookItem.dropLowest != 0 || gradebookItem.keepHighest != 0)}" />
            <h:outputFormat value="#{msgs.cat_drop_highest_display}" rendered="#{gradebookItem.isCategory && gradebookItem.dropHighest != 0}" >
                <f:param value="#{gradebookItem.dropHighest}"/>
            </h:outputFormat>
            <h:outputText value="; " rendered="#{gradebookItem.isCategory && (gradebookItem.dropHighest != 0 && gradebookItem.dropLowest != 0)}" />
            <h:outputFormat value="#{msgs.cat_drop_lowest_display}" rendered="#{gradebookItem.isCategory && gradebookItem.dropLowest != 0}" >
                <f:param value="#{gradebookItem.dropLowest}"/>
            </h:outputFormat>
            
            <h:outputFormat value="#{msgs.cat_keep_highest_display}" rendered="#{gradebookItem.isCategory && gradebookItem.keepHighest != 0}" >
                <f:param value="#{gradebookItem.keepHighest}"/>
            </h:outputFormat>
            <h:outputText value=")" rendered="#{gradebookItem.isCategory && (gradebookItem.dropHighest != 0 || gradebookItem.dropLowest != 0 || gradebookItem.keepHighest != 0)}" />
				

			</h:column>
			<h:column rendered="#{overviewBean.userAbleToEditAssessments}">
				<f:facet name="header">
					<h:panelGroup> 
				    	<h:outputText escape="false" value="&nbsp;" />
				    	<h:outputText value="#{msgs.overview_edit}" styleClass="skip" />						
					</h:panelGroup> 
		    	</f:facet>
				<h:commandLink action="#{overviewBean.navigateToEdit}" rendered="#{gradebookItem.assignment}">
					<h:outputText value="#{msgs.overview_edit}" /><h:outputText escape="false" value="&nbsp;" styleClass="skip" /><h:outputText styleClass="skip" value="#{gradebookItem.name}" />
					<f:param name="assignmentId" value="#{gradebookItem.id}"/>
				</h:commandLink>
			</h:column>
			
			<h:column rendered="#{overviewBean.userAbleToGradeAll}">
				<f:facet name="header">
		    	<t:commandSortHeader columnName="mean" propertyName="mean" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_assignments_header_average}" />
						<h:outputText value="#{msgs.overview_assignments_header_average_exp}" styleClass="skip"/>						
						<h:outputText value="#{msgs.overview_footnote_symbol2}" />
		      </t:commandSortHeader>
		    </f:facet>

				<h:outputText value="#{gradebookItem}" escape="false">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:column>
			
			<h:column rendered="#{overviewBean.weightingEnabled}">
				<f:facet name="header">
		    	<t:commandSortHeader columnName="weight" propertyName="weight" immediate="true" arrow="true">
						<h:outputText value="#{msgs.overview_weight}"/>
		      </t:commandSortHeader>
		    </f:facet>

				<h:outputText value="#{gradebookItem.weight}" rendered="#{gradebookItem.isCategory}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.PRECISE_PERCENTAGE" />
				</h:outputText>
			</h:column>

            <h:column rendered="#{overviewBean.displayTotalPoints}">
                <f:facet name="header">
                    <t:commandSortHeader columnName="pointsPossible" propertyName="pointsPossible" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.overview_assignments_header_total_points}" />
                    </t:commandSortHeader>
                </f:facet>
                <h:outputText value="#{gradebookItem.pointsPossible}">
                    <f:converter converterId="org.sakaiproject.gradebook.jsf.converter.TOTAL_POINTS_CONVERTER" />
                </h:outputText>
            </h:column>

			<h:column>
				<f:facet name="header">
		    	<t:commandSortHeader columnName="dueDate" propertyName="dueDate" immediate="true" arrow="true">
						<h:outputText value="#{msgs.due_date}"/>
		      </t:commandSortHeader>
		    </f:facet>

				<h:outputText value="#{gradebookItem.dueDate}" rendered="#{gradebookItem.assignment && gradebookItem.dueDate != null}">
        	<gbx:convertDateTime/>
        </h:outputText>
				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{gradebookItem.assignment && gradebookItem.dueDate == null}"/>
			</h:column>
			
			<h:column>
				<f:facet name="header">
        	<t:commandSortHeader columnName="released" propertyName="released" immediate="true" arrow="true">
          	<h:outputText value="#{msgs.overview_released}" escape="false"/>
          </t:commandSortHeader>
        </f:facet>
				<h:outputText value="#{msgs.overview_released_true}" rendered="#{gradebookItem.assignment && gradebookItem.released == true }"/>
				<h:outputText value="#{msgs.overview_released_false}" rendered="#{gradebookItem.assignment && gradebookItem.released == false}"/>
			</h:column>
			
			<h:column>
				<f:facet name="header">
        	<t:commandSortHeader columnName="counted" propertyName="counted" immediate="true" arrow="true">
          	<h:outputText value="#{msgs.overview_included_in_cum}" escape="false"/>
          </t:commandSortHeader>
        </f:facet>
				<h:outputText value="#{msgs.overview_included_in_cum_true}" rendered="#{gradebookItem.assignment && gradebookItem.counted == true }"/>
				<h:outputText value="#{msgs.overview_included_in_cum_false}" rendered="#{gradebookItem.assignment && gradebookItem.counted == false}"/>
			</h:column>

            <h:column id="_sort">
                <f:facet name="header">
                    <t:commandSortHeader columnName="sorting" propertyName="sorting" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.overview_assignments_header_sorting}" />
                    </t:commandSortHeader>
                </f:facet>
                <h:panelGroup rendered="#{gradebookItem.assignment}">
                    <h:outputText value="#{gradebookItem.sortPosition != null ? (gradebookItem.sortPosition+1) : (gradebookItem.sortOrder+1)} " 
                        title="#{gradebookItem.sortPosition}" styleClass="sortSpacer" />
                    <h:inputHidden id="assignmentId" value="#{gradebookItem.id}" />
                    <h:inputHidden id="sortOrder" value="#{gradebookItem.sortOrder}" />
                    <%/* -AZ- hacks needed for safari: style="text-decoration: none !important;" 
                         AND <h:outputText value="&nbsp;" escape="false" /> */%>
                    <h:commandLink action="#{overviewBean.sortUp}" rendered="#{!gradebookItem.first}" 
                            title="#{msgs.overview_assignments_sort_up}" styleClass="sortUp" style="text-decoration: none !important;">
                        <f:param name="assignmentId" value="#{gradebookItem.id}"/>
                        <h:outputText value="&nbsp;" escape="false" />
                    </h:commandLink>
                    <h:outputText value="&nbsp;" escape="false" rendered="#{gradebookItem.first}" styleClass="sortSpacer" />
                    <h:commandLink action="#{overviewBean.sortDown}" rendered="#{!gradebookItem.last}" 
                            title="#{msgs.overview_assignments_sort_down}" styleClass="sortDown" style="text-decoration: none !important;">
                        <f:param name="assignmentId" value="#{gradebookItem.id}"/>
                        <h:outputText value="&nbsp;" escape="false" />
                    </h:commandLink>
                    <h:outputText value="&nbsp;" escape="false" rendered="#{gradebookItem.last}" styleClass="sortSpacer" />
                </h:panelGroup>
            </h:column>
			
			<h:column rendered="#{overviewBean.displayGradeEditorCol}">
				<f:facet name="header">
        	<t:commandSortHeader columnName="gradeEditor" propertyName="gradeEditor" immediate="true" arrow="true">
          	<h:outputText value="#{msgs.overview_grade_editor}" />
						<h:outputText value="#{msgs.overview_footnote_symbol3}" />
          </t:commandSortHeader>
        </f:facet>
				<h:outputText value="#{msgs.overview_from} #{gradebookItem.externalAppName}" rendered="#{gradebookItem.assignment && ! empty gradebookItem.externalAppName}"/>
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
