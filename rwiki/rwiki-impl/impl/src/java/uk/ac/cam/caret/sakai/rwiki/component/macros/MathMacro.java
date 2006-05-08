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
 * FIXME needs localisation
 * 
 * @author andrew
 */
// FIXME: Component
public class MathMacro extends BaseMacro
{

	private static final String description = "This is a basic macro that places span/div tags with an appropriate class around the math text. \nThe contents of this macro are pre-escaped, however you cannot put {math} in the contents of this macro, place {{}math} instead.";

	private static final String[] paramDescription = { "1: put \"display\" here for the maths to be placed in a div" };

	public String getDescription()
	{
		return description;
	}

	public String[] getParamDescription()
	{
		return paramDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.BaseMacro#execute(java.io.Writer,
	 *      org.radeox.macro.parameter.MacroParameter)
	 */
	public void execute(Writer writer, MacroParameter params)
			throws IllegalArgumentException, IOException
	{

		String display = params.get(0);
		boolean inline = true;

		if (display != null && display.equals("display"))
		{
			inline = false;
		}

		String content = params.getContent();
		if (inline)
		{
			writer.write("<span class=\"math\">");
			writer.write(content);
			writer.write("</span>");
		}
		else
		{
			writer.write("<div class=\"math\">");
			writer.write(content);
			writer.write("</div>");
		}
	}

	public String getName()
	{
		return "math";
	}

}
