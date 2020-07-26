/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.assignment.api.conversion;

import java.util.List;

public interface AssignmentConversionService {

    /**
     * Starts the assignments conversion
     *
     * @param numberOfAttributes maximum attributes per element in an xml document, 0 means use default
     * @param lengthOfAttribute maximum characters of an attribute, 0 means use default
     */
    void runConversion(int numberOfAttributes, int lengthOfAttribute);

    Object serializeFromXml(String xml, Class clazz);
}
