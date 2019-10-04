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
package org.sakaiproject.wicket.ajax.markup.html.form;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;

public class SakaiAjaxCancelButton extends SakaiAjaxButton
{
	private static final long serialVersionUID = 1L;

	private Class<? extends Page> destination;

	public SakaiAjaxCancelButton(String id, Class<? extends Page> destination)
	{
		super(id);
		this.destination = destination;
		setDefaultFormProcessing(false);
	}

	@Override
	public void onSubmit(AjaxRequestTarget target)
	{
		setResponsePage(destination);
	}
}
