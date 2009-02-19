/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 Sakai Foundation
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


import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
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
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

import netscape.javascript.*;

/**
 * Record audio in different formats and then playback the recorded audio. The
 * captured audio can be saved either as a WAVE, AU or AIFF. Or load an audio
 * file for streaming playback.
 * 
 * @version
 * @(#)CapturePlayback.java 1.11 99/12/03
 * @author Brian Lichtenwalter
 * @author Ed Smiley numerous modifications
 */
public class AudioRecorder extends JPanel implements ActionListener,
		AudioControlContext {

	private static final long serialVersionUID = 0L;


	static ResourceBundle res;


	final int bufSize = 16384;

	FormatControls formatControls = null;

	Capture capture = new Capture();

	Playback playback = new Playback();

	AudioInputStream audioInputStream;

	SamplingGraph samplingGraph;
	
	AudioMeter audioMeter;

	Timer timer;

	JButton playB, captB, saveButton;

	JTextField textField;
	
	DecimalFormat NoDecimalPlaces = new DecimalFormat("#0");

	JTextField rtextField;
	
	JLabel finishedLabel;
	
	JPanel samplingPanelContainer;

	JLabel statusLabel = new JLabel("", SwingConstants.LEFT);

	String fileName;

	String agentId;

	String errStr;
	
	String mediaId;

	double duration, seconds;

	int attempts;

	File file;

	Vector<Line2D> lines = new Vector<Line2D>();

	AudioRecorderParams params;

	String imageUrl;

	ImageIcon recordIcon = null;

	ImageIcon playIcon = null;

	ImageIcon stopIcon = null;
	
	Applet containingApplet = null;

	public AudioRecorder(AudioRecorderParams params) {
		res = AudioUtil.getInstance().getResourceBundle();
		fileName = res.getString("default_file_name");
		
		this.params = params;
		if (params.getAttemptsRemaining() == -1) {
			params.setAttemptsRemaining(params.getAttemptsAllowed());
		}

		// load images
		imageUrl = params.getImageUrl();
		// System.out.println("**** imageUrl="+imageUrl);
		try {
			recordIcon = new ImageIcon(new URL(imageUrl + "/audio_record.gif"));
			playIcon = new ImageIcon(new URL(imageUrl + "/audio_play.gif"));
			stopIcon = new ImageIcon(new URL(imageUrl + "/audio_stop.gif"));
		} catch (Exception ex) {
			reportStatus("**** cannot create image icons for applet:"
					+ ex.toString());
		}

		EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
		SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);
		Border b = new CompoundBorder(eb, sbb);

		formatControls = new FormatControls(params);

		/*
		 * Samigo 2.2 does not required Advanced Settings, so comment it out
		 * setLayout(new BorderLayout()); setBorder(new EmptyBorder(5, 5, 5,
		 * 5)); JPanel p1 = new JPanel(); p1.setLayout(new BoxLayout(p1,
		 * BoxLayout.X_AXIS)); p1.add(formatControls);
		 */

		JPanel p2 = new JPanel();
		p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
		p2.add(BorderLayout.WEST, makeAttemptsAllowedLabel());
		p2.add(makeAudioButtonsPanel());
		
		samplingGraph = new SamplingGraph();
		audioMeter = new AudioMeter(params.getImageUrl());
		
		samplingPanelContainer = makeAudioSamplingPanelContainer(b);
		samplingPanelContainer.add(audioMeter);

		p2.add(samplingPanelContainer);

		JPanel savePanel = new JPanel();
		savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));
		JPanel saveTFpanel = makeSaveTFPanel();
		savePanel.add(saveTFpanel);
		p2.add(savePanel);
		add(p2);

		// p1.add(p2);
		// add(p1);
	}

	private JPanel makeSaveTFPanel() {
		FlowLayout flow = new FlowLayout();
		SpringLayout spring = new SpringLayout();
		JPanel saveTFpanel = new JPanel(spring);
		JLabel flabel = new JLabel(res.getString("current_recordig_length"),
				SwingConstants.LEFT);
		JLabel rlabel = new JLabel(res.getString("attempts_remaining"),
				SwingConstants.LEFT);
		saveTFpanel.add(flabel);
		saveTFpanel.add(textField = new JTextField(""
				+ params.getCurrentRecordingLength()));
		saveTFpanel.add(rlabel);

		if (params.getAttemptsRemaining() >= 9999) {
			saveTFpanel.add(rtextField = new JTextField("Unlimited"));
		} else if (params.getAttemptsRemaining() > 0
				&& params.getAttemptsRemaining() < 9999)
			saveTFpanel.add(rtextField = new JTextField(""
					+ params.getAttemptsRemaining()));
		else
			saveTFpanel.add(rtextField = new JTextField("0"));

		textField.setEditable(false);
		rtextField.setEditable(false);
		Font font = new Font("Ariel", Font.PLAIN, 14);
		flabel.setFont(font);
		rlabel.setFont(font);
		textField.setFont(font);
		rtextField.setFont(font);
		SpringUtilities.makeCompactGrid(saveTFpanel, 2, 2, 5, 5, 5, 5);
		return saveTFpanel;
	}

	private JPanel makeAudioButtonsPanel() {
		JPanel buttonsPanel = new JPanel(new GridLayout(2,3));
//		JPanel buttonsPanel = new JPanel(new SpringLayout());
		// the two leftmost buttons are unlabled
		buttonsPanel.add(new JLabel(""));
		buttonsPanel.add(new JLabel(""));
		finishedLabel = new JLabel(res.getString("Finished"));
		finishedLabel.setForeground(Color.red);
		Font f = finishedLabel.getFont();
		finishedLabel.setFont(f.deriveFont(f.getStyle() ^ Font.BOLD));
		buttonsPanel.add(finishedLabel);
		if (params.getAttemptsRemaining() > 0
				|| params.getAttemptsRemaining() == -1) {
			captB = addButton(res.getString("Record"), buttonsPanel, true,
					params.isEnableRecord());
			if (recordIcon != null)
				captB.setIcon(recordIcon);
		} else {
			captB = addButton(res.getString("Record"), buttonsPanel, false,
					params.isEnableRecord());
			if (recordIcon != null)
				captB.setIcon(recordIcon);
		}
		playB = addButton(res.getString("Play"), buttonsPanel, false, params
				.isEnablePlay());
		saveButton = addButton(res.getString("Save_and_close"), Font.BOLD, buttonsPanel, false, params.isEnableSave());

		if (playIcon != null)
			playB.setIcon(playIcon);
//		SpringUtilities.makeCompactGrid(buttonsPanel, 2, 3, 5, 5, 5, 5);
		return buttonsPanel;
	}

	private JButton addButton(String name, int style, JPanel p,
			boolean state, boolean visible) {
		JButton b = new JButton(name);
		Font f = b.getFont();
		b.setFont(f.deriveFont(f.getStyle() ^ style));
		b.addActionListener(this);
		b.setEnabled(state);
		b.setVisible(visible);
		p.add(b);
		return b;
	}

	private JPanel makeAudioSamplingPanelContainer(Border b) {
		JPanel samplingPanel = new JPanel(new BorderLayout());
		samplingPanel.setBorder(b);
		samplingPanel.setPreferredSize(new Dimension(225, 150));
		return samplingPanel;
	}

	public void open() {
		/*
		 * daisy test code - pls do not delete String mediaUrl =
		 * "http://sakai-l.stanford.edu:8080/samigo/servlet/ShowMedia?mediaId=107";
		 * //String mediaUrl =
		 * "http://sakai-l.stanford.edu:8080/samigo/spacemusic.au";
		 * System.out.println("*****open applet="+mediaUrl);
		 * createAudioInputStream(mediaUrl, true);
		 */
	}

	public void close() {
		if (playback.thread != null) {
			playB.doClick(0);
		}
		if (capture.thread != null) {
			captB.doClick(0);
		}
	}

	/**
	 * Add a button to a panel, enabled or disabled, visible or invisible. We
	 * create invisible buttons if their function is turned off in our
	 * configuration, so that the UI logic always works the same way.
	 * 
	 * @param name
	 * @param p
	 *            the panel
	 * @param state
	 *            enabled/disabled state
	 * @param visible
	 *            visible/invisible state
	 * @return
	 */
	private JButton addButton(String name, JPanel p, boolean state,
			boolean visible) {
		return addButton(name, Font.PLAIN, p, state, visible);
	}

	public void actionPerformed(ActionEvent e) {
		Object obj = e.getSource();

		statusLabel.setVisible(false);
		statusLabel.setText("");
		if (obj.equals(playB)) {
			if (playB.getText().startsWith(res.getString("Play"))) {
				playback.start();
				samplingGraph.start();
				captB.setEnabled(false);
				playB.setText(" " + res.getString("playB_Text"));
				if (stopIcon != null)
					playB.setIcon(stopIcon);
			} else {
				playback.stop();
				samplingGraph.stop();
				if (params.getAttemptsAllowed() > 0) {
					if (attempts == 0)
						captB.setEnabled(false);
					else
						captB.setEnabled(true);
					if (recordIcon != null)
						captB.setIcon(recordIcon);
				}
				playB.setText(res.getString("Play"));
				if (playIcon != null)
					playB.setIcon(playIcon);
			}
		} else if (obj.equals(captB)) {
			if (captB.getText().startsWith(res.getString("Record"))) {
				if (containingApplet != null) {
					JSObject openingWindow = (JSObject)((JSObject)JSObject.getWindow(containingApplet).getMember("opener"));
					openingWindow.call("disableSubmitForGrade", null);
					openingWindow.call("disableSave", null);
					// openingWindow.call("hide", new Object[]{"question" + params.getQuestionId()});
				}
				file = null;
				capture.start();
				fileName = res.getString("default_file_name");
				samplingGraph.start();
				audioMeter.start();
				
				playB.setEnabled(false);
				saveButton.setEnabled(false);
				samplingPanelContainer.remove(samplingGraph);
				samplingPanelContainer.add(audioMeter);
				if (playIcon != null)
					playB.setIcon(playIcon);
				captB.setText(" " + res.getString("playB_Text"));
				if (stopIcon != null)
					captB.setIcon(stopIcon);
				startTimer();
			} else {
				statusLabel.setText(res.getString("processing"));
				statusLabel.setVisible(true);
				playB.setEnabled(false);
				captB.setEnabled(false);
				saveButton.setEnabled(false);
				audioMeter.stop();
				samplingPanelContainer.remove(audioMeter);
				samplingPanelContainer.add(samplingGraph);
				captureAudio();
				if (recordIcon != null)
					captB.setIcon(recordIcon);
			}
		} else if (obj.equals(saveButton)) {
			saveButton.setEnabled(false);
			captB.setEnabled(false);
			playB.setEnabled(false);
			statusLabel.setText(res.getString("processing"));
			statusLabel.setVisible(true);
			saveMedia();
		}
	}

	private void resetAttempts(int attempts0) {
		if (attempts0 > 0) {
			rtextField.setText("" + attempts0);
		}
	}

	public JPanel getFormatControlsPanel() {
		return formatControls;
	}

	public void createAudioInputStream(File file, boolean updateComponents) {
		if (file != null && file.isFile()) {
			try {
				this.file = file;
				errStr = null;
				audioInputStream = AudioSystem.getAudioInputStream(file);
				// playB.setEnabled(true);
				fileName = file.getName();
				long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream
						.getFormat().getFrameRate());
				duration = milliseconds / 1000.0;
				if (updateComponents) {
					formatControls.setFormat(audioInputStream.getFormat());
					samplingGraph.createWaveForm(null, lines, audioInputStream);
				}
			} catch (Exception ex) {
				reportStatus(ex.toString());
			}
		} else {
			reportStatus(res.getString("Audio_file_required_"));
		}
	}

	/**
	 * Post audio data directly.
	 * 
	 * @param audioType
	 *            the audio type string
	 * @param urlString
	 *            the url (in applets must use getCodeBase().toString() +
	 *            same-host relative url)
	 * @param inputStream
	 *            the input stream
	 * @param attemptsLeft
	 *            attempts left
	 */
	public void saveAndPost(InputStream inputStream,
			final AudioFileFormat.Type audioType, final String urlString,
			int attemptsLeft, final boolean post) {
		Thread saveAndPostThread = new Thread() {
			public void run() {
			    while (audioInputStream == null) {
			    	try {
			    		// politely waiting for capture Thread to finish with audioInputStream.
			    		Thread.sleep(1000);
			    	} catch (InterruptedException e) {
			    		// TODO Auto-generated catch block
			    		e.printStackTrace();
			    	}
				}
				// reset to the beginnning of the captured data
				try {
					audioInputStream.reset();
				} catch (Exception ex) {
					reportStatus(res.getString("Unable_to_reset") + ex);
					return;
				}

				if (post)
					postAudio(audioType, urlString);
				
				if (containingApplet != null) {
					JSObject window = (JSObject)JSObject.getWindow(containingApplet);
					JSObject opener = (JSObject)window.getMember("opener");
					opener.call("clickReloadLink", new Object[]{window});
					window.call("close", null);	
				}
			} // end of run
		}; // end of saveAndPostThread
		saveAndPostThread.start();
	}

	public void postAudio(AudioFileFormat.Type audioType, String urlString) {
		String suffix = getAudioSuffix();
		URL url;
		URLConnection urlConn;
		try {
			// URL of audio processing servlet
			// note for applet security, this must be on same host
			agentId = params.getAgentId();
			String queryString = "&agent=" + agentId + "&lastDuration="
					+ duration + "&suffix=" + suffix
					+ "&attempts=" + attempts;
			url = new URL(urlString + queryString);
			urlConn = url.openConnection();
			urlConn.setDoInput(true);
			urlConn.setDoOutput(true);

			// No caching, we want the real thing.
			urlConn.setUseCaches(false);

			urlConn.setRequestProperty("CONTENT-TYPE", getMimeType(audioType));
			// Send binary POST output.
			OutputStream outputStream = urlConn.getOutputStream();
			BufferedReader input = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
			try {
				// System.out.println("**** no. of bytes
				// recorded="+audioInputStream.available());
				int c = AudioSystem.write(audioInputStream, audioType,
						outputStream);
				if (c <= 0) {
					throw new IOException(res.getString("Problems_writing_to"));
				}
				outputStream.flush();
				outputStream.close();

				// Get response data.
				//String reportStr = res.getString("contentlenw") + ": " + c + " " + res.getString("bytes") + ".\n  ";
				

				// need to check that acknowlegement from server matches or
				// display				  
				  
				//StringBuilder reportStrBuf = new StringBuilder(reportStr);
				StringBuilder responseBuf = new StringBuilder();
				String str;
				while (null != ((str = input.readLine()))) {
				//	reportStrBuf.append(str);
					responseBuf.append(str);
				}
				//reportStr = reportStrBuf.toString();
				String response = responseBuf.toString();
				
				input.close();
				mediaId = response;
				// mock up doesn't require report, let's comment it out
				// reportStatus(reportStr + "\n");
			} catch (Exception ex) {
				reportStatus(ex.toString());
			}
			finally{
		      	if (input !=null){
		    		try {
		    			input.close();
		    		} catch (Exception e1) {
		    			e1.printStackTrace();
		    		}
		    	} 
		 
			}
		} catch (IOException ex) {
			reportStatus(ex.toString());
		}
	}

	private void reportStatus(String msg) {
		if ((errStr = msg) != null) {
			System.out.println(errStr);
			samplingGraph.repaint();
		}
	}

	/**
	 * Write data to the OutputChannel.
	 */
	public class Playback implements Runnable {

		SourceDataLine line;

		Thread thread;

		public void start() {
			errStr = null;
			thread = new Thread(this);
			thread.setName(res.getString("thread_Name"));
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		private void shutDown(String message) {
			if ((errStr = message) != null) {
				System.err.println(errStr);
				samplingGraph.repaint();
			}
			if (thread != null) {
				thread = null;
				samplingGraph.stop();
				if (params.getAttemptsAllowed() > 0) {
					if (attempts == 0)
						captB.setEnabled(false);
					else
						captB.setEnabled(true);

				}
				playB.setText(res.getString("Play"));
				if (playIcon != null)
					playB.setIcon(playIcon);
			}
		}

		public void run() {
			// reload the file if loaded by file
			if (file != null) {
				createAudioInputStream(file, false);
			}

			// make sure we have something to play
			if (audioInputStream == null) {
				shutDown(res.getString("No_loaded_audio_to1"));
				return;
			}
			// reset to the beginnning of the stream
			try {
				audioInputStream.reset();
			} catch (Exception e) {
				shutDown(res.getString("Unable_to_reset_the") + e);
				return;
			}

			// get an AudioInputStream of the desired format for playback
			AudioFormat format = formatControls.getFormat();
			AudioInputStream playbackInputStream = AudioSystem
					.getAudioInputStream(format, audioInputStream);

			if (playbackInputStream == null) {
				shutDown(res.getString("Unable_to_convert") + audioInputStream
						+ res.getString("to_format") + format);
				return;
			}

			// define the required attributes for our line,
			// and make sure a compatible line is supported.

			DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
			if (!AudioSystem.isLineSupported(info)) {
				shutDown(res.getString("Line_matching") + info
						+ res.getString("not_supported_"));
				return;
			}

			// get and open the source data line for playback.

			try {
				line = (SourceDataLine) AudioSystem.getLine(info);
				line.open(format, bufSize);
			} catch (LineUnavailableException ex) {
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

			while (thread != null) {
				try {
					if ((numBytesRead = playbackInputStream.read(data)) == -1) {
						break;
					}
					int numBytesRemaining = numBytesRead;
					while (numBytesRemaining > 0) {
						numBytesRemaining -= line.write(data, 0,
								numBytesRemaining);
					}
				} catch (Exception e) {
					shutDown(res.getString("Error_during_playback") + e);
					break;
				}
			}
			// we reached the end of the stream. let the data play out, then
			// stop and close the line.
			if (thread != null) {
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
	class Capture implements Runnable {

		TargetDataLine line;

		Thread thread;

		public void start() {
			errStr = null;
			thread = new Thread(this);
			thread.setName(res.getString("thread_Name1"));
			thread.start();
		}

		public void stop() {
			thread = null;
		}

		private void shutDown(String message) {
			if ((errStr = message) != null && thread != null) {
				thread = null;
				samplingGraph.stop();
				playB.setEnabled(true);
				saveButton.setEnabled(true);
				captB.setText(res.getString("Record"));
				System.err.println(errStr);
				samplingGraph.repaint();
			}
		}

		public void run() {

			duration = 0;
			audioInputStream = null;

			// define the required attributes for our line,
			// and make sure a compatible line is supported.

			AudioFormat format = formatControls.getFormat();
			DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

			if (!AudioSystem.isLineSupported(info)) {
				shutDown(res.getString("Line_matching") + info
						+ res.getString("not_supported_"));
				return;
			}

			// get and open the target data line for capture.

			try {
				line = (TargetDataLine) AudioSystem.getLine(info);
				line.open(format, line.getBufferSize());
			} catch (LineUnavailableException ex) {
				shutDown(res.getString("Unable_to_open_the") + ex);
				return;
			} catch (SecurityException ex) {
				shutDown(ex.toString());
				AudioConfigHelp help = new AudioConfigHelp(true, false, true,
						true);
				help.configHelp();
				return;
			} catch (Exception ex) {
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
			audioMeter.start();

			while (thread != null) {
				if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
					break;
				}
				// we want this to sample in a separate thread, methinks
			    for (int i=0; i < data.length; i++) {
			    	if (Math.abs(data[i]) > 0) {
			    		audioMeter.setLevel(Math.abs(data[i]));
			    		audioMeter.setSeconds(seconds);
			    	}
			    }
				out.write(data, 0, numBytesRead);
				
			}

			// we reached the end of the stream. stop and close the line.
			line.stop();
			line.close();
			line = null;

			// stop and close the output stream
			try {
				out.flush();
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}

			// load bytes into the audio input stream for playback

			byte audioBytes[] = out.toByteArray();
			ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
			audioInputStream = new AudioInputStream(bais, format,
					audioBytes.length / frameSizeInBytes);

			long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / format
					.getFrameRate());
			duration = milliseconds / 1000.0;

			try {
				audioInputStream.reset();
			} catch (Exception ex) {
				ex.printStackTrace();
				return;
			}

			samplingGraph.createWaveForm(audioBytes, lines, audioInputStream);
		}
	} // End class Capture

	/**
	 * Controls for the AudioFormat.
	 */
	class FormatControls extends AudioFormatPanel implements
			AudioControlContext {

		private static final long serialVersionUID = 0L;

		FormatControls(AudioRecorderParams params) {
			super(params);
		}

		public void open() {
		}

		public void close() {
			if (playback.thread != null) {
				playB.doClick(0);
			}
			if (capture.thread != null) {
				captB.doClick(0);
			}

		}
	} // End class FormatControls

	/**
	 * Render a WaveForm.
	 */
	class SamplingGraph extends AudioSampleGraphPanel implements Runnable {


		private static final long serialVersionUID = 0L;
		
		private Thread thread;

		public void reportGraphStatus(String message) {
			reportStatus(message);
		}

		public void paint(Graphics g) {
			AudioSamplingData data = new AudioSamplingData();
			data.setAudioInputStream(audioInputStream);
			data.setCapture(capture);
			data.setCaptureThread(capture.thread);
			data.setDuration(duration);
			data.setErrStr(errStr);
			data.setFileName(fileName);
			data.setLine(lines);
			data.setSeconds(seconds);
			data.setMaxSeconds(params.getMaxSeconds());

			paintData(g, data);

		}

		public void start() {
			thread = new Thread(this);
			thread.setName(AudioSampleGraphPanel.res.getString("thread_Name2"));
			thread.start();
			seconds = 0;
		}

		public void stop() {
			if (thread != null) {
				thread.interrupt();
			}
			thread = null;
		}

		public void run() {
			seconds = 0;
			while (thread != null) {
				if ((playback.line != null) && (playback.line.isOpen())) {

					long milliseconds = (long) (playback.line
							.getMicrosecondPosition() / 1000);
					seconds = milliseconds / 1000.0;
				} else if ((capture.line != null) && (capture.line.isActive())) {

					long milliseconds = (long) (capture.line
							.getMicrosecondPosition() / 1000);
					seconds = milliseconds / 1000.0;
				}

				try {
					Thread.sleep(100);
				} catch (Exception e) {
					break;
				}

				repaint();

				while ((capture.line != null && !capture.line.isActive())
						|| (playback.line != null && !playback.line.isOpen())) {
					try {
						Thread.sleep(10);
					} catch (Exception e) {
						break;
					}
				}
			}
			seconds = 0;
			repaint();
		}
	} // End class SamplingGraph
	
	class AudioMeter extends AudioMeterPanel implements Runnable {

		private static final long serialVersionUID = 0L;
		
		public AudioMeter(String imageUrl) {
			super(imageUrl);
		}
		
		public void start() {
			animator = new Thread(this);
			animator.start();
		}

		public void stop() {
			animator = null;
			offImage = null;
			offGraphics = null;
		    }

		/**
	     * This method is called by the thread that was created in
	     * the start method. It does the main animation.
	     */
	    public void run() {
			// Remember the starting time
			long tm = System.currentTimeMillis();
			while (Thread.currentThread() == animator) {
			    // Display the next frame of animation.
			    repaint();
	
			    // Delay depending on how far we are behind.
			    try {
				tm += delay;
				Thread.sleep(Math.max(0, tm - System.currentTimeMillis()));
			    } catch (InterruptedException e) {
				break;
			    }
			}
	    }
	}

	public static void main(String s[]) {
		AudioRecorderParams params = new AudioRecorderParams();
		AudioRecorder capturePlayback = new AudioRecorder(params);
		capturePlayback.open();
		JFrame f = new JFrame(res.getString("Capture_Playback"));
		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		f.getContentPane().add("Center", capturePlayback);
		f.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = 600;
		int h = 250;
		f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h
				/ 2);
		f.setSize(w, h);
		f.setVisible(true);
	}

	private JPanel makeAttemptsAllowedLabel() {
		GridLayout grid = new GridLayout(2, 3);
		JPanel panel = new JPanel(grid);
		JLabel label1 = new JLabel(res.getString("attempts_allowed") + " "
				+ params.getAttemptsAllowed(), SwingConstants.LEFT);
		if (params.getAttemptsAllowed() >= 9999) {
			label1 = new JLabel(res.getString("attempts_allowed") + " "
					+ res.getString("unlimited"), SwingConstants.LEFT);
		}
		JLabel label2 = new JLabel(res.getString("time_limit") + " "
				+ params.getMaxSeconds() + " sec", SwingConstants.LEFT);
		Font font = new Font("Ariel", Font.PLAIN, 14);
		label1.setFont(font);
		label2.setFont(font);

		Font statusFont = new Font("Ariel", Font.BOLD, 12);
		statusLabel = new JLabel("", SwingConstants.CENTER);
		statusLabel.setFont(statusFont);
		statusLabel.setForeground(Color.red);

		panel.add(label2);
		panel.add(statusLabel);
		panel.add(label1);
		return panel;
	}

	private String getAudioSuffix() {
		String suffix = "au";
		if (params.isSaveWave())
			suffix = "wav";
		if (params.isSaveAiff())
			suffix = "aif";
		return suffix;
	}

	private AudioFileFormat.Type getAudioType() {
		AudioFileFormat.Type type = AudioFileFormat.Type.AU;
		if (params.isSaveWave())
			type = AudioFileFormat.Type.WAVE;
		if (params.isSaveAiff())
			type = AudioFileFormat.Type.AIFF;
		return type;
	}

	private void saveMedia() {
		AudioFileFormat.Type type = getAudioType();

			if (params.isSaveToUrl()) {
				saveAndPost(audioInputStream, type, params.getUrl(), attempts,
						true);
			} else {
				// this is for preview when u just wnat to keep the recording at
				// client
				saveAndPost(audioInputStream, type, params.getUrl(), attempts,
						false);
			}
	}

	private String getMimeType(AudioFileFormat.Type audioType) {
		String mimeType = "audio/basic";

		if (audioType.equals(AudioFileFormat.Type.AIFF)) {
			mimeType = "audio/x-aiff";
		} else if (audioType.equals(AudioFileFormat.Type.WAVE)) {
			mimeType = "audio/x-wav";
		}
		return mimeType;
	}

	private void captureAudio() {
		// timer was started by clicking Record to enforce time limit
		if (timer != null) {
			timer.stop();
		}

		lines.removeAllElements();
		capture.stop();
		samplingGraph.stop();
		// playB.setEnabled(true);
		captB.setText(res.getString("Record"));
		int retry = 1;
		while (audioInputStream == null) {
	    	try {
	    		// politely waiting for capture Thread to finish with audioInputStream.
	    		Thread.sleep(1000);
	    	} catch (InterruptedException e) {
	    		// TODO Auto-generated catch block
	    		e.printStackTrace();
	    	}
		}
		// reset to the beginnning of the captured data
		try {
			audioInputStream.reset();
		} catch (Exception ex) {
			reportStatus(res.getString("Unable_to_reset") + ex);
			return;
		}
		statusLabel.setVisible(false);
		statusLabel.setText("");
		playB.setEnabled(true);
		saveButton.setEnabled(true);
		attempts = params.getAttemptsRemaining();
		if (attempts > 0) {
			if (attempts < 9999) {
				params.setAttemptsRemaining(--attempts);
				rtextField.setText("" + attempts);
			}
		}
		
		if (attempts == 0)
			captB.setEnabled(false);
		else
			captB.setEnabled(true);
		
		// earlier we add 1sec leeway for the applet to load after the timer
		// start. to avoid
		// alarm user, the duration value shown would not be over max seconds
		// allowed.
		// However, for record keeping, the actual duration is saved.
		if (duration > params.getMaxSeconds())
			textField.setText(NoDecimalPlaces.format(params.getMaxSeconds()));
		else
			textField.setText(NoDecimalPlaces.format(duration));
		
		samplingGraph.repaint();
		
		if (containingApplet != null) {
			JSObject openingWindow = (JSObject)((JSObject)JSObject.getWindow(containingApplet).getMember("opener"));
			openingWindow.call("enableSubmitForGrade", null);
			openingWindow.call("enableSave", null);
		}
	}

	private void startTimer() {
		Action stopRecordingAction = new AbstractAction() {
			private static final long serialVersionUID = 0L;

			public void actionPerformed(ActionEvent e) {
				// reportStatus(res.getString("time_passed")+"\n" );
				statusLabel.setText(res.getString("time_expired"));
				statusLabel.setVisible(true);
				playB.setEnabled(false);
				captB.setEnabled(false);
				saveButton.setEnabled(false);
				audioMeter.stop();
				samplingPanelContainer.remove(audioMeter);
				samplingPanelContainer.add(samplingGraph);
				captureAudio();
			}
		};
		// allow 1sec for leeway in case the page loads slow
		timer = new Timer(params.getMaxSeconds() * 1000 + 1000,
				stopRecordingAction);
		timer.start();
	}

	/* Daisy test code for loading media back to applet - pls do not delete */
	public void createAudioInputStream(String mediaUrl, boolean updateComponents) {
		try {
			URL url = new URL(mediaUrl);
			audioInputStream = AudioSystem.getAudioInputStream(url);
			// playB.setEnabled(true);
			long milliseconds = (long) ((audioInputStream.getFrameLength() * 1000) / audioInputStream
					.getFormat().getFrameRate());
			duration = milliseconds / 1000.0;
			System.out.println("*****createAudioInpuStream= "
					+ audioInputStream);
			System.out.println("*** duration= " + duration);
			if (updateComponents) {
				formatControls.setFormat(audioInputStream.getFormat());
				System.out.println("*** calling samplingGraph.createWaveForm");
				samplingGraph.createWaveForm(null, lines, audioInputStream);
			}
		} catch (Exception ex) {
			// doesn't matter; treat it as no input stream
			System.out.println(ex.getMessage());
		}
	}

	public Applet getContainingApplet() {
		return containingApplet;
	}

	public void setContainingApplet(Applet containingApplet) {
		this.containingApplet = containingApplet;
	}

}
