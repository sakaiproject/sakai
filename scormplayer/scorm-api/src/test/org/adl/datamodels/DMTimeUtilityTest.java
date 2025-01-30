package org.adl.datamodels;

import junit.framework.TestCase;

public class DMTimeUtilityTest extends TestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testParsing() {

		// P[yY][mM][dD][T[hH][mM][s[.s]S] 

		assertEquals("", DMTimeUtility.add("BANANA", "BANANA"));
		assertEquals("", DMTimeUtility.add("BANANA", "PT"));
		assertEquals("", DMTimeUtility.add("", ""));
		assertEquals("", DMTimeUtility.add("", "PT"));
		assertEquals("", DMTimeUtility.add("PT", ""));
		assertEquals("", DMTimeUtility.add("P", "PT"));
		assertEquals("", DMTimeUtility.add("PT", "PT"));
		assertEquals("", DMTimeUtility.add("", "PYMDTHMS"));
		assertEquals("", DMTimeUtility.add("", "PYMDTHM"));
		assertEquals("", DMTimeUtility.add("", "PYMDTH"));
		assertEquals("", DMTimeUtility.add("", "PYMDT"));
		assertEquals("", DMTimeUtility.add("", "PYMD"));
		assertEquals("", DMTimeUtility.add("", "PYM"));
		assertEquals("", DMTimeUtility.add("", "PY"));
		assertEquals("", DMTimeUtility.add("", "P"));
		assertEquals("", DMTimeUtility.add("", "PTHMS"));
		assertEquals("", DMTimeUtility.add("", "PTHM"));
		assertEquals("", DMTimeUtility.add("", "PTH"));

		assertEquals("", DMTimeUtility.add("P0YT", "PT"));
		assertEquals("", DMTimeUtility.add("P0Y0M0DT0H0M0S", "PT"));
		assertEquals("", DMTimeUtility.add("P0Y0M0DT0H0M0.0S", "PT"));

		assertEquals("P1Y", DMTimeUtility.add("P1YT", "PT"));
		assertEquals("P1Y", DMTimeUtility.add("P1YT0M", "PT"));
		assertEquals("P1Y", DMTimeUtility.add("P1YM", "PT"));
		assertEquals("P1Y", DMTimeUtility.add("P1YT0M0S", "PT"));
		assertEquals("P1Y", DMTimeUtility.add("P1YMD", "PT"));
		assertEquals("P1Y1M", DMTimeUtility.add("P1Y1MD", "PT"));
		assertEquals("P1Y1M1D", DMTimeUtility.add("P1Y1M1D", "PT"));
		assertEquals("P1M1D", DMTimeUtility.add("P1M1D", "PT"));
		assertEquals("P1D", DMTimeUtility.add("P1D", "PT"));
		assertEquals("P1D", DMTimeUtility.add("P1DT", "PT"));
		assertEquals("P1D", DMTimeUtility.add("P1DT", "PT"));

		assertEquals("PT3H", DMTimeUtility.add("PT3H", "PT"));
		assertEquals("PT5H", DMTimeUtility.add("PT000005H", "PT"));

		assertEquals("PT5M", DMTimeUtility.add("PT5M", "PT"));
		assertEquals("PT5M", DMTimeUtility.add("PT0005M", "PT"));
		assertEquals("PT2H5M", DMTimeUtility.add("PT2H5M", "PT"));
		assertEquals("PT2H5M", DMTimeUtility.add("PT0002H0005M", "PT"));

		assertEquals("PT5S", DMTimeUtility.add("PT5S", "PT"));
		assertEquals("PT5S", DMTimeUtility.add("PT00005S", "PT"));
		assertEquals("PT15S", DMTimeUtility.add("PT15S", "PT"));
		assertEquals("PT15S", DMTimeUtility.add("PT00015S", "PT"));
		assertEquals("PT2H5M15S", DMTimeUtility.add("PT2H5M15S", "PT"));
		assertEquals("PT2H5M15S", DMTimeUtility.add("PT0002H0005M0015S", "PT"));

		assertEquals("P1Y3M2DT3H", DMTimeUtility.add("P1Y3M2DT3H", "PT0H0M"));
		assertEquals("P1Y3M2DT3H", DMTimeUtility.add("P1Y3M2DT3H", "PT0H0M"));

