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

import java.applet.Applet;
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
import java.util.ResourceBundle;
import java.util.Locale;
import javax.swing.Box;
import java.awt.event.KeyEvent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


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
  static ResourceBundle res = AudioUtil.getInstance().getResourceBundle();

  Vector tabPanels = new Vector(4);
  JTabbedPane tabPane = new JTabbedPane();
  int width = 450, height = 350;
  int index;
  AudioRecorderParams params;

  /**
   *
   * @param audioDirectory
   */
  public AudioPanel(String audioDirectory, AudioRecorderParams params)
  {
    this.params = params;

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
    //p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
    p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 0, 0)));

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
    // Move help menu to right side
    menuBar.add(Box.createHorizontalGlue());

    // Help Menu, F1, H - Mnemonic
    JMenu options = (JMenu) menuBar.add(new JMenu(res.getString("Help")));
    options.setMnemonic(KeyEvent.VK_H);

    JMenuItem item = (JMenuItem) options.add(new JMenuItem(res.getString("Configuration")));
    item.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        AudioConfigHelp help = new AudioConfigHelp();
        help.configHelp();
      }
    });

    /* comment out - SAK-8436
    JMenuItem about = (JMenuItem) options.add(new JMenuItem(res.getString("About")));
    about.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        AudioConfigHelp.infoHelp();
      }
    });
    */
  }

  /**
   * Helper method.
   * @param menuBar
   */
  private void configureFileMenu(JMenuBar menuBar)
  {
    // File Menu, F - Mnemonic
    JMenu fileMenu = (JMenu) menuBar.add(new JMenu(res.getString("File")));
    fileMenu.setMnemonic(KeyEvent.VK_F);

    JMenuItem item = (JMenuItem) fileMenu.add(new JMenuItem(res.getString("Exit")));
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
    ( (AudioControlContext) tabPanels.get(index)).close();
  }

  public void open()
  {
    ( (AudioControlContext) tabPanels.get(index)).open();
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(width, height);
  }

  /**
   * Lazy load the tabbed pane
   */
  public void run()
  {
    EmptyBorder eb = new EmptyBorder(5, 5, 5, 5);
    BevelBorder bb = new BevelBorder(BevelBorder.LOWERED);
    CompoundBorder cb = new CompoundBorder(eb, bb);
    JPanel p = new JPanel(new BorderLayout());
    //p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 90, 0)));
    p.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 0, 0)));
    AudioRecorder recorder = new AudioRecorder(params);
    recorder.setContainingApplet((Applet)this.getParent().getParent().getParent().getParent());
    tabPanels.add(recorder);
    p.add(recorder);
    tabPane.addTab(res.getString("Audio_Recorder"), p);

    /* samigo 2.2 did not asked for advnacd settings so commented it out 
    JPanel fp = new JPanel(new BorderLayout());
    fp.setBorder(new CompoundBorder(cb, new EmptyBorder(0, 0, 0, 0)));
    JPanel format = recorder.getFormatControlsPanel();
    tabPanels.add(format);
    fp.add(format);
    tabPane.addTab(res.getString("Advanced_Settings"), fp);
    */
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
      if (file == null || !file.isDirectory())
      {
        System.out.println(res.getString("usage_java_JavaSound"));
      }
      else
      {
        media = args[0];
      }
    }

    AudioRecorderParams params = new AudioRecorderParams();

    final AudioPanel tabPanel = new AudioPanel(media, params);
    JFrame f = new JFrame(res.getString("Audio_Recorder"));
    f.addWindowListener(new WindowAdapter()
    {
      public void windowClosing(WindowEvent e)
      {
        System.exit(0);
      }

      public void windowDeiconified(WindowEvent e)
      {
        tabPanel.open();
      }

      public void windowIconified(WindowEvent e)
      {
        tabPanel.close();
      }
    });
    f.getContentPane().add("Center", tabPanel);
    f.pack();
    Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
    f.setLocation(d.width / 2 - tabPanel.width / 2, d.height / 2 - tabPanel.height / 2);
    f.setSize(new Dimension(tabPanel.width, tabPanel.height));
    f.setVisible(true);
  }
}
