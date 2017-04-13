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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.tool.assessment.data.dao.assessment.ExtendedTime;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentAccessControlIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.AssessmentBaseIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 */
@Slf4j
public class ExtendedTimeFacade {
    public void init() {
        log.info("init");
    }

    public  List<ExtendedTime>  getEntriesForAss                (AssessmentBaseIfc ass) {
        // is this the best way to ensure separation between published and unpublished?
        if(ass instanceof PublishedAssessmentIfc){
            throw new IllegalArgumentException("getEntriesForAss accepts only unpublished assessments.");
        }

        return getEntriesForX(ass, null);
    }

    public  List<ExtendedTime>  getEntriesForPub                (PublishedAssessmentIfc pub) {
        return getEntriesForX(null, pub);
    }

    public  ExtendedTime        getEntryForPubAndUser           (PublishedAssessmentIfc pub, String user) {
        return extendedTimeQueries.getEntryForPubAndUser(pub, user);
    }

    public  ExtendedTime        getEntryForPubAndGroup          (PublishedAssessmentIfc pub, String group) {
        return extendedTimeQueries.getEntryForPubAndGroup(pub, group);
    }

    public  void                saveEntriesPub                  (PublishedAssessmentIfc p, List<ExtendedTime> entries) {
        saveExtendedTimeEntriesHelper(null, p, entries);
    }

    public  void                saveEntries                     (AssessmentFacade aFacade, List<ExtendedTime> entries) {
        saveExtendedTimeEntriesHelper(aFacade, null, entries);
    }

    public  void                copyEntriesToPub                (PublishedAssessmentIfc pub, List<ExtendedTime> entries) {
        List<ExtendedTime> publishedTimes = new ArrayList<>(entries.size());

        for(ExtendedTime entry : entries) {
            ExtendedTime pubEntry = new ExtendedTime(entry);

            pubEntry.setId(null);
            pubEntry.setAssessment(null);
            pubEntry.setPubAssessment(pub);

            publishedTimes.add(pubEntry);
        }

        saveEntriesPub(pub, publishedTimes);
    }

    private List<ExtendedTime>  getEntriesForX                  (AssessmentBaseIfc ass, PublishedAssessmentIfc pub) {
        List<ExtendedTime> results;
        if(ass == null) {
            results = extendedTimeQueries.getEntriesForPub(pub);
        } else {
            results = extendedTimeQueries.getEntriesForAss(ass);
        }

        if(results == null ) {
            results = new ArrayList<>(0);
        }
        return results;
    }
    /**
     * Sync a List of ExtendedTime Entry to the AssessmentAccessControlIfc
     * @param et List of Extended Time entries to sync
     */

    private void syncExtendedTimeDates(List <ExtendedTime> et) {
        for(ExtendedTime e : et) {
        	e.syncDates();
        }
    }

    /**
     * A Helper method to save extended time entries. The following must be true:
     * ( assFacade != null  || pubAssFac != null )
     * @param assFacade
     * @param p
     * @param newExtendedTime
     */
    private void                saveExtendedTimeEntriesHelper   (AssessmentFacade assFacade, PublishedAssessmentIfc p, List<ExtendedTime> newExtendedTime) {
        List<ExtendedTime> oldExtendedTime;
        if(assFacade == null) {
            oldExtendedTime = extendedTimeQueries.getEntriesForPub(p);
            syncExtendedTimeDates(newExtendedTime);
        } else {
            oldExtendedTime = extendedTimeQueries.getEntriesForAss(assFacade.getData());
            syncExtendedTimeDates(newExtendedTime);
        }

        List<ExtendedTime> extraneousInOld = new ArrayList<>();
        for(ExtendedTime old : oldExtendedTime) {
            boolean matched = false;
            for(ExtendedTime newET : newExtendedTime) {
                if(old.getId().equals(newET.getId())) {
                    matched = true;
                }
            }

            if(!matched) {
                extraneousInOld.add(old);
            }
        }

        for(ExtendedTime item : extraneousInOld) {
            extendedTimeQueries.deleteEntry(item);
        }

        extendedTimeQueries.updateEntries(newExtendedTime);
    }

    @Setter
    private ExtendedTimeQueriesAPI extendedTimeQueries;
}
