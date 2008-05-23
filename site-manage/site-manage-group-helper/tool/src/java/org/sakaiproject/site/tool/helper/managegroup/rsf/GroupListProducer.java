package org.sakaiproject.site.tool.helper.managegroup.rsf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.managegroup.impl.SiteManageGroupHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.flow.jsfnav.DynamicNavigationCaseReporter;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author
 *
 */
public class GroupListProducer 
        implements ViewComponentProducer, DynamicNavigationCaseReporter, DefaultView {
    
    public static final String VIEW_ID = "GroupList";
    public Map siteGroups;
    public SiteManageGroupHandler handler;
    public MessageLocator messageLocator;
    public SessionManager sessionManager;
    public FrameAdjustingProducer frameAdjustingProducer;
    
    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {

        if (handler.update) {
            GroupEditViewParameters addParam = new GroupEditViewParameters();
            addParam.viewID = GroupEditProducer.VIEW_ID;
            UIInternalLink.make(tofill, "add-link", messageLocator.getMessage("group.newgroup"), addParam).decorators =
                new DecoratorList(new UITooltipDecorator(messageLocator
                      .getMessage("hint.newgroup")));
            
            UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
            
            UIOutput.make(content, "list-label", messageLocator.getMessage("group.list"));
            
            UIForm groupForm = UIForm.make(content, "groups-form");
            String state = "";
 
            frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
            
            siteGroups = handler.getGroups();
            for (Iterator it=siteGroups.keySet().iterator(); it.hasNext(); ) {
                Object key = it.next();
                Group group = (Group) siteGroups.get(key);
                UIBranchContainer grouprow = 
                    UIBranchContainer.make(groupForm, "group-row:", group.getId());
                
                UIOutput.make(grouprow, "group-name", group.getTitle());
                UIInput name = 
                    UIInput.make(grouprow, "group-name-input", "#{SitegroupEditHandler.nil}", group.getTitle());
                UIOutput nameLabel = 
                    UIOutput.make(grouprow, "group-name-label", messageLocator.getMessage("title"));
                
                nameLabel.decorate(new UILabelTargetDecorator(name));
            }

 
        }
        else {
            //error messages
            UIBranchContainer error = UIBranchContainer.make(tofill, "error:");
            UIOutput.make(error, "message", messageLocator
                    .getMessage("access_error"));
        }
    }

    public List reportNavigationCases() {
        Tool tool = handler.getCurrentTool();
        List togo = new ArrayList();
        togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
        togo.add(new NavigationCase("done", 
                 new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager))));
        

        return togo;
    }
}
