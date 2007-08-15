package org.sakaiproject.scorm.tool.components;

import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.scorm.tool.RunState;

import org.apache.wicket.ajax.AjaxRequestTarget;

public class TreePanel extends Panel {

	private ActivityTree tree;
	private LaunchPanel launchPanel;
	
	public TreePanel(String id, RunState runState, LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		
		tree = new ActivityTree("tree", runState, this);
		tree.setOutputMarkupId(true);
		add(tree);
	}
	
	public void setTreeVisible(boolean isVisible, AjaxRequestTarget target) {
		if (null != tree && tree.isVisible() != isVisible) {
			tree.setVisible(isVisible);
		
			if (target != null)
				target.addComponent(this);
		}
	}

	public LaunchPanel getLaunchPanel() {
		return launchPanel;
	}
	
	public ActivityTree getActivityTree() {
		return tree;
	}
	
}
