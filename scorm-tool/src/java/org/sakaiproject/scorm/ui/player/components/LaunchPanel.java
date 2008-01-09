package org.sakaiproject.scorm.ui.player.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderContributor;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.ui.UISynchronizer;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;

public class LaunchPanel extends Panel implements IHeaderContributor, UISynchronizer {
	private static final long serialVersionUID = 1L;
	
	protected static final String HEADSCRIPTS = "/library/js/headscripts.js";
	protected static final String BODY_ONLOAD_ADDTL="setMainFrameHeight( 'scormConmtent' )";
	
	private PlayerPage view;
	private CommunicationPanel communicationPanel;
	//private TreePanel treePanel;
	private ActivityTree tree;
	
	@SpringBean
	transient LearningManagementSystem lms;
	@SpringBean
	transient ScormResourceService resourceService;
	@SpringBean
	transient ScormSequencingService sequencingService;
	
	public LaunchPanel(String id, final SessionBean sessionBean, PlayerPage view) {
		super(id);
		this.view = view;
		
		communicationPanel = new CommunicationPanel("comPanel", sessionBean, this);
		communicationPanel.setOutputMarkupId(true);
		add(communicationPanel);
				
		//treePanel = new TreePanel("navPanel", sessionBean, this);
		//treePanel.setOutputMarkupId(true);
		//add(treePanel);
		
		tree = new ActivityTree("tree", sessionBean, this);
		/*{

			private static final long serialVersionUID = 1L;

			@Override
			protected LearningManagementSystem lms() {
				return lms;
			}
			
			@Override
			protected ScormResourceService resourceService() {
				return resourceService;
			}
			
			@Override
			protected ScormSequencingService sequencingService() {
				return sequencingService;
			}
			
		};*/
		tree.setOutputMarkupId(true);
		add(tree);
		
	}

	public void synchronizeState(SessionBean sessionBean, AjaxRequestTarget target) {		
		if (null != view) {
			view.synchronizeState(sessionBean, target);
		}
	}

	public PlayerPage getView() {
		return view;
	}
	
	public ActivityTree getTree() {
		return tree;
	}

	public CommunicationPanel getCommunicationPanel() {
		return communicationPanel;
	}	
	
	public void renderHead(IHeaderResponse response) {
		response.renderJavascriptReference(HEADSCRIPTS);
		response.renderOnLoadJavascript(BODY_ONLOAD_ADDTL);
	}
}
