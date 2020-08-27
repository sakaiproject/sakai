<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{editStudentSectionsBean}"/>
	<h:inputHidden id="elementToFocus" value="#{editStudentSectionsBean.elementToFocus}"/>

    <t:aliasBean alias="#{viewName}" value="editSection">
        <%@ include file="/inc/navMenu.jspf"%>
    </t:aliasBean>

        <div class="page-header">
            <h1>
                <h:outputFormat value="#{msgs.edit_student_sections_page_header}">
                    <f:param value="#{editStudentSectionsBean.studentName}"/>
                </h:outputFormat>
            </h1>
        </div>

		<t:div styleClass="instruction">
            <h:outputFormat value="#{msgs.edit_student_sections_current_sections}">
                <f:param value="#{editStudentSectionsBean.studentName}"/>
            </h:outputFormat>
            
            <h:dataTable value="#{editStudentSectionsBean.enrolledSections}"
            	var="section" columnClasses="rightLabel, left, left, left">
	            <h:column>
	            	<h:outputText value="#{section.categoryForDisplay}:"/>
	            </h:column>
	            <h:column>
	            	<h:outputText value="#{section.title}"/>
	            </h:column>
		        <h:column>
		            <t:dataList id="meetingDayList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
			            <t:div>
			            	<h:outputText value="#{meeting.abbreviatedDays}"/>
			            </t:div>
		            </t:dataList>
		        </h:column>
		        <h:column>
		            <t:dataList id="meetingTimeList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
			            <t:div>
				            <h:outputText value="#{meeting.times}"/>
			            </t:div>
		            </t:dataList>
		        </h:column>
            </h:dataTable>
		</t:div>

        <%@ include file="/inc/globalMessages.jspf"%>
    
        <t:div styleClass="instructions">
            <h:outputText value="#{msgs.edit_student_sections_instructions}"/>
        </t:div>

		<t:aliasBean alias="#{filterBean}" value="#{editStudentSectionsBean}">
	        <%@ include file="/inc/sectionFilter.jspf"%>
	    </t:aliasBean>

        <t:dataTable cellpadding="0" cellspacing="0"
            id="editStudentSectionsTable"
            value="#{editStudentSectionsBean.sections}"
            var="section"
            sortColumn="#{editStudentSectionsBean.sortColumn}"
            sortAscending="#{editStudentSectionsBean.sortAscending}"
        	styleClass="listHier sectionTable"
        	columnClasses=",,leftIndent,left,left,left,left,center,center"
            >
            <h:column>
                <f:facet name="header">
                    <h:outputText value="#{msgs.category_table_header}" />
                </f:facet>
                <h:outputText value="#{section.category}"/>
            </h:column>
            <h:column>
                <f:facet name="header">
                    <h:outputText value="#{msgs.category_title_table_header}" />
                </f:facet>
                <h:outputFormat value="#{msgs.section_table_category_header}">
                    <f:param value="#{section.categoryForDisplay}"/>
                </h:outputFormat>
            </h:column>
            <h:column>
                <f:facet name="header">
                    <t:commandSortHeader columnName="title" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_title}" />
                    </t:commandSortHeader>
                </f:facet>
                <h:outputText value="#{section.title}"/>
            </h:column>

            <h:column>
                <f:facet name="header">
                    <t:commandSortHeader columnName="instructor" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_instructor}" />
                    </t:commandSortHeader>
                </f:facet>
                <t:dataList id="instructorName"
                    var="instructorName"
                    value="#{section.instructorNames}"
                    layout="simple">
                    <t:div>
                        <h:outputText value="#{instructorName}" />
                    </t:div>
               </t:dataList>
            </h:column>

	        <h:column>
	            <f:facet name="header">
	                <t:commandSortHeader columnName="meetingDays" immediate="false" arrow="true">
	                	<h:outputText value="#{msgs.student_view_header_day}" />
	                </t:commandSortHeader>
	            </f:facet>
	            <t:dataList id="meetingDayList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
		            <t:div>
		            	<h:outputText value="#{meeting.abbreviatedDays}"/>
		            </t:div>
	            </t:dataList>
	        </h:column>
            <h:column>
                <f:facet name="header">
                    <t:commandSortHeader columnName="meetingTimes" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_time}" />
                    </t:commandSortHeader>
                </f:facet>
	            <t:dataList id="meetingTimeList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
		            <t:div>
			            <h:outputText value="#{meeting.times}"/>
		            </t:div>
	            </t:dataList>
            </h:column>
                        
            <h:column>
                <f:facet name="header">
                    <t:commandSortHeader columnName="location" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_location}" />
                    </t:commandSortHeader>
                </f:facet>
	            <t:dataList id="meetingLocationList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
		            <t:div>
		            	<h:outputText value="#{meeting.location}"/>
		            </t:div>
	            </t:dataList>
            </h:column>

            <h:column>
                <f:facet name="header">
                    <t:commandSortHeader columnName="available" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_available}" />
                    </t:commandSortHeader>
                </f:facet>
            	<h:outputText value="#{section.spotsAvailable}"/>
            </h:column>

            <h:column>
                <h:commandLink
	                id="join"
                    value="#{msgs.edit_student_sections_assign}"
                    actionListener="#{editStudentSectionsBean.processJoinSection}"
                    rendered="#{ ! section.member}">
                    <f:param name="sectionUuid" value="#{section.uuid}"/>
                </h:commandLink>
                <h:commandLink
	                id="unjoin"
                    value="#{msgs.edit_student_sections_drop}"
                    actionListener="#{editStudentSectionsBean.processDrop}"
                    rendered="#{section.member}">
                    <f:param name="sectionUuid" value="#{section.uuid}"/>
                </h:commandLink>
            </h:column>
    
        </t:dataTable>

        <script type="text/javascript">includeWebjarLibrary('datatables');</script>
        <script type="text/javascript">includeWebjarLibrary('datatables-rowgroup')</script>
        <script type="text/javascript">
            $(document).ready(function () {
                $('#editSectionForm\\:editStudentSectionsTable').DataTable({
                    order: [[0, 'asc']],
                    ordering: false,
                    paging: false,
                    info: false,
                    searching: false,
                    rowGroup: {
                        dataSrc: 1,
                        className: 'categoryHeader',
                        startClassName: 'firstCategoryHeader'
                    },
                    columnDefs: [ {
                        targets: [0, 1],
                        visible: false
                    } ]
                });
            });
        </script>

        <t:div styleClass="verticalPadding" rendered="#{empty editStudentSectionsBean.sections}">
            <h:outputText value="#{msgs.no_sections_available}"/>
        </t:div>

</h:form>
</div>
</f:view>
