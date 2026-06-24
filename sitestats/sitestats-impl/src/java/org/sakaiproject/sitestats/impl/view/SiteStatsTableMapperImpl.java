/**
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.sitestats.api.EventStat;
import org.sakaiproject.sitestats.api.LessonBuilderStat;
import org.sakaiproject.sitestats.api.ResourceStat;
import org.sakaiproject.sitestats.api.SitePresence;
import org.sakaiproject.sitestats.api.SiteVisits;
import org.sakaiproject.sitestats.api.Stat;
import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.Util;
import org.sakaiproject.sitestats.api.event.EventRegistryService;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportManager;
import org.sakaiproject.sitestats.api.report.ReportParams;
import org.sakaiproject.sitestats.api.view.SiteStatsTableCell;
import org.sakaiproject.sitestats.api.view.SiteStatsTableColumn;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;

@Slf4j
public class SiteStatsTableMapperImpl implements SiteStatsTableMapper {

	private static final String ICON_SAKAI = "si si-";

	@Setter private StatsManager statsManager;
	@Setter private ReportManager reportManager;
	@Setter private EventRegistryService eventRegistryService;
	@Setter private SiteService siteService;
	@Setter private UserDirectoryService userDirectoryService;
	@Setter private UserTimeService userTimeService;

	@Setter private ResourceLoader messages = new ResourceLoader("Messages");
	private Map<String, ColumnDefinition> definitionsByKey;

	@Override
	public List<SiteStatsTableColumn> getColumns(ReportParams reportParams, boolean sortable) {
		List<SiteStatsTableColumn> columns = new ArrayList<SiteStatsTableColumn>();
		for (ColumnDefinition definition : definitionsByKey().values()) {
			if (reportManager.isReportColumnAvailable(reportParams, definition.reportParamKey)) {
				columns.add(toColumn(definition, sortable));
			}
		}
		return columns;
	}

	@Override
	public SiteStatsTableColumn getColumn(String key, boolean sortable) {
		return toColumn(definitionFor(key), sortable);
	}

	@Override
	public SiteStatsTableCell getCell(Stat stat, String key) {
		ColumnDefinition definition = definitionFor(key);
		SiteStatsTableCell cell = new SiteStatsTableCell();
		Object raw = definition.rawValueProvider.value(stat);
		cell.setRaw(raw);
		cell.setSort(raw);
		cell.setDisplay(raw == null ? "" : definition.displayValueProvider.display(stat, raw));
		if (definition.cellDecorator != null) {
			definition.cellDecorator.decorate(cell, stat, raw);
		}
		return cell;
	}

	@Override
	public Number getNumericValue(Stat stat, String key) {
		Object raw = definitionFor(key).rawValueProvider.value(stat);
		if (raw instanceof Number) {
			if (StatsManager.T_DURATION.equals(key)) {
				double duration = ((Number) raw).doubleValue();
				return Double.valueOf(Util.round(duration / 1000 / 60, 1));
			}
			return (Number) raw;
		}
		throw new IllegalArgumentException("Column is not numeric: " + key);
	}

	private Map<String, ColumnDefinition> definitionsByKey() {
		if (definitionsByKey == null) {
			Map<String, ColumnDefinition> definitions = new LinkedHashMap<String, ColumnDefinition>();
			addDefinition(definitions, column(StatsManager.T_SITE, StatsManager.T_SITE, "th_site", "text", null, StatsManager.T_SITE,
				stat -> stat.getSiteId(),
				(stat, raw) -> siteTitle(asString(raw)),
				this::decorateSite));
			addDefinition(definitions, column(USER_ID, StatsManager.T_USER, "th_id", "text", null, StatsManager.T_USER,
				stat -> stat.getUserId(),
				(stat, raw) -> userDisplayId(asString(raw)),
				null));
			addDefinition(definitions, column(StatsManager.T_USER, StatsManager.T_USER, "th_user", "text", null, SORT_USER_NAME,
				stat -> stat.getUserId(),
				(stat, raw) -> userDisplayName(asString(raw)),
				null));
			addDefinition(definitions, column(StatsManager.T_TOOL, StatsManager.T_TOOL, "th_tool", "text", null, StatsManager.T_TOOL,
				stat -> stat instanceof EventStat ? ((EventStat) stat).getToolId() : null,
				(stat, raw) -> toolName(asString(raw)),
				this::decorateTool));
			addDefinition(definitions, column(StatsManager.T_EVENT, StatsManager.T_EVENT, "th_event", "text", null, StatsManager.T_EVENT,
				stat -> stat instanceof EventStat ? ((EventStat) stat).getEventId() : null,
				(stat, raw) -> eventName(asString(raw)),
				this::decorateEvent));
			addDefinition(definitions, column(StatsManager.T_RESOURCE, StatsManager.T_RESOURCE, "th_resource", "link", null, StatsManager.T_RESOURCE,
				stat -> stat instanceof ResourceStat ? ((ResourceStat) stat).getResourceRef() : null,
				(stat, raw) -> resourceName(asString(raw)),
				this::decorateResource));
			addDefinition(definitions, column(StatsManager.T_RESOURCE_ACTION, StatsManager.T_RESOURCE_ACTION, "th_action", "text", null, StatsManager.T_RESOURCE_ACTION,
				stat -> stat instanceof ResourceStat ? ((ResourceStat) stat).getResourceAction() : null,
				(stat, raw) -> actionName(asString(raw)),
				null));
			addDefinition(definitions, column(StatsManager.T_PAGE, StatsManager.T_PAGE, "th_page", "text", null, StatsManager.T_PAGE,
				stat -> stat instanceof LessonBuilderStat ? ((LessonBuilderStat) stat).getPageRef() : null,
				this::pageTitle,
				null));
			addDefinition(definitions, column(StatsManager.T_PAGE_ACTION, StatsManager.T_PAGE_ACTION, "th_action", "text", null, StatsManager.T_PAGE_ACTION,
				stat -> stat instanceof LessonBuilderStat ? ((LessonBuilderStat) stat).getPageAction() : null,
				(stat, raw) -> actionName(asString(raw)),
				null));
			addDefinition(definitions, column(StatsManager.T_DATE, StatsManager.T_DATE, "th_date", "date", null, StatsManager.T_DATE,
				Stat::getDate,
				(stat, raw) -> dateDisplay((Date) raw),
				null));
			addDefinition(definitions, column(StatsManager.T_DATEMONTH, StatsManager.T_DATEMONTH, "th_date", "date", null, StatsManager.T_DATE,
				Stat::getDate,
				(stat, raw) -> new SimpleDateFormat("yyyy-MM", currentLocale()).format((Date) raw),
				null));
			addDefinition(definitions, column(StatsManager.T_DATEYEAR, StatsManager.T_DATEYEAR, "th_date", "date", null, StatsManager.T_DATE,
				Stat::getDate,
				(stat, raw) -> new SimpleDateFormat("yyyy", currentLocale()).format((Date) raw),
				null));
			addDefinition(definitions, column(StatsManager.T_LASTDATE, StatsManager.T_LASTDATE, "th_lastdate", "date", null, StatsManager.T_DATE,
				Stat::getDate,
				(stat, raw) -> dateDisplay((Date) raw),
				null));
			addDefinition(definitions, column(StatsManager.T_TOTAL, StatsManager.T_TOTAL, "th_total", "number", "end", StatsManager.T_TOTAL,
				stat -> Long.valueOf(stat.getCount()),
				(stat, raw) -> String.valueOf(raw),
				null));
			addDefinition(definitions, column(StatsManager.T_VISITS, StatsManager.T_VISITS, "th_visits", "number", "end", StatsManager.T_VISITS,
				stat -> stat instanceof SiteVisits ? Long.valueOf(((SiteVisits) stat).getTotalVisits()) : Long.valueOf(stat.getCount()),
				(stat, raw) -> String.valueOf(raw),
				null));
			addDefinition(definitions, column(StatsManager.T_UNIQUEVISITS, StatsManager.T_UNIQUEVISITS, "th_uniquevisitors", "number", "end", StatsManager.T_UNIQUEVISITS,
				stat -> stat instanceof SiteVisits ? Long.valueOf(((SiteVisits) stat).getTotalUnique()) : Long.valueOf(stat.getCount()),
				(stat, raw) -> String.valueOf(raw),
				null));
			addDefinition(definitions, column(StatsManager.T_DURATION, StatsManager.T_DURATION, "th_duration", "number", "end", StatsManager.T_DURATION,
				stat -> stat instanceof SitePresence ? Long.valueOf(((SitePresence) stat).getDuration()) : Long.valueOf(stat.getCount()),
				(stat, raw) -> durationDisplay((Number) raw),
				null));
			definitionsByKey = Collections.unmodifiableMap(definitions);
		}
		return definitionsByKey;
	}

	private void addDefinition(Map<String, ColumnDefinition> definitions, ColumnDefinition definition) {
		definitions.put(definition.key, definition);
	}

	private ColumnDefinition column(String key, String reportParamKey, String labelKey, String type, String align, String sortKey,
			RawValueProvider rawValueProvider, DisplayValueProvider displayValueProvider, CellDecorator cellDecorator) {
		return new ColumnDefinition(key, reportParamKey, labelKey, type, align, sortKey, rawValueProvider, displayValueProvider, cellDecorator);
	}

	private ColumnDefinition definitionFor(String key) {
		ColumnDefinition definition = definitionsByKey().get(key);
		if (definition == null) {
			throw new IllegalArgumentException("Unsupported SiteStats table column: " + key);
		}
		return definition;
	}

	private SiteStatsTableColumn toColumn(ColumnDefinition definition, boolean sortable) {
		SiteStatsTableColumn column = new SiteStatsTableColumn();
		column.setKey(definition.key);
		column.setLabel(message(definition.labelKey));
		column.setType(definition.type);
		column.setAlign(definition.align);
		column.setSortKey(definition.sortKey);
		column.setSortable(sortable && StringUtils.isNotBlank(definition.sortKey));
		return column;
	}

	private void decorateSite(SiteStatsTableCell cell, Stat stat, Object raw) {
		if (StringUtils.isBlank(asString(raw))) {
			return;
		}
		try {
			Site site = siteService.getSite(asString(raw));
			cell.setHref(site.getUrl());
		} catch (IdUnusedException e) {
			log.debug("Unable to resolve site {} for SiteStats table cell", raw, e);
		}
	}

	private void decorateTool(SiteStatsTableCell cell, Stat stat, Object raw) {
		String toolId = asString(raw);
		if (StringUtils.isNotBlank(toolId)) {
			cell.setIcon(ICON_SAKAI + toolId.replace('.', '-'));
		}
	}

	private void decorateEvent(SiteStatsTableCell cell, Stat stat, Object raw) {
		Map<String, ToolInfo> eventIdToolMap = eventRegistryService.getEventIdToolMap();
		ToolInfo toolInfo = eventIdToolMap.get(asString(raw));
		if (toolInfo != null) {
			String toolId = toolInfo.getToolId();
			if (StringUtils.isNotBlank(toolId)) {
				cell.setIcon(ICON_SAKAI + toolId.replace('.', '-'));
			}
		}
	}

	private void decorateResource(SiteStatsTableCell cell, Stat stat, Object raw) {
		String resourceRef = asString(raw);
		if (StringUtils.isBlank(resourceRef)) {
			return;
		}
		cell.setHref(statsManager.getResourceURL(resourceRef));
		cell.setIcon(statsManager.getResourceImage(resourceRef));
	}

	private String userDisplayId(String userId) {
		if (userId == null || "-".equals(userId) || EventTrackingService.UNKNOWN_USER.equals(userId)) {
			return "-";
		}
		try {
			return userDirectoryService.getUser(userId).getDisplayId();
		} catch (UserNotDefinedException e) {
			return userId;
		}
	}

	private String userDisplayName(String userId) {
		if (userId == null) {
			return message("user_unknown");
		}
		if ("-".equals(userId)) {
			return message("user_anonymous");
		}
		if (EventTrackingService.UNKNOWN_USER.equals(userId)) {
			return message("user_anonymous_access");
		}
		return statsManager.getUserNameForDisplay(userId);
	}

	private String siteTitle(String siteId) {
		try {
			return siteService.getSite(siteId).getTitle();
		} catch (IdUnusedException e) {
			return message("site_unknown");
		}
	}

	private String toolName(String toolId) {
		return StringUtils.isBlank(toolId) ? "" : eventRegistryService.getToolName(toolId);
	}

	private String eventName(String eventId) {
		return StringUtils.isBlank(eventId) ? "" : eventRegistryService.getEventName(eventId);
	}

	private String resourceName(String resourceRef) {
		String resourceName = statsManager.getResourceName(resourceRef);
		return "null".equals(resourceName) ? message("overview_file_unavailable") : resourceName;
	}

	private String pageTitle(Stat stat, Object raw) {
		if (stat instanceof LessonBuilderStat) {
			String pageTitle = ((LessonBuilderStat) stat).getPageTitle();
			return pageTitle == null ? message("resource_unknown") : pageTitle;
		}
		return "";
	}

	private String actionName(String action) {
		return StringUtils.isBlank(action) ? "" : message("action_" + action);
	}

	private String dateDisplay(Date date) {
		if (date instanceof java.sql.Date && userTimeService != null) {
			return userTimeService.shortLocalizedDate(((java.sql.Date) date).toLocalDate(), currentLocale());
		}
		return DateFormat.getDateInstance(DateFormat.SHORT, currentLocale()).format(date);
	}

	private String durationDisplay(Number raw) {
		double duration = raw.doubleValue();
		duration = Util.round(duration / 1000 / 60, 1);
		return String.valueOf(duration) + " " + message("minutes_abbr");
	}

	private String asString(Object raw) {
		return raw == null ? null : String.valueOf(raw);
	}

	private Locale currentLocale() {
		return messages.getLocale();
	}

	private String message(String key) {
		try {
			return messages.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	private interface RawValueProvider {
		Object value(Stat stat);
	}

	private interface DisplayValueProvider {
		String display(Stat stat, Object raw);
	}

	private interface CellDecorator {
		void decorate(SiteStatsTableCell cell, Stat stat, Object raw);
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static class ColumnDefinition {
		private final String key;
		private final String reportParamKey;
		private final String labelKey;
		private final String type;
		private final String align;
		private final String sortKey;
		private final RawValueProvider rawValueProvider;
		private final DisplayValueProvider displayValueProvider;
		private final CellDecorator cellDecorator;
	}
}
