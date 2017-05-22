/*
 * This file is part of "Rotaract/Intranet".
 *
 * Copyright (c) 2003 Stephan J. Schmidt
 * All Rights Reserved.
 *
 * --LICENSE NOTICE--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * --LICENSE NOTICE--
 */
package org.radeox.test.groovy;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllGroovyTests extends TestCase
{
	public AllGroovyTests(String name)
	{
		super(name);
	}

	public static Test suite()
	{
		TestSuite s = new TestSuite();
		// s.addTestSuite(RadeoxTemplateEngineTest.class);
		return s;
	}
}
