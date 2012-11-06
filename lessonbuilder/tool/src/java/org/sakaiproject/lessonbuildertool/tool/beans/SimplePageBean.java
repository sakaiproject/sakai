/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * Author: Eric Jeney, jeney@rutgers.edu
 * The original author was Joshua Ryan josh@asu.edu. However little of that code is actually left
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
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

package org.sakaiproject.lessonbuildertool.tool.beans;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
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
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.cover.IdManager;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.cc.CartridgeLoader;
import org.sakaiproject.lessonbuildertool.cc.Parser;
import org.sakaiproject.lessonbuildertool.cc.PrintHandler;
import org.sakaiproject.lessonbuildertool.cc.ZipLoader;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.BltiInterface;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;
import org.sakaiproject.lessonbuildertool.service.GroupPermissionsService;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.tool.producers.ShowItemProducer;
import org.sakaiproject.lessonbuildertool.tool.producers.ShowPageProducer;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.springframework.web.multipart.MultipartFile;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;

/**
 * Backing bean for Simple pages
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 * @author Joshua Ryan josh@asu.edu alt^I
 */

// This bean has two related but somewhat separate uses:
// 1) It keeps common data for the producers and other code. In that use the lifetime of the bean is while
//    generating a single page. The bean has common application logic. The producers are pretty much just UI.
//    The DAO is low-level data access. This is everything else. The producers call this bean, and not the 
//    DAO directly. This layer sticks caches on top of the data access, and provides more complex logic. Security
//    is primarily in the DAO, but the DAO only checks permissions. We have to make sure we only access pages
//    and items in our site
//       Most of the caches are local. Since this bean is request-scope they are recreated for each request.
//    Thus we don't have to worry about timing out the entries.
// 2) It is used by RSF to access data. Normally the bean is associated with a specific page. However the 
//    UI often has to update attributes of a specific item. For that use, there are some item-specific variables
//    in the bean. They are only meaningful during item operations, when itemId will show which item is involved.
// While the bean is used by all the producers, the caching was designed specifically for ShowPageProducer.
// That's because it is used a lot more often than the others. ShowPageProducer should do all data access through
// the methods here that cache. There is also caching by hibernate. However this code is cheaper, partly because
// it doesn't have to do synchronization (since it applies just to processing one transaction).

public class SimplePageBean {
	private static Log log = LogFactory.getLog(SimplePageBean.class);

	public enum Status {
		NOT_REQUIRED, REQUIRED, DISABLED, COMPLETED, FAILED
	}
	
    // from ResourceProperites. This isn't in 2.7.1, so define it here. Let's hope it doesn't change...
        public static final String PROP_ALLOW_INLINE = "SAKAI:allow_inline";

	public static final Pattern YOUTUBE_PATTERN = Pattern.compile("v[=/_]([\\w-]{11}([\\?\\&][\\w\\.\\=\\&]*)?)");
	public static final Pattern YOUTUBE2_PATTERN = Pattern.compile("embed/([\\w-]{11}([\\?\\&][\\w\\.\\=\\&]*)?)");
	public static final Pattern SHORT_YOUTUBE_PATTERN = Pattern.compile("([\\w-]{11}([\\?\\&][\\w\\.\\=\\&]*)?)");
	public static final String GRADES[] = { "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "E", "F" };
	public static final String FILTERHTML = "lessonbuilder.filterhtml";
	public static final String LESSONBUILDER_ITEMID = "lessonbuilder.itemid";
	public static final String LESSONBUILDER_PATH = "lessonbuilder.path";
	public static final String LESSONBUILDER_BACKPATH = "lessonbuilder.backpath";
	public static final String LESSONBUILDER_ID = "sakai.lessonbuildertool";

	private static String PAGE = "simplepage.page";
	private static String SITE_UPD = "site.upd";
	private String contents = null;
	private String pageTitle = null;
	private String newPageTitle = null;
	private String subpageTitle = null;
	private boolean subpageNext = false;
	private boolean subpageButton = false;

	private List<Long> currentPath = null;
	private Set<Long> allowedPages = null;    

	private Site currentSite = null; // cache, can be null; used by getCurrentSite

	private List<GroupEntry> currentGroups = null;
	private Set<String> myGroups = null;

	private boolean filterHtml = ServerConfigurationService.getBoolean(FILTERHTML, false);

	public String selectedAssignment = null;
	public String selectedBlti = null;

    // generic entity stuff. selectedEntity is the string
    // coming from the picker. We'll use the same variable for any entity type
	public String selectedEntity = null;
	public String[] selectedEntities = new String[] {};
	public String[] selectedGroups = new String[] {};

	public String selectedQuiz = null;
	
	public long removeId = 0;

	private SimplePage currentPage;
	private Long currentPageId = null;
	private Long currentPageItemId = null;
	private String currentUserId = null;
	private long previousPageId = -1;

    // Item-specific variables. These are set by setters which are called
    // by the various edit dialogs. So they're basically inputs to the
    // methods used to make changes to items. The way it works is that
    // when the user submits the form, RSF takes all the form variables,
    // calls setters for each field, and then calls the method specified
    // by the form. The setters set these variables

	public Long itemId = null;
	public boolean isMultimedia = false;

	public String commentsId;
	public boolean anonymous;
	public String comment;
	public String formattedComment;
	public String editId;
	public boolean graded, sGraded;
	public String maxPoints, sMaxPoints;
	
	public boolean comments;
	public boolean forcedAnon;
	
	public boolean isWebsite = false;

	private String linkUrl;

	private String height, width;

	private String description;
	private String name;
	private boolean required;
	private boolean subrequirement;
	private boolean prerequisite;
	private boolean newWindow;
	private String dropDown;
	private String points;
	private String mimetype;
    // for BLTI, values window, inline, and null for in a new page with navigation
    // but sameWindow should also be set properly, based on the format
	private String format;

	private String numberOfPages;
	private boolean copyPage;

	private String alt = null;
	private String order = null;

	private String youtubeURL;
	private String mmUrl;
	private long youtubeId;

	private boolean hidePage;
	private Date releaseDate;
	private boolean hasReleaseDate;

	private String redirectSendingPage = null;
	private String redirectViewId = null;
	private String quiztool = null;
	private String topictool = null;
	
	private Integer editPrivs = null;
	private String currentSiteId = null;

	public Map<String, MultipartFile> multipartMap;

    // Caches

    // The following caches are used only during a single display of the page. I believe they
    // are so transient that we don't have to worry about synchronizing them or keeping them up to date.
    // Because the producer code tends to deal with items and even item ID's, it doesn't keep objects such
    // as Assignment or PublishedAssessment around. It calls functions here to worry about those. If we
    // don't cache, we'll be doing database lookups a lot. The worst is the code to see whether an item
    // is available. Because it checks all items above, we'd end up order N**2 in the number of items on the
    // page in database queries. It doesn't appear that assignments and assessments do any caching of their
    // own, but hibernate as we use it does.
    //   Normal code shouldn't use the caches directly, but should call something like getAssignment here,
    // which checks the cache and if necessary calls the real getAssignment. I've chosen to do caching on
    // this level, and let the DAO be actual database access. I've really only optimized what is used by
    // ShowPageProducer, as that is used every time a page is shown. Things used when you add or change
    // an item aren't as critical.
    //   If anyone is doing serious work on the code, I recommend creating an Item class that encapsulates
    // all the stuff associated with items. Then the producer would manipulate items. Thus the things in
    // these caches would be held in the Items.

	private Map<Long, SimplePageItem> itemCache = new HashMap<Long, SimplePageItem> ();
	private Map<Long, SimplePage> pageCache = new HashMap<Long, SimplePage> ();
	private Map<Long, List<SimplePageItem>> itemsCache = new HashMap<Long, List<SimplePageItem>> ();
	private Map<String, SimplePageLogEntry> logCache = new HashMap<String, SimplePageLogEntry>();
	private Map<Long, Boolean> completeCache = new HashMap<Long, Boolean>();
    private Map<Long, Boolean> visibleCache = new HashMap<Long, Boolean>();
    // this one needs to be global
	private static Cache groupCache = null;   // itemId => grouplist
	private static Cache resourceCache = null;
	protected static final int DEFAULT_EXPIRATION = 10 * 60;

	public static class PathEntry {
		public Long pageId;
		public Long pageItemId;
		public String title;
	}

	public static class UrlItem {
		public String Url;
		public String label;
		public UrlItem(String Url, String label) {
			this.Url = Url;
			this.label = label;
	    }
	}

	public static class GroupEntry {
	    public String name;
	    public String id;
	}

    // Image types

	private static ArrayList<String> imageTypes;

	static {
		imageTypes = new ArrayList<String>();
		imageTypes.add("bmp");
		imageTypes.add("gif");
		imageTypes.add("icns");
		imageTypes.add("ico");
		imageTypes.add("jpg");
		imageTypes.add("jpeg");
		imageTypes.add("png");
		imageTypes.add("tiff");
		imageTypes.add("tif");
	}

    // Spring Injection

	private SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private ContentHostingService contentHostingService;

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	private GradebookIfc gradebookIfc = null;

	public void setGradebookIfc(GradebookIfc g) {
		gradebookIfc = g;
	}

	private LessonEntity forumEntity = null;
	public void setForumEntity(Object e) {
		forumEntity = (LessonEntity)e;
	}

	private LessonEntity quizEntity = null;
	public void setQuizEntity(Object e) {
		quizEntity = (LessonEntity)e;
	}
	
	private LessonEntity assignmentEntity = null;
	public void setAssignmentEntity(Object e) {
		assignmentEntity = (LessonEntity)e;
	}
        private LessonEntity bltiEntity = null;
        public void setBltiEntity(Object e) {
	    bltiEntity = (LessonEntity)e;
        }
	
	private ToolManager toolManager;
	private SecurityService securityService;
	private SiteService siteService;
	private SimplePageToolDao simplePageToolDao;
	
	private MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator x) {
	    messageLocator = x;
	}
	public MessageLocator getMessageLocator() {
	    return messageLocator;
	}

	static MemoryService memoryService = null;
	public void setMemoryService(MemoryService m) {
	    memoryService = m;
	}

