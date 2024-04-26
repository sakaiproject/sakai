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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;

import junit.framework.TestCase;

import org.sakaiproject.scorm.content.impl.ZipWriter;

public class ZipWriterTest extends TestCase
{
	public void testOne() throws Exception
	{
		boolean testFailure = false;
		StringBuilder msg = new StringBuilder();

		String testFileName = System.getProperty("testZipFileName"); //defined in pom
		testFileName = (testFileName != null ? testFileName : "testZipFile.zip");
		java.net.URL testURL = this.getClass().getClassLoader().getResource(testFileName);

		if (testURL != null)
		{
			FileInputStream contentStream = null;
			String path = testURL.getFile();
			path = URLDecoder.decode(path, "UTF-8");
			FileInputStream in = new FileInputStream(path);
			String newName = path.substring(0, path.lastIndexOf('/') + 1) + "myresult.zip";

			FileOutputStream out = new FileOutputStream(newName);
			ZipWriter writer = new ZipWriter(in, out);

			testFileName = System.getProperty("testAddFileName"); //defined in pom
			testFileName = (testFileName != null ? testFileName : "applicationContext.xml");
			testURL = this.getClass().getClassLoader().getResource(testFileName);

			if (testURL != null)
			{
				contentStream = new FileInputStream(path);
				try
				{
					writer.add("applicationContext.xml", contentStream);
				} 
				catch (UnsupportedOperationException wtf)
				{
					msg.append( "the add method is not supported by this list\n" ).append(wtf.getMessage());
					System.out.println("the add method is not supported by this list\n");
					testFailure = true;
				}
				catch (ClassCastException wtf)
				{
					msg.append("the class of the specified element prevents it from being added to this list\n");
					System.out.println("the class of the specified element prevents it from being added to this list\n" + wtf.getMessage());
					testFailure = true;
				}
				catch (NullPointerException npe)
				{
					msg.append("the specified element is null and this list does not support null elements\n");
					System.out.println("the specified element is null and this list does not support null elements\n" + npe.getMessage());
					testFailure = true;
				}
				catch (IllegalArgumentException wtf)
				{
					msg.append("some aspect of this element prevents it from being added to this list\n");
					System.out.println("some aspect of this element prevents it from being added to this list\n" + wtf.getMessage());
					testFailure = true;
				}

				try
				{
					writer.process();
				} 
				catch (IOException io)
				{
					msg.append("IO Error\n");
					System.out.println("IO Error\n" + io.getMessage());
					testFailure = true;
				} 
				catch (Exception e)
				{
					msg.append("General Exception during processing\n");
					System.out.println("General Exception during processing\n" + e.getMessage());
					testFailure = true;
				}

				if (contentStream != null)
				{
					contentStream.close();
				}
			}
			else
			{
				System.out.println("file not found: " + testFileName + "\n");
				testFailure = true;
			}
		}
		else
		{
			///could not find test zip file
			msg.append( "file not found: " ).append( testFileName ).append("\n");
			System.out.println("file not found: " + testFileName + "\n");
			testFailure = true;
		}

		assertFalse(msg.toString(), testFailure);
	}
}
