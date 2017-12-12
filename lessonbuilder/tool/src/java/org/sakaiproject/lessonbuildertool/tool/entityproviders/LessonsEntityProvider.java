/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/lessonbuilder/trunk/tool/src/java/org/sakaiproject/lessonbuildertool/tool/entityproviders/LessonbuilderEntityProvider.java $
 * $Id: LessonbuilderEntityProvider.java $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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
 
package org.sakaiproject.lessonbuildertool.tool.entityproviders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.sakaiproject.authz.api.SecurityService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.content.api.ContentHostingService;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.EntityView;
import org.sakaiproject.entitybroker.entityprovider.annotations.EntityCustomAction;
import org.sakaiproject.entitybroker.entityprovider.EntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.ActionsExecutable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.AutoRegisterEntityProvider;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Describeable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Outputable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Resolvable;
import org.sakaiproject.entitybroker.entityprovider.capabilities.Sampleable;
import org.sakaiproject.entitybroker.entityprovider.extension.Formats;
import org.sakaiproject.entitybroker.exception.EntityNotFoundException;
import org.sakaiproject.entitybroker.util.AbstractEntityProvider;
import org.sakaiproject.entitybroker.DeveloperHelperService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.SimplePageQuestionAnswer;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.service.LessonsAccess;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.cover.ActiveToolManager;

/**
 * @author fsaez
 *
 */
@Slf4j
public class LessonsEntityProvider extends AbstractEntityProvider implements EntityProvider, AutoRegisterEntityProvider, Outputable, Describeable, Sampleable, Resolvable, ActionsExecutable {
	
	public final static String ENTITY_PREFIX = "lessons";
	public final static String TOOL_COMMON_ID = "sakai.lessonbuildertool";
	public static final String REFERENCE_ROOT = "/lessons";
	
    // from ToolComponent, which is part of kernel-impl, so we can't get to it.

	/** Key in the ThreadLocalManager for binding our current placement. */
	protected final static String CURRENT_PLACEMENT = "sakai:ToolComponent:current.placement";

	/** Key in the ThreadLocalManager for binding our current tool. */
	protected final static String CURRENT_TOOL = "sakai:ToolComponent:current.tool";

	@Override
	public String getEntityPrefix() {
	    	return ENTITY_PREFIX;
	}

	@Override
	public Object getSampleEntity() {
		return null;
	}
	
    /* 
     * security model. Need the following
     * 
     * setToolSession - creates a tool session and sets current site in ThreadLocal.
     *   needed because some tools for which we do group checks use this info 
     *   in their services. (in fact Lessons code current does, though that could be fixed.)
     * checkReadPermission in site
     * isItemAccessible
     *   These are combined by checkItemPermissions, but in loops it is probaby better to
     *   do checkReadPermission once and checkItemPermission for the items
     *   In a loop it's also best to do an explicitly creation of a SimplePageBean and
     *   pass it to each call to checkItemPermission
     * checkUpdatePermission in site
     *   if user has update you don't need to check for read or item permission
     */

