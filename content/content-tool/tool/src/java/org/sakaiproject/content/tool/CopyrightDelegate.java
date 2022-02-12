/**
 * Copyright (c) 2003-2018 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.tool;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.cheftool.Context;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.util.ParameterParser;

/**
 * Used to centralize any copyright related tasks
 * See SAK-39953
 *
 * @author bbailla2
 */
public class CopyrightDelegate
{
	private String copyrightStatus;	// Copyright Status takes its value from the dropdown (eg. "I hold copyright")
	private String copyrightInfo;	// Copyright Info takes its value from the text area (ie. when "Use copyright below." is selected)
	private boolean copyrightAlert;	// copyrightAlert takes it's value from the copyright alert checkbox ("Display copyright alert and require acknowledgement...")

	// Keys to put these into the context
	public static final String COPYRIGHT_STATUS_KEY = "copyrightStatus_selection";
	public static final String COPYRIGHT_INFO_KEY = "copyrightInfo_selection";
	public static final String COPYRIGHT_ALERT_KEY = "copyrightAlert_selection";

	/**
	 * Overload of captureCopyright; puts properties into this class's members
	 * @param params
	 */
	public void captureCopyright(ParameterParser params)
	{
		captureCopyright(params, null);
	}

	/**
	 * Captures copyright properties from the ParameterParser
	 * @param params the ParameterParser to pull the copyright properties from
	 * @param li the copyright properties will be set on li if it exists, otherwise they are stored in this class' members
	 */
	public void captureCopyright(ParameterParser params, ListItem li)
	{
		String copyright = StringUtils.trimToNull(params.getString("copyright"));
		if (copyright == null)
		{
			return;
		}

		String newCopyright = StringUtils.trimToNull(params.getString("newcopyright"));
		boolean crAlert = params.getBoolean("copyrightAlert");
		if (li == null)
		{
			copyrightStatus = copyright;
			copyrightInfo = newCopyright;
			copyrightAlert = crAlert;
		}
		else
		{
			li.setCopyrightInfo(copyright);
			li.setCopyrightStatus(newCopyright);
			li.setCopyrightAlert(crAlert);
		}
	}

	/**
	 * Sets the copyright properties from the private members of this onto the specified ResourcePropertiesEdit
	 * @param props the ResourcePropertiesEdit that the copyright properties will be applied to
	 */
	public void setCopyrightOnEntity(ResourcePropertiesEdit props)
	{
		setCopyrightOnEntity(props, null);
	}

	/**
	 * Sets copyright properties onto the specified ResourcePropertiesEdit. Does not commit changes
	 * @param props the ResourcePropertiesEdit that the copyright properties will be applied to
	 * @param li the copyright properties are sourced from li if it exists; from this class if li is null
	 */
	public void setCopyrightOnEntity(ResourcePropertiesEdit props, ListItem li)
	{
		String crInfo;
		String crStatus;
		boolean crAlert;
		if (li == null)
		{
			crStatus = StringUtils.trimToNull(copyrightStatus);
			crInfo = StringUtils.trimToNull(copyrightInfo);
			crAlert = copyrightAlert;
		}
		else
		{
			crStatus = StringUtils.trimToNull(li.getCopyrightStatus());
			crInfo = StringUtils.trimToNull(li.getCopyrightInfo());
			crAlert = li.hasCopyrightAlert();
		}

		if (crStatus == null)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
		}
		else
		{
			props.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE, crStatus);
		}

		if (crInfo == null)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT);
		}
		else
		{
			props.addProperty(ResourceProperties.PROP_COPYRIGHT, crInfo);
		}

		if (crAlert)
		{
			props.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.TRUE.toString());
		}
		else
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
		}
	}

	/**
	 * Puts previously parsed values into the context (e.g. by UserInputPreserver)
	 */
	public void reloadSelectionsInContext(Context context)
	{
		context.put(COPYRIGHT_STATUS_KEY, copyrightStatus);
		context.put(COPYRIGHT_INFO_KEY, copyrightInfo);
		context.put(COPYRIGHT_ALERT_KEY, copyrightAlert);
	}
}
