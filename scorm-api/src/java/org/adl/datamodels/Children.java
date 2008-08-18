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

package org.adl.datamodels;

import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * <strong>Filename:</strong> Children.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Encapsulates the _children keyword datamodel element defined in SCORM 1.2
 * and SCORM 2004.
 * 
 * @author ADL Technical Team
 */
public class Children extends DMElement
{
   /**
    * Describes the dot-notation binding string for this data model element.
    */
   private String mBinding = "_children";

   /**
    * Describes the children of some data model element as a vector.
    */
   private List mChildrenList = null;

   /**
    * Describes if the string returned from a <code>GetValue()</code>) method
    * is provided in a random order.
    */
   private boolean mRandomize = true;

   /**
    * Constructs a <code>_children</code> keyword data model element consisting
    * of a SCORM conformant set of child data model element names. If the 
    * list of children is empty (size <= 0), then a Illegal Argument Exception
    * is thrown.
    * 
    * @param iChildren Describes the list of children (dot-notation bindings)
    *                  of some data model element.
    * 
    * @param iRandom   Describes if the list of children will provided in a
    *                  random order when a <code>GetValue()</code> operation is
    *                  invoked.
    */ 
   public Children(List iChildren, boolean iRandom) 
   {
      if ( iChildren.size() > 0 )
      {
         mChildrenList = iChildren;
         mRandomize = iRandom;
      }
      else
      {
         throw new IllegalArgumentException("No Children Specified");
      }
   }

   /**
    * Constructs a <code>_children</code> keyword data model element consisting
    * of a SCORM conformant set of child data model element names. If the size
    * of the incoming Vector is <= 0, the method throws an Illegal Argument
    * Exception
    * 
    * @param iChildren Describes the list of children (dot-notation bindings)
    *                  of some data model element.
    * 
    */
   public Children(List iChildren)
   {
      if ( iChildren.size() > 0 )
      {
         mChildrenList = iChildren;
      }
      else
      {
         throw new IllegalArgumentException("No Children Specified");
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

      int result = DMErrorCodes.COMPARE_NOTHING;

      if ( iValue != null )
      {
         // Make sure there are no delimiters defined
         if ( iValue.getDelimiterCount() == 0 )
         {
            result = DMErrorCodes.COMPARE_EQUAL;

            StringTokenizer st = new StringTokenizer(iValue.getValue(), ",");

            // Make sure we are looking for all of the children
            if ( st.countTokens() == mChildrenList.size() )
            {
               Vector found = new Vector();
               for ( int i = 0; i < mChildrenList.size(); i++ )
               {
                  found.add(new Boolean(false));
               }

               boolean located = true;
               while ( st.hasMoreTokens() && located )
               {
                  String tok = st.nextToken();

                  located = false;
                  for ( int i = 0; i < mChildrenList.size(); i++ )
                  {
                	 String child = (String)((List)mChildrenList).get(i);

                     if ( (tok.trim()).equals(child.trim()) )
                     {
                        Boolean already = (Boolean)found.elementAt(i);

                        if ( !(already.booleanValue()) )
                        {
                           found.setElementAt(new Boolean(true), i);
                           located = true;

                           already = (Boolean)found.elementAt(i);

                           break;
                        }
                        else
                        {
                           // Invalid compare
                           break;
                        }
                     }
                  }

                  if ( !located )
                  {
                     result = DMErrorCodes.COMPARE_NOTEQUAL;
                     break;
                  }
               }

               if ( result != DMErrorCodes.COMPARE_NOTEQUAL )
               {
                  for ( int i = 0; i < mChildrenList.size(); i++ )
                  {
                     Boolean ok = (Boolean)found.elementAt(i);

                     if ( !(ok.booleanValue()) )
                     {
                        result = DMErrorCodes.COMPARE_NOTEQUAL;
                        break;
                     }
                  }
               }
            }
            else
            {
               result = DMErrorCodes.COMPARE_NOTEQUAL;
            }
         }
         else
         {
            result = DMErrorCodes.COMPARE_NOTEQUAL;  
         }
      }

      return result;
   }


   /**
    * Describes this data model element's binding string.
    * 
    * @return This data model element's binding string.  Note: The <code>
    *         String</code> returned only applies in the context of its
    *         containing data model or parent data model element.
    */
   public String getDMElementBindingString()
   {
      return mBinding;
   }

   /**
    * Attempt to get the value of this data model element, which may include
    * default delimiters.
    * 
    * @param iArguments  Describes the aruguments for this getValue() call.
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

      // Initialize the return string
      oInfo.mValue = new String("");

      // Should the provided value be randomized?
      if ( mRandomize )
      {
         Random gen = new Random();
         int rand = -1;
         int num = -1;

         Vector usedSet = new Vector();
         boolean ok = false;

         int lookUp;

         for ( int i = 0; i < mChildrenList.size(); i++ )
         {
            // Pick an unselected child
            ok = false;
            while ( !ok )
            {
               rand = gen.nextInt();
               num = Math.abs(rand % mChildrenList.size());

               lookUp = usedSet.indexOf(new Integer(num));

               // Add the new element to the list
               if ( lookUp == -1 )
               {
                  usedSet.add(new Integer(num));

                  if ( oInfo.mValue.equals("") )
                  {
                	  oInfo.mValue += (String)((List)mChildrenList).get(num);
                  }
                  else
                  {
                     oInfo.mValue += ",";
                     oInfo.mValue += (String)((List)mChildrenList).get(num);
                  }

                  // Continue ordering childern
                  ok = true;
               }
            }
         }
      }
      else
      {
         for ( int i = 0; i < mChildrenList.size(); i++ )
         {
            if ( oInfo.mValue.equals("") )
            {
            	oInfo.mValue += (String)((List)mChildrenList).get(i);
            }
            else
            {
               oInfo.mValue += ",";
               oInfo.mValue += (String)((List)mChildrenList).get(i);
            }
         }
      }

      // Getting a keyword data model element never fails.
      return DMErrorCodes.NO_ERROR;
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
      int error = DMErrorCodes.NO_ERROR;

      // If there are more tokens to be processed, we don't have that data
      // model element.
      if ( ioRequest.hasMoreTokens() )
      {
         error = DMErrorCodes.UNDEFINED_ELEMENT;
      }
      else
      {
         // If we here, something is wrong with the request
         error = DMErrorCodes.UNKNOWN_EXCEPTION;
      }

      return error;
   }

   /**
    * Attempt to set the value of this data model element to the value 
    * indicated by the dot-notation token.
    * 
    * @param iValue A token (<code>RequestToken</code>) object that provides the
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
      // Never are allowed to set a _children keyword element, even as an
      // admin action
      return DMErrorCodes.SET_KEYWORD;
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
      // Never have to validate _children, because it is a read-only element
      return DMErrorCodes.NO_ERROR; 
   }

}  // end Children
