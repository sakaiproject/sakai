package org.sakaiproject.taggable.api;

import java.util.List;

/**
 * Container object which will hold important bits of information related to evaluations.
 * @author chrismaurer
 *
 */
public interface EvaluationContainer {

	
	/**
	 * Get the url that will be used when adding a new evaluation
	 * @return
	 */
	public String getAddActionURL();

	/**
	 * Determine if the current user is allowed to add an evaluation
	 * @return
	 */
	public boolean isCanAddEvaluation();
	
	/**
	 * Setter 
	 * @param canAddEvaluation
	 */
	public void setCanAddEvaluation(boolean canAddEvaluation);
	
	/**
	 * Setter
	 * @param addUrlBuilder
	 */
	public void setAddURLBuilder(URLBuilder addUrlBuilder);
	
	/**
	 * Get the list of Evaluations
	 * @return
	 */
	public List<Evaluation> getEvaluations();
	
	/**
	 * Setter
	 * @param evaluations
	 */
	public void setEvaluations(List<Evaluation> evaluations);
	
	/**
	 * Are evaluations allowed at all?
	 * @return
	 */
	public boolean isCanHaveEvaluations();
	
	/**
	 * Setter
	 * @param canHaveEvaluations
	 */
	public void setCanHaveEvaluations(boolean canHaveEvaluations);
	
    /**
     * Do we hide Evaluations from student/participant?
     * @return
     */
    public boolean isHideItemLevelEvaluations();
    
    /**
     * Setter
     * @param isHideEvaluations
     */
    public void setIsHideItemLevelEvaluations(boolean isHideItemLevelEvaluations);
	
}
