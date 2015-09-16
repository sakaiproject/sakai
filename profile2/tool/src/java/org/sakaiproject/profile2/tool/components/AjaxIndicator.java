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

import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.cycle.RequestCycle;


public class AjaxIndicator extends WebMarkupContainer implements IAjaxIndicatorAware {

	public AjaxIndicator(String id) {
		super(id);
	}

	public void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		tag.put("src", RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR, null));
	}

	public String getAjaxIndicatorMarkupId() {
		return this.getMarkupId();
	}
	
	
}
