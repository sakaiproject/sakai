/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.scorm.content.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.sakaiproject.scorm.content.impl.ZipWriter;

public class ZipWriterTest extends TestCase {

	public void testOne() throws Exception {
		FileInputStream in = new FileInputStream("/home/jrenfro/junk/Learning_Technologies.zip");
		FileOutputStream out = new FileOutputStream("/home/jrenfro/junk/result/myresult.zip");
		
		ZipWriter writer = new ZipWriter(in, out);
		
		FileInputStream contentStream = new FileInputStream("/home/jrenfro/junk/applicationContext.xml");
		
		writer.add("applicationContext.xml", contentStream);
		//writer.add("dir/file4.txt", contentStream);
		//writer.remove("file.txt");
		
		writer.process();
		
		if (contentStream != null)
			contentStream.close();
	}
	
	
}
