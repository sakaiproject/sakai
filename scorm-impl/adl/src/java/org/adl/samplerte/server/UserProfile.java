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

package org.adl.samplerte.server;

/**
 * The UserProfile class handles the data required for a user profile.<br><br>
 * 
 * <strong>Filename:</strong> UserProfile.java<br><br>
 * 
 * <strong>Design Issues:</strong><br>
 * This implementation is intended to be used by the SCORM Sample RTE <br>
 * <br>
 * 
 * <strong>References:</strong><br>
 * <ul>
 *     <li>IMS SS Specification
 *     <li>SCORM 2004 3rd Edition
 * </ul>
 * 
 * @author ADL Technical Team
 */
public class UserProfile 
{

   /**
    * The user's password.
    */
   public String mPassword = null;

   /**
    * The user's first name.
    */
   public String mFirstName = null;

   /**
    * The user's last name.
    */
   public String mLastName = null;


   /**
    * The user's ID.
    */
   public String mUserID = null;


   /**
    * The cmi.learner_preference.audio_level associated with this user.
    */
   public String mAudioLevel = null;


   /**
    * The cmi.learner_preference.audio_captioning associated with this user.
    */
   public String mAudioCaptioning = null;


   /**
    * The cmi.learner_preference.language associated with this user.
    */
   public String mLanguage = null;


   /**
    * The cmi.learner_preference.delivery_speed associated with this user.
    */
   public String mDeliverySpeed = null;
   
   /**
    * The admin status associated with this user. True if user is an admin.
    */
   public boolean mAdmin = false;

}
   

