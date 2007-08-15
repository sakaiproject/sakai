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

package org.adl.datamodels;


/**
 * Defines the inteface to a run-time data model that is managed for a SCO.
 * <br><br>
 * 
 * <strong>Filename:</strong> DataModel.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * Provides a standard interface to access a data model's elments.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This is an abstract class.  Specific run-time data models should provide a 
 * concrete implementation.  The <code>DMFactory</code> should be updated to    
 * provide access to all concrete implementations.<br><br>
 * 
 * <strong>Implementation Issues:</strong> None<br><br>
 * 
 * <strong>Known Problems:</strong> None<br><br>
 * 
 * <strong>Side Effects:</strong> None<br><br>
 * 
 * <strong>References:</strong> SCORM 2004<br>
 *  
 * @author ADL Technical Team
 */
public abstract class DataModel implements IDataModel
{
	protected long id;
	
	
   /*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
   
    Public Methods
   
   -*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*/

   /**
    * Describes this data model's binding string.
    * 
    * @return This data model's binding string.
    */
   public abstract String getDMBindingString();


   /**
    * Provides the requested data model element.
    * 
    * @param iElement Describes the requested element's dot-notation bound name.
    * 
    * @return The <code>DMElement</code> corresponding to the requested element
    *         or <code>null</code> if the element does not exist in the data
    *         model.
    */
   public abstract DMElement getDMElement(String iElement);


   /**
    * Performs data model specific initialization.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public abstract int initialize();


   /**
    * Performs data model specific termination.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public abstract int terminate();


   /**
    * Processes a SetValue() request against this data model.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public abstract int setValue(DMRequest iRequest);


   /**
    * Processes an equals() request against this data model. Compares two 
    * values of the same data model element for equality.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @param iValidate Indicates if the provided value should be validated.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public abstract int equals(DMRequest iRequest, boolean iValidate);


   /**
    * Processes an equals() request against this data model. Compares two 
    * values of the same data model element for equality.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public int equals(DMRequest iRequest)
   {
      return equals(iRequest, true);
   }


   /**
    * Processes a validate() request against this data model. Checks the value
    * provided for validity for the specified element.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public abstract int validate(DMRequest iRequest);


   /**
    * Processes a GetValue() request against this data model.
    * 
    * @param iRequest The request (<code>DMRequest</code>) being processed.
    * 
    * @param oInfo    Provides the value returned by this request.
    * 
    * @return An abstract data model error code indicating the result of this
    *         operation.
    */
   public abstract int getValue(DMRequest iRequest, DMProcessingInfo oInfo);


   /**
    * Displays the contents of the entire data model.
    */
   public abstract void showAllElements();


} // end DataModel
