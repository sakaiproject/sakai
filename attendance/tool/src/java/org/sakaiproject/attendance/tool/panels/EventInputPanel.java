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

import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalDialog;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTimeTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import java.time.Instant;
import java.time.LocalDateTime;

import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.pages.EventView;
import org.sakaiproject.attendance.tool.pages.Overview;
import org.sakaiproject.attendance.tool.util.AttendanceFeedbackPanel;

/**
 * EventInputPanel is used to get AttendanceEvent settings for a new or existing AttendanceEvent
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
@Slf4j
public class EventInputPanel extends BasePanel {
    private static final long serialVersionUID = 1L;

    private IModel<AttendanceEvent> eventModel;
    private AttendanceEvent attendanceEvent;
    private ModalDialog window;
    private boolean isEditing;
    private boolean recursiveAddAnother;

    public EventInputPanel(String id, final ModalDialog window, final IModel<AttendanceEvent> event) {
        this(id, window, event, false);
    }

    public EventInputPanel(String id, final ModalDialog window, final IModel<AttendanceEvent> event, boolean recursive) {
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

        add(new Label("dialog-title", new ResourceModel(isEditing ? "attendance.add.edit.header" : "attendance.add.header")));
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
            public void onSubmit(final AjaxRequestTarget target) {
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
        boolean newEvent = e.getId() == null;
        boolean result;
        try {
            result = attendanceLogic.updateAttendanceEvent(e);
        } catch (org.springframework.dao.DataAccessException | jakarta.persistence.PersistenceException failure) {
            // A rolled-back identity insert may have assigned an ID to the page model.
            if (newEvent) {
                e.setId(null);
            }
            log.warn("Unable to save attendance event {}", e.getId(), failure);
            result = false;
        }

        if(result){
            StringResourceModel temp = new StringResourceModel("attendance.add.success", this);
            temp.setParameters(e.getName());
            getSession().success(temp.getString());
        } else {
            error(new StringResourceModel("attendance.add.failure", this).setParameters(e.getName()).getString());
            target.addChildren(form, FeedbackPanel.class);
            return;
        }

        final Class<? extends Page> currentPageClass = getPage().getPageClass();
        if(addAnother) {
            if(Overview.class.equals(currentPageClass)) {
                window.close(target);

                Overview overviewPage = (Overview) getPage();
                final ModalDialog window2 = overviewPage.getAddOrEditItemWindow();
                window2.setContent(new EventInputPanel(ModalDialog.CONTENT_ID, window2, null, true));
                target.addChildren(form, FeedbackPanel.class);
                window2.open(target);
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
            protected void onSubmit(AjaxRequestTarget target) {
                super.onSubmit(target);
                processSave(target, form, createAnother);
            }

            @Override
            public boolean isEnabled() {
                return !attendanceLogic.getCurrentAttendanceSite().getIsSyncing();
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
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

    private void createValues(Form<AttendanceEvent> event) {
        final TextField<String> name = new TextField<>("name");
        name.setRequired(true);

        final IModel<LocalDateTime> startDateModel = new IModel<LocalDateTime>() {
            private static final long serialVersionUID = 1L;

            @Override
            public LocalDateTime getObject() {
                Instant start = eventModel.getObject().getStartDateTime();
                return start != null ? LocalDateTime.ofInstant(start, sakaiProxy.getZoneId()) : null;
            }

            @Override
            public void setObject(LocalDateTime object) {
                eventModel.getObject().setStartDateTime(object != null ? object.atZone(sakaiProxy.getZoneId()).toInstant() : null);
            }

            @Override
            public void detach() {
                eventModel.detach();
            }
        };

        final LocalDateTimeTextField startDateTime = new LocalDateTimeTextField("startDateTime", startDateModel, "yyyy-MM-dd'T'HH:mm") {
            @Override
            protected String[] getInputTypes() {
                return new String[] { "datetime-local" };
            }
        };

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


