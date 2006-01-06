/**********************************************************************************
* $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/component/src/java/org/sakaiproject/tool/assessment/data/dao/grading/AssessmentGradingData.java $
* $Id: AssessmentGradingData.java 4720 2005-12-16 03:29:39Z daisyf@stanford.edu $
***********************************************************************************
*
* Copyright (c) 2004-2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.tool.assessment.ui.queue.delivery;
import org.sakaiproject.tool.assessment.ui.model.delivery.TimedAssessmentGradingModel;
import org.sakaiproject.tool.assessment.ui.queue.delivery.SubmitTimedAssessmentThread;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

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
  private HashMap queue;
  private SubmitTimedAssessmentThread thread;

  private TimedAssessmentQueue() { 
    queue = new HashMap();
  } 

  public static synchronized TimedAssessmentQueue getInstance() { 
    if (instance==null) { 
      instance = new TimedAssessmentQueue(); 
    } 
    return instance; 
  } 

  public void add(TimedAssessmentGradingModel timedAG){
    queue.put(timedAG.getAssessmentGradingId(), timedAG);
    if (thread == null){
      thread = new SubmitTimedAssessmentThread(); 
      thread.run();
    }
  }

  public void remove(TimedAssessmentGradingModel timedAG){
    queue.remove(timedAG.getAssessmentGradingId());
    if (isEmpty())
      thread.destroy();
  }

  public TimedAssessmentGradingModel getTimedAssessmentGrading(Long assessmentGradingId){
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

