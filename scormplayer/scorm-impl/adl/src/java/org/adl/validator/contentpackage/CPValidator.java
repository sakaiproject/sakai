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
 
package org.adl.validator.contentpackage;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.util.Vector;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;

import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLDecoder;

import org.w3c.dom.Document;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.adl.validator.IValidator;
import org.adl.validator.ADLSCORMValidator;
import org.adl.validator.RulesValidator;
import org.adl.validator.sequence.SequenceValidator;

import org.adl.parsers.dom.ADLDOMParser;

import org.adl.util.zip.UnZipHandler;

import org.adl.parsers.dom.DOMTreeUtility;

import org.adl.logging.DetailedLogMessageCollection;

import org.adl.util.LogMessage;
import org.adl.util.MessageType;

import org.adl.util.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * <strong>Filename: </strong>CPValidator.java <br>
 * <strong>Description: </strong>The <code>CPValidator</code> determines
 * whether the content package test subject is conformant with the Content
 * Package Application Profiles( resource, content aggregation) as defined in
 * the Content Aggregation Model of the SCORM. The Content Package Validator
 * performs the following checks: 1) Determines if the required files exist at
 * the root of the package, 2) Parses the manifest for wellformedness, 3) Parses
 * the manifest for validation to the controlling documents, 4) Determines
 * whether or not extensions were used in the manifest, 5) Determines if the
 * manifest abides to the rules defined for the Content Package Application
 * Profile rules, and 6) Validates external metadata and scos if the using
 * system chooses to. The CPValidator inherites from the ADLSCORMValidator to
 * determine if the imsmanifest is wellformed and valid to the xsd(s). The
 * CPValidator object validates the content package test subject against the
 * rules and requirements necessary for meeting each Content Package Application
 * Profile. <br>
 * <br>
 * 
 * @author ADL Technical Team <br>
 *         <br>
 */
public class CPValidator extends ADLSCORMValidator implements IValidator
{

	private static final long serialVersionUID = 1L;

   /**
    * This attribute allows full validation activities (required files check,
    * schema validation, application profile checks, extension detection) to be
    * turned off, allowing only wellformedness checks to occur.
    */
   protected boolean mPerformFullValidation;

   /**
    * Logger object used for debug logging.
    */
   //private transient Logger mLogger = Logger.getLogger("org.adl.util.debug.validator");

   private static Log mLogger = LogFactory.getLog(CPValidator.class);
   
   
   /**
    * This attribute serves as the object that contains the information required
    * for the SCORM Validation checks, containing the DOM of the XML rules that
    * are neccessary for meeting conformance to each of the Application
    * Profiles.
    */
   private RulesValidator mRulesValidator;

   /**
    * This attribute contains the base directory of where the test subject is
    * located. It is used by the validator to determine the location of the
    * package resources, including the manifest, sco's, and/or metadata.
    */
   private String mBaseDirectory;

   /**
    * This attribute contains the list of URLs as defined in the XML base
    * attributes. The XML base attribute specifies a base URI other than the
    * base URI of the document or external entity.
    */
   private String[][] mXMLBase = new String[3][2];

   /**
    * This attribute contains the list of organization level identifier values
    * found. This value is tracked to check that the IDREFs point to valid
    * identifiers.
    */
   private List mOrganizationIdentifierList;

   /**
    * This attribute serves as the data structure used to store the Launch Data
    * information of SCOs and Metadata referenced within the content package.
    */
   private ManifestHandler mManifestHandler;

   /**
    * This attribute holds the value containing the environment variable. The
    * environment variable is set to the using systems install directory.
    */
   private String mEnvironmentVariable;

   /**
    * This attribute contains the populated ManifestMap object, containing all
    * the information needed to validate manifest elements with/without the
    * existance of (Sub) manifest(s).
    */
   private ManifestMap mManifestInfo;

   /**
    * This attribute identifies the manifest that is up for validation during
    * the application profile checks.
    */
   private String mManifestID;

   /**
    * This attribute houses the resource nodes needed to validate that
    * initialization data is pointing to a sco.
    */
   private List mResourceNodes;

   /**
    * This List will hold all the identifierReferences listed in the entire
    * manifest. It will be used to determine if all resources have been
    * referenced for the dangling resource test (excess baggage).
    */
   private List mAllIdrefsList;

   /**
    * This attribute houses the idref values that have been found to be valid
    * references.
    */
   private Vector mValidIdrefs;

   /**
    * This attribute contains a list of the controlling documents that are
    * required to exist at the root of the content package, including those
    * controlling documents required by the IMS Manifest and the adlcp:location
    * metadata. The purpose of this list is to prevent duplication of the
    * controlling documents.
    */
   private List mRequiredFilesList;

   /**
    * This attribute holds the names of all the files listed in the package. It
    * is used to check against the manifest to determine if there are any files
    * in the package that are not listed in the manifest
    */
   private List mFileList;

   /**
    * This attribute holds the identifier values of the resource elements. It is
    * used to validate that the dependency element references a resource
    * identifier within the scope of its manifest only. Referencing resource
    * identifiers in (sub) manifest elements is not permitted.
    */
   private List mResourceIdentifierList;

   /**
    * This List holds all of the Resource Identifiers listed in the manifest. It
    * will be used to verify that all the resources have been referenced for the
    * dangling resource test (excess baggage).
    */
   private List mManifestResourceIdentifierList;

   /**
    * This table holds a listing of the resource identifiers (as the key) along
    * with the files that are associated with each. If a resource does not
    * contain file(s), than no entry is added to the table for that resource.
    */
   private Hashtable mResourceTable;
   
   /**
    * Indicates if the xsi:schemaLocation was declared in the XML instance. The
    * xsi:schemalocation attribute provides a hint to the XML Parser as to where
    * the controlling documents are located.  The xsi:schemalocation attribute
    * is an optional attribute. 
    */
   private boolean mSchemaLocExists;
   
   /**
    * holds all of the hrefs to see if they are duplicated with only changes of
    * letters in upper or lower case
    */
   private CaseSensitiveCollection mHrefCaseSensitiveList;
   

   /**
    * Default Constructor. Sets the attributes to their initial values.
    * 
    * @param iEnvironmentVariable the value specific to the location of the
    *           using system's install.
    */
   public CPValidator(String iEnvironmentVariable)
   {
      super("contentpackage");
      mBaseDirectory = "";

      // Initialize the 2-dimensional array of xml:base attributes
      mXMLBase[0][0] = "manifest";
      mXMLBase[1][0] = "resources";
      mXMLBase[2][0] = "resource";
      mXMLBase[0][1] = "";
      mXMLBase[1][1] = "";
      mXMLBase[2][1] = "";

      mOrganizationIdentifierList = new ArrayList();
      mRulesValidator = new RulesValidator("contentpackage");
      mManifestHandler = new ManifestHandler();
      mEnvironmentVariable = iEnvironmentVariable;
      mManifestInfo = new ManifestMap();
      mManifestID = "";
      mValidIdrefs = new Vector();
      mFileList = new ArrayList();
      mResourceNodes = new ArrayList();
      mResourceIdentifierList = new ArrayList();
      mHrefCaseSensitiveList = new CaseSensitiveCollection();
      mManifestResourceIdentifierList = new ArrayList();
      mRequiredFilesList = new ArrayList();
      mAllIdrefsList = new ArrayList();
      mManifestID = "";
      mResourceTable = new Hashtable();
      mSchemaLocExists = true;
   }

   /**
    * This method initiates the validation process. The checks called from this
    * method include the required files check, manifest checks (wellformedness
    * and schema validation), and application profile checks. If the test
    * subject is in the form of a zip, then the extract is performed prior to
    * the calls mentioned above.
    * 
    * @param iFileName The name of the SCORM Content Package test subject
    * @param iTestType The type of test subject ( pif or media )
    * @param iApplicationProfileType The Application Profile type of the test
    *           subject (content aggregation or resource )
    * @param iManifestOnly The boolean describing whether or not the IMS
    *           Manifest is to be the only subject validated. True implies that
    *           validation occurs only on the IMS Manifest (checks include
    *           wellformedness, schema validation, and application profile
    *           checks). False implies that the entire Content Package be
    *           validated (IMS Manifest checks with the inclusion of the
    *           required files checks, metadata, and sco testing).
    * @return - Boolean value indicating the outcome of the validation checks
    *         True implies that validation was error free, false implies
    *         otherwise.
    */
   public boolean validate(String iFileName, String iTestType, String iApplicationProfileType, boolean iManifestOnly)
   {
      boolean validateResult = true;
      String msgText;
      String rootDirectory;

      if (mLogger.isDebugEnabled()) {
	      mLogger.debug("CPValidator validate()");
	      mLogger.debug("      iXMLFileName coming in is " + iFileName);
	      mLogger.debug("      iApplicationProfileType coming in is " + iApplicationProfileType);
	      mLogger.debug("      iTestType coming in is " + iTestType);
      }
      
      // If the CTS is testing a Package Interchange File, then import the
      // content package (unzip and set up the CTS).
      if( iTestType.equals("pif") )
      {
         rootDirectory = importContentPackage(iFileName);

         mBaseDirectory = mEnvironmentVariable + File.separator + "PackageImport" + File.separator;
      }
      else
      {
         rootDirectory = mBaseDirectory = getPathOfFile(iFileName);
      }

      // Create the absolute URL for the location of the imsmanifest.xml
      String imsManifestFile = mBaseDirectory + "imsmanifest.xml";

      // Now check to see if the manifest file is present.
      File manifestFile = new File(imsManifestFile);
      boolean manifestFound = true;

      // Test to see whether or not the imsmanifest.xml file exists as defined
      // by the pathname.
      if( !manifestFile.exists() )
      {
         mLogger.debug("FAILED: Required file \"imsmanifest.xml\" not " + "found at the root of the package");
         DetailedLogMessageCollection.getInstance().addMessage(
            new LogMessage(MessageType.FAILED, Messages.getString("CPValidator.22")));

         manifestFound = false;
      }

      // Set the ADLSCORMValidator protected attribute with this value.
      // This value is needed for summary logging and ADLValidatorOutcome
      super.setIsIMSManifestPresent(manifestFound);

      // Retrieve imsmanifest for wellformedness and validation parse if
      // high level checks passed
      if( manifestFound )
      {
         mLogger.debug("INFO: Testing the manifest for Well-formedness");

         validateResult = checkWellformedness(imsManifestFile) && validateResult;

         boolean wellformed = super.getIsWellformed();
         mSchemaLocExists = super.getSchemaLocExists();

         // This is only done if the manifest is present, no reason to do so
         //  otherwise
         createManifestFileList(rootDirectory);

         // determine if the root element belongs to the IMS namespace
         // can only perform this check if we have a wellformed document.
         boolean validRoot = false;
         if( wellformed )
         {
            validRoot = super.isRootElementValid();
            super.setIsRootElement(validRoot);
         }

         // continue to validate only if we are dealing with an IMS Manifest
         if( validRoot )
         {
            if( wellformed && !iManifestOnly && mPerformFullValidation )
            {
               // If the imsmanifest.xml is well-formed, then check to make
               // sure all of the required files are present.
               
               // first determine if the default schemas are being used.  If so, warn to inform
               if (!mSchemaLocExists)
               {
                  msgText = Messages.getString("CPValidator.1");
                  mLogger.debug("WARNING: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));  
               }
               
               List declaredNamespaceList = super.getDeclaredNamespaces();
               validateResult = checkForRequiredFiles(mBaseDirectory, declaredNamespaceList) && validateResult;
            }

            if( validateResult && mPerformFullValidation )
            {
               msgText = "Testing the manifest for Validity to the Controlling Documents";
               mLogger.debug("INFO: " + msgText);
               

               // The imsmanifest.xml file is well-formed and all of the
               // required
               // files were present, now check to see if the imsmanifest.xml
               // is valid according to the controlling schemas.
               validateResult = checkValidityToSchema(imsManifestFile) && validateResult;

               mLogger.debug("OTHER: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.XMLOTHER, "HR"));

               msgText = Messages.getString("CPValidator.28");
               mLogger.debug("INFO: " + msgText);

               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

               // Prepare for manifest idref verification

               msgText = Messages.getString("CPValidator.30");
               mLogger.debug("INFO: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

               boolean populateResult = mManifestInfo.populateManifestMap(super.getDocument());

               boolean idrefResult = true;
               if( populateResult )
               {
                  Vector idrefs = mManifestInfo.checkAllIdReferences();

                  if( !idrefs.isEmpty() )
                  {
                     mLogger.debug("invalid idrefs exist");
                     validateResult = false && validateResult;

                     idrefResult = false;

                     super.setIsValidToApplicationProfile(super.getIsValidToApplicationProfile() && false);
                  }
                  else
                  {
                     mLogger.debug("INFO: ID/IDRef Validation checks passed");
                     DetailedLogMessageCollection.getInstance().addMessage(
                        new LogMessage(MessageType.INFO, Messages.getString("CPValidator.34")));
                  }
               }

               // Read in XML rules for Application Profile Checks
               boolean doXMLRulesExist = mRulesValidator.readInRules(iApplicationProfileType);

               if( doXMLRulesExist )
               {
                  trackOrgIdentifiers(super.getDocument());
                  boolean isAppProfileResult = compareToRules(super.getDocument(), "");

                  checkForExcessBaggage(iApplicationProfileType);

                  // Set the ADLSCORMValidator protected attribute with this
                  // value. This value is needed for summary logging and
                  // ADLValidatorOutcome
                  super.setIsValidToApplicationProfile(idrefResult && isAppProfileResult);

                  validateResult = isAppProfileResult && validateResult;
               }
               else
               {
                  // XML rules cannot be found to perform app profile checking,
                  // system error
                  msgText = Messages.getString("CPValidator.36");
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                  validateResult = false && validateResult;
               } // end if the XML Rules Exist
            } // end if perform full validation is requested
         } // end if dealing with IMS Namespace Manifest
         else
         {
            validateResult = false;
         }
      } // end if the manifest was found

      mLogger.debug("CPValidator validate()");

      return validateResult;
   }

   /**
    * This method extracts the selected test subject, in the form of a zip file,
    * to a temporary "/PackageImport" directory in order to perform validation.
    * 
    * @param iPIF The content package zip test file URI
    * @return extractDir returnst the path of the directory the file is
    *         extracted to
    */
   private String importContentPackage(String iPIF)
   {
      // get the extract dir
      mLogger.debug("CPValidator importContentPackage()");

      String extractDir = mEnvironmentVariable + File.separator + "PackageImport" + File.separator;

      mLogger.debug("extractDir = " + extractDir);

      // Unzip the content package into a local directory for processing
      UnZipHandler uzh = new UnZipHandler(iPIF, extractDir);
      uzh.extract();

      mLogger.debug("CPValidator importContentPackage()");
      return extractDir;
   }

   /**
    * This method performs the validation on the imsmanifest.xml file -
    * including wellformedness and validation to the schema checks. This method
    * is called when the validate method is spanned.
    * 
    * @param iManifestFileName - The URI location of the manifest that is to be
    *           parsed for wellformedness and validation to the schema(s).
    * @return - boolean result of the parse. True implies that the manifest was
    *         wellformed and valid to the schema, false implies otherwise.
    */
   private boolean checkValidityToSchema(String iManifestFileName)
   {
      mLogger.debug("CPValidator checkValidityToSchema()");
      boolean manifestResult = true;

      // Send imsmanifest.xml for wellformedness and validation parse
      super.setPerformFullValidation(mPerformFullValidation);
      super.performValidatorParse(iManifestFileName);
      boolean isValid = super.getIsValidToSchema();

      boolean isWellformed = super.getIsWellformed();
      if( mPerformFullValidation )
      {
         if( isValid )
         {
            mLogger.debug("PASSED: " + "The manifest instance is valid to the controlling documents");
         }
         else
         {
            mLogger.debug("FAILED: " + "The manifest instance is not valid to the controlling documents");
         }

         manifestResult = isWellformed && isValid;
      }
      else
      {
         manifestResult = isWellformed;
      }

      mLogger.debug("CPValidator checkValidityToSchema()");

      return manifestResult;
   }

   /**
    * This method performs the validation on the imsmanifest.xml file -
    * including wellformedness. This method is called when the
    * CPValidator.validate method is spanned. When we complete this method, we
    * will have a wellformed document that has been stripped of extenions
    * attributes/elements, comments, and whitespace.
    * 
    * @param iManifestFileName - The URI location of the manifest that is to be
    *           parsed for wellformedness and validation to the schema(s).
    * @return - boolean result of the parse. True implies that the manifest was
    *         wellformed and valid to the schema, false implies otherwise.
    */
   private boolean checkWellformedness(String iManifestFileName)
   {
      mLogger.debug("CPValidator checkWellformedness()");
      boolean wellnessResult = true;

      // Send imsmanifest for wellformedness and validation parse
      super.setPerformFullValidation(false);
      super.performValidatorParse(iManifestFileName);

      boolean isWellformed = super.getIsWellformed();

      wellnessResult = isWellformed;

      mLogger.debug("CPValidator checkWellformedness");

      return wellnessResult;
   }

   /**
    * This method performs the check that the required files - imsmanifest and
    * all supporting controlling documents - exist at the root of the package.
    * This method throws an error if the imsmanifest.xml, ADL CP xsd, or IMS CP
    * controlling documents are not detected at the root of the content package
    * test subject. Warnings are thrown if the IMS SS or IEEE LOM Custom
    * controlling documents are not detected at the root of the content package
    * test subject. At this time, the controlling documents are hardcoded.
    * Future enhancements include the detection of the controlling documents
    * based off of the root manifest declaration.
    * 
    * @param iDirectory The directory to check to see if the required files are
    *           present.
    * @return boolean - true if the required files are present, false otherwise
    */
   private boolean checkForRequiredFiles(String iDirectory, List iDeclaredNamespaces)
   {
      mLogger.debug("CPValidator checkForRequiredFiles()");
      boolean result = true;
      ManifestHandler mdHandler = new ManifestHandler();
      String schemaLocations = "";

      mLogger.debug("XMLOTHER HR: ");

      DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.XMLOTHER, "HR"));

      mLogger.debug("INFO: Searching for Files Required For XML Parsing");
      DetailedLogMessageCollection.getInstance().addMessage(
         new LogMessage(MessageType.INFO, Messages.getString("CPValidator.63")));

      // All control files also are required to be at the root of the package.
      // This includes any and all control files needed to validate XML
      // instances
      // including those referenced by the <adlcp:location> element.

      // first retrieve all the controlling documents needed by the metadata
      // referenced by the <adlcp:location>
      Vector locationMD = mdHandler.getLocationMD(super.getDocument().getDocumentElement());

      int locationMDSize = locationMD.size();

      // loop through each MD to retrieve schemaLoc
      if( !locationMD.isEmpty() )
      {
         //Create an adldomparser object
         ADLDOMParser adldomparser = new ADLDOMParser();

         for( int i = 0; i < locationMDSize; i++ )
         {
            String currentMetadataData = (String)locationMD.elementAt(i);
            // Have to parser the adlcp:location metadata to determine
            // required controlling files
            String metadataFile = iDirectory;
            metadataFile = metadataFile.replace('\\', '/');
            metadataFile = metadataFile + currentMetadataData;

            // retrieve adldomparser attribute values to determine if the
            // adlcplocation metadata was wellfomed. We cannot determine
            // the required controlling documents without wellformed XML

            adldomparser.parseForWellformedness(metadataFile, false, true);

            if( !adldomparser.getIsWellformed() )
            {
               result = false;

               DetailedLogMessageCollection.getInstance().addMessage(
                  new LogMessage(MessageType.FAILED, Messages.getString("CPValidator.64", currentMetadataData,
                     currentMetadataData)));
            }
         }

         // need to add the controlling docs needed by the adlcp:location
         schemaLocations += adldomparser.getSchemaLocation();
      }

      // need to append controlling docs needed by IMS Manifest
      String allSchemaLocations = schemaLocations + super.getSchemaLocation();

      // call a method to parse the controlling document names including those
      // needed by the IMS Manifest and the <adlcp:location> and detect
      // if they exist at the root
      int requiredFileResult = findAndLocateRequiredFiles(allSchemaLocations, iDirectory, iDeclaredNamespaces);

      // Determines if default checking is needed
      if( requiredFileResult == 0 )
      {
         result = false && result;
      }
      else if( requiredFileResult == 1 )
      {
         result = true && result;
      }

      DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.XMLOTHER, "HR"));

