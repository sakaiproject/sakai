package org.sakaiproject.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;
import org.sakaiproject.importer.impl.importables.Assessment;

import junit.framework.TestCase;

public class CommonCartridgeTest extends TestCase {
	private static ImportFileParser parser;
	private byte[] archiveData;
	
	public void setUp() {
		System.out.println("doing setUp()");
		try {
			parser = new CommonCartridgeFileParser();
			FileInputStream archiveStream = new FileInputStream(new File("/Users/zach/psychology.zip"));
			archiveData = new byte[archiveStream.available()];
			archiveStream.read(archiveData,0,archiveStream.available());
			archiveStream.close();
		} catch (IOException e) {
			
		}
	}
	public void testIsValidArchive() {
		assertTrue(parser.isValidArchive(archiveData));
	}
	
	public void testCanGetQti() {
		ImportDataSource ids = parser.parse(archiveData, "/Users/zach/Desktop/psychology");
		Collection importables = ids.getItemsForCategories(ids.getItemCategories());
		int numberOfAssessments = 0;
		for (Iterator i = importables.iterator();i.hasNext();) {
			if(i.next() instanceof Assessment) numberOfAssessments++;
		}
		System.out.println(ids.getItemCategories().size() + " top-level items");
		System.out.println(importables.size() + " importables");
		System.out.println(numberOfAssessments + " assessments");
		assertTrue("Why no assessments?", numberOfAssessments > 0);
	}

}
