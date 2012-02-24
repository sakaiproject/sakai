/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package edu.amc.sakai.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.TestCase;

public class ResourcePropertiesEditStubTest extends TestCase {

	public void testVerifiesPropertyCountInEqualityTest() {
		ResourcePropertiesEditStub stub1 = new ResourcePropertiesEditStub();
		ResourcePropertiesEditStub stub2 = new ResourcePropertiesEditStub();
		
		// add the same property to both objects
		stub1.addProperty("property-1", "value-1");
		stub2.addProperty("property-1", "value-1");
		
		// add one additional property to stub1
		stub1.addProperty("property-2", "value-1");
		
		// equality test should fail
		assertFalse("ResourcePropertiesEditStub should not have been considered equal", stub1.equals(stub2));
	}
	
	public void testVerifiesSingleValuedPropertyInEqualityTest() {
		ResourcePropertiesEditStub stub1 = new ResourcePropertiesEditStub();
		ResourcePropertiesEditStub stub2 = new ResourcePropertiesEditStub();
		
		// add the same property to both objects
		stub1.addProperty("property-1", "value-1");
		stub2.addProperty("property-1", "value-1");
		
		assertEquals("ResourcePropertiesEditStubs failed to compare single-valued properties", stub1, stub2);
	}
	
	public void testVerifiesMultiValuedPropertiesInEqualityTest() {
		ResourcePropertiesEditStub stub1 = new ResourcePropertiesEditStub();
		ResourcePropertiesEditStub stub2 = new ResourcePropertiesEditStub();
		
		// add the same property to both objects
		stub1.addPropertyToList("property-1", "value-1");
		stub1.addPropertyToList("property-1", "value-2");
		stub2.addPropertyToList("property-1", "value-1");
		stub2.addPropertyToList("property-1", "value-2");
		
		assertTrue("Expected List property type, but was: [" + stub1.get("property-1").getClass() + "]", 
				stub1.get("property-1") instanceof List); // sanity check
		assertTrue(((List)stub1.get("property-1")).size() == 2); // sanity check
		assertTrue("Expected List property type, but was: [" + stub1.get("property-1").getClass() + "]",
				stub2.get("property-1") instanceof List); // sanity check
		assertTrue(((List)stub2.get("property-1")).size() == 2); // sanity check
		assertEquals("ResourcePropertiesEditStubs failed to compare multi-valued properties", stub1, stub2);
	}
	
	public void testTreatsEmptyResourcePropertiesEditStubsAsEqual() {
		ResourcePropertiesEditStub stub1 = new ResourcePropertiesEditStub();
		ResourcePropertiesEditStub stub2 = new ResourcePropertiesEditStub();
		assertEquals("Empty ResourcePropertiesEditStubs should be considered equal", stub1, stub2);
	}
	
	public void testReplacesSingleValuedDefaultPropertiesWithOverrides() {
		Properties defaults = new Properties();
		Properties overrides = new Properties();
		
		defaults.setProperty("property-1", "value-1");
		overrides.setProperty("property-1", "value-2");
		
		// mainly interested in verifying that the override isn't simply
		// appended to the default value
		
		ResourcePropertiesEditStub stub = new ResourcePropertiesEditStub(defaults, overrides);
		assertEquals("value-2", stub.getProperty("property-1"));
	}
	
	public void testReplacesMultiValuedDefaultPropertiesWithOverrides() {
		Properties defaults = new Properties();
		Properties overrides = new Properties();
		
		defaults.setProperty("property-1", "value-1;value-2");
		overrides.setProperty("property-1", "value-3;value-4");
		
		// mainly interested in verifying that the override isn't simply
		// appended to the default value
		
		ResourcePropertiesEditStub stub = new ResourcePropertiesEditStub(defaults, overrides);
		List expectedValues = new ArrayList();
		expectedValues.add("value-3");
		expectedValues.add("value-4");
		
		assertTrue("Expected List property type, but was: [" + stub.get("property-1").getClass() + "]", 
				stub.get("property-1") instanceof List); // sanity check
		assertTrue(((List)stub.get("property-1")).size() == 2); // sanity check
		assertEquals(expectedValues, stub.get("property-1"));
	}
	
	public void testPreservesNonOverridenDefaultProperties() {
		
		Properties defaults = new Properties();
		Properties overrides = new Properties();
		
		// add a property to defaults only -- this property should be preserved
		defaults.setProperty("property-1", "value-1");
		
		// add the same property to both objects -- this property should be overriden
		defaults.setProperty("property-2", "value-2");
		overrides.setProperty("property-2", "value-3");
		
		ResourcePropertiesEditStub stub = new ResourcePropertiesEditStub(defaults, overrides);
		
		assertEquals("Should have preserved non-overriden property value", 
				"value-1", stub.getProperty("property-1"));
		
		// this is really just a sanity check
		assertEquals("Should have overriden property value", "value-3", stub.getProperty("property-2"));
	}
	
	public void testParsesMultiValuedDefaultProperties() {
		Properties defaults = new Properties();
		defaults.setProperty("property-1", "value-1;value-2");
		ResourcePropertiesEditStub stub = new ResourcePropertiesEditStub(defaults, null);
		List expectedValues = new ArrayList();
		expectedValues.add("value-1");
		expectedValues.add("value-2");
		assertEquals("Failed to parse multi-valued default properties into a List", expectedValues, 
				stub.get("property-1"));
	}
	
	public void testParsesMultiValuedOverrideProperties() {
		Properties overrides = new Properties();
		overrides.setProperty("property-1", "value-1;value-2");
		ResourcePropertiesEditStub stub = new ResourcePropertiesEditStub(null, overrides);
		List expectedValues = new ArrayList();
		expectedValues.add("value-1");
		expectedValues.add("value-2");
		assertEquals("Failed to parse multi-valued default properties into a List", expectedValues, 
				stub.get("property-1"));
	}
	
	public void testConsidersSingleValuedAndMultiValuedPropertiesUnequalEvenIfMultiValuedPropertyHasOnlyOneValue() {
		// this is important because the APIs for setting single- and multi-valued
		// properties are completely incompatible. e.g. if you have a single-valued
		// property, you can't just call addPropertyToList(String,String) and expect
		// the existing property to be converted to a multi-valued property.
		
		ResourcePropertiesEditStub stub1 = new ResourcePropertiesEditStub();
		ResourcePropertiesEditStub stub2 = new ResourcePropertiesEditStub();
		
		stub1.addProperty("property-1", "value-1");
		stub2.addPropertyToList("property-1", "value-1");
		
		assertFalse("Single and multi-valued properties are incompatible", stub1.equals(stub2));
		
	}
	
}
