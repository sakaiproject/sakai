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

package org.adl.datamodels.datatypes;

import java.io.Serializable;
import java.util.Vector;

import org.adl.datamodels.DMTypeValidator;



/**
 * <strong>Filename:</strong> InteractionValidator.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Provides support for the SCORM Data Model Interaction data types, as defined 
 * in the SCORM 2004.
 * 
 * @author ADL Technical Team
 */
public class InteractionValidator extends DMTypeValidator 
implements Serializable
{
   /**
    * Enumeration of possible interaction types.
    * <br>Multiple Choice
    * <br><b>1</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int MULTIPLE_CHOICE              =  1;


   /**
    * Enumeration of possible interaction types.
    * <br>Fill In
    * <br><b>2</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int FILL_IN                      =  2;

   /**
    * Enumeration of possible interaction types.
    * <br>Long Fill In
    * <br><b>3</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int LONG_FILL_IN                 =  3;

   /**
    * Enumeration of possible interaction types.
    * <br>Likert
    * <br><b>4</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int LIKERT                       =  4;

   /**
    * Enumeration of possible interaction types.
    * <br>Matching
    * <br><b>5</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int MATCHING                     =  5;

   /**
    * Enumeration of possible interaction types.
    * <br>Performance
    * <br><b>6</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int PERFORMANCE                  =  6;

   /**
    * Enumeration of possible interaction types.
    * <br>Sequencing
    * <br><b>7</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int SEQUENCING                   =  7;

   /**
    * Enumeration of possible interaction types.
    * <br>Numeric
    * <br><b>8</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int NUMERIC                      =  8;

   /**
    * Enumeration of possible interaction types.
    * <br>Unknown Type (Error)
    * <br><b>9</b>
    * <br>[DATAMODEL SUBSYSTEM CONSTANT]
    */
   public static final int UNKNOWN_TYPE                 =  9;


   /**
    * Type of interaction that will be validated by 
    * the <code>validate()</code> method.
    */
   protected int mInteractionType =  UNKNOWN_TYPE;


   /** 
    * Describes what data model element this validator belongs to.
    */
   protected String mElement = null;


   /**
    * Describes if an empty string is valid.
    */
   protected boolean mAllowEmpty = true;

   // For hibernate
   public InteractionValidator() { }
   
   /**
    * Constructor required for type initialization.
    * 
    * @param iType  Identifies the type of interaction being validated.
    * 
    * @param iElement  Describes the data model element this validator is 
    * associated with.
    */
   public InteractionValidator(int iType, String iElement) 
   { 
      mInteractionType = iType;

      // Set the element name for the type
      mType = iElement;
      mElement = iElement;
   } 

   /**
    * Constructor required for type initialization.
    * 
    * @param iType Identifies the type of interaction being validated.
    * 
    * @param iAllowEmpty Describes if this validator should allow an empty
    *  string (<code>""</code>).
    * 
    * @param iElement Describes the data model element this validator is 
    *  associated with.
    */
   public InteractionValidator(int iType, boolean iAllowEmpty, String iElement) 
   { 
      mInteractionType = iType;
      mAllowEmpty = iAllowEmpty;

      // Set the element name for the type
      mType = iElement;
      mElement = iElement;
   } 

   /** 
    * Provides the type of this validator
    * 
    * @return The type of interaction supported by this validator
    */
   public int getType()
   {
      return mInteractionType;
   }


   /**
    * Truncates the value to meet the DataType's SPM
    * 
    * @param  iValue  The value to be truncated
    * 
    * @return Returns the value truncated at the DataType's SPM
    */
   public String trunc(String iValue)
   {
      return InteractionTrunc.trunc(iValue, mInteractionType);
   }


   /**
    * Compares two valid data model elements for equality.
    * 
    * @param iFirst  The first value being compared.
    * 
    * @param iSecond The second value being compared.
    * 
    * @param iDelimiters The common set of delimiters associated with the
    * values being compared.
    * 
    * @return <code>true</code> if the two values are equal, otherwise
    *         <code>false</code>.
    */
   public boolean compare(String iFirst, String iSecond, Vector iDelimiters) { return false; }

   /**
    * Validates the provided string against a known format.
    * 
    * @param iValue The value being validated.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int validate(String iValue) { return -1; }

} // end InteractionValidator
