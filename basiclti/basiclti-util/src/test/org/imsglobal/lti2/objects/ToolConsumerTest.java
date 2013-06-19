package org.imsglobal.lti2.objects;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import org.imsglobal.lti2.objects.*;

public class ToolConsumerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void helloWorld() {
        System.out.println("Hello world");
        ToolConsumer x = new ToolConsumer();
        x.setGuid("Fred");
        ObjectMapper mapper = new ObjectMapper();
        try {
            System.out.println(mapper.writeValueAsString(x));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
