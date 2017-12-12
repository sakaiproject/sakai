package edu.indiana.lib.twinpeaks.search.sru;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

@Slf4j
public class CqlParser extends org.xml.sax.helpers.DefaultHandler
{
  //
	// Index mappings (CQL -> 360 Search) (this is pretty much CQL to CQL)
	//
	private static final java.util.Map INDEX_MAP = new java.util.HashMap();
	static
	{
		INDEX_MAP.put("keyword",  " ");
		INDEX_MAP.put("title",    "cs.title=");
		INDEX_MAP.put("author",   "cs.author=");
		INDEX_MAP.put("subject",  "cs.subject=");
		INDEX_MAP.put("year",     "cs.date=");
	}
  //
	// Boolean mappings (CQL -> 360 Search)
	//
	private static final java.util.Map BOOL_RELATION_MAP = new java.util.HashMap();
	static
	{
		BOOL_RELATION_MAP.put("and", " and ");
		BOOL_RELATION_MAP.put("or",  " or ");
		BOOL_RELATION_MAP.put("not", " not ");
	}
  //
	// SAX Parsing
	//
	SAXParser       saxParser;
	StringBuilder   textBuffer;
	StringBuilder   searchClause;
	boolean         inSearchClause;
	java.util.Stack cqlStack;
  //
  // Treat all non-keyword fields as phrases?
  //
	final static boolean TREAT_ALL_FIELDS_AS_PHRASE = true;
	//
	// Are we currently parsing a keyword field?
	//
	boolean inKeyword;

  /**
   * Constructor
   */
	public CqlParser()
	{
		// initialize stack
		cqlStack = new java.util.Stack();

		// initialize SAX Parser
		SAXParserFactory factory;

		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		try
		{
			saxParser = factory.newSAXParser();
		}
		catch (org.xml.sax.SAXException e)
		{
			log.error("SAX exception: " + e);
		}
		catch (ParserConfigurationException e)
		{
			log.error("Parse failed: " + e);
		}
	}

