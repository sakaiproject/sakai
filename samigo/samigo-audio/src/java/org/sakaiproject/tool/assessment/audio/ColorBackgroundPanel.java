/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2008 The Sakai Foundation
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

package org.sakaiproject.tool.assessment.audio;

import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.border.SoftBevelBorder;

public class ColorBackgroundPanel
  extends JPanel
{
  ColorBackgroundPanel()
  {
    init(true);
  }

  ColorBackgroundPanel(boolean beveled)
  {
    init(beveled);
  }

  private void init(boolean beveled)
  {
    if (beveled)
    {
      setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED));

    }
    ColorModel colorModel = new ColorModel();
    setBackground(colorModel.getColor("insetColor"));// Color(0x90, 0xa1, 0xc6));//
  }

}
