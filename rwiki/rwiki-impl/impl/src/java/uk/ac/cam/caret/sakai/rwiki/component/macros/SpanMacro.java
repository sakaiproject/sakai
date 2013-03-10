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

import uk.ac.cam.caret.sakai.rwiki.component.Messages;

public class SpanMacro extends BaseMacro
{


	public String[] getParamDescription()
	{
		return new String[] {
			Messages.getString("SpanMacro.0"), //$NON-NLS-1$
			Messages.getString("SpanMacro.1"), //$NON-NLS-1$
			Messages.getString("SpanMacro.2") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("SpanMacro.3"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "span"; //$NON-NLS-1$
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

		String cssClass = params.get("class"); //$NON-NLS-1$
		if (cssClass == null)
		{
			cssClass = params.get(0);
			if (cssClass.startsWith("id=")) //$NON-NLS-1$
			{
				cssClass = null;
			}
			else if (cssClass.startsWith("name=")) //$NON-NLS-1$
			{
				cssClass = null;
			}
		}
		String id = params.get("id"); //$NON-NLS-1$

		String anchorName = params.get("name"); //$NON-NLS-1$

		writer.write("<span"); //$NON-NLS-1$
		if (cssClass != null && !"".equals(cssClass)) //$NON-NLS-1$
		{
			cssClass = cssClass.replaceAll("[^A-Za-z0-9]", "");
			writer.write(" class='"); //$NON-NLS-1$
			writer.write(cssClass); //$NON-NLS-1$ //$NON-NLS-2$
			writer.write('\'');
		}
		if (id != null && !"".equals(id)) //$NON-NLS-1$
		{
			writer.write(" id='"); //$NON-NLS-1$
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
		if (anchorName != null && !"".equals(anchorName)) //$NON-NLS-1$
		{
			writer.write("<a name=\""); //$NON-NLS-1$
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
			writer.write("'>"); //$NON-NLS-1$
			writer.write("<!-- --></a>"); //$NON-NLS-1$
		}
		if (params.getContent() != null)
		{
			writer.write(params.getContent());
		}
		writer.write("</span>"); //$NON-NLS-1$
	}
}
