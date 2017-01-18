/**
 * Copyright (c) 2007 The Apereo Foundation
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
package org.sakaiproject.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.model.ResourceModel;

public class PagingChoiceRenderer implements IChoiceRenderer {
	private static final long serialVersionUID = 1L;

	private ResourceModel displayBeforeModel, displayAfterModel;
	
	public PagingChoiceRenderer() {
		displayBeforeModel = new ResourceModel("paging.display.before");
		displayAfterModel = new ResourceModel("paging.display.after");
	}
	
	public Object getDisplayValue(Object object) {
		String before = String.valueOf(displayBeforeModel.getObject());
		String str = String.valueOf(object);
		String after = String.valueOf(displayAfterModel.getObject());
		
		return new StringBuilder(before).append(str).append(" ").append(after).toString();
	}

	public String getIdValue(Object object, int index) {
		return String.valueOf(object);
	}
}
