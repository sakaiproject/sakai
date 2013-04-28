package org.sakaiproject.lessonbuildertool.ccexport;;

/***********
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
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.component.cover.ServerConfigurationService;

import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.ccexport.SamigoExport;
import org.sakaiproject.lessonbuildertool.ccexport.AssignmentExport;
import org.sakaiproject.lessonbuildertool.ccexport.ZipPrintStream;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.api.ToolSession;
import uk.org.ponder.messageutil.MessageLocator;

public class CCExport {

    private File root;
    private String rootPath;
    long nextid = 1;
  
    static ContentHostingService contentHostingService;
    public void setContentHostingService(ContentHostingService chs) {
	contentHostingService = chs;
    }
    static SamigoExport samigoExport;
    public void setSamigoExport(SamigoExport se) {
	samigoExport = se;
    }
    static AssignmentExport assignmentExport;
    public void setAssignmentExport(AssignmentExport se) {
	assignmentExport = se;
    }

    static MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator x) {
	messageLocator = x;
    }
    static SimplePageToolDao simplePageToolDao;
    public void setSimplePageToolDao(Object dao) {
	simplePageToolDao = (SimplePageToolDao) dao;
    }

    HttpServletResponse response;
    File errFile = null;
    PrintStream errStream = null;
    String siteId = null;
    static String server = ServerConfigurationService.getServerName();

    class Resource {
	String sakaiId;
	String resourceId;
	String location;
	List<String> dependencies;
    }

    // map of all file resource to be included in cartridge
    Map<String, Resource> fileMap = new HashMap<String, Resource>();
    // map of all Samigo tests
    Map<String, Resource> samigoMap = new HashMap<String, Resource>();
    // map of all Assignments
    Map<String, Resource> assignmentMap = new HashMap<String, Resource>();


    // the error messages are a problem. They won't show until the next page display
    // however errrors at this level are unusual, and we interrupt the download, so the
    // user should never see an incomplete one. Most common errors have to do with
    // problems converting for CC format. Those go into a log file that's included in
    // the ZIP, so the user will see those errors (if he knows the look)

    public static void setErrMessage(String s) {
	ToolSession toolSession = SessionManager.getCurrentToolSession();
	if (toolSession == null) {
	    System.out.println("Lesson Builder error not in tool: " + s);
	    return;
	}
	List<String> errors = (List<String>)toolSession.getAttribute("lessonbuilder.errors");
	if (errors == null)
	    errors = new ArrayList<String>();
	errors.add(s);
	toolSession.setAttribute("lessonbuilder.errors", errors);
    }

    public static void setErrKey(String key, String text ) {
	if (text == null)
	    text = "";
	setErrMessage(messageLocator.getMessage(key).replace("{}", text));
    }

    /*
     * maintain global lists of resources, adding as they are referenced on a page or
     * adding all resources of a kind, depending. Each type of resource has a map
     * indexed by sakai ID, with a generated ID for the cartridge and the name of the
     * file or XML file.
     *
     * the overall flow will be to load all resources and tests into the temp directory
     * and the maps, the walk the lesson hierarchy building imsmanifest.xml. Any resources
     * not used will get some kind of dummy entries in imsmanifest.xml, so that the whole
     * contents of the site is brought over.
     */

    public void doExport(String sid, HttpServletResponse httpServletResponse) {
	response = httpServletResponse;
	siteId = sid;

	if (! startExport())
	    return;
	if (! addAllFiles(siteId))
	    return;
	if (! addAllSamigo(siteId))
	    return;
	if (! addAllAssignments(siteId))
	    return;
	download();

    }

    /*
     * create temp dir and start writing 
     */
    public boolean startExport() {
	try {
	    root = File.createTempFile("ccexport", "root");
	    if (root.exists())
		root.delete();
	    root.mkdir();
	    errFile = new File(root, "export-errors");
	    errStream = new PrintStream(errFile);
	    
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}
	return true;
    }

    String getResourceId () {
       	return "res" + (nextid++);
    }

    String getLocation(String sakaiId) {
	Resource ref = fileMap.get(sakaiId);
	if (ref == null)
	    return null;
	return ref.location;
    }

    public void addFile(String sakaiId, String location) {
	Resource res = new Resource();
	res.sakaiId = sakaiId;
	res.resourceId = getResourceId();
	res.location = location;
	res.dependencies = new ArrayList<String>();

	fileMap.put(sakaiId, res);

    }

    public boolean addAllFiles(String siteId) {
	try {
	    String base = contentHostingService.getSiteCollection(siteId);
	    ContentCollection baseCol = contentHostingService.getCollection(base);
	    return addAllFiles(baseCol, base.length());
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

    }

    public boolean addAllFiles(ContentCollection baseCol, int baselen) {
	try {

	    List<ContentEntity> members = baseCol.getMemberResources();
	    for (ContentEntity e: members) {
		if (e instanceof ContentResource)
		    addFile(e.getId(), e.getId().substring(baselen));
		else
		    addAllFiles((ContentCollection)e, baselen);
	    }
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}
	return true;
    }

    public boolean outputAllFiles (ZipPrintStream out) {
	try {
	    for (Map.Entry<String, Resource> entry: fileMap.entrySet()) {

		ZipEntry zipEntry = new ZipEntry(entry.getValue().location);

		ContentResource resource = contentHostingService.getResource(entry.getKey());

		zipEntry.setSize(resource.getContentLength());
		out.putNextEntry(zipEntry);
		InputStream contentStream = null;
		try {
		    contentStream = resource.streamContent();
		    IOUtils.copy(contentStream, out);
		} catch (Exception e) {
		} finally {
		    if (contentStream != null) {
			contentStream.close();
		    }
		}
	    }
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;

    }

    public boolean addAllSamigo(String siteId) {
	List<String> tests = samigoExport.getEntitiesInSite(siteId);
	if (tests == null)
	    return true;
	// These are going to be loaded into the final file system. I considered
	// putting them in a separate directory to avoid conflicting with real files.
	// However this would force all URLs to be written with ../ at the start,
	// which is probably more dangerous, as it depends upon loaders making the
	// same interpretation of a somewhat ambiguous specification.
	for (String sakaiId: tests) {
	    Resource res = new Resource();
	    res.resourceId = getResourceId();
	    res.location = "cc-objects/" + res.resourceId + ".xml";
	    res.sakaiId = sakaiId;
	    res.dependencies = new ArrayList<String>();
	    samigoMap.put(res.sakaiId, res);
	}

	return true;
    }

    public boolean outputAllSamigo(ZipPrintStream out) {
	try {
	    for (Map.Entry<String, Resource> entry: samigoMap.entrySet()) {

		ZipEntry zipEntry = new ZipEntry(entry.getValue().location);

		out.putNextEntry(zipEntry);
		boolean ok = samigoExport.outputEntity(entry.getValue().sakaiId, out, errStream, this, entry.getValue());
		if (!ok)
		    return false;

	    }
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;

    }

    public boolean addAllAssignments(String siteId) {
	List<String> assignments = assignmentExport.getEntitiesInSite(siteId, this);
	if (assignments == null)
	    return true;
	for (String sakaiId: assignments) {
	    Resource res = new Resource();
	    res.resourceId = getResourceId();
	    int slash = sakaiId.indexOf("/");
	    res.location = "attachments/" + sakaiId.substring(slash+1) + "/assignmentpage.html";
	    res.sakaiId = sakaiId;
	    res.dependencies = new ArrayList<String>();
	    assignmentMap.put(res.sakaiId, res);
	}

	return true;
    }

    public boolean outputAllAssignments(ZipPrintStream out) {
	try {
	    for (Map.Entry<String, Resource> entry: assignmentMap.entrySet()) {

		ZipEntry zipEntry = new ZipEntry(entry.getValue().location);

		out.putNextEntry(zipEntry);
		boolean ok = assignmentExport.outputEntity(entry.getValue().sakaiId, out, errStream, this, entry.getValue());
		if (!ok)
		    return false;

	    }
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;

    }

    public boolean outputManifest(ZipPrintStream out) {
	try {
	    ZipEntry zipEntry = new ZipEntry("imsmanifest.xml");
	    out.putNextEntry(zipEntry);
	    out.print(
		      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<manifest identifier=\"sakai1\"\n  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1\"\nxmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource\"\nxmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxsi:schemaLocation=\"                                                                                                                        \n  http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p2/LOM/ccv1p2_lomresource_v1p0.xsd                  \n  http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imscp_v1p2_v1p0.xsd                     \n  http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p2/LOM/ccv1p2_lommanifest_v1p0.xsd\">\n  <metadata>\n    <schema>IMS Common Cartridge</schema>\n    <schemaversion>1.2.0</schemaversion>\n    <lomimscc:lom>\n      <lomimscc:general>\n	<lomimscc:title>\n	  <lomimscc:string language=\"en-US\">Sakai Export</lomimscc:string>\n	</lomimscc:title>\n	<lomimscc:description>\n	  <lomimscc:string language=\"en-US\">Sakai Export, including only files from site</lomimscc:string>\n	</lomimscc:description>\n	<lomimscc:keyword>\n	  <lomimscc:string language=\"en-US\">Export</lomimscc:string>\n	</lomimscc:keyword>\n      </lomimscc:general>\n    </lomimscc:lom>\n  </metadata>\n ");

	    out.println("  <organizations>");

	    if (false) {
	    out.println("  <organization identifier=\"page\" structure=\"rooted-hierarchy\">");
	    out.println("    <item identifier=\"I_1\">");
	    out.println("      <item identifer=\"I_1_1\">");
	    out.println("        <title>Dummy page</title>");
	    int n = 0;
	    for (Map.Entry<String, Resource> entry: samigoMap.entrySet()) {
		out.println("        <item idenitifer=\"I_I_1_" + n + "\" identifierref=\"" + entry.getValue().resourceId + "\">");
		out.println("          <title>test " + n + "</title>");
		out.println("        </item>");
	    }
	    out.println("      </item>");
	    out.println("    </item>");
	    out.println("  </organization>");
	    }

	    out.println("  </organizations>");
	    out.println("  <resources>");
	    for (Map.Entry<String, Resource> entry: fileMap.entrySet()) {
		out.print(("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + 
			   "\" type=\"webcontent\">\n      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>\n    </resource>\n"));
	    }

	    for (Map.Entry<String, Resource> entry: samigoMap.entrySet()) {
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"imsqti_xmlv1p2/imscc_xmlv1p2/assessment\">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    for (Map.Entry<String, Resource> entry: assignmentMap.entrySet()) {
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"webcontent\" intendeduse=\"assignment\">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    // add error log at the very end
	    String errId = getResourceId();

	    out.println(("    <resource href=\"cc-objects/export-errors\" identifier=\"" + errId + 
			   "\" type=\"webcontent\">\n      <file href=\"cc-objects/export-errors\"/>\n    </resource>"));
	    
	    out.println("  </resources>\n</manifest>");

	    errStream.close();
	    zipEntry = new ZipEntry("cc-objects/export-errors");
	    out.putNextEntry(zipEntry);
	    InputStream contentStream = null;
	    try {
		contentStream = new FileInputStream(errFile);
		IOUtils.copy(contentStream, out);
	    } finally {
		if (contentStream != null) {
		    contentStream.close();
		}
	    }
	} catch (Exception e) {
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;


    }

    public boolean download() {

        OutputStream htmlOut = null;
	ZipPrintStream out = null;
        try {
	    htmlOut = response.getOutputStream();
	    out = new ZipPrintStream(htmlOut);

	    response.setHeader("Content-disposition", "inline; filename=sakai-export.imscc");
	    response.setContentType("application/zip");
	    
	    outputAllFiles (out);
	    outputAllSamigo (out);
	    outputAllAssignments (out);
	    outputManifest (out);
	    
	    if (out != null)
		out.close();

        } catch (Exception ioe) {
	    if (out != null) {
		try {
		    out.close();
		} catch (Exception ignore) {
		}
	    }
	    setErrKey("simplepage.exportcc-fileerr", ioe.getMessage());
	    return false;
	}

	return true;

    }

    public void addDependency(Resource resource, String sakaiId) {
	Resource ref = fileMap.get(sakaiId);
	if (ref != null)
	    resource.dependencies.add(ref.resourceId);
    }

    public String fixup (String s, Resource resource) {
	// http://lessonbuilder.sakaiproject.org/53605/
	StringBuilder ret = new StringBuilder();
	String sakaiIdBase = "/group/" + siteId;
	Pattern target = Pattern.compile("/access/content/group/" + siteId + "|http://lessonbuilder.sakaiproject.org/", Pattern. CASE_INSENSITIVE);
	Matcher matcher = target.matcher(s);
	// technically / isn't allowed in an unquoted attribute, but sometimes people
	// use sloppy HTML	
	Pattern wordend = Pattern.compile("[^-a-zA-Z0-9._:/]");
	int index = 0;
	while (true) {
	    if (!matcher.find()) {
		ret.append(s.substring(index));
		break;
	    }		
	    String sakaiId = null;
	    int start = matcher.start();
	    if (s.regionMatches(false, start, "/access", 0, 7)) { // matched /access/content...
		int sakaistart = start + "/access/content".length(); //start of sakaiid, can't find end until we figure out quoting
		int last = start + "/access/content/group/".length() + siteId.length();
		if (s.regionMatches(true, (start - server.length()), server, 0, server.length())) {    // servername before it
		    start -= server.length();
		    if (s.regionMatches(true, start - 7, "http://", 0, 7)) {   // http:// or https:// before that
			start -= 7;
		    } else if (s.regionMatches(true, start - 8, "https://", 0, 8)) {
			start -= 8;
		    }
		}
		// need to find sakaiend. To do that we need to find the close quote
		int sakaiend = 0;
		char quote = s.charAt(start-1);
		if (quote == '\'' || quote == '"')  // quoted, this is easy
		    sakaiend = s.indexOf(quote, sakaistart);
		else { // not quoted. find first char not legal in unquoted attribute
		    Matcher wordendMatch = wordend.matcher(s);
		    if (wordendMatch.find(sakaistart)) {
			sakaiend = wordendMatch.start();
		    }
		    else
			sakaiend = s.length();
		}
		sakaiId = s.substring(sakaistart, sakaiend);
		ret.append(s.substring(index, start));
		ret.append("$IMS-CC-FILEBASE$..");
		index = last;  // start here next time
	    } else { // matched http://lessonbuilder.sakaiproject.org/
		int last = matcher.end(); // should be start of an integer
		int endnum = s.length();  // end of the integer
		for (int i = last; i < s.length(); i++) {
		    if ("0123456789".indexOf(s.charAt(i)) < 0) {
			endnum = i;
			break;
		    }
		}
		String numString = s.substring(last, endnum);
		if (numString.length() >= 1) {
		    Long itemId = new Long(numString);
		    SimplePageItem item = simplePageToolDao.findItem(itemId);
		    sakaiId = item.getSakaiId();
		    int itemType = item.getType();
		    if ((itemType == SimplePageItem.RESOURCE || itemType == SimplePageItem.MULTIMEDIA) && 
			sakaiId.startsWith(sakaiIdBase)) {
			ret.append(s.substring(index, start));
			ret.append("$IMS-CC-FILEBASE$.." + sakaiId.substring(sakaiIdBase.length()));
			index = endnum;
		    }
		}
	    }
	    if (sakaiId != null) {
		Resource r = fileMap.get(sakaiId);
		if (r != null) {
		    resource.dependencies.add(r.resourceId);
		}
	    }
	}
	return StringEscapeUtils.escapeXml(ret.toString());
    }		

}
