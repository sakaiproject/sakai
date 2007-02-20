/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California, The MIT Corporation
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.io.OutputStream;
import java.io.IOException;
import java.io.Serializable;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * Backing bean to export gradebook data. Currently we support two export
 * formats (CSV or Excel)
 *
 *
 * NOTE: CSV export capabilities are extremely limited! UTF-16 text (such as
 * Chinese) is not supported correctly, for example. Use Excel-formatted output if at all
 * possible.
 *
 * Author:Louis Majanja <louis@media.berkeley.edu>
 * Date: Feb 12, 2007
 * Time: 3:22:21 PM
 */
public class DataExportBean implements Serializable {

    private static final Log logger = LogFactory.getLog(DataExportBean.class);
    /**
     * parser a List of strings
     * into a string
     *
     * @param formattedData
     * @return   String
     */
    private String listToCSVConverter(List formattedData){
        StringBuffer sb = new StringBuffer();
        String csvSep = ",";
        Iterator colIterator = formattedData.iterator();
        while(colIterator.hasNext()){
            List row = (ArrayList) colIterator.next();
            Iterator rowiterator = row.iterator();
            int count = 0;
            while(rowiterator.hasNext()){
                String colItem = (String) rowiterator.next();
                if(count > 0)sb.append(csvSep);
                appendQuoted(sb,colItem);
                count++;
            }
            sb.append("\n");
            if(logger.isDebugEnabled())  logger.debug(sb.toString());
        }
        if(logger.isDebugEnabled())logger.debug(sb.toString());
        return sb.toString();
    }

    /**
     * parses a list of of comma delimited String into
     * an Excel workbook
     *
     * @param formattedData
     * @param gradebookName
     * @return   Excel Workbook
     */
    private HSSFWorkbook listToExcelConverter(List formattedData,String gradebookName){
        HSSFWorkbook wb = new HSSFWorkbook();
        // Excel can not handle sheet names with > 31 characters, and can't handle /\*?[] characters
        String safeGradebookName = StringUtils.left(gradebookName.replaceAll("\\W", "_"), 30);

        HSSFSheet sheet = wb.createSheet(safeGradebookName);
        // iterate through the rows
        Iterator it = formattedData.iterator();
        int rowcount = 0;
        while(it.hasNext()){
            HSSFRow spreadsheetRow = sheet.createRow((short)rowcount);
            //each rows consists of an an array
            List rowcontent = (ArrayList) it.next();
            Iterator iter =  rowcontent.iterator();
            int count = 0;
            while(iter.hasNext()){
                spreadsheetRow.createCell((short)count).setCellValue((String)iter.next());
                count++;
            }
            rowcount++;
        }
        return wb;
    }

    /**
     * Exports data via http as a csv document
     *
     * @param formattedData
     * @param fileName
     */
    public void writeAsCsv(List formattedData, String fileName) {
        String csvString = listToCSVConverter(formattedData);
        FacesContext faces = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
        protectAgainstInstantDeletion(response);
        response.setContentType("text/comma-separated-values");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".csv");
        response.setContentLength(csvString.length());
        OutputStream out = null;
        try {
            out = response.getOutputStream();
            out.write(csvString.getBytes());
            out.flush();
        } catch (IOException e) {
            if(logger.isErrorEnabled())logger.error(e);
            e.printStackTrace();
        } finally {
            try {
                if (out != null) out.close();
            } catch (IOException e) {
                if(logger.isErrorEnabled())logger.error(e);
                e.printStackTrace();
            }
        }
        faces.responseComplete();
    }

    /**
     * Try to head off a problem with downloading files from a secure HTTPS
     * connection to Internet Explorer.
     *
     * When IE sees it's talking to a secure server, it decides to treat all hints
     * or instructions about caching as strictly as possible. Immediately upon
     * finishing the download, it throws the data away.
     *
     * Unfortunately, the way IE sends a downloaded file on to a helper
     * application is to use the cached copy. Having just deleted the file,
     * it naturally isn't able to find it in the cache. Whereupon it delivers
     * a very misleading error message like:
     * "Internet Explorer cannot download roster from sakai.yoursite.edu.
     * Internet Explorer was not able to open this Internet site. The requested
     * site is either unavailable or cannot be found. Please try again later."
     *
     * There are several ways to turn caching off, and so to be safe we use
     * several ways to turn it back on again.
     *
     * This current workaround should let IE users save the files to disk.
     * Unfortunately, errors may still occur if a user attempts to open the
     * file directly in a helper application from a secure web server.
     *
     * TODO Keep checking on the status of this.
     */
    public static void protectAgainstInstantDeletion(HttpServletResponse response) {
        response.reset();	// Eliminate the added-on stuff
        response.setHeader("Pragma", "public");	// Override old-style cache control
        response.setHeader("Cache-Control", "public, must-revalidate, post-check=0, pre-check=0, max-age=0");	// New-style
    }


    /**
     * Exports data via http as an Excel document
     *
     * @param formattedData
     * @param fileName
     */
    public void writeAsExcel(List formattedData, String fileName) {

            HSSFWorkbook wb = listToExcelConverter(formattedData,fileName);
            FacesContext faces = FacesContext.getCurrentInstance();
            HttpServletResponse response = (HttpServletResponse)faces.getExternalContext().getResponse();
            protectAgainstInstantDeletion(response);
            response.setContentType("application/vnd.ms-excel ");
            response.setHeader("Content-disposition", "attachment; filename=" + fileName + ".xls");

            OutputStream out = null;
            try {
                out = response.getOutputStream();
                // For some reason, you can't write the byte[] as in the csv export.
                // You need to write directly to the output stream from the workbook.
                wb.write(out);
                out.flush();
            } catch (IOException e) {
                if(logger.isErrorEnabled())logger.error(e);
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                } catch (IOException e) {
                    if(logger.isErrorEnabled())logger.error(e);
                    e.printStackTrace();
                }
            }
        faces.responseComplete();
    }


    private StringBuffer appendQuoted(StringBuffer sb, String toQuote) {
        if ((toQuote.indexOf(',') >= 0) || (toQuote.indexOf('"') >= 0)) {
            String out = toQuote.replaceAll("\"", "\"\"");
            if(logger.isDebugEnabled()) logger.debug("Turning '" + toQuote + "' to '" + out + "'");
            sb.append("\"").append(out).append("\"");
        } else {
            sb.append(toQuote);
        }
        return sb;
    }



}



