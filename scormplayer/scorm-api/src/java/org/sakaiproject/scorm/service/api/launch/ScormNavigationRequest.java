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

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Encapsulates a sequencing navigation request. Supports the integer-based SCORM navigation
 * requests (start, continue, suspend, etc.) as well as choice navigation targeting a specific
 * activity identifier.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScormNavigationRequest
{
    /** Integer navigation request (SeqNavRequests.*). */
    private final Integer navigationRequest;

    /** Activity identifier for choice navigation requests. */
    private final String choiceActivityId;

    /** Explicit activity identifier for navigateToActivity. */
    private final String targetActivityId;

    public static ScormNavigationRequest ofRequest(int navigationRequest)
    {
        return new ScormNavigationRequest(navigationRequest, null, null);
    }

    public static ScormNavigationRequest ofChoice(String choiceActivityId)
    {
        return new ScormNavigationRequest(null, choiceActivityId, null);
    }

    public static ScormNavigationRequest ofTarget(String targetActivityId)
    {
        return new ScormNavigationRequest(null, null, targetActivityId);
    }

    @JsonCreator
    public static ScormNavigationRequest fromJson(
        @JsonProperty("navigationRequest") Integer navigationRequest,
        @JsonProperty("choiceActivityId") String choiceActivityId,
        @JsonProperty("targetActivityId") String targetActivityId)
    {
        int nonNullCount = 0;
        if (navigationRequest != null)
        {
            nonNullCount++;
        }
        if (choiceActivityId != null)
        {
            nonNullCount++;
        }
        if (targetActivityId != null)
        {
            nonNullCount++;
        }
        if (nonNullCount == 0)
        {
            throw new IllegalArgumentException("At least one navigation attribute must be provided");
        }
        if (nonNullCount > 1)
        {
            throw new IllegalArgumentException("Only one navigation attribute may be provided");
        }
        return new ScormNavigationRequest(navigationRequest, choiceActivityId, targetActivityId);
    }

    public Optional<Integer> navigationRequest()
    {
        return Optional.ofNullable(navigationRequest);
    }

    public Optional<String> choiceActivityId()
    {
        return Optional.ofNullable(choiceActivityId);
    }

    public Optional<String> targetActivityId()
    {
        return Optional.ofNullable(targetActivityId);
    }
}
