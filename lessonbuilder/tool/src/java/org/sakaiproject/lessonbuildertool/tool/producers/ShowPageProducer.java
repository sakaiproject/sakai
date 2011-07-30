/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************
 *
 * This was was originally part of Simple Page Tool and was
 *
 * Copyright (c) 2007 Sakai Project/Sakai Foundation
 * Licensed under the Educational Community License version 1.0
 *
 * The author was Joshua Ryan josh@asu.edu
 *
 * However this version is primarily new code. The new code is
 *
 * Copyright (c) 2010 Rutgers, the State University of New Jersey
 *
 * Author: Eric Jeney, jeney@rutgers.edu
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

package org.sakaiproject.lessonbuildertool.tool.producers;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.service.BltiInterface;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.GroupEntry;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.evolvers.SakaiFCKTextEvolver;
import org.sakaiproject.lessonbuildertool.tool.view.CommentsViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ResourceLoader;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UISelectChoice;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * This produces the primary view of the page. It also handles the editing of
 * the properties of most of the items (through JQuery dialogs).
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 */
public class ShowPageProducer implements ViewComponentProducer, DefaultView, NavigationCaseReporter, ViewParamsReporter {
	private static Log log = LogFactory.getLog(ShowPageProducer.class);

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private FormatAwareDateInputEvolver dateevolver;
	private TimeService timeService;
	private HttpServletRequest httpServletRequest;
	private MemoryService memoryService = null;
	private ToolManager toolManager;
	public TextInputEvolver richTextEvolver;

	// I don't much like the static, because it opens us to a possible race
	// condition, but I don't see much option
	// see the setter. It has to be static because it's used in makeLink, which
	// is static so it can be used
	// by ReorderProducer. I wonder if this whole producer could be made
	// application scope?
	private static LessonEntity forumEntity;
	private static LessonEntity quizEntity;
	private static LessonEntity assignmentEntity;
        private static LessonEntity bltiEntity;
	public MessageLocator messageLocator;
	private LocaleGetter localegetter;
	public static final String VIEW_ID = "ShowPage";
	private static final String DEFAULT_TYPES = "mp4,mov,m2v,3gp,wmv,mp3,swf,wav";
	private static String[] multimediaTypes = null;

	private static List urlCacheLock = new ArrayList();
	private static Cache urlCache = null;

	protected static final int DEFAULT_EXPIRATION = 10 * 60;

	public String getViewID() {
		return VIEW_ID;
	}

	// this code is written to handle the fact the CSS uses NNNpx and old code
	// NNN. We need to be able to convert.
	// Length is intended to be a neutral representation. getOld returns without
	// px, getNew with px, and getOrig
	// the original version
	public class Length {
		String number;
		String unit;

		Length(String spec) {
			spec = spec.trim();
			int numlen;
			for (numlen = 0; numlen < spec.length(); numlen++) {
				if (!Character.isDigit(spec.charAt(numlen))) {
					break;
				}
			}
			number = spec.substring(0, numlen).trim();
			unit = spec.substring(numlen).trim().toLowerCase();
		}

		public String getOld() {
			return number + (unit.equals("px") ? "" : unit);
		}

		public String getNew() {
			return number + (unit.equals("") ? "px" : unit);
		}
	}

	// problem is it needs to work with a null argument
	public static String getOrig(Length l) {
		if (lengthOk(l))
			return l.number + l.unit;
		else
			return "";
	}

	// do we have a valid length?
	public static boolean lengthOk(Length l) {
		if (l == null || l.number == null || l.number.equals("")) {
			if (l != null && l.unit.equals("auto"))
				return true;
			return false;
		}
		return true;
	}

	// created style arguments. This was done at the time when i thought
	// the OBJECT tag actually paid attention to the CSS size. it doesn't.
	public String getStyle(Length w, Length h) {
		return "width: " + w.getNew() + ", height: " + h.getNew();
	}

	// produce abbreviated versions of URLs, for use in constructing titles
	public String abbrevUrl(String url) {
		if (url.startsWith("/")) {
			int suffix = url.lastIndexOf("/");
			if (suffix > 0) {
				url = url.substring(suffix + 1);
			}
			if (url.startsWith("http:__")) {
				url = url.substring(7);
				suffix = url.indexOf("_");
				if (suffix > 0) {
					url = messageLocator.getMessage("simplepage.fromhost").replace("{}", url.substring(0, suffix));
				}
			} else if (url.startsWith("https:__")) {
				url = url.substring(8);
				suffix = url.indexOf("_");
				if (suffix > 0) {
					url = messageLocator.getMessage("simplepage.fromhost").replace("{}", url.substring(0, suffix));
				}
			}
		} else {
			// external, the hostname is probably best
			try {
				URL u = new URL(url);
				url = messageLocator.getMessage("simplepage.fromhost").replace("{}", u.getHost());
			} catch (Exception ignore) {
				log.error("exception " + ignore);
			}
			;
		}

		return url;
	}

	public String myUrl() {
		String url = httpServletRequest.getScheme() + "://" + httpServletRequest.getServerName();
		int port = httpServletRequest.getServerPort();
		if (!((url.startsWith("http:") && port == 80) || (url.startsWith("https:") && port == 443))) {
			url = url + ":" + Integer.toString(port);
		}
		return url;
	}

	// NOTE:
	// pages should normally be called with 3 arguments:
	// sendingPageId - the page to show
	// itemId - the item used to choose the page, because pages can occur in
	// different places, and we need
	// to know the context in which this was called. Note that there's an item
	// even for top-level pages
	// path - push, next, or a number. The number is an index into the
	// breadcrumbs if someone clicks
	// on breadcrumbs. This item is used to maintain the path (the internal
	// form of the breadcrumbs)
	// missing is treated as next.
	// for startup, none of this will be known, so getCurrentPage will find the
	// top level page and item if
	// nothing is specified

	public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {
		GeneralViewParameters params = (GeneralViewParameters) viewParams;
		
		// security model:
		// canEditPage and canReadPage are normal Sakai privileges. They apply
		// to all
		// pages in the site.
		// However when presented with a page, we need to make sure it's
		// actually in
		// this site, or users could get to pages in other sites. That's done
		// by updatePageObject. The model is that producers always work on the
		// current page, and updatePageObject makes sure that is in the current
		// site.
		// At that point we can safely use canEditPage.

		// somewhat misleading. sendingPage specifies the page we're supposed to
		// go to
		if (params.getSendingPage() != -1) {
			// will fail if page not in this site
			// security then depends upon making sure that we only deal with
			// this page
			try {
				simplePageBean.updatePageObject(params.getSendingPage());
			} catch (Exception e) {
				log.warn("ShowPage permission exception " + e);
				UIOutput.make(tofill, "error-div");
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
				return;
			}
		}
		
		boolean canEditPage = simplePageBean.canEditPage();
		boolean canReadPage = simplePageBean.canReadPage();

		if (!canReadPage) {
			// this code is intended for the situation where site permissions
			// haven't been set up.
			// So if the user can't read the page (which is pretty abnormal),
			// see if they have site.upd.
			// if so, give them some explanation and offer to call the
			// permissions helper
			String ref = "/site/" + simplePageBean.getCurrentSiteId();
			if (simplePageBean.canEditSite()) {
				SimplePage currentPage = simplePageBean.getCurrentPage();
				UIOutput.make(tofill, "needPermissions");

				GeneralViewParameters permParams = new GeneralViewParameters();
				permParams.setSendingPage(-1L);
				createStandardToolBarLink(PermissionsHelperProducer.VIEW_ID, tofill, "callpermissions", "simplepage.permissions", permParams, "simplepage.permissions.tooltip");

			}

			// in any case, tell them they can't read the page
			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.nopermissions"));
			return;
		}

		if (params.addTool == GeneralViewParameters.COMMENTS) {
			simplePageBean.addCommentsSection();
		}else if(params.addTool == GeneralViewParameters.STUDENT_CONTENT) {
			simplePageBean.addStudentContentSection();
		}else if(params.addTool == GeneralViewParameters.STUDENT_PAGE) {
			simplePageBean.createStudentPage(params.studentItemId);
			canEditPage = simplePageBean.canEditPage();
		}

		// error from previous operation
		String errMessage = simplePageBean.errMessage();
		if (errMessage != null) {
			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", errMessage);
		}

		// Find the MSIE version, if we're running it.
		int ieVersion = checkIEVersion();

		if (simplePageBean.getTopRefresh()) {
			UIOutput.make(tofill, "refresh");
		}

		// set up locale
		Locale M_locale = null;
		String langLoc[] = localegetter.get().toString().split("_");
		if (langLoc.length >= 2) {
			if ("en".equals(langLoc[0]) && "ZA".equals(langLoc[1])) {
				M_locale = new Locale("en", "GB");
			} else {
				M_locale = new Locale(langLoc[0], langLoc[1]);
			}
		} else {
			M_locale = new Locale(langLoc[0]);
		}

		// clear session attribute if necessary, after calling Samigo
		String clearAttr = params.getClearAttr();

		if (clearAttr != null && !clearAttr.equals("")) {
			Session session = SessionManager.getCurrentSession();
			// don't let users clear random attributes
			if (clearAttr.startsWith("LESSONBUILDER_RETURNURL")) {
				session.setAttribute(clearAttr, null);
			}
		}

		if (multimediaTypes == null) {
			String mmTypes = ServerConfigurationService.getString("lessonbuilder.multimedia.types", DEFAULT_TYPES);
			multimediaTypes = mmTypes.split(",");
			for (int i = 0; i < multimediaTypes.length; i++) {
				multimediaTypes[i] = multimediaTypes[i].trim().toLowerCase();
			}
			Arrays.sort(multimediaTypes);
		}

		// remember that page tool was reset, so we need to give user the option
		// of going to the last page from the previous session
		SimplePageToolDao.PageData lastPage = simplePageBean.toolWasReset();

		// if starting the tool, sendingpage isn't set. the following call
		// will give us the top page.
		SimplePage currentPage = simplePageBean.getCurrentPage();
		
		// now we need to find our own item, for access checks, etc.
		SimplePageItem pageItem = null;
		if (currentPage != null) {
			pageItem = simplePageBean.getCurrentPageItem(params.getItemId());
		}
		// one more security check: make sure the item actually involves this
		// page.
		// otherwise someone could pass us an item from a different page in
		// another site
		// actually this normally happens if the page doesn't exist and we don't
		// have permission to create it
		if (currentPage == null || pageItem == null || (pageItem.getType() != SimplePageItem.STUDENT_CONTENT &&Long.valueOf(pageItem.getSakaiId()) != currentPage.getPageId())) {
			log.warn("ShowPage item not in page");
			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
			return;
		}

		// I believe we've now checked all the args for permissions issues. All
		// other item and
		// page references are generated here based on the contents of the page
		// and items.

		if (currentPage == null || pageItem == null) {
			UIOutput.make(tofill, "error-div");
			if (canEditPage) {
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.impossible1"));
			} else {
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
			}
			return;
		}

		// offer to go to saved page if this is the start of a session, in case
		// user has logged off and logged on again.
		// need to offer to go to previous page? even if a new session, no need
		// if we're already on that page
		if (lastPage != null && lastPage.pageId != currentPage.getPageId()) {
			UIOutput.make(tofill, "refreshAlert");
			UIOutput.make(tofill, "refresh-message", messageLocator.getMessage("simplepage.last-visited"));
			// Should simply refresh
			GeneralViewParameters p = new GeneralViewParameters(VIEW_ID);
			p.setSendingPage(lastPage.pageId);
			p.setItemId(lastPage.itemId);
			// reset the path to the saved one
			p.setPath("log");
			
			String name = lastPage.name;
			
			// Titles are set oddly by Student Content Pages
			SimplePage lastPageObj = simplePageToolDao.getPage(lastPage.pageId);
			if(lastPageObj.getOwner() != null) {
				name = lastPageObj.getTitle();
			}
			
			UIInternalLink.make(tofill, "refresh-link", name, p);
		}

		// path is the breadcrumbs. Push, pop or reset depending upon path=
		// programmer documentation.
		String title;
		if(pageItem.getType() != SimplePageItem.STUDENT_CONTENT) {
			title = pageItem.getName();
		}else {
			title = currentPage.getTitle();
		}
		String newPath = simplePageBean.adjustPath(params.getPath(), currentPage.getPageId(), pageItem.getId(), title);
		simplePageBean.adjustBackPath(params.getBackPath(), currentPage.getPageId(), pageItem.getId(), pageItem.getName());

		// potentially need time zone for setting release date
		if (!canEditPage && currentPage.getReleaseDate() != null && currentPage.getReleaseDate().after(new Date())) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, M_locale);
			TimeZone tz = timeService.getLocalTimeZone();
			df.setTimeZone(tz);
			String releaseDate = df.format(currentPage.getReleaseDate());
			String releaseMessage = messageLocator.getMessage("simplepage.not_yet_available_releasedate").replace("{}", releaseDate);

			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", releaseMessage);

