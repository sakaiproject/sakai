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
package org.sakaiproject.sitestats.impl.event.detailed.refresolvers;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.sitestats.api.event.detailed.ResolvedEventData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.AssessmentData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.SamigoData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.SectionData;
import org.sakaiproject.sitestats.api.event.detailed.samigo.ItemData;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.SamigoRefParser;
import org.sakaiproject.sitestats.impl.event.detailed.refresolvers.parsers.SamigoRefParser.SamigoEventRef;
import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.data.ifc.assessment.EvaluationModelIfc;
import org.sakaiproject.tool.assessment.facade.AssessmentFacade;
import org.sakaiproject.tool.assessment.facade.ItemFacade;
import org.sakaiproject.tool.assessment.facade.SectionFacade;
import org.sakaiproject.tool.assessment.facade.PublishedAssessmentFacade;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.sakaiproject.tool.assessment.services.ItemService;
import org.sakaiproject.tool.assessment.services.PublishedItemService;
import org.sakaiproject.tool.assessment.services.assessment.AssessmentService;
import org.sakaiproject.tool.assessment.services.assessment.PublishedAssessmentService;

@Slf4j
public class SamigoReferenceResolver
{
	public static final String TOOL_ID = "sakai.samigo";

	/**
	 * Resolves the given event reference into meaningful details
	 * @param eventType the event type
	 * @param ref the event reference
	 * @param assessServ a Samigo service
	 * @param pubAssessServ a Samigo service
	 * @param gradeServ a Samigo service
	 * @param itemServ a Samigo service
	 * @param pubItemServ a Samigo service
	 * @return one of the AssessmentData variants, or ResolvedEventData.ERROR
	 */
	public static ResolvedEventData resolveReference(String eventType, String ref, AssessmentService assessServ,
			PublishedAssessmentService pubAssessServ, GradingService gradeServ, ItemService itemServ,
			PublishedItemService pubItemServ)
	{
		Optional<SamigoEventRef> samRefOpt = SamigoRefParser.parse(eventType, ref);
		if (!samRefOpt.isPresent())
		{
			return ResolvedEventData.ERROR;
		}
		SamigoEventRef samRef = samRefOpt.get();

		switch (samRef.type)
		{
			case SUBMISSION:
				return getAssessmentDataForSubId(samRef.id, gradeServ, pubAssessServ);
			case PUBITEM:
				return getItemDataForPubId(samRef.id, pubItemServ);
			case ITEM:
				return getItemData(samRef.id, itemServ);
			case PUBSECTION:
				return getSectionDataForPubId(samRef.id, pubAssessServ);
			case SECTION:
				return getSectionData(samRef.id, assessServ);
			case PUBASSESSMENT:
				return getAssessmentDataForPubId(samRef.id, pubAssessServ);
			case ASSESSMENT:
				return getAssessmentData(samRef.id, assessServ);
			default:
				return ResolvedEventData.ERROR;
		}
	}

	private static ResolvedEventData getAssessmentDataForSubId(long id, GradingService gs, PublishedAssessmentService pas)
	{
		return getAssessmentGradingData(id, gs)
				.flatMap(agd -> Optional.ofNullable(agd.getPublishedAssessmentId()))
				.map(pa -> getAssessmentDataForPubId(pa, pas))
				.orElse(ResolvedEventData.ERROR);
	}

	private static ResolvedEventData getAssessmentDataForPubId(long id, PublishedAssessmentService pas)
	{
		return getPublishedAssessment(id, pas)
				.map(p -> isAnon(p) ? (ResolvedEventData) SamigoData.ANON_ASSESSMENT
						: (ResolvedEventData) new AssessmentData(p.getTitle(), true))
				.orElse(ResolvedEventData.ERROR);
	}

	private static ResolvedEventData getAssessmentData(long id, AssessmentService assessServ)
	{
		return getAssessment(id, assessServ)
				.map(a -> (ResolvedEventData) new AssessmentData(a.getTitle(), false))
				.orElse(ResolvedEventData.ERROR);
	}

