/**********************************************************************************
 * $URL$
 * $Id$
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


package org.sakaiproject.tool.assessment.ui.queue.delivery;
import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class TimedAssessmentQueue { 
  private static TimedAssessmentQueue instance; 
  private ConcurrentHashMap queue;
    // private SubmitTimedAssessmentThread thread;
  private Timer timer;

  private static Log log = LogFactory.getLog(TimedAssessmentQueue.class);
  private TimedAssessmentQueue() { 
    queue = new ConcurrentHashMap ();
  } 

  public static synchronized TimedAssessmentQueue getInstance() { 
    if (instance==null) { 
      instance = new TimedAssessmentQueue(); 
    } 
    return instance; 
  } 
  
  public void add(TimedAssessmentGradingModel timedAG){
	  synchronized (queue) {
		  queue.put(timedAG.getAssessmentGradingId(), timedAG);
		  log.debug("***1. TimedAssessmentQueue.add, before schedule timer="+timer);
		  scheduleTask();
		  log.debug("***2. TimedAssessmentQueue.add, after schedule timer="+timer);
	  }
  }

  private void scheduleTask(){
    if (timer == null){
      timer  = new Timer();
      timer.scheduleAtFixedRate(new SubmitTimedAssessmentThread(), 0, 5*1000); //delay=0s, every 5s 
    }
  }

  public void remove(TimedAssessmentGradingModel timedAG){
	  synchronized (queue) {
		  queue.remove(timedAG.getAssessmentGradingId());
		  if (isEmpty()){
			  log.debug("*** before destroy, timer="+timer);
			  timer.cancel();
			  timer = null;
			  log.debug("*** after destroy, timer="+timer);
		  }
	  }
  }
  
  public TimedAssessmentGradingModel get(Long assessmentGradingId){
    return (TimedAssessmentGradingModel)queue.get(assessmentGradingId);
  }

  public void emptyQueue(){
    queue.clear();
  }

  public boolean isEmpty(){
    return queue.isEmpty();
  }

  public int size(){
    return queue.size();
  }

  public Iterator iterator(){
    Collection c = queue.values(); 
    return c.iterator();
  }

} 

