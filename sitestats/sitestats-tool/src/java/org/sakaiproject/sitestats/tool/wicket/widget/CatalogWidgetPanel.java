/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.tool.wicket.widget;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.sakaiproject.sitestats.api.view.SiteStatsOverview;
import org.sakaiproject.sitestats.api.view.SiteStatsWidget;
import org.sakaiproject.sitestats.api.view.SiteStatsWidgetTab;
import org.sakaiproject.sitestats.tool.facade.Locator;

public class CatalogWidgetPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public CatalogWidgetPanel(String id, String siteId, String widgetId) {
		super(id);
		setRenderBodyOnly(true);

		SiteStatsWidget widget = resolveWidget(siteId, widgetId);
		add(new Widget("widget", widgetId, widget.getIcon(), widget.getTitle(),
				WidgetMiniStats.forWidget(siteId, widgetId), buildTabs(siteId, widgetId, widget.getTabs()), siteId));
	}

	private SiteStatsWidget resolveWidget(String siteId, String widgetId) {
		SiteStatsOverview overview = Locator.getFacade().getSiteStatsViewService().getOverview(siteId);
		for (SiteStatsWidget widget : overview.getWidgets()) {
			if (widgetId.equals(widget.getId())) {
				return widget;
			}
		}
		throw new IllegalArgumentException("Unknown SiteStats widget: " + widgetId);
	}

	private List<AbstractTab> buildTabs(String siteId, String widgetId, List<SiteStatsWidgetTab> tabs) {
		List<AbstractTab> wicketTabs = new ArrayList<AbstractTab>();
		for (SiteStatsWidgetTab tab : tabs) {
			final String tabId = tab.getId();
			wicketTabs.add(new AbstractTab(Model.of(tab.getTitle())) {
				private static final long serialVersionUID = 1L;

				@Override
				public Panel getPanel(String panelId) {
					return new WidgetTabTemplate(panelId, siteId, widgetId, tabId);
				}
			});
		}
		return wicketTabs;
	}
}
