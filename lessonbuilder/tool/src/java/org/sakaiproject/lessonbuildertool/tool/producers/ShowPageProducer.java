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
 *       http://www.opensource.org/licenses/ECL-2.0
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
import java.util.Set;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.Calendar;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.event.api.UsageSession;
import org.sakaiproject.event.cover.UsageSessionService;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswer;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponseTotals;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.SimpleChecklistItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.BltiInterface;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.GroupEntry;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.BltiTool;
import org.sakaiproject.lessonbuildertool.tool.evolvers.SakaiFCKTextEvolver;
import org.sakaiproject.lessonbuildertool.tool.view.CommentsGradingPaneViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.CommentsViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.QuestionGradingPaneViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.ExportCCViewParameters;
import org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.FormattedText;
import org.sakaiproject.util.Web;
import org.sakaiproject.portal.util.CSSUtils;

import uk.org.ponder.localeutil.LocaleGetter;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.builtin.UVBProducer;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInitBlock;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInputMany;
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
import org.apache.commons.lang.StringEscapeUtils;

/**
 * This produces the primary view of the page. It also handles the editing of
 * the properties of most of the items (through JQuery dialogs).
 * 
 * @author Eric Jeney <jeney@rutgers.edu>
 */
public class ShowPageProducer implements ViewComponentProducer, DefaultView, NavigationCaseReporter, ViewParamsReporter {
	private static Logger log = LoggerFactory.getLogger(ShowPageProducer.class);

	String reqStar = "<span class=\"reqStar\">*</span>";
	
	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	private AuthzGroupService authzGroupService;
	private SecurityService securityService;
	private SiteService siteService;
	private FormatAwareDateInputEvolver dateevolver;
	private TimeService timeService;
	private HttpServletRequest httpServletRequest;
	private HttpServletResponse httpServletResponse;
    // have to do it here because we need it in urlCache. It has to happen before Spring initialization
	private static MemoryService memoryService = (MemoryService)ComponentManager.get(MemoryService.class);
	private ToolManager toolManager;
	public TextInputEvolver richTextEvolver;
	private static LessonBuilderAccessService lessonBuilderAccessService;
	
	private Map<String,String> imageToMimeMap;
	public void setImageToMimeMap(Map<String,String> map) {
		this.imageToMimeMap = map;
	}
        public boolean useSakaiIcons = ServerConfigurationService.getBoolean("lessonbuilder.use-sakai-icons", false);
        public boolean allowSessionId = ServerConfigurationService.getBoolean("session.parameter.allow", false);
        public boolean allowCcExport = ServerConfigurationService.getBoolean("lessonbuilder.cc-export", true);
        public boolean allowDeleteOrphans = ServerConfigurationService.getBoolean("lessonbuilder.delete-orphans", false);
        public String portalTemplates = ServerConfigurationService.getString("portal.templates", "morpheus");


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
	private static final String DEFAULT_HTML_TYPES = "html,xhtml,htm,xht";
	private static String[] htmlTypes = null;
    // mp4 means it plays with the flash player if HTML5 doesn't work.
    // flv is also played with the flash player, but it doesn't get a backup <OBJECT> inside the player
    // Strobe claims to handle MOV files as well, but I feel safer passing them to quicktime, though that requires Quicktime installation
        private static final String DEFAULT_MP4_TYPES = "video/mp4,video/m4v,audio/mpeg,audio/mp3,video/x-m4v";
        private static String[] mp4Types = null;
        private static final String DEFAULT_HTML5_TYPES = "video/mp4,video/m4v,video/webm,video/ogg,audio/mpeg,audio/ogg,audio/wav,audio/x-wav,audio/webm,audio/ogg,audio/mp4,audio/aac,audio/mp3,video/x-m4v";
    // jw can also handle audio: audio/mp4,audio/mpeg,audio/ogg
        private static String[] html5Types = null;
	private static final String DEFAULT_WIDTH = "640px";
    // almost ISO. Full ISO isn't available until Java 7. this uses -0400 where ISO uses -04:00
	SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    // WARNING: this must occur after memoryService, for obvious reasons. 
    // I'm doing it this way because it doesn't appear that Spring can do this kind of initialization
    // and it's better to let Java's initialization code handle synchronization than do it ourselves in
    // an init method
	private static Cache urlCache = memoryService.newCache("org.sakaiproject.lessonbuildertool.tool.producers.ShowPageProducer.url.cache");
        String browserString = ""; // set by checkIEVersion;
    	public static int majorVersion = getMajorVersion();
        public static String fullVersion = getFullVersion();

	protected static final int DEFAULT_EXPIRATION = 10 * 60;

	public static int getMajorVersion() {

	    String sakaiVersion = ServerConfigurationService.getString("version.sakai", "11");

	    int major = 2;

		String majorString = "";

		// use - as separator to handle -SNAPSHOT, etc.
		String [] parts = sakaiVersion.split("[-.]");
		if (parts.length >= 1) {
		    majorString = parts[0];
		}

		try {
			major = Integer.parseInt(majorString);
		} catch (NumberFormatException nfe) {
			log.error(
				"Failed to parse Sakai version number. This may impact which versions of dependencies are loaded.");
		}

	    return major;

	}

	public static String getFullVersion() {

	    String sakaiVersion = ServerConfigurationService.getString("version.sakai", "12");

	    int i = sakaiVersion.indexOf("-"); // for -snapshot
	    if (i >= 0)
		sakaiVersion = sakaiVersion.substring(0, i);
	    
	    return sakaiVersion;
	}

	static final String ICONSTYLE = "\n.portletTitle .action .help img {\n        background: url({}/help.gif) center right no-repeat !important;\n}\n.portletTitle .action .help img:hover, .portletTitle .action .help img:focus {\n        background: url({}/help_h.gif) center right no-repeat\n}\n.portletTitle .title img {\n        background: url({}/reload.gif) center left no-repeat;\n}\n.portletTitle .title img:hover, .portletTitle .title img:focus {\n        background: url({}/reload_h.gif) center left no-repeat\n}\n";

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

	public static boolean definiteLength(Length l) {
	    if (l == null || l.number == null)
		return false;
	    if (l.unit.equals("") || l.unit.equals("px"))
		return true;
	    return false;
	}

