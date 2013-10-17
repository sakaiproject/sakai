package org.sakaiproject.taggable.impl;

import java.util.ArrayList;
import java.util.List;

import org.sakaiproject.taggable.api.Evaluation;
import org.sakaiproject.taggable.api.EvaluationContainer;
import org.sakaiproject.taggable.api.URLBuilder;

/**
 * Container object which will hold important bits of information related to evaluations. 
 * @author chrismaurer
 *
 */
public class EvaluationContainerImpl implements EvaluationContainer {

	private URLBuilder addUrlBuilder;
	private List<Evaluation> evaluations = new ArrayList<Evaluation>();
	private boolean canAddEvaluation = false;
	private boolean canHaveEvaluations = false;
	private boolean hideItemLevelEvaluations = false;
	
	public EvaluationContainerImpl() {
		;
	}
	
	public EvaluationContainerImpl(URLBuilder addUrlBuilder) {
		this.addUrlBuilder = addUrlBuilder;
	}
	
	public EvaluationContainerImpl(URLBuilder addUrlBuilder, List<Evaluation> evaluations) {
		this.addUrlBuilder = addUrlBuilder;
		this.evaluations = evaluations;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getAddActionURL() {
		if (addUrlBuilder != null)
			return addUrlBuilder.getURL();
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setAddURLBuilder(URLBuilder addUrlBuilder) {
		this.addUrlBuilder = addUrlBuilder;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isCanAddEvaluation() {
		return canAddEvaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCanAddEvaluation(boolean canAddEvaluation) {
		this.canAddEvaluation = canAddEvaluation;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isCanHaveEvaluations() {
		return canHaveEvaluations;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setCanHaveEvaluations(boolean canHaveEvaluations) {
		this.canHaveEvaluations = canHaveEvaluations;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<Evaluation> getEvaluations() {
		return evaluations;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEvaluations(List<Evaluation> evaluations) {
		this.evaluations = evaluations;
	}

    public boolean isHideItemLevelEvaluations()
    {
        return this.hideItemLevelEvaluations;
    }

    public void setIsHideItemLevelEvaluations(boolean isHideItemLevelEvaluations)
    {
        this.hideItemLevelEvaluations = isHideItemLevelEvaluations;
    }
	
	

}
