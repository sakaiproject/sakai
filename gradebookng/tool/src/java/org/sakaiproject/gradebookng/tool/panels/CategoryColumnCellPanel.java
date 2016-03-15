package org.sakaiproject.gradebookng.tool.panels;

import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.model.ScoreChangedEvent;

/**
 *
 * Cell panel for the students average score in a category
 *
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class CategoryColumnCellPanel extends Panel {

	private static final long serialVersionUID = 1L;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	IModel<Map<String, Object>> model;

	public CategoryColumnCellPanel(final String id, final IModel<Map<String, Object>> model) {
		super(id, model);
		this.model = model;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		// unpack model
		final Map<String, Object> modelData = this.model.getObject();

		final Double score = (Double) modelData.get("score");
		final String studentUuid = (String) modelData.get("studentUuid");
		final Long categoryId = (Long) modelData.get("categoryId");

		// score label
		final Label scoreLabel = new Label("score", Model.of(getCategoryScore(score))) {
			@Override
			public void onEvent(final IEvent<?> event) {
				super.onEvent(event);
				if (event.getPayload() instanceof ScoreChangedEvent) {
					final ScoreChangedEvent scoreChangedEvent = (ScoreChangedEvent) event.getPayload();
					if (studentUuid.equals(scoreChangedEvent.getStudentUuid()) &&
							categoryId.equals(scoreChangedEvent.getCategoryId())) {

						final Double categoryAverage = CategoryColumnCellPanel.this.businessService.getCategoryScoreForStudent(categoryId,
								studentUuid);

						final String newCategoryAverage = (categoryAverage == null) ? getString("label.nocategoryscore")
								: FormatHelper.formatDoubleAsPercentage(categoryAverage);
						((Model<String>) getDefaultModel()).setObject(newCategoryAverage);

						getParent().add(new AttributeAppender("class", "gb-score-dynamically-updated"));

						scoreChangedEvent.getTarget().add(this);
						scoreChangedEvent.getTarget().appendJavaScript(
								String.format("$('#%s').closest('td').addClass('gb-score-dynamically-updated');",
										this.getMarkupId()));
					}
				}
			}
		};
		scoreLabel.setOutputMarkupId(true);
		add(scoreLabel);
	}

	/**
	 * Helper to format a category score
	 *
	 * The value is a double (ie 12.34) that needs to be formatted as a percentage with two decimal places precision. If null, it should
	 * return 'N/A' or equivalent translated string.
	 *
	 * @param score
	 * @return 12.34% type string or N/A if null
	 */
	private String getCategoryScore(final Double score) {

		if (score == null) {
			return getString("label.nocategoryscore");
		}

		return FormatHelper.formatDoubleAsPercentage(score);
	}
}
