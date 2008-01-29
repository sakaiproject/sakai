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

package org.adl.datamodels.ieee;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.adl.datamodels.DMDelimiterDescriptor;
import org.adl.datamodels.DMElement;
import org.adl.datamodels.DMElementDescriptor;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.DMTimeUtility;
import org.adl.datamodels.DataModel;
import org.adl.datamodels.RequestDelimiter;
import org.adl.datamodels.RequestToken;
import org.adl.datamodels.datatypes.DurationValidator;
import org.adl.datamodels.datatypes.LangStringValidator;
import org.adl.datamodels.datatypes.RealRangeValidator;
import org.adl.datamodels.datatypes.ResultValidator;
import org.adl.datamodels.datatypes.SPMRangeValidator;
import org.adl.datamodels.datatypes.URIValidator;
import org.adl.datamodels.datatypes.VocabularyValidator;
import org.adl.util.MessageCollection;


/**
 * <strong>Filename:</strong> SCORM_2004_DM.java<br><br>
 *
 * <strong>Description: </strong> This class implements the set of data model 
 * elements defined in the SCORM 2004.
 *
 * @author ADL Technical Team
 */
public class SCORM_2004_DM extends DataModel implements Serializable
{
	/**
	 * Constant for the smallest permitted maximum for suspend_data
	 */
	public static final int SUSPEND_DATA_SPM = 64000;

	/**
	 * Constant for smallest permitted maximum of 4000.
	 */
	public static final int LONG_SPM = 4000;

	/**
	 * Constant for smallest permitted maximum of 250.
	 */
	public static final int SHORT_SPM = 250;

	/**
	 * Constant for smallest permitted maximum for Comments From LMS
	 */
	public static final int COMMENTS_FROM_LMS_SPM = 100;

	/**
	 * Constant for smallest permitted maximum for Location
	 */
	public static final int LOCATION_SPM = 1000;

	/**
	 * Constant for smallest permitted maximum for Objectives
	 */
	public static final int OBJECTIVES_SPM = 100;
	
	
   private IValidatorFactory validatorFactory;
	
   /**
    * Describes the dot-notation binding string for this data model.
    */
   private String mBinding = "cmi";

   /**
    * Describes the data model elements managed by this data model.
    */
   private Map mElements = null;
   
   /**
    * Default constructor required for serialization support.
    */
   public SCORM_2004_DM() {} 
   
