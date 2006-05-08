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

/**
 * Basic ColorMacro to change the foreground color and background color of some
 * contents. FIXME needs localisation. May even need localisable naming!
 * 
 * @author andrew
 */
public class ColorMacro extends BaseMacro
{

	private static final String COLOR_PARAM = "color";

	private static final String BACKGROUND_COLOR_PARAM = "bgcolor";

	private static final String[] paramDescription = {
			"0,color: Change the foreground color of the contents",
			"1,bgcolor: Change the background color of the contents" };

	private static final String description = "Change the color of some text";

	public String getName()
	{
		return "color";
	}

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

	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{
		writer.write("<span style='");

		String color = params.get(COLOR_PARAM);
		String backgroundColor = params.get(BACKGROUND_COLOR_PARAM);
		if (color == null)
		{
			// Assume we are using attributes
			color = params.get(0);
			backgroundColor = params.get(1);
		}

		// Parse color
		writer.write(parse(color, "color: "));

		writer.write(parse(backgroundColor, "background: "));

		writer.write("'>");
		if (params.getContent() != null)
		{
			writer.write(params.getContent());
		}
		writer.write("</span>");
	}

	private String parse(String color, String cssClass)
	{
		if (color == null) return "";

		// simplest thing remove all (: ; /* */) from the CSS
		char[] disallowedChars = { ':', ';', '/', '*', '{', '}', '"', '\'',
				'\\' };
		for (int i = 0; i < disallowedChars.length; i++)
		{
			if (color.indexOf(disallowedChars[i]) > 0)
			{
				throw new IllegalArgumentException("Color: " + color
						+ " is not a real CSS color!");
			}
		}
		return cssClass + color + ';';
	}

}
