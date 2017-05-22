/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2006, 2007, 2008 The Sakai Foundation
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
 *
 **********************************************************************************/

package org.sakaibrary.xserver;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FindResultSetBean {
	private String baseName;
	private String sourceId;
	private String setNumber;
	private StringBuilder fullName;
	private String status;
	private StringBuilder findErrorText;
	private String numDocs;
	
	public FindResultSetBean( String baseName ) {
		this.baseName = baseName;
	}
	
	public String getBaseName() {
		return baseName;
	}
	
	public void setSourceId( String id ) {
		sourceId = id;
	}
	
	public String getSourceId() {
		return sourceId;
	}
	
	public void setSetNumber( String setNum ) {
		setNumber = setNum;
	}
	
	public String getSetNumber() {
		return setNumber;
	}
	
	public void setFullName( String name ) {
		if( fullName == null ) {
			fullName = new StringBuilder();
		}
		
		fullName.append( name );
	}
	
	public String getFullName() {
		return ( fullName == null ) ? null : fullName.toString();
	}
	
	public void setStatus( String stat ) {
		status = stat;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setFindErrorText( String text ) {
		if( findErrorText == null ) {
			findErrorText = new StringBuilder();
		}
		
		findErrorText.append( text );
	}
	
	public String getFindErrorText() {
		return ( findErrorText == null ) ? null : findErrorText.toString();
	}
	
	public void setNumDocs( String num ) {
		numDocs = num;
	}
	
	public String getNumDocs() {
		return numDocs;
	}
	
	public void printInfo() {
		log.debug( "\nFIND RESULT SET INFO" );
		log.debug( "source id:  " + getSourceId() );
		log.debug( "full name:  " + getFullName() );
		log.debug( "set number: " + getSetNumber() );
		log.debug( "status:     " + getStatus() );
		log.debug( "error text: " + getFindErrorText() );
		log.debug( "docs found: " + getNumDocs() );
	}
}
