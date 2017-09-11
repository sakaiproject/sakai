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
 * indicates that there was an attempt to associate an assignment with an invalid
 * category. This could be because the category does not exist or is not associated
 * with the assignment's gradebook.
 *
 */
public class InvalidCategoryException extends GradebookException {
    public InvalidCategoryException(String message) {
        super(message);
    }
}
