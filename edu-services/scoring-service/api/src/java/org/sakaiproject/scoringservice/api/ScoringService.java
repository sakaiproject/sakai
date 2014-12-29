/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scoringservice.api;

import java.util.List;

/**
 * The ScoringService is a manager of ScoringAgents in the system.  Implementors of ScoringAgents
 * should register with this service.  The ScoringService also provides consumers (typically at the UI
 * level) information about where to launch, or whether the agent has a scoring component, etc.
 */
public interface ScoringService {
    ScoringAgent getDefaultScoringAgent();

    boolean isScoringAgentEnabled(String agentId, String gradebookUid, String gradebookItemId);

    void register(ScoringAgent agent, boolean isDefault);

    /**
     * Register a scoring agent with this service.
     * @param agent
     */
    void register(ScoringAgent agent);

    /**
     * The user interface code will call this method to get a url that will be click on
     * to launch the user into the external app that provides the scoring use case.
     * @param agentId - the selected ScoringAgent we are working with
     * @param gradebookItemId - the gradebookItem we are working with
     * @param studentId - the student we are going to grade
     * @return
     */
    String getScoreLaunchUrl(String agentId, String gradebookUid, String gradebookItemId, String studentId);

    /**
     * The user interface code will call this method to get a url that can be clicked on
     * to launch into the scoring component selection use case in the external app.
     * Scoring component selection is optional.  If hasScoringComponent() is false, this
     * method will not be called for any given ScoringAgent.
     * @param agentId
     * @param gradebookItemId
     * @return
     */
    String getScoringComponentLaunchUrl(String agentId, String gradebookUid, String gradebookItemId);

    /**
     * Get the score if one exists for the given scoring agent, gradebookItem, and student.
     * This is a backend call, not typically made from the UI.
     * @param agentId
     * @param gradebookItemId
     * @param studentId
     * @return
     */
    String getScore(String agentId, String gradebookUid, String gradebookItemId, String studentId);

    /**
     * Get the scoring component registered for this scoring agent and gradebookItem, if there
     * is one.  This is a backend call, not typically invoked from the UI.
     * @param agentId
     * @param gradebookUid
     * @param gradebookItemId
     * @return
     */
    ScoringComponent getScoringComponent(String agentId, String gradebookUid, String gradebookItemId);

    /**
     * get the scoring agent for the given id
     * @param agentId
     * @return
     */
    ScoringAgent getAgentById(String agentId);

    /**
     * check if the given scoring agent supports scoring components or not
     * @param agentId
     * @param gradebookItemId
     * @return
     */
    boolean hasScoringComponent(String agentId, String gradebookUid, String gradebookItemId);

    /**
     * get a list of scoring agents in order.  The UI will cycle thru these in order
     * for selecting an agent for any given gradebook item.
     */
    List<ScoringAgent> getScoringAgents();
}
