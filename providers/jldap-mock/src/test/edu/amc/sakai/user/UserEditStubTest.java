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

import junit.framework.TestCase;

public class UserEditStubTest extends TestCase {

	public void testConsidersUnconfiguredInstancesEqual() {
		UserEditStub user1 = new UserEditStub();
		UserEditStub user2 = new UserEditStub();
		assertEquals("Should consider unconfigured UserEditStubs equal", user1, user2);
	}
	
	public void testIgnoresPasswordAttributeWhenTestingEquality() {
		UserEditStub user1 = new UserEditStub();
		UserEditStub user2 = new UserEditStub();
		
		user1.setPassword("12345");
		user2.setPassword("67890");
		
		assertEquals("Should not consider passwords when testing UserEditStub equality", user1, user2);
	}
	
	public void testIgnoresLoginAttributeWhenTestingEquality() {
		UserEditStub user1 = new UserEditStub();
		UserEditStub user2 = new UserEditStub();
		
		user1.setLogin("12345");
		user2.setLogin("67890");
		
		assertEquals("Should not consider logins when testing UserEditStub equality", user1, user2);
	}
	
	public void testConsidersUdpSettableUserAttributesAndResourcePropertiesWhenTestingEquality() {
		UserEditStub user1 = new UserEditStub();
		UserEditStub user2 = new UserEditStub();
		
		user1.setEid("eid-1");
		user1.setEmail("email-1");
		user1.setFirstName("firstName-1");
		user1.setLastName("lastName-1");
		user1.setType("type-1");
		ResourcePropertiesEditStub user1Props = new ResourcePropertiesEditStub();
		user1Props.addProperty("property-1", "property-value-1");
		user1.setPropertiesEdit(user1Props);
		
		user2.setEid("eid-1");
		user2.setEmail("email-1");
		user2.setFirstName("firstName-1");
		user2.setLastName("lastName-1");
		user2.setType("type-1");
		ResourcePropertiesEditStub user2Props = new ResourcePropertiesEditStub();
		user2Props.addProperty("property-1", "property-value-1");
		user2.setPropertiesEdit(user2Props);
		
		assertEquals(user1, user2);
		
	}
	
	public void testConsidersUdpSettableUserAttributesAndResourcePropertiesWhenTestingEquality_NegativeVariation() {
		UserEditStub user1 = new UserEditStub();
		UserEditStub user2 = new UserEditStub();
		
		user1.setEid("eid-1");
		user1.setEmail("email-1");
		user1.setFirstName("firstName-1");
		user1.setLastName("lastName-1");
		user1.setType("type-1");
		ResourcePropertiesEditStub user1Props = new ResourcePropertiesEditStub();
		user1Props.addProperty("property-1", "property-value-1");
		user1.setPropertiesEdit(user1Props);
		
		user2.setEid("eid-2");
		user2.setEmail("email-2");
		user2.setFirstName("firstName-2");
		user2.setLastName("lastName-2");
		user2.setType("type-2");
		ResourcePropertiesEditStub user2Props = new ResourcePropertiesEditStub();
		user2Props.addProperty("property-2", "property-value-2");
		user2.setPropertiesEdit(user2Props);
		
		assertFalse(user1.equals(user2));
		
	}
	
}
