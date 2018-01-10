/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.search.api.SearchService;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.ToolManager;

import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiObjectService;
import uk.ac.cam.caret.sakai.rwiki.service.api.RWikiSecurityService;
import uk.ac.cam.caret.sakai.rwiki.service.api.model.RWikiObject;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.MessageService;
import uk.ac.cam.caret.sakai.rwiki.service.message.api.PreferenceService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.PopulateService;
import uk.ac.cam.caret.sakai.rwiki.tool.api.ToolRenderService;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupCollectionBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.AuthZGroupEditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.DiffBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.EditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ErrorBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.FullSearchBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HistoryBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.HomeBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.MultiRealmEditBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PermissionsBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PrePopulateBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PreferencesBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PresenceBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.RecentlyVisitedBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ReferencesBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.RenderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ResourceLoaderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.SearchBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ToolConfigBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.UpdatePermissionsBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.AuthZGroupBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.AuthZGroupCollectionBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.AuthZGroupEditBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.DiffHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.MultiRealmEditBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.PreferencesBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.PresenceBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.RecentlyVisitedHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ReverseHistoryHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ReviewHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.UpdatePermissionsBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.UserHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.ViewParamsHelperBean;
import uk.ac.cam.caret.sakai.rwiki.tool.util.WikiPageAction;

/**
 * This is a replacement for the RequestScopeApplicationContext which turned out
 * to be too slow. It is hideous and probably very confusing however it will
 * allow me to keep things working whilst I refactor.
 * 
 * @author andrew
 */
@Slf4j
public class RequestScopeSuperBean
{

	public static final String REQUEST_ATTRIBUTE = "rsacMap";

	private HttpServletRequest request;

	private HashMap<String, Object> map = new HashMap<String, Object>();

	private ApplicationContext context;

	private RWikiSecurityService securityService;

	private RWikiObjectService objectService;

	private ToolRenderService toolRenderService;

	private PopulateService populateService;

	private AuthzGroupService realmService;

	private MessageService messageService;

	private PreferenceService preferenceService;

	private SearchService searchService;
	
	private ResourceLoaderBean resourceLoader;

	private boolean experimental = false;

	private boolean withnotification = false;

	private boolean withcomments = false;

	private org.sakaiproject.tool.api.ToolManager toolManager;

	private SessionManager sessionManager;

	private SiteService siteService;

	private String defaultUIHomePageName;

	private boolean searchExperimental = false;
	
	public static RequestScopeSuperBean getFromRequest(
			HttpServletRequest request)
	{
		return (RequestScopeSuperBean) request.getAttribute(REQUEST_ATTRIBUTE);
	}
	
	// Thread scope
	private static ThreadLocal<RequestScopeSuperBean> requestScopeSuperBeanHolder = new ThreadLocal<RequestScopeSuperBean>();
	public static RequestScopeSuperBean getInstance() {
		return requestScopeSuperBeanHolder.get();
	}
	
	public static void clearInstance() {
		//NameHelper.clearDefaultPage();
		requestScopeSuperBeanHolder.set(null);
		
	}

	public static RequestScopeSuperBean createAndAttach(
			HttpServletRequest request, ApplicationContext context)
	{
		RequestScopeSuperBean rssb = new RequestScopeSuperBean();
		rssb.setRequest(request);
		rssb.setContext(context);
		rssb.init();

		request.setAttribute(REQUEST_ATTRIBUTE, rssb);
		// add it to thread scope
		requestScopeSuperBeanHolder.set(rssb);

		return rssb;
	}

