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

import org.radeox.api.macro.MacroParameter;
import org.radeox.macro.BaseMacro;
import org.radeox.util.Encoder;

import uk.ac.cam.caret.sakai.rwiki.component.Messages;
import uk.ac.cam.caret.sakai.rwiki.component.radeox.service.impl.SpecializedRenderContext;

/**
 * 
 * 
 * @author ieb
 */
public class ImageMacro extends BaseMacro
{


	public String[] getParamDescription()
	{
		return new String[] {
				Messages.getString("ImageMacro.0"), //$NON-NLS-1$
				Messages.getString("ImageMacro.1"), //$NON-NLS-1$
				Messages.getString("ImageMacro.2"), //$NON-NLS-1$
				Messages.getString("ImageMacro.3"), //$NON-NLS-1$
				Messages.getString("ImageMacro.4"), //$NON-NLS-1$
				Messages.getString("ImageMacro.5"), //$NON-NLS-1$
				Messages.getString("ImageMacro.6") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("ImageMacro.7"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "image"; //$NON-NLS-1$
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

		// This code is informed by SnipSnap image Macro, we have used it
		// so that the markup is the same.

		if (params.getLength() > 0)
		{
			String img = params.get("img"); //$NON-NLS-1$
			String alt = null, cssclass = null, target = null, title = null;
			boolean qualifiedParams = img != null;
			if (qualifiedParams)
			{
				alt = params.get("alt"); //$NON-NLS-1$
				title = params.get("title"); //$NON-NLS-1$
				//ext = params.get("ext");
				cssclass = params.get("class"); //$NON-NLS-1$
				target = params.get("target"); //$NON-NLS-1$
			}
			else
			{
				img = params.get(0);
				alt = params.get(1);
				//ext = params.get(2);
				cssclass = params.get(3);
				target = params.get(4);
				title = params.get(5);
			}

			if (title == null && alt != null)
			{
				title = alt;
			}

			String link = params.get("link"); //$NON-NLS-1$

			if (link != null)
			{
				link = context.convertLink(link);

				writer.write("<a href=\"" + Encoder.escape(link) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				if (target != null)
				{
					writer.write("target=\"" + Encoder.escape(target) + "\""); //$NON-NLS-1$ //$NON-NLS-2$
				}
				writer.write(">"); //$NON-NLS-1$
			}

			String imageName = img;
			// for the moment, just allow anything, In the future we will
			// do more processing and perhapse only allow resources
			if (imageName.startsWith("http://") //$NON-NLS-1$
					|| imageName.startsWith("https://") //$NON-NLS-1$
					|| imageName.startsWith("ftp://"))  //$NON-NLS-1$
			{
				throw new IllegalArgumentException(
						Messages.getString("ImageMacro.23")); //$NON-NLS-1$
			}

			imageName = context.convertLink(imageName);
			writer.write("<img src=\""); //$NON-NLS-1$
			writer.write(Encoder.escape(imageName));
			writer.write("\" "); //$NON-NLS-1$
			if (cssclass != null)
			{
				writer.write("class=\""); //$NON-NLS-1$
				writer.write(Encoder.escape(cssclass));
				writer.write("\" "); //$NON-NLS-1$
			}
			if (alt != null)
			{
				writer.write("alt=\"" + Encoder.escape(alt) + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (title != null)
			{
				writer.write("title=\"" + Encoder.escape(title) + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			writer.write("border=\"0\"/>"); //$NON-NLS-1$

			if (link != null)
			{
				writer.write("</a>"); //$NON-NLS-1$
			}
		}
		else
		{
			throw new IllegalArgumentException(
					Messages.getString("ImageMacro.34")); //$NON-NLS-1$
		}
		return;

	}
}

/*******************************************************************************
 * $Header$
 ******************************************************************************/

