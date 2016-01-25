/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.metadata.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.sakaiproject.content.metadata.logic.MetadataService;
import org.sakaiproject.content.metadata.model.*;

/**
 * @author Matthew Buckett
 */
public class MetadataServiceLOM implements MetadataService {

	private List<MetadataType> lomMeta;
	private static final String NAMESPACE_LOM = "http://ltsc.ieee.org/xsd/lomv1.0/";

	public MetadataServiceLOM() {

		GroupMetadataType lomMetadata = new GroupMetadataType();
		lomMetadata.setName("label.lom");
		lomMetadata.setUniqueName("lom");
		lomMetadata.setTranslated(true);
		List<MetadataType<?>> subTags = new ArrayList<MetadataType<?>>();
		lomMetadata.setMetadataTypes(subTags);

		/* LOM role */
		{
			StringMetadataType role = new StringMetadataType();
			role.setName("label.lom_role");
			role.setDescription("descr.lom_role");
			role.setTranslated(true);
			role.setUniqueName(NAMESPACE_LOM+"role");
			subTags.add(role);
		}

		/* LOM coverage */
		{
			StringMetadataType coverage = new StringMetadataType();
			coverage.setName("label.lom_coverage");
			coverage.setDescription("descr.lom_coverage");
			coverage.setTranslated(true);
			coverage.setUniqueName(NAMESPACE_LOM+"coverage");
			subTags.add(coverage);
		}

		/* LOM status */
		{
			EnumMetadataType status = new EnumMetadataType();
			status.setName("label.lom_status");
			status.setDescription("descr.lom_status");
			status.setTranslated(true);
			status.setAllowedValues(Arrays.asList("draft", "final", "revised", "unavailable"));
			status.setUniqueName(NAMESPACE_LOM+"status");
			subTags.add(status);
		}
		/* LOM duration */
		{
			DurationMetadataType duration = new DurationMetadataType();
			duration.setName("label.lom_duration");
			duration.setDescription("descr.lom_duration");
			duration.setTranslated(true);
			duration.setUniqueName(NAMESPACE_LOM + "duration");
			subTags.add(duration);
		}
		/* LOM engagement */
		{
			EnumMetadataType engagement = new EnumMetadataType();
			engagement.setName("label.lom_engagement");
			engagement.setDescription("descr.lom_engagement");
			engagement.setTranslated(true);
			engagement.setAllowedValues(Arrays.asList("active", "expositive", "mixed"));
			engagement.setUniqueName(NAMESPACE_LOM + "engagement");
			subTags.add(engagement);
		}

		/* LOM learning resource */
		{
			EnumMetadataType learningResource = new EnumMetadataType();
			learningResource.setName("label.lom_learning_resource_type");
			learningResource.setDescription("descr.lom_learning_resource_type");
			learningResource.setTranslated(true);
			learningResource.setAllowedValues(Arrays.asList("assignment", "case_study", "course", "diagram", "exam",
					"exercise", "experiment", "field_trip", "figure", "graph", "index", "lecture", "narrative_text",
					"problem_statement", "questionnaire", "quiz", "self_assessment", "simulation", "slide", "table"));
			learningResource.setUniqueName(NAMESPACE_LOM + "learning_resource_type");
			subTags.add(learningResource);
		}
		/* LOM interactivitu level */
		{
			EnumMetadataType interactivity = new EnumMetadataType();
			interactivity.setName("label.lom_interactivity_level");
			interactivity.setDescription("descr.lom_interactivity_level");
			interactivity.setTranslated(true);
			interactivity.setAllowedValues(Arrays.asList("very_low", "low", "medium", "high", "very_high"));
			interactivity.setUniqueName(NAMESPACE_LOM+ "interactivity_level");
			subTags.add(interactivity);
		}
		/* LOM context level */
		{
			EnumMetadataType contextLevel = new EnumMetadataType();
			contextLevel.setName("label.lom_context_level");
			contextLevel.setDescription("descr.lom_context_level");
			contextLevel.setTranslated(true);
			contextLevel.setAllowedValues(Arrays.asList("school", "higher_education", "training", "other"));
			contextLevel.setUniqueName(NAMESPACE_LOM + "context_level");
			subTags.add(contextLevel);
		}
		/* LOM difficulty */
		{
			EnumMetadataType difficulty = new EnumMetadataType();
			difficulty.setName("label.lom_difficulty");
			difficulty.setDescription("descr.lom_difficulty");
			difficulty.setTranslated(true);
			difficulty.setAllowedValues(Arrays.asList("very_easy", "easy", "medium", "difficult", "very_difficult"));
			difficulty.setUniqueName(NAMESPACE_LOM + "difficulty");
			subTags.add(difficulty);
		}
		/* LOM learning time */
		{
			DurationMetadataType learningTime = new DurationMetadataType();
			learningTime.setName("label.lom_learning_time");
			learningTime.setDescription("descr.lom_learning_time");
			learningTime.setTranslated(true);
			learningTime.setUniqueName(NAMESPACE_LOM + "learning_time");
			subTags.add(learningTime);
		}
		/* LOM assumed knowledge */
		{
			StringMetadataType assumedKnowledge = new StringMetadataType();
			assumedKnowledge.setName("label.lom_assumed_knowledge");
			assumedKnowledge.setDescription("descr.lom_assumed_knowledge");
			assumedKnowledge.setTranslated(true);
			assumedKnowledge.setLongText(true);
			assumedKnowledge.setUniqueName(NAMESPACE_LOM + "assumed_knowledge");
			subTags.add(assumedKnowledge);
		}
		/* LOM technical requirements */
		{
			StringMetadataType technicalRequirements = new StringMetadataType();
			technicalRequirements.setName("label.lom_technical_requirements");
			technicalRequirements.setDescription("descr.lom_technical_requirements");
			technicalRequirements.setTranslated(true);
			technicalRequirements.setLongText(true);
			technicalRequirements.setUniqueName(NAMESPACE_LOM + "technical_requirements");
			subTags.add(technicalRequirements);
		}
		/* LOM install remarks */
		{
			StringMetadataType installRemarks = new StringMetadataType();
			installRemarks.setName("label.lom_install_remarks");
			installRemarks.setDescription("descr.lom_install_remarks");
			installRemarks.setTranslated(true);
			installRemarks.setLongText(true);
			installRemarks.setUniqueName(NAMESPACE_LOM + "install_remarks");
			subTags.add(installRemarks);
		}
		/* LOM other requirements */
		{
			StringMetadataType otherRequirements = new StringMetadataType();
			otherRequirements.setName("label.lom_other_requirements");
			otherRequirements.setDescription("descr.lom_other_requirements");
			otherRequirements.setTranslated(true);
			otherRequirements.setLongText(true);
			otherRequirements.setUniqueName(NAMESPACE_LOM + "other_requirements");
			subTags.add(otherRequirements);
		}
		/* LOM granularity level */
		{
			EnumMetadataType granularityLevel = new EnumMetadataType();
			granularityLevel.setName("label.lom_granularity_level");
			granularityLevel.setDescription("descr.lom_granularity_level");
			granularityLevel.setTranslated(true);
			granularityLevel.setAllowedValues(Arrays.asList("raw_data", "lesson", "course", "program"));
			granularityLevel.setUniqueName(NAMESPACE_LOM + "granularity_level");
			subTags.add(granularityLevel);
		}
		/* LOM structure */
		{
			EnumMetadataType structure = new EnumMetadataType();
			structure.setName("label.lom_structure");
			structure.setDescription("descr.lom_structure");
			structure.setTranslated(true);
			structure.setAllowedValues(Arrays.asList("atomic", "collection", "networked", "hierarchical", "linear"));
			structure.setUniqueName(NAMESPACE_LOM + "structure");
			subTags.add(structure);
		}
		/* LOM relation */
		{
			StringMetadataType relation = new StringMetadataType();
			relation.setName("label.lom_relation");
			relation.setDescription("descr.lom_relation");
			relation.setTranslated(true);
			relation.setLongText(true);
			relation.setUniqueName(NAMESPACE_LOM + "relation");
			subTags.add(relation);
		}
		/* LOM reviewer */
		{
			StringMetadataType reviewer = new StringMetadataType();
			reviewer.setName("label.lom_reviewer");
			reviewer.setDescription("descr.lom_reviewer");
			reviewer.setTranslated(true);
			reviewer.setUniqueName(NAMESPACE_LOM + "reviewer");
			subTags.add(reviewer);
		}
		/* LOM review date */
		{
			StringMetadataType reviewDate = new StringMetadataType();
			reviewDate.setName("label.lom_review_date");
			reviewDate.setDescription("descr.lom_review_date");
			reviewDate.setTranslated(true);
			reviewDate.setUniqueName(NAMESPACE_LOM + "review_date");
			subTags.add(reviewDate);
		}
		/* LOM review comments */
		{
			StringMetadataType reviewComments = new StringMetadataType();
			reviewComments.setName("label.lom_review_comments");
			reviewComments.setDescription("descr.lom_review_comments");
			reviewComments.setTranslated(true);
			reviewComments.setLongText(true);
			reviewComments.setUniqueName(NAMESPACE_LOM + "review_comments");
			subTags.add(reviewComments);
		}
		lomMeta = Collections.<MetadataType>singletonList(lomMetadata);
	}


	@Override
	public List<MetadataType> getMetadataAvailable(String resourceType) {
		return lomMeta;
	}

	@Override
	public List<MetadataType> getMetadataAvailable(String siteId, String resourceType) {
		return lomMeta;
	}
}
