package org.sakaiproject.gradebookng.business.helpers;


import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

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
    protected static Map<Integer,String> mapHeaderRow(String[] line) {
        Map<Integer,String> mapping = new LinkedHashMap<Integer,String>();
        for(int i=0;i<line.length;i++){
            mapping.put(i, trim(line[i]));
        }

        return mapping;
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
