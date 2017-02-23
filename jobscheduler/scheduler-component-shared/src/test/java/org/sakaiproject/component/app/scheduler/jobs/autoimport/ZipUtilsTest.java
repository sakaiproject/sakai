package org.sakaiproject.component.app.scheduler.jobs.autoimport;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

/**
 * Just a quick test that we can expand a zipfile.
 * @author buckett
 *
 */
public class ZipUtilsTest {

	@Test
	public void testExpandZip() throws IOException {
		File createTempFile = File.createTempFile(ZipUtilsTest.class.getSimpleName(), null);
		try {
		createTempFile.delete();
		createTempFile.mkdirs();
		InputStream in = ZipUtilsTest.class.getResourceAsStream("test1.zip");
		List<ZipError> expandZip = ZipUtils.expandZip(in, createTempFile.getAbsolutePath());
		assertEquals(0, expandZip.size());
		assertTrue(new File(createTempFile, "test1").exists());
		assertTrue(new File(createTempFile, "test1/content.txt").isFile());
		} finally {
			recursiveDelete(createTempFile);
		}
	}

	// Just tidy up.
	private void recursiveDelete(File createTempFile) {
		LinkedList<File> directories = new LinkedList<File>();
		directories.push(createTempFile);
		
		while(!directories.isEmpty()) {
			File current = directories.peek();
			File[] listFiles = current.listFiles();
			if (listFiles.length == 0) {
				deleteFile(current);
				directories.pop();
			} else {
				for (File file: listFiles) {
					if (file.isFile()) {
						deleteFile(file);
					} else {
						directories.push(file);
					}
				}
			}
		}
		
	}

	protected void deleteFile(File file) {
		if (!file.delete()) {
			throw new RuntimeException("Failed to delete "+ file.getAbsolutePath());
		}
	}

}
