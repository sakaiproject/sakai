/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the"License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/



package org.sakaiproject.tool.assessment.ui.queue.delivery;

import org.sakaiproject.tool.assessment.data.dao.grading.AssessmentGradingData;
import org.sakaiproject.tool.assessment.ui.queue.delivery.TimedAssessmentQueue;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.services.GradingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.util.Date;
import java.util.Iterator;
import java.util.TimerTask;

/**
 * <p>Title: Samigo</p>
 * <p>Description: Sakai Assessment Manager</p>
 * <p>Copyright: Copyright (c) 2004 Sakai Project</p>
 * <p>Organization: Sakai Project</p>
 * @version $Id: SubmitTimedAssessmentThread.java 1294 2005-08-19 17:22:35Z esmiley@stanford.edu $
 */

public class SubmitTimedAssessmentThread extends TimerTask
{

  private static Log log = LogFactory.getLog(SubmitTimedAssessmentThread.class);
  public SubmitTimedAssessmentThread(){}

  public void run(){
    // get the queue, go through the queue till it is empty     
    TimedAssessmentQueue queue = TimedAssessmentQueue.getInstance();
    Iterator iter = queue.iterator();
    while (iter.hasNext()){
      TimedAssessmentGradingModel timedAG = (TimedAssessmentGradingModel)iter.next();
      log.debug("****** going through timedAG in queue, timedAG"+timedAG);
      boolean submitted = timedAG.getSubmittedForGrade();
      long bufferedExpirationTime = timedAG.getBufferedExpirationDate().getTime(); // in millesec
      long currentTime = (new Date()).getTime(); // in millisec

      log.debug("****** submitted="+submitted);
      log.debug("****** currentTime="+currentTime);
      log.debug("****** bufferedExpirationTime="+bufferedExpirationTime);
      log.debug("****** expired="+(currentTime > bufferedExpirationTime));
      if (!submitted){
        if (currentTime > bufferedExpirationTime){ // time's up, i.e. timeLeft + latency buffer reached
          timedAG.setSubmittedForGrade(true);
          // set all the properties right and persist status to DB
          GradingService service = new GradingService();
          AssessmentGradingData ag = service.load(timedAG.getAssessmentGradingId().toString());
          ag.setForGrade(Boolean.TRUE);
          ag.setTimeElapsed(new Integer(timedAG.getTimeLimit()));
          ag.setSubmittedDate(new Date());
          service.saveOrUpdateAssessmentGrading(ag);
          log.debug("**** 4a. time's up, timeLeft+latency buffer reached, saved to DB");
        }
      }
      else{ //submitted, remove from queue if transaction buffer is also reached
        if (currentTime > (bufferedExpirationTime + timedAG.getTransactionBuffer()*1000)){
          queue.remove(timedAG);
          log.debug("**** 4b. transaction buffer reached");
        }
      }
    }
  }

}
