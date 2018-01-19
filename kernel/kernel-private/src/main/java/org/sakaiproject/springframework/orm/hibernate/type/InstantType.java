package org.sakaiproject.springframework.orm.hibernate.type;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Locale;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.compare.ComparableComparator;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.LiteralType;
import org.hibernate.type.VersionType;
import org.hibernate.type.descriptor.sql.TimestampTypeDescriptor;

public class InstantType extends AbstractSingleColumnStandardBasicType<Instant> implements VersionType<Instant>, LiteralType<Instant> {
    public static final InstantType INSTANCE = new InstantType();

    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S 'Z'", Locale.ENGLISH);

    public InstantType() {
        super(TimestampTypeDescriptor.INSTANCE, InstantTypeDescriptor.INSTANCE);
    }

    @Override
    public String objectToSQLString(Instant value, Dialect dialect) throws Exception {
        return "{ts '" + FORMATTER.format(ZonedDateTime.ofInstant(value, ZoneId.of("UTC"))) + "'}";
    }

    @Override
    public Instant seed(SessionImplementor session) {
        return Instant.now();
    }

    @Override
    public Instant next(Instant current, SessionImplementor session) {
        return Instant.now();
    }

    @Override
    public Comparator<Instant> getComparator() {
        return ComparableComparator.INSTANCE;
    }

    @Override
    public String getName() {
        return Instant.class.getSimpleName();
    }

    @Override
    protected boolean registerUnderJavaType() {
        return true;
    }
}
