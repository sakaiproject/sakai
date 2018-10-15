package org.sakaiproject.rubrics.logic;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class RubricsLocalDateTimeSerializer extends LocalDateTimeSerializer {

    @Override
    public void serialize(LocalDateTime value, JsonGenerator g, SerializerProvider provider)
        throws IOException {

        DateTimeFormatterBuilder builder
            = new DateTimeFormatterBuilder()
                .appendText(ChronoField.MONTH_OF_YEAR)
                .appendLiteral(" ")
                .appendText(ChronoField.DAY_OF_MONTH)
                .appendLiteral(", ")
                .appendText(ChronoField.YEAR)
                .appendLiteral(" ")
                .appendText(ChronoField.CLOCK_HOUR_OF_AMPM)
                .appendLiteral(":")
                .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                .appendLiteral(" ")
                .appendText(ChronoField.AMPM_OF_DAY);

        DateTimeFormatter dtf = builder.toFormatter();
        g.writeString(value.format(dtf));
    }
}
