package org.sakaiproject.calendar.impl;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventVector;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.time.api.UserTimeService;
import org.sakaiproject.time.impl.BasicTimeService;
import org.sakaiproject.time.impl.UserLocaleServiceImpl;
import org.sakaiproject.util.ResourceLoader;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import lombok.extern.slf4j.Slf4j;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
public class PDFExportServiceTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    // Useful for debugging what's happening.
    private static boolean deleteFiles = true;

    private TimeService timeService;
    private PDFExportService pdfExportService;
    private DocumentBuilder docBuilder;

    @Mock
    private UserLocaleServiceImpl userLocaleService;
    @Mock
    private UserTimeService userTimeService;
    @Mock
    private BaseCalendarService baseCalendarService;
    @Mock
    private ResourceLoader resourceLoader;

    @Before
    public void setUp() throws ParserConfigurationException {
        BasicTimeService timeService = new BasicTimeService();
        timeService.setUserTimeService(userTimeService);
        timeService.setUserLocaleService(userLocaleService);
        timeService.init();
        this.timeService = timeService;

        when(userTimeService.getLocalTimeZone()).thenReturn(TimeZone.getTimeZone(ZoneOffset.UTC));
        when(userLocaleService.getLocalLocale()).thenReturn("en");

        when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        when(resourceLoader.getString(anyString())).then(invocation -> invocation.getArgument(0));

        pdfExportService = new PDFExportService(timeService, resourceLoader);
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Test
    public void testListExport() throws IOException, InterruptedException {
        Document doc = docBuilder.newDocument();
        TimeRange officeHours = newTimeRange("2007-12-03T09:00:00.00Z", "PT8H");
        // One week.
        TimeRange range = newTimeRange("2007-12-03T00:00:00.00Z", "P7D");
        List<String> calendarReferenceList = Collections.singletonList("/calendar/1");
        CalendarEventVector events = new CalendarEventVector();
        CalendarEvent event = mock(CalendarEvent.class);
        TimeRange eventTimeRange = newTimeRange("2007-12-03T10:30:00.00Z", "PT1H");
        when(event.getRange()).thenReturn(eventTimeRange);
        when(event.getDisplayName()).thenReturn("Test Event");
        events.add(event);

        when(baseCalendarService.getEvents(eq(calendarReferenceList), argThat(arg -> arg.contains(eventTimeRange)) )).thenReturn(events);
        when(baseCalendarService.getEvents(eq(calendarReferenceList), argThat(arg -> arg.contains(eventTimeRange)), anyBoolean())).thenReturn(events);

        Path file = Files.createTempFile("calendar", ".pdf");
        OpenOption[] options = (deleteFiles)?new OpenOption[]{DELETE_ON_CLOSE, CREATE}:new OpenOption[]{CREATE};
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toAbsolutePath(), options));

        String xslFileNameForScheduleType = pdfExportService.getXSLFileNameForScheduleType(CalendarService.LIST_VIEW);
        pdfExportService.generateXMLDocument(CalendarService.LIST_VIEW, doc, range, officeHours, calendarReferenceList, "userId", baseCalendarService, Boolean.FALSE);
        pdfExportService.generatePDF(doc, xslFileNameForScheduleType, out);
        out.close();
        if (!deleteFiles) {
            log.debug("Created file: {}", file.toString());
        }
    }

    @Test
    public void testDayExport() throws IOException, InterruptedException {
        Document doc = docBuilder.newDocument();
        TimeRange officeHours = newTimeRange("2007-12-03T09:00:00.00Z", "PT8H");
        // One week.
        TimeRange range = newTimeRange("2007-12-03T00:00:00.00Z", "P7D");
        List<String> calendarReferenceList = Collections.singletonList("/calendar/1");
        CalendarEventVector events = new CalendarEventVector();
        CalendarEvent event = mock(CalendarEvent.class);
        TimeRange eventTimeRange = newTimeRange("2007-12-03T10:30:00.00Z", "PT1H");
        when(event.getRange()).thenReturn(eventTimeRange);
        when(event.getDisplayName()).thenReturn("Test Event");
        events.add(event);

        when(baseCalendarService.getEvents(eq(calendarReferenceList), any(TimeRange.class))).thenReturn(new CalendarEventVector());
        when(baseCalendarService.getEvents(eq(calendarReferenceList), argThat(arg -> arg.contains(eventTimeRange)))).thenReturn(events);

        Path file = Files.createTempFile("calendar", ".pdf");
        OpenOption[] options = (deleteFiles)?new OpenOption[]{DELETE_ON_CLOSE, CREATE}:new OpenOption[]{CREATE};
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toAbsolutePath(), options));

        String xslFileNameForScheduleType = pdfExportService.getXSLFileNameForScheduleType(CalendarService.DAY_VIEW);
        pdfExportService.generateXMLDocument(CalendarService.DAY_VIEW, doc, range, officeHours, calendarReferenceList, "userId", baseCalendarService, false);
        pdfExportService.generatePDF(doc, xslFileNameForScheduleType, out);
        out.close();
        if (!deleteFiles) {
            log.debug("Created file: {}", file.toString());
        }
    }

    @Test
    public void testWeekExport() throws IOException, InterruptedException {
        Document doc = docBuilder.newDocument();
        TimeRange officeHours = newTimeRange("2007-12-03T09:00:00.00Z", "PT8H");
        // One week.
        TimeRange range = newTimeRange("2007-12-03T00:00:00.00Z", "P7D");
        List<String> calendarReferenceList = Collections.singletonList("/calendar/1");
        CalendarEventVector events = new CalendarEventVector();
        CalendarEvent event = mock(CalendarEvent.class);
        TimeRange eventTimeRange = newTimeRange("2007-12-03T10:30:00.00Z", "PT1H");
        when(event.getRange()).thenReturn(eventTimeRange);
        when(event.getDisplayName()).thenReturn("Test Event");
        events.add(event);

        when(baseCalendarService.getEvents(eq(calendarReferenceList), any(TimeRange.class))).thenReturn(new CalendarEventVector());
        when(baseCalendarService.getEvents(eq(calendarReferenceList), argThat(arg -> arg.contains(eventTimeRange)))).thenReturn(events);

        Path file = Files.createTempFile("calendar", ".pdf");
        OpenOption[] options = (deleteFiles) ? new OpenOption[]{DELETE_ON_CLOSE, CREATE} : new OpenOption[]{CREATE};
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toAbsolutePath(), options));

        String xslFileNameForScheduleType = pdfExportService.getXSLFileNameForScheduleType(CalendarService.WEEK_VIEW);
        pdfExportService.generateXMLDocument(CalendarService.WEEK_VIEW, doc, range, officeHours, calendarReferenceList, "userId", baseCalendarService, false);
        pdfExportService.generatePDF(doc, xslFileNameForScheduleType, out);
        out.close();
        if (!deleteFiles) {
            log.debug("Created file: {}", file.toString());
        }
    }

    @Test
    public void testMonthExport() throws IOException, InterruptedException, TransformerException {
        Document doc = docBuilder.newDocument();
        TimeRange officeHours = newTimeRange("2007-11-03T09:00:00.00Z", "PT8H");
        // One week.
        TimeRange range = newTimeRange("2007-11-03T00:00:00.00Z", "P7D");
        List<String> calendarReferenceList = Collections.singletonList("/calendar/1");
        CalendarEventVector events = new CalendarEventVector();
        CalendarEvent event = mock(CalendarEvent.class);
        TimeRange eventTimeRange = newTimeRange("2007-11-03T10:30:00.00Z", "PT1H");
        when(event.getRange()).thenReturn(eventTimeRange);
        when(event.getDisplayName()).thenReturn("Test Event");
        events.add(event);

        when(baseCalendarService.getEvents(eq(calendarReferenceList), any(TimeRange.class))).thenReturn(new CalendarEventVector());
        when(baseCalendarService.getEvents(eq(calendarReferenceList), argThat(arg -> arg.contains(eventTimeRange)))).thenReturn(events);

        Path file = Files.createTempFile("calendar", ".pdf");
        OpenOption[] options = (deleteFiles) ? new OpenOption[]{DELETE_ON_CLOSE, CREATE} : new OpenOption[]{CREATE};
        OutputStream out = new BufferedOutputStream(Files.newOutputStream(file.toAbsolutePath(), options));

        String xslFileNameForScheduleType = pdfExportService.getXSLFileNameForScheduleType(CalendarService.MONTH_VIEW);
        pdfExportService.generateXMLDocument(CalendarService.MONTH_VIEW, doc, range, officeHours, calendarReferenceList, "userId", baseCalendarService, false);

        pdfExportService.generatePDF(doc, xslFileNameForScheduleType, out);
        out.close();
        if (!deleteFiles) {
            log.debug("Created file: {}", file.toString());
        }
    }
    private TimeRange newTimeRange(String start, String length) {
        Instant beginning = Instant.parse(start);
        Duration duration = Duration.parse(length);
        return timeService.newTimeRange(beginning.toEpochMilli(), duration.toMillis());
    }

    // Useful in debugging.
    private void writeOutDOM(Document doc) {
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            log.debug("{}", writer);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

}
