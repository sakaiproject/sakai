package org.sakaiproject.scorm.client.utils;

import javax.swing.tree.TreeNode;

import org.sakaiproject.scorm.tool.panels.ActivityTree;

import wicket.MarkupContainer;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import wicket.ajax.markup.html.AjaxFallbackLink;
import wicket.ajax.markup.html.AjaxLink;
import wicket.ajax.markup.html.IAjaxLink;
import wicket.markup.ComponentTag;
import wicket.markup.html.link.ExternalLink;
import wicket.markup.html.link.Link;
import wicket.markup.html.tree.Tree;
import wicket.util.string.Strings;

public class ActivityLink extends ExternalLink implements IAjaxLink {
	private static final long serialVersionUID = 1L;

	public ActivityLink(String id, String href, final IActivityLinkCallback callback) {
		super(id, href);
		
		add(new AjaxEventBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			protected void onEvent(AjaxRequestTarget target)
			{
				callback.onClick(target);
			}

			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new ActivityCallDecorator(ActivityLink.this.getAjaxCallDecorator());
			}

		});
	}
	
	
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	// Does this even get called?
	public void onClick(AjaxRequestTarget target) {
		System.out.println("Calling onClick...");
	}
	
	//protected abstract void onNodeLinkClicked(AjaxRequestTarget target, TreeNode node);
}