	public void init()
	{
		securityService = (RWikiSecurityService) context
				.getBean(RWikiSecurityService.class.getName());
		objectService = (RWikiObjectService) context
				.getBean(RWikiObjectService.class.getName());
		toolRenderService = (ToolRenderService) context
				.getBean(ToolRenderService.class.getName());
		populateService = (PopulateService) context
				.getBean(PopulateService.class.getName());
		realmService = (AuthzGroupService) context
				.getBean(AuthzGroupService.class.getName());
		preferenceService = (PreferenceService) context
				.getBean(PreferenceService.class.getName());

		toolManager = (ToolManager) context
				.getBean(ToolManager.class.getName());

		sessionManager = (SessionManager) context.getBean(SessionManager.class
				.getName());
		siteService = (SiteService) context
				.getBean(SiteService.class.getName());


		messageService = (MessageService) context.getBean(MessageService.class
				.getName());

		experimental = ServerConfigurationService.getBoolean(
				"wiki.experimental", false);

		searchExperimental  = ServerConfigurationService.getBoolean(
				  "wiki.fullsearch", true) && 
				  ServerConfigurationService.getBoolean("search.enable", false);

		withnotification = ServerConfigurationService.getBoolean(
				"wiki.notification", true);
		withcomments = ServerConfigurationService.getBoolean("wiki.comments",
				true);
		defaultUIHomePageName = ServerConfigurationService.getString(
				"wiki.ui.homepage", "Home");
		
		if ( searchExperimental ) {
			searchService = (SearchService) context.getBean(SearchService.class
				.getName());
		}
		
		// if the message service has been configured
		// update the presence
		if (messageService != null)
		{
			Session session = sessionManager.getCurrentSession();

			String userId = this.getCurrentUserId();
			if (userId != null && userId.length() > 0)
			{
				String currentPageName = this.getCurrentPageName();
				String pageSpace = this.getCurrentPageSpace();
				if ( currentPageName != null && currentPageName.length() < 255 && pageSpace != null && pageSpace.length() < 255 ) {
					messageService.updatePresence(session.getId(), userId, this.getCurrentPageName(), this
							.getCurrentPageSpace());
				} else {
					log.warn("Page names in wiki cannot be over 225 characters in length, presence not updated. Page Name was "+currentPageName);
				}
			}
		}
		

	}

	public HttpServletRequest getRequest()
	{
		return request;
	}

	public void setRequest(HttpServletRequest request)
	{
		this.request = request;
	}

	public ApplicationContext getContext()
	{
		return context;
	}

	public void setContext(ApplicationContext context)
	{
		this.context = context;
	}

	public ViewParamsHelperBean getNameHelperBean()
	{
		String key = "nameHelperBean";
		if (map.get(key) == null)
		{
			ViewParamsHelperBean vphb = new ViewParamsHelperBean();
			vphb.setServletRequest(request);
			vphb.setSecurityService(securityService);
			vphb.setToolConfigBean(getConfigBean());
			vphb.init();
			map.put(key, vphb);
		}
		return (ViewParamsHelperBean) map.get(key);
	}

	public String getCurrentLocalSpace()
	{
		String key = "currentLocalSpace";
		if (map.get(key) == null)
		{
			ViewParamsHelperBean vphb = this.getNameHelperBean();
			map.put(key, vphb.getLocalSpace());
		}

		return (String) map.get(key);
	}

	public String getCurrentDefaultRealm()
	{
		String key = "currentDefaultRealm";
		if (map.get(key) == null)
		{
			ViewParamsHelperBean vphb = this.getNameHelperBean();
			map.put(key, vphb.getDefaultRealm());
		}
		return (String) map.get(key);
	}

	public String getCurrentPageName(boolean refresh)
	{
		String key = "currentPageName";
		if (map.get(key) == null || refresh )
		{
			map.put(key, getNameHelperBean().getGlobalName());
		}
		return (String) map.get(key);
	}
	public String getCurrentPageName()
	{
		return getCurrentPageName(false);
	}

	public String getCurrentPageSpace()
	{
		String key = "currentPageSpace";
		if (map.get(key) == null)
		{
			map.put(key, getNameHelperBean().getPageSpace());
		}
		return (String) map.get(key);
	}

