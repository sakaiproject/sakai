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
