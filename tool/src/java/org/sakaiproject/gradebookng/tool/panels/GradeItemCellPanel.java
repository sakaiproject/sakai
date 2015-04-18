package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.extensions.ajax.markup.html.AjaxEditableLabel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradeSaveResponse;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.tool.model.GradeInfo;

/**
 * The panel for the cell of a grade item
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class GradeItemCellPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;
		
	IModel<Map<String,Object>> model;

	public GradeItemCellPanel(String id, IModel<Map<String,Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.model.getObject();
		final Long assignmentId = (Long) modelData.get("assignmentId");
		final String studentUuid = (String) modelData.get("studentUuid");
		final Boolean isExternal = (Boolean) modelData.get("isExternal");
		final GradeInfo gradeInfo = (GradeInfo) modelData.get("gradeInfo");
		
		//get grade
		final String formattedGrade = this.formatGrade(gradeInfo.getGrade());
				
		//if assignment is external, normal label
		if(BooleanUtils.isTrue(isExternal)){
			add(new Label("grade", Model.of(formattedGrade)));
		} else {
			AjaxEditableLabel<String> gradeCell = new AjaxEditableLabel<String>("grade", Model.of(formattedGrade)) {
				
				private static final long serialVersionUID = 1L;
				
				private String originalGrade = null;
								
				@Override
				protected String defaultNullLabel() {
					return "-"; // they need something to click
				}
				
				@Override
				//TODO - Is setting a string here overkill since this component is initialised for EVERY cell?
				protected void onInitialize() {
					//set original grade, once only
					super.onInitialize();
					this.originalGrade = this.getLabel().getDefaultModelObjectAsString();
				}
				
				@Override
				protected void onModelChanging() {
					//capture the original grade before it changes
					this.originalGrade = this.getEditor().getValue();
				}
				
				@Override
				protected void onSubmit(final AjaxRequestTarget target) {
					super.onSubmit(target);
					
					String newGrade = this.getEditor().getValue();
					
					//for concurrency, get the original grade we have in the UI and pass it into the service as a check
										
					GradeSaveResponse result = businessService.saveGrade(assignmentId, studentUuid, this.originalGrade, newGrade);
										
					switch (result) {
						case OK: 
							markSuccessful(this);
						break;
						case ERROR: 
							markError(this);
							//TODO fix this message
							error("oh dear");
						break;
						case OVER_LIMIT:
							markWarning(this);
						break;
						case NO_CHANGE:
							//do nothing
						break;
						case CONCURRENT_EDIT:
							markError(this);
							//TODO fix this message
							error("concurrent edit, eep");
						break;
						default:
							throw new UnsupportedOperationException("The response for saving the grade is unknown.");
					}
					
					//todo handle more cases here, like concurrency etc
					
					//format the grade for subsequent display and update the model
					String formattedGrade = formatGrade(newGrade);
					
					this.getLabel().setDefaultModelObject(formattedGrade);
					
					//refresh the components we need
					target.addChildren(getPage(), FeedbackPanel.class);
					target.add(this.getParent().getParent());
					target.add(this);
				}
				
				@Override
				protected void updateLabelAjaxAttributes(AjaxRequestAttributes attributes) {
					//when switching from editor to label
					Map<String,Object> extraParameters = attributes.getExtraParameters();
					extraParameters.put("assignmentId", assignmentId);
					extraParameters.put("studentUuid", studentUuid);
				}
				
				@Override
				protected void updateEditorAjaxAttributes(AjaxRequestAttributes attributes) {
					//when switching from label to editor
					Map<String,Object> extraParameters = attributes.getExtraParameters();
					extraParameters.put("assignmentId", assignmentId);
					extraParameters.put("studentUuid", studentUuid);
				}
				
				/* for setting the original grade at construction time */
				public void setOriginalGrade(String grade) {
					this.originalGrade = grade;
				}
				
				
			};
			
			gradeCell.setType(String.class);
			
			gradeCell.setOutputMarkupId(true);
			
			add(gradeCell);
		}
									
		//menu
		

	}
	
	/**
	 * Format a grade to remove the .0 if present.
	 * @param grade
	 * @return
	 */
	private String formatGrade(String grade) {
		return StringUtils.removeEnd(grade, ".0");		
	}
	
	private void markSuccessful(Component gradeCell) {
		gradeCell.getParent().getParent().add(AttributeModifier.replace("class", "gb-grade-item-header gradeSaveSuccess"));
		//TODO attach a timeout here
	}
	
	private void markError(Component gradeCell) {
		gradeCell.getParent().getParent().add(AttributeModifier.replace("class", "gb-grade-item-header gradeSaveError"));
	}
	
	private void markWarning(Component gradeCell) {
		gradeCell.getParent().getParent().add(AttributeModifier.replace("class", "gb-grade-item-header gradeSaveWarning"));
	}
	

}
