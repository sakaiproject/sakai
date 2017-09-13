/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
/**********************************************************************************
 * $URL: $
 * $Id: $
 ***********************************************************************************/


package org.sakaiproject.lessonbuildertool.tool.producers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.UrlItem;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.tool.producers.PermissionsHelperProducer;
import org.sakaiproject.lessonbuildertool.service.LessonBuilderAccessService;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.portal.util.CSSUtils;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.util.Web;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.localeutil.LocaleGetter;                                                                                          
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UILink;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.UIDisabledDecorator;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.components.decorators.UIStyleDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import org.springframework.core.io.Resource;


/**
 * View items such as quizes that are shown inline
 * 
 * @author Charles Hedrick <hedrick@rutgers.edu>
 */
public class ShowItemProducer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter {

	private SimplePageBean simplePageBean;
	private SimplePageToolDao simplePageToolDao;
	public MessageLocator messageLocator;
	public LocaleGetter localeGetter;
	private HttpServletRequest httpServletRequest;
        private LessonBuilderAccessService lessonBuilderAccessService;

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

	static final String ICONSTYLE = "\n.portletTitle .action .help img {\n        background: url({}/help.gif) center right no-repeat !important;\n}\n.portletTitle .action .help img:hover, .portletTitle .action .help img:focus {\n        background: url({}/help_h.gif) center right no-repeat\n}\n.portletTitle .title img {\n        background: url({}/reload.gif) center left no-repeat;\n}\n.portletTitle .title img:hover, .portletTitle .title img:focus {\n        background: url({}/reload_h.gif) center left no-repeat\n}\n";

	public static final String VIEW_ID = "ShowItem";

	public String getViewID() {
		return VIEW_ID;
	}
    
	public String myUrl() {
	    // previously we computed something, but this will give us the official one
	        return ServerConfigurationService.getServerUrl();
	}

