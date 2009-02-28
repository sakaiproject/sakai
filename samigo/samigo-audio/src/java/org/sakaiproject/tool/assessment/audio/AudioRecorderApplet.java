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

/* AudioRecorderApplet.java
 * Originally based on code from JavaSoundApplet.java
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

import java.util.Locale;
import java.util.ResourceBundle;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JApplet;
import javax.swing.JFrame;

public class AudioRecorderApplet extends JApplet {
	static ResourceBundle res;

	boolean isStandalone = false;

	// Get a parameter value
	public String getParameter(String key, String def) {
		return isStandalone ? System.getProperty(key, def)
				: (getParameter(key) != null ? getParameter(key) : def);
	}

	// Construct the applet
	public AudioRecorderApplet() {
	}

	// Initialize the applet
	static AudioRecorderApplet applet;

	private AudioPanel demo;

	private AudioRecorderParams params;

	public void init() {
		applet = this;
		if (isStandalone) {
			params = new AudioRecorderParams();
		} else {
			params = new AudioRecorderParams(applet);
		}
		res = AudioUtil.getInstance().getResourceBundle();

                String media = "./audio";
		String param = null;
		
		//"Center" means center position
		getContentPane().add("Center",
				demo = new AudioPanel(media, params));
	}

	/*
	private void initAppletParams() {
		AudioRecorderParams params = this.params;
	}

	// Component initialization
	private void jbInit() throws Exception {
	}
*/
	// Start the applet
	public void start() {
	}

	// Stop the applet
	public void stop() {
	}

	// Destroy the applet
	public void destroy() {
	}

	// Get Applet information
	public String getAppletInfo() {
		return (res.getString("Applet_Information"));
	}

	// Get parameter info
	public String[][] getParameterInfo() {
		return null;
	}

	/**
	 * Main method. Run as an application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		AudioRecorderApplet applet = new AudioRecorderApplet();
		applet.isStandalone = true;
		JFrame f = new JFrame(res.getString("Audio_Recorder"));
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		/*
		 * old way to exit program when window is closed - daisyf, 04/06/06
		 * f.addWindowListener(new WindowAdapter() { public void
		 * windowClosing(WindowEvent e) { System.exit(0); } });
		 */
		f.getContentPane().add("Center", applet);
		f.pack();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int w = 450;
		int h = 450;
		f.setLocation(screenSize.width / 2 - w / 2, screenSize.height / 2 - h
				/ 2);
		applet.init();
		applet.start();
		f.setSize(w, h);
		f.show();
	}

	public AudioRecorderParams getParams() {
		return params;
	}
	
}
