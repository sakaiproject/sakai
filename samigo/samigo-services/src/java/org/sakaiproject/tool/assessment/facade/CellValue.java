package org.sakaiproject.tool.assessment.facade;

import java.util.Date;
import java.util.Objects;

/**
 * A spreadsheet cell value. Each concrete variant fixes its own Java type, so pattern-matching
 * to a variant (e.g. {@code instanceof CellValue.LongValue lv}) recovers a properly typed
 * value via {@link #value()} with no cast required.
 */
public sealed interface CellValue<T> {

    T value();

    record BooleanValue(Boolean value) implements CellValue<Boolean> {
        public BooleanValue {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    record IntegerValue(Integer value) implements CellValue<Integer> {
        public IntegerValue {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    record LongValue(Long value) implements CellValue<Long> {
        public LongValue {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    record DoubleValue(Double value) implements CellValue<Double> {
        public DoubleValue {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    record DateValue(Date value) implements CellValue<Date> {
        public DateValue {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    record StringValue(String value) implements CellValue<String> {
        public StringValue {
            Objects.requireNonNull(value, "value cannot be null");
        }
    }

    static CellValue<Boolean> FALSE() {
        return new BooleanValue(Boolean.FALSE);
    }

    static CellValue<Boolean> TRUE() {
        return new BooleanValue(Boolean.TRUE);
    }

    static CellValue<Integer> INTEGER(Integer value) {
        return new IntegerValue(value);
    }

    static CellValue<Long> LONG(Long value) {
        return new LongValue(value);
    }

    static CellValue<Double> DOUBLE(Double value) {
        return new DoubleValue(value);
    }

    static CellValue<Date> DATE(Date value) {
        return new DateValue(value);
    }

    static CellValue<String> STRING(String value) {
        return new StringValue(value);
    }

    static CellValue<String> EMPTY() {
        return new StringValue("");
    }
}
