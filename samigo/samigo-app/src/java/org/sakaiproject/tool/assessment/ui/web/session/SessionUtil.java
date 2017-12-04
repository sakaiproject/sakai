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

package org.sakaiproject.tool.assessment.ui.web.session;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.tool.api.ToolSession; 
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean;
import org.sakaiproject.tool.assessment.integration.context.IntegrationContextFactory;

/**
 * <p>
 * <code>SessionUtil</code> provides a mechanism to assign the inactive
 * interval for the current session. In the event that user has more than one
 * thread accessing the same or separate assessments, <code> SessionUtil </code>
 * guarantees that the session will have the (maximum assessment time limit among threads)
 * as the value of the inactive interval.
 *
 * <pre>
 *   <tt>Scenarios which require a greater inactive interval than the default interval:
 *     - Assessments or surveys which are delivered on a single page
 *     - Questions which require more time than the default application timeout setting
 *   </tt>
 * </pre>
 *
 *
 * @author  Jarrod Lannan
 * @version $Revision$
 * @see     javax.servlet.http.HttpSession
 * @see     javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
 *
 */
@Slf4j
public class SessionUtil {

  public static final String EXTERNAL_APP_INTERVAL = "app_interval_val";
  public static final int HOURS_TO_SECONDS_MULTIPLIER = 60 * 60;
  public static final int MINUTES_TO_SECONDS_MULTIPLIER = 60;
  public static final int DEFAULT_APP_INTERVAL_VAL = 5 * HOURS_TO_SECONDS_MULTIPLIER;

  private static final int INTERVAL_BUFFER = 60 * 15;

  /**
   * Sets the current <code>HttpSession</code> maxInactiveInterval value
   * @param context the faces context
   * @param delivery the delivery bean
   * @param beginAssessment true if called from the beginning of an assessment, otherwise false
   *
   * @see org.sakaiproject.tool.assessment.ui.bean.delivery.DeliveryBean
   */
  public static void setSessionTimeout(FacesContext context, DeliveryBean delivery, boolean beginAssessment){
    if (!IntegrationContextFactory.getInstance().isIntegrated()) {
        return;
    }

    ExternalContext exContext = context.getExternalContext();
    HttpSession session = (HttpSession) exContext.getSession(false);

    if (session == null){
      return;
    }

    /** if we have a tool session then get big session (MySession) */
    if (session instanceof ToolSession){
      session = (HttpSession) SessionManager.getCurrentSession();
    }

    synchronized (session){

      if (beginAssessment){

        /**
         * if we have not already set value
         * (ensure setSessionTimeout is called only once at beginning of assessment)
         */
        int interval = DEFAULT_APP_INTERVAL_VAL;
        if (session.getAttribute(EXTERNAL_APP_INTERVAL) == null)
        {
          if (delivery != null && delivery.getHasTimeLimit())
          {
            interval = delivery.getTimeLimit_hour() * HOURS_TO_SECONDS_MULTIPLIER;
            interval += delivery.getTimeLimit_minute() * MINUTES_TO_SECONDS_MULTIPLIER;
          }
          else
          {
            /** for assessments without time limit */
            interval = DEFAULT_APP_INTERVAL_VAL;
          }
        }

        if (interval > session.getMaxInactiveInterval()){
          if (log.isDebugEnabled()){
            log.debug("begin_assessment: Setting session " + session.getId() + " inactive interval= " + interval + " seconds");
          }
          /** store current interval value */
          session.setAttribute(EXTERNAL_APP_INTERVAL, Integer.valueOf(session.getMaxInactiveInterval()));
          session.setMaxInactiveInterval(interval + INTERVAL_BUFFER);
        }
      }
      else{ /** on assessment submission or 'save and exit' from assessment */
        Integer returnVal = (Integer) session.getAttribute(EXTERNAL_APP_INTERVAL);
        if (returnVal == null){
          return;
        }
        else{
          session.removeAttribute(EXTERNAL_APP_INTERVAL);
          if (log.isDebugEnabled()){
            log.debug("end_assessment: Setting session " + session.getId() + " inactive interval= " + returnVal + " seconds");
          }
          /** set to value of interval before taking */
          session.setMaxInactiveInterval(returnVal.intValue());
        }
      }
    }
  }
}
