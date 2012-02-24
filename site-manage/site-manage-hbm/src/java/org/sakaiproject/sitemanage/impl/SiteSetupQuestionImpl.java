/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
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


package org.sakaiproject.sitemanage.impl;

import java.util.List;
import java.util.Vector;

import org.sakaiproject.sitemanage.api.model.*;

public class SiteSetupQuestionImpl implements SiteSetupQuestion {
	
	public SiteSetupQuestionImpl()
	{
	}
	
	public SiteSetupQuestionImpl(List<SiteSetupQuestionAnswer> answers, boolean isMultipleAnswers, String question, boolean required, SiteTypeQuestions siteTypeQuestions, Integer orderNum, String current)
	{
		this.answers = answers;
		this.isMultipleAnswers = isMultipleAnswers;
		this.question = question;
		this.required = required;
		this.siteTypeQuestions = siteTypeQuestions;
		this.orderNum = orderNum;
		this.current = current;
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

	private List<SiteSetupQuestionAnswer> answers = new Vector<SiteSetupQuestionAnswer>();
	/**
	 * {@inheritDoc}
	 */
	public List<SiteSetupQuestionAnswer> getAnswers()
	{
		return answers;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnswers(List<SiteSetupQuestionAnswer> answers)
	{
		this.answers = answers;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addAnswer(SiteSetupQuestionAnswer answer)
	{
		// update the order number
		answer.setOrderNum(this.getAnswers().size());
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
	
	private Integer orderNum;
	
	/**
	 * {@inheritDoc}
	 */
	public Integer getOrderNum()
	{
		return orderNum;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setOrderNum(Integer orderNum)
	{
		this.orderNum = orderNum;
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
	
	private String current="true";

	/**
	 * {@inheritDoc}
	 * @return
	 */
	public String getCurrent()
	{
		return current;
	}
	
	/**
	 * {@inheritDoc}
	 * @param current
	 */
	public void setCurrent(String current)
	{
		this.current = current;
	}
}
