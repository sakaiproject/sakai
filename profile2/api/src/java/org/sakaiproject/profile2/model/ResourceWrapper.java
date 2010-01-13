/**
 * Copyright (c) 2008-2010 The Sakai Foundation
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

/**
 * This is a simple wrapper object to wrap a byte[] so we can store some other metadata along with it:
 * <p>
 * resourceID - id of resource in CHS or if isExternal then this will be populated with the URL to the resource as the identifier<br />
 * bytes[]	- content<br />
 * mimeType	- content type<br />
 * length	- content length<br />
 * isExternal - if the resource is external, therefore the resourceID will be the external URL.<br />
 * </p>
 * 
 * We could possibly use net.sf.ehcache.MimeTypeByteArray instead however this has more fields.
 * 
 * @author Steve Swinsburg (s.swinsburg@lancaster.ac.uk)
 *
 */
public class ResourceWrapper {

	private String resourceID;
	private byte[] bytes;
	private String mimeType;
	private long length;
	private boolean isExternal;
	
	/**
	 * No-arg constructor
	 */
	public ResourceWrapper() {
	}
	
	/**
	 * Full constructor
	 * @param resourceID
	 * @param bytes
	 * @param mimeType
	 * @param isExternal
	 */
	public ResourceWrapper(String resourceID, byte[] bytes, String mimeType, long length, boolean isExternal) {
		super();
		this.resourceID = resourceID;
		this.bytes = bytes;
		this.mimeType = mimeType;
		this.length = length;
		this.isExternal = isExternal;
	}

	public String getResourceID() {
		return resourceID;
	}

	public void setResourceID(String resourceID) {
		this.resourceID = resourceID;
	}

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

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public boolean isExternal() {
		return isExternal;
	}

	public void setExternal(boolean isExternal) {
		this.isExternal = isExternal;
	}

	
	
}