			return;
		}

		// put out link to index of pages
		GeneralViewParameters showAll = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
		showAll.setSource("summary");
		UIInternalLink.make(tofill, "show-pages", messageLocator.getMessage("simplepage.showallpages"), showAll);

		if (canEditPage) {
			// show tool bar
			createToolBar(tofill, currentPage);
			UIOutput.make(tofill, "edit-title").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editTitle-tooltip")));

			if (pageItem.getPageId() == 0) { // top level page
				UIOutput.make(tofill, "new-page").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-page-tooltip")));
				UIOutput.make(tofill, "import-cc").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.import_cc")));
			}
			
			// Checks to see that user can edit and that this is either a top level page,
			// or a top level student page (not a subpage to a student page)
			if(simplePageBean.getEditPrivs() == 0 && (pageItem.getPageId() == 0)) {
				UIOutput.make(tofill, "remove-page").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.remove-page-tooltip")));
			}else if(simplePageBean.getEditPrivs() == 0 && currentPage.getOwner() != null) {
				SimpleStudentPage studentPage = simplePageToolDao.findStudentPage(currentPage.getTopParent());
				if(studentPage != null && studentPage.getPageId() == currentPage.getPageId()) {
					UIOutput.make(tofill, "remove-page").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.remove-page-tooltip")));
				}
			}

			UIOutput.make(tofill, "dialogDiv");
		} else if (!canReadPage)
			return;
		else {
			// see if there are any unsatisfied prerequisites
			List<String> needed = simplePageBean.pagesNeeded(pageItem);
			if (needed.size() > 0) {
				// yes. error and abort
				if (pageItem.getPageId() != 0) {
					// not top level. This should only happen from a "next"
					// link.
					// at any rate, the best approach is to send the user back
					// to the calling page
					List<SimplePageBean.PathEntry> path = simplePageBean.getHierarchy();
					SimplePageBean.PathEntry containingPage = null;
					if (path.size() > 1) {
						// page above this. this page is on the top
						containingPage = path.get(path.size() - 2);
					}

					if (containingPage != null) { // not a top level page, point
						// to containing page
						GeneralViewParameters view = new GeneralViewParameters(VIEW_ID);
						view.setSendingPage(containingPage.pageId);
						view.setItemId(containingPage.pageItemId);
						view.setPath(Integer.toString(path.size() - 2));
						UIInternalLink.make(tofill, "redirect-link", containingPage.title, view);
						UIOutput.make(tofill, "redirect");
					}

					return;
				}

				// top level page where prereqs not satisified. Output list of
				// pages he needs to do first
				UIOutput.make(tofill, "pagetitle", currentPage.getTitle());
				UIOutput.make(tofill, "error-div");
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.has_prerequistes"));
				UIBranchContainer errorList = UIBranchContainer.make(tofill, "error-list:");
				for (String errorItem : needed) {
					UIBranchContainer errorListItem = UIBranchContainer.make(errorList, "error-item:");
					UIOutput.make(errorListItem, "error-item-text", errorItem);
				}
				return;
			}
		}

		// note page accessed. the code checks to see whether all the required
		// items on it have been finished, and if so marks it complete, else just updates
		// access date save the path because if user goes to it later we want to restore the
		// breadcrumbs
		if(pageItem.getType() != SimplePageItem.STUDENT_CONTENT) {
			simplePageBean.track(pageItem.getId(), newPath);
		}else {
			simplePageBean.track(pageItem.getId(), newPath, currentPage.getPageId());
		}

		UIOutput.make(tofill, "pagetitle", currentPage.getTitle());

		// breadcrumbs
		if (pageItem.getPageId() != 0) {
			// Not top-level, so we have to show breadcrumbs

			List<SimplePageBean.PathEntry> breadcrumbs = simplePageBean.getHierarchy();

			int index = 0;
			if (breadcrumbs.size() > 1) {
				UIOutput.make(tofill, "crumbdiv");
				for (SimplePageBean.PathEntry e : breadcrumbs) {
					// don't show current page. We already have a title. This
					// was too much
					UIBranchContainer crumb = UIBranchContainer.make(tofill, "crumb:");
					GeneralViewParameters view = new GeneralViewParameters(VIEW_ID);
					view.setSendingPage(e.pageId);
					view.setItemId(e.pageItemId);
					view.setPath(Integer.toString(index));
					if (index < breadcrumbs.size() - 1) {
						// Not the last item
						UIInternalLink.make(crumb, "crumb-link", e.title, view);
						UIOutput.make(crumb, "crumb-follow", " > ");
					} else {
						UIOutput.make(crumb, "crumb-follow", e.title).decorate(new UIStyleDecorator("bold"));
					}

					index++;
				}
			}
		}

		// see if there's a next item in sequence.
		simplePageBean.addPrevLink(tofill, pageItem);
		simplePageBean.addNextLink(tofill, pageItem);

		// swfObject is not currently used
		boolean shownSwfObject = false;

		// items to show
		List<SimplePageItem> itemList = (List<SimplePageItem>) simplePageBean.getItemsOnPage(currentPage.getPageId());
		
		// Move all items with sequence <= 0 to the end of the list.
		// Count is necessary to guarantee we don't infinite loop over a
		// list that only has items with sequence <= 0.
		int count = 1;
		while(itemList.size() > count && itemList.get(0).getSequence() <= 0) {
			itemList.add(itemList.remove(0));
			count++;
		}

		// Make sure we only add the comments javascript file once,
		// even if there are multiple comments tools on the page.
		boolean addedCommentsScript = false;
		int commentsCount = 0;

		// Find the most recent comment on the page by current user
		long postedCommentId = -1;
		if (params.postedComment) {
			postedCommentId = findMostRecentComment();
		}

		//
		//
		// MAIN list of items
		//
		// produce the main table
		if (itemList.size() > 0) {
			UIBranchContainer container = UIBranchContainer.make(tofill, "itemContainer:");

			boolean showRefresh = false;
			int textboxcount = 1;

			UIBranchContainer tableContainer = UIBranchContainer.make(container, "itemTable:");

			// formatting: two columns:
			// 1: edit buttons, omitted for student
			// 2: main content
			// For links, which have status icons, the main content is a flush
			// left div with the icon
			// followed by a div with margin-left:30px. That takes it beyond the
			// icon, and avoids the
			// wrap-around appearance you'd get without the margin.
			// Normally the description is shown as a second div with
			// indentation in the CSS.
			// That puts it below the link. However with a link that's a button,
			// we do float left
			// for the button so the text wraps around it. I think that's
			// probably what people would expect.

			UIOutput.make(tableContainer, "colgroup");
			if (canEditPage) {
				UIOutput.make(tableContainer, "col1");
			}
			UIOutput.make(tableContainer, "col2");

			// the table header is for accessibility tools only, so it's
			// positioned off screen
			if (canEditPage) {
				UIOutput.make(tableContainer, "header-edits");
			}

			UIOutput.make(tableContainer, "header-items");

			for (SimplePageItem i : itemList) {

				// listitem is mostly historical. it uses some shared HTML, but
				// if I were
				// doing it from scratch I wouldn't make this distinction. At
				// the moment it's
				// everything that isn't inline.

				boolean listItem = !(i.getType() == SimplePageItem.TEXT || i.getType() == SimplePageItem.MULTIMEDIA
						|| i.getType() == SimplePageItem.COMMENTS || i.getType() == SimplePageItem.STUDENT_CONTENT);
				// (i.getType() == SimplePageItem.PAGE &&
				// "button".equals(i.getFormat())))

				UIBranchContainer tableRow = UIBranchContainer.make(tableContainer, "item:");
				if (!simplePageBean.isItemVisible(i)) {
					continue;
				}

				// you really need the HTML file open at the same time to make
				// sense of the following code
				if (listItem) { // Not an HTML Text, Element or Multimedia
					// Element

					if (canEditPage) {
						UIOutput.make(tableRow, "current-item-id2", String.valueOf(i.getId()));
					}

					// users can declare a page item to be navigational. If so
					// we display
					// it to the left of the normal list items, and use a
					// button. This is
					// used for pages that are "next" pages, i.e. they replace
					// this page
					// rather than creating a new level in the breadcrumbs.
					// Since they can't
					// be required, they don't need the status image, which is
					// good because
					// they're displayed with colspan=2, so there's no space for
					// the image.

					boolean navButton = "button".equals(i.getFormat()) && !i.isRequired();
					boolean notDone = false;
					Status status = Status.NOT_REQUIRED;
					if (!navButton) {
						status = handleStatusImage(tableRow, i);
						if (status == Status.REQUIRED) {
							notDone = true;
						}
					}

					boolean isInline = (i.getType() == SimplePageItem.BLTI && "inline".equals(i.getFormat()));

					UIOutput linktd = UIOutput.make(tableRow, "item-td");
					UIOutput linkdiv = null;
					if (!isInline)
					    linkdiv = UIOutput.make(tableRow, "link-div");

					UIOutput descriptiondiv = null;

					// refresh isn't actually used anymore. We've changed the
					// way things are
					// done so the user never has to request a refresh.
					//   FYI: this actually puts in an IFRAME for inline BLTI items
					showRefresh = !makeLink(tableRow, "link", i, canEditPage, currentPage, notDone, status) || showRefresh;

					// dummy is used when an assignment, quiz, or forum item is
					// copied
					// from another site. The way the copy code works, our
					// import code
					// doesn't have access to the necessary info to use the item
					// from the
					// new site. So we add a dummy, which generates an
					// explanation that the
					// author is going to have to choose the item from the
					// current site
					if (i.getSakaiId().equals(SimplePageItem.DUMMY)) {
						String code = null;
						switch (i.getType()) {
						case SimplePageItem.ASSIGNMENT:
							code = "simplepage.copied.assignment";
							break;
						case SimplePageItem.ASSESSMENT:
							code = "simplepage.copied.assessment";
							break;
						case SimplePageItem.FORUM:
							code = "simplepage.copied.forum";
							break;
						}
						descriptiondiv = UIOutput.make(tableRow, "description", messageLocator.getMessage(code));
					} else {
						descriptiondiv = UIOutput.make(tableRow, "description", i.getDescription());
					}
					if (isInline)
					    descriptiondiv.decorate(new UIFreeAttributeDecorator("style", "margin-top: 4px"));

					if (!isInline) {
					    // nav button gets float left so any description goes to its
					    // right. Otherwise the
					    // description block will display underneath
					    if ("button".equals(i.getFormat())) {
						linkdiv.decorate(new UIFreeAttributeDecorator("style", "float:left"));
					    }
					    // for accessibility
					    if (navButton) {
						linkdiv.decorate(new UIFreeAttributeDecorator("role", "navigation"));
					    }
					}

					// note that a lot of the info here is used by the
					// javascript that prepares
					// the jQuery dialogs
					if (canEditPage) {
						UIOutput.make(tableRow, "edit-td");
						UILink.make(tableRow, "edit-link", messageLocator.getMessage("simplepage.editItem"), "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.generic").replace("{}", i.getName())));

						// the following information is displayed using <INPUT
						// type=hidden ...
						// it contains information needed to populate the "edit"
						// popup dialog
						UIOutput.make(tableRow, "prerequisite-info", String.valueOf(i.isPrerequisite()));

						String itemGroupString = null;

						if (i.getType() == SimplePageItem.ASSIGNMENT) {
							// the type indicates whether scoring is letter
							// grade, number, etc.
							// the javascript needs this to present the right
							// choices to the user
							// types 6 and 8 aren't legal scoring types, so they
							// are used as
							// markers for quiz or forum. I ran out of numbers
							// and started using
							// text for things that aren't scoring types. That's
							// better anyway
							int type = 4;
							LessonEntity assignment = null;
							if (!i.getSakaiId().equals(SimplePageItem.DUMMY)) {
								assignment = assignmentEntity.getEntity(i.getSakaiId());
								if (assignment != null) {
									type = assignment.getTypeOfGrade();
									String editUrl = assignment.editItemUrl(simplePageBean);
									if (editUrl != null) {
										UIOutput.make(tableRow, "edit-url", editUrl);
									}
									itemGroupString = simplePageBean.getItemGroupString(i, assignment, true);
									UIOutput.make(tableRow, "item-groups", itemGroupString);
								}
							}

							UIOutput.make(tableRow, "type", String.valueOf(type));
							String requirement = String.valueOf(i.getSubrequirement());
							if ((type == SimplePageItem.PAGE || type == SimplePageItem.ASSIGNMENT) && i.getSubrequirement()) {
								requirement = i.getRequirementText();
							}
							UIOutput.make(tableRow, "requirement-text", requirement);
						} else if (i.getType() == SimplePageItem.ASSESSMENT) {
							UIOutput.make(tableRow, "type", "6"); // Not used by
							// assignments,
							// so it is
							// safe to dedicate to assessments
							UIOutput.make(tableRow, "requirement-text", (i.getSubrequirement() ? i.getRequirementText() : "false"));
							LessonEntity quiz = quizEntity.getEntity(i.getSakaiId());
							if (quiz != null) {
								String editUrl = quiz.editItemUrl(simplePageBean);
								if (editUrl != null) {
									UIOutput.make(tableRow, "edit-url", editUrl);
								}
								editUrl = quiz.editItemSettingsUrl(simplePageBean);
								if (editUrl != null) {
									UIOutput.make(tableRow, "edit-settings-url", editUrl);
								}
								itemGroupString = simplePageBean.getItemGroupString(i, quiz, true);
								UIOutput.make(tableRow, "item-groups", itemGroupString);
							}
						} else if (i.getType() == SimplePageItem.BLTI) {
						    UIOutput.make(tableRow, "type", "b");
						    LessonEntity blti= bltiEntity.getEntity(i.getSakaiId());
						    if (blti != null) {
							String editUrl = blti.editItemUrl(simplePageBean);
							if (editUrl != null)
							    UIOutput.make(tableRow, "edit-url", editUrl);
							UIOutput.make(tableRow, "item-format", i.getFormat());

							if (i.getHeight() != null)
							    UIOutput.make(tableRow, "item-height", i.getHeight());
							itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							UIOutput.make(tableRow, "item-groups", itemGroupString );
						    }
						} else if (i.getType() == SimplePageItem.FORUM) {
							UIOutput.make(tableRow, "extra-info");
							UIOutput.make(tableRow, "type", "8");
							LessonEntity forum = forumEntity.getEntity(i.getSakaiId());
							if (forum != null) {
								String editUrl = forum.editItemUrl(simplePageBean);
								if (editUrl != null) {
									UIOutput.make(tableRow, "edit-url", editUrl);
								}
								itemGroupString = simplePageBean.getItemGroupString(i, forum, true);
								UIOutput.make(tableRow, "item-groups", itemGroupString);
							}
						} else if (i.getType() == SimplePageItem.PAGE) {
							UIOutput.make(tableRow, "type", "page");
							UIOutput.make(tableRow, "page-next", Boolean.toString(i.getNextPage()));
							UIOutput.make(tableRow, "page-button", Boolean.toString("button".equals(i.getFormat())));
							itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							UIOutput.make(tableRow, "item-groups", itemGroupString);
						} else if (i.getType() == SimplePageItem.RESOURCE) {
						        itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							if (simplePageBean.getInherited())
							    UIOutput.make(tableRow, "item-groups", "--inherited--");
							else
							    UIOutput.make(tableRow, "item-groups", itemGroupString );
							UIOutput.make(tableRow, "item-samewindow", Boolean.toString(i.isSameWindow()));
						}

						if (itemGroupString != null) {
							itemGroupString = simplePageBean.getItemGroupTitles(itemGroupString);
							if (itemGroupString != null) {
							    UIOutput.make(tableRow, (isInline ? "item-group-titles-div" : "item-group-titles"), " [" + itemGroupString + "]");
							}
						}
					}
					// the following are for the inline item types. Multimedia
					// is the most complex because
					// it can be IMG, IFRAME, or OBJECT, and Youtube is treated
					// separately

				} else if (i.getType() == SimplePageItem.MULTIMEDIA) {
					// the reason this code is complex is that we try to choose
					// the best
					// HTML for displaying the particular type of object. We've
					// added complexities
					// over time as we get more experience with different
					// object types and browsers.

					String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
					String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString);
					if (itemGroupTitles != null) {
						itemGroupTitles = "[" + itemGroupTitles + "]";
					}

					UIOutput.make(tableRow, "item-groups", itemGroupString);

					// the reason this code is complex is that we try to choose
					// the best
					// HTML for displaying the particular type of object. We've
					// added complexities
					// over time as we get more experience with different
					// object types and browsers.

					StringTokenizer token = new StringTokenizer(i.getSakaiId(), ".");

					String extension = "";

					while (token.hasMoreTokens()) {
						extension = token.nextToken().toLowerCase();
					}

					// the extension is almost never used. Normally we have
					// the MIME type and use it. Extension is used only if
					// for some reason we don't have the MIME type
					UIComponent item;
					String youtubeKey;

					Length width = null;
					if (i.getWidth() != null) {
						width = new Length(i.getWidth());
					}
					Length height = null;
					if (i.getHeight() != null) {
						height = new Length(i.getHeight());
					}

					// Get the MIME type. For multimedia types is should be in
					// the html field.
					// The old code saved the URL there. So if it looks like a
					// URL ignore it.
					String mimeType = i.getHtml();
					if (mimeType != null && (mimeType.startsWith("http") || mimeType.equals(""))) {
						mimeType = null;
					}

					// here goes. dispatch on the type and produce the right tag
					// type,
					// followed by the hidden INPUT tags with information for the
					// edit dialog
					if (simplePageBean.isImageType(i)) {

						UIOutput.make(tableRow, "imageSpan");

						if (itemGroupString != null) {
							UIOutput.make(tableRow, "item-group-titles3", itemGroupTitles);
							UIOutput.make(tableRow, "item-groups3", itemGroupString);
						}

						String imageName = i.getAlt();
						if (imageName == null || imageName.equals("")) {
							imageName = abbrevUrl(i.getURL());
						}

						item = UIOutput.make(tableRow, "image").decorate(new UIFreeAttributeDecorator("src", i.getItemURL())).decorate(new UIFreeAttributeDecorator("alt", imageName));
						if (lengthOk(height) && lengthOk(width)) {
							item.decorate(new UIFreeAttributeDecorator("width", width.getOld())).decorate(new UIFreeAttributeDecorator("height", height.getOld()));
						}

						// stuff for the jquery dialog
						if (canEditPage) {
							UIOutput.make(tableRow, "imageHeight", getOrig(height));
							UIOutput.make(tableRow, "imageWidth", getOrig(width));
							UIOutput.make(tableRow, "description2", i.getDescription());
							UIOutput.make(tableRow, "mimetype2", mimeType);
							UIOutput.make(tableRow, "current-item-id4", Long.toString(i.getId()));
							UIOutput.make(tableRow, "editmm-td");
							UILink.make(tableRow, "iframe-edit", messageLocator.getMessage("simplepage.editItem"), "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
						}

					} else if ((youtubeKey = simplePageBean.getYoutubeKey(i)) != null) {
						String youtubeUrl = "http://www.youtube.com/v/" + youtubeKey + "?version=3";
						// this is very odd. The official youtube embedding uses
						// <OBJECT> with
						// a stylesheet to specify size. But the only values
						// that actually
						// work are px and percent. I.e. it works just like the
						// old
						// HTML length types. A real stylesheet length
						// understands other units.
						// I'm generating a style sheet, so that our HTML
						// embedding is as close
						// to theirs as possible, even the lengths are actually
						// interpreted as old style

						UIOutput.make(tableRow, "youtubeSpan");

						if (itemGroupString != null) {
							UIOutput.make(tableRow, "item-group-titles4", itemGroupTitles);
							UIOutput.make(tableRow, "item-groups4", itemGroupString);
						}

						// if width is blank or 100% scale the height
						if (width != null && height != null && !height.number.equals("")) {
							if (width.number.equals("") && width.unit.equals("") || width.number.equals("100") && width.unit.equals("%")) {

								int h = Integer.parseInt(height.number);
								if (h > 0) {
									width.number = Integer.toString((int) Math.round(h * 1.641025641));
									width.unit = height.unit;
								}
							}
						}

						// <object style="height: 390px; width: 640px"><param
						// name="movie"
						// value="http://www.youtube.com/v/AKIC7OQqBrA?version=3"><param
						// name="allowFullScreen" value="true"><param
						// name="allowScriptAccess" value="always"><embed
						// src="http://www.youtube.com/v/AKIC7OQqBrA?version=3"
						// type="application/x-shockwave-flash"
						// allowfullscreen="true" allowScriptAccess="always"
						// width="640" height="390"></object>

						item = UIOutput.make(tableRow, "youtubeObject");
						// youtube seems ok with length and width
						if (lengthOk(height) && lengthOk(width)) {
							item.decorate(new UIFreeAttributeDecorator("style", getStyle(width, height)));
						}
						item.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.youtube_player")));

						UIOutput.make(tableRow, "youtubeURLInject").decorate(new UIFreeAttributeDecorator("value", youtubeUrl));

						item = UIOutput.make(tableRow, "youtubeEmbed").decorate(new UIFreeAttributeDecorator("type", "application/x-shockwave-flash")).decorate(new UIFreeAttributeDecorator("src", youtubeUrl));
						if (lengthOk(height) && lengthOk(width)) {
							item.decorate(new UIFreeAttributeDecorator("height", height.getOld())).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
						}

						if (canEditPage) {
							UIOutput.make(tableRow, "youtubeId", String.valueOf(i.getId()));
							UIOutput.make(tableRow, "currentYoutubeURL", youtubeUrl);
							UIOutput.make(tableRow, "currentYoutubeHeight", getOrig(height));
							UIOutput.make(tableRow, "currentYoutubeWidth", getOrig(width));
							UIOutput.make(tableRow, "description4", i.getDescription());
							UIOutput.make(tableRow, "current-item-id5", Long.toString(i.getId()));

							UIOutput.make(tableRow, "youtube-td");
							UILink.make(tableRow, "youtube-edit", messageLocator.getMessage("simplepage.editItem"), "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.youtube")));
						}

						// as of Oct 28, 2010, we store the mime type. mimeType
						// null is an old entry.
						// For that use the old approach of checking the
						// extension.
						// Otherwise we want to use iframes for HTML and OBJECT
						// for everything else
						// We need the iframes because IE up through 8 doesn't
						// reliably display
						// HTML with OBJECT. Experiments show that everything
						// else works with OBJECT
						// for most browsers. Unfortunately IE, even IE 9,
						// doesn't reliably call the
						// right player with OBJECT. EMBED works. But it's not
						// as nice because you can't
						// nest error recovery code. So we use OBJECT for
						// everything except IE, where we
						// use EMBED. OBJECT does work with Flash.
						// application/xhtml+xml is XHTML.

					} else if ((mimeType != null && !mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) || (mimeType == null && Arrays.binarySearch(multimediaTypes, extension) >= 0)) {

						// this code is used for everything that isn't an image,
						// Youtube, or HTML. Typically
						// this is a flash presentation or a movie. Try to be
						// smart about how we show movies.
						// HTML is done with an IFRAME in the next "if" case
						if (itemGroupString != null) {
							UIOutput.make(tableRow, "item-group-titles5", itemGroupTitles);
							UIOutput.make(tableRow, "item-groups5", itemGroupString);
						}

						UIComponent item2;
						UIOutput.make(tableRow, "movieSpan");

						String movieUrl = i.getItemURL();
						String oMimeType = mimeType; // in case we change it for
						// FLV or others
						boolean useFlvPlayer = false;
						boolean useJwPlayer = false;
						// in theory m4v can be DMRed. But Apple's DRM is
						// useless on a web page, so it's got to be an
						// unprotected file.
						boolean isMp4 = mimeType.equals("video/mp4") || mimeType.equals("video/x-m4v");
						// FLV is special. There's no player for flash video in
						// the browser
						// it shows with a special flash program, which I
						// supply. For the moment MP4 is
						// shown with the same player so it uses much of the
						// same code
						if (mimeType != null && (mimeType.equals("video/x-flv") || isMp4)) {
							mimeType = "application/x-shockwave-flash";
							useJwPlayer = ServerConfigurationService.getBoolean("lessonbuilder.usejwplayer", false);
							if (useJwPlayer) {
								movieUrl = "/sakai-lessonbuildertool-tool/templates/jwflvplayer.swf";
							} else {
								movieUrl = "/sakai-lessonbuildertool-tool/templates/StrobeMediaPlayback.swf";
							}
							useFlvPlayer = true;
						}
						// for IE, if we're not supplying a player it's safest
						// to use embed
						// otherwise Quicktime won't work. Oddly, with IE 9 only
						// it works if you set CLASSID to the MIME type,
						// but that's so unexpected that I hate to rely on it.
						// EMBED is in HTML 5, so I think we're OK
						// using it permanently for IE.
						// I prefer OBJECT where possible because of the nesting
						// ability.
						boolean useEmbed = ieVersion > 0 && !mimeType.equals("application/x-shockwave-flash");

						if (useEmbed) {
							item2 = UIOutput.make(tableRow, "movieEmbed").decorate(new UIFreeAttributeDecorator("src", movieUrl)).decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.mm_player").replace("{}", abbrevUrl(i.getURL()))));
						} else {
							item2 = UIOutput.make(tableRow, "movieObject").decorate(new UIFreeAttributeDecorator("data", movieUrl)).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.mm_player").replace("{}", abbrevUrl(i.getURL()))));
						}
						if (mimeType != null) {
							item2.decorate(new UIFreeAttributeDecorator("type", mimeType));
						}
						if (canEditPage) {
							item2.decorate(new UIFreeAttributeDecorator("style", "border: 1px solid black"));
						}

						// some object types seem to need a specification
						if (lengthOk(height) && lengthOk(width)) {
							item2.decorate(new UIFreeAttributeDecorator("height", height.getOld())).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
						}

						if (!useEmbed) {
							if (useFlvPlayer) {
								UIOutput.make(tableRow, "flashvars").decorate(new UIFreeAttributeDecorator("value", (useJwPlayer ? "file=" : "src=") + URLEncoder.encode(myUrl() + i.getItemURL())));
							}

							UIOutput.make(tableRow, "movieURLInject").decorate(new UIFreeAttributeDecorator("value", movieUrl));
							if (!isMp4) {
								UIOutput.make(tableRow, "noplugin-p", messageLocator.getMessage("simplepage.noplugin"));
								UIOutput.make(tableRow, "noplugin-br");
								UILink.make(tableRow, "noplugin", i.getName(), movieUrl);
							}
						}

						if (isMp4) {
							// do fallback. for ie use EMBED
							if (ieVersion > 0) {
								item2 = UIOutput.make(tableRow, "mp4-embed").decorate(new UIFreeAttributeDecorator("src", i.getItemURL())).decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.mm_player").replace("{}", abbrevUrl(i.getURL()))));
							} else {
								item2 = UIOutput.make(tableRow, "mp4-object").decorate(new UIFreeAttributeDecorator("data", i.getItemURL())).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.mm_player").replace("{}", abbrevUrl(i.getURL()))));
							}
							if (oMimeType != null) {
								item2.decorate(new UIFreeAttributeDecorator("type", oMimeType));
							}

							// some object types seem to need a specification
							if (lengthOk(height) && lengthOk(width)) {
								item2.decorate(new UIFreeAttributeDecorator("height", height.getOld())).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
							}

							if (!useEmbed) {
								UIOutput.make(tableRow, "mp4-inject").decorate(new UIFreeAttributeDecorator("value", i.getItemURL()));

								UIOutput.make(tableRow, "mp4-noplugin-p", messageLocator.getMessage("simplepage.noplugin"));
								UILink.make(tableRow, "mp4-noplugin", i.getName(), i.getItemURL());
							}
						}

						if (canEditPage) {
							UIOutput.make(tableRow, "movieId", String.valueOf(i.getId()));
							UIOutput.make(tableRow, "movieHeight", getOrig(height));
							UIOutput.make(tableRow, "movieWidth", getOrig(width));
							UIOutput.make(tableRow, "description3", i.getDescription());
							UIOutput.make(tableRow, "mimetype5", oMimeType);
							UIOutput.make(tableRow, "current-item-id6", Long.toString(i.getId()));

							UIOutput.make(tableRow, "movie-td");
							UILink.make(tableRow, "edit-movie", messageLocator.getMessage("simplepage.editItem"), "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
						}
					} else {
						// finally, HTML. Use an iframe
						// definition of resizeiframe, at top of page
						if (getOrig(height).equals("auto")) {
							UIOutput.make(tofill, "iframeJavascript");
						}

						UIOutput.make(tableRow, "iframeSpan");

						if (itemGroupString != null) {
							UIOutput.make(tableRow, "item-group-titles2", itemGroupTitles);
							UIOutput.make(tableRow, "item-groups2", itemGroupString);
						}

						item = UIOutput.make(tableRow, "iframe").decorate(new UIFreeAttributeDecorator("src", i.getItemURL()));
						// if user specifies auto, use Javascript to resize the
						// iframe when the
						// content changes. This only works for URLs with the
						// same origin, i.e.
						// URLs in this sakai system
						if (getOrig(height).equals("auto")) {
							item.decorate(new UIFreeAttributeDecorator("onload", "resizeiframe('" + item.getFullID() + "')"));
							if (lengthOk(width)) {
								item.decorate(new UIFreeAttributeDecorator("width", width.getOld()));
							}
							item.decorate(new UIFreeAttributeDecorator("height", "300"));
						} else {
							// we seem OK without a spec
							if (lengthOk(height) && lengthOk(width)) {
								item.decorate(new UIFreeAttributeDecorator("height", height.getOld())).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
							}
						}
						item.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.web_content").replace("{}", abbrevUrl(i.getURL()))));

						if (canEditPage) {
							UIOutput.make(tableRow, "iframeHeight", getOrig(height));
							UIOutput.make(tableRow, "iframeWidth", getOrig(width));
							UIOutput.make(tableRow, "description5", i.getDescription());
							UIOutput.make(tableRow, "mimetype3", mimeType);
							UIOutput.make(tableRow, "current-item-id3", Long.toString(i.getId()));
							UIOutput.make(tableRow, "editmm-td");
							UILink.make(tableRow, "iframe-edit", messageLocator.getMessage("simplepage.editItem"), "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
						}
					}

					// end of multimedia object

				} else if (i.getType() == SimplePageItem.COMMENTS) {
					// Load later using AJAX and CommentsProducer

					UIOutput.make(tableRow, "commentsSpan");

					Placement placement = toolManager.getCurrentPlacement();
					UIOutput.make(tableRow, "placementId", placement.getId());

					CommentsViewParameters eParams = new CommentsViewParameters(CommentsProducer.VIEW_ID);
					eParams.itemId = i.getId();
					if (params.postedComment) {
						eParams.postedComment = postedCommentId;
					}

					UIInternalLink.make(tableRow, "commentsLink", eParams);

					if (!addedCommentsScript) {
						UIOutput.make(tofill, "comments-script");
						UIOutput.make(tofill, "fckScript");
						addedCommentsScript = true;
						UIOutput.make(tofill, "delete-dialog");
					}

					if (canEditPage) {
						UIOutput.make(tableRow, "comments-td");

						UILink.make(tableRow, "edit-comments", messageLocator.getMessage("simplepage.editItem"), "")
								.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.comments")));

						UIOutput.make(tableRow, "commentsId", String.valueOf(i.getId()));
						UIOutput.make(tableRow, "commentsAnon", String.valueOf(i.isAnonymous()));
						String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
						if (itemGroupString != null) {
						    String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString);
						    if (itemGroupTitles != null) {
							itemGroupTitles = "[" + itemGroupTitles + "]";
						    }
						    UIOutput.make(tableRow, "comments-groups", itemGroupString);
						    UIOutput.make(tableRow, "item-group-titles6", itemGroupTitles);
						}
					}

					UIForm form = UIForm.make(tableRow, "comment-form");

					UIInput.make(form, "comment-item-id", "#{simplePageBean.itemId}", String.valueOf(i.getId()));
					UIInput.make(form, "comment-edit-id", "#{simplePageBean.editId}");

					UIInput fckInput = UIInput.make(form, "comment-text-area-evolved:", "#{simplePageBean.formattedComment}");
					fckInput.decorate(new UIFreeAttributeDecorator("height", "175"));
					fckInput.decorate(new UIFreeAttributeDecorator("width", "800"));
					fckInput.decorate(new UIStyleDecorator("evolved-box"));

					((SakaiFCKTextEvolver) richTextEvolver).evolveTextInput(fckInput, "" + commentsCount);

					UICommand.make(form, "add-comment", "#{simplePageBean.addComment}");
				}else if(i.getType() == SimplePageItem.STUDENT_CONTENT) {
					UIOutput.make(tableRow, "studentSpan");
					UIOutput.make(tableRow, "studentDiv");
					
					HashMap<Long, SimplePageLogEntry> cache = simplePageBean.cacheStudentPageLogEntries(i.getId());
					List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(i.getId());
					
					boolean hasOwnPage = false;
					String userId = UserDirectoryService.getCurrentUser().getId();
					
					HashMap<String, String> anonymousLookup = new HashMap<String, String>();
					if(i.isAnonymous()) {
						int counter = 1;
						for(SimpleStudentPage page : studentPages) {
							if(anonymousLookup.get(page.getOwner()) == null) {
								anonymousLookup.put(page.getOwner(), messageLocator.getMessage("simplepage.anonymous") + " " + counter++);
							}
						}
					}
					
					// Print each row in the table
					for(SimpleStudentPage page : studentPages) {
						if(page.isDeleted()) continue;
						
						SimplePageLogEntry entry = cache.get(page.getPageId());
						UIBranchContainer row = UIBranchContainer.make(tableRow, "studentRow:");
						UIOutput.make(row, "studentCell");
						
						GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, page.getPageId());
						eParams.setItemId(i.getId());
						eParams.setPath("push");
						
						String username = null;
						
						try {
							username = i.isAnonymous()? anonymousLookup.get(page.getOwner()) : UserDirectoryService.getUser(page.getOwner()).getDisplayName();
							
							if(i.isAnonymous() && canEditPage) {
								username += " (" + UserDirectoryService.getUser(page.getOwner()).getDisplayName() + ")";
							}else if(i.isAnonymous() && page.getOwner().equals(userId)) {
								username += " (" + messageLocator.getMessage("simplepage.comment-you") + ")";
							}
						} catch (UserNotDefinedException e) {
							username = page.getTitle();
						}
						
						UIInternalLink.make(row, "studentLink", username, eParams);
						
						// Never visited page
						if(entry == null) {
							UIOutput.make(row, "newPageImg").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-student-page")));
						}
						
						// There's content they haven't seen
						if(entry == null || entry.getLastViewed().compareTo(page.getLastUpdated()) < 0) {
							UIOutput.make(row, "newContentImg").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-student-content")));
						}
						
						// The comments tool exists, so we might have to show the icon
						if(i.getShowComments() != null && i.getShowComments()) {
							UIOutput.make(row, "commentsImgCell");
						}
						
						// New comments have been added since they last viewed the page
						if(page.getLastCommentChange() != null && (entry == null || entry.getLastViewed().compareTo(page.getLastCommentChange()) < 0)) {
							UIOutput.make(row, "newCommentsImg").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-student-comments")));
						}
						
						if(page.getOwner().equals(userId)) {
							hasOwnPage = true;
						}
					}
					
					if(!hasOwnPage) {
						UIOutput.make(tableRow, "linkRow");
						UIOutput.make(tableRow, "linkCell");
						
						GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
						eParams.addTool = GeneralViewParameters.STUDENT_PAGE;
						eParams.studentItemId = i.getId();
						UIInternalLink.make(tableRow, "linkLink", messageLocator.getMessage("simplepage.add-page"), eParams);
					}
					
					if(canEditPage) {
						UIOutput.make(tableRow, "student-td");
						UILink.make(tableRow, "edit-student", messageLocator.getMessage("simplepage.editItem"), "")
								.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.student")));
						
						UIOutput.make(tableRow, "studentId", String.valueOf(i.getId()));
						UIOutput.make(tableRow, "studentAnon", String.valueOf(i.isAnonymous()));
						UIOutput.make(tableRow, "studentComments", String.valueOf(i.getShowComments()));
						UIOutput.make(tableRow, "forcedAnon", String.valueOf(i.getForcedCommentsAnonymous()));
						String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
						if (itemGroupString != null) {
						    String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString);
						    if (itemGroupTitles != null) {
							itemGroupTitles = "[" + itemGroupTitles + "]";
						    }
						    UIOutput.make(tableRow, "student-groups", itemGroupString);
						    UIOutput.make(tableRow, "item-group-titles7", itemGroupTitles);
						}
					}
				} else {
					// remaining type must be a block of HTML
					UIOutput.make(tableRow, "itemSpan");

					String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
					String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString);
					if (itemGroupTitles != null) {
						itemGroupTitles = "[" + itemGroupTitles + "]";
					}

					UIOutput.make(tableRow, "item-groups-titles-text", itemGroupTitles);

					UIVerbatim.make(tableRow, "content", (i.getHtml() == null ? "" : i.getHtml()));

					// editing is done using a special producer that calls FCK.
					if (canEditPage) {
						GeneralViewParameters eParams = new GeneralViewParameters();
						eParams.setSendingPage(currentPage.getPageId());
						eParams.setItemId(i.getId());
						eParams.viewID = EditPageProducer.VIEW_ID;
						UIOutput.make(tableRow, "edittext-td");
						UIInternalLink.make(tableRow, "edit-link", messageLocator.getMessage("simplepage.editItem"), eParams).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.textbox").replace("{}", Integer.toString(textboxcount))));

						textboxcount++;
					}
				}
			}

			// end of items. This is the end for normal users. Following is
			// special
			// checks and putting out the dialogs for the popups, for
			// instructors.

			boolean showBreak = false;

			// I believe refresh is now done automatically in all cases
			// if (showRefresh) {
			// UIOutput.make(tofill, "refreshAlert");
			//
			// // Should simply refresh
			// GeneralViewParameters p = new GeneralViewParameters(VIEW_ID);
			// p.setSendingPage(currentPage.getPageId());
			// UIInternalLink.make(tofill, "refreshLink", p);
			// showBreak = true;
			// }

			// stuff goes on the page in the order in the HTML file. So the fact
			// that it's here doesn't mean it shows
			// up at the end. This code produces errors and other odd stuff.

			if (canEditPage) {
				// if the page is hidden, warn the faculty [students get stopped
				// at
				// the top]
				if (currentPage.isHidden()) {
					UIOutput.make(tofill, "hiddenAlert").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.pagehidden")));
					UIVerbatim.make(tofill, "hidden-text", messageLocator.getMessage("simplepage.pagehidden.text"));

					showBreak = true;
					// similarly warn them if it isn't released yet
				} else if (currentPage.getReleaseDate() != null && currentPage.getReleaseDate().after(new Date())) {
					DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, M_locale);
					TimeZone tz = timeService.getLocalTimeZone();
					df.setTimeZone(tz);
					String releaseDate = df.format(currentPage.getReleaseDate());
					UIOutput.make(tofill, "hiddenAlert").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.notreleased")));
					UIVerbatim.make(tofill, "hidden-text", messageLocator.getMessage("simplepage.notreleased.text").replace("{}", releaseDate));
					showBreak = true;
				}
			}

			if (showBreak) {
				UIOutput.make(tofill, "breakAfterWarnings");
			}
		}

		// more warnings: if no item on the page, give faculty instructions,
		// students an error
		if (itemList.size() == 0) {
			if (canEditPage) {
				UIOutput.make(tofill, "startupHelp").decorate(new UIFreeAttributeDecorator("src", getLocalizedURL("general.html"))).decorate(new UIFreeAttributeDecorator("id", "iframe"));
				UIOutput.make(tofill, "iframeJavascript");

			} else {
				UIOutput.make(tofill, "error-div");
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.noitems_error_user"));
			}
		}

		// now output the dialogs. but only for faculty (to avoid making the
		// file bigger)
		if (canEditPage) {
			createSubpageDialog(tofill, currentPage);
		}

		createDialogs(tofill, currentPage, pageItem);
	}
	
	public void createDialogs(UIContainer tofill, SimplePage currentPage, SimplePageItem pageItem) {
		createEditItemDialog(tofill, currentPage, pageItem);
		createAddMultimediaDialog(tofill, currentPage);
		createEditMultimediaDialog(tofill, currentPage);
		createEditTitleDialog(tofill, currentPage, pageItem);
		createNewPageDialog(tofill, currentPage, pageItem);
		createRemovePageDialog(tofill, currentPage, pageItem);
		createImportCcDialog(tofill);
		createYoutubeDialog(tofill, currentPage);
		createMovieDialog(tofill, currentPage);
		createCommentsDialog(tofill);
		createStudentContentDialog(tofill);
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	private boolean makeLink(UIContainer container, String ID, SimplePageItem i, boolean canEditPage, SimplePage currentPage, boolean notDone, Status status) {
		return makeLink(container, ID, i, simplePageBean, simplePageToolDao, messageLocator, canEditPage, currentPage, notDone, status);
	}

	/**
	 * 
	 * @param container
	 * @param ID
	 * @param i
	 * @param simplePageBean
	 * @param simplePageToolDao
	 * @return Whether or not this item is available.
	 */
	protected static boolean makeLink(UIContainer container, String ID, SimplePageItem i, SimplePageBean simplePageBean, SimplePageToolDao simplePageToolDao, MessageLocator messageLocator,
			boolean canEditPage, SimplePage currentPage, boolean notDone, Status status) {
		String URL = "";
		boolean available = simplePageBean.isItemAvailable(i);

		if (i.getSakaiId().equals(SimplePageItem.DUMMY)) {
			UILink link = UILink.make(container, ID);
			// disablelink adds a tooltip telling the user to complete prereq's.
			// That's not appropriate here
			link.decorate(new UIFreeAttributeDecorator("onclick", "return false"));
			link.decorate(new UIDisabledDecorator());
			link.decorate(new UIStyleDecorator("disabled"));
		} else if (i.getType() == SimplePageItem.RESOURCE || i.getType() == SimplePageItem.URL) {

			if (i.getType() == SimplePageItem.RESOURCE && i.isSameWindow()) {
				if (available) {
					GeneralViewParameters params = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
					params.setSendingPage(currentPage.getPageId());
					params.setSource(i.getItemURL());
					params.setItemId(i.getId());
					UIInternalLink.make(container, "link", params);
				} else {
					UIInternalLink link = LinkTrackerProducer.make(container, ID, i.getName(), URL, i.getId(), notDone);
					disableLink(link, messageLocator);
				}
			} else {
				if (available) {
					URL = i.getItemURL();
				}

				UIInternalLink link = LinkTrackerProducer.make(container, ID, i.getName(), URL, i.getId(), notDone);

				if (available) {
					link.decorate(new UIFreeAttributeDecorator("target", "_blank"));
				} else {
					disableLink(link, messageLocator);
				}
			}
		} else if (i.getType() == SimplePageItem.PAGE) {
			SimplePage p = simplePageToolDao.getPage(Long.valueOf(i.getSakaiId()));

			if(p != null) {
				GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, p.getPageId());
				eParams.setItemId(i.getId());
				// nextpage indicates whether it should be pushed onto breadcrumbs
				// or replace the top item
				if (i.getNextPage()) {
					eParams.setPath("next");
				} else {
					eParams.setPath("push");
				}
				boolean isbutton = false;
				// button says to display the link as a button. use navIntrTool,
				// which is standard
				// Sakai CSS that generates the type of button used in toolbars. We
				// have to override
				// with background:transparent or we get remnants of the gray
				if ("button".equals(i.getFormat())) {
					isbutton = true;
					UIOutput span = UIOutput.make(container, ID + "-button-span");
					ID = ID + "-button";
					if (!i.isRequired()) {
						span.decorate(new UIFreeAttributeDecorator("class", "navIntraTool buttonItem"));
					}
					isbutton = true;
				}
				
				UILink link;
				if (available) {
					link = UIInternalLink.make(container, ID, eParams);
					if (i.isPrerequisite()) {
						simplePageBean.checkItemPermissions(i, true);
					}
					// at this point we know the page isn't available, i.e. user
					// hasn't
					// met all the prerequistes. Normally we give them a nonworking
					// grayed out link. But if they are the author, we want to
					// give them a real link. Otherwise if it's a subpage they have
					// no way to get to it (currently -- we'll fix that)
					// but we make it look like it's disabled so they can see what
					// students see
				} else if (canEditPage) {
					link = UIInternalLink.make(container, ID, eParams);
					fakeDisableLink(link, messageLocator);
				} else {
					link = UILink.make(container, ID);
					disableLink(link, messageLocator);
				}
			}else {
				log.warn("Lesson Builder Item #" + i.getId() + " does not have an associated page.");
				return false;
			}
		} else if (i.getType() == SimplePageItem.ASSIGNMENT) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}

				GeneralViewParameters params = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				params.setSendingPage(currentPage.getPageId());
				LessonEntity lessonEntity = assignmentEntity.getEntity(i.getSakaiId());
				params.setSource((lessonEntity == null) ? "dummy" : lessonEntity.getUrl());
				params.setItemId(i.getId());
				UIInternalLink.make(container, "link", params);
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID);
				disableLink(link, messageLocator);
			}
		} else if (i.getType() == SimplePageItem.ASSESSMENT) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}

				// we've hacked Samigo to look at a special lesson builder
				// session
				// attribute. otherwise at the end of the test, Samigo replaces
				// the
				// whole screen, exiting form our iframe. The other tools don't
				// do this.
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setClearAttr("LESSONBUILDER_RETURNURL_SAMIGO");
				LessonEntity lessonEntity = quizEntity.getEntity(i.getSakaiId());
				view.setSource((lessonEntity == null) ? "dummy" : lessonEntity.getUrl());
				view.setItemId(i.getId());
				UIInternalLink.make(container, "link", view);
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID, "");
				disableLink(link, messageLocator);
			}
		} else if (i.getType() == SimplePageItem.FORUM) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setItemId(i.getId());
				LessonEntity lessonEntity = forumEntity.getEntity(i.getSakaiId());
				view.setSource((lessonEntity == null) ? "dummy" : lessonEntity.getUrl());
				UIInternalLink.make(container, "link", view);
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID);
				disableLink(link, messageLocator);
			}
		} else if (i.getType() == SimplePageItem.BLTI) {
		    LessonEntity lessonEntity = bltiEntity.getEntity(i.getSakaiId());
		    if ("inline".equals(i.getFormat())) {
			// no availability 
			String height=null;
			if (i.getHeight() != null && !i.getHeight().equals(""))
			    height = height.replace("px","");  // just in case
			
			UIComponent iframe = UIOutput.make(container, "blti-iframe");
			if (lessonEntity != null)
			    iframe.decorate(new UIFreeAttributeDecorator("src", lessonEntity.getUrl()));
			
			String h = "300";
			if (height != null && !height.trim().equals(""))
			    h = height;
			
			iframe.decorate(new UIFreeAttributeDecorator("height", h));
			iframe.decorate(new UIFreeAttributeDecorator("title", i.getName()));
			// normally we get the name from the link text, but there's no link text here
			UIOutput.make(container, "item-name", i.getName());
		    } else if (!"window".equals(i.getFormat())) {
			// this is the default if format isn't valid or is missing
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setItemId(i.getId());
				view.setSource((lessonEntity==null)?"dummy":lessonEntity.getUrl());
				UIInternalLink.make(container, "link", view);

			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				UILink link = UILink.make(container, ID);
				disableLink(link, messageLocator);
			}
		    } else {

			if (available) {
			    URL = (lessonEntity==null)?"dummy":lessonEntity.getUrl();
			}
			UIInternalLink link = LinkTrackerProducer.make(container, ID, i.getName(), URL, i.getId(), notDone);

			if (available) {
				link.decorate(new UIFreeAttributeDecorator("target", "_blank"));
			} else {
				disableLink(link, messageLocator);
			}
		    }
		}

		String note = null;
		if (status == Status.COMPLETED) {
			note = messageLocator.getMessage("simplepage.status.completed");
		}
		if (status == Status.REQUIRED) {
			note = messageLocator.getMessage("simplepage.status.required");
		}

		UIOutput.make(container, ID + "-text", i.getName());
		if (note != null) {
			UIOutput.make(container, ID + "-note", note + " ");
		}

		return available;
	}

	private static void disableLink(UILink link, MessageLocator messageLocator) {
		link.decorate(new UIFreeAttributeDecorator("onclick", "return false"));
		link.decorate(new UIDisabledDecorator());
		link.decorate(new UIStyleDecorator("disabled"));
		link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.complete_required")));
	}

	// show is if it was disabled but don't actually
	private static void fakeDisableLink(UILink link, MessageLocator messageLocator) {
		link.decorate(new UIDisabledDecorator());
		link.decorate(new UIStyleDecorator("disabled"));
		link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.complete_required")));
	}

	public void setSimplePageToolDao(SimplePageToolDao s) {
		simplePageToolDao = s;
	}

	public void setDateEvolver(FormatAwareDateInputEvolver dateevolver) {
		this.dateevolver = dateevolver;
	}

	public void setTimeService(TimeService ts) {
		timeService = ts;
	}

	public void setLocaleGetter(LocaleGetter localegetter) {
		this.localegetter = localegetter;
	}

	public void setForumEntity(LessonEntity e) {
		// forumEntity is static, so it may already have been set
		// there is a possible race condition, but since the bean is
		// a singleton both people in the race will be trying to set
		// the same value. So it shouldn't matter
		if (forumEntity == null) {
			forumEntity = e;
		}
	}

	public void setQuizEntity(LessonEntity e) {
		// forumEntity is static, so it may already have been set
		// there is a possible race condition, but since the bean is
		// a singleton both people in the race will be trying to set
		// the same value. So it shouldn't matter
		if (quizEntity == null) {
			quizEntity = e;
		}
	}

	public void setAssignmentEntity(LessonEntity e) {
		// forumEntity is static, so it may already have been set
		// there is a possible race condition, but since the bean is
		// a singleton both people in the race will be trying to set
		// the same value. So it shouldn't matter
		if (assignmentEntity == null) {
			assignmentEntity = e;
		}
	}

	public void setBltiEntity(LessonEntity e) {
	    	if (bltiEntity == null)
			bltiEntity = e;
	}

	public void setMemoryService(MemoryService m) {
		memoryService = m;
		// do this here rather than in an initializer because we need
		// memoryservice
		// slight race condition possible on outer test, so have to test again
		// when we have the lock
		if (urlCache == null) {
			synchronized (urlCacheLock) {
				if (urlCache == null) {
					urlCache = memoryService.newCache("org.sakaiproject.lessonbuildertool.tool.producers.ShowPageProducer.url.cache");
				}
			}
		}

	}

	public void setToolManager(ToolManager m) {
		toolManager = m;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	/**
	 * Checks for the version of IE. Returns 0 if we're not running IE.
	 * 
	 * @return
	 */
	public int checkIEVersion() {
		UsageSession usageSession = UsageSessionService.getSession();
		String browserString = usageSession.getUserAgent();
		int ieIndex = browserString.indexOf(" MSIE ");
		int ieVersion = 0;
		if (ieIndex >= 0) {
			String ieV = browserString.substring(ieIndex + 6);
			int i = 0;
			int e = ieV.length();
			while (i < e) {
				if (Character.isDigit(ieV.charAt(i))) {
					i++;
				} else {
					break;
				}
			}
			if (i > 0) {
				ieV = ieV.substring(0, i);
				ieVersion = Integer.parseInt(ieV);
			}
		}

		return ieVersion;
	}

	private void createToolBar(UIContainer tofill, SimplePage currentPage) {
		UIBranchContainer toolBar = UIBranchContainer.make(tofill, "tool-bar:");

		// decided not to use long tooltips. with screen reader they're too
		// verbose. We now have good help
		createToolBarLink(ReorderProducer.VIEW_ID, toolBar, "reorder", "simplepage.reorder", currentPage, "simplepage.reorder-tooltip");

		createToolBarLink(EditPageProducer.VIEW_ID, toolBar, "add-text", "simplepage.text", currentPage, "simplepage.text.tooltip").setItemId(null);
		
		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar, "add-resource", "simplepage.resource", false, false,  currentPage, "simplepage.resource.tooltip");

		UILink subpagelink = UIInternalLink.makeURL(toolBar, "subpage-link", "#");
		subpagelink.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.subpage")));
		subpagelink.linktext = new UIBoundString(messageLocator.getMessage("simplepage.subpage"));

		//createToolBarLink(AssignmentPickerProducer.VIEW_ID, toolBar, "add-assignment", "simplepage.assignment", currentPage, "simplepage.assignment");
		//createToolBarLink(QuizPickerProducer.VIEW_ID, toolBar, "add-quiz", "simplepage.quiz", currentPage, "simplepage.quiz");
		//createToolBarLink(ForumPickerProducer.VIEW_ID, toolBar, "add-forum", "simplepage.forum", currentPage, "simplepage.forum");
		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar, "add-multimedia", "simplepage.multimedia", true, false, currentPage, "simplepage.multimedia.tooltip");

		UILink.make(toolBar, "help", messageLocator.getMessage("simplepage.help"), getLocalizedURL("general.html"));

		// Don't show these tools on a student page.
		if(currentPage.getOwner() == null) {
			// Q: Are we running a kernel with KNL-273?
			Class contentHostingInterface = ContentHostingService.class;
			try {
				Method expandMethod = contentHostingInterface.getMethod("expandZippedResource", new Class[] { String.class });
				createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar, "add-website", "simplepage.website", false, true, currentPage, "simplepage.website.tooltip");
			} catch (NoSuchMethodException nsme) {
				// A: No
			} catch (Exception e) {
				// A: Not sure
				log.warn("SecurityException thrown by expandZippedResource method lookup", e);
			}
			
			createToolBarLink(AssignmentPickerProducer.VIEW_ID, toolBar, "add-assignment", "simplepage.assignment", currentPage, "simplepage.assignment");
			
			createToolBarLink(QuizPickerProducer.VIEW_ID, toolBar, "add-quiz", "simplepage.quiz", currentPage, "simplepage.quiz");
			
			createToolBarLink(ForumPickerProducer.VIEW_ID, toolBar, "add-forum", "simplepage.forum", currentPage, "simplepage.forum");
			// in case we're on an old system without current BLTI
			if (((BltiInterface)bltiEntity).servicePresent())
			    createToolBarLink(BltiPickerProducer.VIEW_ID, toolBar, "add-blti", "simplepage.blti", currentPage, "simplepage.blti");
			createToolBarLink(PermissionsHelperProducer.VIEW_ID, toolBar, "permissions", "simplepage.permissions", currentPage, "simplepage.permissions.tooltip");
			
			GeneralViewParameters eParams = new GeneralViewParameters(VIEW_ID);
			eParams.addTool = GeneralViewParameters.COMMENTS;
			UIInternalLink.make(toolBar, "add-comments", messageLocator.getMessage("simplepage.comments"), eParams);
			
			eParams = new GeneralViewParameters(VIEW_ID);
			eParams.addTool = GeneralViewParameters.STUDENT_CONTENT;
			UIInternalLink.make(toolBar, "add-content", messageLocator.getMessage("simplepage.add-content"), eParams);
		}
	}

	private GeneralViewParameters createToolBarLink(String viewID, UIContainer tofill, String ID, String message, SimplePage currentPage, String tooltip) {
		GeneralViewParameters params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		createStandardToolBarLink(viewID, tofill, ID, message, params, tooltip);
		return params;
	}


	private FilePickerViewParameters createFilePickerToolBarLink(String viewID, UIContainer tofill, String ID, String message, boolean resourceType, boolean website, SimplePage currentPage, String tooltip) {
		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(resourceType);
		fileparams.setWebsite(website);
		createStandardToolBarLink(viewID, tofill, ID, message, fileparams, tooltip);
		return fileparams;
	}

	private void createStandardToolBarLink(String viewID, UIContainer tofill, String ID, String message, SimpleViewParameters params, String tooltip) {
		params.viewID = viewID;
		UILink link = UIInternalLink.make(tofill, ID, messageLocator.getMessage(message), params);
		link.decorate(new UITooltipDecorator(messageLocator.getMessage(tooltip)));
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase(null, new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(ReloadPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("reload", new SimpleViewParameters(ReloadPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("removed", new SimpleViewParameters(RemovePageProducer.VIEW_ID)));
		
		return togo;
	}

	private void createSubpageDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "subpage-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.subpage")));
		UIForm form = UIForm.make(tofill, "subpage-form");

		UIOutput.make(form, "subpage-label", messageLocator.getMessage("simplepage.pageTitle_label"));
		UIInput.make(form, "subpage-title", "#{simplePageBean.subpageTitle}");

		GeneralViewParameters view = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
		view.setSendingPage(currentPage.getPageId());

		if(currentPage.getOwner() == null) {
			UIInternalLink.make(form, "subpage-choose", messageLocator.getMessage("simplepage.choose_existing_page"), view);
		}
		
		UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
		UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);

		UICommand.make(form, "create-subpage", messageLocator.getMessage("simplepage.create"), "#{simplePageBean.createSubpage}");
		UICommand.make(form, "cancel-subpage", messageLocator.getMessage("simplepage.cancel"), null);

	}

	private void createEditItemDialog(UIContainer tofill, SimplePage currentPage, SimplePageItem pageItem) {
		UIOutput.make(tofill, "edit-item-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edititem_header")));

		UIForm form = UIForm.make(tofill, "edit-form");

		UIOutput.make(form, "name-label", messageLocator.getMessage("simplepage.name_label"));
		UIInput.make(form, "name", "#{simplePageBean.name}");

		UIOutput.make(form, "description-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description", "#{simplePageBean.description}");

		UIOutput changeDiv = UIOutput.make(form, "changeDiv");
		if(currentPage.getOwner() != null) changeDiv.decorate(new UIStyleDecorator("noDisplay"));
		
		GeneralViewParameters params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = AssignmentPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-assignment", messageLocator.getMessage("simplepage.change_assignment"), params);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = QuizPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-quiz", messageLocator.getMessage("simplepage.change_quiz"), params);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = ForumPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-forum", messageLocator.getMessage("simplepage.change_forum"), params);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = BltiPickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-blti", messageLocator.getMessage("simplepage.change_blti"), params);

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(false);
		fileparams.setWebsite(false);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-resource", messageLocator.getMessage("simplepage.change_resource"), fileparams);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.viewID = PagePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-page", messageLocator.getMessage("simplepage.change_page"), params);

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.setItemId(pageItem.getId());
		params.setReturnView(VIEW_ID);
		params.setTitle(messageLocator.getMessage("simplepage.return_from_edit"));
		params.setSource("SRC");
		params.viewID = ShowItemProducer.VIEW_ID;
		UIInternalLink.make(form, "edit-item-object", params);
		UIOutput.make(form, "edit-item-text");

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.setItemId(pageItem.getId());
		params.setReturnView(VIEW_ID);
		params.setTitle(messageLocator.getMessage("simplepage.return_from_edit"));
		params.setSource("SRC");
		params.viewID = ShowItemProducer.VIEW_ID;
		UIInternalLink.make(form, "edit-item-settings", params);
		UIOutput.make(form, "edit-item-settings-text");

		UIBoundBoolean.make(form, "item-next", "#{simplePageBean.subpageNext}", false);
		UIBoundBoolean.make(form, "item-button", "#{simplePageBean.subpageButton}", false);

		UIInput.make(form, "item-id", "#{simplePageBean.itemId}");

		UIOutput permDiv = UIOutput.make(form, "permDiv");
		if(currentPage.getOwner() != null) permDiv.decorate(new UIStyleDecorator("noDisplay"));
		
		UIBoundBoolean.make(form, "item-required2", "#{simplePageBean.subrequirement}", false);

		UIBoundBoolean.make(form, "item-required", "#{simplePageBean.required}", false);
		UIBoundBoolean.make(form, "item-prerequisites", "#{simplePageBean.prerequisite}", false);

		UIBoundBoolean.make(form, "item-newwindow", "#{simplePageBean.newWindow}", false);

		UISelect radios = UISelect.make(form, "format-select",
						new String[] {"window", "inline", "page"},
						"#{simplePageBean.format}", "");
		UISelectChoice.make(form, "format-window", radios.getFullID(), 0);
		UISelectChoice.make(form, "format-inline", radios.getFullID(), 1);
		UISelectChoice.make(form, "format-page", radios.getFullID(), 2);

		UIInput.make(form, "edit-height-value", "#{simplePageBean.height}");

		UISelect.make(form, "assignment-dropdown", SimplePageBean.GRADES, "#{simplePageBean.dropDown}", SimplePageBean.GRADES[0]);
		UIInput.make(form, "assignment-points", "#{simplePageBean.points}");

		UICommand.make(form, "edit-item", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.editItem}");

		createGroupList(form, null);

		UICommand.make(form, "delete-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "edit-item-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	// for both add multimedia and add resource, as well as updating resources
	// in the edit dialogs
	public void createGroupList(UIContainer tofill, Collection<String> groupsSet) {
		List<GroupEntry> groups = simplePageBean.getCurrentGroups();
		ArrayList<String> values = new ArrayList<String>();
		ArrayList<String> initValues = new ArrayList<String>();

		if (groups == null || groups.size() == 0)
			return;

		for (GroupEntry entry : groups) {
			if (entry.name.startsWith("Access: ")) {
				continue;
			}
			values.add(entry.id);
			if (groupsSet != null && groupsSet.contains(entry.id)) {
				initValues.add(entry.id);
			}
		}
		if (groupsSet == null || groupsSet.size() == 0) {
			initValues.add("");
		}

		// this could happen if the only groups are Access groups
		if (values.size() == 0)
			return;

		UIOutput.make(tofill, "grouplist");
		UISelect select = UISelect.makeMultiple(tofill, "group-list-span", values.toArray(new String[1]), "#{simplePageBean.selectedGroups}", initValues.toArray(new String[1]));

		int index = 0;
		for (GroupEntry entry : groups) {
			if (entry.name.startsWith("Access: ")) {
				continue;
			}
			UIBranchContainer row = UIBranchContainer.make(tofill, "select-group-list:");
			UISelectChoice.make(row, "select-group", select.getFullID(), index);

			UIOutput.make(row, "select-group-text", entry.name);
			index++;
		}

	}

	// for both add multimedia and add resource, as well as updating resources
	// in the edit dialogs
	private void createAddMultimediaDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "add-multimedia-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.resource")));
		UILink.make(tofill, "mm-additional-instructions", messageLocator.getMessage("simplepage.additional-instructions-label"), messageLocator.getMessage("simplepage.additional-instructions"));
		UILink.make(tofill, "mm-additional-website-instructions", messageLocator.getMessage("simplepage.additional-website-instructions-label"), messageLocator.getMessage("simplepage.additional-website-instructions"));

		UIForm form = UIForm.make(tofill, "add-multimedia-form");

		UIOutput.make(form, "mm-file-label", messageLocator.getMessage("simplepage.upload_label"));

		UIOutput.make(form, "mm-url-label", messageLocator.getMessage("simplepage.addLink_label"));
		UIInput.make(form, "mm-url", "#{simplePageBean.mmUrl}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		
		UILink link = UIInternalLink.make(form, "mm-choose", messageLocator.getMessage("simplepage.choose_existing"), fileparams);
		
		// createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, toolBar,
		// "add-resource", "simplepage.resource", false, currentPage,
		// "simplepage.resource.tooltip");

		UICommand.make(form, "mm-add-item", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.addMultimedia}");
		UIInput.make(form, "mm-item-id", "#{simplePageBean.itemId}");
		UIInput.make(form, "mm-is-mm", "#{simplePageBean.isMultimedia}");
		UIInput.make(form, "mm-is-website", "#{simplePageBean.isWebsite}");
		UICommand.make(form, "mm-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createImportCcDialog(UIContainer tofill) {
		UIOutput.make(tofill, "import-cc-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.import_cc")));

		UIForm form = UIForm.make(tofill, "import-cc-form");

		UICommand.make(form, "import-cc-submit", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.importCc}");
		UICommand.make(form, "mm-cancel", messageLocator.getMessage("simplepage.cancel"), null);

		class ToolData {
			String toolId;
			String toolName;
		}

		int numQuizEngines = 0;
		List<ToolData> quizEngines = new ArrayList<ToolData>();

		for (LessonEntity q = quizEntity; q != null; q = q.getNextEntity()) {
			String toolId = q.getToolId();
			String toolName = simplePageBean.getCurrentToolTitle(q.getToolId());
			// we only want the ones that are actually in our site
			if (toolName != null) {
				ToolData toolData = new ToolData();
				toolData.toolId = toolId;
				toolData.toolName = toolName;
				numQuizEngines++;
				quizEngines.add(toolData);
			}
		}

		if (numQuizEngines == 0) {
			UIVerbatim.make(form, "quizmsg", messageLocator.getMessage("simplepage.noquizengines"));
		} else if (numQuizEngines == 1) {
			UIInput.make(form, "quiztool", "#{simplePageBean.quiztool}", quizEntity.getToolId());
		} else { // put up message and then radio buttons for each possibility

			// need values array for RSF's select implementation. It sees radio
			// buttons as a kind of select
			ArrayList<String> values = new ArrayList<String>();
			for (ToolData toolData : quizEngines) {
				values.add(toolData.toolId);
			}

			// the message
			UIOutput.make(form, "quizmsg", messageLocator.getMessage("simplepage.choosequizengine"));
			// now the list of radio buttons
			UISelect quizselect = UISelect.make(form, "quiztools", values.toArray(new String[1]), "#{simplePageBean.quiztool}", null);
			int i = 0;
			for (ToolData toolData : quizEngines) {
				UIBranchContainer toolItem = UIBranchContainer.make(form, "quiztoolitem:", String.valueOf(i));
				UISelectChoice.make(toolItem, "quiztoolbox", quizselect.getFullID(), i);
				UIOutput.make(toolItem, "quiztoollabel", toolData.toolName);
				i++;
			}
		}

		int numTopicEngines = 0;
		List<ToolData> topicEngines = new ArrayList<ToolData>();

		for (LessonEntity q = forumEntity; q != null; q = q.getNextEntity()) {
			String toolId = q.getToolId();
			String toolName = simplePageBean.getCurrentToolTitle(q.getToolId());
			// we only want the ones that are actually in our site
			if (toolName != null) {
				ToolData toolData = new ToolData();
				toolData.toolId = toolId;
				toolData.toolName = toolName;
				numTopicEngines++;
				topicEngines.add(toolData);
			}
		}

		if (numTopicEngines == 0) {
			UIVerbatim.make(form, "topicmsg", messageLocator.getMessage("simplepage.notopicengines"));
		} else if (numTopicEngines == 1) {
			UIInput.make(form, "topictool", "#{simplePageBean.topictool}", forumEntity.getToolId());
		} else {
			ArrayList<String> values = new ArrayList<String>();
			for (ToolData toolData : topicEngines) {
				values.add(toolData.toolId);
			}

			UIOutput.make(form, "topicmsg", messageLocator.getMessage("simplepage.choosetopicengine"));
			UISelect topicselect = UISelect.make(form, "topictools", values.toArray(new String[1]), "#{simplePageBean.topictool}", null);
			int i = 0;
			for (ToolData toolData : topicEngines) {
				UIBranchContainer toolItem = UIBranchContainer.make(form, "topictoolitem:", String.valueOf(i));
				UISelectChoice.make(toolItem, "topictoolbox", topicselect.getFullID(), i);
				UIOutput.make(toolItem, "topictoollabel", toolData.toolName);
				i++;
			}
		}

	}

	private void createEditMultimediaDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "edit-multimedia-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editMultimedia")));

		UIOutput.make(tofill, "instructions");

		UIForm form = UIForm.make(tofill, "edit-multimedia-form");

		UIOutput.make(form, "height-label", messageLocator.getMessage("simplepage.height_label"));
		UIInput.make(form, "height", "#{simplePageBean.height}");

		UIOutput.make(form, "width-label", messageLocator.getMessage("simplepage.width_label"));
		UIInput.make(form, "width", "#{simplePageBean.width}");

		UIOutput.make(form, "description2-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description2", "#{simplePageBean.description}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-resource-mm", messageLocator.getMessage("simplepage.change_resource"), fileparams);

		UIOutput.make(form, "alt-label", messageLocator.getMessage("simplepage.alt_label"));
		UIInput.make(form, "alt", "#{simplePageBean.alt}");

		UIInput.make(form, "mimetype", "#{simplePageBean.mimetype}");

		UICommand.make(form, "edit-multimedia-item", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.editMultimedia}");

		UIInput.make(form, "multimedia-item-id", "#{simplePageBean.itemId}");

		UICommand.make(form, "delete-multimedia-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "edit-multimedia-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createYoutubeDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "youtube-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_youtubelink")));

		UIForm form = UIForm.make(tofill, "youtube-form");
		UIInput.make(form, "youtubeURL", "#{simplePageBean.youtubeURL}");
		UIInput.make(form, "youtubeEditId", "#{simplePageBean.youtubeId}");
		UIInput.make(form, "youtubeHeight", "#{simplePageBean.height}");
		UIInput.make(form, "youtubeWidth", "#{simplePageBean.width}");
		UIOutput.make(form, "description4-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description4", "#{simplePageBean.description}");
		UICommand.make(form, "delete-youtube-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteYoutubeItem}");
		UICommand.make(form, "update-youtube", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateYoutube}");
		UICommand.make(form, "cancel-youtube", messageLocator.getMessage("simplepage.cancel"), null);
		
		if(currentPage.getOwner() == null) {
			UIOutput.make(form, "editgroups-youtube");
		}
	}

	private void createMovieDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "movie-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edititem_header")));

		UIForm form = UIForm.make(tofill, "movie-form");

		UIInput.make(form, "movie-height", "#{simplePageBean.height}");
		UIInput.make(form, "movie-width", "#{simplePageBean.width}");
		UIInput.make(form, "movieEditId", "#{simplePageBean.itemId}");
		UIOutput.make(form, "description3-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description3", "#{simplePageBean.description}");
		UIInput.make(form, "mimetype4", "#{simplePageBean.mimetype}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		UIInternalLink.make(form, "change-resource-movie", messageLocator.getMessage("simplepage.change_resource"), fileparams);

		UICommand.make(form, "delete-movie-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-movie", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateMovie}");
		UICommand.make(form, "movie-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createEditTitleDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		UIOutput.make(tofill, "edit-title-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editTitle")));

		UIForm form = UIForm.make(tofill, "title-form");
		UIOutput.make(form, "pageTitleLabel", messageLocator.getMessage("simplepage.pageTitle_label"));
		UIInput.make(form, "pageTitle", "#{simplePageBean.pageTitle}");

		if (pageItem.getPageId() == 0) {
			UIOutput.make(tofill, "hideContainer");
			UIBoundBoolean.make(form, "hide", "#{simplePageBean.hidePage}", (page.isHidden()));

			Date releaseDate = page.getReleaseDate();

			UIBoundBoolean.make(form, "page-releasedate", "#{simplePageBean.hasReleaseDate}", (releaseDate != null));

			if (releaseDate == null) {
				releaseDate = new Date();
			}
			simplePageBean.setReleaseDate(releaseDate);
			UIInput releaseForm = UIInput.make(form, "releaseDate:", "simplePageBean.releaseDate");
			dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
			dateevolver.evolveDateInput(releaseForm, page.getReleaseDate());

			UIBoundBoolean.make(form, "page-required", "#{simplePageBean.required}", (pageItem.isRequired()));
			UIBoundBoolean.make(form, "page-prerequisites", "#{simplePageBean.prerequisite}", (pageItem.isPrerequisite()));
		}

		UIOutput gradeBook = UIOutput.make(form, "gradebookDiv");
		if(page.getOwner() != null) gradeBook.decorate(new UIStyleDecorator("noDisplay"));
		
		UIOutput.make(form, "page-gradebook");
		Double points = page.getGradebookPoints();
		String pointString = "";
		if (points != null) {
			pointString = points.toString();
		}
		UIInput.make(form, "page-points", "#{simplePageBean.points}", pointString);

		UICommand.make(form, "create-title", messageLocator.getMessage("simplepage.save"), "#{simplePageBean.editTitle}");
		UICommand.make(form, "cancel-title", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

	private void createNewPageDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		UIOutput.make(tofill, "new-page-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-page")));

		UIForm form = UIForm.make(tofill, "new-page-form");

		UIInput.make(form, "newPage", "#{simplePageBean.newPageTitle}");

		UIInput.make(form, "new-page-number", "#{simplePageBean.numberOfPages}");

		UIBoundBoolean.make(form, "new-page-copy", "#{simplePageBean.copyPage}", false);

		UICommand.make(form, "new-page-submit", messageLocator.getMessage("simplepage.save"), "#{simplePageBean.addPages}");
		UICommand.make(form, "new-page-cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

	private void createRemovePageDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		UIOutput.make(tofill, "remove-page-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.remove-page")));

		UIForm form = UIForm.make(tofill, "remove-page-form");
		form.addParameter(new UIELBinding("#{simplePageBean.removeId}", page.getPageId()));
		//GeneralViewParameters params = new GeneralViewParameters(RemovePageProducer.VIEW_ID);
		//UIInternalLink.make(form, "remove-page-submit", "", params).decorate(new UIFreeAttributeDecorator("value", messageLocator.getMessage("simplepage.remove")));

		UICommand.make(form, "remove-page-submit", messageLocator.getMessage("simplepage.remove"), "#{simplePageBean.removePage}");
		UICommand.make(form, "remove-page-cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

	private void createCommentsDialog(UIContainer tofill) {
		UIOutput.make(tofill, "comments-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_commentslink")));

		UIForm form = UIForm.make(tofill, "comments-form");

		UIInput.make(form, "commentsEditId", "#{simplePageBean.itemId}");

		UIBoundBoolean.make(form, "comments-anonymous", "#{simplePageBean.anonymous}");

		UICommand.make(form, "delete-comments-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-comments", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateComments}");
		UICommand.make(form, "cancel-comments", messageLocator.getMessage("simplepage.cancel"), null);
	}
	
	private void createStudentContentDialog(UIContainer tofill) {
		UIOutput.make(tofill, "student-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_studentlink")));

		UIForm form = UIForm.make(tofill, "student-form");

		UIInput.make(form, "studentEditId", "#{simplePageBean.itemId}");

		UIBoundBoolean.make(form, "student-anonymous", "#{simplePageBean.anonymous}");
		UIBoundBoolean.make(form, "student-comments", "#{simplePageBean.comments}");
		UIBoundBoolean.make(form, "student-comments-anon", "#{simplePageBean.forcedAnon}");

		UICommand.make(form, "delete-student-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-student", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateStudent}");
		UICommand.make(form, "cancel-student", messageLocator.getMessage("simplepage.cancel"), null);
	}

	/*
	 * return true if the item is required and not completed, i.e. if we need to
	 * update the status after the user views the item
	 */
	private Status handleStatusImage(UIContainer container, SimplePageItem i) {
		if (i.getType() != SimplePageItem.TEXT && i.getType() != SimplePageItem.MULTIMEDIA) {
			if (!i.isRequired()) {
				addStatusImage(Status.NOT_REQUIRED, container, "status", i.getName());
				return Status.NOT_REQUIRED;
			} else if (simplePageBean.isItemComplete(i)) {
				addStatusImage(Status.COMPLETED, container, "status", i.getName());
				return Status.COMPLETED;
			} else {
				addStatusImage(Status.REQUIRED, container, "status", i.getName());
				return Status.REQUIRED;
			}
		}
		return Status.NOT_REQUIRED;
	}

	// add the checkmark or asterisk. This code supports a couple of other
	// statuses that we
	// never ended up using
	private void addStatusImage(Status status, UIContainer container, String imageId, String name) {
		String imagePath = "/sakai-lessonbuildertool-tool/images/";
		String imageAlt = "";

		// better not to include alt or title. Bundle them with the link. Avoids
		// complexity for screen reader

		if (status == Status.COMPLETED) {
			imagePath += "checkmark.png";
			imageAlt = ""; // messageLocator.getMessage("simplepage.status.completed")
			// + " " + name;
		} else if (status == Status.DISABLED) {
			imagePath += "unavailable.png";
			imageAlt = ""; // messageLocator.getMessage("simplepage.status.disabled")
			// + " " + name;
		} else if (status == Status.FAILED) {
			imagePath += "failed.png";
			imageAlt = ""; // messageLocator.getMessage("simplepage.status.failed")
			// + " " + name;
		} else if (status == Status.REQUIRED) {
			imagePath += "available.png";
			imageAlt = ""; // messageLocator.getMessage("simplepage.status.required")
			// + " " + name;
		} else if (status == Status.NOT_REQUIRED) {
			imagePath += "not-required.png";
			// it's a blank image, no need for screen readers to say anything
			imageAlt = ""; // messageLocator.getMessage("simplepage.status.notrequired");
		}

		UIOutput.make(container, "status-td");
		UIOutput.make(container, imageId).decorate(new UIFreeAttributeDecorator("src", imagePath))
				.decorate(new UIFreeAttributeDecorator("alt", imageAlt)).decorate(new UITooltipDecorator(imageAlt));
	}

	private String getLocalizedURL(String fileName) {

		if (fileName == null || fileName.trim().length() == 0)
			return fileName;
		else {
			fileName = fileName.trim();
		}

		Locale locale = new ResourceLoader().getLocale();

		String helploc = ServerConfigurationService.getString("lessonbuilder.helpfolder", null);

		// we need to test the localized URL and return the initial one if it
		// doesn't exists
		// defaultPath will be the one to use if the localized one doesn't exist
		String defaultPath = null;
		// this is the part up to where we add the locale
		String prefix = null;
		// this is the part after the locale
		String suffix = null;
		// this is an additional prefix needed to make a full URL, for testing
		String testPrefix = null;

		int suffixIndex = fileName.lastIndexOf(".");
		if (suffixIndex >= 0) {
			prefix = fileName.substring(0, suffixIndex);
			suffix = fileName.substring(suffixIndex);
		} else {
			prefix = fileName;
			suffix = "";
		}

		if (helploc != null) {
			// user has specified a base URL. Will be absolute, but may not have
			// http and hostname
			defaultPath = helploc + fileName;
			prefix = helploc + prefix;
			if (helploc.startsWith("http:") || helploc.startsWith("https:")) {
				testPrefix = ""; // absolute, can test as is
			} else {
				testPrefix = myUrl(); // relative, need to make absolute
			}
		} else {
			defaultPath = "/sakai-lessonbuildertool-tool/templates/instructions/" + fileName;
			prefix = "/sakai-lessonbuildertool-tool/templates/instructions/" + prefix;
			testPrefix = myUrl();
		}

		String[] localeDetails = locale.toString().split("_");
		int localeSize = localeDetails.length;
		String filePath = null;
		String localizedPath = null;

		if (localeSize > 2) {
			localizedPath = prefix + "_" + locale.toString() + suffix;
			filePath = testPrefix + localizedPath;
			if (UrlOk(filePath))
				return localizedPath;
		}

		if (localeSize > 1) {
			localizedPath = prefix + "_" + locale.getLanguage() + "_" + locale.getCountry() + suffix;
			filePath = testPrefix + localizedPath;
			if (UrlOk(filePath))
				return localizedPath;
		}

		if (localeSize > 0) {
			localizedPath = prefix + "_" + locale.getLanguage() + suffix;
			filePath = testPrefix + localizedPath;
			if (UrlOk(filePath))
				return localizedPath;
		}

		return defaultPath;

	}

	private boolean UrlOk(String url) {
		Boolean cached = (Boolean) urlCache.get(url);
		if (cached != null)
			return (boolean) cached;

		try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			boolean ret = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
			urlCache.put(url, (Boolean) ret);
			return ret;
		} catch (ProtocolException e) {
			urlCache.put(url, (Boolean) false);
			return false;
		} catch (IOException e) {
			urlCache.put(url, (Boolean) false);
			return false;
		}

	}

	private long findMostRecentComment() {
		List<SimplePageComment> comments = simplePageToolDao.findCommentsOnPageByAuthor(simplePageBean.getCurrentPage().getPageId(), UserDirectoryService.getCurrentUser().getId());

		Collections.sort(comments, new Comparator<SimplePageComment>() {
			public int compare(SimplePageComment c1, SimplePageComment c2) {
				return c1.getTimePosted().compareTo(c2.getTimePosted());
			}
		});

		if (comments.size() > 0)
			return comments.get(comments.size() - 1).getId();
		else
			return -1;
	}

}