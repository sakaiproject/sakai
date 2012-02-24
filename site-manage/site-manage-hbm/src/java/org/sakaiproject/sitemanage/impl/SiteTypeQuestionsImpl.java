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

public class SiteTypeQuestionsImpl implements SiteTypeQuestions
{
	
	private static final long serialVersionUID = 1L;
	
	public SiteTypeQuestionsImpl()
	{
		
	}
	
	public SiteTypeQuestionsImpl(String instruction, List<SiteSetupQuestion> questions, String siteType, String siteTypeId, String url, String urlLabel, String urlTarget)
	{
		this.instruction = instruction;
		this.questions = questions;
		this.siteType = siteType;
		this.siteTypeId = siteTypeId;
		this.url = url;
		this.urlLabel = urlLabel;
		this.urlTarget = urlTarget;
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

	private List<SiteSetupQuestion> questions = new Vector<SiteSetupQuestion>();
	/**
	 * {@inheritDoc}
	 */
	public List<SiteSetupQuestion> getQuestions()
	{
		return questions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setQuestions(List<SiteSetupQuestion> questions)
	{
		this.questions = questions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addQuestion(SiteSetupQuestion question)
	{
		// update order number
		if (this.questions == null)
		{
			this.questions = new Vector<SiteSetupQuestion>();
		}
		question.setOrderNum(this.questions.size());
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
	
	private String urlTarget = "_new";
	/**
	 * get the URL target
	 * @return
	 */
	public String getUrlTarget()
	{
		return urlTarget;
	}
	
	/**
	 * set the URL target
	 * @param url
	 */
	public void setUrlTarget(String urlTarget)
	{
		this.urlTarget = urlTarget;
	}
	
	private String urlLabel;
	
	/**
	 * get the URL label
	 * @return
	 */
	public String getUrlLabel()
	{
		return urlLabel;
	}
	
	/**
	 * set the URL
	 * @param url
	 */
	public void setUrlLabel(String urlLabel)
	{
		this.urlLabel = urlLabel;
	}
}
