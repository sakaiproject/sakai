package org.sakaiproject.profile2.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper class to hold a byte[] and preserve the mimetype.
 * Based on net.sf.ehcache.MimeTypeByteArray
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */

@Data
@NoArgsConstructor
public class MimeTypeByteArray {

	private byte[] bytes;
	private String mimeType;
	
}
