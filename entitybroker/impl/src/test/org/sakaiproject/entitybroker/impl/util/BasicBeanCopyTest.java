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


import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class BasicBeanCopyTest extends TestCase {
	public void testBasicBeanCopy() throws Exception {
		Object1 o1=new Object1();
		Object2 o2=new Object2();
		o1.setI(5);
		o1.setL(4l);
		o1.setS("ASD");
		o2.setO(o1);
		o2.setOs(new ArrayList());
		Object1 newo1=BeanCloner.copy(o1, new Object1());
		assertSame(o1.getI(), newo1.getI());
		assertSame(o1.getL(), newo1.getL());
		assertSame(o1.getS(), newo1.getS());
		Object2 newo2=BeanCloner.copy(o2, new Object2());
		assertNull(newo2.getO());
		assertNull(newo2.getOs());		
	}
	public void testPatternBeanCopy() throws Exception {
		Object1 o1=new Object1();
		Object2 o2=new Object2();
		o2.setO(o1);
		o2.setOs(new ArrayList());
		Object2 newo2=BeanCloner.copy(o2, new Object2(),"*",false);
		assertSame(o2.getO(), newo2.getO());
		assertNull(newo2.getOs());		
	}

	
}