   public SCORM_2004_DM(IValidatorFactory validatorFactory)
   {
	  this.validatorFactory = validatorFactory;
	   
      Vector children = null;
      Vector subchildren = null;
      SCORM_2004_DMElement element = null;
      DMElementDescriptor desc = null;
      DMDelimiterDescriptor del = null;
      mElements = new Hashtable();


      // Create and add the _version element to the data model
      Version version = new Version("1.0");

      // Add description of this element
      desc = new DMElementDescriptor("_version", null, null);
      version.setDescription(desc);

      mElements.put(version.getDMElementBindingString(), version);

      // comments_from_learner
      children = new Vector();

      // comment
      desc = new DMElementDescriptor("comment", null,
                                     LONG_SPM,
                                     new SPMRangeValidator(LONG_SPM));
      del = new DMDelimiterDescriptor("lang", "en",
                                      SHORT_SPM,
                                      new LangStringValidator());

      desc.mDelimiters = new Vector();
      desc.mDelimiters.add(del);

      children.add(desc);

      // location
      desc = new DMElementDescriptor("location", null,
                                     SHORT_SPM,
                                     new SPMRangeValidator(SHORT_SPM));
      children.add(desc);

      // date_time
      desc = new DMElementDescriptor("timestamp", null, validatorFactory.newDateTimeValidator(true));
      children.add(desc);

      desc = new DMElementDescriptor("comments_from_learner", children, 250);

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // comments_from_lms
      children = new Vector();

      // comment
      desc = new DMElementDescriptor("comment", null,
                                     LONG_SPM,
                                     new SPMRangeValidator(LONG_SPM));
      del = new DMDelimiterDescriptor("lang", "en",
                                      SHORT_SPM,
                                      new LangStringValidator());

      desc.mDelimiters = new Vector();
      desc.mDelimiters.add(del);
      desc.mIsWriteable = false;

      children.add(desc);

      // location
      desc = new DMElementDescriptor("location", null,
                                     SHORT_SPM,
                                     new SPMRangeValidator(SHORT_SPM));
      desc.mIsWriteable = false;
      children.add(desc);

      // date_time
      desc = new DMElementDescriptor("timestamp", null, validatorFactory.newDateTimeValidator(true));
      
      desc.mIsWriteable = false;
      children.add(desc);

      desc = new DMElementDescriptor("comments_from_lms", children, 100);

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // completion_status
      String [] vocab = {"unknown", "completed", "not attempted",
         "incomplete"};
      desc = new DMElementDescriptor("completion_status", "unknown",
                                     new VocabularyValidator(vocab));

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // completion_threshold
      desc = new DMElementDescriptor("completion_threshold", null,
                                     new RealRangeValidator(new Double(0.0),
                                                            new Double(1.0)));
      desc.mIsWriteable = false;

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // credit
      String [] creditFormat = {"credit", "no-credit"};

      desc = new DMElementDescriptor("credit", "credit",
                                     new VocabularyValidator(creditFormat));
      desc.mIsWriteable = false;

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // entry
      String [] entryFormat = {"ab-initio", "resume", ""};

      desc = new DMElementDescriptor("entry", "ab-initio",
                                     new VocabularyValidator(entryFormat));
      desc.mIsWriteable = false;

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // exit
      String [] exitFormat = {"time-out", "suspend", "logout", "normal", ""};

      desc = new DMElementDescriptor("exit", "",
                                     new VocabularyValidator(exitFormat));
      // exit is write only
      desc.mIsReadable = false;

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // interactions
      children = new Vector();

      // interaction id
      desc =
      new DMElementDescriptor("id", null,
                              LONG_SPM,
                              new URIValidator(LONG_SPM, 
                                               "long_identifier_type"));

      children.add(desc);

      // interaction type
      String [] typeFormat = {"true-false", "choice", "fill-in", "long-fill-in",
         "matching", "performance", "sequencing", "likert",
         "numeric", "other"};

      desc = new DMElementDescriptor("type", null,
                                     new VocabularyValidator(typeFormat));

      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // interaction objectives
      subchildren = new Vector();

      // interaction objectives id
      desc =
      new DMElementDescriptor("id", null,
                              LONG_SPM,
                              new URIValidator(LONG_SPM, 
                                               "long_identifier_type"));
      desc.mIsUnique = true;
      subchildren.add(desc);

      desc = new DMElementDescriptor("objectives", subchildren, 10);
      desc.mShowChildren = false;
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // interaction timestamp
      desc = new DMElementDescriptor("timestamp", null, validatorFactory.newDateTimeValidator(true));
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // interaction correct_responses
      subchildren = new Vector();

      // interaction correct_responses pattern
      desc = new DMElementDescriptor("pattern", null, null);
      subchildren.add(desc);

      desc = new DMElementDescriptor("correct_responses", subchildren, 250);
      desc.mShowChildren = false;
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("type"));

      children.add(desc);

