package org.sakaiproject.gradebookng.business.helpers;


import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.sakaiproject.gradebookng.business.model.ImportColumn;

import java.text.MessageFormat;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by chmaurer on 1/21/15.
 */
public class BaseImportHelper {

    /**
     * Takes a row of String[] data to determine the position of the columns so that we can
     * correctly parse any arbitrary delimited file. This is required because when we iterate over the rest of the lines,
     * we need to know what the column header is, so we can set the appropriate Object property
     * or add into any possible additional properties (e.g. ResourceProperties), which ever is required.
     *
     * @param line	the already split line
     * @return
     */
    protected static Map<Integer,ImportColumn> mapHeaderRow(String[] line) {
        Map<Integer,ImportColumn> mapping = new LinkedHashMap<Integer,ImportColumn>();
        for(int i=0;i<line.length;i++){
            mapping.put(i, parseHeaderForImportColumn(trim(line[i])));
        }

        return mapping;
    }

    private static ImportColumn parseHeaderForImportColumn(String headerValue) {
        ImportColumn importColumn = new ImportColumn();
        MessageFormat mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_PATTERN);
        Object[] parsedObject;
        try {
            parsedObject = mf.parse(headerValue);
            importColumn.setColumnTitle((String)parsedObject[0]);
            importColumn.setType(ImportColumn.TYPE_ITEM_WITH_POINTS);
            importColumn.setPoints((String)parsedObject[1]);
        } catch (ParseException e) {
            mf = new MessageFormat(ImportGradesHelper.ASSIGNMENT_HEADER_COMMENT_PATTERN);
            try {
                parsedObject = mf.parse(headerValue);
                importColumn.setColumnTitle((String)parsedObject[0]);
                importColumn.setType(ImportColumn.TYPE_ITEM_WITH_COMMENTS);
            } catch (ParseException e1) {
                mf = new MessageFormat(ImportGradesHelper.HEADER_STANDARD_PATTERN);
                try {
                    parsedObject = mf.parse(headerValue);
                    importColumn.setColumnTitle((String)parsedObject[0]);
                    importColumn.setType(ImportColumn.TYPE_REGULAR);
                } catch (ParseException e2) {
                    throw new RuntimeException("Error parsing grade import", e2);
                }
            }
        }

        return importColumn;
    }

    /**
     * Takes a header Row from an Excel file and maps it the same as the CSV mapper does.
     * Only handles string typed cells.
     * @param row Row from an Excel document
     * @return
     */
	/*
	protected static Map<Integer,String> mapHeaderRow(Row row) {
		Map<Integer,String> mapping = new LinkedHashMap<Integer,String>();
		int i = 0;
		for (Cell cell : row) {
			if(Cell.CELL_TYPE_STRING == cell.getCellType()) {
				mapping.put(i, trim(cell.getStringCellValue()));
			}
			i++;
		}
		return mapping;
	}
	*/

    /**
     * Helper to map an Excel Row to a string[] so we can use the same methods to process it as the CSV
     * @param row
     * @return
     */
    protected static String[] convertRow(Row row) {

        int numCells = row.getPhysicalNumberOfCells();
        String[] s = new String[numCells];

        int i = 0;
        for(Cell cell: row) {
            //force cell to String
            cell.setCellType(Cell.CELL_TYPE_STRING);
            s[i] = trim(cell.getStringCellValue());
            i++;
        }

        return s;
    }

    /**
     * Helper to trim a string to null
     * @param s
     * @return
     */
    protected static String trim(String s){
        return StringUtils.trimToNull(s);
    }
}
