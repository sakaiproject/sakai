package org.sakaiproject.site.tool.helper.order.rsf;

import java.util.List;

import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;
import org.sakaiproject.tool.api.Tool;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIJointContainer;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

/**
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
public class PageAddProducer implements ViewComponentProducer, ViewParamsReporter {
    
    public static final String VIEW_ID = "PageAdd";

    public SitePageEditHandler handler;
    public MessageLocator messageLocator;

    public String getViewID() {
        return VIEW_ID;
    }

    public void fillComponents(UIContainer arg0, ViewParameters arg1, ComponentChecker arg2) {
        PageAddViewParameters params = null;

        String toolId = null;
        String newTitle = null;
        String mode = null;
        
        UIBranchContainer responseMode = null;
        
        try {
            params = (PageAddViewParameters) arg1;
            toolId = params.toolId;
            mode = params.mode;
            newTitle = params.newTitle;
            
        }
        catch (Exception e) {
            e.printStackTrace();
            responseMode = UIBranchContainer.make(arg0, "mode-failed:");
            UIOutput.make(responseMode, "message", e.getLocalizedMessage());
            return;
        }    
        
        if ("list".equals(mode)) {
            List tools = handler.getAvailableTools();
            responseMode = UIBranchContainer.make(arg0, "mode-pass:");

            UIOutput.make(responseMode, "message", messageLocator.getMessage("add_inst"));
            
            UIBranchContainer toolList = UIBranchContainer.make(responseMode, "tool-list:");
            toolList.decorators = new DecoratorList(new UITooltipDecorator(messageLocator
                    .getMessage("page_dragadd")));

            UIOutput.make(toolList, "option-label", messageLocator.getMessage("avail_pages"));
            
            PageAddViewParameters refreshParam = new PageAddViewParameters();
            refreshParam.mode = "list";
            refreshParam.viewID = PageAddProducer.VIEW_ID;
            UIInternalLink.make(toolList, "refresh-link", refreshParam).decorators =
                    new DecoratorList(new UITooltipDecorator(messageLocator
                    .getMessage("page_refresh_add")));
            

            for (int i = 0; i < tools.size(); i++ ) {
                UIBranchContainer toolrow = UIBranchContainer.make(toolList, "tool-row:", Integer.toString(i));
                Tool tool = (Tool) tools.get(i);
                toolrow.localID = tool.getId();
                
                UIOutput.make(toolrow, "tool-name", tool.getTitle());
                UIOutput.make(toolrow, "tool-id", tool.getId());
                UIOutput.make(toolrow, "tool-description", tool.getDescription());
                
                PageAddViewParameters addParam = new PageAddViewParameters();
                addParam.mode = "add";
                addParam.toolId = tool.getId();
                addParam.newTitle = tool.getTitle();
                addParam.viewID = PageAddProducer.VIEW_ID;
                UIInternalLink.make(toolrow, "tool-add-url", addParam).decorators =
                    new DecoratorList(new UITooltipDecorator(messageLocator
                            .getMessage("page_add")));;
                
            }
        }
        else if ("add".equals(mode)){
            
            SitePage page = handler.addPage(toolId, newTitle);

            if (page != null) {
                UIJointContainer pagerow = new UIJointContainer(arg0, "page-row:","page-row:");
                
                //needed in order for each tag in the list to have a unique id
                pagerow.localID = page.getId();
                
                UIOutput.make(pagerow, "page-name", page.getTitle());
                UIInput.make(pagerow, "page-name-input", null, page.getTitle());
                
                PageEditViewParameters param = new PageEditViewParameters();
                                
                param.pageId = page.getId();
                param.viewID = PageDelProducer.VIEW_ID;
                UIInternalLink.make(pagerow, "del-link", param); 

                param.viewID = PageEditProducer.VIEW_ID;
                UIInternalLink.make(pagerow, "edit-link", param); 

                if (page.getTools().size() == 1) {
                    ToolConfiguration tool = (ToolConfiguration) page.getTools().get(0);
                    if ("sakai.iframe".equals(tool.getToolId())) {
                         
                        UIOutput.make(pagerow, "new-config-label", "URL:");
                        UIInput.make(pagerow, "page-config-input", null, tool
                            .getPlacementConfig().getProperty("source"));
                    }
                }

                if (handler.allowsHide(page)) {
                    param.viewID = PageEditProducer.VIEW_ID;
                    if (handler.isVisible(page)) {
                        param.visible = "false";
                        UIInternalLink.make(pagerow, "hide-link", param).decorators =
                            new DecoratorList(new UITooltipDecorator(messageLocator
                                .getMessage("page_hide")));
                        param.visible = "true";
                        UIInternalLink.make(pagerow, "show-link-off", param).decorators =
                            new DecoratorList(new UITooltipDecorator(messageLocator
                                .getMessage("page_show")));
                    }
                    else {
                        param.visible = "true";
                        UIInternalLink.make(pagerow, "show-link", param).decorators =
                            new DecoratorList(new UITooltipDecorator(messageLocator
                                .getMessage("page_show")));
                     
                        param.visible = "false";
                        UIInternalLink.make(pagerow, "hide-link-off", param).decorators =
                            new DecoratorList(new UITooltipDecorator(messageLocator
                                .getMessage("page_hide")));
                    }
                }

                responseMode = UIBranchContainer.make(arg0, "add-pass:");
                 
                UIOutput.make(responseMode, "message", page.getTitle() + " " + messageLocator
                    .getMessage("success_add"));
                
            }
            else {
                
            }
        }
    }
    
    public ViewParameters getViewParameters() {
        PageAddViewParameters params = new PageAddViewParameters();
        params.mode = "nil";
        params.toolId = "nil";
        params.newTitle = "nil";
        return params;
    }
    
}
