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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.lessonbuildertool.tool.beans;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.AbstractList;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.StringTokenizer;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.assignment.api.Assignment;
import org.sakaiproject.assignment.api.AssignmentSubmission;
import org.sakaiproject.assignment.api.AssignmentContent;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;

import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.authz.cover.AuthzGroupService;
import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.GroupPermissionsService;
import org.sakaiproject.lessonbuildertool.service.SimplePageToolService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.grading.AssessmentGradingIfc;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacadeQueriesAPI;
import org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Validator;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.springframework.web.multipart.MultipartFile;

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
//    is primarily in the DAO, but the DAO only checks permissions. We have to make sure we only acccess pages
//    and items in our site
// 2) It is used by RSF to access data. Normally the bean is associated with a specific page. However the 
//    UI often has to update attributes of a specific item. For that use, there are some item-specific variables
//    in the bean. They are only meaningful during item operations, when itemId will show which item is involved.
// While the bean is used by all the producers, the caching was designed specifically for ShowPageProducer.
// That's because it is used a lot more often than the others. ShowPageProducer should do all data acess through
// the methods here that cache. There is also caching by hibernate. However this code is cheaper, partly because
// it doesn't have to do synchronization (since it applies just to processing one transaction).

public class SimplePageBean {
	private static Log log = LogFactory.getLog(SimplePageBean.class);

	public enum Status {
		NOT_REQUIRED, REQUIRED, DISABLED, COMPLETED, FAILED
	}

