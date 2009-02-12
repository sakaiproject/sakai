package org.sakaiproject.sitestats.tool.wicket.widget;

import org.apache.wicket.Page;
import org.sakaiproject.sitestats.api.report.ReportDef;

/** Mini stat class for displaying a single link in the widget top bar. */
public abstract class WidgetMiniStatLink extends WidgetMiniStat {
	private static final long	serialVersionUID	= 1L;

	public abstract Page getPageLink();
	
	public abstract String getPageLinkTooltip();

	@Override
	public String getValue() {
		return " ";
	}

	@Override
	public String getSecondValue() {
		return null;
	}

	@Override
	public boolean isWiderText() {
		return false;
	}
	
	@Override
	public String getTooltip() {
		return null;
	}
	
	@Override
	public ReportDef getReportDefinition() {
		return null;
	}

}
