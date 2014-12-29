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
 * 
 * 
 * @author andrew
 */
// FIXME: Component
public class MathMacro extends BaseMacro
{

	public String getDescription()
	{
		return Messages.getString("MathMacro.0"); //$NON-NLS-1$
	}

	public String[] getParamDescription()
	{
		return new String[] { Messages.getString("MathMacro.1") }; //$NON-NLS-1$
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

		if (display != null && "display".equals(display)) //$NON-NLS-1$
		{
			inline = false;
		}

		String content = params.getContent();
		if (inline)
		{
			writer.write("<span class=\"math\">"); //$NON-NLS-1$
			writer.write(content);
			writer.write("</span>"); //$NON-NLS-1$
		}
		else
		{
			writer.write("<div class=\"math\">"); //$NON-NLS-1$
			writer.write(content);
			writer.write("</div>"); //$NON-NLS-1$
		}
	}

	public String getName()
	{
		return "math"; //$NON-NLS-1$
	}

}
