/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.model.Model;

/** 
 * ExternalImage is a component that renders an image given a url
 * 
 * @author	Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 * @since	March, 2009
 *
 */
public class ExternalImage extends WebComponent {

	private static final long serialVersionUID = 1L;

	/**
	 * Render an image into an 'img' tag with the wicket:id and src url
	 * @param id
	 * @param imageUrl
	 */
	public ExternalImage(String id, String imageUrl) {
	    super(id);
	    add(new AttributeModifier("src", true, new Model(imageUrl)));
	    setVisible(!(imageUrl==null || imageUrl.equals("")));
	}
	
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		checkComponentTag(tag, "img");
	}

}