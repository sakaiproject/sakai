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

import java.util.Collection;


import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class PathPatternMatcherTest extends TestCase {
	public void testBasicPatterns()	{
		assertTrue(BeanCloner.matchPathEnd("a.a.b","*.*.*",String.class));
		assertTrue(BeanCloner.matchPathEnd("a.a.b","*.*.*",Object1.class));
		assertFalse(BeanCloner.matchPathEnd("a.a.b","*.*.*",Collection.class));
		assertTrue(BeanCloner.matchPathEnd("a.a.b","*.*.**",Collection.class));
		assertTrue(BeanCloner.matchPathEnd("a.a","*.*.**",Object1.class));
		assertTrue(BeanCloner.matchPathEnd("a.b","a.b.**",Object1.class));
		assertFalse(BeanCloner.matchPathEnd("a.a","a.b.**",Object1.class));
	}
	public void testMultiPatterns()	{
		assertTrue(BeanCloner.matchPathEnd("a.a.b","*,*.*.b",Object1.class));
		assertFalse(BeanCloner.matchPathEnd("a.a.b","*,*.*.c",Object1.class));
	}	
}
