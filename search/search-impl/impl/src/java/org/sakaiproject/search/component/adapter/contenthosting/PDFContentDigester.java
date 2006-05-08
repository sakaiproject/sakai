/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;
import org.sakaiproject.content.api.ContentResource;

/**
 * @author ieb
 */
public class PDFContentDigester extends BaseContentDigester
{
	private static Log log = LogFactory.getLog(PDFContentDigester.class);

	public String getContent(ContentResource contentResource)
	{
		InputStream contentStream = null;
		StringWriter sw = null;
		try
		{
			contentStream = contentResource.streamContent();
			PDFTextStripper stripper = new PDFTextStripper();
			sw = new StringWriter();
			PDDocument pddoc = PDDocument.load(contentStream);
			stripper.writeText(pddoc, sw);
			pddoc.close();
			return sw.toString();
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to get content for indexing", ex);
		}
		finally
		{
			if (sw != null)
			{
				try
				{
					sw.close();
				}
				catch (IOException e)
				{
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
				}
			}
		}
	}

	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

}
