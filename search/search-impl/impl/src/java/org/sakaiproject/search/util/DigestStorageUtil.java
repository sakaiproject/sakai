package org.sakaiproject.search.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DigestStorageUtil {
	private static final Log log = LogFactory.getLog(DigestStorageUtil.class);
	
	private static String getHashOfFile(String ref) {
		MessageDigest md;
		String ret = null;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(ref.getBytes());
			// convert the binary md5 hash into hex
			String md5 = "";
			byte[] b_arr = md.digest();

			for (int i = 0; i < b_arr.length; i++) {
				// convert the high nibble
				byte b = b_arr[i];
				b >>>= 4;
				b &= 0x0f; // this clears the top half of the byte
				md5 += Integer.toHexString(b);

				// convert the low nibble
				b = b_arr[i];
				b &= 0x0F;
				md5 += Integer.toHexString(b);
			}
			ret = md5;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
	
		log.debug("got hash of reference of: " + ret);
		return ret;
	}
	
	public static String getPath(String reference) {
		String ret = "";
		
		reference = getHashOfFile(reference);
		ret = reference.substring(0, 1) + "/" + reference.substring(0, 3) + "/" + reference;
		return ret;
	}
	
	public static int getDigestCount(String reference) {
		return 1;
	}

	
	
	
}
