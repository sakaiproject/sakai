/*
 * This file is part of "SnipSnap Radeox Rendering Engine".
 *
 * Copyright (c) 2002 Stephan J. Schmidt, Matthias L. Jugel
 * All Rights Reserved.
 *
 * Please visit http://radeox.org/ for updates and contact.
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

package org.radeox.test.filter;

import junit.framework.TestCase;

import org.radeox.engine.context.BaseInitialRenderContext;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.filter.Filter;
import org.radeox.filter.context.BaseFilterContext;
import org.radeox.filter.context.FilterContext;

/**
 * Support class for defning JUnit FilterTests.
 * 
 * @author Stephan J. Schmidt
 * @version $Id: FilterTestSupport.java 7707 2006-04-12 17:30:19Z
 *          ian@caret.cam.ac.uk $
 */

public class FilterTestSupport extends TestCase
{
	protected Filter filter;

	protected FilterContext context;

	public FilterTestSupport(String s)
	{
		super(s);
		context = new BaseFilterContext();
		context.setRenderContext(new BaseRenderContext());
	}

	protected void setUp() throws Exception
	{
		super.setUp();
		if (null != filter)
		{
			filter.setInitialContext(new BaseInitialRenderContext());
		}
	}
}
