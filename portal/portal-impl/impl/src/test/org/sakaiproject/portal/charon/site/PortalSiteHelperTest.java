package org.sakaiproject.portal.charon.site;

import junit.framework.TestCase;

public class PortalSiteHelperTest extends TestCase {

	private static final String [] SITE_TITLES = new String[] {
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"Short",
		"Not so long title"
	};
	
	private static final String [] CUT_METHODS = new String[]{"100:0","50:50","0:100","70:30","-1","100:100","a:b"};
	private static final int [] MAX_LENGTHS = new int[]{25,50,15,8,25,50,125};
	private static final String [] CUT_SEPARATORS = new String[]{" ...","...","{..}"," ...","...","{..}","......"};
	
	
	public void testGetResumeTitles() {
		for (int k=0; k<SITE_TITLES.length; k++) {
			getResumeTitle(SITE_TITLES[k]);
		}
	}
	
	public void getResumeTitle(String siteTitle) {
		PortalSiteHelperImpl helper = new PortalSiteHelperImpl(null,false);
		for (int k=0; k<CUT_METHODS.length; k++) {
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
package org.sakaiproject.portal.charon.site;

import junit.framework.TestCase;

public class PortalSiteHelperTest extends TestCase {

	private static final String [] SITE_TITLES = new String[] {
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"This is a really long site with a very high number of characters on it, an probably with strange behaviour in the portal.",
		"Short",
		"Not so long title"
	};
	
	private static final String [] CUT_METHODS = new String[]{"100:0","50:50","0:100","70:30","-1","100:100","a:b"};
	private static final int [] MAX_LENGTHS = new int[]{25,50,15,8,25,50,125};
	private static final String [] CUT_SEPARATORS = new String[]{" ...","...","{..}"," ...","...","{..}","......"};
	
	
	public void testGetResumeTitles() {
		for (int k=0; k<SITE_TITLES.length; k++) {
			getResumeTitle(SITE_TITLES[k]);
		}
	}
	
	public void getResumeTitle(String siteTitle) {
		PortalSiteHelperImpl helper = new PortalSiteHelperImpl(null,false);
		for (int k=0; k<CUT_METHODS.length; k++) {
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
