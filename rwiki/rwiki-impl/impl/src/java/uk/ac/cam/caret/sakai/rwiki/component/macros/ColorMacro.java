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

/**
 * Basic ColorMacro to change the foreground color and background color of some
 * contents. 
 * 
 * @author andrew
 */
public class ColorMacro extends BaseMacro
{

	private static final String COLOR_PARAM = "color"; //$NON-NLS-1$

	private static final String BACKGROUND_COLOR_PARAM = "bgcolor"; //$NON-NLS-1$



	public String getName()
	{
		return "color"; //$NON-NLS-1$
	}

	public String[] getParamDescription()
	{
		return new String[] {
			Messages.getString("ColorMacro.2"), //$NON-NLS-1$
			Messages.getString("ColorMacro.3") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return Messages.getString("ColorMacro.4"); //$NON-NLS-1$
	}

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{
		writer.write("<span style='"); //$NON-NLS-1$

		String color = params.get(COLOR_PARAM);
		String backgroundColor = params.get(BACKGROUND_COLOR_PARAM);
		if (color == null)
		{
			// Assume we are using attributes
			color = params.get(0);
			backgroundColor = params.get(1);
		}

		// Parse color
		writer.write(parse(color, "color: ")); //$NON-NLS-1$

		writer.write(parse(backgroundColor, "background: ")); //$NON-NLS-1$

		writer.write("'>"); //$NON-NLS-1$
		if (params.getContent() != null)
		{
			writer.write(params.getContent());
		}
		writer.write("</span>"); //$NON-NLS-1$
	}

	private String parse(String color, String cssClass)
	{
		if (color == null) return ""; //$NON-NLS-1$

		// simplest thing remove all (: ; /* */) from the CSS
		char[] disallowedChars = { ':', ';', '/', '*', '{', '}', '"', '\'',
				'\\' };
		for (int i = 0; i < disallowedChars.length; i++)
		{
			if (color.indexOf(disallowedChars[i]) > 0)
			{
				throw new IllegalArgumentException(Messages.getString("ColorMacro.1") + color //$NON-NLS-1$
						+ Messages.getString("ColorMacro.13")); //$NON-NLS-1$
			}
		}
		return cssClass + color + ';';
	}

}
