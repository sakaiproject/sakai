/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.ui.player.behaviors;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.behavior.IBehaviorListener;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.sakaiproject.scorm.ui.player.util.Utils;

public abstract class ActivityAjaxEventBehavior extends AjaxEventBehavior {

	private static final long serialVersionUID = 1L;
	
	final private boolean isRelativeUrl;
		
	public ActivityAjaxEventBehavior(String event, boolean isRelativeUrl) {
		super(event);
		this.isRelativeUrl = isRelativeUrl;
	}
	
	public String getCall() {
		return getCallbackScript(false).toString();
	}
	
	protected CharSequence getCallbackScript(boolean onlyTargetActivePage)
	{
		return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "'");
	}
	
	public CharSequence getCallbackUrl()
	{
		/*if (getComponent() == null)
		{
			throw new IllegalArgumentException(
					"Behavior must be bound to a component to create the URL");
		}
		
		final RequestListenerInterface rli;
		
		rli = IBehaviorListener.INTERFACE;
		
		WebRequest webRequest = (WebRequest)getComponent().getRequest();
		HttpServletRequest servletRequest = webRequest.getHttpServletRequest();

		String toolUrl = servletRequest.getContextPath();
		
		AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(toolUrl).append("/");
		url.append("scormplayer").append("/");
		url.append(getComponent().urlFor(this, rli));*/

		return Utils.generateUrl(this, null, getComponent(), isRelativeUrl);
	}

}
