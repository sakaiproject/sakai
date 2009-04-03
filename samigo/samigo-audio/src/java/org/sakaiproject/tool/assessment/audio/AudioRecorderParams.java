/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/sam/tags/sakai_2-2-002/audio/src/java/org/sakaiproject/tool/assessment/audio/AudioRecorderParams.java $
 * $Id: AudioRecorderParams.java 9270 2006-05-10 21:38:40Z daisyf@stanford.edu $
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007, 2008 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  private boolean enableSave = true;
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
  private String imageUrl="";
  private String agentId="";
  private String questionId="";
  private String localeLanguage="";
  private String localeCountry="";

  // -1 indicate that attemptsRemaining is not set, it should be set when question is loaded the 1st time.
  private int attemptsRemaining = -1; 
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
         ex.printStackTrace();
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
        ex.printStackTrace();
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
        ex.printStackTrace();
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
        ex1.printStackTrace();
      }
    }
    s = applet.getParameter("attemptsRemaining");
    if (s != null && !("").equals(s))
    {
      try
      {
        attemptsRemaining  = Integer.parseInt(s);
      }
      catch (NumberFormatException ex1)
      {
        ex1.printStackTrace();
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
        ex1.printStackTrace();
      }
    }

    s = applet.getParameter("imageUrl");
    if (s != null)
    {
      imageUrl = s;
    }

    s = applet.getParameter("agentId");
    if (s != null)
    {
      agentId = s;
    }
    
    s = applet.getParameter("questionId");
    if (s != null)
    {
      questionId = s;
    }
    
    s = applet.getParameter("localeLanguage");

    if (s != null)
    {
      localeLanguage = s;
      AudioUtil.getInstance().setLocaleLanguage(s);
    }
    
    s = applet.getParameter("localeCountry");

    if (s != null)
    {
      localeCountry = s;
      AudioUtil.getInstance().setLocaleCountry(s);
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
  
  public boolean isEnableSave() 
  {
	  return enableSave;
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
  
  public void setEnableSave(boolean enableSave)
  {
	  this.enableSave = enableSave;
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

  public String getImageUrl()
  {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl)
  {
    this.imageUrl = imageUrl;
  }

  public String getAgentId()
  {
    return agentId;
  }

  public void setAgentId(String agentId)
  {
    this.agentId = agentId;
  }
  
  public String getLocaleLanguage()
  {
    return localeLanguage;
  }

  public void setLocaleLanguage(String localeLanguage)
  {
    this.localeLanguage = localeLanguage;
  }
  
  public String getLocaleCountry()
  {
    return localeCountry;
  }

  public void setLocaleCountry(String localeCountry)
  {
    this.localeCountry = localeCountry;
  }

public String getQuestionId() {
	return questionId;
}

public void setQuestionId(String questionId) {
	this.questionId = questionId;
}



}
