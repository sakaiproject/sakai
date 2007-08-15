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
