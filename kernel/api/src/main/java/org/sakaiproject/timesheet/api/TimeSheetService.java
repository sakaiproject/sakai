package org.sakaiproject.timesheet.api;

import java.util.List;

public interface TimeSheetService {

    boolean isTimeSheetEnabled(String siteId);

    public boolean isValidTimeSheetTime(String timeSheet);

    public String getTimeSheetEntryReference(Long timeSheetId);

    public List<TimeSheetEntry> getByReference(String reference);

    public List<TimeSheetEntry> getAllByUserIdAndReference(String userId, String reference);

    public void create(TimeSheetEntry timeSheet);

    public void delete(Long timeSheetId);

    public String getTotalTimeSheet(String reference);

    public Integer timeToInt(String time);

    public String intToTime(int time);

    public boolean existsAny(String reference);

}
