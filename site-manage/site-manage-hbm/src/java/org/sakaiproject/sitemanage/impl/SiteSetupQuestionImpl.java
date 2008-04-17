/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/


package org.sakaiproject.sitemanage.impl;

import java.util.Set;
import java.util.HashSet;

import org.sakaiproject.sitemanage.api.model.*;

public class SiteSetupQuestionImpl implements SiteSetupQuestion {
	
	public SiteSetupQuestionImpl()
	{
	}
	
	public SiteSetupQuestionImpl(Set<SiteSetupQuestionAnswer> answers, boolean isMultipleAnswers, String question, boolean required, SiteTypeQuestions siteTypeQuestions)
	{
		this.answers = answers;
		this.isMultipleAnswers = isMultipleAnswers;
		this.question = question;
		this.required = required;
		this.siteTypeQuestions = siteTypeQuestions;
	}
	
	private static final long serialVersionUID = 1L;
	
	private String id;
	
	/**
	 * {@inheritDoc}
	 */
	public String getId()
	{
		return id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setId(String id)
	{
		this.id = id;
	}
	
	private String question;
	
	/**
	 * {@inheritDoc}
	 */
	public String getQuestion()
	{
		return question;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setQuestion(String question)
	{
		this.question = question;
	}

	private Set<SiteSetupQuestionAnswer> answers = new HashSet<SiteSetupQuestionAnswer>();
	/**
	 * {@inheritDoc}
	 */
	public Set<SiteSetupQuestionAnswer> getAnswers()
	{
		return answers;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnswers(Set<SiteSetupQuestionAnswer> answers)
	{
		this.answers = answers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAnswer(SiteSetupQuestionAnswer answer)
	{
		answer.setQuestion(this);
		this.answers.add(answer);
	}
	
	boolean required;
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isRequired()
	{
		return required;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRequired(boolean required)
	{
		this.required = required;
	}

	boolean isMultipleAnswers;
	
	/**
	 * {@inheritDoc}
	 */
	public boolean getIsMultipleAnswers()
	{
		return isMultipleAnswers;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIsMultipleAnswers(boolean isMultipleAnswers)
	{
		this.isMultipleAnswers = isMultipleAnswers;
	}
	
	private SiteTypeQuestions siteTypeQuestions;
	
	/**
	 * {@inheritDoc}
	 */
	public SiteTypeQuestions getSiteTypeQuestions()
	{
		return siteTypeQuestions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSiteTypeQuestions(SiteTypeQuestions siteTypeQuestions)
	{
		this.siteTypeQuestions = siteTypeQuestions;
	}
}
