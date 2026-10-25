/*
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.sakaiproject.webapi.beans.DashboardLayoutHelper;
import org.sakaiproject.webapi.beans.DashboardWidgetTile;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DashboardLayoutHelperTest {

	@Test
	public void fromIdsDefaultsToOneByOne() {
		List<DashboardWidgetTile> tiles = DashboardLayoutHelper.fromIds(List.of("announcements", "picker", "calendar", "courses", ""));
		assertEquals(3, tiles.size());
		assertEquals("announcements", tiles.get(0).getId());
		assertEquals(1, tiles.get(0).getW());
		assertEquals(1, tiles.get(0).getH());
		assertEquals("calendar", tiles.get(1).getId());
		assertEquals("courses", tiles.get(2).getId());
		assertEquals(1, tiles.get(2).getW());
		assertEquals(2, tiles.get(2).getH());
	}

	@Test
	public void missingSpansDefaultToOneByOne() {
		Map<String, Object> idOnly = new HashMap<>();
		idOnly.put("id", "courses");
		List<DashboardWidgetTile> tiles = DashboardLayoutHelper.normalize(List.of(idOnly));
		assertEquals(1, tiles.size());
		assertEquals(1, tiles.get(0).getW());
		assertEquals(1, tiles.get(0).getH());
	}

	@Test
	public void normalizeClampsSpansAndSkipsPicker() {
		List<DashboardWidgetTile> tiles = DashboardLayoutHelper.normalize(List.of(
				Map.of("id", "courses", "w", 3, "h", 0),
				Map.of("id", "picker", "w", 1, "h", 1)));
		assertEquals(1, tiles.size());
		assertEquals("courses", tiles.get(0).getId());
		assertEquals(2, tiles.get(0).getW());
		assertEquals(1, tiles.get(0).getH());
	}

	@Test
	public void fromJsonNodeReadsTiles() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		List<DashboardWidgetTile> tiles = DashboardLayoutHelper.fromJsonNode(
				mapper.readTree("[{\"id\":\"courses\",\"w\":2,\"h\":2}]"));
		assertEquals(1, tiles.size());
		assertEquals(2, tiles.get(0).getW());
		assertEquals(2, tiles.get(0).getH());
	}

	@Test
	public void unrecognizedPayloadIsEmpty() {
		assertTrue(DashboardLayoutHelper.normalize("not-a-list").isEmpty());
		assertTrue(DashboardLayoutHelper.normalize(null).isEmpty());
	}

	@Test
	public void removeWidgetFiltersById() {
		List<DashboardWidgetTile> layout = DashboardLayoutHelper.fromIds(List.of("tasks", "calendar"));
		List<DashboardWidgetTile> next = DashboardLayoutHelper.removeWidget(layout, "tasks");
		assertEquals(1, next.size());
		assertEquals("calendar", next.get(0).getId());
		assertTrue(DashboardLayoutHelper.removeWidget(null, "tasks").isEmpty());
	}
}
