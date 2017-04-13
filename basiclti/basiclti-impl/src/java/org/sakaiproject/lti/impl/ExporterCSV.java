/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.lti.impl;

import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.lti.api.LTIExporter;

public class ExporterCSV implements LTIExporter {
	private StringBuilder result;
	private String csvSeparator;
	private boolean emptyLine = true;
	
	private static Log M_log = LogFactory.getLog(ExporterCSV.class);
	
	public ExporterCSV() {
		result = new StringBuilder();
		csvSeparator = ServerConfigurationService.getString("csv.separator",",");
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.basiclti.util.LTIExporter#newLine()
	 */
	@Override
	public void newLine() {
		if(result.length() > 0)
			result.append("\n");
		emptyLine = true;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.basiclti.util.LTIExporter#addCell(java.lang.String)
	 */
	@Override
	public void addCell(String text) {
		if(result.length() > 0 && !emptyLine)
			result.append(csvSeparator);
		
		result.append(escapeText(text));
		emptyLine = false;
	}
	
	/* (non-Javadoc)
	 * @see org.sakaiproject.basiclti.util.LTIExporter#write()
	 */
	@Override
	public void write(OutputStream out) {
		try {
			out.write(result.toString().getBytes());
		} catch(Exception e) {
			M_log.error("Error exporting to CSV : "+e.getMessage());
		}
	}
	
	private String escapeText(String text) {
		if(text == null) return "";
		
		if(text.contains("\""))
			text = text.replace("\"", "\"\"");
		if(StringUtils.indexOfAny(text, new String[]{"\"", "\n", "\r", csvSeparator}) > -1)
			text = '"' + text + '"';
		
		return text;
	}
}
