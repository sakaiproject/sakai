package org.sakaiproject.taggable.impl;

import java.util.Date;

import org.sakaiproject.taggable.api.Evaluation;
import org.sakaiproject.taggable.api.URLBuilder;

public class EvaluationImpl implements Evaluation {

	private String siteId; 
	private String siteTitle;
	private Date lastModDate;
	private String createdById;
	private String createdByName;
	private String evalItemTitle;
	private String evalItemURL;

	private URLBuilder editUrlBuilder;
	private URLBuilder removeUrlBuilder;
	
	private boolean canViewEvaluation;
	private boolean canModifyEvaluation;
	private boolean canRemoveEvaluation;

	public EvaluationImpl() {
		;
	}
	
	public EvaluationImpl(URLBuilder editUrlBuilder, URLBuilder removeUrlBuilder) {
		this.editUrlBuilder = editUrlBuilder;
		this.removeUrlBuilder = removeUrlBuilder;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSiteId() {
		return siteId;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getSiteTitle() {
		return siteTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public Date getLastModDate() {
		return lastModDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCreatedById() {
		return createdById;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCreatedByName() {
		return createdByName;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEvalItemTitle() {
		return evalItemTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEvalItemURL() {
		return evalItemURL;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEvalItemURL(String evalItemURL) {
		this.evalItemURL = evalItemURL;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getEditActionURL() {
		if (editUrlBuilder != null)
			return editUrlBuilder.getURL();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRemoveActionURL() {
		if (removeUrlBuilder != null)
			return removeUrlBuilder.getURL();
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSiteId(String siteId) {
		this.siteId = siteId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSiteTitle(String siteTitle) {
		this.siteTitle = siteTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setLastModDate(Date lastModDate) {
		this.lastModDate = lastModDate;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCreatedById(String createdById) {
		this.createdById = createdById;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCreatedByName(String createdByName) {
		this.createdByName = createdByName;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setEvalItemTitle(String evalItemTitle) {
		this.evalItemTitle = evalItemTitle;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCanViewEvaluation(boolean canViewEvaluation) {
		this.canViewEvaluation = canViewEvaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCanViewEvaluation() {
		return canViewEvaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCanModifyEvaluation() {
		return canModifyEvaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCanModifyEvaluation(boolean canModifyEvaluation) {
		this.canModifyEvaluation = canModifyEvaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isCanRemoveEvaluation() {
		return canRemoveEvaluation;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCanRemoveEvaluation(boolean canRemoveEvaluation) {
		this.canRemoveEvaluation = canRemoveEvaluation;
	}
}
