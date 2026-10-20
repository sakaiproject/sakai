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
package org.sakaiproject.springframework.data;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.sakaiproject.springframework.orm.hibernate.AdditionalHibernateMappings;
import org.sakaiproject.springframework.orm.hibernate.impl.AdditionalHibernateMappingsImpl;
import org.sakaiproject.test.SakaiTestConfiguration;
import org.sakaiproject.user.api.model.PreferencesRecord;
import org.sakaiproject.user.impl.PreferencesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = SpringCrudRepositoryTest.RepositoryTestConfiguration.class)
@TestPropertySource(properties = {
        "hibernate.connection.url=jdbc:hsqldb:mem:shared-crud-repository",
        "hibernate.hbm2ddl.auto=create-drop"
})
public class SpringCrudRepositoryTest {

    @Autowired private SpringCrudRepository<PreferencesRecord, String> preferencesRepository;
    @Autowired private SpringCrudRepository<PrimitiveVersionRecord, String> primitiveVersionRepository;
    @Autowired private SpringCrudRepository<UnversionedRecord, String> unversionedRepository;

    @Test
    public void persistsAnAssignedIdWithANullVersion() {
        PreferencesRecord record = new PreferencesRecord(UUID.randomUUID().toString(), "original", null);

        assertSame(record, preferencesRepository.save(record));
        assertEquals(Long.valueOf(0), record.getVersion());
        assertEquals("original", preferencesRepository.findById(record.getId()).orElseThrow().getXml());
    }

    @Test
    public void mergesADetachedEntityWithVersionZero() {
        PreferencesRecord record = preferencesRepository.save(
                new PreferencesRecord(UUID.randomUUID().toString(), "original", null));
        record.setXml("updated");

        PreferencesRecord saved = preferencesRepository.save(record);

        assertNotSame(record, saved);
        assertEquals(Long.valueOf(0), record.getVersion());
        assertEquals(Long.valueOf(1), saved.getVersion());
        assertEquals("updated", preferencesRepository.findById(record.getId()).orElseThrow().getXml());
    }

    @Test
    public void rejectsDeletingAStaleVersion() {
        PreferencesRecord stale = preferencesRepository.save(
                new PreferencesRecord(UUID.randomUUID().toString(), "original", null));
        PreferencesRecord winner = preferencesRepository.findById(stale.getId()).orElseThrow();
        winner.setXml("updated");
        preferencesRepository.save(winner);

        assertThrows(OptimisticLockException.class, () -> preferencesRepository.delete(stale));
        assertEquals("updated", preferencesRepository.findById(stale.getId()).orElseThrow().getXml());
    }

    @Test
    public void usesTheIdForAPrimitiveVersionIncludingZero() {
        PrimitiveVersionRecord record = new PrimitiveVersionRecord();
        record.setId(UUID.randomUUID().toString());
        record.setValue("original");
        PrimitiveVersionRecord saved = primitiveVersionRepository.save(record);
        assertEquals(0, saved.getVersion());
        saved.setValue("updated");

        PrimitiveVersionRecord updated = primitiveVersionRepository.save(saved);

        assertEquals(1, updated.getVersion());
        assertEquals("updated", primitiveVersionRepository.findById(record.getId()).orElseThrow().getValue());
    }

    @Test
    public void preservesAssignedIdMergesWithoutAVersion() {
        UnversionedRecord record = new UnversionedRecord();
        record.setId(UUID.randomUUID().toString());
        record.setValue("original");
        UnversionedRecord saved = unversionedRepository.save(record);
        assertNotSame(record, saved);
        saved.setValue("updated");
        unversionedRepository.save(saved);

        assertEquals("updated", unversionedRepository.findById(record.getId()).orElseThrow().getValue());
        unversionedRepository.delete(saved);
        assertTrue(unversionedRepository.findById(record.getId()).isEmpty());
    }

    @Entity
    @Table(name = "TEST_CRUD_PRIMITIVE_VERSION")
    @Getter @Setter
    public static class PrimitiveVersionRecord implements PersistableEntity<String> {
        @Id private String id;
        @Version private int version;
        private String value;
    }

    @Entity
    @Table(name = "TEST_CRUD_UNVERSIONED")
    @Getter @Setter
    public static class UnversionedRecord implements PersistableEntity<String> {
        @Id private String id;
        private String value;
    }

    public static class PrimitiveVersionRepository extends SpringCrudRepositoryImpl<PrimitiveVersionRecord, String> {
    }

    public static class UnversionedRepository extends SpringCrudRepositoryImpl<UnversionedRecord, String> {
    }

    @Configuration
    @EnableTransactionManagement
    @PropertySource("classpath:/hibernate.properties")
    public static class RepositoryTestConfiguration extends SakaiTestConfiguration {

        @Override
        protected AdditionalHibernateMappings getAdditionalHibernateMappings() {
            AdditionalHibernateMappingsImpl mappings = new AdditionalHibernateMappingsImpl();
            mappings.setAnnotatedClasses(new Class<?>[] { PreferencesRecord.class,
                    PrimitiveVersionRecord.class, UnversionedRecord.class });
            return mappings;
        }

        @Bean
        public PreferencesRepository preferencesRepository(SessionFactory sessionFactory) {
            PreferencesRepository repository = new PreferencesRepository();
            repository.setSessionFactory(sessionFactory);
            return repository;
        }

        @Bean
        public PrimitiveVersionRepository primitiveVersionRepository(SessionFactory sessionFactory) {
            PrimitiveVersionRepository repository = new PrimitiveVersionRepository();
            repository.setSessionFactory(sessionFactory);
            return repository;
        }

        @Bean
        public UnversionedRepository unversionedRepository(SessionFactory sessionFactory) {
            UnversionedRepository repository = new UnversionedRepository();
            repository.setSessionFactory(sessionFactory);
            return repository;
        }
    }
}
