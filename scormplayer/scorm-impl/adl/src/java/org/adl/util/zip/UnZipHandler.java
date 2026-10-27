/*******************************************************************************
 **
 ** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you 
 ** ("Licensee") a non-exclusive, royalty free, license to use, modify and 
 ** redistribute this software in source and binary code form, provided that 
 ** i) this copyright notice and license appear on all copies of the software; 
 ** and ii) Licensee does not utilize the software in a manner which is 
 ** disparaging to ADL Co-Lab Hub.
 **
 ** This software is provided "AS IS," without a warranty of any kind.  ALL 
 ** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING 
 ** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE 
 ** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS 
 ** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF 
 ** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO 
 ** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, 
 ** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 ** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE 
 ** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE 
 ** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH 
 ** DAMAGES.
 **
 *******************************************************************************/
package org.adl.util.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipFile;
import org.sakaiproject.util.Zip;

/**
 * 
 * <strong>Filename:</strong><br>
 * UnZipHandler.java<br>
 * <br>
 * 
 * <strong>Description:</strong><br>
 * A <code>UnZipHandler</code> provides the ability to extract the contents of a
 * zipped file to a given directory.<br>
 * <br>
 * 
 * <strong>Side Effects:</strong><br>
 * The files will be extracted to the given directory
 * 
 * @author ADL Technical Team
 */
@Slf4j
public class UnZipHandler {
	/**
	 * Buffer size for use in extracting Zip File
	 */
	public static final int BUFFER_SIZE = 16384;

	/**
	 * A reference to the Zip File to be extracted
	 */
	private File mZipFile;

	/**
	 * The directory to which the Zip File will be extracted to
	 */
	private String mExtractToDir;

	/**
	 * The encoding of the zip file
	 */
	@Setter
    @Getter
    private String encoding = StandardCharsets.UTF_8.name();

	/**
	 * Constructor for the <code>UnZipHandler</code> class.
	 * 
	 * @param iZipFileName
	 *            Name and path of the <code>.zip</code> file.
	 * @param iTargetDirName
	 *            Name and path of the directory to extract the contents of the
	 *            Zip File to.
	 */
	public UnZipHandler(String iZipFileName, String iTargetDirName, String encoding) {
		setFile(iZipFileName);
		setTargetDirectory(iTargetDirName);
		setEncoding(encoding);
	}

	/**
	 * Extracts the Zip File into the destination directory
	 * 
	 */
    public void extract() throws IOException {
        Path root = Path.of(mExtractToDir);
        Files.createDirectories(root);
        byte[] buffer = new byte[BUFFER_SIZE];

        try (ZipFile archive = new ZipFile(mZipFile, encoding)) {
            // Check the complete package before writing any of its contents.
            for (Enumeration<ZipEntry> entries = archive.getEntries(); entries.hasMoreElements();) {
                Zip.resolveEntryPath(root, entries.nextElement().getName());
            }
            for (Enumeration<ZipEntry> entries = archive.getEntries(); entries.hasMoreElements();) {
                ZipEntry entry = entries.nextElement();
                Path destination = Zip.resolveEntryPath(root, entry.getName());
                if (entry.isDirectory()) {
                    continue;
                }
                Files.createDirectories(destination.getParent());
                try (InputStream in = archive.getInputStream(entry);
                     FileOutputStream out = new FileOutputStream(destination.toFile())) {
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        out.write(buffer, 0, count);
                    }
                }
            }
        }
    }

    /**
	 * Returns the target directory of the extracted contents of the Zip File.
	 * 
	 * @return target The target directory of the extracted contents of the Zip
	 *         File.
	 */
	public String getTargetDirectory() {
		return mExtractToDir;
	}

    /**
	 * Set the name and location of the Zip File to be extracted.
	 * 
	 * @param iFileName
	 *            Name and location of the Zip File to be extracted.
	 */
	private void setFile(String iFileName) {
		try {
			mZipFile = new File(iFileName);
		} catch (NullPointerException npe) {
			log.warn("Could not set zip file [{}], {}", iFileName, npe.toString());
		}
	}

	/**
	 * Set the target directory of the extracted contents of the Zip File.
	 * 
	 * @param iTargetDirPath
	 *            The target directory of the extracted contents of the Zip
	 *            File.
	 * 
	 */
	private void setTargetDirectory(String iTargetDirPath) {
		try {
			mExtractToDir = iTargetDirPath;
		} catch (NullPointerException npe) {
			log.warn("Could not set target directory [{}], {}", iTargetDirPath, npe.toString());
		}
	}
}