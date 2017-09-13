/**
 * Copyright (c) 2003-2017 The Apereo Foundation
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
package org.sakaiproject.gradebookng.tool.component;

import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

/**
 * A GradebookNG implementation of Wicket's AjaxButton that disables the button on click to reduce the likelihood of double-clicks resulting
 * in double actions.
 */
public class GbAjaxButton extends AjaxButton {

	public GbAjaxButton(String id) {
		super(id);
	}

	public GbAjaxButton(String id, Form<?> form) {
		super(id, form);
	}

	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new AjaxCallListener()
				// disable the button right away after clicking it
				// and mark on it if the window is unloading
				.onBefore(
						String.format(
								"$('#%s').prop('disabled', true);" +
										"$(window).on('beforeunload', function() {" +
										"$('#%s').data('unloading', true).prop('disabled', true)});",
								getMarkupId(), getMarkupId()))
				// if the page is unloading, keep it disabled, otherwise
				// add a slight delay in re-enabling it, just in case it succeeded
				// and there's a delay in closing a parent modal
				.onComplete(
						String.format("setTimeout(function() {" +
								"if (!$('#%s').data('unloading')) $('#%s').prop('disabled',false);" +
								"}, 1000)",
								getMarkupId(), getMarkupId())));
	}
}
