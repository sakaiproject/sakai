/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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



package org.sakaiproject.jsf.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import org.sakaiproject.jsf.util.TagUtil;

public class ViewTag extends UIComponentTag
{
	private String m_title = null;
	private String m_toolCssHref = null;

	public String getComponentType()
	{
		return "org.sakaiproject.View";
	}

	public String getRendererType()
	{
		return "org.sakaiproject.View";
	}

	public String getTitle()
	{
		return m_title;
	}

	protected void setProperties(UIComponent component)
	{
		super.setProperties(component);

		TagUtil.setString(component, "title", m_title);
		TagUtil.setString(component, "toolCssHref", m_toolCssHref);
	}

	public void setTitle(String string)
	{
		m_title = string;
	}

	public String getToolCssHref() {
		return m_toolCssHref;
	}

	public void setToolCssHref(String cssHref) {
		m_toolCssHref = cssHref;
	}
}



