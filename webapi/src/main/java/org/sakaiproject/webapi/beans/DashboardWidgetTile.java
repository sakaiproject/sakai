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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DashboardWidgetTile {

	private String id;
	private int w = 1;
	private int h = 1;

	public static DashboardWidgetTile of(String id) {
		if ("courses".equals(id)) {
			return new DashboardWidgetTile(id, 1, 2);
		}
		return new DashboardWidgetTile(id, 1, 1);
	}

	public DashboardWidgetTile clamped() {
		int width = Math.max(1, Math.min(2, w <= 0 ? 1 : w));
		int height = Math.max(1, Math.min(2, h <= 0 ? 1 : h));
		return new DashboardWidgetTile(id, width, height);
	}
}
