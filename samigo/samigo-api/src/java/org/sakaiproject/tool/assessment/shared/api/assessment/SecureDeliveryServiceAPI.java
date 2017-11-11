/**
 * Copyright (c) 2005-2011 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sakaiproject.tool.assessment.shared.api.assessment;

import java.util.Locale;
import java.util.SortedSet;

import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.assessment.data.ifc.assessment.PublishedAssessmentIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.RegisteredSecureDeliveryModuleIfc;
import org.sakaiproject.tool.assessment.data.ifc.assessment.SecureDeliveryModuleIfc;


/**
 * 
 * @author Luis Camargo (lcamargo@respondus.com)
 *
 */
public interface SecureDeliveryServiceAPI {
	
	public static final String MODULE_KEY = "SECURE_DELIVERY_MODULE_KEY";
	public static final String EXITPWD_KEY = "SECURE_DELIVERY_EXITPWD_KEY";
	public static final String TITLE_DECORATION = "SECURE_DELIVERY_TITLE_DECORATION";
	public static final String NONE_ID = "SECURE_DELIVERY_NONE_ID";
	
	public enum PhaseStatus { SUCCESS, FAILURE };	
	public enum Phase { ASSESSMENT_START, ASSESSMENT_FINISH, ASSESSMENT_REVIEW }; 
	 
	
	/**
	 * @return true if at least one secure delivery module (other than NONE_ID) is available.
	 * 
	 * A secure delivery module is available if the plugin that provides it was
	 * successfully loaded.
	 */
	public boolean isSecureDeliveryAvaliable();
	
	/**
	 * @param moduleId
	 * @return true if the module with moduleId is available. NONE_ID is always available.
	 */
	public boolean isSecureDeliveryModuleAvailable( String moduleId );
	
	/**
	 * @return A set of RegisteredSecureDeliveryModuleIfc entries with the module name internationalized 
	 * for the given locale. 
	 * 
	 * The list always includes NONE_ID as its first element. Others are ordered alphabetically by 
	 * their name. 
	 */
	public SortedSet<RegisteredSecureDeliveryModuleIfc> getSecureDeliveryModules( Locale locale );
	
	/**
	 * @param moduleId
	 * @param locale
	 * @return The title decoration for the given module and locale. Returns empty string if moduleId is NONE_ID,
	 *         null, not available or disabled.
	 *         
	 * The title decoration provides a visual indication that the assesment requires a particular
	 * secure delivery module. 
	 * 
	 */
	public String getTitleDecoration( String moduleId, Locale locale );
	
	/**
	 * Checks with the module specified by moduleId if the current delivery phase can continue. Returns
	 * SUCCESS if moduleId is null or NONE_ID or if the module is no longer available or disabled.
	 * 
	 * @param moduleId
	 * @param phase
	 * @param assessment
	 * @param request
	 * @return
	 */
	public PhaseStatus validatePhase( String moduleId, Phase phase, PublishedAssessmentIfc assessment, HttpServletRequest request );
	
	/**
	 * Returns the initial HTML fragments for all active modules. The fragments are inserted into
	 * the assessment list. 
	 * 
	 * @param request
	 * @param locale
	 * @return
	 */
	public String getInitialHTMLFragments( HttpServletRequest request, Locale locale );
	
	/**
	 * Returns an HTML appropriate for the combination of parameters. The fragment is injected
	 * during delivery. Returns empty string if module id is null or NONE_ID or if the module is no longer
	 * available or disabled.
	 * 
	 * @param moduleId
	 * @param assessment
	 * @param request
	 * @param phase
	 * @param status
	 * @param locale
	 * @return
	 */
	public String getHTMLFragment( String moduleId, PublishedAssessmentIfc assessment, HttpServletRequest request, Phase phase, PhaseStatus status, Locale locale );
	
	/**
	 * Uses the module specified to encrypt the exit password before storing it on the assessment settings. The
	 * encryption method used is up to the module implementation. Returns the same password if module id is null or 
	 * NONE_ID or if the module is no longer available.
	 * 
	 * @param moduleId
	 * @param password
	 * @return the encrypted password
	 */
	public String encryptPassword( String moduleId, String password );
	
	/**
	 * Uses the module specified to decrypt the exit password. The encryption method used is up to the module
	 * implementation. Returns the same password if module id is null or NONE_ID or if the module is no longer
	 * available.
	 * 
	 * @param moduleId
	 * @param password
	 * @return the plain text password
	 */
	public String decryptPassword( String moduleId, String password );
	
	
	/**
	 * Helper method to obtain a reference to the runtime instance of the module specified. The context object 
	 * provided is passed to the module itself for validation and the reference is only returned if the module
	 * validation is successful. 
	 * 
	 * How the actual context type and how it's validated is up to each module implementation.   
	 * 
	 * @param moduleId
	 * @param context
	 * @return the reference. null if the module is not avaliable or if the module rejected the context 
	 */
	public SecureDeliveryModuleIfc getModuleReference( String moduleId, Object context );
}
