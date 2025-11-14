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
package org.sakaiproject.scorm.service.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.adl.sequencer.IValidRequests;
import org.adl.sequencer.SeqActivity;
import org.adl.sequencer.SeqNavRequests;

import org.apache.commons.lang3.StringUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import org.sakaiproject.scorm.model.api.Attempt;
import org.sakaiproject.scorm.model.api.ContentPackage;
import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.scorm.navigation.INavigable;
import org.sakaiproject.scorm.service.api.LearningManagementSystem;
import org.sakaiproject.scorm.model.api.ScoBean;
import org.sakaiproject.scorm.navigation.INavigationEvent;
import org.sakaiproject.scorm.service.api.ScormApplicationService;
import org.sakaiproject.scorm.service.api.ScormContentService;
import org.sakaiproject.scorm.service.api.ScormLaunchService;
import org.sakaiproject.scorm.service.api.ScormResourceService;
import org.sakaiproject.scorm.service.api.ScormResultService;
import org.sakaiproject.scorm.service.api.ScormSequencingService;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchContext;
import org.sakaiproject.scorm.service.api.launch.ScormLaunchState;
import org.sakaiproject.scorm.service.api.launch.ScormNavigationRequest;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeInvocation;
import org.sakaiproject.scorm.service.api.launch.ScormRuntimeResult;
import org.sakaiproject.scorm.service.api.launch.ScormTocEntry;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.api.SessionManager;

@Slf4j
public class ScormLaunchServiceImpl implements ScormLaunchService
{
    private static final String NAV_RESULT_TOC = "_TOC_";
    private static final String NAV_RESULT_INVALID_REQ = "_INVALIDNAVREQ_";
    private static final String NAV_RESULT_END_SESSION = "_ENDSESSION_";
    private static final String NAV_RESULT_SEQ_BLOCKED = "_SEQBLOCKED_";

    @Setter
    private ScormLaunchSessionRegistry sessionRegistry;

    @Setter
    private ScormSequencingService sequencingService;

    @Setter
    private ScormResultService resultService;

    @Setter
    private ScormContentService scormContentService;

    @Setter
    private ScormResourceService resourceService;

    @Setter
    private ScormApplicationService scormApplicationService;

    @Setter
    private LearningManagementSystem learningManagementSystem;

    private SessionManager sessionManager;

    public void setSessionManager(SessionManager sessionManager)
    {
        this.sessionManager = sessionManager;
    }

    @Override
    public ScormLaunchContext openSession(long contentPackageId, Optional<ScormNavigationRequest> navigationRequest, Optional<String> completionUrl)
    {
        Session sakaiSession = requireSession();
        String userId = sakaiSession.getUserId();

        ContentPackage contentPackage = scormContentService.getContentPackage(contentPackageId);
        if (contentPackage == null)
        {
            throw new IllegalArgumentException("No SCORM content package for id " + contentPackageId);
        }

        SessionBean sessionBean = sequencingService.newSessionBean(contentPackage);
        completionUrl.ifPresent(sessionBean::setCompletionUrl);

        int navRequest = prepareLaunch(sessionBean, navigationRequest);

        ScormLaunchState state = ScormLaunchState.READY;
        String message = null;
        String launchPath = null;
        RestResourceNavigator navigator = new RestResourceNavigator();

        if (!learningManagementSystem.canLaunchAttempt(contentPackage, sessionBean.getAttemptNumber()))
        {
            state = ScormLaunchState.DENIED;
            message = "Access denied. You may have exhausted the allowed attempts.";
        }
        else
        {
            LaunchResult outcome = performNavigation(sessionBean, navRequest, navigator);
            state = outcome.state;
            message = outcome.message;
            launchPath = outcome.launchPath;
        }

        String effectiveMessage = resolveMessage(state, message);
        String sessionId = null;
        if (state != ScormLaunchState.DENIED && state != ScormLaunchState.ERROR)
        {
            sessionId = sessionRegistry.register(sessionBean, contentPackage, userId, state, effectiveMessage);
        }

        return buildContext(sessionId, sessionBean, contentPackage, launchPath, state, effectiveMessage);
    }

