/**
 * $Id: ValidationLogicDao.java 81430 2010-08-18 14:12:46Z david.horwitz@uct.ac.za $
 * $URL: https://source.sakaiproject.org/svn/reset-pass/trunk/account-validator-impl/src/java/org/sakaiproject/accountvalidator/dao/impl/ValidationLogicDao.java $
 *
 **************************************************************************
 * Copyright (c) 2008, 2009 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.content.copyright;

public class CopyrightItem implements org.sakaiproject.content.copyright.api.CopyrightItem {
	private String type;
	private String text;
	private String licenseUrl;
	
	public CopyrightItem(){}
	public CopyrightItem(String type, String text, String url){
		this.type = type;
		this.text = text;
		this.licenseUrl = url;
	}
	
	public void setType(String s){
		this.type = s;
	}
	public String getType(){
		return this.type;
	}

	public void setText(String s){
		this.text = s;
	}
	public String getText(){
		return this.text;
	}
	
	public void setLicenseUrl(String s){
		this.licenseUrl = s;
	}
	public String getLicenseUrl(){
		return this.licenseUrl;
	}
	
}