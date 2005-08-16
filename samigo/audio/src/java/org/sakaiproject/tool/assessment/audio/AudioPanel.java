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
/* AudioPanel.java
 * Originally based on code from JavaSound.java
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

import java.io.File;
import java.util.Vector;

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
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/**
 * The Java Sound Samples : MidiSynth, Juke, CapturePlayback, Groove.
 *
 * @version @(#)JavaSound.java	1.15 00/01/31
 * @author Brian Lichtenwalter
 */
public class AudioPanel
  extends JPanel
  implements ChangeListener, Runnable
{
  Vector demos = new Vector(4);
  JTabbedPane tabPane = new JTabbedPane();
  int width = 760, height = 500;
  int index;

  /**
   *
   * @param audioDirectory
   */
  public AudioPanel(String audioDirectory)
  {

    setLayout(new BorderLayout());

    JMenuBar menuBar = new JMenuBar();

    if (isStandalone())
    {
      configureFileMenu(menuBar);
    }
    configureHelpMenu(menuBar);
    add(menuBar, BorderLayout.NORTH);

    tabPane.addChangeListener(this);

    EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
    BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
    CompoundBorder cb = new CompoundBorder(eb, bb);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));

    new Thread(this).start();

    add(tabPane, BorderLayout.CENTER);
  }

  private boolean isStandalone()
  {
    if (AudioRecorderApplet.applet == null || AudioRecorderApplet.applet.isStandalone)
    {
      return true;
    }

    return false;
  }

  /**
   * Helper method.
   * @param menuBar
   */
  private void configureHelpMenu(JMenuBar menuBar)
  {
    JMenu options = (JMenu) menuBar.add(new JMenu("Help"));
    JMenuItem item = (JMenuItem) options.add(new JMenuItem("Configuration"));
    item.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        AudioConfigHelp help = new AudioConfigHelp();
        help.configHelp();
      }
    });
    JMenuItem about = (JMenuItem) options.add(new JMenuItem("About"));
    about.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        AudioConfigHelp.infoHelp();
      }
    });
  }

  /**
   * Helper method.
   * @param menuBar
   */
  private void configureFileMenu(JMenuBar menuBar)
  {
    JMenu fileMenu = (JMenu) menuBar.add(new JMenu("File"));
    JMenuItem item = (JMenuItem) fileMenu.add(new JMenuItem("Exit"));
    item.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        System.exit(0);
      }
    });
  }

  /**
   * Handle state changes.
   * @param e the Cahnge Event
   */
  public void stateChanged(ChangeEvent e)
  {
    close();
    System.gc();
    index = tabPane.getSelectedIndex();
    open();
  }

  public void close()
  {
    ( (AudioControlContext) demos.get(index)).close();
  }

  public void open()
  {
    ( (AudioControlContext) demos.get(index)).open();
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(width, height);
  }

  /**
   * Lazy load the tabbed pane with CapturePlayback, MidiSynth and Groove.
   */
  public void run()
  {
    EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
    BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
    CompoundBorder cb = new CompoundBorder(eb, bb);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
    AudioRecorder capturePlayback = new AudioRecorder();
    demos.add(capturePlayback);
    p.add(capturePlayback);
    tabPane.addTab("Audio Recorder", p);
  }

  /**
   *
   * @param args
   */
  public static void main(String[] args)
  {
    String media = "./audio";
    if (args.length > 0)
    {
      File file = new File(args[0]);
      if (file == null && !file.isDirectory())
      {
        System.out.println("usage: java JavaSound audioDirectory");
      }
      else
      {
        media = args[0];
      }
    }

    final AudioPanel demo = new AudioPanel(media);
    JFrame f = new JFrame("Audio Recorder");
    f.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }

      public void windowDeiconified(WindowEvent e)
      {
        demo.open();
      }

      public void windowIconified(WindowEvent e)
      {
        demo.close();
      }
    });
    f.getContentPane().add("Center", demo);
    f.pack();
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation(d.width / 2 - demo.width / 2, d.height / 2 - demo.height / 2);
    f.setSize(new Dimension(demo.width, demo.height));
    f.setVisible(true);
  }
}
