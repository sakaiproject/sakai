/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
/*
 * This code is copied and slightly modified from the Apache Wicket project class
 * 	org.apache.wicket.extensions.ajax.markup.html.AjaxLazyLoadPanel
 * authored by jcompagner
 * 
 * That class is licensed under the following license:
 * 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.ui.player.components;

import org.apache.wicket.Component;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.ajax.AbstractDefaultAjaxBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

public abstract class LazyLoadPanel extends Panel {
	
	private static final long serialVersionUID = 1L;

	public LazyLoadPanel(String id, IModel model) {
		super(id, model);
		setOutputMarkupId(true);
		final Component loadingComponent = getLoadingComponent("content");
		add(loadingComponent.setRenderBodyOnly(true));

		add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			protected void respond(AjaxRequestTarget target)
			{
				Component component = getLazyLoadComponent("content", target);
				LazyLoadPanel.this.replace(component.setRenderBodyOnly(true));
				target.addComponent(LazyLoadPanel.this);
			}

			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				response.renderOnDomReadyJavascript(getCallbackScript().toString());
			}

			public boolean isEnabled(Component component)
			{
				return get("content") == loadingComponent;
			}
		});
		
		/*super(id);
		setOutputMarkupId(true);
		Component loadingComponent = getLoadingComponent("content");
		add(loadingComponent.setRenderBodyOnly(true));
		
		loadingComponent.add(new AbstractDefaultAjaxBehavior()
		{
			private static final long serialVersionUID = 1L;

			protected void respond(AjaxRequestTarget target)
			{
				Component component = getLazyLoadComponent("content", target);
				component.setOutputMarkupId(true);
				LazyLoadPanel.this.replace(component.setRenderBodyOnly(true));
				target.addComponent(LazyLoadPanel.this);
			}
		
			public void renderHead(IHeaderResponse response)
			{
				super.renderHead(response);
				response.renderOnDomReadyJavascript(getCallbackScript().toString());
			}
			
			// Ignore the onlyTargetActivePage param
			protected CharSequence getCallbackScript(boolean onlyTargetActivePage)
			{
				return generateCallbackScript("wicketAjaxGet('" + getCallbackUrl() + "'");
			}
			
			public CharSequence getCallbackUrl()
			{
				if (getComponent() == null)
				{
					throw new IllegalArgumentException(
							"Behavior must be bound to a component to create the URL");
				}
				
				final RequestListenerInterface rli;
				
				rli = IBehaviorListener.INTERFACE;
				
				WebRequest webRequest = (WebRequest)getComponent().getRequest();
				HttpServletRequest servletRequest = webRequest.getHttpServletRequest();

				String toolUrl = servletRequest.getContextPath();
				
				AppendingStringBuffer url = new AppendingStringBuffer();
				url.append(toolUrl).append("/");
				url.append(getComponent().urlFor(this, rli));

				return url;
			}
		});*/
	}

	/**
	 * @param markupId The components markupid.
	 * @return The component that must be lazy created.
	 */
	public abstract Component getLazyLoadComponent(String markupId, AjaxRequestTarget target);
	
	/**
	 * @param markupId The components markupid.
	 * @return The component to show while the real component is being created.
	 */
	public Component getLoadingComponent(String markupId)
	{
		return new Label(markupId,"<img src=\"" + RequestCycle.get().urlFor(AbstractDefaultAjaxBehavior.INDICATOR) + "\"/>").setEscapeModelStrings(false);
	}
	
	
}
