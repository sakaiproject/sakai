/******************************************************************************
 * Copyright 2023 sakaiproject.org Licensed under the Educational
 * Community License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.sakaiproject.webapi.exception;

public class ForbiddenAccessException extends RuntimeException {
    public ForbiddenAccessException() {
        super();
    }

    public ForbiddenAccessException(Throwable cause) {
        super(cause);
    }

    public ForbiddenAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenAccessException(String message) {
        super(message);
    }
}