	public static final Pattern YOUTUBE_PATTERN = Pattern.compile("v[=/_][\\w-]{11}");
	public static final String GRADES[] = { "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "E", "F" };
        public static final String FILTERHTML = "lessonbuilder.filterhtml";
        public static final String LESSONBUILDER_ITEMID = "lessonbuilder.itemid";
	public static final String LESSONBUILDER_PATH = "lessonbuilder.path";

	private static String PAGE = "simplepage.page";
	private static String SITE_UPD = "site.upd";
	private ToolManager toolManager;
	private SecurityService securityService;
	private SiteService siteService;
	private SimplePageToolDao simplePageToolDao;
	private String contents = null;
	private String pageTitle = null;
	private String subpageTitle = null;
        private boolean subpageNext = false;
        private boolean subpageButton = false;
	private List<Long> currentPath = null;
    	private Set<Long>allowedPages = null;    

        private boolean filterHtml = ServerConfigurationService.getBoolean(FILTERHTML, false);

	public String selectedAssignment = null;

    // generic entity stuff. selectedEntity is the string
    // coming from the picker. We'll use the same variable for any entity type
	public String selectedEntity = null;

	public String selectedQuiz = null;

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

	private String linkUrl;

	private String height, width;

	private String description;
	private String name;
	private boolean required;
	private boolean subrequirement;
	private boolean prerequisite;
	private String dropDown;
	private String points;
        private String mimetype;

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
	private Map<Long, List<SimplePageItem>> itemsCache = new HashMap<Long, List<SimplePageItem>> ();
	private Map<Long, SimplePageLogEntry> logCache = new HashMap<Long, SimplePageLogEntry>();
        private Map<Long, Boolean> completeCache = new HashMap<Long, Boolean>();
        private Map<Long, PublishedAssessmentData> quizCache = new HashMap<Long, PublishedAssessmentData>();
        private Map<String, Assignment> assignmentCache = new HashMap<String, Assignment>();
        private Map<String, Integer> assignmentTypeCache = new HashMap<String, Integer>();

        public static class PathEntry {
	    public Long pageId;
	    public Long pageItemId;
	    public String title;
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

	private GradingService gradingService = new GradingService();

    // Use this rather than PublishedAssessmentService so we can do loadPublishedAssessment.
    // That produces far fewer queries, because it doesn't load the questions, which
    // getPublishedAssessment does. Even loadPublishedAssessment does more than we need
    // However I'm reluctant to go down to the hibernate level with someone else's tool.
    // This is really only used by things called from ShowPageProducer, as noted above.

        private PublishedAssessmentFacadeQueriesAPI pService;

	private SessionManager sessionManager;

	public void setSessionManager(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	private ContentHostingService contentHostingService;

	public void setContentHostingService(ContentHostingService contentHostingService) {
		this.contentHostingService = contentHostingService;
	}

	public PublishedAssessmentFacadeQueriesAPI getPublishedAssessmentFacadeQueries() {
	    return pService;
	}

	public void setPublishedAssessmentFacadeQueries(
							PublishedAssessmentFacadeQueriesAPI publishedAssessmentFacadeQueries) {
	    this.pService = publishedAssessmentFacadeQueries;
	}

        LessonEntity forumEntity = null;
        public void setForumEntity(Object e) {
	    forumEntity = (LessonEntity)e;
        }

    // End Injection

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

	public void setDropDown(String dropDown) {
		this.dropDown = dropDown;
	}

	public void setPoints(String points) {
		this.points = points;
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

    // The permissions model assumes that all code operates on the current
    // page. When the current page is set, the set code verifies that the
    // page is in the current site. However when operatig on items, we
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
			// a lot of people feel users shouldn't be able to add javascirpt, etc
			// to their HTML. I thik enforcing that makes Sakai less than useful.
			// So check config options to see whether to do that check
			String html = contents;
			if (filterHtml && ! "false".equals(placement.getPlacementConfig().getProperty("filterHtml")) ||
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
				simplePageToolDao.update(item);
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
		return processResource(SimplePageItem.MULTIMEDIA);
	}

	public String processResource() {
		return processResource(SimplePageItem.RESOURCE);
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
	    } catch (Exception e) {log.error("connection error " + e);};
	    return mimeType;
	}

    // return call from the file picker, used by add resource
    // the picker communicates with us by session variables
	public String processResource(int type) {
		if (!canEditPage())
		    return "permission-failed";

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		List refs = null;
		String id = null;
		String name = null;
		String mimeType = null;

		if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null && toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {

			refs = (List) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			if (refs == null || refs.size() != 1) {
				return "no-reference";
			}
			Reference ref = (Reference) refs.get(0);
			id = ref.getId();

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
				try {
					ContentResourceEdit res = contentHostingService.editResource(id);
					res.setContentType("text/url");
					res.setResourceType("org.sakaiproject.content.types.urlResource");
					url = new String(res.getContent());
					contentHostingService.commitResource(res);
				} catch (Exception ignore) {
					return "no-reference";
				}
				// part 2, find the actual data type.
				if (url != null)
				    mimeType = getTypeOfUrl(url);
			}

		} else {
			return "cancel";
		}

		try {
			contentHostingService.checkResource(id);
		} catch (PermissionException e) {
			return "permission-exception";
		} catch (IdUnusedException e) {
			// Typically Means Cancel
			return "cancel";
		} catch (TypeException e) {
			return "type-exception";
		}

		Long itemId = (Long)toolSession.getAttribute(LESSONBUILDER_ITEMID);

		if (!itemOk(itemId))
		    return "permission-failed";

		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
		toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
		toolSession.removeAttribute(LESSONBUILDER_ITEMID);

		String[] split = id.split("/");

		if (itemId != null && itemId != -1) {
		    SimplePageItem i;
		    i = findItem(itemId);
		    i.setSakaiId(id);
		    if (type == SimplePageItem.MULTIMEDIA && mimeType != null)
			i.setHtml(mimeType);
		    i.setName(name != null ? name : split[split.length - 1]);
		    clearImageSize(i);
		    simplePageToolDao.update(i);
		} else {
		    SimplePageItem i;
 	            i = appendItem(id, (name != null ? name : split[split.length - 1]), type);
		    if (type == SimplePageItem.MULTIMEDIA && mimeType != null) {
			i.setHtml(mimeType);
			simplePageToolDao.update(i);
		    }
		}

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
		int seq = getItemsOnPage(getCurrentPageId()).size() + 1;
		SimplePageItem i = new SimplePageItem(getCurrentPageId(), seq, type, id, name);

		// defaults to a fixed width and height, appropriate for some things, but for an
		// image, leave it blank, since browser will then use the native size
		clearImageSize(i);

		simplePageToolDao.saveItem(i);
		return i;
	}

	/**
	 * User can edit if he has site.upd or simplepage.upd. These do database queries, so 
	 * try to save the results, rather than calling them many times on a page.
	 * @return
	 */
	public boolean canEditPage() {
		String ref = "/site/" + toolManager.getCurrentPlacement().getContext();
		return securityService.unlock(SimplePageToolService.PERMISSION_LESSONBUILDER_UPDATE, ref);
	}

	public boolean canReadPage() {
		String ref = "/site/" + toolManager.getCurrentPlacement().getContext();
		return securityService.unlock(SimplePageToolService.PERMISSION_LESSONBUILDER_READ, ref);
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
		for (SimplePageItem item: items) {
		    itemCache.put(item.getId(), item);
		}
		itemsCache.put(pageid, items);
		return items;
	}

	/**
	 * Removes the item from the page, doesn't actually delete it.
	 * 
	 * @return
	 */
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
		    checkControlGroup(i);
		}
		b = simplePageToolDao.deleteItem(i);


		if (b) {
			List<SimplePageItem> list = getItemsOnPage(getCurrentPageId());
			for (SimplePageItem item : list) {
				if (item.getSequence() > seq) {
					item.setSequence(item.getSequence() - 1);
					simplePageToolDao.update(item);
				}
			}

			return "successDelete";
		} else {
			log.warn("Error Deleting Item: " + itemId);
			return "failure";
		}
	}

	private Site getCurrentSite() {
		Site site = null;
		try {
			site = siteService.getSite(toolManager.getCurrentPlacement().getContext());
		} catch (Exception impossible) {
			impossible.printStackTrace();
		}
		return site;
	}

	public String getCurrentSiteId() {
		try {
		    return toolManager.getCurrentPlacement().getContext();
		} catch (Exception impossible) {
		    return null;
		}
	}

    // recall that code typically operates on a "current page." See below for
    // the code that sets a new current page. We also have a current item, which
    // is the item defining the page. I.e. if the page is a subpage of anotehr
    // one, this is the item on the parent page pointing to this page.  If it's
    // a top-level page, it's a dummy item.  The item is needed in order to do
    // access checks. Whether an item is required, etc, is stored in the item.
    // in theory a page could be caled from several other pages, with different
    // access control parameters. So we need to know the actual item on the page
    // page from which this page was called.

