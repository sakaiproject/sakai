package org.sakaiproject.gradebookng.tool.panels;

import java.lang.Exception;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GbRole;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.GradebookPage;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Header panel for each assignment column in the UI
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class AssignmentColumnHeaderPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private IModel<Assignment> modelData;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	private GradebookNgBusinessService businessService;

	public AssignmentColumnHeaderPanel(String id, IModel<Assignment> modelData) {
		super(id);
		this.modelData = modelData;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		final Assignment assignment = this.modelData.getObject();
		
		//get user's role
		final GbRole role = this.businessService.getUserRole();
		
		Link<String> title = new Link<String>("title", Model.of(assignment.getName())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
								
				//toggle the sort direction on each click
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();
				
				//if null, set a default sort, otherwise toggle, save, refresh.
				if(settings.getAssignmentSortOrder() == null || !assignment.getId().equals(settings.getAssignmentSortOrder().getAssignmentId())) {
					settings.setAssignmentSortOrder(new GbAssignmentGradeSortOrder(assignment.getId(), SortDirection.ASCENDING));
				} else {
					GbAssignmentGradeSortOrder sortOrder = settings.getAssignmentSortOrder();
					SortDirection direction = sortOrder.getDirection();
					direction = direction.toggle();
					sortOrder.setDirection(direction);
					settings.setAssignmentSortOrder(sortOrder);
				}
				
				//save settings
				gradebookPage.setUiSettings(settings);
				
				//refresh
				setResponsePage(new GradebookPage());
			}
			
		};
		title.add(new AttributeModifier("title", assignment.getName()));
		title.add(new Label("label", assignment.getName()));
		
		//set the class based on the sortOrder. May not be set for this assignment so match it
		GradebookPage gradebookPage = (GradebookPage) this.getPage();
		GradebookUiSettings settings = gradebookPage.getUiSettings();
		if(settings != null && settings.getAssignmentSortOrder() != null && settings.getAssignmentSortOrder().getAssignmentId() == assignment.getId()) {
			title.add(new AttributeModifier("class", "gb-sort-" + settings.getAssignmentSortOrder().getDirection().toString().toLowerCase()));
		}
		
		add(title);
		
		add(new Label("totalPoints", Model.of(assignment.getPoints())));
		add(new Label("dueDate", Model.of(FormatHelper.formatDate(assignment.getDueDate(), getString("label.noduedate")))));

		WebMarkupContainer externalAppFlag = gradebookPage.buildFlagWithPopover("externalAppFlag", "");
		if (assignment.getExternalAppName() == null) {
			externalAppFlag.setVisible(false);
		} else {
			externalAppFlag.setVisible(true);
			externalAppFlag.add(new AttributeModifier("data-content", getString("label.gradeitem.externalAppPrefix") + " " + assignment.getExternalAppName()));
			String iconClass = "icon-sakai";
			if ("Assignments".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-assignment-grades";
			} else if ("Tests & Quizzes".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-samigo";
			} else if ("Lesson Builder".equals(assignment.getExternalAppName())) {
				iconClass = "icon-sakai-lessonbuildertool";
			}
			externalAppFlag.add(new AttributeModifier("class", "gb-external-app-flag Mrphs-toolsNav__menuitem--icon icon-active " + iconClass));
		}
		add(externalAppFlag);
		
		add(gradebookPage.buildFlagWithPopover("extraCreditFlag", getString("label.gradeitem.extracredit")).setVisible(assignment.isExtraCredit()));
		add(gradebookPage.buildFlagWithPopover("isCountedFlag", getString("label.gradeitem.counted")).setVisible(assignment.isCounted()));
		add(gradebookPage.buildFlagWithPopover("notCountedFlag", getString("label.gradeitem.notcounted")).setVisible(!assignment.isCounted()));
		add(gradebookPage.buildFlagWithPopover("isReleasedFlag", getString("label.gradeitem.released")).setVisible(assignment.isReleased()));
		add(gradebookPage.buildFlagWithPopover("notReleasedFlag", getString("label.gradeitem.notreleased")).setVisible(!assignment.isReleased()));

		add(new AttributeModifier("data-assignmentId", assignment.getId()));
		add(new AttributeModifier("data-category", assignment.getCategoryName()));
		if (assignment.getWeight() != null) {
			add(new AttributeModifier("data-category-weight", String.format("%s%%", Math.round(assignment.getWeight() * 100))));
		}
		add(new AttributeModifier("data-category-extra-credit", assignment.isCategoryExtraCredit()));

		//menu
		WebMarkupContainer menu = new WebMarkupContainer("menu") {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isVisible() {
				if(role != GbRole.INSTRUCTOR) {
					return false;
				}
				return true;
			}
		};
		
		menu.add(new AjaxLink<Long>("editAssignmentDetails", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			
			@Override
			public void onClick(AjaxRequestTarget target) {
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				ModalWindow window = gradebookPage.getAddOrEditGradeItemWindow();
				window.setContent(new AddOrEditGradeItemPanel(window.getContentId(), window, this.getModel()));
				window.showUnloadConfirmation(false);
				window.show(target);
			}
			
			@Override
			public boolean isVisible() {
				if(assignment.isExternallyMaintained()) {
					return false;
				}
				return true;
			}

		});
		
		menu.add(new Link<Long>("viewAssignmentGradeStatistics", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(new GradebookPage());
			}
		});
		
		menu.add(new Link<Long>("moveAssignmentLeft", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				//given the id, get the assignment, get the sort order, then update and refresh
				//note that we cannot use the passed in assignment sort order in here
				//as we may have had an async reorder on the front end but not had the model data updated, 
				//so we just make sure we get it fresh
				
				long assignmentId = this.getModelObject();

				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();

				if (settings == null) {
					settings = new GradebookUiSettings();
					gradebookPage.setUiSettings(settings);
				}

				if (settings.isCategoriesEnabled()) {
					try {
						int order = businessService.getCategorizedSortOrder(assignmentId);
						businessService.updateCategorizedAssignmentOrder(assignmentId, (order - 1));
					} catch (Exception e) {
						e.printStackTrace();
						error("error reordering within category");
					}
				} else {
					int order = businessService.getAssignmentSortOrder(assignmentId);
					businessService.updateAssignmentOrder(assignmentId, (order-1));
				}

				setResponsePage(new GradebookPage());
			}
		});
				
		menu.add(new Link<Long>("moveAssignmentRight", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				
				long assignmentId = this.getModelObject();

				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();

				if (settings == null) {
					settings = new GradebookUiSettings();
					gradebookPage.setUiSettings(settings);
				}

				if (settings.isCategoriesEnabled()) {
					try {
						int order = businessService.getCategorizedSortOrder(assignmentId);
						businessService.updateCategorizedAssignmentOrder(assignmentId, (order + 1));
					} catch (Exception e) {
						e.printStackTrace();
						error("error reordering within category");
					}
				} else {
					int order = businessService.getAssignmentSortOrder(assignmentId);
					businessService.updateAssignmentOrder(assignmentId, (order+1));
				}
				
				setResponsePage(new GradebookPage());
			}
		});
		
		
		menu.add(new AjaxLink<Long>("hideAssignment", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				long assignmentId = this.getModelObject();
				target.appendJavaScript("sakai.gradebookng.spreadsheet.hideGradeItemAndSyncToolbar('" + assignmentId + "');");
			}	
		});
		
		
		menu.add(new AjaxLink<Long>("setUngraded", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				final ModalWindow window = gradebookPage.getUpdateUngradedItemsWindow();
				final UpdateUngradedItemsPanel panel = new UpdateUngradedItemsPanel(window.getContentId(), this.getModel(), window);
				window.setContent(panel);
				window.showUnloadConfirmation(false);
				window.show(target);

				panel.setOutputMarkupId(true);
				target.appendJavaScript("new GradebookUpdateUngraded($(\"#"+panel.getMarkupId()+"\"));");
			}
			
			@Override
			public boolean isVisible() {
				if(assignment.isExternallyMaintained()) {
					return false;
				}
				return true;
			}
			
		});

		// delete item
		menu.add(new AjaxLink<Long>("deleteGradeItem", Model.of(assignment.getId())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				final ModalWindow window = gradebookPage.getDeleteItemWindow();
				final DeleteItemPanel panel = new DeleteItemPanel(window.getContentId(), this.getModel(), window);

				window.setContent(panel);
				window.showUnloadConfirmation(false);
				window.show(target);
			}
			
			@Override
			public boolean isVisible() {
				if(assignment.isExternallyMaintained()) {
					return false;
				}
				return true;
			}
		});
		
		add(menu);
		
	}
}
