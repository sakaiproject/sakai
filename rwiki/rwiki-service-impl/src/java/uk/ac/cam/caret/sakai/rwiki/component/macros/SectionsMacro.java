/**********************************************************************************
 *
 * $Header$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 University of Cambridge
 * 
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 * 
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/

package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import org.radeox.api.engine.RenderEngine;
import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.sakaiproject.api.section.coursemanagement.CourseSection;
import org.sakaiproject.component.section.cover.SectionAwareness;
import org.sakaiproject.service.framework.portal.cover.PortalService;
import org.sakaiproject.service.legacy.site.Site;
import org.sakaiproject.service.legacy.site.cover.SiteService;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * This is a reimplementation of the LinkMacro but made aware of the sakai://
 * and worksite:// url formats
 * 
 * @author andrew
 * 
 */
public class SectionsMacro extends BaseMacro {
	private static String[] paramDescription = {
			"1,useids: (optional) if true will generate with ID's otherwise will use names, names it the default ",
			"2,categories: (optional) list of comma seperated categories to generate links for ",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Generate a list of links that point to section subsites";

	public String[] getParamDescription() {
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription() {
		return description;
	}
	 public String getName() {
	        return "sakai-sections";
	 }

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException {

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		RenderEngine engine = context.getRenderEngine();

		String useids = params.get("useids", 0);
		String categories = params.get("categories", 1);

		String siteId = PortalService.getCurrentSiteId();
		Site s = null;
		try {
			s = SiteService.getSite(siteId);
		} catch (Exception ex) {

		}
		
		List sections = null;
		if (categories != null && categories.length() > 0) {
			sections = SectionAwareness.getSectionsInCategory(siteId,
					categories);
		} else {
			sections = SectionAwareness.getSections(siteId);
		}
		
		
		for (Iterator is = sections.iterator(); is.hasNext();) {
			CourseSection cs = (CourseSection) is.next();
			String pageName = "";
			
			if ( "true".equals(useids) ) {
				pageName = cs.getUuid()+"/Home";
			} else {
				if ( s !=null ) {
					pageName = s.getReference()+"/";
				}
				pageName += "section/"+cs.getTitle()+"/Home";
			}
			writer.write("\n");
			writer.write("* [ Section: ");
			writer.write(cs.getTitle());
			writer.write("|");
			writer.write(pageName);
			writer.write("]");
		}
		writer.write("\n");
		return;
	}
}
