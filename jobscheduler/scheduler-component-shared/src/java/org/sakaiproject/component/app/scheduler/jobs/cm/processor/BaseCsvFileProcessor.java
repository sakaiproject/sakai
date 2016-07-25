/**
 * Copyright (c) 2003 The Apereo Foundation
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
package org.sakaiproject.component.app.scheduler.jobs.cm.processor;

import au.com.bytecode.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;

@Slf4j
public abstract class BaseCsvFileProcessor extends BaseSeparatedFileProcessor implements FileProcessor {

    public void processFormattedFile(BufferedReader fr, FileProcessorState state) throws Exception {
        CSVReader csvr = new CSVReader(fr);
        String[] line = null;

        while (((line = csvr.readNext()) != null)) {
            state.setRecordCnt(state.getRecordCnt() + 1);

            boolean headerPresent = state.isHeaderRowPresent();

            if (state.getColumns() != line.length) {
                state.appendError("Wrong Number Columns Row:, " + state.getRecordCnt() + "Saw:" + line.length + ", Expecting: " + state.getColumns());
                state.setErrorCnt(state.getErrorCnt() + 1);
            } else if ((headerPresent && state.getRecordCnt() > 1) || !headerPresent) {
                try {
                    line = trimLine(line);
                    processRow(line, state);
                    state.setProcessedCnt(state.getProcessedCnt() + 1);
                } catch (Exception e) {
                    log.debug(e.getMessage(), e);
                    state.appendError("Row " + state.getRecordCnt() + " " + e.getMessage());
                    state.setErrorCnt(state.getErrorCnt() + 1);
                }
            }
        }
        fr.close();
    }

    protected String[] trimLine(String[] line) {
        String trim = "";
        for (int i = 0; i < line.length; i++) {
            trim = line[i].trim();
            line[i] = trim;
        }

        return line;
    }
}