	// created style arguments. This was done at the time when i thought
	// the OBJECT tag actually paid attention to the CSS size. it doesn't.
	public String getStyle(Length w, Length h) {
	    String ret = null;
	    if (lengthOk(w))
		ret = "width:" + w.getNew();
	    if (lengthOk(h)) {
		if (ret != null)
		    ret = ret + ";";
		ret = ret + "height:" + h.getNew();
	    }
	    return ret;
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
				log.error("exception in abbrevurl " + ignore);
			}
			;
		}

		return url;
	}

	public String myUrl() {
	    // previously we computed something, but this will give us the official one
	        return ServerConfigurationService.getServerUrl();
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

                UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localegetter.get().getLanguage()))
		    .decorate(new UIFreeAttributeDecorator("xml:lang", localegetter.get().getLanguage()));        

		UIOutput.make(tofill, "datepicker").decorate(new UIFreeAttributeDecorator("src", 
		  (majorVersion >= 10 ? "/library" : "/lessonbuilder-tool") + "/js/lang-datepicker/lang-datepicker.js"));

		UIOutput.make(tofill, "portletBody").decorate(new UIFreeAttributeDecorator("sakaimajor", Integer.toString(majorVersion)))
		    .decorate(new UIFreeAttributeDecorator("sakaiversion", fullVersion));

		boolean iframeJavascriptDone = false;
		
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
		// go to.  If path is "none", we don't want this page to be what we see
		// when we come back to the tool
		if (params.getSendingPage() != -1) {
			// will fail if page not in this site
			// security then depends upon making sure that we only deal with
			// this page
			try {
				simplePageBean.updatePageObject(params.getSendingPage(), !params.getPath().equals("none"));
			} catch (Exception e) {
				log.warn("ShowPage permission exception " + e);
				UIOutput.make(tofill, "error-div");
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
				return;
			}
		}
		
		boolean canEditPage = simplePageBean.canEditPage();
		boolean canReadPage = simplePageBean.canReadPage();
		boolean canSeeAll = simplePageBean.canSeeAll();  // always on if caneditpage
		
		boolean cameFromGradingPane = params.getPath().equals("none");

		TimeZone localtz = timeService.getLocalTimeZone();
		isoDateFormat.setTimeZone(localtz);

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

		// Find the MSIE version, if we're running it.
		int ieVersion = checkIEVersion();
		// as far as I can tell, none of these supports fck or ck
		// we can make it configurable if necessary, or use WURFL
		// however this test is consistent with CKeditor's check.
		// that desireable, since if CKeditor is going to use a bare
		// text block, we want to handle it as noEditor
		//   Update, Apr 7, 2016: CKeditor now works except for very old
		// browser versions. from my reading of the code, it works except
		// for IE < 7, Firefox < 5, Safari < 5.1. Sakai itself isn't supported
		// for those versions, so I'm not going to bother to test.
		//String userAgent = httpServletRequest.getHeader("User-Agent");
		//if (userAgent == null)
		//    userAgent = "";
		//boolean noEditor = userAgent.toLowerCase().indexOf("mobile") >= 0;
		boolean noEditor = false;

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

		if (htmlTypes == null) {
			String mmTypes = ServerConfigurationService.getString("lessonbuilder.html.types", DEFAULT_HTML_TYPES);
			htmlTypes = mmTypes.split(",");
			for (int i = 0; i < htmlTypes.length; i++) {
				htmlTypes[i] = htmlTypes[i].trim().toLowerCase();
			}
			Arrays.sort(htmlTypes);
		}

		if (mp4Types == null) {
			String m4Types = ServerConfigurationService.getString("lessonbuilder.mp4.types", DEFAULT_MP4_TYPES);
			mp4Types = m4Types.split(",");
			for (int i = 0; i < mp4Types.length; i++) {
				mp4Types[i] = mp4Types[i].trim().toLowerCase();
			}
			Arrays.sort(mp4Types);
		}

		if (html5Types == null) {
			String jTypes = ServerConfigurationService.getString("lessonbuilder.html5.types", DEFAULT_HTML5_TYPES);
			html5Types = jTypes.split(",");
			for (int i = 0; i < html5Types.length; i++) {
				html5Types[i] = html5Types[i].trim().toLowerCase();
			}
			Arrays.sort(html5Types);
		}

		// remember that page tool was reset, so we need to give user the option
		// of going to the last page from the previous session
		SimplePageToolDao.PageData lastPage = simplePageBean.toolWasReset();

		// if this page was copied from another site we may have to update links
		// can only do the fixups if you can write. We could hack permissions, but
		// I assume a site owner will access the site first
		if (canEditPage)
		    simplePageBean.maybeUpdateLinks();

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
		if (currentPage == null || pageItem == null || 
		    (pageItem.getType() != SimplePageItem.STUDENT_CONTENT &&Long.valueOf(pageItem.getSakaiId()) != currentPage.getPageId())) {
			log.warn("ShowPage item not in page");
			UIOutput.make(tofill, "error-div");
			if (currentPage == null)
			    // most likely tool was created by site info but no page
			    // has created. It will created the first time an item is created,
			    // so from a user point of view it looks like no item has been added
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.noitems_error_user"));
			else
			    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
			return;
		}

		// the reason for a seaprate release date test is so we can show the date.
		// there are currently some issues. If the page is not released and the user doesn't have
		// access because of groups, this will show the not released data. That's misleading because
		// when the release date comes the user still won't be able to see it. Not sure if it's worth
		// creating a separate function that just checks the groups. It's easy to test hidden, so I do that. The idea is that
		// if it's both hidden and not released it makes sense to show hidden.

		// check two parts of isitemvisible where we want to give specific errors
		// potentially need time zone for setting release date
		if (!canSeeAll && currentPage.getReleaseDate() != null && currentPage.getReleaseDate().after(new Date()) && !currentPage.isHidden()) {
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, M_locale);
			TimeZone tz = timeService.getLocalTimeZone();
			df.setTimeZone(tz);
			String releaseDate = df.format(currentPage.getReleaseDate());
			String releaseMessage = messageLocator.getMessage("simplepage.not_yet_available_releasedate").replace("{}", releaseDate);

			UIOutput.make(tofill, "error-div");
			UIOutput.make(tofill, "error", releaseMessage);

			return;
		}
		
		// the only thing not already tested (or tested in release check below) in isItemVisible is groups. In theory
		// no one should have a URL to a page for which they aren't in the group,
		// so I'm not trying to give a better message than just hidden
		if (!canSeeAll && currentPage.isHidden() || !simplePageBean.isItemVisible(pageItem)) {
		    UIOutput.make(tofill, "error-div");
		    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available_hidden"));
		    return;
		}



		// I believe we've now checked all the args for permissions issues. All
		// other item and
		// page references are generated here based on the contents of the page
		// and items.

		// needed to process path arguments first, so refresh page goes the right page
		if (simplePageBean.getTopRefresh()) {
		    UIOutput.make(tofill, "refresh");
		    return;    // but there's no point doing anything more
		}

		// error from previous operation
		// consumes the message, so don't do it if refreshing
		List<String> errMessages = simplePageBean.errMessages();
		if (errMessages != null) {
		    UIOutput.make(tofill, "error-div");
		    for (String e: errMessages) {
			UIBranchContainer er = UIBranchContainer.make(tofill, "errors:");
			UIOutput.make(er, "error-message", e);
		    }
		}


		if (canEditPage) {
		    // special instructor-only javascript setup.
		    // but not if we're refreshing
			UIOutput.make(tofill, "instructoronly");
			// Chome and IE will abort a page if some on it was input from
			// a previous submit. I.e. if an HTML editor was used. In theory they
			// only do this if part of it is Javascript, but in practice they do
			// it for images as well. The protection isn't worthwhile, since it only
			// protects the first time. Since it will reesult in a garbled page, 
			// people will just refresh the page, and then they'll get the new
			// contents. The Chrome guys refuse to fix this so it just applies to Javascript
			httpServletResponse.setHeader("X-XSS-Protection", "0");
		}
		
		
		if (currentPage == null || pageItem == null) {
			UIOutput.make(tofill, "error-div");
			if (canEditPage) {
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.impossible1"));
			} else {
				UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
			}
			return;
		}
		
		// Set up customizable CSS
		ContentResource cssLink = simplePageBean.getCssForCurrentPage();
		if(cssLink != null) {
			UIOutput.make(tofill, "customCSS").decorate(new UIFreeAttributeDecorator("href", cssLink.getUrl()));
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
		String ownerName = null;
		if(pageItem.getType() != SimplePageItem.STUDENT_CONTENT) {
			title = pageItem.getName();
		}else {
			title = currentPage.getTitle();
			if(!pageItem.isAnonymous() || canEditPage) {
			    try {
				String owner = currentPage.getOwner();
				String group = currentPage.getGroup();
				if (group != null)
				    ownerName = simplePageBean.getCurrentSite().getGroup(group).getTitle();
				else
				    ownerName = UserDirectoryService.getUser(owner).getDisplayName();
				
			    } catch (Exception ignore) {};
			    if (ownerName != null && !ownerName.equals(title))
				title += " (" + ownerName + ")";
			}
		}
		
		String newPath = null;
		
		// If the path is "none", then we don't want to record this page as being viewed, or set a path
		if(!params.getPath().equals("none")) {
			newPath = simplePageBean.adjustPath(params.getPath(), currentPage.getPageId(), pageItem.getId(), title);
			simplePageBean.adjustBackPath(params.getBackPath(), currentPage.getPageId(), pageItem.getId(), pageItem.getName());
		}
		
		// put out link to index of pages
		GeneralViewParameters showAll = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
		showAll.setSource("summary");
		UIInternalLink.make(tofill, "print-view", showAll)
		    .decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.print_view")));
		UIInternalLink.make(tofill, "show-pages", showAll)
		    .decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.showallpages")));
		
		if (canEditPage) {
			// show tool bar, but not if coming from grading pane
			if(!cameFromGradingPane) {
				createToolBar(tofill, currentPage, (pageItem.getType() == SimplePageItem.STUDENT_CONTENT));
			}
			
			UIOutput.make(tofill, "title-descrip");
			String label = null;
			if (pageItem.getType() == SimplePageItem.STUDENT_CONTENT)
			    label = messageLocator.getMessage("simplepage.editTitle");
			else
			    label = messageLocator.getMessage("simplepage.title");
			String descrip = null;
			if (pageItem.getType() == SimplePageItem.STUDENT_CONTENT)
			    descrip = messageLocator.getMessage("simplepage.title-student-descrip");
			else if (pageItem.getPageId() == 0)
			    descrip = messageLocator.getMessage("simplepage.title-top-descrip");
			else
			    descrip = messageLocator.getMessage("simplepage.title-descrip");

			UIOutput.make(tofill, "edit-title").decorate(new UIFreeAttributeDecorator("title", descrip));
			UIOutput.make(tofill, "edit-title-text", label);
			UIOutput.make(tofill, "title-descrip-text", descrip);

			if (pageItem.getPageId() == 0 && currentPage.getOwner() == null) { // top level page
			    // need dropdown 
				UIOutput.make(tofill, "dropdown");
				UIOutput.make(tofill, "moreDiv");
				UIOutput.make(tofill, "new-page").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-page-tooltip")));
				createToolBarLink(PermissionsHelperProducer.VIEW_ID, tofill, "permissions", "simplepage.permissions", currentPage, "simplepage.permissions.tooltip");
				UIOutput.make(tofill, "import-cc").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.import_cc.tooltip")));
				UIOutput.make(tofill, "export-cc").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.export_cc.tooltip")));

				// Check to see if we have tools registered for external import
				List<Map<String, Object>> toolsFileItem = simplePageBean.getToolsFileItem();
				if ( toolsFileItem.size() > 0 ) {
					UIOutput.make(tofill, "show-lti-import");
					UIForm ltiImport =  UIForm.make(tofill, "lti-import-form");
					makeCsrf(ltiImport, "csrf1");
					GeneralViewParameters ltiParams = new GeneralViewParameters();
					ltiParams.setSendingPage(currentPage.getPageId());
					ltiParams.viewID = LtiFileItemProducer.VIEW_ID;
					UILink link = UIInternalLink.make(tofill, "lti-import-link", messageLocator.getMessage("simplepage.import_lti_button"), ltiParams);
					link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.fileitem.tooltip")));
				}
			}
			
			// Checks to see that user can edit and that this is either a top level page,
			// or a top level student page (not a subpage to a student page)
			if(simplePageBean.getEditPrivs() == 0 && (pageItem.getPageId() == 0)) {
				UIOutput.make(tofill, "remove-li");
				UIOutput.make(tofill, "remove-page").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.remove-page-tooltip")));
				
				if (allowDeleteOrphans) {
				    UIOutput.make(tofill, "delete-orphan-li");
				    UIForm orphan =  UIForm.make(tofill, "delete-orphan-form");
				    makeCsrf(orphan, "csrf1");
				    UICommand.make(orphan, "delete-orphan", "#{simplePageBean.deleteOrphanPages}");
				    UIOutput.make(orphan, "delete-orphan-link").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.delete-orphan-pages-desc")));
				}

			} else if (simplePageBean.getEditPrivs() == 0 && currentPage.getOwner() != null) {
			    // getEditPrivs < 2 if we want to let the student delete. Currently we don't. There can be comments
			    // from other students and the page can be shared
				SimpleStudentPage studentPage = simplePageToolDao.findStudentPage(currentPage.getTopParent());
				if (studentPage != null && studentPage.getPageId() == currentPage.getPageId()) {
					UIOutput.make(tofill, "remove-student");
					UIOutput.make(tofill, "remove-page-student").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.remove-student-page-explanation")));
				}
			}

			UIOutput.make(tofill, "dialogDiv");
			UIOutput.make(tofill, "siteid", simplePageBean.getCurrentSiteId());
			UIOutput.make(tofill, "locale", M_locale.toString());

		} else if (!canReadPage) {
			return;
		} else if (!canSeeAll) {
			// see if there are any unsatisfied prerequisites
		        // if this isn't a top level page, this will check that the page above is
		        // accessible. That matters because we check visible, available and release
		        // only for this page but not for the containing page
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
					} else {
					    UIOutput.make(tofill, "error-div");
					    UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
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

		ToolSession toolSession = SessionManager.getCurrentToolSession();
		// this code is now for 11 only. helpurl is used in 9 and 10 to indicate neo portal
		// at this point only two code paths are intended to work. inline and iframe.
		// inline pushes stuff into the morpheus-generated header. iframe uses an extra line
		// the previous mode required us to try to duplicate the header generated by morpheus
		// this was too error-prone.
		String helpurl = null; /* (String)toolSession.getAttribute("sakai-portal:help-action"); */
		String reseturl = null; /* (String)toolSession.getAttribute("sakai-portal:reset-action"); */

		Placement placement = toolManager.getCurrentPlacement();
		String toolId = placement.getToolId();
		boolean inline = false;

		// inline includes iframes when morpheus is in effect
		if ("morpheus".equals(portalTemplates) && httpServletRequest.getRequestURI().startsWith("/portal/site/")) {
		    inline = true;
		}

		String skinName = null;
		String skinRepo = null;
		String iconBase = null;

		UIComponent titlediv = UIOutput.make(tofill, "titlediv");
		if (inline)
		    titlediv.decorate(new UIFreeAttributeDecorator("style", "display:none"));
		// we need to do special CSS for old portal
		else if (helpurl == null)
		    titlediv.decorate(new UIStyleDecorator("oldPortal"));		

		if (helpurl != null || reseturl != null) {
		    // these URLs are defined if we're in the neo portal
		    // in that case we need our own help and reset icons. We want
		    // to take them from the current skin, so find its prefix.
		    // unfortunately the neoportal tacks neo- on front of the skin
		    // name, so this is more complex than you might think.

		    skinRepo = ServerConfigurationService.getString("skin.repo", "/library/skin");
		    iconBase = skinRepo + "/" + CSSUtils.adjustCssSkinFolder(null) + "/images";

		    UIVerbatim.make(tofill, "iconstyle", ICONSTYLE.replace("{}", iconBase));

		}

		if (helpurl != null) {
		    UILink.make(tofill, (pageItem.getPageId() == 0 ? "helpbutton" : "helpbutton2"), helpurl).
			decorate(new UIFreeAttributeDecorator("onclick",
			         "openWindow('" + helpurl + "', 'Help', 'resizeable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false")).
			decorate(new UIFreeAttributeDecorator("title",
				 messageLocator.getMessage("simplepage.help-button")));
		    if (!inline)
		    UIOutput.make(tofill, (pageItem.getPageId() == 0 ? "helpimage" : "helpimage2")).
			decorate(new UIFreeAttributeDecorator("alt",
			         messageLocator.getMessage("simplepage.help-button")));
		    UIOutput.make(tofill, (pageItem.getPageId() == 0 ? "helpnewwindow" : "helpnewwindow2"), 
				  messageLocator.getMessage("simplepage.opens-in-new"));
		    UILink.make(tofill, "directurl").
			decorate(new UIFreeAttributeDecorator("rel", "#Main" + Web.escapeJavascript(placement.getId()) + "_directurl")).
			decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.direct-link")));
		    // if (inline) {
			UIOutput.make(tofill, "directurl-div").
			    decorate(new UIFreeAttributeDecorator("id", "Main" + Web.escapeJavascript(placement.getId()) + "_directurl"));
			// in general 2.9 doesn't have the url shortener
			if (majorVersion >= 10) {
			    UIOutput.make(tofill, "directurl-input").
				decorate(new UIFreeAttributeDecorator("onclick", "toggleShortUrlOutput('" + myUrl() + "/portal/directtool/" + placement.getId() + "/', this, 'Main" + Web.escapeJavascript(placement.getId()) + "_urlholder');"));
			    UIOutput.make(tofill, "directurl-shorten", messageLocator.getMessage("simplepage.short-url"));
			}
			UIOutput.make(tofill, "directurl-textarea", myUrl() + "/portal/directtool/" + placement.getId() + "/").
			    decorate(new UIFreeAttributeDecorator("class", "portlet title-tools Main" + Web.escapeJavascript(placement.getId()) + "_urlholder"));
			// } else
			UIOutput.make(tofill, "directimage").decorate(new UIFreeAttributeDecorator("alt",
				messageLocator.getMessage("simplepage.direct-link")));
		}

		// morpheus does reset as part of title
		if (reseturl != null && !inline) {
		    UILink.make(tofill, (pageItem.getPageId() == 0 ? "resetbutton" : "resetbutton2"), reseturl).
			decorate(new UIFreeAttributeDecorator("onclick",
				"location.href='" + reseturl + "'; return false")).
			decorate(new UIFreeAttributeDecorator("title",
			        messageLocator.getMessage("simplepage.reset-button")));
		    UIOutput.make(tofill, (pageItem.getPageId() == 0 ? "resetimage" : "resetimage2")).
			decorate(new UIFreeAttributeDecorator("alt",
			        messageLocator.getMessage("simplepage.reset-button")));
		}

		// note page accessed. the code checks to see whether all the required
		// items on it have been finished, and if so marks it complete, else just updates
		// access date save the path because if user goes to it later we want to restore the
		// breadcrumbs
		if(newPath != null) {
			if(pageItem.getType() != SimplePageItem.STUDENT_CONTENT) {
				simplePageBean.track(pageItem.getId(), newPath);
			}else {
				simplePageBean.track(pageItem.getId(), newPath, currentPage.getPageId());
			}
		}

		if(currentPage.getOwner() != null && simplePageBean.getEditPrivs() == 0) {
			SimpleStudentPage student = simplePageToolDao.findStudentPageByPageId(currentPage.getPageId());
			
			// Make sure this is a top level student page
			if(student != null && pageItem.getGradebookId() != null) {
				UIOutput.make(tofill, "gradingSpan");
				UIOutput.make(tofill, "commentsUUID", String.valueOf(student.getId()));
				UIOutput.make(tofill, "commentPoints", String.valueOf((student.getPoints() != null? student.getPoints() : "")));
				UIOutput pointsBox = UIOutput.make(tofill, "studentPointsBox");
				UIOutput.make(tofill, "topmaxpoints", String.valueOf((pageItem.getGradebookPoints() != null? pageItem.getGradebookPoints():"")));
				if (ownerName != null)
				    pointsBox.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.grade-for-student").replace("{}",ownerName)));
			
				List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(student.getItemId());
				
				Collections.sort(studentPages, new Comparator<SimpleStudentPage>() {
					public int compare(SimpleStudentPage o1, SimpleStudentPage o2) {
						String title1 = o1.getTitle();
						if (title1 == null)
							title1 = "";
						String title2 = o2.getTitle();
						if (title2 == null)
							title2 = "";
						return title1.compareTo(title2);
				    }
				});
				
				for(int in = 0; in < studentPages.size(); in++) {
					if(studentPages.get(in).isDeleted()) {
						studentPages.remove(in);
					}
				}
				
				int i = -1;
			
				for(int in = 0; in < studentPages.size(); in++) {
					if(student.getId() == studentPages.get(in).getId()) {
						i = in;
						break;
					}
				}
			
				if(i > 0) {
					GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, studentPages.get(i-1).getPageId());
					eParams.setItemId(studentPages.get(i-1).getItemId());
					eParams.setPath("next");
				
					UIInternalLink.make(tofill, "gradingBack", eParams);
				}
			
				if(i < studentPages.size() - 1) {
					GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, studentPages.get(i+1).getPageId());
					eParams.setItemId(studentPages.get(i+1).getItemId());
					eParams.setPath("next");
				
					UIInternalLink.make(tofill, "gradingForward", eParams);
				}
			
				printGradingForm(tofill);
			}
		}

		// breadcrumbs
		if (pageItem.getPageId() != 0) {
			// Not top-level, so we have to show breadcrumbs

			List<SimplePageBean.PathEntry> breadcrumbs = simplePageBean.getHierarchy();

			int index = 0;
			if (breadcrumbs.size() > 1 || reseturl != null || helpurl !=  null) {
				UIOutput.make(tofill, "crumbdiv");
				if (breadcrumbs.size() > 1)
				    for (SimplePageBean.PathEntry e : breadcrumbs) {
					// don't show current page. We already have a title. This
					// was too much
					UIBranchContainer crumb = UIBranchContainer.make(tofill, "crumb:");
					GeneralViewParameters view = new GeneralViewParameters(VIEW_ID);
					view.setSendingPage(e.pageId);
					view.setItemId(e.pageItemId);
					view.setPath(Integer.toString(index));
					UIComponent link = null;
					if (index < breadcrumbs.size() - 1) {
						// Not the last item
						link = UIInternalLink.make(crumb, "crumb-link", e.title, view);
						UIOutput.make(crumb, "crumb-separator");
					} else {
						UIOutput.make(crumb, "crumb-follow", e.title).decorate(new UIStyleDecorator("bold"));
					}
					index++;
				    }
				else {
				    UIBranchContainer crumb = UIBranchContainer.make(tofill, "crumb:");
				    UILink.make(crumb, "crum-link", currentPage.getTitle(), reseturl);
				}
			} else {
			    if (reseturl != null) {
				UIOutput.make(tofill, "pagetitletext", currentPage.getTitle());
			    } else if (true || !inline) {
				UIOutput.make(tofill, "pagetitle", currentPage.getTitle());
			    }
			}
		} else {
		    if (reseturl != null) {
			UILink.make(tofill, "pagetitlelink", reseturl);
			UIOutput.make(tofill, "pagetitletext", currentPage.getTitle());
		    } else if (true || !inline) {
			UIOutput.make(tofill, "pagetitle", currentPage.getTitle());
		    }
		}

		// see if there's a next item in sequence.
		simplePageBean.addPrevLink(tofill, pageItem);
		simplePageBean.addNextLink(tofill, pageItem);

		// swfObject is not currently used
		boolean shownSwfObject = false;

		long newItemId = -1L;
		String newItemStr = (String)toolSession.getAttribute("lessonbuilder.newitem");
		if (newItemStr != null) {
		    toolSession.removeAttribute("lessonbuilder.newitem");		    
		    try {
			newItemId = Long.parseLong(newItemStr);
		    } catch (Exception e) {}
		}

		// items to show
		List<SimplePageItem> itemList = (List<SimplePageItem>) simplePageBean.getItemsOnPage(currentPage.getPageId());
		
		// Move all items with sequence <= 0 to the end of the list.
		// Count is necessary to guarantee we don't infinite loop over a
		// list that only has items with sequence <= 0.
		// Becauses sequence number is < 0, these start out at the beginning
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

		boolean showDownloads = (simplePageBean.getCurrentSite().getProperties().getProperty("lessonbuilder-nodownloadlinks") == null);

		//
		//
		// MAIN list of items
		//
		// produce the main table

		// Is anything visible?
		// Note that we don't need to check whether any item is available, since the first visible
		// item is always available.
		boolean anyItemVisible = false;

		if (itemList.size() > 0) {
			UIBranchContainer container = UIBranchContainer.make(tofill, "itemContainer:");

			boolean showRefresh = false;
			boolean fisrt = false;
			int textboxcount = 1;

			int cols = 0;
			int colnum = 0;

			UIBranchContainer sectionWrapper = null;
			UIBranchContainer sectionContainer = null;
			UIBranchContainer columnContainer = null;
			UIBranchContainer tableContainer = null;

			boolean first = true;

			for (SimplePageItem i : itemList) {

				// break is not a normal item. handle it first
			        // this will work whether first item is break or not. Might be a section
			        // break or a normal item
				if (first || i.getType() == SimplePageItem.BREAK) {
				    boolean sectionbreak = false;
				    if (first || "section".equals(i.getFormat())) {
					sectionWrapper = UIBranchContainer.make(container, "sectionWrapper:");
					boolean collapsible = i.getAttribute("collapsible") != null && (!"0".equals(i.getAttribute("collapsible")));
					boolean defaultClosed = i.getAttribute("defaultClosed") != null && (!"0".equals(i.getAttribute("defaultClosed")));
					UIOutput sectionHeader = UIOutput.make(sectionWrapper, "sectionHeader");

					// only do this is there's an actual section break. Implicit ones don't have an item to hold the title
					String headerText = "";
					if ("section".equals(i.getFormat()) && i.getName() != null) {
					    headerText = i.getName();
					}
					UIOutput.make(sectionWrapper, "sectionHeaderText", headerText);
					UIOutput collapsedIcon = UIOutput.make(sectionWrapper, "sectionCollapsedIcon");
					sectionHeader.decorate(new UIStyleDecorator(headerText.equals("")? "skip" : ""));
					sectionContainer = UIBranchContainer.make(sectionWrapper, "section:");
					boolean needIcon = false;
					if (collapsible) {
						sectionHeader.decorate(new UIStyleDecorator("collapsibleSectionHeader"));
						sectionHeader.decorate(new UIFreeAttributeDecorator("aria-controls", sectionContainer.getFullID()));
						sectionHeader.decorate(new UIFreeAttributeDecorator("aria-expanded", (defaultClosed?"false":"true")));
						sectionContainer.decorate(new UIStyleDecorator("collapsible"));
						if (defaultClosed ) {
							sectionHeader.decorate(new UIStyleDecorator("closedSectionHeader"));
							sectionContainer.decorate(new UIStyleDecorator("defaultClosed"));
							needIcon = true;
						} else {
							sectionHeader.decorate(new UIStyleDecorator("openSectionHeader"));
						}
					}
					if (!needIcon)
					    collapsedIcon.decorate(new UIFreeAttributeDecorator("style", "display:none"));
					cols = colCount(itemList, i.getId());
					sectionbreak = true;
					colnum = 0;
				    } else if ("colunn".equals(i.getFormat()))
					colnum++;
				    columnContainer = UIBranchContainer.make(sectionContainer, "column:");				    
				    tableContainer = UIBranchContainer.make(columnContainer, "itemTable:");
				    Integer width = new Integer(i.getAttribute("colwidth") == null ? "1" : i.getAttribute("colwidth"));
				    Integer split = new Integer(i.getAttribute("colsplit") == null ? "1" : i.getAttribute("colsplit"));
				    colnum += width; // number after this column

				    String color = i.getAttribute("colcolor");

				    columnContainer.decorate(new UIStyleDecorator("cols" + cols + (colnum == cols?" lastcol":"") + (width > 1?" double":"") + (split > 1?" split":"") + (color == null?"":" col"+color)));
				    UIOutput.make(columnContainer, "break-msg", messageLocator.getMessage(sectionbreak?"simplepage.break-here":"simplepage.break-column-here"));

				    if (canEditPage) {
				    UIComponent delIcon = UIOutput.make(columnContainer, "section-td");
				    if (first)
					delIcon.decorate(new UIFreeAttributeDecorator("style", "display:none"));

				    UIOutput.make(columnContainer, "section2");
				    UIOutput.make(columnContainer, "section3").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.columnopen")));
				    UIOutput.make(columnContainer, "addbottom");
				    UIOutput.make(columnContainer, "addbottom2").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.add-item-column")));
				    UILink link = UILink.make(columnContainer, "section-del-link", (String)null, "/" + i.getId());
				    link.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.join-items")));
				    link.decorate(new UIStyleDecorator(sectionbreak?"section-merge-link":"column-merge-link"));
				    }

				    UIBranchContainer tableRow = UIBranchContainer.make(tableContainer, "item:");
				    tableRow.decorate(new UIFreeAttributeDecorator("class", "break" + i.getFormat()));
				    if (canEditPage) {
					// usual case is this is a break
					if (i.getType() == SimplePageItem.BREAK)
					    UIOutput.make(tableRow, "itemid", String.valueOf(i.getId()));
					else {
					    // page doesn't start with a break. have to use pageid
					    UIOutput.make(tableRow, "itemid", "p" + currentPage.getPageId());
					}
				    }

				    first = false;
				    if (i.getType() == SimplePageItem.BREAK)
				    continue;
				    // for first item, if wasn't break, process it
				}

				// listitem is mostly historical. it uses some shared HTML, but
				// if I were
				// doing it from scratch I wouldn't make this distinction. At
				// the moment it's
				// everything that isn't inline.

				boolean listItem = !(i.getType() == SimplePageItem.TEXT || i.getType() == SimplePageItem.MULTIMEDIA
						|| i.getType() == SimplePageItem.COMMENTS || i.getType() == SimplePageItem.STUDENT_CONTENT
						|| i.getType() == SimplePageItem.QUESTION || i.getType() == SimplePageItem.PEEREVAL
						|| i.getType() == SimplePageItem.CHECKLIST
					        || i.getType() == SimplePageItem.BREAK);
				// (i.getType() == SimplePageItem.PAGE &&
				// "button".equals(i.getFormat())))

				if (!simplePageBean.isItemVisible(i, currentPage)) {
					continue;
				}
				// break isn't a real item. probably don't want to count it
				if (i.getType() != SimplePageItem.BREAK)
				    anyItemVisible = true;

				UIBranchContainer tableRow = UIBranchContainer.make(tableContainer, "item:");

				// set class name showing what the type is, so people can do funky CSS

				String itemClassName = null;

				switch (i.getType()) {
				case SimplePageItem.RESOURCE: itemClassName = "resourceType"; break;
				case SimplePageItem.PAGE: itemClassName = "pageType"; break;
				case SimplePageItem.ASSIGNMENT: itemClassName = "assignmentType"; break;
				case SimplePageItem.ASSESSMENT: itemClassName = "assessmentType"; break;
				case SimplePageItem.TEXT: itemClassName = "textType"; break;
				case SimplePageItem.URL: itemClassName = "urlType"; break;
				case SimplePageItem.MULTIMEDIA: itemClassName = "multimediaType"; break;
				case SimplePageItem.FORUM: itemClassName = "forumType"; break;
				case SimplePageItem.COMMENTS: itemClassName = "commentsType"; break;
				case SimplePageItem.STUDENT_CONTENT: itemClassName = "studentContentType"; break;
				case SimplePageItem.QUESTION: itemClassName = "question"; break;
				case SimplePageItem.BLTI: itemClassName = "bltiType"; break;
				case SimplePageItem.PEEREVAL: itemClassName = "peereval"; break;
				case SimplePageItem.CHECKLIST: itemClassName = "checklistType"; break;
				}

				// inline LTI. Our code calls all BLTI items listItem, but the inline version really isn't
				boolean isInline = (i.getType() == SimplePageItem.BLTI && "inline".equals(i.getFormat()));

				if (listItem && !isInline){
				    itemClassName = itemClassName + " listType";
				}
				if (canEditPage) {
				    itemClassName = itemClassName + "  canEdit";
				}

				if (i.getId() == newItemId)
				    itemClassName = itemClassName + " newItem";

				tableRow.decorate(new UIFreeAttributeDecorator("class", itemClassName));

				if (canEditPage)
				    UIOutput.make(tableRow, "itemid", String.valueOf(i.getId()));

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

					UIOutput linktd = UIOutput.make(tableRow, "item-td");
					
					UIOutput contentCol = UIOutput.make(tableRow, "contentCol");
					// BLTI seems to require explicit specificaiton for column width. Otherwise
					// we get 300 px wide. Don't know why. Doesn't happen to other iframes
					if (isInline)
					    contentCol.decorate(new UIFreeAttributeDecorator("style", "width:100%"));

					UIBranchContainer linkdiv = null;
					if (!isInline) {
					    linkdiv = UIBranchContainer.make(tableRow, "link-div:");
					}
					if (!isInline && !navButton && !"button".equals(i.getFormat())) {
					    UIOutput itemicon = UIOutput.make(linkdiv,"item-icon");
					    switch (i.getType()) {
					    case SimplePageItem.FORUM:
						itemicon.decorate(new UIStyleDecorator("icon-sakai--sakai-forums"));
						break;
					    case SimplePageItem.ASSIGNMENT:
						itemicon.decorate(new UIStyleDecorator("icon-sakai--sakai-assignment-grades"));
						break;
					    case SimplePageItem.ASSESSMENT:
						itemicon.decorate(new UIStyleDecorator("icon-sakai--sakai-samigo"));
						break;
					    case SimplePageItem.BLTI:
						itemicon.decorate(new UIStyleDecorator("fa-globe"));
						break;
					    case SimplePageItem.PAGE:
						itemicon.decorate(new UIStyleDecorator("fa-folder-open-o"));
						break;
					    case SimplePageItem.RESOURCE:
						String mimeType = i.getHtml();

						if("application/octet-stream".equals(mimeType)) {
						    // OS X reports octet stream for things like MS Excel documents.
						    // Force a mimeType lookup so we get a decent icon.
						    mimeType = null;
						}

						if (mimeType == null || mimeType.equals("")) {
						    String s = i.getSakaiId();
						    int j = s.lastIndexOf(".");
						    if (j >= 0)
							s = s.substring(j+1);
						    mimeType = ContentTypeImageService.getContentType(s);
						    // log.info("type " + s + ">" + mimeType);
						}

						String src = null;
						//if (!useSakaiIcons)
						    src = imageToMimeMap.get(mimeType);
						if (src == null) {
						    src = "fa-file-o";
						    //String image = ContentTypeImageService.getContentTypeImage(mimeType);
						    // if (image != null)
						    //	src = "/library/image/" + image;
						}
						
						if(src != null) {
						    itemicon.decorate(new UIStyleDecorator(src));
						}
						break;
					    }
					}

					UIOutput descriptiondiv = null;

					// refresh isn't actually used anymore. We've changed the
					// way things are
					// done so the user never has to request a refresh.
					//   FYI: this actually puts in an IFRAME for inline BLTI items
					showRefresh = !makeLink(tableRow, "link", i, canSeeAll, currentPage, notDone, status) || showRefresh;
					UILink.make(tableRow, "copylink", i.getName(), "http://lessonbuilder.sakaiproject.org/" + i.getId() + "/").
					    decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.copylink2").replace("{}", i.getName())));

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
						linkdiv.decorate(new UIFreeAttributeDecorator("style", "float:none"));
					    }
					    // for accessibility
					    if (navButton) {
						linkdiv.decorate(new UIFreeAttributeDecorator("role", "navigation"));
					    }
					}

					styleItem(tableRow, linktd, contentCol, i, "indentLevel", "custom-css-class");

					// note that a lot of the info here is used by the
					// javascript that prepares
					// the jQuery dialogs
					String itemGroupString = null;
					boolean entityDeleted = false;
					boolean notPublished = false;
					if (canEditPage) {
						UIOutput.make(tableRow, "edit-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.generic").replace("{}", i.getName())));
						UILink.make(tableRow, "edit-link", (String)null, "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.generic").replace("{}", i.getName())));

						// the following information is displayed using <INPUT
						// type=hidden ...
						// it contains information needed to populate the "edit"
						// popup dialog
						UIOutput.make(tableRow, "prerequisite-info", String.valueOf(i.isPrerequisite()));

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
								assignment = assignmentEntity.getEntity(i.getSakaiId(), simplePageBean);
								if (assignment != null) {
									type = assignment.getTypeOfGrade();
									String editUrl = assignment.editItemUrl(simplePageBean);
									if (editUrl != null) {
										UIOutput.make(tableRow, "edit-url", editUrl);
									}
									itemGroupString = simplePageBean.getItemGroupString(i, assignment, true);
									UIOutput.make(tableRow, "item-groups", itemGroupString);
									if (!assignment.objectExists())
									    entityDeleted = true;
									else if (assignment.notPublished())
									    notPublished = true;
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
							LessonEntity quiz = quizEntity.getEntity(i.getSakaiId(),simplePageBean);
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
								if (!quiz.objectExists())
								    entityDeleted = true;

							} else
							    notPublished = quizEntity.notPublished(i.getSakaiId());
						} else if (i.getType() == SimplePageItem.BLTI) {
						    UIOutput.make(tableRow, "type", "b");
						    LessonEntity blti= (bltiEntity == null ? null : bltiEntity.getEntity(i.getSakaiId()));
						    if (blti != null) {
							String editUrl = blti.editItemUrl(simplePageBean);
							if (editUrl != null)
							    UIOutput.make(tableRow, "edit-url", editUrl);
							UIOutput.make(tableRow, "item-format", i.getFormat());

							if (i.getHeight() != null)
							    UIOutput.make(tableRow, "item-height", i.getHeight());
							itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							UIOutput.make(tableRow, "item-groups", itemGroupString );
							if (!blti.objectExists())
							    entityDeleted = true;
							else if (blti.notPublished())
							    notPublished = true;
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
								if (!forum.objectExists())
								    entityDeleted = true;
								else if (forum.notPublished())
								    notPublished = true;

							}
						} else if (i.getType() == SimplePageItem.PAGE) {
							UIOutput.make(tableRow, "type", "page");
							UIOutput.make(tableRow, "page-next", Boolean.toString(i.getNextPage()));
							UIOutput.make(tableRow, "page-button", Boolean.toString("button".equals(i.getFormat())));
							itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							UIOutput.make(tableRow, "item-groups", itemGroupString);
							SimplePage sPage = simplePageBean.getPage(Long.parseLong(i.getSakaiId()));
							Date rDate = sPage.getReleaseDate();
							String rDateString = "";
							if(rDate != null)
								rDateString = rDate.toString();
							UIOutput.make(tableRow, "subpagereleasedate", rDateString);
						} else if (i.getType() == SimplePageItem.RESOURCE) {
						        try {
							    itemGroupString = simplePageBean.getItemGroupStringOrErr(i, null, true);
							} catch (IdUnusedException e) {
							    itemGroupString = "";
							    entityDeleted = true;
							}
							if (simplePageBean.getInherited())
							    UIOutput.make(tableRow, "item-groups", "--inherited--");
							else
							    UIOutput.make(tableRow, "item-groups", itemGroupString );
							UIOutput.make(tableRow, "item-samewindow", Boolean.toString(i.isSameWindow()));

							UIVerbatim.make(tableRow, "item-path", getItemPath(i));
						}

					} // end of canEditPage

					if (canSeeAll) {
						// haven't set up itemgroupstring yet
						if (!canEditPage) {
						    if (!i.getSakaiId().equals(SimplePageItem.DUMMY)) {
							LessonEntity lessonEntity = null;
							switch (i.getType()) {
							case SimplePageItem.ASSIGNMENT:
							    lessonEntity = assignmentEntity.getEntity(i.getSakaiId(), simplePageBean);
							    if (lessonEntity != null)
								itemGroupString = simplePageBean.getItemGroupString(i, lessonEntity, true);
							    if (!lessonEntity.objectExists())
								entityDeleted = true;
							    else if (lessonEntity.notPublished())
								notPublished = true;
							    break;
							case SimplePageItem.ASSESSMENT:
							    lessonEntity = quizEntity.getEntity(i.getSakaiId(),simplePageBean);
							    if (lessonEntity != null)
								itemGroupString = simplePageBean.getItemGroupString(i, lessonEntity, true);
							    else 
								notPublished = quizEntity.notPublished(i.getSakaiId());
							    if (!lessonEntity.objectExists())
								entityDeleted = true;
							    break;
							case SimplePageItem.FORUM:
							    lessonEntity = forumEntity.getEntity(i.getSakaiId());
							    if (lessonEntity != null)
								itemGroupString = simplePageBean.getItemGroupString(i, lessonEntity, true);
							    if (!lessonEntity.objectExists())
								entityDeleted = true;
							    else if (lessonEntity.notPublished())
								notPublished = true;
							    break;
							case SimplePageItem.BLTI:
							    if (bltiEntity != null)
								lessonEntity = bltiEntity.getEntity(i.getSakaiId());
							    if (lessonEntity != null)
								itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							    if (!lessonEntity.objectExists())
								entityDeleted = true;
							    else if (lessonEntity.notPublished())
								notPublished = true;
							    break;
							case SimplePageItem.PAGE:
							    itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							    break;
							case SimplePageItem.RESOURCE:
							    try {
								itemGroupString = simplePageBean.getItemGroupStringOrErr(i, null, true);
							    } catch (IdUnusedException e) {
								itemGroupString = "";
								entityDeleted = true;
							    }
							    break;
							}
						    }
						}

						String releaseString = simplePageBean.getReleaseString(i, M_locale);
						if (itemGroupString != null || releaseString != null || entityDeleted || notPublished) {
							if (itemGroupString != null)
							    itemGroupString = simplePageBean.getItemGroupTitles(itemGroupString, i);
							if (itemGroupString != null) {
							    itemGroupString = " [" + itemGroupString + "]";
							    if (releaseString != null)
								itemGroupString = " " + releaseString + itemGroupString;
							} else if (releaseString != null)
							    itemGroupString = " " + releaseString;
							if (notPublished) {
							    if (itemGroupString != null)
								itemGroupString = itemGroupString + " " + 
								    messageLocator.getMessage("simplepage.not-published");
							    else
								itemGroupString = messageLocator.getMessage("simplepage.not-published");
							}
							if (entityDeleted) {
							    if (itemGroupString != null)
								itemGroupString = itemGroupString + " " + 
								    messageLocator.getMessage("simplepage.deleted-entity");
							    else
								itemGroupString = messageLocator.getMessage("simplepage.deleted-entity");
							}

							if (itemGroupString != null)
							    UIOutput.make(tableRow, (isInline ? "item-group-titles-div" : "item-group-titles"), itemGroupString);
						}

					} // end of canSeeAll

					// the following are for the inline item types. Multimedia
					// is the most complex because
					// it can be IMG, IFRAME, or OBJECT, and Youtube is treated
					// separately

				} else if (i.getType() == SimplePageItem.MULTIMEDIA) {
				    // This code should be read together with the code in SimplePageBean
				    // that sets up this data, method addMultimedia.  Most display is set
				    // up here, but note that show-page.js invokes the jquery oembed on all
				    // <A> items with class="oembed".

				    // historically this code was to display files ,and urls leading to things
				    // like MP4. as backup if we couldn't figure out what to do we'd put something
				    // in an iframe. The one exception is youtube, which we supposed explicitly.
				    //   However we now support several ways to embed content. We use the
				    // multimediaDisplayType code to indicate which. The codes are
				    // 	 1 -- embed code, 2 -- av type, 3 -- oembed, 4 -- iframe
				    // 2 is the original code: MP4, image, and as a special case youtube urls
				    // since we have old entries with no type code, and that behave the same as
				    // 2, we start by converting 2 to null.
				    //  then the logic is
				    //  if type == null & youtube, do youtube
				    //  if type == null & image, do iamge
				    //  if type == null & not HTML do MP4 or other player for file 
				    //  final fallthrough to handel the new types, with IFRAME if all else fails
				    // the old code creates ojbects in ContentHosting for both files and URLs.
				    // The new code saves the embed code or URL itself as an atteibute of the item
				    // If I were doing it again, I wouldn't create the ContebtHosting item
				    //   Note that IFRAME is only used for something where the far end claims the MIME
				    // type is HTML. For weird stuff like MS Word files I use the file display code, which
				    // will end up producing <OBJECT>.

					// the reason this code is complex is that we try to choose
					// the best
					// HTML for displaying the particular type of object. We've
					// added complexities
					// over time as we get more experience with different
					// object types and browsers.

				 	String itemGroupString = null;
					String itemGroupTitles = null;
					boolean entityDeleted = false;
					// new format explicit display indication
					String mmDisplayType = i.getAttribute("multimediaDisplayType");
					// 2 is the generic "use old display" so treat it as null
					if ("".equals(mmDisplayType) || "2".equals(mmDisplayType))
					    mmDisplayType = null;
					if (canSeeAll) {
					    try {
						itemGroupString = simplePageBean.getItemGroupStringOrErr(i, null, true);
					    } catch (IdUnusedException e) {
						itemGroupString = "";
						entityDeleted = true;
					    }
					    itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString, i);
					    if (entityDeleted) {
						if (itemGroupTitles != null)
						    itemGroupTitles = itemGroupTitles + " " + messageLocator.getMessage("simplepage.deleted-entity");
						else
						    itemGroupTitles = messageLocator.getMessage("simplepage.deleted-entity");
					    }
					    if (itemGroupTitles != null) {
						itemGroupTitles = "[" + itemGroupTitles + "]";
					    }
					    UIOutput.make(tableRow, "item-groups", itemGroupString);
					} else if (entityDeleted)
					    continue;
					
					if (!"1".equals(mmDisplayType) && !"3".equals(mmDisplayType))
					    UIVerbatim.make(tableRow, "item-path", getItemPath(i));

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

					String widthSt = i.getWidth();
					Length width = null;

					if (mmDisplayType == null && simplePageBean.isImageType(i)) {
						// a wide default for images would produce completely wrong effect
					    	if (widthSt != null && !widthSt.equals("")) 
						    width = new Length(widthSt);
					} else if (widthSt == null || widthSt.equals("")) {
						width = new Length(DEFAULT_WIDTH);
					} else {
						width = new Length(widthSt);
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
					if (mmDisplayType == null && simplePageBean.isImageType(i)) {

					    if(canSeeAll || simplePageBean.isItemAvailable(i)) {
						    UIOutput.make(tableRow, "imageSpan");

						    if (itemGroupString != null) {
							    UIOutput.make(tableRow, "item-group-titles3", itemGroupTitles);
							    UIOutput.make(tableRow, "item-groups3", itemGroupString);
						    }

						    String imageName = i.getAlt();
						    if (imageName == null || imageName.equals("")) {
							    imageName = abbrevUrl(i.getURL());
						    }

						    item = UIOutput.make(tableRow, "image").decorate(new UIFreeAttributeDecorator("src", i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner()))).decorate(new UIFreeAttributeDecorator("alt", imageName));
						    if (lengthOk(width)) {
							    item.decorate(new UIFreeAttributeDecorator("width", width.getOld()));
						    }
						
						    if(lengthOk(height)) {
							    item.decorate(new UIFreeAttributeDecorator("height", height.getOld()));
						    }
					    } else {
					        UIComponent notAvailableText = UIOutput.make(tableRow, "notAvailableText", messageLocator.getMessage("simplepage.multimediaItemUnavailable"));
						// Grey it out
						    notAvailableText.decorate(new UIStyleDecorator("disabled-text-item"));
					    }

						// stuff for the jquery dialog
						if (canEditPage) {
							UIOutput.make(tableRow, "imageHeight", getOrig(height));
							UIOutput.make(tableRow, "imageWidth", widthSt);  // original value from database
							UIOutput.make(tableRow, "mimetype2", mimeType);
							UIOutput.make(tableRow, "current-item-id4", Long.toString(i.getId()));
							UIOutput.make(tableRow, "item-prereq3", String.valueOf(i.isPrerequisite()));
							UIVerbatim.make(tableRow, "item-path3", getItemPath(i));
							UIOutput.make(tableRow, "editimage-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
							UILink.make(tableRow, "image-edit", (String)null, "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
						}
						
						UIOutput.make(tableRow, "description2", i.getDescription());

					} else if (mmDisplayType == null && (youtubeKey = simplePageBean.getYoutubeKey(i)) != null) {
						String youtubeUrl = SimplePageBean.getYoutubeUrlFromKey(youtubeKey);

						if(canSeeAll || simplePageBean.isItemAvailable(i)) {
						    UIOutput.make(tableRow, "youtubeSpan");

						    if (itemGroupString != null) {
							    UIOutput.make(tableRow, "item-group-titles4", itemGroupTitles);
							    UIOutput.make(tableRow, "item-groups4", itemGroupString);
						    }

						    // if width is blank or 100% scale the height

						    // <object style="height: 390px; width: 640px"><param
						    // name="movie"
						    // value="http://www.youtube.com/v/AKIC7OQqBrA?version=3"><param
						    // name="allowFullScreen" value="true"><param
						    // name="allowScriptAccess" value="always"><embed
						    // src="http://www.youtube.com/v/AKIC7OQqBrA?version=3"
						    // type="application/x-shockwave-flash"
						    // allowfullscreen="true" allowScriptAccess="always"
						    // width="640" height="390"></object>

						    item = UIOutput.make(tableRow, "youtubeIFrame");
						    // youtube seems ok with length and width
						    if(lengthOk(height)) {
							    item.decorate(new UIFreeAttributeDecorator("height", height.getOld()));
						    }
						
						    if(lengthOk(width)) {
							    item.decorate(new UIFreeAttributeDecorator("width", width.getOld()));
						    }
						
						    item.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.youtube_player")));
						    item.decorate(new UIFreeAttributeDecorator("src", youtubeUrl));
						} else {
						    UIComponent notAvailableText = UIOutput.make(tableRow, "notAvailableText", messageLocator.getMessage("simplepage.multimediaItemUnavailable"));
						    // Grey it out
						    notAvailableText.decorate(new UIStyleDecorator("disabled-text-item"));
						}

						if (canEditPage) {
							UIOutput.make(tableRow, "youtubeId", String.valueOf(i.getId()));
							UIOutput.make(tableRow, "currentYoutubeURL", youtubeUrl);
							UIOutput.make(tableRow, "currentYoutubeHeight", getOrig(height));
							UIOutput.make(tableRow, "currentYoutubeWidth", widthSt);
							UIOutput.make(tableRow, "current-item-id5", Long.toString(i.getId()));
							UIOutput.make(tableRow, "item-prereq4", String.valueOf(i.isPrerequisite()));
							UIVerbatim.make(tableRow, "item-path4", getItemPath(i));
							UIOutput.make(tableRow, "youtube-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.youtube")));
							UILink.make(tableRow, "youtube-edit", (String)null, "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.youtube")));
						}

						UIOutput.make(tableRow, "description4", i.getDescription());
						
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

					} else if (mmDisplayType == null && 
						   ((mimeType != null && !mimeType.equals("text/html") && !mimeType.equals("application/xhtml+xml")) ||
						    // ((mimeType != null && (mimeType.startsWith("audio/") || mimeType.startsWith("video/"))) || 
						    (mimeType == null && !(Arrays.binarySearch(htmlTypes, extension) >= 0)))) {

                        // except where explicit display is set,
			// this code is used for everything that isn't an image,
                        // Youtube, or HTML
			// This could be audio, video, flash, or something random like MS word.
                        // Random stuff will turn into an object.
                        // HTML is done with an IFRAME in the next "if" case
		        // The explicit display types are handled there as well

					    // in theory the things that fall through to iframe are
					    // html and random stuff without a defined mime type
					    // random stuff with mime type is displayed with object

						if (mimeType == null) {
						    mimeType = "";
                        }

                        String oMimeType = mimeType; // in case we change it for
                        // FLV or others

                        if (itemGroupString != null) {
                            UIOutput.make(tableRow, "item-group-titles5", itemGroupTitles);
                            UIOutput.make(tableRow, "item-groups5", itemGroupString);
                        }

			UIOutput.make(tableRow, "movieSpan");

                        if(canSeeAll || simplePageBean.isItemAvailable(i)) {

						    UIComponent item2;

                            String movieUrl = i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner());
                            // movieUrl = "https://heidelberg.rutgers.edu" + movieUrl;
                            // Safari doens't always pass cookies to plugins, so we have to pass the arg
                            // this requires session.parameter.allow=true in sakai.properties
                            // don't pass the arg unless that is set, since the whole point of defaulting
                            // off is to not expose the session id
                            String sessionParameter = getSessionParameter(movieUrl);
                            if (sessionParameter != null)
                                movieUrl = movieUrl + "?lb.session=" + sessionParameter;

			    UIComponent movieLink = UIOutput.make(tableRow, "movie-link-div");
			    if (showDownloads)
				UILink.make(tableRow, "movie-link-link", messageLocator.getMessage("simplepage.download_file"), movieUrl);

                            //	if (allowSessionId)
                            //  movieUrl = movieUrl + "?sakai.session=" + SessionManager.getCurrentSession().getId();
                            boolean useFlvPlayer = false;

                            // isMp4 means we try the flash player (if not HTML5)
                            // we also try the flash player for FLV but for mp4 we do an
                            // additional backup if flash fails, but that doesn't make sense for FLV
                            boolean isMp4 = Arrays.binarySearch(mp4Types, mimeType) >= 0;
                            boolean isHtml5 = Arrays.binarySearch(html5Types, mimeType) >= 0;
                            
                            // wrap whatever stuff we decide to put out in HTML5 if appropriate
                            // javascript is used to do the wrapping, because RSF can't really handle this
                            if (isHtml5) {
				// flag for javascript
                                boolean isAudio = mimeType.startsWith("audio/");
                                UIComponent h5video = UIOutput.make(tableRow, (isAudio? "h5audio" : "h5video"));
                                UIComponent h5source = UIOutput.make(tableRow, (isAudio? "h5asource" : "h5source"));
				// HTML5 spec says % isn't legal in width, so have to use style
				String style = null;
                                if (lengthOk(height))
				    style = "height: " + height.getNew();
                                if (lengthOk(width)) {
				    if (style == null)
					style = "";
				    else 
					style = style + ";";
				    style = style + "width: " + width.getNew();
				}
				if (style != null)    
				    h5video.decorate(new UIFreeAttributeDecorator("style", style));
                                h5source.decorate(new UIFreeAttributeDecorator("src", movieUrl)).
                                decorate(new UIFreeAttributeDecorator("type", mimeType));
				String caption = i.getAttribute("captionfile");
				if (!isAudio && caption != null && caption.length() > 0) {
				    movieLink.decorate(new UIStyleDecorator("has-caption allow-caption"));
				    String captionUrl = "/access/lessonbuilder/item/" + i.getId() + caption;
				    sessionParameter = getSessionParameter(captionUrl);
				    // sessionParameter should always be non-null
				    // because this overrides all other checks in /access/lessonbuilder,
				    // we haven't adjusted it to handle these files otherwise
				    if (sessionParameter != null)
					captionUrl = captionUrl + "?lb.session=" + sessionParameter;
				    UIOutput.make(tableRow, "h5track").
					decorate(new UIFreeAttributeDecorator("src", captionUrl));
				} else if (!isAudio) {
				    movieLink.decorate(new UIStyleDecorator("allow-caption"));
				}
                            }

                            // FLV is special. There's no player for flash video in
                            // the browser
                            // it shows with a special flash program, which I
                            // supply. For the moment MP4 is
                            // shown with the same player so it uses much of the
                            // same code
                            if (mimeType != null && (mimeType.equals("video/x-flv") || mimeType.equals("video/flv") || isMp4)) {
                                mimeType = "application/x-shockwave-flash";
                                movieUrl = "/lessonbuilder-tool/templates/StrobeMediaPlayback.swf";
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
                                //item2.decorate(new UIFreeAttributeDecorator("style", "border: 1px solid black"));
                            }

                            // some object types seem to need a specification, so supply our default if necessary
                            if (lengthOk(height) && lengthOk(width)) {
                                item2.decorate(new UIFreeAttributeDecorator("height", height.getOld())).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
                            } else if (definiteLength(width)) {
				// this is mostly because the default is 640 with no height specified
				// we've validated width, so no errors in conversion should occur
				Double h = new Double(width.getOld()) * 0.75;
                                if (oMimeType.startsWith("audio/"))
				    h = 100.0;
                                item2.decorate(new UIFreeAttributeDecorator("height", Double.toString(h))).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
				// flag for javascript to adjust height
				if (!oMimeType.startsWith("audio/"))
				    item2.decorate(new UIFreeAttributeDecorator("defaultsize","true"));
			    } else {				
                                if (oMimeType.startsWith("audio/"))
                                item2.decorate(new UIFreeAttributeDecorator("height", "100")).decorate(new UIFreeAttributeDecorator("width", "400"));
                                else
                                item2.decorate(new UIFreeAttributeDecorator("height", "300")).decorate(new UIFreeAttributeDecorator("width", "400"));
                            }
                            if (!useEmbed) {
                                if (useFlvPlayer) {
                                    UIOutput.make(tableRow, "flashvars").decorate(new UIFreeAttributeDecorator("value", "src=" + URLEncoder.encode(myUrl() + i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner()))));
                                    // need wmode=opaque for player to stack properly with dialogs, etc.
                                    // there is a performance impact, but I'm guessing in our application we don't 
                                    // need ultimate performance for embedded video. I'm setting it only for
                                    // the player, so flash games and other applications will still get wmode=window
                                    UIOutput.make(tableRow, "wmode");
                                } else if (mimeType.equals("application/x-shockwave-flash"))
                                    UIOutput.make(tableRow, "wmode");

                                UIOutput.make(tableRow, "movieURLInject").decorate(new UIFreeAttributeDecorator("value", movieUrl));
                                if (!isMp4 && showDownloads) {
                                    UIOutput.make(tableRow, "noplugin-p", messageLocator.getMessage("simplepage.noplugin"));
                                    UIOutput.make(tableRow, "noplugin-br");
                                    UILink.make(tableRow, "noplugin", i.getName(), movieUrl);
                                }
                            }

                            if (isMp4) {
                                // do fallback. for ie use EMBED
                                if (ieVersion > 0) {
                                    item2 = UIOutput.make(tableRow, "mp4-embed").decorate(new UIFreeAttributeDecorator("src", i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner()))).decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.mm_player").replace("{}", abbrevUrl(i.getURL()))));
                                } else {
                                    item2 = UIOutput.make(tableRow, "mp4-object").decorate(new UIFreeAttributeDecorator("data", i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner()))).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.mm_player").replace("{}", abbrevUrl(i.getURL()))));
                            }
                                if (oMimeType != null) {
                                    item2.decorate(new UIFreeAttributeDecorator("type", oMimeType));
                                }

                                // some object types seem to need a specification, so give a default if needed
                                if (lengthOk(height) && lengthOk(width)) {
                                    item2.decorate(new UIFreeAttributeDecorator("height", height.getOld())).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
				} else if (definiteLength(width)) {
				    // this is mostly because the default is 640 with no height specified
				    // we've validated width, so no errors in conversion should occur
				    Double h = new Double(width.getOld()) * 0.75;
				    if (oMimeType.startsWith("audio/"))
					h = 100.0;
				    item2.decorate(new UIFreeAttributeDecorator("height", Double.toString(h))).decorate(new UIFreeAttributeDecorator("width", width.getOld()));
				    // flag for javascript to adjust height
				    if (!oMimeType.startsWith("audio/"))
					item2.decorate(new UIFreeAttributeDecorator("defaultsize","true"));
                                } else {
                                    if (oMimeType.startsWith("audio/"))
                                    item2.decorate(new UIFreeAttributeDecorator("height", "100")).decorate(new UIFreeAttributeDecorator("width", "100%"));
                                    else
                                    item2.decorate(new UIFreeAttributeDecorator("height", "300")).decorate(new UIFreeAttributeDecorator("width", "100%"));
                                }

                                if (!useEmbed) {
                                    UIOutput.make(tableRow, "mp4-inject").decorate(new UIFreeAttributeDecorator("value", i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner())));

				    if (showDownloads) {
					UIOutput.make(tableRow, "mp4-noplugin-p", messageLocator.getMessage("simplepage.noplugin"));
					UILink.make(tableRow, "mp4-noplugin", i.getName(), i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner()));
				    }
                                }
                            }
			    UIOutput.make(tableRow, "description3", i.getDescription());
                        } else {
					        UIVerbatim notAvailableText = UIVerbatim.make(tableRow, "notAvailableText", messageLocator.getMessage("simplepage.multimediaItemUnavailable"));
                            // Grey it out
						    notAvailableText.decorate(new UIStyleDecorator("disabled-multimedia-item"));
                        }

						if (canEditPage) {
							UIOutput.make(tableRow, "movieId", String.valueOf(i.getId()));
							UIOutput.make(tableRow, "movieHeight", getOrig(height));
							UIOutput.make(tableRow, "movieWidth", widthSt);
							UIOutput.make(tableRow, "mimetype5", oMimeType);
							UIOutput.make(tableRow, "prerequisite", (i.isPrerequisite()) ? "true" : "false");
							UIOutput.make(tableRow, "current-item-id6", Long.toString(i.getId()));
							UIVerbatim.make(tableRow, "item-path5", getItemPath(i));
							UIOutput.make(tableRow, "movie-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
							UILink.make(tableRow, "edit-movie", (String)null, "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
						}
						
					} else {
					    // this is fallthrough for html or an explicit mm display type (i.e. embed code)
					    // odd types such as MS word will be handled by the AV code, and presented as <OBJECT>

					    if(canSeeAll || simplePageBean.isItemAvailable(i)) {
						    

						// definition of resizeiframe, at top of page
						if (!iframeJavascriptDone && getOrig(height).equals("auto")) {
							UIOutput.make(tofill, "iframeJavascript");
							iframeJavascriptDone = true;
						}

						UIOutput.make(tableRow, "iframeSpan");

						if (itemGroupString != null) {
							UIOutput.make(tableRow, "item-group-titles2", itemGroupTitles);
							UIOutput.make(tableRow, "item-groups2", itemGroupString);
						}
						String itemUrl = i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner());
						if ("1".equals(mmDisplayType)) {
						    // embed
						    item = UIVerbatim.make(tableRow, "mm-embed", i.getAttribute("multimediaEmbedCode"));
						    //String style = getStyle(width, height);
						    //if (style != null)
						    //item.decorate(new UIFreeAttributeDecorator("style", style));
						} else if ("3".equals(mmDisplayType)) {
						    item = UILink.make(tableRow, "mm-oembed", i.getAttribute("multimediaUrl"), i.getAttribute("multimediaUrl"));
						    if (lengthOk(width))
							item.decorate(new UIFreeAttributeDecorator("maxWidth", width.getOld()));
						    if (lengthOk(height))
							item.decorate(new UIFreeAttributeDecorator("maxHeight", height.getOld()));
						    // oembed
						} else  {
						    UIOutput.make(tableRow, "iframe-link-div");
						    UILink.make(tableRow, "iframe-link-link", messageLocator.getMessage("simplepage.open_new_window"), itemUrl);
						    item = UIOutput.make(tableRow, "iframe").decorate(new UIFreeAttributeDecorator("src", itemUrl));
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
						}
						item.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.web_content").replace("{}", abbrevUrl(i.getURL()))));

						if (canEditPage) {
							UIOutput.make(tableRow, "iframeHeight", getOrig(height));
							UIOutput.make(tableRow, "iframeWidth", widthSt);
							UIOutput.make(tableRow, "mimetype3", mimeType);
							UIOutput.make(tableRow, "item-prereq2", String.valueOf(i.isPrerequisite()));
							UIOutput.make(tableRow, "embedtype", mmDisplayType);
							UIOutput.make(tableRow, "current-item-id3", Long.toString(i.getId()));
							UIVerbatim.make(tableRow, "item-path2", getItemPath(i));
							UIOutput.make(tableRow, "editmm-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
							UILink.make(tableRow, "iframe-edit", (String)null, "").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.url").replace("{}", abbrevUrl(i.getURL()))));
						}
						
						UIOutput.make(tableRow, "description5", i.getDescription());
					    } else {

					        UIVerbatim notAvailableText = UIVerbatim.make(tableRow, "notAvailableText", messageLocator.getMessage("simplepage.multimediaItemUnavailable"));
                            // Grey it out
						notAvailableText.decorate(new UIStyleDecorator("disabled-multimedia-item"));
					    }

					}

					// end of multimedia object

				} else if (i.getType() == SimplePageItem.COMMENTS) {
					// Load later using AJAX and CommentsProducer

					UIOutput.make(tableRow, "commentsSpan");

					boolean isAvailable = simplePageBean.isItemAvailable(i);
					// faculty missing preqs get warning but still see the comments
					if (!isAvailable && canSeeAll)
					    UIOutput.make(tableRow, "missing-prereqs", messageLocator.getMessage("simplepage.fake-missing-prereqs"));

					// students get warning and not the content
					if (!isAvailable && !canSeeAll) {
					    UIOutput.make(tableRow, "missing-prereqs", messageLocator.getMessage("simplepage.missing-prereqs"));
					}else {
						UIOutput.make(tableRow, "commentsDiv");
						UIOutput.make(tableRow, "placementId", placement.getId());

					        // note: the URL will be rewritten in comments.js to look like
					        //  /lessonbuilder-tool/faces/Comments...
						CommentsViewParameters eParams = new CommentsViewParameters(CommentsProducer.VIEW_ID);
						eParams.itemId = i.getId();
						eParams.placementId = placement.getId();
						if (params.postedComment) {
							eParams.postedComment = postedCommentId;
						}
						eParams.siteId = simplePageBean.getCurrentSiteId();
						eParams.pageId = currentPage.getPageId();
						
						if(params.author != null && !params.author.equals("")) {
							eParams.author = params.author;
							eParams.showAllComments = true;
						}

						UIInternalLink.make(tableRow, "commentsLink", eParams);

						if (!addedCommentsScript) {
							UIOutput.make(tofill, "comments-script");
							UIOutput.make(tofill, "fckScript");
							addedCommentsScript = true;
							UIOutput.make(tofill, "delete-dialog");
						}
						
						// forced comments have to be edited on the main page
						if (canEditPage) {
							// Checks to make sure that the comments item isn't on a student page.
							// That it is graded.  And that we didn't just come from the grading pane.
							if(i.getPageId() > 0 && i.getGradebookId() != null && !cameFromGradingPane) {
								CommentsGradingPaneViewParameters gp = new CommentsGradingPaneViewParameters(CommentGradingPaneProducer.VIEW_ID);
								gp.placementId = toolManager.getCurrentPlacement().getId();
								gp.commentsItemId = i.getId();
								gp.pageId = currentPage.getPageId();
								gp.pageItemId = pageItem.getId();
								gp.siteId = simplePageBean.getCurrentSiteId();
								
								UIInternalLink.make(tableRow, "gradingPaneLink", gp)
								    .decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.show-grading-pane-comments")));
							}

							UIOutput.make(tableRow, "comments-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.comments")));
						
							if (i.getSequence() > 0) {
							    UILink.make(tableRow, "edit-comments", (String)null, "")
									.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.comments")));

							    UIOutput.make(tableRow, "commentsId", String.valueOf(i.getId()));
							    UIOutput.make(tableRow, "commentsAnon", String.valueOf(i.isAnonymous()));
							    UIOutput.make(tableRow, "commentsitem-required", String.valueOf(i.isRequired()));
							    UIOutput.make(tableRow, "commentsitem-prerequisite", String.valueOf(i.isPrerequisite()));
							    UIOutput.make(tableRow, "commentsGrade", String.valueOf(i.getGradebookId() != null));
							    UIOutput.make(tableRow, "commentsMaxPoints", String.valueOf(i.getGradebookPoints()));
							
							    String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
							    if (itemGroupString != null) {
							    	String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString, i);
							    	if (itemGroupTitles != null) {
							    		itemGroupTitles = "[" + itemGroupTitles + "]";
							    	}
							    	UIOutput.make(tableRow, "comments-groups", itemGroupString);
							    	UIOutput.make(tableRow, "item-group-titles6", itemGroupTitles);
							    }
							}
					    	
							// Allows AJAX posting of comment grades
					    	printGradingForm(tofill);
					    }

					    UIForm form = UIForm.make(tableRow, "comment-form");
					    makeCsrf(form, "csrf2");

					    UIInput.make(form, "comment-item-id", "#{simplePageBean.itemId}", String.valueOf(i.getId()));
					    UIInput.make(form, "comment-edit-id", "#{simplePageBean.editId}");

					    // usage * image is required and not done
					    if (i.isRequired() && !simplePageBean.isItemComplete(i))
						UIOutput.make(tableRow, "comment-required-image");

					    UIOutput.make(tableRow, "add-comment-link");
					    UIOutput.make(tableRow, "add-comment-text", messageLocator.getMessage("simplepage.add-comment"));
					    UIInput fckInput = UIInput.make(form, "comment-text-area-evolved:", "#{simplePageBean.formattedComment}");
					    fckInput.decorate(new UIFreeAttributeDecorator("height", "175"));
					    fckInput.decorate(new UIFreeAttributeDecorator("width", "800"));
					    fckInput.decorate(new UIStyleDecorator("evolved-box"));
					    fckInput.decorate(new UIFreeAttributeDecorator("aria-label", messageLocator.getMessage("simplepage.editor")));
					    fckInput.decorate(new UIFreeAttributeDecorator("role", "dialog"));

					    if (!noEditor) {
						fckInput.decorate(new UIStyleDecorator("using-editor"));  // javascript needs to know
						((SakaiFCKTextEvolver) richTextEvolver).evolveTextInput(fckInput, "" + commentsCount);
					    }
					    UICommand.make(form, "add-comment", "#{simplePageBean.addComment}");
					}
				
				}else if (i.getType() == SimplePageItem.PEEREVAL){
					
					String owner=currentPage.getOwner();
					String currentUser=UserDirectoryService.getCurrentUser().getId();
					Long pageId=currentPage.getPageId();
					
					boolean isOpen = false;
					boolean isPastDue = false;
					
					String peerEvalDateOpenStr = i.getAttribute("rubricOpenDate");
					String peerEvalDateDueStr  = i.getAttribute("rubricDueDate");
					boolean peerEvalAllowSelfGrade = Boolean.parseBoolean(i.getAttribute("rubricAllowSelfGrade"));
					boolean gradingSelf = owner.equals(currentUser) && peerEvalAllowSelfGrade;
				
					if (peerEvalDateOpenStr != null && peerEvalDateDueStr != null) {
						Date peerEvalNow = new Date();
						Date peerEvalOpen = new Date(Long.valueOf(peerEvalDateOpenStr));
						Date peerEvalDue = new Date(Long.valueOf(peerEvalDateDueStr));
						
						isOpen = peerEvalNow.after(peerEvalOpen);
						isPastDue = peerEvalNow.after(peerEvalDue);
					}
					
					if(isOpen){
						
					    // data needed to figure out what to show
					    // there are three cases:
					    // individual
					    // group where we evaluate the group
					    // group where we evaluate individuals
					    // for historical reasons when we evaluate the group the first person
					    // to create the group is shown as the owner.
					    
					        // construct row text -> row id
					        // old entries are by text, so need to be able to map them to id

					        Map<String, Long> catMap = new HashMap<String, Long>();

						List<Map> categories = (List<Map>) i.getJsonAttribute("rows");
						if (categories == null)   // not valid to do update on item without rubic
						    continue; 
						for (Map cat: categories) {
						    String rowText = String.valueOf(cat.get("rowText"));
						    String rowId = String.valueOf(cat.get("id"));
						    catMap.put(rowText, new Long(rowId));
						}

						List<String>groupMembers = simplePageBean.studentPageGroupMembers(i, null);

						boolean evalIndividual = (i.isGroupOwned() && "true".equals(i.getAttribute("group-eval-individual")));

						// if we should show form. 
						// individual owned
						// group owned and eval group
						// group owned and eval individual and we're in the group
						// i.e. not eval individual and we're outside group
						if(!(evalIndividual && !groupMembers.contains(currentUser))) {
						    UIOutput.make(tableRow, "peerReviewRubricStudent");
						    UIOutput.make(tableRow, "peer-eval-title-student", String.valueOf(i.getAttribute("rubricTitle")));
						    UIForm peerForm = UIForm.make(tableRow, "peer-review-form");
						    UIInput.make(peerForm, "peer-eval-itemid", "#{simplePageBean.itemId}", String.valueOf(i.getId()));
						    
						    // originally evalTargets was a list if ID's.
						    // but we need to sort by name, so unless we want to keep repeatedly
						    // going from ID to name, we need to use this:
						    class Target {
							String id;
							String name;
							String sort;
							Target(String i) {
							    name = i;
							    try {
								User u = UserDirectoryService.getUser(i);
								name = u.getDisplayName();
								sort = u.getSortName();
							    } catch (Exception ignore) {}
							    id = i;
							}
						    }

						    List<Target>evalTargets = new ArrayList<Target>();
						    if (evalIndividual) {
							String group = simplePageBean.getCurrentPage().getGroup();
							if (group != null)
							    group = "/site/" + simplePageBean.getCurrentSiteId() + "/group/" + group;
							try {
							    AuthzGroup g = authzGroupService.getAuthzGroup(group);
							    Set<Member> members = g.getMembers();
							    for (Member m: members) {
								evalTargets.add(new Target(m.getUserId()));
							    }
							} catch (Exception e) {
							    log.info("unable to get members of group " + group);
							}
							// no need to sort for other alternative, when there's only one
							Collections.sort(evalTargets, new Comparator<Target>() {
								public int compare(Target o1, Target o2) {
								    return o1.sort.compareTo(o2.sort);
								}
							    });
						    } else {
							Target target = new Target(owner);
							// individual handled above. So if group we're evaluating
							// the group. Use group name
							if (i.isGroupOwned()) {
							    String group = simplePageBean.getCurrentPage().getGroup();
							    target.name = simplePageBean.getCurrentSite().getGroup(group).getTitle();
							}
							evalTargets.add(target);
						    }

						    // for old format entries always need page owner or evaluee
						    // for new format when evaluating page need groupId
						    String groupId = null;
						    if (i.isGroupOwned() && !evalIndividual)
							groupId = simplePageBean.getCurrentPage().getGroup();

						    for (Target target: evalTargets) {
							UIContainer entry = UIBranchContainer.make(peerForm, "peer-eval-target:");
						    // for each target

							Map<Long, Map<Integer, Integer>> dataMap = new HashMap<Long, Map<Integer, Integer>>();
							// current data to show to target, all evaluations of target
							// But first see if we should show current data. Only show
							// user data evaluating him
							if ((i.isGroupOwned() && !evalIndividual && groupMembers.contains(currentUser)) ||
							    target.id.equals(currentUser)) {
							
							    List<SimplePagePeerEvalResult> evaluations = simplePageToolDao.findPeerEvalResultByOwner(pageId.longValue(), target.id, groupId);
							    
							    if(evaluations!=null) {
								for(SimplePagePeerEvalResult eval : evaluations) {
								    // for individual eval only show results for that one
									if (evalIndividual && !currentUser.equals(eval.getGradee()))
									    continue;
									Long rowId = eval.getRowId();
									if (rowId == 0L)
									    rowId = catMap.get(eval.getRowText());
									if (rowId == null)
									    continue; // don't recognize old-format entry

									Map<Integer, Integer> rowMap = dataMap.get(rowId);
									if (rowMap == null) {
									    rowMap = new HashMap<Integer, Integer>();
									    dataMap.put(rowId, rowMap);
									}
									Integer n = rowMap.get(eval.getColumnValue());
									if (n == null)
									    n = 1;
									else 
									    n++;
									rowMap.put(eval.getColumnValue(), n);
								}
							    }
							    
							}
							// end current data

							// now get current data to initiaize form. That's just
							// the submission by current user.
							List<SimplePagePeerEvalResult> evaluations = simplePageToolDao.findPeerEvalResult(pageId, currentUser, target.id, groupId);
							Map<Long,Integer> selectedCells = new HashMap<Long,Integer>();
							for (SimplePagePeerEvalResult result: evaluations) {
							    Long rowId = result.getRowId();
							    String rowText = result.getRowText();
							    if (rowId == 0L)
								rowId = catMap.get(rowText);
							    if (rowId == null)
								continue;
							    selectedCells.put(rowId, new Integer(result.getColumnValue()));
							}

							// for each student being evaluated
							UIOutput.make(entry, "peer-eval-target-name", target.name);
							UIOutput.make(entry, "peer-eval-target-id", target.id);

							// keep this is sync with canSubmit in SimplePageBean.savePeerEvalResult
							boolean canSubmit = (!i.isGroupOwned() && (!owner.equals(currentUser) || gradingSelf) ||
									     i.isGroupOwned() && !evalIndividual && (!groupMembers.contains(currentUser) || peerEvalAllowSelfGrade) ||
									     evalIndividual && groupMembers.contains(currentUser) && (peerEvalAllowSelfGrade || !target.id.equals(currentUser)));

							makePeerRubric(entry, i, makeStudentRubric, selectedCells, 
								       dataMap, canSubmit);

						    }

						    // can submit
						    // individual and (not that individual or gradingeself)
						    // group and (not in group or gradingself)
						    // group individual eval and in group
						    if(!i.isGroupOwned() && (!owner.equals(currentUser) || gradingSelf) ||
						       i.isGroupOwned() && !evalIndividual && (!groupMembers.contains(currentUser) || peerEvalAllowSelfGrade) ||
						       evalIndividual && groupMembers.contains(currentUser)) {

							// can actually submit

							if(isPastDue) {
							    UIOutput.make(tableRow, "peer-eval-grade-directions", messageLocator.getMessage("simplepage.peer-eval.past-due-date"));
							} else {
							    makeCsrf(peerForm, "csrf6");
							    UICommand.make(peerForm, "save-peereval-link",  messageLocator.getMessage("simplepage.submit"), "#{simplePageBean.savePeerEvalResult}");
							    UIOutput.make(peerForm, "save-peereval-text", messageLocator.getMessage("simplepage.save"));
							    UIOutput.make(peerForm, "cancel-peereval-link");
							    UIOutput.make(peerForm, "cancel-peereval-text", messageLocator.getMessage("simplepage.cancel"));
							
							    UIOutput.make(tableRow, "peer-eval-grade-directions", messageLocator.getMessage("simplepage.peer-eval.click-on-cell"));
							} 

						    // in theory the only case where we show the form and can't grade
						    // is if it's for yourself.
						    } else {
							UIOutput.make(tableRow, "peer-eval-grade-directions", messageLocator.getMessage("simplepage.peer-eval.cant-eval-yourself"));
						    }
						//buttons
						UIOutput.make(tableRow, "add-peereval-link");
						UIOutput.make(tableRow, "add-peereval-text", messageLocator.getMessage("simplepage.view-peereval"));
						
						}
						if(canEditPage)
							UIOutput.make(tableRow, "peerReviewRubricStudent-edit");//lines up rubric with edit btn column for users with editing privs
					}
				}else if(i.getType() == SimplePageItem.STUDENT_CONTENT) {
					
					UIOutput.make(tableRow, "studentSpan");

					boolean isAvailable = simplePageBean.isItemAvailable(i);
					// faculty missing preqs get warning but still see the comments
					if (!isAvailable && canSeeAll)
					    UIOutput.make(tableRow, "student-missing-prereqs", messageLocator.getMessage("simplepage.student-fake-missing-prereqs"));
					if (!isAvailable && !canSeeAll)
					    UIOutput.make(tableRow, "student-missing-prereqs", messageLocator.getMessage("simplepage.student-missing-prereqs"));
					else {
						boolean isGrader = simplePageBean.getEditPrivs() == 0;

						UIOutput.make(tableRow, "studentDiv");
						
						HashMap<Long, SimplePageLogEntry> cache = simplePageBean.cacheStudentPageLogEntries(i.getId());
						List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(i.getId());

						// notSubmitted will be list of students or groups that didn't submit. Start with those who
						// should submit and remove as we see them
						Set<String> notSubmitted = new HashSet<String>();
						if (i.isGroupOwned()) {
						    String ownerGroups = i.getOwnerGroups();
						    if (ownerGroups != null)
							notSubmitted = new HashSet(Arrays.asList(ownerGroups.split(",")));
						} else {
						    String siteRef = simplePageBean.getCurrentSite().getReference();
						    // only check students
						    List<User> studentUsers = securityService.unlockUsers("section.role.student", siteRef);
						    for (User u: studentUsers)
							notSubmitted.add(u.getId());
						}
					
						boolean hasOwnPage = false;
						String userId = UserDirectoryService.getCurrentUser().getId();
						
					    Collections.sort(studentPages, new Comparator<SimpleStudentPage>() {
						    public int compare(SimpleStudentPage o1, SimpleStudentPage o2) {
							String title1 = o1.getTitle();
							if (title1 == null)
							    title1 = "";
							String title2 = o2.getTitle();
							if (title2 == null)
							    title2 = "";
							return title1.compareTo(title2);
						    }
						});					    

					        UIOutput contentList = UIOutput.make(tableRow, "studentContentTable");
					        UIOutput contentTitle = UIOutput.make(tableRow, "studentContentTitle", messageLocator.getMessage("simplepage.student"));
						contentList.decorate(new UIFreeAttributeDecorator("aria-labelledby", contentTitle.getFullID()));
						boolean seeOnlyOwn = ("true".equals(i.getAttribute("see-only-own")));
						// Print each row in the table
						for(SimpleStudentPage page : studentPages) {
							if(page.isDeleted()) continue;

							// if seeOnlyOwn, skip other entries
							if (seeOnlyOwn && !canSeeAll) {
							    List<String>groupMembers = simplePageBean.studentPageGroupMembers(i, page.getGroup());
							    String currentUser = UserDirectoryService.getCurrentUser().getId();
							    if (!i.isGroupOwned() && !page.getOwner().equals(currentUser) ||
								i.isGroupOwned() && !groupMembers.contains(currentUser))
								continue;
							}

							// remove this from notSubmitted
							if (i.isGroupOwned()) {
							    String pageGroup = page.getGroup();
							    if (pageGroup != null)
								notSubmitted.remove(pageGroup);
							} else {
							    notSubmitted.remove(page.getOwner());
							}
							    
							SimplePageLogEntry entry = cache.get(page.getPageId());
							UIBranchContainer row = UIBranchContainer.make(tableRow, "studentRow:");
							
							// There's content they haven't seen
							if(entry == null || entry.getLastViewed().compareTo(page.getLastUpdated()) < 0) {
							    UIOutput.make(row, "newContentImg").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-student-content")));
							} else
							    UIOutput.make(row, "newContentImgT");
 
							// The comments tool exists, so we might have to show the icon
							if(i.getShowComments() != null && i.getShowComments()) {
 						
							    // New comments have been added since they last viewed the page
							    if(page.getLastCommentChange() != null && (entry == null || entry.getLastViewed().compareTo(page.getLastCommentChange()) < 0)) {
								UIOutput.make(row, "newCommentsImg").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-student-comments")));
							    } else
								UIOutput.make(row, "newCommentsImgT");							
							}
 
							// Never visited page
							if(entry == null) {
							    UIOutput.make(row, "newPageImg").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-student-content-page")));
							} else
							    UIOutput.make(row, "newPageImgT");

							GeneralViewParameters eParams = new GeneralViewParameters(ShowPageProducer.VIEW_ID, page.getPageId());
							eParams.setItemId(i.getId());
							eParams.setPath("push");
							
							String studentTitle = page.getTitle();
						
							String sownerName = null;
							try {
								if(!i.isAnonymous() || canEditPage) {
									if (page.getGroup() != null)
									    sownerName = simplePageBean.getCurrentSite().getGroup(page.getGroup()).getTitle();
									else
									    sownerName = UserDirectoryService.getUser(page.getOwner()).getDisplayName();
									if (sownerName != null && sownerName.equals(studentTitle))
									    studentTitle = "(" + sownerName + ")";
									else
									    studentTitle += " (" + sownerName + ")";
								}else if (simplePageBean.isPageOwner(page)) {
									studentTitle += " (" + messageLocator.getMessage("simplepage.comment-you") + ")";
								}
							} catch (UserNotDefinedException e) {
							}
							
							UIInternalLink.make(row, "studentLink", studentTitle, eParams);
						
							if(simplePageBean.isPageOwner(page)) {
								hasOwnPage = true;
							}
							
							if(i.getGradebookId() != null && simplePageBean.getEditPrivs() == 0) {
								UIOutput.make(row, "studentGradingCell", String.valueOf((page.getPoints() != null? page.getPoints() : "")));
							}
						}
					
						// if grader, show people who didn't submit
						if (simplePageBean.getEditPrivs() == 0) {
						    if (notSubmitted.size() > 0) {
							UIBranchContainer row = UIBranchContainer.make(tableRow, "studentRow:");
							UIOutput.make(row, "missingStudentTitle", messageLocator.getMessage("simplepage.missing-students"));
						    }
						    List<String> missingUsers = new ArrayList<String>();
						    for(String owner: notSubmitted) {
							String sownerName;
							if (i.isGroupOwned()) {
							    sownerName = simplePageBean.getCurrentSite().getGroup(owner).getTitle();
							} else {
							    try {
								sownerName = UserDirectoryService.getUser(owner).getDisplayName();
							    } catch (Exception e) {
								// can't find user, just show userid. Not very useful, but at least shows
								// what happened
								sownerName = owner;
							    }
							}
							missingUsers.add(sownerName);
						    }
						    Collections.sort(missingUsers);
						    for(String owner: missingUsers) {
							UIBranchContainer row = UIBranchContainer.make(tableRow, "studentRow:");
							UIOutput.make(row, "missingStudent", owner);
						    }
						    if (notSubmitted.size() > 0 && i.getGradebookId() != null) {
							UIBranchContainer row = UIBranchContainer.make(tableRow, "studentRow:");
							UIOutput zeroRow = UIOutput.make(row, "student-zero-div");
							UIForm zeroForm = UIForm.make(row, "student-zero-form");
							makeCsrf(zeroForm, "student-zero-csrf");
							UIInput.make(zeroForm, "student-zero-item", "#{simplePageBean.itemId}", String.valueOf(i.getId()));
							UICommand.make(zeroForm, "student-zero", messageLocator.getMessage("simplepage.zero-missing"), "#{simplePageBean.missingStudentSetZero}");
						    }
						}

						if(!hasOwnPage && simplePageBean.myStudentPageGroupsOk(i)) {
							UIBranchContainer row = UIBranchContainer.make(tableRow, "studentRow:");
							UIOutput.make(row, "linkRow");
							UIOutput.make(row, "linkCell");
							
							if (i.isRequired() && !simplePageBean.isItemComplete(i))
								UIOutput.make(row, "student-required-image");

							UIForm studentForm = UIForm.make(row, "add-content-form");
							makeCsrf(studentForm, "csrf27");
							UIInput.make(studentForm, "add-content-itemId", "#{simplePageBean.itemId}", "" + i.getId());
							UICommand.make(studentForm, "add-content", messageLocator.getMessage("simplepage.add-page"), "#{simplePageBean.createStudentPage}");;
						}
					
						String itemGroupString = null;
						// do before canEditAll because we need itemGroupString in it
						if (canSeeAll) {
						    itemGroupString = simplePageBean.getItemGroupString(i, null, true);
						    if (itemGroupString != null) {
							String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString, i);
							if (itemGroupTitles != null) {
							    itemGroupTitles = "[" + itemGroupTitles + "]";
							}
							UIOutput.make(tableRow, "item-group-titles7", itemGroupTitles);
						    }
						}

						if(canEditPage) {
							// Checks to make sure that the comments are graded and that we didn't
							// just come from a grading pane (would be confusing)
							if(i.getAltGradebook() != null && !cameFromGradingPane) {
								CommentsGradingPaneViewParameters gp = new CommentsGradingPaneViewParameters(CommentGradingPaneProducer.VIEW_ID);
								gp.placementId = toolManager.getCurrentPlacement().getId();
								gp.commentsItemId = i.getId();
								gp.pageId = currentPage.getPageId(); 
								gp.pageItemId = pageItem.getId();
								gp.studentContentItem = true;
							
								UIInternalLink.make(tableRow, "studentGradingPaneLink", gp)
								    .decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.show-grading-pane-content")));
							}
							
							UIOutput.make(tableRow, "student-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.student")));
							UILink.make(tableRow, "edit-student", (String)null, "")
									.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.student")));
							
							UIOutput.make(tableRow, "studentId", String.valueOf(i.getId()));
							UIOutput.make(tableRow, "studentAnon", String.valueOf(i.isAnonymous()));
							UIOutput.make(tableRow, "studentComments", String.valueOf(i.getShowComments()));
							UIOutput.make(tableRow, "forcedAnon", String.valueOf(i.getForcedCommentsAnonymous()));
							UIOutput.make(tableRow, "studentGrade", String.valueOf(i.getGradebookId() != null));
							UIOutput.make(tableRow, "studentMaxPoints", String.valueOf(i.getGradebookPoints()));
							UIOutput.make(tableRow, "studentGrade2", String.valueOf(i.getAltGradebook() != null));
							UIOutput.make(tableRow, "studentMaxPoints2", String.valueOf(i.getAltPoints()));
							UIOutput.make(tableRow, "studentitem-required", String.valueOf(i.isRequired()));
							UIOutput.make(tableRow, "studentitem-prerequisite", String.valueOf(i.isPrerequisite()));
							UIOutput.make(tableRow, "peer-eval", String.valueOf(i.getShowPeerEval()));
							makePeerRubric(tableRow,i, makeMaintainRubric, null, null, false);
							makeSamplePeerEval(tableRow);
							
							String peerEvalDate = i.getAttribute("rubricOpenDate");
							String peerDueDate = i.getAttribute("rubricDueDate");
							
							Calendar peerevalcal = Calendar.getInstance();
							
							if (peerEvalDate != null && peerDueDate != null) {
								DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, M_locale);
								//Open date from attribute string
								peerevalcal.setTimeInMillis(Long.valueOf(peerEvalDate));
								
								String dateStr = isoDateFormat.format(peerevalcal.getTime());
								
								UIOutput.make(tableRow, "peer-eval-open-date", dateStr);
								
								//Due date from attribute string
								peerevalcal.setTimeInMillis(Long.valueOf(peerDueDate));
								
								dateStr = isoDateFormat.format(peerevalcal.getTime());
								
								UIOutput.make(tableRow, "peer-eval-due-date", dateStr);
								UIOutput.make(tableRow, "peer-eval-allow-self", i.getAttribute("rubricAllowSelfGrade"));
								
							}else{
								//Default open and due date
								Date now = new Date();
								peerevalcal.setTime(now);

								//Default open date: now
								String dateStr = isoDateFormat.format(peerevalcal.getTime());
								
								UIOutput.make(tableRow, "peer-eval-open-date", dateStr);
								
								//Default due date: 7 days from now
								Date later = new Date(peerevalcal.getTimeInMillis() + 604800000);
								peerevalcal.setTime(later);
								
								dateStr = isoDateFormat.format(peerevalcal.getTime());
								
								//log.info("Setting date to " + dateStr + " and time to " + timeStr);
								
								UIOutput.make(tableRow, "peer-eval-due-date", dateStr);
								UIOutput.make(tableRow, "peer-eval-allow-self", i.getAttribute("rubricAllowSelfGrade"));
							}
							
							//Peer Eval Stats link
							GeneralViewParameters view = new GeneralViewParameters(PeerEvalStatsProducer.VIEW_ID);
							view.setSendingPage(currentPage.getPageId());
							view.setItemId(i.getId());
							if(i.getShowPeerEval()){
								UILink link = UIInternalLink.make(tableRow, "peer-eval-stats-link", view);
							}
							
							if (itemGroupString != null) {
								UIOutput.make(tableRow, "student-groups", itemGroupString);
							}
							UIOutput.make(tableRow, "student-owner-groups", simplePageBean.getItemOwnerGroupString(i));
							UIOutput.make(tableRow, "student-group-owned", (i.isGroupOwned()?"true":"false"));
							UIOutput.make(tableRow, "student-group-owned-eval-individual", (i.getAttribute("group-eval-individual")));
							UIOutput.make(tableRow, "student-group-owned-see-only-own", (i.getAttribute("see-only-own")));
						}
					}
				}else if(i.getType() == SimplePageItem.QUESTION) {
				 	String itemGroupString = null;
					String itemGroupTitles = null;
					if (canSeeAll) {
					    itemGroupString = simplePageBean.getItemGroupString(i, null, true);
					    if (itemGroupString != null)
						itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString, i);
					    if (itemGroupTitles != null) {
						itemGroupTitles = "[" + itemGroupTitles + "]";
					    }
					    if (canEditPage)
						UIOutput.make(tableRow, "item-groups", itemGroupString);
					    if (itemGroupTitles != null)
						UIOutput.make(tableRow, "questionitem-group-titles", itemGroupTitles);
					}
					SimplePageQuestionResponse response = simplePageToolDao.findQuestionResponse(i.getId(), simplePageBean.getCurrentUserId());
					
					UIOutput.make(tableRow, "questionSpan");

					boolean isAvailable = simplePageBean.isItemAvailable(i) || canSeeAll;
					
					UIOutput.make(tableRow, "questionDiv");
					
					UIOutput.make(tableRow, "questionText", i.getAttribute("questionText"));
					
					List<SimplePageQuestionAnswer> answers = new ArrayList<SimplePageQuestionAnswer>();
					if("multipleChoice".equals(i.getAttribute("questionType"))) {
						answers = simplePageToolDao.findAnswerChoices(i);
						UIOutput.make(tableRow, "multipleChoiceDiv");
						UIForm questionForm = UIForm.make(tableRow, "multipleChoiceForm");
						makeCsrf(questionForm, "csrf4");

						UIInput.make(questionForm, "multipleChoiceId", "#{simplePageBean.questionId}", String.valueOf(i.getId()));
						
						String[] options = new String[answers.size()];
						String initValue = null;
						for(int j = 0; j < answers.size(); j++) {
							options[j] = String.valueOf(answers.get(j).getId());
							if(response != null && answers.get(j).getId() == response.getMultipleChoiceId()) {
								initValue = String.valueOf(answers.get(j).getId());
							}
						}
						
						UISelect multipleChoiceSelect = UISelect.make(questionForm, "multipleChoiceSelect:", options, "#{simplePageBean.questionResponse}", initValue);
						if(!isAvailable || response != null) {
							multipleChoiceSelect.decorate(new UIDisabledDecorator());
						}
						 
						for(int j = 0; j < answers.size(); j++) {
							UIBranchContainer answerContainer = UIBranchContainer.make(questionForm, "multipleChoiceAnswer:", String.valueOf(j));
							UISelectChoice multipleChoiceInput = UISelectChoice.make(answerContainer, "multipleChoiceAnswerRadio", multipleChoiceSelect.getFullID(), j);
							
							multipleChoiceInput.decorate(new UIFreeAttributeDecorator("id", multipleChoiceInput.getFullID()));
							UIOutput.make(answerContainer, "multipleChoiceAnswerText", answers.get(j).getText())
								.decorate(new UIFreeAttributeDecorator("for", multipleChoiceInput.getFullID()));
							
							if(!isAvailable || response != null) {
								multipleChoiceInput.decorate(new UIDisabledDecorator());
							}
						}
						 
						UICommand answerButton = UICommand.make(questionForm, "answerMultipleChoice", messageLocator.getMessage("simplepage.answer_question"), "#{simplePageBean.answerMultipleChoiceQuestion}");
						if(!isAvailable || response != null) {
							answerButton.decorate(new UIDisabledDecorator());
						}
					}else if("shortanswer".equals(i.getAttribute("questionType"))) {
						UIOutput.make(tableRow, "shortanswerDiv");
						
						UIForm questionForm = UIForm.make(tableRow, "shortanswerForm");
						makeCsrf(questionForm, "csrf5");

						UIInput.make(questionForm, "shortanswerId", "#{simplePageBean.questionId}", String.valueOf(i.getId()));
						
						UIInput shortanswerInput = UIInput.make(questionForm, "shortanswerInput", "#{simplePageBean.questionResponse}");
						if(!isAvailable || response != null) {
							shortanswerInput.decorate(new UIDisabledDecorator());
							if(response != null && response.getShortanswer() != null) {
								shortanswerInput.setValue(response.getShortanswer());
							}
						}
						
						UICommand answerButton = UICommand.make(questionForm, "answerShortanswer", messageLocator.getMessage("simplepage.answer_question"), "#{simplePageBean.answerShortanswerQuestion}");
						if(!isAvailable || response != null) {
							answerButton.decorate(new UIDisabledDecorator());
						}
					}
					
					Status questionStatus = getQuestionStatus(i, response);
					addStatusImage(questionStatus, tableRow, "questionStatus", null);
					String statusNote = getStatusNote(questionStatus);
					if (statusNote != null) // accessibility version of icon
					    UIOutput.make(tableRow, "questionNote", statusNote);
					String statusText = null;
					if(questionStatus == Status.COMPLETED)
					    statusText = i.getAttribute("questionCorrectText");
					else if(questionStatus == Status.FAILED)
					    statusText = i.getAttribute("questionIncorrectText");
					if (statusText != null && !"".equals(statusText.trim()))
					    UIOutput.make(tableRow, "questionStatusText", statusText);
					
					// Output the poll data
					if("multipleChoice".equals(i.getAttribute("questionType")) &&
							(canEditPage || ("true".equals(i.getAttribute("questionShowPoll")) &&
									(questionStatus == Status.COMPLETED || questionStatus == Status.FAILED)))) {
						UIOutput.make(tableRow, "showPollGraph", messageLocator.getMessage("simplepage.show-poll"));
						UIOutput questionGraph = UIOutput.make(tableRow, "questionPollGraph");
						questionGraph.decorate(new UIFreeAttributeDecorator("id", "poll" + i.getId()));
						
						List<SimplePageQuestionResponseTotals> totals = simplePageToolDao.findQRTotals(i.getId());
						HashMap<Long, Long> responseCounts = new HashMap<Long, Long>();
						// in theory we don't need the first loop, as there should be a total
						// entry for all possible answers. But in case things are out of sync ...
						for(SimplePageQuestionAnswer answer : answers)
						    responseCounts.put(answer.getId(), 0L);
						for(SimplePageQuestionResponseTotals total : totals)
						    responseCounts.put(total.getResponseId(), total.getCount());
						
						for(int j = 0; j < answers.size(); j++) {
							UIBranchContainer pollContainer = UIBranchContainer.make(tableRow, "questionPollData:", String.valueOf(j));
							UIOutput.make(pollContainer, "questionPollText", answers.get(j).getText());
							UIOutput.make(pollContainer, "questionPollNumber", String.valueOf(responseCounts.get(answers.get(j).getId())));
						}
					}
					
					
					if(canEditPage) {
						UIOutput.make(tableRow, "question-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.question")));
						
						// always show grading panel. Currently this is the only way to get feedback
						if( !cameFromGradingPane) {
							QuestionGradingPaneViewParameters gp = new QuestionGradingPaneViewParameters(QuestionGradingPaneProducer.VIEW_ID);
							gp.placementId = toolManager.getCurrentPlacement().getId();
							gp.questionItemId = i.getId();
							gp.pageId = currentPage.getPageId();
							gp.pageItemId = pageItem.getId();
						
							UIInternalLink.make(tableRow, "questionGradingPaneLink", gp)
							    .decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.show-grading-pane")));
						}
						
						UILink.make(tableRow, "edit-question", (String)null, "")
							.decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.question")));
						
						UIOutput.make(tableRow, "questionId", String.valueOf(i.getId()));
						boolean graded = "true".equals(i.getAttribute("questionGraded")) || i.getGradebookId() != null;
						UIOutput.make(tableRow, "questionGrade", String.valueOf(graded));
						UIOutput.make(tableRow, "questionMaxPoints", String.valueOf(i.getGradebookPoints()));
						UIOutput.make(tableRow, "questionGradebookTitle", String.valueOf(i.getGradebookTitle()));
						UIOutput.make(tableRow, "questionitem-required", String.valueOf(i.isRequired()));
						UIOutput.make(tableRow, "questionitem-prerequisite", String.valueOf(i.isPrerequisite()));
						UIOutput.make(tableRow, "questionitem-groups", itemGroupString);
						UIOutput.make(tableRow, "questionCorrectText", String.valueOf(i.getAttribute("questionCorrectText")));
						UIOutput.make(tableRow, "questionIncorrectText", String.valueOf(i.getAttribute("questionIncorrectText")));
						
						if("shortanswer".equals(i.getAttribute("questionType"))) {
							UIOutput.make(tableRow, "questionType", "shortanswer");
							UIOutput.make(tableRow, "questionAnswer", i.getAttribute("questionAnswer"));
						}else {
							UIOutput.make(tableRow, "questionType", "multipleChoice");
							
							for(int j = 0; j < answers.size(); j++) {
								UIBranchContainer answerContainer = UIBranchContainer.make(tableRow, "questionMultipleChoiceAnswer:", String.valueOf(j));
								UIOutput.make(answerContainer, "questionMultipleChoiceAnswerId", String.valueOf(answers.get(j).getId()));
								UIOutput.make(answerContainer, "questionMultipleChoiceAnswerText", answers.get(j).getText());
								UIOutput.make(answerContainer, "questionMultipleChoiceAnswerCorrect", String.valueOf(answers.get(j).isCorrect()));
							}
							
							UIOutput.make(tableRow, "questionShowPoll", String.valueOf(i.getAttribute("questionShowPoll")));
						}
					}
				} else if (i.getType() == SimplePageItem.CHECKLIST) {
					UIOutput checklistItemContainer = UIOutput.make(tableRow, "checklistItemContainer");

					UIOutput checklistDiv = UIOutput.make(tableRow, "checklistDiv");

					styleItem(tableRow, checklistItemContainer, checklistDiv, i, null, null);

					UIOutput checklistTitle = UIOutput.make(tableRow, "checklistTitle", i.getName());

					if(Boolean.valueOf(i.getAttribute(SimplePageItem.NAMEHIDDEN))) {
						if(canEditPage) {
							checklistTitle.setValue("( " + i.getName() + " )");
							checklistTitle.decorate(new UIStyleDecorator("hiddenTitle"));
						} else {
							checklistTitle.decorate(new UIStyleDecorator("noDisplay"));
						}
					}

					UIOutput.make(tableRow, "checklistDescription", i.getDescription());

					List<SimpleChecklistItem> checklistItems = simplePageToolDao.findChecklistItems(i);

					UIOutput.make(tableRow, "checklistItemDiv");
					UIForm checklistForm = UIForm.make(tableRow, "checklistItemForm");

					UIInput.make(checklistForm, "checklistId", "#{simplePageBean.itemId}", String.valueOf(i.getId()));

					ArrayList<String> values = new ArrayList<String>();
					ArrayList<String> initValues = new ArrayList<String>();

					for (SimpleChecklistItem checklistItem : checklistItems) {
						values.add(String.valueOf(checklistItem.getId()));

						boolean isDone = simplePageToolDao.isChecklistItemChecked(i.getId(), checklistItem.getId(), simplePageBean.getCurrentUserId());

						if (isDone) {
							initValues.add(String.valueOf(checklistItem.getId()));
						} else {
							initValues.add("");
						}
					}

					UIOutput.make(checklistForm, "checklistItemsDiv");
					if(!checklistItems.isEmpty()) {
						UISelect select = UISelect.makeMultiple(checklistForm, "checklist-span", values.toArray(new String[1]), "#{simplePageBean.selectedChecklistItems}", initValues.toArray(new String[1]));

						int index = 0;
						for (SimpleChecklistItem checklistItem : checklistItems) {
							UIBranchContainer row = UIBranchContainer.make(checklistForm, "select-checklistitem-list:");
							UISelectChoice.make(row, "select-checklistitem", select.getFullID(), index).decorate(new UIStyleDecorator("checklist-checkbox"));

							UIOutput.make(row, "select-checklistitem-name", checklistItem.getName()).decorate(new UIStyleDecorator("checklist-checkbox-label"));
							index++;
						}
					}

					if (canEditPage) {

						String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
						String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString, i);
						if (itemGroupTitles != null) {
							itemGroupTitles = "[" + itemGroupTitles + "]";
						}

						UIOutput.make(tableRow, "item-groups-titles-checklist", itemGroupTitles);

						UIOutput.make(tableRow, "editchecklist-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-checklist").replace("{}", i.getName())));

						GeneralViewParameters eParams = new GeneralViewParameters();
						eParams.setSendingPage(currentPage.getPageId());
						eParams.setItemId(i.getId());
						eParams.viewID = ChecklistProducer.VIEW_ID;
						UIInternalLink.make(tableRow, "edit-checklist", (String)null, eParams).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-checklist").replace("{}", i.getName())));

						GeneralViewParameters gvp = new GeneralViewParameters();
						gvp.setSendingPage(currentPage.getPageId());
						gvp.setItemId(i.getId());
						gvp.viewID = ChecklistProgressProducer.VIEW_ID;
						UIInternalLink.make(tableRow, "edit-checklistProgress", (String)null, gvp).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.view-checklist").replace("{}", i.getName())));
					}

					makeSaveChecklistForm(tofill);
				}  else {
					// remaining type must be a block of HTML
					UIOutput.make(tableRow, "itemSpan");

					if (canSeeAll) {
					    String itemGroupString = simplePageBean.getItemGroupString(i, null, true);
					    String itemGroupTitles = simplePageBean.getItemGroupTitles(itemGroupString, i);
					    if (itemGroupTitles != null) {
						itemGroupTitles = "[" + itemGroupTitles + "]";
					    }
					    
					    UIOutput.make(tableRow, "item-groups-titles-text", itemGroupTitles);
					}

					if(canSeeAll || simplePageBean.isItemAvailable(i)) {
					    UIVerbatim.make(tableRow, "content", (i.getHtml() == null ? "" : i.getHtml()));
					} else {
					    UIComponent unavailableText = UIOutput.make(tableRow, "content", messageLocator.getMessage("simplepage.textItemUnavailable"));
					    unavailableText.decorate(new UIFreeAttributeDecorator("class", "disabled-text-item"));
					}

					// editing is done using a special producer that calls FCK.
					if (canEditPage) {
						GeneralViewParameters eParams = new GeneralViewParameters();
						eParams.setSendingPage(currentPage.getPageId());
						eParams.setItemId(i.getId());
						eParams.viewID = EditPageProducer.VIEW_ID;
						UIOutput.make(tableRow, "edittext-td").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.textbox").replace("{}", Integer.toString(textboxcount))));
						UIInternalLink.make(tableRow, "edit-link", (String)null, eParams).decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit-title.textbox").replace("{}", Integer.toString(textboxcount))));
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

			if (canSeeAll) {
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
		if (!anyItemVisible) {
			if (canEditPage) {
				String helpUrl = null;
				// order:
				// localized placedholder
				// localized general
				// default placeholder
				// we know the defaults exist because we include them, so
				// we never need to consider default general
				if (currentPage.getOwner() != null)
				    helpUrl = getLocalizedURL("student.html", true);
				else {
				    helpUrl = getLocalizedURL("placeholder.html", false);
				    if (helpUrl == null)
					helpUrl = getLocalizedURL("general.html", false);
				    if (helpUrl == null)
					helpUrl = getLocalizedURL("placeholder.html", true);
				}

				UIOutput.make(tofill, "startupHelp")
				    .decorate(new UIFreeAttributeDecorator("src", helpUrl))
				    .decorate(new UIFreeAttributeDecorator("id", "iframe"));
				if (!iframeJavascriptDone) {
				    UIOutput.make(tofill, "iframeJavascript");
				    iframeJavascriptDone = true;
				}
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
	
	public void makeCsrf(UIContainer tofill, String rsfid) {
	    Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
	    if (sessionToken != null)
		UIInput.make(tofill, rsfid, "simplePageBean.csrfToken", sessionToken.toString());
	}

	public void createDialogs(UIContainer tofill, SimplePage currentPage, SimplePageItem pageItem) {
		createEditItemDialog(tofill, currentPage, pageItem);
		createAddMultimediaDialog(tofill, currentPage);
		createEditMultimediaDialog(tofill, currentPage);
		createEditTitleDialog(tofill, currentPage, pageItem);
		createNewPageDialog(tofill, currentPage, pageItem);
		createRemovePageDialog(tofill, currentPage, pageItem);
		createImportCcDialog(tofill);
		createExportCcDialog(tofill);
		createYoutubeDialog(tofill, currentPage);
		createMovieDialog(tofill, currentPage);
		createCommentsDialog(tofill);
		createStudentContentDialog(tofill, currentPage);
		createQuestionDialog(tofill, currentPage);
		createDeleteItemDialog(tofill, currentPage);
		createColumnDialog(tofill, currentPage);
	}

    // get encrypted version of session id. This is our equivalent of session.id, except that we
    // encrypt it so it can't be used for anything else in case someone captures it.
    // we can't use time to avoid replay, as some time can go by between display of the page and
    // when the user clicks. The only thing this can be used for is reading multimedia files. I
    // think we're willing to risk that. I used to use session.id, but by default that's now off, 
    // and turning it on to use it here would expose us to more serious risks.  Cache the encryption.
    // we could include the whole URL in the encryption if it was worth the additional over head.
    // I think it's not.

    // url is /access/lessonbuilder/item/NNN/url. Because the server side
    // sees a reference starting with /item, we send that.
        public String getSessionParameter(String url) {
	    UsageSession session = UsageSessionService.getSession();
	    if (!url.startsWith("/access/lessonbuilder"))
		return null;
	    url = url.substring("/access/lessonbuilder".length());

	    try {
		Cipher sessionCipher = Cipher.getInstance("Blowfish");
		sessionCipher.init(Cipher.ENCRYPT_MODE, lessonBuilderAccessService.getSessionKey());
		String sessionParam = session.getId() + ":" + url;
		byte[] sessionBytes = sessionParam.getBytes("UTF8");
		sessionBytes = sessionCipher.doFinal(sessionBytes);
		sessionParam = DatatypeConverter.printHexBinary(sessionBytes);
		return sessionParam;
	    } catch (Exception e) {
		log.info("unable to generate encrypted session id " + e);
		return null;
	    }
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setAuthzGroupService(AuthzGroupService authzGroupService) {
		this.authzGroupService = authzGroupService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setSiteService(SiteService siteService) {
		this.siteService = siteService;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
		this.httpServletResponse = httpServletResponse;
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
		boolean fake = !available;  // by default, fake output if not available
		String itemString = Long.toString(i.getId());

		if (i.getSakaiId().equals(SimplePageItem.DUMMY)) {
		    fake = true; // dummy is fake, but still available
		} else if (i.getType() == SimplePageItem.RESOURCE || i.getType() == SimplePageItem.URL) {
			if (available) {
				if (i.getType() == SimplePageItem.RESOURCE && i.isSameWindow()) {
					GeneralViewParameters params = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
					params.setSendingPage(currentPage.getPageId());
					params.setItemId(i.getId());
					UILink link = UIInternalLink.make(container, "link", params);
					link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
					
				}
				else {
				    // run this through /access/lessonbuilder so we can track it even if the user uses the context menu
				    // We could do this only for the notDone case, but I think it could cause trouble for power users
				    // if the url isn't always consistent.
				    if (i.getAttribute("multimediaUrl") != null) { // resource where we've stored the URL ourselves
					URL = "/access/lessonbuilder/item/" + i.getId() + "/";
				    } else {
					URL = i.getItemURL(simplePageBean.getCurrentSiteId(),currentPage.getOwner());
				    }
				    UILink link = UILink.make(container, ID, URL);
				    link.decorate(new UIFreeAttributeDecorator("target", "_blank"));
				    if (notDone)
					link.decorate(new UIFreeAttributeDecorator("onclick", 
										   "afterLink($(this)," + i.getId() + ") ; return true"));
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
					isbutton = true;
				}
				
				UILink link;
				if (available) {
					link = UIInternalLink.make(container, ID, eParams);
					link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));

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
				    // for author, need to be able to get to the subpage to edit it
				    // so put out a function button, but make it look disabled
					fake = false; // so we don't get an fake button as well
					link = UIInternalLink.make(container, ID, eParams);
					link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
					fakeDisableLink(link, messageLocator);
				}  // otherwise fake
			}else {
				log.warn("Lesson Builder Item #" + i.getId() + " does not have an associated page.");
				return false;
			}
		} else if (i.getType() == SimplePageItem.ASSIGNMENT) {
		    // assignments won't let us get the entity if we're not in the group, so set up permissions before other tests
			if (available && i.isPrerequisite()) {
			    simplePageBean.checkItemPermissions(i, true);
			}
			LessonEntity lessonEntity = assignmentEntity.getEntity(i.getSakaiId(), simplePageBean);
			if (available && lessonEntity != null && (canEditPage || !lessonEntity.notPublished())) {

				GeneralViewParameters params = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				params.setSendingPage(currentPage.getPageId());
				params.setItemId(i.getId());
				UILink link = UIInternalLink.make(container, "link", params);
				link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				fake = true; // need to set this in case it's available for missing entity
			}
		} else if (i.getType() == SimplePageItem.ASSESSMENT) {
		    // assignments won't let us get the entity if we're not in the group, so set up permissions before other tests
			if (available && i.isPrerequisite()) {
			    simplePageBean.checkItemPermissions(i, true);
			}
			LessonEntity lessonEntity = quizEntity.getEntity(i.getSakaiId(),simplePageBean);
			if (available && lessonEntity != null && (canEditPage || !quizEntity.notPublished(i.getSakaiId()))) {
				// we've hacked Samigo to look at a special lesson builder
				// session
				// attribute. otherwise at the end of the test, Samigo replaces
				// the
				// whole screen, exiting form our iframe. The other tools don't
				// do this.
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setClearAttr("LESSONBUILDER_RETURNURL_SAMIGO");
				view.setItemId(i.getId());
				UILink link = UIInternalLink.make(container, "link", view);
				link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				fake = true; // need to set this in case it's available for missing entity
			}
		} else if (i.getType() == SimplePageItem.FORUM) {
		    // assignments won't let us get the entity if we're not in the group, so set up permissions before other tests
			if (available && i.isPrerequisite()) {
			    simplePageBean.checkItemPermissions(i, true);
			}
			LessonEntity lessonEntity = forumEntity.getEntity(i.getSakaiId());
			if (available && lessonEntity != null && (canEditPage || !lessonEntity.notPublished())) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setItemId(i.getId());
				UILink link = UIInternalLink.make(container, "link", view);
				link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				fake = true; // need to set this in case it's available for missing entity
			}
		} else if (i.getType() == SimplePageItem.CHECKLIST) {
			if (available) {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
				GeneralViewParameters view = new GeneralViewParameters(ChecklistProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setItemId(i.getId());
				UILink link = UIInternalLink.make(container, "link", view);
				link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				fake = true; // need to set this in case it's available for missing entity
			}
		} else if (i.getType() == SimplePageItem.BLTI) {
		    LessonEntity lessonEntity = (bltiEntity == null ? null : bltiEntity.getEntity(i.getSakaiId()));
		    if ("inline".equals(i.getFormat())) {
			// no availability 
			String height=null;
			if (i.getHeight() != null && !i.getHeight().equals(""))
			    height = i.getHeight().replace("px","");  // just in case
			
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
			if (available && lessonEntity != null) {
			    // I'm fairly sure checkitempermissions doesn't do anything useful for LTI,
			    // as it isn't group aware
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, true);
				}
				GeneralViewParameters view = new GeneralViewParameters(ShowItemProducer.VIEW_ID);
				view.setSendingPage(currentPage.getPageId());
				view.setItemId(i.getId());
				UIComponent link = UIInternalLink.make(container, "link", view)
				    .decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
			} else {
				if (i.isPrerequisite()) {
					simplePageBean.checkItemPermissions(i, false);
				}
				fake = true; // need to set this in case it's available for missing entity
			}
		    } else {
			if (available && lessonEntity != null) {
			    URL = lessonEntity.getUrl();
			    // UIInternalLink link = LinkTrackerProducer.make(container, ID, i.getName(), URL, i.getId(), notDone);
			    UILink link = UILink.make(container, ID, i.getName(), URL);
			    link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
			    link.decorate(new UIFreeAttributeDecorator("target", "_blank"));
			    if (notDone)
				link.decorate(new UIFreeAttributeDecorator("onclick", 
					 "setTimeout(function(){window.location.reload(true)},3000); return true"));

			} else
			    fake = true; // need to set this in case it's available for missing entity
		    }
		}

		String note = null;
		if (status == Status.COMPLETED) {
			note = messageLocator.getMessage("simplepage.status.completed");
		}
		if (status == Status.REQUIRED) {
			note = messageLocator.getMessage("simplepage.status.required");
		}

		if (fake) {
		    ID = ID + "-fake";
		    UIOutput link = UIOutput.make(container, ID, i.getName());
		    link.decorate(new UIFreeAttributeDecorator("lessonbuilderitem", itemString));
		    if (!available)
			link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.complete_required")));
		} else
		    UIOutput.make(container, ID + "-text", i.getName());

		if (note != null) {
			UIOutput.make(container, ID + "-note", note + " ");
		}

		return available;
	}

	private static void disableLink(UIComponent link, MessageLocator messageLocator) {
		link.decorate(new UIFreeAttributeDecorator("onclick", "return false"));
		link.decorate(new UIDisabledDecorator());
		link.decorate(new UIStyleDecorator("disabled"));
		link.decorate(new UIFreeAttributeDecorator("style", "color:#999 !important"));
		link.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.complete_required")));
	}

	private void styleItem(UIContainer row, UIComponent container, UIComponent component, SimplePageItem i, String indent, String customClass) {
	    // Indent level - default to 0 if not previously set
	    String indentLevel = i.getAttribute(SimplePageItem.INDENT)==null?"0":i.getAttribute(SimplePageItem.INDENT);
	    // Indent number in em is 4 times the level of indent

	    if (!"0".equals(indentLevel))
		container.decorate(new UIFreeAttributeDecorator("x-indent", indentLevel));
	    if (indent != null)
		UIOutput.make(row, indent, indentLevel);

	    // Custom css class(es)
	    String customCssClass = i.getAttribute(SimplePageItem.CUSTOMCSSCLASS);
	    if (customCssClass != null && !customCssClass.equals("")) {
		component.decorate(new UIStyleDecorator(customCssClass));
	    }
	    if (customClass != null) {
		UIOutput.make(row, customClass, customCssClass);
	    }

	}

	// show is if it was disabled but don't actually
	private static void fakeDisableLink(UILink link, MessageLocator messageLocator) {
		link.decorate(new UIFreeAttributeDecorator("style", "color:#999 !important"));
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

	public void setToolManager(ToolManager m) {
		toolManager = m;
	}

	public void setLessonBuilderAccessService (LessonBuilderAccessService a) {
	    if (lessonBuilderAccessService == null)
		lessonBuilderAccessService = a;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}

	/**
	 * Checks for the version of IE. Returns 0 if we're not running IE.
	 * But there's a problem. IE 11 doesn't have the MSIE tag. But it stiill
	 * needs to be treated as IE, because the OBJECT tag won't work with Quicktime
	 * Since all I test is > 0, I use a simplified version that returns 0 or 1
	 * @return
	 */
	public int checkIEVersion() {
		UsageSession usageSession = UsageSessionService.getSession();
		if (usageSession == null)
		    return 0;
		browserString = usageSession.getUserAgent();
		if (browserString == null)
		    return 0;
		int ieIndex = browserString.indexOf("Trident/");
		if (ieIndex >= 0)
		    return 1;
		else
		    return 0;

		// int ieVersion = 0;
		// if (ieIndex >= 0) {
		//	String ieV = browserString.substring(ieIndex + 6);
		//	int i = 0;
		//	int e = ieV.length();
		//	while (i < e) {
		//		if (Character.isDigit(ieV.charAt(i))) {
		//			i++;
		//		} else {
		//			break;
		//		}
		//	}
		//	if (i > 0) {
		//		ieV = ieV.substring(0, i);
		//		ieVersion = Integer.parseInt(ieV);
		//}			}
		//
		//		return ieVersion;
	}

	private void createToolBar(UIContainer tofill, SimplePage currentPage, boolean isStudent) {
		UIBranchContainer toolBar = UIBranchContainer.make(tofill, "tool-bar:");
		boolean studentPage = currentPage.getOwner() != null;

		// toolbar

		// dropdowns
		UIOutput.make(toolBar, "icondropc");
		UIOutput.make(toolBar, "icondrop");

		// right side
		createToolBarLink(ReorderProducer.VIEW_ID, toolBar, "reorder", "simplepage.reorder", currentPage, "simplepage.reorder-tooltip");
		UILink.make(toolBar, "help", messageLocator.getMessage("simplepage.help"), 
			    getLocalizedURL( isStudent ? "student.html" : "general.html", true));

		// add content menu
		createToolBarLink(EditPageProducer.VIEW_ID, tofill, "add-text1", null, currentPage, "simplepage.text.tooltip").setItemId(null);
		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, tofill, "add-resource1", null, false, false,  currentPage, "simplepage.resource.tooltip");
		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, tofill, "add-multimedia1", null, true, false, currentPage, "simplepage.multimedia.tooltip");
		UIInternalLink.makeURL(tofill, "subpage-link1", "#").
		    decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.subpage-descrip")));
		UIInternalLink.makeURL(tofill, "addcontent", "#").
		    decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.add-item-page")));

		createToolBarLink(EditPageProducer.VIEW_ID, tofill, "add-text", "simplepage.text", currentPage, "simplepage.text.tooltip").setItemId(null);
		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, tofill, "add-multimedia", "simplepage.multimedia", true, false, currentPage, "simplepage.multimedia.tooltip");
		createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, tofill, "add-resource", "simplepage.resource", false, false,  currentPage, "simplepage.resource.tooltip");
		UIComponent subpagelink = UIInternalLink.makeURL(tofill, "subpage-link", "#");
		subpagelink.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.subpage-descrip")));

		UIOutput.make(tofill, "add-break1");
		UIOutput.make(tofill, "add-break2");
		UIOutput.make(tofill, "add-break3");
		UIOutput.make(tofill, "add-break4");
		UIOutput.make(tofill, "add-break5");

		// content menu not on students
		if (!studentPage) {

		    // add website.
		    // Are we running a kernel with KNL-273?
		    Class contentHostingInterface = ContentHostingService.class;
		    try {
			Method expandMethod = contentHostingInterface.getMethod("expandZippedResource", new Class[] { String.class });
			
			UIOutput.make(tofill, "addwebsite-li");
			createFilePickerToolBarLink(ResourcePickerProducer.VIEW_ID, tofill, "add-website", "simplepage.website", false, true, currentPage, "simplepage.website.tooltip");
		    } catch (NoSuchMethodException nsme) {
			// A: No
		    } catch (Exception e) {
			// A: Not sure
			log.warn("SecurityException thrown by expandZippedResource method lookup", e);
		    }
			
		    UIOutput.make(tofill, "assignment-li");
		    createToolBarLink(AssignmentPickerProducer.VIEW_ID, tofill, "add-assignment", "simplepage.assignment-descrip", currentPage, "simplepage.assignment");

		    UIOutput.make(tofill, "quiz-li");
		    createToolBarLink(QuizPickerProducer.VIEW_ID, tofill, "add-quiz", "simplepage.quiz-descrip", currentPage, "simplepage.quiz");

		    UIOutput.make(tofill, "forum-li");
		    createToolBarLink(ForumPickerProducer.VIEW_ID, tofill, "add-forum", "simplepage.forum-descrip", currentPage, "simplepage.forum.tooltip");

		    UIOutput.make(tofill, "checklist-li");
		    createToolBarLink(ChecklistProducer.VIEW_ID, tofill, "add-checklist", "simplepage.checklist", currentPage, "simplepage.checklist");

		    UIOutput.make(tofill, "question-li");
		    UIComponent questionlink = UIInternalLink.makeURL(tofill, "question-link", "#");
		    questionlink.decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.question-descrip")));

		    UIOutput.make(tofill, "student-li");
		    UIOutput.make(tofill, "add-comments-link").	decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.comments.tooltip")));
		    UIForm form = UIForm.make(tofill, "add-comments-form");
		    UIInput.make(form, "comments-addBefore", "#{simplePageBean.addBefore}");
		    makeCsrf(form, "csrf25");
		    UICommand.make(form, "add-comments", "#{simplePageBean.addCommentsSection}");

		    UIOutput.make(tofill, "studentcontent-li");
		    UIOutput.make(tofill, "add-student-link").	decorate(new UITooltipDecorator(messageLocator.getMessage("simplepage.student-descrip")));
		    form = UIForm.make(tofill, "add-student-form");
		    UIInput.make(form, "add-student-addBefore", "#{simplePageBean.addBefore}");
		    makeCsrf(form, "csrf26");
		    UICommand.make(form, "add-student", "#{simplePageBean.addStudentContentSection}");

		    // in case we're on an old system without current BLTI
		    if (bltiEntity != null && ((BltiInterface)bltiEntity).servicePresent()) {
			Collection<BltiTool> bltiTools = simplePageBean.getBltiTools();
			if (bltiTools != null) {
			    int i = 0;
			    for (BltiTool bltiTool: bltiTools) {
				UIBranchContainer toolItems = UIBranchContainer.make(tofill, "blti-tool:", String.valueOf(i));
				i++;
				GeneralViewParameters params = new GeneralViewParameters();
				params.setSendingPage(currentPage.getPageId());
				params.addTool = bltiTool.id;
				params.viewID = BltiPickerProducer.VIEW_ID;
				UILink link = UIInternalLink.make(toolItems, "add-blti-tool", bltiTool.title, params);
				link.decorate(new UITooltipDecorator(bltiTool.description));
				if (bltiTool.description != null)
				    UIOutput.make(toolItems, "add-blti-description", bltiTool.description);
			    }
			}
			UIOutput.make(tofill, "blti-li");
			createToolBarLink(BltiPickerProducer.VIEW_ID, tofill, "add-blti", "simplepage.blti", currentPage, "simplepage.blti.tooltip");
		    }
			
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
		if (message != null)
		    message = messageLocator.getMessage(message);
		UILink link = UIInternalLink.make(tofill, ID, message , params);
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
		makeCsrf(form, "csrf7");

		UIOutput.make(form, "subpage-label", messageLocator.getMessage("simplepage.pageTitle_label"));
		UIInput.make(form, "subpage-title", "#{simplePageBean.subpageTitle}");

		GeneralViewParameters view = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
		view.setSendingPage(currentPage.getPageId());

		if(currentPage.getOwner() == null) {
			UIInternalLink.make(form, "subpage-choose", messageLocator.getMessage("simplepage.choose_existing_page"), view);
		}
		
		UIBoundBoolean.make(form, "subpage-next", "#{simplePageBean.subpageNext}", false);
		UIBoundBoolean.make(form, "subpage-button", "#{simplePageBean.subpageButton}", false);

		UIInput.make(form, "subpage-add-before", "#{simplePageBean.addBefore}");
		UICommand.make(form, "create-subpage", messageLocator.getMessage("simplepage.create"), "#{simplePageBean.createSubpage}");
		UICommand.make(form, "cancel-subpage", messageLocator.getMessage("simplepage.cancel"), null);

	}

	private void createEditItemDialog(UIContainer tofill, SimplePage currentPage, SimplePageItem pageItem) {
		String currentToolTitle = simplePageBean.getPageTitle();
		String returnFromEditString = messageLocator.getMessage("simplepage.return_from_edit").replace("{}",currentToolTitle);  
  
		UIOutput.make(tofill, "edit-item-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edititem_header")));

		UIForm form = UIForm.make(tofill, "edit-form");
		makeCsrf(form, "csrf8");

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
		params.setId(Long.toString(pageItem.getId()));
		params.setReturnView(VIEW_ID);
		params.setTitle(returnFromEditString);  
		params.setSource("EDIT");
		params.viewID = ShowItemProducer.VIEW_ID;
		UIInternalLink.make(form, "edit-item-object", params);
		UIOutput.make(form, "edit-item-text");

		params = new GeneralViewParameters();
		params.setSendingPage(currentPage.getPageId());
		params.setId(Long.toString(pageItem.getId()));
		params.setReturnView(VIEW_ID);
		params.setTitle(returnFromEditString);  
		params.setSource("SETTINGS");
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

		UIBoundBoolean.make(form, "page-releasedate2", "#{simplePageBean.hasReleaseDate}", Boolean.FALSE);

		String releaseDateString = "";
		try {
			releaseDateString = isoDateFormat.format(new Date());
		} catch (Exception e) {
			log.error(e + "bad format releasedate " + new Date());
		}

		UIOutput releaseForm2 = UIOutput.make(form, "releaseDate2:");
		UIOutput.make(form, "sbReleaseDate", releaseDateString);
		UIInput.make(form, "release_date2", "#{simplePageBean.releaseDate}" );

		String indentOptions[] = {"0","1","2","3","4","5","6","7","8"};
		UISelect.make(form, "indent-level", indentOptions, "#{simplePageBean.indentLevel}", indentOptions[0]);

		// If current user is an admin show the css class input box
		UIInput customCssClass = UIInput.make(form, "customCssClass", "#{simplePageBean.customCssClass}");
		UIOutput.make(form, "custom-css-label", messageLocator.getMessage("simplepage.custom.css.class"));

		// can't use site groups on user content, and don't want students to hack
		// on groups for site content
		if (currentPage.getOwner() == null)
		    createGroupList(form, null, "", "#{simplePageBean.selectedGroups}");

		UICommand.make(form, "delete-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "edit-item-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	// for both add multimedia and add resource, as well as updating resources
	// in the edit dialogs
    public void createGroupList(UIContainer tofill, Collection<String> groupsSet, String prefix, String beanspec) {
		List<GroupEntry> groups = simplePageBean.getCurrentGroups();
		ArrayList<String> values = new ArrayList<String>();
		ArrayList<String> initValues = new ArrayList<String>();

		if (groups == null || groups.size() == 0)
			return;

		for (GroupEntry entry : groups) {
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

		UIOutput.make(tofill, prefix + "grouplist");
		UISelect select = UISelect.makeMultiple(tofill, prefix + "group-list-span", values.toArray(new String[1]), beanspec, initValues.toArray(new String[1]));

		int index = 0;
		for (GroupEntry entry : groups) {
			UIBranchContainer row = UIBranchContainer.make(tofill, prefix + "select-group-list:");
			UISelectChoice.make(row, prefix + "select-group", select.getFullID(), index);

			UIOutput.make(row, prefix + "select-group-text", entry.name);
			index++;
		}

	}

	// for both add multimedia and add resource, as well as updating resources
	// in the edit dialogs
	private void createAddMultimediaDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "add-multimedia-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.resource")));
		UILink.make(tofill, "mm-additional-instructions", messageLocator.getMessage("simplepage.additional-instructions-label"), 
			    getLocalizedURL( "multimedia.html", true));
		UILink.make(tofill, "mm-additional-website-instructions", messageLocator.getMessage("simplepage.additional-website-instructions-label"), 
			    getLocalizedURL( "website.html", true));

		UIForm form = UIForm.make(tofill, "add-multimedia-form");
		makeCsrf(form, "csrf9");

		UIInput.make(form, "mm-name", "#{simplePageBean.name}");
		UIInput.make(form, "mm-names", "#{simplePageBean.names}");
		UIOutput.make(form, "mm-file-label", messageLocator.getMessage("simplepage.upload_label"));

		UIOutput.make(form, "mm-url-label", messageLocator.getMessage("simplepage.addLink_label"));
		UIInput.make(form, "mm-url", "#{simplePageBean.mmUrl}");

		FilePickerViewParameters fileparams = new FilePickerViewParameters();
		fileparams.setSender(currentPage.getPageId());
		fileparams.setResourceType(true);
		fileparams.viewID = ResourcePickerProducer.VIEW_ID;
		
		UILink link = UIInternalLink.make(form, "mm-choose", messageLocator.getMessage("simplepage.choose_existing_or"), fileparams);

		if (currentPage.getOwner() == null) {
		    UIOutput.make(form, "mm-prerequisite-section");
		    UIBoundBoolean.make(form, "mm-prerequisite", "#{simplePageBean.prerequisite}", false);
		}
		UIBoundBoolean.make(form, "mm-file-replace", "#{simplePageBean.replacefile}", false);

		UICommand.make(form, "mm-add-item", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.addMultimedia}");
		UIOutput.make(form, "mm-test-tryother").decorate(new UIFreeAttributeDecorator("value", messageLocator.getMessage("simplepage.mm-test-tryother")));
		UIOutput.make(form, "mm-test-start-over").decorate(new UIFreeAttributeDecorator("value", messageLocator.getMessage("simplepage.mm-test-start-over")));
		UIInput.make(form, "mm-item-id", "#{simplePageBean.itemId}");
		UIInput.make(form, "mm-add-before", "#{simplePageBean.addBefore}");
		UIInput.make(form, "mm-is-mm", "#{simplePageBean.isMultimedia}");
		UIInput.make(form, "mm-display-type", "#{simplePageBean.multimediaDisplayType}");
		UIInput.make(form, "mm-is-website", "#{simplePageBean.isWebsite}");
		UIInput.make(form, "mm-is-caption", "#{simplePageBean.isCaption}");
		UICommand.make(form, "mm-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createImportCcDialog(UIContainer tofill) {
		UIOutput.make(tofill, "import-cc-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.import_cc")));

		UIForm form = UIForm.make(tofill, "import-cc-form");
		makeCsrf(form, "csrf11");

		UICommand.make(form, "import-cc-submit", messageLocator.getMessage("simplepage.save_message"), "#{simplePageBean.importCc}");
		UICommand.make(form, "mm-cancel", messageLocator.getMessage("simplepage.cancel"), null);

		UIBoundBoolean.make(form, "import-toplevel", "#{simplePageBean.importtop}", false);


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

		int numAssignEngines = 0;
		List<ToolData> assignEngines = new ArrayList<ToolData>();

		for (LessonEntity q = assignmentEntity; q != null; q = q.getNextEntity()) {
			String toolId = q.getToolId();
			String toolName = simplePageBean.getCurrentToolTitle(q.getToolId());
			// we only want the ones that are actually in our site
			if (toolName != null) {
				ToolData toolData = new ToolData();
				toolData.toolId = toolId;
				toolData.toolName = toolName;
				numAssignEngines++;
				assignEngines.add(toolData);
			}
		}

		if (numAssignEngines == 0) {
			UIVerbatim.make(form, "assignmsg", messageLocator.getMessage("simplepage.noassignengines"));
		} else if (numAssignEngines == 1) {
			UIInput.make(form, "assigntool", "#{simplePageBean.assigntool}", assignmentEntity.getToolId());
		} else {
			ArrayList<String> values = new ArrayList<String>();
			for (ToolData toolData : assignEngines) {
				values.add(toolData.toolId);
			}

			UIOutput.make(form, "assignmsg", messageLocator.getMessage("simplepage.chooseassignengine"));
			UISelect assignselect = UISelect.make(form, "assigntools", values.toArray(new String[1]), "#{simplePageBean.assigntool}", null);
			int i = 0;
			for (ToolData toolData : assignEngines) {
				UIBranchContainer toolItem = UIBranchContainer.make(form, "assigntoolitem:", String.valueOf(i));
				UISelectChoice.make(toolItem, "assigntoolbox", assignselect.getFullID(), i);
				UIOutput.make(toolItem, "assigntoollabel", toolData.toolName);
				i++;
			}
		}


	}

	private void createExportCcDialog(UIContainer tofill) {
		UIOutput.make(tofill, "export-cc-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.export-cc-title")));

		UIForm form = UIForm.make(tofill, "export-cc-form");

		UIOutput.make(form, "export-cc-v11"); // value is handled by JS, so RSF doesn't need to treat it as input
		UIOutput.make(form, "export-cc-v13"); // value is handled by JS, so RSF doesn't need to treat it as input
		UIOutput.make(form, "export-cc-bank"); // value is handled by JS, so RSF doesn't need to treat it as input
		UICommand.make(form, "export-cc-submit", messageLocator.getMessage("simplepage.exportcc-download"), "#{simplePageBean.importCc}");
		UICommand.make(form, "export-cc-cancel", messageLocator.getMessage("simplepage.cancel"), null);

		// the actual submission is with a GET. The submit button clicks this link.
		ExportCCViewParameters view = new ExportCCViewParameters("exportCc");
		view.setExportcc(true);
		view.setVersion("1.2");
		view.setBank("1");
		UIInternalLink.make(form, "export-cc-link", "export cc link", view);

	}

	private void createEditMultimediaDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "edit-multimedia-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editMultimedia")));

		UIOutput.make(tofill, "instructions");

		UIForm form = UIForm.make(tofill, "edit-multimedia-form");
		makeCsrf(form, "csrf10");

		UIOutput.make(form, "height-label", messageLocator.getMessage("simplepage.height_label"));
		UIInput.make(form, "height", "#{simplePageBean.height}");

		UIOutput.make(form, "width-label", messageLocator.getMessage("simplepage.width_label"));
		UIInput.make(form, "width", "#{simplePageBean.width}");

		UIOutput.make(form, "description2-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description2", "#{simplePageBean.description}");

		if (currentPage.getOwner() == null) {
		    UIOutput.make(form, "multi-prerequisite-section");
		    UIBoundBoolean.make(form, "multi-prerequisite", "#{simplePageBean.prerequisite}",false);
		}

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
		makeCsrf(form, "csrf17");
		UIInput.make(form, "youtubeURL", "#{simplePageBean.youtubeURL}");
		UIInput.make(form, "youtubeEditId", "#{simplePageBean.youtubeId}");
		UIInput.make(form, "youtubeHeight", "#{simplePageBean.height}");
		UIInput.make(form, "youtubeWidth", "#{simplePageBean.width}");
		UIOutput.make(form, "description4-label", messageLocator.getMessage("simplepage.description_label"));
		UIInput.make(form, "description4", "#{simplePageBean.description}");
		UICommand.make(form, "delete-youtube-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteYoutubeItem}");
		UICommand.make(form, "update-youtube", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateYoutube}");
		UICommand.make(form, "cancel-youtube", messageLocator.getMessage("simplepage.cancel"), null);
		UIBoundBoolean.make(form, "youtube-prerequisite", "#{simplePageBean.prerequisite}",false);
		
		if(currentPage.getOwner() == null) {
			UIOutput.make(form, "editgroups-youtube");
		}
	}

	private void createMovieDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "movie-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edititem_header")));

		UIForm form = UIForm.make(tofill, "movie-form");
		makeCsrf(form, "csrf18");

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

		fileparams.setCaption(true);
		UIInternalLink.make(form, "change-caption-movie", messageLocator.getMessage("simplepage.change_caption"), fileparams);

		UIBoundBoolean.make(form, "movie-prerequisite", "#{simplePageBean.prerequisite}",false);

		UICommand.make(form, "delete-movie-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-movie", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateMovie}");
		UICommand.make(form, "movie-cancel", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createEditTitleDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		if (pageItem.getType() == SimplePageItem.STUDENT_CONTENT)
			UIOutput.make(tofill, "edit-title-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.editTitle")));
		else
			UIOutput.make(tofill, "edit-title-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.title")));

		UIForm form = UIForm.make(tofill, "title-form");
		makeCsrf(form, "csrf14");

		UIOutput.make(form, "pageTitleLabel", messageLocator.getMessage("simplepage.pageTitle_label"));
		UIInput.make(form, "pageTitle", "#{simplePageBean.pageTitle}");

		if (page.getOwner() == null) {
			UIOutput.make(tofill, "hideContainer");
			UIBoundBoolean.make(form, "hide", "#{simplePageBean.hidePage}", (page.isHidden()));

			Date releaseDate = page.getReleaseDate();

			UIBoundBoolean.make(form, "page-releasedate", "#{simplePageBean.hasReleaseDate}", (releaseDate != null));

			String releaseDateString = "";

			if (releaseDate != null) {
			try {
			    releaseDateString = isoDateFormat.format(releaseDate);
			} catch (Exception e) {
			    log.info(e + "bad format releasedate " + releaseDate);
			}
			}
			
			UIOutput releaseForm = UIOutput.make(form, "releaseDate:");
			UIOutput.make(form, "currentReleaseDate", releaseDateString);
			UIInput.make(form, "release_date_string", "#{simplePageBean.releaseDate}" );
			UIOutput.make(form, "release_date");

			if (pageItem.getPageId() == 0) {
			    UIOutput.make(form, "prereqContainer");
			    UIBoundBoolean.make(form, "page-required", "#{simplePageBean.required}", (pageItem.isRequired()));
			    UIBoundBoolean.make(form, "page-prerequisites", "#{simplePageBean.prerequisite}", (pageItem.isPrerequisite()));
			}
		}

		UIOutput gradeBook = UIOutput.make(form, "gradeBookDiv");
		if(page.getOwner() != null) gradeBook.decorate(new UIStyleDecorator("noDisplay"));
		
		UIOutput.make(form, "page-gradebook");
		Double points = page.getGradebookPoints();
		String pointString = "";
		if (points != null) {
			pointString = points.toString();
		}
		
		if(page.getOwner() == null) {
			UIOutput.make(form, "csssection");
			ArrayList<ContentResource> sheets = simplePageBean.getAvailableCss();
			String[] options = new String[sheets.size()+2];
			String[] labels = new String[sheets.size()+2];
			
			// Sets up the CSS arrays
			options[0] = null;
			labels[0] = messageLocator.getMessage("simplepage.default-css");
			options[1] = null;
			labels[1] = "----------";
			for(int i = 0; i < sheets.size(); i++) {
				if(sheets.get(i) != null) {
					options[i+2] = sheets.get(i).getId();
					labels[i+2] = sheets.get(i).getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				}else {
					// We show just one un-named separator if there are only site css, or system css, but not both.
					// If we get here, it means we have both, so we name them.
					options[i+2] = null;
					labels[i+2] = "---" + messageLocator.getMessage("simplepage.system") + "---";
					labels[1] = "---" + messageLocator.getMessage("simplepage.site") + "---";
				}
			}
			
			UIOutput.make(form, "cssDropdownLabel", messageLocator.getMessage("simplepage.css-dropdown-label"));
			UISelect.make(form, "cssDropdown", options, labels, "#{simplePageBean.dropDown}", page.getCssSheet());
			
			UIOutput.make(form, "cssDefaultInstructions", messageLocator.getMessage("simplepage.css-default-instructions"));
			UIOutput.make(form, "cssUploadLabel", messageLocator.getMessage("simplepage.css-upload-label"));
			UIOutput.make(form, "cssUpload");
			UIBoundBoolean.make(form, "nodownloads", 
					    "#{simplePageBean.nodownloads}", 
					    (simplePageBean.getCurrentSite().getProperties().getProperty("lessonbuilder-nodownloadlinks") != null));
		}
		UIInput.make(form, "page-points", "#{simplePageBean.points}", pointString);

		UICommand.make(form, "create-title", messageLocator.getMessage("simplepage.save"), "#{simplePageBean.editTitle}");
		UICommand.make(form, "cancel-title", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

	private void createNewPageDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		UIOutput.make(tofill, "new-page-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.new-page")));

		UIForm form = UIForm.make(tofill, "new-page-form");
		makeCsrf(form, "csrf15");

		UIInput.make(form, "newPage", "#{simplePageBean.newPageTitle}");

		UIInput.make(form, "new-page-number", "#{simplePageBean.numberOfPages}");

		UIBoundBoolean.make(form, "new-page-copy", "#{simplePageBean.copyPage}", false);
		
		GeneralViewParameters view = new GeneralViewParameters(PagePickerProducer.VIEW_ID);
		view.setSendingPage(-1L);
		view.newTopLevel = true;
		UIInternalLink.make(tofill, "new-page-choose", messageLocator.getMessage("simplepage.lm_existing_page"), view);

		UICommand.make(form, "new-page-submit", messageLocator.getMessage("simplepage.save"), "#{simplePageBean.addPages}");
		UICommand.make(form, "new-page-cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

	private void createRemovePageDialog(UIContainer tofill, SimplePage page, SimplePageItem pageItem) {
		UIOutput.make(tofill, "remove-page-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.remove-page")));
		UIOutput.make(tofill, "remove-page-explanation", 
			      (page.getOwner() == null ? messageLocator.getMessage("simplepage.remove-page-explanation") :
			       messageLocator.getMessage("simplepage.remove-student-page-explanation")));

		UIForm form = UIForm.make(tofill, "remove-page-form");
		makeCsrf(form, "csrf16");

		form.addParameter(new UIELBinding("#{simplePageBean.removeId}", page.getPageId()));
		
		//		if (page.getOwner() == null) {
		//		    // top level normal page. Use the remove page producer, which can handle removing tools out from under RSF
		//		    GeneralViewParameters params = new GeneralViewParameters(RemovePageProducer.VIEW_ID);
		//		    UIInternalLink.make(form, "remove-page-submit", "", params).decorate(new UIFreeAttributeDecorator("value", messageLocator.getMessage("simplepage.remove")));
		//		} else
		//		    // a student top level page. call remove page directly, as it will just return to show page
		//		    UICommand.make(form, "remove-page-submit", messageLocator.getMessage("simplepage.remove"), "#{simplePageBean.removePage}");

		
		UIComponent button = UICommand.make(form, "remove-page-submit", messageLocator.getMessage("simplepage.remove"), "#{simplePageBean.removePage}");
		if (page.getOwner() == null) // not student page
		    button.decorate(new UIFreeAttributeDecorator("onclick",
			         "window.location='/lessonbuilder-tool/removePage?site=" + simplePageBean.getCurrentSiteId() + 
				 "&page=" + page.getPageId() + "';return false;"));

		UICommand.make(form, "remove-page-cancel", messageLocator.getMessage("simplepage.cancel"), "#{simplePageBean.cancel}");
	}

	private void createCommentsDialog(UIContainer tofill) {
		UIOutput.make(tofill, "comments-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_commentslink")));

		UIForm form = UIForm.make(tofill, "comments-form");
		makeCsrf(form, "csrf19");

		UIInput.make(form, "commentsEditId", "#{simplePageBean.itemId}");

		UIBoundBoolean.make(form, "comments-anonymous", "#{simplePageBean.anonymous}");
		UIBoundBoolean.make(form, "comments-graded", "#{simplePageBean.graded}");
		UIInput.make(form, "comments-max", "#{simplePageBean.maxPoints}");
		
		UIBoundBoolean.make(form, "comments-required", "#{simplePageBean.required}");
		UIBoundBoolean.make(form, "comments-prerequisite", "#{simplePageBean.prerequisite}");

		UICommand.make(form, "delete-comments-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-comments", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateComments}");
		UICommand.make(form, "cancel-comments", messageLocator.getMessage("simplepage.cancel"), null);
	}
	
	private void createStudentContentDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "student-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_studentlink")));

		UIForm form = UIForm.make(tofill, "student-form");
		makeCsrf(form, "csrf20");

		UIInput.make(form, "studentEditId", "#{simplePageBean.itemId}");

		UIBoundBoolean.make(form, "student-anonymous", "#{simplePageBean.anonymous}");
		UIBoundBoolean.make(form, "student-comments", "#{simplePageBean.comments}");
		UIBoundBoolean.make(form, "student-comments-anon", "#{simplePageBean.forcedAnon}");
		UIBoundBoolean.make(form, "student-required", "#{simplePageBean.required}");
		UIBoundBoolean.make(form, "student-prerequisite", "#{simplePageBean.prerequisite}");
		
		UIOutput.make(form, "peer-evaluation-creation");
		
		UIBoundBoolean.make(form, "peer-eval-check", "#{simplePageBean.peerEval}");
		UIInput.make(form, "peer-eval-input-title", "#{simplePageBean.rubricTitle}");
		UIInput.make(form, "peer-eval-input-row", "#{simplePageBean.rubricRow}");

		UIOutput.make(form, "peer_eval_open_date_label", messageLocator.getMessage("simplepage.peer-eval.open_date"));
       
		UIOutput openDateField = UIOutput.make(form, "peer_eval_open_date:");
		UIInput.make(form, "open_date_string", "#{simplePageBean.peerEvalOpenDate}");
		UIOutput.make(form, "open_date_dummy");

		UIOutput dueDateField = UIOutput.make(form, "peer_eval_due_date:");
		UIInput.make(form, "due_date_string", "#{simplePageBean.peerEvalDueDate}");
		UIOutput.make(form, "due_date_dummy");
        
		UIBoundBoolean.make(form, "peer-eval-allow-selfgrade", "#{simplePageBean.peerEvalAllowSelfGrade}");
        
		UIBoundBoolean.make(form, "student-graded", "#{simplePageBean.graded}");
		UIInput.make(form, "student-max", "#{simplePageBean.maxPoints}");

		UIBoundBoolean.make(form, "student-comments-graded", "#{simplePageBean.sGraded}");
		UIInput.make(form, "student-comments-max", "#{simplePageBean.sMaxPoints}");

		UIBoundBoolean.make(form, "student-group-owned", "#{simplePageBean.groupOwned}");
		createGroupList(form, null, "student-", "#{simplePageBean.studentSelectedGroups}");

		UIBoundBoolean.make(form, "student-group-owned-eval-individual", "#{simplePageBean.groupOwnedIndividual}");
		UIBoundBoolean.make(form, "student-group-owned-see-only-own", "#{simplePageBean.seeOnlyOwn}");

		UICommand.make(form, "delete-student-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-student", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateStudent}");
		UICommand.make(form, "cancel-student", messageLocator.getMessage("simplepage.cancel"), null);
		
		// RU Rubrics
		UIOutput.make(tofill, "peer-eval-create-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.peer-eval-create-title")));
	}
	
	private void createQuestionDialog(UIContainer tofill, SimplePage currentPage) {
		UIOutput.make(tofill, "question-dialog").decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.edit_questionlink")));
		
		UIForm form = UIForm.make(tofill, "question-form");
		makeCsrf(form, "csrf21");
		
		UISelect questionType = UISelect.make(form, "question-select", new String[] {"multipleChoice", "shortanswer"}, "#{simplePageBean.questionType}", "");
		UISelectChoice.make(form, "multipleChoiceSelect", questionType.getFullID(), 0);
		UISelectChoice.make(form, "shortanswerSelect", questionType.getFullID(), 1);

		UIOutput.make(form, "question-shortans-del").decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.delete")));
		UIOutput.make(form, "question-mc-del").decorate(new UIFreeAttributeDecorator("alt", messageLocator.getMessage("simplepage.delete")));
		UIInput.make(form, "questionEditId", "#{simplePageBean.itemId}");
		
		UIBoundBoolean.make(form, "question-required", "#{simplePageBean.required}");
		UIBoundBoolean.make(form, "question-prerequisite", "#{simplePageBean.prerequisite}");
		UIInput.make(form, "question-text-input", "#{simplePageBean.questionText}");
		UIInput.make(form, "question-answer-full-shortanswer", "#{simplePageBean.questionAnswer}");
		
		UIBoundBoolean.make(form, "question-graded", "#{simplePageBean.graded}");
		UIInput.make(form, "question-gradebook-title", "#{simplePageBean.gradebookTitle}");
		UIInput.make(form, "question-max", "#{simplePageBean.maxPoints}");
		
		UIInput.make(form, "question-multiplechoice-answer-complete", "#{simplePageBean.addAnswerData}");
		UIInput.make(form, "question-multiplechoice-answer-id", null);
		UIBoundBoolean.make(form, "question-multiplechoice-answer-correct");
		UIInput.make(form, "question-multiplechoice-answer", null);
		UIBoundBoolean.make(form, "question-show-poll", "#{simplePageBean.questionShowPoll}");
		
		UIInput.make(form, "question-correct-text", "#{simplePageBean.questionCorrectText}");
		UIInput.make(form, "question-incorrect-text", "#{simplePageBean.questionIncorrectText}");
		UIInput.make(form, "question-addBefore", "#{simplePageBean.addBefore}");
		
		UICommand.make(form, "delete-question-item", messageLocator.getMessage("simplepage.delete"), "#{simplePageBean.deleteItem}");
		UICommand.make(form, "update-question", messageLocator.getMessage("simplepage.edit"), "#{simplePageBean.updateQuestion}");
		UICommand.make(form, "cancel-question", messageLocator.getMessage("simplepage.cancel"), null);
	}

	private void createDeleteItemDialog(UIContainer tofill, SimplePage currentPage) {
		UIForm form = UIForm.make(tofill, "delete-item-form");
		makeCsrf(form, "csrf22");
		UIInput.make(form, "delete-item-itemid", "#{simplePageBean.itemId}");
		UICommand.make(form, "delete-item-button", "#{simplePageBean.deleteItem}");
	}

	private void createColumnDialog(UIContainer tofill, SimplePage currentPage) {
		UIForm form = UIForm.make(tofill, "column-dialog-form");
		UICommand.make(form, "column-submit", messageLocator.getMessage("simplepage.save"), null);
		UICommand.make(form, "column-cancel", messageLocator.getMessage("simplepage.cancel"), null);
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
	
	/**
	 * Returns a Status object with the status of a user's response to a question.
	 * For showing status images next to the question.
	 */
	private Status getQuestionStatus(SimplePageItem question, SimplePageQuestionResponse response) {
		String questionType = question.getAttribute("questionType");
		boolean noSpecifiedAnswers = false;
		boolean manuallyGraded = false;

		if ("multipleChoice".equals(questionType) &&
		    !simplePageToolDao.hasCorrectAnswer(question))
		    noSpecifiedAnswers = true;
		else if ("shortanswer".equals(questionType) &&
			 "".equals(question.getAttribute("questionAnswer")))
		    noSpecifiedAnswers = true;

		if (noSpecifiedAnswers && "true".equals(question.getAttribute("questionGraded")))
		    manuallyGraded = true;

		if (noSpecifiedAnswers && !manuallyGraded) {
		    // poll. should we show completed if not required? Don't for
		    // other item types, but here there's no separate tool where you
		    // can look at the status. I'm currently showing completed, to
		    // be consistent with non-polls, where I always show a result
		    if (response != null)
			return Status.COMPLETED;
		    if(question.isRequired())
			return Status.REQUIRED;
		    return Status.NOT_REQUIRED;
		}

		if (manuallyGraded && (response != null && !response.isOverridden())) {
			return Status.NEEDSGRADING;
		} else if (response != null && response.isCorrect()) {
			return Status.COMPLETED;
		} else if (response != null && !response.isCorrect()) {
			return Status.FAILED;			
		}else if(question.isRequired()) {
			return Status.REQUIRED;
		}else {
			return Status.NOT_REQUIRED;
		}
	}

        String getStatusNote(Status status) {
	    if (status == Status.COMPLETED)
		return messageLocator.getMessage("simplepage.status.completed");
	    else if (status == Status.REQUIRED)
		return messageLocator.getMessage("simplepage.status.required");
	    else if (status == Status.NEEDSGRADING)
		return messageLocator.getMessage("simplepage.status.needsgrading");
	    else if (status == Status.FAILED)
		return messageLocator.getMessage("simplepage.status.failed");
	    else 
		return null;
	}	    

	// add the checkmark or asterisk. This code supports a couple of other
	// statuses that we
	// never ended up using
	private void addStatusImage(Status status, UIContainer container, String imageId, String name) {
		String imagePath = "/lessonbuilder-tool/images/";
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
		} else if (status == Status.NEEDSGRADING) {
			imagePath += "blue-question.png";
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

	private String getLocalizedURL(String fileName, boolean useDefault) {

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

		// if user specified, we make up an absolute URL
		// otherwise use one relative to the servlet context
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
			// actual URL will be related to templates
			defaultPath = "/lessonbuilder-tool/templates/instructions/" + fileName;
			prefix = "/lessonbuilder-tool/templates/instructions/" + prefix;
			// but have to test relative to servlet base
			testPrefix = "";  // urlok will have to remove /lessonbuilder-tool
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

		if (useDefault)
		    return defaultPath;

		// no localized version available
		return null;

	}

    // this can be either a fully specified URL starting with http: or https: 
    // or something relative to the servlet base, e.g. /lessonbuilder-tool/template/instructions/general.html
	private boolean UrlOk(String url) {
		String origurl = url;
		Boolean cached = (Boolean) urlCache.get(url);
		if (cached != null)
		    return (boolean) cached;

		if (url.startsWith("http:") || url.startsWith("https:")) {
		    // actual URL, check it out

		    try {
			HttpURLConnection.setFollowRedirects(false);
			HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
			con.setRequestMethod("HEAD");
			con.setConnectTimeout(30 * 1000);
			boolean ret = (con.getResponseCode() == HttpURLConnection.HTTP_OK);
			urlCache.put(origurl, (Boolean) ret);
			return ret;
		    } catch (java.net.SocketTimeoutException e) {
			log.error("Internationalization url lookup timed out for " + url + ": Please check lessonbuilder.helpfolder. It appears that the host specified is not responding.");
			urlCache.put(origurl, (Boolean) false);
			return false;
		    } catch (ProtocolException e) {
			urlCache.put(origurl, (Boolean) false);
			return false;
		    } catch (IOException e) {
			urlCache.put(origurl, (Boolean) false);
			return false;
		    }
		} else {
		    // remove the leading /lessonbuilder-tool, since getresource is
		    // relative to the top of the servlet
		    int i = url.indexOf("/", 1);
		    url = url.substring(i);
		    try {
			// inside the war file, check the file system. That avoid issues
			// with odd deployments behind load balancers, where the user's URL may not
			// work from one of the front ends
			if (httpServletRequest.getSession().getServletContext().getResource(url) == null) {
			    urlCache.put(origurl, (Boolean) false);
			    return false;
			} else {
			    urlCache.put(origurl, (Boolean) true);
			    return true;
			}
		    } catch (Exception e) {  // probably malfformed url
			log.error("Internationalization url lookup failed for " + url + ": " + e);
			urlCache.put(origurl, (Boolean) true);
			return true;
		    }

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

	private boolean printedGradingForm = false;
	private void printGradingForm(UIContainer tofill) {
		// Ajax grading form so faculty can grade comments
		if(!printedGradingForm) {
			UIForm gradingForm = UIForm.make(tofill, "gradingForm");
			gradingForm.viewparams = new SimpleViewParameters(UVBProducer.VIEW_ID);
			UIInput idInput = UIInput.make(gradingForm, "gradingForm-id", "gradingBean.id");
			UIInput jsIdInput = UIInput.make(gradingForm, "gradingForm-jsId", "gradingBean.jsId");
			UIInput pointsInput = UIInput.make(gradingForm, "gradingForm-points", "gradingBean.points");
			UIInput typeInput = UIInput.make(gradingForm, "gradingForm-type", "gradingBean.type");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			UIInput csrfInput = UIInput.make(gradingForm, "csrf", "gradingBean.csrfToken", (sessionToken == null ? "" : sessionToken.toString()));
			UIInitBlock.make(tofill, "gradingForm-init", "initGradingForm", new Object[] {idInput, pointsInput, jsIdInput, typeInput, csrfInput, "gradingBean.results"});
			printedGradingForm = true;
		}
	}

	private boolean saveChecklistFormNeeded = false;
	private void makeSaveChecklistForm(UIContainer tofill) {
		// Ajax grading form so faculty can grade comments
		if(!saveChecklistFormNeeded) {
			UIForm saveChecklistForm = UIForm.make(tofill, "saveChecklistForm");
			saveChecklistForm.viewparams = new SimpleViewParameters(UVBProducer.VIEW_ID);
			UIInput checklistIdInput = UIInput.make(saveChecklistForm, "saveChecklistForm-checklistId", "checklistBean.checklistId");
			UIInput checklistItemIdInput = UIInput.make(saveChecklistForm, "saveChecklistForm-checklistItemIdInput", "checklistBean.checklistItemId");
			UIInput checklistItemDone = UIInput.make(saveChecklistForm, "saveChecklistForm-checklistItemDone", "checklistBean.checklistItemDone");
			Object sessionToken = SessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
			String sessionTokenString = null;
			if (sessionToken != null)
			    sessionTokenString = sessionToken.toString();
			UIInput checklistCsrfInput = UIInput.make(saveChecklistForm, "saveChecklistForm-csrf", "checklistBean.csrfToken", sessionTokenString);

			UIInitBlock.make(tofill, "saveChecklistForm-init", "checklistAjax.initSaveChecklistForm", new Object[] {checklistIdInput, checklistItemIdInput, checklistItemDone, checklistCsrfInput, "checklistBean.results"});
			saveChecklistFormNeeded = true;
		}
	}
	
	private String getItemPath(SimplePageItem i)
	{

	    // users seem to want paths for the embedded items, so they can see what's going on
	        if (i.getType() == SimplePageItem.MULTIMEDIA) {
		    String mmDisplayType = i.getAttribute("multimediaDisplayType");
		    if ("".equals(mmDisplayType) || "2".equals(mmDisplayType))
			mmDisplayType = null;
		    if ("1".equals(mmDisplayType)) {
			// embed code
			return FormattedText.escapeHtml(i.getAttribute("multimediaEmbedCode"),false);
		    } else if ("3".equals(mmDisplayType)) {
			// oembed
			return FormattedText.escapeHtml(i.getAttribute("multimediaUrl"),false);
		    } else if ("4".equals(mmDisplayType)) {
			// iframe
			return FormattedText.escapeHtml(i.getItemURL(simplePageBean.getCurrentSiteId(),simplePageBean.getCurrentPage().getOwner()),false);
		    }
		}		

		String itemPath = "";
		boolean isURL = false;
		String pathId = i.getType() == SimplePageItem.MULTIMEDIA ? "path-url":"path-url";
		String[] itemPathTokens = i.getSakaiId().split("/");
		for(int tokenIndex=3 ; tokenIndex < itemPathTokens.length ; tokenIndex++)
		{
			if(isURL)
			{
				itemPath+= "/<a target=\"_blank\" href=\"\" class=\"" + URLEncoder.encode(pathId) + "\">" + FormattedText.escapeHtml(itemPathTokens[tokenIndex],false) + "</a>";
				isURL = false;
			}
			else
			    itemPath+="/" + FormattedText.escapeHtml(itemPathTokens[tokenIndex],false);
			
			isURL = itemPathTokens[tokenIndex].equals("urls") ? true: false;
		}
		return itemPath;
	}
	
	//Output rubric data for a Student Content box. 
	private String[] makeStudentRubric  = {null, "peer-eval-row-student:", "peerReviewIdStudent", "peerReviewTextStudent", "peer-eval-row-data", "#{simplePageBean.rubricPeerGrade}"};
	private String[] makeMaintainRubric = {"peer-eval-title", 		 "peer-eval-row:", 		  "peerReviewId", 		 "peerReviewText", null, null};
	
	private void makePeerRubric(UIContainer parent, SimplePageItem i, String[] peerReviewRsfIds, Map<Long,Integer> selectedCells, Map<Long, Map<Integer, Integer>> dataMap, boolean allowSubmit)
	{
		//log.info("makePeerRubric(): i.getAttributesString() " + i.getAttributeString());
		//log.info("makePeerRubric(): i.getAttribute(\"rubricTitle\") " + i.getAttribute("rubricTitle"));
		//log.info("makePeerRubric(): i.getJsonAttribute(\"rows\") " + i.getJsonAttribute("rows"));
		
		if (peerReviewRsfIds[0] != null)
		    UIOutput.make(parent, peerReviewRsfIds[0], String.valueOf(i.getAttribute("rubricTitle")));
		
		class RubricRow implements Comparable{
			public Long id;
			public String text;
			public RubricRow(Long id, String text){ this.id=id; this.text=text;}
			public int compareTo(Object o){
				RubricRow r = (RubricRow)o;
				if(id==r.id)
					return 0;
				if(id>r.id)
					return 1;
				return -1;
			}
		}
		
		ArrayList<RubricRow> rows = new ArrayList<RubricRow>();
		List categories = (List) i.getJsonAttribute("rows");
		if(categories != null){
			for(Object o: categories){
				Map cat = (Map)o;
				Long rowId = Long.parseLong(String.valueOf(cat.get("id")));
				String rowText = String.valueOf(cat.get("rowText"));
				rows.add(new RubricRow(rowId, rowText));
			}
		}
		//else{log.info("This rubric has no rows.");}
		
		Collections.sort(rows);

		for(RubricRow row : rows){
			UIBranchContainer peerReviewRows = UIBranchContainer.make(parent, peerReviewRsfIds[1]);
			UIOutput.make(peerReviewRows, peerReviewRsfIds[2], String.valueOf(row.id));
			UIOutput.make(peerReviewRows, peerReviewRsfIds[3], row.text);
			if (allowSubmit && peerReviewRsfIds[4] != null)
			    UIInput.make(peerReviewRows, peerReviewRsfIds[4], peerReviewRsfIds[5]);
			if (selectedCells != null) {
			    for (int col = 4; col >= 0; col--) {
				String count = null;
				if (dataMap != null) {
				    Map<Integer, Integer>rowMap = dataMap.get(row.id);
				    if (rowMap != null && rowMap.get(col) != null)
					count = rowMap.get(col).toString();
				}

				UIComponent cell = UIOutput.make(peerReviewRows, "peer-eval-cell:", count);
				Integer selectedValue = selectedCells.get(row.id);

				if (selectedValue != null && selectedValue == col)
				    cell.decorate(new UIStyleDecorator("selectedPeerCell " + col));
				else
				    cell.decorate(new UIStyleDecorator("" + col));
			    }						  
			}			    
		}
	}
	
	private int colCount(List<SimplePageItem> items, long item) {
	    // if item = we're at beginning. start counting immediately
	    boolean found = (item == 0);
	    int cols = 1;
	    for (SimplePageItem i: items) {
		if (i.getId() == item) {
		    String width = i.getAttribute("colwidth");
		    if (width != null)
			cols += (new Integer(width)) - 1;
		    found = true;
		    continue;
		}
		if (found && i.getType() == SimplePageItem.BREAK) {
		    if ("column".equals(i.getFormat())) {
			cols++;
			String width = i.getAttribute("colwidth");
			if (width != null)
			    cols += (new Integer(width)) - 1;
		    } else // section break; in next section. we're done
			break;
		}
	    }
	    return cols;
	}

	private void makeSamplePeerEval(UIContainer parent)
	{
		UIOutput.make(parent, "peer-eval-sample-title", messageLocator.getMessage("simplepage.peer-eval.sample.title"));
		
		UIBranchContainer peerReviewRows = UIBranchContainer.make(parent, "peer-eval-sample-data:");
		UIOutput.make(peerReviewRows, "peer-eval-sample-id", "1");
		UIOutput.make(peerReviewRows, "peer-eval-sample-text", messageLocator.getMessage("simplepage.peer-eval.sample.1"));
		
		peerReviewRows = UIBranchContainer.make(parent, "peer-eval-sample-data:");
		UIOutput.make(peerReviewRows, "peer-eval-sample-id", "2");
		UIOutput.make(peerReviewRows, "peer-eval-sample-text", messageLocator.getMessage("simplepage.peer-eval.sample.2"));
		
		peerReviewRows = UIBranchContainer.make(parent, "peer-eval-sample-data:");
		UIOutput.make(peerReviewRows, "peer-eval-sample-id", "3");
		UIOutput.make(peerReviewRows, "peer-eval-sample-text", messageLocator.getMessage("simplepage.peer-eval.sample.3"));
		
		peerReviewRows = UIBranchContainer.make(parent, "peer-eval-sample-data:");
		UIOutput.make(peerReviewRows, "peer-eval-sample-id", "4");
		UIOutput.make(peerReviewRows, "peer-eval-sample-text", messageLocator.getMessage("simplepage.peer-eval.sample.4"));
	}

}
