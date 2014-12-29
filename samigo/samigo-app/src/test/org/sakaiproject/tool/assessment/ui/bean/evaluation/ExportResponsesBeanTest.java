/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008, 2009 The Sakai Foundation
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


package org.sakaiproject.tool.assessment.ui.bean.evaluation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Workbook;
import org.sakaiproject.tool.assessment.ui.bean.evaluation.ExportResponsesBean;

/**
 * Tests for the spreadsheet export, SAK-16560
 * 
 * @author Aaron Zeckoski (azeckoski @ gmail.com)
 */
public class ExportResponsesBeanTest extends TestCase {

    public void testGetAsWorkbook() {
        ExportResponsesBean bean = new ExportResponsesBean();
        byte[] xlsData = null;
        List<List<Object>> spreadsheetData = null;
        Workbook wb = null;

        // small test
        spreadsheetData = new ArrayList<List<Object>>();
        List<Object> row1 = new ArrayList<Object>();
        row1.add("A");
        row1.add("B");
        row1.add("C");
        row1.add("D");
        spreadsheetData.add( row1 );
        addSheetHeader(spreadsheetData);

        wb = bean.getAsWorkbook(spreadsheetData);
        assertNotNull(wb);
        assertNotNull(wb.getSheet("responses"));
        xlsData = wbToBytes(wb);
        assertNotNull(xlsData);

        // medium test (100 columns x 200 rows)
        spreadsheetData = new ArrayList<List<Object>>();
        for (int i = 0; i < 200; i++) {
            List<Object> row = new ArrayList<Object>();
            for (int j = 0; j < 100; j++) {
                row.add("Item:"+i+":"+j);
            }
            spreadsheetData.add( row );
        }
        addSheetHeader(spreadsheetData);

        wb = bean.getAsWorkbook(spreadsheetData);
        assertNotNull(wb);
        assertNotNull(wb.getSheet("responses"));
        xlsData = wbToBytes(wb);
        assertNotNull(xlsData);
    }

    public void testGetAsWorkbookWide() {
        ExportResponsesBean bean = new ExportResponsesBean();
        byte[] xlsData = null;
        List<List<Object>> spreadsheetData = null;
        Workbook wb = null;

        // huge test (300 columns x 5 rows)
        spreadsheetData = new ArrayList<List<Object>>();
        for (int i = 0; i < 5; i++) {
            List<Object> row = new ArrayList<Object>();
            for (int j = 0; j < 300; j++) {
                row.add("Item:"+i+":"+j);
            }
            spreadsheetData.add( row );
        }
        addSheetHeader(spreadsheetData);

        wb = bean.getAsWorkbook(spreadsheetData);
        assertNotNull(wb);
        assertNotNull(wb.getSheet("responses"));
        xlsData = wbToBytes(wb);
        assertNotNull(xlsData);
    }

    private void addSheetHeader(List<List<Object>> spreadsheetData) {
        ArrayList<Object> header = new ArrayList<Object>();
        header.add(ExportResponsesBean.NEW_SHEET_MARKER);
        header.add("responses");
        spreadsheetData.add(0, header);
    }

    private byte[] wbToBytes(Workbook wb) {
        byte[] bytes;
        ByteArrayOutputStream out = null;
        try {
            out = new ByteArrayOutputStream();
            wb.write(out);
            out.flush();
            bytes = out.toByteArray();
        } catch (IOException e) {
            bytes = null;
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
        }
        return bytes;
    }
}
