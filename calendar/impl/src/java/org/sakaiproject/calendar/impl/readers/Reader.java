/**********************************************************************************
* $URL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2003, 2004 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
* 
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
* 
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.calendar.impl.readers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sakaiproject.exception.ImportException;
import org.sakaiproject.service.legacy.time.TimeService;

/**
 * This class provides common functionality for parsers of various
 * calendar import formats.
 */
public abstract class Reader
{
	protected Map columnHeaderMap;
	protected TimeService timeService;

	/**
	 * Contains header information such as the text label used for the
	 * header and the calendar event property with which it is associated.
	 **/
	public class ColumnHeader
	{
		private String columnProperty;
		private String columnHeader;

		/**
		 * Default constructor 
		 */
		public ColumnHeader()
		{
			super();
		}

		/**
		 * Construct a ColumnHeader with a specified text label used in the import
		 * file and the Calendar Event property that is associated with it.
		 * @param columnHeader
		 * @param columnProperty
		 */
		public ColumnHeader(String columnHeader, String columnProperty)
		{
			this.columnHeader = columnHeader;
			this.columnProperty = columnProperty;
		}

		/**
		 * Gets the column header as it appears in the import file.
		 */
		public String getColumnHeader()
		{
			return columnHeader;
		}

		/**
		 * Gets the calendar event property name associated with this header.
		 */
		public String getColumnProperty()
		{
			return columnProperty;
		}

	}

	/**
	 * Default Constructor
	 */
	public Reader()
	{
		super();

		// Use whatever the default column mapping.
		this.setColumnHeaderToAtributeMapping(this.getDefaultColumnMap());
	}
	
	/**
	 * This class contains the information for a single cell in a given row/column.
	 */
	static public class ReaderImportCell
	{
		private String columnHeader;
		private String value;
		private int lineNumber;
		private int column;
		private String propertyName;

		/**
		 * @param row
		 * @param column
		 * @param value
		 * @param propertyName
		 * @param columnHeader
		 */
		ReaderImportCell(
			int row,
			int column,
			String value,
			String propertyName,
			String columnHeader)
		{
			super();
			this.lineNumber = row;
			this.column = column;
			this.value = value;
			this.propertyName = propertyName;
			this.columnHeader = columnHeader;
		}

		/**
		 * Gets the calendar event property name associated with this header.
		 */
		public String getPropertyName()
		{
			return propertyName;
		}

		/**
		 * Gets the zero-based column number.
		 */
		public int getColumnNumber()
		{
			return column;
		}

		/**
		 * Gets the one-based row number.
		 */
		public int getLineNumber()
		{
			return lineNumber;
		}

		/**
		 * Gets the value of the cell as a string.  No type conversion is performed,
		 * this is just the string read in from the stream.
		 */
		public String getCellValue()
		{
			return value;
		}

		/**
		 * Gets the text header used in the import file for this column.
		 */
		public String getColumnHeader()
		{
			return columnHeader;
		}

	}
	
	/**
	 * Users of this class need to define a callback that will be handled for
	 * each row.
	 */
	public interface ReaderImportRowHandler
	{
		/**
		 * This is the callback that is called for each row.
		 * @param columnIterator Iterator for a collection of CSVReaderImportCell for this row.
		 */
		void handleRow(Iterator columnIterator) throws ImportException;
	}
	
	/**
	 * Create meta-information from the first line of the "file" (actually stream)
	 * that contains the names of the columns.
	 * @param columns
	 */
	protected ColumnHeader[] buildColumnDescriptionArray(String[] columns)
	
	{
		ColumnHeader[] columnDescriptionArray;
		columnDescriptionArray = new ColumnHeader[columns.length];

		for (int i = 0; i < columns.length; i++)
		{
			columnDescriptionArray[i] =
				new ColumnHeader(
					columns[i],
					(String) columnHeaderMap.get(columns[i]));
		}
		return columnDescriptionArray;
	}
	
	/**
	 * Remove leading/trailing quotes
	 * @param columnsReadFromFile
	 */
	protected void trimLeadingTrailingQuotes(String[] columnsReadFromFile)
	{
		for (int i = 0; i < columnsReadFromFile.length; i++)
		{
			String regex2 = "(?:\")*([^\"]+)(?:\")*";
			columnsReadFromFile[i] =
				columnsReadFromFile[i].trim().replaceAll(regex2, "$1");
		}
	}
	
	/**
	 * Users of this class must define a map where the keys are the column headers
	 * that will appear in the first line of the CSV file (stream) and the values
	 * are the associated property names that the callback will receive.
	 * @param columnList
	 */
	public void setColumnHeaderToAtributeMapping(Map columnHeaderMap)
	{
		this.columnHeaderMap = columnHeaderMap;
	}
	
	/**
	 * Split a line into a list of CSVReaderImportCell objects.
	 * @param columnDescriptionArray
	 * @param lineNumber
	 * @param columns
	 */
	protected Iterator processLine(
		ColumnHeader[] columnDescriptionArray,
		int lineNumber,
		String[] columns)
	{
		List list = new ArrayList();

		for (int i = 0; i < columns.length; i++)
		{
			if ( i >= columnDescriptionArray.length )
			{
				continue;
			}
			else
			{
				list.add(
					new ReaderImportCell(
						lineNumber,
						i,
						columns[i],
						columnDescriptionArray[i].getColumnProperty(),
						columnDescriptionArray[i].getColumnHeader()));
			}
		}

		return list.iterator();
	}
	/**
	 * Utility routine to get a BufferedReader
	 * @param stream
	 */
	protected BufferedReader getReader(InputStream stream)
	{
		InputStreamReader inStreamReader = new InputStreamReader(stream);
		BufferedReader bufferedReader = new BufferedReader(inStreamReader);
		return bufferedReader;
	}

	/**
	 * Import a CSV file from a stream and callback on each row.
	 * @param stream Stream of CSV (or other delimited data)
	 * @param handler Callback for each row.
	 */
	abstract public void importStreamFromDelimitedFile(
		InputStream stream,
		ReaderImportRowHandler handler)
		throws ImportException;
		
	/**
	 * Gets the mapping of text column header labels in the import file to
	 * calendar event properties.
	 */
	public Map getColumnHeaderMap()
	
	{
		return columnHeaderMap;
	}

	/**
	 * Each derived class must implement this filter to convert the properties as set by the
	 * reader into a common set of properties that will be used to create calendar events.
	 * Notably, this filter must create a ScheduleImporterService.ACTUAL_TIMERANGE property
	 * that will define the actual start time/date of the event.
	 * @param importStream
	 * @param customFieldNames
	 * @throws ImportException
	 */
	abstract public List filterEvents(List events, String[] customFieldNames) throws ImportException;

	/**
	 * Derived classes must provide a default mapping of text column header labels in the import file to
	 * calendar event properties.
	 * @throws ImportException
	 */
	abstract public Map getDefaultColumnMap();
	
	/**
	 */
	public TimeService getTimeService()
	{
		return timeService;
	}

	/**
	 * @param service
	 */
	public void setTimeService(TimeService service)
	{
		timeService = service;
	}

}