	@Override
	public Object getEntity(EntityReference ref) {
	
		if (!ServerConfigurationService.getBoolean("lessonbuilder.keitai", true))
		    return null;

		//get user uuid
		String id = ref.getId();
		if(StringUtils.isBlank(id)) {
			throw new EntityNotFoundException("Invalid lesson.", ref.getId());
		}

		DecoratedItem ret = null;
		SimplePageItem item = simplePageToolDao.findItem(Long.valueOf(id));
		
		if(item == null)
			throw new EntityNotFoundException("Id not found", id);
		
		SimplePage page = null;
		if (item.getType() == SimplePageItem.PAGE)			
			page = simplePageToolDao.getPage(Long.parseLong(item.getSakaiId()));
		else
			page = simplePageToolDao.getPage(item.getPageId());
		
		if(page != null) {
			String siteId = page.getSiteId();

			Site site = null;
			try {
				site = siteService.getSiteVisit(siteId);
			} catch (IdUnusedException e) {
				throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
			} catch (PermissionException e) {
				throw new EntityNotFoundException("No access to site: " + siteId, siteId);
			}
			
			//check if given site contains the lessonbuilder tool
			//checkTool(site);
			
			setToolSession(site);

			//check if current user has permission to access to the lessonbuilder tool
			// checks tool read and item perm
			if (!checkUpdatePermission(siteId))
			    checkItemPermission(siteId, item.getId());
			
			if (item.getType() == SimplePageItem.PAGE) {
			    String baseURL = developerHelperService.getEntityURL(REFERENCE_ROOT, EntityView.VIEW_LIST, null);   //   /direct/lessons
			    baseURL = baseURL + "/lesson/";
			    ret = new DecoratedPageItem(item.getId(), item.getName(), item.getType(), siteId, site.getTitle(), baseURL + item.getId());
			} else
			    ret = new DecoratedItem(item.getId(), item.getName(), item.getType(), siteId, site.getTitle());
		}
		else
			throw new EntityNotFoundException("Invalid id", id);
			
		return ret;		
	}

	
	/**
	 * site
	 * example: /direct/lessons/site/SITEID.xml
	 */
	@EntityCustomAction(action="site",viewKey=EntityView.VIEW_LIST)
	public List<?> getLessonsInSite(EntityView view, Map<String, Object> params) {
	
		if (!ServerConfigurationService.getBoolean("lessonbuilder.keitai", true))
		    return null;

		String siteId = view.getPathSegment(2);
		
		if (siteId == null || "".equals(siteId)) {
			// format of the view should be in a standard message reference								
			throw new IllegalArgumentException("Must include siteId in the path ("+view+"): e.g. /direct/lessons/site/{siteId}");
		}
		
		Site site = null;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}
		
		//check if given site contains the lessonbuilder tool
		//checkTool(site);

		setToolSession(site);
		
		//check if current user has permission to access to the lessonbuilder tool
		checkReadPermission(siteId);
		
		List<DecoratedSiteItem> ret = new ArrayList<DecoratedSiteItem>();
		
		List<SimplePageItem> list = simplePageToolDao.findItemsInSite(siteId);
					
		// best to do this once so each call to isItemAccessible doesn't have to
		SimplePageBean simplePageBean = null;
		String currentUserId = sessionManager.getCurrentSessionUserId();
		String baseURL = developerHelperService.getEntityURL(REFERENCE_ROOT, EntityView.VIEW_LIST, null);   //   /direct/lessons
		baseURL = baseURL + "/lesson/";

		boolean hasUpdatePermission = checkUpdatePermission(siteId);
		
		for(SimplePageItem item : list)
		{
		    simplePageBean = makeSimplePageBean(simplePageBean, siteId, item);
		    if (hasUpdatePermission || lessonsAccess.isItemAccessible(item.getId(), siteId, currentUserId, simplePageBean)) {
			SimplePage currentSimplePage = simplePageBean.getCurrentPage();
				if (currentSimplePage != null){
					ret.add(new DecoratedSiteItem(item.getId(), item.getName(), baseURL + item.getId(), currentSimplePage.isHidden(), currentSimplePage.getReleaseDate(), currentSimplePage.getGradebookPoints()));
				}
		    }
		}
		
