package org.sakaiproject.gradebookng.tool.panels;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.service.gradebook.shared.Assignment;

/**
 * 
 * Cell panel for the students average score in a category
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CategoryColumnCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name="org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;


	IModel<Map<String,Object>> model;

	public CategoryColumnCellPanel(String id, IModel<Map<String,Object>> model) {
		super(id, model);
		this.model = model;
	}
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		//unpack model
		Map<String,Object> modelData = (Map<String,Object>) this.model.getObject();
		
		Double score = (Double) modelData.get("score");		
		final String studentUuid = (String) modelData.get("studentUuid");
		final Long categoryId = (Long) modelData.get("categoryId");
						
		//score label
		Label scoreLabel = new Label("score", Model.of(getCategoryScore(score))) {
			@Override
			public void onEvent(IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof ScoreChangedEvent) {
					ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();
					if (studentUuid.equals(scoreChangedEvent.getStudentUuid()) && 
							categoryId.equals(scoreChangedEvent.getCategoryId())) {

						Map<Assignment, GbGradeInfo> grades = businessService.getGradesForStudent(studentUuid);
						Double categoryAverage = businessService.getCategoryScoreForStudent(categoryId, studentUuid, grades);

						String newCategoryAverage = (categoryAverage == null) ? getString("label.nocategoryscore") : FormatHelper.formatDoubleAsPercentage(categoryAverage);
						((Model<String>) getDefaultModel()).setObject(newCategoryAverage);
						scoreChangedEvent.getTarget().add(this);
					}
				}
			}
		};
		scoreLabel.setOutputMarkupId(true);
		add(scoreLabel);
		
		//accessibility
		getParent().add(new AttributeModifier("scope", "row"));
		getParent().add(new AttributeModifier("role", "rowheader"));

	}
	
	
	/**
	 * Helper to format a category score
	 * 
	 * The value is a double (ie 12.34) that needs to be formatted as a percentage with two decimal places precision.
	 * If null, it should return 'N/A' or equivalent translated string.
	 * 
	 * @param score
	 * @return 12.34% type string or N/A if null
	 */
	private String getCategoryScore(Double score) {
		
		if(score == null){
			return getString("label.nocategoryscore");
		}
		
		return FormatHelper.formatDoubleAsPercentage(score);
	}
}
