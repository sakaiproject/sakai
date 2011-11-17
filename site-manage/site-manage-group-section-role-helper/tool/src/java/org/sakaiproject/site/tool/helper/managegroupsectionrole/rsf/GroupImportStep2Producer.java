package org.sakaiproject.site.tool.helper.managegroupsectionrole.rsf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.tool.helper.managegroupsectionrole.impl.ImportedGroup;
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
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UICSSDecorator;
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
 * Producer for page 2 of the group import
 */
public class GroupImportStep2Producer implements ViewComponentProducer, NavigationCaseReporter, ViewParamsReporter, ActionResultInterceptor {

	private static Log M_log = LogFactory.getLog(GroupImportStep2Producer.class);
    public SiteManageGroupSectionRoleHandler handler;
    public static final String VIEW_ID = "GroupImportStep2";
    public MessageLocator messageLocator;
    public FrameAdjustingProducer frameAdjustingProducer;
    public SessionManager sessionManager;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewParams, ComponentChecker checker) {
    	
    	GroupImportViewParameters params = (GroupImportViewParameters) viewParams;
    	
    	UIBranchContainer content = UIBranchContainer.make(tofill, "content:");

        List<ImportedGroup> importedGroups = handler.getImportedGroups();
        
        boolean badData = false;
        
        //get list of groups already in this site
        List<Group> existingGroups = handler.getGroups();
        
        //print each group
        for(ImportedGroup importedGroup: importedGroups) {
            UIBranchContainer branch = UIBranchContainer.make(content,"groups:", importedGroup.getGroupTitle());
            
            boolean groupExists = false;
            Group existingGroup = null;
            
            //add title
            UIOutput.make(branch,"title",importedGroup.getGroupTitle());
            
            //check if group already exists
            for(Group g : existingGroups) {
            	if(StringUtils.equals(g.getTitle(), importedGroup.getGroupTitle())) {
            		existingGroup = g;
            		groupExists = true; 
            		UIOutput.make(branch,"groupexistsmsg:",messageLocator.getMessage("import2.groupexists"));
            	}
            }
            
            //if group already exists, get the users that are already in the group,
            //merge so we get one list, then display the new or merged ones appropriately.
            Set<String> userIds = importedGroup.getUserIds();
            List<String> existingUserIds = new ArrayList<String>();
            if(groupExists) {
            	existingUserIds = handler.getGroupUserIds(existingGroup);
            	userIds.addAll(existingUserIds);
        	}
            
            //print each user
            for(String userId: importedGroup.getUserIds()) {
            	
            	UIOutput output = UIOutput.make(branch,"member:",userId);
            	
            	//check user is valid
            	if(handler.isValidSiteUser(userId)){
            		//is user existing?
            		if(existingUserIds.contains(userId)) {
            			//highlight grey
            			Map<String,String> cssMap = new HashMap<String,String>();
                		cssMap.put("color","grey");
                		output.decorate(new UICSSDecorator(cssMap));
            		}
            		
            	} else {
            		badData = true;
            		//highlight red
            		Map<String,String> cssMap = new HashMap<String,String>();
            		cssMap.put("color","red");
            		output.decorate(new UICSSDecorator(cssMap));
            	}
            }
        }
        
        UIForm createForm = UIForm.make(content, "form");
        
        if(badData) {
            UIMessage.make(content, "import2.error", "import2.error");
            handler.resetParams();
            
            UICommand.make(createForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processCancel}");
            
        } else {
            UIMessage.make(content, "import2.okay", "import2.okay");

            UICommand.make(createForm, "continue", messageLocator.getMessage("import2.continue"), "#{SiteManageGroupSectionRoleHandler.processImportedGroups}");
            UICommand.make(createForm, "cancel", messageLocator.getMessage("cancel"), "#{SiteManageGroupSectionRoleHandler.processCancel}");
        }
        
        if(StringUtils.equals(params.status, "error")){
        	UIMessage.make(content, "import2.couldntimportgroups", "import2.couldntimportgroups");
        }
       
        frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
    }

    public List<NavigationCase> reportNavigationCases() {
        List<NavigationCase> togo = new ArrayList<NavigationCase>();
        togo.add(new NavigationCase("success", new SimpleViewParameters(GroupListProducer.VIEW_ID)));
        togo.add(new NavigationCase("error", new GroupImportViewParameters(GroupImportStep2Producer.VIEW_ID, "error")));
        return togo;
    }

    public void interceptActionResult(ARIResult result, ViewParameters incoming, Object actionReturn) {
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
