package org.sakaiproject.sitestats.impl.parser;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.parser.EventFactory;
import org.xml.sax.Attributes;


public class EventFactoryImpl implements EventFactory, ObjectCreationFactory {
	public EventInfo createEvent(String eventId) {
		return new EventInfo(eventId);
	}

	public Object createObject(Attributes attributes) throws Exception {
		String eventId = attributes.getValue("eventId");
		String selected = attributes.getValue("selected");
		String anonymous = attributes.getValue("anonymous");

		if(eventId == null){
			throw new Exception("Mandatory eventId attribute not present on event tag.");
		}
		EventInfo eventInfo = new EventInfo(eventId);
		eventInfo.setSelected(Boolean.parseBoolean(selected));	
		eventInfo.setAnonymous(Boolean.parseBoolean(anonymous));	
		return eventInfo;
	}

	public Digester getDigester() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setDigester(Digester arg0) {
		// TODO Auto-generated method stub

	}

}
