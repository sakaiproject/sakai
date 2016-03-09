package org.sakaiproject.gradebookng.tool.panels;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
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
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.model.GbGradeInfo;
import org.sakaiproject.gradebookng.business.model.GbStudentGradeInfo;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
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

		DefaultCategoryDataset data = new DefaultCategoryDataset();

		SortedMap<String, Integer> counts = new TreeMap();
		Integer extraCredits = 0;

		int range = 10;

		for (int start=0; start < 100; start=start+range) {
			String key = String.format("%d-%d", start, start + range);
			counts.put(key, 0);
		}

		for (Iterator<Double> iter = allGrades.iterator(); iter.hasNext();) {
			Double grade = iter.next();

			if (grade > assignment.getPoints()) {
				extraCredits = extraCredits + 1;
				continue;
			}

			double percentage = grade / assignment.getPoints() * 100;

			int total = Double.valueOf(Math.ceil(percentage) / range).intValue();

			int start = total * range;

			if (start == 100) {
				start = start - range;
			}
			String key = String.format("%d-%d", start, start + range);
			counts.put(key, counts.get(key) + 1);
		}

		for (String label : counts.keySet()) {
			data.addValue(counts.get(label), "count", label);
		}
		if (extraCredits > 0) {
			data.addValue(extraCredits, "count",
					getString("label.statistics.chart.extracredit"));
		}

		JFreeChart chart = ChartFactory.createBarChart(
				null,			// the chart title
				getString("label.statistics.chart.xaxis"),	// the label for the category axis
				null,						// the label for the value axis
				data,					// the dataset for the chart
				PlotOrientation.VERTICAL,		// the plot orientation 
				false,				// show legend
				true,				// show tooltips
				false);				// show urls

		chart.setBorderVisible(false);

		CategoryPlot categoryPlot = chart.getCategoryPlot();
		BarRenderer br = (BarRenderer) categoryPlot.getRenderer();

		br.setItemMargin(0);
		br.setMinimumBarLength(0.05);
		br.setMaximumBarWidth(0.1);
		br.setSeriesPaint(0, new Color(51, 122, 183));
		br.setBarPainter(new StandardBarPainter());
		br.setShadowPaint(new Color(220, 220, 220));
		br.setDefaultShadowsVisible(true);

		br.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
				getString("label.statistics.chart.tooltip"), NumberFormat.getInstance()));

		categoryPlot.setRenderer(br);

		// show only integers in the count axis
		categoryPlot.getRangeAxis().setStandardTickUnits(new NumberTickUnitSource(true));
		categoryPlot.setBackgroundPaint(new Color(238, 238, 238));

		add(new JFreeChartImageWithToolTip("chart", Model.of(chart), "tooltip", 792, 440));

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

		add(new AjaxLink<Void>("done") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				GradeStatisticsPanel.this.window.close(target);
			}
		});
	}

	private String constructAverageLabel(final List<Double> allGrades, final Assignment assignment) {
		double average = calculateAverage(allGrades);
		String averageFormatted = FormatHelper.formatDoubleToTwoDecimalPlaces(Double.valueOf(average));
		Double total = assignment.getPoints();
		String percentage = FormatHelper.formatDoubleAsPercentage(100 * (average / total.doubleValue()));

		return (new StringResourceModel("label.statistics.averagevalue",
				null,
				new Object[] { averageFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage })).getString();
	}

	private String constructMedianLabel(final List<Double> allGrades, final Assignment assignment) {
		double median = calculateMedian(allGrades);
		String medianFormatted = FormatHelper.formatDoubleToTwoDecimalPlaces(Double.valueOf(median));
		Double total = assignment.getPoints();
		String percentage = FormatHelper.formatDoubleAsPercentage(100 * (median / total.doubleValue()));

		return (new StringResourceModel("label.statistics.medianvalue",
				null,
				new Object[] { medianFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage })).getString();
	}

	private String constructLowestLabel(final List<Double> allGrades, final Assignment assignment) {
		double lowest = Collections.min(allGrades);
		String lowestFormatted = FormatHelper.formatDoubleToTwoDecimalPlaces(Double.valueOf(lowest));
		Double total = assignment.getPoints();
		String percentage = FormatHelper.formatDoubleAsPercentage(100 * (lowest / total.doubleValue()));

		return (new StringResourceModel("label.statistics.lowestvalue",
				null,
				new Object[] { lowestFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage })).getString();
	}

	private String constructHighestLabel(final List<Double> allGrades, final Assignment assignment) {
		double highest = Collections.max(allGrades);
		String highestFormatted = FormatHelper.formatDoubleToTwoDecimalPlaces(Double.valueOf(highest));
		Double total = assignment.getPoints();
		String percentage = FormatHelper.formatDoubleAsPercentage(100 * (highest / total.doubleValue()));

		return new StringResourceModel("label.statistics.highestvalue",
				null,
				new Object[] { highestFormatted, FormatHelper.formatGrade(String.valueOf(total)), percentage }).getString();
	}

	private String constructStandardDeviationLabel(final List<Double> allGrades) {
		double deviation = calculateStandardDeviation(allGrades);

		return FormatHelper.formatDoubleToTwoDecimalPlaces(Double.valueOf(deviation));
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

class JFreeChartImageWithToolTip extends NonCachingImage {
	private String imageMapId;
	private int width;
	private int height;
	private ChartRenderingInfo chartRenderingInfo = new ChartRenderingInfo(new StandardEntityCollection());

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
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				try {
					if (chart != null) {
						chartRenderingInfo.clear();
						ChartUtilities.writeChartAsPNG(stream, chart, width, height, chartRenderingInfo);
					}
				} catch (IOException ex) {
					// TODO logging for rendering chart error
				}
				return stream.toByteArray();
			}
		};
		return imageResource;
	}

	@Override
	public void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag) {
		JFreeChart chart = (JFreeChart) getDefaultModelObject();
		if (chart == null) {
			return;
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			if (chart != null) {
				chartRenderingInfo.clear();
				ChartUtilities.writeChartAsPNG(stream, chart, width, height, chartRenderingInfo);
			}
		} catch (IOException ex) {
			// do something
		}
		replaceComponentTagBody(markupStream, openTag, ChartUtilities.getImageMap(imageMapId, chartRenderingInfo));
	}
}