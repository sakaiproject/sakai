package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.validator.routines.DoubleValidator;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
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
		final Double assignmentPoints = (Double) modelData.get("assignmentPoints");
		final String studentUuid = (String) modelData.get("studentUuid");
		final Boolean isExternal = (Boolean) modelData.get("isExternal");
		final GradeInfo gradeInfo = (GradeInfo) modelData.get("gradeInfo");
		
		//note, gradeInfo may be null
		String rawGrade;
		if(gradeInfo != null) {
			rawGrade = gradeInfo.getGrade();
		} else {
			rawGrade = "";
		}
		
		//get grade
		final String formattedGrade = this.formatGrade(rawGrade);
				
		//if assignment is external, normal label
		if(BooleanUtils.isTrue(isExternal)){
			add(new Label("grade", Model.of(formattedGrade)));
			getParent().add(new AttributeModifier("class", "gb-external-item-cell"));
		} else {
			AjaxEditableLabel<String> gradeCell = new AjaxEditableLabel<String>("grade", Model.of(formattedGrade)) {
				
				private static final long serialVersionUID = 1L;
				
				private String originalGrade = null;
								
				@Override
				protected String defaultNullLabel() {
					return "&nbsp;";
				}
				
				@Override
				//TODO - Is setting a string here overkill since this component is initialised for EVERY cell?
				protected void onInitialize() {
					//set original grade, once only
					super.onInitialize();
					this.originalGrade = this.getLabel().getDefaultModelObjectAsString();
					this.addSpecialAttributes();
					
					//check if grade is over limit and mark the cell with the warning class
					if(NumberUtils.toDouble(this.originalGrade) > assignmentPoints) {
						markWarning(this);
					}
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
										
					//perform validation here so we can bypass the backend
					DoubleValidator validator = new DoubleValidator();
					
					if(!validator.isValid(newGrade)) {
						markWarning(this);
						//TODO add the message
					} else {
						
						//for concurrency, get the original grade we have in the UI and pass it into the service as a check
						GradeSaveResponse result = businessService.saveGrade(assignmentId, studentUuid, this.originalGrade, newGrade);
						
						//TODO here, add the message
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
					}
								
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

					AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return "GradebookWicketEventProxy.updateLabel.handleBeforeSend('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, settings);";
						}

						@Override
						public CharSequence getSuccessHandler(Component component) {
							return "GradebookWicketEventProxy.updateLabel.handleSuccess('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, data, textStatus);";
						}

						@Override
						public CharSequence getFailureHandler(Component component) {
							return "GradebookWicketEventProxy.updateLabel.handleFailure('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, errorMessage, textStatus);";
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							return "GradebookWicketEventProxy.updateLabel.handleComplete('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, textStatus);";
						}
					};
					attributes.getAjaxCallListeners().add(myAjaxCallListener);
				}
				
				@Override
				protected void updateEditorAjaxAttributes(AjaxRequestAttributes attributes) {
					//when switching from label to editor
					Map<String,Object> extraParameters = attributes.getExtraParameters();
					extraParameters.put("assignmentId", assignmentId);
					extraParameters.put("studentUuid", studentUuid);

					AjaxCallListener myAjaxCallListener = new AjaxCallListener() {
						@Override
						public CharSequence getBeforeSendHandler(Component component) {
							return "GradebookWicketEventProxy.updateEditor.handleBeforeSend('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, settings);";
						}

						@Override
						public CharSequence getSuccessHandler(Component component) {
							return "GradebookWicketEventProxy.updateEditor.handleSuccess('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, data, textStatus);";
						}

						@Override
						public CharSequence getFailureHandler(Component component) {
							return "GradebookWicketEventProxy.updateEditor.handleFailure('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, errorMessage, textStatus);";
						}
						@Override
						public CharSequence getCompleteHandler(Component component) {
							return "GradebookWicketEventProxy.updateEditor.handleComplete('" + getParentCellFor(component.getParent()).getMarkupId() + "', attrs, jqXHR, textStatus);";
						}
					};
					attributes.getAjaxCallListeners().add(myAjaxCallListener);
				}
				
				/* for setting the original grade at construction time */
				public void setOriginalGrade(String grade) {
					this.originalGrade = grade;
				}

				private void addSpecialAttributes() {
					Component parentCell = getParentCellFor(this);
					parentCell.add(new AttributeModifier("data-assignmentid", assignmentId));
					parentCell.add(new AttributeModifier("data-studentuuid", studentUuid));
					parentCell.add(new AttributeModifier("class", "gb-grade-item-cell"));
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
		getParentCellFor(gradeCell).add(AttributeModifier.replace("class", "gb-grade-item-cell gradeSaveSuccess"));
		//TODO attach a timeout here
	}
	
	private void markError(Component gradeCell) {
		getParentCellFor(gradeCell).add(AttributeModifier.replace("class", "gb-grade-item-cell gradeSaveError"));
	}
	
	private void markWarning(Component gradeCell) {
		getParentCellFor(gradeCell).add(AttributeModifier.replace("class", "gb-grade-item-cell gradeSaveWarning"));
	}

	private Component getParentCellFor(Component gradeCell) {
		return gradeCell.getParent().getParent();
	}
	
}
