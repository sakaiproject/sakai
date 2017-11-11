/**
 * Copyright (c) 2003-2017 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
