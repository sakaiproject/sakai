package org.sakaiproject.sitestats.tool.wicket.panels;

import java.awt.image.BufferedImage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.SummaryVisitsTotals;
import org.sakaiproject.sitestats.tool.facade.SakaiFacade;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadFragment;
import org.sakaiproject.sitestats.tool.wicket.components.AjaxLazyLoadImage;
import org.sakaiproject.sitestats.tool.wicket.pages.OverviewPage;


/**
 * @author Nuno Fernandes
 */
public class VisitsPanel extends Panel {
	private static final long		serialVersionUID	= 1L;
	private static Log				LOG					= LogFactory.getLog(VisitsPanel.class);

	/** Inject Sakai facade */
	@SpringBean
	private transient SakaiFacade	facade;

	/** The site id. */
	private String					siteId				= null;

	/** Selected chart view. */
	private String					selectedView		= StatsManager.VIEW_WEEK;
	private int						selectedWidth		= 0;
	private int						selectedHeight		= 0;

	// UI Componets
	private WebMarkupContainer		selectors			= null;
	private IndicatingAjaxLink		lastWeekLink		= null;
	private WebMarkupContainer		lastWeekLabel		= null;
	private IndicatingAjaxLink		lastMonthLink		= null;
	private WebMarkupContainer		lastMonthLabel		= null;
	private IndicatingAjaxLink		lastYearLink		= null;
	private WebMarkupContainer		lastYearLabel		= null;

	private AjaxLazyLoadFragment	visitsLoader		= null;
	private WebMarkupContainer		visitsTable			= null;
	private Fragment 				visitsTableFrag		= null;

	private AjaxLazyLoadImage		chart				= null;

	/**
	 * Default constructor.
	 * @param id The wicket:id
	 * @param siteId The related site id
	 */
	public VisitsPanel(String id, String siteId) {
		super(id);
		this.siteId = siteId;
		setRenderBodyOnly(true);
		setOutputMarkupId(true);
		renderChart();
		renderSelectors();
		renderTable();		
	}

	/** Render chart selectors. */
	private void renderSelectors() {
		selectors = new WebMarkupContainer("selectors");
		selectors.setOutputMarkupId(true);
		add(selectors);
		// Last week
		boolean inLastWeek = StatsManager.VIEW_WEEK.equals(getSelectedView());
		lastWeekLink = new IndicatingAjaxLink("lastWeekLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setSelectedView(target, StatsManager.VIEW_WEEK);
				target.addComponent(selectors);
			}			
		};
		lastWeekLink.setVisible(!inLastWeek);
		selectors.add(lastWeekLink);
		lastWeekLabel = new WebMarkupContainer("lastWeekLabel");
		lastWeekLabel.setVisible(inLastWeek);
		selectors.add(lastWeekLabel);

