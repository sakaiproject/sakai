package org.sakaiproject.site.tool.helper.order.rsf;

import java.util.Iterator;
import java.util.Map;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;
import org.sakaiproject.site.tool.helper.order.impl.SitePageEditHandler;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.tool.api.Tool;

import uk.ac.cam.caret.sakai.rsf.producers.FrameAdjustingProducer;
import uk.ac.cam.caret.sakai.rsf.util.SakaiURLUtil;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundString;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIComponent;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIInternalLink;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.decorators.UITooltipDecorator;
import uk.org.ponder.rsf.components.decorators.UIAlternativeTextDecorator;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;

/**
 * 
 * @author Joshua Ryan joshua.ryan@asu.edu
 *
 */
public class PageListProducer 
        implements ViewComponentProducer, DefaultView,
        ActionResultInterceptor {
    
    public static final String VIEW_ID = "PageList";
    public Map<String, SitePage> sitePages;
    public SitePageEditHandler handler;
    public MessageLocator messageLocator;
    public SessionManager sessionManager;
    public FrameAdjustingProducer frameAdjustingProducer;
    public ServerConfigurationService serverConfigurationService;
    public String ALLOW_TITLE_EDIT = "org.sakaiproject.site.tool.helper.order.rsf.PageListProducer.allowTitleEdit";
    
    public String getViewID() {
        return VIEW_ID;
    }

    private UIComponent fullyDecorate(UIComponent todecorate, UIBoundString text) {
      return todecorate.decorate(
          new UIAlternativeTextDecorator(text)).decorate(new UITooltipDecorator(text));
    }
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams,
            ComponentChecker checker) {

        if (handler.update) {
            PageAddViewParameters addParam = new PageAddViewParameters();
            addParam.viewID = PageAddProducer.VIEW_ID;

            fullyDecorate(UIInternalLink.make(tofill, "add-link", UIMessage.make("show_add"), addParam), 
                UIMessage.make("page_show_add"));
            UIBranchContainer content = UIBranchContainer.make(tofill, "content:");
            
            UIForm pageForm = UIForm.make(content, "pages-form");
            String state = "";
            
            sitePages = handler.getPages();
    
            for (Iterator<String> it = sitePages.keySet().iterator(); it.hasNext(); ) {
                String key = it.next();
                SitePage page = sitePages.get(key);
                UIBranchContainer pagerow = 
                    UIBranchContainer.make(pageForm, "page-row:", page.getId());
    
                pagerow.decorate(new UITooltipDecorator(UIMessage.make("page_click_n_drag")));
                
                UIOutput.make(pagerow, "page-name", page.getTitle());
                UIInput.make(pagerow, "page-name-input", "#{SitePageEditHandler.nil}", page.getTitle());
                UIMessage.make(pagerow, "page-name-label", "title");
                
                //nameLabel.decorate(new UILabelTargetDecorator(name));
                
                PageEditViewParameters param = new PageEditViewParameters();
                                
                param.pageId = page.getId();

                param.viewID = PageEditProducer.VIEW_ID;
                Object[] pageTitle = new Object[] {page.getTitle()};

                //default value is to allow the Title to be edited.  If the sakai properties 
                //specifically requests this to be set to false, then do not allow this function
                if (serverConfigurationService.getBoolean(ALLOW_TITLE_EDIT, true)) {

                    fullyDecorate(UIInternalLink.make(pagerow, "edit-link", param), 
                        UIMessage.make("page_edit", pageTitle));

                    fullyDecorate(UIInternalLink.make(pagerow, "save-edit-link", param),
                        UIMessage.make("save_page_edit", pageTitle));

                    fullyDecorate(UIOutput.make(pagerow, "cancel-edit-link"),
                        UIMessage.make("cancel_page_edit", pageTitle));
                }
                
                if (page.getTools().size() == 1) {
                    ToolConfiguration tool = (ToolConfiguration) page.getTools().get(0);

                    //if the page only has one tool and it's not site info or required we allow the user to delete it here
                    //TODO: can we get the fact that site info called us
                    if (!handler.isRequired(tool.getToolId()) && !"sakai.sitesetup".equals(tool.getToolId())
                            && !"sakai.siteinfo".equals(tool.getToolId())) {

                        param.viewID = PageDelProducer.VIEW_ID;
                        fullyDecorate(UIInternalLink.make(pagerow, "del-link", param),
                            UIMessage.make("page_remove", pageTitle));
                    }
                  
                    // allow special configuration for the iframe tool. This needs to be generalized
                    //for all tools that want special configuration and/or allow multiple instances 
                    //per site
                    if ("sakai.iframe".equals(tool.getToolId())) {
                        UIInput.make(pagerow, "page-config-input", "#{SitePageEditHandler.nil}", 
                                tool.getPlacementConfig().getProperty("source"));
                    }
                }

                if (handler.allowsHide(page)) {
                    param.viewID = PageEditProducer.VIEW_ID;
                    if (handler.isVisible(page)) {
                        param.visible = "false";
                        fullyDecorate(UIInternalLink.make(pagerow, "hide-link", param),
                            UIMessage.make("page_hide", pageTitle));
                        
                        param.visible = "true";
                        fullyDecorate(UIInternalLink.make(pagerow, "show-link-off", param),
                            UIMessage.make("page_show", pageTitle));
                    }
                    else {
                        param.visible = "true";
                        fullyDecorate(UIInternalLink.make(pagerow, "show-link", param),
                            UIMessage.make("page_show", pageTitle));
                     
                        param.visible = "false";
                        fullyDecorate(UIInternalLink.make(pagerow, "hide-link-off", param),
                            UIMessage.make("page_hide", pageTitle));
                    }
                }
                
                state += page.getId() + " ";
            }

            UIInput.make(pageForm, "state-init", "#{SitePageEditHandler.state}", state);
            fullyDecorate(UICommand.make(pageForm, "save", UIMessage.make("save"), "#{SitePageEditHandler.savePages}"),
            		UIMessage.make("save_message"));

            fullyDecorate(UICommand.make(pageForm, "revert", UIMessage.make("cancel"), "#{SitePageEditHandler.cancel}"),
                UIMessage.make("cancel_message"));

            if (handler.isSiteOrdered()) {
                fullyDecorate(UICommand.make(pageForm, "reset", UIMessage.make("reset"), "#{SitePageEditHandler.reset}"),
                    UIMessage.make("reset_message"));
            }
 
            frameAdjustingProducer.fillComponents(tofill, "resize", "resetFrame");
 
        }
        else {
            //error messages - apparently nothing in template for these
            //UIBranchContainer error = UIBranchContainer.make(tofill, "error:");
            //UIMessage.make(error, "message", "access_error");
        }
    }

    public ServerConfigurationService getServerConfigurationService() {
        return serverConfigurationService;
    }

    public void setServerConfigurationService(
        ServerConfigurationService serverConfigurationService) {
            this.serverConfigurationService = serverConfigurationService;
    }

    public void interceptActionResult(ARIResult result, ViewParameters incoming,
        Object actionReturn) {
        if ("done".equals(actionReturn)) {
          Tool tool = handler.getCurrentTool();
           result.resultingView = new RawViewParameters(SakaiURLUtil.getHelperDoneURL(tool, sessionManager));
        }
    }
}
