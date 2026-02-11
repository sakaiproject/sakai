/*
 *
 * $URL$
 * $Id$
 *
 * Copyright (c) 2025 Charles R. Severance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.lti.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declarative field metadata for form generation, validation, archive, and persistence.
 * <p>
 * Replaces the Foorm model string format with strongly typed attributes.
 * Multi-valued attributes like {@link #choices()} and {@link #fields()} use arrays to preserve order.
 * </p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FoormField {
	/** Canonical field name (archive XML element, DB column). */
	String value();

	/** Field type. */
	FoormType type() default FoormType.TEXT;

	/** Label key for UI. */
	String label() default "";

	/** Whether the field is required. */
	boolean required() default false;

	/** Max length (0 = no limit). */
	int maxlength() default 0;

	/** Whether the field is included in archive XML. */
	boolean archive() default false;

	/** Whether the field is hidden in forms. */
	boolean hidden() default false;

	/** Role required to edit (e.g. "admin"). */
	String role() default "";

	/** Whether the field is read-only. */
	boolean readonly() default false;

	/** Whether the field is persisted (false for computed/transient). */
	boolean persist() default true;

	/** For radio: ordered choices. */
	String[] choices() default {};

	/** For header: ordered child field names. */
	String[] fields() default {};

	/** For textarea: rows. */
	int rows() default 0;

	/** For textarea: cols. */
	int cols() default 0;

	/** Whether shown in advanced section. */
	boolean advanced() default false;
}
