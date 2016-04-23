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

package org.sakaibrary.osid.repository.xserver;

import lombok.extern.slf4j.Slf4j;
import org.sakaibrary.osid.repository.xserver.CreatorPartStructure;
import org.sakaibrary.osid.repository.xserver.DatePartStructure;
import org.sakaibrary.osid.repository.xserver.PagesPartStructure;
import org.sakaibrary.osid.repository.xserver.SourceTitlePartStructure;
import org.sakaibrary.osid.repository.xserver.URLPartStructure;

/**
 * This class wraps an org.osid.repository.Asset to make it more suitable
 * for presentation
 * 
 * @author gbhatnag
 */
@Slf4j
public class AssetPresentationBean implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	private org.osid.repository.Asset asset;
	private String assetId;
	private String displayName;  // title
	private String description;  // abstract
	private Integer content;
	private java.util.ArrayList parts;

	/**
	 * Constructor takes an org.osid.repository.Asset and extracts all
	 * data for easier presentation.
	 */
	public AssetPresentationBean( org.osid.repository.Asset asset ) {
		parts = new java.util.ArrayList();
		
		try {
			assetId = asset.getId().getIdString();
			displayName = asset.getDisplayName();
			description = asset.getDescription();
			content = ( Integer )asset.getContent();
			
			org.osid.repository.RecordIterator rit = asset.getRecords();
			while( rit.hasNextRecord() ) {
				org.osid.repository.Record record = rit.nextRecord();
				
				org.osid.repository.PartIterator pit = record.getParts();
				while( pit.hasNextPart() ) {
					parts.add( pit.nextPart() );
				}
			}
		} catch( Throwable t ) {
			log.warn( "AssetPresentationBean() failed to loop through Asset, " +
					"Record, Parts: " + t.getMessage(), t );
		}

		this.asset = asset;
	}

	public String getAssetId() {
		return assetId;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}
	
	public Integer getContent() {
		return content;
	}
	
	public String getAuthor() throws org.osid.repository.RepositoryException,
	org.osid.shared.SharedException {
		org.osid.shared.ObjectIterator objIterator =
			asset.getPartValuesByPartStructure( CreatorPartStructure.getInstance().getId() );
		
		return ( objIterator.hasNextObject() ) ?
				((String)objIterator.nextObject() + ".") : null;
	}
	
	public String getSourceTitle() throws org.osid.repository.RepositoryException,
	org.osid.shared.SharedException {
		org.osid.shared.ObjectIterator objIterator =
			asset.getPartValuesByPartStructure( SourceTitlePartStructure.getInstance().getId() );
		
		return ( objIterator.hasNextObject() ) ?
				(String)objIterator.nextObject() : null;
	}
	
	public String getDate() throws org.osid.repository.RepositoryException,
	org.osid.shared.SharedException {
		org.osid.shared.ObjectIterator objIterator =
			asset.getPartValuesByPartStructure( DatePartStructure.getInstance().getId() );
		
		return ( objIterator.hasNextObject() ) ?
				(String)objIterator.nextObject() : null;
	}
	
	public String getCoverage() throws org.osid.repository.RepositoryException,
	org.osid.shared.SharedException {
		org.osid.shared.ObjectIterator objIterator =
			asset.getPartValuesByPartStructure( PagesPartStructure.getInstance().getId() );
		
		return ( objIterator.hasNextObject() ) ?
				(String)objIterator.nextObject() : null;
	}
	
	public String getUrl() throws org.osid.repository.RepositoryException,
	org.osid.shared.SharedException {
		org.osid.shared.ObjectIterator objIterator =
			asset.getPartValuesByPartStructure( URLPartStructure.getInstance().getId() );
		
		return ( objIterator.hasNextObject() ) ?
				(String)objIterator.nextObject() : null;
	}

	public java.util.ArrayList getParts() {
		return parts;
	}
}
