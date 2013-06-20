package org.imsglobal.lti2.objects;

import java.util.List;
import java.util.Collections;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;

import org.imsglobal.lti2.objects.*;

public class ToolConsumerTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void helloWorld() {
        System.out.println("Hello world");

        Product_family fam = new Product_family("SakaiCLE", "CLE", "Sakai Project",
            "Amazing open source Collaboration and Learning Environment.", 
            "http://www.sakaiproject.org", "support@sakaiproject.org");

        Product_info info = new Product_info("CTools", "4.0", "The Sakai installation for UMich", fam);

        Product_instance instance = new Product_instance("ctools-001", info, "support@ctools.umich.edu");

        ToolConsumer consumer = new ToolConsumer("00292902192", instance);
        List<Service_offered> services = consumer.getService_offered();
        services.add(StandardServices.LTI2Registration("about:blank"));
        services.add(StandardServices.LTI1Outcomes("about:blank"));
        List<String> capabilities = consumer.getCapability_enabled();
        Collections.addAll(capabilities,ToolConsumer.STANDARD_CAPABILITIES);

        ObjectMapper mapper = new ObjectMapper();
        try {
            // http://stackoverflow.com/questions/6176881/how-do-i-make-jackson-pretty-print-the-json-content-it-generates
            ObjectWriter writer = mapper.defaultPrettyPrintingWriter();
            // ***IMPORTANT!!!*** for Jackson 2.x use the line below instead of the one above: 
            // ObjectWriter writer = mapper.writer().withDefaultPrettyPrinter();
            System.out.println(writer.writeValueAsString(consumer));
            // System.out.println(mapper.writeValueAsString(consumer));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
