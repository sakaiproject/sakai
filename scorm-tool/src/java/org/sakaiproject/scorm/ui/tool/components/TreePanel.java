package org.sakaiproject.scorm.ui.tool.components;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.sakaiproject.scorm.ui.tool.RunState;

public class TreePanel extends Panel {
	private static final long serialVersionUID = 1L;
	
	private ActivityTree tree;
	private LaunchPanel launchPanel;
		
	public TreePanel(String id, final RunState runState, LaunchPanel launchPanel) {
		super(id);
		this.launchPanel = launchPanel;
		
		/*add(new LazyLoadPanel("tree"){
			private static final long serialVersionUID = 1L;

			@Override
            public Component getLazyLoadComponent(String lazyId, AjaxRequestTarget target)
            {
				tree = new ActivityTree(lazyId, runState, TreePanel.this);
				tree.setOutputMarkupId(true);
				runState.displayContent(target);
                return tree;
            }
			
        });*/
		
		tree = new ActivityTree("tree", runState, this);
		tree.setOutputMarkupId(true);
		add(tree);
	}
	
	public void setTreeVisible(boolean isVisible, AjaxRequestTarget target) {
		if (null != tree && tree.isVisible() != isVisible) {
			tree.setVisible(true);
		
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
