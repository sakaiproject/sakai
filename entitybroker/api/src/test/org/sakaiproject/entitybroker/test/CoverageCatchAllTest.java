/******************************************************************************
 * CoverageCatchAllTest.java - created by aaronz on Jul 25, 2007
 * 
 * Copyright (c) 2007 Centre for Academic Research in Educational Technologies
 * Licensed under the Educational Community License version 1.0
 * 
 * A copy of the Educational Community License has been included in this 
 * distribution and is available at: http://www.opensource.org/licenses/ecl1.php
 * 
 *****************************************************************************/

package org.sakaiproject.entitybroker.test;

import junit.framework.TestCase;

import org.sakaiproject.entitybroker.EntityReference;
import org.sakaiproject.entitybroker.IdEntityReference;
import org.sakaiproject.entitybroker.mocks.CoreEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.EntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ReferenceParseableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.ResolvableEntityProviderMock;
import org.sakaiproject.entitybroker.mocks.data.MyEntity;
import org.sakaiproject.entitybroker.mocks.data.TestData;

/**
 * this test class is simply here to make it easier to tell what REAL methods we have missed in the test coverage reports
 * by running all the getters and setters on classes so that the test coverage systems will not report these
 * getters and setters as untested and therefore throw off the tested code percentage
 * 
 * @author Aaron Zeckoski (aaronz@vt.edu)
 */
public class CoverageCatchAllTest extends TestCase {

    public void testEntityProviderMock() {
        // this also sucks, not as badly though -AZ
        EntityProviderMock ep = new EntityProviderMock(TestData.PREFIX1);
        assertEquals(TestData.PREFIX1, ep.getEntityPrefix() );
        ep.prefix = TestData.PREFIX2;
    }

    public void testCoreEntityProviderMock() {
        // this also sucks, not as badly though -AZ
        CoreEntityProviderMock cep = new CoreEntityProviderMock(TestData.PREFIX1, TestData.IDS1);
        assertNotNull(cep);
        assertTrue(cep.entityExists(TestData.IDS1[0]));
        assertFalse(cep.entityExists(null));
        CoreEntityProviderMock cep1 = new CoreEntityProviderMock(TestData.PREFIX1);
        cep1.ids = TestData.IDS3;
    }

    public void testReferenceParseableEntityProviderMock() {
        // this also sucks, not as badly though -AZ
        ReferenceParseableEntityProviderMock ep = new ReferenceParseableEntityProviderMock(TestData.PREFIX1, TestData.IDS1);
        assertNotNull(ep);
        assertEquals(TestData.PREFIX1, ep.getParsedExemplar().prefix);
    }

    public void testResolvableEntityProviderMock() {
        // this also sucks, not as badly though -AZ
        ResolvableEntityProviderMock ep = new ResolvableEntityProviderMock(TestData.PREFIX1, TestData.IDS1);
        assertNotNull(ep);
        EntityReference er = new IdEntityReference(TestData.PREFIX1, TestData.IDS1[0]);
        assertTrue(ep.getEntity(er) instanceof MyEntity);
    }

    public void testMyEntity() {
        // this also sucks, not as badly though -AZ
        MyEntity me = new MyEntity(TestData.IDS1[0]);
        me.id = TestData.IDS1[1];
    }

    public void testTestData() {
        // this sucks a whole lot -AZ
        TestData td = new TestData();
        assertEquals(TestData.PREFIX1, td.entityProvider1.getEntityPrefix() );
    }

}
