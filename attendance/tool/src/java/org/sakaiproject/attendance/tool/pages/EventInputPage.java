package org.sakaiproject.attendance.tool.pages;
import org.apache.commons.codec.binary.StringUtils;
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

public class EventInputPage extends BasePage{
    private static final long serialVersionUID = 1L;
    private IModel<AttendanceEvent> eventModel;
    private AttendanceEvent attendanceEvent;
    private ModalWindow window;
    private boolean isEditing;
    private boolean recursiveAddAnother;
    private String nextPage;
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
            public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
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
        final TextField name = new TextField<String>("name") {
            @Override
            protected void onInitialize(){
                super.onInitialize();
                add(new PlaceholderBehavior(getString("event.placeholder.name")));
            }
        };
        final DateTimeField startDateTime = new DateTimeField("startDateTime");
        name.setRequired(true);
        eventForm.add(name);
        eventForm.add(startDateTime);
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
        if(addAnother) {
            setResponsePage(new EventInputPage(new CompoundPropertyModel<>(new AttendanceEvent())));
        } else {
            setResponsePage(Overview.class);
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
                return super.isVisible();
            }
        };
    }

    public void setNextPage(String canonicalName){
        this.nextPage = canonicalName;
    }

    public String getNextPage(){
        return this.nextPage;
    }
}
