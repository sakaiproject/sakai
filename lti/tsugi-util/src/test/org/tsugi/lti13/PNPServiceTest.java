/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tsugi.lti13;

import static org.junit.Assert.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.tsugi.lti13.objects.PNPService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 * @author csev
 */
public class PNPServiceTest {

    @Test
    public void testConstructor() throws com.fasterxml.jackson.core.JsonProcessingException {

        PNPService ps = new PNPService();
        assertNotNull(ps);

        assertEquals(ps.scope.size(),1);
        assertEquals(ps.scope.get(0), ps.SCOPE_PNP_READONLY);
        ps.afapnp_endpoint_url = "https://www.myuniv.example.com/2344/groups";

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(ps);
        String jsonTest = "{\"afapnp_endpoint_url\":\"https://www.myuniv.example.com/2344/groups\",\"scope\":[\"https://purl.imsglobal.org/spec/lti-afapnp/scope/afapnprecord.readonly\"]}";
        assertEquals(jsonString, jsonTest);
    }

}
