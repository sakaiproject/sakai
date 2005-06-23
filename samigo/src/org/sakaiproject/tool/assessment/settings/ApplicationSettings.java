/*
 *                       Navigo Software License
 *
 * Copyright 2003, Trustees of Indiana University, The Regents of the University
 * of Michigan, and Stanford University, all rights reserved.
 *
 * This work, including software, documents, or other related items (the
 * "Software"), is being provided by the copyright holder(s) subject to the
 * terms of the Navigo Software License. By obtaining, using and/or copying this
 * Software, you agree that you have read, understand, and will comply with the
 * following terms and conditions of the Navigo Software License:
 *
 * Permission to use, copy, modify, and distribute this Software and its
 * documentation, with or without modification, for any purpose and without fee
 * or royalty is hereby granted, provided that you include the following on ALL
 * copies of the Software or portions thereof, including modifications or
 * derivatives, that you make:
 *
 *    The full text of the Navigo Software License in a location viewable to
 *    users of the redistributed or derivative work.
 *
 *    Any pre-existing intellectual property disclaimers, notices, or terms and
 *    conditions. If none exist, a short notice similar to the following should
 *    be used within the body of any redistributed or derivative Software:
 *    "Copyright 2003, Trustees of Indiana University, The Regents of the
 *    University of Michigan and Stanford University, all rights reserved."
 *
 *    Notice of any changes or modifications to the Navigo Software, including
 *    the date the changes were made.
 *
 *    Any modified software must be distributed in such as manner as to avoid
 *    any confusion with the original Navigo Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 *
 * The name and trademarks of copyright holder(s) and/or Indiana University,
 * The University of Michigan, Stanford University, or Navigo may NOT be used
 * in advertising or publicity pertaining to the Software without specific,
 * written prior permission. Title to copyright in the Software and any
 * associated documentation will at all times remain with the copyright holders.
 * The export of software employing encryption technology may require a specific
 * license from the United States Government. It is the responsibility of any
 * person or organization contemplating export to obtain such a license before
 * exporting this Software.
 */

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
