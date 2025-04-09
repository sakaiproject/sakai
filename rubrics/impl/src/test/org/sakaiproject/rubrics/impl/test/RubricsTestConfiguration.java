/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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

import lombok.Getter;
import org.hibernate.SessionFactory;
import org.sakaiproject.assignment.api.AssignmentService;
import org.sakaiproject.grading.api.GradingService;
import org.sakaiproject.rubrics.api.repository.AssociationRepository;
import org.sakaiproject.rubrics.api.repository.CriterionRepository;
import org.sakaiproject.rubrics.api.repository.EvaluationRepository;
import org.sakaiproject.rubrics.api.repository.RatingRepository;
import org.sakaiproject.rubrics.api.repository.RubricRepository;
import org.sakaiproject.rubrics.impl.repository.AssociationRepositoryImpl;
import org.sakaiproject.rubrics.impl.repository.CriterionRepositoryImpl;
import org.sakaiproject.rubrics.impl.repository.EvaluationRepositoryImpl;
import org.sakaiproject.rubrics.impl.repository.RatingRepositoryImpl;
import org.sakaiproject.rubrics.impl.repository.RubricRepositoryImpl;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.mockito.Mockito.mock;

@Configuration
@EnableTransactionManagement
@ImportResource("classpath:/WEB-INF/components.xml")
@PropertySource("classpath:/hibernate.properties")
public class RubricsTestConfiguration extends SakaiTestConfiguration {

    @Autowired
    @Qualifier("org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappings.rubrics")
    @Getter
    protected AdditionalHibernateMappings additionalHibernateMappings;

    @Bean(name="org.sakaiproject.rubrics.api.repository.CriterionRepository")
    public CriterionRepository criterionRepository(SessionFactory sessionFactory) {

        CriterionRepositoryImpl criterionRepository = new CriterionRepositoryImpl();
        criterionRepository.setSessionFactory(sessionFactory);
        return criterionRepository;
    }

    @Bean(name="org.sakaiproject.rubrics.api.repository.RatingRepository")
    public RatingRepository ratingRepository(SessionFactory sessionFactory) {

        RatingRepositoryImpl ratingRepository = new RatingRepositoryImpl();
        ratingRepository.setSessionFactory(sessionFactory);
        return ratingRepository;
    }

    @Bean(name="org.sakaiproject.rubrics.api.repository.RubricRepository")
    public RubricRepository rubricRepository(SessionFactory sessionFactory) {

        RubricRepositoryImpl rubricRepository = new RubricRepositoryImpl();
        rubricRepository.setSessionFactory(sessionFactory);
        return rubricRepository;
    }

    @Bean(name="org.sakaiproject.rubrics.api.repository.AssociationRepository")
    public AssociationRepository associationRepository(SessionFactory sessionFactory) {

        AssociationRepositoryImpl associationRepository = new AssociationRepositoryImpl();
        associationRepository.setSessionFactory(sessionFactory);
        return associationRepository;
    }

    @Bean(name="org.sakaiproject.rubrics.api.repository.EvaluationRepository")
    public EvaluationRepository evaluationRepository(SessionFactory sessionFactory) {

        EvaluationRepositoryImpl evaluationRepository = new EvaluationRepositoryImpl();
        evaluationRepository.setSessionFactory(sessionFactory);
        return evaluationRepository;
    }

    @Bean(name = "org.sakaiproject.time.api.UserTimeService")
    public UserTimeService userTimeService() {
        return mock(UserTimeService.class);
    }

    @Bean(name = "org.sakaiproject.util.api.FormattedText")
    public FormattedText formattedText() {
        return mock(FormattedText.class);
    }

    @Bean(name = "org.sakaiproject.assignment.api.AssignmentService")
    public AssignmentService assignmentService() {
        return mock(AssignmentService.class);
    }

    @Bean(name = "org.sakaiproject.grading.api.GradingService")
    public GradingService gradingService() {
        return mock(GradingService.class);
    }

    @Bean(name = "PersistenceService")
    public PersistenceService assessmentPersistenceService() {
        return mock(PersistenceService.class);
    }
}