        private HttpServletResponse httpServletResponse;
	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
	}

        private LessonBuilderEntityProducer lessonBuilderEntityProducer;
        public void setLessonBuilderEntityProducer(LessonBuilderEntityProducer p) {
	    lessonBuilderEntityProducer = p;
	}

    // End Injection

	public void init () {	
		if (groupCache == null) {
			groupCache = memoryService.newCache("org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.groupCache");
		}
		
		if (resourceCache == null) {
			resourceCache = memoryService.newCache("org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.resourceCache");
		}
	}

    // no destroy. We want to leave the cache intact when we exit, because there's one of us
    // per request.

	public SimplePageItem findItem(long itId) {
		Long itemId = itId;
		SimplePageItem ret = itemCache.get(itemId);
		if (ret != null)
			return ret;
		ret = simplePageToolDao.findItem(itemId);
		if (ret != null)
			itemCache.put(itemId, ret);
		return ret;
	}

	public SimplePage getPage(Long pageId) {
		SimplePage ret = pageCache.get(pageId);
		if (ret != null)
			return ret;
		ret = simplePageToolDao.getPage(pageId);
		if (ret != null)
			pageCache.put(pageId, ret);
		return ret;
	}

	public List<String> errMessages() {
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		List<String> errors = (List<String>)toolSession.getAttribute("lessonbuilder.errors");
		if (errors != null)
			toolSession.removeAttribute("lessonbuilder.errors");
		return errors;
	}

	public void setErrMessage(String s) {
		ToolSession toolSession = sessionManager.getCurrentToolSession();
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

	public void setErrKey(String key, String text ) {
		if (text == null)
		    text = "";
		setErrMessage(messageLocator.getMessage(key).replace("{}", text));
	}

       public void setTopRefresh() {
	   ToolSession toolSession = sessionManager.getCurrentToolSession();
	   if (toolSession == null)
	       return;
	   toolSession.setAttribute("lessonbuilder.topRefresh", true);
       }

       public boolean getTopRefresh() {
	   ToolSession toolSession = sessionManager.getCurrentToolSession();
	   if (toolSession.getAttribute("lessonbuilder.topRefresh") != null) {
	       toolSession.removeAttribute("lessonbuilder.topRefresh");
	       return true;
	   }
	   return false;
       }


    // a lot of these are setters and getters used for the form process, as 
    // described above

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public String getDescription() {
		if (itemId != null && itemId != -1) {
			return findItem(itemId).getDescription();
		} else {
			return null;
		}
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setHidePage(boolean hide) {
		hidePage = hide;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public void setHasReleaseDate(boolean hasReleaseDate) {
		this.hasReleaseDate = hasReleaseDate;
	}

    // gets called for non-checked boxes also, but q will be null
	public void setQuiztool(String q) {
	    if (q != null)
		quiztool = q;
	}

	public void setTopictool(String q) {
	    if (q != null)
		topictool = q;
	}

	public String getName() {
		if (itemId != null && itemId != -1) {
			return findItem(itemId).getName();
		} else {
			return null;
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public void setSubrequirement(boolean subrequirement) {
		this.subrequirement = subrequirement;
	}

	public void setPrerequisite(boolean prerequisite) {
		this.prerequisite = prerequisite;
	}

	public void setNewWindow(boolean newWindow) {
		this.newWindow = newWindow;
	}

	public void setDropDown(String dropDown) {
		this.dropDown = dropDown;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public void setMimetype(String mimetype) {
		if (mimetype != null)
		    mimetype = mimetype.toLowerCase().trim();
		this.mimetype = mimetype;
	}

	public String getPageTitle() {
		return getCurrentPage().getTitle();
	}

	public void setPageTitle(String title) {
		pageTitle = title;
	}

	public void setNewPageTitle(String title) {
		newPageTitle = title;
	}

	public void setNumberOfPages(String n) {
		numberOfPages = n;
	}

	public void setCopyPage(boolean c) {
		this.copyPage = c;
	}

	public String getContents() {
		return (itemId != null && itemId != -1 ? findItem(itemId).getHtml() : "");
	}

	public void setContents(String contents) {
		this.contents = contents;
	}

	public void setItemId(Long id) {
		itemId = id;
	}

	public Long getItemId() {
	    return itemId;
	}

	public void setMultimedia(boolean isMm) {
	    isMultimedia = isMm;
	}
	
	public void setWebsite(boolean isWebsite) {
	    this.isWebsite = isWebsite;
	}

    // hibernate interposes something between us and saveItem, and that proxy gets an
    // error after saveItem does. Thus we never see any value that saveItem might 
    // return. Hence we pass saveItem a list to which it adds the error message. If
    // there is a message from saveItem take precedence over the message we detect here,
    // since it's the root cause.
	public boolean saveItem(Object i, boolean requiresEditPermission) {       
		String err = null;
		List<String>elist = new ArrayList<String>();
		
		try {
			simplePageToolDao.saveItem(i,  elist, messageLocator.getMessage("simplepage.nowrite"), requiresEditPermission);
		} catch (Throwable t) {	
			// this is probably a bogus error, but find its root cause
			while (t.getCause() != null) {
				t = t.getCause();
			}
			err = t.toString();
		}
		
		// if we got an error from saveItem use it instead
		if (elist.size() > 0)
			err = elist.get(0);
		if (err != null) {
			setErrMessage(messageLocator.getMessage("simplepage.savefailed") + err);
			return false;
		}
		
		return true;
	}
	
	public boolean saveItem(Object i) {
		return saveItem(i, true);
	}
	
	public boolean update(Object i) {
		return update(i, true);
	}

    // see notes for saveupdate
	
	// requiresEditPermission determines whether simplePageToolDao should confirm
	// edit permissions before making the update
	boolean update(Object i, boolean requiresEditPermission) {       
		String err = null;
		List<String>elist = new ArrayList<String>();
		try {
			simplePageToolDao.update(i,  elist, messageLocator.getMessage("simplepage.nowrite"), requiresEditPermission);
		} catch (Throwable t) {
			// this is probably a bogus error, but find its root cause
			while (t.getCause() != null) {
				t = t.getCause();
			}
			err = t.toString();
		}
	    // if we got an error from saveItem use it instead
		if (elist.size() > 0)
			err = elist.get(0);
		if (err != null) {
			setErrMessage(messageLocator.getMessage("simplepage.savefailed") + err);
			return false;
		}
		
		return true;
	}

    // The permissions model assumes that all code operates on the current
    // page. When the current page is set, the set code verifies that the
    // page is in the current site. However when operating on items, we
    // have to make sure they are in the current page, or we could end up
    // hacking on an item in a completely different site. This method checks
    // that an item is OK to hack on, given the current page.

	private boolean itemOk(Long itemId) {
		// not specified, we'll add a new one
		if (itemId == null || itemId == -1)
			return true;
		SimplePageItem item = findItem(itemId);
		if (item.getPageId() != getCurrentPageId()) {
			return false;
		}
		return true;
	}

    // called by the producer that uses FCK to update a text block
	public String submit() {
		String rv = "success";
		
		if (!itemOk(itemId))
		    return "permission-failed";
		
		if (canEditPage()) {
			Placement placement = toolManager.getCurrentPlacement();

			StringBuilder error = new StringBuilder();

			// there's an issue with HTML security in the Sakai community.
			// a lot of people feel users shouldn't be able to add javascript, etc
			// to their HTML. I think enforcing that makes Sakai less than useful.
			// So check config options to see whether to do that check
			String html = contents;
			if (getCurrentPage().getOwner() != null || filterHtml 
					&& !"false".equals(placement.getPlacementConfig().getProperty("filterHtml")) ||
					"true".equals(placement.getPlacementConfig().getProperty("filterHtml"))) {
				html = FormattedText.processFormattedText(contents, error);
			} else {
				html = FormattedText.processHtmlDocument(contents, error);
			}
			
			if (html != null) {
				SimplePageItem item;
				// itemid -1 means we're adding a new item to the page, 
				// specified itemid means we're updating an existing one
				if (itemId != null && itemId != -1) {
					item = findItem(itemId);
				} else {
					item = appendItem("", "", SimplePageItem.TEXT);
				}

				item.setHtml(html);
				setItemGroups(item, selectedGroups);
				update(item);
			} else {
				rv = "cancel";
			}

			placement.save();
		} else {
			rv = "cancel";
		}

		return rv;
	}

	public String cancel() {
		return "cancel";
	}

	public String processMultimedia() {
	    return processResource(SimplePageItem.MULTIMEDIA, false);
	}

	public String processResource() {
	    return processResource(SimplePageItem.RESOURCE, false);
	}

        public String processWebSite() {
	    return processResource(SimplePageItem.RESOURCE, true);
	}

    // get mime type for a URL. connect to the server hosting
    // it and ask them. Sorry, but I don't think there's a better way
	public String getTypeOfUrl(String url) {
	    String mimeType = null;

	    // try to find the mime type of the remote resource
	    // this is only likely to be a problem if someone is pointing to
	    // a url within Sakai. We think in realistic cases those that are
	    // files will be handled as files, so anything that comes where
	    // will be HTML. That's the default if this fails.
	    try {
		URLConnection conn = new URL(url).openConnection();
		conn.setConnectTimeout(30000);
		conn.setReadTimeout(30000);
		// generate cookie based on code in  RequestFilter.java
		//String suffix = System.getProperty("sakai.serverId");
		//if (suffix == null || suffix.equals(""))
		//    suffix = "sakai";
		//Session s = sessionManager.getCurrentSession();
		//conn.setRequestProperty("Cookie", "JSESSIONID=" + s.getId() + "." + suffix);
		conn.connect();
		String t = conn.getContentType();
		if (t != null && !t.equals("")) {
		    int i = t.indexOf(";");
		    if (i >= 0)
			t = t.substring(0, i);
		    t = t.trim();
		    mimeType = t;
		}
		conn.getInputStream().close();
	    } catch (Exception e) {log.error("getTypeOfUrl connection error " + e);};
	    return mimeType;
	}

    // return call from the file picker, used by add resource
    // the picker communicates with us by session variables
	public String processResource(int type, boolean isWebSite) {
		if (!canEditPage())
		    return "permission-failed";

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		List refs = null;
		String id = null;
		String name = null;
		String mimeType = null;
		String description = null;

		if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null && toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {

			refs = (List) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			if (refs == null || refs.size() != 1) {
				return "no-reference";
			}
			Reference ref = (Reference) refs.get(0);
			id = ref.getId();
			
			description = ref.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
			
			name = ref.getProperties().getProperty("DAV:displayname");

			// URLs are complex. There are two issues:
			// 1) The stupid helper treats a URL as a file upload. Have to make it a URL type.
			// I suspect we're intended to upload a file from the URL, but I don't think
			// any part of Sakai actually does that. So we reset Sakai's file type to URL
			// 2) Lesson builder needs to know the mime type, to know how to set up the
			// OBJECT or IFRAME. We send that out of band in the "html" field of the 
			// lesson builder item entry. I see no way to do that other than to talk
			// to the server at the other end and see what MIME type it claims.
			mimeType = ref.getProperties().getProperty("DAV:getcontenttype");
			if (mimeType.equals("text/url")) {
			        mimeType = null; // use default rules if we can't find it
				String url = null;
				// part 1, fix up the type fields
				boolean pushed = false;
				try {
					pushed = pushAdvisor();
					ContentResourceEdit res = contentHostingService.editResource(id);
					res.setContentType("text/url");
					res.setResourceType("org.sakaiproject.content.types.urlResource");
					url = new String(res.getContent());
					contentHostingService.commitResource(res, NotificationService.NOTI_NONE);
				} catch (Exception ignore) {
					return "no-reference";
				}finally {
					if(pushed) popAdvisor();
				}
				// part 2, find the actual data type.
				if (url != null)
				    mimeType = getTypeOfUrl(url);
			}

		} else {
			return "cancel";
		}

		boolean pushed = false;
		try {
			pushed = pushAdvisor();
			contentHostingService.checkResource(id);
		} catch (PermissionException e) {
			return "permission-exception";
		} catch (IdUnusedException e) {
			// Typically Means Cancel
			return "cancel";
		} catch (TypeException e) {
			return "type-exception";
		}finally {
			if(pushed) popAdvisor();
		}

		Long itemId = (Long)toolSession.getAttribute(LESSONBUILDER_ITEMID);

		if (!itemOk(itemId))
		    return "permission-failed";

		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
		toolSession.removeAttribute(LESSONBUILDER_ITEMID);

		String[] split = id.split("/");

		if("application/zip".equals(mimeType) && isWebSite) {
		    // We need to set the sakaiId to the resource id of the index file
		    id = expandZippedResource(id);
		    if (id == null)
			return "failed";
		    
		    // We set this special type for the html field in the db. This allows us to
		    // map an icon onto website links in applicationContext.xml
		    // originally it was a special type. The problem is that this is actually
		    // an HTML file, and we may have trouble if we don't show it that way
		    mimeType = "LBWEBSITE";
		    // strip .ZIP off the name
		    if (name == null) {
			name = split[split.length - 1];
		    }
		    if (name.lastIndexOf(".") > 0)
			name = name.substring(0,name.lastIndexOf("."));
		}

		SimplePageItem i;
		if (itemId != null && itemId != -1) {
			i = findItem(itemId);
			i.setSakaiId(id);
			if (mimeType != null)
				i.setHtml(mimeType);
			i.setName(name != null ? name : split[split.length - 1]);
			clearImageSize(i);
		} else {
			i = appendItem(id, (name != null ? name : split[split.length - 1]), type);
			if (mimeType != null) {
				i.setHtml(mimeType);
			}
		}
		
		i.setDescription(description);
		i.setSameWindow(false);
		update(i);

		return "importing";
	}

    // set default for image size for new objects
        private void clearImageSize(SimplePageItem i) {
        		// defaults to a fixed width and height, appropriate for some things, but for an
		// image, leave it blank, since browser will then use the native size
	        if (i.getType() == SimplePageItem.MULTIMEDIA) {
		    if (isImageType(i)) {
			i.setHeight("");
			i.setWidth("");
		    }
		}
	}

    // main code for adding a new item to a page
	private SimplePageItem appendItem(String id, String name, int type)   {
	    // add at the end of the page
	        List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());
		// ideally the following should be the same, but there can be odd cases. So be safe
		int size = items.size();
		if (size > 0) {
		    int seq = items.get(size-1).getSequence();
		    if (seq > size)
			size = seq;
		}
		size++;

		SimplePageItem i = simplePageToolDao.makeItem(getCurrentPageId(), size, type, id, name);

		// defaults to a fixed width and height, appropriate for some things, but for an
		// image, leave it blank, since browser will then use the native size
		clearImageSize(i);

		saveItem(i);
		return i;
	}

	/**
	 * Returns 0 if user has site.upd or simplepage.upd.
	 * Returns 1 if user is page owner
	 * Returns 2 otherwise
	 * @return
	 */
	public int getEditPrivs() {
		if(editPrivs != null) {
			return editPrivs;
		}
		editPrivs = 2;
		String ref = "/site/" + getCurrentSiteId();
		boolean ok = securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
		if(ok) editPrivs = 0;

		SimplePage page = getCurrentPage();
		if(editPrivs != 0 && page != null && getCurrentUserId().equals(page.getOwner())) {
			editPrivs = 1;
		}
		
		return editPrivs;
	}
	
	/**
	 * Returns true if user has site.upd, simplepage.upd, or is page owner.
	 * False otherwise.
	 * @return
	 */
	public boolean canEditPage() {
		if(getEditPrivs() <= 1) {
			return true;
		}else {
			return false;
		}
	}

	public boolean canReadPage() {
		String ref = "/site/" + getCurrentSiteId();
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	}
	public boolean canEditSite() {
		String ref = "/site/" + getCurrentSiteId();
		return securityService.unlock("site.upd", ref);
	}


	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}

	public void setSecurityService(SecurityService service) {
		securityService = service;
	}

	public void setSiteService(SiteService service) {
		siteService = service;
	}

	public void setSimplePageToolDao(Object dao) {
		simplePageToolDao = (SimplePageToolDao) dao;
	}

	public List<SimplePageItem>  getItemsOnPage(long pageid) {
		List<SimplePageItem>items = itemsCache.get(pageid);
		if (items != null)
		    return items;

		items = simplePageToolDao.findItemsOnPage(pageid);
		
		// This code adds a global comments tool to the bottom of each
		// student page, but only if there's something else on the page
		// already and the instructor has enabled the option.
		if(items.size() > 0) {
			SimplePage page = getPage(pageid);
			if(page.getOwner() != null) {
				SimpleStudentPage student = simplePageToolDao.findStudentPage(page.getTopParent());
				if(student != null && student.getCommentsSection() != null) {
					SimplePageItem item = simplePageToolDao.findItem(student.getItemId());
					if(item != null && item.getShowComments() != null && item.getShowComments()) {
						items.add(0, simplePageToolDao.findItem(student.getCommentsSection()));
					}
				}
			}
		}
		
		for (SimplePageItem item: items) {
		    itemCache.put(item.getId(), item);
		}
		
		itemsCache.put(pageid, items);
		return items;
	}

	public String deleteItem()  {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		SimplePageItem i = findItem(itemId);

		int seq = i.getSequence();

		boolean b = false;

		// if access controlled, clear it before deleting item
		if (i.isPrerequisite()) {
		    i.setPrerequisite(false);
		    checkControlGroup(i, false);
		}
		
		// Also delete gradebook entries
		if(i.getGradebookId() != null) {
			gradebookIfc.removeExternalAssessment(getCurrentSiteId(), i.getGradebookId());
		}
		
		if(i.getAltGradebook() != null) {
			gradebookIfc.removeExternalAssessment(getCurrentSiteId(), i.getAltGradebook());
		}
		
		
		b = simplePageToolDao.deleteItem(i);
		
		if (b) {
			List<SimplePageItem> list = getItemsOnPage(getCurrentPageId());
			for (SimplePageItem item : list) {
				if (item.getSequence() > seq) {
					item.setSequence(item.getSequence() - 1);
					update(item);
				}
			}

			return "successDelete";
		} else {
			log.warn("deleteItem error deleting Item: " + itemId);
			return "failure";
		}
	}

    // not clear whether it's worth caching this. The first time it's called for a site
    // the pages are fetched. Beyond that it's a linear search of pages that are in memory
    // ids are sakai.assignment.grades, sakai.samigo, sakai.mneme, sakai.forums, sakai.jforum.tool
	public String getCurrentTool(String commonToolId) {
		Site site = getCurrentSite();
		ToolConfiguration tool = site.getToolForCommonId(commonToolId);
		if (tool == null)
			return null;
		return tool.getId();
	}

	public String getCurrentToolTitle(String commonToolId) {
		Site site = getCurrentSite();
		ToolConfiguration tool = site.getToolForCommonId(commonToolId);
		if (tool == null)
			return null;
		return tool.getTitle();
	}

	private Site getCurrentSite() {
		if (currentSite != null) // cached value
			return currentSite;
		
		try {
		    currentSite = siteService.getSite(getCurrentSiteId());
		} catch (Exception impossible) {
			impossible.printStackTrace();
		}
		
		return currentSite;
	}

    // find page to show in next link
    // If the current page is a LB page, and it has a single "next" link on it, use that

    //  If the current page is a LB page, and it has more than one
    //  "next" link on it, show no next. If there's more than one
    //  next, this is probably a page with a branching question, in
    //  which case there really isn't a single next.

    // If the current page is a LB page, and it is not finished (i.e.
    // there are required items not done), there is no next, or next
    // is grayed out.
    
    //  Otherwise look at the page above in the breadcrumbs. If the
    //  next item on the page is not an inline item, and the item is
    //  available, next should be the next item on that page. (If
    //  it's an inline item we need to go back to the page above so
    //  they can see the inline item next.)

    // If the current page is something like a test, there is an
    // issue. What if the next item is not available when the page is
    // displayed, because it requires that you get a passing score on
    // the current test? For the moment, if the current item is required
    // but the next is not available, show the link but test it when it's
    // clicked.

    // TODO: showpage and showitem, implement next. Should not pass a
    // path argument. That gives next. If there's a pop we do it.
    //    in showitem, check if it's available, if not, show an error
    // with a link to the page above.

    // return: new item on same level, null if none, the item arg if need to go up a level
    //   java really needs to be able to return more than one thing, item == item is being
    //   used as a flag to return up a level
	public SimplePageItem findNextPage(SimplePageItem item) {
		if(item.getType() == SimplePageItem.PAGE) {
			Long pageId = Long.valueOf(item.getSakaiId());
			List<SimplePageItem> items = getItemsOnPage(pageId);
			int nexts = 0;
			SimplePageItem nextPage = null;
			for (SimplePageItem i: items) {
				if (i.getType() == SimplePageItem.PAGE && i.getNextPage()) {
					nextPage = i;
					nexts++;
				}
			}
			// if next, use it; no next if not ready
			if (nexts == 1) {
				if (isItemAvailable(nextPage, pageId))
					return nextPage;
				return null;
			}
			// more than one, presumably you're intended to pick one of them, and
			// there is no generic next
			if (nexts > 1) {
				return null;
			}

			// if this is a next page, if there's no explicIt next it's
			// not clear that it makes sense to go anywhere. it's kind of
			// detached from its parent
			if (item.getNextPage())
				return null;

			// here for a page with no explicit next. Treat like any other item
			// except that we need to compute path op. Page must be complete or we
			// would have returned null.

		}else if(item.getType() == SimplePageItem.STUDENT_CONTENT) {
			return null;
		}

		// this should be a top level page. We're not currently doing next for that.
		// we have to trap it because now and then we have items with bogus 0 page ID, so we
		// could get a spurious next item
		if (item.getPageId() == 0L)
			return null;

		// see if there's an actual next we can go to, otherwise calling page
		SimplePageItem nextItem = simplePageToolDao.findNextItemOnPage(item.getPageId(), item.getSequence());

		// skip items which won't show because user isn't in the group
		while (nextItem != null && !isItemVisible(nextItem)) {
			nextItem = simplePageToolDao.findNextItemOnPage(nextItem.getPageId(), nextItem.getSequence());		
		}

		boolean available = false;
		if (nextItem != null) {

			int itemType = nextItem.getType();
			if (itemType == SimplePageItem.ASSIGNMENT ||
					itemType == SimplePageItem.ASSESSMENT ||
					itemType == SimplePageItem.FORUM ||
					itemType == SimplePageItem.PAGE ||
			                itemType == SimplePageItem.BLTI ||
					itemType == SimplePageItem.RESOURCE && nextItem.isSameWindow()) {
				// it's easy if the next item is available. If it's not, then
				// we need to see if everything other than this item is done and
				// this one is required. In that case the issue must be that this
				// one isn't finished yet. Let's assume the user is going to finish
				// this one. We'll verify that when he actually does the next;
				if (isItemAvailable(nextItem, item.getPageId()) ||
						item.isRequired() && wouldItemBeAvailable(item, item.getPageId()))
					return nextItem;
			}
		}

		// otherwise return to calling page
		return item; // special flag
	}

    // corresponding code for outputting the link
    // perhaps I should adjust the definition of path so that normal items show on it and not just pages
    //   but at the moment path is just the pages. So when we're in a normal item, it doesn't show.
    //   that means that as we do Next between items and pages, when we go to a page it gets pushed
    //   on and when we go from a page to an item, the page has to be popped off.
	public void addNextLink(UIContainer tofill, SimplePageItem item) {
		SimplePageItem nextItem = findNextPage(item);
		if (nextItem == item) { // that we need to go up a level
			List<PathEntry> path = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_PATH);
			int top;
			if (path == null)
				top = -1;
			else
				top = path.size()-1;
		// if we're on a page, have to pop it off first
		// for a normal item the end of the path already is the page above
			if (item.getType() == SimplePageItem.PAGE)
				top--;
			if (top >= 0) {
				PathEntry e = path.get(top);
				GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
				view.setSendingPage(e.pageId);
				view.setItemId(e.pageItemId);
				view.setPath(Integer.toString(top));
				UIInternalLink.make(tofill, "next", messageLocator.getMessage("simplepage.next"), view);
				UIInternalLink.make(tofill, "next1", messageLocator.getMessage("simplepage.next"), view);
			}
	    } else  if (nextItem != null) {
	    	GeneralViewParameters view = new GeneralViewParameters();
	    	int itemType = nextItem.getType();
	    	if (itemType == SimplePageItem.PAGE) {
	    		view.setSendingPage(Long.valueOf(nextItem.getSakaiId()));
	    		view.viewID = ShowPageProducer.VIEW_ID;
	    		if (item.getType() == SimplePageItem.PAGE)
	    			view.setPath("next");  // page to page, just a next
	    		else
	    			view.setPath("push");  // item to page, have to push the page
	    	} else if (itemType == SimplePageItem.RESOURCE) { /// must be a same page resource
	    		view.setSendingPage(Long.valueOf(item.getPageId()));
	    		// to the check. We need the check to set access control appropriately
	    		// if the user has passed.
	    		if (!isItemAvailable(nextItem, nextItem.getPageId()))
	    			view.setRecheck("true");
	    		view.setSource(nextItem.getItemURL(getCurrentSiteId(), getCurrentPage().getOwner()));
	    		view.viewID = ShowItemProducer.VIEW_ID;
	    	} else {
	    		view.setSendingPage(Long.valueOf(item.getPageId()));
	    		LessonEntity lessonEntity = null;
	    		switch (nextItem.getType()) {
	    			case SimplePageItem.ASSIGNMENT:
	    				lessonEntity = assignmentEntity.getEntity(nextItem.getSakaiId()); break;
	    			case SimplePageItem.ASSESSMENT:
	    				view.setClearAttr("LESSONBUILDER_RETURNURL_SAMIGO");
	    				lessonEntity = quizEntity.getEntity(nextItem.getSakaiId()); break;
	    			case SimplePageItem.FORUM:
	    				lessonEntity = forumEntity.getEntity(nextItem.getSakaiId()); break;
	    			case SimplePageItem.BLTI:
				        if (bltiEntity != null)
					    lessonEntity = bltiEntity.getEntity(nextItem.getSakaiId()); break;
	    		}
	    		// normally we won't send someone to an item that
	    		// isn't available. But if the current item is a test, etc, we can't
	    		// know whether the user will pass it, so we have to ask ShowItem to
	    		// to the check. We need the check to set access control appropriately
	    		// if the user has passed.
	    		if (!isItemAvailable(nextItem, nextItem.getPageId()))
	    			view.setRecheck("true");
	    			view.setSource((lessonEntity==null)?"dummy":lessonEntity.getUrl());
	    			if (item.getType() == SimplePageItem.PAGE)
	    				view.setPath("pop");  // now on a have, have to pop it off
	    			view.viewID = ShowItemProducer.VIEW_ID;
	    	}
	    	
	    	view.setItemId(nextItem.getId());
	    	view.setBackPath("push");
	    	UIInternalLink.make(tofill, "next", messageLocator.getMessage("simplepage.next"), view);
	    	UIInternalLink.make(tofill, "next1", messageLocator.getMessage("simplepage.next"), view);
	    }
	}

    // Because of the existence of chains of "next" pages, there's no static approach that will find
    // back links. Thus we keep track of the actual path the user has followed. However we have to
    // prune both path and back path when we return to an item that's already on them to avoid
    // loops of various kinds.

	public void addPrevLink(UIContainer tofill, SimplePageItem item) {
		List<PathEntry> backPath = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_BACKPATH);
		List<PathEntry> path = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_PATH);

		// current item is last on path, so need one before that
		if (backPath == null || backPath.size() < 2)
			return;

		PathEntry prevEntry = backPath.get(backPath.size()-2);
		SimplePageItem prevItem = findItem(prevEntry.pageItemId);

		GeneralViewParameters view = new GeneralViewParameters();
		int itemType = prevItem.getType();
		if (itemType == SimplePageItem.PAGE) {
			view.setSendingPage(Long.valueOf(prevItem.getSakaiId()));
			view.viewID = ShowPageProducer.VIEW_ID;
			// are we returning to a page? If so use existing path entry
			int lastEntry = -1;
			int i = 0;
			long prevItemId = prevEntry.pageItemId;
			for (PathEntry entry: path) {
				if (entry.pageItemId == prevItemId)
					lastEntry = i;
				i++;
			}
			if (lastEntry >= 0)
				view.setPath(Integer.toString(lastEntry));
			else if (item.getType() == SimplePageItem.PAGE)
				view.setPath("next");  // page to page, just a next
			else
				view.setPath("push");  // item to page, have to push the page
		} else if (itemType == SimplePageItem.RESOURCE) { // must be a samepage resource
			view.setSendingPage(Long.valueOf(item.getPageId()));
			view.setSource(prevItem.getItemURL(getCurrentSiteId(),getCurrentPage().getOwner()));
			view.viewID = ShowItemProducer.VIEW_ID;
		}else if(itemType == SimplePageItem.STUDENT_CONTENT) {
			view.setSendingPage(prevEntry.pageId);
			view.setItemId(prevEntry.pageItemId);
			view.viewID =ShowPageProducer.VIEW_ID;
			
			if(item.getType() == SimplePageItem.PAGE) {
				view.setPath("pop");
			}else {
				view.setPath("next");
			}
		} else {
			view.setSendingPage(Long.valueOf(item.getPageId()));
			LessonEntity lessonEntity = null;
			switch (prevItem.getType()) {
			case SimplePageItem.ASSIGNMENT:
				lessonEntity = assignmentEntity.getEntity(prevItem.getSakaiId()); break;
			case SimplePageItem.ASSESSMENT:
				view.setClearAttr("LESSONBUILDER_RETURNURL_SAMIGO");
				lessonEntity = quizEntity.getEntity(prevItem.getSakaiId()); break;
			case SimplePageItem.FORUM:
				lessonEntity = forumEntity.getEntity(prevItem.getSakaiId()); break;
			case SimplePageItem.BLTI:
				if (bltiEntity != null)
				    lessonEntity = bltiEntity.getEntity(prevItem.getSakaiId()); break;
			}
			view.setSource((lessonEntity==null)?"dummy":lessonEntity.getUrl());
			if (item.getType() == SimplePageItem.PAGE)
				view.setPath("pop");  // now on a page, have to pop it off
			view.viewID = ShowItemProducer.VIEW_ID;
		}
		view.setItemId(prevItem.getId());
		view.setBackPath("pop");
		UIInternalLink.make(tofill, "prev", messageLocator.getMessage("simplepage.back"), view);
		UIInternalLink.make(tofill, "prev1", messageLocator.getMessage("simplepage.back"), view);
	}

	public String getCurrentSiteId() {
		if (currentSiteId != null)
		    return currentSiteId;
		try {
		    currentSiteId = toolManager.getCurrentPlacement().getContext();
		    return currentSiteId;
		} catch (Exception impossible) {
		    return null;
		}
	}

    // so access can inject the siteid
	public void setCurrentSiteId(String siteId) {       
		currentSiteId = siteId;
	}

    // recall that code typically operates on a "current page." See below for
    // the code that sets a new current page. We also have a current item, which
    // is the item defining the page. I.e. if the page is a subpage of another
    // one, this is the item on the parent page pointing to this page.  If it's
    // a top-level page, it's a dummy item.  The item is needed in order to do
    // access checks. Whether an item is required, etc, is stored in the item.
    // in theory a page could be called from several other pages, with different
    // access control parameters. So we need to know the actual item on the page
    // page from which this page was called.

    // we need to track the pageitem because references to the same page can appear
    // in several places. In theory each one could have different status of availability
    // so we need to know which in order to check availability
	public void updatePageItem(long item) throws PermissionException {
		SimplePageItem i = findItem(item);
		if (i != null) {
			if (i.getType() != SimplePageItem.STUDENT_CONTENT && (long)currentPageId != (long)Long.valueOf(i.getSakaiId())) {
				log.warn("updatePageItem permission failure " + i + " " + Long.valueOf(i.getSakaiId()) + " " + currentPageId);
				throw new PermissionException(getCurrentUserId(), "set item", Long.toString(item));
			}
		}

		currentPageItemId = item;
		sessionManager.getCurrentToolSession().setAttribute("current-pagetool-item", item);
	}

    // update our concept of the current page. it is imperative to make sure the page is in
    // the current site, or people could hack on other people's pages

    // if you call updatePageObject, consider whether you need to call updatePageItem as well
    // this combines two functions, so maybe not, but any time you're going to a new page 
    // you should do both. Make sure all Producers set the page to the one they will work on
	public void updatePageObject(long l, boolean save) throws PermissionException {
		if (l != previousPageId) {
			currentPage = getPage(l);
			String siteId = getCurrentSiteId();
			
			// get a rare error here, trying to debug it
			if(currentPage == null || currentPage.getSiteId() == null) {
			    throw new PermissionException(getCurrentUserId(), "set page", Long.toString(l));
			}
			
			// page should always be in this site, or someone is gaming us
			if (!currentPage.getSiteId().equals(siteId))
			    throw new PermissionException(getCurrentUserId(), "set page", Long.toString(l));
			previousPageId = l;
			
			if(save) {
				sessionManager.getCurrentToolSession().setAttribute("current-pagetool-page", l);
			}
			
			currentPageId = (Long)l;
		}
	}
	
	public void updatePageObject(long l) throws PermissionException {
		updatePageObject(l, true);
	}

    // if tool was reset, return last page from previous session, so we can give the user
    // a chance to go back
	public SimplePageToolDao.PageData toolWasReset() {
		if (sessionManager.getCurrentToolSession().getAttribute("current-pagetool-page") == null) {
			// no page in session, which means it was reset
			String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
			return simplePageToolDao.findMostRecentlyVisitedPage(getCurrentUserId(), toolId);
		} else
			return null;
	}

    // ought to be simple, but this is typically called at the beginning of a producer, when
    // the current page isn't set yet. So if there isn't one, we use the session variable
    // to tell us what the current page is. Note that a user can add our tool using Site
    // Info. Site info knows nothing about us, so it will make an entry for the page without
    // creating it. When the user then tries to go to the page, this code will be the firsst
    // to notice it. Hence we have to create pages that don't exist
	private long getCurrentPageId()  {
		// return ((ToolConfiguration)toolManager.getCurrentPlacement()).getPageId();

		if (currentPageId != null)
		    return (long)currentPageId;

		Placement placement = toolManager.getCurrentPlacement();
		// See whether the tool is disabled in Sakai site information
		// you can either hide or disable a tool. Our page hidden is
		// really a disable, so we sync Sakai's disabled with our hidden
		// we're only checking when you first go into a tool
		Properties roleConfig = placement.getPlacementConfig();
		String roleList = roleConfig.getProperty("functions.require");
		boolean siteHidden = (roleList != null && roleList.indexOf(SITE_UPD) > -1);

		// Let's go back to where we were last time.
		Long l = (Long) sessionManager.getCurrentToolSession().getAttribute("current-pagetool-page");
		if (l != null && l != 0) {
			try {
				updatePageObject(l);
				Long i = (Long) sessionManager.getCurrentToolSession().getAttribute("current-pagetool-item");
				if (i != null && i != 0)
					updatePageItem(i);
			} catch (PermissionException e) {
			    e.printStackTrace();
				log.warn("getCurrentPageId Permission failed setting to item in toolsession");
				return 0;
			}

			// currentPage should now be set
			syncHidden(currentPage, siteHidden);

			return l;
		} else {
			// No recent activity. Let's go to the top level page.

			l = simplePageToolDao.getTopLevelPageId(((ToolConfiguration) placement).getPageId());;
			// l = simplePageToolDao.getTopLevelPageId(((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId());

			if (l != null) {
				try {
					updatePageObject(l);
					// this should exist except if the page was created by old code
					SimplePageItem i = simplePageToolDao.findTopLevelPageItemBySakaiId(String.valueOf(l));
					if (i == null) {
						// and dummy item, the site is the notional top level page
						i = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, l.toString(), currentPage.getTitle());
						saveItem(i);
					}
					updatePageItem(i.getId());
				} catch (PermissionException e) {
					log.warn("getCurrentPageId Permission failed setting to page in toolsession");
					return 0;
				}

				// currentPage should now be set
				syncHidden(currentPage, siteHidden);

				return l;
			} else {
				// No page found. Let's make a new one.
				String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
				String title = getCurrentSite().getPage(toolId).getTitle(); // Use title supplied

				// during creation
				SimplePage page = simplePageToolDao.makePage(toolId, getCurrentSiteId(), title, null, null);
				if (!saveItem(page)) {
					currentPage = null;
					return 0;
				}
				
				try {
					updatePageObject(page.getPageId());
					l = page.getPageId();

					// and dummy item, the site is the notional top level page
					SimplePageItem i = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, l.toString(), title);
					saveItem(i);
					updatePageItem(i.getId());
				} catch (PermissionException e) {
					log.warn("getCurrentPageId Permission failed setting to new page");
					return 0;
				}

				// currentPage should now be set
				syncHidden(currentPage, siteHidden);

				return l;
			}
		}
	}

        private void syncHidden (SimplePage page, boolean siteHidden) {
	    // only do it for top level pages
	    if (page != null && page.getParent() == null) {
		// hidden in site
		if (siteHidden != page.isHidden()) {
		    page.setHidden(siteHidden);
		    // use quick, as we don't want permission check. even normal users can do this
		    simplePageToolDao.quickUpdate(page);
		}
	    }
	}

	public void setCurrentPageId(long p) {
	    currentPageId = p;
	}

        // current page must be set. 

	public SimplePageItem getCurrentPageItem(Long itemId)  {
		// if itemId is known, this is easy. but check to make sure it's
	        // actually this page, to prevent the user gaming us
		
		if (itemId == null || itemId == -1) 
			itemId = currentPageItemId;
		
		if (itemId != null && itemId != -1) {
			SimplePageItem ret = findItem(itemId);
			if (ret != null && (ret.getSakaiId().equals(Long.toString(getCurrentPageId())) || ret.getType() == SimplePageItem.STUDENT_CONTENT)) {
				try {
					updatePageItem(ret.getId());
				} catch (PermissionException e) {
					log.warn("getCurrentPageItem Permission failed setting to specified item");
					return null;
				}
				return ret;
			} else {
				return null;
			}
		}
		// else must be a top level item
		SimplePage page = getPage(getCurrentPageId());
		
		SimplePageItem ret = simplePageToolDao.findTopLevelPageItemBySakaiId(Long.toString(getCurrentPageId()));
		
		if(ret == null && page.getOwner() != null) {
			ret = simplePageToolDao.findItemFromStudentPage(page.getPageId());
		}
		try {
			updatePageItem(ret.getId());
		} catch (PermissionException e) {
			log.warn("getCurrentPageItem Permission failed setting to top level page in tool session");
			return null;
		}
		return ret;
	}

    // called at the start of showpageproducer, with page info for the page about to be displayed
    // updates the breadcrumbs, which are kept in session variables.
    // returns string version of the new path

	public String adjustPath(String op, Long pageId, Long pageItemId, String title) {
		List<PathEntry> path = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_PATH);

		// if no current path, op doesn't matter. we can just do the current page
		if (path == null || path.size() == 0) {
			PathEntry entry = new PathEntry();
			entry.pageId = pageId;
			entry.pageItemId = pageItemId;
			entry.title = title;
			path = new ArrayList<PathEntry>();
			path.add(entry);
	    } else if (path.get(path.size()-1).pageId.equals(pageId)) {
	    	// nothing. we're already there. this is to prevent 
	    	// oddities if we refresh the page
	    } else if (op == null || op.equals("") || op.equals("next")) {
	    	PathEntry entry = path.get(path.size()-1); // overwrite last item
	    	entry.pageId = pageId;
	    	entry.pageItemId = pageItemId;
	    	entry.title = title;
	    } else if (op.equals("push")) {
	    	// a subpage
	    	PathEntry entry = new PathEntry();
	    	entry.pageId = pageId;
	    	entry.pageItemId = pageItemId;
	    	entry.title = title;
	    	path.add(entry);  // put it on the end
	    } else if (op.equals("pop")) {
	    	// a subpage
	    	path.remove(path.size()-1);
	    } else if (op.startsWith("log")) {
	    	// set path to what was saved in the last log entry for this item
	    	// this is used for users who go directly to a page from the 
	    	// main list of pages.
	    	path = new ArrayList<PathEntry>();
	    	SimplePageLogEntry logEntry = getLogEntry(pageItemId);
	    	if (logEntry != null) {
	    		String items[] = null;
	    		if (logEntry.getPath() != null)
				items = split(logEntry.getPath(), ",");
	    		if (items != null) {
	    			for(String s: items) {
	    				// don't see how this could happen, but it did
	    				if (s.trim().equals("")) {
	    					log.warn("adjustPath attempt to set invalid path: invalid item: " + op + ":" + logEntry.getPath());
	    					return null;
	    				}
	    				SimplePageItem i = findItem(Long.valueOf(s));
	    				if (i == null || i.getType() != SimplePageItem.PAGE) {
	    					log.warn("adjustPath attempt to set invalid path: invalid item: " + op);
	    					return null;
	    				}
	    				SimplePage p = getPage(Long.valueOf(i.getSakaiId()));
	    				if (p == null || !currentPage.getSiteId().equals(p.getSiteId())) {
	    					log.warn("adjustPath attempt to set invalid path: invalid page: " + op);
	    					return null;
	    				}
	    				PathEntry entry = new PathEntry();
	    				entry.pageId = p.getPageId();
	    				entry.pageItemId = i.getId();
	    				entry.title = i.getName();
	    				path.add(entry);
	    			}
	    		}
	    	}
	    } else {
	    	int index = Integer.valueOf(op); // better be number
	    	if (index < path.size()) {
	    		// if we're going back, this should actually
	    		// be redundant
	    		PathEntry entry = path.get(index); // back to specified item
	    		entry.pageId = pageId;
	    		entry.pageItemId = pageItemId;
	    		entry.title = title;
	    		if (index < (path.size()-1))
	    			path.subList(index+1, path.size()).clear();
	    	}
	    }

	    // have new path; set it in session variable
	    sessionManager.getCurrentToolSession().setAttribute(LESSONBUILDER_PATH, path);

	    // and make string representation to return
	    String ret = null;
	    for (PathEntry entry: path) {
	    	String itemString = Long.toString(entry.pageItemId);
	    	if (ret == null)
	    		ret = itemString;
	    	else
	    		ret = ret + "," + itemString;
	    }
	    if (ret == null)
	    	ret = "";
	    return ret;
	}

	public void adjustBackPath(String op, Long pageId, Long pageItemId, String title) {

	    List<PathEntry> backPath = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_BACKPATH);
	    if (backPath == null)
		backPath = new ArrayList<PathEntry>();

	    // default case going directly to something.
	    // normally we want to push it, but if it's already there,
	    // we're going back to it, use the old one
	    if (op == null || op.equals("")) {
		// is it there already? Some would argue that we should use the first occurrence
		int lastEntry = -1;
		int i = 0;
		long itemId = pageItemId;  // to avoid having to use equals
		for (PathEntry entry: backPath) {
		    if (entry.pageItemId == itemId)
			lastEntry = i;
		    i++;
		}
		if (lastEntry >= 0) {
		    // yes, back up to that entry
		    if (lastEntry < (backPath.size()-1))
			backPath.subList(lastEntry+1, backPath.size()).clear();
		    return;
		}
		// no fall through and push the new item
	    }
		

	    if (op.equals("pop")) {
		if (backPath.size() > 0)
		    backPath.remove(backPath.size()-1);
	    } else {  // push or no operation
		PathEntry entry = new PathEntry();
		entry.pageId = pageId;
		entry.pageItemId = pageItemId;
		entry.title = title;
		backPath.add(entry);
	    }

	    // have new path; set it in session variable
	    sessionManager.getCurrentToolSession().setAttribute(LESSONBUILDER_BACKPATH, backPath);
	}

	public void setSubpageTitle(String st) {
		subpageTitle = st;
	}

	public void setSubpageNext(boolean s) {
	    subpageNext = s;
	}

	public void setSubpageButton(boolean s) {
	    subpageButton = s;
	}

    // called from "select page" dialog in Reorder to insert items from anoher page
	public String selectPage()   {

		if (!canEditPage())
		    return "permission-failed";

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		toolSession.setAttribute("lessonbuilder.selectedpage", selectedEntity);

		// doesn't do anything but call back reorder
		// the submit sets selectedEntity, which is passed to Reorder by addResultingViewBinding

		return "selectpage";
	}

    // called from "add subpage" dialog
    // create if itemId == null or -1, else update existing
	public String createSubpage()   {

		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		String title = subpageTitle;
		
		boolean makeNewPage = (selectedEntity == null || selectedEntity.length() == 0);
		boolean makeNewItem = (itemId == null || itemId == -1);

		// make sure the page is legit
		if (!makeNewPage) {
		    SimplePage p = getPage(Long.valueOf(selectedEntity));
		    if (p == null || !getCurrentSiteId().equals(p.getSiteId())) {
			log.warn("addpage tried to add invalid page: " + selectedEntity);
			return "invalidpage";
		    }
		}

		if ((title == null || title.length() == 0) &&
		    (selectedEntity == null || selectedEntity.length() == 0)) {
			return "notitle";
		}

		SimplePage page = getCurrentPage();

		Long parent = page.getPageId();
		Long topParent = page.getTopParent();
		
		// Allows students to make subpages of Student Content pages
		String owner = page.getOwner();
		Boolean groupOwned = page.isGroupOwned();

		if (topParent == null) {
			topParent = parent;
		}

		String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
		SimplePage subpage = null;
		if (makeNewPage) {
		    subpage = simplePageToolDao.makePage(toolId, getCurrentSiteId(), title, parent, topParent);
		    subpage.setOwner(owner);
		    subpage.setGroupOwned(groupOwned);
		    saveItem(subpage);
		    selectedEntity = String.valueOf(subpage.getPageId());
		} else {
		    subpage = getPage(Long.valueOf(selectedEntity));
		}

		SimplePageItem i = null;
		if (makeNewItem)
		    i = appendItem(selectedEntity, subpage.getTitle(), SimplePageItem.PAGE);
		else
		    i = findItem(itemId);

		if (i == null)
		    return "failure";

		if (makeNewItem) {
		    i.setNextPage(subpageNext);
		    if (subpageButton)
			i.setFormat("button");
		    else
			i.setFormat("");
		} else {
		    // when itemid is specified, we're changing pages for existing entry
		    i.setSakaiId(selectedEntity);
		    i.setName(subpage.getTitle());
		}

		update(i);

		if (makeNewPage) {
		    // if creating new entry, go to it
		    try {
			updatePageObject(subpage.getPageId());
			updatePageItem(i.getId());
		    } catch (PermissionException e) {
			log.warn("createSubpage permission failed going to new page");
			return "failed";
		    }
		    adjustPath((subpageNext ? "next" : "push"), subpage.getPageId(), i.getId(), i.getName());

		    submit();

		}

		return "success";
	}


	public String deletePages() {
	    if (getEditPrivs() != 0)
	    	return "permission-failed";

	    String siteId = getCurrentSiteId();

	    for (int i = 0; i < selectedEntities.length; i++) {
	    	SimplePage target = getPage(Long.valueOf(selectedEntities[i]));
	    	if (target != null) {
	    		if (!target.getSiteId().equals(siteId)) {
	    			return "permission-failed";
	    		}
	    		
	    		// delete all the items on the page
	    		List<SimplePageItem> items = getItemsOnPage(target.getPageId());
	    		for (SimplePageItem item: items) {
	    			// if access controlled, clear it before deleting item
	    			if (item.isPrerequisite()) {
	    				item.setPrerequisite(false);
	    				checkControlGroup(item, false);
	    			}
	    			simplePageToolDao.deleteItem(item);
	    		}

	    		// remove from gradebook
	    		gradebookIfc.removeExternalAssessment(siteId, "lesson-builder:" + target.getPageId());
		    
	    		// remove fake item if it's top level. We won't see it if it's still active
	    		// so this means the user has removed it in site info
	    		SimplePageItem item = simplePageToolDao.findTopLevelPageItemBySakaiId(selectedEntities[i]);
	    		if (item != null)
	    			simplePageToolDao.deleteItem(item);			

	    		// currently the UI doesn't allow you to kill top level pages until they have been
	    		// removed by site info, so we don't have to hack on the site pages

	    		// remove page
	    		simplePageToolDao.deleteItem(target);
	    	}
	    }
	    return "success";
	}

    //  remove a top-level page from the left margin. Does not actually delete it.
    //  this and addpages checks only edit page permission. should it check site.upd?
	public String removePage() {
		if (getEditPrivs() != 0) {
			return "permission-failed";
		}
		
		//		if (removeId == 0)
		//		    removeId = getCurrentPageId();
		SimplePage page = getPage(removeId);
		
		if (page == null)
		    return "no-such-page";

		if(page.getOwner() == null) {
		    // this code should never be called
		    return "failure";
		}else {
			SimpleStudentPage studentPage = simplePageToolDao.findStudentPageByPageId(page.getPageId());
			
			if(studentPage != null) {
				studentPage.setDeleted(true);
				update(studentPage, false);
				
				String[] path = split(adjustPath("pop", null, null, null), ",");
				Long itemId = Long.valueOf(path[path.length-1]);
				
				try {
					SimplePageItem item = simplePageToolDao.findItem(itemId);
					updatePageObject(Long.valueOf(item.getSakaiId()));
					updatePageItem(itemId);
				} catch (PermissionException e) {
					return "failure";
				}
				
				return "success";
			}else {
				return "failure";
			}
		}
	}

    // called from "save" in main edit item dialog
	public String editItem() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		if (name.length() < 1) {
			return "Notitle";
		}

		SimplePageItem i = findItem(itemId);
		if (i == null) {
			return "failure";
		} else {
			i.setName(name);
			i.setDescription(description);
			i.setRequired(required);
			i.setPrerequisite(prerequisite);
			i.setSubrequirement(subrequirement);
			i.setNextPage(subpageNext);
			if (subpageButton)
			    i.setFormat("button");
			else
			    i.setFormat("");

			if (points != "") {
				i.setRequirementText(points);
			} else {
				i.setRequirementText(dropDown);
			}

			// currently we only display HTML in the same page
			if (i.getType() == SimplePageItem.RESOURCE)
			    i.setSameWindow(!newWindow);
			else
			    i.setSameWindow(false);

			if (i.getType() == SimplePageItem.BLTI) {
			    if (format == null || format.trim().equals(""))
				i.setFormat("");
			    else
				i.setFormat(format);
			    // this is redundant, but the display code uses it
			    if ("window".equals(format))
				i.setSameWindow(false);
			    else
				i.setSameWindow(true);

			    i.setHeight(height);
			}

			update(i);

			if (i.getType() == SimplePageItem.PAGE) {
				SimplePage page = getPage(Long.valueOf(i.getSakaiId()));
				if (page != null) {
					page.setTitle(name);
					update(page);
				}
			} else {
				checkControlGroup(i, i.isPrerequisite());
			}

			setItemGroups(i, selectedGroups);

			return "successEdit"; // Shouldn't reload page
		}
	}

    // Set access control for an item to the state requested by i.isPrerequisite().
    // This code should depend only upon isPrerequisite() in the item object, not the database,
    // because we call it when deleting or updating items, before saving them to the database.
    // The caller will update the item in the database, typically after this call
    //    correct is correct value, i.e whether it hsould be there or not
	private void checkControlGroup(SimplePageItem i, boolean correct) {
		if (i.getType() == SimplePageItem.RESOURCE) {
		    checkControlResource(i, correct);
		    return;
		}

	    	if (i.getType() != SimplePageItem.ASSESSMENT && 
		    i.getType() != SimplePageItem.ASSIGNMENT && 
		    i.getType() != SimplePageItem.FORUM) {
			// We only do this for assignments and assessments
		        // currently we can't actually set it for forum topics
			return;
		}

		if (i.getSakaiId().equals(SimplePageItem.DUMMY))
		    return;

		SimplePageGroup group = simplePageToolDao.findGroup(i.getSakaiId());
		String ourGroupName = null;
		try {
		    // correct is the correct setting, i.e. if there is supposed to be
		    // a group or not. We only change if reality disagrees with it.
			if (correct) {
				if (group == null) {
					// create our a new access control group, and save the current tool group list with it.
					LessonEntity lessonEntity = null;
					switch (i.getType()) {
					case SimplePageItem.ASSIGNMENT:
					    lessonEntity = assignmentEntity.getEntity(i.getSakaiId()); break;
					case SimplePageItem.ASSESSMENT:
					    lessonEntity = quizEntity.getEntity(i.getSakaiId()); break;
					case SimplePageItem.FORUM:
					    lessonEntity = forumEntity.getEntity(i.getSakaiId()); break;
					}
					if (lessonEntity != null) {
					    String groups = getItemGroupString (i, lessonEntity, true);
					    ourGroupName = "Access: " + getNameOfSakaiItem(i);
					    String groupId = GroupPermissionsService.makeGroup(getCurrentPage().getSiteId(), ourGroupName);
					    saveItem(simplePageToolDao.makeGroup(i.getSakaiId(), groupId, groups));

					    // update the tool access control to point to our access control group
					    
					    String[] newGroups = {groupId};
					    lessonEntity.setGroups(Arrays.asList(newGroups));
					}
				}
			} else {
			    if (group != null) {
				// shouldn't be under control. Delete our access control and put the 
				// groups back into the tool's list

				LessonEntity lessonEntity = null;
				switch (i.getType()) {
				case SimplePageItem.ASSIGNMENT:
				    lessonEntity = assignmentEntity.getEntity(i.getSakaiId()); break;
				case SimplePageItem.ASSESSMENT:
				    lessonEntity = quizEntity.getEntity(i.getSakaiId()); break;
				case SimplePageItem.FORUM:
				    lessonEntity = forumEntity.getEntity(i.getSakaiId()); break;
				}
				if (lessonEntity != null) {
				    String groups = group.getGroups();
				    List<String> groupList = null;
				    if (groups != null && !groups.equals(""))
					groupList = Arrays.asList(groups.split(","));
				    lessonEntity.setGroups(groupList);
				    simplePageToolDao.deleteItem(group);
				}
			    }
			}
		} catch (Exception ex) {
		    ex.printStackTrace();
		}
	}

    // to control a resource, set hidden. /access/lessonbuilder does the actual control
	private void checkControlResource(SimplePageItem i, boolean correct) {
	    String resourceId = i.getSakaiId();

	    if (resourceId != null) {
		try {
		    ContentResource res = contentHostingService.getResource(resourceId);
		    if (res.isHidden() != correct) {
			ContentResourceEdit resEdit = contentHostingService.editResource(resourceId);
			resEdit.setAvailability(correct, resEdit.getReleaseDate(), resEdit.getRetractDate());
			contentHostingService.commitResource(resEdit, NotificationService.NOTI_NONE);
		    }
		} catch (Exception ignore) {}
	    }
	    
	}

	public SimplePage getCurrentPage()  {
		getCurrentPageId();
		return currentPage;
	}

	public void setCurrentPage(SimplePage p) {
	    currentPage = p;
	}

	public String getToolId(String tool) {
		try {
			ToolConfiguration tc = siteService.getSite(currentPage.getSiteId()).getToolForCommonId(tool);
			return tc.getId();
		} catch (IdUnusedException e) {
			// This really shouldn't happen.
		    log.warn("getToolId 1 attempt to get tool config for " + tool + " failed. Tool missing from site?");
		    return null;
		} catch (java.lang.NullPointerException e) {
		    log.warn("getToolId 2 attempt to get tool config for " + tool + " failed. Tool missing from site?");
		    return null;
		}
	}

	public void updateCurrentPage() {
		update(currentPage);
	}

	public List<PathEntry> getHierarchy() {
	    List<PathEntry> path = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_PATH);
	    if (path == null)
		return new ArrayList<PathEntry>();

	    return path;
	}

	public void setSelectedAssignment(String selectedAssignment) {
		this.selectedAssignment = selectedAssignment;
	}

	public void setSelectedEntity(String selectedEntity) {
		this.selectedEntity = selectedEntity;
	}

	public void setSelectedQuiz(String selectedQuiz) {
		this.selectedQuiz = selectedQuiz;
	}

	public void setSelectedBlti(String selectedBlti) {
		this.selectedBlti = selectedBlti;
	}

        public String assignmentRef(String id) {
	    return "/assignment/a/" + getCurrentSiteId() + "/" + id;
	}

    // called by add forum dialog. Create a new item that points to a forum or
    // update an existing item, depending upon whether itemid is set
        public String addForum() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		if (selectedEntity == null) {
			return "failure";
		} else {
			try {
			    LessonEntity selectedObject = forumEntity.getEntity(selectedEntity);
			    if (selectedObject == null) {
				return "failure";
			    }
			    SimplePageItem i;
			    // editing existing item?
			    if (itemId != null && itemId != -1) {
				i = findItem(itemId);
				// if no change, don't worry
				if (!i.getSakaiId().equals(selectedEntity)) {
				    // if access controlled, clear restriction from old assignment and add to new
				    if (i.isPrerequisite()) {
					i.setPrerequisite(false);
					checkControlGroup(i, false);
					// sakaiid and name are used in setting control
					i.setSakaiId(selectedEntity);
					i.setName(selectedObject.getTitle());
					i.setPrerequisite(true);
					checkControlGroup(i, true);
				    } else {
					i.setSakaiId(selectedEntity);
					i.setName(selectedObject.getTitle());
				    }

				    // reset assignment-specific stuff
				    i.setDescription("");
				    update(i);
				}
			    } else {
				// no, add new item
				i = appendItem(selectedEntity, selectedObject.getTitle(), SimplePageItem.FORUM);
				i.setDescription("");
				update(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    ex.printStackTrace();
			    return "failure";
			} finally {
			    selectedEntity = null;
			}
		}

        }

    // called by add assignment dialog. Create a new item that points to an assigment
    // or update an existing item, depending upon whether itemid is set
	public String addAssignment() {
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, new ResourceLoader().getLocale());		
		df.setTimeZone(TimeService.getLocalTimeZone());
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		if (selectedAssignment == null) {
			return "failure";
		} else {
			try {
			    LessonEntity selectedObject = assignmentEntity.getEntity(selectedAssignment);
			    if (selectedObject == null)
				return "failure";

			    SimplePageItem i;
			    // editing existing item?
			    if (itemId != null && itemId != -1) {
				i = findItem(itemId);

				// if no change, don't worry
				LessonEntity existing = assignmentEntity.getEntity(i.getSakaiId());
				String ref = existing.getReference();
				// if same quiz, nothing to do
				if (!ref.equals(selectedAssignment)) {
				    // if access controlled, clear restriction from old assignment and add to new
				    if (i.isPrerequisite()) {
					i.setPrerequisite(false);
					checkControlGroup(i, false);
					// sakaiid and name are used in setting control
					i.setSakaiId(selectedAssignment);
					i.setName(selectedObject.getTitle());
					i.setPrerequisite(true);
					checkControlGroup(i, true);
				    } else {
					i.setSakaiId(selectedAssignment);
					i.setName(selectedObject.getTitle());
				    }
				    // reset assignment-specific stuff
				    if (selectedObject.getDueDate() != null)
					i.setDescription("(" + messageLocator.getMessage("simplepage.due") + " " + df.format(selectedObject.getDueDate()) + ")");
				    else
					i.setDescription(null);
				    update(i);
				}
			    } else {
				// no, add new item
				i = appendItem(selectedAssignment, selectedObject.getTitle(), SimplePageItem.ASSIGNMENT);
				if (selectedObject.getDueDate() != null)
				    i.setDescription("(" + messageLocator.getMessage("simplepage.due") + " " + df.format(selectedObject.getDueDate()) + ")");
				else
				    i.setDescription(null);
				update(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    ex.printStackTrace();
			    return "failure";
			} finally {
			    selectedAssignment = null;
			}
		}
	}

    // called by add blti picker. Create a new item that points to an assigment
    // or update an existing item, depending upon whether itemid is set
	public String addBlti() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		if (selectedBlti == null || bltiEntity == null) {
			return "failure";
		} else {
			try {
			    LessonEntity selectedObject = bltiEntity.getEntity(selectedBlti);
			    if (selectedObject == null)
				return "failure";

			    SimplePageItem i;
			    // editing existing item?
			    if (itemId != null && itemId != -1) {
				i = findItem(itemId);

				// if no change, don't worry
				LessonEntity existing = bltiEntity.getEntity(i.getSakaiId());
				String ref = existing.getReference();
				// if same item, nothing to do
				if (!ref.equals(selectedBlti)) {
				    // if access controlled, clear restriction from old assignment and add to new
				    if (i.isPrerequisite()) {
					i.setPrerequisite(false);
					// sakaiid and name are used in setting control
					i.setSakaiId(selectedBlti);
					i.setName(selectedObject.getTitle());
					i.setPrerequisite(true);
				    } else {
					i.setSakaiId(selectedBlti);
					i.setName(selectedObject.getTitle());
				    }
				    if (format == null || format.trim().equals(""))
					i.setFormat("");
				    else
					i.setFormat(format);

				    // this is redundant, but the display code uses it
				    if ("window".equals(format))
					i.setSameWindow(false);
				    else
					i.setSameWindow(true);

				    i.setHeight(height);
				    setItemGroups(i, selectedGroups);
				    update(i);
				}
			    } else {
				// no, add new item
				i = appendItem(selectedBlti, selectedObject.getTitle(), SimplePageItem.BLTI);
				BltiInterface blti = (BltiInterface)bltiEntity.getEntity(selectedBlti);
				if (blti != null) {
				    int height = blti.frameSize();
				    if (height > 0)
					i.setHeight(Integer.toString(height));
				    else
					i.setHeight("");
				    if (format == null || format.trim().equals(""))
					i.setFormat("");
				    else
					i.setFormat(format);
				}
				update(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    ex.printStackTrace();
			    return "failure";
			} finally {
			    selectedBlti = null;
			}
		}
	}

    /// ShowPageProducers needs the item ID list anyway. So to avoid calling the underlying
    // code twice, we take that list and translate to titles, rather than calling
    // getItemGroups again
	public String getItemGroupTitles(String itemGroups) {
	    if (itemGroups == null || itemGroups.equals(""))
		return null;

	    List<String> groupNames = new ArrayList<String>();
	    Site site = getCurrentSite();
	    String[] groupIds = split(itemGroups, ",");
	    for (int i = 0; i < groupIds.length; i++) {
		Group group=site.getGroup(groupIds[i]);
		if (group != null) {
		    String title = group.getTitle();
		    if (title != null && !title.equals(""))
			groupNames.add(title);
		    else
			groupNames.add(messageLocator.getMessage("simplepage.deleted-group"));
		} else
		    groupNames.add(messageLocator.getMessage("simplepage.deleted-group"));
	    }
	    Collections.sort(groupNames);
	    String ret = "";
	    for (String name: groupNames) {
		if (ret.equals(""))
		    ret = name;
		else
		    ret = ret + "," + name;
	    }

	    return ret;
	}

        public String getItemGroupString (SimplePageItem i, LessonEntity entity, boolean nocache) {
	    StringBuilder ret = new StringBuilder("");
	    Collection<String> groups = null;
	    try {
		groups = getItemGroups (i, entity, nocache);
	    } catch (IdUnusedException exp) {
		// unfortunately some uses aren't user-visible, so it's this or
		// add error handling to all callers
		return "";
	    }
	    if (groups == null)
		return "";
	    for (String g: groups) {
		ret.append(",");
		ret.append(g);
	    }
	    if (ret.length() == 0)
		return "";
	    return ret.substring(1);
	}

         public String getReleaseString(SimplePageItem i) {
	     if (i.getType() == SimplePageItem.PAGE) {
		 SimplePage page = getPage(Long.valueOf(i.getSakaiId()));
		 if (page == null)
		     return null;
		 if (page.isHidden())
		     return messageLocator.getMessage("simplepage.hiddenpage");
		 if (page.getReleaseDate() != null && page.getReleaseDate().after(new Date()))
		     return messageLocator.getMessage("simplepage.pagenotreleased");
	     }
	     return null;
	 }


    //  return GroupEntrys for all groups associated with item
    // need group entries so we can display labels to user
    // entity is optional. pass it if you have it, to avoid requiring
    // us to get it a second time
    // idunusedexception if underlying object doesn't exist
	public Collection<String>getItemGroups (SimplePageItem i, LessonEntity entity, boolean nocache)
	    throws IdUnusedException {

	    Collection<String> ret = new ArrayList<String>();

	    if (!nocache && i.getType() != SimplePageItem.PAGE 
		         && i.getType() != SimplePageItem.TEXT
		         && i.getType() != SimplePageItem.BLTI
		         && i.getType() != SimplePageItem.COMMENTS
		         && i.getType() != SimplePageItem.STUDENT_CONTENT) {
	       Object cached = groupCache.get(i.getSakaiId());
	       if (cached != null) {
		   if (cached instanceof String)
		       return null;
		   return (List<String>)cached;
	       }
	   }

	   if (entity == null) {
	       switch (i.getType()) {
	       case SimplePageItem.ASSIGNMENT:
		   entity = assignmentEntity.getEntity(i.getSakaiId()); break;
	       case SimplePageItem.ASSESSMENT:
		   entity = quizEntity.getEntity(i.getSakaiId()); break;
	       case SimplePageItem.FORUM:
		   entity = forumEntity.getEntity(i.getSakaiId()); break;
	       case SimplePageItem.RESOURCE:
	       case SimplePageItem.MULTIMEDIA:
		   return getResourceGroups(i, nocache);  // responsible for caching the result
		   // throws IdUnusedException if necessary

	       case SimplePageItem.BLTI:
		   entity = bltiEntity.getEntity(i.getSakaiId());
		   if (entity == null || !entity.objectExists())
		       throw new IdUnusedException(i.toString());
		   // fall through: groups controlled by LB
	       // for the following items we don't have non-LB items so don't need itemunused
	       case SimplePageItem.TEXT:
	       case SimplePageItem.PAGE:
	       case SimplePageItem.COMMENTS:
	       case SimplePageItem.STUDENT_CONTENT:
		   return getLBItemGroups(i); // for all native LB objects
	       default:
	    	   return null;
	       }
	   }

	   // only here for object types with underlying entities
	   if (entity == null || !entity.objectExists())
	       throw new IdUnusedException(i.toString());

	   // in principle the groups are stored in a SimplePageGroup if we
	   // are doing access control, and in the tool if not. We can
	   // check that with i.isPrerequisite. However I'm concerned
	   // that if multiple items point to the same object, and some
	   // are set with prerequisite and some are not, that things
	   // could get out of kilter. So I'm going to use the
	   // SimplePageGroup if it exists, and the tool if not.

	   SimplePageGroup simplePageGroup = simplePageToolDao.findGroup(i.getSakaiId());
	   if (simplePageGroup != null) {
	       String groups = simplePageGroup.getGroups();
	       if (groups != null && !groups.equals(""))
		   ret = Arrays.asList(groups.split(","));
	       else 
		   ;  // leave ret as an empty list
	   } else
	       // not under our control, use list from tool
	       ret = entity.getGroups(nocache);	       

	   if (ret == null)
	       groupCache.put(i.getSakaiId(), "*", DEFAULT_EXPIRATION);
	   else
	       groupCache.put(i.getSakaiId(), ret, DEFAULT_EXPIRATION);

	   return ret;

       }

    // obviously this function must be called right after getResourceGroups
       private boolean inherited = false;
       public boolean getInherited() {
	   return inherited;
       }

    // getItemGroups version for resources, since we don't have
    // an interface object. IdUnusedException if the underlying resource doesn't exist
       public Collection<String>getResourceGroups (SimplePageItem i, boolean nocache) 
	   throws IdUnusedException{
    	   SecurityAdvisor advisor = null;
    	   try {
	       
	       // do this before getting privs. It is implemented by seeing whether anon can access,
	       // so the advisor will cause the wrong answer
	   	   boolean inheritingPubView =  contentHostingService.isInheritingPubView(i.getSakaiId());

  	   	  // for isItemVisible to work, users need to be able to get this all the time
                   //if(getCurrentPage().getOwner() != null) {
    			   advisor = new SecurityAdvisor() {
						public SecurityAdvice isAllowed(String userId, String function, String reference) {
							return SecurityAdvice.ALLOWED;
						}
					};
					securityService.pushAdvisor(advisor);
		   //   }
    	   
    		   Collection<String> ret = null;

    		   ContentResource resource = null;
    		   try {
    			   resource = contentHostingService.getResource(i.getSakaiId());
    		   } catch (Exception ignore) {
		       throw new IdUnusedException(i.toString());
    		   }
    		   
    		   Collection<String>groups = null;
    		   AccessMode access = resource.getAccess();
    		   if(AccessMode.INHERITED.equals(access) || inheritingPubView) {
    			   access = resource.getInheritedAccess();
    			   // inherited means that we can't set it locally
    			   // an inherited value of site is OK
    			   // anything else can't be changed, so we set inherited
    			   if (AccessMode.SITE.equals(access) && ! inheritingPubView)
    				   inherited = false;
    			   else
    				   inherited = true;
    			   if (AccessMode.GROUPED.equals(access))
    				   groups = resource.getInheritedGroups();
    		   } else {
    			   // we can always change local modes, even if they are public
    			   inherited = false;
    			   if (AccessMode.GROUPED.equals(access))
    				   groups = resource.getGroups();
    		   }
    		   
    		   if (groups != null) {
    			   ret = new ArrayList<String>();
    			   for (String group: groups) {
    				   int n = group.indexOf("/group/");
    				   ret.add(group.substring(n+7));
    			   }
    		   }
    		   
    		   if (!nocache) {
    			   if (ret == null)
    				   groupCache.put(i.getSakaiId(), "*", DEFAULT_EXPIRATION);
    			   else
    				   groupCache.put(i.getSakaiId(), ret, DEFAULT_EXPIRATION);
    		   }
    		   
    		   return ret;
    	   }finally {
	       if(advisor != null) securityService.popAdvisor();
    	   }
       }

    // no obvious need to cache
       public Collection<String>getLBItemGroups (SimplePageItem i) {
	   List<String> ret = null;

	   String groupString = i.getGroups();
	   if (groupString == null || groupString.equals("")) {
	       return null;
	   }
	       
	   String[] groupsArray = split(groupString, ",");
	   return Arrays.asList(groupsArray);

       }

    // set group list in tool. We'll have an array of group ids
    // returns old list, sorted, or null if entity not found.
    // WARNING: you must check whether isprerequisite. If so, we maintain
    // the group list, so you need to do i.setGroups().
       public List<String> setItemGroups (SimplePageItem i, String[] groups) {
	   // can't allow groups on student pages
	   if (getCurrentPage().getOwner() != null)
	       return null;
	   LessonEntity lessonEntity = null;
	   switch (i.getType()) {
	   case SimplePageItem.ASSIGNMENT:
	       lessonEntity = assignmentEntity.getEntity(i.getSakaiId()); break;
	   case SimplePageItem.ASSESSMENT:
	       lessonEntity = quizEntity.getEntity(i.getSakaiId()); break;
	   case SimplePageItem.FORUM:
	       lessonEntity = forumEntity.getEntity(i.getSakaiId()); break;
	   case SimplePageItem.RESOURCE:
	   case SimplePageItem.MULTIMEDIA:
	       return setResourceGroups (i, groups);
	   case SimplePageItem.TEXT:
	   case SimplePageItem.PAGE:
	   case SimplePageItem.BLTI:
	   case SimplePageItem.COMMENTS:
	   case SimplePageItem.STUDENT_CONTENT:
	       return setLBItemGroups(i, groups);
	   }
	   if (lessonEntity != null) {
	       // need a list to sort it.
	       Collection oldGroupCollection = null;
	       try {
		   oldGroupCollection = getItemGroups(i, lessonEntity, true);
	       } catch (IdUnusedException exc) {
		   return null; // no such entity
	       }
	       List<String>oldGroups = null;
	       if (oldGroupCollection == null)
		   oldGroups = new ArrayList<String>();
	       else
		   oldGroups = new ArrayList<String>(oldGroupCollection);

	       Collections.sort(oldGroups);
	       List<String>newGroups = Arrays.asList(groups);
	       Collections.sort(newGroups);
	       boolean difference = false;
	       if (oldGroups.size() == newGroups.size()) {
		   for (int n = 0; n < oldGroups.size(); n++)
		       if (!oldGroups.get(n).equals(newGroups.get(n))) {
			   difference = true;
			   break;
		       }
	       } else
		   difference = true;

	       if (difference) {
		   if (i.isPrerequisite()) {
		       String groupString = "";
		       for (String groupId: newGroups) {
			   if (groupString.equals(""))
			       groupString = groupId;
			   else
			       groupString = groupString + "," + groupId;
		       }

		       SimplePageGroup simplePageGroup = simplePageToolDao.findGroup(i.getSakaiId());
		       simplePageGroup.setGroups(groupString);
		       update(simplePageGroup);
		   } else
		       lessonEntity.setGroups(Arrays.asList(groups));
	       }
	       return oldGroups;
	   }
	   return null;
       }

       public List<String> setResourceGroups (SimplePageItem i, String[] groups) {

	   ContentResourceEdit resource = null;
	   List<String>ret = null;

	   boolean pushed = false;
	   try {
		   pushed = pushAdvisor();
	       resource = contentHostingService.editResource(i.getSakaiId());

	       if (AccessMode.GROUPED.equals(resource.getInheritedAccess())) {
		   Collection<String> oldGroups = resource.getInheritedGroups();
		   if (oldGroups instanceof List)
		       ret = (List<String>)oldGroups;
		   else if (oldGroups != null)
		       ret = new ArrayList<String>(oldGroups);
	       }
	       // else null

	       if (groups == null || groups.length == 0) {
		   if (AccessMode.GROUPED.equals(resource.getAccess()))
		       resource.clearGroupAccess();
		   // else must be public or site already, leave it
	       } else {
		   Site site = getCurrentSite();
		   for (int n = 0; n < groups.length; n++) {
		       Group group = site.getGroup(groups[n]);
		       groups[n] = group.getReference();
		   }
		   resource.setGroupAccess(Arrays.asList(groups));
	       }
	       contentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
	       resource = null;

	   } catch (java.lang.NullPointerException e) {
	       // KNL-714 gives spurious null pointer
	       setErrMessage(messageLocator.getMessage("simplepage.resourcepossibleerror"));
	   } catch (Exception e) {
	       setErrMessage(e.toString());
	       return null;
	   } finally {
	       // this will generate a traceback in the case of KNL-714, but there's no way
	       // to trap it. Sorry. The log entry will say
	       // org.sakaiproject.content.impl.BaseContentService - cancelResource(): closed ContentResourceEdit
	       // the user will also get a warning
	       if (resource != null) {
		   contentHostingService.cancelResource(resource);
	       }
	       if(pushed) popAdvisor();
	   }

	   return ret;

       }

       public List<String> setLBItemGroups (SimplePageItem i, String[] groups) {

	   List<String>ret = null;
	   // old value
	   String groupString = i.getGroups();
	   if (groupString != null && !groupString.equals("")) {
	       ret = Arrays.asList(groupString.split(","));
	   }
	       
	   groupString = null;
	   if (groups != null) {
	       for (int n = 0; n < groups.length; n++) {
		   if (groupString == null)
		       groupString = groups[n];
		   else
		       groupString = groupString + "," + groups[n];
	       }
	   }
	   i.setGroups(groupString);
	   update(i);

	   return ret; // old value
       }

       public Set<String> getMyGroups() {
	   if (myGroups != null)
	       return myGroups;
	   String userId = getCurrentUserId();
	   Collection<Group> groups = getCurrentSite().getGroupsWithMember(userId);
	   Set<String>ret = new HashSet<String>();
	   if (groups == null)
	       return ret;
	   for (Group group: groups)
	       ret.add(group.getId());
	   myGroups = ret;
	   return ret;
       }

    // sort the list, since it will typically be presented
    // to the user
       public List<GroupEntry> getCurrentGroups() {
	   if (currentGroups != null)
	       return currentGroups;

	   Site site = getCurrentSite();
	   Collection<Group> groups = site.getGroups();
	   List<GroupEntry> groupEntries = new ArrayList<GroupEntry>();
	   for (Group g: groups) {
	       GroupEntry e = new GroupEntry();
	       e.name = g.getTitle();
	       e.id = g.getId();
	       groupEntries.add(e);
	   }

	   Collections.sort(groupEntries,new Comparator() {
		   public int compare(Object o1, Object o2) {
		       GroupEntry e1 = (GroupEntry)o1;
		       GroupEntry e2 = (GroupEntry)o2;
		       return e1.name.compareTo(e2.name);
		   }
	       });
	   currentGroups = groupEntries;
	   return groupEntries;
       }

    // called by add quiz dialog. Create a new item that points to a quiz
    // or update an existing item, depending upon whether itemid is set

	public String addQuiz() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		if (selectedQuiz == null) {
			return "failure";
		} else {
			try {
			    LessonEntity selectedObject = quizEntity.getEntity(selectedQuiz);
			    if (selectedObject == null)
				return "failure";

			    // editing existing item?
			    SimplePageItem i;
			    if (itemId != null && itemId != -1) {
				i = findItem(itemId);
				// do getEntity/getreference to normalize, in case sakaiid is old format
				LessonEntity existing = quizEntity.getEntity(i.getSakaiId());
				String ref = existing.getReference();
				// if same quiz, nothing to do
				if (!ref.equals(selectedQuiz)) {
				    // if access controlled, clear restriction from old quiz and add to new
				    if (i.isPrerequisite()) {
					i.setPrerequisite(false);
					checkControlGroup(i, false);
					// sakaiid and name are used in setting control
					i.setSakaiId(selectedQuiz);
					i.setName(selectedObject.getTitle());
					i.setPrerequisite(true);
					checkControlGroup(i, true);
				    } else {
					i.setSakaiId(selectedQuiz);
					i.setName(selectedObject.getTitle());
				    }
				    // reset quiz-specific stuff
				    i.setDescription("");
				    update(i);
				}
			    } else  // no, add new item
				appendItem(selectedQuiz, selectedObject.getTitle(), SimplePageItem.ASSESSMENT);
			    return "success";
			} catch (Exception ex) {
			    ex.printStackTrace();
			    return "failure";
			} finally {
			    selectedQuiz = null;
			}
		}
	}

	public void setLinkUrl(String url) {
		linkUrl = url;
	}

    // doesn't seem to be used at the moment
	public String createLink() {
		if (linkUrl == null || linkUrl.equals("")) {
			return "cancel";
		}

		String url = linkUrl;
		url = url.trim();

		if (!url.startsWith("http://") && !url.startsWith("https://")) {
			url = "http://" + url;
		}

		appendItem(url, url, SimplePageItem.URL);

		return "success";
	}

	public void setPage(long pageId) {
		sessionManager.getCurrentToolSession().setAttribute("current-pagetool-page", pageId);
		currentPageId = null;
	}

    // more setters and getters used by forms

	public void setHeight(String height) {
		this.height = height;
	}

	public String getHeight() {
		String r = "";
		if (itemId != null && itemId > 0) {
			r = findItem(itemId).getHeight();
		}
		return (r == null ? "" : r);
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getWidth() {
		String r = "";
		if (itemId != null && itemId > 0) {
			r = findItem(itemId).getWidth();
		}
		return (r == null ? "" : r);
	}

	public String getAlt() {
		String r = "";
		if (itemId != null && itemId > 0) {
			r = findItem(itemId).getAlt();
		}
		return (r == null ? "" : r);
	}

    // called by edit multimedia dialog to change parameters in a multimedia item
	public String editMultimedia() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		SimplePageItem i = findItem(itemId);
		if (i != null && i.getType() == SimplePageItem.MULTIMEDIA) {
			i.setHeight(height);
			i.setWidth(width);
			i.setAlt(alt);
			i.setDescription(description);
			i.setHtml(mimetype);
			update(i);
			setItemGroups(i, selectedGroups);
			return "success";
		} else {
			log.warn("editMultimedia Could not find multimedia object: " + itemId);
			return "cancel";
		}
	}

    // called by edit title dialog to change attributes of the page such as the title
	public String editTitle()  {
		if (pageTitle == null || pageTitle.equals("")) {
			return "notitle";
		}

		// because we're using a security advisor, need to make sure it's OK ourselves
		if (!canEditPage()) {
		    return "permission-failed";
		}

		Placement placement = toolManager.getCurrentPlacement();
		SimplePage page = getCurrentPage();
		SimplePageItem pageItem = getCurrentPageItem(null);
		Site site = getCurrentSite();
		boolean needRecompute = false;
		
		if(page.getOwner() == null && getEditPrivs() == 0) {
			// update gradebook link if necessary
			Double currentPoints = page.getGradebookPoints();
			Double newPoints = null;
			
			if (points != null) {
				try {
					newPoints = Double.parseDouble(points);
					if (newPoints == 0.0)
						newPoints = null;
				} catch (Exception ignore) {
					newPoints = null;
				}
			}
			// adjust gradebook entry
			boolean add = false;
			if (newPoints == null && currentPoints != null) {
				gradebookIfc.removeExternalAssessment(site.getId(), "lesson-builder:" + page.getPageId());
			} else if (newPoints != null && currentPoints == null) {
				add = gradebookIfc.addExternalAssessment(site.getId(), "lesson-builder:" + page.getPageId(), null,
						       	pageTitle, newPoints, null, "Lesson Builder");
				
				if(!add) {
					setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
				} else
				    needRecompute = true;
			} else if (currentPoints != null && 
					(!currentPoints.equals(newPoints) || !pageTitle.equals(page.getTitle()))) {
				add = gradebookIfc.updateExternalAssessment(site.getId(), "lesson-builder:" + page.getPageId(), null,
							  	pageTitle, newPoints, null);
				if(!add) {
					setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
				} else if (!currentPoints.equals(newPoints))
					needRecompute = true;
			}
			if (add)
			    page.setGradebookPoints(newPoints);
		}

		if (pageTitle != null && pageItem.getPageId() == 0) {
			try {
				// we need a security advisor because we're allowing users to edit the page if they
				// have
				// simplepage.upd privileges, but site.save requires site.upd.
				securityService.pushAdvisor(new SecurityAdvisor() {
					public SecurityAdvice isAllowed(String userId, String function, String reference) {
						if (function.equals(SITE_UPD) && reference.equals("/site/" + getCurrentSiteId())) {
							return SecurityAdvice.ALLOWED;
						} else {
							return SecurityAdvice.PASS;
						}
					}
				});

				SitePage sitePage = site.getPage(page.getToolId());
					
				for (ToolConfiguration t: sitePage.getTools()) {
					if (t.getId().equals(placement.getId()))
						t.setTitle(pageTitle);
				}
				
				sitePage.setTitle(pageTitle);
				siteService.save(site);
				page.setTitle(pageTitle);
				page.setHidden(hidePage);
				if (hasReleaseDate)
					page.setReleaseDate(releaseDate);
				else
					page.setReleaseDate(null);
				update(page);
				updateCurrentPage();
				placement.setTitle(pageTitle);
				placement.save();
				pageVisibilityHelper(site, page.getToolId(), !hidePage);
				pageItem.setPrerequisite(prerequisite);
				pageItem.setRequired(required);
				pageItem.setName(pageTitle);
				update(pageItem);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				securityService.popAdvisor();
			}
		} else if (pageTitle != null) {
			page.setTitle(pageTitle);
			page.setHidden(hidePage);
			if (hasReleaseDate)
			    page.setReleaseDate(releaseDate);
			else
			    page.setReleaseDate(null);
			update(page);
		}
		
		if(pageTitle != null) {
			if(pageItem.getType() == SimplePageItem.STUDENT_CONTENT) {
				SimpleStudentPage student = simplePageToolDao.findStudentPage(pageItem.getId(), page.getOwner());
				student.setTitle(pageTitle);
				update(student, false);
			} else {
				pageItem.setName(pageTitle);
				update(pageItem);
			}
			
			adjustPath("", pageItem.getPageId(), pageItem.getId(), pageTitle);
		}
		
		String collectionId = contentHostingService.getSiteCollection(getCurrentSiteId()) + "LB-CSS/";
		String uploadId = uploadFile(collectionId);
		if(uploadId != null) {
			page.setCssSheet(uploadId);
			
			// Make sure the relevant caches are wiped clean.
			resourceCache.expire(collectionId);
			resourceCache.expire(uploadId);
		}else {
			page.setCssSheet(dropDown);
		}
		
		update(page);

		// have to do this after the page itself is updated
		if (needRecompute)
		    recomputeGradebookEntries(page.getPageId(), points);
		// points, not newPoints because API wants a string

		if (pageItem.getPageId() == 0) {
			return "reload";
		} else {
			return "success";
		}
	}
	
	private String uploadFile(String collectionId) {
		String name = null;
		String mimeType = null;
		MultipartFile file = null;
		
		if (multipartMap.size() > 0) {
			// 	user specified a file, create it
			file = multipartMap.values().iterator().next();
			if (file.isEmpty())
				file = null;
		}
		
		if (file != null) {
			try {
				contentHostingService.checkCollection(collectionId);
			}catch(Exception ex) {
				try {
					ContentCollectionEdit edit = contentHostingService.addCollection(collectionId);
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "LB-CSS");
					contentHostingService.commitCollection(edit);
				}catch(Exception e) {
					setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
					return null;
				}
			}
			
			//String collectionId = getCollectionId(false);
			// 	user specified a file, create it
			name = file.getOriginalFilename();
			if (name == null || name.length() == 0)
				name = file.getName();
			
			int i = name.lastIndexOf("/");
			if (i >= 0)
				name = name.substring(i+1);
			String base = name;
			String extension = "";
			i = name.lastIndexOf(".");
			if (i > 0) {
				base = name.substring(0, i);
				extension = name.substring(i+1);
			}
			
			mimeType = file.getContentType();
			try {
				ContentResourceEdit res = contentHostingService.addResource(collectionId, 
						  	Validator.escapeResourceName(base),
						  	Validator.escapeResourceName(extension),
						  	MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				res.setContentType(mimeType);
				res.setContent(file.getInputStream());
				try {
					contentHostingService.commitResource(res,  NotificationService.NOTI_NONE);
					// 	there's a bug in the kernel that can cause
					// 	a null pointer if it can't determine the encoding
					// 	type. Since we want this code to work on old
					// 	systems, work around it.
				} catch (java.lang.NullPointerException e) {
					setErrMessage(messageLocator.getMessage("simplepage.resourcepossibleerror"));
				}
				return res.getId();
			} catch (org.sakaiproject.exception.OverQuotaException ignore) {
				setErrMessage(messageLocator.getMessage("simplepage.overquota"));
				return null;
			} catch (Exception e) {
				setErrMessage(messageLocator.getMessage("simplepage.resourceerror").replace("{}", e.toString()));
				log.error("addMultimedia error 1 " + e);
				return null;
			}
		}else {
			return null;
		}
	}

	public String addPages()  {
		if (!canEditPage())
			return "permission-fail";

		// javascript should have checked all this
		if (newPageTitle == null || newPageTitle.equals(""))
			return "fail";

		int numPages = 1;
		if (numberOfPages !=null && !numberOfPages.equals(""))
			numPages = Integer.valueOf(numberOfPages);
		
		String prefix = "";
		String suffix = "";
		int start = 1;

		if (numPages > 1) {
			Pattern pattern = Pattern.compile("(\\D*)(\\d+)(.*)");
			Matcher matcher = pattern.matcher(newPageTitle);
			
			if (!matcher.matches())
				return "fail";

			prefix = matcher.group(1);
			suffix = matcher.group(3);
			start = Integer.parseInt(matcher.group(2));
		}

		if (numPages == 1) {
			addPage(newPageTitle, copyPage);
		} else {
			// note sure what to do here. We have to have a maximum to prevent creating 1,000,000 pages due
			// to a typo. This allows one a week for a year. Surely that's enough. We can make it 
			// configurable if necessary.
			if (numPages > 52)
				numPages = 52;
			
			while (numPages > 0) {
				String title = prefix + Integer.toString(start) + suffix;
				addPage(title, null, copyPage, (numPages == 1));  // only save the last time
				numPages--;
				start++;
			}
	    }

		setTopRefresh();

		return "success";
	}
	
	// Adds an existing page as a top level page
	public String addOldPage() {
		if (getEditPrivs() != 0)
		    return "permission-failed";
		
		SimplePage target = getPage(Long.valueOf(selectedEntity));
		if(target != null)
			addPage(target.getTitle(), target.getPageId(), false, true);
		
		return "success";
	}

	public SimplePage addPage(String title, boolean copyCurrent) {
		return addPage(title, null, copyCurrent, true);
	}
	
        public SimplePage addPage(String title, Long pageId, boolean copyCurrent, boolean doSave) {

		Site site = getCurrentSite();
		SitePage sitePage = site.addPage();

		ToolConfiguration tool = sitePage.addTool(LESSONBUILDER_ID);
		String toolId = tool.getPageId();
		
		SimplePage page = null;
		
		if(pageId == null) {
			page = simplePageToolDao.makePage(toolId, getCurrentSiteId(), title, null, null);
			saveItem(page);
		}else {
			page = getPage(pageId);
			page.setToolId(toolId);
			page.setParent(null);
			page.setTopParent(null);
			update(page);
			title = page.getTitle();
		}

		tool.setTitle(title);
		
		SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(page.getPageId()), title);
		saveItem(item);

		sitePage.setTitle(title);
		sitePage.setTitleCustom(true);
		if (doSave) {
		    try {
			siteService.save(site);
		    } catch (Exception e) {
			log.error("addPage unable to save site " + e);
		    }
		    currentSite = null; // force refetch, since we've changed it. note sure this is strictly needed
		}

	    if (copyCurrent) {
	    	long oldPageId = getCurrentPageId();
	    	long newPageId = page.getPageId();
	    	for (SimplePageItem oldItem: simplePageToolDao.findItemsOnPage(oldPageId)) {
	    		// don't copy pages. It's not clear whether we want to deep copy or
	    		// not. If we do the wrong thing the user coudl end up editing the
	    		// wrong page and losing content.
	    		if (oldItem.getType() == SimplePageItem.PAGE)
	    			continue;
	    		SimplePageItem newItem = simplePageToolDao.copyItem(oldItem);
	    		newItem.setPageId(newPageId);
	    		saveItem(newItem);
	    	}
	    }

	    setTopRefresh();
	    
	    return page;
	}

        // when a gradebook entry is added or point value for page changed, need to
        // add or update all student entries for the page
        // this only updates grades for users that are complete. Others should have 0 score, which won't change
	public void recomputeGradebookEntries(Long pageId, String newPoints) {
	    Map<String, String> userMap = new HashMap<String,String>();
	    List<SimplePageItem> items = simplePageToolDao.findPageItemsBySakaiId(Long.toString(pageId));
	    if (items == null)
		return;
	    for (SimplePageItem item : items) {
		List<String> users = simplePageToolDao.findUserWithCompletePages(item.getId());
		for (String user: users)
		    userMap.put(user, newPoints);
	    }
	    
	    gradebookIfc.updateExternalAssessmentScores(getCurrentSiteId(), "lesson-builder:" + pageId, userMap);
	}

	public boolean isImageType(SimplePageItem item) {
		// if mime type is defined use it
		String mimeType = item.getHtml();
		if (mimeType != null && (mimeType.startsWith("http") || mimeType.equals("")))
			mimeType = null;

		if (mimeType != null && mimeType.length() > 0) {
			return mimeType.toLowerCase().startsWith("image/");
		}

		// else use extension

		String name = item.getSakaiId();

		// starts after last /
		int i = name.lastIndexOf("/");
		if (i >= 0)
			name = name.substring(i+1);
	    
		String extension = null;	    
		i = name.lastIndexOf(".");
		if (i > 0)
			extension = name.substring(i+1);

		if (extension == null)
			return false;

		extension = extension.trim();
		extension = extension.toLowerCase();

		if (imageTypes.contains(extension)) {
			return true;
		} else {
			return false;
		}
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void fixorder() {
	    List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());
	    
	    for(int i = 0; i < items.size(); i++) {
		if(items.get(i).getSequence() <= 0) {
		    items.remove(items.get(i));
		    i--;
		}
	    }

	    int i = 1;
	    for(SimplePageItem item: items) {
		if (item.getSequence() != i) {
		    item.setSequence(i);
		    update(item);
		}
		i++;
	    }

	}

    // called by reorder tool to do the reordering
	public String reorder() {

		if (!canEditPage())
			return "permission-fail";

		if (order == null) {
			return "cancel";
		}
		
		fixorder(); // order has to be contiguous or things will break

		order = order.trim();

		List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());
		
		// Remove items that weren't ordered due to having sequences too low.
		// Typically means they are tacked onto the end automatically.
		for(int i = 0; i < items.size(); i++) {
			if(items.get(i).getSequence() <= 0) {
				items.remove(items.get(i));
				i--;
			}
		}

		List <SimplePageItem> secondItems = null;
		if (selectedEntity != null && !selectedEntity.equals("")) {
		    // second page is involved
		    Long secondPageId = Long.parseLong(selectedEntity);
		    SimplePage secondPage = getPage(secondPageId);
		    if (secondPage != null && secondPage.getSiteId().equals(getCurrentPage().getSiteId())) {
			secondItems = getItemsOnPage(secondPageId);
			if (secondItems.size() == 0)
			    secondItems = null;
			else {
			    for(int i = 0; i < secondItems.size(); i++) {
				if(secondItems.get(i).getSequence() <= 0) {
				    secondItems.remove(secondItems.get(i));
				    i--;
				}
			    }
			}
		    }
		}

		String[] split = split(order, " ");

		// make sure nothing is duplicated. I know it shouldn't be, but
		// I saw the Fluid reorderer get confused once.
		Set<String> used = new HashSet<String>();
		for (int i = 0; i < split.length; i++) {
			if (!used.add(split[i].trim())) {
				log.warn("reorder: duplicate value");
				setErrMessage(messageLocator.getMessage("simplepage.reorder-duplicates"));
				return "failed"; // it was already there. Oops.
			}
		}

		// keep track of which old items are used so we can remove the ones that aren't.
		// items in set are indices into "items"
		Set<Integer>keep = new HashSet<Integer>();

		// now do the reordering
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("---"))
			    break;
			if (split[i].startsWith("*")) {
			    // item from second page. add copy
			    SimplePageItem oldItem = secondItems.get(Integer.valueOf(split[i].substring(1)) - 1);
			    SimplePageItem newItem = simplePageToolDao.copyItem(oldItem);
			    newItem.setPageId(getCurrentPageId());
			    newItem.setSequence(i + 1);
			    saveItem(newItem);
			} else {
			    // existing item. update its sequence and note that it's still used
			    int old = items.get(Integer.valueOf(split[i]) - 1).getSequence();
			    keep.add(Integer.valueOf(split[i]) - 1);
			    items.get(Integer.valueOf(split[i]) - 1).setSequence(i + 1);
			    if (old != i + 1) {
				update(items.get(Integer.valueOf(split[i]) - 1));
			    }

			}
		}

		// now kill all items on the page we didn't see in the new order
		for (int i = 0; i < items.size(); i++) {
		    if (!keep.contains((Integer)i))
			simplePageToolDao.deleteItem(items.get(i));
		}

		itemsCache.remove(getCurrentPage().getPageId());
		// removals left gaps in order. fix it.
		fixorder();
		itemsCache.remove(getCurrentPage().getPageId());

		return "success";
	}

    // this is sort of sleasy. A simple redirect passes no data. Thus it takes
    // us back to the default page. So we advance the default page. It would probably
    // have been better to use a link rather than a command, then the link could
    // have passed the page and item.
    //	public String next() {
    //	    getCurrentPageId();  // sets item id, which is what we want
    //	    SimplePageItem item = getCurrentPageItem(null);
    //
    //	    List<SimplePageItem> items = getItemsOnPage(item.getPageId());
    //
    //	    item = items.get(item.getSequence());  // sequence start with 1, so this is the next item
    //	    updatePageObject(Long.valueOf(item.getSakaiId()));
    //	    updatePageItem(item.getId());
    //
    //	    return "redirect";
    //	}

	public String getCurrentUserId() {
	    if (currentUserId == null)
	    	currentUserId = UserDirectoryService.getCurrentUser().getId();
	    return currentUserId;
	}
	    
    // page is complete, update gradebook entry if any
    // note that if the user has never gone to a page, the gradebook item will be missing.
    // if they gone to it but it's not complete, it will be 0. We can't explicitly set
    // a missing value, and this is the only way things will work if someone completes a page
    // and something changes so it is no longer complete. 
	public void trackComplete(SimplePageItem item, boolean complete ) {
		SimplePage page = getCurrentPage();
	    if (page.getGradebookPoints() != null)
	    	gradebookIfc.updateExternalAssessmentScore(getCurrentSiteId(), "lesson-builder:" + page.getPageId(), getCurrentUserId(), 
	    			complete ? Double.toString(page.getGradebookPoints()) : "0.0");
	}
    
	/**
	 * 
	 * @param itemId
	 *            The ID in the <b>items</b> table.
	 * @param path 
	 *            breadcrumbs, only supplied it the item is a page
	 *            It is valid for code to check path == null to see
	 *            whether it is a page
	 *       Create or update a log entry when user accesses an item.
	 */
	public void track(long itemId, String path) {
		track(itemId, path, null);
	}
	
	public void track(long itemId, String path, Long studentPageId) {
		String userId = getCurrentUserId();
		if (userId == null)
		    userId = ".anon";
		SimplePageLogEntry entry = getLogEntry(itemId, studentPageId);
		String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
		
		if (entry == null) {
			entry = simplePageToolDao.makeLogEntry(userId, itemId, studentPageId);
			
			if (path != null && studentPageId == null) {
				boolean complete = isPageComplete(itemId);
				entry.setComplete(complete);
				entry.setPath(path);
				entry.setToolId(toolId);
				SimplePageItem i = findItem(itemId);
				EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.read", "/lessonbuilder/page/" + i.getSakaiId(), complete));
				trackComplete(i, complete);
				studentPageId = -1L;
			}else if(path != null) {
				entry.setPath(path);
				entry.setComplete(true);
				entry.setToolId(toolId);
				SimplePage page = getPage(studentPageId);
				EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.read", "/lessonbuilder/page/" + page.getPageId(), true));
			}

			saveItem(entry);
			logCache.put(itemId + "-" + studentPageId, entry);
		} else {
			if (path != null && studentPageId == null) {
				boolean wasComplete = entry.isComplete();
				boolean complete = isPageComplete(itemId);
				entry.setComplete(complete);
				entry.setPath(path);
				entry.setToolId(toolId);
				entry.setDummy(false);
				SimplePageItem i = findItem(itemId);
				EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.read", "/lessonbuilder/page/" + i.getSakaiId(), complete));
				if (complete != wasComplete)
				    trackComplete(i, complete);
				studentPageId = -1L;
			}else if(path != null) {
				entry.setComplete(true);
				entry.setPath(path);
				entry.setToolId(toolId);
				entry.setDummy(false);
				SimplePage page = getPage(studentPageId);
				EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.read", "/lessonbuilder/page/" + page.getPageId(), true));
			}

			update(entry);
		}

		//SimplePageItem i = findItem(itemId);
		// todo
		// code can't work anymore. I'm not sure whether it's needed.
		// we don't update a page as complete if the user finishes a test, etc, until he
		// comes back to the page. I'm not sure I feel compelled to do this either. But
		// once we move to the new hiearchy, we'll see

		// top level doesn't have a next level, so avoid null pointer problem
		//		if (i.getPageId() != 0) {
		//		    SimplePageItem nextLevelUp = simplePageToolDao.findPageItemBySakaiId(String.valueOf(i.getPageId()));
		//		    if (isItemComplete(findItem(itemId)) && nextLevelUp != null) {
		//			track(nextLevelUp.getId(), true);
		//		    }
		//		}
	}

	public SimplePageLogEntry getLogEntry(long itemId) {
		return getLogEntry(itemId, null);
	}
	
	public SimplePageLogEntry getLogEntry(long itemId, Long studentPageId) {
		if(studentPageId == null) {
			studentPageId = -1L;
		}
		
		String lookup = itemId + "-" + studentPageId;
		SimplePageLogEntry entry = logCache.get(lookup);

		if (entry != null)
		    return entry;
		String userId = getCurrentUserId();
		if (userId == null)
		    userId = ".anon";
		entry = simplePageToolDao.getLogEntry(userId, itemId, studentPageId);
		
		
		logCache.put(lookup, entry);
		
		return entry;
	}

	public boolean hasLogEntry(long itemId) {
		return (getLogEntry(itemId) != null);
	}

    // if the item has a group requirement, are we in one of the groups.
    // this is called a lot and is fairly expensive, so results are cached
	public boolean isItemVisible(SimplePageItem item) {
		if (canEditPage()) {
		    return true;
		}
		Boolean ret = visibleCache.get(item.getId());
		if (ret != null)
		    return (boolean)ret;

		// item is page, and it is hidden or not released
		if (item.getType() == SimplePageItem.PAGE) {
		    SimplePage page = getPage(Long.valueOf(item.getSakaiId()));
		    if (page.isHidden())
			return false;
		    if (page.getReleaseDate() != null && page.getReleaseDate().after(new Date()))
			return false;
		}

		Collection<String>itemGroups = null;
		try {
		    itemGroups = getItemGroups(item, null, false);
		} catch (IdUnusedException exc) {
		    visibleCache.put(item.getId(), false);
		    return false; // underlying entity missing, don't show it
		}
		if (itemGroups == null || itemGroups.size() == 0) {
		    // this includes items for which for which visibility doesn't apply
		    visibleCache.put(item.getId(), true);
		    return true;
		}

		getMyGroups();

		for (String group: itemGroups) {
			if (myGroups.contains(group)) {
				visibleCache.put(item.getId(), true);
				return true;
			}
		}

		visibleCache.put(item.getId(), false);

		return false;
	}
		
    // this is called in a loop to see whether items are available. Since computing it can require
    // database transactions, we cache the results
	public boolean isItemComplete(SimplePageItem item) {
		if (!item.isRequired()) {
			// We don't care if it has been completed if it isn't required.
			return true;
		} 
		Long itemId = item.getId();
		Boolean cached = completeCache.get(itemId);
		if (cached != null)
		    return (boolean)cached;
		if (item.getType() == SimplePageItem.RESOURCE || item.getType() == SimplePageItem.URL || item.getType() == SimplePageItem.BLTI) {
			// Resource. Completed if viewed.
			if (hasLogEntry(item.getId())) {
				completeCache.put(itemId, true);
				return true;
			} else {
				completeCache.put(itemId, false);
				return false;
			}
		} else if (item.getType() == SimplePageItem.PAGE) {
			SimplePageLogEntry entry = getLogEntry(item.getId());
			if (entry == null || entry.getDummy()) {
				completeCache.put(itemId, false);
				return false;
			} else if (entry.isComplete()) {
				completeCache.put(itemId, true);
				return true;
			} else {
				completeCache.put(itemId, false);
				return false;
			}
		} else if (item.getType() == SimplePageItem.ASSIGNMENT) {
			try {
				if (item.getSakaiId().equals(SimplePageItem.DUMMY)) {
				    completeCache.put(itemId, false);
				    return false;
				}
				LessonEntity assignment = assignmentEntity.getEntity(item.getSakaiId());
				if (assignment == null) {
				    completeCache.put(itemId, false);
				    return false;
				}
				LessonSubmission submission = assignment.getSubmission(getCurrentUserId());

				if (submission == null) {
				    completeCache.put(itemId, false);
				    return false;
				}

				int type = assignment.getTypeOfGrade();

				if (!item.getSubrequirement()) {
					completeCache.put(itemId, true);
					return true;
				} else if (submission.getGradeString() != null) {
				    // assume that assignments always use string grade. this may change
					boolean ret = isAssignmentComplete(type, submission, item.getRequirementText());
					completeCache.put(itemId, ret);
					return ret;
				} else {
					completeCache.put(itemId, false);
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				completeCache.put(itemId, false);
				return false;
			}
		} else if (item.getType() == SimplePageItem.FORUM) {
			try {
				if (item.getSakaiId().equals(SimplePageItem.DUMMY)) {
					completeCache.put(itemId, false);
					return false;
				}
				User user = UserDirectoryService.getUser(getCurrentUserId());
				LessonEntity forum = forumEntity.getEntity(item.getSakaiId());
				if (forum == null)
					return false;
				// for the moment don't find grade. just see if they submitted
				if (forum.getSubmissionCount(user.getId()) > 0) {
					completeCache.put(itemId, true);
					return true;
				} else {
					completeCache.put(itemId, false);
					return false;
				}
			} catch (Exception e) {
			    e.printStackTrace();
			    completeCache.put(itemId, false);
			    return false;
			}
		} else if (item.getType() == SimplePageItem.ASSESSMENT) {
			if (item.getSakaiId().equals(SimplePageItem.DUMMY)) {
			    completeCache.put(itemId, false);
			    return false;
			}
			LessonEntity quiz = quizEntity.getEntity(item.getSakaiId());
			if (quiz == null) {
			    completeCache.put(itemId, false);
			    return false;
			}
			User user = null;
			try {
			    user = UserDirectoryService.getUser(getCurrentUserId());
			} catch (Exception ignore) {
			    completeCache.put(itemId, false);
			    return false;
			}

			LessonSubmission submission = quiz.getSubmission(user.getId());

			if (submission == null) {
				completeCache.put(itemId, false);
				return false;
			} else if (!item.getSubrequirement()) {
				// All that was required was that the user submit the test
				completeCache.put(itemId, true);
				return true;
			} else {
				Float grade = submission.getGrade();
			    if (grade >= Float.valueOf(item.getRequirementText())) {
			    	completeCache.put(itemId, true);
			    	return true;
			    } else {
			    	completeCache.put(itemId, false);
			    	return false;
			    }
			}
		} else if (item.getType() == SimplePageItem.COMMENTS) {
			List<SimplePageComment>comments = simplePageToolDao.findCommentsOnItemByAuthor((long)itemId, getCurrentUserId());
			boolean found = false;
			if (comments != null) {
			    for (SimplePageComment comment: comments) {
				if (comment.getComment() != null && !comment.getComment().equals("")) {
				    found = true;
				    break;
				}
			    }
			}
			if (found) {
			    completeCache.put(itemId, true);
			    return true;
			} else {
			    completeCache.put(itemId, false);
			    return false;
			}
		} else if (item.getType() == SimplePageItem.STUDENT_CONTENT) {
		    // need option for also requiring the student to submit a comment on the content
		        
			SimpleStudentPage student = simplePageToolDao.findStudentPage(itemId, getCurrentUserId());

			if (student != null && ! student.isDeleted()) {
			    completeCache.put(itemId, true);
			    return true;
			} else {
			    completeCache.put(itemId, false);
			    return false;
			}
		} else if (item.getType() == SimplePageItem.TEXT || item.getType() == SimplePageItem.MULTIMEDIA) {
			// In order to be considered "complete", these items
			// only have to be viewed. If this code is reached,
			// we know that that the page has already been viewed.
			completeCache.put(itemId, true);
			return true;
		} else {
			completeCache.put(itemId, false);
			return false;
		}
	}

	private boolean isAssignmentComplete(int type, LessonSubmission submission, String requirementString) {
		String grade = submission.getGradeString();

		if (type == SimplePageItem.ASSESSMENT) {
			if (grade.equals("Pass")) {
				return true;
			} else {
				return false;
			}
		} else if (type == SimplePageItem.TEXT) {
			if (grade.equals("Checked")) {
				return true;
			} else {
				return false;
			}
		} else if (type == SimplePageItem.PAGE) {
			if (grade.equals("ungraded")) {
				return false;
			}

			int requiredIndex = -1;
			int currentIndex = -1;

			for (int i = 0; i < GRADES.length; i++) {
				if (GRADES[i].equals(requirementString)) {
					requiredIndex = i;
				}

				if (GRADES[i].equals(grade)) {
					currentIndex = i;
				}
			}

			if (requiredIndex == -1 || currentIndex == -1) {
				return false;
			} else {
				if (requiredIndex >= currentIndex) {
					return true;
				} else {
					return false;
				}
			}
		} else if (type == SimplePageItem.ASSIGNMENT) {
			// assignment 2 uses gradebook, so we have a float value
			if (submission.getGrade() != null)
				return submission.getGrade() >= Float.valueOf(requirementString);
			// otherwise use the String
			if (Float.valueOf(Integer.valueOf(grade) / 10) >= Float.valueOf(requirementString)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

    // note on completion: there's an issue with subpages. isItemComplete just looks to see if
    // the logEntry shows that the page is complete. But that's filled out when someone actually
    // visits the page. If an instructor has graded something or a student has submitted directly
    // through a tool, requirements in a subpage might have been completed without the student
    // actually visiting the page. 
    //   So for the first subpage that is both required and not completed, we want to recheck
    // the subpage to see if it's now complete. This will have to happen recursively. Of course
    // if it's OK then we need to check the next one, etc.
    //
    // This code will return false immediately when it gets the first thing that is required
    // and not completed. We just do
    // a recusrive check if the subpage is required, visited, and not already completed. The reason
    // for only checking if visited is to avoid false positives for page with no requirements. A
    // page with no required items is completed when it's visited. If there are no reqirements, the
    // recursive call will return true, but if the page hasn't been visited it's still not completed.
    // So we only want to do the recursive call if it's been visited.
    //    I think it's reasonable not to start checking status of quizes etc until the page has been
    // visited.
    //
    // no changes are needed for isItemCompleted. isPageCompleted is called at the starrt of ShowPage
    // by track. That call will update the status of subpages. So when we're doing other operations on
    // the page we can use a simple isItemComplete, because status would have been updated at the start
    // of ShowPage.
    //
    // Note that we only check the first subpage that hasn't been completed. If there's more than one,
    // information about later ones could be out of date. I'm claim that's less important, because it can't
    // affect whether anything is allowed.
    //
    // the recursive call to isItemComplete for subpages has some issues. isItemComplete will
    // use the completeCache, which may be set by isitemcomplete without doing a full recursive
    // scan. We are again depending upon the fact that the first check is done here, which does
    // the necessary recursion. The cache is request-scope. If it were longer-lived we'd have
    // a problem.

    // alreadySeen is needed in case there's a loop in the page structure. This is uncommon but
    // possible

	public boolean isPageComplete(long itemId) {
	    return isPageComplete(itemId, null);
	}

	/**
	 * @param itemId
	 *            The ID of the page from the <b>items</b> table (not the page table).
	 * @return
	 */
	public boolean isPageComplete(long itemId,Set<Long>alreadySeen) {
	    
		// Make sure student content objects aren't treated like pages.
		// TODO: Put in requirements
		if(findItem(itemId).getType() == SimplePageItem.STUDENT_CONTENT) {
			return true;
		}
		
		
		List<SimplePageItem> items = getItemsOnPage(Long.valueOf(findItem(itemId).getSakaiId()));

		for (SimplePageItem item : items) {
			if (!isItemComplete(item)) {
			    if (item.getType() == SimplePageItem.PAGE) {
				// If we get here, must be not completed or isItemComplete would be true
				SimplePageLogEntry entry = getLogEntry(item.getId());
				// two possibilities in next check:
				// 1) hasn't seen page, can't be complete
				// 2) we've checked before; there's a loop; be safe and disallow it
				if (entry == null || entry.getDummy() ||
				    (alreadySeen != null && alreadySeen.contains(item.getId()))) {
				    return false;
				}
				if (alreadySeen == null)
				    alreadySeen = new HashSet<Long>();
				alreadySeen.add(itemId);
				// recursive check to see whether page is complete
				boolean subOK = isPageComplete(item.getId(), alreadySeen);
				if (!subOK) {
				    return false; // nope, that was our last hope
				}
				// was complete; fall through and return true
			    } else
				return false;
			}
		}

		// All of them were complete.
		completeCache.put(itemId, true);
		return true;
	}

    // return list of pages needed for current page. This is the primary code
    // used by ShowPageProducer to see whether the user is allowed to the page
    // (given that they have read permission, of course)
    // Note that the same page can occur
    // multiple places, but we're passing the item, so we've got the right one
	public List<String> pagesNeeded(SimplePageItem item) {
		String currentPageId = Long.toString(getCurrentPageId());
		List<String> needed = new ArrayList<String>();

		if (!item.isPrerequisite()){
			return needed;
		}

	    // authorized or maybe user is gaming us, or maybe next page code
	    // sent them to something that isn't available.
	    // as an optimization check haslogentry first. That will be true if
	    // they have been here before. Saves us the trouble of doing full
	    // access checking. Otherwise do a real check. That should only happen
	    // for next page in odd situations.
		if (item.getPageId() > 0) {
			if (!hasLogEntry(item.getId()) &&
					!isItemAvailable(item, item.getPageId())) {
				SimplePage parent = getPage(item.getPageId());
				if (parent != null)
					needed.add(parent.getTitle());
				else
					needed.add("unknown page");  // not possible, it says
			}
			return needed;
		}

	    // we've got a top level page.
	    // get dummy items for top level pages in site
		List<SimplePageItem> items = simplePageToolDao.findItemsInSite(getCurrentSite().getId());
		// sorted by SQL

		for (SimplePageItem i : items) {
			if (i.getSakaiId().equals(currentPageId)) {
				return needed;  // reached current page. we're done
			}
			if (i.isRequired() && !isItemComplete(i))
				needed.add(i.getName());
		}

		return needed;

	}

    // maybeUpdateLinks checks to see if this page was copied from another
    // site and needs an update
	public void maybeUpdateLinks() {
	    String needsFixup = getCurrentSite().getProperties().getProperty("lessonbuilder-needsfixup");
	    if (needsFixup == null || !needsFixup.equals("true"))
		return;
	    lessonBuilderEntityProducer.updateEntityReferences(getCurrentSiteId());
	    Site site = getCurrentSite();
	    ResourcePropertiesEdit rp = site.getPropertiesEdit();
	    rp.removeProperty("lessonbuilder-needsfixup");
	    try {
		siteService.save(site);
	    } catch (Exception e) {
		log.warn("site save in maybeUpdateLinks " + e);
	    }
	    currentSite = null;  // force refetch next time
	}

	public boolean isItemAvailable(SimplePageItem item) {
	    return isItemAvailable(item, getCurrentPageId());
	}

	public boolean isItemAvailable(SimplePageItem item, long pageId) {
		if (item.isPrerequisite()) {
			List<SimplePageItem> items = getItemsOnPage(pageId);

			for (SimplePageItem i : items) {
				if (i.getSequence() >= item.getSequence()) {
				    break;
				} else if (i.isRequired() && isItemVisible(i)) {
				    if (!isItemComplete(i)) {
				    	return false;
				    }
				} 
			}
		}
		return true;
	}

    // weird variant that works even if current item doesn't have prereq.
	public boolean wouldItemBeAvailable(SimplePageItem item, long pageId) {
		List<SimplePageItem> items = getItemsOnPage(pageId);

		for (SimplePageItem i : items) {
			if (i.getSequence() >= item.getSequence()) {
				break;
			} else if (i.isRequired() && isItemVisible(i)) {
				if (!isItemComplete(i))
					return false;
			}
		}
		return true;
	}

	public String getNameOfSakaiItem(SimplePageItem i) {
		String SakaiId = i.getSakaiId();

		if (SakaiId == null || SakaiId.equals(SimplePageItem.DUMMY))
			return null;

		if (i.getType() == SimplePageItem.ASSIGNMENT) {
			LessonEntity assignment = assignmentEntity.getEntity(i.getSakaiId());
			if (assignment == null)
				return null;
			return assignment.getTitle();
		} else if (i.getType() == SimplePageItem.FORUM) {
			LessonEntity forum = forumEntity.getEntity(i.getSakaiId());
			if (forum == null)
				return null;
			return forum.getTitle();
		} else if (i.getType() == SimplePageItem.ASSESSMENT) {
			LessonEntity quiz = quizEntity.getEntity(i.getSakaiId());
			if (quiz == null)
				return null;
			return quiz.getTitle();
		} else if (i.getType() == SimplePageItem.BLTI) {
		    if (bltiEntity == null)
			return null;
		    LessonEntity blti = bltiEntity.getEntity(i.getSakaiId());
		    if (blti == null)
			return null;
		    return blti.getTitle();
		} else
			return null;
	}
		
    // we allow both ? and &. The key may be the value of something like ?v=, so we don't know
    // whether the next thing is & or ?. To be safe, use & except for the first param, which
    // uses ?. Note that RSF will turn & into &amp; in the src= attribute. THis appears to be correct,
    // as HTML is an SGML dialect.
    // If you run into trouble with &amp;, you can use ; in the following. Google seems to 
    // process it correctly. ; is a little-known alterantive to & that the RFCs do permit
       private String normalizeParams(String URL) {
	   URL = URL.replaceAll("[\\?\\&\\;]", "&");
	   return URL.replaceFirst("\\&", "?");
       }

       private String getYoutubeKeyFromUrl(String URL) {
	   // 	see if it has a Youtube ID
	   if (URL.startsWith("http://www.youtube.com/") || URL.startsWith("http://youtube.com/")) {
	       Matcher match = YOUTUBE_PATTERN.matcher(URL);
	       if (match.find()) {
		   return normalizeParams(match.group(1));
	       }
	       match = YOUTUBE2_PATTERN.matcher(URL);
	       if (match.find()) {
		   return normalizeParams(match.group(1));
	       }
	   }else if(URL.startsWith("http://youtu.be/")) {
	       Matcher match = SHORT_YOUTUBE_PATTERN.matcher(URL);
	       if(match.find()) {
		   return normalizeParams(match.group(1));
	       }
	   }
	   return null;
       }

    /* 
     * return 11-char youtube ID for a URL, or null if it doesn't match
     * we store URLs as content objects, so we have to retrieve the object
     * in order to check. The actual URL is stored as the contents
     * of the entity
     */

	public String getYoutubeKey(SimplePageItem i) {
		String sakaiId = i.getSakaiId();

		SecurityAdvisor advisor = null;
		try {
			if(getCurrentPage().getOwner() != null) {
				// Need to allow access into owner's home directory
				advisor = new SecurityAdvisor() {
					public SecurityAdvice isAllowed(String userId, String function, String reference) {
						if("content.read".equals(function) || "content.hidden".equals(function)) {
							return SecurityAdvice.ALLOWED;
						}else {
							return SecurityAdvice.PASS;
						}
					}
				};
				securityService.pushAdvisor(advisor);
			}
			// find the resource
			ContentResource resource = null;
			try {
				resource = contentHostingService.getResource(sakaiId);
			} catch (Exception ignore) {
				return null;
			}
			
			// 	make sure it's a URL
			if (resource == null ||
			    // copying resources does not preserve this field, so if we do this test, things won't
			    // work in copied sites
			    //		!resource.getResourceType().equals("org.sakaiproject.content.types.urlResource") ||
					!resource.getContentType().equals("text/url")) {
				return null;
			}
			
			// 	get the actual URL
			String URL = null;
			try {
				URL = new String(resource.getContent());
			} catch (Exception ignore) {
				return null;
			}
			if (URL == null) {
				return null;
			}
			
			return getYoutubeKeyFromUrl(URL);
			
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
		    if(advisor != null) securityService.popAdvisor();
		}
		
		// 	no
		return null;
	}

	public String[] split(String s, String p) {
	    if (s == null || s.equals(""))
		return new String[0];
	    else
		return s.split(p);
	}

	/**
	 * Meant to guarantee that the permissions are set correctly on an assessment for a user.
	 * 
	 * @param item
	 * @param shouldHaveAccess
	 */
	public void checkItemPermissions(SimplePageItem item, boolean shouldHaveAccess) {
		checkItemPermissions(item, shouldHaveAccess, true);
	}

	/**
	 * 
	 * @param item
	 * @param shouldHaveAccess
	 * @param canRecurse
	 *            Is it allowed to delete the row in the table for the group and recurse to try
	 *            again. true for normal calls; false if called inside this code to avoid infinite loop
	 */
    // only called if the item should be under control. Also only called if the item is displayed
    // so if it's limited to a group, we'll never add people who aren't in the group, since the
    // item isn't shown to them.
	private void checkItemPermissions(SimplePageItem item, boolean shouldHaveAccess, boolean canRecurse) {
		if (SimplePageItem.DUMMY.equals(item.getSakaiId()))
			return;

		// for pages, presence of log entry is it
		if (item.getType() == SimplePageItem.PAGE) {
			Long itemId = item.getId();
			if (getLogEntry(itemId) != null)
				return;  // already ok
		    // if no log entry, create a dummy entry
			if (shouldHaveAccess) {
				String userId = getCurrentUserId();
				if (userId == null)
					userId = ".anon";
				SimplePageLogEntry entry = simplePageToolDao.makeLogEntry(userId, itemId, null);
				entry.setDummy(true);
				saveItem(entry);
				logCache.put(itemId + "--1", entry);
			}
			return;
		}

		SimplePageGroup group = simplePageToolDao.findGroup(item.getSakaiId());
		if (group == null) {
			// For some reason, the group doesn't exist. Let's re-add it.
			checkControlGroup(item, true);
			group = simplePageToolDao.findGroup(item.getSakaiId());
			if (group == null) {
				return;
			}
		}

		boolean success = true;
		String groupId = group.getGroupId();

		try {
			if (shouldHaveAccess) {
			    success = GroupPermissionsService.addCurrentUser(getCurrentPage().getSiteId(), getCurrentUserId(), groupId);
			} else {
			    success = GroupPermissionsService.removeUser(getCurrentPage().getSiteId(), getCurrentUserId(), groupId);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		// hmmm.... couldn't add or remove from group. Most likely the Sakai-level group
		// doesn't exist, although our database entry says it was created. Presumably
		// the user deleted the group for Site Info. Make very sure that's the cause,
		// or we'll create a duplicate group. I've seen failures for other reasons, such
		// as a weird permissions problem with the only maintain users trying to unjoin
		// a group.

		if (!success && canRecurse) {
			try {
			    AuthzGroupService.getAuthzGroup(groupId);
			    // group exists, it was something else. Who knows what
			    return;
			} catch (org.sakaiproject.authz.api.GroupNotDefinedException ee) {
				
			} catch (Exception e) {
			    // some other failure from getAuthzGroup, shouldn't be possible
			    log.warn("checkItemPermissions unable to join or unjoin group " + groupId);
			}

			log.warn("checkItemPermissions: User seems to have deleted group " + groupId + ". We'll recreate it.");

			// OK, group doesn't exist. When we recreate it, it's going to have a 
			// different groupId, so we have to back out of everything and reset it

			checkControlGroup(item, false);

			// huh? checkcontrolgroup just deleted it
			//simplePageToolDao.deleteItem(group);

			// We've undone it; call ourselves again, since the code at the
			// start will recreate the group

			checkItemPermissions(item, shouldHaveAccess, false);
		}

	}

	public void setYoutubeURL(String url) {
		youtubeURL = url;
	}

	public void setYoutubeId(long id) {
		youtubeId = id;
	}

	public void deleteYoutubeItem() {
		itemId = findItem(youtubeId).getId();
		deleteItem();
	}

	public void setMmUrl(String url) {
		mmUrl = url;
	}

	public void setMultipartMap(Map<String, MultipartFile> multipartMap) {
		this.multipartMap = multipartMap;
	}

	public String getCollectionId(boolean urls) {
		String siteId = getCurrentPage().getSiteId();
	    
		String pageOwner = getCurrentPage().getOwner();
		String collectionId;
		if (pageOwner == null) {
			collectionId = contentHostingService.getSiteCollection(siteId);
		}else {
			collectionId = "/user/" + pageOwner + "/stuff4/";
		}

	    // folder we really want
		String folder = collectionId + Validator.escapeResourceName(getPageTitle()) + "/";
		if (urls)
			folder = folder + "urls/";

	    // OK?
		try {
			contentHostingService.checkCollection(folder);
			// OK, let's use it
			return folder;
		} catch (Exception ignore) {};

   	    // no. create folders as needed

	    // if url subdir, need an extra level
		if (urls) {
			
			// try creating the root. if it exists this will fail. That's OK.
			String root = collectionId + Validator.escapeResourceName(getPageTitle()) + "/";
			try {
				ContentCollectionEdit edit = contentHostingService.addCollection(root);
				edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME,  Validator.escapeResourceName(getPageTitle()));
				contentHostingService.commitCollection(edit);
				// well, we got that far anyway
				collectionId = root;
			} catch (Exception ignore) {
			}

		}

	    // now try creating what we want
		try {
	    	ContentCollectionEdit edit = contentHostingService.addCollection(folder);
	    	if (urls)
	    		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "urls");
	    	else
	    		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, Validator.escapeResourceName(getPageTitle()));
		
	    	contentHostingService.commitCollection(edit);
	    	return folder; // worked. use it
		} catch (Exception ignore) {};

	    // didn't. do the best we can
		return collectionId;
	}

	public boolean isHtml(SimplePageItem i) {
		StringTokenizer token = new StringTokenizer(i.getSakaiId(), ".");

		String extension = "";
					    
		while (token.hasMoreTokens()) {
			extension = token.nextToken().toLowerCase();
		}
					    
	    // we are just starting to store the MIME type for resources now. So existing content
	    // won't have them.
		String mimeType = i.getHtml();
		if (mimeType != null && (mimeType.startsWith("http") || mimeType.equals("")))
			mimeType = null;
	    
		if (mimeType != null && (mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml"))
				|| mimeType == null && (extension.equals("html") || extension.equals("htm"))) {
			return true;
		}
		return false;
	}

	public static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

    // called by dialog to add inline multimedia item, or update existing
    // item if itemid is specified
	public void addMultimedia() {
		SecurityAdvisor advisor = null;
		try {
			if(getCurrentPage().getOwner() != null) {
				advisor = new SecurityAdvisor() {
					public SecurityAdvice isAllowed(String userId, String function, String reference) {
							return SecurityAdvice.ALLOWED;
					}
				};
				securityService.pushAdvisor(advisor);
			}
			if (!itemOk(itemId))
				return;
			if (!canEditPage())
				return;
			
			String name = null;
			String sakaiId = null;
			String mimeType = null;
			MultipartFile file = null;
			
			if (multipartMap.size() > 0) {
				// 	user specified a file, create it
				file = multipartMap.values().iterator().next();
				if (file.isEmpty())
					file = null;
			}
			
			if (file != null) {
				String collectionId = getCollectionId(false);
				// 	user specified a file, create it
				name = file.getOriginalFilename();
				if (name == null || name.length() == 0)
					name = file.getName();
				int i = name.lastIndexOf("/");
				if (i >= 0)
					name = name.substring(i+1);
				String base = name;
				String extension = "";
				i = name.lastIndexOf(".");
				if (i > 0) {
					base = name.substring(0, i);
					extension = name.substring(i+1);
				}
				
				mimeType = file.getContentType();
				try {
					ContentResourceEdit res = contentHostingService.addResource(collectionId, 
							  	Validator.escapeResourceName(base),
							  	Validator.escapeResourceName(extension),
							  	MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
					res.setContentType(mimeType);
					res.setContent(file.getInputStream());
					try {
						contentHostingService.commitResource(res,  NotificationService.NOTI_NONE);
						// 	there's a bug in the kernel that can cause
						// 	a null pointer if it can't determine the encoding
						// 	type. Since we want this code to work on old
						// 	systems, work around it.
					} catch (java.lang.NullPointerException e) {
						setErrMessage(messageLocator.getMessage("simplepage.resourcepossibleerror"));
					}
					sakaiId = res.getId();

					if("application/zip".equals(mimeType) && isWebsite) {
					    // We need to set the sakaiId to the resource id of the index file
					    sakaiId = expandZippedResource(sakaiId);
					    if (sakaiId == null)
						return;
					    
					    // We set this special type for the html field in the db. This allows us to
					    // map an icon onto website links in applicationContext.xml
					    mimeType = "LBWEBSITE";
					}		    
					
				} catch (org.sakaiproject.exception.OverQuotaException ignore) {
					setErrMessage(messageLocator.getMessage("simplepage.overquota"));
					return;
				} catch (Exception e) {
					setErrMessage(messageLocator.getMessage("simplepage.resourceerror").replace("{}", e.toString()));
					log.error("addMultimedia error 1 " + e);
					return;
				};
			} else if (mmUrl != null && !mmUrl.trim().equals("")) {
				// 	user specified a URL, create the item
				String url = mmUrl.trim();
				if (!url.matches("\\w+://.*")) {
					if (!url.startsWith("//"))
						url = "//" + url;
					url = "http:" + url;
				}
				
				name = url;
				String base = url;
				String extension = "";
				int i = url.lastIndexOf("/");
				if (i < 0) i = 0;
				i = url.lastIndexOf(".", i);
				if (i > 0) {
					extension = url.substring(i);
					base = url.substring(0,i);
				}
				
				String collectionId;
				SimplePage page = getCurrentPage();
				
				collectionId = getCollectionId(true);
				
				try {
					// 	urls aren't something people normally think of as resources. Let's hide them
					ContentResourceEdit res = contentHostingService.addResource(collectionId, 
							Validator.escapeResourceName(base),
							Validator.escapeResourceName(extension),
							MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
					res.setContentType("text/url");
					res.setResourceType("org.sakaiproject.content.types.urlResource");
					res.setContent(url.getBytes());
					contentHostingService.commitResource(res, NotificationService.NOTI_NONE);
					sakaiId = res.getId();
				} catch (org.sakaiproject.exception.OverQuotaException ignore) {
					setErrMessage(messageLocator.getMessage("simplepage.overquota"));
					return;
				} catch (Exception e) {
					setErrMessage(messageLocator.getMessage("simplepage.resourceerror").replace("{}", e.toString()));
					log.error("addMultimedia error 2 " + e);
					return;
				};
				// 	connect to url and get mime type
				mimeType = getTypeOfUrl(url);
				
			} else
				// 	nothing to do
				return;
			
			// 	itemId tells us whether it's an existing item
			// 	isMultimedia tells us whether resource or multimedia
			// 	sameWindow is only passed for existing items of type HTML/XHTML
			//   	for new items it should be set true for HTML/XTML, false otherwise
			//   	for existing items it should be set to the passed value for HTML/XMTL, false otherwise
			//   	it is ignored for isMultimedia, as those are always displayed inline in the current page
			
			SimplePageItem item = null;
			if (itemId == -1 && isMultimedia) {
				int seq = getItemsOnPage(getCurrentPageId()).size() + 1;
				item = simplePageToolDao.makeItem(getCurrentPageId(), seq, SimplePageItem.MULTIMEDIA, sakaiId, name);
			} else if(itemId == -1 && isWebsite) {
			    String websiteName = name.substring(0,name.indexOf("."));
			    int seq = getItemsOnPage(getCurrentPageId()).size() + 1;
			    item = simplePageToolDao.makeItem(getCurrentPageId(), seq, SimplePageItem.RESOURCE, sakaiId, websiteName);
			} else if (itemId == -1) {
				int seq = getItemsOnPage(getCurrentPageId()).size() + 1;
				item = simplePageToolDao.makeItem(getCurrentPageId(), seq, SimplePageItem.RESOURCE, sakaiId, name);
			} else {
				item = findItem(itemId);
				if (item == null)
					return;
				item.setSakaiId(sakaiId);
				item.setName(name);
			}
			
			if (mimeType != null) {
				item.setHtml(mimeType);
			} else {
				item.setHtml(null);
			}
			
			// 	if this is an existing item and a resource, leave it alone
			// 	otherwise initialize to false
			if (isMultimedia || itemId == -1)
				item.setSameWindow(false);
			
			clearImageSize(item);
			try {
				if (itemId == -1)
					saveItem(item);
				else
					update(item);
			} catch (Exception e) {
				// 	saveItem and update produce the errors
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		} finally {
		    if(advisor != null) securityService.popAdvisor();
		}
	}

	public boolean deleteRecursive(File path) throws FileNotFoundException{
		if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()){
			for (File f : path.listFiles()){
				ret = ret && deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}

	public void importCc() {
	    if (!canEditPage())
		return;

	    MultipartFile file = null;

	    if (multipartMap.size() > 0) {
		// user specified a file, create it
		file = multipartMap.values().iterator().next();
		if (file.isEmpty())
		    file = null;
	    }

	    if (file != null) {
		File cc = null;
		File root = null;
		try {
		    cc = File.createTempFile("ccloader", "file");
		    root = File.createTempFile("ccloader", "root");
		    if (root.exists())
			root.delete();
		    root.mkdir();
		    BufferedInputStream bis = new BufferedInputStream(file.getInputStream());
		    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(cc));
		    byte[] buffer = new byte[8096];
		    int n = 0;
		    while ((n = bis.read(buffer, 0, 8096)) >= 0) {
			if (n > 0)
			    bos.write(buffer, 0, n);
		    }
		    bis.close();
		    bos.close();

		    CartridgeLoader cartridgeLoader = ZipLoader.getUtilities(cc, root.getCanonicalPath());
		    Parser parser = Parser.createCartridgeParser(cartridgeLoader);

		    LessonEntity quizobject = null;
		    for (LessonEntity q = quizEntity; q != null; q = q.getNextEntity()) {
			if (q.getToolId().equals(quiztool))
			    quizobject = q;
		    }
		    
		    LessonEntity topicobject = null;
		    for (LessonEntity q = forumEntity; q != null; q = q.getNextEntity()) {
			if (q.getToolId().equals(topictool))
			    topicobject = q;
		    }

		    parser.parse(new PrintHandler(this, cartridgeLoader, simplePageToolDao, quizobject, topicobject, bltiEntity));
		    setTopRefresh();
		} catch (Exception e) {
		    setErrKey("simplepage.cc-error", "");
		    System.out.println("exception in importcc, backtrace follows " + e);
		    e.printStackTrace();
		} finally {
		    if (cc != null)
			try {
			    deleteRecursive(cc);
			} catch (Exception e){
			    System.out.println("Unable to delete temp file " + cc);
			}
			try {
			    deleteRecursive(root);
			} catch (Exception e){
			    System.out.println("Unable to delete temp file " + cc);
			}
		}
	    }
	}

    // called by edit dialog to update parameters of a Youtube item
	public void updateYoutube() {
		if (!itemOk(youtubeId))
		    return;
		if (!canEditPage())
		    return;

		SimplePageItem item = findItem(youtubeId);

		// find the new key, if the new thing is a legit youtube url
		String key = getYoutubeKeyFromUrl(youtubeURL);
		if (key == null) {
		    setErrMessage(messageLocator.getMessage("simplepage.must_be_youtube"));
		    return;
		}

		// oldkey had better work, since the youtube edit woudln't
		// be displayed if it wasn't recognized
		String oldkey = getYoutubeKey(item);

		// if there's a new youtube URL, and it's different from
		// the old one, update the URL if they are different
		if (key != null && !key.equals(oldkey)) {
		    String url = "http://www.youtube.com/watch#!v=" + key;
		    String siteId = getCurrentPage().getSiteId();
		    String collectionId = getCollectionId(true);

		    SecurityAdvisor advisor = null;
		    try {
		    	if(getCurrentPage().getOwner() != null) {
					advisor = new SecurityAdvisor() {
						public SecurityAdvice isAllowed(String userId, String function, String reference) {
							return SecurityAdvice.ALLOWED;
						}
					};
					securityService.pushAdvisor(advisor);
				}
		    	
		    	ContentResourceEdit res = contentHostingService.addResource(collectionId,Validator.escapeResourceName("Youtube video " + key),Validator.escapeResourceName("swf"),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
		    	res.setContentType("text/url");
		    	res.setResourceType("org.sakaiproject.content.types.urlResource");
		    	res.setContent(url.getBytes());
		    	contentHostingService.commitResource(res, NotificationService.NOTI_NONE);
		    	item.setSakaiId(res.getId());
		    	
		    } catch (org.sakaiproject.exception.OverQuotaException ignore) {
		    	setErrMessage(messageLocator.getMessage("simplepage.overquota"));
		    } catch (Exception e) {
		    	setErrMessage(messageLocator.getMessage("simplepage.resourceerror").replace("{}", e.toString()));
		    	log.error("addMultimedia error 3 " + e);
		    }finally {
		    	if(advisor != null) securityService.popAdvisor();
		    }
		}

		// even if there's some oddity with URLs, we do these updates
		item.setHeight(height);
		item.setWidth(width);
		item.setDescription(description);
		update(item);

		setItemGroups(item, selectedGroups);

	}

	/**
	 * Adds or removes the requirement to have site.upd in order to see a page
	 * i.e. hide or unhide a page
	 * @param pageId
	 *            The Id of the Page
	 * @param visible
	 * @return true for success, false for failure
	 * @throws IdUnusedException
	 *             , PermissionException
	 */
	private boolean pageVisibilityHelper(Site site, String pageId, boolean visible) throws IdUnusedException, PermissionException {
		SitePage page = site.getPage(pageId);
		List<ToolConfiguration> tools = page.getTools();
		Iterator<ToolConfiguration> iterator = tools.iterator();

		// If all the tools on a page require site.upd then only users with site.upd will see
		// the page in the site nav of Charon... not sure about the other Sakai portals floating
		// about
		while (iterator.hasNext()) {
			ToolConfiguration placement = iterator.next();
			Properties roleConfig = placement.getPlacementConfig();
			String roleList = roleConfig.getProperty("functions.require");
			String visibility = roleConfig.getProperty("sakai-portal:visible");
			boolean saveChanges = false;

			if (roleList == null) {
				roleList = "";
			}
			if (!(roleList.indexOf(SITE_UPD) > -1) && !visible) {
				if (roleList.length() > 0) {
					roleList += ",";
				}
				roleList += SITE_UPD;
				saveChanges = true;
			} else if ((roleList.indexOf(SITE_UPD) > -1) && visible) {
				roleList = roleList.replaceAll("," + SITE_UPD, "");
				roleList = roleList.replaceAll(SITE_UPD, "");
				saveChanges = true;
			}

			if (saveChanges) {
				roleConfig.setProperty("functions.require", roleList);
				if (visible)
				    roleConfig.remove("sakai-portal:visible");
				else
 				    roleConfig.setProperty("sakai-portal:visible", "false");

				placement.save();

				siteService.save(site);
			}

		}

		return true;
	}

    // used by edit dialog to update properties of a multimedia object
	public void updateMovie() {
		if (!itemOk(itemId))
		    return;
		if (!canEditPage())
		    return;

		SimplePageItem item = findItem(itemId);
		item.setHeight(height);
		item.setWidth(width);
		item.setDescription(description);
		item.setHtml(mimetype);
		update(item);

		setItemGroups(item, selectedGroups);

	}
	
	public void addCommentsSection() {
		if(canEditPage()) {
			SimplePageItem item = appendItem("", messageLocator.getMessage("simplepage.comments-section"), SimplePageItem.COMMENTS);
			item.setDescription(messageLocator.getMessage("simplepage.comments-section"));
			update(item);
			
			// Must clear the cache so that the new item appears on the page
			itemsCache.remove(getCurrentPage().getPageId());
		}else {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		}
	}
	
	/**
	 *  Admins can always edit.  Authors can edit for 30 minutes.
	 *  
	 *  The second parameter is only used to distinguish this method from
	 *  the one directly below it.  Allowing CommentsProducer to cache whether
	 *  or not the current user can edit the page, without having to hit the
	 *  database each time.
	 *  
	 */
	public boolean canModifyComment(SimplePageComment c, boolean canEditPage) {
		if(canEditPage) return true;
		
		if(c.getAuthor().equals(UserDirectoryService.getCurrentUser().getId())){
			// Author can edit for 30 minutes.
			if(System.currentTimeMillis() - c.getTimePosted().getTime() <= 1800000) {
				return true;
			}else {
				return false;
			}
		}else {
			return false;
		}
	}
	
	// See method above
	public boolean canModifyComment(SimplePageComment c) {
		return canModifyComment(c, canEditPage());
	}
	
	// May add or edit comments
	public String addComment() {
		boolean html = false;
		
		// Patch in the fancy editor's comment, if it's been used
		if(formattedComment != null && !formattedComment.equals("")) {
			comment = formattedComment;
			html = true;
		}
		
		StringBuilder error = new StringBuilder();
		comment = FormattedText.processFormattedText(comment, error);
		
		if(comment == null || comment.equals("")) {
			setErrMessage(messageLocator.getMessage("simplepage.empty-comment-error"));
			return "failure";
		}
		
		if(editId == null || editId.equals("")) {
			String userId = UserDirectoryService.getCurrentUser().getId();
			
			Double grade = null;
			if(findItem(itemId).getGradebookId() != null) {
				List<SimplePageComment> comments = simplePageToolDao.findCommentsOnItemByAuthor(itemId, userId);
				if(comments != null && comments.size() > 0) {
					grade = comments.get(0).getPoints();
				}
			}
			
			SimplePageComment commentObject = simplePageToolDao.makeComment(itemId, getCurrentPage().getPageId(), userId, comment, IdManager.getInstance().createUuid(), html);
			commentObject.setPoints(grade);
			
			saveItem(commentObject, false);
		}else {
			SimplePageComment commentObject = simplePageToolDao.findCommentById(Long.valueOf(editId));
			if(commentObject != null && canModifyComment(commentObject)) {
				commentObject.setComment(comment);
				update(commentObject, false);
			}else {
				setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
				return "failure";
			}
		}
		
		if(getCurrentPage().getOwner() != null) {
			SimpleStudentPage student = simplePageToolDao.findStudentPage(getCurrentPage().getTopParent());
			student.setLastCommentChange(new Date());
			update(student, false);
		}
		
		return "added-comment";
	}
	
	public String updateComments() {
		if(canEditPage()) {
			SimplePageItem comment = findItem(itemId);
			comment.setAnonymous(anonymous);
			setItemGroups(comment, selectedGroups);
			comment.setRequired(required);
			comment.setPrerequisite(prerequisite);
			
			if(maxPoints == null || maxPoints.equals("")) {
				maxPoints = "1";
			}
			
			if(graded) {
				int points;
				try {
					points = Integer.valueOf(maxPoints);
				}catch(Exception ex) {
					setErrMessage(messageLocator.getMessage("simplepage.integer-expected"));
					return "failure";
				}
				
				// TODO: should update instead of delete/add
				if(comment.getGradebookId() != null && !comment.getGradebookPoints().equals(points)) {
					gradebookIfc.removeExternalAssessment(getCurrentSiteId(), comment.getGradebookId());
				}
				
				if(comment.getGradebookId() == null || !comment.getGradebookPoints().equals(points)) {
					String pageTitle = "";
					String gradebookId = "";
					
					boolean add = true;
					
					if(comment.getPageId() >= 0) {
						pageTitle = getPage(comment.getPageId()).getTitle();
						gradebookId = "lesson-builder:comment:" + comment.getId();
						
						add = gradebookIfc.addExternalAssessment(getCurrentSiteId(), "lesson-builder:comment:" + comment.getId(), null,
								pageTitle + " Comments (item:" + comment.getId() + ")", Integer.valueOf(maxPoints), null, "Lesson Builder");
						if(!add) {
							setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
						}else {
							comment.setGradebookTitle(pageTitle + " Comments (item:" + comment.getId() + ")");
						}
					}else {
						// Must be a student page comments tool.
						SimpleStudentPage studentPage = simplePageToolDao.findStudentPage(Long.valueOf(comment.getSakaiId()));
						SimplePageItem studentPageItem = simplePageToolDao.findItem(studentPage.getItemId());
						
						//pageTitle = simplePageToolDao.findStudentPage(Long.valueOf(comment.getSakaiId())).getTitle();
						gradebookId = "lesson-builder:page-comment:" + studentPageItem.getId();
						
					}
					
					if(add) {
						comment.setGradebookId(gradebookId);
						comment.setGradebookPoints(points);
						regradeComments(comment);
					}
				}
			}else if(comment.getGradebookId() != null && comment.getPageId() >= 0) {
				gradebookIfc.removeExternalAssessment(getCurrentSiteId(), comment.getGradebookId());
				comment.setGradebookId(null);
				comment.setGradebookPoints(null);
			}
			
 			// for forced comments, the UI won't ever do this, but if
 			// it does, update will fail with permissions
			update(comment);
			return "success";
		}else {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "failure";
		}
	}
	
	private void regradeComments(SimplePageItem comment) {
		List<SimplePageComment> comments = simplePageToolDao.findComments(comment.getId());
		for(SimplePageComment c : comments) {
			if(c.getPoints() != null) {
				gradebookIfc.updateExternalAssessmentScore(getCurrentSiteId(), comment.getGradebookId(),
						c.getAuthor(), String.valueOf(c.getPoints()));
			}
		}
	}
	
	/**
	 * Comments aren't actually deleted. The comment field is set to empty.
	 * This is so that the namings remain consistent when the comment section
	 * is set to show names as anonymous.  Otherwise, deleting a post could change
	 * the numbering, which hinders discussion.
	 */
	public String deleteComment(String commentUUID) {
		SimplePageComment comment = simplePageToolDao.findCommentByUUID(commentUUID);
		
		if(comment != null && comment.getPageId() == getCurrentPage().getPageId()) {
			if(canModifyComment(comment)) {
				comment.setComment("");
				update(comment, false);
				return "success";
			}
		}
		
		setErrMessage(messageLocator.getMessage("simplepage.comment-permissions-error"));
		return "failure";
	}
	
	public void addStudentContentSection() {
		if(getCurrentPage().getOwner() == null && canEditPage()) {
			SimplePageItem item = appendItem("", messageLocator.getMessage("simplepage.student-content"), SimplePageItem.STUDENT_CONTENT);
			item.setDescription(messageLocator.getMessage("simplepage.student-content"));
			update(item);
			
			// Must clear the cache so that the new item appears on the page
			itemsCache.remove(getCurrentPage().getPageId());
		}else {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		}
	}
	
	public boolean createStudentPage(long itemId) {
		SimplePage curr = getCurrentPage();
		User user = UserDirectoryService.getCurrentUser();
		
		// Need to make sure the section exists
		SimplePageItem containerItem = simplePageToolDao.findItem(itemId);
		
		// We want to make sure each student only has one top level page per section.
		SimpleStudentPage page = simplePageToolDao.findStudentPage(itemId, user.getId());
		
		if(page == null && containerItem != null && containerItem.getType() == SimplePageItem.STUDENT_CONTENT && canReadPage()) {
			// First create object in lesson_builder_pages.
			String title = user.getDisplayName();
			if (containerItem.isAnonymous()) {
			    List<SimpleStudentPage>  otherPages = simplePageToolDao.findStudentPages(itemId);
			    int serial = 1;
			    if (otherPages != null)
				serial = otherPages.size() + 1;
			    title = messageLocator.getMessage("simplepage.anonymous") + " " + serial;
			}			
			SimplePage newPage = simplePageToolDao.makePage(curr.getToolId(), curr.getSiteId(), title, curr.getPageId(), null);
			newPage.setOwner(user.getId());
			newPage.setGroupOwned(false);
			saveItem(newPage, false);
			
			// Then attach the lesson_builder_student_pages item.
			page = simplePageToolDao.makeStudentPage(itemId, newPage.getPageId(), title, user.getId(), false);
			
			SimplePageItem commentsItem = simplePageToolDao.makeItem(-1, -1, SimplePageItem.COMMENTS, null, messageLocator.getMessage("simplepage.comments-section"));
			saveItem(commentsItem, false);
			
			page.setCommentsSection(commentsItem.getId());
			
			saveItem(page, false);
			
			commentsItem.setAnonymous(containerItem.getForcedCommentsAnonymous());
			commentsItem.setSakaiId(String.valueOf(page.getId()));
			update(commentsItem, false);
			
			newPage.setTopParent(page.getId());
			update(newPage, false);
			
			try {
				updatePageItem(containerItem.getId());
				updatePageObject(newPage.getPageId());
				adjustPath("push", newPage.getPageId(), containerItem.getId(), newPage.getTitle());
			}catch(Exception ex) {
				setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
				return false;
			}
			
			// Reset the edit cache so that they can actually edit their page.
			editPrivs = 1;
			
			return true;
		}else if(page != null) { 
			setErrMessage(messageLocator.getMessage("simplepage.page-exists"));
			return false;
		}else{
			return false;
		}
	}
	
	public HashMap<Long, SimplePageLogEntry> cacheStudentPageLogEntries(long itemId) {
		List<SimplePageLogEntry> entries = simplePageToolDao.getStudentPageLogEntries(itemId, UserDirectoryService.getCurrentUser().getId());
		
		HashMap<Long, SimplePageLogEntry> map = new HashMap<Long, SimplePageLogEntry>();
		for(SimplePageLogEntry entry : entries) {
			logCache.put(entry.getItemId() + "-" + entry.getStudentPageId(), entry);
			map.put(entry.getStudentPageId(), entry);
		}
		
		return map;
	}
	
	private boolean pushAdvisor() {
		if(getCurrentPage().getOwner() != null) {
			securityService.pushAdvisor(new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			});
			return true;
		}else {
			return false;
		}
	}
	
	private void popAdvisor() {
		securityService.popAdvisor();
	}
	
	public String updateStudent() {
		if(canEditPage()) {
			SimplePageItem page = findItem(itemId);
			page.setAnonymous(anonymous);
			page.setShowComments(comments);
			page.setForcedCommentsAnonymous(forcedAnon);
			page.setRequired(required);
			page.setPrerequisite(prerequisite);
			setItemGroups(page, selectedGroups);
			
			// Update the comments tools to reflect any changes
			if(comments) {
				List<SimpleStudentPage> pages = simplePageToolDao.findStudentPages(itemId);
				for(SimpleStudentPage p : pages) {
					if(p.getCommentsSection() != null) {
						SimplePageItem item = simplePageToolDao.findItem(p.getCommentsSection());
						if(item.isAnonymous() != forcedAnon) {
							item.setAnonymous(forcedAnon);
							update(item);
						}
					}
				}
			}
			
			if(maxPoints == null || maxPoints.equals("")) {
				maxPoints = "1";
			}
			
			if(sMaxPoints == null || sMaxPoints.equals("")) {
				sMaxPoints = "1";
			}
			
			// Handle the grading of pages
			if(graded) {
				int points;
				try {
					points = Integer.valueOf(maxPoints);
				}catch(Exception ex) {
					setErrMessage(messageLocator.getMessage("simplepage.integer-expected"));
					return "failure";
				}
				
				// TODO: should update instead of delete/add
				if(page.getGradebookId() != null && !page.getGradebookPoints().equals(points)) {
					gradebookIfc.removeExternalAssessment(getCurrentSiteId(), page.getGradebookId());
				}
				
				if(page.getGradebookId() == null || !page.getGradebookPoints().equals(points)) {
					boolean add = gradebookIfc.addExternalAssessment(getCurrentSiteId(), "lesson-builder:page:" + page.getId(), null,
							getPage(page.getPageId()).getTitle() + " Student Pages (item:" + page.getId() + ")", Integer.valueOf(maxPoints), null, "Lesson Builder");
					
					if(!add) {
						setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
					}else {
						page.setGradebookId("lesson-builder:page:" + page.getId());
						page.setGradebookTitle(getPage(page.getPageId()).getTitle() + " Student Pages (item:" + page.getId() + ")");
						page.setGradebookPoints(points);
						regradeStudentPages(page);
					}
				}
			}else if(page.getGradebookId() != null) {
				gradebookIfc.removeExternalAssessment(getCurrentSiteId(), page.getGradebookId());
				page.setGradebookId(null);
				page.setGradebookPoints(null);
			}
			
			// Handling the grading of comments on pages
			if(sGraded) {
				int points;
				try {
					points = Integer.valueOf(sMaxPoints);
				}catch(Exception ex) {
					setErrMessage(messageLocator.getMessage("simplepage.integer-expected"));
					return "failure";
				}
				
				// todo: use update instead of delete, add
				if(page.getAltGradebook() != null && !page.getAltPoints().equals(points)) {
					gradebookIfc.removeExternalAssessment(getCurrentSiteId(), page.getAltGradebook());
				}
				
				if(page.getAltGradebook() == null || !page.getAltPoints().equals(points)) {
					String title = getPage(page.getPageId()).getTitle() + " Student Page Comments (item:" + page.getId() + ")";
					boolean add = gradebookIfc.addExternalAssessment(getCurrentSiteId(), "lesson-builder:page-comment:" + page.getId(), null,
							title, points, null, "Lesson Builder");
					
					// The assessment couldn't be added
					if(!add) {
						setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
					}else {
						page.setAltGradebook("lesson-builder:page-comment:" + page.getId());
						page.setAltGradebookTitle(title);
						page.setAltPoints(points);
						regradeStudentPageComments(page);
					}
				}
			}else if(page.getAltGradebook() != null) {
				gradebookIfc.removeExternalAssessment(getCurrentSiteId(), page.getAltGradebook());
				page.setAltGradebook(null);
				page.setAltPoints(null);
				ungradeStudentPageComments(page);
			}
			
			update(page);
			
			return "success";
		}else {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "failure";
		}
	}
	
	private void regradeStudentPageComments(SimplePageItem pageItem) {
		List<SimpleStudentPage> pages = simplePageToolDao.findStudentPages(pageItem.getId());
		for(SimpleStudentPage c : pages) {
			SimplePageItem comments = findItem(c.getCommentsSection());
			comments.setGradebookId(pageItem.getAltGradebook());
			comments.setGradebookPoints(pageItem.getAltPoints());
			update(comments);
			regradeComments(comments);
		}
	}
	
	private void ungradeStudentPageComments(SimplePageItem pageItem) {
		List<SimpleStudentPage> pages = simplePageToolDao.findStudentPages(pageItem.getId());
		for(SimpleStudentPage c : pages) {
			SimplePageItem comments = findItem(c.getCommentsSection());
			comments.setGradebookId(null);
			comments.setGradebookPoints(null);
			update(comments);
		}
	}
	
	private void regradeStudentPages(SimplePageItem pageItem) {
		List<SimpleStudentPage> pages = simplePageToolDao.findStudentPages(pageItem.getId());
		for(SimpleStudentPage c : pages) {
			if(c.getPoints() != null) {
				gradebookIfc.updateExternalAssessmentScore(getCurrentSiteId(), pageItem.getGradebookId(),
						c.getOwner(), String.valueOf(c.getPoints()));
			}
		}
	}
	
	private String expandZippedResource(String resourceId) {
		String contentCollectionId = resourceId.substring(0, resourceId.lastIndexOf(".")) + "/";

		try {
			contentHostingService.removeCollection(contentCollectionId);
		} catch (Exception e) {
			log.info("Failed to delete expanded collection");
		}

		// Q: Are we running a kernel with KNL-273?
		Class contentHostingInterface = ContentHostingService.class;
		try {
			Method expandMethod = contentHostingInterface.getMethod("expandZippedResource", new Class[] { String.class });
			// Expand the website
			expandMethod.invoke(contentHostingService, new Object[] { resourceId });
		} catch (NoSuchMethodException nsme) {
			// A: No; should be impossible, UI already tested
			return null;
		} catch (Exception e) {
			// This is very strange. The kernel code will normally trap exceptions
		        // and print a backtrace, robbing us of any ability to see that something
		        // has gone wrong.
			log.error("Exception thrown by expandZippedResource", e);
			setErrKey("simplepage.website.cantexpand", null);
			return null;
		}

		// Now set the html ok flag

		try {
			ContentCollectionEdit cce = contentHostingService.editCollection(contentCollectionId);

			ResourcePropertiesEdit props = cce.getPropertiesEdit();
			props.addProperty(PROP_ALLOW_INLINE, "true");
			List<String> children = cce.getMembers();

			for (int j = 0; j < children.size(); j++) {
				String resId = children.get(j);
				if (resId.endsWith("/")) {
					setPropertyOnFolderRecursively(resId, PROP_ALLOW_INLINE, "true");
				}
			}

			contentHostingService.commitCollection(cce);
			// when you tell someone to create a zip file with index.html at the
			// top level, it's unclear whether they do "zip directory" or "cd; zip *"
			// make both work
			
			ContentCollection cc = cce;

			if (children.size() == 1 && children.get(0).endsWith("/")) {
			    contentCollectionId = children.get(0);
			    cc = contentHostingService.getCollection(contentCollectionId);
			}

			// Now lets work out what type it is and return the appropriate
			// index url

			String index = null;

			String name = contentCollectionId.substring(0, contentCollectionId.lastIndexOf("/"));
			name = name.substring(name.lastIndexOf("/") + 1);
			if (name.endsWith("_HTML")) {
				// This is probably Wimba Create as wc adds this suffix to the
				// zips it creates
				name = name.substring(0, name.indexOf("_HTML"));
			}

			ContentEntity ce = cc.getMember(contentCollectionId + name + ".xml");
			if (ce != null) {
				index = "index.htm";
			}

			// Test for Camtasia
			ce = cc.getMember(contentCollectionId + "ProductionInfo.xml");
			if (ce != null) {
				index = name + ".html";
			}
			
			// Test for Articulate
			ce = cc.getMember(contentCollectionId + "player.html");
			if (ce != null) {
				index = "player.html";
			}

			// Test for generic web site
			ce = cc.getMember(contentCollectionId + "index.html");
			if (ce != null) {
			    index = "index.html";
			}

			ce = cc.getMember(contentCollectionId + "index.htm");
			if (ce != null) {
			    index = "index.htm";
			}

			if (index == null) {
			    // /content/group/nnnn/folder
			    int i = contentCollectionId.indexOf("/", 1);
			    i = contentCollectionId.indexOf("/", i+1);

			    setErrKey("simplepage.website.noindex", contentCollectionId.substring(i));
			    return null;
			}

			//String relativeUrl = contentCollectionId.substring(contentCollectionId.indexOf("/Lesson Builder")) + index;
			// collections end in / already
			String relativeUrl = contentCollectionId + index;
			return relativeUrl;
		} catch (Exception e) {
			log.error(e);
			setErrKey("simplepage.website.cantexpand", null);
			return null;
		}
	}
	
    // see if there is a folder in which images, etc, are likely to be
    // stored for this resource. This only applies to HTML files
    // for index.html, etc, it's the containing folder
    // otherwise, if it's an HTML file, look for a folder with the same name
	public static String associatedFolder(String resourceId) {
	    int i = resourceId.lastIndexOf("/");
	    String folder = null;
	    String name = null;
	    if (i >= 0) {
		folder = resourceId.substring(0, i+1);  // include trailing
		name = resourceId.substring(i+1);
	    } else
		return null;

	    String folderName = resourceId.substring(0, i);
	    i = folderName.lastIndexOf("/");
	    if (i >= 0)
		folderName = folderName.substring(i+1);
	    else
		return null;
	    if (folderName.endsWith("_HTML"))  // wimba create
		folderName = folderName.substring(0, folderName.indexOf("_HTML"));

	    // folder is whole folder
	    // folderName is last atom of folder name
	    // name is last atom of resource id

	    if (name.equals("index.html") || name.equals("index.htm") || name.equals(folderName + ".html"))
		return folder;

	    if (resourceId.endsWith(".html") || resourceId.endsWith(".htm")) {
		i = resourceId.lastIndexOf(".");
		resourceId = resourceId.substring(0, i) + "/";
		// no need to check whether it actually exists
		return resourceId;
	    }
	    return null;
	}
		

	private void setPropertyOnFolderRecursively(String resourceId, String property, String value) {

		try {
			if (contentHostingService.isCollection(resourceId)) {
				// collection
				ContentCollectionEdit col = contentHostingService.editCollection(resourceId);

				ResourcePropertiesEdit resourceProperties = col.getPropertiesEdit();
				resourceProperties.addProperty(property, Boolean.valueOf(value).toString());
				contentHostingService.commitCollection(col);

				List<String> children = col.getMembers();
				for (int i = 0; i < children.size(); i++) {
					String resId = children.get(i);
					if (resId.endsWith("/")) {
						setPropertyOnFolderRecursively(resId, property, value);
					}
				}

			} else {
				// resource
				ContentResourceEdit res = contentHostingService.editResource(resourceId);
				ResourcePropertiesEdit resourceProperties = res.getPropertiesEdit();
				resourceProperties.addProperty(property, Boolean.valueOf(value).toString());
				contentHostingService.commitResource(res, NotificationService.NOTI_NONE);
			}
		} catch (Exception pe) {
			pe.printStackTrace();
		}
	}
	
	/**
	 * Returns an ArrayList containing all of the system-wide and site-wide CSS files.
	 * 
	 * One entry may be null, to separate system-wide from site-wide.
	 * 
	 * Caches lookups, to prevent extra database hits.
	 */
	public ArrayList<ContentResource> getAvailableCss() {
		ArrayList<ContentResource> list = new ArrayList<ContentResource>();
		
		String collectionId = contentHostingService.getSiteCollection(getCurrentSiteId()) + "LB-CSS/";
		
		List<ContentResource> resources = (List<ContentResource>) resourceCache.get(collectionId);
		if(resources == null) {
			resources = contentHostingService.getAllResources(collectionId);
			if(resources == null) resources = new ArrayList<ContentResource>();
			
			resourceCache.put(collectionId, resources);
		}
		
		for(ContentResource r : resources) {
			if(r.getUrl().endsWith(".css")) {
				list.add(r);
			}
		}
		
		collectionId = "/public/LB-CSS/";
		
		resources = null;
		resources = (List<ContentResource>) resourceCache.get(collectionId);
		if(resources == null) {
			resources = contentHostingService.getAllResources(collectionId);
			if(resources == null) resources = new ArrayList<ContentResource>();
			
			resourceCache.put(collectionId, resources);
		}
		
		// Insert separator
		if(list.size() > 0 && resources.size() > 0) {
			list.add(null);
		}
		
		for(ContentResource r : resources) {
			if(r.getUrl().endsWith(".css")) {
				list.add(r);
			}
		}
		
		
		return list;
	}
	/**
	 * First checks if a sheet has been explicitly set.  Then checks for a default
	 * at the site level.  It then finally checks to see if there is a default on the
	 * system level.
	 * 
	 * Caches lookups to prevent too many lookups in the database.
	 */
	public ContentResource getCssForCurrentPage() {
		ContentResource resource = null;
		
		// I'm always using ArrayList for the resourceCache so that I can distinguish
		// between never having looked up the resource, and the resource not being there.
		// Otherwise, if I just check for null, if a resource isn't there, it will still check
		// every time.
		
		String collectionId = getCurrentPage().getCssSheet();
		if(getCurrentPage().getCssSheet() != null) {
			try {
				ArrayList<ContentResource> resources = (ArrayList<ContentResource>) resourceCache.get(collectionId);
				if(resources == null) {
					resource = contentHostingService.getResource(collectionId);
					resources = new ArrayList<ContentResource>();
					resources.add(resource);
					resourceCache.put(collectionId, resources);
				}
				
				if(resources.size() > 0) {
					return resources.get(0);
				}else {
					throw new Exception();
				}
			}catch(Exception ex) {
				if(canEditPage()) {
					setErrMessage(messageLocator.getMessage("simplepage.broken-css"));
				}
				
				resourceCache.put(collectionId, new ArrayList<ContentResource>());
			}
		}
		
		collectionId = contentHostingService.getSiteCollection(getCurrentSiteId())
				+ "LB-CSS/" + ServerConfigurationService.getString("lessonbuilder.default.css", "default.css");
		
		try {
			ArrayList<ContentResource> resources = (ArrayList<ContentResource>) resourceCache.get(collectionId);
			if(resources == null) {
				resource = contentHostingService.getResource(collectionId);
				resources = new ArrayList<ContentResource>();
				resources.add(resource);
				resourceCache.put(collectionId, resources);
			}
			
			if(resources.size() > 0) {
				return resources.get(0);
			}
		}catch(Exception ignore) {
			resourceCache.put(collectionId, new ArrayList<ContentResource>());
		}
		
		collectionId = "/public/LB-CSS/" + ServerConfigurationService.getString("lessonbuilder.default.css", "default.css");
		
		try {
			ArrayList<ContentResource> resources = (ArrayList<ContentResource>) resourceCache.get(collectionId);
			if(resources == null) {
				resource = contentHostingService.getResource(collectionId);
				resources = new ArrayList<ContentResource>();
				resources.add(resource);
				resourceCache.put(collectionId, resources);
			}
			
			if(resources.size() > 0) {
				return resources.get(0);
			}
		}catch(Exception ignore) {
			resourceCache.put(collectionId, new ArrayList<ContentResource>());
		}
				
		return null;
	}
}