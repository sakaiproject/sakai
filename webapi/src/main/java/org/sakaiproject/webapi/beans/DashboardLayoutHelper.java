/******************************************************************************
 * Copyright 2015 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

/** Normalizes dashboard layouts to tiles; missing/invalid spans default to 1x1. */
public final class DashboardLayoutHelper {

	private DashboardLayoutHelper() {
	}

	public static List<DashboardWidgetTile> fromIds(Collection<String> ids) {
		List<DashboardWidgetTile> tiles = new ArrayList<>();
		if (ids == null) {
			return tiles;
		}
		for (String id : ids) {
			if (StringUtils.isNotBlank(id) && !"picker".equals(id)) {
				tiles.add(DashboardWidgetTile.of(id));
			}
		}
		return tiles;
	}

	@SuppressWarnings("unchecked")
	public static List<DashboardWidgetTile> normalize(Object raw) {
		List<DashboardWidgetTile> tiles = new ArrayList<>();
		if (raw == null) {
			return tiles;
		}
		if (raw instanceof JsonNode) {
			return fromJsonNode((JsonNode) raw);
		}
		if (!(raw instanceof List)) {
			return tiles;
		}
		List<?> list = (List<?>) raw;
		for (Object item : list) {
			DashboardWidgetTile tile = toTile(item);
			if (tile != null) {
				tiles.add(tile.clamped());
			}
		}
		return tiles;
	}

	public static List<DashboardWidgetTile> fromJsonNode(JsonNode node) {
		List<DashboardWidgetTile> tiles = new ArrayList<>();
		if (node == null || !node.isArray()) {
			return tiles;
		}
		for (JsonNode item : node) {
			if (item.isTextual()) {
				String id = item.asText();
				if (StringUtils.isNotBlank(id) && !"picker".equals(id)) {
					tiles.add(DashboardWidgetTile.of(id));
				}
			} else if (item.isObject()) {
				String id = item.path("id").asText(null);
				if (StringUtils.isBlank(id) || "picker".equals(id)) {
					continue;
				}
				int w = item.path("w").asInt(1);
				int h = item.path("h").asInt(1);
				tiles.add(new DashboardWidgetTile(id, w, h).clamped());
			}
		}
		return tiles;
	}

	private static DashboardWidgetTile toTile(Object item) {
		if (item instanceof String) {
			String id = (String) item;
			if (StringUtils.isBlank(id) || "picker".equals(id)) {
				return null;
			}
			return DashboardWidgetTile.of(id);
		}
		if (item instanceof DashboardWidgetTile) {
			DashboardWidgetTile tile = (DashboardWidgetTile) item;
			if (StringUtils.isBlank(tile.getId()) || "picker".equals(tile.getId())) {
				return null;
			}
			return tile;
		}
		if (item instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) item;
			Object idObj = map.get("id");
			if (!(idObj instanceof String) || StringUtils.isBlank((String) idObj) || "picker".equals(idObj)) {
				return null;
			}
			int w = intValue(map.get("w"), 1);
			int h = intValue(map.get("h"), 1);
			return new DashboardWidgetTile((String) idObj, w, h);
		}
		return null;
	}

	private static int intValue(Object value, int defaultValue) {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return defaultValue;
			}
		}
		return defaultValue;
	}

	public static List<DashboardWidgetTile> removeWidget(List<DashboardWidgetTile> layout, String widgetId) {
		List<DashboardWidgetTile> next = new ArrayList<>();
		if (layout == null) {
			return next;
		}
		for (DashboardWidgetTile tile : layout) {
			if (tile != null && !widgetId.equals(tile.getId())) {
				next.add(tile);
			}
		}
		return next;
	}
}
