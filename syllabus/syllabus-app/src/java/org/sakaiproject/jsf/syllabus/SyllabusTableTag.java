/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.jsf.syllabus;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.webapp.UIComponentTag;
import lombok.Setter;
import org.sakaiproject.jsf2.util.TagUtil;

@Setter
public class SyllabusTableTag extends UIComponentTag
{
	private String summary;
	private String styleClass;
	private String value;
	private String var;

	@Override
	protected void setProperties(UIComponent component)
	{
		super.setProperties(component);
		TagUtil.setString(component, "summary", summary);
		TagUtil.setString(component, "styleClass", styleClass);
		TagUtil.setObject(component, "value", value);
		((UIData) component).setVar(var);
	}

	@Override
	public void release()
	{
		super.release();
		summary = null;
		styleClass = null;
		value = null;
		var = null;
	}

	@Override
	public String getRendererType()
	{
		return "jakarta.faces.Table";
	}

	public String getComponentType()
	{
		return "SakaiSyllabusTable";
	}
}


