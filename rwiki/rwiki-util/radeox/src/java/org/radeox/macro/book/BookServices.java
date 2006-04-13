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

package org.radeox.macro.book;

/**
 * Manages links to book dealears or comparison services
 * 
 * @author Stephan J. Schmidt
 * @version $Id$
 */

public class BookServices extends TextFileUrlMapper
{
	private static BookServices instance;

	public BookServices()
	{
		super(BookServices.class);
	}

	public static synchronized UrlMapper getInstance()
	{
		if (null == instance)
		{
			instance = new BookServices();
		}
		return instance;
	}

	public String getFileName()
	{
		return "conf/bookservices.txt";
	}

	public String getKeyName()
	{
		return "isbn";
	}
}
