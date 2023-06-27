package org.sakaiproject.microsoft.controller.auxiliar;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.microsoft.api.data.MicrosoftUserIdentifier;
import org.sakaiproject.microsoft.api.data.SakaiSiteFilter;
import org.sakaiproject.microsoft.api.data.SakaiUserIdentifier;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ConfigRequest {
	List<String> synch_config_items = new ArrayList<>();
	boolean onedriveEnabled;
	SakaiUserIdentifier mapped_sakai_user_id;
	MicrosoftUserIdentifier mapped_microsoft_user_id;
	SakaiSiteFilter siteFilter;
	long syncDuration;
	SakaiSiteFilter jobSiteFilter;
}
