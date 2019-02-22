/******************************************************************************
**
** Advanced Distributed Learning Co-Laboratory (ADL Co-Lab) Hub grants you
** ("Licensee") a non-exclusive, royalty free, license to use, modify and
** redistribute this software in source and binary code form, provided that
** i) this copyright notice and license appear on all copies of the software;
** and ii) Licensee does not utilize the software in a manner which is
** disparaging to ADL Co-Lab Hub.
**
** This software is provided "AS IS," without a warranty of any kind.  ALL
** EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
** ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
** OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED.  ADL Co-Lab Hub AND ITS LICENSORS
** SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
** USING, MODIFYING OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES.  IN NO
** EVENT WILL ADL Co-Lab Hub OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE,
** PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
** INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE
** THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE
** SOFTWARE, EVEN IF ADL Co-Lab Hub HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGES.
**
******************************************************************************/
package org.adl.parsers.dom;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.adl.logging.DetailedLogMessageCollection;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;
import org.adl.util.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.dom.TextImpl;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * <strong>Filename: </strong>ADLDOMParser.java<br><br>
 *
 * <strong>Description:</strong><br>The <code>ADLDOMParser</code> object interfaces
 * with the open-source org.apache.xerces.dom.DomParser class to encapsulate
 * and provide parsing activities  - including well-formedness and validation
 * against the schema(s) checks. This object creates a DOM in memory if 
 * wellformedness is found. A DOM object is also created in memory after 
 * validation to the schema(s).
 * 
 * @author ADL Technical Team
 */
public class ADLDOMParser implements ErrorHandler {

	/**
	 * Performs the parsing activities, including the
	 * parse for wellformedness and the parse for validation to the controlling
	 * document.  This attribute represents an instance of the
	 * org.apache.xerces.dom.DomParser class.
	 */
	private DOMParser mParser;

	/**
	 * The <code>Document</code> object is an electronic representation of the
	 * XML produced if the parse was successful. A parse for wellformedness
	 * creates a document object while the parse for validation against the
	 * controlling documents creates a document object as well.  This attribute
	 * houses the document object that is created last.  In no document object is
	 * created, the value remains null. 
	 */
	private Document mDocument;

	/**
	 * Describes if the XML instance is found to be wellformed by
	 * the parser.  The value "false" indicates that the XML instance is not
	 * wellformed XML, "true" indicates it is wellformed XML.
	 */
	private boolean mIsXMLWellformed;

	/**
	 * Describes whether or not the XML instance that is being validated contains
	 * extension elements or attributes.  These are extensions that are not
	 * expected by SCORM.
	 */
	private boolean mExtensionsFound;

	/**
	 * This attribute describes if the XML instance is found to be valid against
	 * the controlling documents by the parser.  The value "false" indicates that
	 * the XML instance is not valid against the controlling documents, "true"
	 * indicates that the XML instance is valid against the controlling
	 * documents.
	 */
	private boolean mIsXMLValidToSchema;

	/**
	 * Describes if errors occured during the parse
	 * for validation against the controlling documents.  The value "true"
	 * indicates that no errors have been thrown during the validation to
	 * the controlling document parse, "false" indicates that errors
	 * have occured during validation to the controlling document parse.
	 */
	private boolean mValidFlag;

	/**
	 * Contains the string describing the location of the
	 * controlling documents that the parser shall parse against.  The format of
	 * this string value shall be identical to the representation of the
	 * schemaLocation attribute in the XML declation
	 * (ie, [namespace of schema] [location of schema] ).
	 */
	private String mSchemaLocation;

	/**
	 * Describes whether or not the xsi:schemaLocation attribute was encountered
	 * during the processing or not.  This state value is used when processing
	 * an XML instance that contains more than one xsi:schemaLocation attribute.
	 */
	private boolean mFirstTimeSchemaLocationFound;

	/**
	 * Indicates if the parser is in the state of validating for
	 * wellformedness or validation against the controlling documents. The value
	 * "true" indicates that the parser is validating against the schemas, 
	 * "false" indicates that it is parsing only for well-formedness.
	 */
	private boolean mStateIsValidating;

	/**
	 * Logger object used for debug logging.
	 */
	private static Log log = LogFactory.getLog(ADLDOMParser.class);

	/**
	 * Indicates if the xsi:schemaLocation was declared in the XML instance. The
	 * xsi:schemalocation attribute provides a hint to the XML Parser as to where
	 * the controlling documents are located.  The xsi:schemalocation attribute
	 * is an optional attribute.  
	 */
	private boolean mSchemaLocExists;

	/**
	 * Contains the list of namespaces that have been declared.  This list is
	 * used to track the required files necessary when setting the schema
	 * location to the default values.  
	 */
	private List<String> mDeclaredNamespaces;

