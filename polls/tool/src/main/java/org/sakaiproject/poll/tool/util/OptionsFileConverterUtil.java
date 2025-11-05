/**
 * Copyright (c) 2003-2019 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**********************************************************************************
 Copyright (c) 2019 Apereo Foundation

 Licensed under the Educational Community License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

            http://opensource.org/licenses/ecl2

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **********************************************************************************/

package org.sakaiproject.poll.tool.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.util.HtmlUtils;

// This method relies on Apache POI to process an Excel file
// Also processes any file and tries to convert it as text if possible.
@Slf4j
public class OptionsFileConverterUtil {

    public static List<String> convertInputStreamToOptionList(InputStream in) throws IOException {
        List<String> optionsList = new ArrayList<String>();
        try (BufferedInputStream bufferedInputStream = new BufferedInputStream(in)) {
            Iterator<Row> iterator;
            switch(FileMagic.valueOf(bufferedInputStream)) {
                case OOXML:
                    log.debug("Input file detected as OOXML.");
                    XSSFWorkbook workbook = new XSSFWorkbook(bufferedInputStream);
                    XSSFSheet datatypeSheet = workbook.getSheetAt(0);
                    iterator = datatypeSheet.iterator();
                    break;
                case OLE2:
                    log.debug("Input file detected as OLE2.");
                    HSSFWorkbook lagacyWorkbook = new HSSFWorkbook(bufferedInputStream);
                    HSSFSheet legacyDatatypeSheet = lagacyWorkbook.getSheetAt(0);
                    iterator = legacyDatatypeSheet.iterator();
                    break;
                default:
                    log.debug("Input file detected as UNKNOWN, try to open it as text and ignore if it's not ASCII text.");
                    try(Scanner scanner = new Scanner(bufferedInputStream).useDelimiter("\\r\\n")) {
                        while(scanner.hasNext()){
                            String inputString = HtmlUtils.htmlEscape(scanner.next(), "UTF-8");
                            if(StringUtils.isNotBlank(inputString)){
                                optionsList.add(inputString);
                            }
                        }
                    } catch(Exception ex){
                        throw new IOException("Error processing the file as text type.", ex);
                    }
                    return optionsList;
            }

            while (iterator.hasNext()) {

                Row currentRow = iterator.next();
                Iterator<Cell> cellIterator = currentRow.iterator();
                if(cellIterator.hasNext()) {
                    Cell currentCell = cellIterator.next();
                    switch(currentCell.getCellType()) {
                        case STRING:
                            if (StringUtils.isNotBlank(currentCell.getStringCellValue())) {
                                optionsList.add(HtmlUtils.htmlEscape(currentCell.getStringCellValue(), "UTF-8"));
                            }
                            break;
                        case NUMERIC:
                             optionsList.add(String.valueOf(currentCell.getNumericCellValue()));
                             break;
                        case BOOLEAN:
                            optionsList.add(currentCell.getBooleanCellValue() ? "1" : "0");
                            break;
                        case FORMULA:
                        case BLANK:
                        case _NONE:
                        case ERROR:
                        default:
                            break;
                    }
                }
            }
        } catch (Exception e) {
            throw new IOException("Error converting the file to options list.");
        }

        return optionsList;
    }
}

