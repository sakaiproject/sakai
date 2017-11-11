package org.sakaiproject.springframework.orm.hibernate.type;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;

public class InstantTypeDescriptor extends AbstractTypeDescriptor<Instant> {
    public static final InstantTypeDescriptor INSTANCE = new InstantTypeDescriptor();

    public InstantTypeDescriptor() {
        super(Instant.class, ImmutableMutabilityPlan.INSTANCE);
    }

    @Override
    public String toString(Instant value) {
        return DateTimeFormatter.ISO_INSTANT.format(value);
    }

    @Override
    public Instant fromString(String string) {
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(string));
    }

    @Override
    public <T> T unwrap(Instant instant, Class<T> type, WrapperOptions options) {
        if (instant == null) {
            return null;
        }

        if (Instant.class.isAssignableFrom(type)) {
            return (T) instant;
        }

        if (Calendar.class.isAssignableFrom(type)) {
            final ZoneId zoneId = ZoneId.ofOffset("UTC", ZoneOffset.UTC);
            return (T) GregorianCalendar.from(instant.atZone(zoneId));
        }

        if (java.sql.Timestamp.class.isAssignableFrom(type)) {
            return (T) Timestamp.from(instant);
        }

        if (java.sql.Date.class.isAssignableFrom(type)) {
            return (T) java.sql.Date.from(instant);
        }

        if (java.sql.Time.class.isAssignableFrom(type)) {
            return (T) java.sql.Time.from(instant);
        }

        if (java.util.Date.class.isAssignableFrom(type)) {
            return (T) Date.from(instant);
        }

        if (Long.class.isAssignableFrom(type)) {
            return (T) Long.valueOf(instant.toEpochMilli());
        }

        throw unknownUnwrap(type);
    }

    @Override
    public <T> Instant wrap(T value, WrapperOptions options) {
        if (value == null) {
            return null;
        }

        if (Instant.class.isInstance(value)) {
            return (Instant) value;
        }

        if (Timestamp.class.isInstance(value)) {
            final Timestamp ts = (Timestamp) value;
            return ts.toInstant();
        }

        if (Long.class.isInstance(value)) {
            return Instant.ofEpochMilli((Long) value);
        }

        if (Calendar.class.isInstance(value)) {
            final Calendar calendar = (Calendar) value;
            return ZonedDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId()).toInstant();
        }

        if (java.util.Date.class.isInstance(value)) {
            return ((java.util.Date) value).toInstant();
        }

        throw unknownWrap(value.getClass());
    }
}
