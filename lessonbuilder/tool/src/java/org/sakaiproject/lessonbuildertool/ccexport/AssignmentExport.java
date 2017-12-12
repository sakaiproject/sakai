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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringEscapeUtils;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.assignment.api.model.Assignment;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.util.Validator;

/*
 * set up as a singleton, but CCExport is not.
 */
@Slf4j
public class AssignmentExport {
    private static SimplePageToolDao simplePageToolDao;

    private AssignmentService assignmentService;

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

    public void init () {
		assignmentService = ComponentManager.get(AssignmentService.class);

	log.info("init()");

    }

    public void destroy()
    {
	log.info("destroy()");
    }

    public List<AssignmentItem> getItemsInSite(String siteId) {
	List<AssignmentItem> ret = new ArrayList<AssignmentItem>();

	for (Assignment assignment : assignmentService.getAssignmentsForContext(siteId)) {
	    if (!assignment.getDraft()) {
		Set<String> attachments = assignment.getAttachments();
		String instructions = assignment.getInstructions();

		AssignmentItem item = new AssignmentItem();
		item.id = LessonEntity.ASSIGNMENT + "/" + assignment.getId();
		item.instructions = instructions;
		item.attachments = new ArrayList<>();
		for (String ref: attachments) {
		    item.attachments.add(ref);
		}
		ret.add(item);
	    }
	}

	if (next != null) {
	    List<AssignmentItem>nextList = next.getItemsInSite(siteId);
	    if (nextList != null)
		ret.addAll(nextList);
	}
	
	return ret;
    }


    // find topics in site, but organized by forum
    public List<String> getEntitiesInSite(String siteId, CCExport bean) {

	List<String> ret = new ArrayList<String>();
	String siteRef = "/group/" + siteId + "/";

	List<AssignmentItem>items = getItemsInSite(siteId);

	for (AssignmentItem item: items) {

	    String instructions = item.instructions;
	    List<String>attachments = item.attachments;
		
	    // special case. one attachment and nothing else.
	    // just export the attachment.
	    // removed this code. Interaction with version 1.3 has made this
	    // too complex to test and maintain.

	    ret.add(item.id);

	    // arrange to include the attachments
	    for (String sakaiId: attachments) {

		if (sakaiId.startsWith("/content/"))
		    sakaiId = sakaiId.substring("/content".length());
		
		String url = null;
		// if it is a URL, need the URL rather than copying the file
		try {
		    ContentResource resource = ContentHostingService.getResource(sakaiId);
		    if (CCExport.islink(resource)) {
			url = new String(resource.getContent());
		    }
		} catch (Exception e) {
		}
		    
		// if attachment isn't a file in resources, arrange for it to be included
		if (url != null)
		    ;  // if it's a URL we don't need a file
		else if (! sakaiId.startsWith(siteRef)) {  // if in resources, already included
		    String assignmentId = item.id;
		    int i = assignmentId.indexOf("/");
		    assignmentId = assignmentId.substring(i+1);
		    int lastSlash = sakaiId.lastIndexOf("/");
		    String lastAtom = sakaiId.substring(lastSlash + 1);
		    bean.addFile(sakaiId, "attachments/" + assignmentId + "/" + lastAtom, null);
		}
	    }
	}

	return ret;
    }

    public static class AssignmentItem {
	public String id;
	public String title;
	public String instructions;
	public List<String> attachments;
	boolean gradable;
	boolean forpoints;
	double maxpoints;
	boolean allowtext;
	boolean allowfile;
    }


