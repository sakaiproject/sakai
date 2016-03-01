/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2016- Charles R. Severance
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.basiclti.util;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.basiclti.util.SakaiBLTIUtil;
import org.sakaiproject.basiclti.util.SakaiLTIProviderUtil;

import org.tsugi.contentitem.objects.Icon;
import org.tsugi.contentitem.objects.PlacementAdvice;
import org.tsugi.contentitem.objects.LtiLinkItem;
import org.tsugi.contentitem.objects.ContentItemResponse;

import org.sakaiproject.tool.api.Tool;
import org.sakaiproject.tool.cover.ToolManager;

/**
 * Some Sakai Utility code for IMS ContentItem
 * This is mostly code to support the Sakai conventions for 
 * dealing with ContentItem.
 */
@SuppressWarnings("deprecation")
public class SakaiContentItemUtil {

	private static Log M_log = LogFactory.getLog(SakaiContentItemUtil.class);

	public static LtiLinkItem getLtiLinkItem(String toolRegistration)
	{
		Tool theTool = ToolManager.getTool(toolRegistration);
		if ( theTool == null ) return null;

                Icon icon = new Icon("https://www.apereo.org/sites/all/themes/apereo/images/apereo-logo-white-bg.png");
                icon.setHeight(64);
                icon.setWidth(64);

                PlacementAdvice placementAdvice = new PlacementAdvice();

		// If we are http, lets go in a new window
		String serverUrl = SakaiBLTIUtil.getOurServerUrl();
		if ( serverUrl.startsWith("https://") ) {
			placementAdvice.setPresentationDocumentTarget(placementAdvice.IFRAME);
		} else {
			placementAdvice.setPresentationDocumentTarget(placementAdvice.WINDOW);
		}

                LtiLinkItem item = new LtiLinkItem(toolRegistration, placementAdvice, icon);
                item.setUrl(SakaiLTIProviderUtil.getProviderLaunchUrl(toolRegistration));
                item.setTitle(theTool.getTitle());

		// Because of weirdness in the CI Implementations and unclear semantics
		// between text and title, we send title twice
                // item.setText(theTool.getDescription());
                item.setText(theTool.getTitle());

		return item;
	}

	public static ContentItemResponse getContentItemResponse(String toolRegistration)
	{
		LtiLinkItem item = getLtiLinkItem(toolRegistration);
		if ( item == null ) return null;
		
                ContentItemResponse resp = new ContentItemResponse();
                resp.addGraph(item);
		return resp;
	}

}
