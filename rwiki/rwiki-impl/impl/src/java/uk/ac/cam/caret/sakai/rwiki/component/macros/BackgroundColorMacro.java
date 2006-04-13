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

/**
 * Basic BackgroundColorMacro to change the background color of some contents.
 * FIXME needs localisation. May even need localisable naming!
 * 
 * @author andrew
 */
public class BackgroundColorMacro extends BaseMacro
{

	private static final String BACKGROUND_COLOR_PARAM = "bgcolor";

	private static final String[] paramDescription = { "0,color: Change the background color of the contents" };

	private static final String description = "Change the background color of some text";

	public String getName()
	{
		return "bgcolor";
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

		String color = params.get(BACKGROUND_COLOR_PARAM);
		if (color == null)
		{
			// Assume we are using attributes
			color = params.get(0);
		}

		// Parse color
		writer.write(parse(color, "background: "));
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
