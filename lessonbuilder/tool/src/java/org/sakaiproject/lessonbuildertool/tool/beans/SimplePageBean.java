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

import com.opencsv.CSVParser;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.authz.api.AuthzGroup;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.authz.api.Member;
import org.sakaiproject.authz.api.SecurityAdvisor;
import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.lti.util.SakaiLTIUtil;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.archive.api.ArchiveService;
import org.sakaiproject.condition.api.ConditionService;
import org.sakaiproject.condition.api.model.Condition;
import org.sakaiproject.condition.api.model.ConditionType;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.ContentTypeImageService;
import org.sakaiproject.content.api.FilePickerHelper;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.db.api.SqlService;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.event.api.NotificationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.id.api.IdManager;
import org.sakaiproject.lessonbuildertool.SimpleChecklistItem;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageComment;
import org.sakaiproject.lessonbuildertool.SimplePageGroup;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageItemImpl;
import org.sakaiproject.lessonbuildertool.SimplePageLogEntry;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEval;
import org.sakaiproject.lessonbuildertool.SimplePagePeerEvalResult;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswer;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionResponse;
import org.sakaiproject.lessonbuildertool.SimpleStudentPage;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderConstants;
import org.sakaiproject.lessonbuildertool.api.LessonBuilderEvents;
import org.sakaiproject.lessonbuildertool.cc.CartridgeLoader;
import org.sakaiproject.lessonbuildertool.cc.Parser;
import org.sakaiproject.lessonbuildertool.cc.PrintHandler;
import org.sakaiproject.lessonbuildertool.cc.ZipLoader;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.AjaxServer;
import org.sakaiproject.lessonbuildertool.service.AssignmentEntity;
import org.sakaiproject.lessonbuildertool.service.BltiInterface;
import org.sakaiproject.lessonbuildertool.service.GradebookIfc;
import org.sakaiproject.lessonbuildertool.service.GroupPermissionsService;
import org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService;
import org.sakaiproject.lessonbuildertool.service.LessonBuilderEntityProducer;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;
import org.sakaiproject.lessonbuildertool.service.LessonSubmission;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.tool.beans.helpers.ResourceHelper;
import org.sakaiproject.lessonbuildertool.tool.beans.helpers.SubpageBulkEditHelper;
import org.sakaiproject.lessonbuildertool.tool.producers.PagePickerProducer;
import org.sakaiproject.lessonbuildertool.tool.producers.ShowItemProducer;
import org.sakaiproject.lessonbuildertool.tool.producers.ShowPageProducer;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.memory.api.Cache;
import org.sakaiproject.memory.api.MemoryService;
import org.sakaiproject.portal.util.ToolUtils;
import org.sakaiproject.grading.api.ConflictingAssignmentNameException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tasks.api.Priorities;
import org.sakaiproject.tasks.api.Task;
import org.sakaiproject.tasks.api.TaskService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.api.UserDirectoryService;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;
import org.sakaiproject.util.api.FormattedText;
import org.sakaiproject.util.comparator.AlphaNumericComparator;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.tsugi.lti.ContentItem;
import org.tsugi.lti.LTIUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInternalLink;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
@Slf4j
public class SimplePageBean {
	public static final int CACHE_MAX_ENTRIES = 5000;
	public static final int CACHE_TIME_TO_LIVE_SECONDS = 600;
	public static final int CACHE_TIME_TO_IDLE_SECONDS = 360;

	public enum Status {
	    NOT_REQUIRED, REQUIRED, DISABLED, COMPLETED, FAILED, NEEDSGRADING
	}
	
        // from ResourceProperites. This isn't in 2.7.1, so define it here. Let's hope it doesn't change...
        public static final String PROP_ALLOW_INLINE = "SAKAI:allow_inline";

	public final Integer FILTER_DEFAULT=0;
	public final Integer FILTER_HIGH=1;
	public final Integer FILTER_LOW=2;
	public final Integer FILTER_NONE=3;

	public static final Pattern YOUTUBE_PATTERN = Pattern.compile("v[=/_]([\\w-]{11}([\\?\\&][\\w\\.\\=\\&]*)?)");
	public static final Pattern YOUTUBE2_PATTERN = Pattern.compile("embed/([\\w-]{11}([\\?\\&][\\w\\.\\=\\&]*)?)");
	public static final Pattern SHORT_YOUTUBE_PATTERN = Pattern.compile("([\\w-]{11}([\\?\\&][\\w\\.\\=\\&]*)?)");
	public static final String GRADES[] = { "A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "E", "F" };
	public static final String FILTERHTML = "lessonbuilder.filterhtml";
	public static final String LESSONBUILDER_ITEMID = "lessonbuilder.itemid";
	public static final String LESSONBUILDER_ADDBEFORE = "sakai.addbefore";
	public static final String LESSONBUILDER_ITEMNAME = "lessonbuilder.itemname";
	public static final String LESSONBUILDER_PATH = "lessonbuilder.path";
	public static final String LESSONBUILDER_BACKPATH = "lessonbuilder.backpath";
	public static final String CALENDAR_TOOL_ID = "sakai.schedule";
	public static final String TWITTER_WIDGET_DEFAULT_HEIGHT = "300";
	public static final String ANNOUNCEMENTS_TOOL_ID = "sakai.announcements";
	public static final String FORUMS_TOOL_ID = "sakai.forums";
	public static final String GRADEBOOK_TOOL_ID = "sakai.gradebookng";
	public static final String GRADEBOOK_CLASSIC_TOOL_ID = "sakai.gradebook.tool";

	private static String PAGE = "simplepage.page";
	private String contents = null;
	private String pageTitle = null;
	private String newPageTitle = null;
	private String subpageTitle = null;
	private boolean subpageNext = false;
	private boolean subpageButton = false;
	private String csrfToken = null;

	private List<Long> currentPath = null;
	private Set<Long> allowedPages = null;    

	private Site currentSite = null; // cache, can be null; used by getCurrentSite

	private List<GroupEntry> currentGroups = null;
	private Set<String> myGroups = null;

	private String filterHtml = ServerConfigurationService.getString(FILTERHTML);

	public String selectedAssignment = null;
	public String selectedBlti = null;

    // generic entity stuff. selectedEntity is the string
    // coming from the picker. We'll use the same variable for any entity type
	public String selectedEntity = null;
	public String selectedSite = null;
	public String[] selectedEntities = new String[] {};
	public String[] selectedGroups = new String[] {};
	public String[] studentSelectedGroups = new String[] {};

	public String selectedQuiz = null;

	public String[] selectedChecklistItems = new String[] {};

	private Map<Long, SubpageBulkEditHelper> subpageBulkEditTitleMap = new HashMap<>();
	public String subpageBulkEditJson = null;
	
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
	public int multimediaDisplayType = 0;
	public String multimediaMimeType = null;

	public String commentsId;
	public boolean anonymous;
	public String comment;
	public String formattedComment;
	public String editId;
	public boolean graded, sGraded;
	public String gradebookTitle;
	public String maxPoints, sMaxPoints;
	
	public boolean comments;
	public boolean forcedAnon;
	public boolean groupOwned;
	public boolean groupOwnedIndividual;
	public boolean seeOnlyOwn;
    
	public String questionType;
    public String questionText, questionCorrectText, questionIncorrectText;
    public String questionAnswer;
    public Boolean questionShowPoll;
    private HashMap<Integer, String> questionAnswers = null;
    
    public Long questionId;
    public String questionResponse;
	
	public boolean isWebsite = false;
	public boolean isCaption = false;

	private String linkUrl;

	private String height, width;

	@Setter private Boolean importArchive = false;

	private String description;
	private String name;
	private String names;
	private boolean required;
	private boolean replacefile;
	private boolean subrequirement;
	private boolean prerequisite;
	private boolean newWindow;
	private String dropDown;
	private String points;
	private String mimetype;
	
    // for BLTI, values window, inline, and null for in a new page with navigation
    // but sameWindow should also be set properly, based on the format
	private String format;

	private boolean nameHidden;

	private String numberOfPages;
	private boolean copyPage;

	private String indentLevel;
	private String customCssClass;
	private String buttonColor;
	private boolean forceButtonColor = false;

	private String alt = null;
	private String order = null;

	private String youtubeURL;
	private String mmUrl;
	private long youtubeId;

	private boolean hidePage;
	private boolean hidePageFromNavigation;
	private String visibilityChoice = "visible";
	private String visibilityChoice2 = "visible";
	private Date releaseDate;
	private boolean hasReleaseDate;
	private boolean nodownloads;
	private String addBefore;  // add new item before this item
    
	private String redirectSendingPage = null;
	private String redirectViewId = null;
	private String quiztool = null;
	private String topictool = null;
	private String assigntool = null;
	private boolean importtop = false;
	
	private Integer editPrivs = null;
	private String currentSiteId = null;

	public Map<String, MultipartFile> multipartMap;

	private HashMap<Long, String> checklistItems = new HashMap<>();
	
	public String rubricSelections;
	
	public boolean peerEval;
	public String rubricTitle;
	public String rubricRow;
	private HashMap<Integer, String> rubricRows = null;
	
	//variables used for forum-summary setting
	private String forumSummaryHeight;
	private String forumSummaryDropDown;
	private Date peerEvalDueDate;
	private Date peerEvalOpenDate;
	private boolean peerEvalAllowSelfGrade;
	private String folderPath;
	private String newOwner;
	//variables used for announcements widget
	private String announcementsHeight;
	private String announcementsDropdown;
	//variables used for twitter setting used in the widget
	private String twitterDropDown;
	private String twitterUsername;
	private String twitterWidgetHeight;

	private String layoutSectionTitle;
	private boolean layoutSectionCollapsible = false;
	private boolean layoutSectionStartCollapsed = false;
	private boolean layoutSectionShowBorders = true;
	private String layoutSelect;
	private String pageLayoutSelect;
	private String pageSubpageTitle;
	private boolean pageSubpageButton;
	private int pageSubpageCount;
	private int pageTaskCount;
	private boolean pageTaskCollapsible;
	private String pageButtonColorScheme;
	private String pageColorScheme;
	private boolean pageTaskClosed;
	private String layoutColorScheme;

	public static final String NewColors[] = {
			"none",
			"ngray",
			"nblack",
			"nblue",
			"nblue2",
			"nred",
			"nnavy",
			"nnavy2",
			"ngreen",
			"norange",
			"ngold",
			"nteal",
			"npurple"
	};
	public static final String NewColorLabels[] = {
			"Default",
			"Gray",
			"Black",
			"Blue",
			"Blue Dark",
			"Red",
			"Navy",
			"Navy Dark",
			"Green",
			"Orange",
			"Gold",
			"Teal",
			"Purple"
	};
	private String[] newColorLabelsI18n = null;
	public String[] getNewColorLabelsI18n() {
		if (newColorLabelsI18n == null) {
			newColorLabelsI18n = new String[]{messageLocator.getMessage("simplepage.columnnone"), messageLocator.getMessage("simplepage.col.newgray"), messageLocator.getMessage("simplepage.col.newblack"), messageLocator.getMessage("simplepage.col.newblue"), messageLocator.getMessage("simplepage.col.newblue2"), messageLocator.getMessage("simplepage.col.newred"), messageLocator.getMessage("simplepage.col.newnavy"), messageLocator.getMessage("simplepage.col.newnavy2"), messageLocator.getMessage("simplepage.col.newgreen"), messageLocator.getMessage("simplepage.col.neworange"), messageLocator.getMessage("simplepage.col.newgold"), messageLocator.getMessage("simplepage.col.newteal"), messageLocator.getMessage("simplepage.col.newpurple")};
		}
		return newColorLabelsI18n;
	}
	public static final String pageLayoutValues[] = {"none", "addSubpageList", "interiorResources", "interiorTask"};
	public static final String FAILURE = "failure";
	public static final String FORCE_BTN = "forceBtn";
	public static final String SECTION = "section";
    public static final String COL_WIDTH = "colwidth";
	public static final String INTERIOR_RESOURCES = "interiorResources";
	public static final String COLUMN = "column";
	public static final String COL_COLOR = "colcolor";
	public static final String CHECKLIST_ITEMS = "checklistItems";
	public static final String BTN_COLOR = "btnColor";
	public static final String CUSTOM_CSS_CLASS = "customCssClass";
	public static final String COLLAPSIBLE = "collapsible";
	public static final String BG_SUCCESS = "bg-success";
	public static final String INDENT_LEVEL = "indentLevel";
	public static final String NAME_HIDDEN = "namehidden";
	public static final String DEFAULT_CLOSED = "defaultHidden";
	public static final String BUTTON = "button";
	public static final String INTERIOR_TASK = "interiorTask";
	public static final String ADD_SUBPAGE_LIST = "addSubpageList";
	public static final String SUCCESS = "success";
	public static final String PREFIX_URL = "/url/";

        // SAK-41846 - Counters to adjust item sequences when multiple files are added simultaneously
        private int totalMultimediaFilesToAdd = 0;
        private int remainingMultimediaFilesToAdd = 0;
        
     // Spring Injection

    @Setter private SessionManager sessionManager;
    @Setter private ContentHostingService contentHostingService;
    @Setter private GradebookIfc gradebookIfc = null;
    @Setter private AssignmentService assignmentService;
    @Setter private ToolManager toolManager;
    @Setter private LTIService ltiService;
    @Setter private SecurityService securityService;
    @Setter private SiteService siteService;
    @Setter private AuthzGroupService authzGroupService;
    @Getter @Setter private SimplePageToolDao simplePageToolDao;
    @Setter private LessonsAccess lessonsAccess;
    @Setter private LessonBuilderAccessService lessonBuilderAccessService;
    @Getter @Setter private MessageLocator messageLocator;
    @Setter private HttpServletResponse httpServletResponse;
    @Setter private LessonBuilderEntityProducer lessonBuilderEntityProducer;
    @Setter private SqlService sqlService;
    @Setter private ContentTypeImageService contentTypeImageService;
    @Setter private EventTrackingService eventTrackingService;
    @Setter private NotificationService notificationService;
    @Setter private IdManager idManager;
    @Setter private UserDirectoryService userDirectoryService;
    @Setter private FormattedText formattedText;
    @Setter private UserTimeService userTimeService;
    @Setter private ConditionService conditionService;
    @Getter @Setter private TaskService taskService;
    @Getter @Setter private ArchiveService archiveService;

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

        // End Injection

        DateFormat isoDateFormat;

	public void setPeerEval(boolean peerEval) {
		this.peerEval = peerEval;
	}

	public String getNewOwner() {
		return newOwner;
	}

	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}

	public void setRubricTitle(String rubricTitle) {
		this.rubricTitle = rubricTitle;
	}
	
	public void setRubricRow(String rubricRow) {

		if (StringUtils.isBlank(rubricRow)) {
			return;
		}
		this.rubricRow = rubricRow;
		
		if(rubricRows==null) {
			rubricRows = new HashMap<>();
		}
		rubricRows.put(rubricRows.size(), rubricRow);
	}
	
	public Date getPeerEvalDueDate () {
		return peerEvalDueDate;
	}
	
        // format comes back as 2014-05-27T16:15:00-04:00
        // if user's computer is on a different time zone, we want the UI to match 
        // Sakai. Hence we really want to handle everything as local time.
        // That means we want to ignore the time zone on input
	public void setPeerEvalDueDate(String date){
	    try {
		date = date.substring(0,19);
		this.peerEvalDueDate = isoDateFormat.parse(date);
	    } catch (Exception e) {
		log.info("{} bad format duedate {}", e, date);
	    }
	}
	
	public Date getPeerEvalOpenDate() {
		return peerEvalOpenDate;
	}
	
	public void setPeerEvalOpenDate(String date) {
	    try {
		date = date.substring(0,19);
		this.peerEvalOpenDate = isoDateFormat.parse(date);
	    } catch (Exception e) {
		log.info("{} bad format duedate {}", e, date);
	    }
	}
	
	public boolean getPeerEvalAllowSelfGrade(){
		return peerEvalAllowSelfGrade;
	}
	
	public void setPeerEvalAllowSelfGrade(boolean self){
		this.peerEvalAllowSelfGrade = self;
	}
	ArrayList<String> rubricPeerGrades;
	public String rubricPeerGrade;
	
	public void setRubricPeerGrade(String rubricPeerGrade) {
		if (StringUtils.isBlank(rubricPeerGrade))
		    return;
		this.rubricPeerGrade = rubricPeerGrade;
		
		if(rubricPeerGrades == null) {
			rubricPeerGrades = new ArrayList<>();
		}
		rubricPeerGrades.add(rubricPeerGrade);
	}
	public String getFolderPath() {
		return folderPath;
	}

	public void setFolderPath(String folderPath) {
		this.folderPath = folderPath;
	}

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

	private Map<Long, SimplePageItem> itemCache = new HashMap<> ();
	private Map<Long, SimplePage> pageCache = new HashMap<> ();
	private Map<Long, List<SimplePageItem>> itemsCache = new HashMap<> ();
	private Map<String, SimplePageLogEntry> logCache = new HashMap<>();
	private Map<Long, Boolean> completeCache = new HashMap<>();
	private Map<Long, Boolean> visibleCache = new HashMap<>();
	// this one needs to be global
	static MemoryService memoryService = (MemoryService)org.sakaiproject.component.cover.ComponentManager.get("org.sakaiproject.memory.api.MemoryService");
	private static Cache<String, Object> groupCache = memoryService.getCache("org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.groupCache");  // itemId => grouplist
	private static Cache<String, List> resourceCache = memoryService.getCache("org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.resourceCache");
	protected static final int DEFAULT_EXPIRATION = 10 * 60;

	public static class PathEntry {
		public Long pageId;
		public Long pageItemId;
		public String title;
	}

	public static class UrlItem {
		public String Url;
		public String label;
		public String description;
		public String fa_icon = null;
		public Boolean search = Boolean.FALSE;
		public UrlItem(String Url, String label) {
			this.Url = Url;
			this.label = label;
		}
		public UrlItem(String Url, String label, String description, String fa_icon, Boolean search) {
			this.Url = Url;
			this.label = label;
			this.description = description;
			this.fa_icon = fa_icon;
			this.search = search;
		}
	}

	public static class GroupEntry {
	    public String name;
	    public String id;
	}

	public static class BltiTool {
	    public int id;
	    public String title;
	    public String description; // can be null
	    public String addText;
	    public String addInstructions; // can be null
	}

	public static Map<Integer,BltiTool> bltiTools = initBltiTools();

	public static Map<Integer,BltiTool> initBltiTools() {
	    String[] bltiToolLines = ServerConfigurationService.getStrings("lessonbuilder.blti_tools");
	    if (bltiToolLines == null || bltiToolLines.length == 0)
		return null;
	    CSVParser csvParser = new CSVParser();
	    Map<Integer,BltiTool> ret = new HashMap<>();
	    for (int i = 0; i < bltiToolLines.length; i++) {
		String[] items;
		try {
		    items = csvParser.parseLine(bltiToolLines[i]);
		} catch (Exception e) {
		    log.info("bad blti tool spec in lessonbuilder.blti_tools {} {}", i, bltiToolLines[i]);
		    continue;
		}
		if (items.length < 5) {
		    log.info("bad blti tool spec in lessonbuilder.blti_tools {} {}", i, bltiToolLines[i]);
		    continue;
		}
		BltiTool bltiTool = new BltiTool();
		try {
		    bltiTool.id = Integer.parseInt(items[0]);
		} catch (Exception e) {
		    log.info("first item in line not integer in lessonbuilder.blti_tools {} {}", i, bltiToolLines[i]);
		    continue;
		}
		if (items[1] == null || items[1].length() == 0) {
		    log.info("second item in line missing in lessonbuilder.blti_tools {} {}", i, bltiToolLines[i]);
		    continue;
		}
		bltiTool.title = items[1];
		// allow null but not zero length
		if (items[2] == null || items[2].length() == 0)
		    bltiTool.description = null;
		else
		    bltiTool.description = items[2];
		if (items[3] == null || items[3].length() == 0) {
		    log.info("third item in line missing in lessonbuilder.blti_tools {} {}", i, bltiToolLines[i]);
		    continue;
		}
		bltiTool.addText = items[3];
		// allow null but not zero length
		if (items[4] == null || items[4].length() == 0)
		    bltiTool.addInstructions = null;
		else
		    bltiTool.addInstructions = items[4];
		ret.put(bltiTool.id, bltiTool);
	    }
	    for (BltiTool tool: ret.values()) {
		log.info("{} {} {} {}", tool.id, tool.title, tool.description, tool.addText);
	    }
	    return ret;
	}

	public BltiTool getBltiTool(int i) {
	    if (bltiTools == null)
		return null;
	    return bltiTools.get(i);
	}

	public Collection<BltiTool> getBltiTools() {
	    if (bltiTools == null)
		return null;
	    return bltiTools.values();
	}

    // Image types

	public static ArrayList<String> imageTypes;

