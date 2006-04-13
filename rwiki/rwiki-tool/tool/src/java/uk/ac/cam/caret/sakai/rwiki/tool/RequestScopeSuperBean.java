/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package uk.ac.cam.caret.sakai.rwiki.tool;

import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.api.AuthzGroupService;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.cover.SessionManager;
import org.springframework.context.ApplicationContext;

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
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PermissionsBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PrePopulateBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PreferencesBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.PresenceBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.RecentlyVisitedBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ReferencesBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.RenderBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.SearchBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.UpdatePermissionsBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.ViewBean;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.AuthZGroupBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.AuthZGroupCollectionBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.AuthZGroupEditBeanHelper;
import uk.ac.cam.caret.sakai.rwiki.tool.bean.helper.DiffHelperBean;
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
// FIXME: Tool
public class RequestScopeSuperBean
{
	private static Log log = LogFactory.getLog(RequestScopeSuperBean.class);

	public static final String REQUEST_ATTRIBUTE = "rsacMap";

	private HttpServletRequest request;

	private HashMap map = new HashMap();

	private ApplicationContext context;

	private RWikiSecurityService securityService;

	private RWikiObjectService objectService;

	private ToolRenderService toolRenderService;

	private PopulateService populateService;

	private AuthzGroupService realmService;

	private MessageService messageService;

	private PreferenceService preferenceService;

	// TODO: Search private SearchService searchService;

	private boolean experimental = false;

	private boolean withnotification = false;

	public static RequestScopeSuperBean getFromRequest(
			HttpServletRequest request)
	{
		return (RequestScopeSuperBean) request.getAttribute(REQUEST_ATTRIBUTE);
	}

	public static RequestScopeSuperBean createAndAttach(
			HttpServletRequest request, ApplicationContext context)
	{
		RequestScopeSuperBean rssb = new RequestScopeSuperBean();
		rssb.setRequest(request);
		rssb.setContext(context);
		rssb.init();

		request.setAttribute(REQUEST_ATTRIBUTE, rssb);

		return rssb;
	}

	public void init()
	{
		securityService = (RWikiSecurityService) context
				.getBean("securityService");
		objectService = (RWikiObjectService) context
				.getBean(RWikiObjectService.class.getName());
		toolRenderService = (ToolRenderService) context
				.getBean("toolRenderService");
		populateService = (PopulateService) context.getBean("populateService");
		realmService = (AuthzGroupService) context
				.getBean(AuthzGroupService.class.getName());
		preferenceService = (PreferenceService) context
				.getBean(PreferenceService.class.getName());

		// TODO: Search searchService = (SearchService)
		// context.getBean(SearchService.class
		// TODO: Search .getName());

		messageService = (MessageService) context.getBean(MessageService.class
				.getName());
		// if the message service has been configured
		// update the presence
		if (messageService != null)
		{
			Session session = SessionManager.getCurrentSession();

			messageService.updatePresence(session.getId(), this
					.getCurrentUser(), this.getCurrentPageName(), this
					.getCurrentPageSpace());
		}
		experimental = ServerConfigurationService.getBoolean(
				"wiki.experimental", false);
		withnotification = ServerConfigurationService.getBoolean(
				"wiki.notification", false);

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

	public String getCurrentPageName()
	{
		String key = "currentPageName";
		if (map.get(key) == null)
		{
			map.put(key, getNameHelperBean().getGlobalName());
		}
		return (String) map.get(key);
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
			Site s = SiteService.getSite(PortalService.getCurrentSiteId());
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

	public RWikiObject getCurrentRWikiObject()
	{
		String key = "currentRWikiObject";
		if (map.get(key) == null)
		{

			RWikiObject rwo = objectService.getRWikiObject(
					getCurrentPageName(), getCurrentLocalSpace());
			map.put(key, rwo);
		}
		return (RWikiObject) map.get(key);
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
		String key = "fullSearchBean";
		if (map.get(key) == null)
		{
			/*
			 * TODO Search FullSearchBean sb = new
			 * FullSearchBean(getCurrentSearch(), getCurrentSearchPage(),
			 * getCurrentLocalSpace(), searchService); map.put(key, sb);
			 */
		}
		return (FullSearchBean) map.get(key);
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
			ViewBean vb = new ViewBean(null, vphb.getDefaultRealm());
			hb.setHomeLinkUrl(vb.getViewUrl());
			hb.setHomeLinkValue(vb.getLocalName());
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
			AuthZGroupBean rb = AuthZGroupBeanHelper.createRealmBean(
					realmService, getCurrentRWikiObject(), getErrorBean(),
					getViewBean());
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
			pb = PreferencesBeanHelper.createPreferencesBean(getCurrentUser(),
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
		if (experimental)
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
}