    @Override
    public Optional<ScormLaunchContext> getSession(String sessionId)
    {
        return sessionRegistry.lookup(sessionId)
            .filter(entry -> StringUtils.equals(requireSession().getUserId(), entry.getUserId()))
            .map(entry -> buildContext(sessionId, entry.getSessionBean(), entry.getContentPackage(), resolveLaunchPath(entry.getSessionBean()).orElse(null), entry.getState(), resolveMessage(entry.getState(), entry.getMessage())));
    }

	@Override
	public ScormLaunchContext navigate(String sessionId, ScormNavigationRequest request)
	{
		ReentrantLock lock = requireSessionLock(sessionId);
		lock.lock();
		try
		{
			ScormLaunchSessionRegistry.Entry entry = getOwnedEntry(sessionId);
			SessionBean sessionBean = entry.getSessionBean();

			RestResourceNavigator navigator = new RestResourceNavigator();
			ScormLaunchState state = ScormLaunchState.READY;
			String message = null;
			String launchPath = null;

			if (request.navigationRequest().isPresent())
			{
				LaunchResult outcome = performNavigation(sessionBean, request.navigationRequest().get(), navigator);
				state = outcome.state;
				message = outcome.message;
				launchPath = outcome.launchPath;
			}
			request.choiceActivityId().ifPresent(choice -> sequencingService.navigate(choice, sessionBean, navigator, null));
			request.targetActivityId().ifPresent(target -> sequencingService.navigateToActivity(target, sessionBean, navigator, null));

			if (launchPath == null)
			{
				launchPath = Optional.ofNullable(navigator.getLastLaunchPath()).orElseGet(() -> resolveLaunchPath(sessionBean).orElse(null));
			}
			if (launchPath == null && state == ScormLaunchState.READY)
			{
				state = ScormLaunchState.CHOICE_REQUIRED;
				if (message == null)
				{
					message = "Select an activity to continue.";
				}
			}
			String effectiveMessage = resolveMessage(state, message);
			sessionRegistry.updateState(sessionId, state, effectiveMessage);
			return buildContext(sessionId, sessionBean, entry.getContentPackage(), launchPath, state, effectiveMessage);
		}
		finally
		{
			lock.unlock();
		}
	}

	@Override
	public ScormRuntimeResult runtime(String sessionId, ScormRuntimeInvocation invocation)
	{
		ReentrantLock lock = requireSessionLock(sessionId);
		lock.lock();
		try
		{
			ScormLaunchSessionRegistry.Entry entry = getOwnedEntry(sessionId);
			SessionBean sessionBean = entry.getSessionBean();
			RestResourceNavigator navigator = new RestResourceNavigator();

			String methodName = invocation.getMethod();
			List<String> args = invocation.getArguments();

			String result = "";
			ScoAwareRuntimeContext runtimeContext = prepareSco(sessionBean, methodName);
			ScoBean originalSco = runtimeContext.previousSco;
			String originalScoId = sessionBean.getScoId();
			ScoBean targetSco = selectScoBean(sessionBean, invocation.getScoId(), originalSco);
			boolean swappedSco = targetSco != null && targetSco != originalSco;
			if (swappedSco)
			{
				sessionBean.setDisplayingSco(targetSco);
				if (StringUtils.isNotBlank(targetSco.getScoId()))
				{
					sessionBean.setScoId(targetSco.getScoId());
				}
			}
			try
			{
				result = executeRuntimeCall(sessionBean, navigator, methodName, args);
			}
			finally
			{
				if (swappedSco)
				{
					sessionBean.setDisplayingSco(originalSco);
					sessionBean.setScoId(originalScoId);
				}
				finalizeSco(sessionBean, methodName, runtimeContext);
			}

			String errorCode = scormApplicationService.getLastError(sessionBean);
			String diagnostic = errorCode != null ? scormApplicationService.getDiagnostic(errorCode, sessionBean) : null;
			String launchPath = navigator.getLastLaunchPath();

			if (launchPath == null)
			{
				launchPath = resolveLaunchPath(sessionBean).orElse(null);
			}

			ScormLaunchState persistedState = sessionBean.isEnded() ? ScormLaunchState.ERROR : ScormLaunchState.READY;
			String persistedMessage = sessionBean.isEnded() ? "Session has ended." : null;
			sessionRegistry.updateState(sessionId, persistedState, persistedMessage);

			return ScormRuntimeResult.builder()
				.value(result)
				.errorCode(errorCode)
				.diagnostic(diagnostic)
				.launchPath(launchPath)
				.sessionEnded(sessionBean.isEnded())
				.build();
		}
		finally
		{
			lock.unlock();
		}
	}

