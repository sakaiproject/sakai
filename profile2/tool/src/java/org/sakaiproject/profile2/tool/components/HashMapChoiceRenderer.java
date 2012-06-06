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

import java.util.Map;

import org.apache.wicket.markup.html.form.IChoiceRenderer;


/* HashMapChoiceRenderer.java
 * Steve Swinsburg
 * s.swinsburg@lancaster.ac.uk
 * December 2008
 * 
 * Wraps up the IChoiceRenderer actions into a simple constructor that takes a Map of choices that are to be rendered
 * So as to separate key/value in a dropdownchoice component and can be reused.
 * 
 */



public class HashMapChoiceRenderer implements IChoiceRenderer {
	
	private Map m_choices;
	
	public HashMapChoiceRenderer(Map choices) {
		m_choices = choices;
	}

	public String getDisplayValue(Object object) {
		return (String) m_choices.get(object);
	}

	public String getIdValue(Object object, int index) {
		return object.toString();
	}
}
