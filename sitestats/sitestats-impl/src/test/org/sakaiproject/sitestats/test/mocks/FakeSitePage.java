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
package org.sakaiproject.sitestats.test.mocks;

import java.util.ArrayList;
import java.util.List;

import org.mockito.Mockito;
import org.sakaiproject.site.api.SitePage;
import org.sakaiproject.site.api.ToolConfiguration;

@SuppressWarnings("serial")
public abstract class FakeSitePage implements SitePage {
	private String siteId;
	private String toolId;

	public FakeSitePage set(String siteId, String toolId) {
		this.siteId = siteId;
		this.toolId = toolId;
		return this;
	}

	public List getTools() {
		List<ToolConfiguration> tc = new ArrayList<ToolConfiguration>();
		tc.add(Mockito.spy(FakeToolConfiguration.class).set(toolId));
		return tc;
	}
}
