/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.sitestats.api.parser.EventParserTip;

/**
 * Parser for event references that follow the common pattern of including a site id, an optional subcontext, and an entity id
 *
 * @author plukasew
 */
public class GenericRefParser
{
	protected static final String CONTEXT = "contextId";
	protected static final String SUBCONTEXT = "subContextId";
	protected static final String ENTITY = "entityId";

	/**
	 * Parse the context, subcontext, and entity from the given ref using the provided parser tips
	 * @param ref the event reference to parseTip
	 * @param tips the parser tips that indicate where in the ref string to find each item
	 * @return a GenericEventRef object containing the context, subcontext, and entity (empty string if not found)
	 */
	public static GenericEventRef parse(String ref, List<EventParserTip> tips)
	{
		String context = "";
		String subContext = "";
		String entity = "";

		for (EventParserTip tip : tips)
		{
			switch (tip.getFor())
			{
				case CONTEXT:
					context = parseTip(ref, tip);
					break;
				case SUBCONTEXT:
					subContext = parseTip(ref, tip);
					break;
				case ENTITY:
					entity = parseTip(ref, tip);
					break;
			}
		}

		return new GenericEventRef(context, subContext, entity);
	}

	/**
	 * Parses the event ref using the given tip, returning the item found
	 * @param ref the event reference
	 * @param tip parser tip indicating where to find the item in the reference
	 * @return the item indicated by the parser tip, or empty string if not found
	 */
	public static String parseTip(String ref, EventParserTip tip)
	{
		int index = Integer.parseInt(tip.getIndex());
		String[] splitRef = ref.split(tip.getSeparator());
		if (index >= splitRef.length)
		{
			// ref cannot be parsed by this tip
			return "";
		}
		return StringUtils.trimToEmpty(splitRef[index]);
	}

	public static class GenericEventRef
	{
		public final String contextId;
		public final String subContextId;
		public final String entityId;

		/**
		 * Constructor for a generic parsed event reference
		 * @param context typically the site id
		 * @param subContext used by some events to indicate a sublevel in a hierarchy, below site but above an actual entity
		 * @param entity typically the unique id for a single announcement, forum post, etc.
		 */
		public GenericEventRef(String context, String subContext, String entity)
		{
			contextId = context;
			subContextId = subContext;
			entityId = entity;
		}
	}
}
