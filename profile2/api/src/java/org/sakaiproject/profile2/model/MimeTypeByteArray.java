package org.sakaiproject.profile2.model;

/**
 * Wrapper class to hold a byte[] and preserve the mimetype.
 * Based on net.sf.ehcache.MimeTypeByteArray
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class MimeTypeByteArray {

	private byte[] bytes;
	private String mimeType;
	
	public MimeTypeByteArray() {}
	
	
	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
}
