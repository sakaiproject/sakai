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
		} else if ( kv.length == 1 ) {
			op.setProperty(positional[i++], kv[0]);
		}
	}
	return op; 
    } 

    // Abstract this away for testing purposed
    public Object getField(Object row, String column)
    {
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

    // Abstract to be overridden
    public String htmlSpecialChars(String str)
    {
	return str;
    }

    // Abstract this away for testing purposes
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
	if ( retval == null ) return def;
	return null;
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
	sb.append(".input\" class=\"shorttext\" style=\"clear:none;\">");
	if ( required ) 
	{
		sb.append("<span class=\"reqStar\" title=\"");
		sb.append(getI18N(label, loader));
		sb.append("\">*</span>");
	}
	sb.append("<label for=\"");
	sb.append(field);
	sb.append("\">");
	sb.append(getI18N(label,loader));
	sb.append("</label>");
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

    public String formInputTextArea(String value, String field, String label,
		boolean required, String rows, String cols, Object loader)
    {
	if ( value == null ) value = "";
	StringBuffer sb = new StringBuffer();
	formInputStart(sb, field, "textarea", label, required, loader);
	sb.append("<textarea id=\"");
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

    public String formInputRadio(Integer value, String field, String label,
		boolean required, String [] choices, Object loader)
    {
	StringBuffer sb = new StringBuffer();
	formInputStart(sb, field, "radio", label, required, loader);
	int val = 0;
	if ( value != null ) val = value.intValue();

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

    public String formInputInteger(Integer value,String field,String label,
		boolean required,String size, Object loader)
    {
	if ( value == null ) value = new Integer(0);
	return formInputText(value.toString(),field,label,required,size,loader);
    }

    public String formInput(Object row,String fieldinfo, Object loader)
    {
	Properties info = parseFormString(fieldinfo);
	String field = info.getProperty("field", null);
	String type = info.getProperty("type", null);
 	Object value = getField(row, field);
	if ( field == null || type == null ) return null;

	String label = info.getProperty("label",field);
	boolean required = "true".equals(info.getProperty("required","false"));
	String size = info.getProperty("size","40");
	String cols = info.getProperty("cols","25");
	String rows = info.getProperty("rows","2");

	if ( "integer".equals(type) ) return formInputInteger((Integer) value,field,label,required,size,loader);
	if ( "text".equals(type) ) return formInputText((String) value,field,label,required,size,loader);
	if ( "url".equals(type) ) return formInputURL((String) value,field,label,required,size,loader);
	if ( "id".equals(type) ) return formInputId((String) value,field,label,required,size,loader);
	if ( "textarea".equals(type) ) return formInputTextArea((String) value,field,label,required,rows,cols,loader);
	if ( "radio".equals(type) ) 
	{
		String choices = info.getProperty("choices",null);
		if ( choices == null ) return "\n<!-- Foorm.formInput() requires choices=on,off,part -->\n";
		String [] choiceList = choices.split(",");
		if ( choiceList.length < 1 ) return "\n<!-- Foorm.formInput() requires choices=on,off,part -->\n";
		return formInputRadio((Integer) value, field, label, required, choiceList, loader);
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
	StringBuffer sb = new StringBuffer();
	for(String formInput : formDefinition ) 
	{
		sb.append(formInput(row, formInput));
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
	sb.append("<div class=\"row\">\n");
	sb.append(getI18N(label, loader));
	sb.append("<br/>");
    }

    public void formOutputEnd(StringBuffer sb, String field,String label, Object loader)
    {
	sb.append("</div>\n");
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

    public String formOutputRadio(Integer value, String field, String label,
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

    public String formOutputInteger(Integer value,String field,String label, Object loader)
    {
	String strval = "";
	if ( value != null ) strval = value.toString();
	return formOutputText(strval,field,label,loader);
    }

    public String formOutput(Object row,String fieldinfo, Object loader)
    {
	Properties info = parseFormString(fieldinfo);
	String field = info.getProperty("field", null);
	String type = info.getProperty("type", null);
 	Object value = getField(row, field);
	if ( field == null || type == null ) return null;

	String label = info.getProperty("label",field);

	if ( "integer".equals(type) ) return formOutputInteger((Integer) value,field,label,loader);
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
		return formOutputRadio((Integer) value, field, label, choiceList, loader);
	}
        return "\n<!-- Foorm.formOutput() unrecognized type " + type + " field="+field+" -->\n";
    }

    public String formOutput(Object row, String [] formDefinition)
    {
	StringBuffer sb = new StringBuffer();
	for(String formOutput : formDefinition ) 
	{
		sb.append(formOutput(row, formOutput));
		sb.append("\n");
	}
	return sb.toString();
    }

    public String formValidate(HashMap parms, String [] formDefinition, Object loader)
    {
    	return formExtract(parms, formDefinition, loader, null);

    }

    // dataMap should be empty
    public String formExtract(HashMap parms, String [] formDefinition, 
		Object loader, Map<String, Object> dataMap)
    {
	StringBuffer sb = new StringBuffer();

	for ( String formInput : formDefinition ) 
	{
		Properties info =  parseFormString(formInput);
		String field = info.getProperty("field", null);
		String type = info.getProperty("type", null);
		if ( field == null || type == null ) continue;
		String label = info.getProperty("label",field);
		String dataField = null;
		String [] dataArray = (String []) parms.get(field);
		if ( dataArray != null ) dataField = dataArray[0].trim();
		// System.out.println("field="+field+" data="+dataField);

		if ( "true".equals(info.getProperty("required")) && ( dataField == null || dataField.length() < 1 ) )
		{
			if ( sb.length() > 0 ) sb.append(", ");
			sb.append(getI18N("foorm.missing.field", "Required Field: ", loader));
			sb.append(getI18N(label, loader));
		}
		if ( "integer".equals(type) || "radio".equals(type) ) {
			if ( dataField == null ||  dataField.length() < 1 ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else {
				try {
					Integer ival = new Integer(dataField);
					if ( dataMap != null ) dataMap.put(field,ival);
				} catch (Exception e) {
					if ( sb.length() > 0 ) sb.append(", ");
					sb.append(getI18N("foorm.numeric.field", "Field should be a number: ", loader));
					sb.append(getI18N(label,loader));
				}
			}
		}
		if ( "id".equals(type) ) {
			if ( dataField == null ||  dataField.length() < 1 ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else if ( dataField.matches("^[0-9a-zA-Z._-]*$") ) {
				if ( dataMap != null ) dataMap.put(field,dataField);
			} else { 
				if ( sb.length() > 0 ) sb.append(", ");
				sb.append(getI18N("foorm.id.field", "Field has invalid characters: ", loader));
				sb.append(getI18N(label, loader));
			}
		}

		// Should we check size?
		if ( "url".equals(type) ) {
			if ( dataField == null ||  dataField.length() < 1 ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else if ( dataField.matches("^(http://|https://)[a-z0-9][a-z0-9]*.*") ) {
				if ( dataMap != null ) dataMap.put(field,dataField);
			} else { 
				if ( sb.length() > 0 ) sb.append(", ");
				sb.append(getI18N("foorm.url.field", "Field is not a url: ", loader));
				sb.append(getI18N(label, loader));
			}
		}

		// Should we check size?
		if ( "text".equals(type) || "textarea".equals(type) ) {
			if ( dataField == null ||  dataField.length() < 1 ) {
				if ( dataMap != null ) dataMap.put(field,null);
			} else {
				if ( dataMap != null ) dataMap.put(field,dataField);
			}
		}
	}
	if ( sb.length() < 1 ) return null;
	return sb.toString();
    }

    // Filter a form definition based on a controlling row.
    //
    // The controlling row has fields that are interpreted as
    // 0=force off, 1=force on, 2 = delegate setting
    // For radio buttons in our form, it simply checks for 
    // the field of the same name in the controlling row.  
    // For non-radio fields, it looks for a field in the 
    // controlling row prepended by 'allow'.
    public String [] filterForm(Object controlRow, String [] fieldinfo)
    {
	ArrayList<String> ret = new ArrayList<String> ();
	for (String line : fieldinfo) 
	{
		Properties fields = parseFormString(line);
		String field = fields.getProperty("field", null);
		String type = fields.getProperty("type", null);
		if ( field == null || type == null) continue;
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
