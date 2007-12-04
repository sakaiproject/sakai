/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/
package org.adl.datamodels;

import java.io.Serializable;

public interface IDataModel extends Serializable {

	/**
	 * Describes this data model's binding string.
	 * 
	 * @return This data model's binding string.
	 */
	public String getDMBindingString();

	/**
	 * Provides the requested data model element.
	 * 
	 * @param iElement Describes the requested element's dot-notation bound name.
	 * 
	 * @return The <code>DMElement</code> corresponding to the requested element
	 *         or <code>null</code> if the element does not exist in the data
	 *         model.
	 */
	public DMElement getDMElement(String iElement);

	/**
	 * Performs data model specific initialization.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public int initialize();

	/**
	 * Performs data model specific termination.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public int terminate();

	/**
	 * Processes a SetValue() request against this data model.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public int setValue(DMRequest iRequest);

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
	public int equals(DMRequest iRequest, boolean iValidate);

	/**
	 * Processes an equals() request against this data model. Compares two 
	 * values of the same data model element for equality.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public int equals(DMRequest iRequest);

	/**
	 * Processes a validate() request against this data model. Checks the value
	 * provided for validity for the specified element.
	 * 
	 * @param iRequest The request (<code>DMRequest</code>) being processed.
	 * 
	 * @return An abstract data model error code indicating the result of this
	 *         operation.
	 */
	public int validate(DMRequest iRequest);

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
	public int getValue(DMRequest iRequest, DMProcessingInfo oInfo);

	/**
	 * Displays the contents of the entire data model.
	 */
	public void showAllElements();

}