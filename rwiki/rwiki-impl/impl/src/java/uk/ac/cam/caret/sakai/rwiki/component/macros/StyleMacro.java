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

public class StyleMacro extends BaseMacro
{


	public String[] getParamDescription()
	{
		return new String[] {
			Messages.getString("StyleMacro.0"), //$NON-NLS-1$
			Messages.getString("StyleMacro.1") }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.radeox.macro.Macro#getDescription()
	 */
	public String getDescription()
	{
		return  Messages.getString("StyleMacro.2"); //$NON-NLS-1$
	}

	public String getName()
	{
		return "style"; //$NON-NLS-1$
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

		String href = params.get("href");
		String media = "all";
		boolean qualifiedParams = href != null;
		if (qualifiedParams)
		{
			media = params.get("media"); //$NON-NLS-1$
		}
		else
		{
			href = params.get(0);
			media = params.get(1);
		}
		if ( media == null || media.length() == 0 ) {
			media = "all";
		}
		if ( href != null && href.startsWith("http") ) {
			writer.write("<link type=\"text/css\" rel=\"StyleSheet\" media=\"");
			writer.write(media);
			writer.write("\" href=\"");
			writer.write(href);
			if (params.getContent() != null) {
				writer.write("\" > ");
				writer.write("\n<!\\-\\-\n");
				writer.write(params.getContent());
				writer.write("\n\\-\\->\n");
				writer.write("</link>\n"); //$NON-NLS-1$
			} else {
				writer.write("\" /> \n");
			}
		} else {
			writer.write("\n<style>\n<!\\-\\-\n"); 
			if (params.getContent() != null) {
				writer.write(params.getContent());
			}
			writer.write("\n\\-\\->\n</div>\n"); //$NON-NLS-1$
		}
	}
}