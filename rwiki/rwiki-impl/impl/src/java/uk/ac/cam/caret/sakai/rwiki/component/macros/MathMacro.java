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
