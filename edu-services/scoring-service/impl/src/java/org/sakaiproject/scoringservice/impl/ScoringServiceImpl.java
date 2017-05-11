package org.sakaiproject.scoringservice.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringComponent;
import org.sakaiproject.scoringservice.api.ScoringService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.OrderComparator;

/**
 * Sakai's implementation of the ScoringService.  Keeps an in memory
 * list of ScoringAgents, and delegates calls to the agents as necessary.
 */
public class ScoringServiceImpl implements ScoringService {
    private Map<String, ScoringAgent> scoringAgentMap = new HashMap<>();
    private List<ScoringAgent> sortedScoringAgents = new ArrayList<>();
    private ScoringAgent defaultScoringAgent;

    private static final Logger log = LoggerFactory.getLogger(ScoringServiceImpl.class);

    public void init(){
    }

    public void register(ScoringAgent agent, boolean isDefault) {
        if (agent == null) {
            throw new RuntimeException("can't register a null agent");
        }
        if (agent.getAgentId() == null) {
            throw new RuntimeException("the scoring agentId is null");
        }
        scoringAgentMap.put(agent.getAgentId(), agent);
        sortedScoringAgents = new ArrayList<ScoringAgent>(scoringAgentMap.values());
        Collections.sort(sortedScoringAgents, new OrderComparator());

        if (isDefault) {
            defaultScoringAgent = agent;
        }
    }

    @Override
    public void register(ScoringAgent agent) {
        if (agent == null) {
            throw new RuntimeException("can't register a null agent");
        }
        if (agent.getAgentId() == null) {
            throw new RuntimeException("the scoring agentId is null");
        }
        scoringAgentMap.put(agent.getAgentId(), agent);
        sortedScoringAgents = new ArrayList<ScoringAgent>(scoringAgentMap.values());
        Collections.sort(sortedScoringAgents, new OrderComparator());
    }

    @Override
    public String getScoreLaunchUrl(String agentId, String gradebookUid, String gradebookItemId, String studentId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.getScoreLaunchUrl(gradebookUid, gradebookItemId, studentId);
    }

    @Override
    public String getScoringComponentLaunchUrl(String agentId, String gradebookUid, String gradebookItemId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.getScoringComponentLaunchUrl(gradebookUid, gradebookItemId);
    }

    @Override
    public String getScore(String agentId, String gradebookUid, String gradebookItemId, String studentId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.getScore(gradebookUid, gradebookItemId, studentId);

    }

    @Override
    public ScoringComponent getScoringComponent(String agentId, String gradebookUid, String gradebookItemId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        if (!scoringAgent.hasScoringComponent())  {
            return null;
        }
        return scoringAgent.getScoringComponent(gradebookUid, gradebookItemId);
    }

    public ScoringAgent getAgentById(String agentId) {
        ScoringAgent scoringAgent = scoringAgentMap.get(agentId);
        if (scoringAgent == null) {
            throw new RuntimeException("can't locate an agent with a null agentId");
        }
        return scoringAgent;
    }

    @Override
    public boolean hasScoringComponent(String agentId, String gradebookUid, String gradebookItemId) {
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.hasScoringComponent();
    }

    public boolean isScoringAgentEnabled(String agentId, String gradebookUid, String gradebookItemId){
        ScoringAgent scoringAgent = getAgentById(agentId);
        return scoringAgent.isEnabled( gradebookUid,  gradebookItemId);
    }

    public ScoringAgent getDefaultScoringAgent() {
        return defaultScoringAgent;
    }

    @Override
    public List<ScoringAgent> getScoringAgents() {
        return sortedScoringAgents;
    }
}
