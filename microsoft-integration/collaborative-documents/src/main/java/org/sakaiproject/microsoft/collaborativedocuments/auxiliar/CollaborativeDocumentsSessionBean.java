package org.sakaiproject.microsoft.collaborativedocuments.auxiliar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.microsoft.api.data.MicrosoftDriveItem;
import org.sakaiproject.microsoft.api.data.MicrosoftTeam;
import org.sakaiproject.microsoft.api.data.MicrosoftTeamWrapper;

import lombok.Data;

@Data
public class CollaborativeDocumentsSessionBean {
	/**
	 * Contains all items from all Teams, indexed by {TeamId}
	 */
	private Map<String, MicrosoftTeamWrapper> itemsByTeam = new HashMap<>();
	
	/**
	 * Contains all items indexed by {ItemId}
	 */
	private Map<String, MicrosoftDriveItem> itemsMap = new HashMap<>();
	
	private Object currentItem;
	private MicrosoftTeam currentTeam;
	
	private String lastSortBy;
	
	public List<String> getSortedTeamKeys(){
		List<String> ret = new ArrayList<>(itemsByTeam.keySet());
		ret.sort((a, b) -> {
			return a.compareToIgnoreCase(b);
		});
		return ret;
	}
}
