package org.sakaiproject.util;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Just allows simple building of a JAR file.
 *
 * @link http://stackoverflow.com/questions/1281229/how-to-use-jaroutputstream-to-create-a-jar-file
 */
public class JarBuilder {

	private final File file;
	private JarOutputStream target;

	public JarBuilder(String file) {
		this.file = new File(file);
	}

	public void start() throws IOException {
		Manifest manifest = new Manifest();
		manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		target = new JarOutputStream(new FileOutputStream(file), manifest);
	}

	public void stop() throws IOException {
		target.close();
	}

	/**
	 * Recursively add files to a JAR archive.
	 *
	 * @param directory The file/directory to add.
	 * @param file      File contained in the directory to add.
	 * @param remove    If true remove files we process.
	 * @throws IOException If something went wrong.
	 */
	public void add(String directory, String file, boolean remove) throws IOException {
		BufferedInputStream in = null;
		try {
			File source = new File(directory, file);
			if (source.isDirectory()) {
				String name = file.replace("\\", "/");
				if (!name.isEmpty()) {
					if (!name.endsWith("/"))
						name += "/";
					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}
				for (String nestedFile : source.list()) {
					add(directory, file + File.separator + nestedFile, remove);
				}
			} else {

				JarEntry entry = new JarEntry(file.replace("\\", "/"));
				entry.setTime(source.lastModified());
				target.putNextEntry(entry);
				in = new BufferedInputStream(new FileInputStream(source));

				byte[] buffer = new byte[1024];
				while (true) {
					int count = in.read(buffer);
					if (count == -1)
						break;
					target.write(buffer, 0, count);
				}
				target.closeEntry();
			}
			if (remove) {
				source.delete();
			}
		} finally {
			if (in != null)
				in.close();
		}
	}
}
