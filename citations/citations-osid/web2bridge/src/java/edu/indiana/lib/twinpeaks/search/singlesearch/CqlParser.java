package edu.indiana.lib.twinpeaks.search.singlesearch;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import edu.indiana.lib.twinpeaks.util.*;


public class CqlParser extends org.xml.sax.helpers.DefaultHandler {

private static org.apache.commons.logging.Log	_log = LogUtils.getLog(CqlParser.class);

	// index mappings (CQL -> Sirsi)
	private static final java.util.Map INDEX_MAP = new java.util.HashMap();
	static {
		INDEX_MAP.put( "keyword", " " );
		INDEX_MAP.put( "title",   ":TITLE" );
		INDEX_MAP.put( "author",  ":CREATOR" );
		INDEX_MAP.put( "subject", ":SUBJECT" );
		INDEX_MAP.put( "year",    ":DATE" );
	}

	// boolean mappings (CQL -> Sirsi)
	private static final java.util.Map BOOL_RELATION_MAP = new java.util.HashMap();
	static {
		BOOL_RELATION_MAP.put( "and", " AND " );
		BOOL_RELATION_MAP.put( "or",  " OR " );
	}

	// for SAX Parsing
	SAXParser saxParser;
	StringBuffer textBuffer;
	StringBuffer searchClause;
	boolean inSearchClause;
	java.util.Stack cqlStack;

