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
package org.sakaiproject.scorm.ui.player.components;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.wicket.markup.html.panel.Panel;

import org.sakaiproject.scorm.model.api.SessionBean;
import org.sakaiproject.user.api.User;

@Slf4j
public class AdminPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	@Getter @Setter protected String selectedLearner;
	@Getter @Setter protected String binding;
	@Getter @Setter protected String dataValue;

	public AdminPanel(String id, String contentPackageId, final SessionBean sessionBean)
	{
		super(id);
	}

	public class UserWrapper implements Serializable
	{
		private static final long serialVersionUID = 1L;

		@Getter private String id;
		@Getter private String displayName;

		public UserWrapper(User user)
		{
			this.id = user.getId();
			this.displayName = user.getDisplayName();
		}
	}
}
