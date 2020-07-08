/**
 * Copyright (c) 2006-2020 The Apereo Foundation
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
package org.sakaiproject.wicket.component;

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
import org.sakaiproject.portal.util.PortalUtils;
import org.sakaiproject.util.DateFormatterUtil;

/**
 * A TextField equipped with a standard Sakai datepicker.
 * 
 * This component adds the jquery-ui and lang-datepicker scripts to the page by default,
 * so they do not need to be added separately. If you know JQuery UI is already loaded on the page,
 * call setLoadJQueryUI(false).
 *
 * JQuery is also required but is assumed to already be loaded on the page.
 *
 * @author plukasew
 */
public class SakaiDateTimeField extends TextField<ZonedDateTime>
{
	private static final String DATEPICKERSCRIPT = "/library/js/lang-datepicker/lang-datepicker.js%s";
	private static final String JQUERYUISCRIPT = "/library/webjars/jquery-ui/1.12.1/jquery-ui.min.js%s";

	private SakaiIsoDateConverter dateConverter;
	private static final String DATEPICKER_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String DATEPICKER_FORMAT_DATE_ONLY = "yyyy-MM-dd";

	private boolean useTime = true, allowEmptyDate = true, loadJQueryUI = true;
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
	public <C> IConverter<C> getConverter(Class<C> type)
	{
		return ZonedDateTime.class.isAssignableFrom(type) ? (IConverter<C>) dateConverter : super.getConverter(type);
	}

	@Override
	public void renderHead(IHeaderResponse response)
	{
		super.renderHead(response);

		final String version = PortalUtils.getCDNQuery();
		if (loadJQueryUI)
		{
			response.render(JavaScriptHeaderItem.forUrl(String.format(JQUERYUISCRIPT, version)));
		}
		response.render(JavaScriptHeaderItem.forUrl(String.format(DATEPICKERSCRIPT, version)));
		String pattern = useTime ? DATEPICKER_FORMAT : DATEPICKER_FORMAT_DATE_ONLY;
		String formattedDate = getModelObject() == null ? "" : getModelObject().format(DateTimeFormatter.ofPattern(pattern, getSession().getLocale()));
		String script = String.format("SDP.initSakaiDatePicker('%s','%s', %b, %b);", getMarkupId(), formattedDate, useTime, allowEmptyDate);
		response.render(OnDomReadyHeaderItem.forScript(script));
	}

	/**
	 * Sets whether to include a time component in the display of the text field and date picker. Defaults to true.
	 * @param value true if using time
	 * @return this, for method chaining
	 */
	public SakaiDateTimeField setUseTime(boolean value)
	{
		useTime = value;
		return this;
	}

	/**
	 * Sets whether to allow fields to have no date set. Defaults to true.
	 * @param value true if empty dates are allowed
	 * @return this, for method chaining
	 */
	public SakaiDateTimeField setAllowEmptyDate(boolean value)
	{
		allowEmptyDate = value;
		return this;
	}

	/**
	 * Sets whether to have the component load the JQueryUI dependency on the page. Defaults to true.
	 * @param value true if JQueryUI should be loaded by this component
	 * @return this, for method chaining
	 */
	public SakaiDateTimeField setLoadJQueryUI(boolean value)
	{
		loadJQueryUI = value;
		return this;
	}

	/**
	 * Custom converter to parse the hidden ISO date string from lang-datepicker.js and return a ZonedDateTime
	 */
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
				throw new ConversionException("Invalid ISO date: " + isoDateStr).setResourceKey("sakaidatetimefield.error.dateformat");
			}

			// The string we get back from the datepicker has an offset which may or may not match the desired timezone.
			// Since the user cannot change the timezone using the datepicker, we just ignore the datepicker-provided offset
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
