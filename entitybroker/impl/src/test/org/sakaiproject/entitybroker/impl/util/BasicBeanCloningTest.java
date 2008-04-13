/*
 *  Copyright 2007 CodersLog.com 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *   
 *  	http://www.apache.org/licenses/LICENSE-2.0
 *   
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */

package org.sakaiproject.entitybroker.impl.util;

import java.util.ArrayList;
import java.util.List;


import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class BasicBeanCloningTest extends TestCase {
	protected Object1 o1;
	protected Object2 o2;
	
   public void setUp()	{
		o1=new Object1(10,100000l,"Hello World!");
		o2=new Object2();
		o2.setO(new Object1(20,20000l,"Hello World2!"));
		o2.setOs(new ArrayList());
		for(int i=0;i<10;i++)	{
			o2.getOs().add(new Object1(i*10,i*100l,"Hello"+i));
		}
	}
	
	public void testSimpleClone()	{
		Object1 newo=BeanCloner.clone(o1);
		assertNotNull(newo);		
		assertNotSame(o1,newo);
		assertEquals(o1, newo);
	}
	public void testNestedClone()	{
		Object2 newo=BeanCloner.clone(o2);
		assertNotNull(newo);		
		assertNotSame(o2, newo);
		assertNotSame(o2.getO(), newo.getO());
		assertEquals(o2.getO(), newo.getO());
	}
	public void testArrayPropertyClone()	{
		Object2 newo=BeanCloner.clone(o2);
		assertNotNull(newo);		
		assertNotSame(o2, newo);
		assertNotSame(o2.getOs(),newo.getOs());
		for(int i=0;i<o2.getOs().size();i++)	{
			assertEquals(o2.getOs().get(i),newo.getOs().get(i));
		}
	}
	public void testArrayClone()	{
		List<Object1> newos=BeanCloner.clone(o2.getOs());	
		assertNotSame(o2.getOs(), newos);
		for(int i=0;i<o2.getOs().size();i++)	{
			assertEquals(o2.getOs().get(i),newos.get(i));
		}
	}
	
	public void testRecursiveClone()	{
		o2.setO2(o2);
		Object2 newo=BeanCloner.clone(o2,"*");	
		assertNotSame(o2, newo);
		assertSame(newo,newo.getO2());
	}	
	public void testNestedClonePattern()	{
		Object2 newo=BeanCloner.clone(o2,"o");
		assertNotNull(newo);
		assertNotSame(o2, newo);
		assertNotSame(o2.getO(), newo.getO());
		assertEquals(o2.getO(), newo.getO());
		assertNull(newo.getOs());
	}

	public void testArrayClonePattern()	{
		Object2 newo=BeanCloner.clone(o2,"os");
		assertNotNull(newo);		
		assertNotSame(o2, newo);
		assertNull(newo.getO());
		assertNotSame(o2.getOs(),newo.getOs());
		for(int i=0;i<o2.getOs().size();i++)	{
			assertEquals(o2.getOs().get(i),newo.getOs().get(i));
		}
	}
	
	public void testCloneMultiPattern()	{
		Object2 newo=BeanCloner.clone(o2,"os,o");
		assertNotNull(newo);		
		assertNotSame(o2, newo);
		assertNotSame(o2.getOs(),newo.getOs());
		for(int i=0;i<o2.getOs().size();i++)	{
			assertEquals(o2.getOs().get(i),newo.getOs().get(i));
		}
		assertNotSame(o2.getO(),newo.getO());
		assertEquals(o2.getO(),newo.getO());
	}	
	
	public void assertEquals(Object1 o1, Object1 o2)	{
		assertEquals(o1.getI(),o2.getI());
		assertEquals(o1.getL(),o2.getL());
		assertEquals(o1.getS(),o2.getS());	
	}	
	
	public void testPatternBeanCloneNCWithNoC() throws Exception {
		Object1 o1=new Object1();
		Object2 o2=new Object2();
		o2.setO(o1);
		o2.setOs(new ArrayList());
		Object2 newo2=BeanCloner.clone(o2, "NC*");
		assertNotSame(o2.getO(), newo2.getO());
		assertNull(newo2.getOs());		
	}
	public void testPatternBeanCloneNCWithC() throws Exception {
		Object1 o1=new Object1();
		Object2 o2=new Object2();
		o2.setO(o1);
		o2.setO2(o2);
		o2.setOs(new ArrayList());
		Object2 newo2=BeanCloner.clone(o2, "NC*");
		assertNotSame(o2.getO(), newo2.getO());
		assertNull(newo2.getOs());		
		assertNull(newo2.getO2());
	}	
	
}
