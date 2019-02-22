/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.wicket.markup.html.link;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.resource.ResourceReference;

public class IconLink extends Panel
{
	private static final long serialVersionUID = 1L;

	public IconLink(String id, Class<?> pageClass, PageParameters params, ResourceReference iconReference)
	{
		this(id, pageClass, params, iconReference, null);
	}

	public IconLink(String id, Class<?> pageClass, PageParameters params, ResourceReference iconReference, String popupWindowName)
	{
		super(id);
		add(newLink("link", pageClass, params, iconReference, popupWindowName));
	}

	private BookmarkablePageLink newLink(String baseId, Class<?> destPage, PageParameters params, final ResourceReference icon, String popupWindowName)
	{
		BookmarkablePageLink actionLink = new BookmarkablePageLink(baseId, destPage, params);

		// Add the passed icon 
		if (icon != null)
		{
			String iconId = new StringBuilder().append(baseId).append("Icon").toString();
			Image iconImage = new Image(iconId)
			{
				private static final long serialVersionUID = 1L;
	
				@Override
				protected ResourceReference getImageResourceReference()
				{
					return icon;
				}
			};

			actionLink.add(iconImage);
		}

		if (popupWindowName != null)
		{
			actionLink.setPopupSettings(new PopupSettings(PopupSettings.RESIZABLE | PopupSettings.SCROLLBARS).setWindowName(popupWindowName));
		}

		return actionLink;
	}
}
