/**********************************************************************************
 * $URL:$
 * $Id:$
 ***********************************************************************************
 *
 * Copyright (c) 2008 The Sakai Foundation
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

package org.sakaiproject.content.metadata.model;

import java.util.Collections;
import java.util.Map;

import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;

/**
 * @author Colin Hebert
 */
public class DateMetadataType extends MetadataType<Time>
{

	private static final long serialVersionUID = 1L;
	private String minimumDateTime;
	private String maximumDateTime;
	private boolean date = true;
	private boolean time = false;
	private boolean defaultToday = true;

	public String getMinimumDateTime()
	{
		return minimumDateTime;
	}

	public void setMinimumDateTime(String minimumDateTime)
	{
		this.minimumDateTime = minimumDateTime;
	}

	public String getMaximumDateTime()
	{
		return maximumDateTime;
	}

	public void setMaximumDateTime(String maximumDateTime)
	{
		this.maximumDateTime = maximumDateTime;
	}

	public boolean isDate()
	{
		return date;
	}

	public void setDate(boolean date)
	{
		this.date = date;
	}

	public boolean isTime()
	{
		return time;
	}

	public void setTime(boolean time)
	{
		this.time = time;
	}

	public boolean isDefaultToday()
	{
		return defaultToday;
	}

	public void setDefaultToday(boolean defaultToday)
	{
		this.defaultToday = defaultToday;
	}

	@Override
	/**
	 * {@inheritDoc}
	 *
	 * Returns today's date if the default has been set to today
	 */
	public Time getDefaultValue()
	{
		return defaultToday ? TimeService.newTime() : super.getDefaultValue();
	}

	@Override
	public MetadataRenderer getRenderer()
	{
		return new DateMetadataRenderer();
	}

	@Override
	public MetadataConverter<Time> getConverter()
	{
		return new DateTimeConverter();
	}

	@Override
	public MetadataValidator<Time> getValidator()
	{
		return new DateMetadataValidator();
	}


	private final class DateMetadataValidator implements MetadataValidator<Time>
	{
		public boolean validate(Time metadataValue)
		{
			if (metadataValue == null)
				return !isRequired();
			//assume format yyyyMMddhhmm
			try {
				if (minimumDateTime != null && !minimumDateTime.isEmpty()  && metadataValue.before(TimeService.newTimeLocal(Integer.parseInt(minimumDateTime.substring(0, 4)),
						Integer.parseInt(minimumDateTime.substring(4, 6)), Integer.parseInt(minimumDateTime.substring(6, 8)), Integer.parseInt(minimumDateTime.substring(8, 10)),
						Integer.parseInt(minimumDateTime.substring(10, 12)), 0, 0)))
					return false;
				if (maximumDateTime != null && !maximumDateTime.isEmpty() && metadataValue.after(TimeService.newTimeLocal(Integer.parseInt(maximumDateTime.substring(0, 4)),
						Integer.parseInt(maximumDateTime.substring(4, 6)), Integer.parseInt(maximumDateTime.substring(6, 8)), Integer.parseInt(maximumDateTime.substring(8, 10)),
						Integer.parseInt(maximumDateTime.substring(10, 12)), 0, 0)))
					return false;
			} catch(Exception e){}

			return true;
		}
	}

	private final class DateTimeConverter implements MetadataConverter<Time>
	{
		public String toString(Time metadataValue)
		{
			return (metadataValue != null) ? metadataValue.toStringLocal() : null;
		}

		public Time fromString(String stringValue)
		{
			return (stringValue != null && !stringValue.isEmpty()) ? TimeService.newTimeLocal(Integer.parseInt(stringValue.substring(0, 4)),
					Integer.parseInt(stringValue.substring(4, 6)), Integer.parseInt(stringValue.substring(6, 8)), Integer.parseInt(stringValue.substring(8, 10)),
					Integer.parseInt(stringValue.substring(10, 12)), 0, 0) : null;
		}

		public Map<String, ?> toProperties(Time metadataValue)
		{
			String stringValue = toString(metadataValue);
			return Collections.singletonMap(getUniqueName(), stringValue);
		}

		public Time fromProperties(Map<String, ?> properties)
		{
			return fromString((String) properties.get(getUniqueName()));
		}

		public Time fromHttpForm(Map<String, ?> parameters, String parameterSuffix)
		{
			int year=0,month=0,day=0,hour=0,min=0;
			String param;
			String str_year,str_month,str_day,str_hour,str_min;
			
			if(date) {
				param = (String)parameters.get(getUniqueName() + "_year_" + parameterSuffix);
				year = (param != null && !"".equals(param)) ? Integer.parseInt(param) : 0;
				param = (String)parameters.get(getUniqueName() + "_month_" + parameterSuffix);
				month = (param != null && !"".equals(param)) ? Integer.parseInt(param) : 0;
				param = (String)parameters.get(getUniqueName() + "_day_" + parameterSuffix);
				day = (param != null && !"".equals(param)) ? Integer.parseInt(param) : 0;
			}
			
			if(time) {
				param = (String)parameters.get(getUniqueName() + "_hour_" + parameterSuffix);
				hour = (param != null && !"".equals(param)) ? Integer.parseInt(param) : 0;
				param = (String)parameters.get(getUniqueName() + "_minute_" + parameterSuffix);
				min = (param != null && !"".equals(param)) ? Integer.parseInt(param) : 0;
				
				String ampm = (String) parameters.get(getUniqueName() + "_ampm_" + parameterSuffix);
				if("pm".equals(ampm))
				{
					if(hour < 12)
					{
						hour += 12;
					}
				}
				else if(hour == 12)
				{
					hour = 0;
				}
			}
			str_year = String.format("%04d", year);
			str_month = String.format("%02d", month);
			str_day = String.format("%02d", day);
			str_hour = String.format("%02d", hour);
			str_min = String.format("%02d", min);

			return fromString(str_year+str_month+str_day+str_hour+str_min+"00000");
		}
	}
	
	private final static class DateMetadataRenderer implements MetadataRenderer
	{

		public String getMetadataTypeEditTemplate()
		{
			return null;	//To change body of implemented methods use File | Settings | File Templates.
		}

		public String getMetadataTypeDisplayTemplate()
		{
			return null;	//To change body of implemented methods use File | Settings | File Templates.
		}

		public String getMetadataValueEditTemplate()
		{
			return "meta_edit_date";
		}

		public String getMetadataValueDisplayTemplate()
		{
			return "meta_display_date";
		}
	}
}
