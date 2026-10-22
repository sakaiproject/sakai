package org.sakaiproject.attendance.tool.pages;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.extensions.markup.html.form.DateTextField;
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
import java.util.Date;


public class EventInputPage extends BasePage{
    private static final long serialVersionUID = 1L;
    private IModel<AttendanceEvent> eventModel;
    private AttendanceEvent attendanceEvent;
    private ModalWindow window;
    private boolean isEditing;
    private boolean recursiveAddAnother;
    @Getter @Setter private String nextPage;
    public static String STUDENT_OVERVIEW = "org.sakaiproject.attendance.tool.pages.StudentView";
    public static String GRADING = "org.sakaiproject.attendance.tool.pages.GradingPage";
    public static String SETTINGS = "org.sakaiproject.attendance.tool.pages.SettingsPage";
    public static String IMPORTEXPORT = "org.sakaiproject.attendance.tool.pages.ExportPage";

    public EventInputPage(final IModel<AttendanceEvent> event) {
        this.isEditing = true;
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

        final IModel<Date> startDateModel = new IModel<Date>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Date getObject() {
                Instant start = eventModel.getObject().getStartDateTime();
                return start != null ? Date.from(start) : null;
            }

            @Override
            public void setObject(Date object) {
                eventModel.getObject().setStartDateTime(object != null ? object.toInstant() : null);
            }

            @Override
            public void detach() {
                eventModel.detach();
            }
        };

        final DateTextField startDateTime = new DateTextField("startDateTime", startDateModel, "yyyy-MM-dd'T'HH:mm");

        eventForm.add(name);
        eventForm.add(startDateTime);
        return eventForm;

    }

    private void processSave(AjaxRequestTarget target, Form<?> form, boolean addAnother) {
        AttendanceEvent e = (AttendanceEvent) form.getModelObject();
        e.setAttendanceSite(attendanceLogic.getCurrentAttendanceSite());
        boolean result = attendanceLogic.updateAttendanceEvent(e);
        if(result){
            StringResourceModel temp = new StringResourceModel("attendance.add.success", this);
            temp.setParameters(e.getName());
            getSession().success(temp.getString());
        } else {
            error(getString("attendance.add.failure"));
            target.addChildren(form, FeedbackPanel.class);
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
