/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/portal/trunk/portal-impl/impl/src/java/org/sakaiproject/portal/charon/CharonPortal.java $
 * $Id: CharonPortal.java 122221 2013-04-04 21:24:12Z ottenhoff@longsight.com $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.portal.charon.site;

import org.sakaiproject.tool.api.ToolManager;

//import junit.framework.TestCase;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PortalSiteHelperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    ToolManager toolManager;

	private static final String [] SITE_TITLES = new String[] {
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"Short",
		"Not so long title"
	};
	
	private static final String [] CUT_METHODS = new String[]{"100:0","50:50","0:100","70:30","-1","100:100","a:b"};
	private static final int [] MAX_LENGTHS = new int[]{25,50,15,8,25,50,125};
	private static final String [] CUT_SEPARATORS = new String[]{" ...","...","{..}"," ...","...","{..}","......"};
	private PortalSiteHelperImpl helper;
	
    @Before
    public void setUp() throws Exception {
		helper = new PortalSiteHelperImpl(null,false);
		helper.setToolManager(toolManager);
    }

	@Test
	public void testGetResumeTitles() {
		for (int k=0; k<SITE_TITLES.length; k++) {
			getResumeTitle(SITE_TITLES[k]);
		}
	}
	
	public void getResumeTitle(String siteTitle) {
		for (int k=0; k<CUT_METHODS.length; k++) {
			assertNotNull(helper);
			String resumeTitle = helper.getResumeTitle(siteTitle,CUT_METHODS[k],MAX_LENGTHS[k],CUT_SEPARATORS[k]);
			// The resume title has the defined length (if has enough length)
			assertEquals(Math.min(MAX_LENGTHS[k],siteTitle.length()),resumeTitle.length());
			if (siteTitle.length()>MAX_LENGTHS[k] && MAX_LENGTHS[k]>=10) {
				// The title must to be cut, so it has to contains cut separator
				assertEquals(true,resumeTitle.contains(CUT_SEPARATORS[k]));
				int [] finalCutMethod = helper.getCutMethod(CUT_METHODS[k]);
				// Check if ends or start with cut separator
				if (finalCutMethod[1]==0) assertEquals(true,resumeTitle.endsWith(CUT_SEPARATORS[k]));
				else assertEquals(false,resumeTitle.endsWith(CUT_SEPARATORS[k]));
				if (finalCutMethod[0]==0) assertEquals(true,resumeTitle.startsWith(CUT_SEPARATORS[k]));
				else assertEquals(false,resumeTitle.startsWith(CUT_SEPARATORS[k]));
			} else if (siteTitle.length()>MAX_LENGTHS[k]) {
				// Title truncate
				assertEquals(siteTitle.trim().substring(0,MAX_LENGTHS[k]),resumeTitle);
			} else {
				// Title without change
				assertEquals(siteTitle.trim(),resumeTitle);
			}
		}
	}
	
}
