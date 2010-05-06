package org.sakaiproject.profile2.model;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 * ProfileImageResource
 * 
 * <p>This is a wrapper class which contains fields for all types of profile images. Only one field will be set at any given time.</p>
 * <p>Use the getBinary or getUrl methods to do the work and return the data as either a String or byte[]. If String, this will be a URL/URI you can use directly.
 * If byte[] this will be the uploaded image. Consult both.</p>
 * 
 * <p>Note. Eventually, this will return only Strings. The byte[] will be base64 encoded and returned as a data URI you can use, however this
 * is not supported in browsers older than IE8 (Safari, Firefox, Chrome, all ok though).
 * 
 * <p>This is not related to the similarly named hibernate model (hbm.model.ProfileImage)</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
public class ProfileImage {

	private byte[] uploadedImage;
	private String externalImageUrl;
	private String officialImageUrl;
	private String officialImageEncoded;
	
	public ProfileImage() {}
	
	
	/**
	 * Get access to the binary data from either the uploaded image or the base64 encoded data
	 * @return byte[] or null if none
	 */
	public byte[] getBinary() {
		if(uploadedImage != null){
			return uploadedImage;
		}
		if(StringUtils.isNotBlank(officialImageEncoded)){
			return Base64.decodeBase64(officialImageEncoded);
		}
		return null;
	}
	
	/**
	 * Get access to the URL from either the external image that a user can set, or an official image.
	 * @return url or null.
	 */
	public String getUrl() {
		if(StringUtils.isNotBlank(externalImageUrl)){
			return externalImageUrl;
		}
		if(StringUtils.isNotBlank(officialImageUrl)){
			return officialImageUrl;
		}
		return null;
	}
	
	
	
	
	
	public byte[] getUploadedImage() {
		return uploadedImage;
	}

	public void setUploadedImage(byte[] uploadedImage) {
		this.uploadedImage = uploadedImage;
	}

	public String getExternalImageUrl() {
		return externalImageUrl;
	}

	public void setExternalImageUrl(String externalImageUrl) {
		this.externalImageUrl = externalImageUrl;
	}

	public String getOfficialImageUrl() {
		return officialImageUrl;
	}

	public void setOfficialImageUrl(String officialImageUrl) {
		this.officialImageUrl = officialImageUrl;
	}

	public String getOfficialImageEncoded() {
		return officialImageEncoded;
	}

	public void setOfficialImageEncoded(String officialImageEncoded) {
		this.officialImageEncoded = officialImageEncoded;
	}
	
	
	
}
