/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 Sakai Foundation
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

package org.sakaiproject.content.impl.serialize.impl.test;

import org.junit.Assert;
import org.junit.Test;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer;
import org.sakaiproject.content.impl.serialize.impl.Type1BaseContentResourceSerializer;
import org.sakaiproject.entity.api.serialize.EntityParseException;

public class Type1BaseContentCollectionSerializerTest
{
	/**
	 * Test method for {@link org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer#parse(org.sakaiproject.entity.api.serialize.SerializableEntity, java.lang.String)}.
	 * @throws Exception 
	 */
	@Test
	public final void testParse() throws Exception
	{
		Type1BaseContentCollectionSerializer t1 = new Type1BaseContentCollectionSerializer();
		t1.setTimeService(new MockTimeService());
		MockSerializableCollectionAcccess sc = new MockSerializableCollectionAcccess();
		byte[] serialized = t1.serialize(sc);
		t1.parse(sc, serialized);
		sc.check();
	}

	/**
	 * Test method for {@link org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer#serialize(org.sakaiproject.entity.api.serialize.SerializableEntity)}.
	 * @throws Exception 
	 */
	@Test
	public final void testSerialize() throws Exception
	{
		Type1BaseContentCollectionSerializer t1 = new Type1BaseContentCollectionSerializer();
		t1.setTimeService(new MockTimeService());
		MockSerializableCollectionAcccess sc = new MockSerializableCollectionAcccess();
		byte[] s = t1.serialize(sc);
		MockSerializableResourceAcccess sr = new MockSerializableResourceAcccess();
		try {
			byte[] s1 = t1.serialize(sr);
			Assert.fail("Should have refused to serialize a ResourceAccess Object ");
		} catch ( EntityParseException epe ) {
			
		}
	}

	/**
	 * Test method for {@link org.sakaiproject.content.impl.serialize.impl.Type1BaseContentCollectionSerializer#accept(java.lang.String)}.
	 */
	@Test
	public final void testAccept()
	{
		Type1BaseContentCollectionSerializer t1 = new Type1BaseContentCollectionSerializer();
		
		Assert.assertEquals(true,t1.accept((Type1BaseContentCollectionSerializer.BLOB_ID+"the rest of the  blob").getBytes()));
		Assert.assertEquals(false,t1.accept((Type1BaseContentResourceSerializer.BLOB_ID+"the rest of the  blob").getBytes()));
		Assert.assertEquals(false,t1.accept(("0"+Type1BaseContentCollectionSerializer.BLOB_ID+"the rest of the  blob").getBytes()));
		Assert.assertEquals(false,t1.accept(null));
		Assert.assertEquals(false,t1.accept(("0somethisdfjsdkjfs dfjsldkf").getBytes()));
	}
}
