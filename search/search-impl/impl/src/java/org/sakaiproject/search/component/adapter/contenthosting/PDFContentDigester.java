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
 *       http://www.osedu.org/licenses/ECL-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pdfbox.pdfparser.PDFParser;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.search.api.SearchUtils;

/**
 * @author ieb
 */
public class PDFContentDigester extends BaseContentDigester
{
	private static Log log = LogFactory.getLog(PDFContentDigester.class);
	
	public String getContent(ContentResource contentResource)
	{
		if ( contentResource != null && 
				contentResource.getContentLength() > maxDigestSize  ) {
			throw new RuntimeException("Attempt to get too much content as a string on "+contentResource.getReference());
		}

		InputStream contentStream = null;
		PDFParser parser = null;
		try
		{
			contentStream = contentResource.streamContent();
			parser = new PDFParser(new BufferedInputStream(contentStream));
			parser.parse();
			PDDocument pddoc = parser.getPDDocument();
			
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setLineSeparator("\n");		
			CharArrayWriter cw = new CharArrayWriter();
			stripper.writeText(pddoc, cw);
			pddoc.close();
			return SearchUtils.appendCleanString(cw.toCharArray(),null).toString();
		}
		catch (Exception ex)
		{
			try {
				PDDocument pddoc = parser.getPDDocument();
				pddoc.close();
			} catch ( Exception e ) {
				log.debug(e);
			}
			throw new RuntimeException("Failed to get content for indexing: cause: "+ex.getMessage(), ex);
		}
		finally
		{
			if (contentStream != null)
			{
				try
				{
					contentStream.close();
				}
				catch (IOException e)
				{
					log.debug(e);
				}
			}
		}
	}
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

	

}
