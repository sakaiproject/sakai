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

import org.sakaiproject.assignment.cover.AssignmentService;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.lessonbuildertool.SimplePage;
import org.sakaiproject.lessonbuildertool.SimplePageItem;
import org.sakaiproject.lessonbuildertool.model.SimplePageToolDao;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean;
import org.sakaiproject.lessonbuildertool.tool.beans.SimplePageBean.Status;
import org.sakaiproject.lessonbuildertool.tool.view.FilePickerViewParameters;
import org.sakaiproject.lessonbuildertool.tool.view.GeneralViewParameters;
import org.sakaiproject.lessonbuildertool.tool.producers.PermissionsHelperProducer;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lessonbuildertool.service.LessonEntity;

import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.api.ToolSession;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.cover.SiteService;

import uk.org.ponder.messageutil.MessageLocator;
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
	public static final String VIEW_ID = "ShowItem";

	public String getViewID() {
		return VIEW_ID;
	}
    
	public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {

	    // to do assignment/quiz, etc arguments are
	    //   sendingpage, itemid - reflect the item that the user clicked
	    //   source - URL to call
	    //   clearattr may be used for Samigo hack
	    //   return is to sendingpage, with path coming from current path retrieved from SimplePageBean
	    // to create a new assignment, quiz, etc
	    //   path non-null is what triggers this
	    //   sendingpage, itemid - these are arguments that we'll use to return to the "add assignment" page
	    //   source - URL to call
	    //   clearattr may be used for Samigo hack
	    //   returnview - viewID to return to
	    //   title - the string for the return button

	    // as far as I can see there are no permissions issues here. It just
	    // sticks things in an iframe. The stuff it sticks had better check though

	    GeneralViewParameters params = (GeneralViewParameters)viewParams;

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
		    String toolUrl = "/portal/site/" + sitePage.getSiteId() + "/page/" + sitePage.getId() + "?clearAttr=" + clearAttr;
		    session.setAttribute(clearAttr, toolUrl);
		}
	    }

	    String pathOp = params.getPath();
	    // only pop is valid; we don't have the data for the other options
	    if (pathOp != null && !pathOp.equals(""))
		simplePageBean.adjustPath(pathOp, params.getSendingPage(), null, null);

	    List<SimplePageBean.PathEntry> breadcrumbs = simplePageBean.getHierarchy();
	    SimplePageItem item = simplePageBean.findItem (params.getItemId());

	    if (item != null)
		simplePageBean.adjustBackPath(params.getBackPath(), params.getSendingPage(), item.getId(), item.getName());

	    // this is a "next" page where we couldn't tell if the item is
	    // available. Need to check here in order to set ACLs. If not available,
	    // return to calling page
	    if (item != null && "true".equals(params.getRecheck())) {
		if (simplePageBean.isItemAvailable(item, item.getPageId())) {
		    if (item.isPrerequisite()) {
			simplePageBean.checkItemPermissions(item, true); // set acl, etc
		    }
		} else {
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
	    }
	    if (sendingPage != -1 && breadcrumbs != null && breadcrumbs.size() > 0) {
		SimplePageBean.PathEntry entry = breadcrumbs.get(breadcrumbs.size()-1);

		String returnView = params.getReturnView();
		if (returnView == null || returnView.equals("")) {
		    GeneralViewParameters view = new GeneralViewParameters(ShowPageProducer.VIEW_ID);
		    view.setSendingPage(entry.pageId);
		    view.setItemId(entry.pageItemId);
		    // path defaults to null, which is next
		    UIInternalLink.make(tofill, "return", messageLocator.getMessage("simplepage.return"), view);
		} else {
		    GeneralViewParameters view = new GeneralViewParameters(returnView);
		    view.setSendingPage(sendingPage);;
		    view.setItemId(((GeneralViewParameters) params).getItemId());
		    UIInternalLink.make(tofill, "return", ((GeneralViewParameters) params).getTitle() , view);
		}
	    }

	    // see if we can add a next button
	    if (item != null) {
		simplePageBean.addPrevLink(tofill, item);
		simplePageBean.addNextLink(tofill, item);
	    }

	    UILink.make(tofill, "iframe1", params.getSource());
	}

	public void setSimplePageBean(SimplePageBean simplePageBean) {
		this.simplePageBean = simplePageBean;
	}

	public void setSimplePageToolDao(SimplePageToolDao s) {
		simplePageToolDao = s;
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
