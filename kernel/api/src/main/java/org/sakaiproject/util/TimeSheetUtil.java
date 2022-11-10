/*
 * Copyright (c) 2003-2022 The Apereo Foundation
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
package org.sakaiproject.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.timesheet.api.TimeSheetEntry;

public class TimeSheetUtil {

    public String intToTime(int time) {
        String timeReturn = "";
        if(time >= 60) {
            timeReturn = timeReturn.concat((time/60)+"h").concat(" ");
            timeReturn = timeReturn.concat((time%60)+"m");
        }else if(time>0 && time<60) {
            timeReturn = time+"m";
        }
        return timeReturn;
    }

    public int timeToInt(String time) {
        int timeParseInt=0;
        if(StringUtils.isNotBlank(time)) {
            String timeSheet[] = time.split("h|H");
            if(timeSheet.length > 1) {
                timeParseInt = timeParseInt + Integer.parseInt(timeSheet[0].trim())*60;
                timeParseInt = timeParseInt + Integer.parseInt(timeSheet[1].split("m|M")[0].trim());
            }else {
                if(timeSheet[0].contains("m") || timeSheet[0].contains("M")) {
                    timeParseInt = timeParseInt + Integer.parseInt(timeSheet[0].split("m|M")[0].trim());
                }else {
                    timeParseInt = timeParseInt + Integer.parseInt(timeSheet[0].trim())*60;
                }
            }
        }
        return timeParseInt;
    }

    public String getTotalTimeSheet(List<TimeSheetEntry> ats) {
        int totalTime = 0;
        for (TimeSheetEntry timeSheet : ats) {
            totalTime = totalTime + timeToInt(timeSheet.getDuration());
        }

        return intToTime(totalTime);
    }
}
