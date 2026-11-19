package org.adl.datamodels.datatypes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.TimeZone;

import org.adl.datamodels.DMErrorCodes;
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
	public void compareAcceptedPartialDates() {
		for (String value : new String[] {"2012", "2012-07", "2012-07-26T12"}) {
			assertEquals(DMErrorCodes.NO_ERROR, validator.validate(value));
			assertTrue(validator.compare(value, value, Collections.emptyList()));
		}
		assertTrue(validator.compare("2012", "2012-01-01", Collections.emptyList()));
		assertTrue(validator.compare("2012-07", "2012-07-01", Collections.emptyList()));
		assertTrue(validator.compare("2012-07-26T12", "2012-07-26T12:00:00", Collections.emptyList()));
	}

	@Test
	public void compareOffsetDateTimesByInstant() {
		TimeZone previous = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			assertTrue(validator.compare("2012-07-26T12:00:00.00+02:00", "2012-07-26T10:00:00.00Z", Collections.emptyList()));
			assertFalse(validator.compare("2012-07-26T12:00:00.00+02:00", "2012-07-26T12:00:00.00Z", Collections.emptyList()));
		} finally {
			TimeZone.setDefault(previous);
		}
	}

	@Test
	public void compareOffsetDateTimesAcrossDaylightSavingOverlap() {
		TimeZone previous = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
			assertFalse(validator.compare("2024-11-03T01:30:00.00-04:00", "2024-11-03T01:30:00.00-05:00", Collections.emptyList()));
			assertTrue(validator.compare("2024-11-03T01:30:00.00-04:00", "2024-11-03T05:30:00.00Z", Collections.emptyList()));
			assertTrue(validator.compare("2024-11-03T01:30:00.00", "2024-11-03T01:30:00.00-04:00", Collections.emptyList()));
			assertFalse(validator.compare("2024-11-03T01:30:00.00", "2024-11-03T01:30:00.00-05:00", Collections.emptyList()));
		} finally {
			TimeZone.setDefault(previous);
		}
	}

	@Test
	public void compareAcceptedBareHourOffsets() {
		String offsetDateTime = "2012-07-26T12:00:00.00+23";
		assertEquals(DMErrorCodes.NO_ERROR, validator.validate(offsetDateTime));
		assertTrue(validator.compare(offsetDateTime, "2012-07-25T13:00:00.00Z", Collections.emptyList()));
	}
}
