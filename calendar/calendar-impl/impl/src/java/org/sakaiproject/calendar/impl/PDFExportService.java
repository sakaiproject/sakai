package org.sakaiproject.calendar.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import lombok.extern.slf4j.Slf4j;

import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.FopFactoryBuilder;
import org.apache.fop.apps.MimeConstants;
import org.apache.xmlgraphics.io.Resource;
import org.apache.xmlgraphics.io.ResourceResolver;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import org.sakaiproject.calendar.api.CalendarEvent;
import org.sakaiproject.calendar.api.CalendarEventVector;
import org.sakaiproject.calendar.api.CalendarService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.api.TimeBreakdown;
import org.sakaiproject.time.api.TimeRange;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.CalendarUtil;
import org.sakaiproject.util.ResourceLoader;

/**
 * This class deals with the generation of a PDF for a calendar.
 */
@Slf4j
public class PDFExportService {

    private static final int SCHEDULE_INTERVAL_IN_MINUTES = 15;

    private static final int MAX_OVERLAPPING_COLUMNS = 7;

    private static final int TIMESLOT_FOR_OVERLAP_DETECTION_IN_MINUTES = 10;

    // XML Node/Attribute Names
    private static final String COLUMN_NODE_NAME = "col";

    private static final String EVENT_NODE_NAME = "event";

    private static final String FACULTY_EVENT_ATTRIBUTE_NAME = "Faculty";

    private static final String FACULTY_NODE = "faculty";

    private static final String DESCRIPTION_NODE = "description";

    private static final String FROM_ATTRIBUTE_STRING = "from";

    private static final String GROUP_NODE = "grp";

    private static final String LIST_DATE_ATTRIBUTE_NAME = "dt";

    private static final String LIST_DAY_OF_MONTH_ATTRIBUTE_NAME = "dayofmonth";

    private static final String LIST_DAY_OF_WEEK_ATTRIBUTE_NAME = "dayofweek";

    private static final String LIST_NODE_NAME = "list";

    private static final String MONTH_NODE_NAME = "month";

    private static final String MAX_CONCURRENT_EVENTS_NAME = "maxConcurrentEvents";

    private static final String PLACE_NODE = "place";

    private static final String ROW_NODE_NAME = "row";

    private static final String SCHEDULE_NODE = "schedule";

    private static final String START_DAY_WEEK_ATTRIBUTE_NAME = "startdayweek";

    private static final String MONTH_ATTRIBUTE_NAME = "month";

    private static final String YEAR_ATTRIBUTE_NAME = "yyyy";

    private static final String START_TIME_ATTRIBUTE_NAME = "start-time";

    private static final String SUB_EVENT_NODE_NAME = "subEvent";

    private static final String TITLE_NODE = "title";

    private static final String TO_ATTRIBUTE_STRING = "to";

    private static final String TYPE_NODE = "type";

    private static final String UID_NODE = "uid";

    // Constants for time calculations
    private static long MILLISECONDS_IN_DAY = (60 * 60 * 24 * 1000);

    private final static long MILLISECONDS_IN_HOUR = (60 * 60 * 1000);

    private final static long MILLISECONDS_IN_MINUTE = (1000 * 60);

    private static final long MINIMUM_EVENT_LENGTH_IN_MSECONDS = (29 * MILLISECONDS_IN_MINUTE);

    // XSL File Names
    private final static String DAY_VIEW_XSLT_FILENAME = "schedule.xsl";

    private final static String LIST_VIEW_XSLT_FILENAME = "schlist.xsl";

    private final static String MONTH_VIEW_XSLT_FILENAME = "schedulemm.xsl";

    private final static String WEEK_VIEW_XSLT_FILENAME = "schedule.xsl";


    // Misc.
    private static final String HOUR_MINUTE_SEPARATOR = ":";

    // FOP Configuration
    private final static String FOP_USERCONFIG = "fonts/fop.xml";
    private final static String FOP_FONTBASEDIR = "fonts";

    private TimeService timeService;
    private TransformerFactory transformerFactory;
    private ResourceLoader rb;
    private FopFactory fopFactory;


    public PDFExportService(TimeService timeService, ResourceLoader rb) {
        this.timeService = timeService;
        this.rb = rb;

        transformerFactory = TransformerFactory.newInstance();
        transformerFactory.setURIResolver( new MyURIResolver(getClass().getClassLoader()) );

        try {

            URI baseDir = getClass().getClassLoader().getResource(FOP_FONTBASEDIR).toURI();
            FopFactoryBuilder builder = new FopFactoryBuilder(baseDir, new ClassPathResolver());
            InputStream userConfig = getClass().getClassLoader().getResourceAsStream(FOP_USERCONFIG);
            fopFactory = builder.setConfiguration(new DefaultConfigurationBuilder().build(userConfig)).build();

        } catch (IOException | URISyntaxException | SAXException | ConfigurationException e) {
            // We won't be able to do anything if we can't create a FopFactory so may as well get caller to handle.
            throw new RuntimeException("Failed to setup Apache FOP for calendar PDF exports.", e);
        }

    }

