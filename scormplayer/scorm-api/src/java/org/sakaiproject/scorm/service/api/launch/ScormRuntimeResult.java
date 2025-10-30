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

/**
 * Result returned after invoking a SCORM runtime method.
 */
@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class ScormRuntimeResult
{
    /** Raw return value from the runtime method invocation. */
    @NonNull
    private final String value;

    /** Runtime error code captured after invocation (equivalent to GetLastError). */
    private final String errorCode;

    /** Diagnostic information for the captured error code, if any. */
    private final String diagnostic;

    /** Updated launch path if sequencing selected a new SCO, otherwise null. */
    private final String launchPath;

    /** Indicates whether the session has transitioned to an ended state. */
    private final boolean sessionEnded;
}
