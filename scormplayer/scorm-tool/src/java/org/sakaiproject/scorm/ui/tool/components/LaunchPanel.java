package org.sakaiproject.scorm.ui.tool.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.sakaiproject.scorm.client.api.ScormClientFacade;
import org.sakaiproject.scorm.ui.tool.RunState;
import org.sakaiproject.scorm.ui.tool.pages.View;

public class LaunchPanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	@SpringBean
	ScormClientFacade clientFacade;
	
	private View view;
	private CommunicationPanel communicationPanel;
	private TreePanel treePanel;
	
	
	public LaunchPanel(String id, final RunState runState, View view) {
		super(id);
		this.view = view;
		
		communicationPanel = new CommunicationPanel("comPanel", runState, this);
		communicationPanel.setOutputMarkupId(true);
		add(communicationPanel);
				
		treePanel = new TreePanel("navPanel", runState, this);
		treePanel.setOutputMarkupId(true);
		add(treePanel);
	}
		
	
	
	public void synchronizeState(RunState runState, AjaxRequestTarget target) {
		
		if (null != treePanel && null != treePanel.getActivityTree() && null != runState 
				&& treePanel.getActivityTree().isVisible() != runState.isTreeVisible()) {
			treePanel.setTreeVisible(runState.isTreeVisible(), target);
		}
		
		if (null != view) {
			view.synchronizeState(runState, target);
		}
	}

	public View getView() {
		return view;
	}
	
	public TreePanel getTreePanel() {
		return treePanel;
	}

	public CommunicationPanel getCommunicationPanel() {
		return communicationPanel;
	}	
}
