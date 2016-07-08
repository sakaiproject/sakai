/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.emailtemplateservice.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.io.InputStream;

import org.sakaiproject.emailtemplateservice.model.EmailTemplate;
import org.sakaiproject.emailtemplateservice.model.EmailTemplateLocaleUsers;
import org.sakaiproject.emailtemplateservice.model.RenderedTemplate;

public interface EmailTemplateService {

   /**
    * Field for the current user's email
    */
   public static final String CURRENT_USER_EMAIL = "currentUserEmail";

   /**
    * field for current user's first name
    */
   public static final String CURRENT_USER_FIRST_NAME ="currentUserFirstName";


   /**
    * Field for current user's last name
    */
   public static final String CURRENT_USER_LAST_NAME = "currentUserLastName";

   /**
    * Field for the current user's diplay Name
    */
   public static final String CURRENT_USER_DISPLAY_NAME = "currentUserDisplayName";

   /**
    * Field for the current user's diplay Id
    *  
    */
   public static final String CURRENT_USER_DISPLAY_ID = "currentUserDisplayId";

   
   /**
    * Field for the local LMS name (e.g., CamTools, CTools, Vula)
    */
   public static final String LOCAL_SAKAI_NAME="localSakaiName";
   
   /**
    *  Field for the local LMS url
    */
   public static final String LOCAL_SAKAI_URL="localSakaiURL";
   
   /**
    *  Support email address
    */
   public static final String LOCAL_SAKAI_SUPPORT_MAIL = "localSupportMail";


   /**
    * Get an email template in the default locale
    * @param key the template key which identifies a template
    * @param locale the locale for the template
    * @return an email template with this key or null if none can be found
    */
   public EmailTemplate getEmailTemplate(String key, Locale locale);

   /**
    * Get a list of all the email templates in the system
    * @param max limit the number of items returned (0 for all)
    * @param start start with this item (0 for the first one)
    * @return a list of templates
    */
   public List<EmailTemplate> getEmailTemplates(int max, int start);

   /**
    * Get a rendered Template in the specied locale 
    * @param key the template key which identifies a template
    * @param locale the locale for the template
    * @param replacementValues a set of replacement values which are in the map like so:<br/>
    * key => value (String => String)<br/>
    * username => aaronz<br/>
    * course_title => Math 1001 Differential Equations<br/>
    * @return the processed template
    */
   public RenderedTemplate getRenderedTemplate(String key, Locale locale, Map<String, String> replacementValues);

   /**
    * Get a template in the appropriate locale for a user - is the user has no preferred locale set the system default will be used
    * @param key the template key which identifies a template
    * @param userReference the unique reference for a user (e.g. /user/aaronz) 
    * @param replacementValues a set of replacement values which are in the map like so:<br/>
    * key => value (String => String)<br/>
    * username => aaronz<br/>
    * course_title => Math 1001 Differential Equations<br/>
    * @return the processed template
    */
   public RenderedTemplate getRenderedTemplateForUser(String key, String userReference, Map<String, String> replacementValues);

   /**
    * Save a template to storage
    * @param emailTemplate an email template, cannot be null
    */
   public void saveTemplate(EmailTemplate emailTemplate);

   /**
    * update a template
    * @param emailTemplate an email template, cannot be null
    */
   public void updateTemplate(EmailTemplate emailTemplate);
   
   /**
    * INTERNAL USE: get an email template by the unique id
    */
   public EmailTemplate getEmailTemplateById(Long id);

   /**
    * Utility method for getting templates for many users messages to many users
    * @param templateId
    * @param user references e.g. "/user/admin"
    * @return
    */
   public Map<EmailTemplateLocaleUsers, RenderedTemplate> getRenderedTemplates(String key, List<String> userReferences, Map<String, String> replacementValues); 
   
   
   /**
    * Utility method that will construct the Mesages and send them
    * @param key template key
    * @param userReferences list of user references (e.g /user/admin)
    * @param replacementValues
    */
   public void sendRenderedMessages(String key, List<String> userReferences, Map<String, String> replacementValues, String from, String fromName);

   /**
    * Takes the list of paths supplied and looks up the XML files using the services classloader. Each file is parsed
	* into an EmailTemplate and saved.
    * @param templatePaths A List of template path Strings
    */
   public void processEmailTemplates(List<String> templatePaths);
   
   
   /**
    * Export a given template as xml
    * @param key
    * @param locale
    * @return
    */
   public String exportTemplateAsXml(String key, Locale locale);
   
   
   /**
    * Does a template for the key exist in this locale?
    * @param key
    * @return
    */
   public boolean templateExists(String key, Locale locale);
   /**
   * Utility to send message to user, also when user has changed the email address.
   * @param userIds
   * @param emailAddresses
   * @param renderedTemplate
   * @param from
   * @param fromName
   */
   public void sendMessage(List<String> userIds, List<String> emailAddresses, RenderedTemplate renderedTemplate,  String from, String fromName );
   
   /**
   * Registers a new template with the service, defined by the given XML file
   * @param templateResourceStream the resource stream for the XML file
   * @param templateRegistrationKey the key (name) to register the template under
   * @return true if template registration was successful
   */
   public boolean importTemplateFromXmlFile(InputStream templateResourceStream, String templateRegistrationKey);
   
}
