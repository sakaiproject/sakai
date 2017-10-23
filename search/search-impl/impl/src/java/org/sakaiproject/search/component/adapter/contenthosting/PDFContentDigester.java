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

import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.exception.ServerOverloadException;
import org.sakaiproject.search.api.SearchUtils;

/**
 * @author ieb
 */
public class PDFContentDigester extends BaseContentDigester
{
	private static Logger log = LoggerFactory.getLogger(PDFContentDigester.class);

	public String getContent(ContentResource contentResource)
	{
		if (contentResource == null) {
			throw new RuntimeException("Null contentResource passed to getContent");
		}

		InputStream contentStream = null;
		PDDocument pddoc = null;
		try {
			contentStream = contentResource.streamContent();
			pddoc = PDDocument.load(contentStream);
			if (pddoc != null) {
				PDFTextStripper stripper = new PDFTextStripper();
				stripper.setLineSeparator("\n");		
				CharArrayWriter cw = new CharArrayWriter();
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
			
			if (contentStream != null)
			{
				try
				{
					contentStream.close();
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
