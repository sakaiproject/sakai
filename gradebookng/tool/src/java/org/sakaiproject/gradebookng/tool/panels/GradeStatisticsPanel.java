package org.sakaiproject.gradebookng.tool.panels;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.NonCachingImage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.entity.StandardEntityCollection;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.sakaiproject.gradebookng.business.GbGradingType;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.service.gradebook.shared.Assignment;

public class GradeStatisticsPanel extends Panel {

	private static final long serialVersionUID = 1L;

	private final ModalWindow window;
	private final GbGradingType gradingType;

	@SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
	protected GradebookNgBusinessService businessService;

	public GradeStatisticsPanel(final String id, final IModel<Long> model, final ModalWindow window) {
		super(id, model);
		this.window = window;
		this.gradingType = GbGradingType.valueOf(this.businessService.getGradebook().getGrade_type());
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		final Long assignmentId = ((Model<Long>) getDefaultModel()).getObject();

		final Assignment assignment = this.businessService.getAssignment(assignmentId.longValue());

		GradeStatisticsPanel.this.window.setTitle(
				(new StringResourceModel("label.statistics.title", null,
						new Object[] { assignment.getName() }).getString()));

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

		Collections.sort(allGrades);

		final DefaultCategoryDataset data = new DefaultCategoryDataset();

		final SortedMap<String, Integer> counts = new TreeMap();
		Integer extraCredits = 0;

		// Start off with a 0-50% range
		counts.put(String.format("%d-%d", 0, 50), 0);

		final int range = 10;
		for (int start = 50; start < 100; start = start + range) {
			final String key = String.format("%d-%d", start, start + range);
			counts.put(key, 0);
		}

		for (final Double grade : allGrades) {
			if (isExtraCredit(grade, assignment)) {
				extraCredits = extraCredits + 1;
				continue;
			}

			final double percentage;
			if (GbGradingType.PERCENTAGE.equals(gradingType)) {
				percentage = grade;
			} else {
				percentage = grade / assignment.getPoints() * 100;
			}

			final int total = Double.valueOf(Math.ceil(percentage) / range).intValue();

			int start = total * range;

			if (start == 100) {
				start = start - range;
			}

			String key = String.format("%d-%d", start, start + range);

			if (start < 50) {
				key = String.format("%d-%d", 0, 50);
			}

			counts.put(key, counts.get(key) + 1);
		}

		for (final String label : counts.keySet()) {
			data.addValue(counts.get(label), "count", label);
		}
		if (extraCredits > 0) {
			data.addValue(extraCredits, "count",
					getString("label.statistics.chart.extracredit"));
		}

		final JFreeChart chart = ChartFactory.createBarChart(
				null, // the chart title
				getString("label.statistics.chart.xaxis"), // the label for the category axis
				getString("label.statistics.chart.yaxis"), // the label for the value axis
				data, // the dataset for the chart
				PlotOrientation.VERTICAL, // the plot orientation
				false, // show legend
				true, // show tooltips
				false); // show urls

		chart.setBorderVisible(false);

		chart.setAntiAlias(false);

		final CategoryPlot categoryPlot = chart.getCategoryPlot();
		final BarRenderer br = (BarRenderer) categoryPlot.getRenderer();

		br.setItemMargin(0);
		br.setMinimumBarLength(0.05);
		br.setMaximumBarWidth(0.1);
		br.setSeriesPaint(0, new Color(51, 122, 183));
		br.setBarPainter(new StandardBarPainter());
		br.setShadowPaint(new Color(220, 220, 220));
		BarRenderer.setDefaultShadowsVisible(true);

		br.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
				getString("label.statistics.chart.tooltip"), NumberFormat.getInstance()));

		categoryPlot.setRenderer(br);

		// show only integers in the count axis
		categoryPlot.getRangeAxis().setStandardTickUnits(new NumberTickUnitSource(true));
		categoryPlot.setBackgroundPaint(Color.white);

		add(new JFreeChartImageWithToolTip("chart", Model.of(chart), "tooltip", 540, 300));

		add(new Label("graded", String.valueOf(allGrades.size())));

		if (allGrades.size() > 0) {
			add(new Label("average", constructAverageLabel(allGrades, assignment)));
			add(new Label("median", constructMedianLabel(allGrades, assignment)));
			add(new Label("lowest", constructLowestLabel(allGrades, assignment)));
			add(new Label("highest", constructHighestLabel(allGrades, assignment)));
			add(new Label("deviation", constructStandardDeviationLabel(allGrades)));
		} else {
			add(new Label("average", "-"));
			add(new Label("median", "-"));
			add(new Label("lowest", "-"));
			add(new Label("highest", "-"));
			add(new Label("deviation", "-"));
		}

		add(new GbAjaxLink("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				GradeStatisticsPanel.this.window.close(target);
			}
		});
	}

	private String constructAverageLabel(final List<Double> allGrades, final Assignment assignment) {
		final double average = calculateAverage(allGrades);
		final String averageFormatted = FormatHelper.formatDoubleToDecimal(Double.valueOf(average));

		if (GbGradingType.PERCENTAGE.equals(gradingType)) {
			return (new StringResourceModel("label.percentage.valued",
				null,
				new Object[] { averageFormatted })).getString();
		}

		final Double total = assignment.getPoints();
		final String percentage = FormatHelper.formatDoubleAsPercentage(100 * (average / total.doubleValue()));

		return (new StringResourceModel("label.statistics.averagevalue",
				null,
				new Object[] { averageFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage })).getString();
	}

	private String constructMedianLabel(final List<Double> allGrades, final Assignment assignment) {
		final double median = calculateMedian(allGrades);
		final String medianFormatted = FormatHelper.formatDoubleToDecimal(Double.valueOf(median));

		if (GbGradingType.PERCENTAGE.equals(gradingType)) {
			return (new StringResourceModel("label.percentage.valued",
				null,
				new Object[] { medianFormatted })).getString();
		}

		final Double total = assignment.getPoints();
		final String percentage = FormatHelper.formatDoubleAsPercentage(100 * (median / total.doubleValue()));

		return (new StringResourceModel("label.statistics.medianvalue",
				null,
				new Object[] { medianFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage })).getString();
	}

	private String constructLowestLabel(final List<Double> allGrades, final Assignment assignment) {
		final double lowest = Collections.min(allGrades);
		final String lowestFormatted = FormatHelper.formatDoubleToDecimal(Double.valueOf(lowest));

		if (GbGradingType.PERCENTAGE.equals(gradingType)) {
			return (new StringResourceModel("label.percentage.valued",
				null,
				new Object[] { lowestFormatted })).getString();
		}

		final Double total = assignment.getPoints();
		final String percentage = FormatHelper.formatDoubleAsPercentage(100 * (lowest / total.doubleValue()));

		return (new StringResourceModel("label.statistics.lowestvalue",
				null,
				new Object[] { lowestFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage })).getString();
	}

	private String constructHighestLabel(final List<Double> allGrades, final Assignment assignment) {
		final double highest = Collections.max(allGrades);
		final String highestFormatted = FormatHelper.formatDoubleToDecimal(Double.valueOf(highest));

		if (GbGradingType.PERCENTAGE.equals(gradingType)) {
			return (new StringResourceModel("label.percentage.valued",
				null,
				new Object[] { highestFormatted })).getString();
		}

		final Double total = assignment.getPoints();
		final String percentage = FormatHelper.formatDoubleAsPercentage(100 * (highest / total.doubleValue()));

		return new StringResourceModel("label.statistics.highestvalue",
				null,
				new Object[] { highestFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage }).getString();
	}

	private String constructStandardDeviationLabel(final List<Double> allGrades) {
		final double deviation = calculateStandardDeviation(allGrades);

		return FormatHelper.formatDoubleToDecimal(Double.valueOf(deviation));
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

	private boolean isExtraCredit(Double grade, Assignment assignment) {
		return (GbGradingType.PERCENTAGE.equals(gradingType) && grade > 100) ||
			(GbGradingType.POINTS.equals(gradingType) && grade > assignment.getPoints());
	}
}

