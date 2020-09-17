/**
 * Copyright (c) 2005-2020 The Apereo Foundation
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

package org.sakaiproject.tool.assessment.util;

import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.tool.assessment.ui.listener.util.ContextUtil;

/**
 * The purpose of this class is to ensure that a quiz's (or exception's) open window is greater than or equal to time limit.
 * @author bjones86
 */
public class TimeLimitValidator
{
    /**
     * Given a start and due date, determine if the availability window is longer than or equal to the sum of the provided timer hours and timer minutes.
     * If the availability window is not equal to or greater than the time limit, an error message is generated for the UI (provided that the message
     * bundle and key strings are not empty, and the {@link FacesContext} was provided.
     * 
     * @param start The start {@link Date} of the availability window
     * @param due The end {@link Date} of the availability window
     * @param timerHours Number of hours included in the time limit
     * @param timerMinutes Number of minutes included in the time limit
     * @param messageBundle The name of the message bundle to access if an error message needs to be displayed
     * @param messageKey The key of the error message to be displayed if need be
     * @param context {@link FacesContext} to add the error message to if need be
     * @return true if the availability window is larger than or equal to the time limit; false otherwise
     */
    public static boolean availableLongerThanTimer( Date start, Date due, Integer timerHours, Integer timerMinutes, String messageBundle, String messageKey, FacesContext context )
    {
        // Short circuit: if there's no due date, the quiz is open indefinately which means the timer cannot exceed the availability window
        if( due == null )
        {
            return true;
        }

        long timerTotalMinutes = 0;
        if( timerHours != null )
        {
            timerTotalMinutes += timerHours.longValue() * 60;
        }
        if( timerMinutes != null )
        {
            timerTotalMinutes += timerMinutes.longValue();
        }

        long openWindowMinutes = (due.getTime() - start.getTime()) / 1000 / 60;
        boolean availableLongerThanTimer = openWindowMinutes >= timerTotalMinutes;

        // Add error message if we have message bundle name, message key, and FacesContext
        if( !availableLongerThanTimer && StringUtils.isNotBlank( messageBundle ) && StringUtils.isNotBlank( messageKey ) && context != null )
        {
            String errorMsg = ContextUtil.getLocalizedString( messageBundle, messageKey );
            context.addMessage( null, new FacesMessage( FacesMessage.SEVERITY_WARN, errorMsg, null ) );
        }

        return availableLongerThanTimer;
    }

    /**
     * Determine if a timer is in place based on the amount of hours and minutes specified.
     * @param timedHours Number of hours for the time limit (can be null or 0+)
     * @param timedMinutes Number of minutes for the time limit (can be null or 0+)
     * @return true if the timer has a positive value greater than 0; false otherwise
     */
    public static boolean hasTimer( Integer timedHours, Integer timedMinutes )
    {
        return (timedHours != null && timedHours > 0) || (timedMinutes != null && timedMinutes > 0);
    }
}
