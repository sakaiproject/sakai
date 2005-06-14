import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Line2D;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import java.util.Vector;
import java.util.Enumeration;
import java.io.*;
import javax.sound.sampled.*;
import java.awt.font.*;
import java.text.*;
import java.net.*;
import java.util.*;
import mindbright.ssh.*;
import mindbright.*;
import mindbright.ssh.SSHSCP;
import mindbright.ssh.SSHPasswordAuthenticator;


/**
 *  CapturePlayback application.  Records audio in different formats
 *  and then playback the recorded audio.  The captured audio can
 *  be saved either as a WAVE, AU or AIFF and transferred to server via FTP
 *
 * Based on JavaSoundApplet by Brian Lichtenwalter (Sun Microsystems)
 * @version @(#)CapturePlayback.java	1.0	08/29/00
 *
 * @author Scott Stocker
 * @author Ed Smiley props files and other mods
 */


/* Secure Copy  is accomplished using MindBright's MindTerm package.*/
/* GUI and SSH modifications by Vassil Chatalbashev*/



public class CapturePlayback extends JPanel implements ActionListener, ControlContext {

    final int bufSize = 16384;

    FormatControls formatControls = new FormatControls();
    Capture capture = new Capture();
    Playback playback = new Playback();

    AudioInputStream audioInputStream;
    SamplingGraph samplingGraph;

    JButton playB, captB, pausB, loadB;
    JButton auB, aiffB, waveB;
    JTextField textField;
    JPanel p1, p2, labelPanel;
    JLabel label;
    EmptyBorder eb = new EmptyBorder(5,5,5,5);
    SoftBevelBorder sbb = new SoftBevelBorder(SoftBevelBorder.LOWERED);

    SSHSCP sshScp  = null; //needs to be global for threads to access it

    protected  ImageIcon recordIconUp = null;
    protected  ImageIcon recordIconDown = null;
    protected  ImageIcon recordIconNormal = null;
    protected  ImageIcon stopIconUp = null;
    protected  ImageIcon stopIconDown =null;
    protected  ImageIcon stopIconNormal = null;
    protected  ImageIcon playIconUp = null;
    protected  ImageIcon playIconDown = null;
    protected  ImageIcon playIconNormal = null;
    protected  ImageIcon submitIconNormal = null;
    protected  ImageIcon submitIconDown = null;
    protected  ImageIcon submitIconUp = null;



    /*
     * imageUrl, sshHost, filename, appName, and numtrys
     * are passed in as applet parameters.
     *
     * SSH_USER and SSH_PASSWORD are pulled from properties file
     */

    protected String imageUrl = "http://coursework.stanford.edu/applet/images/";
    protected String sshHost = "coursework.stanford.edu";
    protected String sshDir = "/usr/local/coursework/upload/tmp";
    protected String fileName  = "testfile.au";
    protected String appName = "CourseWork";

    // default settings, should be overridden unless there is a ghost in the machine.
    protected static String SSH_USER = "casper";
    protected static String SSH_PASSWORD = "ghost";

    protected static final AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
    protected static final float rate = 16000;			// 8000, 11025, 16000, 22050, 44100
    protected static final int sampleSize = 8;			//	8 or 16 bits
    protected static final String signedString = "signed";	// signed or unsigned
    protected static final boolean bigEndian = true;		// no idea what this does
    protected static final int channels = 1;			// 1 = mono, 2 = stereo


    String errStr;
    double duration, seconds, maxSeconds;
    int tryCounter = 0;
    int numTrysInt = 0;
    File file;
    Vector lines = new Vector();

