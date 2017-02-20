package org.sakaiproject.lessonbuildertool.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class MnemeEntityTest {

    private MnemeEntity mnemeEntity;

    @Before
    public void setUp() {
        mnemeEntity = new MnemeEntity();
    }

    @Test
    public void testGetEntityGood() {
        // Expected format of entity
        LessonEntity entity = mnemeEntity.getEntity("/mneme/id");
        assertEquals("sakai.mneme", entity.getToolId());
        assertEquals("/mneme/id", entity.getReference());
    }

    @Test
    public void testGetEntityDummy() {
        // Some entries in the DB have a dummy reference.
        LessonEntity entity = mnemeEntity.getEntity("/dummy");
        assertNull(entity);
    }

    @Test
    public void testGetEntitySlash() {
        // Attempt to break parsing
        LessonEntity entity = mnemeEntity.getEntity("/");
        assertNull(entity);
    }

    @Test
    public void testGetEntityEmpty() {
        // Attempt to break parsing
        LessonEntity entity = mnemeEntity.getEntity("");
        assertNull(entity);
    }

    @Test
    public void testGetEntityJunk() {
        // Attempt to break parsing
        LessonEntity entity = mnemeEntity.getEntity("junkjunkjunk");
        assertNull(entity);
    }
}