    // we need to track the pageitem because references to the same page can appear
    // in several places. In theory each one could have different status of availability
    // so we need to know which in order to check availability
	public void updatePageItem(long item) throws PermissionException {
	    SimplePageItem i = findItem(item);
	    if (i != null) {
		if ((long)currentPageId != (long)Long.valueOf(i.getSakaiId())) {
		    log.warn("updatepage item permission failure " + i + " " + Long.valueOf(i.getSakaiId()) + " " + currentPageId);
		    throw new PermissionException(getCurrentUserId(), "set item", Long.toString(item));
		}
	    }

	    currentPageItemId = item;
	    sessionManager.getCurrentToolSession().setAttribute("current-pagetool-item", item);
	}

    // update our concept of the current page. it is imperative to make sure the page is in
    // the current site, or people could hack on other people's pages

    // if you call updatePageObject, consider whether you need to call updatePageItem as well
    // this combines two functions, so maybe not, but any time you're goign to a new page 
    // you should do both. Make sure all Producers set the page to the one they will work on
	public void updatePageObject(long l) throws PermissionException {
		if (l != previousPageId) {
			currentPage = simplePageToolDao.getPage(l);
			String siteId = toolManager.getCurrentPlacement().getContext();
			// page should always be in this site, or someone is gaming us
			if (!currentPage.getSiteId().equals(siteId))
			    throw new PermissionException(getCurrentUserId(), "set page", Long.toString(l));
			previousPageId = l;
			sessionManager.getCurrentToolSession().setAttribute("current-pagetool-page", l);
			currentPageId = (Long)l;
		}
	}

