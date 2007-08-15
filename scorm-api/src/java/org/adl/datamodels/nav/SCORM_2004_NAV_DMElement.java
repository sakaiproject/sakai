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

package org.adl.datamodels.nav;

import org.adl.datamodels.DMElement;
import org.adl.datamodels.DMElementDescriptor;
import org.adl.datamodels.RequestToken;
import org.adl.datamodels.DMErrorCodes;
import org.adl.datamodels.RequestDelimiter;
import org.adl.datamodels.DMDelimiterDescriptor;
import org.adl.datamodels.DMDelimiter;
import org.adl.datamodels.DMProcessingInfo;
import org.adl.datamodels.DMRequest;
import org.adl.datamodels.Children;
import org.adl.datamodels.Count;

import org.adl.datamodels.datatypes.URIValidator;

import org.adl.sequencer.ADLTOC;

import java.util.Hashtable;
import java.util.Vector;

import java.io.Serializable;

/**
 * <br><br>
 * 
 * <strong>Filename:</strong> SCORM_2004_NAV_DMElement.java<br><br>
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
 *     <li>SCORM 2004</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class SCORM_2004_NAV_DMElement extends DMElement implements Serializable
{

   /** 
	* Describes the navigation data model this element is a member of.
	*/
   protected SCORM_2004_NAV_DM  mDM = null;
	
   /**
    * Default constructor required for serialization support.
    */
   public SCORM_2004_NAV_DMElement() 
   {
     // The default constructor does not explicitly define a functionallity   
   }

   /**
    * Constructs one data model element of this the SCORM 2004 navigation
    * data model.
    * 
    * @param iDescription A description of this data model element.
    * 
    * @param iParent The parent of this data model element.
    * 
    * @param iDM The SCORM 2004 navagation data model this element
    *            is a member of.
    */
   public SCORM_2004_NAV_DMElement(DMElementDescriptor iDescription,
                                   DMElement iParent,
                                   SCORM_2004_NAV_DM iDM)
   {
      // Set this element's description
      mDescription = iDescription;

      // Set the parent
      mParent = iParent;

      // Set the data model
      mDM = iDM;

      // Check if this element is managing records -- is it an array container?
      if ( mDescription.mSPM != -1 && mDescription.mChildren != null )
      {
         // Initialize the set of records
         mRecords = new Vector();
      }

      // Check if this element has children
      if ( mDescription.mChildren != null )
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
               SCORM_2004_NAV_DMElement element = 
                  new SCORM_2004_NAV_DMElement(desc, this, iDM);
               mChildren.put(desc.mBinding, element);
            }
         }
      }
      else
      {

         // This must be a leaf element, so initialize 
         if ( mDescription.mDelimiters != null )
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

            mValue = new String("");
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
    * 
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

         // Make sure the value is valid against this element's type
         result = validate(iValue);

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
            if ( mDelimiters != null )
            {
               Vector checked = new Vector();

               // Set all delimiters to 'not checked'
               for ( int j = 0; j < mDelimiters.size(); j++ )
               {
                  checked.add(new Boolean(false));
               }

               boolean found = true;

               // Compare all delimiters provided with this value
               for ( ; i < iValue.getDelimiterCount() && found && equal; i++ )
               {
                  RequestDelimiter del = iValue.getDelimiterAt(i);

                  // Assume this delimiter is not defined for the element
                  found = false;

                  // Check if this element includes the specified delimiter
                  for ( int j = 0; j < mDelimiters.size(); j++ )
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
                                    equal = equal &&
                                       toCheck.mDescription.
                                       mValidator.
                                       compare(toCheck.mDescription.mDefault,
                                               del.getValue(),
                                               null);
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
                                                       null);
                              }
                           }
                        }

                        break;
                     }
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
                           equal = false;
                        }
                     }
                  }
               }
            }

            // Only compare the data model values if its delimiters where equal
            if ( equal )
            {
               String compareWith = iValue.getValue();

               // Add all 'undefined' delimiters to the validation string
               for ( ; i < iValue.getDelimiterCount(); i++ )
               {
                  RequestDelimiter del = iValue.getDelimiterAt(i);

                  compareWith += del.showDotNotation();
               }

               // If no comparison method is defined, just do a string compare
               if ( mDescription.mValidator == null )
               {
                  equal = compareWith.equals(mValue);
               }
               else
               {
                  equal = mDescription.mValidator.compare(compareWith, 
                                                          mValue, 
                                                          null);
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
    * @param iArguments Describes the arguments for this getValue() call.
    * 
    * @param iAdmin Describes if this request is an administrative action.
    * 
    * @param iDelimiters Indicates if the data model element's default
    *                    delimiters should be included in the return string.
    * 
    * @param oInfo Provides the value of this data model element.
    *              <b>Note: The caller of this function must provide an
    *              initialized (new) <code>DMProcessingInfo</code> to
    *              hold the return value.</b>
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

      String target = null;

      // If this request includes an arguement, make sure this is the
      // 'choice' data model element
      if ( iArguments != null )
      {

         if ( mDescription.mBinding.equals("request") )
         {
            // Only validation requests have arguments
            result = DMErrorCodes.INVALID_REQUEST;
         }
         else
         {
            if ( mDescription.mBinding.equals("choice") )
            {
               // There can be only one argument
               if ( iArguments.getDelimiterCount() == 1 )
               {
                  // Look for the 'target' argument
                  RequestDelimiter del = iArguments.getDelimiterAt(0);

                  if ( del.getName().equals("target") )
                  {
                     target = del.getValue();

                     // Make sure the 'target' is a valid URI
                     URIValidator uri = new URIValidator();
                     result =  uri.validate(target);
                  }
                  else
                  {
                     // 'target' is the only valid argument for 'choice'
                     result = DMErrorCodes.INVALID_ARGUMENT;
                  }
               }
               else
               {
                  // There can only be one argument for 'choice'
                  result = DMErrorCodes.INVALID_ARGUMENT;
               }
            }
            else
            {
               // This should not occur
               result = DMErrorCodes.UNDEFINED_ELEMENT;
            }
         }
      }

      if ( result == DMErrorCodes.NO_ERROR  ||
           result == DMErrorCodes.SPM_EXCEEDED )
      {
         if ( mDescription.mBinding.equals("request") )
         {
            oInfo.mValue = "";

            // If this is a 'choice' request add the target delimiter
            if ( mValue.equals("choice") )
            {
               if ( mDelimiters != null )
               {
                  DMDelimiter del = (DMDelimiter)mDelimiters.get(0);

                  oInfo.mValue += del.getDotNotation(iDelimiters);
               }
               else
               {
                  // Really bad error
                  result = DMErrorCodes.UNKNOWN_EXCEPTION;
               }
            }

            // Return the current request
            oInfo.mValue += mValue;
         }
         else
         {
            // If this is a 'choice' request -- make sure we have a target
            if ( mDescription.mBinding.equals("choice") && target == null )
            {
               // There MUST be one argument for 'choice'
               result = DMErrorCodes.INVALID_ARGUMENT;
            }
            else
            {
               // Parent data model elments do not directly store data
               if ( mValue != null )
               {
                  // Assume the result is unknown
                  oInfo.mValue = "unknown";

                  if ( mDM.getNavRequests() != null )
                  {
                     if ( mDescription.mBinding.equals("continue") )
                     {
                        if ( mDM.getNavRequests().isContinueEnabled() ||
                             mDM.getNavRequests().isContinueExitEnabled() )
                        {
                           oInfo.mValue = "true";
                        }
                        else
                        {
                           oInfo.mValue = "false";
                        }
                     }
                     else if ( mDescription.mBinding.equals("previous") )
                     {
                        if ( mDM.getNavRequests().isPreviousEnabled() )
                        {
                           oInfo.mValue = "true";
                        }
                        else
                        {
                           oInfo.mValue = "false";
                        }
                     }
                     else
                     {
                        if ( mDM != null && mDM.getNavRequests().getChoice() != null )
                        {
                           boolean ok = (ADLTOC)mDM.getNavRequests().getChoice().get(target) != null;

                           if ( ok )
                           {
                              oInfo.mValue = "true";
                           }
                           else
                           {
                              oInfo.mValue = "false";
                           }
                        }
                     }
                  }
               }
               else
               {
                  // This should not occur
                  result = DMErrorCodes.UNDEFINED_ELEMENT;
               }
            }
         }
      }

      return result; 
   }

   /**
    * Processes a data model request on this data model element.  This method
    * will enforce data model element depedencies and keyword application.
    *
    * @param ioRequest Provides the dot-notation request being applied to thi
    *                  data model element.  The <code>DMRequest</code> will b
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
               if ( mDescription.mChildren != null )
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
               if ( mRecords != null )
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
               result = DMErrorCodes.UNDEFINED_ELEMENT;
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
                  if ( mChildrenBindings != null )
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
                           if ( desc.mDependentOn != null )
                           {

                              for ( int i = 0; i < desc.mDependentOn.size();
                                  i++ )
                              {
                                 String check = (String)
                                 desc.mDependentOn.get(i);

                                 // Ensure the dependent element is initializ
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
                                    // The specified dependent element does n
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
            int idx = Integer.parseInt( tok.getValue() );

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

                        // Look for this element in the children of the recor
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
                           // The request child element does not exist, so th
                           // is no reason to create the new record
                           result = DMErrorCodes.UNDEFINED_ELEMENT;

                           ok = false;
                        }
                        else
                        {
                           if ( desc.mDependentOn != null )
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
                        {                           // Create the new record
                           DMElementDescriptor desc =
                              (DMElementDescriptor)mDescription.clone();
                           desc.mSPM = -1;

                           SCORM_2004_NAV_DMElement element =
                              new SCORM_2004_NAV_DMElement(desc, this, mDM);

                           mRecords.add(element);

                           // Check if the new size exceeds the SPM
                           if ( mRecords.size() > mDescription.mSPM )
                           {
                              result = DMErrorCodes.SPM_EXCEEDED;
                           }

                           // Provide the requested record
                           oInfo.mElement = element;

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
    *               value to be set and may include a set of delimiters.
    *
    * @param iAdmin Indicates if this operation is administrative or not.  If
    *               The operation is administrative, read/write and data type
    *               characteristics of the data model element should be
    *               ignored.
    *
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int setValue(RequestToken iValue, boolean iAdmin)
   {

      // TODO THis local variable is not used, if we don't need it lets get rid of it.
      String oldValue = null;

      // Assume no processing errors
      int result = DMErrorCodes.NO_ERROR;

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

      // If no validation errors, set the indicated value
      if ( result == DMErrorCodes.NO_ERROR ||
           result == DMErrorCodes.SPM_EXCEEDED )
      {
         // Remember the old value
         oldValue = mValue;

         // Clear the current value
         mValue = new String("");

         // Remember where the last 'good' delimiter was located
         int i = 0;

         if ( mDelimiters != null )
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
               for ( int j = 0; j < mDelimiters.size(); j++ )
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
                              // Set the delimiter's value
                              toSet.mValue = del.getValue();
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
         }

         // Add all 'undefined' delimiters to the validation string
         for ( ; i < iValue.getDelimiterCount(); i++ )
         {
            RequestDelimiter del = iValue.getDelimiterAt(i);

            mValue += del.showDotNotation();
         }

         // Add the token value to the validation string
         mValue += iValue.getValue();

         // This data model element is now initialized
         mInitialized = true;

         // If this is element is indicating a navigation request for the SCO
         // set the current navigation request
         if ( mDescription.mBinding.equals("request") )
         {
            // If the navigation request is 'choice' use the target instead
            if ( mValue.equals("choice") )
            {
               // Find the 'target' delimiter -- there should be only one
               DMDelimiter target = (DMDelimiter)mDelimiters.get(0);

               mDM.setNavRequest(target.mValue);
            }
            else
            {
               mDM.setNavRequest(mValue);
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

      if ( mDescription.mDelimiters != null )
      {
         boolean found = true;

         // Validate all delimiters provided with this value
         for ( ; i < iValue.getDelimiterCount() && found; i++ )
         {
            RequestDelimiter del = iValue.getDelimiterAt(i);

            // Assume this delimiter is not defined for the element
            found = false;

            // Check if this element includes the specified delimiter
            for ( int j = 0; j < mDescription.mDelimiters.size(); j++ )
            {
               DMDelimiterDescriptor desc =
                  (DMDelimiterDescriptor)mDescription.mDelimiters.get(j);

               if ( desc.mName.equals(del.getName()) )
               {
                  found = true;

                  // Validate the delimiter's value
                  if ( desc.mValidator != null )
                  {
                     result = desc.mValidator.validate(del.getValue());
                  }

                  break;
               }
            }

            // If any of the delimiters is invalid, we are done
            if ( result != DMErrorCodes.NO_ERROR )
            {
               break;
            }
         }
      }

      // Validate the value if all of the delimiters are valid
      if ( result == DMErrorCodes.NO_ERROR )
      {
         toValidate = new String("");

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
         }
      }

      return result;
   }

}  // end SCORM_2004_NAV_DMElement
