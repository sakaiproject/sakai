/**********************************************************************************
 * Copyright (c) 2026 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.samigo.api.search;

/**
 * Thrown when a question search cannot be executed: search is disabled, the query is rejected
 * by the search engine, or the search engine cannot be reached.
 */
public class QuestionSearchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public QuestionSearchException(String message) {
        super(message);
    }

    public QuestionSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