    public AssignmentItem getContents(String assignmentRef) {

	if (!assignmentRef.startsWith(LessonEntity.ASSIGNMENT + "/")) {
	    if (next == null)
		return null;
	    else 
		return next.getContents(assignmentRef);
	}

	AssignmentItem ret = new AssignmentItem();
	ret.attachments = new ArrayList<>();

	int i = assignmentRef.indexOf("/");
	String assignmentId = assignmentRef.substring(i+1);
	ret.id = assignmentId;

	Assignment assignment = null;

	try {
	    assignment = assignmentService.getAssignment(assignmentId);
	} catch (Exception e) {
	    log.info("failed to find " + assignmentId);
	    return null;
	}

	ret.title = assignment.getTitle();
	
	ret.instructions = assignment.getInstructions();
	
	ret.maxpoints = 0.0;
	ret.gradable = false;
	ret.forpoints = false;

	Assignment.GradeType typeOfGrade = assignment.getTypeOfGrade();
	// in Sakai only point-based goes to gradebook.
	// in CC we have gradeable with optional point value
	// I've chosen to specify a point value only for question with a point value

	switch (typeOfGrade) {
		case SCORE_GRADE_TYPE: ret.maxpoints = assignment.getMaxGradePoint() / 10.0;
			ret.forpoints = true;
		case LETTER_GRADE_TYPE:
		case PASS_FAIL_GRADE_TYPE:
		case CHECK_GRADE_TYPE: ret.gradable = true;
	}

	ret.allowtext = false;
	ret.allowfile = false;

	Assignment.SubmissionType typeOfSubmission = assignment.getTypeOfSubmission();
	switch (typeOfSubmission) {
		case TEXT_ONLY_ASSIGNMENT_SUBMISSION: ret.allowtext = true; break;
		case ATTACHMENT_ONLY_ASSIGNMENT_SUBMISSION: ret.allowfile = true; break;
		case TEXT_AND_ATTACHMENT_ASSIGNMENT_SUBMISSION: ret.allowtext = true; ret.allowfile = true; break;
	}

	Set<String> attachments = assignment.getAttachments();

	for (String ref: attachments) {
	    ret.attachments.add(ref);
	}
	
	return ret;
    }


    // this is weird, because it's a web content, not a learning application. So we need to produce an HTML file
    // with instructions and relative references to any attachments

    public boolean outputEntity(String assignmentRef, ZipPrintStream out, PrintStream errStream, CCExport bean, CCExport.Resource resource) {

	AssignmentItem contents = getContents(assignmentRef);
	if (contents == null)
	    return false;
	
	String title = contents.title;
	String instructions = bean.relFixup(contents.instructions, resource);

	List<String>attachments = contents.attachments;

	out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
	out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
	out.println("<body>");
	if (instructions != null && !instructions.trim().equals("")) {
	    out.println("<div>");
	    out.println(instructions);
	    out.println("</div> ");
	}


	out.println(outputAttachments(resource, attachments, bean, "../../"));

	out.println("</body>");
	out.println("</html>");

	return true;
   }

    static String outputAttachments(CCExport.Resource resource, List<String>attachments, CCExport bean, String prefix) {

	StringBuilder out = new StringBuilder();

	if (attachments.size() > 0) {
	    out.append("<ul>\n");

	    for (String sakaiId: attachments) {
		if (sakaiId.startsWith("/content/"))
		    sakaiId = sakaiId.substring("/content".length());
		
		String URL = null;
		// if it is a URL, need the URL rather than copying the file
		if (!sakaiId.startsWith("///")) {
		    try {
			ContentResource res = ContentHostingService.getResource(sakaiId);
			if (CCExport.islink(res)) {
			    URL = new String(res.getContent());
			}
		    } catch (Exception e) {
		    }
		}
		
		String location = bean.getLocation(sakaiId);
		int lastSlash = sakaiId.lastIndexOf("/");
		String lastAtom = sakaiId.substring(lastSlash + 1);
		
		// assumption here is that if the user entered a URL, it's in valid syntax
		// if we generate it from file location, it needs to be escaped
		if (URL != null) {
		    out.append("<li><a href=\"" + URL + "\">" + StringEscapeUtils.escapeHtml(URL) + "</a>\n");
		} else {
		    URL = prefix + Validator.escapeUrl(location);  // else it's in the normal site content
		    URL = URL.replaceAll("//", "/");
		    out.append("<li><a href=\"" + URL + "\">" + StringEscapeUtils.escapeHtml(lastAtom) + "</a><br/>\n");
		    bean.addDependency(resource, sakaiId);
		}
	    }
	    out.append("</ul>\n");
	}

	return out.toString();

    }

