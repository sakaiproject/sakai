/**********************************************************************************
*
* $Id$
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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.NumberConverter;

import lombok.extern.slf4j.Slf4j;

import org.sakaiproject.util.ResourceLoader;

/**
 * The standard JSF number formatters only round values. We generally need
 * them truncated.
 * This converter truncates the input value (probably a double) to four
 * decimal places, and then returns it as a percentage with a maximum of two
 * decimal places. Be careful when using this converter. It will truncate your input
 * at 4 decimals before it is converted to a percentage. Then, the percentage is rounded 
 * at 2 decimal places. So if you pass .12349, it will first get truncated to .1234 and
 * then converted to a percentage like 12.34%.  You may not want to use this with
 * decimals with more than 4 decimal places prior to conversion to %.
 * 
 */
@Slf4j
public class PrecisePercentageConverter extends NumberConverter {
	public PrecisePercentageConverter() {
		setType("percent");
		setMaxFractionDigits(2); // beware because this rounds at 2 decimals
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
				// Truncate to 4 decimal places.
				value = new Double(FacesUtil.getRoundDown(((Number)value).doubleValue(), 4));
			}
			formattedScore = super.getAsString(context, component, value);
		}

		return formattedScore;
	}

}
