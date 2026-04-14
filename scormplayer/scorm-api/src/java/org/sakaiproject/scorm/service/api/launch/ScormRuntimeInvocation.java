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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.NonNull;

/**
 * Descriptor for a SCORM runtime API invocation.
 */
@Getter
public class ScormRuntimeInvocation
{
    /** Runtime API method name (e.g. Initialize, GetValue, SetValue). */
    @NonNull
    private final String method;

    /** Ordered list of string arguments passed to the runtime method. */
    @NonNull
    private final List<String> arguments;

    /** SCO identifier to target for this runtime call (optional). */
    private final String scoId;

    /** Score/completion hints embedded at Terminate (Xerte workaround for beforeunload XHR blocking). */
    private final String hintScoreScaled;
    private final String hintCompletionStatus;
    private final String hintSuccessStatus;

    @JsonCreator
    public ScormRuntimeInvocation(
        @JsonProperty("method") @NonNull String method,
        @JsonProperty("arguments") @NonNull List<String> arguments,
        @JsonProperty("scoId") String scoId,
        @JsonProperty("hintScoreScaled") String hintScoreScaled,
        @JsonProperty("hintCompletionStatus") String hintCompletionStatus,
        @JsonProperty("hintSuccessStatus") String hintSuccessStatus)
    {
        this.method = method;
        this.arguments = List.copyOf(arguments);
        this.scoId = scoId;
        this.hintScoreScaled = hintScoreScaled;
        this.hintCompletionStatus = hintCompletionStatus;
        this.hintSuccessStatus = hintSuccessStatus;
    }
}
