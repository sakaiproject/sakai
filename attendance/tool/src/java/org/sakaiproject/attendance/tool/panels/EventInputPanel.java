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

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.pages.EventView;
import org.sakaiproject.attendance.tool.pages.Overview;
import org.sakaiproject.attendance.tool.util.AttendanceFeedbackPanel;
import org.sakaiproject.attendance.tool.util.ConfirmationLink;
import org.sakaiproject.attendance.tool.util.PlaceholderBehavior;


/**
 * EventInputPanel is used to get AttendanceEvent settings for a new or existing AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class EventInputPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private IModel<AttendanceEvent> eventModel;
    private AttendanceEvent attendanceEvent;
    private ModalWindow window;
    private boolean isEditing;
    private boolean recursiveAddAnother;

    public EventInputPanel(String id, final ModalWindow window, final IModel<AttendanceEvent> event) {
        this(id, window, event, false);
    }

    public EventInputPanel(String id, final ModalWindow window, final IModel<AttendanceEvent> event, boolean recursive) {
        super(id, event);
        this.window = window;
        this.recursiveAddAnother = recursive;

        if(event == null) {
            this.isEditing = false;
            this.attendanceEvent = new AttendanceEvent();
            this.eventModel = new CompoundPropertyModel<>(attendanceEvent);
        } else {
            this.isEditing = true;
            this.eventModel = event;
            attendanceEvent = event.getObject();
        }

        add(createEventInputForm());
    }

    private Form<AttendanceEvent> createEventInputForm() {
        Form<AttendanceEvent> eventForm = new Form<>("event", this.eventModel);

        eventForm.add(new AttendanceFeedbackPanel("addEditItemFeedback"));

        AjaxSubmitLink submit = createSubmitLink("submit", eventForm, false);

        submit.add(new Label("submitLabel", getSubmitLabel()));
        eventForm.add(submit);

        eventForm.add(createSubmitLink("submitAndAddAnother", eventForm, true));

        final AjaxButton cancel = new AjaxButton("cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
                window.close(target);

                if(recursiveAddAnother) { // assumes will only occur from Overview page
                    setResponsePage(getPage().getPageClass());
                }
            }
        };
        cancel.setDefaultFormProcessing(false);
        eventForm.add(cancel);
        createValues(eventForm);

        return eventForm;
    }

    private void processSave(AjaxRequestTarget target, Form<?> form, boolean addAnother) {
        AttendanceEvent e = (AttendanceEvent) form.getModelObject();
        e.setAttendanceSite(attendanceLogic.getCurrentAttendanceSite());
        boolean result = attendanceLogic.updateAttendanceEvent(e);

        if(result){
            StringResourceModel temp = new StringResourceModel("attendance.add.success", null, new String[]{e.getName()});
            getSession().success(temp.getString());
        } else {
            error(getString("attendance.add.failure"));
            target.addChildren(form, FeedbackPanel.class);
        }

        final Class<? extends Page> currentPageClass = getPage().getPageClass();
        if(addAnother) {
            if(Overview.class.equals(currentPageClass)) {
                window.close(target);

                Overview overviewPage = (Overview) getPage();
                final ModalWindow window2 = overviewPage.getAddOrEditItemWindow();
                window2.setTitle(new ResourceModel("attendance.add.header"));
                window2.setContent(new EventInputPanel(window2.getContentId(), window2, null, true));
                target.addChildren(form, FeedbackPanel.class);
                window2.show(target);
            }
        } else {
            if(EventView.class.equals(currentPageClass)) {
                setResponsePage(new EventView(attendanceEvent, ((EventView)getPage()).getReturnPage()));
            } else {
                setResponsePage(currentPageClass);
            }
        }
    }

    private AjaxSubmitLink createSubmitLink(final String id, final Form<?> form, final boolean createAnother) {
        return new AjaxSubmitLink(id, form) {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                super.onSubmit(target, form);

                processSave(target, form, createAnother);
            }

            @Override
            public boolean isEnabled() {
                return !attendanceLogic.getCurrentAttendanceSite().getIsSyncing();
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.addChildren(form, FeedbackPanel.class);
            }

            @Override
            public boolean isVisible() {
                if(createAnother) {
                    return !isEditing;
                }

                return super.isVisible();
            }
        };
    }

    private void createValues(Form<AttendanceEvent> event){
        final TextField name = new TextField<String>("name") {
            @Override
            protected void onInitialize(){
                super.onInitialize();
                add(new PlaceholderBehavior(getString("event.placeholder.name")));
            }
        };
        final DateTimeField startDateTime = new DateTimeField("startDateTime");

        name.setRequired(true);

        event.add(name);
        event.add(startDateTime);
    }

    private ResourceModel getSubmitLabel() {
        if(this.isEditing) {
            return new ResourceModel("attendance.add.edit");
        }

        return new ResourceModel("attendance.add.create");
    }
}



