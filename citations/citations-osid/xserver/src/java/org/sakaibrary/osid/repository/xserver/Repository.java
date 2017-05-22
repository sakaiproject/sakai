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
import org.sakaibrary.xserver.XServerException;
import org.sakaibrary.xserver.XServer;
import org.sakaiproject.citation.util.impl.CQL2XServerFindCommand;

/** 
 * 
 * @author gbhatnag
 */
@Slf4j
public class Repository implements org.osid.repository.Repository
{
	private static final long serialVersionUID = 1L;

	private org.osid.shared.Id id = null;
	private String idString = null;
	private String displayName = null;
	private String description = null;

	// Types
	private org.osid.shared.Type repositoryType = new Type( "sakaibrary",
			"repository", "metasearch" );

	private org.osid.shared.Type assetType = new Type( "sakaibrary", "asset", 
	"citation" );

	private org.osid.shared.Type searchPropertiesType = new Type( "sakaibrary",
			"properties", "asynchMetasearch" );

	private org.osid.shared.Type searchStatusPropertiesType = new Type(
			"sakaibrary", "properties", "metasearchStatus" );

	private org.osid.shared.Type recordStructureType = new Type( "sakaibrary",
			"recordStructure", "citation" );

	// Properties
	private org.osid.shared.Properties searchStatusProperties = null;
	private org.osid.shared.Properties searchProperties = null;

	// Vectors for Iterators
	private java.util.Vector searchTypeVector = new java.util.Vector();

	/**
	 * Constructs a Repository
	 * 
	 * @param displayName
	 * @param description
	 * @param idString
	 * @param searchTypeVector
	 * @param idManager
	 * @throws org.osid.repository.RepositoryException
	 */
	protected Repository( String displayName
			, String description
			, String idString
			, java.util.Vector searchTypeVector
			, org.osid.id.IdManager idManager )
	throws org.osid.repository.RepositoryException
	{
		this.displayName = displayName;
		this.description = description;
		this.idString = idString;
		this.searchTypeVector = searchTypeVector;

		try {
			this.id = idManager.getId(this.idString);
		} catch (Throwable t) {
			log.warn(t.getMessage());
		}
	}

	public String getDisplayName()
	throws org.osid.repository.RepositoryException
	{
		return this.displayName;
	}

