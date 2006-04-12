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

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.sakaiproject.service.legacy.site.Site;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderEngine;

/**
 * Provides access to worksite information
 * 
 * @author ieb
 * 
 */
public class WorksiteInfoMacro extends BaseMacro {
	private static final String DESCRIPTION = "description";

	private static final String SHORTDESCRIPTION = "shortdescription";

	private static final String WIKISPACE = "wikispace";

	private static String[] paramDescription = {
			"1,info: The type of info to provide, worksiteinfo:title gives Title (default), "
					+ " worksiteinfo:description, "
					+ " worksiteinfo:shortdescription, "
					+ " worksiteinfo:wikispace ",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Generates worksite information";

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
		return "worksiteinfo";
	}


	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException {

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		SpecializedRenderEngine spRe = (SpecializedRenderEngine) context
				.getRenderEngine();
		
		String infotype = params.get("info", 0);
		Site s = context.getSite();
		if ( s != null ) {
			if (DESCRIPTION.equals(infotype)) {
				writer.write(s.getDescription());
			} else if (SHORTDESCRIPTION.equals(infotype)) {
				writer.write(s.getShortDescription());
			} else if (WIKISPACE.equals(infotype)) {
				writer.write(spRe.getSpace());
			} else {
				writer.write(s.getTitle());
			}
		} else {
			writer.write("No Site Found for page");
		}
	}
}
