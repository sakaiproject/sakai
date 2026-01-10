/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.ui.queue.delivery;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.scheduling.api.SchedulingService;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;

/**
 * <p>Title: TimedAssessmentQueue</p>
 * <p>Description: A queue of assessment timers executed in a pool of threads</p>
 */
@Slf4j
public class TimedAssessmentQueue { 

  private ConcurrentMap queue;
  private SchedulingService schedulingService;
  private ConcurrentMap<Long, ScheduledFuture<?>> tasks;

  private static class Loader {
    static final TimedAssessmentQueue INSTANCE = new TimedAssessmentQueue(ComponentManager.get(SchedulingService.class));
  }

  private TimedAssessmentQueue(SchedulingService schedulingService) {
    this.schedulingService = schedulingService;
    queue = new ConcurrentHashMap();
    tasks = new ConcurrentHashMap<>();
  } 


  // Get the instance of this class
  public static TimedAssessmentQueue getInstance() { 
    return Loader.INSTANCE;
  } 

  
  // Add a timed assessment to the queue
  public void add(TimedAssessmentGradingModel timedAG){
          // Add grading data to the queue
          queue.put(timedAG.getAssessmentGradingId(),timedAG);
          try {
              // Repeat the task until stopped or an exception is thrown
              // Store the resulting ScheduledFuture for the task
              // delay=0s, every 3s 
              tasks.put(timedAG.getAssessmentGradingId(),
                            schedulingService.scheduleAtFixedRate(
                            new TimedAssessmentRunnable(timedAG.getAssessmentGradingId()),
                            0,
                            3000,
                            TimeUnit.MILLISECONDS)); 
              log.info( "SAMIGO_TIMED_ASSESSMENT:QUEUE:NEWTIMER:SUCCESS ID:{}", timedAG.getAssessmentGradingId());
          } catch (Exception ex) {
              log.error("SAMIGO_TIMED_ASSESSMENT:QUEUE:NEWTIMER:FAILED ID:{} Exception:{}", timedAG.getAssessmentGradingId(), ex);
          }
  }


  // Remove a timed assessment from the queue
  public void remove(TimedAssessmentGradingModel timedAG){
          remove(timedAG.getAssessmentGradingId());
  }


  // Remove a timed assessment from the queue
  public void remove(long timedAG){
    log.info("SAMIGO_TIMED_ASSESSMENT:QUEUE:REMOVE ID:{}", timedAG);
    // Stop the task and remove it
    ScheduledFuture future = tasks.get(timedAG);

    if (future != null) {
      future.cancel(true);
    }

    tasks.remove(timedAG);
    // Remove the grading data from the queue
    queue.remove(timedAG);
  }


  // Get assessment data from the queue
  public TimedAssessmentGradingModel get(long assessmentGradingId){
    return (TimedAssessmentGradingModel)queue.get(assessmentGradingId);
  }

}
