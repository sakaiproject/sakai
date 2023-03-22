package org.sakaiproject.microsoft.controller.auxiliar;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SiteSynchronizationRequest {
	private List<String> selectedSiteIds = new ArrayList<String>();
	private List<String> selectedTeamIds = new ArrayList<String>();
	private boolean forced = false;
	private String newTeamName;
}
