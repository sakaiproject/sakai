package org.adl.datamodels.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Collections;

import org.junit.Test;

public class DateTimeValidatorImplTest {

	private final DateTimeValidatorImpl validator = new DateTimeValidatorImpl();

	@Test
	public void compareEqualLocalDateTimes() {
		assertTrue(validator.compare("2012-07-26T12:00:00", "2012-07-26T12:00:00", Collections.emptyList()));
	}

	@Test
	public void compareIgnoresSubSecondPrecision() {
		assertTrue(validator.compare("2012-07-26T12:00:00.500", "2012-07-26T12:00:00", Collections.emptyList()));
	}

	@Test
	public void compareUnequalTimes() {
		assertFalse(validator.compare("2012-07-26T12:00:00", "2012-07-26T12:00:01", Collections.emptyList()));
	}

	@Test
	public void compareDateOnly() {
		assertTrue(validator.compare("2012-07-26", "2012-07-26", Collections.emptyList()));
	}

	@Test
	public void compareInvalidReturnsFalse() {
		assertFalse(validator.compare("not-a-date", "2012-07-26T12:00:00", Collections.emptyList()));
	}

	@Test
	public void compareNullReturnsFalse() {
		assertFalse(validator.compare(null, "2012-07-26T12:00:00", Collections.emptyList()));
		assertFalse(validator.compare("2012-07-26T12:00:00", null, Collections.emptyList()));
		assertFalse(validator.compare(null, null, Collections.emptyList()));
	}

	@Test
	public void parseToSecondPrecisionTruncatesNanos() {
		LocalDateTime parsed = DateTimeValidatorImpl.parseToSecondPrecision("2012-07-26T12:00:00.999");
		assertEquals(LocalDateTime.of(2012, 7, 26, 12, 0, 0), parsed);
	}

	@Test
	public void parseOffsetKeepsLocalFields() {
		LocalDateTime parsed = DateTimeValidatorImpl.parseToSecondPrecision("2012-07-26T12:00:00+02:00");
		assertEquals(LocalDateTime.of(2012, 7, 26, 12, 0, 0), parsed);
	}
}