	static {
		imageTypes = new ArrayList<>();
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

	private static final String DEFAULT_HTML_TYPES = "html,xhtml,htm,xht";
	private static String[] htmlTypes = null;

        static {
	    String mmTypes = ServerConfigurationService.getString("lessonbuilder.html.types", DEFAULT_HTML_TYPES);
	    htmlTypes = mmTypes.split(",");
	    for (int i = 0; i < htmlTypes.length; i++) {
		htmlTypes[i] = htmlTypes[i].trim().toLowerCase();
	    }
	    Arrays.sort(htmlTypes);
	}



 	/**
 	 *  almost ISO format. real thing can't be done until Java 7. uses -0400 rather than -04:00
     *  DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
 	 * @return DateFormat
 	 */
 	private DateFormat getIsoDateFormat() {
 	    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 	    TimeZone tz = userTimeService.getLocalTimeZone();
 	    format.setTimeZone(tz);
 	    return format;
 	}

	// Don't put things here. It isn't always called.
	public void init () {
		isoDateFormat = getIsoDateFormat();
	}

	static PagePickerProducer pagePickerProducer = null;
       // need the bean, because findallpages uses a global that's in the class */
	public PagePickerProducer pagePickerProducer() {
	    if (pagePickerProducer == null) {
		pagePickerProducer = new PagePickerProducer();
		pagePickerProducer.setSimplePageBean(this);
		pagePickerProducer.setSimplePageToolDao(simplePageToolDao);
	    }
	    return pagePickerProducer;
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

    // findStudentPage for current user
    // Calls appropriate Dao code depending upon whether it's controlled by group or individual owner.
    // by putting it here rather than in the Dao we can use caching for all the objects.
    // This is used student-side so optimiztion is important.

        public SimpleStudentPage findStudentPage(SimplePageItem item) {
	    if (item.isGroupOwned()) {
		Set<String> myGroups = getMyGroups();
		return simplePageToolDao.findStudentPage(item.getId(), myGroups);
	    } else {
		return simplePageToolDao.findStudentPage(item.getId(), getCurrentUserId());
	    }
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
		    log.info("Lesson Builder error not in tool: {}", s);
		    return;
		}
		List<String> errors = (List<String>)toolSession.getAttribute("lessonbuilder.errors");
		if (errors == null)
		    errors = new ArrayList<>();
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

	public void setNameHidden(boolean nameHidden) {
		this.nameHidden = nameHidden;
	}

	public boolean getNameHidden() {
		if (itemId != null && itemId != -1) {
			return Boolean.valueOf(findItem(itemId).getAttribute(SimplePageItem.NAMEHIDDEN));
		} else {
			return false;
		}
	}

	public String getIndentLevel() {
		if (itemId != null && itemId != -1) {
			return findItem(itemId).getAttribute(SimplePageItem.INDENT);
		} else {
			// default is zero
			return "0";
		}
	}

	public void setIndentLevel(String indentLevel) {
		this.indentLevel = indentLevel;
	}

	public String getCustomCssClass() {
		return customCssClass;
	}

	public void setCustomCssClass(String customCssClass) {
		this.customCssClass = customCssClass;
	}

	public String getButtonColor() {
		return buttonColor;
	}

	public void setButtonColor(String buttonColor) {
		this.buttonColor = buttonColor;
	}

	public boolean getHidePage() {
		SimplePage currentPage = getCurrentPage();
		if (currentPage != null) {
			return currentPage.isHidden();
		}
		return hidePage;
	}

	public void setHidePage(boolean hide) {
		hidePage = hide;
	}

	public boolean getHidePageFromNavigation() {
		SimplePage currentPage = getCurrentPage();
		if (currentPage != null) {
			return currentPage.isHiddenFromNavigation();
		}
		return hidePageFromNavigation;
	}

	public void setHidePageFromNavigation(boolean hidePageFromNavigation) {
		this.hidePageFromNavigation = hidePageFromNavigation;
	}

	public String getVisibilityChoice() {
		SimplePage currentPage = getCurrentPage();
		if (currentPage != null) {
			if (currentPage.isHidden()) {
				return "hide";
			} else if (currentPage.isHiddenFromNavigation()) {
				return "hideFromNav";
			}
		}
		return "visible";
	}

	public void setVisibilityChoice(String visibilityChoice) {
		if (visibilityChoice == null) {
			visibilityChoice = "visible";
		}

		this.visibilityChoice = visibilityChoice;

		this.hidePage = "hide".equals(visibilityChoice);
		this.hidePageFromNavigation = "hideFromNav".equals(visibilityChoice);
	}

	public String getVisibilityChoice2() {
		SimplePage currentPage = getCurrentPage();
		if (currentPage != null) {
			if (currentPage.isHidden()) {
				return "hide";
			} else if (currentPage.isHiddenFromNavigation()) {
				return "hideFromNav";
			}
		}
		return "visible";
	}

	public void setVisibilityChoice2(String visibilityChoice2) {
		// Handle null values gracefully
		if (visibilityChoice2 == null) {
			visibilityChoice2 = "visible";
		}
		
		this.visibilityChoice2 = visibilityChoice2;
		
		// Update the boolean flags based on the radio selection
		this.hidePage = "hide".equals(visibilityChoice2);
		this.hidePageFromNavigation = "hideFromNav".equals(visibilityChoice2);
	}

    // argument is in ISO8601 format, which has -04:00 time zone.
    // if user's computer is on a different time zone, we want the UI to match
    // Sakai. Hence we really want to handle everything as local time.
    // That means we want to ignore the time zone on input
    public void setReleaseDate(String date) {
        if (StringUtils.isBlank(date)) {
            this.releaseDate = null;
        } else {
            try {
                if(date.charAt(10) == ' '){	//the raw date that arrives from the datepicker's ISO8601 field might be missing a char [ex. "2019-08-03T13:45:00" is correct, "2019-08-03 13:45:00" is incorrect], so we will force it in here.
                    date = date.replace(' ', 'T');
                }
                date = date.substring(0,19);
                this.releaseDate = isoDateFormat.parse(date);
            } catch (Exception e) {
                log.warn("{}bad format releasedate {}", e, date);
                try {
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
                    TimeZone tz = userTimeService.getLocalTimeZone();
                    format.setTimeZone(tz);
                    this.releaseDate = format.parse(date);
                } catch (Exception e2) {
                    log.warn("{}bad format releasedate {}", e2, date);
                    this.releaseDate = null;
                }
            }
        }
    }

	public Date getReleaseDate() {
		return this.releaseDate;
	}

	public void setHasReleaseDate(boolean hasReleaseDate) {
		this.hasReleaseDate = hasReleaseDate;
	}

        // Can this method be safely commented out (in tandem with the nodownloads reference in applicationContext.xml)?
        // See LSNBLDR-816.
    	public void setNodownloads(boolean n) {
		this.nodownloads = n;
	} 

	public void setAddBefore(String n) {
		this.addBefore = n;
	}

        public void setImporttop(boolean i) {
	    this.importtop = i;
	}

    // gets called for non-checked boxes also, but q will be null
	public void setQuiztool(String q) {
	    if (q != null)
		quiztool = q;
	}

	public void setAssigntool(String q) {
	    if (q != null)
		assigntool = q;
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

	public void setNames(String names) {
		this.names = names;
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
	
	public void setReplacefile(boolean replacefile) {
		this.replacefile = replacefile;
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
	public void setTwitterDropDown(String twitterDropDown) {
		this.twitterDropDown = twitterDropDown;
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
	
	public void setMultimediaDisplayType(String type) {
	    if (type != null && !type.trim().equals("")) {
		try {
		    multimediaDisplayType = Integer.valueOf(type);
		} catch (Exception e) {}
	    }
	}

	public void setMultimediaMimeType(String type) {
	    multimediaMimeType = type;
	}

	public void setWebsite(boolean isWebsite) {
	    this.isWebsite = isWebsite;
	}

	public void setForumSummaryHeight(String forumSummaryHeight) {
		this.forumSummaryHeight = forumSummaryHeight;
	}

	public void setForumSummaryDropDown(String forumSummaryDropDown) {
		this.forumSummaryDropDown = forumSummaryDropDown;
	}

	public void setCaption(boolean isCaption) {
	    this.isCaption = isCaption;
	}
	public void setAnnouncementsHeight(String announcementsHeight) {
		this.announcementsHeight = announcementsHeight;
	}
	public void setAnnouncementsDropdown(String announcementsDropdown) {
		this.announcementsDropdown = announcementsDropdown;
	}

	public void setTwitterUsername(String twitterUsername) {
		this.twitterUsername = twitterUsername;
	}

	public void setTwitterWidgetHeight(String twitterWidgetHeight) {
		this.twitterWidgetHeight = twitterWidgetHeight;
	}


	public String getLayoutSectionTitle() {
		return layoutSectionTitle;
	}

	public void setLayoutSectionTitle(String layoutSectionTitle) {
		this.layoutSectionTitle = layoutSectionTitle;
	}

	public boolean isLayoutSectionCollapsible() {
		return layoutSectionCollapsible;
	}

	public void setLayoutSectionCollapsible(boolean layoutSectionCollapsible) {
		this.layoutSectionCollapsible = layoutSectionCollapsible;
	}

	public boolean isLayoutSectionStartCollapsed() {
		return layoutSectionStartCollapsed;
	}

	public void setLayoutSectionStartCollapsed(boolean layoutSectionStartCollapsed) {
		this.layoutSectionStartCollapsed = layoutSectionStartCollapsed;
	}

	public boolean isLayoutSectionShowBorders() {
		return layoutSectionShowBorders;
	}

	public void setLayoutSectionShowBorders(boolean layoutSectionShowBorders) {
		this.layoutSectionShowBorders = layoutSectionShowBorders;
	}

	public boolean isButtonColorForced(){
		return forceButtonColor;
	}

	public void setForceButtonColor(boolean forceButtonColor){
		this.forceButtonColor = forceButtonColor;
	}

	public String getLayoutSelect() {
		return layoutSelect;
	}

	public void setLayoutSelect(String layoutSelect) {
		this.layoutSelect = layoutSelect;
	}

	public void setPageLayoutSelect(String pageLayoutSelect) {
		this.pageLayoutSelect = pageLayoutSelect;
	}
	public String getPageLayoutSelect(){
		return pageLayoutSelect;
	}

	public void setPageSubpageTitle(String pageSubpageTitle){
		this.pageSubpageTitle = pageSubpageTitle;
	}
	public String getPageSubpageTitle(){
		return pageSubpageTitle;
	}

	public void setPageSubpageCount(int count){
		this.pageSubpageCount = count;
	}
	public int getPageSubpageCount(){
		return pageSubpageCount;
	}

	public void setPageTaskCount(int count){
		this.pageTaskCount = count;
	}
	public int getPageTaskCount(){
		return pageTaskCount;
	}

	public void setPageTaskCollapsible(boolean whether){
		this.pageTaskCollapsible = whether;
	}
	public boolean isPageTaskCollapsible(){
		return this.pageTaskCollapsible;
	}

	public void setPageTaskClosed(boolean whether){
		this.pageTaskClosed = whether;
	}
	public boolean isPageTaskClosed(){
		return pageTaskClosed;
	}

	public void setPageColorScheme(String pageColorScheme){
		this.pageColorScheme = pageColorScheme;
	}
	public String getPageColorScheme(){
		return pageColorScheme;
	}

	public void setPageButtonColorScheme(String pageButtonColorScheme){
		this.pageButtonColorScheme = pageButtonColorScheme;
	}
	public String getPageButtonColorScheme(){
		return pageButtonColorScheme;
	}

	public void setPageSubpageButton(boolean whether){
		this.pageSubpageButton = whether;
	}
	public boolean getPageSubpageButton(){
		return pageSubpageButton;
	}

	public String getLayoutColorScheme() {
		return layoutColorScheme;
	}

	public void setLayoutColorScheme(String layoutColorScheme) {
		this.layoutColorScheme = layoutColorScheme;
	}

    // hibernate interposes something between us and saveItem, and that proxy gets an
    // error after saveItem does. Thus we never see any value that saveItem might 
    // return. Hence we pass saveItem a list to which it adds the error message. If
    // there is a message from saveItem take precedence over the message we detect here,
    // since it's the root cause.
	public boolean saveItem(Object i, boolean requiresEditPermission) {       
		String err = null;
		List<String>elist = new ArrayList<>();
		
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
			setErrMessage(messageLocator.getMessage("simplepage.savefailed"));
			return false;
		}
		
		return true;
	}
	
	public boolean saveItem(Object i) {
		return saveItem(i, true);
	}
	
	public boolean saveOrUpdate(Object i) {
		String err = null;
		List<String>elist = new ArrayList<>();
		
		try {
			simplePageToolDao.saveOrUpdate(i,  elist, messageLocator.getMessage("simplepage.nowrite"), true);
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
			setErrMessage(messageLocator.getMessage("simplepage.savefailed"));
			return false;
		}
		
		return true;
	}

	public boolean update(Object i) {
		return update(i, true);
	}

    // see notes for saveupdate
	
	// requiresEditPermission determines whether simplePageToolDao should confirm
	// edit permissions before making the update
	boolean update(Object i, boolean requiresEditPermission) {       
		String err = null;
		List<String>elist = new ArrayList<>();
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
			setErrMessage(messageLocator.getMessage("simplepage.savefailed"));
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

	public boolean itemOk(Long itemId) {
		// not specified, we'll add a new one
		if (itemId == null || itemId == -1)
			return true;
		SimplePageItem item = findItem(itemId);
		
		return item != null ? item.getPageId() == getCurrentPageId() : true;
	}
	
	public Integer getFilterLevel(Placement placement) {
		if (placement == null) {
			placement = toolManager.getCurrentPlacement();
		}

	    Integer filter;
        if (getCurrentPage().getOwner() != null) {
            filter = FILTER_DEFAULT; // always filter student content
        } else {
            // this is instructor content.
            // see if specified
            String filterSpec = placement.getPlacementConfig().getProperty("filterHtml");
            if (filterSpec == null)
            filterSpec = filterHtml;
            // no, default to LOW. That will allow embedding but not Javascript
            if (filterSpec == null) // should never be null. unspeciifed should give ""
            filter = FILTER_DEFAULT;
            // old specifications
            else if (filterSpec.equalsIgnoreCase("true"))
            filter = FILTER_HIGH; // old value of true produced the same result as missing
            else if (filterSpec.equalsIgnoreCase("false"))
            filter = FILTER_NONE;
            // new ones
            else if (filterSpec.equalsIgnoreCase("default"))
            filter = FILTER_DEFAULT;
            else if (filterSpec.equalsIgnoreCase("high"))
            filter = FILTER_HIGH;
            else if (filterSpec.equalsIgnoreCase("low"))
            filter = FILTER_LOW;
            else if (filterSpec.equalsIgnoreCase("none"))
            filter = FILTER_NONE;
            // unspecified
            else
            filter = FILTER_DEFAULT;
        }
        return filter;
	}

    // called by the producer that uses FCK to update a text block
	public String submit() {
		String rv = "success";
		
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!checkCsrf())
		    return "permission-failed";
		
		if (canEditPage()) {
			Placement placement = toolManager.getCurrentPlacement();

// WARNING: keep in sync with code in AjaxFilter.java

			// there's an issue with HTML security in the Sakai community.
			// a lot of people feel users shouldn't be able to add javascript, etc
			// to their HTML. I think enforcing that makes Sakai less than useful.
			// So check config options to see whether to do that check

			String html;

			// figure out how to filter
			Integer filter = getFilterLevel(placement);

			if (filter.equals(FILTER_NONE)) {
			    html = formattedText.processHtmlDocument(contents, null);
			} else if (filter.equals(FILTER_DEFAULT)) {
			    html = formattedText.processFormattedText(contents, null, null);
			} else {
			    FormattedText.Level level = filter.equals(FILTER_HIGH)
				    ? FormattedText.Level.HIGH
				    : FormattedText.Level.LOW;
			    html = formattedText.processFormattedText(contents, null, level);
			}

// WARNING: keep in sync with code in AjaxFilter.java

			// if (getCurrentPage().getOwner() != null || filterHtml 
			//		&& !"false".equals(placement.getPlacementConfig().getProperty("filterHtml")) ||
			//		"true".equals(placement.getPlacementConfig().getProperty("filterHtml"))) {
			//	html = FormattedText.processFormattedText(contents, error);
			//} else {
			//	html = FormattedText.processHtmlDocument(contents, error);

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
				item.setPrerequisite(this.prerequisite);
				setItemGroups(item, selectedGroups);
				saveOrUpdate(item);
			} else {
				rv = "cancel";
			}
			// placement.save();

		} else {
			rv = "cancel";
		}

		return rv;
	}

	// called by the checklist producer
	// to add or update a checklist (simplepageitem)
	public String addChecklist() {

		if (!itemOk(itemId)) {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "permission-failed";
		}
		if (!checkCsrf())
		    return "permission-failed";
		if(!canEditPage()) {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "failure";
		}

		final String checklistName = StringUtils.trimToEmpty(name);
		if(checklistName.isEmpty()) {
			setErrMessage(messageLocator.getMessage("simplepage.checklist-empty-name"));
			return "failure";
		}

		SimplePageItem item;
		if (itemId != null && itemId != -1) {
			item = findItem(itemId);
		} else {
			// Adding a checklist to the page
			item = appendItem("", messageLocator.getMessage("simplepage.checklistName"), SimplePageItem.CHECKLIST);
		}

		item.setName(checklistName);
		item.setDescription(description);
		setItemGroups(item, selectedGroups);

		// Set the indent level for this item
		item.setAttribute(SimplePageItem.INDENT, indentLevel);

		// Set the custom css class
		item.setAttribute(SimplePageItem.CUSTOMCSSCLASS, customCssClass);

		item.setAttribute(SimplePageItem.BUTTONCOLOR, buttonColor);

		// Is the name hidden from students
		item.setAttribute(SimplePageItem.NAMEHIDDEN, String.valueOf(nameHidden));

		Long max = simplePageToolDao.maxChecklistItem(item);

		// Get existing item ids
		List<Long> previousIds = new ArrayList<>();
		for(SimpleChecklistItem checklistItem : simplePageToolDao.findChecklistItems(item)) {
			previousIds.add(checklistItem.getId());
		}

		simplePageToolDao.clearChecklistItems(item);

		final JSONParser jsonParser = new JSONParser();

		for(long i = 0; checklistItems.get(i) != null; i++) {
			// get data sent from post operation for this checklist item
			final String data = checklistItems.get(i);
			try {
				final JSONObject checklistItem = (JSONObject) jsonParser.parse(data);

				final String itemName = (String) checklistItem.get("text");

				// Don't save checklist items with a blank name
				if (StringUtils.isNotBlank(itemName)) {
					Long checklistItemId;
					if (StringUtils.isBlank((String) checklistItem.get("id"))) {
						checklistItemId = -1L;
					} else {
						checklistItemId = Long.valueOf((String) checklistItem.get("id"));
					}
					if (checklistItemId <= 0L) {
						checklistItemId = ++max;
					}
					Long linkedId;
					if (StringUtils.isBlank((String) checklistItem.get("link"))) {
						linkedId = -1L;
					} else {
						linkedId = Long.valueOf((String) checklistItem.get("link"));
					}
					// Remove checklistItemId from list of those to be cleaned up
					previousIds.remove(checklistItemId);
					simplePageToolDao.addChecklistItem(item, checklistItemId, itemName, linkedId);
				}
			} catch (ClassCastException e) {
				log.error("Parser returned a non-JSONObject. ", e);
			} catch (ParseException e) {
				log.error("ParseException occurred while trying to save checklist item.", e);
			}
		}

		// Delete all checklist item statuses for checklist items that no longer exist.
		if(!previousIds.isEmpty()) {
			for(Long checklistItemIdToDelete : previousIds) {
				simplePageToolDao.deleteAllSavedStatusesForChecklistItem(item.getId(), checklistItemIdToDelete);
			}
		}

		saveOrUpdate(item);

		return "success";
	}

	public void setAddChecklistItemData(String data) {
		if(StringUtils.isBlank(data)) {
			return;
		}

		final JSONParser parser = new JSONParser();

		try {
			final JSONObject checklistItem = (JSONObject) parser.parse(data);
			final Long index = (Long) checklistItem.get("index");

			// We store with the index so that we can maintain the order
			// in which the instructor inputted the checklist items
			checklistItems.put(index, data);

		} catch (ClassCastException e) {
			log.error("Parser returned a non-JSONObject. ", e);
			checklistItems = new HashMap<>();
		} catch (ParseException e) {
			checklistItems = new HashMap<>();
		}
	}

	public String cancel() {
		return "cancel";
	}

	public String processMultimedia() {
	    return processResource(SimplePageItem.MULTIMEDIA, false, false);
	}

	public String processResource() {
	    return processResource(SimplePageItem.RESOURCE, false, false);
	}

        public String processWebSite() {
	    return processResource(SimplePageItem.RESOURCE, true, false);
	}

        public String processCaption() {
	    return processResource(SimplePageItem.RESOURCE, false, true);
	}

    // get mime type for a URL. connect to the server hosting
    // it and ask them. Sorry, but I don't think there's a better way
	public String getTypeOfUrl(String url) {
	    String mimeType = "text/html";

	    // try to find the mime type of the remote resource
	    // this is only likely to be a problem if someone is pointing to
	    // a url within Sakai. We think in realistic cases those that are
	    // files will be handled as files, so anything that comes where
	    // will be HTML. That's the default if this fails.
	    URLConnection conn = null;
	    try {
		conn = new URL(new URL(ServerConfigurationService.getServerUrl()),url).openConnection();
		conn.setConnectTimeout(10000);
		conn.setReadTimeout(10000);
		// generate cookie based on code in  RequestFilter.java
		//String suffix = System.getProperty("sakai.serverId");
		//if (suffix == null || suffix.equals(""))
		//    suffix = "sakai";
		//Session s = sessionManager.getCurrentSession();
		//conn.setRequestProperty("Cookie", "JSESSIONID=" + s.getId() + "." + suffix);
		conn.connect();
		String t = conn.getContentType();
		if (StringUtils.isNotBlank(t)) {
		    int i = t.indexOf(";");
		    if (i >= 0)
			t = t.substring(0, i);
		    t = t.trim();
		    mimeType = t;
		}
	    } catch (Exception e) {
		log.error("getTypeOfUrl connection error {}", e.toString());
	    } finally {
		if (conn != null) {
		    try {
			conn.getInputStream().close();
		    } catch (Exception e) {
			log.error("getTypeOfUrl unable to close {}", e.toString());
		    }
		}
	    }
	    return mimeType;
	}

    // return call from the file picker, used by add resource
    // the picker communicates with us by session variables
	public String processResource(int type, boolean isWebSite, boolean isCaption) {
		if (!canEditPage())
		    return "permission-failed";

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		Long itemId = (Long)toolSession.getAttribute(LESSONBUILDER_ITEMID);
		addBefore = (String)toolSession.getAttribute(LESSONBUILDER_ADDBEFORE);
		name = (String)toolSession.getAttribute(LESSONBUILDER_ITEMNAME);
		toolSession.removeAttribute(LESSONBUILDER_ITEMID);
		toolSession.removeAttribute(LESSONBUILDER_ADDBEFORE);
		toolSession.removeAttribute(LESSONBUILDER_ITEMNAME);

		if (!itemOk(itemId))
		    return "permission-failed";

		// if itemId specified, better only be one resource, since we replacing an existing one

		List<Reference> refs;
		String returnMesssage = null;

		if (toolSession.getAttribute(FilePickerHelper.FILE_PICKER_CANCEL) == null && toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS) != null) {
			refs = (List) toolSession.getAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			//Changed 'refs.size != 1' to refs.isEmpty() as there can be multiple Resources
			// more than one is an error if replacing an existing one
			if (refs == null || refs.isEmpty())
				return "no-reference";
			// if item id specified, use first item only. Can't really return an error because of the way
			// the UI works
			if (itemId != null && itemId != -1)
				returnMesssage = processSingleResource(refs.get(0), type, isWebSite, isCaption, itemId);
			else {
				for(Reference reference : refs){
					itemsCache.remove(getCurrentPage().getPageId());
					returnMesssage = processSingleResource(reference, type, isWebSite, isCaption, itemId);
					name = null;  // only use name for first
			    }
			}
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);
		} else {
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_ATTACHMENTS);
			toolSession.removeAttribute(FilePickerHelper.FILE_PICKER_CANCEL);

			return "cancel";
		}

		return returnMesssage;
        }

	//This method is written to enable user to select multiple Resources from the tool
	private String processSingleResource(Reference reference,int type, boolean isWebSite, boolean isCaption, Long itemId){

		String id  = reference.getId();
		String description = reference.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
		String name = this.name; // user specified name overrides file name
		if (StringUtils.isBlank(name))
		    name = reference.getProperties().getProperty("DAV:displayname");

		// URLs are complex. There are two issues:
		// 1) The stupid helper treats a URL as a file upload. Have to make it a URL type.
		// I suspect we're intended to upload a file from the URL, but I don't think
		// any part of Sakai actually does that. So we reset Sakai's file type to URL
		// 2) Lesson builder needs to know the mime type, to know how to set up the
		// OBJECT or IFRAME. We send that out of band in the "html" field of the
		// lesson builder item entry. I see no way to do that other than to talk
		// to the server at the other end and see what MIME type it claims.
		String mimeType = reference.getProperties().getProperty("DAV:getcontenttype");
		if (mimeType.equals("text/url")) {
			mimeType = null; // use default rules if we can't find it
			String url = null;
			// part 1, fix up the type fields
			SecurityAdvisor editUrlAdvisor = null;
			try {
				editUrlAdvisor = pushAdvisor();
				ContentResourceEdit res = contentHostingService.editResource(id);
				res.setContentType("text/url");
				res.setResourceType("org.sakaiproject.content.types.urlResource");
				url = new String(res.getContent());
				contentHostingService.commitResource(res, notificationService.NOTI_NONE);
			} catch (Exception ignore) {
				return "no-reference";
			}finally {
				popAdvisor(editUrlAdvisor);
			}
			// part 2, find the actual data type.
			if (url != null)
				mimeType = getTypeOfUrl(url);
		} else if (isCaption) {
			// sakai probably sees it as a normal text file.
			// some browsers require the mime type to be right
			SecurityAdvisor editAdvisor = null;
			try {
				editAdvisor = pushAdvisor();
				ContentResourceEdit res = contentHostingService.editResource(id);
				res.setContentType("text/vtt");
				contentHostingService.commitResource(res, notificationService.NOTI_NONE);
			} catch (Exception ignore) {
				return "no-reference";
			}finally {
				popAdvisor(editAdvisor);
			}
		}

		try {
		    // I don't think we want the user adding anything he doesn't have access to
		    // accessservice depends upon that
			contentHostingService.checkResource(id);
		} catch (PermissionException e) {
			return "permission-exception";
		} catch (IdUnusedException e) {
			// Typically Means Cancel
			return "cancel";
		} catch (TypeException e) {
			return "type-exception";
		}

		String[] split = id.split("/");

		if("application/zip".equals(mimeType) && isWebSite) {
		    if (split[split.length-1].lastIndexOf(".") < 1) {
			// no extension. kernel code will fail
			setErrMessage(messageLocator.getMessage("simplepage.website.noextension"));
			// the problem with failing is that they won't see the error message. So return OK.
			return "importing";
		    }

		    // We need to set the sakaiId to the resource id of the index file
		    id = expandZippedResource(id);
		    if (id == null)
			return "importing"; // nothing left to do
		    
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
		if (itemId != null && itemId != -1 && isCaption) {
			// existing item, add or change caption
			i = findItem(itemId);
			i.setAttribute("captionfile", id);

		} else if (itemId != null && itemId != -1) {  // updating existing item
			i = findItem(itemId);
			
			// editing an existing item which might have customized properties
			// retrieve the original resource and check for customizations
			ResourceHelper resHelp = new ResourceHelper(getContentResource(i.getSakaiId()));
			boolean hasCustomName = !isWebsite && resHelp.isNameCustom(i.getName());  // ignore website names for now
			boolean hasCustomDesc = resHelp.isDescCustom(i.getDescription());
			
			i.setSakaiId(id);
			if (mimeType != null)
				i.setHtml(mimeType);
			if (!hasCustomName)
			{
				i.setName(name != null ? name : split[split.length - 1]);
			}
			if (!hasCustomDesc)
			{
				i.setDescription(description);
			}
			clearImageSize(i);
			// with a new underlying file, it's hard to see how an old caption file
			// could still be valid
			i.removeAttribute("captionfile");
			
			i.removeAttribute("multimediaUrl");
			
		} else {  // adding new item
			i = appendItem(id, (name != null ? name : split[split.length - 1]), type);
			if (mimeType != null) {
				i.setHtml(mimeType);
			}
			i.setDescription(description);
			i.setSameWindow(false);
		}
		
		i.setAttribute("addedby", getCurrentUserId());
		saveOrUpdate(i);

		return "importing";
	}
	
	private ContentResource getContentResource(String id)
	{
		ContentResource res = null;
		SecurityAdvisor pushed = null;
		try
		{
			pushed = pushAdvisor();
			res = contentHostingService.getResource(id);
		}
		catch (PermissionException | IdUnusedException | TypeException ex)
		{
			// ignore
		}
		finally
		{
			popAdvisor(pushed);
		}
		
		return res;
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
	        // minimize race conditions for sequence numbers by making sure we fetch items
	        // we're going to update directly from the database
		simplePageToolDao.setRefreshMode();
	        List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());
		// ideally the following should be the same, but there can be odd cases. So be safe
		long before = 0;
		String beforeStr = addBefore;
		boolean addAfter = false;
		if (beforeStr != null && beforeStr.startsWith("-")) {
		    addAfter = true;
		    beforeStr = beforeStr.substring(1);
		}
		if (StringUtils.isNotBlank(beforeStr)) {
		    try {
			before = Long.parseLong(beforeStr);
		    } catch (Exception e) {
			// nothing. ignore bad arg
		    }
		}

		// we have an item id. insert before it
		int nseq = 0;  // sequence number of new item
		boolean after = false; // we found the item to insert before
		if (before > 0) {
		    // have an item number specified, look for the item to insert before
		    for (SimplePageItem item: items) {
			if (item.getId() == before) {
				//if Comments Section item, find the biggest sequence
				//and assign nseq to be after it for the new item
				if(isCommentsItem(item)) {
					nseq = findNextSequence(items);
				}
				//for all the other regular items
				else {
					// found item to insert before
					// use its sequence and bump up it and all after
					nseq = item.getSequence();
					after = true;
					if (addAfter) {
						nseq++;
						continue;
					}
				}
			}
			if (after && item.getPageId() >= 0) {
			    item.setSequence(item.getSequence() + 1);
			    simplePageToolDao.quickUpdate(item);
			}
		    }			    
		}

		// if after not set, we didn't find the item; either no item specified or it
		// isn't on the page
		if (!after) {
		    nseq = items.size();
		    if (nseq > 0) {
			int seq = items.get(nseq-1).getSequence();
			if (seq > nseq)
			    nseq = seq;
		    }
		    nseq++;
		}
		    
		SimplePageItem i = simplePageToolDao.makeItem(getCurrentPageId(), nseq, type, id, name);

		// defaults to a fixed width and height, appropriate for some things, but for an
		// image, leave it blank, since browser will then use the native size
		clearImageSize(i);

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		toolSession.setAttribute("lessonbuilder.newitem", ""+i.getId()); 

		return i;
	}

	/**
	 * isPageOwner(page)
	 *
	 * if it's a student page and currernt user is owner or in owning group
	 *
	 **/

        // There's a copy in LessonsAccess for use by services. Keep them in sync
	public boolean isPageOwner(SimplePage page) {
	    String owner = page.getOwner();
	    String group = page.getGroup();
	    if (group != null)
		group = "/site/" + page.getSiteId() + "/group/" + group;
	    if (owner == null)
		return false;
	    if (group == null)
		return owner.equals(getCurrentUserId());
	    else
		return authzGroupService.getUserRole(getCurrentUserId(), group) != null;

	}

	public boolean isPageOwner(SimpleStudentPage page) {
	    String owner = page.getOwner();
	    String group = page.getGroup();
	    if (group != null)
		group = "/site/" + getCurrentSiteId() + "/group/" + group;
	    if (owner == null)
		return false;
	    if (group == null)
		return owner.equals(getCurrentUserId());
	    else
		return authzGroupService.getUserRole(getCurrentUserId(), group) != null;

	}

	/**
	 * Returns 0 if user has site.upd or simplepage.upd.
	 * Returns 1 if user is page owner
	 * Returns 2 otherwise
	 * @return
	 */
        // There's a copy of canEditPage in LessonsAccess for use by services. Keep them in sync
	public int getEditPrivs() {
		if (currentPage!=null && currentPage.getOwner()!=null && currentUserId!=null && currentPage.getOwner().equals(currentUserId)){
			editPrivs = 0;
			return editPrivs;
		}
		if(editPrivs != null) {
			return editPrivs;
		}
		editPrivs = 2;
		String ref = "/site/" + getCurrentSiteId();
		boolean ok = securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
		if(ok) editPrivs = 0;

		SimplePage page = getCurrentPage();
		if(editPrivs != 0 && page != null && isPageOwner(page)) {
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
		return getEditPrivs() <= 1;
	}

	public boolean canReadPage() {
		String ref = "/site/" + getCurrentSiteId();
		return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_READ, ref);
	}

	public boolean canEditSite() {
		String ref = "/site/" + getCurrentSiteId();
		return securityService.unlock("site.upd", ref);
	}

	public boolean canSeeAll() {
	    if (canEditPage())
		return true;
	    String ref = "/site/" + getCurrentSiteId();
	    return securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_SEE_ALL, ref);
	}

