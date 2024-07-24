/**
 * Copyright (c) 2003-2018 The Apereo Foundation
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
package org.sakaiproject.calendar.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.sakaiproject.calendar.impl.readers.Reader;
import org.sakaiproject.exception.ImportException;
import org.sakaiproject.util.CalendarEventType;
import org.sakaiproject.util.ResourceLoader;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Slf4j
public class GenericImportRowHandler implements Reader.ReaderImportRowHandler {

    private ResourceLoader rb;
    @Getter
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
    public void handleRow(Iterator<Reader.ReaderImportCell> columnIterator) throws ImportException {
        final Map<String,Object> eventProperties = new HashMap<>();

        // Add all the properties to the map
        while (columnIterator.hasNext()) {
            Reader.ReaderImportCell column = columnIterator.next();

            String value = column.getCellValue().trim();
            Object mapCellValue = null;

            if (!value.isEmpty()) {
                if (frequencyColumn != null && frequencyColumn.equals(column.getColumnHeader())) {
                    mapCellValue = column.getCellValue();
                } else if (endTimeColumn != null && endTimeColumn.equals(column.getColumnHeader())
                        || (startTimeColumn != null && startTimeColumn.equals(column.getColumnHeader()))) {

                    try {
                        mapCellValue = LocalTime.parse(value, GenericCalendarImporter.timeFormatter());
                    } catch (Exception e) {
                        log.debug("Could not parse time [{}], {}", value, e);
                        try {
                            mapCellValue = LocalTime.parse(value, GenericCalendarImporter.time24HourFormatter());
                        } catch (Exception e1) {
                            log.debug("Could not parse time [{}], {}", value, e1);
                            String msg = rb.getFormattedMessage(
                                    "err_time",
                                    Integer.valueOf(column.getLineNumber()),
                                    column.getColumnHeader());
                            throw new ImportException(msg);
                        }
                    }
                } else if (durationTimeColumn != null && durationTimeColumn.equals(column.getColumnHeader())) {
                    String timeFormatErrorString = rb.getFormattedMessage(
                            "err_time",
                            Integer.valueOf(column.getLineNumber()),
                            column.getColumnHeader());

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
                    try {
                        mapCellValue = LocalDate.parse(value, GenericCalendarImporter.dateISOFormatter());
                    } catch (Exception e) {
                        try {
                            mapCellValue = LocalDate.parse(value, GenericCalendarImporter.dateMDYFormatter());
                        } catch (Exception e1) {
                            String msg = rb.getFormattedMessage("err_date",
                                    Integer.valueOf(column.getLineNumber()),
                                    column.getColumnHeader());
                            throw new ImportException(msg + ", " + e1);
                        }
                    }
                } else if (intervalColumn != null && intervalColumn.equals(column.getColumnHeader())
                        || repeatColumn != null && repeatColumn.equals(column.getColumnHeader())) {
                    try {
                        mapCellValue = Integer.valueOf(column.getCellValue());
                    } catch (NumberFormatException ex) {
                        String msg = rb.getFormattedMessage("err_interval",
                                Integer.valueOf(column.getLineNumber()),
                                column.getColumnHeader());
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
}