    public CapturePlayback(String fileName, String timeLimit, String numTrys,
      String sshHost, String sshDir, String imageUrl, String appName) {

        this.fileName     = fileName;
        this.sshHost      = sshHost;
        this.sshDir       = sshDir;
        this.imageUrl     = imageUrl;
        this.appName      = appName;

        System.out.println("CapturePlayback image url=" + imageUrl);

        try {
            maxSeconds = Double.valueOf(timeLimit).doubleValue();
        }
        catch (NumberFormatException ex) {
            reportStatus(ex.toString());
        }

        try {
            numTrysInt = Integer.parseInt(numTrys);
        }
        catch (NumberFormatException ex) {
            reportStatus(ex.toString());
        }

        //----- Load all images
        URL recordUpUrl = null;
        URL recordDownUrl = null;
        URL recordNormalUrl = null;
        URL stopUpUrl = null;
        URL stopDownUrl= null;
        URL stopNormalUrl = null;
        URL playUpUrl = null;
        URL playDownUrl = null;
        URL playNormalUrl = null;
        URL submitNormalUrl = null;
        URL submitUpUrl = null;
        URL submitDownUrl = null;

        try{
            recordUpUrl = new URL(imageUrl+"RecordUp.gif");
            recordDownUrl = new URL(imageUrl+"RecordDown.gif");
            recordNormalUrl = new URL(imageUrl+"RecordNormal.gif");
            stopUpUrl =new URL(imageUrl+"StopUp.gif");
            stopDownUrl =new URL(imageUrl+"StopDown.gif");
            stopNormalUrl =new URL(imageUrl+"StopNormal.gif");
            playUpUrl= new URL( imageUrl+"PlayUp.gif");
            playDownUrl= new URL( imageUrl+"PlayDown.gif");
            playNormalUrl= new URL( imageUrl+"PlayNormal.gif");
            submitNormalUrl = new URL(imageUrl +"SubmitNormal.gif");
            submitUpUrl = new URL(imageUrl +"SubmitUp.gif");
            submitDownUrl = new URL(imageUrl +"SubmitDown.gif");
        }catch (Exception e){
            System.err.println("Failed to load button images!");
        }

        recordIconUp = new ImageIcon(recordUpUrl);
        recordIconNormal = new ImageIcon(recordNormalUrl);
        recordIconDown = new ImageIcon(recordDownUrl);

        stopIconUp = new ImageIcon(stopUpUrl);
        stopIconDown = new ImageIcon(stopDownUrl);
        stopIconNormal = new ImageIcon(stopNormalUrl);

        playIconUp = new ImageIcon(playUpUrl);
        playIconDown = new ImageIcon(playDownUrl);
        playIconNormal = new ImageIcon(playNormalUrl);


        submitIconUp = new ImageIcon(submitUpUrl);
        submitIconDown = new ImageIcon(submitDownUrl);
        submitIconNormal = new ImageIcon(submitNormalUrl);


        //----- end load of images----------

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(eb);


        p2 = new JPanel();
        p2.setBorder(sbb);
        p2.setLayout(new BorderLayout(2,2));

        JPanel buttonsPanel = new JPanel();

        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER,0, 1));
        buttonsPanel.setMaximumSize(new Dimension(300, 50));

        //RECORD BUTTON
        captB = addButton(null, buttonsPanel, true);

        captB.setMargin(new Insets(0,0,0,0));
        captB.setBorderPainted(false);
        captB.setFocusPainted(false);
        captB.setContentAreaFilled(false);

        captB.setIcon(recordIconNormal);
        captB.setPressedIcon(recordIconDown);
        captB.setRolloverIcon(recordIconUp);

        //PLAY BUTTON
        playB = addButton(null, buttonsPanel, false);

        buttonsPanel.add(playB);
        playB.setIcon(playIconNormal);
        playB.setPressedIcon(playIconDown);
        playB.setRolloverIcon(playIconUp);

        playB.setMargin(new Insets(0,0,0,0));
        playB.setBorderPainted(false);
        playB.setFocusPainted(false);
        playB.setContentAreaFilled(false);

        //SUBMIT BUTTON
        auB = addButton(null, buttonsPanel, false);

        auB.setMargin(new Insets(0,0,0,0));
        auB.setBorderPainted(false);
        auB.setFocusPainted(false);
        auB.setContentAreaFilled(false);
        auB.setIcon(submitIconNormal);
        auB.setPressedIcon(submitIconDown);
        auB.setRolloverIcon(submitIconUp);

        auB.setMaximumSize(new Dimension(50, 10));
        captB.setMaximumSize(new Dimension(50, 10));
        playB.setMaximumSize(new Dimension(50, 10));


        p2.add(buttonsPanel, BorderLayout.NORTH);

        JPanel samplingPanel = new JPanel(new BorderLayout());
        eb = new EmptyBorder(3,3,3,3);
        samplingPanel.setBorder(new CompoundBorder(eb, sbb));
        samplingPanel.add(samplingGraph = new SamplingGraph());
        p2.add(samplingPanel, BorderLayout.CENTER);

        JPanel savePanel = new JPanel();
        savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));

        labelPanel = new JPanel();
        labelPanel.setBorder(BorderFactory.createEtchedBorder());
        labelPanel.setLayout(new BoxLayout(labelPanel,BoxLayout.Y_AXIS));
        JPanel labelBpanel = new JPanel();
        label = new JLabel(formatTextForLabel(" Hit <font color=\"0000AA\"><b>'Record'</b></font> <font color=\"A70716\">to begin recording.</font>"));
        labelBpanel.add(label);
        labelPanel.add(labelBpanel);

        p2.add(labelPanel, BorderLayout.SOUTH);
        //p1.add(p2);
        add(p2);


    }

    public String formatTextForLabel(String text){
        return ("<html><font size=\"2\" face=\"arial, helvetica\" color=\"A70716\"><B>"+text+"</B></font></html>");
    }


    public void open() { }


    public void close() {
        if (playback.thread != null) {
            playB.doClick(0);
        }
        if (capture.thread != null) {
            captB.doClick(0);
        }
    }


    private JButton addButton(String name, JPanel p, boolean state) {
        JButton b = new JButton(name);
        b.addActionListener(this);
        b.setEnabled(state);
        p.add(b);
        return b;
    }

    private void showSaveMessage() {
        URL stopURL = null;
        p2.removeAll();
        try{
            stopURL = new URL (imageUrl +"stopsign.gif");
        }catch(Exception e){
            System.err.println("Failed to load image!");
        }
        ImageIcon stopSign = new ImageIcon(stopURL);
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.setBorder(sbb);


        JPanel temp = new JPanel();
        temp.setLayout(new BorderLayout(3,3));



        temp.add(new JLabel(stopSign), BorderLayout.EAST);


        JLabel saveLabel = new JLabel("");
        saveLabel.setVerticalAlignment(SwingConstants.CENTER);
        saveLabel.setHorizontalAlignment(SwingConstants.CENTER);
        saveLabel.setText(
          "<html><center>" +
          "<font size=\"2\" face=\"arial, helvetica\" color =\"0000AA\"><b>" +
          "Your audio answer has been recorded." +
          "</b></font></center></html>");


        JLabel saveLabel2 = new JLabel("");
        saveLabel2.setVerticalAlignment(SwingConstants.CENTER);
        saveLabel2.setHorizontalAlignment(SwingConstants.CENTER);

        saveLabel2.setText(
          "<html><center>" +
          "<font size=\"3\" face=\"arial, helvetica\" color=\"0000AA\">" +
          "Make sure you hit <b><font color = \"AA3333\">\"Save\"</font></b>" +
         "after you have COMPLETED your recording.<BR>" +
         "If you fail to do so, your answer will not be saved." +
         "</font></center></html>");

        JPanel savePanel = new JPanel();
        savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));
        savePanel.add(Box.createRigidArea(new Dimension(0, 12)));
        savePanel.add(saveLabel);
        savePanel.add(Box.createRigidArea(new Dimension(0, 10)));
        savePanel.add(saveLabel2);
        temp.add(savePanel, BorderLayout.CENTER);


        temp.add(new JLabel(stopSign), BorderLayout.WEST);
        p2.add(temp);
        p2.revalidate();
        p2.repaint();


    }

    public void actionPerformed(ActionEvent e) {

        Object obj = e.getSource();

        if (obj.equals(auB)) {
            saveToFile(fileName, AudioFileFormat.Type.AU);
            //xshowSaveMessage();
        } else if (obj.equals(aiffB)) {
            saveToFile(fileName, AudioFileFormat.Type.AIFF);
        } else if (obj.equals(waveB)) {
            saveToFile(fileName, AudioFileFormat.Type.WAVE);
        } else if (obj.equals(playB)) {
            if (playB.getIcon()== playIconNormal){
                //if (playB.getText().startsWith("Play")) {
                playback.start();
                samplingGraph.start();
                captB.setEnabled(false);
                //pausB.setEnabled(true);
                //playB.setText("Stop");
                playB.setIcon(stopIconNormal);
                playB.setPressedIcon(stopIconDown);
                playB.setRolloverIcon(stopIconUp);
            } else {
                playback.stop();
                samplingGraph.stop();
                captB.setEnabled(true);
                //pausB.setEnabled(false);

                //playB.setText("Play");
                playB.setIcon(playIconNormal);
                playB.setPressedIcon(playIconDown);
                playB.setRolloverIcon(playIconUp);
            }
        } else if (obj.equals(captB)) {

            if (captB.getIcon()== recordIconNormal){
                file = null;
                if ((numTrysInt == 0) || (tryCounter < numTrysInt)){
                    tryCounter++;
                    capture.start();
                    samplingGraph.start();
                    playB.setEnabled(false);
                    label.setText(formatTextForLabel(
                      "Hit <font color=\"0000AA\">" +
                      "<b>'Stop'</b>" +
                      " <font color=\"A70716\">to stop recording.</font>"));
                    //pausB.setEnabled(true);
                    auB.setEnabled(false);
                    //                	qaiffB.setEnabled(false);
                    //                	waveB.setEnabled(false);
                    //captB.setText("Stop");
                    captB.setIcon(stopIconNormal);
                    captB.setPressedIcon(stopIconDown);
                    captB.setRolloverIcon(stopIconUp);

                } else {
                    if (numTrysInt == 1) {
                        JOptionPane.showMessageDialog(null,
                                                      "You may only record your answer one time. Please 'Submit' your answer now.");
                    } else {
                        JOptionPane.showMessageDialog(null,
                                                      "You have exceeded the allowed number of trys and cannot record again.");
                    }
                }

            } else {
                lines.removeAllElements();
                capture.stop();
                samplingGraph.stop();
                playB.setEnabled(true);

                auB.setEnabled(true);

                captB.setIcon(recordIconNormal);
                captB.setPressedIcon(recordIconDown);
                captB.setRolloverIcon(recordIconUp);

                if ((numTrysInt > 0) && (tryCounter == (numTrysInt - 1))) {
                    JOptionPane.showMessageDialog(null,
                    "Notice: You will be allowed only one more attempt to " +
                    "re-record your answer.");
                }
                label.setText(formatTextForLabel(
                  "Hit <font color=\"0000AA\">'Play'</font>" +
                  " <font color=\"A70716\">to review.  " +
                  "Hit</font> <font color=\"0000AA\">'Submit'</font>" +
                  " <font color=\"A70716\">to upload your recording.</font>"));
            }
        } else if (obj.equals(pausB)) {
            if (pausB.getText().startsWith("Pause")) {
                if (capture.thread != null) {
                    capture.line.stop();
                } else {
                    if (playback.thread != null) {
                        playback.line.stop();
                    }
                }
                pausB.setText("Resume");
            } else {
                if (capture.thread != null) {
                    capture.line.start();
                } else {
                    if (playback.thread != null) {
                        playback.line.start();
                    }
                }
                pausB.setText("Pause");
            }
        }
    }


    public void createAudioInputStream(File file, boolean updateComponents) {
        if (file != null && file.isFile()) {
            try {
                this.file = file;
                errStr = null;
                audioInputStream = AudioSystem.getAudioInputStream(file);
                playB.setEnabled(true);
                fileName = file.getName();
                long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / audioInputStream.getFormat().getFrameRate());
                duration = milliseconds / 1000.0;
                auB.setEnabled(true);
 //               aiffB.setEnabled(true);
 //               waveB.setEnabled(true);
                if (updateComponents) {
                    formatControls.setFormat(audioInputStream.getFormat());
                    samplingGraph.createWaveForm(null);
                }
            } catch (Exception ex) {
                reportStatus(ex.toString());
            }
        } else {
            reportStatus("Audio file required.");
        }
    }

