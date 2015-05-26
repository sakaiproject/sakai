/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
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
	//protected IAjaxCallDecorator getAjaxCallDecorator()
	//{
	//	return null;
	//}

	
	/**
	 * Listener method invoked on the AJAX request generated when the user clicks the link
	 * 
	 * @param target
	 */
	public abstract void onClick(AjaxRequestTarget target);
	

}
