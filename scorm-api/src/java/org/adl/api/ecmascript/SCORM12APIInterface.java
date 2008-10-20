/*******************************************************************************
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

package org.adl.api.ecmascript;

/**
 * Provides an interface for a SCO to communicate with the LMS.  This API is
 * defined in the SCORM Version 1.2 Run-Time Environment Book.<br><br>
 * 
 * <strong>Filename:</strong> SCORM12APIInterface.java<br><br>
 * 
 * <strong>Description:</strong><br>
 * This interface represents the API Interface for SCO to LMS communication as
 * defined in the SCORM Version 1.2.  This class contains all method signatures
 * that are available for SCORM Version 1.2 Conformant SCOs.<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * <br>
 * 
 * <strong>Implementation Issues:</strong><br>
 * It is the responsibility of the implementation of this interface to
 * perform any and all LMS behaviors including but not limited to error code 
 * management as defined in the SCORM Version 1.2.<br><br>
 * <b>Note: </b>Due to the nature and syntax of the SCORM API, this file does 
 * not follow the ADL Java Coding Standard for method names.<br><br>
 * 
 * <strong>Known Problems:</strong><br><br>
 * 
 * <strong>Side Effects:</strong><br><br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>SCORM 1.2</li>
 * </ul>
 * 
 * @author ADL Technical Team
 */