    private String executeRuntimeCall(SessionBean sessionBean, RestResourceNavigator navigator, String methodName, List<String> args)
    {
        String operation = StringUtils.trimToEmpty(methodName);
        ScoBean scoBean = sessionBean.getDisplayingSco();

        switch (operation)
        {
            case "Initialize":
            {
                String parameter = args.isEmpty() ? "" : args.get(0);
                boolean ok = scormApplicationService.initialize(parameter, sessionBean, scoBean);
                return ok ? "true" : "false";
            }
            case "Terminate":
            {
                String parameter = args.isEmpty() ? "" : args.get(0);
                INavigationEvent navigationEvent = scormApplicationService.newNavigationEvent();
                boolean ok = scormApplicationService.terminate(parameter, navigationEvent, sessionBean, scoBean);
                if (ok)
                {
                    if (navigationEvent.isChoiceEvent())
                    {
                        sequencingService.navigate(navigationEvent.getChoiceEvent(), sessionBean, navigator, null);
                    }
                    else
                    {
                        sequencingService.navigate(navigationEvent.getEvent(), sessionBean, navigator, null);
                    }
                }
                return ok ? "true" : "false";
            }
            case "Commit":
            {
                String parameter = args.isEmpty() ? "" : args.get(0);
                boolean ok = scormApplicationService.commit(parameter, sessionBean, scoBean);
                return ok ? "true" : "false";
            }
            case "GetValue":
            {
                String element = args.isEmpty() ? "" : args.get(0);
                return scormApplicationService.getValue(element, sessionBean, scoBean);
            }
            case "SetValue":
            {
                String element = args.size() > 0 ? args.get(0) : "";
                String value = args.size() > 1 ? args.get(1) : "";
                boolean ok = scormApplicationService.setValue(element, value, sessionBean, scoBean);
                return ok ? "true" : "false";
            }
            case "GetLastError":
            {
                String code = scormApplicationService.getLastError(sessionBean);
                return code != null ? code : "0";
            }
            case "GetErrorString":
            {
                String errorCode = args.isEmpty() ? "" : args.get(0);
                String text = scormApplicationService.getErrorString(errorCode, sessionBean);
                return text != null ? text : "";
            }
            case "GetDiagnostic":
            {
                String errorCode = args.isEmpty() ? "" : args.get(0);
                String diagnostic = scormApplicationService.getDiagnostic(errorCode, sessionBean);
                return diagnostic != null ? diagnostic : "";
            }
            default:
                log.warn("Unsupported SCORM runtime method: {}", methodName);
                throw new IllegalArgumentException("Unsupported SCORM runtime method: " + methodName);
        }
    }

	@Override
	public void closeSession(String sessionId)
	{
		ReentrantLock lock = sessionRegistry.getLock(sessionId);
		if (lock != null)
		{
			lock.lock();
		}
		try
		{
			ScormLaunchSessionRegistry.Entry entry = sessionRegistry.lookup(sessionId)
				.filter(e -> StringUtils.equals(requireSession().getUserId(), e.getUserId()))
				.orElse(null);

			if (entry != null)
			{
				Optional.ofNullable(entry.getSessionBean().getDisplayingSco())
					.ifPresent(scoBean -> scormApplicationService.discardScoBean(scoBean.getScoId(), entry.getSessionBean(), new RestResourceNavigator()));
			}
			sessionRegistry.remove(sessionId);
		}
		finally
		{
			if (lock != null)
			{
				lock.unlock();
			}
		}
	}

