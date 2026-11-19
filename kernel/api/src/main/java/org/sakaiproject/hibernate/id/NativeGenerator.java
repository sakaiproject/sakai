/**
 * Copyright (c) 2003-2026 The Apereo Foundation
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
package org.sakaiproject.hibernate.id;

import java.lang.reflect.Member;
import java.util.EnumSet;
import java.util.Properties;

import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;

import org.hibernate.MappingException;
import org.hibernate.boot.model.relational.Database;
import org.hibernate.boot.model.relational.ExportableProducer;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.Generator;
import org.hibernate.generator.GeneratorCreationContext;
import org.hibernate.generator.OnExecutionGenerator;
import org.hibernate.id.Configurable;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.IdentityGenerator;
import org.hibernate.id.PersistentIdentifierGenerator;
import org.hibernate.id.PostInsertIdentityPersister;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.id.insert.InsertGeneratedIdentifierDelegate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;

/**
 * Generator that picks a strategy based on the {@linkplain Dialect#getNativeIdentifierGeneratorStrategy() dialect}.
 * <p>
 * This is a backport of {@code org.hibernate.id.NativeGenerator} introduced in Hibernate 7.0, adapted to the
 * Hibernate 6.6 generator SPI. It keeps the Hibernate 7 shape so moving to Hibernate 7 only requires swapping
 * the annotation import and deleting this class.
 * <ul>
 * <li>Dialects with identity column support (MariaDB, MySQL) get an {@code AUTO_INCREMENT} identity column.</li>
 * <li>Other dialects (Oracle, PostgreSQL) get a sequence configured from {@code sequenceForm}.</li>
 * </ul>
 * Differences from Hibernate 7: the {@code TABLE} and {@code UUID} native strategies do not exist in the
 * Hibernate 6.6 dialect SPI, so {@code tableForm} is ignored.
 */
public class NativeGenerator
        implements OnExecutionGenerator, BeforeExecutionGenerator, Configurable, ExportableProducer,
        AnnotationBasedGenerator<org.sakaiproject.hibernate.annotations.NativeGenerator> {

    private GenerationType generationType;
    private org.sakaiproject.hibernate.annotations.NativeGenerator annotation;
    private Generator dialectNativeGenerator;

    public GenerationType getGenerationType() {
        return generationType;
    }

    @Override
    public void initialize(org.sakaiproject.hibernate.annotations.NativeGenerator nativeGenerator, Member member,
                           GeneratorCreationContext context) {
        annotation = nativeGenerator;
        // the 6.6 equivalent of Dialect#getNativeValueGenerationStrategy() in Hibernate 7
        final String strategy = context.getDatabase().getDialect().getNativeIdentifierGeneratorStrategy();
        if ("identity".equals(strategy)) {
            generationType = GenerationType.IDENTITY;
            // Hibernate 6.6 only flags the column as identity for its own legacy generators, not custom ones
            context.getProperty().getValue().getColumns().get(0).setIdentity(true);
            dialectNativeGenerator = new IdentityGenerator();
        } else {
            generationType = GenerationType.SEQUENCE;
            dialectNativeGenerator = new SequenceStyleGenerator();
        }
    }

    @Override
    public void configure(Type type, Properties parameters, ServiceRegistry serviceRegistry) throws MappingException {
        if (dialectNativeGenerator instanceof SequenceStyleGenerator sequenceStyleGenerator) {
            applyProperties(parameters, annotation.sequenceForm());
            sequenceStyleGenerator.configure(type, parameters, serviceRegistry);
        } else if (dialectNativeGenerator instanceof Configurable configurable) {
            configurable.configure(type, parameters, serviceRegistry);
        }
    }

    @Override
    public void registerExportables(Database database) {
        if (dialectNativeGenerator instanceof ExportableProducer exportableProducer) {
            exportableProducer.registerExportables(database);
        }
    }

    @Override
    public void initialize(SqlStringGenerationContext context) {
        if (dialectNativeGenerator instanceof Configurable configurable) {
            configurable.initialize(context);
        }
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return dialectNativeGenerator.getEventTypes();
    }

    @Override
    public boolean generatedOnExecution() {
        return dialectNativeGenerator.generatedOnExecution();
    }

    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner, Object currentValue,
                           EventType eventType) {
        return ((BeforeExecutionGenerator) dialectNativeGenerator).generate(session, owner, currentValue, eventType);
    }

    @Override
    public boolean referenceColumnsInSql(Dialect dialect) {
        return ((OnExecutionGenerator) dialectNativeGenerator).referenceColumnsInSql(dialect);
    }

    @Override
    public boolean writePropertyValue() {
        return ((OnExecutionGenerator) dialectNativeGenerator).writePropertyValue();
    }

    @Override
    public String[] getReferencedColumnValues(Dialect dialect) {
        return ((OnExecutionGenerator) dialectNativeGenerator).getReferencedColumnValues(dialect);
    }

    @Override
    public InsertGeneratedIdentifierDelegate getGeneratedIdentifierDelegate(PostInsertIdentityPersister persister) {
        return ((OnExecutionGenerator) dialectNativeGenerator).getGeneratedIdentifierDelegate(persister);
    }

    /**
     * Same parameter mapping Hibernate applies to a {@link SequenceGenerator} in
     * {@code GenerationStrategyInterpreter#interpretSequenceGenerator}.
     */
    private void applyProperties(Properties properties, SequenceGenerator sequenceAnnotation) {
        properties.put(IdentifierGenerator.GENERATOR_NAME, sequenceAnnotation.name());
        if (!sequenceAnnotation.catalog().isEmpty()) {
            properties.put(PersistentIdentifierGenerator.CATALOG, sequenceAnnotation.catalog());
        }
        if (!sequenceAnnotation.schema().isEmpty()) {
            properties.put(PersistentIdentifierGenerator.SCHEMA, sequenceAnnotation.schema());
        }
        if (!sequenceAnnotation.sequenceName().isEmpty()) {
            properties.put(SequenceStyleGenerator.SEQUENCE_PARAM, sequenceAnnotation.sequenceName());
        }
        properties.put(SequenceStyleGenerator.INCREMENT_PARAM, String.valueOf(sequenceAnnotation.allocationSize()));
        properties.put(SequenceStyleGenerator.INITIAL_PARAM, String.valueOf(sequenceAnnotation.initialValue()));
    }
}
