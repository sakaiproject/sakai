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

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.util.StringTokenizer;

@Slf4j
public abstract class BaseSeparatedFileProcessor extends BaseFileProcessor {
    @Setter
    private String token = "|";

    public void processFormattedFile(BufferedReader fr, FileProcessorState state) throws Exception {
        String temp = fr.readLine();
        String[] line = null;

        while (temp != null) {
            state.setRecordCnt(state.getRecordCnt() + 1);

            StringTokenizer tok = new StringTokenizer(temp, token, false);

            if (state.getColumns() != tok.countTokens()) {
                state.appendError("Wrong Number Columns Row " + state.getRecordCnt() + ", Saw " + state.getColumns() + ", Expected: " + temp.length());
                state.setErrorCnt(state.getErrorCnt() + 1);
            } else {
                line = new String[tok.countTokens()];

                for (int i = 0; i < line.length; i++) {
                    line[i] = tok.nextToken();
                }

                boolean headerPresent = state.isHeaderRowPresent();

                if ((headerPresent && state.getRecordCnt() > 1) || !headerPresent) {
                    try {
                        processRow(line, state);
                        state.setProcessedCnt(state.getProcessedCnt() + 1);
                    } catch (Exception err) {
                        log.error(err.getMessage(), err);
                        state.appendError("Row " + state.getRecordCnt() + " " + err.getMessage());
                        state.setErrorCnt(state.getErrorCnt() + 1);
                    }
                }
            }

            temp = fr.readLine();
        }

        fr.close();
    }
}
