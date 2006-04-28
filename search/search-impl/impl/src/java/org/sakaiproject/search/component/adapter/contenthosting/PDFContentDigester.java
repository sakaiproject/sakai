/**
 * 
 */
package org.sakaiproject.search.component.adapter.contenthosting;

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
		try
		{
			PDFTextStripper stripper = new PDFTextStripper();
			StringWriter sw = new StringWriter();
			PDDocument pddoc = PDDocument.load(contentResource.streamContent()); 
			stripper.writeText(pddoc
					, sw);
			pddoc.close();
			return sw.toString();
		}
		catch (Exception ex)
		{
			throw new RuntimeException("Failed to get content for indexing",ex);
		}
	}

	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

}
