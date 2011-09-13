/**
 * 
 */
package org.sakaiproject.tool.resetpass;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.tool.api.Placement;
import org.sakaiproject.tool.api.ToolManager;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessageList;
import uk.org.ponder.rsf.components.UIBranchContainer;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;


/**
 * @author dhorwitz
 *
 */
public class FormProducer implements ViewComponentProducer, DefaultView,NavigationCaseReporter {

	public static final String VIEW_ID = "form";
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ViewComponentProducer#getViewID()
	 */
	public String getViewID() {
		return VIEW_ID;
	}

	MessageLocator messageLocator;
	public void setMessageLocator(MessageLocator ml) {
		messageLocator = ml;
	}
	
	private ServerConfigurationService serverConfigurationService;
	public void setServerConfigurationService(ServerConfigurationService s) {
		this.serverConfigurationService = s;
	}
	
	private TargettedMessageList tml;
	  
	  public void setTargettedMessageList(TargettedMessageList tml) {
		    this.tml = tml;
	  }
  
	private ToolManager toolManager;
	public void setToolManager(ToolManager toolManager) {
		this.toolManager = toolManager;
	}
	
	/* (non-Javadoc)
	 * @see uk.org.ponder.rsf.view.ComponentProducer#fillComponents(uk.org.ponder.rsf.components.UIContainer, uk.org.ponder.rsf.viewstate.ViewParameters, uk.org.ponder.rsf.view.ComponentChecker)
	 */
	public void fillComponents(UIContainer tofill, ViewParameters viewParms,
			ComponentChecker comp) {
		// TODO Auto-generated method stub

		
		if (tml!=null) {
			if (tml.size() > 0) {

		    	for (int i = 0; i < tml.size(); i ++ ) {
		    		UIBranchContainer errorRow = UIBranchContainer.make(tofill,"error-row:");
		    		if (tml.messageAt(i).args != null ) {	    		
		    			UIVerbatim.make(errorRow, "error", messageLocator.getMessage(tml.messageAt(i).acquireMessageCode(), (String[])tml.messageAt(i).args[0]));
		    		} else {
		    			UIVerbatim.make(errorRow, "error", messageLocator.getMessage(tml.messageAt(i).acquireMessageCode()));
		    		}
		    		
		    	}
		    }
		}
		// Get the instructions from the tool placement.
		Placement placement = toolManager.getCurrentPlacement();
		if (placement != null) {
			String instuctions = placement.getConfig().getProperty("instructions");
			if (instuctions != null && instuctions.length() > 0) {
				UIVerbatim.make(tofill, "instructions", instuctions);
			}
		} else {
			String[] args = new String[1];
			args[0]=serverConfigurationService.getString("ui.service", "Sakai Based Service");
			UIVerbatim.make(tofill,"main",messageLocator.getMessage("mainText", args));
		}
		UIForm form = UIForm.make(tofill,"form");
		UIInput.make(form,"input","#{userBean.email}");
		UICommand.make(form,"submit",UIMessage.make("postForm"),"#{formHandler.processAction}");
	}

	
	  public List<NavigationCase> reportNavigationCases() {
		    List<NavigationCase> togo = new ArrayList<NavigationCase>(); // Always navigate back to this view.
		    togo.add(new NavigationCase(null, new SimpleViewParameters(VIEW_ID)));
		    togo.add(new NavigationCase("Success", new SimpleViewParameters(ConfirmProducer.VIEW_ID)));
		    return togo;
	  }
	  
}