	/**
	 * Converts a CQL-formatted search query into a format that 360 Search
	 * can understand.  Uses org.z3950.zing.cql.CQLNode.toXCQL() and SAX Parsing
	 * to convert the cqlSearchQuery into an almost identical query parameter.
	 *
	 * @param cqlSearchQuery CQL-formatted search query.
	 * @return A 360 Search query
	 * @see org.z3950.zing.cql.CQLNode.toXCQL()
	 */
	public String doCQL2MetasearchCommand( String cqlSearchQuery )
	{
		if (cqlSearchQuery == null || cqlSearchQuery.equals( "" ))
		{
			return null;
		}

		org.z3950.zing.cql.CQLParser parser = new org.z3950.zing.cql.CQLParser();
		org.z3950.zing.cql.CQLNode root = null;

    // Parse the user criteria
		try
		{
			root = parser.parse(cqlSearchQuery);
		}
		catch (Exception exception)
		{
			log.error("Exception in CQL parse: " + exception);
		}

		if (root == null)
		{
			return null;
		}

		String cqlXml = root.toXCQL( 0 );

		log.debug("CQL XML:");
		log.debug(cqlXml);

		// Open the CQL XML string as a stream
		java.io.ByteArrayInputStream byteInputStream = null;
		try
		{
			byteInputStream = new java.io.ByteArrayInputStream(cqlXml.getBytes("UTF8"));
		}
		catch (Exception exception)
		{
			log.error("Exception opening input stream: " + exception);
		}

		if (byteInputStream == null)
		{
			return null;
		}

		// clear the stack
		cqlStack.removeAllElements();

		// Parse
		try
		{
			saxParser.parse(byteInputStream, this);
			byteInputStream.close();
		}
		catch (Exception exception)
		{
			log.error("Exception in parse: " + exception);
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
	public void startElement(String namespaceURI,
	                         String sName,
	                         String qName,
	                         Attributes attrs) throws SAXException
	{
		// set flags to avoid overwriting duplicate tag data
		if (qName.equals( "searchClause" ))
		{
			inSearchClause = true;
			inKeyword = false;
		}
	}

	/**
	 * Receive notification of the end of an element.
	 *
	 * @see org.xml.sax.helpers.DefaultHandler
	 */
	public void endElement(String namespaceURI, String sName, String qName)
	                       throws SAXException
	{
		// extract data
		extractDataFromText(qName);

		// clear flags
		if (qName.equals("searchClause"))
		{
			inSearchClause = false;
			inKeyword = false;
		}
	}

	/**
	 * Receive notification of character data inside an element.
	 *
	 * @see org.xml.sax.helpers.DefaultHandler
	 */
	public void characters(char[] buf, int offset, int len)
	                       throws SAXException
	{
		// store character data
		String text = new String(buf, offset, len);

		if (textBuffer == null)
		{
			textBuffer = new StringBuilder(text);
		} else {
			textBuffer.append(text);
		}
	}


	//-------------------------
	// PRIVATE HELPER METHODS -
	//-------------------------

	private void extractDataFromText(String element)
	{
		if (textBuffer == null)
		{
			return;
		}

		String text = textBuffer.toString().trim();

		if (text.equals("") && !element.equals("triple"))
		{
			return;
		}
    //
		// check for a boolean relation value
		//
		if (!inSearchClause && element.equals( "value"))
		{
			cqlStack.push(text);
		}
    //
		// Construct a search clause
		//
		if (inSearchClause)
		{
			if (searchClause == null)
			{
				searchClause = new StringBuilder();
			}
      //
      // General syntax: title=macbeth
      //
      //    (title is the index, = is the value, macbeth is the term)
      //
			if (element.equals("index"))
			{
			  String field = translateIndex(text);

			  inKeyword = ((String) INDEX_MAP.get("keyword")).equals(field);
				searchClause.append(field);
			}
			else if (element.equals("value"))
			{
			  //
				// The relation value is always supplied as '='.
				//
				// We don't need it - do nothing.
				//
			}
			else if (element.equals("term"))
			{
			  //
			  // Search term processing:
			  //
				// * Honor '+' encoding for embedded spaces
				// * Add double quotes
			  //
				// Example:
				//      aa+bb+cc  <becomes>  "aa bb cc"
				//
				if (TREAT_ALL_FIELDS_AS_PHRASE || inKeyword)
				{
				  searchClause.append('"');
				}
			  searchClause.append(text.replaceAll("\\+", " ").trim());

				if (TREAT_ALL_FIELDS_AS_PHRASE || inKeyword)
				{
				  searchClause.append('"');
        }
				cqlStack.push(searchClause.toString().trim());
				searchClause = null;
			}
		}
    //
		// evaluate expression so far if we hit a </triple>
		//
		if( element.equals( "triple" ) )
		{
			String rightOperand    = (String) cqlStack.pop();
			String leftOperand     = (String) cqlStack.pop();
			String booleanRelation = (String) cqlStack.pop();

			cqlStack.push(leftOperand.replaceAll("\\+", " ").trim()
                +   translateBooleanRelation(booleanRelation)
					      +   rightOperand.replaceAll("\\+", " ").trim());
		}

		textBuffer = null;
	}

  /**
   * Translate a CQL index to the appropriate 360 Search field name
   * @param cqlIndex CQL index name
   * @return 360 Search field name
   */
	private String translateIndex(String cqlIndex)
	{
		String s360Index = ( String ) INDEX_MAP.get(cqlIndex);

		if (s360Index == null || s360Index.equals( "" ))
		{
			log.error("translateIndex(): bad index, using KEYWORD");
			s360Index = (String) INDEX_MAP.get("keyword");
		}

		return s360Index;
	}

  /**
   * Translate a CQL boolean term to the appropriate 360 Search syntax
   * @param booleanRelation CQL boolean term
   * @return A boolean operation
   */
	private String translateBooleanRelation(String booleanRelation)
	{
		String s360Boolean = (String) BOOL_RELATION_MAP.get(booleanRelation);

		if (s360Boolean == null || s360Boolean.equals( "" ))
		{
			log.error("translateBooleanRelation(): bad boolean relation, using AND" );
			s360Boolean = (String) BOOL_RELATION_MAP.get("and");
		}
		return s360Boolean;
	}

  /**
   * Main(): test
   */
  public static void main(String[] args)
  {
    CqlParser parser = new CqlParser();
    String query;

    query = "title=\"My Title\"";
    log.debug("CQL: {}", query);
    log.debug("360: {}", parser.doCQL2MetasearchCommand(query));

    query = "title=\"\\\"My Title\\\"\"";
    log.debug("CQL: {}", query);
    log.debug("360: {}", parser.doCQL2MetasearchCommand(query));

    query = "title=\"My Title\" and keyword=\"some keywords\"";
    log.debug("CQL: {}", query);
    log.debug("360: {}", parser.doCQL2MetasearchCommand(query));

    query = "title=\"My Title\" and keyword=\"some keywords\" and year=\"1999\"";
    log.debug("CQL: {}", query);
    log.debug("360: {}", parser.doCQL2MetasearchCommand(query));
  }
}

