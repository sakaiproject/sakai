package org.sakaiproject.attendance.tool.pages;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.markup.html.form.datetime.LocalDateTimeTextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.attendance.model.AttendanceEvent;
import org.sakaiproject.attendance.tool.util.AttendanceFeedbackPanel;
import org.sakaiproject.attendance.tool.util.PlaceholderBehavior;

import java.time.Instant;
import java.time.LocalDateTime;


@Slf4j
public class EventInputPage extends BasePage{
    private static final long serialVersionUID = 1L;
    private IModel<AttendanceEvent> eventModel;
    private AttendanceEvent attendanceEvent;
    @Getter @Setter private String nextPage;
    public static String STUDENT_OVERVIEW = "org.sakaiproject.attendance.tool.pages.StudentView";
    public static String GRADING = "org.sakaiproject.attendance.tool.pages.GradingPage";
    public static String SETTINGS = "org.sakaiproject.attendance.tool.pages.SettingsPage";
    public static String IMPORTEXPORT = "org.sakaiproject.attendance.tool.pages.ExportPage";

    public EventInputPage(final IModel<AttendanceEvent> event) {
        this.eventModel = event;
        attendanceEvent = event.getObject();
        disableLink(this.addLink);
        Form<AttendanceEvent> eventForm = createEventInputForm();
        this.add(eventForm);
    }

    private Form<AttendanceEvent> createEventInputForm() {
        Form<AttendanceEvent> eventForm = new Form<>("event", this.eventModel);
        eventForm.add(new AttendanceFeedbackPanel("addEditItemFeedback"));
        eventForm.add(new Label("addItemTitle", getString("event.add")));
        AjaxSubmitLink submit = createSubmitLink("submit", eventForm, false);
        submit.add(new Label("submitLabel", new ResourceModel("attendance.add.create")));
        eventForm.add(submit);
        AjaxSubmitLink submitMore = createSubmitLink("submitAndAddAnother", eventForm, true);
        submitMore.add(new Label("submitMoreLabel", new ResourceModel("attendance.add.create.another")));
        eventForm.add(submitMore);
        final AjaxButton cancel = new AjaxButton("cancel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(final AjaxRequestTarget target) {
                // decide where to go based on where we came from.
                String nextPage = EventInputPage.this.getNextPage();
                if(StringUtils.equals(nextPage, EventInputPage.GRADING)) {
                    setResponsePage(new GradingPage());
                }else if(StringUtils.equals(nextPage, EventInputPage.IMPORTEXPORT)) {
                    setResponsePage(new ExportPage());
                }else if(StringUtils.equals(nextPage, EventInputPage.SETTINGS)) {
                    setResponsePage(new SettingsPage());
                }else if(StringUtils.equals(nextPage, EventInputPage.STUDENT_OVERVIEW)){
                    setResponsePage(new StudentView());
                }else{
                    setResponsePage(new Overview());
                }
            }
        };
        cancel.setDefaultFormProcessing(false);
        eventForm.add(cancel);
        final TextField<String> name = new TextField<>("name");
        name.setRequired(true);
        name.add(new PlaceholderBehavior(getString("event.placeholder.name")));

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

        eventForm.add(name);
        eventForm.add(startDateTime);
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
        if(addAnother) {
            setResponsePage(new EventInputPage(new CompoundPropertyModel<>(new AttendanceEvent())));
        } else {
            setResponsePage(Overview.class);
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
                return super.isVisible();
            }
        };
    }

}
