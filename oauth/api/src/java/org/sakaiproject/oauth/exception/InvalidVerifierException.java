/*
 * #%L
 * OAuth API
 * %%
 * Copyright (C) 2009 - 2013 Sakai Foundation
 * %%
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
 * #L%
 */
package org.sakaiproject.oauth.exception;

/**
 * Exception thrown when the given verifier doesn't match the expected verifier.
 *
 * @author Colin Hebert
 */
public class InvalidVerifierException extends OAuthException {
    public InvalidVerifierException() {
    }

    public InvalidVerifierException(Throwable cause) {
        super(cause);
    }

    public InvalidVerifierException(String message) {
        super(message);
    }

    public InvalidVerifierException(String message, Throwable cause) {
        super(message, cause);
    }
}
