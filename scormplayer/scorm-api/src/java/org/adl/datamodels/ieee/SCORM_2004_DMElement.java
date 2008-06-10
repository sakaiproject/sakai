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
*******************************************************************************/

package org.adl.datamodels.ieee;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

import org.adl.datamodels.Children;
import org.adl.datamodels.Count;
import org.adl.datamodels.DMDelimiter;
import org.adl.datamodels.DMDelimiterDescriptor;
import org.adl.datamodels.DMElement;
import org.adl.datamodels.DMElementDescriptor;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.DataModel;
import org.adl.datamodels.RequestDelimiter;
import org.adl.datamodels.RequestToken;
import org.adl.datamodels.datatypes.InteractionTrunc;
import org.adl.datamodels.datatypes.InteractionValidator;
import org.adl.datamodels.datatypes.LangStringValidator;
import org.adl.datamodels.datatypes.SPMRangeValidator;
import org.adl.datamodels.datatypes.VocabularyValidator;
import org.adl.logging.DetailedLogMessageCollection;
import org.adl.util.LogMessage;
import org.adl.util.MessageType;

/**
 * <br><br>
 * 
 * <strong>Filename:</strong> SCORM_2004_DMElement.java<br><br>
 * 
 * <strong>Description:</strong><br><br>
 * 
 * <strong>Design Issues:</strong><br><br>
 * 
 * <strong>Implementation Issues:</strong><br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 2004
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class SCORM_2004_DMElement extends DMElement implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	/** 
	 * Describes the data model this element is a member of
	 */
	protected SCORM_2004_DM mDM = null;
	
	
   /**
    * Default constructor required for serialization support.
    */
   public SCORM_2004_DMElement() 
   {
      // The default constructor - no explicit functionallity defined
   }

   /**
    * Initializes one data model element of the SCORM 2004 data model.
    * 
    * @param iDescription  Identies which data characteristics of the data model
    *                      element.
    * 
    * @param iParent       Describes the parent of this data model element.
    * 
    * @param iDM           Describes the data model containing this data model
    *                      element.
    */
   public SCORM_2004_DMElement(DMElementDescriptor iDescription,
                               DMElement iParent,
                               SCORM_2004_DM iDM) 
   {
      // Set this element's description
      mDescription = iDescription;

      // Set the parent
      mParent = iParent;

      // Set the data model
      mDM = iDM;

      // Check if this element is managing records -- is it an array container?
      if ( mDescription.mSPM != -1 && mDescription.mChildren != null && mDescription.mChildren.size() > 0)
      {
         // Initialize the set of records
         mRecords = new Vector();
      }

      // Check if this element has children
      if ( mDescription.mChildren != null && mDescription.mChildren.size() > 0)
      {
         // Initialize the set of children bindings
         mChildrenBindings = new Vector();

         for ( int i = 0; i < mDescription.mChildren.size(); i++ )
         {
            DMElementDescriptor desc = 
            (DMElementDescriptor)mDescription.mChildren.get(i);

            mChildrenBindings.add(desc.mBinding);

            // If this is not an array container
            if ( mRecords == null )
            {
               if ( mChildren == null )
               {
                  mChildren = new Hashtable();
               }

               // Initialize the leaf child element
               SCORM_2004_DMElement element = 
               new SCORM_2004_DMElement(desc, this, mDM);
               mChildren.put(desc.mBinding, element);
            }
         }
      }
      else
      {

         // This must be a leaf element, so initialize 
         if ( mDescription.mDelimiters != null && mDescription.mDelimiters.size() > 0)
         {
            // Initialize this element's set of delimiters 
            mDelimiters = new Vector();

            for ( int i = 0; i < mDescription.mDelimiters.size(); i++ )
            {
               DMDelimiterDescriptor desc = 
               (DMDelimiterDescriptor)mDescription.mDelimiters.get(i);

               // Create the child element
               DMDelimiter delimit = new DMDelimiter(desc);
               mDelimiters.add(delimit);
            }
         }

         if ( mDescription.mInitial != null )
         {
            mValue = mDescription.mInitial;
            mInitialized = true;
         }
         else
         {
            // The value is considered uninitialized until the SCO sets it
            mInitialized = false;

            mValue = "";
         }
      }
   }


   /**
    * Compares the provided value to the value stored in this data model
    * element.
    * 
    * @param iValue A token (<code>RequestToken</code>) object that provides the
    *               value to be compared against the exiting value; this request
    *               may include a set of delimiters.
    * @param iValidate Describes if the value being compared should be
    *                  validated first.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int equals(RequestToken iValue, boolean iValidate)
   {
      // Assume there is nothing to compare
      int result = DMErrorCodes.COMPARE_NOTHING;

      // Make sure there is something to compare
      if ( mValue != null && iValue != null )
      {
         // Assume these values are equal
         boolean equal = true;

         if ( iValidate )
         {
            // Make sure the value is valid against this element's type
            result = validate(iValue);
         }

         if ( result == DMErrorCodes.TYPE_MISMATCH )
         {
            // An invalid value cannot be equal to any valid (set) data model
            // element.
            equal = false;
         }

         if ( equal )
         {
            int i = 0;

            // If delimiters are defined on this data model element, make sure
            // any provided delimiters are equal, or the existing delimiter
            // value is the default.
            if ( mDelimiters != null && mDelimiters.size() > 0)
            {
               Vector checked = new Vector();

               // Set all delimiters to 'not checked'
               for ( int j = 0; j < mDelimiters.size(); j++ )
               {
                  checked.add(new Boolean(false));
               }

               boolean found = true;

               // Compare all delimiters provided with this value
               for ( ; i < iValue.getDelimiterCount() && equal; i++ )
               {
                  RequestDelimiter del = iValue.getDelimiterAt(i);

                  // Assume this delimiter is not defined for the element
                  found = false;

                  // Check if this element includes the specified delimiter
                  for ( int j = 0; j < mDelimiters.size() && equal; j++ )
                  {
                     DMDelimiter toCheck = 
                     (DMDelimiter)mDelimiters.get(j);

                     if ( toCheck.mDescription.mName.equals(del.getName()) )
                     {
                        // Make sure we haven't already checked this delimter
                        boolean alreadyChecked = 
                        ((Boolean)checked.get(j)).booleanValue();

                        if ( !alreadyChecked )
                        {
                           // Remember we've checked this delimiter
                           found = true;
                           checked.set(j, new Boolean(true));

                           // Check if the value is the delimiter's default
                           if ( toCheck.mValue == null )
                           {
                              if ( toCheck.mDescription.mDefault != null )
                              {
                                 // Compare the provided value with the default
                                 if ( toCheck.mDescription.mValidator == null )
                                 {
                                    equal = equal && toCheck.mDescription.
                                            mDefault.equals(del.getValue());
                                 }
                                 else
                                 {
                                    equal = 
                                    equal &&
                                    toCheck.mDescription.
                                    mValidator.
                                    compare(toCheck.mDescription.mDefault, 
                                            del.getValue(),
                                            mDelimiters);
                                 }
                              }
                              else
                              {
                                 equal = false;
                              }
                           }
                           else
                           {
                              // Compare the two delimiter values
                              if ( toCheck.mDescription.mValidator == null )
                              {
                                 equal = equal && 
                                         toCheck.mValue.equals(del.getValue());
                              }
                              else
                              {
                                 equal = equal && 
                                         toCheck.mDescription.
                                         mValidator.compare(toCheck.mValue, 
                                                            del.getValue(),
                                                            mDelimiters);

                                 // If the first compare doesn't work
                                 // test the SPM
                                 if ( !equal )
                                 {
                                 equal = toCheck.mDescription.
                                         mValidator.
                                            compare(
                                               toCheck.mDescription.mValidator.
                                                  trunc(toCheck.mValue), 
                                               del.getValue(),
                                               mDelimiters);
                                 }
                              }
                           }
                        }

                        break;
                     }
                  }

                  if ( !found )
                  {
                     break;
                  }
               }

               if ( equal )
               {
                  // Make sure any delimters not included in the request are 
                  // equal to their defaults
                  for ( int j = 0; j < mDelimiters.size() && equal; j++ )
                  {
                     boolean check = 
                     !((Boolean)checked.get(j)).booleanValue();

                     if ( check )
                     {
                        DMDelimiter toCheck =
                        (DMDelimiter)mDelimiters.get(j);

                        if ( toCheck.mValue != null )
                        {
                           // Compare the current value to the default
                           if ( toCheck.mDescription.mValidator != null )
                           {
                              equal = equal &&
                                      toCheck.mDescription.
                                      mValidator.
                                      compare(toCheck.mDescription.mDefault,
                                              toCheck.mValue,
                                              mDelimiters);
                           }
                           else
                           {
                              equal = false;
                           }
                        }
                     }
                  }
               }
            }

            // Only compare the data model values if its delimiters where equal
            if ( equal )
            {

               String compareWith = "";

               // Add all 'undefined' delimiters to the validation string
               for ( ; i < iValue.getDelimiterCount(); i++ )
               {
                  RequestDelimiter del = iValue.getDelimiterAt(i);

                  compareWith += del.showDotNotation();
               }

               compareWith = compareWith + iValue.getValue();

               // If no comparison method is defined, just do a string compare
               if ( mDescription.mValidator == null )
               {
                  equal = compareWith.equals(mValue);
               }
               else
               {
                  equal = mDescription.mValidator.compare(compareWith, 
                                                          mValue,
                                                          mDelimiters);

                  // If the first compare doesn't work test the SPM
                  if ( !equal )
                  {
                     equal = mDescription.mValidator.
                        compare(compareWith, 
                                mDescription.mValidator.trunc(mValue),
                                mDelimiters);
                  }
               }
            }
         }

         // Set the correct result value
         if ( equal )
         {
            result =  DMErrorCodes.COMPARE_EQUAL;
         }
         else
         {
            result =DMErrorCodes.COMPARE_NOTEQUAL;
         }
      }

      return result;
   }

   /**
    * Attempt to get the value of this data model element, which may include
    * default delimiters.
    * 
    * @param iArguments  Describes the arguments for this getValue() call.
    * 
    * @param iAdmin      Describes if this request is an administrative action.
    * 
    * @param iDelimiters Indicates if the data model element's default
    *                    delimiters should be included in the return string.
    * 
    * @param oInfo       Provides the value of this data model element.
    *                    <b>Note: The caller of this function must provide an
    *                    initialized (new) <code>DMProcessingInfo</code> to
    *                    hold the return value.</b>
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int getValue(RequestToken iArguments,
                       boolean iAdmin,
                       boolean iDelimiters, 
                       DMProcessingInfo oInfo)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // The SCORM 2004 run-time data model does not have any data model 
      // elements requiring arguments.  Any attempt to use delimiters with
      // a GetValue() request would be an invalid SCORM 2004 DM request.
      if ( iArguments != null )
      {
         result = DMErrorCodes.INVALID_REQUEST;
      }
      else
      {
         // Parent data model elments do not directly store data
         if ( mDescription.mSPM == -1 )
         {
            // Is the element write only?
            if ( mDescription.mIsReadable || iAdmin )
            {

               // Is the element initialized?
               if ( mInitialized )
               {
                  // Initialize the return string
                  oInfo.mValue = "";

                  // Add delimiters as required
                  if ( mDelimiters != null && mDelimiters.size() > 0)
                  {
                     for ( int i = 0; i < mDelimiters.size(); i++ )
                     {
                        DMDelimiter del = 
                        (DMDelimiter)mDelimiters.get(i);

                        oInfo.mValue += del.getDotNotation(iDelimiters);
                     }
                  }

                  // Add the element's value
                  oInfo.mValue += mValue;
               }
               else
               {
                  result = DMErrorCodes.NOT_INITIALIZED;                  
               }
            }
            else
            {
               result = DMErrorCodes.WRITE_ONLY;
            }
         }
         else
         {
            // This should not occur
            result = DMErrorCodes.UNDEFINED_ELEMENT;
         }
      }

      return result; 
   }

   /**
    * Provides the internal value stored for the data model element.
    * 
    * @return  The element's internal value
    */
   /* package */
   String getInternalValue()
   {
      return mValue;
   }

   /**
    * Processes a data model request on this data model element.  This method
    * will enforce data model element depedencies and keyword application.
    * 
    * @param ioRequest Provides the dot-notation request being applied to this
    *                  data model element.  The <code>DMRequest</code> will be
    *                  updated to account for processing against this data
    *                  model element.
    * 
    * @param oInfo     Provides the value of this data model element.
    *                  <b>Note: The caller of this function must provide an
    *                  initialized (new) <code>DMProcessingInfo</code> to
    *                  hold the return value.</b>
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int processRequest(DMRequest ioRequest, DMProcessingInfo oInfo)
   {
      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // Make sure there are more tokens to process
      if ( ioRequest.hasMoreTokens() )
      {
         // Get the next token
         RequestToken tok = ioRequest.getNextToken();

         // Check if this token is an index or an element
         if ( tok.getType() == RequestToken.TOKEN_ELEMENT )
         {
            // Check for keyword data model elements
            if ( tok.getValue().equals("_children") )
            {
               if ( mDescription.mChildren != null && mDescription.mChildren.size() > 0)
               {
                  // Make sure we are allowed to process this request
                  if ( mDescription.mShowChildren == true )
                  {
                     oInfo.mElement = new Children(mChildrenBindings);

                     DMElementDescriptor desc = 
                     new DMElementDescriptor("_children", null, null);
                     oInfo.mElement.setDescription(desc);
                  }
                  else
                  {
                     result = DMErrorCodes.DOES_NOT_HAVE_CHILDREN;
                  }
               }
               else
               {
                  result = DMErrorCodes.DOES_NOT_HAVE_CHILDREN;
               }
            }
            else if ( tok.getValue().equals("_count") )
            {
               if ( mRecords != null)
               {
                  oInfo.mElement = new Count(mRecords.size());

                  DMElementDescriptor desc = 
                  new DMElementDescriptor("_count", null, null);
                  oInfo.mElement.setDescription(desc);
               }
               else
               {
                  result = DMErrorCodes.DOES_NOT_HAVE_COUNT;
               }
            }
            else if ( tok.getValue().equals("_version") )
            {
               // This should have been handled by the DataModel 
               result = DMErrorCodes.DOES_NOT_HAVE_VERSION;
            }
            else
            {
               // Make sure we are not expecting an index
               if ( mRecords != null && mRecords.size() > 0)
               {
                  result = DMErrorCodes.UNDEFINED_ELEMENT;
               }
               else
               {
                  // Make sure this element has children
                  if ( mChildrenBindings != null && mChildrenBindings.size() > 0)
                  {
                     // Look for this element in the children set
                     int idx = mChildrenBindings.indexOf(tok.getValue());

                     if ( idx != -1 )
                     {

                        DMElement element =
                        (DMElement)mChildren.get(tok.getValue());

                        DMElementDescriptor desc = element.getDescription();

                        // Is this a SetValue or a GetValue request
                        if ( ioRequest.isGetValueRequest() )
                        {

                           oInfo.mElement = element;
                        }
                        else // SetValue()
                        {

                           boolean ok = true;

                           // Enforce data model dependencies, if they exist
                           if ( desc.mDependentOn != null && desc.mDependentOn.size() > 0)
                           {

                              for ( int i = 0; i < desc.mDependentOn.size();
                                  i++ )
                              {
                                 String check = (String)
                                                desc.mDependentOn.get(i);

                                 // Ensure the dependent element is initialized
                                 DMElement e = (DMElement)mChildren.get(check);

                                 if ( e != null )
                                 {
                                    if ( !e.isInitialized() )
                                    {
                                       // Dependend element is not initalized
                                       result =
                                       DMErrorCodes.DEP_NOT_ESTABLISHED;

                                       ok = false;
                                       break;
                                    }
                                 }
                                 else
                                 {
                                    // The specified dependent element does not
                                    // exist -- unknown state
                                    result = DMErrorCodes.UNKNOWN_EXCEPTION;

                                    ok = false;
                                    break;
                                 }
                              }
                           }

                           // Should we attempt to set this element?
                           if ( ok )
                           {
                              oInfo.mElement = element;
                           }
                        }
                     }
                     else
                     {
                        result = DMErrorCodes.UNDEFINED_ELEMENT;
                     }     
                  }
                  else
                  {
                     // This element has no children
                     result = DMErrorCodes.UNDEFINED_ELEMENT;
                  }
               }
            }
         }
         else if ( tok.getType() == RequestToken.TOKEN_INDEX )
         {

            // Process the index token
            int idx = Integer.parseInt(tok.getValue());

            // Is this a SetValue or a GetValue request
            if ( ioRequest.isGetValueRequest() )
            {
               // Check if the request record has already been created
               if ( idx < mRecords.size() )
               {
                  // Provide the requested record
                  oInfo.mElement = (DMElement)mRecords.get(idx);
               }
               else
               {
                  // index out of range error
                  result = DMErrorCodes.OUT_OF_RANGE;
               }
            }
            else // SetValue()
            {
               // Check if this record is in a valid range
               if ( idx >= mRecords.size() )
               {
                  // Is this a new record request
                  if ( idx == mRecords.size() && 
                       !(mDescription.mMaximum &&
                         mRecords.size() == mDescription.mSPM) )
                  {
                     boolean ok = true;

                     // Enforce data model dependencies

                     // We can assume that we only need to look at the next
                     // token because the dependency will be defined on that
                     // token's element
                     RequestToken lookAt = ioRequest.getCurToken();
                     boolean found = false;
                     boolean isWriteable = true;

                     if ( lookAt != null )
                     {
                        DMElementDescriptor desc = null;

                        // Look for this element in the children of the record
                        // NOTE: This implementation assumes there are no
                        // nested arrays -- SCORM 2004 does not require them
                        for ( int i = 0; 
                              i < mDescription.mChildren.size(); i++ )
                        {
                           desc = 
                           (DMElementDescriptor)
                           mDescription.mChildren.get(i);

                           if ( desc.mBinding.equals(lookAt.getValue()) )
                           {
                              // Found it, so we're done
                              found = true;
                              isWriteable = desc.mIsWriteable;

                              break;
                           }
                        }

                        if ( !found )
                        {
                           // The request child element does not exist, so 
                           // there is no reason to create the new record
                           result = DMErrorCodes.UNDEFINED_ELEMENT;

                           ok = false;
                        }
                        else
                        {
                           if ( desc.mDependentOn != null && desc.mDependentOn.size() > 0)
                           {
                              // Dependend element is not initalized
                              result = DMErrorCodes.DEP_NOT_ESTABLISHED;

                              ok = false;
                           }
                        }
                     }
                     else
                     {
                        // No next token, so there is no reason to create the
                        // new record
                        result = DMErrorCodes.UNDEFINED_ELEMENT;

                        ok = false;
                     }

                     if ( ok )
                     {
                        // Only create the new record if the array is not 
                        // read-only and is not an admin request
                        if ( isWriteable || ioRequest.isAdminRequest() )
                        {

                           // Create the new record
                           DMElementDescriptor desc =
                           (DMElementDescriptor)mDescription.clone();

                           desc.mOldSPM = desc.mSPM;
                           desc.mSPM = -1;

                           SCORM_2004_DMElement element = 
                           new SCORM_2004_DMElement(desc, this, mDM);

                           mRecords.add(element);

                           // Provide the requested record
                           oInfo.mElement = element;
                           oInfo.mRecords = mRecords;

                           // Check if the new size exceeds the SPM
                           if ( mRecords.size() > mDescription.mSPM )
                           {
                              // Collection SPM Exceeded, create warning.
                              String dn = getDotNotation((DataModel)mDM);
                              String warn = "Collection SPM exceeded";

                              // Add the SPM Exceeded warning 
                              //    to the message log
                              DetailedLogMessageCollection.getInstance().addMessage(
                                 new LogMessage(MessageType.WARNING, warn));
                           }
                        }
                        else
                        {
                           // Don't create this record when trying to set a
                           // read-only child
                           result = DMErrorCodes.READ_ONLY;
                        }
                     }
                  }
                  else
                  {
                     if ( mRecords.size() == mDescription.mSPM &&
                          mDescription.mMaximum )
                     {
                        // Exceeds absolute maximum -- nothing was created
                        result = DMErrorCodes.MAX_EXCEEDED;
                     }
                     else
                     {
                        // index out of of order
                        result = DMErrorCodes.SET_OUT_OF_ORDER;
                     }
                  }
               }
               else
               {
                  // Provide the record requested
                  oInfo.mElement = (DMElement)mRecords.get(idx);
               }
            }
         }
         else
         {

            // Wrong type of token, this shouldn't happen
            result = DMErrorCodes.UNKNOWN_EXCEPTION;
         }
      }
      else
      {
         // No more tokens, this shouldn't happen
         result = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return result;
   }

   /**
    * Attempt to set the value of this data model element to the value 
    * indicated by the dot-notation token.
    * 
    * @param iValue A token (<code>RequestToken</code>) object that provides 
    *               the value to be set and may include a set of delimiters.
    * 
    * @param iAdmin Indicates if this operation is administrative or not.  If
    *               the operation is administrative, read/write and data type
    *               characteristics of the data model element should be
    *               ignored.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int setValue(RequestToken iValue, boolean iAdmin)
   {

      String oldValue = null;

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // We are not allowed to write to a collection
      if ( mDescription.mSPM != -1 )
      {
         result = DMErrorCodes.UNDEFINED_ELEMENT;
      }
      else
      {
         // If this is not an administrative action, validate the data type
         if ( !iAdmin )
         {
            // Make sure we are allowed to set this element
            if ( mDescription.mIsWriteable )
            {
               result = validate(iValue);
            }
            else
            {
               result = DMErrorCodes.READ_ONLY;
            }
         }
      }

      // If no validation errors, set the indicated value
      if ( result == DMErrorCodes.NO_ERROR )
      {
         // Remember the old value
         oldValue = mValue;

         // Clear the current value
         mValue = "";

         // Remember where the last 'good' delimiter was located
         int i = 0;

         if ( mDelimiters != null && mDelimiters.size() > 0)
         {
            boolean found = true;
            Vector set = new Vector();

            // Set all delimiters to their defaults
            for ( int j = 0; j < mDelimiters.size(); j++ )
            {
               DMDelimiter del = (DMDelimiter)mDelimiters.get(j);

               del.mValue = null;

               // Remember that we have not set this delimiter yet
               set.add(new Boolean(false));
            }

            // Attempt to set all delimiters provided with this value
            for ( ; i < iValue.getDelimiterCount() && found; i++ )
            {
               RequestDelimiter del = iValue.getDelimiterAt(i);

               // Assume this delimiter is not defined for the element
               found = false;

               // Check if this element includes the specified delimiter
               for ( int j = 0; j < mDelimiters.size() && !found; j++ )
               {
                  DMDelimiter toSet = (DMDelimiter)mDelimiters.get(j);

                  if ( toSet.mDescription.mName.equals(del.getName()) )
                  {
                     // Make sure we haven't set this delimiter yet
                     boolean setAlready =                                       
                     ((Boolean)set.get(j)).booleanValue();

                     if ( !setAlready )
                     {
                        found = true;
                        set.add(j, new Boolean(false));

                        // Check if the value is the delimiter's default
                        if ( toSet.mDescription.mDefault != null )
                        {
                           if ( !toSet.mDescription.
                                mDefault.equals(del.getValue()) )
                           {
                              String val = del.getValue();

                              // Check to see if we need to trucate the value
                              if ( mTruncSPM && 
                                   toSet.mDescription.mValueSPM != -1 )
                              {
                                 val = val.
                                    substring(0,
                                              toSet.
                                                 mDescription.mValueSPM - 1);
                              }

                              // Set the delimiter's value
                              toSet.mValue = val;
                           }
                           else
                           {
                              // Reset the default default
                              toSet.mValue = null;
                           }
                        }
                        else
                        {
                           // Set the delimiter's value
                           toSet.mValue = del.getValue();
                        }
                     }

                     break;
                  }
               }
            }

            // If we didn't find the delimiter, move back one
            if ( !found )
            {
               i--;
            }
         }

         // Add all 'undefined' delimiters to the validation string
         for ( ; i < iValue.getDelimiterCount(); i++ )
         {
            RequestDelimiter del = iValue.getDelimiterAt(i);

            mValue += del.showDotNotation();
         }

         // Add the token value to the validation string
         mValue += iValue.getValue();

         // Check to see if we need to trucate the value
         if ( mTruncSPM && (mDescription.mValueSPM != -1) )
         {
            if ( mDescription.mValueSPM == -2 )
            {
               mValue = 
                  InteractionTrunc.trunc(mValue, 
                                         ((InteractionValidator)
                                            mDescription.mValidator).getType());
            }
            else
            {
               if ( mValue.length() > mDescription.mValueSPM )
               {
                  mValue = mValue.substring(0, mDescription.mValueSPM);
               }
            }
         }

//       This data model element is now initialized
         mInitialized = true;
         
         //  Setting descriptors based on interaction type
         //  This is a special case.  The interaction type element affects
         //  the SPM and datatype of the correct_responses and learner_response
         if ( mDescription.mBinding.equals("type") )
         {
            // Make sure the type acctually changed
            if ( !mInitialized || !mValue.equals(oldValue) )
            {
               // Get the cmi.interatctions.x container descriptor
               DMElementDescriptor desc = mParent.getDescription();

               // Look for correct_responses
               for ( int j = 0; j < desc.mChildren.size(); j++ )
               {
                  DMElementDescriptor curChild = 
                  (DMElementDescriptor)desc.mChildren.get(j);

                  DMElementDescriptor child = null;

                  // Test to see if we need to clone the child descriptor
                  // If so, use a clone for the following if-switch
                  // and set the ith index of the mChildren array when finished
                  // newDesc.mChildren.replaceAt(j, clone);

                  if ( curChild.mBinding.equals("correct_responses") )
                  {

                     child = (DMElementDescriptor)curChild.clone();

                     // Switch the SPM and data type of the correct_responses
                     handleCorrectResponses(child);  

                     // Create an element of the appropriate type & replace 
                     SCORM_2004_DMElement element = 
                     new SCORM_2004_DMElement(child, mParent, mDM);

                     mParent.putChild(child.mBinding, element);
                  }
                  else if ( curChild.mBinding.equals("learner_response") )
                  {

                     child = (DMElementDescriptor)curChild.clone();

                     // Switch the SPM and data type of the learner_response
                     handleLearnerResponse(child);

                     // Create an element of the appropriate type & replace 
                     SCORM_2004_DMElement element = 
                     new SCORM_2004_DMElement(child, mParent, mDM);

                     mParent.putChild(child.mBinding, element);
                  }
               }
            }
         }
      }

      

      return result;
   }

   /**
    * Validates a dot-notation token against this data model's defined data
    * type.
    * 
    * @param iValue A token (<code>RequestToken</code>) object that provides
    *               the value to be checked, possibily including a set of
    *               delimiters.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int validate(RequestToken iValue)
   {

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

      // This is the string we are going to validate
      String toValidate = null;

      // Remember where the last 'good' delimiter was located
      int i = 0;

      if ( mDelimiters != null && mDelimiters.size() > 0)
      {
         Vector checked = new Vector();

         // Set all delimiters to 'not checked'
         for ( int j = 0; j < mDelimiters.size(); j++ )
         {
            checked.add(new Boolean(false));
         }

         boolean found = true;

         // Validate all delimiters provided with this value
         for ( ; i < iValue.getDelimiterCount() && found; i++ )
         {
            RequestDelimiter del = iValue.getDelimiterAt(i);

            // Assume this delimiter is not defined for the element
            found = false;

            // Check for invalid whitespace around a 'known' delimiter
            String trim = del.getName(); /* .trim(); */
            boolean ws = trim.length() != del.getName().length();

            // Check if this element includes the specified delimiter
            for ( int j = 0; j < mDelimiters.size() && !found; j++ )
            {
               DMDelimiter toCheck = (DMDelimiter)mDelimiters.get(j);

               if ( toCheck.mDescription.mName.equals(trim) )
               {
                  // Make sure we haven't already checked this delimter
                  boolean alreadyChecked = 
                  ((Boolean)checked.get(j)).booleanValue();

                  if ( !alreadyChecked )
                  {
                     // If a known delimiter has whitespace, flag the error
                     if ( ws )
                     {
                        result = DMErrorCodes.TYPE_MISMATCH;
                        break;
                     }
                     else
                     {
                        // Remember we've checked this delimiter
                        found = true;
                        checked.set(j, new Boolean(true));

                        // Validate the delimiter's value
                        if ( toCheck.mDescription.mValidator != null )
                        {
                           result = toCheck.mDescription.
                                    mValidator.validate(del.getValue());

                           // If any of the delimiters are invalid, we are done
                           if ( result != DMErrorCodes.NO_ERROR )
                           {
                              break;
                           }
                        }
                     }
                  }
                  else
                  {
                     // We already found this delimiter
                     break;
                  }
               }
            }

            if ( result != DMErrorCodes.NO_ERROR )
            {
               break;
            }
         }

         // If we didn't find this delimiter, move back one in those provided
         if ( !found )
         {
            i--;
         }
      }

      // Validate the value if all of the delimiters are valid
      if ( result == DMErrorCodes.NO_ERROR )
      {
         toValidate = "";

         // Add all 'undefined' delimiters to the validation string
         for ( ; i < iValue.getDelimiterCount(); i++ )
         {
            RequestDelimiter del = iValue.getDelimiterAt(i);

            toValidate += del.showDotNotation();
         }

         // Add the token value to the validation string
         toValidate += iValue.getValue();

         if ( mDescription.mValidator != null )
         {
            result = mDescription.mValidator.validate(toValidate);

            if ( result == DMErrorCodes.SPM_EXCEEDED )
            {
               // Type validation SPM Exceeded, create warning.
               String warn = mDescription.mValidator.getTypeName() +
                             " SPM exceeded";

               // Add the SPM Exceeded warning to the message log
               DetailedLogMessageCollection.getInstance().addMessage(
                  new LogMessage(MessageType.WARNING, warn));

               // Clear this error
               result = DMErrorCodes.NO_ERROR;
            }
         }
      }

      return result;
   }

   /**
    * Handles setting an interaction's learner_response element datatype and
    * SPM based on the interaction type element.
    * 
    * @param ioChild The <code>DMElementDescriptor</code> for the interaction
    *               element whose type is being changed.
    */
   private void handleLearnerResponse(DMElementDescriptor ioChild)
   {

      // We know what data model element we will be validating, so inform the
      // validator.
      String e = "cmi.interactions.n.learner_response";

      DMDelimiterDescriptor del = null;

      if ( mValue.equals("true-false") )
      {
         String [] boolVocab = {"true", "false"};
         ioChild.mValidator = new VocabularyValidator(boolVocab);
      }
      else if ( mValue.equals("choice") )
      {
         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.MULTIPLE_CHOICE, e);
      }
      else if ( mValue.equals("fill-in") )
      {
         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.FILL_IN, e);
      }
      else if ( mValue.equals("long-fill-in") )
      {
         // Add the lang delimiter
         del = new DMDelimiterDescriptor("lang", "en",
                                         SCORM_2004_DM.SHORT_SPM,
                                         new LangStringValidator());
         ioChild.mDelimiters = new Vector();
         ioChild.mDelimiters.add(del);

         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.LONG_FILL_IN, e);
      }
      else if ( mValue.equals("likert") )
      {
         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.LIKERT, e);
      }
      else if ( mValue.equals("matching") )
      {
         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.MATCHING, e);
      }
      else if ( mValue.equals("performance") )
      {
         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.PERFORMANCE, false, e);
      }
      else if ( mValue.equals("sequencing") )
      {
         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.SEQUENCING, e);
      }
      else if ( mValue.equals("numeric") )
      {
         ioChild.mValueSPM = -2;

         ioChild.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.NUMERIC, e);
      }
      else if ( mValue.equals("other") )
      {
         ioChild.mValueSPM = SCORM_2004_DM.LONG_SPM;
         ioChild.mValidator = new SPMRangeValidator(SCORM_2004_DM.LONG_SPM);
      }
   }

   /**
    * Handles setting an interaction's correct_responses element datatype and
    * SPM based on the interaction type element.
    * 
    * @param ioChild The <code>DMElementDescriptor</code> for the interaction
    *                element whose type is being changed.
    */ 
   private void handleCorrectResponses(DMElementDescriptor ioChild)
   {

      // We know what data model element we will be validating, so inform the
      // validator.
      String e = "cmi.interactions.n.correct_responses.n.pattern";

      DMDelimiterDescriptor del = null;

      // Find 'pattern' child to make the correct descriptor changes
      DMElementDescriptor curPattern = 
      (DMElementDescriptor)ioChild.mChildren.get(0);

      DMElementDescriptor pattern = 
      (DMElementDescriptor)curPattern.clone();

      // Clear attributes that may have been cloned from another type
      pattern.mIsUnique = false;
      pattern.mDelimiters = null;
      pattern.mValidator = null;

      if ( pattern.mBinding.equals("pattern") )
      {
         if ( mValue.equals("true-false") )
         {
            ioChild.mSPM = 1;
            ioChild.mMaximum = true;

            String [] boolVocab = {"true", "false"};
            pattern.mValidator = new VocabularyValidator(boolVocab);
         }
         else if ( mValue.equals("choice") )
         {
            ioChild.mSPM = 10;
            ioChild.mMaximum = false;

            pattern.mIsUnique = true;
            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.MULTIPLE_CHOICE, e);
         }
         else if ( mValue.equals("fill-in") )
         {
            // Add the case_matters delimiter
            String [] boolVocab = {"true", "false"};
            del = 
            new DMDelimiterDescriptor("case_matters", "false",
                                      new VocabularyValidator(boolVocab));
            pattern.mDelimiters = new Vector();
            pattern.mDelimiters.add(del);

            // Add the order_matters delimiter
            del = 
            new DMDelimiterDescriptor("order_matters", "true",
                                      new VocabularyValidator(boolVocab));
            pattern.mDelimiters.add(del);

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.FILL_IN, e);

            // Set the spm
            ioChild.mSPM = 5;
            ioChild.mMaximum = false;
         }
         else if ( mValue.equals("long-fill-in") )
         {
            // Add the case_matters delimiter
            String [] boolVocab = {"true", "false"};
            del = 
            new DMDelimiterDescriptor("case_matters", "false",
                                      new VocabularyValidator(boolVocab));

            pattern.mDelimiters = new Vector();
            pattern.mDelimiters.add(del);

            // Add the lang delimiter
            del =
            new DMDelimiterDescriptor("lang", "en",
                                      new LangStringValidator());
            pattern.mDelimiters.add(del);

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.LONG_FILL_IN, e);

            // Set the patterns spm
            ioChild.mSPM = 5;
            ioChild.mMaximum = false;
         }
         else if ( mValue.equals("likert") )
         {
            ioChild.mSPM = 1;
            ioChild.mMaximum = true;

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.LIKERT, e);
         }
         else if ( mValue.equals("matching") )
         {
            ioChild.mSPM = 5;
            ioChild.mMaximum = false;

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.MATCHING, false, e);
         }
         else if ( mValue.equals("performance") )
         {
            // Add the order_matters delimiter
            String [] boolVocab = {"true", "false"};
            del = 
            new DMDelimiterDescriptor("order_matters", "true",
                                      new VocabularyValidator(boolVocab));
            pattern.mDelimiters = new Vector();
            pattern.mDelimiters.add(del);

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.PERFORMANCE, false, e);

            ioChild.mSPM = 5;
            ioChild.mMaximum = false;
         }
         else if ( mValue.equals("sequencing") )
         {
            ioChild.mSPM = 5;
            ioChild.mMaximum = false;

            pattern.mIsUnique = true;

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.SEQUENCING, e);
         }
         else if ( mValue.equals("numeric") )
         {
            ioChild.mSPM = 1;
            ioChild.mMaximum = true;

            pattern.mValueSPM = -2;

            pattern.mValidator = mDM.getValidatorFactory().newInteractionValidator(InteractionValidator.NUMERIC, e);
         }
         else if ( mValue.equals("other") )
         {
            ioChild.mSPM = 1;
            ioChild.mMaximum = true;

            pattern.mValueSPM = SCORM_2004_DM.LONG_SPM;

            pattern.mValidator = new 
               SPMRangeValidator(SCORM_2004_DM.LONG_SPM);
         }
      }
      else
      {
         // Really bad error condition
      }

      // Replace existing descriptor
      ioChild.mChildren.remove(0);
      ioChild.mChildren.add(0, pattern);


   }

}  // end SCORM_2004_DMElement
