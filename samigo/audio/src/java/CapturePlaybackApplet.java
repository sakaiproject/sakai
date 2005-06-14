import javax.swing.JApplet;
import javax.swing.*;
/**
 * This applet will capture a sound clip and transfer it to a server via FTP.
 *
 * Parameters that can be used in the CapturePlayback.html file inside
 * the applet tag to customize applet runs :
 *            <param name="file" value="audioFileName">
 *
 *
 * Based on JavaSoundApplet by Brian Lichtenwalter (Sun Microsystems)
 * Copyright (c) 1998, 1999 by Sun Microsystems, Inc. All Rights Reserved.
 *
 * @version @(#)CapturePlaybackApplet.java	v1.0  8/29/00
 * @author Scott Stocker
 */
public class CapturePlaybackApplet extends JApplet {

    static CapturePlaybackApplet applet;
    private CapturePlayback captureSession;

    public void init() {
        applet = this;
        String fileName = "testfile.au"; // this is the default file name if none is provided
        String timeLimit = "30";         // this is the default time limit if none is provided
        String numTrys = "0";		 // this is the default number of trys to record. 0 = unlimited
        String sshHost = this.getCodeBase().getHost();
        int sshPort = this.getCodeBase().getPort();
        String sshDir = "/tmp"; // this is the default SSH director to save files.
        String imageUrl =       // default URL for applet images.
          "http://" + sshHost + ":" + sshPort + "/applets/images/";
        String appName = "Audio Upload";
        String param = null;    // holder for applet params

        // set filename from input parameter "file"
        if ((param = getParameter("file")) != null && param.length() > 0) {
            fileName = param;
        }

        // set application name from input parameter "app"
        if ((param = getParameter("app")) != null && param.length() > 0) {
            appName = param;
        }

        // set image url from input parameter "imageUrl"
        if ((param = getParameter("imageUrl")) != null && param.length() > 0) {
            imageUrl = "http://" + sshHost  + ":" + sshPort +  "/" + param + "/";
        }

        // set timeLimit from input parameter "seconds"
        if ((param = getParameter("seconds")) != null && param.length() > 0) {
            int timeLimitInt = 0;
            try {
                timeLimitInt = Integer.parseInt(param);
            }
            catch (NumberFormatException ex) {};
            if (timeLimitInt > 1) { // if param is an integer greater than 1, set the time limit
                timeLimit = param;
            }
        }

        // set numTrys from input parameter "limit"
        if ((param = getParameter("limit")) != null && param.length() > 0) {
            int numTrysInt = 0;
            try {
                numTrysInt = Integer.parseInt(param);
            }
            catch (NumberFormatException ex) {};
            if (numTrysInt >= 1) { // if param is an integer >= 1, set the numTrys
                numTrys = param;
            }
        }

        // set sshDir from input parameter "dir"
        if ((param = getParameter("dir")) != null && param.length() > 0) {
            sshDir = param;
        }

        System.out.println("sshHost = " + sshHost);
        System.out.println("sshPort = " + sshHost);
        System.out.println("imageUrl = " + imageUrl);

        //JFrame frame = new JFrame();
        getContentPane().add("Center",
             captureSession = new CapturePlayback(fileName, timeLimit, numTrys,
                   sshHost, sshDir, imageUrl, appName));
    }

    public void start() {
        captureSession.open();
    }

    public void stop() {
        captureSession.close();
    }
}
