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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.AjaxChannel;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;


/**
 * @author Nuno Fernandes
 */
public abstract class AjaxLazyLoadFragment extends Panel {
	private static final long				serialVersionUID	= 1L;

	// State:
	// 0:add loading component
	// 1:loading component added, waiting for ajax replace
	// 2:ajax replacement completed
	private byte state = 0;

	/**
	 * @param id
	 */
	public AjaxLazyLoadFragment(final String id) {
		super(id);
		setOutputMarkupId(true);

		final AbstractDefaultAjaxBehavior behavior = new AbstractDefaultAjaxBehavior() {
			@Override
			protected void respond(AjaxRequestTarget target) {
				Fragment fragment = getLazyLoadFragment("content");
				AjaxLazyLoadFragment.this.replace(fragment.setRenderBodyOnly(true));
				target.add(AjaxLazyLoadFragment.this);
				setState((byte) 2);
			}

			@Override
			public void renderHead(Component component, IHeaderResponse response) {
				response.render(OnDomReadyHeaderItem.forScript(getCallbackScript().toString()));
				super.renderHead(component, response);
			}
			
			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
			{
				super.updateAjaxAttributes(attributes);
				
				attributes.setChannel(new AjaxChannel(getId()));
			}

			@Override
			public boolean isEnabled(Component component) {
				return state < 2;
			}			
		};
		add(behavior);
		
	}
	
	@Override
	protected void onBeforeRender() {
		if(state == 0){
			final Component loadingComponent = getLoadingComponent("content");
			add(loadingComponent.setRenderBodyOnly(true));
			setState((byte) 1);
		}
		super.onBeforeRender();
	}

	/**
	 * @param markupId The frgment markupid.
	 * @return The fragment that must be lazy created.
	 */
	public abstract Fragment getLazyLoadFragment(String markupId);

	/**
	 * @param markupId The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId) {
		Label indicator = new Label(markupId, "<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR, null) + "\"/>");
		indicator.setEscapeModelStrings(false);
		indicator.add(new AttributeModifier("title", new Model("...")));
		return indicator;
	}

	private void setState(byte state) {
		if(this.state != state){
			addStateChange();
		}
		this.state = state;
	}
	
}
