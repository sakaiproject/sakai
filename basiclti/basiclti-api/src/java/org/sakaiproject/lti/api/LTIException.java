/**
 * Copyright (c) 2011-2012 The Apereo Foundation
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
package org.sakaiproject.lti.api;

/**
 * Created by IntelliJ IDEA.
 * User: jbush
 * Date: 2/15/12
 * Time: 5:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class LTIException extends Exception {
    private String errorKey;

    public LTIException(String errorKey, String errorMessage) {
        super(errorMessage);
        this.errorKey = errorKey;
    }


    public LTIException(String errorKey, String errorMessage, Exception exception) {
        super(errorMessage, exception);
        this.errorKey = errorKey;
    }

    public String getErrorKey() {
        return errorKey;
    }
}
