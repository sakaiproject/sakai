<f:view>
<div class="portletBody">
<h:form id="studentViewForm">
<%/*
    Due to the limited screen real estate available in sakai iframes, several
    columns have been removed from this view.  The standalone version has
    retained these extra columns.  When the portal learns to live without iframes,
    the standalone version of this page should replace this one.
*/%>

    <sakai:flowState bean="#{studentViewBean}"/>

        <h3><h:outputText value="#{msgs.student_view_page_header}"/></h3>
    
        <t:div styleClass="instructions" rendered="#{not empty studentViewBean.instructions}">
            <h:outputText value="#{studentViewBean.instructions}"/>
        </t:div>

        <%@ include file="/inc/globalMessages.jspf"%>

		<t:div>
			<h:outputText value="#{msgs.student_view_view}"/>
	        <h:selectOneMenu value="#{studentViewBean.filter}" onchange="this.form.submit()">
                <f:selectItem itemLabel="#{msgs.student_view_all} #{msgs.student_view_sections}" itemValue=""/>
                <f:selectItem itemLabel="#{msgs.student_view_my} #{msgs.student_view_sections}" itemValue="MY"/>
	            <f:selectItems value="#{studentViewBean.categorySelectItems}"/>
	        </h:selectOneMenu>
		</t:div>
	    
        <sec:rowGroupTable cellpadding="0" cellspacing="0"
            id="studentViewSectionsTable"
            value="#{studentViewBean.sections}"
            var="section"
            sortColumn="#{studentViewBean.sortColumn}"
            sortAscending="#{studentViewBean.sortAscending}"
        	styleClass="listHier sectionTable"
        	columnClasses="leftIndent,left,left,left,left,center,center"
            >
    
            <h:column>
                <f:facet name="header">
                    <t:commandSortHeader columnName="title" immediate="true" arrow="true">
                        <h:outputText value="#{msgs.student_view_header_title}" />
                    </t:commandSortHeader>
                </f:facet>
                <h:outputText value="#{section.title}"  styleClass="studentSectionInfo" rendered="#{section.member}"/>
                <h:outputText value="#{section.title}"  rendered="#{ ! section.member}"/>
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
            	<t:div rendered="#{ ! section.readOnly }">
                <h:commandLink
                    actionListener="#{studentViewBean.processJoinSection}"
                    rendered="#{section.joinable && studentViewBean.joinAllowed}">
                    <f:param name="sectionUuid" value="#{section.uuid}"/>
										<h:outputText value="#{msgs.student_view_join} " />	
										<h:outputFormat value="#{msgs.student_view_join_sc}" styleClass="skip">
								        <f:param value="#{section.title}"/>
								    </h:outputFormat>

                </h:commandLink>
                <h:commandLink
                    actionListener="#{studentViewBean.processSwitchSection}"
                    rendered="#{section.switchable && studentViewBean.switchAllowed}">
                    <f:param name="sectionUuid" value="#{section.uuid}"/>
										<h:outputText value="#{msgs.student_view_switch} " />	
								    <h:outputFormat value="#{msgs.student_view_switch_sc}" styleClass="skip">
								        <f:param value="#{section.title}"/>
								    </h:outputFormat>

                </h:commandLink>
                </t:div>
                <h:outputText
                    value="#{msgs.student_view_full}"
                    rendered="#{section.full}"/>
                <h:outputText
                	styleClass="studentSectionInfo"
                	value="#{msgs.student_view_member}"
                    rendered="#{section.member}"/>
            </h:column>
    
        </sec:rowGroupTable>

        <t:div styleClass="verticalPadding" rendered="#{empty studentViewBean.sections && ! studentViewBean.siteWithoutSections}">
            <h:outputText value="#{msgs.student_view_no_sections_to_display}"/>
        </t:div>

        <t:div styleClass="verticalPadding" rendered="#{studentViewBean.siteWithoutSections}">
            <h:outputText value="#{msgs.no_sections_available}"/>
        </t:div>

</h:form>
</div>
</f:view>
