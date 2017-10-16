/**
 * Copyright (c) 2004-2013 The Apereo Foundation
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
//http://stackoverflow.com/questions/4328711/read-url-to-string-in-few-lines-of-java-code

//Added maxlines limiter
package org.sakaiproject.tool.postem;

import java.net.*;
import java.io.*;


public class URLConnectionReader {

	public static String NEW_LINE = System.getProperty("line.separator");
	
	public static String getText(String url) throws IOException {
		return getText(url,1000);
	}
	
	public static String getText(String url, int maxLines) throws IOException {
        URL website = new URL(url);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                    connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;
        int lines = 0;
        
        while ((inputLine = in.readLine()) != null && lines < maxLines)  {
            response.append(inputLine);
            response.append(NEW_LINE);
            lines ++;
        }
        in.close();
        return response.toString();
    }
}
