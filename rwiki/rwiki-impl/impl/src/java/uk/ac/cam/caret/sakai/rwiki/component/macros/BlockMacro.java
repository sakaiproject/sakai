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

public class BlockMacro extends BaseMacro
{
	private static String[] paramDescription = {
			"1,class: The class to assign to this block.",
			"id: An id to assign to this block.",
			"anchor: An anchor to assign to this block" };

	private static String description = "Places a div around a block of rwiki rendered content.";

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
		return "block";
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

		String cssClass = params.get("class");
		if (cssClass == null)
		{
			cssClass = params.get(0);
			if (cssClass.startsWith("id="))
			{
				cssClass = null;
			}
			else if (cssClass.startsWith("name="))
			{
				cssClass = null;
			}
		}
		String id = params.get("id");

		String anchorName = params.get("name");

		writer.write("<div");
		if (cssClass != null && !cssClass.equals(""))
		{
			writer.write(" class='");
			writer.write(cssClass.replaceAll("'", "&apos;"));
			writer.write('\'');
		}
		if (id != null && !id.equals(""))
		{
			writer.write(" id='");
			char[] nameChars = id.toCharArray();
			int end = 0;
			for (int i = 0; i < nameChars.length; i++)
			{
				if (Character.isLetterOrDigit(nameChars[i]))
				{
					nameChars[end++] = nameChars[i];
				}
			}
			if (end > 0)
			{
				writer.write(nameChars, 0, end);
			}
			writer.write('\'');
		}
		writer.write('>');
		if (anchorName != null && !anchorName.equals(""))
		{
			writer.write("<a name=\"");
			char[] nameChars = anchorName.toCharArray();
			int end = 0;
			for (int i = 0; i < nameChars.length; i++)
			{
				if (Character.isLetterOrDigit(nameChars[i]))
				{
					nameChars[end++] = nameChars[i];
				}
			}
			if (end > 0)
			{
				writer.write(nameChars, 0, end);
			}
			writer.write("'>");
			writer.write("<!-- --></a>");
		}
		if (params.getContent() != null)
		{
			writer.write(params.getContent());
		}
		writer.write("</div>");
	}
}
