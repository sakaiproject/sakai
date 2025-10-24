package org.adl.sequencer.impl;

import junit.framework.TestCase;
import org.adl.sequencer.IDuration;

public class ADLDurationTest extends TestCase {

    public void testSchemaDurationStoresSeconds() {
        ADLDuration duration = new ADLDuration(IDuration.FORMAT_SCHEMA, "PT1H2M3S");

        assertEquals(3723L, duration.mDuration);
        assertEquals("PT1H2M3S", duration.format(IDuration.FORMAT_SCHEMA));
        assertEquals("3723.0", duration.format(IDuration.FORMAT_SECONDS));
    }

    public void testLegacySecondBasedDurationsRemainCompatible() {
        ADLDuration legacy = new ADLDuration();
        legacy.mDuration = 3661L; // legacy persisted seconds

        ADLDuration fresh = new ADLDuration(IDuration.FORMAT_SCHEMA, "PT1H1M1S");

        assertEquals("PT1H1M1S", legacy.format(IDuration.FORMAT_SCHEMA));
        assertEquals("PT1H1M1S", fresh.format(IDuration.FORMAT_SCHEMA));
        assertEquals("3661.0", legacy.format(IDuration.FORMAT_SECONDS));
        assertEquals(IDuration.EQ, legacy.compare(fresh));
    }
}
