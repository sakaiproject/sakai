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

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser that extracts the relevant tokens (entity type and id) from the ref string
 * @author bbailla2
 * @author plukasew
 */
public class SamigoRefParser
{
	private static final String REGEX_ASSESSMENT_ID = "assessmentId=(\\d+)";
	private static final String REGEX_ITEM_ID = "itemId=(\\d+)";
	private static final String REGEX_PUBLISHED_ASSESSMENT_ID_1 = "publishedAssessmentId=(\\d+)";
	private static final String REGEX_PUBLISHED_ASSESSMENT_ID_2 = "publishedAssessmentID=(\\d+)";
	private static final String REGEX_PUBLISHED_ASSESSMENT_ID_3 = "publishedAssessmentId(\\d+)";
	private static final String REGEX_SECTION_ID = "sectionId=(\\d+)";
	private static final String REGEX_SUBMISSION_ID = "submissionId=(\\d+)";

	private static final String PUB_REVISE = "sam.pubassessment.revise";

	/**
	 * Parse the event ref, returning the type of reference (assessment, submission, etc) and the id.
	 * @param eventType the event type
	 * @param eventRef the ref to parse
	 * @return parsed event reference, or empty if reference was invalid
	 */
	public static Optional<SamigoEventRef> parse(String eventType, String eventRef)
	{
		// Samigo event references vary greatly and can contain a lot of information. Rather than keeping track of what
		// each individual event type stores, we take a simpler approach and try to identify the main entity that the
		// ref is about. We do this using a specificity hierarchy and extract only the most specific entity:
		// submission/item/section > published assessment > assessment
		Optional<Long> id = matchRegex(eventRef, REGEX_SUBMISSION_ID);
		if (id.isPresent())
		{
			return Optional.of(new SamigoEventRef(Type.SUBMISSION, id.get()));
		}

		id = matchRegex(eventRef, REGEX_ITEM_ID);
		if (id.isPresent())
		{
			Type type = PUB_REVISE.equals(eventType) ? Type.PUBITEM : Type.ITEM;
			return Optional.of(new SamigoEventRef(type, id.get()));
		}

		id = matchRegex(eventRef, REGEX_SECTION_ID);
		if (id.isPresent())
		{
			Type type = PUB_REVISE.equals(eventType) ? Type.PUBSECTION : Type.SECTION;
			return Optional.of(new SamigoEventRef(type, id.get()));
		}

		Long pubId = matchRegex(eventRef, REGEX_PUBLISHED_ASSESSMENT_ID_1)
				.orElseGet(() -> matchRegex(eventRef, REGEX_PUBLISHED_ASSESSMENT_ID_2)
						.orElseGet(()-> matchRegex(eventRef, REGEX_PUBLISHED_ASSESSMENT_ID_3)
								.orElse(-1L)));
		if (pubId >= 0)
		{
			return Optional.of(new SamigoEventRef(Type.PUBASSESSMENT, pubId));
		}

		id = matchRegex(eventRef, REGEX_ASSESSMENT_ID);
		if (id.isPresent())
		{
			return Optional.of(new SamigoEventRef(Type.ASSESSMENT, id.get()));
		}

		return Optional.empty();
	}

	private static Optional<Long> matchRegex(String text, String regex)
	{
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(text);
		if (m.find())
		{
			return Optional.of(Long.valueOf(m.group(1)));
		}

		return Optional.empty();
	}

	public enum Type { ASSESSMENT, PUBASSESSMENT, SECTION, PUBSECTION, ITEM, PUBITEM, SUBMISSION };

	public static class SamigoEventRef
	{
		public final Type type;
		public final long id;

		/**
		 * Data from a parsed event reference
		 * @param type the type of entity referenced
		 * @param id the entity id
		 */
		public SamigoEventRef(Type type, long id)
		{
			this.type = type;
			this.id = id;
		}
	}
}
