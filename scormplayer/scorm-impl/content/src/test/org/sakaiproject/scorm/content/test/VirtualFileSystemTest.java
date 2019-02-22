/**
 * Copyright (c) 2007 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.scorm.content.test;

import java.util.List;

import junit.framework.TestCase;

import org.sakaiproject.scorm.content.impl.VirtualFileSystem;

public class VirtualFileSystemTest extends TestCase
{
	private void printChildren(List<String> list)
	{
		for (String name : list)
		{
			System.out.println("NAME: " + name);
		}
	}

	public void testOne() throws Exception
	{
		VirtualFileSystem fs = new VirtualFileSystem("/content/group/asdfba/myzipfile.zip");

		fs.addPath("this/is/a/file");
		fs.addPath("this/is/a/second/file");
		fs.addPath("this/is/another/file");
		fs.addPath("this/is/a/word");

		System.out.println("this/" + fs.getCount("this/"));
		printChildren(fs.getChildren("/this/"));

		System.out.println("this/is/" + fs.getCount("this/is/"));
		printChildren(fs.getChildren("/this/is"));

		System.out.println("this/is/a/" + fs.getCount("this/is/a/"));
		printChildren(fs.getChildren("/this/is/a"));
	}
}
