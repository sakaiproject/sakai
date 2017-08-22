/**
 * Copyright (c) 2008-2014 The Apereo Foundation
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
package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

/**
 * Class to add a confirm javascript window as needed.
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class JavascriptEventConfirmation extends AttributeModifier {
	
	private static final long serialVersionUID = 1L;

	public JavascriptEventConfirmation(String event, IModel model) {
		super(event, model);
	}

	protected String newValue(final String currentValue, final String replacementValue) {
		String prefix = "var conf = confirm('" + replacementValue + "'); " + "if (!conf) return false; ";
		String result = prefix;
		if (currentValue != null) {
			result = prefix + currentValue;
		}
		return result;
	}
}