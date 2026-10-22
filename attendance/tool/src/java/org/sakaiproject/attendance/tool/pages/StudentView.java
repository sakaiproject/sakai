/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.pages;


import java.time.Instant;
import java.util.Date;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceRecordProvider;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.panels.AttendanceGradePanel;
import org.sakaiproject.attendance.tool.panels.AttendanceRecordFormDataPanel;
import org.sakaiproject.attendance.tool.panels.AttendanceRecordFormHeaderPanel;
import org.sakaiproject.attendance.tool.panels.StatisticsPanel;

/**
 * StudentView is the view of a single user (a student)'s AttendanceRecords
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class StudentView extends BasePage {
    private static final    long        serialVersionUID    = 1L;
    private                 String      studentId;
    private                 Long        previousEventId;
    private                 boolean     isStudent           = false;
    private                 String      returnPage          = "";
    private                 AttendanceStatusProvider attendanceStatusProvider;

    public StudentView() {
        this.studentId = sakaiProxy.getCurrentUserId();

        init();
    }

    public StudentView(String id, String fromPage) {
        this.studentId = id;
        this.returnPage = fromPage;

        init();
    }

    public StudentView(String id, Long eventId, String fromPage) {
        this.studentId = id;
        this.previousEventId = eventId;
        this.returnPage = fromPage;

        init();
    }

    private void init() {
        this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);
        if(this.role != null && this.role.equals("Student")){
            this.isStudent = true;
            hideNavigationLink(this.addLink);
            hideNavigationLink(this.homepageLink);
            hideNavigationLink(this.studentOverviewLink);
            hideNavigationLink(this.settingsLink);
            hideNavigationLink(this.gradingLink);
            hideNavigationLink(this.exportLink);
        }

        add(new Label("student-view-hidden","") {
            @Override
            public boolean isVisible() {
                return isStudent;
            }
        });

        add(createHeader());
        add(createGrade());
        add(createStatistics());
        add(createStudentViewHeader());
        add(createTable());
    }

    private WebMarkupContainer createHeader() {
        WebMarkupContainer header = new WebMarkupContainer("header") {
            @Override
            public boolean isVisible() {
                return !isStudent;
            }
        };
        header.setOutputMarkupPlaceholderTag(true);

        WebMarkupContainer event = new WebMarkupContainer("event") {
            @Override
            public boolean isVisible() {
                return !returnPage.equals(BasePage.STUDENT_OVERVIEW_PAGE);
            }
        };

        Link<Void> eventLink = new Link<Void>("event-link") {
            @Override
            public void onClick() {
                setResponsePage(new EventView(previousEventId, returnPage));
            }
        };

        if(!isStudent && previousEventId != null) {
            eventLink.add(new Label("event-link-text", attendanceLogic.getAttendanceEvent(previousEventId).getName()));
        } else {
            eventLink.add(new Label("event-link-text", ""));
        }
        event.add(eventLink);


        header.add(event);

        Label studentName = new Label("student-name", sakaiProxy.getUserSortName(this.studentId) + " (" + sakaiProxy.getUserDisplayId(this.studentId) + ")");
        header.add(studentName);

        return header;
    }

    private WebMarkupContainer createGrade() {
        WebMarkupContainer grade = new WebMarkupContainer("grade") {
            @Override
            public boolean isVisible() {
                Boolean isGradeShown = attendanceLogic.getCurrentAttendanceSite().getIsGradeShown();
                return (isGradeShown == null ? false : isGradeShown) || !isStudent;
            }
        };

        AttendanceGradePanel attendanceGrade = new AttendanceGradePanel("attendance-grade", attendanceLogic.getAttendanceGrade(this.studentId), feedbackPanel) {
            @Override
            public boolean isEnabled() {
                return !isStudent;
            }
        };

        grade.add(attendanceGrade);

        return grade;
    }

    private AjaxLazyLoadPanel createStatistics() {
        AjaxLazyLoadPanel statsPanelSlow = new AjaxLazyLoadPanel("statsPanelSlow") {    //putting the Stats table in a lazyLoadPanel makes it load independently of the rest of the page.
            @Override
            public Component getLazyLoadComponent(String s) {
                return new StatisticsPanel(s, returnPage, studentId, previousEventId);
            }
        };
        return statsPanelSlow;
    }

    private WebMarkupContainer createStudentViewHeader() {
        WebMarkupContainer studentView = new WebMarkupContainer("student-view") {
            @Override
            public boolean isVisible() {
                return isStudent;
            }
        };

        studentView.add(new Label("student-name", sakaiProxy.getUserSortName(this.studentId) + " (" + sakaiProxy.getUserDisplayId(this.studentId) + ")"));

        return studentView;
    }

    private WebMarkupContainer createTable(){
        WebMarkupContainer studentViewData = new WebMarkupContainer("student-view-data");

        if(!isStudent) {
            studentViewData.add(new Label("take-attendance-header", getString("attendance.student.view.take.attendance")));
        } else {
            studentViewData.add(new Label("take-attendance-header", getString("attendance.student.view.attendance")));
        }
        DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-names", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {  //a label for each Status name.
                item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
            }
        };
        Label commentLabel = new Label("comments-label", "Comments");
        if(isStudent){
            commentLabel.setVisible(attendanceLogic.getCurrentAttendanceSite().getShowCommentsToStudents());    //don't show the Comments label unless students can see comments.
        }
        studentViewData.add(commentLabel);
        studentViewData.add(statusHeaders);
        studentViewData.add(new Label("event-name-header", new ResourceModel("attendance.record.form.header.event")));
        studentViewData.add(new Label("event-date-header", new ResourceModel("attendance.record.form.header.date")));
        studentViewData.add(createData());

        return studentViewData;
    }

    private DataView<AttendanceRecord> createData(){
        DataView<AttendanceRecord> dataView = new DataView<AttendanceRecord>("records", new AttendanceRecordProvider(this.studentId)) {
            @Override
            protected void populateItem(final Item<AttendanceRecord> item) {
                Link<Void> eventLink = new Link<Void>("event-link") {
                    private static final long serialVersionUID = 1L;
                    public void onClick() {
                        setResponsePage(new EventView(item.getModelObject().getAttendanceEvent(), returnPage));
                    }
                };
                eventLink.add(new Label("record-name", item.getModelObject().getAttendanceEvent().getName()));
                if(isStudent) {
                    disableLink(eventLink);
                }
                item.add(eventLink);
                Instant start = item.getModelObject().getAttendanceEvent().getStartDateTime();
                item.add(new Label("event-date", start != null ? Date.from(start) : null));
                AjaxLazyLoadPanel dataPanelSlow = new AjaxLazyLoadPanel("dataPanelSlow") {
                    @Override
                    public Component getLazyLoadComponent(String s) {
                        return new AttendanceRecordFormDataPanel(s, item.getModel(), returnPage, feedbackPanel);
                    }
                };
                item.add(dataPanelSlow);
            }
        };

        return dataView;
    }
}
