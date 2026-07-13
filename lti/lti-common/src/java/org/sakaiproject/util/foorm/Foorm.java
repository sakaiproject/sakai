/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2011 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util.foorm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.lti.api.LTIService;
import org.sakaiproject.util.ResourceLoader;
import lombok.extern.slf4j.Slf4j;

import org.tsugi.lti.LTIUtil;

@Slf4j
public class Foorm {
	
	private static final String LTI_SEARCH_TOKEN_SEPARATOR_REGEX = LTIService.LTI_SEARCH_TOKEN_SEPARATOR_AND+"|"+LTIService.LTI_SEARCH_TOKEN_SEPARATOR_OR.replace("|", "\\|");

	public static String[] positional = { "field", "type" };

	// Anything longer than this is treated as "LONG TEXT"
	// With multiple megabytes of text possible
	// Make this larger than 2^16 (65535)
	public static int MAX_TEXT = 70000;

	// Parse a form field description
	// field:type:key=value:key2=value2
	public static Properties parseFormString(String str) {
		Properties op = new Properties();
		String[] pairs = str.split(":");
		int i = 0;
		for (String s : pairs) {
			String[] kv = s.split("=");
			if (kv.length == 2) {
				op.setProperty(kv[0], kv[1]);
			} else if (kv.length == 1 && i < positional.length) {
				op.setProperty(positional[i++], kv[0]);
			} else {
				// TODO : Logger something here
			}
		}
		return op;
	}

	public Object getField(Object row, String column) {
		if (row instanceof Properties) {
			return ((Properties) row).getProperty(column);
		}
		if (row instanceof Map) {
			return ((Map) row).get(column);
		}
		return null;
	}

	/**
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public boolean isFieldSet(Object row, String column) {
		if (row instanceof Properties) {
			return ((Properties) row).containsKey(column);
		}
		if (row instanceof Map) {
			return ((Map) row).containsKey(column);
		}
		return false;
	}

	/**
	 * Return the actual data fields
	 * 
	 * @param fieldInfo
	 * @return
	 */
	public String[] getPersistedFields(String fieldInfo[]) {
		ArrayList<String> aa = new ArrayList<String>();
		for (String line : fieldInfo) {
			Properties info = parseFormString(line);
			String field = info.getProperty("field");

			String type = info.getProperty("type", null);
			if ("header".equals(type)) continue;
			String persist = info.getProperty("persist", "true");
			if (!Boolean.valueOf(persist)) continue;

			if (field == null) {
				throw new IllegalArgumentException(
						"All model elements must include field name and type");
			}
			aa.add(field);
		}

		String[] retval = new String[aa.size()];
		return (String[]) aa.toArray(retval);
	}

	/**
	 * 
	 * @param row
	 * @param key
	 * @param value
	 */
	public void setField(Object row, String key, Object value) {
		if (row instanceof Properties) {
			if (value == null) {
				((Properties) row).setProperty(key, "");
			} else {
				((Properties) row).setProperty(key, value.toString());
			}
		}
		if (row instanceof Map) {
			((Map) row).put(key, value);
		}
	}

	// Expect to be overridden
	public String htmlSpecialChars(String str) {
		return str;
	}

	// Expect to be overridden
	private String loadI18N(String key, ResourceLoader loader) {
		return loader == null ? null : loader.getString(key, null);
	}

	// Abstract this away for testing purposes
	private String getI18N(String str, ResourceLoader loader) {
		return getI18N(str, str, loader);
	}

	/**
	 * 
	 * @param str
	 * @param def
	 * @param loader
	 * @return
	 */
	private String getI18N(String str, String def, ResourceLoader loader) {
		if (loader == null) {
			return def;
		}
		if (str == null) {
			return def;
		}
		String retval = loadI18N(str, loader);
		return retval == null ? def : retval;
	}

	/**
	 * 
	 * @param row
	 * @param fieldinfo
	 * @return
	 */
	public String formInput(Object row, String fieldinfo) {
		return formInput(row, fieldinfo, null);
	}

	/**
	 * 
	 * @param sb
	 * @param field
	 * @param type
	 * @param label
	 * @param required
	 * @param loader
	 */
	private void formInputStart(StringBuffer sb, String field, String type, String label,
			boolean required, ResourceLoader loader) {
		// Checkbox and radio no longer call this
		sb.append("<div id=\"");
		sb.append(field);
		sb.append("-input\" class=\"foorm-"+type+"\" style=\"clear:both;\">");

		if (label == null ) return;

		sb.append("<label for=\"");
		sb.append(field);
		sb.append("\" style=\"display:block;float:none;\">");

		if ( required ) {
			sb.append("<span class=\"foorm-required\" style=\"color:#903;font-weight:bold;\" title=\"");
			sb.append(getI18N(label, loader));
			sb.append("\">*</span>");
		}
		sb.append(getI18N(label, loader));
		sb.append("</label>");
	}

