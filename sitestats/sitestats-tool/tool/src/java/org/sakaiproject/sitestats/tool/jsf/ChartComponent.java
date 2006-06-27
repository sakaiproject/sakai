/**********************************************************************************
 *
 * Copyright (c) 2006 Universidade Fernando Pessoa
 *
 * Licensed under the Educational Community License Version 1.0 (the "License");
 * By obtaining, using and/or copying this Original Work, you agree that you have read,
 * understand, and will comply with the terms and conditions of the Educational Community License.
 * You may obtain a copy of the License at:
 *
 *      http://cvs.sakaiproject.org/licenses/license_1_0.html
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 **********************************************************************************/
package org.sakaiproject.sitestats.tool.jsf;

import java.io.IOException;
import java.util.Map;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class ChartComponent extends UIComponentBase {
	public static final String COMPONENT_TYPE = "org.sakaiproject.sitestats.tool.jsf.Chart";

	/** Our log (commons). */
	private static Log	LOG				= LogFactory.getLog(ChartComponent.class);

	// Parameters
	private String		type;
	//private int			border			= 0;


	public String getFamily() {
		return "SiteStatsFamily";
	}
	
	public void encodeBegin(FacesContext context) throws IOException {
//		ResponseWriter writer = context.getResponseWriter();
//		String path = FacesContext.getCurrentInstance().getExternalContext().getRequestContextPath();
		Map attr = getAttributes();
		
		// parse parameters
		if(!parseParameters(attr)) return;
		
		// write output
		/*writer.startElement("img", this);
		writer.writeAttribute("id", id, null);
		writer.writeAttribute("width", new Integer(width), null);
		writer.writeAttribute("height", new Integer(height), null);
		writer.writeAttribute("src", path + "/chartServlet?key=" + key, null);
		writer.writeAttribute("border", border+"", null);
		writer.endElement("img");*/
		
	}
	
	public void endodeEnd(FacesContext context) throws IOException{
	}
	
	private boolean parseParameters(Map map){
		// Required: TYPE
		type = getParam(map, ChartTag.P_TYPE, null);
		if(type == null) LOG.error("ChartComponent: parameter 'type' is required!");
		
		// Optional: BORDER
		//border = getParam(map, ChartTag.P_BORDER, 0);
		
		return true;
	}
	
	private String getParam(Map attr, String key, String def) {
		if (attr.containsKey(key)) 
			return (String) attr.get(key);
		else
			return def;
	}

//	private int getParam(Map attr, String key, int def) {
//		if (attr.containsKey(key))
//			return Integer.parseInt(((String) attr.get(key)).replaceAll("%", ""));
//		 else 
//			return def;
//	}
//
//	private boolean getParam(Map attr, String key, boolean def) {
//		if (attr.containsKey(key))
//			return Boolean.valueOf((String)attr.get(key)).booleanValue();
//		else
//			return def;
//	}
}
