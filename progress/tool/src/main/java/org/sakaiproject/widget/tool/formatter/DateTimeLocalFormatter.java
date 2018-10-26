package org.sakaiproject.widget.tool.formatter;

import org.springframework.format.Formatter;
import org.springframework.lang.UsesJava8;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

@UsesJava8
public class DateTimeLocalFormatter implements Formatter<Instant> {

    @Override
    public Instant parse(String text, Locale locale) throws ParseException {
        Objects.requireNonNull(text);
        if (text.length() > 0 && text.contains("T")) {
            // assuming ISO_LOCAL_DATE_TIME a la "2007-12-03T10:15:30"
            return LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.systemDefault()).toInstant();
        }
        return Instant.now();
    }

    @Override
    public String print(Instant object, Locale locale) {
        return object.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}