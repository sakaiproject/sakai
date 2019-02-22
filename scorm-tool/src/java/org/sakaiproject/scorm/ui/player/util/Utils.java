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
package org.sakaiproject.scorm.ui.player.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.util.string.Strings;

public final class Utils
{
	public final static String generateUrl(Behavior behavior, Component component, boolean isRelative)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Behavior must be bound to a component to create the URL");
		}

		String relativePagePath = component.urlForListener(behavior, component.getPage().getPageParameters()).toString();
		String url = null;

		if (!isRelative)
		{
			ServletWebRequest webRequest = (ServletWebRequest) component.getRequest();
			HttpServletRequest servletRequest = webRequest.getContainerRequest();
			String contextPath = servletRequest.getContextPath();
			String relativePath = relativePagePath.replaceAll("\\.\\.\\/", "");
			url = new StringBuilder(contextPath).append("/").append(relativePath).toString();
		}
		else
		{
			url = relativePagePath;
		}

		return url;
	}

	static String removeDoubleDots(String path)
	{
		List newcomponents = new ArrayList(Arrays.asList(path.split("/")));

		for (int i = 0; i < newcomponents.size(); i++)
		{
			if (i < newcomponents.size() - 1)
			{
				// Verify for a ".." component at next iteration
				if (((String)newcomponents.get(i)).length() > 0 && newcomponents.get(i + 1).equals(".."))
				{
					newcomponents.remove(i);
					newcomponents.remove(i);
					i = i - 2;
					if (i < -1)
					{
						i = -1;
					}
				}
			}
		}

		String newpath = Strings.join("/", (String[])newcomponents.toArray(new String[newcomponents.size()]));
		return path.endsWith( "/") ? newpath + "/" : newpath;
	}

	public final static Comparator getPropertyComparator( SortParam sort )
	{
		Comparator propertyComparator = new Comparator()
		{
			@Override
			public int compare( Object o1, Object o2 )
			{
				Object p1 = PropertyResolver.getValue( sort.getProperty().toString(), o1 );
				Object p2 = PropertyResolver.getValue( sort.getProperty().toString(), o2 );

				if( p1 != null && p2 != null & p1 instanceof Comparable )
				{
					return ((Comparable) p1).compareTo( (Comparable) p2 );
				}

				return 0;
			}
		};

		return propertyComparator;
	}
}
