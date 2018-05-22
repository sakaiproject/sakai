package org.sakaiproject.calendar.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.sakaiproject.calendar.impl.readers.CSVReader;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.time.api.TimeService;
import org.sakaiproject.util.ResourceLoader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CSVReaderTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private CSVReader csvReader;

    @Mock
    private TimeService timeService;

    @Mock
    private ResourceLoader resourceLoader;

    @Before
    public void setUp() {
        csvReader = new CSVReader();
        csvReader.setTimeService(timeService);
    }

    @Test
    public void testExampleImport() throws ImportException {
        Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        GenericImportRowHandler genericImportRowHandler = new GenericImportRowHandler(csvReader.getDefaultColumnMap(), resourceLoader);
        InputStream in = getClass().getResourceAsStream("/example_import_file.csv");
        csvReader.importStreamFromDelimitedFile(in, genericImportRowHandler);
        List<Map<String, Object>> rowList = genericImportRowHandler.getRowList();
        Assert.assertEquals(7, rowList.size());
        {
            Map<String, Object> row = rowList.get(0);
            Assert.assertEquals("Example Event 1", row.get(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER));
            Assert.assertEquals("Activity", row.get(GenericCalendarImporter.ITEM_TYPE_DEFAULT_COLUMN_HEADER));
        }
        {
            Map<String, Object> row = rowList.get(1);
            Assert.assertEquals("Example Event 2", row.get(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER));
            Assert.assertEquals("Exam", row.get(GenericCalendarImporter.ITEM_TYPE_DEFAULT_COLUMN_HEADER));
        }
    }

    @Test
    public void testFailedImport() throws ImportException {
        GenericImportRowHandler genericImportRowHandler = new GenericImportRowHandler(csvReader.getDefaultColumnMap(), resourceLoader);
        InputStream in = new ByteArrayInputStream("this does not parse correctly".getBytes());
        csvReader.importStreamFromDelimitedFile(in, genericImportRowHandler);
        List<Map<String, Object>> rowList = genericImportRowHandler.getRowList();
        Assert.assertTrue(rowList.isEmpty());
    }

    @Test
    public void testActivityAliases() throws ImportException {
        // When importing you can use the import alias for the event type.
        Mockito.when(resourceLoader.getLocale()).thenReturn(Locale.ENGLISH);
        GenericImportRowHandler genericImportRowHandler = new GenericImportRowHandler(csvReader.getDefaultColumnMap(), resourceLoader);
        InputStream in = new ByteArrayInputStream((
                "Title, Description, Date, Start, Duration,Type\n" +
                "Event, Description , 5/2/2004, 11:00 AM, 1:00, event.cancellation"
        ).getBytes());
        csvReader.importStreamFromDelimitedFile(in, genericImportRowHandler);
        List<Map<String, Object>> rowList = genericImportRowHandler.getRowList();
        Assert.assertEquals(1, rowList.size());
        {
            Map<String, Object> row = rowList.get(0);
            Assert.assertEquals("Event", row.get(GenericCalendarImporter.TITLE_DEFAULT_COLUMN_HEADER));
            Assert.assertEquals("Cancellation", row.get(GenericCalendarImporter.ITEM_TYPE_DEFAULT_COLUMN_HEADER));
        }
    }

}
