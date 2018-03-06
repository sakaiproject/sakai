/**
 * Copyright (c) 2015, The Apereo Foundation
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
 */

package org.sakaiproject.samigo.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import lombok.extern.slf4j.Slf4j;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;

import org.sakaiproject.event.api.Event;
import org.sakaiproject.event.api.EventTrackingService;
import org.sakaiproject.samigo.util.SamigoConstants;

@Slf4j
public class SamigoObserver implements Observer {

    public void init() {
        log.info("init()");
        eventTrackingService.addLocalObserver(this);
    }

    public void destroy(){
        log.info("destroy");
        eventTrackingService.deleteObserver(this);
    }

    public void update(Observable arg0, Object arg) {
        if (!(arg instanceof Event)){
            return;
        }

        Event event = (Event) arg;
        String eventType = event.getEvent();

        if(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_NOTI.equals(eventType)) {
            log.debug("Assessment Submitted Event");
            String hashMapString = event.getResource();
            Map<String, Object> notiValues =  stringToHashMap(hashMapString);
            samigoETSProvider.notify(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED, notiValues, event);
        } else if(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO.equals(eventType)){
            log.debug("Assessment Auto Submitted Event");
            String hashMapString = event.getResource();
            Map<String, Object> notiValues =  stringToHashMap(hashMapString);
            samigoETSProvider.notify(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_AUTO, notiValues, event);
        } else if(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD.equals(eventType)){
            log.debug("Assessment Timed Submitted Event");
            String hashMapString = event.getResource();
            Map<String, Object> notiValues = stringToHashMap(hashMapString);
            samigoETSProvider.notify(SamigoConstants.EVENT_ASSESSMENT_SUBMITTED_TIMER_THREAD, notiValues, event);
        }
    }

    /*
     * stringToHashMap
     * Derived from http://stackoverflow.com/a/26486046
     */
    private Map<String, Object> stringToHashMap(String hashMapString){
        Map<String, Object> map = new HashMap<>();

        hashMapString = StringUtils.substringBetween(hashMapString, "{", "}");           //remove curly brackets
        String[] keyValuePairs = hashMapString.split(",");              //split the string to create key-value pairs

        for(String pair : keyValuePairs)                        //iterate over the pairs
        {
            String[] entry = pair.split("=");                   //split the pairs to get key and value
            String key = StringUtils.trim(entry[0]);
            if(entry.length == 2) {
                if (key.equals("assessmentGradingID") || key.equals("publishedAssessmentID")) {
                    map.put(key, Long.valueOf(StringUtils.trim(entry[1])));
                } else {
                    map.put(key, String.valueOf(StringUtils.trim(entry[1])));          //add them to the hashmap and trim whitespaces
                }
            } else{
                map.put(key, "");
            }
        }

        return map;
    }

    @Setter
    private EventTrackingService    eventTrackingService;

    @Setter
    private SamigoETSProviderImpl   samigoETSProvider;
}
