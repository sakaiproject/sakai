package org.sakaiproject.microsoft.controller.auxiliar;

import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AutoconfigRequest {
	private SakaiSiteFilter filter = new SakaiSiteFilter();
	private String teamPattern = "";
	private boolean newTeam = false;
	private boolean newChannel = false;
}
