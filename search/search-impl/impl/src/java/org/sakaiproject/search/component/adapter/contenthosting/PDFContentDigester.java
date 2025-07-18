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
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.search.api.SearchUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ieb
 */
@Slf4j
public class PDFContentDigester extends BaseContentDigester
{

	public String getContent(ContentResource contentResource)
	{
		if (contentResource == null) {
			throw new RuntimeException("Null contentResource passed to getContent");
		}

		PDDocument pddoc = null;
		try {
			pddoc = Loader.loadPDF(new RandomAccessReadBuffer(contentResource.getContent()));
			if (pddoc != null) {
				// Get the literal filename with extension
				ResourceProperties props = contentResource.getProperties();
				String fileName = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
				
				PDFTextStripper stripper = new PDFTextStripper();
				stripper.setLineSeparator("\n");		
				CharArrayWriter cw = new CharArrayWriter();
				
				// Include the literal filename at the beginning of the indexed content
				cw.write(fileName + "\n");
				
				stripper.writeText(pddoc, cw);
				return SearchUtils.appendCleanString(cw.toCharArray(),null).toString();
			}
		} catch (ServerOverloadException e) {
			String eMessage = e.getMessage();
			if (eMessage == null) {
				eMessage = e.toString();
			}
			throw new RuntimeException("Failed to get content for indexing: cause: ServerOverloadException: " + eMessage, e);
		}
		catch (IOException e) {
			String eMessage = e.getMessage();
			if (eMessage == null) {
				eMessage = e.toString();
			}
			throw new RuntimeException("Failed to get content for indexing: cause: IOException:  "+ eMessage , e);
		}
		finally
		{
			if (pddoc != null) {
				try {
					pddoc.close();
				} 
				catch (IOException e) {
					log.debug(e.getMessage());
				}
			}
			
			if (pddoc != null)
			{
				try
				{
					pddoc.close();
				}
				catch (IOException e)
				{
					log.debug(e.getMessage());
				}
			}
		}
		return null;
	}
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}



}