    private Session requireSession()
    {
        if (sessionManager == null)
        {
            throw new IllegalStateException("SessionManager is not available; configure a SessionManager bean before invoking SCORM launch services.");
        }

        Session session = sessionManager.getCurrentSession();
        if (session == null || StringUtils.isBlank(session.getUserId()))
        {
            throw new IllegalStateException("No active Sakai session");
        }
        return session;
    }

    private ScormLaunchSessionRegistry.Entry getOwnedEntry(String sessionId)
    {
        String userId = requireSession().getUserId();
        return sessionRegistry.lookup(sessionId)
            .filter(entry -> StringUtils.equals(userId, entry.getUserId()))
            .orElseThrow(() -> new IllegalArgumentException("Unknown or unauthorized SCORM session: " + sessionId));
    }

    private int prepareLaunch(SessionBean sessionBean, Optional<ScormNavigationRequest> navigationOverride)
    {
        sessionBean.setAttempt(null);
        sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, null, null);

        IValidRequests navigationState = sessionBean.getNavigationState();
        int navRequest = navigationState != null && navigationState.isStartEnabled() ? SeqNavRequests.NAV_START : SeqNavRequests.NAV_NONE;

        ContentPackage contentPackage = sessionBean.getContentPackage();
        String learnerId = sessionBean.getLearnerId();

        int attemptsCount = resultService.countAttempts(contentPackage.getContentPackageId(), learnerId);
        long attemptNumber;

        if (attemptsCount > 0)
        {
            Attempt latestAttempt = resultService.getNewstAttempt(contentPackage.getContentPackageId(), learnerId);

            if (latestAttempt != null)
            {
                if (latestAttempt.isSuspended())
                {
                    attemptNumber = latestAttempt.getAttemptNumber();
                    sessionBean.setAttempt(latestAttempt);
                    navRequest = SeqNavRequests.NAV_RESUMEALL;
                }
                else if (latestAttempt.isNotExited())
                {
                    // Server crash or unclean exit - continue with the same attempt
                    attemptNumber = latestAttempt.getAttemptNumber();
                    sessionBean.setAttempt(latestAttempt);
                }
                else
                {
                    attemptNumber = latestAttempt.getAttemptNumber() + 1;
                }
            }
            else
            {
                attemptNumber = 1L;
            }
        }
        else
        {
            attemptNumber = 1L;
        }

        sessionBean.setAttemptNumber(attemptNumber);

        if (navigationOverride.isPresent())
        {
            navRequest = navigationOverride.get().navigationRequest().orElse(navRequest);
        }

