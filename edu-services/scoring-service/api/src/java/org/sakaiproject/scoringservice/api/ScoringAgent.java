package org.sakaiproject.scoringservice.api;

import org.springframework.core.Ordered;

/**
 * Created with IntelliJ IDEA.
 * User: jbush
 * Date: 5/21/13
 * Time: 1:48 PM
 * To change this template use File | Settings | File Templates.
 */
public interface ScoringAgent extends Ordered {
    String getAgentId();
    String getName();

    /**
     * Get the score if one exists for the given scoring agent, gradebookItem, and student.
     * This is a backend call, not typically made from the UI.
     * @param gradebookItemId
     * @param studentId
     * @return
     */
    String getScore(String gradebookUid, String gradebookItemId, String studentId);

    /**
     * check if the given scoring agent supports scoring components or not
     * @return
     */
    boolean hasScoringComponent();

    /**
     * Get the scoring component registered for this scoring agent and gradebookItem, if there
     * is one.  This is a backend call, not typically invoked from the UI.
     * @param gradebookItemId
     * @return
     */
    ScoringComponent getScoringComponent(String gradebookUid, String gradebookItemId);


    /**
     * The user interface code will call this method to get a url that will be click on
     * to launch the user into the external app that provides the scoring use case.
     * @param gradebookItemId - the gradebookItem we are working with
     * @param studentId - the student we are going to grade
     * @return
     */
    String getScoreLaunchUrl(String gradebookUid, String gradebookItemId, String studentId);


    /**
     * The user interface code will call this method to get a url that can be clicked on
     * to launch into the scoring component selection use case in the external app.
     * Scoring component selection is optional.  If hasScoringComponent() is false, this
     * method will not be called for any given ScoringAgent.
     * @param gradebookItemId
     * @return
     */
    String getScoringComponentLaunchUrl(String gradebookUid, String gradebookItemId);

    /**
      * The user interface code will call this method to get a url that can be clicked on
      * to launch into the view score use case in the external app for a given student.
      *
      * @param gradebookUid - the gradebook we are working with
      * @param gradebookItemId - the gradebook item we are working with
      * @param studentId - the student we are going to grade
      * @return
      */
    String getViewScoreLaunchUrl(String gradebookUid, String gradebookItemId, String studentId);

    /**
     * The user interface code will call this method to get a url that will be click on
     * to launch the user into the external app that provides the scoring use case.  Does not
     * launch to any specific student but for the whole roster.
     * @param gradebookUid - the gradebook we are working with
     * @param gradebookItemId - the gradebookItem we are working with
     * @return
     */
    String getScoreLaunchUrl(String gradebookUid, String gradebookItemId);

    boolean isEnabled(String gradebookUid, String gradebookItemId);

}
