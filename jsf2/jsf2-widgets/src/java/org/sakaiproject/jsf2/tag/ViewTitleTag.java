/**********************************************************************************
Copyright (c) 2018 Apereo Foundation

Licensed under the Educational Community License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 **********************************************************************************/

package org.sakaiproject.jsf2.tag;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;
import org.sakaiproject.jsf2.util.TagUtil;

public class ViewTitleTag extends UIComponentTag
{
	private String indent;
	private String value;

	public String getComponentType()
	{
		return ("javax.faces.Output");
	}

	public String getRendererType()
	{
		return "org.sakaiproject.ViewTitle";
	}

	protected void setProperties(UIComponent component)
	{

		super.setProperties(component);

		TagUtil.setInteger(component, "indent", indent);
		TagUtil.setString(component, "value", value);
	}

	public void release()
	{
		indent = null;
		value = null;
	}

	public String getIndent() {
		return indent;
	}
	public void setIndent(String indent) {
		this.indent = indent;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
