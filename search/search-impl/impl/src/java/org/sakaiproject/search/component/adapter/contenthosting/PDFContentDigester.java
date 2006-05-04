/**
 * 
 */
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
