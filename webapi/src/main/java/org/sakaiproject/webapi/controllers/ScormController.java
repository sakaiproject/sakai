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
package org.sakaiproject.webapi.controllers;

import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.adl.sequencer.IValidRequests;

import org.apache.commons.lang3.StringUtils;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.service.api.ScormLaunchService;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchContext;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchState;
import org.sakaiproject.scorm.service.api.launch.ScormNavigationRequest;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeInvocation;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeResult;
import org.sakaiproject.scorm.service.api.launch.ScormTocEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@RestController
@RequestMapping(path = "/scorm", produces = MediaType.APPLICATION_JSON_VALUE)
public class ScormController extends AbstractSakaiApiController {

    @Autowired
    private ScormLaunchService scormLaunchService;

    @PostMapping(path = "/sessions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScormSessionResponse createSession(@RequestBody ScormSessionRequest request) {

        checkSakaiSession();
        if (request.getContentPackageId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "contentPackageId must be positive");
        }

        Optional<ScormNavigationRequest> navRequest = request.navigationRequest()
            .map(ScormNavigationRequest::ofRequest);

        try {
            ScormLaunchContext context = scormLaunchService.openSession(request.getContentPackageId(), navRequest, Optional.ofNullable(StringUtils.trimToNull(request.getCompletionUrl())));
            return toResponse(context);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }

    @GetMapping(path = "/sessions/{sessionId}")
    public ScormSessionResponse getSession(@PathVariable String sessionId) {

        checkSakaiSession();

        return scormLaunchService.getSession(sessionId)
            .map(this::toResponse)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "SCORM session not found"));
    }

    @PostMapping(path = "/sessions/{sessionId}/nav", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScormSessionResponse navigate(@PathVariable String sessionId, @RequestBody ScormNavigationPayload payload) {

        checkSakaiSession();

        ScormNavigationRequest request = buildNavigationRequest(payload)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Navigation request payload is empty"));

        try {
            ScormLaunchContext context = scormLaunchService.navigate(sessionId, request);
            return toResponse(context);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @PostMapping(path = "/sessions/{sessionId}/runtime", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ScormRuntimeResponse runtime(@PathVariable String sessionId, @RequestBody ScormRuntimePayload payload) {
        checkSakaiSession();

        if (StringUtils.isBlank(payload.getMethod())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Runtime method is required");
        }

        ScormRuntimeInvocation invocation = new ScormRuntimeInvocation(
            payload.getMethod(),
            Optional.ofNullable(payload.getArguments()).orElse(List.of()),
            payload.getScoId());

        try {
            ScormRuntimeResult result = scormLaunchService.runtime(sessionId, invocation);
            return new ScormRuntimeResponse(result.getValue(), result.getErrorCode(), result.getDiagnostic(), result.getLaunchPath(), result.isSessionEnded());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    @DeleteMapping(path = "/sessions/{sessionId}")
    public void closeSession(@PathVariable String sessionId) {

        checkSakaiSession();
        scormLaunchService.closeSession(sessionId);
    }

    private ScormSessionResponse toResponse(ScormLaunchContext context) {

        SessionBean sessionBean = context.getSessionBean();
        IValidRequests nav = sessionBean.getNavigationState();

        NavigationState navigationState = NavigationState.builder()
            .start(nav != null && nav.isStartEnabled())
            .resume(nav != null && nav.isResumeEnabled())
            .previous(nav != null && nav.isPreviousEnabled())
            .next(nav != null && nav.isContinueEnabled())
            .suspend(nav != null && nav.isSuspendEnabled())
            .choice(nav != null && nav.getChoice() != null && !nav.getChoice().isEmpty())
            .build();

        return ScormSessionResponse.builder()
            .sessionId(context.getSessionId())
            .contentPackageId(context.getContentPackage().getContentPackageId())
            .contentPackageTitle(context.getContentPackage().getTitle())
            .attemptNumber(sessionBean.getAttemptNumber())
            .launchPath(context.getLaunchPath())
            .completionUrl(sessionBean.getCompletionUrl())
            .showLegacyControls(context.isShowLegacyControls())
            .showToc(context.isShowToc())
            .state(context.getState())
            .message(context.getMessage())
            .navigation(navigationState)
            .toc(context.getTocEntries())
            .currentActivityId(context.getCurrentActivityId())
            .currentScoId(context.getCurrentScoId())
            .build();
    }

    private Optional<ScormNavigationRequest> buildNavigationRequest(ScormNavigationPayload payload) {

        if (payload == null) {
            return Optional.empty();
        }

        if (payload.getNavigationRequest() != null) {
            return Optional.of(ScormNavigationRequest.ofRequest(payload.getNavigationRequest()));
        }
        if (StringUtils.isNotBlank(payload.getChoiceActivityId())) {
            return Optional.of(ScormNavigationRequest.ofChoice(payload.getChoiceActivityId()));
        }
        if (StringUtils.isNotBlank(payload.getTargetActivityId())) {
            return Optional.of(ScormNavigationRequest.ofTarget(payload.getTargetActivityId()));
        }

        return Optional.empty();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ScormSessionRequest {

        private long contentPackageId;
        private Integer navigationRequest;
        private String completionUrl;

        public Optional<Integer> navigationRequest() {
            return Optional.ofNullable(navigationRequest);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ScormNavigationPayload {

        private Integer navigationRequest;
        private String choiceActivityId;
        private String targetActivityId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class ScormRuntimePayload {

        private String method;
        private List<String> arguments;
        private String scoId;
    }

    @Data
    @Builder
    private static class NavigationState {

        private final boolean start;
        private final boolean resume;
        private final boolean previous;
        private final boolean next;
        private final boolean suspend;
        private final boolean choice;
    }

    @Data
    @Builder
    @JsonInclude(Include.NON_NULL)
    private static class ScormSessionResponse {

        private final String sessionId;
        private final long contentPackageId;
        private final String contentPackageTitle;
        private final long attemptNumber;
        private final String launchPath;
        private final String completionUrl;
        private final boolean showLegacyControls;
        private final boolean showToc;
        private final ScormLaunchState state;
        private final String message;
        private final NavigationState navigation;
        private final List<ScormTocEntry> toc;
        private final String currentActivityId;
        private final String currentScoId;
    }

    private record ScormRuntimeResponse(String value, String errorCode, String diagnostic, String launchPath, boolean sessionEnded) { }
}
