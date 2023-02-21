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
 * Thrown if a component directory is attempted to be loaded, but does not conform to the expectations.
 * <p>
 * See {@link TraditionalComponent} for the expectations for traditional, on-disk components. There are no other
 * component types defined at this time.
 */
public class MalformedComponentException extends Exception {
    public MalformedComponentException(Path path, String problem) {
        super(String.format("Malformed component at: %s -- %s", path, problem));
    }
}
