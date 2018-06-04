package org.sakaiproject.calendar.impl;

import org.sakaiproject.calendar.impl.readers.Reader;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.ResourceLoader;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

class GenericImportRowHandler implements Reader.ReaderImportRowHandler {

    private ResourceLoader rb;
    private final List<Map<String,Object>> rowList;

    String frequencyColumn;
    String startTimeColumn;
    String endTimeColumn;
    String durationTimeColumn;
    String dateColumn;
    String endsColumn;
    String intervalColumn;
    String repeatColumn;

    public GenericImportRowHandler(Map<String, String> columnMap, ResourceLoader rb) {
        this.rb = rb;
        this.rowList = new ArrayList<>();
        frequencyColumn = columnMap.get(GenericCalendarImporter.FREQUENCY_DEFAULT_COLUMN_HEADER);
        startTimeColumn = columnMap.get(GenericCalendarImporter.START_TIME_DEFAULT_COLUMN_HEADER);
        endTimeColumn = columnMap.get(GenericCalendarImporter.END_TIME_DEFAULT_COLUMN_HEADER);
        durationTimeColumn = columnMap.get(GenericCalendarImporter.DURATION_DEFAULT_COLUMN_HEADER);
        dateColumn = columnMap.get(GenericCalendarImporter.DATE_DEFAULT_COLUMN_HEADER);
        endsColumn = columnMap.get(GenericCalendarImporter.ENDS_DEFAULT_COLUMN_HEADER);
        intervalColumn = columnMap.get(GenericCalendarImporter.INTERVAL_DEFAULT_COLUMN_HEADER);
        repeatColumn = columnMap.get(GenericCalendarImporter.REPEAT_DEFAULT_COLUMN_HEADER);
    }

    // This is the callback that is called for each row.
    public void handleRow(Iterator columnIterator) throws ImportException {
        final Map<String,Object> eventProperties = new HashMap<>();

        // Add all the properties to the map
        while (columnIterator.hasNext()) {
            Reader.ReaderImportCell column = (Reader.ReaderImportCell) columnIterator.next();

            String value = column.getCellValue().trim();
            Object mapCellValue = null;

            // First handle any empy columns.
            if (value.length() == 0) {
                mapCellValue = null;
            } else {
                if (frequencyColumn != null && frequencyColumn.equals(column.getColumnHeader())) {
                    mapCellValue = column.getCellValue();
                } else if (endTimeColumn != null && endTimeColumn.equals(column.getColumnHeader())
                        || (startTimeColumn != null && startTimeColumn.equals(column.getColumnHeader()))) {
                    boolean success = false;

                    try {
                        mapCellValue = GenericCalendarImporter.timeFormatter().parse(value);
                        success = true;
                    } catch (ParseException e) {
                        // Try another format
                    }

                    if (!success) {
                        try {
                            mapCellValue = GenericCalendarImporter.timeFormatterWithSeconds().parse(value);
                            success = true;
                        } catch (ParseException e) {
                            // Try another format
                        }
                    }

                    if (!success) {
                        try {
                            mapCellValue = GenericCalendarImporter.time24HourFormatter().parse(value);
                            success = true;
                        } catch (ParseException e) {
                            // Try another format
                        }
                    }

                    if (!success) {
                        try {
                            mapCellValue = GenericCalendarImporter.time24HourFormatterWithSeconds().parse(value);
                            success = true;
                        } catch (ParseException e) {
                            // Give up, we've run out of possible formats.
                            String msg = (String) rb.getFormattedMessage(
                                    "err_time",
                                    new Object[]{Integer.valueOf(column.getLineNumber()),
                                            column.getColumnHeader()});
                            throw new ImportException(msg);
                        }
                    }
                } else if (durationTimeColumn != null && durationTimeColumn.equals(column.getColumnHeader())) {
                    String timeFormatErrorString = (String) rb.getFormattedMessage(
                            "err_time",
                            new Object[]{Integer.valueOf(column.getLineNumber()),
                                    column.getColumnHeader()});

                    String parts[] = value.split(":");

                    if (parts.length == 1) {
                        // Convert to minutes to get into one property field.
                        try {
                            mapCellValue = Integer.valueOf(Integer.parseInt(parts[0]));
                        } catch (NumberFormatException ex) {
                            throw new ImportException(timeFormatErrorString);
                        }
                    } else if (parts.length == 2) {
                        // Convert to hours:minutes to get into one property field.
                        try {
                            mapCellValue = Integer.valueOf(Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]));
                        } catch (NumberFormatException ex) {
                            throw new ImportException(timeFormatErrorString);
                        }
                    } else {
                        // Not a legal format of mm or hh:mm
                        throw new ImportException(timeFormatErrorString);
                    }
                } else if (dateColumn != null && dateColumn.equals(column.getColumnHeader())
                        || (endsColumn != null && endsColumn.equals(column.getColumnHeader()))) {
                    DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, rb.getLocale());
                    df.setLenient(false);
                    try {
                        mapCellValue = df.parse(value);
                    } catch (ParseException e) {
                        String msg = rb.getFormattedMessage("err_date",
                                new Object[]{Integer.valueOf(column.getLineNumber()),
                                        column.getColumnHeader()});
                        throw new ImportException(msg);
                    }
                } else if (intervalColumn != null && intervalColumn.equals(column.getColumnHeader())
                        || repeatColumn != null && repeatColumn.equals(column.getColumnHeader())) {
                    try {
                        mapCellValue = Integer.valueOf(column.getCellValue());
                    } catch (NumberFormatException ex) {
                        String msg = rb.getFormattedMessage("err_interval",
                                new Object[]{Integer.valueOf(column.getLineNumber()),
                                        column.getColumnHeader()});
                        throw new ImportException(msg);
                    }
                } else if (GenericCalendarImporter.ITEM_TYPE_PROPERTY_NAME.equals(column.getPropertyName())) {
                    String cellValue = column.getCellValue();
                    if (cellValue != null) {
                        mapCellValue = CalendarEventType.getEventTypeFromImportType(cellValue);
                    }
                    if (mapCellValue == null) {
                        mapCellValue = cellValue;
                    }
                } else {
                    // Just a string...
                    mapCellValue = column.getCellValue();
                }
            }

            // Store in the map for later reference.
            eventProperties.put(column.getColumnHeader(), mapCellValue);
        }

        // Add the map of properties for this row to the list of rows.
        rowList.add(eventProperties);
    }

    public List<Map<String,Object>> getRowList() {
        return rowList;
    }
}
