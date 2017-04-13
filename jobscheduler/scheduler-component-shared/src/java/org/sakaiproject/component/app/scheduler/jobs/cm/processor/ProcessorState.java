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

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ProcessorState {
    public void reset();

    public void appendError(String errorStr);

    public Map<String, Object> getConfiguration();

    public void setConfiguration(Map<String, ?> configuration);

    public int getDeleteCnt();

    public void setDeleteCnt(int cnt);

    public Date getEndDate();

    public void setEndDate(Date end);

    public int getErrorCnt();

    public void setErrorCnt(int cnt);

    public List<String> getErrorList();

    public void clearErrorList();

    public int getIgnoreCnt();

    public void setIgnoreCnt(int cnt);

    public int getInsertCnt();

    public void setInsertCnt(int cnt);

    public int getProcessedCnt();

    public void setProcessedCnt(int cnt);

    public int getRecordCnt();

    public void setRecordCnt(int cnt);

    public Date getStartDate();

    public void setStartDate(Date start);

    public int getUpdateCnt();

    public void setUpdateCnt(int cnt);

    public void incrementDeleteCnt();

    public void incrementErrorCnt();

    public void incrementIgnoreCnt();

    public void incrementInsertCnt();

    public void incrementProcessedCnt();

    public void incrementRecordCnt();

    public void incrementUpdateCnt();
}