	public void updateDisplayName( String displayName )
	throws org.osid.repository.RepositoryException
	{
		// this data is Consumer read-only
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED );
	}

	public String getDescription()
	throws org.osid.repository.RepositoryException
	{
		return this.description;
	}

	public void updateDescription(String description)
	throws org.osid.repository.RepositoryException
	{
		// this data is Consumer read-only
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.shared.Id getId()
	throws org.osid.repository.RepositoryException
	{
		return this.id;
	}

	public org.osid.shared.Type getType()
	throws org.osid.repository.RepositoryException
	{
		return this.repositoryType;
	}

	public org.osid.repository.Asset createAsset( String displayName
			, String description
			, org.osid.shared.Type assetType )
	throws org.osid.repository.RepositoryException
	{
		if( (displayName == null ) || (description == null) || (assetType == null) )
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		if (!assetType.isEqual(this.assetType))
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.UNKNOWN_TYPE);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public void deleteAsset(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.repository.AssetIterator getAssets()
	throws org.osid.repository.RepositoryException
	{
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.repository.AssetIterator getAssetsByType(
			org.osid.shared.Type assetType)
	throws org.osid.repository.RepositoryException
	{
		if (assetType == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.shared.TypeIterator getAssetTypes()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		results.addElement( this.assetType );

		try
		{
			return new TypeIterator( results );
		}
		catch (Throwable t)
		{
			log.warn(t.getMessage());
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public org.osid.repository.RecordStructureIterator getRecordStructures()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		results.addElement(RecordStructure.getInstance());
		return new RecordStructureIterator(results);
	}

	public org.osid.repository.RecordStructureIterator
	getMandatoryRecordStructures(org.osid.shared.Type assetType)
	throws org.osid.repository.RepositoryException
	{
		if (assetType == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		if (assetType.isEqual(this.assetType))
		{
			java.util.Vector results = new java.util.Vector();
			results.addElement(RecordStructure.getInstance());
			return new RecordStructureIterator(results);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.shared.SharedException.UNKNOWN_TYPE);
	}

	public org.osid.shared.TypeIterator getSearchTypes()
	throws org.osid.repository.RepositoryException
	{
		try
		{
			return new TypeIterator(this.searchTypeVector);
		}
		catch (Throwable t)
		{
			log.warn(t.getMessage());
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public org.osid.shared.TypeIterator getStatusTypes()
	throws org.osid.repository.RepositoryException
	{
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.shared.Type getStatus(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public boolean validateAsset(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public void invalidateAsset(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.repository.Asset getAsset(org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.repository.Asset getAssetByDate(org.osid.shared.Id assetId
			, long date)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.shared.LongValueIterator getAssetDates(
			org.osid.shared.Id assetId)
	throws org.osid.repository.RepositoryException
	{
		if (assetId == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.repository.AssetIterator getAssetsBySearch(
			java.io.Serializable searchCriteria
			, org.osid.shared.Type searchType
			, org.osid.shared.Properties searchProperties )
	throws org.osid.repository.RepositoryException
	{
		java.util.ArrayList<String> databaseIds = null;
		String guid = null;
		String baseUrl = null;
		String username = null;
		String password = null;
		boolean knownPropertiesType = false;
		String criteria = null;

		/* check parameters */
		if( searchCriteria == null || searchType == null ) {
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT );
		}

		if( !( searchCriteria instanceof String ) ) {
			log.warn( "getAssetsBySearch() invalid search criteria: " + searchCriteria );
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED );
		}

		// check searchProperties
		if( searchProperties == null ) {
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT );
		} else {
			// check searchProperties type
			try {
				knownPropertiesType = searchProperties.getType().isEqual(
						searchPropertiesType );
			} catch( org.osid.shared.SharedException se ) {
				log.warn( "Unable to check searchProperties Type" );
			}
			if( !knownPropertiesType ) {
				log.warn( "searchProperties are of unknown type" );
				throw new org.osid.repository.RepositoryException(
						org.osid.shared.SharedException.UNKNOWN_TYPE );
			}

			// check if required fields are part of searchProperties
			try {
				databaseIds = ( java.util.ArrayList<String> )
				searchProperties.getProperty( "databaseIds" );
				guid        = ( String ) searchProperties.getProperty( "guid" );
				baseUrl     = ( String ) searchProperties.getProperty( "baseUrl" );
				username    = ( String ) searchProperties.getProperty( "username" );
				password    = ( String ) searchProperties.getProperty( "password" );
			} catch( org.osid.shared.SharedException se ) {
				log.warn( "Problem getting guid from org.osid.shared.Properties " +
						"object passed to getAssetsBySearch().", se );
				throw new org.osid.repository.RepositoryException(
						org.osid.OsidException.OPERATION_FAILED );
			}

			if( guid == null || guid.trim().equals( "" ) ||
					baseUrl == null || baseUrl.trim().equals( "" ) ||
					username == null || username.trim().equals( "" ) ||
					password == null || password.trim().equals( "" ) ) {
				log.warn( "required search property is null or empty:" +
						"\n  guid: " + guid +
						"\n  baseUrl: " + baseUrl +
						"\n  username: " + username +
						"\n  password: " + password );
				throw new org.osid.repository.RepositoryException(
						org.osid.OsidException.NULL_ARGUMENT );
			}

			if( databaseIds == null || databaseIds.size() == 0 ) {
				log.warn( "ERROR: databaseIds from org.osid.shared.Properties is " +
				"null or empty" );
				throw new org.osid.repository.RepositoryException(
						org.osid.OsidException.OPERATION_FAILED );
			}
		}

		// check search type
		for( int i = 0; i < searchTypeVector.size(); i ++ ) {
			org.osid.shared.Type type = ( org.osid.shared.Type )
			( searchTypeVector.elementAt( i ) );

			if( !type.isEqual( searchType ) ) {
				log.warn( "searchType is of unknown type" );
				throw new org.osid.repository.RepositoryException(
						org.osid.shared.SharedException.UNKNOWN_TYPE );
			}
		}

		/* 
		 * parameter checking clear, conduct an X-Server metasearch
		 */

		// update searchProperties
		this.searchProperties = searchProperties;

		// convert the cql-formatted searchCriteria to X-Server formatted find_command
		criteria = doCQL2FindCommand( ( String ) searchCriteria );

		// setup Metasearch session
		org.sakaibrary.xserver.session.MetasearchSessionManager msm = 
			org.sakaibrary.xserver.session.MetasearchSessionManager.getInstance();
		org.sakaibrary.xserver.session.MetasearchSession metasearchSession =
			msm.getMetasearchSession( guid );

		if( metasearchSession == null ) {
			metasearchSession = new org.sakaibrary.xserver.session.MetasearchSession(
					guid );
		}

		// update searchStatusProperties
		java.util.Properties searchStatusProperties = new java.util.Properties();
		searchStatusProperties.put( "status", "searching" );
		searchStatusProperties.put( "statusMessage", "search has just begun" );

		// establish the following session items for a guaranteed fresh session
		metasearchSession.setLoggedIn( false );
		metasearchSession.setUsername( username );
		metasearchSession.setPassword( password );
		metasearchSession.setSessionId( null );
		metasearchSession.setBaseUrl( baseUrl );
		metasearchSession.setRepositoryId( this.id );
		metasearchSession.setRepositoryDisplayName( this.displayName );
		metasearchSession.setSearchProperties( searchProperties );
		metasearchSession.setSearchStatusProperties( searchStatusProperties );
		metasearchSession.setSingleSearchSource( ( databaseIds.size() > 1 ) ? false : true );
		metasearchSession.setGotMergeError( false );
		metasearchSession.setFoundGroupNumber( null );
		metasearchSession.setMergedGroupNumber( null );
		metasearchSession.setRecordsSetNumber( null );
		metasearchSession.setNumRecordsFound( new Integer( 0 ) );
		metasearchSession.setNumRecordsFetched( new Integer( 0 ) );
		metasearchSession.setNumRecordsMerged( new Integer( 0 ) );
		msm.putMetasearchSession( guid, metasearchSession );

		// get an XServer
		XServer xserver = null;

		try {
			xserver = new XServer( guid );
		} catch( XServerException xse ) {
			if( xse.getErrorCode() != null && !xse.getErrorCode().trim().equals("") )
			{
				log.warn( "X-Server error " + xse.getErrorCode() + " - " +
						xse.getErrorText() );
			}
			else
			{
				log.warn( "X-Server error - " + xse.getErrorText() );
			}
			throw new org.osid.repository.RepositoryException(
					org.sakaibrary.osid.repository.xserver.
					MetasearchException.METASEARCH_ERROR );
		}

		try {
			// initiate the asynchronous search
			xserver.initAsynchSearch( criteria, databaseIds );
		} catch( XServerException xse ) {
			if( xse.getErrorCode() != null && !xse.getErrorCode().trim().equals("") )
			{
				log.warn( "X-Server error " + xse.getErrorCode() + " - " +
						xse.getErrorText() );
			}
			else
			{
				log.warn( "X-Server error - " + xse.getErrorText() );
			}
			throw new org.osid.repository.RepositoryException(
					org.sakaibrary.osid.repository.xserver.
					MetasearchException.METASEARCH_ERROR );
		}

		// return an empty AssetIterator
		return new AssetIterator( guid );
	}

	public org.osid.shared.Id copyAsset(org.osid.repository.Asset asset)
	throws org.osid.repository.RepositoryException
	{
		throw new org.osid.repository.RepositoryException(
				org.osid.OsidException.UNIMPLEMENTED);
	}

	public org.osid.repository.RecordStructureIterator getRecordStructuresByType(
			org.osid.shared.Type recordStructureType )
	throws org.osid.repository.RepositoryException
	{
		if (recordStructureType == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}
		if( recordStructureType.isEqual( this.recordStructureType ) )
		{
			java.util.Vector results = new java.util.Vector();
			results.addElement( RecordStructure.getInstance() );
			return new RecordStructureIterator( results );
		}
		throw new org.osid.repository.RepositoryException(
				org.osid.shared.SharedException.UNKNOWN_TYPE );
	}

	public org.osid.shared.PropertiesIterator getProperties()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();

		results.addElement( searchProperties );
		String guid = null;
		try {
			guid = ( String ) searchProperties.getProperty( "guid" );
		} catch( org.osid.shared.SharedException se ) {
			log.warn( "getProperties() could not get guid: " +
					se.getMessage(), se );
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED);
		}

		if( searchProperties != null ) {
			try {
				XServer xserver = new XServer( guid );
				searchStatusProperties = xserver.getSearchStatusProperties();
			} catch( XServerException xse ) {
				// ignore
			}
		} else {
			searchStatusProperties = null;
		}

		results.addElement( searchStatusProperties );
		try
		{
			return new PropertiesIterator( results );
		}
		catch (Throwable t)
		{
			log.warn(t.getMessage());
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public org.osid.shared.Properties getPropertiesByType(
			org.osid.shared.Type propertiesType )
	throws org.osid.repository.RepositoryException
	{
		if (propertiesType == null)
		{
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NULL_ARGUMENT);
		}

		org.osid.shared.PropertiesIterator pi = getProperties();

		try {
			while( pi.hasNextProperties() ) {
				org.osid.shared.Properties properties = pi.nextProperties();
				if( properties.getType().isEqual( propertiesType ) ) {
					return properties;
				}
			}
		} catch( org.osid.shared.SharedException se ) {
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED );
		} catch( Exception e ) {
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED );
		}

		// didn't find a match for the given type
		throw new org.osid.repository.RepositoryException(
				org.osid.shared.SharedException.UNKNOWN_TYPE );
	}

	public org.osid.shared.TypeIterator getPropertyTypes()
	throws org.osid.repository.RepositoryException
	{
		java.util.Vector results = new java.util.Vector();
		results.addElement( searchPropertiesType );
		results.addElement( searchStatusPropertiesType );

		try
		{
			return new TypeIterator( results );
		}
		catch (Throwable t)
		{
			log.warn(t.getMessage());
			throw new org.osid.repository.RepositoryException(
					org.osid.OsidException.OPERATION_FAILED);
		}
	}

	public boolean supportsUpdate()
	throws org.osid.repository.RepositoryException
	{
		return false;
	}

	public boolean supportsVersioning()
	throws org.osid.repository.RepositoryException
	{
		return false;
	}

	private String doCQL2FindCommand( String cqlString ) {
		CQL2XServerFindCommand cql2Xserver = new CQL2XServerFindCommand();
		return cql2Xserver.doCQL2MetasearchCommand( cqlString );
	}
}
