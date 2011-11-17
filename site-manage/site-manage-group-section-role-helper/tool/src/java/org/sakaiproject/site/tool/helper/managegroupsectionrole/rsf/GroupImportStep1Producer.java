package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.SiteManageGroupSectionRoleHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
   * Producer for page 1 of the group import
   */
public class GroupImportStep1Producer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {
    
	private static Log M_log = LogFactory.getLog(GroupImportStep1Producer.class);
	public SiteManageGroupSectionRoleHandler handler;
	public static final String VIEW_ID = "GroupImportStep1";
	public MessageLocator messageLocator;
	public FrameAdjustingProducer frameAdjustingProducer;
	public SessionManager sessionManager;
	
	public String getViewID() {
		return VIEW_ID;
	}
	
	public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {
	    	
		GroupImportViewParameters params = (GroupImportViewParameters) viewParams;
	    	
		UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
		UIVerbatim.make(content, "import1.instr.req.1", messageLocator.getMessage("import1.instr.req.1"));
		UIVerbatim.make(content, "import1.instr.req.2", messageLocator.getMessage("import1.instr.req.2"));
		UIVerbatim.make(content, "import1.instr.req.3", messageLocator.getMessage("import1.instr.req.3"));
		UIForm uploadForm = UIForm.make(content, "uploadform");
		UICommand.make(uploadForm, "continue", messageLocator.getMessage("import1.continue"), "#{SiteManageGroupSectionRoleHandler.processUploadAndCheck}");
		UICommand.make(uploadForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processCancel}");
	    
		if(StringUtils.equals(params.status, "error")){
			UIMessage.make(content, "import1.error", "import1.error");
			handler.resetParams();
		}
		
		frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
		    
	}
	
	public List<NavigationCase> reportNavigationCases() {
		List<NavigationCase> togo = new ArrayList<NavigationCase>();
		togo.add(new NavigationCase("success", new SimpleViewParameters(GroupImportStep2Producer.VIEW_ID)));
		togo.add(new NavigationCase("error", new GroupImportViewParameters(GroupImportStep1Producer.VIEW_ID, "error")));
		return togo;
	}
	
	
	public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
		//from processCancel
		if ("done".equals(actionReturn)) {
			handler.resetParams();
			Tool tool = handler.getCurrentTool();
			result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
		}
	}
	
	public ViewParameters getViewParameters() {
		return new GroupImportViewParameters();
	}
}