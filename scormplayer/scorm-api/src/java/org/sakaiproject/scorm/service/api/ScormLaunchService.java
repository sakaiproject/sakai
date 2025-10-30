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
package org.sakaiproject.scorm.service.api;

import java.util.Optional;

import org.sakaiproject.scorm.service.api.launch.ScormLaunchContext;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchState;
import org.sakaiproject.scorm.service.api.launch.ScormNavigationRequest;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeInvocation;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeResult;

/**
 * Entry point for launching SCORM 2004 packages via RESTful workflows.
 * Implementations are responsible for orchestrating sequencing, runtime calls,
 * and lifecycle management of {@link org.sakaiproject.scorm.model.api.SessionBean} instances.
 */
public interface ScormLaunchService
{
    /**
     * Create or resume a SCORM launch session for the current user.
     *
     * <p>The resulting context includes a {@code sessionId} only when a session has been registered. When
     * {@link ScormLaunchContext#getState()} returns {@link ScormLaunchState#READY} (or {@link ScormLaunchState#CHOICE_REQUIRED})
     * the caller may proceed with runtime and navigation requests using the provided identifier. For
     * {@link ScormLaunchState#DENIED} or {@link ScormLaunchState#ERROR}, no session is registered and the identifier will be null.
     * Active sessions are automatically expired by the {@link org.sakaiproject.scorm.service.impl.ScormLaunchSessionRegistry}
     * after its configured time-to-live (two hours by default) or can be removed eagerly via {@link #closeSession(String)}.
     *
     * @param contentPackageId the numeric identifier of the content package
     * @param request optional launch navigation override (e.g., force start/resume)
     * @param completionUrl optional URL to redirect learners to upon completion/terminate
     * @return a populated launch context describing the session state
     */
    ScormLaunchContext openSession(long contentPackageId, Optional<ScormNavigationRequest> request, Optional<String> completionUrl);

    /**
     * Retrieve an existing launch context by session identifier.
     *
     * @param sessionId the opaque launch session identifier
     * @return optional launch context if found and owned by the current user
     */
    Optional<ScormLaunchContext> getSession(String sessionId);

    /**
     * Process a SCORM navigation request (continue, previous, choice, etc.).
     *
     * @param sessionId the launch session identifier
     * @param request navigation request descriptor
     * @return updated launch context after navigation has been applied
     */
    ScormLaunchContext navigate(String sessionId, ScormNavigationRequest request);

    /**
     * Invoke a SCORM 2004 runtime API call.
     *
     * @param sessionId the launch session identifier
     * @param invocation runtime invocation descriptor
     * @return runtime response including return value and error metadata
     */
    ScormRuntimeResult runtime(String sessionId, ScormRuntimeInvocation invocation);

    /**
     * Terminate and discard a launch session.
     *
     * @param sessionId the launch session identifier
     */
    void closeSession(String sessionId);
}
