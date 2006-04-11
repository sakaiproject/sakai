/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Portions copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
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
package org.sakaiproject.tool.assessment.audio;

import java.io.Serializable;
import java.applet.Applet;

/**
 *
 * <p> By default, we turn more things on than we would from an applet
  we support runnning as an application, too
</p>
 * <p>Description: </p>
 * <p>Sakai Project Copyright (c) 2005</p>
 * <p> </p>
 * @author Ed Smiley <esmiley@stanford.edu>
 *
 */
public class AudioRecorderParams implements Serializable
{
  private boolean enablePlay = true;
  private boolean enableRecord = true;
  private boolean enablePause = true;
  private boolean enableLoad = false;
  private boolean saveAu = true;
  private boolean saveWave = true;
  private boolean saveAiff = true;
  private boolean saveToFile = true;
  private boolean saveToUrl = false;
  private String fileName = "audio";
  private String url = "";
  private String compression = "linear";
  private int frequency = 8000;
  private int bits = 8;
  private boolean signed = true;
  private boolean bigendian = true;
  private boolean stereo = false;
  private int maxSeconds = 60;
  private int attemptsAllowed = 5;
  private int attemptsRemaining = 5;
  private int currentRecordingLength=0;

  /**
   * compression algorithms
   * btw using "u" for the greek letter "mu"
   * perhaps we should be calling this "mu-law" and showing that letter in UI.
   */
  public static final String compressionAllowed[] =
    {
    "ulaw",
    "alaw",
    "linear",
  };

  /**
   * sampling rates
   */
  public static final int frequenciesAllowed[] =
    {
    8000, 11025, 16000, 22050, 44100,
  };

  /**
   * 8 or 16 bit
   */
  public static int bitsAllowed[] =
    {
    8, 16,
  };

  /**
   * Support runnning as an application.  We turn off url and trn off save to
   * url.  Thsi has to be explicitly turned on.
   *
   */
  public AudioRecorderParams()
  {
    // keep all defaults
  }

  /**
   *
   * <p>From an applet we set all values that are specified in existing applet
   * parameters, the names and properties correspond.
   * </p>
   * @param applet the applet using these settings
   */
  public AudioRecorderParams(Applet applet)
  {
    // set values from applet parameters

    String s = applet.getParameter("enablePlay");
    if ("true".equalsIgnoreCase(s))
    {
      enablePlay = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      enablePlay = false;
    }

    s = applet.getParameter("enableRecord");
    if ("true".equalsIgnoreCase(s))
    {
      enableRecord = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      enableRecord = false;
    }

    s = applet.getParameter("enablePause");
    if ("true".equalsIgnoreCase(s))
    {
      enablePause = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      enablePause = false;
    }

    s = applet.getParameter("enableLoad");
    if ("true".equalsIgnoreCase(s))
    {
      enableLoad = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      enableLoad = false;
    }

    s = applet.getParameter("saveAu");
    if ("true".equalsIgnoreCase(s))
    {
      saveAu = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      saveAu = false;
    }

    s = applet.getParameter("saveWave");
    if ("true".equalsIgnoreCase(s))
    {
      saveWave = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      saveWave = false;
    }

    s = applet.getParameter("saveAiff");
    if ("true".equalsIgnoreCase(s))
    {
      saveAiff = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      saveAiff = false;
    }

    s = applet.getParameter("saveToFile");
    if ("true".equalsIgnoreCase(s))
    {
      saveToFile = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      saveToFile = false;
    }

    s = applet.getParameter("saveToUrl");
    if ("true".equalsIgnoreCase(s))
    {
      saveToUrl = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      saveToUrl = false;
    }

    s = applet.getParameter("fileName");
    if (s != null)
    {
      fileName = s;
    }
    s = applet.getParameter("url");
    if (s != null)
    {
      url = s;
    }

    s = applet.getParameter("compression");
    if (s != null)
    {
      for (int i = 0; i < this.compressionAllowed.length; i++)
      {
        if (compressionAllowed[i].equalsIgnoreCase(s))
        {
          compression = compressionAllowed[i];
        }
      }
    }

    s = applet.getParameter("frequency");
    if (s != null)
    {
      int f = 0;
      try
      {
        f = Integer.parseInt(s);
      }
      catch (NumberFormatException ex)
      {
        // leave
      }

      for (int i = 0; i < this.frequenciesAllowed.length; i++)
      {
        if (frequenciesAllowed[i] == f)
        {
          frequency = f;
        }
      }
    }

    s = applet.getParameter("bits");
    if (s != null)
    {
      int b = 0;
      try
      {
        b = Integer.parseInt(s);
      }
      catch (NumberFormatException ex)
      {
        // leave
      }

      for (int i = 0; i < this.bitsAllowed.length; i++)
      {
        if (bitsAllowed[i] == b)
        {
          bits = b;
        }
      }
    }

    s = applet.getParameter("signed");
    if ("true".equalsIgnoreCase(s))
    {
      signed = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      signed = false;
    }

    s = applet.getParameter("bigendian");

    if ("true".equalsIgnoreCase(s))
    {
      bigendian = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      bigendian = false;
    }

    s = applet.getParameter("stereo");
    if ("true".equalsIgnoreCase(s))
    {
      stereo = true;
    }
    else if ("false".equalsIgnoreCase(s))
    {
      stereo = false;
    }

    s = applet.getParameter("maxSeconds");
    if (s != null)
    {
      try
      {
        maxSeconds = Integer.parseInt(s);
      }
      catch (NumberFormatException ex)
      {
        // leave
      }
    }
    s = applet.getParameter("attemptsAllowed");
    if (s != null)
    {
      try
      {
        attemptsAllowed  = Integer.parseInt(s);
      }
      catch (NumberFormatException ex1)
      {
        // leave
      }
    }
    s = applet.getParameter("attemptsRemaining");
    if (s != null)
    {
      try
      {
        attemptsRemaining  = Integer.parseInt(s);
      }
      catch (NumberFormatException ex1)
      {
        // leave
      }
    }
    s = applet.getParameter("currentRecordingLength");
    if (s != null)
    {
      try
      {
        currentRecordingLength  = Integer.parseInt(s);
      }
      catch (NumberFormatException ex1)
      {
        // leave
      }
    }

  }