	/**
	 * 
	 * @param sb
	 * @param field
	 * @param label
	 * @param required
	 * @param loader
	 */
	private void formInputEnd(StringBuffer sb, String field, String type, String label, boolean required,
			ResourceLoader loader) {
		sb.append("</div>\n");
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param size
	 * @param loader
	 * @return
	 */
	private String formInputText(String value, String field, String label, boolean required,
			String size, String readonly, ResourceLoader loader) {
		if (value == null)
			value = "";
		StringBuffer sb = new StringBuffer();
		formInputStart(sb, field, "text", label, required, loader);
		sb.append("<div id=\"div_");
		sb.append(field);
		sb.append("\"><input type=\"text\" class=\"form-control\" id=\"");
		sb.append(field);
		sb.append("\" name=\"");
		sb.append(field);
		sb.append("\" size=\"");
		sb.append(size);
		sb.append("\"");
		if (Boolean.valueOf(readonly)) sb.append(" readonly ");
		// sb.append(" style=\"border:1px solid #555;padding:5px;font-size:1em;width:300px\" value=\"");
		sb.append(" value=\"");
		sb.append(htmlSpecialChars(value));
		sb.append("\"/></div>");
		formInputEnd(sb, field, "text", label, required, loader);
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @return
	 */
	private String formInputKey(Object value, String field) {
		Long key = LTIUtil.toLongNull(value);
		if (key == null)
			return "";
		String val = key.toString();
		return formInputHidden(val, field);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @return
	 */
	private String formInputHidden(String value, String field) {
		if (value == null)
			return "";
		if ("".equals(value))
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("<input type=\"hidden\" id=\"");
		sb.append(field);
		sb.append("\" name=\"");
		sb.append(field);
		sb.append("\" value=\"");
		sb.append(htmlSpecialChars(value));
		sb.append("\"/>");
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param rows
	 * @param cols
	 * @param loader
	 * @return
	 */
	private String formInputTextArea(String value, String field, String label,
			boolean required, String rows, String cols, String readonly, ResourceLoader loader) {
		if (value == null)
			value = "";
		StringBuffer sb = new StringBuffer();
		formInputStart(sb, field, "textarea", label, required, loader);
		sb.append("<textarea  class=\"form-control\" id=\"");
		sb.append(field);
		sb.append("\" name=\"");
		sb.append(field);
		sb.append("\" rows=\"");
		sb.append(rows);
		sb.append("\" cols=\"");
		sb.append(cols);
        sb.append("\"");
        if (Boolean.valueOf(readonly)) sb.append(" readonly ");
		sb.append(">");
		sb.append(htmlSpecialChars(value));
		sb.append("</textarea>\n");
		formInputEnd(sb, field, "textarea", label, required, loader);
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param choices
	 * @param loader
	 * @return
	 */
	public String formInputRadio(Object value, String field, String label,
			boolean required, String[] choices, ResourceLoader loader) {
		StringBuffer sb = new StringBuffer();

		sb.append("<div id=\""+field+"-input\">");
		sb.append("<h4 id=\""+field+"-header\">");
		sb.append(getI18N(label, loader));
		sb.append("</h4>\n");
		int val = 0;
		if (value != null && value instanceof Number)
			val = ((Number) value).intValue();
		if (value != null && value instanceof String) {
			Integer ival = new Integer((String) value);
			val = ival.intValue();
		}
		if (val < 0)
			val = 0;
		if (choices == null || val >= choices.length)
			val = 0;
		int i = 0;
		sb.append("<ol id=\""+field+"-list\" style=\"list-style-type:none\">\n");
		for (String choice : choices) {
			String checked = "";
			if (i == val)
				checked = " checked=\"checked\"";
			String id = field + "_" + choice;
			sb.append("<li id=\""+id+"\" style=\"border:3px; padding:3px; margin:7px;\">\n");
			sb.append("<input type=\"radio\" name=\"");
			sb.append(field);
			sb.append("\" id=\"");
			sb.append(id);
			sb.append("-input\" value=\"" + i + "\" ");
			sb.append(checked);
			sb.append("/> <label for=\"");
			sb.append(id);
			sb.append("-input\">");
			sb.append(getI18N(label + "_" + choice, loader));
			sb.append("</label></li>\n");
			i++;
		}
		sb.append("</ol></div>\n");
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param loader
	 * @return
	 */
	public String formInputCheckbox(Object value, String field, String label,
			boolean required, ResourceLoader loader) {
		StringBuffer sb = new StringBuffer();
		// formInputStart(sb, field, "checkbox", label, required, loader);
		int val = LTIUtil.toInt(value);
		String checked = "";
		if (val == 1) checked = " checked=\"checked\"";
		sb.append("<li><input type=\"checkbox\" name=\"");
		sb.append(field);
		sb.append("\" value=\"1\" id=\"");
		sb.append(field);
		sb.append("\"");
		sb.append(checked);
		// onclick fires after "checked" is updated so it is the new state of checked
		// http://stackoverflow.com/questions/4471401/getting-value-of-html-checkbox-from-onclick-onchange-events
		if ( val == 1 ) {
			sb.append("onclick=\"if(this.checked) document.getElementById('");
			sb.append(field);
			sb.append(".mirror').name = '");
			sb.append(field);
			sb.append(".ignore'; else document.getElementById('");
			sb.append(field);
			sb.append(".mirror').name = '");
			sb.append(field);
			sb.append("';\"");
		}
		sb.append("/> ");
		if ( val == 1 ) {
			sb.append("<input type=\"hidden\" name=\"");
			sb.append(field);
			sb.append(".ignore\" id=\"");
			sb.append(field);
			sb.append(".mirror\" value=\"0\" />");
		}
		sb.append("<label for=\"");
		sb.append(field);
		sb.append("\">");
		sb.append(getI18N(label, loader));
		sb.append("</label>");
		// formInputEnd(sb, field, "checkbox", label, required, loader);
		sb.append("</li>\n");
		return sb.toString();
	}

	/**
	 * 
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formInputHeader(String field, String label, ResourceLoader loader) {
		StringBuffer sb = new StringBuffer();
		sb.append("<h4 id=\""+field+"\">");
		sb.append(getI18N(label, loader));
		sb.append("</h4>\n");
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param size
	 * @param loader
	 * @return
	 */
	private String formInputURL(String value, String field, String label, boolean required,
			String size, String readonly, ResourceLoader loader) {
		return formInputText(value, field, label, required, size, readonly, loader);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param size
	 * @param loader
	 * @return
	 */
	private String formInputId(String value, String field, String label, boolean required,
			String size, ResourceLoader loader) {
		return formInputText(value, field, label, required, size, "false", loader);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param required
	 * @param size
	 * @param loader
	 * @return
	 */
	private String formInputInteger(Object value, String field, String label,
			boolean required, String size, String readonly, ResourceLoader loader) {
		if (value == null)
			value = "";
		if (value instanceof Integer && ((Integer) value).intValue() == 0)
			value = "";
		if (value instanceof Long && ((Long) value).intValue() == 0)
			value = "";
		if (value instanceof String)
			return formInputText((String) value, field, label, required, size, readonly, loader);
		return formInputText(value.toString(), field, label, required, size, readonly, loader);
	}

	/**
	 * Produce a form for creating a new object or editing an existing object
	 */
	public String formInput(Object row, String fieldinfo, ResourceLoader loader) {
		Properties info = parseFormString(fieldinfo);
		String field = info.getProperty("field", null);
		String type = info.getProperty("type", null);
		
		if (field == null || type == null) {
			throw new IllegalArgumentException(
					"All model elements must include field name and type");
		}

		Object value = getField(row, field);
		String label = info.getProperty("label", field);
		
		// look for fields with a tool id prefix like 694_fa_prefix
		int pos = field.indexOf("_");
		if (pos != -1 && field.length() > pos+1)
		{
			String first = field.substring(0,pos);
			String second = field.substring(pos+1);
			try
			{
				// the first array item should be an long value
				Long.parseLong(first);
				// reset the input value
				value = getField(row, second);
				// reset the input label
				label = info.getProperty("label", second);
			}
			catch (NumberFormatException e)
			{
				// do nothing
			}
		}
		
		String hidden = info.getProperty("hidden", null);
		if ("true".equals(hidden))
			return "";

		boolean required = "true".equals(info.getProperty("required", "false"));
		String size = info.getProperty("size", "40");
		String readonly = info.getProperty("readonly", "false");
		String cols = info.getProperty("cols", "40");
		String rows = info.getProperty("rows", "5");

		if ("key".equals(type))
			return formInputKey(value, field);
		if ("integer".equals(type))
			return formInputInteger(value, field, label, required, size, readonly, loader);
		if ("text".equals(type))
			return formInputText((String) value, field, label, required, size, readonly, loader);
		if ("hidden".equals(type))
			return formInputHidden((String) value, field);
		if ("url".equals(type))
			return formInputURL((String) value, field, label, required, size, readonly, loader);
		if ("id".equals(type))
			return formInputId((String) value, field, label, required, size, loader);
		if ("textarea".equals(type))
			return formInputTextArea((String) value, field, label, required, rows, cols, readonly, loader);
		if ("autodate".equals(type))
			return "";
		if ("date".equals(type))
			return "";
		if ("checkbox".equals(type)) {
			return formInputCheckbox(value, field, label, required, loader);
		}
		if ("radio".equals(type)) {
			String choices = info.getProperty("choices", null);
			if (choices == null)
				return "\n<!-- Foorm.formInput() requires choices=on,off,part -->\n";
			String[] choiceList = choices.split(",");
			if (choiceList.length < 1)
				return "\n<!-- Foorm.formInput() requires choices=on,off,part -->\n";
			
			// set the default value of radio button
			if (value == null)
			{
				value= "0";
			}
			return formInputRadio(value, field, label, required, choiceList, loader);
		}
		if ("header".equals(type))
			return formInputHeader(field, label, loader);
		return "\n<!-- Foorm.formInput() unrecognized type " + type + " field=" + field
			+ " -->\n";
	}

	/**
	 * 
	 * @param fieldinfo
	 * @return
	 */
	public ArrayList<String> utilI18NStrings(String[] fieldinfo) {
		return checkI18NStrings(fieldinfo, null);
	}

	/**
	 * 
	 * @param fieldinfo
	 * @param loader
	 * @return
	 */
	public ArrayList<String> checkI18NStrings(String[] fieldinfo, ResourceLoader loader) {
		ArrayList<String> strings = new ArrayList<String>();
		for (String line : fieldinfo) {
			Properties info = parseFormString(line);
			String label = info.getProperty("label", info.getProperty("field"));
			String type = info.getProperty("type", null);
			String hidden = info.getProperty("hidden", null);
			if ("true".equals(hidden))
				continue;
			if ("autodate".equals(type))
				continue;
			if ("date".equals(type))
				continue;

			String choices = info.getProperty("choices", null);
			if (loadI18N(label, loader) == null)
				strings.add(label);
			if ("radio".equals(type) && choices != null) {
				String[] choiceList = choices.split(",");
				for (String choice : choiceList) {
					String newkey = label + "_" + choice;
					if (loadI18N(newkey, loader) == null)
						strings.add(newkey);
				}
			}
		}
		return strings;
	}

	/**
	 * 
	 * @param row
	 * @param formDefinition
	 * @return
	 */
	public String formInput(Object row, String[] formDefinition) {
		return formInput(row, formDefinition, null);
	}

	/**
	 * 
	 * @param row
	 * @param formDefinition
	 * @param loader
	 * @return
	 */
	public String formInput(Object row, String[] formDefinition, ResourceLoader loader) {
		StringBuffer sb = new StringBuffer();
		String header = null;
		String fieldList[] = null;
		boolean inCheckboxes = false;
		for (String inp : formDefinition) {
			String tmp = formInput(row, inp, loader);
			if (tmp.length() < 1)
				continue;
			Properties info = parseFormString(inp);
			String type = info.getProperty("type", null);
			String field = info.getProperty("field", null);

			if ( inCheckboxes && ! "checkbox".equals(type) ) {
				sb.append("</ol>\n");
				inCheckboxes = false;
			}

			if ( "header".equals(type) ) { 
				String fields = info.getProperty("fields", "");

				fieldList = fields.split(",");
				if (fieldList.length > 1) {
					header = tmp;
					continue;
				}
			}

			if ( header != null && Arrays.asList(fieldList).contains(field) ) {
				sb.append(header);
				sb.append("\n");
				header = null;
				fieldList = null;
			}

			if ( ! inCheckboxes && "checkbox".equals(type) ) {
				sb.append("<ol id=\""+field+"-checkbox-start\" style=\"list-style-type:none\">\n");
				inCheckboxes = true;
			}

			sb.append(tmp);
			sb.append("\n");
		}

		if ( inCheckboxes ) {
			sb.append("</ol>\n");
			inCheckboxes = false;
		}
		return sb.toString();
	}

	/**
	 * 
	 * @param row
	 * @param fieldinfo
	 * @return
	 */
	public String formOutput(Object row, String fieldinfo) {
		return formOutput(row, fieldinfo, null);
	}

	/**
	 * 
	 * @param sb
	 * @param field
	 * @param label
	 * @param loader
	 */
	public void formOutputStart(StringBuffer sb, String field, String label, ResourceLoader loader) {
		sb.append("<div class=\"foorm-text\" id=\""+field+"\">\n");
		if (label != null) {
			sb.append("<b>");
			sb.append(getI18N(label, loader));
			sb.append("</b><br/>");
			sb.append("<span id=\"foorm_output_"+field+"\">\n");
		}
	}

	/**
	 * 
	 * @param sb
	 * @param field
	 * @param label
	 * @param loader
	 */
	public void formOutputEnd(StringBuffer sb, String field, String label, ResourceLoader loader) {
		sb.append("</div>\n");
	}

	public String formOutputHeader(String field, String label, ResourceLoader loader) {
		StringBuffer sb = new StringBuffer();
		formOutputStart(sb, field, label, loader);
		formOutputEnd(sb, field, label, loader);
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formOutputText(String value, String field, String label, ResourceLoader loader) {
		if (value == null)
			value = "";
		StringBuffer sb = new StringBuffer();
		formOutputStart(sb, field, label, loader);
		sb.append(htmlSpecialChars(value));
		formOutputEnd(sb, field, label, loader);
		return sb.toString();
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formOutputTextArea(String value, String field, String label, ResourceLoader loader) {
		return formOutputText(value, field, label, loader);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param choices
	 * @param loader
	 * @return
	 */
	public String formOutputRadio(Long value, String field, String label, String[] choices,
			ResourceLoader loader) {
		int val = 0;
		if (value != null)
			val = value.intValue();
		if (val > choices.length - 1)
			val = 0;
		String str = getI18N(label + "_" + choices[val], loader);
		return formOutputText(str, field, label, loader);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formOutputCheckbox(Long value, String field, String label, 
			ResourceLoader loader) {
		int val = LTIUtil.toInt(value);
		String str = getI18N(label, loader);
		String off = getI18N("bl_off", "(Off)", loader);
		if ( val != 1 ) str = off + " " + str;
		return formOutputText(str, field, label, loader);
	}
	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formOutputURL(String value, String field, String label, ResourceLoader loader) {
		return formOutputText(value, field, label, loader);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formOutputId(String value, String field, String label, ResourceLoader loader) {
		return formOutputText(value, field, label, loader);
	}

	/**
	 * 
	 * @param value
	 * @param field
	 * @param label
	 * @param loader
	 * @return
	 */
	public String formOutputInteger(Long value, String field, String label, ResourceLoader loader) {
		String strval = "";
		if (value != null)
			strval = value.toString();
		return formOutputText(strval, field, label, loader);
	}

	/**
	 * 
	 * @param row
	 * @param fieldinfo
	 * @param loader
	 * @return
	 */
	public String formOutput(Object row, String fieldinfo, ResourceLoader loader) {
		Properties info = parseFormString(fieldinfo);
		String field = info.getProperty("field", null);
		String type = info.getProperty("type", null);
		Object value = getField(row, field);
		if (field == null || type == null) {
			throw new IllegalArgumentException(
					"All model elements must include field name and type");
		}

		String hidden = info.getProperty("hidden", null);
		if ("true".equals(hidden))
			return "";

		String label = info.getProperty("label", field);

		if ("key".equals(type))
			return ""; // Key will be handled by the caller
		if ("autodate".equals(type))
			return "";
		if ("date".equals(type))
			return "";
		if ("integer".equals(type))
			return formOutputInteger(LTIUtil.toLongNull(value), field, label, loader);
		if ("text".equals(type))
			return formOutputText((String) value, field, label, loader);
		if ("url".equals(type))
			return formOutputURL((String) value, field, label, loader);
		if ("id".equals(type))
			return formOutputId((String) value, field, label, loader);
		if ("textarea".equals(type))
			return formOutputTextArea((String) value, field, label, loader);
		if ("checkbox".equals(type)) {
			return formOutputCheckbox(LTIUtil.toLongNull(value), field, label, loader);
		}
		if ("header".equals(type)) {
			return formOutputHeader(field, label, loader);
		}
		if ("radio".equals(type)) {
			String choices = info.getProperty("choices", null);
			if (choices == null)
				return "\n<!-- Foorm.formOutput() requires choices=on,off,part -->\n";
			String[] choiceList = choices.split(",");
			if (choiceList.length < 1)
				return "\n<!-- Foorm.formOutput() requires choices=on,off,part -->\n";
			return formOutputRadio(LTIUtil.toLongNull(value), field, label, choiceList, loader);
		}
		return "\n<!-- Foorm.formOutput() unrecognized type " + type + " field=" + field
			+ " -->\n";
	}

	/**
	 * 
	 * @param row
	 * @param formDefinition
	 * @param loader
	 * @return
	 */
	public String formOutput(Object row, String[] formDefinition, ResourceLoader loader) {
		StringBuffer sb = new StringBuffer();
		for (String formOutput : formDefinition) {
			String tmp = formOutput(row, formOutput, loader);
			if (tmp.length() < 1)
				continue;
			sb.append(tmp);
			sb.append("\n");
		}
		return sb.toString();
	}

	// dataMap should be empty
	/**
	 * dataMap should be empty
	 * errors should be empty
	 */
	public String formExtract(Object parms, String[] formDefinition, ResourceLoader loader,
			boolean forInsert, Map<String, Object> dataMap, SortedMap<String,String> errors) {
		StringBuffer sb = new StringBuffer();
		String error = null;

		for (String formInput : formDefinition) {
			Properties info = parseFormString(formInput);
			String field = info.getProperty("field", null);
			String type = info.getProperty("type", null);
			if (field == null || type == null) {
				throw new IllegalArgumentException(
						"All model elements must include field name and type");
			}
			if ( "header".equals(type) ) continue;

			String persist = info.getProperty("persist", "true");
			if (!Boolean.valueOf(persist)) continue;

			String label = info.getProperty("label", field);
			log.debug("field={} type={}", field, type);

			// Check the automatically populate empty date fields
			if ("autodate".equals(type) && dataMap != null && (!isFieldSet(parms, field)) ) {
				Timestamp sqlTimestamp = new Timestamp(
						new Date().getTime());
				if ("updated_at".equals(field) || (forInsert && "created_at".equals(field))) {
					dataMap.put(field, sqlTimestamp);
				}
			}

			// For update, we don't worry about fields that are not set
			if ((!forInsert) && (!isFieldSet(parms, field)))
				continue;

			Object dataField = getField(parms, field);
			String sdf = null;
			if (dataField instanceof String) sdf = (String) dataField;
			sdf = StringUtils.trim(sdf);
			if (sdf != null && sdf.length() < 1) {
				sdf = null;
				dataField = null;
			}

			if ("true".equals(info.getProperty("required")) && (dataField == null)) {
				if (sb.length() > 0) sb.append(", ");
				error = getI18N("foorm.missing.field", "Required Field:", loader) + " " + getI18N(label, loader);
				sb.append(error);
				if ( errors != null ) errors.put(label, error);
			}

			String maxs = adjustMax(info.getProperty("maxlength", null));
			if (maxs != null && dataField instanceof String) {
				int maxlength = (new Integer(maxs)).intValue();
				String truncate = info.getProperty("truncate", "true");
				if ( maxlength >= MAX_TEXT ) {
					// We are OK
				} else if (sdf.length() > maxlength) {
					if ("true".equals(truncate)) {
						sdf = sdf.substring(0, maxlength);
						dataField = sdf;
					} else {
						if (sb.length() > 0)
							sb.append(", ");
						error = getI18N("foorm.maxlength.field", "Field >", loader) + " " + maxlength
								+ " " + getI18N(label, loader);
						sb.append(error);
						if ( errors != null ) errors.put(label, error);
					}
				}
			}

			if ("integer".equals(type) || "radio".equals(type) || "checkbox".equals(type) ) {
				if (dataField == null) {
					if (dataMap != null)
						dataMap.put(field, null);
				} else if (dataField instanceof Number) {
						if (dataMap != null)
						    dataMap.put(field, ((Number) dataField).intValue());
				} else {
					try {
						Integer ival = new Integer(sdf);
						if (dataMap != null)
							dataMap.put(field, ival);
					} catch (Exception e) {
						if (sb.length() > 0)
							sb.append(", ");
						error = getI18N("foorm.integer.field", "Field should be an integer:", loader) + " " + getI18N(label, loader);
						sb.append(error);
						if ( errors != null ) errors.put(label, error);
					}
				}
			}

			if ("id".equals(type)) {
				if (sdf == null) {
					if (dataMap != null)
						dataMap.put(field, null);
				} else if (sdf.matches("^[0-9a-zA-Z._-]*$")) {
					if (dataMap != null)
						dataMap.put(field, sdf);
				} else {
					if (sb.length() > 0)
						sb.append(", ");
					error = getI18N("foorm.id.field", "Field has invalid characters:", loader) + " " + getI18N(label, loader);
					sb.append(error);
					if ( errors != null ) errors.put(label, error);
				}
			}

			if ("url".equals(type)) {
				if (sdf == null) {
					if (dataMap != null)
						dataMap.put(field, null);
				} else if (sdf.matches("^(http://|https://)[a-zA-Z0-9][a-zA-Z0-9]*.*")) {
					if (dataMap != null)
						dataMap.put(field, sdf);
				} else {
					if (sb.length() > 0)
						sb.append(", ");
					error = getI18N("foorm.url.field", "Field is not a url:", loader) + " " + getI18N(label, loader);
					sb.append(error);
					if ( errors != null ) errors.put(label, error);
				}
			}

			if ("text".equals(type) || "textarea".equals(type)) {
				if (sdf == null) {
					if (dataMap != null)
						dataMap.put(field, null);
				} else if ("true".equals(info.getProperty("alphanumeric")) && !sdf.matches("^[a-zA-Z0-9]+$")) {
					if (sb.length() > 0)
						sb.append(", ");
					error = getI18N("foorm.alphanumeric.field", "Field must contain only letters and numbers:", loader) + " "
							+ getI18N(label, loader);
					sb.append(error);
					if (errors != null)
						errors.put(label, error);
				} else {
					if (dataMap != null)
						dataMap.put(field, sdf);
				}
			}

			if ("date".equals(type) ) {
				if (sdf == null) {
					if (dataMap != null)
						dataMap.put(field, null);
				} else {
					if (dataMap != null)
						dataMap.put(field, getInstantUTC(sdf));
				}
			}
		}
		if (sb.length() < 1)
			return null;
		return sb.toString();
	}

	/**
	 * Split given search clause into valid tokens
	 * 
	 * We assume a valid search clause like :
	 * 
	 * SEARCH_FIELD_1:SEARCH_VALUE_1[#&#|#\\|#]SEARCH_FIELD_2:SEARCH_VALUE_2[#&#|#\\|#]...[#&#|#\\|#]SEARCH_FIELD_N:SEARCH_VALUE_N
	 * 
	 * @param search
	 * @return list with search tokens
	 */
	public List<String> getSearchTokens(String search) {
		try {
			return Arrays.asList(search.split(LTI_SEARCH_TOKEN_SEPARATOR_REGEX));
		}
		catch (Exception ex) {
			return new ArrayList<String>();
		}
	}
	
	/**
	 * Get separators between tokens in a search clause
	 * 
	 * We assume a valid search clause like :
	 * 
	 * SEARCH_FIELD_1:SEARCH_VALUE_1[#&#|#\\|#]SEARCH_FIELD_2:SEARCH_VALUE_2[#&#|#\\|#]...[#&#|#\\|#]SEARCH_FIELD_N:SEARCH_VALUE_N
	 * 
	 * @param search
	 * @return list with search separators
	 */
	public List<String> getSearchSeparators(String search) {
		try {
			List<String> ret = new ArrayList<String>();
			Pattern pattern = Pattern.compile(LTI_SEARCH_TOKEN_SEPARATOR_REGEX);
	        Matcher m = pattern.matcher(search);
	        while (m.find()) {
	        	ret.add(m.group());
	        }
	        return ret;
		}
		catch (Exception ex) {
			return new ArrayList<String>();
		}
	}

	/**
	 * Get search field from a search token
	 * 
	 * We assume a valid search token like :
	 * 
	 * SEARCH_FIELD:SEARCH_VALUE
	 * 
	 * @param search
	 * @return search field
	 */
	public String getSearchField(String search) {
		if (search != null) {
			int endIndex = search.indexOf(":");
			if (endIndex > 0) {
				return search.substring(0, endIndex);
			}
		}
		return "";
	}

	/**
	 * Get search value from a search token
	 * 
	 * We assume a valid search token like :
	 * 
	 * SEARCH_FIELD:SEARCH_VALUE
	 * 
	 * @param search
	 * @return search value
	 */
	public String getSearchValue(String search) {
		if (search != null && search.indexOf(":") >= 0) {
			return search.substring(search.indexOf(":") + 1);
		}
		return "";
	}

	/**
	 * Determine if a search string is a raw search and not a search clause+value
	 */
	public boolean isSearchRaw(String search) {
		return search.matches("[( ]*(\\w+\\.)?\\w+\\s*=.+");
	}

	// Filter a form definition based on a controlling row and/or a regex
	//
	// The controlling row has fields that are interpreted as
	// 0=force off, 1=force on, 2 = delegate setting
	// For radio buttons in our form, it simply checks for
	// the field of the same name in the controlling row.
	// For non-radio fields, it looks for a field in the
	// controlling row prepended by 'allow'.
	/**
	 * 
	 */
	public String[] filterForm(Object controlRow, String[] fieldinfo) {
		return filterForm(controlRow, fieldinfo, null, null);
	}

	/**
	 * 
	 * @param fieldinfo
	 * @param includePattern
	 * @param excludePattern
	 * @return
	 */
	public String[] filterForm(String[] fieldinfo, String includePattern,
			String excludePattern) {
		return filterForm(null, fieldinfo, includePattern, excludePattern);
	}

	/**
	 * 
	 * @param controlRow
	 * @param fieldinfo
	 * @param includePattern
	 * @param excludePattern
	 * @return
	 */
	public String[] filterForm(Object controlRow, String[] fieldinfo,
			String includePattern, String excludePattern) {
		if (fieldinfo == null)
			return null;
		ArrayList<String> ret = new ArrayList<String>();
		for (String line : fieldinfo) {
			if (includePattern != null && (!line.matches(includePattern)))
				continue;
			if (excludePattern != null && (line.matches(excludePattern)))
				continue;
			Properties fields = parseFormString(line);
			String field = fields.getProperty("field", null);
			String type = fields.getProperty("type", null);
			String allowed = fields.getProperty("allowed", null);
			if (field == null || type == null) {
				throw new IllegalArgumentException(
						"All model elements must include field name and type");
			}
			// always allow autodate fields
			if ("autodate".equals(type) || "date".equals(type))
			{
				ret.add(line);
			}
			// always allow the SITE_ID field
			else if ("SITE_ID".equals(field))
			{
				ret.add(line);
			}
			// We always assume radio and checkbox may be allowed
			else if ("radio".equals(type) || "checkbox".equals(type) ) {
				// Field = Always Off (0), Always On (1), or Delegate(2)
				int value = LTIUtil.toInt(getField(controlRow, field));
				if ( value == 2 || ! isFieldSet(controlRow, field) ) ret.add(line);
			// When there is an allow field in the control row, check it
			} else if ( isFieldSet(controlRow, "allow" + field) && ! "false".equals(allowed) ) {
				Object allowRow = getField(controlRow, "allow" + field);
				int value = LTIUtil.toInt(allowRow);
				if ( value == 1 ) ret.add(line);
			} else {
				ret.add(line);
			}

		}
		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * Determines if the tool instance has configurable settings.
	 * For instance if the admin tool disallows every type of instructor customization, this method would return false for instructors
	 */
	public boolean formHasConfiguration(Object controlRow, String[] fieldinfo, String includePattern, String excludePattern) {
		if (fieldinfo == null) {
			return false;
		}

		for (String line : fieldinfo) {
			if ((includePattern != null && (!line.matches(includePattern))) || (excludePattern != null && (line.matches(excludePattern)))) {
				continue;
			}

			Properties fields = parseFormString(line);
			String field = fields.getProperty("field", null);
			String type = fields.getProperty("type", null);
			String allowed = fields.getProperty("allowed", null);

			if (field == null || type == null) {
				throw new IllegalArgumentException("All model elements must include field name and type");
			}

			if ("radio".equals(type) || "checkbox".equals(type)) {
				int value = LTIUtil.toInt(getField(controlRow, field));
				if (value == 2 || !isFieldSet(controlRow, field)) {
					// radio / checkbox is configuration
					return true;
				}
			} else if (isFieldSet(controlRow, "allow" + field) && !"false".equals(allowed)) {
				Object allowRow = getField(controlRow, "allow" + field);
				int value = LTIUtil.toInt(allowRow);

				// "Allow external tool to store setting data" enters this block, but it's not configuration; so exclude LTI_SETTINGS
				if (value == 1 && !LTIService.LTI_SETTINGS.equals(field)) {
					return true;
				}
			}
		}

		return false;
	}

	// Paging helpers

	/**
	 * Deal with the vagaries of date object types returned from this library - all UTC
	 */
	// https://www.baeldung.com/java-date-to-localdate-and-localdatetime
	public static Instant getInstantUTC(Object input)
	{
		if ( input == null ) return null;

		String dateString = null;
		if ( input instanceof LocalDateTime ) {
			return ((LocalDateTime) input).toInstant(ZoneOffset.UTC);
		} else if ( input instanceof Timestamp ) {
			return ((Timestamp) input).toInstant();
		} else if ( input instanceof Date ) {
			Date dateToConvert = (Date) input;
			return dateToConvert.toInstant();
		} else if ( input instanceof String ) {
			dateString = (String) input;
			if ( dateString.trim().length() < 1 ) return null;
		} else {
			dateString = input.toString();
		}

		// https://stackoverflow.com/questions/4024544/how-to-parse-dates-in-multiple-formats-using-simpledateformat
		String pattern = "[yyyy-MM-dd[['T'][ ]HH:mm:ss[.SSSSSSSz][.SSS[XXX][X]]]]";
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC);
			TemporalAccessor accessor = formatter.parse(dateString);
			return Instant.from(accessor);
		} catch(Exception e) {
			return null;
		}

	}

	/**
	 * Deal with suffixes like "M" and "K"
	 */
	public String adjustMax(String maxs)
	{
		if ( maxs == null ) return null;
		maxs = maxs.toLowerCase();
		if ( maxs.endsWith("m")) maxs = maxs.replace("m","000000");
		if ( maxs.endsWith("k")) maxs = maxs.replace("k","000");
		return maxs;
	}

	/**
	 * Take a Foorm Map and de-serialize it into an XML Element
	 */
	public static void mergeThing(Element element, String[] model, Map<String, Object> thing) {
		 for (String formInput : model) {
			Properties info = parseFormString(formInput);
			String field = info.getProperty("field", null);
			String type = info.getProperty("type", null);
			String archive = info.getProperty("archive", null);
			if ( ! "true".equals(archive) ) continue;

			NodeList nl = element.getElementsByTagName(field);
			if ( nl.getLength() < 1 ) continue;
			String value = nl.item(0).getTextContent();
			if ( StringUtils.isEmpty(value) ) continue;
			if ("checkbox".equals(type) || "integer".equals(type) || "radio".equals(type) || "key".equals(type) ) {
				Long longVal = LTIUtil.toLong(value, -1L);
				if ( longVal < 0 ) continue;
				thing.put(field, longVal);
			} else {
				thing.put(field, value);
			}
		}
	}

	/**
	 * Take a Foorm Map and serialize it into an XML Element
	 */
	public static Element archiveThing(Document doc, String tag, String[] model, Map<String, Object> thing) {
		Element retval = doc.createElement(tag);
		for (String formInput : model) {
			Properties info = Foorm.parseFormString(formInput);
			String field = info.getProperty("field", null);
			String type = info.getProperty("type", null);
			String archive = info.getProperty("archive", null);
			if ( ! "true".equals(archive) ) continue;

			Object o = thing.get(field);
			if ( o == null ) continue;

			Element newElement = doc.createElement(field);
			newElement.setTextContent(o.toString());
			retval.appendChild(newElement);
		}
		return retval;
	}

	/**
	 * Convert a Map<String, Object> to Properties for simple types
	 */
	public static Properties convertToProperties(Map<String, Object> map) {
		Properties properties = new Properties();

		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();

			// Check if the value is a simple type
			if (value instanceof String || value instanceof Number || value instanceof Boolean) {
				properties.put(key, value.toString());
			} else {
				continue; // Ignore non-simple types
			}
		}

		return properties;
	}
}
