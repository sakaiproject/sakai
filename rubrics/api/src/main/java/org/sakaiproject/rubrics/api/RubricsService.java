/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.rubrics.api;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.sakaiproject.entity.api.Entity;
import org.sakaiproject.rubrics.api.beans.AssociationTransferBean;
import org.sakaiproject.rubrics.api.beans.CriterionTransferBean;
import org.sakaiproject.rubrics.api.beans.EvaluationTransferBean;
import org.sakaiproject.rubrics.api.beans.RatingTransferBean;
import org.sakaiproject.rubrics.api.beans.RubricTransferBean;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;

public interface RubricsService {

    public static final String REFERENCE_ROOT = Entity.SEPARATOR + "rubrics";

    RubricTransferBean createDefaultRubric(String siteId);

    /**
     * Copy the specified source rubric to the site denoted by toSiteId
     *
     * @param rubricId The source rubric id
     * @param toSiteId The target site
     * @return The new rubric bean
     */
    RubricTransferBean copyRubricToSite(Long rubricId, String toSiteId);

    List<RubricTransferBean> getRubricsForSite(String siteId);

    List<RubricTransferBean> getSharedRubrics();

    RubricTransferBean saveRubric(RubricTransferBean rubricBean);

    void deleteRubric(Long rubricId);

    Optional<CriterionTransferBean> createDefaultCriterion(String siteId, Long rubricId);

    Optional<CriterionTransferBean> createDefaultEmptyCriterion(String siteId, Long rubricId);

    CriterionTransferBean copyCriterion(Long rubricId, Long sourceId);

    Optional<RatingTransferBean> createDefaultRating(String siteId, Long rubricId, Long criterionId, int position);

    /**
     * This method is used for updating a Criterion's values it should not be used to create a new Criterion
     * as it will not correctly update it's mapped entities
     *
     * @param bean who's values are used to update the referenced Criterion
     * @param siteId
     * @return a CriterionTransferBean that reflects the changes made to the Criterion, or the same bean if the criterion could not be fetched from persistence
     */
    CriterionTransferBean updateCriterion(CriterionTransferBean bean, String siteId);

    void deleteCriterion(Long rubricId, Long criterionId, String siteId);

    void sortRubricCriteria(Long rubricId, List<Long> sortedCriterionIds);

    void sortCriterionRatings(Long criteriaId, List<Long> sortedRatingIds);

    /**
     * This method is used for updating a Criterion's values it should not be used to create a new Criterion
     * as it will not correctly update it's mapped entities
     *
     * @param bean   who's values are used to update the referenced Rating
     * @param siteId
     * @return a RatingTransferBean that reflects the changes made to the Rating, or the same bean if the Rating could not be fetched from persistence
     */
    RatingTransferBean updateRating(RatingTransferBean bean, String siteId);

    CriterionTransferBean deleteRating(Long ratingId, Long criterionId, String siteId, Long rubricId);

    Optional<RubricTransferBean> getRubric(Long rubricId);

    Optional<CriterionTransferBean> getCriterion(Long criterionId, String siteId);

    Optional<AssociationTransferBean> getAssociationForToolAndItem(String toolId, String itemId, String siteId);

    Optional<EvaluationTransferBean> getEvaluation(Long evaluationId, String siteId);

    Optional<EvaluationTransferBean> getEvaluationForToolAndItemAndEvaluatedItemAndOwnerId(String toolId, String itemId, String evaluatedItemId, String evaluatedItemOwnerId, String siteId, boolean isPeer);

    boolean deleteEvaluationForToolAndItemAndEvaluatedItemId(String toolId, String itemId, String evaluatedItemId, String siteId);

    List<EvaluationTransferBean> getEvaluationsForToolAndItem(String toolId, String itemId, String siteId);

    EvaluationTransferBean saveEvaluation(EvaluationTransferBean evaluationBean, String siteId);

    EvaluationTransferBean cancelDraftEvaluation(Long draftEvaluationId);

    boolean hasAssociatedRubric(String toolId, String associatedToolItemId);

    Optional<ToolItemRubricAssociation> getRubricAssociation(String toolId, String associatedToolItemId);

    /**
     * Save the association between a tool item (an assignment, for example), and a rubric
     * If you are deactivating/removing an existing association, it will soft-delete its data by doing setActive(false) on it.
     * If you are reactivating an association that has already existed, it will take the old data for it and simply do setActive(true) on it.
     *
     * @param toolId the tool id, something like "sakai.assignment"
     * @param toolItemId the id of the tool's item that is being associated with the rubric
     * @param params A hashmap with all the rbcs params comming from the component. The tool should generate it.
     */
    Optional<ToolItemRubricAssociation> saveRubricAssociation(String toolId, String toolItemId, Map<String, String> params);
    Optional<ToolItemRubricAssociation> saveRubricAssociation(String toolId, String toolItemId, Map<String, String> params, String siteId);

    String createContextualFilename(RubricTransferBean rubric, String toolId, String itemId, String evaluatedItemId, String siteId);

    byte[] createPdf(String siteId, Long rubricId, String toolId, String itemId, String evaluatedItemId)
            throws IOException;

    String getRubricEvaluationObjectId(String itemId, String userId, String toolId, String siteId);

    void deleteRubricAssociation(String toolId, String itemId);
    void softDeleteRubricAssociation(String toolId, String itemId);
    void restoreRubricAssociation(String toolId, String itemId);

    void deleteRubricAssociationsByItemIdPrefix(String itemId, String toolId);
    void softDeleteRubricAssociationsByItemIdPrefix(String itemId, String toolId);
    void restoreRubricAssociationsByItemIdPrefix(String itemId, String toolId);

    void deleteSiteRubrics(String siteId);

    /**
     * Find the associations of a rubric to a tool
     *
     * @param rubricId the rubric id
     * @param toolId the tool id, something like "sakai.assignment"
     * @return a List of ToolItemRubricAssociation containing the associations of a Rubric to a tool. Example: All the rubric associations to assessments.
     */
    List<ToolItemRubricAssociation> getRubricAssociationsByRubricAndTool(Long rubricId, String toolId);

}
