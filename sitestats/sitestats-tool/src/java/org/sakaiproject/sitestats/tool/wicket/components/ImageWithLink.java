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
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.sakaiproject.sitestats.api.StatsManager;


/**
 * @author Nuno Fernandes
 */
public class ImageWithLink extends Panel {
	private static final long		serialVersionUID	= 1L;

	public ImageWithLink(String id) {
		this(id, null, null, null, null);
	}
	
	public ImageWithLink(String id, String imgUrl, String lnkUrl, String lnkLabel, String lnkTarget) {
		super(id);
		setRenderBodyOnly(false);
		boolean exists = (lnkTarget != null && lnkLabel != null && lnkUrl != null);
		ExternalLink lnk = null;
		if(exists) {
			add( new ExternalImage("image", imgUrl).setVisible(imgUrl != null) );
			lnk = new ExternalLink("link", lnkUrl, lnkLabel);
			lnk.add(new AttributeModifier("target", true, new Model(lnkTarget)));
		}else{
			StringBuilder b = new StringBuilder();
			b.append(lnkLabel);
			b.append(' ');
			b.append(((String) new ResourceModel("resource_unknown").getObject()));
			add( new ExternalImage("image", StatsManager.SILK_ICONS_DIR + "cross.png").setVisible(true) );
			lnk = new ExternalLink("link", lnkUrl, b.toString());
			lnk.setEnabled(false);
		}
		add(lnk);
	}
}