class JFreeChartImageWithToolTip extends NonCachingImage {
	private final String imageMapId;
	private final int width;
	private final int height;
	private final ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo(new StandardEntityCollection());

	public JFreeChartImageWithToolTip(final String id, final IModel<JFreeChart> model,
			final String imageMapId, final int width, final int height) {
		super(id, model);
		this.imageMapId = imageMapId;
		this.width = width;
		this.height = height;
	}

	@Override
	protected IResource getImageResource() {
		IResource imageResource = null;
		final JFreeChart chart = (JFreeChart) getDefaultModelObject();
		imageResource = new DynamicImageResource() {
			@Override
			protected byte[] getImageData(final Attributes attributes) {
				final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				try {
					if (chart != null) {
						JFreeChartImageWithToolTip.this.chartRenderingInfo.clear();
						ChartUtilities.writeChartAsPNG(stream, chart, JFreeChartImageWithToolTip.this.width,
								JFreeChartImageWithToolTip.this.height, JFreeChartImageWithToolTip.this.chartRenderingInfo);
					}
				} catch (final IOException ex) {
					// TODO logging for rendering chart error
				}
				return stream.toByteArray();
			}
		};
		return imageResource;
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		final JFreeChart chart = (JFreeChart) getDefaultModelObject();
		if (chart == null) {
			return;
		}
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			this.chartRenderingInfo.clear();
			ChartUtilities.writeChartAsPNG(stream, chart, this.width, this.height, this.chartRenderingInfo);
		} catch (final IOException ex) {
			// do something
		}
		replaceComponentTagBody(markupStream, openTag, ChartUtilities.getImageMap(this.imageMapId, this.chartRenderingInfo));
	}
}