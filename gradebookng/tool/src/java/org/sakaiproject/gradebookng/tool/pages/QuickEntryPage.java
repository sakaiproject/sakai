package org.sakaiproject.gradebookng.tool.pages;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.sakaiproject.gradebookng.tool.model.GbModalWindow;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.grading.api.GradeType;
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.tool.panels.BulkGradePanel;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.SortType;

import lombok.Getter;
import lombok.Setter;
import org.sakaiproject.user.api.User;

public class QuickEntryPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private Assignment assignmentNow;
    private GbGroup groupNow;

    @Getter
    private GbModalWindow bulkGrade;
    @Getter
    private GbModalWindow bulkComment;
    private boolean noErrors = true;

    private String gradebookUid;
    private String siteId;

    public QuickEntryPage() {
        disableLink(this.quickEntryPageLink);
		
        gradebookUid = getCurrentGradebookUid();
        siteId = getCurrentSiteId();
    }

    @Override
    public void onInitialize() {
        super.onInitialize();

        GradeType gradeType = this.businessService.getGradebookSettings(gradebookUid, siteId).getGradeType();

        SortType sortBy = SortType.SORT_BY_NAME;
        final List<Assignment> assignments = this.businessService.getGradebookAssignments(gradebookUid, siteId, sortBy);
        final DropDownChoice<Assignment> itempicker = new DropDownChoice<Assignment>("itempicker", new Model<Assignment>(),assignments, new ChoiceRenderer<Assignment>(){
            private static final long serialVersionUID = 1L;
            @Override
            public Object getDisplayValue(final Assignment a) {
                return a.getName();
            }
            @Override
            public String getIdValue(final Assignment a,final int index) {
                return String.valueOf(a.getId());
            }
        });
        itempicker.add(new AjaxFormComponentUpdatingBehavior("change") {  // add the onchange to the chooser
            private static final long serialVersionUID = 1L;
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                final Assignment selection = (Assignment) itempicker.getDefaultModelObject();   //get selected assignment
                final PageParameters pageParameters = new PageParameters();
                if(groupNow != null){
                    pageParameters.add("groupNow", groupNow.getId());
                }
                pageParameters.add("selected", selection.getId());
                setResponsePage(QuickEntryPage.class, pageParameters);  // refresh this page with the selected item
            }
        });
        final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups(gradebookUid, siteId);
        final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<GbGroup>("groupicker", new Model<GbGroup>(), groups, new ChoiceRenderer<GbGroup>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getDisplayValue(final GbGroup g) {
                return g.getTitle();
            }
            @Override
            public String getIdValue(final GbGroup g, final int index) {
                return g.getId() != null ? g.getId() : "";
            }
        });
        groupFilter.add(new AjaxFormComponentUpdatingBehavior("change") {
            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                final GbGroup selectedgroup = (GbGroup) groupFilter.getDefaultModelObject();
                final PageParameters pageParameters = new PageParameters();
                if(selectedgroup != null){
                    pageParameters.add("groupNow", selectedgroup.getId());
                }
                pageParameters.add("selected",assignmentNow.getId());
                setResponsePage(QuickEntryPage.class,pageParameters);
            }
        });
        final PageParameters params = getPageParameters();
        final String selecteditem = params.get("selected").toOptionalString();
        groupFilter.setVisible(!groups.isEmpty() && !params.get("selected").isNull());  // if only one item or no assignment, hide the dropdown
        groupFilter.setNullValid(true);
        final QuickEntryPageModel pageModel = new QuickEntryPageModel();
        pageModel.setItemgrades(new ArrayList<>());

        final Form<?> form = new Form<>("form", Model.of(pageModel)) {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(){
                noErrors = true;    //reset on every submission; we should be assessing this from scratch every time
                QuickEntryPageModel dataNow = this.getModelObject();
                ArrayList<QuickEntryRowModel> allgrades = dataNow.getItemgrades();
                Long itemId = dataNow.getItemIdNow();
                for(QuickEntryRowModel row: allgrades){ //first loop vor validation only
                    try {
                        double gradeValidator = FormatHelper.validateDouble(row.getGrade());
                        if(gradeValidator<0){
                            getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                            row.setHasError(true);
                            noErrors = false;
                        } else {
                            row.setHasError(false);
                        }
                    } catch (NumberFormatException e){
                        getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                        row.setHasError(true);
                        noErrors = false;
                    } catch (NullPointerException n){
                        row.setGrade("");   //NPE is actually ok, we just need to turn it into a blank string.
                    }
                }
                if(noErrors){   //if still no errors, second loop to start saving things
                    for(QuickEntryRowModel row: allgrades){
                        String oldGrade = businessService.getGradeForStudentForItem(gradebookUid, siteId, row.getStudentid(),itemId).getGrade();
                        GradeSaveResponse succeeded = businessService.saveGrade(gradebookUid, siteId, itemId,row.getStudentid(),oldGrade,row.getGrade(),row.getComment());
                        if(succeeded == GradeSaveResponse.ERROR || succeeded == GradeSaveResponse.CONCURRENT_EDIT){
                            getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                            row.setHasError(true);
                            noErrors = false;
                            break;
                        } else {
                            row.setHasError(false);
                        }
                        if(row.commentChanged()){   //in case a changed comment is with an unchanged/blank grade
                            boolean commentSucceeded = businessService.updateAssignmentGradeComment(gradebookUid, siteId, itemId,row.getStudentid(),row.getComment());
                            if(!commentSucceeded){
                                getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                                row.setHasError(true);
                                noErrors = false;
                                break;
                            } else {
                                row.setHasError(false);
                            }
                        }
                        GradeSaveResponse succeededExcuse = businessService.saveExcuse(gradebookUid, siteId, itemId,row.getStudentid(),row.isExcused());
                        if(succeededExcuse == GradeSaveResponse.ERROR){
                            getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                            row.setHasError(true);
                            noErrors = false;
                            break;
                        } else {
                            row.setHasError(false);
                        }
                    }
                }
                if(noErrors){
                    getSession().success(MessageFormat.format(getString("quickentry.success"), assignmentNow.getName()));
                    final Assignment selection = (Assignment) itempicker.getDefaultModelObject();   //get selected assignment
                    final PageParameters pageParameters = new PageParameters();
                    final GbGroup selectedgroup = (GbGroup) groupFilter.getDefaultModelObject();
                    if(selectedgroup != null){
                        pageParameters.add("groupNow", selectedgroup.getId());
                    }
                    pageParameters.add("selected", selection.getId());
                    setResponsePage(QuickEntryPage.class, pageParameters);
                }
            }
        };
        WebMarkupContainer tableVisibility = new WebMarkupContainer("tableVisibility");
        if(StringUtils.isNotBlank(selecteditem)){
            for (final Assignment a : assignments) {
                if (selecteditem.equals(a.getId().toString())) {
                    this.assignmentNow = a;
                    pageModel.setItemIdNow(a.getId());
                    itempicker.setModel(Model.of(this.assignmentNow));  //make sure dropdown shows current assignment
                    break;
                }
            }
            for(final GbGroup g:groups){
                if(!params.get("groupNow").isNull() && params.get("groupNow").toString().equals(g.getId())){
                    this.groupNow = g;
                    groupFilter.setModel(Model.of(this.groupNow));
                    break;
                }
            }
            form.add(new Label("itemtitle", assignmentNow.getName()));
            String localePoints = FormatHelper.formatGradeForDisplay(assignmentNow.getPoints(), gradeType);
            String itemdetails = " - " + (gradeType == GradeType.PERCENTAGE ? getString("quickentry.percentages") : getString("quickentry.points")) + ": " + localePoints;
            if(assignmentNow.getExternallyMaintained()){
                itemdetails = itemdetails + " - " + MessageFormat.format(getString("quickentry.externally"),assignmentNow.getExternalAppName());
            }
            form.add(new Label("itemdetails", itemdetails));

            // The getUsers call will both sort and remove orphaned/invalid users
            final List<String> gradableUserIds = this.businessService.getGradeableUsers(gradebookUid, siteId, null);
            final List<User> gradableUsers = this.businessService.getUsers(gradableUserIds);
            Map<String, List<String>> groupContainer = this.businessService.getGroupMemberships(gradebookUid, siteId);
            List<QuickEntryRowModel> rows = new ArrayList<>();

            int totalstudents = 0;
            int studentsnow = 0;
            for (User userNow: gradableUsers) {
                final String uid = userNow.getId();
                QuickEntryRowModel rowNow = new QuickEntryRowModel();
                totalstudents++;
                if(!params.get("groupNow").isNull() && !groupContainer.get("/site/" + getCurrentSiteId() + "/group/"+params.get("groupNow").toString()).contains(uid)){
                    continue;
                }
                studentsnow++;
                rowNow.setName(userNow.getLastName() + ", " + userNow.getFirstName() + " (" + userNow.getDisplayId() + ')');
                String commentNow = this.businessService.getAssignmentGradeComment(gradebookUid, this.assignmentNow.getId(),uid);
                if(commentNow != null){
                    rowNow.setComment(commentNow);
                    rowNow.setOriginalComment(commentNow);
                } else {
                    rowNow.setComment("");
                    // Wicket inputs have empty strings converted into nulls. but since there's no component that actually backs
                    // this field, we need to set it to null
                    rowNow.setOriginalComment(null);
                }
                String gradeNow = this.businessService.getGradeForStudentForItem(gradebookUid, siteId, uid, this.assignmentNow.getId()).getGrade();
                String localeGrade = FormatHelper.formatGradeForDisplay(gradeNow, gradeType);
                rowNow.setGrade(StringUtils.defaultIfBlank(localeGrade, null));
                rowNow.setExcused(!Objects.equals(this.businessService.getAssignmentExcuse(gradebookUid, this.assignmentNow.getId(),uid), "0"));
                rowNow.setLocked(this.assignmentNow.getExternallyMaintained());
                rowNow.setMaxGrade(this.assignmentNow.getPoints());
                rowNow.setStudentid(uid);
                rows.add(rowNow);
                ((QuickEntryPageModel)form.getModelObject()).getItemgrades().add(rowNow);
            }
            form.add(new Label("summarycount",MessageFormat.format(getString("quickentry.count"),studentsnow,totalstudents)));
            ListView<QuickEntryRowModel> userFields = new ListView<QuickEntryRowModel>("studentRow",rows){
                @Override
                protected void populateItem(final ListItem<QuickEntryRowModel> item) {
                    item.add(new Label("studentName",item.getModelObject().getName()));
                    TextField<String> gradeNow = new TextField<>("studentGrade", new PropertyModel<>(item.getModelObject(),"grade"));
                    String gradeClass = "enabledGrade";
                    if(item.getModelObject().isLocked()){
                        gradeNow.setEnabled(false);
                        gradeClass = "disabledGrade";
                        item.add(new WebMarkupContainer("lockicon").add(new AttributeModifier("class","fa fa-lock")));
                    } else {
                        item.add(new WebMarkupContainer("lockicon"));
                    }
                    if(item.getModelObject().isHasError()){
                        gradeClass = gradeClass + " errorCell";
                    }
                    gradeNow.add(new AttributeModifier("class",gradeClass));
                    item.add(gradeNow);

                    item.add(new TextArea<>("studentComment", new PropertyModel<String>(item.getModelObject(), "comment")));
                    AjaxCheckBox excused = new AjaxCheckBox("studentExcuse",new PropertyModel<>(item.getModelObject(), "excused")){
                        @Override
                        public void onUpdate(AjaxRequestTarget target) {}
                    };
                    item.add(excused);
                }
            };
            tableVisibility.add(userFields);

            // Submit is disabled until user modifies a grade or comment
            final SubmitLink submit = new SubmitLink("submit");
            form.add(submit);

            Button reset = new Button("reset"){
                private static final long serialVersionUID = 1L;
                @Override
                public void onSubmit(){
                    final Assignment selection = (Assignment) itempicker.getDefaultModelObject();   //get selected assignment
                    final PageParameters pageParameters = new PageParameters();
                    final GbGroup selectedgroup = (GbGroup) groupFilter.getDefaultModelObject();
                    if(selectedgroup != null){
                        pageParameters.add("groupNow", selectedgroup.getId());
                    }
                    pageParameters.add("selected", selection.getId());
                    setResponsePage(QuickEntryPage.class, pageParameters);
                }
            };
            reset = reset.setDefaultFormProcessing(false);
            form.add(reset);
        } else {
            WebMarkupContainer itemtitle = new WebMarkupContainer("itemtitle",null);
            WebMarkupContainer emptyrow = new WebMarkupContainer("studentRow",null);
            emptyrow.add(new WebMarkupContainer("studentName",null));
            emptyrow.add(new WebMarkupContainer("studentGrade",null));
            emptyrow.add(new WebMarkupContainer("studentComment",null));
            emptyrow.setVisible(false);
            form.add(emptyrow);
            form.add(itemtitle);
            form.add(new WebMarkupContainer("itemdetails",null));
            form.add(new Label("summarycount",""));
            form.add(new WebMarkupContainer("submit",null).setVisible(false));
            form.add(new WebMarkupContainer("reset",null).setVisible(false));
            tableVisibility.setVisible(false);
        }
        form.add(tableVisibility);

        form.add(new Button("cancel"){
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(){
                setResponsePage(GradebookPage.class, null);
            }
        }.setDefaultFormProcessing(false).setVisible(this.assignmentNow != null));

        form.add(this.bulkGrade = new GbModalWindow("bulkGradeModal"));
        bulkGrade.setTitle(getString("quickentry.replacegradelabel"));
        bulkGrade.setInitialHeight(240);

        form.add(new AjaxLink<Void>("showBulkGrade") {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                bulkGrade.setComponentToReturnFocusTo(this);
                bulkGrade.show(target);
            }
            @Override
            public boolean isVisible(){
                return assignmentNow != null && !assignmentNow.getExternallyMaintained();
            }
        });
        form.add(this.bulkComment = new GbModalWindow("bulkCommentModal"));
        bulkComment.setTitle(getString("quickentry.comment.caption"));
        bulkComment.setInitialHeight(300);

        form.add(new AjaxLink<Void>("showBulkComment") {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                bulkComment.setComponentToReturnFocusTo(this);
                bulkComment.show(target);
            }
            @Override
            public boolean isVisible(){
                return assignmentNow != null;
            }
        });
        if(assignmentNow != null){
            bulkGrade.setContent(new BulkGradePanel(bulkGrade.getContentId(),false,assignmentNow.getPoints(), gradeType).add(new AttributeModifier("class","quickEntryBulk")));
            bulkComment.setContent(new BulkGradePanel(bulkComment.getContentId(),true,assignmentNow.getPoints(), gradeType).add(new AttributeModifier("class","quickEntryBulk")));
        }
        form.add(bulkGrade);
        form.add(bulkComment);
        form.add(itempicker);
        form.add(groupFilter);
        this.add(form);
    }

    @Override
    public void renderHead(final IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forUrl("/gradebookng-tool/scripts/gradebook-quick-entry.js"));
    }

    @Setter
    @Getter
    private static class QuickEntryPageModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long itemIdNow;
        private ArrayList<QuickEntryRowModel> itemgrades;
    }

    @Setter
    @Getter
    private static class QuickEntryRowModel implements Serializable {
        private static final long serialVersionUID = 1L;
        private String studentid;
        private String name;
        private String grade;
        private String comment;
        private String originalComment;
        private boolean excused = false;
        private boolean locked;
        private double maxGrade;
        private boolean hasError;

        public boolean commentChanged() {
            return !StringUtils.equals(comment, originalComment);
        }
    }
}
