/**********************************************************************************
 * $URL: https://source.sakaiproject.org/svn/trunk/sakai/sam/src/org/sakaiproject/tool/assessment/services/qti/QTIService.java $
 * $Id: QTIService.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
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
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
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
import java.awt.LayoutManager;

/**
 * Render a WaveForm.
 */
public class AudioSamplePanel
  extends JPanel
{
  private static final String RESOURCE_PACKAGE =
    "org.sakaiproject.tool.assessment.audio";
  private static final String RESOURCE_NAME = "AudioResources";
  static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PACKAGE + "." +
    RESOURCE_NAME, Locale.getDefault());

  private static final Font font10 = new Font("serif", Font.PLAIN, 10);
  private static final Font font12 = new Font("serif", Font.PLAIN, 12);
  private static final Color graphColor = new Color(0, 180, 20);
  private static final Color currentPositionColor = new Color(64, 200, 20);
  private static final Color backgroundColor = new Color(0, 128, 20);
  private static final Color gridColor = new Color(0, 140, 20);

  private Vector lines;
  private AudioInputStream audioInputStream;
  private String reportStatus;
  private String errStr;
  private Runnable capture;
  private Thread captureThread;
  private double seconds;
  private String fileName;
  private double duration;

  public AudioSamplePanel(AudioSamplingData data)
  {
    setBackground(backgroundColor);
    this.lines = data.getLine();
    this.audioInputStream = data.getAudioInputStream();
    this.reportStatus = data.getReportStatus();
    this.errStr = data.getErrStr();
    this.capture = data.getCapture();
    this.captureThread = data.getCaptureThread();
    this.seconds = data.getSeconds();
    this.fileName = data.getFileName();
    this.duration = data.getDuration();
  }

  public void reportStatus(String msg)
  {
    System.out.println("Status: " + msg);
  }

  public void createWaveForm(byte[] audioBytes)
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
        reportStatus(ex.toString());
        return;
      }
    }

    Dimension d = getSize();
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
      double y_new = (double) (h * (128 - my_byte) / 256);
      lines.add(new Line2D.Double(x, y_last, x, y_new));
      y_last = y_new;
    }

    repaint();
  }

  public void paint(Graphics g)
  {

    Dimension d = getSize();
    int w = d.width;
    int h = d.height;
    int INFOPAD = 15;

    Graphics2D g2 = (Graphics2D) g;
    g2.setBackground(getBackground());
    g2.clearRect(0, 0, w, h);
    g2.setColor(Color.black);
    g2.fillRect(0, h - INFOPAD, w, INFOPAD);

    if (errStr != null)
    {
      g2.setColor(Color.red);
      g2.setFont(new Font(res.getString("g2_Font"), Font.BOLD, 18));
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
    else if (captureThread != null)
    {
      g2.setColor(graphColor);
      g2.setFont(font12);
      g2.drawString(res.getString("Length_") + String.valueOf(seconds), 3,
                    h - 4);
    }
    else
    {
      g2.setColor(graphColor);
      g2.setFont(font12);
      g2.drawString(res.getString("File_") + fileName + "  " +
                    res.getString("Length_1") +
                    String.valueOf(duration) + "  " + res.getString("Position_") +
                    String.valueOf(seconds), 3, h - 4);

      if (audioInputStream != null)
      {
        // .. render sampling graph ..
        g2.setColor(graphColor);
        for (int i = 1; i < lines.size(); i++)
        {
          g2.draw( (Line2D) lines.get(i));
        }

        // .. draw current position ..
        if (seconds != 0)
        {
          double loc = seconds / duration * w;
          g2.setColor(currentPositionColor);
          g2.setStroke(new BasicStroke(3));
          g2.draw(new Line2D.Double(loc, 0, loc, h - INFOPAD - 2));
        }
      }
    }
  }

} // End class SamplingGraph
