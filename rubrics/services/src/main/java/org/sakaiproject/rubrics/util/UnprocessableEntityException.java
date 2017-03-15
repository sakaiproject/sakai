/**********************************************************************************
 *
 * Copyright (c) 2017 The Sakai Foundation
 *
 * Original developers:
 *
 *   Unicon
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.rubrics.util;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception to be thrown by any event handlers, validators or controllers when a provided resource contains bad
 * state such as invalid associations.
 *
 * This should be handled and return an HTTP 422 Unprocessable Entity response.
 */
@ResponseStatus(value= HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableEntityException extends RuntimeException {

    public UnprocessableEntityException() { }

    public UnprocessableEntityException(String msg) {
        super(msg);
    }

    public UnprocessableEntityException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

    public UnprocessableEntityException(Throwable throwable) {
        super(throwable);
    }

}
