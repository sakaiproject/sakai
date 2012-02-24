/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
 * Copyright (c) 2007, 2008 The Sakai Foundation
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

package org.sakaiproject.jsf.spreadsheet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class SpreadsheetUtil {
	/**
	 * Download a spreadsheet file containing the input list of data.
	 * 
	 * @param spreadsheetData a list of rows, beginning with a header row, each being a list
	 * @param fileName not including the file extension, since that's format-dependent
	 */
	public static void downloadSpreadsheetData(List<List<Object>> spreadsheetData, String fileName, SpreadsheetDataFileWriter fileWriter) {
        FacesContext faces = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
        protectAgainstInstantDeletion(response);
       	fileWriter.writeDataToResponse(spreadsheetData, fileName, response);
       	faces.responseComplete();
	}

    /**
     * Try to head off a problem with downloading files from a secure HTTPS
     * connection to Internet Explorer.
     *
     * When IE sees it's talking to a secure server, it decides to treat all hints
     * or instructions about caching as strictly as possible. Immediately upon
     * finishing the download, it throws the data away.
     *
     * Unfortunately, the way IE sends a downloaded file on to a helper
     * application is to use the cached copy. Having just deleted the file,
     * it naturally isn't able to find it in the cache. Whereupon it delivers
     * a very misleading error message like:
     * "Internet Explorer cannot download roster from sakai.yoursite.edu.
     * Internet Explorer was not able to open this Internet site. The requested
     * site is either unavailable or cannot be found. Please try again later."
     *
     * There are several ways to turn caching off, and so to be safe we use
     * several ways to turn it back on again.
     *
     * This current workaround should let IE users save the files to disk.
     * Unfortunately, errors may still occur if a user attempts to open the
     * file directly in a helper application from a secure web server.
     *
     * TODO Keep checking on the status of this.
     */
    private static void protectAgainstInstantDeletion(HttpServletResponse response) {
        response.reset();	// Eliminate the added-on stuff
        response.setHeader("Pragma", "public");	// Override old-style cache control
        response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
    }

	/**
	 * Convenience method for setting the content-disposition:attachment header with escaping a file name.
	 * @param response
	 * @param fileName unescaped file name of the attachment
	 */
	protected static void setEscapedAttachmentHeader(final HttpServletResponse response, final String fileName) {
		String escapedFilename;
		try {
			escapedFilename = URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
		} catch (UnsupportedEncodingException e) {
			escapedFilename = fileName;
		}

		FacesContext faces = FacesContext.getCurrentInstance();
		HttpServletRequest request = (HttpServletRequest) faces.getExternalContext().getRequest();
		String userAgent = request.getHeader("User-Agent");
		if (userAgent != null && userAgent.contains("MSIE")) {
			response.setHeader("Content-Disposition", "attachment" +
					((!StringUtils.isEmpty(escapedFilename)) ? ("; filename=\"" + escapedFilename + "\"") : ""));
		} else {
			response.setHeader("Content-Disposition", "attachment" +
					((!StringUtils.isEmpty(escapedFilename)) ? ("; filename*=utf-8''" + escapedFilename) : ""));
		}
	}
}
