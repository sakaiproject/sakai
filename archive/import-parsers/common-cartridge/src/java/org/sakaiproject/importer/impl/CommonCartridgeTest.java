package org.sakaiproject.importer.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.sakaiproject.importer.api.ImportDataSource;
import org.sakaiproject.importer.api.ImportFileParser;

import junit.framework.TestCase;

public class CommonCartridgeTest extends TestCase {
	private static ImportFileParser parser;
	
	public void setUp() {
		System.out.println("doing setUp()");
		parser = new CommonCartridgeFileParser();
	}
	public void testIsValidArchive() {
		try {
			FileInputStream archiveStream = new FileInputStream(new File("/Users/zach/Downloads/CCSamplePackage.zip"));
			byte[] archiveData = new byte[archiveStream.available()];
			archiveData = new byte[archiveStream.available()];
			archiveStream.read(archiveData,0,archiveStream.available());
			archiveStream.close();
			assertTrue(parser.isValidArchive(archiveData));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
