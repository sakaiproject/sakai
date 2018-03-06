/**
 * Copyright (c) 2003-2016 The Apereo Foundation
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
package org.sakaiproject.lessonbuildertool.ccexport;

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

import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;

import uk.org.ponder.messageutil.MessageLocator;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.view.ExportCCViewParameters;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;

@Slf4j
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
    static ForumsExport forumsExport;
    public void setForumsExport(ForumsExport se) {
	forumsExport = se;
    }
    static BltiExport bltiExport;
    public void setBltiExport(BltiExport se) {
      bltiExport = se;
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
    int version = V12;
    boolean doBank = false;

    class Resource {
	String sakaiId;
	String resourceId;
	String location;
	String use;
	String title;
	String url;
	boolean islink;
	boolean isbank;
	Set<String> dependencies;
    }

    // map of all file resource to be included in cartridge
    Map<String, Resource> fileMap = new HashMap<String, Resource>();
    // map of all Samigo tests
    Map<String, Resource> samigoMap = new HashMap<String, Resource>();
    Map<Long, Resource> poolMap = new HashMap<Long, Resource>();
    Resource samigoBank = null;
    // map of all Assignments
    Map<String, Resource> assignmentMap = new HashMap<String, Resource>();
    // map of all Forums
    Map<String, Resource> forumsMap = new HashMap<String, Resource>();
    // map of all Blti instances
    Map<String, Resource> bltiMap = new HashMap();
 
    // to prevent pages from being output more than once
    Set<Long> pagesDone = new HashSet();

    // list of item ID's that use embed code. Need to output
    // an HTML page with the embed code. The string is the embed code,
    // with fixups done
    Map<Long, String> embedMap = new HashMap<Long,String>();
    // itemID's that are links. Need to output the link XML file
    Set<Resource> linkSet = new HashSet<Resource>();

    // the error messages are a problem. They won't show until the next page display
    // however errrors at this level are unusual, and we interrupt the download, so the
    // user should never see an incomplete one. Most common errors have to do with
    // problems converting for CC format. Those go into a log file that's included in
    // the ZIP, so the user will see those errors (if he knows the look)

    public static void setErrMessage(String s) {
	ToolSession toolSession = SessionManager.getCurrentToolSession();
	if (toolSession == null) {
	    log.error("Lesson Builder error not in tool: " + s);
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

    // current we don't support 1.0
    public static final int V11 = 1;
    public static final int V12 = 2;
    public static final int V13 = 3;

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

    public void doExport(String sid, HttpServletResponse httpServletResponse, ExportCCViewParameters params) {
	response = httpServletResponse;
	siteId = sid;
	if ("1.1".equals(params.getVersion()))
	    version = V11;
	else if ("1.3".equals(params.getVersion()))
	    version = V13;
	if ("1".equals(params.getBank()))
	    doBank = true;

	if (! startExport())
	    return;
	if (! addAllFiles(siteId))
	    return;
	if (! addAllSamigo(siteId))
	    return;
	if (! addAllAssignments(siteId))
	    return;
	if (! addAllForums(siteId))
	    return;
	if (!addAllBlti(siteId))
	    return;
	download();

    }

    /*
     * create temp dir and start writing 
     */
    public boolean startExport() {
	try {
	    root = File.createTempFile("ccexport", "root");
	    if (root.exists()) {
		if (!root.delete())
		    throw new IOException("unabled to delete old temp file for export");
	    }
	    if (!root.mkdir())
		throw new IOException("unable to make directory for export");		
	    errFile = new File(root, "export-errors");
	    errStream = new PrintStream(errFile);
	    
	} catch (Exception e) {
	    log.error("Lessons export error outputting file, startExport " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}
	return true;
    }

    String getResourceId () {
       	return "res" + (nextid++);
    }

    String getResourceIdPeek () {
       	return "res" + nextid;
    }

    public void setIntendeduse (String sakaiId, String intendeduse) {
	Resource ref = fileMap.get(sakaiId);
	if (ref == null)
	    return;
	ref.use = intendeduse;
    }

    String getLocation(String sakaiId) {
	Resource ref = fileMap.get(sakaiId);
	if (ref == null)
	    return null;
	return ref.location;
    }

    public Resource addFile(String sakaiId, String location) {
	return addFile(sakaiId, location, null);
    }

    public Resource addFile(String sakaiId, String location, String use) {
	Resource res = new Resource();
	res.sakaiId = sakaiId;
	res.resourceId = getResourceId();
	res.location = location;
	res.dependencies = new HashSet<String>();
	res.use = use;
	res.islink = false;
	res.isbank = false;
	fileMap.put(sakaiId, res);
	return res;
    }

    public boolean addAllFiles(String siteId) {
	try {
	    String base = contentHostingService.getSiteCollection(siteId);
	    ContentCollection baseCol = contentHostingService.getCollection(base);
	    return addAllFiles(baseCol, base.length());
	} catch (org.sakaiproject.exception.IdUnusedException e) {
	    setErrKey("simplepage.exportcc-noresource", e.getMessage());
	    return false;
	} catch (Exception e) {
	    log.error("Lessons export error outputting file, addAllFiles " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

    }

    public static boolean islink(ContentResource r) {
	return r.getResourceType().equals("org.sakaiproject.content.types.urlResource") ||
	    r.getContentType().equals("text/url");
    }


    public boolean addAllFiles(ContentCollection baseCol, int baselen) {
	try {

	    List<ContentEntity> members = baseCol.getMemberResources();
	    for (ContentEntity e: members) {

		// don't export things we generate. Can lead to collisions
		String filename = e.getId().substring(baselen);
		if (filename.equals("cc-objects/export-errors") || filename.equals("cc-objects"))
		    continue;

		if (e instanceof ContentResource) {
		    boolean islink = islink((ContentResource)e);
		    String location = null;
		    if (islink) {
			location = "attachments/" + getResourceIdPeek() + ".xml";
			String url = new String(((ContentResource)e).getContent());
			// see if Youtube. If so, use the current recommended URL
			String youtubeKey = SimplePageBean.getYoutubeKeyFromUrl(url);
			if (youtubeKey != null)
			    // code is also in ShowPageProducer. keep in sync
			    url = SimplePageBean.getYoutubeUrlFromKey(youtubeKey);
			Resource res = addFile(e.getId(), location);
			res.islink = true;
			res.url = url;
			// try to get title from resource. Will normally be the URL
			res.title = ((ContentResource)e).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
			if (res.title == null)
			    res.title = url;
			// queue this so we output the XML file
			linkSet.add(res);
		    } else {
			location = e.getId().substring(baselen);
			Resource res = addFile(e.getId(), location);
		    }
		} else 
		    addAllFiles((ContentCollection)e, baselen);
	    }
	} catch (Exception e) {
	    log.error("Lessons export error outputting file, addAllFiles 2 " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}
	return true;
    }

    public boolean outputAllFiles (ZipPrintStream out) {
	try {
	    for (Map.Entry<String, Resource> entry: fileMap.entrySet()) {

		// normally this is a file ID for contenthosting.
		// But jforum gives us an actual filesystem filename. We stick /// on
		// the front to make that clear. inSakai is contenthosting.
		boolean inSakai = !entry.getKey().startsWith("///"); 

		ZipEntry zipEntry = new ZipEntry(entry.getValue().location);

		// for contenthosting
		ContentResource resource = null;
		// for raw file
		File infile = null;
		InputStream instream = null;
		if (inSakai) {
		    resource = contentHostingService.getResource(entry.getKey());
		    // if URL there's no file to output. The link XML file will
		    // be done at the end, since some links are discovered while outputting manifest
		    if (((Resource)entry.getValue()).islink) {
			continue;
		    } else
			zipEntry.setSize(resource.getContentLength());
		} else {
		    infile = new File(entry.getKey().substring(3));
		    instream = new FileInputStream(infile);
		}

		out.putNextEntry(zipEntry);

		InputStream contentStream = null;
					    
		// see if this is HTML. If so, we need to scan it.
		String filename = entry.getKey();
		int lastdot = filename.lastIndexOf(".");
		int lastslash = filename.lastIndexOf("/");
		String extension = "";
		if (lastdot >= 0 && lastdot > lastslash)
		    extension = filename.substring(lastdot+1);
		String mimeType = null;
		if (inSakai)
		    mimeType = resource.getContentType();
		boolean isHtml = false;
		if (mimeType != null && (mimeType.startsWith("http") || mimeType.equals("")))
		    mimeType = null;
		if (mimeType != null && (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml"))
				|| mimeType == null && (extension.equals("html") || extension.equals("htm"))) {
		    isHtml = true;
		}

		try {
			if (isHtml) {
				// treat html separately. Need to convert urls to relative
				String content = null;
				if (inSakai) {
					content = new String(resource.getContent());
				} else {
					byte[] b = new byte[(int) infile.length()];
					instream.read(b);
					content = new String(b);
				}
				content = relFixup(content, entry.getValue());
				out.print(content);
			} else {
				if (inSakai) {
					contentStream = resource.streamContent();
				} else  {
					contentStream = instream;
				}
				IOUtils.copy(contentStream, out);
			}
		} catch (Exception e) {
			log.error("Lessons export error outputting file " + e);
		} finally {
			IOUtils.closeQuietly(contentStream);
			IOUtils.closeQuietly(instream);
		}

	    }
	} catch (Exception e) {
	    log.error("Lessons export error outputting file, outputAllFiles " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;

    }

    // xxx/abc/../ccc
    // xxx/ccc
    // xxx/../ccc
    // ccc

    public String removeDotDot(String s) {
	while (true) {
	    int i = s.indexOf("/../");
	    if (i < 1)
		return s;
	    int j = s.lastIndexOf("/", i-1);
	    if (j < 0)
		j = 0;
	    else
		j = j + 1;
	    s = s.substring(0, j) + s.substring(i+4);
	}
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
	    res.dependencies = new HashSet<String>();
	    res.use = null;
	    res.islink = false;
	    res.isbank = false;
	    samigoMap.put(res.sakaiId, res);
	}
	List<Long> poolIds = samigoExport.getAllPools();
	if (doBank && poolIds.size() > 0) {
	    if (version >= V13) {
		for (Long poolId: poolIds) {
		    Resource res = new Resource();
		    res.resourceId = getResourceId();
		    res.location = "cc-objects/" + res.resourceId + ".xml";
		    res.sakaiId = null;
		    res.dependencies = new HashSet<String>();
		    res.use = null;
		    res.islink = false;
		    res.isbank = false;
		    poolMap.put(poolId, res);
		}
	    } else {
		Resource res = new Resource();
		res.resourceId = getResourceId();
		res.location = "cc-objects/" + res.resourceId + ".xml";
		res.sakaiId = null;
		res.dependencies = new HashSet<String>();
		res.use = null;
		res.islink = false;
		res.isbank = false;
		poolMap.put(1L, res);
	    }
	}
	return true;
    }

    public boolean outputAllSamigo(ZipPrintStream out) {
	try {
	    for (Map.Entry<String, Resource> entry: samigoMap.entrySet()) {

		ZipEntry zipEntry = new ZipEntry(entry.getValue().location);
		out.putNextEntry(zipEntry);
		boolean ok = samigoExport.outputEntity(entry.getValue().sakaiId, out, errStream, this, entry.getValue(), version);
		if (!ok)
		    return false;

	    }
	    if (poolMap.size() > 0) {
		for (Map.Entry<Long, Resource> entry: poolMap.entrySet()) {
		    ZipEntry zipEntry = new ZipEntry(entry.getValue().location);
		    out.putNextEntry(zipEntry);
		    boolean ok = samigoExport.outputBank(entry.getKey(), out, errStream, this, samigoBank, version);
		    if (!ok)
			return false;
		}
	    }
	} catch (Exception e) {
	    log.error("output sam " + e);
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
	    res.dependencies = new HashSet<String>();
	    res.use = null;
	    res.islink = false;
	    res.isbank = false;
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

		if (version >= V13) {
		    String xmlHref = "cc-objects/" + entry.getValue().resourceId + ".xml";

		    zipEntry = new ZipEntry(xmlHref);

		    out.putNextEntry(zipEntry);
		    ok = assignmentExport.outputEntity2(entry.getValue().sakaiId, out, errStream, this, entry.getValue());
		    if (!ok)
			return false;
		}
	    }

	} catch (Exception e) {
	    log.error("Lessons export error outputting file, outputAllAssignments " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;
    }

    public boolean addAllForums(String siteId) {
	List<String> forums = forumsExport.getEntitiesInSite(siteId, this);
	if (forums == null)
	    return true;
	for (String sakaiId: forums) {
	    Resource res = new Resource();
	    res.resourceId = getResourceId();
	    res.location = "cc-objects/" + res.resourceId + ".xml";
	    res.sakaiId = sakaiId;
	    res.dependencies = new HashSet<String>();
	    res.use = null;
	    res.islink = false;
	    res.isbank = false;
	    forumsMap.put(res.sakaiId, res);
	}
	return true;
    }

    public boolean outputAllForums(ZipPrintStream out) {
	try {
	    for (Map.Entry<String, Resource> entry: forumsMap.entrySet()) {
		ZipEntry zipEntry = new ZipEntry(entry.getValue().location);

		out.putNextEntry(zipEntry);
		boolean ok = forumsExport.outputEntity(entry.getValue().sakaiId, out, errStream, this, entry.getValue(), version);
		if (!ok)
		    return false;

	    }
	} catch (Exception e) {
	    log.error("problem in outputallforums, outputAllForums " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}

	return true;

    }

    public boolean addAllBlti(String siteId) {
	List<String> bltis = bltiExport.getEntitiesInSite(siteId, this);
	if (bltis == null)
	    return true;
	for (String sakaiId : bltis) {
	    Resource res = new Resource();
	    res.resourceId = getResourceId();
	    res.location = ("cc-objects/" + res.resourceId + ".xml");
	    res.sakaiId = sakaiId;
	    res.dependencies = new HashSet();
	    res.use = null;
	    res.islink = false;
	    res.isbank = false;
	    bltiMap.put(res.sakaiId, res);
	}
	return true;
    }

    public boolean outputAllBlti(ZipPrintStream out) {
	try {
	    for (Map.Entry entry : bltiMap.entrySet()) {
		ZipEntry zipEntry = new ZipEntry(((Resource)entry.getValue()).location);
		
		out.putNextEntry(zipEntry);
		boolean ok = bltiExport.outputEntity(((Resource)entry.getValue()).sakaiId, out, this.errStream, this, (Resource)entry.getValue(), version);
		if (!ok)
		    return false;
	    }
	} catch (Exception e) {
	    log.error("problem in outputallforums, outputAllBlti " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}
	return true;
    }

    public boolean outputAllTexts(ZipPrintStream out) {
	try {
	    List<SimplePageItem> items = simplePageToolDao.findTextItemsInSite(this.siteId);
	    
	    for (SimplePageItem item : items) {
		String location = "attachments/item-" + item.getId() + ".html";
		
		ZipEntry zipEntry = new ZipEntry(location);
		out.putNextEntry(zipEntry);

		Resource res = new Resource();
		res.sakaiId = ("/text/" + item.getId());
		res.resourceId = getResourceId();
		res.location = location;
		res.dependencies = new HashSet();
		res.use = null;
		res.islink = false;
		res.isbank = false;
		fileMap.put(res.sakaiId, res);

		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
		out.println("<body>");
		out.print(relFixup(item.getHtml(), res, new StringBuilder()));
		out.println("</body>");
		out.println("</html>");

	    }
	} catch (Exception e) {
	    log.error("Lessons export error outputting file, outputAllTexts " + e);
	    setErrKey("simplepage.exportcc-fileerr", e.getMessage());
	    return false;
	}
	return true;
    }

    public boolean outputLessons(ZipPrintStream out)  {
	out.println("  <organization identifier=\"page\" structure=\"rooted-hierarchy\">");
	out.println("    <item identifier=\"I_1\">");
	List<SimplePageItem> sitePages = simplePageToolDao.findItemsInSite(ToolManager.getCurrentPlacement().getContext());

	for (SimplePageItem i : sitePages)
	    pagesDone.add(Long.valueOf(i.getSakaiId()));
	for (SimplePageItem i : sitePages) {
	    outputLessonPage(out, Long.valueOf(i.getSakaiId()), i.getName(), 6, true);
	}
	out.println("    </item>");
	out.println("  </organization>");
	return true;
    }
    
    public void outputIndent(ZipPrintStream out, int indent) {
	for (int i = 0; i < indent; i++)
	    out.print(" ");
    }

    public SimplePageItem outputLessonPage(ZipPrintStream out, Long pageId, String title, int indent, boolean shownext) {
	SimplePageItem next = null;
	boolean multiplenext = false;
	
	pagesDone.add(pageId);

	outputIndent(out, indent); out.println("<item identifier=\"page_" + pageId + "\">");
	outputIndent(out, indent + 2); out.println("<title>" + StringEscapeUtils.escapeXml(title) + "</title>");

	List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId.longValue());
	for (SimplePageItem item : items) {
	    if (item.getNextPage()) {
		if (next == null) {
		    next = item;
		}
		else if (!multiplenext) {
		    next = null;
		    multiplenext = true;
		}
	    }
	}

	for (SimplePageItem item : items) {
	    Resource res = null;
	    String sakaiId = null;
	    String itemString = null;
	    String urlTitle = null;

	    switch (item.getType()) {
	    case SimplePageItem.PAGE:
		Long pId = Long.valueOf(item.getSakaiId());
		if (this.pagesDone.contains(pId)) {
		    this.errStream.println(messageLocator.getMessage("simplepage.exportcc-pagealreadydone").replace("{1}", title).replace("{2}", item.getName()));
		} else if ((next != null) && (item.getId() == next.getId())) {
		    if (shownext) {
			SimplePageItem n = outputLessonPage(out, pId, item.getName(), indent + 2, false);
			while ((n != null) && (!this.pagesDone.contains(pId = Long.valueOf(n.getSakaiId())))) {
			    n = outputLessonPage(out, pId, n.getName(), indent + 2, false);
			}
			if ((n != null) && (this.pagesDone.contains(pId))) {
			    errStream.println(messageLocator.getMessage("simplepage.exportcc-pagealreadydone").replace("{1}", title).replace("{2}", item.getName()));
			}
		    }
		} else {
		    outputLessonPage(out, pId, item.getName(), indent + 2, true);
		}
		break;
	    case SimplePageItem.MULTIMEDIA:
		String embedCode = item.getAttribute("multimediaEmbedCode");
		if (embedCode != null && embedCode.length() > 0) {
		    String location = "attachments/item-" + item.getId() + ".html";
		    res = new Resource();
		    res.sakaiId = ("/text/" + item.getId());
		    res.resourceId = getResourceId();
		    res.location = location;
		    res.dependencies = new HashSet();
		    res.use = null;
		    res.islink = false;
		    res.isbank = false;
		    fileMap.put(res.sakaiId, res);
		    embedMap.put(item.getId(), relFixup(embedCode, res));
		    // item won't have a title, so we have to specify one. But with an embed code
		    // there's no useful title. So just use generic text.
		    urlTitle = messageLocator.getMessage("simplepage.importcc-embedtitle");
		    break;
		}
		String oembed = item.getAttribute("multimediaUrl");
		if (oembed != null && oembed.length() > 0) {
		    // we've already done outputAllFiles, so this code is simply
		    // to get the <resource> output.
		    String location = "attachments/item-" + item.getId() + ".xml";
		    // first argument is dummy in this case, since it's not in the file system
		    res = addFile(oembed, location);
		    res.islink = true;
		    res.url = oembed;
		    res.title = item.getName();
		    if (res.title == null || res.title.length() == 0)
			res.title = oembed;
		    // queue this to output the XML link file
		    linkSet.add(res);
		    // no actual item, so we need to supply a title. Use the URL
		    urlTitle = oembed;
		    break;
		}
	    case SimplePageItem.RESOURCE:
		res = (Resource)this.fileMap.get(item.getSakaiId());
		break;
	    case SimplePageItem.ASSIGNMENT:
		sakaiId = item.getSakaiId();
		if (sakaiId.indexOf("/", 1) < 0)
		    sakaiId = "assignment/" + sakaiId;
		else
		    sakaiId = sakaiId.substring(1);
		res = (Resource)this.assignmentMap.get(sakaiId);
		break;
	    case SimplePageItem.ASSESSMENT:
		sakaiId = item.getSakaiId();
		if (sakaiId.indexOf("/", 1) < 0)
		    sakaiId = "sam_pub/" + sakaiId;
		else
		    sakaiId = sakaiId.substring(1);
		res = (Resource)samigoMap.get(sakaiId);
		break;
	    case SimplePageItem.TEXT:
		res = (Resource)fileMap.get("/text/" + item.getId());
		break;
	    case SimplePageItem.FORUM:
		res = (Resource)forumsMap.get(item.getSakaiId().substring(1));
		break;
	    case SimplePageItem.BLTI:
		res = (Resource)bltiMap.get(item.getSakaiId().substring(1));
		break;
	    case SimplePageItem.COMMENTS:
	    case SimplePageItem.STUDENT_CONTENT:
	    case SimplePageItem.QUESTION:
	    case SimplePageItem.PEEREVAL:
		switch (item.getType()) {
		case SimplePageItem.COMMENTS:
		    itemString = messageLocator.getMessage("simplepage.comments-section");
		    break;
		case SimplePageItem.STUDENT_CONTENT:
		    itemString = messageLocator.getMessage("simplepage.student-content");
		    break;
		case SimplePageItem.QUESTION:
		    itemString = messageLocator.getMessage("simplepage.questionName");
		    break;
		case SimplePageItem.PEEREVAL:
		    itemString = messageLocator.getMessage("simplepage.peerEval-secotion");
		    break;
		case SimplePageItem.BREAK:
		    itemString = messageLocator.getMessage("simplepage.break");
		    break;
		}
		errStream.println(messageLocator.getMessage("simplepage.exportcc-bad-type").replace("{1}", title).replace("{2}", item.getName()).replace("{3}", itemString));
		break;
	    }
	    if (res != null) {
		outputIndent(out, indent + 2); out.println("<item identifier=\"item_" + item.getId() + "\" identifierref=\"" + res.resourceId + "\">");
		String ititle = item.getName();
		
		if ((ititle == null) || (ititle.equals(""))) {
		    if (urlTitle != null)
			ititle = urlTitle;
		    else
			ititle = messageLocator.getMessage("simplepage.importcc-texttitle");
		}
		outputIndent(out, indent + 4); out.println("<title>" + StringEscapeUtils.escapeXml(ititle) + "</title>");
		// output Sakai-specific information, if any
		outputItemMetadata(out, indent, item);
		outputIndent(out, indent + 2); out.println("</item>"); 
	    }
	}
	outputIndent(out, indent); out.println("</item>");
	if (shownext) {
	    return null;
	}
	return next;
    }

    public void	outputItemMetadata(ZipPrintStream out, int indent, SimplePageItem item) {
	// inline types
	switch (item.getType()) {
	case SimplePageItem.MULTIMEDIA:
	    String mmDisplayType = item.getAttribute("multimediaDisplayType");
	    if (mmDisplayType == null || mmDisplayType.equals(""))
		mmDisplayType = "2";
	    outputIndent(out, indent +4); out.println("<metadata>");
	    outputIndent(out, indent +6); out.println("<lom:lom>");
	    outputIndent(out, indent +8); out.println("<lom:general>");
	    outputIndent(out, indent +10); out.println("<lom:structure>");
	    outputIndent(out, indent +12); out.println("<lom:source>inline.lessonbuilder.sakaiproject.org</lom:source>");
	    outputIndent(out, indent +12); out.println("<lom:value>true</lom:value>");
	    outputIndent(out, indent +12); out.println("<lom:source>mmDisplayType.lessonbuilder.sakaiproject.org</lom:source>");
	    outputIndent(out, indent +12); out.println("<lom:value>" + mmDisplayType + "</lom:value>");
	    outputIndent(out, indent +10); out.println("</lom:structure>");
	    outputIndent(out, indent +8); out.println("</lom:general>");
	    outputIndent(out, indent +6); out.println("</lom:lom>");
	    outputIndent(out, indent +4); out.println("</metadata>");
	    break;
	case SimplePageItem.TEXT:
	    outputIndent(out, indent +4); out.println("<metadata>");
	    outputIndent(out, indent +6); out.println("<lom:lom>");
	    outputIndent(out, indent +8); out.println("<lom:general>");
	    outputIndent(out, indent +10); out.println("<lom:structure>");
	    outputIndent(out, indent +12); out.println("<lom:source>inline.lessonbuilder.sakaiproject.org</lom:source>");
	    outputIndent(out, indent +12); out.println("<lom:value>true</lom:value>");
	    outputIndent(out, indent +10); out.println("</lom:structure>");
	    outputIndent(out, indent +8); out.println("</lom:general>");
	    outputIndent(out, indent +6); out.println("</lom:lom>");
	    outputIndent(out, indent +4); out.println("</metadata>");
	    break;
	}
    };

    public boolean outputManifest(ZipPrintStream out) {
	    
	String title = "Sakai";  // should never be used
	try {
	    Site site = null;
	    site = SiteService.getSite(siteId);
	    title = site.getTitle();
	} catch (Exception impossible) {
	    // impossible, one hopes
	}

	try {
	    ZipEntry zipEntry = new ZipEntry("imsmanifest.xml");
	    out.putNextEntry(zipEntry);
	    switch (version) {
	    case V11:
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<manifest identifier=\"cctd0001\"");
		out.println("  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1\"");
		out.println("  xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource\"");
		out.println("  xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest\"");
		out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		out.println("  xsi:schemaLocation=\"");
		out.println("  http://ltsc.ieee.org/xsd/imsccv1p1/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lomresource_v1p0.xsd ");
		out.println("  http://www.imsglobal.org/xsd/imsccv1p1/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imscp_v1p2_v1p0.xsd ");
		out.println("  http://ltsc.ieee.org/xsd/imsccv1p1/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p1/LOM/ccv1p1_lommanifest_v1p0.xsd\">");
		out.println("  <metadata>");
		out.println("    <schema>IMS Common Cartridge</schema>");
		out.println("    <schemaversion>1.1.0</schemaversion>");
		out.println("    <lomimscc:lom>");
		out.println("      <lomimscc:general>");
		out.println("        <lomimscc:title>");
		out.println("          <lomimscc:string>" + StringEscapeUtils.escapeXml(title) + "</lomimscc:string>");
		out.println("        </lomimscc:title>");
//		out.println("        <lomimscc:description>");
//		out.println("          <lomimscc:string language=\"en-US\">Sakai Export, including only files from site</lomimscc:string>");
//		out.println("        </lomimscc:description>");
//		out.println("        <lomimscc:keyword>");
//		out.println("          <lomimscc:string language=\"en-US\">Export</lomimscc:string>");
//		out.println("        </lomimscc:keyword>");
		out.println("      </lomimscc:general>");
		out.println("    </lomimscc:lom>");
		out.println("  </metadata>");
	    break;

	    case V13:
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<manifest identifier=\"cctd0001\"");
		out.println("  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1\"");
		out.println("  xmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource\"");
		out.println("  xmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest\"");
		out.println("  xmlns:cpx=\"http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2\"");
		out.println("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
		out.println("  xsi:schemaLocation=\"http://ltsc.ieee.org/xsd/imsccv1p3/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p3/LOM/ccv1p3_lomresource_v1p0.xsd ");
		out.println("                       http://www.imsglobal.org/xsd/imsccv1p3/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imscp_v1p2_v1p0.xsd ");
		out.println("                       http://ltsc.ieee.org/xsd/imsccv1p3/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p3/LOM/ccv1p3_lommanifest_v1p0.xsd ");
		out.println("                       http://www.imsglobal.org/xsd/imsccv1p3/imscp_extensionv1p2 http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_cpextensionv1p2_v1p0.xsd\">");
		out.println("  <metadata>");
		out.println("    <schema>IMS Common Cartridge</schema>");
		out.println("    <schemaversion>1.3.0</schemaversion>");
		out.println("    <lomimscc:lom>");
		out.println("      <lomimscc:general>");
		out.println("        <lomimscc:title>");
		out.println("          <lomimscc:string language=\"en-US\">" + StringEscapeUtils.escapeXml(title) + "</lomimscc:string>");
		out.println("        </lomimscc:title>");
		out.println("      </lomimscc:general>");
		out.println("    </lomimscc:lom>");
		out.println("  </metadata>");
		break;

	    default:
	    out.print(
		      "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<manifest identifier=\"sakai1\"\n  xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1\"\nxmlns:lom=\"http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource\"\nxmlns:lomimscc=\"http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest\"\nxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\nxsi:schemaLocation=\"                                                                                                                        \n  http://ltsc.ieee.org/xsd/imsccv1p2/LOM/resource http://www.imsglobal.org/profile/cc/ccv1p2/LOM/ccv1p2_lomresource_v1p0.xsd                  \n  http://www.imsglobal.org/xsd/imsccv1p2/imscp_v1p1 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imscp_v1p2_v1p0.xsd                     \n  http://ltsc.ieee.org/xsd/imsccv1p2/LOM/manifest http://www.imsglobal.org/profile/cc/ccv1p2/LOM/ccv1p2_lommanifest_v1p0.xsd\">\n  <metadata>\n    <schema>IMS Common Cartridge</schema>\n    <schemaversion>1.2.0</schemaversion>\n    <lomimscc:lom>\n      <lomimscc:general>\n	<lomimscc:title>\n	  <lomimscc:string>" + StringEscapeUtils.escapeXml(title) + "</lomimscc:string>\n	</lomimscc:title>\n      </lomimscc:general>\n    </lomimscc:lom>\n  </metadata>\n ");
	    }

	    out.println("  <organizations>");
	    outputLessons(out);
	    out.println("  </organizations>");

	    String qtiid = null;
	    String bankid = null;
	    String topicid = null;
	    String linkid = null;
	    String usestr = "";
	    switch (version) {
	    case V11:
		qtiid = "imsqti_xmlv1p2/imscc_xmlv1p1/assessment";
		bankid = "imsqti_xmlv1p2/imscc_xmlv1p1/question-bank";
		topicid = "imsdt_xmlv1p1";
		linkid = "imswl_xmlv1p1";
		usestr = "";
		break;
	    case V13:
		qtiid = "imsqti_xmlv1p2/imscc_xmlv1p3/assessment";
		bankid = "imsqti_xmlv1p2/imscc_xmlv1p3/question-bank";
		topicid = "imsdt_xmlv1p3";
		linkid = "imswl_xmlv1p3";
		usestr = " intendeduse=\"assignment\"";
		break;
	    default:
		qtiid = "imsqti_xmlv1p2/imscc_xmlv1p2/assessment";
		bankid = "imsqti_xmlv1p2/imscc_xmlv1p2/question-bank";
		topicid = "imsdt_xmlv1p2";
		linkid = "imswl_xmlv1p2";
		usestr = " intendeduse=\"assignment\"";
	    }

	    out.println("  <resources>");
	    for (Map.Entry<String, Resource> entry: fileMap.entrySet()) {
		String use = "";
		if (version >= V12) {
		    if (entry.getValue().use != null)
			use = " intendeduse=\"" + entry.getValue().use + "\"";
		}
		String type = "webcontent";
		if (((Resource)entry.getValue()).islink)
		    type = linkid;
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"" + type + "\"" + use + ">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    for (Map.Entry<String, Resource> entry: samigoMap.entrySet()) {
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"" + qtiid + "\">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    // question bank
	    for (Map.Entry<Long, Resource> entry: poolMap.entrySet()) {
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"" + bankid + "\">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    for (Map.Entry<String, Resource> entry: assignmentMap.entrySet()) {
		String variantId = null;
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"webcontent\"" + usestr + ">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		if (version >= V13) {
		    variantId = getResourceId();
		    out.println("      <cpx:variant identifier=\"" + getResourceId() + "\" identifierref=\"" + variantId + "\">");
		    out.println("        <cpx:metadata/>");
		    out.println("      </cpx:variant>");
		}
		out.println("    </resource>");

		// output the preferred version for 1.3 and up
		if (version >= V13) {
		    String xmlHref = "cc-objects/" + entry.getValue().resourceId + ".xml";
		    out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(xmlHref) + "\" identifier=\"" + variantId + "\" type=\"assignment_xmlv1p0\">");
		    out.println("      <file href=\"" + StringEscapeUtils.escapeXml(xmlHref) + "\"/>");
		    for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		    out.println("    </resource>");
		}
	    }

	    for (Map.Entry<String, Resource> entry: forumsMap.entrySet()) {
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\" identifier=\"" + entry.getValue().resourceId + "\" type=\"" + topicid + "\">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(entry.getValue().location) + "\"/>");
		for (String d: entry.getValue().dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    for (Map.Entry entry : this.bltiMap.entrySet()) {
		out.println("    <resource href=\"" + StringEscapeUtils.escapeXml(((Resource)entry.getValue()).location) + "\" identifier=\"" + ((Resource)entry.getValue()).resourceId + "\" type=\"imsbasiclti_xmlv1p0\">");
		out.println("      <file href=\"" + StringEscapeUtils.escapeXml(((Resource)entry.getValue()).location) + "\"/>");
		for (String d : ((Resource)entry.getValue()).dependencies)
		    out.println("      <dependency identifierref=\"" + d + "\"/>");
		out.println("    </resource>");
	    }

	    // add error log at the very end
	    String errId = getResourceId();

	    out.println(("    <resource href=\"cc-objects/export-errors\" identifier=\"" + errId + 
			   "\" type=\"webcontent\">\n      <file href=\"cc-objects/export-errors\"/>\n    </resource>"));
	    
	    out.println("  </resources>\n</manifest>");

	    // items with embed code. need to put out the HTML page
	    for (Map.Entry entry : this.embedMap.entrySet()) {
		Long itemId = (Long)entry.getKey();
		String location = "attachments/item-" + itemId + ".html";
		
		ZipEntry ze = new ZipEntry(location);
		out.putNextEntry(ze);

		out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"  \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">");
		out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">");
		out.println("<body>");
		out.print((String)entry.getValue());
		out.println("</body>");
		out.println("</html>");
	    }		

	    // links. need to put out the XML file defining the link
	    for (Resource res: linkSet) {
		ZipEntry ze = new ZipEntry(res.location);
		out.putNextEntry(ze);
		switch (version) {
		case V11:
		    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		    out.println("<webLink xmlns=\"http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1\"");
		    out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		    out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p1/imswl_v1p1 http://www.imsglobal.org/profile/cc/ccv1p1/ccv1p1_imswl_v1p1.xsd\">");
		    out.println("  <title>" + StringEscapeUtils.escapeXml(res.title) + "</title>");
		    out.println("  <url href=\"" + StringEscapeUtils.escapeXml(res.url) + "\"/>");
		    out.println("</webLink>");
		    break;
		case V13:
		    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		    out.println("<webLink xmlns=\"http://www.imsglobal.org/xsd/imsccv1p3/imswl_v1p3\"");
		    out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p3/imswl_v1p3 ");
		    out.println("      http://www.imsglobal.org/profile/cc/ccv1p3/ccv1p3_imswl_v1p3.xsd\">");
		    out.println("  <title>" + StringEscapeUtils.escapeXml(res.title) + "</title>");
		    out.println("  <url href=\"" + StringEscapeUtils.escapeXml(res.url) + "\"/>");
		    out.println("</webLink>");
		    break;
		default:
		    out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		    out.println("<webLink xmlns=\"http://www.imsglobal.org/xsd/imsccv1p2/imswl_v1p2\"");
		    out.println("      xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
		    out.println("      xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsccv1p2/imswl_v1p2 http://www.imsglobal.org/profile/cc/ccv1p2/ccv1p2_imswl_v1p2.xsd\">");
		    out.println("  <title>" + StringEscapeUtils.escapeXml(res.title) + "</title>");
		    out.println("  <url href=\"" + StringEscapeUtils.escapeXml(res.url) + "\"/>");
		    out.println("</webLink>");
		}
	    }

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
	    log.error("Lessons export error outputting file, outputManifest " + e);
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
	    outputAllForums (out);
	    outputAllBlti(out);
	    outputAllTexts(out);
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
	    log.error("Lessons export error outputting file, download " + ioe);
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
	// I'm matching against /access/content/group not /access/content/group/SITEID, because SITEID can in some installations
	// be user chosen. In that case there could be escaped characters, and the escaping in HTML URL's isn't unique. 
	Pattern target = Pattern.compile("(?:https?:)?(?://[-a-zA-Z0-9.]+(?::[0-9]+)?)?/access/content(/group/)|http://lessonbuilder.sakaiproject.org/", Pattern. CASE_INSENSITIVE);
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
	    if (matcher.start(1) >= 0) { // matched /access/content...
		// make sure it's the right siteid. This approach will get it no matter
		// how the siteid is url encoded
		int startsite = matcher.end(1);
		int last = s.indexOf("/", startsite);
		if (last < 0)
		    continue;
		String sitepart = null;
		try {
		    sitepart = URLDecoder.decode(s.substring(startsite, last), "UTF-8");
		} catch (Exception e) {
		    log.info("decode failed in CCExport " + e);
		}
		if (!siteId.equals(sitepart))
		    continue;

		// it matches, now map it
		// unfortunately the hostname and port are a bit unpredictable. Don't use them for match. I think siteids are
		// unique enough that if /access/content/group/SITEID matches that should be enough
		int sakaistart = matcher.start(1); //start of sakaiid, can't find end until we figure out quoting

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
		try {
		    sakaiId = removeDotDot(URLDecoder.decode(s.substring(sakaistart, sakaiend), "UTF-8"));
		} catch (Exception e) {
		    log.info("Exception in CCExport URLDecoder " + e);
		}
		ret.append(s.substring(index, start));
		ret.append("$IMS-CC-FILEBASE$..");
		ret.append(removeDotDot(s.substring(last, sakaiend)));
		index = sakaiend;  // start here next time
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
			if (s.charAt(endnum) == '/')
			    endnum++;
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

    // turns the links into relative links
    // fixups will get a list of offsets where fixups were done, for loader to reconstitute HTML
    public String relFixup (String s, Resource resource, StringBuilder fixups) {
	// http://lessonbuilder.sakaiproject.org/53605/
	StringBuilder ret = new StringBuilder();
	String sakaiIdBase = "/group/" + siteId;
	// I'm matching against /access/content/group not /access/content/group/SITEID, because SITEID can in some installations
	// be user chosen. In that case there could be escaped characters, and the escaping in HTML URL's isn't unique. 
	Pattern target = Pattern.compile("(?:https?:)?(?://[-a-zA-Z0-9.]+(?::[0-9]+)?)?/access/content(/group/)|http://lessonbuilder.sakaiproject.org/", Pattern. CASE_INSENSITIVE);
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
	    if (matcher.start(1) >= 0) { // matched /access/content...
		// make sure it's the right siteid. This approach will get it no matter
		// how the siteid is url encoded
		int startsite = matcher.end(1);
		int last = s.indexOf("/", startsite);
		if (last < 0)
		    continue;
		String sitepart = null;
		try {
		    sitepart = URLDecoder.decode(s.substring(startsite, last), "UTF-8");
		} catch (Exception e) {
		    log.info("decode failed in CCExport " + e);
		}
		if (!siteId.equals(sitepart))
		    continue;

		int sakaistart = matcher.start(1); //start of sakaiid, can't find end until we figure out quoting

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
		try {
		    sakaiId = removeDotDot(URLDecoder.decode(s.substring(sakaistart, sakaiend), "UTF-8"));
		} catch (Exception e) {
		    log.info("Exception in CCExport URLDecoder " + e);
		}
		// do the mapping. resource.location is a relative URL of the page we're looking at
		// sakaiid is the URL of the object, starting /group/
		String base = getParent(resource.location);
		String thisref = sakaiId.substring(sakaiIdBase.length()+1);
		String relative = relativize(thisref, base);
		ret.append(s.substring(index, start));
		// we're now at start of URL. save it for fixup list
		if (fixups != null) {
		    if (fixups.length() > 0)
			fixups.append(",");
		    fixups.append("" + ret.length());
		}
		// and now add the new relative URL
		ret.append(relative.toString());
		index = sakaiend;  // start here next time
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
			String base = getParent(resource.location);
			String thisref = sakaiId.substring(sakaiIdBase.length()+1);
			String relative = relativize(thisref, base);
			// we're now at start of URL. save it for fixup list
			if (fixups != null) {
			    if (fixups.length() > 0)
				fixups.append(",");
			    fixups.append("" + ret.length());
			}
			// and now add the new relative URL
			ret.append(relative);
			if (s.charAt(endnum) == '/')
			    endnum++;
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
	if (fixups != null && fixups.length() > 0) {
	    return ("<!--fixups:" + fixups.toString() + "-->" + ret.toString());
	}
	return ret.toString();
    }		

    public String relFixup (String s, Resource resource) {
	return relFixup(s, resource, null);

    }
    // return base directory of file, including trailing /
    // "" if it is in home directory
    public String getParent(String s) {
	int i = s.lastIndexOf("/");
	if (i < 0)
	    return "";
	else
	    return s.substring(0, i+1);
    }

    // return relative path to target from base
    // base is assumed to be "" or ends in /
    public String relativize(String target, String base) {
	if (base.equals(""))
	    return target;
	if (target.startsWith(base))
	    return target.substring(base.length());
	else {
	    // get parent directory of base directory.
	    // base directory ends in /
	    int i = base.lastIndexOf("/", base.length()-2);
	    if (i < 0)
		base = "";
	    else
		base = base.substring(0, i+1); // include /
	    return "../" + relativize(target, base);
	}
    }

}
