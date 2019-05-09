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
package org.sakaiproject.sitestats.tool.transformers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.sakaiproject.sitestats.api.event.detailed.EventDetail;
import org.sakaiproject.sitestats.api.event.detailed.samigo.AssessmentData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.ItemData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.SamigoData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.SectionData;
import org.sakaiproject.util.ResourceLoader;

/**
 * View-layer logic for presenting the data contained in the ResolvedEventData object,
 * default mechanism of presentation is a simple K/V list
 * @author plukasew
 */
public class SamigoResolvedRefTransformer
{
	private static final String ASSESSMENT = "de_sam_assessment";

	/**
	 * Transforms SamigoData for presentation to the user
	 * @param data the data
	 * @param rl resource loader for i18n
	 * @return EventDetails for presentation
	 */
	public static List<EventDetail> transform(SamigoData data, ResourceLoader rl)
	{
		if (data instanceof AssessmentData.AnonymousAssessment)
		{
			return Collections.singletonList(EventDetail.newText(rl.getString(ASSESSMENT), rl.getString("de_sam_assessment_anon")));
		}
		else if (data instanceof SectionData)
		{
			SectionData section = (SectionData) data;
			List<EventDetail> details = new ArrayList<>(2);
			details.add(getAssessmentDetails(section.assessment, rl));
			details.add(getSectionDetails(section, rl));
			return details;
		}
		else if (data instanceof ItemData)
		{
			ItemData item = (ItemData) data;
			List<EventDetail> details = new ArrayList<>(3);
			details.add(getAssessmentDetails(item.section.assessment, rl));
			details.add(getSectionDetails(item.section, rl));
			details.add(EventDetail.newText(rl.getString("de_sam_question"), String.valueOf(item.questionNumber)));
			return details;
		}

		return Collections.singletonList(getAssessmentDetails((AssessmentData) data, rl));
	}

	private static EventDetail getAssessmentDetails(AssessmentData ad, ResourceLoader rl)
	{
		String title = ad.published ? ad.title : rl.getFormattedMessage("de_sam_assessment_draft", ad.title);
		return EventDetail.newText(rl.getString(ASSESSMENT), title);
	}

	private static EventDetail getSectionDetails(SectionData section, ResourceLoader rl)
	{
		return EventDetail.newText(rl.getString("de_sam_part"), section.title);
	}
}
