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
