/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Charles Hedrick, hedrick@rutgers.edu
 *
 * Copyright (c) 2013 Rutgers, the State University of New Jersey
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");                                                                
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.ccexport;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;

import org.w3c.dom.Document;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.assignment2.model.constants.AssignmentConstants;
import org.sakaiproject.assignment2.model.Assignment2;
import org.sakaiproject.assignment2.model.AssignmentAttachment;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.db.api.SqlReader;
import org.sakaiproject.db.cover.SqlService;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.ccexport.ZipPrintStream;
import org.sakaiproject.lessonbuildertool.ccexport.AssignmentExport.AssignmentItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;

/*
 * set up as a singleton, but also instantiated by CCExport.
 * The purpose of the singleton setup is just to get the dependencies.
 * So they are all declared static.
 */
@Slf4j
public class Assignment2Export extends AssignmentExport {
    private static SimplePageToolDao simplePageToolDao;

    public void setSimplePageToolDao(Object dao) {
	simplePageToolDao = (SimplePageToolDao) dao;
    }

    static MessageLocator messageLocator = null;
    public void setMessageLocator(MessageLocator m) {
	messageLocator = m;
    }

    static AssignmentExport next = null;
    public void setNext(AssignmentExport n) {
	next = n;
    }

    public void setPrev(AssignmentExport p) {
	p.setNext(this);
    }

    static boolean haveA2 = false;

    public void init () {

	if (ComponentManager.get("org.sakaiproject.assignment2.service.api.Assignment2Service") != null)
	    haveA2 = true;
	log.info("Assignment2Export init: haveA2 = " + haveA2);

	log.info("init()");

    }

    public void destroy()
    {
	log.info("destroy()");
    }

    public List<AssignmentItem> getItemsInSite(String siteId) {

	if (!haveA2) {
	    if (next != null) 
		return next.getItemsInSite(siteId);
	    else
		return new ArrayList<AssignmentItem>();
	}

	List<AssignmentItem> ret = new ArrayList<AssignmentItem>();
	String siteRef = "/group/" + siteId + "/";

	Connection connection = null;
	try {
	    connection = SqlService.borrowConnection();
	    String sql="select INSTRUCTIONS, ASSIGNMENT_ID from A2_ASSIGNMENT_T where CONTEXT = ? and DRAFT=0 and REMOVED=0";
	    Object fields[] = new Object[1];
	    fields[0] = siteId;
	    
	    ret = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    AssignmentItem item = new AssignmentItem();
			    item.id = LessonEntity.ASSIGNMENT2 + "/" + result.getString(2);
			    item.instructions = result.getString(1);
			    item.attachments = new ArrayList<String>();
			    return item;
			} catch (Exception e) {
			    return null;
			}
		    }
		});
	    
	    sql="select ATTACHMENT_REFERENCE from A2_ASSIGN_ATTACH_T where ASSIGNMENT_ID = ?";

	    for (AssignmentItem item: ret) {

		fields[0] = new Long(item.id.substring(LessonEntity.ASSIGNMENT2.length() + 1));

		item.attachments = SqlService.dbRead(connection, sql, fields, null);

	    }

	} catch (Exception e) {
	    // leave ret as null list
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}	

	if (next != null) {
	    List<AssignmentItem>nextList = next.getItemsInSite(siteId);
	    if (nextList != null)
		ret.addAll(nextList);
	}

	return ret;
    }

    public AssignmentItem getContents(String assignmentRef) {
	if (!assignmentRef.startsWith(LessonEntity.ASSIGNMENT2 + "/")) {
	    if (next == null)
		return null;
	    else 
		return next.getContents(assignmentRef);
	}

	AssignmentItem ret = null;

	int i = assignmentRef.indexOf("/");
	String assignmentId = assignmentRef.substring(i+1);
	Connection connection = null;

	try {
	    connection = SqlService.borrowConnection();
	    String sql="select INSTRUCTIONS, DUE_DATE, GRADEBOOK_ITEM_ID, TITLE, ASSIGNMENT_ID, REQUIRES_SUBMISSION, SUBMISSION_TYPE, GRADED from A2_ASSIGNMENT_T where ASSIGNMENT_ID = ?";
	    Object fields[] = new Object[1];
	    fields[0] = new Long(assignmentId);
	    List<AssignmentItem>items = SqlService.dbRead(connection, sql, fields, new SqlReader()
		{
		    public Object readSqlResultRecord(ResultSet result)
		    {
			try {
			    AssignmentItem item = new AssignmentItem();
			    item.id = result.getString(5);
			    item.title = result.getString(4);
			    item.instructions = result.getString(1);
			    item.attachments = new ArrayList<String>();
			    int requiressubmit = result.getInt(7);
			    int submittype = result.getInt(7);
			    item.allowfile = false;
			    item.allowtext = false;
			    if (requiressubmit != 0) {
				if (submittype == 1 || submittype == 2)
				    item.allowfile = true;
				if (submittype == 0 || submittype == 2)
				    item.allowtext = true;
			    }
			    item.gradable = (result.getInt(8) != 0);
			    // a2 doesn't keep the grading scale. It uses the gradebook. But on import we don't set
			    // up the gradebook entry. For constency we say it's gradeable but don't give a point value.
			    item.forpoints = false;
			    item.maxpoints = 0.0;
			    return item;
			} catch (Exception e) {
			    return null;
			}
		    }
		});

	    if (items == null || items.size() == 0)
		return null;

	    ret = items.get(0);

	    fields[0] = new Long(ret.id);

	    sql="select ATTACHMENT_REFERENCE from A2_ASSIGN_ATTACH_T where ASSIGNMENT_ID = ?";

	    ret.attachments = SqlService.dbRead(connection, sql, fields, null);

	} catch (Exception e) {
	    log.info("error reading assignment2 " + e);
	    // leave ret as null list
	} finally {
	    try {
		if (connection != null)
		    SqlService.returnConnection(connection);
	    } catch (Exception ignore) {};
	}	
	
	return ret;
    }

    // use outputEntity from super

}
