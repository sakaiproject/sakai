/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

package uk.ac.cam.caret.sakai.rwiki.component.macros;

import java.io.IOException;
import java.io.Writer;

import org.radeox.macro.BaseMacro;
import org.radeox.macro.parameter.MacroParameter;
import org.radeox.util.Encoder;

import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * FIXME needs localisation
 * 
 * @author ieb
 */
// FIXME: Component
public class ImageMacro extends BaseMacro
{
	private static String[] paramDescription = {
			"1,img: URL to the image, image URL must be relative, or absolute but cannot be external, It may also startwith worksite:/ or sakai:/ if referencing resources in the worksite or the whole of sakai. When referencing resources in other worksites (eg sakai:/) you must include the site id ",
			"2,alt: (optional) Alt text ",
			"3,ext: (optional) ignored at the moment",
			"4,class: (optional) css class applied to the image",
			"5,target: (optional) Target window",
			"6,title: (optional) Title the image, (will default to the same value as alt)",
			"Remember if using positional parameters, you must include dummies for the optional parameters" };

	private static String description = "Places an Image in the page";

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return description;
	}

	public String getName()
	{
		return "image";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#execute(java.io.Writer,
	 *      org.radeox.macro.parameter.MacroParameter)
	 */
	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		SpecializedRenderContext context = (SpecializedRenderContext) params
				.getContext();

		String siteId = context.getSiteId();
		// This code is informed by SnipSnap image Macro, we have used it
		// so that the markup is the same.

		if (params.getLength() > 0)
		{
			String img = params.get("img");
			String alt = null, ext = null, cssclass = null, target = null, title = null;
			boolean qualifiedParams = img != null;
			if (qualifiedParams)
			{
				alt = params.get("alt");
				title = params.get("title");
				ext = params.get("ext");
				cssclass = params.get("class");
				target = params.get("target");
			}
			else
			{
				img = params.get(0);
				alt = params.get(1);
				ext = params.get(2);
				cssclass = params.get(3);
				target = params.get(4);
				title = params.get(5);
			}

			if (title == null && alt != null)
			{
				title = alt;
			}

			String link = params.get("link");

			if (link != null)
			{
				// check url for sakai:// or worksite://
				if (link.startsWith("sakai:/"))
				{
					link = "/access/content/group/"
							+ link.substring("sakai:/".length());
				}
				else if (link.startsWith("worksite:/"))
				{
					link = "/access/content/group/" + siteId + "/"
							+ link.substring("worksite:/".length());
				}

				writer.write("<a href=\"" + Encoder.escape(link) + "\"");
				if (target != null)
				{
					writer.write("target=\"" + Encoder.escape(target) + "\"");
				}
				writer.write(">");
			}

			String imageName = img;
			// for the moment, just allow anything, In the future we will
			// do more processing and perhapse only allow resources
			if (imageName.startsWith("http://")
					|| imageName.startsWith("https://")
					|| imageName.startsWith("ftp://"))
				throw new IllegalArgumentException(
						"External URLs are not allowed, only relative or absolute");
			if (imageName.startsWith("worksite:/"))
			{
				imageName = imageName.substring("worksite:/".length());
				imageName = "/access/content/group/" + siteId + "/" + imageName;
			}
			if (imageName.startsWith("sakai:/"))
			{
				imageName = imageName.substring("sakai:/".length());
				imageName = "/access/content/group/" + imageName;
			}
			writer.write("<img src=\"");
			writer.write(imageName);
			writer.write("\" ");
			if (cssclass != null)
			{
				writer.write("class=\"");
				writer.write(cssclass);
				writer.write("\" ");
			}
			if (alt != null)
			{
				writer.write("alt=\"" + Encoder.escape(alt) + "\" ");
			}
			if (title != null)
			{
				writer.write("title=\"" + Encoder.escape(title) + "\" ");
			}
			writer.write("border=\"0\"/>");

			if (link != null)
			{
				writer.write("</a>");
			}
		}
		else
		{
			throw new IllegalArgumentException(
					"Number of arguments does not match");
		}
		return;

	}

}

/*******************************************************************************
 * $Header$
 ******************************************************************************/

