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

import java.io.Serializable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class CSSFeedbackPanel extends FeedbackPanel {
	private static final long	serialVersionUID	= 1L;

	public CSSFeedbackPanel(String id) {
		super(id);
		WebMarkupContainer feedbackul = (WebMarkupContainer) get("feedbackul");
		if(feedbackul != null){
			feedbackul.add(new AttributeModifier("class", true, new Model() {
				private static final long	serialVersionUID	= 1L;
				public Serializable getObject() {
					if(anyErrorMessage()){
						return "alertMessage";
					}else if(anyMessage()){
						return "success";
					}else{
						return "";
					}
				}
			}));
			feedbackul.add(new AttributeModifier("style", true, new Model("list-style-type:none")));
		}
	}
}