    public boolean outputEntity2(String assignmentRef, ZipPrintStream out, PrintStream errStream, CCExport bean, CCExport.Resource resource) {

	AssignmentItem contents = getContents(assignmentRef);
	if (contents == null)
	    return false;
	
	String title = contents.title;

	// relFixup is for stuff that's in an actual HTML file, fixup for stuff in an XML descriptor
	String instructions = bean.fixup(contents.instructions, resource);
	List<String>attachments = contents.attachments;

	// the spec doesn't allow URLs in attachments, so if any of our attachments are URLs,
	// put the attachments as a list inside the instructions
	boolean useAttachments = (attachments.size() > 0);
	for (String sakaiId: attachments) {
	    if (sakaiId.startsWith("/content/"))
		sakaiId = sakaiId.substring("/content".length());

	    String URL = null;
	    // if it is a URL, need the URL rather than copying the file
	    try {
		ContentResource res = ContentHostingService.getResource(sakaiId);
		if (CCExport.islink(res)) {
		    useAttachments = false;
		    break;
		}
	    } catch (Exception e) {
	    }
	}

	out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
	out.println("<assignment xmlns=\"http://www.imsglobal.org/xsd/imscc_extensions/assignment\"");
        out.println("     xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
        out.println("     xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imscc_extensions/assignment http://www.imsglobal.org/profile/cc/cc_extensions/cc_extresource_assignmentv1p0_v1p0.xsd\"");
        out.println("  identifier=\"AssigmentId\">");

	if (title == null || title.length() == 0)
	    title = "Assignment";
	out.println("  <title>" + StringEscapeUtils.escapeXml(title) + "</title>");
	if (useAttachments || attachments.size() == 0)
	    out.println("  <text texttype=\"text/html\">" + instructions + "</text>");
	else
	    out.println("  <text texttype=\"text/html\">" + StringEscapeUtils.escapeXml("<div>") + instructions + StringEscapeUtils.escapeXml(outputAttachments(resource, attachments, bean, "$IMS-CC-FILEBASE$../") + "</div>") + "</text>");
	
	// spec requires an instructor text even though we don't normally have one.
	out.println("<instructor_text texttype=\"text/plain\"></instructor_text>");
	out.println("<gradable" + (contents.forpoints ? (" points_possible=\"" + contents.maxpoints + "\"") : "") + ">" + 
		    contents.gradable + "</gradable>");

	if (useAttachments) {
	    out.println("  <attachments>");

	    for (String sakaiId: attachments) {
		if (sakaiId.startsWith("/content/"))
		    sakaiId = sakaiId.substring("/content".length());

		String URL = null;
		// if it is a URL, need the URL rather than copying the file
		try {
		    ContentResource res = ContentHostingService.getResource(sakaiId);
		    if (CCExport.islink(res)) {
			URL = new String(res.getContent());
		    }
		} catch (Exception e) {
		}

		String location = bean.getLocation(sakaiId);
		int lastSlash = sakaiId.lastIndexOf("/");
		String lastAtom = sakaiId.substring(lastSlash + 1);

		if (URL != null) {
		    out.println("    <attachment href=\"" + StringEscapeUtils.escapeXml(URL) + "\" role=\"All\" />");
		} else {
		    URL = "../" + location;  // else it's in the normal site content
		    URL = URL.replaceAll("//", "/");
		    out.println("    <attachment href=\"" + StringEscapeUtils.escapeXml(URL) + "\" role=\"All\" />");
		    bean.addDependency(resource, sakaiId);
		}
	    }
	    out.println("  </attachments>");
	}
	out.println("  <submission_formats>");
	// our text input is HTML
	if (contents.allowtext)
	    out.println("    <format  type=\"html\" />");
	// file input allows both file and URL
	if (contents.allowfile) {
	    out.println("    <format  type=\"file\" />");
	    out.println("    <format  type=\"url\" />");
	}
	out.println("  </submission_formats>");

	out.println("</assignment>");

	return true;
   }

}