        return navRequest;
    }

    private LaunchResult performNavigation(SessionBean sessionBean, int navRequest, RestResourceNavigator navigator)
    {
        RestResourceNavigator agent = navigator != null ? navigator : new RestResourceNavigator();
        String result = sequencingService.navigate(navRequest, sessionBean, agent, null);
        if (log.isDebugEnabled())
        {
            log.debug("Navigation outcome for request {}: {}", navRequest, result);
        }

        if (result == null || StringUtils.contains(result, NAV_RESULT_TOC))
        {
            sessionBean.setStarted(true);
            agent.displayResource(sessionBean, null);
            String launchPath = Optional.ofNullable(agent.getLastLaunchPath()).orElseGet(() -> resolveLaunchPath(sessionBean).orElse(null));
            if (StringUtils.isBlank(launchPath))
            {
                IValidRequests navState = sessionBean.getNavigationState();
                if (navState != null && navState.getChoice() != null && navState.getChoice().size() == 1)
                {
                    String choiceId = navState.getChoice().keySet().iterator().next();
                    sequencingService.navigate(choiceId, sessionBean, agent, null);
                    agent.displayResource(sessionBean, null);
                    launchPath = Optional.ofNullable(agent.getLastLaunchPath()).orElseGet(() -> resolveLaunchPath(sessionBean).orElse(null));
                    if (StringUtils.isNotBlank(launchPath))
                    {
                        return new LaunchResult(launchPath, ScormLaunchState.READY, null);
                    }
                }
                return new LaunchResult(null, ScormLaunchState.CHOICE_REQUIRED, "Select an activity to continue.");
            }
            return new LaunchResult(launchPath, ScormLaunchState.READY, null);
        }

        if (StringUtils.equals(result, NAV_RESULT_INVALID_REQ))
        {
            IValidRequests state = sessionBean.getNavigationState();
            if (state != null && state.isSuspendEnabled())
            {
                result = sequencingService.navigate(SeqNavRequests.NAV_SUSPENDALL, sessionBean, agent, null);
                if (StringUtils.equals(result, NAV_RESULT_END_SESSION))
                {
                    result = sequencingService.navigate(SeqNavRequests.NAV_RESUMEALL, sessionBean, agent, null);
                    if (result == null || StringUtils.contains(result, NAV_RESULT_TOC))
                    {
                        sessionBean.setStarted(true);
                        agent.displayResource(sessionBean, null);
                        String launchPath = Optional.ofNullable(agent.getLastLaunchPath()).orElseGet(() -> resolveLaunchPath(sessionBean).orElse(null));
                        if (StringUtils.isBlank(launchPath))
                        {
                            return new LaunchResult(null, ScormLaunchState.CHOICE_REQUIRED, "Select an activity to continue.");
                        }
                        return new LaunchResult(launchPath, ScormLaunchState.READY, null);
                    }
                }
            }

            if (StringUtils.equals(result, NAV_RESULT_INVALID_REQ))
            {
                result = sequencingService.navigate(SeqNavRequests.NAV_ABANDONALL, sessionBean, agent, null);
                if (StringUtils.equals(result, NAV_RESULT_END_SESSION))
                {
                        sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, agent, null);
                        state = sessionBean.getNavigationState();
                        if (state != null && state.isStartEnabled())
                        {
                            result = sequencingService.navigate(SeqNavRequests.NAV_START, sessionBean, agent, null);
                    }
                }
            }
        }
        else if (StringUtils.equals(result, NAV_RESULT_SEQ_BLOCKED))
        {
            result = sequencingService.navigate(SeqNavRequests.NAV_NONE, sessionBean, agent, null);
        }

        if (result == null || StringUtils.contains(result, NAV_RESULT_TOC))
        {
            sessionBean.setStarted(true);
            agent.displayResource(sessionBean, null);
            String launchPath = Optional.ofNullable(agent.getLastLaunchPath()).orElseGet(() -> resolveLaunchPath(sessionBean).orElse(null));
            if (StringUtils.isBlank(launchPath))
            {
                return new LaunchResult(null, ScormLaunchState.CHOICE_REQUIRED, "Select an activity to continue.");
            }
            return new LaunchResult(launchPath, ScormLaunchState.READY, null);
        }

        return new LaunchResult(null, ScormLaunchState.ERROR, "Unable to launch SCORM session: " + result);
    }

	private Optional<String> resolveLaunchPath(SessionBean sessionBean)
	{
		if (sessionBean == null || sessionBean.getLaunchData() == null)
		{
			return Optional.empty();
		}

        String resourceId = sessionBean.getContentPackage().getResourceId();
        String launchLine = sessionBean.getLaunchData().getLaunchLine();

        if (StringUtils.isBlank(launchLine) || StringUtils.isBlank(resourceId))
        {
            return Optional.empty();
        }

        String decodedLaunchLine = java.net.URLDecoder.decode(launchLine, java.nio.charset.StandardCharsets.UTF_8);
        String decodedResourceId = java.net.URLDecoder.decode(resourceId, java.nio.charset.StandardCharsets.UTF_8);

        String resourcePath = resourceService.getResourcePath(StringUtils.removeStart(decodedResourceId, "/"), StringUtils.removeStart(decodedLaunchLine, "/"));
        if (StringUtils.isBlank(resourcePath))
        {
            return Optional.empty();
        }

        resourcePath = StringUtils.removeStart(resourcePath, "/");

        return Optional.of("contentpackages/resourceName/" + resourcePath);
	}

	private ScoBean selectScoBean(SessionBean sessionBean, String requestedScoId, ScoBean fallbackSco)
	{
		if (StringUtils.isBlank(requestedScoId))
		{
			return fallbackSco;
		}

		Map<String, ScoBean> scoBeans = sessionBean.getScoBeans();
		ScoBean existing = scoBeans != null ? scoBeans.get(requestedScoId) : null;
		if (existing != null)
		{
			if (existing.isTerminated())
			{
				log.debug("Discarding terminated ScoBean {} before reinitialization", requestedScoId);
				if (scoBeans != null)
				{
					scoBeans.remove(requestedScoId);
				}
				existing = null;
			}
			else
			{
			return existing;
		}
		}

		return scormApplicationService.produceScoBean(requestedScoId, sessionBean);
	}

	private ScoAwareRuntimeContext prepareSco(SessionBean sessionBean, String methodName)
	{
		ScoAwareRuntimeContext context = new ScoAwareRuntimeContext();
		context.previousSco = sessionBean.getDisplayingSco();

		if (StringUtils.equalsIgnoreCase("Initialize", methodName))
		{
			// Ensure the current SCO bean exists before initialization without creating placeholders.
			scormApplicationService.produceScoBean(sessionBean.getScoId(), sessionBean);
		}

		return context;
	}

    private void finalizeSco(SessionBean sessionBean, String methodName, ScoAwareRuntimeContext context)
    {
        if (StringUtils.equalsIgnoreCase("Terminate", methodName) && context.previousSco != null)
        {
            scormApplicationService.discardScoBean(context.previousSco.getScoId(), sessionBean, new RestResourceNavigator());
        }
    }

    private ScormLaunchContext buildContext(String sessionId, SessionBean sessionBean, ContentPackage contentPackage, String launchPath, ScormLaunchState state, String message)
    {
        String effectiveMessage = resolveMessage(state, message);
        List<ScormTocEntry> tocEntries = buildTocEntries(sessionBean);
        boolean hasMultipleLaunchables = hasMultipleLaunchableEntries(tocEntries);
        boolean showToc = shouldShowToc(contentPackage, state, hasMultipleLaunchables);
        List<ScormTocEntry> effectiveToc = showToc ? tocEntries : Collections.emptyList();
        return ScormLaunchContext.builder()
            .sessionId(sessionId)
            .sessionBean(sessionBean)
            .launchPath(launchPath)
            .contentPackage(contentPackage)
            .showToc(showToc)
            .showLegacyControls(contentPackage.isShowNavBar())
            .tocEntries(effectiveToc)
            .currentActivityId(StringUtils.trimToNull(sessionBean.getActivityId()))
            .currentScoId(StringUtils.trimToNull(sessionBean.getScoId()))
            .state(state)
            .message(effectiveMessage)
            .build();
    }

	private String resolveMessage(ScormLaunchState state, String message)
	{
		if (state == null)
		{
			return message;
		}
		if (state == ScormLaunchState.DENIED && message == null)
		{
			return "Access denied. You may have exhausted the allowed attempts.";
		}
		if (state == ScormLaunchState.CHOICE_REQUIRED && message == null)
		{
			return "Select an activity to continue.";
		}
		return message;
	}

    private boolean shouldShowToc(ContentPackage contentPackage, ScormLaunchState state, boolean hasMultipleLaunchables)
    {
        if (contentPackage != null && contentPackage.isShowTOC())
        {
            return true;
        }

        if (state == ScormLaunchState.CHOICE_REQUIRED)
        {
            return true;
        }

        return hasMultipleLaunchables;
    }

    private List<ScormTocEntry> buildTocEntries(SessionBean sessionBean)
    {
        if (sessionBean == null)
        {
            return Collections.emptyList();
        }

        TreeModel treeModel = sequencingService.getTreeModel(sessionBean);
        if (treeModel == null)
        {
            return Collections.emptyList();
        }

        Object root = treeModel.getRoot();
        if (!(root instanceof DefaultMutableTreeNode))
        {
            return Collections.emptyList();
        }

        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) root;
        String currentActivityId = StringUtils.trimToNull(sessionBean.getActivityId());
        ScormTocEntry rootEntry = toTocEntry(rootNode, currentActivityId);

        if (StringUtils.isBlank(rootEntry.getActivityId()) && rootEntry.getChildren() != null && !rootEntry.getChildren().isEmpty())
        {
            return new ArrayList<>(rootEntry.getChildren());
        }

        return Collections.singletonList(rootEntry);
    }

    private boolean hasMultipleLaunchableEntries(List<ScormTocEntry> entries)
    {
        return countLaunchableEntries(entries) > 1;
    }

    private int countLaunchableEntries(List<ScormTocEntry> entries)
    {
        if (entries == null || entries.isEmpty())
        {
            return 0;
        }
        int count = 0;
        for (ScormTocEntry entry : entries)
        {
            if (entry == null)
            {
                continue;
            }
            if (entry.isLeaf() && StringUtils.isNotBlank(entry.getActivityId()))
            {
                count++;
            }
            count += countLaunchableEntries(entry.getChildren());
        }
        return count;
    }

    private ScormTocEntry toTocEntry(DefaultMutableTreeNode node, String currentActivityId)
    {
        SeqActivity activity = node != null && node.getUserObject() instanceof SeqActivity ? (SeqActivity) node.getUserObject() : null;
        String activityId = activity != null ? StringUtils.trimToNull(activity.getID()) : null;
        String title = activity != null ? decodeTitle(activity.getTitle()) : null;
        boolean isLeaf = node != null && node.isLeaf();

        ScormTocEntry.ScormTocEntryBuilder builder = ScormTocEntry.builder()
            .activityId(activityId)
            .title(StringUtils.defaultIfBlank(title, activityId != null ? activityId : "(Untitled activity)"))
            .leaf(isLeaf)
            .current(activityId != null && activityId.equals(currentActivityId));

        if (node != null)
        {
            Enumeration<TreeNode> children = node.children();
            while (children.hasMoreElements())
            {
                Object next = children.nextElement();
                if (next instanceof DefaultMutableTreeNode)
                {
                    builder.child(toTocEntry((DefaultMutableTreeNode) next, currentActivityId));
                }
            }
        }

        return builder.build();
    }

    private String decodeTitle(String encodedTitle)
    {
        if (encodedTitle == null)
        {
            return null;
        }
        try
        {
            return URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8);
        }
        catch (IllegalArgumentException e)
        {
            log.debug("Unable to decode SCORM activity title [{}]", encodedTitle, e);
            return encodedTitle;
        }
    }

	private ReentrantLock requireSessionLock(String sessionId)
	{
		ReentrantLock lock = sessionRegistry.getLock(sessionId);
		if (lock == null)
		{
			throw new IllegalArgumentException("Unknown or expired SCORM session: " + sessionId);
		}
		return lock;
	}

    private static final class LaunchResult
    {
        private final String launchPath;
        private final ScormLaunchState state;
        private final String message;

        private LaunchResult(String launchPath, ScormLaunchState state, String message)
        {
            this.launchPath = launchPath;
            this.state = state;
            this.message = message;
        }
    }

    private static final class ScoAwareRuntimeContext
    {
        private ScoBean previousSco;
    }

    private class RestResourceNavigator implements INavigable
    {
        @Getter
        private String lastLaunchPath;

        @Override
        public void displayResource(SessionBean sessionBean, Object target)
        {
            lastLaunchPath = resolveLaunchPath(sessionBean).orElse(null);
        }
    }

}