		return ret;
    }
	
	/**
	 * user
	 * example: /direct/lessons/user.xml
	 */
	@EntityCustomAction(action="user",viewKey=EntityView.VIEW_LIST)
	public List<?> getLessonsForUser(EntityView view, Map<String, Object> params) {
		
		if (!ServerConfigurationService.getBoolean("lessonbuilder.keitai", true))
		    return null;

		List<DecoratedUserItem> ret = new ArrayList<DecoratedUserItem>();
		
		String currentUserId = sessionManager.getCurrentSessionUserId();
		String baseURL = developerHelperService.getEntityURL(REFERENCE_ROOT, EntityView.VIEW_LIST, null);   //   /direct/lessons
		baseURL = baseURL + "/lesson/";

		//first of all, we get a list with all sites that the user can access
		List<Site> siteList = getUserSites();
		for(Site s : siteList) {
			SimplePageBean simplePageBean = null;
			try {
				//check if given site contains the lessonbuilder tool
				//checkTool(s);
				
				setToolSession(s);

				//check if current user has permission to access to the lessonbuilder tool
				checkReadPermission(s.getId());
				
				List<SimplePageItem> list = simplePageToolDao.findItemsInSite(s.getId());
							
				boolean hasUpdatePermission = checkUpdatePermission(s.getId());

				for(SimplePageItem item : list)
				{
				    simplePageBean = makeSimplePageBean(simplePageBean, s.getId(), item);
				    if (hasUpdatePermission || lessonsAccess.isItemAccessible(item.getId(), s.getId(), currentUserId, simplePageBean))
					ret.add(new DecoratedUserItem(item.getId(), item.getName(), s.getId(), s.getTitle(), baseURL + item.getId()));
				}

			}catch(EntityNotFoundException e) { //if there is no lessonbuilder tool in that site, just skip it		
			}catch(SecurityException e) { //if current user can not access to the lessonbuilder tool on that site, just skip it				
			}
		}
		
		return ret;
	}
	
	/**
	 * lesson
	 * example: /direct/lessons/lesson/LESSONID.xml
	 */
	@EntityCustomAction(action="lesson",viewKey=EntityView.VIEW_LIST)
	public Object getLesson(EntityView view, Map<String, Object> params) {
	
		if (!ServerConfigurationService.getBoolean("lessonbuilder.keitai", true))
		    return null;

		String id = view.getPathSegment(2);
		
		if (id == null || "".equals(id)) {
			// format of the view should be in a standard message reference								
			throw new IllegalArgumentException("Must include lessonId in the path ("+view+"): e.g. /direct/lessons/lesson/{lessonId}");
		}	

		boolean simpleType = false;
		boolean fullTree = true;
		if(params != null)
		{
			try
			{
				if(params.get("type") != null)
					simpleType = "simple".equals(params.get("type"));
			}
			catch(Exception e){}
			try
			{
				if(params.get("fullTree") != null)
					fullTree = Boolean.valueOf((String)params.get("fullTree"));
			}
			catch(Exception e){}	
		}
		
		List ret = new ArrayList();
		
		//get item by id
		SimplePageItem item = simplePageToolDao.findItem(Long.parseLong(id));
		
		if(item == null)
			throw new EntityNotFoundException("Id not found", id);
			
		SimplePage page = null;
		if (item.getType() == SimplePageItem.PAGE)			
			page = simplePageToolDao.getPage(Long.parseLong(item.getSakaiId()));
		else
			page = simplePageToolDao.getPage(item.getPageId());
		
		if(page != null)			
		{
			String siteId = page.getSiteId();

			Site site = null;
			try {
				site = siteService.getSiteVisit(siteId);
			} catch (IdUnusedException e) {
				throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
			} catch (PermissionException e) {
				throw new EntityNotFoundException("No access to site: " + siteId, siteId);
			}
			
			//check if given site contains the lessonbuilder tool
			//checkTool(site);
			
			setToolSession(site);

			//check if current user has permission to access to the lessonbuilder tool
			//checks tool read and item permission
			boolean hasUpdatePermission = checkUpdatePermission(siteId);

			if (!hasUpdatePermission)
			    checkItemPermission(siteId, item.getId());

			// remove after testing
			// hasUpdatePermission = false;

			String currentUserId = sessionManager.getCurrentSessionUserId();
			
			//if required item is not a page or we just want a single item
			if (item.getType() != SimplePageItem.PAGE || !fullTree)
			{
				if(simpleType)
					ret.add(new DecoratedItem(item.getId(), item.getName(), item.getType(), site.getId(), site.getTitle()));
				else
					addItem(item, ret, hasUpdatePermission, page);
				return ret.get(0);
			}
			
			// build map of all pages, so we can check if any is repeated
			Map<Long,SimplePage> pageMap = new HashMap<Long,SimplePage>();
				
			// all pages
			List<SimplePage> pages = simplePageToolDao.getSitePages(siteId);
			for (SimplePage p: pages)
				pageMap.put(p.getPageId(), p);
			
			if(simpleType)
				findAllSimplePages(item, ret, pageMap, hasUpdatePermission, site, currentUserId);
			else
				findAllPages(item, ret, pageMap, hasUpdatePermission, siteId, currentUserId);	
		}
		else
		{
			throw new EntityNotFoundException("Given id does not pertain to any page", id);
		}
		
		if (ret != null && ret.size() > 0)
		    return ret.get(0);
		return ret;
	}


	/**
	 * subnav-prerequisites
	 * example: /direct/lessons/subnav-prerequisites/SITEID.xml
	 */
	@EntityCustomAction(action = "subnav-prerequisites", viewKey = EntityView.VIEW_LIST)
	public String getPrerequisiteDataForSubNav(final EntityView view, final Map<String, Object> params) {
		final String siteId = view.getPathSegment(2);

		if (siteId == null || "".equals(siteId)) {
			throw new IllegalArgumentException("siteId is required");
		}
		Site site = null;
		try {
			site = siteService.getSiteVisit(siteId);
		} catch (IdUnusedException e) {
			throw new EntityNotFoundException("Invalid siteId: " + siteId, siteId);
		} catch (PermissionException e) {
			throw new EntityNotFoundException("No access to site: " + siteId, siteId);
		}

		setToolSession(site);

		final JSONObject result = new JSONObject();

		final List<SimplePage> topLevelPages = simplePageToolDao.getTopLevelPages(siteId);
		SimplePageBean simplePageBean = null;
		final String currentUserId = sessionManager.getCurrentSessionUserId();

		for (SimplePage topLevelPage : topLevelPages) {
			final List<SimplePageItem> itemsOnPage = simplePageToolDao.findItemsOnPage(topLevelPage.getPageId());
			final JSONArray inaccessibleItems = new JSONArray();

			for (SimplePageItem item : itemsOnPage) {
				simplePageBean = makeSimplePageBean(simplePageBean, siteId, item);
				if (!lessonsAccess.isItemAccessible(item.getId(), siteId, currentUserId, simplePageBean)) {
					inaccessibleItems.add(String.valueOf(item.getId()));
				}
			}

			result.put(topLevelPage.getToolId(), inaccessibleItems);
		}

		return result.toJSONString();
	}
	
	
	@Override
	public String[] getHandledOutputFormats() {
		return new String[] {Formats.XML, Formats.JSON};
	}
	
	// --------------------------------------------------------------------------------
	//								OUTPUT OBJECTS
	// --------------------------------------------------------------------------------
	
	//for getEntity and action=lesson & type=simple 
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedItem {
		private long id;
		private String lessonTitle;
		private int type;
		private String siteId;
		private String siteTitle;
	}

	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedPageItem extends DecoratedItem {
		private String contentsURL;
		public DecoratedPageItem(long id, String title, int type, String siteId, String siteTitle, String contentsURL) {
		    super(id, title, type, siteId, siteTitle);
		    this.contentsURL = contentsURL;
		}		    
	}

	// for action=site
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedSiteItem {
		private long id;
		private String lessonTitle;
		private String contentsURL;
		private boolean hidden;
		private Date releaseDate;
		private Double gradebookPoints;
	}
	
	//for action=user
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedUserItem {
		private long id;
		private String lessonTitle;
		private String siteId;
		private String siteTitle;
		private String contentsURL;
	}
	
	// simplified version of SimplePageItem (base object) for action=lesson & type!=simple(default)
	/**
	 * LessonBase: is a wrapper of SimplePageItem, so we can show only the attributes we want. This class is the base from which all other classes extend.
	 * Any attribute shared by all kinds of "items", should be placed here. Attributes are called as SimplePageItem to avoid misunderstandings
	 */
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class LessonBase {
		private long id;
		private String name;
		private int type;		
		private long pageId;
		private boolean prerequisite;
		private boolean required;
		private String description;
		
		public LessonBase(SimplePageItem item)
		{
			if(item != null)
			{
				this.id = item.getId();
				this.name = item.getName();
				this.type = item.getType();				
				this.pageId = item.getPageId();
				this.prerequisite = item.isPrerequisite();
				this.required = item.isRequired();
				this.description = item.getDescription();
			}
		}
	}
	
	//(based on LessonBase) for most cases
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedLesson extends LessonBase{		
		private String sakaiId;
		private String html;
		private String url;
		
		public DecoratedLesson(SimplePageItem item)
		{
			super(item);
			if(item != null)
			{
				this.sakaiId = item.getSakaiId();
				this.html = item.getHtml();
				this.url = item.getURL();
			}
		}
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedLessonPage extends DecoratedLesson {
		private String contentsURL;
		private List<LessonBase> contentsList;
		private boolean hidden;
		private Date releaseDate;
		private Double gradebookPoints;
		
		public void setContentsList(List<LessonBase> list) {
		    contentsList = list;
		}
		public DecoratedLessonPage(SimplePageItem item, SimplePage simplePage) {
		    super(item);
		    String baseURL = developerHelperService.getEntityURL(REFERENCE_ROOT, EntityView.VIEW_LIST, null);   //   /direct/lessons
		    baseURL = baseURL + "/lesson/";
		    this.contentsURL = baseURL + item.getId();
		    this.contentsList = null;
		    
		    if (simplePage != null) {
		        this.hidden = simplePage.isHidden();
		        this.releaseDate = simplePage.getReleaseDate();
		        this.gradebookPoints = simplePage.getGradebookPoints();
		    }
		}		    
	}
	
	//(based on LessonBase) for most cases
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedResource extends DecoratedLesson{                
	    private Boolean openInNewWindow;
	    private String contentType;

	    public DecoratedResource(SimplePageItem item)
	    {
	        super(item);
	        if(item != null)
	        {
	            this.openInNewWindow = !item.isSameWindow();
	            
	            // retrieve the content type for this resource since the html
	            // field doesn't differentiate html from url resources
	            ContentResource resource = null;
	            try {
	                resource = contentHostingService.getResource(item.getSakaiId());
	                if (resource != null) {
	                    this.contentType = resource.getContentType();
	                }
	            } catch (Exception e) {
	                log.warn("Exception when retrieving resource for sakaiId:" + item.getSakaiId() + ". " + e);
	            } 
	        }
	    }
	}
        
	// For properties related to grading a DecoratedLesson
        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public class GradedDecoratedLesson extends DecoratedLesson{                
            private String requiredPointsEarned;

            public GradedDecoratedLesson(SimplePageItem item)
            {
                super(item);
                if(item != null && item.getSubrequirement())
                {
                    this.requiredPointsEarned = item.getRequirementText();
                }
            }
        }
        
        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public class DecoratedStudentContent extends DecoratedLesson{                
            private Integer contentGradebookPoints;
            private Integer commentsGradebookPoints;
            private boolean addCommentsSection;

            public DecoratedStudentContent(SimplePageItem item)
            {
                super(item);
                this.contentGradebookPoints = item.getGradebookPoints();
                this.commentsGradebookPoints = item.getAltPoints();
                this.addCommentsSection = item.getShowComments();
            }
        }
        
        @NoArgsConstructor
        @AllArgsConstructor
        @Data
        public class DecoratedComments extends DecoratedLesson {
            private Integer gradebookPoints;

            public DecoratedComments(SimplePageItem item)
            {
                super(item);
                this.gradebookPoints = item.getGradebookPoints();
            }
        }

	//for question items (base)
	@NoArgsConstructor
	@Data
	public class DecoratedQuiz extends LessonBase {
		private String questionText;
		private String questionCorrectText;
		private String questionIncorrectText;
		private String questionType;
		private String questionGraded;
		
		public DecoratedQuiz(SimplePageItem item)
		{
			super(item);
		}
		
		public DecoratedQuiz(SimplePageItem item, String questionText, String questionCorrectText, String questionIncorrectText, String questionType, String questionGraded)
		{
			super(item);
			this.questionText = questionText;
			this.questionCorrectText = questionCorrectText;
			this.questionIncorrectText = questionIncorrectText;
			this.questionType = questionType;
			this.questionGraded = questionGraded;
		}
	}
	
	//for multiple choice questions
	@NoArgsConstructor
	@Data
	public class DecoratedMultipleChoiceQuestion extends DecoratedQuiz {
		private String questionShowPoll;
		private List<DecoratedAnswerItem> answersList;
		
		public DecoratedMultipleChoiceQuestion(SimplePageItem item)
		{
			super(item);
			answersList = new ArrayList<DecoratedAnswerItem>();
		}
		
		public DecoratedMultipleChoiceQuestion(SimplePageItem item, String questionText, String questionCorrectText, String questionIncorrectText, String questionGraded)
		{
			super(item, questionText, questionCorrectText, questionIncorrectText, "multipleChoice", questionGraded);
			answersList = new ArrayList<DecoratedAnswerItem>();
		}
		
		public DecoratedMultipleChoiceQuestion(SimplePageItem item, String questionText, String questionCorrectText, String questionIncorrectText, String questionGraded, String questionShowPoll)
		{
			super(item, questionText, questionCorrectText, questionIncorrectText, "multipleChoice", questionGraded);
			this.questionShowPoll = questionShowPoll;
			answersList = new ArrayList<DecoratedAnswerItem>();
		}
		
		public void addAnswer(DecoratedAnswerItem answer)
		{
			answersList.add(answer);
		}		
	}
	
	//for shortanswer questions
	@NoArgsConstructor
	@Data
	public class DecoratedShortAnswerQuestion extends DecoratedQuiz {
		private String questionAnswer;
		
		public DecoratedShortAnswerQuestion(SimplePageItem item)
		{
			super(item);
		}
		
		public DecoratedShortAnswerQuestion(SimplePageItem item, String questionText, String questionCorrectText, String questionIncorrectText, String questionGraded)
		{
			super(item, questionText, questionCorrectText, questionIncorrectText, "shortanswer", questionGraded);
		}
		
		public DecoratedShortAnswerQuestion(SimplePageItem item, String questionText, String questionCorrectText, String questionIncorrectText, String questionGraded, String questionAnswer)
		{
			super(item, questionText, questionCorrectText, questionIncorrectText, "shortanswer", questionGraded);
			this.questionAnswer = questionAnswer;
		}		
	}
	
	//answers for multiple choice questions
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public class DecoratedAnswerItem {
		private long id;
		private String text;
		private Boolean correct;
	}
	
	
	// --------------------------------------------------------------------------------
	//								PRIVATE METHODS
	// --------------------------------------------------------------------------------
	private void checkTool(Site site) {
		if(site != null)		
		{
			//check user can access the tool, it might be hidden
			Collection col = site.getTools(TOOL_COMMON_ID);			
			if(col == null || col.isEmpty())
				throw new EntityNotFoundException("No tool "+TOOL_COMMON_ID+" in site: " + site.getId(), site.getId());
				
			for(Object o : col)
			{
				ToolConfiguration toolConfig = (ToolConfiguration)o;
				if(!toolManager.isVisible(site, toolConfig))
					throw new EntityNotFoundException("No access to tool in site: " + site.getId(), site.getId());
			}
		}
		else
			throw new EntityNotFoundException("Invalid site", "-null-");
	}	
	
    // Some of the tools (e.g. Formums) require a valid session in order to do permission
    // checking. So if we don't already have a session with the right site, set one up.

	private void setToolSession(Site site) {
	    Placement p = toolManager.getCurrentPlacement();
	    if (p == null || !site.getId().equals(p.getContext())) {
		ToolConfiguration siteTool = site.getToolForCommonId(TOOL_COMMON_ID);
		ActiveTool tool = ActiveToolManager.getActiveTool(siteTool.getToolId());
		Session ses = sessionManager.getCurrentSession();
		ToolSession toolSession = ses.getToolSession(siteTool.getId());
		sessionManager.setCurrentToolSession(toolSession);
		ThreadLocalManager.set(CURRENT_PLACEMENT, siteTool);
		ThreadLocalManager.set(CURRENT_TOOL, tool);
	    }

	}
	private void checkReadPermission(String siteId) {
		checkReadPermission(siteId, sessionManager.getCurrentSessionUserId());
	}
	
	private void checkItemPermission(String siteId, long itemId) {
	    String currentUserId = sessionManager.getCurrentSessionUserId();
	    checkReadPermission(siteId, currentUserId);
	    if (!lessonsAccess.isItemAccessible(itemId, siteId, currentUserId, null)) {
		throw new SecurityException("User "+currentUserId+" does not have permission to read lessons on site " + siteId);
	    }
	}

	private void checkReadPermission(String siteId, String userId) {
		if(!securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_READ, siteService.siteReference(siteId))) {
			throw new SecurityException("User "+userId+" does not have permission to read lessons on site " + siteId);
		}
	}
	
	private boolean checkUpdatePermission(String siteId) {
		return checkUpdatePermission(siteId, sessionManager.getCurrentSessionUserId());
	}
	
	private boolean checkUpdatePermission(String siteId, String userId) {
		return securityService.unlock(userId, SimplePage.PERMISSION_LESSONBUILDER_UPDATE, siteService.siteReference(siteId));
	}
	
	private SimplePage getPage(SimplePageBean simplePageBean, Long pageId) {
	    // use cached version if we have the bean
	    if (simplePageBean != null)
		return simplePageBean.getPage(pageId);
	    else
		return simplePageToolDao.getPage(pageId);
	}

	private SimplePageBean makeSimplePageBean(SimplePageBean simplePageBean,String siteId, SimplePageItem item) {
	    SimplePage page = null;
	    if (item.getType() == SimplePageItem.PAGE)			
		page = getPage(simplePageBean, Long.parseLong(item.getSakaiId()));
	    else
		page = getPage(simplePageBean, item.getPageId());
	    if (simplePageBean == null)
		simplePageBean = lessonsAccess.makeSimplePageBean(null, siteId, page);
	    else {
		// reuse current bean
		simplePageBean.setCurrentPage(page);
		simplePageBean.setCurrentPageId(page.getPageId());
	    }
	    return simplePageBean;
	}

	private synchronized List<Site> getUserSites() {
		List<Site> siteList = new ArrayList<Site>();
		
		try
		{			
			siteList = siteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ACCESS, null, null, null, SortType.TITLE_ASC, null);		
		}catch(Exception e){
			log.warn("getUserSites -> "+e);
		}
		return siteList;
	}
	
    // caller has done checkItemPermissions for the page. We have to check it
    // for all items on the page. Since caller has checked site read we
    // only have to check isItemAccessible.

	/**
	 * findAllPages : explores lessonbuilder tree from a base pageItem and fills a list of LessonBase items
	 * 
	 * @param pageItem
	 * @param entries
	 * @param pageMap
	 * @param hasUpdatePermission
	 */
    private void findAllPages(SimplePageItem pageItem, List<LessonBase> entries, Map<Long,SimplePage> pageMap, boolean hasUpdatePermission, String siteId, String currentUserId) {
	    Long pageId = Long.valueOf(pageItem.getSakaiId());	    	    
	    // already done if page is null
	    if (pageMap.get(pageId) == null)
	    	return;

	    
	    SimplePage currPage = pageMap.get(pageId);
	    // say done
	    pageMap.remove(pageId);
	    LessonBase lessonPage = addItem(pageItem, entries, hasUpdatePermission, currPage);

	    // now recursively do subpages
	    List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);

	    if (items != null && items.size() > 0) {
				if (lessonPage instanceof DecoratedLessonPage) {
				    entries = new ArrayList<LessonBase>();
				    ((DecoratedLessonPage)lessonPage).setContentsList(entries);
				}

	    SimplePageBean simplePageBean = null;

	    // subpages done in place
	    for (SimplePageItem item: items) {
			
		simplePageBean = makeSimplePageBean(simplePageBean, siteId, item);

		// skip if don't have permission
		if (!hasUpdatePermission && !lessonsAccess.isItemAccessible(item.getId(), siteId, currentUserId, simplePageBean))
		    continue;

	    	if (item.getType() == SimplePageItem.PAGE)
		    findAllPages(item, entries, pageMap, hasUpdatePermission, siteId, currentUserId );
		else
		    addItem(item, entries, hasUpdatePermission, simplePageBean.getCurrentPage());
	    }
	    }
	}
	
    // caller has done checkItemPermissions for the page. We have to check it
    // for all items on the page. Since caller has checked site read we
    // only have to check isItemAccessible.

	/**
	 * findAllPages : explores lessonbuilder tree from a base pageItem and fills a list of DecoratedItem items
	 * 
	 * @param pageItem
	 * @param entries
	 * @param pageMap
	 * @param site
	 */
	private void findAllSimplePages(SimplePageItem pageItem, List<DecoratedItem> entries, Map<Long,SimplePage> pageMap, boolean hasUpdatePermission, Site site, String currentUserId) {
	    Long pageId = Long.valueOf(pageItem.getSakaiId());	
	    // already done if page is null
	    if (pageMap.get(pageId) == null)
	    	return;

	    // say done
	    pageMap.remove(pageId);
		entries.add(new DecoratedItem(pageItem.getId(), pageItem.getName(), pageItem.getType(), site.getId(), site.getTitle()));

	    // now recursively do subpages
	    List<SimplePageItem> items = simplePageToolDao.findItemsOnPage(pageId);
	    
	    SimplePageBean simplePageBean = null;
	    
	    // subpages done in place
	    for (SimplePageItem item: items) {
			
		simplePageBean = makeSimplePageBean(simplePageBean, site.getId(), item);

		// skip if don't have permission
		if (!hasUpdatePermission && !lessonsAccess.isItemAccessible(item.getId(), site.getId(), currentUserId, simplePageBean))
		    continue;

	    	if (item.getType() == SimplePageItem.PAGE)
		    findAllSimplePages(item, entries, pageMap, hasUpdatePermission, site, currentUserId);
		else
		    entries.add(new DecoratedItem(item.getId(), item.getName(), item.getType(), site.getId(), site.getTitle()));
	    }
	}	
	
	
	
	/**
	 * addItem : creates a new LessonBase item based on the given SimplePageItem item and adds it to the given list
	 * depending of SimplePageItem's type, the new LessonBase object could be one of the following:
	 * 	- QUESTION -> DecoratedQuiz (DecoratedMultipleChoiceQuestion or DecoratedShortAnswerQuestion)
	 * 	- OTHERS -> DecoratedLesson
	 * 
	 * @param item
	 * @param list
	 * @param hasUpdatePermission
	 * @param simplePage
	 */
	private LessonBase addItem(SimplePageItem item, List<LessonBase> list, boolean hasUpdatePermission, SimplePage simplePage)
	{
		if(list == null)
			list = new ArrayList<LessonBase>();
					
		if(item != null)
		{
			LessonBase lesson = null;
			//check type
			switch (item.getType()) {
				case SimplePageItem.QUESTION:
					if ("multipleChoice".equals(item.getAttribute("questionType"))) {
						lesson = new DecoratedMultipleChoiceQuestion(item,
								item.getAttribute("questionText"),
								item.getAttribute("questionCorrectText"),
								item.getAttribute("questionIncorrectText"),
								item.getAttribute("questionGraded"),
								item.getAttribute("questionShowPoll"));
	
						List<SimplePageQuestionAnswer> answers = simplePageToolDao
								.findAnswerChoices(item);
						for (SimplePageQuestionAnswer a : answers) {
							DecoratedAnswerItem answer = new DecoratedAnswerItem();
							answer.setId(a.getId());
							answer.setText(a.getText());
							// only show correct value if has permissions
							if (hasUpdatePermission)
								answer.setCorrect(a.isCorrect());
	
							((DecoratedMultipleChoiceQuestion) lesson)
									.addAnswer(answer);
						}
	
					} else if ("shortanswer".equals(item
							.getAttribute("questionType"))) {
						lesson = new DecoratedShortAnswerQuestion(item,
								item.getAttribute("questionText"),
								item.getAttribute("questionCorrectText"),
								item.getAttribute("questionIncorrectText"),
								item.getAttribute("questionGraded"),
								item.getAttribute("questionAnswer"));
					}
					break;
				case SimplePageItem.PAGE:
					lesson = new DecoratedLessonPage(item, simplePage);
					break;
				case SimplePageItem.RESOURCE:
					lesson = new DecoratedResource(item);
					break;
				case SimplePageItem.ASSIGNMENT:
					lesson = new GradedDecoratedLesson(item);
					break;
				case SimplePageItem.ASSESSMENT:
					lesson = new GradedDecoratedLesson(item);
					break;
				case SimplePageItem.STUDENT_CONTENT:
					lesson = new DecoratedStudentContent(item);
					break;
				case SimplePageItem.COMMENTS:
					lesson = new DecoratedComments(item);
					break;
				default:
					lesson = new DecoratedLesson(item);
					break;
			}

			list.add(lesson);
			return lesson;
		}
		return null;
	}
	
	@Setter
	private SecurityService securityService;
	
	@Setter
	private SessionManager sessionManager;
	
	@Setter
	private SiteService siteService;	
	
	@Setter
	private ToolManager toolManager;

	@Setter
	private SimplePageToolDao simplePageToolDao;	

	@Setter
	private LessonsAccess lessonsAccess;

        @Setter
        private DeveloperHelperService developerHelperService;
        
        @Setter
        private ContentHostingService contentHostingService;

}
