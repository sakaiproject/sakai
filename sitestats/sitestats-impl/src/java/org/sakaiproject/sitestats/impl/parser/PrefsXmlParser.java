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

import org.sakaiproject.sitestats.api.PrefsData;
import org.w3c.dom.Element;

final class PrefsXmlParser {

	private PrefsXmlParser() {
	}

	static PrefsData parse(InputStream input) throws Exception {
		Element root = XmlElementReader.parseRoot(input, "prefs");
		PrefsData prefsData = new PrefsData();
		setBoolean(root, "listToolEventsOnlyAvailableInSite", prefsData::setListToolEventsOnlyAvailableInSite);
		setBoolean(root, "showOwnStatisticsToStudents", prefsData::setShowOwnStatisticsToStudents);
		setBoolean(root, "chartIn3D", prefsData::setChartIn3D);
		setFloat(root, "chartTransparency", prefsData::setChartTransparency);
		setBoolean(root, "itemLabelsVisible", prefsData::setItemLabelsVisible);
		setBoolean(root, "useAllTools", prefsData::setUseAllTools);

		Element toolEventsDef = XmlElementReader.getDirectChild(root, "toolEventsDef");
		if(toolEventsDef != null) {
			prefsData.setToolEventsDef(ToolEventsXmlParser.parse(toolEventsDef));
		}
		return prefsData;
	}

	private static void setBoolean(Element element, String name, BooleanSetter setter) throws Exception {
		if(XmlElementReader.optionalValue(element, name) != null) {
			setter.set(XmlElementReader.optionalBoolean(element, name, false));
		}
	}

	private static void setFloat(Element element, String name, FloatSetter setter) throws Exception {
		if(XmlElementReader.optionalValue(element, name) != null) {
			setter.set(XmlElementReader.optionalFloat(element, name, 0.0f));
		}
	}

	private interface BooleanSetter {
		void set(boolean value);
	}

	private interface FloatSetter {
		void set(float value);
	}
}
