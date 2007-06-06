package org.sakaiproject.scorm.content.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipWriter {

	private ZipInputStream inStream;
	private ZipOutputStream outStream;
	
	private Set<String> removeSet = new HashSet<String>();
	private List<Entry> addList = new LinkedList<Entry>();
	
	public ZipWriter(InputStream contentStream, OutputStream resultStream) {
		this.inStream = new ZipInputStream(contentStream);
		this.outStream = new ZipOutputStream(resultStream);
	}
	
	public void add(String name, InputStream entryStream) {
		addList.add(new Entry(name, entryStream));
	}
	
	public void remove(String name) {
		removeSet.add(name);
	}
	
	public void replace(String name, InputStream entryStream) {
		add(name, entryStream);
		remove(name);
	}
	
	public void process() throws IOException {
		ZipEntry entry;

		entry = (ZipEntry) inStream.getNextEntry();
		while (entry != null) {
			if (!removeSet.contains(entry.getName()))
				addEntry(entry, inStream);
			entry = (ZipEntry) inStream.getNextEntry();
		}
		for (Entry e : addList) {
			addEntry(new ZipEntry(e.getName()), e.getContentStream());
		}
		outStream.close();
	}
	
	
	private void addEntry(ZipEntry entry, InputStream contentStream) throws IOException {
		outStream.putNextEntry(entry);
		int len;
		byte[] buf = new byte[1024];
		
		while ((len = contentStream.read(buf)) > 0) {
			outStream.write(buf, 0, len);
		}
		
		outStream.closeEntry();
	}
	
	public class Entry {
		String name;
		InputStream contentStream;
		
		public Entry(String name, InputStream contentStream) {
			this.name = name;
			this.contentStream = contentStream;
		}
		
		public String getName() {
			return name;
		}
		
		public InputStream getContentStream() {
			return contentStream;
		}
	}
	
}
