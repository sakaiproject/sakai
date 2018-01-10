/* *****************************************************************************
 * Bb6AnnouncementTranslator.java - created by aaronz
 * 
 * Copyright (c) 2006 Virginia Polytechnic Institute and State University
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 * Contributors:
 * Aaron Zeckoski (aaronz@vt.edu)
 * 
 * ****************************************************************************/

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
import org.sakaiproject.importer.impl.Blackboard6FileParser;
import org.sakaiproject.importer.impl.XPathHelper;
import org.sakaiproject.importer.impl.importables.Announcement;

/**
 * This class takes the announcement data from the BB6 import file and
 * places it into a generic announcement object
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
@Slf4j
public class Bb6AnnouncementTranslator implements IMSResourceTranslator {

	public String getTypeName() {
		return "resource/x-bb-announcement";
	}

	public Importable translate(Node resourceNode, Document descriptor, String contextPath, String archiveBasePath) {
		// create a new generic object to return
		Announcement item = new Announcement();
		
		// this sets the display category of this item apparently (the one the user will see)
		item.setLegacyGroup(item.getDisplayType());

		// Dates from Bb are formatted like '2007-05-08 23:45:00 EDT'
		DateFormat df = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss zzz");
		String bbid = XPathHelper.getNodeValue("/ANNOUNCEMENT/@id", descriptor);

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
			log.warn("Could not parse date startdate for "+bbid+": " + e.toString());
		}

		// attempt to parse the end date
		try {
			Date d = df.parse(XPathHelper.getNodeValue("/ANNOUNCEMENT/DATES/RESTRICTEND/@value", descriptor));
			item.setEnd(d);
		} catch (ParseException e) {
			// report it but continue
			log.warn("Could not parse date enddate for "+bbid+": " + e.toString());
		}

		log.info("Translation complete for BB6 announcement item:" + bbid);
		log.debug("Announcement item: " + item.toString());
		item.setLegacyGroup(Blackboard6FileParser.ANNOUNCEMENT_GROUP);
		return item;
	}

	public boolean processResourceChildren() {
		return false;
	}

}
