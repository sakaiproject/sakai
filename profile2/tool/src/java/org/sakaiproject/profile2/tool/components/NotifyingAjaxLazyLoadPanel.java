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

import org.apache.wicket.Component;
import org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.IHeaderContributor;

/**
 * A Sakai-specific extension to the built in AjaxLazyLoadPanel that allows javascript to be run after its loaded.
 * 
 * <p>e.g. response.renderOnDomReadyJavascript("setMainFrameHeight(window.name);");</p>
 * 
 * @author Steve Swinsburg steve.swinsburg@gmail.com)
 *
 */
public abstract class NotifyingAjaxLazyLoadPanel extends AjaxLazyLoadPanel implements IHeaderContributor {

	
	private static final long serialVersionUID = 1L;

	public NotifyingAjaxLazyLoadPanel(String id) {
		super(id);
	}

	public abstract void renderHead(IHeaderResponse response);
	
	public abstract Component getLazyLoadComponent(String markupId);
}