/**
 * Copyright (c) 2008-2012 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.profile2.model;

import lombok.Data;
import lombok.NoArgsConstructor;

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
 * <p>You can get alt text for the image via getAltText()</p>
 * 
 * <p>This is not related to the similarly named hibernate model (hbm.model.ProfileImage)</p>
 * 
 * @author Steve Swinsburg (steve.swinsburg@gmail.com)
 *
 */
@Data
@NoArgsConstructor
public class ProfileImage {

	private byte[] uploadedImage;
	private String externalImageUrl;
	private String officialImageUrl;
	private String officialImageEncoded;
	private String altText;
	private String mimeType;
	private boolean isDefault;
		
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
	
}
