package org.sakaiproject.search.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.record.formula.functions.Countif;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.search.api.SearchService;

public class DigestStorageUtil {

	private static final String DIGEST_STORE_FOLDER = "/searchdigest/";
	private static final Log log = LogFactory.getLog(DigestStorageUtil.class);


	private static String getHashOfFile(String ref) {
		return DigestUtils.md5Hex(ref);
	}

	public static String getPath(String reference) {
		log.debug("getPath(" + reference);
		String ret = "";

		reference = getHashOfFile(reference);
		ret = reference.substring(0, 1) + "/" + reference.substring(0, 3) + "/" + reference;
		return ret;
	}

	public static int getDigestCount(String reference) {
		return 1;
	}

	public static StringBuilder getFileContents(String ref, String digestCount) {
		StringBuilder sb = new StringBuilder();
		BufferedReader input = null;
		try {
			String storePath = ServerConfigurationService.getString("bodyPath@org.sakaiproject.content.api.ContentHostingService");
			storePath += DIGEST_STORE_FOLDER;
			String digestFilePath = storePath + DigestStorageUtil.getPath(ref) + "/digest." + digestCount;
			log.debug("opening: " + digestFilePath);

			input =  new BufferedReader(new FileReader(digestFilePath));

			String line = null; //not declared within while loop
			/*
			 * readLine is a bit quirky :
			 * it returns the content of a line MINUS the newline.
			 * it returns null only for the END of the stream.
			 * it returns an empty String if two newlines appear in a row.
			 */
			while (( line = input.readLine()) != null){
				sb.append(line);
				sb.append(System.getProperty("line.separator"));

			}
		}
		catch (FileNotFoundException e) {
			log.warn("Unable to open digest for item: " + SearchService.FIELD_REFERENCE + " with count " + digestCount + " fileNotFound");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (input != null) {
				try {
					input.close();
				}
				catch (IOException iox) {
					log.debug("exeption in final block!");
				}
			}
		}

		return sb;
	}

	/**
	 * Save the digested content to a disc store
	 * @param ref
	 * @param content
	 */
	public static void saveContentToStore(String ref, String content, int version) {
		String storePath = ServerConfigurationService.getString("bodyPath@org.sakaiproject.content.api.ContentHostingService");
		if (storePath != null ) {
			storePath += DIGEST_STORE_FOLDER;
			FileOutputStream fileOutput = null;
			try {
				if (!new File(storePath).exists())
					new File(storePath).mkdirs();
				String exPath = DigestStorageUtil.getPath(ref);
				String filePath = storePath + exPath;

				if (!new File(filePath).exists()) {
					log.debug("creating folder" + filePath);
					new File(filePath).mkdirs();
				}


				log.debug("filePath: " + filePath);
				fileOutput = new FileOutputStream(filePath + "/digest." + version);
				fileOutput.write(content.getBytes("UTF8"));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
			finally {
				try {
					if(fileOutput != null) fileOutput.close();
				} catch (IOException e) {
					log.error("Exception in finally block: "+e);
				}
			}
		}
	}


	public static int getDocCount(String ref) {
		String storePath = ServerConfigurationService.getString("bodyPath@org.sakaiproject.content.api.ContentHostingService");
		int count = 0;
		if (storePath != null ) {
			storePath += DIGEST_STORE_FOLDER;
			String exPath = DigestStorageUtil.getPath(ref);
			String filePath = storePath + exPath;
			if (new File(filePath).exists()) {
				File dir = new File(filePath);
				String[] children = dir.list();
				if (children == null) {
					return 0;
				} else {
					for (int i=0; i<children.length; i++) {
						String fileName = children[i];
						if (fileName.contains(".")) {
							Integer countIn = getCountFromFileName(fileName);
							if (countIn.intValue() > count) {
								count = countIn.intValue();
							}

						}
					}
					return count;
				}


			}
		}
		return 0;
	}

	private static Integer getCountFromFileName(String fileName) {
		if (!fileName.contains(".")) {
			return null;
		}
		Integer countIn = null;
		String countStr = fileName.substring(fileName.lastIndexOf('.') + 1 , fileName.length());
		log.debug("count string is: " + countStr);
		try {
			countIn = Integer.valueOf(countStr);
		} 
		catch (NumberFormatException nfe) {
			log.warn("filename:  " + fileName + "has nonNumeric exension");
		}
		return countIn;
	}

	public static void cleanOldDigests(String ref) {
		String storePath = ServerConfigurationService.getString("bodyPath@org.sakaiproject.content.api.ContentHostingService");
		int docCount = getDigestCount(ref);

		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		Date yesterDay = cal.getTime();

		if (storePath != null ) {
			storePath += DIGEST_STORE_FOLDER;
			String exPath = DigestStorageUtil.getPath(ref);
			String filePath = storePath + exPath;
			if (new File(filePath).exists()) {
				File dir = new File(filePath);
				String[] children = dir.list();
				if (children == null) {
					return;
				} else {
					for (int i=0; i<children.length; i++) {
						String fileName = children[i];
						if (docCount > getCountFromFileName(fileName)) {
							File file = new File(filePath + "/" + fileName);
							if (file.exists()) {
								Date lastMod = new Date(file.lastModified());
								//is this more than a day old?
								if (lastMod.before(yesterDay)) {
									file.delete();
								}


							}
						}
					}
				}
			}
		}
	}
	
	
	public static void deleteAllDigests(String reference) {
		String storePath = ServerConfigurationService.getString("bodyPath@org.sakaiproject.content.api.ContentHostingService");
		if (storePath != null ) {
			storePath += DIGEST_STORE_FOLDER;
			String exPath = DigestStorageUtil.getPath(reference);
			String filePath = storePath + exPath;
			File dir = new File(filePath);
			if (dir.exists()) {
				dir.delete();
			}
			}
	}
	
}
