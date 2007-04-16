/*
 * $Id: Clock.java 460265 2006-04-16 13:36:52Z jdonnerstag $ $Revision: 460265 $ $Date:
 * 2006-02-12 03:04:38 +0100 (So, 12 Feb 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.sakaiproject.scorm.client;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.model.AbstractReadOnlyModel;

/**
 * A simple component that displays current time
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class Clock extends Label
{
	/**
	 * Constructor
	 * 
	 * @param id
	 *            Component id
	 * @param tz
	 *            Timezone
	 */
	public Clock(String id, TimeZone tz)
	{
		super(id, new ClockModel(tz));

	}

	/**
	 * A model that returns current time in the specified timezone via a
	 * formatted string
	 * 
	 * @author Igor Vaynberg (ivaynberg)
	 */
	private static class ClockModel extends AbstractReadOnlyModel
	{
		private DateFormat df;

		/**
		 * @param tz
		 */
		public ClockModel(TimeZone tz)
		{
			df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
			df.setTimeZone(tz);
		}

		/**
		 * @see wicket.model.AbstractReadOnlyModel#getObject(wicket.Component)
		 */
		public Object getObject(Component component)
		{
			return df.format(new Date());
		}
	}
}