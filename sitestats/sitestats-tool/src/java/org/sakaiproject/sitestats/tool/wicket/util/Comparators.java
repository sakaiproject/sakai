/**
 * Copyright (c) 2006-2019 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.wicket.util;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.extensions.markup.html.form.select.IOptionRenderer;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.tool.facade.Locator;
import org.sakaiproject.sitestats.tool.wicket.models.LoadableDisplayUserListModel.DisplayUser;

/**
 * Utility class to house Comparators using a common RuleBasedCollator
 * @author plukasew
 */
@Slf4j
public class Comparators
{
	private static Collator collator = Collator.getInstance();
	static
	{
		try
		{
			collator = new RuleBasedCollator(((RuleBasedCollator)Collator.getInstance()).getRules().replaceAll("<'\u005f'", "<' '<'\u005f'"));
		}
		catch (ParseException e)
		{
			log.error("Unable to create RuleBasedCollator", e);
		}
	}

	/**
	 * Returns a comparator for strings
	 * @return the comparator
	 */
	public static final Comparator<String> getStringComparator()
	{
		return (String o1, String o2) -> collator.compare(o1, o2);
	}

	/**
	 * Returns a comparator for ToolInfo objects, comparing based on the tool name
	 * @return the comparator
	 */
	public static final Comparator<ToolInfo> getToolInfoComparator()
	{
		return (ToolInfo o1, ToolInfo o2) ->
		{
			String toolName1 = Locator.getFacade().getEventRegistryService().getToolName(o1.getToolId());
			String toolName2 = Locator.getFacade().getEventRegistryService().getToolName(o2.getToolId());
			return collator.compare(toolName1, toolName2);
		};
	}

	/**
	 * Returns a comparator using the given renderer to generate display strings which are compared
	 * @param renderer the renderer
	 * @return the comparator
	 */
	public static final Comparator<Object> getOptionRendererComparator(final IOptionRenderer renderer)
	{
		return (Object o1, Object o2) -> collator.compare(renderer.getDisplayValue(o1), renderer.getDisplayValue(o2));
	}

	/**
	 * Returns a comparator using the given renderer to generate display strings which are compared
	 * @param renderer the renderer
	 * @return the comparator
	 */
	public static final Comparator<Object> getChoiceRendererComparator(final IChoiceRenderer renderer)
	{
		return (Object o1, Object o2) -> collator.compare(renderer.getDisplayValue(o1), renderer.getDisplayValue(o2));
	}

	/**
	 * Returns a comparator for DisplayUser objects, comparing on the display value
	 * @return
	 */
	public static final Comparator<DisplayUser> getDisplayUserComparator()
	{
		return (DisplayUser d1, DisplayUser d2) -> collator.compare(d1.display, d2.display);
	}
}
