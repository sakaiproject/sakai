package org.sakaiproject.gradebookng.business.helpers;


import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.gradebookng.business.model.ImportedGrade;
import org.sakaiproject.util.BaseResourcePropertiesEdit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chmaurer on 1/21/15.
 */
@CommonsLog
public class ImportGradesHelper extends BaseImportHelper {

    private static final String IMPORT_USER_ID="Student ID";
    private static final String IMPORT_USER_NAME="Student Name";


    /**
     * Parse a CSV into a list of ImportedGrade objects. Returns list if ok, or null if error
     * @param is InputStream of the data to parse
     * @return
     */
    public static List<ImportedGrade> parseCsv(InputStream is) {

        //manually parse method so we can support arbitrary columns
        CSVReader reader = new CSVReader(new InputStreamReader(is));
        String [] nextLine;
        int lineCount = 0;
        List<ImportedGrade> list = new ArrayList<ImportedGrade>();
        Map<Integer,String> mapping = null;

        try {
            while ((nextLine = reader.readNext()) != null) {

                if(lineCount == 0) {
                    //header row, capture it
                    mapping = mapHeaderRow(nextLine);
                } else {
                    //map the fields into the object
                    list.add(mapLine(nextLine, mapping));
                }
                lineCount++;
            }
        } catch (Exception e) {
            log.error("Error reading imported file: " + e.getClass() + " : " + e.getMessage());
            return null;
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return list;
    }

    /**
     * Parse an XLS into a list of ImportedGrade objects
     * Note that only the first sheet of the Excel file is supported.
     *
     * @param is InputStream of the data to parse
     * @return
     */
    public static List<ImportedGrade> parseXls(InputStream is) {

        int lineCount = 0;
        List<ImportedGrade> list = new ArrayList<ImportedGrade>();
        Map<Integer,String> mapping = null;

        try {
            Workbook wb = WorkbookFactory.create(is);
            Sheet sheet = wb.getSheetAt(0);
            for (Row row : sheet) {

                String[] r = convertRow(row);

                if(lineCount == 0) {
                    //header row, capture it
                    mapping = mapHeaderRow(r);
                } else {
                    //map the fields into the object
                    list.add(mapLine(r, mapping));
                }
                lineCount++;
            }

        } catch (Exception e) {
            log.error("Error reading imported file: " + e.getClass() + " : " + e.getMessage());
            return null;
        }

        return list;
    }

    /**
     * Takes a row of data and maps it into the appropriate ImportedGrade properties
     * We have a fixed list of properties, anything else goes into ResourceProperties
     * @param line
     * @param mapping
     * @return
     */
    private static ImportedGrade mapLine(String[] line, Map<Integer,String> mapping){

        ImportedGrade grade = new ImportedGrade();
        ResourceProperties p = new BaseResourcePropertiesEdit();

        for(Map.Entry<Integer,String> entry: mapping.entrySet()) {
            int i = entry.getKey();
            //trim in case some whitespace crept in
            String col = trim(entry.getValue());

            //now check each of the main properties in turn to determine which one to set, otherwise set into props
            if(StringUtils.equals(col, IMPORT_USER_ID)) {
                grade.setStudentId(trim(line[i]));
            } else if(StringUtils.equals(col, IMPORT_USER_NAME)) {
                grade.setStudentName(trim(line[i]));
//            } else if(StringUtils.equals(col, IMPORT_LAST_NAME)) {
//                u.setLastName(trim(line[i]));
//            } else if(StringUtils.equals(col, IMPORT_EMAIL)) {
//                u.setEmail(trim(line[i]));
//            } else if(StringUtils.equals(col, IMPORT_PASSWORD)) {
//                u.setPassword(trim(line[i]));
//            } else if(StringUtils.equals(col, IMPORT_TYPE)) {
//                u.setType(trim(line[i]));
            } else {

                //only add if not blank
                if(StringUtils.isNotBlank(trim(line[i]))) {
                    p.addProperty(col, trim(line[i]));
                }
            }
        }

        grade.setProperties(p);
        return grade;
    }
}