		// Last month
		boolean inLastMonth = StatsManager.VIEW_MONTH.equals(getSelectedView());
		lastMonthLink = new IndicatingAjaxLink("lastMonthLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setSelectedView(target, StatsManager.VIEW_MONTH);
				target.addComponent(selectors);
			}
		};
		lastMonthLink.setVisible(!inLastMonth);
		selectors.add(lastMonthLink);
		lastMonthLabel = new WebMarkupContainer("lastMonthLabel");
		lastMonthLabel.setVisible(inLastMonth);
		selectors.add(lastMonthLabel);

		// Last year
		boolean inLastYear = StatsManager.VIEW_YEAR.equals(getSelectedView());
		lastYearLink = new IndicatingAjaxLink("lastYearLink") {
			@Override
			public void onClick(AjaxRequestTarget target) {
				setSelectedView(target, StatsManager.VIEW_YEAR);
				target.addComponent(selectors);
			}

		};
		lastYearLink.setVisible(!inLastYear);
		selectors.add(lastYearLink);
		lastYearLabel = new WebMarkupContainer("lastYearLabel");
		lastYearLabel.setVisible(inLastYear);
		selectors.add(lastYearLabel);
	}

	/** Render table. */
	private void renderTable() {
		visitsLoader = new AjaxLazyLoadFragment("visitsTableContainer") {
			private static final long	serialVersionUID	= 12L;

			@Override
			public Fragment getLazyLoadFragment(String markupId) {
				return renderTableData(markupId);
			}			
		};
		add(visitsLoader);
	}
	
	/** Render data. */
	@SuppressWarnings("deprecation")
	private Fragment renderTableData(String markupId) {
		// markup
		visitsTableFrag = new Fragment(markupId, "visitsTableFragment", this);
		visitsTable = new WebMarkupContainer("visitsTableData");
		visitsTableFrag.add(visitsTable);
		
		SummaryVisitsTotals summaryVisitsTotals = facade.getStatsManager().getSummaryVisitsTotals(siteId);
		
		// Total visits
		final Label totalVisits = new Label("totalVisits", String.valueOf(summaryVisitsTotals.getTotalVisits()));
		visitsTable.add(totalVisits);

		// Average Visits
		StringBuilder avgVisitsStr = new StringBuilder();
		avgVisitsStr.append(summaryVisitsTotals.getLast7DaysVisitsAverage());
		avgVisitsStr.append('/');
		avgVisitsStr.append(summaryVisitsTotals.getLast30DaysVisitsAverage());
		avgVisitsStr.append('/');
		avgVisitsStr.append(summaryVisitsTotals.getLast365DaysVisitsAverage());
		final Label avgVisits = new Label("avgVisits", avgVisitsStr.toString());
		visitsTable.add(avgVisits);

		// Total Unique Visits
		final Label totalUniqueVisits = new Label("totalUniqueVisits", String.valueOf(summaryVisitsTotals.getTotalUniqueVisits()));
		visitsTable.add(totalUniqueVisits);

		// Total Unique Visits / Total logged in users
		StringBuilder totalUniqueVisitsRelStr = new StringBuilder();
		totalUniqueVisitsRelStr.append(summaryVisitsTotals.getTotalUniqueVisits());
		totalUniqueVisitsRelStr.append('/');
		totalUniqueVisitsRelStr.append(summaryVisitsTotals.getTotalUsers());
		totalUniqueVisitsRelStr.append(" (");
		totalUniqueVisitsRelStr.append(summaryVisitsTotals.getPercentageOfUsersThatVisitedSite());
		totalUniqueVisitsRelStr.append("%)");
		final Label totalUniqueVisitsRel = new Label("totalUniqueVisitsRel", totalUniqueVisitsRelStr.toString());
		visitsTable.add(totalUniqueVisitsRel);
		
		return visitsTableFrag;
	}
	
	public AjaxLazyLoadImage getChartComponent() {
		return chart;
	}

	/** Render chart. */
	@SuppressWarnings("serial")
	public void renderChart() {
		chart = new AjaxLazyLoadImage("visitsChart", OverviewPage.class) {
			@Override
			public BufferedImage getBufferedImage() {
				return getChartImage(selectedWidth, 200);
			}

			@Override
			public BufferedImage getBufferedImage(int width, int height) {
				return getChartImage(width, height);
			}
		};
		chart.setAutoDetermineChartSizeByAjax(".chartContainer");
		add(chart);
	}
	
	private BufferedImage getChartImage(int width, int height) {
		PrefsData prefsData = facade.getStatsManager().getPreferences(siteId, false);
		int _width = (width <= 0) ? 350 : width;
		int _height = (height <= 0) ? 200: height;
		BufferedImage img = facade.getChartService().generateVisitsChart(
				siteId, selectedView, 
				_width, _height,
				prefsData.isChartIn3D(), prefsData.getChartTransparency(),
				prefsData.isItemLabelsVisible());
		return img;
	}

	public final String getSelectedView() {
		return selectedView;
	}

	public final void setSelectedView(AjaxRequestTarget target, String selectedView) {
		this.selectedView = selectedView;
		
		boolean inLastWeek = StatsManager.VIEW_WEEK.equals(selectedView);
		boolean inLastMonth = StatsManager.VIEW_MONTH.equals(selectedView);
		boolean inLastYear = StatsManager.VIEW_YEAR.equals(selectedView);
		lastWeekLink.setVisible(!inLastWeek);
		lastWeekLabel.setVisible(inLastWeek);
		lastMonthLink.setVisible(!inLastMonth);
		lastMonthLabel.setVisible(inLastMonth);
		lastYearLink.setVisible(!inLastYear);
		lastYearLabel.setVisible(inLastYear);
		chart.renderImage(target, true);
	}

	public void setChartSize(int width, int height) {
		this.selectedWidth = width;
		this.selectedHeight = height;
	}

}
