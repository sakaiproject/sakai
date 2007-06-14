package org.sakaiproject.scorm.client.utils;

import javax.swing.tree.TreeNode;

import org.sakaiproject.scorm.tool.components.ActivityTree;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.tree.LinkTree;
import org.apache.wicket.util.string.Strings;

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
