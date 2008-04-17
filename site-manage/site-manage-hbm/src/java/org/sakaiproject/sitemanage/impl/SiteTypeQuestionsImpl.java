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

public class SiteTypeQuestionsImpl implements SiteTypeQuestions
{
	
	private static final long serialVersionUID = 1L;
	
	public SiteTypeQuestionsImpl()
	{
		
	}
	
	public SiteTypeQuestionsImpl(String instruction, Set<SiteSetupQuestion> questions, String siteType, String siteTypeId)
	{
		this.instruction = instruction;
		this.questions = questions;
		this.siteType = siteType;
		this.siteTypeId = siteTypeId;
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
	
	private String siteTypeId;
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteTypeId()
	{
		return siteTypeId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSiteTypeId(String siteTypeId)
	{
		this.siteTypeId = siteTypeId;
	}
	
	private String siteType;
	
	/**
	 * {@inheritDoc}
	 */
	public String getSiteType()
	{
		return siteType;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setSiteType(String siteType)
	{
		this.siteType = siteType;
	}

	private Set<SiteSetupQuestion> questions = new HashSet<SiteSetupQuestion>();
	/**
	 * {@inheritDoc}
	 */
	public Set<SiteSetupQuestion> getQuestions()
	{
		return questions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setQuestions(Set<SiteSetupQuestion> questions)
	{
		this.questions = questions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addQuestion(SiteSetupQuestion question)
	{
		question.setSiteTypeQuestions(this);
		this.questions.add(question);
	}
	
	private String instruction;
	
	/**
	 * {@inheritDoc}
	 */
	public String getInstruction()
	{
		return instruction;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setInstruction(String instruction)
	{
		this.instruction = instruction;
	}
	
	private String url;
	/**
	 * {@inheritDoc}
	 */
	public String getUrl()
	{
		return url;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setUrl(String url)
	{
		this.url = url;
	}
}
