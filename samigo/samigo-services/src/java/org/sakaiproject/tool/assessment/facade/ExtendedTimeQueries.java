/*
 * Copyright (c) 2016, The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.sakaiproject.tool.assessment.facade;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.ManyToOneType;
import org.hibernate.type.StringType;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate4.HibernateCallback;
import org.springframework.orm.hibernate4.support.HibernateDaoSupport;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class ExtendedTimeQueries extends HibernateDaoSupport implements ExtendedTimeQueriesAPI {

    /**
     * init
     */
    public  void                init                    () {
        log.info("init()");
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public  List<ExtendedTime>  getEntriesForAss        (AssessmentBaseIfc ass) {
        log.debug("getEntriesForAss " + ass.getAssessmentBaseId());

        try {
            HibernateCallback hcb = (Session s) -> {
                Query q = s.getNamedQuery(QUERY_GET_ENTRIES_FOR_ASSESSMENT);
                q.setParameter(ASSESSMENT_ID, ass, new ManyToOneType(null, "org.sakaiproject.tool.assessment.data.dao.assessment.AssessmentBaseData"));
                return q.list();
            };
            return (List<ExtendedTime>) getHibernateTemplate().execute(hcb);
        } catch (DataAccessException e) {
            log.error("Failed to get Extended TimeEntries for Assessment: " + ass.getAssessmentBaseId(), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public  List<ExtendedTime>  getEntriesForPub        (PublishedAssessmentIfc pub) {
        log.debug("getEntriesForPub " + pub.getPublishedAssessmentId());

        try {
            HibernateCallback hcb = (Session s) -> {
                Query q = s.getNamedQuery(QUERY_GET_ENTRIES_FOR_PUBLISHED);
                q.setParameter(PUBLISHED_ID, pub, new ManyToOneType(null, "org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData"));
                return q.list();
            };

            return (List<ExtendedTime>) getHibernateTemplate().execute(hcb);
        } catch (DataAccessException e) {
            log.error("Failed to get Extended Time Entries for Published Assessment: " + pub.getPublishedAssessmentId(), e);
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public  ExtendedTime        getEntryForPubAndUser   (PublishedAssessmentIfc pub, String userId) {
        log.debug("getEntryForPubAndUser, pub: '" + pub.getPublishedAssessmentId() + "' User: " + userId);

        return getPubAndX(QUERY_GET_ENTRY_FOR_PUB_N_USER, pub, USER_ID, userId);
    }

    /**
     * {@inheritDoc}
     */
    public  ExtendedTime        getEntryForPubAndGroup  (PublishedAssessmentIfc pub, String groupId) {
        log.debug("getEntryForPubAndGroup, pub: '" + pub.getPublishedAssessmentId() + "' group: " + groupId);

        return getPubAndX(QUERY_GET_ENTRY_FOR_PUB_N_GROUP, pub, GROUP, groupId);
    }

    /**
     * {@inheritDoc}
     */
    public  boolean             updateEntry             (ExtendedTime e) {
        log.debug("updating entry assessment: '" + e.getAssessmentId() + "' pubId: '" + e.getPubAssessmentId() + "' user: '" + e.getUser() + "' group: " + e.getGroup());

        try {
            getHibernateTemplate().saveOrUpdate(e);
            return true;
        } catch (DataAccessException de) {
            log.error("Error updating extended time entry" , de);
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public  void                updateEntries           (List<ExtendedTime> entries) {
        entries.forEach(this::updateEntry);
    }

    /**
     * {@inheritDoc}
     */
    public  boolean             deleteEntry             (final ExtendedTime e) {
        log.debug("Removing ExtendedTime entry id: " + e.getId());

       try {
           getHibernateTemplate().delete(e);
           return true;
       } catch (DataAccessException de) {
           log.error("Failed to delete extendedTime entry, id: " + e.getId() + ".", de);
           return false;
       }
    }

    @SuppressWarnings("unchecked")
    private ExtendedTime        getPubAndX              (final String query, final PublishedAssessmentIfc pub, final String secondParam, final String secondParamValue) {
        try{
            HibernateCallback hcb = (Session s) -> {
                Query q = s.getNamedQuery(query);
                q.setParameter(PUBLISHED_ID, pub, new ManyToOneType(null, "org.sakaiproject.tool.assessment.data.dao.assessment.PublishedAssessmentData"));
                q.setParameter(secondParam, secondParamValue, new StringType());
                return q.uniqueResult();
            };

            return (ExtendedTime) getHibernateTemplate().execute(hcb);
        } catch (DataAccessException e) {
            log.error("Failed to get extended time for pub: " + pub.getPublishedAssessmentId() + " and user/group: " + secondParamValue, e);
            return null;
        }
    }
}
