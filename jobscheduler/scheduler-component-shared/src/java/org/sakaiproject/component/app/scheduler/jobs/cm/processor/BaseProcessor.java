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

import org.apache.commons.lang.StringUtils;

import java.util.Map;

public abstract class BaseProcessor implements DataProcessor {

    public ProcessorState init(Map<String, Object> configuration) {
        BaseProcessorState bps = new BaseProcessorState();
        bps.setConfiguration(configuration);
        return bps;
    }

    public abstract String getProcessorTitle();

    public void preProcess(ProcessorState state) throws Exception {
    }

    public void postProcess(ProcessorState state) throws Exception {
    }

    public void process(ProcessorState state) throws Exception {
    }

    public String getReport(ProcessorState state) {
        StringBuilder reportTxt = new StringBuilder();
        reportTxt.append("\n").append(getProcessorTitle()).append("\n");

        if (state == null) {
            reportTxt.append("\n\nProcessor state appears not to have created successfully. No reporting data is available.\n");

            return reportTxt.toString();
        }

        Map<String, Object> config = state.getConfiguration();
        reportTxt.append("\nConfiguration:\n");

        for (String key : config.keySet()) {
            // filter out the large temporary maps
            if (StringUtils.equals(key, "path.base") || StringUtils.endsWith(key, ".filename")) {
                reportTxt.append(String.format("%1$-105s => %2$s%n", key, config.get(key).toString()));
            }
        }

        reportTxt.append("\nResults:\n");
        reportTxt.append(String.format("%1$-20s%2$d%n", "Records", state.getRecordCnt()));
        reportTxt.append(String.format("%1$-20s%2$d%n", "Processed", state.getProcessedCnt()));
        reportTxt.append(String.format("%1$-20s%2$d%n", "Errors", state.getErrorCnt()));
        reportTxt.append(String.format("%1$-20s%2$d%n", "Inserts", state.getInsertCnt()));
        reportTxt.append(String.format("%1$-20s%2$d%n", "Updates", state.getUpdateCnt()));
        reportTxt.append(String.format("%1$-20s%2$d%n", "Unchanged", state.getIgnoreCnt()));
        reportTxt.append(String.format("%1$-20s%2$d%n", "Deletes", state.getDeleteCnt()));
        reportTxt.append(String.format("%1$-20s%2$tc%n", "Start", state.getStartDate()));
        reportTxt.append(String.format("%1$-20s%2$tc%n", "End", state.getEndDate()));

        reportTxt.append("\nMessages:\n");
        for (String error : state.getErrorList()) {
            reportTxt.append("\n* " + error);
        }

        return reportTxt.toString();
    }
}
