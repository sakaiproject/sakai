package org.sakaiproject.gradebookng.tool.panels;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.Exception;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.SortDirection;
import org.sakaiproject.gradebookng.business.model.GbAssignmentGradeSortOrder;
import org.sakaiproject.gradebookng.tool.model.GradebookUiSettings;
import org.sakaiproject.gradebookng.tool.pages.EditGradebookItemPage;
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
		
		Link<String> title = new Link<String>("title", Model.of(assignment.getName())) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick() {
				
				//toggle the sort direction on each click
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				GradebookUiSettings settings = gradebookPage.getUiSettings();
				
				//if null, set a default sort, otherwise toggle, save, refresh.
				if(settings == null) {
					settings = new GradebookUiSettings();
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
		
		//set the class based on the sortOrder. May not be set for this assignment
		GradebookPage gradebookPage = (GradebookPage) this.getPage();
		GradebookUiSettings settings = gradebookPage.getUiSettings();
		if(settings != null && settings.getAssignmentSortOrder() != null) {
			title.add(new AttributeModifier("class", "gb-sort-" + settings.getAssignmentSortOrder().getDirection().toString().toLowerCase()));
		}
		
		add(title);
		
		
		add(new Label("totalPoints", Model.of(assignment.getPoints())));
		add(new Label("dueDate", Model.of(getDueDate(assignment.getDueDate()))));

		WebMarkupContainer externalAppFlag = new WebMarkupContainer("externalAppFlag");
		if (assignment.getExternalAppName() == null) {
			externalAppFlag.setVisible(false);
		} else {
			externalAppFlag.setVisible(true);
			externalAppFlag.add(new AttributeModifier("title", getString("label.gradeitem.externalAppPrefix") + " " + assignment.getExternalAppName()));
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

		add(new WebMarkupContainer("extraCreditFlag").setVisible(assignment.isExtraCredit()));
		add(new WebMarkupContainer("isCountedFlag").setVisible(assignment.isCounted()));
		add(new WebMarkupContainer("notCountedFlag").setVisible(!assignment.isCounted()));
		add(new WebMarkupContainer("isReleasedFlag").setVisible(assignment.isReleased()));
		add(new WebMarkupContainer("notReleasedFlag").setVisible(!assignment.isReleased()));

		add(new AttributeModifier("data-assignmentId", assignment.getId()));
		add(new AttributeModifier("data-category", assignment.getCategoryName()));
		if (assignment.getWeight() != null) {
			add(new AttributeModifier("data-category-weight", String.format("%s%%", Math.round(assignment.getWeight() * 100))));
		}
		add(new AttributeModifier("data-category-extra-credit", assignment.isCategoryExtraCredit()));

		//menu
		add(new Link<Long>("editAssignmentDetails", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(new EditGradebookItemPage(this.getModel()));
			}
			@Override
			public boolean isVisible() {
				if(assignment.isExternallyMaintained()) {
					return false;
				}
				return true;
			}
		});
		
		add(new Link<Long>("viewAssignmentGradeStatistics", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				setResponsePage(new GradebookPage());
			}
		});
		
		add(new Link<Long>("moveAssignmentLeft", Model.of(assignment.getId())){
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
		
				
		add(new Link<Long>("moveAssignmentRight", Model.of(assignment.getId())){
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
		
		
		add(new AjaxLink<Long>("hideAssignment", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				long assignmentId = this.getModelObject();
				target.appendJavaScript("sakai.gradebookng.spreadsheet.hideGradeItemAndSyncToolbar('" + assignmentId + "');");
			}	
		});
		
		
		add(new AjaxLink<Long>("setUngraded", Model.of(assignment.getId())){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				
				GradebookPage gradebookPage = (GradebookPage) this.getPage();
				final ModalWindow window = gradebookPage.getUpdateUngradedItemsWindow();
				
				window.setContent(new UpdateUngradedItemsPanel(window.getContentId(), this.getModel(), window));
				window.showUnloadConfirmation(false);
				window.show(target);
			}	
		});
		
		
		

	}
	
	
	private String getDueDate(Date assignmentDueDate) {
		//TODO locale formatting via ResourceLoader
		
		if(assignmentDueDate == null) {
			return getString("label.noduedate");
		}
		
		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
    	return df.format(assignmentDueDate);
	}

}