	public List<SimplePageItem>  getItemsOnPage(long pageid) {
		List<SimplePageItem>items = itemsCache.get(pageid);
		if (items != null)
		    return items;

		items = simplePageToolDao.findItemsOnPage(pageid);

		// This code adds a global comments tool to the bottom of each
		// student page, but only if there's something else on the page
		// already and the instructor has enabled the option.
		//   For some reason these are added to the beginning. In ShowPageProducer
		// they are moved to the end. Beacuse that reverses the order, put peer first
		// here in order to get it last. We need to check whether we can't just put
		// them at the end in the first place.
		if(items.size() > 0) {
			SimplePage page = getPage(pageid);
				if(isStudentPage(page)) {
				SimpleStudentPage student = simplePageToolDao.findStudentPage(page.getTopParent());
				if(student != null && student.getCommentsSection() != null) {
					SimplePageItem item = simplePageToolDao.findItem(student.getItemId());
					if(item != null && item.getShowPeerEval() != null && item.getShowPeerEval()) {
						String peerEval=item.getAttributeString();
						SimplePageItem studItem =  new SimplePageItemImpl();
						studItem.setSakaiId(page.getTopParent().toString());

						studItem.setAttributeString(peerEval);
						studItem.setGroupOwned(item.isGroupOwned());
						studItem.setName("peerEval");
						studItem.setPageId(-10L);
						studItem.setType(SimplePageItem.PEEREVAL); // peer eval defined in SimplePageItem.java
						studItem.setId(item.getId());
						items.add(0,studItem);
					}
					if(item != null && item.getShowComments() != null && item.getShowComments()) {
						//copy the attribute string from the top student section page  to each student page
						SimplePageItem commentsSection = simplePageToolDao.findItem(student.getCommentsSection());
						if(commentsSection != null) {
							items.add(0, commentsSection);
						}
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
		if (!itemOk(itemId) || !canEditPage()) {
		    return "permission-failed";
		}
		if (!checkCsrf())
		    return "permission-failed";

		SimplePageItem i = findItem(itemId);
		if(i == null) {
			log.warn("deleteItem: null item.  id: {}", itemId);
			return "failure";
		}

		return deleteItem(i);
	}

	public String deleteItem(SimplePageItem item) {
		return deleteItem(item, true);
	}

	public String deleteItem(SimplePageItem item, boolean updateSequence) {

		// if access controlled, clear it before deleting item
		if (item.isPrerequisite()) {
		    item.setPrerequisite(false);
		    checkControlGroup(item, false);
		}
		
		// Also delete gradebook entries
		if(item.getGradebookId() != null) {
			gradebookIfc.removeExternalAssessment(getCurrentSiteId(), item.getGradebookId());
		}
		
		if(item.getAltGradebook() != null) {
			gradebookIfc.removeExternalAssessment(getCurrentSiteId(), item.getAltGradebook());
		}

		// If SimplePageItem is a checklist delete all of the saved statuses
		if(item.getType() == SimplePageItem.CHECKLIST) {
			simplePageToolDao.deleteAllSavedStatusesForChecklist(item);
		}

		//delete corresponding response entries for question item
		if(item.getType() == SimplePageItem.QUESTION) {
			simplePageToolDao.deleteQuestionResponsesForItem(item);
		}

		//delete comment entries
		if(item.getType() == SimplePageItem.COMMENTS) {
			simplePageToolDao.deleteCommentsForLessonsItem(item);
		}

		// delete lessonsItem log
		simplePageToolDao.deleteLogForLessonsItem(item);

		boolean deleted = simplePageToolDao.deleteItem(item);

		// Delete task
		String reference = "/lessonbuilder/item/" + item.getId();
		taskService.removeTaskByReference(reference);

		if (updateSequence && deleted) {
			// minimize opening for race conditions on sequence number by forcing new fetches
			simplePageToolDao.setRefreshMode();

			int seq = item.getSequence();
			List<SimplePageItem> list = getItemsOnPage(getCurrentPageId());
			for (SimplePageItem it : list) {
				if (it.getSequence() > seq) {
					it.setSequence(it.getSequence() - 1);
					update(it);
				}
			}
		}

		// When a question item is deleted, remove assotiated Conditions
		if (deleted && item.getType() == SimplePageItem.QUESTION) {
			for (Condition condition : getItemConditions(item)) {
				conditionService.deleteCondition(condition.getId());
			}
		}

		if (deleted) {
			return "successDelete";
		} else {
			log.warn("deleteItem error deleting Item: {}", itemId);
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

	public Site getCurrentSite() {
		if (currentSite != null) // cached value
			return currentSite;
		
		try {
		    currentSite = siteService.getSite(getCurrentSiteId());
		} catch (Exception impossible) {
			log.error(impossible.getMessage(), impossible);
		}
		
		return currentSite;
	}

    // after someone else hacks on the site
	public void clearCurrentSite() {  
	    currentSite = null;
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
	    		view.setSendingPage(item.getPageId());
	    		// to the check. We need the check to set access control appropriately
	    		// if the user has passed.
	    		if (!isItemAvailable(nextItem, nextItem.getPageId()))
	    			view.setRecheck("true");
	    		view.viewID = ShowItemProducer.VIEW_ID;
	    	} else {
	    		view.setSendingPage(item.getPageId());

	    		// normally we won't send someone to an item that
	    		// isn't available. But if the current item is a test, etc, we can't
	    		// know whether the user will pass it, so we have to ask ShowItem to
	    		// to the check. We need the check to set access control appropriately
	    		// if the user has passed.
	    		if (!isItemAvailable(nextItem, nextItem.getPageId()))
	    			view.setRecheck("true");
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
		if (prevItem == null)
			return;
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
			view.setSendingPage(item.getPageId());
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
			view.setSendingPage(item.getPageId());
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
				log.warn("updatePageItem permission failure {} {} {}", i, Long.valueOf(i.getSakaiId()), currentPageId);
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
	public long getCurrentPageId()  {
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
		boolean siteHidden = (roleList != null && roleList.contains(SiteService.SECURE_UPDATE_SITE));

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

			// currentPage should now be set
			syncHidden(currentPage, siteHidden);

			return l;
		} else {
			// No recent activity. Let's go to the top level page.
			String placementPageId = ((ToolConfiguration) placement).getPageId();
			log.debug("Current placement.getId() {} placementPageId {}", placement.getId(), placementPageId);
			l = simplePageToolDao.getTopLevelPageId(placementPageId);
			log.debug("Top level page for placement {} is {}", placementPageId, l);

			if (l != null) {
				try {
					// TODO: A lot of things can go wrong here methinks -- Chuck
					updatePageObject(l);
					// this should exist except if the page was created by old code
					log.debug("findTopLevelPageItemBySakaiId {}",l);
					SimplePageItem i = simplePageToolDao.findTopLevelPageItemBySakaiId(String.valueOf(l));
					if (i == null) {
						// add vestigial item, the site is the notional top level page
						log.debug("no vestigial page found, making new item pageId {} title {}", l.toString(), currentPage.getTitle());
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
				log.debug("no page found, making new page toolId {} siteId {} title {}", toolId, getCurrentSiteId(), title);

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
					log.debug("Top level page not found, creating new vestigial item {} for page {} itemId {}", title, l, i.getId());
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
			if (ret != null && ((Long.toString(getCurrentPageId()).equals(ret.getSakaiId())) || ret.getType() == SimplePageItem.STUDENT_CONTENT)) {
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
		
		if(ret == null && isStudentPage(page)) {
			ret = simplePageToolDao.findItemFromStudentPage(page.getPageId());
		}
		if (ret == null)
		    return null;
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

		// Allow the new portal lessons subnav to push a subpage on the context
		if (op.equals("clear_and_push")) {
			// clear path for top level subpage
			path = new ArrayList<>();

			// add an entry for the parent page
			SimplePage parentPage = null;

			// the current page may have a parent
			if (currentPage.getParent() != null && currentPage.getParent() != 0) {
				parentPage = getPage(currentPage.getParent());

			// try and get it from the current item
			} else if (currentPageItemId != null) {
				SimplePageItem item = getCurrentPageItem(currentPageItemId);
				parentPage = getPage(item.getPageId());
			}

			PathEntry parentEntry = new PathEntry();
			parentEntry.pageItemId = -1L;
			if (parentPage == null) {
				// we tried our best, default these values so things don't break
				parentEntry.pageId = -1L;
				parentEntry.title = "";
			} else {
				parentEntry.pageId = parentPage.getPageId();
				parentEntry.title = parentPage.getTitle();
			}
			path.add(parentEntry);

			// add the subpage
			PathEntry entry = new PathEntry();
			entry.pageId = pageId;
			entry.pageItemId = pageItemId;
			entry.title = title;
			path.add(entry);  // put it on the end

		// if no current path, op doesn't matter. we can just do the current page
		} else if (path == null || path.isEmpty()) {
			PathEntry entry = new PathEntry();
			entry.pageId = pageId;
			entry.pageItemId = pageItemId;
			entry.title = title;
			path = new ArrayList<>();
			path.add(entry);
	    } else if (path.get(path.size()-1).pageId.equals(pageId)) {
	    	// nothing. we're already there. this is to prevent 
	    	// oddities if we refresh the page
	    } else if (StringUtils.equalsAny(op, "", "next")) {
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
	    	path = new ArrayList<>();
	    	SimplePageLogEntry logEntry = getLogEntry(pageItemId);
	    	if (logEntry != null) {
	    		String items[] = null;
	    		if (logEntry.getPath() != null)
				items = split(logEntry.getPath(), ",");
	    		if (items != null) {
	    			for(String s: items) {
	    				// don't see how this could happen, but it did
	    				if (StringUtils.isBlank(s)) {
	    					log.warn("adjustPath attempt to set invalid path: invalid item: {}:{}", op, logEntry.getPath());
	    					return null;
	    				}
	    				SimplePageItem i = findItem(Long.valueOf(s));
	    				if (i == null || i.getType() != SimplePageItem.PAGE) {
	    					log.warn("adjustPath attempt to set invalid path: invalid item: {}", op);
	    					return null;
	    				}
	    				SimplePage p = getPage(Long.valueOf(i.getSakaiId()));
	    				if (p == null || !currentPage.getSiteId().equals(p.getSiteId())) {
	    					log.warn("adjustPath attempt to set invalid path: invalid page: {}", op);
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
		backPath = new ArrayList<>();

	    // default case going directly to something.
	    // normally we want to push it, but if it's already there,
	    // we're going back to it, use the old one
	    if (StringUtils.isBlank(op)) {
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
		

	    if (StringUtils.equals(op, "pop")) {
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

	public void setCsrfToken(String s) {
	    csrfToken = s;
	}

    // called from "select page" dialog in Reorder to insert items from anoher page
	public String selectPage()   {

		if (!canEditPage())
		    return "permission-failed";
		if (!checkCsrf())
		    return "permission-failed";

		ToolSession toolSession = sessionManager.getCurrentToolSession();
		toolSession.setAttribute("lessonbuilder.selectedpage", selectedEntity);

		// doesn't do anything but call back reorder
		// the submit sets selectedEntity, which is passed to Reorder by addResultingViewBinding

		return "selectpage";
	}

	public String selectSite(){
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		toolSession.setAttribute("lessonbuilder.selectedsite", selectedSite);
		toolSession.setAttribute("lessonbuilder.loadFromSite", "loadFromSite");	//indicate that Load Pages From This Site has been clicked
		return "selectsite";
	}

    // called from "add subpage" dialog
    // create if itemId == null or -1, else update existing
	public String createSubpage()   {

		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";
		if (!checkCsrf())
		    return "permission-failed";

		String title = subpageTitle;
		
		boolean makeNewPage = (selectedEntity == null || selectedEntity.length() == 0);
		boolean makeNewItem = (itemId == null || itemId == -1);

		// make sure the page is legit
		if (!makeNewPage) {
		    SimplePage p = getPage(Long.valueOf(selectedEntity));
		    if (p == null || !getCurrentSiteId().equals(p.getSiteId())) {
			log.warn("addpage tried to add invalid page: {}", selectedEntity);
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
		String group = page.getGroup();

		if (topParent == null) {
			topParent = parent;
		}

		String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
		SimplePage subpage;
		if (makeNewPage) {
		    subpage = simplePageToolDao.makePage(toolId, getCurrentSiteId(), title, parent, topParent);
		    subpage.setOwner(owner);
		    if(isStudentPage(page)){
		        subpage.setOwned(false);
		    } else {
		        subpage.setOwned(owner!=null);
		    }
		    subpage.setGroup(group);
		    saveItem(subpage);
		    selectedEntity = String.valueOf(subpage.getPageId());
		} else {
		    subpage = getPage(Long.valueOf(selectedEntity));
		}

		SimplePageItem i;
		if (makeNewItem)
		    i = appendItem(selectedEntity, subpage.getTitle(), SimplePageItem.PAGE);
		else {
		    i = findItem(itemId);
		   
		}
		if (i == null)
		    return "failure";

		if (makeNewItem) {
		    i.setNextPage(subpageNext);
		    if (subpageButton)
			i.setFormat("button");
		    else
			i.setFormat("");

		    if(StringUtils.isNotEmpty(buttonColor)){
		    	i.setAttribute("btnColor", buttonColor);
			}
		} else {
		    // when itemid is specified, we're changing pages for existing entry
		    i.setSakaiId(selectedEntity);
		    i.setName(subpage.getTitle());
		}

		saveOrUpdate(i);

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
	
	public OrphanPageFinder getOrphanFinder(String siteId) {
		return new OrphanPageFinder(siteId, simplePageToolDao, pagePickerProducer());
	}

	//This will be called from the UI
	public String deleteOrphanPages() {
	    if (getEditPrivs() != 0)
	    	return "permission-failed";
	    if (!checkCsrf())
	    	return "permission-failed";
	    return deleteOrphanPagesInternal();
	}

	//This is an internal call that expects you will check permissions before calling it
	//Public because it's accessed from entity producer
	public String deleteOrphanPagesInternal() {
	    OrphanPageFinder orphanFinder = getOrphanFinder(getCurrentSiteId());
	    selectedEntities = orphanFinder.getOrphanStringsIds();
	    deletePagesInternal();
	    return "success";
	}

	//External method for deleting pages for the tool CSRF protected
	public String deletePages() {
		if (getEditPrivs() != 0)
			return "permission-failed";
		if (!checkCsrf())
			return "permission-failed";
		return deletePagesInternal();
	}
	
	//Service method for deleting pages
	protected String deletePagesInternal() {
	    String siteId = getCurrentSiteId();
	    log.debug("Found {} pages to delete", selectedEntities.length);
	    for (int i = 0; i < selectedEntities.length; i++) {
		deletePage(siteId, Long.valueOf(selectedEntities[i]));
		if ((i % 10) == 0) {
		    // we've seen situations with a million orphan pages
		    // we don't want to leave those all in cache
		    simplePageToolDao.flush();
		    simplePageToolDao.clear();
		}
	    }
	    return "success";
	}


	public void deletePage(String siteId, Long pageId) {
		
	    SimplePage target = simplePageToolDao.getPage(pageId);
	    if (target != null) {
		if (!target.getSiteId().equals(siteId)) {
		    return;
		}
		// delete all the items on the page
		List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(target.getPageId());
		for (SimplePageItem item: items) {
		    // if access controlled, clear it before deleting item
		    if (item.isPrerequisite()) {
			item.setPrerequisite(false);
			// doesn't seem to use any internal data
			checkControlGroup(item, false);
		    }

		    // delete gradebook entries
		    if(item.getGradebookId() != null) {
			gradebookIfc.removeExternalAssessment(siteId, item.getGradebookId());
		    }
		    if(item.getAltGradebook() != null) {
			gradebookIfc.removeExternalAssessment(siteId, item.getAltGradebook());
		    }

		    //actually delete item
		    simplePageToolDao.deleteItem(item);

		}
		

		// remove from gradebook
		Double currentPoints = target.getGradebookPoints();
		if (currentPoints != null && currentPoints != 0.0)
		    gradebookIfc.removeExternalAssessment(siteId, "lesson-builder:" + pageId);
	    
		// remove fake item if it's top level. We won't see it if it's still active
		// so this means the user has removed it in site info
		SimplePageItem item = simplePageToolDao.findTopLevelPageItemBySakaiId(pageId+"");
		if (item != null)
		    simplePageToolDao.deleteItem(item);			
		
		// currently the UI doesn't allow you to kill top level pages until they have been
		// removed by site info, so we don't have to hack on the site pages
		
		// remove page
		simplePageToolDao.deleteItem(target);
	    }
	}

    //  remove a top-level page from the left margin. Does not actually delete it.
    //  this and addpages checks only edit page permission. should it check site.upd?
	public String removePage() {
		if (getEditPrivs() != 0) {
			return "permission-failed";
		}
		if (!checkCsrf())
		    return "permission-failed";
		
		//		if (removeId == 0)
		//		    removeId = getCurrentPageId();
		SimplePage page = getPage(removeId);
		
		if (page == null)
		    return "no-such-page";

		if(!isStudentPage(page)) {
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
		if (!checkCsrf())
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

			if (StringUtils.isNotBlank(points)) {
				i.setRequirementText(points);
			} else {
				i.setRequirementText(dropDown);
			}

			// Set the indent level for this item
			i.setAttribute(SimplePageItem.INDENT, indentLevel);

			// Set the custom css class
			i.setAttribute(SimplePageItem.CUSTOMCSSCLASS, customCssClass);

			i.setAttribute(SimplePageItem.BUTTONCOLOR, buttonColor);
			// currently we only display HTML in the same page
			if (i.getType() == SimplePageItem.RESOURCE)
			    i.setSameWindow(!newWindow);
			else
			    i.setSameWindow(false);

			if (i.getType() == SimplePageItem.BLTI) {
			    if (StringUtils.isBlank(format))
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

			if (i.getType() == SimplePageItem.PAGE) {
				SimplePage page = getPage(Long.valueOf(i.getSakaiId()));
				if (page != null) {
					page.setTitle(name);
					if (hasReleaseDate)
						page.setReleaseDate(releaseDate);
					else
						page.setReleaseDate(null);
					page.setHidden(hidePage);
					page.setHiddenFromNavigation(hidePageFromNavigation);
					update(page);
				}
			} else {
				checkControlGroup(i, i.isPrerequisite());
			}

			setItemGroups(i, selectedGroups);
			update(i);

			return "successEdit"; // Shouldn't reload page
		}
	}

    // Set access control for an item to the state requested by i.isPrerequisite().
    // This code should depend only upon isPrerequisite() in the item object, not the database,
    // because we call it when deleting or updating items, before saving them to the database.
    // The caller will update the item in the database, typically after this call
    //    correct is correct value, i.e whether it hsould be there or not
    // WARNING: with argument false is called by the service layer. Can't depend upon data in the bean
	public void checkControlGroup(SimplePageItem i, boolean correct) {
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

		String sakaiId = i.getSakaiId();
		// /sam_core/nnn isn't published. It can't have groups. When it is published
		// we do a fixup which will call this again to create the real groups
		if (sakaiId.equals(SimplePageItem.DUMMY) || sakaiId.startsWith("/sam_core/"))
		    return;

		SimplePageGroup group = simplePageToolDao.findGroup(i.getSakaiId());
		String ourGroupName;
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
					    lessonEntity = quizEntity.getEntity(i.getSakaiId(),this); break;
					case SimplePageItem.FORUM:
					    lessonEntity = forumEntity.getEntity(i.getSakaiId()); break;
					}
					if (lessonEntity != null) {
					    String groups = getItemGroupString (i, lessonEntity, true);
					    ourGroupName = messageLocator.getMessage("simplepage.access-group").replace("{}", getNameOfSakaiItem(i));
					    // backup in case group was created by old code
					    String oldGroupName = "Access: " + getNameOfSakaiItem(i);
					    // this can produce duplicate names. Searches are actually done based
					    // on entity reference, not title, so this is acceptable though confusing
					    // to users. But using object ID's for the name would be just as confusing.
					    if (!sqlService.getVendor().equals("mysql"))
						ourGroupName = utf8truncate(ourGroupName, 99);
					    else if (ourGroupName.length() > 99) 
						ourGroupName = ourGroupName.substring(0, 99);
					    String groupId = null;
					    try {
						securityService.pushAdvisor(siteUpdAdvisor);
						groupId = GroupPermissionsService.makeGroup(getCurrentPage().getSiteId(), ourGroupName, oldGroupName, i.getSakaiId(), this);
					    } finally {
						securityService.popAdvisor(siteUpdAdvisor);
					    }
					    saveItem(simplePageToolDao.makeGroup(i.getSakaiId(), groupId, groups, getCurrentPage().getSiteId()));

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
				    lessonEntity = quizEntity.getEntity(i.getSakaiId(),this); break;
				case SimplePageItem.FORUM:
				    lessonEntity = forumEntity.getEntity(i.getSakaiId()); break;
				}
				if (lessonEntity != null) {
				    String groups = group.getGroups();
				    List<String> groupList = null;
				    if (StringUtils.isNotBlank(groups))
					groupList = Arrays.asList(groups.split(","));
				    lessonEntity.setGroups(groupList);
				    simplePageToolDao.deleteItem(group);
				}
			    }
			}
		} catch (Exception ex) {
		    log.error(ex.getMessage(), ex);
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
			contentHostingService.commitResource(resEdit, notificationService.NOTI_NONE);
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
		    log.warn("getToolId 1 attempt to get tool config for {} failed. Tool missing from site?", tool);
		    return null;
		} catch (java.lang.NullPointerException e) {
		    log.warn("getToolId 2 attempt to get tool config for {} failed. Tool missing from site?", tool);
		    return null;
		}
	}

	public void updateCurrentPage(boolean requiresEditPermission) {
		update(currentPage, requiresEditPermission);
	}

	public List<PathEntry> getHierarchy() {
	    List<PathEntry> path = (List<PathEntry>)sessionManager.getCurrentToolSession().getAttribute(LESSONBUILDER_PATH);
	    if (path == null)
		return new ArrayList<>();

	    return path;
	}

	public void setSelectedAssignment(String selectedAssignment) {
		this.selectedAssignment = selectedAssignment;
	}

	public void setSelectedSite(String siteid){
		this.selectedSite = siteid;
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

        public boolean checkCsrf() {
	    Object sessionToken = sessionManager.getCurrentSession().getAttribute("sakai.csrf.token");
		return sessionToken != null && sessionToken.toString().equals(csrfToken);
	}

    // called by add forum dialog. Create a new item that points to a forum or
    // update an existing item, depending upon whether itemid is set
        public String addForum() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";
		if (!checkCsrf())
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

				    update(i);
				}
			    } else {
				// no, add new item
				i = appendItem(selectedEntity, selectedObject.getTitle(), SimplePageItem.FORUM);
				i.setDescription("");
				saveItem(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    log.error(ex.getMessage(), ex);
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
		df.setTimeZone(userTimeService.getLocalTimeZone());
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!canEditPage())
		    return "permission-failed";
		if (!checkCsrf())
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
				String ref = null;
				if (existing != null)
				    ref = existing.getReference();
				// if same quiz, nothing to do
				if ((existing == null) || !ref.equals(selectedAssignment)) {
				    // if access controlled, clear restriction from old assignment and add to new
				    if (i.isPrerequisite()) {
					if (existing !=  null) {
					    i.setPrerequisite(false);
					    checkControlGroup(i, false);
					}
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
				    // Because we don't update the due date when it changes, this raises more
				    // problems than it fixes. It's also done only for assignments and not tests
				    //	if (selectedObject.getDueDate() != null)
				    //	 i.setDescription("(" + messageLocator.getMessage("simplepage.due") + " " + df.format(selectedObject.getDueDate()) + ")");
				    //  else
				    // i.setDescription(null);

				    update(i);
				}
			    } else {
				// no, add new item
				i = appendItem(selectedAssignment, selectedObject.getTitle(), SimplePageItem.ASSIGNMENT);
				//if (selectedObject.getDueDate() != null)
				//  i.setDescription("(" + messageLocator.getMessage("simplepage.due") + " " + df.format(selectedObject.getDueDate()) + ")");
				//else
				i.setDescription(null);
				saveItem(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    log.error(ex.getMessage(), ex);
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
		if (!checkCsrf())
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
				String ref = null;
				if (existing != null)
				    ref = existing.getReference();
				// if same item, nothing to do
				if ((existing == null) || !ref.equals(selectedBlti)) {
				    // if access controlled, clear restriction from old assignment and add to new
				    // group access not used for BLTI items, so don't need the setcontrolgroup
				    // logic from other item types
				    i.setSakaiId(selectedBlti);
				    i.setName(selectedObject.getTitle());
				    if (StringUtils.isBlank(format))
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

				//Copy the LTI tool description to the item description.
				if(StringUtils.isNotBlank(description)){
					i.setDescription(description);
				}

				BltiInterface blti = (BltiInterface)bltiEntity.getEntity(selectedBlti);
				if (blti != null) {
				    int height = blti.frameSize();
				    if (height > 0)
					i.setHeight(Integer.toString(height));
				    else
					i.setHeight("");
				    if (StringUtils.isBlank(format))
					i.setFormat("");
				    else
					i.setFormat(format);
				}
				saveItem(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    log.error(ex.getMessage(), ex);
			    return "failure";
			} finally {
			    selectedBlti = null;
			}
		}
	}

    /// ShowPageProducers needs the item ID list anyway. So to avoid calling the underlying
    // code twice, we take that list and translate to titles, rather than calling
    // getItemGroups again
	public String getItemGroupTitles(String itemGroups, SimplePageItem item) {
		String ret = null;
		if (StringUtils.isNotBlank(itemGroups)) {

			List<String> groupNames = new ArrayList<>();
			for (String groupId : split(itemGroups, ",")) {
				Group group = getCurrentSite().getGroup(groupId);
				if (group != null && StringUtils.isNotBlank(group.getTitle())) {
					groupNames.add(group.getTitle());
				} else {
					groupNames.add(messageLocator.getMessage("simplepage.deleted-group"));
				}
			}
			groupNames.sort(new AlphaNumericComparator());
			ret = StringUtils.join(groupNames, ", ");
		}

		if (StringUtils.isBlank(ret) && item.isPrerequisite()) {
			return messageLocator.getMessage("simplepage.prerequisites_tag");
		} else if (item.isPrerequisite()) {
			return messageLocator.getMessage("simplepage.prerequisites_tag") + "; " + ret;
		}

		return ret;
	}

    private String getParentTitle(SimplePageItem item, boolean continuation, Set<Long> seen) {
        if (item != null) {
            // get parent item
            List<SimplePageItem> parentItems = simplePageToolDao.findItemsBySakaiId(Long.toString(item.getPageId()));
            if (!parentItems.isEmpty()) {
                SimplePageItem parent = parentItems.get(0);
                // skip if this parent was already seen, guard against infinite loop
                if (!seen.contains(parent.getId())) {
                    seen.add(parent.getId());
                    String title = parent.getName();
                    if (seen.size() > 1) {
                        title += continuation ? " (" + messageLocator.getMessage("simplepage.printall.continuation") + ") " : " > ";
                    }
                    return getParentTitle(parent, continuation, seen) + title;
                }
            }
        }
        return "";
    }

    public String getSubPagePath(SimplePageItem item, boolean subPageTitleContinue) {
        String subPageTitle = getParentTitle(item, subPageTitleContinue, new HashSet<>());
        return StringUtils.trimToNull(subPageTitle);
    }

    // too much existing code to convert to throw at the moment
        public String getItemGroupString (SimplePageItem i, LessonEntity entity, boolean nocache) {
	    String groups;
	    try {
		groups = getItemGroupStringOrErr (i, entity, nocache);
	    } catch (IdUnusedException exp) {
		// unfortunately some uses aren't user-visible, so it's this or
		// add error handling to all callers
		return "";
	    }
	    return groups;
	}

    // use this one in the future
        public String getItemGroupStringOrErr (SimplePageItem i, LessonEntity entity, boolean nocache)
	           throws IdUnusedException{
	    StringBuilder ret = new StringBuilder("");
	    Collection<String> groups;
	    // may throw IdUnUsed
	    groups = getItemGroups (i, entity, nocache);
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

	public String getItemOwnerGroupString (SimplePageItem i) {
	    String ret = i.getOwnerGroups();
	    if (ret == null)
		ret = "";
	    return ret;
	}

	public String getReleaseString(SimplePageItem i, Locale locale) {
		if (i.getType() == SimplePageItem.PAGE) {
			SimplePage page = getPage(Long.valueOf(i.getSakaiId()));
			if (page == null) {
				return null;
			}
			if (page.isHidden()) {
				return messageLocator.getMessage("simplepage.hiddenpage");
			}
			// for index of pages we need to show even out of date release dates
			if (page.getReleaseDate() != null ) {
				DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale);
				TimeZone tz = userTimeService.getLocalTimeZone();
				df.setTimeZone(tz);
				String releaseDate = df.format(page.getReleaseDate());
				if(Instant.now().isBefore(page.getReleaseDate().toInstant())){
					return messageLocator.getMessage("simplepage.pagenotreleased").replace("{}", releaseDate);
				} else{
					return messageLocator.getMessage("simplepage.pagereleased").replace("{}", releaseDate);
				}
			}
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

	    Collection<String> ret = new ArrayList<>();

	    if (!nocache && i.getType() != SimplePageItem.PAGE 
		         && i.getType() != SimplePageItem.TEXT
		         && i.getType() != SimplePageItem.CHECKLIST
		         && i.getType() != SimplePageItem.BLTI
		         && i.getType() != SimplePageItem.COMMENTS
		         && i.getType() != SimplePageItem.QUESTION
		         && i.getType() != SimplePageItem.TWITTER
			 && i.getType() != SimplePageItem.BREAK
		         && i.getType() != SimplePageItem.STUDENT_CONTENT
		         && i.getType() != SimplePageItem.CALENDAR) {
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
		   entity = quizEntity.getEntity(i.getSakaiId(),this); break;
	       case SimplePageItem.FORUM:
		   entity = forumEntity.getEntity(i.getSakaiId()); break;
	       case SimplePageItem.MULTIMEDIA:
		   String displayType = i.getAttribute("multimediaDisplayType");
		   if (StringUtils.equalsAny(displayType, "1", "3") || i.getAttribute("multimediaUrl") != null)
		       return getLBItemGroups(i); // for all native LB objects
		   else
		       return getResourceGroups(i, nocache);  // responsible for caching the result
	       case SimplePageItem.RESOURCE:
		   if (i.getAttribute("multimediaUrl") != null)
		       return getLBItemGroups(i); // for all native LB objects
		   return getResourceGroups(i, nocache);  // responsible for caching the result
		   // fall through: groups controlled by LB
	       // for the following items we don't have non-LB items so don't need itemunused
	       case SimplePageItem.BLTI: // Groups always managed in lessons
	       case SimplePageItem.TEXT:
	       case SimplePageItem.RESOURCE_FOLDER:
	       case SimplePageItem.CHECKLIST:
	       case SimplePageItem.PAGE:
	       case SimplePageItem.COMMENTS:
	       case SimplePageItem.QUESTION:
	       case SimplePageItem.TWITTER:
	       case SimplePageItem.STUDENT_CONTENT:
	       case SimplePageItem.CALENDAR:
		   return getLBItemGroups(i); // for all native LB objects
	       default:
	    	   return null;
	       }
	   }

	   // only here for object types with underlying entities
	   boolean exists = false;
	   SecurityAdvisor sa = null;
	   try {
	       sa = pushAdvisorAlways();  // assignments won't let the student look
	       if (entity != null)
		   exists = entity.objectExists();
	   } finally {
	       popAdvisor(sa);
	   }

	   if (!exists) {
	       throw new IdUnusedException(i.toString());
	   }

	   // in principle the groups are stored in a SimplePageGroup if we
	   // are doing access control, and in the tool if not. We can
	   // check that with i.isPrerequisite. However I'm concerned
	   // that if multiple items point to the same object, and some
	   // are set with prerequisite and some are not, that things
	   // could get out of kilter. So I'm going to use the
	   // SimplePageGroup if it exists, and the tool if not.

	   // this can be needed if the call to getEntity causes us to recognize
	   // a recently published test and replace the /sam_core with a /sam_pub
	   // in that case the entity is up to date but the sakaiid is not
	   if (i.getSakaiId().startsWith("/sam_core") && entity != null) {
	       i.setSakaiId(entity.getReference());
	   }


	   SimplePageGroup simplePageGroup = simplePageToolDao.findGroup(i.getSakaiId());
	   if (simplePageGroup != null) {
	       String groups = simplePageGroup.getGroups();
	       if (StringUtils.isNotBlank(groups))
		   ret = Arrays.asList(groups.split(","));
	   } else {
	       // not under our control, use list from tool
	       SecurityAdvisor advisor = null;
	       try {
		   advisor = pushAdvisorAlways();
		   ret = entity.getGroups(nocache); // assignments won't let a student see
	       } finally {
		   popAdvisor(advisor);
	       }
	   }

	   if (ret == null)
	       groupCache.put(i.getSakaiId(), "*");
	   else
	       groupCache.put(i.getSakaiId(), ret);

	   return ret;

       }

    // only makes sense for SimplePageItem.RESOURCE or .MULTIMEDIA
	public String getContentType(SimplePageItem item) {
	    String mimeType = item.getHtml();
	    // for files the code no longer stores the mimetype in lessons
	    // so if lessons doesn't have one, get it from the kernel
	    
	    // old code put URLs in html field. no legit types start with http
	    if (mimeType != null && (mimeType.startsWith("http")))
		mimeType = null;

	    if (StringUtils.isBlank(mimeType)) {
		String mmDisplayType = item.getAttribute("multimediaDisplayType");
		// 2 is the generic "use old display" so treat it as null
		// only do this for type 2, since that's where there's an actual file
		if (StringUtils.isBlank(mmDisplayType) || "2".equals(mmDisplayType)) {
		    try {
			ContentResource res = contentHostingService.getResource(item.getSakaiId());
			mimeType = res.getContentType();
		    } catch (Exception ignore) {
		    }
		}
	    }
	    if("application/octet-stream".equals(mimeType)) {
		// OS X reports octet stream for things like MS Excel documents.
		// Force a mimeType lookup so we get a decent icon.
		// Probably not needed for normal files, as kernel should sniff
		// correctly, but we may need it for URLs
		mimeType = null;
	    }

	    if (StringUtils.isBlank(mimeType)) {
		String s = item.getSakaiId();
		    if (s != null) {
		        int j = s.lastIndexOf(".");
		        if (j >= 0)
			        s = s.substring(j+1);
		        mimeType = contentTypeImageService.getContentType(s);
		        // log.info("type " + s + ">" + mimeType);
		    }
	    }

	    // if still nothing, call it octet-stream just so we don't return null
	    if (StringUtils.isBlank(mimeType))
		mimeType = "application/octet-stream";

	    return mimeType;
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

		   advisor = pushAdvisorAlways();
    	   
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
				  inherited = !(AccessMode.SITE.equals(access) && ! inheritingPubView);
    			   if (AccessMode.GROUPED.equals(access))
    				   groups = resource.getInheritedGroups();
    		   } else {
    			   // we can always change local modes, even if they are public
    			   inherited = false;
    			   if (AccessMode.GROUPED.equals(access))
    				   groups = resource.getGroups();
    		   }
    		   
    		   if (groups != null) {
    			   ret = new ArrayList<>();
    			   for (String group: groups) {
    				   int n = group.indexOf("/group/");
    				   ret.add(group.substring(n+7));
    			   }
    		   }
    		   
    		   if (!nocache) {
    			   if (ret == null)
    				   groupCache.put(i.getSakaiId(), "*");
    			   else
    				   groupCache.put(i.getSakaiId(), ret);
    		   }
    		   
    		   return ret;
    	   }finally {
    		  popAdvisor(advisor);
    	   }
   	}

    // no obvious need to cache
       public Collection<String> getLBItemGroups(SimplePageItem i) {

	   String groupString = i.getGroups();
	   if (StringUtils.isBlank(groupString)) {
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
	   if (isStudentPage(getCurrentPage())) {
	       return null;
	   }

	   //Remove the existing groups from the item cache before assigning the new groups.
	   if(i != null && StringUtils.isNotBlank(i.getSakaiId())){
	       log.info("Removing the item {} assigned groups from the cache.", i.getSakaiId());
	       groupCache.remove(i.getSakaiId());
	   }

	   LessonEntity lessonEntity = null;
	   switch (i.getType()) {
	   case SimplePageItem.ASSIGNMENT:
	       lessonEntity = assignmentEntity.getEntity(i.getSakaiId()); break;
	   case SimplePageItem.ASSESSMENT:
	       lessonEntity = quizEntity.getEntity(i.getSakaiId(),this); break;
	   case SimplePageItem.FORUM:
	       lessonEntity = forumEntity.getEntity(i.getSakaiId()); break;
	   case SimplePageItem.MULTIMEDIA:
	       String displayType = i.getAttribute("multimediaDisplayType");
	       if (StringUtils.equalsAny(displayType, "1", "3") || i.getAttribute("multimediaUrl") != null)
		   return setLBItemGroups(i, groups);
	       else
		   return setResourceGroups (i, groups);
	   case SimplePageItem.RESOURCE:
	       if (i.getAttribute("multimediaUrl") != null)
		   return setLBItemGroups(i, groups);
	       return setResourceGroups (i, groups);
	   case SimplePageItem.TEXT:
	   case SimplePageItem.CHECKLIST:
	   case SimplePageItem.PAGE:
	   case SimplePageItem.BLTI:
	   case SimplePageItem.COMMENTS:
	   case SimplePageItem.TWITTER:
	   case SimplePageItem.QUESTION:
	   case SimplePageItem.STUDENT_CONTENT:
	   case SimplePageItem.CALENDAR:
	       return setLBItemGroups(i, groups);
	   case SimplePageItem.BREAK:
	       return null;  // better not actually happen
	   }
	   if (lessonEntity != null) {
	       // need a list to sort it.
	       Collection oldGroupCollection;
	       try {
		   oldGroupCollection = getItemGroups(i, lessonEntity, true);
	       } catch (IdUnusedException exc) {
		   return null; // no such entity
	       }
	       List<String>oldGroups;
	       if (oldGroupCollection == null)
		   oldGroups = new ArrayList<>();
	       else
		   oldGroups = new ArrayList<>(oldGroupCollection);

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
			   if (StringUtils.isBlank(groupString))
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

	   SecurityAdvisor pushedAdvisor = null;
	   try {
	       pushedAdvisor = pushAdvisor();
	       resource = contentHostingService.editResource(i.getSakaiId());

	       if (AccessMode.GROUPED.equals(resource.getInheritedAccess())) {
		   Collection<String> oldGroups = resource.getInheritedGroups();
		   if (oldGroups instanceof List)
		       ret = (List<String>)oldGroups;
		   else if (oldGroups != null)
		       ret = new ArrayList<>(oldGroups);
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
	       contentHostingService.commitResource(resource, notificationService.NOTI_NONE);
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
	       popAdvisor(pushedAdvisor);
	   }

	   return ret;

       }

       public List<String> setLBItemGroups (SimplePageItem i, String[] groups) {

	   List<String>ret = null;
	   // old value
	   String groupString = i.getGroups();
	   if (StringUtils.isNotBlank(groupString)) {
	       ret = Arrays.asList(groupString.split(","));
	   }
	       
	   groupString = null;
	   if (groups != null) {
		   for (String group : groups) {
			   if (groupString == null)
				   groupString = group;
			   else
				   groupString = groupString + "," + group;
		   }
	   }
	   i.setGroups(groupString);

	   return ret; // old value
       }

       public Set<String> getMyGroups() {
	   if (myGroups != null)
	       return myGroups;
	   String userId = getCurrentUserId();
	   Collection<Group> groups = getCurrentSite().getGroupsWithMember(userId);
	   Set<String>ret = new HashSet<>();
	   if (groups == null)
	       return ret;
	   for (Group group: groups)
	       ret.add(group.getId());
	   myGroups = ret;
	   return ret;
       }

    // get actual groups that should submit to this student content item
    // only called if the item has groups. If there are specified groups
    // use them, otherwise get all groups
	public Set<String> getOwnerGroups(SimplePageItem item) {
	    Set<String> ret = new HashSet<>();
	    String ownerGroups = item.getOwnerGroups();
	    if (ownerGroups == null || ownerGroups.equals("")) {
		List<GroupEntry> groupEntries = getCurrentGroups();
		for (GroupEntry entry: groupEntries)
		    ret.add(entry.id);
	    } else {
		String [] groups = ownerGroups.split(",");
		for (String group: groups) {
		    if (StringUtils.isNotBlank(group))
			ret.add(group);
		}
	    }
	    return ret;
	}

    // sort the list, since it will typically be presented
    // to the user. This skips our access groups
       public List<GroupEntry> getCurrentGroups() {
	   if (currentGroups != null)
	       return currentGroups;

	   Site site = getCurrentSite();
	   Collection<Group> groups = site.getGroups();
	   List<GroupEntry> groupEntries = new ArrayList<>();
	   for (Group g: groups) {
	       if (g.getProperties().getProperty("lessonbuilder_ref") != null ||
		   g.getTitle().startsWith("Access: "))
		   continue;
	       GroupEntry e = new GroupEntry();
	       e.name = g.getTitle();
	       e.id = g.getId();
	       groupEntries.add(e);
	   }

	   Collections.sort(groupEntries,new Comparator() {
		   public int compare(Object o1, Object o2) {
		       GroupEntry e1 = (GroupEntry)o1;
		       GroupEntry e2 = (GroupEntry)o2;
		       return new AlphaNumericComparator().compare(e1.name, e2.name);
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
		if (!checkCsrf())
		    return "permission-failed";

		if (selectedQuiz == null) {
			return "failure";
		} else {
			try {
			    LessonEntity selectedObject = quizEntity.getEntity(selectedQuiz,this);
			    if (selectedObject == null)
				return "failure";

			    // editing existing item?
			    SimplePageItem i;
			    if (itemId != null && itemId != -1) {
				i = findItem(itemId);
				// do getEntity/getreference to normalize, in case sakaiid is old format
				LessonEntity existing = quizEntity.getEntity(i.getSakaiId(),this);
				String ref = null;
				if (existing != null)
				    ref = existing.getReference();
				// if same quiz, nothing to do
				if ((existing == null) || !ref.equals(selectedQuiz)) {
				    // if access controlled, clear restriction from old quiz and add to new
				    if (i.isPrerequisite()) {
					if (existing != null) {
					    i.setPrerequisite(false);
					    checkControlGroup(i, false);
					}
					// sakaiid and name are used in setting control
					i.setSakaiId(selectedQuiz);
					i.setName(selectedObject.getTitle());
					i.setPrerequisite(true);
					checkControlGroup(i, true);
				    } else {
					i.setSakaiId(selectedQuiz);
					i.setName(selectedObject.getTitle());
				    }
				    update(i);
				}
			    } else { // no, add new item
				i = appendItem(selectedQuiz, selectedObject.getTitle(), SimplePageItem.ASSESSMENT);
				saveItem(i);
			    }
			    return "success";
			} catch (Exception ex) {
			    log.error(ex.getMessage(), ex);
			    return "failure";
			} finally {
			    selectedQuiz = null;
			}
		}
	}

	public void setLinkUrl(String url) {
		linkUrl = url;
	}

	public String normUrl (String url) {
		// these should never happen, but I like making methods robust
		if (url == null)
		    return null;
		url = url.trim();
		if (StringUtils.isBlank(url))
		    return url;

		// the intent is to handle something like www.cnn.com or www.cnn.com/foo
		// Note that the result has no protocol. That means it will use the protocol
		// of the page it's displayed from, which should be right.
		if (!url.startsWith("http:") && !url.startsWith("https:") && !url.startsWith("/")) {
		    String atom = url;
		    int i = atom.indexOf("/");
		    if (i >= 0)
			atom = atom.substring(0, i);
		    // first atom is hostname
		    if (atom.contains( "." )) {
			String server= ServerConfigurationService.getServerUrl();
			if (server.startsWith("https:"))
			    url = "https://" + url;
			else
			    url = "http://" + url;
		    }
		}

		return url;
	}

    // doesn't seem to be used at the moment
	public String createLink() {
		if (StringUtils.isBlank(linkUrl)) {
			return "cancel";
		}

		String url = linkUrl;
		url = normUrl(url);

		SimplePageItem i = appendItem(url, url, SimplePageItem.URL);
		saveItem(i);

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
		if (!checkCsrf())
		    return "permission-failed";

		SimplePageItem i = findItem(itemId);
		if (i != null && i.getType() == SimplePageItem.MULTIMEDIA) {
			i.setHeight(height);
			i.setWidth(width);
			i.setAlt(alt);
			i.setDescription(description);
			i.setHtml(mimetype);
			i.setPrerequisite(this.prerequisite);
			setItemGroups(i, selectedGroups);
			update(i);
			return "success";
		} else {
			log.warn("editMultimedia Could not find multimedia object: {}", itemId);
			return "cancel";
		}
	}

    // called by edit title dialog to change attributes of the page such as the title
	public String editTitle()  {
		if (StringUtils.isBlank(pageTitle)) {
			return "notitle";
		}
		
		// because we're using a security advisor, need to make sure it's OK ourselves
		if (!canEditPage()) {
		    return "permission-failed";
		}
		if (!checkCsrf())
		    return "permission-failed";

		Placement placement = toolManager.getCurrentPlacement();
		SimplePage page = getCurrentPage();
		SimplePageItem pageItem = getCurrentPageItem(null);
		Site site = getCurrentSite();
		boolean needRecompute = false;
		boolean isOwner = false;

		if(!isStudentPage(page) && getEditPrivs() == 0) {
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
			List<String> gradebookUids = Arrays.asList(site.getId());
			if (gradebookIfc.isGradebookGroupEnabled(site.getId())) {
				gradebookUids = gradebookIfc.getGradebookGroupInstances(site.getId());
			}
			if (newPoints == null && currentPoints != null) {
				add = gradebookIfc.removeExternalAssessment(site.getId(), "lesson-builder:" + page.getPageId());
			} else if (newPoints != null && currentPoints == null) {
				try {
					add = gradebookIfc.addExternalAssessment(gradebookUids, site.getId(), "lesson-builder:" + page.getPageId(), null,
							pageTitle, newPoints, null, LessonBuilderConstants.TOOL_ID);
				} catch(ConflictingAssignmentNameException cane) {
					add = false;
					setErrMessage(messageLocator.getMessage("simplepage.existing-gradebook"));
				}
				if(!add) {
					setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
				} else
				    needRecompute = true;
			} else if (currentPoints != null && 
					(!currentPoints.equals(newPoints) || !pageTitle.equals(page.getTitle()))) {
				add = gradebookIfc.updateExternalAssessment(gradebookUids, "lesson-builder:" + page.getPageId(), null,
							  	pageTitle, newPoints, null);
				if(!add) {
					setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
				} else if (!currentPoints.equals(newPoints))
					needRecompute = true;
			}
			if (add)
			    page.setGradebookPoints(newPoints);

			isOwner = currentUserId!=null && currentUserId.equals(page.getOwner());
			if (newOwner==null){
				page.setOwner(newOwner);
				page.setOwned(false);
			}
			else {
				Set<String> siteMembers = site.getUsers();
				boolean ownerIsMember = siteMembers.contains(newOwner);
				if (ownerIsMember) {
					page.setOwner(newOwner);
					page.setOwned(true);
				}
				else {
					User user = null;
					try {
						user = userDirectoryService.getUser(newOwner);
						String displayName = user.getDisplayName();
						setErrMessage(messageLocator.getMessage("simplepage.not-member").replace("{}", displayName));
					} catch (UserNotDefinedException e) {
						log.warn("Can't find user from owner:{}", newOwner);
					}
				}
			}
		}

		if (pageTitle != null && pageItem.getPageId() == 0) {
			// we need a security advisor because we're allowing users to edit the page if they
			// have simplepage.upd privileges, but site.save requires site.upd.
			try {
				securityService.pushAdvisor(siteUpdAdvisor);

				SitePage sitePage = site.getPage(page.getToolId());
					
				for (ToolConfiguration t: sitePage.getTools()) {
					if (t.getId().equals(placement.getId()))
						t.setTitle(pageTitle);
				}
				
				sitePage.setTitle(pageTitle);
				try {
				    siteService.save(site);
				} catch (Exception e) {
				    // there's no actual error for title too long. I end up with a generic
				    // Runtimeexception. For that reason we can only guess that this it the cause. I don't want to
				    // check for 99 characters, because the kernel could change.
				    setErrMessage(messageLocator.getMessage("simplepage.title_too_long"));
				    return("failed");
				}
				page.setTitle(pageTitle);
				page.setHidden(hidePage);
				page.setHiddenFromNavigation(hidePageFromNavigation);
				if (hasReleaseDate)
					page.setReleaseDate(releaseDate);
				else
					page.setReleaseDate(null);
				update(page, !isOwner);
				updateCurrentPage(!isOwner);
				placement.setTitle(pageTitle);
				placement.save();
				pageVisibilityHelper(site, page.getToolId(), !hidePage);
				pageItem.setPrerequisite(prerequisite);
				pageItem.setRequired(required);
				pageItem.setName(pageTitle);
				update(pageItem, !isOwner);

			} catch (Exception e) {
				log.error(e.getMessage(), e);
			} finally {
				securityService.popAdvisor(siteUpdAdvisor);
			}
		} else if (pageTitle != null) {
			page.setTitle(pageTitle);
			page.setHidden(hidePage);
			page.setHiddenFromNavigation(hidePageFromNavigation);
			if (hasReleaseDate)
			    page.setReleaseDate(releaseDate);
			else
			    page.setReleaseDate(null);
			update(page, !isOwner);
		}

		if(pageTitle != null) {
			if(pageItem.getType() == SimplePageItem.STUDENT_CONTENT) {
				SimpleStudentPage student = simplePageToolDao.findStudentPageByPageId(page.getPageId());
				student.setTitle(pageTitle);
				update(student, false);
			} else {
				pageItem.setName(pageTitle);
				update(pageItem, !isOwner);
			}
			
			adjustPath("", pageItem.getPageId(), pageItem.getId(), pageTitle);
		}
		
		String collectionId = contentHostingService.getSiteCollection(getCurrentSiteId()) + "LB-CSS/";
		String uploadId = uploadFile(collectionId);
		if(uploadId != null) {
			page.setCssSheet(uploadId);
			
			// Make sure the relevant caches are wiped clean.
			resourceCache.remove(collectionId);
			resourceCache.remove(uploadId);
		}else {
			page.setCssSheet(dropDown);
		}
		
		update(page, !isOwner);

		// have to do this after the page itself is updated
		if (needRecompute)
		    recomputeGradebookEntries(page.getPageId(), points);
		// points, not newPoints because API wants a string
		
		// Create or update a task
		String reference = "/lessonbuilder/page/" + page.getPageId();
                // Dashboard task widget: When a page item is added create a task item for the site members.
		createOrUpdateTask(reference, page.getSiteId(), page.getTitle(), null);

		if (pageItem.getPageId() == 0) {
			return "reload";
		} else {
			return "success";
		}
	}
	
	private boolean uploadSizeOk(MultipartFile file) {
	    long uploadedFileSize = file.getSize();
	    return uploadSizeOk(uploadedFileSize);
	}

	private boolean uploadSizeOk(long uploadedFileSize) {
	    if (uploadedFileSize == 0) {
		setErrMessage(messageLocator.getMessage("simplepage.filezero"));
		return false;
	    }

	    // implement precedence rules: ceiling if set, else max, else 20
	    String max = ServerConfigurationService.getString("content.upload.max", null);
	    String ceiling = ServerConfigurationService.getString("content.upload.ceiling", null);
	    String effective = ceiling;
	    if (effective == null)
		effective = max;
	    if (effective == null)
		effective = "20";
	    long maxFileSizeInBytes = 20 * 1024 * 1024;
	    try {
		maxFileSizeInBytes = Long.parseLong(effective) * 1024 * 1024;
	    } catch(NumberFormatException e) {
		log.warn("Unable to parse content.upload.max retrieved from properties file during upload");
	    }

	    if (uploadedFileSize > maxFileSizeInBytes) {
		String limit = Long.toString(maxFileSizeInBytes / (1024*1024));
		setErrMessage(messageLocator.getMessage("simplepage.filetoobig").replace("{}", limit));
		return false;
	    }
	    return true;
	}

	private String uploadFile(String collectionId) {
		String name;
		String mimeType;
		MultipartFile file = null;
		
		if (multipartMap.size() > 0) {
			// 	user specified a file, create it
			file = multipartMap.values().iterator().next();
		}
		
		if (file != null) {

			// uploadsizeok would otherwise complain about 0 length file. For
			// this case it's valid. Means no file.
			if (file.getSize() == 0)
			    return null;
			if (!uploadSizeOk(file))
			    return null;

			try {
				contentHostingService.checkCollection(collectionId);
			}catch(Exception ex) {
				try {
					ContentCollectionEdit edit = contentHostingService.addCollection(collectionId);
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, "LB-CSS");
					//this folder should be hidden with access from access user
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, "true");
					contentHostingService.commitCollection(edit);
				}catch(Exception e) {
					setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
					return null;
				}
			}
			
			//String collectionId = getCollectionIdfalse);
			// 	user specified a file, create it
			name = file.getOriginalFilename();
			if (StringUtils.isBlank(name))
				name = file.getName();
			
			mimeType = file.getContentType();
			try {
				String[] names = fixFileName(collectionId, name);
				ContentResourceEdit res = contentHostingService.addResource(collectionId, names[0], names[1], MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
				res.setContentType(mimeType);
				res.setContent(file.getInputStream());
				try {
					contentHostingService.commitResource(res,  notificationService.NOTI_NONE);
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
				log.error("addMultimedia error 1 {}", e.toString());
				return null;
			}
		}else {
			return null;
		}
	}

	private int getNextSequenceOnPage(long pageId){	//find highest Sequence number among a page's items and return a higher one that can be used for a new item
		List<SimplePageItem> itemsNow = getItemsOnPage(pageId);
		if (itemsNow==null || itemsNow.isEmpty()){
			return 1;
		}
		int currentHighest = 1;
		for(SimplePageItem itemNow: itemsNow){
			if(itemNow.getSequence() > currentHighest){
				currentHighest = itemNow.getSequence();
			}
		}
		return currentHighest + 1;
	}

	public String addPageLayout(){
		int sequence = getNextSequenceOnPage(getCurrentPageId());	//find the starting sequence number based on whatever might already be on the page. Must be incremented after every use.
       	if(StringUtils.equals(this.pageLayoutSelect, ADD_SUBPAGE_LIST)){
			SimplePage pageNow = null;
			SimplePageItem itemNow = null;
			if(StringUtils.isBlank(pageSubpageTitle)){
				setErrMessage(messageLocator.getMessage("simplepage.layout.page.emptyName"));
				return FAILURE;
			}
			itemNow = simplePageToolDao.makeItem(getCurrentPageId(),sequence,SimplePageItem.BREAK,null,null);
			sequence++;
			itemNow.setFormat(SECTION);
			itemNow.setAttribute(FORCE_BTN,Boolean.FALSE.toString());
			itemNow.setAttribute(CHECKLIST_ITEMS,null);
			update(itemNow);	//finish writing break before subpages
			for(int count=1; count<=pageSubpageCount; count++){
				pageNow = addPage(pageSubpageTitle + ' ' + count, null, false, false);	//make new page
				pageNow.setParent(getCurrentPageId());	//clean up page data
				pageNow.setTopParent(getCurrentPage().getPageId());
				update(pageNow);	//save revised page data
				itemNow = simplePageToolDao.findItemsBySakaiId(Long.toString(pageNow.getPageId())).get(0);	//there should only be one item with this sakaiId, because it was just now created as part of addPage.
				if(pageSubpageButton){	//clean up item data
					itemNow.setFormat(BUTTON);
				}
				itemNow.setPageId(getCurrentPageId());
				itemNow.setAttribute(BTN_COLOR,pageButtonColorScheme);
				itemNow.setSequence(sequence);
				sequence++;
				update(itemNow);	//save revised item data
			}
		} else if(StringUtils.equals(this.pageLayoutSelect, INTERIOR_RESOURCES) || StringUtils.equals(this.pageLayoutSelect, INTERIOR_TASK)){	//first handle the parts that these layouts have in common.
			SimplePageItem itemNow = simplePageToolDao.makeItem(getCurrentPageId(), sequence, SimplePageItem.BREAK, null, null);
			sequence++;
			itemNow.setFormat(SECTION);
			itemNow.setAttribute(COL_WIDTH, "2");
			itemNow.setAttribute(FORCE_BTN,Boolean.FALSE.toString());
			itemNow.setAttribute(CHECKLIST_ITEMS,null);
			update(itemNow);	//finish writing top Section/break item
			itemNow = simplePageToolDao.makeItem(getCurrentPageId(), sequence,SimplePageItem.TEXT,null,null);
			sequence++;
			String overviewHtmlAppender = "";
			if(StringUtils.equals(this.pageLayoutSelect, INTERIOR_RESOURCES)){	//special Overview text for interiorResources only
				overviewHtmlAppender = overviewHtmlAppender + messageLocator.getMessage("simplepage.layout.page.overview",messageLocator.getMessage("simplepage.layout.page.overview.label.tasks"));
				overviewHtmlAppender = overviewHtmlAppender + messageLocator.getMessage("simplepage.layout.page.overview.task");
			} else {	//text items for interiorTask only
				overviewHtmlAppender = overviewHtmlAppender + messageLocator.getMessage("simplepage.layout.page.overview",messageLocator.getMessage("simplepage.layout.page.overview.label.learningoutcomes"));
				overviewHtmlAppender = overviewHtmlAppender + messageLocator.getMessage("simplepage.layout.page.overview.lo");
			}
			overviewHtmlAppender = overviewHtmlAppender + messageLocator.getMessage("simplepage.layout.page.overview.tail");
			itemNow.setHtml(overviewHtmlAppender);
			itemNow.setAttribute(CHECKLIST_ITEMS,null);
			update(itemNow);	//finish writing second item [overview]
			itemNow = simplePageToolDao.makeItem(getCurrentPageId(),sequence,SimplePageItem.BREAK,null,null);
			sequence++;
			itemNow.setFormat(COLUMN);
			itemNow.setAttribute(FORCE_BTN,Boolean.FALSE.toString());
			itemNow.setAttribute(CHECKLIST_ITEMS,null);
			itemNow.setAttribute(COL_COLOR,"trans");
			update(itemNow);	//finish writing third item, Break for the checklist
			itemNow = simplePageToolDao.makeItem(getCurrentPageId(),sequence,SimplePageItem.CHECKLIST,null,messageLocator.getMessage("simplepage.layout.page.overview.label.checklist"));
			sequence++;
			itemNow.setDescription(messageLocator.getMessage("simplepage.layout.page.trackprogress"));
			itemNow.setAttribute(INDENT_LEVEL,"0");
			itemNow.setAttribute(NAME_HIDDEN,Boolean.FALSE.toString());
			simplePageToolDao.addChecklistItem(itemNow,Long.valueOf(0),messageLocator.getMessage("simplepage.layout.page.additem"),Long.valueOf(-1));
			simplePageToolDao.addChecklistItem(itemNow,Long.valueOf(1),messageLocator.getMessage("simplepage.layout.page.additem"),Long.valueOf(-1));
			simplePageToolDao.addChecklistItem(itemNow,Long.valueOf(2),messageLocator.getMessage("simplepage.layout.page.additem"),Long.valueOf(-1));
			itemNow.setAttribute(BTN_COLOR,null);
			itemNow.setAttribute(CUSTOM_CSS_CLASS,BG_SUCCESS);
			update(itemNow);	//finish writing fourth item, the checklist itself
			if(StringUtils.equals(this.pageLayoutSelect, INTERIOR_RESOURCES)){	//parts unique to interiorResources
				itemNow = simplePageToolDao.makeItem(getCurrentPageId(),sequence,SimplePageItem.TEXT,null,null);
				sequence++;
				itemNow.setHtml(messageLocator.getMessage("simplepage.layout.page.dates"));
				itemNow.setAttribute(CHECKLIST_ITEMS,null);
				update(itemNow); 	//finish writing fifth item, Important Dates
				itemNow = simplePageToolDao.makeItem(getCurrentPageId(),sequence,SimplePageItem.BREAK,null,messageLocator.getMessage("simplepage.layout.page.overview.label.resources"));
				sequence++;
				itemNow.setFormat(SECTION);
				itemNow.setAttribute(FORCE_BTN,Boolean.FALSE.toString());
				itemNow.setAttribute(CHECKLIST_ITEMS,null);
				itemNow.setAttribute(COL_COLOR,"nnavy");
				update(itemNow);	//finish writing last page, Resources heading
			} else {	//parts unique to interiorTask
				for(int count=1; count<=this.pageTaskCount; count++){
					itemNow = simplePageToolDao.makeItem(getCurrentPageId(),sequence,SimplePageItem.BREAK,null, messageLocator.getMessage("simplepage.layout.page.overview.label.task") + count);
					sequence++;
					itemNow.setFormat(SECTION);
					itemNow.setAttribute(FORCE_BTN,Boolean.FALSE.toString());
					itemNow.setAttribute(CHECKLIST_ITEMS,null);
					itemNow.setAttribute(COL_COLOR,pageColorScheme);
					if(this.pageTaskCollapsible){
						itemNow.setAttribute(COLLAPSIBLE,"1");
					}
					if(this.pageTaskClosed){
						itemNow.setAttribute(DEFAULT_CLOSED,"1");
					}
					update(itemNow);	//finish writing an individual Task page
				}
			}
		}	//completed either interiorResources or interiorTask layout.
		return SUCCESS;
	}

	public String addLayout() {
		if (!canEditPage()) {
			return "permission-fail";
		}
		if (!checkCsrf()) {
			return "permission-fail";
		}
		SimplePageItem newSection = appendItem("", this.layoutSectionTitle, SimplePageItem.BREAK);
		newSection.setFormat("section");
		if (this.layoutSectionCollapsible) {
			newSection.setAttribute("collapsible", "1");
		}
		if (this.layoutSectionStartCollapsed) {
			newSection.setAttribute("defaultClosed", "1");
		}
		if (StringUtils.equals(this.layoutSelect, "left-double")) {
			newSection.setAttribute("colwidth", "2");
		}

		String colorScheme = "";
		if (StringUtils.equals("none", this.layoutColorScheme)) {
			if (!this.layoutSectionShowBorders) {
				colorScheme = "trans";
			}
		} else {
			if (this.layoutSectionShowBorders) {
				colorScheme = this.layoutColorScheme;
			} else {
				colorScheme = this.layoutColorScheme + "-trans";
			}
		}

		newSection.setAttribute("colcolor", colorScheme);
		newSection.setAttribute("forceBtn", Boolean.toString(this.forceButtonColor));
		saveOrUpdate(newSection);

		if (!StringUtils.equals(this.layoutSelect, "single-column")) {
			SimplePageItem col1 = appendItem("", "", SimplePageItem.BREAK);
			col1.setFormat("column");
			col1.setSequence(newSection.getSequence()+1);
			if (StringUtils.equals(this.layoutSelect, "right-double")) {
				col1.setAttribute("colwidth", "2");
			}
			col1.setAttribute("colcolor", colorScheme);
			col1.setAttribute("forceBtn", Boolean.toString(forceButtonColor));
			saveOrUpdate(col1);

			if (StringUtils.equals(this.layoutSelect, "three-equal")) {
				SimplePageItem col2 = appendItem("", "", SimplePageItem.BREAK);
				col2.setFormat("column");
				col2.setSequence(col1.getSequence()+1);
				col2.setAttribute("colcolor", colorScheme);
				col2.setAttribute("forceBtn", Boolean.toString(this.forceButtonColor));
				saveOrUpdate(col2);
			}
		}

		setTopRefresh();

		return "success";
	}

	public String addPages()  {
		if (!canEditPage())
			return "permission-fail";
		if (!checkCsrf())
		    return "permission-failed";

		// javascript should have checked all this
		if (StringUtils.isBlank(newPageTitle))
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
		if (!checkCsrf())
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

		ToolConfiguration tool = sitePage.addTool(LessonBuilderConstants.TOOL_ID);
		String toolId = tool.getPageId();
		
		SimplePage page;
		
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
		
		// Does the top-level page item already exist, as in the case of adding a pre-existing page? If so, don't create another item.
		SimplePageItem existingTop = simplePageToolDao.findTopLevelPageItemBySakaiId(Long.toString(page.getPageId()));
		if (existingTop == null) {
			SimplePageItem item = simplePageToolDao.makeItem(0, 0, SimplePageItem.PAGE, Long.toString(page.getPageId()), title);
			saveItem(item);
		}

		sitePage.setTitle(title);
		sitePage.setTitleCustom(true);
		if (doSave) {
		    try {
			siteService.save(site);
		    } catch (Exception e) {
			log.error("addPage unable to save site {}", e.toString());
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
			simplePageToolDao.copyItem2(oldItem, newItem);
	    	}
	    }

	    setTopRefresh();
	    
	    return page;
	}

	// when a gradebook entry is added or point value for page changed, need to
	// add or update all student entries for the page
	// this only updates grades for users that are complete. Others should have 0 score, which won't change
	public void recomputeGradebookEntries(Long pageId, String newPoints) {
		Map<String, String> userMap = new HashMap<>();
		List<SimplePageItem> items = simplePageToolDao.findPageItemsBySakaiId(Long.toString(pageId));
		if (items == null)
			return;
		List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
		Set<String> gbGroups = new HashSet<>();
		for (SimplePageItem item : items) {
			List<String> users = simplePageToolDao.findUserWithCompletePages(item.getId());
			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
				try {
					Collection<String> groupsAux = getItemGroups(item, null, false);
					if (groupsAux != null) {
						gbGroups.addAll(groupsAux);
					}
				} catch (Exception e) {
					log.error("Error getting groups for item {} : {} ", item.getId(), e.getMessage());
				}
			}

			for (String user: users) {
				userMap.put(user, newPoints);
			}
		}
		
		if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId()) && !gbGroups.isEmpty()) {
			gradebookUids = new ArrayList<>(gbGroups);
		}
		for (String gradebookUid : gradebookUids) {
			gradebookIfc.updateExternalAssessmentScores(gradebookUid, getCurrentSiteId(), "lesson-builder:" + pageId, userMap);
		}
	}

	// there's one of these in Validator, but it isn't quite right, because it doesn't look at /
	// return lowercase version, since we want uppercase versions to match
	public static String getExtension(String name) {
		String extension = org.springframework.util.StringUtils.getFilenameExtension(name);
		return StringUtils.trimToEmpty(extension).toLowerCase();
	}

	public boolean isPDFType(SimplePageItem item) {
		String mimeType = getContentType(item);
		return StringUtils.isNotBlank(mimeType) && MediaType.APPLICATION_PDF_VALUE.toLowerCase().equals(mimeType.toLowerCase());
	}

	public boolean isImageType(SimplePageItem item) {

		String mimeType = getContentType(item);

		if (mimeType != null && mimeType.length() > 0 && !mimeType.equals("application/octet-stream")) {
		    return mimeType.toLowerCase().startsWith("image/");
		}

		// no usable type found
		// getContentType already checked extensions. But we have our own idea of image types, which is site-configurable
		// to check for them

		String extension = getExtension(item.getSakaiId());

		return imageTypes.contains(extension);
	}

	public boolean isHtmlType(SimplePageItem item) {

		String mimeType = getContentType(item);

		if (mimeType != null && mimeType.length() > 0 && !mimeType.equals("application/octet-stream")) {
		    return mimeType.equals("text/html") || mimeType.equals("application/xhtml+xml");
		}

		// no usable type found
		// getContentType already checked extensions. But we have our own idea of html types, which is site-configurable
		// to check for them

		String extension = getExtension(item.getSakaiId());

		return Arrays.binarySearch(htmlTypes, extension) >= 0;

	}

	public void setOrder(String order) {
		this.order = order;
	}

	public void fixorder() {
	    List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());
		items.sort(Comparator.comparing(SimplePageItem::getSequence));

		// remove items where sequence is <= 0
		items.stream().filter(i -> i.getSequence() <= 0).forEach(items::remove);

		// the remaining items are ordered by sequence
		int i = 1;
		for (SimplePageItem item : items) {
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
		if (!checkCsrf())
		    return "permission-failed";

		if (order == null) {
			return "cancel";
		}

		simplePageToolDao.setRefreshMode();

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
		String otherSiteId = "";
		if (selectedEntity != null && !selectedEntity.equals("")) {
		    // second page is involved
		    Long secondPageId = Long.parseLong(selectedEntity);
		    SimplePage secondPage = getPage(secondPageId);
			otherSiteId = secondPage.getSiteId();	//we need this populated to check on inter-site copying later on.
		    if (secondPage != null) {
			secondItems = getItemsOnPage(secondPageId);
			if (secondItems.isEmpty())
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
		Set<String> used = new HashSet<>();
		for (int i = 0; i < split.length; i++) {
			if (!used.add(split[i].trim())) {
				log.warn("reorder: duplicate value");
				setErrMessage(messageLocator.getMessage("simplepage.reorder-duplicates"));
				return "failed"; // it was already there. Oops.
			}
		}

		// keep track of which old items are used so we can remove the ones that aren't.
		// items in set are indices into "items"
		Set<SimplePageItem> keep = new HashSet<>();

		// now do the reordering
		for (int i = 0; i < split.length; i++) {
			if (split[i].equals("---"))
			    break;
			if (split[i].startsWith("*")) {
			    // item from second page. add copy
			    SimplePageItem oldItem = secondItems.get(Integer.parseInt(split[i].substring(1)) - 1);
			    SimplePageItem newItem = simplePageToolDao.copyItem(oldItem);
				if (newItem.getType() == SimplePageItem.PAGE){	//if the new item is a page link, we need to see if that page is in another site.
					SimplePage p = simplePageToolDao.getPage(Long.valueOf(newItem.getSakaiId()));
					boolean fromOtherSite = !StringUtils.equals(getCurrentSiteId(), otherSiteId);	//identifies if the page is from another site
					if (fromOtherSite){	//if the page is from another site, we will re-link to a new blank one for this site instead.
						Long parent = getCurrentPage().getPageId();
						Long topParent = getCurrentPage().getTopParent();
						String toolId = ((ToolConfiguration) toolManager.getCurrentPlacement()).getPageId();
						SimplePage newPage = simplePageToolDao.makePage(toolId, getCurrentSiteId(), p.getTitle(), parent, topParent);
						saveItem(newPage);
						newItem.setSakaiId(Long.valueOf(newPage.getPageId()).toString());
					}
				} else if (newItem.getType()==SimplePageItem.MULTIMEDIA || newItem.getType()==SimplePageItem.RESOURCE){	//resource/multimedia links from other sites will reference their original site unless we change it
					boolean fromOtherSite = !StringUtils.contains(newItem.getSakaiId(), getCurrentSiteId());
					if (fromOtherSite){	//replace any reference to the other site with this site's ID.
						String newSakaiId = newItem.getSakaiId().replace(otherSiteId, getCurrentSiteId());
						newItem.setSakaiId(newSakaiId);
					}
				} else if (newItem.getType() == SimplePageItem.RESOURCE_FOLDER){	//links to Resource folders from other sites will have their site IDs in the attributes; we must change them
					boolean fromOtherSite = !StringUtils.contains(newItem.getSakaiId(), getCurrentSiteId());
					if (fromOtherSite){	//replace any reference to the other site with this site's ID.
						String newSiteId = newItem.getAttribute("dataDirectory").replace(otherSiteId, getCurrentSiteId());
						newItem.setAttribute("dataDirectory", newSiteId);
					}
				} else if (newItem.getType() == SimplePageItem.TEXT){	//look for the old site ID in text and replace it.
					if (!StringUtils.isBlank(otherSiteId)){
						String newText = newItem.getHtml().replace(otherSiteId, getCurrentSiteId());
						newItem.setHtml(newText);
					}
				}
			    newItem.setPageId(getCurrentPageId());
			    newItem.setSequence(i + 1);
			    saveItem(newItem);
			    simplePageToolDao.copyItem2(oldItem, newItem);
			} else {
			    // existing item. update its sequence and note that it's still used
			    SimplePageItem existingItem = items.get(Integer.parseInt(split[i]) - 1);
			    existingItem.setSequence(i + 1);
			    keep.add(existingItem);
				update(existingItem);
			}
		}

		// delete items on the page that were not kept without changing the sequence of those items kept
		items.stream().filter(Predicate.not(keep::contains)).forEach(i -> deleteItem(i, false));

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
	    	currentUserId = userDirectoryService.getCurrentUser().getId();
	    return currentUserId;
	}
	    
    // page is complete, update gradebook entry if any
    // note that if the user has never gone to a page, the gradebook item will be missing.
    // if they gone to it but it's not complete, it will be 0. We can't explicitly set
    // a missing value, and this is the only way things will work if someone completes a page
    // and something changes so it is no longer complete. 
	public void trackComplete(SimplePageItem item, boolean complete ) {
		SimplePage page = getCurrentPage();
		if (page.getGradebookPoints() != null) {
	    	gradebookIfc.updateExternalAssessmentScore(getCurrentSiteId(), getCurrentSiteId(), "lesson-builder:" + page.getPageId(), getCurrentUserId(), 
	    			complete ? Double.toString(page.getGradebookPoints()) : "0.0");
		}
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
		if (path != null) {
		    List<String> pathItems = new ArrayList<>(Arrays.asList(path.split(",")));
		    int last = pathItems.size();
		    path = "";
		    for (int i = 0; i < last; i ++) {
			String item = pathItems.get(i);
			int lastIndex = pathItems.lastIndexOf(item);
			if (lastIndex > i) {
			    // item occurs more than once. kill intermediates
			    for (int j = i; j < lastIndex; j++)
				pathItems.remove(i); // as we remove, the index stays the same
			    last -= (lastIndex - i); // number of items removed
			}
			// reconstruct string; will have extra , at the beginning
			path += "," + item;
		    }
		    // now make sure it's not too long for database field
		    while (path.length() > 255) {
			int i = path.indexOf(",", 1);
			if (i > 0)
			    path = path.substring(i);  // kill first item
		    }
		    path = path.substring(1); // kill initial comma
		}

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
				SimplePageItem item = findItem(itemId);
				//If sakaiId is not empty, the item is a page, if not is an item
				if(!StringUtils.isEmpty(item.getSakaiId())){
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_READ, "/lessonbuilder/page/" + item.getSakaiId(), complete));
				}else{
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_READ, "/lessonbuilder/item/" + item.getId(), complete));
				}
				trackComplete(item, complete);
				studentPageId = -1L;
			}else if(path != null) {
				entry.setPath(path);
				entry.setComplete(true);
				entry.setToolId(toolId);
				SimplePage page = getPage(studentPageId);
				eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_READ, "/lessonbuilder/page/" + page.getPageId(), true));
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
				SimplePageItem item = findItem(itemId);
				//If sakaiId is not empty, the item is a page, if not is an item
				if(StringUtils.isNotBlank(item.getSakaiId())){
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_READ, "/lessonbuilder/page/" + item.getSakaiId(), complete));
				}else{
					eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.ITEM_READ, "/lessonbuilder/item/" + item.getId(), complete));
				}
				if (complete != wasComplete)
				    trackComplete(item, complete);
			}else if(path != null) {
				entry.setComplete(true);
				entry.setPath(path);
				entry.setToolId(toolId);
				entry.setDummy(false);
				SimplePage page = getPage(studentPageId);
				eventTrackingService.post(eventTrackingService.newEvent(LessonBuilderEvents.PAGE_READ, "/lessonbuilder/page/" + page.getPageId(), true));
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

	public boolean isItemVisible(SimplePageItem item) {
	    return isItemVisible(item, null);
	}

	public boolean isItemVisible(SimplePageItem item, SimplePage page) {
	    return isItemVisible(item, page, true);
	}

    // if the item has a group requirement, are we in one of the groups.
    // this is called a lot and is fairly expensive, so results are cached
    // for student pages, if it's not the owner, use resources test for resources
    // for a student page, we don't bypass hidden and release date, so it's safest
    // just to call contentHosting.
	public boolean isItemVisible(SimplePageItem item, SimplePage page, boolean testpriv) {
		if (testpriv && canSeeAll()) {
		    return true;
		}
		Boolean ret = visibleCache.get(item.getId());
		if (ret != null) {
		    return (boolean)ret;
		}
		// item is page, and it is hidden or not released
		if (item.getType() == SimplePageItem.BREAK) {
			return true;  // breaks are always visible to all users
		} else if (item.getType() == SimplePageItem.PAGE) {
			SimplePage itemPage = getPage(Long.valueOf(item.getSakaiId()));
			if (itemPage.isHidden()) {
				return false;
			}
			if (itemPage.getReleaseDate() != null && itemPage.getReleaseDate().after(new Date())) {
				return true;
			}
		} else if (page != null && isStudentPage(page) && (item.getType() == SimplePageItem.RESOURCE || item.getType() == SimplePageItem.MULTIMEDIA)) {

		    // check for inline types. No resource to check. Since this section is for student page, no groups either
		    if (item.getType() == SimplePageItem.MULTIMEDIA) {
			String displayType = item.getAttribute("multimediaDisplayType");
			if (StringUtils.equalsAny(displayType, "1", "3") || item.getAttribute("multimediaUrl") != null)
			    return true;
		    }
		    // resource stored as a direct URL
		    if (item.getType() == SimplePageItem.RESOURCE && item.getAttribute("multimediaUrl") != null)
			return true;

		    // This code is taken from LessonBuilderAccessService, mostly

		    // for student pages, we give people access to files in the owner's worksite
		    // get data we need to check that

		    String id = item.getSakaiId();
		    String owner = page.getOwner();  // if student content
		    String group = page.getGroup();  // if student content
		    if (group != null)
			group = "/site/" + page.getSiteId() + "/group/" + group;
		    String currentSiteId = page.getSiteId();

		    // if group owned, and /user/xxxx is the person who created the resource
		    // this will be his uesrid. Note that xxxx is eid, so we need to translate
		    String usersite = null;
					    
		    if (owner != null && group != null && id.startsWith("/user/")) {
			String username = id.substring(6);
			int slash = username.indexOf("/");
			if (slash > 0)
			    usersite = username.substring(0,slash);
			// normally it is /user/EID, so convert to userid
			try {
			    usersite = userDirectoryService.getUserId(usersite);
			} catch (Exception e) {};
			String itemcreator = item.getAttribute("addedby");
			if (usersite != null && itemcreator != null && !usersite.equals(itemcreator))
			    usersite = null;
		    }

		    if (owner != null && usersite != null && authzGroupService.getUserRole(usersite, group) != null) {
			return true;
		    } else if (owner != null && group == null && id.startsWith("/user/" + owner)) {
			return true;
		    } else {
			try {
			    contentHostingService.checkResource(id);
			    return true;
			} catch (Exception e) {
			    // I think we should hide the item no matter what the error is
			    return false;
			}
		    }
		}

		Collection<String>itemGroups = null;
		SecurityAdvisor advisor = null;
		LessonEntity entity = null;
		try {
		    // need to do this before pushing the advisor, or we get bad results
		    boolean canSeeAll = canSeeAll();
		    advisor = pushAdvisorAlways();
		    if (!canSeeAll && !item.isRequired()) {
			switch (item.getType()) {
			case SimplePageItem.ASSIGNMENT:
			    entity = assignmentEntity.getEntity(item.getSakaiId());
			    if (entity == null || entity.notPublished())
				return false;
			    break;
			case SimplePageItem.ASSESSMENT:
				entity = quizEntity.getEntity(item.getSakaiId(), this);
				if (entity == null || entity.notPublished())
				return false;
			    break;
			case SimplePageItem.FORUM:
			    entity = forumEntity.getEntity(item.getSakaiId());
			    if (entity == null || entity.notPublished())
				return false;
			    break;
			case SimplePageItem.BLTI:
				if (bltiEntity != null) {
					entity = bltiEntity.getEntity(item.getSakaiId());
				}
				if (entity == null || entity.notPublished()) {
					return false;
				} else {
					// After checking that it exists reset to null so that groups are
					// checked internal to Lessons
					entity = null;
				}
			}
		    }
		} finally {
			popAdvisor(advisor);
		}

		Condition rootCondition = conditionService.getRootConditionForItem(currentSiteId, LessonBuilderConstants.TOOL_ID,
				Long.toString(item.getId())).orElse(null);

		if (rootCondition != null) {
			return conditionService.evaluateCondition(rootCondition, getCurrentUserId());
		}

		try {
		    // entity can be null. passing the actual entity just avoids a second lookup
		    itemGroups = getItemGroups(item, entity, false);
		} catch (IdUnusedException exc) {
		    // underlying entity missing. Normally don't show it. But if it's required
		    // we have to. Can't do group test if there's nothing to test, so return true here
		    // basically you can't group restrict an item until it exists
		    visibleCache.put(item.getId(), item.isRequired());
		    return item.isRequired();
		}

		if (itemGroups == null || itemGroups.isEmpty()) {
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
		
    private boolean arePageItemsComplete(long pageId) {

        int sequence = 0;
        SimplePageItem i = simplePageToolDao.findNextItemOnPage(pageId, sequence);

        while (i != null) {
            if (i.isRequired() && !isItemComplete(i) && isItemVisible(i)) {
                return false;
            }
            sequence++;
            i = simplePageToolDao.findNextItemOnPage(pageId, sequence);
        }

        return true;
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
			        boolean apic = arePageItemsComplete(Long.parseLong(item.getSakaiId())); 
				completeCache.put(itemId, apic);
				return apic;
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

				if (assignment.getSubmissionType() != AssignmentEntity.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION
						&& (submission == null || BooleanUtils.isNotTrue(submission.getUserSubmission()))) {
				    completeCache.put(itemId, false);
				    return false;
				}

				int type = assignment.getTypeOfGrade();

				if (!item.getSubrequirement()) {
					completeCache.put(itemId, true);
					return true;
				} else if (submission.getGradeString() != null) {
				    // assume that assignments always use string grade. this may change
					boolean ret = isAssignmentComplete(type, assignment, submission, item.getRequirementText());
					completeCache.put(itemId, ret);
					return ret;
				} else {
					completeCache.put(itemId, false);
					return false;
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
				completeCache.put(itemId, false);
				return false;
			}
		} else if (item.getType() == SimplePageItem.FORUM) {
			try {
				if (item.getSakaiId().equals(SimplePageItem.DUMMY)) {
					completeCache.put(itemId, false);
					return false;
				}
				User user = userDirectoryService.getUser(getCurrentUserId());
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
			    log.error(e.getMessage(), e);
			    completeCache.put(itemId, false);
			    return false;
			}
		} else if (item.getType() == SimplePageItem.ASSESSMENT) {
			if (item.getSakaiId().equals(SimplePageItem.DUMMY)) {
			    completeCache.put(itemId, false);
			    return false;
			}
			LessonEntity quiz = quizEntity.getEntity(item.getSakaiId(),this);
			if (quiz == null) {
			    completeCache.put(itemId, false);
			    return false;
			}
			User user;
			try {
			    user = userDirectoryService.getUser(getCurrentUserId());
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
				Double grade = submission.getGrade();
			    // 1.99999 should match 2, so do a bit of rounding up
			    if ((grade + 0.0001d) >= Double.valueOf(item.getRequirementText())) {
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
		        
			SimpleStudentPage student = findStudentPage(item);

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
		} else if (item.getType() == SimplePageItem.QUESTION) {
			SimplePageQuestionResponse response = simplePageToolDao.findQuestionResponse(item.getId(), getCurrentUserId());
			if(response != null) {
				completeCache.put(itemId, true);
				return true;
			}else {
				completeCache.put(itemId, false);
				return false;
			}
		} else if (item.getType() == SimplePageItem.PEEREVAL){
			SimplePagePeerEval peerEval = simplePageToolDao.findPeerEval(item.getId());
			boolean result = peerEval == null;
				completeCache.put(itemId, result);
				return result;
		} else if (item.getType() == SimplePageItem.CHECKLIST) {
			// Simply viewing will complete at this time
			completeCache.put(itemId, true);
			return true;
		}
		else {
			completeCache.put(itemId, false);
			return false;
		}
	}

	private boolean isAssignmentComplete(int type, LessonEntity assEntity, LessonSubmission submission, String requirementString) {
		String grade = submission.getGradeString();

		if (type == SimplePageItem.ASSESSMENT) {
			return grade.equals("Pass");
		} else if (type == SimplePageItem.TEXT) {
			return grade.equals("Checked");
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
				return requiredIndex >= currentIndex;
			}
		} else if (type == SimplePageItem.ASSIGNMENT) {
			if (submission.getUserSubmission() || assEntity.getSubmissionType() == AssignmentEntity.NON_ELECTRONIC_ASSIGNMENT_SUBMISSION) {
				if (submission.getGrade() != null) {
					// assignment 2 uses gradebook, so we have a float value
					// use some fuzz so 1.9999 is the same as 2
					return (submission.getGrade() + 0.0001d) >= Double.valueOf(requirementString);
				} else {
					Integer scaleFactor = ((AssignmentEntity) assEntity).getScaleFactor();
					if (scaleFactor == null) {
						scaleFactor = assignmentService.getScaleFactor();
					}
					// otherwise use the String. With two strings we can use exact decimal arithmetic
					return (new BigDecimal(grade)).compareTo(new BigDecimal(requirementString).multiply(new BigDecimal(scaleFactor))) >= 0;
				}
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
	 * @param alreadySeen
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
			if (!isItemComplete(item) && isItemVisible(item)) {
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
				    alreadySeen = new HashSet<>();
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
    // (given that they have read permission, of course). Use LessonsAccess
    // elsewhere, because there are additional checks in ShowPageProducer that
    // are not in this code.
    // Note that the same page can occur
    // multiple places, but we're passing the item, so we've got the right one
	public List<String> pagesNeeded(SimplePageItem item) {
		String currentPageId = Long.toString(getCurrentPageId());
		List<String> needed = new ArrayList<>();

	    // authorized or maybe user is gaming us, or maybe next page code
	    // sent them to something that isn't available.
	    // as an optimization check haslogentry first. That will be true if
	    // they have been here before. Saves us the trouble of doing full
	    // access checking. Otherwise do a real check. That should only happen
	    // for next page in odd situations. The code in ShowPageProducer checks
	    // visible and release for this page, so we really need
	    // available for this item. But to be complete we do need to check
	    // accessibility of the containing page.
		if (item.getPageId() > 0) {
			if (!hasLogEntry(item.getId()) &&
		       	    (!isItemAvailable(item, item.getPageId()) ||
			     !lessonsAccess.isPageAccessible(item.getPageId(), getCurrentSiteId(), getCurrentUserId(), this))){
				SimplePage parent = getPage(item.getPageId());
				if (parent != null)
					needed.add(parent.getTitle());
				else
					needed.add("unknown page");  // not possible, it says
			}
			return needed;
		}

		// we've got a top level page.
		// There is no containing page, so this is just available (i.e. prerequesites).
		// We can't use the normal code because we need a list of prerequisite pages.

		if (!item.isPrerequisite()){
			return needed;
		}

		final List<SimplePageItem> items = simplePageToolDao.getOrderedTopLevelPageItems(getCurrentSite().getId());

		for (SimplePageItem i : items) {
			if (i.getSakaiId().equals(currentPageId)) {
				return needed;	// reached current page. we're done
			}
			if (i.isRequired() && !isItemComplete(i) && isItemVisible(i)) {
				needed.add(i.getName());
			}
		}

		return needed;

	}

    // maybeUpdateLinks checks to see if this page was copied from another
    // site and needs an update
    // only works if you have lessons write permission. Caller shold check
	public void maybeUpdateLinks() {

	    String needsFixup = getCurrentSite().getProperties().getProperty("lessonbuilder-needsfixup");
	    if (needsFixup != null && needsFixup.length() != 0) {

		// it's important for only one process to do the update. So instead of depending upon something
		// that can be cached and is not synced across sites, do this directly in the DB with somehting
		// atomic. Also site save is veyr heavy weight, and not well interlocked. Much better just
		// to remove the property. This should only be needed for 10 min (cache lifetime), after which
		// the test above will show that it's not needed
		//   Permission note: this should work for a student. A full site save won't. However this
		// code only gets called for people with lessons.write. Normally lessons.write is also people
		// with site.upd, but maybe not always. It should be OK for anyone to clear this flag in this code
		
		int updated = 0;
		try {
		    updated = simplePageToolDao.clearNeedsFixup(getCurrentSiteId());
		} catch (Exception e) {
		    // should get here if the flag has been removed already by another process
		    log.warn("clearneedsfixup {}", e.toString());
		}
		// only do this if there was a flag to delete
		if (updated != 0) {
		    lessonBuilderEntityProducer.updateEntityReferences(getCurrentSiteId());
		    currentSite = null;  // force refetch next time
		}
	    }

	    int fixupType = simplePageToolDao.clearNeedsGroupFixup(getCurrentSiteId());
	    if (fixupType != 0)
		lessonBuilderEntityProducer.fixupGroupRefs(getCurrentSiteId(), this, fixupType);

	}

	private boolean isCommentsItem(SimplePageItem item) {
		return item.getType() == SimplePageItem.COMMENTS;
	}

	private int findNextSequence(List<SimplePageItem> items) {
		int nseq = 0;
		for (SimplePageItem item : items) {
			if( item.getSequence() > nseq) {
				nseq = item.getSequence();
			}
		}
		return nseq++;
	}

	public boolean isItemAvailable(SimplePageItem item) {
	    return isItemAvailable(item, getCurrentPageId());
	}

	public boolean isItemAvailable(SimplePageItem item, long pageId) {
		if (item.isPrerequisite()) {
			List<SimplePageItem> items = getItemsOnPage(pageId);

			for (SimplePageItem i : items) {
			    // log.info(i.getSequence() + " " + i.isRequired() + " " + isItemVisible(i) + " " + isItemComplete(i));
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
		LessonEntity quiz = quizEntity.getEntity(i.getSakaiId(),this);
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
       private static String normalizeParams(String URL) {
	   URL = URL.replaceAll("[\\?\\&\\;]", "&");
	   return URL.replaceFirst("\\&", "?");
       }

       public static String getYoutubeKeyFromUrl(String URL) {
	   // 	see if it has a Youtube ID
	   int offset = 0;
	   if (URL.startsWith("http:"))
	       offset = 5;
	   else if (URL.startsWith("https:"))
	       offset = 6;

	   if (URL.startsWith("//www.youtube.com/", offset) || URL.startsWith("//youtube.com/", offset)) {
	       Matcher match = YOUTUBE_PATTERN.matcher(URL);
	       if (match.find()) {
		   return normalizeParams(match.group(1));
	       }
	       match = YOUTUBE2_PATTERN.matcher(URL);
	       if (match.find()) {
		   return normalizeParams(match.group(1));
	       }
	   }else if(URL.startsWith("//youtu.be/", offset)) {
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
		String URL;

		URL = i.getAttribute("multimediaUrl");
		if (URL != null)
		    return getYoutubeKeyFromUrl(URL);

		// this is called only from contexts where we know it's OK to get the data.
		// indeed if I were doing it over I'd put it in the item, not resources
		SecurityAdvisor advisor = null;
		try {
			// if(getCurrentPage().getOwner() != null) {
				// Need to allow access into owner's home directory
				advisor = new SecurityAdvisor() {
					public SecurityAdvice isAllowed(String userId, String function, String reference) {
						if(StringUtils.equalsAny(function, "content.read", "content.hidden")) {
							return SecurityAdvice.ALLOWED;
						}else {
							return SecurityAdvice.PASS;
						}
					}
				};
				securityService.pushAdvisor(advisor);
			// }
			// find the resource
			ContentResource resource;
			try {
				resource = contentHostingService.getResource(sakaiId);
			} catch (Exception ignore) {
				return null;
			}
			
			// 	make sure it's a URL
			if (resource == null ||
			    // need to check both. Sakai 10 sets only resource type, but earlier releases don't
			    // copy that when doing site copy, so for them have to check contenttype.
			    (!resource.getResourceType().equals("org.sakaiproject.content.types.urlResource") &&
			     !resource.getContentType().equals("text/url"))) {
				return null;
			}
			
			// 	get the actual URL
			try {
				URL = new String(resource.getContent());
			} catch (Exception ignore) {
				return null;
			}
			
			return getYoutubeKeyFromUrl(URL);
			
		}catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}finally {
		    if(advisor != null) securityService.popAdvisor(advisor);
		}
		
		// 	no
		return null;
	}

    // current recommended best URL for youtube. Put here because the same code is
    // used a couple of different places
        public static String getYoutubeUrlFromKey(String key) {
	    return "https://www.youtube.com/embed/" + key + "?wmode=opaque";
	}

	public String[] split(String s, String p) {
	    if (StringUtils.isBlank(s))
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

		boolean success;
		String groupId = group.getGroupId();

		try {
			if (shouldHaveAccess) {
			    success = GroupPermissionsService.addCurrentUser(getCurrentPage().getSiteId(), getCurrentUserId(), groupId);
			} else {
			    success = GroupPermissionsService.removeUser(getCurrentPage().getSiteId(), getCurrentUserId(), groupId);
			}
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
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
			    authzGroupService.getAuthzGroup(groupId);
			    // group exists, it was something else. Who knows what
			    return;
			} catch (org.sakaiproject.authz.api.GroupNotDefinedException ee) {
				
			} catch (Exception e) {
			    // some other failure from getAuthzGroup, shouldn't be possible
			    log.warn("checkItemPermissions unable to join or unjoin group {}", groupId);
			}

			log.warn("checkItemPermissions: User seems to have deleted group {}. We'll recreate it.", groupId);

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

        public String[] fixFileName(String collectionId, String name) {
		String[] ret = new String[2];
		ret[0] = "";
		ret[1] = "";

		if (StringUtils.isBlank(name))
		    return ret;

		int i = name.lastIndexOf("/");
		if (i >= 0)
		    name = name.substring(i+1);
		String base = name;
		String extension = org.springframework.util.StringUtils.getFilenameExtension(name);
		base = StringUtils.removeEnd(name, "." + extension);

		base = Validator.escapeResourceName(base);
		extension = Validator.escapeResourceName(extension);
		ret[0] = base;
		ret[1] = extension;

		// that was easy. But now have to deal with names that are too long

		// the longest identifier content service will actually take is 247. Note sure why
		// from that subtract 1 for the period, and 4 for _xxx if we have duplicates
		// that would give 242. Actually use 240, just for safety.
		int maxname = 240 - collectionId.length();

		if (maxname < 1) {
		    return ret;  // nothing we can do. let other layers return error
		}

		int namelen = name.length();
		
		if (namelen <= maxname)
		    return ret;  // easy case, no problem

		int overage = namelen - maxname; // amount to cut

		// doesn't seem to make sense to use ellipses for less than length of 8. better just to truncate
		// if possible, truncate the base
		if (base.length() > (overage + 8)) {
		    ret[0] = org.apache.commons.lang3.StringUtils.abbreviateMiddle(name, "_", maxname - extension.length());
		    return ret;
		}

		// but what about b.eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee, or more likely, .xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
		if (extension.length() > (overage + 8)) {
		    ret[1] = org.apache.commons.lang3.StringUtils.abbreviateMiddle(extension, "_", maxname - base.length());
		    return ret;
		}

		// not enough for both name and extension. just use name
		ret[1] = "";
		    
		if (base.length() <= maxname)
		    return ret;

		// base has to be larger than maxname, so final length will be maxname
		if (maxname > 8) {
		    ret[0] = org.apache.commons.lang3.StringUtils.abbreviateMiddle(base, "_", maxname);
		    return ret;
		}

		ret[0] = base.substring(0, maxname);  // string is longer than maxname by test above
		ret[1] = "";

		return ret;

	}


// for group-owned student pages, put it in the worksite of the current user
	public String getCollectionId(boolean urls) {
		String siteId = getCurrentPage().getSiteId();
		String baseDir = ServerConfigurationService.getString("lessonbuilder.basefolder", null);
		boolean hiddenWithAccessDir = ServerConfigurationService.getBoolean("lessonbuilder.folder.hidden.withaccess", true);
		String pageOwner = getCurrentPage().getOwner();
		String collectionId;
		String folder;
		// for owned pages, use the same kind of hierarchy. One exception: use site name as basedir
		if (pageOwner != null) {
		    String title = getCurrentSite().getTitle();
		    baseDir = Validator.escapeResourceName(org.apache.commons.lang3.StringUtils.abbreviateMiddle(title,"_",30));
		}		    

		// if (pageOwner == null) {
			collectionId = contentHostingService.getSiteCollection(siteId);
			if (baseDir != null) {
			    if (!baseDir.endsWith("/"))
				baseDir = baseDir + "/";
			    collectionId = collectionId + baseDir;
			    // basedir which is hidden; have to create it if it doesn't exist, so we can make hidden with access
				try {
				    try {
					contentHostingService.checkCollection(collectionId);
				    } catch (IdUnusedException idex) {
					ContentCollectionEdit edit = contentHostingService.addCollection(collectionId);
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME,  Validator.escapeResourceName(baseDir.substring(0,baseDir.length()-1)));
					edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, String.valueOf(hiddenWithAccessDir));
					contentHostingService.commitCollection(edit);
				    }
				} catch (Exception ignore) {
				    // I've been ignoring errors.
				    // that will cause failure at a later stage where we can
				    // return an error message. This may not be optimal.
				}
			}
			// actual folder. Use hierarchy of files
			SimplePage page = getCurrentPage();
			String folderString = page.getFolder();
			if (folderString != null) {
			    folder = collectionId + folderString; 
			} else {
			    Path path = getPagePath(page, new HashSet<>());
			    String title = path.title;

			    // there's a limit of 255 to resource names. Leave 30 chars for the file.
			    // getPagePath limits folder names in the hiearchy to 30, but in weird situations
			    // we could a very deep hierarchy and the whole thing could get too long. If that
			    // happens, just use the page name. We assume the collection ID will be /group/UUID, 
			    // so it will always be reasonable. In theory we could have to do yet another test
			    // on collection ID.

			    // 33 is a name of length 30 and -NN for duplicates
			    // actual length is 255, but I worry about weird characters I don't understand
			    if (title.length() > (250 - collectionId.length() - 33)) {
				title = Validator.escapeResourceName(org.apache.commons.lang3.StringUtils.abbreviateMiddle(getPageTitle(),"_",30)) + "/";
			    }
			    
			    // make sure folder names are unique
			    if (simplePageToolDao.doesPageFolderExist(getCurrentSiteId(), title)) {
				String base = title.substring(0, title.length() - 1);
				for (int suffix = 1; suffix < 100; suffix++) {
				    String trial = base + "-" + suffix + "/";
				    if (!simplePageToolDao.doesPageFolderExist(getCurrentSiteId(), trial)) {
					title = trial;
					break;
				    }
				}
			    }

			    folder = collectionId + title;
			    page.setFolder(title);			    
			    simplePageToolDao.quickUpdate(page);
			}
			// folder = collectionId + Validator.escapeResourceName(getPageTitle()) + "/";
		// }else {
		//	collectionId = "/user/" + getCurrentUserId() + "/stuff4/";
		//	// actual folder -- just use page name for student content
		//	folder = collectionId + Validator.escapeResourceName(getPageTitle()) + "/";
		//}

	    // folder we really want
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
				edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, String.valueOf(hiddenWithAccessDir));
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
	    	else {
	    		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_DISPLAY_NAME, Validator.escapeResourceName(getPageTitle()));
	    		edit.getPropertiesEdit().addProperty(ResourceProperties.PROP_HIDDEN_WITH_ACCESSIBLE_CONTENT, String.valueOf(hiddenWithAccessDir));
		}		
	    	contentHostingService.commitCollection(edit);
	    	return folder; // worked. use it
		} catch (Exception ignore) {};

	    // didn't. do the best we can
		return collectionId;
	}

	public class Path {
	    public int level;
	    public String title;
	    public Path(int level, String title) {
		this.level = level;
		this.title = title;
	    }
	}

    // not implemented for student pages
	public Path getPagePath(SimplePage page, Set<Long>seen) {
	    seen.add(page.getPageId());
	    List<SimplePageItem> items = simplePageToolDao.findPageItemsBySakaiId(Long.toString(page.getPageId()));
	    if (items == null || items.isEmpty()) {
		return new Path(0, Validator.escapeResourceName(org.apache.commons.lang3.StringUtils.abbreviateMiddle(page.getTitle(),"_",30)) + "/");
	    }
	    else {
		int minlevel = 9999;
		String bestPath = "";
		for (SimplePageItem i: items) {
		    SimplePage p = simplePageToolDao.getPage(i.getPageId());
		    if (p == null)
			continue;
		    if (isStudentPage(p)) { // probably can't happen
			    continue;
		    }
		    if (seen.contains(p.getPageId()))  // already seen this page, we're in a loop
			continue;
		    Path path = getPagePath(p, seen);
		    // there can be loops in the network
		    if (path.level < minlevel) {
			minlevel = path.level;
			bestPath = path.title;
		    }
		}
		if (bestPath.equals(""))
		    return new Path(0, Validator.escapeResourceName(org.apache.commons.lang3.StringUtils.abbreviateMiddle(page.getTitle(),"_",30)) + "/");
		return new Path(minlevel + 1, bestPath + Validator.escapeResourceName(org.apache.commons.lang3.StringUtils.abbreviateMiddle(page.getTitle(),"_",30)) + "/");
	    }
	}

	public boolean isHtml(SimplePageItem i) {
		String extension = org.springframework.util.StringUtils.getFilenameExtension(i.getSakaiId());
					    
	    // we are just starting to store the MIME type for resources now. So existing content
	    // won't have them.
		String mimeType = i.getHtml();
		if (mimeType != null && (mimeType.startsWith("http") || StringUtils.isBlank(mimeType)))
			mimeType = null;
	    
		return mimeType != null && StringUtils.equalsAny(mimeType, "text/html","application/xhtml+xml")
			   || mimeType == null && StringUtils.equalsAny(extension, "html","htm");
	}

	public static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;

	// called by dialog to add inline multimedia item, or update existing
	// item if itemid is specified
	// NOTE: in a group-owned student page, the files are put in the home directory
	// of the current user. That's the only consistent approach I could come up with
	// this function uses a security advisor, so that will work.
	public void addMultimedia() {

	    // This code must be read together with the SimplePageItem.MULTIMEDIA
	    // display code in ShowPageProducer.java (To find it search for
	    // multimediaDisplayType) and with the code in show-page.js that
	    // handles the add multimedia dialog (look for #mm-add-item)

		// historically this code was to display files ,and urls leading to things
		// like MP4. as backup if we couldn't figure out what to do we'd put something
		// in an iframe. The one exception is youtube, which we supposed explicitly.
		// However we now support several ways to embed content. We use the
		// multimediaDisplayType code to indicate which. The codes are
		// 	 1 -- embed code, 2 -- av type, 3 -- oembed, 4 -- iframe
		// 2 is the original code: MP4, image, and as a special case youtube urls
		// For all practical purposes type 2 is the same as the old items that don't
		// have type codes (although iframes are also handled by the old code)
		// the old code creates ojbects in ContentHosting for both files and URLs.
		// The new code saves the embed code or URL itself as an atteibute of the item
		// If I were doing it again, I wouldn't create the ContebtHosting item
		// Note that IFRAME is only used for something where the far end claims the MIME
		// type is HTML. For weird stuff like MS Word files I use the file display code, which
		// ShowPageProducer figures out how to display type 2 (or default) items 
		// on the fly, so we don't have to known here what they are.

		SecurityAdvisor advisor = null;
		try {
			advisor = pushAdvisor();
			if (!itemOk(itemId))
				return;
			if (!canEditPage())
				return;
			if (!checkCsrf())
			    return;

			// For the sole case of adding multimedia as content links above another item
			// (i.e., not the Add Content button at the top of the Lessons page, nor the Add Content '+'
			// button at the bottom of a section), the items below the newly inserted content links
			// need to have their sequence values revised, especially if there is more than one item to insert.
			// First, collect these items in the itemsToFix list before inserting the new content links (which
			// affects item sequencing for the page). After inserting the new content links, revise the sequence
			// values for itemsToFix.
			List<SimplePageItem> itemsToFix = null;
			boolean fixItemSequence = StringUtils.isNotEmpty(addBefore) && !StringUtils.startsWith(addBefore, "-");
			if (fixItemSequence) {
				List<SimplePageItem> items = getItemsOnPage(getCurrentPageId());
				items.sort(Comparator.comparing(SimplePageItem::getSequence));
				long addBeforeItemId = Long.valueOf(addBefore);
				int addBeforeIndex = IntStream.range(0, items.size())
					.filter(i -> addBeforeItemId == items.get(i).getId())
					.findFirst()
					.orElse(-1);
				if (addBeforeIndex >= 0) {
					itemsToFix = items.stream().skip(addBeforeIndex).collect(Collectors.toList());
				}
			}

			// SAK-41846 - Initialize counters to keep track of files to add and the item sequence values to adjust
			totalMultimediaFilesToAdd = multipartMap.size();
			remainingMultimediaFilesToAdd = totalMultimediaFilesToAdd;

			if (multipartMap.size() > 0) {
				// 	user specified a file, create it
				String[] fnames = new String[0];
				// names always gets sent. Only use it if there is something there
				if (names.length() > 0)
				    fnames = names.split("\n");
				int fileindex = 0;
				for (MultipartFile file : multipartMap.values()) {
					if (file.isEmpty()) {
						file = null;
					}
					// for file uploads only, name is in names rather than name
					String fname = name;
					if (fnames.length > fileindex) {
					    fname = fnames[fileindex].trim();
					}
					name = null;  // don't reuse name
					addMultimediaFile(file, fname);
					fileindex++;
				}
			}

			if (fixItemSequence) {
				for (SimplePageItem itemToFix : itemsToFix) {
					itemToFix.setSequence(itemToFix.getSequence() + totalMultimediaFilesToAdd);
					update(itemToFix);
				}
			}

		} catch (Exception exception) {
			log.error(exception.getMessage(), exception);
		} finally {
			popAdvisor(advisor);

			// Reset these counters
			totalMultimediaFilesToAdd = 0;
			remainingMultimediaFilesToAdd = 0;
		}
	}

	public void addMultimediaFile(MultipartFile file, String name){
		try{
			
			String sakaiId = null;
			String mimeType = null;
			// urlResource is for an item that's going to be type RESOURCE
			// but is a URL. We used to create URL resource objects, but we
			// no longer do. Now it's an attribute.
			String urlResource = null;
			
			if (file != null) {
				if (!uploadSizeOk(file))
				    return;

				String collectionId = getCollectionId(false);
				// 	user specified a file, create it
				String fname = file.getOriginalFilename();
				if (StringUtils.isBlank(fname))
					fname = file.getName();

				mimeType = file.getContentType();
				try {
					ContentResourceEdit res;
					if (itemId != -1 && replacefile) {
					    // upload new version -- get existing file
					    SimplePageItem item = findItem(itemId);
					    String resId = item.getSakaiId();
					    res = contentHostingService.editResource(resId);
					} else {
					    // otherwise create a new file
					    if (isWebsite) {
							
						// the code below tests whether it's actually a zip file. But we can't be sure it is until after
						// the file is saved, since the kernel does the test. Thus about all we can do is check isWebsite.
						// that indicates that the user intended it to be a zip file.
						// in order to expand it, the kernel needs an extension
						if (fname.lastIndexOf(".") < 1)
						    fname = fname + ".zip";
					    }
					    String[] names = fixFileName(collectionId, fname);
					    res = contentHostingService.addResource(collectionId, names[0], names[1], MAXIMUM_ATTEMPTS_FOR_UNIQUENESS);
					}
					if (isCaption)
					    res.setContentType("text/vtt");
					// don't use mime type, to give kernel a chance to look at the contents
					//else if (!"application/octet-stream".equals(mimeType))
					//  res.setContentType(mimeType);
					res.setContent(file.getInputStream());
					try {
						contentHostingService.commitResource(res,  notificationService.NOTI_NONE);
						// reset mime type. kernel may have improved it if it was null
						String newMimeType = res.getContentType();
						if ((StringUtils.isBlank(newMimeType) || newMimeType.equals("application/octet-stream")) && StringUtils.isNotBlank(mimeType)) {
						    // kernel didn't find anything useful. If browser sent something, use it
						    res = contentHostingService.editResource(res.getId());
						    res.setContentType(mimeType);
						    contentHostingService.commitResource(res,  notificationService.NOTI_NONE);
						}
						// note that we don't save the mime type in the lessons item anymore
						// display code will use the item type from resources
						// 	there's a bug in the kernel that can cause
						// 	a null pointer if it can't determine the encoding
						// 	type. Since we want this code to work on old
						// 	systems, work around it.
					} catch (java.lang.NullPointerException e) {
						setErrMessage(messageLocator.getMessage("simplepage.resourcepossibleerror"));
					}

					sakaiId = res.getId();

					if(StringUtils.equalsAny(mimeType, "application/zip", "application/x-zip-compressed")  && isWebsite) {
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
					log.error("addMultimedia error 1 {}", e.toString());
					return;
				}
				// if user supplied name use it, else the filename
				// if multiple files, the user supplied name is for first only, or we'd
				// have several links with the same name
				if (StringUtils.isBlank(name)) {
				    name = fname;
				}
			} else if (StringUtils.isNotBlank(mmUrl) && multimediaDisplayType != 1 && multimediaDisplayType != 3) {
				// 	user specified a URL, create the item
				String url = normUrl(mmUrl);
				
				// generate name if user didn't supply one
				if (StringUtils.isBlank(name)) {
				    name = url;
				}

				// default name should be "web page". But this is late enough that I don't
				// want to add strings, so it's going to be "Web ". Hopefully this will never
				// happen. It's hard to see how there could be a URL with no hostname or path
				if (name == null)
				    name = messageLocator.getMessage("simplepage.web_content").replace("{}","");

				// don't let names get too long
				if (name.length() > 80)
				    name = name.substring(0,39) + "..." + name.substring(name.length()-39);
				// as far as I can see, this is used only to find the extension, so this is OK
				sakaiId = PREFIX_URL + name;

				urlResource = url;
				// new dialog passes the mime type
				if (StringUtils.isNotBlank(multimediaMimeType))
				    mimeType = multimediaMimeType;
				else
				    mimeType = getTypeOfUrl(url);
				
			} else if (StringUtils.isNotBlank(mmUrl) && (multimediaDisplayType == 1 || multimediaDisplayType == 3)) {
			    // fall through. we have an embed code, don't need file
			} else
				// 	nothing to do
				return;
			
			// 	itemId tells us whether it's an existing item
			// 	isMultimedia tells us whether resource or multimedia
			// 	sameWindow is only passed for existing items of type HTML/XHTML
			//  for new items it should be set true for HTML/XTML, false otherwise
			//  for existing items it should be set to the passed value for HTML/XMTL, false otherwise
			//  it is ignored for isMultimedia, as those are always displayed inline in the current page
			
			SimplePageItem item;
			if (itemId == -1 && isMultimedia) {
			    item = appendItem(sakaiId, name, SimplePageItem.MULTIMEDIA);
			} else if (itemId == -1) {
			    item = appendItem(sakaiId, name, SimplePageItem.RESOURCE);
			} else if (isCaption) {
				item = findItem(itemId);
				if (item == null)
					return;
				item.setAttribute("captionfile", sakaiId);
				update(item);
				return;
			} else {
				item = findItem(itemId);
				if (item == null)
					return;
				
				item.setSakaiId(sakaiId);
				// the UI shows the existing name and lets the user change it, so we
				// can always use the name from the UI
				if (name != null && !name.trim().equals(""))
				{
					item.setName(name);
				}
			}
			
			// for new file, old captions don't make sense
			item.removeAttribute("captionfile");
			// remember who added it, for permission checks
			item.setAttribute("addedby", getCurrentUserId());

			item.setPrerequisite(this.prerequisite);

			if (mimeType != null) {
				item.setHtml(mimeType);
			} else {
				item.setHtml(null);
			}
			
			if (urlResource != null) { // link to item, where item is a URL
			    String nurl = urlResource;
			    item.setAttribute("multimediaUrl", nurl);
			    item.setSakaiId(sakaiIdFromUrl(nurl, item));
			}
			if (StringUtils.isNotBlank(mmUrl) && isMultimedia) {
			    // embed item, where item is a URL or embed code
			    if (multimediaDisplayType == 1)
				// the code is filtered by the UI, so the user can see the effect.
				// This protects against someone handcrafting a post.
				// The code is similar to that in submit, but currently doesn't
				// have folder-specific override (because there are no folders involved)
				item.setAttribute("multimediaEmbedCode", AjaxServer.filterHtml(mmUrl.trim()));
			    else if (multimediaDisplayType == 3) {
				String nurl = normUrl(mmUrl);
				item.setAttribute("multimediaUrl", nurl);
				item.setSakaiId(sakaiIdFromUrl(nurl, item));
			    }
			    item.setAttribute("multimediaDisplayType", Integer.toString(multimediaDisplayType));
			}
			// 	if this is an existing item and a resource, leave it alone
			// 	otherwise initialize to false
			if (isMultimedia || itemId == -1)
				item.setSameWindow(false);

			clearImageSize(item);

			// SAK-41846 - Adjust the sequence if mutliple files are being added simultaneously
			if (totalMultimediaFilesToAdd > 0) {
			    int diff = totalMultimediaFilesToAdd - remainingMultimediaFilesToAdd;
			    if (diff > 0) {
				item.setSequence(item.getSequence() + diff);
			    }
			    remainingMultimediaFilesToAdd--;
			}

			try {
			    //		if (itemId == -1)
			    //		saveItem(item);
			    //		  else
					saveOrUpdate(item);
			} catch (Exception e) {
			    log.info("save error {}", e.toString());
				// 	saveItem and update produce the errors
			}
		}catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}

	// for types where we save the URL directly we need a unique Sakai id. It
	// has to be unique, not too long, and it has to end in the right extension.
	// has to be unique because this is sometimes used as a key for caching
	// The item ID is to make it unique
	public String sakaiIdFromUrl(String url, SimplePageItem item) {
	    if (url.length() > 80)
		url = url.substring(url.length()-80);
	    return PREFIX_URL + item.getId() + "/" + url;
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

	public List<Map<String, Object>> getToolsImportItem() {
		return ltiService.getToolsImportItem(getCurrentSiteId());
	}

	public void handleImportItem() {
		ToolSession toolSession = sessionManager.getCurrentToolSession();
		if (toolSession != null) toolSession.setAttribute("lessonbuilder.fileImportDone", "true");
                String contentItemUrl = ToolUtils.getRequestParameter("content_item_url");
                log.debug("handleImportItem contentItemUrl={}", contentItemUrl);

                // Retrieve the tool associated with the content item
                String toolId = ToolUtils.getRequestParameter("toolId");
                Long toolKey = LTIUtil.toLongNull(toolId);
                if ( toolKey == 0 || toolKey < 0 ) {
                        setErrKey("simplepage.lti-import-error-id", toolId);
                        return;
                }

                Map<String, Object> tool = ltiService.getTool(toolKey, getCurrentSiteId());
                if ( tool == null ) {
                        setErrKey("simplepage.lti-import-error-id", toolId);
                        return;
                }

		InputStream fis;
		if ( contentItemUrl != null && contentItemUrl.length() > 1 ) {
			try {
				URL parsedUrl = new URL(contentItemUrl);
				URLConnection yc = parsedUrl.openConnection();
				fis = yc.getInputStream();
			} catch ( Exception e ) {
				setErrKey("simplepage.lti-import-error-reading-url", contentItemUrl);
				log.error(e.getMessage(), e);
				return;
			}

			// log.info("Importing...");
			long length = importCcFromStream(fis);
			if ( length > 0 && toolSession != null) {
				String successMessage = messageLocator.getMessage("simplepage.lti-import-success-length").replace("{}", length+"");
				toolSession.setAttribute("lessonbuilder.fileImportDone", successMessage);
			}
		} else {
			setErrKey("simplepage.lti-import-missing-url", null);
		}
	}

	// Import a Common Cartridge
	public void importCc() {
	    if (!canEditPage())
		return;
	    if (!checkCsrf())
		return;

	    // Import an uploaded file
	    MultipartFile file = null;
	    if (multipartMap.size() > 0) {
		// user specified a file, create it
		file = multipartMap.values().iterator().next();
	    }

	    InputStream fis;
	    if (file != null) {
		if (!uploadSizeOk(file))
		    return;

		try {
		    fis = file.getInputStream();
		} catch(IOException e) {
		    setErrKey("simplepage.cc-error", "");
		    log.error(e.getMessage(), e);
		    return;
		}
		long length = importCcFromStream(fis);
		setTopRefresh();
	    }
	}

	// Import a Common Cartridge form an InputStream
	private long importCcFromStream(InputStream fis) {

		File cc = null;
		File root = null;
	        long length = 0;
		try {
		    cc = File.createTempFile("ccloader", "file");
		    root = File.createTempFile("ccloader", "root");
            log.debug("cc = {} root={}", cc.getAbsoluteFile(), root.getAbsoluteFile());
		    if (root.exists()) {
			if (!root.delete()) {
			    setErrMessage("unable to delete temp file for load");
			    return -1;
			}
		    }
		    if (!root.mkdir()) {
			setErrMessage("unable to create temp directory for load");
			return -1;
		    }
			BufferedOutputStream bos;
			try (BufferedInputStream bis = new BufferedInputStream(fis))
			{
				bos = new BufferedOutputStream(new FileOutputStream(cc));
				byte[] buffer = new byte[8096];
				int n = 0;
				while ((n = bis.read(buffer, 0, 8096)) >= 0) {
					if (n > 0) {
						bos.write(buffer, 0, n);
						length += n;
					}
				}
			}
		    bos.close();

		    CartridgeLoader cartridgeLoader = ZipLoader.getUtilities(cc, root.getCanonicalPath());

		    // Force the unzip to happen as it would have happened anyways
			if ( importArchive ) {
				log.debug("checking for embedded site archive");
				cartridgeLoader.unzip();
				String unzipPath = cartridgeLoader.getUnzipPath();

				log.debug("unzipPath={}", unzipPath);

				// Check if this is a CC + sakai_archive
				String archivePath = unzipPath + "/sakai_archive/";
				File archive = new File(archivePath);
				if ( archive.isDirectory() ) {
					String userId = userDirectoryService.getCurrentUser().getId();
					String siteId = getCurrentSiteId();
					log.debug("archive.merge( {}, {}, {})", archivePath, userId, siteId);
					archiveService.merge(archivePath, siteId, userId);
					return 0;
				}
			}

		    Parser parser = Parser.createCartridgeParser(cartridgeLoader);

		    LessonEntity quizobject = quizEntity;
		    for (LessonEntity q = quizEntity; q != null; q = q.getNextEntity()) {
			if (q.getToolId().equals(quiztool))
			    quizobject = q;
		    }
		    
		    LessonEntity assignobject = assignmentEntity;
		    for (LessonEntity q = assignmentEntity; q != null; q = q.getNextEntity()) {
			if (q.getToolId().equals(assigntool))
			    assignobject = q;
		    }
		    

		    LessonEntity topicobject = forumEntity;
		    for (LessonEntity q = forumEntity; q != null; q = q.getNextEntity()) {
			if (q.getToolId().equals(topictool))
			    topicobject = q;
		    }

		    log.debug("parser.parse {} {} {} {} {} {} {}", this, cartridgeLoader, simplePageToolDao, quizobject, topicobject, bltiEntity, assignobject);
		    parser.parse(new PrintHandler(this, cartridgeLoader, simplePageToolDao, quizobject, topicobject, bltiEntity, assignobject, importtop));
		} catch (Exception e) {
		    setErrKey("simplepage.cc-error", "");
		  
		    log.error(e.getMessage(), e);
		    length = -1;
		} finally {
		    if (cc != null)
			try {
			    deleteRecursive(cc);
			} catch (Exception e){
			    
			}
			try {
			    deleteRecursive(root);
			} catch (Exception e){
			    
			}
		}
		return length;
	}

	// called by edit dialog to update parameters of a Youtube item
	public void updateYoutube() {
		if (!itemOk(youtubeId))
		    return;
		if (!canEditPage())
		    return;
		if (!checkCsrf())
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
		if (!key.equals(oldkey)) {
		    String url = "https://youtu.be/" + key;
		    item.setName("youtub.be/" + key);
		    item.setSakaiId(sakaiIdFromUrl(url, item));
		    item.setAttribute("multimediaUrl", url);
		    item.setAttribute("multimediaDisplayType", "2");
		}

		// even if there's some oddity with URLs, we do these updates
		item.setHeight(height);
		item.setWidth(width);
		item.setDescription(description);
		item.setPrerequisite(this.prerequisite);

		setItemGroups(item, selectedGroups);
		update(item);

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
			if (!roleList.contains(SiteService.SECURE_UPDATE_SITE) && !visible) {
				if (roleList.length() > 0) {
					roleList += ",";
				}
				roleList += SiteService.SECURE_UPDATE_SITE;
				saveChanges = true;
			} else if ((roleList.contains( SiteService.SECURE_UPDATE_SITE )) && visible) {
				roleList = roleList.replaceAll("," + SiteService.SECURE_UPDATE_SITE, "");
				roleList = roleList.replaceAll(SiteService.SECURE_UPDATE_SITE, "");
				saveChanges = true;
			}

			if (saveChanges) {
				roleConfig.setProperty("functions.require", roleList);
				if (visible) {
				    roleConfig.remove(ToolManager.PORTAL_VISIBLE);
				} else {
					roleConfig.setProperty(ToolManager.PORTAL_VISIBLE, "false");
				}

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
		if (!checkCsrf())
		    return;

		SimplePageItem item = findItem(itemId);
		item.setHeight(height);
		item.setWidth(width);
		item.setDescription(description);
		item.setPrerequisite(prerequisite);
		item.setHtml(mimetype);

		setItemGroups(item, selectedGroups);
		update(item);

	}
	
	public void addCommentsSection() {
		if (!canEditPage()) 
		    return;
		if (!checkCsrf())
		    return;

			SimplePageItem item = appendItem("", messageLocator.getMessage("simplepage.comments-section"), SimplePageItem.COMMENTS);
			item.setDescription(messageLocator.getMessage("simplepage.comments-section"));
			saveItem(item);
			
			// Must clear the cache so that the new item appears on the page
			itemsCache.remove(getCurrentPage().getPageId());

	}
	
	/**
	 *  Admins can always edit.  Authors can edit for 30 minutes.
	 *  
	 *  The second parameter is only used to distinguish this method from
	 *  the one directly below it.  Allowing CommentsProducer to cache whether
	 *  or not the current user can edit the page, without having to hit the
	 *  database each time.
	 *  
	 * @param c
	 * @param canEditPage
	 * @return
	 */
	public boolean canModifyComment(SimplePageComment c, boolean canEditPage) {
		if(canEditPage) return true;
		
		if(c.getAuthor().equals(userDirectoryService.getCurrentUser().getId())){
			// Author can edit for 30 minutes.
			return System.currentTimeMillis() - c.getTimePosted().getTime() <= 1800000;
		}else {
			return false;
		}
	}
	
	// See method above
	public boolean canModifyComment(SimplePageComment c) {
		return canModifyComment(c, canEditPage());
	}
	
	// May add or edit comments
        // handle situation where user opens CKedit and then opens a
        // different page. So we can't depend upon current page.
	public String addComment() {
		if (!checkCsrf())
		    return "permission-failed";

		boolean html = false;
		
		// Patch in the fancy editor's comment, if it's been used
		if(StringUtils.isNotBlank(formattedComment)) {
			comment = formattedComment;
			html = true;
		}
		
		comment = formattedText.processFormattedText(comment, null, null);
		
		// get this from itemId to avoid issues if someone has opened
		// a different page in another window
		Long currentPageId;
		SimplePageItem commentItem = findItem(itemId);
		if (commentItem == null) {
		    // should be impossible
		    return "failure";
		}
		currentPageId = commentItem.getPageId();
		if (currentPageId == -1L) {
		    // student page. item doesn't have pageid because the same item
		    // is on all pages / subpages for that student. instead the sakaid
		    // points to a studentpage entry.
		    long studentPageId = -1L;
		    try {
			studentPageId = Long.parseLong(commentItem.getSakaiId());
		    } catch (Exception e){}
		    SimpleStudentPage studentPage = null;
		    if (studentPageId != -1L) 
			studentPage = simplePageToolDao.findStudentPage(studentPageId);
		    // this gets the top-level student page for this student.
		    // that seems good enough for testing whether the user has successfully gotten to the page
		    if (studentPage != null)
			currentPageId = studentPage.getPageId();
		}
		SimplePage currentPage = getPage(currentPageId);
		if (currentPage == null) {
		    // should be impossible
		    return "failure";
		}

		// student page
		boolean isStudent = isStudentPage(currentPage);

		// testing whether user can get to the page is complex.
		// but you can't add a comment to a page you haven't seen,
		// so just check that. After that we know user can read page.
                // Use methods that let us pass page so we don't use current page
		// Student pages don't use avaiable / visible tests

		if (!simplePageToolDao.isPageVisited(currentPageId, getCurrentUserId(), currentPage.getOwner()) ||
		    !isStudent && !isItemAvailable(commentItem, currentPageId) ||
		    !isStudent && !isItemVisible(commentItem, currentPage, true)) {
		    // security failure
		    return "failure";
		}		    

		if(StringUtils.isBlank(comment)) {
			setErrMessage(messageLocator.getMessage("simplepage.empty-comment-error"));
			return "failure";
		}
		
		if(StringUtils.isBlank(editId)) {
			String userId = userDirectoryService.getCurrentUser().getId();
			
			Double grade = null;
			if(commentItem.getGradebookId() != null) {
				List<SimplePageComment> comments = simplePageToolDao.findCommentsOnItemByAuthor(itemId, userId);
				if(comments != null && comments.size() > 0) {
					grade = comments.get(0).getPoints();
				}
			}
			
			SimplePageComment commentObject = simplePageToolDao.makeComment(itemId, currentPageId, userId, comment, idManager.createUuid(), html);
			commentObject.setPoints(grade);
			
			// Complete user task
			this.completeUserTask(itemId, userId);
			
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
		
		if(isStudentPage(getCurrentPage())) {
			SimpleStudentPage student = simplePageToolDao.findStudentPage(currentPage.getTopParent());
			student.setLastCommentChange(new Date());
			update(student, false);
		}
		
		return "added-comment";
	}

	public String updateComments() {
		if (!checkCsrf())
		    return "permission-failed";

		if(canEditPage()) {
			SimplePageItem comment = findItem(itemId);

			comment.setAnonymous(anonymous);
			comment.setRequired(required);
			comment.setPrerequisite(prerequisite);

			if (maxPoints == null || maxPoints.equals("")) {
				maxPoints = "1";
			}

			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId()) && (selectedGroups == null || selectedGroups.length <= 0)) {
				setErrMessage(messageLocator.getMessage("simplepage.multi_gradebook.no_group"));
				return "failure";
			}

			Integer points = parseMaxPoints(maxPoints);

			if (graded) {
				if (points == null) {
					return "failure";
				}

				String pageTitle;
				String gradebookId;
				boolean add = true;

				List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
				List<String> commentGroupList = comment.getGroups() != null ? Arrays.asList(comment.getGroups().split(",")) : new ArrayList<>();

				if (comment.getPageId() >= 0) {
					pageTitle = getPage(comment.getPageId()).getTitle();
					gradebookId = "lesson-builder:comment:" + comment.getId();

					String commentTitle = " Comments (item:" + comment.getId() + ")";

					if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
						gradebookUids = Arrays.asList(selectedGroups);

						List<String> addedGroups = buildGradebookUidList(commentGroupList, gradebookUids, "CREATE");
						List<String> updatedGroups = buildGradebookUidList(commentGroupList, gradebookUids, "UPDATE");
						List<String> deletedGroups = buildGradebookUidList(commentGroupList, gradebookUids, "DELETE");

						if (addedGroups != null && addedGroups.size() > 0) {
							add = gradebookIfc.addExternalAssessment(addedGroups, getCurrentSiteId(), gradebookId, null,
								pageTitle + commentTitle, Integer.valueOf(maxPoints), null, LessonBuilderConstants.TOOL_ID);
						}

						if (updatedGroups != null && updatedGroups.size() > 0) {
							add = gradebookIfc.updateExternalAssessment(updatedGroups, gradebookId, null,
								pageTitle + commentTitle, Integer.valueOf(maxPoints), null);
						}

						if (deletedGroups != null && deletedGroups.size() > 0) {
							add = gradebookIfc.removeExternalAssessmentByGradebookList(deletedGroups, gradebookId);
						}

						if (!add) {
							setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
						} else {
							comment.setGradebookTitle(pageTitle + commentTitle);
						}

					} else {
						if (comment.getGradebookId() != null && !comment.getGradebookPoints().equals(points)) {
							add = gradebookIfc.updateExternalAssessment(gradebookUids, gradebookId, null,
									pageTitle + commentTitle, Integer.valueOf(maxPoints), null);
						} else {
							try {
								add = gradebookIfc.addExternalAssessment(gradebookUids, getCurrentSiteId(), gradebookId, null,
									pageTitle + commentTitle, Integer.valueOf(maxPoints), null, LessonBuilderConstants.TOOL_ID);
							} catch(ConflictingAssignmentNameException cane) {
								add = false;
								setErrMessage(messageLocator.getMessage("simplepage.existing-gradebook"));
							}
						}
					}

					if (!add) {
						setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
					} else {
						comment.setGradebookTitle(pageTitle + commentTitle);
					}
				} else {
					// Must be a student page comments tool.
					SimpleStudentPage studentPage = simplePageToolDao.findStudentPage(Long.valueOf(comment.getSakaiId()));
					SimplePageItem studentPageItem = simplePageToolDao.findItem(studentPage.getItemId());

					gradebookId = "lesson-builder:page-comment:" + studentPageItem.getId();
				}

				if (add) {
					comment.setGradebookId(gradebookId);
					comment.setGradebookPoints(points);
					regradeComments(comment);
				}
			} else if (comment.getGradebookId() != null && comment.getPageId() >= 0) {
				gradebookIfc.removeExternalAssessment(getCurrentSiteId(), comment.getGradebookId());
				comment.setGradebookId(null);
				comment.setGradebookPoints(null);
				selectedGroups = new String[] {};
			}

			setItemGroups(comment, selectedGroups);

			// Create or update a task
			String reference = "/lessonbuilder/item/" + comment.getId();
			String title = getPage(comment.getPageId()).getTitle() + " - " + messageLocator.getMessage("simplepage.comments-task-title");
                        // Dashboard task widget: When a comment item is added create a task item for the site members.
			createOrUpdateTask(reference, getCurrentSiteId(), title, null);

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
				gradebookIfc.updateExternalAssessmentScore(getCurrentSiteId(), getCurrentSiteId(), comment.getGradebookId(),
						c.getAuthor(), String.valueOf(c.getPoints()));
			}
		}
	}
	
	/**
	 * Comments aren't actually deleted. The comment field is set to empty.
	 * This is so that the namings remain consistent when the comment section
	 * is set to show names as anonymous.  Otherwise, deleting a post could change
	 * the numbering, which hinders discussion.
	 * @param commentUUID
	 * @return
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
		if (!canEditPage()) {
		    return;
		}
		if (!checkCsrf())
		    return;

		if(canEditPage()) {
			SimplePageItem item = appendItem("", messageLocator.getMessage("simplepage.student-content"), SimplePageItem.STUDENT_CONTENT);
			item.setDescription(messageLocator.getMessage("simplepage.student-content"));
			saveItem(item);
			
			// Must clear the cache so that the new item appears on the page
			itemsCache.remove(getCurrentPage().getPageId());
		}else {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		}
	}
	
	public boolean myStudentPageGroupsOk (SimplePageItem item) {
	    if (item.isGroupOwned()) {
		// all groups we are a member of
		Collection<Group> groups = getCurrentSite().getGroupsWithMember(getCurrentUserId());

		String allowedString = item.getOwnerGroups();
		// if no group list specified, we're OK if user is in any groups
		if (allowedString == null || allowedString.length() == 0)
		    return groups != null && groups.size() > 0;

		// otherwise have to check 
		HashSet<String> allowedIds = new HashSet<>(Arrays.asList(allowedString.split(",")));
		HashSet<String> inIds = new HashSet<>();
		for (Group g: groups)
		    inIds.add(g.getId());

		// see if overlap between allowed and in
		inIds.retainAll(allowedIds);
		return inIds.size() > 0;
	    }
	    // if not group owned, always OK
	    return true;
	}

	public String createStudentPage() {
		if (!itemOk(itemId))
		    return "permission-failed";
		if (!checkCsrf())
		    return "permission-failed";
		// no check of canedit, since students can do this
		// canread is checked below

		SimplePage curr = getCurrentPage();
		User user = userDirectoryService.getCurrentUser();
		
		// Need to make sure the section exists
		SimplePageItem containerItem = simplePageToolDao.findItem(itemId);
		
		// We want to make sure each student only has one top level page per section.
		SimpleStudentPage page = findStudentPage(containerItem);
		
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
			Group group = null;
			String groupId = null;
			if (containerItem.isGroupOwned()) {
			    String allowedString = containerItem.getOwnerGroups();
			    HashSet<String> allowedGroups = null;
			    if (allowedString != null && allowedString.length() > 0)
				allowedGroups = new HashSet<>(Arrays.asList(allowedString.split(",")));
			    Collection<Group> groups = getCurrentSite().getGroupsWithMember(user.getId());
			    if (groups.isEmpty()) {
				setErrMessage(messageLocator.getMessage("simplepage.owner-groups-nogroup"));
				return "permission-failed";
			    }
			    // ideally just one matches. But if more than one does, let's be deterministic
			    // about which one we use.
			    List<GroupEntry> groupEntries = new ArrayList<>();
			    for (Group g: groups) {
				if (allowedGroups != null && ! allowedGroups.contains(g.getId()))
				    continue;

				if (allowedGroups == null && (g.getProperties().getProperty("lessonbuilder_ref") != null || g.getTitle().startsWith("Access: ")))
				    continue;
				GroupEntry e = new GroupEntry();
				e.name = g.getTitle();
				e.id = g.getId();
				groupEntries.add(e);
			    }
			    if (groupEntries.isEmpty()) {
				setErrMessage(messageLocator.getMessage("simplepage.owner-groups-nogroup"));
				return "permission-failed";
			    }
			    Collections.sort(groupEntries,new Comparator() {
				    public int compare(Object o1, Object o2) {
					GroupEntry e1 = (GroupEntry)o1;
					GroupEntry e2 = (GroupEntry)o2;
					return e1.name.compareTo(e2.name);
				    }
				});
			    GroupEntry groupEntry = groupEntries.get(0);
			    if (!containerItem.isAnonymous())
				title = groupEntry.name;
			    groupId = groupEntry.id;
			}
			SimplePage newPage = simplePageToolDao.makePage(curr.getToolId(), curr.getSiteId(), title, curr.getPageId(), null);
			newPage.setOwner(user.getId());
			newPage.setGroup(groupId);
			//this is a student page so set 'owned' as false
			newPage.setOwned(false);
			saveItem(newPage, false);
			
			// Then attach the lesson_builder_student_pages item.
			// Then attach the lesson_builder_student_pages item.
			page = simplePageToolDao.makeStudentPage(itemId, newPage.getPageId(), title, user.getId(), groupId);
						
			SimplePageItem commentsItem = simplePageToolDao.makeItem(-1, -1, SimplePageItem.COMMENTS, null, messageLocator.getMessage("simplepage.comments-section"));
			saveItem(commentsItem, false);
			
			page.setCommentsSection(commentsItem.getId());
			
			saveItem(page, false);
			
			commentsItem.setAnonymous(containerItem.getForcedCommentsAnonymous());
			commentsItem.setSakaiId(String.valueOf(page.getId()));
			update(commentsItem, false);
			
			newPage.setTopParent(page.getId());
			update(newPage, false);
			
			// Complete user task
			this.completeUserTask(itemId, user.getId());
			
			try {
				updatePageItem(containerItem.getId());
				updatePageObject(newPage.getPageId());
				adjustPath("push", newPage.getPageId(), containerItem.getId(), newPage.getTitle());
			}catch(Exception ex) {
				setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
				return "permission-failed";
			}
			
			// Reset the edit cache so that they can actually edit their page.
			String ref = "/site/" + getCurrentSiteId();
			boolean ok = securityService.unlock(SimplePage.PERMISSION_LESSONBUILDER_UPDATE, ref);
			if(ok) 
			    editPrivs = 0;
			else
			    editPrivs = 1;
			
			return "success";
		}else if(page != null) { 
			setErrMessage(messageLocator.getMessage("simplepage.page-exists"));
			return "permission-failed";
		}else{
			return "permission-failed";
		}
	}
	
	public HashMap<Long, SimplePageLogEntry> cacheStudentPageLogEntries(long itemId) {
		List<SimplePageLogEntry> entries = simplePageToolDao.getStudentPageLogEntries(itemId, userDirectoryService.getCurrentUser().getId());
		
		HashMap<Long, SimplePageLogEntry> map = new HashMap<>();
		for(SimplePageLogEntry entry : entries) {
			logCache.put(entry.getItemId() + "-" + entry.getStudentPageId(), entry);
			map.put(entry.getStudentPageId(), entry);
		}
		
		return map;
	}

	private SecurityAdvisor siteUpdAdvisor = new SecurityAdvisor() {
		public SecurityAdvice isAllowed(String userId, String function, String reference) {
			if (function.equals(SiteService.SECURE_UPDATE_SITE) && reference.equals("/site/" + getCurrentSiteId())) {
				return SecurityAdvice.ALLOWED;
			} else {
				return SecurityAdvice.PASS;
			}
		}
	};

	private SecurityAdvisor pushAdvisorAlways() {
	    SecurityAdvisor alwaysAdvisor = new SecurityAdvisor() {
		    public SecurityAdvice isAllowed(String userId, String function, String reference) {
			return SecurityAdvice.ALLOWED;
		    }
		};
	    securityService.pushAdvisor(alwaysAdvisor);
	    return alwaysAdvisor;
	}

	private SecurityAdvisor pushAdvisor() {
		if(isStudentPage(getCurrentPage())) {
			SecurityAdvisor studentAdvisor = new SecurityAdvisor() {
				public SecurityAdvice isAllowed(String userId, String function, String reference) {
					return SecurityAdvice.ALLOWED;
				}
			};
			securityService.pushAdvisor(studentAdvisor);
			return studentAdvisor;
		}else {
			return null;
		}
	}
	
	private void popAdvisor(SecurityAdvisor sa) {
		// If pushAdvisor() was called on a non-student page, this SecurityAdvisor will be null
		if (sa != null) {
			securityService.popAdvisor(sa);
		}
	}
	
	public void setAddAnswerData(String data) {
		if(StringUtils.isBlank(data)) {
			return;
		}
		
		int separator = data.indexOf(":");
		String indexString = data.substring(0, separator);
		Integer index = Integer.valueOf(indexString);
		data = data.substring(separator+1);
		
		
		// I think this method should only be called from one thread
		// so this should be safe.
		if(questionAnswers == null) {
			questionAnswers = new HashMap<>();
			log.debug("setAddAnswer: it was null");
		}
		
		// We store with the index so that we can maintain the order
		// in which the instructor inputted the answers
		questionAnswers.put(index, data);
	}
	
	/** Used for both adding and updating questions on a page. */
	public String updateQuestion() {
		if (!itemOk(itemId)) {
		    setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		    return "permission-failed";
		}
		if(!canEditPage()) {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "failure";
		}
		if (!checkCsrf())
		    return "permission-failed";
		
		if(questionType == null) {
			setErrMessage(messageLocator.getMessage("simplepage.no-question-type"));
			log.warn("No question type provided for question.");
			return "failure";
		}
		
		SimplePageItem item;
		if(itemId != null && itemId != -1) {
			item = findItem(itemId);
		}else {
			// Adding a question to the page
			item = appendItem("", messageLocator.getMessage("simplepage.questionName"), SimplePageItem.QUESTION);
			item.setAttribute("questionType", "shortanswer");
		}
		
		item.setAttribute("questionText", questionText);
		item.setAttribute("questionCorrectText", questionCorrectText);
		item.setAttribute("questionIncorrectText", questionIncorrectText);
		item.setAttribute("questionType", questionType);
		
		if(questionType.equals("shortanswer")) {
			String shortAnswers[] = questionAnswer.split("\n");
			questionAnswer = "";
			for (String shortAnswer : shortAnswers) {
				String a = shortAnswer.trim();
				if (! a.equals(""))
					questionAnswer = questionAnswer + a + "\n";
			}
			item.setAttribute("questionAnswer", questionAnswer);
		}else if(questionType.equals("multipleChoice")) {
			Long max = simplePageToolDao.maxQuestionAnswer(item);
			simplePageToolDao.clearQuestionAnswers(item);

			for(int i = 0; questionAnswers.get(i) != null; i++) {
				// get data sent from post operation for this answer
				String data = questionAnswers.get(i);
				
				// split the data into the actual fields
				String[] fields = data.split(":", 3);
				Long answerId;
				if (fields[0].equals(""))
				    answerId = -1L;
				else
				    answerId = Long.valueOf(fields[0]);
				if (answerId <= 0L)
				    answerId = ++max;
				Boolean correct = fields[1].equals("true");
				String text = fields[2];
				if (text != null && !text.trim().equals(""))
				    simplePageToolDao.addQuestionAnswer(item, answerId, text, correct);

			}
			
			item.setAttribute("questionShowPoll", String.valueOf(questionShowPoll));


		}

		if (graded) {
			Integer pointsInt = parseMaxPoints(maxPoints);

			if (pointsInt == null) {
				return "failure";
			}

			if (StringUtils.isBlank(gradebookTitle)) {
				gradebookTitle = null;
			}

			List<String> questionGroupList = item.getGroups() != null ? Arrays.asList(item.getGroups().split(",")) : new ArrayList<>();
			List<String> gradebookUids = Arrays.asList(getCurrentSiteId());

			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
				gradebookUids = Arrays.asList(selectedGroups);

				List<String> addedGroups = buildGradebookUidList(questionGroupList, gradebookUids, "CREATE");
				List<String> updatedGroups = buildGradebookUidList(questionGroupList, gradebookUids, "UPDATE");
				List<String> deletedGroups = buildGradebookUidList(questionGroupList, gradebookUids, "DELETE");

				if (gradebookTitle != null) {
					createGradebookItem(addedGroups, item, pointsInt);
					updateGradebookItem(updatedGroups, item, pointsInt);
					deleteGradebookItem(deletedGroups, item, questionGroupList);
				} else {
					deleteGradebookItem(questionGroupList, item, questionGroupList);
					selectedGroups = new String[] {};
				}
			} else {
				if (gradebookTitle != null && (item.getGradebookId() == null || item.getGradebookId().equals(""))) {
					createGradebookItem(gradebookUids, item, pointsInt);
				} else if (gradebookTitle != null) {
					updateGradebookItem(gradebookUids, item, pointsInt);
				} else if(gradebookTitle == null && (item.getGradebookId() != null && !item.getGradebookId().equals(""))) {
					deleteGradebookItem(gradebookUids, item, new ArrayList<>());
				}
			}

			item.setAttribute("questionGraded", String.valueOf(graded));
			item.setGradebookPoints(pointsInt);
		} else {
		    item.setGradebookPoints(null);
		}

		item.setRequired(required);
		item.setPrerequisite(prerequisite);

		setItemGroups(item, selectedGroups);

		saveOrUpdate(item);

		// Create or update a task
		String reference = "/lessonbuilder/item/" + item.getId();
		String title = getPage(item.getPageId()).getTitle() + " - " + messageLocator.getMessage("simplepage.question-task-title");
                // Dashboard task widget: When a question item is added create a task item for the site members.
		createOrUpdateTask(reference, getCurrentSiteId(), title, null);

		// Create a condition for every new question item
		Long savedItemId = Long.valueOf(item.getId());
		List<Condition> itemConditions = conditionService.getConditionsForItem(currentSiteId,
				LessonBuilderConstants.TOOL_ID, savedItemId.toString());

		if (itemConditions.isEmpty() && savedItemId >= 0) {
			Condition itemCondition = Condition.builder()
					.siteId(currentSiteId)
					.toolId(LessonBuilderConstants.TOOL_ID)
					.itemId(savedItemId.toString())
					.type(ConditionType.COMPLETED)
					.build();

			conditionService.saveCondition(itemCondition);
		}

		if(questionType.equals("multipleChoice")) {
			simplePageToolDao.syncQRTotals(item);
		}

		regradeAllQuestionResponses(item.getId());
		
		return "success";
	}

	private List<String> buildGradebookUidList(List<String> groupList, List<String> newGroupList, String type) {
		if (type != null && !StringUtils.isBlank(type)) {
			switch (type) {
				case "CREATE":
					List<String> addedGroups = new ArrayList<>(newGroupList);
					addedGroups.removeAll(groupList);

					return addedGroups;
				case "UPDATE":
					List<String> updatedGroups = new ArrayList<>(newGroupList);
					updatedGroups.retainAll(groupList);

					return updatedGroups;
				case "DELETE":
					List<String> deletedGroups = new ArrayList<>(groupList);
					deletedGroups.removeAll(newGroupList);

					return deletedGroups;
				default:
					return newGroupList;
			}
		} else {
			return newGroupList;
		}
	}

	private void createGradebookItem(List<String> creatableList, SimplePageItem item, Integer pointsInt) {
		if (itemId != null && itemId < 0) {
			saveItem(item);
		}

		String gradebookId = "lesson-builder:question:" + item.getId();
		String title = gradebookTitle;

		if (title == null || title.equals("")) {
			title = questionText;
		}

		try {
			boolean add = gradebookIfc.addExternalAssessment(creatableList, getCurrentSiteId(), gradebookId, null, title, pointsInt, null, LessonBuilderConstants.TOOL_ID);
			if (!add) {
				setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
			} else {
				item.setGradebookId(gradebookId);
				item.setGradebookTitle(title);
			}
		} catch(ConflictingAssignmentNameException cane) {
			setErrMessage(messageLocator.getMessage("simplepage.existing-gradebook"));
		}
	}

	private void updateGradebookItem(List<String> updateableList, SimplePageItem item, Integer pointsInt) {
		if (updateableList != null && updateableList.size() > 0) {
			gradebookIfc.updateExternalAssessment(updateableList, item.getGradebookId(), null, gradebookTitle, pointsInt, null);

			item.setGradebookTitle(gradebookTitle);
		}
	}

	private void deleteGradebookItem(List<String> deleteableList, SimplePageItem item, List<String> newGroupList) {
		if (deleteableList != null && deleteableList.size() > 0) {
			gradebookIfc.removeExternalAssessmentByGradebookList(deleteableList, item.getGradebookId());

			if (newGroupList == null || newGroupList.isEmpty()) {
				item.setGradebookId(null);
				item.setGradebookTitle(null);
			}
		}
	}

	private void regradeAllQuestionResponses(long questionId) {
		List<SimplePageQuestionResponse> responses = simplePageToolDao.findQuestionResponses(questionId);
		for(SimplePageQuestionResponse response : responses) {
			gradeQuestionResponse(response);
			update(response);
		}
	}
	 
	private boolean gradeQuestionResponse(SimplePageQuestionResponse response) {
		SimplePageItem question = findItem(response.getQuestionId());
		if(question == null) {
			log.warn("Invalid question for QuestionResponse {}", response.getId());
			return false;
		}
		
		Double gradebookPoints = null;
		if (question.getGradebookPoints() != null)
		    gradebookPoints = (double)question.getGradebookPoints();
		boolean questionGraded = "true".equals(question.getAttribute("questionGraded"));

		boolean correct;
		if(response.isOverridden()) {
			// The teacher set this score manually, so we'd rather not mess with it.
			correct = response.isCorrect();
			gradebookPoints = response.getPoints();
		}else if(question.getAttribute("questionType") != null && question.getAttribute("questionType").equals("multipleChoice")) {
			SimplePageQuestionAnswer answer = simplePageToolDao.findAnswerChoice(question, response.getMultipleChoiceId());
			if(answer != null && answer.isCorrect()) {
				correct = true;
			}else if(answer != null && !answer.isCorrect()){
				boolean noCorrectAnswers = !simplePageToolDao.hasCorrectAnswer(question);
				if (noCorrectAnswers) {
					correct = !questionGraded; // if not graded, it is a poll and any answer is "correct", otherwise requires manual grading so default to false
					gradebookPoints = null;
				} else { // autograded
					correct = false;
					gradebookPoints = 0.0;
				}
			}else {
				// The answer no longer exists, so we'll just leave everything the way it was last time it was graded.
				correct = response.isCorrect();
				gradebookPoints = response.getPoints();
			}
		}else if(question.getAttribute("questionType") != null && question.getAttribute("questionType").equals("shortanswer")) {
			String correctAnswer = question.getAttribute("questionAnswer");
			StringTokenizer correctAnswerTokenizer = new StringTokenizer(correctAnswer, "\n");
			String theirResponse = response.getShortanswer().trim().toLowerCase();
			
			int totalTokens = correctAnswerTokenizer.countTokens();

			if (totalTokens > 0) {  // correct answers exist
				correct = false;
				for(int i = 0; i < totalTokens; i++) {
					String token = correctAnswerTokenizer.nextToken().replaceAll("\n", "").trim().toLowerCase();

					if(theirResponse.equals(token)) {
						correct = true;
						break;
					}
				}
				if (questionGraded) {
					if (!correct) {
						gradebookPoints = 0.0;
					}
				} else {
					gradebookPoints = null;
				}
			} else if (questionGraded) {  // no correct answers, manually graded
				correct = false;
				gradebookPoints = null;
			} else {  // no correct answers, ungraded
				correct = !theirResponse.isEmpty(); // any non-empty answer is considered "correct" for this question type
				gradebookPoints = null;
			}
		}else {
			log.warn("Invalid question type for question {}", question.getId());
			correct = false;
		}
		
		response.setCorrect(correct);
		if (gradebookPoints != null && questionGraded) {
		    response.setPoints(gradebookPoints);
		
			if(question.getGradebookId() != null && !question.getGradebookId().equals("")) {
				gradebookIfc.updateExternalAssessmentScore(getCurrentSiteId(), getCurrentSiteId(), question.getGradebookId(),
					   response.getUserId(), String.valueOf(gradebookPoints));
			}
		}

		return correct;
	}
	
	public String answerMultipleChoiceQuestion() {
		String userId = getCurrentUserId();
		
		if (!itemOk(questionId) || !canReadPage())
		    return "permission-failed";
		if (!checkCsrf())
		    return "permission-failed";

		SimplePageItem question = findItem(questionId);

		SimplePageQuestionResponse response = simplePageToolDao.findQuestionResponse(questionId, userId); 
		if(response != null) {
			if(!canEditPage()) {
				// Don't let students re-answer questions.
				setErrMessage(messageLocator.getMessage("simplepage.permissions-question"));
				return "failure";
			}
		}else {
			response = simplePageToolDao.makeQuestionResponse(userId, questionId);
		}
		
		long responseId = Long.valueOf(questionResponse);
		response.setMultipleChoiceId(responseId);
		simplePageToolDao.incrementQRCount(questionId, responseId);
		
		SimplePageQuestionAnswer answer = simplePageToolDao.findAnswerChoice(question, response.getMultipleChoiceId());
		response.setOriginalText(answer.getText());
		
		gradeQuestionResponse(response);

		// Complete user task
		this.completeUserTask(questionId, userId);

		saveItem(response);
		
		return "success";
	}
	
	public String answerShortanswerQuestion() {
		String userId = getCurrentUserId();
		
		if (!itemOk(questionId) || !canReadPage())
		    return "permission-failed";
		if (!checkCsrf())
		    return "permission-failed";

		SimplePageQuestionResponse response = simplePageToolDao.findQuestionResponse(questionId, userId); 
		if(response != null) {
			if(!canEditPage()) {
				// Don't let students re-answer questions.
				setErrMessage(messageLocator.getMessage("simplepage.permissions-question"));
				return "failure";
			}
		}else {
			response = simplePageToolDao.makeQuestionResponse(userId, questionId);
		}
		
		SimplePageItem question = findItem(response.getQuestionId());
		
		if (questionResponse != null)
		    questionResponse = questionResponse.trim();
		response.setShortanswer(questionResponse);
		gradeQuestionResponse(response);
		
		// Complete user task
		this.completeUserTask(questionId, userId);
		
		saveItem(response);
		
		return "success";
	}
	
	public String updateStudent() {
		if (!checkCsrf())
		    return "permission-failed";

		if(canEditPage()) {
			SimplePageItem page = findItem(itemId);
			
			page.setAnonymous(anonymous);
			page.setShowComments(comments);
			page.setForcedCommentsAnonymous(forcedAnon);
			page.setRequired(required);
			page.setPrerequisite(prerequisite);
			page.setGroupOwned(groupOwned);
			if (groupOwnedIndividual)
			    page.setAttribute("group-eval-individual", "true");
			else
			    page.removeAttribute("group-eval-individual");
			if (seeOnlyOwn)
			    page.setAttribute("see-only-own", "true");
			else
			    page.removeAttribute("see-only-own");
			
			page.setShowPeerEval(peerEval);
			
			if (studentSelectedGroups == null || studentSelectedGroups.length == 0)
			    page.setOwnerGroups("");
			else {
			    StringBuilder ownerGroups = new StringBuilder();
			    boolean first = true;
				for (String studentSelectedGroup : studentSelectedGroups) {
					// ignore groups that don't exist or aren't in this site
					if (getCurrentSite().getGroup(studentSelectedGroup) == null )
						continue;
					if (first)
						first = false;
					else
						ownerGroups.append(",");
					ownerGroups.append(studentSelectedGroup);
				}
			    page.setOwnerGroups(ownerGroups.toString());
			}

			// RU Rubrics
			// This function is called last. By the time this function is called, rubricRows has been created. 
			//the peerEval should not be in here 
		
			if(rubricRows==null)log.info("rubricRows is null");else	log.info("rubricRows is not null");
			if(peerEval){
				String result = addPeerEval();
				log.info("peerEval{}", result);
			}
		
			if(StringUtils.isBlank(maxPoints)) {
				maxPoints = "1";
			}
			
			if(StringUtils.isBlank(sMaxPoints)) {
				sMaxPoints = "1";
			}

			List<String> studentPageGroupList = page.getGroups() != null
					? Arrays.asList(page.getGroups().split(","))
					: new ArrayList<>();

			List<String> gradebookUids = Arrays.asList(getCurrentSiteId());

			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
				gradebookUids = Arrays.asList(selectedGroups);

				if (gradebookUids == null || gradebookUids.size() <= 0) {
					setErrMessage(messageLocator.getMessage("simplepage.multi_gradebook.no_group"));
					return "failure";
				}
			}

			// Handle the grading of pages
			if (graded) {
				Integer points = parseMaxPoints(maxPoints);

				if (points == null) {
					return "failure";
				}

				if (StringUtils.isBlank(gradebookTitle)) {
					gradebookTitle = getPage(page.getPageId()).getTitle() + " Student Pages (item:" + page.getId()
							+ ")";
				}

				String pageGradebookId = "lesson-builder:page:" + page.getId();

				boolean add = buildGradebookPageItem(studentPageGroupList, gradebookUids, gradebookTitle, pageGradebookId, page, points, false);

				if (!add) {
					setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
				} else {
					page.setGradebookId(pageGradebookId);
					page.setGradebookTitle(gradebookTitle);
					page.setGradebookPoints(points);
					regradeStudentPages(page);
				}
			} else if (page.getGradebookId() != null) {
				gradebookIfc.removeExternalAssessment(getCurrentSiteId(), page.getGradebookId());
				page.setGradebookId(null);
				page.setGradebookPoints(null);
				page.setGradebookTitle(null);
			}

			// Handling the grading of comments on pages
			if (sGraded) {

				Integer pointsC = parseMaxPoints(sMaxPoints);
				if (pointsC == null) {
					return "failure";
				}

				String title = getPage(page.getPageId()).getTitle() + " Student Page Comments (item:" + page.getId() + ")";
				String pageCommentGradebookId = "lesson-builder:page-comment:" + page.getId();

				boolean add = buildGradebookPageItem(studentPageGroupList, gradebookUids, title, pageCommentGradebookId, page, pointsC, true);

				if (!add) {
					setErrMessage(messageLocator.getMessage("simplepage.no-gradebook"));
				} else {
					page.setAltGradebook(pageCommentGradebookId);
					page.setAltGradebookTitle(title);
					page.setAltPoints(pointsC);
					regradeStudentPageComments(page);
				}
			} else if (page.getAltGradebook() != null) {
				gradebookIfc.removeExternalAssessment(getCurrentSiteId(), page.getAltGradebook());
				page.setAltGradebook(null);
				page.setAltPoints(null);
				ungradeStudentPageComments(page);
			}

			setItemGroups(page, selectedGroups);

			// Create or update a task
			String reference = "/lessonbuilder/item/" + itemId;
			String title = getPage(page.getPageId()).getTitle() + " - " + messageLocator.getMessage("simplepage.student-contents-task-title");
			// Dashboard task widget: When a student content item is added create a task item for the site members.
                        createOrUpdateTask(reference, getCurrentSiteId(), title, null);
			
			update(page);
			
			return "success";
		} else {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "failure";
		}
	}

	public boolean buildGradebookPageItem(List<String> groupList, List<String> gradebookUids,
		String pageGradebookTitle, String itemGradebookId, SimplePageItem simplePageItem, Integer pageItemPoints, boolean isStudentComment) {
		boolean add = true;

		if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
			List<String> addedGroups = new ArrayList<>();
			List<String> updatedGroups = buildGradebookUidList(groupList, gradebookUids, "UPDATE");
			List<String> deletedGroups = buildGradebookUidList(groupList, gradebookUids, "DELETE");

			if (isStudentComment) {
				if (simplePageItem.getAltGradebook() == null && simplePageItem.getAltPoints() == null) {
					addedGroups = gradebookUids;
				} else {
					addedGroups = buildGradebookUidList(groupList, gradebookUids, "CREATE");
				}
			} else {
				if (simplePageItem.getGradebookId() == null && simplePageItem.getGradebookPoints() == null) {
					addedGroups = gradebookUids;
				} else {
					addedGroups = buildGradebookUidList(groupList, gradebookUids, "CREATE");
				}
			}

			if (addedGroups != null && addedGroups.size() > 0) {
				add = gradebookIfc.addExternalAssessment(addedGroups, getCurrentSiteId(), itemGradebookId,
				null, pageGradebookTitle, pageItemPoints, null, LessonBuilderConstants.TOOL_ID);
			}

			if (updatedGroups != null && updatedGroups.size() > 0) {
				add = gradebookIfc.updateExternalAssessment(updatedGroups, itemGradebookId,
				null, pageGradebookTitle, pageItemPoints, null);
			}

			if (deletedGroups != null && deletedGroups.size() > 0) {
				add = gradebookIfc.removeExternalAssessmentByGradebookList(deletedGroups, itemGradebookId);
			}
		} else {
			if (!isStudentComment) {
				if (simplePageItem.getGradebookId() == null && simplePageItem.getGradebookPoints() == null && !StringUtils.isBlank(pageGradebookTitle)) {
					add = gradebookIfc.addExternalAssessment(gradebookUids, getCurrentSiteId(),
					itemGradebookId, null, pageGradebookTitle,
						pageItemPoints, null, LessonBuilderConstants.TOOL_ID);
				} else if (simplePageItem.getGradebookId() != null && (!simplePageItem.getGradebookPoints().equals(pageItemPoints) ||
					!simplePageItem.getGradebookTitle().equals(pageGradebookTitle))) {
					add = gradebookIfc.updateExternalAssessment(gradebookUids, itemGradebookId, null, pageGradebookTitle,
						pageItemPoints, null);
				}
			} else {
				if (simplePageItem.getAltGradebook() == null && simplePageItem.getAltPoints() == null &&
					!StringUtils.isBlank(pageGradebookTitle)) {
					add = gradebookIfc.addExternalAssessment(gradebookUids, getCurrentSiteId(),
						itemGradebookId, null, pageGradebookTitle,
						pageItemPoints, null, LessonBuilderConstants.TOOL_ID);
				} else if (simplePageItem.getAltGradebook() != null && (!simplePageItem.getAltPoints().equals(pageItemPoints) ||
					!simplePageItem.getAltGradebook().equals(pageGradebookTitle))) {
					add = gradebookIfc.updateExternalAssessment(gradebookUids, itemGradebookId, null, pageGradebookTitle,
						pageItemPoints, null);
				}
			}
		}

		return add;
	}

	public Integer parseMaxPoints(String maxPoints) {
		try {
			return Integer.valueOf(maxPoints);
		} catch (Exception ex) {
			setErrMessage(messageLocator.getMessage("simplepage.integer-expected"));
			return null;
		}
	}

	public String missingStudentSetZero() {
	    if (!checkCsrf())
		return "permission-failed";
	    if(!canEditPage())
		return "permission-failed";
	    if (!itemOk(itemId))
		return "permission-failed";
        
	    SimplePageItem item = findItem(itemId);
	    String gradebookId = item.getGradebookId();
	    if(gradebookId == null) {
		setErrMessage(messageLocator.getMessage("simplepage.not-graded"));
		return "failure";
	    }

	    // initialize notsubmitted to all userids or groupids
	    Set<String> notSubmitted = new HashSet<>();
	    if (item.isGroupOwned()) {
		notSubmitted = getOwnerGroups(item);
	    } else {
		Set<Member> members = new HashSet<>();
		try {
		    members = authzGroupService.getAuthzGroup(siteService.siteReference(getCurrentSiteId())).getMembers();
		} catch (Exception e) {
		    // since site obviously exists, this should be impossible
		}
		for (Member m: members)
		    notSubmitted.add(m.getUserId());
	    }
					
	    // now go through all student pages and remove them from the list
	    List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(item.getId());
	    for(SimpleStudentPage page : studentPages) {
		if(page.isDeleted()) continue;

		// remove this from notSubmitted
		if (item.isGroupOwned()) {
		    String pageGroup = page.getGroup();
		    if (pageGroup != null)
			notSubmitted.remove(pageGroup);
		} else {
		    notSubmitted.remove(page.getOwner());
		}
	    }
	    
	    // now zero the grades
	    for (String owner: notSubmitted) {
		List<String> owners;
		if (item.isGroupOwned()) {
		    owners = studentPageGroupMembers(item, owner);
		} else {
		    owners = new ArrayList<>();
		    owners.add(owner);
		}
		for (String userid: owners) {
			List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
				try {
					gradebookUids = new ArrayList<String>(getItemGroups(item, null, false));
				} catch (Exception e) {
					log.error("Error getting groups for item {} : {}", item.getId(), e.getMessage());
				}
			}
			for (String gradebookUid : gradebookUids) {
				gradebookIfc.updateExternalAssessmentScore(gradebookUid, getCurrentSiteId(), gradebookId, userid, "0.0");
			}
		}
	    }    

	    return "success";

	}

	public String missingCommentsSetZero() {
	    if (!checkCsrf())
		return "permission-failed";
	    if(!canEditPage())
		return "permission-failed";
	    if (!itemOk(itemId))
		return "permission-failed";
        
	    SimplePageItem commentItem = findItem(itemId);
	    boolean studentComments = (commentItem.getType() == SimplePageItem.STUDENT_CONTENT);

	    String gradebookId;

	    if (studentComments) {
		gradebookId = commentItem.getAltGradebook();
	    } else {
		gradebookId = commentItem.getGradebookId();
	    }

	    if (gradebookId == null) {
		setErrMessage(messageLocator.getMessage("simplepage.not-graded"));
		return "failure";
	    }

	    // initialize notsubmitted to all userids or groupids
	    Set<String> notSubmitted = new HashSet<>();
	    Set<Member> members = new HashSet<>();
	    try {
		members = authzGroupService.getAuthzGroup(siteService.siteReference(getCurrentSiteId())).getMembers();
	    } catch (Exception e) {
		// since site obviously exists, this should be impossible
	    }
	    for (Member m: members)
		notSubmitted.add(m.getUserId());
					
	    // now go through all comments and remove them from the list
	    List<SimplePageComment> comments;
	    
	    if(!studentComments) {
		comments = simplePageToolDao.findComments(itemId);
	    }else {
		List<SimpleStudentPage> studentPages = simplePageToolDao.findStudentPages(itemId);
		
		List<Long> commentsItemIds = new ArrayList<>();
		for(SimpleStudentPage p : studentPages) {
		    // If the page is deleted, don't show the comments
		    if(!p.isDeleted()) {
			commentsItemIds.add(p.getCommentsSection());
		    }
		}
		
		comments = simplePageToolDao.findCommentsOnItems(commentsItemIds);
	    }

	    // have comments. look at owners
	    for(SimplePageComment comment : comments) {
		if(StringUtils.isBlank(comment.getComment())) {
		    continue;
		}
		notSubmitted.remove(comment.getAuthor());
	    }

	    // now zero grade
	    for (String owner: notSubmitted) {
			List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
				try {
					gradebookUids = new ArrayList<String>(getItemGroups(commentItem, null, false));
				} catch (Exception e) {
					log.error("Error getting groups for item {} : {}", commentItem.getId(), e.getMessage());
				}
			}
			for (String gradebookUid : gradebookUids) {
				gradebookIfc.updateExternalAssessmentScore(gradebookUid, getCurrentSiteId(), gradebookId, owner, "0.0");
			}
	    }
		
	    return "success";

	}

	public String missingAnswersSetZero() {
	    if (!checkCsrf())
		return "permission-failed";
	    if(!canEditPage())
		return "permission-failed";
	    if (!itemOk(itemId))
		return "permission-failed";
        
	    SimplePageItem questionItem = findItem(itemId);

	    String gradebookId = questionItem.getGradebookId();

	    if (gradebookId == null) {
		setErrMessage(messageLocator.getMessage("simplepage.not-graded"));
		return "failure";
	    }

	    // initialize notsubmitted to all userids or groupids
	    Set<String> notSubmitted = new HashSet<>();
	    Set<Member> members = new HashSet<>();
	    try {
		members = authzGroupService.getAuthzGroup(siteService.siteReference(getCurrentSiteId())).getMembers();
	    } catch (Exception e) {
		// since site obviously exists, this should be impossible
	    }
	    for (Member m: members)
		notSubmitted.add(m.getUserId());
					
	    // now go through all answers and remove them from the list
	    List<SimplePageQuestionResponse> responses = simplePageToolDao.findQuestionResponses(questionItem.getId());
		
	    // have answers. look at owners
	    for(SimplePageQuestionResponse response : responses) {
		    notSubmitted.remove(response.getUserId());
	    }

	    // now zero grade
	    for (String owner: notSubmitted) {
			List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
			if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
				try {
					gradebookUids = new ArrayList<String>(getItemGroups(questionItem, null, false));
				} catch (Exception e) {
					log.error("Error getting groups for item {} : {}", questionItem.getId(), e.getMessage());
				}
			}
			for (String gradebookUid : gradebookUids) {
				gradebookIfc.updateExternalAssessmentScore(gradebookUid, getCurrentSiteId(), gradebookId, owner, "0.0");
			}
	    }
		
	    return "success";

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
			    if( c.getGroup() == null) {
					List<String> gradebookUids = Arrays.asList(getCurrentSiteId());
					if (gradebookIfc.isGradebookGroupEnabled(getCurrentSiteId())) {
						try {
							gradebookUids = new ArrayList<String>(getItemGroups(pageItem, null, false));
						} catch (Exception e) {
							log.error("Error getting groups for item {} : {}", pageItem.getId(), e.getMessage());
						}
					}
					for (String gradebookUid : gradebookUids) {
						gradebookIfc.updateExternalAssessmentScore(gradebookUid, getCurrentSiteId(), pageItem.getGradebookId(), c.getOwner(), String.valueOf(c.getPoints()));
					}
			    } else {
				String group = c.getGroup();
				if (group != null)
				    group = "/site/" + getCurrentSiteId() + "/group/" + group;
				try {
				    AuthzGroup g = authzGroupService.getAuthzGroup(group);
				    Set<Member> members = g.getMembers();
				    for (Member m: members) {
					gradebookIfc.updateExternalAssessmentScore(group, getCurrentSiteId(), pageItem.getGradebookId(),
					      m.getUserId(), String.valueOf(c.getPoints()));
				    }
				} catch (Exception e) {
				    log.info("unable to get members of group {}", group);
				}
			    }
			}
		}
	}
	
	private String expandZippedResource(String resourceId) {
		String contentCollectionId = resourceId.substring(0, resourceId.lastIndexOf(".")) + "/";

		try {
			contentHostingService.removeCollection(contentCollectionId);
		} catch (Exception e) {
			log.warn("Failed to delete expanded collection");
		}

		try {
			contentHostingService.expandZippedResource(resourceId);
		} catch (Exception e) {
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

			// a directory with just a subdirectory. Use the subdirectory
			if (children.size() == 1 && children.get(0).endsWith("/")) {
			    contentCollectionId = children.get(0);
			    cc = contentHostingService.getCollection(contentCollectionId);
			}

			// With MacOS we might have __MACOSX and a subdirectory. __MACOSX should be ignored,
			// so treat it just like the case above
			if (children.size() == 2 && children.get(0).endsWith("/") && children.get(1).endsWith("/")) {
			    String dataChild = null;
			    if (children.get(0).endsWith("__MACOSX/")) {
				dataChild = children.get(1);
			    } else if (children.get(1).endsWith("__MACOSX/")) {
				dataChild = children.get(0);
			    }
			    
			    if (dataChild != null) {
				contentCollectionId = dataChild;
				cc = contentHostingService.getCollection(contentCollectionId);
			    }
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

			// Test for Camtasia < 9
			ce = cc.getMember(contentCollectionId + "ProductionInfo.xml");
			if (ce != null) {
				index = name + ".html";
			}

			// Test for Camtasia 9
			ce = cc.getMember(contentCollectionId + name + "_player.html");
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
			log.error(e.getMessage(), e);
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
	    String folder;
	    String name;
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

	    if (StringUtils.equalsAny(name, "index.html", "index.htm", folderName + ".html"))
		return folder;

	    if (StringUtils.endsWithAny(resourceId, ".html", ".htm")) {
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
				contentHostingService.commitResource(res, notificationService.NOTI_NONE);
			}
		} catch (Exception pe) {
			log.error(pe.getMessage(), pe);
		}
	}
	
	/**
	 * Returns an ArrayList containing all of the system-wide and site-wide CSS files.
	 * 
	 * One entry may be null, to separate system-wide from site-wide.
	 * 
	 * Caches lookups, to prevent extra database hits.
	 * @return
	 */
	public ArrayList<ContentResource> getAvailableCss() {
		ArrayList<ContentResource> list = new ArrayList<>();
		
		String collectionId = contentHostingService.getSiteCollection(getCurrentSiteId()) + "LB-CSS/";
		
		List<ContentResource> resources = (List<ContentResource>) resourceCache.get(collectionId);
		if(resources == null) {
			resources = contentHostingService.getAllResources(collectionId);
			if(resources == null) resources = new ArrayList<>();
			
			resourceCache.put(collectionId, resources);
		}
		
		// For a while, up to Sakai 11.3, uploading a custom css file directly from
		// the lessonbuilder page could cause Sakai to generate file names like foo.css-1
		// In such cases, they should appear in the dropdown for custom css
		Pattern cssPattern = Pattern.compile("\\.css(-[0-9][0-9]*){0,1}$", Pattern.CASE_INSENSITIVE);
		
		for(ContentResource r : resources) {
			if(cssPattern.matcher(r.getUrl()).find()) {
				list.add(r);
			}
		}
		
		collectionId = "/public/LB-CSS/";
		
		resources = (List<ContentResource>) resourceCache.get(collectionId);
		if(resources == null) {
			resources = contentHostingService.getAllResources(collectionId);
			if(resources == null) resources = new ArrayList<>();
			
			resourceCache.put(collectionId, resources);
		}
		
		// Insert separator
		if(list.size() > 0 && resources.size() > 0) {
			list.add(null);
		}
		
		for(ContentResource r : resources) {
			if(cssPattern.matcher(r.getUrl()).find()) {
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
	 * @return
	 */
	public ContentResource getCssForCurrentPage() {
		ContentResource resource;
		
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
					resources = new ArrayList<>();
					resources.add(resource);
					resourceCache.put(collectionId, resources);
				}
				
				if(resources.size() > 0) {
					return resources.get(0);
				}else {
					throw new Exception();
				}
			}catch(Exception ex) {
				resourceCache.put(collectionId, new ArrayList<>());
			}
		}
		
		collectionId = contentHostingService.getSiteCollection(getCurrentSiteId())
				+ "LB-CSS/" + ServerConfigurationService.getString("lessonbuilder.default.css", "default.css");
		
		try {
			ArrayList<ContentResource> resources = (ArrayList<ContentResource>) resourceCache.get(collectionId);
			if(resources == null) {
				resource = contentHostingService.getResource(collectionId);
				resources = new ArrayList<>();
				resources.add(resource);
				resourceCache.put(collectionId, resources);
			}
			
			if(resources.size() > 0) {
				return resources.get(0);
			}
		}catch(Exception ignore) {
			resourceCache.put(collectionId, new ArrayList<>());
		}
		
		collectionId = "/public/LB-CSS/" + ServerConfigurationService.getString("lessonbuilder.default.css", "default.css");
		
		try {
			ArrayList<ContentResource> resources = (ArrayList<ContentResource>) resourceCache.get(collectionId);
			if(resources == null) {
				resource = contentHostingService.getResource(collectionId);
				resources = new ArrayList<>();
				resources.add(resource);
				resourceCache.put(collectionId, resources);
			}
			
			if(resources.size() > 0) {
				return resources.get(0);
			}
		}catch(Exception ignore) {
			resourceCache.put(collectionId, new ArrayList<>());
		}
				
		return null;
	}
	/** Used for both adding and updating peer evaluation on a page. */
	public String addPeerEval() {
		if (!itemOk(itemId)) {
		    setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		    return "permission-failed";
		}
		if(!canEditPage()) {
			setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
			return "failure";
		}
		
		SimplePageItem item;
		if(itemId != null && itemId != -1) {
			item = findItem(itemId);
			
		}else {
			
			item = appendItem("", messageLocator.getMessage("simplepage.peerEval"), SimplePageItem.PEEREVAL);
		}
	
		Long max = simplePageToolDao.maxPeerEvalRow(item);
		simplePageToolDao.clearPeerEvalRows(item);
		if (rubricRows == null){
			return "failure";
		}
		
		item.setAttribute("rubricTitle", rubricTitle);
		
		Calendar peerevalcal = Calendar.getInstance();
		
		
		peerevalcal.setTime(peerEvalOpenDate);
		long peerEvalDate = peerevalcal.getTimeInMillis();
		item.setAttribute("rubricOpenDate",String.valueOf(peerEvalDate));
		Date openDate = peerevalcal.getTime();
		peerevalcal.setTime(peerEvalDueDate);
		peerEvalDate = peerevalcal.getTimeInMillis();
		item.setAttribute("rubricDueDate",String.valueOf(peerEvalDate));
		Date dueDate = peerevalcal.getTime();
		if (openDate.after(dueDate) ) {
			//error message for dueDate before openDate
			setErrMessage(messageLocator.getMessage("simplepage.dueDatebeforopenDate"));
			return "failure";
			
		}
		item.setAttribute("rubricAllowSelfGrade",String.valueOf(peerEvalAllowSelfGrade));
		
		for(int i = 0; rubricRows.get(i) != null; i++) {
			// get data sent from post operation for this answer
			String data = rubricRows.get(i);
			
			// split the data into the actual fields
			String[] fields = data.split(":", 2);
			Long rowId;
			if (StringUtils.isBlank(fields[0]))
			    rowId = -1L;
			else
			   rowId = Long.valueOf(fields[0]);
			if (rowId <= 0L)
			    rowId = max - rowId;
			String text = fields[1];
			simplePageToolDao.addPeerEvalRow(item, rowId, text);
		}
		saveOrUpdate(item);
		return "success";
	}

	 /**
	 * To add latest announcements in a div in Lessons page
	 * @return status
	 */
	public String addAnnouncements(){
		if (!itemOk(itemId))
			return "permission-failed";
		if (!checkCsrf())
			return "permission-failed";
		String status = "success";
		if (canEditPage()) {
			SimplePageItem item;
			if (itemId != null && itemId != -1) {
				//existing item, need to update
				item = findItem(itemId);
			}else{
				//new item ,add it
				item = appendItem("", "", SimplePageItem.ANNOUNCEMENTS);
			}
			//setting height in the item attribute
			item.setAttribute("height", announcementsHeight);
			item.setAttribute("numberOfAnnouncements", announcementsDropdown);
			item.setPrerequisite(this.prerequisite);
			setItemGroups(item, selectedGroups);
			saveOrUpdate(item);
		}else{
			status = "cancel";
		}
		return status;
	}
	public String savePeerEvalResult() {
		
		String userId = getCurrentUserId();
		
		// can evaluate if this is a student page and we're in the site
		// and the page has a rubric
		if (!isStudentPage(getCurrentPage()) || !canReadPage()) {
		    setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		    return "permission-failed";
		}

		// does page have rubric?
    
		SimpleStudentPage studentPage = simplePageToolDao.findStudentPage(currentPage.getTopParent());
		SimplePageItem item = simplePageToolDao.findItem(studentPage.getItemId());
		if(item != null && item.getShowPeerEval() != null && item.getShowPeerEval())
		    ;
		else {
		    setErrMessage(messageLocator.getMessage("simplepage.permissions-general"));
		    return "permission-failed";
		}

		if (!checkCsrf())
		    return "permission-failed";
		
		if (rubricPeerGrades == null)
		    return "success"; // nothing to do

		// construct row text -> row id
		// old entries are by text, so need to be able to map them to id

		Map<String, Long> rowMap = new HashMap<>();

		SimplePageItem i = findItem(itemId);
                List<Map> categories = (List<Map>) i.getJsonAttribute("rows");
		if (categories == null)   // not valid to do update on item without rubic
		    return "fail";
		for (Map cat: categories) {
		    String rowText = String.valueOf(cat.get("rowText"));
		    String rowId = String.valueOf(cat.get("id"));
		    rowMap.put(rowText, new Long(rowId));
		}

		// set up data for permission checks
		// user is in site because we check canRead
		String owner = getCurrentPage().getOwner();
		String currentUser = getCurrentUserId();
		List<String>groupMembers = studentPageGroupMembers(item, null);
		boolean evalIndividual = (item.isGroupOwned() && "true".equals(item.getAttribute("group-eval-individual")));
		boolean gradingSelf = Boolean.parseBoolean(item.getAttribute("rubricAllowSelfGrade"));
		// check is down below at canSubmit.

		// data from user: build map target --> <category --> grades>

		Map<String, Map<Long, Integer>> dataMap = new HashMap<>();
		for (String gradeLine: rubricPeerGrades) {
		    String[] items = gradeLine.split(":", 3);
		    Map<Long, Integer> catMap = dataMap.get(items[2]);
		    if (catMap == null) {
			catMap = new HashMap<>();
			dataMap.put(items[2], catMap);
		    }
		    catMap.put(new Long(items[0]), new Integer(items[1]));
		}

		// have user data, now update database

		// evalTargets are targets that it's legal to evaluate for this page
		// owner, or if evaluating individuals on a gorup page, all members of the group

		Set<String>evalTargets = new HashSet<>();

		if (evalIndividual) {
		    String group = getCurrentPage().getGroup();
		    if (group != null)
			group = "/site/" + getCurrentSiteId() + "/group/" + group;
		    try {
			AuthzGroup g = authzGroupService.getAuthzGroup(group);
			Set<Member> members = g.getMembers();
			for (Member m: members) {
			    evalTargets.add(m.getUserId());
			}
		    } catch (Exception e) {
			log.info("unable to get members of group {}", group);
		    }
		} else {
		    evalTargets.add(getCurrentPage().getOwner());
		}

		// now do the actual data update. loop over users

		// search will always include target. where a group is being
		// evaluted also need groupId. In that case old format entries are
		// by page owner, new are by group, so we need both
		String groupId = null;
		if (item.isGroupOwned() && !evalIndividual)
		    groupId = getCurrentPage().getGroup();

		for (String target: dataMap.keySet()) {
		    // is this someone we can evaluate? In normal case only page owner

		    // keep this in sync with the same expression in ShowPageProducer
		    boolean canSubmit = (!item.isGroupOwned() && (!owner.equals(currentUser) || gradingSelf) ||
					 i.isGroupOwned() && !evalIndividual && (!groupMembers.contains(currentUser) || gradingSelf) ||
					 evalIndividual && groupMembers.contains(currentUser) && (gradingSelf || !target.equals(currentUser)));
		    if (!canSubmit)
			continue;
		    if (!evalTargets.contains(target))
			continue;
		    // get old evaluations, as we need to mark them deleted
		    List<SimplePagePeerEvalResult> oldEvaluations = simplePageToolDao.findPeerEvalResult(getCurrentPage().getPageId(), userId, target, groupId);
		    Map <Long, Integer> rows = dataMap.get(target);  // rows of new data
		    for (SimplePagePeerEvalResult result: oldEvaluations) {
			Long rowId = result.getRowId();
			if (rowId == 0L)
			    rowId = rowMap.get(result.getRowText());
			if (rowId == null || rows.get(rowId) != null) { // old value is bogus or we have a new value for this row
			    result.setSelected(false);  // invalidate old result
			    update(result,false);
			}			    
		    }
		    // now add new evaluations
		    for (Long rowId: rows.keySet()) {
			int grade = rows.get(rowId); // grade for this row from data
			// create a new format entry. use group id when evaluating group, and rowId rather than text
			SimplePagePeerEvalResult ret = simplePageToolDao.makePeerEvalResult(getCurrentPage().getPageId(), (groupId == null ? target: null), groupId, userId,  null, rowId, grade);
			saveItem(ret,false);		
		    }
		    
		}
		return "success";

	}
	
	public List<String>studentPageGroupMembers(SimplePageItem item, String group) {
	    List<String>groupMembers = new ArrayList<>();
	    if (item.isGroupOwned()) {
		if (group == null) {
		    SimplePage page = getCurrentPage();
		    group = page.getGroup();
		}
		if (group != null)
		    group = "/site/" + getCurrentSiteId() + "/group/" + group;
		try {
		    AuthzGroup g = authzGroupService.getAuthzGroup(group);
		    Set<Member> members = g.getMembers();
		    for (Member m: members) {
			groupMembers.add(m.getUserId());
		    }
		} catch (Exception e) {
		    log.info("unable to get members of group {}", group);
		}
	    }
		if (groupMembers == null) {
			return new ArrayList<String>();
		}
		        
	    return groupMembers;
	}	    


	// May add or edit comments
        // can't find a call to this. Security cheking is probaby wrong.
		public String addComment1() {
			boolean html = false;
			
			// Patch in the fancy editor's comment, if it's been used
			if(StringUtils.isNotBlank(formattedComment)) {
				comment = formattedComment;
				html = true;
			}
			
		comment = formattedText.processFormattedText(comment, null, null);
			
			if(StringUtils.isBlank(comment)) {
				setErrMessage(messageLocator.getMessage("simplepage.empty-comment-error"));
				return "failure";
			}
			
			if(StringUtils.isBlank(editId)) {
				String userId = userDirectoryService.getCurrentUser().getId();
				
				Double grade = null;
				if(findItem(itemId).getGradebookId() != null) {
					List<SimplePageComment> comments = simplePageToolDao.findCommentsOnItemByAuthor(itemId, userId);
					if(comments != null && comments.size() > 0) {
						grade = comments.get(0).getPoints();
					}
				}
				
				SimplePageComment commentObject = simplePageToolDao.makeComment(itemId, getCurrentPage().getPageId(), userId, comment, idManager.createUuid(), html);
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
			
			if(isStudentPage(getCurrentPage())) {
				SimpleStudentPage student = simplePageToolDao.findStudentPage(getCurrentPage().getTopParent());
				student.setLastCommentChange(new Date());
				update(student, false);
			}
			
			return "added-comment";
		}

    /**
     * Truncate a Java string so that its UTF-8 representation will not 
     * exceed the specified number of bytes.
     *
     * For discussion of why you might want to do this, see
     * http://lpar.ath0.com/2011/06/07/unicode-alchemy-with-db2/
     * @param input
     * @param length
     * @return
     */
	public static String utf8truncate(String input, int length) {
	StringBuilder result = new StringBuilder(length);
	int resultlen = 0;
	for (int i = 0; i < input.length(); i++) {
	    char c = input.charAt(i);
	    int charlen = 0;
	    if (c <= 0x7f) {
		charlen = 1;
	    } else if (c <= 0x7ff) {
		charlen = 2;
	    } else if (c <= 0xd7ff) {
		charlen = 3;
	    } else if (c <= 0xdbff) {
		charlen = 4;
	    } else if (c <= 0xdfff) {
		charlen = 0;
	    } else if (c <= 0xffff) {
		charlen = 3;
	    }
	    if (resultlen + charlen > length) {
		break;
	    }
	    result.append(c);
	    resultlen += charlen;
	}
	return result.toString();
    }

	/**
	 * To add latest conversations in a div on Lessons Page
	 * @return
	 */
	public String addForumSummary(){
		if (!itemOk(itemId) || !checkCsrf()) {
			return "permission-failed";
		}
		String status = "success";
		if (canEditPage()) {
			SimplePageItem item;
			// itemid -1 means we're adding a new item to the page,
			// specified itemid means we're updating an existing one
			if (itemId != null && itemId != -1) {
				item = findItem(itemId);
			} else {
				item = appendItem("", "", SimplePageItem.FORUM_SUMMARY);
			}
			//setting forum height variable value in the attribute
			item.setAttribute("height", forumSummaryHeight);
			item.setAttribute("numberOfConversations", forumSummaryDropDown);
			item.setPrerequisite(this.prerequisite);
			setItemGroups(item, selectedGroups);
			saveOrUpdate(item);
		} else {
			status = "cancel";
		}
		return status;
	}

	public boolean isStudentPage(SimplePage page){
		return page != null && page.getOwner()!=null && !page.isOwned();
	}

	/**
	 * Method to add Resources folder into Lessons tool
	 * @return
	 */
	public String folderPickerSubmit(){
		if (!itemOk(itemId))
			return "permission-failed";
		if (!checkCsrf())
			return "permission-failed";
		//Check if user has submitted page without selecting folder?
		String defaultPath = contentHostingService.getSiteCollection(getCurrentSiteId());
		folderPath = folderPath.substring(folderPath.indexOf("/", 1));
		if(folderPath == null ||  folderPath.equals(defaultPath)){
			return "failure";
		}
		String dataDirectory = defaultPath + folderPath;
		String status = "success";
		if (canEditPage()) {
			SimplePageItem item;
			// itemid -1 means we're adding a new item to the page,
			// specified itemid means we're updating an existing one
			if (itemId != null && itemId != -1) {
				item = findItem(itemId);
			} else {
				item = appendItem("", "", SimplePageItem.RESOURCE_FOLDER);
			}
			item.setAttribute("dataDirectory", dataDirectory);
			item.setPrerequisite(this.prerequisite);
			setItemGroups(item, selectedGroups);
			update(item);

		} else {
			status = "cancel";
		}
		return status;
	}
	/**
	 * Method to add calendar component in the Lessons Page
	 * @param ab
	 * @return
	 */
	public String addCalendar(){
		if (!itemOk(itemId) || !checkCsrf())
			return "permission-failed";
		String result = "success";
		if (canEditPage()) {
			//set addBefore value which will be used by append item to place calendar item at correct place
			SimplePageItem item;
			if (itemId != null && itemId != -1) {
				//existing item, need to update
				item = findItem(itemId);
			}else{
				//new item,add it
				item = appendItem("", "", SimplePageItem.CALENDAR);
			}
			item.setPrerequisite(this.prerequisite);
			setItemGroups(item, selectedGroups);
			update(item);
			// Must clear the cache so that the calendar appears on the page as soon as it's added
			itemsCache.remove(getCurrentPage().getPageId());
		}
		else{
			result = "cancel";
		}
		return result;
	}
	/**
	 * To add twitter timeline with given parameters in a Lessons page
	 */
	public String addTwitterTimeline(){
		if (!itemOk(itemId) || !checkCsrf()){
			return "permission-failed";
		}
		//if username is not provided return
		if(StringUtils.isBlank(twitterUsername)){
			return "failure";
		}
		//Check if height is supplied if not then set to default
		if(StringUtils.isBlank(twitterWidgetHeight)){
			twitterWidgetHeight = TWITTER_WIDGET_DEFAULT_HEIGHT;
		}
		//if user has added @ symbol with the username, remove it
		if( twitterUsername.contains("@")){
			twitterUsername = StringUtils.remove(twitterUsername, "@");
		}
		String status = "success";
		if (canEditPage()) {
			SimplePageItem item;
			// itemid -1 means we're adding a new item to the page,
			// specified itemid means we're updating an existing one
			if (itemId != null && itemId != -1) {
				item = findItem(itemId);
			} else {
				item = appendItem("", "", SimplePageItem.TWITTER);
			}
			//setting height , username and number of tweets as attributes for the twitter item.
			item.setAttribute("height", twitterWidgetHeight);
			item.setAttribute("username", twitterUsername);
			item.setAttribute("numberOfTweets", twitterDropDown );
			item.setPrerequisite(this.prerequisite);
			setItemGroups(item, selectedGroups);
			update(item);

		} else {
			status = "cancel";
		}
		return status;
	}

	// Does the site contain GradebookNG or Gradebook Classic
	public boolean isGradebookExists() {
		return (this.getCurrentTool(this.GRADEBOOK_TOOL_ID) != null || this.getCurrentTool(this.GRADEBOOK_CLASSIC_TOOL_ID) != null);
	}
	
	private void updateTask(String reference, String title, Date dueDate) {
		Optional<Task> taskOpt = taskService.getTask(reference);
		if (taskOpt.isPresent()) {
			Task task = taskOpt.get();
			task.setDescription(title);
			task.setDue(dueDate == null ? null : dueDate.toInstant());
			taskService.saveTask(task);
		}
	}
	
	private void createOrUpdateTask(String reference, String siteId, String title, Date dueDate) {
		Optional<Task> taskOpt = taskService.getTask(reference);
		if (!taskOpt.isPresent()) {
			try {
				Task task = new Task();
				task.setSiteId(siteId);
				task.setReference(reference);
				task.setSystem(true);
				task.setDescription(title);
				task.setDue(dueDate == null ? null : dueDate.toInstant());
				Site site = siteService.getSite(siteId);
				Set<String> users = site.getUsersIsAllowed("section.role.student");
				taskService.createTask(task, users, Priorities.HIGH);
			} catch (Exception e) {
				log.error(messageLocator.getMessage("simplepage.errorCreatingTask"), e);
			}
		} else {
			Task task = taskOpt.get();
			task.setDescription(title);
			task.setDue(dueDate == null ? null : dueDate.toInstant());
			taskService.saveTask(task);
		}
	}
	
	private void completeUserTask(Long itemId, String userId) {
		taskService.completeUserTaskByReference("/lessonbuilder/item/" + itemId, Arrays.asList(userId) );
	}

	private void setSubpageTitleBulkEditData() {
		final JSONParser parser = new JSONParser();
		try {
			JSONArray subpageArray = (JSONArray) parser.parse(subpageBulkEditJson);
			for (Object obj : subpageArray) {
				JSONObject subpageTitleInfo = (JSONObject) parser.parse((String) obj);
				String itemId = (String) subpageTitleInfo.get("itemId");
				String newTitle = (String) subpageTitleInfo.get("title");
				String description = (String) subpageTitleInfo.get("description");
				if (StringUtils.isNotBlank(itemId) && StringUtils.isNotBlank(newTitle)) {
					SubpageBulkEditHelper subpageHelper = new SubpageBulkEditHelper(newTitle, description);
					subpageBulkEditTitleMap.put(Long.valueOf(itemId), subpageHelper);
				} else {
					throw new RuntimeException("Page id is null or new title is blank");
				}
			}
		} catch (ClassCastException e) {
			log.error("Parser returned a non-JSONObject. ", e);
			subpageBulkEditTitleMap = new HashMap<>();
		} catch (ParseException e) {
			log.error("Parser unable to parse json. ", e);
			subpageBulkEditTitleMap = new HashMap<>();
		} catch (RuntimeException e) {
			log.error("Bad data. Null page id or blank page title. ", e);
			subpageBulkEditTitleMap = new HashMap<>();
		}
	}

	/**
	 * Method to save subpage bulk edit changes
	 */
	public String subpageBulkEditSubmit() {
		if (canEditPage() || !checkCsrf()) {
			setSubpageTitleBulkEditData();
			if (subpageBulkEditTitleMap.isEmpty()) {
				setErrMessage(messageLocator.getMessage("simplepage.bulk-edit-pages.blank"));
				return "failure";
			}
			subpageBulkEditTitleMap.forEach((itemId, subpageHelper) -> {
				SimplePageItem item = simplePageToolDao.findItem(itemId);
				if (item != null && (!StringUtils.equals(item.getName(), subpageHelper.getTitle()) || !StringUtils.equals(item.getDescription(), subpageHelper.getDescription()))) {
					SimplePage subpage = getPage(Long.valueOf(item.getSakaiId()));
					subpage.setTitle(subpageHelper.getTitle());
					update(subpage);
					item.setName(subpageHelper.getTitle());
					item.setDescription(subpageHelper.getDescription());
					update(item);
				}
			});
			return "success";
		} else {
			return "permission-failed";
		}
	}

	public List<Condition> getItemConditions() {
		return itemOk(itemId)
				? getItemConditions(findItem(itemId))
				: Collections.emptyList();
	}

	public List<Condition> getItemConditions(SimplePageItem item) {
		return conditionService.getConditionsForItem(currentSiteId,
				LessonBuilderConstants.TOOL_ID, Long.valueOf(item.getId()).toString());
	}

}
