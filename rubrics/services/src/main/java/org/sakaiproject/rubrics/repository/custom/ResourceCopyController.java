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

package org.sakaiproject.rubrics.repository.custom;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.sakaiproject.rubrics.RubricsConfiguration;
import org.sakaiproject.rubrics.model.Criterion;
import org.sakaiproject.rubrics.model.Rating;
import org.sakaiproject.rubrics.model.Rubric;
import org.sakaiproject.rubrics.repository.CriterionRepository;
import org.sakaiproject.rubrics.repository.RatingRepository;
import org.sakaiproject.rubrics.repository.RubricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RepositoryRestController
@NoArgsConstructor
@AllArgsConstructor
public class ResourceCopyController {

    @Autowired
    RubricsConfiguration rubricsConfiguration;

    @Autowired
    private RubricRepository rubricRepository;

    @Autowired
    private CriterionRepository criterionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    RepositoryEntityLinks repositoryEntityLinks;

    private static final String X_COPY_HEADER = "x-copy-source";
    private static final String X_LANG_HEADER = "lang";

    @PreAuthorize("canCopy(#sourceId, 'Rubric')")
    @RequestMapping(value = "/rubrics", method = RequestMethod.POST, headers = {X_COPY_HEADER,X_LANG_HEADER})
    ResponseEntity<Rubric> copyRubric(@Param("sourceId") @RequestHeader(value = X_COPY_HEADER) String sourceId, @RequestHeader(value = X_LANG_HEADER) String lang)
            throws CloneNotSupportedException {
        Rubric sourceRubric = null;
        Rubric clonedRubric = null;
        if ("default".equalsIgnoreCase(sourceId)) {
            sourceRubric = rubricsConfiguration.getInstance().getDefaultLayoutConfiguration(lang).getDefaultRubric();
            clonedRubric = sourceRubric.clone();
        } else {
            sourceRubric = rubricRepository.findOne(Long.parseLong(sourceId));
            clonedRubric = sourceRubric.clone();
            clonedRubric.setTitle(sourceRubric.getTitle() + " Copy");
        }
        clonedRubric = rubricRepository.save(clonedRubric);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(repositoryEntityLinks.linkFor(Rubric.class).slash(clonedRubric.getId()).toUri());
        return new ResponseEntity<Rubric>(clonedRubric, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("canCopy(#sourceId, 'Criterion')")
    @RequestMapping(value = "/criterions", method = RequestMethod.POST, headers = {X_COPY_HEADER,X_LANG_HEADER})
    ResponseEntity<Criterion> copyCriterion(@Param("sourceId") @RequestHeader(value = X_COPY_HEADER) String sourceId, @RequestHeader(value = X_LANG_HEADER) String lang)
            throws CloneNotSupportedException {
        Criterion sourceCriterion = null;
        Criterion clonedCriterion = null;
        if ("default".equalsIgnoreCase(sourceId)) {
            sourceCriterion = rubricsConfiguration.getInstance().getDefaultLayoutConfiguration(lang).getDefaultCriterion();
            clonedCriterion = sourceCriterion.clone();
        } else {
            sourceCriterion = criterionRepository.findOne(Long.parseLong(sourceId));
            clonedCriterion = sourceCriterion.clone();
            clonedCriterion.setTitle(sourceCriterion.getTitle() + " Copy");
        }
        clonedCriterion = criterionRepository.save(clonedCriterion);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(repositoryEntityLinks.linkFor(Criterion.class).slash(clonedCriterion.getId()).toUri());
        return new ResponseEntity<Criterion>(clonedCriterion, headers, HttpStatus.CREATED);
    }

    @PreAuthorize("canCopy(#sourceId, 'Rating')")
    @RequestMapping(value = "/ratings", method = RequestMethod.POST,  headers = {X_COPY_HEADER,X_LANG_HEADER})
    ResponseEntity<Rating> copyRating(@Param("sourceId") @RequestHeader(value = X_COPY_HEADER) String sourceId, @RequestHeader(value = X_LANG_HEADER) String lang)
            throws CloneNotSupportedException {
        Rating sourceRating = null;
        Rating clonedRating = null;
        if ("default".equalsIgnoreCase(sourceId)) {
            sourceRating = rubricsConfiguration.getInstance().getDefaultLayoutConfiguration(lang).getDefaultRating();
            clonedRating = sourceRating.clone();
        } else {
            sourceRating = ratingRepository.findOne(Long.parseLong(sourceId));
            clonedRating = sourceRating.clone();
            clonedRating.setTitle(sourceRating.getTitle() + " Copy");
        }
        clonedRating = ratingRepository.save(clonedRating);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(repositoryEntityLinks.linkFor(Rating.class).slash(clonedRating.getId()).toUri());
        return new ResponseEntity<Rating>(clonedRating, headers, HttpStatus.CREATED);
    }
}
