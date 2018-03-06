/**
 * Copyright (c) 2005-2016 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.importer.impl.translators;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import org.sakaiproject.importer.api.Importable;
import org.sakaiproject.importer.api.IMSResourceTranslator;
import org.sakaiproject.importer.impl.Blackboard55FileParser;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.Announcement;

@Slf4j
public class Bb55AnnouncementTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		// TODO Auto-generated method stub
		return "resource/x-bb-announcement";
	}

	public boolean processResourceChildren() {
		return false;
	}

	public Importable translate(Node archiveResource, Document descriptor, String contextPath, String archiveBasePath) {
		// create a new generic object to return
		Announcement item = new Announcement();

		// Dates from Bb are formatted like '2007-05-08 23:45:00 EDT'
		DateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss zzz");

		// populate the generic object fields
		item.setTitle(XPathHelper.getNodeValue("/ANNOUNCEMENT/TITLE/@value", descriptor));
		item.setDescription(XPathHelper.getNodeValue("/ANNOUNCEMENT/DESCRIPTION/TEXT", descriptor));

		item.setHtml(Boolean.getBoolean(XPathHelper.getNodeValue("/ANNOUNCEMENT/FLAGS/ISHTML/@value", descriptor)));
		item.setLiternalNewline(Boolean.getBoolean(XPathHelper.getNodeValue("/ANNOUNCEMENT/FLAGS/ISNEWLINELITERAL/@value", descriptor)));
		item.setPermanent(Boolean.getBoolean(XPathHelper.getNodeValue("/ANNOUNCEMENT/ISPERMANENT/@value", descriptor)));

		// attempt to parse the start date
		try {
			Date d = df.parse(XPathHelper.getNodeValue("/ANNOUNCEMENT/DATES/RESTRICTSTART/@value", descriptor));
			item.setStart(d);
		} catch (ParseException e) {
			// report it but continue
			log.warn("Could not parse date startdate for "+item.getTitle()+": " + e.toString());
		}

		// attempt to parse the end date
		try {
			Date d = df.parse(XPathHelper.getNodeValue("/ANNOUNCEMENT/DATES/RESTRICTEND/@value", descriptor));
			item.setEnd(d);
		} catch (ParseException e) {
			// report it but continue
			log.warn("Could not parse date enddate for "+item.getTitle()+": " + e.toString());
		}

		log.info("Translation complete for BB55 announcement item:" + item.getTitle());
		log.debug("Announcement item: " + item.toString());
		item.setLegacyGroup(Blackboard55FileParser.ANNOUNCEMENT_GROUP);
		return item;
	}

}
