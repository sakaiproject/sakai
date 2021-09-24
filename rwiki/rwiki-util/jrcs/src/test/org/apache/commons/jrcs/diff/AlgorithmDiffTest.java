package org.apache.commons.jrcs.diff;

import org.junit.Test;

public class AlgorithmDiffTest {

    @Test
    public void MyersTest() {
        new MyersDiffTests();
    }

    @Test
    public void SimpleTest() {
        new SimpleDiffTests();
    }
}
