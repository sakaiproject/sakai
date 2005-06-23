/**********************************************************************************
* $HeaderURL$
* $Id$
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
*                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://cvs.sakaiproject.org/licenses/license_1_0.html
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/

package org.sakaiproject.tool.assessment.settings;

import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.assessment.business.entity.Constants;

/**
 * @author <a href="mailto:lance@indiana.edu">Lance Speelmon </a>
 * @version $Id: ApplicationSettings.java,v 1.1 2004/06/17 19:20:21
 *          lancespeelmon Exp $
 */
public class ApplicationSettings
{
  private static final Log LOG = LogFactory.getLog(ApplicationSettings.class);
  private static boolean disableMultipleChoiceSingle = false;
  private static boolean disableMultipleChoiceMultiple = false;
  private static boolean disableMultipleChoiceSurvey = false;
  private static boolean disableShortAnswerEssay = false;
  private static boolean disableFillInTheBlank = false;
  private static boolean disableMatching = false;
  private static boolean disableTrueFalse = false;
  private static boolean disableAudioRecording = false;
  private static boolean disableFileUpload = false;
  private static boolean poolingUserAdminDisabled = false;
  private static boolean enableAutoSaveForGrading = false;
  private static String buildVersion;
  private static String buildTime;
  private static String buildTag;

  static
  {
    try
    {
      Properties p = PathInfo.getInstance().getSettingsProperties(
          Constants.SETTINGS_FILE);
      if (p == null)
      {
        throw new Error("Could not find settings file: "
            + Constants.SETTINGS_FILE);
      }

      Boolean b = null;
      // disableMultipleChoiceSingle
      b = new Boolean(p.getProperty("disableMultipleChoiceSingle", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableMultipleChoiceSingle = true");
        disableMultipleChoiceSingle = true;
      }
      // disableMultipleChoiceMultiple
      b = new Boolean(p.getProperty("disableMultipleChoiceMultiple", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableMultipleChoiceMultiple = true");
        disableMultipleChoiceMultiple = true;
      }
      // disableMultipleChoiceSurvey
      b = new Boolean(p.getProperty("disableMultipleChoiceSurvey", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableMultipleChoiceSurvey = true");
        disableMultipleChoiceSurvey = true;
      }
      // disableShortAnswerEssay
      b = new Boolean(p.getProperty("disableShortAnswerEssay", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableShortAnswerEssay = true");
        disableShortAnswerEssay = true;
      }
      // disableFillInTheBlank
      b = new Boolean(p.getProperty("disableFillInTheBlank", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableFillInTheBlank = true");
        disableFillInTheBlank = true;
      }
      // disableMatching
      b = new Boolean(p.getProperty("disableMatching", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableMatching = true");
        disableMatching = true;
      }
      // disableTrueFalse
      b = new Boolean(p.getProperty("disableTrueFalse", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableTrueFalse = true");
        disableTrueFalse = true;
      }
      // disableAudioRecording
      b = new Boolean(p.getProperty("disableAudioRecording", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableAudioRecording = true");
        disableAudioRecording = true;
      }
      // disableFileUpload
      b = new Boolean(p.getProperty("disableFileUpload", "false"));
      if (b.booleanValue())
      {
        LOG.info("disableFileUpload = true");
        disableFileUpload = true;
      }
      // poolingUserAdminDisabled
      b = new Boolean(p.getProperty("poolingUserAdminDisabled", "false"));
      if (b.booleanValue())
      {
        LOG.info("poolingUserAdminDisabled = true");
        poolingUserAdminDisabled = true;
      }

      // enableAutoSaveForGrading
      b = new Boolean(p.getProperty("enableAutoSaveForGrading", "false"));
      if (b.booleanValue())
      {
        LOG.info("enableAutoSaveForGrading= true");
        enableAutoSaveForGrading = true;
      }

      // build information
      final ResourceBundle rb = ResourceBundle
          .getBundle("org.navigoproject.build");
      buildVersion = rb.getString("build.version");
      buildTime = rb.getString("build.time");
      buildTag = rb.getString("build.tag");
      if (LOG.isInfoEnabled())
      {
        LOG.info("buildVersion=" + buildVersion);
        LOG.info("buildTime=" + buildTime);
        LOG.info("buildTag=" + buildTag);
      }
    }
    catch (IOException e)
    {
      LOG.fatal(e);
      throw new Error(e);
    }
  }

  /**
   * @return Returns the disableAudioRecording.
   */
  public static boolean isDisableAudioRecording()
  {
    return disableAudioRecording;
  }

  /**
   * @return Returns the disableFileUpload.
   */
  public static boolean isDisableFileUpload()
  {
    return disableFileUpload;
  }

  /**
   * @return Returns the disableFillInTheBlank.
   */
  public static boolean isDisableFillInTheBlank()
  {
    return disableFillInTheBlank;
  }

  /**
   * @return Returns the disableMatching.
   */
  public static boolean isDisableMatching()
  {
    return disableMatching;
  }

  /**
   * @return Returns the disableMultipleChoiceMultiple.
   */
  public static boolean isDisableMultipleChoiceMultiple()
  {
    return disableMultipleChoiceMultiple;
  }

  /**
   * @return Returns the disableMultipleChoiceSingle.
   */
  public static boolean isDisableMultipleChoiceSingle()
  {
    return disableMultipleChoiceSingle;
  }

  /**
   * @return Returns the disableMultipleChoiceSurvey.
   */
  public static boolean isDisableMultipleChoiceSurvey()
  {
    return disableMultipleChoiceSurvey;
  }

  /**
   * @return Returns the disableShortAnswerEssay.
   */
  public static boolean isDisableShortAnswerEssay()
  {
    return disableShortAnswerEssay;
  }

  /**
   * @return Returns the disableTrueFalse.
   */
  public static boolean isDisableTrueFalse()
  {
    return disableTrueFalse;
  }

  /**
   * @return Returns the poolingUserAdminDisabled.
   */
  public static boolean isPoolingUserAdminDisabled()
  {
    return poolingUserAdminDisabled;
  }

  /**
   * @return Returns the enableAutoSaveForGrading.
   */
  public static boolean isEnableAutoSaveForGrading()
  {
    return enableAutoSaveForGrading;
  }

  /**
   * @return Returns the buildTag.
   */
  public static String getBuildTag()
  {
    return buildTag;
  }

  /**
   * @return Returns the buildTime.
   */
  public static String getBuildTime()
  {
    return buildTime;
  }

  /**
   * @return Returns the buildVersion.
   */
  public static String getBuildVersion()
  {
    return buildVersion;
  }
}
