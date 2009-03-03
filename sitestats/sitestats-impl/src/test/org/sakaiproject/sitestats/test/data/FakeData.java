package org.sakaiproject.sitestats.test.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.sitestats.api.StatsManager;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;


public class FakeData {
	// SITEs
	public final static String					SITE_A_ID			= "site-a-id";
	public final static String					SITE_A_REF			= "/site/site-a-id";
	public final static String					SITE_A_ALIAS		= "site-a-alias";
	public final static String					SITE_A_TARGET		= SITE_A_REF;
	public final static String					SITE_B_ID			= "site-b-id";
	public final static String					SITE_B_REF			= "/site/site-b-id";

	// TOOLs & EVENTs
	public final static String					TOOL_CHAT			= "sakai.chat";
	public final static String					EVENT_CHATNEW		= "chat.new";
	public final static String					EVENT_CONTENTNEW	= "content.new";
	public final static String					EVENT_CONTENTREV	= "content.revise";
	
	// EVENT LIST & EVENT MAP
	public final static List<String>			EVENTIDS			= new ArrayList<String>();
	static{
		EVENTIDS.add(EVENT_CHATNEW); 
		EVENTIDS.add(EVENT_CONTENTNEW); 
		EVENTIDS.add(EVENT_CONTENTREV); 
	};
	public final static Map<String, ToolInfo>	EVENTID_TOOL_MAP	= new HashMap<String, ToolInfo>();
	static{
		ToolInfo chat = new ToolInfo("sakai.chat");
		chat.addEvent(new EventInfo(EVENT_CHATNEW));
		chat.setEventParserTip(new EventParserTip("contextId", "/", "3"));
		EVENTID_TOOL_MAP.put(EVENT_CHATNEW, chat);

		ToolInfo resources = new ToolInfo(StatsManager.RESOURCES_TOOLID);
		resources.addEvent(new EventInfo(EVENT_CONTENTNEW));
		resources.addEvent(new EventInfo(EVENT_CONTENTREV));
		resources.setEventParserTip(new EventParserTip("contextId", "/", "3"));
		EVENTID_TOOL_MAP.put(EVENT_CONTENTNEW, resources);
		EVENTID_TOOL_MAP.put(EVENT_CONTENTREV, resources);
	}
}