  public boolean isBigendian()
  {
    return bigendian;
  }

  public int getBits()
  {
    return bits;
  }

  public String getCompression()
  {
    return compression;
  }

  public boolean isEnableLoad()
  {
    return enableLoad;
  }

  public boolean isEnablePause()
  {
    return enablePause;
  }

  public boolean isEnablePlay()
  {
    return enablePlay;
  }

  public boolean isEnableRecord()
  {
    return enableRecord;
  }

  public String getFileName()
  {
    return fileName;
  }

  public int getFrequency()
  {
    return frequency;
  }

  public int getMaxSeconds()
  {
    return maxSeconds;
  }

  public int getCurrentRecordingLength()
  {
    return currentRecordingLength;
  }

  public int getAttemptsAllowed()
  {
    return attemptsAllowed;
  }

  public int getAttemptsRemaining()
  {
    return attemptsRemaining;
  }

  public boolean isSaveAiff()
  {
    return saveAiff;
  }

  public boolean isSaveAu()
  {
    return saveAu;
  }

  public boolean isSaveToFile()
  {
    return saveToFile;
  }

  public boolean isSaveToUrl()
  {
    return saveToUrl;
  }

  public boolean isSaveWave()
  {
    return saveWave;
  }

  public boolean isSigned()
  {
    return signed;
  }

  public boolean isStereo()
  {
    return stereo;
  }

  public String getUrl()
  {
    return url;
  }

  public void setUrl(String url)
  {
    this.url = url;
  }

  public void setStereo(boolean stereo)
  {
    this.stereo = stereo;
  }

  public void setSigned(boolean signed)
  {
    this.signed = signed;
  }

  public void setSaveWave(boolean saveWave)
  {
    this.saveWave = saveWave;
  }

  public void setSaveToUrl(boolean saveToUrl)
  {
    this.saveToUrl = saveToUrl;
  }

  public void setSaveToFile(boolean saveToFile)
  {
    this.saveToFile = saveToFile;
  }

  public void setSaveAu(boolean saveAu)
  {
    this.saveAu = saveAu;
  }

  public void setSaveAiff(boolean saveAiff)
  {
    this.saveAiff = saveAiff;
  }

  public void setAttemptsAllowed(int attemptsAllowed)
  {
    this.attemptsAllowed = attemptsAllowed;
  }

  public void setAttemptsRemaining(int attemptsRemaining)
  {
    this.attemptsRemaining = attemptsRemaining;
  }

  public void setMaxSeconds(int maxSeconds)
  {
    this.maxSeconds = maxSeconds;
  }

  public void setCurrentRecordingLength(int currentRecordingLength)
  {
    this.currentRecordingLength = currentRecordingLength;
  }

  public void setFrequency(int frequency)
  {
    this.frequency = frequency;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public void setEnableRecord(boolean enableRecord)
  {
    this.enableRecord = enableRecord;
  }

  public void setEnablePlay(boolean enablePlay)
  {
    this.enablePlay = enablePlay;
  }

  public void setEnablePause(boolean enablePause)
  {
    this.enablePause = enablePause;
  }

  public void setEnableLoad(boolean enableLoad)
  {
    this.enableLoad = enableLoad;
  }

  public void setCompression(String compression)
  {
    this.compression = compression;
  }

  public void setBits(int bits)
  {
    this.bits = bits;
  }

  public void setBigendian(boolean bigendian)
  {
    this.bigendian = bigendian;
  }
}