	public CqlParser()
	{
		// initialize stack
		cqlStack = new java.util.Stack();

		// initialize SAX Parser
		SAXParserFactory factory;

		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware( true );
		try {
			saxParser = factory.newSAXParser();
		} catch( org.xml.sax.SAXException e ) {
			e.printStackTrace();
		} catch( ParserConfigurationException e ) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts a CQL-formatted search query into a format that the X-Server
	 * can understand.  Uses org.z3950.zing.cql.CQLNode.toXCQL() and SAX Parsing
	 * to convert the cqlSearchQuery into an X-Server find_command.
	 *
	 * @param cqlSearchQuery CQL-formatted search query.
	 * @return X-Server find_command or null if cqlSearchQuery is null or empty.
	 * @see org.z3950.zing.cql.CQLNode.toXCQL()
	 */
	public String doCQL2MetasearchCommand( String cqlSearchQuery ) {

		if( cqlSearchQuery == null || cqlSearchQuery.equals( "" ) ) {
			return null;
		}

		org.z3950.zing.cql.CQLParser parser = new org.z3950.zing.cql.CQLParser();
		org.z3950.zing.cql.CQLNode root = null;

		try {
			// parse the criteria
			root = parser.parse( cqlSearchQuery );
		} catch( java.io.IOException ioe ) {
			ioe.printStackTrace();
		} catch( org.z3950.zing.cql.CQLParseException e ) {
			e.printStackTrace();
		}
		String cqlXml = root.toXCQL( 0 );

		_log.debug("CQL XML:");
		_log.debug(cqlXml);

		// get cqlXml as a stream
		java.io.ByteArrayInputStream byteInputStream = null;
		try {
			byteInputStream = new java.io.ByteArrayInputStream(
					cqlXml.getBytes( "UTF8" ) );
		} catch( java.io.UnsupportedEncodingException uee ) {
			uee.printStackTrace();
		}

		// clear the stack
		cqlStack.removeAllElements();

		// run the parser
		try {
			saxParser.parse( byteInputStream, this );
			byteInputStream.close();
		} catch( java.io.IOException ioe ) {
			ioe.printStackTrace();
		} catch( org.xml.sax.SAXException spe ) {
			spe.printStackTrace();
		}

		String cqlResult = ( String ) cqlStack.pop();
		return cqlResult.trim();
	}

	//----------------------------------
	// DEFAULT HANDLER IMPLEMENTATIONS -
	//----------------------------------

	/**
	 * Receive notification of the beginning of an element.
	 *
	 * @see org.xml.sax.helpers.DefaultHandler
	 */
	public void startElement( String namespaceURI, String sName,
			String qName, Attributes attrs ) throws SAXException {
		// set flags to avoid overwriting duplicate tag data
		if( qName.equals( "searchClause" ) ) {
			inSearchClause = true;
		}
	}

	/**
	 * Receive notification of the end of an element.
	 *
	 * @see org.xml.sax.helpers.DefaultHandler
	 */
	public void endElement( String namespaceURI, String sName, String qName )
	throws SAXException {
		// extract data
		extractDataFromText( qName );

		// clear flags
		if( qName.equals( "searchClause" ) ) {
			inSearchClause = false;
		}
	}

	/**
	 * Receive notification of character data inside an element.
	 *
	 * @see org.xml.sax.helpers.DefaultHandler
	 */
	public void characters( char[] buf, int offset, int len )
	throws SAXException {
		// store character data
		String text = new String( buf, offset, len );

		if( textBuffer == null ) {
			textBuffer = new StringBuffer( text );
		} else {
			textBuffer.append( text );
		}
	}


	//-------------------------
	// PRIVATE HELPER METHODS -
	//-------------------------

	private void extractDataFromText( String element ) {
		if( textBuffer == null ) {
			return;
		}

		String text = textBuffer.toString().trim();
		if( text.equals( "" ) && !element.equals( "triple" ) ) {
			return;
		}
    //
		// check for a boolean relation value
		//
		if( !inSearchClause && element.equals( "value" ) ) {
			cqlStack.push( text );
		}
    //
		// Construct a search clause
		//
		if( inSearchClause ) {
			if( searchClause == null ) {
				searchClause = new StringBuffer();
			}
      //
      // General syntax: title=macbeth
      //
      //    (title is the index, = is the value, macbeth is the term)
      //
			if( element.equals( "index" ) ) {
				searchClause.append( translateIndex( text ) );

			} else if( element.equals( "value" ) ) {
			  //
				// The relation value is always supplied as '='.
				// We don't need it.  Just use a space for Web2
				//
				searchClause.append( ' ' );

			} else if( element.equals( "term" ) ) {
				// Discard '+' encoding for embedded spaces (should we url decode?).
				//
				// Unless our caller provides enclosing quotes (\"), this will
				// produce a series of keywords, not a phrase.
				//
			  searchClause.append( text.replaceAll("\\+", " ").trim() );
				cqlStack.push( searchClause.toString().trim() );
				searchClause = null;
			}
		}
    //
		// evaluate expression so far if we hit a </triple>
		//
		if( element.equals( "triple" ) ) {
			String rightOperand    = ( String ) cqlStack.pop();
			String leftOperand     = ( String ) cqlStack.pop();
			String booleanRelation = ( String ) cqlStack.pop();

			cqlStack.push( leftOperand +
					translateBooleanRelation( booleanRelation ) +
					rightOperand );
		}

		textBuffer = null;
	}

	private String translateIndex( String cqlIndex ) {
		String xserverIndex = ( String ) INDEX_MAP.get( cqlIndex );

		if( xserverIndex == null || xserverIndex.equals( "" ) ) {
			_log.error( "\nERROR (CQL2XServerFindCommand." +
					"translateIndex()): bad index" );
			// default to keyword
			xserverIndex = " ";
		}

		return xserverIndex;
	}

	private String translateBooleanRelation( String booleanRelation ) {
		String xserverBoolean = ( String ) BOOL_RELATION_MAP.get( booleanRelation );

		if( xserverBoolean == null || xserverBoolean.equals( "" ) ) {
			_log.error( "\nERROR (CQL2XServerFindCommand." +
					"translateBooleanRelation()): bad boolean relation" );
			// default to and
			xserverBoolean = " AND ";
		}

		return xserverBoolean;
	}

  public static void main(String[] args)
  {
    CqlParser parser = new CqlParser();
    String query;

    query = "title=\"My Title\"";
    System.out.println();
    System.out.println("CQL:    " + query);
    System.out.println("Sirsi:  " + parser.doCQL2MetasearchCommand(query));

    query = "title=\"\\\"My Title\\\"\"";
    System.out.println();
    System.out.println("CQL:    " + query);
    System.out.println("Sirsi:  " + parser.doCQL2MetasearchCommand(query));

    query = "title=\"My Title\" and keyword=\"some keywords\"";
    System.out.println();
    System.out.println("CQL:    " + query);
    System.out.println("Sirsi:  " + parser.doCQL2MetasearchCommand(query));

    query = "title=\"My Title\" and keyword=\"some keywords\" and year=\"1999\"";
    System.out.println();
    System.out.println("CQL:    " + query);
    System.out.println("Sirsi:  " + parser.doCQL2MetasearchCommand(query));
  }
}

