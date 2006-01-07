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
/* AudioRecorder.java
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.filechooser.FileFilter;


/**
 *  Record audio in different formats
 *  and then playback the recorded audio.  The captured audio can
 *  be saved either as a WAVE, AU or AIFF.  Or load an audio file
 *  for streaming playback.
 *
 * @version @(#)CapturePlayback.java	1.11	99/12/03
 * @author Brian Lichtenwalter
 * @author Ed Smiley numerous modifications
 */
public class AudioRecorder
  extends JPanel
  implements ActionListener, AudioControlContext
{

  private static final String RESOURCE_PACKAGE = "org.sakaiproject.tool.assessment.audio";
  private static final String RESOURCE_NAME = "AudioResources";
  static ResourceBundle res = ResourceBundle.getBundle(RESOURCE_PACKAGE + "." +
      RESOURCE_NAME, Locale.getDefault());
  final int bufSize = 16384;

  FormatControls formatControls = new FormatControls();
  Capture capture = new Capture();
  Playback playback = new Playback();

  AudioInputStream audioInputStream;
  SamplingGraph samplingGraph;

  JButton playB, captB, pausB, loadB;
  JButton auB, aiffB, waveB;
  JTextField textField;

  String fileName = res.getString("default_file_name");
  String errStr;
  double duration, seconds;
  File file;
  Vector lines = new Vector();

  public AudioRecorder()
  {
    setLayout(new BorderLayout());
    EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
    SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
    Border b = new CompoundBorder(eb, sbb);

    setBorder(new EmptyBorder(5, 5, 5, 5));

    JPanel p1 = new JPanel();
    p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
    p1.add(formatControls);

    JPanel p2 = new JPanel();
    p2.setBorder(sbb);
    p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));

    JPanel buttonsPanel = makeAudioButtonPanel();
    p2.add(buttonsPanel);

    JPanel samplingPanel = makeAudioSamplingPanel(b);
    p2.add(samplingPanel);

    JPanel savePanel = new ColorBackgroundPanel();//new JPanel();
    savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));

    JPanel saveTFpanel = makeSaveTFPanel();
    savePanel.add(saveTFpanel);

    JPanel saveBpanel = makeSaveBPanel();
    savePanel.add(saveBpanel);

    p2.add(savePanel);

    p1.add(p2);
    add(p1);
  }

  private JPanel makeSaveBPanel()
  {
    JPanel saveBpanel = new ColorBackgroundPanel(false);
    auB = addButton(res.getString("Save_AU"), saveBpanel, false);
    aiffB = addButton(res.getString("Save_AIFF"), saveBpanel, false);
    waveB = addButton(res.getString("Save_WAVE"), saveBpanel, false);

    return saveBpanel;
  }

  private JPanel makeSaveTFPanel()
  {
    JPanel saveTFpanel = new ColorBackgroundPanel(false);
    saveTFpanel.add(new JLabel(res.getString("File_to_save_")));
    saveTFpanel.add(textField = new JTextField(fileName));
    saveTFpanel.setPreferredSize(new Dimension(140, 25));

    return saveTFpanel;
  }

  private JPanel makeAudioButtonPanel()
  {
    JPanel buttonsPanel = new ColorBackgroundPanel();
    playB = addButton(res.getString("Play"), buttonsPanel, false);
    captB = addButton(res.getString("Record"), buttonsPanel, true);
    pausB = addButton(res.getString("Pause"), buttonsPanel, false);
    loadB = addButton(res.getString("Load_"), buttonsPanel, true);

    return buttonsPanel;
  }

  private JPanel makeAudioSamplingPanel(Border b)
  {
    JPanel samplingPanel = new JPanel(new BorderLayout());
    samplingPanel.setBorder(b);
    samplingPanel.add(samplingGraph = new SamplingGraph());
    return samplingPanel;
  }

  public void open()
  {}

  public void close()
  {
    if (playback.thread != null)
    {
      playB.doClick(0);
    }
    if (capture.thread != null)
    {
      captB.doClick(0);
    }
  }

  private JButton addButton(String name, JPanel p, boolean state)
  {
    JButton b = new JButton(name);
    b.addActionListener(this);
    b.setEnabled(state);
    p.add(b);
    return b;
  }

  public void actionPerformed(ActionEvent e)
  {
    Object obj = e.getSource();
    if (obj.equals(auB))
    {
      saveToFile(textField.getText().trim(), AudioFileFormat.Type.AU);
    }
    else if (obj.equals(aiffB))
    {
      saveToFile(textField.getText().trim(), AudioFileFormat.Type.AIFF);
    }
    else if (obj.equals(waveB))
    {
      saveToFile(textField.getText().trim(), AudioFileFormat.Type.WAVE);
    }
    else if (obj.equals(playB))
    {
      if (playB.getText().startsWith(res.getString("Play")))
      {
        playback.start();
        samplingGraph.start();
        captB.setEnabled(false);
        pausB.setEnabled(true);
        playB.setText(" " + res.getString("playB_Text"));
      }
      else
      {
        playback.stop();
        samplingGraph.stop();
        captB.setEnabled(true);
        pausB.setEnabled(false);
        playB.setText(res.getString("Play"));
      }
    }
    else if (obj.equals(captB))
    {
      if (captB.getText().startsWith(res.getString("Record")))
      {
        file = null;
        capture.start();
        fileName = res.getString("default_file_name");
        samplingGraph.start();
        loadB.setEnabled(false);
        playB.setEnabled(false);
        pausB.setEnabled(true);
        auB.setEnabled(false);
        aiffB.setEnabled(false);
        waveB.setEnabled(false);
        captB.setText(" " + res.getString("playB_Text"));
      }
      else
      {
        lines.removeAllElements();
        capture.stop();
        samplingGraph.stop();
        loadB.setEnabled(true);
        playB.setEnabled(true);
        pausB.setEnabled(false);
        auB.setEnabled(true);
        aiffB.setEnabled(true);
        waveB.setEnabled(true);
        captB.setText(res.getString("Record"));
      }
    }
    else if (obj.equals(pausB))
    {
      if (pausB.getText().startsWith(res.getString("Pause")))
      {
        if (capture.thread != null)
        {
          capture.line.stop();
        }
        else
        {
          if (playback.thread != null)
          {
            playback.line.stop();
          }
        }
        pausB.setText(" " + res.getString("pausB_Text"));
      }
      else
      {
        if (capture.thread != null)
        {
          capture.line.start();
        }
        else
        {
          if (playback.thread != null)
          {
            playback.line.start();
          }
        }
        pausB.setText(res.getString("Pause"));
      }
    }
    else if (obj.equals(loadB))
    {
      try
      {
        File file = new File(System.getProperty(res.getString("user_dir")));
        JFileChooser fc = new JFileChooser(file);
        fc.setFileFilter(new javax.swing.filechooser.FileFilter()
        {
          public boolean accept(File f)
          {
            if (f.isDirectory())
            {
              return true;
            }
            String name = f.getName();
            if (name.endsWith(res.getString("_au")) || name.endsWith(res.getString("_wav")) ||
                name.endsWith(res.getString("_aiff")) || name.endsWith(res.getString("_aif")))
            {
              return true;
            }
            return false;
          }

          public String getDescription()
          {
            return res.getString("_au_wav_aif");
          }
        });

        if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
        {
          createAudioInputStream(fc.getSelectedFile(), true);
        }
      }
      catch (SecurityException ex)
      {
        AudioConfigHelp help = new AudioConfigHelp();
        help.configHelp();
        ex.printStackTrace();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
      }
    }
  }

  public JPanel getFormatControlsPanel()
  {
    return formatControls;
  }

  public void createAudioInputStream(File file, boolean updateComponents)
  {
    if (file != null && file.isFile())
    {
      try
      {
        this.file = file;
        errStr = null;
        audioInputStream = AudioSystem.getAudioInputStream(file);
        playB.setEnabled(true);
        fileName = file.getName();
        long milliseconds = (long) ( (audioInputStream.getFrameLength() * 1000) /
                                    audioInputStream.getFormat().getFrameRate());
        duration = milliseconds / 1000.0;
        auB.setEnabled(true);
        aiffB.setEnabled(true);
        waveB.setEnabled(true);
        if (updateComponents)
        {
          formatControls.setFormat(audioInputStream.getFormat());
          samplingGraph.createWaveForm(null, lines, audioInputStream);
        }
      }
      catch (Exception ex)
      {
        reportStatus(ex.toString());
      }
    }
    else
    {
      reportStatus(res.getString("Audio_file_required_"));
    }
  }

  /**
   * Save to a temporary file, then post it.
   * We could do this directly without a file, but this way
   * user has a local copy they can replay.
   *
   * @param tempFileName the file name where data is temporarily stored.
   * @param audioType the audio type string
   * @param urlString the url (in applets must use getCodeBase().toString() +
   * same-host relative url)
   */
  public void saveToFileAndPost(String tempFileName,
                                AudioFileFormat.Type audioType,
                                String urlString,
                                String filePath,
                                int retriesLeft)
  {
    saveToFile(tempFileName, audioType);

    URL url;
    URLConnection urlConn;
    try
    {
      // URL of audio processing servlet
      // note for applet security, this must be on same host
      url = new URL(urlString);
      urlConn = url.openConnection();
      urlConn.setDoInput(true);
      urlConn.setDoOutput(true);

      // No caching, we want the real thing.
      urlConn.setUseCaches(false);

      // determine the content type and extension
      String mimeExtension = audioType.getExtension();
      String mimeType = "audio/basic";

      if (audioType.equals(AudioFileFormat.Type.AIFF))
      {
        mimeType = "audio/x-aiff";
      }
      else if (audioType.equals(AudioFileFormat.Type.WAVE))
      {
        mimeType = "audio/x-wav";
      }

      // Specify the content type.
      urlConn.setRequestProperty("Content-Type", mimeType);

      // Specify meta-information in "From:" header

      // audio test delivery path is in the form:
      // assessmentXXX/questionXXX/agentId/audio.ext

      String fileName = filePath + "/" + "audio" + "." + mimeExtension;

      /**
       * @todo deal with retriesLeft version later
       */
//      urlConn.setRequestProperty("From", fileName + "|" +retriesLeft);
      urlConn.setRequestProperty("From", fileName);


      // Send binary POST output.
      OutputStream outputStream = urlConn.getOutputStream();
      FileInputStream inputStream = new FileInputStream(fileName);
      BufferedInputStream buf_inputStream = new BufferedInputStream(inputStream);
      BufferedOutputStream buf_outputStream = new BufferedOutputStream(outputStream);

      int i=0;
      int count=0;
      if (buf_inputStream !=null){
        while ((i=buf_inputStream.read()) != -1){
          buf_outputStream.write(i);
          count++;
        }
      }
      urlConn.setRequestProperty( "Content-length", "" + count);
      String reportStr = "Content-length written: " + count + " bytes.\n";
      buf_outputStream.flush();
      buf_outputStream.close();

      // Get response data.
      DataInputStream input = new DataInputStream(urlConn.getInputStream());

      String str;
      while (null != ( (str = input.readLine())))
      {
        reportStr += str;
      }
      input.close();
      reportStatus(reportStr + "\n");
    }
    catch (IOException ex)
    {
      reportStatus(ex.toString());
    }
    samplingGraph.repaint();

  }

  public void saveToFile(String name, AudioFileFormat.Type fileType)
  {

    if (audioInputStream == null)
    {
      reportStatus(res.getString("No_loaded_audio_to"));
      return;
    }
    else if (file != null)
    {
      createAudioInputStream(file, false);
    }

    // reset to the beginnning of the captured data
    try
    {
      audioInputStream.reset();
    }
    catch (Exception e)
    {
      reportStatus(res.getString("Unable_to_reset") + e);
      return;
    }


    File file = new File(fileName = name);
    try
    {
      if (AudioSystem.write(audioInputStream, fileType, file) == -1)
      {
        throw new IOException(res.getString("Problems_writing_to"));
      }
    }
    catch (Exception ex)
    {
      reportStatus(ex.toString());
    }
    samplingGraph.repaint();
  }

  private void reportStatus(String msg)
  {
    if ( (errStr = msg) != null)
    {
      System.out.println(errStr);
      samplingGraph.repaint();
    }
  }

  /**
   * Write data to the OutputChannel.
   */
  public class Playback
    implements Runnable
  {

    SourceDataLine line;
    Thread thread;

    public void start()
    {
      errStr = null;
      thread = new Thread(this);
      thread.setName(res.getString("thread_Name"));
      thread.start();
    }

    public void stop()
    {
      thread = null;
    }

    private void shutDown(String message)
    {
      if ( (errStr = message) != null)
      {
        System.err.println(errStr);
        samplingGraph.repaint();
      }
      if (thread != null)
      {
        thread = null;
        samplingGraph.stop();
        captB.setEnabled(true);
        pausB.setEnabled(false);
        playB.setText(res.getString("Play"));
      }
    }

    public void run()
    {

      // reload the file if loaded by file
      if (file != null)
      {
        createAudioInputStream(file, false);
      }

      // make sure we have something to play
      if (audioInputStream == null)
      {
        shutDown(res.getString("No_loaded_audio_to1"));
        return;
      }
      // reset to the beginnning of the stream
      try
      {
        audioInputStream.reset();
      }
      catch (Exception e)
      {
        shutDown(res.getString("Unable_to_reset_the") + e);
        return;
      }

      // get an AudioInputStream of the desired format for playback
      AudioFormat format = formatControls.getFormat();
      AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(
        format, audioInputStream);

      if (playbackInputStream == null)
      {
        shutDown(res.getString("Unable_to_convert") + audioInputStream +
                 res.getString("to_format") + format);
        return;
      }

      // define the required attributes for our line,
      // and make sure a compatible line is supported.

      DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                                             format);
      if (!AudioSystem.isLineSupported(info))
      {
        shutDown(res.getString("Line_matching") + info + res.getString("not_supported_"));
        return;
      }

      // get and open the source data line for playback.

      try
      {
        line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format, bufSize);
      }
      catch (LineUnavailableException ex)
      {
        shutDown(res.getString("Unable_to_open_the") + ex);
        return;
      }

      // play back the captured audio data

      int frameSizeInBytes = format.getFrameSize();
      int bufferLengthInFrames = line.getBufferSize() / 8;
      int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
      byte[] data = new byte[bufferLengthInBytes];
      int numBytesRead = 0;

      // start the source data line
      line.start();

      while (thread != null)
      {
        try
        {
          if ( (numBytesRead = playbackInputStream.read(data)) == -1)
          {
            break;
          }
          int numBytesRemaining = numBytesRead;
          while (numBytesRemaining > 0)
          {
            numBytesRemaining -= line.write(data, 0, numBytesRemaining);
          }
        }
        catch (Exception e)
        {
          shutDown(res.getString("Error_during_playback") + e);
          break;
        }
      }
      // we reached the end of the stream.  let the data play out, then
      // stop and close the line.
      if (thread != null)
      {
        line.drain();
      }
      line.stop();
      line.close();
      line = null;
      shutDown(null);
    }
  } // End class Playback

  /**
   * Reads data from the input channel and writes to the output stream
   */
  class Capture
    implements Runnable
  {

    TargetDataLine line;
    Thread thread;

    public void start()
    {
      errStr = null;
      thread = new Thread(this);
      thread.setName(res.getString("thread_Name1"));
      thread.start();
    }

    public void stop()
    {
      thread = null;
    }

    private void shutDown(String message)
    {
      if ( (errStr = message) != null && thread != null)
      {
        thread = null;
        samplingGraph.stop();
        loadB.setEnabled(true);
        playB.setEnabled(true);
        pausB.setEnabled(false);
        auB.setEnabled(true);
        aiffB.setEnabled(true);
        waveB.setEnabled(true);
        captB.setText(res.getString("Record"));
        System.err.println(errStr);
        samplingGraph.repaint();
      }
    }

    public void run()
    {

      duration = 0;
      audioInputStream = null;

      // define the required attributes for our line,
      // and make sure a compatible line is supported.

      AudioFormat format = formatControls.getFormat();
      DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                                             format);

      if (!AudioSystem.isLineSupported(info))
      {
        shutDown(res.getString("Line_matching") + info + res.getString("not_supported_"));
        return;
      }

      // get and open the target data line for capture.

      try
      {
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format, line.getBufferSize());
      }
      catch (LineUnavailableException ex)
      {
        shutDown(res.getString("Unable_to_open_the") + ex);
        return;
      }
      catch (SecurityException ex)
      {
        shutDown(ex.toString());
        AudioConfigHelp help = new AudioConfigHelp(true, false, true, true);
        help.configHelp();
        return;
      }
      catch (Exception ex)
      {
        shutDown(ex.toString());
        return;
      }

      // play back the captured audio data
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int frameSizeInBytes = format.getFrameSize();
      int bufferLengthInFrames = line.getBufferSize() / 8;
      int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
      byte[] data = new byte[bufferLengthInBytes];
      int numBytesRead;

      line.start();

      while (thread != null)
      {
        if ( (numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1)
        {
          break;
        }
        out.write(data, 0, numBytesRead);
      }

      // we reached the end of the stream.  stop and close the line.
      line.stop();
      line.close();
      line = null;

      // stop and close the output stream
      try
      {
        out.flush();
        out.close();
      }
      catch (IOException ex)
      {
        ex.printStackTrace();
      }

      // load bytes into the audio input stream for playback

      byte audioBytes[] = out.toByteArray();
      ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
      audioInputStream = new AudioInputStream(bais, format,
                                              audioBytes.length / frameSizeInBytes);

      long milliseconds = (long) ( (audioInputStream.getFrameLength() * 1000) /
                                  format.getFrameRate());
      duration = milliseconds / 1000.0;

      try
      {
        audioInputStream.reset();
      }
      catch (Exception ex)
      {
        ex.printStackTrace();
        return;
      }

      samplingGraph.createWaveForm(audioBytes, lines, audioInputStream);
    }
  } // End class Capture

  /**
   * Controls for the AudioFormat.
   */
  class FormatControls extends AudioFormatPanel implements AudioControlContext
  {
    public void open()
    {
    }

    public void close()
    {
      if (playback.thread != null)
      {
        playB.doClick(0);
      }
      if (capture.thread != null)
      {
        captB.doClick(0);
      }

    }
  } // End class FormatControls

  /**
   * Render a WaveForm.
   */
  class SamplingGraph extends AudioSampleGraphPanel implements Runnable
  {
    private Thread thread;

    public void reportGraphStatus(String message)
    {
      reportStatus(message);
    }

    public void paint(Graphics g)
    {
      AudioSamplingData data = new AudioSamplingData();
      data.setAudioInputStream(audioInputStream);
      data.setCapture(capture);
      data.setCaptureThread(capture.thread);
      data.setDuration(duration);
      data.setErrStr(errStr);
      data.setFileName(fileName);
      data.setLine(lines);
      data.setSeconds(seconds);

      paintData(g, data);
    }

    public void start()
    {
      thread = new Thread(this);
      thread.setName(res.getString("thread_Name2"));
      thread.start();
      seconds = 0;
    }

    public void stop()
    {
      if (thread != null)
      {
        thread.interrupt();
      }
      thread = null;
    }

    public void run()
    {
      seconds = 0;
      while (thread != null)
      {
        if ( (playback.line != null) && (playback.line.isOpen()))
        {

          long milliseconds = (long) (playback.line.getMicrosecondPosition() /
                                      1000);
          seconds = milliseconds / 1000.0;
        }
        else if ( (capture.line != null) && (capture.line.isActive()))
        {

          long milliseconds = (long) (capture.line.getMicrosecondPosition() /
                                      1000);
          seconds = milliseconds / 1000.0;
        }

        try
        {
          thread.sleep(100);
        }
        catch (Exception e)
        {
          break;
        }

        repaint();

        while ( (capture.line != null && !capture.line.isActive()) ||
               (playback.line != null && !playback.line.isOpen()))
        {
          try
          {
            thread.sleep(10);
          }
          catch (Exception e)
          {
            break;
          }
        }
      }
      seconds = 0;
      repaint();
    }
  } // End class SamplingGraph

  public static void main(String s[])
  {
    AudioRecorder capturePlayback = new AudioRecorder();
    capturePlayback.open();
    JFrame f = new JFrame(res.getString("Capture_Playback"));
    f.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }
    });
    f.getContentPane().add(res.getString("Center"), capturePlayback);
    f.pack();
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    int w = 720;
    int h = 340;
    f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h / 2);
    f.setSize(w, h);
    f.show();
  }
}
