package org.sakaiproject.util.foorm;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.ResultSet;

// rm Foorm.class ; javac Foorm.java ; java Foorm

public class Foorm {

    public static String [] positional = { "field", "type" };
    // Parse a form field description
    // field:type:key=value:key2=value2
    public Properties parseFormString(String str) { 
	Properties op = new Properties();
	String [] pairs = str.split(":"); 
	int i = 0;
	for (String s : pairs) {
		String [] kv = s.split("=");
		if ( kv.length == 2 ) {
			op.setProperty(kv[0], kv[1]);
		} else if ( kv.length == 1 && i < positional.length ) {
			op.setProperty(positional[i++], kv[0]);
	        } else {
	                // TODO : Log something here
	        }
	}
	return op; 
    } 

    // Returns -1 on failure
    public Long getLongKey(Object key ) { return getLong(key); }

    public Long getLong(Object key)
    {
	Long retval = getLongNull(key);
	if ( retval != null ) return retval;
	return new Long(-1);
    }

    public Long getLongNull(Object key)
    {
        if ( key == null ) return null;
        // if ( key instanceof Long ) return (Long) key;
        // if ( key instanceof Integer ) return new Long((Integer) key);
	if ( key instanceof Number ) return new Long( ( (Number) key).longValue() );
        if ( key instanceof String ) {
                try {
                        return new Long( (String) key );
                } catch(Exception e) {
                       return null;
                }
        }
        return null;
    }

