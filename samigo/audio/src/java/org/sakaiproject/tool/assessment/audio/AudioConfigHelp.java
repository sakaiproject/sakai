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
/* AudioConfigHelp.java
 * Loosely based on code from JavaSound.java
 * @(#)JavaSound.java	1.15	00/01/31
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

import java.io.Serializable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AudioConfigHelp
  implements Serializable
{
  private static String message;
  private static String about;
  private boolean configHardware;
  private boolean configRecord;
  private boolean configPlayback;
  private boolean configApplet;
  private boolean configFileWrite;

  /**
   * This is an everything-on help message.
   */
  public AudioConfigHelp()
  {
    this.configHardware = false;
    this.configRecord = false;
    this.configPlayback = false;
    this.configApplet = false;
    this.configFileWrite = false; // we won't support for now
  }

  /**
   * Support for more sophistication, as we are able to detect some error conditions.
   * @param configHardware
   * @param configRecord
   * @param configPlayback
   * @param configApplet
   */
  public AudioConfigHelp(boolean configHardware, boolean configRecord,
                         boolean configPlayback, boolean configApplet)
  {
    this.configHardware = configHardware;
    this.configRecord = configRecord;
    this.configPlayback = configPlayback;
    this.configApplet = configApplet;
    this.configFileWrite = false; // we won't support for now
  }

  /**
   * We use the information to configure a message.
   */
  public void configHelp()
  {
    int number = 1;

    String msg = "When running the Audio Recorder\n";

    if (!configHardware)
    {
      msg += "  " + number++ +
        ") You must have a properly configured sound card and microphone.\n";
    }
    if (!configApplet)
    {
      msg += "  " + number++ +
        ") You must have a properly configured Java 1.5 plugin installed.\n";
    }
    if (!configRecord || !configPlayback || !configFileWrite)
    {
      msg += "  " + number++ + ") Have these permissions added to the .java.policy file:\n\n";
      msg += "    grant { \n";

      if (!configRecord )
      {
        msg += "      permission javax.sound.sampled.AudioPermission \"record\"; \n";
      }
      if (!configPlayback)
      {
        msg +=
          "      permission java.util.PropertyPermission \"user.dir\", \"read\";\n";
      }
      if (!!configFileWrite)
      {
        msg +=
          "      permission java.io.FilePermission \"<<ALL FILES>>\", \"read, write\";\n";
      }

      msg += "      }; \n\n";
    }

    msg += "Please make sure you correct this first before using.\n\n";

    // to make this work we use the trick of putting it in a static,
    // this is running in the user's JVM so shouldn't be a problem.
    message = msg;

    new Thread(new Runnable()
    {
      public void run()
      {
        JOptionPane.showMessageDialog(null, message, "Configuration Help",
                                      JOptionPane.INFORMATION_MESSAGE);
      }
    }).start();
  }

  public static void infoHelp()
  {
    about = "ABOUT SAKAI AUDIO RECORDER\n\n" +
    "Portions copyright (c) 2005 " +
    "The Regents of the University of Michigan,\n" +
    "Trustees of Indiana University, " +
    "Board of Trustees of the Leland Stanford, Jr., University, " +
    "and The MIT Corporation.\n\n" +
    "Licensed under the Educational Community License Version 1.0, see \n" +
    "        http://cvs.sakaiproject.org/licenses/license_1_0.html\n\n" +
    "Portions Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.\n" +
    "(non-exclusive, royalty free, license to use, modify and redistribute)";

    new Thread(new Runnable()
   {
     public void run()
      {
        JOptionPane.showMessageDialog(null, about, "About the Audio Recorder",
                                      JOptionPane.INFORMATION_MESSAGE);
      }
    }).start();
  }

  public boolean isConfigHardware()
  {
    return configHardware;
  }

  public void setConfigHardware(boolean configHardware)
  {
    this.configHardware = configHardware;
  }

  public boolean isConfigRecord()
  {
    return configRecord;
  }

  public void setConfigRecord(boolean configRecord)
  {
    this.configRecord = configRecord;
  }

  public boolean isConfigPlayback()
  {
    return configPlayback;
  }

  public void setConfigPlayback(boolean configPlayback)
  {
    this.configPlayback = configPlayback;
  }

  public boolean isConfigApplet()
  {
    return configApplet;
  }

  public void setConfigApplet(boolean configApplet)
  {
    this.configApplet = configApplet;
  }

}