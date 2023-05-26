package org.sakaiproject.tool.assessment.ui.servlet.event;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.ui.servlet.SamigoBaseServlet;
import org.sakaiproject.util.api.FormattedText;

import com.opencsv.CSVWriter;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportEventLogServlet extends SamigoBaseServlet {


    private static final DateTimeFormatter EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(SamigoConstants.EVENT_LOG_BUNDLE);

    private UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);
    private FormattedText formattedText = ComponentManager.get(FormattedText.class);

    public static final String PARAM_SITE_ID = "siteId";
    public static final String PARAM_ASSESSMENT_ID = "assessmentId";


    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // Required
        String siteId = StringUtils.trimToNull(req.getParameter(PARAM_SITE_ID));
        // Optional
        String assessmentId = req.getParameter(PARAM_ASSESSMENT_ID);

        // Check if siteId is present
        if (siteId == null) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required param siteId is not present.");
            return;
        }

        // Check permissions
        if (!canExportEventLog(siteId)) {
            Optional<String> optUserId = getUserId();
            if (optUserId.isPresent()) {
                log.warn("User with id [{}] is not allowed to export event log for site [{}]", optUserId.get(), siteId);
            } else {
                log.warn("Unauthenticated user tried to export event log for site [{}]", siteId);
            }

            res.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // Check if assessmentId is parsable, if there is one
        if ((assessmentId != null && !NumberUtils.isParsable(assessmentId))) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Value of optional param assessmentId is invalid.");
            return;
        }

        // Set headers
        String filename = StringUtils.replace(RESOURCE_BUNDLE.getString("log"), " ", "_");
        res.setContentType("text/csv");
        res.setHeader("Content-Disposition", "\"attachment;filename=\"" + filename + ".csv\";");

        // Get data
        EventLogService eventLogService = new EventLogService();
        List<EventLogData> eventLogDataList;
        if (assessmentId != null) {
            eventLogDataList = eventLogService.getEventLogData(siteId, Long.valueOf(assessmentId));
        } else {
            eventLogDataList = eventLogService.getEventLogDataBySiteId(siteId);
        }

        // Prepare data for csv
        List<String[]> lines = new LinkedList<>() {{
            // Add headers
            add(new String[] {
                RESOURCE_BUNDLE.getString("title"),
                RESOURCE_BUNDLE.getString("id"),
                RESOURCE_BUNDLE.getString("user_id"),
                RESOURCE_BUNDLE.getString("date_startd"),
                RESOURCE_BUNDLE.getString("date_submitted"),
                RESOURCE_BUNDLE.getString("duration")
            });
        }};

        for (EventLogData eventLogData : eventLogDataList) {
            Integer minutes = eventLogData.getEclipseTime();

            lines.add(new String[] {
                eventLogData.getTitle(),
                eventLogData.getAssessmentIdStr(),
                eventLogData.getUserDisplay(),
                csvDateFormat(eventLogData.getStartDate()),
                csvDateFormat(eventLogData.getEndDate()),
                minutes != null ? minutes.toString() : null,
                StringUtils.trimToEmpty(eventLogData.getErrorMsg())
            });
        }

        // Write csv to response
        PrintWriter out = res.getWriter();
        writeCsv(lines, out);
        out.flush();
        out.close();
    }

    private void writeCsv(List<String[]> lines, Writer writer) {
        char csvSeperator = StringUtils.equals(formattedText.getDecimalSeparator(), ",") ? ';' : ',';

        try (CSVWriter csvWriter = new CSVWriter(writer, csvSeperator, '"', '\\', "\n")) {
            for (String[] line : lines) {
                csvWriter.writeNext(line);
            }
        } catch (Exception e) {
            log.debug("Could not write csv: {}", e.toString());
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        doGet(req, res);
    }

    public boolean canExportEventLog(String siteId) {
        return hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_CREATE, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_EDIT_OWN, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_DELETE_OWN, siteId)
                && hasPrivilege(SamigoConstants.AUTHZ_QUESTIONPOOL_COPY_OWN, siteId);
    }

    private String csvDateFormat(Date date) {
        if (date == null) {
            return "";
        }

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(),
                userTimeService.getLocalTimeZone().toZoneId());
        return zonedDateTime.format(EXPORT_DATE_TIME_FORMATTER);
    }
}
