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

package org.sakaiproject.attendance.tool.panels;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.component.cover.ServerConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceRecordFormDataPanel is a panel used to display the data contained within an AttendanceRecord
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceRecordFormDataPanel extends BasePanel {
    private static final    long                        serialVersionUID = 1L;
    private                 IModel<AttendanceRecord>    recordIModel;
    private                 boolean                     restricted ;
    private                 boolean                     showCommentsToStudents;
    private                 List<Component>             ajaxTargets = new ArrayList<Component>();
    private                 String                      returnPage;
    private                 Status                      oldStatus;

    private                 WebMarkupContainer          commentContainer;
    private                 WebMarkupContainer          noComment;
    private                 WebMarkupContainer          yesComment;

    public AttendanceRecordFormDataPanel(String id, IModel<AttendanceRecord> aR,  String rP, FeedbackPanel fP) {
        super(id, aR);
        this.recordIModel = aR;
        this.oldStatus = aR.getObject().getStatus();
        this.showCommentsToStudents = recordIModel.getObject().getAttendanceEvent().getAttendanceSite().getShowCommentsToStudents();
        this.restricted = this.role != null && this.role.equals("Student");
        this.returnPage = rP;
        enable(fP);
        this.ajaxTargets.add(this.pageFeedbackPanel);

        add(createRecordInputForm());
    }

    private Form<AttendanceRecord> createRecordInputForm() {
        Form<AttendanceRecord> recordForm = new Form<AttendanceRecord>("attendanceRecord", this.recordIModel) {
            protected void onSubmit() {
                AttendanceRecord aR = (AttendanceRecord) getDefaultModelObject();
                if(aR.getStatus() == null) {
                    aR.setStatus(Status.UNKNOWN);
                }
                boolean result = attendanceLogic.updateAttendanceRecord(aR, oldStatus);
                String[] resultMsgVars = new String[]{sakaiProxy.getUserSortName(aR.getUserID()), aR.getAttendanceEvent().getName(), getStatusString(aR.getStatus())};
                StringResourceModel temp;
                if(result){
                    temp = new StringResourceModel("attendance.record.save.success", null, resultMsgVars);
                    getSession().info(temp.getString());
                    oldStatus = aR.getStatus();
                } else {
                    temp = new StringResourceModel("attendance.record.save.failure", null, resultMsgVars);
                    getSession().error(temp.getString());
                }
            }

            @Override
            public boolean isEnabled() {
                return !recordIModel.getObject().getAttendanceEvent().getAttendanceSite().getIsSyncing();
            }
        };

        createStatusRadio(recordForm);
        createCommentBox(recordForm);

        boolean noRecordBool = recordForm.getModelObject().getStatus().equals(Status.UNKNOWN) && restricted;
        recordForm.setVisibilityAllowed(!noRecordBool);

        WebMarkupContainer noRecordContainer = new WebMarkupContainer("no-record");
        noRecordContainer.setVisibilityAllowed(noRecordBool);
        add(noRecordContainer);

        return recordForm;
    }

    private void createStatusRadio(final Form<AttendanceRecord> rF) {
        AttendanceStatusProvider attendanceStatusProvider = new AttendanceStatusProvider(attendanceLogic.getCurrentAttendanceSite(), AttendanceStatusProvider.ACTIVE);
        DataView<AttendanceStatus> attendanceStatusRadios = new DataView<AttendanceStatus>("status-radios", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                final Status itemStatus = item.getModelObject().getStatus();
                Radio statusRadio = new Radio<Status>("record-status", new Model<Status>(itemStatus));
                item.add(statusRadio);
                statusRadio.add(new AjaxFormSubmitBehavior(rF, "onclick") {
                    protected void onSubmit(AjaxRequestTarget target) {
                        target.appendJavaScript("attendance.recordFormRowSetup("+ this.getAttributes().getFormId() + ");");
                        for (Component c : ajaxTargets) {
                            target.add(c);
                        }
                    }
                });
                ajaxTargets.add(statusRadio);
                statusRadio.setLabel(Model.of(getStatusString(itemStatus))); //main label; it is the raw Status name. It will not be visible
                SimpleFormComponentLabel normalLabel = new SimpleFormComponentLabel("record-status-name-normal", statusRadio); //secondary label with normal-looking version of the status, for display in mobile view
                normalLabel.add(new AttributeAppender("content", getStatusString(itemStatus)));
                statusRadio.setLabel(Model.of("")); //second label for creating a colored box
                SimpleFormComponentLabel clickBox = new SimpleFormComponentLabel("record-status-box", statusRadio);
                AttributeAppender iconMaker = null;
                switch (getStatusString(itemStatus)){   //make icon based on what status it is.
                    case "Present":
                        iconMaker = new AttributeAppender("class", " fa fa-check");
                        break;
                    case "Absent":
                        iconMaker = new AttributeAppender("class", " fa fa-times");
                        break;
                    case "Excused":
                        iconMaker = new AttributeAppender("class", " fa fa-genderless");
                        break;
                    case "Late":
                        iconMaker = new AttributeAppender("class", " fa fa-clock-o");
                        break;
                    case "Left Early":
                        iconMaker = new AttributeAppender("class", " fa fa-sign-out");
                        break;
                    default:
                        iconMaker = new AttributeAppender("class", " fa fa-check");
                        break;
                }
                clickBox.add(iconMaker);
                item.add(new SimpleFormComponentLabel("record-status-name", statusRadio));
                item.add(clickBox);
                item.add(new Label("record-status-name-raw", itemStatus.toString()));
                item.add(normalLabel);
            }
        };

        RadioGroup group = new RadioGroup<Status>("attendance-record-status-group", new PropertyModel<Status>(this.recordIModel,"status"));
        group.setOutputMarkupPlaceholderTag(true);
        group.setRenderBodyOnly(false);
        group.add(attendanceStatusRadios);
        group.setEnabled(!this.restricted);

        rF.add(group);
    }

    private void createCommentBox(final Form<AttendanceRecord> rF) {

        commentContainer = new WebMarkupContainer("comment-container");
        commentContainer.setOutputMarkupId(true);

        noComment = new WebMarkupContainer("no-comment");
        noComment.setOutputMarkupId(true);

        yesComment = new WebMarkupContainer("yes-comment");
        yesComment.setOutputMarkupId(true);

        if(recordIModel.getObject().getComment() != null && !recordIModel.getObject().getComment().equals("")) {
            noComment.setVisible(false);
        } else {
            yesComment.setVisible(false);
        }

        commentContainer.add(noComment);
        commentContainer.add(yesComment);

        final TextArea<String> commentBox = new TextArea<String>("comment", new PropertyModel<String>(this.recordIModel, "comment"));

        final AjaxSubmitLink saveComment = new AjaxSubmitLink("save-comment") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);
                if(recordIModel.getObject().getComment() != null && !recordIModel.getObject().getComment().equals("")) {
                    noComment.setVisible(false);
                    yesComment.setVisible(true);
                } else {
                    noComment.setVisible(true);
                    yesComment.setVisible(false);
                }
                commentContainer.addOrReplace(noComment);
                commentContainer.addOrReplace(yesComment);
                for (Component c : ajaxTargets) {
                    target.add(c);
                }
            }
        };

        commentContainer.add(saveComment);
        commentContainer.add(commentBox);

        ajaxTargets.add(commentContainer);

        if(restricted) {
            commentContainer.setVisible(showCommentsToStudents);
            saveComment.setVisible(!showCommentsToStudents);
            commentBox.setEnabled(!showCommentsToStudents);
            noComment.setVisible(!showCommentsToStudents);
            commentContainer.add(new Label("add-header", new ResourceModel("attendance.record.form.view.comment")));
        } else {
            commentContainer.add(new Label("add-header", new ResourceModel("attendance.record.form.add.comment")));
        }

        rF.add(commentContainer);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        final String version = ServerConfigurationService.getString("portal.cdn.version", "");
        response.render(JavaScriptHeaderItem.forUrl(String.format("javascript/attendanceRecordForm.js?version=%s", version)));
        response.render(OnDomReadyHeaderItem.forScript("attendance.recordFormSetup();"));
    }
}