	/**
	 * Default Constructor.  Sets the attributes to their initial values.
	 */
	public ADLDOMParser() {
		mParser = null;
		mDocument = null;
		mIsXMLWellformed = false;
		mIsXMLValidToSchema = false;
		mSchemaLocation = null;
		mStateIsValidating = false;
		mValidFlag = true;
		mExtensionsFound = false;
		mFirstTimeSchemaLocationFound = true;
		mSchemaLocExists = false;
		mDeclaredNamespaces = new ArrayList<>();
	}

	/**
	*  This method adds the input schema location value to the set of 
	*  schema locations to be used by the parser during a validating parse.
	* 
	* @param iSchemaLocation The schema location (namespace and location)
	* @param iXMLFileName - the XML that is being parsed for schemaLocation values 
	*/
	public void addSchemaLocationToList(String iSchemaLocation, String iXMLFileName) {
		log.debug("addSchemaLocationToList()");
		log.debug("Schema Location: " + iSchemaLocation);

		String fileName = searchFile(iXMLFileName, "xml");

		//Gets the current directory from the xml file path
		File tempXMLfile = new File(fileName);
		String xsdLocation = "file:///" + tempXMLfile.getParent() + "/";
		xsdLocation = xsdLocation.replaceAll(" ", "%20");
		xsdLocation = xsdLocation.replace('\\', '/');
		String schemaStr = "";

		StringTokenizer st = new StringTokenizer(iSchemaLocation, " ");

		// Builds the schema list
		while (st.hasMoreTokens()) {
			// get the namespace part of the schema location
			String namespace = st.nextToken();

			// check to see if the namespace is already in the set of schema
			// locations.  If so, no need to add the namespace or location
			// to the set of namespaces.
			// always need to account for LOM namespace due the same namespace 
			// is shared for different profiles
			if (!isSchemaLocationPresent(namespace) || namespace.equals("http://ltsc.ieee.org/xsd/LOM")) {
				schemaStr += namespace + " ";

				if (st.hasMoreTokens()) {
					String location = st.nextToken();

					if ((location.substring(0, 5).equals("http:")) || (location.substring(0, 6).equals("https:")) || (location.substring(0, 4).equals("ftp:"))
					        || (location.substring(0, 5).equals("ftps:"))) {
						// No need to append the XSD location (xsdLocation) to the
						// schema location
						schemaStr = schemaStr + location + " ";
					} else {
						// The schema location is not an external URL, append the 
						// local XSD location (xsdLocation) to the schema location 
						schemaStr = schemaStr + xsdLocation + location + " ";
					}
				}
			} // end if isSchemaLocationPresent
			else {
				// advance to the next token.  This will place the curser on the
				// location of the namespace we just processed.
				st.nextToken();
			}
		} // end looping over tokens

		if (mSchemaLocation == null) {
			mSchemaLocation = schemaStr;
		} else {
			mSchemaLocation += schemaStr;
		}
	}

	/**
	 * This method checks to see if the node that is being processed contains
	 * an xsi:schemaLocation attribute.  If it does, the method adds the name
	 * value of the attribute to the list of schemaLocations.
	 * 
	 * @param iNode The node that is being processed
	 * @param iXMLFileName The name of the XML test subject
	 */
	private void checkForSchemaLocations(Node iNode, String iXMLFileName) {
		log.debug("checkForSchemaLocations()");
		log.debug("Processing Node: [" + iNode.getLocalName() + "]");
		log.debug("Processing Nodes Namespace: [" + iNode.getNamespaceURI() + "]");

		// Get the list of attributes of the element
		NamedNodeMap attrList = iNode.getAttributes();

		// Loop over the attributes for this element, remove any attributes
		// that are extensions
		log.debug("Processing " + attrList.getLength() + " attributes");
		for (int i = 0; i < attrList.getLength(); i++) {
			Attr currentAttribute = (Attr) attrList.item(i);
			String parentNamespace = iNode.getNamespaceURI();
			String attributeNamespace = currentAttribute.getNamespaceURI();

			log.debug("Processing attribute [" + i + "]: [" + currentAttribute.getLocalName() + "]");
			log.debug("Attributes Namespace [" + i + "]: [" + attributeNamespace + "]");
			if (!mDeclaredNamespaces.contains(attributeNamespace) && (attributeNamespace != null)) {
				mDeclaredNamespaces.add(attributeNamespace);
			}

			log.debug("Attributes Parent Node [" + i + "]: [" + iNode.getLocalName() + "]");
			log.debug("Parent Nodes Namespace [" + i + "]: [" + parentNamespace + "]");

			if (!mDeclaredNamespaces.contains(attributeNamespace) && (parentNamespace != null)) {
				mDeclaredNamespaces.add(parentNamespace);
			}

			// If one of the attributes is xsi:schemaLocation, then
			// save off the namespace and the schema location.  These
			// will be used later during the validation process
			if (currentAttribute.getLocalName().equals("schemaLocation")
			        && currentAttribute.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema-instance")) {
				// A schema location has been defined in the XML, don't 
				// use the defaults
				if (mFirstTimeSchemaLocationFound) {
					// Don't use the default schema locations
					mSchemaLocation = null;
					mFirstTimeSchemaLocationFound = false;
					// flag that xsi:schemalocation attribute has been declared 
					mSchemaLocExists = true;
				}
				addSchemaLocationToList(currentAttribute.getValue(), iXMLFileName);
			}
		} // end looping over attributes

		log.debug("checkForSchemaLocations()");
	}

