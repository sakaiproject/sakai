package org.sakaiproject.blti.entityprovider;

import java.util.Locale;
import java.util.Map;

import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Statisticable;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;

/**
 * An entity provider to register Basic LTI events with SiteStats 
 * 
 */
public class BLTIEventsEntityProvider extends AbstractEntityProvider implements AutoRegisterEntityProvider, Statisticable, Describeable {

	public final static String PREFIX = "basiclti-events";
	public final static String TOOL_ID = "sakai.basiclti";

	public final static String[] EVENT_KEYS = new String[] {
		"basiclti.launch",
			"basiclti.config"
	};



	public String getEntityPrefix() {
		return PREFIX;
	}

	public String getAssociatedToolId() {
		return TOOL_ID;
	}

	public String[] getEventKeys() {
		return EVENT_KEYS;
	}

	public Map<String, String> getEventNames(Locale locale) {
		return null;
	}

}