public interface SCORM12APIInterface
{

/**
 * This method indicates to the API Adapter that the SCO is going to 
 * communicate with the LMS.  It allows the LMS to handle LMS specific 
 * initialization issues.  It is a requirement of the SCO that it call this 
 * method before calling any other API methods.<br>
 * 
 * @param iParam 
 *      ""  An empty string must be passed for conformance to this standard.
 *      Values other than "" are reserved for future extensions.
 * 
 * @return String representing a boolean.
 *  <ul>
 *      <li> <code>true</code> result indicates that the LMSInitialize("") was 
 *       successful</li> 
 *      <li><code>false</code> result indicates that the LMSInitialize("") was 
 *       unsuccessful</li>
 *  </ul>
 * If a return value of <code>false</code> is returned, then this signifies 
 * to the SCO that the LMS is in an unknown state and that any additional  
 * API calls will not be processed by the LMS.
 */
   String LMSInitialize(String iParam);
   
/**
 * The SCO must call this when it has determined that it no longer 
 * needs to communicate with the LMS, if it successfully called 
 * <code>LMSInitialize</code> at any previous point.  This call signifies two 
 *       things:
 * <ol> 
 *    <li>The SCO can be assured that any data set using LMSSetValue() calls 
 *        has been persisted by the LMS.</li>
 *    <li>The SCO has finished communicating with the LMS.</li>  
 * </ol>
 *
 * @param iParam ""  An empty string must be passed for conformance to 
 * this standard.  Values other than "" are reserved for future extensions.
 * 
 * @return result of LMSFinish is a String representing a boolean.
 * <ul>
 *    <li><code>true</code> result indicates that the LMSFinish("") was 
 *        successful </li>
 *    <li><code>false</code> result indicates that the LMSFinish("") 
 *        was unsuccessful </li>
 * </ul>
 * If a return value of <code>true</code> is returned, then the SCO may no 
 * longer call any other API methods.If a return value of "false" is  
 * returned, then this signifies to the SCO that the LMS is in an unknown  
 * state and that any additional API calls may or may not be processed by 
 * the LMS.
 */
   String LMSFinish(String iParam);

/**
 * This method allows the SCO to obtain information from the LMS.  
 * It is used to determine:
 * <ul>
 *    <li>Values for various categories (groups) and elements in 
 *        the data model</li>
 *    <li>The version of the data model supported </li>
 *    <li>Whether a specific category or element is supported </li>
 *    <li>The number of items currently in an array or list of elements </li>
 * </ul>
 * The complete data element name and/or keywords are provided as a parameter.  
 * The current value of the requested data model parameter is returned.  
 * Only one value -- always a string -- is returned for each call.
 * 
 * @param iDataModelElement 
 * <ul>
 *    <li>datamodel.group.element  Returns the value of the named element</li> 
 *    <li>datamodel._version  The _version keyword is used to determine 
 *        the version of the data model supported by the LMS.</li>
 *    <li>datamodel.element._count The _count keyword is used to determine the 
 *        number of elements currently in an array.  The count is the 
 *        total number of elements in the array, not the index number of the 
 *        last position in the array.</li>
 *    <li>datamodel.element._children  The _children keyword is used to 
 *        determine all of the elements in a group or category that are 
 *        supported by the LMS.</li>
 * </ul>
 * 
 * @return All return values are strings.
 * 
 * <ul>
 *    <li>LMSGetValue(datamodel.group.element) The return value is a string 
 *        representing the current value of the requested element or 
 *        group.</li>
 *    <li>LMSGetValue(datamodel._version) The return value is a string 
 *        representing the version of the data model supported by the LMS.</li>
 *    <li>LMSGetValue(datamodel.group._children)  The return value is a 
 *        comma-separated list of all of the element names in the specified 
 *        group or category that are supported by the LMS. If an element has 
 *        no children, but is supported, an empty string ("") is returned.  
 *        An empty string ("") is also returned if an element is not supported.
 *        A subsequent request for last error [LMSGetLastError()] can determine 
 *        if the element is not supported.  The error "401 Not implemented 
 *        error" indicates the element is not supported.</li>
 *    <li>LMSGetValue(datamodel.group._count) The return value is an integer 
 *        that indicates the number of items currently   in an element list or 
 *        array.</li>
 * </ul>
 */
   String LMSGetValue(String iDataModelElement);
  
/**
 * This method allows the SCO to send information to the LMS.  
 * The API Adapter may be designed to immediately forward the 
 * information to the LMS, or it may be designed to forward information
 * based on some other approach.  This method is used to set the 
 * current values for various categories (groups) and elements in 
 * the data model. The data element name and its group are provided 
 * as a parameter.  The newly desired value of the data element is 
 * included as the second parameter in the call.  Only one value is 
 * sent with each call.
 * 
 * @param iDataModelElement This is the name of a fully qualified element 
 *        defined in the data model.  The argument is case sensitive.  The 
 *        argument is a string enclosed in quotes. The following represents 
 *        some forms this parameter may take. 
 * <ul>
 *    <li>datamodel.element This is the name of a category  
 *        or group defined in the Data Model. An example is 
 *        "cmi.comments".</li>
 *    <li>datamodel.group.element This is the name of an element 
 *        defined in the Data Model. An example is 
 *        "cmi.core.lesson_status".datamodel.group.n.element The value of the 
 *        sub-element in the nth-1 member of the element array 
 *        (zero-based indexing is used).</li>
 * </ul>
 * 
 * @param iValue The value to use for setting the data model element.
 * 
 * @return String representing a boolean
 * <ul>
 *    <li><code>true</code> result indicates that the LMSSetValue() was 
 *        successful </li> 
 *    <li><code>false</code> result indicates that the LMSSetValue() was 
 *        unsuccessful </li>
 * </ul>
 */
   String LMSSetValue(String iDataModelElement, String iValue);
   
/**
 * If the API Adapter is caching values received from the SCO via an 
 * <code>LMSSetValue()</code>, this call requires that any values not yet 
 * persisted by the LMS be persisted.  In some implementations, the API 
 * Adapter may persist set values as soon as they are received, and not cache 
 * them on the client.  In such implementations, this API call is redundant 
 * and would result in no additional action from the API Adapter.  This call 
 * ensures to the SCO that the data sent, via an <code>LMSSetValue()</code> 
 * call, will be persisted by the LMS upon completion of the LMSCommit().
 * 
 * @param iParam "".  An empty string must be passed for conformance to this 
 *        standard.  Values other than "" are reserved for future extensions.
 * 
 * @return String representing a boolean
 * <ul>
 *    <li><code>true</code> result indicates that the LMSCommit("") was 
 *        successful </li> 
 *    <li><code>false</code> result indicates that the LMSCommit("") was 
 *        unsuccessful </li>
 * </ul>
 * If a return value of <code>false</code> is returned, then this signifies 
 * to the SCO that the LMS is in an unknown state and that any additional API 
 * calls may or may not be processed by the LMS.
 */
   String LMSCommit(String iParam);
   
/**
 * The SCO must have a way of assessing whether or not any given API call 
 * was successful, and if it was not successful, what went wrong.  This 
 * method returns an error status code resulting from the previous API call.  
 * Each time an API method is called (with the exception of this one, 
 * <code>LMSGetErrorString</code>, and <code>LMSGetDiagnostic</code> -- the 
 * error methods), the error code is reset.  The SCO may call the error 
 * methods any number of times to retrieve the error code, and the code  
 * cannot change until the next API call is made.
 * 
 * @return The return values are Strings that can be converted to integer 
 *         numbers that identify errors falling into the following categories:
 * <ul>
 *    <li>   100's General errors</li>
 *    <li>   200's      Syntax errors</li>
 *    <li>   300's      LMS errors</li>
 *    <li>   400's      Data model errors</li>
 * </ul> 
 * The following codes are available for error messages:
 * <ul>
 *    <li>0     No error</li>
 *    <li>101   General exception </li>
 *    <li>201   Invalid argument error </li>
 *    <li>202   Element cannot have children </li>
 *    <li>203   Element not an array - cannot have count </li>
 *    <li>301   Not initialized</li>
 *    <li>401   Not implemented error </li>
 *    <li>402   Invalid set value, element is a keyword </li>
 *    <li>403   Element is read only </li>
 *    <li>404   Element is write only </li>
 *    <li>405   Incorrect Data Type </li>
 * </ul>
 */
   String LMSGetLastError();
   
/**
 * This method enables the content to obtain a textual description 
 * of the error represented by the error code number.
 * 
 * @param iErrorCode An integer number representing an error code.
 * 
 * @return A string that represents the verbal description of an error.
 */
   String LMSGetErrorString(String iErrorCode);
   
/**
 * This method enables vendor-specific error descriptions to be 
 * developed and accessed by the content.  These would normally 
 * provide additional detail regarding the error.
 * 
 * @param iErrorCode  The parameter may take one of two forms.  
 * <ul>
 *    <li>An integer number representing an error code.  
 *        This requests additional information on the listed error code.</li>
 *    <li>"".  An empty string.  
 *        This requests additional information on the last error that 
 *        occurred.</li>
 * </ul>
 * 
 * @return The return value is a string that represents any vendor-desired 
 *         additional information relating to either the requested error or 
 *         the last error.
 */
  String LMSGetDiagnostic(String iErrorCode);

 
}  // end SCORM12APIInterface
