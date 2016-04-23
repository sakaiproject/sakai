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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import lombok.extern.slf4j.Slf4j;
import org.sakaibrary.xserver.session.MetasearchSession;
import org.sakaibrary.xserver.session.MetasearchSessionManager;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * @author gbhatnag
 * @version
 */
@Slf4j
public class AssetIterator extends org.xml.sax.helpers.DefaultHandler
implements org.osid.repository.AssetIterator {
  /*
   * Xserver error codes
   */
  public static final int XSERVER_ERROR_MERGE_LIMIT = 134;
  public static final int XSERVER_ERROR_ALL_MERGED  = 137;

	private static final long serialVersionUID = 1L;
	private static final String REGULAR_EXPRESSION_FILE = "/data/citationRegex.txt";

	private java.util.LinkedList assetQueue;
	private java.util.ArrayList regexArray;
	private String guid;
	private int totalRecordsCursor = 0;
	private int numRecordsReturned = 0;
	private org.osid.shared.Id repositoryId;
	private org.osid.shared.Id recordStructureId;
	private org.osid.repository.Asset asset;
	private org.osid.repository.Record record;
	/*
	 * Preferred URL handling
	 */
	private String preferredUrl;
	private String preferredUrlFormat;

	// for SAX parsing
	private StringBuilder textBuffer;

	// session
	private MetasearchSessionManager msm;
	org.osid.shared.Properties statusProperties;


	/**
	 * Constructs an empty AssetIterator
	 *
	 * @param guid globally unique identifier for this session
	 * @throws org.osid.repository.RepositoryException
	 */
	protected AssetIterator( String guid )
	throws org.osid.repository.RepositoryException {
		this.guid = guid;

		// get session cache manager
		msm = MetasearchSessionManager.getInstance();

		// create assetQueue
		assetQueue = new java.util.LinkedList();

		// load citation regular expressions
		try {
			regexArray = loadCitationRegularExpressions( REGULAR_EXPRESSION_FILE );
		} catch( java.io.IOException ioe ) {
			log.warn( "AssetIterator() failed reading citation regular " +
					"expressions - regex file: " + REGULAR_EXPRESSION_FILE, ioe );
		}
	}

	private java.util.ArrayList loadCitationRegularExpressions( String filename )
	throws java.io.IOException {
		java.util.ArrayList regexArray = new java.util.ArrayList();

		java.io.InputStream is = this.getClass().getResourceAsStream( filename );
		try {
    		java.io.BufferedReader regexes = new java.io.BufferedReader(
    				new java.io.InputStreamReader( is ) );
    		try {
        		// read the regex file and add regexes to array
        		String regex;
        		while( ( regex = regexes.readLine() ) != null ) {
        			String [] nameRegex = regex.split( "=" );
        
        			CitationRegex citationRegex = new CitationRegex();
        			citationRegex.setName( nameRegex[ 0 ].trim() );
        			citationRegex.setRegex( nameRegex[ 1 ].trim() );
        
        			regexArray.add( citationRegex );
        		}
    		} finally {
    		    regexes.close();
    		}
		} finally {
	        is.close();
		}

		return regexArray;
	}

	public boolean hasNextAsset()
	throws org.osid.repository.RepositoryException {
		MetasearchSession metasearchSession = msm.getMetasearchSession( guid );

		// get an XServer to check status and update number of records found
		org.sakaibrary.xserver.XServer xserver = null;
		statusProperties = null;
		try {
			xserver = new org.sakaibrary.xserver.XServer( guid );
      xserver.updateSearchStatusProperties();
			statusProperties = xserver.getSearchStatusProperties();
		} catch( org.sakaibrary.xserver.XServerException xse ) {
			log.warn( "X-Server error: " + xse.getErrorCode() +
					" - " + xse.getErrorText() );

      // throw exception now that status has been updated
      throw new org.osid.repository.RepositoryException(
          org.sakaibrary.osid.repository.xserver.MetasearchException.
          METASEARCH_ERROR );
		}

		// check status for error/timeout
		String status = null;

		try {
			status = ( String ) statusProperties.getProperty( "status" );
		} catch( org.osid.shared.SharedException se ) {
			log.warn( "hasNextAsset() failed getting status " +
					"property", se );
		}

		if( status != null ) {
      // status and statusMessage are set by XServer.updateSearchStatusProperties
			if( status.equals( "error" ) ) {
			  throw new org.osid.repository.RepositoryException(
						org.sakaibrary.osid.repository.xserver.MetasearchException.
						METASEARCH_ERROR );
			} else if( status.equals( "timeout" ) ) {
				throw new org.osid.repository.RepositoryException(
						org.sakaibrary.osid.repository.xserver.MetasearchException.
						SESSION_TIMED_OUT );
			} else if( status.equals( "empty" ) ) {
				// no records found
				return false;
			}
		} else {
			log.warn( "hasNextAsset() - status property is null" );
		}

		// get updated metasearchSession
		metasearchSession = msm.getMetasearchSession( guid );
		Integer numRecordsFound = metasearchSession.getNumRecordsFound();

		if( numRecordsFound == null || numRecordsFound.intValue() == 0 ) {
			// still searching for records, return true
			return true;
		}

		// check if passed max number of attainable records
    int maxAttainable;
    boolean gotMergeError = metasearchSession.isGotMergeError();
    if( gotMergeError ) {
      maxAttainable = 300;
    } else {
      maxAttainable = numRecordsFound.intValue();
    }

		return ( numRecordsReturned < maxAttainable );
	}

	public org.osid.repository.Asset nextAsset()
	throws org.osid.repository.RepositoryException {
		log.debug( "nextAsset() [entry] - returned: " + numRecordsReturned + "; total: " +
        totalRecordsCursor + "; in queue: " + assetQueue.size() );

		// return Asset, if ready
		if( assetQueue.size() > 0 ) {
			numRecordsReturned++;
			return ( org.osid.repository.Asset ) assetQueue.removeFirst();
		}

		// assetQueue is empty - check whether we should get more records
		// or throw an Exception
		if( hasNextAsset() ) {
			// hasNextAsset() will throw timeout/error Exceptions if any
			String status = null;

			try {
				status = ( String ) statusProperties.getProperty( "status" );
			} catch( org.osid.shared.SharedException se ) {
				log.warn( "nextAsset() failed getting status property", se );
			}

			if( status == null || !status.equals( "ready" ) ) {
				// the X-Server is still searching/fetching - try again later
				throw new org.osid.repository.RepositoryException(
						org.sakaibrary.osid.repository.xserver.
						MetasearchException.ASSET_NOT_FETCHED );
			}

			// get records from the X-Server
			MetasearchSession metasearchSession = msm.getMetasearchSession( guid );
			org.osid.shared.Id repositoryId = metasearchSession.getRepositoryId();

			try {
			  org.sakaibrary.xserver.XServer xserver =
			    new org.sakaibrary.xserver.XServer( guid );

			  log.debug( "nextAsset() calling XServer.getRecordsXML() - assets in " +
			      "queue: " + assetQueue.size() );
			  createAssets( xserver.getRecordsXML( totalRecordsCursor ),
			      repositoryId );
			} catch( org.sakaibrary.xserver.XServerException xse ) {
			  log.warn( "X-Server error: " + xse.getErrorCode() + " - " +
			      xse.getErrorText() );
        //
        // Have all (or too many) records been merged?  If so, indicate
        // we've fetched everything we can (end-of-file)
        //
        if ((xse.getErrorCodeIntValue() == XSERVER_ERROR_MERGE_LIMIT) ||
            (xse.getErrorCodeIntValue() == XSERVER_ERROR_ALL_MERGED))
        {
          log.debug("nextAsset(), Xserver Error "
                +                 xse.getErrorCodeIntValue()
                +                 ", throwing NO_MORE_ITERATOR_ELEMENTS");

       		throw new org.osid.repository.RepositoryException(
  				          org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS);
        }
        //
        // Search error
        //
        throw new org.osid.repository.RepositoryException(
			      org.sakaibrary.osid.repository.xserver.MetasearchException.
			      METASEARCH_ERROR );
			}
			log.debug( "nextAsset(), XServer.getRecordsXML() returns - assets in " +
			    "queue: " + assetQueue.size() );
      //
      // Make sure there really is an asset available - if not, signal "end-of-file"
      //
      // Note: this issue can come up if a database provides an estimate but
      // no actual results
      //
      if (assetQueue.size() == 0)
      {
        log.debug("nextAsset(), An asset is expected, but the asset queue is enpty");

     		throw new org.osid.repository.RepositoryException(
				          org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS);
      }
      //
			// records have been fetched and Assets queued
			//
			totalRecordsCursor += assetQueue.size();
			numRecordsReturned++;
			return ( org.osid.repository.Asset ) assetQueue.removeFirst();
		} else {
			// no assets available
			throw new org.osid.repository.RepositoryException(
					org.osid.shared.SharedException.NO_MORE_ITERATOR_ELEMENTS );
		}
	}

	/**
	 * This method parses the xml StringBuilder and creates Assets, Records
	 * and Parts in the Repository with the given repositoryId.
	 *
	 * @param xml input xml in "sakaibrary" format
	 * @param log the log being used by the Repository
	 * @param repositoryId the Id of the Repository in which to create Assets,
	 * Records and Parts.
	 *
	 * @throws org.osid.repository.RepositoryException
	 */
	private void createAssets( java.io.ByteArrayInputStream xml,
			org.osid.shared.Id repositoryId )
		throws org.osid.repository.RepositoryException {
		this.repositoryId = repositoryId;
		recordStructureId = RecordStructure.getInstance().getId();
		textBuffer = new StringBuilder();

		// use a SAX parser
		javax.xml.parsers.SAXParserFactory factory;
		javax.xml.parsers.SAXParser saxParser;

		// set up the parser
		factory = javax.xml.parsers.SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );

		// start parsing
		try {
		  saxParser = factory.newSAXParser();
		  saxParser.parse( xml, this );
		  xml.close();
		} catch (SAXParseException spe) {
		  // Use the contained exception, if any
		  Exception x = spe;

		  if (spe.getException() != null) {
		    x = spe.getException();
		  }

		  // Error generated by the parser
		  log.warn("createAssets() parsing exception: " +
		      spe.getMessage() + " - xml line " + spe.getLineNumber() +
		      ", uri " + spe.getSystemId(), x );
		} catch (SAXException sxe) {
		  // Error generated by this application
		  // (or a parser-initialization error)
		  Exception x = sxe;

		  if (sxe.getException() != null) {
		    x = sxe.getException();
		  }

		  log.warn( "createAssets() SAX exception: " + sxe.getMessage(),	x );
		} catch (ParserConfigurationException pce) {
		  // Parser with specified options can't be built
		  log.warn( "createAssets() SAX parser cannot be built with " +
		  "specified options" );
		} catch (IOException ioe) {
		  // I/O error
		  log.warn( "createAssets() IO exception", ioe );
		}
	}

	//----------------------------------
	// SAX DEFAULT HANDLER IMPLEMENTATIONS -
	//----------------------------------

	/**
	 * Receive notification of the beginning of an element.
	 *
	 * @see DefaultHandler
	 */
	public void startElement( String namespaceURI, String sName,
			String qName, org.xml.sax.Attributes attrs ) throws
			org.xml.sax.SAXException {
		if( qName.equals( "record" ) ) {
			populateAssetFromText( "record_start" );
			/*
			 * No preferred URL seen (yet)
			 */
			preferredUrl = null;
			preferredUrlFormat = null;
		}
	}

	/**
	 * Receive notification of the end of an element.
	 *
	 * @see DefaultHandler
	 */
	public void endElement( String namespaceURI, String sName, String qName )
	throws org.xml.sax.SAXException {
		populateAssetFromText( qName );
	}

	/**
	 * Receive notification of character data inside an element.
	 *
	 * @see DefaultHandler
	 */
	public void characters( char[] buf, int offset, int len )
	throws org.xml.sax.SAXException {
		// store character data
		String text = new String( buf, offset, len );

		if( textBuffer == null ) {
			textBuffer = new StringBuilder( text );
		} else {
			textBuffer.append( text );
		}
	}

	private void populateAssetFromText( String elementName ) {
		// new record
		if( elementName.equals( "record_start" ) ) {
			try {
			// create a new asset... need title, description, assetId
			asset = new Asset( null, null, getId(), repositoryId );

			// create a new record
			record = asset.createRecord( recordStructureId );
			} catch( org.osid.repository.RepositoryException re ) {
				log.warn( "populateAssetFromText() failed to " +
						"create new Asset/Record pair.", re );
			}
		} else if( elementName.equals( "record" ) ) {
			// a record has ended: do post-processing //

			// set dateRetrieved
			setDateRetrieved();

			// use inLineCitation to fill in other fields, if possible
			org.osid.repository.Part inLineCitation;
			try {
				if( ( inLineCitation = recordHasPart(
						InLineCitationPartStructure.getInstance().getType() ) )
						!= null ) {
					doRegexParse( ( String )inLineCitation.getValue() );
				}
			} catch( org.osid.repository.RepositoryException re ) {
				log.warn( "populateAssetFromText() failed to " +
						"gracefully process inLineCitation value.", re );
			}

      // create a preferred URL (if we found all the parts)
      try 
      {
        if (preferredUrl != null)
        {
          if ((preferredUrlFormat != null) &&
             !(preferredUrlFormat.equalsIgnoreCase("HTML")))
          {
  				  log.debug("Unexpected URL format: " + preferredUrlFormat);
  			  }

          if ((preferredUrlFormat == null) || 
              (preferredUrlFormat.equalsIgnoreCase("HTML")))
          {
  				  record.createPart(PreferredUrlPartStructure.getInstance().getId(), 
  				                    preferredUrl);
  			  }
  			}
  		} 
  		catch( org.osid.repository.RepositoryException exception) 
  		{
  			log.warn("Failed to create preferred URL Part", exception);
  		}
  		finally
  		{
  		  preferredUrl = null;
  		  preferredUrlFormat = null;
  		}

      // All done with this asset
			assetQueue.add( asset );
			return;
		}

		if( textBuffer == null ) {
			return;
		}

		String text = textBuffer.toString().trim();
		if( text.equals( "" ) ) {
			return;
		}

		try {
			if( elementName.equals( "title" ) ) {
				asset.updateDisplayName( text );
			} else if( elementName.equals( "abstract" ) ) {
				asset.updateDescription( text );
			} else if( elementName.equals( "author" ) ) {
				record.createPart( CreatorPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "date" ) ) {
				record.createPart( DatePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "doi" ) ) {
				record.createPart( DOIPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "edition" ) ) {
				record.createPart( EditionPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "inLineCitation" ) ) {
				record.createPart( InLineCitationPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "isnIdentifier" ) ) {
				record.createPart( IsnIdentifierPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "issue" ) ) {
				record.createPart( IssuePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "language" ) ) {
				record.createPart( LanguagePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "note" ) ) {
				record.createPart( NotePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "openUrl" ) ) {
				record.createPart( OpenUrlPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "pages" ) ) {
				createPagesPart( text );
			} else if( elementName.equals( "publisherInfo" ) ) {
				record.createPart( PublisherPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "rights" ) ) {
				record.createPart( RightsPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "sourceTitle" ) ) {
				record.createPart( SourceTitlePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "subject" ) ) {
				record.createPart( SubjectPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "type" ) ) {
				record.createPart( TypePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "url" ) ) {
				record.createPart( URLPartStructure.getInstance().getId(), text );
				preferredUrl = text;
			} else if( elementName.equals( "urlLabel" ) ) {
				record.createPart( URLLabelPartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "urlFormat" ) ) {
				record.createPart( URLFormatPartStructure.getInstance().getId(),
						text );
			  preferredUrlFormat = text;
			} else if( elementName.equals( "volume" ) ) {
				record.createPart( VolumePartStructure.getInstance().getId(),
						text );
			} else if( elementName.equals( "volumeIssue" ) ) {
				doRegexParse( text );
			} else if( elementName.equals( "year" ) ) {
				record.createPart( YearPartStructure.getInstance().getId(),
						text );
			}
		} catch( org.osid.repository.RepositoryException re ) {
			log.warn( "populateAssetFromText() failed to " +
					"create new Part.", re );
		}

		textBuffer = null;
	}

	private void setDateRetrieved() {
		java.util.GregorianCalendar now = new java.util.GregorianCalendar();
		int month = now.get( java.util.Calendar.MONTH ) + 1;
		int date = now.get( java.util.Calendar.DATE );
		String monthStr, dateStr;

		if( month < 10 ) {
			monthStr = "0" + month;
		} else {
			monthStr = String.valueOf( month );
		}

		if( date < 10 ) {
			dateStr = "0" + date;
		} else {
			dateStr = String.valueOf( date );
		}
		String dateRetrieved = now.get( java.util.Calendar.YEAR ) + "-" +
		monthStr + "-" + dateStr;

		try {
			record.createPart( DateRetrievedPartStructure.getInstance().getId(),
				dateRetrieved );
		} catch( org.osid.repository.RepositoryException re ) {
			log.warn( "setDateRetrieved() failed " +
					"creating new dateRetrieved Part.", re );
		}
	}

	/**
	 * This method searches the current record for a Part using its
	 * PartStructure Type.
	 *
	 * @param partStructureType PartStructure Type of Part you need.
	 * @return the Part if it exists in the current record, null if it does not.
	 */
	private org.osid.repository.Part recordHasPart(
			org.osid.shared.Type partStructureType ) {
		try {
			org.osid.repository.PartIterator pit = record.getParts();

			while( pit.hasNextPart() ) {
				org.osid.repository.Part part = pit.nextPart();

				if( part.getPartStructure().getType().isEqual( partStructureType ) ) {
					return part;
				}
			}
		} catch( org.osid.repository.RepositoryException re ) {
			log.warn( "recordHasPart() failed getting Parts.", re );
		}

		// did not find the Part
		return null;
	}

	/**
	 * This method does its best to map data contained in an inLineCitation to
	 * other fields such as volume, issue, etc. in the case that they are empty.
	 * It compares the citation to a known set of regular expressions contained
	 * in REGULAR_EXPRESSION_FILE.  Adding a new regular expression entails
	 * adding a new case for parsing in this method.
	 *
	 * @param citation inLineCitation to be parsed
	 */
	private void doRegexParse( String citation ) {
		String regexName = null;
		Pattern pattern;
		Matcher matcher;
		boolean hasVolume = false;
		boolean hasIssue = false;
		boolean hasDate = false;
		boolean hasPages = false;
		boolean hasSourceTitle = false;

		for( int i = 0; i < regexArray.size(); i++ ) {
			CitationRegex citationRegex = ( CitationRegex ) regexArray.get( i );
			pattern = Pattern.compile( citationRegex.getRegex() );
			matcher = pattern.matcher( citation );

			if( matcher.find() ) {
				regexName = citationRegex.getName();
				break;
			}
		}

		if( regexName != null ) {
			// determine which fields are necessary
			try {
				hasVolume =
					recordHasPart( VolumePartStructure.getInstance().getType() )
					== null ? false : true;

				hasIssue =
					recordHasPart( IssuePartStructure.getInstance().getType() )
					== null ? false : true;

				hasDate =
					recordHasPart( DatePartStructure.getInstance().getType() )
					== null ? false : true;

				hasPages =
					recordHasPart( PagesPartStructure.getInstance().getType() )
					== null ? false : true;

				hasSourceTitle =
					recordHasPart( SourceTitlePartStructure.getInstance().getType() )
					== null ? false : true;

				// if all true, no need to go further
				if( hasVolume && hasIssue && hasDate && hasPages && hasSourceTitle ) {
					return;
				}

				// check for matching regex
				if( regexName.equals( "zooRec" ) ) {
					// .+ \d+(\(\d+\))?, (.*)? \d{4}: \d+-\d+
					if( !hasVolume ) {
						pattern = Pattern.compile( "\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group() );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "\\(\\d+\\)" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasDate ) {
						pattern = Pattern.compile( ", (.*)? \\d{4}:" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String date = matcher.group().substring( 2,
									matcher.group().length()-1 );
							record.createPart( DatePartStructure.getInstance().getId(),
									date );
						}
					}

					if( !hasPages ) {
						pattern = Pattern.compile( "\\d+-\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							createPagesPart( matcher.group() );
						}
					}

					if( !hasSourceTitle ) {
						pattern = Pattern.compile( "\\D+\\d" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String sourceTitle = matcher.group().substring( 0,
									matcher.group().length()-2 );
							record.createPart(
									SourceTitlePartStructure.getInstance().getId(),
									sourceTitle );
						}
					}
				} else if( regexName.equals( "animBehavAbs" ) ) {
					// .+ Vol\. \d+, no\. \d+, (\d+)? pp\.|p\. \d+(-\d+.)? (.*)? \d{4}\.$
					if( !hasVolume ) {
						pattern = Pattern.compile( "Vol\\. \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "no\\. \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasDate ) {
						pattern = Pattern.compile( "(pp\\.|p\\.) \\d+(-\\d+\\.)? (.*)? \\d{4}\\.$" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String date = matcher.group().substring(
									matcher.group().indexOf( " ", 4 ) + 1,
									matcher.group().length()-1 );
							record.createPart( DatePartStructure.getInstance().getId(),
									date );
						}
					}

					if( !hasPages ) {
						pattern = Pattern.compile( "(pp\\.|p\\.) \\d+(-\\d+\\.)?" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							createPagesPart( matcher.group() );
						}
					}

					if( !hasSourceTitle ) {
						pattern = Pattern.compile( ".+ \\[" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String sourceTitle = matcher.group().substring( 0,
									matcher.group().length() - 2 );
							record.createPart(
									SourceTitlePartStructure.getInstance().getId(),
									sourceTitle );
						}
					}
				} else if( regexName.equals( "pubMed" ) ) {
					// .+ (Volume: \\d+, )?Issue: ((\\d+)|(\\w+)), Date: \\d{4} \\d+ \\d+,( Pages: \\d+-\\d+)?
					if( !hasVolume ) {
						pattern = Pattern.compile( "Volume: \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "Issue: ((\\d+)|(\\w+))" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String issue = matcher.group().substring( 7,
									matcher.group().length() );
							record.createPart( IssuePartStructure.getInstance().getId(),
									issue );
						}
					}

					if( !hasDate ) {
						pattern = Pattern.compile( "Date: \\d{4} \\d+ \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String date = matcher.group().substring( 6,
									matcher.group().length() );
							date = date.replaceAll( "\\s", "-" );
							record.createPart( DatePartStructure.getInstance().getId(),
									date );
						}
					}

					if( !hasPages ) {
						pattern = Pattern.compile( "\\d+-\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							createPagesPart( matcher.group() );
						}
					}

					if( !hasSourceTitle ) {
						pattern = Pattern.compile( ".+\\. Vol" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String sourceTitle = matcher.group().substring( 0,
									matcher.group().length()-5 );
							record.createPart(
									SourceTitlePartStructure.getInstance().getId(),
									sourceTitle );
						}
					}
				} else if( regexName.equals( "isiWos" ) ) {
					// ^\d+( \(\d+\))?: \w+-.+(.+)?( \w{3})?( \w{3}-\w{3})?( \d+)? \d{4}$
					if( !hasVolume ) {
						pattern = Pattern.compile( "^\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group() );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "\\(\\d+\\)" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasDate ) {
						pattern = Pattern.compile( "( \\w{3})?( \\w{3}-\\w{3})?( \\d+)? \\d{4}$" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( DatePartStructure.getInstance().getId(),
									matcher.group().trim() );
						}
					}

					if( !hasPages ) {
						pattern = Pattern.compile( " \\w+(-\\w+)?" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							createPagesPart( matcher.group().trim() );
						}
					}
				} else if( regexName.equals( "jstor" ) ) {
					// .+, Vol\. \d+(, No\. \d+)?
					if( !hasVolume ) {
						pattern = Pattern.compile( "Vol\\. \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "No\\. \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasSourceTitle ) {
						pattern = Pattern.compile( ".+, Vol" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							String sourceTitle = matcher.group().substring( 0,
									matcher.group().length() - 5 );
							record.createPart(
									SourceTitlePartStructure.getInstance().getId(),
									sourceTitle );
						}
					}
				} else if( regexName.equals( "eric" ) ) {
					// ^v\d+ n|v\d+ p\d+-\d+( \w{3})?( \w{3}-\w{3})?( \d+)? \d{4}$
					if( !hasVolume ) {
						pattern = Pattern.compile( "^v\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( " (n|v)\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().trim().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasDate ) {
						pattern = Pattern.compile( "( \\w{3})?( \\w{3}-\\w{3})?( \\d+)? \\d{4}$" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( DatePartStructure.getInstance().getId(),
									matcher.group().trim() );
						}
					}

					if( !hasPages ) {
						pattern = Pattern.compile( "\\d+-\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							createPagesPart( matcher.group() );
						}
					}
				} else if( regexName.equals( "proquest" ) ) {
					// ^\d+; \d+(; .+)?
					if( !hasVolume ) {
						pattern = Pattern.compile( "^\\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group() );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "; \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasSourceTitle ) {
						pattern = Pattern.compile( "; \\D+$" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( SourceTitlePartStructure.getInstance().getId(),
									matcher.group().substring( 2, matcher.group().length() ) );
						}
					}
				} else if( regexName.equals( "psycInfo" ) ) {
					// ^Vol \d+\([\w\p{Punct}]+\))
					if( !hasVolume ) {
						pattern = Pattern.compile( "^Vol \\d+" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( VolumePartStructure.getInstance().getId(),
									matcher.group().replaceAll( "\\D", "" ) );
						}
					}

					if( !hasIssue ) {
						pattern = Pattern.compile( "\\(.+\\)" );
						matcher = pattern.matcher( citation );
						if( matcher.find() ) {
							record.createPart( IssuePartStructure.getInstance().getId(),
									matcher.group().substring( 1,
											matcher.group().length() - 1 ) );
						}
					}
				}
			} catch( org.osid.repository.RepositoryException re ) {
				log.warn( "doRegexParse() failed getting " +
						"PartStructure Types.", re );
			}
		}
	}

	private void createPagesPart( String text )
	throws org.osid.repository.RepositoryException {
		if( text.charAt( 0 ) == ',' ) {
			// getting a poorly formatted field
			return;
		}

		record.createPart( PagesPartStructure.getInstance().getId(), text );

		// get start and end page if possible
		String [] pages = text.split( "-" );

		if( pages.length == 0 ) {
			// cannot create start/end page.
			return;
		}

		String spage = pages[ 0 ].trim();

		// delete all non-digit chars (ie: p., pp., etc.)
		spage = spage.replaceAll( "\\D", "" );

		// create startPage part
		record.createPart( StartPagePartStructure.getInstance().getId(),
				spage );

		// end page
		if( pages.length == 2 ) {
			String epage = pages[ 1 ].trim();
			epage = epage.replaceAll( "\\D", "" );
			record.createPart( EndPagePartStructure.getInstance().getId(),
					epage );
		}
	}

	private String getId() {
		return "asset" +
		Math.random() * 1000 +
		System.currentTimeMillis();
	}
}
