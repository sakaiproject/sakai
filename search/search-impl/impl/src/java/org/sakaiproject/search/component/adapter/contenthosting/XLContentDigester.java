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

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import org.sakaiproject.content.api.ContentResource;

/**
 * @author ieb
 */
@Slf4j
public class XLContentDigester extends BaseContentDigester
{

	static {
		System.setProperty("org.apache.poi.util.POILogger", "org.apache.poi.util.NullLogger");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sakaiproject.search.component.adapter.contenthosting.BaseContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	
	public void loadContent(Writer writer, ContentResource contentResource)
	{
		if ( contentResource != null && 
				contentResource.getContentLength() > maxDigestSize  ) {
			throw new RuntimeException("Attempt to get too much content as a string on "+contentResource.getReference());
		}
		if (contentResource == null) {
			throw new RuntimeException("Null contentResource passed the loadContent");
		}
		
		InputStream contentStream = null;
	    try
		{
			contentStream = contentResource.streamContent();
			
            POIFSFileSystem fs = new POIFSFileSystem(contentStream);
            HSSFWorkbook workbook = new HSSFWorkbook(fs);

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                HSSFSheet sheet = workbook.getSheetAt(i);

                Iterator<Row> rows = sheet.rowIterator();
                while (rows.hasNext()) {
                    HSSFRow row = (HSSFRow) rows.next();

                    Iterator<Cell> cells = row.cellIterator();
                    while (cells.hasNext()) {
                        HSSFCell cell = (HSSFCell) cells.next();
                        switch (cell.getCellType()) {
                        case HSSFCell.CELL_TYPE_NUMERIC:
                            String num = Double.toString(cell.getNumericCellValue()).trim();
                            if (num.length() > 0) {
                                writer.write(num + " ");
                            }
                            break;
                        case HSSFCell.CELL_TYPE_STRING:
                            String text = cell.getStringCellValue().trim();
                            if (text.length() > 0) {
                                writer.write(text + " ");
                            }
                            break;
                        }
                    }
                }
            }

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
					log.debug(e.getMessage());
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
		CharArrayWriter writer = new CharArrayWriter();
		loadContent(writer, contentResource);
		return new CharArrayReader(writer.toCharArray());
	}

	/* (non-Javadoc)
	 * @see org.sakaiproject.search.component.adapter.contenthosting.ContentDigester#getContent(org.sakaiproject.content.api.ContentResource)
	 */
	public String getContent(ContentResource contentResource)
	{
		CharArrayWriter writer = new CharArrayWriter();
		loadContent(writer, contentResource);
		return new String(writer.toCharArray());
	}
	
	
	
}
