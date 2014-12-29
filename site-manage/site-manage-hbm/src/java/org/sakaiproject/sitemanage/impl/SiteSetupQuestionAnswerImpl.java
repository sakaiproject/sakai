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

import org.sakaiproject.sitemanage.api.model.*;

public class SiteSetupQuestionAnswerImpl implements SiteSetupQuestionAnswer{
	
	private static final long serialVersionUID = 1L;
	
	public SiteSetupQuestionAnswerImpl()
	{
		
	}
	
	public SiteSetupQuestionAnswerImpl(String answer, String answerString, boolean isFillInBlank, Integer orderNum)
	{
		this.answer = answer;
		this.answerString = answerString;
		this.isFillInBlank = isFillInBlank;
		this.orderNum = orderNum;
	}
	
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
	
	private boolean isFillInBlank;
	
	/**
	 * {@inheritDoc}
	 */
	public boolean getIsFillInBlank()
	{
		return isFillInBlank;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIsFillInBlank(boolean isFillInBlank)
	{
		this.isFillInBlank = isFillInBlank;
	}
	
	private String answer;

	/**
	 * {@inheritDoc}
	 */
	public String getAnswer()
	{
		return answer;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnswer(String answer)
	{
		this.answer = answer;
	}

	private String answerString;
	
	/**
	 * {@inheritDoc}
	 */
	public String getAnswerString()
	{
		return answerString;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAnswerString(String answerString)
	{
		this.answerString = answerString;
	}

	private SiteSetupQuestion question;
	
	/**
	 * {@inheritDoc}
	 */
	public SiteSetupQuestion getQuestion()
	{
		return question;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setQuestion(SiteSetupQuestion question)
	{
		this.question = question;
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
}
