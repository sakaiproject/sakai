package org.sakaiproject.timesheet.impl;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.timesheet.api.TimeSheetEntry;
import org.sakaiproject.timesheet.api.TimeSheetService;
import org.sakaiproject.timesheet.api.repository.TimeSheetRepository;
import org.sakaiproject.tool.api.SessionManager;
import org.sakaiproject.util.TimeSheetUtil;
import org.springframework.transaction.annotation.Transactional;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeSheetServiceImpl implements TimeSheetService {

    private static final String SAK_PROP_TIMESHEET_TIME_PATTERN_DEFAULT = "^([0-9]?[0-9]h|[0-9]?[0-9]H)$|([0-9]?[0-9]m|[0-9]?[0-9]M)$|(([0-9]?[0-9]h|[0-9]?[0-9]H)[ ]?([0-9]?[0-9]m|[0-9]?[0-9]M))$";
    private static final String SAK_PROP_TIMESHEET_SITES_ALLOWED = "timesheet.sites.allowed";

    @Setter private ServerConfigurationService serverConfigurationService;
    @Setter private SessionManager sessionManager;
    @Setter private TimeSheetRepository timeSheetRepository;
    @Setter private TimeSheetUtil timeSheetUtil;
    private Pattern timeSheetTimePattern;

    public void init() {
        timeSheetTimePattern = Pattern.compile(serverConfigurationService.getString("timesheet.timePattern", SAK_PROP_TIMESHEET_TIME_PATTERN_DEFAULT));
    }

    @Override
    public boolean isTimeSheetEnabled(String siteId) {
        List<String> timesheetConfig = serverConfigurationService.getStringList(SAK_PROP_TIMESHEET_SITES_ALLOWED, Collections.singletonList("none"));

        // TODO logic for determining whether this feature is enabled in the current site
        return Stream.of("all", siteId).filter(Objects::nonNull).anyMatch(timesheetConfig::contains);
    }

    @Override
    @Transactional
    public void create(TimeSheetEntry timeSheetEntry) {
        timeSheetRepository.save(timeSheetEntry);
        log.debug("Created new TimeSheetEntry with id {}", timeSheetEntry.getId());
    }

    @Override
    @Transactional
    public void delete(Long timeSheetId) {
        timeSheetRepository.deleteById(timeSheetId);
        log.debug("Deleted TimeSheetEntry with id {}", timeSheetId);
    }

    @Override
    public boolean existsAny(String reference) {
        return timeSheetRepository.findByReference(reference).isPresent();
    }

    @Override
    public boolean isValidTimeSheetTime(String time) {
        return StringUtils.isNotBlank(time) && timeSheetTimePattern.matcher(time).matches();
    }

    @Override
    public String getTimeSheetEntryReference(Long timeSheetId) {
        if (timeSheetId != null) {
            Optional<TimeSheetEntry> tse = timeSheetRepository.findById(timeSheetId);
            return tse.isPresent() ? tse.get().getReference() : null;
        }
        return null;
    }

    public List<TimeSheetEntry> getByReference(String reference) {
        Optional<List<TimeSheetEntry>> entries = timeSheetRepository.findByReference(reference);
        log.debug("Finding TimeSheetEntry with reference {}", reference);
        return entries.isPresent() ? entries.get() : null;
    }

    public List<TimeSheetEntry> getAllByUserIdAndReference(String userId, String reference) {
        Optional<List<TimeSheetEntry>> entries = timeSheetRepository.findAllByUserIdAndReference(userId, reference);
        log.debug("Finding TimeSheetEntry for user with id {} and reference {}", userId, reference);
        return entries.isPresent() ? entries.get() : null;
    }

    public Integer timeToInt(String time) {
        return timeSheetUtil.timeToInt(time);
    }

    public String intToTime(int time) {
        return timeSheetUtil.intToTime(time);
    }

    @Override
    public String getTotalTimeSheet(String reference) {
        Optional<List<TimeSheetEntry>> entries = timeSheetRepository.findByReference(reference);
        if (entries.isPresent()) {
            return timeSheetUtil.getTotalTimeSheet(entries.get());
        }
        return null;
    }
}
