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
import org.sakaiproject.sitestats.api.parser.EventParserTip;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.GenericRefParser.GenericEventRef;

/**
 * Parser for event references from the Messages and Forums tools. 
 *
 * @author plukasew
 */
public class MsgForumsRefParser
{
	protected static final String TOOL = "tool";
	protected static final String USER = "userId";

	/**
	 * Returns the values extracted from the ref string using the given parser tips.
	 * @param ref the reference string
	 * @param tips the tips to use for parsing
	 * @return a custom object populated with raw string values from the reference string
	 */
	public static MsgForumsEventRef parse(String ref, List<EventParserTip> tips)
	{
		/* Format and examples:
		/<tool>/site/<site_id>/<level>/<item_id>/<user_id>
		/forums/site/<siteID>/Message/<messageID>/<userID>
		/forums/site/4a4be716-9414-4423-a3eb-863e70952d6e/Message/3333128/01584302-9de1-4a0d-801b-c4da83192ba1
		*/

		// first parseTip the generic parts
		GenericEventRef genRef = GenericRefParser.parse(ref, tips);

		// handle the custom parts
		String tool = "";
		String user = "";

		for (EventParserTip tip : tips)
		{
			switch (tip.getFor())
			{
				case TOOL:
					tool = GenericRefParser.parseTip(ref, tip);
					break;
				case USER:
					user = GenericRefParser.parseTip(ref, tip);
					break;
			}
		}

		return new MsgForumsEventRef(tool, genRef.contextId, genRef.subContextId, genRef.entityId, user);
	}

	public static class MsgForumsEventRef extends GenericRefParser.GenericEventRef
	{
		public final String tool;
		public final String userId;

		/**
		 * Constructor
		 * @param tool typically "messages" or "forums"
		 * @param context the site id
		 * @param subContext typically "Forum", "Topic", or "Message"
		 * @param entity the forum/topic/message id
		 * @param user the user id
		 */
		public MsgForumsEventRef(String tool, String context, String subContext, String entity, String user)
		{
			super(context, subContext, entity);
			this.tool = tool;
			userId = user;
		}
	}
}
