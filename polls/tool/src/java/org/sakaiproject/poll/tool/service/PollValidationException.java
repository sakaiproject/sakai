/**********************************************************************************
 * Copyright (c) 2025 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.service;

public class PollValidationException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final Object[] args;

    public PollValidationException(String messageKey, Object... args) {
        super(messageKey);
        this.args = args;
    }

    public PollValidationException(String messageKey, Throwable cause, Object... args) {
        super(messageKey, cause);
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }
}
