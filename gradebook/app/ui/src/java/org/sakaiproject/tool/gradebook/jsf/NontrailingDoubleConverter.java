/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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

import org.sakaiproject.util.ResourceLoader;

/**
 * The standard JSF NumberConverter handles output formatting of double
 * numbers nicely by printing them as an integer if there's nothing past
 * the decimal point. On input either a Long or a Double might be
 * returned from "getAsObject". In earlier versions of MyFaces, if the
 * input value was going to a Double bean property, conversion would
 * happen silently. In MyFaces 1.1.1, a IllegalArgumentException is
 * thrown. This converter emulates the old behavior by converting Long
 * values to Double values before passing them to the backing bean.
 */
public class NontrailingDoubleConverter extends NumberConverter {
	public NontrailingDoubleConverter() {
		setType("number");
		ResourceLoader rl = new ResourceLoader();
		setLocale(rl.getLocale());
	}

	/**
	 * Always returns either a null or a Double.
	 */
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		Object number = super.getAsObject(context, component, value);
		if (number != null) {
			if (number instanceof Long) {
				number = new Double(FacesUtil.getRoundDown(((Long)number).doubleValue(), 2));
			}
		}
		return number;
	}

}
