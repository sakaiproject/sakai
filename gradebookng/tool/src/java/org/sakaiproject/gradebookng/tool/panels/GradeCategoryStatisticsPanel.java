package org.sakaiproject.gradebookng.tool.panels;

import java.awt.Color;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberTickUnitSource;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.sakaiproject.gradebookng.business.GradebookNgBusinessService;
import org.sakaiproject.gradebookng.business.util.FormatHelper;
import org.sakaiproject.gradebookng.tool.component.GbAjaxLink;
import org.sakaiproject.gradebookng.tool.component.JFreeChartImageWithToolTip;
import org.sakaiproject.service.gradebook.shared.CategoryDefinition;

public class GradeCategoryStatisticsPanel extends Panel {

    private static final long serialVersionUID = 1L;

    private final ModalWindow window;
    private final CategoryDefinition category;
    @SpringBean(name = "org.sakaiproject.gradebookng.business.GradebookNgBusinessService")
    protected GradebookNgBusinessService businessService;

    public GradeCategoryStatisticsPanel(final String id, final IModel<Long> model, final ModalWindow window, final CategoryDefinition category) {
        super(id, model);
        this.window = window;
        this.category = category;
    }

    public GradeCategoryStatisticsPanel(final String id, final IModel<Long> model, final ModalWindow window, final String categoryId) {
        super(id, model);
        this.window = window;
        List<CategoryDefinition> categories = this.businessService.getGradebookCategories();
        CategoryDefinition categ = null;
        for(CategoryDefinition cat : categories){
            if(StringUtils.equalsIgnoreCase(cat.getId().toString(), categoryId)){
                categ = cat;
                break;
            }
        }
        this.category = categ;
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        final List<Double> allAssignmentGrades = businessService.getCategoryAssignmentTotals(category);

        GradeCategoryStatisticsPanel.this.window.setTitle(
                (new StringResourceModel("label.statistics.title", null,
                        new Object[] { category.getName() }).getString()));

        final DefaultCategoryDataset data = new DefaultCategoryDataset();
        final SortedMap<String, Integer> counts = new TreeMap();

        // Start off with a 0-50% range
        counts.put(String.format("%d-%d", 0, 50), 0);

        final int range = 10;
        for (int start = 50; start < 100; start = start + range) {
            final String key = String.format("%d-%d", start, start + range);
            counts.put(key, 0);
        }

        for (final Double grade : allAssignmentGrades) {
            final double percentage = grade;
            final int total = Double.valueOf(Math.ceil(percentage) / range).intValue();
            int start = total * range;

            if(start > 100){
                start = 100;
            }
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

        final JFreeChart chart = ChartFactory.createBarChart(
                null, // the chart title
                getString("label.statistics.category.chart.xaxis"), // the label for the category axis
                getString("label.statistics.category.chart.yaxis"), // the label for the value axis
                data, // the dataset for the chart
                PlotOrientation.VERTICAL, // the plot orientation
                false, // show legend
                true, // show tooltips
                false); // show urls

        chart.setBorderVisible(false);
        chart.setAntiAlias(false);

        final CategoryPlot categoryPlot = chart.getCategoryPlot();
        final BarRenderer br = getBarRenderer(categoryPlot);

        categoryPlot.setRenderer(br);

        // show only integers in the count axis
        categoryPlot.getRangeAxis().setStandardTickUnits(new NumberTickUnitSource(true));
        categoryPlot.setBackgroundPaint(Color.white);

        add(new JFreeChartImageWithToolTip("chart", Model.of(chart), "tooltip", 540, 300));

        addLabels(allAssignmentGrades);

        add(new GbAjaxLink("done") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                GradeCategoryStatisticsPanel.this.window.close(target);
            }
        });
    }

    private BarRenderer getBarRenderer(CategoryPlot categoryPlot){
        BarRenderer br = (BarRenderer) categoryPlot.getRenderer();
        br.setItemMargin(0);
        br.setMinimumBarLength(0.05);
        br.setMaximumBarWidth(0.1);
        br.setSeriesPaint(0, new Color(51, 122, 183));
        br.setBarPainter(new StandardBarPainter());
        br.setShadowPaint(new Color(220, 220, 220));
        BarRenderer.setDefaultShadowsVisible(true);

        br.setBaseToolTipGenerator(new StandardCategoryToolTipGenerator(
                getString("label.statistics.chart.tooltip"), NumberFormat.getInstance()));

        return br;
    }

    private void addLabels(List<Double> allAssignmentGrades) {
        if (allAssignmentGrades.size() > 0) {
            add(new Label("average", constructAverageLabel(allAssignmentGrades)));
            add(new Label("median", constructMedianLabel(allAssignmentGrades)));
            add(new Label("lowest", constructLowestLabel(allAssignmentGrades)));
            add(new Label("highest", constructHighestLabel(allAssignmentGrades)));
            add(new Label("deviation", constructStandardDeviationLabel(allAssignmentGrades)));
            add(new Label("graded", String.valueOf(allAssignmentGrades.size())));
        } else {
            add(new Label("average", "-"));
            add(new Label("median", "-"));
            add(new Label("lowest", "-"));
            add(new Label("highest", "-"));
            add(new Label("deviation", "-"));
            add(new Label("graded", "-"));
        }
    }
    private String constructAverageLabel(final List<Double> allAssignmentGrades) {
        final double average = businessService.calculateAverage(allAssignmentGrades);
        final String percentage = FormatHelper.formatDoubleAsPercentage(average);

        return percentage;
    }

    private String constructMedianLabel(final List<Double> allAssignmentGrades) {
        final double median = calculateMedian(allAssignmentGrades);
        final String percentage = FormatHelper.formatDoubleAsPercentage(median);

        return percentage;
    }

    private String constructLowestLabel(final List<Double> allAssignmentGrades) {
        final double lowest = Collections.min(allAssignmentGrades);
        final String percentage = FormatHelper.formatDoubleAsPercentage(lowest);

        return percentage;
    }

    private String constructHighestLabel(final List<Double> allAssignmentGrades) {
        final double highest = Collections.max(allAssignmentGrades);
        final String percentage = FormatHelper.formatDoubleAsPercentage(highest);

        return percentage;
    }

    private String constructStandardDeviationLabel(final List<Double> allAssignmentGrades) {
        final double deviation = calculateStandardDeviation(allAssignmentGrades);

        return FormatHelper.formatDoubleToDecimal(Double.valueOf(deviation));
    }

    private double calculateMedian(final List<Double> allAssignmentGrades) {
        final int middle = allAssignmentGrades.size() / 2;
        if (allAssignmentGrades.size() % 2 == 1) {
            return allAssignmentGrades.get(middle);
        } else {
            return (allAssignmentGrades.get(middle - 1) + allAssignmentGrades.get(middle)) / 2.0;
        }
    }

    private double calculateVariance(final List<Double> allAssignmentGrades) {
        final double mean = businessService.calculateAverage(allAssignmentGrades);
        double sum = 0;

        for (int i = 0; i < allAssignmentGrades.size(); i++) {
            final double grade = allAssignmentGrades.get(i);
            sum += (mean - grade) * (mean - grade);
        }

        return sum / allAssignmentGrades.size();
    }

    private double calculateStandardDeviation(final List<Double> allAssignmentGrades) {
        return Math.sqrt(calculateVariance(allAssignmentGrades));
    }

}
