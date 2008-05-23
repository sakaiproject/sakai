package org.sakaiproject.site.tool.helper.managegroup.rsf;

import java.net.URLDecoder;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.managegroup.impl.SiteManageGroupHandler;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.decorators.UILabelTargetDecorator;
import uk.org.ponder.rsf.evolvers.TextInputEvolver;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
import uk.org.ponder.stringutil.StringList;

/**
 * 
 * @author
 *
 */
public class GroupEditProducer implements ViewComponentProducer, ViewParamsReporter {

    public SiteManageGroupHandler handler;
    public static final String VIEW_ID = "GroupEdit";
    public MessageLocator messageLocator;
    
    private TextInputEvolver richTextEvolver;
	public void setRichTextEvolver(TextInputEvolver richTextEvolver) {
				this.richTextEvolver = richTextEvolver;
	}

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
    	
    	 UIForm toolsForm = UIForm.make(arg0, "groups-form");

        // List tools = handler.getAvailableTools();

         StringList toolItems = new StringList();
         
         UISelect toolSelect = UISelect.makeMultiple(toolsForm, "select-tools",
                       null, "#{SitePageEditHandler.selectedTools}", new String[] {});

         UIOutput.make(toolsForm, "prompt", messageLocator.getMessage("group.newgroup"));
         UIOutput.make(toolsForm, "instructions", messageLocator.getMessage("editgroup.instruction"));
         
         UIMessage titleTextLabel = UIMessage.make(arg0, "group_title_label", "group.title");
         UIInput titleTextIn = UIInput.make(toolsForm, "group_title", "","");
		 UILabelTargetDecorator.targetLabel(titleTextLabel, titleTextIn);
		 

		 UIMessage groupDescrLabel = UIMessage.make(arg0, "group_descr_label", "group.description"); 
		 UIInput groupDescr = UIInput.make(toolsForm, "group_description", "", ""); 
		 richTextEvolver.evolveTextInput(groupDescr);
		 UILabelTargetDecorator.targetLabel(groupDescrLabel, groupDescr);
            
       /*  for (int i = 0; i < tools.size(); i++ ) {
             UIBranchContainer toolRow = UIBranchContainer.make(toolsForm, "tool-row:", Integer.toString(i));

             Tool tool = (Tool) tools.get(i);
             
             UIOutput.make(toolRow, "tool-name", tool.getTitle());
             UIOutput.make(toolRow, "tool-id", tool.getId());
             UIOutput.make(toolRow, "tool-description", tool.getDescription());  
             UISelectChoice.make(toolRow, "tool-select", toolSelect.getFullID(), i);
             
             toolItems.add(tool.getId());
         }*/
         
         toolSelect.optionlist.setValue(toolItems.toStringArray());
         
         UICommand.make(toolsForm, "save", messageLocator.getMessage("editgroup.update"), 
                        "#{SitePageEditHandler.addTools}");

         UICommand.make(toolsForm, "cancel", messageLocator.getMessage("editgroup.cancel"), 
                        "#{SitePageEditHandler.back}");
    }
    
    public ViewParameters getViewParameters() {
        GroupEditViewParameters params = new GroupEditViewParameters();

        //Bet you can't guess what my first language was? ;-)
        params.groupId = "nil";
        params.newTitle = "nil";
        params.newConfig = "nil";
        return params;
    }

}