		assertEquals("P1DT30M44.10S", DMTimeUtility.add("P1DT30M44.10S", "PT"));

	}

	public void testParsingPermuating() {
		for (int second = 0; second <= 59; second++) {
			for (int minute = 0; minute <= 59; minute++) {
				for (int hour = 0; hour <= 23; hour++) {
					for (int day = 0; day < 2; day++) {
						for (int month = 0; month < 2; month++) {
							for (int year = 0; year < 2; year++) {

								String value = buildValue(second, minute, hour, day);
								assertEquals(value, DMTimeUtility.add(value, ""));
							}
						}

					}
				}
			}

		}
	}

	protected String buildValue(int second, int minute, int hour, int day) {
	    String value = "P";
	    value += (day > 0 ? day + "D" : "");
	    value += (hour != 0 || minute != 0 || second != 0 ? "T" : "");
	    value += (hour > 0 ? hour + "H" : "");
	    value += (minute > 0 ? minute + "M" : "");
	    value += (second > 0 ? second + "S" : "");
	    if ("P".equals(value)) {
	    	value = "";
	    }
	    return value;
    }

	public void testIllegal() {
		// f fractions of a second are used, SCORM further restricts the string to
		// a maximum of 2 digits (e.g., 34.45 – valid, 34.45454545 – not valid).
		assertEquals("P1DT30M44.10S", DMTimeUtility.add("P1DT30M44.10S", "PT"));
		assertEquals("", DMTimeUtility.add("P1DT30M44.100000S", "PT"));
	}
	
	public void testDateAddition() {
		assertEquals("P2Y", DMTimeUtility.add("P1Y", "P1Y"));
		assertEquals("P200Y", DMTimeUtility.add("P100Y", "P100Y"));
		assertEquals("P2Y1M", DMTimeUtility.add("P1Y1M", "P1Y"));
		assertEquals("P2Y1M", DMTimeUtility.add("P1Y", "P1Y1M"));
		assertEquals("P200Y2M", DMTimeUtility.add("P100Y2M", "P100Y"));
		assertEquals("P200Y4M", DMTimeUtility.add("P100Y2M", "P100Y2M"));
		assertEquals("P200Y4M1D", DMTimeUtility.add("P100Y2M1D", "P100Y2M"));
		assertEquals("P200Y4M1D", DMTimeUtility.add("P100Y2M", "P100Y2M1D"));
		assertEquals("P100Y2M1D", DMTimeUtility.add("P100Y2M", "P1D"));
	}
	
	public void testAddTime() {
		assertEquals("PT23H", DMTimeUtility.add("PT13H", "PT10H"));
		assertEquals("P1D", DMTimeUtility.add("PT14H", "PT10H"));
		for (int second1 = 1; second1 <= (60*60*25); second1 += 100) {
			for (int second2 = 1; second2 <= (60*60*25*7); second2 += 10000) {
				String one = buildTime(second1);
				String two = buildTime(second2);
				String expected = buildTime(second1 + second2);
				assertEquals(expected, DMTimeUtility.add(one, two));
				assertEquals(expected, DMTimeUtility.add(two, one));
			}
		}
	}
	
	public void testNormalizeSeconds() {
		for (int second = 1; second <= (60*60*25); second++) {
			
			String expected = buildTime(second);
			String value = "PT" + second + "S";
			assertEquals(expected, DMTimeUtility.add(value, "PT"));
		}
	}

	protected String buildTime(int second) {
	    int sec = second; // BEWARE, do not change the actual value of second
	    int minute = 0;
	    int hour = 0;
	    int day = 0;
	    if (sec > 59) {
	    	minute = (sec / 60);
	    	sec = sec % 60;
	    }
	    if (minute > 59) {
	    	hour = (minute / 60);
	    	minute = minute % 60;
	    }
	    if (hour > 23) {
	    	day = (hour / 24);
	    	hour = hour % 24;
	    }
	    String expected = buildValue(sec, minute, hour, day);
	    return expected;
    }

	public void testNormalizeMinutes() {
		for (int minute = 1; minute <= (60*25); minute++) {
			
			int min = minute;
			int hour = 0;
			int day = 0;
			if (min > 59) {
				hour = (min / 60);
				min = min % 60;
			}
			if (hour > 23) {
				day = (hour / 24);
				hour = hour % 24;
			}
			String value = "PT" + minute + "M";
			String expected = buildValue(0, min, hour, day);
			assertEquals(expected, DMTimeUtility.add(value, "PT"));
		}
	}
	
	public void testNormalizeHours() {
		for (int hour = 1; hour <= (25); hour++) {
			
			int hr = hour;
			int day = 0;
			if (hr > 23) {
				day = (hr / 24);
				hr = hr % 24;
			}
			String value = "PT" + hour + "H";
			String expected = buildValue(0, 0, hr, day);
			assertEquals(expected, DMTimeUtility.add(value, "PT"));
		}
	}
	
}
