/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.tool.wicket.components;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.model.IModel;


public abstract class IndicatingAjaxRadioGroup extends RadioGroup implements IAjaxIndicatorAware {
	private static final long						serialVersionUID	  = 1L;
	private AbstractAjaxBehavior					ajaxUpdatingBehavior;
	private Object 									forModelObjectOnly;
	private AjaxIndicatorAppender				    indicatorAppender  	  = new AjaxIndicatorAppender();

	public IndicatingAjaxRadioGroup(final String id) {
		this(id, null, null, false);
	}

	public IndicatingAjaxRadioGroup(final String id, final Object forModelObjectOnly, final boolean lazyLoadData) {
		this(id, null, forModelObjectOnly, lazyLoadData);
	}
	
	public IndicatingAjaxRadioGroup(final String id, final IModel model, final Object forModelObjectOnly, final boolean lazyLoadData) {
		super(id, model);
		setOutputMarkupId(true);	
		
		if(lazyLoadData) {
			this.forModelObjectOnly = forModelObjectOnly;
			add(indicatorAppender);	
			ajaxUpdatingBehavior = new AjaxFormChoiceComponentUpdatingBehavior() {
				private static final long	serialVersionUID	= 1L;
	
				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					if(forModelObjectOnly != null && forModelObjectOnly.equals(getModelObject())) {
						IndicatingAjaxRadioGroup.this.onUpdate(target);
					}
				}
			};
			add(ajaxUpdatingBehavior);
		}
	}

	public void removeAjaxUpdatingBehavior() {
		if(getBehaviors().contains(ajaxUpdatingBehavior)) {
			remove(ajaxUpdatingBehavior);
		}
	}

	/**
	 * Listener method invoked on an ajax update call
	 * @param target
	 */
	protected abstract void onUpdate(AjaxRequestTarget target);

	public String getAjaxIndicatorMarkupId() {
		if(forModelObjectOnly != null && forModelObjectOnly.equals(getModelObject())) {
			return indicatorAppender.getMarkupId();
		}else{
			return null;
		}
	}

}
