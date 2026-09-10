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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessException;

import org.sakaiproject.sitemanage.api.model.SiteSetupQuestion;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionAnswer;
import org.sakaiproject.sitemanage.api.model.SiteSetupQuestionService;
import org.sakaiproject.sitemanage.api.model.SiteSetupUserAnswer;
import org.sakaiproject.sitemanage.api.model.SiteTypeQuestions;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Transactional
public class SiteSetupQuestionServiceImpl implements SiteSetupQuestionService {
	
	@Setter private SessionFactory sessionFactory;

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
	   CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
	   CriteriaQuery<SiteTypeQuestions> cq = cb.createQuery(SiteTypeQuestions.class);
	   Root<SiteTypeQuestionsImpl> root = cq.from(SiteTypeQuestionsImpl.class);
	   cq.select(root);
	   List<SiteTypeQuestions> rvList = sessionFactory.getCurrentSession().createQuery(cq).getResultList();
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
	  CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
	  CriteriaQuery<SiteTypeQuestions> cq = cb.createQuery(SiteTypeQuestions.class);
	  Root<SiteTypeQuestionsImpl> root = cq.from(SiteTypeQuestionsImpl.class);
	  cq.select(root);
	  List<SiteTypeQuestions> qList = sessionFactory.getCurrentSession().createQuery(cq).getResultList();
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
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSetupQuestion> cq = cb.createQuery(SiteSetupQuestion.class);
		Root<SiteSetupQuestionImpl> root = cq.from(SiteSetupQuestionImpl.class);
		cq.select(root);
		return sessionFactory.getCurrentSession().createQuery(cq).getResultList();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public SiteTypeQuestions getSiteTypeQuestions(String siteType)
	{
		SiteTypeQuestions rv = null;
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteTypeQuestions> cq = cb.createQuery(SiteTypeQuestions.class);
		Root<SiteTypeQuestionsImpl> root = cq.from(SiteTypeQuestionsImpl.class);
		cq.select(root).where(cb.equal(root.get("siteType"), siteType));
		List<SiteTypeQuestions> rvList = sessionFactory.getCurrentSession().createQuery(cq).getResultList();
		if (rvList != null && rvList.size() == 1)
		{
			rv = rvList.get(0);
		}
		return rv;
	}
	
	public SiteSetupQuestionAnswer getSiteSetupQuestionAnswer(String answerId)
	{
		CriteriaBuilder cb = sessionFactory.getCurrentSession().getCriteriaBuilder();
		CriteriaQuery<SiteSetupQuestionAnswer> cq = cb.createQuery(SiteSetupQuestionAnswer.class);
		Root<SiteSetupQuestionAnswerImpl> root = cq.from(SiteSetupQuestionAnswerImpl.class);
		cq.select(root).where(cb.equal(root.get("id"), answerId));
		List<SiteSetupQuestionAnswer> rvList = sessionFactory.getCurrentSession().createQuery(cq).getResultList();
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
			sessionFactory.getCurrentSession().saveOrUpdate(q);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().delete(question);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().saveOrUpdate(answer);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().delete(answer);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().saveOrUpdate(siteTypeQuestions);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().delete(siteTypeQuestions);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().saveOrUpdate(siteSetupUserAnswer);
			return true;
		}
		catch (DataAccessException | HibernateException e)
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
			sessionFactory.getCurrentSession().delete(siteSetupUserAnswer);
			return true;
		}
		catch (DataAccessException | HibernateException e)
		{
			log.error("Hibernate could not delete: Site={} user={} question={}", siteSetupUserAnswer.getSiteId(), siteSetupUserAnswer.getUserId(), siteSetupUserAnswer.getQuestionId());
			return false;
		}
	}

}
