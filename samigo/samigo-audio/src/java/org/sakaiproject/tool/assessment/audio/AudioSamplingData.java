/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright 2006 Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
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
import java.util.Vector;
import javax.sound.sampled.AudioInputStream;

public class AudioSamplingData
  implements Serializable
{
  private String errStr;
  private String fileName;
  private double seconds;
  private double duration;
  private Runnable capture;
  private Thread captureThread;
  private java.util.Vector line;
  private javax.sound.sampled.AudioInputStream audioInputStream;
  private double maxSeconds;

  public String getErrStr()
  {
    return errStr;
  }

  public void setErrStr(String errStr)
  {
    this.errStr = errStr;
  }

  public String getFileName()
  {
    return fileName;
  }

  public void setFileName(String fileName)
  {
    this.fileName = fileName;
  }

  public double getSeconds()
  {
    return seconds;
  }

  public void setSeconds(double seconds)
  {
    this.seconds = seconds;
  }

  public double getDuration()
  {
    return duration;
  }

  public void setDuration(double duration)
  {
    this.duration = duration;
  }

  public Runnable getCapture()
  {
    return capture;
  }

  public void setCapture(Runnable capture)
  {
    this.capture = capture;
  }

  public Thread getCaptureThread()
  {
    return captureThread;
  }

  public void setCaptureThread(Thread captureThread)
  {
    this.captureThread = captureThread;
  }
  public Vector getLine()
  {
    return line;
  }
  public void setLine(java.util.Vector line)
  {
    this.line = line;
  }
  public AudioInputStream getAudioInputStream()
  {
    return audioInputStream;
  }
  public void setAudioInputStream(javax.sound.sampled.AudioInputStream audioInputStream)
  {
    this.audioInputStream = audioInputStream;
  }

  public double getMaxSeconds()
  {
    return maxSeconds;
  }

  public void setMaxSeconds(double maxSeconds)
  {
    this.maxSeconds = maxSeconds;
  }

}
