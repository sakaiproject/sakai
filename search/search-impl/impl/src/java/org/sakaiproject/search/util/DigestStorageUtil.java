package org.sakaiproject.search.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DigestStorageUtil {

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

	
	
	
}
