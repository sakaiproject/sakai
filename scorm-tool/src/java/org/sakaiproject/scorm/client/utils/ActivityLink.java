package org.sakaiproject.scorm.client.utils;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.html.link.ExternalLink;

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
