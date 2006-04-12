/**********************************************************************************
 *
 * $Header$
 *
 * This file is derived from code in the "SnipSnap Radeox Rendering Engine" which
 * was licensed under the ASF License Version 2.0 and contains copyright notices from
 * below.
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Derivations are:
 *  Copyright (c) 2005 University of Cambridge
 *
 * Please visit http://radeox.org/ for updates and contact.
 *
 * --ORIGINAL LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --ORIGINAL LICENSE NOTICE--
 * 
 * From what I can see of the ASF I can now license this code under the ECL as below:
 * 
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
 * 
 **/

package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.api.engine.ImageRenderEngine;
import org.radeox.api.engine.RenderEngine;
import org.radeox.macro.BaseLocaleMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.Encoder;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * This is a reimplementation of the LinkMacro but made aware of the sakai://
 * and worksite:// url formats
 * 
 * @author andrew
 * 
 */
public class SakaiLinkMacro extends BaseLocaleMacro {
	private static String[] paramDescription = {
			"1,text: Text of the link ",
			"2,url: URL of the link, if this is external and no target is specified, a new window will open ",
			"3,img: (optional) if 'none' then no small URL image will be used",
			"4,target: (optional) Target window, if 'none' is specified, the url will use the current window",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Generated a link";

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

	public String getLocaleKey() {
		return "macro.link";
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException {

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();
		RenderEngine engine = context.getRenderEngine();

		String text = params.get("text", 0);
		String url = params.get("url", 1);
		String img = params.get("img", 2);
		String target = params.get("target", 3);

		// check for single url argument (text == url)
		if (params.getLength() == 1) {
			url = text;
			text = Encoder.toEntity(text.charAt(0))
					+ Encoder.escape(text.substring(1));
		}

		if (url != null && text != null) {
			if (target == null) {
				if (url.indexOf("://") >= 0 && url.indexOf("://") < 10) {
					target = "rwikiexternal";
				} else {
					target = "none";
				}

			}

			// check url for sakai:// or worksite://
			if (url.startsWith("sakai:/")) {
				url = "/access/content/group/"
						+ url.substring("sakai:/".length());
			} else if (url.startsWith("worksite:/")) {
				url = "/access/content/group/" + context.getSiteId() + "/"
						+ url.substring("worksite:/".length());
			}

			writer.write("<span class=\"nobr\">");
			if (!"none".equals(img) && engine instanceof ImageRenderEngine) {
				writer.write(((ImageRenderEngine) engine)
						.getExternalImageLink());
			}
			writer.write("<a href=\"");
			writer.write(url);
			writer.write("\"");
			if (!"none".equals(target)) {
				writer.write(" target=\"");
				writer.write(target);
				writer.write("\" ");
			}
			writer.write(">");
			writer.write(text);
			writer.write("</a></span>");
		} else {
			throw new IllegalArgumentException(
					"link needs a name and a url as argument");
		}
		return;
	}
}
