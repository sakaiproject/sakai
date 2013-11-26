package org.sakaiproject.scoringservice.api;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/21/13
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
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
