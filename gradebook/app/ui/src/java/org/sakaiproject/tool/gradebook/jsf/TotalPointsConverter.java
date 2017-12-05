/**********************************************************************************
*
* $Id: TotalPointsConverter.java 105079 2014-01-14 11:04:11Z rlong@unicon.net $
*
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.jsf;

import java.text.DecimalFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.ResourceLoader;

/**
 * The standard JSF number formatters only round values. We generally need
 * them truncated.
 * This converter truncates the input value (probably a double) to two
 * decimal places, removes any non-significant zeroes, and then returns it with a maximum of two decimal places.
 * Example: 10.0 -> 10, 10.50 -> 10.5
 */
@Slf4j
public class TotalPointsConverter extends NumberConverter {
    public TotalPointsConverter() {
        setType("number");
        ResourceLoader rl = new ResourceLoader();
        setLocale(rl.getLocale());
    }

    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

        String formattedScore;
        if (value == null) {
            formattedScore = FacesUtil.getLocalizedString("score_null_placeholder");
        } else {
            if (value instanceof Number) {
                DecimalFormat decimalFormat = new DecimalFormat("#.##");
                decimalFormat.format(((Number)value).doubleValue());
            }
            formattedScore = super.getAsString(context, component, value);
        }

        return formattedScore;
    }

}
