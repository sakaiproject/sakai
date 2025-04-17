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


import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.attendance.model.AttendanceGrade;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.AttendanceUserStats;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.tool.models.ProfileImage;
import org.sakaiproject.attendance.tool.panels.AttendanceGradePanel;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * StudentOverview is an overview of all the students and their statistics in the AttendanceSite
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class StudentOverview extends BasePage {
    private static final long serialVersionUID = 1L;

    private AttendanceStatusProvider attendanceStatusProvider;

    private DropDownChoice<String> groupChoice;
    private String selectedGroup;

    public StudentOverview() {
        disableLink(this.studentOverviewLink);

        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);

        add(createHeader());
        add(createStatsTable());
    }

    public StudentOverview(String selectedGroup) {
        disableLink(this.studentOverviewLink);

        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        this.selectedGroup = selectedGroup;

        this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);

        add(createHeader());
        add(createStatsTable());
    }

    private WebMarkupContainer createHeader() {
        WebMarkupContainer  contain         = new WebMarkupContainer("student-overview-header");
        Label               title           = new Label("student-overview-title", new ResourceModel("attendance.student.overview.title"));
        Label               subtitle        = new Label("student-overview-subtitle", new ResourceModel("attendance.student.overview.subtitle"));

        contain.add(title);
        contain.add(subtitle);

        return contain;
    }

    private WebMarkupContainer createStatsTable() {
        WebMarkupContainer  statsTable      = new WebMarkupContainer("student-overview-stats-table");

        createStatsTableHeader(statsTable);
        createStatsTableData(statsTable);

        return statsTable;
    }

    private void createStatsTableHeader(WebMarkupContainer t) {
        //headers for the table
        Label               studentName     = new Label("header-student-name",       new ResourceModel("attendance.header.student"));
        Label               studentPhoto    = new Label("header-student-photo",       new ResourceModel("attendance.header.photo"));
        Label               grade           = new Label("header-grade",               new ResourceModel("attendance.header.grade"));
        Label               totalPoints     = new Label("total-points", "Total: " + attendanceLogic.getCurrentAttendanceSite().getMaximumGrade());

        DataView<AttendanceStatus> statusHeaders = new DataView<AttendanceStatus>("status-headers", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                item.add(new Label("header-status-name", getStatusString(item.getModelObject().getStatus())));
            }
        };

        Link<Void>          settings        = new Link<Void>("settings-link") {
            private static final long serialVersionUID = 1L;

            public void onClick() {
                setResponsePage(new GradingPage());
            }
        };

        t.add(studentName);
        t.add(studentPhoto);
        t.add(grade);
        t.add(totalPoints);
        t.add(statusHeaders);
        t.add(settings);
    }

    private void createStatsTableData(WebMarkupContainer t) {
        final Map<String, AttendanceGrade> gradeMap = attendanceLogic.getAttendanceGrades();

        // Add form to filter table
        final Form<?> filterForm = new Form<Void>("filter-table-form"){
            @Override
            protected void onSubmit() {
                setResponsePage(new StudentOverview(groupChoice.getModelObject()));
            }
        };

        add(filterForm);

        List<String> groupIds = sakaiProxy.getAvailableGroupsForCurrentSite();
        Collections.sort(groupIds, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return sakaiProxy.getGroupTitleForCurrentSite(o1).compareTo(sakaiProxy.getGroupTitleForCurrentSite(o2));
            }
        });
        groupChoice = new DropDownChoice<String>("group-choice", new PropertyModel<String>(this, "selectedGroup"), groupIds, new IChoiceRenderer<String>() {
            @Override
            public Object getDisplayValue(String s) {
                return sakaiProxy.getGroupTitleForCurrentSite(s);
            }

            @Override
            public String getIdValue(String s, int i) {
                return s;
            }
        });
        groupChoice.setNullValid(true);

        groupChoice.add(new AjaxFormSubmitBehavior("onchange") {
            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
            }
        });
        filterForm.add(groupChoice);
        filterForm.add(new Label("group-choice-label", new ResourceModel("attendance.event.view.filter")));

        List<AttendanceUserStats> userStatsList = attendanceLogic.getUserStatsForCurrentSite(selectedGroup);
        final ListView<AttendanceUserStats> uListView = new ListView<AttendanceUserStats>("students", userStatsList) {
            @Override
            protected void populateItem(ListItem<AttendanceUserStats> item) {
                final String id = item.getModelObject().getUserID();
                Link<Void> studentLink = new Link<Void>("student-link") {
                    public void onClick() {
                        setResponsePage(new StudentView(id, BasePage.STUDENT_OVERVIEW_PAGE));
                    }
                };
                studentLink.add(new Label("student-name", sakaiProxy.getUserSortName(id) + " (" + sakaiProxy.getUserDisplayId(id) + ")"));
                item.add(studentLink);

                ProfileImage profilePhoto = new ProfileImage("student-photo", new Model<String>(String.format("/direct/profile/%s/image/official?siteId=%s", id, sakaiProxy.getCurrentSiteId())));
                item.add(profilePhoto);

                DataView<AttendanceStatus> activeStatusStats = new DataView<AttendanceStatus>("active-status-stats", attendanceStatusProvider) {
                    @Override
                    protected void populateItem(Item<AttendanceStatus> statusItem) {
                        Status itemStatus = statusItem.getModelObject().getStatus();
                        int stat = attendanceLogic.getStatsForStatus(item.getModelObject(), itemStatus);
                        statusItem.add(new Label("student-stats", stat));
                    }
                };
                item.add(activeStatusStats);
                item.add(new AttendanceGradePanel("attendance-grade", gradeMap.get(id), feedbackPanel));
            }
        };

        Label noStudents = new Label("no-students", new ResourceModel("attendance.student.overview.no.students")) {
            @Override
            public boolean isVisible(){
                return uListView.size() <= 0;
            }
        };
        Label noStudents2 = new Label("no-students2", new ResourceModel("attendance.student.overview.no.students.2")) {
            @Override
            public boolean isVisible(){
                return uListView.size() <= 0;
            }
        };

        t.add(uListView);
        t.add(noStudents);
        t.add(noStudents2);
    }
}
