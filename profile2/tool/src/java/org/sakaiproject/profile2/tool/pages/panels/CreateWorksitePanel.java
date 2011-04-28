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
package org.sakaiproject.profile2.tool.pages.panels;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.ResourceModel;

/**
 * Panel for creating a worksite from a group of people.
 * 
 * @author d.b.robinson@lancaster.ac.uk
 *
 */
public class CreateWorksitePanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Creates an instance of <code>CreateWorksitePanel</code>.
	 * 
	 * @param id the wicket id.
	 */
	public CreateWorksitePanel(String id) {
		super(id);

		WebMarkupContainer connectionsContainer = new WebMarkupContainer("connectionsContainer");
		add(connectionsContainer);

		connectionsContainer.add(new Label("connectionsLabel",
				new ResourceModel("heading.worksite.connections")));
		
		
	}
}
