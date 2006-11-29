<f:view>
<div class="portletBody">
<h:form id="editSectionForm">

    <sakai:flowState bean="#{editStudentSectionsBean}"/>

    <x:aliasBean alias="#{viewName}" value="editSection">
        <%@include file="/inc/navMenu.jspf"%>
    </x:aliasBean>

        <h3>
            <h:outputFormat value="#{msgs.edit_student_sections_page_header}">
                <f:param value="#{editStudentSectionsBean.studentName}"/>
            </h:outputFormat>
        </h3>
        
        <%@include file="/inc/globalMessages.jspf"%>
    
        <x:div styleClass="instructions">
            <h:outputText value="#{msgs.edit_student_sections_instructions}"/>
        </x:div>

        <sec:sectionTable cellpadding="0" cellspacing="0"
            id="editStudentSectionsTable"
            value="#{editStudentSectionsBean.sections}"
            var="section"
            sortColumn="#{editStudentSectionsBean.sortColumn}"
            sortAscending="#{editStudentSectionsBean.sortAscending}"
            styleClass="listHier"
	        rules="rows"
        	columnClasses="leftIndent,left,left,left,left,center,center"
            >
    
            <h:column>
                <f:facet name="header">
                    <x:commandSortHeader columnName="title" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_title}" />
                    </x:commandSortHeader>
                </f:facet>
                <h:outputText value="#{section.title}"/>
            </h:column>

            <h:column>
                <f:facet name="header">
                    <x:commandSortHeader columnName="instructor" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_instructor}" />
                    </x:commandSortHeader>
                </f:facet>
                <x:dataList id="instructorName"
                    var="instructorName"
                    value="#{section.instructorNames}"
                    layout="simple">
                    <x:div>
                        <h:outputText value="#{instructorName}" />
                    </x:div>
               </x:dataList>
            </h:column>

	        <h:column>
	            <f:facet name="header">
	                <x:commandSortHeader columnName="meetingDays" immediate="false" arrow="true">
	                	<h:outputText value="#{msgs.student_view_header_day}" />
	                </x:commandSortHeader>
	            </f:facet>
	            <x:dataList id="meetingDayList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
		            <x:div>
		            	<h:outputText value="#{meeting.abbreviatedDays}"/>
		            </x:div>
	            </x:dataList>
	        </h:column>
            <h:column>
                <f:facet name="header">
                    <x:commandSortHeader columnName="meetingTimes" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_time}" />
                    </x:commandSortHeader>
                </f:facet>
	            <x:dataList id="meetingTimeList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
		            <x:div>
			            <h:outputText value="#{meeting.times}"/>
		            </x:div>
	            </x:dataList>
            </h:column>
                        
            <h:column>
                <f:facet name="header">
                    <x:commandSortHeader columnName="location" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_location}" />
                    </x:commandSortHeader>
                </f:facet>
	            <x:dataList id="meetingLocationList" value="#{section.decoratedMeetings}" var="meeting" layout="simple">
		            <x:div>
		            	<h:outputText value="#{meeting.location}"/>
		            </x:div>
	            </x:dataList>
            </h:column>

            <h:column>
                <f:facet name="header">
                    <x:commandSortHeader columnName="available" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_available}" />
                    </x:commandSortHeader>
                </f:facet>
            	<h:outputText value="#{section.spotsAvailable}"/>
            </h:column>

            <h:column>
                <h:commandLink
                    value="#{msgs.edit_student_sections_assign}"
                    actionListener="#{editStudentSectionsBean.processJoinSection}"
                    rendered="#{ ! section.member}">
                    <f:param name="sectionUuid" value="#{section.uuid}"/>
                </h:commandLink>
                <h:outputText
                    value="#{msgs.student_view_member}"
                    styleClass="formLabel"
                    rendered="#{section.member}"/>
            </h:column>
    
        </sec:sectionTable>

        <x:div styleClass="verticalPadding" rendered="#{empty editStudentSectionsBean.sections}">
            <h:outputText value="#{msgs.no_sections_available}"/>
        </x:div>

</h:form>
</div>
</f:view>
