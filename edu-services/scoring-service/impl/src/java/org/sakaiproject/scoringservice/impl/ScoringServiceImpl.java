/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.scoringservice.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.OrderComparator;

import org.sakaiproject.scoringservice.api.ScoringAgent;
import org.sakaiproject.scoringservice.api.ScoringComponent;
import org.sakaiproject.scoringservice.api.ScoringService;

/**
 * Sakai's implementation of the ScoringService.  Keeps an in memory
 * list of ScoringAgents, and delegates calls to the agents as necessary.
 */
public class ScoringServiceImpl implements ScoringService {
    private Map<String, ScoringAgent> scoringAgentMap = new HashMap<>();
    private List<ScoringAgent> sortedScoringAgents = new ArrayList<>();
    private ScoringAgent defaultScoringAgent;

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
