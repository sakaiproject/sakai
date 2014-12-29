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
 * @author denny.denny at gmail.com
 */
public class CommentMacro extends BaseMacro
{
	static final String[] COLORS = {"#C7D22C", "#CBBEDA", "#77BBCE", "#9AAFDC", "#E5438E", "#F7E81D", "#EB94B6", "#EC9B26", 
		"#EC5B60", "#A883B9"};
	int nextItem = 0;

	
	public String getDescription()
	{
		return Messages.getString("CommentMacro.0");
	}

	public String[] getParamDescription()
	{
		return new String[] { Messages.getString("CommentMacro.1") };
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
		String color = COLORS[nextItem];
		
		writer.write("<span class=\"inline-wiki-comment-text\" style=\"background-color: " + color + "\">");
		if (params.getContent() != null) {
			writer.write(params.getContent());
		}
		writer.write("</span>");
		writer.write("<span class=\"inline-wiki-comment\" style=\"background-color: " + color + "\">");
		if (params.get(0) != null) {
			writer.write(params.get(0));
		}
		writer.write("</span>");
		nextItem = (nextItem + 1) % COLORS.length;
	}

	public String getName()
	{
		return "comment";
	}

}
