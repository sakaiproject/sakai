/**
 * Copyright (c) 2003-2012 The Apereo Foundation
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

package org.sakaiproject.service.gradebook.shared;

/**
 * An exception thrown by the gradebook application.  If a gradebook
 * client does not want to handle each of the gradebook's specific exceptions
 * individually, it can simply deal with GradebookException to handle all possible
 * exceptions.
 *
 * These were changed to runtime exceptions after the 2.1 release to make
 * it easier to throw them while using Hibernate. By default, Spring will
 * mark a transaction as rollback-only when a runtime exception is thrown
 * by a proxied method but will leave the transaction alone when a checked
 * exception is thrown. To preserve the original transaction-preserving
 * behavior, this exception class is explicitly called out in the Spring
 * TransactionProxyFactoryBean configuration file using the following syntax:
 *
 * <prop key="create*">PROPAGATION_REQUIRED,+org.sakaiproject.service.gradebook.shared.GradebookException</prop>
 */
public class GradebookException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GradebookException(final String message) {
        super(message);
    }

	public GradebookException(final Throwable t) {
        super(t);
    }
}