	private static ResolvedEventData getSectionData(long id, AssessmentService assessServ)
	{
		return getSection(id, assessServ)
				.map(s -> (ResolvedEventData) new SectionData(new AssessmentData(s.getAssessment().getTitle(), false), s.getTitle()))
				.orElse(ResolvedEventData.ERROR);
	}

	private static ResolvedEventData getSectionDataForPubId(long id, PublishedAssessmentService pas)
	{
		return getPubSection(id, pas)
				.map(s -> (ResolvedEventData) new SectionData(new AssessmentData(s.getAssessment().getTitle(), true), s.getTitle()))
				.orElse(ResolvedEventData.ERROR);
	}

	private static ResolvedEventData getItemData(long id, ItemService is)
	{
		return getItem(id, is)
				.map(i -> (ResolvedEventData)
						new ItemData(new SectionData(new AssessmentData(i.getSection().getAssessment().getTitle(), false),
								i.getSection().getTitle()), i.getSequence()))
				.orElse(ResolvedEventData.ERROR);
	}

	private static ResolvedEventData getItemDataForPubId(long id, PublishedItemService is)
	{
		return getPubItem(id, is)
				.map(i -> (ResolvedEventData)
						new ItemData(new SectionData(new AssessmentData(i.getSection().getAssessment().getTitle(), true),
								i.getSection().getTitle()), i.getSequence()))
				.orElse(ResolvedEventData.ERROR);
	}

	private static boolean isAnon(PublishedAssessmentFacade paf)
	{
		if (paf != null)
		{
			EvaluationModelIfc evalModel = paf.getEvaluationModel();
			if (evalModel != null)
			{
				Integer anonGrading = evalModel.getAnonymousGrading();
				return anonGrading != null && anonGrading == 1;
			}
		}

		return false;
	}

	private static Optional<AssessmentGradingData> getAssessmentGradingData(long assessmentGradingId, GradingService gs)
	{
		try
		{
			return Optional.ofNullable(gs.load(String.valueOf(assessmentGradingId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve assessmentgradingdata", e);
			return Optional.empty();
		}
	}

	private static Optional<AssessmentFacade> getAssessment(long assessmentId, AssessmentService as)
	{
		try
		{
			return Optional.ofNullable(as.getAssessment(String.valueOf(assessmentId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve assessment", e);
			return Optional.empty();
		}
	}

	private static Optional<SectionFacade> getSection(long sectionId, AssessmentService as)
	{
		try
		{
			return Optional.ofNullable(as.getSection(String.valueOf(sectionId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve section", e);
			return Optional.empty();
		}
	}

	private static Optional<SectionFacade> getPubSection(long sectionId, PublishedAssessmentService pas)
	{
		try
		{
			return Optional.ofNullable(pas.getSection(String.valueOf(sectionId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve pubsection", e);
			return Optional.empty();
		}
	}

	private static Optional<ItemFacade> getItem(long itemId, ItemService is)
	{
		try
		{
			return Optional.ofNullable(is.getItem(String.valueOf(itemId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve item", e);
			return Optional.empty();
		}
	}

	private static Optional<ItemFacade> getPubItem(long itemId, PublishedItemService is)
	{
		try
		{
			return Optional.ofNullable(is.getItem(String.valueOf(itemId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve pubitem", e);
			return Optional.empty();
		}
	}

	private static Optional<PublishedAssessmentFacade> getPublishedAssessment(long assessmentId, PublishedAssessmentService pas)
	{
		try
		{
			return Optional.ofNullable(pas.getPublishedAssessment(String.valueOf(assessmentId)));
		}
		catch (RuntimeException e)
		{
			log.warn("Unable to retrieve publishedassessment", e);
			return Optional.empty();
		}
	}
}
