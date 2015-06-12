/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
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

package org.sakaiproject.pasystem.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Value;


/**
 * A collection of validation errors.
 */
public class Errors {

    @Value
    static class Error {
        private String field;
        private String errorCode;
    }

    private final List<Error> errors;

    public Errors() {
        errors = new ArrayList<Error>();
    }

    public void addError(String field, String code) {
        errors.add(new Error(field, code));
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    /**
     * Combine the errors in this object with the errors in another.
     */
    public Errors merge(Errors other) {
        errors.addAll(other.toList());
        return this;
    }

    public List<Error> toList() {
        return new ArrayList<Error>(errors);
    }

    public Map<String, String> toMap() {
        return errors.stream().collect(Collectors.toMap(Error::getField, Error::getErrorCode));
    }

}
