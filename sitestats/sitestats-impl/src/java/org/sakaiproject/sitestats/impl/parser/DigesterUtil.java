package org.sakaiproject.sitestats.impl.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.impl.PrefsDataImpl;

public class DigesterUtil {

	public static Digester configureToolEventsDefDigester(String prefix, Digester digester) {        
	    // root
	    digester.addObjectCreate(prefix + "toolEventsDef", ArrayList.class );
	
	    // tool tag
	    ToolFactoryImpl toolFactory = new ToolFactoryImpl();
	    digester.addFactoryCreate(prefix + "toolEventsDef/tool", toolFactory);
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/toolId", "toolId" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/additionalToolIds", "additionalToolIdsStr" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/selected", "selected" );
	    digester.addSetNext(prefix + "toolEventsDef/tool", "add" );
	
	    // event tag
	    EventFactoryImpl eventFactoryImpl = new EventFactoryImpl();
	    digester.addFactoryCreate(prefix + "toolEventsDef/tool/event", eventFactoryImpl);
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/eventId", "eventId" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/selected", "selected" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/anonymous", "anonymous" );
	    digester.addSetNext(prefix + "toolEventsDef/tool/event", "addEvent" );
	    
	    return digester;
	}

	public static Digester configurePrefsDigester(Digester digester) {        
	    // prefs root
	    digester.addObjectCreate("prefs", PrefsDataImpl.class );
	    digester.addSetProperties("prefs" );
	    digester.addBeanPropertySetter("prefs/listToolEventsOnlyAvailableInSite", "setListToolEventsOnlyAvailableInSite" );
	    digester.addBeanPropertySetter("prefs/chartIn3D", "setChartIn3D" );
	    digester.addBeanPropertySetter("prefs/chartTransparency", "setChartTransparency" );
	    digester.addBeanPropertySetter("prefs/itemLabelsVisible", "setItemLabelsVisible" );
	    
	    // toolEventsDef
	    digester = configureToolEventsDefDigester("prefs/", digester);
	    digester.addSetNext("prefs/toolEventsDef", "setToolEventsDef" );
	    
	    
	    return digester;
	}

	public static List<ToolInfo> parseToolEventsDefinition(InputStream input) throws Exception{
		Digester digester = new Digester();
	    digester.setValidating(false);
	    
	    digester = configureToolEventsDefDigester("", digester);
	
	    // eventParserTip tag
	    EventParserTipFactoryImpl eventParserTipFactoryImpl = new EventParserTipFactoryImpl();
	    digester.addFactoryCreate("toolEventsDef/tool/eventParserTip", eventParserTipFactoryImpl);
	    digester.addSetNestedProperties("toolEventsDef/tool/eventParserTip");
	    digester.addSetNext("toolEventsDef/tool/eventParserTip", "setEventParserTip" );
	    
	    return (List<ToolInfo>) digester.parse( input );
	}

}