/*
UPLOAD FILE VIA SSH TO SERVER
*/
    public void saveToFile(String name, AudioFileFormat.Type fileType){


        if (audioInputStream == null) {
            reportStatus("No loaded audio to save");
            return;
        }

        final  JPanel progressWindow = new UploadProgress();
        final AudioFileFormat.Type localFileType = fileType;
        p2.removeAll();
        p2.add(progressWindow);
        progressWindow.repaint();

        Thread copyThread = new Thread(){

                public void run(){

                    if (file != null){
                        createAudioInputStream(file, false);
                    }

                    try {
                        audioInputStream.reset();
                    } catch (Exception e) {
                        reportStatus("Unable to reset stream " + e);
                        return;
                    }

                    try {
                      InputStream in =
                        this.getClass().getResourceAsStream("audio.properties");
                      Properties props = new Properties();
                      props.load(in);
                      SSH_USER = (String) props.get("user");
                      SSH_PASSWORD = (String) props.get("password");
                    }
                    catch (Exception ex) {
                      reportStatus("Unable to locate user " + ex);
                    }

                    SSHPasswordAuthenticator auth = new SSHPasswordAuthenticator(SSH_USER, SSH_PASSWORD);
                    try {
                        sshScp = new SSHSCP(sshHost, 22, auth, null, false, false);
                    }catch (IOException e){
                        e.printStackTrace();
                        reportStatus(e.toString());
                    }

                    sshScp.setIndicator((mindbright.ssh.SSHSCPIndicator)progressWindow);

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    //write to intermediate output stream so that filetype information is written too;
                    boolean error = false;
                    try{
                        AudioSystem.write(audioInputStream, localFileType, out);
                    }catch (Exception e){
                        e.printStackTrace();
                        reportStatus(e.toString());
                    }

                    byte audioBytes[] = out.toByteArray();
                    int length = out.size();

                    try{
                        sshScp.courseworkCopy(new ByteArrayInputStream(audioBytes), sshDir +"/"+fileName, fileName, length);
                    }catch  (Exception e){
                        //LOG.debug("here is a good place");
                        error = true;
                        e.printStackTrace();
                        reportStatus(e.toString());
                    }

                    //samplingGraph.repaint();

                    try{
                        sleep(200);
                    }catch (Exception e){
                    }
                    //   showSaveMessage();
                    if (!error)showSaveMessage();
                    else
                        {//display error window;

                            showErrorWindow();
                        }
                }
            };

        copyThread.start();

    }
    protected void showErrorWindow(){
        p2.removeAll();
        p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
        p2.setBorder(sbb);


        JPanel temp = new JPanel();
        temp.setLayout(new BorderLayout(3,3));


        //add  LEFT filelr
        temp.add(Box.createRigidArea(new Dimension(40, 0 )), BorderLayout.EAST);


        JLabel saveLabel = new JLabel("");
        saveLabel.setVerticalAlignment(SwingConstants.CENTER);
        saveLabel.setHorizontalAlignment(SwingConstants.CENTER);
        saveLabel.setText(
        "<html><center><font size=\"2\" face=\"arial, helvetica\" " +
        "color =\"000000\"><b>" + appName +
        " encountered a problem uploading your answer! " +
        "Please contact the <font color =0000AA>" + appName +
        "</FONT> administrator.</b></font></center></html>");


        JPanel savePanel = new JPanel();
        savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));
        savePanel.add(Box.createRigidArea(new Dimension(0, 52)));
        savePanel.add(saveLabel);
        temp.add(savePanel, BorderLayout.CENTER);
        temp.add(Box.createRigidArea(new Dimension(20, 0)), BorderLayout.EAST);
        p2.add(temp);
        p2.revalidate();
        p2.repaint();
    }


    private void reportStatus(String msg) {
        if ((errStr = msg) != null) {
            System.err.println(errStr);
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
            thread.setName("Playback");
            thread.start();
        }

        public void stop() {
            thread = null;
        }
        private void doClickShutDown(String message){

            if ((errStr=message)!=null){
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            thread = null;
            samplingGraph.stop();
            playB.doClick();
        }

        private void shutDown(String message) {
            if ((errStr = message) != null) {
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            if (thread != null) {
                thread = null;
                samplingGraph.stop();
                captB.setEnabled(true);
//                pausB.setEnabled(false);

                playB.setIcon(playIconNormal);
                playB.setPressedIcon(playIconDown);
                playB.setRolloverIcon(playIconUp);
            } else{
                playB.setIcon(playIconNormal);
                playB.setPressedIcon(playIconDown);
                playB.setRolloverIcon(playIconUp);
            }
        }

        public void run() {

            // reload the file if loaded by file
            if (file != null) {
                createAudioInputStream(file, false);
            }

            // make sure we have something to play
            if (audioInputStream == null) {
                shutDown("No loaded audio to play back");
                return;
            }
            // reset to the beginnning of the stream
            try {
                audioInputStream.reset();
            } catch (Exception e) {
                shutDown("Unable to reset the stream\n" + e);
                return;
            }

            // get an AudioInputStream of the desired format for playback
            AudioFormat format = formatControls.getFormat();
            AudioInputStream playbackInputStream = AudioSystem.getAudioInputStream(format, audioInputStream);

            if (playbackInputStream == null) {
                shutDown("Unable to convert stream of format " + audioInputStream + " to format " + format);
                return;
            }

            // define the required attributes for our line,
            // and make sure a compatible line is supported.

            DataLine.Info info = new DataLine.Info(SourceDataLine.class,
                format);
            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the source data line for playback.

            try {
                line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format, bufSize);
            } catch (LineUnavailableException ex) {
                doClickShutDown("Please wait for the media in the main testing window to finish playing.");
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
                    while (numBytesRemaining > 0 ) {
                        numBytesRemaining -= line.write(data, 0, numBytesRemaining);
                    }
                } catch (Exception e) {
                    shutDown("Error during playback: " + e);
                    break;
                }
            }
            // we reached the end of the stream.  let the data play out, then
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
            thread.setName("Capture");
            thread.start();
        }

        public void stop() {
            thread = null;
        }

        //this is a shutdown method which instead of explicitly setting the icons of the appropriate buttons, performs a doClick.

        private void doClickShutDown(String message){
            if ((errStr=message)!=null){
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            thread = null;
            samplingGraph.stop();
            captB.doClick();
        }

        private void shutDown(String message) {

            System.out.println("shutting down");
            if ((errStr = message) != null) {
                System.err.println(errStr);
                samplingGraph.repaint();
            }
            System.out.println("thread is..." +thread);
            if (thread != null) {
                thread = null;
                samplingGraph.stop();
                playB.setEnabled(true);
//                pausB.setEnabled(false);

                captB.setIcon(recordIconNormal);
                captB.setPressedIcon(recordIconDown);
                captB.setRolloverIcon(recordIconUp);

            } else{
                captB.setIcon(recordIconNormal);
                captB.setPressedIcon(recordIconDown);
                captB.setRolloverIcon(recordIconUp);

            }
        }

        public void run() {


            duration = 0;
            audioInputStream = null;

            // define the required attributes for our line,
            // and make sure a compatible line is supported.

            AudioFormat format = formatControls.getFormat();
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                format);

            if (!AudioSystem.isLineSupported(info)) {
                shutDown("Line matching " + info + " not supported.");
                return;
            }

            // get and open the target data line for capture.

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format, line.getBufferSize());
            } catch (LineUnavailableException ex) {
                if (tryCounter > 0 ) tryCounter--;

                doClickShutDown("Please wait for the media in the main testing window to finish playing.");
                return;

            } catch (SecurityException ex) {
                shutDown(ex.toString());
                showInfoDialog();
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

            while (thread != null) {
                if((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
                    break;
                }
                out.write(data, 0, numBytesRead);
            }

            // we reached the end of the stream.  stop and close the line.
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
            audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

            long milliseconds = (long)((audioInputStream.getFrameLength() * 1000) / format.getFrameRate());
            duration = milliseconds / 1000.0;

            try {
                audioInputStream.reset();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            samplingGraph.createWaveForm(audioBytes);
        }
    } // End class Capture


    /**
     * Controls for the AudioFormat.
     */

    class FormatControls extends JPanel {

        Vector groups = new Vector();
        JToggleButton linrB, ulawB, alawB, rate8B, rate11B, rate16B, rate22B, rate44B;
        JToggleButton size8B, size16B, signB, unsignB, litB, bigB, monoB,sterB;

        public AudioFormat getFormat() {
               return new AudioFormat(encoding, rate, sampleSize,
                          channels, (sampleSize/8)*channels, rate, bigEndian);
        }


        public void setFormat(AudioFormat format) {
            AudioFormat.Encoding type = format.getEncoding();
            if (type == AudioFormat.Encoding.ULAW) {
                ulawB.doClick();
            } else if (type == AudioFormat.Encoding.ALAW) {
                alawB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_SIGNED) {
                linrB.doClick(); signB.doClick();
            } else if (type == AudioFormat.Encoding.PCM_UNSIGNED) {
                linrB.doClick(); unsignB.doClick();
            }
            float rate = format.getFrameRate();
            if (rate == 8000) {
                rate8B.doClick();
            } else if (rate == 11025) {
                rate11B.doClick();
            } else if (rate == 16000) {
                rate16B.doClick();
            } else if (rate == 22050) {
                rate22B.doClick();
            } else if (rate == 44100) {
                rate44B.doClick();
            }
            switch (format.getSampleSizeInBits()) {
                case 8  : size8B.doClick(); break;
                case 16 : size16B.doClick(); break;
            }
            if (format.isBigEndian()) {
                bigB.doClick();
            } else {
                litB.doClick();
            }
            if (format.getChannels() == 1) {
                monoB.doClick();
            } else {
                sterB.doClick();
            }
        }
    } // End class FormatControls


    /**
     * Render a WaveForm.
     */
    class SamplingGraph extends JPanel implements Runnable {

        private Thread thread;
        private Font font10 = new Font("serif", Font.PLAIN, 10);
        private Font font12 = new Font("serif", Font.PLAIN, 12);
        private Font font14 = new Font("serif", Font.PLAIN, 14);
        private Font font16 = new Font("serif", Font.PLAIN, 16);
        private Font font24 = new Font("serif", Font.PLAIN, 24);
        Color jfcBlue = new Color(204, 204, 255);
        Color pink = new Color(255, 175, 175);


        public SamplingGraph() {
            setPreferredSize(new Dimension(358, 60));
            setBackground(new Color(20, 20, 20));
        }


        public void createWaveForm(byte[] audioBytes) {

            lines.removeAllElements();  // clear the old vector

            AudioFormat format = audioInputStream.getFormat();
            if (audioBytes == null) {
                try {
                    audioBytes = new byte[
                                          (int)(audioInputStream.getFrameLength()
                                                * format.getFrameSize())];
                    audioInputStream.read(audioBytes);
                } catch (Exception ex) {
                    reportStatus(ex.toString());
                    return;
                }
            }

            Dimension d = getSize();

            //LOG.debug("height:" +d.height + "widht:  "+d.width);
            int w = d.width;
            int h = d.height-15; //???
            int[] audioData = null;
            if (format.getSampleSizeInBits() == 16) {
                 int nlengthInSamples = audioBytes.length / 2;
                 audioData = new int[nlengthInSamples];
                 if (format.isBigEndian()) {
                    for (int i = 0; i < nlengthInSamples; i++) {
                         /* First byte is MSB (high order) */
                         int MSB = (int) audioBytes[2*i];
                         /* Second byte is LSB (low order) */
                         int LSB = (int) audioBytes[2*i+1];
                         audioData[i] = MSB << 8 | (255 & LSB);
                     }
                 } else {
                     for (int i = 0; i < nlengthInSamples; i++) {
                         /* First byte is LSB (low order) */
                         int LSB = (int) audioBytes[2*i];
                         /* Second byte is MSB (high order) */
                         int MSB = (int) audioBytes[2*i+1];
                         audioData[i] = MSB << 8 | (255 & LSB);
                     }
                 }
             } else if (format.getSampleSizeInBits() == 8) {
                 int nlengthInSamples = audioBytes.length;
                 audioData = new int[nlengthInSamples];
                 if (format.getEncoding().toString().startsWith("PCM_SIGN")) {
                     for (int i = 0; i < audioBytes.length; i++) {
                         audioData[i] = audioBytes[i];
                     }
                 } else {
                     for (int i = 0; i < audioBytes.length; i++) {
                         audioData[i] = audioBytes[i] - 128;
                     }
                 }
            }

            int frames_per_pixel = audioBytes.length / format.getFrameSize()/w;
            byte my_byte = 0;
            double y_last = 0;
            int numChannels = format.getChannels();
            for (double x = 0; x < w && audioData != null; x++) {
                int idx = (int) (frames_per_pixel * numChannels * x);
                if (format.getSampleSizeInBits() == 8) {
                     my_byte = (byte) audioData[idx];
                } else {
                     my_byte = (byte) (128 * audioData[idx] / 32768 );
                }
                //System.out.print(" byte:"+my_byte);
                double y_new = (double) (h * (128 - my_byte) / 256);
                //System.out.print(" height:"+ y_new);
                lines.add(new Line2D.Double(x, y_last, x, y_new));
                y_last = y_new;
            }

            repaint();
        }


        public void paint(Graphics g) {

            Dimension d = getSize();
            int w = d.width;
            int h = d.height;
            int INFOPAD = 15;

            Graphics2D g2 = (Graphics2D) g;
            g2.setBackground(getBackground());
            g2.clearRect(0, 0, w, h);
            g2.setColor(Color.white);
            g2.fillRect(0, h-INFOPAD, w, INFOPAD);

            if (errStr != null) {
                g2.setColor(jfcBlue);
                g2.setFont(new Font("serif", Font.BOLD, 13));
                g2.drawString("ERROR", 5, 15);
                g2.setColor(Color.white);
                AttributedString as = new AttributedString(errStr);
                as.addAttribute(TextAttribute.FONT, font12, 0, errStr.length());
                AttributedCharacterIterator aci = as.getIterator();
                FontRenderContext frc = g2.getFontRenderContext();
                LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
                float x = 5, y = 20;
                lbm.setPosition(0);
                while (lbm.getPosition() < errStr.length()) {
                    TextLayout tl = lbm.nextLayout(w-x-5);
                    if (!tl.isLeftToRight()) {
                        x = w - tl.getAdvance();
                    }
                    tl.draw(g2, x, y += tl.getAscent());
                    y += tl.getDescent() + tl.getLeading();
                }
            } else if (capture.thread != null) {
                g2.setColor(Color.black);
                g2.setFont(font12);
                //String tryStr;
                //	if (numTrysInt ==
                g2.drawString("Time (sec): " + String.valueOf(seconds), 3, h-4);

            } else if (audioInputStream != null) {


                    // .. render sampling graph ..
                    g2.setColor(jfcBlue);
                    for (int i = 1; i < lines.size(); i++) {
                        g2.draw((Line2D) lines.get(i));
                    }

                    // .. draw current position ..
                    if (seconds != 0) {
                        double loc = seconds/duration*w;
                        g2.setColor(pink);
                        g2.setStroke(new BasicStroke(3));
                        g2.draw(new Line2D.Double(loc, 0, loc, h-INFOPAD-2));
                    }
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("Length (sec): " + String.valueOf(duration) +
                              "        Time Limit (sec): " + String.valueOf(maxSeconds), 3, h-4);
            }

            else {
                g2.setColor(Color.black);
                g2.setFont(font12);
                g2.drawString("Time Limit (sec): " + String.valueOf(maxSeconds), 3, h-4);
            }
        }

        public void start() {
            thread = new Thread(this);
            thread.setName("SamplingGraph");
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
                if ((playback.line != null) && (playback.line.isOpen()) ) {

                    long milliseconds = (long)(playback.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                } else if ( (capture.line != null) && (capture.line.isActive()) ) {

                    long milliseconds = (long)(capture.line.getMicrosecondPosition() / 1000);
                    seconds =  milliseconds / 1000.0;
                    if (seconds > maxSeconds) {
/*

Here instead of doing captB.doClick(), we do all the actions necessary, since we need to set the labeltext prior to shutting down the thread (whereas captB.doClick() i,e, calling actionPerformed on the main window, would do it the other way around. If the thread is shut down before we do label.setText(),the java implementation of HTML tags within labels raises a null pointer exception.

*/

                        playB.setEnabled(true);
                        auB.setEnabled(true);


                        captB.setIcon(recordIconNormal);
                        captB.setPressedIcon(recordIconDown);
                        captB.setRolloverIcon(recordIconUp);

                        lines.removeAllElements();

                        if ((numTrysInt > 0) && (tryCounter == (numTrysInt - 1))) {
                            JOptionPane.showMessageDialog(null,
                                                          "Notice: You will be allowed only one more attempt to re-record your answer.");
                        }
                        label.setText(formatTextForLabel(
                         "Hit <font color =\"0000AA\">'Play'</font> " +
                         "<font color=\"A70716\"> to review.  " +
                         "Hit</font> <font color =\"0000AA\">'Submit'</font> " +
                         "<font color =\"A70716\"> to upload your recording.</font>"));


                        capture.stop();
                        samplingGraph.stop();
                    }
                }

                try { thread.sleep(100); } catch (Exception e) { break; }

                repaint();

                while ((capture.line != null && !capture.line.isActive()) ||
                       (playback.line != null && !playback.line.isOpen()))
                {
                    try { thread.sleep(10); } catch (Exception e) { break; }
                }
            }
            seconds = 0;
            repaint();
        }
    } // End class SamplingGraph


    public static void showInfoDialog() {
        final String msg =
            "When running the Java Sound demo as an applet these permissions\n" +
            "are necessary in order to load/save files and record audio :  \n\n"+
            "grant { \n" +
            "  permission java.io.FilePermission \"<<ALL FILES>>\", \"read, write\";\n" +
            "  permission javax.sound.sampled.AudioPermission \"record\"; \n" +
            "  permission java.util.PropertyPermission \"user.dir\", \"read\";\n"+
            "}; \n\n" +
            "The permissions need to be added to the .java.policy file.";
        new Thread(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(null, msg, "Applet Info", JOptionPane.INFORMATION_MESSAGE);
            }
        }).start();
    }


    /*
     * Main takes 7 args -
     * save filename, time limit in seconds, number of allowed trys, SSH host,
     * SSH Dir, imageUrl, application name for communication with user
     */
    public static void main(String[] args) {
        CapturePlayback capturePlayback = new CapturePlayback(args[0], args[1], args[2], args[3],
          args[4], args[5], args[6]);
        capturePlayback.open();
        JFrame f = new JFrame("Capture/Playback");
        f.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { System.exit(0); }
        });
        f.getContentPane().add("Center", capturePlayback);
        f.pack();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int w = 400;
        int h = 170;
        f.setLocation(screenSize.width/2 - w/2, screenSize.height/2 - h/2);
        f.setSize(w, h);
        f.show();
    }

    class UploadProgress extends JPanel implements mindbright.ssh.SSHSCPIndicator {

        long        startTime=0;
        long        lastTime=0;
        int         totTransSize=0;
        int         fileTransSize=0;
        int         curFileSize=0;
        int         lastSize=0;
        int         fileCnt=0;
        boolean     doneCopying;


        JPanel mainPanel;
        JLabel message;
        JProgressBar progress;
        JPanel progressPanel;
        JPanel upperPanel;
        JPanel lowerPanel;
        NumberFormat nf = NumberFormat.getInstance();//this will control how many decimal places to display after the percentage 'double'
        TransferInfo transferInfo;

        public UploadProgress(){
            super();
            transferInfo = new TransferInfo();

            nf.setMaximumFractionDigits(1);//to be used to trim doubles

            setLayout(new BorderLayout());

            upperPanel = new JPanel();
            upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));

            message = new JLabel(formatTextForLabel("<center>Connecting to the<font color =\"0000AA\"><i>" + appName + "</i></font> <font color =\"A70716\">server</font></center"));
            message.setVerticalAlignment(SwingConstants.CENTER);
            message.setHorizontalAlignment(SwingConstants.CENTER);
            upperPanel.add(Box.createRigidArea(new Dimension(0,25)));
            upperPanel.add(message);

            add(upperPanel, BorderLayout.NORTH);

            JPanel barContainer = new JPanel();
            barContainer.setLayout(new FlowLayout());
            progress = new JProgressBar(JProgressBar.HORIZONTAL);

            barContainer.add(Box.createRigidArea(new Dimension(25,0)));
            barContainer.add(progress);
            barContainer.add(Box.createRigidArea(new Dimension(25,0)));


            progress.setPreferredSize(new Dimension(300, 20));
            progressPanel = new JPanel();
            progressPanel.setLayout(new BoxLayout(progressPanel, BoxLayout.Y_AXIS));
            progressPanel.add(Box.createRigidArea(new Dimension(0, 20)));
            progressPanel.add(barContainer);
            // progressPanel.add(Box.createRigidArea(new Dimension(0, 25)));

            //progressPanel.add(transferInfoPanel);


            lowerPanel = new JPanel();
            lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.Y_AXIS));

            JPanel transferInfoPanel = new JPanel();

            transferInfoPanel.setBorder(new EmptyBorder(5,5,5,5));
            transferInfoPanel.add(transferInfo);

            lowerPanel.add(transferInfoPanel);
            //lowerPanel.add(Box.createRigidArea(new Dimension(0, 25)));
            add(lowerPanel, BorderLayout.SOUTH);

            //  add(Box.createRigidArea(new Dimension(0,20)), BorderLayout.NORTH);
            add(progressPanel, BorderLayout.CENTER);
            add(Box.createRigidArea(new Dimension(6, 0)), BorderLayout.EAST);
            add(Box.createRigidArea(new Dimension(6, 0)), BorderLayout.WEST);





        }

        public String formatTextForLabel(String text){
            return ("<html><font size=\"2\" face=\"arial, helvetica\" color=\"A70716\"><B>"+text+"</B></font></html>");
        }



        public void progress(int size) {
            totTransSize  += size;
            fileTransSize += size;
            if((curFileSize > 0) && ((((totTransSize - lastSize) * 100) / curFileSize) >= 1)) {
                progress.setValue(fileTransSize);
                double percent = progress.getPercentComplete();
                String percentStr = nf.format(percent*100);
                //if (percentStr.length() <4)
                //  while (percentStr.length()<4)percentStr = " "+ percentStr;
                //percentageLabel.setText(formatTextForLabel("<font color =\"0000AA\">"+ percentStr+ "% done</font>"));

                long   now    = System.currentTimeMillis();
                long   totSec = ((now - startTime) / 1000);
                double rate   = (totSec != 0 ? (((double)totTransSize / 1024) / totSec) : 0.0);
                totSec = (now - lastTime);
                if(totSec != 0) {
                    double rate2 = ((double)(totTransSize - lastSize) / 1024) / totSec;
                    rate = (rate + rate2) / 2.0;
                }
                String rateStr = nf.format(rate);
                //if (rateStr.length() < 6){
                //  while (rateStr.length() <6) rateStr =" "+ rateStr;
                //	}
                //transferInfo.setText(formatTextForLabel("Transfer rate: <font color =\"0000AA\"> " +rateStr + " kB/sec</font>"));
                lastSize = totTransSize;
                lastTime = now;

                //now update the transferinfo panel
                transferInfo.update(rateStr, percentStr);
                repaint();
            }


        }
        private double round (double val){
            val = val*10.0;
            val = Math.floor(val);
            val = val/10.0;
            return val;
        }


        public void endFile() {

            progress.setValue(curFileSize);

            message.setText(formatTextForLabel(
              "<center>Answer uploaded successfully" +
              " to the <font color =\"0000AA\"><i>" + appName +
              "</i></font> <font color=\"A70716\">server</font></center>"));
            double percent = progress.getPercentComplete();
            transferInfo.update(null, nf.format(percent*100));
            repaint();
            //percentageLabel.setText(formatTextForLabel("<font color =\"0000AA\">"+ nf.format(percent*100)+ "% done</font>"));

            //showSaveMessage();
        }
        public void startFile(String file, int size) {
            double kSize = (double)size / 1024;
            progress.setMaximum(size);
            //if(startTime == 0)
            startTime = System.currentTimeMillis();
            curFileSize   = size;
            fileTransSize = 0;
            fileCnt++;
            message.setText(formatTextForLabel(
          "<center>Uploading your answer to the <font color =\"0000AA\"><i>" +
          appName + "</i></font> <font color =\"A70716\">server</font></center>"));
        }

        public void connected(String server){
            message.setText(formatTextForLabel(
          "<center>Connected to the <font color=\"0000AA\"><i>" + appName +
          "</i></font> <font color =\"A70716\">server</font></center>"));
        }
        public void startDir(String dir){
        }
        public void endDir(){

        }

        private class TransferInfo extends JPanel{

            String transferRate ="";
            String percentDone ="";
            final Font font14 = new Font("Arial", Font.BOLD, 12);
            final Color blue  = new Color(0,0,170);
            final Color red = new Color(167,7,22);//Color.red;

            public TransferInfo(){
                super();
                setPreferredSize(new Dimension(400, 40));

            }


            //pass null for the other parameter if u only want to change one
            public void update(String transferRate, String percentDone){
                if (transferRate != null) this.transferRate = transferRate;
                if (percentDone != null) this.percentDone = percentDone;
                repaint();
                UploadProgress.this.repaint();

            }

            public void paint(Graphics g) {
                g.setFont(font14);
                g.setColor(getBackground());
                g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(blue);
                g.drawString("Transfer Rate:", 60, 20);
                g.setColor(red);
                if(transferRate.length()>0)
                    g.drawString(transferRate+ " kB/sec", 147,20);

                g.setColor (blue);
                g.drawString("Percent Done:", 225, 20);
                g.setColor(red);
                if(percentDone.length() >0)
                g.drawString(percentDone+ " %", 310,20);

            }
        }
    }
}
