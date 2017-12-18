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

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.sitemanage.api.model.SiteSetupQuestion;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionAnswer;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService;
import org.sakaiproject.sitemanage.api.model.SiteSetupUserAnswer;
import org.sakaiproject.sitemanage.api.model.SiteTypeQuestions;

@Slf4j
public class SiteSetupQuestionServiceImpl extends HibernateDaoSupport implements SiteSetupQuestionService {
	
	private static final String QUERY_ANY_SITETYPE_QUESTIONS = "findAnySiteTypeQuestions";
	
	private static final String QUERY_ALL_QUESTIONS = "findAllSiteSetupQuestions";
	
	private static final String QUERY_QUESTIONS_BY_SITETYPE = "findQuestionsBySiteType";
	
	private static final String QUERY_ANSWER_BY_ID = "findAnswerById";

	/**
	 * Init
	 */
   public void init()
   {
      log.info("init()");
   }
   
   /**
    * Destroy
    */
   public void destroy()
   {
      log.info("destroy()");
   }
   
   /**
	 * {@inheritDoc}
	 */
   public boolean hasAnySiteTypeQuestions()
   {
	   List<SiteTypeQuestions> rvList = (List<SiteTypeQuestions>) getHibernateTemplate().findByNamedQuery(QUERY_ANY_SITETYPE_QUESTIONS);
	   if (rvList != null && !rvList.isEmpty())
	   {
		   return true;
	   }
	   return false;
   }
   
   /**
	 * {@inheritDoc}
	 */
  public void removeAllSiteTypeQuestions()
  {
	  List<SiteTypeQuestions> qList = (List<SiteTypeQuestions>) getHibernateTemplate().findByNamedQuery(QUERY_ANY_SITETYPE_QUESTIONS);
	  if (qList != null && !qList.isEmpty())
	  {
		  for(SiteTypeQuestions q : qList)
		  {
			  removeSiteTypeQuestions(q);
		  }
	  }
  }
   
   /**
	 * {@inheritDoc}
	 */
	public List<SiteSetupQuestion> getAllSiteQuestions()
	{
		return (List<SiteSetupQuestion>) getHibernateTemplate().findByNamedQuery(QUERY_ALL_QUESTIONS);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SiteTypeQuestions getSiteTypeQuestions(String siteType)
	{
		SiteTypeQuestions rv = null;
		List<SiteTypeQuestions> rvList = (List<SiteTypeQuestions>) getHibernateTemplate().findByNamedQueryAndNamedParam(QUERY_QUESTIONS_BY_SITETYPE, "siteType", siteType);
		if (rvList != null && rvList.size() == 1)
		{
			rv = rvList.get(0);
		}
		return rv;
	}
	
	public SiteSetupQuestionAnswer getSiteSetupQuestionAnswer(String answerId)
	{
		List<SiteSetupQuestionAnswer> rvList = (List<SiteSetupQuestionAnswer>) getHibernateTemplate().findByNamedQueryAndNamedParam(QUERY_ANSWER_BY_ID, "id", answerId);
		if (rvList != null && rvList.size() == 1)
		{
			return rvList.get(0);
		}
		return null;
	}

	/*********** SiteSetupQuestion **************/
	
	/**
	 * {@inheritDoc}
	 */
	public SiteSetupQuestion newSiteSetupQuestion()
	{
		SiteSetupQuestion question = new SiteSetupQuestionImpl();
		
		return question;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean saveSiteSetupQuestion(SiteSetupQuestion q)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(q);
			return true;
		}
		catch (DataAccessException e)
		{
			log.warn(this + ".saveSiteSetupQuestion() Hibernate could not save. question=" + q.getQuestion());
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeSiteSetupQuestion(SiteSetupQuestion question)
	{
		try 
		{
			//org.hibernate.LockMode cannot be resolved. It is indirectly referenced from required .class files.
			getHibernateTemplate().delete(question);
			return true;
		}
		catch (DataAccessException e)
		{
			log.error("Hibernate could not delete: question={}", question.getQuestion(), e);
			return false;
		}
	}
	
	/********* SiteSetupQuestionAnswer ***************/
	
	/**
	 * {@inheritDoc}
	 */
	public SiteSetupQuestionAnswer newSiteSetupQuestionAnswer()
	{
		SiteSetupQuestionAnswer answer = new SiteSetupQuestionAnswerImpl();
		
		return answer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean saveSiteSetupQuestionAnswer(SiteSetupQuestionAnswer answer)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(answer);
			return true;
		}
		catch (DataAccessException e)
		{
		 	log.warn("Hibernate could not save. answer={}", answer.getAnswer(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeSiteSetupQuestionAnswer(SiteSetupQuestionAnswer answer)
	{
		try 
		{
			//org.hibernate.LockMode cannot be resolved. It is indirectly referenced from required .class files.
			getHibernateTemplate().delete(answer);
			return true;
		}
		catch (DataAccessException e)
		{
			log.error("Hibernate could not delete: answer={}", answer.getAnswer(), e);
			return false;
		}
	}

	/************ SiteTypeQuestions *******************/
	
	/**
	 * {@inheritDoc}
	 */
	public SiteTypeQuestions newSiteTypeQuestions()
	{
		SiteTypeQuestions questions = new SiteTypeQuestionsImpl();
		
		return questions;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean saveSiteTypeQuestions(SiteTypeQuestions siteTypeQuestions)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(siteTypeQuestions);
			return true;
		}
		catch (DataAccessException e)
		{
		 	log.warn("Hibernate could not save. siteType={}", siteTypeQuestions.getSiteType());
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeSiteTypeQuestions(SiteTypeQuestions siteTypeQuestions)
	{
		try 
		{
			//org.hibernate.LockMode cannot be resolved. It is indirectly referenced from required .class files.
			getHibernateTemplate().delete(siteTypeQuestions);
			return true;
		}
		catch (DataAccessException e)
		{
			log.error("Hibernate could not delete: siteType={}", siteTypeQuestions.getSiteType(), e);
			return false;
		}
	}
	
	/************ SiteSetupUserAnswer *******************/
	
	/**
	 * {@inheritDoc}
	 */
	public SiteSetupUserAnswer newSiteSetupUserAnswer()
	{
		SiteSetupUserAnswer uAnswer = new SiteSetupUserAnswerImpl();
		
		return uAnswer;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean saveSiteSetupUserAnswer(SiteSetupUserAnswer siteSetupUserAnswer)
	{
		try 
		{
			getHibernateTemplate().saveOrUpdate(siteSetupUserAnswer);
			return true;
		}
		catch (DataAccessException e)
		{
		 	log.warn("Hibernate could not save. Site={} user={} question={}", siteSetupUserAnswer.getSiteId(), siteSetupUserAnswer.getUserId(), siteSetupUserAnswer.getQuestionId(), e);
			return false;
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean removeSiteSetupUserAnswer(SiteSetupUserAnswer siteSetupUserAnswer)
	{
		try 
		{
			//org.hibernate.LockMode cannot be resolved. It is indirectly referenced from required .class files.
			getHibernateTemplate().delete(siteSetupUserAnswer);
			return true;
		}
		catch (DataAccessException e)
		{
			log.error("Hibernate could not delete: Site={} user={} question={}", siteSetupUserAnswer.getSiteId(), siteSetupUserAnswer.getUserId(), siteSetupUserAnswer.getQuestionId());
			return false;
		}
	}

}
