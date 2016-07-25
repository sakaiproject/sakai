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

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseProcessorState implements ProcessorState
{
    @Getter
    private Map<String, Object> configuration = new HashMap<>();

    @Getter @Setter
    private Date
        /** Start Date */
        startDate,
        /** End Date */
        endDate;

    @Getter @Setter
    private int
        /** Record Cnt */
        recordCnt = 0,
        /** Error Cnt */
        errorCnt = 0,
        /** Update Cnt */
        updateCnt = 0,
        /** Insert Cnt */
        insertCnt = 0,
        /** Insert Cnt */
        ignoreCnt = 0,
        /** Delete Cnt */
        deleteCnt = 0,
        /** Processed Cnt */
        processedCnt = 0;

    @Getter @Setter
    private List<String> errorList = new ArrayList<String>();

    public void reset()
    {
        startDate = null;
        endDate = null;
        recordCnt = 0;
        errorCnt = 0;
        updateCnt = 0;
        insertCnt = 0;
        ignoreCnt = 0;
        deleteCnt = 0;
        processedCnt = 0;

        errorList = new ArrayList<String>();

        configuration = new HashMap();
    }

    public void clearErrorList()
    {
        errorList.clear();
    }

    public void incrementErrorCnt() {
        this.errorCnt++;
    }

    public void incrementProcessedCnt() {
        this.processedCnt++;
    }

    public void incrementRecordCnt() {
        this.recordCnt++;
    }

    public void incrementIgnoreCnt() {
        this.ignoreCnt++;
    }

    public void incrementInsertCnt() {
        this.insertCnt++;
    }

    public void incrementUpdateCnt() {
        this.updateCnt++;
    }

    public void incrementDeleteCnt() {
        this.deleteCnt++;
    }

    public void appendError(String txt) {
        errorList.add(txt);
    }

    public void setConfiguration (Map<String, ?> c)
    {
        configuration.clear();

        if (c != null)
        {
            configuration.putAll(c);
        }
    }
}