    // if tool was reset, return last page from previous session, so we can give the user
    // a chance to go back
	public SimplePageToolDao.PageData toolWasReset() {
	    if (sessionManager.getCurrentToolSession().getAttribute("current-pagetool-page") == null) {
		// no page in session, which means it was reset
		String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
		return simplePageToolDao.findMostRecentlyVisitedPage(toolId);
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

		// Let's go back to where we were last time.
		Long l = (Long) sessionManager.getCurrentToolSession().getAttribute("current-pagetool-page");
		if (l != null && l != 0) {
		    try {
			updatePageObject(l);
			Long i = (Long) sessionManager.getCurrentToolSession().getAttribute("current-pagetool-item");
			if (i != null && i != 0)
			    updatePageItem(i);
		    } catch (PermissionException e) {
			log.warn("getCurrentPageId Permission failed setting to item in toolsession");
			return 0;
		    }
		    return l;
		} else {
			// No recent activity. Let's go to the top level page.
			l = simplePageToolDao.getTopLevelPageId(((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId());

			if (l != null) {
			    try {
				updatePageObject(l);
				// this should exist except if the page was created by old code
				SimplePageItem i = simplePageToolDao.findTopLevelPageItemBySakaiId(String.valueOf(l));
				if (i == null) {
				    // and dummy item, the site is the notional top level page
				    i = new SimplePageItem(0, 0, SimplePageItem.PAGE, l.toString(), currentPage.getTitle());
				    simplePageToolDao.saveItem(i);
				}
				updatePageItem(i.getId());
			    } catch (PermissionException e) {
				log.warn("getCurrentPageId Permission failed setting to page in toolsession");
				return 0;
			    }
			    return l;
			} else {
				// No page found. Let's make a new one.
				String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
				String title = getCurrentSite().getPage(toolId).getTitle(); // Use title supplied
																			// during creation
				SimplePage page = new SimplePage(toolId, toolManager.getCurrentPlacement().getContext(), title, null, null);
				simplePageToolDao.saveItem(page);
				try {
				    updatePageObject(page.getPageId());
				    l = page.getPageId();

				    // and dummy item, the site is the notional top level page
				    SimplePageItem i = new SimplePageItem(0, 0, SimplePageItem.PAGE, l.toString(), title);
				    simplePageToolDao.saveItem(i);
				    updatePageItem(i.getId());
				} catch (PermissionException e) {
				    log.warn("getCurrentPageId Permission failed setting to new page");
				    return 0;
				}
				return l;
			}
		}
	}

        // current page must be set. 

	public SimplePageItem getCurrentPageItem(Long itemId)  {
		// if itemId is known, this is easy. but check to make sure it's
	        // actually this page, to prevent the user gaming us
		if (itemId == null || itemId == -1) 
		    itemId = currentPageItemId;
	        if (itemId != null && itemId != -1) {
		    SimplePageItem ret = findItem(itemId);
		    if (ret != null && ret.getSakaiId().equals(Long.toString(getCurrentPageId()))) {
			try {
			    updatePageItem(ret.getId());
			} catch (PermissionException e) {
			    log.warn("getCurrentPageItem Permission failed setting to specified item");
			    return null;
			}
			return ret;
		    } else 
			return null;
		}
		// else must be a top level item
		SimplePageItem ret = simplePageToolDao.findTopLevelPageItemBySakaiId(Long.toString(getCurrentPageId()));
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
	    } else if (op.startsWith("set:")) {
		// set complete path
		String items[] = op.substring(4).split(",");
		// verify that items are all part of this site, and build new path list
		path = new ArrayList<PathEntry>();
		for(String s: items) {
		    SimplePageItem i = findItem(Long.valueOf(s));
		    if (i == null || i.getType() != SimplePageItem.PAGE) {
			log.warn("attempt to set invalid path: invalid item: " + op);
			return null;
		    }
		    SimplePage p = simplePageToolDao.getPage(Long.valueOf(i.getSakaiId()));
		    if (p == null || !currentPage.getSiteId().equals(p.getSiteId())) {
			log.warn("attempt to set invalid path: invalid page: " + op);
			return null;
		    }
		    PathEntry entry = new PathEntry();
		    entry.pageId = p.getPageId();
		    entry.pageItemId = i.getId();
		    entry.title = i.getName();
		    path.add(entry);
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

	public void setSubpageTitle(String st) {
		subpageTitle = st;
	}

	public void setSubpageNext(boolean s) {
	    subpageNext = s;
	}

	public void setSubpageButton(boolean s) {
	    subpageButton = s;
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

		if ((title == null || title.length() == 0) &&
		    (selectedEntity == null || selectedEntity.length() == 0)) {
			return "notitle";
		}

		SimplePage page = getCurrentPage();

		Long parent = page.getPageId();
		Long topParent = page.getTopParent();

		if (topParent == null) {
			topParent = parent;
		}

		String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
		SimplePage subpage = null;
		if (makeNewPage) {
		    subpage = new SimplePage(toolId, toolManager.getCurrentPlacement().getContext(), title, parent, topParent);
		    simplePageToolDao.saveItem(subpage);
		    selectedEntity = String.valueOf(subpage.getPageId());
		} else {
		    subpage = simplePageToolDao.getPage(Long.valueOf(selectedEntity));
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

		simplePageToolDao.update(i);

		if (makeNewPage) {
		    // if creating new entry, go to it
		    try {
			updatePageObject(subpage.getPageId());
			updatePageItem(i.getId());
		    } catch (PermissionException e) {
			log.warn("createsubpage permission failed going to new page");
			return "failed";
		    }
		    adjustPath((subpageNext ? "next" : "push"), subpage.getPageId(), i.getId(), i.getName());

		    submit();

		}

		return "success";
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

			simplePageToolDao.update(i);

			if (i.getType() == SimplePageItem.PAGE) {
				SimplePage page = simplePageToolDao.getPage(Long.valueOf(i.getSakaiId()));
				if (page != null) {
					page.setTitle(name);
					simplePageToolDao.update(page);
				}
			} else {
				checkControlGroup(i);
			}

			return "successEdit"; // Shouldn't reload page
		}
	}

    // Set access control for an item to the state requested by i.isPrerequisite().
    // This code should depend only upon isPrerequisite() in the item object, not the database,
    // because we call it when deleting or updating items, before saving them to the database.
    // The caller will update the item in the database, typically after this call
	private void checkControlGroup(SimplePageItem i) {
	    	if (i.getType() != SimplePageItem.ASSESSMENT && 
		    i.getType() != SimplePageItem.ASSIGNMENT) {
			// We only do this for assignments and assessments
		        // currently we can't actually set it for forum topics
			return;
		}

		if (i.getSakaiId().equals(SimplePageItem.DUMMY))
		    return;

		SimplePageGroup group = simplePageToolDao.findGroup(i.getSakaiId());
		try {
			if (i.isPrerequisite()) {
				if (group == null) {
				        String groupId = GroupPermissionsService.makeGroup(getCurrentPage().getSiteId(), "Access: " + getNameOfSakaiItem(i));
					simplePageToolDao.saveItem(new SimplePageGroup(i.getSakaiId(), groupId));
					GroupPermissionsService.addControl(i.getSakaiId(), getCurrentPage().getSiteId(), groupId, i.getType());
				} else {
					GroupPermissionsService.addControl(i.getSakaiId(), getCurrentPage().getSiteId(), group.getGroupId(), i.getType());
				}
			} else {
				// if no group ID, nothing to do
				if (group != null)
					GroupPermissionsService.removeControl(i.getSakaiId(), getCurrentPage().getSiteId(), group.getGroupId(), i.getType());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public SimplePage getCurrentPage()  {
		getCurrentPageId();
		return currentPage;
	}

	public String getToolId(String tool) {
		try {
			ToolConfiguration tc = siteService.getSite(currentPage.getSiteId()).getToolForCommonId(tool);
			return tc.getId();
		} catch (IdUnusedException e) {
			// This really shouldn't happen.
		    log.warn("attempt to get tool config for " + tool + " failed. Tool missing from site?");
		    return null;
		} catch (java.lang.NullPointerException e) {
		    log.warn("attempt to get tool config for " + tool + " failed. Tool missing from site?");
		    return null;
		}
	}

	public void updateCurrentPage() {
		simplePageToolDao.update(currentPage);
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

        public String assignmentRef(String id) {
	    return "/assignment/a/" + toolManager.getCurrentPlacement().getContext() + "/" + id;
	}

	public int getAssignmentTypeOfGrade(String ref) {
	    Integer type = assignmentTypeCache.get(ref);
	    if (type != null)
		return (int)type;
	    Assignment assignment = getAssignment(ref);
	    // null should be impossible unless we have a bad reference in the database
	    if (assignment == null)
		return 1;
	    AssignmentContent content = assignment.getContent();
	    if (content == null) {
		type = 1;
	    } else 
		type = assignment.getContent().getTypeOfGrade();
	    assignmentTypeCache.put(ref, type);
	    return (int)type;
	}

	public Assignment getAssignment(String ref) {
	    Assignment ret = assignmentCache.get(ref);
	    if (ret != null)
		return ret;
	    try {
		ret = AssignmentService.getAssignment(ref);
	    } catch (Exception e) {
		ret = null;
	    }
	    if (ret != null)
		assignmentCache.put(ref, ret);
	    return ret;
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
					checkControlGroup(i);
					// sakaiid and name are used in setting control
					i.setSakaiId(selectedEntity);
					i.setName(selectedObject.getTitle());
					i.setPrerequisite(true);
						checkControlGroup(i);
				    } else {
					i.setSakaiId(selectedEntity);
					i.setName(selectedObject.getTitle());
				    }

				    // reset assignment-specific stuff
				    i.setDescription("");
				    simplePageToolDao.update(i);
				}
			    } else {
				// no, add new item
				i = appendItem(selectedEntity, selectedObject.getTitle(), SimplePageItem.FORUM);
				i.setDescription("");
				simplePageToolDao.update(i);
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
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";

		if (selectedAssignment == null) {
			return "failure";
		} else {
			try {
			        Assignment a = getAssignment(assignmentRef(selectedAssignment));
				SimplePageItem i;
				// editing existing item?
				if (itemId != null && itemId != -1) {
					i = findItem(itemId);
					// if no change, don't worry
					if (!i.getSakaiId().equals(selectedAssignment)) {
					    // if access controlled, clear restriction from old assignment and add to new
					    if (i.isPrerequisite()) {
						i.setPrerequisite(false);
						checkControlGroup(i);
						// sakaiid and name are used in setting control
						i.setSakaiId(selectedAssignment);
						i.setName(a.getTitle());
						i.setPrerequisite(true);
						checkControlGroup(i);
					    } else {
						i.setSakaiId(selectedAssignment);
						i.setName(a.getTitle());
					    }

					    // reset assignment-specific stuff
					    i.setDescription("(Due " + a.getDueTimeString() + ")");
					    simplePageToolDao.update(i);
					}
				} else {
				    // no, add new item
				    i = appendItem(selectedAssignment, a.getTitle(), SimplePageItem.ASSIGNMENT);
				    i.setDescription("(Due " + a.getDueTimeString() + ")");
				    simplePageToolDao.update(i);
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
				PublishedAssessmentData a = getPublishedAssessment(new Long(selectedQuiz));
				// editing existing item?
				if (itemId != null && itemId != -1) {
				    SimplePageItem i = findItem(itemId);
				    // if same quiz, nothing to do
				    if (i.getSakaiId().equals(selectedQuiz)) {
					// if access controlled, clear restriction from old quiz and add to new
					if (i.isPrerequisite()) {
					    i.setPrerequisite(false);
					    checkControlGroup(i);
					    // sakaiid and name are used in setting control
					    i.setSakaiId(selectedQuiz);
					    i.setName(a.getTitle());
					    i.setPrerequisite(true);
					    checkControlGroup(i);
					} else {
					    i.setSakaiId(selectedQuiz);
					    i.setName(a.getTitle());
					}
					// reset quiz-specific stuff
					i.setDescription("");
					simplePageToolDao.update(i);
				    }
				} else  // no, add new item
				    appendItem(selectedQuiz, a.getTitle(), SimplePageItem.ASSESSMENT);
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
			simplePageToolDao.update(i);
			return "success";
		} else {
			log.warn("Could not find multimedia object: " + itemId);
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

		if (pageTitle != null && pageItem.getPageId() == 0) {
			try {
				// we need a security advisor because we're allowing users to edit the page if they
				// have
				// simplepage.upd privileges, but site.save requires site.upd.
				securityService.pushAdvisor(new SecurityAdvisor() {
					public SecurityAdvice isAllowed(String userId, String function, String reference) {
						if (function.equals(SITE_UPD) && reference.equals("/site/" + toolManager.getCurrentPlacement().getContext())) {
							return SecurityAdvice.ALLOWED;
						} else {
							return SecurityAdvice.PASS;
						}
					}
				});

				if (true) {
					Site site = getCurrentSite();
					SitePage sitePage = site.getPage(page.getToolId());
					sitePage.setTitle(pageTitle);
					siteService.save(site);
					page.setTitle(pageTitle);
					page.setHidden(hidePage);
					if (hasReleaseDate)
					    page.setReleaseDate(releaseDate);
					else
					    page.setReleaseDate(null);
					simplePageToolDao.update(page);
					updateCurrentPage();
					placement.setTitle(pageTitle);
					placement.save();
					pageVisibilityHelper(site, page.getToolId(), !hidePage);
					pageItem.setPrerequisite(prerequisite);
					pageItem.setRequired(required);
					simplePageToolDao.update(pageItem);
				}

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				securityService.popAdvisor();
			}
		}

		if (pageTitle != null) {
			page.setTitle(pageTitle);
			simplePageToolDao.update(page);
		}

		if (pageItem.getPageId() == 0) {
			return "reload";
		} else {
			return "success";
		}
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

    // called by reorder tool to do the reordering
	public String reorder() {

	        if (!canEditPage())
		    return "permission-fail";

		if (order == null) {
			return "cancel";
		}
		order = order.trim();

		List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());

		String[] split = order.split(" ");

		// make sure nothing is duplicated. I know it shouldn't be, but
		// I saw the Fluid reorderer get confused once.
		Set<Integer> used = new HashSet<Integer>();
		for (int i = 0; i < split.length; i++) {
		    if (!used.add(Integer.valueOf(split[i]))) {
			log.warn("reorder: duplicate value");
			return "failed"; // it was already there. Oops.
		    }
		}

		// now do the reordering
		for (int i = 0; i < split.length; i++) {
			int old = items.get(Integer.valueOf(split[i]) - 1).getSequence();
			items.get(Integer.valueOf(split[i]) - 1).setSequence(i + 1);

			if (old != i + 1) {
				simplePageToolDao.update(items.get(Integer.valueOf(split[i]) - 1));
			}
		}

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
		currentUserId = sessionManager.getCurrentSessionUserId();
	    return currentUserId;
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
		String userId = getCurrentUserId();
		SimplePageLogEntry entry = getLogEntry(itemId);

		if (entry == null) {
			entry = new SimplePageLogEntry(userId, itemId);

			if (path != null) {
				boolean complete = isPageComplete(itemId);
				entry.setComplete(complete);
				entry.setPath(path);
				String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
				entry.setToolId(toolId);
				SimplePageItem i = findItem(itemId);
				EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.read", "/lessonbuilder/page/" + i.getSakaiId(), complete));
			}

			simplePageToolDao.saveItem(entry);
			logCache.put((Long)itemId, entry);
		} else {
			if (path != null) {
				boolean complete = isPageComplete(itemId);
				entry.setComplete(complete);
				entry.setPath(path);
				String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
				entry.setToolId(toolId);
				entry.setDummy(false);
				SimplePageItem i = findItem(itemId);
				EventTrackingService.post(EventTrackingService.newEvent("lessonbuilder.read", "/lessonbuilder/page/" + i.getSakaiId(), complete));
			}

			simplePageToolDao.update(entry);
		}

		SimplePageItem i = findItem(itemId);
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
		SimplePageLogEntry entry = logCache.get((Long)itemId);
		if (entry != null)
		    return entry;
		entry = simplePageToolDao.getLogEntry(getCurrentUserId(), itemId);
		logCache.put((Long)itemId, entry);
		return entry;
	}

	public boolean hasLogEntry(long itemId) {
		return (getLogEntry(itemId) != null);
	}

    // this is called in a loop to see whether items are avaiable. Since computing it can require
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
		if (item.getType() == SimplePageItem.RESOURCE || item.getType() == SimplePageItem.URL) {
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
				User user = UserDirectoryService.getUser(getCurrentUserId());
				Assignment assignment = getAssignment(assignmentRef(item.getSakaiId()));
				AssignmentSubmission submission = AssignmentService.getSubmission(assignment.getReference(), user);
				int type = getAssignmentTypeOfGrade(assignmentRef(item.getSakaiId()));

				if (submission == null) {
					completeCache.put(itemId, false);
					return false;
				} else if (!item.getSubrequirement()) {
					completeCache.put(itemId, true);
					return true;
				} else if (submission.getGradeReleased()) {
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
			PublishedAssessmentData assessment = null;

			if (item.getSakaiId().equals(SimplePageItem.DUMMY)) {
			    completeCache.put(itemId, false);
			    return false;
			}

			try {
			    assessment = getPublishedAssessment(new Long(item.getSakaiId()));
			} catch (RuntimeException ex) {
				// Most likely a group permissions problem.
				checkItemPermissions(item, isItemAvailable(item));
			}

			if (assessment == null) {
				completeCache.put(itemId, false);
				return false;
			}

			AssessmentGradingIfc grading = null;
			if (assessment.getEvaluationModel().getScoringType() == EvaluationModelIfc.LAST_SCORE) {
			    grading = gradingService.getLastSubmittedAssessmentGradingByAgentId(item.getSakaiId(), getCurrentUserId(), null);
			} else {
			    grading = gradingService.getHighestSubmittedAssessmentGrading(item.getSakaiId(), getCurrentUserId());
			}

			if (grading == null) {
				completeCache.put(itemId, false);
				return false;
			} else if (!item.getSubrequirement()) {
				// All that was required was that the user submit the test
				completeCache.put(itemId, true);
				return true;
			} else {
				float grade = grading.getFinalScore();
				if (grade >= Float.valueOf(item.getRequirementText())) {
					completeCache.put(itemId, true);
					return true;
				} else {
					completeCache.put(itemId, false);
					return false;
				}
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

	private boolean isAssignmentComplete(int type, AssignmentSubmission submission, String requirementString) {
		String grade = submission.getGrade();

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
			if (Float.valueOf(Integer.valueOf(grade) / 10) >= Float.valueOf(requirementString)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * @param itemId
	 *            The ID of the page from the <b>items</b> table (not the page table).
	 * @return
	 */
	public boolean isPageComplete(long itemId) {
	    
		List<SimplePageItem> items = getItemsOnPage(Long.valueOf(findItem(itemId).getSakaiId()));

		for (SimplePageItem item : items) {
			if (!isItemComplete(item)) {
				return false;
			}
		}

		// All of them were complete.
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

	    // user can only get an error here if they're gaming us. The
	    // system will never send the user to a page they aren't allowed to see
	    if (item.getPageId() > 0) {
		if (!hasLogEntry(item.getId())) { // including dummy entry
		    SimplePage prereqPage = simplePageToolDao.getPage(item.getPageId());
		    if (prereqPage != null)
			needed.add(prereqPage.getTitle());
		    else
			needed.add("Page not available");
		}
		return needed;
	    }

	    // we've got a top level page.
	    // get dummy items for top level pages in site
            List<SimplePageItem> items = 
		simplePageToolDao.findItemsInSite(getCurrentSite().getId());
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

	public boolean isItemAvailable(SimplePageItem item) {
	    return isItemAvailable(item, getCurrentPageId());
	}

	public boolean isItemAvailable(SimplePageItem item, long pageId) {
		if (item.isPrerequisite()) {
			List<SimplePageItem> items = getItemsOnPage(pageId);

			for (SimplePageItem i : items) {
				if (i.getSequence() >= item.getSequence()) {
				    break;
				} else if (i.isRequired()) {
				    if (!isItemComplete(i))
					return false;
				}
			}
		}
		return true;
	}

        public String getNameOfSakaiItem(SimplePageItem i) {
	        String SakaiId = i.getSakaiId();

		if (SakaiId == null || SakaiId.equals(SimplePageItem.DUMMY))
		    return null;

		if (i.getType() == SimplePageItem.ASSIGNMENT) {
		    try {
			Assignment a = getAssignment(assignmentRef(i.getSakaiId()));
			return a.getTitle();
		    } catch (Exception e) {
			return null;
		    }
	        } else if (i.getType() == SimplePageItem.FORUM) {
		    LessonEntity forum = forumEntity.getEntity(i.getSakaiId());
		    if (forum == null)
			return null;
		    return forum.getTitle();
		} else if (i.getType() == SimplePageItem.ASSESSMENT) {
		    PublishedAssessmentData a = getPublishedAssessment(new Long(i.getSakaiId()));
		    return a.getTitle();
		} else
		    return null;
	}
		

        public PublishedAssessmentData getPublishedAssessment(Long publishedId) {
	    PublishedAssessmentData ret = quizCache.get(publishedId);
	    if (ret != null)
		return ret;
	    ret = pService.loadPublishedAssessment(publishedId);
	    if (ret != null)
		quizCache.put(publishedId, ret);
	    return ret;
	}

	public String getAssessmentAlias(String publishedId) {
		try {
		    return getPublishedAssessment(new Long(publishedId)).getAssessmentMetaDataByLabel("ALIAS");
		} catch (Exception ex) {
			return null;
		}
	}

    /* 
     * return 11-char youtube ID for a URL, or null if it doesn't match
     * we store URLs as content objects, so we have to retrieve the object
     * in order to check. The actual URL is stored as the contents
     * of the entity
     */

	public String getYoutubeKey(SimplePageItem i) {
		String sakaiId = i.getSakaiId();

		// find the resource
		ContentResource resource = null;
		try {
		    resource = contentHostingService.getResource(sakaiId);
		} catch (Exception ignore) {
		    return null;
		}

		// make sure it's a URL
		if (resource == null ||
		    !resource.getResourceType().equals("org.sakaiproject.content.types.urlResource") ||
		    !resource.getContentType().equals("text/url")) {
		    return null;
		}

		// get the actual URL
		String URL = null;
		try {
		    URL = new String(resource.getContent());
		} catch (Exception ignore) {
		    return null;
		}
		if (URL == null) {
		    return null;
		}

		// see if it has a Youtube ID
		if (URL.startsWith("http://www.youtube.com/") || URL.startsWith("http://youtube.com/")) {
			Matcher match = YOUTUBE_PATTERN.matcher(URL);
			if (match.find()) {
				return match.group().substring(2);
			}
   		}
		
		// no
		return null;
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
			SimplePageLogEntry entry = new SimplePageLogEntry(getCurrentUserId(), itemId);
			entry.setDummy(true);
			simplePageToolDao.saveItem(entry);
			logCache.put((Long)itemId, entry);
		    }
		    return;
		}

		SimplePageGroup group = simplePageToolDao.findGroup(item.getSakaiId());
		if (group == null) {
			// For some reason, the group doesn't exist. Let's re-add it.
			String groupId;
			try {
				groupId = GroupPermissionsService.makeGroup(getCurrentPage().getSiteId(), "Access: " + getNameOfSakaiItem(item));
				simplePageToolDao.saveItem(new SimplePageGroup(item.getSakaiId(), groupId));
				GroupPermissionsService.addControl(item.getSakaiId(), getCurrentPage().getSiteId(), groupId, item.getType());
			} catch (IOException e) {
				// If this fails, there's no way for us to check the permissions
				// in the group. This shouldn't happen.

				e.printStackTrace();
				return;
			}

			group = simplePageToolDao.findGroup(item.getSakaiId());
			if (group == null) {
				// Something really weird is up.
				log.warn("Can't create a group for " + item.getName() + " permissions.");
				return;
			}
		}

		boolean success = true;
		String groupId = group.getGroupId();

		try {
			if (shouldHaveAccess) {
			    success = GroupPermissionsService.addCurrentUser(getCurrentPage().getSiteId(), getCurrentUserId(), groupId);
			} else {
			    success = GroupPermissionsService.removeUser(getCurrentPage().getSiteId(), groupId);
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
			    log.warn("unable to join or unjoin group " + groupId);
			}

			log.warn("Lesson Builder: User seems to have deleted group " + groupId + ". We'll recreate it.");

			// OK, group doesn't exist. When we recreate it, it's going to have a 
			// different groupId, so we have to back out of everything and reset it

			try {
				GroupPermissionsService.removeControl(item.getSakaiId(), getCurrentPage().getSiteId(), groupId, item.getType());
			} catch (IOException e) {
				// Shoudln't happen, but we'll continue anyway
				e.printStackTrace();
			}
			simplePageToolDao.deleteItem(group);

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
		simplePageToolDao.deleteItem(findItem(youtubeId));
	}

	public void setMmUrl(String url) {
		mmUrl = url;
	}

        public void setMultipartMap(Map<String, MultipartFile> multipartMap) {
	    this.multipartMap = multipartMap;
	}

	public String getCollectionId(boolean urls) {
	    String siteId = getCurrentPage().getSiteId();
	    String collectionId = contentHostingService.getSiteCollection(siteId);

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
		};

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

        public static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

    // called by dialog to add inline multimedia item, or update existing
    // item if itemid is specified
	public void addMultimedia() {

	    if (!itemOk(itemId))
		return;
	    if (!canEditPage())
		return;

	    String name = null;
	    String sakaiId = null;
	    String mimeType = null;
	    MultipartFile file = null;

	    if (multipartMap.size() > 0) {
		// user specified a file, create it
		file = multipartMap.values().iterator().next();
		if (file.isEmpty())
		    file = null;
	    }

	    if (file != null) {
		String collectionId = getCollectionId(false);
		// user specified a file, create it
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
		    contentHostingService.commitResource(res,  NotificationService.NOTI_NONE);
		    sakaiId = res.getId();

		} catch (Exception e) {log.error("error " + e);};
	    } else if (mmUrl != null && !mmUrl.trim().equals("")) {
		// user specified a URL, create the item
		String url = mmUrl.trim();
		if (!url.startsWith("http:") &&
		    !url.startsWith("https:")) {
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

		String collectionId = getCollectionId(true);
		try {
		    // urls aren't something people normally think of as resources. Let's hide them
		    ContentResourceEdit res = contentHostingService.addResource(collectionId, 
						  Validator.escapeResourceName(base),
						  Validator.escapeResourceName(extension),
						  MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
		    res.setContentType("text/url");
		    res.setResourceType("org.sakaiproject.content.types.urlResource");
		    res.setContent(url.getBytes());
		    contentHostingService.commitResource(res, NotificationService.NOTI_NONE);
		    sakaiId = res.getId();
		} catch (Exception e) {log.error("error " + e);};
		
		// connect to url and get mime type
		mimeType = getTypeOfUrl(url);

	    } else
		// nothing to do
		return;
		
	    SimplePageItem item = null;
	    if (itemId == -1 && isMultimedia) {
		int seq = getItemsOnPage(getCurrentPageId()).size() + 1;
		item = new SimplePageItem(getCurrentPageId(), seq, SimplePageItem.MULTIMEDIA, sakaiId, name);
	    } else if (itemId == -1) {
		int seq = getItemsOnPage(getCurrentPageId()).size() + 1;
		item = new SimplePageItem(getCurrentPageId(), seq, SimplePageItem.RESOURCE, sakaiId, name);

	    } else {
		item = findItem(itemId);
		if (item == null)
		    return;
		item.setSakaiId(sakaiId);
		item.setName(name);
	    }

	    if (item.getType() == SimplePageItem.MULTIMEDIA && mimeType != null) {
		item.setHtml(mimeType);
	    } else {
		item.setHtml(null);
	    }
	    clearImageSize(item);
	    if (itemId == -1)
		simplePageToolDao.saveItem(item);
	    else
		simplePageToolDao.update(item);

	}


    // called by edit dialog to update parameters of a Youtube item
	public void updateYoutube() {
		if (!itemOk(youtubeId))
		    return;
		if (!canEditPage())
		    return;

		SimplePageItem item = findItem(youtubeId);

		// find the new key, if the new thing is a legit youtube url
		Matcher match = YOUTUBE_PATTERN.matcher(youtubeURL);
		String key = null;
		if (match.find()) {
		    key = match.group().substring(2);
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

		    try {
			ContentResourceEdit res = contentHostingService.addResource(collectionId,Validator.escapeResourceName("Youtube video " + key),Validator.escapeResourceName("swf"),MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
			res.setContentType("text/url");
			res.setResourceType("org.sakaiproject.content.types.urlResource");
			res.setContent(url.getBytes());
			contentHostingService.commitResource(res, NotificationService.NOTI_NONE);
			item.setSakaiId(res.getId());
		    } catch (Exception e) {log.error("error " + e);};
		}

		// even if there's some oddity with URLs, we do these updates
		item.setHeight(height);
		item.setWidth(width);
		item.setDescription(description);
		simplePageToolDao.update(item);

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
			} else if (visible) {
				roleList = roleList.replaceAll("," + SITE_UPD, "");
				roleList = roleList.replaceAll(SITE_UPD, "");
				saveChanges = true;
			}

			if (saveChanges) {
				roleConfig.setProperty("functions.require", roleList);

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
		simplePageToolDao.update(item);
	}
}