    /**
     * Takes a DOM structure and renders a PDF
     *
     * @param doc         DOM structure
     * @param xslFileName The XSLT file to use to do the transform
     * @param streamOut The outputstream to write the output to.
     * @throws RuntimeException If there was a problem transforming the document to PDF.
     */
    void generatePDF(Document doc, String xslFileName, OutputStream streamOut) {

        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(xslFileName);
            StreamSource source = new StreamSource(in);
            Transformer transformer = transformerFactory.newTransformer(source);
            Source src = new DOMSource(doc);

            Calendar c = Calendar.getInstance(timeService.getLocalTimeZone(), rb.getLocale());
            CalendarUtil calUtil = new CalendarUtil(c, rb);
            String[] dayNames = calUtil.getCalendarDaysOfWeekNames(true);
            String[] monthNames = calUtil.getCalendarMonthNames(true);

            // Kludge: Xalan in JDK 1.4/1.5 does not properly resolve java classes
            // (http://xml.apache.org/xalan-j/faq.html#jdk14)
            // Clean this up in JDK 1.6 and pass ResourceBundle/ArrayList parms
            transformer.setParameter("dayNames0", dayNames[0]);
            transformer.setParameter("dayNames1", dayNames[1]);
            transformer.setParameter("dayNames2", dayNames[2]);
            transformer.setParameter("dayNames3", dayNames[3]);
            transformer.setParameter("dayNames4", dayNames[4]);
            transformer.setParameter("dayNames5", dayNames[5]);
            transformer.setParameter("dayNames6", dayNames[6]);

            transformer.setParameter("jan", monthNames[0]);
            transformer.setParameter("feb", monthNames[1]);
            transformer.setParameter("mar", monthNames[2]);
            transformer.setParameter("apr", monthNames[3]);
            transformer.setParameter("may", monthNames[4]);
            transformer.setParameter("jun", monthNames[5]);
            transformer.setParameter("jul", monthNames[6]);
            transformer.setParameter("aug", monthNames[7]);
            transformer.setParameter("sep", monthNames[8]);
            transformer.setParameter("oct", monthNames[9]);
            transformer.setParameter("nov", monthNames[10]);
            transformer.setParameter("dec", monthNames[11]);

            transformer.setParameter("site", rb.getString("event.site"));
            transformer.setParameter("event", rb.getString("event.event"));
            transformer.setParameter("location", rb.getString("event.location"));
            transformer.setParameter("type", rb.getString("event.type"));
            transformer.setParameter("from", rb.getString("event.from"));

            transformer.setParameter("sched", rb.getString("sched.for"));
            if (log.isDebugEnabled()) {

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                transformer.transform(src, new StreamResult(bos));
                log.debug(new String(bos.toByteArray()));
            }

            Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, streamOut);
            transformer.transform(src, new SAXResult(fop.getDefaultHandler()));
        } catch (TransformerException e) {
            log.warn("Failed to generate XSL-FO with XSLT.", e);
            throw new RuntimeException(e);
        } catch (FOPException e) {
            log.warn("Failed to produce PDF from XSL-FO.", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * @param scheduleType
     *        daily, weekly, monthly, or list (no yearly).
     * @param doc
     *        XML output document
     * @param timeRange
     *        this is the overall time range. For example, for a weekly schedule, it would be the start/end times for the currently selected week period.
     * @param dailyTimeRange
     *        On a weekly time schedule, even if the overall time range is for a week, you're only looking at a portion of the day (e.g., 8 AM to 6 PM, etc.)
     * @param calendarReferenceList
     * @param userID
     * @param baseCalendarService
     */
    void generateXMLDocument(int scheduleType, Document doc, TimeRange timeRange, TimeRange dailyTimeRange,
                                       List calendarReferenceList, String userID, BaseCalendarService baseCalendarService, boolean reverseOrder)
    {

        // This list will have an entry for every week day that we care about.
        List timeRangeList = null;
        TimeRange actualTimeRange = null;
        Element topLevelMaxConcurrentEvents = null;

        switch (scheduleType)
        {
            case CalendarService.WEEK_VIEW:
                actualTimeRange = timeRange;
                timeRangeList = getTimeRangeListForWeek(actualTimeRange, calendarReferenceList, dailyTimeRange);
                break;

            case CalendarService.MONTH_VIEW:
                // Make sure that we trim off the days of the previous and next
                // month. The time range that we're being passed is "padded"
                // with extra days to make up a full block of an integral number
                // of seven day weeks.
                actualTimeRange = shrinkTimeRangeToCurrentMonth(timeRange);
                timeRangeList = splitTimeRangeIntoListOfSingleDayTimeRanges(actualTimeRange, null);
                break;

            case CalendarService.LIST_VIEW:
                //
                // With the list view, we want to come up with a list of days
                // that have events, not every day in the range.
                //
                actualTimeRange = timeRange;

                timeRangeList = makeListViewTimeRangeList(actualTimeRange, calendarReferenceList, baseCalendarService, reverseOrder);
                break;

            case CalendarService.DAY_VIEW:
                //
                // We have a single entry in the list for a day. Having a singleton
                // list may seem wasteful, but it allows us to use one loop below
                // for all processing.
                //
                actualTimeRange = timeRange;
                timeRangeList = splitTimeRangeIntoListOfSingleDayTimeRanges(actualTimeRange, dailyTimeRange);
                break;

            default:
                log.warn(".generateXMLDocument(): bad scheduleType parameter = " + scheduleType);
                break;
        }

        if (timeRangeList != null)
        {
            // Create Root Element
            Element root = doc.createElement(SCHEDULE_NODE);

            if (userID != null)
            {
                writeStringNodeToDom(doc, root, UID_NODE, userID);
            }

            // Write out the number of events that we have per timeslot.
            // This is used to figure out how to display overlapping events.
            // At this level, assume that we start with 1 event.
            topLevelMaxConcurrentEvents = writeStringNodeToDom(doc, root, MAX_CONCURRENT_EVENTS_NAME, "1");

            // Add a start time node.
            writeStringNodeToDom(doc, root, START_TIME_ATTRIBUTE_NAME, getTimeString(dailyTimeRange.firstTime()));

            // Add the Root Element to Document
            doc.appendChild(root);

            //
            // Only add a "month" node with the first numeric day
            // of the month if we're in the month view.
            //
            if (scheduleType == CalendarService.MONTH_VIEW)
            {
                CalendarUtil monthCalendar = new CalendarUtil(rb);

                // Use the middle of the month since the start/end ranges
                // may be in an adjacent month.
                TimeBreakdown breakDown = actualTimeRange.firstTime().breakdownLocal();

                monthCalendar.setDay(breakDown.getYear(), breakDown.getMonth(), breakDown.getDay());

                int firstDayOfMonth = monthCalendar.getFirstDayOfMonth(breakDown.getMonth() - 1);

                // Create a list of events for the given day.
                Element monthElement = doc.createElement(MONTH_NODE_NAME);
                monthElement.setAttribute(START_DAY_WEEK_ATTRIBUTE_NAME, Integer.toString(firstDayOfMonth));
                monthElement.setAttribute(MONTH_ATTRIBUTE_NAME, Integer.toString(breakDown.getMonth()));
                monthElement.setAttribute(YEAR_ATTRIBUTE_NAME, Integer.toString(breakDown.getYear()));

                root.appendChild(monthElement);
            }

            Iterator itList = timeRangeList.iterator();

            int maxNumberOfColumnsPerRow = 1;

            // Go through all the time ranges (days)
            while (itList.hasNext())
            {
                TimeRange currentTimeRange = (TimeRange) itList.next();
                int maxConcurrentEventsOverListNode = 1;

                // Get a list of merged events.
                CalendarEventVector calendarEventVector = baseCalendarService.getEvents(calendarReferenceList, currentTimeRange);

                //
                // We don't need to generate "empty" event lists for the list view.
                //
                if (scheduleType == CalendarService.LIST_VIEW && calendarEventVector.size() == 0)
                {
                    continue;
                }

                // Create a list of events for the given day.
                Element eventList = doc.createElement(LIST_NODE_NAME);

                // Set the current date
                eventList.setAttribute(LIST_DATE_ATTRIBUTE_NAME, getDateFromTime(currentTimeRange.firstTime()));

                eventList.setAttribute(LIST_DAY_OF_MONTH_ATTRIBUTE_NAME, getDayOfMonthFromTime(currentTimeRange.firstTime()));

                // Set the maximum number of events per timeslot
                // Assume 1 as a starting point. This may be changed
                // later on.
                eventList.setAttribute(MAX_CONCURRENT_EVENTS_NAME, Integer.toString(maxConcurrentEventsOverListNode));

                // Calculate the day of the week.
                Calendar c = Calendar.getInstance(timeService.getLocalTimeZone(), rb.getLocale());
                CalendarUtil cal = new CalendarUtil(c, rb);

                Time date = currentTimeRange.firstTime();
                TimeBreakdown breakdown = date.breakdownLocal();

                cal.setDay(breakdown.getYear(), breakdown.getMonth(), breakdown.getDay());

                // Set the day of the week as a node attribute.
                eventList.setAttribute(LIST_DAY_OF_WEEK_ATTRIBUTE_NAME, Integer.toString(cal.getDay_Of_Week(true) - 1));

                // Attach this list to the top-level node
                root.appendChild(eventList);

                Iterator itEvent = calendarEventVector.iterator();

                //
                // Day and week views use a layout table to assist in constructing the
                // rowspan information for layout.
                //
                if (scheduleType == CalendarService.DAY_VIEW || scheduleType == CalendarService.WEEK_VIEW)
                {
                    SingleDayLayoutTable layoutTable = new SingleDayLayoutTable(currentTimeRange, MAX_OVERLAPPING_COLUMNS,
                            SCHEDULE_INTERVAL_IN_MINUTES);

                    // Load all the events into our layout table.
                    while (itEvent.hasNext())
                    {
                        CalendarEvent event = (CalendarEvent) itEvent.next();
                        layoutTable.addEvent(event);
                    }

                    List layoutRows = layoutTable.getLayoutRows();

                    Iterator rowIterator = layoutRows.iterator();

                    // Iterate through the list of rows.
                    while (rowIterator.hasNext())
                    {
                        LayoutRow layoutRow = (LayoutRow) rowIterator.next();
                        TimeRange rowTimeRange = layoutRow.getRowTimeRange();

                        if (maxNumberOfColumnsPerRow < layoutRow.size())
                        {
                            maxNumberOfColumnsPerRow = layoutRow.size();
                        }

                        if (maxConcurrentEventsOverListNode < layoutRow.size())
                        {
                            maxConcurrentEventsOverListNode = layoutRow.size();
                        }

                        Element eventNode = doc.createElement(EVENT_NODE_NAME);
                        eventList.appendChild(eventNode);

                        // Add the "from" time as an attribute.
                        eventNode.setAttribute(FROM_ATTRIBUTE_STRING, getTimeString(rowTimeRange.firstTime()));

                        // Add the "to" time as an attribute.
                        eventNode.setAttribute(TO_ATTRIBUTE_STRING, getTimeString(performEndMinuteKludge(rowTimeRange.lastTime()
                                .breakdownLocal())));

                        Element rowNode = doc.createElement(ROW_NODE_NAME);

                        // Set an attribute indicating the number of columns in this row.
                        rowNode.setAttribute(MAX_CONCURRENT_EVENTS_NAME, Integer.toString(layoutRow.size()));

                        eventNode.appendChild(rowNode);

                        Iterator layoutRowIterator = layoutRow.iterator();

                        // Iterate through our list of column lists.
                        while (layoutRowIterator.hasNext())
                        {
                            Element columnNode = doc.createElement(COLUMN_NODE_NAME);
                            rowNode.appendChild(columnNode);

                            List columnList = (List) layoutRowIterator.next();

                            Iterator columnListIterator = columnList.iterator();

                            // Iterate through the list of columns.
                            while (columnListIterator.hasNext())
                            {
                                CalendarEvent event = (CalendarEvent) columnListIterator.next();
                                generateXMLEvent(doc, columnNode, event, SUB_EVENT_NODE_NAME, rowTimeRange, true, false, false, baseCalendarService);
                            }
                        }
                    }
                }
                else
                {
                    // Generate XML for all the events.
                    while (itEvent.hasNext())
                    {
                        CalendarEvent event = (CalendarEvent) itEvent.next();
                        generateXMLEvent(doc, eventList, event, EVENT_NODE_NAME, currentTimeRange, false, false,
                                (scheduleType == CalendarService.LIST_VIEW ? true : false), baseCalendarService);
                    }
                }

                // Update this event after having gone through all the rows.
                eventList.setAttribute(MAX_CONCURRENT_EVENTS_NAME, Integer.toString(maxConcurrentEventsOverListNode));

            }

            // Set the node value way up at the head of the document to indicate
            // what the maximum number of columns was for the entire document.
            topLevelMaxConcurrentEvents.getFirstChild().setNodeValue(Integer.toString(maxNumberOfColumnsPerRow));
        }

    }

    /**
     * Given a schedule type, the appropriate XSLT file is returned
     */
    String getXSLFileNameForScheduleType(int scheduleType)
    {
        // get a relative path to the file
        String baseFileName = "";

        switch (scheduleType)
        {
            case BaseCalendarService.WEEK_VIEW:
                baseFileName = WEEK_VIEW_XSLT_FILENAME;
                break;

            case BaseCalendarService.DAY_VIEW:
                baseFileName = DAY_VIEW_XSLT_FILENAME;
                break;

            case BaseCalendarService.MONTH_VIEW:
                baseFileName = MONTH_VIEW_XSLT_FILENAME;
                break;

            case BaseCalendarService.LIST_VIEW:
                baseFileName = LIST_VIEW_XSLT_FILENAME;
                break;

            default:
                log.debug("PrintFileGeneration.getXSLFileNameForScheduleType(): unexpected scehdule type = " + scheduleType);
                break;
        }

        return baseFileName;
    }

    /**
     * Make a list of days for use in generating an XML document for the list view.
     * @param timeRange
     * @param calendarReferenceList
     * @param baseCalendarService
     * @param reverseOrder
     */
    private List makeListViewTimeRangeList(TimeRange timeRange, List calendarReferenceList, BaseCalendarService baseCalendarService, boolean reverseOrder)
    {
        // This is used to dimension a hash table. The default load factor is .75.
        // A rehash isn't done until the number of items in the table is .75 * the number
        // of items in the capacity.
        final int DEFAULT_INITIAL_HASH_CAPACITY = 150;

        List listOfDays = new ArrayList();

        // Get a list of merged events.
        CalendarEventVector calendarEventVector = baseCalendarService.getEvents(calendarReferenceList, timeRange, reverseOrder);

        Iterator itEvents = calendarEventVector.iterator();
        HashMap datesSeenSoFar = new HashMap(DEFAULT_INITIAL_HASH_CAPACITY);

        while (itEvents.hasNext())
        {

            CalendarEvent event = (CalendarEvent) itEvents.next();

            //
            // Each event may span multiple days, so we need to split each
            // events's time range into single day slots.
            //
            List timeRangeList = splitTimeRangeIntoListOfSingleDayTimeRanges(event.getRange(), null);

            Iterator itDatesInRange = timeRangeList.iterator();

            while (itDatesInRange.hasNext())
            {
                TimeRange curDay = (TimeRange) itDatesInRange.next();
                String curDate = curDay.firstTime().toStringLocalDate();

                if (!datesSeenSoFar.containsKey(curDate))
                {
                    // Add this day to list
                    TimeBreakdown startBreakDown = curDay.firstTime().breakdownLocal();

                    listOfDays.add(getFullDayTimeRangeFromYMD(startBreakDown.getYear(), startBreakDown.getMonth(), startBreakDown
                            .getDay()));

                    datesSeenSoFar.put(curDate, "");
                }
            }
        }

        return listOfDays;
    }

    /**
     * Returns a list of daily time ranges for every day in a range.
     * @param timeRange
     *        overall time range
     * @param dailyTimeRange
     */
    private ArrayList splitTimeRangeIntoListOfSingleDayTimeRanges(TimeRange timeRange, TimeRange dailyTimeRange)
    {

        TimeBreakdown startBreakdown = timeRange.firstTime().breakdownLocal();
        TimeBreakdown endBreakdown = timeRange.lastTime().breakdownLocal();

        GregorianCalendar startCalendarDate = new GregorianCalendar();
        startCalendarDate.set(startBreakdown.getYear(), startBreakdown.getMonth() - 1, startBreakdown.getDay(), 0, 0, 0);

        long numDaysInTimeRange = getNumberDaysGivenTwoDates(startBreakdown.getYear(), startBreakdown.getMonth() - 1,
                startBreakdown.getDay(), endBreakdown.getYear(), endBreakdown.getMonth() - 1, endBreakdown.getDay());

        ArrayList splitTimeRanges = new ArrayList();

        TimeBreakdown dailyStartBreakDown = null;
        TimeBreakdown dailyEndBreakDown = null;

        if (dailyTimeRange != null)
        {
            dailyStartBreakDown = dailyTimeRange.firstTime().breakdownLocal();
            dailyEndBreakDown = dailyTimeRange.lastTime().breakdownLocal();
        }

        for (long i = 0; i < numDaysInTimeRange; i++)
        {
            Time curStartTime = null;
            Time curEndTime = null;

            if (dailyTimeRange != null)
            {
                //
                // Use the same start/end times for all days.
                //
                curStartTime = timeService.newTimeLocal(startCalendarDate.get(GregorianCalendar.YEAR), startCalendarDate
                                .get(GregorianCalendar.MONTH) + 1, startCalendarDate.get(GregorianCalendar.DAY_OF_MONTH),
                        dailyStartBreakDown.getHour(), dailyStartBreakDown.getMin(), dailyStartBreakDown.getSec(),
                        dailyStartBreakDown.getMs());

                curEndTime = timeService.newTimeLocal(startCalendarDate.get(GregorianCalendar.YEAR), startCalendarDate
                        .get(GregorianCalendar.MONTH) + 1, startCalendarDate.get(GregorianCalendar.DAY_OF_MONTH), dailyEndBreakDown
                        .getHour(), dailyEndBreakDown.getMin(), dailyEndBreakDown.getSec(), dailyEndBreakDown.getMs());

                splitTimeRanges.add(timeService.newTimeRange(curStartTime, curEndTime, true, false));
            }
            else
            {
                //
                // Add a full day range since no start/stop time was specified.
                //
                splitTimeRanges.add(getFullDayTimeRangeFromYMD(startCalendarDate.get(GregorianCalendar.YEAR), startCalendarDate
                        .get(GregorianCalendar.MONTH) + 1, startCalendarDate.get(GregorianCalendar.DAY_OF_MONTH)));
            }

            // Move to the next day.
            startCalendarDate.add(GregorianCalendar.DATE, 1);
        }

        return splitTimeRanges;
    }

    /**
     * Calculate the number of days in a range of time given two dates.
     *
     * @param startYear
     * @param startMonth
     *        (zero based, 0-11)
     * @param startDay
     *        (one based, 1-31)
     * @param endYear
     *        (one based, 1-31)
     * @param endMonth
     * @param endDay
     */
    private static long getNumberDaysGivenTwoDates(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay)
    {
        GregorianCalendar startDate = new GregorianCalendar();
        GregorianCalendar endDate = new GregorianCalendar();

        startDate.set(startYear, startMonth, startDay, 0, 0, 0);
        endDate.set(endYear, endMonth, endDay, 0, 0, 0);

        long duration = endDate.getTime().getTime() - startDate.getTime().getTime();

        // Allow for daylight savings time.
        return ((duration + MILLISECONDS_IN_HOUR) / (24 * MILLISECONDS_IN_HOUR)) + 1;
    }

    /**
     * The time ranges that we get from the CalendarAction class have days in the week of the first and last weeks padded out to make a full week. This function will shrink this range to only one month.
     * @param expandedTimeRange
     */
    private TimeRange shrinkTimeRangeToCurrentMonth(TimeRange expandedTimeRange)
    {
        long millisecondsInWeek = (7 * MILLISECONDS_IN_DAY);

        Time startTime = expandedTimeRange.firstTime();

        // Grab something in the middle of the time range so that we know that we're
        // in the right month.
        Time somewhereInTheMonthTime = timeService.newTime(startTime.getTime() + 2 * millisecondsInWeek);

        TimeBreakdown somewhereInTheMonthBreakdown = somewhereInTheMonthTime.breakdownLocal();


        CalendarUtil calendar = new CalendarUtil(rb);

        calendar.setDay(somewhereInTheMonthBreakdown.getYear(), somewhereInTheMonthBreakdown.getMonth(),
                somewhereInTheMonthBreakdown.getDay());

        int numDaysInMonth = calendar.getNumberOfDays();

        //
        // Construct a new time range starting on the first day of the month and ending on
        // the last day at one millisecond before midnight.
        //
        return timeService.newTimeRange(timeService.newTimeLocal(somewhereInTheMonthBreakdown.getYear(),
                somewhereInTheMonthBreakdown.getMonth(), 1, 0, 0, 0, 0), timeService.newTimeLocal(somewhereInTheMonthBreakdown
                .getYear(), somewhereInTheMonthBreakdown.getMonth(), numDaysInMonth, 23, 59, 59, 999));
    }

    /**
     * Generates a list of time ranges for a week. Each range in the list is a day.
     *  @param timeRange start & end date range
     * @param calendarReferenceList list of calendar(s)
     * @param dailyTimeRange start and end hour/minute time range
     */
    private ArrayList getTimeRangeListForWeek(TimeRange timeRange, List calendarReferenceList, TimeRange dailyTimeRange)
    {
        TimeBreakdown startBreakdown = timeRange.firstTime().breakdownLocal();

        GregorianCalendar startCalendarDate = (GregorianCalendar)GregorianCalendar.getInstance(timeService.getLocalTimeZone(), rb.getLocale());
        startCalendarDate.set(startBreakdown.getYear(),	startBreakdown.getMonth() - 1, startBreakdown.getDay(), 0, 0, 0);

        ArrayList weekDayTimeRanges = new ArrayList();

        TimeBreakdown startBreakDown = dailyTimeRange.firstTime().breakdownLocal();

        TimeBreakdown endBreakDown = dailyTimeRange.lastTime().breakdownLocal();

        // Search all seven weekdays
        // Note: no assumption can be made regarding the first day being Sunday,
        // since in some locales, the first weekday is Monday.
        for (int i = 0; i <= 6; i++)
        {
            //
            // Use the same start/end times for all days.
            //
            Time curStartTime = timeService.newTimeLocal(startCalendarDate.get(GregorianCalendar.YEAR), startCalendarDate
                    .get(GregorianCalendar.MONTH) + 1, startCalendarDate.get(GregorianCalendar.DAY_OF_MONTH), startBreakDown
                    .getHour(), startBreakDown.getMin(), startBreakDown.getSec(), startBreakDown.getMs());

            Time curEndTime = timeService.newTimeLocal(startCalendarDate.get(GregorianCalendar.YEAR), startCalendarDate
                    .get(GregorianCalendar.MONTH) + 1, startCalendarDate.get(GregorianCalendar.DAY_OF_MONTH), endBreakDown
                    .getHour(), endBreakDown.getMin(), endBreakDown.getSec(), endBreakDown.getMs());

            TimeRange newTimeRange = timeService.newTimeRange(curStartTime, curEndTime, true, false);
            weekDayTimeRanges.add(newTimeRange);

            // Move to the next day.
            startCalendarDate.add(GregorianCalendar.DATE, 1);
        }

        return weekDayTimeRanges;
    }

    /**
     * Make a full-day time range given a year, month, and day
     * @param year
     * @param month
     * @param day
     */
    private TimeRange getFullDayTimeRangeFromYMD(int year, int month, int day)
    {
        return timeService.newTimeRange(timeService.newTimeLocal(year, month, day, 0, 0, 0, 0), timeService.newTimeLocal(year,
                month, day, 23, 59, 59, 999));
    }

    /**
     * Generates the XML for an event.
     * @param doc
     * @param parent
     * @param event
     * @param eventNodeName
     * @param containingTimeRange
     * @param forceMinimumTime
     * @param hideGroupIfNoSpace
     * @param performEndTimeKludge
     * @param baseCalendarService
     */
    private void generateXMLEvent(Document doc, Element parent, CalendarEvent event, String eventNodeName,
                                  TimeRange containingTimeRange, boolean forceMinimumTime, boolean hideGroupIfNoSpace, boolean performEndTimeKludge, BaseCalendarService baseCalendarService)
    {
        Element eventElement = doc.createElement(eventNodeName);

        TimeRange trimmedTimeRange = trimTimeRange(containingTimeRange, event.getRange());

        // Optionally force the event to have a minimum time slot.
        if (forceMinimumTime)
        {
            trimmedTimeRange = roundRangeToMinimumTimeInterval(trimmedTimeRange);
        }

        // Add the "from" time as an attribute.
        eventElement.setAttribute(FROM_ATTRIBUTE_STRING, getTimeString(trimmedTimeRange.firstTime()));

        // Add the "to" time as an attribute.
        Time endTime = null;

        // Optionally adjust the end time
        if (performEndTimeKludge)
        {
            endTime = performEndMinuteKludge(trimmedTimeRange.lastTime().breakdownLocal());
        }
        else
        {
            endTime = trimmedTimeRange.lastTime();
        }

        eventElement.setAttribute(TO_ATTRIBUTE_STRING, getTimeString(endTime));

        //
        // Add the group (really "site") node
        // Provide that we have space or if we've been told we need to display it.
        //
        if (!hideGroupIfNoSpace || trimmedTimeRange.duration() > MINIMUM_EVENT_LENGTH_IN_MSECONDS)
        {
            writeStringNodeToDom(doc, eventElement, GROUP_NODE, event.getSiteName());
        }

        // Add the display name node.
        writeStringNodeToDom(doc, eventElement, TITLE_NODE, event.getDisplayName());

        // Add the event type node.
        writeStringNodeToDom(doc, eventElement, TYPE_NODE, event.getType());

        // Add the place/location node.
        writeStringNodeToDom(doc, eventElement, PLACE_NODE, event.getLocation());

        // If a "Faculty" extra field is present, then add the node.
        writeStringNodeToDom(doc, eventElement, FACULTY_NODE, event.getField(FACULTY_EVENT_ATTRIBUTE_NAME));

        // If a "Description" field is present, then add the node.
        writeStringNodeToDom(doc, eventElement, DESCRIPTION_NODE, event.getDescription());

        parent.appendChild(eventElement);
    }

    /**
     * Gets a standard time string give the time parameter.
     * @param time
     */
    private String getTimeString(Time time)
    {
        TimeBreakdown timeBreakdown = time.breakdownLocal();

        DecimalFormat twoDecimalDigits = new DecimalFormat("00");

        return timeBreakdown.getHour() + HOUR_MINUTE_SEPARATOR + twoDecimalDigits.format(timeBreakdown.getMin());
    }

    /**
     * Trim the range that is passed in to the containing time range.
     */
    private TimeRange trimTimeRange(TimeRange containingRange, TimeRange rangeToTrim)
    {
        long containingRangeStartTime = containingRange.firstTime().getTime();
        long containingRangeEndTime = containingRange.lastTime().getTime();

        long rangeToTrimStartTime = rangeToTrim.firstTime().getTime();
        long rangeToTrimEndTime = rangeToTrim.lastTime().getTime();

        long trimmedStartTime = 0, trimmedEndTime = 0;

        trimmedStartTime = Math.min(Math.max(containingRangeStartTime, rangeToTrimStartTime), containingRangeEndTime);
        trimmedEndTime = Math.max(Math.min(containingRangeEndTime, rangeToTrimEndTime), rangeToTrimStartTime);

        return timeService.newTimeRange(timeService.newTime(trimmedStartTime), timeService.newTime(trimmedEndTime), true, false);
    }

    /**
     * This routine is used to round the end time. The time is stored at one minute less than the actual end time,
     * but the user will expect to see the end time on the hour. For example, an event that ends at 10:00 is
     * actually stored at 9:59. This code should really be in a central place so that the velocity template can see it as well.
     */
    private Time performEndMinuteKludge(TimeBreakdown breakDown)
    {
        int endMin = breakDown.getMin();
        int endHour = breakDown.getHour();

        int tmpMinVal = endMin % TIMESLOT_FOR_OVERLAP_DETECTION_IN_MINUTES;

        if (tmpMinVal == 4 || tmpMinVal == 9)
        {
            endMin = endMin + 1;

            if (endMin == 60)
            {
                endMin = 00;
                endHour = endHour + 1;
            }
        }

        return timeService.newTimeLocal(breakDown.getYear(), breakDown.getMonth(), breakDown.getDay(), endHour, endMin, breakDown
                .getSec(), breakDown.getMs());
    }

    /**
     * Utility routine to write a string node to the DOM.
     */
    private Element writeStringNodeToDom(Document doc, Element parent, String nodeName, String nodeValue)
    {
        if (nodeValue != null && nodeValue.length() != 0)
        {
            Element name = doc.createElement(nodeName);
            name.appendChild(doc.createTextNode(nodeValue));
            parent.appendChild(name);
            return name;
        }

        return null;
    }

    /**
     * Rounds a time range up to a minimum interval.
     */
    private TimeRange roundRangeToMinimumTimeInterval(TimeRange timeRange)
    {
        TimeRange roundedTimeRange = timeRange;

        if (timeRange.duration() < MINIMUM_EVENT_LENGTH_IN_MSECONDS)
        {
            roundedTimeRange = timeService.newTimeRange(timeRange.firstTime().getTime(), MINIMUM_EVENT_LENGTH_IN_MSECONDS);
        }

        return roundedTimeRange;
    }

    /**
     * Gets the standard date string from the time parameter
     */
    private String getDateFromTime(Time time)
    {
        TimeBreakdown timeBreakdown = time.breakdownLocal();
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT,rb.getLocale());
        return dateFormat.format(new Date(time.getTime()));

    }

    private String getDayOfMonthFromTime(Time time)
    {
        TimeBreakdown timeBreakdown = time.breakdownLocal();
        return Integer.toString(timeBreakdown.getDay());
    }

    private String getEventDescription(String type){
        if ((type!=null) && (type.trim()!="")){
            return CalendarEventType.getLocalizedLegendFromEventType(type);
        }else{
            return CalendarEventType.getLocalizedLegendFromEventType("Activity");
        }
    }

    /**
     * This is a container for a list of columns, plus the timerange for all the events contained in the row. This time range is a union of all the separate time ranges.
     */
    protected static class LayoutRow extends ArrayList
    {
        // Union of all event time ranges in this row.
        private TimeRange rowTimeRange;

        /**
         * Gets the union of all event time ranges in this row.
         */
        public TimeRange getRowTimeRange()
        {
            return rowTimeRange;
        }

        /**
         * Sets the union of all event time ranges in this row.
         */
        public void setRowTimeRange(TimeRange range)
        {
            rowTimeRange = range;
        }
    }

    /**
     * Table used to layout a single day, with potentially overlapping events.
     */
    protected class SingleDayLayoutTable
    {
        protected long millisecondsPerTimeslot;

        protected int numCols;

        protected int numRows;

        protected ArrayList rows;

        // Overall time range for this table.
        protected TimeRange timeRange;

        /**
         * Constructor for SingleDayLayoutTable
         */
        public SingleDayLayoutTable(TimeRange timeRange, int maxNumberOverlappingEvents, int timeslotInMinutes)
        {
            this.timeRange = timeRange;
            numCols = maxNumberOverlappingEvents;

            millisecondsPerTimeslot = timeslotInMinutes * MILLISECONDS_IN_MINUTE;

            numRows = getNumberOfRowsNeeded(timeRange);

            rows = new ArrayList(numRows);

            for (int i = 0; i < numRows; i++)
            {
                ArrayList newRow = new ArrayList(numCols);

                rows.add(i, newRow);

                for (int j = 0; j < numCols; j++)
                {
                    newRow.add(j, new LayoutTableCell());
                }
            }
        }

        /**
         * Adds an event to the SingleDayLayoutTable
         */
        public void addEvent(CalendarEvent calendarEvent)
        {
            if (calendarEvent == null)
            {
                return;
            }

            int startingRow = getStartingRow(roundRangeToMinimumTimeInterval(calendarEvent.getRange()));

            int numRowsNeeded = getNumberOfRowsNeeded(roundRangeToMinimumTimeInterval(calendarEvent.getRange()));

            // Trim to the end of the table.
            if (startingRow + numRowsNeeded >= getNumRows())
            {
                numRowsNeeded = getNumRows() - startingRow;
            }

            // Get the first column that has enough sequential free intervals to
            // contain this event.
            int columnNumber = getFreeColumn(startingRow, numRowsNeeded);

            if (columnNumber != -1)
            {
                for (int i = startingRow; i < startingRow + numRowsNeeded; i++)
                {
                    LayoutTableCell cell = getCell(i, columnNumber);

                    // All cells have the calendar event information.
                    cell.setCalendarEvent(calendarEvent);

                    // Only the first cell is marked as such.
                    if (i == startingRow)
                    {
                        cell.setFirstCell(true);
                    }

                    cell.setFirstCellRow(startingRow);
                    cell.setFirstCellColumn(columnNumber);

                    cell.setThisCellRow(i);
                    cell.setThisCellColumn(columnNumber);

                    cell.setNumCellsInEvent(numRowsNeeded);
                }
            }
        }

        /**
         * Convert the time range to fall entirely within the time range of the layout table.
         */
        protected TimeRange adjustTimeRangeToLayoutTable(TimeRange eventTimeRange)
        {
            Time lowerBound = null, upperBound = null;

            //
            // Make sure that the upper/lower bounds fall within the layout table.
            //
            if (this.timeRange.firstTime().compareTo(eventTimeRange.firstTime()) > 0)
            {
                lowerBound = this.timeRange.firstTime();
            }
            else
            {
                lowerBound = eventTimeRange.firstTime();
            }

            if (this.timeRange.lastTime().compareTo(eventTimeRange.lastTime()) < 0)
            {
                upperBound = this.timeRange.lastTime();
            }
            else
            {
                upperBound = eventTimeRange.lastTime();
            }

            return timeService.newTimeRange(lowerBound, upperBound, true, false);
        }

        /**
         * Returns true if there are any events in this or other rows that overlap the event associated with this cell.
         */
        protected boolean cellHasOverlappingEvents(int rowNum, int colNum)
        {
            LayoutTableCell cell = this.getFirstCell(rowNum, colNum);

            // Start at the first cell of this event and check every row
            // to see if we find any cells in that row that are not empty
            // and are not one of ours.
            if (cell != null && !cell.isEmptyCell())
            {
                for (int i = cell.getFirstCellRow(); i < (cell.getFirstCellRow() + cell.getNumCellsInEvent()); i++)
                {
                    for (int j = 0; j < this.numCols; j++)
                    {
                        LayoutTableCell curCell = this.getCell(i, j);

                        if (curCell != null && !curCell.isEmptyCell() && curCell.getCalendarEvent() != cell.getCalendarEvent())
                        {
                            return true;
                        }
                    }
                }
            }

            return false;
        }

        /**
         * Get a particular cell. Returns a reference to the actual cell and not a copy.
         */
        protected LayoutTableCell getCell(int rowNum, int colNum)
        {
            if (rowNum < 0 || rowNum >= this.numRows || colNum < 0 || colNum >= this.numCols)
            {
                // Illegal cell indices
                return null;
            }
            else
            {
                ArrayList row = (ArrayList) rows.get(rowNum);
                return (LayoutTableCell) row.get(colNum);
            }
        }

        /**
         * Gets the first cell associated with the event that's stored at this row/column
         */
        protected LayoutTableCell getFirstCell(int rowNum, int colNum)
        {
            LayoutTableCell cell = this.getCell(rowNum, colNum);

            if (cell == null || cell.isEmptyCell())
            {
                return null;
            }
            else
            {
                return getCell(cell.getFirstCellRow(), cell.getFirstCellColumn());
            }
        }

        /**
         * Looks for a column where the whole event can be placed.
         */
        protected int getFreeColumn(int rowNum, int numberColumnsNeeded)
        {
            // Keep going through the columns until we hit one that has
            // enough empty cells to accomodate our event.
            for (int i = 0; i < this.numCols; i++)
            {
                boolean foundOccupiedCell = false;

                for (int j = rowNum; j < rowNum + numberColumnsNeeded; j++)
                {
                    LayoutTableCell cell = getCell(j, i);

                    if (cell == null)
                    {
                        // Out of range.
                        return -1;
                    }

                    if (!cell.isEmptyCell())
                    {
                        foundOccupiedCell = true;
                        break;
                    }
                }

                if (!foundOccupiedCell)
                {
                    return i;
                }
            }

            return -1;
        }

        /**
         * Creates a list of lists of lists. The outer list is a list of rows. Each row is a list of columns. Each column is a list of column values.
         */
        public List getLayoutRows()
        {
            List allRows = new ArrayList();

            // Scan all rows in the table.
            for (int mainRowIndex = 0; mainRowIndex < this.getNumRows(); mainRowIndex++)
            {
                // If we hit a starting row, then iterate through all rows of the
                // event group.
                if (isStartingRowOfGroup(mainRowIndex))
                {
                    LayoutRow newRow = new LayoutRow();
                    allRows.add(newRow);

                    int numRowsInGroup = getNumberRowsInEventGroup(mainRowIndex);

                    newRow.setRowTimeRange(getTimeRangeForEventGroup(mainRowIndex, numRowsInGroup));

                    for (int columnIndex = 0; columnIndex < this.getNumCols(); columnIndex++)
                    {
                        List columnList = new ArrayList();
                        boolean addedCell = false;

                        for (int eventGroupRowIndex = mainRowIndex; eventGroupRowIndex < mainRowIndex + numRowsInGroup; eventGroupRowIndex++)
                        {
                            LayoutTableCell cell = getCell(eventGroupRowIndex, columnIndex);

                            if (cell.isFirstCell())
                            {
                                columnList.add(cell.getCalendarEvent());
                                addedCell = true;
                            }
                        }

                        // Don't add to our list unless we actually added a cell.
                        if (addedCell)
                        {
                            newRow.add(columnList);
                        }
                    }

                    // Get ready for the next iteration. Skip those
                    // rows that we have already processed.
                    mainRowIndex += (numRowsInGroup - 1);
                }
            }

            return allRows;
        }

        protected int getNumberOfRowsNeeded(TimeRange eventTimeRange)
        {
            TimeRange adjustedTimeRange = adjustTimeRangeToLayoutTable(eventTimeRange);

            // Use the ceiling function to obtain the next highest integral number of time slots.
            return (int) (Math.ceil((double) (adjustedTimeRange.duration()) / (double) millisecondsPerTimeslot));
        }

        /**
         * Gets the number of rows in an event group. This function assumes that the row that it starts on is the starting row of the group.
         */
        protected int getNumberRowsInEventGroup(int rowNum)
        {
            int numEventRows = 0;

            if (isStartingRowOfGroup(rowNum))
            {
                numEventRows++;

                // Keep going unless we see an all empty row
                // or another starting row.
                for (int i = rowNum + 1; i < this.getNumRows() && !isEmptyRow(i) && !isStartingRowOfGroup(i); i++)
                {
                    numEventRows++;
                }
            }

            return numEventRows;
        }

        /**
         * Gets the total number of columns in the layout table.
         */
        public int getNumCols()
        {
            return this.numCols;
        }

        /**
         * Gets the total number of rows in the layout table.
         */
        public int getNumRows()
        {
            return rows.size();
        }

        /**
         * Given a time range, returns the starting row number in the layout table.
         */
        protected int getStartingRow(TimeRange eventTimeRange)
        {
            TimeRange adjustedTimeRange = adjustTimeRangeToLayoutTable(eventTimeRange);

            TimeRange timeRangeToStart = timeService.newTimeRange(this.timeRange.firstTime(), adjustedTimeRange.firstTime(), true,
                    true);

            //
            // We form a new time range where the ending time is the (adjusted) event
            // time range and the starting time is the starting time of the layout table.
            // The number of rows required for this range will be the starting row of the table.
            //
            return getNumberOfRowsNeeded(timeRangeToStart);
        }

        /**
         * Returns the earliest/latest times for events in this group. This function assumes that the row that it starts on is the starting row of the group.
         */
        public TimeRange getTimeRangeForEventGroup(int rowNum, int numRowsInThisEventGroup)
        {
            Time firstTime = null;
            Time lastTime = null;

            for (int i = rowNum; i < rowNum + numRowsInThisEventGroup; i++)
            {
                for (int j = 0; j < this.getNumCols(); j++)
                {
                    LayoutTableCell cell = getCell(i, j);
                    CalendarEvent event = cell.getCalendarEvent();

                    if (event != null)
                    {
                        TimeRange adjustedTimeRange = adjustTimeRangeToLayoutTable(roundRangeToMinimumTimeInterval(cell
                                .getCalendarEvent().getRange()));

                        //
                        // Replace our earliest time to date with the
                        // time from the event, if the time from the
                        // event is earlier.
                        //
                        if (firstTime == null)
                        {
                            firstTime = adjustedTimeRange.firstTime();
                        }
                        else
                        {
                            Time eventFirstTime = adjustedTimeRange.firstTime();

                            if (eventFirstTime.compareTo(firstTime) < 0)
                            {
                                firstTime = eventFirstTime;
                            }
                        }

                        //
                        // Replace our latest time to date with the
                        // time from the event, if the time from the
                        // event is later.
                        //
                        if (lastTime == null)
                        {
                            lastTime = adjustedTimeRange.lastTime();
                        }
                        else
                        {
                            Time eventLastTime = adjustedTimeRange.lastTime();

                            if (eventLastTime.compareTo(lastTime) > 0)
                            {
                                lastTime = eventLastTime;
                            }
                        }
                    }
                }
            }

            return timeService.newTimeRange(firstTime, lastTime, true, false);
        }

        /**
         * Returns true if this row has only empty cells.
         */
        protected boolean isEmptyRow(int rowNum)
        {
            boolean sawNonEmptyCell = false;

            for (int i = 0; i < this.getNumCols(); i++)
            {
                LayoutTableCell cell = getCell(rowNum, i);

                if (!cell.isEmptyCell())
                {
                    sawNonEmptyCell = true;
                    break;
                }
            }
            return !sawNonEmptyCell;
        }

        /**
         * Returns true if this row has only starting cells and no continuation cells.
         */
        protected boolean isStartingRowOfGroup(int rowNum)
        {
            boolean sawContinuationCells = false;
            boolean sawFirstCell = false;

            for (int i = 0; i < this.getNumCols(); i++)
            {
                LayoutTableCell cell = getCell(rowNum, i);

                if (cell.isContinuationCell())
                {
                    sawContinuationCells = true;
                }

                if (cell.isFirstCell)
                {
                    sawFirstCell = true;
                }
            }

            //
            // In order to be a starting row must have a "first"
            // cell no continuation cells.
            //
            return (!sawContinuationCells && sawFirstCell);
        }

        /**
         * Returns true if there are any cells in this row associated with events which overlap each other in this row or any other row.
         */
        public boolean rowHasOverlappingEvents(int rowNum)
        {
            for (int i = 0; i < this.getNumCols(); i++)
            {
                if (cellHasOverlappingEvents(rowNum, i))
                {
                    return true;
                }
            }

            return false;
        }
    }
    /**
     * This is a single cell in a layout table (an instance of SingleDayLayoutTable).
     */
    protected static class LayoutTableCell
    {
        protected CalendarEvent calendarEvent = null;

        protected int firstCellColumn = -1;

        protected int firstCellRow = -1;

        protected boolean isFirstCell = false;

        protected int numCellsInEvent = 0;

        protected int thisCellColumn = -1;

        protected int thisCellRow = -1;

        /**
         * Gets the calendar event associated with this cell.
         */
        public CalendarEvent getCalendarEvent()
        {
            return calendarEvent;
        }

        /**
         * Gets the first column associated with this cell.
         */
        public int getFirstCellColumn()
        {
            return firstCellColumn;
        }

        /**
         * Gets the first row associated with this cell.
         */
        public int getFirstCellRow()
        {
            return firstCellRow;
        }

        /**
         * Get the number of cells in this event.
         */
        public int getNumCellsInEvent()
        {
            return numCellsInEvent;
        }

        /**
         * Gets the column associated with this particular cell.
         */
        public int getThisCellColumn()
        {
            return thisCellColumn;
        }

        /**
         * Gets the row associated with this cell.
         */
        public int getThisCellRow()
        {
            return thisCellRow;
        }

        /**
         * Returns true if this cell is a continuation of an event and not the first cell in the event.
         */
        public boolean isContinuationCell()
        {
            return !isFirstCell() && !isEmptyCell();
        }

        /**
         * Returns true if this cell is not associated with any events.
         */
        public boolean isEmptyCell()
        {
            return calendarEvent == null;
        }

        /**
         * Returns true if this is the first cell in a column of cells associated with an event.
         */
        public boolean isFirstCell()
        {
            return isFirstCell;
        }

        /**
         * Set the calendar event associated with this cell.
         */
        public void setCalendarEvent(CalendarEvent event)
        {
            calendarEvent = event;
        }

        /**
         * Set flag indicating that this is the first cell in column of cells associated with an event.
         */
        public void setFirstCell(boolean b)
        {
            isFirstCell = b;
        }

        /**
         * Sets a value in this cell to point to the very first cell in the column of cells associated with this event.
         */
        public void setFirstCellColumn(int i)
        {
            firstCellColumn = i;
        }

        /**
         * Sets a value in this cell to point to the very first cell in the column of cells associated with this event.
         */
        public void setFirstCellRow(int i)
        {
            firstCellRow = i;
        }

        /**
         * Gets the number of cells (if any) in the group of cells associated with this cell by event.
         */
        public void setNumCellsInEvent(int i)
        {
            numCellsInEvent = i;
        }

        /**
         * Sets the actual column index for this cell.
         */
        public void setThisCellColumn(int i)
        {
            thisCellColumn = i;
        }

        /**
         * Sets the actual row index for this cell.
         */
        public void setThisCellRow(int i)
        {
            thisCellRow = i;
        }
    }
    /**
     ** Internal class for resolving stylesheet URIs
     **/
    protected static class MyURIResolver implements URIResolver
    {

        ClassLoader classLoader = null;

        /**
         ** Constructor: use BaseCalendarService ClassLoader
         **/
        public MyURIResolver( ClassLoader classLoader )
        {
            this.classLoader = classLoader;
        }
        /**
         ** Resolve XSLT pathnames invoked within stylesheet (e.g. xsl:import)
         ** using ClassLoader.
         **
         ** @param href href attribute of XSLT file
         ** @param base base URI in affect when href attribute encountered
         ** @return Source object for requested XSLT file
         **/
        public Source resolve( String href, String base )
                throws TransformerException
        {
            InputStream in = classLoader.getResourceAsStream(href);
            return (Source)(new StreamSource(in));
        }

    }

    /**
     * This is a hack to get fonts to load out of the classpath in both tests and production. Tomcat has a URI
     * handler for classpath: but this doesn't exist when running in tests.
     */
    private static final class ClassPathResolver implements ResourceResolver {
        @Override
        public OutputStream getOutputStream(URI uri) throws IOException {
            return getClass().getResource(uri.toString()).openConnection()
                    .getOutputStream();
        }

        @Override
        public Resource getResource(URI uri) throws IOException {
            InputStream inputStream = getClass().getResourceAsStream(uri.getPath());
            return new Resource(inputStream);
        }
    }

}
