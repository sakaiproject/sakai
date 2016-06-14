package org.sakaiproject.gradebookng.business.helpers;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.sakaiproject.gradebookng.business.exception.GbImportExportException;
import org.sakaiproject.gradebookng.business.model.ImportColumn;

import lombok.extern.slf4j.Slf4j;


/**
 * Created by chmaurer on 1/21/15.
 * 
 * TODO collapse this into ImportGradesHelper, it doesnt need to be separate and there is overlap from that class back into this one
 */
@Slf4j
public class BaseImportHelper {
	
	// patterns for detecting column headers and their types
	final static Pattern ASSIGNMENT_WITH_POINTS_PATTERN = Pattern.compile("([\\w ]+ \\[\\d+\\])");
	final static Pattern ASSIGNMENT_COMMENT_PATTERN = Pattern.compile("(\\* [\\w ]+)");
	final static Pattern STANDARD_HEADER_PATTERN = Pattern.compile("([\\w ]+)");
	final static Pattern POINTS_PATTERN = Pattern.compile("(\\d+)(?=]$)");


	/**
	 * Takes a row of String[] data to determine the position of the columns so that we can correctly parse any arbitrary delimited file.
	 * This is required because when we iterate over the rest of the lines, we need to know what the column header is, so we can set the
	 * appropriate Object property or add into any possible additional properties (e.g. ResourceProperties), which ever is required.
	 *
	 * @param line the already split line
	 * @return
	 */
	protected static Map<Integer, ImportColumn> mapHeaderRow(final String[] line) throws GbImportExportException {
		final Map<Integer, ImportColumn> mapping = new LinkedHashMap<Integer, ImportColumn>();
		for (int i = 0; i < line.length; i++) {
			
			ImportColumn column = parseHeaderForImportColumn(trim(line[i]));
			if(column != null) {
				mapping.put(i, column);
			}
		}

		return mapping;
	}
	
	private static ImportColumn parseHeaderForImportColumn(final String headerValue) throws GbImportExportException {
		final ImportColumn importColumn = new ImportColumn();
		
		System.out.println("headerValue: " + headerValue + "%%%");
				
		//assignment with points header
		Matcher m1 = ASSIGNMENT_WITH_POINTS_PATTERN.matcher(headerValue);
		if(m1.matches()) {
			
			//extract title and score
			Matcher titleMatcher = STANDARD_HEADER_PATTERN.matcher(headerValue);
			Matcher pointsMatcher = POINTS_PATTERN.matcher(headerValue);

			if(titleMatcher.find()) {
				importColumn.setColumnTitle(trim(titleMatcher.group()));
			}
			if(pointsMatcher.find()) {
				importColumn.setPoints(pointsMatcher.group());
			}
			
			importColumn.setType(ImportColumn.TYPE_ITEM_WITH_POINTS);
			
			return importColumn;
		}
		
		Matcher m2 = ASSIGNMENT_COMMENT_PATTERN.matcher(headerValue);
		if(m2.matches()) {
			
			//extract title
			Matcher titleMatcher = STANDARD_HEADER_PATTERN.matcher(headerValue);
				
			if(titleMatcher.find()) {
				importColumn.setColumnTitle(trim(titleMatcher.group()));
			}
			importColumn.setType(ImportColumn.TYPE_ITEM_WITH_COMMENTS);
			
			return importColumn;
		}
		
		Matcher m3 = STANDARD_HEADER_PATTERN.matcher(headerValue);
		if(m3.matches()) {
		
			importColumn.setColumnTitle(headerValue);
			importColumn.setType(ImportColumn.TYPE_REGULAR);
			
			return importColumn;
		}
		
		//if we got here, couldn't parse the column header, throw an error
		throw new GbImportExportException("Invalid column header, skipping: " + headerValue);
		
	}

	/**
	 * Helper to map an Excel {@link Row} to a String[] so we can use the same methods to process it as the CSV
	 * 
	 * @param row
	 * @return
	 */
	protected static String[] convertRow(final Row row) {

		final int numCells = row.getPhysicalNumberOfCells();
		final String[] s = new String[numCells];

		int i = 0;
		for (final Cell cell : row) {
			// force cell to String
			cell.setCellType(Cell.CELL_TYPE_STRING);
			s[i] = trim(cell.getStringCellValue());
			i++;
		}

		return s;
	}

	/**
	 * Helper to trim a string to null
	 * 
	 * @param s
	 * @return
	 */
	protected static String trim(final String s) {
		return StringUtils.trimToNull(s);
	}
}
