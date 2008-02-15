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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.print.DocFlavor.URL;

import junit.framework.TestCase;

import org.sakaiproject.scorm.content.impl.ZipWriter;

public class ZipWriterTest extends TestCase {

	public void testOne() throws Exception {
		
		boolean testFailure = false;
		StringBuffer msg = new StringBuffer();
		
		String testFileName = System.getProperty("testZipFileName"); //defined in pom
		
		java.net.URL testURL = this.getClass().getClassLoader().getResource(testFileName);
		

		if(testURL!=null){
			FileInputStream contentStream = null;
			String path = testURL.getPath();
			FileInputStream in = new FileInputStream(testURL.getPath().substring(path.indexOf(":")+1));
			String newName= path.substring(path.indexOf(":")+1, path.lastIndexOf(File.separator)+1)+"myresult.zip";
			
			FileOutputStream out = new FileOutputStream(newName);
			

			ZipWriter writer = new ZipWriter(in, out);
			
			testFileName = System.getProperty("testAddFileName"); //defined in pom
			testURL = this.getClass().getClassLoader().getResource(testFileName);
			
			if(testURL!=null){
				contentStream = new FileInputStream(testURL.getPath().substring(path.indexOf(":")+1));
				
				try {
					writer.add("applicationContext.xml", contentStream);
					
				}catch (    UnsupportedOperationException wtf) {
					msg.append("the add method is not supported by this list\n" + wtf.getMessage());
					System.out.println("the add method is not supported by this list\n");
					testFailure = true;
				}catch (    ClassCastException wtf){
					msg.append("the class of the specified element prevents it from being added to this list\n");
					System.out.println("the class of the specified element prevents it from being added to this list\n" + wtf.getMessage());
					testFailure = true;
				}catch (    NullPointerException npe){
					msg.append("the specified element is null and this list does not support null elements\n");
					System.out.println("the specified element is null and this list does not support null elements\n" + npe.getMessage());
					testFailure = true;
				}catch(    IllegalArgumentException wtf){
					msg.append("some aspect of this element prevents it from being added to this list\n");
					System.out.println("some aspect of this element prevents it from being added to this list\n" + wtf.getMessage());
					testFailure = true;
				}
				//writer.add("dir/file4.txt", contentStream);
				//writer.remove("file.txt");
				try{
					writer.process();
				}catch(IOException io){
					msg.append("IO Error\n");
					System.out.println("IO Error\n" + io.getMessage());
					testFailure = true;
				}catch(Exception e){
					msg.append("General Exception during processing\n");
					System.out.println("General Exception during processing\n" + e.getMessage());
					testFailure = true;
				}

				if (contentStream != null)
					contentStream.close();
			}else{
				System.out.println("file not found: " + testFileName + "\n");
				testFailure = true;
				}
		}else{///could not find test zip file
			msg.append("file not found: " + testFileName + "\n");
			System.out.println("file not found: " + testFileName + "\n");
			testFailure = true;
		}

		assertFalse(msg.toString(), testFailure);
	}
	
	
}
