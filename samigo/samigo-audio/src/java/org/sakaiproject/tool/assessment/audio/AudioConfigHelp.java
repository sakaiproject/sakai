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
import javax.swing.JOptionPane;
import java.util.ResourceBundle; 

public class AudioConfigHelp
  implements Serializable
{
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
static ResourceBundle res = AudioUtil.getInstance().getResourceBundle();
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
    String msg = res.getString("When_running_the") + "\n";

    if (!configHardware)
    {
      msg += " * " +
        res.getString("_You_must_have_a") + "\n";
    }
    if (!configApplet)
    {
      msg += " * " +
        res.getString("_You_must_have_a1") + "\n";
    }
    if (!configRecord || !configPlayback || !configFileWrite)
    {
      msg += " * " + res.getString("_Have_these") + "\n\n";
      msg += res.getString("grant_") + "\n";

      if (!configRecord )
      {
        msg +=  "            " +
          res.getString("permission_javax");
      }
      if (!configPlayback)
      {
        msg += "            " +
          res.getString("permission_java_util");
      }
      if (!!configFileWrite)
      {
        msg += "            " +
          res.getString("permission_java_io");
      }

      msg += "      }; \n\n";
    }

    msg += res.getString("Please_make_sure_you") + "\n";

    // to make this work we use the trick of putting it in a static,
    // this is running in the user's JVM so shouldn't be a problem.
    message = msg;

    new Thread(new Runnable()
    {
      public void run()
      {
        JOptionPane.showMessageDialog(null, message, res.getString("Configuration_Help"),
                                      JOptionPane.INFORMATION_MESSAGE);
      }
    }).start();
  }

  public static void infoHelp()
  {
    about = res.getString("ABOUT_SAKAI_AUDIO") +
    res.getString("Portions_copyright_c") +
    res.getString("Licensed_under_the") +
    res.getString("http_opensource") +
    res.getString("Portions_Copyright_c");

    new Thread(new Runnable()
   {
     public void run()
      {
        JOptionPane.showMessageDialog(null, about, res.getString("About_the_Audio"),
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
