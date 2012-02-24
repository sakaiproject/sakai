/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

public class CourierTag extends UIComponentTag
{
	private String refresh = null;

	public String getComponentType()
	{
		return "org.sakaiproject.Courier";
	}

	public String getRendererType()
	{
		return "org.sakaiproject.Courier";
	}

	protected void setProperties(UIComponent component)
	{
		super.setProperties(component);

		TagUtil.setString(component, "refresh", refresh);
	}
	
	public void release()
	{
		super.release();
		refresh = null;
	}

	public String getRefresh()
	{
		return refresh;
	}
	public void setRefresh(String string)
	{
		refresh = string;
	}
}



