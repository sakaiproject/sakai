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

import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.attendance.model.*;

import java.util.*;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.RestartResponseException;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;

import org.apache.wicket.model.ResourceModel;



/**
 * Created by james on 6/8/17.
 */
public class ImportConfirmation  extends BasePage{
    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        disableLink(exportLink);


        add(new ImportConfirmation.UploadForm("form"));
    }
    private static final long serialVersionUID = 1L;
    private AttendanceStatusProvider attendanceStatusProvider;
    private DropDownChoice<String> groupChoice;
    private String selectedGroup;
    private List<ImportConfirmList> uploadICLList;

    public ImportConfirmation(List<ImportConfirmList> attendanceItemDataList, Boolean commentsChanged) {

        this.uploadICLList = attendanceItemDataList;
        homepageLink = new Link<Void>("homepage-link2") {
            private static final long serialVersionUID = 1L;
            public void onClick() {

                setResponsePage(new Overview());
            }
        };
        homepageLink.add(new Label("homepage-link-label",new ResourceModel("attendance.link.homepage")).setRenderBodyOnly(true));
        homepageLink.add(new AttributeModifier("title", new ResourceModel("attendance.link.homepage.tooltip")));
        add(homepageLink);

        if(this.role != null && this.role.equals("Student")) {
            throw new RestartResponseException(StudentView.class);
        }

        this.attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);

        add(createStatsTable(attendanceItemDataList, commentsChanged));
    }

    private WebMarkupContainer createStatsTable(List<ImportConfirmList> attendanceItemDataList, boolean commentsChanged) {
        WebMarkupContainer statsTable = new WebMarkupContainer("student-overview-stats-table");
        createStatsTableHeader(statsTable, commentsChanged);
        createStatsTableData(statsTable, attendanceItemDataList, commentsChanged);
        return statsTable;
    }

    private void createStatsTableHeader(WebMarkupContainer attendanceItemTableContainer, boolean commentsChanged) {
        WebMarkupContainer oldCommentContainer = new WebMarkupContainer("old-comment-header") {
            @Override
            public boolean isVisible() {
                return commentsChanged;
            }
        };
        WebMarkupContainer newCommentContainer = new WebMarkupContainer("new-comment-header"){
            @Override
            public boolean isVisible() {
                return commentsChanged;
            }
        };
        attendanceItemTableContainer.add(oldCommentContainer);
        attendanceItemTableContainer.add(newCommentContainer);
        oldCommentContainer.add(new Label("old-comment", "Old Comment"));
        newCommentContainer.add(new Label("new-comment", "New Comment"));
    }

    private void createStatsTableData(WebMarkupContainer attendanceItemTableContainer, List<ImportConfirmList> attendanceItemDataList, boolean commentsChanged) {
        final Map<String, AttendanceGrade> gradeMap = attendanceLogic.getAttendanceGrades();

        final ListView<ImportConfirmList> uListView = new ListView<ImportConfirmList>("students", attendanceItemDataList) {
            int importConfirmListIndex = 0;
            @Override
            protected void populateItem(ListItem<ImportConfirmList> item) {
                WebMarkupContainer newCommentwmc = new WebMarkupContainer("new-comment-wmc");
                WebMarkupContainer oldCommentwmc = new WebMarkupContainer("old-comment-wmc");
                String stat = "";
                if(attendanceItemDataList.get(importConfirmListIndex).getEventDate().equals("NODATE")){
                    item.add(new Label("event-name-label", String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getEventName())));
                } else {
                    item.add(new Label("event-name-label", String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getEventName()) + "[" + String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getEventDate()) + "]"));
                }
                item.add(new Label("student-name-label", sakaiProxy.getUserSortName(attendanceItemDataList.get(importConfirmListIndex).getUserID())));

                stat =String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getOldStatus());
                stat = changeStatString(stat);
                item.add(new Label("old-status-label", stat));

                stat = String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getOldComment());
                stat = changeStatString(stat);
                item.add(oldCommentwmc);
                oldCommentwmc.add(new Label("old-comment-label", stat));
                if(commentsChanged){
                    oldCommentwmc.setVisible(true);
                } else {
                    oldCommentwmc.setVisible(false);
                }

                stat = String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getStatus());
                stat = changeStatString(stat);
                item.add(new Label("new-status-label", stat));


                stat = String.valueOf(attendanceItemDataList.get(importConfirmListIndex).getComment());
                stat = changeStatString(stat);
                item.add(newCommentwmc);
                newCommentwmc.add(new Label("new-comment-label", stat));
                if(commentsChanged){
                    newCommentwmc.setVisible(true);
                } else {
                    newCommentwmc.setVisible(false);
                }

                importConfirmListIndex++;
            }
        };

        Label noChanges = new Label("no-changes", new ResourceModel("attendance.export.import.save.noChange")) {
            @Override
            public boolean isVisible(){
                return uListView.size() <= 0;
            }
        };
        attendanceItemTableContainer.add(uListView);
        attendanceItemTableContainer.add(noChanges);
    }

    private String changeStatString(String stat){
        if(stat.equals("PRESENT")){
            stat = "Present";
        } else if(stat.equals("UNEXCUSED_ABSENCE")){
            stat = "Unexcused Absence";
        } else if(stat.equals("EXCUSED_ABSENCE")){
            stat = "Excused Absence";
        } else if(stat.equals("LATE")){
            stat = "Late";
        } else if(stat.equals("LEFT_EARLY")){
            stat = "Left Early";
        } else if(stat.equals("UNKNOWN")){
            stat = "";
        } else if(stat.equals("null")){
            stat = "";
        }
        return stat;
    }

    private class UploadForm extends Form<Void> {

        public UploadForm(final String id) {
            super(id);
            SubmitLink completeImport = new SubmitLink("submitLink") {
                public void onSubmit() {
                    for (int i = 0; i < uploadICLList.size(); i++){
                        boolean updated = attendanceLogic.updateAttendanceRecord(uploadICLList.get(i).getAttendanceRecord(), uploadICLList.get(i).getOldStatus());
                        attendanceLogic.updateAttendanceSite(uploadICLList.get(i).getAttendanceSite());
                    }
                    getSession().success(getString("attendance.export.confirmation.import.save.success"));
                    setResponsePage(new Overview());
                }
            };
            Label lowerErrorAlert = new Label("lowerErrorAlert");
            if (!getSession().getFeedbackMessages().isEmpty()){
                completeImport.setEnabled(false);
                lowerErrorAlert = new Label("lowerErrorAlert", getString("attendance.import.errors.exist"));
            }
            if(uploadICLList.size()<1){
                getSession().error(getString("attendance.export.import.save.noChange"));
                completeImport.setEnabled(false);
            }
            add(lowerErrorAlert);
            add(completeImport);
            add(new SubmitLink("submitLink2") {
                public void onSubmit() {
                    setResponsePage(new ExportPage());
                }
            });
        }
    }

}
