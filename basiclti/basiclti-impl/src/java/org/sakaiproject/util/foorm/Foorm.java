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
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.util.foorm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

// rm Foorm.class ; javac Foorm.java ; java Foorm

/**
 * 
 */
public class Foorm {

  /**
   * 
   */
  public static String[] positional = { "field", "type" };

  // Parse a form field description
  // field:type:key=value:key2=value2
  /**
   * 
   */
  public Properties parseFormString(String str) {
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
        // TODO : Log something here
      }
    }
    return op;
  }

  // Returns -1 on failure
  /**
   * 
   */
  public Long getLongKey(Object key) {
    return getLong(key);
  }

  /**
   * 
   * @param key
   * @return
   */
  public Long getLong(Object key) {
    Long retval = getLongNull(key);
    if (retval != null)
      return retval;
    return new Long(-1);
  }

  /**
   * 
   * @param key
   * @return
   */
  public Long getLongNull(Object key) {
    if (key == null)
      return null;
    if (key instanceof Number)
      return new Long(((Number) key).longValue());
    if (key instanceof String) {
      try {
        return new Long((String) key);
      } catch (Exception e) {
        return null;
      }
    }
    return null;
  }

  /**
   * 
   * @param o
   * @return
   */
  public static int getInt(Object o) {
    if (o instanceof String) {
      try {
        return (new Integer((String) o)).intValue();
      } catch (Exception e) {
        return -1;
      }
    }
    if (o instanceof Number)
      return ((Number) o).intValue();
    return -1;
  }

  // Abstract this away for testing purposes
  /**
   * 
   */
  public Object getField(Object row, String column) {
    if (row instanceof java.util.Properties) {
      return ((java.util.Properties) row).getProperty(column);
    }
    if (row instanceof java.util.Map) {
      return ((java.util.Map) row).get(column);
    }
    if (row instanceof java.sql.ResultSet) {
      try {
        return ((java.sql.ResultSet) row).getObject(column);
      } catch (Exception e) {
        return null;
      }
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
    if (row instanceof java.util.Properties) {
      return ((java.util.Properties) row).containsKey(column);
    }
    if (row instanceof java.util.Map) {
      return ((java.util.Map) row).containsKey(column);
    }
    if (row instanceof java.sql.ResultSet) {
      try {
        Object x = ((java.sql.ResultSet) row).getObject(column);
        return true;
      } catch (Exception e) {
        return false;
      }
    }
    return false;
  }

  /**
   * 
   * @param fieldInfo
   * @return
   */
  public String[] getFields(String fieldInfo[]) {
    ArrayList<String> aa = new ArrayList<String>();
    for (String line : fieldInfo) {
      Properties info = parseFormString(line);
      String field = info.getProperty("field");
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
    if (row instanceof java.util.Properties) {
      if (value == null) {
        ((java.util.Properties) row).setProperty(key, "");
      } else {
        ((java.util.Properties) row).setProperty(key, value.toString());
      }
    }
    if (row instanceof java.util.Map) {
      ((java.util.Map) row).put(key, value);
    }
    if (row instanceof java.sql.ResultSet) {
      // TODO: Log message
    }
  }

  // Expect to be overridden
  /**
   * 
   */
  public String htmlSpecialChars(String str) {
    return str;
  }

  // Expect to be overridden
  /**
   * 
   */
  public String loadI18N(String str, Object loader) {
    if (loader == null)
      return null;
    if (loader instanceof Properties) {
      return ((Properties) loader).getProperty(str, null);
    }
    return null;
  }

  // Abstract this away for testing purposes
  /**
   * 
   */
  public String getI18N(String str, Object loader) {
    return getI18N(str, str, loader);
  }

  /**
   * 
   * @param str
   * @param def
   * @param loader
   * @return
   */
  public String getI18N(String str, String def, Object loader) {
    if (loader == null)
      return def;
    if (str == null)
      return def;
    String retval = loadI18N(str, loader);
    if (retval != null)
      return retval;
    return def;
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
  public void formInputStart(StringBuffer sb, String field, String type, String label,
      boolean required, Object loader) {
    sb.append("<p id=\"");
    sb.append(field);
    sb.append(".input\" class=\"foorm-"+type+"\" style=\"clear:all;\">");

    if (label != null && ( ! "checkbox".equals(type) ) ) {
      sb.append("<label for=\"");
      sb.append(field);
      sb.append("\" style=\"display:block;float:none;\">");
    }
    if (label != null && required ) {
      sb.append("<span class=\"foorm-required\" style=\"color:#903;font-weight:bold;\" title=\"");
      sb.append(getI18N(label, loader));
      sb.append("\">*</span>");
    }
    if (label != null && ( ! "checkbox".equals(type) ) ) {
      sb.append(getI18N(label, loader));
      sb.append("</label>");
    }
  }

  /**
   * 
   * @param sb
   * @param field
   * @param label
   * @param required
   * @param loader
   */
  public void formInputEnd(StringBuffer sb, String field, String label, boolean required,
      Object loader) {
    sb.append("</p>\n");
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
  public String formInputText(String value, String field, String label, boolean required,
      String size, Object loader) {
    if (value == null)
      value = "";
    StringBuffer sb = new StringBuffer();
    formInputStart(sb, field, "text", label, required, loader);
    sb.append("<input type=\"text\" id=\"");
    sb.append(field);
    sb.append("\" name=\"");
    sb.append(field);
    sb.append("\" size=\"");
    sb.append(size);
    sb.append("\" style=\"border:1px solid #555;padding:5px;font-size:1em;width:300px\" value=\"");
    sb.append(htmlSpecialChars(value));
    sb.append("\"/>");
    formInputEnd(sb, field, label, required, loader);
    return sb.toString();
  }

  /**
   * 
   * @param value
   * @param field
   * @return
   */
  public String formInputKey(Object value, String field) {
    Long key = getLongNull(value);
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
  public String formInputHidden(String value, String field) {
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
  public String formInputTextArea(String value, String field, String label,
      boolean required, String rows, String cols, Object loader) {
    if (value == null)
      value = "";
    StringBuffer sb = new StringBuffer();
    sb.append("<p id=\"");
    sb.append(field);
    sb.append(".input\" class=\"longtext\" style=\"clear:all;\">");
    formInputStart(sb, field, "textarea", label, required, loader);
    sb.append("<textarea style=\"border:1px solid #555;width:300px\" id=\"");
    sb.append(field);
    sb.append("\" name=\"");
    sb.append(field);
    sb.append("\" rows=\"");
    sb.append(rows);
    sb.append("\" cols=\"");
    sb.append(cols);
    sb.append("\"/>");
    sb.append(htmlSpecialChars(value));
    sb.append("</textarea>\n");
    formInputEnd(sb, field, label, required, loader);
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
      boolean required, String[] choices, Object loader) {
    StringBuffer sb = new StringBuffer();
    // formInputStart(sb, field, "radio", label, required, loader);
    sb.append("<h4>");
    sb.append(getI18N(label, loader));
    sb.append("</h4>\n");
    int val = 0;
    if (value != null && value instanceof Integer)
      val = ((Integer) value).intValue();
    if (value != null && value instanceof String) {
      Integer ival = new Integer((String) value);
      val = ival.intValue();
    }
    if (val < 0)
      val = 0;
    if (choices == null || val >= choices.length)
      val = 0;
    int i = 0;
    for (String choice : choices) {
      String checked = "";
      if (i == val)
        checked = " checked=\"checked\"";
      sb.append("<p  style=\"border:padding:3px;;margin:7px 3px;\">\n");
      sb.append("<input type=\"radio\" name=\"");
      sb.append(field);
      sb.append("\" value=\"" + i + "\" id=\"");
      String id = field + "_" + choice;
      sb.append(id + "\"");
      sb.append(checked);
      sb.append("/><label for=\"");
      sb.append(id);
      sb.append("\">");
      sb.append(getI18N(label + "_" + choice, loader));
      sb.append("</label></p>\n");
      i++;
    }
    formInputEnd(sb, field, label, required, loader);
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
      boolean required, Object loader) {
    StringBuffer sb = new StringBuffer();
    formInputStart(sb, field, "checkbox", label, required, loader);
    int val = getInt(value);
    String checked = "";
    if (val == 1) checked = " checked=\"checked\"";
    sb.append("<input type=\"checkbox\" name=\"");
    sb.append(field);
    sb.append("\" value=\"1\" id=\"");
    sb.append(field + "\"");
    sb.append(checked);
    sb.append("/>");
    sb.append(getI18N(label, loader));
    sb.append("<br/>\n");
    formInputEnd(sb, field, label, required, loader);
    return sb.toString();
  }

  /**
   * 
   * @param field
   * @param label
   * @param loader
   * @return
   */
  public String formInputHeader(String field, String label, Object loader) {
    StringBuffer sb = new StringBuffer();
    sb.append("<h4>");
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
  public String formInputURL(String value, String field, String label, boolean required,
      String size, Object loader) {
    return formInputText(value, field, label, required, size, loader);
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
  public String formInputId(String value, String field, String label, boolean required,
      String size, Object loader) {
    return formInputText(value, field, label, required, size, loader);
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
  public String formInputInteger(Object value, String field, String label,
      boolean required, String size, Object loader) {
    if (value == null)
      value = "";
    if (value instanceof Integer && ((Integer) value).intValue() == 0)
      value = "";
    if (value instanceof Long && ((Long) value).intValue() == 0)
      value = "";
    if (value instanceof String)
      return formInputText((String) value, field, label, required, size, loader);
    return formInputText(value.toString(), field, label, required, size, loader);
  }

  // Produce a form for createing a new object or editing an existing object
  /**
   * 
   */
  public String formInput(Object row, String fieldinfo, Object loader) {
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
    boolean required = "true".equals(info.getProperty("required", "false"));
    String size = info.getProperty("size", "40");
    String cols = info.getProperty("cols", "40");
    String rows = info.getProperty("rows", "5");

    if ("key".equals(type))
      return formInputKey(value, field);
    if ("integer".equals(type))
      return formInputInteger(value, field, label, required, size, loader);
    if ("text".equals(type))
      return formInputText((String) value, field, label, required, size, loader);
    if ("hidden".equals(type))
      return formInputHidden((String) value, field);
    if ("url".equals(type))
      return formInputURL((String) value, field, label, required, size, loader);
    if ("id".equals(type))
      return formInputId((String) value, field, label, required, size, loader);
    if ("textarea".equals(type))
      return formInputTextArea((String) value, field, label, required, rows, cols, loader);
    if ("autodate".equals(type))
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
  public ArrayList<String> checkI18NStrings(String[] fieldinfo, Object loader) {
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
  public String formInput(Object row, String[] formDefinition, Object loader) {
    StringBuffer sb = new StringBuffer();
    String header = null;
    String fieldList[] = null;
    for (String formInput : formDefinition) {
      String tmp = formInput(row, formInput, loader);
      if (tmp.length() < 1)
        continue;
      Properties info = parseFormString(formInput);
      String type = info.getProperty("type", null);
      String field = info.getProperty("field", null);

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

      sb.append(tmp);
      sb.append("\n");
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
  public void formOutputStart(StringBuffer sb, String field, String label, Object loader) {
    sb.append("<p class=\"row\">\n");
    if (label != null) {
      sb.append("<b>");
      sb.append(getI18N(label, loader));
      sb.append("</b><br/>");
    }
  }

  /**
   * 
   * @param sb
   * @param field
   * @param label
   * @param loader
   */
  public void formOutputEnd(StringBuffer sb, String field, String label, Object loader) {
    sb.append("</p>\n");
  }

  /**
   * 
   * @param value
   * @param field
   * @param label
   * @param loader
   * @return
   */
  public String formOutputText(String value, String field, String label, Object loader) {
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
  public String formOutputTextArea(String value, String field, String label, Object loader) {
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
      Object loader) {
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
      Object loader) {
    int val = getInt(value);
    String str = getI18N(label, loader);
    if ( val != 1 ) str = "(Off) " + str;
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
  public String formOutputURL(String value, String field, String label, Object loader) {
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
  public String formOutputId(String value, String field, String label, Object loader) {
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
  public String formOutputInteger(Long value, String field, String label, Object loader) {
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
  public String formOutput(Object row, String fieldinfo, Object loader) {
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
    if ("integer".equals(type))
      return formOutputInteger(getLongNull(value), field, label, loader);
    if ("text".equals(type))
      return formOutputText((String) value, field, label, loader);
    if ("url".equals(type))
      return formOutputURL((String) value, field, label, loader);
    if ("id".equals(type))
      return formOutputId((String) value, field, label, loader);
    if ("textarea".equals(type))
      return formOutputTextArea((String) value, field, label, loader);
    if ("checkbox".equals(type)) {
      return formOutputCheckbox(getLongNull(value), field, label, loader);
    }
    if ("radio".equals(type)) {
      String choices = info.getProperty("choices", null);
      if (choices == null)
        return "\n<!-- Foorm.formOutput() requires choices=on,off,part -->\n";
      String[] choiceList = choices.split(",");
      if (choiceList.length < 1)
        return "\n<!-- Foorm.formOutput() requires choices=on,off,part -->\n";
      return formOutputRadio(getLongNull(value), field, label, choiceList, loader);
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
  public String formOutput(Object row, String[] formDefinition, Object loader) {
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

  /**
   * 
   * @param parms
   * @param formDefinition
   * @param forInsert
   * @param loader
   * @return
   */
  public String formValidate(Properties parms, String[] formDefinition,
      boolean forInsert, Object loader) {
    return formExtract(parms, formDefinition, loader, forInsert, null);
  }

  // dataMap should be empty
  /**
   * 
   */
  public String formExtract(Object parms, String[] formDefinition, Object loader,
      boolean forInsert, Map<String, Object> dataMap) {
    StringBuffer sb = new StringBuffer();

    for (String formInput : formDefinition) {
      Properties info = parseFormString(formInput);
      String field = info.getProperty("field", null);
      String type = info.getProperty("type", null);
      if (field == null || type == null) {
        throw new IllegalArgumentException(
            "All model elements must include field name and type");
      }
      if ( "header".equals(type) ) continue;
      String label = info.getProperty("label", field);

      // For update, we don't worry about fields that are not set
      if ((!forInsert) && (!isFieldSet(parms, field)))
        continue;
      Object dataField = getField(parms, field);
      String sdf = null;
      if (dataField instanceof String)
        sdf = (String) dataField;
      if (sdf != null && sdf.length() < 1) {
        sdf = null;
        dataField = null;
      }

      if ("autodate".equals(type)
          && ("created_at".equals(field) || "updated_at".equals(field))) {
        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(
            new java.util.Date().getTime());
        if (dataMap != null)
          dataMap.put(field, sqlTimestamp);
      }
      // System.out.println("field="+field+" data="+dataField);

      if ("true".equals(info.getProperty("required")) && (dataField == null)) {
        if (sb.length() > 0)
          sb.append(", ");
        sb.append(getI18N("foorm.missing.field", "Required Field: ", loader));
        sb.append(getI18N(label, loader));
      }

      String maxs = info.getProperty("maxlength", null);
      if (maxs != null && dataField instanceof String) {
        int maxlength = (new Integer(maxs)).intValue();
        String truncate = info.getProperty("truncate", "true");
        if (sdf.length() > maxlength) {
          if ("true".equals(truncate)) {
            sdf = sdf.substring(0, maxlength);
            dataField = sdf;
          } else {
            if (sb.length() > 0)
              sb.append(", ");
            sb.append(getI18N("foorm.maxlength.field", "Field > " + maxlength
                + " Field: ", loader));
            sb.append(getI18N(label, loader));
          }
        }
      }

      if ("integer".equals(type) || "radio".equals(type) || "checkbox".equals(type) ) {
        if (dataField == null) {
          if (dataMap != null)
            dataMap.put(field, null);
        } else if (dataField instanceof Integer) {
          if (dataMap != null)
            dataMap.put(field, dataField);
        } else {
          try {
            Integer ival = new Integer(sdf);
            if (dataMap != null)
              dataMap.put(field, ival);
          } catch (Exception e) {
            if (sb.length() > 0)
              sb.append(", ");
            sb.append(getI18N("foorm.integer.field", "Field should be an integer: ",
                loader));
            sb.append(getI18N(label, loader));
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
          sb.append(getI18N("foorm.id.field", "Field has invalid characters: ", loader));
          sb.append(getI18N(label, loader));
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
          sb.append(getI18N("foorm.url.field", "Field is not a url: ", loader));
          sb.append(getI18N(label, loader));
        }
      }

      if ("text".equals(type) || "textarea".equals(type)) {
        if (sdf == null) {
          if (dataMap != null)
            dataMap.put(field, null);
        } else {
          if (dataMap != null)
            dataMap.put(field, sdf);
        }
      }
    }
    if (sb.length() < 1)
      return null;
    return sb.toString();
  }

  /**
   * 
   * @param dataMap
   * @return
   */
  public String[] insertForm(Map<String, Object> dataMap) {
    StringBuffer fields = new StringBuffer();
    StringBuffer qmarks = new StringBuffer();
    for (String key : dataMap.keySet()) {
      if (qmarks.length() > 0) {
        fields.append(", ");
        qmarks.append(", ");
      }
      fields.append(key);
      qmarks.append("?");
    }
    // fields.append(" ) VALUES (");
    // fields.append(qmarks);
    // fields.append(" ) ");
    return new String[] { fields.toString(), qmarks.toString() };
  }

  /**
   * 
   * @param fieldinfo
   * @return
   */
  public String formSelect(String[] fieldinfo) {
    StringBuffer fields = new StringBuffer();
    for (String line : fieldinfo) {
      Properties info = parseFormString(line);
      String field = info.getProperty("field");
      String type = info.getProperty("type");
      if (field == null || type == null) {
        throw new IllegalArgumentException(
            "All model elements must include field name and type");
      }
      if ( "header".equals(type) ) continue;

      if (fields.length() > 0) {
        fields.append(", ");
      }
      fields.append(field);
    }
    return fields.toString();
  }

  /**
   * 
   * @param dataMap
   * @return
   */
  public String updateForm(Map<String, Object> dataMap) {
    StringBuffer fields = new StringBuffer();
    for (String key : dataMap.keySet()) {
      if (!dataMap.containsKey(key))
        continue;
      if ("created_at".equals(key))
        continue;
      if (fields.length() > 0)
        fields.append(", ");
      fields.append(key);
      fields.append("=?");
    }
    return fields.toString();
  }

  /**
   * 
   * @param dataMap
   * @return
   */
  public Object[] getInsertObjects(Map<String, Object> dataMap) {
    Object[] retval = new Object[dataMap.size()];
    int i = 0;
    for (String key : dataMap.keySet()) {
      retval[i++] = dataMap.get(key);
    }
    return retval;
  }

  /**
   * 
   * @param dataMap
   * @return
   */
  public Object[] getUpdateObjects(Map<String, Object> dataMap) {
    int size = dataMap.size();
    for (String key : dataMap.keySet()) {
      if (!dataMap.containsKey(key))
        size--;
      if ("created_at".equals(key))
        size--;
    }
    Object[] retval = new Object[size];
    int i = 0;
    for (String key : dataMap.keySet()) {
      if (!dataMap.containsKey(key))
        continue;
      if ("created_at".equals(key))
        continue;
      retval[i++] = dataMap.get(key);
    }
    return retval;
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
      if (field == null || type == null) {
        throw new IllegalArgumentException(
            "All model elements must include field name and type");
      }
      if ("radio".equals(type) || "checkbox".equals(type) ) {
        // Field = Always Off (0), Always On (1), or Delegate(2)
        int value = getInt(getField(controlRow, field));
        if ( value == 2 || ! isFieldSet(controlRow, field) ) ret.add(line);
      } else {
        // Allow = 0ff (0) or On (1)
        int value = getInt(getField(controlRow, "allow" + field));
        if (value == 1 || ! isFieldSet(controlRow, "allow"+field) ) ret.add(line);
      }
    }
    return ret.toArray(new String[ret.size()]);
  }

  // http://technology-ameyaaloni.blogspot.com/2010/06/mysql-to-hsql-migration-tips.html
  /**
   * 
   */
  public String formSql(String fieldinfo, String vendor) {
    Properties info = parseFormString(fieldinfo);
    String field = info.getProperty("field", null);
    String type = info.getProperty("type", null);
    if ( "header".equals(type) ) return null;
    String maxs = info.getProperty("maxlength", null);
    int maxlength = 0;
    if (maxs != null)
      maxlength = (new Integer(maxs)).intValue();
    if (maxlength < 1)
      maxlength = 80;
    String required = info.getProperty("required", null);

    if (field == null || type == null) {
      throw new IllegalArgumentException(
          "All model elements must include field name and type");
    }

    String schema = null;

    if ("key".equals(type)) {
      if ("hsqldb".equals(vendor)) {
        schema = "INTEGER IDENTITY PRIMARY KEY";
      } else if ("oracle".equals(vendor)) {
        schema = "INTEGER";
      } else {
        schema = "INTEGER NOT NULL AUTO_INCREMENT";
      }
    } else if ("autodate".equals(type)) {
      if ("oracle".equals(vendor)) {
        schema = "TIMESTAMP NOT NULL";
      } else {
        schema = "DATETIME NOT NULL";
      }
    } else if ("integer".equals(type)) {
      if ("oracle".equals(vendor)) {
        schema = "INTEGER";
      } else {
        schema = "INT";
      }
    } else if ("url".equals(type) || "text".equals(type) || "textarea".equals(type)) {
      if ("oracle".equals(vendor)) {
        if (maxlength < 4000) {
          schema = "VARCHAR2(" + maxlength + ")";
        } else {
          schema = "CLOB";
        }
      } else if ("hsqldb".equals(vendor)) {
        schema = "VARCHAR(" + maxlength + ")";
      } else {
        if (maxlength < 512) {
          schema = "VARCHAR(" + maxlength + ")";
        } else {
          schema = "TEXT(" + maxlength + ")";
        }
      }
    } else if ("radio".equals(type) || "checkbox".equals(type) ) {
      if ("oracle".equals(vendor)) {
        schema = "NUMBER(1) DEFAULT '0'";
      } else {
        schema = "TINYINT DEFAULT '0'";
      }
    }
    if (schema == null)
      return null;

    if ("true".equals(required) && !(schema.indexOf("NOT NULL") > 0))
      schema += " NOT NULL";
    return "    " + field + " " + schema;
  }

  /**
   * 
   * @param table
   * @param formDefinition
   * @param vendor
   * @param doReset
   * @return
   */
  public String[] formSqlTable(String table, String[] formDefinition, String vendor,
      boolean doReset) {
    String theKey = formSqlKey(formDefinition);
    String fieldList = formSqlFields(formDefinition, vendor);
    ArrayList<String> rv = new ArrayList<String>();
    if (doReset)
      rv.add("DROP TABLE " + table);
    if ("oracle".equals(vendor)) {
      rv.add("CREATE TABLE " + table + " (\n" + formSqlFields(formDefinition, vendor)
          + "\n)\n");
      if (theKey != null) {
        String seqName = getSqlSequence(table, theKey, vendor);
        if (seqName != null) {
          if (doReset)
            rv.add("DROP SEQUENCE " + seqName);
          rv.add("CREATE SEQUENCE " + seqName + " INCREMENT BY 1 START WITH 1\n");
        }
      }
    } else {
      String keySpec = "";
      if (theKey != null)
        keySpec = ",\n PRIMARY KEY( " + theKey + " )";
      rv.add("CREATE TABLE " + table + " (\n" + formSqlFields(formDefinition, vendor)
          + keySpec + "\n)\n");
    }
    return rv.toArray(new String[rv.size()]);
  }

  /**
   * 
   * @param table
   * @param theKey
   * @param vendor
   * @return
   */
  public String getSqlSequence(String table, String theKey, String vendor) {
    if (!"oracle".equals(vendor))
      return null;
    if (table == null || theKey == null)
      return null;
    return table + "_" + theKey + "_sequence";
  }

  /**
   * 
   * @param formDefinition
   * @param vendor
   * @return
   */
  public String formSqlFields(String[] formDefinition, String vendor) {
    StringBuffer sb = new StringBuffer();
    for (String formField : formDefinition) {
      String retval = formSql(formField, vendor);
      if (retval == null)
        continue;
      if (sb.length() > 0)
        sb.append(",\n");
      sb.append(retval);
    }
    return sb.toString();
  }

  /**
   * 
   * @param formDefinition
   * @return
   */
  public String formSqlKey(String[] formDefinition) {
    StringBuffer sb = new StringBuffer();
    String theKey = null;
    for (String formField : formDefinition) {
      Properties info = parseFormString(formField);
      String field = info.getProperty("field", null);
      String type = info.getProperty("type", null);
      if (field == null || type == null) {
        throw new IllegalArgumentException(
            "All model elements must include field name and type");
      }
      if (!"key".equals(type))
        continue;
      if (theKey != null) {
        throw new IllegalArgumentException("Models can only have one key column.");
      }
      theKey = field;
    }
    return theKey;
  }

  // Paging helpers

  // startRec is zero-based
  /**
   * 
   */
  public String getPagedSelect(String sqlIn, int startRec, int endRec, String vendor) {
    if ("hsqldb".equals(vendor)) {
      if (startRec > endRec)
        return null;
      sqlIn = sqlIn.trim();
      int position = sqlIn.toLowerCase().indexOf("select ");
      if (position != 0)
        return null;
      int recordCount = (endRec - startRec) + 1;
      String retval = "select limit " + startRec + " " + recordCount + " "
          + sqlIn.substring(position + 7);
      return retval;
    } else if ("oracle".equals(vendor)) {
      if (startRec > endRec)
        return null;
      String retval = "select * from ( select a.*, ROWNUM rnum from ( " + sqlIn
          + " ) a where rownum <= " + (endRec + 1) + " ) where rnum >= " + (startRec + 1);
      return retval;
    } else { // MySql for sure
      if (startRec > endRec)
        return null;
      int recordCount = (endRec - startRec) + 1;
      return sqlIn + " limit " + startRec + "," + recordCount;
    }
  }

  /*
   * public static void main(String[] args) { System.out.println("Hello, World");
   * System.out.println(parseFormString("title:text:required=true:size=25"));
   * System.out.println
   * (parseFormString("description:textarea:required=true:rows=2:cols=25"));
   * System.out.println
   * (parseFormString("sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"
   * ));
   * 
   * HashMap row = new HashMap(); row.put("title", "Fred"); row.put("description","Desc");
   * row.put("sendemail", new Integer(1)); row.put("acceptgrades", new Integer(1));
   * row.put("preferheight", new Integer(100));
   * 
   * System.out.println(getField(row,"title"));
   * 
   * System.out.println(formInput(row,"title:text:required=true:size=25"));
   * System.out.println
   * (formInput(row,"description:textarea:required=true:rows=2:cols=25"));
   * System.out.println
   * (formInput(row,"sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"
   * ));
   * 
   * String [] test_form = { "title:text:size=80",
   * "preferheight:integer:label=bl_preferheight:size=80",
   * "sendname:radio:label=bl_sendname:choices=off,on,content",
   * "acceptgrades:radio:label=bl_acceptgrades:choices=off,on", "homepage:url:size=100",
   * "webpage:url:size=100",
   * "customparameters:textarea:required=true:label=bl_customparameters:rows=5:cols=25" }
   * ;
   * 
   * System.out.println(formInput(row, test_form));
   * 
   * System.out.println(formOutput(row, test_form));
   * 
   * HashMap hm = new HashMap(); String [] blah = { "blah" }; String [] number = { "1" };
   * String [] cnn = { "http://www.cnn.com/" }; hm.put("title",blah);
   * hm.put("acceptgrades",blah); hm.put("preferheight",number); hm.put("homepage",blah);
   * hm.put("webpage",cnn);
   * 
   * System.out.println(formValidate(hm, test_form, null));
   * 
   * HashMap<String, Object> rm = new HashMap<String,Object> ();
   * 
   * System.out.println(formExtract(hm, test_form, null,rm));
   * System.out.println("--- Result Map ---"); System.out.println(rm);
   * 
   * HashMap crow = new HashMap(); crow.put("allowtitle", new Integer(0)); // Should
   * suppress crow.put("allowpreferheight",new Integer(1)); crow.put("allowwebpage",new
   * Integer(0)); // Should suppress crow.put("sendname", new Integer(1)); // Should
   * suppress crow.put("acceptgrades", new Integer(2)); // crow.put("preferheight", new
   * Integer(100)); (Leave alone - should be allowed)
   * 
   * String [] ff = filterForm(crow, test_form); System.out.println(Arrays.toString(ff));
   * 
   * System.out.println("--- Required I18N Strings ---"); ArrayList<String> strings =
   * utilI18NStrings(test_form); System.out.println(strings); }
   */
}
