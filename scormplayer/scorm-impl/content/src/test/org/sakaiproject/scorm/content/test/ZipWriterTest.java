package org.sakaiproject.scorm.content.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.sakaiproject.scorm.content.impl.ZipWriter;

import junit.framework.TestCase;

public class ZipWriterTest extends TestCase {

	public void testOne() throws Exception {
		FileInputStream in = new FileInputStream("/home/jrenfro/junk/myarchive.zip");
		FileOutputStream out = new FileOutputStream("/home/jrenfro/junk/result/myresult.zip");
		
		ZipWriter writer = new ZipWriter(in, out);
		
		FileInputStream contentStream = new FileInputStream("/home/jrenfro/junk/file.txt");
		
		writer.add("dir/file4.txt", contentStream);
		writer.remove("file.txt");
		
		writer.process();
		
		if (contentStream != null)
			contentStream.close();
	}
	
	
}
