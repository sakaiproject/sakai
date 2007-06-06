package org.sakaiproject.scorm.content.test;

import java.util.List;

import org.sakaiproject.scorm.content.impl.VirtualFileSystem;

import junit.framework.TestCase;

public class VirtualFileSystemTest extends TestCase {

	public void testOne() throws Exception {
		
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
	
	private void printChildren(List<String> list) {
		for (String name : list) {
			System.out.println("NAME: " + name);
		}
	}
	
	
}
