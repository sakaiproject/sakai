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

import org.tsugi.jackson.JacksonUtil;
import org.tsugi.lti13.objects.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 *
 * @author csev
 */
public class GroupServiceTest {

    @Test
    public void testConstructor() throws com.fasterxml.jackson.core.JsonProcessingException {

        GroupService gs = new GroupService();
        assertNotNull(gs);
        assertEquals(gs.service_versions.size(),1);
        assertEquals(gs.service_versions.get(0), "1.0");

        assertEquals(gs.scope.size(),1);
        assertEquals(gs.scope.get(0), "https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly");
        gs.context_groups_url = "https://www.myuniv.example.com/2344/groups";
        gs.context_group_sets_url = "https://www.myuniv.example.com/2344/groups/sets";

        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(gs);
        String jsonTest = "{\"scope\":[\"https://purl.imsglobal.org/spec/lti-gs/scope/contextgroup.readonly\"],\"context_groups_url\":\"https://www.myuniv.example.com/2344/groups\",\"context_group_sets_url\":\"https://www.myuniv.example.com/2344/groups/sets\",\"service_versions\":[\"1.0\"]}";
        assertEquals(JacksonUtil.getHashMapFromJSONString(jsonString), JacksonUtil.getHashMapFromJSONString(jsonTest));
    }

    @Test
    public void testAddServiceVersion() {
        GroupService groupService = new GroupService();
        groupService.service_versions.add("1.1");
        assertTrue(groupService.service_versions.contains("1.1"));
    }

}
