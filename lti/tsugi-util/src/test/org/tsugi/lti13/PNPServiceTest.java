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
        assertEquals(ps.pnp_supported_versions.size(),1);
        assertEquals(ps.pnp_supported_versions.get(0), ps.VERSION_1_0);

        assertEquals(ps.scope.size(),1);
        assertEquals(ps.scope.get(0), ps.SCOPE_PNP_READONLY);
        ps.pnp_settings_service_url = "https://www.myuniv.example.com/2344/groups";

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(ps);
        String jsonTest = "{\"scope\":[\"https://purl.imsglobal.org/spec/lti-pnp/scope/pnpsettings.readonly\"],\"pnp_settings_service_url\":\"https://www.myuniv.example.com/2344/groups\",\"pnp_supported_versions\":[\"http://purl.imsglobal.org/spec/afapnp/v1p0/schema/openapi/afapnpv1p0service_openapi3_v1p0\"]}";
        assertEquals(jsonString, jsonTest);
    }

    @Test
    public void testAddServiceVersion() {
        PNPService groupService = new PNPService();
        groupService.pnp_supported_versions.add("1.1");
        assertTrue(groupService.pnp_supported_versions.contains("1.1"));
    }

}
