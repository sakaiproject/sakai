package org.apache.commons.jrcs.diff;

import org.apache.commons.jrcs.diff.myers.MyersDiff;

public class MyersDiffTests extends DiffTest {

    public MyersDiffTests(String name) {
        super(name, new MyersDiff());
    }


}
