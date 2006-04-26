/**
 * 
 */
package org.sakaiproject.search.component.adapter.contenthosting;

import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.sakaiproject.content.api.ContentResource;

/**
 * @author ieb
 */
public class XLContentDigester extends BaseContentDigester
{
	private static Log log = LogFactory.getLog(XLContentDigester.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public String getContent(ContentResource contentResource)
	{

		try
		{
			 HSSFWorkbook workbook = new HSSFWorkbook(contentResource
					.streamContent());
			StringBuffer sb = new StringBuffer();
			int nsheets = workbook.getNumberOfSheets();
			
			for (int i = 0; i < nsheets; i++)
			{
				HSSFSheet sheet = workbook.getSheetAt(i);
				int r = sheet.getFirstRowNum();
				int lr = sheet.getLastRowNum();
				for ( ; r <= lr; r++ ) {
					HSSFRow  row = sheet.getRow(r);
					short c = row.getFirstCellNum();
					short lc = row.getLastCellNum();
					for ( ; c <= lc; c++ ) {
						HSSFCell cell = row.getCell(c);
						HSSFRichTextString cstr = cell.getRichStringCellValue();
						sb.append(cstr.getString()).append(" ");
					}
				}
			}
			return sb.toString();
		}
		catch (Exception e)
		{
			log.error(e);
			throw new RuntimeException("Failed to read content for indexing ");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContentReader(org.sakaiproject.content.api.ContentResource)
	 */
	public Reader getContentReader(ContentResource contentResource)
	{
		return new StringReader(getContent(contentResource));
	}

}
