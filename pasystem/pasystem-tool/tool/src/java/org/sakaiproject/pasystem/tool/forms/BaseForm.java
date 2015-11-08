/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.tool.forms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import lombok.Data;

/**
 * Basic form validation.
 */
@Data
class BaseForm {

    protected String uuid;
    protected long startTime;
    protected long endTime;

    protected static long parseTime(String timeString) {
        if (timeString == null || "".equals(timeString)) {
            return 0;
        }

        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX").parse(timeString).getTime();
        } catch (ParseException e) {
            return -1;
        }
    }

    public boolean hasValidStartTime() {
        return startTime >= 0;
    }

    public boolean hasValidEndTime() {
        return endTime >= 0;
    }
}

