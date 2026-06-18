/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0.
 */
package org.sakaiproject.sitestats.impl.parser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.sitestats.api.event.EventInfo;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.w3c.dom.Element;

final class ToolEventsXmlParser {

	private ToolEventsXmlParser() {
	}

	static List<ToolInfo> parse(InputStream input) throws Exception {
		return parse(XmlElementReader.parseRoot(input, "toolEventsDef"));
	}

	static List<ToolInfo> parse(Element toolEventsDefElement) throws Exception {
		List<ToolInfo> tools = new ArrayList<ToolInfo>();
		for(Element toolElement : XmlElementReader.getDirectChildren(toolEventsDefElement, "tool")) {
			ToolInfo toolInfo = parseTool(toolElement);
			for(Element eventElement : XmlElementReader.getDirectChildren(toolElement, "event")) {
				toolInfo.addEvent(parseEvent(eventElement));
			}
			for(Element eventParserTipElement : XmlElementReader.getDirectChildren(toolElement, "eventParserTip")) {
				toolInfo.addEventParserTip(parseEventParserTip(eventParserTipElement));
			}
			tools.add(toolInfo);
		}
		return tools;
	}

	private static ToolInfo parseTool(Element toolElement) throws Exception {
		String toolId = XmlElementReader.requiredValue(toolElement, "toolId", "tool");
		ToolInfo toolInfo = new ToolInfo(toolId.trim());
		toolInfo.setSelected(XmlElementReader.optionalBoolean(toolElement, "selected", false));
		String additionalToolIds = XmlElementReader.optionalValue(toolElement, "additionalToolIds");
		if(additionalToolIds != null) {
			toolInfo.setAdditionalToolIdsStr(additionalToolIds);
		}
		return toolInfo;
	}

	private static EventInfo parseEvent(Element eventElement) throws Exception {
		String eventId = XmlElementReader.requiredValue(eventElement, "eventId", "event");
		EventInfo eventInfo = new EventInfo(eventId);
		eventInfo.setSelected(XmlElementReader.optionalBoolean(eventElement, "selected", false));
		eventInfo.setAnonymous(XmlElementReader.optionalBoolean(eventElement, "anonymous", false));
		eventInfo.setResolvable(XmlElementReader.optionalBoolean(eventElement, "resolvable", false));
		return eventInfo;
	}

	private static EventParserTip parseEventParserTip(Element eventParserTipElement) throws Exception {
		return new EventParserTip(
				XmlElementReader.requiredValue(eventParserTipElement, "for", "eventParserTip"),
				XmlElementReader.requiredValue(eventParserTipElement, "separator", "eventParserTip"),
				XmlElementReader.requiredValue(eventParserTipElement, "index", "eventParserTip"));
	}
}
