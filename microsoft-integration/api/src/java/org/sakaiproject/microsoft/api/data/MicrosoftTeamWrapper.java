package org.sakaiproject.microsoft.api.data;

import java.util.List;

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
	
	public boolean hasItems() {
        return items != null && items.size() > 0;
    }
	
	public static MicrosoftTeamWrapperBuilder builder(MicrosoftTeam team){
		return hiddenBuilder().team(team);
	}
}
