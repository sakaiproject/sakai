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

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;
import org.sakaiproject.tool.assessment.services.PersistenceService;

@Slf4j
public class SectionFacadeQueries  extends HibernateDaoSupport implements SectionFacadeQueriesAPI {

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
      SectionData section = (SectionData) getHibernateTemplate().load(SectionData.class, sectionId);
      return new SectionFacade(section);
  }

  public SectionData load(Long sectionId) {
      return (SectionData) getHibernateTemplate().load(SectionData.class, sectionId);
  }

  public void addSectionMetaData(Long sectionId, String label, String value) {
    SectionData section = (SectionData)getHibernateTemplate().load(SectionData.class, sectionId);
    if (section != null) {

      SectionMetaData sectionmetadata = new SectionMetaData(section, label, value);
    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(sectionmetadata);
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
    final String query = "from SectionMetaData imd where imd.section.sectionId = :id and imd.label = :label";
    
    final HibernateCallback<List> hcb = session -> {
        Query q = session.createQuery(query);
        q.setLong("id", sectionId);
        q.setString("label", label);
        return q.list();
    };
    List sectionmetadatalist = getHibernateTemplate().execute(hcb);

    int retryCount = PersistenceService.getInstance().getPersistenceHelper().getRetryCount();
    while (retryCount > 0){
      try {
        getHibernateTemplate().deleteAll(sectionmetadatalist);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete section metadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().getPersistenceHelper().retryDeadlock(e, retryCount);
      }
    }
  }
}
