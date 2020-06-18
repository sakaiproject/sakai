/**
 * Copyright (c) 2003-2019 The Apereo Foundation
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
package org.sakaiproject.acadtermmanage.tool.wicketstuff;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.model.IModel;

/** An ordinary Wicket Link with a special id so it can be used in ActionPanel */
public abstract class ActionLink<T> extends Link<T> {

	private static final long serialVersionUID = 1L;
	
	

	public ActionLink(){
		super(ActionPanel.LINK_ID);
	}
	
	public ActionLink(IModel<T> model){
		super(ActionPanel.LINK_ID,model);
	}

}
