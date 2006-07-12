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
		if ( contentResource != null && 
				contentResource.getContentLength() > maxDigestSize  ) {
			throw new RuntimeException("Attempt to get too much content as a string on "+contentResource.getReference());
		}
		InputStream contentStream = null;
		try
		{
			contentStream = contentResource.streamContent();
			HSSFWorkbook workbook = new HSSFWorkbook(contentStream);
			StringBuffer sb = new StringBuffer();
			int nsheets = workbook.getNumberOfSheets();

			for (int i = 0; i < nsheets; i++)
			{
				HSSFSheet sheet = workbook.getSheetAt(i);
				int r = sheet.getFirstRowNum();
				int lr = sheet.getLastRowNum();
				for (; r <= lr; r++)
				{
					HSSFRow row = sheet.getRow(r);
					short c = row.getFirstCellNum();
					short lc = row.getLastCellNum();
					for (; c <= lc; c++)
					{
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
			throw new RuntimeException("Failed to read content for indexing ",
					e);
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
				}
			}
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
