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
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
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
import org.sakaiproject.grading.api.GradingConstants;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.model.GbGroup;
import org.sakaiproject.gradebookng.business.model.GbUser;
import org.sakaiproject.gradebookng.tool.panels.BulkGradePanel;
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.grading.api.Assignment;
import org.sakaiproject.grading.api.SortType;

import lombok.Getter;
import lombok.Setter;

public class QuickEntryPage extends BasePage {
    private static final long serialVersionUID = 1L;

    private Assignment assignmentNow;
    private GbGroup groupNow;
    private String assignmentIdNow;
    @Getter
    private ModalWindow bulkGrade;
    @Getter
    private ModalWindow bulkComment;
    private boolean noErrors = true;

    public QuickEntryPage() {
        disableLink(this.quickEntryPageLink);
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        Integer gradeType = this.businessService.getGradebookSettings().getGradeType();
        SortType sortBy = SortType.SORT_BY_NAME;
        final List<Assignment> assignments = this.businessService.getGradebookAssignments(sortBy);
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
        itempicker.add(new AjaxFormComponentUpdatingBehavior("onchange") {  // add the onchange to the chooser
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
        final List<GbGroup> groups = this.businessService.getSiteSectionsAndGroups();
        final DropDownChoice<GbGroup> groupFilter = new DropDownChoice<GbGroup>("groupicker", new Model<GbGroup>(), groups, new ChoiceRenderer<GbGroup>() {
            private static final long serialVersionUID = 1L;
            @Override
            public Object getDisplayValue(final GbGroup g) {
                return g.getTitle();
            }
            @Override
            public String getIdValue(final GbGroup g, final int index) {
                return g.getId();
            }
        });
        groupFilter.add(new AjaxFormComponentUpdatingBehavior("onchange") {
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
        groupFilter.setVisible(groups.size() > 0 && !params.get("selected").isNull());  // if only one item or no assignment, hide the dropdown
        groupFilter.setNullValid(true);
        final QuickEntryPageModel pageModel = new QuickEntryPageModel();
        pageModel.setItemgrades(new ArrayList<>());
        final Form form = new Form("form", Model.of(pageModel)) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(){
                noErrors = true;    //reset on every submission; we should be assessing this from scratch every time
                QuickEntryPageModel dataNow = (QuickEntryPageModel) this.getModelObject();
                ArrayList<QuickEntryRowModel> allgrades = dataNow.getItemgrades();
                Long itemId = dataNow.getItemIdNow();
                for(QuickEntryRowModel row: allgrades){ //first loop vor validation only
                    try {
                        Double gradeValidator = Double.valueOf(row.getGrade());
                        if(gradeValidator<0){
                            getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                            row.setHasError(true);
                            noErrors = false;
                            continue;
                        } else {
                            row.setHasError(false);
                        }
                    } catch (NumberFormatException e){
                        getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                        row.setHasError(true);
                        noErrors = false;
                        continue;
                    } catch (NullPointerException n){
                        row.setGrade("");   //NPE is actually ok, we just need to turn it into a blank string.
                    }
                }
                if(noErrors){   //if still no errors, second loop to start saving things
                    for(QuickEntryRowModel row: allgrades){
                        String oldGrade = businessService.getGradeForStudentForItem(row.getStudentid(),itemId).getGrade();
                        GradeSaveResponse succeeded = businessService.saveGrade(itemId,row.getStudentid(),oldGrade,row.getGrade(),row.getComment());
                        if(succeeded == GradeSaveResponse.ERROR || succeeded == GradeSaveResponse.CONCURRENT_EDIT){
                            getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                            row.setHasError(true);
                            noErrors = false;
                            break;
                        } else {
                            row.setHasError(false);
                        }
                        if(row.commentChanged()){   //in case a changed comment is with an unchanged/blank grade
                            boolean commentSucceeded = businessService.updateAssignmentGradeComment(itemId,row.getStudentid(),row.getComment());
                            if(!commentSucceeded){
                                getSession().error(MessageFormat.format(getString("quickentry.error"),row.getName()));
                                row.setHasError(true);
                                noErrors = false;
                                break;
                            } else {
                                row.setHasError(false);
                            }
                        }
                        GradeSaveResponse succeededExcuse = businessService.saveExcuse(itemId,row.getStudentid(),row.isExcused());
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
        form.add(new AttributeModifier("class","quickEntryForm"));
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
            String itemdetails = " - " + (Objects.equals(GradingConstants.GRADE_TYPE_PERCENTAGE, gradeType) ? getString("quickentry.percentages") : getString("quickentry.points")) + ": " + assignmentNow.getPoints().toString();
            if(assignmentNow.getExternallyMaintained()){
                itemdetails = itemdetails + " - " + MessageFormat.format(getString("quickentry.externally"),assignmentNow.getExternalAppName());
            }
            form.add(new Label("itemdetails", itemdetails));
            final List<String> gradableUsers = this.businessService.getGradeableUsers();
            List<QuickEntryRowModel> rows = new ArrayList<>();
            Map<String, List<String>> groupContainer = this.businessService.getGroupMemberships();
            int totalstudents = 0;
            int studentsnow = 0;
            for(String uid: gradableUsers){
                QuickEntryRowModel rowNow = new QuickEntryRowModel();
                totalstudents++;
                if(!params.get("groupNow").isNull() && !groupContainer.get("/site/" + this.businessService.getCurrentSiteId() + "/group/"+params.get("groupNow").toString()).contains(uid)){
                    continue;
                }
                studentsnow++;
                GbUser userNow = this.businessService.getUser(uid);
                rowNow.setName(userNow.getLastName() + ", " + userNow.getFirstName() + " (" + userNow.getDisplayId() + ')');
                String commentNow = this.businessService.getAssignmentGradeComment(this.assignmentNow.getId(),uid);
                if(commentNow != null){
                    rowNow.setComment(commentNow);
                    rowNow.setOriginalComment(commentNow);
                } else {
                    rowNow.setComment("");
                    // Wicket inputs have empty strings converted into nulls. but since there's no component that actually backs
                    // this field, we need to set it to null
                    rowNow.setOriginalComment(null);
                }
                String gradeNow = this.businessService.getGradeForStudentForItem(uid,this.assignmentNow.getId()).getGrade();
                if(StringUtils.isNotBlank(gradeNow)){
                    rowNow.setGrade(gradeNow);
                } else {
                    rowNow.setGrade(null);
                }
                rowNow.setExcused(this.businessService.getAssignmentExcuse(this.assignmentNow.getId(),uid) != "0");
                rowNow.setLocked(this.assignmentNow.getExternallyMaintained());
                rowNow.setMaxGrade(this.assignmentNow.getPoints());
                rowNow.setStudentid(uid);
                rows.add(rowNow);
                ((QuickEntryPageModel)form.getModelObject()).getItemgrades().add(rowNow);
            }
            rows.sort(new Comparator<QuickEntryRowModel>() {
                @Override
                public int compare(QuickEntryRowModel quickEntryRowModel, QuickEntryRowModel t1) {
                    return quickEntryRowModel.getName().compareTo(t1.getName());
                }
            });
            form.add(new Label("summarycount",MessageFormat.format(getString("quickentry.count"),studentsnow,totalstudents)).add(new AttributeModifier("class","summarycount")));
            ListView<QuickEntryRowModel> userFields = new ListView<QuickEntryRowModel>("studentRow",rows){
                @Override
                protected void populateItem(final ListItem<QuickEntryRowModel> item) {
                    item.add(new Label("studentName",item.getModelObject().getName()));
                    TextField gradeNow = new TextField<String>("studentGrade", new PropertyModel<String>(item.getModelObject(),"grade"));
                    gradeNow.add(new AttributeModifier("onchange","enableUpdate()"));
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
                    item.add(new TextArea<String>("studentComment",new PropertyModel<String>(item.getModelObject(),"comment")).add(new AttributeModifier("class","quickEntryComment")).add(new AttributeModifier("onchange","enableUpdate()")));
                    AjaxCheckBox excused = new AjaxCheckBox("studentExcuse",new PropertyModel<Boolean>(item.getModelObject(), "excused")){
                        @Override
                        public void onUpdate(AjaxRequestTarget target){}    //necessary for compliance but not actual functionality.
                    };
                    excused.add(new AttributeModifier("onchange","enableUpdate()"));
                    item.add(excused);
                }
            };
            tableVisibility.add(userFields);
            final SubmitLink submit = new SubmitLink("submit");
            submit.add(new AttributeModifier("id","quickentrySubmit")).add(new AttributeModifier("disabled",""));
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
            reset.add(new AttributeModifier("id","quickentryReset"));
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
        form.add(this.bulkGrade = new ModalWindow("bulkGradeModal"));
        bulkGrade.setTitle(getString("quickentry.replacegradelabel"));
        bulkGrade.setInitialHeight(240);
        form.add(new AjaxLink<Void>("showBulkGrade") {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
                bulkGrade.show(target);
            }
            @Override
            public boolean isVisible(){
                return assignmentNow != null && !assignmentNow.getExternallyMaintained();
            }
        });
        form.add(this.bulkComment = new ModalWindow("bulkCommentModal"));
        bulkComment.setTitle(getString("quickentry.comment.caption"));
        bulkComment.setInitialHeight(300);
        form.add(new AjaxLink<Void>("showBulkComment") {
            @Override
            public void onClick(AjaxRequestTarget target)
            {
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

    private class QuickEntryPageModel implements Serializable {
        private static final long serialVersionUID = 1L;
        @Getter @Setter private Long itemIdNow;
        @Getter @Setter private ArrayList<QuickEntryRowModel> itemgrades;
    }

    private class QuickEntryRowModel implements Serializable {
        private static final long serialVersionUID = 1L;
        @Getter @Setter private String studentid;
        @Getter @Setter private String name;
        @Getter @Setter private String grade;
        @Getter @Setter private String comment;
        @Getter @Setter private String originalComment;
        @Getter @Setter private boolean excused = false;
        @Getter @Setter private boolean locked;
        @Getter @Setter private double maxGrade;
        @Getter @Setter private boolean hasError;

        public boolean commentChanged() {
            return !StringUtils.equals(comment, originalComment);
        }
    }
}
