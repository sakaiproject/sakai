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

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
					log.info("the add method is not supported by this list");
					testFailure = true;
				}
				catch (ClassCastException wtf)
				{
					msg.append("the class of the specified element prevents it from being added to this list\n");
					log.info("the class of the specified element prevents it from being added to this list: {}", wtf.getMessage());
					testFailure = true;
				}
				catch (NullPointerException npe)
				{
					msg.append("the specified element is null and this list does not support null elements\n");
					log.info("the specified element is null and this list does not support null elements: {}", npe.getMessage());
					testFailure = true;
				}
				catch (IllegalArgumentException wtf)
				{
					msg.append("some aspect of this element prevents it from being added to this list\n");
					log.info("some aspect of this element prevents it from being added to this list: {}", wtf.getMessage());
					testFailure = true;
				}

				try
				{
					writer.process();
				} 
				catch (IOException io)
				{
					msg.append("IO Error\n");
					log.info("IO Error: {}", io.getMessage());
					testFailure = true;
				} 
				catch (Exception e)
				{
					msg.append("General Exception during processing\n");
					log.info("General Exception during processing: {}", e.getMessage());
					testFailure = true;
				}

				if (contentStream != null)
				{
					contentStream.close();
				}
			}
			else
			{
				log.info("file not found: {}", testFileName);
				testFailure = true;
			}
		}
		else
		{
			///could not find test zip file
			msg.append( "file not found: " ).append( testFileName ).append("\n");
			log.info("file not found: {}", testFileName);
			testFailure = true;
		}

		assertFalse(msg.toString(), testFailure);
	}
}
