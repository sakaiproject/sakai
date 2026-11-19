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
package org.sakaiproject.hibernate.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.hibernate.annotations.IdGeneratorType;

import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.TableGenerator;
/**
 * Generator that picks a strategy based on the dialect: identity columns where the database supports them
 * (MariaDB, MySQL), a sequence otherwise (Oracle, PostgreSQL).
 * <p>
 * This is a backport of {@code org.hibernate.annotations.NativeGenerator} introduced in Hibernate 7.0, which
 * replaces the {@code hibernate.id.new_generator_mappings=false} behaviour that was removed in Hibernate 6.
 * Once Sakai moves to Hibernate 7 this annotation should be replaced by the Hibernate one by changing the import.
 */
@Target({METHOD, FIELD, TYPE})
@Retention(RUNTIME)
@IdGeneratorType(org.sakaiproject.hibernate.id.NativeGenerator.class)
public @interface NativeGenerator {

    /**
     * Configures the sequence generation when the dialect reports
     * {@linkplain jakarta.persistence.GenerationType#SEQUENCE} as its native generator.
     */
    SequenceGenerator sequenceForm() default @SequenceGenerator();

    /**
     * Configures the table generation when the dialect reports
     * {@linkplain jakarta.persistence.GenerationType#TABLE} as its native generator.
     */
    TableGenerator tableForm() default @TableGenerator();
}