	public String getCurrentSearch()
	{
		String key = "currentSearch";
		if (map.get(key) == null)
		{
			map.put(key, getNameHelperBean().getSearch());
		}
		return (String) map.get(key);
	}

	/**
	 * The currently requested page
	 * 
	 * @return
	 */
	public String getCurrentSearchPage()
	{
		String key = "currentSearchPage";
		if (map.get(key) == null)
		{
			map.put(key, getNameHelperBean().getSearchPage());
		}
		return (String) map.get(key);
	}
	
	public String getCurrentUserId() 
	{
		String key = "currentUserId";
		if (map.get(key) == null)
		{
			map.put(key, sessionManager.getCurrentSessionUserId());
		}
		return (String) map.get(key);
	}

	public String getCurrentUser()
	{
		String key = "currentUser";
		if (map.get(key) == null)
		{
			UserHelperBean uhb = new UserHelperBean();
			uhb.setServletRequest(request);
			uhb.init();
			map.put(key, uhb.getUser());
		}
		return (String) map.get(key);
	}

	public String getWorksiteOwner()
	{
		try
		{
			Site s = siteService.getSite(toolManager.getCurrentPlacement()
					.getContext());
			return s.getCreatedBy().getId();
		}
		catch (IdUnusedException e)
		{
		}
		return "admin";
	}

	public ViewBean getViewBean()
	{
		String key = "viewBean";
		if (map.get(key) == null)
		{
			ViewBean vb = new ViewBean();
			vb.setLocalSpace(this.getCurrentLocalSpace());
			vb.setPageName(this.getCurrentPageName());
			map.put(key, vb);
		}
		return (ViewBean) map.get(key);
	}
	
	public ResourceLoaderBean getResourceLoaderBean()
	{
		String key = "resourceLoaderBean";
		if (map.get(key) == null)
		{
			ResourceLoaderBean rb = new ResourceLoaderBean();
			rb.init(request);
			map.put(key, rb);
		}
		return (ResourceLoaderBean) map.get(key);
	}


	public RWikiObject getCurrentRWikiObject(boolean refresh)
	{
		String key = "currentRWikiObject";
		if (map.get(key) == null || refresh )
		{

			RWikiObject rwo = objectService.getRWikiObject(
					getCurrentPageName(), getCurrentLocalSpace());
			map.put(key, rwo);
		}
		return (RWikiObject) map.get(key);
	}

	public RWikiObject getCurrentRWikiObject()
	{
		return getCurrentRWikiObject(false);
	}
	
	public String getCurrentRWikiObjectReference() 
	{
		String key = "currentRWikiObjectReference";
		if ( map.get(key) == null ) 
		{
			RWikiObject rwo = getCurrentRWikiObject();
			Entity e =  objectService.getEntity(rwo);
			map.put(key,e.getReference());
		}
		return (String) map.get(key);
	}

	public RecentlyVisitedBean getRecentlyVisitedBean()
	{
		String key = "recentlyVisitedBean";
		if (map.get(key) == null)
		{
			RecentlyVisitedHelperBean rvhb = new RecentlyVisitedHelperBean();
			rvhb.setServletRequest(request);
			rvhb.setDefaultSpace(getCurrentDefaultRealm());
			rvhb.init();
			map.put(key, rvhb.getRecentlyVisitedBean());
		}
		return (RecentlyVisitedBean) map.get(key);
	}

	public boolean getWithBreadcrumbs()
	{
		String key = "withBreadcrumbs";
		if (map.get(key) == null)
		{
			ViewParamsHelperBean vphb = getNameHelperBean();
			map.put(key, vphb.getWithBreadcrumbs());
		}
		return !"0".equals(map.get(key));
	}

