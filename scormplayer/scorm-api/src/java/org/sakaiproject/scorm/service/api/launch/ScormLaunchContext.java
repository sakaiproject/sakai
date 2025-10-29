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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;

/**
 * Immutable projection of the state required to launch and monitor a SCORM session.
 */
@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class ScormLaunchContext
{
    /**
     * Opaque identifier for the launch session when one has been established.
     * Absent when launch access is denied or an unrecoverable error occurs.
     */
    private final String sessionId;

    /** Current SCORM session bean backing sequencing and runtime state. */
    @NonNull
    private final SessionBean sessionBean;

    /**
     * Resolved launch path suitable for embedding in an iframe, when available.
     * This is typically a relative Sakai resource URL (e.g. contentpackages/resourceName/...).
     */
    private final String launchPath;

    /** Associated content package metadata. */
    @NonNull
    private final ContentPackage contentPackage;

    /** Indicates whether a Table of Contents should be rendered. */
    private final boolean showToc;

    /** Indicates whether the legacy SCORM button bar should be displayed. */
    private final boolean showLegacyControls;

    /** Overall launch state for callers to interpret. */
    @NonNull
    @Builder.Default
    private final ScormLaunchState state = ScormLaunchState.READY;

    /** Optional human-readable message associated with the current state. */
    private final String message;
}