      mLogger.debug("returning the following result " + result);
      mLogger.debug("CPValidator checkForRequiredFiles()");

      // Set the ADLSCORMValidator protected attribute for summary log info
      super.setIsRequiredFiles(result);

      return result;
   }

   /**
    * This method parses the mSchemaLocation value and retrieves only the schema
    * name. Once the schema name is tokenized, it is then checked for existance
    * at the root of the package.
    * 
    * @param iSchemaLocations -- the string value that houses the
    *           schemaLocations
    * @param iDirectory -- the base directory of the test suite that is used to
    *           build the location of the schemas.
    * @return integer - 1 if files are found, 0 if files are not found.
    */
   private int findAndLocateRequiredFiles(String iSchemaLocations, String iDirectory, List iDeclaredNamespaces)
   {
      int result = 1;
      File currentFile;
      boolean currentResult;
      String msgText = "";
      String tok = "";
      String defaultSchemaLocList = "";

      String xsdLocation = iDirectory.replace('\\', '/');
      
      // determine if we are dealing with the defaulted schema location list
      if (!mSchemaLocExists)
      {
         // the default schemas have been set. 
         
         // retrieve namespace section of schemaLocation value to determine which
         // required files apply
         String[] namespace = iSchemaLocations.split(" ");
         for (int x=0; x <namespace.length; x++)
         {
            if(namespace[x].startsWith("http"))
            {
               // we have a namespace that needs compared to the declared namespaces
               if (iDeclaredNamespaces.contains(namespace[x]) )
               {
                  //remove namespace and schemalocation from schemalocation value
                  defaultSchemaLocList += namespace[x] + " " + namespace[x+1] + " ";
               }
            }
         }
         iSchemaLocations = defaultSchemaLocList;
      }



      StringTokenizer st = new StringTokenizer(iSchemaLocations, " ");

      //Parse the schemaLocation value to obtain only the schemaName
      // then check each to see if it exists in the package

      while( st.hasMoreTokens() )
      {
         //This is the namespace value
         tok = st.nextToken();

         if( st.hasMoreTokens() )
         {
            // this is the schemaLocation value
            tok = st.nextToken();

            StringTokenizer schemaLocationTokens = new StringTokenizer(tok, "/");

            String schemaName = "";
            String toke = "";
            String previousToke = "";
            boolean verifyExistance = true;

            while( schemaLocationTokens.hasMoreTokens() )
            {
               // Need to obtain only the schema files name from the string
               previousToke = toke;
               toke = schemaLocationTokens.nextToken();

               if( !schemaLocationTokens.hasMoreTokens() )
               {
                  if( !previousToke.equals("vocab") )
                  {
                     schemaName = xsdLocation + toke;
                  }
                  else
                  {
                     // Do not need to verify schemas that aren't located
                     // at the root (i.e. custom vocab schemas).
                     verifyExistance = verifyExistance && false;
                  }
               }
            }

            if( verifyExistance )
            {
               // Test if this file exists
               // do not test duplicates

               if( !mRequiredFilesList.contains(toke) )
               {
                  currentFile = new File(schemaName);
                  currentResult = currentFile.exists();

                  if( currentResult )
                  {
                     mLogger.debug("PASSED: File \"" + toke + "\" found " + "at the root of the content package");
                     DetailedLogMessageCollection.getInstance().addMessage(
                        new LogMessage(MessageType.PASSED, Messages.getString("CPValidator.81", toke)));
                  }
                  else
                  {
                     msgText = Messages.getString("CPValidator.83", toke);
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = 0;

                  }
                  // add to List to account for future duplicate files
                  mRequiredFilesList.add(toke);
               }
            }
         }
      }
      return result;
   }

   /**
    * This method retrieves the directory location of the test subject by
    * truncating the filename off of the URL passed in.
    * 
    * @param iFileName The absolute path of the test subject file
    * @return String - the directory that the file is located
    */
   private String getPathOfFile(String iFileName)
   {
      mLogger.debug("CPValidator getPathOfFile()");

      String result = "";
      String tmp = "";

      try
      {
         StringTokenizer token = new StringTokenizer(iFileName, File.separator, true);

         int numTokens = token.countTokens();

         // We want all but the last token added
         numTokens--;

         for( int i = 0; i < numTokens; i++ )
         {
            tmp = token.nextToken();

            mLogger.debug("token = " + tmp);

            result = result + tmp;
         }
      }
      catch( NullPointerException npe )
      {
         npe.printStackTrace();
      }

      mLogger.debug("CPValidator getPathOfFile()");

      return result;
   }

   /**
    * This method tracks all identifier values found in the manifest for the
    * organization elements only. Tracking of organization identifiers is
    * performed in order to verify that default attribute points to valid
    * organization identifier value. These identifers are tracked recursively by
    * walking through test subject dom and adding the identifier elements found
    * to a list.
    * 
    * @param iParentNode Root node of the test subject
    */
   private void trackOrgIdentifiers(Node iParentNode)
   {
      // recursively find the organization ids and add them to the vector

      mLogger.debug("CPValidator trackOrgIdentifiers()");
      String msgText = "";

      if( iParentNode != null )
      {
         int type = iParentNode.getNodeType();

         switch( type )
         {
            case Node.DOCUMENT_NODE:
            {
               Node rootNode = ( (Document)iParentNode ).getDocumentElement();

               trackOrgIdentifiers(rootNode);

               break;
            }

            case Node.ELEMENT_NODE:
            {
               String nodeName = iParentNode.getLocalName();

               if( nodeName.equals("manifest") )
               {
                  Node orgsNode = DOMTreeUtility.getNode(iParentNode, "organizations");

                  if( orgsNode != null )
                  {
                     Vector orgNodes = DOMTreeUtility.getNodes(orgsNode, "organization");

                     // Loop through the oganization elements to retrieve the
                     // identifier attribute values

                     int orgNodesSize = orgNodes.size();
                     for( int i = 0; i < orgNodesSize; i++ )
                     {
                        Node currentChild = (Node)orgNodes.elementAt(i);
                        String orgIdentifier = DOMTreeUtility.getAttributeValue(currentChild, "identifier");

                        mOrganizationIdentifierList.add(orgIdentifier);
                        msgText = "Just added " + orgIdentifier + "to the org vector";
                        mLogger.debug(msgText);
                     }
                  }
               }

               // Get (sub)manifests and make call recursively
               Vector subManifestList = DOMTreeUtility.getNodes(iParentNode, "manifest");

               int subManifestListSize = subManifestList.size();
               for( int j = 0; j < subManifestListSize; j++ )
               {
                  Node currentSubManifest = (Node)subManifestList.elementAt(j);
                  trackOrgIdentifiers(currentSubManifest);
               }
               break;
            }
            default:
            {
               // Do nothing - no defined requirements to process any other
               // type of node type
               break;
            }
         }
      }

      mLogger.debug("CPValidator trackOrgIdentifiers()");
   }

   /**
    * This method tracks all identifier values found in the manifest for the
    * resource elements only. Tracking of resource identifiers is performed in
    * order to verify that dependency element points to valid resource
    * identifier value. The dependency element shall only point within its
    * parents scope. Sub-manifest resources may not be referenced. <br>
    * 
    * @param iResourcesNode The &lt;resources&gt; node that parents the
    *           &lt;resource&gt; elements. <br>
    */
   private void trackResourceIdentifiers(Node iResourcesNode)
   {
      // recursively find the resource ids and add them to the list

      mLogger.debug("CPValidator trackResourceIdentifiers()");

      Vector resourceNodes = new Vector();

      if( iResourcesNode != null )
      {
         // this will return a Vector of all child nodes of iResourceNode whos
         //  name = "resource"
         resourceNodes = DOMTreeUtility.getNodes(iResourcesNode, "resource");
         int resourceNodesSize = resourceNodes.size();

         for( int i = 0; i < resourceNodesSize; i++ )
         {
            Node currentChild = (Node)resourceNodes.elementAt(i);

            String resourceId = DOMTreeUtility.getAttributeValue(currentChild, "identifier");

            // This list is cleaned out for each manifest/sub-manifest
            mResourceIdentifierList.add(resourceId);

            // This list holds ALL of the identifier values listed in the
            //  entire manifest. 
            mManifestResourceIdentifierList.add(resourceId);
         }
      }// end if

      mLogger.debug("CPValidator trackResourceIdentifiers()");
   }

   /**
    * This method performs the application profile checks for the
    * adlcp:scormType attribute. The application profile checks include
    * verifying that the attribute belongs to the ADL CP namespace and that it
    * exists as an attribute of the IMS resource element only.
    * 
    * @param iCurrentAttribute the scormType attribute to be tested
    * @param iParentNode the parent element that the attribute belongs to.
    * @return boolean True implies that the application profile checks passed;
    *         false implies that they did not
    */
   private boolean checkSCORMTypeReq(Attr iCurrentAttribute, Node iParentNode)
   {
      boolean result = false;

      if( DOMTreeUtility.isAppropriateElement(iParentNode, "resource", "http://www.imsglobal.org/xsd/imscp_v1p1") )
      {
         result = true;
      }
      else
      {
         String msgText = Messages.getString("CPValidator.107", iCurrentAttribute.getLocalName(), "resource");

         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

      }
      return result;
   }

   /**
    * This method performs the application profile checks for the
    * adlcp:objectivesGlobalToSystem attribute. The application profile checks
    * include verifying that the attribute belongs to the IMS namespace and that
    * it exists as an attribute of the IMS organization element only.
    * 
    * @param iCurrentAttribute the objectivesGlobalToSystem attribute to be
    *           tested
    * @param iParentNode the parent element that the attribute belongs to.
    * @return boolean True implies that the application profile checks passed;
    *         false implies that they did not
    */
   private boolean checkObjGlobalToSystemReq(Attr iCurrentAttribute, Node iParentNode)
   {
      boolean result = false;

      if( DOMTreeUtility.isAppropriateElement(iParentNode, "organization", "http://www.imsglobal.org/xsd/imscp_v1p1") )
      {
         result = true;
      }
      else
      {
         String msgText = Messages.getString("CPValidator.107", iCurrentAttribute.getLocalName(), "organization");

         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

      }
      return result;
   }

   /**
    * This method performs the meat of the application profile checks. The
    * application profiles are described in XML format. Each application profile
    * has its own XML representation. These XML rules are parsed in order to
    * form a document object. The test subject manifest is also available in
    * document format. This method compares the manifest to the rules described
    * in the XML dom. This recursive method is driven by the test subject dom.
    * 
    * @param iTestSubjectNode Test Subject DOM
    * @param iPath Path of the rule to compare to
    * @return - boolean result of the checks performed. True if the check was
    *         conformant, false otherwise.
    */
   private boolean compareToRules(Node iTestSubjectNode, String iPath)
   {
      mLogger.debug("CPValidator compareToRules");
      mLogger.debug("Node: " + iTestSubjectNode.getLocalName());
      mLogger.debug("Namespace: " + iTestSubjectNode.getNamespaceURI());
      mLogger.debug("Path: " + iPath);

      boolean result = true;
      String msgText = "";

      // is there anything to do?
      if( iTestSubjectNode == null )
      {
         result = false;
         return result;
      }

      // Determine which type of DOM Tree Node we are dealing with
      switch( iTestSubjectNode.getNodeType() )
      {
         case Node.PROCESSING_INSTRUCTION_NODE:
         {
            // Skip any processing instructions, nothing for us to do
            break;
         }
         case Node.DOCUMENT_NODE:
         {
            // Found the root document node
            Node rootNode = ( (Document)iTestSubjectNode ).getDocumentElement();
            String rootNodeName = rootNode.getLocalName();

            mLogger.debug("DOCUMENT_NODE found");
            mLogger.debug("Namespace: " + rootNode.getNamespaceURI());
            mLogger.debug("Node Name: " + rootNodeName);

            mLogger.debug("INFO: Testing element <" + rootNodeName + "> for minimum conformance");
            DetailedLogMessageCollection.getInstance().addMessage(
               new LogMessage(MessageType.INFO, Messages.getString("CPValidator.131", rootNodeName)));

            mLogger.debug("PASSED: Multiplicity for element <" + rootNodeName + "> has been verified");
            DetailedLogMessageCollection.getInstance().addMessage(
               new LogMessage(MessageType.PASSED, Messages.getString("CPValidator.135", rootNodeName)));

            result = compareToRules(rootNode, "") && result;

            break;
         }
         case Node.ELEMENT_NODE:
         {
            // Found an Element Node
            String parentNodeName = iTestSubjectNode.getLocalName();

            if( parentNodeName.equals("manifest") )
            {
               // retrieve resources nodes for sco reference validation
               Node resourcesNode = DOMTreeUtility.getNode(iTestSubjectNode, "resources");

               if( resourcesNode != null )
               {
                  // retrieve resource nodes for sco reference validation
                  mResourceNodes = DOMTreeUtility.getNodes(resourcesNode, "resource");
                  // Must also track resource identifier values for
                  // dependency identifierref scope validation

                  trackResourceIdentifiers(resourcesNode);
               }
            }

            String dataType = null;
            int multiplicityUsed = -1;
            int minRule = -1;
            int maxRule = -1;
            int spmRule = -1;

            mLogger.debug("Looping through attributes for the input " + "element <" + parentNodeName + ">");
            

            // Look for the attributes of this element
            NamedNodeMap attrList = iTestSubjectNode.getAttributes();
            int numAttr = attrList.getLength();
            mLogger.debug("There are " + numAttr + " attributes of <" + parentNodeName + "> elememt to test");

            Attr currentAttrNode = null;
            String currentNodeName = "";
            String attributeValue = "";

            // Loop throught attributes
            for( int i = 0; i < numAttr; i++ )
            {
               currentAttrNode = (Attr)attrList.item(i);
               currentNodeName = currentAttrNode.getLocalName();
               
               mLogger.debug("Processing the [" + currentAttrNode.getNamespaceURI() + "] " + currentNodeName
                  + " attribute of the <" + parentNodeName + "> element.");

               //  If the current attribute is persistState then additional
               // checks may be necessary
               if( currentNodeName.equalsIgnoreCase("persistState") )
               {
                  // we must fail. SCORM 3rd Edition Addendum has removed the
                  // persistState attribute from the adlcp namespaced schema
                  msgText = Messages.getString("CPValidator.274", currentNodeName);
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                  result = false && result;
               }

               // If the current attribute is scormType then additional
               // checks may be necessary
               if( currentNodeName.equalsIgnoreCase("scormType") )
               {
                  // Application Profile Check: Check to make sure that the
                  // adlcp:scormType attribute can only appear on an
                  // <imscp:resource> element

                  result = checkSCORMTypeReq(currentAttrNode, iTestSubjectNode) && result;

               }

               // If the current attribute is objectivesGlobalToSystem then
               // additional checks may be necessary
               if( currentNodeName.equalsIgnoreCase("objectivesGlobalToSystem") )
               {
                  // Application Profile Check: Check that the
                  // adlseq:objectivesGlobalToSystem attribute can only appear
                  // on an <imscp:organization> element.
                  result = checkObjGlobalToSystemReq(currentAttrNode, iTestSubjectNode) && result;
               }

               // Retrieve the application profile rules only if the the current
               // attribute being processed has SCORM application profile
               // requirements
               mLogger.debug("Additional checks needed for attribute [" + currentNodeName + "].\r\n");
      
               // Retreive the data type rules
               dataType = mRulesValidator.getRuleValue(parentNodeName, iPath, "datatype", currentNodeName);

               // If the data type rules are for an xml:base, then there is
               // more processing that needs to take place.
               if( dataType.equalsIgnoreCase("xmlbase") )
               {
                  // This is a xml:base data type
                  msgText = Messages.getString("CPValidator.164", currentNodeName);
                  mLogger.debug("INFO: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

                  multiplicityUsed = getMultiplicityUsed(attrList, currentNodeName);

                  // We will assume that no attribute can exist more than
                  // once (ever). According to W3C. Therefore, min and max
                  // rules must exist.

                  // Get the min rule and convert to an int
                  minRule = Integer.parseInt(mRulesValidator
                     .getRuleValue(parentNodeName, iPath, "min", currentNodeName));

                  // Get the max rule and convert to an int
                  maxRule = Integer.parseInt(mRulesValidator
                     .getRuleValue(parentNodeName, iPath, "max", currentNodeName));

                  if( ( minRule != -1 ) || ( maxRule != -1 ) )
                  {
                     if( multiplicityUsed >= minRule && multiplicityUsed <= maxRule )
                     {
                        msgText = Messages.getString("CPValidator.169", currentNodeName);
                        mLogger.debug("PASSED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.PASSED, msgText));
                     }
                     else
                     {
                        msgText = Messages.getString("CPValidator.175", currentNodeName);
                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));

                        result = false && result;

                     } // mult used >= minRule && mult used <= maxRule
                  } // end minRule != -1, maxRule != -1

                  // Get the spm rule and convert to an int
                  spmRule = Integer.parseInt(mRulesValidator
                     .getRuleValue(parentNodeName, iPath, "spm", currentNodeName));

                  attributeValue = currentAttrNode.getValue();

                  // Check the attributes for smallest permitted maximum(spm)
                  // conformance.

                  result = checkSPMConformance(currentNodeName, attributeValue, spmRule, true) && result;

                  // Check to make sure slashes are correct
                     result = checkForSlashes("xml:base", attributeValue) && result;

                  if( parentNodeName.equals("manifest") )
                  {
                     mXMLBase[0][1] = attributeValue;
                     mLogger.debug(" XML:base found in manifest, value is " + attributeValue);
                  }
                  else if( parentNodeName.equals("resources") )
                  {
                     mXMLBase[1][1] = attributeValue;
                     mLogger.debug(" XML:base found in resources, value is " + attributeValue);
                  }
                  else if( parentNodeName.equals("resource") )
                  {
                     mXMLBase[2][1] = attributeValue;
                     mLogger.debug(" XML:base found in resource, value is " + attributeValue);
                  }
               } // end if xml:base
            } // end looping over set of attributes for the element

            // If we are processing an <imscp:manifest> element, then there
            // are special cases application profile checks needed.
            if( parentNodeName.equalsIgnoreCase("manifest") )
            {
               mLogger.debug("Manifest node, additional check's being done.");
               mLogger.debug("Determining how many times the " + "identifier attribute is present.");

               multiplicityUsed = getMultiplicityUsed(attrList, "identifier");

               if( multiplicityUsed < 1 )
               {
                  mLogger.debug("FAILED: Mandatory attribute \"identifier\"" + " could not be found");
                  DetailedLogMessageCollection.getInstance().addMessage(
                     new LogMessage(MessageType.FAILED, Messages.getString("CPValidator.198", "identifier")));

                  result = false && result;
               }
            }
            else if( parentNodeName.equalsIgnoreCase("organizations")
               && ( mRulesValidator.getApplicationProfile() ).equals("contentaggregation") )
            {
               // multiple <organization> elements exist, but there is no
               // default attribute.
               // not a conformance check, warning only
               multiplicityUsed = getMultiplicityUsed(attrList, "default");

               if( multiplicityUsed < 1 )
               {
                  mLogger.debug("ERROR: Mandatory attribute \"default\" " + "could not be found");
                  DetailedLogMessageCollection.getInstance().addMessage(
                     new LogMessage(MessageType.FAILED, Messages.getString("CPValidator.198", "default")));

                  result = false && result;
               }
            }
            else if( parentNodeName.equalsIgnoreCase("organization")
               && ( mRulesValidator.getApplicationProfile() ).equals("contentaggregation") )
            {
               multiplicityUsed = getMultiplicityUsed(attrList, "identifier");
               if( multiplicityUsed < 1 )
               {
                  mLogger.debug("FAILED: Mandatory attribute \"identifier\" " + "could not be found");
                  DetailedLogMessageCollection.getInstance().addMessage(
                     new LogMessage(MessageType.FAILED, Messages.getString("CPValidator.198", "identifier")));

                  result = false && result;
               }
            }
            else if( parentNodeName.equalsIgnoreCase("item")
               && ( mRulesValidator.getApplicationProfile() ).equals("contentaggregation") )
            {
               multiplicityUsed = getMultiplicityUsed(attrList, "identifier");
               if( multiplicityUsed < 1 )
               {
                  mLogger.debug("FAILED: Mandatory attribute \"identifier\" " + "could not be found");
                  DetailedLogMessageCollection.getInstance().addMessage(
                     new LogMessage(MessageType.FAILED, Messages.getString("CPValidator.198", "identifier")));

                  result = false && result;
               }

               // need to perform a special check to warn when parameters
               // are present but no identifierref is
               int idrefMult = -1;
               int paramMult = -1;

               idrefMult = getMultiplicityUsed(attrList, "identifierref");
               paramMult = getMultiplicityUsed(attrList, "parameters");

               if( ( idrefMult < 1 ) && !( paramMult < 1 ) )
               {
                  // we have a parameters but no identifierref - warning
                  msgText = Messages.getString("CPValidator.220");

                  mLogger.debug("WARNING: " + msgText);

                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

               }
               // have to store the idref values in a vector for future
               // app profile checking of resource attributes

               if( idrefMult >= 1 )
               {
                  String iDREFValue = DOMTreeUtility.getAttributeValue(iTestSubjectNode, "identifierref");

                  boolean validIdref = mResourceIdentifierList.contains(iDREFValue);

                  if( validIdref )
                  {
                     mValidIdrefs.add(iDREFValue);
                  }

                  // Whether or not it is true we need to keep track of ALL of
                  //  the idrefs so we can look for dangling references after
                  //  the entire list of refs, including those on sub-manifests
                  //  have been inventoried.  
                  mAllIdrefsList.add(iDREFValue);

               }

               // Perform a special check to ensure that initialization data
               // only exists on items that reference SCOs
               NodeList childrenOfItem = iTestSubjectNode.getChildNodes();
               if( childrenOfItem != null )
               {
                  Node currentItemChild;
                  String currentItemChildName;
                  int len = childrenOfItem.getLength();
                  for( int k = 0; k < len; k++ )
                  {
                     currentItemChild = childrenOfItem.item(k);
                     currentItemChildName = currentItemChild.getLocalName();

                     if( currentItemChildName.equals("timeLimitAction") || currentItemChildName.equals("dataFromLMS")
                        || currentItemChildName.equals("completionThreshold") )
                     {
                        if( idrefMult < 1 )
                        {
                           // we have an item that contains initialization data
                           // and does not reference a resource at all

                           result = false && result;

                           msgText = Messages.getString("CPValidator.226", currentItemChildName);

                           mLogger.debug("FAILED: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.FAILED, msgText));

                        }
                        else
                        {
                           // we must verify that the resource it is referencing
                           // is a sco

                           String idrefValue = DOMTreeUtility.getAttributeValue(iTestSubjectNode, "identifierref");

                           result = result && checkForReferenceToSco(idrefValue);

                        }
                     }
                  }
               }
            }
            else if( parentNodeName.equalsIgnoreCase("resource") )
            {
               checkBucketUniqueness(iTestSubjectNode);
               boolean resourceResult = checkResourceAttributes(iTestSubjectNode, attrList);

               result = result && resourceResult;
            }
            else if( parentNodeName.equalsIgnoreCase("bucket") )
            {
               checkBucketAttributes(iTestSubjectNode);
            }
            else if( parentNodeName.equalsIgnoreCase("size") )
            {
               checkSizeAttributes(iTestSubjectNode);
            }

            // test the attributes

            for( int j = 0; j < numAttr; j++ )
            {
               currentAttrNode = (Attr)attrList.item(j);
               currentNodeName = currentAttrNode.getLocalName();

               dataType = mRulesValidator.getRuleValue(parentNodeName, iPath, "datatype", currentNodeName);

               // we do not want to test for xml namespaces or extensions

               if( dataType.equalsIgnoreCase("idref") || dataType.equalsIgnoreCase("id")
                  || dataType.equalsIgnoreCase("vocabulary") || dataType.equalsIgnoreCase("text")
                  || dataType.equalsIgnoreCase("uri") )
               {
                  msgText = Messages.getString("CPValidator.164", currentNodeName);
                  mLogger.debug("INFO: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.INFO, msgText));

                  multiplicityUsed = getMultiplicityUsed(attrList, currentNodeName);

                  // We will assume that no attribute can exist more than
                  // once (ever). According to W3C. Therefore, min and max
                  // rules must exist.

                  //get the min rule and convert to an int

                  minRule = Integer.parseInt(mRulesValidator
                     .getRuleValue(parentNodeName, iPath, "min", currentNodeName));

                  //get the max rule and convert to an int

                  maxRule = Integer.parseInt(mRulesValidator
                     .getRuleValue(parentNodeName, iPath, "max", currentNodeName));

                  if( ( minRule != -1 ) || ( maxRule != -1 ) )
                  {
                     if( multiplicityUsed >= minRule && multiplicityUsed <= maxRule )
                     {
                        msgText = Messages.getString("CPValidator.169", currentNodeName);
                        mLogger.debug("PASSED: " + msgText);
                        
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.PASSED, msgText));
                     }
                     else
                     {
                        msgText = Messages.getString("CPValidator.175", currentNodeName);
                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));

                        result = false && result;
                     }
                  }

                  //get the spm rule and convert to an int
                  spmRule = Integer.parseInt(mRulesValidator
                     .getRuleValue(parentNodeName, iPath, "spm", currentNodeName));

                  attributeValue = currentAttrNode.getValue();

                  if( dataType.equalsIgnoreCase("idref") )
                  {
                     // This is a IDREF data type
                     // check the attributes for smallest permitted maximum
                     // (spm) conformance.

                     result = checkSPMConformance(currentNodeName, attributeValue, spmRule, true) && result;

                     // check the Default Idref to make sure it points to an
                     // valid identifier.

                     if( currentNodeName.equalsIgnoreCase("default") )
                     {
                        boolean foundDefaultIdentifier = false;
                        // check for this identifer in the organization list
                        int numOrganizationIdentifiers = mOrganizationIdentifierList.size();

                        for( int i = 0; i < numOrganizationIdentifiers; i++ )
                        {
                           String identifier = (String)( mOrganizationIdentifierList.get(i) );

                           if( identifier.equals(attributeValue) )
                           {
                              foundDefaultIdentifier = true;

                              break;
                           }
                        }
                        if( foundDefaultIdentifier )
                        {
                           msgText = Messages.getString("CPValidator.251", currentNodeName);
                           mLogger.debug("PASSED: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.PASSED, msgText));
                        }
                        else
                        {
                           msgText = Messages.getString("CPValidator.254", currentNodeName);
                           mLogger.debug("FAILED: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.FAILED, msgText));

                           result = false && result;
                        }
                     }
                     if( currentNodeName.equalsIgnoreCase("identifierref")
                        && parentNodeName.equalsIgnoreCase("dependency") )
                     {
                        mAllIdrefsList.add(currentAttrNode.getValue());
                     }

                  }
                  else if( dataType.equalsIgnoreCase("id") )
                  {
                     // This is a id data type
                     // check the attributes for smallest permitted maximum
                     // (spm) conformance.

                     result = checkSPMConformance(currentNodeName, attributeValue, spmRule, true) && result;

                     if( parentNodeName.equals("manifest") )
                     {
                        mManifestID = currentAttrNode.getValue();
                        mLogger.debug("mManifestID is " + mManifestID);
                     }

                     // imsssp id attributes here
                  }
                  else if( dataType.equalsIgnoreCase("uri") )
                  {
                     // This is a URI data type
                     // check the attributes for smallest permitted maximum
                     // (spm) conformance. Only perform these checks if
                     // the value is not an empty string

                     String myAttributeValue = currentAttrNode.getValue();
                     if( !myAttributeValue.equals("") )
                     {
                        // check to ensure there are no leading slashes
                        result = checkForSlashes("href", myAttributeValue) && result;

                        // check if the file exists
                        // apply xml:base on href value before href checks
                        if( doesXMLBaseExist() )
                        {
                           mLogger.debug("APPLYING XML BASE");
                           myAttributeValue = applyXMLBase(myAttributeValue);
                        }

                        if( myAttributeValue.indexOf('\\') != -1 )
                        {
                           msgText = Messages.getString("CPValidator.265", myAttributeValue);

                           mLogger.debug("FAILED: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.FAILED, msgText));

                           result &= false;
                        }

                        // check href spm after it is pre-appended with xml:base
                        result = checkSPMConformance(currentNodeName, myAttributeValue, spmRule, true) && result;

                        result = checkHref(myAttributeValue) && result;

                     }
                  }
                  else if( dataType.equalsIgnoreCase("vocabulary") )
                  {
                     // This is a VOCAB data type
                     // retrieve the vocab rule values and check against the
                     // vocab values that exist within the test subject

                     msgText = "Testing attribute \"" + currentNodeName + "\" for valid vocabulary";
                     mLogger.debug("INFO: " + msgText);

                     Vector vocabAttribValues = mRulesValidator.getAttribVocabRuleValues(parentNodeName, iPath,
                        currentNodeName);
                     // we are assuming that only 1 vocabulary value may
                     // exist for an attribute

                     result = checkVocabulary(currentNodeName, attributeValue, vocabAttribValues, true) && result;
                  }
                  else if( dataType.equalsIgnoreCase("text") )
                  {
                     //This is a TEXT data type
                     // check the attributes for smallest permitted maximum
                     // (spm) conformance.

                     result = checkSPMConformance(currentNodeName, attributeValue, spmRule, true) && result;

                     // test the parameter attribute for valid syntax
                     if( currentNodeName.equalsIgnoreCase("parameters") )
                     {
                        ParameterChecker pc = new ParameterChecker();
                        result = pc.checkParameters(attributeValue) && result;
                     }
                  }
               }
            } //done with attributes

            // Test the child Nodes

            NodeList children = iTestSubjectNode.getChildNodes();

            if( children != null )
            {
               int numChildren = children.getLength();

               // update the path for this child element

               String path;

               if( iPath.equals("") )
               {
                  // the node is a DOCUMENT or a root <manifest>
                  path = parentNodeName;
               }
               else if( ( iPath.equals("manifest.manifest") ) && ( parentNodeName.equals("manifest") ) )
               {
                  path = iPath;
               }
               else if( parentNodeName.equalsIgnoreCase("item") )
               {
                  // the Node is an <imscp:item>
                  if( iPath.equals("manifest.organizations.organization.item") )
                  {
                     path = iPath;
                  }
                  else
                  {
                     path = iPath + "." + parentNodeName;
                  }
               }
               else
               {
                  path = iPath + "." + parentNodeName;
               }

               // SPECIAL CASE: check for mandatory elements

               if( parentNodeName.equalsIgnoreCase("manifest") )
               {

                  // check for mandatory metadata element at package level
                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "metadata");
                  if( multiplicityUsed < 1 )
                  {
                     msgText = Messages.getString("CPValidator.287", "metadata");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }
                  else
                  // check for mandatory children
                  {
                     Node caMetadataNode = DOMTreeUtility.getNode(iTestSubjectNode, "metadata");

                     // check for mandatory <imscp:schema> element
                     multiplicityUsed = getMultiplicityUsed(caMetadataNode, "schema");

                     if( multiplicityUsed < 1 )
                     {
                        msgText = Messages.getString("CPValidator.287", "schema");
                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));

                        result = false && result;
                     }

                     // check for mandatory <imscp:schemaversion> element
                     multiplicityUsed = getMultiplicityUsed(caMetadataNode, "schemaversion");

                     if( multiplicityUsed < 1 )
                     {
                        msgText = Messages.getString("CPValidator.287", "schemaversion");
                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));

                        result = false && result;
                     }
                  }

                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "organizations");
                  if( multiplicityUsed < 1 )
                  {
                     msgText = Messages.getString("CPValidator.287", "organizations");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }

                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "resources");
                  if( multiplicityUsed < 1 )
                  {
                     msgText = Messages.getString("CPValidator.287", "resources");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }
               }
               else if( parentNodeName.equalsIgnoreCase("organizations")
                  && ( mRulesValidator.getApplicationProfile() ).equals("contentaggregation") )
               {
                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "organization");

                  if( multiplicityUsed < 1 )
                  {
                     msgText = Messages.getString("CPValidator.287", "organization");

                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }

               }
               // have to check to ensure that empty organizations exist
               // for resource package

               else if( parentNodeName.equalsIgnoreCase("organizations")
                  && ( mRulesValidator.getApplicationProfile() ).equals("resource") )
               {
                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "organization");
                  if( multiplicityUsed > 0 )
                  {
                     msgText = Messages.getString("CPValidator.311");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }
                  else
                  {
                     msgText = Messages.getString("CPValidator.312");
                     // we have an empty <orgs> element, display a valid msg
                     mLogger.debug("PASSED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

                  }
               }
               else if( parentNodeName.equalsIgnoreCase("organization")
                  && ( mRulesValidator.getApplicationProfile() ).equals("contentaggregation") )
               {
                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "title");
                  if( multiplicityUsed < 1 )
                  {
                     msgText = Messages.getString("CPValidator.287", "title");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }

                  multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, "item");
                  if( multiplicityUsed < 1 )
                  {
                     msgText = Messages.getString("CPValidator.287", "item");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                     result = false && result;
                  }

                  // special checks for item
                  result = checkItem(iTestSubjectNode, mManifestInfo) && result;

               }

               for( int z = 0; z < numChildren; z++ )
               {

                  Node currentChild = children.item(z);
                  String currentChildName = currentChild.getLocalName();

                  msgText = "Currentchild is " + currentChildName + " and path is " + path;

                  mLogger.debug(msgText);

                  if( currentChildName != null )
                  {

                     if( ( ( currentChildName.equals("timeLimitAction") ) || ( currentChildName.equals("dataFromLMS") )
                        || ( currentChildName.equals("completionThreshold") ) || ( currentChildName
                        .equals("presentation") ) )
                        && ( !parentNodeName.equals("item") ) )
                     {
                        result = false && result;

                        msgText = Messages.getString("CPValidator.328", currentChildName, "item");

                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));
                     }

                     if( ( ( currentChildName.equals("constrainedChoiceConsiderations") ) || ( currentChildName
                        .equals("rollupConsiderations") ) )
                        && ( !parentNodeName.equals("sequencing") ) )
                     {

                        result = false && result;

                        msgText = Messages.getString("CPValidator.328", currentChildName, "sequencing");

                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));
                     }

                     // must enforce that the adlcp:location exist
                     // as a child of metadata only - warning for best practice.

                     if( ( currentChildName.equals("location") ) && ( !parentNodeName.equals("metadata") ) )
                     {

                        result = false && result;

                        msgText = Messages.getString("CPValidator.328", currentChildName, "metadata");

                        mLogger.debug("WARNING: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.WARNING, msgText));
                     }

                     if( ( currentChildName.equals("sequencing") ) && ( !parentNodeName.equals("item") )
                        && ( !parentNodeName.equals("organization") ) )
                     {

                        result = false && result;

                        msgText = Messages.getString("CPValidator.345", currentChildName);

                        mLogger.debug("FAILED: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));
                     }

                     dataType = mRulesValidator.getRuleValue(currentChildName, path, "datatype");
                     // must enforce that the imsssp:bucket exist
                     // as a child of a resource only.

                     if( ( currentChildName.equals("bucket") ) && ( !parentNodeName.equals("resource") ) )
                     {
                        // Check to enforce that bucket is a child of a resource
                        msgText = "<" + currentChildName + "> can only " + "exist as a child of a <resource>";

                        mLogger.debug("SSP: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));

                     }

                     // must enforce that the imsssp:size exist
                     // as a child of a bucket only.

                     if( ( currentChildName.equals("size") ) && ( !parentNodeName.equals("bucket") ) )
                     {

                        msgText = "<" + currentChildName + "> can only " + "exist as a child of a <bucket>";

                        mLogger.debug("SSP: " + msgText);
                        DetailedLogMessageCollection.getInstance().addMessage(
                           new LogMessage(MessageType.FAILED, msgText));
                     }

                     // Check the SCORMType of the resource; it must be sco
                     if( ( currentChildName.equals("bucket") ) && ( parentNodeName.equals("resource") ) )
                     {
                        // Now check to ensure that the resource type is SCO
                        String typeS = DOMTreeUtility.getAttributeValue(iTestSubjectNode, "scormType");

                        if( !typeS.equalsIgnoreCase("sco") )
                        {
                           //result = false;

                           msgText = "The <" + currentChildName + "> shall" + " only exist in a resource that is "
                              + " scormType = \"sco\".";

                           mLogger.debug("SSP: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.FAILED, msgText));
                        }

                     }

                     // we do not want to test for extensions here

                     if( dataType.equalsIgnoreCase("parent") || dataType.equalsIgnoreCase("vocabulary")
                        || dataType.equalsIgnoreCase("text") || dataType.equalsIgnoreCase("sequencing")
                        || dataType.equalsIgnoreCase("metadata") || dataType.equalsIgnoreCase("decimal") )
                     {
                        // SCORM 3rd edition -- we need to ignore (sub)manifest
                        // and warn only

                        if( currentChildName.equals("manifest") && path.equals("manifest") )
                        {
                           msgText = Messages.getString("CPValidator.100");
                           mLogger.debug("WARNING: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.WARNING, msgText));

                           // Make cleansing call for excess baggage here
                           // pass (sub)manifest node into a new function

                           // retrieve all adlcp:location uri's contained in the
                           //(sub)manifest
                           Vector submanifestURIList = mManifestHandler.getLocationMD(currentChild);
                           trackSubManifest(currentChild, submanifestURIList);
                        }
                        else
                        // we are not dealing with (sub)manifest
                        {
                           msgText = Messages.getString("CPValidator.131", currentChildName);

                           mLogger.debug("INFO: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.INFO, msgText));

                           multiplicityUsed = getMultiplicityUsed(iTestSubjectNode, currentChildName);

                           //get the min rule and convert to an int

                           minRule = Integer.parseInt(mRulesValidator.getRuleValue(currentChildName, path, "min"));

                           //get the max rule and convert to an int

                           maxRule = Integer.parseInt(mRulesValidator.getRuleValue(currentChildName, path, "max"));

                           if( ( minRule != -1 ) && ( maxRule != -1 ) )
                           {
                              if( multiplicityUsed >= minRule && multiplicityUsed <= maxRule )
                              {
                                 msgText = Messages.getString("CPValidator.135", currentChildName);
                                 mLogger.debug("PASSED: " + msgText);
                                 DetailedLogMessageCollection.getInstance().addMessage(
                                    new LogMessage(MessageType.PASSED, msgText));
                              }
                              else
                              {
                                 msgText = Messages.getString("CPValidator.364", currentChildName);
                                 mLogger.debug("FAILED: " + msgText);
                                 DetailedLogMessageCollection.getInstance().addMessage(
                                    new LogMessage(MessageType.FAILED, msgText));

                                 result = false && result;
                              }
                           }
                           else if( ( minRule != -1 ) && ( maxRule == -1 ) )
                           {
                              if( multiplicityUsed >= minRule )
                              {
                                 msgText = Messages.getString("CPValidator.135", currentChildName);
                                 mLogger.debug("PASSED: " + msgText);
                                 DetailedLogMessageCollection.getInstance().addMessage(
                                    new LogMessage(MessageType.PASSED, msgText));
                              }
                              else
                              {
                                 msgText = Messages.getString("CPValidator.364", currentChildName);
                                 mLogger.debug("FAILED: " + msgText);
                                 DetailedLogMessageCollection.getInstance().addMessage(
                                    new LogMessage(MessageType.FAILED, msgText));

                                 result = false && result;
                              }
                           }
                           // test for each particular data type

                           if( dataType.equalsIgnoreCase("parent") )
                           {
                              // need to populate the files that belong to each resource
                              if( currentChildName.equals("resources") )
                              {
                                 populateResourceTable(currentChild);
                              }

                              // Verify that if the resource href matches
                              // the file href if the resource is local
                              if( currentChildName.equals("resource") )
                              {
                                 result = checkResourceFileHref(currentChild) && result;
                              }

                              //This is a parent element, need to recurse
                              result = compareToRules(currentChild, path) && result;

                           }
                           else if( dataType.equalsIgnoreCase("sequencing") )
                           {
                              // This is a sequencing data type

                              SequenceValidator sequenceValidator = new SequenceValidator();

                              result = sequenceValidator.validate(currentChild) && result;
                           }
                           else if( dataType.equalsIgnoreCase("metadata") )
                           {
                              // This is a metadata data type - no longer need
                              // to
                              // check for lom and location to coexist
                              // must detect that the metadata exists in
                              // location
                              if( currentChildName.equals("location") )
                              {
                                 String currentLocationValue = mRulesValidator.getTaggedData(currentChild);

                                 // check to ensure there are no leading slashes
                                 result = checkForSlashes("location", currentLocationValue) && result;

                                 currentLocationValue = applyXMLBase(currentLocationValue);

                                 result = result && checkHref(currentLocationValue);
                              }
                           }
                           else if( dataType.equalsIgnoreCase("text") )
                           {
                              // This is a text data type
                              // check spm

                              // first must retrieve the value of this child
                              // element

                              String currentChildValue = mRulesValidator.getTaggedData(currentChild);

                              //get the spm rule and convert to an int

                              spmRule = Integer.parseInt(mRulesValidator.getRuleValue(currentChildName, path, "spm"));

                              result = checkSPMConformance(currentChildName, currentChildValue, spmRule, false)
                                 && result;
                           }
                           else if( dataType.equalsIgnoreCase("vocabulary") )
                           {
                              // This is a vocabulary data type
                              // more than one vocabulary token may exist

                              msgText = Messages.getString("CPValidator.383", currentChildName);
                              mLogger.debug("INFO: " + msgText);
                              DetailedLogMessageCollection.getInstance().addMessage(
                                 new LogMessage(MessageType.INFO, msgText));

                              // retrieve the value of this element

                              String currentChildValue = mRulesValidator.getTaggedData(currentChild);

                              Vector vocabValues = mRulesValidator.getVocabRuleValues(currentChildName, path);

                              result = checkVocabulary(currentChildName, currentChildValue, vocabValues, false)
                                 && result;
                           }
                           else if( dataType.equalsIgnoreCase("decimal") )
                           {
                              // This is a decimal data type
                              // only adlcp:completionThreshold is of this type
                              // and currently all checks are enforced by
                              // the schema. No additional checks needed at
                              // this time.
                           }
                        }
                     } // end ignorning and warning (sub)manifest
                  } // end something
               }

            }
            // remove the xml:base value for this particular element

            if( parentNodeName.equals("manifest") )
            {
               mXMLBase[0][1] = "";
            }
            else if( parentNodeName.equals("resources") )
            {
               mXMLBase[1][1] = "";
            }
            else if( parentNodeName.equals("resource") )
            {
               mXMLBase[2][1] = "";
            }

            break;
         }

         // handle entity reference nodes
         case Node.ENTITY_REFERENCE_NODE:
         {
            break;
         }

         // text
         case Node.COMMENT_NODE:
         {
            break;
         }

         case Node.CDATA_SECTION_NODE:
         {
            break;
         }

         case Node.TEXT_NODE:
         {
            break;
         }
         default:
         {
            // Do nothing - no defined requirements to process any other
            // type of node type
            break;
         }
      }// end switch statement

      mLogger.debug("CPValidator compareToRules()");
      return result;
   }

   /**
    * This method populates the mResourceTable attribute which is responsible
    * for storing an array of files (from the &lt;file&gt; and
    * &lt;dependency&gt; elements) that belong to each resourceID. If a resource
    * does not contain an association of files, then an key will not be added to
    * the hashtable for that resource. This method assists in the checking of
    * the requirement that if a resource is local to a content package, than it
    * must contain an identifical &lt;file&gt; element in association with it.
    * 
    * @param iResourcesNode - The &lt;resources&gt; node
    */
   private void populateResourceTable(Node iResourcesNode)
   {
      //we must manually retrieve the resources xml:base attribute value
      String resourcesXMLBase = DOMTreeUtility.getAttributeValue(iResourcesNode, "base");

      // retrieve all resource nodes from resource parent
      Vector resourceNodes = DOMTreeUtility.getNodes(iResourcesNode, "resource");
      List fileHrefList;
      if( !resourceNodes.isEmpty() )
      {
         Node currentResource;
         int numChildren = resourceNodes.size();
         mLogger.debug("Number of resource children are " + numChildren);

         // loop through each resource and track all files per resource identifer
         for( int t = 0; t < numChildren; t++ )
         {
            fileHrefList = new ArrayList();
            currentResource = (Node)resourceNodes.elementAt(t);
            // resourceID used for hash table key
            String resourceID = DOMTreeUtility.getAttributeValue(currentResource, "identifier");
            
            mLogger.debug("Calling findFiles on resource " + resourceID);

            // call our recursive function here
            // pass in a resource node and expect back an arrayList of files

            List resourcesVisited = new ArrayList();
            findFileHrefs(currentResource, resourcesXMLBase, fileHrefList, resourceNodes, resourcesVisited);

            if( !fileHrefList.isEmpty() )
            {
               mResourceTable.put(resourceID, fileHrefList);
            }
            else
            {
               mLogger.debug("fileHrefList is empty");
            }
         }
      }
   }

   /**
    * This recursive method is used to gather all the files that belong to the
    * &lt;resource&gt; element. The files are tracked from the &lt;file&gt; and
    * &lt;dependency&gt; elements(s).
    * 
    * @param resourceNode &lt;resource&gt; element
    * @param resourcesXMLBase xml:base attribute value of the &lt;resources&gt;
    *           element
    * @param fileHrefList list of files being tracked for the &lt;resource&gt;
    *           element
    * @param resourceNodes vector containing all the &lt;resource&gt; elements
    *           that belong to the &lt;resources&gt; element
    *          
    * @param resourcesVisited list contains the &lt;resource&gt; elements 
    *           tracked for dependency referencing to prevent circular
    *           dependencies.
    */
   private void findFileHrefs(Node resourceNode,
                              String resourcesXMLBase,
                              List fileHrefList,
                              Vector resourceNodes,
                              List resourcesVisited)
   {
      //we must manually retrieve the resource xml:base attribute value
      // the applyXMLBase is not aware of it at this time of processing
      String resourceXMLBase = DOMTreeUtility.getAttributeValue(resourceNode, "base");
      String resourceID = DOMTreeUtility.getAttributeValue(resourceNode, "identifier");
      resourcesVisited.add(resourceID);      

     // add all file elements that are direct children of resource to the array
      Vector fileNodes = DOMTreeUtility.getNodes(resourceNode, "file");

      if( !fileNodes.isEmpty() )
      {
         Node currentFileChild;
         int len = fileNodes.size();
         for( int k = 0; k < len; k++ )
         {
            currentFileChild = (Node)fileNodes.elementAt(k);
            String fileHref = DOMTreeUtility.getAttributeValue(currentFileChild, "href");
            // apply XMLBase to fileHRef add it to a list
            fileHref = mXMLBase[0][1] + resourcesXMLBase + resourceXMLBase + fileHref;
            fileHrefList.add(fileHref);
            mLogger.debug("Added this to fileHrefList " + fileHref);
         }
      }

      // now check for dependency elements and trace to retrieve the file
      // elements
      Vector dependencyNodes = DOMTreeUtility.getNodes(resourceNode, "dependency");
      if( !dependencyNodes.isEmpty() )
      {
         // determine identifierref and add to the hashtable based on this key
         Node currentDependency;
         int len = dependencyNodes.size();
         mLogger.debug("number of dependency is " + len);
         for( int y = 0; y < len; y++ )
         {
            currentDependency = (Node)dependencyNodes.elementAt(y);
            String dependencyIDRef = DOMTreeUtility.getAttributeValue(currentDependency, "identifierref");

            // need to avoid circular dependency here (where a dependency
            // references the resource it belongs to)
            if( !dependencyIDRef.equals(resourceID) )
            {
               // retrieving resource element with identifier that matches
               // identifierref
               Node current;
               String currentResourceID = "";
               Node dependencyMatch = null;
               int numChildren = resourceNodes.size();
              
               for( int k = 0; k < numChildren; k++ )
               {
                  current = (Node)resourceNodes.elementAt(k);
                  
                  currentResourceID = DOMTreeUtility.getAttributeValue(current, "identifier");

                  mLogger.debug("Comparing resource id " + currentResourceID + " to this dependencyIDRef "
                     + dependencyIDRef);
                  
                  if( currentResourceID.equals(dependencyIDRef) )
                  {
                     // first determine if we have a circular dependency before settting the match
                     if(!resourcesVisited.contains(dependencyIDRef))
                     {
                        dependencyMatch = current;
                        break;     
                     }
                     
                  }       
               }
               // retrieve all the file elements of the resource referenced by
               // the dependency and add them to the list if we have a match
               if( dependencyMatch != null )
               {  
                  findFileHrefs(dependencyMatch, resourcesXMLBase, fileHrefList,
                                   resourceNodes, resourcesVisited);
               }
            }
         }
      }
   }

   /**
    * This method checks that if the &lt;resource&gt; is local to the content
    * package, then a &lt;file&gt; element is required that represents the
    * &lt;resource&gt; itself. The href attribute of the &lt;file&gt; element
    * shall be identical to the href attribute of the &lt;resource&gt;
    * 
    * @param iResourceNode - The &lt;resource&gt; element
    * @return boolean - Result of the overall check. True if the checks passed,
    *         false otherwise.
    */
   private boolean checkResourceFileHref(Node iResourceNode)
   {
      boolean result = true;
      String msgText = "";

      // retrive the href and apply xml:base
      String resourceHref = DOMTreeUtility.getAttributeValue(iResourceNode, "href");

      // only continue check if the resourceHref exists
      if( !resourceHref.equals("") )
      {
         //remove all parameters from the end of the href, if they exist

         // first deal with ?
         int questionMarkIndex = resourceHref.indexOf('?');
         if( questionMarkIndex > 0 )
         {
            resourceHref = resourceHref.substring(0, questionMarkIndex);
         }

         // also deal with #
         int poundIndex = resourceHref.indexOf('#');
         if( poundIndex > 0 )
         {
            resourceHref = resourceHref.substring(0, poundIndex);
         }
         
         //we must manually retrieve the resource xml:base attribute value
         // the applyXMLBase is not aware of it at this time of processing
         String resourceXMLBase = DOMTreeUtility.getAttributeValue(iResourceNode, "base");
         
         // resourceID used for logging error messages
         String resourceID = DOMTreeUtility.getAttributeValue(iResourceNode, "identifier");
         mLogger.debug("Checking for this resource now " + resourceID);

         // apply XML Base to the href if it exists
         resourceHref = mXMLBase[0][1] + mXMLBase[1][1] + resourceXMLBase + resourceHref;
         int len = resourceHref.length();

         // only need to perform this check on local resource
         if( ( (len > 4) && (!resourceHref.substring(0, 5).equals("http:")) ) & 
             ( (len > 5) && (!resourceHref.substring(0, 6).equals("https:")) ) & 
             ( (len > 3) && (!resourceHref.substring(0, 4).equals("ftp:")) ) & 
             ( (len > 4) && (!resourceHref.substring(0, 5).equals("ftps:")) ) 
           )
         {
            // we have a resource that is local to the package, must have a file
            // href that is identifical to it

            ArrayList files = (ArrayList)mResourceTable.get(resourceID);

            if( files != null )
            {
               if( !files.contains(resourceHref) )
               {
                  // the resourceHref does not contain a matching file href when
                  // it should
                  result = false;

                  msgText = Messages.getString("CPValidator.628", resourceID);
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
               }
            }
            else
            {
               // there are no files that exist for the local resource
               result = false;

               msgText = Messages.getString("CPValidator.628", resourceID);
               mLogger.debug("FAILED: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            }
         }
      }
      return result;
   }

   /**
    * This method assists with the application profile check for the validation
    * of the resource attributes.
    * 
    * @param iResourceNode The &lt;resources&gt; element
    * @param iAttrList - The list of resource attributes.
    * @return boolean - result of the overall check. True if the checks passed,
    *         false otherwise.
    */
   private boolean checkResourceAttributes(Node iResourceNode, NamedNodeMap iAttrList)
   {
      mLogger.debug("CPValidator checkResourceAttributes");

      int idMultiplicityUsed = -1;
      int typeMultiplicityUsed = -1;
      int scormMultiplicityUsed = -1;
      int hrefMultiplicityUsed = -1;

      String msgText;
      boolean result = true;

      // check for mandatory attributes

      idMultiplicityUsed = getMultiplicityUsed(iAttrList, "identifier");
      if( idMultiplicityUsed < 1 )
      {
         msgText = Messages.getString("CPValidator.198", "identifier");
         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

         result = false && result;
      }

      typeMultiplicityUsed = getMultiplicityUsed(iAttrList, "type");

      if( typeMultiplicityUsed < 1 )
      {
         msgText = Messages.getString("CPValidator.198", "type");
         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

         result = false && result;
      }

      scormMultiplicityUsed = getMultiplicityUsed(iAttrList, "scormType");
      if( scormMultiplicityUsed < 1 )
      {
         msgText = Messages.getString("CPValidator.198", "scormType");
         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

         result = false && result;
      }

      // special rules only apply to content aggregation application profile
      if( mRulesValidator.getApplicationProfile().equals("contentaggregation") )
      {
         // special checks to be enforced when an <item> references a <resource>

         // retrieve resource.identifier value and compare to idref values in
         // the valid mValidIdrefs vector. If a match is found, it is assumed
         // that the special checks should be enforced.

         String resourceID = DOMTreeUtility.getAttributeValue(iResourceNode, "identifier");
         boolean referencesAResource = false;

         if( !resourceID.equals("") )
         {
            // loop through mValidIdrefs to find a matching reference

            for( int i = 0; i < mValidIdrefs.size(); i++ )
            {
               String currentIdref = (String)mValidIdrefs.elementAt(i);

               if( resourceID.equals(currentIdref) )
               {
                  referencesAResource = true;
               }
            }

            if( referencesAResource )
            {
               // (1) href is mandatory when referenced by an item

               hrefMultiplicityUsed = getMultiplicityUsed(iAttrList, "href");

               if( hrefMultiplicityUsed < 1 )
               {
                  msgText = Messages.getString("CPValidator.431");
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                  result = false && result;
               }

               // (2) type attribute shall be set to "webcontent" when
               // referenced by an item

               String typeValue = DOMTreeUtility.getAttributeValue(iResourceNode, "type");

               if( !typeValue.equals("webcontent") )
               {
                  msgText = Messages.getString("CPValidator.2");
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

                  result = false && result;
               }
            }
         }
      }
      return result;
   }

   /**
    * This method assists with the application profile check for the validation
    * of the bucket attributes. <br>
    * 
    * @param iBucketNode The resources node <br>
    */
   private void checkBucketAttributes(Node iBucketNode)
   {
      mLogger.debug("CPValidator checkBucketAttributes");

      String msgText;
      boolean foundValidChar = false;

      NamedNodeMap attrList = iBucketNode.getAttributes();
      int numAttr = attrList.getLength();

      Attr currentAttrNode;
      String currentAttrName;
      String attributeValue = null;

      // find the bucketID and bucketType attributes

      for( int i = 0; i < numAttr; i++ )
      {
         currentAttrNode = (Attr)attrList.item(i);
         currentAttrName = currentAttrNode.getLocalName();

         // Check bucketID and bucketType Attribute Values for nothing but
         // white space

         if( currentAttrName.equals("bucketID") || currentAttrName.equals("bucketType") )
         {
            // Find the length of bucket attribute and check if its all
            // whitespace
            attributeValue = currentAttrNode.getValue();

            for( int j = 0; j < attributeValue.length(); j++ )
            {
               char tempChar = attributeValue.charAt(j);

               if( !( Character.isWhitespace(tempChar) ) )
               {
                  foundValidChar = true;
                  break;
               }
            }
            if( !foundValidChar )
            {
               msgText = "Attribute \"" + currentAttrName + "\" must "
                  + "contain valid characters, all whitespace found.";
               mLogger.debug("SSP: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            }

            foundValidChar = false;

         }
      }
   }

   /**
    * This method assists with the application profile check for the validation
    * of the bucket attributes. <br>
    * 
    * @param iResourceNode The resources node <br>
    */
   private void checkBucketUniqueness(Node iResourceNode)
   {
      mLogger.debug("CPValidator checkBucketUniqueness");

      String msgText;
      List idList = new ArrayList();

      NodeList childrenOfItem = iResourceNode.getChildNodes();

      if( childrenOfItem != null )
      {
         Node currentChild;
         String currentChildName;
         int len = childrenOfItem.getLength();

         for( int k = 0; k < len; k++ )
         {
            currentChild = childrenOfItem.item(k);
            currentChildName = currentChild.getLocalName();

            if( currentChildName.equals("bucket") )
            {
               // Get the bucketID attribute

               String bucketId = DOMTreeUtility.getAttributeValue(currentChild, "bucketID");
               // Check if id already exists in the list: if it does set the
               // flag
               if( idList.contains(bucketId) )
               {
                  // ERROR, duplicate ID exists in resource
                  msgText = "BucketID \"" + bucketId + "\" must be unqiue" + " for a <resource>.";
                  mLogger.debug("SSP: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

               }
               else
               {
                  idList.add(bucketId);
               }
            }

         }

      }
   }

   /**
    * This method assists with the application profile check for the validation
    * of the bucket attributes. <br>
    * 
    * @param iSizeNode The resources node <br>
    */
   private void checkSizeAttributes(Node iSizeNode)
   {
      mLogger.debug("CPValidator checkSizeAttributes");

      String msgText;

      NamedNodeMap attrList = iSizeNode.getAttributes();
      int numAttr = attrList.getLength();

      Attr currentAttrNode;
      String currentAttrName;
      String attributeValue = null;
      int minVal = 0;

      // find the minimum and requested attributes

      for( int i = 0; i < numAttr; i++ )
      {
         currentAttrNode = (Attr)attrList.item(i);
         currentAttrName = currentAttrNode.getLocalName();

         // Check bucketID and bucketType Attribute Values for even numbers

         if( currentAttrName.equals("minimum") || currentAttrName.equals("requested") )
         {
            // Get the value and check if it is a valid even integer
            attributeValue = currentAttrNode.getValue();

            // Assume the value is valid
            boolean valid = true;

            if( attributeValue == null )
            {
               // A null value can never be valid
               valid = false;
            }

            try
            {
               int value = Integer.parseInt(attributeValue, 10);

               if( value < minVal || ( value % 2 ) != 0 )
               {
                  valid = false;
               }
            }
            catch( Exception e )
            {
               valid = false;
            }

            if( !valid )
            {
               msgText = "Size Attribute \"" + currentAttrName + "\" must " + "contain a valid even integer value.";
               mLogger.debug("SSP: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            }

         }
      }
   }

   /**
    * This method assists with the application profile check for the validation
    * with of items. It first checks the identifierref attribute of an item for
    * existance on non-leaf items. It than checks the title multiplcity, as a
    * title is not permitted on an item that references a (sub)manifest.
    * 
    * @param iOrgNode The organizations node
    * @param iManifestInfo - the populated ManifestMap object
    * @return boolean - result of the organization multiplicity check. True if
    *         the href checks passed, false otherwise.
    */
   private boolean checkItem(Node iOrgNode, ManifestMap iManifestInfo)
   {
      mLogger.debug("CPValidator checkItem");

      boolean result = true;

      result = checkItemIdentifierRef(iOrgNode) && result;
      result = checkItemChildMultiplicity(iOrgNode, iManifestInfo) && result;

      return result;
   }

   /**
    * This method assists with the application profile check for validation that
    * a sco resource is referenced by an item identifierref.
    * 
    * @param idrefValue - the idrentifierref value to be matched to a sco
    *           resource.
    * @return boolean - result of the reference to sco check.
    */
   private boolean checkForReferenceToSco(String idrefValue)
   {
      mLogger.debug("CPValidator checkForReferenceToSco");
      mLogger.debug("Input Identifierref: " + idrefValue);

      boolean result = true;
      int len = mResourceNodes.size();
      String id;
      String type;
      String msgText;

      for( int i = 0; i < len; i++ )
      {
         Node currentResource = (Node)mResourceNodes.get(i);
         id = DOMTreeUtility.getAttributeValue(currentResource, "identifier");
         mLogger.debug("Identifier of <resource> #" + i + " is: " + id);

         if( id.equals(idrefValue) )
         {
            // we have a matching reference
            // now check scormType and error if not sco

            type = DOMTreeUtility.getAttributeValue(currentResource, "scormType");

            mLogger.debug("SCORM Type of <resource> #" + i + " is: " + type);

            if( !type.equalsIgnoreCase("sco") )
            {
               result = false;

               msgText = Messages.getString("CPValidator.452", idrefValue);

               mLogger.debug("FAILED: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            }
         }
      }
      return result;
   }

   /**
    * This method checks the item to ensure that the identifierref attribute is
    * not used on a non-leaf item. This method also checks to ensure that a leaf
    * item shall reference a resource. A leaf item fails if it contains no
    * identifierref attribute at all, or if it contains an identifierref
    * attribute that is set to an empty string.
    * 
    * @param iOrgNode - the organization node containing the item element(s)
    * @return boolean - result of the check for the item identifierref attribute
    *         True if the identifierref passes, false otherwise.
    */
   private boolean checkItemIdentifierRef(Node iOrgNode)
   {

      mLogger.debug("CPValidator checkItemIdentifierRef");

      String msgText = "";
      NodeList orgChildren = iOrgNode.getChildNodes();
      int orgChildSize = orgChildren.getLength();
      Node currentNode;
      String currentName;
      boolean result = true;

      for( int j = 0; j < orgChildSize; j++ )
      {
         currentNode = orgChildren.item(j);
         currentName = currentNode.getLocalName();

         if( currentName.equals("item") )
         {
            NodeList itemChildren = currentNode.getChildNodes();
            int itemChildrenSize = itemChildren.getLength();
            boolean itemHasItemChildren = false;

            for( int k = 0; k < itemChildrenSize; k++ )
            {

               // see if we have a child item of item
               // if so, this signals that the currentNode is not a leaf and
               // should not have an identifierref

               Node currentItemChild = itemChildren.item(k);
               String currentItemChildName = currentItemChild.getLocalName();

               if( currentItemChildName != null )
               {

                  if( currentItemChildName.equals("item") )
                  {

                     NamedNodeMap attrList = currentNode.getAttributes();
                     int numAttr = attrList.getLength();
                     Attr currentAttrNode = null;
                     String currentNodeName = "";

                     for( int i = 0; i < numAttr; i++ )
                     {
                        currentAttrNode = (Attr)attrList.item(i);
                        currentNodeName = currentAttrNode.getLocalName();

                        if( currentNodeName.equals("identifierref") )
                        {
                           result = result && false;
                           msgText = Messages.getString("CPValidator.461");
                           mLogger.debug("FAILED: " + msgText);
                           DetailedLogMessageCollection.getInstance().addMessage(
                              new LogMessage(MessageType.FAILED, msgText));

                        }// end if ( currentNodeName.equals("identifierref") )

                     }// end for
                     // set the flag that signals that the item is NOT a leaf
                     // item
                     itemHasItemChildren = true;
                  }// end if ( currentItemChildName.equals("item") )
               }
            }
            // need to check if we are dealing with a leaf item
            if( !itemHasItemChildren )
            {
               // Verify that a leaf item references a resource
               Attr identifierRef = DOMTreeUtility.getAttribute(currentNode, "identifierref");
               if( identifierRef == null )
               {
                  // ERROR, must reference a resource therefore it must contain
                  // an identifierref attribute
                  result = result && false;
                  msgText = Messages.getString("CPValidator.464");
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
               }
               else
               {
                  String identifierRefValue = identifierRef.getValue();
                  if( identifierRefValue.equals("") )
                  {
                     // ERROR, must reference a resource therefore it cannot
                     // contain an identifierref attibute that is set to an
                     // empty string
                     result = result && false;
                     msgText = Messages.getString("CPValidator.467");
                     mLogger.debug("FAILED: " + msgText);
                     DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
                  }
               }

            }
         }
      }
      return result;
   }

   /**
    * This method validates the multiplicity of the children of the item
    * element. The title element is required to be present.
    * 
    * @param iNode - the organization element
    * @param iManifestInfo - the populated ManifestMap object
    * @return boolean - result of the title multiplicity check. True implies
    *         that the title multiplicity was properly adhered to, false implies
    *         otherwise.
    */
   private boolean checkItemChildMultiplicity(Node iNode, ManifestMap iManifestInfo)
   {
      mLogger.debug("CPValidator checkItemChildMultiplicity");

      String msgText = "";
      boolean result = true;

      String iNodeName = iNode.getLocalName();

      if( iNodeName.equals("organization") )
      {
         NodeList iNodeChildren = iNode.getChildNodes();
         int iNodeChildSize = iNodeChildren.getLength();

         Node currentNode;
         String currentName;

         for( int j = 0; j < iNodeChildSize; j++ )
         {
            currentNode = iNodeChildren.item(j);
            currentName = currentNode.getLocalName();

            if( currentName.equals("item") )
            {
               // search for item element and recurse
               result = checkItemChildMultiplicity(currentNode, iManifestInfo) && result;
            }
         }
      }

      if( iNodeName.equals("item") )
      {

         NodeList itemChild = iNode.getChildNodes();
         int itemSize = itemChild.getLength();
         String currentItemChildName = "";
         boolean titleFound = false;

         for( int v = 0; v < itemSize; v++ )
         {
            Node currentItemChild = itemChild.item(v);
            currentItemChildName = currentItemChild.getLocalName();

            if( currentItemChildName != null )
            {
               if( currentItemChildName.equals("title") )
               {
                  titleFound = true;
               }
               else if( currentItemChildName.equals("item") )
               {
                  result = checkItemChildMultiplicity(currentItemChild, iManifestInfo) && result;
               }
            }
         }
         // title is mandatory
         if( !titleFound )
         {
            msgText = Messages.getString("CPValidator.287", "title");
            mLogger.debug("FAILED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

            result = false && result;
         }
      }

      return result;
   }

   /**
    * This method assists with the application profile check for the validation
    * of the href attribute(s). This method attempts to verify that the href
    * values can be detected.
    * 
    * @param iURIString The URI value of the href attribute
    * @return boolean - result of the href check. True if the href checks
    *         passed, false otherwise.
    */
   private boolean checkHref(String iURIString)
   {
      mLogger.debug("CPValidator checkHref()");
      mLogger.debug("iURISting is " + iURIString);

      boolean result = true;
      String msgText = "";

      if( !( iURIString.equals("") ) )
      {
         // check for a valid protocol

         if( ( (iURIString.length() > 4) && (iURIString.substring(0, 5).equals("http:")) ) || 
             ( (iURIString.length() > 5) && (iURIString.substring(0, 6).equals("https:")) ) ||
             ( (iURIString.length() > 3) && (iURIString.substring(0, 4).equals("ftp:")) ) || 
             ( (iURIString.length() > 4) && (iURIString.substring(0, 5).equals("ftps:")) ) )
         {
            // This is an external SCO
            try
            {
               URL url = new URL(iURIString);
               URLConnection urlConn = url.openConnection();
               HttpURLConnection httpUrlConn = (HttpURLConnection)urlConn;
               int code = httpUrlConn.getResponseCode();
   
               // try to access the address
               if( code == 200 )
               {
                  msgText = Messages.getString("CPValidator.502", iURIString);
                  mLogger.debug("PASSED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
               }
               else
               {
                  msgText = Messages.getString("CPValidator.505", iURIString);
                  mLogger.debug("FAILED: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
                  result = false;
               }
            }
            catch( MalformedURLException mfue )
            {
               mLogger.debug("MalformedURLException thrown when creating " + "URL with \"" + iURIString + "\"");
               msgText = Messages.getString("CPValidator.514", iURIString);
               mLogger.debug("FAILED: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
               result = false;
            }
            catch( IOException ioe )
            {
               mLogger.debug("IOException thrown when opening a connection " + "to \"" + iURIString + "\"");
               msgText = Messages.getString("CPValidator.520", iURIString);
               mLogger.debug("FAILED: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
               result = false;
            }
         }
         else if( iURIString.substring(0, 5).equals("file:") )
         {
            // This is the local file system
            msgText = Messages.getString("CPValidator.524", iURIString);
            mLogger.debug("FAILED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            result = false;
         }
         else
         {
            // Check the local URLs
            result = checkLocalURL(iURIString) && result; 
         }
      }
      return result;
   }
   
   /**
    * 
    * @param iURIString the URI passed in
    * @return boolean - result of the local URL check. True if the local
    *                 checks passed, false otherwise.
    */
   private boolean checkLocalURL( String iURIString )
   {
      boolean result = true;
      String msgText = "";
      
      if( iURIString.charAt(0) == '/' )
      {
         // This is referencing the users home directory

         msgText = Messages.getString("CPValidator.527", iURIString);
         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
         result = false;
      }
      else
      {
         String absolutePath = getBaseDirectory() + iURIString;
         mLogger.debug("Absolute path is " + absolutePath);

         // strip off the query string and parameters
         int queryIndex = absolutePath.indexOf('?');
         if( queryIndex > 0 )
         {
            absolutePath = absolutePath.substring(0, queryIndex);
         }

         // strip off the fragment string and parameters
         int fragmentIndex = absolutePath.indexOf('#');
         if( fragmentIndex > 0 )
         {
            absolutePath = absolutePath.substring(0, fragmentIndex);
         }
         
         // after the parameters have been stripped off we want to check to see if
         // this URI is different from any others by case (upper/lower)
         
         // if the absolutePath is in the list then we fall through, if it isn't then
         // we check to see if it is in there but with a difference in case
         if(!mHrefCaseSensitiveList.contains(absolutePath))
         {
            // the href is not in the list in this case, now check to see
            // if it is in there in a different case
            if(mHrefCaseSensitiveList.containsIgnoreCase(absolutePath))
            {
               // raise a warning because the href is in the list but with 
               // a difference in case
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(
                  MessageType.WARNING, Messages.getString("CPValidator.528", iURIString)));
            }
            else
            {
               // it's not in the list at all, so add it for future reference     
               mHrefCaseSensitiveList.add(absolutePath);
            }
         }

         // decode any encrypted URL syntax
         try
         { 	 
             absolutePath = URLDecoder.decode(absolutePath, "UTF-8");             
         }
         catch( UnsupportedEncodingException uee )
         {
            mLogger.error("UnsupportedEncodingException thrown while " + "decoding the file path.");
            uee.printStackTrace();
         }

         // try to access the file
         try
         {
            File fileToFind = new File(absolutePath);
            if( fileToFind.isFile() )
            {
               msgText = Messages.getString("CPValidator.534", iURIString);
               mLogger.debug("PASSED: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));

               // This file has been physically located, remove from the
               //  list...it's NOT excess baggage
               String tempStr = fileToFind.getPath();
               tempStr = tempStr.substring(tempStr.indexOf("PackageImport\\") + 14, tempStr.length());

               // we have to switch the direction of the slashes to
               // correspond with those in the
               //  mFileList (had to originally change those because
               // backwards slashes can't exist
               // in the manifest
               tempStr = tempStr.replace('\\', '/');
               mFileList.remove(tempStr);
            }
            else
            {
               msgText = Messages.getString("CPValidator.537", iURIString);
               mLogger.debug("FAILED: " + msgText);
               DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
               result = false;
            }
         }
         catch( NullPointerException npe )
         {
            mLogger.error("NullPointerException thrown when accessing " + absolutePath);
            msgText = Messages.getString("CPValidator.537", iURIString);
            mLogger.debug("FAILED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            result = false;
         }
         catch( SecurityException se )
         {
            mLogger.error("SecurityException thrown when accessing " + absolutePath);
            msgText = Messages.getString("CPValidator.537", iURIString);
            mLogger.debug("FAILED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
            result = false;
         }
      }
      return result;
   }

   /**
    * This method assists with the application profile check of the smallest
    * permitted maximums. The smallest permitted maximum value of an element
    * describes the maximum number of characters that a system that is going to
    * process that data at a minimum must support.
    * 
    * @param iElementName Name of the element being checked for spm
    * @param iElementValue value being checked for smp
    * @param iSPMRule value allowed for spm ( value retrieved from rules )
    * @param iAmAnAttribute flags determines if its an attribute (true), or an
    *           element that is being validated for valid vocabulary tokens.
    * @return - boolean result of spm check. True if the spm checks passed,
    *         false otherwise.
    */
   private boolean checkSPMConformance(String iElementName, String iElementValue, int iSPMRule, boolean iAmAnAttribute)
   {
      boolean result = true;
      String msgText = "";

      int elementValueLength = iElementValue.length();

      if( iSPMRule != -1 )
      {
         if( elementValueLength > iSPMRule )
         {
            if( iAmAnAttribute )
            {
               if( iElementName.equals("base") )
               {
                  msgText = Messages.getString("CPValidator.552", "xml:base", iSPMRule);
               }
               else if( iElementName.equals("href") && doesXMLBaseExist() )
               {
                  String replace = Integer.toString(iSPMRule);
                  msgText = Messages.getString("CPValidator.553", "xml:base", "href", replace);
               }
               else
               {
                  msgText = Messages.getString("CPValidator.552", iElementName, iSPMRule);
               }
            }
            else
            {
               msgText = Messages.getString("CPValidator.555", iElementName, iSPMRule);
            }

            mLogger.debug("WARNING: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
         }
         else if( elementValueLength < 1 )
         {
            if( iAmAnAttribute )
            {
               msgText = Messages.getString("CPValidator.559", iElementName);
            }
            else
            {
               msgText = Messages.getString("CPValidator.561", iElementName);
            }

            mLogger.debug("FAILED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

            result = false;
         }
         else
         {
            if( iAmAnAttribute )
            {
               if( iElementName.equals("base") )
               {
                  msgText = Messages.getString("CPValidator.564", "xml:base");
               }
               else
               {
                  msgText = Messages.getString("CPValidator.564", iElementName);
               }
            }
            else
            {
               msgText = Messages.getString("CPValidator.566", iElementName);
            }

            mLogger.debug("PASSED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
         }
      }
      else if( elementValueLength < 1 )
      {
         if( iAmAnAttribute )
         {
            msgText = Messages.getString("CPValidator.559", iElementName);
         }
         else
         {
            msgText = Messages.getString("CPValidator.561", iElementName);
         }
         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

         result = false;
      }
      else
      {
         if( iAmAnAttribute )
         {
            msgText = Messages.getString("CPValidator.564", iElementName);
            if( iElementName.equals("base") )
            {
               msgText = Messages.getString("CPValidator.564", "xml:base");

            }
            else if( iElementName.equals("href") )
            {
               msgText = Messages.getString("CPValidator.565", "xml:base", "href");
            }
            else
            {
               msgText = Messages.getString("CPValidator.564", iElementName);
            }
         }
         else
         {
            msgText = Messages.getString("CPValidator.566", iElementName);
         }

         mLogger.debug("PASSED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
      }

      return result;
   }

   /**
    * This method assists with the application profile check for valid
    * vocabularies. The vocabulary value is compared to those defined by the
    * application profile rules. It is assumed that only 1 vocabulary token may
    * exist for an element/attribute
    * 
    * @param iName Name of the element/attribute being checked for valid
    *           vocabulary.
    * @param iValue Vocabulary string value that exists for the
    *           element/attribute in the test subject
    * @param iVocabValues Vector containing a list of the valid vocabulary
    *           values for the element/attribute.
    * @param iAmAnAttribute Flags determines if its an attribute (true), or an
    *           element that is being validated for valid vocabulary tokens.
    * @return boolean - true if the value is a valid vocab token, false
    *         otherwise
    */
   private boolean checkVocabulary(String iName, String iValue, Vector iVocabValues, boolean iAmAnAttribute)
   {
      mLogger.debug("CPValidator checkVocabulary()");
      
      boolean result = false;
      String msgText;

      // loop through the valid vocabulary vector to see if the
      // attribute value matches a valid token

      for( int i = 0; i < iVocabValues.size(); i++ )
      {
         if( iValue.equals(iVocabValues.elementAt(i)) )
         {
            result = true;
            
            // special warning produced if we find 2nd Edition scorm token
            if(iName.equals("schemaversion"))
            {
               if (iVocabValues.elementAt(i).equals("CAM 1.3"))   
               {
                  msgText = Messages.getString("CPValidator.630", "CAM 1.3", "2004 3rd Edition");
                  mLogger.debug("WARNING: " + msgText);
                  DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));
               }
            }
         }
      }

      if( result )
      {
         if( iAmAnAttribute )
         {
            msgText = Messages.getString("CPValidator.581", iValue, iName);
         }
         else
         {
            msgText = Messages.getString("CPValidator.584", iValue, iName);
         }

         mLogger.debug("PASSED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.PASSED, msgText));
      }
      else
      {
         if( iAmAnAttribute )
         {
            msgText = Messages.getString("CPValidator.588", iValue, iName);
         }
         else
         {
            msgText = Messages.getString("CPValidator.592", iValue, iName);
         }

         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));
      }
      mLogger.debug("CPValidator checkVocabulary()");

      return result;
   }

   /**
    * This method assists with the application profile check for the
    * multiplicity of elements. This method returns the number of times the
    * element was detected based on the given element name and the given parent
    * node of that element name.
    * 
    * @param iParentNode The parent node of the element being searched
    * @param iNodeName The name of the element being searched for
    * @return - int: number of instances of a given element
    */
   public int getMultiplicityUsed(Node iParentNode, String iNodeName)
   {
      mLogger.debug("CPValidator getMultiplicityUsed() - Elements");
      mLogger.debug("Input Parent Node: " + iParentNode.getLocalName());
      mLogger.debug("Input Node we are looking for: " + iNodeName);

      // Need a list to find how many kids to cycle through
      NodeList kids = iParentNode.getChildNodes();
      int count = 0;

      int kidsLength = kids.getLength();
      for( int i = 0; i < kidsLength; i++ )
      {
         if( kids.item(i).getNodeType() == Node.ELEMENT_NODE )
         {
            String currentNodeName = kids.item(i).getLocalName();
            //String currentNodeNamespace = kids.item(i).getNamespaceURI();

            if( currentNodeName.equals(iNodeName) )
            {
               count++;
            } // end if the current node name equals the name we are looking for
         } // end of the node type is ELEMENT_NODE
      } // end looping over children

      mLogger.debug("The " + iNodeName + ", appeared " + count + " times.");

      return count;
   }

   /**
    * This method assists with the application profile check for the
    * multiplicity of attributes. This method returns the number of times the
    * attribue was detected based on the given attribute name and the given
    * parent node of that element name.
    * 
    * @param iAttributeMap A list of attributes
    * @param iNodeName The name of the element being searched for
    * @return - int: number of instances of a given attribute
    */
   public int getMultiplicityUsed(NamedNodeMap iAttributeMap, String iNodeName)
   {
      mLogger.debug("CPValidator getMultiplicityUsed() - Attributes");
      mLogger.debug("Input Node we are looking for: " + iNodeName);

      int result = 0;
      int length = iAttributeMap.getLength();
      String currentName;

      for( int i = 0; i < length; i++ )
      {
         currentName = ( (Attr)iAttributeMap.item(i) ).getLocalName();

         if( currentName.equals(iNodeName) )
         {

            result++;

         } // end if current name equals node name
      } // end looping over attributes

      mLogger.debug("The " + iNodeName + ", appeared " + result + " times.");

      return result;
   }

   /**
    * Returns the mBaseDirectory attribute that contains the base directory of
    * where the test subject is located. It is used by the validator to
    * determine the location of the package resources, including the
    * imsmanifest, sco's, and/or metadata.
    * 
    * @return String that contains the path of the base directory
    */
   private String getBaseDirectory()
   {

      return mBaseDirectory;
   }

   /**
    * This method determines if xml:base is declared in the IMS Manifest.
    * 
    * @return boolean describing if xml:base is declared. True implies that
    *         xml:base does exist within the IMS Manifest, false implies that
    *         xml:base was not declared in the IMS Manifest.
    */
   private boolean doesXMLBaseExist()
   {
      boolean xmlBaseExists = true;

      // determine if the xml:base array contains values
      if( ( mXMLBase[0][1].equals("") ) && ( mXMLBase[1][1].equals("") ) && ( mXMLBase[2][1].equals("") ) )
      {
         // xml:base was not declared for a manifest, resources, or resource
         // element
         xmlBaseExists = false;
      }

      return xmlBaseExists;
   }

   /**
    * This method builds the XML base value that is to be pre-appended to the
    * href attribute values prior to any attempts to located the href values
    * locations.
    * 
    * @param iHrefValue href value to apply xml:base values to
    * @return String that contains the href value pre-appended with the xml:base
    */
   private String applyXMLBase(String iHrefValue)
   {

      mLogger.debug("mXMLBase[0][1]: " + mXMLBase[0][1]);
      mLogger.debug("mXMLBase[1][1]: " + mXMLBase[1][1]);
      mLogger.debug("mXMLBase[2][1]: " + mXMLBase[2][1]);
      mLogger.debug("href: " + iHrefValue);
      return mXMLBase[0][1] + mXMLBase[1][1] + mXMLBase[2][1] + iHrefValue;

   }

   /**
    * This method retrives the launch data from the manifestHandler object. The
    * launch data returned contains information for the launching of SCOs found
    * within the content package test subject. This method is only called if the
    * user chooses to validate SCOs.
    * 
    * @param iDefaultOrganizationOnly A boolean representing whether or not
    *           launch data should be collected from the default organization
    *           only.
    * @param iRemoveAssets A boolean representing whether or not assets should
    *           be removed in the LaunchData list.
    * @return Vector: Containing the launch information
    */
   public Vector getLaunchData(boolean iDefaultOrganizationOnly, boolean iRemoveAssets)
   {
      return mManifestHandler.getLaunchData(( super.getDocument() ).getDocumentElement(), iDefaultOrganizationOnly,
         iRemoveAssets);
   }

   /**
    * This method accesses the manifest handler to extract all SCO launch
    * information
    * 
    * @param iRolledUpDocument - Dom with manifest rollup
    * @param iDefaultOrganizationOnly - boolean describing which organization to
    *           read from
    * @param iRemoveAssets - boolean describing if to include assets in launch
    *           locatation
    * @return Returns a vector of launch data
    */
   public Vector getLaunchData(Document iRolledUpDocument, boolean iDefaultOrganizationOnly, boolean iRemoveAssets)
   {
      return mManifestHandler.getLaunchData(iRolledUpDocument.getDocumentElement(), iDefaultOrganizationOnly,
         iRemoveAssets);
   }

   /**
    * This method retrives the metadata information from the manifestHandler
    * object. The metadata information returned contains information for the
    * validation of the metadata found within the content package test subject.
    * This method is only called if the user chooses to validate metadata.
    * 
    * @return Vector: Containing the metadata launch information
    */
   public Vector getMetadataDataList()
   {

      return mManifestHandler.getMetadata(( super.getDocument() ).getDocumentElement(), mBaseDirectory);
   }

   /**
    * This method is used to turn full content package validation off -
    * including required files check, validation to the schema, and application
    * profile checks. Turning full validation off allows only a parse for
    * well-formedness to be preformed.
    * 
    * @param iValue True implies to parse for well-formedness and validation to
    *           the schema, false implies to parse for well-formedness only.
    */
   public void setPerformValidationToSchema(boolean iValue)
   {
      mPerformFullValidation = iValue;
   }

   /**
    * This method checks the values of xml:base, href, and adlcp:location to
    * ensure that they DO NOT begin with a slash '/' It also checks xml:base for
    * the required trailing slash '/'
    * 
    * @param iName Contains the name of the attribute/element being passed in
    * @param iValue Contains the value of the attribute/element being passed in
    * @return boolean: True if xml:base contains a trailing slash '/' and/or if
    *         href, xml:base, and location DO NOT contain leading slashes.
    */
   private boolean checkForSlashes(String iName, String iValue)
   {
      mLogger.debug("CPValidator checkForSlashes()");
      mLogger.debug("Name: " + iName);
      mLogger.debug("Value: " + iValue);

      String msgText = "";
      boolean result = true;

      // Getting the last character in the string
      char tempChar = iValue.charAt(iValue.length() - 1);

      // if the name is xml:base, check to make sure the last character is "\"
      if( iName.equals("xml:base") )
      {
         if( tempChar != '/' )
         {
            msgText = Messages.getString("CPValidator.624", iName);
            mLogger.debug("FAILED: " + msgText);
            DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

            result &= false;
         }
      }

      // gets the first character in the string
      tempChar = iValue.charAt(0);
      // make sure the first character IS NOT a "\"
      if( tempChar == File.separatorChar || tempChar == '/' )
      {
         msgText = Messages.getString("CPValidator.627", iName);
         mLogger.debug("FAILED: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.FAILED, msgText));

         result = false && result;
      }
      return result;

   } // end checkForSlashes()

   /**
    * This method checks all files and folders in a content package and creates
    * a list of the files. This list is used to determine if any files are
    * present and not referenced in the imsmanifest.xml file. <br>
    * <br>
    * 
    * @param rootDirectory A string containing the path of the root directory of
    *           the content package contents.
    */
   private void createManifestFileList(String rootDirectory)
   {

      File fileOrFolder = new File(rootDirectory);

      // This will hold a list of all files and folders in this directory
      File[] filesInDirectory;

      // if this is a directory we need to drill down to all other folders and
      //   files to make sure we account for everything
      if( fileOrFolder.isDirectory() && !fileOrFolder.getName().equals("common")
         && !fileOrFolder.getName().equals("vocab") && !fileOrFolder.getName().equals("extend")
         && !fileOrFolder.getName().equals("unique") )
      {
         filesInDirectory = fileOrFolder.listFiles();

         int fileCount = 0;

         // recursive call to drill down to all included levels of the
         //  root directory
         while( fileCount < filesInDirectory.length )
         {
            createManifestFileList(filesInDirectory[fileCount].getPath());
            fileCount++;
         }// end while

      }// end if

      // if it is a file and it isn't an xsd or the manifest add it to the list
      if( fileOrFolder.isFile() && !fileOrFolder.getName().endsWith(".xsd")
         && !fileOrFolder.getName().equals("imsmanifest.xml") && !fileOrFolder.getName().endsWith(".dtd") )
      {
         String tempStr = fileOrFolder.getPath();

         tempStr = tempStr.substring(tempStr.indexOf("PackageImport\\") + 14, tempStr.length());

         tempStr = tempStr.replace('\\', '/');
         // if the filename is already in the List don't add it
         if( !mFileList.contains(tempStr) )
         {
            mFileList.add(tempStr);
         }

      }
   }// end createManifestmFileList()

   /**
    * This method checks a list of files created from those listed in the
    * imsmanifest.xml file. As files are physically located they are removed
    * from this list. After the package has been completely validated this
    * method checks the list to determine if any files are still there. If they
    * are still in the list then they exist in the package but are not
    * referenced in the imsmanifest.xml file. Therefore they are determined to
    * be extaneous files that do not need to be included in the package. <br>
    * 
    * @param iApplicationProfileType if the application type is content
    *           aggregation then we have additional checks to make
    */
   private void checkForExcessBaggage(String iApplicationProfileType)
   {
      String msgText = "";
      if( mFileList.size() > 0 )
      {
         // If there is only one file in the list
         if( mFileList.size() == 1 )
         {
            msgText = "All files included in the content package should "
               + "be declared and referenced in the manifest when " + "interchanging packages.  The following file is "
               + "contained in the content package but was not " + "referenced in the manifest.  It appears that this "
               + "file may be extraneous.  Appropriate action may " + "need to be taken to correct this problem";
         }
         // If there is more than one file in the list
         else
         {
            msgText = "All files included in the content package should "
               + "be declared and referenced in the manifest when "
               + "interchanging packages.  The following files are " + "contained in the content package but were not "
               + "referenced in the manifest.  It appears that these "
               + "files may be extraneous.  Appropriate action may " + "need to be taken to correct this problem:";
         }

         mLogger.debug("WARNING: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

         // we've displayed the warning, now list the files creating the warning
         for( int i = 0; i < mFileList.size(); i++ )
         {
            mLogger.debug((String)mFileList.get(i));
            DetailedLogMessageCollection.getInstance().addMessage(
               new LogMessage(MessageType.OTHER, (String)mFileList.get(i)));
         }

      }// end if

      msgText = "";

      mManifestResourceIdentifierList.removeAll(mAllIdrefsList);

      // check to see if there are any dangling resources in the list
      if( mManifestResourceIdentifierList.size() > 0 && iApplicationProfileType.equals("contentaggregation") )
      {
         if( mManifestResourceIdentifierList.size() == 1 )
         {

            msgText = "All files included in the content package should "
               + "be declared and referenced in the manifest when "
               + "interchanging packages.  The following resource is "
               + "listed in the manifest but was not referenced by "
               + "an \"item\" or \"dependency\".  It appears that this "
               + "may be a dangling reference.  Appropriate action " + "may need to be taken to correct this problem";
         }
         // If there is more than one dangling resource in the list
         else
         {

            msgText = "All files included in the content package should "
               + "be declared and referenced in the manifest when "
               + "interchanging packages.  The following resources " + "are listed in the manifest but were not "
               + "referenced by any \"item\" or \"dependency\".  It "
               + "appears that these may be dangling references.  "
               + "Appropriate action may need to be taken to correct " + "this problem:";
         }

         mLogger.debug("WARNING: " + msgText);
         DetailedLogMessageCollection.getInstance().addMessage(new LogMessage(MessageType.WARNING, msgText));

         // we've displayed the warning, now list the files creating the warning
         for( int j = 0; j < mManifestResourceIdentifierList.size(); j++ )
         {
            mLogger.debug((String)mManifestResourceIdentifierList.get(j));
            DetailedLogMessageCollection.getInstance().addMessage(
               new LogMessage(MessageType.OTHER, (String)mManifestResourceIdentifierList.get(j)));
         }

      }// end if

   }// end checkForExcessBaggage()

   /**
    * Recursively walk the (sub)manifest node and track resource.href, file.href
    * and adlcp:location URI values. We will attempt to detect these files. If
    * found, they will be removed from the mFileList in order to account for
    * excessive baggage that is caused from (sub)manifests.
    * 
    * @param iSubmanifestNode the (sub)manifest element node
    * @param iSubmanifestURIList used to keep track of file and href references
    *           in (sub)manifests to exclude them from the excess
    *           baggage/dangling resource checks
    */
   private void trackSubManifest(Node iSubmanifestNode, Vector iSubmanifestURIList)
   {
      // must add URIs from the resource href and the file href
      if( iSubmanifestNode.getNodeName().equals("manifest") )
      {
         //retrieve xml:base at manifest if it exists
         String manifestXmlBase = DOMTreeUtility.getAttributeValue(iSubmanifestNode, "base");

         Node resourcesNode = DOMTreeUtility.getNode(iSubmanifestNode, "resources");

         if( resourcesNode != null )
         {
            String resourcesXmlBase = DOMTreeUtility.getAttributeValue(resourcesNode, "base");

            Vector resourceNodes = DOMTreeUtility.getNodes(resourcesNode, "resource");

            // Loop through the resource elements to retrieve the
            // href attribute values

            for( int i = 0; i < resourceNodes.size(); i++ )
            {
               Node currentResourceChild = (Node)resourceNodes.elementAt(i);
               String resourceHref = DOMTreeUtility.getAttributeValue(currentResourceChild, "href");

               String resourceXmlBase = DOMTreeUtility.getAttributeValue(currentResourceChild, "base");

               String xmlBase = manifestXmlBase + resourcesXmlBase + resourceXmlBase;

               iSubmanifestURIList.add(xmlBase + resourceHref);
               mLogger.debug("Just added " + xmlBase + resourceHref + "to the sub vector");

               // get children to add file href if it exists
               Vector fileNodes = DOMTreeUtility.getNodes(currentResourceChild, "file");

               // Loop through the file elements to retrieve the
               // href attribute values

               for( int j = 0; j < fileNodes.size(); j++ )
               {
                  Node currentFileChild = (Node)fileNodes.elementAt(j);
                  String fileHref = DOMTreeUtility.getAttributeValue(currentFileChild, "href");

                  iSubmanifestURIList.add(xmlBase + fileHref);

                  mLogger.debug("Just added " + xmlBase + fileHref + "to the sub vector");
               }
            }
         }
      }
      // recursivly call for nested child (sub)manifest
      NodeList manifestChildren = iSubmanifestNode.getChildNodes();

      for( int k = 0; k < manifestChildren.getLength(); k++ )
      {
         String currentNodeName = manifestChildren.item(k).getLocalName();

         if( currentNodeName.equals("manifest") )
         {
            trackSubManifest(manifestChildren.item(k), iSubmanifestURIList);
         }
      }

      // start cleansing process
      for( int m = 0; m < iSubmanifestURIList.size(); m++ )
      {
         mFileList.remove(iSubmanifestURIList.elementAt(m));
      }
   }
      
}// end class CPValidator
