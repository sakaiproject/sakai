package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxCallDecorator;
import org.apache.wicket.ajax.calldecorator.CancelEventIfNoAjaxDecorator;
import org.apache.wicket.ajax.markup.html.IAjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.link.ExternalLink;

/**
 * This is a simple link component for rendering external links but also provides an AjaxRequestTarget.
 * <p>
 * The url will be set into the href attribute of the tag.
 * If you need the link to open in a new window, use jQuery.
 * <code>
 * &ltscript type="text/javascript"&gt;
 *		$(document).ready(function(){
 *			$(function(){
 *			    $('a.new-window').click(function(){
 *			        window.open(this.href);
 *			        return false;
 *			    });
 *			});
 *		});
 *	&lt/script&gt;
 * </code>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public abstract class AjaxExternalLink<T> extends ExternalLink implements IAjaxLink {

	private static final long serialVersionUID = 1L;
	
	public AjaxExternalLink(String id) {
		this(id,null);
	}
	
	public AjaxExternalLink(String id, String url) {
		super(id, url);
		
		add(new AjaxEventBehavior("onclick")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
				onClick(target);
			}

			@Override
			protected IAjaxCallDecorator getAjaxCallDecorator()
			{
				return new CancelEventIfNoAjaxDecorator(AjaxExternalLink.this.getAjaxCallDecorator());
			}

			@Override
			protected void onComponentTag(ComponentTag tag)
			{
				// add the onclick handler only if link is enabled
				if (isLinkEnabled())
				{
					super.onComponentTag(tag);
				}
			}
		});
	}
	
	
	/**
	 * Returns ajax call decorator that will be used to decorate the ajax call.
	 * 
	 * @return ajax call decorator
	 */
	protected IAjaxCallDecorator getAjaxCallDecorator()
	{
		return null;
	}

	
	/**
	 * Listener method invoked on the AJAX request generated when the user clicks the link
	 * 
	 * @param target
	 */
	public abstract void onClick(AjaxRequestTarget target);
	

}
