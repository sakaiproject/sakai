/**
 * Copyright (c) 2006-2018 The Apereo Foundation
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
package org.sakaiproject.sitestats.tool.wicket.components;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.sakaiproject.sitestats.api.StatsManager;
import static org.sakaiproject.sitestats.tool.wicket.pages.BasePage.DATEPICKERSCRIPT;
import static org.sakaiproject.sitestats.tool.wicket.pages.BasePage.JQUERYSCRIPT;
import static org.sakaiproject.sitestats.tool.wicket.pages.BasePage.JQUERYUISCRIPT;
import org.sakaiproject.util.DateFormatterUtil;

/**
 * A TextField equipped with a standard Sakai datepicker.
 *
 * @author plukasew
 */
public class SakaiDateTimeField extends TextField<ZonedDateTime>
{
	private IConverter<ZonedDateTime> dateConverter;
	private static final String DATEPICKER_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String DATEPICKER_FORMAT_DATE_ONLY = "yyyy-MM-dd";
	private static final String FIELD_JS = StatsManager.SITESTATS_WEBAPP + "/script/sakaidatetimefield.js";

	private boolean useTime = true;
	private final ZoneId zoneId;

	/**
	 * @param id wicket id
	 * @param model a ZonedDateTime. Can be null if no time is set.
	 * @param timeZoneId timezone for this time, cannot be null. Should match the model's timezone, if model is non-null.
	 */
	public SakaiDateTimeField(String id, IModel<ZonedDateTime> model, ZoneId timeZoneId)
	{
		super(id, model);
		Objects.requireNonNull(timeZoneId);
		zoneId = timeZoneId;
	}

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		setOutputMarkupId(true);
		dateConverter = new SakaiIsoDateConverter(getMarkupId());

		add(AttributeModifier.append("class", "sakai-datetimefield"));
	}

	@Override
	public <ZonedDateTime> IConverter<ZonedDateTime> getConverter(Class<ZonedDateTime> type)
	{
		return (IConverter<ZonedDateTime>) dateConverter;
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		response.render(JavaScriptHeaderItem.forUrl(JQUERYSCRIPT));
		response.render(JavaScriptHeaderItem.forUrl(JQUERYUISCRIPT));
		response.render(JavaScriptHeaderItem.forUrl(DATEPICKERSCRIPT));
		response.render(JavaScriptHeaderItem.forUrl(FIELD_JS));
		String pattern = useTime ? DATEPICKER_FORMAT : DATEPICKER_FORMAT_DATE_ONLY;
		String formattedDate = getModelObject().format(DateTimeFormatter.ofPattern(pattern, getSession().getLocale()));
		String loadFunction = useTime ? "loadJQueryDatePicker" : "loadJQueryDateOnlyPicker";
		String script = String.format("%s('%s','%s');", loadFunction, getMarkupId(), formattedDate);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	public void setUseTime(boolean value)
	{
		useTime = value;
	}

	private class SakaiIsoDateConverter implements IConverter<ZonedDateTime>
	{
		private final String componentMarkupId;

		public SakaiIsoDateConverter(String componentMarkupId)
		{
			this.componentMarkupId = componentMarkupId;
		}

		@Override
		public ZonedDateTime convertToObject(String string, Locale locale) throws ConversionException
		{
			// ignore actual input string and read in iso date from hidden input instead
			String isoParam = componentMarkupId + "ISO8601";
			String isoDateStr = getRequest().getRequestParameters().getParameterValue(isoParam).toString("");
			// format is like '2017-12-03T10:15:30-04:00'
			if (!DateFormatterUtil.isValidISODate(isoDateStr))
			{
				throw new ConversionException("Invalid ISO date: " + isoDateStr);
			}

			// The string we get back from the datepicker has an offset which may or may not match the desired timezone.
			// Since the user cannot choose a timezone using the datepicker, we just ignore the datepicker-provided offset
			// and set the timezone from the stored zone.
			LocalDateTime local = LocalDateTime.parse(isoDateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
			return ZonedDateTime.of(local, zoneId);
		}

		@Override
		public String convertToString(ZonedDateTime c, Locale locale)
		{
			// ignore model date, it will be set via javascript on page load
			return "";
		}

	}
}