      // interaction weighting
      desc = new DMElementDescriptor("weighting", null,
                                     new RealRangeValidator(null,
                                                            null));
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // interaction learner_response
      desc = new DMElementDescriptor("learner_response", null, null);
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("type"));

      children.add(desc);

      // interaction result
      String [] resultFormat = {"correct", "incorrect", "unanticipated",
         "neutral"};
      desc = new DMElementDescriptor("result", null,
                                     new ResultValidator(resultFormat));
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // interaction latency
      desc = new DMElementDescriptor("latency", null,
                                     new DurationValidator());
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // interaction description
      desc = new DMElementDescriptor("description", null,
                                     SHORT_SPM,
                                     new SPMRangeValidator(SHORT_SPM));

      del = new DMDelimiterDescriptor("lang", "en",
                                      SHORT_SPM,
                                      new LangStringValidator());
      desc.mDelimiters = new Vector();
      desc.mDelimiters.add(del);
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // Finish creating the interaction element and add to the datamodel
      desc = new DMElementDescriptor("interactions", children, 250);

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // launch_data
      desc = new DMElementDescriptor("launch_data", null,
                                     LONG_SPM,
                                     new SPMRangeValidator(LONG_SPM));
      desc.mIsWriteable = false;

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // learner_id
      desc =
      new DMElementDescriptor("learner_id", null,
                              LONG_SPM,
                              new URIValidator(LONG_SPM, 
                                               "long_identifier_type"));
      desc.mIsWriteable = false;

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // learner_name
      desc = new DMElementDescriptor("learner_name", null,
                                     SHORT_SPM,
                                     new SPMRangeValidator(SHORT_SPM));

      del = new DMDelimiterDescriptor("lang", "en",
                                      SHORT_SPM,
                                      new LangStringValidator());
      desc.mDelimiters = new Vector();
      desc.mDelimiters.add(del);
      desc.mIsWriteable = false;

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // learner_preference
      children = new Vector();

      // learner_preference audio_level
      desc = new DMElementDescriptor("audio_level", "1",
                                     new RealRangeValidator(new Double(0.0),
                                                            null));

      children.add(desc);

      // learner_preference language
      desc = new DMElementDescriptor("language", "",
                                     SHORT_SPM,
                                     new LangStringValidator(true));

      children.add(desc);

      // learner_preference delivery_speed
      desc = new DMElementDescriptor("delivery_speed", "1",
                                     new RealRangeValidator(new Double(0.0),
                                                            null));

      children.add(desc);

      // learner_preference audio_captioning
      String [] audioFormat = {"-1", "0", "1"};
      desc = new DMElementDescriptor("audio_captioning", "0",
                                     new VocabularyValidator(audioFormat));

      children.add(desc);

      //  Now add the children to learner_preference and add to the Data Model
      desc = new DMElementDescriptor("learner_preference", children);
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // location
      desc = new DMElementDescriptor("location", null,
                                     1000,
                                     new SPMRangeValidator(1000));

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // max_time_allowed
      desc = new DMElementDescriptor("max_time_allowed", null,
                                     new DurationValidator());
      desc.mIsWriteable = false;

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // mode
      String [] modeFormat = {"browse", "normal", "review"};

      desc = new DMElementDescriptor("mode", "normal",
                                     new VocabularyValidator(modeFormat));
      desc.mIsWriteable = false;

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // objectives
      children = new Vector();

      // objectives id
      desc =
      new DMElementDescriptor("id", null,
                              LONG_SPM,
                              new URIValidator(LONG_SPM, 
                                               "long_identifier_type"));
      desc.mWriteOnce = true;
      desc.mIsUnique = true;

      children.add(desc);

      // objectives score
      subchildren = new Vector();

      // objectives score scaled
      desc = new DMElementDescriptor("scaled", null,
                                     new RealRangeValidator(new Double(-1.0),
                                                            new Double(1.0)));
      subchildren.add(desc);

      // objectives score raw
      desc = new DMElementDescriptor("raw", null,
                                     new RealRangeValidator(null, null));
      subchildren.add(desc);

      // objectives score min
      desc = new DMElementDescriptor("min", null,
                                     new RealRangeValidator(null, null));
      subchildren.add(desc);

      // objectives score max
      desc = new DMElementDescriptor("max", null,
                                     new RealRangeValidator(null, null));
      subchildren.add(desc);

      // Create the score
      desc = new DMElementDescriptor("score", subchildren);
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      // Add the score and its children to the objectives children
      children.add(desc);

      // objectives success_status
      String [] successFormat = {"passed", "failed", "unknown"};
      desc = new DMElementDescriptor("success_status", "unknown",
                                     new VocabularyValidator(successFormat));
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // objectives completion_status
      String [] completionFormat = {"completed", "incomplete", "not attempted",
         "unknown"};
      desc = new DMElementDescriptor("completion_status", "unknown",
                                     new VocabularyValidator(completionFormat));
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // objectvies progress measure.
      desc = new DMElementDescriptor("progress_measure", null,
                                     new RealRangeValidator(new Double(0),
                                                            new Double(1.0)));
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // objectives description
      desc = new DMElementDescriptor("description", null,
                                     SHORT_SPM,
                                     new SPMRangeValidator(SHORT_SPM));

      del = new DMDelimiterDescriptor("lang", "en",
                                      SHORT_SPM,
                                      new LangStringValidator());

      desc.mDelimiters = new Vector();
      desc.mDelimiters.add(del);
      desc.mDependentOn = new Vector();
      desc.mDependentOn.add(new String("id"));

      children.add(desc);

      // Create the objectives element and add it to the data model
      desc = new DMElementDescriptor("objectives", children, 100);
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // progress_measure
      desc = new DMElementDescriptor("progress_measure", null,
                                     new RealRangeValidator(new Double(0),
                                                            new Double(1.0)));
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // scaled_passing_score
      desc = new DMElementDescriptor("scaled_passing_score", null,
                                     new RealRangeValidator(new Double(-1.0),
                                                            new Double(1.0)));
      desc.mIsWriteable = false;

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // score
      children = new Vector();

      // score scaled
      desc = new DMElementDescriptor("scaled", null,
                                     new RealRangeValidator(new Double(-1.0),
                                                            new Double(1.0)));
      children.add(desc);

      // score raw
      desc = new DMElementDescriptor("raw", null,
                                     new RealRangeValidator(null, null));
      children.add(desc);

      // score min
      desc = new DMElementDescriptor("min", null,
                                     new RealRangeValidator(null, null));
      children.add(desc);

      // score max
      desc = new DMElementDescriptor("max", null,
                                     new RealRangeValidator(null, null));
      children.add(desc);

      // Create the score element and add it to the data model
      desc = new DMElementDescriptor("score", children);
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // session_time
      desc = new DMElementDescriptor("session_time", null,
                                     new DurationValidator());
      desc.mIsReadable = false;
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // success_status
      desc = new DMElementDescriptor("success_status", "unknown",
                                     new VocabularyValidator(successFormat));

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // suspend_data
      desc = new DMElementDescriptor("suspend_data", null,
                                     SUSPEND_DATA_SPM,
                                     new SPMRangeValidator(SUSPEND_DATA_SPM));

      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // time_limit_action
      String [] actionFormat = {"exit,message", "continue,message",
         "exit,no message", "continue,no message"};
      desc = new DMElementDescriptor("time_limit_action", "continue,no message",
                                     new VocabularyValidator(actionFormat));
      desc.mIsWriteable = false;

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);

      // total_time
      desc = new DMElementDescriptor("total_time", "PT0H0M0S",
                                     new DurationValidator());
      desc.mIsWriteable = false;

      // Create and add this element to the data model
      element = new SCORM_2004_DMElement(desc, null, this);
      mElements.put(desc.mBinding, element);
   }
   
   public Map getElements() {
	   return mElements;
   }

   public int equals(DMRequest iRequest) {
	   return equals(iRequest, true);
   }
   
   /**
    * Processes an equals() request against this data model. Compares two values
    * of the same data model element for equality.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * @param iValidate Indicates if the provided value should be validated.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int equals(DMRequest iRequest, boolean iValidate)
   {

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Create an 'out' variable
      DMProcessingInfo pi = new DMProcessingInfo();

      // Process this request
      result = findElement(iRequest, pi);

      // If we found the 'leaf' element, finish the request
      if ( result == DMErrorCodes.NO_ERROR )
      {
         RequestToken tok = iRequest.getNextToken();

         // Before processing, make sure this is the last token in the request
         if ( !iRequest.hasMoreTokens() )
         {
            // Make sure this is a Value token
            if ( tok.getType() == RequestToken.TOKEN_VALUE )
            {
               result = pi.mElement.equals(tok, iValidate);
            }
            else
            {
               // Wrong type of token -- value expected
               result = DMErrorCodes.INVALID_REQUEST;
            }
         }
         else
         {
            // Too many tokens
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }

      return result;
   }

   /**
    * Describes this data model's binding string.
    *
    * @return This data model's binding string.
    */
   public String getDMBindingString()
   {
      return mBinding;
   }

   /**
    * Provides the request data model element.
    *
    * @param iElement Describes the requested element's dot-notation bound name.
    *
    * @return The <code>DMElement</code> corresponding to the requested element
    *         or <code>null</code> if the element does not exist in the data
    *         model.
    */
   public DMElement getDMElement(String iElement)
   {
      DMElement element = (DMElement)mElements.get(iElement);

      return element;
   }

   /**
    * Processes a GetValue() request against this data model.  Retrieves the 
    * value associated with the requested data model element in the current 
    * set of data model values.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * @param oInfo Provides the value returned by this request.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int getValue(DMRequest iRequest, DMProcessingInfo oInfo)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Create an 'out' variable
      DMProcessingInfo pi = new DMProcessingInfo();

      // Process this request
      result = findElement(iRequest, pi);

      // If we found the 'leaf' elmeent, finish the request
      if ( result == DMErrorCodes.NO_ERROR )
      {
         RequestToken tok = iRequest.getNextToken();

         // Before processing, make sure this is the last token in the request
         if ( !iRequest.hasMoreTokens() )
         {
            result = pi.mElement.getValue(tok,
                                          iRequest.isAdminRequest(),
                                          iRequest.supplyDefaultDelimiters(),
                                          oInfo);
         }
         else
         {
            // Too many tokens
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }

      return result;
   }
   
   /**
    * Performs data model specific initialization.
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int initialize()
   {
      return DMErrorCodes.NO_ERROR;
   }

   /**
    * Processes a SetValue() request against this data model.  This includes a check
    * for the validity of the value for the specified data model element.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int setValue(DMRequest iRequest)
   {

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Create an 'out' variable
      DMProcessingInfo pi = new DMProcessingInfo();

      // Process this request
      result = findElement(iRequest, pi);

      // If we found the 'leaf' elmeent, finish the request
      if ( result == DMErrorCodes.NO_ERROR )
      {
         RequestToken tok = iRequest.getNextToken();

         // Before processing, make sure this is the last token in the requset
         if ( !iRequest.hasMoreTokens() )
         {
            // Make sure this is a Value token
            if ( tok.getType() == RequestToken.TOKEN_VALUE )
            {
               // Check uniqueness constraints
               if ( pi.mElement.getDescription() != null )
               {
                  if ( pi.mElement.getDescription().mIsUnique )
                  {
                     if ( !confirmUniqueness(iRequest, tok) )
                     {
                        // Not Unique
                        result = DMErrorCodes.NOT_UNIQUE;

      		        if ( pi.mElement.isInitialized() )
      		        {
      		            // Need to check if the current value is different
      			    // than the provided value
      			    if ( ((SCORM_2004_DMElement)pi.mElement).getInternalValue().equals(tok.getValue()) )
      			    {
      			       // Setting the current value to an identical
      			       result = DMErrorCodes.NO_ERROR;
      			    }
      		        }

                        if ( result != DMErrorCodes.NO_ERROR )
                        {
                           // New storage was initialized but will not be used, delete
                           if ( pi.mRecords != null )
                           {
                              // Remove the last element from the record set
                              pi.mRecords.remove(pi.mRecords.size() - 1);
                           }    
                        }
                     }
                  }

		  if ( result == DMErrorCodes.NO_ERROR && 
		       pi.mElement.getDescription().mWriteOnce )
		  {
		      if ( pi.mElement.isInitialized() )
		      {
			  // Need to check if the current value is different
			  // than the provided value
			  if ( !(((SCORM_2004_DMElement)pi.mElement).
				 getInternalValue()).
			       equals(tok.getValue()) )
			  {
			     // Attempt to overwrite ID
			     result = DMErrorCodes.OVERWRITE_ID;
			  }
		      }
		  }
               }

               if ( result == DMErrorCodes.NO_ERROR )
               {
                  result = pi.mElement.setValue(tok, iRequest.isAdminRequest());

                  // If new storage was initialized but not used, delete
                  if ( pi.mRecords != null )
                  {
                     if ( !(result == DMErrorCodes.NO_ERROR ||
                            result == DMErrorCodes.SPM_EXCEEDED) )
                     {
                        // Remove the last element from the record set
                        pi.mRecords.remove(pi.mRecords.size() - 1);
                     }
                  }
               }
            }
            else
            {
               // Wrong type of token -- value expected
               result = DMErrorCodes.INVALID_REQUEST;
            }
         }
         else
         {
            // Too many tokens
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }

      return result;
   }

   /**
    * Displays the contents of the entire data model.
    * NOTE:  This function has not been implemented.
    */
   public void showAllElements()
   {
	   if (null != mElements) {
		   for (Object obj : mElements.values()) {
			  			   
			   DMElement elem = (DMElement)obj;
			    
			   System.out.println("DMElement Binding: " + elem.getDMElementBindingString());
			  
			   DMElementDescriptor desc = elem.getDescription();
			   
			   if (null != desc) {
				   System.out.println("DMElement Desc Binding " + desc.mBinding);
				   System.out.println("DMElement Desc Initial " + desc.mInitial);
				   
				   List delims = desc.mDelimiters;
				   
				   if (null != delims) {
					   for (Object o : delims) {
						   
						   
						   DMDelimiterDescriptor del = (DMDelimiterDescriptor)o;
						   
						   if (null != del) {
							   System.out.println("DMElement Delim Value " + del.mName);
							   
							   //DMDelimiterDescriptor delDesc = del;
							   
							   //if (null != delDesc)
							   //   System.out.println("DMElement Delim Desc " + delDesc.mName);
						   }
					   }
				   }
			   }
		   }
	   }
	   
      // At this time, there is no defined implementation for this method.
   }

   /**
    * Performs data model specific termination.  Called when the API processes
    * a Terminate request from a SCO.
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int terminate()
   {
      DMRequest req = null;
      DMProcessingInfo dmInfo = null;

      int err = DMErrorCodes.NO_ERROR;


      req = new DMRequest("cmi.total_time");
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);

      String totalTime = dmInfo.mValue;

      req = new DMRequest("cmi.session_time", true, false);
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);
      if ( err == DMErrorCodes.NO_ERROR )
      {
         String sessionTime = dmInfo.mValue;
         String addedTime = DMTimeUtility.add( totalTime, sessionTime );
         req = new DMRequest("cmi.total_time", addedTime, true);
         req.getNextToken();
         err = setValue(req);
      }

      // Update completion status
      req = new DMRequest("cmi.progress_measure");
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);

      String progress = dmInfo.mValue;

      req = new DMRequest("cmi.completion_threshold");
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);

      String threshold = dmInfo.mValue;

      if ( progress != null && threshold != null &&
           (! progress.equals("")) &&
           (! threshold.equals("")) )
      {
         if ( Double.parseDouble(progress) >= Double.parseDouble(threshold) )
         {
            req = new DMRequest("cmi.completion_status", "completed", true);
            req.getNextToken();

            err = setValue(req);
         }
         else if ( Double.parseDouble(progress) <
                   Double.parseDouble(threshold) )
         {
            req = new DMRequest("cmi.completion_status", "incomplete", true);
            req.getNextToken();

            err = setValue(req);
         }
      }

      // Update the success status
      req = new DMRequest("cmi.score.scaled");
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);

      progress = dmInfo.mValue;

      req = new DMRequest("cmi.scaled_passing_score");
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);

      threshold = dmInfo.mValue;

      if ( threshold != null &&
           !threshold.equals("") )
      {
         if( progress != null &&
             !progress.equals("") )
         {
            if ( Double.parseDouble(progress) >= Double.parseDouble(threshold) )
            {
               req = new DMRequest("cmi.success_status", "passed", true);
               req.getNextToken();

               err = setValue(req);
            }
            else
            {
               req = new DMRequest("cmi.success_status", "failed", true);
               req.getNextToken();

               err = setValue(req);
            }
         }
         else
         {
            // Reset success_status to unknown
            req = new DMRequest("cmi.success_status", "unknown", true);
            req.getNextToken();

            err = setValue(req);
         }
      }

      // Set the entry flag based on the exit value
      req = new DMRequest("cmi.exit", true, false);
      req.getNextToken();

      dmInfo = new DMProcessingInfo();
      err = getValue(req, dmInfo);

      // Check if the current Learner Attempt has been suspended
      if ( dmInfo.mValue.equals("suspend") || dmInfo.mValue.equals("logout") )
      {
         // The next time this SCO is experienced, the current
         // Learner Session will be "resumed"
         req = new DMRequest("cmi.entry", "resume", true);
         req.getNextToken();

         err = setValue(req);

         // Clear the current Learner Session Time by creating a new element
         DMElementDescriptor desc =
            new DMElementDescriptor("session_time",
                                    null,
                                    new DurationValidator());

         desc.mIsReadable = false;
         SCORM_2004_DMElement element =
            new SCORM_2004_DMElement(desc, null, this);
         mElements.put(desc.mBinding, element);
      }
      else
      {
         req = new DMRequest("cmi.entry", "", true);
         req.getNextToken();

         err = setValue(req);
      }

      // This Learner Session is over, clear cmi.exit
      req = new DMRequest("cmi.exit", "", true);
      req.getNextToken();

      err = setValue(req);

      return DMErrorCodes.NO_ERROR;
   }

   /**
    * Processes a validate() request against this data model.  Checks that
    * the value of the request is valid for the specified data model element.
    *
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   public int validate(DMRequest iRequest)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Create an 'out' variable
      DMProcessingInfo pi = new DMProcessingInfo();

      // Process this request
      result = findElement(iRequest, pi);

      // If we found the 'leaf' elmeent, finish the request
      if ( result == DMErrorCodes.NO_ERROR )
      {
         RequestToken tok = iRequest.getNextToken();

         // Before processing, make sure this is the last token in the request
         if ( !iRequest.hasMoreTokens() )
         {
            // Make sure this is a Value token
            if ( tok.getType() == RequestToken.TOKEN_VALUE )
            {
               result = pi.mElement.validate(tok);
            }
            else
            {
               // Wrong type of token -- value expected
               result = DMErrorCodes.INVALID_REQUEST;
            }
         }
         else
         {
            // Too many tokens
            result = DMErrorCodes.INVALID_REQUEST;
         }
      }

      return result;
   }


   /**
    * Determines if the value provided is unique across all instances
    * of the data model element indicated by the request.
    * 
    * @param ioRequest The request requiring a uniqueness check.
    * @param iValue The value being checked.
    * 
    * @return <code>true</code> if the value is unique for the
    *         given request, otherwise <code>false</code>.
    */
   private boolean confirmUniqueness(DMRequest ioRequest,
                                     RequestToken iValue)
   {
      boolean unique = true;

      // Pause the MessageCollection so we don't get SPM errors during
      // this check.
      MessageCollection.getInstance().pause(true);

      // Need to check how many indexes to pass
      int depth = 0;

      ioRequest.reset();

      while ( ioRequest.hasMoreTokens() )
      {
         RequestToken next = ioRequest.getCurToken();

         if ( next.getType() == RequestToken.TOKEN_INDEX )
         {
            depth++;
         }

         next = ioRequest.getNextToken();
      }

      // The uniqueness check comes last -- reset the request
      ioRequest.reset();

      // Create the new dot-notation binding string for this check
      String request = new String("");

      RequestToken tok = ioRequest.getNextToken();
      request = request + tok.getValue();

      // Get the element token
      tok = ioRequest.getNextToken();
      request = request + "." + tok.getValue();

      DMProcessingInfo pi = new DMProcessingInfo();
      int result = DMErrorCodes.NO_ERROR;

      int idxCount = 0;

      // Process tokens until we reach the index
      while ( ioRequest.hasMoreTokens() && result == DMErrorCodes.NO_ERROR )
      {
         RequestToken next = ioRequest.getCurToken();

         if ( next.getType() != RequestToken.TOKEN_INDEX )
         {
            request = request + "." + next.getValue();
            next = ioRequest.getNextToken();
         }
         else
         {
            idxCount++;
            if (idxCount == depth)
            {
               // Found the correct index token -- Done.
               break;
            }
            request = request + "." + next.getValue();
            next = ioRequest.getNextToken();
         }
      }

      // Skip over the index to find the field be tested
      tok = ioRequest.getNextToken();
      tok = ioRequest.getNextToken();

      // How many elements do we need to look at?
      int count = 0;
      pi = new DMProcessingInfo();
      String newRequest = request + "._count";

      DMRequest req = null;
      try
      {
         req = new DMRequest(newRequest);

         // Eat the dm token
         req.getNextToken();

         // Process the request
         result = getValue(req, pi);

         if ( result == DMErrorCodes.NO_ERROR )
         {
            count = Integer.parseInt(pi.mValue);
         }
      }
      catch ( Exception e )
      {
         e.printStackTrace();
      }

      String value = iValue.getValue();
      for ( int i = 0 ; i < iValue.getDelimiterCount(); i++ )
      {
         RequestDelimiter del = iValue.getDelimiterAt(i);
         value = value + del.showDotNotation();
      }

      // Loop across all records, looking at the target field
      for ( int i = 0; i < (count - 1); i++ )
      {
         newRequest = request + "." + String.valueOf(i) + "." + tok.getValue();

         try
         {
            req = new DMRequest(newRequest, value);

            // Eat the dm token
            req.getNextToken();

            result = equals(req, false);

            if ( result == DMErrorCodes.COMPARE_EQUAL )
            {
               unique = false;
               break;
            }
         }
         catch ( Exception e )
         {
            // Do nothing
         }
      }

      // Un-pause the MessageCollection
      MessageCollection.getInstance().pause(false);

      return unique;
   }

   /**
    * Processes a data model request by finding the target leaf element.
    *
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    *
    * @param oInfo Provides the value returned by this request.
    *
    * @return A data model error code indicating the result of this
    *         operation.
    */
   private int findElement(DMRequest iRequest, DMProcessingInfo oInfo)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Get the first specified element
      RequestToken tok = iRequest.getNextToken();

      if ( tok != null && tok.getType() == RequestToken.TOKEN_ELEMENT )
      {

         DMElement element = (DMElement)mElements.get(tok.getValue());

         if ( element != null )
         {
            oInfo.mElement = element;

            // Check if we need to stop before the last token
            tok = iRequest.getCurToken();
            boolean done = false;

            if ( tok != null )
            {
               if ( iRequest.isGetValueRequest() )
               {
                  if ( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                  {
                     // We're done
                     done = true;
                  }
                  else if ( tok.getType() == RequestToken.TOKEN_VALUE )
                  {
                     // Get requests cannot have value tokens
                     result = DMErrorCodes.INVALID_REQUEST;

                     done = true;
                  }
               }
               else
               {
                  if ( tok.getType() == RequestToken.TOKEN_VALUE )
                  {
                     // We're done
                     done = true;
                  }
                  else if ( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                  {
                     // Set requests cannot have argument tokens
                     result = DMErrorCodes.INVALID_REQUEST;

                     done = true;
                  }
               }
            }

            // Process remaining tokens
            while ( !done && iRequest.hasMoreTokens() &&
                    result == DMErrorCodes.NO_ERROR )
            {
               result = element.processRequest(iRequest, oInfo);

               // Move to the next element if processing was successful
               if ( result == DMErrorCodes.NO_ERROR )
               {
                  element = oInfo.mElement;
               }
               else
               {
                  oInfo.mElement = null;
               }

               // Check if we need to stop before the last token
               tok = iRequest.getCurToken();

               if ( tok != null )
               {
                  if ( iRequest.isGetValueRequest() )
                  {
                     if ( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                     {
                        // We're done
                        done = true;
                     }
                     else if ( tok.getType() == RequestToken.TOKEN_VALUE )
                     {
                        // Get requests cannot have value tokens
                        result = DMErrorCodes.INVALID_REQUEST;

                        done = true;
                     }
                  }
                  else
                  {
                     if ( tok.getType() == RequestToken.TOKEN_VALUE )
                     {
                        // We're done
                        done = true;
                     }
                     else if ( tok.getType() == RequestToken.TOKEN_ARGUMENT )
                     {
                        // Set requests cannot have argument tokens
                        result = DMErrorCodes.INVALID_REQUEST;

                        done = true;
                     }
                  }
               }
            }
         }
         else
         {
            // Unknown element
            result = DMErrorCodes.UNDEFINED_ELEMENT;
         }
      }
      else
      {
         // No initial element specified
         result = DMErrorCodes.INVALID_REQUEST;
      }

      // Make sure we are at a leaf element
      if ( result == DMErrorCodes.NO_ERROR )
      {
         if ( oInfo.mElement.getDescription().mChildren != null && oInfo.mElement.getDescription().mChildren.size() > 0)
         {
            // Unknown element
            result = DMErrorCodes.UNDEFINED_ELEMENT;
         }
      }

      return result;
   }

public IValidatorFactory getValidatorFactory() {
	return validatorFactory;
}

public void setValidatorFactory(IValidatorFactory validatorFactory) {
	this.validatorFactory = validatorFactory;
}

} // end SCORM_2004_DM
