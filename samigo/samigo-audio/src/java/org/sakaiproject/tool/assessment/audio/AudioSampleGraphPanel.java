/**********************************************************************************
 * $URL$
 * $Id$
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

/* AudioSamplePanel.java
 * Originally based on code from CapturePlayback.java
 * @(#)CapturePlayback.java	1.11	99/12/03
 *
 * portions Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package org.sakaiproject.tool.assessment.audio;

import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import javax.swing.JPanel;

/**
 * Render a WaveForm.
 */
public class AudioSampleGraphPanel
  extends JPanel
{

	private static final long serialVersionUID = 0L;

	static ResourceBundle res = AudioUtil.getInstance().getResourceBundle();

	static ColorModel colorModel= new ColorModel();

  private static final Font font10 = new Font("serif", Font.PLAIN, 10);
  private static final Font font12 = new Font("serif", Font.PLAIN, 12);
  private static final Color graphColor = colorModel.getColor("graphColor");//new Color(0, 180, 20);
  private static final Color currentPositionColor = colorModel.getColor("graphCurrentPositionColor");// Color(64, 200, 20);
  private static final Color backgroundColor = colorModel.getColor("graphBackgroundColor");//new Color(0, 128, 20);
  private static final Color gridColor = colorModel.getColor("graphGridColor");//  new Color(0, 140, 20);


  public AudioSampleGraphPanel()
  {
    setBackground(backgroundColor);
  }

  public void reportGraphStatus(String msg)
  {
    System.out.println("Status: " + msg);
  }

  public void createWaveForm(
    byte[] audioBytes, Vector<Line2D> lines, AudioInputStream audioInputStream)
  {

    lines.removeAllElements(); // clear the old vector

    AudioFormat format = audioInputStream.getFormat();
    if (audioBytes == null)
    {
      try
      {
        audioBytes = new byte[
          (int) (audioInputStream.getFrameLength()
                 * format.getFrameSize())];
        audioInputStream.read(audioBytes);
      }
      catch (Exception ex)
      {
        reportGraphStatus(ex.toString());
        return;
      }
    }

    Dimension d = getParent().getSize();
    int w = d.width;
    int h = d.height - 15;
    int[] audioData = null;
    if (format.getSampleSizeInBits() == 16)
    {
      int nlengthInSamples = audioBytes.length / 2;
      audioData = new int[nlengthInSamples];
      if (format.isBigEndian())
      {
        for (int i = 0; i < nlengthInSamples; i++)
        {
          /* First byte is MSB (high order) */
          int MSB = (int) audioBytes[2 * i];
          /* Second byte is LSB (low order) */
          int LSB = (int) audioBytes[2 * i + 1];
          audioData[i] = MSB << 8 | (255 & LSB);
        }
      }
      else
      {
        for (int i = 0; i < nlengthInSamples; i++)
        {
          /* First byte is LSB (low order) */
          int LSB = (int) audioBytes[2 * i];
          /* Second byte is MSB (high order) */
          int MSB = (int) audioBytes[2 * i + 1];
          audioData[i] = MSB << 8 | (255 & LSB);
        }
      }
    }
    else if (format.getSampleSizeInBits() == 8)
    {
      int nlengthInSamples = audioBytes.length;
      audioData = new int[nlengthInSamples];
      if (format.getEncoding().toString().startsWith(res.getString("PCM_SIGN")))
      {
        for (int i = 0; i < audioBytes.length; i++)
        {
          audioData[i] = audioBytes[i];
        }
      }
      else
      {
        for (int i = 0; i < audioBytes.length; i++)
        {
          audioData[i] = audioBytes[i] - 128;
        }
      }
    }

    int frames_per_pixel = audioBytes.length / format.getFrameSize() / w;
    byte my_byte = 0;
    double y_last = 0;
    int numChannels = format.getChannels();
    // we can normalize the waveform in the display by finding the signal peak
    // and then we calculate a scale factor to use when drawing
    int signalPeak = 0;
    for (double x = 0; x < w && audioData != null; x++)
    {
      int idx = (int) (frames_per_pixel * numChannels * x);
      if (format.getSampleSizeInBits() == 8)
      {
        my_byte = (byte) audioData[idx];
      }
      else
      {
        my_byte = (byte) (128 * audioData[idx] / 32768);
      }
      if (Math.abs(my_byte) > signalPeak) {
    	  signalPeak = Math.abs(my_byte);
      }
    }
    double scaleFactor = 128 / (double)signalPeak;
    
    for (double x = 0; x < w && audioData != null; x++)
    {
      int idx = (int) (frames_per_pixel * numChannels * x);
      if (format.getSampleSizeInBits() == 8)
      {
        my_byte = (byte) audioData[idx];
      }
      else
      {
        my_byte = (byte) (128 * audioData[idx] / 32768);
      }
      double y_new = (double) (h * (128 - (my_byte * scaleFactor)) / 256);
      lines.add(new Line2D.Double(x, y_last, x, y_new));
      y_last = y_new;
    }

    repaint();
  }

  public void paintData(Graphics g, AudioSamplingData data)
  {
    Vector<Line2D> lines;
    AudioInputStream audioInputStream = data.getAudioInputStream();
    String errStr;
    Runnable capture;
    Thread captureThread;
    double seconds;
    String fileName;
    double duration;
    double maxSeconds;

    lines = data.getLine();
    errStr = data.getErrStr();
    capture = data.getCapture();
    captureThread = data.getCaptureThread();
    seconds = data.getSeconds();
    fileName = data.getFileName();
    duration = data.getDuration();
    maxSeconds = data.getMaxSeconds();
    //System.out.println("*** seconds="+seconds);
    //System.out.println("*** duration="+duration);

    Dimension d = getSize();
    int w = d.width;
    int h = d.height;
    int INFOPAD = 15;

    Graphics2D g2 = (Graphics2D) g;
    g2.setBackground(getBackground());
    g2.clearRect(0, 0, w, h);
    g2.setColor(colorModel.getColor("darkColor"));
    g2.fillRect(0, h - INFOPAD, w, INFOPAD);

    int gridHeight = h - INFOPAD - 2;
    drawGrid(g2, w, gridHeight);

    if (errStr != null)
    {
      drawErrorText(errStr, w, g2);
    }
    else if (captureThread != null)
    {
      if (seconds > maxSeconds)
        drawLengthText(maxSeconds, h, g2);
      else
        drawLengthText(seconds, h, g2);
    }
    else
    {
      if (duration > maxSeconds)
        drawFileLengthText(seconds, fileName, maxSeconds, h, g2);
      else
        drawFileLengthText(seconds, fileName, duration, h, g2);

      if (audioInputStream != null)
      {
        drawSamplingGraph(lines, g2);

        if (seconds != 0)
        {
          drawCurrentPosition(seconds, duration, w, h, INFOPAD, g2);
        }
      }
    }
  }

  private void drawGrid(Graphics2D g2, int w, int h)
  {
    g2.setColor(gridColor);
    for (int x = 0; x < w; x += 10)
    {
      g2.draw(new Line2D.Double(x, 0, x, h));
    }
    for (int y = 0; y < h; y += 10)
    {
      g2.draw(new Line2D.Double(0, y, w, y));
    }
  }

  private void drawCurrentPosition(double seconds, double duration, int w,
                                    int h, int INFOPAD, Graphics2D g2)
  {
    double loc = seconds / duration * w;
    g2.setColor(currentPositionColor);
    g2.setStroke(new BasicStroke(3));
    g2.draw(new Line2D.Double(loc, 0, loc, h - INFOPAD - 2));
  }

  private void drawSamplingGraph(Vector<Line2D> lines, Graphics2D g2)
  {
    g2.setColor(graphColor);
    for (int i = 1; i < lines.size(); i++)
    {
      g2.draw( (Line2D) lines.get(i));
    }
  }

  private void drawFileLengthText(double seconds, String fileName, double duration,
                               int h, Graphics2D g2)
  {
    g2.setColor(graphColor);
    g2.setFont(font12);
    g2.drawString(res.getString("Length_1") +
                  String.valueOf(duration), 3, h - 4);
    /*
    g2.drawString(res.getString("File_") + fileName + "  " +
                  res.getString("Length_1") +
                  String.valueOf(duration) + "  " + res.getString("Position_") +
                  String.valueOf(seconds), 3, h - 4);
    */
  }

  private void drawLengthText(double seconds, int h, Graphics2D g2)
  {
    g2.setColor(graphColor);
    g2.setFont(font12);
    g2.drawString(res.getString("Length_") + String.valueOf(seconds), 3,
                  h - 4);
  }

  private void drawErrorText(String errStr, int w, Graphics2D g2)
  {
    g2.setColor(colorModel.getColor("alertColor"));
    g2.setFont(new Font(res.getString("g2_Font"), Font.BOLD, 20));
    g2.drawString(res.getString("ERROR"), 5, 20);
    AttributedString as = new AttributedString(errStr);
    as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
    AttributedCharacterIterator aci = as.getIterator();
    FontRenderContext frc = g2.getFontRenderContext();
    LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
    float x = 5, y = 25;
    lbm.setPosition(0);
    while (lbm.getPosition() < errStr.length())
    {
      TextLayout tl = lbm.nextLayout(w - x - 5);
      if (!tl.isLeftToRight())
      {
        x = w - tl.getAdvance();
      }
      tl.draw(g2, x, y += tl.getAscent());
      y += tl.getDescent() + tl.getLeading();
    }
  }

} // End class SamplingGraph