	/**
	 * Configures the parser by setting the appopropriate properties
	 * and features of the DOMParser object. Such properties and features
	 * include configuring a wellformedness parser or a validation parser,
	 * setting schema locations, turning on/off namespaces, etc.  This method
	 * must be called prior to any attempts to parse.   
	 */
	private void configureParser() {
		log.debug("configureParser()");

		// instantiate the Xerces DOMParser object to use the default
		// configuration

		//The default configuration is to allow xml 1.0 and 1.1 parsing
		mParser = new DOMParser();

		if (mParser != null) {
			// check to see if we are configuring a validating or non-validating
			//parser
			if (mStateIsValidating) // configuring a validating parser
			{
				if (mSchemaLocation != null) {
					try {
						mParser.setFeature("http://xml.org/sax/features/validation", true);
						mParser.setFeature("http://apache.org/xml/features/validation/schema", true);
						mParser.setFeature("http://xml.org/sax/features/namespaces", true);
						mParser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);

						log.debug("setting schemas to - " + mSchemaLocation);

						mParser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation", mSchemaLocation);

						mParser.setErrorHandler(this);
					} catch (SAXException se) {
						String msgText = Messages.getString("ADLDOMParser.30");
						log.error(msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.TERMINATE, msgText));
					}
				} else {
					String msgText = "Schema Location(s) not set";
					log.error("SEVERE: " + msgText);

					mValidFlag = false;
				}
			} else // configuring a non-validating parser
			{
				try {
					mParser.setFeature("http://xml.org/sax/features/validation", false);
					mParser.setFeature("http://xml.org/sax/features/namespaces", true);
					mParser.setFeature("http://apache.org/xml/features/continue-after-fatal-error", false);

					mParser.setErrorHandler(this);
				} catch (SAXException se) {
					String msgText = Messages.getString("ADLDOMParser.36");

					log.error("TERMINATE: " + msgText);
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.TERMINATE, msgText));
				}
			}
		} else {
			String msgText = Messages.getString("ADLDOMParser.38");
			log.error("TERMINATE: " + msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.TERMINATE, msgText));
		}
		log.debug("configureParser()");
	}

	/**
	 * Part of the org.xml.sax.ErrorHandler interface.  The parser
	 * calls this method when it wants to generate a error.  It is an interface
	 * that is implemented by the ADLDOMParser.
	 *
	 * @param iSPE    Exception object created by the parser.
	 */
	@Override
	public void error(SAXParseException iSPE) {
		log.debug("error()");

		// determine what state we are in to set the appropriate flags
		if (mStateIsValidating) {
			mValidFlag = false;
		} else {
			mIsXMLWellformed = false;
		}

		String msgText = iSPE.getMessage() + " line: " + iSPE.getLineNumber() + ", col: " + iSPE.getColumnNumber();

		log.debug("FAILED: " + msgText);

		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

		log.debug("error()");
	}

	/**
	 * Part of the org.xml.sax.ErrorHandler interface.  The parser
	 * calls this method when it wants to generate a fatal error.  It is an
	 * interface that is implemented by the ADLDOMParser.
	 *
	 * @param iSPE    Exception object created by the parser.
	 */
	@Override
	public void fatalError(SAXParseException iSPE) {
		log.debug("fatalError()");

		// determine what state we are in to set the appropriate flags
		if (mStateIsValidating) {
			mValidFlag = false;
		} else {
			mIsXMLWellformed = false;
		}

		String msgText = iSPE.getMessage() + " line: " + iSPE.getLineNumber() + ", col: " + iSPE.getColumnNumber();

		log.debug("FAILED: " + msgText);

		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

		log.debug("fatalError()");
	}

	/**
	 * This list contains the namespaces that have been declared in the root
	 * manifest.
	 * @return List containing the namespaces that have been declared
	 */
	public List<String> getDeclaredNamespaces() {
		return mDeclaredNamespaces;
	}

	/**
	 * Returns the document created during a parse. A parse for
	 * wellformedness creates a document object while the parse for validation
	 * against the controlling documents creates a seperate document object.
	 *
	 * @return Document An electronic representation of the XML produced by
	 * the parse.
	 */
	public Document getDocument() {
		return mDocument;
	}

	/**
	 * Returns whether or not the XML instance was valid to the
	 * schema.  The value "false" indicates that the XML instance is not valid
	 * against the controlling documents, "true" indicates that the XML instance
	 * is valid against the controlling documents.
	 *
	 * @return boolean describes if the instance was found to be valid
	 *                   against the schema(s).
	 */
	public boolean getIsValidToSchema() {
		return mIsXMLValidToSchema;
	}

	/**
	 * This method returns whether or not the XML instance was found to be
	 * wellformed.  The value "false" indicates that the XML instance is not
	 * wellformed XML, "true" indicates it is wellformed XML.
	 *
	 * @return boolean describes if the instance was found to be wellformed.
	 */
	public boolean getIsWellformed() {
		return mIsXMLWellformed;
	}

	/**
	 * Returns the schema location attribute.  This attribute represents the 
	 * namespace and location pair(s) found during the processing of an 
	 * XML instance.
	 * 
	 * @return String Returns the location of the schemas used in the parsing and 
	 * validation
	 */
	public String getSchemaLocation() {
		return mSchemaLocation;
	}

	/**
	 * This method returns whether or not the XML instance contains the optional
	 * xsi:schemalocation attribute declaration.  The xsi:schemalocation attribute
	 * provides a hint to the XML Parser as to where the controlling documents
	 * can be found.  
	 * 
	 * @return boolean describing if the xsi:schemalocation attribute was
	 * declared in the XML instance.  
	 */
	public boolean getSchemaLocExists() {
		return mSchemaLocExists;
	}

	/**
	 * Returns whether or not extensions attributes/elements were found during
	 * the processing of an XML instance.  These are extensions that are not
	 * expected by SCORM
	 * 
	 * @return boolean Returns true or false based on whether or not extensions were
	 * found in the XML instance
	 */
	public boolean isExtensionsFound() {
		return mExtensionsFound;
	}

	/**
	 * This method determines if the namespace has already been accounted
	 * for in the schema location listing.  If it has, then there is no reason
	 * to add it to the set of schema locations.  
	 * 
	 * @param iNamespace - The string we are looking searching for.
	 * 
	 * @return Returns whether or not the string has already been accounted
	 * for.
	 */
	private boolean isSchemaLocationPresent(String iNamespace) {
		boolean result = false;

		if (mSchemaLocation != null) {
			StringTokenizer st = new StringTokenizer(mSchemaLocation, " ");

			// Builds the schema list
			while (st.hasMoreTokens()) {
				// get the token
				String token = st.nextToken();

				if (token.equals(iNamespace)) {
					result = true;
					break;

				}
			}
		}

		return result;
	}

	/**
	 * Performs the validating parse - parsing for well-formed
	 * XML and validation to the controlling documents.
	 *
	 * @param iXMLFileName - the xml document to be parsed for validation
	 * against the controlling documents.
	 */
	public void parseForValidation(String iXMLFileName) {
		log.debug("parseForValidation()");
		log.debug("   iXMLFileName coming in is " + iXMLFileName);

		configureParser();

		Document wellformednessDocument = null;
		String msgText;

		String fileName = searchFile(iXMLFileName, "xml");

		if (!fileName.isEmpty()) {
			if (mParser != null) {
				try {
					msgText = "Parsing for Well-Formedness first to obtain document";
					log.debug(msgText);

					mParser.parse(setUpInputSource(fileName));
					wellformednessDocument = mParser.getDocument();

					if (wellformednessDocument != null) {
						mIsXMLWellformed = true;
					}
				} catch (SAXException se) {
					msgText = "SAXException thrown during non-validating parse";
					log.error(msgText);
				} catch (IOException ioe) {
					msgText = "IOException thrown during non-validating parse";
					log.error(msgText);

				} catch (NullPointerException npe) {
					msgText = "NullPointerException thrown during non-validating " + "parse";
					log.error(msgText);

				}
				if (mIsXMLWellformed) {
					//we have wellformed XML, now parsing for validity to xsd(s)
					mStateIsValidating = true;

					configureParser();

					if (mParser != null) {
						msgText = Messages.getString("ADLDOMParser.81");
						log.debug(msgText);
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

						if (mSchemaLocation == null) {
							/// schema locations have not been set
							mValidFlag = false;
						} else {
							try {
								mParser.parse(setUpInputSource(fileName));

							} catch (SAXException se) {
								msgText = "SAXException thrown during validating" + " parse";
								log.error(msgText);
							} catch (IOException ioe) {
								msgText = "IOException thrown during" + " validating parse";
								log.error(msgText);
							} catch (NullPointerException npe) {
								msgText = "NullPointerException thrown during " + "non-validating parse";
								log.error(msgText);

							}
						}
					} else {
						mValidFlag = false;
					}
				} else {
					//instance is not well-formed, therefore, validity not checked
					//ensure that the mValidFlag is set to false;
					msgText = "The XML is not wellformed, cannot continue with validation against the Controlling Documents";
					log.debug(msgText);

					mValidFlag = false;
				}
			} else {
				//parser is null, can not continue parse for validation
				mValidFlag = false;
			}
		} else {
			// error in file name
			mValidFlag = false;
		}

		// assign the apporopriate value to the mIsXMLValidToSchema attribute
		setValidXMLToSchemaAttribute();

		log.debug("valid = " + getIsValidToSchema());

		if (getIsValidToSchema()) {
			msgText = Messages.getString("ADLDOMParser.91");
			log.debug(msgText);
			DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
		}

		log.debug("parseForValidation()");
	}

	/**
	 * Performs the non-validating parse - parsing for well-formed
	 * XML only.
	 *
	 * @param iXMLFileName - the xml document to be parsed for wellformedness in
	 * the form of a URL 
	 *
	 * @param iLogRelated - boolean determining whether or not the parse<br>
	 * should log messages.  True implies log messages, false implies otherwise.
	 * Messages do not need to be logged when parsing to create a dom for
	 * system operation purposes only.
	 * 
	 * @param iControllingDocRelated - boolean describing if pruneTree needs
	 * to be called.  This will be set to true when a parseForWellformedness
	 * is called for a controlling document check in the Content Package test.  
	 */
	public void parseForWellformedness(java.net.URL iXMLFileName, boolean iLogRelated, boolean iControllingDocRelated) {
		log.debug("parseForWellformedness()");
		log.debug("   iXMLFileName coming in is " + iXMLFileName);
		log.debug("   iLogRelated coming in is " + iLogRelated);

		configureParser();

		Document wellformednessDocument = null;
		String msgText;

		if (mParser != null) {
			try {
				msgText = "Validating for Well-Formedness";
				log.debug(msgText);

				if (iLogRelated) {
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));
				}

				//gets an input source from the URL passed in.
				mParser.parse(new InputSource(iXMLFileName.openStream()));

				msgText = "The XML is Well-formed";
				log.debug(msgText);

				if (iLogRelated) {
					DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
				}
				wellformednessDocument = mParser.getDocument();

				if (wellformednessDocument != null) {
					mIsXMLWellformed = true;
				}
			} catch (SAXException se) {
				msgText = "SAXException thrown during non-validating parse";
				log.error(msgText);
			} catch (IOException ioe) {
				msgText = "IOException thrown during non-validating parse";
				log.error(msgText);
			}
		} else {
			// parser is null, can not continue wellformedness
			//mWellnessFlag = false;
		}

		// assign the appropriate value to the document attribute
		setDocumentAttribute(wellformednessDocument, iLogRelated, iXMLFileName.toString(), iControllingDocRelated);

		log.debug("wellformed = " + getIsWellformed());

		log.debug("parseForWellformedness()");
	}

	/**
	 * This method performs the non-validating parse - parsing for well-formed
	 * XML only.
	 *
	 * @param iXMLFileName - the xml document to be parsed for wellformedness in
	 * the form of a string
	 *
	 * @param iLogRelated - boolean determining whether or not the parse
	 * should log messages.  True implies log messages, false implies otherwise.
	 * Messages do not need to be logged when parsing to create a document for
	 * system operation purposes only.
	 * 
	 * @param iControllingDocRelated - boolean describing if pruneTree needs
	 * to be called.  This will be set to true when a parseForWellformedness
	 * is called for a controlling document check in the Content Package test.
	 *
	 */
	public void parseForWellformedness(String iXMLFileName, boolean iLogRelated, boolean iControllingDocRelated) {
		log.debug("parseForWellformedness()");
		log.debug("   iXMLFileName coming in is " + iXMLFileName);
		log.debug("   iLogRelated coming in is " + iLogRelated);

		configureParser();

		Document wellformednessDocument = null;
		String msgText;

		String fileName = searchFile(iXMLFileName, "xml");

		//Gets the current directory from the xml file path
		File tempXMLfile = new File(fileName);
		String xsdLocation = "file:///" + tempXMLfile.getParent() + "/";
		xsdLocation = xsdLocation.replaceAll(" ", "%20");
		xsdLocation = xsdLocation.replace('\\', '/');

		if (!fileName.isEmpty()) {
			if (mParser != null) {
				try {
					msgText = Messages.getString("ADLDOMParser.52");
					log.debug(msgText);

					if (iLogRelated) {
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));
					}

					mParser.parse(setUpInputSource(fileName));
					msgText = Messages.getString("ADLDOMParser.53");
					log.debug(msgText);

					if (iLogRelated) {
						DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
					}
					wellformednessDocument = mParser.getDocument();

					if (wellformednessDocument != null) {
						mIsXMLWellformed = true;
					}
				} catch (SAXException se) {
					msgText = "SAXException thrown during non-validating parse";
					log.error(msgText);
				} catch (IOException ioe) {
					msgText = "IOException thrown during non-validating parse";
					log.error(msgText);

				}
			}
		}
		// assign the appropriate value to the document attribute
		setDocumentAttribute(wellformednessDocument, iLogRelated, iXMLFileName, iControllingDocRelated);

		log.debug("FINAL SCHEMA LOCATION:  " + mSchemaLocation);
		log.debug("wellformed = " + getIsWellformed());
		log.debug("parseForWellformedness()");
	}

	/**
	    * Prints all needed information for a given DOM node,
	    * including the node type, node name, and node value.
	    *
	    * @param iNodeTypeString type of node to print
	    *
	    * @param iNode   DOM node to print
	    */
	protected void printNodeInfo(String iNodeTypeString, Node iNode) {
		StringBuffer typeStr = new StringBuffer("(null)");
		StringBuffer nodeType = new StringBuffer("(null)");
		StringBuffer nodeName = new StringBuffer("(null)");
		StringBuffer nodeValue = new StringBuffer("(null)");
		String sp = " -- ";

		if (iNode != null) {
			if (iNodeTypeString != null) {
				typeStr = new StringBuffer(iNodeTypeString);
			}

			nodeType = new StringBuffer(Short.toString(iNode.getNodeType()));

			if (iNode.getNodeName() != null) {
				nodeName = new StringBuffer(iNode.getNodeName());
			}

			if (iNode.getNodeValue() != null) {
				nodeValue = new StringBuffer(iNode.getNodeValue());
			}
		}
		while (typeStr.length() < 42) {
			typeStr.append(" ");
		}
		while (nodeName.length() < 15) {
			nodeName.append(" ");
		}
		while (nodeValue.length() < 10) {
			nodeValue.append(" ");
		}

		log.debug(typeStr + sp + nodeType + sp + nodeName + sp + nodeValue);

	}

	/**
	 * Traverses the DOM Tree and removes Ignorable Whitespace Text Nodes and
	 * Comment Text.  The function also removes extension elements and
	 * attributes that are not defined by SCORM.  If extensions are found
	 * the function also sets a flag stating that extensions are present in the
	 * input XML instance.
	 *
	 * @param iNode The node to be pruned of whitespace and comments<br>
	 * @param iXMLFileName The XML file to be pruned
	 */
	private void pruneTree(Node iNode, String iXMLFileName) {
		String value;

		// is there anything to do?
		if (iNode == null){
			return;
		}

		switch (iNode.getNodeType()) {
		case Node.PROCESSING_INSTRUCTION_NODE: {
			break;
		}
		case Node.DOCUMENT_NODE: {
			pruneTree(((Document) iNode).getDocumentElement(), iXMLFileName);
			break;
		}
		case Node.ELEMENT_NODE: {
			log.debug("Processing Element Node: [" + iNode.getLocalName() + "]");
			log.debug("************************************************");
			log.debug("Processing Element Node: [" + iNode.getLocalName() + "]");

			checkForSchemaLocations(iNode, iXMLFileName);

			// Get the list of attributes of the element
			NamedNodeMap attrList = iNode.getAttributes();

			// Loop over the attributes for this element, remove any attributes
			// that are extensions
			log.debug("Processing " + attrList.getLength() + " attributes");
			for (int i = 0; i < attrList.getLength(); i++) {
				Attr currentAttribute = (Attr) attrList.item(i);

				if (!(DOMTreeUtility.isSCORMAppProfileNode(currentAttribute, iNode))) {
					log.debug("Extension attribute, removing: [" + currentAttribute.getNamespaceURI() + "] " + currentAttribute.getLocalName()
					        + " from the its parent node [" + iNode.getNamespaceURI() + "] " + iNode.getLocalName());

					// Remove the Element Node from the DOM
					attrList.removeNamedItemNS(currentAttribute.getNamespaceURI(), currentAttribute.getLocalName());
					i--;
					mExtensionsFound = true;
				} else {
					log.debug("Valid SCORM attribute, keeping attribute: [" + currentAttribute.getNamespaceURI() + "] " + currentAttribute.getLocalName());
				}
			}

			log.debug("Done processing attributes for node: [" + iNode.getNamespaceURI() + "] " + iNode.getLocalName());
			log.debug("************************************************");

			// Done looping over the attributes for this element, now loop over
			// the set of children nodes.

			log.debug("");
			log.debug("************************************************");
			log.debug("Processing direct-descendances for node: [" + iNode.getNamespaceURI() + "] " + iNode.getLocalName());

			NodeList children = iNode.getChildNodes();
			if (children != null) {
				// Loop over set of children elements for this element, remove
				// any elements that are extensions
				log.debug("Processing " + children.getLength() + " elements");
				for (int z = 0; z < children.getLength(); z++) {
					Node childNode = children.item(z);

					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						log.debug("Processing element: [" + childNode + "]");
						log.debug("Elements Namespace: [" + childNode.getNamespaceURI() + "]");
						log.debug("Elements Parent Node: [" + iNode.getLocalName() + "]");
						log.debug("Parent Nodes Namespace: [" + iNode.getNamespaceURI() + "]");

						if (!(DOMTreeUtility.isSCORMAppProfileNode(children.item(z), children.item(z).getParentNode()))) {
							// Before we remove the element see if the elemen
							// contains any xsi:schemaLocations.  We need
							// to add them to the list of schema locations for 
							// parsing
							checkForSchemaLocations(childNode, iXMLFileName);

							log.debug("Extension Element Found, removing from DOM Tree ");

							// Remove the Element Node from the DOM
							children.item(z).getParentNode().removeChild(children.item(z));
							z--;
							mExtensionsFound = true;
						} else {
							log.debug("ADL SCORM Element Found, leaving " + "element in DOM Tree");
							pruneTree(children.item(z), iXMLFileName);
						}
					} // end if NodeType == ELEMENT_NODE

					if (childNode instanceof TextImpl) {
						value = children.item(z).getNodeValue().trim();

						if (((TextImpl) children.item(z)).isIgnorableWhitespace()) {
							iNode.removeChild(children.item(z));
							z--;
						} else if (value.length() == 0) {
							iNode.removeChild(children.item(z));
							z--;
						}
					} else if (children.item(z).getNodeType() == Node.COMMENT_NODE) {
						iNode.removeChild(children.item(z));
						z--;
					}
				} // end looping over children nodes
			} // end if there are children

			log.debug("Done processing direct-descendants for node: [" + iNode.getNamespaceURI() + "] " + iNode.getLocalName());
			log.debug("**************************************************");

			break;

		}

		// handle entity reference nodes
		case Node.ENTITY_REFERENCE_NODE: {

			NodeList children = iNode.getChildNodes();
			if (children != null) {
				int len = children.getLength();
				for (int i = 0; i < len; i++) {
					pruneTree(children.item(i), iXMLFileName);
				}
			}
			break;
		}

		// text
		case Node.COMMENT_NODE: {
			break;
		}
		case Node.CDATA_SECTION_NODE: {
			break;
		}
		case Node.TEXT_NODE: {
			break;
		}
		default: {
			break;
		}
		}
	}

	/**
	    * Searches for the full path of the specified file and returns
	    * it's string name.
	    *
	    * @param iFileName Name of the file to be checked.
	    * @param iFileType File extension of this file. (java, xml).
	    *
	    * @return The full absolute path and filename of the given file.
	    */
	private String searchFile(String iFileName, String iFileType) {
		log.debug("searchFile()");
		log.debug("   iFileName coming in is: " + iFileName);
		log.debug("   iFileType coming in is: " + iFileType);

		String outFileName = "";

		//check to make sure the file exists then assign the full path and name to
		//outFileName to be returned
		try {
			if (iFileName.length() > 6 && (iFileName.substring(0, 5).equals("http:") || iFileName.substring(0, 6).equals("https:"))) {
				outFileName = iFileName;
			} else {
				File inFile = new File(iFileName);

				if (inFile.isFile() == true) {
					outFileName = inFile.getAbsolutePath();
				} else {
					String msgText = "File Not A Normal File";
					log.error(msgText);
				}
			}
		} catch (NullPointerException npe) {
			String msgText = "File is Empty";
			log.error(msgText);
		} catch (SecurityException se) {
			String msgText = "File Not Accessible";
			log.error(msgText);
		}
		log.debug("searchFile()");

		return outFileName;
	}

	/**
	* A seperate document object is created for a wellformedness parse and a
	* parse for validation against the controlling documents.  The validity
	* document contains inserts of the the default element/attribute
	* values as defined in the XSDs.  The wellformedness document contains the
	* elements/attributes as they appear in the XML instance.  For this reason,
	* the wellformedness document is set, if the XML was found to be 
	* wellformed, and used during the Application Profiles checks.
	*
	* @param ioWellformednessDoc DOM created after wellformedness parse.
	* @param iLogRelated boolean describing if the method should log messages
	* @param iXMLFileName name of the xml test subject
	* @param iControllingDocRelated boolean descibing if the xml is to be 
	*        validated against the controlling docs
	*/
	private void setDocumentAttribute(Document ioWellformednessDoc, boolean iLogRelated, String iXMLFileName, boolean iControllingDocRelated) {
		log.debug("setDocumentAttribute()");

		if (mIsXMLWellformed) {
			if (iLogRelated || iControllingDocRelated) {
				pruneTree(ioWellformednessDoc, iXMLFileName);
			}

			mDocument = ioWellformednessDoc;
		}

		//else mDocument shall remain = null
		log.debug("setDocumentAttribute()");
	}

	/**
	 * The DOMParser allows using a system to hardcode the location of the
	 * controlling documents that are to be used during the parse for validation.
	 * This method permits the setting of these controlling document locations.
	 * <br>
	 *
	 * @param iSchemaLocation
	 *               The schemaLocation string in the exact format as it
	 *               would appear in the xsi:schemaLocation attribute of an XML
	 *               instance.
	 */
	public void setSchemaLocation(String iSchemaLocation) {
		mSchemaLocation = iSchemaLocation;
	}

	/**
	 * Sets up the file source for the test subject file.
	 *
	 * @param iFileName file to setup input source for.
	 *
	 * @return InputSource
	 */
	private InputSource setupFileSource(String iFileName) {
		log.debug("setupFileSource()");
		String msgText;
		boolean defaultEncoding = true;
		String encoding = null;
		PushbackInputStream inputStream;
		FileInputStream inFile;

		try {
			File xmlFile = new File(iFileName);
			log.debug(xmlFile.getAbsolutePath());

			if (xmlFile.isFile()) {
				InputSource is = null;

				defaultEncoding = true;
				if (xmlFile.length() > 1) {
					inFile = new FileInputStream(xmlFile);
					inputStream = new PushbackInputStream(inFile, 4);

					// Reads the initial 4 bytes of the file to check for a Byte
					// Order Mark and determine the encoding

					byte bom[] = new byte[4];
					int n, pushBack;
					n = inputStream.read(bom, 0, bom.length);

					// UTF-8 Encoded
					if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
						encoding = "UTF-8";
						defaultEncoding = false;
						pushBack = n - 3;
					}
					// UTF-16 Big Endian Encoded
					else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
						encoding = "UTF-16BE";
						defaultEncoding = false;
						pushBack = n - 2;
					}
					// UTF-16 Little Endian Encoded               
					else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
						encoding = "UTF-16LE";
						defaultEncoding = false;
						pushBack = n - 2;
					}
					// Default encoding
					else {
						// Unicode BOM mark not found, unread all bytes                  
						pushBack = n;
					}

					// Place any non-BOM bytes back into the stream
					if (pushBack > 0) {
						inputStream.unread(bom, (n - pushBack), pushBack);
					}

					if (defaultEncoding == true) { //Reads in ASCII file.
						FileReader fr = new FileReader(xmlFile);
						is = new InputSource(fr);
					}
					// Reads the file in the determined encoding
					else {
						StringBuffer dataString;
						try ( //Creates a buffer with the size of the xml encoded file
								BufferedReader inStream = new BufferedReader(new InputStreamReader(inputStream, encoding)))
						{
							dataString = new StringBuffer();
							String s = "";
							//Builds the encoded file to be parsed
							while ((s = inStream.readLine()) != null) {
								dataString.append(s);
							}
						}
						inputStream.close();
						inFile.close();
						is = new InputSource(new StringReader(dataString.toString()));
						is.setEncoding(encoding);
					}
				}
				return is;
			} else if ((iFileName.length() > 6) && (iFileName.substring(0, 5).equals("http:") || iFileName.substring(0, 6).equals("https:"))) {
				URL xmlURL = new URL(iFileName);
				InputStream xmlIS = xmlURL.openStream();
				InputSource is = new InputSource(xmlIS);
				return is;
			} else {
				msgText = "XML File: " + iFileName + " is not a file or URL";
				log.error(msgText);
			}
		} catch (NullPointerException npe) {
			msgText = "Null pointer exception" + npe;
			log.error(msgText);
		} catch (SecurityException se) {
			msgText = "Security Exception" + se;
			log.error(msgText);
		} catch (FileNotFoundException fnfe) {
			msgText = "File Not Found Exception" + fnfe;
			log.error(msgText);
		} catch (Exception e) {
			msgText = "General Exception" + e;
			log.error(msgText);
		}

		log.debug("setUpFileSource()");

		return new InputSource();
	}

	/**
	 * This method sets up the input source for the test subject file.
	 *
	 * @param iFileName The name of the file we are are setting up the input 
	 * source for.
	 *
	 * @return Returns an input source needed for parsing
	 */
	private InputSource setUpInputSource(String iFileName) {
		log.debug("setUpInputSource()");
		InputSource is = new InputSource();
		is = setupFileSource(iFileName);

		log.debug("setUpInputSource()");
		return is;
	}

	/**
	 * Sets the mIsXMLValidToSchema attribute to it's final value.
	 * This is necessary to work around the problem with Xerces now throwing
	 * an exception when the schema location can not be determined.
	 */
	private void setValidXMLToSchemaAttribute() {
		log.debug("setValidXMLSchemaAttribute()");

		mIsXMLValidToSchema = mValidFlag;

		log.debug("setValidXMLSchemaAttribute()");
	}

	/**
	 * This method is part of the org.xml.sax.ErrorHandler interface.  The parser
	 * calls this method when it wants to generate a warning.  It is an interface
	 * that is implemented by the ADLDOMParser.
	 *
	 * @param iSPE SAX Parse Exception object created by the parser.
	 */
	@Override
	public void warning(SAXParseException iSPE) {
		log.debug("warning()");

		// determine what state we are in to set the appropriate flags
		if (mStateIsValidating) {
			mValidFlag = false;
		} else {
			mIsXMLWellformed = false;
		}

		String msgText = iSPE.getMessage() + " line: " + iSPE.getLineNumber() + ", col: " + iSPE.getColumnNumber();

		log.debug("FAILED: " + msgText);

		DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

		log.debug("warning(SAXParseException)");
	}
}