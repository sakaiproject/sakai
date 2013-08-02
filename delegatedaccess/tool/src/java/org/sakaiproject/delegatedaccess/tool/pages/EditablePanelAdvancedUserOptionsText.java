package org.sakaiproject.delegatedaccess.tool.pages;

import java.util.Map;

import javax.swing.tree.TreeNode;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.sakaiproject.delegatedaccess.model.NodeModel;

public class EditablePanelAdvancedUserOptionsText extends Panel{

	private boolean loadedFlag = false;
	
	public EditablePanelAdvancedUserOptionsText(String id, IModel inputModel, final NodeModel nodeModel, final TreeNode node, Map<String, Object> settings) {
		super(id);
		
		final WebMarkupContainer inheritedSpan = new WebMarkupContainer("inheritedSpan");
		inheritedSpan.setOutputMarkupId(true);
		final String inheritedSpanId = inheritedSpan.getMarkupId();
		add(inheritedSpan);
		
		
		AjaxLink<Void> inheritedToolsLink = new AjaxLink<Void>("inheritedToolsLink"){
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='';");
			}
		};
		
		add(inheritedToolsLink);
		
		AjaxLink<Void> closeInheritedSpanLink = new AjaxLink<Void>("closeInheritedSpanLink") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick(AjaxRequestTarget target) {
				target.appendJavascript("document.getElementById('" + inheritedSpanId + "').style.display='none';");
			}
		};
		inheritedSpan.add(closeInheritedSpanLink);

		Label inheritedNodeTitle = new Label("inheritedNodeTitle", nodeModel.getNode().title);
		inheritedSpan.add(inheritedNodeTitle);
		
		final boolean allowBecomeUser = nodeModel.getNodeAllowBecomeUser();
		
		Label allowBecomeUserLabel = new Label("allowBecomeUserTitle", new StringResourceModel("allowedBeomceUser", null)){
			@Override
			public boolean isVisible() {
				return nodeModel.getNodeAllowBecomeUser();
			}
		};
		inheritedSpan.add(allowBecomeUserLabel);
	}
	
	public boolean isLoadedFlag() {
		return loadedFlag;
	}
	
}
