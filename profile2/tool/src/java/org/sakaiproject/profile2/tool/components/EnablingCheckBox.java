/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.tool.components;

import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.string.Strings;

/**
 * An AjaxCheckBox with some convenience methods to see whether or not it is checked
 * 
 * Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 */
public abstract class EnablingCheckBox extends AjaxCheckBox{

	private static final long serialVersionUID = 1L;
	
	public EnablingCheckBox(final String id) {
		this(id, null);
	}
	
	public EnablingCheckBox(final String id, IModel model) {
		super(id, model);
	}
	 
	public boolean isChecked() {
	  
		final String value = getValue();
	  
		if (value != null) {
			try {
				return Strings.isTrue(value);
			} catch (StringValueConversionException e) {
				return false;
			}
		}
		return false;
	}
}