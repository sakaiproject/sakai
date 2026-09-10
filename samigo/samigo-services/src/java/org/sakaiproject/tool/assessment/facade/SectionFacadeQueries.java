/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008, 2009 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.facade;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
public class SectionFacadeQueries implements SectionFacadeQueriesAPI {

  @Setter private SessionFactory sessionFactory;

  public SectionFacadeQueries () {
  }

  public IdImpl getId(String id) {
    return new IdImpl(id);
  }

  public IdImpl getId(Long id) {
    return new IdImpl(id);
  }

  public IdImpl getId(long id) {
    return new IdImpl(id);
  }

  public SectionFacade get(Long sectionId) {
      SectionData section = (SectionData) sessionFactory.getCurrentSession().get(SectionData.class, sectionId);
      return new SectionFacade(section);
  }

  public SectionData load(Long sectionId) {
      return (SectionData) sessionFactory.getCurrentSession().get(SectionData.class, sectionId);
  }

  public void addSectionMetaData(Long sectionId, String label, String value) {
    Session session = sessionFactory.getCurrentSession();
    SectionData section = (SectionData) session.get(SectionData.class, sectionId);
    if (section != null) {

      SectionMetaData sectionmetadata = new SectionMetaData(section, label, value);
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        session.merge(sectionmetadata);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem add section metadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
    }
  }

  public void deleteSectionMetaData(final Long sectionId, final String label) {
	  
    Session session = sessionFactory.getCurrentSession();
    CriteriaBuilder cb = session.getCriteriaBuilder();
    CriteriaQuery<SectionMetaData> cq = cb.createQuery(SectionMetaData.class);
    Root<SectionMetaData> root = cq.from(SectionMetaData.class);
    cq.where(
        cb.equal(root.get("section").get("sectionId"), sectionId),
        cb.equal(root.get("label"), label));

    List<SectionMetaData> sectionmetadatalist = session.createQuery(cq).list();

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        for (SectionMetaData sectionMetaData : sectionmetadatalist) {
          session.remove(sectionMetaData);
        }
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete section metadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }
}
