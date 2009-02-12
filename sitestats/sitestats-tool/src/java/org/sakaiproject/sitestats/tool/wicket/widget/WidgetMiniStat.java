package org.sakaiproject.sitestats.tool.wicket.widget;

import java.io.Serializable;

import org.sakaiproject.sitestats.api.report.ReportDef;

/** Mini stat class for displaying a single value in the widget top bar. */
public abstract class WidgetMiniStat implements Serializable {
	private static final long	serialVersionUID	= 1L;
	
	public WidgetMiniStat() {
	}

	public abstract String getValue();

	public abstract String getSecondValue();

	public abstract String getLabel();

	public abstract String getTooltip();

	public abstract boolean isWiderText();

	public abstract ReportDef getReportDefinition();
}