	public RenderBean getRenderBean()
	{
		String key = "renderBean";
		if (map.get(key) == null)
		{
			RenderBean rb = new RenderBean(getCurrentRWikiObject(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public HistoryBean getHistoryBean()
	{
		String key = "historyBean";
		if (map.get(key) == null)
		{
			HistoryBean hb = new HistoryBean(getCurrentRWikiObject(),
					getCurrentLocalSpace());
			map.put(key, hb);
		}
		return (HistoryBean) map.get(key);
	}

	public ReverseHistoryHelperBean getReverseHistoryHelperBean()
	{
		String key = "reverseHistoryHelperBean";
		if (map.get(key) == null)
		{
			ReverseHistoryHelperBean rhhb = new ReverseHistoryHelperBean();
			rhhb.setRwikiObject(getCurrentRWikiObject());
			rhhb.setRwikiObjectService(objectService);
			map.put(key, rhhb);
		}
		return (ReverseHistoryHelperBean) map.get(key);
	}

	public DiffBean getDiffBean()
	{
		String key = "diffBean";
		if (map.get(key) == null)
		{
			DiffHelperBean dhb = new DiffHelperBean();
			dhb.setServletRequest(request);
			dhb.setRwikiObject(getCurrentRWikiObject());
			dhb.setRwikiObjectService(objectService);
			dhb.init();
			map.put(key, dhb.getDiffBean());
		}
		return (DiffBean) map.get(key);
	}

	public PrePopulateBean getPrePopulateBean()
	{
		String key = "prePopulateBean";
		if (map.get(key) == null)
		{
			PrePopulateBean ppb = new PrePopulateBean();
			ppb.setPopulateService(populateService);
			ppb.setCurrentGroup(getCurrentDefaultRealm());
			ppb.setCurrentPageRealm(getCurrentPageSpace());
			ppb.setWoksiteOwner(getWorksiteOwner());
			map.put(key, ppb);
		}
		return (PrePopulateBean) map.get(key);
	}

	public FullSearchBean getFullSearchBean()
	{
		if ( searchExperimental ) 
		{
			String key = "fullSearchBean";
			if (map.get(key) == null)
			{
				FullSearchBean sb = new FullSearchBean(getCurrentSearch(),
						getCurrentSearchPage(), getCurrentLocalSpace(),
						searchService, toolManager);
				map.put(key, sb);
				
			}
			return (FullSearchBean) map.get(key);
		} 
		return null;
	}

	public SearchBean getSearchBean()
	{
		String key = "searchBean";
		if (map.get(key) == null)
		{
			SearchBean sb = new SearchBean(getCurrentSearch(),
					getCurrentLocalSpace(), objectService);
			map.put(key, sb);
		}
		return (SearchBean) map.get(key);
	}

	public PermissionsBean getPermissionsBean()
	{
		String key = "permissionsBean";
		if (map.get(key) == null)
		{
			PermissionsBean pb = new PermissionsBean(getCurrentRWikiObject(),
					objectService);
			map.put(key, pb);
		}
		return (PermissionsBean) map.get(key);
	}

	public ErrorBean getErrorBean()
	{
		String key = "errorBean";
		if (map.get(key) == null)
		{
			map.put(key, new ErrorBean());
		}
		return (ErrorBean) map.get(key);
	}

	public EditBean getEditBean()
	{
		String key = "editBean";
		if (map.get(key) == null)
		{
			ViewParamsHelperBean vphb = getNameHelperBean();

			EditBean editBean = new EditBean();
			editBean.setPreviousContent(vphb.getContent());
			editBean.setPreviousVersion(vphb.getSubmittedVersion());
			editBean.setSaveType(vphb.getSaveType());
			map.put(key, editBean);
		}
		return (EditBean) map.get(key);
	}

	public ReviewHelperBean getReviewHelperBean()
	{
		String key = "reviewHelperBean";
		if (map.get(key) == null)
		{
			ReviewHelperBean rhb = new ReviewHelperBean();
			rhb.setServletRequest(request);
			rhb.setRwikiObject(getCurrentRWikiObject());
			rhb.setRwikiObjectService(objectService);
			rhb.init();
			map.put(key, rhb);
		}
		return (ReviewHelperBean) map.get(key);
	}

	public RenderBean getReviewRenderBean()
	{
		String key = "reviewRenderBean";
		if (map.get(key) == null)
		{
			RenderBean rb = new RenderBean(getReviewHelperBean().getMock(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);

	}

	public RenderBean getViewRightRenderBean()
	{
		String key = "viewRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "view_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public RenderBean getEditRightRenderBean()
	{
		String key = "editRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "edit_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public RenderBean getInfoRightRenderBean()
	{
		String key = "infoRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "info_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public RenderBean getReviewRightRenderBean()
	{
		String key = "reviewRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "review_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public RenderBean getDiffRightRenderBean()
	{
		String key = "diffRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "diff_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public RenderBean getSearchRightRenderBean()
	{
		String key = "searchRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "search_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public RenderBean getPreviewRightRenderBean()
	{
		String key = "previewRightRenderBean";
		if (map.get(key) == null)
		{
			String pageName = "preview_right";
			RenderBean rb = new RenderBean(pageName, getCurrentDefaultRealm(),
					toolRenderService, objectService, getWithBreadcrumbs());
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

	public ReferencesBean getReferencesBean()
	{
		String key = "referencesBean";
		if (map.get(key) == null)
		{
			ReferencesBean rb = new ReferencesBean(getCurrentRWikiObject(),
					objectService, getCurrentLocalSpace());
			map.put(key, rb);
		}
		return (ReferencesBean) map.get(key);
	}

	public HomeBean getHomeBean()
	{
		String key = "homeBean";
		if (map.get(key) == null)
		{
			HomeBean hb = new HomeBean();
			ViewParamsHelperBean vphb = getNameHelperBean();
			ViewBean vb = new ViewBean(getConfigBean().getHomePage(), vphb.getDefaultRealm());
			hb.setHomeLinkUrl(vb.getViewUrl());
			hb.setHomeLinkValue(getConfigBean().getHomePageName());
			map.put(key, hb);
		}
		return (HomeBean) map.get(key);
	}

	public UpdatePermissionsBean getUpdatePermissionsBean()
	{
		String key = "updatePermissionsBean";
		if (map.get(key) == null)
		{
			UpdatePermissionsBean ub = UpdatePermissionsBeanHelper
					.createUpdatePermissionsBean(request, objectService);
			map.put(key, ub);
		}
		return (UpdatePermissionsBean) map.get(key);
	}

	public AuthZGroupBean getRealmBean()
	{
		String key = "realmBean";
		if (map.get(key) == null)
		{
			String siteId = toolManager.getCurrentPlacement().getContext();
			AuthZGroupBean rb = AuthZGroupBeanHelper.createRealmBean(
					realmService, siteService,getCurrentRWikiObject(), getErrorBean(),
					getViewBean(),siteId);
			map.put(key, rb);
		}
		return (AuthZGroupBean) map.get(key);
	}

	public AuthZGroupEditBean getRealmEditBean()
	{
		String key = "realmEditBean";
		if (map.get(key) == null)
		{
			AuthZGroupEditBean reb = AuthZGroupEditBeanHelper
					.createRealmEditBean(request, getViewBean());
			map.put(key, reb);
		}

		return (AuthZGroupEditBean) map.get(key);
	}

	public AuthZGroupCollectionBean getAuthZGroupCollectionBean()
	{
		String key = "authZGroupCollectionBean";
		if (map.get(key) == null)
		{
			AuthZGroupCollectionBean cb = AuthZGroupCollectionBeanHelper
					.createAuthZCollectionBean(realmService,
							getCurrentRWikiObject(), getViewBean(),
							objectService);
			map.put(key, cb);
		}
		return (AuthZGroupCollectionBean) map.get(key);
	}

	public MultiRealmEditBean getMultiRealmEditBean()
	{
		String key = "multiRealmEditBean";
		if (map.get(key) == null)
		{
			MultiRealmEditBean cb = MultiRealmEditBeanHelper
					.createMultiRealmEditBean(
							getCurrentRWikiObject(), request,this);
			map.put(key, cb);
		}
		return (MultiRealmEditBean) map.get(key);
	}

	public PresenceBean getPresenceBean()
	{
		String key = "presenceBean";
		PresenceBean pb = (PresenceBean) map.get(key);
		if (pb == null)
		{
			pb = PresenceBeanHelper.createRealmBean(messageService,
					getCurrentPageName(), getCurrentPageSpace());
			map.put(key, pb);
		}
		return pb;
	}

	/**
	 * @return Returns the messageService.
	 */
	public MessageService getMessageService()
	{
		return messageService;
	}

	public PreferencesBean getPreferencesBean()
	{
		String key = "preferencesBean";
		PreferencesBean pb = (PreferencesBean) map.get(key);
		if (pb == null)
		{
			pb = PreferencesBeanHelper.createPreferencesBean(getCurrentUserId(),
					getCurrentPageSpace(), preferenceService);
			map.put(key, pb);
		}
		return pb;
	}

	/**
	 * @return Returns the experimental.
	 */
	public boolean getExperimental()
	{
		return experimental;
	}

	/**
	 * @param experimental
	 *        The experimental to set.
	 */
	public void setExperimental(boolean experimental)
	{
		this.experimental = experimental;
	}

	public String getSearchTarget()
	{
		if ( searchExperimental )
		{
			return WikiPageAction.FULL_SEARCH_ACTION.getName();
		}
		else
		{
			return WikiPageAction.SEARCH_ACTION.getName();
		}
	}

	/**
	 * @return Returns the withnotification.
	 */
	public boolean isWithnotification()
	{
		return withnotification;
	}

	/**
	 * @param withnotification
	 *        The withnotification to set.
	 */
	public void setWithnotification(boolean withnotification)
	{
		this.withnotification = withnotification;
	}

	/**
	 * @return Returns the withcomments.
	 */
	public boolean isWithcomments()
	{
		return withcomments;
	}

	/**
	 * @param withcomments
	 *        The withcomments to set.
	 */
	public void setWithcomments(boolean withcomments)
	{
		this.withcomments = withcomments;
	}
	
	
	public ToolConfigBean getConfigBean()
	{
		String key = "toolConfigBean";
		ToolConfigBean configBean = (ToolConfigBean) map.get(key);
		if (configBean == null)
		{
			configBean = new ToolConfigBean(toolManager.getCurrentPlacement(), this.defaultUIHomePageName);
			map.put(key, configBean);
		}
		return configBean;
	}

	public boolean getLoadAutoSave() {
		boolean b =  getNameHelperBean().isLoadAutoSave();
		return b;
	}
	public boolean getRemoveAutoSave() {
		boolean b =  getNameHelperBean().isRemoveAutoSave();
		return b;
	}
	
	public String getPageRevisionContent(RWikiObject currentRWikiObject,int previousRevision) {
		RWikiObject  rwo = objectService.getRWikiHistoryObject(currentRWikiObject, previousRevision);
		if ( rwo == null ) {
			return "";
		} 
		return rwo.getContent();
	}
	

	public RenderBean getPreviewRenderBean()
	{
		String key = "renderBean";
		if (map.get(key) == null)
		{
			ViewParamsHelperBean vphb = getNameHelperBean();

			RenderBean rb = new RenderBean(getCurrentRWikiObject(),
					toolRenderService, objectService, getWithBreadcrumbs());
			String content = vphb.getContent();
			rb.setPreviewContent(content);
			map.put(key, rb);
		}
		return (RenderBean) map.get(key);
	}

}
