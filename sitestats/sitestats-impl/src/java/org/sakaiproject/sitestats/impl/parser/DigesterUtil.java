/**
 * $URL$
 * $Id$
 *
 * Copyright (c) 2006-2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.sitestats.impl.parser;

import java.beans.IntrospectionException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.betwixt.AttributeDescriptor;
import org.apache.commons.betwixt.expression.Context;
import org.apache.commons.betwixt.io.BeanReader;
import org.apache.commons.betwixt.io.BeanWriter;
import org.apache.commons.betwixt.strategy.DefaultObjectStringConverter;
import org.apache.commons.betwixt.strategy.ValueSuppressionStrategy;
import org.apache.commons.digester.Digester;
import org.sakaiproject.sitestats.api.PrefsData;
import org.sakaiproject.sitestats.api.event.ToolInfo;
import org.sakaiproject.sitestats.api.report.ReportDef;
import org.sakaiproject.sitestats.api.report.ReportParams;

public class DigesterUtil {

	public static Digester configureToolEventsDefDigester(String prefix, Digester digester) {        
	    // root
	    digester.addObjectCreate(prefix + "toolEventsDef", ArrayList.class );
	
	    // tool tag
	    ToolFactoryImpl toolFactory = new ToolFactoryImpl();
	    digester.addFactoryCreate(prefix + "toolEventsDef/tool", toolFactory);
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/toolId", "toolId" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/additionalToolIds", "additionalToolIdsStr" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/selected", "selected" );
	    digester.addSetNext(prefix + "toolEventsDef/tool", "add" );
	
	    // event tag
	    EventFactoryImpl eventFactoryImpl = new EventFactoryImpl();
	    digester.addFactoryCreate(prefix + "toolEventsDef/tool/event", eventFactoryImpl);
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/eventId", "eventId" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/selected", "selected" );
	    digester.addBeanPropertySetter(prefix + "toolEventsDef/tool/event/anonymous", "anonymous" );
	    digester.addSetNext(prefix + "toolEventsDef/tool/event", "addEvent" );
	    
	    return digester;
	}

	public static Digester configurePrefsDigester(Digester digester) {        
	    // prefs root
	    digester.addObjectCreate("prefs", PrefsData.class );
	    digester.addSetProperties("prefs" );
	    digester.addBeanPropertySetter("prefs/listToolEventsOnlyAvailableInSite", "setListToolEventsOnlyAvailableInSite" );
	    digester.addBeanPropertySetter("prefs/chartIn3D", "setChartIn3D" );
	    digester.addBeanPropertySetter("prefs/chartTransparency", "setChartTransparency" );
	    digester.addBeanPropertySetter("prefs/itemLabelsVisible", "setItemLabelsVisible" );
	    digester.addBeanPropertySetter("prefs/useAllTools", "setUseAllTools" );
	    
	    // toolEventsDef
	    digester = configureToolEventsDefDigester("prefs/", digester);
	    digester.addSetNext("prefs/toolEventsDef", "setToolEventsDef" );
	    
	    
	    return digester;
	}

	public static List<ToolInfo> parseToolEventsDefinition(InputStream input) throws Exception{
		Digester digester = new Digester();
	    digester.setValidating(false);
	    
	    digester = configureToolEventsDefDigester("", digester);
	
	    // eventParserTip tag
	    EventParserTipFactoryImpl eventParserTipFactoryImpl = new EventParserTipFactoryImpl();
	    digester.addFactoryCreate("toolEventsDef/tool/eventParserTip", eventParserTipFactoryImpl);
	    digester.addSetNestedProperties("toolEventsDef/tool/eventParserTip");
	    digester.addSetNext("toolEventsDef/tool/eventParserTip", "setEventParserTip" );
	    
	    return (List<ToolInfo>) digester.parse( input );
	}

	public static ReportParams convertXmlToReportParams(String inputString) throws Exception {
		BeanReader beanReader = getBeanReader();	    
	    StringReader reader = null;
	    ReportParams reportParams = null;
	    try{
		    reader = new StringReader(inputString);
		    reportParams = (ReportParams) beanReader.parse(reader);
	    }finally{
	    	if(reader != null) {
	    		reader.close();
	    	}
	    }
	    return reportParams;
	}

	public static String convertReportParamsToXml(ReportParams reportParams) throws Exception {
		String xml = null;
		StringWriter outputWriter = null;
		try{
			outputWriter = new StringWriter(); 
			outputWriter.write("<?xml version='1.0' ?>"); 
			BeanWriter beanWriter = getBeanWriter(outputWriter);
			beanWriter.write("ReportParams", reportParams);
			xml = outputWriter.toString();
		}finally{
			outputWriter.close();
		}
        return xml;
	}
	


	public static List<ReportDef> convertXmlToReportDefs(String inputString) throws Exception {
		BeanReader beanReader = getBeanReader();	    
	    StringReader reader = null;
	    List<ReportDef> reportDefs = null;
	    try{
		    reader = new StringReader(inputString);
		    reportDefs = (List<ReportDef>) beanReader.parse(reader);
	    }finally{
	    	if(reader != null) {
	    		reader.close();
	    	}
	    }
	    return reportDefs;
	}

	public static String convertReportDefsToXml(List<ReportDef> reportDef) throws Exception {
		String xml = null;
		StringWriter outputWriter = null;
		try{
			outputWriter = new StringWriter();
	        outputWriter.write("<?xml version='1.0' ?>");        
	        BeanWriter beanWriter = getBeanWriter(outputWriter);
	        beanWriter.write("List", reportDef);
	        xml = outputWriter.toString();
		}finally{
			outputWriter.close();
		}
        return xml;
	}
	
	private static BeanWriter getBeanWriter(final StringWriter outputWriter) {
		BeanWriter beanWriter = new BeanWriter(outputWriter);
	    beanWriter.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
	    beanWriter.getBindingConfiguration().setMapIDs(false);
	    beanWriter.getBindingConfiguration().setValueSuppressionStrategy(new NullEmptyValueSuppressionStrategy());
	    beanWriter.getBindingConfiguration().setObjectStringConverter(new SitestatsObjectStringConverter());
	    beanWriter.setEndOfLine("");		
		return beanWriter;
	}
	
	private static BeanReader getBeanReader() throws IntrospectionException {
		BeanReader beanReader = new BeanReader();
        beanReader.getXMLIntrospector().getConfiguration().setAttributesForPrimitives(false);
        beanReader.getBindingConfiguration().setMapIDs(false);
        beanReader.getBindingConfiguration().setValueSuppressionStrategy(new NullEmptyValueSuppressionStrategy());
        beanReader.getBindingConfiguration().setObjectStringConverter(new SitestatsObjectStringConverter());
        beanReader.registerBeanClass("List", ArrayList.class);
	    beanReader.registerBeanClass("ReportDef", ReportDef.class);
	    beanReader.registerBeanClass("ReportParams", ReportParams.class);
		return beanReader;
	}
	
	private static class NullEmptyValueSuppressionStrategy extends ValueSuppressionStrategy {

		@Override
		public boolean suppressAttribute(AttributeDescriptor attributeDescriptor, String value) {
			if(value == null || "".equals(value.trim())) {
				return true;
			}
			return false;
		}
		
	}
	
	private static class SitestatsObjectStringConverter extends DefaultObjectStringConverter {
		@Override
		public Object stringToObject(String value, Class type, String flavour, Context context) {
			if(value != null && !("").equals(value.trim())) {
				return super.stringToObject(value, type, flavour, context);
			}else{
				return null;
			}
			/*if(isUtilDate(type) ){
				try{

					return formatter.parse(value);

				}catch(ParseException ex){
					handleException(ex);
					return value;
				}
			}else{
				// use ConvertUtils implementation
				return super.stringToObject(value, type, flavour, context);
			}*/			
		}
		
		/*private boolean isUtilDate(Class type) {
			return (
					    java.util.Date.class.isAssignableFrom(type) 
					&& !java.sql.Date.class.isAssignableFrom(type) 
					&& !java.sql.Time.class.isAssignableFrom(type) 
					&& !java.sql.Timestamp.class.isAssignableFrom(type)
					);
		}*/
	}

}