    // Abstract this away for testing purposes
    public Object getField(Object row, String column)
    {
    	if ( row instanceof java.util.Properties ) {
		return ( (java.util.Properties)row ).getProperty(column);
	}
	if ( row instanceof java.util.Map ) {
		return ( (java.util.Map)row ).get(column);
	}
	if ( row instanceof java.sql.ResultSet ) {
		try
		{
			return ( (java.sql.ResultSet)row ).getObject(column);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	return null;
    }

   public boolean isFieldSet(Object row, String column)
    {
    	if ( row instanceof java.util.Properties ) {
		return ( (java.util.Properties)row ).containsKey(column);
	}
	if ( row instanceof java.util.Map ) {
		return ( (java.util.Map)row ).containsKey(column);
	}
	if ( row instanceof java.sql.ResultSet ) {
		try
		{
			Object x = ( (java.sql.ResultSet)row ).getObject(column);
                        return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	return false;
    }
    public String [] getFields(String fieldInfo[])
    {
        ArrayList<String> aa = new ArrayList<String> ();
        for (String line : fieldInfo) {
                Properties info = parseFormString(line);
                String field = info.getProperty("field");
	        if ( field == null ) {
                        throw new IllegalArgumentException("All model elements must include field name and type");
                }
                aa.add(field);
        }
        
        String[] retval = new String[aa.size()];
        return (String[]) aa.toArray(retval);
    }
    
    public void setField(Object row, String key, Object value)
    {
    	if ( row instanceof java.util.Properties ) {
    	        if ( value == null ) {
		        ( (java.util.Properties)row ).setProperty(key,"");
		} else { 
		        ( (java.util.Properties)row ).setProperty(key,value.toString());
		}
	}
	if ( row instanceof java.util.Map ) {
		( (java.util.Map)row ).put(key,value);
	}
	if ( row instanceof java.sql.ResultSet ) {
		// TODO: Log message
	}
    }
		
    // Expect to be overridden
    public String htmlSpecialChars(String str)
    {
	return str;
    }

    // Expect to be overriddrn 
    public String loadI18N(String str, Object loader)
    {
	if ( loader == null ) return null;
        if ( loader instanceof Properties) { 
		return ((Properties) loader).getProperty(str,null);
	}
	return null;
    }

    // Abstract this away for testing purposes
    public String getI18N(String str, Object loader)
    {
	return getI18N(str, str, loader);
    }

    public String getI18N(String str, String def, Object loader)
    {
	if (loader == null ) return def;
	if (str == null ) return def;
	String retval = loadI18N(str, loader);
	if ( retval != null ) return retval;
	return def;
    }

    public String formInput(Object row,String fieldinfo)
    {
	return formInput(row, fieldinfo, null);
    }

    public void formInputStart(StringBuffer sb, String field, String type, 
		String label, boolean required, Object loader)
    {
	sb.append("<p id=\"");
	sb.append(field);
	sb.append(".input\" class=\"shorttext\" style=\"clear:all;\">");
	if ( label != null && required ) 
	{
		sb.append("<span class=\"reqStar\" title=\"");
		sb.append(getI18N(label, loader));
		sb.append("\">*</span>");
	}
	if ( label != null ) 
	{
        	sb.append("<label for=\"");
	        sb.append(field);
	        sb.append("\">");
	        sb.append(getI18N(label,loader));
	        sb.append("</label>");
	}
    }

    public void formInputEnd(StringBuffer sb, String field,String label,
		boolean required, Object loader)
    {
	sb.append("</p>\n");
    }

    public String formInputText(String value,String field,String label,
		boolean required, String size, Object loader)
    {
	if ( value == null ) value = "";
	StringBuffer sb = new StringBuffer();
	formInputStart(sb, field, "text", label, required, loader);
	sb.append("<input type=\"text\" id=\"");
	sb.append(field);
	sb.append("\" name=\"");
	sb.append(field);
	sb.append("\" size=\"");
	sb.append(size);
	sb.append("\" value=\"");
	sb.append(htmlSpecialChars(value));
	sb.append("\"/>");
	formInputEnd(sb, field, label, required, loader);
	return sb.toString();
    }
    
    public String formInputKey(Object value, String field)
    {
        if ( value == null ) return "";
        if ( value instanceof Integer || value instanceof Long ) 
        {
                String val = value.toString();
                return formInputHidden(val,field);
        }
        return "";
    }
    
    public String formInputHidden(String value,String field)
    {
	if ( value == null ) return "";
	if ( "".equals(value) ) return "";
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

    public String formInputTextArea(String value, String field, String label,
		boolean required, String rows, String cols, Object loader)
    {
	if ( value == null ) value = "";
	StringBuffer sb = new StringBuffer();
	sb.append("<p id=\"");
	sb.append(field);
	sb.append(".input\" class=\"longtext\" style=\"clear:all;\">");
	formInputStart(sb, field, "textarea", label, required, loader);
	sb.append("<br cler=\"all\"/>\n<textarea id=\"");
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

    public String formInputRadio(Object value, String field, String label,
		boolean required, String [] choices, Object loader)
    {
	StringBuffer sb = new StringBuffer();
	formInputStart(sb, field, "radio", label, required, loader);
	sb.append("<br clear=\"all\"/>\n");
	int val = 0;
	if ( value != null && value instanceof Integer ) val = ((Integer) value).intValue();
	if ( value != null && value instanceof String ) {
		Integer ival = new Integer((String) value);
                val = ival.intValue();
	}
	if ( val < 0 ) val = 0;
	if ( choices == null || val >= choices.length ) val = 0;
	int i = 0;
	for ( String choice : choices ) {
		String checked = "";
		if (i == val) checked = " checked=\"checked\"";
		sb.append("<input type=\"radio\" name=\"");
		sb.append(field);
		sb.append("\" value=\""+i+"\" id=\"");
		sb.append(field+"_"+choice+"\"");
		sb.append(checked);
		sb.append("/>");
		sb.append(getI18N(label+"_"+choice, loader));
		sb.append("<br/>\n");
		i++;
	}
	formInputEnd(sb, field, label, required, loader);
	return sb.toString();
    }

    public String formInputURL(String value,String field,String label,
		boolean required,String size, Object loader)
    {
	return formInputText(value,field,label,required,size,loader);
    }

    public String formInputId(String value,String field,String label,
		boolean required,String size, Object loader)
    {
	return formInputText(value,field,label,required,size,loader);
    }

    public String formInputInteger(Object value,String field,String label,
		boolean required,String size, Object loader)
    {
	if ( value == null ) value = "";
	if ( value instanceof Integer && ((Integer) value).intValue() == 0 ) value = "";
        if ( value instanceof Long && ((Long) value).intValue() == 0 ) value = "";
	if ( value instanceof String ) return formInputText((String) value,field,label,required,size,loader);
	return formInputText(value.toString(),field,label,required,size,loader);
    }

    // Produce a form for createing a new object or editing an existing object
    public String formInput(Object row,String fieldinfo, Object loader)
    {
	Properties info = parseFormString(fieldinfo);
	String field = info.getProperty("field", null);
	String type = info.getProperty("type", null);
 	Object value = getField(row, field);
	if ( field == null || type == null ) {
                throw new IllegalArgumentException("All model elements must include field name and type");
        }

	String hidden = info.getProperty("hidden",null);
        if ( "true".equals(hidden)) return "";

	String label = info.getProperty("label",field);
	boolean required = "true".equals(info.getProperty("required","false"));
	String size = info.getProperty("size","40");
	String cols = info.getProperty("cols","25");
	String rows = info.getProperty("rows","2");

        if ( "key".equals(type)) return formInputKey(value,field);
        if ( "integer".equals(type)) return formInputInteger(value,field,label,required,size,loader);
	if ( "text".equals(type) ) return formInputText((String) value,field,label,required,size,loader);
	if ( "hidden".equals(type) ) return formInputHidden((String) value,field);
	if ( "url".equals(type) ) return formInputURL((String) value,field,label,required,size,loader);
	if ( "id".equals(type) ) return formInputId((String) value,field,label,required,size,loader);
	if ( "textarea".equals(type) ) return formInputTextArea((String) value,field,label,required,rows,cols,loader);
        if ( "autodate".equals(type) ) return "";
	if ( "radio".equals(type) ) 
	{
		String choices = info.getProperty("choices",null);
		if ( choices == null ) return "\n<!-- Foorm.formInput() requires choices=on,off,part -->\n";
		String [] choiceList = choices.split(",");
		if ( choiceList.length < 1 ) return "\n<!-- Foorm.formInput() requires choices=on,off,part -->\n";
		return formInputRadio(value, field, label, required, choiceList, loader);
	}
        return "\n<!-- Foorm.formInput() unrecognized type " + type + " field="+field+" -->\n";
    }

    public ArrayList<String> utilI18NStrings(String[] fieldinfo) 
    {
	return checkI18NStrings(fieldinfo, null);
    }

    public ArrayList<String> checkI18NStrings(String[] fieldinfo, Object loader) 
    {
    	ArrayList<String> strings = new ArrayList<String>();
	for (String line : fieldinfo) {
		Properties info = parseFormString(line);
		String label = info.getProperty("label",info.getProperty("field"));
		String type = info.getProperty("type", null);
		String hidden = info.getProperty("hidden", null);
                if ( "true".equals(hidden) ) continue;
                if ( "autodate".equals(type) ) continue;

		String choices = info.getProperty("choices",null);
		if ( loadI18N(label, loader) == null ) strings.add(label);
		if ( "radio".equals(type) && choices != null ) 
		{
			String [] choiceList = choices.split(",");
			for ( String choice : choiceList) 
			{
				String newkey = label+"_"+choice;
				if ( loadI18N(newkey, loader) == null ) strings.add(newkey);
			}
		}
	}
	return strings;
    }

    public String formInput(Object row, String [] formDefinition)
    {
    	return formInput(row, formDefinition, null);
    }

    public String formInput(Object row, String [] formDefinition, Object loader)
    {
	StringBuffer sb = new StringBuffer();
	for(String formInput : formDefinition ) 
	{
		String tmp = formInput(row, formInput, loader);
                if ( tmp.length() < 1 ) continue;
		sb.append(tmp);
		sb.append("\n");
	}
	return sb.toString();
    }

    public String formOutput(Object row,String fieldinfo)
    {
	return formOutput(row, fieldinfo, null);
    }

    public void formOutputStart(StringBuffer sb, String field,String label, Object loader)
    {
	sb.append("<p class=\"row\">\n");
	if ( label != null ) {
	        sb.append("<b>");
        	sb.append(getI18N(label, loader));
	        sb.append("</b><br/>");
	}
    }

    public void formOutputEnd(StringBuffer sb, String field,String label, Object loader)
    {
	sb.append("</p>\n");
    }

    public String formOutputText(String value,String field,String label, Object loader)
    {
	if ( value == null ) value = "";
	StringBuffer sb = new StringBuffer();
	formOutputStart(sb, field, label, loader);
	sb.append(htmlSpecialChars(value));
	formOutputEnd(sb, field, label, loader);
	return sb.toString();
    }

    public String formOutputTextArea(String value, String field, String label, Object loader)
    {
	return formOutputText(value,field,label,loader);
    }

    public String formOutputRadio(Long value, String field, String label,
		String [] choices, Object loader)
    {
	int val = 0;
	if ( value != null ) val = value.intValue();
	if ( val > choices.length-1 ) val = 0;
	String str = getI18N(label+"_"+choices[val], loader);
	return formOutputText(str,field,label,loader);
    }

    public String formOutputURL(String value,String field,String label, Object loader)
    {
	return formOutputText(value,field,label,loader);
    }

    public String formOutputId(String value,String field,String label, Object loader)
    {
	return formOutputText(value,field,label,loader);
    }

    public String formOutputInteger(Long value,String field,String label, Object loader)
    {
	String strval = "";
	if ( value != null ) strval = value.toString();
	return formOutputText(strval,field,label,loader);
    }

    public String formOutput(Object row, String fieldinfo, Object loader)
    {
	Properties info = parseFormString(fieldinfo);
	String field = info.getProperty("field", null);
	String type = info.getProperty("type", null);
 	Object value = getField(row, field);
	if ( field == null || type == null ) {
                  throw new IllegalArgumentException("All model elements must include field name and type");
        }

	String hidden = info.getProperty("hidden",null);
        if ( "true".equals(hidden)) return "";

	String label = info.getProperty("label",field);

	if ( "key".equals(type) ) return "";  // Key will be handled by the caller
        if ( "autodate".equals(type) ) return "";
	if ( "integer".equals(type) ) return formOutputInteger(getLongNull(value),field,label,loader);
	if ( "text".equals(type) ) return formOutputText((String) value,field,label,loader);
	if ( "url".equals(type) ) return formOutputURL((String) value,field,label,loader);
	if ( "id".equals(type) ) return formOutputId((String) value,field,label,loader);
	if ( "textarea".equals(type) ) return formOutputTextArea((String) value,field,label,loader);
	if ( "radio".equals(type) ) 
	{
		String choices = info.getProperty("choices",null);
		if ( choices == null ) return "\n<!-- Foorm.formOutput() requires choices=on,off,part -->\n";
		String [] choiceList = choices.split(",");
		if ( choiceList.length < 1 ) return "\n<!-- Foorm.formOutput() requires choices=on,off,part -->\n";
		return formOutputRadio(getLongNull(value), field, label, choiceList, loader);
	}
        return "\n<!-- Foorm.formOutput() unrecognized type " + type + " field="+field+" -->\n";
    }

    public String formOutput(Object row, String [] formDefinition, Object loader)
    {
	StringBuffer sb = new StringBuffer();
	for(String formOutput : formDefinition ) 
	{
                String tmp = formOutput(row, formOutput, loader);
                if ( tmp.length() < 1 ) continue;
		sb.append(tmp);
		sb.append("\n");
	}
	return sb.toString();
    }

    public String formValidate(Properties parms, String [] formDefinition, boolean forInsert, Object loader)
    {
    	return formExtract(parms, formDefinition, loader, forInsert, null);
    }

    // dataMap should be empty
    public String formExtract(Object parms, String [] formDefinition, 
		Object loader, boolean forInsert, Map<String, Object> dataMap)
    {
	StringBuffer sb = new StringBuffer();

	for ( String formInput : formDefinition ) 
	{
		Properties info =  parseFormString(formInput);
		String field = info.getProperty("field", null);
		String type = info.getProperty("type", null);
	        if ( field == null || type == null ) {
                        throw new IllegalArgumentException("All model elements must include field name and type");
                }		String label = info.getProperty("label",field);

                // For update, we don't worry about fields that are not set
                if ( ( ! forInsert ) && ( ! isFieldSet(parms,field) ) ) continue;
		Object dataField = getField(parms, field);
		String sdf = null;
		if ( dataField instanceof String ) sdf = (String) dataField;
		if ( sdf != null && sdf.length() < 1 ) 
		{
		        sdf = null;
		        dataField = null;
		}
		
                if ( "autodate".equals(type) && ( "created_at".equals(field) || "updated_at".equals(field) ) ) 
                {
                        java.sql.Timestamp sqlTimestamp = new java.sql.Timestamp(new java.util.Date().getTime());
			if ( dataMap != null ) dataMap.put(field,sqlTimestamp);
                }
		// System.out.println("field="+field+" data="+dataField);

		if ( "true".equals(info.getProperty("required")) && ( dataField == null ) )
		{
			if ( sb.length() > 0 ) sb.append(", ");
			sb.append(getI18N("foorm.missing.field", "Required Field: ", loader));
			sb.append(getI18N(label, loader));
		}
		
		String maxs = info.getProperty("maxlength", null);
		if ( maxs != null && dataField instanceof String ) {
	                int maxlength = (new Integer(maxs)).intValue();
	                String truncate = info.getProperty("truncate", "true");
                        if ( sdf.length() > maxlength ) {
                                if ( "true".equals(truncate)) {
                                        sdf = sdf.substring(0,maxlength);
                                        dataField = sdf;
                                } else { 
                                        if ( sb.length() > 0 ) sb.append(", ");
			                sb.append(getI18N("foorm.maxlength.field", "Field > "+maxlength+" Field: ", loader));
			                sb.append(getI18N(label, loader));
                                }
                        }
		}
		
		if ( "integer".equals(type) || "radio".equals(type) ) {
			if ( dataField == null ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else if ( dataField instanceof Integer ) {
			        if ( dataMap != null ) dataMap.put(field, dataField);
			} else {
				try {
					Integer ival = new Integer(sdf);
					if ( dataMap != null ) dataMap.put(field,ival);
				} catch (Exception e) {
					if ( sb.length() > 0 ) sb.append(", ");
					sb.append(getI18N("foorm.integer.field", "Field should be an integer: ", loader));
					sb.append(getI18N(label,loader));
				}
			}
		}
		
		if ( "id".equals(type) ) {
			if ( sdf == null ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else if ( sdf.matches("^[0-9a-zA-Z._-]*$") ) {
				if ( dataMap != null ) dataMap.put(field,sdf);
			} else { 
				if ( sb.length() > 0 ) sb.append(", ");
				sb.append(getI18N("foorm.id.field", "Field has invalid characters: ", loader));
				sb.append(getI18N(label, loader));
			}
		}

		if ( "url".equals(type) ) {
			if ( sdf == null ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else if ( sdf.matches("^(http://|https://)[a-zA-Z0-9][a-zA-Z0-9]*.*") ) {
				if ( dataMap != null ) dataMap.put(field,sdf);
			} else { 
				if ( sb.length() > 0 ) sb.append(", ");
				sb.append(getI18N("foorm.url.field", "Field is not a url: ", loader));
				sb.append(getI18N(label, loader));
			}
		}

		if ( "text".equals(type) || "textarea".equals(type) ) {
			if ( sdf == null ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else {
				if ( dataMap != null ) dataMap.put(field,sdf);
			}
		}
       }
	if ( sb.length() < 1 ) return null;
	return sb.toString();
    }

    public String [] insertForm(Map<String, Object> dataMap)
    {
	StringBuffer fields = new StringBuffer();
	StringBuffer qmarks = new StringBuffer();
        for ( String key : dataMap.keySet() ) {
		if ( qmarks.length() > 0 ) {
			fields.append(", ");
			qmarks.append(", ");
		}
		fields.append(key);
		qmarks.append("?");
	}
	//fields.append(" ) VALUES (");
	//fields.append(qmarks);
	//fields.append(" ) ");
	return new String[] {fields.toString(), qmarks.toString()};
    }
    
    public String formSelect(String [] fieldinfo)
    {
	StringBuffer fields = new StringBuffer();
	for (String line : fieldinfo) {
		Properties info = parseFormString(line);
		String field = info.getProperty("field");
	        if ( field == null ) {
                        throw new IllegalArgumentException("All model elements must include field name and type");
                }
		if ( fields.length() > 0 ) {
			fields.append(", ");
		}
		fields.append(field);	
	}
	return fields.toString();
    }

    public String updateForm(Map<String, Object> dataMap)
    {
	StringBuffer fields = new StringBuffer();
        for ( String key : dataMap.keySet() ) {
                if ( ! dataMap.containsKey(key) ) continue;
                if ( "created_at".equals(key) ) continue;
		if ( fields.length() > 0 ) fields.append(", ");
		fields.append(key);
		fields.append("=?");
	}
	return fields.toString();
    }

    public Object [] getInsertObjects(Map<String, Object> dataMap)
    {
	Object [] retval = new Object[dataMap.size()];
	int i = 0;
        for ( String key : dataMap.keySet() ) {
		retval[i++] = dataMap.get(key);
	}
	return retval;
    }

    public Object [] getUpdateObjects(Map<String, Object> dataMap)
    {
        int size = dataMap.size();
        for ( String key : dataMap.keySet() ) {
                if ( ! dataMap.containsKey(key) ) size--;
		if ( "created_at".equals(key) ) size--;
	}
	Object [] retval = new Object[size];
	int i = 0;
        for ( String key : dataMap.keySet() ) {
                if ( ! dataMap.containsKey(key) ) continue;
                if ( "created_at".equals(key) ) continue;
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
    public String [] filterForm(Object controlRow, String [] fieldinfo)
    {
        return filterForm(controlRow, fieldinfo, null, null);
    }
    
    public String [] filterForm(String [] fieldinfo, String includePattern, String excludePattern)
    {
        return filterForm(null, fieldinfo, includePattern, excludePattern);
    }
    
    public String [] filterForm(Object controlRow, String [] fieldinfo, String includePattern, String excludePattern)
    {
	if ( fieldinfo == null ) return null;
	ArrayList<String> ret = new ArrayList<String> ();
	for (String line : fieldinfo) 
	{
		if ( includePattern != null&& ( ! line.matches(includePattern) ) ) continue;
		if ( excludePattern != null && ( line.matches(excludePattern) ) ) continue;
		Properties fields = parseFormString(line);
		String field = fields.getProperty("field", null);
		String type = fields.getProperty("type", null);
	        if ( field == null || type == null ) {
                        throw new IllegalArgumentException("All model elements must include field name and type");
                }
		if ( "radio".equals(type) ) 
		{
			// Field = Always Off (0), Always On (1), or Delegate(2)
        		Object value = getField(controlRow, field);
			if ( value != null && ! ( value instanceof Integer) ) continue;
           		if ( value == null || ((Integer)value).intValue() == 2 ) ret.add(line);
		} 
		else 
		{
			// Allow = 0ff (0) or On (1)
        		Object value = getField(controlRow, "allow"+field);
			if ( value != null && ! ( value instanceof Integer) ) continue;
           		if ( value == null || ((Integer)value).intValue() == 1 ) ret.add(line);
		}
	}
	return ret.toArray( new String[ ret.size() ] );
    }

    // http://technology-ameyaaloni.blogspot.com/2010/06/mysql-to-hsql-migration-tips.html
    public String formSql(String fieldinfo, String vendor)
    {
	Properties info = parseFormString(fieldinfo);
	String field = info.getProperty("field", null);
	String type = info.getProperty("type", null);
	String maxs = info.getProperty("maxlength", null);
        int maxlength = 0;
        if ( maxs != null ) maxlength = (new Integer(maxs)).intValue();
        if ( maxlength < 1 ) maxlength = 80;
	String required = info.getProperty("required", null);

	if ( field == null || type == null ) {
                throw new IllegalArgumentException("All model elements must include field name and type");
        }

        String schema = null;

	if ( "key".equals(type) ) {
                if ( "hsqldb".equals(vendor) ) {
                        schema = "INTEGER IDENTITY PRIMARY KEY";
		} else if  ( "oracle".equals(vendor) ) {
			schema = "INTEGER";
                } else {
                        schema = "INTEGER NOT NULL AUTO_INCREMENT";
                }
        } else if ( "autodate".equals(type) ) {
		if ( "oracle".equals(vendor) ) {
			schema = "TIMESTAMP NOT NULL";
		} else {
	                schema = "DATETIME NOT NULL";
		}
        } else if ( "integer".equals(type) ) {
		if ( "oracle".equals(vendor) ) {
			schema = "INTEGER";
		} else {
	                schema = "INT";
		}
        } else if ( "url".equals(type) || "text".equals(type) || "textarea".equals(type) ) {
		if ( "oracle".equals(vendor)  ) {
			if ( maxlength < 4000 ) {
				schema = "VARCHAR2("+maxlength+")";
			} else {
				schema = "CLOB";
			}
		} else if ( "hsqldb".equals(vendor) ) {
			schema = "VARCHAR("+maxlength+")";
		} else { 
			if ( maxlength < 512 ) {
				schema = "VARCHAR("+maxlength+")";
			} else {
				schema = "TEXT("+maxlength+")";
			}
		}
        } else if ( "radio".equals(type) ) {
		if ( "oracle".equals(vendor) ) {
			schema = "NUMBER(1) DEFAULT '0'";
		} else {
	                schema = "TINYINT DEFAULT '0'";
		}
        }
        if ( schema == null ) return null;

        if ( "true".equals(required) && ! (schema.indexOf("NOT NULL") > 0) ) schema += " NOT NULL";
        return "    " + field + " " + schema;
    }

    public String [] formSqlTable(String table, String [] formDefinition, String vendor)
    {
	String theKey = formSqlKey(formDefinition);
	String fieldList = formSqlFields(formDefinition, vendor);
	String createCommand = null;
	String sequenceCommand = null;
	if ( "oracle".equals(vendor) )
	{
		createCommand = "CREATE TABLE "+table+" (\n"+formSqlFields(formDefinition, vendor)+"\n)\n";
		if ( theKey != null ) {
			String seqName = getSqlSequence(table, theKey, vendor);
			if ( seqName != null ) sequenceCommand = "CREATE SEQUENCE "+seqName+" INCREMENT BY 1 START WITH 1\n";
		}
	}
	else
	{
		String keySpec = "";
		if (theKey != null ) keySpec = " PRIMARY KEY( "+theKey+" )";
		createCommand = "CREATE TABLE "+table+" (\n"+formSqlFields(formDefinition, vendor)+keySpec+"\n)\n";
	}
	if ( sequenceCommand == null ) return new String[] {createCommand};
	return new String[] {createCommand, sequenceCommand};
    }

    public String getSqlSequence(String table, String theKey, String vendor)
    {
	if ( ! "oracle".equals(vendor) ) return null;
	if ( table == null || theKey == null ) return null;
	return table+"_"+theKey+"_sequence";
    }

    public String formSqlFields(String [] formDefinition, String vendor)
    {
	StringBuffer sb = new StringBuffer();
	for(String formField : formDefinition ) 
	{
                String retval = formSql(formField, vendor);
                if ( retval == null ) continue;
		if ( sb.length() > 0 ) sb.append(",\n");
		sb.append(retval);
	}
	return sb.toString();
    }

    public String formSqlKey(String [] formDefinition)
    {
	StringBuffer sb = new StringBuffer();
	String theKey = null;
	for(String formField : formDefinition ) 
	{
                Properties info = parseFormString(formField);
	        String field = info.getProperty("field", null);
        	String type = info.getProperty("type", null);
	        if ( field == null || type == null ) {
                        throw new IllegalArgumentException("All model elements must include field name and type");
                }
                if ( ! "key".equals(type) ) continue;
	        if ( theKey != null ) {
                        throw new IllegalArgumentException("Models can only have one key column.");
                }
		theKey = field;
	}
	return theKey;
    }

/*
    public static void main(String[] args) {
        System.out.println("Hello, World");
        System.out.println(parseFormString("title:text:required=true:size=25"));
        System.out.println(parseFormString("description:textarea:required=true:rows=2:cols=25"));
        System.out.println(parseFormString("sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"));

	HashMap row = new HashMap();
	row.put("title", "Fred");
	row.put("description","Desc");
	row.put("sendemail", new Integer(1));
	row.put("acceptgrades", new Integer(1));
	row.put("preferheight", new Integer(100));

        System.out.println(getField(row,"title"));

	System.out.println(formInput(row,"title:text:required=true:size=25"));
	System.out.println(formInput(row,"description:textarea:required=true:rows=2:cols=25"));
	System.out.println(formInput(row,"sendemail:radio:requred=true:label=bl_sendemail:choices=on,off,part"));

	String [] test_form = {
		"title:text:size=80",
		"preferheight:integer:label=bl_preferheight:size=80",
		"sendname:radio:label=bl_sendname:choices=off,on,content",
		"acceptgrades:radio:label=bl_acceptgrades:choices=off,on",
		"homepage:url:size=100",
		"webpage:url:size=100",
		"customparameters:textarea:required=true:label=bl_customparameters:rows=5:cols=25" } ;

	System.out.println(formInput(row, test_form));

	System.out.println(formOutput(row, test_form));

        HashMap hm = new HashMap();
	String [] blah = { "blah" };
	String [] number = { "1" };
	String [] cnn = { "http://www.cnn.com/" };
	hm.put("title",blah);
	hm.put("acceptgrades",blah);
	hm.put("preferheight",number);
	hm.put("homepage",blah);
	hm.put("webpage",cnn);

	System.out.println(formValidate(hm, test_form, null));

	HashMap<String, Object> rm = new HashMap<String,Object> ();

	System.out.println(formExtract(hm, test_form, null,rm));
	System.out.println("--- Result Map ---");
	System.out.println(rm);

	HashMap crow = new HashMap();
	crow.put("allowtitle", new Integer(0));  // Should suppress
	crow.put("allowpreferheight",new Integer(1));
	crow.put("allowwebpage",new Integer(0));  // Should suppress
	crow.put("sendname", new Integer(1)); // Should suppress
	crow.put("acceptgrades", new Integer(2));  
	// crow.put("preferheight", new Integer(100)); (Leave alone - should be allowed)

	String [] ff = filterForm(crow, test_form);
	System.out.println(Arrays.toString(ff));

	System.out.println("--- Required I18N Strings ---");
    	ArrayList<String> strings = utilI18NStrings(test_form);
	System.out.println(strings);
    }
*/
}
