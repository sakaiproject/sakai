package org.sakaiproject.tool.assessment.ui.servlet.event;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.sakaiproject.component.api.ServerConfigurationService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.samigo.util.SamigoConstants;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.tool.assessment.data.dao.assessment.EventLogData;
import org.sakaiproject.tool.assessment.services.assessment.EventLogService;
import org.sakaiproject.tool.assessment.ui.servlet.SamigoBaseServlet;
import org.sakaiproject.util.api.FormattedText;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.sakaiproject.util.ResourceLoader;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExportEventLogServlet extends SamigoBaseServlet {


    private static final DateTimeFormatter EXPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ResourceLoader RESOURCE_BUNDLE = new ResourceLoader(SamigoConstants.EVENT_LOG_BUNDLE);

	private ServerConfigurationService serverConfigurationService = ComponentManager.get(ServerConfigurationService.class);
    private FormattedText formattedText = ComponentManager.get(FormattedText.class);
    private UserTimeService userTimeService = ComponentManager.get(UserTimeService.class);

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

        boolean displayIpAddressColumn = serverConfigurationService.getBoolean(SamigoConstants.SAK_PROP_EVENTLOG_IPADDRESS_ENABLED,
                SamigoConstants.SAK_PROP_DEFAULT_EVENTLOG_IPADDRESS_ENABLED);

        // Set headers
        res.setContentType("text/csv;charset=" + StandardCharsets.ISO_8859_1.name());

        String filename = StringUtils.replace(RESOURCE_BUNDLE.getString("log"), " ", "_") + ".csv";
        res.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename).build().toString());

        // Get data
        EventLogService eventLogService = new EventLogService();
        List<EventLogData> eventLogDataList;
        if (assessmentId != null) {
            eventLogDataList = eventLogService.getEventLogData(siteId, Long.valueOf(assessmentId));
        } else {
            eventLogDataList = eventLogService.getEventLogDataBySiteId(siteId);
        }

        // Prepare data for csv
        List<String[]> lines = new LinkedList<>();

        List<String> headerList = new ArrayList<>(){{
                add(RESOURCE_BUNDLE.getString("title"));
                add(RESOURCE_BUNDLE.getString("id"));
                add(RESOURCE_BUNDLE.getString("user_id"));
                add(RESOURCE_BUNDLE.getString("date_startd"));
                add(RESOURCE_BUNDLE.getString("date_submitted"));
                add(RESOURCE_BUNDLE.getString("duration"));
                add(RESOURCE_BUNDLE.getString("errors"));
                if (displayIpAddressColumn) {
                    add(RESOURCE_BUNDLE.getString("ipAddress"));
                }
        }};
        lines.add(headerList.toArray(new String[headerList.size()]));

        for (EventLogData eventLogData : eventLogDataList) {
            Integer minutes = eventLogData.getEclipseTime();

            List<String> cellList = new ArrayList<>(){{
                    add(eventLogData.getTitle());
                    add(eventLogData.getAssessmentIdStr());
                    add(eventLogData.getUserDisplay());
                    add(csvDateFormat(eventLogData.getStartDate()));
                    add(csvDateFormat(eventLogData.getEndDate()));
                    add(minutes != null ? minutes.toString() : null);
                    add(StringUtils.trimToEmpty(eventLogData.getErrorMsg()));
                    if (displayIpAddressColumn) {
                        add(StringUtils.trimToEmpty(eventLogData.getIpAddress()));
                    }
            }};

            lines.add(cellList.toArray(new String[cellList.size()]));
        }

        // Write csv to response
        PrintWriter out = res.getWriter();
        writeCsv(lines, out);
        out.flush();
        out.close();
    }

    private void writeCsv(List<String[]> lines, Writer writer) {
        char csvSeperator = StringUtils.equals(formattedText.getDecimalSeparator(), ",") ? ';' : ',';

        try (CSVWriter csvWriter = (CSVWriter) new CSVWriterBuilder(writer).withSeparator(csvSeperator).build()) {
            csvWriter.writeAll(lines);
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
