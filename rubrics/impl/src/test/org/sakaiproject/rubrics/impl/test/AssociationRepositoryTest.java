/**
 * Copyright (c) 2026 The Apereo Foundation
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
package org.sakaiproject.rubrics.impl.test;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.rubrics.api.model.Rubric;
import org.sakaiproject.rubrics.api.model.ToolItemRubricAssociation;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RubricsTestConfiguration.class)
public class AssociationRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    @Autowired private AssociationRepository associationRepository;
    @Autowired private RubricRepository rubricRepository;

    @Test
    public void queriesMatchRubricIdsAndReturnNoAssociationsForUnusedRubrics() {
        Rubric rubric = saveRubric("Associated rubric");
        Rubric unusedRubric = saveRubric("Unused rubric");

        ToolItemRubricAssociation association = new ToolItemRubricAssociation();
        association.setRubric(rubric);
        association.setToolId("sakai.samigo");
        association.setItemId("assessment.question");
        association = associationRepository.save(association);

        assertEquals(association.getId(), associationRepository
                .findByItemIdAndRubricId("assessment.question", rubric.getId()).get().getId());
        assertFalse(associationRepository
                .findByItemIdAndRubricId("assessment.question", unusedRubric.getId()).isPresent());

        List<ToolItemRubricAssociation> associations = associationRepository.findByRubricId(rubric.getId());
        assertEquals(1, associations.size());
        assertEquals(association.getId(), associations.get(0).getId());
        assertTrue(associationRepository.findByRubricId(unusedRubric.getId()).isEmpty());

        associations = associationRepository.findByRubricIdAndToolId(rubric.getId(), "sakai.samigo");
        assertEquals(1, associations.size());
        assertEquals(association.getId(), associations.get(0).getId());
        assertTrue(associationRepository.findByRubricIdAndToolId(rubric.getId(), "sakai.assignment").isEmpty());
        assertTrue(associationRepository.findByRubricIdAndToolId(unusedRubric.getId(), "sakai.samigo").isEmpty());
    }

    private Rubric saveRubric(String title) {
        Rubric rubric = new Rubric();
        rubric.setTitle(title);
        rubric.setOwnerId("site1");
        rubric.setCreatorId("instructor");
        return rubricRepository.save(rubric);
    }
}
