/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/trunk/component/src/java/org/sakaiproject/tool/assessment/facade/SectionFacadeQueries.java $
 * $Id: SectionFacadeQueries.java 9273 2006-05-10 22:34:28Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.assessment.facade;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData;
import org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentData;
import org.sakaiproject.tool.assessment.data.dao.assessment.ItemData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionData;
import org.sakaiproject.tool.assessment.data.dao.assessment.SectionMetaData;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.sakaiproject.tool.assessment.services.PersistenceService;
import org.sakaiproject.tool.assessment.osid.shared.impl.IdImpl;

public class SectionFacadeQueries  extends HibernateDaoSupport implements SectionFacadeQueriesAPI {
  private static Log log = LogFactory.getLog(SectionFacadeQueries.class);

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

  /*
  public static void main(String[] args) throws DataFacadeException {
    SectionFacadeQueriesAPI instance = new SectionFacadeQueries ();
    // add an assessmentTemplate
    if (args[0].equals("add")) {
      Long assessmentId = new Long(args[1]);
      Long sectionId = instance.addSection(assessmentId);
      SectionFacade section = instance.get(sectionId);
      print(section);
    }
    if (args[0].equals("remove")) {
      instance.remove(new Long(args[1]));
    }
    if (args[0].equals("load")) {
      SectionFacade s = (SectionFacade)instance.get(new Long(args[1]));
      print(s);
    }
    System.exit(0);
  }
 
  
  
  public static void print(SectionFacade section) {
    //log.debug("**sectionId #" + section.getId());
    //log.debug("**Section Title = " + section.getTitle());
    //log.debug("**Item = " + section.getItemSet());
  }

  
  public Long addSection(Long assessmentId) {
    // take default submission model
    SectionData section = new SectionData();
      AssessmentBaseData assessment = (AssessmentBaseData) getHibernateTemplate().load(AssessmentBaseData.class, assessmentId);
      //section.setAssessmentId(assessmentId);
      section.setAssessment((AssessmentData)assessment);
      section.setDuration( Integer.valueOf(30));
      section.setSequence(Integer.valueOf(1));
      section.setTitle("section title");
      section.setDescription("section description");
      section.setTypeId(TypeFacade.DEFAULT_SECTION);
      section.setStatus(Integer.valueOf(1));
      section.setCreatedBy("1");
      section.setCreatedDate(new Date());
      section.setLastModifiedBy("1");
      section.setLastModifiedDate(new Date());
      ItemManager itemManager = new ItemManager();
      ItemData item = itemManager.prepareItem();
      item.setSection(section);
      section.addItem(item);

      getHibernateTemplate().save(section);
    return section.getSectionId();
  } 
  
  
  public void remove(Long sectionId) {
      SectionFacade section = (SectionFacade) getHibernateTemplate().load(SectionData.class, sectionId);
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().delete(section);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem removing section: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }

*/
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
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().save(sectionmetadata);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem add section metadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
    }
  }

  public void deleteSectionMetaData(final Long sectionId, final String label) {
    final String query = "from SectionMetaData imd where imd.section.sectionId=? and imd.label= ? ";
    
    final HibernateCallback hcb = new HibernateCallback(){
    	public Object doInHibernate(Session session) throws HibernateException, SQLException {
    		Query q = session.createQuery(query);
    		q.setLong(0, sectionId.longValue());
    		q.setString(1, label);
    		return q.list();
    	};
    };
    List sectionmetadatalist = getHibernateTemplate().executeFind(hcb);

//    List sectionmetadatalist = getHibernateTemplate().find(query,
//        new Object[] { sectionId, label },
//        new org.hibernate.type.Type[] { Hibernate.LONG , Hibernate.STRING });
    int retryCount = PersistenceService.getInstance().getRetryCount().intValue();
    while (retryCount > 0){
      try {
        getHibernateTemplate().deleteAll(sectionmetadatalist);
        retryCount = 0;
      }
      catch (Exception e) {
        log.warn("problem delete section metadata: "+e.getMessage());
        retryCount = PersistenceService.getInstance().retryDeadlock(e, retryCount);
      }
    }
  }



}
