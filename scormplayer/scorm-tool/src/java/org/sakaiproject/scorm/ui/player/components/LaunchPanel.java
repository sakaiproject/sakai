package org.sakaiproject.scorm.ui.player.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.ui.player.pages.PlayerPage;

public class LaunchPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	private PlayerPage view;
	private CommunicationPanel communicationPanel;
	private TreePanel treePanel;
	
	
	public LaunchPanel(String id, final SessionBean sessionBean, PlayerPage view) {
		super(id);
		this.view = view;
		
		communicationPanel = new CommunicationPanel("comPanel", sessionBean, this);
		communicationPanel.setOutputMarkupId(true);
		add(communicationPanel);
				
		treePanel = new TreePanel("navPanel", sessionBean, this);
		treePanel.setOutputMarkupId(true);
		add(treePanel);
	}

	public void synchronizeState(SessionBean sessionBean, AjaxRequestTarget target) {		
		if (null != view) {
			view.synchronizeState(sessionBean, target);
		}
	}

	public PlayerPage getView() {
		return view;
	}
	
	public TreePanel getTreePanel() {
		return treePanel;
	}

	public CommunicationPanel getCommunicationPanel() {
		return communicationPanel;
	}	
}
