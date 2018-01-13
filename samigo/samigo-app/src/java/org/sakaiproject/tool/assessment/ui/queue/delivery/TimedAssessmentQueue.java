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

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.event.cover.EventTrackingService;
import org.sakaiproject.event.cover.NotificationService;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.tool.api.Session;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;

/**
 * <p>Title: TimedAssessmentQueue</p>
 * <p>Description: A queue of assessment timers executed in a pool of threads</p>
 */
@Slf4j
public class TimedAssessmentQueue { 

  private ConcurrentHashMap queue;
  private HashMap<Long, ScheduledFuture<?>> tasks;
  private ScheduledThreadPoolExecutor threadPool;

  private static class Loader {
    static final TimedAssessmentQueue INSTANCE = new TimedAssessmentQueue();
  }

  private TimedAssessmentQueue() { 
    queue = new ConcurrentHashMap();
    tasks = new HashMap();
    // Get any custom thread count, or default to 4
    int threads = ServerConfigurationService.getInt("samigo.timerThreadCount", 4);
    log.info( "SAMIGO_TIMED_ASSESSMENT:QUEUE:INIT: THREADS:" + threads + " (Set property 'samigo.timerThreadCount' to adjust.)");
    threadPool = new ScheduledThreadPoolExecutor(threads);
    // Set the default removal policy so we don't leak memory with finished tasks
    threadPool.setRemoveOnCancelPolicy(true);
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
              tasks.put(timedAG.getAssessmentGradingId(), threadPool.scheduleAtFixedRate(new TimedAssessmentRunnable(timedAG.getAssessmentGradingId()), 0, 3000, TimeUnit.MILLISECONDS)); 
              log.info( "SAMIGO_TIMED_ASSESSMENT:QUEUE:NEWTIMER:SUCCESS ID:" + timedAG.getAssessmentGradingId());
          } catch (Exception ex) {
              log.error("SAMIGO_TIMED_ASSESSMENT:QUEUE:NEWTIMER:FAILED ID:" + timedAG.getAssessmentGradingId() + " Exception:" + ex);
          }
  }


  // Remove a timed assessment from the queue
  public void remove(TimedAssessmentGradingModel timedAG){
          remove(timedAG.getAssessmentGradingId());
  }


  // Remove a timed assessment from the queue
  public void remove(long timedAG){
    log.info("SAMIGO_TIMED_ASSESSMENT:QUEUE:REMOVE ID:" + timedAG);
    // Stop the task and remove it
    tasks.get(timedAG).cancel(true);
    tasks.remove(timedAG);
    // Remove the grading data from the queue
    queue.remove(timedAG);
  }


  // Get assessment data from the queue
  public TimedAssessmentGradingModel get(long assessmentGradingId){
    return (TimedAssessmentGradingModel)queue.get(assessmentGradingId);
  }

}
