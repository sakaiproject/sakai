/**
 * Copyright (c) 2024 The Apereo Foundation
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
package org.sakaiproject.microsoft.api.data;

import java.util.List;
import java.util.ArrayList;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(builderMethodName = "hiddenBuilder")
public class MicrosoftTeamWrapper {
	private MicrosoftTeam team;
	@Builder.Default
	private boolean expanded = false;
	@Builder.Default
	private List<MicrosoftDriveItem> items = null;
	
	public void addItem(MicrosoftDriveItem item) {
		if(item != null) {
			if(items == null) {
				items = new ArrayList<MicrosoftDriveItem>();
			}
			items.add(item);
		}
	}
	
	public void clearItems() {
		items = null;
	}
	
	public boolean hasItems() {
        return items != null && items.size() > 0;
    }
	
	public static MicrosoftTeamWrapperBuilder builder(MicrosoftTeam team){
		return hiddenBuilder().team(team);
	}
}
