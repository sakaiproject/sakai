package org.sakaiproject.gradebookng.tool.panels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.service.gradebook.shared.Assignment;

public class GradeStatisticsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public GradeStatisticsPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Long assignmentId = ((Model<Long>) getDefaultModel()).getObject();

		final Assignment assignment = this.businessService.getAssignment(assignmentId.longValue());

		add(new Label("title", new StringResourceModel("label.statistics.title",
				null, new Object[] { assignment.getName() }).getString()));

		final List<GbStudentGradeInfo> gradeInfo = this.businessService.buildGradeMatrix(Arrays.asList(assignment));

		final List<Double> allGrades = new ArrayList<>();

		for (int i = 0; i < gradeInfo.size(); i++) {
			final GbStudentGradeInfo studentGradeInfo = gradeInfo.get(i);

			final Map<Long, GbGradeInfo> studentGrades = studentGradeInfo.getGrades();
			final GbGradeInfo grade = studentGrades.get(assignmentId);

			if (grade == null || grade.getGrade() == null) {
				// this is not the grade you are looking for
			} else {
				allGrades.add(Double.valueOf(grade.getGrade()));
			}
		}

		add(new Label("graded", new StringResourceModel("label.statistics.gradedoutof",
				null, new Object[] {
						String.valueOf(allGrades.size()),
						String.valueOf(gradeInfo.size()) }).getString()));
		add(new Label("outof", String.valueOf(assignment.getPoints())));

		if (allGrades.size() > 0) {
			Collections.sort(allGrades);
			add(new Label("average", String.valueOf(calculateAverage(allGrades))));
			add(new Label("median", String.valueOf(calculateMedian(allGrades))));
			add(new Label("lowest", String.valueOf(Collections.min(allGrades))));
			add(new Label("highest", String.valueOf(Collections.max(allGrades))));
			add(new Label("variance", String.valueOf(calculateVariance(allGrades))));
			add(new Label("deviation", String.valueOf(calculateStandardDeviation(allGrades))));
		} else {
			add(new Label("average", "-"));
			add(new Label("median", "-"));
			add(new Label("lowest", "-"));
			add(new Label("highest", "-"));
			add(new Label("variance", "-"));
			add(new Label("deviation", "-"));
		}

		add(new AjaxLink<Void>("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				GradeStatisticsPanel.this.window.close(target);
			}
		});
	}

	private double calculateAverage(final List<Double> allGrades) {
		double sum = 0;
		for (int i = 0; i < allGrades.size(); i++) {
			sum += allGrades.get(i);
		}
		return sum / allGrades.size();
	}

	private double calculateMedian(final List<Double> allGrades) {
		final int middle = allGrades.size() / 2;
		if (allGrades.size() % 2 == 1) {
			return allGrades.get(middle);
		} else {
			return (allGrades.get(middle - 1) + allGrades.get(middle)) / 2.0;
		}
	}

	private double calculateVariance(final List<Double> allGrades) {
		final double mean = calculateAverage(allGrades);
		double sum = 0;

		for (int i = 0; i < allGrades.size(); i++) {
			final double grade = allGrades.get(i);
			sum += (mean - grade) * (mean - grade);
		}

		return sum / allGrades.size();
	}

	private double calculateStandardDeviation(final List<Double> allGrades) {
		return Math.sqrt(calculateVariance(allGrades));
	}
}