	public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {

	    // to do assignment/quiz, etc arguments are
	    //   sendingpage, itemid - reflect the item that the user clicked
	    //   source - null
	    //   clearattr may be used for Samigo hack
	    //   return is to sendingpage, with path coming from current path retrieved from SimplePageBean
	    // to create a new assignment, quiz, etc
	    //   path non-null is what triggers this
	    //   sendingpage, id - these are arguments that we'll use to return to the "add assignment" page. id is pageitem ID
	    //   source - EDIT or SETTINGS
	    //   clearattr may be used for Samigo hack
	    //   returnview - viewID to return to
	    //   title - the string for the return button

	    // as far as I can see there are no permissions issues here. It just
	    // sticks things in an iframe. The stuff it sticks had better check though

	    GeneralViewParameters params = (GeneralViewParameters)viewParams;

	    UIOutput.make(tofill, "html").decorate(new UIFreeAttributeDecorator("lang", localeGetter.get().getLanguage()))
		.decorate(new UIFreeAttributeDecorator("xml:lang", localeGetter.get().getLanguage()));        

	    if (!simplePageBean.canReadPage()) {
		UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
		return;
	    }

	    long sendingPage = params.getSendingPage();

	    // the following code should check whether it's an assessment. Currently I don't
	    // bother as I haven't put links in the other tools
	    Session session = SessionManager.getCurrentSession();
	    ToolSession toolSession = SessionManager.getCurrentToolSession();
	    ToolConfiguration toolConfiguration = SiteService.findTool(toolSession.getPlacementId());
	    SitePage sitePage = toolConfiguration.getContainingPage();
	    String clearAttr = params.getClearAttr();
	    if (clearAttr != null && !clearAttr.equals("")) {
		// don't let users clear random attributes
		if (clearAttr.startsWith("LESSONBUILDER_RETURNURL")) {
		    String toolUrl = ServerConfigurationService.getPortalUrl() + "/site/" + sitePage.getSiteId() + "/page/" + sitePage.getId() + "?clearAttr=" + clearAttr;
		    session.setAttribute(clearAttr, toolUrl);
		}
	    }


	    String pathOp = params.getPath();
	    // only pop is valid; we don't have the data for the other options
	    if (pathOp != null && !pathOp.equals(""))
		simplePageBean.adjustPath(pathOp, params.getSendingPage(), null, null);

	    List<SimplePageBean.PathEntry> breadcrumbs = simplePageBean.getHierarchy();
	    SimplePageItem item = simplePageBean.findItem (params.getItemId());

	    // precompute tests we'll need more than once
	    int type = 0;
	    boolean available = false;
	    if (item != null) {
		type = item.getType();
		available = simplePageBean.isItemVisible(item) && simplePageBean.isItemAvailable(item, item.getPageId());
	    }
	    
	    // update permissions in tools if appropriate.
	    if (available) {
		if (type == SimplePageItem.RESOURCE || type == SimplePageItem.BLTI)
		    simplePageBean.track(params.getItemId(), null);
		else if (item.isPrerequisite() && (type == SimplePageItem.PAGE || type == SimplePageItem.ASSIGNMENT || type == SimplePageItem.ASSESSMENT || type == SimplePageItem.FORUM))
		    simplePageBean.checkItemPermissions(item, true); // set acl, etc		

	    }

	    boolean inline = false;
	    String portalTemplates = ServerConfigurationService.getString("portal.templates", "morpheus");
	    if ("morpheus".equals(portalTemplates) && httpServletRequest.getRequestURI().startsWith("/portal/site/")) {
		inline = true;
	    }

	    UIComponent portletBody = UIOutput.make(tofill, "portletBody");
	    portletBody.decorate(new UIFreeAttributeDecorator("class", "showItem" + (inline?" showItemMorpheus":" showItemNoMorpheus")));

	    // this is a "next" page where we couldn't tell if the item is
	    // available. Need to check here in order to set ACLs. If not available,
	    // return to calling page
	    if (item != null && "true".equals(params.getRecheck())) {
	        if (!available) {
		    SimplePageBean.PathEntry containingPage = null;
		    if (breadcrumbs.size() > 0)  // shouldn't ever fail
			containingPage = breadcrumbs.get(breadcrumbs.size()-1);  // page we're on
		    if (containingPage != null) {  // shouldn't fail
			GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
			view.setSendingPage(containingPage.pageId);
			view.setItemId(containingPage.pageItemId);
			view.setPath("next");
			UIInternalLink.make(tofill, "redirect-link", containingPage.title, view);
			UIOutput.make(tofill, "redirect");
		    }
		    return;
		}
	    } else if (item != null && item.getType() == SimplePageItem.RESOURCE) {
		// other item types we depend upon the underlying tool, except
		// resources we have to do ourselves
		// NOTE: consider doing this for BLTI also
		if (!available) {
		    UIOutput.make(tofill, "hiddenAlert");
		    UIOutput.make(tofill, "hidden-text", messageLocator.getMessage("simplepage.complete_required"));
		    return;
		}
	    }

	    // currently only support 11
	    String helpurl = null; /* (String)toolSession.getAttribute("sakai-portal:help-action"); */
	    String reseturl = null; /* (String)toolSession.getAttribute("sakai-portal:reset-action"); */
	    String skinName = null;
	    String skinRepo = null;
	    String iconBase = null;

	    Placement placement = ToolManager.getCurrentPlacement();
	    String toolId = placement.getToolId();

	    if (helpurl != null || reseturl != null) {

		skinRepo = ServerConfigurationService.getString("skin.repo", "/library/skin");
		iconBase = skinRepo + "/" + CSSUtils.adjustCssSkinFolder(null) + "/images";
		UIVerbatim.make(tofill, "iconstyle", ICONSTYLE.replace("{}", iconBase));

	    }

	    if (helpurl != null) {
		UILink.make(tofill, "helpbutton2", helpurl).
		    decorate(new UIFreeAttributeDecorator("onclick",
					  "openWindow('" + helpurl + "', 'Help', 'resizeable=yes,toolbar=no,scrollbars=yes,menubar=yes,width=800,height=600'); return false")).
		    decorate(new UIFreeAttributeDecorator("title",
				 messageLocator.getMessage("simplepage.help-button")));
		if (!inline)
		UIOutput.make(tofill, "helpimage2").
		    decorate(new UIFreeAttributeDecorator("alt",
				 messageLocator.getMessage("simplepage.help-button")));
		UIOutput.make(tofill, "helpnewwindow2",
		    messageLocator.getMessage("simplepage.opens-in-new"));

		UILink.make(tofill, "directurl").
		    decorate(new UIFreeAttributeDecorator("rel", "#Main" + Web.escapeJavascript(placement.getId()) + "_directurl")).
		    decorate(new UIFreeAttributeDecorator("title", messageLocator.getMessage("simplepage.direct-link")));
		//		if (inline) {
		    UIOutput.make(tofill, "directurl-div").
			decorate(new UIFreeAttributeDecorator("id", "Main" + Web.escapeJavascript(placement.getId()) + "_directurl"));
		    if (ShowPageProducer.getMajorVersion() >= 10) {
			UIOutput.make(tofill, "directurl-input").
			    decorate(new UIFreeAttributeDecorator("onclick", "toggleShortUrlOutput('" + myUrl() + "/portal/directtool/" + placement.getId() + "/', this, 'Main" + Web.escapeJavascript(placement.getId()) + "_urlholder');"));
			UIOutput.make(tofill, "directurl-shorten", messageLocator.getMessage("simplepage.short-url"));
		    }
		    UIOutput.make(tofill, "directurl-textarea", myUrl() + "/portal/directtool/" + placement.getId() + "/").
			decorate(new UIFreeAttributeDecorator("class", "portlet title-tools Main" + Web.escapeJavascript(placement.getId()) + "_urlholder"));
		    //		} else
		    UIOutput.make(tofill, "directimage").decorate(new UIFreeAttributeDecorator("alt",
			messageLocator.getMessage("simplepage.direct-link")));

	    }
	    
	    if (reseturl != null) {
		UIComponent link = UILink.make(tofill, "resetbutton2", reseturl).
		    decorate(new UIFreeAttributeDecorator("title",
			        messageLocator.getMessage("simplepage.reset-button")));
		if (!inline)
		    link.decorate(new UIFreeAttributeDecorator("onclick",
							       "location.href='" + reseturl + "'; return false"));

		if (!inline)
		UIOutput.make(tofill, "resetimage2").
		    decorate(new UIFreeAttributeDecorator("alt",
			        messageLocator.getMessage("simplepage.reset-button")));
	    }

	    if (item != null)
		simplePageBean.adjustBackPath(params.getBackPath(), params.getSendingPage(), item.getId(), item.getName());

	    UIComponent nav = UIOutput.make(tofill, "nav");
	    if (inline)
		nav.decorate(new UIFreeAttributeDecorator("style", "display:none"));

	    String returnView = params.getReturnView();

	    // return to lesson doesn't make sense for resources, since they aren't separate applications in
	    // the same sense. But we do want breadcrumbs.
	    if (sendingPage != -1 && breadcrumbs != null && breadcrumbs.size() > 0) {
		SimplePageBean.PathEntry entry = breadcrumbs.get(breadcrumbs.size()-1);

		if (item != null && item.getType() == SimplePageItem.RESOURCE) {
		    int index = 0;
		    for (SimplePageBean.PathEntry e : breadcrumbs) {
			// don't show current page. We already have a title. This was too much
			UIBranchContainer crumb = UIBranchContainer.make(tofill, "crumb:");
			GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
			view.setSendingPage(e.pageId);
			view.setItemId(e.pageItemId);
			view.setPath(Integer.toString(index));
			UIInternalLink.make(crumb, "crumb-link", e.title, view);
			UIOutput.make(crumb, "crumb-separator");
			if (index == breadcrumbs.size() - 1) {
			    UIBranchContainer finalcrumb = UIBranchContainer.make(tofill, "crumb:");

			    UIOutput.make(finalcrumb, "crumb-follow", item.getName());
			}
			index++;
		    }
		} else {

		    if (returnView == null || returnView.equals("")) {
			GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
			view.setSendingPage(entry.pageId);
			view.setItemId(entry.pageItemId);
			// path defaults to null, which is next
			String currentToolTitle = simplePageBean.getPageTitle();
			String returnText = messageLocator.getMessage("simplepage.return").replace("{}",currentToolTitle); 
			UIInternalLink.make(tofill, "return",  returnText, view);  
			UIOutput.make(tofill, "returnwarning", messageLocator.getMessage("simplepage.return.warning"));  

		    int index = 0;
		    for (SimplePageBean.PathEntry e : breadcrumbs) {
			// don't show current page. We already have a title. This was too much
			UIBranchContainer crumb = UIBranchContainer.make(tofill, "crumb:");
			GeneralViewParameters rview = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
			rview.setSendingPage(e.pageId);
			rview.setItemId(e.pageItemId);
			rview.setPath(Integer.toString(index));
			UIInternalLink.make(crumb, "crumb-link", e.title, rview);
			UIOutput.make(crumb, "crumb-separator");
			if (index == breadcrumbs.size() - 1) {
			    UIBranchContainer finalcrumb = UIBranchContainer.make(tofill, "crumb:");

			    UIOutput.make(finalcrumb, "crumb-follow", item.getName());
			}
			index++;
		    }


		    } else {
			GeneralViewParameters view = new GeneralViewParameters(returnView);
			view.setSendingPage(sendingPage);;
			view.setItemId(new Long(((GeneralViewParameters) params).getId()));
			view.setAddBefore(((GeneralViewParameters) params).getAddBefore());
			UIInternalLink.make(tofill, "return", ((GeneralViewParameters) params).getTitle() , view);
			UIOutput.make(tofill, "returnwarning", messageLocator.getMessage("simplepage.return.warning"));
		    }
		}
	    }

	    // see if we can add a next button
	    if (item != null && (returnView == null || returnView.equals(""))) {
		simplePageBean.addPrevLink(tofill, item);
		simplePageBean.addNextLink(tofill, item);
	    }

	    // future: we have the item. Why not get source from there?
	    // this isn't a security issue, since source is /access/lessonbuilder, and
	    // that will be checked. THat's not the case when the URL is directly present.
	    // in that case we have to get it from the item.
	    String source = params.getSource();
	    if (item == null && (source == null || !source.startsWith("CREATE/"))) {
		UIOutput.make(tofill, "error", messageLocator.getMessage("simplepage.not_available"));
		return;
	    }
	    if(item != null && item.getType() == SimplePageItem.FORUM_SUMMARY) {
		// get messageId, topicId and forumId from the parameters for the source url
		source = myUrl()+ "/portal/tool/" + simplePageBean.getCurrentTool(simplePageBean.FORUMS_TOOL_ID)
			+ "/discussionForum/message/dfViewThreadDirect.jsf?&messageId=" + params.getMessageId()
			+ "&topicId=" + params.getTopicId() + "&forumId=" + params.getForumId();
	    }
	    if (source.startsWith("CREATE/")) {
		if (source.startsWith("CREATE/ASSIGN/")) {
		    List<UrlItem> createLinks = assignmentEntity.createNewUrls(simplePageBean);
		    Integer i = new Integer(source.substring("CREATE/ASSIGN/".length()));
		    source = createLinks.get(i).Url;
		} else if (source.startsWith("CREATE/QUIZ/")) {
		    List<UrlItem> createLinks = quizEntity.createNewUrls(simplePageBean);
		    Integer i = new Integer(source.substring("CREATE/QUIZ/".length()));
		    source = createLinks.get(i).Url;
		} else if (source.startsWith("CREATE/FORUM/")) {
		    List<UrlItem> createLinks = forumEntity.createNewUrls(simplePageBean);
		    Integer i = new Integer(source.substring("CREATE/FORUM/".length()));
		    source = createLinks.get(i).Url;
		}
	    } else if (item.getAttribute("multimediaUrl") != null)
		source = item.getAttribute("multimediaUrl");
	    else switch (item.getType()) {
		case SimplePageItem.RESOURCE:
		    source = item.getItemURL(simplePageBean.getCurrentSiteId(), simplePageBean.getCurrentPage().getOwner());
		    if (lessonBuilderAccessService.needsCopyright(item.getSakaiId()))
			source = "/access/require?ref=" + URLEncoder.encode("/content" + item.getSakaiId()) + "&url=" + URLEncoder.encode(source.substring(7));
		    break;
		case SimplePageItem.CHECKLIST:
		    source = item.getItemURL(simplePageBean.getCurrentSiteId(), simplePageBean.getCurrentPage().getOwner());
		    break;
		case SimplePageItem.FORUM_SUMMARY:
			source = myUrl()+ "/portal/tool/" + simplePageBean.getCurrentTool(simplePageBean.FORUMS_TOOL_ID)
					+"/discussionForum/message/dfViewThreadDirect.jsf?&messageId=" + params.getMessageId()
					+ "&topicId=" + params.getTopicId() + "&forumId=" + params.getForumId();
			break;
		case SimplePageItem.ASSIGNMENT:
		case SimplePageItem.ASSESSMENT:
		case SimplePageItem.FORUM:
		case SimplePageItem.BLTI:
		    LessonEntity lessonEntity = null;
		    switch (item.getType()) {
		    case SimplePageItem.ASSIGNMENT:
			lessonEntity = assignmentEntity.getEntity(item.getSakaiId()); break;
		    case SimplePageItem.ASSESSMENT:
			lessonEntity = quizEntity.getEntity(item.getSakaiId(),simplePageBean); break;
		    case SimplePageItem.FORUM:
			lessonEntity = forumEntity.getEntity(item.getSakaiId()); break;
		    case SimplePageItem.BLTI:
			if (bltiEntity != null)
			    lessonEntity = bltiEntity.getEntity(item.getSakaiId()); break;
		    }
		    if ("EDIT".equals(source))
			source = (lessonEntity==null)?"dummy":lessonEntity.editItemUrl(simplePageBean);
		    else if ("SETTINGS".equals(source))
			source = (lessonEntity==null)?"dummy":lessonEntity.editItemSettingsUrl(simplePageBean);
		    else if ("SETTINGS".equals(source));
		    else
			source = (lessonEntity==null)?"dummy":lessonEntity.getUrl();
		}

	    UIComponent iframe = UILink.make(tofill, "iframe1", source);
	    if (item != null && item.getType() == SimplePageItem.BLTI) {
		String height = item.getHeight();
		if (height == null || height.equals(""))
		    iframe.decorate(new UIFreeAttributeDecorator("height", "1200"));
		else
		    iframe.decorate(new UIFreeAttributeDecorator("height", height));
		iframe.decorate(new UIFreeAttributeDecorator("onload", ""));
	    }
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(SimplePageToolDao s) {
		simplePageToolDao = s;
	}

	public void setHttpServletRequest(HttpServletRequest httpServletRequest) {
		this.httpServletRequest = httpServletRequest;
	}

	public void setLessonBuilderAccessService(LessonBuilderAccessService a) {
	    lessonBuilderAccessService = a;
	}

	public List reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		togo.add(new NavigationCase("failure", new SimpleViewParameters(ShowItemProducer.VIEW_ID)));
		togo.add(new NavigationCase("cancel", new SimpleViewParameters(ShowPageProducer.VIEW_ID)));
		return togo;
	}

	public ViewParameters getViewParameters() {
		return new GeneralViewParameters();
	}
}
