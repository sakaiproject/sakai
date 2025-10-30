/*
 * Copyright (c) 2025 The Apereo Foundation
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
package org.sakaiproject.scorm.service.api.launch;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

/**
 * Table-of-contents projection used by the REST launcher.
 */
@Getter
@Builder
public class ScormTocEntry
{
    /**
     * Sequencing activity identifier.
     */
    private final String activityId;

    /**
     * Human-readable activity title.
     */
    private final String title;

    /**
     * Whether this entry represents a leaf activity (SCO or asset).
     */
    private final boolean leaf;

    /**
     * True when this entry corresponds to the SCORM activity currently being delivered.
     */
    private final boolean current;

    /**
     * Nested child entries mirroring the sequencing tree.
     */
    @Singular
    private final List<ScormTocEntry> children;
}
