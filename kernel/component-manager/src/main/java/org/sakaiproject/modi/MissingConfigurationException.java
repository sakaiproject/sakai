/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.modi;

import java.nio.file.Path;

/**
 * Thrown to indicate that a required configuration file is missing. This should halt Sakai startup because missing
 * defaults or core customizations could cause startup that creates or modifies content in unexpected ways.
 */
public class MissingConfigurationException extends RuntimeException {
    public MissingConfigurationException(Path path) {
        super(String.format("Cannot boot Sakai; missing supplied startup configuration file: %s", path));
    }
}
