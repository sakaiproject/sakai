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


/* AudioFormatPanel.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Line2D;
import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.border.Border;

public class AudioFormatPanel extends JPanel
{
     static ResourceBundle 	res = AudioUtil.getInstance().getResourceBundle();
 
    protected AudioRecorderParams params;
    Vector groups = new Vector();
    JToggleButton linrB, ulawB, alawB, rate8B, rate11B, rate16B, rate22B,
      rate44B;
    JToggleButton size8B, size16B, signB, unsignB, litB, bigB, monoB, sterB;

    public AudioFormatPanel(AudioRecorderParams params)
    {
      this.params = params;
      setLayout(new GridLayout(0, 1));
      EmptyBorder eb = new EmptyBorder(0, 0, 0, 5);
      BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
      CompoundBorder cb = new CompoundBorder(eb, bb);
      setBorder(new CompoundBorder(cb, new EmptyBorder(8, 5, 5, 5)));
      JPanel p1 = new ColorBackgroundPanel(false);

      ButtonGroup encodingGroup = makeEncodingGroup(params, p1);
      add(p1);
      groups.addElement(encodingGroup);

      JPanel p2 = new ColorBackgroundPanel(false);
      JPanel p2b = new ColorBackgroundPanel(false);

      ButtonGroup sampleRateGroup = makeSampleRateGroup(params, p2, p2b);
      add(p2);
      add(p2b);
      groups.addElement(sampleRateGroup);

      JPanel p3 = new ColorBackgroundPanel(false);

      ButtonGroup sampleSizeInBitsGroup = makeSizeInBitsGroup(params, p3);
      add(p3);
      groups.addElement(sampleSizeInBitsGroup);

      JPanel p4 = new ColorBackgroundPanel(false);

      ButtonGroup signGroup = makeSignGroup(params, p4);
      add(p4);
      groups.addElement(signGroup);

      JPanel p5 = new ColorBackgroundPanel(false);

      ButtonGroup endianGroup = makeEndianGroup(params, p5);
      add(p5);
      groups.addElement(endianGroup);

      JPanel p6 = new ColorBackgroundPanel(false);
      ButtonGroup channelsGroup = makeChannelsGroup(params, p6);
      add(p6);
      groups.addElement(channelsGroup);
    }

    private ButtonGroup makeEncodingGroup(AudioRecorderParams params, JPanel p1)
    {
      ButtonGroup encodingGroup = new ButtonGroup();

      boolean linear = false;
      boolean ulaw = false;
      boolean alaw = false;

      if ("linear".equals(params.getCompression()))
      {
        linear = true;
      }
      else if ("ulaw".equals(params.getCompression()))
      {
        ulaw = true;
      }
      else
      {
        alaw = true;
      }

      linrB = addToggleButton(p1, encodingGroup, res.getString("linear"), linear);
      ulawB = addToggleButton(p1, encodingGroup, res.getString("ulaw"), ulaw);
      alawB = addToggleButton(p1, encodingGroup, res.getString("alaw"), alaw);
      return encodingGroup;
    }

    private ButtonGroup makeSampleRateGroup(AudioRecorderParams params,
                                            JPanel p2, JPanel p2b)
    {
      ButtonGroup sampleRateGroup = new ButtonGroup();

      boolean rates[] = { false, false, false, false,  false,};

      int rate = params.getFrequency();

      for (int i = 0; i < params.frequenciesAllowed.length; i++)
      {
        if (rate==params.frequenciesAllowed[i])
        {
          rates[i] = true;
          break;
        }

      }

      rate8B =  addToggleButton(p2, sampleRateGroup,  "8000",  rates[0]);
      rate11B = addToggleButton(p2, sampleRateGroup,  "11025", rates[1]);
      rate16B = addToggleButton(p2b, sampleRateGroup, "16000", rates[2]);
      rate22B = addToggleButton(p2b, sampleRateGroup, "22050", rates[3]);
      rate44B = addToggleButton(p2b, sampleRateGroup, "44100", rates[4]);
      return sampleRateGroup;
    }

    private ButtonGroup makeSizeInBitsGroup(AudioRecorderParams params,
                                            JPanel p3)
    {
      ButtonGroup sampleSizeInBitsGroup = new ButtonGroup();

      boolean bit8 = params.getBits()==8 ? true : false;

      size8B = addToggleButton(p3, sampleSizeInBitsGroup, "8", bit8);
      size16B = addToggleButton(p3, sampleSizeInBitsGroup, "16", !bit8);
      return sampleSizeInBitsGroup;
    }

    private ButtonGroup makeSignGroup(AudioRecorderParams params, JPanel p4)
    {
      ButtonGroup signGroup = new ButtonGroup();

      signB = addToggleButton(p4, signGroup, res.getString("signed"),
                              params.isSigned());
      unsignB = addToggleButton(p4, signGroup, res.getString("unsigned"),
                                !params.isSigned());
      return signGroup;
    }

    private ButtonGroup makeEndianGroup(AudioRecorderParams params, JPanel p5)
    {
      ButtonGroup endianGroup = new ButtonGroup();
      litB = addToggleButton(p5, endianGroup, res.getString("little_endian"),
                             !params.isBigendian());
      bigB = addToggleButton(p5, endianGroup, res.getString("big_endian"),
                             params.isBigendian());
      return endianGroup;
    }

    private ButtonGroup makeChannelsGroup(AudioRecorderParams params, JPanel p6)
    {
      ButtonGroup channelsGroup = new ButtonGroup();
      monoB = addToggleButton(p6, channelsGroup, res.getString("mono"),
                              !params.isStereo());
      sterB = addToggleButton(p6, channelsGroup, res.getString("stereo"),
                              params.isStereo());
      return channelsGroup;
    }

    private JToggleButton addToggleButton(JPanel p, ButtonGroup g,
                                          String name, boolean state)
    {
      JToggleButton b = new JToggleButton(name, state);
      p.add(b);
      g.add(b);
      return b;
    }

    public AudioFormat getFormat()
    {

      Vector v = new Vector(groups.size());
      for (int i = 0; i < groups.size(); i++)
      {
        ButtonGroup g = (ButtonGroup) groups.get(i);
        for (Enumeration e = g.getElements(); e.hasMoreElements(); )
        {
          AbstractButton b = (AbstractButton) e.nextElement();
          if (b.isSelected())
          {
            v.add(b.getText());
            break;
          }
        }
      }

      AudioFormat.Encoding encoding = AudioFormat.Encoding.ULAW;
      String encString = (String) v.get(0);
      float rate = Float.valueOf( (String) v.get(1)).floatValue();
      int sampleSize = Integer.valueOf( (String) v.get(2)).intValue();
      String signedString = (String) v.get(3);
      boolean bigEndian = ( (String) v.get(4)).startsWith(res.getString("big"));
      int channels = ( (String) v.get(5)).equals(res.getString("mono")) ? 1 : 2;

      if (encString.equals(res.getString("linear")))
      {
        if (signedString.equals(res.getString("signed")))
        {
          encoding = AudioFormat.Encoding.PCM_SIGNED;
        }
        else
        {
          encoding = AudioFormat.Encoding.PCM_UNSIGNED;
        }
      }
      else if (encString.equals(res.getString("alaw")))
      {
        encoding = AudioFormat.Encoding.ALAW;
      }
      return new AudioFormat(encoding, rate, sampleSize,
                             channels, (sampleSize / 8) * channels, rate,
                             bigEndian);
    }

    public void setFormat(AudioFormat format)
    {
      AudioFormat.Encoding type = format.getEncoding();
      if (type == AudioFormat.Encoding.ULAW)
      {
        ulawB.doClick();
      }
      else if (type == AudioFormat.Encoding.ALAW)
      {
        alawB.doClick();
      }
      else if (type == AudioFormat.Encoding.PCM_SIGNED)
      {
        linrB.doClick();
        signB.doClick();
      }
      else if (type == AudioFormat.Encoding.PCM_UNSIGNED)
      {
        linrB.doClick();
        unsignB.doClick();
      }
      float rate = format.getFrameRate();
      if (rate == 8000)
      {
        rate8B.doClick();
      }
      else if (rate == 11025)
      {
        rate11B.doClick();
      }
      else if (rate == 16000)
      {
        rate16B.doClick();
      }
      else if (rate == 22050)
      {
        rate22B.doClick();
      }
      else if (rate == 44100)
      {
        rate44B.doClick();
      }
      switch (format.getSampleSizeInBits())
      {
        case 8:
          size8B.doClick();
          break;
        case 16:
          size16B.doClick();
          break;
      }
      if (format.isBigEndian())
      {
        bigB.doClick();
      }
      else
      {
        litB.doClick();
      }
      if (format.getChannels() == 1)
      {
        monoB.doClick();
      }
      else
      {
        sterB.doClick();
      }
    }

}
